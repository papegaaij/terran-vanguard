package vanguard.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G4 (design/campaign Level 49, the memory; ART-G's {@code backdrop-proposal.yaml}): the Choir's
 * memories over the tissue. A memory is a section's stretch of the backdrop: its deep piece(s) and its low-air and
 * ground tile sets. Each is drawn over the <b>whole screen</b> while it shows (its tile sets wrap; not from a seam, so
 * gate 2's floor lies under the Dust Colossus at once) under one visibility read from {@code memory-mask} (a data
 * image, one texel a screen pixel: R the forming threshold, top edge down; G the melting one, sides first):
 * {@code smoothstep(R − 0.08, R + 0.08, p·1.16 − 0.08)} while it forms and {@code 1 − smoothstep(G − 0.08, G + 0.08,
 * q·1.16 − 0.08)} while it melts, p and q 0 → 1 over {@value #SECONDS} s: no flash, the luminance one way.
 *
 * <p>A memory forms at its section's start on the level clock (the approach's at 0, 31 and 61; a gate's at its warning,
 * the section's start) and melts at its end (31, 61, 90), a gate's when its echo dissolves ({@code ECHO_DISSOLVE}: the
 * fight's echo, by the gates' order), the last section's never. A melting memory is kept until its melt ends. The
 * crawl: a memory's pieces lie where the data places them relative to the ground as it was when the memory formed, so
 * a gate whose ground crawled on while the clock stood (every gate of a rush) moves none of the later ones. Nothing is
 * allocated per frame.
 */
final class MemoryLooks implements Disposable {
    /** The data image's name in the level's backdrop folder. */
    static final String MASK = "memory-mask";
    /** How long a memory forms and melts, s. */
    static final double SECONDS = 2;

    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            varying vec2 v_place;
            void main() {
                v_color = a_color;
                v_color.a = v_color.a * (255.0 / 254.0);
                v_texCoords = a_texCoord0;
                v_place = a_position.xy;
                gl_Position = u_projTrans * a_position;
            }
            """;

    /**
     * {@code u_mask}: the mask's texture (unit 1); {@code u_maskRegion}: its region's (u, v) top left and (u2, v2)
     * bottom right; {@code u_field}: the play field's left edge and size (x0, w, h); {@code u_vis}: p and q.
     */
    private static final String FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            varying vec2 v_place;
            uniform sampler2D u_texture;
            uniform sampler2D u_mask;
            uniform vec4 u_maskRegion;
            uniform vec3 u_field;
            uniform vec2 u_vis;
            void main() {
                vec4 texel = v_color * texture2D(u_texture, v_texCoords);
                vec2 at = clamp(vec2((v_place.x - u_field.x) / u_field.y, v_place.y / u_field.z), 0.0, 1.0);
                vec2 thresholds = texture2D(u_mask, vec2(mix(u_maskRegion.x, u_maskRegion.z, at.x),
                        mix(u_maskRegion.w, u_maskRegion.y, at.y))).rg;
                float formed = smoothstep(thresholds.r - 0.08, thresholds.r + 0.08, u_vis.x * 1.16 - 0.08);
                float melted = smoothstep(thresholds.g - 0.08, thresholds.g + 0.08, u_vis.y * 1.16 - 0.08);
                gl_FragColor = vec4(texel.rgb, texel.a * formed * (1.0 - melted));
            }
            """;

    /** A memory's deep piece: its image, its additive light (null without), its x and its centre on the deep layer. */
    private record Piece(TextureRegion image, TextureRegion glow, float x, double centre) {}

    /** One memory: its section's times, its gate's fight (-1: none), its pieces and tile sets. */
    private static final class Memory {
        final double formAt;
        final double endAt;
        final int fight;
        final boolean last;
        final List<Piece> pieces = new ArrayList<>();
        final List<BackdropTiles.Tiles> tileSets = new ArrayList<>();
        final List<BackdropLayer> layers = new ArrayList<>();
        /** The ground's actual scroll less its planned at the memory's forming (the crawls before it); NaN unknown. */
        double groundShift = Double.NaN;
        /** The real time its echo dissolved (its melt's start), NaN while it lives. */
        double dissolvedAt = Double.NaN;
        /** p and q now. */
        float formed;

        float melted;

        Memory(double formAt, double endAt, int fight, boolean last) {
            this.formAt = formAt;
            this.endAt = endAt;
            this.fight = fight;
            this.last = last;
        }

        boolean shows() {
            return formed > 0 && melted < 1;
        }
    }

    private final LevelData level;
    private final BackdropTiles tiles;
    private final AtlasRegion mask;
    private final Memory[] memories;
    private final double deepFactor;
    private ShaderProgram program;
    /** The ground's scroll at the last update, px. */
    private double ground;

    private MemoryLooks(Sprites sprites, LevelData level, String folder, BackdropTiles tiles) {
        this.level = level;
        this.tiles = tiles;
        mask = sprites.backdrop(folder + MASK, 1).first();
        BackdropData data = level.backdrop();
        deepFactor = data.factor(BackdropLayer.DEEP);
        List<SectionsBlock.Section> sections = level.sections();
        List<Memory> found = new ArrayList<>();
        Memory[] bySection = new Memory[sections.size()];
        int arenas = 0;
        for (int s = 0; s < sections.size(); s++) {
            SectionsBlock.Section section = sections.get(s);
            int fight = section.isArena() ? arenas++ : -1;
            Memory memory = new Memory(level.sectionStart(s), section.end(), fight, s == sections.size() - 1);
            for (String id : section.tiles()) {
                BackdropData.TileSet tileSet = data.tileSets().get(id);
                if (tileSet.layer() != BackdropLayer.DEEP) {
                    memory.tileSets.add(BackdropTiles.tiles(sprites, folder, id, tileSet));
                    memory.layers.add(tileSet.layer());
                }
            }
            bySection[s] = memory;
        }
        for (BackdropData.PlacedPiece placed : data.placed()) {
            BackdropData.Piece piece = data.pieces().get(placed.piece());
            if (piece.layer() != BackdropLayer.DEEP) {
                continue;
            }
            int s = sectionAt(sections, placed.t());
            String glow = folder + placed.piece() + "-glow";
            bySection[s].pieces.add(new Piece(
                    sprites.backdrop(folder + placed.piece(), 1).first(),
                    sprites.hasBackdrop(glow) ? sprites.backdrop(glow, 1).first() : null,
                    (float) placed.x(),
                    level.pieceCentre(placed)));
        }
        for (Memory memory : bySection) {
            if (!memory.pieces.isEmpty() || !memory.tileSets.isEmpty()) {
                found.add(memory);
            }
        }
        memories = found.toArray(Memory[]::new);
    }

    /** The memory looks of a level whose backdrop has the {@value #MASK} data image (Level 49); null without. */
    static MemoryLooks of(Sprites sprites, LevelData level, String folder, BackdropTiles tiles) {
        return sprites.hasBackdrop(folder + MASK) ? new MemoryLooks(sprites, level, folder, tiles) : null;
    }

    /** The section (0-based) whose stretch holds level time {@code t}. */
    static int sectionAt(List<SectionsBlock.Section> sections, double t) {
        for (int s = 0; s < sections.size(); s++) {
            if (t < sections.get(s).end()) {
                return s;
            }
        }
        return sections.size() - 1;
    }

    /** How far a memory has formed or melted {@code since} s after it began, 0 to 1. */
    static float share(double since) {
        return (float) Math.clamp(since / SECONDS, 0, 1);
    }

    /** A memory's visibility at a texel with thresholds {@code r} (forming) and {@code g} (melting), the shader's twin. */
    static float visibility(float r, float g, float formed, float melted) {
        return smooth(r - 0.08f, r + 0.08f, formed * 1.16f - 0.08f)
                * (1 - smooth(g - 0.08f, g + 0.08f, melted * 1.16f - 0.08f));
    }

    private static float smooth(float from, float to, float x) {
        float t = Math.clamp((x - from) / (to - from), 0, 1);
        return t * t * (3 - 2 * t);
    }

    /**
     * Once a frame: the memories' forming and melting at drawn level time {@code seconds}, real time {@code real} and the
     * ground's {@code groundScroll}; a gate's memory melts once its fight's echo has dissolved.
     */
    void update(Sortie sortie, double seconds, double real, double groundScroll) {
        ground = groundScroll;
        var fights = sortie.bosses();
        for (Memory memory : memories) {
            memory.formed = share(seconds - memory.formAt);
            if (seconds < memory.formAt) {
                // Not formed yet (or a restart before it): its anchor is taken again as it forms.
                memory.groundShift = Double.NaN;
            } else if (Double.isNaN(memory.groundShift)) {
                memory.groundShift = groundScroll - level.scrollAt(Math.max(seconds, memory.formAt));
            }
            if (memory.fight >= 0 && memory.fight < fights.fights()) {
                boolean dissolved = sortie.setPiece(fights.fightPiece(memory.fight))
                        .echo()
                        .map(echo -> echo.dissolved())
                        .orElse(false);
                if (!dissolved) {
                    memory.dissolvedAt = Double.NaN;
                } else if (Double.isNaN(memory.dissolvedAt)) {
                    memory.dissolvedAt = real;
                }
                memory.melted = Double.isNaN(memory.dissolvedAt) ? 0 : share(real - memory.dissolvedAt);
            } else {
                memory.melted = memory.last ? 0 : share(seconds - memory.endAt);
            }
        }
    }

    /** M11 part G: the memories' p and q now (memory by memory, the data's order), for the tests. */
    float formed(int memory) {
        return memories[memory].formed;
    }

    float melted(int memory) {
        return memories[memory].melted;
    }

    int count() {
        return memories.length;
    }

    /** The deep layer: each showing memory's pieces (and their lights, additive) over the tissue. */
    void drawDeep(SpriteBatch batch) {
        for (Memory memory : memories) {
            if (!memory.shows() || memory.pieces.isEmpty()) {
                continue;
            }
            begin(batch, memory);
            double scroll = (ground - (Double.isNaN(memory.groundShift) ? 0 : memory.groundShift)) * deepFactor;
            for (Piece piece : memory.pieces) {
                float y = (float) (piece.centre() - scroll);
                draw(batch, piece.image(), piece.x(), y);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (Piece piece : memory.pieces) {
                if (piece.glow() != null) {
                    draw(batch, piece.glow(), piece.x(), (float) (piece.centre() - scroll));
                }
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            end(batch);
        }
    }

    /** A layer's tile sets of each showing memory over the whole screen ({@code low-air}, {@code ground}). */
    void drawLayer(SpriteBatch batch, BackdropLayer layer, double seconds) {
        long scroll = tiles.scroll(layer, ground);
        for (Memory memory : memories) {
            if (!memory.shows() || !memory.layers.contains(layer)) {
                continue;
            }
            begin(batch, memory);
            for (int k = 0; k < memory.tileSets.size(); k++) {
                if (memory.layers.get(k) == layer) {
                    tiles.drawTiles(batch, memory.tileSets.get(k), scroll, scroll + HEIGHT, scroll, seconds, 0);
                }
            }
            end(batch);
        }
    }

    private static void draw(SpriteBatch batch, TextureRegion image, float x, float y) {
        float w = image.getRegionWidth();
        float h = image.getRegionHeight();
        if (y + h / 2 < 0 || y - h / 2 > HEIGHT) {
            return;
        }
        batch.draw(image, X0 + x - w / 2, y - h / 2);
    }

    private void begin(SpriteBatch batch, Memory memory) {
        if (program == null) {
            program = new ShaderProgram(VERTEX, FRAGMENT);
            if (!program.isCompiled()) {
                throw new IllegalStateException("memory shader: " + program.getLog());
            }
        }
        batch.setShader(program);
        mask.getTexture().bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        program.setUniformi("u_mask", 1);
        program.setUniformf("u_maskRegion", mask.getU(), mask.getV(), mask.getU2(), mask.getV2());
        program.setUniformf("u_field", X0, WIDTH, HEIGHT);
        program.setUniformf("u_vis", memory.formed, memory.melted);
    }

    private static void end(SpriteBatch batch) {
        batch.setShader(null);
    }

    @Override
    public void dispose() {
        if (program != null) {
            program.dispose();
            program = null;
        }
    }
}
