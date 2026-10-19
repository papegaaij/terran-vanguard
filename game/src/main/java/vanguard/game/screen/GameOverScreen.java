package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.campaign.Campaign;
import vanguard.game.GameServices;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.Voices;
import vanguard.game.hangar.Names;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Words;

/**
 * The game over screen (design/systems/retry: hard, no retry left), built to the chosen
 * game-over-r08-a (M6 part B): the game over cue, the campaign's stats, Okafor's last transmission
 * (design/systems/retry/data.yaml, voiced through the radio filter once the cue has opened), and the
 * hard table of the high scores, with the name entry when the score makes the top 10 (Q2 = a).
 * Once the name is taken (or when there is none to enter), confirm (or a click) goes to the main
 * menu, where Continue returns to the level's briefing and hangar with its retries renewed (the autosave
 * the failure wrote) and Load game offers the other saves.
 */
public final class GameOverScreen implements GameScreen {
    private static final float CUE_VOLUME = 0.8f;
    /** The transmission starts this long after the cue. */
    static final float VOICE_DELAY = 2.5f;

    static final int TITLE_Y = 20;
    static final int SUBTITLE_Y = 76;
    static final int PANEL_Y = 104;
    static final int PANEL_HEIGHT = 372;
    static final int LEFT_X = 24;
    static final int LEFT_WIDTH = 300;
    static final int RIGHT_X = 336;
    static final int RIGHT_WIDTH = 600;
    static final int PADDING = 18;
    /** The table inside the right panel. */
    static final int TABLE_X = RIGHT_X + PADDING;

    static final int TABLE_Y = PANEL_Y + 14;
    static final int TABLE_WIDTH = RIGHT_WIDTH - 2 * PADDING;
    /** The transmission's characters per line in the left panel. */
    private static final int TEXT_CHARS = (LEFT_WIDTH - 2 * PADDING) / 8;

    private final GameServices services;
    private final Campaign campaign;
    private final String mission;
    private final ScoreEntry scores;
    private final LevelData.RadioLine transmission;
    private final Optional<Voices.Voice> voice;
    private boolean voiced;
    private float elapsed;

    /** @param mission the mission that ended the campaign, "MISSION 01 - BREAK AT DAWN" */
    GameOverScreen(GameServices services, Campaign campaign, String mission) {
        this.services = services;
        this.campaign = campaign;
        this.mission = mission;
        scores = new ScoreEntry(services, campaign.difficulty(), campaign.score(), campaign.nextLevel());
        transmission = services.content.retry().gameOver();
        voice = services.voices.radio(
                transmission.speaker(),
                transmission.line(),
                transmission.expression().map(Expression::slug).orElse(Expression.NEUTRAL.slug()));
        services.sfx.play(Sfx.GAME_OVER, CUE_VOLUME, 1, 0);
    }

    @Override
    public Transition update(float seconds) {
        elapsed += seconds;
        services.voices.update(seconds);
        if (!voiced && elapsed >= VOICE_DELAY) {
            voiced = true;
            voice.ifPresent(services.voices::play);
        }
        if (scores.entering()) {
            scores.update(TABLE_X, TABLE_Y, TABLE_WIDTH);
            return Transition.STAY;
        }
        if (services.menu.confirm() || services.menu.back() || services.menu.clicked()) {
            services.play(Sfx.MENU_CONFIRM);
            return Transition.replace(MainMenuScreen.menu(services));
        }
        return Transition.STAY;
    }

    /** The campaign's best grade and where it was flown, "A+ (M07)"; a dash before any level was won. */
    static String bestGrade(Campaign campaign) {
        List<String> order = campaign.gradeOrder();
        String best = "-";
        int bestRank = Integer.MAX_VALUE;
        for (int level = 1; level < campaign.nextLevel(); level++) {
            Optional<String> grade = campaign.grade(level);
            if (grade.isPresent() && order.indexOf(grade.get()) >= 0 && order.indexOf(grade.get()) < bestRank) {
                bestRank = order.indexOf(grade.get());
                best = String.format(Locale.ROOT, "%s (M%02d)", grade.get(), level);
            }
        }
        return best;
    }

