package vanguard.game.render;

import static vanguard.game.render.LevelRenderer.crawlingArena;
import static vanguard.game.render.LevelRenderer.pivots;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import vanguard.content.AlliesData;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;

/**
 * RF6: a level's looks, every part's presentation the play field is drawn with: built and wired from the level data
 * and its script, freed, restarted with the level and stepped after each simulation step. {@link DrawPasses} extends
 * it with the pass order, so its passes name the looks directly; a new part adds its looks here and its calls to the
 * pass it is drawn in. {@link LevelRenderer} is the face the screens use.
 */
class LevelLooks {
    final Sprites sprites;

    final EnemyLooks[] looks;
    final WeaponLooks weapons;
    /** What the passes share in the drawn frame: the white flashes' strength and what the beam burns. */
    final PlayFrame play = new PlayFrame();
    /** The ground objects' passes. */
    final GroundPasses ground;
    /** The enemy units' passes. */
    final UnitPasses units;
    /** The looks that follow the sortie's enemy kinds, built once. */
    final KindLooks kinds;
    /** The set pieces' passes (the bosses, the Kraken's and the Siege Spire's among them). */
    final SetPiecePasses setPieces;
    /** The ship's, its shots' and the Airstrike's passes. */
    final PlayerPasses player;
    /** The props' passes: debris, cranes, pickups, sondes, spore mines and bullets. */
    final PropPasses props;

    final BossLooks bossLooks;

    /** Level 05's sleds, lobs and battery outlines. */
    final LunaLooks luna;

    final Backdrop backdrop;
    /** M5 part C: the backdrop's level and real clocks at the render time. */
    final BackdropClock clock = new BackdropClock();
    /** M5 part C: Level 09's collapse (the lean, the drop, its shadow, dust, dust peak and heap); null without one. */
    final CollapseLooks collapse;
    /** The level's road and convoy, if it has them. */
    final ConvoyLooks convoy;
    /** M5 part D: an air escort's units (Level 10's shuttles), if the level has one. */
    final ShuttleLooks shuttles;
    /** M9 part D: a hull escort's pivots (Level 31's tug), null without one; its looks built at the first draw. */
    private final JsonValue tugPivots;

    private TugLooks tug;
    /** M5 part D: a scripted loss's glow and lance (Level 10), if the level has one. */
    final LossLooks loss;
    /** M8 part F: Rook's loss's trails, flashes and spin-down (Level 26), if the level has one. */
    final RookLossLooks rookLoss;

    final FlashShader flash;
    /** M7 part A: the Act 3 arsenal in flight: beams, drones, fields, EMP, lance, overcharge, dash. */
    final ArsenalLooks arsenal;
    /** Level 06's darkness, glows, sweeps and the Smart Bomb's flash and ring. */
    final FarsideLooks farside;

    /** Level 07's lifeboat tow. */
    final TowLooks tows;
    /** M9 part G: the level's story pickup (Level 34's Helix core: in the hold, rising, loose); null without one. */
    final StoryLooks story;
    /** The ship's engine flames, damage smoke and sparks and its shield ring. */
    final ShipLooks shipLooks;
    /** Rook's craft, its flames, smoke, muzzle flash and eject pod (M5 part A). */
    final WingmanLooks wingmanLooks;
    /** M8 part A: the Act 4 systems: harpoons, depth charges, the sonar, the flares, the Bulwark. */
    final DeepWaterLooks deepWater;
    /** M8 part D: the silt the walkers kick up on the sea floor (Level 24). */
    final PlainLooks plain;
    /** The flyers' drop shadows on the ground layer. */
    final Shadows shadows;

