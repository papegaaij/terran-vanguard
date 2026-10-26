package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * A huge set-piece unit flying its passes (design/enemies/space/leviathan): its centre follows
 * each pass's path with the pass's fixed heading; its parts are hit boxes at fixed offsets around
 * it that take the hits, fire while it is on the player's layer and stay wrecked once destroyed.
 * Its position is a function of the level time, so a restart only resets the parts. One instance
 * per {@link SetPieceSpec}, made when the sortie is.
 *
 * <p>A boss ({@link BossSpec}, design/enemies/bosses) is a set piece without passes: it arrives on
 * the level clock, descends (taking no damage), settles and sways; its chains hang parts on necks
 * that bend toward the player, and its phases fire their attacks and streams through the
 * {@link BossActions} until its vital part is destroyed. Everything it needs is allocated here, so
 * stepping it does not allocate.
 */
public final class SetPiece implements Hashed {
    /** What a boss does to the rest of the level; the sortie implements it once. */
    interface BossActions {
        /** One aimed shot from (x, y) with {@code gun}'s speed and damage. */
        void aimed(double x, double y, EnemyGun gun);

        /**
         * Round 34, P2: one shot of {@code gun} from (x, y) straight down the screen (a part with {@code
         * aim: down}); by default a bullet at its speed and damage.
         */
        default void down(double x, double y, EnemyGun gun) {
            bullet(x, y, -StrictMath.PI / 2, gun.bulletSpeed(), gun.damage());
        }

        /** A ring of {@code count} bullets from (x, y). */
        void ring(double x, double y, int count, double speed, double damage);

        /** One bullet from (x, y) at {@code angle} radians (0 = right, y up). */
        void bullet(double x, double y, double angle, double speed, double damage);

        /** One unit of {@code stream} from the left or the right edge. */
        void release(BossSpec.Stream stream, boolean left);

        /** It entered phase {@code phase} (after the first). */
        void phase(int phase);

        /**
         * Phase {@code phase} ended on its timer (a timeout), just before the next one is entered
         * with {@link #phase(int)}.
         */
        default void timeout(int phase) {}

        /**
         * One unit of {@code spawn} leaves an open window part at (x, y), flying straight out at
         * {@code angle} radians (0 = right, y up) at the spawn's speed, gliding and holding as the
         * spawn says.
         */
        default void launch(BossSpec.Spawn spawn, double x, double y, double angle) {}

        /** M5 part E: lane {@code lane} (1-based) is telegraphed for a slam. */
        default void telegraph(int lane) {}

        /**
         * M5 part E: a slam strikes lane {@code lane} (1-based): the impact on the ship, the escort and
         * the convoy ships in it (the splash bullets come through {@link #bullet}).
         */
        default void slam(int lane) {}

        /** M5 part E: part {@code part} starts to surface ({@code up}) or to dive. */
        default void surface(int part, boolean up) {}

        /** M5 part E: the phase's release (the arena's jelly field) is let in, once. */
        default void release(BossSpec.Release release) {}

        /** M5 part E: a phase drops {@code pickup} at (x, y) as it begins (the shield cell). */
        default void drop(PickupType pickup, double x, double y) {}

        /**
         * M5 part E: the lanes holding a convoy ship a slam hurts, bit {@code n} for lane {@code n}
         * (the slams' "nearest ship" choice); 0 without.
         */
        default int shipLanes() {
            return 0;
        }

        /** M5 part H: a lob of {@code gun}'s mortar from (x, y) at the ship's position now (the boss's {@code lob}). */
        default void lob(double x, double y, EnemyGun gun) {}

        /**
         * M5 part H: unit {@code unit} of {@code units} of {@code spawn}, an ambush launch, leaves window
         * part {@code part} at (x, y) (the Siege Spire's maw's Wraiths).
         */
        default void ambush(BossSpec.Spawn spawn, int part, int unit, int units, double x, double y) {}

        /**
         * M5 part H: attack {@code a}'s sweep starts its telegraph ({@code telegraph}) or its beam, from
         * (x, y).
         */
        default void sweep(int a, boolean telegraph, double x, double y) {}

        /**
         * Round 40: part {@code part} starts its aim turn's glow (the tell before its aimed shot; the Siege
         * Spire's pods), at (x, y).
         */
        default void aimTell(int part, double x, double y) {}

        /**
         * M7 part F: one volley of {@code gun} from (x, y) along {@code aim} (radians clockwise from straight
         * down; a walking boss's chin burst along its heading, its parts' {@code aim: down}); by default a bullet
         * at its speed and damage.
         */
        default void along(double x, double y, double aim, EnemyGun gun) {
            bullet(x, y, StrictMath.atan2(-Trig.cos(aim), -Trig.sin(aim)), gun.bulletSpeed(), gun.damage());
        }

        /** M7 part F: part {@code part}'s hatch opens at (x, y), its homing salvo's tell ({@link EnemyGun.HomingSpec}). */
        default void hatch(int part, double x, double y) {}

        /** M7 part F: a walking boss's flame cone starts its tell at its flamer (x, y). */
        default void coneTell(double x, double y) {}

        /** M7 part F: a walking boss's flame cone starts to burn at its flamer (x, y). */
        default void coneBurn(double x, double y) {}

        /** Round 44: a walking boss's rail lance starts its charge at (x, y). */
        default void lanceCharge(double x, double y) {}

        /** Round 44: the rail lance fires from (x, y). */
        default void lanceFire(double x, double y) {}

        /** Round 44: a walking boss braces for a stomp at (x, y). */
        default void stompTell(double x, double y) {}

        /** Round 44: it stomps at (x, y). */
        default void stomp(double x, double y) {}

        /**
         * M7 part H: a breaching boss's cycle event {@code type} ({@link SimEvents.Type#BREACH_TELL}, {@code
         * BREACH_RISE}, {@code BITE_TELL}, {@code BITE_SNAP}, {@code BREACH_DIVE}) at (x, y) with its {@code value}.
         */
        default void breach(SimEvents.Type type, double x, double y, int value) {}

        /**
         * M7 part H: a breaching boss's jaws snap at (x, y): the ship and Rook within {@code radius} px of it take
         * {@code damage}; returns what it caught ({@link BreachArena#BITE_SHIP}, {@link BreachArena#BITE_WINGMAN} bits).
         */
        default int bite(double x, double y, double radius, double damage) {
            return 0;
        }

        /**
         * Step SIM-K: a breaching boss's eruption ring of {@code radius} px about (x, y) reaches the ship and Rook within
         * it, except those in {@code spared} (bits as {@link #bite}'s, hit before), who take {@code damage}; returns
         * whom it caught now.
         */
        default int strike(double x, double y, double radius, double damage, int spared) {
            return 0;
        }

        /**
         * M7 part H, step H3: a breaching boss tears a rock of {@code rocks} loose toward its mouth at (mouthX,
         * mouthY) ({@link PulledDebris#tear}).
         */
        default void tearLoose(PulledDebris rocks, double mouthX, double mouthY) {}

        /** M8 part H: a coiled boss's event {@code type} at (x, y) with its {@code value} ({@link CoilArena}). */
        default void coil(SimEvents.Type type, double x, double y, int value) {}

        /** M8 part H: a coiled boss dives (the radio's {@code boss-dive}). */
        default void dive() {}

        /**
         * M8 part H: a coiled boss's swinging bight or lunge ({@code what}: {@link CoilArena#SWEEP}, {@link
         * CoilArena#LUNGE}) reaches the ship and the escort where {@link CoilArena#reaches} says, except those in
         * {@code spared} ({@link CoilArena#STRUCK_SHIP}, {@link CoilArena#STRUCK_ESCORT} bits, hit before): they take
         * {@code damage}; returns whom it caught now.
         */
        default int coilStrike(CoilArena coil, int what, double damage, int spared) {
            return 0;
        }

        /**
         * M8 part H: a coiled boss sheds its Eel Swarm at shed point {@code point} ({@link CoilSpec.Shed}); returns the
         * chain's serial (-1 when none could be let in).
         */
        default int shed(int point) {
            return -1;
        }

        /** M8 part H: whether the shed chain with serial {@code serial} still swims. */
        default boolean shedAlive(int serial) {
            return false;
        }
    }

    /** How a boss moves now (design/enemies/bosses, the poses of part G). */
    public enum Motion {
        /** Not arrived yet, or destroyed. */
        NONE,
        /** Its entrance: in from above the top edge to its hover height. */
        PASS,
        /** At its station, swaying. */
        HOLD,
        /** A phase's move: gliding to its new station and sinking (or rising) to its new layer. */
        DESCEND,
        /** A phase's move: turning in place into its new pose. */
        TURN
    }

    private final SetPieceSpec spec;
    private final double extent;
    private final double[] partHp;
    private final int[] partTicksSinceHit;
    private final int[] volleyTicks;
    private final int[] burstLeft;
    private final int[] burstTicks;

    private int pass;
    private boolean present;
    private boolean destroyed;
    private boolean escaped;
    private boolean onPlane;
    private Layer layer;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double altitude;
    private double prevAltitude;
    private double cos;
    private double sin;
    private int hitCooldown;

    // A boss's state; the arrays are empty for a set piece flying passes.
    private final BossSpec boss;
    private final double maxHp;
    /** The parts out of the boss bar: the fire-only ones (M7 part H: and a breaching boss's spine vents). */
    private final boolean[] outOfBar;

    /** Its chains: the necks its parts hang on and the slam arms. */
    private final BossChains chains;

    private final int[] exposedFrom;
    private final int[] attackTicks;
    private final int[] attackBurstLeft;
    private final int[] attackBurstTicks;
    private final int[] attackPart;
    private final int[] attackNext;
    /** Per attack, a spiral's arm angle and its steps to the next bullet. */
    private final double[] spiralAngles;

    private final int[] spiralTicks;
    /** Per part, whether it is a fire-only part (never damaged, out of the bar). */
    private final boolean[] armoured;
    /** Per pose and part, its offset from the centre on the play plane. */
    private final double[][] poseDx;

    private final double[][] poseDy;
    private final Hitbox[] poseBody;
    /** Its phases' windows (which parts they hold, which are open, the windows' clock). */
    private final BossWindows windows;

