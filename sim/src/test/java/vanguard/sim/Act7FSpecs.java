package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M11 part F, step F1: a test Chimera (design/enemies/bosses/vornes-chimera) on the built engine with part F's
 * extensions: two mirrored batteries, a rack and a bay (phase 1), three regenerating sacs (phase 2, opening 3 s), the
 * capsule (phase 3, opening 2 s, its own hex shield); the rise out of the well, the rear sweep, the last words and Rook's
 * phase-1 order; its level (an arena that holds 8 s after it); and a rig that steps it alone.
 */
final class Act7FSpecs {
    private Act7FSpecs() {}

    static final int CAPSULE = 0;
    static final int BAT_L = 1;
    static final int BAT_R = 2;
    static final int RACK = 3;
    static final int BAY = 4;
    static final int SAC_L = 5;
    static final int SAC_R = 6;
    static final int SAC_S = 7;
    static final List<Integer> PHASE_1 = List.of(BAT_L, BAT_R, RACK, BAY);
    static final List<Integer> SACS = List.of(SAC_L, SAC_R, SAC_S);

    static final int LANCE = 0;
    static final int FAN = 1;
    static final int SPORE_PAIR = 2;
    static final int SPIRAL = 3;

    static final double ARRIVE = 3;
    static final double ARENA_END = ARRIVE + 1;
    static final double END = ARENA_END + 30;
    static final double X = 240;
    /** The station: y 140 below the top edge. */
    static final double Y = PlayField.HEIGHT - 140;
    /** The well's point: y 60 below the top edge. */
    static final double WELL_Y = PlayField.HEIGHT - 60;

    static final Hitbox SIZE = new Hitbox(200, 210);
    static final Hitbox BODY = new Hitbox(120, 150);
    static final double CONTACT = 25;
    static final EnemySpec.Shield CAPSULE_SHIELD = new EnemySpec.Shield(120, 2.0, 1.0, 3.0);
    static final RearSweepSpec SWEEP = new RearSweepSpec(1.0, 200, 160, 100, SPORE_PAIR, 0.4);
    static final LastWordsSpec WORDS = new LastWordsSpec(4.5, 0.5);
    static final BossSpec.Rise RISE = new BossSpec.Rise(4, X, WELL_Y);

    private static PartSpec part(String name, double dx, double dy, Hitbox box, double hp, boolean vital) {
        return new PartSpec(name, dx, dy, box, hp, vital, 10, Optional.empty(), 0, 1);
    }

    static List<PartSpec> parts() {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(part("capsule", 0, -10, new Hitbox(44, 60), 1900, true));
        parts.add(part("bat-l", -70, 10, new Hitbox(32, 32), 700, false));
        parts.add(part("bat-r", 70, 10, new Hitbox(32, 32), 700, false));
        parts.add(part("rack", 0, 60, new Hitbox(30, 30), 700, false));
        parts.add(part("bay", 0, -50, new Hitbox(40, 28), 800, false));
        parts.add(part("sac-l", -70, 10, new Hitbox(44, 44), 550, false));
        parts.add(part("sac-r", 70, 10, new Hitbox(44, 44), 550, false));
        parts.add(part("sac-s", 0, 60, new Hitbox(44, 44), 550, false));
        return parts;
    }

