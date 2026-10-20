package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws a sprite blended towards a flat colour by an amount, keeping its silhouette: the white
 * hit flash of design/art-direction (Animation rules) and the ship's blue shield shimmer. GLES 2
 * compatible, like every shader of the game.
 */
public final class FlashShader implements Disposable {
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
    private static final String FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec4 u_flash;
            void main() {
                vec4 texel = v_color * texture2D(u_texture, v_texCoords);
                gl_FragColor = vec4(mix(texel.rgb, u_flash.rgb, u_flash.a), texel.a);
            }
            """;

    /**
     * Step GFX2: a glowing shot's art recoloured by its light: each texel's brightest channel times the colour, blended
     * {@code u_flash.a} of the way from the art's own colour (its alpha kept), so the additive art's dark halo stays
     * dark and its cyan body takes the colour (the rear-guard drone's gold bolt from the light drone's cyan art).
     */
    private static final String RECOLOUR_FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec4 u_flash;
            void main() {
                vec4 texel = v_color * texture2D(u_texture, v_texCoords);
                float light = max(texel.r, max(texel.g, texel.b));
                gl_FragColor = vec4(mix(texel.rgb, u_flash.rgb * light, u_flash.a), texel.a);
            }
            """;

    /**
     * Step GFX3: only a sprite's cracks lit in a flat colour (the Dust Colossus's bulge and eruption marker, their dark
     * violet cracks glowing lime): a texel's crack share is how violet (blue over green) and how dark it is, its output
     * the colour at the texel's alpha times that share times {@code u_flash.a}, for an additive draw over the sprite.
     */
    private static final String CRACKS_FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec4 u_flash;
            void main() {
                vec4 texel = v_color * texture2D(u_texture, v_texCoords);
                float lum = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
                float crack = clamp((texel.b - texel.g + 0.08) * 5.0, 0.0, 1.0) * clamp((0.38 - lum) * 6.0, 0.0, 1.0);
                gl_FragColor = vec4(u_flash.rgb, texel.a * crack * u_flash.a);
            }
            """;

    private final ShaderProgram program;
    private final ShaderProgram recolour;
    private final ShaderProgram cracks;

    public FlashShader() {
        program = new ShaderProgram(VERTEX, FRAGMENT);
        if (!program.isCompiled()) {
            throw new IllegalStateException("flash shader: " + program.getLog());
        }
        recolour = new ShaderProgram(VERTEX, RECOLOUR_FRAGMENT);
        if (!recolour.isCompiled()) {
            throw new IllegalStateException("recolour shader: " + recolour.getLog());
        }
        cracks = new ShaderProgram(VERTEX, CRACKS_FRAGMENT);
        if (!cracks.isCompiled()) {
            throw new IllegalStateException("cracks shader: " + cracks.getLog());
        }
    }

    /**
     * Step GFX2: every draw until {@link #end} recoloured by its light towards {@code colour}, {@code amount} (0..1) of
     * the way (a glowing shot drawn in another colour than its art's: the rear-guard drone's gold bolt).
     */
    public void beginRecolour(SpriteBatch batch, Color colour, float amount) {
        batch.setShader(recolour);
        recolour.setUniformf("u_flash", colour.r, colour.g, colour.b, amount);
    }

    /** Draws {@code region} centred on (x, y), blended by {@code amount} (0..1) towards {@code colour}. */
    public void draw(SpriteBatch batch, TextureRegion region, float x, float y, Color colour, float amount) {
        batch.setShader(program);
        program.setUniformf("u_flash", colour.r, colour.g, colour.b, amount);
        batch.draw(region, x - region.getRegionWidth() / 2f, y - region.getRegionHeight() / 2f);
        batch.setShader(null);
    }

    /**
     * M5 part F: every draw until {@link #end} blended by {@code amount} towards {@code colour} (Level
     * 12's cloud banks lit from inside by a lightning flash).
     */
    public void begin(SpriteBatch batch, Color colour, float amount) {
        batch.setShader(program);
        program.setUniformf("u_flash", colour.r, colour.g, colour.b, amount);
    }

    /**
     * Step GFX3: every draw until {@link #end} shows only its dark violet cracks, in {@code colour} at {@code amount}
     * (0..1) of their opacity (drawn additively over the sprite: the cracks glowing).
     */
    public void beginCracks(SpriteBatch batch, Color colour, float amount) {
        batch.setShader(cracks);
        cracks.setUniformf("u_flash", colour.r, colour.g, colour.b, amount);
    }

    /** Back to the batch's own shader after {@link #begin}. */
    public void end(SpriteBatch batch) {
        batch.setShader(null);
    }

    @Override
    public void dispose() {
        program.dispose();
        recolour.dispose();
        cracks.dispose();
    }
}
