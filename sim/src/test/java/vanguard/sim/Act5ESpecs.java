package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M9 part E, step E1: the Level 32 fixtures the sim tests fly (the approved docs' numbers at medium): the Sentinel
 * Tower (its hardened collar round the emitter, its sweep all round with a full turn every third), the Strider (legs that
 * topple it, the pod carrying its homing salvos), a SAM-Nest-like turret, a mining charge, a cache's lock and a level
 * with one fork. Content builds the same from the data (E2).
 */
final class Act5ESpecs {
    private Act5ESpecs() {}

    /** The Sentinel's sweep: 120° over 1.6 s after a 0.8 s telegraph, 220 × 6 px, every 4.5 s, 1 s in, laser 8. */
    static final EnemySpec.Sweep SWEEP = new EnemySpec.Sweep(Math.toRadians(120), 1.6, 0.8, 220, 6, 4.5, 1.0, 8);

    /** The full turn every third sweep: 360° over 3.0 s after a 1.0 s ring telegraph, in a 6 s slot. */
    static final EnemySpec.GroundSweep FULL_TURNS = new EnemySpec.GroundSweep(3, 3.0, 1.0, 6.0);

    /** The hardened collar (72 × 72) round the 28 px emitter: 120 HP, bounty 40, a machine, scrolling with the ground. */
    static final EnemySpec COLLAR = EnemySpec.of(
                    "sentinel-tower", 120, new Hitbox(72, 72), Layer.GROUND, 0, false, 40, 0)
            .withTerrain(true)
            .withMachine(true)
            .withHardened(true)
            .withCore(Optional.of(new Hitbox(28, 28)));

    /** The Sentinel Tower: the collar with its sweep. */
    static final EnemySpec SENTINEL = sentinel(FULL_TURNS);

    static EnemySpec sentinel(EnemySpec.GroundSweep turns) {
        return COLLAR.withSweep(Optional.of(SWEEP)).withGroundSweep(Optional.of(turns));
    }

    /** The Strider's twin cannons: a fan of 3, 12° apart, at the ship every 2.4 s, 160 px/s, medium 6. */
    static final EnemyGun CANNONS =
            new EnemyGun(2.4, 1.0, 1, 160, 6, false, 3, Math.toRadians(12), Double.POSITIVE_INFINITY, Math.PI);

    /** The shoulder pod's homing salvo: 2 diamonds every 5 s (110 px/s, 80°/s, 2 HP, life 4 s, at most 8 its own). */
    static final EnemyGun POD = new EnemyGun(
                    5, 0, 2, 110, 6, false, 1, Math.toRadians(20), Math.toRadians(120), Math.PI)
            .withHoming(
                    Optional.of(new EnemyGun.HomingSpec(Math.toRadians(80), 4, 2, 8, 0.4, true).withPerShooter(true)));

    /** The left leg (80 HP, 22 × 40 at 42 px left of the heading; it topples the Strider), bounty 6. */
    static final EnemySpec.UnitPart LEFT_LEG =
            new EnemySpec.UnitPart("leg-left", 80, new Hitbox(22, 40), -42, 0, 6, false, 6, 0).withTopples();

    static final EnemySpec.UnitPart RIGHT_LEG =
            new EnemySpec.UnitPart("leg-right", 80, new Hitbox(22, 40), 42, 0, 6, false, 6, 0).withTopples();
    /** The shoulder pod (30 HP, 24 × 24 on the left shoulder; it carries the homing salvos), bounty 8. */
    static final EnemySpec.UnitPart POD_PART =
            new EnemySpec.UnitPart("pod", 30, new Hitbox(24, 24), -20, -18, 8, false, 8, 0).withSilencesSpit();

    /** The topple: a 0.6 s stagger, a 0.8 s fall, a 48 × 140 px fall line. */
    static final EnemySpec.Topple TOPPLE = new EnemySpec.Topple(0.6, 0.8, 48, 140);

    /** The Strider: hull 220 (64 × 64), bounty 40 (+ 6 + 6 + 8 for its parts: 60), walking 32 px/s, turning 45°/s. */
    static final EnemySpec STRIDER = EnemySpec.of("strider", 220, new Hitbox(64, 64), Layer.GROUND, 0, false, 40, 32)
            .withMachine(true)
            .withGun(Optional.of(CANNONS))
            .withWalker(Optional.of(new EnemySpec.Walker(32, Math.toRadians(45), 32, 0, Optional.of(POD), 0, true, 0)))
            .withToppleParts(List.of(LEFT_LEG, RIGHT_LEG, POD_PART), Optional.of(TOPPLE));

    /** A SAM-Nest-like turret: 20 HP, bounty 15, an aimed shot every 1 s, scrolling with the ground. */
    static final EnemySpec NEST = Act5BSpecs.NEST;

    /** A hardened ground turret (a Rail Bunker's place): 60 HP, bounty 30, no gun. */
    static final EnemySpec BUNKER = EnemySpec.of("rail-bunker", 60, new Hitbox(40, 40), Layer.GROUND, 0, false, 30, 0)
            .withTerrain(true)
            .withHardened(true);

    /** A hovering air target (a Talon's place): 32 HP, bounty 12, no gun. */
    static final EnemySpec FLYER = EnemySpec.of("talon", 32, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 120)
            .withTerrain(true);

    /** A mining charge at {@code t}, {@code x}: one hit (a sliver of HP), bursting 80 in 64 px, paying nothing. */
    static GroundObjectSpec charge(double t, double x) {
        return new GroundObjectSpec(t, x, new Hitbox(20, 20), 1e-6, 0, Optional.empty(), 0, 0, "", false, -1, 1)
                .withBlast(new GroundObjectSpec.Blast(64, 80));
    }

    /** A cache's blast door lock at {@code t}, {@code x}: 20 HP, freeing a crate of 50 without a secret. */
    static GroundObjectSpec lock(double t, double x) {
        return new GroundObjectSpec(t, x, new Hitbox(24, 24), 20, 0, Optional.empty(), 0, 50, "", false, -1, 1);
    }

    /** A bolt of {@code damage}, anti-ground or not, 10 a second from the nose. */
    static WeaponSpec bolt(double damage, boolean antiGround) {
        return new WeaponSpec(
                antiGround ? "mortar-bolt" : "bolt",
                "pulse",
                "pulse",
                WeaponSpec.Delivery.BOLT,
                antiGround,
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
                List.of(TestSpecs.muzzle(0, 21, 0)));
    }

    static Loadout loadout(WeaponSpec weapon) {
        return TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, weapon, weapon));
    }

    /** A slow level (10 px/s for 60 s) with these ground units, waves and objects. */
    static LevelScript slow(List<GroundUnit> units, List<WaveSpec> waves, List<GroundObjectSpec> objects) {
        return LevelScript.builder(32, 5)
                .sections(List.of(new Section(60, 10)))
                .waves(waves)
                .groundUnits(units)
                .groundObjects(objects)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    // ---- The fork ----

    /** The fork island's tip row: it reaches the top edge at 4 s (130 px/s). */
    static final double TIP = 520;

    /** A tunnel (walls at 10 and 470) with one island 165–315 px across from {@link #TIP} for 2,000 px. */
    static final Terrain FORK = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {10, 10})),
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {470, 470})),
            List.of(new Terrain.Island(
                    new double[] {215, 165, 165, 240, 315, 315, 265},
                    new double[] {TIP, TIP + 50, TIP + 2_000, TIP + 2_040, TIP + 2_000, TIP + 50, TIP})));

    /** The radio's branch lines. */
    static final RadioCue LEFT_LINE =
            new RadioCue(CueTrigger.BRANCH, 0, "1:left", "Okafor", "The old drift.", false, "neutral", "Okafor");

    static final RadioCue RIGHT_LINE =
            new RadioCue(CueTrigger.BRANCH, 0, "1:right", "Okafor", "The charge seam.", false, "neutral", "Okafor");

    static final Branch LEFT = new Branch(0, Branch.LEFT);
    static final Branch RIGHT = new Branch(0, Branch.RIGHT);

    /**
     * The fork level (130 px/s for 30 s): a Skitter wave in each channel at 9 s, a turret in each channel entering
     * before the choice (5 s) and after it (10 s), a charge and a cache's lock in each at 10 s, and a plain turret in
     * the open at 2 s.
     */
    static LevelScript forkLevel() {
        List<WaveSpec> waves = List.of(
                TestSpecs.wave(
                                9,
                                WaveSpec.Formation.SNAKE,
                                TestSpecs.SKITTER,
                                3,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .withBranch(LEFT),
                TestSpecs.wave(
                                9,
                                WaveSpec.Formation.SNAKE,
                                TestSpecs.SKITTER,
                                4,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .withBranch(RIGHT));
        List<GroundUnit> units = List.of(
                new GroundUnit(2, 240, NEST, -1),
                new GroundUnit(5, 90, NEST, -1).withBranch(LEFT),
                new GroundUnit(5, 390, NEST, -1).withBranch(RIGHT),
                new GroundUnit(10, 60, NEST, -1).withBranch(LEFT),
                new GroundUnit(10, 420, NEST, -1).withBranch(RIGHT));
        List<GroundObjectSpec> objects = List.of(
                lock(10, 90).withBranch(LEFT),
                lock(10, 390).withBranch(RIGHT),
                charge(10.5, 90).withBranch(LEFT),
                charge(10.5, 390).withBranch(RIGHT));
        GroundSpec ground = LevelScript.builder(32, 5)
                .sections(List.of(new Section(30, 130)))
                .objectives(ObjectiveSpec.NONE)
                .build()
                .ground()
                .withTerrain(Optional.of(new GroundSpec.TerrainSpec(FORK, 10, 28, 0.12, 0.5).withForks(List.of(0))));
        return LevelScript.builder(32, 5)
                .sections(List.of(new Section(30, 130)))
                .ground(ground)
                .waves(waves)
                .groundUnits(units)
                .groundObjects(objects)
                .radio(List.of(LEFT_LINE, RIGHT_LINE))
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    /** The commands that keep the ship in the {@code side} channel (left x ≈ 90, right x ≈ 390), firing or not. */
    static int steer(Sortie sortie, int side, boolean fire) {
        double goal = side == Branch.LEFT ? 90 : 390;
        int move = sortie.ship().x() < goal - 4
                ? Command.RIGHT.bit()
                : sortie.ship().x() > goal + 4 ? Command.LEFT.bit() : 0;
        return move | (fire ? Command.FIRE.bit() : 0);
    }

    /** The samples that set the part-E components (for the copy test). */
    static List<EnemySpec> samples() {
        return List.of(SENTINEL, STRIDER);
    }
}
