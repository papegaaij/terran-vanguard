package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M9 part H, step H1a: a test Iron Sovereign (design/enemies/bosses/iron-sovereign; round 60 Q7 a) at the design's
 * medium numbers, its places turned to the simulation's (y up): the centre at (240, 380) (the screen's 160 px below
 * the top edge); the core 70×70 (5,000 HP, vital, armoured until phase 4); the outer ring (radius 200, 6°/s clockwise)
 * with 6 SAM batteries 30×30 (220 HP), the middle ring (140, 10°/s counter-clockwise) with 4 rail batteries 36×36
 * (330), the inner ring (85) stepping 120° clockwise in 4 s, then holding 4 s (a 0.4 s tell at its end), with 3 laser
 * batteries 32×32 (440); each ring's first battery straight down at rest. The fire arc 120° on the bearing clamped
 * to 60° either side of straight down; a 4 s spin-up from the halt; a locked ring grinds 2 s; the survivors ×1.25 then
 * ×1.5; cook-offs 0.15 s apart.
 *
 * <p>Attacks (step H1b): the SAM batteries the SAM Nest's salvo (2 homing diamonds 0.25 s apart, its 0.4 s hatch, at
 * most 8 alive, holding within 96 px), staggered, one a battery every 6 s; the rail batteries the Rail Bunker's line
 * ({@link Act5Specs#RAIL}: telegraph 1 s, 8 px, live 0.2 s, 10, holding within 100 px), staggered, one a battery every 5
 * s; the laser batteries the Sentinel Tower's arc sweep with a 300 px beam (telegraph 0.8 s, sweep 1.6 s) at each
 * inner-ring hold; the shut core a fan of 5 over 40° every 3.0 / 2.5 / 2.0 s by phase after a 0.5 s glow; the open core
 * (after a 3 s opening, its hex shield 80) a 2-arm spiral 4 s on (after a 0.6 s tell) and 2 s off, its rail every 6 s
 * below half its HP, and its 4 rim bays (40 px out, the first straight down) a Dart stream of 4 (0.4 s apart, ±15°,
 * at most 16 alive) one bay every 5 s, the first 1 s after the opening, after a 0.5 s tell, skipping a bay within 72 px
 * of the ship. Phases "Three rings", "Two rings", "One ring" end on 1, 2 and 3 locked rings, "The core" on the core.
 */
final class Act5HSpecs {
    private Act5HSpecs() {}

    static final int CORE = 0;
    static final List<Integer> SAMS = List.of(1, 2, 3, 4, 5, 6);
    static final List<Integer> RAILS = List.of(7, 8, 9, 10);
    static final List<Integer> LASERS = List.of(11, 12, 13);

    static final int OUTER = 0;
    static final int MIDDLE = 1;
    static final int INNER = 2;

    static final int SAM = 0;
    static final int RAIL = 1;
    static final int LASER = 2;
    static final int FAN_1 = 3;
    static final int FAN_2 = 4;
    static final int FAN_3 = 5;
    static final int SPIRAL = 6;
    static final int CORE_RAIL = 7;

    static final double ARRIVE = 3;
    static final double X = 240;
    static final double Y = 380;
    static final double SPIN_UP = 4;
    static final double SAM_HP = 220;
    static final double RAIL_HP = 330;
    static final double LASER_HP = 440;
    static final double CORE_HP = 5000;
    static final double TOTAL_HP = 6 * SAM_HP + 4 * RAIL_HP + 3 * LASER_HP + CORE_HP;

    static final EnemySpec.Sweep SWEEP = new EnemySpec.Sweep(Math.toRadians(120), 1.6, 0.8, 300, 6, 4.5, 1.0, 8, 0, 0);

    /** The core's hex shield: 80, the shared refill (after 2 s at 50 % a second), the breaker's 3 s hold. */
    static final EnemySpec.Shield CORE_SHIELD = new EnemySpec.Shield(80, 2.0, 0.5, 3.0);

    /** The bays' Dart streams: 4 every 5 s, 0.4 s apart, the first 1 s in; ±15°; straight 0.4 s; 16 alive. */
    static final EnemySpec.Launcher BAY_DARTS =
            new EnemySpec.Launcher(Act5CSpecs.DART, 4, 5, 0.4, 1, 240, Math.toRadians(30), 0.4, 16);

    /** The core block: the glow 0.5 s, the opening 3 s, the shield, the spiral 0.6 + 4 + 2 s, the rail below half. */
    static final RingSpec.Core CORE_BLOCK = new RingSpec.Core(
            CORE,
            0.5,
            3,
            Optional.of(CORE_SHIELD),
            SPIRAL,
            4,
            2,
            0.6,
            CORE_RAIL,
            0.5,
            Optional.of(new RingSpec.Bays(4, 40, 0, BAY_DARTS, 0.5, 72)));

    /** The rings at the given arc and speed factor (easy 90° / hard 150° and ×1.2: the content's difficulty). */
    static RingSpec rings(double arcDegrees, double speed) {
        return new RingSpec(
                        List.of(
                                new RingSpec.Ring(
                                        "outer", 200, 1, Math.toRadians(6) * speed, 0, SAMS, SAM, Optional.empty()),
                                new RingSpec.Ring(
                                        "middle",
                                        140,
                                        -1,
                                        Math.toRadians(10) * speed,
                                        0,
                                        RAILS,
                                        RAIL,
                                        Optional.empty()),
                                new RingSpec.Ring(
                                        "inner",
                                        85,
                                        1,
                                        0,
                                        0,
                                        LASERS,
                                        LASER,
                                        Optional.of(new RingSpec.Step(Math.toRadians(120), 4 / speed, 4, 0.4)))),
                        Math.toRadians(arcDegrees),
                        new BossSpec.Clamp(Math.toRadians(-60), Math.toRadians(60)),
                        SPIN_UP,
                        2,
                        List.of(1.0, 1.25, 1.5),
                        List.of(1, 2, 3, 0),
                        0.15)
                .withCore(Optional.of(CORE_BLOCK));
    }

    private static BossSpec.Phase phase(String name, List<Integer> attacks, List<Integer> exposes) {
        return new BossSpec.Phase(
                name,
                List.of(),
                0,
                attacks,
                false,
                Optional.empty(),
                exposes,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                BossSpec.PHASE_DELAY_SECONDS,
                Optional.empty(),
                Optional.empty(),
                BossSpec.PhaseArena.NONE);
    }

    private static PartSpec part(String name, double radius, double degrees, Hitbox box, double hp, int bounty) {
        double a = Math.toRadians(degrees);
        return new PartSpec(
                name, -radius * Math.sin(a), -radius * Math.cos(a), box, hp, false, bounty, Optional.empty(), 0, 1);
    }

    /** The test Sovereign; without {@code attacks} it fires nothing (the geometry's tests). */
    static SetPieceSpec sovereign(boolean attacks, RingSpec rings) {
        return sovereign(attacks, rings, 6);
    }

    /** As {@link #sovereign(boolean, RingSpec)}, a SAM battery's salvo every {@code samSeconds} (step H1b: the cap). */
    static SetPieceSpec sovereign(boolean attacks, RingSpec rings, double samSeconds) {
        return sovereign(attacks ? List.of(SAM, RAIL, LASER) : List.of(), attacks, rings, samSeconds);
    }

    /** Step H1b: the test Sovereign whose ring phases fire only the laser sweeps (Rook's wedge test). */
    static final SetPieceSpec LASERS_ONLY = sovereign(List.of(LASER), false, rings(120, 1), 6);

    /**
     * The test Sovereign, its ring phases firing {@code batteries} (and the shut core's fans and the open core's
     * attacks when {@code core}).
     */
    private static SetPieceSpec sovereign(List<Integer> batteries, boolean core, RingSpec rings, double samSeconds) {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("core", 0, 0, new Hitbox(70, 70), CORE_HP, true, 379, Optional.empty(), 0, 1));
        for (int k = 0; k < 6; k++) {
            parts.add(part("sam-" + (k + 1), 200, 60 * k, new Hitbox(30, 30), SAM_HP, 6));
        }
        for (int k = 0; k < 4; k++) {
            parts.add(part("rail-" + (k + 1), 140, 90 * k, new Hitbox(36, 36), RAIL_HP, 6));
        }
        for (int k = 0; k < 3; k++) {
            parts.add(part("laser-" + (k + 1), 85, 120 * k, new Hitbox(32, 32), LASER_HP, 6));
        }
        EnemyGun sam = Act5BSpecs.samGun(samSeconds, 2, 8, 96, true);
        EnemyGun rail = EnemyGun.aimed(5, 0, 1, 300, 10, false);
        EnemyGun sweep = EnemyGun.aimed(4.5, 0, 1, 1, 8, false);
        List<BossSpec.Attack> list = new ArrayList<>();
        list.add(new BossSpec.Attack(
                "sam", BossSpec.Pattern.AIMED, sam, 0.25, false, 1, 0, 0, 0, SAMS, true, Optional.empty()));
        list.add(new BossSpec.Attack(
                        "rail", BossSpec.Pattern.AIMED, rail, 0, false, 1, 0, 0, 0, RAILS, true, Optional.empty())
                .withLine(Act5Specs.RAIL));
        list.add(new BossSpec.Attack(
                "laser", BossSpec.Pattern.SWEEP, sweep, 0, false, 1, 0, 0, 0, LASERS, false, Optional.of(SWEEP)));
        double[] fans = {3.0, 2.5, 2.0};
        for (int f = 0; f < 3; f++) {
            EnemyGun fan = new EnemyGun(
                    fans[f],
                    0,
                    1,
                    150,
                    4,
                    false,
                    5,
                    Math.toRadians(40),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY);
            list.add(new BossSpec.Attack(
                    "core-fan-" + (f + 1), BossSpec.Pattern.FAN, fan, 0, false, 5, 0, 0, 0, List.of(CORE)));
        }
        list.add(new BossSpec.Attack(
                "core-spiral",
                BossSpec.Pattern.SPIRAL,
                EnemyGun.aimed(0.2, 0, 1, 130, 4, false),
                0,
                false,
                1,
                2,
                Math.toRadians(72),
                4,
                List.of(CORE)));
        list.add(new BossSpec.Attack(
                        "core-rail",
                        BossSpec.Pattern.AIMED,
                        EnemyGun.aimed(6, 0, 1, 300, 10, false),
                        0,
                        false,
                        1,
                        0,
                        0,
                        0,
                        List.of(CORE))
                .withLine(Act5Specs.RAIL));
        List<Integer> none = List.of();
        List<Layer> layers = new ArrayList<>();
        for (int p = 0; p < parts.size(); p++) {
            layers.add(Layer.GROUND);
        }
        BossSpec boss = BossSpec.builder(
                        ARRIVE,
                        X,
                        Y,
                        0,
                        0,
                        1,
                        Layer.GROUND,
                        false,
                        "TEST SOVEREIGN",
                        150,
                        List.of(),
                        list,
                        List.of(
                                phase("Three rings", with(batteries, core ? FAN_1 : -1), none),
                                phase("Two rings", with(batteries, core ? FAN_2 : -1), none),
                                phase("One ring", with(batteries, core ? FAN_3 : -1), none),
                                phase("The core", core ? List.of(SPIRAL, CORE_RAIL) : none, List.of(CORE))))
                .deathSeconds(5)
                .arena(Optional.of(new BossSpec.Arena(BossSpec.Lanes.NONE, null, layers, List.of())))
                .build()
                .withRings(Optional.of(rings));
        return new SetPieceSpec(
                "test-sovereign",
                new Hitbox(440, 440),
                new Hitbox(70, 70),
                10,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    private static List<Integer> with(List<Integer> attacks, int attack) {
        List<Integer> all = new ArrayList<>(attacks);
        if (attack >= 0) {
            all.add(attack);
        }
        return all;
    }

    /** The test Sovereign at medium, firing. */
    static final SetPieceSpec SOVEREIGN = sovereign(true, rings(120, 1));

    /** The test Sovereign at medium, silent. */
    static final SetPieceSpec QUIET = sovereign(false, rings(120, 1));

    /** The halted arena: 3 s at 120 px/s, the arena (speed 0) to 4 s, 20 s at 120 after it. */
    static LevelScript level(SetPieceSpec sovereign) {
        return level(sovereign, List.of());
    }

    /** As {@link #level(SetPieceSpec)}, with its radio. */
    static LevelScript level(SetPieceSpec sovereign, List<RadioCue> radio) {
        return LevelScript.builder(35, 4)
                .sections(List.of(
                        new Section(ARRIVE, 120), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 21, 120)))
                .setPieces(List.of(sovereign))
                .radio(radio)
                .build();
    }

    static Sortie sortie(SetPieceSpec sovereign, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level(sovereign), rules, TestSpecs.FULL_ARMOUR);
    }

    static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    static RingArena rings(Sortie sortie) {
        return boss(sortie).ring().orElseThrow();
    }

    /** Steps until the rings are up (the spin-up over). */
    static void untilUp(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(ARRIVE + 15) && rings(sortie).spinningUp(); i++) {
            sortie.step(0);
        }
        if (rings(sortie).spinningUp()) {
            throw new AssertionError("up within " + (ARRIVE + 15) + " s");
        }
    }

    /** Kills every battery of ring {@code ring} (flat hits, as the Lance's). */
    static void killRing(SetPiece boss, int ring) {
        List<Integer> parts = boss.ring().orElseThrow().spec().rings().get(ring).parts();
        for (int k = 0; k < parts.size(); k++) {
            int p = parts.get(k);
            boss.damagePartFlat(p, boss.partHp(p) + 1);
        }
    }

    /**
     * A sortie-free rig: the boss on its own, the scroll halted under it from its arrival, the ship where the test puts
     * it; it records what the boss does through its {@link BossActions}.
     */
    static class Rig implements BossActions {
        final SetPiece piece;
        int tick;
        double shipX = X;
        double shipY = 60;
        /** This step's events: {type ordinal, value} and where. */
        final List<SimEvents.Type> types = new ArrayList<>();

        final List<Integer> values = new ArrayList<>();
        final List<double[]> places = new ArrayList<>();
        /** This step's aimed shots and fans: where they left. */
        final List<double[]> shots = new ArrayList<>();
        /** This step's sweeps started: {attack, telegraph 1/0, x, y}. */
        final List<double[]> sweeps = new ArrayList<>();
        /** Step H1b: this step's bay launches: {x, y, angle}. */
        final List<double[]> launches = new ArrayList<>();

        final List<Integer> phases = new ArrayList<>();

        Rig(SetPieceSpec spec) {
            piece = new SetPiece(spec);
            piece.anchorAt(Y);
            tick = SimStep.ticks(ARRIVE) - 1;
        }

        void step() {
            types.clear();
            values.clear();
            places.clear();
            shots.clear();
            sweeps.clear();
            launches.clear();
            phases.clear();
            piece.ground(0, true);
            tick++;
            piece.update(tick);
            piece.act(shipX, shipY, this, true);
        }

        void steps(double seconds) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step();
            }
        }

        /** Steps until {@code type} happens (its values in that step), at most {@code seconds}. */
        List<Integer> until(SimEvents.Type type, double seconds) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step();
                List<Integer> found = values(type);
                if (!found.isEmpty()) {
                    return found;
                }
            }
            throw new AssertionError(type + " within " + seconds + " s");
        }

        List<Integer> values(SimEvents.Type type) {
            List<Integer> found = new ArrayList<>();
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i) == type) {
                    found.add(values.get(i));
                }
            }
            return found;
        }

        RingArena rings() {
            return piece.ring().orElseThrow();
        }

        @Override
        public void aimed(double x, double y, EnemyGun gun) {
            shots.add(new double[] {x, y});
        }

        @Override
        public void ring(double x, double y, int count, double speed, double damage) {
            shots.add(new double[] {x, y});
        }

        @Override
        public void bullet(double x, double y, double angle, double speed, double damage) {
            shots.add(new double[] {x, y});
        }

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {
            phases.add(phase);
        }

        @Override
        public void sweep(int a, boolean telegraph, double x, double y) {
            sweeps.add(new double[] {a, telegraph ? 1 : 0, x, y});
        }

        @Override
        public void bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {
            launches.add(new double[] {x, y, angle});
        }

        @Override
        public void ring(SimEvents.Type type, double x, double y, int value) {
            types.add(type);
            values.add(value);
            places.add(new double[] {x, y});
        }
    }
}
