package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import vanguard.content.AlliesData;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.BossSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Ship;
import vanguard.sim.ShipSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.Wingman;

/**
 * Draws a level back to front, interpolating every position between the last two simulation
 * steps: the backdrop down to the ground layer, the ground objects and the convoy, the overhead
 * ground pieces (a bridge's arches, a gate's roof: the convoy passes under them), the debris and
 * the ground units (a turret stands on an arch) with their glints, the flyers' drop shadows on the
 * ground layer (its tiles, road and pieces mark where they may fall, see {@link Shadows}),
 * the low-air flyers, the low-air layer's banks (so a low flyer can sit inside them), a hull
 * boss's shadow while it flies above the play plane, a lifeboat tow (friendly, under the flyers), the flyers
 * and a set piece on the play plane, a set piece breaking up at its death, the solid death pieces of air units (tatters, husks), the debris chunks, the cranes, the pickups, the solid rounds
 * (missiles, bombs, shells), Rook's craft beside it (or his drifting eject pod, {@link WingmanLooks}), the ship with its engine flames, damage smoke and sparks, wing pods and
 * shield ring ({@link ShipLooks}), the glowing shots, muzzle flashes and
 * effects, a set piece on high-air (above the ship, at the high-air scale), the units a boss off the
 * play plane has launched (they leave its sacs downward, so they show over its hull), the high-air layer,
 * then the spore mines and the enemy bullets (small or large orbs, {@link BulletLooks}) above every layer (design/enemies, bullet readability
 * rules), an act boss's death flash, the edge warnings and the credit numbers.
 *
 * <p>M5 part E, a level over water: between the deep layer and the ground's tiles the {@code sub}
 * layer through its {@link WaterLooks} pass (the Kraken's parts under the water or its foreshadowing,
 * the sunken triggers, the naval units' plain bodies, the torpedoes and the under-water effects);
 * over the ground's pieces the arena boss ({@link KrakenLooks}: its lanes' churn, its grip, head and
 * arms); the naval units' surface layers ({@link NavalLooks}) with the ground units; the surface's
 * water effects (bursts, splashes, ripple trains) with the debris; the lanes' marks and splashes over
 * the convoy; the frigate's flak puffs on low-air.
 *
 * <p>This class keeps the pass order ({@code draw}, pinned by {@code PassOrderTest}); the parts' own
 * passes live in their looks: the ground objects' in {@link GroundPasses}, the enemy units' in {@link
 * UnitPasses} (with the looks that follow the sortie's enemy kinds in {@link KindLooks}), the set pieces'
 * in {@link SetPiecePasses}, the ship's, its shots' and the Airstrike's in {@link PlayerPasses}, the
 * props' in {@link PropPasses}; they share the drawn frame's state through {@link PlayFrame}.
 */
public final class LevelRenderer {

    /**
     * A set piece on high-air is drawn this much larger than on the play plane (the first pass's
     * sprites are drawn at it); descending and rising it scales between the two.
     */
    static final float HIGH_AIR_SCALE = 1.25f;
    /**
     * A set piece off the play plane is drawn at 75 % opacity (body, parts and glow alike); descending
     * and rising it eases between that and opaque by its altitude, so it is opaque when it reaches the
     * plane, where it switches below the ship and can collide.
     */
    private static final float HIGH_AIR_OPACITY = 0.75f;

    /** M5 part E: the strength a torpedo's lit back is added over the chop with ({@link PlayerPasses#drawTorpedoBacks}). */
    static final float TORPEDO_BACK = 0.9f;
    /** The white flashes' strength with the Gameplay tab's flash reduction on. */
    private static final float REDUCED_FLASH = 0.35f;

    private final Sprites sprites;

    private final EnemyLooks[] looks;
    private final WeaponLooks weapons;
    /** What the passes share in the drawn frame: the white flashes' strength and what the beam burns. */
    private final PlayFrame play = new PlayFrame();
    /** The ground objects' passes. */
    private final GroundPasses ground;
    /** The enemy units' passes. */
    private final UnitPasses units;
    /** The looks that follow the sortie's enemy kinds, built once. */
    private final KindLooks kinds;
    /** The set pieces' passes (the bosses, the Kraken's and the Siege Spire's among them). */
    private final SetPiecePasses setPieces;
    /** The ship's, its shots' and the Airstrike's passes. */
    private final PlayerPasses player;
    /** The props' passes: debris, cranes, pickups, sondes, spore mines and bullets. */
    private final PropPasses props;

    private final BossLooks bossLooks;

    /** Level 05's sleds, lobs and battery outlines. */
    private final LunaLooks luna;

    private final Backdrop backdrop;
    /** M5 part C: the backdrop's level and real clocks at the render time. */
    private final BackdropClock clock = new BackdropClock();
    /** M5 part C: Level 09's collapse (the lean, the drop, its shadow, dust, dust peak and heap); null without one. */
    private final CollapseLooks collapse;
    /** The level's road and convoy, if it has them. */
    private final ConvoyLooks convoy;
    /** M5 part D: an air escort's units (Level 10's shuttles), if the level has one. */
    private final ShuttleLooks shuttles;
    /** M5 part D: a scripted loss's glow and lance (Level 10), if the level has one. */
    private final LossLooks loss;

