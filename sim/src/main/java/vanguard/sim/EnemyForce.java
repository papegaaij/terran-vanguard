package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The level's enemies: the units entering by the {@link WaveSchedule} and the units fixed to the
 * ground ({@link GroundUnit}, entering at the top edge with the scroll), flying or
 * scrolling and firing at the ship, and their bullets. What a kill earns is the {@link Sortie}'s.
 */
final class EnemyForce {
    private static final int ENEMY_CAPACITY = 192;
    /** M5 part D: swarms in flight. */
    private static final int FLOCK_CAPACITY = 8;

    static final int BULLET_CAPACITY = 256;

    /** What the sortie does when a unit leaves the screen alive (just before it is gone). */
    interface Escapes {
        void escaped(Enemy enemy);

        /**
         * A spawner burst on its own (design/enemies/air/brood-pod), just before it is gone: its
         * units are released already.
         */
        default void burst(Enemy enemy) {
            escaped(enemy);
        }

        /**
         * M5 part C: a periodic spawner (design/enemies/ground/hive-node) released {@code units}
         * units, which count among the level's enemies as they are released.
         */
        default void released(Enemy spawner, int units) {}
    }

    private final SplitMix64 rng;
    private final Rules rules;
    private final SimEvents events;
    private final Escapes escapes;
    private final List<EnemySpec> kinds;
    private final WaveSchedule waves;
    private final List<GroundUnit> groundUnits;
    private final int[] groundKinds;
    private final int[] groundTicks;
    /** M5 part E: each ground unit's current as a direction (x, y up), for a drifting raft. */
    private final double[] groundCurrentX;

    private final double[] groundCurrentY;
    /** The units in flight, each reporting its damage's events (M7 part D: aura rings, a curled shell) into the step's. */
    private final Pool<Enemy> enemies;
    /** M7 part D: whether a kind of the level has an aura (the rings are stepped only then). */
    private final boolean auras;
    /** M7 part D: whether a kind of the level has a curl-roll (its ball crushes ground targets). */
    private final boolean rollers;
    /** M7 part E: whether a kind of the level has a pull (the ship and the pickups are pulled only then). */
    private final boolean pulls;
    /** M7 part E: the pull on the ship in this step, px/s (y up); 0 without one. */
    private double pullX;

    private double pullY;

    /** The fire of the units and the bosses' parts: bullets, spores and mortar lobs. */
    private final EnemyFire guns;
    /** The target-the-objective hook. */
    private final ObjectiveHook objectiveHook;
    /** M5 part H: the root bursts. */
    private final RootBursts rootBursts;
    /** The segment chains (M7 part C: the ground's too). */
    private final ChainRules chainRules;
    /** M7 part B: the Burrower waves' stagger. */
    private final Burrowers burrowers;

    private final Pool<Flock> flocks = new Pool<>(FLOCK_CAPACITY, Flock::new, Flock[]::new);
    /** Flocks started in this attempt, for their serials. */
    private int flocksSpawned;
    /** The flock the members entering in this step join (a swarm's members enter together); null for none. */
    private Flock joining;
    /**
     * M7 part G: the pursuits' refill motes left out in this attempt because their flock had no room (or was gone):
     * planned, they count nowhere ({@link #skipped()}).
     */
    private int skipped;

    private int spawned;
    private int nextGround;
    /** M5 part F: the level's wind this step, px/s to the right; exactly 0 without weather. */
    private double wind;
    /** M5 part G: per ground unit entering from a side, the steps its edge warning starts and ends, and its edge bit. */
    private final int[] sideWarningStarts;

