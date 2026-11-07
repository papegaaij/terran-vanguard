package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import vanguard.sim.LevelScript;

/**
 * M11 part G, step G4 (design/campaign Level 49, the echoes; design/art-direction, the spectral tint; ART-G's reference
 * sheet {@code level-49-echoes/concept/echo-tint-final-r94-a.png}, {@code tools/art/act7_memory.py}'s {@code
 * echo_tint}): the memory's spectral tint, a render-only shader around the passes that draw the remembered units, the
 * echoes, their launched units, their death pieces and the debris (the supply pod) in a level that asks for it (one
 * whose rush flies echoes). Bullets, beams and their glows are drawn in their own passes, outside it: never tinted.
 *
 * <p>Each texel's colour is desaturated {@link #K} toward a cold violet-white of its own brightness, then lifted a step
 * ({@code × (1 − LIFT) + violet-white × LIFT}); an additive draw's lift goes with the texel's light, so a glow's dark
 * quad stays dark. Its alpha erodes smoothly to 0 inside the sprite's canvas: the alpha blurred over σ = edge / 2.5
 * ({@link #edgePx}: 8 % of the region's smaller side, 1.5 to 10 px), then {@code smoothstep((a − 0.3) / 0.55)}. The
 * whole tint shimmers in brightness by ±{@link #SHIMMER_AMP} at {@link #SHIMMER_HZ} Hz. {@link #shade} is the colour's
 * twin on the CPU, for the tests. Nothing is allocated per frame; no simulation state (the goldens cannot move).
 */
public final class EchoTint implements Disposable {
    /** The desaturation toward violet-white. */
    public static final float K = 0.4f;
    /** The units' lift toward violet-white (a step brighter than the tinted worlds). */
    public static final float LIFT = 0.26f;
    /** The shimmer: ±6 % brightness at 0.35 Hz (≤ 0.5 Hz). */
    public static final float SHIMMER_AMP = 0.06f;

    public static final float SHIMMER_HZ = 0.35f;
    /** The violet-white (226, 206, 255), 0-1. */
    public static final float[] VIOLET_W = {226 / 255f, 206 / 255f, 255 / 255f};
    /** The brightness weights of the desaturation ({@code act7_r84.spectral}). */
    static final float[] WEIGHTS = {0.3f, 0.59f, 0.11f};

    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = a_color;
                v_color.a = v_color.a * (255.0 / 254.0);
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;

    /**
     * The tint. {@code u_region}: the drawn region's texture bounds (min u, min v, max u, max v); {@code u_sigma}: the
     * blur's σ in texture units (x, y), 0 for no erosion; {@code u_tint}: x the shimmer's factor, y 1 for an additive
     * draw. The blur is a centre tap and two rings of eight (at σ and 2σ), clamped to the region.
     */
    private static final String FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec4 u_region;
            uniform vec2 u_sigma;
            uniform vec2 u_tint;
            const vec3 VW = vec3(0.886275, 0.807843, 1.0);
            const vec3 WEIGHTS = vec3(0.3, 0.59, 0.11);
            float alphaAt(vec2 offset) {
                return texture2D(u_texture, clamp(v_texCoords + offset * u_sigma, u_region.xy, u_region.zw)).a;
            }
            void main() {
                vec4 texel = v_color * texture2D(u_texture, v_texCoords);
                float lum = dot(texel.rgb, WEIGHTS);
                vec3 spectral = texel.rgb * (1.0 - 0.4) + lum * VW * 1.08 * 0.4;
                float light = mix(1.0, max(texel.r, max(texel.g, texel.b)), u_tint.y);
                vec3 colour = (spectral * (1.0 - 0.26) + VW * 0.26 * light) * u_tint.x;
                float fall = 1.0;
                if (u_sigma.x > 0.0) {
                    float a1 = alphaAt(vec2(1.0, 0.0)) + alphaAt(vec2(-1.0, 0.0)) + alphaAt(vec2(0.0, 1.0))
                            + alphaAt(vec2(0.0, -1.0)) + alphaAt(vec2(0.7071, 0.7071)) + alphaAt(vec2(-0.7071, 0.7071))
                            + alphaAt(vec2(0.7071, -0.7071)) + alphaAt(vec2(-0.7071, -0.7071));
                    float a2 = alphaAt(vec2(2.0, 0.0)) + alphaAt(vec2(-2.0, 0.0)) + alphaAt(vec2(0.0, 2.0))
                            + alphaAt(vec2(0.0, -2.0)) + alphaAt(vec2(1.4142, 1.4142)) + alphaAt(vec2(-1.4142, 1.4142))
                            + alphaAt(vec2(1.4142, -1.4142)) + alphaAt(vec2(-1.4142, -1.4142));
                    float inner = 0.2 * alphaAt(vec2(0.0)) + 0.0625 * a1 + 0.0375 * a2;
                    fall = smoothstep(0.0, 1.0, (inner - 0.3) / 0.55);
                }
                gl_FragColor = vec4(clamp(colour, 0.0, 1.0), texel.a * fall);
            }
            """;

    private ShaderProgram program;

    /** Whether a level asks for the tint: its rush flies echoes (design/campaign Level 49). */
    public static boolean wanted(LevelScript script) {
        return script.setPieces().stream()
                .flatMap(piece -> piece.boss().stream())
                .anyMatch(boss -> boss.echo().isPresent());
    }

    /** The erosion's edge for a region {@code w} × {@code h} px: 8 % of its smaller side, 1.5 to 10 px. */
    public static float edgePx(int w, int h) {
        return Math.clamp(0.08f * Math.min(w, h), 1.5f, 10f);
    }

    /** The shimmer's brightness factor at real time {@code seconds}: {@code 1 + 0.06 sin(2π 0.35 t)}. */
    public static float shimmer(double seconds) {
        return (float) (1 + SHIMMER_AMP * Math.sin(2 * Math.PI * SHIMMER_HZ * seconds));
    }

    /**
     * The shader's colour on the CPU: texel {@code rgb} (0-1, straight alpha) tinted with shimmer factor {@code
     * shimmer}, into {@code out} (the alpha's erosion aside).
     */
    public static void shade(float[] rgb, float shimmer, float[] out) {
        float lum = rgb[0] * WEIGHTS[0] + rgb[1] * WEIGHTS[1] + rgb[2] * WEIGHTS[2];
        for (int c = 0; c < 3; c++) {
            float spectral = rgb[c] * (1 - K) + lum * VIOLET_W[c] * 1.08f * K;
            out[c] = Math.clamp((spectral * (1 - LIFT) + VIOLET_W[c] * LIFT) * shimmer, 0, 1);
        }
    }

    /**
     * From here on (until {@link #end}) the batch's draws are tinted at real time {@code seconds}: only on a {@link
     * TintBatch} (a plain batch draws as it is).
     */
    public void begin(SpriteBatch batch, double seconds) {
        if (!(batch instanceof TintBatch tinting)) {
            return;
        }
        if (program == null) {
            program = new ShaderProgram(VERTEX, FRAGMENT);
            if (!program.isCompiled()) {
                throw new IllegalStateException("echo tint shader: " + program.getLog());
            }
        }
        tinting.tint(program, shimmer(seconds));
    }

    /** Back to the batch's own shader. */
    public void end(SpriteBatch batch) {
        if (batch instanceof TintBatch tinting) {
            tinting.tint(null, 1);
        }
    }

    @Override
    public void dispose() {
        if (program != null) {
            program.dispose();
            program = null;
        }
    }
}
