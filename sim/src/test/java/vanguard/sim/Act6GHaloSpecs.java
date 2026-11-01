package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M10 part G, step G1b (design/enemies/ground/halo-platform; Level 41): a test Halo Platform at the design's medium
 * numbers, a held set piece on the ring engine. The core 60×60 (480 HP, vital, bounty 60) behind its barrier until the
 * third turret falls (its phase "The core" exposes it; the barrier fades over 0.5 s, the core's opening); one ring at
 * radius 96 turning 20°/s clockwise, +8°/s a turret lost (eased 0.8 s), with 6 turrets 34×34 (180 HP, bounty 10),
 * alternating gun (parts 1, 3, 5) and rail (2, 4, 6), the first straight down at rest; the fire arc 120° all round (no
 * clamp); a 1 s spin-up; cook-offs 0.15 s apart. The gun turrets an aimed burst of 3 (0.12 s apart, 160 px/s, 6)
 * every 2.4 s after a 0.3 s muzzle tell; the rail turrets the Rail Bunker's line ({@link Act5Specs#RAIL}: telegraph 1
 * s, holding within 100 px) every 4.5 s; the open core a ring of 10 (100 px/s, 4) every 3.0 s after a 0.5 s eye flare.
 * The armour patch drops at its death.
 */
final class Act6GHaloSpecs {
    private Act6GHaloSpecs() {}

    static final String SLUG = "halo-platform";
    static final int CORE = 0;
    static final List<Integer> TURRETS = List.of(1, 2, 3, 4, 5, 6);
    static final List<Integer> GUNS = List.of(1, 3, 5);
    static final List<Integer> RAILS = List.of(2, 4, 6);

    static final int GUN = 0;
    static final int RAIL = 1;
    static final int CORE_RING = 2;

    static final double SPIN_UP = 1;
    static final double HOVER_Y = 350;
    static final double TURRET_HP = 180;
    static final double CORE_HP = 480;

    /** The speed-ups: 20°/s + 8°/s a turret lost, as factors of 20°/s. */
    static final RingSpec.Losses LOSSES = new RingSpec.Losses(List.of(1.0, 1.4, 1.8, 2.2, 2.6, 3.0), 0.8);

    private static PartSpec turret(int k) {
        double a = Math.toRadians(60 * k);
        return new PartSpec(
                (k % 2 == 0 ? "gun-" : "rail-") + (k / 2 + 1),
                -96 * Math.sin(a),
                -96 * Math.cos(a),
                new Hitbox(34, 34),
                TURRET_HP,
                false,
                10,
                Optional.empty(),
                0,
                1);
    }

    private static BossSpec.Phase phase(
            String name, List<Integer> until, int left, List<Integer> attacks, List<Integer> exposes) {
        return new BossSpec.Phase(
                name,
                until,
                left,
                attacks,
                false,
                Optional.empty(),
                exposes,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                BossSpec.PhaseArena.NONE);
    }

    /** The rings: one, its batteries' attacks alternating, the losses' speed-up and the two tells. */
    static RingSpec rings() {
        return new RingSpec(
                        List.of(new RingSpec.Ring("ring", 96, 1, Math.toRadians(20), 0, TURRETS, GUN, Optional.empty())
                                .withAttacks(List.of(GUN, RAIL, GUN, RAIL, GUN, RAIL))),
                        Math.toRadians(120),
                        RingSpec.ALL_ROUND,
                        SPIN_UP,
                        1,
                        List.of(1.0),
                        List.of(0, 0),
                        0.15,
                        Optional.of(new RingSpec.Core(
                                CORE, 0, 0.5, Optional.empty(), -1, 1, 0, 0, -1, 1, Optional.empty())))
                .withLosses(Optional.of(LOSSES))
                .withTells(List.of(new RingSpec.Tell(GUN, 0.3), new RingSpec.Tell(CORE_RING, 0.5)));
    }

