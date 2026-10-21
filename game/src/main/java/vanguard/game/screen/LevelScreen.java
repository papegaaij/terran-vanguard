package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.DifficultyData;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.content.SpecialsData;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.Flight;
import vanguard.content.campaign.SaveSlots;
import vanguard.game.GameServices;
import vanguard.game.audio.DelayedSounds;
import vanguard.game.audio.FlightSounds;
import vanguard.game.audio.LevelMusic;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.Tracks;
import vanguard.game.audio.Voices;
import vanguard.game.input.Action;
import vanguard.game.input.FlightCommands;
import vanguard.game.level.Barks;
import vanguard.game.level.ControlPrompts;
import vanguard.game.level.Outro;
import vanguard.game.level.PromptTexts;
import vanguard.game.level.RadioQueue;
import vanguard.game.level.RadioSchedule;
import vanguard.game.render.AirborneWalkers;
import vanguard.game.render.AtmosphereRamp;
import vanguard.game.render.BossBanner;
import vanguard.game.render.CreditNumbers;
import vanguard.game.render.DroneLooks;
import vanguard.game.render.EdgeWarnings;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.EventPrompt;
import vanguard.game.render.Hud;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.MilitiaFlak;
import vanguard.game.render.PodPivots;
import vanguard.game.render.ScreenFlash;
import vanguard.game.render.ScreenShake;
import vanguard.game.render.SecretGlints;
import vanguard.game.render.SetPieceWrecks;
import vanguard.game.render.StormLooks;
import vanguard.game.render.TargetingOverlay;
import vanguard.game.render.ThreatArrows;
import vanguard.game.render.WaveBanners;
import vanguard.game.render.WeaponLooks;
import vanguard.game.settings.Settings;
import vanguard.sim.CueTrigger;
import vanguard.sim.FixedStepClock;
import vanguard.sim.HazardSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelResult;
import vanguard.sim.LevelScript;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Shot;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSpec;
import vanguard.sim.WarningEdge;
import vanguard.sim.Wingman;

/**
 * Flying a level of the campaign: runs the simulation at its fixed step from the campaign's
 * level-start state, turns its events into sound, effects, radio chatter and HUD feedback, plays
 * the level's music cues and draws it interpolated. When the level is won it shows the radio's
 * last messages ({@link Outro}), banks the result in the campaign and shows the debrief. When the ship is destroyed the
 * death plays out, then the mission failed screen opens over the level, or the game over screen
 * follows when no retry is left (design/systems/retry); the failure's used retry is autosaved at
 * once, and so is a game over's return to the hangar before the level. A failed primary objective (the
 * convoy lost) fails the level the same way without the explosion and the slow motion, and the
 * mission failed screen shows the level's own failure line. The sortie flies what {@link Flight} maps of the fitted loadout; the HUD lists the fitted
 * items that fly from M4 on. Pause (Esc / P / Start), the window losing
 * the focus and a gamepad disconnecting open the pause menu over it. The Gameplay tab's text speed
 * and flash reduction and the Controls tab's auto-fire apply from the next frame on.
 *
 * <p>Each step's events go, one by one, to the radio ({@link RadioEvents}), to the play field's effects
 * ({@link EventEffects}) and then to this screen's own handling (music, HUD and the attempt's flow).
 */
public final class LevelScreen implements GameScreen {
    /** Every run of a level gets the same waves; the seed only picks hover times. */
    private static final long SEED = 2185;

    private static final int MAX_STEPS_PER_FRAME = 8;
    /** The sensor suite's level from which it shows the threat arrows (design/player/systems). */
    private static final int THREAT_ARROW_SENSOR = 2;
    /** Death plays in slow motion for a second (design/systems/retry). */
    private static final float SLOW_MOTION_SECONDS = 1;

    private static final float SLOW_MOTION_RATE = 0.5f;
    /** From the ship's destruction to the mission failed screen: the explosion, the slow motion and the sting's start. */
    static final float FAILED_SCREEN_SECONDS = 3;
    /** The ship's blue shimmer fades over this many steps after a shield hit. */
    private static final int SHIMMER_TICKS = 8;

    static final int TINY_EXPLOSION_FRAME_TICKS = 2;
    static final int LARGE_EXPLOSION_FRAME_TICKS = 4;
    /** A missile's smoke trail: a puff every this many steps, each frame of it shown this long. */
    private static final int TRAIL_TICKS = 4;

    private static final int TRAIL_FRAME_TICKS = 2;
    /** A ground unit's remains stay on the ground until they scroll off (at most 10 s). */
    static final int REMAINS_TICKS = SimStep.ticks(10);

    /**
     * M5 part E: the water effects' frames (tools/art/water_fx.py's 20 fps: 3 steps a frame), a ripple
     * train's (12 frames over 1.6 s) and its start after a burst; the frigate's flak puff (6 steps a
     * frame); a raft's sinking (6 steps a frame, 1 s); a severed arm's pops (4 steps a frame, each
     * segment 3 steps after the one rootward of it); the Kraken's death burst (8 steps a frame, centred
     * 40 px below its head).
     */
    static final int WATER_FRAME_TICKS = 3;

    private static final float RADIO_VOLUME = 0.5f;
    private static final float TYPING_VOLUME = 0.15f;

    private final GameServices services;
    private final Campaign campaign;
    private final LevelData level;
    /** The level's number: its unit atlas stays loaded while the screen is open. */
    private final int levelNumber;

    private final Sortie sortie;
    private final FixedStepClock clock = new FixedStepClock(SimStep.SECONDS, MAX_STEPS_PER_FRAME);
    private final EnemyLooks[] looks;
    /** M5 part C: the ground units in a pounce's air window before the step, to tell where a death belongs. */
    private final AirborneWalkers airborne = new AirborneWalkers();

    private final WeaponLooks weaponLooks;

    private final Array<AtlasRegion> flak;
    /** M7 part D: Level 17's militia flak (the level's {@code flak}); null without it. */
    private MilitiaFlak militiaFlak;

