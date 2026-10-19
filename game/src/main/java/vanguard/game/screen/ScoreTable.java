package vanguard.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import vanguard.content.campaign.HighScores;
import vanguard.game.hangar.Names;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.game.ui.NameEntry;

/**
 * A difficulty's top 10 as the game over screen, the release's end and the SCORES screen draw it
 * (chosen game-over-r08-a): rank, name, score and the furthest mission ({@code M12}) in rows of
 * the body font, the new entry's row on the amber selection bar with its name being typed, and
 * under the table the name entry's letter grid. Every position is relative to the table's top-left
 * corner and its width, so the screens place it where they need it.
 */
final class ScoreTable {
    /** The rows' first line below the table's top (the header above it). */
    static final int ROWS_Y = 24;

    static final int ROW_STEP = 22;
    /** The table's height: its header and ten rows. */
    static final int HEIGHT = ROWS_Y + HighScores.SIZE * ROW_STEP;
    /** The grid's top below the table's top. */
    static final int GRID_Y = HEIGHT + 8;

    static final int KEY_WIDTH = 25;
    static final int KEY_HEIGHT = 18;
    static final int KEY_GAP = 2;
    static final int GRID_WIDTH = NameEntry.COLUMNS * (KEY_WIDTH + KEY_GAP) - KEY_GAP;
    /** The table with the grid below it. */
    static final int HEIGHT_WITH_GRID = GRID_Y + 2 * KEY_HEIGHT + KEY_GAP;

    private static final float CURSOR_BLINK_SECONDS = 0.5f;

    /** A row as drawn: an entry, or the new one with its name as typed. */
    record Row(String name, long score, int level, boolean entering) {}

    private ScoreTable() {}

    /**
     * The rows: the table, with the new entry's row at {@code place} while its name is entered (or
     * after, to highlight it), the last entry dropping out of a full table.
     */
    static List<Row> rows(List<HighScores.Entry> table, OptionalInt place, String name, long score, int level) {
        List<Row> rows = new ArrayList<>();
        for (HighScores.Entry entry : table) {
            rows.add(new Row(entry.name(), entry.score(), entry.level(), false));
        }
        if (place.isPresent()) {
            rows.add(place.getAsInt(), new Row(name, score, level, true));
        }
        return rows.subList(0, Math.min(HighScores.SIZE, rows.size()));
    }

    /** The rows of a table as stored, the entry at {@code highlight} (just entered) marked. */
    static List<Row> rows(List<HighScores.Entry> table, OptionalInt highlight) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            HighScores.Entry entry = table.get(i);
            rows.add(new Row(entry.name(), entry.score(), entry.level(), highlight.equals(OptionalInt.of(i))));
        }
        return rows;
    }

    /** The furthest level as the table shows it: {@code M12}. */
    static String level(int level) {
        return String.format(Locale.ROOT, "M%02d", level);
    }

    /** The grid's left edge: centred in the table's width. */
    static float gridX(float x, float width) {
        return Math.round(x + (width - GRID_WIDTH) / 2);
    }

    /** A key's hit region, where {@link #draw} draws it. */
    static Box key(float x, float y, float width, int index) {
        int column = index % NameEntry.COLUMNS;
        int row = index / NameEntry.COLUMNS;
        return new Box(
                gridX(x, width) + column * (KEY_WIDTH + KEY_GAP),
                y + GRID_Y + row * (KEY_HEIGHT + KEY_GAP),
                KEY_WIDTH,
                KEY_HEIGHT);
    }

    /**
     * Draws the table: its amber header, the rows (the entering one on the selection bar) and, while
     * a name is entered, the grid.
     *
     * @param entry the name entry while it runs
     * @param elapsed seconds for the blinking cursor
     */
    static void draw(
            SpriteBatch batch,
            Glass glass,
            String heading,
            List<Row> rows,
            Optional<NameEntry> entry,
            float elapsed,
            float x,
            float y,
            float width) {
        glass.header(batch, heading, x, x + width, y);
        float right = x + width;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int textY = (int) y + ROWS_Y + i * ROW_STEP;
            Color colour = row.entering() ? Glass.AMBER : Glass.WHITE;
            if (row.entering()) {
                glass.selection(batch, x - 6, textY - 6, width + 6, ROW_STEP);
            }
            glass.right(batch, glass.fonts.body, Integer.toString(i + 1), Glass.LABEL, x + 24, textY);
            String name = row.name();
            boolean typing = row.entering() && entry.isPresent() && !entry.get().done();
            glass.shadowed(batch, glass.fonts.body, name, colour, x + 40, textY);
            if (typing && elapsed % (2 * CURSOR_BLINK_SECONDS) < CURSOR_BLINK_SECONDS) {
                glass.shadowed(
                        batch, glass.fonts.body, "_", colour, x + 40 + Fonts.width(glass.fonts.body, name), textY);
            }
            glass.right(
                    batch,
                    glass.fonts.body,
                    Names.grouped(row.score()),
                    row.entering() ? Glass.AMBER : Glass.BODY,
                    right - 60,
                    textY);
            glass.right(batch, glass.fonts.body, level(row.level()), Glass.CYAN, right - 6, textY);
        }
        if (rows.isEmpty()) {
            glass.shadowed(batch, glass.fonts.label, "NO SCORES YET.", Glass.LABEL, x + 40, y + ROWS_Y + 4);
        }
        entry.filter(running -> !running.done()).ifPresent(running -> drawGrid(batch, glass, running, x, y, width));
    }

    private static void drawGrid(SpriteBatch batch, Glass glass, NameEntry entry, float x, float y, float width) {
        for (int i = 0; i < NameEntry.KEYS.size(); i++) {
            Box key = key(x, y, width, i);
            String label = NameEntry.KEYS.get(i);
            boolean enabled = !label.equals(NameEntry.END) || entry.valid();
            glass.chip(
                    batch,
                    glass.fonts.label,
                    label,
                    key.x(),
                    key.y(),
                    key.width(),
                    key.height(),
                    i == entry.selected(),
                    enabled);
        }
    }
}
