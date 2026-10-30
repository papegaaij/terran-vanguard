package vanguard.game;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.utils.Disposable;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.HighScores;
import vanguard.content.campaign.SaveSlots;
import vanguard.content.campaign.ScoreFile;
import vanguard.game.audio.Mixer;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.SfxBank;
import vanguard.game.audio.Voices;
import vanguard.game.display.DisplayModes;
import vanguard.game.input.ActionInput;
import vanguard.game.input.GdxDevices;
import vanguard.game.input.GdxPointer;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;
import vanguard.game.render.FlashShader;
import vanguard.game.render.Sprites;
import vanguard.game.render.TransmissionStatic;
import vanguard.game.settings.Settings;
import vanguard.game.settings.SettingsStore;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.game.ui.TitleScene;

/**
 * What every screen shares for the whole run: files and audio, the input devices and actions, the
 * settings, the display switcher, the launch difficulty, the game's content (the design data) with
 * the campaign's rules, the save slots, the sprite atlases, the mixer and the sound effects, the UI
 * kit with its fonts and the title scene, the flash shader and the portraits' transmission static.
 */
public final class GameServices implements Disposable {
    private static final Logger LOG = Logger.getLogger(GameServices.class.getName());

    /** The menu sounds' level. */
    private static final float MENU_VOLUME = 0.5f;

    public final Files files;
    public final Audio audio;
    public final GdxDevices devices;
    /** The mouse's events and position, the game's input processor. */
    public final GdxPointer mouse;
    /** The mouse as the out-of-game screens read it in a frame (design/ui, Mouse). */
    public final Pointer pointer;

    public final ActionInput input;
    public final MenuInput menu;
    public final DisplayModes display;
    /** The difficulty of {@code --difficulty}: the bench flies it, the difficulty select starts on it. */
    public final Difficulty difficulty;
    /** A debug option for testing ({@code --invulnerable}): nothing hits the ship. */
    public final boolean invulnerable;
    /**
     * M8 part A, a debug option for testing ({@code --medium underwater[@<s>]}): the level start flies under water from
     * this level second on; empty for the level's own medium.
     */
    public final java.util.OptionalDouble underwaterFrom;

    public final Content content;
    public final CampaignRules campaignRules;
    /** What the hangar shop sells, with its sell-back and repair rules. */
    public final Catalogue catalogue;
    /** The save slots in the saves directory next to the settings file; read-only in a debug run. */
    public final SaveSlots saves;
    /** The high scores in scores.yaml next to the settings file; read-only in a debug run. */
    public final ScoreFile scores;
    /** The game's version, {@code 0.1.0} ({@code dev} without the build's version file). */
    public final String version;

    public final Sprites sprites;
    public final Mixer mixer;
    public final SfxBank sfx;
    /** The spoken radio lines and briefing pages (design/audio/voice). */
    public final Voices voices;

    public final Fonts fonts;
    public final Glass glass;
    public final TitleScene titleScene;

    public final FlashShader flash;
    public final TransmissionStatic transmissionStatic;

    private final SettingsStore store;
    private Settings settings;

