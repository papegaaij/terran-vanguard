package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The level's enemies: the units entering by the {@link WaveSchedule} and the units fixed to the
 * ground ({@link LevelScript.GroundUnit}, entering at the top edge with the scroll), flying or
 * scrolling and firing at the ship, and their bullets. What a kill earns is the {@link Sortie}'s.
 */
final class EnemyForce {
    private static final int ENEMY_CAPACITY = 192;
    /** Segment chains in flight: the waves' and their regrown rear parts. */
    private static final int CHAIN_CAPACITY = 12;
    /** M5 part D: swarms in flight. */
    private static final int FLOCK_CAPACITY = 8;

    static final int BULLET_CAPACITY = 256;
    /** No enemy bullet spawns this close to the ship (design/enemies, bullet readability rules). */
    private static final double NO_FIRE_DISTANCE = 72;

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

    private static final int MINE_CAPACITY = 96;
    private static final int LOB_CAPACITY = 32;
    /** How far outside a lob's impact circle its ring starts, px. */
    private static final double RING_MARGIN = 12;

    private final SplitMix64 rng;
    private final Rules rules;
    private final SimEvents events;
    private final Escapes escapes;
    private final List<EnemySpec> kinds;
    private final WaveSchedule waves;
    private final List<LevelScript.GroundUnit> groundUnits;
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

    private final Pool<EnemyBullet> bullets = new Pool<>(BULLET_CAPACITY, EnemyBullet::new, EnemyBullet[]::new);
    private final Pool<Mine> mines = new Pool<>(MINE_CAPACITY, Mine::new, Mine[]::new);
    private final Pool<Lob> lobs = new Pool<>(LOB_CAPACITY, Lob::new, Lob[]::new);
    private final Pool<Chain> chains = new Pool<>(CHAIN_CAPACITY, Chain::new, Chain[]::new);
    private final Pool<Flock> flocks = new Pool<>(FLOCK_CAPACITY, Flock::new, Flock[]::new);
    /** Flocks started in this attempt, for their serials. */
    private int flocksSpawned;
    /** The flock the members entering in this step join (a swarm's members enter together); null for none. */
    private Flock joining;
    /** Chains started in this attempt, for their serials. */
    private int chainsSpawned;
    /** Per enemy kind, its chain's segment, tail and regrown head kinds; -1 for others. */
    private final int[] segmentKinds;

    private final int[] tailKinds;
    private final int[] regrownKinds;
    private int spawned;
    private int nextGround;
    /** M5 part F: the level's wind this step, px/s to the right; exactly 0 without weather. */
    private double wind;
    /** The convoy the target-the-objective hook aims at; null without one. */
    private Convoy convoy;
    /**
     * Per enemy kind, its hook mode ({@code nearest} for an escort's hooked kinds; M5 part G: a defend
     * primary's modes); null without a convoy.
     */
    private LevelScript.HookMode[] modes;
    /** M5 part G: the direct hits of the lobs aimed at the objective that landed on it in the last {@link #moveLobs}. */
    private double objectiveDamage;
    /**
     * M7 part D: per convoy unit, the hits the hooked attacks dealt it since the last {@link #move} began
     * (bomb runs, the direct hits of the lobs aimed at it, D3's rolls), which the sortie applies; empty
     * without ground allies.
     */
    private double[] allyHits = new double[0];
    /** M5 part G: per ground unit entering from a side, the steps its edge warning starts and ends, and its edge bit. */
    private final int[] sideWarningStarts;

    private final int[] sideWarningEnds;
    private final int[] sideWarningEdges;
    /**
     * M5 part H, the root bursts (design/campaign Level 14, Hazards; decision H2 = a): per ground unit
     * its slot in the burst arrays, -1 for a unit no burst grows; per slot its ground unit, the level
     * step its telegraph starts, whether it started in this attempt, the burst's move away from the ship
     * (px, the same for every growth of a burst), its point's height in ground space (the screen height
     * plus the ground's scroll at the telegraph), the half width its growths need from the field's edges,
     * and whether it is its burst's first growth (which raises the burst's events).
     */
    private final int[] burstSlot;

    private final int[] burstUnit;
    private final int[] burstTelegraphTicks;
    private final boolean[] burstTelegraphed;
    private final double[] burstShift;
    private final double[] burstBase;
    private final double[] burstFoot;
    private final boolean[] burstFirst;
    /** M5 part H: the ship's x, the ground's scroll and its last step, as {@link #bursts} last saw them. */
    private double burstShipX = PlayField.WIDTH / 2.0;

    private double burstScroll;
    private double burstScrollStep;
    /** Per enemy kind, the kind a spawner releases; -1 for others. */
    private final int[] broodKinds;
    /** The spawner that entered last, which the escorts entering after it circle. */
    private Enemy lastCarrier;
    /**
     * M5 part B: per staggered walker wave its volley clock, in steps since the first of its units
     * came onto the screen (counting that step as 1); 0 before (see {@link Enemy#staggeredVolley}).
     */
    private final int[] volleyClocks;
    /**
     * M7 part B: per Burrower wave its stagger clock (design/enemies/ground/burrower, Swarm stagger): steps
     * until the next bulge of its units may start; 0 when one may now.
     */
    private final int[] burrowWaits;
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