    /** The layers on which an enemy was destroyed in this attempt: a prompt that skips on one of them has left. */
    private final EnumSet<Layer> layersHit = EnumSet.noneOf(Layer.class);

    private final FlightSounds sounds;
    private final LevelRenderer renderer;
    private final Hud hud;
    private final Effects effects = Effects.glowing();
    private final Effects debris = Effects.solid();
    /** The solid death pieces of units: a flyer's at its play-field position, a ground unit's on the ground. */
    private final Effects pieces = Effects.solid();
    /** The Airstrike's blasts on the ground, so they stay where the bombs landed. */
    private final Effects blasts = Effects.glowing();
    /** The set pieces breaking up at their death. */
    private final SetPieceWrecks wrecks;

    private final CreditNumbers creditNumbers = new CreditNumbers();
    private final EdgeWarnings warnings;
    /** An act boss's warning banner, its death's screen flash and the chain's bursts' sounds. */
    private final BossBanner banner;
    /** The set piece the banner announces, -1 before one. */
    private int bannerPiece = -1;
    /** Round 41: the set pieces warned of ahead of their arrival ({@code BOSS_INCOMING}) in this attempt. */
    private final java.util.Set<Integer> announced = new java.util.HashSet<>();
    /** The wave warning banner of a side or rear wave's edge warning (design/ui/hud). */
    private final WaveBanners waveBanners;
    /** The sensor suite's threat arrows, with a sensor suite at L2+ (design/player/systems); null without. */
    private final ThreatArrows threatArrows;

    private final ScreenFlash screenFlash = new ScreenFlash();
    private final DelayedSounds chainSounds;

    private final RadioQueue radio = new RadioQueue();
    private final RadioSchedule radioSchedule;
    /** The radio's side of the events: the cued lines, the calls, the warnings and Rook's barks. */
    private final RadioEvents radioEvents;
    /** The play field's side of the events: impacts, kills, deaths, wrecks and the renderer's hand-offs. */
    private final EventEffects eventEffects;
    /** Rook's radio barks while he flies in the level (M5 part A); null without him. */
    private final Barks barks;

    private final ControlPrompts prompts;
    private final PromptTexts promptTexts;
    private final LevelMusic music;
    private final String name;
    private final Flight flight;
    private float slowMotion;
    private final Outro outro = new Outro();
    /** What the ship's destruction leads to, and how long until its screen opens. */
    private Optional<Campaign.Failure> failure = Optional.empty();

    private float failedIn;
    /** M5 part G: whether the campaign flies on easy (Level 13's shorter whiteout). */
    private final boolean easy;
    /**
     * M5 part F: the shake-off's event prompt in the play field (Level 12), on every latch until the
     * attempt's first shake-off; null in a level without one.
     */
    private final EventPrompt latchPrompt;
    /** The collect pop's sprite and its frames' steps (20 fps). */
    static final String SONDE_COLLECT = "weather-sonde-collect";

    private boolean launchPending = true;
    private int shimmer;
    private int typed;