    /** The test Halo Platform arriving at {@code arrive} s, held; {@code spoofable}: its guns fooled by the spoofer. */
    static SetPieceSpec halo(double arrive, boolean spoofable) {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("core", 0, 0, new Hitbox(60, 60), CORE_HP, true, 60, Optional.empty(), 0, 1));
        for (int k = 0; k < 6; k++) {
            parts.add(turret(k));
        }
        List<BossSpec.Attack> attacks = List.of(
                new BossSpec.Attack(
                        "gun",
                        BossSpec.Pattern.AIMED,
                        EnemyGun.aimed(2.4, 0, 3, 160, 6, false),
                        0.12,
                        false,
                        1,
                        0,
                        0,
                        0,
                        GUNS,
                        false,
                        Optional.empty()),
                new BossSpec.Attack(
                                "rail",
                                BossSpec.Pattern.AIMED,
                                EnemyGun.aimed(4.5, 0, 1, 300, 10, false),
                                0,
                                false,
                                1,
                                0,
                                0,
                                0,
                                RAILS,
                                false,
                                Optional.empty())
                        .withLine(Act5Specs.RAIL),
                new BossSpec.Attack(
                        "core-ring",
                        BossSpec.Pattern.RING,
                        EnemyGun.aimed(3.0, 0, 1, 100, 4, false),
                        0,
                        false,
                        10,
                        0,
                        0,
                        0,
                        List.of(CORE)));
        List<Layer> layers = new ArrayList<>();
        for (int p = 0; p < parts.size(); p++) {
            layers.add(Layer.GROUND);
        }
        BossSpec boss = BossSpec.builder(
                        arrive,
                        240,
                        HOVER_Y,
                        0,
                        0,
                        1,
                        Layer.GROUND,
                        false,
                        "HALO PLATFORM",
                        60,
                        List.of(),
                        attacks,
                        List.of(
                                phase("The ring", TURRETS, 3, List.of(GUN, RAIL), List.of()),
                                phase("The core", List.of(), 0, List.of(GUN, RAIL, CORE_RING), List.of(CORE))))
                .deathSeconds(2)
                .arena(Optional.of(new BossSpec.Arena(BossSpec.Lanes.NONE, null, layers, List.of())))
                .held(true)
                .spoofable(spoofable)
                .build()
                .withRings(Optional.of(rings()));
        return new SetPieceSpec(
                SLUG,
                new Hitbox(240, 240),
                new Hitbox(60, 60),
                0,
                parts,
                List.of(),
                Optional.of(PickupType.ARMOUR_PATCH),
                Optional.of(boss));
    }

    /** The cues of {@link #level}: 0 the Halo's halt, 1 its fall, 2–4 the boss cues it never starts. */
    static final List<RadioCue> RADIO = List.of(
            Act6GSpecs.cue(CueTrigger.GATE_HALT, SLUG),
            Act6GSpecs.cue(CueTrigger.HALO_DESTROYED, ""),
            Act6GSpecs.cue(CueTrigger.BOSS_ARRIVAL, ""),
            Act6GSpecs.cue(CueTrigger.BOSS_PHASE, "The core"),
            Act6GSpecs.cue(CueTrigger.BOSS_DESTROYED, SLUG));

    /** {@link HeldGatesTest#SECTIONS}' two gates: gate 0 held by two clamps, gate 1 by the Halo arriving at 21.9 s. */
    static LevelScript level(boolean spoofable) {
        return LevelScript.builder(41, 6)
                .sections(HeldGatesTest.SECTIONS)
                .groundUnits(HeldGatesTest.holders().subList(0, 2))
                .setPieces(List.of(halo(21.9, spoofable)))
                .objectives(ObjectiveSpec.NONE)
                .radio(RADIO)
                .build();
    }

    /** A sortie of {@link #level}, the ship carrying the spoofer (0.5 s) or not. */
    static Sortie sortie(boolean spoofer) {
        Loadout loadout = spoofer ? TestSpecs.LOADOUT.withSpoofer(0.5) : TestSpecs.LOADOUT;
        return new Sortie(1, loadout, level(true), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** Steps the sortie through gate 0 (its clamps destroyed in its halt) into the Halo's halt. */
    static void untilHaloHalt(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(30) && !(sortie.halting() && sortie.gate() == 1); i++) {
            sortie.step(0);
            if (sortie.halting() && sortie.gate() == 0) {
                HeldArenaTest.killHolders(sortie);
            }
        }
        if (!(sortie.halting() && sortie.gate() == 1)) {
            throw new AssertionError("the Halo's halt within 30 s");
        }
    }

    /** Kills turret (or part) {@code p} outright, as the Lance's flat hits do. */
    static void kill(SetPiece halo, int p) {
        halo.damagePartFlat(p, halo.partHp(p) + 1);
    }

    /** The sortie-free rig of {@link Act5HSpecs.Rig}, recording the stale aims of held shots too. */
    static final class Rig extends Act5HSpecs.Rig {
        /** This step's held shots' aims: {x, y}. */
        final List<double[]> aims = new ArrayList<>();

        Rig(SetPieceSpec spec) {
            super(spec);
        }

        @Override
        void step() {
            aims.clear();
            super.step();
        }

        @Override
        public void aimedAt(double x, double y, EnemyGun gun, double atX, double atY) {
            aims.add(new double[] {atX, atY});
            aimed(x, y, gun);
        }

        /** Steps until the rings are up. */
        Rig up() {
            until(SimEvents.Type.RINGS_UP, SPIN_UP + 0.5);
            return this;
        }
    }
}
