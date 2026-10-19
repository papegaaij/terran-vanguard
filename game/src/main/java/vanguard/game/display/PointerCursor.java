package vanguard.game.display;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Optional;
import vanguard.game.render.PixelScreen;

/**
 * The mouse pointer's look (design/ui, Mouse): the UI kit's glass pointer ({@code ui/pointer.png},
 * tools/art/pointer.py) as a hardware cursor at the window's integer scale, so it grows with the
 * pixels; the system arrow where the art is missing or the platform refuses the cursor. While a
 * level is flown the pointer is hidden by a transparent cursor, not caught, so it can still leave a
 * window.
 */
public final class PointerCursor implements Disposable {
    /** The art's file and its hotspot, the arrow's tip. */
    static final String FILE = "ui/pointer.png";

    private static final int HOTSPOT_X = 0;
    private static final int HOTSPOT_Y = 0;
    /** The transparent cursor's size: GLFW wants a power of two. */
    private static final int HIDDEN_SIZE = 16;

    private enum Shown {
        NONE,
        GLASS,
        SYSTEM,
        HIDDEN
    }

    private final Optional<Pixmap> art;
    private Optional<Cursor> glass = Optional.empty();
    private int glassScale;
    private Optional<Cursor> hidden = Optional.empty();
    private boolean hiddenTried;
    private Shown shown = Shown.NONE;

    public PointerCursor(Files files) {
        art = load(files.internal(FILE));
    }

    private static Optional<Pixmap> load(FileHandle file) {
        if (!file.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Pixmap(file));
        } catch (GdxRuntimeException e) {
            Gdx.app.error("pointer", "cannot read " + FILE + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /** The window's integer scale in logical units, as large as the cursor is drawn. */
    static int scale(int windowWidth, int windowHeight) {
        return Math.max(1, Math.min(windowWidth / PixelScreen.WIDTH, windowHeight / PixelScreen.HEIGHT));
    }

    /** Shows the glass pointer, or hides the pointer while a level is flown; call every frame. */
    public void update(boolean hide) {
        if (hide) {
            show(Shown.HIDDEN, hiddenCursor());
            return;
        }
        int scale = scale(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (scale != glassScale) {
            glass.ifPresent(Cursor::dispose);
            glass = art.flatMap(pixmap -> cursor(pixmap, scale));
            glassScale = scale;
            shown = Shown.NONE;
        }
        show(glass.isPresent() ? Shown.GLASS : Shown.SYSTEM, glass);
    }

    private void show(Shown wanted, Optional<Cursor> cursor) {
        if (wanted == shown) {
            return;
        }
        shown = wanted;
        if (cursor.isPresent()) {
            Gdx.graphics.setCursor(cursor.get());
        } else {
            Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
        }
    }

    private Optional<Cursor> hiddenCursor() {
        if (!hiddenTried) {
            hiddenTried = true;
            Pixmap clear = new Pixmap(HIDDEN_SIZE, HIDDEN_SIZE, Pixmap.Format.RGBA8888);
            hidden = newCursor(clear, 0, 0);
            clear.dispose();
        }
        return hidden;
    }

    /** The art scaled up by whole pixels on a power-of-two canvas, as GLFW wants it. */
    private static Optional<Cursor> cursor(Pixmap pixmap, int scale) {
        int width = pixmap.getWidth() * scale;
        int height = pixmap.getHeight() * scale;
        int size = MathUtils.nextPowerOfTwo(Math.max(width, height));
        Pixmap scaled = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        scaled.setBlending(Pixmap.Blending.None);
        scaled.setFilter(Pixmap.Filter.NearestNeighbour);
        scaled.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
        Optional<Cursor> cursor = newCursor(scaled, HOTSPOT_X * scale, HOTSPOT_Y * scale);
        scaled.dispose();
        return cursor;
    }

    private static Optional<Cursor> newCursor(Pixmap pixmap, int hotspotX, int hotspotY) {
        try {
            return Optional.of(Gdx.graphics.newCursor(pixmap, hotspotX, hotspotY));
        } catch (GdxRuntimeException e) {
            Gdx.app.error("pointer", "no custom cursor, the system arrow instead: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void dispose() {
        glass.ifPresent(Cursor::dispose);
        hidden.ifPresent(Cursor::dispose);
        art.ifPresent(Pixmap::dispose);
    }
}
