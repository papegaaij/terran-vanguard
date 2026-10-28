package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock;
import vanguard.sim.SimStep;

/**
 * M9 part A (design/world/belt, Vacuum rules; design/art-direction, the vacuum explosion set; tools/art/explosions.py
 * {@code --vacuum}, round 61 a "flash and ring" written until the pick): a kill where the level's section is in vacuum
 * ({@code medium: vacuum}). In place of its air rung's fireball the vacuum burst of the same rung ({@code
 * explosion-vacuum-<rung>}, additive: a short flash and a thin ring) over its debris thrown out in a ring ({@code
 * explosion-vacuum-debris-<rung>}, solid, the same frames' timing): no smoke. A medium or large unit leaves its wreck:
 * its hull darkened, drifting off with the scroll and slowly turning rather than falling, fading over its last second
 * ({@link #startWreck}, drawn with the death pieces).
 */
public final class VacuumDeaths {
    /** The rungs of the ladder, as the air ladder picks them for a kind ({@link EnemyLooks#explosion}). */
    static final String[] RUNGS = DeepDeaths.RUNGS;
    /** The smallest rung that leaves a drifting wreck (medium). */
    static final int WRECK_RUNG = 2;
    /** A wreck drifts this long, fading over the last {@value #WRECK_FADE_SECONDS} s. */
    static final double WRECK_SECONDS = 3.0;

    static final double WRECK_FADE_SECONDS = 1.0;
    /** A wreck turns at up to this many degrees a second (its sign and share from where it died). */
    static final double WRECK_SPIN = 40;
    /** A wreck drifts across at up to this many px a second (the burst's push). */
    static final double WRECK_DRIFT = 12;
    /** A wreck's hull is darkened to this share of its colour (charred). */
    static final float WRECK_SHADE = 0.4f;

    private static final int WRECKS = 16;

    private final Sprites sprites;
    private final LevelData level;
    private final boolean anyVacuum;
    private final List<Array<AtlasRegion>> bursts = new ArrayList<>();
    private final List<Array<AtlasRegion>> debris = new ArrayList<>();
    /** The wrecks: frame (null free), start step, where (y with the ground scroll at the start), turn, drift. */
    private final AtlasRegion[] wreck = new AtlasRegion[WRECKS];

    private final long[] wreckTick = new long[WRECKS];
    private final double[] wreckX = new double[WRECKS];
    private final double[] wreckY = new double[WRECKS];
    private final double[] wreckSpin = new double[WRECKS];
    private final double[] wreckDrift = new double[WRECKS];
    private int nextWreck;

    VacuumDeaths(Sprites sprites, LevelData level) {
        this.sprites = sprites;
        this.level = level;
        boolean vacuum = false;
        for (int i = 0; i < level.sections().size(); i++) {
            vacuum |= level.mediumOf(i) == SectionsBlock.Medium.VACUUM;
        }
        anyVacuum = vacuum;
        for (String rung : RUNGS) {
            bursts.add(optional("explosion-vacuum-" + rung));
            debris.add(optional("explosion-vacuum-debris-" + rung));
        }
    }

    private Array<AtlasRegion> optional(String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether a kill at level time {@code seconds} dies in vacuum: its section's medium, with the bursts' art. */
    public boolean at(double seconds) {
        if (!anyVacuum || bursts.getFirst().isEmpty()) {
            return false;
        }
        int section = level.sectionAt(seconds);
        int index = section == 0 ? level.sections().size() - 1 : section - 1;
        return level.mediumOf(index) == SectionsBlock.Medium.VACUUM;
    }

    /** The rung (an index of {@link #RUNGS}) of a kind whose air burst is {@code explosion}. */
    int rung(Array<AtlasRegion> explosion) {
        if (explosion == sprites.explosionTiny) {
            return 0;
        }
        if (explosion == sprites.explosionSmall) {
            return 1;
        }
        return explosion == sprites.explosionLarge ? 3 : 2;
    }

    /** The vacuum burst of {@code look}'s rung (additive: the flash and the ring). */
    public Array<AtlasRegion> burst(EnemyLooks look) {
        return bursts.get(rung(look.explosion()));
    }

    /** The debris of {@code look}'s rung (solid, thrown out in a ring). */
    public Array<AtlasRegion> debris(EnemyLooks look) {
        return debris.get(rung(look.explosion()));
    }

    /** Whether {@code look}'s unit leaves a drifting wreck (a medium or large rung, with frames to show). */
    public boolean wrecks(EnemyLooks look) {
        return rung(look.explosion()) >= WRECK_RUNG && !look.frames().isEmpty();
    }

    /** The level restarts: no wreck drifts. */
    public void restart() {
        java.util.Arrays.fill(wreck, null);
        nextWreck = 0;
    }

    /**
     * A unit of {@code look} died at (x, y) at step {@code tick}: its wreck (its first frame) drifts off with the ground
     * scroll ({@code groundScroll} now), turning; nothing for a small unit.
     */
    public void startWreck(EnemyLooks look, double x, double y, long tick, double groundScroll) {
        if (!wrecks(look)) {
            return;
        }
        int w = nextWreck;
        nextWreck = (nextWreck + 1) % WRECKS;
        wreck[w] = look.frames().get(look.frames().size / 2);
        wreckTick[w] = tick;
        wreckX[w] = x;
        wreckY[w] = y + groundScroll;
        // Its turn and push from where it died: deterministic, no random draw.
        double u = Math.sin(x * 12.9898 + y * 78.233) * 43758.5453;
        u -= Math.floor(u);
        wreckSpin[w] = (2 * u - 1) * WRECK_SPIN;
        wreckDrift[w] = (x < vanguard.sim.PlayField.WIDTH / 2.0 ? -1 : 1) * WRECK_DRIFT * (0.4 + 0.6 * u);
    }

    /** A wreck's opacity {@code seconds} after its death: whole, then fading over its last second; 0 once gone. */
    static float wreckOpacity(double seconds) {
        if (seconds < 0 || seconds >= WRECK_SECONDS) {
            return 0;
        }
        double left = WRECK_SECONDS - seconds;
        return (float) Math.min(1, left / WRECK_FADE_SECONDS);
    }

    /** With the death pieces: every wreck drifting at its turn, darkened, at the drawn moment. */
    public void drawWrecks(SpriteBatch batch, long tick, float alpha, double groundScroll) {
        for (int w = 0; w < WRECKS; w++) {
            if (wreck[w] == null) {
                continue;
            }
            double seconds = (tick - wreckTick[w] + alpha) * SimStep.SECONDS;
            float opacity = wreckOpacity(seconds);
            if (opacity <= 0) {
                if (seconds >= WRECK_SECONDS) {
                    wreck[w] = null;
                }
                continue;
            }
            AtlasRegion region = wreck[w];
            float width = region.getRegionWidth();
            float height = region.getRegionHeight();
            double x = wreckX[w] + wreckDrift[w] * seconds;
            double y = wreckY[w] - groundScroll;
            batch.setColor(WRECK_SHADE, WRECK_SHADE, WRECK_SHADE, opacity);
            batch.draw(
                    region,
                    (float) (PixelScreen.PLAY_FIELD_X + x - width / 2),
                    (float) (y - height / 2),
                    width / 2,
                    height / 2,
                    width,
                    height,
                    1,
                    1,
                    (float) (wreckSpin[w] * seconds));
        }
        batch.setColor(Color.WHITE);
    }
}
