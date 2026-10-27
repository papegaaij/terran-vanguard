package vanguard.sim;

import static vanguard.sim.TestSpecs.INFINITE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M5 part G's fixtures (design/campaign Level 13, Polar Relay): the Nansen Relay, the Skimmer, the
 * hooked Stinger, Scuttler and Polyp Mortar with the stat blocks' numbers at medium, and a level of
 * Level 13's shape: the approach at 140 px/s to t=40 with the 2 s ease, the timed halt to t=220 and the
 * exit to t=235, the relay standing at (240, 290 y-up) from t=40 and synchronising until t=218.
 */
final class PartGSpecs {
    static final double RELAY_X = 240;
    static final double RELAY_Y = 290;
    static final double SYNC_START = 40;
    static final double SYNC_SECONDS = 178;
    static final double APPROACH = 140;

    static final AllySpec RELAY = relay(600);

    /** The Skimmer's aimed shot: every 1.8 s at 190 px/s, `small` = 4 (hard: a 2-shot burst). */
    static final EnemyGun SKIMMER_GUN = EnemyGun.aimed(1.8, 0.5, 1, 190, 4, false);

    static final EnemySpec SKIMMER = skimmer(SKIMMER_GUN);
    static final EnemyGun SCUTTLER_FAN =
            new EnemyGun(2.8, 0, 1, 140, 4, false, 5, Math.toRadians(60), INFINITE, INFINITE);
    static final EnemyGun SCUTTLER_SPIT = EnemyGun.aimed(4, 0, 1, 120, 6, false);
    static final EnemySpec SCUTTLER = scuttler();

    private PartGSpecs() {}

    /** The relay: footprint 96×96, hit box 80×80, {@code hp}, relay-aimed hits, dives of 10, lobs. */
    static AllySpec relay(double hp) {
        return new AllySpec(
                "nansen-relay",
                new Hitbox(96, 96),
                new Hitbox(80, 80),
                hp,
                true,
                0,
                0.5,
                0,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                10,
                true);
    }

    /** The Skimmer: HP 5, hit box 22×32, `ground`, 180 px/s, its weave 40 px every 1.2 s, its gun. */
    static EnemySpec skimmer(EnemyGun gun) {
        return EnemySpec.of("skimmer", 5, new Hitbox(22, 32), Layer.GROUND, 10, false, 14, 180)
                .withGun(Optional.of(gun))
                .withSine(Optional.of(new EnemySpec.Sine(40, 1.2)));
    }

    /** A Skimmer without its weave, to follow its lane exactly. */
    static EnemySpec straightSkimmer() {
        return EnemySpec.of("skimmer", 5, new Hitbox(22, 32), Layer.GROUND, 10, false, 14, 180)
                .withGun(Optional.of(SKIMMER_GUN));
    }

    private static EnemySpec scuttler() {
        return EnemySpec.of("scuttler", 28, new Hitbox(46, 40), Layer.GROUND, 15, false, 25, 40)
                .withGun(Optional.of(SCUTTLER_FAN))
                .withWalker(Optional.of(new EnemySpec.Walker(
                        40,
                        Math.toRadians(90),
                        24,
                        Math.toRadians(45),
                        Optional.of(SCUTTLER_SPIT),
                        Math.toRadians(90))));
    }

    /** Level 13's sections: the approach to 40 s, the timed halt to 220 s (2 s eases), the exit to 235 s. */
    static List<Section> sections() {
        return List.of(new Section(40, APPROACH), new Section(220, 0, false, 2), new Section(235, APPROACH));
    }

    /** The relay's defend primary with the hook {@code modes}. */
    static ObjectiveSpec.Defend defend(AllySpec ally, Map<String, HookMode> modes) {
        return new ObjectiveSpec.Defend(ally, RELAY_X, RELAY_Y, SYNC_START, SYNC_SECONDS, modes);
    }

    /** A wave of {@code count} {@code enemy} at {@code t} on {@code lanes} (lane walkers). */
    static WaveSpec lanes(
            double t, WaveSpec.Formation formation, EnemySpec enemy, int count, List<List<WaveSpec.At>> lanes) {
        return TestSpecs.wave(t, formation, enemy, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .withLanes(lanes);
    }

    /** A level of Level 13's shape with these waves, ground units, radio and objectives, nothing else. */
    static LevelScript level(
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundUnit> units,
            List<RadioCue> radio,
            Optional<ObjectiveSpec.Defend> defend,
            Secondary secondary,
            List<DropSpec.PlacedPickup> placed) {
        return LevelScript.builder(13, 2)
                .sections(sections)
                .waves(waves)
                .groundUnits(units)
                .radio(radio)
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary).withDefend(defend))
                .ground(GroundSpec.NONE.withWater(true))
                .drops(DropSpec.NONE.withPlacedPickups(placed))
                .build();
    }

    /** Level 13's shape with the relay hooked by {@code modes}, the waves and units, no radio or secondary. */
    static LevelScript level(Map<String, HookMode> modes, List<WaveSpec> waves, List<GroundUnit> units) {
        return level(sections(), waves, units, List.of(), Optional.of(defend(RELAY, modes)), Secondary.NONE, List.of());
    }

    static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** A sortie on the rules with the ship that nothing hits (the debug option). */
    static Sortie untouchable(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the level clock reaches {@code seconds}. */
    static void runTo(Sortie sortie, double seconds) {
        while (sortie.levelSeconds() < seconds - 1e-9) {
            sortie.step(Command.NONE);
        }
    }

    /** Steps {@code seconds} of steps. */
    static void run(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.NONE);
        }
    }

    /** How many events of {@code type} the last step raised. */
    static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }
}
