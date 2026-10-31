package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.game.render.BackdropTiles.Tiles;
import vanguard.sim.PlayField;

/**
 * RF9d: the {@link Backdrop}'s atmosphere: per atmosphere stretch its look (low-air cloud banks, high-air wisps, haze
 * over the deep and far layers), which ramps across a stretch boundary ({@link #blend}); M5 part C's collapse dust peak
 * over it; M5 part H's high-air strength ({@link #frontFade}); M10 part A's foreground cloud banks over the {@code air}
 * bodies ({@link ForegroundBanks}, null without them). Nothing is allocated per frame.
 */
final class BackdropAtmosphere {
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    /** M5 part H: the high-air layer's strength ({@link #frontFade}), 1 by default. */
    private float frontFade = 1;

    private final LevelData level;
    private final double ramp;
    /** The atmosphere along the level, each with its look in {@link #looks} (M5 part G: easy's, {@link #easy}). */
    private List<LevelData.Stretch> stretches;

    private final TextureRegion pixel;
    private final Color haze;
    private final BackdropTiles tiles;

    private final Look[] looks;
    /** M5 part C: the collapse's dust peak's look (Level 09's {@code heavy}); null without a collapse. */
    private final Look dust;
    /** How strongly the dust peak shows now, 0 to 1 ({@link #dustPeak}). */
    private float dustWeight;

    /** M10 part A: the level's foreground cloud banks (Level 36); null without them. */
    private final ForegroundBanks banks;

    private Look from;
    private Look to;
    private float weight;

    private record Look(Tiles banks, Tiles wisps, float haze) {}

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     * @param tiles the layer tiles, which draw a look's tile sets
     */
    BackdropAtmosphere(Sprites sprites, LevelData level, String folder, BackdropTiles tiles) {
        this.level = level;
        this.tiles = tiles;
        BackdropData data = level.backdrop();
        ramp = data.ramp();
        pixel = sprites.pixel;
        haze = Color.valueOf(data.hazeColour());
        stretches = level.atmosphereStretches();
        looks = new Look[stretches.size()];
        for (int s = 0; s < looks.length; s++) {
            BackdropData.Look look =
                    data.atmosphere().of(stretches.get(s).atmosphere()).orElseThrow();
            looks[s] = new Look(
                    look.banks()
                            .map(id -> BackdropTiles.tiles(
                                    sprites, folder, id, data.tileSets().get(id)))
                            .orElse(null),
                    look.wisps()
                            .map(id -> BackdropTiles.tiles(
                                    sprites, folder, id, data.tileSets().get(id)))
                            .orElse(null),
                    (float) look.haze());
        }
        banks = ForegroundBanks.any(level) ? new ForegroundBanks(level) : null;
        if (banks != null) {
            // M10 part B, step B4: the banks' own images (a bank's {@code piece}, ART-B's bank-a..f), where the atlas
            // holds them; a bank without one keeps the code veil.
            banks.pieces(name -> sprites.hasBackdrop(folder + name)
                    ? sprites.backdrop(folder + name).first()
                    : null);
        }
        dust = level.collapse()
                .flatMap(collapse -> data.atmosphere().of(collapse.dust().atmosphere()))
                .map(look -> new Look(
                        look.banks()
                                .map(id -> BackdropTiles.tiles(
                                        sprites, folder, id, data.tileSets().get(id)))
                                .orElse(null),
                        look.wisps()
                                .map(id -> BackdropTiles.tiles(
                                        sprites, folder, id, data.tileSets().get(id)))
                                .orElse(null),
                        (float) look.haze()))
                .orElse(null);
    }

    /**
     * M5 part G: the atmosphere as the difficulty shows it: on {@code easy} a peak with an easy length
     * that long (Level 13's whiteout, 6 s); the same stretches, only their times differ.
     */
    void easy(boolean easy) {
        stretches = level.atmosphereStretches(easy);
    }

    /** M10 part A: the foreground banks {@code difficulty} draws (easy leaves some out). */
    void difficulty(vanguard.content.Difficulty difficulty) {
        if (banks != null) {
            banks.difficulty(difficulty);
        }
    }

