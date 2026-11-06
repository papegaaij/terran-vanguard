package vanguard.game.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Optional;
import vanguard.content.CreditsData;
import vanguard.game.GameServices;
import vanguard.game.audio.Fanfare;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.Tracks;
import vanguard.game.input.Action;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.CreditsRoll;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;

/**
 * The credits (design/ui/credits, chosen credits-r08-a): the roll of {@code ui/credits.txt}, the
 * game's own credits around the attributions that CREDITS.md's licences ask for, scrolls up a glass
 * column over the dimmed title scene at a reading pace and stops with its last line in the middle.
 * Holding confirm or down (Enter, the D-pad, A) runs it faster, holding up runs it back; back (Esc /
 * B) returns to the screen below at any time, as confirm does once the roll has stopped. The mouse
 * wheel scrubs the roll, the right button goes back, and a click goes back once the roll has stopped.
 *
 * <p>M11 part A, step A3b (design/ui/credits, After the final level): the roll plays track 28 "Credits" ({@code
 * ui/credits}' data: once, again after {@code restart} s of silence while the roll is open), the music below it (the main
 * menu's title theme, the ending's track 27 over {@code fade} s) going quiet meanwhile ({@link Below}); after the
 * campaign's end it rolls over the ending pages' last image (the dark gate) at half brightness instead of the title
 * scene.
 */
public final class CreditsScreen implements GameScreen {
    /** The roll file, written from CREDITS.md by {@code :pipeline:credits}. */
    public static final String FILE = "ui/credits.txt";

    public static final int COLUMN_X = 200;
    public static final int COLUMN_WIDTH = 560;
    /** The text's margin inside the column on either side. */
    public static final int PADDING = 20;
    /** The width the roll's lines are wrapped to. */
    public static final int TEXT_WIDTH = COLUMN_WIDTH - 2 * PADDING;

    /** The roll shows between these heights, fading over {@link #FADE} px at either edge. */
    static final int TOP = 14;

    static final int BOTTOM = PixelScreen.HEIGHT - 40;
    static final int FADE = 40;
    /** The reading pace, px per second, and the factor holding a direction or confirm applies. */
    static final float SPEED = 30;

    static final float FAST = 8;
    /** One notch of the mouse wheel scrubs the roll by this many px, down the roll or back. */
    static final float WHEEL_STEP = 36;

    static final String HINTS = "HOLD ENTER/DOWN FASTER    HOLD UP REWIND    ESC BACK";
    static final String END_HINTS = "ENTER / ESC BACK";

    /** M11 part A: the credits' track's volume, before the mixer's music gain. */
    static final float MUSIC_VOLUME = 0.6f;
    /** M11 part A: the backdrop image's brightness behind the roll after the campaign. */
    static final float IMAGE_BRIGHTNESS = 0.5f;

    /** M11 part A: the music of the screen below while the roll is open: its share of its volume, 1 back to full. */
    @FunctionalInterface
    interface Below {
        Below NONE = share -> {};

        void volume(float share);
    }

    private final GameServices services;
    private final CreditsRoll roll;
    private final Below below;
    /** The share the music below fades out over, s (0: it is silenced at once). */
    private final float fade;

    private final CreditsData music;
    private final Optional<Texture> image;
    private Optional<Fanfare> track = Optional.empty();
    /** Seconds since the credits' track ended (its silence before it starts again); negative while it plays. */
    private float silence;

    private float elapsed;
    /** Where the roll's top is: it starts just below the column and ends with its last line in the middle. */
    private float offset = BOTTOM;

    public CreditsScreen(GameServices services) {
        this(services, Below.NONE, 0, Optional.empty());
    }

    /**
     * M11 part A, step A3b: the roll over a screen whose music goes quiet under it ({@code below}, faded over {@code
     * fade} s), over {@code image} (a path in the assets, the ending's last image) at half brightness when given.
     */
    CreditsScreen(GameServices services, Below below, float fade, Optional<String> image) {
        this.services = services;
        this.below = below;
        this.fade = fade;
        this.music = services.content.credits();
        this.image = image.map(services.files::internal)
                .filter(com.badlogic.gdx.files.FileHandle::exists)
                .map(Texture::new);
        track = playTrack();
        Fonts fonts = services.glass.fonts;
        roll = CreditsRoll.layout(
                services.files.internal(FILE).readString("UTF-8"),
                TEXT_WIDTH,
                size -> Fonts.advance(CreditsRoll.font(fonts, size)),
                Math.round(services.titleScene.logoHeight(CreditsRoll.LOGO_WIDTH)));
    }

    /** The offset at which the roll stops: its last line in the middle of the column. */
    private float end() {
        return (TOP + BOTTOM) / 2f - roll.height();
    }

    private Optional<Fanfare> playTrack() {
        return Fanfare.play(
                services.audio, services.files.internal(Tracks.path(music.music())), MUSIC_VOLUME, services.mixer);
    }

    /** M11 part A: the share of its volume the music below plays at {@code elapsed} s into the roll (a {@code fade}). */
    static float belowShare(float elapsed, float fade) {
        return fade <= 0 ? 0 : Math.clamp(1 - elapsed / fade, 0, 1);
    }

    /**
     * M11 part A: whether the credits' track starts again after {@code silence} s of silence since it ended ({@code
     * restart} s, design/ui/credits).
     */
    static boolean restarts(float silence, double restart) {
        return silence >= restart;
    }

    /** The credits' track: once it has ended, again after its silence. */
    private void followTrack(float seconds) {
        if (track.isEmpty() || !track.get().ended()) {
            return;
        }
        silence += seconds;
        if (restarts(silence, music.restart())) {
            track.get().close();
            track = playTrack();
            silence = 0;
        }
    }

    @Override
    public Transition update(float seconds) {
        elapsed += seconds;
        below.volume(belowShare(elapsed, fade));
        followTrack(seconds);
        boolean ended = offset <= end();
        if (services.menu.back() || (ended && (services.menu.confirm() || services.menu.clicked()))) {
            services.play(Sfx.MENU_BACK);
            below.volume(1);
            return Transition.BACK;
        }
        float speed = SPEED;
        if (services.input.held(Action.MENU_UP)) {
            speed = -SPEED * FAST;
        } else if (services.input.held(Action.MENU_CONFIRM) || services.input.held(Action.MENU_DOWN)) {
            speed = SPEED * FAST;
        }
        offset = Math.clamp(
                offset - speed * seconds - WHEEL_STEP * services.menu.pointer().wheel(), end(), BOTTOM);
        return Transition.STAY;
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        if (image.isPresent()) {
            // M11 part A: after the campaign, the dark gate at half brightness in the title scene's place.
            batch.setColor(IMAGE_BRIGHTNESS, IMAGE_BRIGHTNESS, IMAGE_BRIGHTNESS, 1);
            batch.draw(image.get(), 0, 0, PixelScreen.WIDTH, PixelScreen.HEIGHT);
            batch.setColor(1, 1, 1, 1);
        } else {
            services.titleScene.draw(batch, 0.5f);
        }
        // The column runs past the top and bottom of the screen: only its sides' trim shows.
        glass.panel(batch, COLUMN_X, -24, COLUMN_WIDTH, PixelScreen.HEIGHT + 48);
        roll.draw(
                batch, glass, services.titleScene, COLUMN_X + COLUMN_WIDTH / 2f, Math.round(offset), TOP, BOTTOM, FADE);
        glass.hints(batch, offset <= end() ? END_HINTS : HINTS);
    }

    @Override
    public void dispose() {
        track.ifPresent(Fanfare::close);
        image.ifPresent(Texture::dispose);
    }
}
