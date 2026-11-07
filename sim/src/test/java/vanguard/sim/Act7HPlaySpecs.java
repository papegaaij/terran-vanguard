package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M11 part H, step H1b: a test Choir Heart (design/enemies/bosses/choir-heart) with step H1b's mechanics on top of step
 * H1a's: six membranes as part windows singing the membranes' playbook (each its own song, each in any eight draws, two
 * at once from the half at most one releasing, the last one its own song and the Herald's by turns), the arena's
 * playbook (the Heart's line, the sweep, the wall spit, the pit brood from the walls, the spiral), the storm's (two at
 * once, never two lines, a line every third draw), the heartbeat, its last words at 0 s with the flash at K + 3.5 s, and
 * its level's closing gate (a transit timed from K, reversed, with the scene's beats).
 */
final class Act7HPlaySpecs {
    private Act7HPlaySpecs() {}

    static final int CORE = 0;
    static final int M1 = 1;
    static final int M2 = 2;
    static final int M3 = 3;
    static final int M4 = 4;
    static final int M5 = 5;
    static final int M6 = 6;
    static final int N_LU = 7;
    static final int N_LL = 8;
    static final int N_RU = 9;
    static final int N_RL = 10;
    static final int[] MEMBRANES = {M1, M2, M3, M4, M5, M6};
    static final int[] NODES = {N_LU, N_LL, N_RU, N_RL};

    // The attacks.
    static final int HEARTBEAT = 0;
    static final int HERALD = 1;
    static final int THORN = 2;
    static final int SPORE = 3;
    static final int SPIRAL = 4;
    static final int PAIR = 5;
    static final int LINE = 6;

    // Phase 1's moves.
    static final int SONG_HERALD = 0;
    static final int SONG_THORN = 1;
    static final int SONG_SPORES = 2;
    static final int SONG_SEEDS = 3;
    static final int SONG_SPIRAL = 4;
    static final int SONG_MOTES = 5;

    // Phase 2's moves.
    static final int ARENA_LINE = 0;
    static final int ARENA_SWEEP = 1;
    static final int ARENA_SPIT = 2;
    static final int ARENA_PITS = 3;
    static final int ARENA_SPIRAL = 4;

    /** The pit brood's places: a pit on each wall (dx sign the wall, 6 px in from its face), y up. */
    static final double PIT_LEFT_Y = PlayField.HEIGHT - 300;

    static final double PIT_RIGHT_Y = PlayField.HEIGHT - 340;

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
                    new ArenaWallsSpec.Node(N_LU, true, Act7HSpecs.NODE_UPPER),
                    new ArenaWallsSpec.Node(N_LL, true, Act7HSpecs.NODE_LOWER),
                    new ArenaWallsSpec.Node(N_RU, false, Act7HSpecs.NODE_UPPER),
                    new ArenaWallsSpec.Node(N_RL, false, Act7HSpecs.NODE_LOWER)),
            10,
            28,
            0.12,
            0.5);

    private static PartSpec part(String name, double dx, double dy, Hitbox box, double hp, boolean vital) {
        return new PartSpec(name, dx, dy, box, hp, vital, 10, Optional.empty(), 0, 1);
    }

    /** The parts: the membranes on the rim at the doc's places (the Heart's centre at y 100 below the top edge). */
    static List<PartSpec> parts() {
        Hitbox mouth = new Hitbox(40, 40);
        Hitbox node = new Hitbox(36, 36);
        List<PartSpec> parts = new ArrayList<>();
        parts.add(part("core", 0, -50, new Hitbox(56, 56), 6600, true));
        parts.add(part("m1", -150, -50, mouth, 1050, false));
        parts.add(part("m2", -90, -85, mouth, 1050, false));
        parts.add(part("m3", -30, -100, mouth, 1050, false));
        parts.add(part("m4", 30, -100, mouth, 1050, false));
        parts.add(part("m5", 90, -85, mouth, 1050, false));
        parts.add(part("m6", 150, -50, mouth, 1050, false));
        for (String name : List.of("n-lu", "n-ll", "n-ru", "n-rl")) {
            parts.add(part(name, 0, 0, node, 1750, false));
        }
        return parts;
    }

    private static EnemyGun fan(int count, double spreadDegrees, double speed, double damage) {
        return new EnemyGun(
                2,
                0,
                1,
                speed,
                damage,
                false,
                count,
                Math.toRadians(spreadDegrees),
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    /** The Spore Bomber's spore: armed after 0.5 s, its ring of 6 after 4 s. */
    static final EnemyGun SPORE_GUN = new EnemyGun(
            1.6,
            0,
            1,
            90,
            6,
            false,
            1,
            0,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Optional.of(new EnemyGun.MineSpec(0.5, 4, 0, 1, 6, 4, true, 1)));

    static List<BossSpec.Attack> attacks() {
        EnemyGun lance = EnemyGun.aimed(10, 0, 1, 600, 10, false);
        return List.of(
                new BossSpec.Attack(
                        "heartbeat",
                        BossSpec.Pattern.RING,
                        EnemyGun.aimed(3.0, 0, 1, 100, 4, false),
                        0,
                        false,
                        12,
                        0,
                        0,
                        0,
                        List.of(CORE)),
                new BossSpec.Attack(
                        "herald-song",
                        BossSpec.Pattern.FAN,
                        fan(16, 337.5, 100, 4),
                        0,
                        false,
                        16,
                        0,
                        0,
                        0,
                        List.of(M1)),
                new BossSpec.Attack(
                        "needler-thorns", BossSpec.Pattern.FAN, fan(3, 10, 160, 4), 0, false, 3, 0, 0, 0, List.of(M2)),
                new BossSpec.Attack("spore-drop", BossSpec.Pattern.AIMED, SPORE_GUN, 0, false, 1, 0, 0, 0, List.of(M3)),
                new BossSpec.Attack(
                        "choir-spiral",
                        BossSpec.Pattern.SPIRAL,
                        EnemyGun.aimed(0.25, 0, 1, 120, 4, false),
                        0,
                        false,
                        1,
                        3,
                        Math.toRadians(60),
                        3,
                        List.of(M5)),
                new BossSpec.Attack(
                        "pair", BossSpec.Pattern.FAN, fan(2, 8, 150, 4), 0, false, 2, 0, 0, 0, List.of(CORE)),
                new BossSpec.Attack("heart-line", BossSpec.Pattern.AIMED, lance, 0, false, 1, 0, 0, 0, List.of(CORE))
                        .withLine(new EnemySpec.LaserLine(1.2, 0.5, 20, 10, 10, 0, 0).withLockBefore(0.2)));
    }

    static final EnemySpec.Launcher SEEDS =
            new EnemySpec.Launcher(Act6ASpecs.air(10), 6, 5, 0.1, 0, 120, Math.toRadians(60), 0.5, 8);
    static final EnemySpec.Launcher MOTES =
            new EnemySpec.Launcher(Act6ASpecs.air(6), 8, 5, 0.1, 0, 140, Math.toRadians(40), 0.5, 8);
    static final EnemySpec.Launcher SKITTERS =
            new EnemySpec.Launcher(Act6ASpecs.air(20), 3, 5, 0.2, 0, 100, Math.toRadians(30), 0.5, 6);

    private static PhasePlaybook.Move brood(EnemySpec.Launcher launcher, PhasePlaybook.Bays bays) {
        return new PhasePlaybook.Move(PhasePlaybook.Kind.BROOD, 1, -1, 0, 0, 1, List.of(), Optional.of(bays));
    }

    private static List<Integer> list(int[] parts) {
        List<Integer> list = new ArrayList<>();
        for (int p : parts) {
            list.add(p);
        }
        return list;
    }

    /** Phase 1, the membranes' songs: each from its membrane, every one in any 8 draws, two at once from the half. */
    static PhasePlaybook membranes(boolean pairs) {
        PhasePlaybook.Bays here = new PhasePlaybook.Bays(SEEDS, List.of(new BossSpec.Offset(0, 0))).towardShip();
        PhasePlaybook.Bays motes = new PhasePlaybook.Bays(MOTES, List.of(new BossSpec.Offset(0, 0))).towardShip();
        List<PhasePlaybook.Move> moves = List.of(
                PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, HERALD, 2, 0.6)
                        .from(M1)
                        .covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, THORN, 3, 0.4)
                        .from(M2)
                        .covered(PAIR),
                PhasePlaybook.Move.spores(1, SPORE, 5, 0.2, 80).from(M3).covered(PAIR),
                brood(SEEDS, here).from(M4).covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0)
                        .from(M5)
                        .covered(PAIR),
                brood(MOTES, motes).every(3).from(M6).covered(PAIR));
        return new PhasePlaybook(0, list(MEMBRANES), 0.8, 0.9, 0.6, 0.5, moves)
                .withEnsemble(new PhasePlaybook.Ensemble(
                        8,
                        pairs ? Optional.of(new PhasePlaybook.Parallel(0.4, true, true, true)) : Optional.empty(),
                        List.of(SONG_HERALD)));
    }

    /** Phase 2, the arena: the line at least every third draw, the sweep, the wall spit, the pit brood, the spiral. */
    static PhasePlaybook arena() {
        PhasePlaybook.Bays pits = new PhasePlaybook.Bays(
                        SKITTERS, List.of(new BossSpec.Offset(-6, PIT_LEFT_Y), new BossSpec.Offset(6, PIT_RIGHT_Y)))
                .towardShip()
                .wallPits();
        List<PhasePlaybook.Move> moves = List.of(
                PhasePlaybook.Move.of(PhasePlaybook.Kind.LINE, 1, LINE, 1, 0).covered(PAIR),
                PhasePlaybook.Move.sweep(1, LINE, Math.toRadians(40)).covered(PAIR),
                PhasePlaybook.Move.wallSpit(1, PAIR, 4, 0.4, 120).covered(PAIR),
                brood(SKITTERS, pits).every(3).covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0)
                        .covered(PAIR));
        return new PhasePlaybook(1, List.of(CORE), 0.8, 0.9, 0.6, 0.5, moves).withLanceEvery(3);
    }

    /** Phase 3, the storm: two at once from six, never two lines, a line every third draw, no releases. */
    static PhasePlaybook storm() {
        List<PhasePlaybook.Move> moves = List.of(
                PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, HERALD, 2, 0.6)
                        .covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, THORN, 3, 0.4)
                        .covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0)
                        .covered(PAIR),
                PhasePlaybook.Move.of(PhasePlaybook.Kind.LINE, 1, LINE, 1, 0).covered(PAIR),
                PhasePlaybook.Move.sweep(1, LINE, Math.toRadians(40)).covered(PAIR),
                PhasePlaybook.Move.spores(1, SPORE, 5, 0.2, 80).covered(PAIR));
        return new PhasePlaybook(2, List.of(CORE), 0.8, 0.6, 0.5, 0.5, moves)
                .withLanceEvery(3)
                .withEnsemble(new PhasePlaybook.Ensemble(
                        0, Optional.of(new PhasePlaybook.Parallel(0.4, true, true, false)), List.of()));
    }

    static BossSpec.Phase phase(String name, int[] until, int[] exposes) {
        return Act7HSpecs.phase(name, list(until), list(exposes));
    }

    /** The Heart: its playbooks, part windows, heartbeat, walls, Rook's orders and last words (flash at 3.5 s). */
    static BossSpec boss(boolean pairs) {
        return BossSpec.builder(
                        Act7HSpecs.ARRIVE,
                        Act7HSpecs.X,
                        Act7HSpecs.Y,
                        100,
                        0,
                        8,
                        Layer.AIR,
                        false,
                        "CHOIR HEART",
                        160,
                        List.of(),
                        attacks(),
                        List.of(
                                phase("The membranes", MEMBRANES, new int[0]),
                                phase("The arena", NODES, NODES),
                                phase("The core", new int[] {CORE}, new int[] {CORE})))
                .openings(List.of(
                        new PhaseOpening(1, 4, Optional.of(PickupType.ARMOUR_PATCH), 0, -120),
                        new PhaseOpening(2, 4, Optional.of(PickupType.ARMOUR_PATCH), 0, -120)))
                .armoured(List.of())
                .playbooks(List.of(membranes(pairs), arena(), storm()))
                .partWindows(Optional.of(new PartWindowsSpec(list(MEMBRANES), 3, 3)))
                .heartbeat(HEARTBEAT)
                .walls(Optional.of(WALLS))
                .rookOrders(List.of(
                        new BossSpec.RookOrder(0, list(MEMBRANES), BossSpec.RookOrder.Pick.OPEN_WINDOW),
                        new BossSpec.RookOrder(1, list(NODES), BossSpec.RookOrder.Pick.OWN_SIDE),
                        new BossSpec.RookOrder(2, List.of(CORE))))
                .lastWords(Optional.of(new LastWordsSpec(0, 0.5).withFlashAt(3.5)))
                .deathSeconds(4)
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

    static final SetPieceSpec HEART = heart(boss(true));

    /** The closing gate, from K: the gate closing, the form at 12 s to (240, 120), the warp home, the near side, ... */
    static final double GATE_CLOSING = 10.5;

    static final double FORM = 12;
    static final double WARP = 29;
    static final double NEAR_SIDE = 30.5;
    static final double PLANE_OUT = 31;
    static final double FORM_X = 240;
    static final double FORM_Y = PlayField.HEIGHT - 420;

    static final VrellLevel.Transit GATE = new VrellLevel.Transit(FORM, WARP, 1.5)
            .withTo(FORM_X, FORM_Y, 1.5)
            .withReverse(true)
            .afterKill(List.of(
                    new VrellLevel.Transit.Beat(VrellLevel.Transit.Beat.Kind.GATE_CLOSING, GATE_CLOSING, 0),
                    new VrellLevel.Transit.Beat(VrellLevel.Transit.Beat.Kind.NEAR_SIDE, NEAR_SIDE, 0),
                    new VrellLevel.Transit.Beat(VrellLevel.Transit.Beat.Kind.PLANE_OUT, PLANE_OUT, 0),
                    new VrellLevel.Transit.Beat(VrellLevel.Transit.Beat.Kind.GLYPH_DARK, 35, 1),
                    new VrellLevel.Transit.Beat(VrellLevel.Transit.Beat.Kind.GLYPH_DARK, 36, 2)));

    /** 3 s at 160 px/s, the arena (held while the boss lives, 12 s after its K), then the race: 60 s at 300 px/s. */
    static LevelScript level(SetPieceSpec heart) {
        return LevelScript.builder(48, 6)
                .sections(List.of(
                        new Section(Act7HSpecs.ARRIVE, 160),
                        new Section(Act7HSpecs.ARENA_END, 0, true, Section.TIMED_HALT_RAMP_SECONDS, 0, 12),
                        new Section(Act7HSpecs.ARENA_END + 60, 300)))
                .setPieces(List.of(heart))
                .vrell(VrellLevel.NONE.withTransit(Optional.of(GATE)))
                .build();
    }

    static Sortie sortie(long seed, SetPieceSpec heart) {
        return new Sortie(seed, TestSpecs.LOADOUT, level(heart), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    static Sortie invulnerable(long seed, SetPieceSpec heart) {
        return new Sortie(
                seed, TestSpecs.LOADOUT, level(heart), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Kills the given parts of the sortie's boss as hits would (closed windows too: the kill is flat). */
    static void kill(Sortie sortie, int... parts) {
        for (int p : parts) {
            Act7HSpecs.kill(sortie, p);
        }
    }
}
