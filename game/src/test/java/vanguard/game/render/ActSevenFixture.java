package vanguard.game.render;

import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Range;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M11 part A, step A3a: Act 7's unit mechanics for the game's tests, as sim's {@code Act7ASpecs} builds them from the
 * approved docs at medium (design/enemies, Window shields and Distorted space; design/enemies/space/gate-warden,
 * rift-skater): a window-shelled unit with a straight line in the ship's column, a hovering Gate Warden with its exit up
 * and leaving tell, a teleporting Rift Skater and a level whose current drifts at a steady angle with a reversal. Flown
 * with the starter loadout and the ship invulnerable; the stand-in sprites are the placeholders (none has art yet).
 */
public final class ActSevenFixture {
    /** The ship's column (its start), where its fire meets a unit. */
    public static final double COLUMN = PlayField.WIDTH / 2.0;

    /** The window shield: open 3 s on a breaker hit or the EMP, regrowing 0.4 s, the iris 0.15 s, a tick per 0.15 s. */
    public static final EnemySpec.WindowShield SHELL = new EnemySpec.WindowShield(3.0, 0.4, 0.15, 0.15);

    /** The Gate Warden's line: 0.8 s telegraph, 1.0 s live, 10 px, heavy 10, every 4.0 s, its first after {@code first}. */
    static EnemySpec.LaserLine line(boolean straight, double first) {
        return new EnemySpec.LaserLine(0.8, 1.0, 10, 10, 4.0, first, 0)
                .withMostTelegraphing(2)
                .withStraight(straight);
    }

    /** A shelled unit riding the ground in the ship's column, its straight line first 1.5 s after it is on. */
    public static final EnemySpec SHELLED = EnemySpec.of(
                    "gate-warden", 360, new Hitbox(56, 60), Layer.AIR, 15, false, 40, 0)
            .withTerrain(true)
            .withLaserLine(Optional.of(line(true, 1.5)))
            .withWindowShield(Optional.of(SHELL));

    /** A Gate Warden holding its station 140 px below the top edge for {@code hold} s, then rising out at 90 px/s. */
    public static EnemySpec wardenSpec(double hold) {
        return EnemySpec.of("gate-warden", 360, new Hitbox(56, 60), Layer.AIR, 15, false, 40, 90)
                .withHover(Optional.of(
                        new EnemySpec.Hover(new Range(hold, hold), new Range(140, 140)).withExitUp(90, 2.0)))
                .withLaserLine(Optional.of(line(true, 0.5)))
                .withWindowShield(Optional.of(SHELL));
    }

    /** The Rift Skater's teleport: fade 0.2 s, ring 0.5 s, 120 px off the ship, 3 jumps of 90-130 px. */
    public static final EnemySpec.Teleport JUMPS =
            new EnemySpec.Teleport(0.2, 0.5, 120, new Range(90, 130), new Range(30, 60), 1.2, 1.0, 3, 0.25, 4);

    /** A Rift Skater: 32 HP, 26×20, 140 px/s gliding. */
    public static final EnemySpec SKATER = EnemySpec.of(
                    "rift-skater", 32, new Hitbox(26, 20), Layer.AIR, 8, true, 14, 140)
            .withTeleport(Optional.of(JUMPS));

    private ActSevenFixture() {}

    private static Sortie sortie(LevelScript level) {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(DomesFixture.CONTENT, Difficulty.MEDIUM),
                level,
                SimSpecs.rules(DomesFixture.CONTENT, DomesFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }

    private static LevelScript.Builder level(double speed) {
        return LevelScript.builder(1, 1)
                .sections(List.of(new Section(60, speed)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)));
    }

    private static WaveSpec single(double t, EnemySpec spec) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, spec, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .build();
    }

    /** A shelled unit riding down the ship's column. */
    public static Sortie shelled() {
        return sortie(level(30)
                .groundUnits(List.of(new GroundUnit(0, COLUMN, SHELLED, -1)))
                .build());
    }

    /** A Gate Warden entering at once, holding {@code hold} s. */
    public static Sortie warden(double hold) {
        return sortie(level(130).waves(List.of(single(0, wardenSpec(hold)))).build());
    }

    /** A Rift Skater entering at once. */
    public static Sortie skater() {
        return sortie(level(130).waves(List.of(single(0, SKATER))).build());
    }

    /** The current of {@link #drift}: 20° to the right, a reversal at 6 s run 3 s, told 2 s ahead. */
    public static final HazardSpec.Current CURRENT =
            new HazardSpec.Current(List.of(new HazardSpec.Bend(0, 20)), List.of(new HazardSpec.Reversal(6, 3)), 2.0);

    /** A level at 130 px/s whose current drifts at {@link #CURRENT}'s angle (no units). */
    public static Sortie drift() {
        return sortie(level(130)
                .hazards(HazardSpec.NONE.withCurrent(Optional.of(CURRENT)))
                .build());
    }

    /** The first unit of {@code slug} in the sortie now; null for none. */
    public static Enemy find(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }
}