    /**
     * The line under the title: "HARD - RETRIES 0 / 3 - MISSION 12 - STORM FRONT"; none were left
     * (the campaign has renewed them already for the hangar the autosave holds).
     */
    String subtitle() {
        String retries = campaign.retriesPerLevel()
                .map(per -> String.format(Locale.ROOT, " - RETRIES 0 / %d", per))
                .orElse("");
        return campaign.difficulty().name() + retries + " - " + mission;
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        services.titleScene.draw(batch, 0.25f);
        var heading = glass.fonts.heading;
        heading.getData().setScale(2);
        glass.centred(batch, heading, "GAME OVER", Glass.ALERT, PixelScreen.WIDTH / 2f, TITLE_Y);
        heading.getData().setScale(1);
        glass.centred(batch, glass.fonts.body, subtitle(), Glass.ALERT, PixelScreen.WIDTH / 2f, SUBTITLE_Y);
        drawCampaign(batch, glass);
        glass.panel(batch, RIGHT_X, PANEL_Y, RIGHT_WIDTH, PANEL_HEIGHT, 0.85f);
        scores.draw(batch, glass, TABLE_X, TABLE_Y, TABLE_WIDTH, elapsed);
        glass.hints(batch, scores.entering() ? ScoreEntry.hints() : "ENTER MAIN MENU");
    }

    private void drawCampaign(SpriteBatch batch, Glass glass) {
        glass.panel(batch, LEFT_X, PANEL_Y, LEFT_WIDTH, PANEL_HEIGHT, 0.85f);
        int x = LEFT_X + PADDING;
        int right = LEFT_X + LEFT_WIDTH - PADDING;
        glass.header(batch, "CAMPAIGN", x, right, PANEL_Y + 14);
        String[][] rows = {
            {"SCORE", Names.grouped(campaign.score())},
            {"FURTHEST", String.format(Locale.ROOT, "MISSION %02d", campaign.nextLevel())},
            {"PLAYTIME", SlotsScreen.playtime(campaign.playtime())},
            {"ENEMIES", Names.grouped(campaign.kills())},
            {"BEST GRADE", bestGrade(campaign)},
            {"SHIPS LOST", Integer.toString(campaign.deaths())}
        };
        int y = PANEL_Y + 36;
        for (int i = 0; i < rows.length; i++) {
            glass.shadowed(batch, glass.fonts.label, rows[i][0], Glass.LABEL, x, y + 4);
            glass.right(batch, glass.fonts.body, rows[i][1], i == 0 ? Glass.GREEN : Glass.WHITE, right, y);
            y += 24;
        }
        y += 6;
        glass.header(batch, "LAST TRANSMISSION", x, right, y);
        y += 20;
        for (String line : Words.wrap(transmission.line().toUpperCase(Locale.ROOT), TEXT_CHARS)) {
            glass.shadowed(batch, glass.fonts.label, line, Glass.BODY, x, y);
            y += 14;
        }
        glass.shadowed(
                batch,
                glass.fonts.label,
                "- CMDR " + transmission.speaker().toUpperCase(Locale.ROOT),
                Glass.AMBER,
                x,
                y + 2);
        int noteY = PANEL_Y + PANEL_HEIGHT - 52;
        for (String line :
                Words.wrap("NO RETRIES LEFT. CONTINUE RETURNS TO THE BRIEFING BEFORE THIS MISSION.", TEXT_CHARS)) {
            glass.shadowed(batch, glass.fonts.label, line, Glass.LABEL, x, noteY);
            noteY += 14;
        }
    }

    @Override
    public void dispose() {
        services.sfx.stop(Sfx.GAME_OVER);
        services.voices.stop();
    }
}
