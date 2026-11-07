package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * The entries of an {@link EnemyForce} (out of the force): the units let in by the {@link WaveSchedule} (M9 part B: and
 * the halt cycle's), the ground units at their time (from a side, a root burst, a gate's anchor, a holder, a dormant
 * unit), the units let in outside the waves (a boss's streams, launches and ambushes); the serials, the schedule's
 * place for a checkpoint, the side edges' warnings and the level's unit counts. Its arrays are allocated once.
 */
final class EnemyEntries {
    /** M9 part B: the wave numbers of the halt cycles start past the plan's (cycle c's at (c + 1) × this). */
    private static final int HALT_WAVES = 1 << 16;

    private final EnemyForce force;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final List<EnemySpec> kinds;
    private final WaveSchedule waves;
    private final ObjectiveHook objectiveHook;
    private final RootBursts rootBursts;
    private final ChainRules chainRules;
    private final EnemyFlocks flocks;
    private final EnemyCarriers carriers;
    private final List<GroundUnit> groundUnits;
    private final int[] groundKinds;
    private final int[] groundTicks;
    /** M5 part E: each ground unit's current as a direction (x, y up), for a drifting raft. */
    private final double[] groundCurrentX;

    private final double[] groundCurrentY;
    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): per ground unit, whether it is flipped ({@link
     * TravelFrame}): one entering at the top edge with the scroll while it runs reversed comes up from the bottom.
     */
    private final boolean[] groundFlips;
    /** M5 part G: per ground unit entering from a side, the steps its edge warning starts and ends, and its edge bit. */
    private final int[] sideWarningStarts;

    private final int[] sideWarningEnds;
    private final int[] sideWarningEdges;
    /**
     * M9 part B: the halt cycle's plan (design/campaign Level 29); null without one. M10 part G, step G1a: per held gate
     * (one entry, the level's one cycle, serving every gate).
     */
    private final WaveSchedule[] haltWaves;
    /** M9 part B: the halt cycle's length, steps; 0 without one. M10 part G: per gate, as {@link #haltWaves}. */
    private final int[] haltEveryTicks;
    /** M9 part B: whether a ground unit enters dormant (the wake pass runs only then). */
    private final boolean dormants;

    private int spawned;
    private int nextGround;
    /** M9 part B: the halt cycle's units let in so far in this attempt (they count among the level's enemies). */
    private int haltReleased;
    /** M9 part B: the holders left out for want of room in the pool in this attempt (never seen). */
    private int holdersLeftOut;
    /** M7 part B: the level's no-surface zones, which the sortie scrolls; none by default. */
    private SurfaceZones zones = SurfaceZones.NONE;
    /** M7 part C: the level's tendril gates; null without. */
    private Links links;
    /** M7 part C: per ground unit, its gate × 2 (+ 1 the right anchor); -1 for a unit that is no gate's anchor. */
    private int[] gateOf;
    /** M9 part B: the rocks of the waves on rocks and their riders ({@link RockRides}); null without such waves. */
    private RockRides rides;
    /** M9 part E: the level's forks ({@link Forks}); null without. */
    private Forks forks;
    /**
     * M11 part C, step CD1 ({@link Pits}): each pit wave's own plan, let in on its pit's clock; null in a level without
     * pit waves.
     */
    private WaveSchedule[] pitPlans;
    /** M11 part C: per pit wave, its index in the level's waves (its units' wave number). */
    private int[] pitWaveIndexes;
    /** M11 part C: per wave of the level, its pit wave's index (-1 for a wave of no pit): the main plan skips them. */
    private int[] pitWaveOf;
    /** M11 part C: the pit waves' units never let in so far in this attempt (their pit left the screen held). */
    private int pitLost;
    /** M11 part F, step F1: the level's rear waves in the lane away from the ship; null without such a wave. */
    private RearLanes rearLanes;
    /** M11 part C: the pit wave being let in now ({@link #spawnPit}): its shift to the pit and its rise, steps. */
    private boolean pitting;

    private double pitDx;
    private double pitDy;
    private int pitRiseTicks;

    /**
     * @param reversedAt M7 part G: whether the scroll runs reversed at a level time, s: the ground units entering then
     *     are flipped
     * @param haltEveryTicks M9 part B: the halt cycle's length, steps; 0 without one ({@code haltWaves} null); M10 part
     *     G: per gate (one entry: the level's one cycle at every gate)
     */
    EnemyEntries(
            EnemyForce force,
            Pool<Enemy> enemies,
            SimEvents events,
            List<EnemySpec> kinds,
            WaveSchedule waves,
            WaveSchedule[] haltWaves,
            int[] haltEveryTicks,
            List<GroundUnit> groundUnits,
            java.util.function.DoublePredicate reversedAt,
            ObjectiveHook objectiveHook,
            RootBursts rootBursts,
            ChainRules chainRules,
            EnemyFlocks flocks,
            EnemyCarriers carriers) {
        this.force = force;
        this.enemies = enemies;
        this.events = events;
        this.kinds = kinds;
        this.waves = waves;
        this.haltWaves = haltWaves;
        this.haltEveryTicks = haltEveryTicks;
        this.objectiveHook = objectiveHook;
        this.rootBursts = rootBursts;
        this.chainRules = chainRules;
        this.flocks = flocks;
        this.carriers = carriers;
        // M9 part E: a fork branch's ground units sleep until (unless) their side is chosen.
        dormants = groundUnits.stream()
                .anyMatch(unit -> unit.dormant() || unit.branch().isPresent());
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

    /** Back to the level start: the schedules at their first unit, nothing let in. */
    void reset() {
        waves.reset();
        for (WaveSchedule halt : haltWaves) {
            if (halt != null) {
                halt.reset();
            }
        }
        haltReleased = 0;
        holdersLeftOut = 0;
        spawned = 0;
        nextGround = 0;
        pitLost = 0;
        if (pitPlans != null) {
            for (WaveSchedule plan : pitPlans) {
                plan.reset();
            }
        }
        if (rearLanes != null) {
            rearLanes.reset();
        }
    }

    /**
     * M11 part C, step CD1: the level's pit waves' plans ({@code plans}, in the waves' order), their indexes among the
     * waves, and per wave its pit wave (-1 for none): the main plan leaves their units to {@link #spawnPit}.
     */
    void pits(WaveSchedule[] plans, int[] waveIndexes, int[] waveOf) {
        pitPlans = plans;
        pitWaveIndexes = waveIndexes;
        pitWaveOf = waveOf;
    }

    /** M11 part F, step F1: the level's rear lanes ({@link RearLanes}): their units come up in the drawn lane. */
    void rearLanes(RearLanes lanes) {
        rearLanes = lanes;
    }

    /** M11 part C: the number of pit waves. */
    int pitWaves() {
        return pitPlans == null ? 0 : pitPlans.length;
    }

    /** M11 part C: pit wave {@code p}'s plan. */
    WaveSchedule pitPlan(int p) {
        return pitPlans[p];
    }

    /**
     * M11 part C, step CD1 ({@link Pits}): lets in pit wave {@code p}'s units due at {@code tick} on its own clock (its
     * planned time plus its hold), each moved by (dx, dy) px to its pit's point and rising out of it for {@code
     * riseTicks} steps.
     */
    void spawnPit(int p, int tick, double dx, double dy, int riseTicks) {
        pitting = true;
        pitDx = dx;
        pitDy = dy;
        pitRiseTicks = riseTicks;
        spawnDue(pitPlans[p], tick, pitWaveIndexes[p]);
        pitting = false;
        pitDx = 0;
        pitDy = 0;
        pitRiseTicks = 0;
    }

    /** M11 part C: pit wave {@code p}'s units not let in yet will never come (their pit left the screen held). */
    void pitLost(int p) {
        WaveSchedule plan = pitPlans[p];
        while (plan.due(Integer.MAX_VALUE) != null) {
            pitLost++;
        }
    }

    /** M11 part C: the pit waves' units never let in so far in this attempt (counted nowhere). */
    int pitLost() {
        return pitLost;
    }

    /** Back to a boss checkpoint (after {@link #reset}): the schedule where it was then. */
    void restore(int scheduleNext, int ground, int spawnedUnits) {
        waves.next(scheduleNext);
        nextGround = ground;
        spawned = spawnedUnits;
    }

    /** Lets in every unit due at {@code levelTick}, flying and on the ground. */
    void spawn(int levelTick) {
        spawnDue(waves, levelTick, 0);
        spawnGround(levelTick);
    }

    /** See {@link EnemyForce#spawnHalt}. */
    void spawnHalt(int gate, int haltTick, int cyclesBefore) {
        int g = haltWaves.length == 1 ? 0 : gate;
        WaveSchedule halt = haltWaves[g];
        if (halt == null) {
            return;
        }
        int cycle = cyclesBefore + haltTick / haltEveryTicks[g];
        int tick = haltTick % haltEveryTicks[g];
        if (tick == 0) {
            halt.reset();
        }
        int before = spawned;
        // Each cycle's waves are waves of their own (a leader's wing is its own cycle's).
        spawnDue(halt, tick, (cycle + 1) * HALT_WAVES);
        haltReleased += spawned - before;
    }

    /** See {@link EnemyForce#restoreHalt}. */
    void restoreHalt(int released) {
        haltReleased = released;
    }

    int holdersLeftOut() {
        return holdersLeftOut;
    }

    int haltReleased() {
        return haltReleased;
    }

    /** M9 part E: the level's forks: a branch's waves fly only when it is chosen, its ground units sleep till then. */
    void forks(Forks levelForks) {
        forks = levelForks;
    }

    /** M9 part B: the rocks' pass of the waves on rocks (the riders board at their entry). */
    void rides(RockRides rockRides) {
        rides = rockRides;
    }

    /** M9 part B: whether the level's waves carry rocks ({@link WaveSpec#onRocks()}). */
    int rocks() {
        return waves.rocks();
    }

    /** See {@link EnemyForce#wake}. */
    void wake(int levelTick) {
        if (!dormants) {
            return;
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.wake(levelTick)) {
                events.add(SimEvents.Type.WOKE, enemy.x(), enemy.y(), enemy.kind());
            }
        }
    }

    /** Lets in every unit of {@code schedule} due at {@code tick}; their wave numbers start at {@code waveBase}. */
    private void spawnDue(WaveSchedule schedule, int tick, int waveBase) {
        for (Spawn spawn = schedule.due(tick); spawn != null; spawn = schedule.due(tick)) {
            if (schedule == waves && pitWaveOf != null && pitWaveOf[schedule.lastWave()] >= 0) {
                // M11 part C, step CD1: a pit wave's unit comes out of its pit on the pit's clock (spawnPit).
                continue;
            }
            if (forks != null && schedule.lastBranch() >= 0 && !forks.chosen(schedule.lastBranch())) {
                // M9 part E: a branch not chosen (its fork's choice falls before its waves): it never comes.
                spawned++;
                continue;
            }
            if (spawn.enemy().chain().isPresent()) {
                chainRules.spawnChain(spawn);
                continue;
            }
            if (spawn.swarm().isPresent()) {
                flocks.enter(spawn, schedule, spawned, pitDx, pitDy, pitRiseTicks);
                spawned++;
                continue;
            }
            if (carriers.held(spawn, spawned, schedule.lastTag())) {
                // M8 part D: held unspawned until its rising carrier's layer flips (it peels off then).
                spawned++;
                continue;
            }
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                // M11 part F, step F1b: a rear lane wave's unit may fly its mirrored path ({@link RearLanes#plan}).
                enemy.spawn(
                        rearLanes != null && schedule == waves
                                ? rearLanes.plan(schedule.lastWave(), schedule.next() - 1, spawn)
                                : spawn,
                        spawned);
                if (pitting) {
                    // M11 part C, step CD1: out of its pit's mouth, rising.
                    enemy.shift(pitDx, pitDy);
                    enemy.pitRise = pitRiseTicks;
                }
                if (rearLanes != null && schedule == waves) {
                    // M11 part F, step F1: a rear wave in the lane away from the ship, drawn as its warning started.
                    double dx = rearLanes.shift(schedule.lastWave());
                    if (dx != 0) {
                        enemy.shift(dx, 0);
                    }
                }
                enemy.wave = waveBase + schedule.lastWave();
                if (schedule.lastLeader() != null) {
                    // M9 part B: its wave's first unit flies the leader's stat block.
                    EnemySpec lead = schedule.lastLeader();
                    enemy.lead(lead, kinds.indexOf(lead));
                }
                if (schedule.lastSeat() != null) {
                    // M9 part B: it boards its rock (the first of the rock places it).
                    rides.board(enemy, schedule.lastSeat());
                }
                WallMounts.Placed mounted = schedule.lastMounted();
                if (mounted != null) {
                    // M8 part E: a wall's nest stands on its foot; a cave's unit holds its hover only once out.
                    enemy.mounted(mounted);
                }
                if (schedule.lastFlipped()) {
                    // M7 part G: its wave entered while the scroll ran reversed.
                    enemy.flip();
                } else if (schedule.lastFrame() == TravelFrame.RISING) {
                    // M10 part F: a front formation rising from the bottom edge flies in the mirrored frame.
                    enemy.frame = TravelFrame.RISING;
                }
                enemy.entered(schedule.lastEntry());
                enemy.tag(schedule.lastTag());
                enemy.hunts = schedule.lastHunts();
                enemy.volley(schedule.lastVolleyGroup(), schedule.lastVolleyUnit());
                enemy.wavePlace(schedule.lastPlace());
                if (schedule.lastLineDelay() > 0) {
                    // M11 part A: a staggered line wave's unit telegraphs its first line in its turn.
                    enemy.lineBeam.stagger(schedule.lastLineDelay());
                }
                if (spawn.enemy().teleport().isPresent()) {
                    // M11 part A: a teleporting unit counts its rings and reads the current with the level's others.
                    enemy.teleporting.field(force.jumps());
                    if (spawn.enemy().teleport().get().cycle().isPresent()) {
                        // M11 part E, step E1: a cycling unit's rows and arms from its wave (a pair's shared draws).
                        WaveSpec wave = schedule.lastWaveSpec();
                        enemy.teleporting.wave(
                                enemy.wave,
                                schedule.lastPlace(),
                                wave != null && wave.rows().isPresent()
                                        ? wave.rows().get()
                                        : null,
                                wave == null ? 1 : wave.count());
                    }
                }
                if (HoverHold.stationed(schedule.lastWaveSpec())) {
                    // M11 part E, step E1 (design/campaign Level 47, the Herald's station): no sine at its fixed point.
                    enemy.hoverHold.station();
                }
                if (force.airLinks() != null && spawn.enemy().airLink().isPresent()) {
                    // M9 part D: it joins its wave's linked group (a circle wave's closes the ring).
                    force.airLinks()
                            .join(
                                    enemy,
                                    enemy.wave,
                                    schedule.lastPlace(),
                                    schedule.lastWaveSize(),
                                    spawn.orbit().isPresent());
                }
                if (enemy.coils()) {
                    // M8 part E: a coil keeps its orbit off the level's rock (the trench clamp).
                    enemy.coiling.canyon(force.canyon());
                }
                if (spawn.enemy().burrow().isPresent()) {
                    enemy.burrowGroup(schedule.lastBurrowGroup());
                    enemy.surfaceZones(zones);
                }
                if (spawn.escort().isPresent()) {
                    // M10 part B (design/campaign Level 36): a pair of drifting carriers' escort circles their
                    // midpoint.
                    enemy.escorting.circle(carriers.lastCarrier(), carriers.lastPartner());
                }
                if (objectiveHook.bombs(enemy, spawn)) {
                    // M7 part D: a front entry descends to the bomb row, a side entry keeps its entry row.
                    ObjectiveSpec.GroundAllies allies = objectiveHook.convoy().groundSpec();
                    double row = schedule.lastEntry() == WaveSpec.Entry.SIDES
                            ? enemy.y()
                            : PlayField.HEIGHT - allies.bombRow();
                    double slot = (schedule.lastPlace() - (schedule.lastWaveSize() - 1) / 2.0) * allies.bombSlot();
                    enemy.bombRun(row, slot, SimStep.ticks(allies.bombRunIn()));
                }
            }
            carriers.entered(spawn.enemy(), enemy);
            spawned++;
        }
        flocks.entriesOver();
    }

    /** Lets in every ground unit due at {@code levelTick}. */
    private void spawnGround(int levelTick) {
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
            if (enemy == null && unit.holder()) {
                // M9 part B: a holder that found no room never stands: it holds nothing.
                holdersLeftOut++;
            }
            if (enemy != null && unit.holder()) {
                // M9 part B: it holds the level's arena while it stands.
                enemy.holdsArena();
            }
            if (enemy != null && unit.dormant()) {
                // M9 part B: dark scenery until its time on the level clock.
                enemy.dormantUntil(SimStep.ticks(unit.dormantUntil()));
            }
            if (enemy != null && unit.branch().isPresent()) {
                // M9 part E: a fork branch's unit: dark scenery until its side is chosen, for good if it is not.
                enemy.branch = unit.branch().get().code();
                if (forks == null || forks.dormantOnEntry(enemy.branch)) {
                    enemy.dormantUntil(Integer.MAX_VALUE);
                }
            }
            spawned++;
            nextGround++;
        }
        flocks.entriesOver();
    }

    /** M7 part B: the level's no-surface zones (the sortie's); the Burrowers entering from now keep out of them. */
    void surfaceZones(SurfaceZones surfaceZones) {
        zones = surfaceZones;
    }

    /** The serial of the next unit let in (the units let in so far), counting it. */
    int nextSerial() {
        return spawned++;
    }

    /** See {@link EnemyForce#links}. */
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

    /** See {@link EnemyForce#launch}. */
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

    /** See {@link EnemyForce#launchAmbush}. */
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

    /** Lets in one unit planned outside the waves (a boss's stream), now. */
    void release(Spawn plan) {
        Enemy enemy = enemies.obtain();
        if (enemy != null) {
            enemy.spawn(plan, spawned);
        }
        spawned++;
    }

    int scheduleNext() {
        return waves.next();
    }

    int nextGround() {
        return nextGround;
    }

    int spawned() {
        return spawned;
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

    /** Step MARK70: whether a wave of neutral craft is announced at {@code levelTick}. */
    boolean friendlies(int levelTick) {
        return waves.friendlies(levelTick);
    }

    /** See {@link EnemyForce#unitsOf(String)}. */
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

    int unitsOf(String slug, String tag) {
        return waves.unitsOf(slug, tag);
    }

    /** See {@link EnemyForce#units()}. */
    /**
     * M9 part E: per fork branch ({@link Branch#code()}, {@code forks} × 2 of them), the units counted as enemies its
     * waves and ground units send.
     */
    int[] branchUnits(int forks) {
        int[] out = new int[2 * forks];
        for (int code = 0; code < out.length; code++) {
            out[code] = waves.branchUnits(code);
        }
        for (GroundUnit unit : groundUnits) {
            if (unit.branch().isPresent() && unit.enemy().countsAsEnemy()) {
                out[unit.branch().get().code()]++;
            }
        }
        return out;
    }

    int units() {
        int count = waves.counted() + groundUnits.size();
        for (EnemySpec kind : kinds) {
            if (kind.brood().isPresent()) {
                count += waves.unitsOf(kind.slug()) * kind.brood().get().count();
            }
        }
        return count;
    }

    /**
     * M11 part G, step G1: as {@link #units()}, of the waves and ground units due before the level clock's {@code tick}
     * (a deadline's kill ratio's).
     */
    int unitsBefore(int tick) {
        int count = waves.countedBefore(tick);
        for (GroundUnit unit : groundUnits) {
            count += SimStep.ticks(unit.t()) < tick ? 1 : 0;
        }
        for (EnemySpec kind : kinds) {
            if (kind.brood().isPresent()) {
                count += waves.unitsOfBefore(kind.slug(), tick)
                        * kind.brood().get().count();
            }
        }
        return count;
    }

    /** M9 part B: the halt cycle's length in steps of {@code halt}; 0 without one. */
    static int haltEveryTicks(Optional<HaltCycle> halt) {
        return halt.map(cycle -> Math.max(1, SimStep.ticks(cycle.everySeconds())))
                .orElse(0);
    }
}
