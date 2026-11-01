package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M10 part H, step H1a: a test Ascendant at medium (design/enemies/bosses/ascendant; m10h-gaps.md §1 rows 1–6 and 13),
 * on the spec's screen (480×540, y down) turned into the simulation's (y up). Its centre rests at the screen's (240,
 * 125), sim (240, 415), after a 6 s bow-first descent; its bow a wedge from the top edge (x 40–440) to the 100 px tip at
 * the screen's y 250; contact 25 once a second, the ship pushed 24 px. Phase 1: the bow cannon 2,800 (not vital) under
 * its gold hex shield of 240 fed by the generators (800 each), four mirror batteries of 1,100 behind their panels (45°
 * to their own side, ±20° at 30°/s, 120° arc, heat 260, down 3 s, regrowth 0.4 s), bursting 3 aimed {@code medium} 6
 * every 3 s after a 0.5 s tell, two at once, 0.6 s apart; the cannon's playbook (the spinal lance, the gold fan, the
 * missile salvo from the generators' racks, the Dart stream every third draw; rest 1.5 s, 0.8 s below half, tells
 * 0.8 s). Step H1b: the eruption (4 s untouchable, an armour patch), phase 2a's three regenerating sacs (1,300; 4 % a
 * second after 1.5 s) with the heart sealed, phase 2b's heart (2,500; 2 % after 2.0 s) once two sacs are dead, the
 * Vrell playbook (the brood every third draw, the spore spiral, the bile lobs, the grafted guns; rests 1.2 s, 0.7 s
 * below half of phase 2's HP), the hull's break at the heart's death (no vital part) and the lifeboat's escape (from
 * the screen's (240, 175) in 14 s, the cap 1,400, its bounty 60). Level 42's own data is step H2's.
 */
final class Act6HSpecs {
    private Act6HSpecs() {}

    static final int CANNON = 0;
    static final int GEN_L = 1;
    static final int GEN_R = 2;
    static final int BAT_1 = 3;
    static final int BAT_2 = 4;
    static final int BAT_3 = 5;
    static final int BAT_4 = 6;
    static final int SAC_1 = 7;
    static final int SAC_2 = 8;
    static final int SAC_3 = 9;
    static final int HEART = 10;
    static final int LIFEBOAT = 11;
    static final List<Integer> SACS = List.of(7, 8, 9);
    static final List<Integer> PHASE_2 = List.of(7, 8, 9, 10);
    static final List<Integer> PHASE_1 = List.of(CANNON, GEN_L, GEN_R, BAT_1, BAT_2, BAT_3, BAT_4);
    static final List<Integer> BATTERIES = List.of(BAT_1, BAT_2, BAT_3, BAT_4);
    static final List<Integer> GENERATORS = List.of(GEN_L, GEN_R);

    static final int LANCE = 0;
    static final int FAN = 1;
    static final int SALVO = 2;
    static final int BURST = 3;
    static final int SPIRAL = 4;
    static final int LOB = 5;
    static final int GRAFTED = 6;
    /** The playbook's moves, in its order. */
    static final int MOVE_LANCE = 0;

    static final int MOVE_FAN = 1;
    static final int MOVE_SALVO = 2;
    static final int MOVE_DARTS = 3;

    static final double ARRIVE = 3;
    static final double ARENA_END = ARRIVE + 1;
    static final double END = ARENA_END + 30;
    /** The centre at rest: the screen's (240, 125). */
    static final double X = 240;

    static final double Y = PlayField.HEIGHT - 125;
    /** The sprite box: from the top edge to below the tip. */
    static final Hitbox SIZE = new Hitbox(480, 270);
    /** The descent from above the top edge to its rest in 6 s. */
    static final double ENTRY_REACH = 135;

    static final double DESCENT = (PlayField.HEIGHT + ENTRY_REACH - Y) / 6;
    static final double CONTACT = 25;
    static final WedgeBody WEDGE = new WedgeBody(200, 125, 50, -125, 24);
    static final double TOTAL_HP = 2800 + 2 * 800 + 4 * 1100 + 3 * 1300 + 2500;
    /** The lifeboat's cap and bounty. */
    static final double CAP = 1400;

    static final int LIFEBOAT_BOUNTY = 60;
    /** The Vrell playbook's moves, in its order. */
    static final int MOVE_BROOD = 0;

    static final int MOVE_SPIRAL = 1;
    static final int MOVE_LOBS = 2;
    static final int MOVE_GRAFTED = 3;

    static final EnemySpec.Shield CANNON_SHIELD = new EnemySpec.Shield(240, 2.0, 0.5, 3.0);
    static final EnemySpec.Mirror PANEL = new EnemySpec.Mirror(120, 260, 30, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30);

    /** The hull bays' Dart streams: 4 a bay 0.4 s apart, ±15°, 16 alive. */
    static final EnemySpec.Launcher DARTS =
            new EnemySpec.Launcher(Act5CSpecs.DART, 4, 5, 0.4, 1, 240, Math.toRadians(30), 0.4, 16);

    /** A part at the spec's screen place (sx, sy). */
    private static PartSpec part(String name, double sx, double sy, Hitbox box, double hp, boolean vital) {
        return part(name, sx, sy, box, hp, vital, 10);
    }

    private static PartSpec part(String name, double sx, double sy, Hitbox box, double hp, boolean vital, int bounty) {
        return new PartSpec(name, sx - X, (PlayField.HEIGHT - sy) - Y, box, hp, vital, bounty, Optional.empty(), 0, 1);
    }

    /** A Chimera as the brood launches it: 40 HP, regenerating. */
    static final EnemySpec CHIMERA = EnemySpec.of("chimera", 40, new Hitbox(28, 24), Layer.AIR, 10, true, 14, 0)
            .withRegeneration(Optional.of(new EnemySpec.Regeneration(0.20, 1.0)));

    /** The brood: a wing of 4 Chimeras 0.3 s apart, ±20°, at most 6 boss-launched units alive. */
    static final EnemySpec.Launcher BROOD =
            new EnemySpec.Launcher(CHIMERA, 4, 5, 0.3, 1, 200, Math.toRadians(40), 0.5, 6);

    /** The bile lob: a mortar of 1.0 s (the marks' time), 20 px, a ring of 10 {@code small} 4. */
    static final EnemyGun BILE = new EnemyGun(
            4,
            0,
            1,
            110,
            6,
            false,
            1,
            0,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Optional.empty(),
            Optional.of(new EnemyGun.MortarSpec(1.0, 20, 10, 4)));

    /** The escape: K+2.5 the flash, K+3 the breakout at the screen's (240, 175), 14 s up, the cap 1,400. */
    static final EscapeSpec ESCAPE = new EscapeSpec(
            LIFEBOAT,
            HEART,
            2.5,
            3,
            240,
            PlayField.HEIGHT - 175,
            14,
            20,
            90,
            60,
            PlayField.HEIGHT - 200,
            CAP,
            0.8,
            60,
            4,
            16,
            20);

    static List<PartSpec> parts() {
        Hitbox battery = new Hitbox(36, 36);
        Hitbox generator = new Hitbox(40, 40);
        List<PartSpec> parts = new ArrayList<>();
        parts.add(part("cannon", 240, 235, new Hitbox(56, 40), 2800, false));
        parts.add(part("gen-l", 110, 60, generator, 800, false));
        parts.add(part("gen-r", 370, 60, generator, 800, false));
        parts.add(part("bat-1", 150, 130, battery, 1100, false));
        parts.add(part("bat-2", 185, 190, battery, 1100, false));
        parts.add(part("bat-3", 330, 130, battery, 1100, false));
        parts.add(part("bat-4", 295, 190, battery, 1100, false));
        parts.add(part("sac-1", 200, 50, new Hitbox(44, 44), 1300, false));
        parts.add(part("sac-2", 280, 50, new Hitbox(44, 44), 1300, false));
        parts.add(part("sac-3", 240, 150, new Hitbox(44, 44), 1300, false));
        parts.add(part("heart", 240, 100, new Hitbox(60, 60), 2500, false));
        parts.add(part("lifeboat", 240, 175, new Hitbox(40, 60), CAP, false, LIFEBOAT_BOUNTY));
        return parts;
    }

    static List<BossSpec.Attack> attacks() {
        EnemyGun lanceGun = EnemyGun.aimed(10, 0, 1, 600, 10, false);
        EnemyGun fanGun = new EnemyGun(
                2, 0, 1, 150, 4, false, 9, Math.toRadians(80), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        EnemyGun burstGun = EnemyGun.aimed(3.0, 0, 3, 160, 6, false);
        return List.of(
                new BossSpec.Attack(
                                "spinal-lance", BossSpec.Pattern.AIMED, lanceGun, 0, false, 1, 0, 0, 0, List.of(CANNON))
                        .withLine(new EnemySpec.LaserLine(1.2, 0.5, 24, 10, 10, 0, 0).withLockBefore(0.2)),
                new BossSpec.Attack("gold-fan", BossSpec.Pattern.FAN, fanGun, 0, false, 9, 0, 0, 0, List.of(CANNON)),
                new BossSpec.Attack(
                        "missile-salvo",
                        BossSpec.Pattern.AIMED,
                        Act5BSpecs.samGun(10, 1, 8, 96, false),
                        0,
                        false,
                        1,
                        0,
                        0,
                        0,
                        GENERATORS),
                new BossSpec.Attack(
                        "battery-burst", BossSpec.Pattern.AIMED, burstGun, 0.15, false, 1, 0, 0, 0, BATTERIES),
                new BossSpec.Attack(
                        "spore-spiral",
                        BossSpec.Pattern.SPIRAL,
                        EnemyGun.aimed(0.25, 0, 1, 120, 4, false),
                        0,
                        false,
                        1,
                        3,
                        Math.toRadians(60),
                        4,
                        List.of(HEART)),
                new BossSpec.Attack("bile-lob", BossSpec.Pattern.LOB, BILE, 0, false, 1, 0, 0, 0, List.of(HEART)),
                new BossSpec.Attack(
                        "grafted-guns",
                        BossSpec.Pattern.FAN,
                        new EnemyGun(
                                2,
                                0,
                                1,
                                150,
                                4,
                                false,
                                5,
                                Math.toRadians(40),
                                Double.POSITIVE_INFINITY,
                                Double.POSITIVE_INFINITY),
                        0,
                        false,
                        5,
                        0,
                        0,
                        0,
                        GENERATORS));
    }

    /** The Vrell playbook from {@code sources} in phase {@code phase}: rests 1.2 s, 0.7 s below half of phase 2's HP. */
    static PhasePlaybook vrell(int phase, List<Integer> sources) {
        return new PhasePlaybook(
                        phase,
                        sources,
                        0.8,
                        1.2,
                        0.7,
                        0.5,
                        List.of(
                                new PhasePlaybook.Move(
                                        PhasePlaybook.Kind.BROOD,
                                        1,
                                        -1,
                                        0,
                                        0,
                                        3,
                                        SACS,
                                        Optional.of(new PhasePlaybook.Bays(BROOD, List.of(new BossSpec.Offset(0, 0))))),
                                PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, SPIRAL, 1, 0),
                                PhasePlaybook.Move.of(PhasePlaybook.Kind.LOBS, 1, LOB, 3, 0),
                                new PhasePlaybook.Move(
                                        PhasePlaybook.Kind.MOUNTS,
                                        1,
                                        GRAFTED,
                                        3,
                                        0.4,
                                        1,
                                        GENERATORS,
                                        Optional.empty())))
                .withLowParts(PHASE_2);
    }

    static BossSpec.Phase phase(String name, List<Integer> until, List<Integer> exposes, double delay) {
        return phase(name, until, 0, exposes, delay);
    }

    static BossSpec.Phase phase(String name, List<Integer> until, int left, List<Integer> exposes, double delay) {
        return new BossSpec.Phase(
                name,
                until,
                left,
                List.of(),
                false,
                Optional.empty(),
                exposes,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                delay,
                Optional.empty(),
                Optional.empty(),
                BossSpec.PhaseArena.NONE);
    }

    static List<PartMirror> mirrors(EnemySpec.Mirror panel) {
        return List.of(
                new PartMirror(BAT_1, 45, 20, panel),
                new PartMirror(BAT_2, 45, 20, panel),
                new PartMirror(BAT_3, -45, 20, panel),
                new PartMirror(BAT_4, -45, 20, panel));
    }

    /** The cannon's playbook: the lance, the fan, the salvo from both racks, the Darts from two bays every third draw. */
    static PhasePlaybook playbook(double rest, double lowRest) {
        return new PhasePlaybook(
                0,
                List.of(CANNON),
                0.8,
                rest,
                lowRest,
                0.5,
                List.of(
                        PhasePlaybook.Move.of(PhasePlaybook.Kind.LINE, 1, LANCE, 1, 0),
                        PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, FAN, 3, 0.5),
                        new PhasePlaybook.Move(
                                PhasePlaybook.Kind.SALVO, 1, SALVO, 3, 0.25, 1, GENERATORS, Optional.empty()),
                        new PhasePlaybook.Move(
                                PhasePlaybook.Kind.STREAM,
                                1,
                                -1,
                                0,
                                0,
                                3,
                                List.of(),
                                Optional.of(new PhasePlaybook.Bays(
                                        DARTS, List.of(new BossSpec.Offset(-60, 40), new BossSpec.Offset(60, 40)))))));
    }

    /** The eruption: 4 s untouchable, an armour patch under the bow's tip. */
    static final PhaseOpening OPENING = new PhaseOpening(1, 4, Optional.of(PickupType.ARMOUR_PATCH), 0, -(125 + 30));

    /** The sacs 4 % a second after 1.5 s, the heart 2 % after 2.0 s. */
    static final List<PartRegen> REGENERATION = List.of(
            new PartRegen(SAC_1, 0.04, 1.5),
            new PartRegen(SAC_2, 0.04, 1.5),
            new PartRegen(SAC_3, 0.04, 1.5),
            new PartRegen(HEART, 0.02, 2.0));

    static final List<PhaseCue> CUES = List.of(
            new PhaseCue("first-generator", 0, GENERATORS, 1, 0, 25),
            new PhaseCue("cannon-half", 0, List.of(CANNON), 0, 0.5, 0));

    /** The test Ascendant: with {@code fire} its batteries burst and its cannon draws its playbook. */
    static SetPieceSpec ascendant(boolean fire) {
        return ascendant(fire, PANEL, BossSpec.PHASE_DELAY_SECONDS);
    }

    static SetPieceSpec ascendant(boolean fire, EnemySpec.Mirror panel, double firstDelay) {
        BossSpec boss = BossSpec.builder(
                        ARRIVE,
                        X,
                        Y,
                        DESCENT,
                        0,
                        1,
                        Layer.AIR,
                        false,
                        "ASCENDANT",
                        150,
                        List.of(),
                        attacks(),
                        List.of(
                                phase("The gold hull", PHASE_1, List.of(), firstDelay),
                                phase("eruption", SACS, 1, SACS, 4),
                                phase("heart-open", List.of(HEART), List.of(HEART), 0)))
                .incomingSeconds(5)
                .wedge(Optional.of(WEDGE))
                .fedShields(List.of(new FedShield(CANNON, CANNON_SHIELD, GENERATORS)))
                .mirrors(mirrors(panel))
                .batteryFire(fire ? Optional.of(new BatteryFire(BATTERIES, BURST, 2, 0.6, 0.5)) : Optional.empty())
                .playbooks(fire ? List.of(playbook(1.5, 0.8), vrell(1, SACS), vrell(2, List.of(HEART))) : List.of())
                .cues(CUES)
                .openings(List.of(OPENING))
                .regeneration(REGENERATION)
                .escape(Optional.of(ESCAPE))
                .build();
        return new SetPieceSpec(
                "ascendant",
                SIZE,
                new Hitbox(400, 250),
                CONTACT,
                parts(),
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    static final SetPieceSpec ASCENDANT = ascendant(true);
    static final SetPieceSpec QUIET = ascendant(false);

    /** 3 s at 160 px/s, the arena (a 1 s ease to rest, held while the boss lives), 30 s at 160 after it. */
    static LevelScript level(SetPieceSpec ascendant) {
        return level(ascendant, List.of());
    }

    /** As {@link #level(SetPieceSpec)} with {@code radio}'s cues. */
    static LevelScript level(SetPieceSpec ascendant, List<RadioCue> radio) {
        return LevelScript.builder(42, 6)
                .radio(radio)
                .sections(List.of(
                        new Section(ARRIVE, 160),
                        new Section(ARENA_END, 0, true, Section.TIMED_HALT_RAMP_SECONDS, 0, 4),
                        new Section(END, 160)))
                .setPieces(List.of(ascendant))
                .build();
    }

    static Sortie sortie(SetPieceSpec ascendant, Loadout loadout, Rules rules) {
        return new Sortie(1, loadout, level(ascendant), rules, TestSpecs.FULL_ARMOUR);
    }

    /** Steps the sortie with {@code command} until the boss has settled (engaged), at most 15 s. */
    static SetPiece settle(Sortie sortie, int command) {
        SetPiece boss = sortie.setPiece(0);
        for (int i = 0; i < SimStep.ticks(15) && !boss.engaged(); i++) {
            sortie.step(command);
        }
        if (!boss.engaged()) {
            throw new AssertionError("the Ascendant never settled");
        }
        return boss;
    }

    /** A bolt of {@code damage}, 10 a second from the nose, flying {@code degrees} off straight up (+: to the right). */
    static WeaponSpec bolt(double damage, double degrees) {
        return new WeaponSpec(
                "bolt",
                "pulse",
                "pulse",
                WeaponSpec.Delivery.BOLT,
                false,
                10,
                damage,
                900,
                new Hitbox(4, 12),
                TestSpecs.INFINITE,
                TestSpecs.INFINITE,
                1,
                0,
                0,
                Math.PI,
                0,
                0,
                List.of(TestSpecs.muzzle(0, 21, degrees)));
    }

    /** A rig that steps the boss alone, the ship where the test puts it, recording what it fires and its events. */
    static final class Rig implements BossActions {
        final SetPiece piece;
        int tick;
        double shipX = X;
        double shipY = 60;
        double random = 0.5;
        final List<SimEvents.Type> types = new ArrayList<>();
        final List<Integer> values = new ArrayList<>();
        /** This step's aimed shots and fans: {x, y}. */
        final List<double[]> shots = new ArrayList<>();
        /** This step's homing missiles: {x, y}. */
        final List<double[]> missiles = new ArrayList<>();
        /** This step's bay launches: {x, y, angle}. */
        final List<double[]> launches = new ArrayList<>();
        /** This step's lobs: {fromX, fromY, toX, toY}. */
        final List<double[]> lobs = new ArrayList<>();
        /** This step's bullets: {x, y, angle, speed}. */
        final List<double[]> bullets = new ArrayList<>();
        /** The drops so far: {pickup ordinal, x, y}. */
        final List<double[]> drops = new ArrayList<>();

        final List<String> cues = new ArrayList<>();
        final SplitMix64 draws = new SplitMix64(42);

        Rig(SetPieceSpec spec) {
            piece = new SetPiece(spec);
            tick = SimStep.ticks(ARRIVE) - 1;
        }

        void step() {
            types.clear();
            values.clear();
            shots.clear();
            missiles.clear();
            launches.clear();
            lobs.clear();
            bullets.clear();
            tick++;
            piece.update(tick);
            piece.act(shipX, shipY, this, true);
        }

        void steps(double seconds) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step();
            }
        }

        /** Steps until the boss settles. */
        Rig settled() {
            for (int i = 0; i < SimStep.ticks(15) && !piece.engaged(); i++) {
                step();
            }
            if (!piece.engaged()) {
                throw new AssertionError("never settled");
            }
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

        void kill(int p) {
            piece.damagePartFlat(p, piece.partHp(p) + 10_000);
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
            bullets.add(new double[] {x, y, angle, speed});
        }

        @Override
        public void lobAt(double x, double y, EnemyGun gun, double toX, double toY) {
            lobs.add(new double[] {x, y, toX, toY});
        }

        @Override
        public void drop(PickupType pickup, double x, double y) {
            drops.add(new double[] {pickup.ordinal(), x, y});
        }

        /** Kills every phase-1 part and steps into the eruption. */
        Rig toEruption() {
            for (int p : PHASE_1) {
                kill(p);
            }
            step();
            return this;
        }

        /** From the eruption, steps past its 4 s. */
        Rig pastEruption() {
            steps(4.05);
            return this;
        }

        @Override
        public void release(BossSpec.Stream stream, boolean left) {}

        @Override
        public void phase(int phase) {}

        @Override
        public void stationMissile(double x, double y, EnemyGun gun) {
            missiles.add(new double[] {x, y});
        }

        @Override
        public void bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {
            launches.add(new double[] {x, y, angle});
        }

        @Override
        public void ring(SimEvents.Type type, double x, double y, int value) {
            types.add(type);
            values.add(value);
        }

        @Override
        public void bossCue(String subject) {
            cues.add(subject);
        }

        @Override
        public double draw() {
            return draws.nextDouble();
        }
    }
}
