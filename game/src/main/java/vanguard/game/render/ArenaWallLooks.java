package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;
import vanguard.sim.ArenaWalls;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H4 (design/enemies/bosses/choir-heart, Arena: the walls of phase 2; ART-H's {@code
 * choir-heart-wall-l/-r} with their {@code -edge} and {@code -veins}, {@code pivots/choir-heart.json}'s {@code walls}):
 * the arena's walls as ribbed tissue strips along the simulation's faces ({@link ArenaWalls#leftAt}, {@link
 * ArenaWalls#rightAt}): each {@value #SLICE} px row band of the 128x256 strip (wrapping vertically) is drawn with its
 * face column ({@code face_x}: 112 on the left strip, 15 on the right) on the face at that height, so the tissue follows
 * the halves, the join between them and their breathing; its frame the breath's phase (0.5 Hz x 8). Over the dark
 * ({@link #drawGlows}): the lit inner edge (additive, always: the walls read in the dark) and the veins' glow while a
 * half is told (its 1.0 s tell before a move), eased in and out over {@value #VEIN_EASE} s; step G6: and on both walls
 * over a wall spit's 0.8 s tell ({@link HeartLooks#spitTelling}: its vein points are drawn as it fires, so the whole
 * veins tell it). A wall at its edge (no rock) draws nothing. Nothing is allocated per frame.
 */
final class ArenaWallLooks {
    /** The strips are drawn in row bands this tall, px. */
    static final int SLICE = 2;
    /** The breath: its rate (the walls' 0.5 Hz) and the strip's frames over one. */
    static final double BREATH_HZ = 0.5;
    /** The veins' glow eases in and out over this long, s. */
    static final double VEIN_EASE = 0.25;

    private final List<Array<AtlasRegion>> tissue = new ArrayList<>();
    private final List<Array<AtlasRegion>> edge = new ArrayList<>();
    private final List<Array<AtlasRegion>> veins = new ArrayList<>();
    /** The face column in the left and the right strip, px from the strip's left. */
    private final int[] faceX;

    private final TextureRegion slice = new TextureRegion();
    /** Per half its veins' glow now, 0-1 (eased). */
    private final float[] glow = new float[4];

    private double lastSeconds = Double.NaN;

    private ArenaWallLooks(Sprites sprites, JsonValue walls) {
        String[] sides = {"l", "r"};
        faceX = new int[2];
        for (int s = 0; s < 2; s++) {
            String name = HeartLooks.SLUG + "-wall-" + sides[s];
            tissue.add(frames(sprites, name));
            edge.add(frames(sprites, name + "-edge"));
            veins.add(frames(sprites, name + "-veins"));
            faceX[s] = walls == null ? (s == 0 ? 112 : 15) : walls.get("face_x").getInt(sides[s]);
        }
    }

    private static Array<AtlasRegion> frames(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The walls' looks for the Choir Heart's level (its pivots' {@code walls}); null without the strips. */
    static ArenaWallLooks of(Sprites sprites, JsonValue pivots) {
        if (sprites == null || pivots == null || !sprites.has(HeartLooks.SLUG + "-wall-l")) {
            return null;
        }
        return new ArenaWallLooks(sprites, pivots.get("walls"));
    }

    /** The breath's frame at {@code seconds} of {@code frames}. */
    static int breathFrame(double seconds, int frames) {
        double phase = seconds * BREATH_HZ;
        return (int) Math.floor((phase - Math.floor(phase)) * frames) % Math.max(1, frames);
    }

    /** The walls' tissue (the normal pass, darkened in phase 2 with the field). */
    void draw(SpriteBatch batch, Sortie sortie, double seconds) {
        ArenaWalls walls = sortie.arenaWalls();
        if (walls == null || !walls.standing()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawStrips(batch, walls, tissue, seconds);
    }

    /** Over the dark: the walls' lit inner edge, and their veins while told. */
    void drawGlows(SpriteBatch batch, Sortie sortie, double seconds) {
        ArenaWalls walls = sortie.arenaWalls();
        double step = Double.isNaN(lastSeconds) ? 0 : Math.max(0, seconds - lastSeconds);
        lastSeconds = seconds;
        boolean spitting = HeartLooks.spitTelling(HeartLooks.heart(sortie));
        for (int h = 0; h < glow.length; h++) {
            float target = walls != null && (walls.told(h) || spitting) ? 1 : 0;
            float move = (float) (step / VEIN_EASE);
            glow[h] = target > glow[h] ? Math.min(target, glow[h] + move) : Math.max(target, glow[h] - move);
        }
        if (walls == null || !walls.standing()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawStrips(batch, walls, edge, seconds);
        for (int s = 0; s < 2; s++) {
            float lower = glow[2 * s + 1];
            float upper = glow[2 * s];
            if (lower <= 0 && upper <= 0) {
                continue;
            }
            drawSide(batch, walls, veins.get(s), s, seconds, lower, upper);
        }
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawStrips(SpriteBatch batch, ArenaWalls walls, List<Array<AtlasRegion>> sets, double seconds) {
        for (int s = 0; s < 2; s++) {
            drawSide(batch, walls, sets.get(s), s, seconds, 1, 1);
        }
        batch.setColor(1, 1, 1, 1);
    }

    /**
     * One wall's strip along its face, at {@code lower}'s alpha below the split and {@code upper}'s above it. Across the
     * join (step GH5) the strip goes in 1 px rows along an organic face ({@link #joinFace}), each row softened: drawn
     * once more at the next row's face at half alpha, so the step between two rows is a soft fringe, not a hard stair.
     */
    private void drawSide(
            SpriteBatch batch,
            ArenaWalls walls,
            Array<AtlasRegion> frames,
            int s,
            double seconds,
            float lower,
            float upper) {
        if (frames.isEmpty()) {
            return;
        }
        boolean additive = batch.getBlendDstFunc() == GL20.GL_ONE;
        AtlasRegion image = frames.get(breathFrame(seconds, frames.size));
        int width = image.getRegionWidth();
        int height = image.getRegionHeight();
        double split = walls.split();
        double below = s == 0 ? walls.leftAt(split - ArenaWalls.JOIN) : walls.rightAt(split - ArenaWalls.JOIN);
        double above = s == 0 ? walls.leftAt(split + ArenaWalls.JOIN) : walls.rightAt(split + ArenaWalls.JOIN);
        int y = 0;
        while (y < PlayField.HEIGHT) {
            boolean inJoin = Math.abs(y + 1 - split) < ArenaWalls.JOIN + SLICE && Math.abs(above - below) >= 0.5;
            // 1 px rows across the join, and one more where a band would start on an odd row (the strip's wrap)
            int step = inJoin || y % SLICE != 0 ? 1 : SLICE;
            double mid = y + step / 2.0;
            double face =
                    inJoin ? joinFace(below, above, mid, split, s) : s == 0 ? walls.leftAt(mid) : walls.rightAt(mid);
            int at = y;
            y += step;
            if (s == 0 ? face <= 0.5 : face >= PlayField.WIDTH - 0.5) {
                continue;
            }
            float a = mid < split ? lower : upper;
            if (a <= 0) {
                continue;
            }
            // The strip's rows wrap; its top row is the highest: screen row y reads strip row (height - 1 - y mod h).
            int inStrip = Math.floorMod(at, height);
            int rows = Math.min(step, height - inStrip);
            int top = height - inStrip - rows;
            slice.setRegion(image, 0, top, width, rows);
            if (inJoin) {
                double next = joinFace(below, above, mid + 1, split, s);
                // The fringe: the row once more where the next row's face is, at half alpha; drawn first so the row
                // itself covers it where both have tissue (in the additive glows the two halves share the light).
                if (Math.abs(next - face) >= 1) {
                    batch.setColor(1, 1, 1, a * 0.5f);
                    batch.draw(slice, Math.round(X0 + next - faceX[s]), at);
                    if (additive) {
                        batch.setColor(1, 1, 1, a * 0.5f);
                        batch.draw(slice, Math.round(X0 + face - faceX[s]), at);
                        continue;
                    }
                }
            }
            batch.setColor(1, 1, 1, a);
            batch.draw(slice, Math.round(X0 + face - faceX[s]), at);
        }
    }

    /**
     * Step GH5: the face across the join as drawn, px from the left edge: from the lower half's face ({@code below}) to
     * the upper's ({@code above}) along an eased bend (half the simulation's straight run, half a raised cosine) with a
     * soft wobble (two sines, out of phase per wall), largest mid-join and none at its ends, so the join is tissue, not a
     * straight line. Within a few px of the simulation's face.
     */
    static double joinFace(double below, double above, double y, double split, int side) {
        double t = (y - (split - ArenaWalls.JOIN)) / (2 * ArenaWalls.JOIN);
        if (t <= 0) {
            return below;
        }
        if (t >= 1) {
            return above;
        }
        double eased = 0.5 * t + 0.25 * (1 - Math.cos(Math.PI * t));
        double diff = above - below;
        double u = y - split;
        double wobble = 0.6 * Math.sin(u * 0.27 + 1.3 + 2.1 * side) + 0.4 * Math.sin(u * 0.61 + 4.1 + 0.7 * side);
        double amplitude = Math.min(5, 1.5 + 0.06 * Math.abs(diff)) * Math.sin(Math.PI * t);
        return below + diff * eased + amplitude * wobble;
    }
}