    final BitmapFont font;
    /** The Targeting computer's HP bars and weak-point brackets; null without one fitted. */
    TargetingOverlay targeting;
    /** The Salvage scanner's glint on the secrets' objects; null without one fitted. */
    SecretGlints secretGlints;
    /** M5 part E: the {@code sub} pass of a level over water; null over land. */
    final WaterLooks water;
    /** M5 part E: the arena boss (the Harbour Kraken); null without one. */
    final KrakenLooks kraken;
    /** M5 part H: the anchored boss without lanes (the Siege Spire) and its canopy's death; null without it. */
    final SpireLooks spire;
    /** M7 part F: the walking boss (the Revenant Walker), its cone, missiles and wreck; null without it. */
    final WalkerLooks walker;
    /**
     * M7 part F: Level 19's foundry looks (the vents' grates and fire, the gantries and the nests' cradles, the shift
     * office, the ingots and pallets, the crews' rovers); null without them.
     */
    final FoundryLooks foundry;
    /** M8 part G: Level 27's heat plumes (their mouths, tells, eruptions and bends); null without them. */
    final PlumeLooks plumes;
    /** M5 part H: the level's root bursts (Level 14); null without them. */
    final RootBurstLooks rootBursts;
    /**
     * M5 part E: water effects on the surface (bursts of kills and blasts on the water, splashes,
     * ripple trains), alpha-blended, scrolling with the sea, under the ground units.
     */
    final Effects surfaceWater = Effects.solid();
    /** M5 part E: effects under the water, drawn in the {@code sub} pass: under-water bursts. */
    final Effects underWater = Effects.solid();
    /** M5 part E: the torpedoes' bubble trails, over the chop and under the convoy (bubbles rise to the surface). */
    final Effects bubbles = Effects.solid();
    /** M5 part E: solid effects on low-air (the frigate's flak puffs over the convoy). */
    final Effects lowAir = Effects.solid();
    /** Step GFX2: the generated soft disc and ring (strobe halos, flak flashes, the pursuit swarm's glows). */
    final SoftGlow softGlow = new SoftGlow();
    /** Step GFX2: the militia flak's flash cores over their puffs. */
    final FlakFlashes flakFlashes = new FlakFlashes();

    /** M5 part F: the level's weather looks (Level 12's rain, lightning, gusts, hidden glows and eye). */
    final WeatherLooks weather;
    /** M7 part E: the level's dust storm (Level 18's visibility mask, veil and silhouettes); null without one. */
    final StormLooks storm;

    /** M7 part C: the wall touch's sparks and the ship's flash, the stopped shots' sparks at rock. */
    final CanyonLooks canyonLooks;
    /**
     * M7 part C: the canyon's presentation beyond its collision lines (the walls' heights, the islands'
     * looks, the face patches) from the level data ({@link WallProjection.Looks#of}; {@link #canyon}).
     */
    WallProjection.Looks canyon;

    /** M5 part G: a defend primary's ally (Level 13's Nansen Relay); null without one. */
    final RelayLooks relay;
    /** M7 part D: the level's ground allies (Level 17's domes); null without them ({@link #groundAllies}). */
    DomeLooks domes;
    /** M5 part G: the supply drones carrying the drops; null without them. */
    final DroneLooks drones;
    /** M8 part B: the ground vehicles carrying drops (Level 22's ice-crawler); null without them. */
    final IceCrawlerLooks crawlerDrops;
    /** M7 part G: Level 20's travelling ridge on the ground (null for another level). */
    final RidgeLooks ridge;
    /** M7 part H: the breaching boss (the Dust Colossus), its ridge, craters, vents and rocks; null without it. */
    final ColossusLooks colossus;
    /** M8 part H: the coiled boss (the Abyssal Maw), its coil, head, sacs, implants, dive, lunge and death; null without it. */
    final MawLooks maw;
    /**
     * M9 part H: the ringed boss (the Iron Sovereign), its rings, batteries, bays, core, lines and death; null without
     * it.
     */
    final SovereignLooks sovereign;
    /** M9 part H: Level 35's mass-relay flash; null without a relay. */
    final MassRelayLooks massRelay;
    /** M8 part H: the finale's cruisers (Level 28's run); null without them. */
    final CruiserLooks cruisers;
    /** M8 part H: the breakout through the ice ceiling and the orbit's white (Level 28); null in another level. */
    final BreakoutLooks breakout;
    /** M7 part H: the script time a crawling arena ends at (Level 21's t=245), NaN without one ({@link #stepped}). */
    final double crawlEnd;
    /** M5 part G: the arctic setting's snow on high-air; null in another setting. */
    SnowLooks snow;

