package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.audio.Sfx;
import vanguard.sim.Command;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Range;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part D: Level 17's new units for the game's tests while their mapping is being written (step D4), as sim's
 * {@code SiegeSpecs} builds them from their stat blocks at medium (design/enemies/air/choir-herald,
 * design/enemies/ground/shellback): the Choir Herald (HP 60, hovering 12 s 175 px below the top edge with its
 * 32 px drift, its song a ring of 10 every 4.0 s, its aura of 90 px) and the Shellback (body 120, its vent 24 at
 * the tail, its mortar every 4.0 s, its curl-roll at 40 %); a ground target of 1000 HP to carry a ring. One
 * section at {@value #SPEED} px/s ({@value #SHELLBACK_SPEED} for the Shellback), flown with the starter loadout and the ship invulnerable.
 */
public final class SiegeUnitsFixture {
    public static final double SPEED = 30;
    /** The Shellback's level scrolls slower, so its roll (2.5 s at 140 px/s) stays on the screen. */
    public static final double SHELLBACK_SPEED = 10;

    public static final double LENGTH = 60;
    private static final double INFINITE = Double.POSITIVE_INFINITY;

    public static final EnemySpec.Aura AURA = new EnemySpec.Aura(90, 40, 0.25, 2.0, 0.5, 1.0, 0.3, 0.8);
    /** The song: a ring of 10 (a fan over 9/10 of a turn) every 4.0 s, slow 100 px/s, small 4. */
    static final EnemyGun SONG = new EnemyGun(
            4.0,
            1.0,
            1,
            100,
            4,
            false,
            10,
            2 * Math.PI * 9 / 10,
            INFINITE,
            INFINITE,
            Optional.empty(),
            Optional.empty());

    public static final EnemySpec HERALD = new EnemySpec(
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

    static final EnemyGun VENT_MORTAR = new EnemyGun(
            4.0,
            1.5,
            1,
            110,
            10,
            false,
            1,
            0,
            INFINITE,
            INFINITE,
            Optional.empty(),
            Optional.of(new EnemyGun.MortarSpec(1.2, 32, 8, 4)));

    public static final EnemySpec.Roll ROLL = new EnemySpec.Roll(0.4, 0.6, 2.5, 140, 0.6, 1.5, 1.5, new Hitbox(72, 72));

    public static final EnemySpec SHELLBACK = new EnemySpec(
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
            .withPart(Optional.of(new EnemySpec.UnitPart("vent", 24, new Hitbox(28, 28), 0, 30, 12, true, 8)))
            .withRoll(Optional.of(ROLL));

    /** A ground target fixed to the ground that carries a ring: 1000 HP, a 24 px box, no gun. */
    public static final EnemySpec TARGET = new EnemySpec(
            "target",
            1000,
            new Hitbox(24, 24),
            Layer.GROUND,
            0,
            false,
            0,
            0,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    /** The target's x: 60 px right of the Herald's hover, out of the ship's column (its shots hit only the Herald). */
    public static final double TARGET_X = PlayField.WIDTH / 2.0 + 60;

    private SiegeUnitsFixture() {}

    private static LevelScript level(List<WaveSpec> waves, List<LevelScript.GroundUnit> units) {
        return level(waves, units, SPEED);
    }

    private static LevelScript level(List<WaveSpec> waves, List<LevelScript.GroundUnit> units, double speed) {
        return new LevelScript(
                17,
                3,
                0,
                List.of(new LevelScript.Section(LENGTH, speed)),
                waves,
                List.of(),
                units,
                0,
                List.of(),
                new LevelScript.Secondary(0.8, 50),
                List.of());
    }

    private static Sortie sortie(LevelScript level) {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(DomesFixture.CONTENT, Difficulty.MEDIUM),
                level,
                SimSpecs.rules(DomesFixture.CONTENT, DomesFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }

    /** A Herald at t=0 (it hovers over the ship's column) and the ground target entering beside it. */
    public static Sortie herald() {
        WaveSpec herald = new WaveSpec(
                0,
                WaveSpec.Formation.SINGLE,
                HERALD,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of());
        return sortie(level(List.of(herald), List.of(new LevelScript.GroundUnit(0, TARGET_X, TARGET, -1))));
    }

    /** A Shellback at t=0 walking straight down the ship's column. */
    public static Sortie shellback() {
        double x = PlayField.WIDTH / 2.0;
        WaveSpec walker = new WaveSpec(
                0,
                WaveSpec.Formation.CONVOY,
                SHELLBACK,
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
                List.of(List.of(new WaveSpec.At(x, -60), new WaveSpec.At(x, 900))));
        return sortie(level(List.of(walker), List.of(), SHELLBACK_SPEED));
    }

    /** The unit in flight of {@code spec}; null when there is none. */
    public static vanguard.sim.Enemy find(Sortie sortie, EnemySpec spec) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == spec) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** Steps the sortie without input until level time {@code seconds}. */
    public static void runTo(Sortie sortie, double seconds) {
        while (sortie.levelSeconds() < seconds && !sortie.complete()) {
            sortie.step(Command.NONE);
        }
    }

    /**
     * Looks without art for the sound tests, one per enemy kind of {@code sortie}: a kill plays the small
     * explosions and draws nothing.
     */
    public static EnemyLooks[] quietLooks(Sortie sortie) {
        EnemyLooks[] looks = new EnemyLooks[sortie.enemyKinds().size()];
        Array<AtlasRegion> none = new Array<>(0);
        EnemyLooks.DeathEffect noDeath = new EnemyLooks.DeathEffect(none, 1, 0);
        NavalLooks dry = new NavalLooks(
                none, none, none, none, none, none, none, none, new float[0][], none, none, none, new Array<>(0));
        for (int k = 0; k < looks.length; k++) {
            looks[k] = new EnemyLooks(
                    none,
                    1,
                    false,
                    10,
                    none,
                    Sfx.EXPLOSION_SMALL_A,
                    Sfx.EXPLOSION_SMALL_B,
                    none,
                    none,
                    noDeath,
                    noDeath,
                    none,
                    none,
                    1,
                    1,
                    0,
                    false,
                    none,
                    none,
                    none,
                    none,
                    none,
                    dry,
                    noDeath);
        }
        return looks;
    }
}
