package vanguard.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import vanguard.content.ActData;
import vanguard.content.BriefingPage;
import vanguard.content.Channel;
import vanguard.content.Content;
import vanguard.content.campaign.BriefingScript;
import vanguard.content.campaign.Briefings;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.OutroScript;
import vanguard.content.campaign.SaveSlots;
import vanguard.game.GameServices;
import vanguard.game.audio.Fanfare;
import vanguard.game.audio.LevelMusic;
import vanguard.game.audio.MusicStreamer;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.Tracks;
import vanguard.game.audio.Voices;
import vanguard.game.briefing.BriefingExit;
import vanguard.game.briefing.BriefingPager;
import vanguard.game.input.MenuInput;
import vanguard.game.render.PixelScreen;
import vanguard.game.render.TransmissionStatic;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Speaker;
import vanguard.game.ui.Words;

/**
 * The briefing before a level (design/ui/briefing, chosen briefing-r08-a) over the dimmed hero
 * scene with the briefing theme: the act title card first when the level opens its act
 * (act-title-r08-a, held 3.5 s), then the pages, each with its speaker's portrait and name plate,
 * typed out at twice the Gameplay tab's text speed (the radio's) with a soft blip; the mission's objectives and the
 * hangar teaser stay below. A page may show a tactical map or mission image (tools/art/briefing_images.py)
 * above its text; a page whose text does not fit below its image goes on over the next screens with
 * the same image, each counted as a page. The portrait shows the page's expression. Confirm shows
 * the whole page, then the next, as a click does; Back (Esc / B, the mouse's right button) asks to
 * quit to the main menu ({@link BriefingExit}), on the title card too, keeping the campaign as it is. The portrait opens through a burst of
 * transmission static, again when the speaker changes, and closes through one after the last page;
 * then the campaign goes on with {@code next} (the hangar).
 *
 * <p>The act outro after an act's last debrief (design/campaign, Act intro and outro) uses the same
 * screen: its pages with their images, the act's track (track 24, the act-complete fanfare) once
 * under the first page instead of the briefing theme, which follows once the fanfare has played out
 * or the first page is left; the bottom panel says the act is complete and where the campaign goes
 * on. A page image that is not rendered yet is left out, and the page's text takes its place.
 *
 * <p>Not built: the threat summary of the concept (it is the hangar intel panel's, part B2).
 */
public final class BriefingScreen implements GameScreen {
    static final float TITLE_CARD_SECONDS = 3.5f;

    private static final float MUSIC_VOLUME = 0.6f;
    private static final float FANFARE_VOLUME = 0.8f;
    /** The outro's fanfare fades out over this when the first page is left, and the theme fades in. */
    private static final float FADE_SECONDS = 1.5f;

    private static final float TYPING_VOLUME = 0.15f;
    private static final float CURSOR_BLINK_SECONDS = 0.5f;

    private static final int HEADER_Y = 12;
    private static final int COLUMN_Y = 52;
    private static final int COLUMN_HEIGHT = 380;
    private static final int LEFT_X = 12;
    private static final int LEFT_WIDTH = 220;
    private static final int TEXT_PANEL_X = 240;
    private static final int TEXT_PANEL_WIDTH = 708;
    private static final int TEXT_X = 258;
    private static final int TEXT_Y = 74;
    private static final int LINE = 24;
    /** The page image's top left and size (design/ui/briefing: 672x240 above the text). */
    private static final int IMAGE_Y = 64;

    static final int IMAGE_WIDTH = 672;
    static final int IMAGE_HEIGHT = 240;
    /** The text's top below an image. */
    private static final int IMAGE_TEXT_Y = IMAGE_Y + IMAGE_HEIGHT + 12;

