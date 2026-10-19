package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import vanguard.content.Difficulty;
import vanguard.content.campaign.HighScores;
import vanguard.game.GameServices;
import vanguard.game.audio.Sfx;
import vanguard.game.ui.Glass;
import vanguard.game.ui.NameEntry;

/**
 * A campaign's score going into the high scores (design/systems/scoring, High scores; M6 part B,
 * Q2 = a: at a game over and at the release's end): the difficulty's table read from scores.yaml,
 * the name entry when the score makes the top 10, and the table written with the new entry (not in
 * a debug run, which says so). The game over screen and the release's end each hold one and place
 * its {@link ScoreTable}.
 */
final class ScoreEntry {
    static final String SAVED = "SCORE SAVED.";
    static final String DEBUG_RUN = "SCORES OFF - DEBUG RUN: NOT SAVED.";
    static final String FAILED = "THE SCORES COULD NOT BE SAVED.";
    static final String MISSED = "THIS SCORE DID NOT MAKE THE TOP 10.";

    private final GameServices services;
    private final Difficulty difficulty;
    private final long score;
    private final int level;
    private final LocalDate date = LocalDate.now();
    private HighScores scores;
    private final OptionalInt place;
    private final Optional<NameEntry> entry;
    private OptionalInt highlight = OptionalInt.empty();
    private String status;

    /** @param level the furthest level */
    ScoreEntry(GameServices services, Difficulty difficulty, long score, int level) {
        this.services = services;
        this.difficulty = difficulty;
        this.score = score;
        this.level = level;
        scores = services.scores.read();
        place = scores.rank(difficulty, score, date);
        entry = place.isPresent() ? Optional.of(new NameEntry(scores.lastName())) : Optional.empty();
        status = place.isPresent() ? "" : MISSED;
    }

    /** Whether the score makes the top 10: the name entry runs. */
    boolean qualifies() {
        return place.isPresent();
    }

    /** The place the score takes, 0 = first. */
    OptionalInt place() {
        return place;
    }

    /** Whether the name entry is still running: the screen waits for it. */
    boolean entering() {
        return entry.isPresent() && !entry.get().done();
    }

    /** The heading over the table: {@code HIGH SCORES - HARD}. */
    static String heading(Difficulty difficulty) {
        return "HIGH SCORES - " + difficulty.name();
    }

    /**
     * Reads a frame's input into the name entry, the table at (x, y) and {@code width}; when END takes
     * the name, the entry goes into the table and the table into scores.yaml.
     */
    void update(float x, float y, float width) {
        if (!entering()) {
            return;
        }
        NameEntry running = entry.get();
        switch (running.update(services.menu, index -> ScoreTable.key(x, y, width, index))) {
            case NONE -> {}
            case MOVED -> services.play(Sfx.MENU_MOVE);
            case EDITED -> services.play(Sfx.MENU_CONFIRM);
            case REFUSED -> services.play(Sfx.SHOP_DENIED);
            case DONE -> record(running.result());
        }
    }

    private void record(String name) {
        scores = scores.add(difficulty, new HighScores.Entry(name, score, level, date));
        highlight = place;
        if (services.debugRun()) {
            status = DEBUG_RUN;
            services.play(Sfx.MENU_CONFIRM);
        } else if (services.saveScores(scores)) {
            status = SAVED;
            services.play(Sfx.SAVE_DONE);
        } else {
            status = FAILED;
            services.play(Sfx.SHOP_DENIED);
        }
    }

    /** The table's rows as they show now. */
    List<ScoreTable.Row> rows() {
        List<HighScores.Entry> table = scores.table(difficulty);
        if (entering()) {
            return ScoreTable.rows(table, place, entry.get().name(), score, level);
        }
        return ScoreTable.rows(table, highlight);
    }

    /** The line under the table once the entry is over (or there is none). */
    String status() {
        return status;
    }

    void draw(SpriteBatch batch, Glass glass, float x, float y, float width, float elapsed) {
        ScoreTable.draw(batch, glass, heading(difficulty), rows(), entry, elapsed, x, y, width);
        if (!entering() && !status.isEmpty()) {
            glass.shadowed(
                    batch,
                    glass.fonts.label,
                    status,
                    status.equals(SAVED) ? Glass.GREEN : status.equals(MISSED) ? Glass.LABEL : Glass.AMBER,
                    x + 40,
                    y + ScoreTable.GRID_Y + 6);
        }
    }

    /** The key hints while the name is entered. */
    static String hints() {
        return "ARROWS PICK OR TYPE A LETTER    ENTER ADD    ESC DELETE    END CONFIRM";
    }
}
