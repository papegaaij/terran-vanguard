package vanguard.game.screen;

import static vanguard.game.screen.EventEffects.MEDIUM_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.LARGE_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.REMAINS_TICKS;
import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.WATER_FRAME_TICKS;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.game.GameServices;
import vanguard.game.audio.DelayedSounds;
import vanguard.game.audio.FlightSounds;
import vanguard.game.audio.Sfx;
import vanguard.game.render.DeepDeaths;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.ScreenFlash;
import vanguard.game.render.SetPieceDeath;
import vanguard.game.render.SetPieceWrecks;
import vanguard.game.render.VacuumDeaths;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * {@link EventEffects}' death family (RF9e): a unit's death (its pieces, explosion and glow; below the waterline, in
 * vacuum, a Siren's or a Nautilus's own), the set pieces' chained deaths and the bosses' own (the Siege Spire, the
 * Revenant Walker, the Dust Colossus, the Iron Sovereign, the Kraken, the act bosses from tail to head), and the steps
 * from a boss's death to its credit shower.
 */
final class DeathEffects {
    private static final int KRAKEN_DEATH_FRAME_TICKS = 8;

    private static final double KRAKEN_DEATH_BELOW_HEAD = 40;

    /** A set piece's death: a medium burst at each part, one after the other, then a large one at its centre. */
    private static final int CHAIN_STEP_TICKS = 6;

    /** A set piece's death cloud (the Leviathan's ichor) shows each frame for 6 steps. */
    private static final int DEATH_CLOUD_FRAME_TICKS = 6;

    /** A boss's crown petals tearing off: 10 fps. */
    private static final int PETAL_FRAME_TICKS = 6;

    /** The chain's bursts' and the klaxon's levels. */
    private static final float CHAIN_SOUND_VOLUME = 0.7f;

    /** The shower starts after the chained bursts over the frigate's four parts (an act boss's: after its chain). */
    private static final int SHOWER_DELAY_TICKS = 4 * CHAIN_STEP_TICKS;

    /** The steps from a boss's death to its credit shower: the end of its chained bursts. */
    private int showerDelay = SHOWER_DELAY_TICKS;

    /** Each set piece's death cloud, its slug's {@code -ichor} frames, by index in the script; empty for none. */
    private final List<Array<AtlasRegion>> deathClouds;

    private final GameServices services;
    private final Sortie sortie;
    private final EnemyLooks[] looks;
    private final FlightSounds sounds;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Effects debris;
    private final Effects pieces;
    private final SetPieceWrecks wrecks;
    private final ScreenFlash screenFlash;
    private final DelayedSounds chainSounds;
    private final Array<AtlasRegion> explosionMedium;
    private final WaterEffects water;
    private final EffectStarts starts;

    DeathEffects(
            GameServices services,
            Sortie sortie,
            EnemyLooks[] looks,
            FlightSounds sounds,
            LevelRenderer renderer,
            Effects effects,
            Effects debris,
            Effects pieces,
            SetPieceWrecks wrecks,
            ScreenFlash screenFlash,
            DelayedSounds chainSounds,
            Array<AtlasRegion> explosionMedium,
            WaterEffects water,
            EffectStarts starts) {
        this.services = services;
        this.sortie = sortie;
        this.looks = looks;
        this.sounds = sounds;
        this.renderer = renderer;
        this.effects = effects;
        this.debris = debris;
        this.pieces = pieces;
        this.wrecks = wrecks;
        this.screenFlash = screenFlash;
        this.chainSounds = chainSounds;
        this.explosionMedium = explosionMedium;
        this.water = water;
        this.starts = starts;
        deathClouds = sortie.script().setPieces().stream()
                .map(spec -> services.sprites.has(spec.slug() + "-ichor")
                        ? services.sprites.frames(spec.slug() + "-ichor")
                        : new Array<AtlasRegion>())
                .toList();
    }

    /**
     * A set piece's death: the Siege Spire's, a walker's, the Dust Colossus's, a ringed boss's, an arena boss's or
     * (every other) its chained death with its death cloud.
     */
    void setPieceDeath(int k, double x, double y) {
        if (LevelRenderer.spire(sortie.setPiece(k))) {
            spireDeath(k, x, y);
        } else if (sortie.setPiece(k).stride().isPresent()) {
            walkerDeath(k, x, y);
        } else if (sortie.setPiece(k).breach().isPresent()) {
            colossusDeath(k, x, y);
        } else if (sortie.setPiece(k).ring().isPresent()) {
            ringDeath(k, x, y);
        } else if (sortie.setPiece(k).arena().isPresent()) {
            arenaDeath(k, x, y);
        } else {
            chainedDeath(k, deathClouds.get(k), x, y);
        }
    }

