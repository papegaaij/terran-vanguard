package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * The level's enemies: the units entering by the {@link WaveSchedule} and the units fixed to the
 * ground ({@link GroundUnit}, entering at the top edge with the scroll), flying or
 * scrolling and firing at the ship, and their bullets. What a kill earns is the {@link Sortie}'s.
 *
 * <p>RF8: the coordinator keeps the units' pool, the update order and the API; the concerns live in package-private
 * collaborators, each owning its state: {@link EnemyEntries} (the waves', the halt cycle's and the ground's entries,
 * the serials), {@link EnemyFlocks} (the swarms), {@link EnemyCarriers} (spawners, escorts, broods), {@link
 * EnemyMoves} (the units' step), {@link WalkerFire}, {@link EnemyPull}, {@link WingScatter}, {@link EnemyKinds};
 * the fire is {@link EnemyFire}'s, the chains {@link ChainRules}', the root bursts {@link RootBursts}'.
 */
final class EnemyForce {
    private static final int ENEMY_CAPACITY = 192;

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

        /**
         * M9 part C (design/enemies/air/rail-serpent, the machine split): a wreck's car blew: it pays its bounty (a
         * part's: no kill), just before it is freed.
         */
        default void wreckPaid(Enemy car) {}

        /**
         * M10 part A (design/enemies, The three-way war): a unit was lost to the war, not to the player (a prey unit the
         * hunters killed, a hunter dead on its prey), just before it is freed: no kill, no pay.
         */
        default void lostToWar(Enemy enemy) {
            escaped(enemy);
        }
    }

    private final SimEvents events;
    private final List<EnemySpec> kinds;
    /** The units in flight, each reporting its damage's events (M7 part D: aura rings, a curled shell) into the step's. */
    private final Pool<Enemy> enemies;
    /** M7 part D: whether a kind of the level has an aura (the rings are stepped only then). */
    private final boolean auras;
    /** M9 part A: whether a kind of the level has a {@code laser-line} (design/enemies/ground/rail-bunker). */
    private final boolean lines;
    /** Step FIX7: whether a kind of the level keeps its drifting hold apart from the others ({@link HoverHold#keepApart}). */
    private final boolean apart;
    /** M7 part D: whether a kind of the level has a curl-roll (its ball crushes ground targets). */
    private final boolean rollers;

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
    /** M8 part E: the slowing rings in flight (design/enemies/naval/siren); null in a level without a ring unit. */
    private final SonarRings rings;
    /** M9 part D: the air links of the linked groups (design/enemies/space/shard-drone); null without such a unit. */
    private final AirLinks airLinks;
    /** M5 part D: the swarms. */
    private final EnemyFlocks flocks;
    /** The spawners and their escorts. */
    private final EnemyCarriers carriers;
    /** M5 part B: the walkers' attacks and the staggered waves' volley clocks. */
    private final WalkerFire walkers;
    /** The units' step in {@link #move}. */
    private final EnemyMoves moves;
    /** M9 part F: the prey latches (Level 33's uneasy alliance); null in a level without a prey unit. */
    private final PreyLatches prey;
    /** M10 part A, step A1b: the level's turn, steps; 0 without one (the links live from the hunters' entry). */
    private int turnTicks;
    /** M10 part A (The three-way war): the hunting waves' prey; null in a level without a hunting wave. */
    private final PreyWar war;
    /** M7 part E: the pull on the ship. */
    private final EnemyPull pull;
    /** The entries: waves, halt cycle, ground units, the units let in outside the waves. */
    private final EnemyEntries entries;

    /** M7 part C: the level's rock and the scroll now; null in a level without terrain. */
    private Canyon canyon;
    /** M7 part C: the ground's scroll now (a ground chain crawls and is placed in ground space). */
    private double groundScroll;

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
        this(waveSpecs, groundUnits, extraKinds, rng, rules, events, escapes, spans, reversedAt, WallMounts.NONE);
    }

    /** @param mounts M8 part E: the level's walls, for the waves out of them (a cave's, a wall's nests) */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes,
            java.util.function.Function<WaveSpec, Optional<Formations.Span>> spans,
            java.util.function.DoublePredicate reversedAt,
            WallMounts mounts) {
        this(
                waveSpecs,
                groundUnits,
                extraKinds,
                rng,
                rules,
                events,
                escapes,
                spans,
                reversedAt,
                mounts,
                Optional.empty());
    }

    /**
     * @param halt M9 part B (design/campaign Level 29): the held arena's halt cycle, planned after the waves (with the
     *     same generator: only a level with one plans it); empty for none
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
            java.util.function.DoublePredicate reversedAt,
            WallMounts mounts,
            Optional<HaltCycle> halt) {
        this.events = events;
        enemies = new Pool<>(ENEMY_CAPACITY, () -> new Enemy(events), Enemy[]::new);
        List<WaveSpec> haltSpecs = halt.map(HaltCycle::waves).orElse(List.of());
        kinds = EnemyKinds.of(waveSpecs, groundUnits, extraKinds, haltSpecs);
        objectiveHook = new ObjectiveHook(kinds, events);
        guns = new EnemyFire(rng, rules, events, objectiveHook);
        auras = kinds.stream().anyMatch(kind -> kind.aura().isPresent());
        lines = kinds.stream().anyMatch(kind -> kind.laserLine().isPresent());
        apart = kinds.stream()
                .anyMatch(kind -> kind.hover().map(EnemySpec.Hover::keepApart).orElse(false));
        rollers = kinds.stream().anyMatch(kind -> kind.roll().isPresent());
        pull = new EnemyPull(kinds, enemies);
        WaveSchedule waves = new WaveSchedule(waveSpecs, kinds, rng, spans, reversedAt, mounts);
        // M9 part B: the halt cycle's own plan, on the halt's clock (no spans, no reversed scroll, no walls).
        WaveSchedule haltWaves = halt.isEmpty()
                ? null
                : new WaveSchedule(haltSpecs, kinds, rng, wave -> Optional.empty(), t -> false, WallMounts.NONE);
        // M8 part E: only with a unit that sings a slowing ring.
        rings = kinds.stream().anyMatch(kind -> kind.slowRing().isPresent()) ? new SonarRings() : null;
        // M9 part D: only with an air-linked unit.
        airLinks = kinds.stream().anyMatch(kind -> kind.airLink().isPresent()) ? new AirLinks(events) : null;
        walkers = new WalkerFire(waves.volleyClocks(), events, guns, objectiveHook);
        burrowers = new Burrowers(waves.burrowClocks(), enemies, events, guns);
        chainRules = new ChainRules(this, kinds, waves, enemies, events, escapes, guns);
        flocks = new EnemyFlocks(this, enemies, events, escapes);
        carriers = new EnemyCarriers(this, kinds, enemies, events, escapes);
        rootBursts = new RootBursts(groundUnits, events);
        entries = new EnemyEntries(
                this,
                enemies,
                events,
                kinds,
                waves,
                haltWaves,
                EnemyEntries.haltEveryTicks(halt),
                groundUnits,
                reversedAt,
                objectiveHook,
                rootBursts,
                chainRules,
                flocks,
                carriers);
        // M10 part A (The three-way war): only in a level with a hunting wave.
        boolean hunting = waveSpecs.stream().anyMatch(wave -> !wave.hunts().isEmpty())
                || haltSpecs.stream().anyMatch(wave -> !wave.hunts().isEmpty());
        war = hunting ? new PreyWar(enemies, guns.bullets(), events, escapes) : null;
        // M9 part F: only in a level with prey (Level 33's marked Talon wings); M10 part A: or a hunting wave.
        prey = hunting || kinds.stream().anyMatch(kind -> kind.preyRole() == EnemySpec.PreyRole.PREY)
                ? new PreyLatches(enemies, events)
                : null;
        moves = new EnemyMoves(
                this,
                enemies,
                events,
                escapes,
                guns,
                objectiveHook,
                burrowers,
                carriers,
                walkers,
                rings,
                rollers,
                prey,
                war);
    }

    /** M10 part A (The three-way war): the hunting waves' prey; null in a level without a hunting wave. */
    PreyWar war() {
        return war;
    }

    /**
     * M10 part A (design/player/systems, the IFF spoofer): the ship carries the spoofer: every spoofable unit's aimed
     * volleys come {@code delaySeconds} late at their stale aim (0: none, as without the module).
     */
    void spoof(double delaySeconds) {
        moves.spoof(Math.max(0, SimStep.ticks(delaySeconds)));
    }

    /** M9 part D: the air links of the linked groups; null in a level without an air-linked unit. */
    AirLinks airLinks() {
        return airLinks;
    }

    /** M9 part D: the target-the-objective hook (the air links' touch on a hull escort asks it per kind). */
    ObjectiveHook hookOf() {
        return objectiveHook;
    }

    /** M9 part D: a unit of a linked group is gone, {@code destroyed} or away: its links break. */
    void linkMemberGone(Enemy unit, boolean destroyed) {
        if (airLinks != null && unit.spec().airLink().isPresent()) {
            airLinks.gone(unit, destroyed);
        }
    }

    /** M8 part E: the slowing rings in flight; null in a level without a unit that sings one. */
    SonarRings rings() {
        return rings;
    }

    /** M8 part E: a singing unit was destroyed: its song ends, its ring in flight with it. */
    void silence(Enemy unit) {
        if (rings != null) {
            rings.silence(unit.serial());
        }
    }

    /**
     * M8 part E: the rings and the hulls' slows (the ship's, Rook's), only in a level with a ring unit, so the other
     * levels hash as before.
     */
    void addRingsTo(StateHash hash, Ship ship, Wingman wingman) {
        if (airLinks != null) {
            // M9 part D: only in a level with an air-linked unit.
            airLinks.addTo(hash);
        }
        if (rings != null) {
            rings.addTo(hash);
            ship.slow.addTo(hash);
            if (wingman != null) {
                wingman.slow.addTo(hash);
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
        moves.wind(pxPerSecond);
    }

    /** Back to the level start: no enemies or bullets, the schedule at its first unit. */
    void reset() {
        Pools.clear(enemies);
        guns.reset();
        chainRules.reset();
        flocks.reset();
        entries.reset();
        carriers.reset();
        if (rings != null) {
            rings.reset();
        }
        if (airLinks != null) {
            airLinks.reset();
        }
        walkers.reset();
        burrowers.reset();
        objectiveHook.clearAllyHits();
        rootBursts.reset();
    }

    /** M5 part H: the root bursts' telegraphs, before {@link #spawn} in a step ({@link RootBursts#bursts}). */
    void bursts(int levelTick, double shipX, double groundScroll, double scrollStep) {
        rootBursts.bursts(levelTick, shipX, groundScroll, scrollStep, entries.nextGround());
    }

    /** M5 part H: how far a burst moves sideways to clear the ship ({@link RootBursts#clearOfShip}). */
    static double clearOfShip(double x, double foot, double shipX) {
        return RootBursts.clearOfShip(x, foot, shipX);
    }

    /** Lets in every unit due at {@code levelTick}, flying and on the ground. */
    void spawn(int levelTick) {
        if (war != null && turnTicks > 0) {
            // M10 part A, step A1b: the hunting links are live from the level's turn.
            boolean live = levelTick >= turnTicks;
            war.live(live);
            if (prey != null) {
                prey.warLive(live);
            }
        }
        entries.spawn(levelTick);
    }

    /**
     * M10 part A, step A1b (design/enemies, The three-way war): the level's turn at level step {@code ticks}: the hunting
     * waves' links are live from it ({@link LevelScript#turn()}); without one they are live from the hunters' entry.
     */
    void turn(int ticks) {
        turnTicks = ticks;
        if (war != null && ticks > 0) {
            war.live(false);
            if (prey != null) {
                prey.warLive(false);
            }
        }
    }

    /**
     * M9 part B (design/campaign Level 29, the halt cycle): lets in the halt cycle's units due at step {@code haltTick}
     * of the halt (0 at its first held step); the cycle repeats every its length. Nothing without a halt cycle.
     */
    void spawnHalt(int haltTick) {
        entries.spawnHalt(haltTick);
    }

    /** M9 part B: the holders left out for want of room in the pool in this attempt. */
    int holdersLeftOut() {
        return entries.holdersLeftOut();
    }

    /** M9 part B: the halt cycle's units let in so far in this attempt. */
    int haltReleased() {
        return entries.haltReleased();
    }

    /** M9 part E: the level's forks ({@link EnemyEntries#forks}). */
    void forks(Forks levelForks) {
        entries.forks(levelForks);
    }

    /** M9 part E: per fork branch, the units counted as enemies it sends ({@link EnemyEntries#branchUnits}). */
    int[] branchUnits(int forks) {
        return entries.branchUnits(forks);
    }

    /** M9 part B: the rocks' pass of the waves on rocks (the riders board at their entry). */
    void rides(RockRides rockRides) {
        entries.rides(rockRides);
    }

    /** M9 part B: whether the level's waves carry rocks ({@link WaveSpec#onRocks()}). */
    int rocks() {
        return entries.rocks();
    }

    /**
     * M9 part B: the dormant units whose time on the level clock has come wake (their tell starts, {@link
     * SimEvents.Type#WOKE}). Nothing in a level without a dormant unit.
     */
    void wake(int levelTick) {
        entries.wake(levelTick);
    }

    /**
     * M9 part B (design/enemies/air/talon, Squad leader): {@code leader} (its wave's) was killed: its wing breaks off
     * ({@link WingScatter#scatter}).
     */
    void scatter(Enemy leader, Ship ship) {
        WingScatter.scatter(enemies, leader, ship, events, guns);
    }

    /**
     * M7 part G: the pursuits' refill motes left out so far in this attempt (their flock was full): planned, they are
     * taken off the level's units ({@link Sortie#enemyTotal()}) and so off the kill ratio's count.
     */
    int skipped() {
        return flocks.skipped();
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
        flocks.move(shipX, shipY);
    }

    /**
     * M7 part G: the pursuit in flight (the first pursuit flock's, design/campaign Level 20), empty for none; no
     * allocation.
     */
    java.util.Optional<Pursuit> pursuit() {
        return flocks.pursuit();
    }

    /** M5 part D: a flock's member was destroyed (before it is freed): it drops out of its flock. */
    void flockMemberGone(Enemy enemy) {
        enemy.flock().drop(enemy.member());
    }

    Pool<Flock> flocks() {
        return flocks.pool();
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
        return entries.nextSerial();
    }

    /**
     * M7 part C: the level's tendril gates {@code levelLinks} of {@code gates}: each gate's anchors among the
     * ground units (the first unused unit at its time and x of its anchor's kind, as {@link
     * LevelScript#withGates} adds them) start its tendril as they enter.
     */
    void links(Links levelLinks, List<GroundSpec.Gate> gates) {
        entries.links(levelLinks, gates);
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
        if (chainRules.chains().size() > 0) {
            moveChains(ship, firing);
        }
        if (flocks.pool().size() > 0) {
            moveFlocks(ship.x(), ship.y());
        }
        walkers.tick();
        burrowers.tick();
        if (rings != null) {
            // M8 part E: the rings in flight expand and scroll with the ground before the nests sing new ones.
            rings.step(groundScroll);
        }
        moves.step(ship, firing, groundScroll);
        if (apart) {
            // Step FIX7: the drifting holds kept apart turn away from each other before they touch.
            HoverHold.keepApart(enemies);
        }
        if (prey != null) {
            // M9 part F: the leeches on their prey, then the new prey latches.
            prey.step();
        }
        if (war != null) {
            // M10 part A: the hunting rammers on their prey.
            war.rams();
        }

        if (auras) {
            // M7 part D: the Heralds' rings after the moves (the hits of this step meet this step's rings).
            Auras.step(enemies, events);
        }
        if (lines) {
            // M9 part A: the laser lines after the moves, locked on the ship where it is now.
            LineBeam.stepAll(enemies, ship.x(), ship.y(), events);
        }
    }

    /** M7 part D: whether a kind of the level has a curl-roll (the sortie's ground objects can be crushed). */
    boolean rollers() {
        return rollers;
    }

    /** M7 part E: whether a kind of the level has a pull (design/enemies/ground/dust-devil). */
    boolean pulls() {
        return pull.pulls();
    }

    /**
     * M7 part E (design/enemies/ground/dust-devil, Pull): works out the pull on the ship at (shipX, shipY) for this
     * step: per unit with a pull on the play field, not stunned by the EMP Burst, its speed at the ship's distance
     * toward its centre; the pulls of several units add up. Exactly 0 without a unit with a pull in the level.
     */
    void pull(double shipX, double shipY) {
        pull.pull(shipX, shipY);
    }

    /** M7 part E: no pull on the ship in this step (it does not fly). */
    void noPull() {
        pull.none();
    }

    /** M7 part E: the pull on the ship in this step, px/s to the right; 0 without one. */
    double pullX() {
        return pull.x();
    }

    /** M7 part E: the pull on the ship in this step, px/s up the screen; 0 without one. */
    double pullY() {
        return pull.y();
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
        carriers.heraldGone(herald, killed);
    }

    /**
     * M10 part B (design/campaign Level 36, the harvesters): drifting carrier {@code carrier} is gone, destroyed or
     * away (before it is freed): its pair's escorts circle the other one, or break off when both are gone.
     */
    void carrierGone(Enemy carrier) {
        carriers.carrierEnded(carrier);
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

    /** M7 part C: the level's rock; null in a level without terrain. */
    Canyon canyon() {
        return canyon;
    }

    /** M7 part B: the level's no-surface zones (the sortie's); the Burrowers entering from now keep out of them. */
    void surfaceZones(SurfaceZones surfaceZones) {
        entries.surfaceZones(surfaceZones);
    }

    /**
     * A spawner ends, destroyed or bursting on its own (design/enemies/air/brood-pod): its units
     * fly out of its centre, spread evenly over its arc centred on the direction to the ship, and
     * its escorts break off.
     */
    void hatch(Enemy pod, Ship ship) {
        carriers.hatch(pod, ship);
    }

    /**
     * One unit of {@code enemy} launched by a boss's window from (x, y) at {@code angle} radians
     * (0 = right, y up) and {@code speed} px/s, gliding {@code glideSeconds} before it holds (see
     * {@link Enemy#launch}); {@code enemy} is one of {@link #kinds()}.
     */
    void launch(EnemySpec enemy, double x, double y, double angle, double speed, double glideSeconds) {
        entries.launch(enemy, x, y, angle, speed, glideSeconds);
    }

    /**
     * M9 part H, step H1b: one unit of {@code launcher} out of a ringed boss's rim bay at (x, y) along {@code angle}
     * ({@link EnemyCarriers#bayLaunch}); returns whether it was launched.
     */
    boolean bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {
        return carriers.bayLaunch(launcher, kinds.indexOf(launcher.unit()), x, y, angle);
    }

    /**
     * M5 part H (design/enemies/bosses/siege-spire, the maw's Wraiths; decision H3 = a): a unit a boss's
     * window launches as an ambush, flying {@code plan}'s rear path from where it starts; a rear unit from
     * its re-entry on (Rook's Trail). Returns whether the pool had room for it.
     */
    boolean launchAmbush(Spawn plan) {
        return entries.launchAmbush(plan);
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
        return rootBursts.burstGrown(b, entries.nextGround());
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
        carriers.riseEnded(carrier, ship);
    }

    /** M8 part D: the escorts held by rising carriers (hashed only while there are any). */
    void addRiseEscortsTo(StateHash hash) {
        carriers.addRiseEscortsTo(hash);
    }

    /**
     * M8 part H: lets in one chain planned outside the waves (a coiled boss's shed Eel Swarm) by {@code entry}, now;
     * returns its serial (-1 when none could be let in).
     */
    int releaseChain(Spawn plan, WaveSpec.Entry entry) {
        return chainRules.releaseChain(plan, entry);
    }

    /** M8 part H: whether the chain with serial {@code serial} still swims. */
    boolean chainAlive(int serial) {
        return chainRules.chainAlive(serial);
    }

    /** Lets in one unit planned outside the waves (a boss's stream), now. */
    void release(Spawn plan) {
        entries.release(plan);
    }

    /** One bullet from (x, y) at {@code angle} radians ({@link EnemyFire#fireAngle}). */
    void fireAngle(double x, double y, double angle, double speed, double damage, Ship ship) {
        guns.fireAngle(x, y, angle, speed, damage, ship);
    }

    /** Adds the staggered waves' volley clocks to {@code hash}; nothing without one (Act 1's hashes stay). */
    void addVolleyClocksTo(StateHash hash) {
        walkers.addTo(hash);
        // M7 part B: the Burrower waves' stagger clocks; nothing without one.
        burrowers.addTo(hash);
    }

    /** The schedule's place, for a boss checkpoint: the next wave unit, the next ground unit, the units let in. */
    int scheduleNext() {
        return entries.scheduleNext();
    }

    int nextGround() {
        return entries.nextGround();
    }

    int spawned() {
        return entries.spawned();
    }

    /** Back to a boss checkpoint: an empty field, the schedule where it was then. */
    void restore(int scheduleNext, int ground, int spawnedUnits) {
        restore(scheduleNext, ground, spawnedUnits, 0);
    }

    /** As {@link #restore(int, int, int)}; M7 part G: with the refill motes left out by then ({@link #skipped()}). */
    void restore(int scheduleNext, int ground, int spawnedUnits, int skippedUnits) {
        reset();
        entries.restore(scheduleNext, ground, spawnedUnits);
        flocks.skipped(skippedUnits);
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

    /** Step BOSS70: a thrown line mine ({@link EnemyFire#tossMine}). */
    boolean tossMine(
            EnemyGun gun, double x, double y, double toX, double toY, double throwSeconds, double lifeSeconds) {
        return guns.tossMine(gun, x, y, toX, toY, throwSeconds, lifeSeconds);
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

    /** M9 part C: the spores and the line mines ({@link EnemyFire#moveMines(Ship, double, double, double, double, double)}). */
    void moveMines(Ship ship, double scroll, double shipX, double shipY, double rookX, double rookY) {
        guns.moveMines(ship, scroll, shipX, shipY, rookX, rookY);
    }

    /** M9 part C: a line mine shot or touched bursts ({@link EnemyFire#lineMineBurst}). */
    void lineMineBurst(Mine mine, Ship ship, boolean shot) {
        guns.lineMineBurst(mine, ship, shot);
    }

    /** M9 part C: this step's mine blasts on a hull ({@link EnemyFire#blastDamage}). */
    double blastDamage(Hull hull, double x, double y) {
        return guns.blastDamage(hull, x, y);
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

    /** M10 part A (design/enemies, Reflect): a mirror's bounced bullet ({@link EnemyFire#bounce}). */
    boolean bounce(double x, double y, double vx, double vy, double damage) {
        return guns.bounce(x, y, vx, vy, damage);
    }

    /** A destroyed unit with a death burst pops into its puff of bullets. */
    void deathBurst(Enemy enemy, Ship ship, boolean rammed) {
        guns.deathBurst(enemy, ship, rammed);
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
        if (war != null) {
            // M10 part A: the hunters' bullets that reach their prey.
            war.bullets();
        }
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
        return entries.warnings(levelTick);
    }

    /** Step MARK70: whether a wave of neutral craft is announced at {@code levelTick}. */
    boolean friendlies(int levelTick) {
        return entries.friendlies(levelTick);
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
        return entries.unitsOf(slug);
    }

    /** M5 part C: the units of the enemy {@code slug} of the waves tagged {@code tag} (a scoped escapes objective). */
    int unitsOf(String slug, String tag) {
        return entries.unitsOf(slug, tag);
    }

    /**
     * Every unit the level sends that counts as an enemy (M9 part B: its neutral craft left out), flying and on the
     * ground, and the units its spawners release; M9 part B: the halt cycle's units count as they come ({@link
     * #haltReleased()}).
     */
    int units() {
        return entries.units();
    }
}
