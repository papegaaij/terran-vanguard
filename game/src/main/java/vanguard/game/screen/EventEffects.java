package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.LARGE_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.REMAINS_TICKS;
import static vanguard.game.screen.LevelScreen.SONDE_COLLECT;
import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.WATER_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.optionalFrames;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.EnumSet;
import java.util.List;
import vanguard.content.LevelData;
import vanguard.game.GameServices;
import vanguard.game.audio.DelayedSounds;
import vanguard.game.audio.FlightSounds;
import vanguard.game.render.AirborneWalkers;
import vanguard.game.render.CreditNumbers;
import vanguard.game.render.DeepDeaths;
import vanguard.game.render.DiamondLooks;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.ScreenFlash;
import vanguard.game.render.SetPieceWrecks;
import vanguard.game.render.TriggerBreak;
import vanguard.game.render.WeaponLooks;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Layer;
import vanguard.sim.Missiles;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * The play field's side of {@link LevelScreen}'s events: the impacts and glances, the kills with their
 * pieces, glows, remains and husks (on the water: bursts, ripples and sinkings), the ground targets'
 * wrecks and break-aparts, the set pieces' and bosses' chained deaths (the Kraken's and the Siege Spire's
 * own), the boss's credit shower, the splashes and flak puffs, and the renderer's hand-offs (a gun's
 * recoil, the Spire's pods and maw, Rook's hit flash). RF9e: the dispatch is here, the families are their own classes:
 * the water's ({@link WaterEffects}) and the deaths' ({@link DeathEffects}); a new family becomes another. M10 part A:
 * the mirrors' ({@link MirrorEffects}), the domes' ({@link DomeEffects}) and the regrowing turrets' ({@link
 * RegrowthEffects}); step A3b: the lightning strikes' ({@link StormEffects}) and the eruptions' ({@link
 * EruptionEffects}).
 */
final class EventEffects {
    private static final int IMPACT_FRAME_TICKS = 2;

    /** A destroyed ground target's 8 debris frames last 0.4 s. */
    private static final int DEBRIS_FRAME_TICKS = 3;

    static final int FLAK_FRAME_TICKS = 6;

    /** M5 part G: a Skimmer's flip and sink, 6 steps a frame (tools/art/skimmer.py: 1 s). */
    static final int SKIMMER_SINK_TICKS = 6;

    /** M5 part C: per kind the creep's wither after a periodic spawner's death (the Hive Node); empty for none. */
    private final List<Array<AtlasRegion>> withers;

    static final int MEDIUM_EXPLOSION_FRAME_TICKS = 3;

    private final Array<AtlasRegion> explosionMedium;

    /** M7 part F: a fizzled missile's puff. */
    private final Array<AtlasRegion> explosionTiny;

    /** Each ground object's break-apart, its look's {@code -break} frames, by index in the script. */
    private final List<Array<AtlasRegion>> groundBreaks;

    /**
     * Each ground object's wreck, left where it was destroyed: its look's wrecked frame (the third),
     * by index in the script; empty for a look without one (the cargo container).
     */
    private final List<Array<AtlasRegion>> groundWrecks;

    /** The spark of a shot glancing off a hardened target. */
    private final Array<AtlasRegion> glance;

    /** M5 part G: only a burst on a naval unit plays the water splash (Level 13's ice, {@code water_splash}). */
    private final boolean navalSplashOnly;

    /** M5 part F: a weather sonde's collect pop (tools/art/l12_props.py); empty without it. */
    private final Array<AtlasRegion> sondeCollect;

    private static final int SONDE_COLLECT_FRAME_TICKS = 3;

    private final GameServices services;
    private final Sortie sortie;
    private final EnemyLooks[] looks;
    private final WeaponLooks weaponLooks;
    private final AirborneWalkers airborne;
    private final Array<AtlasRegion> flak;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Effects debris;
    private final Effects pieces;
    private final Effects blasts;
    private final CreditNumbers creditNumbers;
    /** The layers on which an enemy was destroyed in this attempt (the screen's, for its prompts). */
    private final EnumSet<Layer> layersHit;

    /** RF9e: the water and the death families, and their shared start (a floe's drift). */
    private final EffectStarts starts;