    private final int[] sideWarningEnds;
    private final int[] sideWarningEdges;
    /** Per enemy kind, the kind a spawner releases; -1 for others. */
    private final int[] broodKinds;
    /** The spawner that entered last, which the escorts entering after it circle. */
    private Enemy lastCarrier;
    /** M8 part D: the escorts held by a rising carrier until its rise's middle. */
    private final RiseEscorts riseEscorts = new RiseEscorts();
    /**
     * M5 part B: per staggered walker wave its volley clock, in steps since the first of its units
     * came onto the screen (counting that step as 1); 0 before (see {@link Enemy#staggeredVolley}).
     */
    private final int[] volleyClocks;
    /** M7 part B: the level's no-surface zones, which the sortie scrolls; none by default. */
    private SurfaceZones zones = SurfaceZones.NONE;
    /** M7 part C: the level's rock and the scroll now; null in a level without terrain. */
    private Canyon canyon;
    /** M7 part C: the ground's scroll now (a ground chain crawls and is placed in ground space). */
    private double groundScroll;
    /** M7 part C: the level's tendril gates; null without. */
    private Links links;
    /** M7 part C: per ground unit, its gate × 2 (+ 1 the right anchor); -1 for a unit that is no gate's anchor. */
    private int[] gateOf;
    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): per ground unit, whether it is flipped ({@link
     * TravelFrame}): one entering at the top edge with the scroll while it runs reversed comes up from the bottom.
     */
    private final boolean[] groundFlips;

    /** Plans the waves with {@code rng}, which also spreads the aimed shots later. */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes) {
        this(waveSpecs, groundUnits, List.of(), rng, rules, events, escapes);
    }

    /** @param extraKinds enemies that enter outside the waves (a boss's streams), among the kinds too */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes) {
        this(waveSpecs, groundUnits, extraKinds, rng, rules, events, escapes, wave -> Optional.empty());
    }

    /**
     * @param spans M7 part C: per wave, the canyon's flyable span at its entry row (formations laid out on
     *     it, {@link Formations.Span}); empty for the whole field
     */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes,
            java.util.function.Function<WaveSpec, Optional<Formations.Span>> spans) {
        this(waveSpecs, groundUnits, extraKinds, rng, rules, events, escapes, spans, t -> false);
    }

    /**
     * @param reversedAt M7 part G: whether the scroll runs reversed at a level time, s ({@link Sortie#travelAt}{@code
     *     < 0}): the waves at such a time and the ground units entering then are flipped ({@link TravelFrame})
     */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes,
            java.util.function.Function<WaveSpec, Optional<Formations.Span>> spans,
            java.util.function.DoublePredicate reversedAt) {
        this.rng = rng;
        this.rules = rules;
        this.events = events;
        this.escapes = escapes;
        enemies = new Pool<>(ENEMY_CAPACITY, () -> new Enemy(events), Enemy[]::new);
        List<EnemySpec> distinct = new ArrayList<>(
                waveSpecs.stream().map(WaveSpec::enemy).distinct().toList());
        for (GroundUnit unit : groundUnits) {
            if (!distinct.contains(unit.enemy())) {
                distinct.add(unit.enemy());
            }
        }
        for (EnemySpec extra : extraKinds) {
            if (!distinct.contains(extra)) {
                distinct.add(extra);
            }
        }
        // The units a spawner releases are among the kinds too (a brood, or M5 part C's periodic spawn).
        for (int k = 0; k < distinct.size(); k++) {
            EnemySpec spec = distinct.get(k);
            if (spec.brood().isPresent()
                    && !distinct.contains(spec.brood().get().enemy())) {
                distinct.add(spec.brood().get().enemy());
            }
            if (spec.spawner().isPresent()
                    && !distinct.contains(spec.spawner().get().enemy())) {
                distinct.add(spec.spawner().get().enemy());
            }
        }
        // So are a chain's segments, tail and regrown head.
        for (int k = 0; k < distinct.size(); k++) {
            EnemySpec spec = distinct.get(k);
            if (spec.chain().isPresent()) {
                EnemySpec.ChainSpec chain = spec.chain().get();
                for (EnemySpec part : java.util.Arrays.asList(chain.segment(), chain.tail(), chain.regrown())) {
                    // M7 part C: a ground chain's regrown head may be null (it never regrows).
                    if (part != null && !distinct.contains(part)) {
                        distinct.add(part);
                    }
                }
            }
        }
        kinds = List.copyOf(distinct);
        objectiveHook = new ObjectiveHook(kinds, events);
        guns = new EnemyFire(rng, rules, events, objectiveHook);
        auras = kinds.stream().anyMatch(kind -> kind.aura().isPresent());
        rollers = kinds.stream().anyMatch(kind -> kind.roll().isPresent());
        pulls = kinds.stream().anyMatch(kind -> kind.pull().isPresent());
        broodKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec spec = kinds.get(k);
            broodKinds[k] = spec.brood().isPresent()
                    ? kinds.indexOf(spec.brood().get().enemy())
                    : spec.spawner().isPresent()
                            ? kinds.indexOf(spec.spawner().get().enemy())
                            : -1;
        }
        this.waves = new WaveSchedule(waveSpecs, kinds, rng, spans, reversedAt);
        volleyClocks = new int[waves.volleyClocks()];
        burrowers = new Burrowers(waves.burrowClocks(), enemies, events, guns);
        chainRules = new ChainRules(this, kinds, waves, enemies, events, escapes, guns);
        this.groundUnits = groundUnits;
        groundKinds = new int[groundUnits.size()];
        groundTicks = new int[groundUnits.size()];
        groundCurrentX = new double[groundUnits.size()];
        groundCurrentY = new double[groundUnits.size()];
        groundFlips = new boolean[groundUnits.size()];
        for (int i = 0; i < groundUnits.size(); i++) {
            GroundUnit unit = groundUnits.get(i);
            // M7 part G: only a unit entering at the top edge with the scroll (not from a side, nor a root burst's).
            groundFlips[i] = unit.side().isEmpty() && unit.burst().isEmpty() && reversedAt.test(unit.t());
            groundKinds[i] = kinds.indexOf(groundUnits.get(i).enemy());
            groundTicks[i] = SimStep.ticks(groundUnits.get(i).t());
            groundCurrentX[i] = StrictMath.sin(groundUnits.get(i).currentRadians());
            groundCurrentY[i] = -StrictMath.cos(groundUnits.get(i).currentRadians());
        }
        rootBursts = new RootBursts(groundUnits, events);
        int sides = (int)
                groundUnits.stream().filter(unit -> unit.side().isPresent()).count();
        sideWarningStarts = new int[sides];
        sideWarningEnds = new int[sides];
        sideWarningEdges = new int[sides];
        int w = 0;
        for (GroundUnit unit : groundUnits) {
            if (unit.side().isPresent()) {
                sideWarningStarts[w] = SimStep.ticks(unit.t() - WaveSchedule.EDGE_WARNING_SECONDS);
                sideWarningEnds[w] = SimStep.ticks(unit.t());
                sideWarningEdges[w] = unit.side().get().fromLeft() ? WarningEdge.LEFT.bit() : WarningEdge.RIGHT.bit();
                w++;
            }
        }
    }

    /** The target-the-objective hook in mode {@code nearest} ({@link ObjectiveHook#hook(Convoy, List)}). */
    void hook(Convoy target, List<String> slugs) {
        objectiveHook.hook(target, slugs);
    }

    /** M5 part G: the hook of a defend primary ({@link ObjectiveHook#hook(Convoy, ObjectiveSpec.Defend)}). */
    void hook(Convoy target, ObjectiveSpec.Defend defend) {
        objectiveHook.hook(target, defend);
    }

    /** The hook of {@code target} with each enemy kind in its mode in {@code targetedBy}. */
    void hook(Convoy target, java.util.Map<String, HookMode> targetedBy) {
        objectiveHook.hook(target, targetedBy);
    }

    /** M7 part D: convoy unit {@code k} takes {@code hits} from a hooked attack this step. */
    void hitAlly(int k, double hits) {
        objectiveHook.hitAlly(k, hits);
    }

    /** M7 part D: the hits the hooked attacks dealt ground ally {@code k} since the last {@link #move} began. */
    double allyHits(int k) {
        return objectiveHook.allyHits(k);
    }

    /**
     * M5 part F: the level's wind for the next {@link #move}, px/s to the right (design/campaign Level
     * 12, Weather): it pushes the units with a latch and the Whirl Seeds.
     */
    void wind(double pxPerSecond) {
        wind = pxPerSecond;
    }

    /** Back to the level start: no enemies or bullets, the schedule at its first unit. */
    void reset() {
        Pools.clear(enemies);
        guns.reset();
        chainRules.reset();
        Pools.clear(flocks);
        flocksSpawned = 0;
        joining = null;
        skipped = 0;
        waves.reset();
        spawned = 0;
        nextGround = 0;
        lastCarrier = null;
        riseEscorts.reset();
        java.util.Arrays.fill(volleyClocks, 0);
        burrowers.reset();
        objectiveHook.clearAllyHits();
        rootBursts.reset();
    }

    /** M5 part H: the root bursts' telegraphs, before {@link #spawn} in a step ({@link RootBursts#bursts}). */
    void bursts(int levelTick, double shipX, double groundScroll, double scrollStep) {
        rootBursts.bursts(levelTick, shipX, groundScroll, scrollStep, nextGround);
    }

    /** M5 part H: how far a burst moves sideways to clear the ship ({@link RootBursts#clearOfShip}). */
    static double clearOfShip(double x, double foot, double shipX) {
        return RootBursts.clearOfShip(x, foot, shipX);
    }

    /** Lets in every unit due at {@code levelTick}, flying and on the ground. */
    void spawn(int levelTick) {
        for (Spawn spawn = waves.due(levelTick); spawn != null; spawn = waves.due(levelTick)) {
            if (spawn.enemy().chain().isPresent()) {
                chainRules.spawnChain(spawn);
                continue;
            }
            if (spawn.swarm().isPresent()) {
                spawnMember(spawn);
                continue;
            }
            if (spawn.escort().isPresent()
                    && lastCarrier != null
                    && lastCarrier.riseHolding()
                    && riseEscorts.hold(spawn, lastCarrier, spawned, waves.lastTag())) {
                // M8 part D: held unspawned until its rising carrier's layer flips (it peels off then).
                spawned++;
                continue;
            }
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                enemy.spawn(spawn, spawned);
                if (waves.lastFlipped()) {
                    // M7 part G: its wave entered while the scroll ran reversed.
                    enemy.flip();
                }
                enemy.entered(waves.lastEntry());
                enemy.tag(waves.lastTag());
                enemy.volley(waves.lastVolleyGroup(), waves.lastVolleyUnit());
                enemy.wavePlace(waves.lastPlace());
                if (spawn.enemy().burrow().isPresent()) {
                    enemy.burrowGroup(waves.lastBurrowGroup());
                    enemy.surfaceZones(zones);
                }
                if (spawn.escort().isPresent()) {
                    enemy.escort(lastCarrier);
                }
                if (objectiveHook.bombs(enemy, spawn)) {
                    // M7 part D: a front entry descends to the bomb row, a side entry keeps its entry row.
                    ObjectiveSpec.GroundAllies allies = objectiveHook.convoy().groundSpec();
                    double row =
                            waves.lastEntry() == WaveSpec.Entry.SIDES ? enemy.y() : PlayField.HEIGHT - allies.bombRow();
                    double slot = (waves.lastPlace() - (waves.lastWaveSize() - 1) / 2.0) * allies.bombSlot();
                    enemy.bombRun(row, slot, SimStep.ticks(allies.bombRunIn()));
                }
            }
            if (spawn.enemy().brood().isPresent()
                    || spawn.enemy().aura().isPresent()
                    || spawn.enemy().rise().isPresent()) {
                // M7 part D: a Herald is a carrier too; its escorts circle it inside its aura. M8 part D: so is a
                // rising unit (the Abyss Ray), which holds its escorts until its rise's middle.
                lastCarrier = enemy;
            }
            spawned++;
        }
        while (nextGround < groundTicks.length && groundTicks[nextGround] <= levelTick) {
            GroundUnit unit = groundUnits.get(nextGround);
            Enemy enemy = enemies.obtain();
            if (enemy != null && unit.burst().isPresent()) {
                rootBursts.grow(nextGround, unit, enemy, groundKinds[nextGround], spawned);
            } else if (enemy != null && unit.side().isPresent()) {
                // M5 part G: in from a side edge, drifting across (Level 13's mortar floe).
                enemy.rootAtSide(
                        unit.enemy(), groundKinds[nextGround], unit.side().get(), spawned, unit.group());
            } else if (enemy != null) {
                enemy.root(
                        unit.enemy(),
                        groundKinds[nextGround],
                        unit.x(),
                        spawned,
                        unit.group(),
                        groundCurrentX[nextGround],
                        groundCurrentY[nextGround]);
                if (groundFlips[nextGround]) {
                    // M7 part G: it comes up from the bottom edge with the reversed scroll.
                    enemy.flip();
                }
                if (gateOf != null && gateOf[nextGround] >= 0) {
                    // M7 part C: a gate's anchor: its tendril starts with the pair.
                    links.anchored(gateOf[nextGround] >> 1, (gateOf[nextGround] & 1) == 1, enemy);
                }
            }
            spawned++;
            nextGround++;
        }
        joining = null;
    }

    /**
     * M5 part D: a swarm's member enters: the first of its wave starts the flock (null when the pool
     * has none free: then the wave's members are left out), the others join it in entry order.
     */
    private void spawnMember(Spawn spawn) {
        Spawn.Swarm swarm = spawn.swarm().orElseThrow();
        if (swarm.refill()) {
            refill(spawn, swarm);
            return;
        }
        if (swarm.member() == 0) {
            joining = flocks.obtain();
            if (joining != null) {
                joining.start(
                        spawn.enemy().flock().orElseThrow(),
                        flocksSpawned++,
                        swarm.route(),
                        waves.lastEntry(),
                        waves.lastFlipped(),
                        spawn.tick() * SimStep.SECONDS);
            }
        }
        if (joining != null) {
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                enemy.join(joining, swarm.member(), spawn, spawned, swarm.x(), swarm.y(), joining.startFacing());
                if (joining.flipped()) {
                    // M7 part G: a member lives in its flock's frame.
                    enemy.flip();
                }
                enemy.entered(waves.lastEntry());
                enemy.tag(waves.lastTag());
                joining.member(swarm.member(), enemy, swarm.x(), swarm.y());
            }
        }
        spawned++;
    }

    /**
     * M7 part G (design/campaign Level 20, The pursuit swarm): a pursuit's refill mote joins its wave's flock (the
     * flock of its route) in a free slot, from the top edge at the leader point's x; with no room (or no flock) it is
     * left out and counts nowhere ({@link #skipped()}).
     */
    private void refill(Spawn spawn, Spawn.Swarm swarm) {
        Flock flock = null;
        for (int f = 0; f < flocks.size(); f++) {
            if (flocks.get(f).route() == swarm.route()) {
                flock = flocks.get(f);
                break;
            }
        }
        int slot = flock == null ? -1 : flock.freeSlot();
        Enemy enemy = slot < 0 ? null : enemies.obtain();
        if (enemy == null) {
            skipped++;
        } else {
            double x = Math.clamp(flock.leaderX() + swarm.x(), 0, PlayField.WIDTH);
            enemy.join(flock, slot, spawn, spawned, x, swarm.y(), flock.joinFacing(x, swarm.y()));
            enemy.entered(waves.lastEntry());
            enemy.tag(waves.lastTag());
            flock.join(enemy);
        }
        spawned++;
    }

    /**
     * M7 part G: the pursuits' refill motes left out so far in this attempt (their flock was full): planned, they are
     * taken off the level's units ({@link Sortie#enemyTotal()}) and so off the kill ratio's count.
     */
    int skipped() {
        return skipped;
    }

    /**
     * M5 part D: the flocks fly a step; a loop-back raises its event; once a flock has flown its
     * course its members off the play field are gone (escaped), and those still on it a while later
     * too; a flock without members ends.
     */
    void moveFlocks() {
        moveFlocks(Double.NaN, Double.NaN);
    }

    /**
     * As {@link #moveFlocks()}; M7 part G: a pursuit steps toward the ship at ({@code shipX}, {@code shipY}) and raises
     * its tell, lunge and break-off events (while it has members); NaN for none (a pursuit then holds its x).
     */
    void moveFlocks(double shipX, double shipY) {
        for (int f = flocks.size() - 1; f >= 0; f--) {
            Flock flock = flocks.get(f);
            if (flock.pursuing()) {
                flock.advance(
                        canyon,
                        Double.isNaN(shipX) ? flock.leaderX() : shipX,
                        Double.isNaN(shipY) ? flock.leaderY() : shipY);
                pursuitEvents(flock);
            } else if (flock.spec().ram().isPresent() && !Double.isNaN(shipX)) {
                // M8 part C: its rams dive at the ship (design/enemies/naval/glassfin).
                flock.advance(canyon, shipX, shipY);
            } else {
                flock.advance(canyon);
            }
            for (int i = 0; i < flock.size(); i++) {
                if (flock.rammedOff(i)) {
                    // M8 part C: a ram that dove off the play field is gone (it escaped); none loops back.
                    Enemy member = flock.member(i);
                    flock.drop(i);
                    escapes.escaped(member);
                    free(member);
                }
            }
            if (flock.loopedBackNow() > 0) {
                events.add(SimEvents.Type.LOOP_BACK, flock.leaderX(), flock.leaderY(), flock.loopedBackNow());
            }
            if (flock.finished()) {
                boolean lingered = flock.lingered();
                for (int i = 0; i < flock.size(); i++) {
                    Enemy member = flock.member(i);
                    if (member != null && (lingered || !PlayField.overlaps(member.x(), member.y(), member.hitbox()))) {
                        flock.drop(i);
                        escapes.escaped(member);
                        free(member);
                    }
                }
            }
            if (!flock.alive() && flock.ended()) {
                flocks.free(f);
            }
        }
    }

    /** M7 part G: a pursuit's tell, lunge and break-off events of this step, only while its flock has members. */
    private void pursuitEvents(Flock flock) {
        Pursuit pursuit = flock.pursuit().orElseThrow();
        if (!flock.alive()) {
            return;
        }
        if (pursuit.brokeNow()) {
            events.add(SimEvents.Type.PURSUIT_BREAK, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
        if (pursuit.toldNow()) {
            events.add(SimEvents.Type.PURSUIT_TELL, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
        if (pursuit.lungedNow()) {
            events.add(SimEvents.Type.PURSUIT_LUNGE, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
    }

    /**
     * M7 part G: the pursuit in flight (the first pursuit flock's, design/campaign Level 20), empty for none; no
     * allocation.
     */
    java.util.Optional<Pursuit> pursuit() {
        for (int f = 0; f < flocks.size(); f++) {
            if (flocks.get(f).pursuing()) {
                return flocks.get(f).pursuit();
            }
        }
        return java.util.Optional.empty();
    }

    /** M5 part D: a flock's member was destroyed (before it is freed): it drops out of its flock. */
    void flockMemberGone(Enemy enemy) {
        enemy.flock().drop(enemy.member());
    }

    Pool<Flock> flocks() {
        return flocks;
    }

    /** The chains fly a step ({@link ChainRules#moveChains}). */
    void moveChains(Ship ship, boolean firing) {
        chainRules.moveChains(ship, firing);
    }

    /** Frees the unit {@code enemy} from the pool. */
    void free(Enemy enemy) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (enemies.get(i) == enemy) {
                enemies.free(i);
                return;
            }
        }
    }

    /** A chain member was destroyed (before it is freed): the chain is cut there ({@link ChainRules#memberDestroyed}). */
    void memberDestroyed(Enemy enemy) {
        chainRules.memberDestroyed(enemy);
    }

    /** M7 part C: whether destroying chain head {@code enemy} now pays its head-first bonus (call before it is cut). */
    boolean headFirstBonusDue(Enemy enemy) {
        return enemy.link() == 0 && enemy.chain().headFirstBonusDue();
    }

    /** M7 part C: the ground has scrolled {@code scroll} px in this attempt (set every step before the spawns). */
    void groundScroll(double scroll) {
        groundScroll = scroll;
    }

    /** M7 part C: the ground's scroll now. */
    double groundScroll() {
        return groundScroll;
    }

    /** The serial of the next unit let in (the units let in so far), counting it. */
    int nextSerial() {
        return spawned++;
    }

    /**
     * M7 part C: the level's tendril gates {@code levelLinks} of {@code gates}: each gate's anchors among the
     * ground units (the first unused unit at its time and x of its anchor's kind, as {@link
     * LevelScript#withGates} adds them) start its tendril as they enter.
     */
    void links(Links levelLinks, List<GroundSpec.Gate> gates) {
        links = levelLinks;
        gateOf = new int[groundUnits.size()];
        java.util.Arrays.fill(gateOf, -1);
        for (int g = 0; g < gates.size(); g++) {
            GroundSpec.Gate gate = gates.get(g);
            for (int side = 0; side < 2; side++) {
                double x = side == 0 ? gate.leftX() : gate.rightX();
                int found = -1;
                for (int i = 0; i < groundUnits.size() && found < 0; i++) {
                    GroundUnit unit = groundUnits.get(i);
                    if (gateOf[i] < 0
                            && unit.t() == gate.t()
                            && unit.x() == x
                            && unit.enemy().equals(gate.anchor())) {
                        found = i;
                    }
                }
                if (found < 0) {
                    throw new IllegalArgumentException("gate " + g + "'s anchors are not among the ground units");
                }
                gateOf[found] = g * 2 + side;
            }
        }
    }

    /** Whether destroying chain member {@code enemy} now pays its chain's first bonus (the tail, first). */
    boolean firstBonusDue(Enemy enemy) {
        return enemy.chain().firstBonusDue(enemy.link());
    }

    Pool<Chain> chains() {
        return chainRules.chains();
    }

    /**
     * Flies every unit and scrolls the ground units by {@code groundScroll}; those whose gun is
     * ready fire at the ship when {@code firing}. A grouped ground unit that leaves the screen
     * alive is reported as escaped.
     */
    void move(Ship ship, boolean firing, double groundScroll) {
        objectiveHook.clearAllyHits();
        Convoy convoy = objectiveHook.convoy();
        if (chainRules.chains().size() > 0) {
            moveChains(ship, firing);
        }
        if (flocks.size() > 0) {
            moveFlocks(ship.x(), ship.y());
        }
        for (int g = 0; g < volleyClocks.length; g++) {
            if (volleyClocks[g] > 0) {
                volleyClocks[g]++;
            }
        }
        burrowers.tick();
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            // M7 part A: a unit the EMP Burst stuns at the step's start does nothing of its own this step.
            boolean stunned = enemy.empStunned();
            if (!stunned && enemy.bombLoaded()) {
                // M7 part D: its bomb run steers to the next dome coming down.
                int dome = convoy.nextDome(enemy.x(), enemy.y());
                enemy.bombTarget(dome, dome < 0 ? 0 : convoy.get(dome).x());
            }
            int target = objectiveHook.target(enemy, ship);
            double aimX = target < 0 ? ship.x() : convoy.get(target).x();
            double aimY = target < 0 ? ship.y() : convoy.get(target).y();
            // M5 part G: a diver under the always hook dives at the objective while it can be aimed at.
            int dive =
                    objectiveHook.mode(enemy.kind()) == HookMode.ALWAYS ? convoy.objective(enemy.x(), enemy.y()) : -1;
            boolean flying = dive < 0
                    ? enemy.move(ship.x(), ship.y(), groundScroll, aimX, aimY, wind)
                    : enemy.move(
                            ship.x(),
                            ship.y(),
                            groundScroll,
                            aimX,
                            aimY,
                            wind,
                            convoy.get(dive).x(),
                            convoy.get(dive).y(),
                            true);
            if (flying
                    && !stunned
                    && enemy.bombRun() >= 0
                    && enemy.bombArmed()
                    && convoy.over(enemy.bombRun(), enemy.x(), enemy.y())) {
                // M7 part D: over its dome's hit box: it drops its load, one hit, and flies on down (round 41: once
                // its run-in is over).
                int dome = enemy.bombRun();
                enemy.dropBomb();
                events.add(SimEvents.Type.BOMB_DROPPED, enemy.x(), enemy.y(), dome);
                objectiveHook.hitAlly(dome, convoy.spec(dome).bombs());
            }
            if (flying && enemy.risenNow()) {
                // M8 part D: its layer flipped: its escorts peel off its shadow.
                peel(enemy, ship);
            }
            if (flying && !stunned && enemy.decloakedNow()) {
                events.add(SimEvents.Type.DECLOAK, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && !stunned && enemy.swoopedNow()) {
                events.add(SimEvents.Type.SWOOP, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && canyon != null && enemy.swooping()) {
                // M7 part C: a swoop is clamped to the flyable side (no damage).
                enemy.clampX(
                        canyon.flyableX(enemy.y(), enemy.x(), enemy.hitbox().width() / 2));
            }
            if (!flying && enemy.burrowing()) {
                events.add(SimEvents.Type.BURROW_ESCAPED, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && !stunned && rollers && enemy.rolling() && convoy != null && convoy.groundAllies()) {
                objectiveHook.crush(enemy);
            }
            if (!flying) {
                if (enemy.spec().brood().isPresent()) {
                    carrierEnded(enemy);
                }
                if (enemy.spec().aura().isPresent()) {
                    heraldGone(enemy, false);
                }
                if (enemy.spec().rise().isPresent()) {
                    riseEnded(enemy, ship);
                }
                escapes.escaped(enemy);
                enemies.free(i);
            } else if (stunned) {
                // Stunned: it neither fires nor bursts, spawns, pounces or sweeps; its timers wait.
                continue;
            } else if (enemy.spec().brood().isPresent() && enemy.burstDue()) {
                hatch(enemy, ship);
                escapes.burst(enemy);
                enemies.free(i);
            } else if (enemy.spec().spawner().isPresent()) {
                spawnCycle(enemy, ship, firing);
            } else if (enemy.walking()) {
                fireWalker(enemy, ship, firing);
            } else if (enemy.spec().sweep().isPresent()) {
                if (enemy.telegraphStarted()) {
                    events.add(SimEvents.Type.SWEEP_TELEGRAPH, enemy.x(), enemy.y(), enemy.kind());
                } else if (enemy.sweepStarted()) {
                    events.add(SimEvents.Type.SWEEP_FIRED, enemy.x(), enemy.y(), enemy.kind());
                }
            } else if (enemy.spec().ring().isPresent()) {
                guns.pulse(enemy, ship, firing);
            } else if (enemy.burrowing()) {
                burrowers.burrow(enemy, ship, firing);
            } else if (enemy.vortex()) {
                guns.vortex(enemy, ship, firing);
            } else if (enemy.laneStrafer()) {
                guns.passTrigger(enemy, ship, firing);
            } else if (enemy.trigger() && firing) {
                EnemyGun gun = enemy.spec().gun().orElseThrow();
                if (gun.mine().isPresent()) {
                    guns.dropMine(enemy, gun);
                } else if (gun.mortar().isPresent()) {
                    guns.lob(enemy, gun, ship);
                } else if (gun.up()) {
                    guns.fireUp(enemy, gun);
                } else if (objectiveHook.mode(enemy.kind()) == HookMode.ALTERNATE) {
                    guns.fireAt(enemy, ship, objectiveHook.alternate(enemy, ship));
                } else {
                    guns.fireAt(enemy, ship, objectiveHook.target(enemy, ship));
                }
            }
        }
        if (auras) {
            // M7 part D: the Heralds' rings after the moves (the hits of this step meet this step's rings).
            Auras.step(enemies, events);
        }
    }

    /** M7 part D: whether a kind of the level has a curl-roll (the sortie's ground objects can be crushed). */
    boolean rollers() {
        return rollers;
    }

    /** M7 part E: whether a kind of the level has a pull (design/enemies/ground/dust-devil). */
    boolean pulls() {
        return pulls;
    }

    /**
     * M7 part E (design/enemies/ground/dust-devil, Pull): works out the pull on the ship at (shipX, shipY) for this
     * step: per unit with a pull on the play field, not stunned by the EMP Burst, its speed at the ship's distance
     * toward its centre; the pulls of several units add up. Exactly 0 without a unit with a pull in the level.
     */
    void pull(double shipX, double shipY) {
        pullX = 0;
        pullY = 0;
        if (!pulls) {
            return;
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.spec().pull().isEmpty()
                    || enemy.empStunned()
                    || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            double dx = enemy.x() - shipX;
            double dy = enemy.y() - shipY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed = enemy.spec().pull().get().shipSpeedAt(distance);
            if (speed > 0 && distance > 0) {
                pullX += dx / distance * speed;
                pullY += dy / distance * speed;
            }
        }
    }

    /** M7 part E: no pull on the ship in this step (it does not fly). */
    void noPull() {
        pullX = 0;
        pullY = 0;
    }

    /** M7 part E: the pull on the ship in this step, px/s to the right; 0 without one. */
    double pullX() {
        return pullX;
    }

    /** M7 part E: the pull on the ship in this step, px/s up the screen; 0 without one. */
    double pullY() {
        return pullY;
    }

    /** M7 part E: the unit with {@code serial} in flight; null when there is none (it died or left). */
    Enemy bySerial(int serial) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.serial() == serial) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * M7 part D: Herald {@code herald} is gone (before it is freed): its escorts break off; {@code killed}, every
     * ring it fed collapses ({@link SimEvents.Type#RINGS_COLLAPSED} when there was one); escaped, its rings are
     * no longer fed (kept, then they fade).
     */
    void heraldGone(Enemy herald, boolean killed) {
        carrierEnded(herald);
        if (!killed) {
            return;
        }
        int collapsed = 0;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.ringFeeder() == herald.serial()) {
                unit.collapseRing();
                collapsed++;
            }
        }
        if (collapsed > 0) {
            events.add(SimEvents.Type.RINGS_COLLAPSED, herald.x(), herald.y(), collapsed);
        }
    }

    /** M8 part A (design/world/europa, Under water rules): the level's medium, for the bullets' speed. */
    void medium(Medium water) {
        guns.medium(water);
    }

    /** M8 part A: the Bulwark in the escort slot, which draws a share of the aimed volleys at the ship. */
    void lure(Bulwark bulwark) {
        guns.lure(bulwark);
    }

    /**
     * M7 part C (design/campaign Level 16, What walls stop): the level's rock; enemy bullets stop at it (the
     * sortie's pass), and a swoop and a swarm are clamped to the flyable side.
     */
    void canyon(Canyon rock) {
        canyon = rock;
    }

    /** M7 part B: the level's no-surface zones (the sortie's); the Burrowers entering from now keep out of them. */
    void surfaceZones(SurfaceZones surfaceZones) {
        zones = surfaceZones;
    }

    /**
     * A walker's attacks (design/enemies/ground/scuttler): its fan along its facing, which aims at
     * nobody, or (M5 part B, design/enemies/ground/creeper) centred on the ship, on its own clock or
     * its wave's staggered one; and its spit, aimed (at the ship, or the convoy unit the hook picks)
     * while the ship is behind it.
     */
    private void fireWalker(Enemy enemy, Ship ship, boolean firing) {
        if (enemy.spec().turret().isPresent()) {
            // M7 part F (design/enemies/ground/warden-tank): its turret's bursts along the turret's aim, any way.
            if (enemy.trigger() && firing) {
                guns.fire(
                        enemy.partX(),
                        enemy.partY(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        enemy.turretAim(),
                        ship,
                        -1,
                        true,
                        enemy.frame());
            }
            return;
        }
        boolean fan = enemy.volleyGroup() >= 0 ? enemy.staggeredVolley(volleyClocks) : enemy.trigger();
        boolean spit = enemy.spit(ship.x(), ship.y());
        if (enemy.landedNow()) {
            events.add(SimEvents.Type.POUNCE_LANDED, enemy.x(), enemy.y(), enemy.kind());
        }
        if (!firing) {
            return;
        }
        if (enemy.pounce(ship.x(), ship.y())) {
            events.add(SimEvents.Type.POUNCE, enemy.x(), enemy.y(), enemy.kind());
        }
        if (fan && enemy.spec().gun().orElseThrow().mortar().isPresent()) {
            // M7 part D: a walker with a mortar (design/enemies/ground/shellback) lobs instead of its fan.
            guns.lob(enemy, enemy.spec().gun().get(), ship);
        } else if (fan) {
            boolean atShip = enemy.spec().walker().orElseThrow().fanAtShip();
            int inArc = objectiveHook.mode(enemy.kind()) == HookMode.IN_ARC ? objectiveHook.inArc(enemy) : -1;
            if (inArc >= 0) {
                // M5 part G: the objective within its armour's arc: the fan centred on it, aimed at it.
                guns.fire(
                        enemy.x(),
                        enemy.y(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        0,
                        ship,
                        inArc,
                        false,
                        enemy.frame());
            } else {
                guns.fire(
                        enemy.x(),
                        enemy.y(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        enemy.facing(),
                        ship,
                        -1,
                        !atShip,
                        enemy.frame());
            }
        }
        if (spit) {
            EnemyGun gun = enemy.spec().walker().orElseThrow().spit().orElseThrow();
            int target = objectiveHook.mode(enemy.kind()) == HookMode.NEAREST
                    ? objectiveHook.convoy().nearest(enemy.x(), enemy.y(), ship.x(), ship.y())
                    : -1;
            guns.fire(enemy.x(), enemy.y(), gun, false, 0, ship, target, false, enemy.frame());
        }
    }

    /**
     * A spawner ends, destroyed or bursting on its own (design/enemies/air/brood-pod): its units
     * fly out of its centre, spread evenly over its arc centred on the direction to the ship, and
     * its escorts break off.
     */
    void hatch(Enemy pod, Ship ship) {
        EnemySpec.Brood brood = pod.spec().brood().orElseThrow();
        fanOut(pod, brood.enemy(), brood.count(), brood.arcRadians(), brood.speed(), ship);
        events.add(SimEvents.Type.BROOD_HATCHED, pod.x(), pod.y(), pod.kind());
        carrierEnded(pod);
    }

    /**
     * {@code count} units of {@code enemy} fly out of {@code from}'s centre, spread evenly over
     * {@code arc} radians centred on the direction to the ship, at {@code speed} px/s.
     */
    private void fanOut(Enemy from, EnemySpec enemy, int count, double arc, double speed, Ship ship) {
        int kind = broodKinds[from.kind()];
        // M7 part G: worked out in the spawner's frame, which its units live in too.
        TravelFrame frame = from.frame();
        double fromY = from.frameY();
        double toShip = StrictMath.atan2(frame.y(ship.y()) - fromY, ship.x() - from.x());
        for (int k = 0; k < count; k++) {
            double angle = count == 1 ? toShip : toShip - arc / 2 + k * arc / (count - 1);
            Enemy unit = enemies.obtain();
            if (unit != null) {
                unit.hatch(enemy, kind, from.x(), fromY, Trig.cos(angle) * speed, Trig.sin(angle) * speed, spawned);
                if (frame.flipped()) {
                    unit.flip();
                }
            }
            spawned++;
        }
    }

    /**
     * A periodic spawner's step (M5 part C, design/enemies/ground/hive-node): its cycle runs, its
     * iris's telegraph is announced, and a due release (while the enemies fire) sends its units out
     * like a brood's, counted among the level's enemies as they are released.
     */
    private void spawnCycle(Enemy enemy, Ship ship, boolean firing) {
        int step = enemy.spawnStep(ship.x(), ship.y());
        if (step == Enemy.SPAWN_TELEGRAPH) {
            events.add(SimEvents.Type.SPAWN_TELEGRAPH, enemy.x(), enemy.y(), enemy.kind());
        } else if (step == Enemy.SPAWN_RELEASE && firing) {
            EnemySpec.Spawner spawner = enemy.spec().spawner().orElseThrow();
            fanOut(enemy, spawner.enemy(), spawner.count(), spawner.arcRadians(), spawner.speed(), ship);
            events.add(SimEvents.Type.SPAWN_RELEASED, enemy.x(), enemy.y(), enemy.kind());
            escapes.released(enemy, spawner.count());
        }
    }

    /**
     * One unit of {@code enemy} launched by a boss's window from (x, y) at {@code angle} radians
     * (0 = right, y up) and {@code speed} px/s, gliding {@code glideSeconds} before it holds (see
     * {@link Enemy#launch}); {@code enemy} is one of {@link #kinds()}.
     */
    void launch(EnemySpec enemy, double x, double y, double angle, double speed, double glideSeconds) {
        int kind = kinds.indexOf(enemy);
        if (kind < 0) {
            throw new IllegalArgumentException(enemy.slug() + " is not among the level's enemy kinds");
        }
        Enemy unit = enemies.obtain();
        if (unit != null) {
            unit.launch(enemy, kind, x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, glideSeconds, spawned);
        }
        spawned++;
    }

    /**
     * M5 part H (design/enemies/bosses/siege-spire, the maw's Wraiths; decision H3 = a): a unit a boss's
     * window launches as an ambush, flying {@code plan}'s rear path from where it starts; a rear unit from
     * its re-entry on (Rook's Trail). Returns whether the pool had room for it.
     */
    boolean launchAmbush(Spawn plan) {
        Enemy unit = enemies.obtain();
        if (unit != null) {
            unit.spawn(plan, spawned);
            unit.entered(WaveSpec.Entry.REAR);
            unit.launchedByBoss();
        }
        spawned++;
        return unit != null;
    }

    /** M5 part H: the units a boss launched as ambushes alive now (the Siege Spire's maw's Wraiths). */
    int launchedAlive() {
        int alive = 0;
        for (int j = 0; j < enemies.size(); j++) {
            alive += enemies.get(j).launched() ? 1 : 0;
        }
        return alive;
    }

    /** M5 part H: a boss part at (x, y) lobs {@code gun}'s mortar at where the ship is now. */
    void bossLob(double x, double y, EnemyGun gun, Ship ship) {
        guns.bossLob(x, y, gun, ship);
    }

    /** M5 part H: the root bursts' state, hashed only in a level with them. */
    void addBurstsTo(StateHash hash) {
        rootBursts.addBurstsTo(hash);
    }

    /** M5 part H: the root bursts' growths in the level (each growth a slot). */
    int burstSlots() {
        return rootBursts.burstSlots();
    }

    /** M5 part H: whether slot {@code b} is its burst's first growth. */
    boolean burstFirst(int b) {
        return rootBursts.burstFirst(b);
    }

    /** M5 part H: slot {@code b}'s burst. */
    RootBurst burstOf(int b) {
        return rootBursts.burstOf(b);
    }

    /** M5 part H: whether slot {@code b}'s telegraph has started in this attempt. */
    boolean burstTelegraphed(int b) {
        return rootBursts.burstTelegraphed(b);
    }

    /** M5 part H: whether slot {@code b}'s growth has appeared in this attempt (or the attempt restarted past it). */
    boolean burstGrown(int b) {
        return rootBursts.burstGrown(b, nextGround);
    }

    /** M5 part H: slot {@code b}'s burst's centre x after its move away from the ship. */
    double burstX(int b) {
        return rootBursts.burstX(b);
    }

    /** M5 part H: slot {@code b}'s burst's point's height in ground space. */
    double burstBase(int b) {
        return rootBursts.burstBase(b);
    }

    /**
     * M8 part D: rising carrier {@code carrier} is gone (destroyed or away, before it is freed): the escorts it still
     * held peel off where it was.
     */
    void riseEnded(Enemy carrier, Ship ship) {
        peel(carrier, ship);
        carrierEnded(carrier);
    }

    /**
     * M8 part D (design/enemies/naval/abyss-ray, design/enemies/naval/barbfin): the escorts {@code carrier} held peel
     * off from its centre, in their entry order, to hold points spread evenly across their circle's diameter around
     * its x at their hover's height above the ship ({@link EnemyStarts#peel}); they keep their own cadence after it
     * leaves or dies.
     */
    private void peel(Enemy carrier, Ship ship) {
        int count = riseEscorts.count(carrier);
        for (int i = 0; i < count; i++) {
            int k = riseEscorts.first(carrier);
            Spawn plan = riseEscorts.plan(k);
            int serial = riseEscorts.serial(k);
            String tag = riseEscorts.tag(k);
            riseEscorts.remove(k);
            double radius = plan.escort().isPresent() ? plan.escort().get().radius() : 0;
            double offset = count == 1 ? 0 : radius * (2.0 * i / (count - 1) - 1);
            Enemy unit = enemies.obtain();
            if (unit != null) {
                unit.peel(plan, carrier.x(), carrier.y(), offset, ship.y(), serial);
                unit.tag(tag);
            }
        }
    }

    /** M8 part D: the escorts held by rising carriers (hashed only while there are any). */
    void addRiseEscortsTo(StateHash hash) {
        if (riseEscorts.size() > 0) {
            riseEscorts.addTo(hash);
        }
    }

    /** The escorts circling {@code carrier} break off: it is gone. */
    private void carrierEnded(Enemy carrier) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.escorts(carrier)) {
                enemy.carrierEnded();
            }
        }
        if (lastCarrier == carrier) {
            lastCarrier = null;
        }
    }

    /** Lets in one unit planned outside the waves (a boss's stream), now. */
    void release(Spawn plan) {
        Enemy enemy = enemies.obtain();
        if (enemy != null) {
            enemy.spawn(plan, spawned);
        }
        spawned++;
    }

    /** One bullet from (x, y) at {@code angle} radians ({@link EnemyFire#fireAngle}). */
    void fireAngle(double x, double y, double angle, double speed, double damage, Ship ship) {
        guns.fireAngle(x, y, angle, speed, damage, ship);
    }

    /** Adds the staggered waves' volley clocks to {@code hash}; nothing without one (Act 1's hashes stay). */
    void addVolleyClocksTo(StateHash hash) {
        for (int clock : volleyClocks) {
            hash.add(clock);
        }
        // M7 part B: the Burrower waves' stagger clocks; nothing without one.
        burrowers.addTo(hash);
    }

    /** The schedule's place, for a boss checkpoint: the next wave unit, the next ground unit, the units let in. */
    int scheduleNext() {
        return waves.next();
    }

    int nextGround() {
        return nextGround;
    }

    int spawned() {
        return spawned;
    }

    /** Back to a boss checkpoint: an empty field, the schedule where it was then. */
    void restore(int scheduleNext, int ground, int spawnedUnits) {
        restore(scheduleNext, ground, spawnedUnits, 0);
    }

    /** As {@link #restore(int, int, int)}; M7 part G: with the refill motes left out by then ({@link #skipped()}). */
    void restore(int scheduleNext, int ground, int spawnedUnits, int skippedUnits) {
        reset();
        waves.next(scheduleNext);
        nextGround = ground;
        spawned = spawnedUnits;
        skipped = skippedUnits;
    }

    /** A set piece's part fires its gun from (x, y) at the ship, as a unit that aims at once. */
    void fireFrom(double x, double y, EnemyGun gun, Ship ship) {
        guns.fireFrom(x, y, gun, ship);
    }

    /** Round 34, P2: {@code gun}'s volley from (x, y) straight down the screen, as an aimed one otherwise. */
    void fireDown(double x, double y, EnemyGun gun, Ship ship) {
        guns.fireDown(x, y, gun, ship);
    }

    /** A spore of {@code gun}'s mine at (x, y), drifting at {@code angle} radians (0 = right, y up). */
    void dropMine(EnemyGun gun, double x, double y, double angle) {
        guns.dropMine(gun, x, y, angle);
    }

    /** M7 part F: the homing missiles in flight ({@link EnemyFire#missiles()}). */
    Missiles missiles() {
        return guns.missiles();
    }

    /**
     * M7 part F: {@code gun}'s volley from (x, y) along {@code aim} (radians clockwise from straight down), as an
     * aimed one otherwise (a walking boss's chin burst along its heading).
     */
    void fireAlong(double x, double y, EnemyGun gun, double aim, Ship ship) {
        guns.fire(x, y, gun, false, aim, ship, -1, true);
    }

    /** Drifts the spores ({@link EnemyFire#moveMines}). */
    void moveMines(Ship ship) {
        guns.moveMines(ship);
    }

    /** Flies the lobs ({@link EnemyFire#moveLobs(Ship, double)}); the damage of this step's direct hits. */
    double moveLobs(Ship ship, double groundScroll) {
        return guns.moveLobs(ship, groundScroll);
    }

    /** As {@link #moveLobs(Ship, double)}; M7 part A: a Deflector Pod's field may catch a direct hit. */
    double moveLobs(Ship ship, double groundScroll, Deflectors deflectors) {
        return guns.moveLobs(ship, groundScroll, deflectors);
    }

    Pool<Lob> lobs() {
        return guns.lobs();
    }

    /** M5 part G: the direct hits the lobs aimed at the objective dealt it in the last {@link #moveLobs}. */
    double objectiveLobDamage() {
        return guns.objectiveLobDamage();
    }

    /** A destroyed unit with a death burst pops into its puff of bullets. */
    void deathBurst(Enemy enemy, Ship ship) {
        guns.deathBurst(enemy, ship);
    }

    /** A ring of {@code count} bullets from (x, y) ({@link EnemyFire#burst}). */
    void burst(double x, double y, int count, double speed, double damage, Ship ship) {
        guns.burst(x, y, count, speed, damage, ship);
    }

    Pool<Mine> mines() {
        return guns.mines();
    }

    void moveBullets() {
        guns.moveBullets();
    }

    /**
     * The edges whose waves are active (design/player/wingmen): a unit of a wave that entered from
     * the sides ({@link WarningEdge#LEFT} and {@link WarningEdge#RIGHT}) or the rear
     * ({@link WarningEdge#BOTTOM}) is still alive, as {@link WarningEdge} bits. A chain's members
     * count by their chain's edge, a flock's by their flock's (M5 part D): its wave's, the rear once
     * it has looped back; a rear ambush unit is a rear one from its re-entry on.
     */
    int activeEdges() {
        int edges = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            WaveSpec.Entry entry = enemy.chain() != null
                    ? enemy.chain().entry()
                    : enemy.flock() != null ? enemy.flock().entry() : enemy.entry();
            if (entry == WaveSpec.Entry.SIDES) {
                edges |= WarningEdge.LEFT.bit() | WarningEdge.RIGHT.bit();
            } else if (enemy.flock() != null && enemy.flock().pursuing()) {
                // M7 part G: a pursuit's motes press from the top edge whatever their wave's entry.
                edges |= WarningEdge.TOP.bit();
            } else if (entry == WaveSpec.Entry.REAR) {
                // M7 part G: a flipped unit's rear is the screen's top.
                edges |= enemy.flipped() ? WarningEdge.TOP.bit() : WarningEdge.BOTTOM.bit();
            }
        }
        return edges;
    }

    /** The edges showing an edge warning at {@code levelTick}, as {@link WarningEdge} bits. */
    int warnings(int levelTick) {
        int edges = waves.warnings(levelTick);
        // M5 part G: and a ground unit entering from a side (Level 13's mortar floe).
        for (int w = 0; w < sideWarningStarts.length; w++) {
            if (levelTick >= sideWarningStarts[w] && levelTick < sideWarningEnds[w]) {
                edges |= sideWarningEdges[w];
            }
        }
        return edges;
    }

    Pool<Enemy> enemies() {
        return enemies;
    }

    Pool<EnemyBullet> bullets() {
        return guns.bullets();
    }

    /** The distinct enemies of the level; {@link Enemy#kind()} indexes this list. */
    List<EnemySpec> kinds() {
        return kinds;
    }

    /** The units of the enemy {@code slug} the level sends, flying and on the ground. */
    int unitsOf(String slug) {
        int count = waves.unitsOf(slug);
        for (GroundUnit unit : groundUnits) {
            count += unit.enemy().slug().equals(slug) ? 1 : 0;
        }
        for (EnemySpec kind : kinds) {
            if (kind.brood().isPresent() && kind.brood().get().enemy().slug().equals(slug)) {
                count += waves.unitsOf(kind.slug()) * kind.brood().get().count();
            }
        }
        return count;
    }

    /** M5 part C: the units of the enemy {@code slug} of the waves tagged {@code tag} (a scoped escapes objective). */
    int unitsOf(String slug, String tag) {
        return waves.unitsOf(slug, tag);
    }

    /** Every unit the level sends, flying and on the ground, and the units its spawners release. */
    int units() {
        int count = waves.units() + groundUnits.size();
        for (EnemySpec kind : kinds) {
            if (kind.brood().isPresent()) {
                count += waves.unitsOf(kind.slug()) * kind.brood().get().count();
            }
        }
        return count;
    }
}