    final LevelData level;

    /**
     * M8 part C: the under-water grade with its wipe, caustics, headlights and marine snow (Level 23 on); null in a
     * level without an under-water look ({@link UnderwaterGrade}).
     */
    final UnderwaterGrade grade;
    /**
     * M8 part F: a dark level's lights and pools in the grade's light map and its pickups' gold halos (Level 26); null
     * in another level ({@link DarkLight}).
     */
    final DarkLight dark;

    /** M8 part C: the Stormhawk's hydro-kit wakes and refit flash (Rook's: {@link WingmanLooks#hydro}). */
    final HydroLooks shipHydro;
    /** M8 part C: the kills below the waterline (the under-water bursts, shards, husks, the pumps' breaks). */
    final DeepDeaths deepDeaths;
    /** M8 part C: the swimming vehicles carrying drops (Level 23's Thera Deep tender); null without them. */
    final IceCrawlerLooks tenderDrops;
    /** M8 part E: the moored buoys releasing drops (Level 25's survey buoy); null without them. */
    final BuoyLooks buoys;
    /** M9 part A: the Tesla Coil Pod's arcs; null without one fitted (built with the first sortie, {@link #stepped}). */
    ArcLooks arcs;

    private boolean arcsBuilt;
    /** M10 part A, step A3b: the Plasma Arc's chains; null without one fitted (built with the first sortie). */
    ChainArcLooks chainArcs;
    /** M10 part A, step A3b: the told lightning strikes (Level 38); null without them (built with the first sortie). */
    LightningLooks lightning;
    /** M10 part A, step A3b: Io's eruptions (Level 39); null without them (built with the first sortie). */
    EruptionLooks eruptions;
    /**
     * M9 part A: the hex shields' looks; null while their art is not loaded (it is in the Act 5 levels' unit atlases;
     * built with the first sortie).
     */
    EnemyShieldLooks shields;
    /** M10 part A: Act 6's unit mechanics (mirrors, regrowth, regeneration, domes, prey hits, spoofed turrets). */
    final JovianLooks jovian;
    /** M9 part A: the units' {@code laser-line} telegraphs and rails. */
    final LineLooks lines;
    /** M9 part B: the escort swap's looks (Rook hidden in the hold, the Ember's launch, its flight away). */
    final RescueLooks rescue;
    /** M9 part A: the kills in vacuum (the belt's bursts, debris and drifting wrecks). */
    final VacuumDeaths vacuumDeaths;
    /** M9 part A: the level's homing missiles as homing diamonds; null in a level without them ({@link #diamonds}). */
    DiamondLooks diamonds;
    /**
     * @param files the assets, for the pivot files of the cranes and set pieces
     * @param script the level's script, whose set pieces it draws
     * @param levelKey the level's key, {@code <act>/level-NN-<slug>}
     */
    LevelLooks(
            Sprites sprites,
            EnemyLooks[] looks,
            WeaponLooks weapons,
            Files files,
            FlashShader flash,
            BitmapFont font,
            LevelData level,
            LevelScript script,
            String levelKey) {
        this.sprites = sprites;
        this.looks = looks;
        this.weapons = weapons;
        CraneLooks craneLooks =
                new CraneLooks(sprites, level.cranes().isPresent() ? pivots(files, "crane-four") : null);
        SetPieceLooks[] setPieceLooks = script.setPieces().stream()
                .map(spec -> spec.isBoss() ? null : new SetPieceLooks(sprites, spec, pivots(files, spec.slug())))
                .toArray(SetPieceLooks[]::new);
        SetPieceSpec boss = script.setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElse(null);
        JsonValue bossPivots = boss == null ? null : pivots(files, boss.slug());
        bossLooks = new BossLooks(sprites, flash, boss, bossPivots);
        kraken = KrakenLooks.of(sprites, flash, boss, bossPivots);
        String folder = Backdrop.folder(levelKey, level);
        spire = SpireLooks.of(
                sprites,
                flash,
                boss,
                bossPivots,
                sprites.hasBackdrop(folder + "canopy-tear")
                        ? sprites.backdrop(folder + "canopy-tear", SpireLooks.TEAR_TIMES.length)
                        : new Array<>(0),
                sprites.hasBackdrop(folder + "daylight-beams")
                        ? sprites.backdrop(folder + "daylight-beams", 2)
                        : new Array<>(0));
        walker = WalkerLooks.of(sprites, flash, boss, bossPivots);
        colossus = ColossusLooks.of(sprites, flash, boss, bossPivots, folder);
        maw = MawLooks.of(sprites, flash, boss);
        sovereign = SovereignLooks.of(sprites, flash, boss, bossPivots);
        massRelay = MassRelayLooks.of(sprites, script);
        foundry = FoundryLooks.of(sprites, level, script);
        plumes = PlumeLooks.of(sprites, level);
        rootBursts = RootBurstLooks.of(sprites, script);
        List<EnemySpec> launches =
                boss == null ? List.of() : boss.boss().map(BossSpec::spawnKinds).orElse(List.of());
        String sledRun = Backdrop.folder(levelKey, level) + "sled-run";
        luna = new LunaLooks(
                sprites,
                script.hazards().sled().isPresent() && sprites.hasBackdrop(sledRun)
                        ? sprites.backdrop(sledRun, 30)
                        : null,
                flash);
        this.backdrop = new Backdrop(sprites, level, levelKey);
        // M7 part H: a crawling arena (Level 21's) lays the next section's backdrop from the ground at its start.
        int crawling = crawlingArena(script);
        backdrop.crawls(crawling);
        crawlEnd = crawling < 0 ? Double.NaN : script.sections().get(crawling).end();
        canyon = WallProjection.Looks.of(level);
        this.collapse = level.collapse()
                .map(spec -> new CollapseLooks(sprites, spec, backdrop, Backdrop.folder(levelKey, level)))
                .orElse(null);
        this.convoy = new ConvoyLooks(sprites, flash, level, script, levelKey, name -> pivots(files, name));
        this.shuttles = new ShuttleLooks(
                sprites,
                flash,
                script,
                script.objectives()
                        .escort()
                        .map(escort -> pivots(files, escort.ally().slug()))
                        .orElse(null));
        this.tugPivots = script.objectives()
                .escort()
                .filter(escort -> escort.hull().isPresent())
                .map(escort -> pivots(files, TugLooks.SLUG))
                .orElse(null);
        this.loss = new LossLooks(sprites, script);
        this.rookLoss = new RookLossLooks(sprites, files, script.rookLoss());
        this.flash = flash;
        this.farside = new FarsideLooks(sprites, script);
        this.tows = new TowLooks(sprites, Content.levelNumber(levelKey));
        this.story = StoryLooks.of(sprites, level, script.groundObjects()).orElse(null);
        this.shipLooks = new ShipLooks(sprites, files);
        this.wingmanLooks = new WingmanLooks(sprites, files, flash);
        this.shadows = new Shadows();
        this.font = font;
        this.water = script.ground().water() ? new WaterLooks() : null;
        this.weather = new WeatherLooks(sprites, level, levelKey);
        this.storm = StormLooks.of(sprites, level, levelKey);
        this.level = level;
        this.relay = RelayLooks.of(sprites, flash, script, name -> pivots(files, name));
        this.drones = DroneLooks.of(sprites, level);
        this.crawlerDrops = IceCrawlerLooks.of(sprites, level, pivots(files, "ice-crawler"));
        this.ridge = RidgeLooks.of(sprites, level);
        this.snow = SnowLooks.of(level, false, sprites.pixel);
        this.arsenal = new ArsenalLooks(sprites, flash);
        this.canyonLooks = new CanyonLooks(sprites);
        ground = new GroundPasses(sprites, flash, play, weather, script, levelKey, softGlow);
        kinds = new KindLooks(sprites, files);
        units = new UnitPasses(
                looks, flash, play, kinds, farside, luna, weather, shipLooks, surfaceWater, launches, softGlow);
        jovian = new JovianLooks(sprites, softGlow);
        setPieces = new SetPiecePasses(flash, play, font, setPieceLooks, bossLooks, kraken, spire);
        player = new PlayerPasses(sprites, weapons, flash, play, shipLooks, arsenal, canyonLooks, water != null);
        props = new PropPasses(sprites, flash, play, clock, craneLooks, script);
        props.ownStory(story != null);
        deepWater = new DeepWaterLooks(sprites, looks, weapons, water != null);
        plain = new PlainLooks(sprites);
        // M8 part C: the under-water grade, the hydro-kit's effects, the tender, the ice shaft's walls.
        grade = UnderwaterGrade.of(level, sprites.pixel);
        // M8 part F: a dark level's light pools in the grade, the units' glow-only frames.
        dark = grade == null ? null : DarkLight.of(level, kinds, softGlow);
        if (dark != null) {
            grade.dark(dark);
            // M8 part G: Level 27's lit vent field keeps the units' plain looks (only black water draws the glow-only).
            kinds.dark(DarkLight.blackWater(level));
            dark.rookLoss(rookLoss);
        }
        if (grade != null) {
            grade.rookLoss(rookLoss);
        }
        deepDeaths = new DeepDeaths(sprites, kinds);
        vacuumDeaths = new VacuumDeaths(sprites, level);
        lines = new LineLooks(sprites.pixel);
        rescue = new RescueLooks(sprites);
        List<Double> waterlines = level.waterlines();
        shipHydro = new HydroLooks(sprites, "ship", waterlines);
        shipLooks.hydro(shipHydro);
        wingmanLooks.hydro(new HydroLooks(sprites, "rook", waterlines));
        tenderDrops = IceCrawlerLooks.of(sprites, level, pivots(files, "thera-tender"), true);
        // M8 part E: the survey buoy at the narrows' end; the Nautili leaving through the trench's rock.
        buoys = BuoyLooks.of(sprites, level);
        // M8 part H: the finale's cruisers (their flak drawn by their own markers) and the breakout.
        cruisers = CruiserLooks.of(sprites, script, pivots(files, CruiserLooks.SLUG));
        if (cruisers != null) {
            luna.skipLobs(cruisers::flakLob);
        }
        breakout = BreakoutLooks.of(sprites, level);
        canyonLooks.ice(sprites.hasBackdrop(folder + Backdrop.ICE_WALL));
        if (!waterlines.isEmpty()) {
            backdrop.wetFrom(
                    UnderwaterGrade.waterlineRow(level, waterlines.getFirst()) - vanguard.sim.PlayField.HEIGHT);
        }
    }

