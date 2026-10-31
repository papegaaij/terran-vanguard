package vanguard.game.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3a: Act 6's unit mechanics for the game's tests, as sim's {@code Act6ASpecs} builds them from the
 * approved unit docs at medium (design/enemies/air/mirror-interceptor, chimera; design/enemies/ground/graft-turret,
 * shield-pylon): a mirrored unit, a regenerating Chimera, a regrowing Graft Turret, a Shield Pylon with a unit under its
 * dome. They ride the ground (terrain), placed by time and x, on one section at {@value #SPEED} px/s, flown with the
 * starter loadout and the ship invulnerable; the stand-in sprites are the placeholders (none of them has art yet).
 */
public final class JovianFixture {
    public static final double SPEED = 30;
    /** The ship's column (its start), where its fire meets a unit. */
    public static final double COLUMN = PlayField.WIDTH / 2.0;

    /** The Mirror Interceptor's mirror: arc 120°, heat 130, 60°/s, down 3 s, regrow 0.4 s, gap 0.15 s, 110 px/s × 4. */
    public static final EnemySpec.Mirror MIRROR =
            new EnemySpec.Mirror(120, 130, 60, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30);

    public static final EnemySpec MIRRORED = EnemySpec.of(
                    "mirror-interceptor", 120, new Hitbox(52, 48), Layer.AIR, 15, false, 30, 0)
            .withTerrain(true)
            .withMachine(true)
            .withMirror(Optional.of(MIRROR));

    /** The Chimera: 40 HP, 20 % of its maximum a second after 1.0 s without a hit. */
    public static final EnemySpec CHIMERA = EnemySpec.of("chimera", 40, new Hitbox(28, 24), Layer.AIR, 10, true, 14, 0)
            .withTerrain(true)
            .withMachine(true)
            .withRegeneration(Optional.of(new EnemySpec.Regeneration(0.20, 1.0)));

    /** The Graft Turret's regrowth: window 5 s, knit 0.8 s, wake 0.5 s, its root 16 HP in 24×24, bonus 5. */
    public static final EnemySpec.Regrowth REGROWTH = new EnemySpec.Regrowth(5.0, 0.8, 0.5, 16, new Hitbox(24, 24), 5);

    public static final EnemySpec GRAFT = EnemySpec.of(
                    "graft-turret", 24, new Hitbox(32, 32), Layer.GROUND, 10, true, 15, 0)
            .withTerrain(true)
            .withMachine(true)
            .withRegrowth(Optional.of(REGROWTH));

    /** A Shield Pylon whose crown faces up the screen: its dome (r 110) 140 px above it, back over 0.3 s. */
    public static final EnemySpec PYLON = EnemySpec.of(
                    "shield-pylon", 240, new Hitbox(40, 40), Layer.GROUND, 0, false, 45, 0)
            .withTerrain(true)
            .withMachine(true)
            .withDome(Optional.of(new EnemySpec.Dome(110, 140, Math.PI, 2.0, 0.3)));

    /** A plain ground unit for the dome to cover. */
    public static final EnemySpec COVERED = EnemySpec.of(
                    "covered-target", 200, new Hitbox(30, 30), Layer.GROUND, 0, false, 10, 0)
            .withTerrain(true)
            .withMachine(true);

    private JovianFixture() {}

    private static Sortie sortie(List<GroundUnit> units) {
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(60, SPEED)))
                .groundUnits(units)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        return new Sortie(
                1,
                SimSpecs.starterLoadout(DomesFixture.CONTENT, Difficulty.MEDIUM),
                level,
                SimSpecs.rules(DomesFixture.CONTENT, DomesFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }

    private static GroundUnit at(double t, double x, EnemySpec spec) {
        return new GroundUnit(t, x, spec, -1);
    }

    /** M10 part A, step A3b: Plasma Arc L5 and a Reflex Shield on the ship's front, a pack of Chimeras riding down into its reach. */
    public static Sortie arcFlight() {
        Loadout loadout = SimSpecs.loadout(
                DomesFixture.CONTENT,
                DomesFixture.CONTENT.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "plasma-arc", 5)),
                "Reflex Shield",
                DomesFixture.CONTENT.armour().plating().get(1).name(),
                0,
                Difficulty.MEDIUM);
        List<GroundUnit> pack = new ArrayList<>();
        double x = COLUMN;
        for (int k = 0; k < 4; k++) {
            pack.add(new GroundUnit(k * 1.5, x + (k % 2 == 0 ? -30 : 30), CHIMERA, -1));
        }
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(60, SPEED)))
                .groundUnits(pack)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        return new Sortie(
                1,
                loadout,
                level,
                SimSpecs.rules(DomesFixture.CONTENT, DomesFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }

    /** A mirrored unit in the ship's column. */
    public static Sortie mirror() {
        return sortie(List.of(at(0, COLUMN, MIRRORED)));
    }

    /** A Chimera in the ship's column. */
    public static Sortie chimera() {
        return sortie(List.of(at(0, COLUMN, CHIMERA)));
    }

    /** A Graft Turret in the ship's column. */
    public static Sortie graft() {
        return sortie(List.of(at(0, COLUMN, GRAFT)));
    }

    /** A pylon left of the ship's column and a unit 140 px above it, in the middle of its dome. */
    public static Sortie dome() {
        double x = COLUMN - 120;
        return sortie(List.of(at(0, x, PYLON), at(140 / SPEED, x, COVERED)));
    }

    /** The unit of {@code spec} in flight; null when there is none. */
    public static Enemy find(Sortie sortie, EnemySpec spec) {
        return SiegeUnitsFixture.find(sortie, spec);
    }

    /** Steps with {@code commands} until {@code done} holds or {@code seconds} of level time ran; whether it held. */
    public static boolean runUntil(
            Sortie sortie, int commands, double seconds, java.util.function.BooleanSupplier done) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            sortie.step(commands);
            if (done.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    /** Steps without fire for {@code seconds}. */
    public static void idle(Sortie sortie, double seconds) {
        runUntil(sortie, Command.NONE, seconds, () -> false);
    }
}
