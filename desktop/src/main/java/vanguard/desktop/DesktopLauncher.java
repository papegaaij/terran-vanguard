package vanguard.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.audio.Lwjgl3Audio;
import java.nio.file.Path;
import vanguard.content.campaign.SaveSlots;
import vanguard.content.campaign.ScoreFile;
import vanguard.game.TerranVanguard;
import vanguard.game.display.Bounds;
import vanguard.game.display.DisplayModes;
import vanguard.game.display.DisplaySettings;
import vanguard.game.display.Monitors;
import vanguard.game.display.WindowMode;

/**
 * Starts the game in the display mode, on the monitor and at the window position of the settings
 * file: on the first start in borderless full screen. Alt+Enter or F11 switch modes at any time.
 */
public final class DesktopLauncher {
    /** Raised from libGDX's default of 16 so many effects plus the music stream can play at once. */
    private static final int AUDIO_SOURCES = 64;
    /** Stream buffers: 8 x 4096 bytes = 186 ms of 44.1 kHz stereo (bytes, not samples as libGDX's Javadoc says). */
    private static final int AUDIO_BUFFER_BYTES = 4096;

    private static final int AUDIO_BUFFER_COUNT = 8;

    /**
     * The window icons (tools/art/app_icon.py): GLFW picks the size the platform asks for; macOS has
     * no window icons, the app bundle's icns stands in.
     */
    static final String[] WINDOW_ICONS = {
        "icon/icon-16.png",
        "icon/icon-32.png",
        "icon/icon-48.png",
        "icon/icon-64.png",
        "icon/icon-128.png",
        "icon/icon-256.png"
    };

    private DesktopLauncher() {}

    public static void main(String[] args) {
        if (printsVersion(args)) {
            System.out.println(Version.line());
            return;
        }
        System.out.println(Version.line());
        LaunchOptions options = LaunchOptions.parse(args);
        var settingsFile = new SettingsFile(
                options.settingsFile().orElseGet(() -> ConfigDirectory.current().resolve(SettingsFile.FILE_NAME)));
        DisplaySettings settings = settingsFile.read();
        System.out.println("settings: " + settingsFile.path());
        var saves = saveSlots(options, settingsFile.path());
        if (!saves.writable()) {
            System.out.println("debug run: no save is written");
        }
        // So do the screenshots of the screenshot key (design/ui/controls).
        var screenshots = settingsFile.path().toAbsolutePath().resolveSibling("screenshots");
        var displayModes = new DisplayModes(settings, settingsFile::write);
        var game = new TerranVanguard(
                displayModes,
                settingsFile.readSettings(),
                settingsFile,
                options.difficulty(),
                options.debugSpeed(),
                options.invulnerable(),
                options.startLevel(),
                options.debugFit(),
                options.level(),
                options.actEnd(),
                options.underwaterFrom(),
                options.benchSeconds(),
                saves,
                scoreFile(options, settingsFile.path()),
                screenshots,
                Version.current());
        new Lwjgl3Application(game, configuration(settings, System.getProperty("os.name"))) {
            /** libGDX's OpenAL audio with the master limiter on the final mix (design/audio, Master limiter). */
            @Override
            public Lwjgl3Audio createAudio(Lwjgl3ApplicationConfiguration config) {
                return new LimitedAudio(AUDIO_SOURCES, AUDIO_BUFFER_COUNT, AUDIO_BUFFER_BYTES);
            }
        };
    }

    /** Whether the command line asks only for the version ({@code --version}): print it and exit. */
    static boolean printsVersion(String[] args) {
        return args.length == 1 && args[0].equals("--version");
    }

    /**
     * The save slots: they live next to the settings file (design/systems/saves), so --settings
     * moves them too; a debug run only reads them, it writes no save.
     */
    static SaveSlots saveSlots(LaunchOptions options, Path settingsFile) {
        Path directory = settingsFile.toAbsolutePath().resolveSibling("saves");
        return options.debugRun() ? SaveSlots.readOnly(directory) : new SaveSlots(directory);
    }

    /**
     * The high scores (design/systems/scoring): scores.yaml next to the settings file, like the
     * saves; a debug run only reads it.
     */
    static ScoreFile scoreFile(LaunchOptions options, Path settingsFile) {
        Path file = settingsFile.toAbsolutePath().resolveSibling(ScoreFile.FILE_NAME);
        return options.debugRun() ? ScoreFile.readOnly(file) : ScoreFile.of(file);
    }

    private static Lwjgl3ApplicationConfiguration configuration(DisplaySettings settings, String osName) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Terran Vanguard");
        if (!osName.startsWith("Mac")) {
            config.setWindowIcon(FileType.Classpath, WINDOW_ICONS);
        }
        Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
        Monitor monitor =
                Monitors.find(monitors, settings.monitor(), Lwjgl3ApplicationConfiguration.getPrimaryMonitor());
        Bounds window = settings.window()
                .filter(bounds -> Monitors.isOnAny(monitors, bounds))
                .orElseGet(() -> Monitors.newWindow(monitor));
        config.setWindowedMode(window.width(), window.height());
        config.setWindowPosition(window.x(), window.y());
        if (settings.mode() == WindowMode.FULL_SCREEN) {
            // The monitor's current mode: GLFW attaches the window without a video mode switch.
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode(monitor));
        }
        config.setAutoIconify(autoIconify(osName));
        config.useVsync(true);
        config.setAudioConfig(AUDIO_SOURCES, AUDIO_BUFFER_BYTES, AUDIO_BUFFER_COUNT);
        return config;
    }

    /**
     * Whether a full-screen window minimises when it loses the focus (GLFW's default). Not on Linux
     * and other Unix systems (X11): there a screenshot tool, a notification or a launcher taking the focus would throw the player
     * to the desktop, and the window manager lets other windows above an unfocused full-screen
     * window anyway; the level pauses on the focus loss either way. Windows keeps it, because GLFW
     * keeps its full-screen windows topmost, and macOS too.
     */
    static boolean autoIconify(String osName) {
        return osName.startsWith("Windows") || osName.startsWith("Mac");
    }
}