    private final FlashShader flash;
    /** M7 part A: the Act 3 arsenal in flight: beams, drones, fields, EMP, lance, overcharge, dash. */
    private final ArsenalLooks arsenal;
    /** Level 06's darkness, glows, sweeps and the Smart Bomb's flash and ring. */
    private final FarsideLooks farside;

    /** Level 07's lifeboat tow. */
    private final TowLooks tows;
    /** The ship's engine flames, damage smoke and sparks and its shield ring. */
    private final ShipLooks shipLooks;
    /** Rook's craft, its flames, smoke, muzzle flash and eject pod (M5 part A). */
    private final WingmanLooks wingmanLooks;
    /** The flyers' drop shadows on the ground layer. */
    private final Shadows shadows;

    private final BitmapFont font;
    /** The Targeting computer's HP bars and weak-point brackets; null without one fitted. */
    private TargetingOverlay targeting;
    /** The Salvage scanner's glint on the secrets' objects; null without one fitted. */
    private SecretGlints secretGlints;
    /** M5 part E: the {@code sub} pass of a level over water; null over land. */
    private final WaterLooks water;
    /** M5 part E: the arena boss (the Harbour Kraken); null without one. */
    private final KrakenLooks kraken;
    /** M5 part H: the anchored boss without lanes (the Siege Spire) and its canopy's death; null without it. */
    private final SpireLooks spire;
    /** M7 part F: the walking boss (the Revenant Walker), its cone, missiles and wreck; null without it. */
    private final WalkerLooks walker;
    /**
     * M7 part F: Level 19's foundry looks (the vents' grates and fire, the gantries and the nests' cradles, the shift
     * office, the ingots and pallets, the crews' rovers); null without them.
     */
    private final FoundryLooks foundry;
    /** M5 part H: the level's root bursts (Level 14); null without them. */
    private final RootBurstLooks rootBursts;
    /**
     * M5 part E: water effects on the surface (bursts of kills and blasts on the water, splashes,
     * ripple trains), alpha-blended, scrolling with the sea, under the ground units.
     */
    private final Effects surfaceWater = Effects.solid();
    /** M5 part E: effects under the water, drawn in the {@code sub} pass: under-water bursts. */
    private final Effects underWater = Effects.solid();
    /** M5 part E: the torpedoes' bubble trails, over the chop and under the convoy (bubbles rise to the surface). */
    private final Effects bubbles = Effects.solid();
    /** M5 part E: solid effects on low-air (the frigate's flak puffs over the convoy). */
    private final Effects lowAir = Effects.solid();
    /** Step GFX2: the generated soft disc and ring (strobe halos, flak flashes, the pursuit swarm's glows). */
    private final SoftGlow softGlow = new SoftGlow();
    /** Step GFX2: the militia flak's flash cores over their puffs. */
    private final FlakFlashes flakFlashes = new FlakFlashes();

    /** M5 part F: the level's weather looks (Level 12's rain, lightning, gusts, hidden glows and eye). */
    private final WeatherLooks weather;
    /** M7 part E: the level's dust storm (Level 18's visibility mask, veil and silhouettes); null without one. */
    private final StormLooks storm;

    /** M7 part C: the wall touch's sparks and the ship's flash, the stopped shots' sparks at rock. */
    private final CanyonLooks canyonLooks;
    /**
     * M7 part C: the canyon's presentation beyond its collision lines (the walls' heights, the islands'
     * looks, the face patches) from the level data ({@link WallProjection.Looks#of}; {@link #canyon}).
     */
    private WallProjection.Looks canyon;
    /** M7 part C: the terrain the backdrop draws ({@link Backdrop#canyon}), to notice a new one. */
    private Terrain canyonTerrain;

    /**
     * M7 part D: a trigger look over contents with sprites of their own, whose {@code -glow} is the trigger's glow
     * where the look has none (Level 17's seed-vault capsule blinking under the collapsed greenhouse frame,
     * tools/art/siege_units.py; vanguard.pipeline.SpriteUse claims the contents with the look).
     */
    static final java.util.Map<String, String> TRIGGER_CONTENTS = java.util.Map.of("greenhouse-frame", "seed-capsule");

    /** M5 part G: a defend primary's ally (Level 13's Nansen Relay); null without one. */
    private final RelayLooks relay;
    /** M7 part D: the level's ground allies (Level 17's domes); null without them ({@link #groundAllies}). */
    private DomeLooks domes;
    /** M5 part G: the supply drones carrying the drops; null without them. */
    private final DroneLooks drones;
    /** M7 part G: Level 20's travelling ridge on the ground (null for another level). */
    private final RidgeLooks ridge;
    /** M5 part G: the arctic setting's snow on high-air; null in another setting. */
    private SnowLooks snow;

    private final LevelData level;