    private static final int BOTTOM_Y = 440;
    private static final int BOTTOM_HEIGHT = 90;
    private static final int TEASER_X = 492;
    /** Letterbox bars of the title card. */
    private static final int BAR = 58;
    /**
     * The act's still behind its title card (tools/art/act_stills.py, {@code ui/<act>-still.png}) is
     * drawn darkened to this, so the chrome lettering reads over it; the title scene stands in, at
     * its own darkening, for an act without a still.
     */
    private static final float STILL_BRIGHTNESS = 0.55f;

    private static final float SCENE_BRIGHTNESS = 0.45f;

    private final GameServices services;
    private final Campaign campaign;
    private final Script script;
    private final Supplier<GameScreen> next;
    /** The briefing theme; in an outro it starts after the fanfare. */
    private Optional<MusicStreamer> music;
    /** The outro's fanfare under its first page. */
    private Optional<Fanfare> fanfare;
    /** The fanfare's fade-out and the theme's fade-in, 1 for none. */
    private float fade = 1;

    private final Optional<Texture> titleLettering;
    private final Optional<Texture> titleStill;
    private final List<Screen> screens = new ArrayList<>();
    private final Map<String, Optional<Texture>> images = new HashMap<>();
    private final BriefingPager pager;
    private final BriefingExit exit;
    /** Each briefing page's voice, by its index in the script. */
    private final List<Optional<Voices.Voice>> pageVoices = new ArrayList<>();
    /** The page whose voice was started last. */
    private int voicedPage = -1;
    /** The music's duck under the voice, 1 for none. */
    private float duck = 1;

    private float titleCard;
    private float elapsed;
    private int typed;
    /** Seconds since the speaker's portrait opened. */
    private float sinceOpened;
    /** Seconds until the portrait has closed after the last page; infinite before that. */
    private float untilClosed = Float.POSITIVE_INFINITY;

    /**
     * What the screen shows, a mission briefing's or an act outro's.
     *
     * @param header the act line of the header, on the left
     * @param mission the header's right side, {@code MISSION 07: BROOD CARRIER}
     * @param objectivesHeading the bottom panel's left heading
     * @param objectives its lines, the first one in amber
     * @param teaserHeading the bottom panel's right heading
     * @param teaser its lines
     * @param done the hint once the last page is shown
     * @param fanfare the cue under the first page instead of the briefing theme
     */
    private record Script(
            Optional<ActData.TitleCard> titleCard,
            String actDirectory,
            String header,
            String mission,
            List<BriefingPage> pages,
            String objectivesHeading,
            List<String> objectives,
            String teaserHeading,
            List<String> teaser,
            String done,
            Optional<String> fanfare) {}

    /** @param next the screen after the last page */
    public BriefingScreen(GameServices services, Campaign campaign, BriefingScript script, Supplier<GameScreen> next) {
        this(services, campaign, mission(services, script), next);
    }

    /** The act outro (design/campaign, Act intro and outro); {@code next} after its last page. */
    public static BriefingScreen outro(
            GameServices services, Campaign campaign, OutroScript outro, Supplier<GameScreen> next) {
        Onward onward = onward(services.content, outro);
        return new BriefingScreen(
                services,
                campaign,
                new Script(
                        Optional.empty(),
                        outro.actDirectory(),
                        outro.act(),
                        String.format(Locale.ROOT, "AFTER MISSION %02d", outro.last()),
                        outro.pages(),
                        "ACT COMPLETE",
                        List.of(
                                displayed(outro.act()),
                                String.format(Locale.ROOT, "MISSIONS %02d - %02d FLOWN", outro.first(), outro.last())),
                        "NEXT",
                        Words.wrap(displayed(onward.next()), (PixelScreen.WIDTH - LEFT_X - 14 - TEASER_X) / 8),
                        onward.hint(),
                        outro.music()),
                next);
    }

    /**
     * What follows an act outro, for its NEXT panel and its last page's hint.
     *
     * @param next the NEXT panel's text
     * @param hint the hint once the last page is shown
     */
    record Onward(String next, String hint) {}