    /**
     * M5 part G: the difficulty's presentation: on {@code easy} a peak with an easy length (Level 13's
     * whiteout, 6 s) in the backdrop and the snow; M7 part E: the dust storm's radii (Level 18).
     */
    void difficulty(Difficulty difficulty) {
        boolean easy = difficulty == Difficulty.EASY;
        // M10 part A: and the foreground banks easy leaves out.
        backdrop.difficulty(difficulty);
        snow = SnowLooks.of(level, easy, sprites.pixel);
        if (storm != null) {
            storm.difficulty(difficulty);
        }
    }

    /**
     * M7 part D: the level's ground allies' looks (Level 17's domes), each in its sprite set: its own {@code sprite}
     * in the level's {@code allies} block, else its ally's ({@code allies}); none without them.
     */
    void groundAllies(LevelScript script, AlliesData allies) {
        domes = DomeLooks.of(sprites, script, DomeLooks.names(level, allies));
    }

    /** M9 part A: whether the level's homing missiles are homing diamonds ({@link DiamondLooks#diamonds}). */
    void diamonds(boolean diamondMissiles) {
        diamonds = DiamondLooks.of(sprites, diamondMissiles);
    }

    /**
     * The fitted utility modules' marks: the Targeting computer's (over the units, under the
     * bullets) and the Salvage scanner's glint (over the ground objects, cranes and tows, in the
     * dark as well); null for a module that is not fitted.
     */
    void modules(TargetingOverlay targetingOverlay, SecretGlints glints) {
        targeting = targetingOverlay;
        secretGlints = glints;
    }

