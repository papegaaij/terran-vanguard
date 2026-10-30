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
import vanguard.game.audio.Sfx;
import vanguard.game.render.AirborneWalkers;
import vanguard.game.render.CreditNumbers;
import vanguard.game.render.DeepDeaths;
import vanguard.game.render.DiamondLooks;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.ScreenFlash;
import vanguard.game.render.SetPieceDeath;
import vanguard.game.render.SetPieceWrecks;
import vanguard.game.render.TriggerBreak;
import vanguard.game.render.VacuumDeaths;
import vanguard.game.render.WeaponLooks;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Layer;
import vanguard.sim.Missiles;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * The play field's side of {@link LevelScreen}'s events: the impacts and glances, the kills with their
 * pieces, glows, remains and husks (on the water: bursts, ripples and sinkings), the ground targets'
 * wrecks and break-aparts, the set pieces' and bosses' chained deaths (the Kraken's and the Siege Spire's
 * own), the boss's credit shower, the splashes and flak puffs, and the renderer's hand-offs (a gun's
 * recoil, the Spire's pods and maw, Rook's hit flash).
 */
final class EventEffects {
    private static final int IMPACT_FRAME_TICKS = 2;

    /** A destroyed ground target's 8 debris frames last 0.4 s. */
    private static final int DEBRIS_FRAME_TICKS = 3;

    /**
     * M5 part G: the drift (px/s across and up) of the ground unit whose death {@link #react} is starting,
     * from its {@link SimEvents.Type#WRECK_DRIFT}; 0 otherwise.
     */
    private double wreckDriftX;

    private double wreckDriftY;

    private static final int RIPPLE_FRAME_TICKS = 8;

    private static final int RIPPLE_DELAY_TICKS = 12;

    static final int FLAK_FRAME_TICKS = 6;

    private static final int RAFT_SINK_TICKS = 6;

    private static final int ARM_POP_FRAME_TICKS = 4;

    private static final int ARM_POP_STEP_TICKS = 3;

    private static final int KRAKEN_DEATH_FRAME_TICKS = 8;

    private static final double KRAKEN_DEATH_BELOW_HEAD = 40;

    /** A torpedo's impact is under the water when a submerged unit lies within this of it, px. */
    private static final double UNDER_REACH = 24;

    /** M5 part E: how far beside a surfaced part's hit box a hit still counts as on it, px. */
    private static final double SOLID_REACH = 8;

    /** M5 part C: per kind the creep's wither after a periodic spawner's death (the Hive Node); empty for none. */
    private final List<Array<AtlasRegion>> withers;

    /** M5 part E: the shared ripple train, the frigate's flak puff and a severed arm's pop; empty without them. */
    private final Array<AtlasRegion> ripple;

    private final Array<AtlasRegion> armPop;

    /** A set piece's death: a medium burst at each part, one after the other, then a large one at its centre. */
    private static final int CHAIN_STEP_TICKS = 6;

    private static final int MEDIUM_EXPLOSION_FRAME_TICKS = 3;

    /** A set piece's death cloud (the Leviathan's ichor) shows each frame for 6 steps. */
    private static final int DEATH_CLOUD_FRAME_TICKS = 6;

    /** A boss's crown petals tearing off: 10 fps. */
    private static final int PETAL_FRAME_TICKS = 6;

    private final Array<AtlasRegion> explosionMedium;

    /** M7 part F: a fizzled missile's puff. */
    private final Array<AtlasRegion> explosionTiny;

    /** Each set piece's death cloud, its slug's {@code -ichor} frames, by index in the script; empty for none. */
    private final List<Array<AtlasRegion>> deathClouds;

    /** Each ground object's break-apart, its look's {@code -break} frames, by index in the script. */
    private final List<Array<AtlasRegion>> groundBreaks;

    /**
     * Each ground object's wreck, left where it was destroyed: its look's wrecked frame (the third),
     * by index in the script; empty for a look without one (the cargo container).
     */
    private final List<Array<AtlasRegion>> groundWrecks;

    /** The spark of a shot glancing off a hardened target. */
    private final Array<AtlasRegion> glance;