    private final WaterEffects water;
    private final DeathEffects deaths;
    /** M10 part A: Act 6's families: the mirrors', the domes' and the regrowing turrets'. */
    private final MirrorEffects mirrors;

    private final DomeEffects domes;
    private final RegrowthEffects regrowth;
    /** M10 part A, step A3b: the Jovian hazards' families: the lightning strikes' and the eruptions'. */
    private final StormEffects storms;

    private final EruptionEffects eruptions;

    EventEffects(
            GameServices services,
            LevelData level,
            Sortie sortie,
            EnemyLooks[] looks,
            WeaponLooks weaponLooks,
            AirborneWalkers airborne,
            Array<AtlasRegion> flak,
            FlightSounds sounds,
            LevelRenderer renderer,
            Effects effects,
            Effects debris,
            Effects pieces,
            Effects blasts,
            SetPieceWrecks wrecks,
            CreditNumbers creditNumbers,
            ScreenFlash screenFlash,
            DelayedSounds chainSounds,
            EnumSet<Layer> layersHit) {
        this.services = services;
        this.sortie = sortie;
        this.looks = looks;
        this.weaponLooks = weaponLooks;
        this.airborne = airborne;
        this.flak = flak;
        this.renderer = renderer;
        this.effects = effects;
        this.debris = debris;
        this.pieces = pieces;
        this.blasts = blasts;
        this.creditNumbers = creditNumbers;
        this.layersHit = layersHit;
        withers = java.util.Arrays.stream(looks)
                .map(look -> EnemyLooks.wither(look.creep(), REMAINS_TICKS))
                .toList();
        glance = services.sprites.frames("ballistic-impact");
        explosionMedium = services.sprites.frames("explosion-medium");
        explosionTiny = optionalFrames(services, "explosion-tiny");
        // A trigger is never destroyed (it is spent and stays, a billboard's toppled frame its own
        // wreck): no break-apart, no wreck. M7 part C: a look without break-apart frames (Level 16's
        // cargo rovers: intact, damaged, wreck) leaves only its wreck frame and the small explosion.
        groundBreaks = sortie.script().groundObjects().stream()
                .map(spec ->
                        spec.trigger() ? new Array<AtlasRegion>() : optionalFrames(services, spec.look() + "-break"))
                .toList();
        groundWrecks = sortie.script().groundObjects().stream()
                .map(spec -> spec.trigger() ? new Array<AtlasRegion>() : services.sprites.frames(spec.look()))
                .map(frames -> frames.size > 2 ? Array.with(frames.get(2)) : new Array<AtlasRegion>())
                .toList();
        navalSplashOnly = level.navalSplashOnly();
        sondeCollect = optionalFrames(services, SONDE_COLLECT);
        starts = new EffectStarts(sortie);
        water = new WaterEffects(
                services,
                sortie,
                looks,
                weaponLooks,
                airborne,
                sounds,
                renderer,
                effects,
                pieces,
                starts,
                navalSplashOnly);
        deaths = new DeathEffects(
                services,
                sortie,
                looks,
                sounds,
                renderer,
                effects,
                debris,
                pieces,
                wrecks,
                screenFlash,
                chainSounds,
                explosionMedium,
                water,
                starts);
        mirrors = new MirrorEffects(sortie, renderer, effects, glance, services.sprites.explosionTiny);
        domes = new DomeEffects(sortie, renderer, effects, glance);
        regrowth =
                new RegrowthEffects(sortie, effects, services.sprites.explosionSmall, services.sprites.explosionTiny);
        storms = new StormEffects(sortie, renderer, effects, services.sprites.explosionTiny);
        eruptions = new EruptionEffects(
                sortie,
                renderer,
                weaponLooks,
                effects,
                services.sprites.explosionTiny,
                services.sprites.explosionSmall);
    }

    /**
     * The event whose effects event type {@code type} draws: a destroyed neutral craft (Level 29's wavering Stormhawks,
     * {@link SimEvents.Type#NEUTRAL_DOWN}) bursts as its kind's kill does (step FIX6: the user's playthrough
     * 2026-10-30, they vanished); M10 part A: a unit lost to the three-way war ({@link SimEvents.Type#PREY_KILLED}) dies as a
     * kill does (design/audio/sfx: the war's kills play the prey's rung as any death); every other type draws its own.
     */
    static SimEvents.Type drawnAs(SimEvents.Type type) {
        return type == SimEvents.Type.NEUTRAL_DOWN || type == SimEvents.Type.PREY_KILLED
                ? SimEvents.Type.ENEMY_DESTROYED
                : type;
    }

