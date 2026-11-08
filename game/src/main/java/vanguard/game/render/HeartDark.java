package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import java.nio.IntBuffer;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M11 part H, step H4 (design/enemies/bosses/choir-heart, the walls' phase: "the light goes out but for the Heart's glow";
 * design/campaign Level 50; m11h-gaps.md §1 #9; the boss's {@code dark}): the darkness of the Heart's walls' phase, a
 * run-time ambient where Act 4's {@link DarkLight} and Level 06's {@link FarsideLooks} darkness have the level's own
 * constant one. A light map multiplied over what is drawn up to the ship (the backdrop, the walls' tissue, the Heart's
 * crown and parts, the units, Rook and the ship): its base the <b>ambient</b> ({@link #target}: full outside the phase;
 * in it the data's {@code ambient}, 8 % (easy 20 %, the Dark levels assist's floor 20 %), and a quarter of the light
 * back per node cut ({@code per_node}), full once every node is cut), eased over {@value #EASE_SECONDS} s to each change;
 * over it the ship's headlight cone ahead, a pool round the ship and round Rook so the hulls read, a small pool round
 * each of the player's shots, and a faint rim pool on every unit (so a release reads outside the cone). The bullets, the
 * glows (the Heart's pulse, the walls' lit edge, the units' glow frames, all drawn after it), the bursts and the HUD
 * stay as they are: the darkness is the play field's, never the HUD's. Presentation only; nothing is allocated per
 * frame.
 */
final class HeartDark implements Disposable {
    /** The ambient eases to a change over this long, s. */
    static final double EASE_SECONDS = 1;
    /** The Dark levels assist's ambient floor (Level 26's rule). */
    static final float ASSIST_FLOOR = DarkLight.ASSIST_AMBIENT;
    /** The headlight: its reach ahead of the nose and its full angle. */
    static final double CONE_LENGTH = 300;

    static final double CONE_ANGLE = Math.toRadians(48);
    /** The cone starts this far above the ship's centre (its nose). */
    static final float NOSE = 18;
    /** The pools' radii: round the ship and Rook, round a shot, a unit's rim (per its body's half-size). */
    static final float SHIP_POOL = 70;

    static final float SHOT_POOL = 20;
    static final double RIM_REACH = 1.5;
    /** A unit's rim pool's strength (of the light missing to full there). */
    static final float RIM_LIGHT = 0.3f;

    private static final Color HEADLIGHT = new Color(0.78f, 0.86f, 1f, 0.95f);
    private static final Color POOL = new Color(1f, 0.95f, 0.9f, 0.85f);
    private static final Color SHOT = new Color(0.8f, 0.85f, 1f, 0.6f);
    private static final int RADIAL_SIZE = 64;

    private final int phase;
    private final float ambient;
    private final float perNode;
    private float floor;
    /** The ambient now (eased) and the real time it was last eased. */
    private float now = 1;

    private double lastReal = Double.NaN;

    private FrameBuffer lightMap;
    private Texture radial;
    private Texture cone;
    private final Matrix4 projection = new Matrix4();
    private final Matrix4 saved = new Matrix4();
    private final IntBuffer boundFramebuffer = BufferUtils.newIntBuffer(1);
    private final IntBuffer boundViewport = BufferUtils.newIntBuffer(16);

    /**
     * @param phase the phase that is dark (0-based)
     * @param ambient the light outside the cone and the pools, 0-1 (the difficulty's)
     * @param perNode the share of the light back per node cut
     */
    HeartDark(int phase, double ambient, double perNode) {
        this.phase = phase;
        this.ambient = (float) ambient;
        this.perNode = (float) perNode;
    }

    /** The Dark levels assist: the ambient is held at least at its floor. */
    void assist(boolean on) {
        floor = on ? ASSIST_FLOOR : 0;
    }

    /**
     * The ambient the dark eases to: full outside the dark phase or after the kill; in it {@code ambient} and {@code
     * perNode} of the rest back per node cut ({@code cut} of them), never under {@code floor}.
     */
    static float target(boolean dark, double ambient, double perNode, int cut, float floor) {
        if (!dark) {
            return 1;
        }
        double back = Math.min(1, perNode * cut);
        return (float) Math.max(floor, ambient + (1 - ambient) * back);
    }

    /** The ambient now, after easing toward this frame's target at real time {@code real}. */
    float ease(Sortie sortie, double real) {
        SetPiece heart = HeartLooks.heart(sortie);
        boolean dark = heart != null
                && heart.present()
                && heart.phase() == phase
                && !heart.lastWords().map(words -> words.spoken()).orElse(false);
        int cut = 0;
        if (dark && sortie.arenaWalls() != null) {
            for (int p = 0; p < heart.partCount(); p++) {
                if (sortie.arenaWalls().rides(p) && heart.partHp(p) <= 0) {
                    cut++;
                }
            }
        }
        float goal = target(dark, ambient, perNode, cut, floor);
        double step = Double.isNaN(lastReal) || real < lastReal ? 1e9 : real - lastReal;
        lastReal = real;
        float move = (float) (step / EASE_SECONDS);
        now = goal > now ? Math.min(goal, now + move) : Math.max(goal, now - move);
        return now;
    }

    /** The level restarts: the light is full again at once. */
    void reset() {
        now = 1;
        lastReal = Double.NaN;
    }

    /** Whether any of the field is dark this frame (the glows are then drawn after it). */
    boolean active() {
        return now < 0.999f;
    }

    /** Darkens what is drawn so far on the play field by the light map (nothing while the light is full). */
    void darken(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!active()) {
            return;
        }
        if (lightMap == null) {
            lightMap = new FrameBuffer(Pixmap.Format.RGBA8888, PlayField.WIDTH, PlayField.HEIGHT, false);
            projection.setToOrtho2D(0, 0, PlayField.WIDTH, PlayField.HEIGHT);
            radial = radialTexture();
            cone = coneTexture(CONE_LENGTH, CONE_ANGLE);
        }
        batch.end();
        saved.set(batch.getProjectionMatrix());
        // FrameBuffer.end() binds the window, not the frame buffer that was bound before (no nesting).
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, boundFramebuffer);
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, boundViewport);
        lightMap.begin();
        Gdx.gl.glClearColor(now, now, now, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(projection);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        float missing = 1 - now;
        if (sortie.flying()) {
            float x = (float) sortie.ship().renderX(alpha);
            float y = (float) sortie.ship().renderY(alpha);
            batch.setColor(HEADLIGHT.r, HEADLIGHT.g, HEADLIGHT.b, HEADLIGHT.a * missing);
            batch.draw(cone, x - cone.getWidth() / 2f, y + NOSE);
            batch.setColor(POOL.r, POOL.g, POOL.b, POOL.a * missing);
            pool(batch, x, y, SHIP_POOL);
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null) {
            batch.setColor(POOL.r, POOL.g, POOL.b, POOL.a * missing);
            pool(batch, rook.renderX(alpha), rook.renderY(alpha), SHIP_POOL * 0.75f);
        }
        batch.setColor(SHOT.r, SHOT.g, SHOT.b, SHOT.a * missing);
        for (int i = 0; i < sortie.shotCount(); i++) {
            pool(batch, sortie.shot(i).renderX(alpha), sortie.shot(i).renderY(alpha), SHOT_POOL);
        }
        batch.setColor(1, 1, 1, RIM_LIGHT * missing);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            double half =
                    Math.max(unit.spec().hitbox().width(), unit.spec().hitbox().height()) / 2;
            pool(batch, unit.renderX(alpha), unit.renderY(alpha), (float) (half * RIM_REACH + 8));
        }
        batch.setColor(Color.WHITE);
        batch.end();
        lightMap.end();
        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, boundFramebuffer.get(0));
        Gdx.gl.glViewport(boundViewport.get(0), boundViewport.get(1), boundViewport.get(2), boundViewport.get(3));
        batch.setProjectionMatrix(saved);
        batch.begin();
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.draw(
                lightMap.getColorBufferTexture(),
                X0,
                0,
                PlayField.WIDTH,
                PlayField.HEIGHT,
                0,
                0,
                PlayField.WIDTH,
                PlayField.HEIGHT,
                false,
                true);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void pool(SpriteBatch batch, double x, double y, float radius) {
        batch.draw(radial, (float) (x - radius), (float) (y - radius), 2 * radius, 2 * radius);
    }

    /** A white disc fading smoothly from its centre to 0 at its rim, for every pool. */
    private static Texture radialTexture() {
        Pixmap pixmap = new Pixmap(RADIAL_SIZE, RADIAL_SIZE, Pixmap.Format.RGBA8888);
        double half = RADIAL_SIZE / 2.0;
        for (int y = 0; y < RADIAL_SIZE; y++) {
            for (int x = 0; x < RADIAL_SIZE; x++) {
                double d = Math.hypot(x + 0.5 - half, y + 0.5 - half) / half;
                float a = (float) Math.max(0, 1 - d);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, a * a * (3 - 2 * a)));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /** The headlight: a cone {@code length} px long and {@code angle} wide, pointing up, fading to its end and sides. */
    private static Texture coneTexture(double length, double angle) {
        int height = (int) Math.ceil(length);
        int width = (int) Math.ceil(2 * length * Math.tan(angle / 2)) + 2;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        double half = width / 2.0;
        for (int y = 0; y < height; y++) {
            // Pixmap rows run down: row 0 is the far end.
            double along = (height - y - 0.5) / length;
            for (int x = 0; x < width; x++) {
                double off = Math.abs(Math.atan2(x + 0.5 - half, (height - y - 0.5)));
                double side = Math.clamp(1 - off / (angle / 2), 0, 1);
                double s = Math.min(1, side * 3);
                float a = (float) (s * s * (3 - 2 * s) * (1 - along * along));
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        if (lightMap != null) {
            lightMap.dispose();
            radial.dispose();
            cone.dispose();
        }
    }
}
