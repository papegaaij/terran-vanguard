package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M8 part H, step H1: a test Abyssal Maw (design/enemies/bosses/abyssal-maw) shaped like the design's medium numbers,
 * the screen's places turned to the simulation's (y up): the head at (240, 445) (110×100, the skull ×0.5) with its
 * throat 40 px below its centre (×2 while the jaw is open); the gill sacs at (178, 460), (302, 460), (190, 412) and
 * (290, 412); the implants at the neck (110, 480) and on the left (40, 240) and right (440, 240) coil (×2 on the head's
 * HP); the coil 31 segments of 64 px, 56 apart, from beside the head along the top (y 522) to the top-left corner, down
 * the left edge (x 18), along the bottom (y 3) and up the right edge (x 462); its inner flanks x 50 / 430 and y 35, the
 * ship 12 px inside them. HP small so the tests reach each phase: the head 1,000, the sacs 150 each.
 *
 * <p>Phase 1 (until the sacs or the head below 70 %): the sacs' rings of 10 every 1.6 s after a 0.6 s swell, the
 * throat's fan of 5 over 40° every 5 s (a 0.6 s glow, the jaw open 3 s), an Eel Swarm every 10 s (at most one). Phase 2
 * (until below 30 %): rings every 2.4 s, the fan walls of 17 over 150° every 4 s after a 0.8 s glow, sweeps every 7 s
 * left and right (tell 1.2, swing 0.5 in 110 px, hold 0.8, back 0.8; 10 damage), the dive below 50 % (slide 2, quiet 4,
 * shadow 2, lunge 0.8, body 3, return 2; a 90 px column within 95–385; 10 damage), an Eel Swarm every 12 s. Phase 3:
 * the implants in turn (tell 1, open 4, a burst of 3 every 1.5 s after a 0.4 s arc), sweeps every 6 s left, right and
 * bottom (90 px), an Eel Swarm every 8 s (at most two), an armour patch as it begins. Easy: walls of 13 with two gaps;
 * hard: a 1.6 s shadow and the implants open 3 s.
 */
final class MawSpecs {
    private MawSpecs() {}

    static final int HEAD = 0;
    static final List<Integer> SACS = List.of(1, 2, 3, 4);
    static final List<Integer> IMPLANTS = List.of(5, 6, 7);
    static final int NECK = 5;
    static final int LEFT_IMPLANT = 6;
    static final int RIGHT_IMPLANT = 7;

    static final int RING_1 = 0;
    static final int THROAT_FAN = 1;
    static final int RING_2 = 2;
    static final int WALL = 3;
    static final int BURST = 4;

    static final double ARRIVE = 3;
    static final double HEAD_HP = 1000;
    static final double SAC_HP = 150;
    static final double REST_X = 240;
    static final double REST_Y = 445;
    static final double THROAT_DY = -40;

    /** The test's choices: the attacks (rings, fans, walls, bursts), the sweeps, the sheds, and the difficulty. */
    record Fit(boolean attacks, boolean sweeps, boolean sheds, boolean easy, boolean hard) {
        static final Fit ALL = new Fit(true, true, true, false, false);
        static final Fit QUIET = new Fit(false, false, false, false, false);

        Fit withAttacks() {
            return new Fit(true, sweeps, sheds, easy, hard);
        }

        Fit withSweeps() {
            return new Fit(attacks, true, sheds, easy, hard);
        }

        Fit withSheds() {
            return new Fit(attacks, sweeps, true, easy, hard);
        }

        Fit easyFit() {
            return new Fit(attacks, sweeps, sheds, true, false);
        }

        Fit hardFit() {
            return new Fit(attacks, sweeps, sheds, false, true);
        }
    }

    static final CoilSpec.Body BODY = new CoilSpec.Body(
            31,
            56,
            new Hitbox(64, 64),
            List.of(
                    new BossSpec.Point(185, 522),
                    new BossSpec.Point(18, 522),
                    new BossSpec.Point(18, 3),
                    new BossSpec.Point(462, 3),
                    new BossSpec.Point(462, 540)),
            90,
            50,
            430,
            35,
            PlayField.HEIGHT + 12,
            12);

    static final List<CoilSpec.ShedPoint> SHED_POINTS = List.of(
            new CoilSpec.ShedPoint(CoilSpec.Side.LEFT, 270),
            new CoilSpec.ShedPoint(CoilSpec.Side.BOTTOM, 240),
            new CoilSpec.ShedPoint(CoilSpec.Side.RIGHT, 270));

