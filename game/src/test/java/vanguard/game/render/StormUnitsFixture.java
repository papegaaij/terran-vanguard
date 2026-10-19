package vanguard.game.render;

import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part E: Level 18's new units for the game's tests, as the content maps their stat blocks at medium
 * (design/enemies/ground/dust-devil, design/enemies/air/ghost-drone; content's {@code StormRules}): a Dust Devil
 * coming down the middle of a slow level (so it cycles on the screen a few times), and a Ghost Drone dropping into
 * its lane over the ship's column, its pass trigger firing. One section, flown with the starter loadout and the ship
 * invulnerable.
 */
public final class StormUnitsFixture {
    /** The devil's level scrolls slowly, so it stays on the screen for several cycles. */
    public static final double DEVIL_SPEED = 20;

    public static final double DRONE_SPEED = 60;
    public static final double LENGTH = 60;

    public static final EnemySpec DEVIL =
            SimSpecs.enemy(DomesFixture.CONTENT, "dust-devil", Difficulty.MEDIUM, Optional.empty());
    public static final EnemySpec DRONE =
            SimSpecs.enemy(DomesFixture.CONTENT, "ghost-drone", Difficulty.MEDIUM, Optional.empty());

    private StormUnitsFixture() {}

    private static Sortie sortie(EnemySpec enemy, double speed) {
        WaveSpec wave = new WaveSpec(
                0,
                WaveSpec.Formation.LINE_ABREAST,
                enemy,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of());
        LevelScript level = new LevelScript(
                18,
                3,
                0,
                List.of(new LevelScript.Section(LENGTH, speed)),
                List.of(wave),
                List.of(),
                List.of(),
                0,
                List.of(),
                new LevelScript.Secondary(0.8, 50),
                List.of());
        return new Sortie(
                1,
                SimSpecs.starterLoadout(DomesFixture.CONTENT, Difficulty.MEDIUM),
                level,
                SimSpecs.rules(DomesFixture.CONTENT, DomesFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }

    /** A Dust Devil at t=0 coming down the middle of the screen. */
    public static Sortie devil() {
        return sortie(DEVIL, DEVIL_SPEED);
    }

    /** A Ghost Drone at t=0 dropping into its lane over the ship's column. */
    public static Sortie drone() {
        return sortie(DRONE, DRONE_SPEED);
    }

    /** The fixture's unit in flight; null before it comes in and after it has gone. */
    public static Enemy find(Sortie sortie) {
        return sortie.enemyCount() == 0 ? null : sortie.enemy(0);
    }
}
