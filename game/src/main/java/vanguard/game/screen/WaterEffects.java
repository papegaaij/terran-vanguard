package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.WATER_FRAME_TICKS;
import static vanguard.game.screen.LevelScreen.optionalFrames;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.GameServices;
import vanguard.game.audio.FlightSounds;
import vanguard.game.render.AirborneWalkers;
import vanguard.game.render.Effects;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.WeaponLooks;
import vanguard.sim.Layer;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * {@link EventEffects}' water family (RF9e): a torpedo's impact over or under the sea, a burst's splash and its ripple
 * train, a kill on the water bursting and sinking, an arena boss's severed arm popping over a water burst, and whether a
 * hit lies on what of an arena boss is above or under the water.
 */
final class WaterEffects {
    private static final int RIPPLE_FRAME_TICKS = 8;

    private static final int RIPPLE_DELAY_TICKS = 12;

    private static final int RAFT_SINK_TICKS = 6;

    private static final int ARM_POP_FRAME_TICKS = 4;

    private static final int ARM_POP_STEP_TICKS = 3;

    /** A torpedo's impact is under the water when a submerged unit lies within this of it, px. */
    private static final double UNDER_REACH = 24;

    /** M5 part E: how far beside a surfaced part's hit box a hit still counts as on it, px. */
    private static final double SOLID_REACH = 8;

    /** M5 part E: the shared ripple train and a severed arm's pop; empty without them. */
    private final Array<AtlasRegion> ripple;

    private final Array<AtlasRegion> armPop;

    /** M5 part G: only a burst on a naval unit plays the water splash (Level 13's ice, {@code water_splash}). */
    private final boolean navalSplashOnly;

    private final GameServices services;
    private final Sortie sortie;
    private final EnemyLooks[] looks;
    private final WeaponLooks weaponLooks;
    private final AirborneWalkers airborne;
    private final FlightSounds sounds;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Effects pieces;
    private final EffectStarts starts;

    WaterEffects(
            GameServices services,
            Sortie sortie,
            EnemyLooks[] looks,
            WeaponLooks weaponLooks,
            AirborneWalkers airborne,
            FlightSounds sounds,
            LevelRenderer renderer,
            Effects effects,
            Effects pieces,
            EffectStarts starts,
            boolean navalSplashOnly) {
        this.services = services;
        this.sortie = sortie;
        this.looks = looks;
        this.weaponLooks = weaponLooks;
        this.airborne = airborne;
        this.sounds = sounds;
        this.renderer = renderer;
        this.effects = effects;
        this.pieces = pieces;
        this.starts = starts;
        this.navalSplashOnly = navalSplashOnly;
        ripple = optionalFrames(services, "water-ripple");
        armPop = optionalFrames(services, "harbour-kraken-arm-death");
    }

    /**
     * M5 part E: a torpedo's impact (mount {@code mount}) at (x, y): under the water when it struck a
     * unit there, else on the surface; false for a weapon that does not run under the water.
     */
    boolean waterImpact(int mount, double x, double y) {
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
    boolean arenaSolid(double x, double y) {
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

    /** M5 part G: a burst counts as on a naval unit within this reach of its hit box, px. */
    static final double NAVAL_REACH = 8;

    /**
     * M5 part G (user decision G5 = a): whether a burst at (x, y) is the water splash: on a water level
     * every one, but on a {@code water_splash: naval-only} level (Level 13's ice) only one on a naval unit
     * (a unit with its own water layer, the Skimmer); every other is the ground burst.
     */
    boolean splashes(double x, double y) {
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
    boolean waterBurst(String rung, double x, double y) {
        return waterBurst(rung, x, y, true);
    }

    /** As {@link #waterBurst(String, double, double)}, with the water explosion's sound or without (a ship's hit and sinking have their own). */
    boolean waterBurst(String rung, double x, double y, boolean sound) {
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
    void ripple(double x, double y, int delay) {
        if (ripple.size > 0) {
            renderer.surfaceWater().startOnGround(ripple, RIPPLE_FRAME_TICKS, x, y, delay, sortie.groundScroll());
        }
    }

    /**
     * M5 part E: a kill on the water (design/art-direction, Water): on the surface its pieces and glow
     * with its tier's water burst and a ripple train, a raft sinking under its gun; under the water
     * ({@code under}) only its under-water burst, through the sub pass. No crater, no wreck.
     */
    void waterDeath(int kind, double x, double y, boolean under) {
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
        starts.start(pieces, deathPieces.frames(), deathPieces.ticksPerFrame(), x, y, deathPieces.delayTicks(), true);
        Array<AtlasRegion> burst = look.naval().water();
        if (!burst.isEmpty()) {
            renderer.surfaceWater().startOnGround(burst, WATER_FRAME_TICKS, x, y, 0, scroll);
        } else {
            starts.start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, true);
        }
        EnemyLooks.DeathEffect glow = look.deathGlow();
        starts.start(effects, glow.frames(), glow.ticksPerFrame(), x, y, glow.delayTicks(), true);
        if (look.naval().skims()) {
            // M5 part G: a Skimmer's small burst over its water splash; its flip and sink follow its
            // LANE_WALKER_DOWN at its heading.
            starts.start(effects, look.explosion(), TINY_EXPLOSION_FRAME_TICKS, x, y, 0, true);
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
    boolean arenaPartDown(int part, double x, double y) {
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
}
