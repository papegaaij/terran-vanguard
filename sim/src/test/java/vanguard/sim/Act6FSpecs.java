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
        return guard(squadron, List.of(), HP);
    }

    /** The guard with its boss {@code attacks} (the playbook's) and {@code hp} an ace. */
    static SetPieceSpec guard(SquadronSpec squadron, List<BossSpec.Attack> attacks, double hp) {
        List<PartSpec> parts = new ArrayList<>();
        for (SquadronSpec.Ace ace : squadron.aces()) {
            parts.add(new PartSpec(
                    ace.callSign().toLowerCase(java.util.Locale.ROOT),
                    0,
                    0,
                    new Hitbox(42, 36),
                    hp,
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
                        attacks,
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

    // Step F1b: the playbook's attacks (indices into the boss's attacks) and the playbook per difficulty.

    static final int SNIPER = 0;
    static final int TAP = 1;
    static final int PAIR = 2;
    static final int FAN = 3;
    static final int SPIRAL = 4;
    static final int ROLL_FAN = 5;
    static final int RIBBON = 6;
    static final int DIVE_BURST = 7;
    static final int RAKE_PAIR = 8;
    static final int PINCER_FAN = 9;

    static EnemyGun gun(double interval, double speed, double damage, int fan, double spreadDegrees) {
        return new EnemyGun(
                interval,
                0,
                1,
                speed,
                damage,
                false,
                fan,
                Math.toRadians(spreadDegrees),
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    static BossSpec.Attack attack(String name, BossSpec.Pattern pattern, EnemyGun gun, int arms, double turn) {
        return new BossSpec.Attack(
                name, pattern, gun, 0, false, 1, arms, turn, 0, List.of(), false, Optional.empty(), List.of());
    }

    /** The sniper's line: {@code telegraph} s (medium 1.0, easy 1.2, hard 0.8), locked 0.2 s before, live 0.3 s. */
    static EnemySpec.LaserLine line(double telegraph) {
        return new EnemySpec.LaserLine(telegraph, 0.3, 6, 8, telegraph + 0.3, 0, 0).withLockBefore(0.2);
    }

    static List<BossSpec.Attack> attacks(double lineTelegraph, int arms) {
        BossSpec.Attack sniper = attack("sniper-line", BossSpec.Pattern.AIMED, gun(2, 600, 8, 1, 0), 0, 0);
        return List.of(
                sniper.withLine(line(lineTelegraph)),
                attack("sniper-tap", BossSpec.Pattern.AIMED, gun(2, 600, 8, 1, 0), 0, 0)
                        .withLine(line(0.8)),
                attack("pair-volley", BossSpec.Pattern.AIMED, gun(2, 170, 4, 2, 6), 0, 0),
                attack("fan", BossSpec.Pattern.FAN, gun(2, 150, 4, 5, 40), 0, 0),
                attack("spiral-loop", BossSpec.Pattern.SPIRAL, gun(0.25, 130, 4, 1, 0), arms, Math.PI / 2),
                attack("roll-fan", BossSpec.Pattern.FAN, gun(2, 150, 4, 5, 40), 0, 0),
                attack("ribbon", BossSpec.Pattern.AIMED, gun(2, 80, 4, 1, 0), 0, 0),
                attack("dive-burst", BossSpec.Pattern.AIMED, gun(2, 200, 6, 1, 0), 0, 0),
                attack("rake-pair", BossSpec.Pattern.AIMED, gun(2, 170, 4, 2, 6), 0, 0),
                attack("pincer-fan", BossSpec.Pattern.FAN, gun(2, 150, 4, 5, 40), 0, 0));
    }

    /**
     * The playbook at a difficulty: {@code taps} the medium double tap's 1 (easy 0, hard 2), {@code edgeTaps} after a
     * death, {@code diveTell} the dive's tell (easy 1.0), {@code restScale} every rest's (easy 1.3, hard 0.8).
     */
    static SquadronPlaybook playbook(int taps, int edgeTaps, double diveTell, double restScale) {
        SquadronPlaybook.Play dive = SquadronPlaybook.Play.of(
                        SquadronPlaybook.Kind.FEINT_DIVE, 3, DIVE_BURST, -1, 3, 5, 0.12)
                .withTell(diveTell);
        return new SquadronPlaybook(
                List.of(
                        new SquadronPlaybook.Pool(
                                List.of(
                                        SquadronPlaybook.Play.of(
                                                SquadronPlaybook.Kind.SNIPER_LINE, 3, SNIPER, TAP, taps, edgeTaps, 0.6),
                                        SquadronPlaybook.Play.of(
                                                SquadronPlaybook.Kind.PAIR_VOLLEY, 2, PAIR, -1, 4, 0, 0.4),
                                        SquadronPlaybook.Play.of(SquadronPlaybook.Kind.FAN, 2, FAN, -1, 2, 0, 0.5),
                                        SquadronPlaybook.Play.of(SquadronPlaybook.Kind.SLIDE, 1, -1, -1, 0, 0, 0)),
                                0),
                        new SquadronPlaybook.Pool(
                                List.of(
                                        SquadronPlaybook.Play.of(
                                                        SquadronPlaybook.Kind.SPIRAL_LOOP, 3, SPIRAL, -1, 0, 3, 0)
                                                .withSeconds(2.5),
                                        SquadronPlaybook.Play.of(
                                                SquadronPlaybook.Kind.ROLL_FAN, 2, ROLL_FAN, -1, 1, 0, 0),
                                        SquadronPlaybook.Play.of(SquadronPlaybook.Kind.RIBBON, 2, RIBBON, -1, 0, 0, 0)
                                                .withTrail(new SquadronPlaybook.Trail(8, 80, 2, 50))),
                                0),
                        new SquadronPlaybook.Pool(
                                List.of(
                                        dive,
                                        SquadronPlaybook.Play.of(
                                                SquadronPlaybook.Kind.REAR_RAKE, 2, RAKE_PAIR, -1, 0, 0, 0.4),
                                        SquadronPlaybook.Play.of(
                                                        SquadronPlaybook.Kind.OVERHEAD, 1.5, DIVE_BURST, -1, 3, 5, 0.12)
                                                .withTell(diveTell)),
                                0)),
                0.8,
                1.2 * restScale,
                2.0 * restScale,
                0.3,
                0.5,
                Optional.of(new SquadronPlaybook.Pincer(1.5, PINCER_FAN, 0.5, 4)),
                new SquadronPlaybook.Conductor(1, 2));
    }

    /** Step F1b's played guards: the stations, joins, HP, line, arms, taps and rests of each difficulty. */
    static final SetPieceSpec PLAYED = guard(MEDIUM.withPlaybook(playbook(1, 2, 0.8, 1)), attacks(1.0, 2), HP);

    static final SetPieceSpec PLAYED_EASY =
            guard(squadronAt(EASY, 130).withPlaybook(playbook(0, 1, 1.0, 1.3)), attacks(1.2, 1), 1200);

    static final SetPieceSpec PLAYED_HARD =
            guard(squadronAt(HARD, 90).withPlaybook(playbook(2, 3, 0.8, 0.8)), attacks(0.8, 3), 2080);

    /** {@code squadron} with the feinter pulling out at {@code pullOut} (easy 130, hard 90). */
    static SquadronSpec squadronAt(SquadronSpec squadron, double pullOut) {
        List<SquadronSpec.Ace> aces = new ArrayList<>(squadron.aces());
        SquadronSpec.Ace k = aces.get(KESTREL);
        SquadronSpec.Station s = k.station();
        aces.set(
                KESTREL,
                new SquadronSpec.Ace(
                        k.part(),
                        k.callSign(),
                        k.role(),
                        k.join(),
                        new SquadronSpec.Station(
                                s.yMin(),
                                s.yMax(),
                                s.x(),
                                s.speed(),
                                s.dashSpeed(),
                                s.climbSpeed(),
                                s.spanMin(),
                                s.spanMax(),
                                pullOut,
                                s.committedKeepOff(),
                                s.loopSeconds())));
        return new SquadronSpec(
                aces,
                squadron.tellSeconds(),
                squadron.separation(),
                squadron.hull(),
                squadron.shield(),
                squadron.contact());
    }

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
        /** Step F1b: every shot so far: {tick, 0 aimed / 1 bullet, x, y, angle (bullet), speed (bullet), damage}. */
        final List<double[]> shots = new ArrayList<>();
        /** Step F1b: the radio subjects raised so far, with their ticks. */
        final List<String> cues = new ArrayList<>();

        final List<Integer> cueTicks = new ArrayList<>();
        /** Step F1b: false: the actions record nothing (an allocation run). */
        boolean recording = true;

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
            if (recording) {
                types.clear();
                values.clear();
                places.clear();
            }
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
        public void aimed(double x, double y, EnemyGun gun) {
            if (recording) {
                shots.add(new double[] {tick, 0, x, y, 0, gun.bulletSpeed(), gun.damage(), gun.fan()});
            }
        }

        @Override
        public void ring(double x, double y, int count, double speed, double damage) {}

        @Override
        public void bullet(double x, double y, double angle, double speed, double damage) {
            if (recording) {
                shots.add(new double[] {tick, 1, x, y, angle, speed, damage, 1});
            }
        }

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {}

        /** Step F1b: the boss's line events ({@link BossLines}) are recorded as the squadron's. */
        @Override
        public void ring(SimEvents.Type type, double x, double y, int value) {
            squadron(type, x, y, value);
        }

        @Override
        public void squadronCue(String subject) {
            if (recording) {
                cues.add(subject);
                cueTicks.add(tick);
            }
        }

        @Override
        public void squadron(SimEvents.Type type, double x, double y, int value) {
            if (!recording) {
                return;
            }
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