    private static BossSpec.Phase phase(
            String name, List<Integer> until, List<Integer> attacks, double below, Optional<PickupType> drop) {
        return new BossSpec.Phase(
                name,
                until,
                0,
                attacks,
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        Double.isNaN(below) ? List.of() : List.of(HEAD),
                        below,
                        BossSpec.Slamming.NONE,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        drop));
    }

    private static PartSpec part(String name, double dx, double dy, Hitbox box, double hp, int bounty, double times) {
        return new PartSpec(name, dx, dy, box, hp, false, bounty, Optional.empty(), 0, times);
    }

    static CoilSpec coil(Fit fit) {
        CoilSpec.Sweeps sweeps = fit.sweeps()
                ? new CoilSpec.Sweeps(
                        List.of(0.0, 7.0, 6.0),
                        List.of(
                                List.of(),
                                List.of(CoilSpec.Side.LEFT, CoilSpec.Side.RIGHT),
                                List.of(CoilSpec.Side.LEFT, CoilSpec.Side.RIGHT, CoilSpec.Side.BOTTOM)),
                        5,
                        fit.easy() ? 1.5 : fit.hard() ? 0.9 : 1.2,
                        0.5,
                        0.8,
                        0.8,
                        110,
                        90,
                        10,
                        new BossSpec.Clamp(120, 380),
                        new BossSpec.Clamp(140, 340))
                : CoilSpec.Sweeps.NONE;
        return new CoilSpec(
                BODY,
                6,
                5,
                SACS,
                0.6,
                new CoilSpec.Jaw(0.6, 3),
                Optional.of(new CoilSpec.Wall(WALL, 0.8, fit.easy() ? 2 : 1, 40, 90, 140)),
                sweeps,
                Optional.of(new CoilSpec.Dive(
                        1,
                        0.5,
                        2,
                        4,
                        fit.easy() ? 2.4 : fit.hard() ? 1.6 : 2.0,
                        0.8,
                        3,
                        3,
                        90,
                        new BossSpec.Clamp(95, 385),
                        10)),
                Optional.of(new CoilSpec.Implants(IMPLANTS, 2, BURST, 1, fit.easy() ? 5 : fit.hard() ? 3 : 4, 0.4)),
                fit.sheds()
                        ? Optional.of(new CoilSpec.Shed(
                                EelSwarmTest.eel(fit.hard() ? 10 : 8, List.of(), Optional.of(EelSwarmTest.SWAY)),
                                SHED_POINTS,
                                List.of(10.0, 12.0, 8.0),
                                List.of(1, 1, 2),
                                1))
                        : Optional.empty());
    }

    /** The test Maw. */
    static SetPieceSpec maw(Fit fit) {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("head", 0, 0, new Hitbox(110, 100), HEAD_HP, true, 297, Optional.empty(), 0, 0.5));
        parts.add(part("sac-1", -62, 15, new Hitbox(36, 36), SAC_HP, 12, 1));
        parts.add(part("sac-2", 62, 15, new Hitbox(36, 36), SAC_HP, 12, 1));
        parts.add(part("sac-3", -50, -33, new Hitbox(36, 36), SAC_HP, 12, 1));
        parts.add(part("sac-4", 50, -33, new Hitbox(36, 36), SAC_HP, 12, 1));
        parts.add(part("implant-neck", -130, 35, new Hitbox(40, 40), 1, 0, 2));
        parts.add(part("implant-left", -200, -205, new Hitbox(40, 40), 1, 0, 2));
        parts.add(part("implant-right", 200, -205, new Hitbox(40, 40), 1, 0, 2));
        EnemyGun ring1 = EnemyGun.aimed(1.6, 0, 1, 100, 4, false);
        EnemyGun ring2 = EnemyGun.aimed(2.4, 0, 1, 100, 4, false);
        EnemyGun fan = new EnemyGun(
                5, 0, 1, 130, 6, false, 5, Math.toRadians(40), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        int orbs = fit.easy() ? 13 : 17;
        EnemyGun wall = new EnemyGun(
                4, 0, 1, 110, 4, false, orbs, Math.toRadians(150), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        EnemyGun burst = EnemyGun.aimed(1.5, 0, fit.easy() ? 1 : 3, 140, 4, false);
        List<BossSpec.Attack> attacks = List.of(
                new BossSpec.Attack("ring-1", BossSpec.Pattern.RING, ring1, 0, false, 10, 0, 0, 0, SACS),
                new BossSpec.Attack("throat-fan", BossSpec.Pattern.FAN, fan, 0, false, 5, 0, 0, 0, List.of(HEAD)),
                new BossSpec.Attack("ring-2", BossSpec.Pattern.RING, ring2, 0, false, 10, 0, 0, 0, SACS),
                new BossSpec.Attack("wall", BossSpec.Pattern.FAN, wall, 0, false, orbs, 0, 0, 0, List.of(HEAD)),
                new BossSpec.Attack("burst", BossSpec.Pattern.AIMED, burst, 0.15, false, 1, 0, 0, 0, IMPLANTS));
        List<Layer> layers = new ArrayList<>();
        for (int p = 0; p < parts.size(); p++) {
            layers.add(Layer.SUB);
        }
        var arena = new BossSpec.Arena(
                BossSpec.Lanes.NONE,
                null,
                layers,
                List.of(new BossSpec.Spot("throat", HEAD, 0, THROAT_DY, new Hitbox(40, 30), 2, true)));
        BossSpec boss = BossSpec.builder(
                        ARRIVE,
                        REST_X,
                        REST_Y,
                        0,
                        0,
                        1,
                        Layer.AIR,
                        false,
                        "TEST MAW",
                        130,
                        List.of(),
                        attacks,
                        List.of(
                                phase(
                                        "Coiled",
                                        SACS,
                                        fit.attacks() ? List.of(RING_1, THROAT_FAN) : List.of(),
                                        0.7,
                                        Optional.empty()),
                                phase(
                                        "Tightening",
                                        List.of(),
                                        fit.attacks() ? List.of(RING_2, WALL) : List.of(),
                                        0.3,
                                        Optional.empty()),
                                phase(
                                        "Implants",
                                        List.of(),
                                        fit.attacks() ? List.of(BURST) : List.of(),
                                        Double.NaN,
                                        Optional.of(PickupType.ARMOUR_PATCH))))
                .deathSeconds(4)
                .arena(Optional.of(arena))
                .build()
                .withCoil(Optional.of(coil(fit)));
        return new SetPieceSpec(
                "test-maw",
                new Hitbox(150, 130),
                new Hitbox(110, 100),
                10,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    /** The halted arena: 3 s at 120 px/s, the arena (speed 0) to 4 s, 20 s at 120 after it. */
    static LevelScript level(Fit fit, List<RadioCue> radio) {
        return LevelScript.builder(28, 4)
                .sections(List.of(
                        new Section(ARRIVE, 120), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 21, 120)))
                .setPieces(List.of(maw(fit)))
                .radio(radio)
                .build();
    }

    static Sortie sortie(Fit fit, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level(fit, List.of()), rules, TestSpecs.FULL_ARMOUR);
    }

    static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    static CoilArena coil(Sortie sortie) {
        return boss(sortie).coil().orElseThrow();
    }

    /** Steps until the Maw is up (engaged). */
    static void untilUp(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(ARRIVE + 10) && !boss(sortie).engaged(); i++) {
            sortie.step(0);
        }
        if (!boss(sortie).engaged()) {
            throw new AssertionError("up within " + (ARRIVE + 10) + " s");
        }
    }

    /** Takes the head to {@code share} of its HP (a flat hit, as the Lance's). */
    static void headTo(Sortie sortie, double share) {
        SetPiece boss = boss(sortie);
        boss.damagePartFlat(HEAD, boss.partHp(HEAD) - share * HEAD_HP);
    }

    /** Steps until the boss is in {@code phase}, at most {@code seconds}. */
    static void untilPhase(Sortie sortie, int phase, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds) && boss(sortie).phase() < phase; i++) {
            sortie.step(0);
        }
        if (boss(sortie).phase() < phase) {
            throw new AssertionError("phase " + phase + " within " + seconds + " s");
        }
    }

    /** Steps until {@code type} happens (returns its values in that step), at most {@code seconds}. */
    static List<Integer> until(Sortie sortie, SimEvents.Type type, double seconds, int commands) {
        return BreachArenaTest.until(sortie, type, seconds, commands);
    }

    static List<Integer> values(Sortie sortie, SimEvents.Type type) {
        return BreachArenaTest.values(sortie, type);
    }

    static void steps(Sortie sortie, double seconds, int commands) {
        BreachArenaTest.steps(sortie, seconds, commands);
    }

    /** Flies the ship to (x, y) (as far as it gets). */
    static void steerTo(Sortie sortie, double x, double y) {
        BreachArenaTest.steerTo(sortie, x, y);
    }
}
