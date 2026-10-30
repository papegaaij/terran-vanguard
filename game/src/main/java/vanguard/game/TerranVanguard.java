package vanguard.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.DebugFit;
import vanguard.content.campaign.SaveSlots;
import vanguard.content.campaign.ScoreFile;
import vanguard.game.display.DisplayModes;
import vanguard.game.display.PointerCursor;
import vanguard.game.display.Screenshots;
import vanguard.game.render.PixelScreen;
import vanguard.game.screen.LevelScreen;
import vanguard.game.screen.MainMenuScreen;
import vanguard.game.screen.ScreenFlow;
import vanguard.game.settings.Settings;
import vanguard.game.settings.SettingsStore;
import vanguard.sim.SimStep;

/**
 * The game: runs the screen flow in the 960x540 pixel screen, samples the input actions once per
 * frame, toggles the display mode and takes screenshots (F12) on every screen. It starts at the
 * title screen or, for testing, straight in Level 01; in bench mode it exits after the set time.
 * While the window is minimised a level shows its pause menu and nothing is drawn. The mouse works
 * on every out-of-game screen with the glass pointer; while a level is flown the pointer is hidden
 * and the mouse does nothing.
 */
public final class TerranVanguard extends ApplicationAdapter {
    /** Frames longer than this (a stall, a dragged window) count as this long. */
    private static final float MAX_FRAME_SECONDS = 0.25f;

    private final DisplayModes displayModes;
    private final Settings settings;
    private final SettingsStore store;
    private final Difficulty difficulty;
    private final float timeScale;
    private final boolean invulnerable;
    private final boolean startLevel;
    private final Optional<DebugFit> debugFit;
    private final int level;
    private final boolean actEnd;
    private final java.util.OptionalDouble underwaterFrom;
    private final Optional<BenchRun> bench;
    private final Optional<ShotRun> shots;
    private final SaveSlots saves;
    private final ScoreFile scores;
    private final String version;
    private final Screenshots screenshots;
    /** The content, loading on its own thread from the launch on (startup step), while the window opens. */
    private final FutureTask<GameServices.Data> data = new FutureTask<>(GameServices.Data::load);
    /**
     * The content's validator checks, run on the loading thread after the parsing; the render thread throws their
     * problems once they are done, as the start did before. Null once seen.
     */
    private FutureTask<Void> checked = new FutureTask<>(() -> {
        data.get().parsed().check();
        return null;
    });

    private SpriteBatch batch;
    private PixelScreen pixelScreen;
    private GameServices services;
    private ScreenFlow screens;
    private PointerCursor cursor;
    /** The start's times until the first frame is drawn, then null. */
    private StartupTimes startup;
    /** Whether the sound effects still decode in the background (logged once they are ready). */
    private boolean soundsPending = true;

    /**
     * @param displayModes the display mode switcher, set up from the settings file
     * @param settings the settings from the settings file
     * @param store writes changed settings back
     * @param difficulty the difficulty the bench flies at, and the difficulty select starts on
     * @param timeScale a debug option: game time runs this many times faster than real time (1 = normal)
     * @param invulnerable a debug option: nothing hits the ship
     * @param startLevel start in Level 01 rather than at the title screen
     * @param debugFit a debug option: the weapons the level start flies
     * @param level a debug option: the level the level start flies
     * @param actEnd a debug option: winning the level start's level ends its act (act summary, outro)
     * @param underwaterFrom M8 part A, a debug option: the level start flies under water from this level second on
     * @param benchSeconds exit after this many seconds and log the frame count; 0 runs until quit
     * @param shots RF6, a debug option: the level ticks to write a shot of, one simulation step a frame, then exit;
     *     empty for none
     * @param saves the save slots: read-only in a debug run, which writes no save
     * @param scores the high-score file: read-only in a debug run
     * @param screenshots the directory the screenshot key writes into
     * @param version the game's version, {@code 0.1.0}, which the main menu shows as {@code v0.1}
     */
    public TerranVanguard(
            DisplayModes displayModes,
            Settings settings,
            SettingsStore store,
            Difficulty difficulty,
            float timeScale,
            boolean invulnerable,
            boolean startLevel,
            Optional<DebugFit> debugFit,
            int level,
            boolean actEnd,
            java.util.OptionalDouble underwaterFrom,
            double benchSeconds,
            List<Long> shots,
            SaveSlots saves,
            ScoreFile scores,
            Path screenshots,
            String version) {
        this.displayModes = displayModes;
        this.settings = settings;
        this.store = store;
        this.difficulty = difficulty;
        this.timeScale = timeScale;
        this.invulnerable = invulnerable;
        this.startLevel = startLevel;
        this.debugFit = debugFit;
        this.level = level;
        this.actEnd = actEnd;
        this.underwaterFrom = underwaterFrom;
        this.bench = benchSeconds > 0 ? Optional.of(new BenchRun(benchSeconds)) : Optional.empty();
        this.shots = shots.isEmpty() ? Optional.empty() : Optional.of(new ShotRun(level, shots));
        this.saves = saves;
        this.scores = scores;
        this.version = version;
        this.screenshots = new Screenshots(screenshots);
        FutureTask<Void> check = checked;
        Thread loader = new Thread(
                () -> {
                    data.run();
                    check.run();
                },
                "content-loader");
        loader.setDaemon(true);
        loader.start();
    }

