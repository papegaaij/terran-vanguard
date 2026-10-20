package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.SaveSlots;
import vanguard.game.GameServices;
import vanguard.game.audio.MusicStreamer;
import vanguard.game.audio.Sfx;
import vanguard.game.hangar.Names;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Glass;

/**
 * The release's end (M6 part B, Q1 = a, pending the user's review in round 35): after the last
 * built act's outro ({@code CampaignRoute.releaseEnd}) comes an end card ("ACTS I-III COMPLETE",
 * the campaign's totals, "TO BE CONTINUED" with the next act's name; M7 part H: all from the acts' data, {@link
 * ReleaseTexts}), then the name entry when the
 * score makes the difficulty's top 10, then the credits roll over it, then the main menu. The title
 * theme plays from the card through the credits. It writes the autosave as the hangar would (never
 * in a debug run), so Continue reaches the hangar before the next level, which says the act is
 * coming soon.
 */
public final class ReleaseEndScreen implements GameScreen {
    private static final float MUSIC_VOLUME = 0.6f;
    static final int PANEL_X = 260;
    static final int PANEL_Y = 136;
    static final int PANEL_WIDTH = 440;
    static final int PANEL_HEIGHT = 186;
    /** The name entry's panel. */
    static final int ENTRY_X = 180;

    static final int ENTRY_Y = 104;
    static final int ENTRY_WIDTH = 600;
    static final int ENTRY_HEIGHT = 340;
    static final int TABLE_X = ENTRY_X + 18;
    static final int TABLE_Y = ENTRY_Y + 14;
    static final int TABLE_WIDTH = ENTRY_WIDTH - 36;

    /** The steps of the sequence. */
    enum Phase {
        CARD,
        ENTRY,
        CREDITS
    }

    private final GameServices services;
    private final Campaign campaign;
    private final ScoreEntry scores;
    private final Optional<MusicStreamer> music;
    private final String autosave;
    private Phase phase = Phase.CARD;
    private float elapsed;

    public ReleaseEndScreen(GameServices services, Campaign campaign) {
        this.services = services;
        this.campaign = campaign;
        int last = campaign.nextLevel() - 1;
        scores = new ScoreEntry(services, campaign.difficulty(), campaign.score(), Math.max(1, last));
        if (services.debugRun()) {
            autosave = "AUTOSAVE OFF - DEBUG RUN";
        } else {
            autosave = services.save(SaveSlots.Slot.AUTOSAVE, campaign) ? "AUTOSAVED" : "AUTOSAVE FAILED";
        }
        music = MusicStreamer.play(
                services.audio, services.files.internal("music/title-theme.ogg"), MUSIC_VOLUME, services.mixer);
    }

    /** The step after {@code phase}: the name entry only for a top-10 score, then the credits. */
    static Phase next(Phase phase, boolean qualifies) {
        return switch (phase) {
            case CARD -> qualifies ? Phase.ENTRY : Phase.CREDITS;
            case ENTRY -> Phase.CREDITS;
            case CREDITS -> throw new IllegalStateException("the credits lead to the main menu");
        };
    }

    @Override
    public Transition update(float seconds) {
        elapsed += seconds;
        if (phase == Phase.CREDITS) {
            // The credits went back to this screen: the release has ended.
            return Transition.replace(MainMenuScreen.menu(services));
        }
        if (phase == Phase.ENTRY && scores.entering()) {
            scores.update(TABLE_X, TABLE_Y, TABLE_WIDTH);
            return Transition.STAY;
        }
        if (!(services.menu.confirm() || services.menu.back() || services.menu.clicked())) {
            return Transition.STAY;
        }
        services.play(Sfx.MENU_CONFIRM);
        phase = next(phase, scores.qualifies());
        elapsed = 0;
        return phase == Phase.CREDITS ? Transition.open(new CreditsScreen(services)) : Transition.STAY;
    }

    Phase phase() {
        return phase;
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        services.titleScene.draw(batch, 0.35f);
        if (phase == Phase.ENTRY) {
            drawEntry(batch, glass);
        } else {
            drawCard(batch, glass);
        }
    }

