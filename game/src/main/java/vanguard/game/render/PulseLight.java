package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M11 part H, step H4 (design/campaign Level 50, Layout: the Heart's pulse reaching further each chamber;
 * tools/art/act7_core.py's H4 hook 2, the backdrop's {@code pulse} block): the Heart's pulse as a smooth screen-space
 * light over the hive core, the additive {@code pulse-light} (512 px, radius 256, {@code (1 - d²)³} to 0 at its canvas's
 * edge) drawn per section at its {@code centre} (screen px, y down), scaled to its {@code reach} (its radius, px) and at
 * {@code peak} × the pulse, each eased from its first value to its second over the section, a {@value #EASE_SECONDS} s
 * ease from the section before at a section's start; none where a section has none. The pulse is the backdrop's own (the
 * veins' breath before the Heart, its beat in the fight, out at K): {@link VeinPulse#shareAt}. Nothing is allocated per
 * frame.
 */
final class PulseLight {
    /** A section's light eases from the one before over this long from its start, s. */
    static final double EASE_SECONDS = 2;
    /** The light's canvas radius, px. */
    static final double RADIUS = 256;

    private final LevelData level;
    private final TextureRegion light;
    /** Per section (0-based) centre x, y (down), reach from, to, peak from, to; NaN reach for none. */
    private final double[][] sections;
    /** This frame's light: its centre (y up), radius and alpha. */
    private final double[] now = new double[4];

    private PulseLight(LevelData level, TextureRegion light, List<BackdropData.PulseLight> lights) {
        this.level = level;
        this.light = light;
        sections = new double[level.sections().size()][];
        for (BackdropData.PulseLight entry : lights) {
            if (entry.section() >= sections.length || entry.centre().isEmpty()) {
                continue;
            }
            var centre = entry.centre().get();
            sections[entry.section()] = new double[] {
                centre.x(),
                centre.y(),
                entry.reach().get().get(0),
                entry.reach().get().get(1),
                entry.peak().get().get(0),
                entry.peak().get().get(1)
            };
        }
    }

    /** The pulse light of a level whose backdrop has a {@code pulse} block and the {@code pulse-light} image; null else. */
    static PulseLight of(Sprites sprites, LevelData level, String folder) {
        if (sprites == null || level.backdrop().pulse().isEmpty() || !sprites.hasBackdrop(folder + "pulse-light")) {
            return null;
        }
        return new PulseLight(
                level,
                sprites.backdrop(folder + "pulse-light", 1).first(),
                level.backdrop().pulse().get());
    }

    /** For the tests: the light's numbers without its image. */
    static PulseLight of(LevelData level) {
        return new PulseLight(level, null, level.backdrop().pulse().orElse(List.of()));
    }

    /**
     * The light at level time {@code t} into {@code out}: centre x, y (up), radius, peak (before the pulse); a peak of 0
     * where there is none.
     */
    void at(double t, double[] out) {
        int section = level.sectionAt(t) - 1;
        if (section < 0) {
            section = sections.length - 1;
        }
        double start = section == 0 ? 0 : level.sections().get(section - 1).end();
        double end = level.sections().get(section).end();
        double own = end > start ? Math.clamp((t - start) / (end - start), 0, 1) : 1;
        values(section, own, out);
        double ease = (t - start) / EASE_SECONDS;
        if (section > 0 && ease < 1) {
            double[] before = {0, 0, 0, 0};
            values(section - 1, 1, before);
            double e = Math.clamp(ease, 0, 1);
            e = e * e * (3 - 2 * e);
            if (sections[section] == null) {
                System.arraycopy(before, 0, out, 0, 3);
            } else if (sections[section - 1] == null) {
                before[0] = out[0];
                before[1] = out[1];
                before[2] = out[2];
            }
            for (int i = 0; i < 4; i++) {
                out[i] = before[i] + (out[i] - before[i]) * e;
            }
        }
    }

    private void values(int section, double share, double[] out) {
        double[] s = sections[section];
        if (s == null) {
            out[3] = 0;
            return;
        }
        out[0] = s[0];
        out[1] = PlayField.HEIGHT - s[1];
        out[2] = s[2] + (s[3] - s[2]) * share;
        out[3] = s[4] + (s[5] - s[4]) * share;
    }

    /** Draws the light at level time {@code t} with the pulse {@code pulse} (0-1), additive; nothing at 0. */
    void draw(SpriteBatch batch, double t, float pulse) {
        if (light == null || pulse <= 0) {
            return;
        }
        at(t, now);
        float alpha = (float) (now[3] * pulse);
        if (alpha <= 0) {
            return;
        }
        float size = (float) (2 * now[2] * light.getRegionWidth() / (2 * RADIUS));
        Texture texture = light.getTexture();
        Texture.TextureFilter min = texture.getMinFilter();
        Texture.TextureFilter mag = texture.getMagFilter();
        batch.flush();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, Math.min(1, alpha));
        batch.draw(light, (float) (X0 + now[0] - size / 2), (float) (now[1] - size / 2), size, size);
        batch.flush();
        texture.setFilter(min, mag);
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
