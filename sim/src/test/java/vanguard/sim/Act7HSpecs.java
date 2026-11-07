package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M11 part H, step H1a: a test Choir Heart (design/enemies/bosses/choir-heart) on the built engine with step H1a's
 * mechanics: two membranes (phase 1), four wall nodes riding the arena walls (phase 2, opening 4 s with an armour
 * patch: the closing), the core (phase 3, opening 4 s with an armour patch: the opening); the heartbeat ring every 3 s;
 * the walls (a 320 px corridor), the drift (60 px/s, |θ| ≤ 15°, one reversal at the second node or 25 s) and the phase
 * checkpoints; its level (an arena that holds while it lives).
 */
final class Act7HSpecs {
    private Act7HSpecs() {}

    static final int CORE = 0;
    static final int M1 = 1;
    static final int M2 = 2;
    /** The nodes: the left wall's upper and lower, the right wall's upper and lower. */
    static final int N_LU = 3;

    static final int N_LL = 4;
    static final int N_RU = 5;
    static final int N_RL = 6;
    static final List<Integer> MEMBRANES = List.of(M1, M2);
    static final List<Integer> NODES = List.of(N_LU, N_LL, N_RU, N_RL);

    static final double ARRIVE = 3;
    static final double ARENA_END = ARRIVE + 1;
    static final double END = ARENA_END + 30;
    static final double X = 240;
    /** The Heart's centre: y 100 below the top edge. */
    static final double Y = PlayField.HEIGHT - 100;
    /** The nodes' heights: 220 and 400 below the top edge (the doc's), y up. */
    static final double NODE_UPPER = PlayField.HEIGHT - 220;

    static final double NODE_LOWER = PlayField.HEIGHT - 400;

    static final ArenaWallsSpec WALLS = new ArenaWallsSpec(
            1,
            320,
            1.0,
            3.0,
            12,
            0.5,
            20,
            40,
            List.of(
                    new ArenaWallsSpec.Node(N_LU, true, NODE_UPPER),
                    new ArenaWallsSpec.Node(N_LL, true, NODE_LOWER),
                    new ArenaWallsSpec.Node(N_RU, false, NODE_UPPER),
                    new ArenaWallsSpec.Node(N_RL, false, NODE_LOWER)),
            10,
            28,
            0.12,
            0.5);

    static final PhaseDriftSpec DRIFT = new PhaseDriftSpec(
            1,
            60,
            List.of(
                    new HazardSpec.Bend(0, 0),
                    new HazardSpec.Bend(6, 12),
                    new HazardSpec.Bend(14, -10),
                    new HazardSpec.Bend(24, 15),
                    new HazardSpec.Bend(34, -8)),
            Optional.of(new PhaseDriftSpec.Reversal(2, 25, 3, 2)),
            1);

    private static PartSpec part(String name, double dx, double dy, Hitbox box, double hp, boolean vital) {
        return new PartSpec(name, dx, dy, box, hp, vital, 10, Optional.empty(), 0, 1);
    }

    static List<PartSpec> parts() {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(part("core", 0, -50, new Hitbox(56, 56), 6600, true));
        parts.add(part("m1", -90, -60, new Hitbox(40, 40), 1050, false));
        parts.add(part("m2", 90, -60, new Hitbox(40, 40), 1050, false));
        parts.add(part("n-lu", 0, 0, new Hitbox(36, 36), 1750, false));
        parts.add(part("n-ll", 0, 0, new Hitbox(36, 36), 1750, false));
        parts.add(part("n-ru", 0, 0, new Hitbox(36, 36), 1750, false));
        parts.add(part("n-rl", 0, 0, new Hitbox(36, 36), 1750, false));
        return parts;
    }

    static BossSpec.Phase phase(String name, List<Integer> until, List<Integer> exposes) {
        return new BossSpec.Phase(
                name,
                until,
                0,
                List.of(0),
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

    /** The Heart with or without its walls, drift and phase checkpoints (hard has none of the checkpoints). */
    static BossSpec boss(Optional<ArenaWallsSpec> walls, Optional<PhaseDriftSpec> drift, boolean checkpoints) {
        BossSpec.Attack heartbeat = new BossSpec.Attack(
                "heartbeat",
                BossSpec.Pattern.RING,
                EnemyGun.aimed(3.0, 0, 1, 100, 4, false),
                0,
                false,
                12,
                0,
                0,
                0,
                List.of(CORE));
        return BossSpec.builder(
                        ARRIVE,
                        X,
                        Y,
                        100,
                        0,
                        8,
                        Layer.AIR,
                        false,
                        "CHOIR HEART",
                        160,
                        List.of(),
                        List.of(heartbeat),
                        List.of(
                                phase("The membranes", MEMBRANES, List.of()),
                                phase("The arena", NODES, NODES),
                                phase("The core", List.of(CORE), List.of(CORE))))
                .openings(List.of(
                        new PhaseOpening(1, 4, Optional.of(PickupType.ARMOUR_PATCH), 0, -120),
                        new PhaseOpening(2, 4, Optional.of(PickupType.ARMOUR_PATCH), 0, -120)))
                .armoured(List.of())
                .walls(walls)
                .drift(drift)
                .phaseCheckpoints(checkpoints)
                .build();
    }

    static SetPieceSpec heart(BossSpec boss) {
        return new SetPieceSpec(
                "choir-heart",
                new Hitbox(220, 160),
                new Hitbox(160, 100),
                25,
                parts(),
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    static final SetPieceSpec HEART = heart(boss(Optional.of(WALLS), Optional.of(DRIFT), true));

    /** 3 s at 160 px/s, the arena (held while the boss lives, 8 s after its K), 30 s at 160 after it. */
    static LevelScript level(SetPieceSpec heart) {
        return LevelScript.builder(48, 6)
                .sections(List.of(
                        new Section(ARRIVE, 160),
                        new Section(ARENA_END, 0, true, Section.TIMED_HALT_RAMP_SECONDS, 0, 8),
                        new Section(END, 160)))
                .setPieces(List.of(heart))
                .build();
    }

    static Sortie sortie(SetPieceSpec heart) {
        return new Sortie(1, TestSpecs.LOADOUT, level(heart), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    static Sortie invulnerable(SetPieceSpec heart) {
        return new Sortie(
                1, TestSpecs.LOADOUT, level(heart), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Kills part {@code p} of the sortie's boss as a hit would (its bounty paid); false while it is untouchable. */
    static boolean kill(Sortie sortie, int p) {
        SetPiece boss = sortie.setPiece(0);
        if (boss.damagePartFlat(p, boss.partHp(p) + 100_000)) {
            sortie.objectivePasses.wreck(0, p);
            return true;
        }
        return false;
    }

    /** Steps until the boss is engaged (at most 15 s); the ship holds still. */
    static void untilEngaged(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(15) && !sortie.setPiece(0).engaged(); i++) {
            sortie.step(0);
        }
    }

    /** Steps (the ship holding still) until the boss's phase is {@code phase} (at most 15 s). */
    static void untilPhase(Sortie sortie, int phase) {
        for (int i = 0; i < SimStep.ticks(15) && sortie.setPiece(0).phase() != phase; i++) {
            sortie.step(0);
        }
    }

    /** From the level start into phase 2 (the arena): the boss engaged, both membranes killed. */
    static void intoArena(Sortie sortie) {
        untilEngaged(sortie);
        kill(sortie, M1);
        kill(sortie, M2);
        untilPhase(sortie, 1);
    }

    /** Steps for {@code seconds} with the ship holding still. */
    static void steps(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }
}