    /** Event {@code i}'s effects (in the step's event order, so the effects start in it). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (drawnAs(events.type(i))) {
            case ENEMY_HIT -> {
                // M8 part A: a harpoon's impact is DeepWaterLooks' (its cavity under it).
                if (!weaponLooks.ownImpact(events.value(i)) && !water.waterImpact(events.value(i), x, y)) {
                    effects.start(weaponLooks.impact(events.value(i)), IMPACT_FRAME_TICKS, x, y);
                }
            }
            case GROUND_HIT -> {
                if (!weaponLooks.ownImpact(events.value(i)) && !water.waterImpact(events.value(i), x, y)) {
                    effects.startOnGround(
                            weaponLooks.impact(events.value(i)), IMPACT_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case SHOT_GLANCED -> effects.start(glance, IMPACT_FRAME_TICKS, x, y);
            case BLAST -> {
                if (weaponLooks.depthCharge(events.value(i))) {
                    // M8 part A: a depth charge's pressure dome is DeepWaterLooks'.
                    return;
                }
                // A bomb's or shell's burst is the small explosion; a mine bursts in its own blast.
                Array<AtlasRegion> blast = weaponLooks.blast(events.value(i), services.sprites.explosionSmall);
                if (blast == services.sprites.explosionSmall
                        && !water.arenaSolid(x, y)
                        && water.splashes(x, y)
                        && water.waterBurst("small", x, y)) {
                    // M5 part E: over the water a bomb or shell lands in a splash (it hits surfaced units
                    // only); on the Kraken's surfaced flesh or Tiamat's deck it bursts as on land.
                    return;
                }
                effects.start(
                        blast,
                        blast == services.sprites.explosionSmall
                                ? TINY_EXPLOSION_FRAME_TICKS
                                : MEDIUM_EXPLOSION_FRAME_TICKS,
                        x,
                        y);
            }
            case ENEMY_DESTROYED -> {
                int kind = events.value(i);
                EnemyLooks look = looks[kind];
                // M5 part C: a Ravager shot in its pounce's air window dies in the air.
                Layer layer =
                        airborne.layerOf(kind, sortie.enemyKinds().get(kind).layer(), x, y);
                layersHit.add(layer);
                if (renderer.overWater()
                        && (layer == Layer.GROUND || layer == Layer.SUB)
                        && (!navalSplashOnly || look.naval().twoLayers())) {
                    // M5 part E: a kill on the water bursts and sinks: no crater, no wreck. M5 part G:
                    // on Level 13's ice only a naval unit's (a Skimmer's); the walkers die on the floes.
                    water.waterDeath(kind, x, y, layer == Layer.SUB);
                    return;
                }
                // M5 part G: a unit on a drifting floe (Level 13's mortars) dies on it: its burst and
                // remains drift on with the floe instead of staying where it was shot.
                boolean drifting = i + 1 < events.size() && events.type(i + 1) == SimEvents.Type.WRECK_DRIFT;
                starts.drift(
                        drifting ? SimEvents.driftX(events.value(i + 1)) : 0,
                        drifting ? SimEvents.driftY(events.value(i + 1)) : 0);
                if (deaths.deepDeath(kind, x, y, layer == Layer.GROUND)) {
                    // M8 part C: a kill below the waterline bursts under water (DeepDeaths): no fireball, no smoke.
                } else if (deaths.vacuumDeath(kind, x, y, layer == Layer.GROUND)) {
                    // M9 part A: a kill in vacuum flashes and throws its debris in a ring (VacuumDeaths): no smoke.
                } else {
                    deaths.death(kind, x, y, layer == Layer.GROUND);
                }
                if (layer == Layer.GROUND && !withers.get(kind).isEmpty()) {
                    // The Hive Node's creep withers over 2 s under its stump, then lies dry.
                    debris.startOnGround(
                            withers.get(kind),
                            EnemyLooks.WITHER_FRAME_TICKS,
                            x,
                            y,
                            0,
                            sortie.groundScroll(),
                            starts.driftX(),
                            starts.driftY());
                }
                if (layer == Layer.GROUND && !look.remains().isEmpty()) {
                    debris.startOnGround(
                            look.remains(),
                            REMAINS_TICKS / look.remains().size,
                            x,
                            y,
                            0,
                            sortie.groundScroll(),
                            starts.driftX(),
                            starts.driftY());
                }
                starts.drift(0, 0);
            }
            case BROOD_BURST -> {
                // A self-burst: the pod's death animation without a kill.
                deaths.death(events.value(i), x, y);
            }
            case WALKER_DOWN -> {
                // The legless husk at its last heading, left on the ground like a turret's stump.
                int value = events.value(i);
                EnemyLooks look = looks[SimEvents.walkerKind(value)];
                if (!look.husks().isEmpty()) {
                    debris.startOnGround(
                            look.husk(SimEvents.walkerFacing(value)), REMAINS_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case LANE_WALKER_DOWN -> {
                // M5 part G: a Skimmer's hull flips and sinks at its heading, in its foam ring.
                int value = events.value(i);
                Array<AtlasRegion> sink =
                        looks[SimEvents.walkerKind(value)].naval().sinkAt(SimEvents.walkerFacing(value));
                if (sink.size > 0 && renderer.overWater()) {
                    renderer.surfaceWater().startOnGround(sink, SKIMMER_SINK_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case CLAMP_HIT -> effects.start(glance, IMPACT_FRAME_TICKS, x, y);
            case DEBRIS_HIT -> effects.start(weaponLooks.impact(events.value(i)), IMPACT_FRAME_TICKS, x, y);
            case DEBRIS_DESTROYED, MINE_BURST, MINE_DESTROYED ->
                effects.start(services.sprites.explosionTiny, TINY_EXPLOSION_FRAME_TICKS, x, y);
            // M7 part D: a Shellback's vent bursts on the ground (its dead stump stays: ShellbackLooks).
            case UNIT_PART_DESTROYED ->
                effects.startOnGround(
                        services.sprites.explosionSmall, TINY_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
            case PART_DESTROYED -> {
                if (!water.arenaPartDown(events.value(i), x, y)) {
                    if (ringBoss() && services.sprites.has("explosion-vacuum-medium")) {
                        // M9 part H: a battery of the Iron Sovereign bursts in vacuum (no fire, no rising smoke).
                        effects.startOnGround(
                                services.sprites.frames("explosion-vacuum-medium"),
                                MEDIUM_EXPLOSION_FRAME_TICKS,
                                x,
                                y,
                                0,
                                sortie.groundScroll());
                    } else if (arenaBoss()) {
                        // Round 41 (the 2026-10-19 playtest): an anchored boss's part (the Spire's core in its
                        // kill step) bursts on the ground and scrolls away with its wreck once the scroll resumes.
                        effects.startOnGround(
                                explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                    } else {
                        effects.start(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y);
                    }
                }
            }
            case SET_PIECE_DESTROYED -> {
                deaths.setPieceDeath(events.value(i), x, y);
            }
            case SHOT_FIRED -> {
                Array<AtlasRegion> splash = weaponLooks.dropSplash(events.value(i));
                if (splash != null && renderer.overWater()) {
                    // M5 part E: a torpedo drops into the water under its pod.
                    renderer.surfaceWater().startOnGround(splash, WATER_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case ENEMY_FIRED -> renderer.enemyFired(x, y, sortie.tick());
            // M5 part H: the Siege Spire's mortar pods open for their lobs, its maw flares at a launch.
            case MORTAR_LOBBED -> renderer.lobbed(sortie, x, y, sortie.tick());
            case BOSS_LAUNCHED -> renderer.bossLaunched(sortie.tick());
            case ALLY_HIT -> {
                if (sortie.navalConvoy()) {
                    // M5 part E: a slam's blast on the hull.
                    water.waterBurst("medium", x, y, false);
                }
            }
            case ALLY_FLAK -> {
                renderer.flak(events.value(i), sortie.tick());
                if (flak.size > 0) {
                    renderer.lowAir().start(flak, FLAK_FRAME_TICKS, x, y);
                }
            }
            case GROUND_DESTROYED -> {
                int object = events.value(i);
                if (!groundWrecks.get(object).isEmpty()) {
                    debris.startOnGround(groundWrecks.get(object), REMAINS_TICKS, x, y, 0, sortie.groundScroll());
                }
                if (!groundBreaks.get(object).isEmpty()) {
                    debris.startOnGround(groundBreaks.get(object), DEBRIS_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
                if (!water.splashes(x, y) || !water.waterBurst("small", x, y)) {
                    // M8 part C: under the water (Level 23's probe) the under-water small burst.
                    effects.startOnGround(
                            renderer.deepDeaths().present() && renderer.underwaterAt(y)
                                    ? renderer.deepDeaths().smallFlash()
                                    : services.sprites.explosionSmall,
                            TINY_EXPLOSION_FRAME_TICKS,
                            x,
                            y,
                            0,
                            sortie.groundScroll());
                }
            }
            case TRIGGER_SPENT -> {
                // A trigger whose spent frame is a wreck (Level 08's billboard topples) bursts in a
                // destructible's small explosion; its spent frame stays as the wreck (TriggerBreak).
                // M7 part C: the relay hut's door bursts the same way, its burst frame staying.
                GroundObjectSpec spent = sortie.script().groundObjects().get(events.value(i));
                if (TriggerBreak.breaks(spent) || TriggerBreak.doorBursts(spent)) {
                    effects.startOnGround(
                            services.sprites.explosionSmall,
                            TINY_EXPLOSION_FRAME_TICKS,
                            x,
                            y,
                            0,
                            sortie.groundScroll());
                }
            }
            case CREDITS_PICKED_UP -> creditNumbers.show(events.value(i), x, y);
            case CHAIN_POP -> {
                // A chained death's burst: the member's own death, as when it is shot (no kill), in
                // the same step as its sound; until then the member was drawn intact.
                deaths.death(SimEvents.chainPopKind(events.value(i)), x, y);
            }
            case CHAIN_CUT, CHAIN_REGROWN ->
                // M8 part C: an eel's cut under water bursts in the under-water small burst.
                effects.start(
                        renderer.deepDeaths().present() && renderer.underwaterAt(y)
                                ? renderer.deepDeaths().smallFlash()
                                : services.sprites.explosionSmall,
                        TINY_EXPLOSION_FRAME_TICKS,
                        x,
                        y);
            case AIRSTRIKE_BLAST -> {
                if (!water.splashes(x, y) || !water.waterBurst("medium", x, y)) {
                    blasts.startOnGround(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case ALLY_LOST -> {
                if (sortie.navalConvoy()) {
                    // M5 part E: a hull goes down in a water burst (its sinking is ConvoyLooks').
                    water.waterBurst("large", x, y, false);
                } else if (sortie.defending()) {
                    // M5 part G: the relay blows up on its floe (its dark frame and smoke column are RelayLooks').
                    blasts.startOnGround(
                            services.sprites.explosionLarge,
                            LARGE_EXPLOSION_FRAME_TICKS,
                            x,
                            y,
                            0,
                            sortie.groundScroll());
                } else if (sortie.airEscort()) {
                    // M5 part D: a shuttle bursts in the air and glides down into far (ShuttleLooks).
                    effects.start(services.sprites.explosionSmall, TINY_EXPLOSION_FRAME_TICKS, x, y);
                } else {
                    // It burns on the road: the blast stays where it was, scrolling with the ground.
                    blasts.startOnGround(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            // Rook (M5 part A): his hull flashes on a hit; his low-armour bark; as he ejects his
            // craft bursts in the medium explosion and he shouts his eject bark.
            case WINGMAN_HIT -> renderer.wingmanHit(sortie.tick());
            case SONDE_COLLECTED -> {
                if (sondeCollect.size > 0) {
                    effects.start(sondeCollect, SONDE_COLLECT_FRAME_TICKS, x, y);
                }
            }
            // M5 part H, step H7b: the boss's credit shower after its chained bursts.
            case BOSS_DESTROYED -> creditShower(events.value(i), x, y);
            // M7 part F: a homing missile shot down or burst on a hull bursts small; one whose life ran out puffs out.
            case MISSILE_DESTROYED -> missileGone(events.value(i), x, y);
            case WINGMAN_EJECTED -> effects.start(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y);
            case SHIP_DESTROYED -> effects.start(services.sprites.explosionLarge, LARGE_EXPLOSION_FRAME_TICKS, x, y);
            // M10 part A: Act 6's mechanics, each family its own class.
            case MIRROR_BOUNCED, MIRROR_ABSORBED, MIRROR_BROKEN, MIRROR_REGROWING -> mirrors.react(events, i);
            case DOME_HIT, DOME_DOWN, DOME_UP, DOME_COLLAPSED -> domes.react(events, i);
            case ROOT_EXPOSED, ROOT_KILLED, KNITTING, REGROWN -> regrowth.react(events, i);
            // The three-way war's teal hit where it lands; the spoofed turret's amber stutter (JovianLooks).
            case PREY_HIT -> renderer.jovian().preyHit(x, y, sortie.tick());
            case SPOOF_HESITATE -> renderer.jovian().hesitate(sortie, x, y);
            // M10 part A, step A3b: the Jovian hazards, each family its own class.
            case STRIKE_TOLD, STRIKE, STRIKE_HIT -> storms.react(events, i);
            case VENT_ERUPTED, ROCK_HIT, ROCK_DESTROYED, ROCK_STRUCK -> eruptions.react(events, i);
            default -> {}
        }
    }

    /**
     * M7 part F: a homing missile shot down or burst on a hull bursts small; one whose life ran out puffs out. M8 part G:
     * under the water (the Depth Hunter's torpedoes) the under-water bursts: the {@code small} one over its cavity, the
     * {@code tiny} one for a torpedo that ran out.
     */
    private void missileGone(int cause, double x, double y) {
        DeepDeaths deep = renderer.deepDeaths();
        boolean fizzled = cause == Missiles.FIZZLED;
        DiamondLooks diamonds = renderer.diamondLooks();
        if (diamonds != null) {
            // M9 part A: a homing diamond pops (shot down or burst on a hull) or fizzles at its life's end.
            effects.start(
                    fizzled ? diamonds.fizzle() : diamonds.pop(),
                    fizzled ? DiamondLooks.FIZZLE_FRAME_TICKS : DiamondLooks.POP_FRAME_TICKS,
                    x,
                    y);
            return;
        }
        if (deep.present() && renderer.underwaterAt(y)) {
            if (!fizzled) {
                pieces.start(deep.smallCavity(), TINY_EXPLOSION_FRAME_TICKS, x, y);
            }
            effects.start(fizzled ? deep.tinyFlash() : deep.smallFlash(), TINY_EXPLOSION_FRAME_TICKS, x, y);
            return;
        }
        effects.start(fizzled ? explosionTiny : services.sprites.explosionSmall, TINY_EXPLOSION_FRAME_TICKS, x, y);
    }

