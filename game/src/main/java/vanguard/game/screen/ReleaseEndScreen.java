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
 *
 * <p>M11 part A, step A3b (design/ui/debrief, The campaign end, Q12 a): after the campaign's last act's last level and
 * its ending pages ({@code CampaignRoute.campaignEnd}) the card takes its <b>campaign form</b> ({@link #campaignEnd}):
 * {@code CAMPAIGN COMPLETE}, {@code MEDIUM - ALL 50 MISSIONS FLOWN}, the totals with {@code SECRETS FOUND n OF m}
 * (m the secrets of the built levels; step PT5), {@code THE GATE IS DARK.} in amber, the replay line and {@code AUTOSAVED} (the
 * completed campaign: {@code nextLevel} 51); track 27 plays on from the pages under the card and the name entry, whose
 * entry is marked complete ({@code WON}); the credits roll then plays track 28 over the pages' last image.
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
    /** M11 part A: the campaign form's track 27, on from the ending pages; null in the release's form. */
    private final EndingMusic ending;
    /** M11 part A: the ending pages' last image (a path in the assets), behind the credits roll after the campaign. */
    private final Optional<String> lastImage;

    private final String autosave;
    private Phase phase = Phase.CARD;
    private float elapsed;

    public ReleaseEndScreen(GameServices services, Campaign campaign) {
        this(services, campaign, null, Optional.empty());
    }

    /**
     * M11 part A, step A3b: the end card's campaign form after the ending pages, {@code ending} (track 27) playing on
     * under it; {@code lastImage} the pages' last image behind the credits.
     */
    static ReleaseEndScreen campaignEnd(
            GameServices services, Campaign campaign, EndingMusic ending, Optional<String> lastImage) {
        return new ReleaseEndScreen(services, campaign, ending, lastImage);
    }

    private ReleaseEndScreen(GameServices services, Campaign campaign, EndingMusic ending, Optional<String> lastImage) {
        this.services = services;
        this.campaign = campaign;
        this.ending = ending;
        this.lastImage = lastImage;
        int last = campaign.nextLevel() - 1;
        scores = new ScoreEntry(services, campaign.difficulty(), campaign.score(), Math.max(1, last), ending != null);
        if (services.debugRun()) {
            autosave = "AUTOSAVE OFF - DEBUG RUN";
        } else {
            autosave = services.save(SaveSlots.Slot.AUTOSAVE, campaign) ? "AUTOSAVED" : "AUTOSAVE FAILED";
        }
        music = ending != null
                ? Optional.empty()
                : MusicStreamer.play(
                        services.audio, services.files.internal("music/title-theme.ogg"), MUSIC_VOLUME, services.mixer);
    }

    /** M11 part A: whether this is the campaign's end (its campaign form), not the release's. */
    boolean campaignForm() {
        return ending != null;
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
        if (ending != null) {
            ending.update(seconds);
        }
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
        return phase == Phase.CREDITS ? Transition.open(credits()) : Transition.STAY;
    }

    /**
     * The credits roll with its own track 28: in the campaign form track 27 fades out over the credits' fade as it
     * starts, over the pages' last image; in the release's the title theme goes quiet under it.
     */
    private CreditsScreen credits() {
        float fade = (float) services.content.credits().fade();
        if (ending != null) {
            ending.fadeOut(fade);
            return new CreditsScreen(services, share -> {}, fade, lastImage);
        }
        return new CreditsScreen(
                services,
                share -> music.ifPresent(streamer -> streamer.setVolume(MUSIC_VOLUME * share)),
                fade,
                Optional.empty());
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
        glass.centred(
                batch,
                heading,
                campaignForm() ? CampaignEnd.TITLE : ReleaseTexts.title(services.content),
                Glass.WHITE,
                PixelScreen.WIDTH / 2f,
                36);
        heading.getData().setScale(1);
        glass.centred(
                batch,
                glass.fonts.body,
                campaignForm()
                        ? CampaignEnd.flown(campaign.difficulty(), campaign.nextLevel() - 1)
                        : String.format(
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
            {
                "SECRETS FOUND",
                campaignForm()
                        ? CampaignEnd.found(campaign.secretsFound(), CampaignEnd.secrets(services.content))
                        : Integer.toString(campaign.secretsFound())
            },
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
        if (campaignForm()) {
            // M11 part A: the gate is dark; every mission open for replay.
            glass.centred(batch, glass.fonts.heading, CampaignEnd.DARK, Glass.AMBER, PixelScreen.WIDTH / 2f, 356);
            glass.centred(batch, glass.fonts.body, CampaignEnd.REPLAY, Glass.BODY, PixelScreen.WIDTH / 2f, 392);
        } else {
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
        }
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
        if (ending != null) {
            ending.close();
        }
    }
}
