package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WindowShell;

/**
 * M11 part A, step A3a (design/enemies, Window shields; design/enemies/space/gate-warden, Behaviour and Look): a window
 * shell as the player sees it, on the stand-in unit sprite until the Warden's own frames (ART-B) carry the shell. The
 * closed shell is a pale gold-white ovoid {@value #SHELL_GROW_X} px wider and {@value #SHELL_GROW_Y} px taller than its
 * unit's hit box (the Warden's 56×60 in its 76×84 egg), translucent with a brighter rim and six petals parted by dark
 * seams, a slow specular sweep running over it ({@value #SWEEP_SECONDS} s). It opens like an iris over the shell's iris
 * time ({@link WindowShell#iris()}): each petal folds back out along its middle ({@link #petalOut}) as it fades ({@link
 * #petalOpacity}). M11 part B, step B4: the Gate Warden wears its production frames' shell instead ({@link
 * WardenLooks}); this one stays for a shelled unit without its own. Broken by a breaker hit or the EMP it is gone for its hold (the shatter is the screen's {@code
 * shell-break} effect), then regrows over its regrowth time behind a silver-gold shimmer band rising over it ({@link
 * #regrowOpacity}, {@link #shimmerY}). Behind it the glyph band and the eye burn crimson as it opens ({@link
 * #glyphLevel}), dim while closed, and dim and flicker in the unit's leaving tell ({@link #flicker}). The petals are
 * textures generated once per level in smooth float alpha that fades to 0 inside their canvas ({@link #petal}; no
 * ordered dither: the 2026-10-30 dither audit), the glows {@link SoftGlow}'s. Allocates nothing per frame.
 */
final class WindowShellLooks implements Disposable {
    /** The shell's size past its unit's hit box, px (the Warden: 56×60 in 76×84). */
    static final int SHELL_GROW_X = 20;

    static final int SHELL_GROW_Y = 24;
    /** The petals and their seams. */
    static final int PETALS = 6;
    /** The canvas round the shell: the soft edge fades to 0 inside it. */
    static final int MARGIN = 4;
    /** The closed shell's opacity in its middle and at its rim. */
    static final double BODY = 0.24;

    static final double RIM = 0.78;
    /** A seam's darkening at its middle, and its half width, px. */
    static final double SEAM = 0.5;

    static final double SEAM_WIDTH = 0.9;
    /** How far a petal folds back out along its middle when the shell is open, px. */
    static final double PETAL_OUT = 9;
    /** The specular sweep runs over the shell once in this many seconds. */
    static final double SWEEP_SECONDS = 2.5;
    /** The shell: pale gold-white; the sweep and the shimmer: warm white and silver-gold (additive). */
    static final Color SHELL = Color.valueOf("F2E6C4");

    static final Color SWEEP = Color.valueOf("FFF4D8");
    static final Color SHIMMER = Color.valueOf("E8E2C8");
    /** The glyphs and the eye: the gate's crimson. */
    static final Color CRIMSON = Color.valueOf("E0203C");
    /** The glyph band's light closed, and the share it adds open. */
    static final double GLYPHS_CLOSED = 0.18;

    static final double GLYPHS_OPEN = 0.62;
    /** In the leaving tell the glyphs dim to this share and flicker each {@value #FLICKER_TICKS} steps. */
    static final double TELL_DIM = 0.5;

    static final int FLICKER_TICKS = 3;
    /** The units whose regrowth is followed at once. */
    private static final int UNITS = 16;

    private final SoftGlow glow;
    private final List<EnemySpec> kinds;
    /** Per enemy kind its six petals (null for a kind without a shell); generated at the first draw. */
    private TextureRegion[][] petals;

    private Texture[] textures;
    private final SerialClock regrowths = new SerialClock(UNITS);

    WindowShellLooks(List<EnemySpec> kinds, SoftGlow glow) {
        this.glow = glow;
        this.kinds = kinds;
    }