    /** M9 part H: whether the level has a ringed boss (the Iron Sovereign), whose batteries burst in vacuum. */
    private boolean ringBoss() {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k).ring().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** Whether the level has an anchored arena boss (the Kraken, the Siege Spire), whose parts lie on the ground. */
    private boolean arenaBoss() {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k).arena().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** The coins of a boss's credit shower fly out this many steps apart, in a ring of this many. */
    private static final int SHOWER_STEP_TICKS = 3;

    private static final int SHOWER_COINS = 16;

    /** A coin's spin: the pickups' 10 fps. */
    private static final int SHOWER_FRAME_TICKS = 6;

    /**
     * A boss's credit shower (design/enemies/bosses: the death sequence): its credits as a number
     * over the bell and a ring of salvage coins spinning out after the chained bursts.
     */
    private void creditShower(int credits, double x, double y) {
        int delay = deaths.showerDelay();
        creditNumbers.show(credits, x, y);
        for (int c = 0; c < SHOWER_COINS; c++) {
            double angle = 2 * Math.PI * c / SHOWER_COINS;
            double reach = 40 + 50 * (c % 3);
            effects.start(
                    services.sprites.salvageSmall,
                    SHOWER_FRAME_TICKS,
                    x + Math.cos(angle) * reach,
                    y + Math.sin(angle) * reach,
                    delay + c * SHOWER_STEP_TICKS,
                    1,
                    1);
        }
    }
}
