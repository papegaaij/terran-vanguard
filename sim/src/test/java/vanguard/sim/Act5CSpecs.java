package vanguard.sim;

import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M9 part C, step C1: the Level 30 fixtures the sim tests fly (the approved numbers of round 60: the Minelayer, its
 * line mines by C1 a and C2 a, the Gilded Gunship with its arcs and Dart streams, the Dart Drone, the Rail Serpent and
 * its machine split, the bomb rack under {@code ground: none}). Content builds the same from the data (C2).
 */
final class Act5CSpecs {
    private Act5CSpecs() {}

    /**
     * The line mine (design/enemies/space/minelayer): arms after 1 s, 1 HP, a ring of 8 slow 4s, pays 0; within 48 px
     * it blinks 0.6 s, then bursts with a 40 px heavy-10 blast; a shot one sets off the mines within 40 px, 0.08 s a hop.
     */
    static final EnemyGun.MineSpec LINE_MINE = new EnemyGun.MineSpec(1, 30, 0, 1, 8, 4, false, 0)
            .withLine(Optional.of(new EnemyGun.LineMine(48, 0.6, 40, 10, 40, 0.08)));

    /** The layer's gun: its line mines, their ring at 100 px/s, a touch medium 6. */
    static final EnemyGun MINE_GUN =
            new EnemyGun(1, 0, 1, 100, 6, false, 1, 0, INFINITE, INFINITE, Optional.of(LINE_MINE));

    /** The medium lay path and line: 120 px/s up a lane 70 px in, 150 px/s across; 3 × 4 mines, 32/56, a 112 gap. */
    static final EnemySpec.MineLayer LAY = new EnemySpec.MineLayer(120, 150, 70, 3, 4, 32, 56, 112, 0, 40);

    /** Easy's line: the middle cluster 3 mines, a 144 px gap. */
    static final EnemySpec.MineLayer EASY_LAY = new EnemySpec.MineLayer(120, 150, 70, 3, 4, 32, 56, 144, 1, 40);

    static EnemySpec minelayer(EnemySpec.MineLayer lay) {
        return EnemySpec.of("minelayer", 110, new Hitbox(64, 44), Layer.AIR, 15, false, 26, 120)
                .withGun(Optional.of(MINE_GUN))
                .withMachine(true)
                .withMineLayer(Optional.of(lay));
    }

    static final EnemySpec MINELAYER = minelayer(LAY);

    /** Gap points: left-centre and right-centre (Level 30's authored gaps). */
    static final double GAP_LEFT = 160;

    static final double GAP_RIGHT = 320;

    /** A column of {@code count} layers from the rear at {@code t}, entering from {@code edge}'s lane. */
    static WaveSpec layers(double t, EnemySpec layer, int count, WaveSpec.Edge edge, WaveSpec.MineLines lines) {
        return TestSpecs.wave(t, WaveSpec.Formation.COLUMN, layer, count, WaveSpec.Entry.REAR, edge)
                .withMineLines(lines);
    }

    /** The Dart Drone: tiny, 1 HP, contact 6, 240 px/s; launched it flies straight, then chases at 60°/s for 3 s. */
    static final EnemySpec DART = EnemySpec.of("dart-drone", 1, new Hitbox(12, 12), Layer.AIR, 6, true, 2, 240)
            .withChase(new EnemySpec.Chase(240, Math.toRadians(60), 3), Optional.empty())
            .withMachine(true);

    /** The Gunship's bays: streams of 3 every 5 s, 0.25 s apart, the first 1 s in; ±30°; straight 0.4 s; 6 alive. */
    static final EnemySpec.Launcher BAYS = new EnemySpec.Launcher(DART, 3, 5, 0.25, 1, 240, Math.toRadians(60), 0.4, 6);

    /** Its arcs: front ±45° × 0.5, engines ±60° × 2. */
    static final EnemySpec.FacingArmour ARCS =
            new EnemySpec.FacingArmour(Math.toRadians(45), 0.5, Math.toRadians(60), 2);

    /** Its ring: 12 slow bullets (110 px/s, small 4) every 3 s, the lamps 0.4 s before. */
    static final EnemyGun RING =
            new EnemyGun(3, 1.5, 1, 110, 4, false, 12, 2 * Math.PI * 11 / 12, INFINITE, INFINITE).withTell(0.4);