    /** Frees the light map and the generated textures. */
    void dispose() {
        softGlow.dispose();
        backdrop.dispose();
        jovian.dispose();
        farside.dispose();
        if (grade != null) {
            grade.dispose();
        }
        if (dark != null) {
            dark.dispose();
        }
        if (storm != null) {
            storm.dispose();
        }
        shadows.dispose();
        if (water != null) {
            water.dispose();
        }
        if (bossLooks.hull != null) {
            bossLooks.hull.dispose();
        }
    }

    /** The level restarts: the boss's parts forget their opening animations. */
    void restart() {
        clock.reset();
        shipLooks.reset();
        shuttles.reset();
        wingmanLooks.reset();
        if (bossLooks.hull != null) {
            bossLooks.hull.reset();
        }
        if (kraken != null) {
            kraken.reset();
        }
        if (spire != null) {
            spire.reset();
        }
        if (walker != null) {
            walker.reset();
        }
        if (colossus != null) {
            colossus.reset();
        }
        if (maw != null) {
            maw.reset();
        }
        if (sovereign != null) {
            sovereign.reset();
        }
        if (massRelay != null) {
            massRelay.reset();
        }
        if (cruisers != null) {
            cruisers.reset();
        }
        if (breakout != null) {
            breakout.reset();
        }
        backdrop.crawlPending();
        if (plumes != null) {
            plumes.restart();
        }
        if (foundry != null) {
            foundry.restart();
        }
        units.restart();
        convoy.reset();
        if (relay != null) {
            relay.reset();
        }
        surfaceWater.clear();
        underWater.clear();
        bubbles.clear();
        lowAir.clear();
        flakFlashes.clear();
        arsenal.restart();
        if (arcs != null) {
            arcs.restart();
        }
        if (chainArcs != null) {
            chainArcs.restart();
        }
        if (lightning != null) {
            lightning.restart();
        }
        if (eruptions != null) {
            eruptions.restart();
        }
        if (shields != null) {
            shields.restart();
        }
        jovian.restart();
        vacuumDeaths.restart();
        canyonLooks.restart();
        kinds.restart();
        if (story != null) {
            story.restart();
        }
        deepWater.restart();
        plain.clear();
        props.restartBullets();
    }