    /** The steps from a boss's death to its credit shower: the end of its chained bursts. */
    private int showerDelay = SHOWER_DELAY_TICKS;

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
    private final FlightSounds sounds;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Effects debris;
    private final Effects pieces;
    private final Effects blasts;
    private final SetPieceWrecks wrecks;
    private final CreditNumbers creditNumbers;
    private final ScreenFlash screenFlash;
    private final DelayedSounds chainSounds;
    /** The layers on which an enemy was destroyed in this attempt (the screen's, for its prompts). */
    private final EnumSet<Layer> layersHit;

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
        this.sounds = sounds;
        this.renderer = renderer;
        this.effects = effects;
        this.debris = debris;
        this.pieces = pieces;
        this.blasts = blasts;
        this.wrecks = wrecks;
        this.creditNumbers = creditNumbers;
        this.screenFlash = screenFlash;
        this.chainSounds = chainSounds;
        this.layersHit = layersHit;
        withers = java.util.Arrays.stream(looks)
                .map(look -> EnemyLooks.wither(look.creep(), REMAINS_TICKS))
                .toList();
        glance = services.sprites.frames("ballistic-impact");
        explosionMedium = services.sprites.frames("explosion-medium");
        explosionTiny = optionalFrames(services, "explosion-tiny");
        ripple = optionalFrames(services, "water-ripple");
        armPop = optionalFrames(services, "harbour-kraken-arm-death");
        deathClouds = sortie.script().setPieces().stream()
                .map(spec -> services.sprites.has(spec.slug() + "-ichor")
                        ? services.sprites.frames(spec.slug() + "-ichor")
                        : new Array<AtlasRegion>())
                .toList();
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
    }

    /**
     * The event whose effects event type {@code type} draws: a destroyed neutral craft (Level 29's wavering Stormhawks,
     * {@link SimEvents.Type#NEUTRAL_DOWN}) bursts as its kind's kill does (step FIX6: the user's playthrough
     * 2026-10-30, they vanished); every other type draws its own.
     */
    static SimEvents.Type drawnAs(SimEvents.Type type) {
        return type == SimEvents.Type.NEUTRAL_DOWN ? SimEvents.Type.ENEMY_DESTROYED : type;
    }

    /** Event {@code i}'s effects (in the step's event order, so the effects start in it). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (drawnAs(events.type(i))) {
            case ENEMY_HIT -> {
                // M8 part A: a harpoon's impact is DeepWaterLooks' (its cavity under it).
                if (!weaponLooks.ownImpact(events.value(i)) && !waterImpact(events.value(i), x, y)) {
                    effects.start(weaponLooks.impact(events.value(i)), IMPACT_FRAME_TICKS, x, y);
                }
            }
            case GROUND_HIT -> {
                if (!weaponLooks.ownImpact(events.value(i)) && !waterImpact(events.value(i), x, y)) {
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
                        && !arenaSolid(x, y)
                        && splashes(x, y)
                        && waterBurst("small", x, y)) {
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
                    waterDeath(kind, x, y, layer == Layer.SUB);
                    return;
                }
                // M5 part G: a unit on a drifting floe (Level 13's mortars) dies on it: its burst and
                // remains drift on with the floe instead of staying where it was shot.
                boolean drifting = i + 1 < events.size() && events.type(i + 1) == SimEvents.Type.WRECK_DRIFT;
                wreckDriftX = drifting ? SimEvents.driftX(events.value(i + 1)) : 0;
                wreckDriftY = drifting ? SimEvents.driftY(events.value(i + 1)) : 0;
                if (deepDeath(kind, x, y, layer == Layer.GROUND)) {
                    // M8 part C: a kill below the waterline bursts under water (DeepDeaths): no fireball, no smoke.
                } else if (vacuumDeath(kind, x, y, layer == Layer.GROUND)) {
                    // M9 part A: a kill in vacuum flashes and throws its debris in a ring (VacuumDeaths): no smoke.
                } else {
                    death(kind, x, y, layer == Layer.GROUND);
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
                            wreckDriftX,
                            wreckDriftY);
                }
                if (layer == Layer.GROUND && !look.remains().isEmpty()) {
                    debris.startOnGround(
                            look.remains(),
                            REMAINS_TICKS / look.remains().size,
                            x,
                            y,
                            0,
                            sortie.groundScroll(),
                            wreckDriftX,
                            wreckDriftY);
                }
                wreckDriftX = wreckDriftY = 0;
            }
            case BROOD_BURST -> {
                // A self-burst: the pod's death animation without a kill.
                death(events.value(i), x, y);
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
                if (!arenaPartDown(events.value(i), x, y)) {
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
                if (LevelRenderer.spire(sortie.setPiece(events.value(i)))) {
                    spireDeath(events.value(i), x, y);
                } else if (sortie.setPiece(events.value(i)).stride().isPresent()) {
                    walkerDeath(events.value(i), x, y);
                } else if (sortie.setPiece(events.value(i)).breach().isPresent()) {
                    colossusDeath(events.value(i), x, y);
                } else if (sortie.setPiece(events.value(i)).ring().isPresent()) {
                    ringDeath(events.value(i), x, y);
                } else if (sortie.setPiece(events.value(i)).arena().isPresent()) {
                    arenaDeath(events.value(i), x, y);
                } else {
                    chainedDeath(events.value(i), deathClouds.get(events.value(i)), x, y);
                }
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
                    waterBurst("medium", x, y, false);
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
                if (!splashes(x, y) || !waterBurst("small", x, y)) {
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
                death(SimEvents.chainPopKind(events.value(i)), x, y);
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
                if (!splashes(x, y) || !waterBurst("medium", x, y)) {
                    blasts.startOnGround(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case ALLY_LOST -> {
                if (sortie.navalConvoy()) {
                    // M5 part E: a hull goes down in a water burst (its sinking is ConvoyLooks').
                    waterBurst("large", x, y, false);
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

    /**
     * M5 part E: a torpedo's impact (mount {@code mount}) at (x, y): under the water when it struck a
     * unit there, else on the surface; false for a weapon that does not run under the water.
     */
    private boolean waterImpact(int mount, double x, double y) {
        Array<AtlasRegion> surface = weaponLooks.waterImpact(mount, false);
        if (surface == null || !renderer.overWater() || !splashes(x, y)) {
            return false;
        }
        boolean under = airborne.underNear(x, y, UNDER_REACH) || arenaUnder(x, y);
        if (!under && arenaSolid(x, y)) {
            // A hit on the Kraken's surfaced head or arm is on flesh: the normal impact (round 33, user).
            return false;
        }
        sounds.waterExplosion(under, x);
        Array<AtlasRegion> frames = under ? weaponLooks.waterImpact(mount, true) : surface;
        if (frames != null) {
            (under ? renderer.underWater() : renderer.surfaceWater())
                    .startOnGround(frames, WATER_FRAME_TICKS, x, y, 0, sortie.groundScroll());
        }
        return true;
    }

    /**
     * M5 part E: whether (x, y) lies on what of an arena boss is above the water: its platform's deck,
     * a surfaced part (the Kraken's head up) or a slam arm's segment out of the water (rising or
     * awash). A hit there is on flesh or steel and shows the normal impact, not the sea's splash and
     * ripples (round 33, user, 2026-10-09).
     */
    private boolean arenaSolid(double x, double y) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.arena().isEmpty() || !piece.present()) {
                continue;
            }
            double cx = piece.renderX(1);
            double cy = piece.renderY(1);
            if (Math.abs(x - cx) <= piece.body().width() / 2
                    && Math.abs(y - cy) <= piece.body().height() / 2) {
                return true;
            }
            for (int p = 0; p < piece.partCount(); p++) {
                var box = piece.spec().parts().get(p).box();
                if (piece.partLayer(p) != Layer.SUB
                        && !piece.partWrecked(p)
                        && Math.abs(piece.partX(p) - x) <= box.width() / 2 + SOLID_REACH
                        && Math.abs(piece.partY(p) - y) <= box.height() / 2 + SOLID_REACH) {
                    return true;
                }
            }
            var chains = piece.boss().orElseThrow().chains();
            int start = 0;
            for (var chain : chains) {
                int part = chain.part();
                if (piece.partLayer(part) != Layer.SUB && !piece.partWrecked(part)) {
                    for (int i = start; i < start + chain.segments(); i++) {
                        if (Math.abs(cx + piece.segmentOffsetX(i) - x)
                                        <= chain.box().width() / 2 + SOLID_REACH
                                && Math.abs(cy + piece.segmentOffsetY(i) - y)
                                        <= chain.box().height() / 2 + SOLID_REACH) {
                            return true;
                        }
                    }
                }
                start += chain.segments();
            }
        }
        return false;
    }

    /** M5 part E: whether (x, y) lies on a submerged part of an arena boss (the Kraken's head or arms under the water). */
    private boolean arenaUnder(double x, double y) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.arena().isEmpty() || !piece.present()) {
                continue;
            }
            for (int p = 0; p < piece.partCount(); p++) {
                var box = piece.spec().parts().get(p).box();
                if (piece.partLayer(p) == Layer.SUB
                        && Math.abs(piece.partX(p) - x) <= box.width() / 2 + UNDER_REACH
                        && Math.abs(piece.partY(p) - y) <= box.height() / 2 + UNDER_REACH) {
                    return true;
                }
            }
        }
        return false;
    }

    /** M5 part G: a Skimmer's flip and sink, 6 steps a frame (tools/art/skimmer.py: 1 s). */
    static final int SKIMMER_SINK_TICKS = 6;

    /** M5 part G: a burst counts as on a naval unit within this reach of its hit box, px. */
    static final double NAVAL_REACH = 8;

    /**
     * M5 part G (user decision G5 = a): whether a burst at (x, y) is the water splash: on a water level
     * every one, but on a {@code water_splash: naval-only} level (Level 13's ice) only one on a naval unit
     * (a unit with its own water layer, the Skimmer); every other is the ground burst.
     */
    private boolean splashes(double x, double y) {
        if (!navalSplashOnly) {
            return true;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            var enemy = sortie.enemy(i);
            if (looks[enemy.kind()].naval().twoLayers()
                    && Math.abs(enemy.renderX(1) - x) <= enemy.hitbox().width() / 2 + NAVAL_REACH
                    && Math.abs(enemy.renderY(1) - y) <= enemy.hitbox().height() / 2 + NAVAL_REACH) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part E: over the water, a burst of the {@code rung} size on its surface at (x, y) with its
     * ripple train after it; false (nothing started) over land.
     */
    private boolean waterBurst(String rung, double x, double y) {
        return waterBurst(rung, x, y, true);
    }

    /** As {@link #waterBurst(String, double, double)}, with the water explosion's sound or without (a ship's hit and sinking have their own). */
    private boolean waterBurst(String rung, double x, double y, boolean sound) {
        if (!renderer.overWater()) {
            return false;
        }
        if (sound) {
            sounds.waterExplosion(false, x);
        }
        String name = "explosion-water-" + rung;
        if (services.sprites.has(name)) {
            renderer.surfaceWater()
                    .startOnGround(services.sprites.frames(name), WATER_FRAME_TICKS, x, y, 0, sortie.groundScroll());
        }
        ripple(x, y, RIPPLE_DELAY_TICKS);
        return true;
    }

    /** M5 part E: the shared ripple train on the sea at (x, y), after {@code delay} steps. */
    private void ripple(double x, double y, int delay) {
        if (ripple.size > 0) {
            renderer.surfaceWater().startOnGround(ripple, RIPPLE_FRAME_TICKS, x, y, delay, sortie.groundScroll());
        }
    }

    /**
     * M5 part E: a kill on the water (design/art-direction, Water): on the surface its pieces and glow
     * with its tier's water burst and a ripple train, a raft sinking under its gun; under the water
     * ({@code under}) only its under-water burst, through the sub pass. No crater, no wreck.
     */
    private void waterDeath(int kind, double x, double y, boolean under) {
        EnemyLooks look = looks[kind];
        double scroll = sortie.groundScroll();
        sounds.waterExplosion(under, x);
        if (under) {
            Array<AtlasRegion> burst = look.naval().under();
            if (!burst.isEmpty()) {
                renderer.underWater().startOnGround(burst, WATER_FRAME_TICKS, x, y, 0, scroll);
            }
            return;
        }
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        start(pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), true);
        Array<AtlasRegion> burst = look.naval().water();
        if (!burst.isEmpty()) {
            renderer.surfaceWater().startOnGround(burst, WATER_FRAME_TICKS, x, y, 0, scroll);
        } else {
            start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, true);
        }
        EnemyLooks.DeathEffect glow = look.deathGlow();
        start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), true);
        if (look.naval().skims()) {
            // M5 part G: a Skimmer's small burst over its water splash; its flip and sink follow its
            // LANE_WALKER_DOWN at its heading.
            start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, true);
        } else if (!look.naval().sink().isEmpty()) {
            // The gunless raft sinks over its plain body, which fades under the water with it.
            renderer.surfaceWater().startOnGround(look.naval().sink(), RAFT_SINK_TICKS, x, y, 0, scroll);
            if (!look.naval().raftSub().isEmpty()) {
                renderer.underWater().startOnGround(raftSinking(look.naval()), RAFT_SINK_TICKS, x, y, 0, scroll);
            }
        }
        ripple(x, y, RIPPLE_DELAY_TICKS);
    }

    /** The raft's plain body held for its sinking's frames (the sub pass draws it under the sink frames). */
    private static Array<AtlasRegion> raftSinking(vanguard.game.render.NavalLooks naval) {
        Array<AtlasRegion> held = new Array<>(naval.sink().size / 2);
        for (int i = 0; i < naval.sink().size / 2; i++) {
            held.add(naval.raftSub().first());
        }
        return held;
    }

    /**
     * M5 part E: an arena boss's part destroyed (value {@code part}; a severed slam arm): its segments
     * pop one after the other from the root, crimson, over a water burst; false when no arena boss
     * holds such a part (the usual medium burst then).
     */
    private boolean arenaPartDown(int part, double x, double y) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.arena().isEmpty()) {
                continue;
            }
            double[] points = LevelRenderer.armPoints(piece, part);
            if (points.length == 0 || armPop.size == 0) {
                return false;
            }
            for (int s = 0; s < points.length / 2; s++) {
                effects.startOnGround(
                        armPop,
                        ARM_POP_FRAME_TICKS,
                        piece.renderX(1) + points[2 * s],
                        piece.renderY(1) + points[2 * s + 1],
                        s * ARM_POP_STEP_TICKS,
                        sortie.groundScroll());
            }
            waterBurst("medium", x, y, false);
            return true;
        }
        return false;
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

    /** M5 part H: the Siege Spire's chained bursts over its free spire, this many over its death chain. */
    static final int SPIRE_DEATH_BURSTS = 14;

    /** M5 part H: they lie on a spiral out to this radius, px (the free spire is drawn 244 px). */
    static final double SPIRE_DEATH_RADIUS = 90;

    /**
     * M5 part H: the Siege Spire's death (design/enemies/bosses/siege-spire, Death: {@code huge}, "the
     * spire cracks open, chained explosions over 3 s"): its death frames where it died ({@link
     * LevelRenderer}, from its wreck's age), {@code medium} bursts spiralling out over the free spire
     * through the chain with their sounds, then {@code large} blasts at its core, the death flash (round 42: local,
     * round its core) and the credit shower; the canopy's withering and the daylight follow on the wreck's clock. Round 41 (the
     * 2026-10-19 playtest: "some parts of it kept moving with the ship up"): the bursts and blasts lie on
     * the ground, so they scroll away with the wreck and its stump once the scroll resumes after the kill.
     */
    private void spireDeath(int k, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        double scroll = sortie.groundScroll();
        int chain = LevelRenderer.chainTicks(piece);
        showerDelay = chain;
        wrecks.start(k, x, y, 1, 1, false);
        for (int n = 0; n < SPIRE_DEATH_BURSTS; n++) {
            int at = n * chain / SPIRE_DEATH_BURSTS;
            double angle = n * 2.4;
            double reach = SPIRE_DEATH_RADIUS * (0.25 + 0.75 * n / (SPIRE_DEATH_BURSTS - 1.0));
            double px = x + Math.cos(angle) * reach;
            double py = y + Math.sin(angle) * reach;
            effects.startOnGround(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, px, py, at, scroll);
            if (n % 2 == 0) {
                chainSounds.play(
                        n % 4 == 0 ? Sfx.EXPLOSION_MEDIUM_A : Sfx.EXPLOSION_MEDIUM_B,
                        CHAIN_SOUND_VOLUME,
                        0.8f + 0.1f * (n % 3),
                        pan(px),
                        at);
            }
        }
        Array<AtlasRegion> large = services.sprites.explosionLarge;
        effects.startOnGround(large, LARGE_EXPLOSION_FRAME_TICKS, x, y, chain, scroll);
        effects.startOnGround(large, LARGE_EXPLOSION_FRAME_TICKS, x - 30, y + 24, chain + 4, scroll);
        effects.startOnGround(large, LARGE_EXPLOSION_FRAME_TICKS, x + 34, y - 18, chain + 8, scroll);
        chainSounds.play(sounds.deathBlast(k), 1, 1, pan(x), chain);
        chainSounds.play(Sfx.EXPLOSION_LARGE_C, 1, 0.9f, pan(x), chain + 3);
        if (LevelRenderer.flashesAtDeath(piece)) {
            // Round 42: a white glow round its core, not the whole field washed white for most of a second.
            screenFlash.startAt(chain, x, y);
        }
    }

    /** M7 part F: the walker's chained bursts over its topple, this many over its death. */
    static final int WALKER_DEATH_BURSTS = 10;

    /** M7 part F: they lie within this of its centre, px (its hull is 110×120), drifting forward as it falls. */
    static final double WALKER_DEATH_RADIUS = 55;

    /**
     * M7 part F: the walker's death (design/enemies/bosses/revenant-walker, Death: the {@code huge} machine variant):
     * its topple over its 2 s death and then its wreck where it fell (WalkerLooks), {@code medium} bursts over its hull
     * through the topple with their sounds, drifting forward along its heading as it falls, then {@code large} blasts
     * where the hull lands and the credit shower; no screen flash (a mid-boss). The bursts lie on the ground, so they
     * scroll away with the wreck once the scroll resumes.
     */
    private void walkerDeath(int k, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        double scroll = sortie.groundScroll();
        int chain = LevelRenderer.chainTicks(piece);
        showerDelay = chain;
        double heading = piece.stride().orElseThrow().headingRadians();
        double forwardX = -Math.sin(heading);
        double forwardY = -Math.cos(heading);
        for (int n = 0; n < WALKER_DEATH_BURSTS; n++) {
            int at = n * chain / WALKER_DEATH_BURSTS;
            double angle = n * 2.4;
            double reach = WALKER_DEATH_RADIUS * (0.3 + 0.7 * ((n * 7) % WALKER_DEATH_BURSTS) / WALKER_DEATH_BURSTS);
            double fall = 40.0 * n / WALKER_DEATH_BURSTS;
            double px = x + Math.cos(angle) * reach + forwardX * fall;
            double py = y + Math.sin(angle) * reach + forwardY * fall;
            effects.startOnGround(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, px, py, at, scroll);
            if (n % 2 == 0) {
                chainSounds.play(
                        n % 4 == 0 ? Sfx.EXPLOSION_MEDIUM_A : Sfx.EXPLOSION_MEDIUM_B,
                        CHAIN_SOUND_VOLUME,
                        0.8f + 0.1f * (n % 3),
                        pan(px),
                        at);
            }
        }
        Array<AtlasRegion> large = services.sprites.explosionLarge;
        double landX = x + forwardX * 40;
        double landY = y + forwardY * 40;
        effects.startOnGround(large, LARGE_EXPLOSION_FRAME_TICKS, landX, landY, chain, scroll);
        effects.startOnGround(large, LARGE_EXPLOSION_FRAME_TICKS, landX - 28, landY + 20, chain + 4, scroll);
        chainSounds.play(sounds.deathBlast(k), 1, 1, pan(landX), chain);
    }

    /**
     * M7 part H, step HB: the Dust Colossus's death (design/enemies/bosses/dust-colossus, Death: {@code huge}, "the
     * last mouth rears up and shudders, the sand splits along the whole ridge, chained eruptions of dust and lime ichor
     * over 3 s, the mouth sinks into a collapsing crater; screen flash at the chain's end; credit shower"): its death
     * frames, the split ridge glowing and the crater and the furrow are {@code ColossusLooks}'; step GK: its 4.5 s
     * death throe ({@code ColossusThroe}, round 44 K.b) runs past the simulation's 3 s chain, so the white flash (round
     * 42's local flash, as the Siege Spire's: a glow round the mouth, not the whole field washed white) comes at its
     * 4.2 s and the credit shower at its end ({@link LevelRenderer#colossusFlashTicks()}, {@link
     * LevelRenderer#colossusShowerTicks()}). Its sound is its own death ({@code BossCues}: the chained eruptions and the
     * blast in the file, 6.1 s), so no chained bursts' sounds.
     */
    private void colossusDeath(int k, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        showerDelay = LevelRenderer.colossusShowerTicks();
        if (LevelRenderer.flashesAtDeath(piece)) {
            screenFlash.startAt(LevelRenderer.colossusFlashTicks(), x, y);
        }
    }

    /**
     * M9 part H: the Iron Sovereign's death (design/enemies/bosses/iron-sovereign, Death: "the wrecked batteries cook off
     * ring by ring ... the core collapses inward and then blows ... then the screen flash (an act boss) and the credit
     * shower over the core"): its cook-offs, drifting segments, implosion and blast are {@code SovereignLooks}', its
     * sounds {@code SovereignCues}'; here the white flash over the field and the credit shower, both at the core's blow
     * ({@link LevelRenderer#ringBlastTicks}).
     */
    private void ringDeath(int k, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        int blow = LevelRenderer.ringBlastTicks(piece);
        showerDelay = blow;
        if (LevelRenderer.flashesAtDeath(piece)) {
            screenFlash.start(blow);
        }
    }

    /**
     * M5 part E: an arena boss's death (design/enemies/bosses/harbour-kraken, Death: the {@code huge}
     * water-surface variant): its wreck's sinking and sliding (KrakenLooks), water bursts at its parts,
     * a large one at its head with its own lime and crimson burst, spray, then the credit shower.
     */
    private void arenaDeath(int k, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        showerDelay = LevelRenderer.chainTicks(piece);
        wrecks.start(k, x, y, 1, 1, false);
        double headY = y + renderer.krakenHeadDy();
        double scroll = sortie.groundScroll();
        for (int p = 0; p < piece.partCount(); p++) {
            if (services.sprites.has("explosion-water-medium")) {
                renderer.surfaceWater()
                        .startOnGround(
                                services.sprites.frames("explosion-water-medium"),
                                WATER_FRAME_TICKS,
                                piece.partX(p),
                                piece.partY(p),
                                p * CHAIN_STEP_TICKS,
                                scroll);
            }
        }
        if (services.sprites.has("explosion-water-large")) {
            Array<AtlasRegion> large = services.sprites.frames("explosion-water-large");
            renderer.surfaceWater().startOnGround(large, WATER_FRAME_TICKS, x, headY, showerDelay / 2, scroll);
            renderer.surfaceWater().startOnGround(large, WATER_FRAME_TICKS, x - 40, headY + 30, showerDelay, scroll);
            renderer.surfaceWater().startOnGround(large, WATER_FRAME_TICKS, x + 40, headY - 20, showerDelay, scroll);
        }
        if (services.sprites.has(piece.slug() + "-death")) {
            effects.startOnGround(
                    services.sprites.frames(piece.slug() + "-death"),
                    KRAKEN_DEATH_FRAME_TICKS,
                    x,
                    headY - KRAKEN_DEATH_BELOW_HEAD,
                    0,
                    scroll);
        }
        ripple(x, headY, showerDelay);
    }

    /**
     * A unit's death at its position: its solid pieces, its explosion and its glow (the pieces first,
     * so the glows of the same death draw over them). A ground unit's stay on the ground, scrolling
     * with it and with its husk; a flyer's stay where it died on the play field.
     */
    private void death(int kind, double x, double y) {
        // M8 part E: a Siren's nest dies on its wall, scrolling with the ground.
        boolean onGround = sortie.enemyKinds().get(kind).layer() == Layer.GROUND
                || renderer.deepDeaths().mounted(sortie, kind);
        if (!deepDeath(kind, x, y, onGround) && !vacuumDeath(kind, x, y, onGround)) {
            death(kind, x, y, onGround);
        }
        trenchDeath(kind, x, y);
    }

    /**
     * M8 part E: a Siren's collapse on its wall, the pale plume rising from its sac, then its husk left on the wall
     * (all on the ground); a Spiral Nautilus's shell cracking into halves and, killed uncoiled, its dark crown sinking.
     */
    private void trenchDeath(int kind, double x, double y) {
        DeepDeaths deep = renderer.deepDeaths();
        Array<AtlasRegion> collapse = deep.sirenCollapse(sortie, kind, x);
        if (!collapse.isEmpty()) {
            double bodyX = deep.sirenBodyX(x);
            start(pieces, collapse, DeepDeaths.sirenCollapseTicks(), bodyX, y, 0, true);
            Array<AtlasRegion> plume = deep.sirenPlume(sortie, kind);
            if (!plume.isEmpty()) {
                pieces.startOnGround(
                        plume,
                        DeepDeaths.sirenPlumeTicks(),
                        deep.sirenSacX(sortie, x),
                        y + deep.sirenSacDy(sortie, x),
                        0,
                        sortie.groundScroll(),
                        0,
                        DeepDeaths.sirenPlumeRise(plume.size));
            }
            Array<AtlasRegion> husk = deep.sirenHusk(sortie, kind, x);
            if (!husk.isEmpty()) {
                debris.startOnGround(husk, REMAINS_TICKS, bodyX, y, DeepDeaths.sirenHuskDelay(), sortie.groundScroll());
            }
        }
        Array<AtlasRegion> dark = deep.nautilusDark(sortie, kind, x, y);
        if (!dark.isEmpty()) {
            pieces.startDrifting(
                    dark,
                    DeepDeaths.halvesTicks(),
                    x + deep.nautilusDarkDx(sortie),
                    y + deep.nautilusDarkDy(sortie),
                    0,
                    0,
                    -DeepDeaths.darkSink());
        }
        start(pieces, deep.nautilusHalves(sortie, kind), DeepDeaths.halvesTicks(), x, y, 0, false);
    }

    /**
     * M8 part C: a kill below the waterline (the renderer's grade at its height; design/art-direction, the explosion
     * ladder under water): the under-water burst of its rung over its cavity, its own death pieces and glow, a
     * Glassfin's shards or an eel part's husk sinking, a siphon pump's break and its remains on the ground. False
     * (nothing started) above the water or without the art.
     */
    private boolean deepDeath(int kind, double x, double y, boolean onGround) {
        DeepDeaths deep = renderer.deepDeaths();
        if (!deep.present() || !renderer.underwaterAt(y)) {
            return false;
        }
        EnemyLooks look = looks[kind];
        start(pieces, deep.cavity(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        start(pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        start(pieces, deep.pieces(sortie, kind, x, y), DeepDeaths.piecesTicks(sortie, kind), x, y, 0, onGround);
        start(pieces, deep.pumpBreak(sortie, kind), DeepDeaths.breakTicks(), x, y, 0, onGround);
        // M8 part G: a Depth Hunter's oil bloom and hull break under the burst (no wreck).
        start(pieces, deep.hunterOil(sortie, kind), DeepDeaths.hunterOilTicks(), x, y, 0, onGround);
        start(pieces, deep.hunterBreak(sortie, kind), DeepDeaths.hunterBreakTicks(), x, y, 0, onGround);
        // M8 part D: an Abyss Ray's break-up (faint for a shadow killed in the deep), a Barbfin's sinking shell and
        // ink cloud, under the burst.
        Array<AtlasRegion> breakUp = deep.rayBreakUp(sortie, kind);
        float rayOpacity = deep.rayOpacity(sortie);
        if (!breakUp.isEmpty()) {
            pieces.start(breakUp, DeepDeaths.rayBreakUpTicks(), x, y, 0, 1, rayOpacity);
        }
        Array<AtlasRegion> shell = deep.barbfinShell(sortie, kind, x, y);
        if (!shell.isEmpty()) {
            pieces.startDrifting(shell, DeepDeaths.shellTicks(), x, y, 0, 0, -DeepDeaths.shellSink());
        }
        start(pieces, deep.barbfinInk(sortie, kind), DeepDeaths.inkTicks(), x, y - DeepDeaths.inkBelow(), 0, false);
        start(effects, deep.flash(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
        Array<AtlasRegion> bloom = deep.rayBloom(sortie, kind);
        if (!bloom.isEmpty()) {
            effects.start(bloom, DeepDeaths.rayBloomTicks(), x, y, 0, 1, rayOpacity);
        }
        for (Array<AtlasRegion> remains : deep.pumpRemains(sortie, kind)) {
            debris.startOnGround(remains, REMAINS_TICKS, x, y, 0, sortie.groundScroll());
        }
        // M8 part G: a relay beacon's lamp breaking at its lamp, its dark mast left on the ridge.
        start(
                effects,
                deep.beaconBreak(sortie, kind),
                DeepDeaths.beaconBreakTicks(),
                x + deep.beaconLampDx(sortie),
                y + deep.beaconLampDy(sortie),
                0,
                onGround);
        for (Array<AtlasRegion> remains : deep.beaconRemains(sortie, kind)) {
            debris.startOnGround(remains, REMAINS_TICKS, x, y, 0, sortie.groundScroll());
        }
        return true;
    }

    /** As above, on the ground or (a pouncer shot in the air) on the play field. */
    /**
     * M9 part A: a kill in vacuum (the belt's sections, {@link VacuumDeaths}): the vacuum burst of its rung over its
     * debris ring, its own death pieces and glow, a medium or large unit's wreck drifting off; false (nothing started)
     * outside vacuum.
     */
    private boolean vacuumDeath(int kind, double x, double y, boolean onGround) {
        VacuumDeaths vacuum = renderer.vacuumDeaths();
        if (!vacuum.at(sortie.levelSeconds())) {
            return false;
        }
        EnemyLooks look = looks[kind];
        start(pieces, vacuum.debris(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        start(pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        start(effects, vacuum.burst(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
        if (!onGround && !renderer.breaksUp(sortie, kind)) {
            vacuum.startWreck(look, x, y, sortie.tick(), sortie.groundScroll());
        }
        return true;
    }

    private void death(int kind, double x, double y, boolean onGround) {
        EnemyLooks look = looks[kind];
        // M7 part E: a Dust Devil's funnel collapses under its grit cloud.
        EnemyLooks.DeathEffect collapse = look.collapse();
        start(pieces, collapse.frames(), collapse.ticksPerFrame(), x, y, collapse.delayTicks(), onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        start(pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
    }

    /** Starts an animation, if there is one, centred on the position: on the ground or on the play field. */
    private void start(
            Effects into,
            Array<AtlasRegion> frames,
            int ticksPerFrame,
            double x,
            double y,
            int delayTicks,
            boolean onGround) {
        if (frames.isEmpty()) {
            return;
        }
        if (onGround) {
            into.startOnGround(
                    frames, ticksPerFrame, x, y, delayTicks, sortie.groundScroll(), wreckDriftX, wreckDriftY);
        } else {
            into.start(frames, ticksPerFrame, x, y, delayTicks);
        }
    }

    /**
     * A set piece's chained death (design/enemies/space/leviathan: chained {@code medium} bursts
     * along the body, ichor cloud): a burst at each part in turn, each with its death cloud when it
     * has one; then its break-up's blasts over the body while the body comes apart (or, without a
     * break-up, a large burst at the centre). Off the play plane the bursts sit where its parts are
     * drawn there, turned with the pass's heading, and every burst is scaled and faded as it is.
     */
    private void chainedDeath(int k, Array<AtlasRegion> cloud, double x, double y) {
        SetPiece piece = sortie.setPiece(k);
        if (piece.boss().isPresent()) {
            showerDelay = LevelRenderer.chainTicks(piece);
            if (LevelRenderer.tailToHead(piece)) {
                tailToHeadDeath(k, piece, cloud, x, y);
                return;
            }
        }
        double altitude = piece.onPlane() ? 0 : piece.altitude(1);
        float scale = LevelRenderer.highAirScale(altitude);
        float opacity = LevelRenderer.highAirOpacity(altitude);
        boolean crossing = piece.boss().isEmpty()
                && !piece.spec().passes().get(piece.pass()).descends();
        double heading = piece.boss().isPresent()
                ? 0
                : piece.spec().passes().get(piece.pass()).headingRadians();
        SetPieceDeath death = renderer.death(k);
        wrecks.start(k, x, y, scale, opacity, crossing);
        String petals = piece.spec().slug() + "-petals";
        if (piece.boss().isPresent() && services.sprites.has(petals)) {
            // the crown's petals tear off the core as it bursts, at the break-up's swap when it has
            // one (design/enemies/bosses/gorgon-frigate)
            for (int p = 0; p < piece.partCount(); p++) {
                if (piece.spec().parts().get(p).vital()) {
                    pieces.start(
                            services.sprites.frames(petals),
                            PETAL_FRAME_TICKS,
                            x + piece.partOffsetX(p),
                            y + piece.partOffsetY(p),
                            death == null ? 0 : death.swap);
                }
            }
        }
        for (int p = 0; p < piece.partCount(); p++) {
            int delay = p * CHAIN_STEP_TICKS;
            double px = x + piece.partOffsetX(p) * scale;
            double py = y + piece.partOffsetY(p) * scale;
            effects.start(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, px, py, delay, scale, opacity);
            if (!cloud.isEmpty()) {
                effects.start(cloud, DEATH_CLOUD_FRAME_TICKS, px, py, delay, scale, opacity);
            }
        }
        if (death == null) {
            int delay = piece.partCount() * CHAIN_STEP_TICKS;
            effects.start(services.sprites.explosionLarge, LARGE_EXPLOSION_FRAME_TICKS, x, y, delay, scale, opacity);
            if (!cloud.isEmpty()) {
                effects.start(cloud, DEATH_CLOUD_FRAME_TICKS, x, y, delay, scale, opacity);
            }
            return;
        }
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);
        for (SetPieceDeath.Blast blast : death.blasts) {
            double px = x + (blast.dx() * cos + blast.dy() * sin) * scale;
            double py = y + (-blast.dx() * sin + blast.dy() * cos) * scale;
            effects.start(blast.frames(), blast.ticksPerFrame(), px, py, blast.at(), scale, opacity);
        }
        sounds.breakUp(x, death.swap);
    }

    /**
     * An act boss's chained death (design/enemies/bosses/brood-carrier, Death): {@code medium}
     * bursts at every part and along the hull from tail to head over its chain, each with its ichor
     * cloud and a burst's sound; at the chain's end {@code large} blasts at the head and the centre,
     * the screen flash and then the credit shower; its break-up's blasts on top when it has one. The
     * body stays under the chain ({@link SetPieceWrecks}).
     */
    private void tailToHeadDeath(int k, SetPiece piece, Array<AtlasRegion> cloud, double x, double y) {
        double altitude = piece.onPlane() ? 0 : piece.altitude(1);
        float scale = LevelRenderer.highAirScale(altitude);
        SetPieceDeath death = renderer.death(k);
        wrecks.start(k, x, y, scale, 1, false);
        int chain = LevelRenderer.chainTicks(piece);
        // A boss with its own sac burst (round 25, the Brood Carrier) bursts each sac with it.
        Sfx sacBurst = sounds.sacBurst(k);
        int n = 0;
        for (var burst : LevelRenderer.deathChain(piece)) {
            double px = x + burst.dx();
            double py = y + burst.dy();
            effects.start(explosionMedium, MEDIUM_EXPLOSION_FRAME_TICKS, px, py, burst.at(), scale, 1);
            if (!cloud.isEmpty() && burst.part() >= 0) {
                effects.start(cloud, DEATH_CLOUD_FRAME_TICKS, px, py, burst.at(), scale, 1);
            }
            if (sacBurst != null && burst.part() >= 0 && sac(piece, burst.part())) {
                chainSounds.play(sacBurst, CHAIN_SOUND_VOLUME, 0.85f + 0.05f * (burst.part() % 3), pan(px), burst.at());
            } else if (n++ % 2 == 0) {
                chainSounds.play(
                        n % 4 == 1 ? Sfx.EXPLOSION_MEDIUM_A : Sfx.EXPLOSION_MEDIUM_B,
                        CHAIN_SOUND_VOLUME,
                        0.8f + 0.1f * (n % 3),
                        pan(px),
                        burst.at());
            }
        }
        double[] head = LevelRenderer.deathHead(piece);
        effects.start(
                services.sprites.explosionLarge,
                LARGE_EXPLOSION_FRAME_TICKS,
                x + head[0],
                y + head[1],
                chain,
                scale,
                1);
        effects.start(services.sprites.explosionLarge, LARGE_EXPLOSION_FRAME_TICKS, x, y, chain, scale, 1);
        if (!cloud.isEmpty()) {
            effects.start(cloud, DEATH_CLOUD_FRAME_TICKS, x, y, chain, scale, 1);
        }
        chainSounds.play(sounds.deathBlast(k), 1, 1, pan(x), chain);
        chainSounds.play(Sfx.EXPLOSION_LARGE_C, 1, 0.9f, pan(x), chain + 3);
        if (LevelRenderer.flashesAtDeath(piece)) {
            screenFlash.start(chain);
        }
        if (death != null) {
            for (SetPieceDeath.Blast blast : death.blasts) {
                effects.start(
                        blast.frames(), blast.ticksPerFrame(), x + blast.dx(), y + blast.dy(), blast.at(), scale, 1);
            }
            sounds.breakUp(x, death.swap);
        }
    }

    /** Whether part {@code p} of a boss is a bay sac: neither its vital core nor a fire-only turret. */
    private static boolean sac(SetPiece piece, int p) {
        return !piece.spec().parts().get(p).vital() && !piece.partArmoured(p);
    }

    /** A sound's pan for a play-field x: the play field's width spans 1.2 of the stereo field. */
    private static float pan(double x) {
        return (float) Math.clamp((x / PlayField.WIDTH - 0.5) * 1.2, -0.6, 0.6);
    }

    /** The chain's bursts' and the klaxon's levels. */
    private static final float CHAIN_SOUND_VOLUME = 0.7f;

    /** The coins of a boss's credit shower fly out this many steps apart, in a ring of this many. */
    private static final int SHOWER_STEP_TICKS = 3;

    private static final int SHOWER_COINS = 16;

    /** A coin's spin: the pickups' 10 fps. */
    private static final int SHOWER_FRAME_TICKS = 6;

    /** The shower starts after the chained bursts over the frigate's four parts (an act boss's: after its chain). */
    private static final int SHOWER_DELAY_TICKS = 4 * CHAIN_STEP_TICKS;

    /**
     * A boss's credit shower (design/enemies/bosses: the death sequence): its credits as a number
     * over the bell and a ring of salvage coins spinning out after the chained bursts.
     */
    private void creditShower(int credits, double x, double y) {
        int delay = showerDelay;
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