    /**
     * @param files the assets, for the pivot files of the cranes and set pieces
     * @param script the level's script, whose set pieces it draws
     * @param levelKey the level's key, {@code <act>/level-NN-<slug>}
     */
    public LevelRenderer(
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
        foundry = FoundryLooks.of(sprites, level, script);
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
        this.loss = new LossLooks(sprites, script);
        this.flash = flash;
        this.farside = new FarsideLooks(sprites, script);
        this.tows = new TowLooks(sprites);
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
        this.ridge = RidgeLooks.of(sprites, level);
        this.snow = SnowLooks.of(level, false, sprites.pixel);
        this.arsenal = new ArsenalLooks(sprites, flash);
        this.canyonLooks = new CanyonLooks(sprites);
        ground = new GroundPasses(sprites, flash, play, weather, script, levelKey, softGlow);
        kinds = new KindLooks(sprites, files);
        units = new UnitPasses(
                looks, flash, play, kinds, farside, luna, weather, shipLooks, surfaceWater, launches, softGlow);
        setPieces = new SetPiecePasses(flash, play, font, setPieceLooks, bossLooks, kraken, spire);
        player = new PlayerPasses(sprites, weapons, flash, play, shipLooks, arsenal, canyonLooks, water != null);
        props = new PropPasses(sprites, flash, play, clock, craneLooks, script);
    }

    /**
     * M5 part G: the difficulty's presentation: on {@code easy} a peak with an easy length (Level 13's
     * whiteout, 6 s) in the backdrop and the snow; M7 part E: the dust storm's radii (Level 18).
     */
    public void difficulty(Difficulty difficulty) {
        boolean easy = difficulty == Difficulty.EASY;
        backdrop.easy(easy);
        snow = SnowLooks.of(level, easy, sprites.pixel);
        if (storm != null) {
            storm.difficulty(difficulty);
        }
    }

    /**
     * M7 part D: the level's ground allies' looks (Level 17's domes), each in its sprite set: its own {@code sprite}
     * in the level's {@code allies} block, else its ally's ({@code allies}); none without them.
     */
    public void groundAllies(LevelScript script, AlliesData allies) {
        domes = DomeLooks.of(sprites, script, DomeLooks.names(level, allies));
    }

    /**
     * The fitted utility modules' marks: the Targeting computer's (over the units, under the
     * bullets) and the Salvage scanner's glint (over the ground objects, cranes and tows, in the
     * dark as well); null for a module that is not fitted.
     */
    public void modules(TargetingOverlay targetingOverlay, SecretGlints glints) {
        targeting = targetingOverlay;
        secretGlints = glints;
    }