    /**
     * What follows the outro on the campaign's route ({@code CampaignRoute.beforeNextLevel}): the
     * next act's title card and briefing when the next level opens an act with data (Act 2 after
     * Act 1), the next level's briefing when it has one, otherwise the hangar (Level 08's capture,
     * round 30: the hint said the hangar while the Act 2 title card came first); after the last built
     * act, the release's end (M6 part B).
     */
    static Onward onward(Content content, OutroScript outro) {
        if (content.levelKey(outro.nextLevel()).isEmpty()) {
            // M6 part B (Q1 = a): the release's end, ReleaseEndScreen.
            return new Onward(ReleaseTexts.outroNext(content), "ENTER TO THE END");
        }
        Optional<BriefingScript> briefing = Briefings.before(content, outro.nextLevel());
        if (briefing.isEmpty()) {
            String hangar = content.levelKey(outro.nextLevel())
                    .map(key -> String.format(
                            Locale.ROOT,
                            "THE HANGAR BEFORE MISSION %02d: %s",
                            outro.nextLevel(),
                            Content.levelName(key)))
                    .orElse(String.format(Locale.ROOT, "THE HANGAR BEFORE MISSION %02d", outro.nextLevel()));
            return new Onward(hangar, "ENTER TO THE HANGAR");
        }
        BriefingScript script = briefing.get();
        String mission =
                String.format(Locale.ROOT, "THE BRIEFING FOR MISSION %02d: %s", script.mission(), script.missionName());
        return script.titleCard()
                .map(card -> new Onward(
                        card.act() + ": " + card.name() + ", THEN " + mission, displayed("ENTER TO " + card.act())))
                .orElseGet(() -> new Onward(mission, "ENTER TO THE BRIEFING"));
    }

    private static Script mission(GameServices services, BriefingScript script) {
        BriefingPage teaser = script.teaser();
        return new Script(
                script.titleCard(),
                script.actDirectory(),
                script.act(),
                String.format(Locale.ROOT, "MISSION %02d: %s", script.mission(), script.missionName()),
                script.pages(),
                "OBJECTIVES",
                script.objectives(),
                "IN THE HANGAR",
                Words.wrap(
                        displayed(Speaker.of(teaser.speaker(), teaser.portrait(), services.sprites)
                                        .name() + ": \"" + teaser.line() + "\""),
                        (PixelScreen.WIDTH - LEFT_X - 14 - TEASER_X) / 8),
                "ENTER TO THE HANGAR",
                Optional.empty());
    }

    private BriefingScreen(GameServices services, Campaign campaign, Script script, Supplier<GameScreen> next) {
        this.services = services;
        this.campaign = campaign;
        this.script = script;
        this.next = next;
        motifPath = interceptMotif(services.content, script.actDirectory());
        List<Integer> lengths = new ArrayList<>();
        for (BriefingPage page : script.pages()) {
            pageVoices.add(services.voices.briefing(page));
            Speaker speaker = Speaker.of(page.speaker(), page.portrait(), services.sprites);
            Optional<Texture> image = page.image().flatMap(name -> images.computeIfAbsent(name, this::image));
            boolean intercepted = page.onChannel() == Channel.ENEMY;
            for (List<String> lines : screens(page, image.isPresent())) {
                screens.add(new Screen(page.speaker(), speaker, image, lines, pageVoices.size() - 1, intercepted));
                lengths.add(lines.stream().mapToInt(String::length).sum());
            }
        }
        pager = new BriefingPager(lengths);
        exit = services.debugRun() ? BriefingExit.debugRun() : new BriefingExit(BriefingExit.autosaves(campaign));
        titleCard = script.titleCard().isPresent() ? TITLE_CARD_SECONDS : 0;
        titleLettering = script.titleCard().map(card -> {
            var texture = new Texture(services.files.internal("ui/" + script.actDirectory() + "-title.png"));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            return texture;
        });
        titleStill = script.titleCard().flatMap(card -> {
            var file = services.files.internal("ui/" + script.actDirectory() + "-still.png");
            return file.exists() ? Optional.of(new Texture(file)) : Optional.empty();
        });
        fanfare = script.fanfare()
                .flatMap(name -> Fanfare.play(
                        services.audio, services.files.internal(Tracks.path(name)), FANFARE_VOLUME, services.mixer));
        music = fanfare.isPresent() ? Optional.empty() : theme();
    }

