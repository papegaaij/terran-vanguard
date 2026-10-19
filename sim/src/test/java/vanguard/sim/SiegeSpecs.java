package vanguard.sim;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M7 part D's fixtures (design/campaign Level 17, Dome Siege; design/allies, Ares Landing dome): hand-built
 * domes, a hooked Spore Bomber, a mortar walker and a level of one section at 80 px/s with three domes: A at
 * x 130 entering at t=5, B at x 350 at t=14 and the larger Eden at x 240 at t=23.
 */
final class SiegeSpecs {
    static final double SPEED = 80;
    static final double LENGTH = 40;
    /** The bomb row, px below the top edge, and the slots' spacing. */
    static final double BOMB_ROW = 160;

    static final double BOMB_SLOT = 40;
    static final double A_X = 130;
    static final double A_T = 5;
    static final double B_X = 350;
    static final double B_T = 14;
    static final double EDEN_X = 240;
    static final double EDEN_T = 23;

    static final AllySpec DOME = dome(120, 3);
    static final AllySpec EDEN = dome(144, 3);

    /** The Spore Bomber at medium: HP 24, 45 px/s, a spore every 1.6 s. */
    static final EnemySpec BOMBER = new EnemySpec(
            "spore-bomber",
            24,
            new Hitbox(52, 56),
            Layer.LOW_AIR,
            10,
            false,
            25,
            45,
            Optional.empty(),
            Optional.of(45.0),
            Optional.empty(),
            Optional.empty(),
            Optional.of(new EnemyGun(
                    1.6,
                    0,
                    1,
                    90,
                    6,
                    false,
                    1,
                    0,
                    TestSpecs.INFINITE,
                    TestSpecs.INFINITE,
                    Optional.of(new EnemyGun.MineSpec(1, 8, 20, 1, 6, 4, true, 1)))),
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.of(new Range(100, 300)),
            Optional.empty());

    /** A walker with a mortar (a Shellback's vent, without its curl): 40 px/s, the Polyp Mortar's lob. */
    static final EnemySpec MORTAR_WALKER = new EnemySpec(
            "shellback",
            60,
            new Hitbox(60, 60),
            Layer.GROUND,
            15,
            false,
            40,
            40,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(CraterNestTest.MORTAR_GUN),
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(new EnemySpec.Walker(
                    40, Math.toRadians(90), 24, Math.toRadians(45), Optional.empty(), Math.toRadians(90))));

    /** M7 part D, step D3: the Choir Herald's aura at medium (design/enemies/air/choir-herald/data.yaml). */
    static final EnemySpec.Aura AURA = new EnemySpec.Aura(90, 40, 0.25, 2.0, 0.5, 1.0, 0.3, 0.8);

    /** Its song: a ring of 10 every 4.0 s (slow 100 px/s, small 4), the first 1.0 s into its hover. */
    static final EnemyGun SONG = new EnemyGun(
            4.0,
            1.0,
            1,
            100,
            4,
            false,
            10,
            2 * Math.PI * 9 / 10,
            TestSpecs.INFINITE,
            TestSpecs.INFINITE,
            Optional.empty(),
            Optional.empty());

