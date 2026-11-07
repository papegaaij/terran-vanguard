package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;

/**
 * M11 part G, step G4: the game's sprite batch, which can draw through {@link EchoTint}'s shader around the passes that
 * ask for it. While tinting, every region drawn hands the shader its own bounds and erosion edge (flushing first when
 * they change), a draw's own shader (a hit flash's) is drawn as it is and the batch's own shader returns to the tint
 * after it, and an additive draw is told to the shader. Outside a tint it draws exactly as a {@link SpriteBatch}.
 * Nothing is allocated per draw.
 */
public final class TintBatch extends SpriteBatch {
    /** The tint's shader while tinting; null otherwise. */
    private ShaderProgram tint;

    private float shimmer = 1;
    /** What the shader was last told: the texture, the region's bounds, the edge and whether additive. */
    private Texture told;

    private float toldU;
    private float toldV;
    private float toldU2;
    private float toldV2;
    private boolean toldAdditive;
    private boolean toldErosion;

    /** Starts ({@code program} non-null, at shimmer factor {@code shimmerFactor}) or ends the tint. */
    void tint(ShaderProgram program, float shimmerFactor) {
        tint = null;
        super.setShader(program);
        tint = program;
        shimmer = shimmerFactor;
        told = null;
    }

    /** Whether the batch draws through the tint now. */
    public boolean tinting() {
        return tint != null;
    }

    @Override
    public void setShader(ShaderProgram shader) {
        // While tinting, the batch's own shader is the tint's: a draw's own shader returns to it.
        super.setShader(shader == null && tint != null ? tint : shader);
        told = null;
    }

    /** Tells the shader about the region about to be drawn (u, v to u2, v2 of {@code texture}, erosion or not). */
    private void region(Texture texture, float u, float v, float u2, float v2, int w, int h, boolean erosion) {
        if (tint == null || !isDrawing() || getShader() != tint) {
            return;
        }
        boolean additive = getBlendDstFunc() == GL20.GL_ONE;
        if (texture == told
                && u == toldU
                && v == toldV
                && u2 == toldU2
                && v2 == toldV2
                && additive == toldAdditive
                && erosion == toldErosion) {
            return;
        }
        flush();
        told = texture;
        toldU = u;
        toldV = v;
        toldU2 = u2;
        toldV2 = v2;
        toldAdditive = additive;
        toldErosion = erosion;
        float sigma = erosion ? EchoTint.edgePx(w, h) / 2.5f : 0;
        tint.setUniformf("u_region", Math.min(u, u2), Math.min(v, v2), Math.max(u, u2), Math.max(v, v2));
        tint.setUniformf("u_sigma", sigma / texture.getWidth(), sigma / texture.getHeight());
        tint.setUniformf("u_tint", shimmer, additive ? 1 : 0);
    }

    private void region(TextureRegion region) {
        region(
                region.getTexture(),
                region.getU(),
                region.getV(),
                region.getU2(),
                region.getV2(),
                region.getRegionWidth(),
                region.getRegionHeight(),
                true);
    }

    /** A texture drawn without a region: tinted, not eroded. */
    private void plain(Texture texture) {
        region(texture, 0, 0, 1, 1, texture.getWidth(), texture.getHeight(), false);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        region(region);
        super.draw(region, x, y, width, height);
    }

    @Override
    public void draw(
            TextureRegion region,
            float x,
            float y,
            float originX,
            float originY,
            float width,
            float height,
            float scaleX,
            float scaleY,
            float rotation) {
        region(region);
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    @Override
    public void draw(
            TextureRegion region,
            float x,
            float y,
            float originX,
            float originY,
            float width,
            float height,
            float scaleX,
            float scaleY,
            float rotation,
            boolean clockwise) {
        region(region);
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        region(region);
        super.draw(region, width, height, transform);
    }

    @Override
    public void draw(
            Texture texture,
            float x,
            float y,
            float originX,
            float originY,
            float width,
            float height,
            float scaleX,
            float scaleY,
            float rotation,
            int srcX,
            int srcY,
            int srcWidth,
            int srcHeight,
            boolean flipX,
            boolean flipY) {
        plain(texture);
        super.draw(
                texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth,
                srcHeight, flipX, flipY);
    }

    @Override
    public void draw(
            Texture texture,
            float x,
            float y,
            float width,
            float height,
            int srcX,
            int srcY,
            int srcWidth,
            int srcHeight,
            boolean flipX,
            boolean flipY) {
        plain(texture);
        super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        plain(texture);
        super.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);
    }

    @Override
    public void draw(
            Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        plain(texture);
        super.draw(texture, x, y, width, height, u, v, u2, v2);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        plain(texture);
        super.draw(texture, x, y, width, height);
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        plain(texture);
        super.draw(texture, spriteVertices, offset, count);
    }
}
