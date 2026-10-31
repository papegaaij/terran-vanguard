package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part A, step A1b: the Act 6 player and hazard side's fixtures, after the approved docs at medium
 * (design/player/weapons/plasma-arc, design/player/shields Reflex Shield (Act 6), design/world/jovian Hazards & set
 * pieces); the content's own fixtures are step A2b's.
 */
final class Act6BSpecs {
    private Act6BSpecs() {}

    /** The Plasma Arc L1's chain: 3 strikes, jumps of 90 px, × 0.75 a jump. */
    static final WeaponSpec.Chain CHAIN = new WeaponSpec.Chain(3, 90, 0.75);

    /** The Plasma Arc L1: 4 discharges a second, 7.5 a first strike, reach 200 px, a 90° cone from the nose. */
    static final WeaponSpec PLASMA_ARC = WeaponSpec.chain(
            "plasma-arc", "arc", "tesla", 4, 7.5, 200, Math.toRadians(90), CHAIN, TestSpecs.muzzle(0, 21, 0));

    static Armament.Mount arcMount() {
        return new Armament.Mount(Armament.Slot.FRONT, PLASMA_ARC, PLASMA_ARC);
    }

    /** The Reflex Shield: 40, 6/s after 1.0 s; reflects at 75 %, half the damage, bolts at 640 px/s × 2. */
    static final ShieldModel REFLEX =
            new ShieldModel(40, 6, 1.0, 1.0).withReflect(new ShieldModel.Reflect(0.75, 0.5, 640, 2));

    /** The world's lightning numbers: told 1.0 s, live 0.25 s, 40 px wide, 10 to the ship, 60 to the air units. */
    static HazardSpec.Strikes strikes(double clearance, HazardSpec.Strike... at) {
        return new HazardSpec.Strikes(List.of(at), 1.0, 0.25, 40, 10, 60, clearance, 1.0);
    }

    /** Calm weather (no gust, drift or flash) with {@code strikes}. */
    static HazardSpec.Weather storm(HazardSpec.Strikes strikes) {
        return new HazardSpec.Weather(List.of(), 2.5, 0.25, 0, 0, List.of(), List.of()).withStrikes(strikes);
    }

    /** The world's eruption numbers: tell 1.2 s, rise 0.5 s, fly 2.5 s, fall 0.4 s, a 100° fan, 120 → 60 px/s. */
    static HazardSpec.EruptionSpec eruptions(HazardSpec.Vent... vents) {
        return new HazardSpec.EruptionSpec(
                List.of(vents), 1.2, 0.5, 2.5, 0.4, Math.toRadians(100), 120, 60, new Hitbox(16, 16), 6, 10);
    }

    /** A level of {@code seconds} at 130 px/s with {@code hazards}, its ground units and waves. */
    static LevelScript level(double seconds, HazardSpec hazards, List<GroundUnit> units, List<WaveSpec> waves) {
        return LevelScript.builder(1, 1)
                .sections(List.of(new Section(seconds, 130)))
                .hazards(hazards)
                .groundUnits(units)
                .waves(waves)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    /** A level of {@code seconds} with the strikes {@code strikes} and {@code units}. */
    static LevelScript storm(double seconds, HazardSpec.Strikes strikes, List<GroundUnit> units, List<WaveSpec> waves) {
        return level(seconds, HazardSpec.NONE.withWeather(Optional.of(storm(strikes))), units, waves);
    }

    /** A plain target riding the ground on {@code layer}: {@code hp}, bounty 10. */
    static EnemySpec target(String slug, Layer layer, double hp) {
        return EnemySpec.of(slug, hp, new Hitbox(20, 20), layer, 0, false, 10, 0)
                .withTerrain(true);
    }

    /** A sortie of {@code level} with {@code loadout}; the ship is hit as in play. */
    static Sortie sortie(LevelScript level, Loadout loadout) {
        return new Sortie(1, loadout, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** How many {@code type} events the last step raised. */
    static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }

    /** Steps {@code steps} times with {@code commands}; returns the {@code type} events raised. */
    static int run(Sortie sortie, int steps, int commands, SimEvents.Type type) {
        int n = 0;
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
            n += sortie.events().count(type);
        }
        return n;
    }

    /** The unit of {@code slug}; null when it is gone. */
    static Enemy find(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }
}