    /** Frees the light map and the generated textures. */
    public void dispose() {
        softGlow.dispose();
        farside.dispose();
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
    public void restart() {
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
        canyonLooks.restart();
        kinds.restart();
    }

    /** M7 part C: the canyon's presentation beyond its collision lines (the level data's by default). */
    void canyon(WallProjection.Looks looks) {
        canyon = looks;
        canyonTerrain = null;
    }

    /**
     * M7 part A: after each simulation step, the Act 3 arsenal's looks follow its events (catches,
     * drone breaks, dashes, scorch marks; {@link ArsenalLooks}).
     */
    public void stepped(Sortie sortie) {
        arsenal.stepped(sortie);
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
        if (walker != null) {
            walker.stepped(sortie);
        }
        if (foundry != null) {
            foundry.stepped(sortie);
        }
        kinds.tanks(sortie).stepped(sortie);
    }

    /** M5 part E: the renderer's own effects advance one simulation step (with the screen's). */
    public void stepEffects() {
        surfaceWater.step();
        underWater.step();
        bubbles.step();
        lowAir.step();
    }

    /** M5 part E: whether the level is over water: its kills and blasts burst on the water ({@link #surfaceWater()}). */
    public boolean overWater() {
        return water != null;
    }

    /** M5 part E: the surface's water effects (bursts, splashes, ripple trains), started on the ground. */
    public Effects surfaceWater() {
        return surfaceWater;
    }

    /** M5 part E: the effects under the water (under-water bursts), through the {@code sub} pass. */
    public Effects underWater() {
        return underWater;
    }

    /** M5 part E: the torpedoes' bubble trails, on the surface over the chop (not through the {@code sub} pass). */
    public Effects bubbles() {
        return bubbles;
    }

    /** M5 part E: solid effects on low-air (the frigate's flak puffs). */
    public Effects lowAir() {
        return lowAir;
    }

    /**
     * Step GFX2: a militia flak burst ({@link MilitiaFlak}) at the play-field point (x, y) at step {@code tick}: its
     * flash core over the puff ({@link FlakFlashes}).
     */
    public void militiaFlak(double x, double y, long tick) {
        flakFlashes.start(x, y, tick);
    }

    /** M5 part E: the escort frigate (convoy unit {@code k}) fired a flak burst at step {@code tick}: its bow gun flashes. */
    public void flak(int k, long tick) {
        convoy.flak(k, tick);
    }

    /**
     * M5 part E: where a slam arm's segments lie, px from the boss's centre ({@link KrakenLooks#armPoints}),
     * for its severed arm's pops; empty without the Kraken's looks.
     */
    public static double[] armPoints(SetPiece piece, int part) {
        return piece.arena().isPresent() ? KrakenLooks.armPoints(piece, part) : new double[0];
    }

    /** M5 part E: the Kraken's head's offset from its centre, px (y up); 0 without the Kraken's looks. */
    public double krakenHeadDy() {
        return kraken == null ? 0 : kraken.headDy();
    }

    /** M5 part E: an enemy fired a volley from (x, y) at step {@code tick} (a gun's recoil frame). */
    public void enemyFired(double x, double y, long tick) {
        units.enemyFired(x, y, tick);
    }

    /** M5 part H: a mortar lobbed from (x, y) at step {@code tick}: the Siege Spire's mortar pod opens. */
    public void lobbed(Sortie sortie, double x, double y, long tick) {
        setPieces.lobbed(sortie, x, y, tick);
    }

    /** M5 part H: a boss's window launched a unit at step {@code tick}: the Siege Spire's maw flares. */
    public void bossLaunched(long tick) {
        setPieces.bossLaunched(tick);
    }

    /** Set piece {@code k}'s break-up at its death, or null for none. */
    public SetPieceDeath death(int k) {
        return setPieces.death(k);
    }

    /** Rook was hit at step {@code tick}: his hull flashes white. */
    public void wingmanHit(long tick) {
        wingmanLooks.hit(tick);
    }

    /** A unit's pivot file in assets/pivots/, or null when it has none. */
    static JsonValue pivots(Files files, String name) {
        FileHandle file = files.internal("pivots/" + name + ".json");
        return file.exists() ? new JsonReader().parse(file) : null;
    }

    /**
     * @param effects glowing animations over the flyers: explosions, impacts, death glows (those of
     *     ground units on the ground, see {@link Effects#startOnGround})
     * @param debris solid animations on the ground: wrecks, debris, husks
     * @param pieces solid death pieces over the flyers (those of ground units on the ground)
     * @param blasts glowing animations on the ground below the flyers: the Airstrike's blasts
     * @param wrecks the set pieces whose death is playing, drawn breaking up
     * @param alpha interpolation between the previous and the current step
     * @param shieldShimmer 0..1, how strongly the ship shows its last shield hit
     * @param flashReduction tone the white hit and invulnerability flashes down (Gameplay tab)
     * @param screenFlash an act boss's death flash over the play field
     */
    public void draw(
            SpriteBatch batch,
            Sortie sortie,
            Effects effects,
            Effects debris,
            Effects pieces,
            Effects blasts,
            SetPieceWrecks wrecks,
            CreditNumbers credits,
            EdgeWarnings warnings,
            float alpha,
            float shieldShimmer,
            boolean flashReduction,
            ScreenFlash screenFlash) {
        play.whiteFlash = flashReduction ? REDUCED_FLASH : 1;
        play.heatTarget = ArsenalLooks.heatTarget(sortie);
        int piece = ArsenalLooks.heatPiece(play.heatTarget);
        play.heatPiece = piece >= 0 && piece < sortie.setPieceCount() ? sortie.setPiece(piece) : null;
        play.heatPart = ArsenalLooks.heatPart(play.heatTarget);
        play.groundHeatX = ArsenalLooks.groundHeatX(sortie);
        double lag = SimStep.SECONDS * (1 - alpha);
        double scroll = sortie.groundScroll() - sortie.groundSpeed() * lag;
        // M5 part C: between two steps script time moves at the level clock's rate (a fifth in a hold).
        double seconds = sortie.levelSeconds() - lag * sortie.scriptRate();
        clock.record(sortie.levelSeconds(), sortie.realSeconds());
        clock.at(seconds, Math.max(0, sortie.realSeconds() - lag));
        if (collapse != null) {
            collapse.update(sortie, scroll, lag);
        }
        kinds.buildLampreys(sortie);
        weather.at(seconds);
        if (sortie.terrain() != canyonTerrain) {
            // M7 part C: the canyon's walls are the simulation's terrain, never a placed art piece.
            canyonTerrain = sortie.terrain();
            backdrop.canyon(canyonTerrain, canyon);
        }
        backdrop.drawBehind(batch, scroll, clock);
        if (water != null) {
            // M5 part E: the sub layer between the deep swell and the surface's chop (E4 = c).
            water.begin(batch);
            drawSub(batch, sortie, alpha, seconds, wrecks, scroll);
            water.end(batch, sortie.realSeconds() - lag);
        }
        // The ground layer marks where the flyers' shadows may fall (not on open space or the far layer).
        shadows.clear(batch);
        shadows.beginGround(batch);
        backdrop.drawGroundTiles(batch, scroll, clock);
        convoy.drawRoad(batch, Math.round(scroll));
        // With Level 09's collapse's shadow, heap and base dust under the towers (CollapseLooks).
        backdrop.drawGroundPieces(batch, scroll, clock);
        if (ridge != null) {
            // M7 part G: the travelling ridge (the Dust Colossus's body under the sand) on the ground, both ways.
            ridge.draw(batch, seconds, scroll);
        }
        if (foundry != null) {
            // M7 part F: the vents' grates, the office, the ingots, the gantries and cradles, the rovers on the ground.
            foundry.drawGround(batch, sortie, scroll, seconds, clock.real());
        }
        shadows.endGround(batch);
        // M5 part F: the eye's light shaft on the calm sea, under the ground objects.
        weather.drawShaft(batch, seconds);
        // M5 part E: over the chop and under the convoy: the Kraken's foreshadowing shadow (before it
        // scrolls in) and the torpedoes' lit backs and bubbles just under the surface.
        if (kraken != null && !SetPiecePasses.krakenShown(sortie, wrecks)) {
            kraken.drawForeshadow(batch, seconds, sortie.tick());
        }
        player.drawTorpedoBacks(batch, sortie, alpha);
        bubbles.draw(batch, scroll);
        // M5 part E: the Kraken's churn on the lanes, then its grip, head and arms on the platform's sea.
        setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, true);
        // M5 part H: the root bursts' cracks and roots, then the Siege Spire's rooted body (or its stump).
        if (rootBursts != null) {
            rootBursts.drawGround(batch, sortie, seconds, scroll);
        }
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.GROUND);
        if (walker != null) {
            // M7 part F: the walking boss on the ground (its legs, hull, vents, flamer and pods), or its topple and
            // wreck.
            walker.drawGround(batch, sortie, alpha, scroll, play);
        }
        luna.drawSled(batch, sortie, scroll, alpha);
        if (storm == null) {
            luna.drawMarkers(batch, sortie, alpha);
        }
        ground.drawGround(batch, sortie, alpha);
        convoy.drawConvoy(batch, sortie, alpha, play.whiteFlash);
        if (relay != null) {
            // M5 part G: the defended relay on its floe, its damage and, once online, its lights.
            relay.draw(batch, sortie, alpha, play.whiteFlash, seconds, clock.real());
        }
        if (domes != null) {
            // M7 part D: the domes on the ground, their hits, cracks, flicker and cave-in with its dust.
            domes.draw(batch, sortie, alpha, play.whiteFlash, clock.real());
        }
        // M5 part D: shuttles on their pads and low in their liftoff, lost ones gliding into far.
        shuttles.drawLow(batch, sortie, alpha, play.whiteFlash);
        shadows.beginGround(batch);
        backdrop.drawOverhead(batch, scroll, clock);
        shadows.endGround(batch);
        units.drawCreep(batch, sortie, alpha);
        // M7 part A: the beam's scorch marks and the Orbital Lance's scorch line on the ground.
        arsenal.drawGround(batch, scroll);
        arsenal.drawGroundGlow(batch, scroll, sortie.tick());
        debris.draw(batch, scroll);
        // M7 part F: the Warden Tanks' wrecks, scrolling with the ground and riding their belts.
        kinds.tanks(sortie).drawWrecks(batch, scroll);
        // M7 part B: the Burrowers' craters and filling holes on the ground, under the ground units.
        kinds.burrowers(sortie).drawGround(batch, sortie.tick(), alpha, scroll);
        surfaceWater.draw(batch, scroll);
        boolean overHull = units.bossOffPlane(sortie);
        // M7 part C: a killed Threadcrawler's husk under the ground units, the crawler and its pores with them.
        kinds.crawlers(sortie).drawHusks(batch, scroll);
        // M7 part E: the Dust Devils' grit swirls on the ground under the ground units (and under an open one).
        kinds.vortices(sortie).drawSwirls(batch, sortie, alpha);
        units.drawEnemies(batch, sortie, alpha, Depth.GROUND, Launched.ANY);
        kinds.crawlers(sortie).drawChains(batch, sortie, alpha, looks, units.unitFlash);
        kinds.crawlers(sortie).drawPores(batch, sortie, alpha, scroll);
        // M7 part D: a Shellback's dead vent, vent swell and belly glow; the ground units' aura rings.
        kinds.shellbacks(sortie).drawOver(batch, sortie, alpha);
        // M7 part F: the Warden Tanks' turrets (or torn rings) on their hulls, their muzzle flashes.
        kinds.tanks(sortie).drawOver(batch, sortie, alpha);
        AuraLooks auras = kinds.aura(sortie);
        if (auras != null) {
            auras.drawRings(batch, sortie, alpha, true);
        }
        // M5 part H: the root bursts' dust and the tear-free shockwave over the ground units.
        if (rootBursts != null) {
            rootBursts.drawDust(batch, sortie, seconds, scroll);
        }
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SHOCKWAVE);
        if (walker != null) {
            // M7 part F: the walker's flame cone (its tell's outline, its burn) and pilot light over the ground units.
            walker.drawCone(batch, sortie, alpha, clock.real());
        }
        if (foundry != null) {
            // M7 part F: the vents' heat glow over the tell, the flame column over the burn, the smoke after it.
            foundry.drawFire(batch, sortie, scroll);
        }
        ground.drawGlints(batch, sortie, alpha);
        // M5 part E: the telegraphed lanes' marks and the slams' splashes over the convoy and the rafts.
        setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, false);
        farside.darken(batch, sortie, alpha, scroll, seconds);
        farside.drawGlows(batch, sortie, looks, alpha, seconds);
        ground.drawGroundGlows(batch, sortie, alpha);
        player.drawBomberShadows(batch, sortie, alpha);
        drawShadows(batch, sortie, alpha);
        if (relay != null) {
            // M5 part G: a destroyed relay's smoke column over the ground units.
            relay.drawColumn(batch, sortie, alpha, clock.real());
        }
        if (drones != null) {
            // M5 part G: the supply drones low over the relay, under the air units.
            drones.draw(batch, sortie, relay, alpha, seconds);
        }
        units.drawEnemies(batch, sortie, alpha, Depth.LEAP, Launched.ANY);
        units.drawEnemies(batch, sortie, alpha, Depth.LOW_AIR, Launched.ANY);
        blasts.draw(batch, scroll);
        lowAir.draw(batch, scroll);
        flakFlashes.draw(batch, softGlow, sortie.tick(), alpha);
        if (collapse != null) {
            collapse.drawOver(batch);
        }
        drawLowAir(batch, scroll);
        // M5 part F: the eye's turning wall, a flash's bolt and lit cloud, the bodies a flash reveals
        // under the banks, the hidden bodies' glows.
        weather.drawOverBanks(batch, seconds, play.whiteFlash);
        drawRevealed(batch, sortie, alpha);
        drawHiddenGlows(batch, sortie, alpha);
        if (storm != null) {
            // M7 part E: the dust storm's veil over the ground and low-air outside the radius round the ship, an
            // open Dust Devil's core glowing through it, the mortars' markers over it (always drawn).
            storm.drawVeil(batch, sortie, alpha, seconds, clock.real());
            storm.drawHiddenGlows(batch, sortie, alpha, clock.real());
            luna.drawMarkers(batch, sortie, alpha);
        }
        setPieces.drawBossShadows(batch, sortie, alpha);
        units.drawUnitGlows(batch, sortie, alpha);
        // M5 part D: the scripted loss's glow under the air layer, then the shuttles under the flyers.
        loss.drawGlow(batch, sortie, alpha, seconds);
        tows.draw(batch, sortie, alpha, seconds);
        shuttles.drawAir(batch, sortie, alpha, play.whiteFlash);
        // M7 part C: the gates' tendrils on the player's plane, under the air units.
        kinds.tendrils(sortie).draw(batch, sortie, alpha);
        if (auras != null) {
            // M7 part D: the Heralds' radius circles and threads under the flyers.
            auras.drawAuras(batch, sortie, alpha, clock.real());
        }
        units.drawEnemies(batch, sortie, alpha, Depth.AIR, overHull ? Launched.NOT : Launched.ANY);
        if (auras != null) {
            // M7 part D: the Heralds' crests over their bodies, the flyers' rings.
            auras.drawCrests(batch, sortie, alpha);
            auras.drawRings(batch, sortie, alpha, false);
        }
        units.drawChains(batch, sortie, alpha);
        if (storm != null) {
            // M7 part E: the air units outside the radius as silhouettes in the dust keeping their lights, then glows.
            storm.drawSilhouettes(batch, sortie, alpha, units);
        } else {
            // M7 part E: the air units' lights (the Ghost Drone's eye and running lights, its glint) in every level.
            units.drawAirGlows(batch, sortie, alpha);
        }
        farside.drawSweeps(batch, sortie, alpha);
        luna.drawBlobs(batch, sortie, alpha);
        player.drawAirstrike(batch, sortie, alpha);
        setPieces.drawSetPieces(batch, sortie, alpha, seconds, false);
        // M5 part H: the free spire tearing loose, airborne or dying.
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.AIR);
        setPieces.drawWrecks(batch, sortie, wrecks, alpha, seconds);
        pieces.draw(batch, scroll);
        props.drawDebris(batch, sortie, alpha);
        props.drawCranes(batch, sortie, alpha);
        if (secretGlints != null) {
            secretGlints.draw(batch, sortie, alpha);
        }
        props.drawSondes(batch, sortie, alpha);
        props.drawPickups(batch, sortie, alpha);
        if (storm != null) {
            // M7 part E: a faint glint on the pickups the dust would hide.
            storm.drawPickupGlints(batch, sortie, alpha);
        }
        player.drawShots(batch, sortie, alpha, false);
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null) {
            wingmanLooks.draw(batch, rook, sortie.tick(), alpha, play.whiteFlash);
            if (storm != null) {
                // M7 part E: Rook outside the radius, a silhouette with his running lights.
                storm.drawWingman(batch, rook, alpha, wingmanLooks);
            }
        }
        if (sortie.flying()) {
            player.drawShip(batch, sortie, alpha, shieldShimmer);
            units.drawLatched(batch, sortie, alpha);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        player.drawShots(batch, sortie, alpha, true);
        if (sortie.flying()) {
            player.drawMuzzles(batch, sortie, alpha, true);
            // M7 part A: the fields, the drones' lights, the beams, the overcharge's shell.
            arsenal.drawOverShip(batch, sortie, alpha);
        }
        // M7 part A: the EMP Burst's crackle on the stunned units.
        arsenal.drawStunned(batch, sortie, alpha);
        if (rook != null) {
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, true);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (sortie.flying()) {
            player.drawMuzzles(batch, sortie, alpha, false);
        }
        if (rook != null) {
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, false);
        }
        effects.draw(batch, scroll);
        canyonLooks.draw(batch, scroll);
        // M5 part D: the lance above the air layer.
        loss.drawLance(batch, sortie, alpha);
        if (rook != null) {
            // His eject pod over the explosion once it has popped out of it.
            wingmanLooks.drawOver(batch, rook, sortie.tick(), alpha);
        }
        farside.drawSmartBomb(batch, sortie, alpha, play.whiteFlash);
        // M7 part A: the EMP Burst's ring, the Orbital Lance's targeting line and column.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        arsenal.drawSpecials(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        setPieces.drawSetPieces(batch, sortie, alpha, seconds, true);
        if (overHull) {
            units.drawEnemies(batch, sortie, alpha, Depth.AIR, Launched.ONLY);
        }
        // M5 part D: the cloaked units' shimmer on high-air, above the ship.
        units.drawEnemies(batch, sortie, alpha, Depth.HIGH_AIR, Launched.ANY);
        double canopyDeath = setPieces.spireDeathSeconds(sortie, wrecks, alpha);
        if (spire != null) {
            // M5 part H: the canopy's veins die with the Siege Spire.
            backdrop.frontFade(SpireLooks.veins(canopyDeath));
        }
        backdrop.drawFront(batch, scroll, clock);
        if (spire != null) {
            // M5 part H: the canopy tearing and the low sun's beams after the Spire's death.
            spire.drawDaylight(batch, canopyDeath, backdrop.highAirScroll(scroll));
        }
        if (snow != null) {
            // M5 part G: the arctic snow on high-air, falling on the real clock in the halt.
            snow.draw(batch, seconds, clock.real());
        }
        // M5 part F: the rain, the breakout's light, the gust telegraph and the sky flash, under the bullets.
        weather.drawFront(
                batch, seconds, clock.real(), sortie.wind(), SimStep.ticks(sortie.levelSeconds()), play.whiteFlash);
        if (targeting != null) {
            targeting.draw(batch, sortie, alpha);
        }
        props.drawMines(batch, sortie, alpha);
        // M5 part H: the Spire's sweep, its telegraph line and beam, with the bullets.
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SWEEP);
        if (walker != null) {
            // M7 part F: the homing missiles over their smoke trails, with the bullets.
            walker.drawMissiles(batch, sortie, alpha);
        }
        props.drawBullets(batch, sortie, alpha);
        screenFlash.draw(batch, sprites.pixel, alpha, play.whiteFlash);
        warnings.draw(batch, sortie.tick(), alpha);
        credits.draw(batch, font);
        setPieces.drawBossBar(batch, sortie);
    }

    /**
     * M5 part F: the low-air layer (its tiles, banks and pieces); in a level with weather, during a
     * lightning flash's reveal, lit from inside toward a pale blue and seen through, so the bodies
     * under the banks show (F1 = a). The tint follows the flash reduction; the see-through does not.
     */
    private void drawLowAir(SpriteBatch batch, double scroll) {
        float tint = weather.bankTint(play.whiteFlash);
        float opacity = weather.bankOpacity();
        if (tint <= 0 && opacity >= 1) {
            backdrop.drawLowAir(batch, scroll, clock);
            return;
        }
        flash.begin(batch, WeatherLooks.LIT, tint);
        batch.setColor(1, 1, 1, opacity);
        backdrop.drawLowAir(batch, scroll, clock);
        batch.setColor(Color.WHITE);
        flash.end(batch);
    }

    /**
     * M5 part F, a lightning flash's reveal (F1 = a): the {@code low-air} units and low-air triggers
     * under the banks drawn again over them at {@link WeatherLooks#revealed}, so wherever they are on
     * the screen they show through every bank layer over them as the banks are seen through.
     */
    private void drawRevealed(SpriteBatch batch, Sortie sortie, float alpha) {
        float opacity = weather.revealed();
        if (opacity <= 0) {
            return;
        }
        batch.setColor(1, 1, 1, opacity);
        units.drawRevealed(batch, sortie, alpha);
        ground.drawRevealed(batch, sortie, alpha);
        batch.setColor(Color.WHITE);
    }

    /**
     * M5 part F, over the banks that hide them (F1 = a): each {@code low-air} unit's faint glow and a
     * low-air trigger's own glow (the canister's amber beacon, its "faint glint").
     */
    private void drawHiddenGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!weather.occludes()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (weather.glows()) {
            units.drawHiddenGlows(batch, sortie, alpha, clock.real());
        }
        ground.drawHiddenGlows(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * The steps of a boss's chained death (design/enemies/bosses): an act boss's from tail to head
     * over its chain length, a mid-boss's {@value BossPose#LEGACY_CHAIN_STEP_TICKS} steps a part.
     */
    public static int chainTicks(SetPiece piece) {
        double seconds = BossPose.chainSeconds(piece);
        return Double.isNaN(seconds) ? piece.partCount() * BossPose.LEGACY_CHAIN_STEP_TICKS : SimStep.ticks(seconds);
    }

    /** Whether a boss's death is the act boss's chain from tail to head (else the frigate's per-part bursts). */
    public static boolean tailToHead(SetPiece piece) {
        return !Double.isNaN(BossPose.chainSeconds(piece));
    }

    /** Whether a boss's death flashes the screen: an act boss's (mid-bosses have none). */
    public static boolean flashesAtDeath(SetPiece piece) {
        return BossPose.actBoss(piece);
    }

    /**
     * An act boss's chained death from tail to head in the pose it died in (its hull's length and
     * width from its size), the bursts' offsets at the scale it was drawn at.
     */
    public static List<DeathChain.Burst> deathChain(SetPiece piece) {
        double angle = BossPose.angle(piece, BossPose.turn(piece, 1));
        double[] xs = new double[piece.partCount()];
        double[] ys = new double[piece.partCount()];
        for (int p = 0; p < xs.length; p++) {
            xs[p] = piece.partOffsetX(p);
            ys[p] = piece.partOffsetY(p);
        }
        return DeathChain.plan(
                xs, ys, angle, piece.spec().size().height(), piece.spec().size().width(), chainTicks(piece));
    }

    /** The head end of a boss's hull in the pose it died in, px from its centre (x right, y up). */
    public static double[] deathHead(SetPiece piece) {
        double angle = BossPose.angle(piece, BossPose.turn(piece, 1));
        double length = piece.spec().size().height();
        return new double[] {DeathChain.headX(angle, length), DeathChain.headY(angle, length)};
    }

    /** Where an enemy is drawn in the stack. */
    enum Depth {
        /** The ground units, with the ground objects. */
        GROUND,
        /**
         * M5 part C: a pouncer leaping off the ground (the Ravager), over its pack and the flyers'
         * shadows, under the low flyers; in its air window it is on {@link #AIR}.
         */
        LEAP,
        /** The low flyers, below the low-air banks. */
        LOW_AIR,
        /** The flyers on the play plane. */
        AIR,
        /**
         * M5 part D: a cloaked unit on high-air (the Wraith before its decloak), drawn as its shimmer
         * above the ship and the effects, under the high-air layer and the bullets.
         */
        HIGH_AIR;

        /** Its depth by the layer it is on now ({@link Enemy#layer()}: a pounce's air window is air). */
        static Depth of(Enemy enemy) {
            if (enemy.cloaked()) {
                return HIGH_AIR;
            }
            Layer layer = enemy.layer();
            if (enemy.leaping()) {
                return layer == Layer.AIR ? AIR : LEAP;
            }
            // M7 part B: a Burrower is on the ground layer in every phase (its ripple, bulge and body), no shadow.
            if (enemy.burrowing()) {
                return GROUND;
            }
            // M5 part G: a lane walker (the Skimmer) is a boat on the water: its sub body, wake and
            // surface frames are drawn with the ground units, under the low-air banks, without a drop shadow.
            if (enemy.grounded() || enemy.walking() || enemy.laneWalking()) {
                return GROUND;
            }
            // M7 part E: a vortex (the Dust Devil) closed, rising or sinking is on the ground layer, drawn with the
            // ground units under the dust storm's veil like its open self on low-air (round 41 K.a: drawn with the
            // flyers it showed over the veil as a dark silhouette and vanished into the dust as it opened).
            if (layer == Layer.GROUND && enemy.spec().vortex().isPresent()) {
                return GROUND;
            }
            return layer == Layer.LOW_AIR ? LOW_AIR : AIR;
        }
    }

    /** Which of a depth's enemies a pass draws: all, all but a boss's launched units, or only those. */
    enum Launched {
        ANY,
        NOT,
        ONLY
    }

    /**
     * M5 part E: the {@code sub} layer, in the {@link WaterLooks} pass: the Kraken's parts under the
     * water (or, before it scrolls in, its foreshadowing shadow), the sunken triggers and the freed
     * pod's rise, the naval units' plain bodies, the torpedoes and the under-water effects.
     */
    private void drawSub(
            SpriteBatch batch, Sortie sortie, float alpha, double seconds, SetPieceWrecks wrecks, double scroll) {
        setPieces.drawKrakenSub(batch, sortie, alpha, wrecks, scroll);
        ground.drawSunken(batch, sortie, alpha);
        units.drawNavalSub(batch, sortie, alpha);
        player.drawUnderWater(batch, sortie, alpha);
        underWater.draw(batch, scroll);
    }

    /**
     * The flyers' drop shadows on the ground layer, from their frames' alpha (design/art-direction,
     * Shadows): the low flyers' at the low-air offset, the play plane's flyers' (the units, the
     * chains' segments and the ship) at the air offset, a leaping pouncer's sliding out with its lift;
     * under the low flyers and the low-air layer.
     */
    private void drawShadows(SpriteBatch batch, Sortie sortie, float alpha) {
        shadows.begin(batch);
        units.drawShadows(batch, sortie, alpha);
        if (sortie.flying()) {
            Ship craft = sortie.ship();
            Shadows.draw(
                    batch,
                    sprites.ship.get(craft.bank() + ShipSpec.HARD_BANK),
                    Math.round(X0 + craft.renderX(alpha)),
                    Math.round(craft.renderY(alpha)),
                    Shadows.AIR_DX,
                    Shadows.AIR_DY,
                    1);
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null) {
            wingmanLooks.drawShadow(batch, rook, alpha);
        }
        shuttles.drawShadows(batch, sortie, alpha);
        setPieces.drawSpireShadows(batch, sortie, alpha);
        shadows.end(batch);
    }

    /** M5 part H: the Siege Spire's passes ({@link SpireLooks}). */
    enum SpirePass {
        GROUND,
        SHOCKWAVE,
        AIR,
        SWEEP
    }

    /** M5 part H: whether a set piece is the Siege Spire, drawn by its own looks (its death too). */
    public static boolean spire(SetPiece piece) {
        return SpireLooks.drawn(piece);
    }

    /** A set piece's scale at {@code altitude} (0 on the play plane, 1 on high-air), for it and its effects. */
    public static float highAirScale(double altitude) {
        return (float) (1 + (HIGH_AIR_SCALE - 1) * altitude);
    }

    /** A set piece's opacity at {@code altitude} (0 on the play plane, 1 on high-air), for it and its effects. */
    public static float highAirOpacity(double altitude) {
        return (float) (1 - (1 - HIGH_AIR_OPACITY) * altitude);
    }
}