    private final double entryY;
    /** Steps since it arrived; -1 before. */
    private int bossTicks;
    /** Steps since it settled; -1 before. */
    private int swayTicks;
    /** The step of {@link #swayTicks} its sway (re)started from, at its station. */
    private int swayStart;
    /** Whether its phases run (from its arrival or its settle). */
    private boolean engaged;
    /** Steps into the phase's opening move; -1 when none runs. */
    private int moveTicks;

    private double moveFromX;
    private double moveFromY;
    private double moveFromAltitude;
    private Layer moveFromLayer;
    /** Its station: where it sways. */
    private double anchorX;

    private double anchorY;
    /** Its pose (an index into its part layouts) and the one a move turns it from. */
    private int pose;

    private int fromPose;
    /** Steps since the phase engaged (after its move): its timeout's clock. */
    private int phaseTicks;
    /** Bit f set: phase f ended on its timer. */
    private long timeouts;

    private int phase;
    private int turn;
    private int turnTicks;
    private int streamTicks;
    private int streamLeft;
    private int streamUnitTicks;
    private int streamUnits;
    /** The steps from its arrival (the bar appearing) to its kill; -1 while it lives. */
    private int killTicks;

    /** M5 part E: an arena boss's lanes, slams and surfacing; null for any other. */
    private final SlamArena arena;
    /** {@link #arena} for the presentation, made once (stepping allocates nothing). */
    private final java.util.Optional<SlamArena> arenaView;
    /** M5 part E: where an arena boss's centre lies on the ground layer, px above the screen's bottom at the level start. */
    private double anchorGround;
    /** M5 part E: the ground's scroll this step and whether the scroll has halted in the arena. */
    private double groundScroll;
    /**
     * Round 41: how far the ground scrolled in this step ({@link #ground}), which a destroyed arena boss
     * moves down with; derived from the sortie's scroll every step, so not hashed.
     */
    private double groundStep;

    private boolean halted;
    /** M5 part E: an arena boss scrolling in with the ground before it arrives (no bar, no damage). */
    private boolean approaching;

    /**
     * M5 part H: whether an anchored boss has a lift (a phase with a move): its state is hashed only then, so Level
     * 11's Kraken hashes as before.
     */
    private final boolean lifts;
    /** Round 40: its attacks' aim turns (the Siege Spire's pods). */
    private final BossAimTurns aimTurns;
    /**
     * M5 part H, the lift (design/enemies/bosses/siege-spire; decision H4 = a): whether it has left its
     * anchor (from its lift's start), per part whether it stays at the anchor (the stump: the parts
     * destroyed or armoured when it lifted), the pose it lifted from, and the step of {@link #bossTicks}
     * its lift ended at (-1 before).
     */
    private boolean lifted;

    private final boolean[] stays;
    private int liftPose;
    private int liftEndTicks;
    /** M5 part H: its attacks' sweeps (design/enemies, Boss attacks and root bursts). */
    private final BossSweeps sweeps;
    /** M7 part F: a walking boss's passes, plate, vents and cone; null for any other. */
    private final StrideArena stride;
    /** {@link #stride} for the presentation, made once. */
    private final java.util.Optional<StrideArena> strideView;
    /** M7 part H: a breaching boss's breach cycle, ridge and suction; null for any other. */
    private final BreachArena breach;
    /** {@link #breach} for the presentation, made once. */
    private final java.util.Optional<BreachArena> breachView;
    /** M7 part F: a walking boss's hatches (its homing salvos' tell; round 44: its pods' armour). */
    private final BossHatches hatches;
    /** M8 part H: a coiled boss's body, wall and script; null for any other. */
    private final CoilArena coil;
    /** {@link #coil} for the presentation, made once. */
    private final java.util.Optional<CoilArena> coilView;

    /** M5 part H: a sweep's beam hit the ship ({@link #sweepHit}). */
    static final int SWEPT_SHIP = 1;
    /** M5 part H: a sweep's beam hit the wingman. */
    static final int SWEPT_WINGMAN = 2;

    SetPiece(SetPieceSpec spec) {
        this.spec = spec;
        extent = Math.max(spec.size().width(), spec.size().height()) / 2;
        int parts = spec.parts().size();
        partHp = new double[parts];
        partTicksSinceHit = new int[parts];
        volleyTicks = new int[parts];
        burstLeft = new int[parts];
        burstTicks = new int[parts];
        boss = spec.boss().orElse(null);
        armoured = new boolean[parts];
        if (boss != null) {
            for (int p : boss.armoured()) {
                armoured[p] = true;
            }
        }
        outOfBar = armoured.clone();
        if (boss != null && boss.coil().isPresent()) {
            // M8 part H: a coiled boss's gill sacs and implants are out of the bar (the implants count on the head's).
            CoilSpec coiled = boss.coil().get();
            for (int p : coiled.sacs()) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
            for (int p : coiled.implants().map(CoilSpec.Implants::parts).orElse(List.of())) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
        }
        if (boss != null && boss.vents().isPresent()) {
            // M7 part H, step H3: the spine vents are out of the bar.
            for (int p : boss.vents().get().parts()) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
        }
        double hp = 0;
        for (int p = 0; p < parts; p++) {
            hp += outOfBar[p] ? 0 : spec.parts().get(p).hp();
        }
        maxHp = hp;
        exposedFrom = new int[parts];
        int poses = boss == null || boss.poses().isEmpty() ? 1 : boss.poses().size();
        poseDx = new double[poses][parts];
        poseDy = new double[poses][parts];
        poseBody = new Hitbox[poses];
        for (int k = 0; k < poses; k++) {
            BossSpec.Pose layout =
                    boss == null || boss.poses().isEmpty() ? null : boss.poses().get(k);
            if (layout != null && layout.offsets().size() != parts) {
                throw new IllegalArgumentException(spec.slug() + ": pose " + layout.name() + " places every part");
            }
            poseBody[k] = layout == null ? spec.body() : layout.body();
            for (int p = 0; p < parts; p++) {
                poseDx[k][p] = layout == null
                        ? spec.parts().get(p).dx()
                        : layout.offsets().get(p).dx();
                poseDy[k][p] = layout == null
                        ? spec.parts().get(p).dy()
                        : layout.offsets().get(p).dy();
            }
        }
        chains = new BossChains(this, boss, spec.parts());
        // A boss arriving on high air is drawn (and its parts placed) at the high-air scale.
        double scale = boss != null && boss.layer() == Layer.HIGH_AIR ? BossSpec.HIGH_AIR_SCALE : 1;
        double reach = spec.size().height() / 2 * scale;
        for (int p = 0; p < parts; p++) {
            PartSpec part = spec.parts().get(p);
            reach = Math.max(reach, (Math.abs(poseDy[0][p]) + part.box().height() / 2) * scale);
        }
        entryY = PlayField.HEIGHT + reach;
        int attacks = boss == null ? 0 : boss.attacks().size();
        attackTicks = new int[attacks];
        attackBurstLeft = new int[attacks];
        attackBurstTicks = new int[attacks];
        attackPart = new int[attacks];
        attackNext = new int[attacks];
        spiralAngles = new double[attacks];
        spiralTicks = new int[attacks];
        int phases = boss == null ? 0 : boss.phases().size();
        if (phases > Long.SIZE - 1) {
            throw new IllegalArgumentException(spec.slug() + ": at most " + (Long.SIZE - 1) + " phases");
        }
        for (int f = 0; f < phases; f++) {
            for (int p : boss.phases().get(f).exposes()) {
                exposedFrom[p] = Math.max(exposedFrom[p], f);
            }
        }
        windows = new BossWindows(this, boss, parts, phases);
        arena = boss != null && boss.arena().isPresent()
                ? new SlamArena(this, boss, boss.arena().get(), parts)
                : null;
        arenaView = java.util.Optional.ofNullable(arena);
        stride = boss != null && boss.stride().isPresent() ? new StrideArena(this, boss, spec) : null;
        strideView = java.util.Optional.ofNullable(stride);
        breach = boss != null && boss.breach().isPresent() ? new BreachArena(this, boss, spec) : null;
        breachView = java.util.Optional.ofNullable(breach);
        coil = boss != null && boss.coil().isPresent() ? new CoilArena(this, boss, spec) : null;
        coilView = java.util.Optional.ofNullable(coil);
        hatches = new BossHatches(
                this, boss, parts, stride != null && boss.stride().get().hatchArmour());
        boolean moves = false;
        for (int f = 0; f < phases; f++) {
            moves |= boss.phases().get(f).move().isPresent();
        }
        lifts = arena != null && moves;
        stays = new boolean[parts];
        sweeps = new BossSweeps(this, boss);
        aimTurns = new BossAimTurns(this, boss);
        reset();
    }

    /** Back to the level start: before its first pass, every part whole. */
    void reset() {
        pass = 0;
        present = false;
        destroyed = false;
        escaped = false;
        onPlane = false;
        layer = Layer.HIGH_AIR;
        x = y = prevX = prevY = 0;
        altitude = prevAltitude = 1;
        cos = 1;
        sin = 0;
        hitCooldown = 0;
        for (int p = 0; p < partHp.length; p++) {
            partHp[p] = spec.parts().get(p).hp();
            partTicksSinceHit[p] = Integer.MAX_VALUE;
            volleyTicks[p] = 0;
            burstLeft[p] = 0;
            burstTicks[p] = 0;
        }
        if (boss != null) {
            layer = boss.layer();
            bossTicks = -1;
            swayTicks = -1;
            phase = 0;
            swayStart = 0;
            engaged = false;
            moveTicks = -1;
            moveFromX = moveFromY = moveFromAltitude = 0;
            moveFromLayer = layer;
            anchorX = boss.x();
            anchorY = boss.hoverY();
            pose = fromPose = 0;
            phaseTicks = 0;
            timeouts = 0;
            windows.reset();
            java.util.Arrays.fill(spiralAngles, 0);
            java.util.Arrays.fill(spiralTicks, 0);
            turn = -1;
            turnTicks = 0;
            streamTicks = 0;
            streamLeft = 0;
            streamUnitTicks = 0;
            streamUnits = 0;
            killTicks = -1;
            chains.reset();
            java.util.Arrays.fill(attackTicks, 0);
            java.util.Arrays.fill(attackBurstLeft, 0);
            java.util.Arrays.fill(attackBurstTicks, 0);
            java.util.Arrays.fill(attackPart, -1);
            java.util.Arrays.fill(attackNext, 0);
            aimTurns.reset();
            x = prevX = boss.x();
            y = prevY = entryY;
            altitude = prevAltitude = layer == Layer.HIGH_AIR ? 1 : 0;
            approaching = false;
            lifted = false;
            java.util.Arrays.fill(stays, false);
            liftPose = 0;
            liftEndTicks = -1;
            sweeps.reset();
            if (arena != null) {
                arena.reset();
                y = prevY = anchorGround - groundScroll;
            }
            hatches.reset();
            if (stride != null) {
                // M7 part F: off the screen above the top edge until it arrives.
                stride.reset();
                x = prevX = stride.x();
                y = prevY = stride.y();
            }
            if (breach != null) {
                // M7 part H: buried above the top edge until it arrives.
                breach.reset();
                x = prevX = breach.mouthX();
                y = prevY = breach.mouthY();
            }
            if (coil != null) {
                // M8 part H: under the water at its rest until it arrives.
                coil.reset();
                x = prevX = coil.headX();
                y = prevY = coil.headY();
            }
            chains.place();
        }
    }

