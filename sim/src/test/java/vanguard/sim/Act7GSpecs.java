package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M11 part G, step G1 (design/campaign Level 49, the echoes): fixtures for the rush's per-gate arenas and the echoes'
 * keys. A rush of three gates: the first crawling at 20 px/s under a descending boss, the second crawling at 20 px/s
 * under a breaching boss, the third halted under an anchored arena boss.
 */
final class Act7GSpecs {
    private Act7GSpecs() {}

    /** The gates' crawl, px/s. */
    static final double CRAWL = 20;

    /** 100 px/s to 10 s; gate 0 to 12 s (crawl), 120 to 20 s, gate 1 to 30 s (crawl), 120 to 38 s, gate 2 to 40 s. */
    static final List<Section> SECTIONS = List.of(
            new Section(10, 100),
            new Section(12, CRAWL, true, 0, 0, 0, true),
            new Section(20, 120, false, 2, 2),
            new Section(30, CRAWL, true, 0, 0, 0, true),
            new Section(38, 120, false, 2, 2),
            new Section(40, 0, true),
            new Section(55, 120, false, 2, 2));

    /** {@code piece} with its boss arriving at {@code arrive} s. */
    static SetPieceSpec at(SetPieceSpec piece, double arrive) {
        return new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                piece.boss().map(boss -> boss.withArriveSeconds(arrive)));
    }

    /** {@code piece} as an echo ({@code echo}). */
    static SetPieceSpec echo(SetPieceSpec piece, EchoSpec echo) {
        return new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                piece.boss().map(boss -> boss.withEcho(Optional.of(echo))));
    }

    /** The three bosses: a descending one, the breaching test colossus and the quiet test Sovereign (anchored). */
    static List<SetPieceSpec> arenaBosses() {
        return List.of(
                BossRushTest.echo("carrier", 11.5),
                at(BreachArenaTest.colossus(false), 21),
                at(Act5HSpecs.QUIET, 39.5));
    }

    static LevelScript rush(List<Section> sections, List<SetPieceSpec> bosses) {
        return LevelScript.builder(49, 7)
                .sections(sections)
                .setPieces(bosses)
                .objectives(ObjectiveSpec.NONE)
                .vrell(VrellLevel.NONE.withRush(Optional.of(BossRushTest.RUSH)))
                .build();
    }

    static Sortie sortie(LevelScript script) {
        return new Sortie(1, TestSpecs.LOADOUT, script, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Steps until fight {@code f}'s gate halts ({@link SimEvents.Type#FIGHT_NEXT} with {@code f}). */
    static void untilFight(Sortie sortie, int f) {
        for (int i = 0; i < SimStep.ticks(80); i++) {
            sortie.step(0);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.FIGHT_NEXT
                        && sortie.events().value(e) == f) {
                    return;
                }
            }
        }
        throw new AssertionError("fight " + f + " never began");
    }

    /** Kills set piece {@code k} at its vital part. */
    static void kill(Sortie sortie, int k) {
        SetPiece piece = sortie.setPiece(k);
        for (int p = 0; p < piece.spec().parts().size(); p++) {
            if (piece.spec().parts().get(p).vital()) {
                sortie.objectivePasses.wreck(k, p);
                return;
            }
        }
        sortie.objectivePasses.wreck(k, 0);
    }

    /** Steps once; returns how far the ground scrolled. */
    static double step(Sortie sortie) {
        double before = sortie.groundScroll();
        sortie.step(0);
        return sortie.groundScroll() - before;
    }

    // ---- The Brood Carrier echo's keys: windows on a move's end, the swept volley, the alternating brood ----

    static final int MANDIBLE = 0;
    static final List<Integer> SACS = List.of(1, 2, 3, 4);
    static final int CORE = 5;
    static final int FAN = 0;
    static final int PAIR = 1;
    static final double STATION_X = 240;
    static final double STATION_Y = 420;
    /** The move indexes in the echo's playbooks. */
    static final int MOVE_FANS = 0;

    static final int MOVE_SWEEP = 1;
    static final int MOVE_BROOD = 2;
    static final double SWEEP = Math.toRadians(30);

    private static PartSpec part(String name, double dx, double dy, double hp, boolean vital) {
        return new PartSpec(name, dx, dy, new Hitbox(40, 40), hp, vital, 20, Optional.empty(), 0, 1);
    }

    static PhasePlaybook carrierBook(int phase, List<Integer> sources) {
        PhasePlaybook.Bays bays = new PhasePlaybook.Bays(Act6HSpecs.BROOD, List.of(new BossSpec.Offset(0, 0)))
                .alternating(List.of(Act6HSpecs.DARTS), true);
        return new PhasePlaybook(
                phase,
                sources,
                0.8,
                1.0,
                0.7,
                0.5,
                List.of(
                        PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, FAN, 2, 0.5)
                                .covered(PAIR),
                        PhasePlaybook.Move.sweptVolley(1, FAN, 3, 0.3, SWEEP).covered(PAIR),
                        new PhasePlaybook.Move(PhasePlaybook.Kind.BROOD, 1, -1, 0, 0, 3, SACS, Optional.of(bays))
                                .covered(PAIR)));
    }

    /**
     * A small Brood Carrier echo arriving at {@code arrive} s: the mandibles' source, four sacs in two pairs opening on
     * each move's end, the core exposed once two sacs are dead; forming in place by a 3 s rise from its station.
     */
    static SetPieceSpec carrierEcho(double arrive) {
        EnemyGun fan = new EnemyGun(
                2, 0, 1, 150, 4, false, 5, Math.toRadians(50), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        EnemyGun pair = new EnemyGun(
                2, 0, 1, 150, 4, false, 2, Math.toRadians(8), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        List<BossSpec.Attack> attacks = List.of(
                new BossSpec.Attack("mandible-fan", BossSpec.Pattern.FAN, fan, 0, false, 5, 0, 0, 0, List.of(MANDIBLE)),
                new BossSpec.Attack("pair", BossSpec.Pattern.FAN, pair, 0, false, 2, 0, 0, 0, List.of(MANDIBLE)));
        BossSpec.Windows windows = new BossSpec.Windows(
                        List.of(List.of(1, 2), List.of(3, 4)), 2.4, 2, 0, false, List.of())
                .openingOnMoves();
        List<BossSpec.Phase> phases = List.of(
                new BossSpec.Phase(
                        "Sacs",
                        SACS,
                        2,
                        List.of(),
                        false,
                        Optional.empty(),
                        List.of(),
                        Double.NaN,
                        Double.POSITIVE_INFINITY,
                        0,
                        Optional.empty(),
                        Optional.of(windows),
                        BossSpec.PhaseArena.NONE),
                new BossSpec.Phase(
                        "Core",
                        List.of(),
                        0,
                        List.of(),
                        false,
                        Optional.empty(),
                        List.of(CORE),
                        Double.NaN,
                        Double.POSITIVE_INFINITY,
                        1,
                        Optional.empty(),
                        Optional.of(windows),
                        BossSpec.PhaseArena.NONE));
        BossSpec boss = BossSpec.builder(
                        arrive, STATION_X, STATION_Y, 200, 0, 1, Layer.AIR, false, "ECHO", 60, List.of(), attacks,
                        phases)
                .deathSeconds(2)
                .playbooks(List.of(carrierBook(0, List.of(MANDIBLE)), carrierBook(1, List.of(CORE, MANDIBLE))))
                .build()
                .withRise(Optional.of(new BossSpec.Rise(3, STATION_X, STATION_Y)))
                .withEcho(Optional.of(new EchoSpec(0, 1.0)));
        return new SetPieceSpec(
                "brood-carrier",
                new Hitbox(300, 200),
                new Hitbox(80, 80),
                0,
                List.of(
                        part("mandibles", 0, -70, 400, false),
                        part("sac-1", -110, 0, 100, false),
                        part("sac-2", 110, 0, 100, false),
                        part("sac-3", -110, 50, 100, false),
                        part("sac-4", 110, 50, 100, false),
                        part("core", 0, 20, 500, true)),
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    /** A level of 120 s at 100 px/s with {@code piece}. */
    static LevelScript level(SetPieceSpec piece) {
        return LevelScript.builder(49, 7)
                .sections(List.of(new Section(120, 100)))
                .setPieces(List.of(piece))
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    /** A rig that steps one boss alone, the ship where the test puts it, recording its fire and events. */
    static final class Rig implements BossActions {
        final SetPiece piece;
        int tick;
        double shipX = 100;
        double shipY = 100;
        final List<SimEvents.Type> types = new ArrayList<>();
        /** This step's aimed-at shots: {x, y, atX, atY}. */
        final List<double[]> aimedAt = new ArrayList<>();
        /** This step's plain aimed shots: {x, y}. */
        final List<double[]> aimed = new ArrayList<>();
        /** This step's bay launches: {x, y}, and their launchers. */
        final List<double[]> launches = new ArrayList<>();

        final List<EnemySpec.Launcher> launchers = new ArrayList<>();
        final SplitMix64 draws = new SplitMix64(42);

        Rig(SetPieceSpec spec) {
            piece = new SetPiece(spec);
            tick = SimStep.ticks(spec.boss().orElseThrow().arriveSeconds()) - 1;
        }

        void step() {
            types.clear();
            aimedAt.clear();
            aimed.clear();
            launches.clear();
            launchers.clear();
            tick++;
            piece.update(tick);
            piece.act(shipX, shipY, this, true);
        }

        @Override
        public void aimed(double x, double y, EnemyGun gun) {
            aimed.add(new double[] {x, y});
        }

        @Override
        public void aimedAt(double x, double y, EnemyGun gun, double atX, double atY) {
            aimedAt.add(new double[] {x, y, atX, atY});
        }

        @Override
        public void ring(double x, double y, int count, double speed, double damage) {}

        @Override
        public void bullet(double x, double y, double angle, double speed, double damage) {}

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {}

        @Override
        public void ring(SimEvents.Type type, double x, double y, int value) {
            types.add(type);
        }

        @Override
        public void bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {
            launches.add(new double[] {x, y});
            launchers.add(launcher);
        }

        @Override
        public double draw() {
            return draws.nextDouble();
        }
    }
}
