package vanguard.game.render;

import static vanguard.game.render.LevelRenderer.drawnScroll;
import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
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
 * <p>RF6: the frame is drawn in named passes, in this order: {@link #behindPass behind}, {@link #groundPass ground},
 * {@link #groundUnitsPass ground units}, {@link #shadowPass shadows}, {@link #lowAirPass low-air}, {@link
 * #planeUnitsPass plane units}, {@link #setPiecePass set pieces}, {@link #shotPass shots}, {@link #highAirPass
 * high-air}, {@link #weatherPass weather} and {@link #overlayPass overlays}; each takes the {@link Frame} and draws
 * the looks of {@link LevelLooks}. The order is the behaviour: {@code PassOrderTest} pins it.
 */
final class DrawPasses extends LevelLooks {
    /** The white flashes' strength with the Gameplay tab's flash reduction on. */
    private static final float REDUCED_FLASH = 0.35f;

    /** M7 part C: the terrain the backdrop draws ({@link Backdrop#canyon}), to notice a new one. */
    private Terrain canyonTerrain;
    /** M8 part C: whether the grade covers some of the field this frame: the glows are drawn after it. */
    private boolean graded;
    /** M8 part E: the sortie and interpolation being drawn, for what is drawn under the walls ({@link #drawUnderWalls}). */
    private Sortie drawing;

    private float drawingAlpha;

    /** The frame being drawn, handed from pass to pass (one, reused: nothing is allocated per frame). */
    private final Frame frame = new Frame();

    DrawPasses(
            Sprites sprites,
            EnemyLooks[] looks,
            WeaponLooks weapons,
            Files files,
            FlashShader flash,
            BitmapFont font,
            LevelData level,
            LevelScript script,
            String levelKey) {
        super(sprites, looks, weapons, files, flash, font, level, script, levelKey);
        backdrop.underWalls(this::drawUnderWalls);
    }

    /** M7 part C: the canyon's presentation beyond its collision lines (the level data's by default). */
    void canyon(WallProjection.Looks presentation) {
        canyon = presentation;
        canyonTerrain = null;
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
    void draw(
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
        drawing = sortie;
        drawingAlpha = alpha;
        Frame f = frame;
        f.sortie = sortie;
        f.effects = effects;
        f.debris = debris;
        f.pieces = pieces;
        f.blasts = blasts;
        f.wrecks = wrecks;
        f.credits = credits;
        f.warnings = warnings;
        f.alpha = alpha;
        f.shieldShimmer = shieldShimmer;
        f.screenFlash = screenFlash;
        prepare(f);
        behindPass(batch, f);
        groundPass(batch, f);
        groundUnitsPass(batch, f);
        shadowPass(batch, f);
        lowAirPass(batch, f);
        planeUnitsPass(batch, f);
        setPiecePass(batch, f);
        shotPass(batch, f);
        highAirPass(batch, f);
        weatherPass(batch, f);
        overlayPass(batch, f);
    }

    /** Before the passes: the looks' state for this frame (the clocks, the heat, the grade, the weather, the walls). */
    private void prepare(Frame f) {
        Sortie sortie = f.sortie;
        float alpha = f.alpha;
        // M8 part A: Rook's craft wears the hydro-kit under water (the ship's: PlayerPasses.hull).
        wingmanLooks.underwater(sortie.underwater());
        shipLooks.underwater(sortie.underwater());
        play.heatTarget = ArsenalLooks.heatTarget(sortie);
        int piece = ArsenalLooks.heatPiece(play.heatTarget);
        play.heatPiece = piece >= 0 && piece < sortie.setPieceCount() ? sortie.setPiece(piece) : null;
        play.heatPart = ArsenalLooks.heatPart(play.heatTarget);
        play.groundHeatX = ArsenalLooks.groundHeatX(sortie);
        double lag = SimStep.SECONDS * (1 - alpha);
        double scroll = drawnScroll(sortie, alpha);
        // M5 part C: between two steps script time moves at the level clock's rate (a fifth in a hold).
        double seconds = sortie.levelSeconds() - lag * sortie.scriptRate();
        f.lag = lag;
        f.scroll = scroll;
        f.seconds = seconds;
        clock.record(sortie.levelSeconds(), sortie.realSeconds());
        clock.at(seconds, Math.max(0, sortie.realSeconds() - lag));
        if (collapse != null) {
            collapse.update(sortie, scroll, lag);
        }
        kinds.buildLampreys(sortie);
        graded = false;
        if (dark != null && maw != null) {
            // M8 part H: the vent's light gutters under the Maw's dive.
            dark.ventDim(maw.ventDim(sortie));
        }
        if (grade != null) {
            // M8 part C: this frame's look and waterline wipe.
            grade.prepare(sortie, scroll, seconds);
            graded = grade.active();
        }
        weather.at(seconds);
        if (sortie.terrain() != canyonTerrain) {
            // M7 part C: the canyon's walls are the simulation's terrain, never a placed art piece.
            canyonTerrain = sortie.terrain();
            backdrop.canyon(canyonTerrain, canyon);
        }
    }

    /** The backdrop behind the ground and, over water, the {@code sub} layer. */
    private void behindPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        SetPieceWrecks wrecks = f.wrecks;
        float alpha = f.alpha;
        double lag = f.lag;
        double scroll = f.scroll;
        double seconds = f.seconds;
        backdrop.drawBehind(batch, scroll, clock);
        if (water != null) {
            // M5 part E: the sub layer between the deep swell and the surface's chop (E4 = c).
            water.begin(batch);
            drawSub(batch, sortie, alpha, seconds, wrecks, scroll);
            water.end(batch, sortie.realSeconds() - lag);
        }
    }

    /** The ground layer: its tiles, road and pieces, the ground objects, the convoy, the overhead pieces. */
    private void groundPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        SetPieceWrecks wrecks = f.wrecks;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
        // The ground layer marks where the flyers' shadows may fall (not on open space or the far layer).
        shadows.clear(batch);
        shadows.beginGround(batch);
        backdrop.drawGroundTiles(batch, scroll, clock);
        convoy.drawRoad(batch, Math.round(scroll));
        // With Level 09's collapse's shadow, heap and base dust under the towers (CollapseLooks).
        // M9 part B: the prison tender's hold opens at the escort swap's release (Level 29).
        backdrop.holdOpened(sortie.escortSwap().released() ? sortie.escortSwap().ticksSinceRelease() : -1);
        backdrop.drawGroundPieces(batch, scroll, clock);
        if (ridge != null) {
            // M7 part G: the travelling ridge (the Dust Colossus's body under the sand) on the ground, both ways.
            ridge.draw(batch, seconds, scroll);
        }
        if (foundry != null) {
            // M7 part F: the vents' grates, the office, the ingots, the gantries and cradles, the rovers on the ground.
            foundry.drawGround(batch, sortie, scroll, seconds, clock.real());
        }
        if (plumes != null) {
            // M8 part G: the heat plumes' mouths on the sea floor.
            plumes.drawGround(batch, sortie, scroll);
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
        if (colossus != null) {
            // M7 part H: the breaching boss's craters, funnel, body, tell, mouth, glow and vents; its furrow and
            // crater.
            colossus.drawGround(batch, sortie, alpha, scroll, clock.real());
        }
        luna.drawSled(batch, sortie, scroll, alpha);
        if (storm == null) {
            luna.drawMarkers(batch, sortie, alpha);
        }
        if (crawlerDrops != null) {
            // M8 part B: Conamara's ice-crawler driving up the road with the armour patch, and back.
            crawlerDrops.draw(batch, seconds);
        }
        if (story != null) {
            // M9 part G: Level 34's Helix core shut in the hold, under the clamps that hold it.
            story.drawHold(batch, sortie, alpha);
        }
        ground.drawGround(batch, sortie, alpha);
        // M9 part E: Level 32's blast doors, mining charges and repair crate.
        kinds.deepCore(sortie).drawObjects(batch, sortie, alpha, flash, play.whiteFlash);
        // M9 part F: Level 33's Claim 114, its beacon blinking.
        kinds.leechField(sortie).drawObjects(batch, sortie, alpha, flash, play.whiteFlash);
        convoy.drawConvoy(batch, sortie, alpha, play.whiteFlash);
        // M9 part D: a hull escort's tug (Level 31's Bellwether) over the outcrops.
        tug(sortie).draw(batch, sortie, alpha, play.whiteFlash);
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
    }

    /** The ground units with the marks, wrecks and glows on the ground under the flyers. */
    private void groundUnitsPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        Effects debris = f.debris;
        SetPieceWrecks wrecks = f.wrecks;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
        units.drawCreep(batch, sortie, alpha);
        // M7 part A: the beam's scorch marks and the Orbital Lance's scorch line on the ground.
        arsenal.drawGround(batch, scroll);
        arsenal.drawGroundGlow(batch, scroll, sortie.tick());
        debris.draw(batch, scroll);
        // M7 part F: the Warden Tanks' wrecks, scrolling with the ground and riding their belts.
        kinds.tanks(sortie).drawWrecks(batch, scroll);
        // M8 part B: the Drill Rigs' cracked ice and stumps, their hoses' decals and pumps under the cores.
        kinds.rigs(sortie).drawGround(batch, sortie, alpha, scroll);
        // M7 part B: the Burrowers' craters and filling holes on the ground, under the ground units.
        kinds.burrowers(sortie).drawGround(batch, sortie.tick(), alpha, scroll);
        surfaceWater.draw(batch, scroll);
        f.overHull = units.bossOffPlane(sortie);
        // M7 part C: a killed Threadcrawler's husk under the ground units, the crawler and its pores with them.
        kinds.crawlers(sortie).drawHusks(batch, scroll);
        // M7 part E: the Dust Devils' grit swirls on the ground under the ground units (and under an open one).
        kinds.vortices(sortie).drawSwirls(batch, sortie, alpha);
        // M8 part C: the live siphon pumps' roots in their racks, under the ground units.
        kinds.pumps(sortie).drawGround(batch, sortie, alpha);
        // M8 part D: the silt puffs at the walkers' feet on the sea floor, under them.
        plain.draw(batch, scroll);
        // M9 part B: the SAM Nests' bases, the clamp guns' clamps and the opened clamps, under the ground units.
        kinds.ceres(sortie).drawGround(batch, sortie, alpha, scroll);
        // M9 part E: the Striders' wrecks and hulks, the opened blast doors and repair crate.
        kinds.deepCore(sortie).drawGround(batch, sortie, scroll);
        // M9 part F: Claim 114 shot open.
        kinds.leechField(sortie).drawGround(batch, sortie, scroll);
        // Round 67 G.1 c: the liveried Warden Tanks' faint ground shadows, under the ground units.
        kinds.tanks(sortie).drawShadows(batch, sortie, alpha);
        units.drawEnemies(batch, sortie, alpha, Depth.GROUND, Launched.ANY);
        // M9 part E: the Sentinels' telegraphs (the arc, the reach ring) and the toppling Striders' fall lines.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        kinds.deepCore(sortie).drawTells(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        kinds.crawlers(sortie).drawChains(batch, sortie, alpha, looks, units.unitFlash);
        kinds.crawlers(sortie).drawPores(batch, sortie, alpha, scroll);
        // M7 part D: a Shellback's dead vent, vent swell and belly glow; the ground units' aura rings.
        kinds.shellbacks(sortie).drawOver(batch, sortie, alpha);
        // M7 part F: the Warden Tanks' turrets (or torn rings) on their hulls, their muzzle flashes.
        kinds.tanks(sortie).drawOver(batch, sortie, alpha);
        // M8 part B: the dead rigs' steam plumes, the live ones' bore glow and muzzle tell.
        kinds.rigs(sortie).drawOver(batch, sortie, looks, alpha, scroll);
        AuraLooks auras = kinds.aura(sortie);
        f.auras = auras;
        if (auras != null) {
            auras.drawRings(batch, sortie, alpha, true);
        }
        if (shields != null) {
            // M9 part A: the ground units' hex shields.
            shields.draw(batch, sortie, alpha, true);
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
        if (colossus != null) {
            // M7 part H: the suction's sand streaks into an open mouth, over the ground units.
            colossus.drawStreaks(batch, sortie, alpha, scroll, clock.real());
        }
        if (foundry != null) {
            // M7 part F: the vents' heat glow over the tell, the flame column over the burn, the smoke after it.
            foundry.drawFire(batch, sortie, scroll);
        }
        if (plumes != null) {
            // M8 part G: the plumes' silt, glow, eruption and heat over the ground units, the bends' ripples.
            plumes.drawOver(batch, sortie, scroll);
        }
        if (maw != null) {
            // M8 part H: the dive's darkness, the lunge's shadow, the coil, the sacs, implants and head, its death.
            maw.draw(batch, sortie, alpha, play.whiteFlash);
        }
        if (sovereign != null) {
            // M9 part H: the station: its base, rings, arc band, batteries, bays and core; its death.
            sovereign.draw(batch, sortie, alpha, play.whiteFlash, scroll);
        }
        ground.drawGlints(batch, sortie, alpha);
        // M5 part E: the telegraphed lanes' marks and the slams' splashes over the convoy and the rafts.
        setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, false);
        farside.darken(batch, sortie, alpha, scroll, seconds);
        farside.drawGlows(batch, sortie, looks, alpha, seconds);
        if (!graded) {
            // M8 part C: under the grade the glows wait for it (after the ship).
            ground.drawGroundGlows(batch, sortie, alpha);
        }
        if (cruisers != null) {
            // Step I2: the cruisers' holes in the night ice where they broke through, on the ground under the flyers.
            cruisers.drawHoles(batch, sortie, scroll);
        }
    }

    /** The deep risers, the bombers' and the flyers' drop shadows on the ground layer. */
    private void shadowPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        float alpha = f.alpha;
        // M8 part D: the Abyss Rays deep under the play plane (the shadow, the rise's first half, the sink's second).
        units.drawRisers(batch, sortie, alpha);
        player.drawBomberShadows(batch, sortie, alpha);
        drawShadows(batch, sortie, alpha);
    }

    /** Low-air: the low flyers, the low-air layer's banks, the weather and the storm's veil over them. */
    private void lowAirPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        Effects blasts = f.blasts;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
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
        if (cruisers != null) {
            // M8 part H: the cruisers on low-air: their rise, glow, tells, broadsides, point defence, flak and sparks.
            cruisers.draw(batch, sortie);
        }
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
        if (!graded) {
            units.drawUnitGlows(batch, sortie, alpha);
        }
    }

    /** The flyers on the play plane and what flies with them. */
    private void planeUnitsPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
        boolean overHull = f.overHull;
        AuraLooks auras = f.auras;
        // M5 part D: the scripted loss's glow under the air layer, then the shuttles under the flyers.
        loss.drawGlow(batch, sortie, alpha, seconds);
        tows.draw(batch, sortie, alpha, seconds);
        shuttles.drawAir(batch, sortie, alpha, play.whiteFlash);
        // M7 part C: the gates' tendrils on the player's plane, under the air units.
        kinds.tendrils(sortie).draw(batch, sortie, alpha);
        // M9 part D: the Shard groups' air links, under the drones.
        kinds.rig(sortie).drawLinks(batch, sortie);
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
        if (shields != null) {
            // M9 part A: the flyers' hex shields over their hulls.
            shields.draw(batch, sortie, alpha, false);
        }
        units.drawChains(batch, sortie, alpha);
        if (tenderDrops != null) {
            // M8 part C: the Thera Deep tender swimming up with the armour patch, and back.
            tenderDrops.drawBodies(batch, seconds);
        }
        if (buoys != null) {
            // M8 part E: the survey buoy moored on the play plane, releasing the armour patch.
            buoys.drawBodies(batch, seconds, scroll);
        }
        if (storm != null) {
            // M7 part E: the air units outside the radius as silhouettes in the dust keeping their lights, then glows.
            storm.drawSilhouettes(batch, sortie, alpha, units);
        } else if (!graded) {
            // M7 part E: the air units' lights (the Ghost Drone's eye and running lights, its glint) in every level.
            units.drawAirGlows(batch, sortie, alpha);
        }
        farside.drawSweeps(batch, sortie, alpha);
        luna.drawBlobs(batch, sortie, alpha);
        player.drawAirstrike(batch, sortie, alpha);
    }

    /** The set pieces on the play plane, their wrecks, the death pieces, debris, cranes and pickups. */
    private void setPiecePass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        Effects pieces = f.pieces;
        SetPieceWrecks wrecks = f.wrecks;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
        setPieces.drawSetPieces(batch, sortie, alpha, seconds, false);
        // M5 part H: the free spire tearing loose, airborne or dying.
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.AIR);
        setPieces.drawWrecks(batch, sortie, wrecks, alpha, seconds);
        // M9 part A: the wrecks drifting off in vacuum, under the death pieces.
        vacuumDeaths.drawWrecks(batch, sortie.tick(), alpha, scroll);
        pieces.draw(batch, scroll);
        // M9 part B: the Talons' wings spinning off, the belt's rocks shot apart.
        kinds.ceres(sortie).drawPieces(batch, sortie, alpha);
        // M9 part C: a Gunship's halves and gold plates flung out.
        kinds.minefield(sortie).drawPieces(batch, sortie, alpha, scroll);
        // M9 part D: a Shard Drone's shards, a Buzzsaw's blades.
        kinds.rig(sortie).drawPieces(batch, sortie, alpha);
        // M9 part E: the Sentinels' heads toppling off.
        kinds.deepCore(sortie).drawPieces(batch, sortie, scroll);
        props.drawDebris(batch, sortie, alpha);
        if (colossus != null) {
            // M7 part H: the rocks the breaching boss tears loose toward its mouth; its throe's plume and rock rain.
            colossus.drawRocks(batch, sortie, alpha, scroll);
        }
        props.drawCranes(batch, sortie, alpha);
        if (secretGlints != null) {
            secretGlints.draw(batch, sortie, alpha);
        }
        props.drawSondes(batch, sortie, alpha);
        props.drawPickups(batch, sortie, alpha);
        if (story != null) {
            // M9 part G: the Helix core rising out of the hold, then drifting loose (PropPasses leaves it out).
            story.drawAir(batch, sortie, alpha);
        }
        if (storm != null) {
            // M7 part E: a faint glint on the pickups the dust would hide.
            storm.drawPickupGlints(batch, sortie, alpha);
        }
    }

    /** The shots, Rook and the ship, the under-water grade, the glowing shots, muzzles and effects, the specials. */
    private void shotPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        Effects effects = f.effects;
        float alpha = f.alpha;
        float shieldShimmer = f.shieldShimmer;
        double scroll = f.scroll;
        double seconds = f.seconds;
        // M8 part A: the harpoons' cables and motor lights under them.
        deepWater.drawUnderShots(batch, sortie, alpha);
        player.drawShots(batch, sortie, alpha, false);
        Wingman rook = sortie.wingman().orElse(null);
        // M8 part A: the Bulwark flies in his slot.
        deepWater.drawBulwark(batch, sortie, alpha);
        // M9 part B: the rescued Rook hidden in the tender's hold, his Ember's launch out of it (RescueLooks).
        rescue.draw(batch, sortie, rook, alpha);
        if (rook != null && !rescue.hides(sortie, rook)) {
            wingmanLooks.draw(batch, rook, sortie.tick(), alpha, play.whiteFlash);
            // M8 part F: his spin-down after his loss (Level 26), in his craft's place.
            rookLoss.drawSpin(batch, rook, alpha);
            if (storm != null) {
                // M7 part E: Rook outside the radius, a silhouette with his running lights.
                storm.drawWingman(batch, rook, alpha, wingmanLooks);
            }
        }
        if (sortie.flying()) {
            player.drawShip(batch, sortie, alpha, shieldShimmer);
            units.drawLatched(batch, sortie, alpha);
        }
        // M8 AW: the depth charges tossed from the stern rack over the hull.
        deepWater.drawTossed(batch, sortie, alpha);
        drawUnderGrade(batch, sortie, alpha, scroll, seconds, rook);
        // M8 part F: over the grade the Anglers' glow, stalk, lure, ring flash and death; the pickups' gold halos.
        kinds.anglers(sortie).drawGlows(batch, sortie, alpha);
        if (dark != null) {
            dark.drawHalos(batch, sortie, alpha, clock.real());
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        player.drawShots(batch, sortie, alpha, true);
        // M8 part A: the depth charges' fuse lamps and the Decoy Flares.
        deepWater.drawOverShots(batch, sortie, alpha);
        if (sortie.flying()) {
            player.drawMuzzles(batch, sortie, alpha, true);
            if (arcs != null) {
                // M9 part A: the Tesla Coil Pod's arcs and its terminal's light.
                arcs.draw(batch, sortie, alpha);
            }
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
        // M8 part A: the harpoons' impacts, the depth charges' bursts, the Bulwark's sparks and loss.
        deepWater.drawEffects(batch, scroll);
        canyonLooks.draw(batch, scroll);
        // M5 part D: the lance above the air layer.
        loss.drawLance(batch, sortie, alpha);
        // M8 part F: Rook's loss: the torpedo trails, the hits' flashes, his failing lamp (over the grade).
        rookLoss.drawOver(batch, sortie, alpha);
        if (rook != null) {
            // His eject pod over the explosion once it has popped out of it.
            wingmanLooks.drawOver(batch, rook, sortie.tick(), alpha);
        }
        farside.drawSmartBomb(batch, sortie, alpha, play.whiteFlash);
        // M7 part A: the EMP Burst's ring, the Orbital Lance's targeting line and column.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        arsenal.drawSpecials(batch, sortie, alpha);
        // M8 part A: the Sonar Pulse's ring and contact brackets.
        deepWater.drawSonar(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** High-air: the set pieces and units above the ship, the high-air layer, the snow, the breakout, the daylight. */
    private void highAirPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        SetPieceWrecks wrecks = f.wrecks;
        float alpha = f.alpha;
        double scroll = f.scroll;
        double seconds = f.seconds;
        boolean overHull = f.overHull;
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
        if (grade != null) {
            // M8 part C: the marine snow on the real clock, where the field is graded.
            grade.drawSnow(batch, scroll, clock.real());
        }
        if (breakout != null) {
            // M8 part H: the ice ceiling's burst and slabs at the breakout, the orbit's white.
            breakout.draw(batch, sortie, alpha, scroll, seconds);
        }
        if (spire != null) {
            // M5 part H: the canopy tearing and the low sun's beams after the Spire's death.
            spire.drawDaylight(batch, canopyDeath, backdrop.highAirScroll(scroll));
        }
        if (snow != null) {
            // M5 part G: the arctic snow on high-air, falling on the real clock in the halt.
            snow.draw(batch, seconds, clock.real());
        }
    }

    /** The weather's front: the rain, the breakout's light, the gust telegraph and the sky flash, under the bullets. */
    private void weatherPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        double seconds = f.seconds;
        // M5 part F: the rain, the breakout's light, the gust telegraph and the sky flash, under the bullets.
        weather.drawFront(
                batch, seconds, clock.real(), sortie.wind(), SimStep.ticks(sortie.levelSeconds()), play.whiteFlash);
    }

    /** Over every layer: the targeting marks, the mines and bullets, the boss's death flash, the warnings and the bar. */
    private void overlayPass(SpriteBatch batch, Frame f) {
        Sortie sortie = f.sortie;
        SetPieceWrecks wrecks = f.wrecks;
        CreditNumbers credits = f.credits;
        EdgeWarnings warnings = f.warnings;
        float alpha = f.alpha;
        ScreenFlash screenFlash = f.screenFlash;
        double scroll = f.scroll;
        if (targeting != null) {
            targeting.draw(batch, sortie, alpha);
        }
        props.drawMines(batch, sortie, alpha);
        // M9 part C: the line mines, their lamps and bursts.
        kinds.mines(sortie).draw(batch, sortie, alpha);
        // M5 part H: the Spire's sweep, its telegraph line and beam, with the bullets.
        setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SWEEP);
        // M9 part A: the laser lines' telegraphs and rails, with the bullets.
        lines.draw(batch, sortie, kinds.leechField(sortie));
        // M9 part F: the Rail Bunkers' locked lines, rails and muzzle flashes.
        kinds.leechField(sortie).drawLines(batch, sortie, alpha);
        if (sovereign != null) {
            // M9 part H: the station's rail lines and laser sweeps; step BOSS70: its thrown wrecks and their debris.
            sovereign.drawThrown(batch, sortie, alpha);
            sovereign.drawLines(batch, sortie, alpha);
        }
        if (massRelay != null) {
            // M9 part H: the mass relay's line, bolt and trail.
            massRelay.draw(batch, sortie, alpha);
        }
        if (walker != null) {
            // M7 part F: the homing missiles over their smoke trails, with the bullets.
            walker.drawMissiles(batch, sortie, alpha);
        } else if (diamonds != null) {
            // M9 part A: the Ascendancy's homing diamonds over their exhaust, with the bullets.
            diamonds.draw(batch, sortie, alpha);
        } else {
            // M8 part G: the Depth Hunters' torpedoes over their wakes, with the bullets.
            kinds.hunters(sortie).drawTorpedoes(batch, sortie, alpha);
        }
        props.drawBullets(batch, sortie, alpha);
        if (massRelay != null) {
            // M9 part H: the relay's flash over the field.
            massRelay.drawFlash(batch, sortie, alpha, play.whiteFlash);
        }
        screenFlash.draw(batch, sprites.pixel, alpha, play.whiteFlash);
        warnings.draw(batch, sortie.tick(), alpha);
        credits.draw(batch, font);
        setPieces.drawBossBar(batch, sortie);
    }

    /**
     * M8 part C: after the ship, Rook and the flyers: the under-water grade over the field ({@link UnderwaterGrade}),
     * then the light it must not dim: the ground's and the units' glows (held back while graded), the ice shaft's collar
     * glow, the deep units' lights (the eels' stripe and jaws, the Glassfins' glint, the pumps' sumps, the jellies'
     * pulse), the tender's lights and the hydro-kit's refit flashes at a waterline.
     */
    private void drawUnderGrade(
            SpriteBatch batch, Sortie sortie, float alpha, double scroll, double seconds, Wingman rook) {
        if (grade != null) {
            grade.draw(batch, sortie, alpha, scroll, clock.real());
            // M8 part F: the headlights' faint volume, so the cones read in empty black water.
            grade.drawHaze(batch);
        }
        // M8 part D: the ground pieces' lights over the grade (Level 24's relay beacons' pulse).
        backdrop.drawPieceGlows(batch, scroll, clock);
        if (graded) {
            ground.drawGroundGlows(batch, sortie, alpha);
            units.drawUnitGlows(batch, sortie, alpha);
            if (storm == null) {
                units.drawAirGlows(batch, sortie, alpha);
            }
        }
        backdrop.drawCanyonGlow(batch);
        units.drawDeepGlows(batch, sortie, alpha);
        // M8 part E: the Sirens' glow, their rings and the slowed hulls' shimmer; the Nautili's lip, crown and flecks.
        kinds.sirens(sortie).drawGlows(batch, sortie, alpha, clock.real());
        kinds.nautili(sortie).drawGlows(batch, sortie, alpha);
        if (maw != null) {
            // M8 part H: the Maw's teal lines, tells, sacs', implants' and head's glows, its death's flashes.
            maw.drawGlows(batch, sortie, alpha);
        }
        if (sovereign != null) {
            // M9 part H: the station's tells and lights, the core's glow and shield, its death's flashes.
            sovereign.drawGlows(batch, sortie, alpha, scroll);
        }
        if (tenderDrops != null) {
            tenderDrops.drawLights(batch, seconds);
        }
        if (buoys != null) {
            buoys.drawLights(batch, seconds, clock.real(), scroll);
        }
        if (sortie.flying()) {
            shipHydro.drawRefit(
                    batch,
                    seconds,
                    Math.round(X0 + sortie.ship().renderX(alpha)),
                    Math.round(sortie.ship().renderY(alpha)));
        }
        if (rook != null) {
            wingmanLooks.drawRefit(batch, rook, seconds, alpha);
        }
    }

    /** M8 part E: under the canyon's walls, over its floor: the Spiral Nautili leaving through the rock. */
    private void drawUnderWalls(SpriteBatch batch) {
        if (drawing != null) {
            units.drawUnderWalls(batch, drawing, drawingAlpha);
        }
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
        // M8 part A: over water, the harpoons' cables, the depth charges' lamps and bursts under the surface.
        deepWater.drawSub(batch, sortie, alpha, scroll);
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
                    player.hull(sortie).get(craft.bank() + ShipSpec.HARD_BANK),
                    Math.round(X0 + craft.renderX(alpha)),
                    Math.round(craft.renderY(alpha)),
                    Shadows.AIR_DX,
                    Shadows.AIR_DY,
                    1);
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null && !rescue.hides(sortie, rook)) {
            wingmanLooks.drawShadow(batch, rook, alpha);
        }
        deepWater.drawBulwarkShadow(batch, sortie, alpha);
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

    /** The frame being drawn: the screen's effects, the interpolation and what one pass hands to a later one. */
    private static final class Frame {
        Sortie sortie;
        Effects effects;
        Effects debris;
        Effects pieces;
        Effects blasts;
        SetPieceWrecks wrecks;
        CreditNumbers credits;
        EdgeWarnings warnings;
        float alpha;
        float shieldShimmer;
        ScreenFlash screenFlash;
        /** The time from the drawn moment to the last step, s. */
        double lag;
        /** The ground's scroll as drawn ({@link LevelRenderer#drawnScroll}). */
        double scroll;
        /** The script time as drawn, s. */
        double seconds;
        /** Whether a boss off the play plane has launched units, drawn over its hull. */
        boolean overHull;
        /** The ground and air units' auras (the Heralds'), or null. */
        AuraLooks auras;
    }
}