    /**
     * The campaign's next level, flown from the campaign's level-start state.
     *
     * @param levelKey the level, as in {@link Content#level(String)}
     */
    public LevelScreen(GameServices services, Campaign campaign, String levelKey) {
        this.services = services;
        this.campaign = campaign;
        Difficulty difficulty = campaign.difficulty();
        level = services.content.level(levelKey);
        levelNumber = Content.levelNumber(levelKey);
        // The level's own sprites (its unit atlas) until the screen closes.
        services.sprites.enterLevel(levelNumber);
        Rules rules = SimSpecs.rules(services.content, levelKey, difficulty);
        campaign.launch();
        flight = Flight.of(services.content, services.catalogue, campaign);
        sortie = new Sortie(
                SEED,
                flight.loadout(),
                SimSpecs.level(services.content, levelKey, difficulty),
                services.invulnerable ? rules.withInvulnerableShip() : rules,
                campaign.armour());
        radioSchedule = new RadioSchedule(sortie.script(), RadioSchedule.fitted(sortie));
        barks = sortie.wingman().isPresent()
                ? Barks.of(
                        services.content.wingmen().barks(),
                        sortie.script(),
                        RadioSchedule.fitted(sortie),
                        radio,
                        this::bark)
                : null;
        radioEvents = new RadioEvents(services, level, sortie, radio, radioSchedule, barks);
        wrecks = new SetPieceWrecks(sortie.setPieceCount());
        looks = EnemyLooks.of(sortie.enemyKinds(), services.sprites, services.content);
        weaponLooks = new WeaponLooks(
                sortie.armament(),
                flight.weapons().stream().mapToInt(Flight.Weapon::level).toArray(),
                services.sprites,
                new PodPivots(services.files),
                sortie.wingman().map(Wingman::gun));
        flak = optionalFrames(services, "escort-frigate-flak");
        sounds = new FlightSounds(
                services.sfx,
                looks,
                sortie.armament(),
                sortie.script().setPieces().stream().map(SetPieceSpec::slug).toList(),
                sortie.wingman().map(Wingman::gun));
        // M5 part E: Level 11's sounds: ships on the convoy, the Kraken's slam cycle for its sound's timing.
        sounds.naval(sortie.navalConvoy());
        // M5 part G: Level 13's relay sounds and its supply drones' launches.
        sounds.defend(
                sortie.defending(),
                level.released().stream()
                        .filter(drop -> drop.sprite().isPresent())
                        .mapToDouble(LevelData.Drop::t)
                        .toArray(),
                DroneLooks.ARC_SECONDS);
        // M7 part D: Level 17's domes: glass hits and breaks, the bombers' drops.
        sounds.ground(sortie.groundAllies());
        // M5 part H: Level 14's canopy withers at the Siege Spire's death.
        sounds.canopy(Sfx.afterBoss(Sfx.ambience(level.music().ambience())) != null);
        // M7 part H: Level 21's ridge pass rumbles (the Colossus's back under the dunes); Level 20's ridge has its bed.
        if (breachingBoss(sortie.script())) {
            level.ridge().ifPresent(ridge -> sounds.ridgePass(ridge.t()));
        }
        easy = difficulty == Difficulty.EASY;
        sortie.script().setPieces().stream()
                .flatMap(piece -> piece.boss().stream())
                .flatMap(boss -> boss.arena().stream())
                .map(arena -> arena.slam())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .ifPresent(slam -> sounds.slamCycle(slam.telegraphSeconds(), slam.riseSeconds(), slam.secondSeconds()));
        sounds.flareSeconds(sortie.script()
                .hazards()
                .darkness()
                .map(HazardSpec.Darkness::flareSeconds)
                .orElse(0.0));
        renderer = new LevelRenderer(
                services.sprites,
                looks,
                weaponLooks,
                services.files,
                services.flash,
                services.fonts.body,
                level,
                sortie.script(),
                levelKey);
        // M5 part G: easy's shorter whiteout (Level 13), in the backdrop and the snow; M7 part E: the storm's radii.
        renderer.difficulty(difficulty);
        // M7 part D: Level 17's domes, each in its sprite set.
        renderer.groundAllies(sortie.script(), services.content.allies());
        militiaFlak = level.flak().map(MilitiaFlak::new).orElse(null);
        warnings = new EdgeWarnings(services.sprites.pixel, services.fonts.body);
        banner = new BossBanner(services.sprites.pixel, services.fonts.heading, services.fonts.body);
        waveBanners = new WaveBanners(services.sprites.pixel, services.fonts.body, sortie.airEscort());
        latchPrompt = level.prompts().orElse(List.of()).stream()
                .filter(LevelData.Prompt::onEvent)
                .findFirst()
                .map(prompt -> new EventPrompt(
                        services.sprites.pixel, services.fonts.body, prompt.action(), prompt.keys(), prompt.seconds()))
                .orElse(null);
        threatArrows = flight.sensor() >= THREAT_ARROW_SENSOR ? new ThreatArrows() : null;
        renderer.modules(
                flight.targeting()
                        ? new TargetingOverlay(
                                services.sprites.pixel,
                                services.content,
                                sortie.enemyKinds(),
                                SimSpecs.targeting(services.content))
                        : null,
                flight.salvage() > 0 ? new SecretGlints(services.sprites) : null);
        chainSounds = new DelayedSounds(services.sfx);
        eventEffects = new EventEffects(
                services,
                level,
                sortie,
                looks,
                weaponLooks,
                airborne,
                flak,
                sounds,
                renderer,
                effects,
                debris,
                pieces,
                blasts,
                wrecks,
                creditNumbers,
                screenFlash,
                chainSounds,
                layersHit);
        name = Content.levelName(levelKey);
        hud = new Hud(
                services.sprites,
                services.fonts,
                services.transmissionStatic,
                sortie.script().number(),
                name,
                campaign.credits(),
                flight,
                SimSpecs.regenBonus(services.content, flight.sparePower()),
                services.content.player().pickups().overdrive().seconds());
        prompts = new ControlPrompts(level.controlPrompts());
        promptTexts = new PromptTexts(services.input.bindings());
        // The level's theme (design/audio/music, track list) as its two stems, over its setting's ambience.
        String theme = theme(level.music().track());
        var full = services.files.internal(Tracks.path(theme));
        var base = services.files.internal(Tracks.basePath(theme));
        music = new LevelMusic(
                services.audio,
                services.mixer,
                // A theme without stems plays its full mix as both (the intensity layer always on).
                base.exists() ? base : full,
                full,
                services.sfx,
                Sfx.ambience(level.music().ambience()),
                level.music().startSection(),
                level.music().startDb().orElse(0.0),
                level.music()::full,
                level.music().ambienceFrom().orElse(Double.POSITIVE_INFINITY),
                level.music().voiceLoop().flatMap(loop -> voiceLoop(level, loop)),
                bossMusic(level.music())
                        .map(names -> new LevelMusic.Boss(
                                names.warning().map(name -> services.files.internal(Tracks.path(name))),
                                names.track().map(name -> services.files.internal(Tracks.path(name))),
                                Tracks.BOSS_WARNING_BARS_SECONDS)));
        // M5 part D: the ambience crossfading by section (Level 10's ocean from section 4).
        music.ambienceChanges(ambienceChanges(level.music()));
        // M5 part F: the theme's level by section (Level 12's eye at -6 dB).
        music.sectionLevels(level.music()::sectionLevel);
        // M5 part G: the jingle inside the level (Level 13's "Grid sync complete"), the wind in the whiteout.
        level.music().jingle().ifPresent(jingle -> music.jingle(jingle.t(), jingle.fadeSeconds()));
        // M7 part E: the dust storm's bed swelling as the view closes in (Level 18), and track 9's motif layer.
        // M7 part H: the wind's calm over Level 21's settling dust, from the crawling arena's end (the kill's jump).
        double calmFrom = crawlEnd(sortie.script());
        music.ambienceLevels(t -> AtmosphereRamp.ambienceDb(level, easy, t)
                + StormLooks.swellDb(level, difficulty, t)
                + LevelMusic.calmDb(t, calmFrom));
        music.motif(services.files.internal(Tracks.motifPath(theme)), level.music()::motifOn);
        // M7 part F: the sections that play the motif layer alone over the ambience (Level 19's crawl).
        music.motifAlone(level.music()::motifAlone);
        // M7 part G: the ambience layers over the setting's loop (Level 20's basin rumble under the ridge).
        music.ambienceLayers(ambienceLayers(level.music()));
    }

    /** M7 part H: the end of the level's crawling arena (a breaching boss's, Level 21's), s; NaN without one. */
    public static double crawlEnd(LevelScript script) {
        for (Section section : script.sections()) {
            if (section.crawls()) {
                return section.end();
            }
        }
        return Double.NaN;
    }