    /** The steps from the last boss's death to its credit shower. */
    int showerDelay() {
        return showerDelay;
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
        water.ripple(x, headY, showerDelay);
    }

    /**
     * A unit's death at its position: its solid pieces, its explosion and its glow (the pieces first,
     * so the glows of the same death draw over them). A ground unit's stay on the ground, scrolling
     * with it and with its husk; a flyer's stay where it died on the play field.
     */
    void death(int kind, double x, double y) {
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
            starts.start(pieces, collapse, DeepDeaths.sirenCollapseTicks(), bodyX, y, 0, true);
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
        starts.start(pieces, deep.nautilusHalves(sortie, kind), DeepDeaths.halvesTicks(), x, y, 0, false);
    }

    /**
     * M8 part C: a kill below the waterline (the renderer's grade at its height; design/art-direction, the explosion
     * ladder under water): the under-water burst of its rung over its cavity, its own death pieces and glow, a
     * Glassfin's shards or an eel part's husk sinking, a siphon pump's break and its remains on the ground. False
     * (nothing started) above the water or without the art.
     */
    boolean deepDeath(int kind, double x, double y, boolean onGround) {
        DeepDeaths deep = renderer.deepDeaths();
        if (!deep.present() || !renderer.underwaterAt(y)) {
            return false;
        }
        EnemyLooks look = looks[kind];
        starts.start(pieces, deep.cavity(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        starts.start(
                pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        starts.start(pieces, deep.pieces(sortie, kind, x, y), DeepDeaths.piecesTicks(sortie, kind), x, y, 0, onGround);
        starts.start(pieces, deep.pumpBreak(sortie, kind), DeepDeaths.breakTicks(), x, y, 0, onGround);
        // M8 part G: a Depth Hunter's oil bloom and hull break under the burst (no wreck).
        starts.start(pieces, deep.hunterOil(sortie, kind), DeepDeaths.hunterOilTicks(), x, y, 0, onGround);
        starts.start(pieces, deep.hunterBreak(sortie, kind), DeepDeaths.hunterBreakTicks(), x, y, 0, onGround);
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
        starts.start(
                pieces, deep.barbfinInk(sortie, kind), DeepDeaths.inkTicks(), x, y - DeepDeaths.inkBelow(), 0, false);
        starts.start(effects, deep.flash(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        starts.start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
        Array<AtlasRegion> bloom = deep.rayBloom(sortie, kind);
        if (!bloom.isEmpty()) {
            effects.start(bloom, DeepDeaths.rayBloomTicks(), x, y, 0, 1, rayOpacity);
        }
        for (Array<AtlasRegion> remains : deep.pumpRemains(sortie, kind)) {
            debris.startOnGround(remains, REMAINS_TICKS, x, y, 0, sortie.groundScroll());
        }
        // M8 part G: a relay beacon's lamp breaking at its lamp, its dark mast left on the ridge.
        starts.start(
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

    /**
     * M9 part A: a kill in vacuum (the belt's sections, {@link VacuumDeaths}): the vacuum burst of its rung over its
     * debris ring, its own death pieces and glow, a medium or large unit's wreck drifting off; false (nothing started)
     * outside vacuum.
     */
    boolean vacuumDeath(int kind, double x, double y, boolean onGround) {
        VacuumDeaths vacuum = renderer.vacuumDeaths();
        if (!vacuum.at(sortie.levelSeconds())) {
            return false;
        }
        EnemyLooks look = looks[kind];
        starts.start(pieces, vacuum.debris(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        starts.start(
                pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        starts.start(effects, vacuum.burst(look), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        starts.start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
        if (!onGround && !renderer.breaksUp(sortie, kind)) {
            vacuum.startWreck(look, x, y, sortie.tick(), sortie.groundScroll());
        }
        return true;
    }

    /** As above, on the ground or (a pouncer shot in the air) on the play field. */
    void death(int kind, double x, double y, boolean onGround) {
        EnemyLooks look = looks[kind];
        // M7 part E: a Dust Devil's funnel collapses under its grit cloud.
        EnemyLooks.DeathEffect collapse = look.collapse();
        starts.start(pieces, collapse.frames(), collapse.ticksPerFrame(), x, y, collapse.delayTicks(), onGround);
        EnemyLooks.DeathEffect deathPieces = look.deathPieces();
        starts.start(
                pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), onGround);
        starts.start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, onGround);
        EnemyLooks.DeathEffect glow = look.deathGlow();
        starts.start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), onGround);
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
}