    private void generate() {
        petals = new TextureRegion[kinds.size()][];
        textures = new Texture[kinds.size() * PETALS];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec kind = kinds.get(k);
            if (kind.windowShield().isEmpty()) {
                continue;
            }
            int width = width(kind);
            int height = height(kind);
            petals[k] = new TextureRegion[PETALS];
            for (int p = 0; p < PETALS; p++) {
                Texture texture = texture(petal(p, width, height), width, height);
                textures[k * PETALS + p] = texture;
                petals[k][p] = new TextureRegion(texture);
            }
        }
    }

    /** Whether any of the kinds has a window shell. */
    static boolean any(List<EnemySpec> kinds) {
        return kinds.stream().anyMatch(kind -> kind.windowShield().isPresent());
    }

    /** The shell's canvas, px: its ovoid and the margin round it. */
    static int width(EnemySpec kind) {
        return (int) Math.round(kind.hitbox().width()) + SHELL_GROW_X + 2 * MARGIN;
    }

    static int height(EnemySpec kind) {
        return (int) Math.round(kind.hitbox().height()) + SHELL_GROW_Y + 2 * MARGIN;
    }

    /** After each step: the regrowing shells' starts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        regrowths.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().windowShield().isPresent() && enemy.window().regrowing()) {
                regrowths.touch(enemy.serial(), tick);
            }
        }
        regrowths.end();
    }

    /** The level restarts. */
    void restart() {
        regrowths.clear();
    }

    /**
     * Over the air units: each shelled unit's glyphs and eye (additive, flickering in its leaving tell), its shell's
     * petals and their sweep, or its regrowth and shimmer.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, double seconds, WardenLooks framed) {
        if (petals == null) {
            generate();
        }
        long now = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            TextureRegion[] own = petals[enemy.kind()];
            // M11 part B, step B4: a Warden on its production frames wears their shell (WardenLooks).
            if (own == null || (framed != null && framed.draws(enemy))) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            WindowShell shell = enemy.window();
            double iris = shell.iris();
            double radius = Math.min(enemy.hitbox().width(), enemy.hitbox().height()) / 2.0;
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            float glyphs =
                    (float) (glyphLevel(iris, enemy.leavingTell()) * flicker(enemy.leavingTell(), now, enemy.serial()));
            SoftGlow.tint(batch, CRIMSON, glyphs);
            glow.ring(batch, x, y, radius * 0.8);
            SoftGlow.tint(batch, CRIMSON, (float) (0.15 + 0.6 * iris));
            glow.disc(batch, x, y, radius * 0.7);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            EnemySpec.WindowShield spec = enemy.spec().windowShield().get();
            float opacity;
            double out;
            double regrown = -1;
            if (shell.regrowing()) {
                regrown = regrowShare(now - regrowths.since(enemy.serial(), now), SimStep.ticks(spec.regrowSeconds()));
                opacity = regrowOpacity(regrown);
                out = 0;
            } else if (shell.held()) {
                // Broken for the hold: the petals shattered (the screen's shell-break), nothing until it regrows.
                continue;
            } else {
                opacity = petalOpacity(iris);
                out = petalOut(iris);
            }
            if (opacity > 0) {
                for (int p = 0; p < PETALS; p++) {
                    double middle = petalMiddle(p);
                    batch.setColor(1, 1, 1, opacity);
                    PlayFrame.drawCentred(batch, own[p], x + out * Math.cos(middle), y + out * Math.sin(middle));
                }
                batch.setColor(Color.WHITE);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            double halfWidth = own[0].getRegionWidth() / 2.0 - MARGIN;
            double halfHeight = own[0].getRegionHeight() / 2.0 - MARGIN;
            if (regrown >= 0) {
                SoftGlow.tint(batch, SHIMMER, (float) (0.7 * Math.sin(Math.PI * regrown)));
                glow.disc(batch, x, y + shimmerY(regrown, halfHeight), halfWidth * 1.6);
            } else if (opacity > 0) {
                double sweep = (seconds % SWEEP_SECONDS) / SWEEP_SECONDS;
                SoftGlow.tint(batch, SWEEP, (float) (0.35 * opacity * Math.sin(Math.PI * sweep)));
                glow.disc(
                        batch,
                        x + (sweep - 0.5) * 1.2 * halfWidth,
                        y + (0.5 - sweep) * 0.9 * halfHeight + 0.3 * halfHeight,
                        halfWidth * 0.9);
            }
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** Petal {@code p}'s middle, radians counter-clockwise from +x (y up): petal 0 straight up, then every 60°. */
    static double petalMiddle(int p) {
        return Math.PI / 2 + p * 2 * Math.PI / PETALS;
    }

    /** How far the petals have folded back out at the iris's {@code iris} (0 closed .. 1 open), px. */
    static double petalOut(double iris) {
        return PETAL_OUT * smooth(iris);
    }

    /** The petals' opacity at the iris's {@code iris}: whole closed, gone open. */
    static float petalOpacity(double iris) {
        return (float) (1 - smooth(iris));
    }

    /** How far a regrowth has run after {@code ticks} of its {@code total} steps, 0..1. */
    static double regrowShare(long ticks, int total) {
        return total <= 0 ? 1 : Math.clamp((double) ticks / total, 0, 1);
    }

    /** The regrowing shell's opacity at its share: it fills in behind the shimmer. */
    static float regrowOpacity(double share) {
        return (float) smooth(share);
    }

    /** The shimmer band's height over the unit's middle at the regrowth's share: from its foot to its crown, px. */
    static double shimmerY(double share, double halfHeight) {
        return (2 * share - 1) * halfHeight;
    }

    /** The glyph band's light at the iris's {@code iris}: dim behind the closed shell, burning open; dimmed in the tell. */
    static double glyphLevel(double iris, boolean leaving) {
        double level = GLYPHS_CLOSED + GLYPHS_OPEN * iris;
        return leaving ? level * TELL_DIM : level;
    }

    /**
     * The leaving tell's flicker on step {@code tick} for unit {@code serial}: 1 outside the tell, else a stepwise
     * random 0.25..1 that changes every {@value #FLICKER_TICKS} steps (deterministic, no allocation).
     */
    static double flicker(boolean leaving, long tick, int serial) {
        if (!leaving) {
            return 1;
        }
        long n = tick / FLICKER_TICKS * 0x9E3779B97F4A7C15L + serial * 0xC2B2AE3D27D4EB4FL;
        n ^= n >>> 29;
        n *= 0xBF58476D1CE4E5B9L;
        n ^= n >>> 32;
        double r = (n >>> 11) * 0x1.0p-53;
        return 0.25 + 0.75 * r;
    }

    private static double smooth(double t) {
        double c = Math.clamp(t, 0, 1);
        return c * c * (3 - 2 * c);
    }

    /**
     * Petal {@code p}'s alpha in a {@code width} × {@code height} canvas (rows from the top): its sector of the shell's
     * ovoid (the canvas less {@link #MARGIN}), {@link #BODY} in the middle rising to {@link #RIM} at the rim, the edge and
     * the sector's sides soft over about a pixel, the seams between the petals darkened from the crown outward; 0 on the
     * canvas's border.
     */
    static float[] petal(int p, int width, int height) {
        float[] alpha = new float[width * height];
        double a = width / 2.0 - MARGIN;
        double b = height / 2.0 - MARGIN;
        double half = Math.PI / PETALS;
        double middle = petalMiddle(p);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double dx = col + 0.5 - width / 2.0;
                double dy = height / 2.0 - (row + 0.5);
                double r = Math.hypot(dx / a, dy / b);
                double edge = Math.clamp((1 - r) * Math.min(a, b) / 1.2 + 0.5, 0, 1);
                if (edge <= 0) {
                    continue;
                }
                double body = BODY + (RIM - BODY) * smoothStep(0.55, 1.0, r);
                double angle = Math.atan2(dy, dx);
                double off = Math.abs(wrap(angle - middle));
                double px = Math.hypot(dx, dy);
                double side = Math.clamp((half - off) * px / 1.0 + 0.5, 0, 1);
                double toSeam = Math.abs(half - off) * px;
                // The seams fade out toward the crown (the petals meet under the eye there).
                double seam =
                        1 - SEAM * smoothStep(0.15, 0.55, r) * Math.exp(-(toSeam / SEAM_WIDTH) * (toSeam / SEAM_WIDTH));
                alpha[row * width + col] = (float) (body * edge * side * seam);
            }
        }
        return alpha;
    }

    private static double smoothStep(double from, double to, double x) {
        return smooth((x - from) / (to - from));
    }

    private static double wrap(double angle) {
        double a = angle % (2 * Math.PI);
        if (a > Math.PI) {
            a -= 2 * Math.PI;
        } else if (a < -Math.PI) {
            a += 2 * Math.PI;
        }
        return a;
    }

    private static Texture texture(float[] alpha, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                pixmap.drawPixel(col, row, Color.rgba8888(SHELL.r, SHELL.g, SHELL.b, alpha[row * width + col]));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        if (textures == null) {
            return;
        }
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }
}