    /** M7 part H: whether the level's boss breaches (the Dust Colossus): its ridge pass has the boss's rumble. */
    public static boolean breachingBoss(LevelScript script) {
        return script.setPieces().stream()
                .flatMap(piece -> piece.boss().stream())
                .anyMatch(boss -> boss.breach().isPresent());
    }

    /** M7 part G: the level's {@code music.ambience_layers}, each its sound at its level by the level time. */
    static List<LevelMusic.AmbienceLayer> ambienceLayers(LevelData.Music music) {
        return music.ambienceLayers().orElse(List.of()).stream()
                .map(layer -> new LevelMusic.AmbienceLayer(Sfx.ambience(layer.ambience()), layer::level))
                .toList();
    }

    /** M5 part D: the level's {@code music.ambience_changes} as the music's crossfades, each to its setting's loop. */
    static List<LevelMusic.AmbienceChange> ambienceChanges(LevelData.Music music) {
        return music.ambienceChangeList().stream()
                .map(change -> new LevelMusic.AmbienceChange(
                        change.section(), Sfx.ambience(change.ambience()), change.crossfade()))
                .toList();
    }

    /** A level's act boss music by file name: the warning (track 22) and the boss track (track 18). */
    record BossMusic(Optional<String> warning, Optional<String> track) {}

    /**
     * The level's {@code music.boss_warning} and {@code boss_track} (design/tech/architecture, the
     * level music block); empty without either.
     */
    static Optional<BossMusic> bossMusic(LevelData.Music music) {
        Optional<String> warning = music.bossWarning();
        Optional<String> track = music.bossTrack();
        return warning.isEmpty() && track.isEmpty() ? Optional.empty() : Optional.of(new BossMusic(warning, track));
    }

    /** A music block's voice loop: its speaker's first timed radio line, if it has a voice file. */
    private Optional<LevelMusic.VoiceLoop> voiceLoop(LevelData level, LevelData.Music.VoiceLoop loop) {
        return level.radio().stream()
                .filter(cue -> cue.t().isPresent() && cue.speaker().equals(loop.speaker()))
                .findFirst()
                .flatMap(cue -> services.voices.radio(
                        cue.speaker(),
                        cue.line(),
                        cue.expression().orElse(Expression.NEUTRAL).slug()))
                .map(voice -> new LevelMusic.VoiceLoop(
                        services.files.internal(voice.path()), loop.section(), (float) Math.pow(10, loop.db() / 20)));
    }

    /** The file name of a level theme by its track number (design/audio/music, track list). */
    static String theme(int track) {
        return Tracks.name(track)
                .orElseThrow(() -> new IllegalArgumentException("no music file for track " + track + " yet"));
    }

    /** "MISSION 01 - BREAK AT DAWN", for the pause menu. */
    String mission() {
        return String.format(Locale.ROOT, "MISSION %02d - %s", sortie.script().number(), name)
                .toUpperCase(Locale.ROOT);
    }

    Campaign campaign() {
        return campaign;
    }

    /** The time flown in this attempt. */
    double seconds() {
        return sortie.levelSeconds();
    }

    long score() {
        return sortie.score();
    }

    /** What the attempt has earned so far, for the mission failed screen. */
    int attemptCredits() {
        return sortie.credits();
    }

    /** The level's own line for the mission failed screen, when its primary objective failed (design/systems/retry). */
    Optional<RadioCue> failureLine() {
        return radioEvents.failureLine();
    }

    /**
     * The mission failed (or game over) screen takes the radio over (design/systems/retry): the voice
     * on the radio is cut and the HUD's radio box closes with it, so no line stands frozen under the
     * screen, which speaks the level's own failure line (Level 13's capture, round 35: Varga's line
     * stayed in the box under Okafor's "Nansen is gone").
     */
    static void failedScreenOpens(RadioQueue radio, Runnable stopVoice) {
        stopVoice.run();
        radio.clear();
    }

    /**
     * Whether the mission failed screen offers Retry from boss (design/systems/retry): the attempt
     * reached the boss checkpoint, on a difficulty with boss checkpoints ({@code boss_checkpoint} of
     * design/systems/difficulty: easy and medium).
     */
    boolean bossCheckpoint() {
        return checkpointOffered(services.content.difficulty(), campaign.difficulty(), sortie.bossCheckpoint());
    }

    /** Whether Retry from boss is offered: the checkpoint was reached and the difficulty has them. */
    static boolean checkpointOffered(DifficultyData data, Difficulty difficulty, boolean reached) {
        return reached && data.bossCheckpoint().of(difficulty);
    }

    /** Restarts at the boss checkpoint: the boss's arrival on an empty field, with the defences and tallies of then. */
    void retryFromBoss() {
        sortie.retryFromBoss();
        outro.stop();
        slowMotion = 0;
        failure = Optional.empty();
        radioEvents.clearFailureLine();
    }

    /** Starts the level over from its start state with {@code armour} (a retry, or the pause menu's restart). */
    void retry(double armour) {
        // The flight's Rook: the gear's, or the --escort debug option's (never the gear's).
        double rook = campaign.escortFlight().map(Campaign.EscortFlight::armour).orElse(0.0);
        if (sortie.wingman().isPresent() && rook > 0) {
            // Rook starts over with his level-start armour, raised to the floor like the player's.
            sortie.retry(armour, rook);
        } else {
            sortie.retry(armour);
        }
        outro.stop();
        slowMotion = 0;
        failure = Optional.empty();
        radioEvents.clearFailureLine();
    }

    /** The pointer is hidden while the level is flown, and shows again once it is won (design/ui, Mouse). */
    @Override
    public boolean pointerHidden() {
        return !outro.running();
    }