    @Override
    public void create() {
        startup = new StartupTimes();
        Gdx.app.log("gl", Gdx.gl.glGetString(GL20.GL_RENDERER) + " / " + Gdx.gl.glGetString(GL20.GL_VERSION));
        batch = new SpriteBatch();
        pixelScreen = new PixelScreen();
        startup.mark("gl");
        services = new GameServices(
                Gdx.files,
                Gdx.audio,
                displayModes,
                settings,
                store,
                difficulty,
                invulnerable,
                underwaterFrom,
                saves,
                scores,
                version,
                data,
                startup);
        screens = new ScreenFlow(startLevel ? testLevel() : MainMenuScreen.title(services), Gdx.app::exit);
        Gdx.input.setInputProcessor(services.mouse);
        cursor = new PointerCursor(Gdx.files);
        startup.mark(startLevel ? "level" : "title");
    }

    /** Level 01 (or the {@code --level}) of a new campaign at the launch difficulty, for testing; a debug run, which writes no save. */
    private LevelScreen testLevel() {
        Campaign campaign = DebugFit.startAt(services.campaignRules, difficulty, level);
        debugFit.ifPresent(fit -> fit.applyTo(campaign, services.catalogue));
        if (actEnd) {
            campaign.debugActEnd();
        }
        return new LevelScreen(
                services,
                campaign,
                CampaignRoute.launch(services.content, campaign).orElseThrow());
    }

    @Override
    public void render() {
        float frameSeconds = Gdx.graphics.getDeltaTime();
        displayModes.poll();
        services.input.update(services.devices);
        services.sfx.update();
        if (checked != null && checked.isDone()) {
            GameServices.join(checked);
            checked = null;
        }
        // RF6: a shot run steps the simulation once a frame, whatever the frame's real time.
        float seconds = shots.isPresent() ? (float) SimStep.SECONDS : Math.min(frameSeconds, MAX_FRAME_SECONDS);
        services.menu.update(seconds);
        boolean flying = screens.pointerHidden();
        cursor.update(flying);
        services.mouse.scaling(services.settings().video().scaling());
        services.pointer.enabled(!flying);
        services.pointer.update(services.mouse, services.devices.focused(), seconds);
        screens.update(shots.isPresent() ? seconds : seconds * timeScale);
        screenshots.update(seconds);
        pixelScreen.begin(batch);
        screens.draw(batch);
        String shot = shots.isPresent() ? shots.get().due(screens.levelTick()) : null;
        if (shot != null) {
            screenshots.captureNow(pixelScreen, batch, shot);
        } else if (screenshots.requested()) {
            screenshots.capture(pixelScreen, batch);
        }
        screenshots.draw(batch, services.glass, services.fonts);
        pixelScreen.end(batch, services.settings().video());
        if (startup != null) {
            Gdx.app.log("startup", startup.report());
            startup = null;
        }
        if (soundsPending && services.sfx.finished()) {
            Gdx.app.log(
                    "startup",
                    String.format(Locale.ROOT, "sound effects ready after %.2f s", StartupTimes.sinceLaunch()));
            soundsPending = false;
        }
        if (bench.isPresent() && bench.get().frame(frameSeconds)) {
            Gdx.app.log("bench", bench.get().report());
            Gdx.app.exit();
        }
        if (shot != null && shots.get().done()) {
            Gdx.app.log("shots", "the last shot is written");
            Gdx.app.exit();
        }
    }

    /**
     * libGDX calls this when the window is minimised (on Windows and macOS also when a full-screen
     * window loses the focus) and before the game ends: a level opens its pause menu even where the
     * focus stays, and the game stops drawing until {@link #resume} rather than spinning unthrottled
     * (V-sync does not wait for a minimised window). The music thread keeps playing.
     */
    @Override
    public void pause() {
        services.input.interrupt();
        Gdx.graphics.setContinuousRendering(false);
    }

    /** The window is back: draw every frame again. */
    @Override
    public void resume() {
        Gdx.graphics.setContinuousRendering(true);
    }

    @Override
    public void resize(int width, int height) {
        displayModes.resized();
    }

    @Override
    public void dispose() {
        displayModes.storeCurrent();
        screens.dispose();
        cursor.dispose();
        services.dispose();
        pixelScreen.dispose();
        batch.dispose();
    }
}
