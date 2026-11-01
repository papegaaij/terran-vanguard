package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M10 part F, step F1a: a test Honour Guard at medium (design/enemies/bosses/honour-guard; m10f-gaps.md §1 rows 1–3
 * and 7): three aces of 1,600 HP and 120 credits each, Harker joining from the top 1 s after the sting, Vesper from
 * the side away from him at his 2/3 or 20 s later, Kestrel from the bottom 15 s after her or at the first death, each
 * told 2.5 s ahead; the stations of the spec in px up; hex shields of 60; contact 15 (the ace takes 40). Level 40's own
 * data is step F2's.
 */
final class Act6FSpecs {
    private Act6FSpecs() {}

    static final int HARKER = 0;
    static final int VESPER = 1;
    static final int KESTREL = 2;

    /** The duel's arrival on the level clock (the sting), its arena's end, the level's end. */
    static final double ARRIVE = 3;

    static final double ARENA_END = ARRIVE + 1;
    static final double END = ARENA_END + 30;
    static final double HP = 1600;
    static final int BOUNTY = 120;
    /** The parked centre: well above the top edge. */
    static final double PARK_X = PlayField.WIDTH / 2.0;

    static final double PARK_Y = PlayField.HEIGHT + 400;

    static final SquadronSpec.Station HARKER_STATION =
            new SquadronSpec.Station(370, 470, 160, 140, 280, 140, 40, 80, 0, 0, 0);
    static final SquadronSpec.Station VESPER_STATION =
            new SquadronSpec.Station(210, 390, 90, 200, 280, 200, 160, 220, 120, 120, 2.5);
    static final SquadronSpec.Station KESTREL_STATION =
            new SquadronSpec.Station(20, 120, 30, 140, 320, 260, 60, 60, 110, 85, 0);

    static final EnemySpec.Shield SHIELD = new EnemySpec.Shield(60, 2, 0.5, 3);
    static final SquadronSpec.Contact CONTACT = new SquadronSpec.Contact(15, 40, 24, 1);

    /** The joins at medium: Vesper at Harker's 2/3 or 20 s, Kestrel 15 s after her or at the first death. */
    static SquadronSpec squadron(double vesperShare, double vesperAfter, double kestrelAfter) {
        return new SquadronSpec(
                List.of(
                        new SquadronSpec.Ace(
                                HARKER,
                                "HARKER",
                                SquadronSpec.Role.SNIPER,
                                new SquadronSpec.Join(SquadronSpec.Edge.TOP, 1, 0, false),
                                HARKER_STATION),
                        new SquadronSpec.Ace(
                                VESPER,
                                "VESPER",
                                SquadronSpec.Role.DANCER,
                                new SquadronSpec.Join(SquadronSpec.Edge.SIDE, vesperAfter, vesperShare, false),
                                VESPER_STATION),
                        new SquadronSpec.Ace(
                                KESTREL,
                                "KESTREL",
                                SquadronSpec.Role.FEINTER,
                                new SquadronSpec.Join(SquadronSpec.Edge.BOTTOM, kestrelAfter, 0, true),
                                KESTREL_STATION)),
                2.5,
                80,
                new Hitbox(60, 54),
                SHIELD,
                CONTACT);
    }

    static final SquadronSpec MEDIUM = squadron(2.0 / 3, 20, 15);
    /** Easy: Vesper at Harker's half or 25 s, Kestrel 20 s later. */
    static final SquadronSpec EASY = squadron(0.5, 25, 20);
    /** Hard: Vesper at 3/4 or 15 s, Kestrel 10 s later. */
    static final SquadronSpec HARD = squadron(0.75, 15, 10);

    static BossSpec.Phase idle() {
        return new BossSpec.Phase(
                "The duel",
                List.of(),
                0,
                List.of(),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                BossSpec.PHASE_DELAY_SECONDS,
                Optional.empty(),
                Optional.empty(),
                BossSpec.PhaseArena.NONE);
    }