    /**
     * M5 part E: an arena boss's place on the ground layer: its centre's height above the screen's
     * bottom edge at the level start, so that it lies {@link BossSpec#hoverY()} above the bottom
     * edge once the scroll has halted in its arena (the sortie works it out).
     */
    void anchorAt(double ground) {
        anchorGround = ground;
    }

    /**
     * M5 part E: the ground's scroll this step and whether the scroll has halted in the arena, read
     * by an arena boss as it moves (before {@link #update}).
     */
    void ground(double scroll, boolean halt) {
        groundStep = scroll - groundScroll;
        groundScroll = scroll;
        halted = halt;
    }

    /**
     * Moves it to where its pass has it at {@code levelTick}; returns whether its last pass ended
     * with it alive this step (it escaped). A boss arrives at its time and then flies on its own
     * clock (the level clock may halt in its arena); it never escapes.
     */
    boolean update(int levelTick) {
        if (boss != null) {
            prevX = x;
            prevY = y;
            for (int p = 0; p < partTicksSinceHit.length; p++) {
                if (partTicksSinceHit[p] < Integer.MAX_VALUE) {
                    partTicksSinceHit[p]++;
                }
            }
            if (hitCooldown > 0) {
                hitCooldown--;
            }
            moveBoss(levelTick);
            return false;
        }
        prevX = x;
        prevY = y;
        prevAltitude = altitude;
        if (hitCooldown > 0) {
            hitCooldown--;
        }
        for (int p = 0; p < partTicksSinceHit.length; p++) {
            if (partTicksSinceHit[p] < Integer.MAX_VALUE) {
                partTicksSinceHit[p]++;
            }
        }
        if (destroyed || escaped) {
            return false;
        }
        double t = levelTick * SimStep.SECONDS;
        List<Pass> passes = spec.passes();
        while (pass < passes.size()) {
            Pass current = passes.get(pass);
            if (t < current.start()) {
                present = false;
                return false;
            }
            if (place(current, t)) {
                if (!present) {
                    // Entering a pass: it appears at its first point, without a jump from the last one.
                    present = true;
                    prevX = x;
                    prevY = y;
                    prevAltitude = altitude;
                    cos = Trig.cos(current.headingRadians());
                    sin = Trig.sin(current.headingRadians());
                }
                boolean plane = layer.collidesWithPlayer();
                if (plane && !onPlane) {
                    arm();
                }
                onPlane = plane;
                return false;
            }
            present = false;
            onPlane = false;
            pass++;
        }
        escaped = true;
        return true;
    }

    /** Places it on {@code pass} at {@code t}; returns false once the pass is over. */
    private boolean place(Pass current, double t) {
        List<Waypoint> path = current.path();
        if (!current.descends()) {
            if (t > path.getLast().t()) {
                return false;
            }
            along(path, t);
            layer = current.layer();
            altitude = layer == Layer.HIGH_AIR ? 1 : 0;
            return true;
        }
        if (t < current.leaveAt()) {
            along(path, t);
            if (t < current.descendAt() + current.descentSeconds()) {
                layer = Layer.HIGH_AIR;
                altitude = t < current.descendAt() ? 1 : 1 - (t - current.descendAt()) / current.descentSeconds();
            } else {
                layer = current.layer();
                altitude = 0;
            }
            return true;
        }
        along(path, current.leaveAt());
        y += (t - current.leaveAt()) * current.leaveSpeed();
        layer = Layer.HIGH_AIR;
        altitude = Math.min(1, (t - current.leaveAt()) / current.descentSeconds());
        return y - extent < PlayField.HEIGHT;
    }

    /** Its centre on the path at {@code t}: between the waypoints around it, at the last one after it. */
    private void along(List<Waypoint> path, double t) {
        Waypoint last = path.getLast();
        if (t >= last.t()) {
            x = last.x();
            y = last.y();
            return;
        }
        for (int i = 1; i < path.size(); i++) {
            Waypoint to = path.get(i);
            if (t < to.t()) {
                Waypoint from = path.get(i - 1);
                double p = Math.max(0, (t - from.t()) / (to.t() - from.t()));
                x = from.x() + (to.x() - from.x()) * p;
                y = from.y() + (to.y() - from.y()) * p;
                return;
            }
        }
    }

    /** It reached the player's layer: every living part's gun starts on its first-shot time. */
    private void arm() {
        for (int p = 0; p < partHp.length; p++) {
            PartSpec part = spec.parts().get(p);
            volleyTicks[p] = SimStep.ticks(part.firstShotSeconds()) + 1;
            burstLeft[p] = 0;
            burstTicks[p] = 0;
        }
    }

    /** Whether part {@code p} fires a shot this step: it lives, has a gun and the unit is on the player's layer. */
    boolean trigger(int p) {
        PartSpec part = spec.parts().get(p);
        if (!present || !onPlane || partHp[p] <= 0 || part.gun().isEmpty()) {
            return false;
        }
        EnemyGun gun = part.gun().get();
        if (burstLeft[p] > 0) {
            if (--burstTicks[p] > 0) {
                return false;
            }
            burstLeft[p]--;
            burstTicks[p] = SimStep.ticks(EnemyGun.BURST_GAP_SECONDS);
            return true;
        }
        if (--volleyTicks[p] > 0) {
            return false;
        }
        volleyTicks[p] = SimStep.ticks(gun.intervalSeconds());
        burstLeft[p] = gun.burst() - 1;
        burstTicks[p] = SimStep.ticks(EnemyGun.BURST_GAP_SECONDS);
        return true;
    }

