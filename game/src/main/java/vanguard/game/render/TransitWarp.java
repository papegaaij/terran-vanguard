package vanguard.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import vanguard.sim.PlayField;

/**
 * M11 part B, step B4 (design/world/vrell-space, Hazards &amp; set pieces: gate transit; design/campaign Level 43,
 * Layout, and Level 44, the opening): the gate transit's warp as a post-effect over the play field (the HUD drawn
 * after it stays as it is). At its share {@code w} (0 to 1 over the warp's 1.5 s) the field drawn so far is copied and
 * drawn back through a fragment shader: a radial pull toward the field's centre ({@link #PULL} of the way at {@code w}
 * = 1, eased in as w², falling off over {@link #RADIUS} of the field's height round it, so the event plane draws the
 * world in) and one smooth grade to the event plane's violet ({@link #VIOLET}, Level 44's opening plane's mean colour,
 * smoothstep in w). The grade sets each pixel's luminance on a straight path from its own (the unpulled pixel's) to the
 * violet's ({@link #luminance}): the pull moves the colours, never the brightness, so every pixel's brightness changes
 * one way only, toward the violet's, with no flash and no inversion (the colours are a mix of the scene's and the
 * violet's). At w = 1 the field is the violet alone: the level ends there and the debrief opens over it. Level 44 opens
 * in it, unbending as w falls from 1 to 0 over its {@code opens_in_transit}.
 *
 * <p>{@link #shade} is the shader's twin on the CPU, for the tests. Nothing is allocated per frame.
 */
public final class TransitWarp implements Disposable {
    /** The event plane's violet (Level 44's plane-1 tiles' mean, pale violet), 0-1 RGB. */
    public static final float[] VIOLET = {167 / 255f, 152 / 255f, 204 / 255f};
    /** How far the pull draws the field in at the warp's end, a share of a pixel's distance from the centre. */
    static final float PULL = 0.35f;
    /** The pull's falloff round the centre, a share of the field's height (a Gaussian's width). */
    static final float RADIUS = 0.45f;
    /** Rec. 709 luminance weights. */
    static final float[] WEIGHTS = {0.2126f, 0.7152f, 0.0722f};

    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec2 v_texCoords;
            void main() {
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;
    private static final String FRAGMENT = """
            #ifdef GL_ES
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif
            #endif
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform float u_grade;
            uniform float u_pull;
            uniform vec3 u_violet;
            uniform vec2 u_aspect;
            uniform float u_radius;
            const vec3 WEIGHTS = vec3(0.2126, 0.7152, 0.0722);
            void main() {
                vec2 d = v_texCoords - vec2(0.5);
                vec2 a = d * u_aspect;
                float falloff = exp(-dot(a, a) / (u_radius * u_radius));
                vec2 from = clamp(vec2(0.5) + d * (1.0 + u_pull * falloff), 0.0, 1.0);
                vec3 pulled = texture2D(u_texture, from).rgb;
                vec3 own = texture2D(u_texture, v_texCoords).rgb;
                float target = mix(dot(own, WEIGHTS), dot(u_violet, WEIGHTS), u_grade);
                vec3 colour = mix(pulled, u_violet, u_grade);
                colour += vec3(target - dot(colour, WEIGHTS));
                gl_FragColor = vec4(clamp(colour, 0.0, 1.0), 1.0);
            }
            """;

    private ShaderProgram shader;
    private Texture copy;

    /** The grade's share at the warp's share {@code w}: smoothstep. */
    public static float grade(double w) {
        double c = Math.clamp(w, 0, 1);
        return (float) (c * c * (3 - 2 * c));
    }

    /** The pull's share at the warp's share {@code w}: eased in, w². */
    public static float pull(double w) {
        double c = Math.clamp(w, 0, 1);
        return (float) (PULL * c * c);
    }

    /** A colour's luminance (0-1 RGB at {@code offset}). */
    static float luminance(float[] rgb, int offset) {
        return WEIGHTS[0] * rgb[offset] + WEIGHTS[1] * rgb[offset + 1] + WEIGHTS[2] * rgb[offset + 2];
    }

    /**
     * The shader on the CPU: the field {@code src} ({@code width} × {@code height} RGB 0-1, rows from the bottom, as
     * the copy) at the warp's share {@code w} into {@code out} (the same layout), sampled at the texel centres.
     */
    public static void shade(float[] src, int width, int height, double w, float[] out) {
        float g = grade(w);
        float p = pull(w);
        float violetLuminance = luminance(VIOLET, 0);
        float aspect = (float) width / height;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float u = (col + 0.5f) / width;
                float v = (row + 0.5f) / height;
                float dx = u - 0.5f;
                float dy = v - 0.5f;
                float ax = dx * aspect;
                float falloff = (float) Math.exp(-(ax * ax + dy * dy) / (RADIUS * RADIUS));
                float fu = Math.clamp(0.5f + dx * (1 + p * falloff), 0, 1);
                float fv = Math.clamp(0.5f + dy * (1 + p * falloff), 0, 1);
                int fc = Math.min(width - 1, (int) (fu * width));
                int fr = Math.min(height - 1, (int) (fv * height));
                int own = (row * width + col) * 3;
                int from = (fr * width + fc) * 3;
                float target = luminance(src, own) + (violetLuminance - luminance(src, own)) * g;
                float r = src[from] + (VIOLET[0] - src[from]) * g;
                float gr = src[from + 1] + (VIOLET[1] - src[from + 1]) * g;
                float b = src[from + 2] + (VIOLET[2] - src[from + 2]) * g;
                float shift = target - (WEIGHTS[0] * r + WEIGHTS[1] * gr + WEIGHTS[2] * b);
                out[own] = Math.clamp(r + shift, 0, 1);
                out[own + 1] = Math.clamp(gr + shift, 0, 1);
                out[own + 2] = Math.clamp(b + shift, 0, 1);
            }
        }
    }

    /**
     * Draws the warp at share {@code w} over the play field drawn so far into the internal screen (the batch drawing,
     * its projection the internal screen's); nothing at 0.
     */
    public void apply(SpriteBatch batch, double w) {
        if (!(w > 0)) {
            return;
        }
        if (shader == null) {
            shader = new ShaderProgram(VERTEX, FRAGMENT);
            if (!shader.isCompiled()) {
                throw new IllegalStateException("transit warp shader: " + shader.getLog());
            }
            copy = new Texture(WIDTH, HEIGHT, Pixmap.Format.RGBA8888);
            copy.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        batch.flush();
        copy.bind();
        Gdx.gl.glCopyTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, (int) X0, 0, WIDTH, HEIGHT);
        batch.disableBlending();
        batch.setShader(shader);
        shader.setUniformf("u_grade", grade(w));
        shader.setUniformf("u_pull", pull(w));
        shader.setUniformf("u_violet", VIOLET[0], VIOLET[1], VIOLET[2]);
        shader.setUniformf("u_aspect", (float) WIDTH / HEIGHT, 1f);
        shader.setUniformf("u_radius", RADIUS);
        // The copy's first row is the field's bottom (as frame buffers store it): drawn flipped in y.
        batch.draw(copy, X0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
        batch.flush();
        batch.setShader(null);
        batch.enableBlending();
    }

    @Override
    public void dispose() {
        if (shader != null) {
            shader.dispose();
            copy.dispose();
        }
    }
}