    GameServices(
            Files files,
            Audio audio,
            DisplayModes display,
            Settings settings,
            SettingsStore store,
            Difficulty difficulty,
            boolean invulnerable,
            java.util.OptionalDouble underwaterFrom,
            SaveSlots saves,
            ScoreFile scores,
            String version,
            Future<Data> data,
            StartupTimes times) {
        this.files = files;
        this.audio = audio;
        this.display = display;
        this.settings = settings;
        this.store = store;
        this.difficulty = difficulty;
        this.invulnerable = invulnerable;
        this.underwaterFrom = underwaterFrom;
        devices = new GdxDevices();
        devices.deadZone(settings.controls().deadZone());
        mouse = new GdxPointer();
        pointer = new Pointer();
        input = new ActionInput(settings.controls().bindings());
        menu = new MenuInput(input, pointer);
        this.saves = saves;
        this.scores = scores;
        this.version = version;
        times.mark("input");
        sprites = new Sprites(files);
        times.mark("sprites");
        mixer = new Mixer(settings.audio());
        sfx = new SfxBank(audio, files, mixer);
        times.mark("sfx");
        fonts = new Fonts(files);
        glass = new Glass(fonts, sprites);
        times.mark("fonts");
        titleScene = new TitleScene(files);
        times.mark("title scene");
        flash = new FlashShader();
        transmissionStatic = new TransmissionStatic();
        times.mark("shaders");
        // The content loads on its own thread from the launch on (startup step); this waits for what is left of it.
        Data loaded = join(data);
        content = loaded.content();
        campaignRules = loaded.campaignRules();
        catalogue = loaded.catalogue();
        times.mark("content wait");
        voices = new Voices(audio, files, mixer, content);
        times.mark("voices");
    }

    /**
     * The game's content (the design data, parsed) with the campaign's rules and the shop's catalogue: plain Java, so
     * it loads on a background thread while the window opens and the textures and sounds load; the validator's checks
     * run on that thread after it ({@link ContentLoader.Parsed#check}), while the title screen shows.
     */
    record Data(ContentLoader.Parsed parsed, CampaignRules campaignRules, Catalogue catalogue) {
        /** Parses the data files on the classpath. */
        static Data load() {
            ContentLoader.Parsed parsed = ContentLoader.parseClasspath();
            Content content = parsed.content();
            return new Data(parsed, CampaignRules.of(content), Catalogue.of(content));
        }

        Content content() {
            return parsed.content();
        }
    }

    /** A background task's result; its failure (a content problem) is thrown here as it was thrown there. */
    static <T> T join(Future<T> data) {
        try {
            return data.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while loading the content", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException problem) {
                throw problem;
            }
            throw new IllegalStateException("could not load the content", e.getCause());
        }
    }

    public Settings settings() {
        return settings;
    }

    /** Applies changed settings at once: bindings, dead zone and volumes; the screens read the rest each frame. */
    public void change(Settings changed) {
        settings = changed;
        input.rebind(changed.controls().bindings());
        devices.deadZone(changed.controls().deadZone());
        mixer.set(changed.audio());
    }

    /** Writes the settings to the settings file. */
    public void saveSettings() {
        store.save(settings);
    }

    /**
     * Whether this is a debug run (a level start, {@code --bench}, {@code --invulnerable}, {@code
     * --debug-speed}): it writes no save at all, its save slots are read-only (design/systems/saves).
     */
    public boolean debugRun() {
        return !saves.writable();
    }

    /**
     * Writes the campaign into a save slot; a failure is logged, since it must not stop the game. A
     * replay is never saved (design/ui/mission-select), nor is anything in a debug run: nothing is
     * written for them.
     *
     * @return whether the save was written
     */
    public boolean save(SaveSlots.Slot slot, Campaign campaign) {
        if (campaign.replay().isPresent()) {
            return false;
        }
        try {
            return saves.write(slot, campaign.save(Instant.now()));
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "could not write save slot " + slot.index() + " in " + saves.directory(), e);
            return false;
        }
    }

    /**
     * Writes the high scores; a failure is logged, since it must not stop the game, and a debug run
     * writes nothing.
     *
     * @return whether the file was written
     */
    public boolean saveScores(HighScores table) {
        try {
            return scores.write(table);
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "could not write the high scores to " + scores.path(), e);
            return false;
        }
    }

    /** Plays a menu sound. */
    public void play(Sfx sound) {
        sfx.play(sound, MENU_VOLUME, 1, 0);
    }

    @Override
    public void dispose() {
        transmissionStatic.dispose();
        flash.dispose();
        titleScene.dispose();
        fonts.dispose();
        voices.stop();
        sfx.dispose();
        sprites.dispose();
    }
}