    @Override
    public Transition update(float seconds) {
        campaign.play(seconds);
        if (services.input.pressed(Action.PAUSE) || services.input.interrupted()) {
            services.voices.pause();
            // M7 part A: the held loops (a beam's hum, the lance's) are silent in the pause; the next
            // step's watch starts them again.
            sounds.stopLoops();
            return Transition.open(new PauseScreen(services, this));
        }
        if (outro.update(seconds, radio)) {
            // A voice still speaking when the outro's cap hands over is cut, not left to play over the
            // debrief (Level 12's level-end line, playtest 2026-10-18).
            services.voices.stop();
            return Transition.replace(debrief());
        }
        if (failure.isPresent()) {
            failedIn -= seconds;
            if (failedIn <= 0) {
                Campaign.Failure what = failure.get();
                failure = Optional.empty();
                failedScreenOpens(radio, services.voices::stop);
                return what == Campaign.Failure.GAME_OVER
                        ? Transition.replace(new GameOverScreen(services, campaign, mission()))
                        : Transition.open(new MissionFailedScreen(services, this));
            }
        }
        Settings settings = services.settings();
        radio.charsPerSecond(settings.gameplay().textSpeed());
        creditNumbers.visible(settings.gameplay().creditNumbers());
        float simSeconds = seconds;
        if (slowMotion > 0) {
            slowMotion -= seconds;
            simSeconds *= SLOW_MOTION_RATE;
        }
        int steps = clock.advance(simSeconds);
        int stepCommands = FlightCommands.of(services.input, settings.controls());
        for (int i = 0; i < steps; i++) {
            airborne.record(sortie);
            sortie.step(stepCommands);
            if (!sortie.launching() && sortie.section() == 1) {
                prompts.update(stepCommands);
            }
            effects.step();
            debris.step();
            pieces.step();
            blasts.step();
            renderer.stepEffects();
            wrecks.step();
            sounds.step();
            creditNumbers.step();
            screenFlash.step();
            chainSounds.step();
            if (shimmer > 0) {
                shimmer--;
            }
            if (barks != null) {
                // M5 part C: his spacing runs on real time, the waves' barks on the level clock.
                barks.clock(sortie.levelSeconds(), sortie.realSeconds(), sortie.scriptRate());
            }
            react(sortie.events());
            closeBannerOverFight();
            militiaFlak();
            renderer.stepped(sortie);
            if (latchPrompt != null) {
                latchPrompt.step(sortie.latched());
            }
            if (barks != null) {
                barks.step(
                        SimStep.ticks(sortie.levelSeconds()), sortie.realSeconds(), armourShare(), overdrivePickups());
            }
            smokeTrails();
            sounds.watch(sortie);
            // M7 part G: the warnings are named by travel (in a reversed section the top is the rear).
            boolean reversed = sortie.reversed();
            int started = warnings.step(sortie.edgeWarnings(), sortie.tick(), reversed);
            sounds.edgeWarnings(started);
            if (barks != null && WarningEdge.TOP.in(started)) {
                // M7 part G: a re-entry from the top in reversed travel (a Wraith, a swarm's loop-back) is a rear
                // wave: "Up top!".
                barks.reentry(SimStep.ticks(sortie.levelSeconds()), SimStep.ticks(sortie.realSeconds()), true);
            } else if (barks != null && WarningEdge.BOTTOM.in(started) && !reversed) {
                // M5 part D: a re-entry at the bottom edge (a Wraith, a swarm's loop-back) is a rear wave;
                // M5 part H: timed on the real clock (the maw's launches in Level 14's halted arena). M7 part G: in
                // reversed travel a bottom warning is a lesson for a wave from ahead: no bark.
                barks.reentry(SimStep.ticks(sortie.levelSeconds()), SimStep.ticks(sortie.realSeconds()));
            }
            waveBanners.step(sortie.edgeWarnings(), started, sortie.tick(), reversed);
        }
        if (launchPending && sortie.launching()) {
            sounds.launch();
            launchPending = false;
        }
        services.voices.resume();
        playRadio(seconds);
        services.voices.update(seconds);
        music.update(
                sortie.section(),
                sortie.levelSeconds(),
                seconds,
                radio.current().isPresent() || services.voices.playing(),
                runTimeFull(level.music(), sortie.holdActive(), sortie.collapseStarted()));
        return Transition.STAY;
    }

    /**
     * M5 part C, the music's run-time hook ({@code full_on}, design/audio/music): whether the full mix
     * plays now, while a hold zone runs or from the collapse on, as the level's music block names them.
     */
    static boolean runTimeFull(LevelData.Music music, boolean holdActive, boolean collapseStarted) {
        return (holdActive && music.fullOn(LevelData.Music.FullOn.HOLD))
                || (collapseStarted && music.fullOn(LevelData.Music.FullOn.COLLAPSE));
    }

    /**
     * M7 part D: the militia's flak over the spaceport (Level 17's {@code flak}): a burst due now puffs on {@code
     * low-air} as the frigate's flak does, with its distant sound; M7 part G: in the top band for Level 20's rim guns.
     */
    private void militiaFlak() {
        if (militiaFlak == null) {
            return;
        }
        int burst = militiaFlak.step(sortie.levelSeconds());
        if (burst < 0) {
            return;
        }
        double x = MilitiaFlak.x(burst);
        double y = MilitiaFlak.y(burst, militiaFlak.band());
        if (flak.size > 0) {
            renderer.lowAir().start(flak, EventEffects.FLAK_FRAME_TICKS, x, y);
        }
        renderer.militiaFlak(x, y, sortie.tick());
        sounds.militiaFlak(x);
    }

    static Array<AtlasRegion> optionalFrames(GameServices services, String name) {
        return services.sprites.has(name) ? services.sprites.frames(name) : new Array<>(0);
    }

    /** The ship's armour as a share of its most, for Rook's bark about it. */
    private double armourShare() {
        var defences = sortie.ship().defences();
        return defences.maxArmour() > 0 ? defences.armour() / defences.maxArmour() : 1;
    }