    /**
     * The Choir Herald at medium: HP 60, in at 100 px/s, hovering 12 s 175 px below the top edge with its 32 px
     * drift over 10 s, then up and out; its song and its aura.
     */
    static final EnemySpec HERALD = new EnemySpec(
                    "choir-herald",
                    60,
                    new Hitbox(52, 48),
                    Layer.AIR,
                    10,
                    false,
                    30,
                    100,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new EnemySpec.Hover(new Range(12, 12), new Range(175, 175))),
                    Optional.empty(),
                    Optional.of(SONG),
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new EnemySpec.Sine(32, 10)),
                    Optional.empty(),
                    Optional.empty())
            .withAura(Optional.of(AURA));

    /** The Shellback's vent mortar at medium: every 4.0 s, marker 1.2 s, heavy 10, a ring of 8 small 4. */
    static final EnemyGun VENT_MORTAR = new EnemyGun(
            4.0,
            1.5,
            1,
            110,
            10,
            false,
            1,
            0,
            TestSpecs.INFINITE,
            TestSpecs.INFINITE,
            Optional.empty(),
            Optional.of(new EnemyGun.MortarSpec(1.2, 32, 8, 4)));

    /** Its vent: 24 HP, 28×28 at 30 px towards the tail, 12 credits, the mortar's; a body-first kill pays 40 + 8. */
    static final EnemySpec.UnitPart VENT = new EnemySpec.UnitPart("vent", 24, new Hitbox(28, 28), 0, 30, 12, true, 8);

    /** Its curl-roll: at 40 %, curl 0.6 s, roll 2.5 s at 140 px/s, uncurl 0.6 s, belly 1.5 s at ×1.5, a 72 px ball. */
    static final EnemySpec.Roll ROLL = new EnemySpec.Roll(0.4, 0.6, 2.5, 140, 0.6, 1.5, 1.5, new Hitbox(72, 72));

    /** The Shellback at medium (design/enemies/ground/shellback/data.yaml): body 120 HP, 40 credits, 24 px/s. */
    static final EnemySpec SHELLBACK = new EnemySpec(
                    "shellback",
                    120,
                    new Hitbox(84, 84),
                    Layer.GROUND,
                    15,
                    false,
                    40,
                    24,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(VENT_MORTAR),
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(
                            new EnemySpec.Walker(24, Math.toRadians(45), 32, 0, Optional.empty(), Math.toRadians(90))))
            .withPart(Optional.of(VENT))
            .withRoll(Optional.of(ROLL));

    private SiegeSpecs() {}

    /** One {@code walker} at {@code t} on the ground path through {@code path} (x, depth below the top edge). */
    static WaveSpec walker(double t, EnemySpec walker, WaveSpec.At... path) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.CONVOY,
                walker,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of(List.of(path)));
    }

    /** The unit on the field whose spec is {@code spec}; null when there is none. */
    static Enemy find(Sortie sortie, EnemySpec spec) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == spec) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** The index of {@code enemy} among the units in flight. */
    static int indexOf(Sortie sortie, Enemy enemy) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == enemy) {
                return i;
            }
        }
        throw new AssertionError("not in flight");
    }

    /** A dome: footprint {@code size}, hit box 32 px less, {@code hits} to break; bomb runs, lobs and rolls one hit each. */
    static AllySpec dome(double size, double hits) {
        return new AllySpec(
                "ares-landing-dome",
                new Hitbox(size, size),
                new Hitbox(size - 32, size - 32),
                hits,
                false,
                0,
                0.34,
                0,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                true,
                1,
                1);
    }

    /** The three domes with the hook {@code modes}. */
    static LevelScript.GroundAllies domes(Map<String, LevelScript.HookMode> modes) {
        return new LevelScript.GroundAllies(
                List.of(
                        new LevelScript.GroundAlly(DOME, "Bradbury", A_X, A_T),
                        new LevelScript.GroundAlly(DOME, "Lowell", B_X, B_T),
                        new LevelScript.GroundAlly(EDEN, "Eden", EDEN_X, EDEN_T)),
                modes,
                BOMB_ROW,
                BOMB_SLOT);
    }

    /** The bombers hooked {@code always}. */
    static LevelScript.GroundAllies hookedDomes() {
        return domes(Map.of("spore-bomber", LevelScript.HookMode.ALWAYS));
    }

    /** The level with these waves, ground units, secondary and ground allies. */
    static LevelScript level(
            List<WaveSpec> waves,
            List<LevelScript.GroundUnit> units,
            LevelScript.Secondary secondary,
            Optional<LevelScript.GroundAllies> allies) {
        return new LevelScript(
                        17,
                        3,
                        0,
                        List.of(new LevelScript.Section(LENGTH, SPEED)),
                        waves,
                        List.of(),
                        units,
                        0,
                        List.of(),
                        secondary,
                        List.of(),
                        List.of(),
                        List.of(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of(),
                        Optional.empty(),
                        List.of(),
                        List.of(),
                        List.of(),
                        Optional.empty(),
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        List.of(),
                        Optional.empty())
                .withGroundAllies(allies);
    }

    /** The level with the hooked domes and these waves, no secondary. */
    static LevelScript level(List<WaveSpec> waves) {
        return level(waves, List.of(), LevelScript.Secondary.NONE, Optional.of(hookedDomes()));
    }

    /** A wave of {@code count} bombers at {@code t}. */
    static WaveSpec bombers(
            double t, WaveSpec.Formation formation, int count, WaveSpec.Entry entry, WaveSpec.Edge edge) {
        return TestSpecs.wave(t, formation, BOMBER, count, entry, edge);
    }

    /** One mortar walker at {@code t}, walking straight down at {@code x}. */
    static WaveSpec walker(double t, double x) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.CONVOY,
                MORTAR_WALKER,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of(List.of(new WaveSpec.At(x, -40), new WaveSpec.At(x, 600))));
    }

    /** A sortie on the rules with the ship that nothing hits (the debug option), so it flies to the end. */
    static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Where a dome of footprint {@code size} entering at {@code enterT} stands at level time {@code t} (y up). */
    static double domeY(double enterT, double size, double t) {
        return PlayField.HEIGHT + size / 2 - SPEED * (t - enterT);
    }

    static void runTo(Sortie sortie, double seconds) {
        PartGSpecs.runTo(sortie, seconds);
    }

    static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }
}
