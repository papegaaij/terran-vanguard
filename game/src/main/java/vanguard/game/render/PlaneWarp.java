package vanguard.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import vanguard.sim.PlayField;

/**
 * M11 part B, step B4 (design/world/vrell-space, Parallax layers: the event plane's slow smooth warp; Level 44): the
 * deep layer's slow warp. Right after the deep layer is drawn the field is copied and drawn back displaced by ART-B's
 * map ({@code level-44/plane-warp}, tools/art/backdrop_l44.py: red and green the x and y displacement, 128 none, ±127
 * ±{@value #REACH} px; smooth and periodic every {@value #PERIOD} px), the map drifting {@value #DRIFT} of a period a
 * second (a point's wobble well under 0.5 Hz). The map is a region of the backdrop atlas: the shader wraps it inside
 * the region itself. {@link #displacement} is its twin on the CPU, for the tests. Nothing is allocated per frame.
 */
final class PlaneWarp implements Disposable {
    /** The map's name in a level's backdrop folder. */
    static final String MAP = "plane-warp";
    /** The map's full displacement, px. */
    static final float REACH = 8;
    /** The map repeats every this many px on the field. */
    static final float PERIOD = 256;
    /** The map drifts this many periods a second (x; y at 0.7 of it). */
    static final float DRIFT = 0.1f;

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
            uniform sampler2D u_map;
            uniform vec4 u_region;
            uniform vec2 u_field;
            uniform vec2 u_offset;
            uniform float u_period;
            uniform float u_reach;
            void main() {
                vec2 px = v_texCoords * u_field;
                vec2 cell = fract(px / u_period + u_offset);
                vec2 at = mix(u_region.xy, u_region.zw, cell);
                vec2 d = (texture2D(u_map, at).rg - vec2(128.0 / 255.0)) * (255.0 / 127.0) * u_reach;
                vec2 from = clamp((px + d) / u_field, vec2(0.0), vec2(1.0));
                gl_FragColor = vec4(texture2D(u_texture, from).rgb, 1.0);
            }
            """;

    private final AtlasRegion map;
    private ShaderProgram shader;
    private Texture copy;

    PlaneWarp(AtlasRegion map) {
        this.map = map;
    }

    /** The level's warp from its backdrop folder's map; null for a level without one. */
    static PlaneWarp of(Sprites sprites, String folder) {
        String name = folder + MAP;
        if (sprites == null || !sprites.hasBackdrop(name)) {
            return null;
        }
        AtlasRegion map = sprites.backdrop(name, 1).first();
        // A rotated region would turn the map's axes: no warp then (the packer keeps the backdrops upright).
        return map.rotate ? null : new PlaneWarp(map);
    }

    /** The map's offset at real {@code seconds}, periods (x and y). */
    static float offsetX(double seconds) {
        return (float) ((seconds * DRIFT) % 1);
    }

    static float offsetY(double seconds) {
        return (float) ((seconds * DRIFT * 0.7) % 1);
    }

    /** A map texel's displacement, px: its channel (0-255) to ±{@value #REACH}, 128 none. */
    static float displacement(int channel) {
        return (channel - 128) / 127f * REACH;
    }

    /** Draws the deep layer drawn so far back through the warp at real {@code seconds} (the batch drawing). */
    void apply(SpriteBatch batch, double seconds) {
        if (shader == null) {
            shader = new ShaderProgram(VERTEX, FRAGMENT);
            if (!shader.isCompiled()) {
                throw new IllegalStateException("plane warp shader: " + shader.getLog());
            }
            copy = new Texture(WIDTH, HEIGHT, Pixmap.Format.RGBA8888);
            copy.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        batch.flush();
        copy.bind();
        Gdx.gl.glCopyTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, (int) X0, 0, WIDTH, HEIGHT);
        map.getTexture().bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        batch.disableBlending();
        batch.setShader(shader);
        shader.setUniformi("u_map", 1);
        // Half a texel inside the region, so the wrap never samples a neighbour on the atlas page.
        float halfU = 0.5f / map.getTexture().getWidth();
        float halfV = 0.5f / map.getTexture().getHeight();
        shader.setUniformf(
                "u_region", map.getU() + halfU, map.getV() + halfV, map.getU2() - halfU, map.getV2() - halfV);
        shader.setUniformf("u_field", WIDTH, HEIGHT);
        shader.setUniformf("u_offset", offsetX(seconds), offsetY(seconds));
        shader.setUniformf("u_period", PERIOD);
        shader.setUniformf("u_reach", REACH);
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
