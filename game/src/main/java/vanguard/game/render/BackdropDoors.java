package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;

/**
 * M10 part G, step G4 (design/campaign Level 41, the gates' blast doors; tools/art/backdrop_l41.py's {@code
 * gate-N-doors_0..11}): the held gates' doors, the backdrop's {@code doors} rows, drawn on the ground layer over the
 * fortress's pieces at their rows ({@code t}, {@code x}, as placed pieces): shut (frame 0) from the level's start; at
 * their gate's {@code GATE_OPENED} the frames play once over the row's {@code seconds} (the leaves parting along the
 * scroll's own ease) and the last (open) holds. After a restart from the Halo's checkpoint the gates already passed stand
 * open ({@link #openedBefore}). Presentation only; nothing is allocated per frame.
 */
final class BackdropDoors {
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final long NEVER = Long.MIN_VALUE / 2;
    /** A door opened before the restart: its last frame from the start. */
    private static final long LONG_AGO = Long.MIN_VALUE / 4;

    private final List<BackdropData.Door> doors;
    private final Array<Array<AtlasRegion>> frames = new Array<>();
    private final long[] bottom;
    private final float[] width;
    private final float[] height;
    private final int[] ticks;
    /** Per door the step its gate opened (NEVER while shut). */
    private final long[] openedAt;
    /** The last step seen ({@link #stepped}); whether a restart's first step is still to come. */
    private long now;

    private boolean fresh = true;

    BackdropDoors(Sprites sprites, LevelData level, String folder) {
        BackdropData data = level.backdrop();
        doors = data.doors().orElse(List.of());
        bottom = new long[doors.size()];
        width = new float[doors.size()];
        height = new float[doors.size()];
        ticks = new int[doors.size()];
        openedAt = new long[doors.size()];
        for (int d = 0; d < doors.size(); d++) {
            BackdropData.Door door = doors.get(d);
            BackdropData.Piece spec = data.pieces().get(door.piece());
            frames.add(sprites.backdrop(folder + door.piece(), spec.imageCount()));
            double centre = level.scrollAt(door.t()) * data.factor(BackdropLayer.GROUND) + HEIGHT / 2.0;
            bottom[d] = Math.round(centre - spec.size().height() / 2);
            width[d] = (float) spec.size().width();
            height[d] = (float) spec.size().height();
            ticks[d] = Math.max(1, SimStep.ticks(door.seconds()));
        }
        restart();
    }

    /** Whether the level has doors. */
    boolean any() {
        return !doors.isEmpty();
    }

    /** The level restarts: every door shut. */
    void restart() {
        Arrays.fill(openedAt, NEVER);
        fresh = true;
    }

    /**
     * After each step at step {@code tick}, the held gate now {@code gate} (from 0): the first step after a restart opens
     * the gates already passed; then each {@code GATE_OPENED} ({@link #opened}) is the caller's.
     */
    void stepped(long tick, int gate) {
        now = tick;
        if (fresh) {
            fresh = false;
            openedBefore(gate);
        }
    }

    /** Held gate {@code gate} (from 0, {@code GATE_OPENED}'s value) opened at step {@code tick}: its doors part. */
    void opened(int gate, long tick) {
        for (int d = 0; d < doors.size(); d++) {
            if (doors.get(d).gate() == gate + 1 && openedAt[d] == NEVER) {
                openedAt[d] = tick;
            }
        }
    }

    /** After a restart at a later gate (the Halo's checkpoint): the gates before {@code gate} (from 0) stand open. */
    void openedBefore(int gate) {
        for (int d = 0; d < doors.size(); d++) {
            if (doors.get(d).gate() <= gate && openedAt[d] == NEVER) {
                openedAt[d] = LONG_AGO;
            }
        }
    }

    /** A door's frame {@code since} steps after its gate opened (negative: shut), its {@code count} frames over {@code ticks}. */
    static int frame(long since, int ticks, int count) {
        if (since < 0 || count <= 1) {
            return 0;
        }
        return (int) Math.min(count - 1, since * (count - 1) / Math.max(1, ticks));
    }

    /** Door {@code d}'s frame at step {@code tick}. */
    int frameAt(int d, long tick) {
        return openedAt[d] == NEVER ? 0 : frame(tick - openedAt[d], ticks[d], frames.get(d).size);
    }

    /** The doors at their rows on the ground layer scrolled by {@code scroll} px, at the last step seen. */
    void draw(SpriteBatch batch, long scroll) {
        long tick = now;
        for (int d = 0; d < doors.size(); d++) {
            float y = bottom[d] - scroll;
            if (y >= HEIGHT || y + height[d] <= 0) {
                continue;
            }
            float x = X0 + Math.round(doors.get(d).x() - width[d] / 2);
            batch.draw(frames.get(d).get(frameAt(d, tick)), x, y);
        }
    }
}