    private void drawCard(SpriteBatch batch, Glass glass) {
        var heading = glass.fonts.heading;
        heading.getData().setScale(2);
        glass.centred(batch, heading, ReleaseTexts.title(services.content), Glass.WHITE, PixelScreen.WIDTH / 2f, 36);
        heading.getData().setScale(1);
        glass.centred(
                batch,
                glass.fonts.body,
                String.format(
                        Locale.ROOT,
                        "%s - MISSIONS 01 - %02d FLOWN",
                        campaign.difficulty().name(),
                        campaign.nextLevel() - 1),
                Glass.CYAN,
                PixelScreen.WIDTH / 2f,
                98);
        glass.panel(batch, PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 0.85f);
        int x = PANEL_X + 18;
        int right = PANEL_X + PANEL_WIDTH - 18;
        glass.header(batch, "CAMPAIGN", x, right, PANEL_Y + 14);
        String[][] rows = {
            {"SCORE", Names.grouped(campaign.score())},
            {"ENEMIES DESTROYED", Names.grouped(campaign.kills())},
            {"DATA CORES FOUND", Integer.toString(campaign.dataCores().size())},
            {"A+ GRADES", String.format(Locale.ROOT, "%d OF %d", topGrades(campaign), campaign.nextLevel() - 1)},
            {"SHIPS LOST", Integer.toString(campaign.deaths())},
            {"PLAYTIME", SlotsScreen.playtime(campaign.playtime())}
        };
        int y = PANEL_Y + 36;
        for (int i = 0; i < rows.length; i++) {
            glass.shadowed(batch, glass.fonts.label, rows[i][0], Glass.LABEL, x, y + 4);
            glass.right(batch, glass.fonts.body, rows[i][1], i == 0 ? Glass.GREEN : Glass.WHITE, right, y);
            y += 24;
        }
        glass.centred(batch, glass.fonts.heading, "TO BE CONTINUED", Glass.AMBER, PixelScreen.WIDTH / 2f, 356);
        glass.centred(
                batch,
                glass.fonts.body,
                ReleaseTexts.comingSoon(services.content),
                Glass.BODY,
                PixelScreen.WIDTH / 2f,
                392);
        glass.centred(
                batch,
                glass.fonts.label,
                ReleaseTexts.continueLine(services.content),
                Glass.LABEL,
                PixelScreen.WIDTH / 2f,
                424);
        glass.right(
                batch,
                glass.fonts.label,
                autosave,
                autosave.equals("AUTOSAVED") ? Glass.GREEN : Glass.AMBER,
                PixelScreen.WIDTH - 16,
                PixelScreen.HEIGHT - 36);
        glass.hints(batch, scores.qualifies() ? "ENTER HIGH SCORES" : "ENTER CREDITS");
    }

    private void drawEntry(SpriteBatch batch, Glass glass) {
        glass.centred(batch, glass.fonts.heading, "NEW HIGH SCORE", Glass.WHITE, PixelScreen.WIDTH / 2f, 32);
        glass.centred(
                batch,
                glass.fonts.body,
                String.format(
                        Locale.ROOT,
                        "%s - SCORE %s - RANK %d",
                        campaign.difficulty().name(),
                        Names.grouped(campaign.score()),
                        scores.place().orElse(0) + 1),
                Glass.AMBER,
                PixelScreen.WIDTH / 2f,
                70);
        glass.panel(batch, ENTRY_X, ENTRY_Y, ENTRY_WIDTH, ENTRY_HEIGHT, 0.85f);
        scores.draw(batch, glass, TABLE_X, TABLE_Y, TABLE_WIDTH, elapsed);
        glass.hints(batch, scores.entering() ? ScoreEntry.hints() : "ENTER CREDITS");
    }

    /** How many of the won levels have an A+ as their best grade. */
    static int topGrades(Campaign campaign) {
        String top = campaign.gradeOrder().getFirst();
        int count = 0;
        for (int level = 1; level < campaign.nextLevel(); level++) {
            if (campaign.grade(level).equals(Optional.of(top))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void dispose() {
        music.ifPresent(MusicStreamer::close);
    }
}