    static List<BossSpec.Attack> attacks() {
        EnemyGun lanceGun = EnemyGun.aimed(10, 0, 1, 600, 10, false);
        EnemyGun fanGun = new EnemyGun(
                2, 0, 1, 150, 4, false, 7, Math.toRadians(70), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        EnemyGun pairGun = new EnemyGun(
                2, 0, 1, 150, 4, false, 2, Math.toRadians(8), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        return List.of(
                new BossSpec.Attack("lance", BossSpec.Pattern.AIMED, lanceGun, 0, false, 1, 0, 0, 0, List.of(CAPSULE))
                        .withLine(new EnemySpec.LaserLine(1.2, 0.4, 16, 10, 10, 0, 0).withLockBefore(0.2)),
                new BossSpec.Attack("gold-fan", BossSpec.Pattern.FAN, fanGun, 0, false, 7, 0, 0, 0, List.of(CAPSULE)),
                new BossSpec.Attack("spore-pair", BossSpec.Pattern.FAN, pairGun, 0, false, 2, 0, 0, 0, SACS),
                new BossSpec.Attack(
                        "spore-spiral",
                        BossSpec.Pattern.SPIRAL,
                        EnemyGun.aimed(0.25, 0, 1, 120, 4, false),
                        0,
                        false,
                        1,
                        2,
                        Math.toRadians(60),
                        3,
                        List.of(CAPSULE)));
    }

    /**
     * The playbooks: phase 1 the fan and the lance (a lance at least every third draw); phase 2 the spiral and the rear
     * sweep (never first, at least every {@code sweepEvery}-th draw); phase 3 the lance, the fan, the spiral and the
     * sweep (a lance or the sweep at least every third).
     */
    static List<PhasePlaybook> playbooks(int sweepEvery) {
        PhasePlaybook.Move sweep = PhasePlaybook.Move.rearSweep(1).neverFirst();
        return List.of(
                new PhasePlaybook(
                                0,
                                List.of(CAPSULE),
                                0.8,
                                1.0,
                                0.7,
                                0.5,
                                List.of(
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.LINE, 1, LANCE, 1, 0),
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, FAN, 2, 0.5)))
                        .withLanceEvery(3),
                new PhasePlaybook(
                                1,
                                SACS,
                                0.8,
                                1.0,
                                0.7,
                                0.5,
                                List.of(PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0), sweep))
                        .withLanceEvery(sweepEvery),
                new PhasePlaybook(
                                2,
                                List.of(CAPSULE),
                                0.8,
                                0.8,
                                0.6,
                                0.5,
                                List.of(
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.LINE, 1, LANCE, 1, 0),
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, FAN, 2, 0.5),
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0),
                                        sweep))
                        .withLanceEvery(3));
    }

    static BossSpec.Phase phase(String name, List<Integer> until, List<Integer> exposes, double delay) {
        return Act6HSpecs.phase(name, until, exposes, delay);
    }

    static BossSpec boss(List<PhasePlaybook> playbooks) {
        return BossSpec.builder(
                        ARRIVE,
                        X,
                        Y,
                        100,
                        120,
                        8.4,
                        Layer.AIR,
                        true,
                        "VORNE'S CHIMERA",
                        67,
                        List.of(),
                        attacks(),
                        List.of(
                                phase("The gold", PHASE_1, List.of(), BossSpec.PHASE_DELAY_SECONDS),
                                phase("The tissue", SACS, SACS, 3),
                                phase("Both", List.of(CAPSULE), List.of(CAPSULE), 2)))
                .mirrors(List.of(
                        new PartMirror(BAT_L, 40, 0, Act6HSpecs.PANEL),
                        new PartMirror(BAT_R, -40, 0, Act6HSpecs.PANEL)))
                .playbooks(playbooks)
                .openings(List.of(
                        new PhaseOpening(1, 3, Optional.of(PickupType.ARMOUR_PATCH), 0, -120),
                        new PhaseOpening(2, 2, Optional.empty(), 0, 0)))
                .regeneration(List.of(
                        new PartRegen(SAC_L, 0.04, 1.5),
                        new PartRegen(SAC_R, 0.04, 1.5),
                        new PartRegen(SAC_S, 0.04, 1.5)))
                .armoured(List.of())
                .rearSweep(Optional.of(SWEEP))
                .lastWords(Optional.of(WORDS))
                .rise(Optional.of(RISE))
                .partShields(List.of(new PartShield(CAPSULE, CAPSULE_SHIELD)))
                .rookOrders(List.of(new BossSpec.RookOrder(0, List.of(BAY, RACK, BAT_L, BAT_R))))
                .build();
    }

    static SetPieceSpec chimera(List<PhasePlaybook> playbooks) {
        return new SetPieceSpec(
                "vornes-chimera",
                SIZE,
                BODY,
                CONTACT,
                parts(),
                List.of(),
                Optional.empty(),
                Optional.of(boss(playbooks)));
    }

    static final SetPieceSpec CHIMERA = chimera(playbooks(3));

    /** A Chimera whose phase 2 draws the sweep every second draw (the spacing lever). */
    static final SetPieceSpec SWEEPING = chimera(playbooks(2));

    /** 3 s at 160 px/s, the arena (held while the boss lives, 8 s after its K), 30 s at 160 after it. */
    static LevelScript level(SetPieceSpec chimera) {
        return LevelScript.builder(48, 6)
                .sections(List.of(
                        new Section(ARRIVE, 160),
                        new Section(ARENA_END, 0, true, Section.TIMED_HALT_RAMP_SECONDS, 0, 8),
                        new Section(END, 160)))
                .setPieces(List.of(chimera))
                .build();
    }

    static Sortie sortie(SetPieceSpec chimera, Loadout loadout, Rules rules) {
        return new Sortie(1, loadout, level(chimera), rules, TestSpecs.FULL_ARMOUR);
    }

    /** Kills part {@code p} of the sortie's boss as a hit would (its bounty paid; the capsule's K). */
    static void kill(Sortie sortie, int p) {
        SetPiece boss = sortie.setPiece(0);
        if (boss.damagePartFlat(p, boss.partHp(p) + 100_000)) {
            sortie.objectivePasses.wreck(0, p);
        }
    }

    /** A rig that steps the Chimera alone, the ship where the test puts it, recording its fire, events and cues. */
    static class Rig implements BossActions {
        final SetPiece piece;
        int tick;
        double shipX = 100;
        double shipY = 100;
        final List<SimEvents.Type> types = new ArrayList<>();
        final List<Integer> values = new ArrayList<>();
        final List<double[]> eventPlaces = new ArrayList<>();
        /** This step's aimed shots: {x, y}. */
        final List<double[]> shots = new ArrayList<>();

        final List<String> cues = new ArrayList<>();
        final List<String> sweepCues = new ArrayList<>();
        final SplitMix64 draws;

        Rig(SetPieceSpec spec) {
            this(spec, 42);
        }

        Rig(SetPieceSpec spec, long seed) {
            piece = new SetPiece(spec);
            tick = SimStep.ticks(ARRIVE) - 1;
            draws = new SplitMix64(seed);
        }

        void step() {
            types.clear();
            values.clear();
            eventPlaces.clear();
            shots.clear();
            tick++;
            piece.update(tick);
            piece.act(shipX, shipY, this, true);
        }

        void steps(double seconds) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step();
            }
        }

        Rig settled() {
            for (int i = 0; i < SimStep.ticks(15) && !piece.engaged(); i++) {
                step();
            }
            if (!piece.engaged()) {
                throw new AssertionError("never settled");
            }
            return this;
        }

        void kill(int p) {
            piece.damagePartFlat(p, piece.partHp(p) + 100_000);
        }

        /** Kills phase 1's parts and steps past the fusion's opening into phase 2. */
        Rig toPhase2() {
            for (int p : PHASE_1) {
                kill(p);
            }
            steps(3.05);
            return this;
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

        double[] place(SimEvents.Type type) {
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i) == type) {
                    return eventPlaces.get(i);
                }
            }
            return null;
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
        public void phase(int phase) {}

        @Override
        public void ring(SimEvents.Type type, double x, double y, int value) {
            types.add(type);
            values.add(value);
            eventPlaces.add(new double[] {x, y});
        }

        @Override
        public void bossCue(String subject) {
            cues.add(subject);
        }

        @Override
        public void sweepCue(String subject) {
            sweepCues.add(subject);
        }

        @Override
        public double draw() {
            return draws.nextDouble();
        }
    }
}