    private Optional<MusicStreamer> theme() {
        return MusicStreamer.play(
                services.audio, services.files.internal("music/briefing-theme.ogg"), MUSIC_VOLUME, services.mixer);
    }

    /**
     * M9 part B (design/campaign Act 5, Act intro: pages 2-3 "play track 12's motif stem alone"): the motif stem of the
     * act's first level's theme ({@code music/hollow-rock-motif.ogg}), played under an intercepted page instead of the
     * briefing theme; empty when the act's first level has no theme with a motif stem.
     */
    static Optional<String> interceptMotif(vanguard.content.Content content, String actDirectory) {
        return Optional.ofNullable(content.acts().get(actDirectory))
                .flatMap(act -> content.levelKey(act.levels().first()))
                .map(key -> content.level(key).music().track())
                .flatMap(Tracks::name)
                .map(Tracks::motifPath);
    }

    /** M9 part B: the intercept's motif stem ({@link #interceptMotif}). */
    private final Optional<String> motifPath;

    /** M9 part B: how far the music has crossed from the briefing theme to the intercept's motif (0 to 1). */
    private float motifFade;

    /** M9 part B: the intercept's motif stream, opened at the first intercepted page; empty before or without one. */
    private Optional<MusicStreamer> motifMusic = Optional.empty();

    /**
     * M9 part B: the music crosses to the intercept's motif stem alone while an intercepted page shows and back to the
     * briefing theme after it, over {@value #FADE_SECONDS} s.
     */
    private void followIntercept(float seconds, boolean intercepted) {
        if (intercepted && motifMusic.isEmpty() && motifPath.isPresent()) {
            var file = services.files.internal(motifPath.get());
            if (file.exists()) {
                motifMusic = MusicStreamer.play(services.audio, file, 0, services.mixer);
            }
        }
        float target = intercepted && motifMusic.isPresent() ? 1 : 0;
        float step = seconds / FADE_SECONDS;
        motifFade = target > motifFade ? Math.min(target, motifFade + step) : Math.max(target, motifFade - step);
        motifMusic.ifPresent(streamer -> streamer.setVolume(MUSIC_VOLUME * duck * motifFade));
    }

    /**
     * A screen of a briefing page: its speaker (the short name and the name plate with the page's
     * portrait), its image and the lines it shows.
     */
    private record Screen(
            String speakerName,
            Speaker speaker,
            Optional<Texture> image,
            List<String> lines,
            int dataPage,
            boolean intercepted) {}

    /**
     * M9 part A (design/ui/briefing, Intercepted transmissions): an intercepted page's header in place of the act and
     * mission line.
     */
    static final String INTERCEPTED = "INTERCEPTED \u00b7 ALL CHANNELS";
    /** M9 part A: the enemy channel's amber, its name plate's and its text's (the radio's, MissionPanel). */
    static final Color ENEMY_CHANNEL = Color.valueOf("FFA035");
    /** M9 part A: the carrier hiss under an intercepted page's typing, in place of the blip. */
    static final float CARRIER_VOLUME = 1f;

    /** The scratch region of the page's image under the steady interference (its tear). */
    private final TextureRegion imageRegion = new TextureRegion();
    /** Whether the shown screen is an intercepted page, at the last update (its open and close sounds). */
    private boolean interceptShown;

    /** Upper case, as the bitmap fonts write it. */
    static String displayed(String text) {
        return text.toUpperCase(Locale.ROOT);
    }

