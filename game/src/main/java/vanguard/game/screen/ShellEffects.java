package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SimEvents;

/**
 * M11 part A, step A3a (design/enemies, Window shields): the window shells' events on the play field. A hit spent on a
 * closed shell throws ART-A's pale gold glint and flakes where it landed ({@code shell-spark}, 0.2 s; the shared glance
 * until the art is in the level's atlas); a breaker hit or the EMP that breaks a shell open shatters it into gold-white
 * flakes round the unit ({@code shell-break}, 0.4 s; nothing without the art: the shell's look then just goes). The iris,
 * the regrowth and the glyphs are the shell look's own (the shells' state, {@code WindowShellLooks}); its opening and
 * closing only sound.
 */
final class ShellEffects {
    static final int SPARK_FRAME_TICKS = 2;
    static final int BREAK_FRAME_TICKS = 3;

    private final LevelRenderer renderer;
    private final Effects effects;
    private final Array<AtlasRegion> glance;

    ShellEffects(LevelRenderer renderer, Effects effects, Array<AtlasRegion> glance) {
        this.renderer = renderer;
        this.effects = effects;
        this.glance = glance;
    }

    /**
     * M11 part B, step B4 (design/enemies/space/gate-warden, death): a shelled unit's death shatters its shell ({@code
     * shell-break}) over its slabs.
     */
    void died(vanguard.sim.EnemySpec kind, double x, double y) {
        Array<AtlasRegion> shatter = renderer.vrell().shellBreak();
        if (shatter != null && kind.windowShield().isPresent()) {
            effects.start(shatter, BREAK_FRAME_TICKS, x, y);
        }
    }

    /** Event {@code i}, a shell's ({@code SHELL_FLAKE}, {@code _BROKEN}, {@code _OPEN}, {@code _CLOSE}, {@code _TICK}). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (events.type(i)) {
            case SHELL_FLAKE -> {
                Array<AtlasRegion> spark = renderer.vrell().shellSpark();
                effects.start(spark != null ? spark : glance, SPARK_FRAME_TICKS, x, y);
            }
            case SHELL_BROKEN -> {
                Array<AtlasRegion> shatter = renderer.vrell().shellBreak();
                if (shatter != null) {
                    effects.start(shatter, BREAK_FRAME_TICKS, x, y);
                }
            }
            default -> {}
        }
    }
}