    /**
     * M7 part A: after each simulation step, the Act 3 arsenal's looks follow its events (catches,
     * drone breaks, dashes, scorch marks; {@link ArsenalLooks}).
     */
    void stepped(Sortie sortie) {
        if (!arcsBuilt) {
            arcs = ArcLooks.of(sprites, sortie.armament());
            chainArcs = ChainArcLooks.of(sprites, sortie.armament());
            lightning = LightningLooks.of(sprites, sortie);
            eruptions = EruptionLooks.of(softGlow, sortie);
            shields = EnemyShieldLooks.of(sprites, looks);
            arcsBuilt = true;
        }
        if (shields != null) {
            shields.stepped(sortie);
        }
        if (arcs != null) {
            arcs.stepped(sortie);
        }
        if (chainArcs != null) {
            chainArcs.stepped(sortie);
        }
        jovian.stepped(sortie);
        arsenal.stepped(sortie);
        deepWater.stepped(sortie);
        kinds.burrowers(sortie).stepped(sortie);
        units.stepped(sortie);
        canyonLooks.stepped(sortie);
        kinds.crawlers(sortie).stepped(sortie);
        AuraLooks auras = kinds.aura(sortie);
        if (auras != null) {
            auras.stepped(sortie);
        }
        kinds.shellbacks(sortie).stepped(sortie);
        kinds.vortices(sortie).stepped(sortie);
        kinds.ghostDrones(sortie).stepped(sortie);
        // M9 part B: Level 29's units' gun clocks, the Talons' wings, the opened clamps, the rocks shot apart.
        kinds.ceres(sortie).stepped(sortie);
        // M9 part C: the line mines' bursts; the Gunships' gun clocks, launches and plate sparks, the trains' splits.
        kinds.mines(sortie).stepped(sortie);
        kinds.minefield(sortie).stepped(sortie);
        // M9 part D: the Hornets' launches, the links' snaps, the dashes' bounces, the shards and blades.
        kinds.rig(sortie).stepped(sortie);
        // M9 part E: the towers' heads and telegraphs, the Striders' torsos, guns' clocks and knees, the remains,
        // bursts.
        kinds.deepCore(sortie).stepped(sortie);
        // M9 part F: the Rail Bunkers' turrets and rails, the prey Talons' tumble, Claim 114 shot open.
        kinds.leechField(sortie).stepped(sortie);
        if (story != null) {
            // M9 part G: the story pickup's group's places on the ground (the crate in the hold at their centre).
            story.stepped(sortie);
        }
        if (walker != null) {
            walker.stepped(sortie);
        }
        if (colossus != null) {
            colossus.stepped(sortie);
        }
        if (maw != null) {
            maw.stepped(sortie);
        }
        if (sovereign != null) {
            sovereign.stepped(sortie);
        }
        if (massRelay != null) {
            massRelay.stepped(sortie);
        }
        if (cruisers != null) {
            cruisers.stepped(sortie);
        }
        if (breakout != null) {
            breakout.stepped(sortie);
        }
        if (!Double.isNaN(crawlEnd)) {
            // M7 part H: the section after the crawling arena starts once the clock passes its end (the kill's jump or
            // the hold's end): its tiles from the next seam above the top edge, its pieces shifted by the crawl.
            boolean started = sortie.levelSeconds() >= crawlEnd;
            if (started && !backdrop.crawlEnded()) {
                backdrop.crawlEnded(sortie.groundScroll());
            } else if (!started && backdrop.crawlEnded()) {
                backdrop.crawlPending();
            }
        }
        if (plumes != null) {
            plumes.stepped(sortie);
        }
        if (foundry != null) {
            foundry.stepped(sortie);
        }
        kinds.tanks(sortie).stepped(sortie);
        kinds.rigs(sortie).stepped(sortie);
        // M8 part C: the eels' tells and volleys, the Glassfins' glints.
        kinds.eels(sortie).stepped(sortie);
        kinds.glassfins(sortie).stepped(sortie);
        // M8 part D: the Rays' fans, the Barbfins' needles (their tells), the bullets' shooters, the walkers' silt.
        kinds.rays(sortie).stepped(sortie);
        kinds.barbfins(sortie).stepped(sortie);
        // M8 part E: the rings' rock, the Nautili's roll and glances.
        kinds.sirens(sortie).stepped(sortie);
        kinds.nautili(sortie).stepped(sortie);
        // M8 part F: the Anglers' triggers, rings and deaths.
        kinds.anglers(sortie).stepped(sortie);
        // M8 part G: the Hunters' rises and launches, the torpedoes' wakes.
        kinds.hunters(sortie).stepped(sortie);
        props.steppedBullets(sortie);
        plain.stepped(sortie, looks);
    }

    /** M5 part E: the renderer's own effects advance one simulation step (with the screen's). */
    void stepEffects() {
        surfaceWater.step();
        underWater.step();
        bubbles.step();
        lowAir.step();
        deepWater.stepEffects();
        plain.step();
    }

    /** M9 part D: the hull escort's tug looks ({@link TugLooks}), built at the first draw (they read its sections). */
    TugLooks tug(Sortie sortie) {
        if (tug == null) {
            tug = new TugLooks(sprites, sortie, tugPivots);
        }
        return tug;
    }
}