    /** A page's text in quotes, word-wrapped to the text panel's width in the body font (10 px a character). */
    static List<String> lines(BriefingPage page) {
        return Words.wrap(displayed("\"" + page.line() + "\""), (TEXT_PANEL_X + TEXT_PANEL_WIDTH - 18 - TEXT_X) / 10);
    }

    /** A page's lines split into the screens that show them: all of them, or a few below the page's image. */
    static List<List<String>> screens(BriefingPage page) {
        return screens(page, page.image().isPresent());
    }

    /** As {@link #screens(BriefingPage)}, with or without the page's image (one not rendered yet is left out). */
    static List<List<String>> screens(BriefingPage page, boolean image) {
        List<String> lines = lines(page);
        int perScreen = maxLines(image);
        List<List<String>> screens = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += perScreen) {
            screens.add(lines.subList(i, Math.min(lines.size(), i + perScreen)));
        }
        return screens;
    }

    /** How many lines the text panel holds, below an image or without one. */
    static int maxLines(boolean image) {
        return (COLUMN_Y + COLUMN_HEIGHT - 20 - textTop(image)) / LINE;
    }

    private static int textTop(boolean image) {
        return image ? IMAGE_TEXT_Y : TEXT_Y;
    }

    /** A page image, if it is rendered yet (the act outro's come later). */
    private Optional<Texture> image(String name) {
        var file = services.files.internal("ui/briefing/" + name + ".png");
        return file.exists() ? Optional.of(new Texture(file)) : Optional.empty();
    }

    @Override
    public Transition update(float seconds) {
        campaign.play(seconds);
        elapsed += seconds;
        MenuInput input = services.menu;
        switch (exit.update(input)) {
            case BRIEFING -> {}
            case ASKING -> {
                return Transition.STAY;
            }
            case STAYED -> {
                services.play(Sfx.MENU_BACK);
                return Transition.STAY;
            }
            case LEAVE -> {
                services.play(Sfx.MENU_CONFIRM);
                services.voices.stop();
                if (BriefingExit.autosaves(campaign)) {
                    services.save(SaveSlots.Slot.AUTOSAVE, campaign);
                }
                return Transition.replace(MainMenuScreen.menu(services));
            }
        }
        if (titleCard > 0) {
            titleCard -= seconds;
            if (input.confirm() || input.clicked()) {
                services.play(Sfx.MENU_CONFIRM);
                titleCard = 0;
            }
            return Transition.STAY;
        }
        sinceOpened += seconds;
        if (Float.isFinite(untilClosed)) {
            untilClosed -= seconds;
            return untilClosed > 0 ? Transition.STAY : Transition.replace(next.get());
        }
        String speaker = speaker();
        if (input.confirm() || input.clicked()) {
            services.play(Sfx.MENU_CONFIRM);
            pager.confirm();
        }
        if (pager.done()) {
            services.voices.stop();
            untilClosed = TransmissionStatic.SECONDS;
            intercept(false, false);
            return Transition.STAY;
        }
        speak(seconds);
        if (!speaker().equals(speaker)) {
            sinceOpened = 0;
        }
        int appeared = pager.update(seconds, services.settings().gameplay().briefingTextSpeed());
        boolean intercepted = screens.get(pager.page()).intercepted();
        intercept(intercepted, intercepted && !pager.pageComplete());
        for (int i = 0; i < appeared; i++) {
            // M9 part A: an intercepted page types under its carrier hiss, without the blip.
            if (++typed % 2 == 0 && !intercepted) {
                services.sfx.play(Sfx.TYPEWRITER, TYPING_VOLUME, 1, 0);
            }
        }
        return Transition.STAY;
    }

    /**
     * M9 part A: the enemy channel's sounds: its static sweep as an intercepted page opens and as it closes (to a
     * friendly page or the briefing's end), its carrier hiss while its text types ({@code typing}).
     */
    private void intercept(boolean intercepted, boolean typing) {
        if (intercepted != interceptShown) {
            services.sfx.play(intercepted ? Sfx.INTERCEPT_OPEN : Sfx.INTERCEPT_CLOSE, 1, 1, 0);
            interceptShown = intercepted;
        }
        boolean carrier = services.sfx.looping(Sfx.INTERCEPT_CARRIER);
        if (typing && !carrier) {
            services.sfx.loop(Sfx.INTERCEPT_CARRIER, CARRIER_VOLUME);
        } else if (!typing && carrier) {
            services.sfx.stop(Sfx.INTERCEPT_CARRIER);
        }
    }

    /** The short name of the current page's speaker. */
    private String speaker() {
        return screens.get(pager.page()).speakerName();
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        if (titleCard > 0) {
            drawTitleCard(batch, glass, script.titleCard().orElseThrow());
        } else {
            drawPages(batch, glass);
        }
        exit.dialog().ifPresent(dialog -> glass.dialog(batch, dialog));
    }

    private void drawPages(SpriteBatch batch, Glass glass) {
        services.titleScene.draw(batch, 0.35f);
        glass.panel(batch, LEFT_X, HEADER_Y, PixelScreen.WIDTH - 2 * LEFT_X, 34, 0.85f);
        if (screens.get(pager.page()).intercepted()) {
            // M9 part A: an intercepted page's header in place of the act and mission line.
            glass.shadowed(batch, glass.fonts.body, INTERCEPTED, ENEMY_CHANNEL, LEFT_X + 14, HEADER_Y + 8);
        } else {
            glass.shadowed(batch, glass.fonts.body, displayed(script.header()), Glass.AMBER, LEFT_X + 14, HEADER_Y + 8);
            glass.right(
                    batch,
                    glass.fonts.body,
                    script.mission(),
                    Glass.WHITE,
                    PixelScreen.WIDTH - LEFT_X - 14,
                    HEADER_Y + 8);
        }
        drawSpeaker(batch, glass);
        drawText(batch, glass);
        drawBottom(batch, glass);
    }

    private void drawSpeaker(SpriteBatch batch, Glass glass) {
        glass.panel(batch, LEFT_X, COLUMN_Y, LEFT_WIDTH, COLUMN_HEIGHT, 0.8f);
        Screen screen = screens.get(pager.page());
        Speaker speaker = screen.speaker();
        float centre = LEFT_X + LEFT_WIDTH / 2f;
        int portraitX = Math.round(centre - 72);
        int portraitY = COLUMN_Y + 18;
        glass.frame(batch, portraitX - 3, portraitY - 3, 150, 150);
        int portraitBottom = PixelScreen.HEIGHT - portraitY - 144;
        batch.draw(speaker.portrait(), portraitX, portraitBottom);
        float noise = TransmissionStatic.strength(sinceOpened, untilClosed);
        if (screen.intercepted()) {
            // M9 part A: the steady interference over an intercepted page's portrait for the whole page.
            services.transmissionStatic.drawSteady(
                    batch, speaker.portrait(), portraitX, portraitBottom, 144, 144, elapsed, noise);
        } else {
            services.transmissionStatic.draw(batch, portraitX, portraitBottom, 144, 144, noise);
        }
        Color plate = screen.intercepted() ? ENEMY_CHANNEL : Glass.AMBER;
        glass.centred(batch, glass.fonts.body, speaker.name(), plate, centre, portraitY + 158);
        glass.centred(
                batch,
                glass.fonts.label,
                speaker.role(),
                screen.intercepted() ? ENEMY_CHANNEL : Glass.CYAN,
                centre,
                portraitY + 180);
        String page = "PAGE " + (pager.page() + 1) + " / " + pager.pages();
        glass.centred(batch, glass.fonts.label, page, Glass.LABEL, centre, portraitY + 196);
    }

    private void drawText(SpriteBatch batch, Glass glass) {
        glass.panel(batch, TEXT_PANEL_X, COLUMN_Y, TEXT_PANEL_WIDTH, COLUMN_HEIGHT, 0.8f);
        Screen screen = screens.get(pager.page());
        screen.image().ifPresent(image -> {
            glass.frame(batch, TEXT_X - 3, IMAGE_Y - 3, IMAGE_WIDTH + 6, IMAGE_HEIGHT + 6);
            batch.draw(image, TEXT_X, PixelScreen.HEIGHT - IMAGE_Y - IMAGE_HEIGHT);
            if (screen.intercepted()) {
                // M9 part A: the page's image received on the same feed, under the same interference.
                imageRegion.setRegion(image);
                services.transmissionStatic.drawSteady(
                        batch,
                        imageRegion,
                        TEXT_X,
                        PixelScreen.HEIGHT - IMAGE_Y - IMAGE_HEIGHT,
                        Math.min(IMAGE_WIDTH, image.getWidth()),
                        Math.min(IMAGE_HEIGHT, image.getHeight()),
                        elapsed + 1.3f,
                        TransmissionStatic.STEADY);
            }
        });
        int top = textTop(screen.image().isPresent());
        BitmapFont font = glass.fonts.body;
        int left = pager.shown();
        List<String> lines = screen.lines();
        int cursorX = TEXT_X;
        int cursorY = top;
        for (int i = 0; i < lines.size() && left > 0; i++) {
            String line = lines.get(i).substring(0, Math.min(left, lines.get(i).length()));
            left -= line.length();
            glass.shadowed(
                    batch, font, line, screen.intercepted() ? ENEMY_CHANNEL : Glass.WHITE, TEXT_X, top + i * LINE);
            cursorX = TEXT_X + line.length() * 10 + 2;
            cursorY = top + i * LINE;
        }
        if (!pager.pageComplete() || elapsed % (2 * CURSOR_BLINK_SECONDS) < CURSOR_BLINK_SECONDS) {
            glass.bar(batch, screen.intercepted() ? ENEMY_CHANNEL : Glass.AMBER, cursorX, cursorY - 1, 10, 16);
        }
    }

    private void drawBottom(SpriteBatch batch, Glass glass) {
        int width = PixelScreen.WIDTH - 2 * LEFT_X;
        glass.panel(batch, LEFT_X, BOTTOM_Y, width, BOTTOM_HEIGHT, 0.85f);
        glass.header(batch, script.objectivesHeading(), LEFT_X + 12, TEASER_X - 20, BOTTOM_Y + 10);
        for (int i = 0; i < script.objectives().size(); i++) {
            int y = BOTTOM_Y + 26 + i * 14;
            glass.bar(batch, i == 0 ? Glass.AMBER : Glass.CYAN, LEFT_X + 14, y + 2, 5, 5);
            glass.shadowed(
                    batch,
                    glass.fonts.label,
                    script.objectives().get(i),
                    i == 0 ? Glass.WHITE : Glass.CYAN,
                    LEFT_X + 26,
                    y);
        }
        glass.header(batch, script.teaserHeading(), TEASER_X, LEFT_X + width - 12, BOTTOM_Y + 10);
        List<String> teaser = script.teaser();
        for (int i = 0; i < Math.min(3, teaser.size()); i++) {
            glass.shadowed(batch, glass.fonts.label, teaser.get(i), Glass.BODY, TEASER_X, BOTTOM_Y + 26 + i * 12);
        }
        String hints = pager.page() == pager.pages() - 1 && pager.pageComplete()
                ? script.done()
                : "ENTER CONTINUE    ESC MAIN MENU";
        glass.right(batch, glass.fonts.label, hints, Glass.DIM, LEFT_X + width - 12, BOTTOM_Y + 72);
    }

    /** The act title card: the letterboxed still of the act (or the title scene), the chrome act number and name, the settings and the missions. */
    private void drawTitleCard(SpriteBatch batch, Glass glass, ActData.TitleCard card) {
        glass.fill(batch, Color.BLACK, 0, 0, PixelScreen.WIDTH, PixelScreen.HEIGHT);
        titleStill.ifPresentOrElse(
                still -> {
                    batch.setColor(STILL_BRIGHTNESS, STILL_BRIGHTNESS, STILL_BRIGHTNESS, 1);
                    batch.draw(still, 0, 0);
                    batch.setColor(Color.WHITE);
                },
                () -> services.titleScene.draw(batch, SCENE_BRIGHTNESS));
        glass.fill(batch, Color.BLACK, 0, 0, PixelScreen.WIDTH, BAR);
        glass.fill(batch, Color.BLACK, 0, PixelScreen.HEIGHT - BAR, PixelScreen.WIDTH, BAR);
        Texture lettering = titleLettering.orElseThrow();
        int x = (PixelScreen.WIDTH - lettering.getWidth()) / 2;
        int top = 138;
        batch.draw(lettering, x, PixelScreen.HEIGHT - top - lettering.getHeight());
        glass.fill(batch, Glass.TRIM_LIGHT, 180, top + 22, 380 - 180, 1);
        glass.fill(batch, Glass.TRIM_LIGHT, 580, top + 22, 780 - 580, 1);
        int below = top + lettering.getHeight() + 24;
        glass.centred(batch, glass.fonts.body, displayed(card.line()), Glass.CYAN, PixelScreen.WIDTH / 2f, below);
        ActData.Levels levels =
                services.content.acts().get(script.actDirectory()).levels();
        String missions = String.format(Locale.ROOT, "MISSIONS %02d - %02d", levels.first(), levels.last());
        glass.centred(batch, glass.fonts.label, missions, Glass.LABEL, PixelScreen.WIDTH / 2f, PixelScreen.HEIGHT - 36);
    }

    /**
     * The page's voice starts with its first screen and plays over the screens it goes on over;
     * turning to another page cuts it (design/audio/voice, Briefings). The music ducks under it.
     */
    private void speak(float seconds) {
        int dataPage = screens.get(pager.page()).dataPage();
        if (dataPage != voicedPage) {
            voicedPage = dataPage;
            pageVoices.get(dataPage).ifPresentOrElse(services.voices::play, services.voices::stop);
        }
        services.voices.update(seconds);
        float target = services.voices.playing() ? LevelMusic.DUCKED : 1;
        duck += (target - duck) * Math.min(1, LevelMusic.DUCK_RATE * seconds);
        followFanfare(seconds, dataPage);
        followIntercept(seconds, screens.get(pager.page()).intercepted());
        music.ifPresent(streamer -> streamer.setVolume(MUSIC_VOLUME * duck * fade * (1 - motifFade)));
    }

    /**
     * The outro's fanfare plays under the first page; once it has played out or the page is left,
     * it fades out and the briefing theme fades in.
     */
    private void followFanfare(float seconds, int dataPage) {
        if (fanfare.isEmpty()) {
            fade = Math.min(1, fade + seconds / FADE_SECONDS);
            return;
        }
        Fanfare cue = fanfare.get();
        boolean leaving = dataPage > 0 || cue.ended();
        if (leaving) {
            fade -= seconds / FADE_SECONDS;
        }
        if (fade <= 0 || cue.ended()) {
            cue.close();
            fanfare = Optional.empty();
            fade = 0;
            music = theme();
            return;
        }
        cue.setVolume(FANFARE_VOLUME * duck * fade);
    }

    @Override
    public void dispose() {
        services.voices.stop();
        if (services.sfx.looping(Sfx.INTERCEPT_CARRIER)) {
            services.sfx.stop(Sfx.INTERCEPT_CARRIER);
        }
        music.ifPresent(MusicStreamer::close);
        motifMusic.ifPresent(MusicStreamer::close);
        fanfare.ifPresent(Fanfare::close);
        titleLettering.ifPresent(Texture::dispose);
        titleStill.ifPresent(Texture::dispose);
        images.values().forEach(image -> image.ifPresent(Texture::dispose));
    }
}