    /** M10 part A: the foreground banks; null in a level without them. */
    ForegroundBanks banks() {
        return banks;
    }

    /**
     * M10 part A: the foreground banks over the {@code air} bodies for the ground scrolled by {@code groundScroll}, seen
     * through by {@code seeThrough} (a lightning flash's reveal, 0..1).
     */
    void drawForegroundBanks(SpriteBatch batch, double groundScroll, float seeThrough) {
        if (banks != null) {
            banks.update(groundScroll, seeThrough);
            banks.draw(batch);
        }
    }

    /** M10 part A: frees the banks' veil. */
    void dispose() {
        if (banks != null) {
            banks.dispose();
        }
    }

    /**
     * M5 part C: the collapse's event-triggered dust peak (Level 09) shows at {@code weight}, 0 (the
     * section's own atmosphere) to 1 (the peak's: its banks, wisps and haze), blended like a change
     * across a section boundary.
     */
    void dustPeak(float weight) {
        dustWeight = dust == null ? 0 : Math.clamp(weight, 0, 1);
    }

    /**
     * M5 part H: the high-air layer's strength, 1 (its own) to 0 (gone): Level 14's canopy veins fading
     * out after the Siege Spire's death.
     */
    void frontFade(float fade) {
        frontFade = Math.clamp(fade, 0, 1);
    }

    /** M5 part H: the high-air layer's strength now ({@link #frontFade(float)}). */
    float frontFade() {
        return frontFade;
    }

    /**
     * Sets the looks to draw: the atmosphere stretch's (a section, or its peak), or the two across a
     * stretch boundary while the change ramps.
     */
    void blend(double seconds) {
        int current = looks.length - 1;
        for (int s = 0; s < looks.length; s++) {
            if (seconds < stretches.get(s).end()) {
                current = s;
                break;
            }
        }
        from = to = looks[current];
        weight = 1;
        for (int s = 1; s < looks.length; s++) {
            double progress = (seconds - stretches.get(s).start()) / ramp + 0.5;
            if (progress > 0 && progress < 1) {
                from = looks[s - 1];
                to = looks[s];
                weight = (float) (progress * progress * (3 - 2 * progress));
            }
        }
        if (dustWeight > 0) {
            // The collapse's dust over whatever the section shows (it falls in a section's middle).
            from = weight < 0.5f ? from : to;
            to = dust;
            weight = dustWeight;
        }
    }

    /** The haze over the deep and far layers, as {@link #blend} set the looks. */
    void drawHaze(SpriteBatch batch) {
        float veil = from.haze() + (to.haze() - from.haze()) * weight;
        if (veil > 0) {
            batch.setColor(haze.r, haze.g, haze.b, veil);
            batch.draw(pixel, X0, 0, WIDTH, HEIGHT);
            batch.setColor(Color.WHITE);
        }
    }

    /** The low-air cloud banks on a layer scrolled by {@code scroll}, as {@link #blend} set the looks. */
    void drawBanks(SpriteBatch batch, long scroll, double seconds) {
        drawAtmosphere(batch, from.banks(), to.banks(), scroll, seconds);
    }

    /** The high-air wisps on a layer scrolled by {@code scroll}, as {@link #blend} set the looks. */
    void drawWisps(SpriteBatch batch, long scroll, double seconds) {
        drawAtmosphere(batch, from.wisps(), to.wisps(), scroll, seconds);
    }

    /** One look's tile set, or two crossfading while the atmosphere changes. */
    private void drawAtmosphere(SpriteBatch batch, Tiles old, Tiles current, long scroll, double seconds) {
        float alpha = batch.getColor().a;
        if (old != current && old != null) {
            batch.setColor(1, 1, 1, alpha * (1 - weight));
            tiles.drawTiles(batch, old, scroll, scroll + HEIGHT, scroll, seconds, 0);
        }
        if (current != null) {
            batch.setColor(1, 1, 1, old == current ? alpha : alpha * weight);
            tiles.drawTiles(batch, current, scroll, scroll + HEIGHT, scroll, seconds, 0);
        }
        batch.setColor(1, 1, 1, alpha);
    }
}