    static SetPieceSpec guard(SquadronSpec squadron) {
        List<PartSpec> parts = new ArrayList<>();
        for (SquadronSpec.Ace ace : squadron.aces()) {
            parts.add(new PartSpec(
                    ace.callSign().toLowerCase(java.util.Locale.ROOT),
                    0,
                    0,
                    new Hitbox(42, 36),
                    HP,
                    false,
                    BOUNTY,
                    Optional.empty(),
                    0,
                    1));
        }
        BossSpec boss = BossSpec.builder(
                        ARRIVE,
                        PARK_X,
                        PARK_Y,
                        100,
                        0,
                        1,
                        Layer.AIR,
                        true,
                        "HONOUR GUARD",
                        75,
                        List.of(),
                        List.of(),
                        List.of(idle()))
                .engagesOnArrival(true)
                .squadron(Optional.of(squadron))
                .build();
        return new SetPieceSpec(
                "honour-guard",
                new Hitbox(60, 54),
                new Hitbox(1, 1),
                15,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    static final SetPieceSpec GUARD = guard(MEDIUM);

    /** 3 s at 160 px/s, the duel's arena (a 20 px/s drift, held 4 s after the kill), 30 s at 160 after it. */
    static LevelScript level(SetPieceSpec guard) {
        return LevelScript.builder(40, 6)
                .sections(List.of(
                        new Section(ARRIVE, 160),
                        new Section(ARENA_END, 20, true, Section.TIMED_HALT_RAMP_SECONDS, 0, 4),
                        new Section(END, 160)))
                .setPieces(List.of(guard))
                .build();
    }

    static Sortie sortie(SetPieceSpec guard, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level(guard), rules, TestSpecs.FULL_ARMOUR);
    }

    static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    static Squadron squadron(Sortie sortie) {
        return boss(sortie).squadron().orElseThrow();
    }

    /** Kills ace {@code a} outright (a flat hit, as the Lance's; its shield first). */
    static void kill(SetPiece boss, int a) {
        int p = boss.squadron().orElseThrow().part(a);
        boss.damagePartFlat(p, boss.partHp(p) + 1000);
    }

    /** Kills ace {@code a} in the sortie as a hit would: its bounty paid, the last one ending the piece. */
    static void kill(Sortie sortie, int a) {
        SetPiece boss = boss(sortie);
        int p = boss.squadron().orElseThrow().part(a);
        if (boss.damagePartFlat(p, boss.partHp(p) + 1000)) {
            sortie.objectivePasses.wreck(0, p);
        }
    }

    /**
     * A stand-in for step F1b's playbook: an ace holding its station rests 1.2 s, then draws one of its role's moves
     * from its own seeded random; now and then the two flankers fly the pincer together.
     */
    static final class Playbook implements BossActions {
        private static final AceMove[][] MOVES = {
            {AceMove.SLIDE, AceMove.HOLD},
            {AceMove.LOOP, AceMove.ROLL, AceMove.RIBBON},
            {AceMove.DIVE, AceMove.RAKE, AceMove.OVERHEAD}
        };
        private final SplitMix64 random;
        private final int[] rest = new int[3];

        Playbook(long seed) {
            random = new SplitMix64(seed);
        }

        void step(Squadron squadron, double shipX, double shipY) {
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (!squadron.ready(a)) {
                    rest[a] = SimStep.ticks(1.2);
                    continue;
                }
                if (rest[a] > 0) {
                    rest[a]--;
                    continue;
                }
                if (a != HARKER && squadron.ready(VESPER) && squadron.ready(KESTREL) && random.nextDouble() < 0.15) {
                    squadron.startPincer(VESPER, KESTREL, shipX, shipY);
                    continue;
                }
                AceMove[] moves = MOVES[squadron.spec().aces().get(a).role().ordinal()];
                squadron.start(a, moves[(int) (random.nextDouble() * moves.length)], shipX, shipY, this);
            }
        }

        @Override
        public void aimed(double x, double y, EnemyGun gun) {}

        @Override
        public void ring(double x, double y, int count, double speed, double damage) {}

        @Override
        public void bullet(double x, double y, double angle, double speed, double damage) {}

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {}

        @Override
        public double draw() {
            return random.nextDouble();
        }
    }

    /**
     * A sortie-free rig: the guard on its own, the ship where the test puts it; it records the squadron's events and
     * draws from its own seeded random (a replay draws the same).
     */
    static class Rig implements BossActions {
        final SetPiece piece;
        final Squadron squadron;
        final SplitMix64 random;
        int tick;
        double shipX = Ship.START_X;
        double shipY = Ship.START_Y;
        final List<SimEvents.Type> types = new ArrayList<>();
        final List<Integer> values = new ArrayList<>();
        final List<double[]> places = new ArrayList<>();

        Rig(SetPieceSpec spec, long seed) {
            piece = new SetPiece(spec);
            squadron = piece.squadron().orElseThrow();
            random = new SplitMix64(seed);
            tick = SimStep.ticks(ARRIVE) - 1;
        }

        Rig(SetPieceSpec spec) {
            this(spec, 1);
        }

        void step() {
            types.clear();
            values.clear();
            places.clear();
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

        /** Steps until every ace is on the field (wholly), at most {@code seconds}. */
        void untilAllFly(double seconds) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step();
                boolean all = true;
                for (int a = 0; a < squadron.aceCount(); a++) {
                    all &= squadron.touchable(a);
                }
                if (all) {
                    return;
                }
            }
            throw new AssertionError("all three on the field within " + seconds + " s");
        }

        @Override
        public void aimed(double x, double y, EnemyGun gun) {}

        @Override
        public void ring(double x, double y, int count, double speed, double damage) {}

        @Override
        public void bullet(double x, double y, double angle, double speed, double damage) {}

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {}

        @Override
        public void squadron(SimEvents.Type type, double x, double y, int value) {
            types.add(type);
            values.add(value);
            places.add(new double[] {x, y});
        }

        @Override
        public double draw() {
            return random.nextDouble();
        }
    }
}