    /** Plans the waves with {@code rng}, which also spreads the aimed shots later. */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<LevelScript.GroundUnit> groundUnits,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes) {
        this(waveSpecs, groundUnits, List.of(), rng, rules, events, escapes);
    }

    /** @param extraKinds enemies that enter outside the waves (a boss's streams), among the kinds too */
    EnemyForce(
            List<WaveSpec> waveSpecs,
            List<LevelScript.GroundUnit> groundUnits,
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
            List<LevelScript.GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            SplitMix64 rng,
            Rules rules,
            SimEvents events,
            Escapes escapes,
            java.util.function.Function<WaveSpec, Optional<Formations.Span>> spans) {
        this.rng = rng;
        this.rules = rules;
        this.events = events;
        this.escapes = escapes;
        enemies = new Pool<>(ENEMY_CAPACITY, () -> new Enemy(events), Enemy[]::new);
        List<EnemySpec> distinct = new ArrayList<>(
                waveSpecs.stream().map(WaveSpec::enemy).distinct().toList());
        for (LevelScript.GroundUnit unit : groundUnits) {
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
        auras = kinds.stream().anyMatch(kind -> kind.aura().isPresent());
        rollers = kinds.stream().anyMatch(kind -> kind.roll().isPresent());
        segmentKinds = new int[kinds.size()];
        tailKinds = new int[kinds.size()];
        regrownKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            var chain = kinds.get(k).chain();
            segmentKinds[k] = chain.isPresent() ? kinds.indexOf(chain.get().segment()) : -1;
            tailKinds[k] = chain.isPresent() ? kinds.indexOf(chain.get().tail()) : -1;
            regrownKinds[k] = chain.isPresent() && chain.get().regrown() != null
                    ? kinds.indexOf(chain.get().regrown())
                    : -1;
        }
        broodKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec spec = kinds.get(k);
            broodKinds[k] = spec.brood().isPresent()
                    ? kinds.indexOf(spec.brood().get().enemy())
                    : spec.spawner().isPresent()
                            ? kinds.indexOf(spec.spawner().get().enemy())
                            : -1;
        }
        this.waves = new WaveSchedule(waveSpecs, kinds, rng, spans);
        volleyClocks = new int[waves.volleyClocks()];
        burrowWaits = new int[waves.burrowClocks()];
        this.groundUnits = groundUnits;
        groundKinds = new int[groundUnits.size()];
        groundTicks = new int[groundUnits.size()];
        groundCurrentX = new double[groundUnits.size()];
        groundCurrentY = new double[groundUnits.size()];
        for (int i = 0; i < groundUnits.size(); i++) {
            groundKinds[i] = kinds.indexOf(groundUnits.get(i).enemy());
            groundTicks[i] = SimStep.ticks(groundUnits.get(i).t());
            groundCurrentX[i] = StrictMath.sin(groundUnits.get(i).currentRadians());
            groundCurrentY[i] = -StrictMath.cos(groundUnits.get(i).currentRadians());
        }
        burstSlot = new int[groundUnits.size()];
        java.util.Arrays.fill(burstSlot, -1);
        int bursting = (int)
                groundUnits.stream().filter(unit -> unit.burst().isPresent()).count();
        burstUnit = new int[bursting];
        burstTelegraphTicks = new int[bursting];
        burstTelegraphed = new boolean[bursting];
        burstShift = new double[bursting];
        burstBase = new double[bursting];
        burstFoot = new double[bursting];
        burstFirst = new boolean[bursting];
        int b = 0;
        for (int i = 0; i < groundUnits.size(); i++) {
            LevelScript.GroundUnit unit = groundUnits.get(i);
            if (unit.burst().isEmpty()) {
                continue;
            }
            LevelScript.RootBurst burst = unit.burst().get();
            burstSlot[i] = b;
            burstUnit[b] = i;
            burstTelegraphTicks[b] = SimStep.ticks(burst.t());
            boolean first = true;
            double foot = 0;
            for (int j = 0; j < groundUnits.size(); j++) {
                LevelScript.GroundUnit other = groundUnits.get(j);
                if (other.burst().isPresent() && other.burst().get().index() == burst.index()) {
                    first &= j >= i;
                    foot = Math.max(
                            foot,
                            Math.abs(other.x() - burst.x())
                                    + other.enemy().hitbox().width() / 2);
                }
            }
            burstFirst[b] = first;
            burstFoot[b] = foot;
            b++;
        }
        int sides = (int)
                groundUnits.stream().filter(unit -> unit.side().isPresent()).count();
        sideWarningStarts = new int[sides];
        sideWarningEnds = new int[sides];
        sideWarningEdges = new int[sides];
        int w = 0;
        for (LevelScript.GroundUnit unit : groundUnits) {
            if (unit.side().isPresent()) {
                sideWarningStarts[w] = SimStep.ticks(unit.t() - WaveSchedule.EDGE_WARNING_SECONDS);
                sideWarningEnds[w] = SimStep.ticks(unit.t());
                sideWarningEdges[w] = unit.side().get().fromLeft() ? WarningEdge.LEFT.bit() : WarningEdge.RIGHT.bit();
                w++;
            }
        }
    }

    /**
     * The target-the-objective hook in mode {@code nearest} (design/enemies): the aimed attacks of
     * the enemies {@code slugs} go at the ship or the nearest unit of {@code target}, whichever is
     * closer, chosen every step and as each shot is fired.
     */
    void hook(Convoy target, List<String> slugs) {
        convoy = target;
        modes = new LevelScript.HookMode[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            modes[k] = slugs.contains(kinds.get(k).slug()) ? LevelScript.HookMode.NEAREST : LevelScript.HookMode.NONE;
        }
    }

    /**
     * M5 part G (design/campaign Level 13, What hurts the relay; user decision G1 = a): the hook of a
     * defend primary, {@code target} its ally, each enemy kind in its {@link LevelScript.Defend#mode
     * mode}.
     */
    void hook(Convoy target, LevelScript.Defend defend) {
        hook(target, defend.targetedBy());
    }

    /**
     * The hook of {@code target} with each enemy kind in its mode in {@code targetedBy} (not listed: not
     * hooked): a defend primary's (M5 part G) or (M7 part D) the ground allies'.
     */
    void hook(Convoy target, java.util.Map<String, LevelScript.HookMode> targetedBy) {
        convoy = target;
        modes = new LevelScript.HookMode[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            modes[k] = targetedBy.getOrDefault(kinds.get(k).slug(), LevelScript.HookMode.NONE);
        }
        allyHits = target.groundAllies() ? new double[target.size()] : new double[0];
    }

    /**
     * M7 part D: convoy unit {@code k} takes {@code hits} from a hooked attack this step (the sortie applies
     * them after the moves, {@link #allyHits(int)}); D3's Shellback roll crossing a dome adds its {@link
     * AllySpec#rolls()} here. Nothing without ground allies.
     */
    void hitAlly(int k, double hits) {
        if (k >= 0 && k < allyHits.length && hits > 0) {
            allyHits[k] += hits;
        }
    }

    /** M7 part D: the hits the hooked attacks dealt ground ally {@code k} since the last {@link #move} began. */
    double allyHits(int k) {
        return k < allyHits.length ? allyHits[k] : 0;
    }

    /** M7 part D: whether the hook aims at ground allies (Level 17's domes). */
    private boolean groundHook() {
        return convoy != null && convoy.groundAllies();
    }

    /**
     * M7 part D (design/campaign Level 17, What goes for the domes): whether {@code enemy}, just let in, flies
     * a bomb run: a mine layer of a kind the ground allies' hook names {@code always}, entering on its own
     * path (not a carrier's escort).
     */
    private boolean bombs(Enemy enemy, Spawn spawn) {
        return groundHook()
                && mode(enemy.kind()) == LevelScript.HookMode.ALWAYS
                && enemy.spec().gun().isPresent()
                && enemy.spec().gun().get().mine().isPresent()
                && spawn.escort().isEmpty()
                && spawn.walk().isEmpty()
                && spawn.release().isEmpty()
                && !spawn.lane()
                && spawn.field().isEmpty()
                && enemy.spec().burrow().isEmpty();
    }

    /** The hook mode of enemy kind {@code kind}; {@link LevelScript.HookMode#NONE} without a convoy. */
    private LevelScript.HookMode mode(int kind) {
        return modes == null ? LevelScript.HookMode.NONE : modes[kind];
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
        Pools.clear(bullets);
        Pools.clear(mines);
        Pools.clear(lobs);
        Pools.clear(chains);
        chainsSpawned = 0;
        Pools.clear(flocks);
        flocksSpawned = 0;
        joining = null;
        waves.reset();
        spawned = 0;
        nextGround = 0;
        lastCarrier = null;
        java.util.Arrays.fill(volleyClocks, 0);
        java.util.Arrays.fill(burrowWaits, 0);
        java.util.Arrays.fill(allyHits, 0);
        java.util.Arrays.fill(burstTelegraphed, false);
        java.util.Arrays.fill(burstShift, 0);
        java.util.Arrays.fill(burstBase, 0);
    }

    /**
     * M5 part H, the root bursts' telegraphs (design/campaign Level 14, Hazards; decision H2 = a), before
     * {@link #spawn} in a step: every burst whose time has come and whose telegraph has not started in
     * this attempt starts it now, moved away from the ship at {@code shipX} when its centre lies closer
     * than {@link LevelScript.RootBurst#SHIP_CLEARANCE} (its event once per burst); {@code groundScroll}
     * is the ground's scroll after this step's {@code scrollStep}.
     */
    void bursts(int levelTick, double shipX, double groundScroll, double scrollStep) {
        burstShipX = shipX;
        burstScroll = groundScroll;
        burstScrollStep = scrollStep;
        for (int b = 0; b < burstUnit.length; b++) {
            if (!burstTelegraphed[b] && burstUnit[b] >= nextGround && levelTick >= burstTelegraphTicks[b]) {
                telegraph(b);
            }
        }
    }

    /** M5 part H: burst slot {@code b}'s telegraph starts: its move away from the ship, its point, its event. */
    private void telegraph(int b) {
        LevelScript.RootBurst burst = groundUnits.get(burstUnit[b]).burst().orElseThrow();
        burstShift[b] = clearOfShip(burst.x(), burstFoot[b], burstShipX);
        burstBase[b] = PlayField.HEIGHT - burst.depth() + burstScroll;
        burstTelegraphed[b] = true;
        if (burstFirst[b]) {
            events.add(
                    SimEvents.Type.ROOT_BURST_TELEGRAPH,
                    burst.x() + burstShift[b],
                    PlayField.HEIGHT - burst.depth(),
                    burst.index());
        }
    }

    /**
     * M5 part H: how far a burst centred at {@code x}, its growths reaching {@code foot} px either side,
     * moves sideways so that it lies {@link LevelScript.RootBurst#SHIP_CLEARANCE} px from the ship at
     * {@code shipX}: 0 when it does already; else away from the ship (a burst right under it toward the
     * field's middle), to the other side when the field has no room there, kept on the field.
     */
    static double clearOfShip(double x, double foot, double shipX) {
        double clearance = LevelScript.RootBurst.SHIP_CLEARANCE;
        double d = x - shipX;
        if (Math.abs(d) >= clearance) {
            return 0;
        }
        double away = d > 0 ? 1 : d < 0 ? -1 : shipX <= PlayField.WIDTH / 2.0 ? 1 : -1;
        double low = Math.min(foot, PlayField.WIDTH / 2.0);
        double high = PlayField.WIDTH - low;
        double to = shipX + away * clearance;
        if (to < low || to > high) {
            to = shipX - away * clearance;
        }
        return Math.clamp(to, low, high) - x;
    }

    /** Lets in every unit due at {@code levelTick}, flying and on the ground. */
    void spawn(int levelTick) {
        for (Spawn spawn = waves.due(levelTick); spawn != null; spawn = waves.due(levelTick)) {
            if (spawn.enemy().chain().isPresent()) {
                spawnChain(spawn);
                continue;
            }
            if (spawn.swarm().isPresent()) {
                spawnMember(spawn);
                continue;
            }
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                enemy.spawn(spawn, spawned);
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
                if (bombs(enemy, spawn)) {
                    // M7 part D: a front entry descends to the bomb row, a side entry keeps its entry row.
                    LevelScript.GroundAllies allies = convoy.groundSpec();
                    double row =
                            waves.lastEntry() == WaveSpec.Entry.SIDES ? enemy.y() : PlayField.HEIGHT - allies.bombRow();
                    double slot = (waves.lastPlace() - (waves.lastWaveSize() - 1) / 2.0) * allies.bombSlot();
                    enemy.bombRun(row, slot);
                }
            }
            if (spawn.enemy().brood().isPresent() || spawn.enemy().aura().isPresent()) {
                // M7 part D: a Herald is a carrier too; its escorts circle it inside its aura.
                lastCarrier = enemy;
            }
            spawned++;
        }
        while (nextGround < groundTicks.length && groundTicks[nextGround] <= levelTick) {
            LevelScript.GroundUnit unit = groundUnits.get(nextGround);
            Enemy enemy = enemies.obtain();
            if (enemy != null && unit.burst().isPresent()) {
                // M5 part H: a root burst's growth stands at its burst's point, which scrolled since the telegraph.
                int b = burstSlot[nextGround];
                if (!burstTelegraphed[b]) {
                    // Its telegraph fell before a boss checkpoint the attempt restarted from: it starts now.
                    telegraph(b);
                }
                LevelScript.RootBurst burst = unit.burst().get();
                enemy.root(unit.enemy(), groundKinds[nextGround], unit.x(), spawned, unit.group());
                // The ground units scroll by this step's distance after they are let in.
                double y = burstBase[b] - burstScroll + burstScrollStep;
                enemy.grow(burst.index(), unit.x() + burstShift[b], y, SimStep.ticks(burst.firstShotSeconds()));
                if (burstFirst[b]) {
                    events.add(
                            SimEvents.Type.ROOT_BURST_GREW,
                            burst.x() + burstShift[b],
                            burstBase[b] - burstScroll,
                            burst.index());
                }
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
     * A wave's segment chain enters: its head (the unit that counts as the kill, carrying the
     * wave's pickup), its segments and its tail, strung out behind the path's start; members the
     * pool has no room for are left out.
     */
    private void spawnChain(Spawn spawn) {
        Chain chain = chains.obtain();
        if (chain == null) {
            spawned++;
            return;
        }
        EnemySpec head = spawn.enemy();
        EnemySpec.ChainSpec spec = head.chain().orElseThrow();
        if (head.crawl().isPresent()) {
            // M7 part C: a ground chain crawls its path in ground space (the Threadcrawler).
            chain.startGround(spec, chainsSpawned++, head.crawl().get(), groundScroll, waves.lastEntry());
        } else {
            chain.start(spec, chainsSpawned++, spawn, regrownKinds[spawn.kind()], waves.lastEntry());
        }
        int members = spec.members();
        for (int i = 0; i < members; i++) {
            Enemy member = enemies.obtain();
            if (member == null) {
                break;
            }
            boolean tail = i == members - 1;
            EnemySpec part = i == 0 ? head : tail ? spec.tail() : spec.segment();
            int kind = i == 0 ? spawn.kind() : tail ? tailKinds[spawn.kind()] : segmentKinds[spawn.kind()];
            Hitbox box = i == 0 || tail ? part.hitbox() : spec.segmentBoxes().get(i - 1);
            member.link(chain, i, part, kind, box, spawned++, 0, 0, 0);
            if (i == 0) {
                member.carry(spawn.carried());
            }
            chain.member(i, member);
        }
        chain.settle();
    }

    /**
     * M5 part D: a swarm's member enters: the first of its wave starts the flock (null when the pool
     * has none free: then the wave's members are left out), the others join it in entry order.
     */
    private void spawnMember(Spawn spawn) {
        Spawn.Swarm swarm = spawn.swarm().orElseThrow();
        if (swarm.member() == 0) {
            joining = flocks.obtain();
            if (joining != null) {
                joining.start(spawn.enemy().flock().orElseThrow(), flocksSpawned++, swarm.route(), waves.lastEntry());
            }
        }
        if (joining != null) {
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                enemy.join(joining, swarm.member(), spawn, spawned, swarm.x(), swarm.y(), joining.startFacing());
                enemy.entered(waves.lastEntry());
                enemy.tag(waves.lastTag());
                joining.member(swarm.member(), enemy, swarm.x(), swarm.y());
            }
        }
        spawned++;
    }

    /**
     * M5 part D: the flocks fly a step; a loop-back raises its event; once a flock has flown its
     * course its members off the play field are gone (escaped), and those still on it a while later
     * too; a flock without members ends.
     */
    void moveFlocks() {
        for (int f = flocks.size() - 1; f >= 0; f--) {
            Flock flock = flocks.get(f);
            flock.advance(canyon);
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
            if (!flock.alive()) {
                flocks.free(f);
            }
        }
    }

    /** M5 part D: a flock's member was destroyed (before it is freed): it drops out of its flock. */
    void flockMemberGone(Enemy enemy) {
        enemy.flock().drop(enemy.member());
    }

    Pool<Flock> flocks() {
        return flocks;
    }

    /**
     * The chains fly a step: a rear part whose new head has grown gets it (and lunges at the ship),
     * the dying members pop one by one, and a chain that has flown its course leaves with its
     * members (its wave's head counting as escaped).
     */
    void moveChains(Ship ship, boolean firing) {
        for (int c = chains.size() - 1; c >= 0; c--) {
            Chain chain = chains.get(c);
            if (chain.regrowDue()) {
                Enemy head = null;
                if (chain.alive()) {
                    head = enemies.obtain();
                    if (head != null) {
                        int kind = chain.regrownKind();
                        EnemySpec regrown = chain.spec().regrown();
                        head.link(
                                chain,
                                0,
                                regrown,
                                kind,
                                regrown.hitbox(),
                                spawned++,
                                chain.headX(),
                                chain.headY(),
                                chain.headFacing());
                        events.add(SimEvents.Type.CHAIN_REGROWN, chain.headX(), chain.headY(), kind);
                    }
                }
                chain.grown(head, ship.x(), ship.y());
            }
            Enemy popped = chain.popDue();
            if (popped != null) {
                events.add(
                        SimEvents.Type.CHAIN_POP,
                        popped.x(),
                        popped.y(),
                        SimEvents.chainPopValue(popped.kind(), popped.hitbox().width()));
                free(popped);
            }
            if (!chain.advance(groundScroll)) {
                for (int i = 0; i < chain.size(); i++) {
                    Enemy member = chain.member(i);
                    if (member != null) {
                        if (i == 0 && chain.original()) {
                            escapes.escaped(member);
                        }
                        free(member);
                    }
                }
                chains.free(c);
            } else if (!chain.alive() && !chain.regrowing()) {
                chains.free(c);
            } else if (chain.poreFiring() >= 0 && firing) {
                firePore(chain, ship);
            }
        }
    }

    /**
     * M7 part C (design/enemies/ground/threadcrawler, Travelling wave): the pore due in {@code chain}'s wave
     * fires its slow spore at the ship, from a segment on the play field that is neither destroyed nor doomed,
     * within the bullet budget and not too close to the ship ({@link SimEvents.Type#PORE_FIRED}).
     */
    private void firePore(Chain chain, Ship ship) {
        int pore = chain.poreFiring();
        Enemy member = chain.member(pore);
        if (member == null || !PlayerFire.onField(member) || member.empStunned()) {
            return;
        }
        EnemySpec.PoreWave wave = chain.crawl().pores();
        double dx = ship.x() - member.x();
        double dy = ship.y() - member.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        bullets.obtain()
                .fire(
                        member.x(),
                        member.y(),
                        dx / distance * wave.bulletSpeed(),
                        dy / distance * wave.bulletSpeed(),
                        wave.damage());
        events.add(SimEvents.Type.PORE_FIRED, member.x(), member.y(), pore);
    }

    /** Frees the unit {@code enemy} from the pool. */
    private void free(Enemy enemy) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (enemies.get(i) == enemy) {
                enemies.free(i);
                return;
            }
        }
    }

    /**
     * A chain member was destroyed (before it is freed): the chain is cut there; a split-off rear
     * part becomes a chain of its own that grows a new head.
     */
    void memberDestroyed(Enemy enemy) {
        Chain chain = enemy.chain();
        Chain rear = chains.obtain();
        boolean split = chain.destroyed(enemy.link(), rear, chainsSpawned);
        if (split) {
            chainsSpawned++;
            events.add(SimEvents.Type.CHAIN_CUT, enemy.x(), enemy.y(), enemy.kind());
        } else if (rear != null) {
            chains.free(chains.size() - 1);
        }
        if (chain.takeCut()) {
            // M7 part C: a ground chain's cut stops its rear part.
            events.add(SimEvents.Type.CHAIN_CUT, enemy.x(), enemy.y(), enemy.kind());
        }
    }

    /** M7 part C: whether destroying chain head {@code enemy} now pays its head-first bonus (call before it is cut). */
    boolean headFirstBonusDue(Enemy enemy) {
        return enemy.link() == 0 && enemy.chain().headFirstBonusDue();
    }

    /** M7 part C: the ground has scrolled {@code scroll} px in this attempt (set every step before the spawns). */
    void groundScroll(double scroll) {
        groundScroll = scroll;
    }

    /**
     * M7 part C: the level's tendril gates {@code levelLinks} of {@code gates}: each gate's anchors among the
     * ground units (the first unused unit at its time and x of its anchor's kind, as {@link
     * LevelScript#withGates} adds them) start its tendril as they enter.
     */
    void links(Links levelLinks, List<LevelScript.Gate> gates) {
        links = levelLinks;
        gateOf = new int[groundUnits.size()];
        java.util.Arrays.fill(gateOf, -1);
        for (int g = 0; g < gates.size(); g++) {
            LevelScript.Gate gate = gates.get(g);
            for (int side = 0; side < 2; side++) {
                double x = side == 0 ? gate.leftX() : gate.rightX();
                int found = -1;
                for (int i = 0; i < groundUnits.size() && found < 0; i++) {
                    LevelScript.GroundUnit unit = groundUnits.get(i);
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
        return chains;
    }

    /**
     * Flies every unit and scrolls the ground units by {@code groundScroll}; those whose gun is
     * ready fire at the ship when {@code firing}. A grouped ground unit that leaves the screen
     * alive is reported as escaped.
     */
    void move(Ship ship, boolean firing, double groundScroll) {
        if (allyHits.length > 0) {
            java.util.Arrays.fill(allyHits, 0);
        }
        if (chains.size() > 0) {
            moveChains(ship, firing);
        }
        if (flocks.size() > 0) {
            moveFlocks();
        }
        for (int g = 0; g < volleyClocks.length; g++) {
            if (volleyClocks[g] > 0) {
                volleyClocks[g]++;
            }
        }
        for (int g = 0; g < burrowWaits.length; g++) {
            if (burrowWaits[g] > 0) {
                burrowWaits[g]--;
            }
        }
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            // M7 part A: a unit the EMP Burst stuns at the step's start does nothing of its own this step.
            boolean stunned = enemy.empStunned();
            if (!stunned && enemy.bombLoaded()) {
                // M7 part D: its bomb run steers to the next dome coming down.
                int dome = convoy.nextDome(enemy.x(), enemy.y());
                enemy.bombTarget(dome, dome < 0 ? 0 : convoy.get(dome).x());
            }
            int target = target(enemy, ship);
            double aimX = target < 0 ? ship.x() : convoy.get(target).x();
            double aimY = target < 0 ? ship.y() : convoy.get(target).y();
            // M5 part G: a diver under the always hook dives at the objective while it can be aimed at.
            int dive = mode(enemy.kind()) == LevelScript.HookMode.ALWAYS ? convoy.objective(enemy.x(), enemy.y()) : -1;
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
            if (flying && !stunned && enemy.bombRun() >= 0 && convoy.over(enemy.bombRun(), enemy.x(), enemy.y())) {
                // M7 part D: over its dome's hit box: it drops its load, one hit, and flies on down.
                int dome = enemy.bombRun();
                enemy.dropBomb();
                events.add(SimEvents.Type.BOMB_DROPPED, enemy.x(), enemy.y(), dome);
                hitAlly(dome, convoy.spec(dome).bombs());
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
                crush(enemy);
            }
            if (!flying) {
                if (enemy.spec().brood().isPresent()) {
                    carrierEnded(enemy);
                }
                if (enemy.spec().aura().isPresent()) {
                    heraldGone(enemy, false);
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
                pulse(enemy, ship, firing);
            } else if (enemy.burrowing()) {
                burrow(enemy, ship, firing);
            } else if (enemy.trigger() && firing) {
                EnemyGun gun = enemy.spec().gun().orElseThrow();
                if (gun.mine().isPresent()) {
                    dropMine(enemy, gun);
                } else if (gun.mortar().isPresent()) {
                    lob(enemy, gun, ship);
                } else if (gun.up()) {
                    fireUp(enemy, gun);
                } else if (mode(enemy.kind()) == LevelScript.HookMode.ALTERNATE) {
                    fireAt(enemy, ship, alternate(enemy, ship));
                } else {
                    fireAt(enemy, ship, target(enemy, ship));
                }
            }
        }
        if (auras) {
            // M7 part D: the Heralds' rings after the moves (the hits of this step meet this step's rings).
            Auras.step(enemies);
        }
    }

    /**
     * M7 part D (design/enemies/ground/shellback, Curl and roll; design/allies): a rolling ball over a standing
     * ground ally's hit box crushes it: one hit per roll per ally (its {@link AllySpec#rolls()}), whatever the
     * hook says (easy's unhooked Shellbacks crush too), applied with the step's other hooked hits.
     */
    private void crush(Enemy enemy) {
        Hitbox ball = enemy.hitbox();
        for (int k = 0; k < convoy.size(); k++) {
            Ally ally = convoy.get(k);
            if (enemy.crushedAlly(k)
                    || !ally.alive()
                    || !ball.overlaps(enemy.x(), enemy.y(), convoy.spec(k).hitbox(), ally.x(), ally.y())) {
                continue;
            }
            enemy.markCrushed(k);
            events.add(SimEvents.Type.ROLL_CRUSHED, enemy.x(), enemy.y(), k);
            hitAlly(k, convoy.spec(k).rolls());
        }
    }

    /** M7 part D: whether a kind of the level has a curl-roll (the sortie's ground objects can be crushed). */
    boolean rollers() {
        return rollers;
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

    /**
     * M5 part E, a proximity ring (design/enemies/naval/driftjelly, user decision E3 = b): a unit on
     * the screen whose ring is ready fires it when the ship's centre is within its reach, surfaced or
     * submerged (the bullets are on the player's plane either way); its cooldown then starts. A ring
     * due while the ship is closer than the bullets may spawn waits until it is not.
     */
    private void pulse(Enemy enemy, Ship ship, boolean firing) {
        EnemySpec.ProximityRing ring = enemy.spec().ring().get();
        if (!firing || !enemy.ringReady() || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
            return;
        }
        double dx = ship.x() - enemy.x();
        double dy = ship.y() - enemy.y();
        double distance = dx * dx + dy * dy;
        if (distance > ring.within() * ring.within() || distance < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE) {
            return;
        }
        burst(enemy.x(), enemy.y(), ring.count(), ring.bulletSpeed(), ring.damage(), ship);
        enemy.ringFired();
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) ring.damage());
    }

    /**
     * M7 part B, a Burrower's step after its move (design/enemies/ground/burrower): its surfacing's and
     * dive's events, its rings when due (while {@code firing}; none while the ship is closer than bullets
     * may spawn), and, waiting at its spot, its wave's stagger: a spot that is no longer allowed (the ship
     * within the clearance, a no-surface zone under it) sends it to a new one; otherwise it bulges once its
     * wave's clock lets it and fewer than its cap of its wave would still be up when it surfaces.
     */
    private void burrow(Enemy enemy, Ship ship, boolean firing) {
        Burrowing cycle = enemy.burrow();
        int now = cycle.now();
        if ((now & Burrowing.NOW_SURFACED) != 0) {
            events.add(SimEvents.Type.BURROW_SURFACED, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & Burrowing.NOW_DIVE) != 0) {
            events.add(SimEvents.Type.BURROW_DIVE, enemy.x(), enemy.y(), enemy.kind());
        }
        EnemySpec.BurrowRing ring = cycle.ringDue();
        if (ring != null && firing) {
            burst(enemy.x(), enemy.y(), ring.count(), ring.bulletSpeed(), ring.damage(), ship);
            events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) ring.damage());
        }
        if (!cycle.waiting()) {
            return;
        }
        if (!cycle.spotAllowed(ship.x(), ship.y())) {
            cycle.repick(ship.x(), ship.y());
            return;
        }
        int group = enemy.burrowGroup();
        if (group >= 0 && burrowWaits[group] > 0) {
            return;
        }
        EnemySpec.Burrow spec = enemy.spec().burrow().orElseThrow();
        if (group >= 0 && upWhenSurfacing(enemy, group, cycle.bulgeTicks()) >= spec.maxUp()) {
            return;
        }
        cycle.bulge();
        if (group >= 0) {
            burrowWaits[group] = SimStep.ticks(spec.staggerSeconds());
        }
        events.add(SimEvents.Type.BURROW_BULGE, enemy.x(), enemy.y(), enemy.kind());
    }

    /** M7 part B: how many other units of Burrower wave {@code group} will still be up {@code ticks} steps from now. */
    private int upWhenSurfacing(Enemy enemy, int group, int ticks) {
        int up = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy other = enemies.get(j);
            if (other != enemy && other.burrowGroup() == group && other.burrow().upFor() > ticks) {
                up++;
            }
        }
        return up;
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
            lob(enemy, enemy.spec().gun().get(), ship);
        } else if (fan) {
            boolean atShip = enemy.spec().walker().orElseThrow().fanAtShip();
            int inArc = mode(enemy.kind()) == LevelScript.HookMode.IN_ARC ? inArc(enemy) : -1;
            if (inArc >= 0) {
                // M5 part G: the objective within its armour's arc: the fan centred on it, aimed at it.
                fire(enemy.x(), enemy.y(), enemy.spec().gun().orElseThrow(), false, 0, ship, inArc, false);
            } else {
                fire(enemy.x(), enemy.y(), enemy.spec().gun().orElseThrow(), false, enemy.facing(), ship, -1, !atShip);
            }
        }
        if (spit) {
            EnemyGun gun = enemy.spec().walker().orElseThrow().spit().orElseThrow();
            int target = mode(enemy.kind()) == LevelScript.HookMode.NEAREST
                    ? convoy.nearest(enemy.x(), enemy.y(), ship.x(), ship.y())
                    : -1;
            fire(enemy.x(), enemy.y(), gun, false, 0, ship, target, false);
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
        double toShip = StrictMath.atan2(ship.y() - from.y(), ship.x() - from.x());
        for (int k = 0; k < count; k++) {
            double angle = count == 1 ? toShip : toShip - arc / 2 + k * arc / (count - 1);
            Enemy unit = enemies.obtain();
            if (unit != null) {
                unit.hatch(enemy, kind, from.x(), from.y(), Trig.cos(angle) * speed, Trig.sin(angle) * speed, spawned);
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

    /**
     * M5 part H (design/enemies, Boss attacks and root bursts; decision H5 = a): a boss part at (x, y)
     * lobs {@code gun}'s mortar at where the ship is now (the marker does not follow it).
     */
    void bossLob(double x, double y, EnemyGun gun, Ship ship) {
        Lob lob = lobs.obtain();
        if (lob != null) {
            lob.launch(gun, x, y, ship.x(), ship.y());
            events.add(SimEvents.Type.MORTAR_LOBBED, x, y);
        }
    }

    /** M5 part H: the root bursts' state, hashed only in a level with them (Act 1's and Levels 08–13's hashes stay). */
    void addBurstsTo(StateHash hash) {
        for (int b = 0; b < burstUnit.length; b++) {
            hash.add(burstTelegraphed[b] ? 1 : 0).add(burstShift[b]).add(burstBase[b]);
        }
    }

    /** M5 part H: the root bursts' growths in the level (each growth a slot; a burst's first is its burst's). */
    int burstSlots() {
        return burstUnit.length;
    }

    /** M5 part H: whether slot {@code b} is its burst's first growth (the burst's own state and events go by it). */
    boolean burstFirst(int b) {
        return burstFirst[b];
    }

    /** M5 part H: slot {@code b}'s burst. */
    LevelScript.RootBurst burstOf(int b) {
        return groundUnits.get(burstUnit[b]).burst().orElseThrow();
    }

    /** M5 part H: whether slot {@code b}'s telegraph has started in this attempt. */
    boolean burstTelegraphed(int b) {
        return burstTelegraphed[b];
    }

    /** M5 part H: whether slot {@code b}'s growth has appeared in this attempt (or the attempt restarted past it). */
    boolean burstGrown(int b) {
        return burstUnit[b] < nextGround;
    }

    /** M5 part H: slot {@code b}'s burst's centre x after its move away from the ship. */
    double burstX(int b) {
        return burstOf(b).x() + burstShift[b];
    }

    /** M5 part H: slot {@code b}'s burst's point's height in ground space: minus the ground's scroll, its screen height. */
    double burstBase(int b) {
        return burstBase[b];
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

    /**
     * Whom a unit's aimed attack goes at now: the convoy unit the hook picks, or -1 for the ship
     * (units without the hook, fans and mines always aim at the ship).
     */
    private int target(Enemy enemy, Ship ship) {
        LevelScript.HookMode mode = mode(enemy.kind());
        if (mode == LevelScript.HookMode.ALWAYS) {
            // M5 part G: every attack of the unit (a fan, a lob, a turret's barrel) at the objective.
            return enemy.spec().gun().isPresent()
                            && enemy.spec().gun().get().mine().isEmpty()
                    ? convoy.objective(enemy.x(), enemy.y())
                    : -1;
        }
        if (mode != LevelScript.HookMode.NEAREST || enemy.spec().gun().isEmpty()) {
            return -1;
        }
        EnemyGun gun = enemy.spec().gun().get();
        if (gun.fan() != 1 || gun.mine().isPresent()) {
            return -1;
        }
        return convoy.nearest(enemy.x(), enemy.y(), ship.x(), ship.y());
    }

    /**
     * M5 part G, the {@code alternate} hook (user decision G1 = a): whom the shot {@link Enemy#trigger()}
     * let go goes at. A burst's first shot starts an attack: every second attack goes at the objective
     * while it is closer than the ship (centre to centre), the others at the ship; the burst's other
     * shots go where its first went.
     */
    private int alternate(Enemy enemy, Ship ship) {
        if (enemy.burstShot() == 0) {
            int target = -1;
            boolean second = enemy.startAttack() % 2 == 0;
            if (second && groundHook()) {
                // M7 part D: the nearest ground ally on the screen while it is closer than the ship.
                target = convoy.nearestCloser(enemy.x(), enemy.y(), ship.x(), ship.y());
            } else if (second) {
                int objective = convoy.objective();
                if (objective >= 0) {
                    Ally ally = convoy.get(objective);
                    double ox = ally.x() - enemy.x();
                    double oy = ally.y() - enemy.y();
                    double sx = ship.x() - enemy.x();
                    double sy = ship.y() - enemy.y();
                    target = ox * ox + oy * oy < sx * sx + sy * sy ? objective : -1;
                }
            }
            enemy.attackTarget(target);
        }
        return enemy.attackTarget();
    }

    /**
     * M5 part G, the {@code in-arc} hook: the objective a walker's fan goes at, when it can be aimed at
     * and lies within the walker's armour's arc of its facing (design/enemies/ground/scuttler: ±45°);
     * -1 otherwise.
     */
    private int inArc(Enemy enemy) {
        int objective = convoy.objective(enemy.x(), enemy.y());
        if (objective < 0) {
            return -1;
        }
        Ally ally = convoy.get(objective);
        double bearing = StrictMath.atan2(-(ally.x() - enemy.x()), -(ally.y() - enemy.y()));
        double off = Math.abs(Math.IEEEremainder(bearing - enemy.facing(), 2 * StrictMath.PI));
        return off <= enemy.spec().walker().orElseThrow().frontArc() ? objective : -1;
    }

    /**
     * A shot at the ship (or where it is going for a unit that leads the target), a turret's along
     * its barrel, or a fan centred on that line; turned by a random angle within the difficulty's
     * spread.
     */
    private void fireAt(Enemy enemy, Ship ship, int target) {
        fire(
                enemy.x(),
                enemy.y(),
                enemy.spec().gun().orElseThrow(),
                enemy.leadsTarget(),
                enemy.aim(),
                ship,
                target,
                false);
    }

    /**
     * M5 part D (design/enemies/air/wraith, user decision of 2026-10-08): one shot of a burst fired
     * straight up the screen as a fixed fan, the burst's shots in turn from the fan's left edge to
     * its right; aimed at nobody, without the difficulty's aim spread, and fired however close the
     * ship is (within the bullet budget).
     */
    private void fireUp(Enemy enemy, EnemyGun gun) {
        if (bullets.size() >= rules.bulletBudget()) {
            return;
        }
        int shot = enemy.burstShot();
        double angle = StrictMath.PI / 2
                + (gun.burst() == 1 ? 0 : gun.spreadRadians() / 2 - shot * gun.spreadRadians() / (gun.burst() - 1));
        bullets.obtain()
                .fire(
                        enemy.x(),
                        enemy.y(),
                        Trig.cos(angle) * gun.bulletSpeed(),
                        Trig.sin(angle) * gun.bulletSpeed(),
                        gun.damage());
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) gun.damage());
    }

    /** Lets in one unit planned outside the waves (a boss's stream), now. */
    void release(Spawn plan) {
        Enemy enemy = enemies.obtain();
        if (enemy != null) {
            enemy.spawn(plan, spawned);
        }
        spawned++;
    }

    /**
     * One bullet from (x, y) at {@code angle} radians (0 = right, y up), within the bullet budget;
     * none when the ship is closer than the bullets may spawn.
     */
    void fireAngle(double x, double y, double angle, double speed, double damage, Ship ship) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        bullets.obtain().fire(x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, damage);
    }

    /** Adds the staggered waves' volley clocks to {@code hash}; nothing without one (Act 1's hashes stay). */
    void addVolleyClocksTo(StateHash hash) {
        for (int clock : volleyClocks) {
            hash.add(clock);
        }
        // M7 part B: the Burrower waves' stagger clocks; nothing without one.
        for (int wait : burrowWaits) {
            hash.add(wait);
        }
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
        reset();
        waves.next(scheduleNext);
        nextGround = ground;
        spawned = spawnedUnits;
    }

    /** A set piece's part fires its gun from (x, y) at the ship, as a unit that aims at once. */
    void fireFrom(double x, double y, EnemyGun gun, Ship ship) {
        fire(x, y, gun, false, 0, ship, -1, false);
    }

    /** Round 34, P2: {@code gun}'s volley from (x, y) straight down the screen, as an aimed one otherwise. */
    void fireDown(double x, double y, EnemyGun gun, Ship ship) {
        fire(x, y, gun, false, 0, ship, -1, true);
    }

    /**
     * @param target the convoy unit the shot is aimed at; -1 for the ship
     * @param alongAim whether the volley goes along {@code aim} (a walker's facing fan) whatever the gun
     */
    private void fire(
            double x, double y, EnemyGun gun, boolean leads, double aim, Ship ship, int target, boolean alongAim) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        if (target >= 0) {
            dx = convoy.get(target).x() - x;
            dy = convoy.get(target).y() - y;
            distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                return;
            }
        }
        if (alongAim || Double.isFinite(gun.turnRate())) {
            // Along the barrel: radians clockwise from straight down.
            dx = -Trig.sin(aim);
            dy = -Trig.cos(aim);
            distance = 1;
        } else if (leads && target < 0) {
            double flight = distance / gun.bulletSpeed();
            dx += ship.vx() * flight;
            dy += ship.vy() * flight;
            distance = Math.sqrt(dx * dx + dy * dy);
        }
        dx /= distance;
        dy /= distance;
        double turn = rules.aimedSpread() > 0 ? rng.range(-rules.aimedSpread(), rules.aimedSpread()) : 0;
        for (int k = 0; k < gun.fan(); k++) {
            if (bullets.size() >= rules.bulletBudget()) {
                break;
            }
            double angle =
                    turn + (gun.fan() == 1 ? 0 : -gun.spreadRadians() / 2 + k * gun.spreadRadians() / (gun.fan() - 1));
            double cos = Trig.cos(angle);
            double sin = Trig.sin(angle);
            EnemyBullet bullet = bullets.obtain();
            bullet.fire(
                    x,
                    y,
                    (dx * cos - dy * sin) * gun.bulletSpeed(),
                    (dx * sin + dy * cos) * gun.bulletSpeed(),
                    gun.damage(),
                    target);
        }
        events.add(SimEvents.Type.ENEMY_FIRED, x, y, (int) gun.damage());
    }

    /** A mine layer drops a spore under it, drifting in a random direction. */
    private void dropMine(Enemy enemy, EnemyGun gun) {
        dropMine(gun, enemy.x(), enemy.y(), rng.range(0, 2 * StrictMath.PI));
    }

    /** A spore of {@code gun}'s mine at (x, y), drifting at {@code angle} radians (0 = right, y up). */
    void dropMine(EnemyGun gun, double x, double y, double angle) {
        Mine mine = mines.obtain();
        if (mine != null) {
            mine.drop(gun, x, y, angle);
            events.add(SimEvents.Type.MINE_DROPPED, x, y);
        }
    }

    /**
     * Drifts the spores; one whose life ran out bursts into its ring (unless it never bursts),
     * away from the ship (no bullet spawns close to it).
     */
    void moveMines(Ship ship) {
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (!mine.move()) {
                EnemyGun.MineSpec spec = mine.spec();
                if (mine.expired() && spec.bursts()) {
                    burst(mine.x(), mine.y(), spec.ring(), mine.gun().bulletSpeed(), spec.ringDamage(), ship);
                    events.add(SimEvents.Type.MINE_BURST, mine.x(), mine.y());
                }
                mines.free(i);
            }
        }
    }

    /**
     * A mortar lobs a blob at where the ship is now (the marker does not follow it); M5 part G: under
     * the always hook at the objective while it can be aimed at.
     */
    private void lob(Enemy enemy, EnemyGun gun, Ship ship) {
        Lob lob = lobs.obtain();
        if (lob != null) {
            LevelScript.HookMode mode = mode(enemy.kind());
            int target = mode == LevelScript.HookMode.ALWAYS
                    ? convoy.objective(enemy.x(), enemy.y())
                    : mode == LevelScript.HookMode.ALTERNATE ? alternate(enemy, ship) : -1;
            if (target >= 0 && groundHook()) {
                // M7 part D: the marker on the dome, in ground space: it scrolls with the dome.
                lob.launchOnGround(
                        gun,
                        enemy.x(),
                        enemy.y(),
                        convoy.get(target).x(),
                        convoy.get(target).y(),
                        target);
            } else if (target >= 0) {
                lob.launch(
                        gun,
                        enemy.x(),
                        enemy.y(),
                        convoy.get(target).x(),
                        convoy.get(target).y(),
                        target);
            } else {
                lob.launch(gun, enemy.x(), enemy.y(), ship.x(), ship.y());
            }
            events.add(SimEvents.Type.MORTAR_LOBBED, enemy.x(), enemy.y());
        }
    }

    /**
     * Flies the lobs, their start scrolling with the ground by {@code groundScroll}; one that lands
     * bursts into its ring (whether the ship is near or not: the ring starts just outside the impact
     * circle) and hits a ship within its impact circle. Returns the damage of the direct hits in this
     * step, which the ship takes when it can be hit.
     */
    double moveLobs(Ship ship, double groundScroll) {
        return moveLobs(ship, groundScroll, null);
    }

    /**
     * As {@link #moveLobs(Ship, double)}; M7 part A: a direct hit whose blob lands in a Deflector Pod's
     * field while it holds a charge is caught instead (its ring still forms). {@code deflectors}: null
     * while nothing can catch (the ship does not fly).
     */
    double moveLobs(Ship ship, double groundScroll, Deflectors deflectors) {
        double direct = 0;
        objectiveDamage = 0;
        for (int i = lobs.size() - 1; i >= 0; i--) {
            Lob lob = lobs.get(i);
            lob.scroll(groundScroll);
            if (!lob.fly()) {
                continue;
            }
            EnemyGun gun = lob.gun();
            EnemyGun.MortarSpec mortar = gun.mortar().orElseThrow();
            double x = lob.targetX();
            double y = lob.targetY();
            // The ring starts just outside the impact circle, so a ship hit directly is not also
            // hit by the ring as it forms.
            double start = mortar.impactRadius() + RING_MARGIN;
            for (int k = 0; k < mortar.ring() && bullets.size() < rules.bulletBudget(); k++) {
                double angle = 2 * StrictMath.PI * k / mortar.ring();
                double cos = Trig.cos(angle);
                double sin = Trig.sin(angle);
                bullets.obtain()
                        .fire(
                                x + cos * start,
                                y + sin * start,
                                cos * gun.bulletSpeed(),
                                sin * gun.bulletSpeed(),
                                mortar.ringDamage());
            }
            double dx = ship.x() - x;
            double dy = ship.y() - y;
            boolean hit = dx * dx + dy * dy <= mortar.impactRadius() * mortar.impactRadius();
            if (hit && deflectors != null && deflectors.catches(x, y, events)) {
                hit = false;
            }
            if (hit) {
                direct += gun.damage();
            }
            // M5 part G: a lob aimed at the objective deals its direct hit to it when it lands on it.
            if (lob.target() >= 0 && convoy.touches(lob.target(), x, y, EnemyGun.BULLET)) {
                objectiveDamage += gun.damage();
                // M7 part D: on a ground ally, one hit (the ally's spec says whether lobs hurt it).
                if (convoy.spec(lob.target()).lobs()) {
                    hitAlly(lob.target(), 1);
                }
            }
            events.add(SimEvents.Type.MORTAR_IMPACT, x, y, hit ? 1 : 0);
            lobs.free(i);
        }
        return direct;
    }

    Pool<Lob> lobs() {
        return lobs;
    }

    /** M5 part G: the direct hits the lobs aimed at the objective dealt it in the last {@link #moveLobs}. */
    double objectiveLobDamage() {
        return objectiveDamage;
    }

    /** A destroyed unit with a death burst pops into its puff of bullets. */
    void deathBurst(Enemy enemy, Ship ship) {
        EnemySpec.DeathBurst puff = enemy.spec().deathBurst().orElseThrow();
        burst(enemy.x(), enemy.y(), puff.count(), puff.speed(), puff.damage(), ship);
    }

    /**
     * A ring of {@code count} bullets from (x, y), within the bullet budget; none when the ship is
     * closer than the bullets may spawn (design/enemies, bullet readability rules).
     */
    void burst(double x, double y, int count, double speed, double damage, Ship ship) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE) {
            return;
        }
        for (int k = 0; k < count && bullets.size() < rules.bulletBudget(); k++) {
            double angle = 2 * StrictMath.PI * k / count;
            bullets.obtain().fire(x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, damage);
        }
    }

    Pool<Mine> mines() {
        return mines;
    }

    void moveBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            bullet.move();
            if (!PlayField.overlaps(bullet.x(), bullet.y(), EnemyGun.BULLET)) {
                bullets.free(i);
            }
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
            } else if (entry == WaveSpec.Entry.REAR) {
                edges |= WarningEdge.BOTTOM.bit();
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
        return bullets;
    }

    /** The distinct enemies of the level; {@link Enemy#kind()} indexes this list. */
    List<EnemySpec> kinds() {
        return kinds;
    }

    /** The units of the enemy {@code slug} the level sends, flying and on the ground. */
    int unitsOf(String slug) {
        int count = waves.unitsOf(slug);
        for (LevelScript.GroundUnit unit : groundUnits) {
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
