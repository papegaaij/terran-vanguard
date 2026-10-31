package vanguard.game.screen;

import com.badlogic.gdx.Gdx;
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
import vanguard.content.HazardsBlock;
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
import vanguard.game.level.StaticLeads;
import vanguard.game.render.AirborneWalkers;
import vanguard.game.render.AtmosphereRamp;
import vanguard.game.render.BossBanner;
import vanguard.game.render.BreachLine;
import vanguard.game.render.CreditNumbers;
import vanguard.game.render.DroneLooks;
import vanguard.game.render.EdgeWarnings;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.EventPrompt;
import vanguard.game.render.FriendlyBanner;
import vanguard.game.render.FriendlyMarks;
import vanguard.game.render.Hud;
import vanguard.game.render.IceCrawlerLooks;
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
import vanguard.game.render.WaterlineTell;
import vanguard.game.render.WaveBanners;
import vanguard.game.render.WeaponLooks;
import vanguard.game.settings.Settings;
import vanguard.sim.CueTrigger;
import vanguard.sim.FixedStepClock;
import vanguard.sim.HazardSpec;
import vanguard.sim.Layer;
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
    /** Step MARK70: "FRIENDLIES INBOUND — HOLD FIRE" before a wave of neutral craft, in its own slot under it. */
    private final FriendlyBanner friendlyBanner;
    /** Step MARK70: the IFF brackets and FRIENDLY tags of the neutral craft on the screen. */
    private final FriendlyMarks friendlyMarks;
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
    /**
     * M8 part C: the rules strip at a waterline into the water (Level 23: {@code UNDER WATER · BULLETS ×0.7 · DRAG ·
     * TORPEDOES +50 %}, 4 s below the ship); null in a level that never goes under water.
     */
    private final EventPrompt rulesStrip;
    /**
     * M9 part G (design/campaign Level 34, The freighter): the story pickup's line in the play field once collected
     * ({@code HELIX CORE SALVAGED}, the event prompt's look); null in a level without one.
     */
    private final EventPrompt storyLine;
    /** M8 part C: the medium's edge for the waterline's tell (the strip, the weapon rows' flash). */
    private final WaterlineTell waterlineTell = new WaterlineTell();
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
        long loadStart = System.nanoTime();
        // The level's own sprites (its unit atlas) and backdrop images until the screen closes.
        services.sprites.enterLevel(levelNumber);
        long atlasesLoaded = System.nanoTime();
        // Every sound effect ready before the level's first tick (startup step: they decode in the background).
        services.sfx.finish();
        long soundsReady = System.nanoTime();
        Rules rules = SimSpecs.rules(services.content, levelKey, difficulty);
        campaign.launch();
        flight = Flight.of(services.content, services.catalogue, campaign);
        LevelScript script = SimSpecs.level(services.content, levelKey, difficulty);
        if (services.underwaterFrom.isPresent()) {
            // M8 part A, the debug option --medium underwater[@<s>]: under water from that level second on.
            script = script.toBuilder()
                    .medium(SimSpecs.underwaterFrom(services.content, services.underwaterFrom.getAsDouble()))
                    .build();
        }
        sortie = new Sortie(
                SEED,
                flight.loadout(),
                script,
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
        staticLeads = new StaticLeads(level.radio());
        wrecks = new SetPieceWrecks(sortie.setPieceCount());
        // M9 part G: a unit in its livery from the livery's act on (the Warden Tank in Ascendancy colours, Act 5).
        int act = EnemyLooks.act(levelKey);
        looks = EnemyLooks.of(sortie.enemyKinds(), services.sprites, services.content, act);
        weaponLooks = new WeaponLooks(
                sortie.armament(),
                flight.weapons().stream().mapToInt(Flight.Weapon::level).toArray(),
                services.sprites,
                new PodPivots(services.files),
                escortGun(sortie));
        flak = optionalFrames(services, "escort-frigate-flak");
        sounds = new FlightSounds(
                services.sfx,
                looks,
                sortie.armament(),
                sortie.script().setPieces().stream().map(SetPieceSpec::slug).toList(),
                escortGun(sortie));
        // M5 part E: Level 11's sounds: ships on the convoy, the Kraken's slam cycle for its sound's timing.
        sounds.naval(sortie.navalConvoy());
        // M5 part G: Level 13's relay sounds and its supply drones' launches.
        sounds.defend(
                sortie.defending(),
                level.released().stream()
                        .filter(drop -> drop.sprite().isPresent())
                        // M8 part B: a ground vehicle's drop (Level 22's ice-crawler) has no rotor launch.
                        .filter(drop -> !IceCrawlerLooks.carried(drop))
                        .mapToDouble(LevelData.Drop::t)
                        .toArray(),
                DroneLooks.ARC_SECONDS);
        // M8 part B: Level 22's ice geysers sound as geysers, not furnace vents.
        sounds.geysers(level.vents()
                .map(vents -> vents.lookOrDefault().equals(HazardsBlock.Vents.ICE_GEYSER))
                .orElse(false));
        // M8 part G: Level 27's heat plumes rumble and roar as plumes; Varga's scan pings before the wreck.
        sounds.plumes(level.vents()
                .map(vents -> vents.lookOrDefault().equals(HazardsBlock.Vents.HEAT_PLUME))
                .orElse(false));
        sounds.scan(level);
        // Step I2: Level 28's breakout sounds as the ship breaches the ice, with the burst.
        sounds.breach(BreachLine.of(level));
        // M7 part D: Level 17's domes: glass hits and breaks, the bombers' drops.
        sounds.ground(sortie.groundAllies());
        // M9 part D4b: a rock tow (Level 31's claim rock) breaks with a rock's crumble, not a cable's snap.
        vanguard.game.render.LevelRenderer.towLook(levelNumber).ifPresent(sounds::towLook);
        // M5 part H: Level 14's canopy withers at the Siege Spire's death.
        sounds.canopy(Sfx.afterBoss(Sfx.ambience(level.music().ambience())) != null);
        // M7 part H: Level 21's ridge pass rumbles (the Colossus's back under the dunes); Level 20's ridge has its bed.
        if (breachingBoss(sortie.script())) {
            level.ridge().ifPresent(ridge -> sounds.ridgePass(ridge.t()));
        }
        // M8 part D: Level 24's relay beacons ping with their pulse while one is on the screen.
        sounds.beacons(level);
        if (vanguard.game.render.LevelRenderer.darkLevel(level)) {
            // M8 part F: the dark's sounds (the crown hum, the headlights' clicks at the start and the lamps' out).
            sounds.dark(lampsOut(level));
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
        renderer.liveries(slug -> EnemyLooks.root(services.content, slug, act, services.sprites));
        // M5 part G: easy's shorter whiteout (Level 13), in the backdrop and the snow; M7 part E: the storm's radii.
        renderer.difficulty(difficulty);
        // M7 part D: Level 17's domes, each in its sprite set.
        renderer.groundAllies(sortie.script(), services.content.allies());
        // M9 part A: the Ascendancy's homing missiles are drawn and heard as homing diamonds.
        boolean diamonds = vanguard.game.render.DiamondLooks.diamonds(services.content, sortie.enemyKinds());
        renderer.diamonds(diamonds);
        sounds.diamonds(diamonds);
        militiaFlak = level.flak().map(MilitiaFlak::new).orElse(null);
        warnings = new EdgeWarnings(services.sprites.pixel, services.fonts.body);
        banner = new BossBanner(services.sprites.pixel, services.fonts.heading, services.fonts.body);
        waveBanners = new WaveBanners(services.sprites.pixel, services.fonts.body, sortie.airEscort());
        friendlyBanner = new FriendlyBanner(services.sprites.pixel, services.fonts.body, sortie.airEscort());
        friendlyMarks = new FriendlyMarks(services.sprites.pixel, services.fonts.label);
        latchPrompt = level.prompts().orElse(List.of()).stream()
                .filter(LevelData.Prompt::onEvent)
                .findFirst()
                .map(prompt -> new EventPrompt(
                        services.sprites.pixel, services.fonts.body, prompt.action(), prompt.keys(), prompt.seconds()))
                .orElse(null);
        rulesStrip = level.underwater()
                ? new EventPrompt(
                        services.sprites.pixel,
                        services.fonts.label,
                        WaterlineTell.ACTION,
                        WaterlineTell.rules(
                                services.content.europa().underwater(),
                                services.fonts.label.getData().hasGlyph('\u00d7') ? "\u00d7" : "x"),
                        WaterlineTell.SECONDS)
                : null;
        storyLine = level.pickups().stream()
                .flatMap(pickup -> pickup.story().stream())
                .findFirst()
                .map(story -> new EventPrompt(
                        services.sprites.pixel,
                        services.fonts.body,
                        storyName(story),
                        STORY_SALVAGED,
                        STORY_LINE_SECONDS))
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
        // M8 part F: the timed stem changes (Level 26's dark stem, the silence, the warm pad).
        music.stemTimes(level.music().stemTimeList(), stem -> services.files.internal(Tracks.stemPath(theme, stem)));
        // M8 part H: the run under the theme's motif alone after the act boss's kill (Level 28).
        music.motifAfterBoss(level.music().motifAfterBoss().orElse(null));
        Gdx.app.log(
                "level",
                String.format(
                        Locale.ROOT,
                        "Level %02d loaded in %.2f s (atlases %.2f, sound effects %.2f)",
                        levelNumber,
                        (System.nanoTime() - loadStart) / 1e9,
                        (atlasesLoaded - loadStart) / 1e9,
                        (soundsReady - atlasesLoaded) / 1e9));
    }

    /**
     * M8 part H (design/enemies/bosses/abyssal-maw, Music): whether a coiled boss is in its dive's darkness now, from
     * its slide into the dark through the lunge (the boss music drops away under it and comes back after).
     */
    public static boolean coilDiving(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            var coil = sortie.setPiece(k).coil();
            if (coil.isPresent()) {
                switch (coil.get().diveState()) {
                    case SLIDE, QUIET, SHADOW, LUNGE -> {
                        return true;
                    }
                    default -> {}
                }
            }
        }
        return false;
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

    /** M9 part G: the story pickup's line's second word: {@code HELIX CORE SALVAGED}. */
    static final String STORY_SALVAGED = "SALVAGED";
    /** M9 part G: how long the story pickup's line shows, s. */
    static final double STORY_LINE_SECONDS = 3;

    /** M9 part G: a story's name on the HUD: {@code helix-core} reads {@code HELIX CORE}. */
    static String storyName(String story) {
        return story.replace('-', ' ').toUpperCase(Locale.ROOT);
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

    /** RF6 ({@code --shots}): the simulation's tick in this attempt. */
    long tick() {
        return sortie.tick();
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

    /**
     * M8 part A: the gun flying in the escort slot, whose shots carry the escort's mount: Rook's, or the Bulwark's
     * cannon; empty without an escort.
     */
    static java.util.Optional<vanguard.sim.WeaponSpec> escortGun(Sortie sortie) {
        return sortie.wingman().map(Wingman::gun).or(() -> sortie.bulwark().map(vanguard.sim.Bulwark::gun));
    }

    /** Starts the level over from its start state with {@code armour} (a retry, or the pause menu's restart). */
    void retry(double armour) {
        // The flight's Rook: the gear's, or the --escort debug option's (never the gear's).
        double rook = campaign.escortFlight().map(Campaign.EscortFlight::armour).orElse(0.0);
        if ((sortie.wingman().isPresent() || sortie.bulwark().isPresent()) && rook > 0) {
            // Rook starts over with his level-start armour, raised to the floor like the player's; M8 part A: the
            // Bulwark the same.
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
            return Transition.replace(DebriefTally.debrief(services, campaign, sortie, level, name));
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
        // M8 part F: the Dark levels assist (a dark level's ambient at 20 %).
        renderer.darkAssist(settings.gameplay().darkAssist());
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
                // M9 part B (round 62): the rescued Rook barks only once he is in his slot (Level 29).
                barks.inSlot(rookInSlot(sortie));
            }
            react(sortie.events());
            closeBannerOverFight();
            militiaFlak();
            renderer.stepped(sortie);
            if (latchPrompt != null) {
                latchPrompt.step(sortie.latched());
            }
            if (waterlineTell.step(sortie.underwater()) && rulesStrip != null && sortie.flying()) {
                // M8 part C: the waterline's tell: the rules strip and the weapon rows' flash.
                rulesStrip.flash(sortie.realSeconds());
                hud.waterline();
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
            // 2026-10-21 (user): a unit's own re-entry (a Wraith's pop-up, a loop-back) and a boss warn no more, so a
            // warning here is a wave's first entry; one no rear wave's bark covers (a cross unit's) gets Rook's bark.
            if (barks != null && WarningEdge.TOP.in(started)) {
                // M7 part G: a wave from the top in reversed travel is a rear wave: "Up top!".
                barks.reentry(SimStep.ticks(sortie.levelSeconds()), SimStep.ticks(sortie.realSeconds()), true);
            } else if (barks != null && WarningEdge.BOTTOM.in(started) && !reversed) {
                // M5 part H: timed on the real clock. M7 part G: in reversed travel a bottom warning is a lesson for a
                // wave from ahead: no bark.
                barks.reentry(SimStep.ticks(sortie.levelSeconds()), SimStep.ticks(sortie.realSeconds()));
            }
            waveBanners.step(sortie.edgeWarnings(), started, sortie.tick(), reversed);
            friendlyBanner.step(sortie.friendliesInbound(), sortie.tick());
        }
        if (launchPending && sortie.launching()) {
            sounds.launch();
            launchPending = false;
        }
        services.voices.resume();
        playRadio(seconds);
        services.voices.update(seconds);
        // M8 part F: a line with its own duck (Level 26's Choir); the portrait's static ahead of its line, its sound.
        music.radioDuck(radioEvents.radioDuckDb());
        staticLead();
        music.bossDrop(coilDiving(sortie));
        music.update(
                sortie.section(),
                sortie.levelSeconds(),
                seconds,
                radio.current().isPresent() || services.voices.playing(),
                runTimeFull(level.music(), sortie.holdActive(), sortie.collapseStarted()));
        return Transition.STAY;
    }

    /** M8 part F: the level time the last lamps go out (its under-water look's ambient ramp's end); NaN for none. */
    static double lampsOut(LevelData level) {
        for (int i = 0; i < level.sections().size(); i++) {
            var ramp = level.underwaterLookOf(i).flatMap(look -> look.ramp());
            if (ramp.isPresent()) {
                return ramp.get().until();
            }
        }
        return Double.NaN;
    }

    /**
     * M8 part F (design/campaign Level 26, The Choir): the HUD portrait's interference ahead of a line with a {@code
     * static_lead}, and its sound (the Choir's static) as it starts; replayed by a retry (the level clock).
     */
    private void staticLead() {
        if (!staticLeads.any()) {
            return;
        }
        double now = sortie.levelSeconds();
        if (staticLeads.started(leadSeconds, now) && !sortie.complete()) {
            services.sfx.play(Sfx.CHOIR_STATIC, CHOIR_STATIC_VOLUME, 1, 0);
        }
        leadSeconds = now;
        hud.staticLead(staticLeads.strength(now));
    }

    /** M8 part F: the Choir's static plays at its file's level (it is levelled under the voices). */
    static final float CHOIR_STATIC_VOLUME = 1f;

    /** M8 part F: the level's static leads ({@code static_lead}) and the level time at the last frame. */
    private final StaticLeads staticLeads;

    private double leadSeconds;

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
                // M8 part H: the reveal sting (track 30) at the cruisers' rise (Level 28).
                case CRUISER_RISE -> music.reveal();
                // M9 part B: Rook's return sting (track 31) as the rescued Ember launches from the tender's hold.
                case ESCORT_SWAP -> {
                    if (events.value(i) == vanguard.sim.EscortSwap.JOINS) {
                        music.rookReturn();
                    }
                }
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
                case STORY_COLLECTED -> {
                    if (storyLine != null) {
                        storyLine.flash(sortie.realSeconds());
                    }
                }
                case SORTIE_RESTARTED -> {
                    if (latchPrompt != null) {
                        latchPrompt.reset();
                    }
                    if (storyLine != null) {
                        storyLine.reset();
                    }
                    if (rulesStrip != null) {
                        rulesStrip.reset();
                    }
                    waterlineTell.reset();
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
                    friendlyBanner.clear();
                    screenFlash.clear();
                    chainSounds.clear();
                    renderer.restart();
                    music.restart();
                    launchPending = true;
                }
                case BOSS_RETRY -> music.bossRetry();
                case SPECIAL_READY -> hud.specialReady();
                case SPECIAL_DENIED -> hud.specialDenied();
                // M8 part A: the special's row changed state at a waterline: it flashes once (its chime: FlightSounds).
                case SPECIAL_LIMIT -> hud.specialLimit();
                case SHIELD_HIT -> {
                    shimmer = SHIMMER_TICKS;
                    renderer.shieldReflex(false);
                }
                // M10 part A, step A3b: a hit the Reflex Shield turned into a bolt (after its SHIELD_HIT): the ring and
                // the hull's shimmer gold.
                case REFLEX -> renderer.shieldReflex(true);
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

    /**
     * Whether {@code piece} is engaged (its phases run, its guns fire) with a living part under the warning band; M9 part
     * H: a ringed boss (the Iron Sovereign) fights once its rings are up, not while they spin up (nothing fires then).
     */
    static boolean fightsUnderBanner(SetPiece piece) {
        if (!piece.engaged()
                || piece.destroyed()
                || piece.ring().map(vanguard.sim.RingArena::spinningUp).orElse(false)) {
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
     * M9 part B (Level 29, round 62): whether Rook flies in his slot, so his barks run: not while the rescued Rook waits
     * in the tender's hold or flies into the slot ({@link Barks#inSlot}); always without an escort swap.
     */
    static boolean rookInSlot(Sortie sortie) {
        return sortie.wingman().map(rook -> !rook.waiting() && !rook.joining()).orElse(true);
    }

    /**
     * Whether a cue's line counts as a scripted line for Rook's barks (their spacing, and a waiting
     * bark withdrawn): his first kill (M5 part B), the first loop-back (M5 part D, his flock line) and
     * the first boss part shot off (M5 part E, Level 11's severed slam arm) and the first latch (M5
     * part F, Level 12).
     */
    static boolean scriptedForBarks(CueTrigger trigger) {
        return trigger == CueTrigger.ESCORT_FIRST_KILL
                // M9 part B (round 62): the rescued Rook's first line as he joins his slot (Level 29's "Miss me?").
                || trigger == CueTrigger.ESCORT_JOINED
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
            // M8 part A (step A6, decided): the Sonar Pulse and the Decoy Flares are the ship's own, no call: the
            // ping and the flares' launch answer the press.
            case SMART_BOMB, EMP, OVERCHARGE, SONAR, FLARES -> null;
        };
    }

    /** M9 part A: whether the line on the radio came over the enemy channel (its close is the intercept's). */
    private boolean openIntercepted;

    /** The radio's squelch on open and close, and a soft blip for every other typed character. */
    private void playRadio(float seconds) {
        float untilTimed = sortie.complete()
                ? Float.POSITIVE_INFINITY
                : radioSchedule.untilTimed(
                        sortie.levelSeconds(), sortie.scriptRate(), RadioSchedule.escortFlying(sortie));
        switch (radio.update(seconds, untilTimed)) {
            case OPENED -> {
                // The voice starts with the message; an urgent line cuts the one that plays
                // (design/audio/voice, Playback).
                RadioQueue.Message message = radio.current().orElseThrow();
                // M9 part A: an enemy-channel line opens with the intercept's static sweep instead of the squelch.
                openIntercepted = message.intercepted();
                services.sfx.play(openIntercepted ? Sfx.INTERCEPT_OPEN : Sfx.RADIO_OPEN, RADIO_VOLUME, 1, 0);
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
                services.sfx.play(openIntercepted ? Sfx.INTERCEPT_CLOSE : Sfx.RADIO_CLOSE, RADIO_VOLUME, 1, 0);
                openIntercepted = false;
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
        // M9 part C: a lane buoy's strobe flashing as it passes ticks faintly.
        double strobe = renderer.strobed();
        if (!Double.isNaN(strobe) && sortie.flying() && !sortie.complete()) {
            sounds.buoyTick(strobe);
        }
        if (threatArrows != null && sortie.flying()) {
            threatArrows.draw(batch, sortie, clock.alpha());
        }
        friendlyMarks.draw(batch, sortie, clock.alpha());
        waveBanners.draw(batch, sortie.tick(), clock.alpha());
        friendlyBanner.draw(batch, sortie.tick(), clock.alpha());
        if (latchPrompt != null && sortie.flying()) {
            latchPrompt.draw(batch, sortie.ship().renderY(clock.alpha()), sortie.realSeconds());
        }
        if (rulesStrip != null && sortie.flying()) {
            rulesStrip.draw(batch, sortie.ship().renderY(clock.alpha()), sortie.realSeconds());
        }
        if (storyLine != null && sortie.flying()) {
            storyLine.draw(batch, sortie.ship().renderY(clock.alpha()), sortie.realSeconds());
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
