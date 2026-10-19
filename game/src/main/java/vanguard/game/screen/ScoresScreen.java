package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Optional;
import java.util.OptionalInt;
import vanguard.content.Difficulty;
import vanguard.content.campaign.HighScores;
import vanguard.content.campaign.SaveGame;
import vanguard.game.GameServices;
import vanguard.game.audio.Sfx;
import vanguard.game.input.MenuInput;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.game.ui.Glass;

/**
 * The high scores from the main menu (M6 part B, Q3 = a, pending the user's review in round 35):
 * the top 10 of each difficulty on three tabs, switched with Q / E (the bumpers) or left / right as
 * in the options, or a click on a tab; it opens on the current campaign's difficulty (the most
 * recent save), medium without one. Back, confirm or a click beside the tabs returns to the menu.
 */
public final class ScoresScreen implements GameScreen {
    static final int TAB_Y = 84;
    static final int TAB_WIDTH = 120;
    static final int TAB_HEIGHT = 24;
    static final int TAB_GAP = 8;
    static final int PANEL_X = 180;
    static final int PANEL_Y = 120;
    static final int PANEL_WIDTH = 600;
    static final int PANEL_HEIGHT = ScoreTable.HEIGHT + 30;
    static final int TABLE_X = PANEL_X + 18;
    static final int TABLE_Y = PANEL_Y + 14;
    static final int TABLE_WIDTH = PANEL_WIDTH - 36;

    private final GameServices services;
    private final HighScores scores;
    private int tab;

    ScoresScreen(GameServices services) {
        this.services = services;
        scores = services.scores.read();
        tab = services.saves
                .mostRecent()
                .map(SaveGame::difficulty)
                .orElse(Difficulty.MEDIUM)
                .ordinal();
    }

    /** A difficulty's tab, centred under the title. */
    static Box tab(int index) {
        int count = Difficulty.values().length;
        float width = count * TAB_WIDTH + (count - 1) * TAB_GAP;
        return new Box(
                Math.round((PixelScreen.WIDTH - width) / 2) + index * (TAB_WIDTH + TAB_GAP),
                TAB_Y,
                TAB_WIDTH,
                TAB_HEIGHT);
    }

    @Override
    public Transition update(float seconds) {
        MenuInput input = services.menu;
        int count = Difficulty.values().length;
        int before = tab;
        if (input.previousTab() || input.left()) {
            tab = Math.floorMod(tab - 1, count);
        } else if (input.nextTab() || input.right()) {
            tab = Math.floorMod(tab + 1, count);
        } else if (input.clicked()) {
            for (int i = 0; i < count; i++) {
                if (input.pointer().clicks(tab(i))) {
                    tab = i;
                }
            }
            if (tab == before && !input.pointer().clicks(tab(tab))) {
                services.play(Sfx.MENU_BACK);
                return Transition.BACK;
            }
        }
        if (tab != before) {
            services.play(Sfx.MENU_MOVE);
            return Transition.STAY;
        }
        if (input.back() || input.confirm()) {
            services.play(Sfx.MENU_BACK);
            return Transition.BACK;
        }
        return Transition.STAY;
    }

    Difficulty difficulty() {
        return Difficulty.values()[tab];
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        services.titleScene.draw(batch, 0.35f);
        glass.centred(batch, glass.fonts.heading, "HIGH SCORES", Glass.WHITE, PixelScreen.WIDTH / 2f, 36);
        for (int i = 0; i < Difficulty.values().length; i++) {
            Box box = tab(i);
            glass.tab(
                    batch,
                    glass.fonts.body,
                    Difficulty.values()[i].name(),
                    box.x(),
                    box.y(),
                    box.width(),
                    box.height(),
                    i == tab);
        }
        glass.panel(batch, PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 0.85f);
        ScoreTable.draw(
                batch,
                glass,
                ScoreEntry.heading(difficulty()),
                ScoreTable.rows(scores.table(difficulty()), OptionalInt.empty()),
                Optional.empty(),
                0,
                TABLE_X,
                TABLE_Y,
                TABLE_WIDTH);
        glass.centred(
                batch,
                glass.fonts.label,
                "A SCORE IS ENTERED AT A GAME OVER (HARD) AND AT THE END OF THE CAMPAIGN.",
                Glass.LABEL,
                PixelScreen.WIDTH / 2f,
                PANEL_Y + PANEL_HEIGHT + 14);
        glass.hints(batch, "Q/E DIFFICULTY    ESC BACK");
    }

    @Override
    public void dispose() {}
}
