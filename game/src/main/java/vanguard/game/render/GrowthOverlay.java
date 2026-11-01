package vanguard.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import vanguard.content.FallingCityData;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M10 part C, step C4a (design/campaign Level 37, The Vrell turn): the backdrop's {@code growth} burst over the ground at
 * the turn. From its {@code t} its image lies on its ground rows (as a ground piece with its {@code bottom} row) over the
 * city's pieces, each pixel shown once the reveal map's value is at most {@code (now − t) ÷ seconds × 255} (one texture
 * and a threshold in a shader: the teal growth racing from the dome rims along the seams), its glow additive under the
 * same reveal; once revealed it is drawn as a plain ground piece. Its texels below the shadows' ground threshold are
 * discarded, so it marks the shadow stencil as the city's pieces do ({@link Shadows}). Nothing is allocated per frame.
 */
final class GrowthOverlay {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final int HEIGHT = PlayField.HEIGHT;

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
     * The drawn region's texture coordinates ({@code u_from}: u, v, u2, v2) mapped onto the reveal map's ({@code u_to}),
     * the same size: a texel shows where the map's value is at most {@code u_threshold}. High precision where the
     * device has it (a 2048 px page needs more than mediump's steps).
     */
    private static final String FRAGMENT = """
            #ifdef GL_ES
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform sampler2D u_reveal;
            uniform vec4 u_from;
            uniform vec4 u_to;
            uniform float u_threshold;
            void main() {
                vec4 texel = texture2D(u_texture, v_texCoords);
                vec2 k = (v_texCoords - u_from.xy) / (u_from.zw - u_from.xy);
                float reveal = texture2D(u_reveal, mix(u_to.xy, u_to.zw, k)).r;
                if (texel.a < 1.0 / 512.0 || reveal > u_threshold) {
                    discard;
                }
                gl_FragColor = v_color * texel;
            }
            """;

    private final FallingCityData.Growth growth;
    private final AtlasRegion image;
    private final AtlasRegion reveal;
    private final AtlasRegion glow;
    private final ShaderProgram shader;

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     */
    GrowthOverlay(Sprites sprites, LevelData level, String folder) {
        growth = level.backdrop().growth().orElseThrow();
        image = sprites.backdrop(folder + growth.piece(), 1).first();
        reveal = sprites.backdrop(folder + growth.reveal(), 1).first();
        glow = sprites.backdrop(folder + growth.glow(), 1).first();
        if (reveal.getRegionWidth() != image.getRegionWidth()
                || reveal.getRegionHeight() != image.getRegionHeight()
                || glow.getRegionWidth() != image.getRegionWidth()
                || glow.getRegionHeight() != image.getRegionHeight()) {
            throw new IllegalStateException("'" + growth.piece() + "': its reveal map and glow are its size");
        }
        shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("growth shader: " + shader.getLog());
        }
    }

    void dispose() {
        shader.dispose();
    }

    /**
     * The reveal threshold at level time {@code script}: the map's values shown, 0..1 (its value ÷ 255, half a step of
     * slack); negative before the burst.
     */
    static float threshold(FallingCityData.Growth growth, double script) {
        if (script < growth.t()) {
            return -1;
        }
        double share = Math.clamp((script - growth.t()) / growth.seconds(), 0, 1);
        return (float) ((share * 255 + 0.5) / 255);
    }

    /** The burst over the ground scrolled by {@code groundScroll}, from its {@code t} (the ground pass). */
    void draw(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        float threshold = threshold(growth, clock.script());
        if (threshold < 0) {
            return;
        }
        long y = growth.bottom() - Math.round(groundScroll);
        if (y >= HEIGHT || y + image.getRegionHeight() <= 0) {
            return;
        }
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        if (threshold >= 1) {
            // Revealed: a ground piece and its light (the batch's shader marks the stencil as for every piece).
            batch.draw(image, X0, y);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.draw(glow, X0, y);
            batch.setBlendFunction(src, dst);
            return;
        }
        ShaderProgram before = batch.getShader();
        batch.setShader(shader);
        reveal.getTexture().bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        shader.setUniformi("u_reveal", 1);
        shader.setUniformf("u_threshold", threshold);
        shader.setUniformf("u_to", reveal.getU(), reveal.getV(), reveal.getU2(), reveal.getV2());
        shader.setUniformf("u_from", image.getU(), image.getV(), image.getU2(), image.getV2());
        batch.draw(image, X0, y);
        batch.flush();
        shader.setUniformf("u_from", glow.getU(), glow.getV(), glow.getU2(), glow.getV2());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.draw(glow, X0, y);
        batch.setBlendFunction(src, dst);
        batch.setShader(before);
    }
}
