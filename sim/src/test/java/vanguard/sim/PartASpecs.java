package vanguard.sim;

import static vanguard.sim.TestSpecs.muzzle;

import java.util.List;

/**
 * M7 part A's test weapons with the approved numbers (round 36): the Ion Beam
 * (design/player/weapons/ion-beam), the Light Drone Bay (design/player/weapons/light-drone-bay, the
 * drone's behaviour in design/player/wingmen) and the Deflector Pod
 * (design/player/weapons/deflector-pod), per level 1–5 and the overdrive row (index 6), as {@code
 * vanguard.content.SimSpecs} is to build them from their data files. Each level's overdrive is the next
 * level's numbers, L5's the overdrive row.
 */
final class PartASpecs {
    private PartASpecs() {}

    /** The overdrive row's index in the per-level arrays (levels are 1–5). */
    static final int OD = 6;

    static final double[] BEAM_WIDTH = {0, 6, 8, 10, 12, 14, 18};
    static final double[] BEAM_DPS = {0, 45, 60, 80, 105, 130, 160};

    /** The front muzzle at the nose, 21 px above the ship's centre. */
    static final WeaponSpec.Muzzle NOSE = muzzle(0, 21, 0);

    static WeaponSpec ionBeam(int level) {
        return WeaponSpec.beam("ion-beam", "beam", "beam", BEAM_WIDTH[level], BEAM_DPS[level], NOSE);
    }

    static Armament.Mount beamMount(int level) {
        return new Armament.Mount(Armament.Slot.FRONT, ionBeam(level), ionBeam(level == 5 ? OD : level + 1));
    }

    static final double[] DRONE_RATE = {0, 6, 6, 6, 6, 6, 7};
    static final double[] DRONE_DAMAGE = {0, 1.7, 2.3, 3.2, 4, 5, 5};
    static final int[] DRONE_HITS = {0, 8, 9, 10, 11, 12, 12};
    static final double[] DRONE_REBUILD = {0, 6, 5.5, 5, 4.5, 4, 4};
    /** A wing pod's position: 16 px out, 3 px behind the ship's centre (the Swivel Gun's). */
    static final double POD_X = 16;

    static final double POD_Y = -3;

    static WeaponSpec droneGun(Armament.Slot side, int level) {
        double dx = side == Armament.Slot.LEFT_WING ? -POD_X : POD_X;
        return TestSpecs.bolt(
                "light-drone-bay",
                DRONE_RATE[level],
                DRONE_DAMAGE[level],
                900,
                new Hitbox(3, 8),
                TestSpecs.INFINITE,
                1,
                muzzle(dx, POD_Y, 0));
    }

    /** The light drone: orbit 36 px, 2.4 s a turn, convergence 140 px ahead, 0.3 s fade-in, contacts 4 / 2 hits and 10 damage. */
    static DroneSpec drone(int level) {
        return new DroneSpec(36, 2.4, 140, DRONE_HITS[level], DRONE_REBUILD[level], 0.3, 4, 2, 10);
    }

    static Armament.Mount droneBay(Armament.Slot side, int level) {
        int next = level == 5 ? OD : level + 1;
        return Armament.Mount.droneBay(side, droneGun(side, level), droneGun(side, next), drone(level), drone(next));
    }

    static final double[] DEFLECTOR_RADIUS = {0, 22, 24, 26, 28, 30, 32};
    static final int[] DEFLECTOR_CHARGES = {0, 3, 3, 4, 4, 5, 6};
    static final double[] DEFLECTOR_RECHARGE = {0, 0.67, 0.55, 0.5, 0.45, 0.4, 0.35};

    static DeflectorSpec deflector(int level) {
        return new DeflectorSpec(DEFLECTOR_RADIUS[level], DEFLECTOR_CHARGES[level], DEFLECTOR_RECHARGE[level]);
    }

    static Armament.Mount deflectorPod(Armament.Slot side, int level) {
        return Armament.Mount.deflector(side, deflector(level), deflector(level == 5 ? OD : level + 1));
    }

    static Sortie sortie(LevelScript level, Armament.Mount... mounts) {
        return new Sortie(1, TestSpecs.loadout(mounts), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** A level of units moving down with the scroll (130 px/s) from the top edge. */
    static LevelScript units(GroundUnit... units) {
        return units(List.of(), units);
    }

    static LevelScript units(List<GroundObjectSpec> ground, GroundUnit... units) {
        return LevelScript.builder(1, 1)
                .sections(List.of(new Section(60, 130)))
                .groundObjects(ground)
                .groundUnits(List.of(units))
                .secrets((int) ground.stream().filter(GroundObjectSpec::trigger).count())
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    static GroundUnit unit(double t, double x, EnemySpec spec) {
        return new GroundUnit(t, x, spec, -1);
    }

    /** A target that neither moves on its own nor fires, with {@code box} on {@code layer}. */
    static EnemySpec dummy(Layer layer, double hp, Hitbox box, boolean rammedAway) {
        return EnemySpec.of("dummy", hp, box, layer, 0, rammedAway, 0, 0).withTerrain(true);
    }

    static EnemySpec dummy(Layer layer, double hp) {
        return dummy(layer, hp, new Hitbox(20, 20), false);
    }
}
