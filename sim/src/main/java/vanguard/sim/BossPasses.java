package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * The set pieces' passes in a {@link Sortie}'s step and what the bosses do to the level (it is their
 * {@link SetPiece.BossActions}): their flight, arrival and escape, their guns, phases, streams, windows'
 * launches and ambushes (with the bottom edge's warnings ahead of the re-entries), the arena boss's
 * telegraphs, slams, roots and release.
 */
final class BossPasses implements SetPiece.BossActions {
    /** M5 part H: the bottom edge's warnings ahead of the launched units' re-entries, a ring of real steps [start, end). */
    static final int WARNINGS = 8;

    private final Sortie s;
    private final LevelScript script;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Objectives objectives;
    private final Rules rules;
    private final SetPiece[] setPieces;
    /** M5 part A: the wingman in the escort slot (Rook); null without one. */
    private final Wingman wingman;
    /** The escort objective's convoy, or (M5 part E) the naval convoy; null without one. */
    private final Convoy convoy;

    private final HitPasses hitPasses;
    private final AllyPasses allies;
    /** The boss streams' units, planned once: per stream, from the left and from the right. */
    private final BossSpec.Stream[] streams;

    private final Spawn[] streamFromLeft;
    private final Spawn[] streamFromRight;
    /** M5 part E: the arena boss (anchored, with lanes); null without one. */
    private final SetPiece arenaBoss;
    /** M5 part E: a lane's box (every lane has the same), centred on a lane's middle; null without lanes. */
    private final Hitbox laneBox;
    /** Part G: the set piece of a parts objective (Level 07's carrier); -1 without one. */
    private final int partsPiece;
    /** M5 part E: the units the arena boss's release lets in, planned when the sortie is made; empty without one. */
    private final Spawn[] arenaRelease;
    /** M5 part H: the arena boss's ambush launches planned once; null without them. */
    private final Launches launches;
    /** M5 part H: the ambush launches' openings in this attempt (they take the planned sets in turn). */
    private int ambushOpenings;

    private final int[] warningStarts;
    private final int[] warningEnds;
    private int warningNext;
    /** The phase a boss entered in this step, for its event; -1 for none. */
    int bossPhase = -1;

    BossPasses(
            Sortie s,
            LevelScript script,
            long seed,
            BossSpec.Stream[] streams,
            SetPiece arenaBoss,
            Hitbox laneBox,
            int partsPiece,
            Ship ship,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Objectives objectives,
            Rules rules,
            SetPiece[] setPieces,
            Wingman wingman,
            Convoy convoy,
            HitPasses hitPasses,
            AllyPasses allies) {
        this.s = s;
        this.script = script;
        this.streams = streams;
        this.arenaBoss = arenaBoss;
        this.laneBox = laneBox;
        this.partsPiece = partsPiece;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.objectives = objectives;
        this.rules = rules;
        this.setPieces = setPieces;
        this.wingman = wingman;
        this.convoy = convoy;
        this.hitPasses = hitPasses;
        this.allies = allies;
        arenaRelease = arenaBoss == null ? new Spawn[0] : planRelease(arenaBoss, seed);
        streamFromLeft = new Spawn[streams.length];
        streamFromRight = new Spawn[streams.length];
        for (int i = 0; i < streams.length; i++) {
            int kind = force.kinds().indexOf(streams[i].enemy());
            streamFromLeft[i] = Formations.streamUnit(streams[i].enemy(), kind, true);
            streamFromRight[i] = Formations.streamUnit(streams[i].enemy(), kind, false);
        }
        launches = arenaBoss == null ? null : Launches.plan(arenaBoss, force.kinds(), seed);
        warningStarts = new int[launches == null ? 0 : WARNINGS];
        warningEnds = new int[warningStarts.length];
    }

    /** M5 part H: only in a level with ambush launches, so the other levels hash as before. */
    void addTo(StateHash hash) {
        if (launches != null) {
            hash.add(ambushOpenings).add(warningNext);
            for (int w = 0; w < warningStarts.length; w++) {
                hash.add(warningStarts[w]).add(warningEnds[w]);
            }
        }
    }

    /**
     * M5 part H (design/enemies/bosses/siege-spire, the maw's Wraiths; decision H3 = a): unit {@code
     * unit} of {@code units} of an ambush launch from window part {@code part} at (x, y): it flies its
     * planned rear path unless the boss's launched units alive reach the spawn's most already (then it is
     * left out); it counts among the level's enemies, and the bottom edge is warned ahead of its re-entry.
     */
    private void launchAmbush(BossSpec.Spawn spawn, int part, int unit, int units, double x, double y) {
        if (unit == 0) {
            ambushOpenings++;
        }
        if (launches == null || force.launchedAlive() >= spawn.ambushMost()) {
            return;
        }
        Spawn plan = launches.plan(spawn, part, ambushOpenings - 1, unit, units);
        if (plan == null || !force.launchAmbush(plan)) {
            return;
        }
        s.streamReleased++;
        events.add(SimEvents.Type.BOSS_LAUNCHED, x, y, plan.kind());
        int reentry = s.realTick + launches.reentry(spawn, part, ambushOpenings - 1, unit, units);
        warningStarts[warningNext] = Math.max(s.realTick, reentry - SimStep.ticks(WaveSchedule.EDGE_WARNING_SECONDS));
        warningEnds[warningNext] = reentry;
        warningNext = (warningNext + 1) % warningStarts.length;
    }

    /** M5 part H: the bottom edge's bit while a launched unit's re-entry warning runs, else 0. */
    int launchWarnings() {
        for (int w = 0; w < warningStarts.length; w++) {
            if (s.realTick >= warningStarts[w] && s.realTick < warningEnds[w]) {
                return WarningEdge.BOTTOM.bit();
            }
        }
        return 0;
    }

    /** M7 part F: the walking boss's next entry edge's warning bit while it is off the screen, else 0. */
    int strideWarnings() {
        int edges = 0;
        for (SetPiece piece : setPieces) {
            if (piece.stride().isPresent() && piece.present() && !piece.destroyed()) {
                edges |= piece.stride().get().warning();
            }
            if (piece.breach().isPresent() && piece.present() && !piece.destroyed()) {
                // M7 part H: a breaching boss's next side or rear point.
                edges |= piece.breach().get().warning();
            }
        }
        return edges;
    }

    /** M7 part F: the bottom edge's bit while a walking boss walks up the screen (a rear wave for Rook), else 0. */
    int strideRear() {
        int edges = 0;
        for (SetPiece piece : setPieces) {
            if (piece.stride().isPresent() && piece.present() && !piece.destroyed()) {
                edges |= piece.stride().get().rear();
            }
            if (piece.breach().isPresent() && piece.present() && !piece.destroyed()) {
                // M7 part H: a rear breach (design/enemies/bosses/dust-colossus: a rear wave for Rook).
                edges |= piece.breach().get().rear();
            }
        }
        return edges;
    }

    /** M5 part H: no launched unit's warning runs (a new attempt, a retry from the boss on an empty field). */
    void clearLaunchWarnings() {
        Arrays.fill(warningStarts, 0);
        Arrays.fill(warningEnds, 0);
        warningNext = 0;
        ambushOpenings = 0;
    }

    /** Salt for the arena release's own generator, so its planning leaves the sortie's sequence alone. */
    private static final long RELEASE_SALT = 0x4B52414B454EL;

    /** The arena release's units lie in rows this far apart down their lane, from this far below its top, px. */
    private static final double RELEASE_ROW = 70;

    private static final double RELEASE_TOP = 50;

    /** ... jittered this far sideways and down, px. */
    private static final double RELEASE_JITTER_X = 30;

    private static final double RELEASE_JITTER_Y = 12;

    /**
     * M5 part E: the arena boss's release planned once (its first): its units placed over its lanes in
     * turn, in rows down from near each lane's top, jittered by a generator of its own; the share of a
     * submerging enemy's start begins under the water.
     */
    private Spawn[] planRelease(SetPiece piece, long seed) {
        BossSpec boss = piece.boss().orElseThrow();
        SlamArena arena = piece.arena().orElseThrow();
        BossSpec.Release release = null;
        for (BossSpec.Phase phase : boss.phases()) {
            if (release == null && phase.arena().release().isPresent()) {
                release = phase.arena().release().get();
            }
        }
        if (release == null) {
            return new Spawn[0];
        }
        SplitMix64 luck = new SplitMix64(seed ^ RELEASE_SALT);
        EnemySpec enemy = release.enemy();
        int kind = force.kinds().indexOf(enemy);
        int submerged = enemy.submerge().isPresent()
                ? (int) Math.round(enemy.submerge().get().start() * release.count())
                : 0;
        int[] rows = new int[arena.laneCount() + 1];
        Spawn[] units = new Spawn[release.count()];
        for (int i = 0; i < units.length; i++) {
            int lane = release.lanes().get(i % release.lanes().size());
            double x = arena.laneWidth() * (lane - 0.5) + luck.range(-RELEASE_JITTER_X, RELEASE_JITTER_X);
            double y = arena.laneTop()
                    - RELEASE_TOP
                    - RELEASE_ROW * rows[lane]++
                    + luck.range(-RELEASE_JITTER_Y, RELEASE_JITTER_Y);
            units[i] = new Spawn(
                    0,
                    kind,
                    enemy,
                    FlightPath.through(x, y, x, -PlayField.HEIGHT),
                    enemy.speed(),
                    0,
                    java.util.Optional.empty(),
                    Spawn.Exit.DOWN,
                    false,
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.of(new Spawn.Field(x, y, 0, -enemy.speed(), i < submerged, luck.nextLong())));
        }
        return units;
    }

    /**
     * M5 part E: a slam strikes lane {@code lane}: the ship and Rook take its damage when their hull
     * overlaps the lane (once per slam; not while the ship cannot be hurt), the convoy ships in it a
     * hit, and the impact's event.
     */
    private void slamLane(int lane) {
        SlamArena arena = arenaBoss.arena().orElseThrow();
        double laneX = arena.laneWidth() * (lane - 0.5);
        double laneY = arena.laneTop() / 2;
        events.add(SimEvents.Type.SLAM, laneX, laneY, lane);
        double damage =
                arenaBoss.boss().orElseThrow().arena().orElseThrow().slam().damage();
        if (s.flying() && !s.complete && !s.failed && !rules.invulnerableShip()) {
            if (!ship.evading() && ship.spec().hull().overlaps(ship.x(), ship.y(), laneBox, laneX, laneY)) {
                double lost = ship.defences().armourLost();
                hitPasses.damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()));
            }
            if (wingman != null
                    && !wingman.ejected()
                    && wingman.hull().overlaps(wingman.x(), wingman.y(), laneBox, laneX, laneY)) {
                wingman.hit(damage, ship.x(), ship.y(), events);
            }
        }
        allies.slamAllies(lane);
    }

    /** Round 40: a {@link SimEvents.Type#ROOT_HIT} for each rooted arm's root hit this step. */
    void rootHits() {
        SlamArena arena = arenaBoss.arena().orElseThrow();
        for (int a = 0; a < arena.armCount(); a++) {
            int p = arena.armPart(a);
            if (arenaBoss.partTicksSinceHit(p) == 0) {
                events.add(SimEvents.Type.ROOT_HIT, arenaBoss.partX(p), arenaBoss.partY(p), p);
            }
        }
    }

    /**
     * The set pieces fly their passes: one reaching the player's layer is announced, one ending its
     * last pass alive has escaped (its line, and an escapes objective about it fails).
     */
    void flySetPieces() {
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            boolean onPlane = piece.onPlane();
            if (piece.boss().isPresent()) {
                if (piece.arena().isPresent()) {
                    piece.ground(s.groundScroll, s.scrollHalted());
                }
                if (piece.breach().isPresent()) {
                    // M7 part H: its next point may follow the ship.
                    piece.breachTargets(ship.x(), ship.y());
                }
                if (piece.stride().isPresent()) {
                    // M7 part F: its passes' lanes and its cone read where the ship and Rook are.
                    boolean rook = wingman != null && !wingman.ejected();
                    piece.strideTargets(ship.x(), ship.y(), rook ? wingman.x() : 0, rook ? wingman.y() : 0, rook);
                }
                piece.update(s.levelTick);
                if (piece.stride().isPresent() && piece.stride().get().footfall() && !piece.destroyed()) {
                    events.add(SimEvents.Type.WALKER_STEP, piece.x(), piece.y(), k);
                }
                BossSpec boss = piece.boss().get();
                if (boss.incomingSeconds() > 0 && s.clockTicked && s.levelTick == incomingTick(boss)) {
                    // Round 41: the warning ahead of the arrival (the level clock moves at most a step a step).
                    events.add(SimEvents.Type.BOSS_INCOMING, piece.x(), piece.y(), k);
                }
                if (piece.bossTicks() == 0) {
                    events.add(SimEvents.Type.BOSS_ARRIVED, piece.x(), piece.y(), k);
                } else if (piece.swayTicks() == 0) {
                    events.add(SimEvents.Type.BOSS_SETTLED, piece.x(), piece.y(), k);
                }
                if (piece.liftEndedNow()) {
                    // M5 part H: the lift is over: it holds at its new station and engages.
                    events.add(SimEvents.Type.SPIRE_LIFTED, piece.x(), piece.y(), piece.phase());
                }
                continue;
            }
            if (piece.update(s.levelTick)) {
                events.add(SimEvents.Type.SET_PIECE_ESCAPED, piece.x(), piece.y(), k);
                if (objectives.escapeLost(piece.slug(), "")) {
                    events.add(SimEvents.Type.OBJECTIVE_FAILED, piece.x(), piece.y());
                }
                radio.cue(CueTrigger.ENEMY_ESCAPED, piece.slug());
            } else if (!onPlane && piece.onPlane()) {
                events.add(SimEvents.Type.SET_PIECE_DESCENDED, piece.x(), piece.y(), k);
            }
        }
    }

    /**
     * Round 41: the level clock's step of {@code boss}'s {@link SimEvents.Type#BOSS_INCOMING}: its {@link
     * BossSpec#incomingSeconds()} before its arrival, at the first step at the earliest.
     */
    static int incomingTick(BossSpec boss) {
        return Math.max(1, SimStep.ticks(boss.arriveSeconds()) - SimStep.ticks(boss.incomingSeconds()));
    }

    /** The set pieces' living parts fire their guns while on the player's layer. */
    void fireSetPieces(boolean firing) {
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (piece.boss().isPresent()) {
                bossPhase = -1;
                piece.act(ship.x(), ship.y(), this, firing);
                if (bossPhase > 0) {
                    phaseEntered(k, piece);
                }
                continue;
            }
            List<PartSpec> parts = piece.spec().parts();
            for (int p = 0; p < parts.size(); p++) {
                if (piece.trigger(p) && firing) {
                    force.fireFrom(
                            piece.partX(p), piece.partY(p), parts.get(p).gun().orElseThrow(), ship);
                }
            }
        }
    }

    /**
     * Boss {@code k} entered phase {@link #bossPhase}: its event and line, the timeout line first
     * when the phase before ran out of time with parts it waited for alive (Level 07's "Forget the
     * sacs"), and a parts objective whose phase is over fails with one of its parts alive.
     */
    private void phaseEntered(int k, SetPiece piece) {
        BossSpec boss = piece.boss().orElseThrow();
        BossSpec.Phase entered = boss.phases().get(bossPhase);
        String name = entered.name();
        events.add(SimEvents.Type.BOSS_PHASE, piece.x(), piece.y(), bossPhase);
        if (piece.arena().isPresent()
                && entered.windows().isPresent()
                && launchesAmbushes(entered.windows().get())) {
            // M5 part H: the Siege Spire's claw crown opens: its maw is exposed and launches.
            List<List<Integer>> groups = entered.windows().get().groups();
            for (int g = 0; g < groups.size(); g++) {
                for (int i = 0; i < groups.get(g).size(); i++) {
                    int p = groups.get(g).get(i);
                    events.add(SimEvents.Type.SPIRE_MAW_OPENED, piece.partX(p), piece.partY(p), p);
                }
            }
        }
        if (piece.lifting()) {
            // M5 part H: the tear-free: the shockwave at the anchor, the stump left behind.
            events.add(SimEvents.Type.SPIRE_LIFT, piece.stumpX(), piece.stumpY(), bossPhase);
        }
        if (timedOut(piece, boss, bossPhase - 1)) {
            radio.cue(CueTrigger.BOSS_TIMEOUT, name);
        }
        radio.cue(CueTrigger.BOSS_PHASE, name);
        if (k == partsPiece
                && bossPhase > script.objectives().secondary().beforePhase()
                && objectives.partsSurvived()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, piece.x(), piece.y());
        }
    }

    /** M5 part H: whether {@code windows} launch ambushes (the Siege Spire's maw). */
    private static boolean launchesAmbushes(BossSpec.Windows windows) {
        List<BossSpec.Spawn> spawns = windows.spawns();
        for (int i = 0; i < spawns.size(); i++) {
            if (spawns.get(i).ambush()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether phase {@code phase} timed out: it ended on its timer while it also waited for parts
     * (Level 07's broadside phase with sacs alive). A purely timed phase (the overhead pass) never
     * times out in this sense.
     */
    private static boolean timedOut(SetPiece piece, BossSpec boss, int phase) {
        return piece.endedOnTimeout(phase)
                && !boss.phases().get(phase).untilParts().isEmpty();
    }

    @Override
    public void aimed(double x, double y, EnemyGun gun) {
        if (gun.homing().isPresent()) {
            // M7 part F: a homing gun launches a missile (the Revenant Walker's pods).
            force.missiles().launch(x, y, gun, ship.x(), ship.y());
            return;
        }
        force.fireFrom(x, y, gun, ship);
    }

    @Override
    public void along(double x, double y, double aim, EnemyGun gun) {
        force.fireAlong(x, y, gun, aim, ship);
    }

    @Override
    public void hatch(int part, double x, double y) {
        events.add(SimEvents.Type.POD_HATCH, x, y, part);
    }

    @Override
    public void coneTell(double x, double y) {
        events.add(SimEvents.Type.CONE_TELL, x, y);
        // M7 part F (step F4): the walker's first flame tell is its fight's first telegraph (Varga's line).
        radio.cue(CueTrigger.FIRST_TELEGRAPH, "");
    }

    @Override
    public void coneBurn(double x, double y) {
        events.add(SimEvents.Type.CONE_BURN, x, y);
    }

    @Override
    public void lanceCharge(double x, double y) {
        events.add(SimEvents.Type.LANCE_CHARGE, x, y);
    }

    @Override
    public void lanceFire(double x, double y) {
        events.add(SimEvents.Type.LANCE_FIRE, x, y);
    }

    @Override
    public void stompTell(double x, double y) {
        events.add(SimEvents.Type.STOMP_TELL, x, y);
    }

    @Override
    public void stomp(double x, double y) {
        events.add(SimEvents.Type.STOMP, x, y);
    }

    @Override
    public void breach(SimEvents.Type type, double x, double y, int value) {
        events.add(type, x, y, value);
    }

    /**
     * M7 part H: the bite snaps at (x, y): the ship (unless it cannot be hurt) and Rook with their centres within
     * {@code radius} take {@code damage} (the Kraken slam's impact rule: once, at the snap).
     */
    @Override
    public int bite(double x, double y, double radius, double damage) {
        int hit = 0;
        if (s.flying() && !s.complete && !s.failed && !rules.invulnerableShip()) {
            if (!ship.evading() && within(ship.x(), ship.y(), x, y, radius)) {
                hit |= BreachArena.BITE_SHIP;
                double lost = ship.defences().armourLost();
                hitPasses.damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()));
            }
            if (wingman != null && !wingman.ejected() && within(wingman.x(), wingman.y(), x, y, radius)) {
                hit |= BreachArena.BITE_WINGMAN;
                wingman.hit(damage, ship.x(), ship.y(), events);
            }
        }
        return hit;
    }

    /** M7 part H, step H3: a breaching boss tears a rock loose toward its mouth (the simulation's random numbers). */
    @Override
    public void tearLoose(PulledDebris rocks, double mouthX, double mouthY) {
        rocks.tear(s.debris, s.rng, mouthX, mouthY);
    }

    private static boolean within(double px, double py, double x, double y, double radius) {
        double dx = px - x;
        double dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void down(double x, double y, EnemyGun gun) {
        force.fireDown(x, y, gun, ship);
    }

    @Override
    public void ring(double x, double y, int count, double speed, double damage) {
        force.burst(x, y, count, speed, damage, ship);
        events.add(SimEvents.Type.ENEMY_FIRED, x, y, (int) damage);
    }

    @Override
    public void bullet(double x, double y, double angle, double speed, double damage) {
        force.fireAngle(x, y, angle, speed, damage, ship);
    }

    @Override
    public void release(BossSpec.Stream stream, boolean left) {
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] == stream) {
                force.release(left ? streamFromLeft[i] : streamFromRight[i]);
                s.streamReleased++;
                return;
            }
        }
    }

    @Override
    public void phase(int phase) {
        bossPhase = phase;
    }

    @Override
    public void launch(BossSpec.Spawn spawn, double x, double y, double angle) {
        // A window's unit counts among the level's enemies, as a stream's does.
        force.launch(spawn.enemy(), x, y, angle, spawn.speed(), spawn.glideSeconds());
        s.streamReleased++;
        events.add(SimEvents.Type.BOSS_LAUNCHED, x, y, force.kinds().indexOf(spawn.enemy()));
    }

    @Override
    public void telegraph(int lane) {
        SlamArena arena = arenaBoss.arena().orElseThrow();
        events.add(SimEvents.Type.TELEGRAPH, arena.laneWidth() * (lane - 0.5), arena.laneTop(), lane);
        radio.cue(CueTrigger.FIRST_TELEGRAPH, "");
        if ((shipLanes() & (1 << lane)) != 0) {
            // Round 40: the first telegraph on a convoy ship's lane (the root hint).
            radio.cue(CueTrigger.FIRST_SHIP_TELEGRAPH, "");
        }
    }

    @Override
    public void slam(int lane) {
        slamLane(lane);
    }

    @Override
    public void surface(int part, boolean up) {
        events.add(
                up ? SimEvents.Type.SURFACE : SimEvents.Type.DIVE, arenaBoss.partX(part), arenaBoss.partY(part), part);
    }

    @Override
    public void release(BossSpec.Release release) {
        // The arena's field: its units count among the level's enemies as they come, as a stream's.
        for (Spawn unit : arenaRelease) {
            force.release(unit);
        }
        s.streamReleased += arenaRelease.length;
    }

    @Override
    public void drop(PickupType pickup, double x, double y) {
        double half = EnemyGun.BULLET.width();
        s.pickupPasses.drop(
                pickup, Math.clamp(x, half, PlayField.WIDTH - half), Math.clamp(y, half, PlayField.HEIGHT - half));
    }

    @Override
    public int shipLanes() {
        if (convoy == null || !convoy.naval()) {
            return 0;
        }
        int mask = 0;
        for (int k = 0; k < convoy.size(); k++) {
            if (convoy.slammable(k) && convoy.lane(k) > 0) {
                mask |= 1 << convoy.lane(k);
            }
        }
        return mask;
    }

    @Override
    public void lob(double x, double y, EnemyGun gun) {
        force.bossLob(x, y, gun, ship);
    }

    @Override
    public void ambush(BossSpec.Spawn spawn, int part, int unit, int units, double x, double y) {
        launchAmbush(spawn, part, unit, units, x, y);
    }

    @Override
    public void sweep(int a, boolean telegraph, double x, double y) {
        events.add(telegraph ? SimEvents.Type.SPIRE_SWEEP_TELEGRAPH : SimEvents.Type.SPIRE_SWEEP, x, y, a);
    }

    @Override
    public void aimTell(int part, double x, double y) {
        events.add(SimEvents.Type.AIM_TELL, x, y, part);
    }
}