    /** The overdrive pickups on the field, for Rook's bark when one appears. */
    private int overdrivePickups() {
        int count = 0;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.OVERDRIVE) {
                count++;
            }
        }
        return count;
    }

    /** Queues one of Rook's barks (an event line; his eject bark urgent) with its voice file, if it has one. */
    private RadioQueue.Message bark(Barks.Line line) {
        return radioEvents.queue(
                line.speaker(), line.speaker(), line.expression(), line.text(), false, line.priority());
    }

    /** Every few steps a missile with a smoke trail (the Hornet) leaves a puff where it is. */
    private void smokeTrails() {
        if (sortie.tick() % TRAIL_TICKS != 0) {
            return;
        }
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            Array<AtlasRegion> trail = weaponLooks.trail(shot.mount());
            if (trail != null && weaponLooks.trailUnder(shot.mount())) {
                // M5 part E: a torpedo's bubbles stay on the sea, risen to its surface.
                if (renderer.overWater()) {
                    renderer.bubbles()
                            .startOnGround(
                                    trail,
                                    WATER_FRAME_TICKS,
                                    shot.renderX(1),
                                    shot.renderY(1),
                                    0,
                                    sortie.groundScroll());
                }
            } else if (trail != null) {
                pieces.start(trail, TRAIL_FRAME_TICKS, shot.renderX(1), shot.renderY(1));
            }
        }
    }

    private void react(SimEvents events) {
        sounds.play(events);
        for (int i = 0; i < events.size(); i++) {
            // Each event in turn: its radio lines, its effects, then the screen's own (music, HUD, flow).
            radioEvents.react(events, i);
            eventEffects.react(events, i);
            switch (events.type(i)) {
                case BOSS_DESTROYED -> music.bossDown();
                case BOSS_INCOMING -> {
                    // Round 41 (the 2026-10-19 playtest, Level 14): a boss with a lead time is announced that
                    // long before it arrives, so its warning box shows before the boss is reached.
                    announced.add(events.value(i));
                    warnBoss(events.value(i));
                }
                case BOSS_ARRIVED -> {
                    if (announced.contains(events.value(i))) {
                        // Announced ahead: no second warning, and the box does not stay over the boss.
                        if (bannerPiece == events.value(i)) {
                            banner.close(sortie.tick());
                        }
                    } else {
                        warnBoss(events.value(i));
                    }
                }
                case SHIP_DESTROYED -> {
                    slowMotion = SLOW_MOTION_SECONDS;
                    music.cut();
                    failure = Optional.of(campaign.fail());
                    // The used retry, or a game over's return to the launch state, goes into the
                    // autosave at once (design/systems/retry).
                    services.save(SaveSlots.Slot.AUTOSAVE, campaign);
                    failedIn = FAILED_SCREEN_SECONDS;
                }
                case SORTIE_RESTARTED -> {
                    if (latchPrompt != null) {
                        latchPrompt.reset();
                    }
                    layersHit.clear();
                    effects.clear();
                    debris.clear();
                    pieces.clear();
                    blasts.clear();
                    wrecks.clear();
                    creditNumbers.clear();
                    radio.clear();
                    services.voices.stop();
                    warnings.clear();
                    banner.clear();
                    bannerPiece = -1;
                    // A retry from the boss checkpoint (recorded just before the arrival) has no lead
                    // warning again: the arrival warns.
                    announced.clear();
                    waveBanners.clear();
                    screenFlash.clear();
                    chainSounds.clear();
                    renderer.restart();
                    music.restart();
                    launchPending = true;
                }
                case BOSS_RETRY -> music.bossRetry();
                case SPECIAL_READY -> hud.specialReady();
                case SPECIAL_DENIED -> hud.specialDenied();
                case SHIELD_HIT -> shimmer = SHIMMER_TICKS;
                // M5 part D: the scripted loss's lance ducks the theme (the music block's duck, no sting).
                case SCRIPTED_LOSS ->
                    level.music()
                            .duck()
                            .filter(duck -> duck.on() == LevelData.Music.DuckOn.SCRIPTED_LOSS)
                            .ifPresent(duck -> music.duck(duck.db(), duck.seconds()));
                case PRIMARY_FAILED -> {
                    // As a wreck, without the explosion and the slow motion (design/systems/retry).
                    music.cut();
                    failure = Optional.of(campaign.fail());
                    services.save(SaveSlots.Slot.AUTOSAVE, campaign);
                    failedIn = FAILED_SCREEN_SECONDS;
                }
                // M5 part F: a latch raises the shake-off's event prompt until the attempt's first
                // shake-off; both duck the theme a moment so their sounds cut through the storm.
                case LAMPREY_LATCHED -> {
                    if (latchPrompt != null) {
                        latchPrompt.latched(sortie.realSeconds());
                    }
                    music.duck(FlightSounds.CUE_DUCK_DB, FlightSounds.CUE_DUCK_SECONDS);
                }
                case LAMPREY_SHAKEN -> {
                    if (latchPrompt != null) {
                        latchPrompt.shaken();
                    }
                    music.duck(FlightSounds.CUE_DUCK_DB, FlightSounds.CUE_DUCK_SECONDS);
                }
                case LEVEL_COMPLETE -> {
                    music.fadeOut();
                    outro.start();
                    // The radio plays out the closing lines (the user's playtest of 2026-10-19).
                    radio.outro();
                    if (barks != null) {
                        // The outro's radio is the level-end lines': no bark of Rook's holds them back.
                        barks.levelComplete();
                    }
                }
                default -> {}
            }
        }
    }

    /**
     * A boss's warning (design/ui/hud), at its arrival or (round 41) its lead time before: an act boss's
     * warning track with the klaxon and the banner, a mid-boss's sting.
     */
    private void warnBoss(int piece) {
        if (music.bossArrived()) {
            services.sfx.play(Sfx.KLAXON, KLAXON_VOLUME, 1, 0);
            sortie.setPiece(piece).boss().ifPresent(boss -> {
                banner.show(boss.barName(), sortie.tick());
                bannerPiece = piece;
            });
        } else if (level.music().bossSting().isPresent()) {
            music.sting(Sfx.MINIBOSS_STING);
        }
    }

    /**
     * The warning box never hides a boss that fights (the 2026-10-19 playtest: the Siege Spire fired from
     * under it): once the boss it announces is engaged with a living part under the band, it closes.
     */
    private void closeBannerOverFight() {
        if (bannerPiece >= 0 && banner.showing(sortie.tick()) && fightsUnderBanner(sortie.setPiece(bannerPiece))) {
            banner.close(sortie.tick());
        }
    }

    /** How far a boss part reaches up and down from its centre, for {@link #fightsUnderBanner}. */
    static final double PART_REACH = 32;

    /** Whether {@code piece} is engaged (its phases run, its guns fire) with a living part under the warning band. */
    static boolean fightsUnderBanner(SetPiece piece) {
        if (!piece.engaged() || piece.destroyed()) {
            return false;
        }
        for (int p = 0; p < piece.partCount(); p++) {
            if (!piece.partWrecked(p) && BossBanner.under(piece.partY(p), PART_REACH)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The convoy unit a cue's line names ({@code {ally}}): the first hit's, the first lost's, and (M5
     * part D) for {@code ally-lost} the one lost last; -1 for a cue about none.
     */
    static int cueUnit(CueTrigger trigger, Sortie sortie) {
        return switch (trigger) {
            case FIRST_ALLY_HIT -> sortie.firstAllyHit();
            case FIRST_ALLY_LOST -> sortie.firstAllyLost();
            case ALLY_LOST -> sortie.lastAllyLost();
            // M5 part E: each convoy unit's first hit names the unit hit (Level 11's ships).
            case ALLY_HIT -> sortie.lastAllyHit();
            default -> -1;
        };
    }

    /**
     * Whether a cue's line counts as a scripted line for Rook's barks (their spacing, and a waiting
     * bark withdrawn): his first kill (M5 part B), the first loop-back (M5 part D, his flock line) and
     * the first boss part shot off (M5 part E, Level 11's severed slam arm) and the first latch (M5
     * part F, Level 12).
     */
    static boolean scriptedForBarks(CueTrigger trigger) {
        return trigger == CueTrigger.ESCORT_FIRST_KILL
                || trigger == CueTrigger.FIRST_LOOP_BACK
                // M5 part E: his line on the first slam arm severed (Level 11's calamari).
                || trigger == CueTrigger.BOSS_PART_DESTROYED
                // M5 part F: his line on the first latch (Level 12's passenger).
                || trigger == CueTrigger.FIRST_LATCH;
    }

    private static final float KLAXON_VOLUME = 0.8f;

    /**
     * M7 part A: the radio call a special's press answers with: Hammer Lead's for the Airstrike, Lance
     * Control's for the Orbital Lance; null for a special without one (or none fitted).
     */
    static SpecialsData.Radio specialCall(SpecialSpec.Kind kind, SpecialsData specials) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case AIRSTRIKE -> specials.airstrike().radio();
            case LANCE ->
                specials.orbitalLance() == null ? null : specials.orbitalLance().radio();
            case SMART_BOMB, EMP, OVERCHARGE -> null;
        };
    }

    /** The radio's squelch on open and close, and a soft blip for every other typed character. */
    private void playRadio(float seconds) {
        float untilTimed = sortie.complete()
                ? Float.POSITIVE_INFINITY
                : radioSchedule.untilTimed(
                        sortie.levelSeconds(), sortie.scriptRate(), RadioSchedule.escortFlying(sortie));
        switch (radio.update(seconds, untilTimed)) {
            case OPENED -> {
                services.sfx.play(Sfx.RADIO_OPEN, RADIO_VOLUME, 1, 0);
                // The voice starts with the message; an urgent line cuts the one that plays
                // (design/audio/voice, Playback).
                RadioQueue.Message message = radio.current().orElseThrow();
                if (barks != null) {
                    // A Rook line on the radio, bark or scripted, starts the barks' spacing.
                    barks.opened(message.speaker(), sortie.realSeconds());
                }
                message.voice()
                        .ifPresentOrElse(
                                path -> services.voices.play(new Voices.Voice(path, message.voiceSeconds())),
                                services.voices::stop);
            }
            case CLOSED -> {
                services.sfx.play(Sfx.RADIO_CLOSE, RADIO_VOLUME, 1, 0);
                services.voices.stop();
            }
            case TYPED -> {
                if (++typed % 2 == 0) {
                    services.sfx.play(Sfx.TYPEWRITER, TYPING_VOLUME, 1, 0);
                }
            }
            case NONE -> {}
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // M7 part H: the play field's sideways shake (the ridge pass's tremble) within the screen-shake setting; the
        // HUD
        // drawn after it stays still and its side panels cover the shaken edges.
        float shake = ScreenShake.scaled(
                renderer.shakeX(sortie, sortie.realSeconds()),
                services.settings().gameplay().screenShake());
        if (shake != 0) {
            unshaken.set(batch.getTransformMatrix());
            shaken.set(unshaken).translate(shake, 0, 0);
            batch.setTransformMatrix(shaken);
        }
        renderer.draw(
                batch,
                sortie,
                effects,
                debris,
                pieces,
                blasts,
                wrecks,
                creditNumbers,
                warnings,
                clock.alpha(),
                (float) shimmer / SHIMMER_TICKS,
                services.settings().gameplay().flashReduction(),
                screenFlash);
        if (threatArrows != null && sortie.flying()) {
            threatArrows.draw(batch, sortie, clock.alpha());
        }
        waveBanners.draw(batch, sortie.tick(), clock.alpha());
        if (latchPrompt != null && sortie.flying()) {
            latchPrompt.draw(batch, sortie.ship().renderY(clock.alpha()), sortie.realSeconds());
        }
        banner.draw(batch, sortie.tick(), clock.alpha());
        if (shake != 0) {
            batch.setTransformMatrix(unshaken);
        }
        hud.draw(batch, sortie, radio, visiblePrompts());
    }

    /** M7 part H: the batch's transform without and with the play field's shake (reused, nothing allocated). */
    private final Matrix4 unshaken = new Matrix4();

    private final Matrix4 shaken = new Matrix4();

    /**
     * The control prompts show in the first section, once the launch is over; a level's
     * contextual prompts from their time for their seconds (design/ui/hud, control prompts).
     */
    private List<PromptTexts.Text> visiblePrompts() {
        List<PromptTexts.Text> shown = new ArrayList<>();
        if (!sortie.launching() && sortie.section() == 1) {
            shown.addAll(promptTexts.of(prompts.pending()));
        }
        double t = sortie.levelSeconds();
        for (LevelData.Prompt prompt : level.prompts().orElse(List.of())) {
            if (prompt.requiresSpecial() && !sortie.special().fitted()) {
                continue;
            }
            if (prompt.onEvent()) {
                // M5 part F: an event prompt shows in the play field, below the ship (EventPrompt).
                continue;
            }
            // Done what it says, as the control prompts: an enemy destroyed on its skip layer
            // (Level 02's ground unit, Level 03's Spore Bomber on low-air) makes it leave.
            boolean done = prompt.skipLayer().map(layersHit::contains).orElse(false);
            if (!done && t >= prompt.t() && t < prompt.t() + prompt.seconds()) {
                shown.add(new PromptTexts.Text(prompt.action(), prompt.keys()));
            }
        }
        return shown;
    }

    /** Banks the won level in the campaign and shows its debrief. */
    private DebriefScreen debrief() {
        LevelResult result = sortie.result();
        int launchBalance = campaign.credits();
        boolean newBest = campaign.complete(
                result,
                sortie.ship().defences().armour(),
                sortie.special().used(),
                sortie.special().found(),
                sortie.wingman()
                        .map(rook -> java.util.OptionalDouble.of(rook.armour()))
                        .orElse(java.util.OptionalDouble.empty()));
        if (newBest) {
            // A replay's better grade goes into the save it was started from (design/ui/mission-select).
            campaign.replay()
                    .ifPresent(replay -> MissionSelectScreen.keepGrade(
                            services, replay, result.grade().letter()));
        }
        return new DebriefScreen(
                services,
                campaign,
                result,
                sortie.script().number(),
                name,
                launchBalance,
                newBest,
                !sortie.script().objectives().secondary().none(),
                debriefCount(sortie),
                level.music().debriefJingle());
    }

    /**
     * The secondary's count the debrief's row shows: a naval convoy's hulls afloat ({@code HULLS
     * AFLOAT n / 3}, M5 part E), the weather sondes collected ({@code SONDES n / 3}, M5 part F), the relay's
     * integrity ({@code RELAY n %}, M5 part G), a labelled group's units destroyed ({@code ROOTS n / 8}, M5
     * part H), the tendrils broken ({@code BARRIERS BROKEN n / 9}, M7 part C), the ground allies never hit ({@code
     * DOMES UNTOUCHED n / 5}, M7 part D) or, for a standing secondary, still standing ({@code DOMES STANDING n /
     * 5}, round 41); none else.
     */
    static DebriefScreen.Hulls debriefCount(Sortie sortie) {
        if (sortie.groundAllies()) {
            // M7 part D: the domes never hit of all (Level 17), after the tracker's label; round 41: the domes
            // still standing of all when the secondary is a standing one ("No dome lost").
            String label = sortie.script().objectives().secondary().label();
            boolean standing = sortie.script().objectives().secondary().standing();
            return new DebriefScreen.Hulls(
                    (label.isEmpty() ? "DOMES" : label.toUpperCase(Locale.ROOT)) + " "
                            + (standing ? DebriefScreen.STANDING : DebriefScreen.UNTOUCHED),
                    standing ? sortie.alliesStanding() : sortie.alliesUntouched(),
                    sortie.allyCount());
        }
        if (sortie.navalConvoy()) {
            return new DebriefScreen.Hulls(sortie.alliesAfloat(), sortie.damageableAllies());
        }
        if (sortie.sondes().count() > 0
                && sortie.script().objectives().secondary().sondes()) {
            return new DebriefScreen.Hulls(
                    DebriefScreen.SONDES,
                    sortie.sondes().collected(),
                    sortie.sondes().count());
        }
        if (sortie.script().objectives().secondary().links() && sortie.escapesTotal() > 0) {
            // M7 part C: the gates' tendrils broken of all the level's (Level 16's BARRIERS BROKEN n / 9).
            return new DebriefScreen.Hulls(
                    DebriefScreen.BARRIERS_BROKEN, sortie.escapesDestroyed(), sortie.escapesTotal());
        }
        if (sortie.defending() && sortie.script().objectives().secondary().integrity() > 0) {
            // M5 part G: the relay's integrity left (Level 13's RELAY n %).
            return DebriefScreen.Hulls.share(
                    DebriefScreen.RELAY, percent(sortie.ally(0).hpShare()));
        }
        String label = sortie.script().objectives().secondary().label();
        if (sortie.script().objectives().secondary().byGroups() && !label.isEmpty() && sortie.groupUnits(0) > 0) {
            // M5 part H: a labelled group of units destroyed of all (Level 14's ROOTS n / 8).
            return new DebriefScreen.Hulls(
                    label.toUpperCase(Locale.ROOT), sortie.groupUnitsDestroyed(0), sortie.groupUnits(0));
        }
        return DebriefScreen.Hulls.NONE;
    }

    /**
     * M5 part G: a share as the debrief's whole percent, rounded down so a relay under half never reads
     * 50 % (the secondary's threshold).
     */
    static int percent(double share) {
        return (int) Math.floor(share * 100 + 1e-9);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        music.dispose();
        sounds.stopLoops();
        warnings.dispose();
        banner.dispose();
        if (threatArrows != null) {
            threatArrows.dispose();
        }
        services.sprites.leaveLevel(levelNumber);
    }
}