    /** The Gilded Gunship: 88 HP, its 12-point shield, its arcs, its ring and its bays, 45 px/s. */
    static final EnemySpec GUNSHIP = EnemySpec.of(
                    "gilded-gunship", 88, new Hitbox(64, 76), Layer.AIR, 15, false, 28, 45)
            .withShield(Optional.of(Act5Specs.shield(12)))
            .withFacingArmour(Optional.of(ARCS))
            .withGun(Optional.of(RING))
            .withLauncher(Optional.of(BAYS))
            .withMachine(true);

    /** A Gunship with neither gun nor bays (for the armour's numbers). */
    static final EnemySpec PLATED = EnemySpec.of("gilded-gunship", 88, new Hitbox(64, 76), Layer.AIR, 15, false, 28, 45)
            .withShield(Optional.of(Act5Specs.shield(12)))
            .withFacingArmour(Optional.of(ARCS));

    /** One Gunship straight down the field's middle (the ship's start x), from {@code t}. */
    static WaveSpec gunship(double t, EnemySpec gunship) {
        return TestSpecs.wave(t, WaveSpec.Formation.COLUMN, gunship, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    private static EnemySpec car(String slug, double hp, int bounty) {
        return EnemySpec.of(slug, hp, new Hitbox(34, 34), Layer.AIR, 10, false, bounty, 0)
                .withMachine(true);
    }

    /** A turret car's gun: an aimed shot every 2.4 s (160 px/s, small 4), its lamp 0.2 s before; staggered 0.6 s. */
    static final EnemyGun CAR_GUN = EnemyGun.aimed(2.4, 0.5, 1, 160, 4, false).withTell(0.2);

    /** The Rail Serpent's split: the rear drifts from 68 px/s (40 % of 170) to a stop over 1.5 s, then blows. */
    static final EnemySpec.Scatter WRECK = new EnemySpec.Scatter(68, 1.5, true);

    /**
     * A Rail Serpent of {@code cars} cars (the last its tail) at 170 px/s: engine 150 HP (40 credits), cars 16 HP (3),
     * turret cars every third 24 HP (5), the tail 20 HP (0, 10 more first); cars 0.85 × 48 px apart; pops 0.1 s.
     */
    static EnemySpec railSerpent(int cars) {
        List<Hitbox> boxes = new ArrayList<>();
        List<Double> offsets = new ArrayList<>(List.of(0.0));
        for (int i = 0; i < cars - 1; i++) {
            boxes.add(new Hitbox(34, 34));
        }
        for (int i = 1; i <= cars; i++) {
            offsets.add(offsets.get(i - 1) + 0.85 * (i == 1 ? 56 : 48));
        }
        EnemySpec.ChainSpec chain = new EnemySpec.ChainSpec(
                        car("rail-serpent-car", 16, 3),
                        boxes,
                        car("rail-serpent-tail", 20, 0),
                        10,
                        null,
                        0,
                        0,
                        offsets,
                        0.1,
                        1,
                        0)
                .withScatter(Optional.of(WRECK))
                .withCars(Optional.of(
                        new EnemySpec.Cars(car("rail-serpent-turret", 24, 5).withGun(Optional.of(CAR_GUN)), 3, 0.6)));
        return EnemySpec.of("rail-serpent", 150, new Hitbox(45, 45), Layer.AIR, 20, false, 40, 170)
                .withMachine(true)
                .withChain(Optional.of(chain));
    }

    static final EnemySpec SERPENT = railSerpent(10);

    /** A bomb rack on the left wing: dropped bombs onto the ground. */
    static final WeaponSpec BOMB_RACK = new WeaponSpec(
            "bomb-rack",
            "bomb",
            "bomb",
            WeaponSpec.Delivery.DROPPED,
            true,
            1.2,
            10,
            0,
            new Hitbox(8, 12),
            0,
            INFINITE,
            1,
            20,
            0,
            Math.PI,
            0.5,
            0,
            List.of(muzzle(-16, -3, 0)));

    /** The samples that set the part-C components (for the copy test). */
    static List<EnemySpec> samples() {
        return List.of(MINELAYER, GUNSHIP, DART, SERPENT);
    }
}