    /**
     * A boss arrives at its time, descends to its height and then sways there; a phase's move
     * glides it to a new station and layer and turns it into a new pose first.
     */
    private void moveBoss(int levelTick) {
        if (destroyed) {
            if (arena != null) {
                // Round 41 (the 2026-10-19 playtest): a dead arena boss, the free spire too, falls to the
                // ground and scrolls away with it, with its stump, so every part's place moves with the ground.
                y -= groundStep;
            }
            return;
        }
        prevAltitude = altitude;
        if (breach != null) {
            moveBreach(levelTick);
            return;
        }
        if (coil != null) {
            moveCoil(levelTick);
            return;
        }
        if (stride != null) {
            moveStride(levelTick);
            return;
        }
        if (arena != null) {
            moveAnchored(levelTick);
            return;
        }
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = layer == Layer.HIGH_AIR ? 1 : 0;
            x = prevX = boss.x();
            y = prevY = entryY;
            if (boss.engagesOnArrival()) {
                engaged = true;
                startPhase();
            }
        }
        bossTicks++;
        if (moveTicks >= 0) {
            stepMove();
        } else if (swayTicks < 0) {
            y = Math.max(anchorY, entryY - bossTicks * boss.descentSpeed() * SimStep.SECONDS);
            if (y <= anchorY) {
                swayTicks = 0;
                if (!engaged) {
                    engaged = true;
                    startPhase();
                }
            }
        } else {
            swayTicks++;
            x = anchorX
                    + boss.sineAmplitude()
                            * Trig.sin(
                                    2 * StrictMath.PI * (swayTicks - swayStart) * SimStep.SECONDS / boss.sinePeriod());
        }
    }

    /**
     * M5 part E: an arena boss lies on the ground and scrolls with it: before it arrives it scrolls in
     * ({@link #approaching()} once a part of it may be over the top edge); it arrives on its time (the
     * bar), settles and engages when the scroll has halted. M5 part H: a phase's move lifts it off its
     * anchor ({@link #stepLift()}); from then on it holds at its new station, swaying.
     */
    private void moveAnchored(int levelTick) {
        if (!lifted) {
            x = boss.x();
            y = anchorGround - groundScroll;
        }
        if (bossTicks < 0) {
            approaching = y - (entryY - PlayField.HEIGHT) < PlayField.HEIGHT;
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            approaching = false;
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
        }
        bossTicks++;
        if (moveTicks >= 0) {
            stepLift();
        } else if (swayTicks >= 0) {
            swayTicks++;
            if (lifted) {
                x = anchorX
                        + boss.sineAmplitude()
                                * Trig.sin(2
                                        * StrictMath.PI
                                        * (swayTicks - swayStart)
                                        * SimStep.SECONDS
                                        / boss.sinePeriod());
            }
        } else if (halted) {
            swayTicks = 0;
            engaged = true;
            startPhase();
        }
    }

    /**
     * M7 part F: a walking boss arrives on its time off the top edge and walks its passes ({@link StrideArena});
     * it settles and engages once its hull is wholly on the screen (invulnerable until then).
     */
    private void moveStride(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
            stride.arrive(phase);
            x = prevX = stride.x();
            y = prevY = stride.y();
        } else {
            stride.step(phase);
            x = stride.x();
            y = stride.y();
        }
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (wholeOnField(stride.hull())) {
            swayTicks = 0;
            engaged = true;
            startPhase();
        }
    }

    /**
     * M7 part H: a breaching boss arrives on its time (the bar), buried; its cycle runs on the real steps ({@link
     * BreachArena}) and it engages when its intro is over (its first tell); its centre is its mouth's.
     */
    private void moveBreach(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
            breach.arrive();
        } else {
            breach.step(phase);
        }
        x = breach.mouthX();
        y = breach.mouthY();
        if (poseDx.length == BreachArena.POSES) {
            // Its data's poses, one per breach point in the poses' order: the mouth's part layout at the point.
            pose = fromPose = breach.pose().ordinal();
        }
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (breach.engages()) {
            swayTicks = 0;
            engaged = true;
            startPhase();
        }
    }

    /**
     * M8 part H: a coiled boss arrives on its time (the bar) under the water and rises over its intro; its script runs
     * on the real steps ({@link CoilArena}) and it engages once it is up; its centre is its head's, on the play plane
     * (its body colliding) while the head is.
     */
    private void moveCoil(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            altitude = prevAltitude = 0;
            coil.arrive();
        } else {
            coil.step(phase);
        }
        x = coil.headX();
        y = coil.headY();
        onPlane = coil.headOnPlane() && layer.collidesWithPlayer();
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (coil.engages()) {
            swayTicks = 0;
            engaged = true;
            startPhase();
        }
    }

    /** M8 part H: the coiled boss reads where the ship is before it moves (a bight, a gap, the column follow it). */
    void coilTargets(double shipX, double shipY) {
        if (coil != null) {
            coil.targets(shipX, shipY);
        }
    }

    /** M7 part H: the breaching boss reads where the ship is before it moves (its next point may follow it). */
    void breachTargets(double shipX, double shipY) {
        if (breach != null) {
            breach.targets(shipX, shipY);
        }
    }

    /** M7 part F: whether part {@code p}'s box lies wholly on the play field. */
    private boolean wholeOnField(int p) {
        Hitbox box = box(p);
        double px = partX(p);
        double py = partY(p);
        return px - box.width() / 2 >= 0
                && px + box.width() / 2 <= PlayField.WIDTH
                && py - box.height() / 2 >= 0
                && py + box.height() / 2 <= PlayField.HEIGHT;
    }

    /** M7 part F: the walking boss reads where the ship and Rook are ({@code rookIn}: he flies) before it moves. */
    void strideTargets(double shipX, double shipY, double rookX, double rookY, boolean rookIn) {
        if (stride != null) {
            stride.targets(shipX, shipY, rookX, rookY, rookIn);
        }
    }

    /**
     * M5 part H, one step of an anchored boss's lift (design/enemies/bosses/siege-spire; decision H4 =
     * a): it glides from its anchor to the move's station, eased (smoothstep); at the glide's end it and
     * its lifting parts take the move's layer (on the player's layer only from then: no contact while it
     * lifts), then it turns into the move's pose, and at the end the phase engages there.
     */
    private void stepLift() {
        BossSpec.Move move = boss.phases().get(phase).move().orElseThrow();
        moveTicks++;
        int glide = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        double share = glide == 0 ? 1 : Math.min(1, (double) moveTicks / glide);
        double eased = share * share * (3 - 2 * share);
        x = moveFromX + (move.x() - moveFromX) * eased;
        y = moveFromY + (move.y() - moveFromY) * eased;
        if (share >= 1 && layer != move.layer()) {
            layer = move.layer();
            for (int p = 0; p < stays.length; p++) {
                if (!stays[p]) {
                    arena.lift(p, move.layer());
                }
            }
        }
        onPlane = layer.collidesWithPlayer();
        if (moveTicks >= glide && 2 * (moveTicks - glide) >= turnSteps) {
            pose = move.pose();
        }
        if (moveTicks >= glide + turnSteps) {
            pose = fromPose = move.pose();
            anchorX = move.x();
            anchorY = move.y();
            moveTicks = -1;
            swayStart = swayTicks;
            liftEndTicks = bossTicks;
            armPhase();
        }
    }

    /**
     * One step of the phase's move: the glide to its station and layer, then the turn in place
     * (the pose switching half-way); at its end the phase engages.
     */
    private void stepMove() {
        BossSpec.Move move = boss.phases().get(phase).move().orElseThrow();
        moveTicks++;
        int descend = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        double share = descend == 0 ? 1 : Math.min(1, (double) moveTicks / descend);
        double toAltitude = move.layer() == Layer.HIGH_AIR ? 1 : 0;
        x = moveFromX + (move.x() - moveFromX) * share;
        y = moveFromY + (move.y() - moveFromY) * share;
        altitude = moveFromAltitude + (toAltitude - moveFromAltitude) * share;
        if (share >= 1) {
            layer = move.layer();
        } else if (moveFromLayer == Layer.HIGH_AIR || move.layer() == Layer.HIGH_AIR) {
            layer = Layer.HIGH_AIR;
        } else {
            layer = move.layer();
        }
        onPlane = layer.collidesWithPlayer();
        if (moveTicks >= descend && 2 * (moveTicks - descend) >= turnSteps) {
            pose = move.pose();
        }
        if (moveTicks >= descend + turnSteps) {
            pose = fromPose = move.pose();
            anchorX = move.x();
            anchorY = move.y();
            moveTicks = -1;
            if (swayTicks < 0) {
                // A move cut its entrance short: it holds at the new station without settling again.
                swayTicks = 1;
            }
            swayStart = swayTicks;
            armPhase();
        }
    }

    /**
     * A boss's step after it moved: its necks bend toward the ship, its windows close in time, its
     * phase ends on its parts or its timer, and once engaged (and not moving) its attacks, windows
     * and streams run; they reach the level through {@code actions} only while {@code firing}.
     */
    void act(double shipX, double shipY, BossActions actions, boolean firing) {
        if (boss == null || !present || destroyed) {
            return;
        }
        chains.bend(
                shipX, shipY, !engaged ? Double.NaN : boss.phases().get(phase).bendRadians());
        if (!engaged) {
            return;
        }
        windows.tick();
        hatches.tick();
        if (moveTicks >= 0) {
            return;
        }
        sweeps.aimAt(shipX, shipY);
        if (sweeps.any()) {
            sweeps.step(actions);
        }
        phaseTicks++;
        while (phase < boss.phases().size() - 1 && phaseOver(boss.phases().get(phase))) {
            if (!partsDown(boss.phases().get(phase)) && !arenaOver(boss.phases().get(phase))) {
                timeouts |= 1L << phase;
                actions.timeout(phase);
            }
            phase++;
            startPhase();
            actions.phase(phase);
            dropOnStart(actions);
            if (moveTicks >= 0) {
                return;
            }
        }
        BossSpec.Phase current = boss.phases().get(phase);
        if (coil != null) {
            // M8 part H: the coil's events, strikes, fire and sheds on its own clocks.
            coil.act(actions, firing);
            return;
        }
        if (breach != null) {
            // M7 part H: the breach's events and bite, then the phase's attacks from the open mouth.
            breach.act(actions, firing);
            breachAttacks(current, actions, firing);
            if (current.windows().isPresent()) {
                windows.step(current.windows().get(), shipX, shipY, actions, firing);
            }
            if (current.stream().isPresent()) {
                stream(current.stream().get(), actions, firing);
            }
            return;
        }
        if (stride != null) {
            // M7 part F: the flame cone and the vents after the walk.
            stride.act(phase, actions, firing);
        }
        if (arena != null) {
            boolean fans = arena.step(current, shipX, actions, firing);
            chains.place();
            if (current.arena().surface().isPresent()) {
                // The phase's attacks fire from its surfacing part while it is up, each after its tell.
                if (fans) {
                    for (int i = 0; i < current.attacks().size(); i++) {
                        volley(current.attacks().get(i), current, actions, firing);
                    }
                }
                return;
            }
        }
        if (current.alternate()) {
            alternate(current, actions, firing);
        } else {
            for (int i = 0; i < current.attacks().size(); i++) {
                runAttack(current.attacks().get(i), current, actions, firing);
            }
        }
        if (current.windows().isPresent()) {
            windows.step(current.windows().get(), shipX, shipY, actions, firing);
        }
        if (current.stream().isPresent()) {
            stream(current.stream().get(), actions, firing);
        }
    }

    /**
     * M7 part H: while the mouth is open the phase's first attack fires on the breach's ring times and its other
     * attacks half an interval after each (a spiral among them runs on its own clock while the mouth is open).
     */
    private void breachAttacks(BossSpec.Phase current, BossActions actions, boolean firing) {
        int fires = breach.fires();
        boolean first = true;
        for (int i = 0; i < current.attacks().size(); i++) {
            int a = current.attacks().get(i);
            if (boss.attacks().get(a).pattern() == BossSpec.Pattern.SPIRAL) {
                // Step H3: from the glow's end while open, its first bullets at once.
                if (breach.spiralStarts()) {
                    spiralTicks[a] = 1;
                }
                if (breach.spiralling()) {
                    runAttack(a, current, actions, firing);
                }
            } else if (breach.timed(a)) {
                if ((fires & (first ? BreachArena.RING : BreachArena.OTHERS)) != 0) {
                    volley(a, current, actions, firing);
                }
                first = false;
            }
        }
        int burst = breach.ventBurst();
        if (burst >= 0) {
            // Step H3: the risen spine vents' aimed burst on its own clock while the mouth is open.
            if (breach.opened()) {
                attackTicks[burst] = SimStep.ticks(BreachArena.VENT_FIRST_SECONDS) + 1;
                attackBurstLeft[burst] = 0;
            }
            if (breach.ventsFiring()) {
                runAttack(burst, current, actions, firing);
            } else {
                attackBurstLeft[burst] = 0;
            }
        }
    }

    /** Whether the phase is over: its parts are down, its timer ran out, or (M5 part E) its slams or its HP share. */
    private boolean phaseOver(BossSpec.Phase current) {
        if (coil != null && coil.holdsPhase(phase)) {
            // M8 part H: the dive's phase does not end before its dive is over.
            return false;
        }
        return partsDown(current)
                || arenaOver(current)
                || (current.timed() && phaseTicks >= SimStep.ticks(current.seconds()));
    }

    /**
     * M5 part E: whether an arena phase is over: it struck its slams (or, ending on slams, every arm
     * is severed), or its parts hold less than its share of their HP.
     */
    private boolean arenaOver(BossSpec.Phase current) {
        if (arena == null) {
            return false;
        }
        BossSpec.PhaseArena keys = current.arena();
        if (keys.slams() > 0 && (arena.slams() >= keys.slams() || !arena.armsLeft())) {
            return true;
        }
        if (!keys.onShare()) {
            return false;
        }
        double left = 0;
        double full = 0;
        for (int i = 0; i < keys.belowParts().size(); i++) {
            int p = keys.belowParts().get(i);
            left += Math.max(0, partHp[p]);
            full += spec.parts().get(p).hp();
        }
        return left < keys.below() * full;
    }

    /** M5 part E: a phase that drops a pickup as it begins drops it under its surfacing part (or the centre). */
    private void dropOnStart(BossActions actions) {
        if (arena == null) {
            return;
        }
        BossSpec.PhaseArena keys = boss.phases().get(phase).arena();
        if (keys.drop().isPresent() && breach != null) {
            // M7 part H, step H3: in the open field, not at the (maybe buried, edge) mouth.
            actions.drop(keys.drop().get(), breach.dropX(), breach.dropY());
        } else if (keys.drop().isPresent() && coil != null) {
            // M8 part H: in the open field inside the coil, not at the head by the top edge.
            actions.drop(keys.drop().get(), coil.dropX(), coil.dropY());
        } else if (keys.drop().isPresent()) {
            int p = keys.surface().isPresent() ? keys.surface().get().part() : -1;
            actions.drop(keys.drop().get(), p < 0 ? x : partX(p), p < 0 ? y : partY(p));
        }
    }

    /** Whether at most the phase's {@code left} of its parts are alive (never for a phase without parts). */
    private boolean partsDown(BossSpec.Phase current) {
        if (current.untilParts().isEmpty()) {
            return false;
        }
        int alive = 0;
        for (int i = 0; i < current.untilParts().size(); i++) {
            alive += partHp[current.untilParts().get(i)] > 0 ? 1 : 0;
        }
        return alive <= current.left();
    }

    /** A phase starts: its windows close; it begins its move, or engages at once. */
    private void startPhase() {
        windows.closeAll();
        BossSpec.Phase current = boss.phases().get(phase);
        if (arena != null) {
            arena.phaseStarted(current);
        }
        // M5 part H: a sweep under way ends with its phase.
        sweeps.endAll();
        if (current.move().isPresent() && arena != null && !lifted) {
            // M5 part H: the lift: what is destroyed or armoured now stays at the anchor (the stump).
            lifted = true;
            liftPose = pose;
            for (int p = 0; p < stays.length; p++) {
                stays[p] = armoured[p] || partHp[p] <= 0;
            }
        }
        if (current.move().isPresent()) {
            moveTicks = 0;
            moveFromX = x;
            moveFromY = y;
            moveFromAltitude = altitude;
            moveFromLayer = layer;
            fromPose = pose;
            return;
        }
        armPhase();
    }

    /**
     * A phase engages: its timer starts; after its delay its attacks fire (each a volley one
     * interval later), its alternation takes its first turn, its windows open and its stream sends.
     */
    private void armPhase() {
        BossSpec.Phase current = boss.phases().get(phase);
        // Round 40: an aim turn's glow ends with its phase.
        aimTurns.endGlows();
        int delay = SimStep.ticks(current.delaySeconds());
        for (int i = 0; i < current.attacks().size(); i++) {
            int a = current.attacks().get(i);
            BossSpec.Attack attack = boss.attacks().get(a);
            attackTicks[a] = delay + SimStep.ticks(interval(attack, current));
            attackBurstLeft[a] = 0;
            spiralTicks[a] = delay;
            if (attack.stagger()) {
                // M5 part H: a staggered attack's parts take their turns from the first again.
                attackNext[a] = 0;
            }
            if (attack.aimTurns().isPresent()) {
                // Round 40: the first aimed shot one turn after the delay, from the first part.
                aimTurns.arm(a, attack.aimTurns().get(), delay);
            }
        }
        turn = -1;
        turnTicks = Math.max(1, delay);
        streamLeft = 0;
        streamTicks = Math.max(1, delay);
        windows.arm(current, delay);
        phaseTicks = 0;
    }

    /** The phase's attacks in turn: each runs its turn (a ring its interval, a spiral its duration), then hands over. */
    private void alternate(BossSpec.Phase current, BossActions actions, boolean firing) {
        if (--turnTicks <= 0) {
            turn = (turn + 1) % current.attacks().size();
            BossSpec.Attack attack = boss.attacks().get(current.attacks().get(turn));
            turnTicks = Math.max(1, SimStep.ticks(attack.turnSeconds()));
            spiralTicks[current.attacks().get(turn)] = 0;
            if (attack.pattern() != BossSpec.Pattern.SPIRAL) {
                volley(current.attacks().get(turn), current, actions, firing);
            }
        }
        if (turn < 0) {
            return;
        }
        int a = current.attacks().get(turn);
        BossSpec.Attack attack = boss.attacks().get(a);
        if (attack.pattern() == BossSpec.Pattern.SPIRAL) {
            spiral(a, attack, current, actions, firing);
        }
    }

    /** A spiral's step: its arms turn, and each fires a bullet every interval (each spiral on its own state). */
    private void spiral(int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        spiralAngles[a] = Math.IEEEremainder(
                spiralAngles[a] + attack.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        if (--spiralTicks[a] > 0) {
            return;
        }
        spiralTicks[a] = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
        if (!firing) {
            return;
        }
        List<Integer> parts = firingParts(attack, current);
        for (int i = 0; i < parts.size(); i++) {
            int p = parts.get(i);
            if (partHp[p] <= 0) {
                continue;
            }
            for (int k = 0; k < attack.arms(); k++) {
                actions.bullet(
                        partX(p),
                        partY(p),
                        spiralAngles[a] + 2 * StrictMath.PI * k / attack.arms(),
                        attack.gun().bulletSpeed(),
                        attack.gun().damage());
            }
        }
    }

    /** An attack fired on its own interval: a volley when it is due, then the rest of its burst. */
    private void runAttack(int a, BossSpec.Phase current, BossActions actions, boolean firing) {
        BossSpec.Attack attack = boss.attacks().get(a);
        fireAttack(a, attack, current, actions, firing);
        if (attack.aimTurns().isPresent()) {
            // Round 40: after the regular fire, so a part holds it through its aimed shot's step as well.
            aimTurns.turn(a, attack, current, actions, firing);
        }
    }

    private void fireAttack(
            int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        if (attack.pattern() == BossSpec.Pattern.SPIRAL) {
            spiral(a, attack, current, actions, firing);
            return;
        }
        if (attackBurstLeft[a] > 0) {
            if (--attackBurstTicks[a] <= 0) {
                attackBurstLeft[a]--;
                attackBurstTicks[a] = Math.max(1, SimStep.ticks(attack.burstGapSeconds()));
                shoot(a, attack, current, actions, firing);
            }
        }
        if (--attackTicks[a] > 0) {
            if (stride != null && attack.gun().homing().isPresent()) {
                hatches.tell(attack, current, attackTicks[a], attackNext[a], actions, firing);
            }
            return;
        }
        attackTicks[a] = Math.max(1, SimStep.ticks(interval(attack, current)));
        volley(a, current, actions, firing);
    }

    /** M7 part F: whether part {@code p}'s hatch is open (its homing salvo's tell and the salvo). */
    public boolean partHatchOpen(int p) {
        return hatches.open(p);
    }

    /**
     * The time between two volleys of {@code attack}: its gun's interval; M5 part H: a staggered
     * attack's turns come that interval ÷ its parts apart.
     */
    private static double interval(BossSpec.Attack attack, BossSpec.Phase current) {
        double interval = attack.gun().intervalSeconds();
        return attack.stagger()
                ? interval / Math.max(1, firingParts(attack, current).size())
                : interval;
    }

    /** The first shot of a volley: a rotating attack picks the next living part; a burst follows. */
    private void volley(int a, BossSpec.Phase current, BossActions actions, boolean firing) {
        BossSpec.Attack attack = boss.attacks().get(a);
        List<Integer> parts = firingParts(attack, current);
        attackPart[a] = -1;
        if (attack.stagger()) {
            // M5 part H: the next part's turn; a destroyed part's turn passes silently.
            int p = parts.get(attackNext[a] % parts.size());
            attackNext[a] = (attackNext[a] + 1) % parts.size();
            if (partHp[p] <= 0) {
                attackBurstLeft[a] = 0;
                return;
            }
            attackPart[a] = p;
        }
        if (attack.rotate()) {
            for (int i = 0; i < parts.size(); i++) {
                int candidate = (attackNext[a] + i) % parts.size();
                if (partHp[parts.get(candidate)] > 0) {
                    attackPart[a] = parts.get(candidate);
                    attackNext[a] = (candidate + 1) % parts.size();
                    break;
                }
            }
            if (attackPart[a] < 0) {
                return;
            }
        }
        attackBurstLeft[a] = attack.pattern() == BossSpec.Pattern.AIMED || attack.pattern() == BossSpec.Pattern.FAN
                ? attack.gun().burst() - 1
                : 0;
        attackBurstTicks[a] = Math.max(1, SimStep.ticks(attack.burstGapSeconds()));
        shoot(a, attack, current, actions, firing);
    }

    /** One shot (or ring) of attack {@code a} from its part, or from every living firing part. */
    private void shoot(int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        if (!firing) {
            return;
        }
        if (attackPart[a] >= 0) {
            // Round 40: a part in its aim turn's glow holds its regular fire.
            if (partHp[attackPart[a]] > 0 && attackPart[a] != aimTurns.part(a)) {
                fireFrom(a, attackPart[a], attack, actions);
            }
            return;
        }
        List<Integer> parts = firingParts(attack, current);
        for (int i = 0; i < parts.size(); i++) {
            int p = parts.get(i);
            if (partHp[p] > 0 && p != aimTurns.part(a)) {
                fireFrom(a, p, attack, actions);
            }
        }
    }

    private void fireFrom(int a, int p, BossSpec.Attack attack, BossActions actions) {
        if (breach != null && !arena.layer(p).touchable()) {
            // M7 part H, step H3: a breaching boss fires from no buried part (a sunk spine vent).
            return;
        }
        if (stride != null && !partOnField(p)) {
            // M7 part F: a walking boss fires only from parts on the screen.
            return;
        }
        if (attack.pattern() == BossSpec.Pattern.LOB) {
            // M5 part H: the mortar root's lob at the ship's position now.
            actions.lob(partX(p), partY(p), attack.gun());
            return;
        }
        if (attack.pattern() == BossSpec.Pattern.SWEEP) {
            sweeps.start(a, p, attack, actions);
            return;
        }
        // M5 part E: an arena boss's surfacing part fires from its hit box's lower edge (the Kraken's beak).
        double fireY = arena != null && p == arena.surfacePart()
                ? partY(p) - spec.parts().get(p).box().height() / 2
                : partY(p);
        if (attack.pattern() == BossSpec.Pattern.RING) {
            actions.ring(
                    partX(p),
                    fireY,
                    attack.count(),
                    attack.gun().bulletSpeed(),
                    attack.gun().damage());
        } else if (attack.firesDown(p) && stride != null) {
            // M7 part F: a walking boss's "down" is along its heading (its chin burst in its first phase).
            actions.along(partX(p), fireY, stride.headingRadians(), attack.gun());
        } else if (attack.firesDown(p)) {
            // Round 34, P2: this part fires its aimed attack straight down (the kinder Siege Spire's pods).
            actions.down(partX(p), fireY, attack.gun());
        } else {
            actions.aimed(partX(p), fireY, attack.gun());
        }
    }

    /** M5 part H: whether attack {@code a}'s sweep is in its telegraph now. */
    public boolean sweepTelegraphing(int a) {
        return sweeps.telegraphing(a);
    }

    /** M5 part H: whether attack {@code a}'s beam sweeps now (it hurts what it touches, once per sweep each). */
    public boolean sweepBeaming(int a) {
        return sweeps.beaming(a);
    }

    /** M5 part H: the attacks whose sweeps run, for the presentation (as {@link BossSpec#attacks()}). */
    public int attackCount() {
        return sweeps.count();
    }

    /** Round 40: the part glowing in attack {@code a}'s aim turn (its aimed shot comes next); -1 for none. */
    public int aimPart(int a) {
        return aimTurns.part(a);
    }

    /** Round 40: how far attack {@code a}'s aim glow has run, 0–1 (0 without a glowing part). */
    public double aimGlowShare(int a) {
        return aimTurns.glowShare(a);
    }

    /** Round 40: whether part {@code p} glows in an aim turn (the tell before its aimed shot). */
    public boolean partAimGlowing(int p) {
        return aimTurns.glowing(p);
    }

    /** M5 part H: attack {@code a}'s beam's origin now, px from the left: its part's centre. */
    public double sweepOriginX(int a) {
        return sweeps.originX(a);
    }

    /** M5 part H: attack {@code a}'s beam's origin now, px up: its sweep's origin below its part's centre. */
    public double sweepOriginY(int a) {
        return sweeps.originY(a);
    }

    /** M5 part H: attack {@code a}'s sweep's first edge, radians clockwise from straight down (the telegraph's line). */
    public double sweepFrom(int a) {
        return sweeps.from(a);
    }

    /** M5 part H: attack {@code a}'s sweep's last edge, radians clockwise from straight down. */
    public double sweepTo(int a) {
        return sweeps.to(a);
    }

    /**
     * M5 part H: attack {@code a}'s beam's heading, radians clockwise from straight down, moving evenly
     * from {@link #sweepFrom} to {@link #sweepTo} over the sweep; {@code alpha} interpolates between steps.
     */
    public double sweepBeam(int a, double alpha) {
        return sweeps.beam(a, alpha);
    }

    /** M5 part H: whether attack {@code a}'s current sweep hit {@code what} ({@link #SWEPT_SHIP}, {@link #SWEPT_WINGMAN}) already. */
    boolean sweepHit(int a, int what) {
        return sweeps.hit(a, what);
    }

    /** M5 part H: attack {@code a}'s current sweep hit {@code what}: it does not again in this sweep. */
    void markSweepHit(int a, int what) {
        sweeps.markHit(a, what);
    }

    /** The parts that fire {@code attack}: its own, or else the parts the phase ends on. */
    static List<Integer> firingParts(BossSpec.Attack attack, BossSpec.Phase current) {
        return attack.parts().isEmpty() ? current.untilParts() : attack.parts();
    }

    /** The phase's stream: the first at its start, then every {@code every} s, one unit per interval. */
    private void stream(BossSpec.Stream stream, BossActions actions, boolean firing) {
        if (--streamTicks <= 0) {
            streamTicks = Math.max(1, SimStep.ticks(stream.everySeconds()));
            streamLeft = stream.count();
            streamUnitTicks = 1;
        }
        if (streamLeft > 0 && --streamUnitTicks <= 0) {
            streamLeft--;
            streamUnitTicks = Math.max(1, SimStep.ticks(stream.intervalSeconds()));
            boolean left =
                    switch (stream.edge()) {
                        case LEFT -> true;
                        case RIGHT -> false;
                        case NONE, ALTERNATING, TOP, BOTTOM -> streamUnits % 2 == 0;
                    };
            streamUnits++;
            if (firing) {
                actions.release(stream, left);
            }
        }
    }

    /**
     * Whether part {@code p} takes no damage now: a boss's parts before it engages (its descent)
     * and during a phase's move, its fire-only parts, the parts a later phase exposes before it,
     * and a window part while its window is shut. A shot on it glances off.
     */
    public boolean partShielded(int p) {
        if (boss == null) {
            return false;
        }
        if (!engaged || moveTicks >= 0 || armoured[p] || phase < exposedFrom[p]) {
            return true;
        }
        if (breach != null && !arena.layer(p).touchable()) {
            // M7 part H: a breaching boss's buried mouth: nothing touches it (the Lance and the Smart Bomb included).
            return true;
        }
        if (coil != null && coil.implant(p) >= 0 && !coil.implantOpen(coil.implant(p))) {
            // M8 part H: a shut implant is armoured.
            return true;
        }
        if (hatches.armoured(p) && !hatches.open(p) && !stride.overheating()) {
            // Round 44: a walking boss's pod is armoured while its hatch is shut (until it overheats).
            return true;
        }
        return windows.windowed(phase, p) && windows.openTicks(p) == 0;
    }

    /** Round 44: whether part {@code p} takes damage only while its hatch is open (a walking boss's pod). */
    public boolean partHatchArmoured(int p) {
        return hatches.armoured(p);
    }

    /** Whether part {@code p} is fire-only: it fires, never takes damage, is not in the bar and pays nothing. */
    public boolean partArmoured(int p) {
        return armoured[p];
    }

    /** Whether the current phase's windows hold part {@code p} (it opens and shuts). */
    public boolean partWindowed(int p) {
        return boss != null && windows.windowed(phase, p);
    }

    /** Whether part {@code p}'s window is open now and the part lives (it takes damage, unless the boss is moving). */
    public boolean partOpen(int p) {
        return windows.openTicks(p) > 0 && partHp[p] > 0;
    }

    /** Seconds part {@code p}'s window stays open; 0 shut. */
    public double partOpenSeconds(int p) {
        return windows.openTicks(p) * SimStep.SECONDS;
    }

    /** The current phase's window open time, s (0 without windows): with {@link #partOpenSeconds} the opening's progress. */
    public double windowOpenSeconds() {
        if (boss == null) {
            return 0;
        }
        var windows = boss.phases().get(phase).windows();
        return windows.isPresent() ? windows.get().openSeconds() : 0;
    }

    /** Whether a shot of {@code size} at (sx, sy) touches one of its armoured neck segments (not a slam arm's). */
    boolean neckTouches(Hitbox size, double sx, double sy) {
        return chains.neckTouches(size, sx, sy);
    }

    /**
     * M7 part A: where a beam's column (centred on {@code cx}, {@code halfWidth} either side, running up
     * from {@code fromY}) first touches one of its armoured neck segments (not a slam arm's); {@link
     * Double#POSITIVE_INFINITY} for none.
     */
    double neckColumn(double cx, double halfWidth, double fromY) {
        return chains.neckColumn(cx, halfWidth, fromY);
    }

    /**
     * M7 part A: where a beam's column (see {@link #neckColumn}) first touches part {@code p}: its hit
     * box, or a slam arm's segments; {@link Double#POSITIVE_INFINITY} for none (a lifted boss's stump
     * is scenery, as for shots).
     */
    double partColumn(int p, double cx, double halfWidth, double fromY) {
        if (lifted && stays[p]) {
            return Double.POSITIVE_INFINITY;
        }
        double nearest = PlayerFire.columnContact(cx, halfWidth, fromY, partX(p), partY(p), box(p));
        int c = chains.slamChain(p);
        return c < 0 ? nearest : chains.segmentsColumn(c, cx, halfWidth, fromY, nearest);
    }

    /**
     * M5 part E: part {@code p}'s current layer: an arena boss's part's own ({@code sub} under the
     * water, {@code ground} on the surface), else the unit's.
     */
    public Layer partLayer(int p) {
        return arena == null ? layer : arena.layer(p);
    }

    /**
     * Whether a shot of {@code size} at (sx, sy) touches part {@code p}: its hit box, and (M5 part E)
     * a slam arm's segments, which are the arm's own hit boxes.
     */
    boolean partTouches(int p, Hitbox size, double sx, double sy) {
        if (lifted && stays[p]) {
            // M5 part H: the stump is scenery: shots fly over it.
            return false;
        }
        if (size.overlaps(sx, sy, box(p), partX(p), partY(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsTouch(c, size, sx, sy);
    }

    /** Whether a blast of {@code radius} at (bx, by) reaches part {@code p} (its hit box or a slam arm's segment). */
    boolean partInBlast(int p, double bx, double by, double radius) {
        if (lifted && stays[p]) {
            return false;
        }
        if (PlayerFire.inBlast(bx, by, radius, partX(p), partY(p), box(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsInBlast(c, bx, by, radius);
    }

    /** Whether part {@code p} overlaps the play field (its hit box, or a slam arm's segment). */
    boolean partOnField(int p) {
        if (PlayField.overlaps(partX(p), partY(p), box(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsOnField(c);
    }

    /** Part {@code p}'s hit box now: its spec's, M7 part F a walking boss's turned with its heading. */
    private Hitbox box(int p) {
        return stride != null ? stride.partBox(p) : spec.parts().get(p).box();
    }

    /**
     * M7 part F: whether a shot of {@code size} at (sx, sy) meets a walking boss's front plate (it glances); M8 part H:
     * or a coiled boss's armoured coil.
     */
    boolean plateTouches(Hitbox size, double sx, double sy) {
        return (stride != null && stride.plateTouches(size, sx, sy)) || (coil != null && coil.touches(size, sx, sy));
    }

    /** M7 part F: where a beam's column first touches a walking boss's front plate (M8 part H: a coil); +∞ for none. */
    double plateColumn(double cx, double halfWidth, double fromY) {
        double plate = stride == null ? Double.POSITIVE_INFINITY : stride.plateColumn(cx, halfWidth, fromY);
        return coil == null ? plate : Math.min(plate, coil.column(cx, halfWidth, fromY));
    }

    /**
     * Part {@code p} takes damage from a blast at (hx, hy): times an open weak spot's multiplier there
     * (M5 part E: the Kraken's eyes), else the part's own; returns whether that destroyed it.
     */
    boolean damagePartAt(int p, double amount, double hx, double hy) {
        return damagePartAt(p, amount, hx, hy, 0, 0);
    }

    /**
     * Part {@code p} takes damage from a shot at (hx, hy) flying along (dirX, dirY): times the
     * multiplier of an open weak spot on its line ahead (M5 part E: the Kraken's eyes), else the
     * part's own; returns whether that destroyed it.
     */
    boolean damagePartAt(int p, double amount, double hx, double hy, double dirX, double dirY) {
        if (arena == null) {
            return damagePart(p, amount);
        }
        partTicksSinceHit[p] = 0;
        if (coil != null) {
            // M8 part H: the throat while the jaw is open; an implant's hits count on the head.
            int q = coil.hpPart(p);
            partTicksSinceHit[q] = 0;
            boolean alive = partHp[q] > 0;
            partHp[q] -= amount
                    * coil.multiplier(p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[q] <= 0;
        }
        boolean alive = partHp[p] > 0;
        if (breach != null) {
            // M7 part H: the gullet's weak spots while the mouth is open.
            partHp[p] -= amount
                    * breach.multiplier(
                            p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[p] <= 0;
        }
        if (stride != null) {
            // M7 part F: the vents, turned with the heading, open after each burn.
            partHp[p] -= amount
                    * stride.multiplier(
                            p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[p] <= 0;
        }
        partHp[p] -= amount
                * arena.multiplier(
                        p,
                        hx,
                        hy,
                        dirX,
                        dirY,
                        partX(p),
                        partY(p),
                        spec.parts().get(p).multiplier());
        return alive && partHp[p] <= 0;
    }

    /**
     * Part {@code p} takes damage, times its multiplier; returns whether that destroyed it (M8 part H: the part whose
     * HP it took, {@link #hpPart}).
     */
    boolean damagePart(int p, double amount) {
        partTicksSinceHit[p] = 0;
        int q = hpPart(p);
        partTicksSinceHit[q] = 0;
        boolean alive = partHp[q] > 0;
        partHp[q] -= amount * spec.parts().get(p).multiplier();
        return alive && partHp[q] <= 0;
    }

    /**
     * M8 part H: the part whose HP a hit on part {@code p} takes: a coiled boss's implant's is the head's; any other
     * part's its own. A hit that destroys reports this part.
     */
    int hpPart(int p) {
        return coil == null ? p : coil.hpPart(p);
    }

    /**
     * M7 part A, the Orbital Lance (design/player/specials): part {@code p} takes {@code amount} as it
     * is, no weak-point multiplier; returns whether that destroyed it.
     */
    boolean damagePartFlat(int p, double amount) {
        partTicksSinceHit[p] = 0;
        int q = hpPart(p);
        partTicksSinceHit[q] = 0;
        boolean alive = partHp[q] > 0;
        partHp[q] -= amount;
        return alive && partHp[q] <= 0;
    }

    /** Destroys part {@code p} outright (the vital part took the rest with it); returns whether it still lived (and pays). */
    boolean wreckPart(int p) {
        // A fire-only part goes down with the boss but pays nothing.
        boolean alive = partHp[p] > 0 && !armoured[p];
        partHp[p] = Math.min(partHp[p], 0);
        return alive;
    }

    /** The unit is destroyed: it leaves the play field at once (its death plays where it was). */
    void destroy() {
        if (boss != null && killTicks < 0) {
            killTicks = Math.max(0, bossTicks);
        }
        destroyed = true;
        present = false;
        onPlane = false;
        if (stride != null) {
            // M7 part F: its cone goes cold as it topples.
            stride.died();
        }
        if (breach != null) {
            // M7 part H: no suction, no bite.
            breach.died();
        }
        if (coil != null) {
            // M8 part H: nothing fires, no sweep, no wall.
            coil.died();
        }
    }

    /** The body touching the ship: whether it deals its contact damage now (at most once per its interval). */
    boolean strike() {
        if (hitCooldown > 0) {
            return false;
        }
        hitCooldown = SimStep.ticks(SetPieceSpec.HIT_INTERVAL_SECONDS);
        return true;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(pass)
                .add(present ? 1 : 0)
                .add(destroyed ? 1 : 0)
                .add(escaped ? 1 : 0)
                .add(onPlane ? 1 : 0)
                .add(layer.ordinal())
                .add(x)
                .add(y)
                .add(altitude)
                .add(hitCooldown);
        for (int p = 0; p < partHp.length; p++) {
            hash.add(partHp[p])
                    .add(partTicksSinceHit[p])
                    .add(volleyTicks[p])
                    .add(burstLeft[p])
                    .add(burstTicks[p]);
        }
        if (boss != null) {
            hash.add(bossTicks)
                    .add(swayTicks)
                    .add(phase)
                    .add(turn)
                    .add(turnTicks)
                    .add(swayStart)
                    .add(engaged ? 1 : 0)
                    .add(moveTicks)
                    .add(moveFromX)
                    .add(moveFromY)
                    .add(moveFromAltitude)
                    .add(moveFromLayer.ordinal())
                    .add(anchorX)
                    .add(anchorY)
                    .add(pose)
                    .add(fromPose)
                    .add(phaseTicks)
                    .add(timeouts)
                    .add(windows.ticks())
                    .add(windows.next())
                    .add(windows.openings())
                    .add(prevAltitude)
                    .add(streamTicks)
                    .add(streamLeft)
                    .add(streamUnitTicks)
                    .add(streamUnits)
                    .add(killTicks);
            chains.addTo(hash);
            for (int a = 0; a < attackTicks.length; a++) {
                hash.add(attackTicks[a])
                        .add(attackBurstLeft[a])
                        .add(attackBurstTicks[a])
                        .add(attackPart[a])
                        .add(attackNext[a])
                        .add(spiralAngles[a])
                        .add(spiralTicks[a]);
            }
            windows.addOpenTicks(hash);
            if (arena != null) {
                // M5 part E: only an arena boss (Level 11) hashes these.
                hash.add(approaching ? 1 : 0);
                arena.addTo(hash);
            }
            if (lifts) {
                // M5 part H: only a boss that lifts (Level 14) hashes these.
                hash.add(lifted ? 1 : 0).add(liftPose).add(liftEndTicks);
                for (boolean stay : stays) {
                    hash.add(stay ? 1 : 0);
                }
            }
            if (breach != null) {
                // M7 part H: only a breaching boss (Level 21) hashes these.
                breach.addTo(hash);
            }
            if (coil != null) {
                // M8 part H: only a coiled boss (Level 28) hashes these.
                coil.addTo(hash);
            }
            if (stride != null) {
                // M7 part F: only a walking boss (Level 19) hashes these.
                stride.addTo(hash);
                hatches.addTo(hash);
            }
            // Round 40: only a boss with aim turns hashes these; M5 part H: only a boss that sweeps (Level 14) these.
            aimTurns.addTo(hash);
            sweeps.addTo(hash);
        }
    }

    double x() {
        return x;
    }

    /** M5 part E: its arena (lanes, slams and surfacing); null for any other boss. */
    SlamArena slamArena() {
        return arena;
    }

    double y() {
        return y;
    }

    public SetPieceSpec spec() {
        return spec;
    }

    /** Its enemy's slug ({@code leviathan}): the sprites and the death effect go by it. */
    public String slug() {
        return spec.slug();
    }

    /** Whether it is on screen in a pass (not before, between or after its passes, nor once destroyed). */
    public boolean present() {
        return present;
    }

    /**
     * M5 part E: whether an arena boss is scrolling in with the ground before it arrives: drawn (on
     * the water, with its platform) but not {@link #present()}: no bar, no hits, no targets.
     */
    public boolean approaching() {
        return approaching;
    }

    /** M5 part E: an arena boss's lanes, slams and surfacing; empty for any other. */
    public java.util.Optional<SlamArena> arena() {
        return arenaView;
    }

    /** M7 part F: a walking boss's passes, plate, vents and flame cone; empty for any other. */
    public java.util.Optional<StrideArena> stride() {
        return strideView;
    }

    /** M7 part H: a breaching boss's breach cycle, ridge and suction; empty for any other. */
    public java.util.Optional<BreachArena> breach() {
        return breachView;
    }

    /** M8 part H: a coiled boss's body, wall and script; empty for any other. */
    public java.util.Optional<CoilArena> coil() {
        return coilView;
    }

    /**
     * M5 part H (design/enemies/bosses/siege-spire; decision H4 = a): whether an anchored boss has left
     * its anchor (from its lift's start on): the stump stands at ({@link #stumpX()}, {@link #stumpY()}).
     */
    public boolean lifted() {
        return lifted;
    }

    /** M5 part H: whether the lift runs now (the glide off the anchor; invulnerable, holding its fire). */
    public boolean lifting() {
        return lifted && moveTicks >= 0;
    }

    /**
     * M5 part H: how far the lift's glide is, between the previous and the current step ({@code alpha}
     * in [0, 1]): 0 before it, 1 once the boss holds at its new station.
     */
    public double liftShare(double alpha) {
        if (!lifted) {
            return 0;
        }
        if (moveTicks < 0) {
            return 1;
        }
        int glide = SimStep.ticks(boss.phases().get(phase).move().orElseThrow().descendSeconds());
        return glide == 0 ? 1 : Math.clamp((moveTicks - 1 + alpha) / glide, 0.0, 1.0);
    }

    /** M5 part H: whether the lift ended in this step (the boss engages at its new station). */
    boolean liftEndedNow() {
        return liftEndTicks >= 0 && liftEndTicks == bossTicks;
    }

    /**
     * M5 part H: the stump's centre (where an anchored boss's centre lies on the ground), px from the
     * left; it scrolls with the ground after the kill.
     */
    public double stumpX() {
        return boss == null ? x : boss.x();
    }

    /** M5 part H: the stump's centre now, px up from the bottom edge (y up), with the ground. */
    public double stumpY() {
        return anchorGround - groundScroll;
    }

    /** M5 part H: whether part {@code p} stayed at the anchor in the lift (the stump's: drawn there, never hit). */
    public boolean partStays(int p) {
        return lifted && stays[p];
    }

    public boolean destroyed() {
        return destroyed;
    }

    /** Whether it ended its last pass alive. */
    public boolean escaped() {
        return escaped;
    }

    /** The index of the pass it flies (or will fly next) in {@link SetPieceSpec#passes()}. */
    public int pass() {
        return pass;
    }

    /** The name of that pass ({@code cross}, {@code descend}); empty after the last. */
    public String passName() {
        return pass < spec.passes().size() ? spec.passes().get(pass).name() : "";
    }

    /** The layer it flies on now: {@code high-air} out of reach, {@code air} once descended. */
    public Layer layer() {
        return layer;
    }

    /** Whether it is on the player's layer: its guns fire and its body collides. */
    public boolean onPlane() {
        return onPlane;
    }

    /** Where it faces, radians clockwise from straight down the screen, as {@link Enemy#facing()}; fixed per pass. */
    public double facing() {
        return present && boss == null ? spec.passes().get(pass).headingRadians() : 0;
    }

    /** Its boss script; empty for a set piece flying passes. */
    public java.util.Optional<BossSpec> boss() {
        return spec.boss();
    }

    /** Seconds since a boss arrived (its bar appeared); negative before. */
    public double bossSeconds() {
        return bossTicks * SimStep.SECONDS;
    }

    /** Steps since a boss arrived; -1 before. */
    int bossTicks() {
        return bossTicks;
    }

    /** Whether a boss has settled after its descent (it takes damage and attacks). */
    public boolean settled() {
        return swayTicks >= 0;
    }

    /** Steps since a boss settled; -1 before. */
    int swayTicks() {
        return swayTicks;
    }

    /** A boss's phase, 0 for its first. */
    public int phase() {
        return phase;
    }

    /** The seconds from a boss's arrival to its kill; negative while it lives (or before it arrived). */
    public double killSeconds() {
        return killTicks < 0 ? -1 : killTicks * SimStep.SECONDS;
    }

    /** The boss bar: its parts' remaining HP as a share of their total, 0–1 (fire-only parts left out). */
    public double barShare() {
        double left = 0;
        for (int p = 0; p < partHp.length; p++) {
            left += outOfBar[p] ? 0 : Math.max(0, partHp[p]);
        }
        return left / maxHp;
    }

    /** A boss's neck segments, all chains in order. */
    public int segmentCount() {
        return chains.segmentCount();
    }

    /** Neck segment {@code i}'s centre offset from its centre, px to the right. */
    public double segmentOffsetX(int i) {
        return chains.segmentOffsetX(i);
    }

    /** Neck segment {@code i}'s centre offset from its centre, px up. */
    public double segmentOffsetY(int i) {
        return chains.segmentOffsetY(i);
    }

    /** The chain neck segment {@code i} belongs to. */
    public int segmentChain(int i) {
        return chains.segmentChain(i);
    }

    /** Chain {@code c}'s angle at its piece {@code k} (0 at the anchor; its segments, then its tip), radians counter-clockwise. */
    public double chainAngle(int c, int k) {
        return chains.angle(c, k);
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    /** How high above the player's layer it flies: 1 on {@code high-air}, 0 on {@code air}, in between while it descends or rises. */
    public double altitude(double alpha) {
        return prevAltitude + (altitude - prevAltitude) * alpha;
    }

    public int partCount() {
        return partHp.length;
    }

    /** Whether part {@code p} is destroyed: it no longer fires or takes hits and is drawn wrecked. */
    public boolean partWrecked(int p) {
        return partHp[p] <= 0;
    }

    /** Part {@code p}'s HP left (at most its spec's, 0 or less once wrecked). */
    public double partHp(int p) {
        return partHp[p];
    }

    /** Simulation steps since part {@code p} was last hit, for its hit flash; {@link Integer#MAX_VALUE} before. */
    public int partTicksSinceHit(int p) {
        return partTicksSinceHit[p];
    }

    /** Part {@code p}'s centre on the screen now: its offset turned with the heading. */
    public double partX(int p) {
        return x + partOffsetX(p);
    }

    /** Part {@code p}'s centre on the screen now (y up). */
    public double partY(int p) {
        return y + partOffsetY(p);
    }

    /**
     * Part {@code p}'s offset from the centre on the screen, px to the right: its data offset turned
     * with the pass's heading (fixed per pass), so {@code renderX(alpha) + partOffsetX(p)} draws it.
     * A boss's is its pose's offset, grown by the high-air scale with its altitude ({@link #scale()}):
     * already the screen offset, not to be scaled again.
     */
    public double partOffsetX(int p) {
        if (lifted && stays[p]) {
            // M5 part H: a part of the stump stays at the anchor, in the pose it lifted from.
            return stumpX() + poseDx[liftPose][p] - x;
        }
        if (chains.atTip(p)) {
            return chains.tipX(p);
        }
        if (stride != null) {
            // M7 part F: a walking boss's offsets turn with its heading.
            return stride.turnX(poseDx[pose][p], poseDy[pose][p]);
        }
        if (breach != null && breach.ventOf(p) >= 0) {
            // M7 part H, step H3: a spine vent where it rose along the ridge.
            return breach.ventX(breach.ventOf(p)) - x;
        }
        if (coil != null && coil.implant(p) >= 0) {
            // M8 part H: an implant rides its coil segment.
            return coil.implantX(coil.implant(p)) - x;
        }
        if (boss != null) {
            return poseDx[pose][p] * scale();
        }
        PartSpec part = spec.parts().get(p);
        return part.dx() * cos + part.dy() * sin;
    }

    /** Part {@code p}'s offset from the centre on the screen, px up. */
    public double partOffsetY(int p) {
        if (lifted && stays[p]) {
            return stumpY() + poseDy[liftPose][p] - y;
        }
        if (chains.atTip(p)) {
            return chains.tipY(p);
        }
        if (stride != null) {
            return stride.turnY(poseDx[pose][p], poseDy[pose][p]);
        }
        if (breach != null && breach.ventOf(p) >= 0) {
            return breach.ventY(breach.ventOf(p)) - y;
        }
        if (coil != null && coil.implant(p) >= 0) {
            return coil.implantY(coil.implant(p)) - y;
        }
        if (boss != null) {
            return poseDy[pose][p] * scale();
        }
        PartSpec part = spec.parts().get(p);
        return -part.dx() * sin + part.dy() * cos;
    }

    /** A boss's size factor now: {@link BossSpec#HIGH_AIR_SCALE} on high air, 1 on the play plane, between them as it descends. */
    public double scale() {
        return 1 + (BossSpec.HIGH_AIR_SCALE - 1) * altitude;
    }

    /** Its armoured body's hit box now: a boss's pose's, else its spec's. */
    public Hitbox body() {
        return boss == null ? spec.body() : poseBody[pose];
    }

    /** How a boss moves now: its entrance, holding, or a phase's descent or turn. */
    public Motion motion() {
        if (boss == null || !present || destroyed || bossTicks < 0) {
            return Motion.NONE;
        }
        if (moveTicks >= 0) {
            int descend =
                    SimStep.ticks(boss.phases().get(phase).move().orElseThrow().descendSeconds());
            return moveTicks < descend ? Motion.DESCEND : Motion.TURN;
        }
        return swayTicks < 0 ? Motion.PASS : Motion.HOLD;
    }

    /** A boss's pose: an index into {@link BossSpec#poses()} (0, the arrival pose, without poses); it switches half-way through a turn. */
    public int pose() {
        return pose;
    }

    /** The pose a move turns it from (its pose when it holds). */
    public int turnFromPose() {
        return fromPose;
    }

    /** The pose a move turns it into (its pose when it holds). */
    public int turnToPose() {
        return moveTicks >= 0 ? boss.phases().get(phase).move().orElseThrow().pose() : pose;
    }

    /** The name of its pose; empty without poses. */
    public String poseName() {
        return boss == null || boss.poses().isEmpty()
                ? ""
                : boss.poses().get(pose).name();
    }

    /**
     * How far through its turn it is between the previous and the current step: 0 before the turn
     * (and when not moving), 1 at its end, between them the turn's frames.
     */
    public double turnShare(double alpha) {
        if (moveTicks < 0) {
            return 0;
        }
        BossSpec.Move move = boss.phases().get(phase).move().orElseThrow();
        int descend = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        if (turnSteps == 0) {
            return moveTicks >= descend ? 1 : 0;
        }
        return Math.clamp((moveTicks - 1 + alpha - descend) / turnSteps, 0, 1);
    }

    /** Whether its phases run (from its arrival or its settle). */
    public boolean engaged() {
        return engaged;
    }

    /** Whether phase {@code phase} ended on its timer (a timeout) rather than on its parts. */
    public boolean endedOnTimeout(int phase) {
        return (timeouts & (1L << phase)) != 0;
    }

    /** Seconds since the current phase engaged (after its move): its timeout's clock. */
    public double phaseSeconds() {
        return phaseTicks * SimStep.SECONDS;
    }

    /** Whether every part is wrecked. */
    boolean allWrecked() {
        for (int p = 0; p < partHp.length; p++) {
            if (partHp[p] > 0 && !armoured[p]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return spec.slug() + " pass " + pass + " at " + x + ", " + y + " parts " + Arrays.toString(partHp);
    }
}
