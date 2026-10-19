package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import vanguard.game.render.LevelRenderer.SpirePass;
import vanguard.sim.SetPiece;
import vanguard.sim.Ship;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The set pieces for {@link LevelRenderer}'s passes: the bosses and the other set pieces on and above
 * the play plane, their break-ups and carcasses, a hull boss's shadow and the boss bar; the arena boss
 * (the Harbour Kraken, {@link KrakenLooks}) and the anchored one (the Siege Spire, {@link SpireLooks}).
 */
final class SetPiecePasses {
    /** The vital part's glow pulses between 55 % and full every 1.2 s (the review loop's). */
    private static final double GLOW_PERIOD_SECONDS = 1.2;

    /** A hull boss's carcass darkens over its chained death to this shade, and drifts after it, px/s. */
    private static final float CARCASS_SHADE = 0.45f;

    private static final double CARCASS_DRIFT_X = 5;

    private static final double CARCASS_DRIFT_Y = -12;

    private final FlashShader flash;
    private final PlayFrame play;
    private final BitmapFont font;
    /** Per set piece its sprites; null for a boss, which {@link #bossLooks} draws. */
    private final SetPieceLooks[] setPieceLooks;

    private final BossLooks bossLooks;
    /** M5 part E: the arena boss (the Harbour Kraken); null without one. */
    private final KrakenLooks kraken;
    /** M5 part H: the anchored boss without lanes (the Siege Spire) and its canopy's death; null without it. */
    private final SpireLooks spire;

    SetPiecePasses(
            FlashShader flash,
            PlayFrame play,
            BitmapFont font,
            SetPieceLooks[] setPieceLooks,
            BossLooks bossLooks,
            KrakenLooks kraken,
            SpireLooks spire) {
        this.flash = flash;
        this.play = play;
        this.font = font;
        this.setPieceLooks = setPieceLooks;
        this.bossLooks = bossLooks;
        this.kraken = kraken;
        this.spire = spire;
    }

    /**
     * The set pieces on the play plane ({@code high} false) or above it: the first pass's unit as
     * drawn on its heading; the second pass's body in its sway with every part on it, intact or
     * wrecked, a hit part flashing white, and the vital part's glow pulsing until it is destroyed.
     * Off the play plane (arriving, descending, rising) it is drawn above the ship, scaled between
     * the high-air and the play-plane size and faded between 75 % and full opacity by its altitude;
     * it switches below the ship, opaque, when it reaches the plane, where it can collide.
     */
    void drawSetPieces(SpriteBatch batch, Sortie sortie, float alpha, double seconds, boolean high) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (!piece.present() || piece.onPlane() == high || piece.arena().isPresent()) {
                // M5 part E: an arena boss lies on the water ({@link #drawKraken}).
                continue;
            }
            if (piece.boss().isPresent()) {
                Ship ship = sortie.ship();
                bossLooks.draw(
                        batch,
                        piece,
                        alpha,
                        seconds,
                        play.whiteFlash,
                        ship.renderX(alpha),
                        ship.renderY(alpha),
                        piece == play.heatPiece ? play.heatPart : -1);
                continue;
            }
            SetPieceLooks look = setPieceLooks[k];
            double x = piece.renderX(alpha);
            double y = piece.renderY(alpha);
            int sway = SetPieceLooks.sway(seconds);
            float altitude = piece.onPlane() ? 0 : (float) piece.altitude(alpha);
            float opacity = LevelRenderer.highAirOpacity(altitude);
            batch.setColor(1, 1, 1, opacity);
            if (!piece.spec().passes().get(piece.pass()).descends()) {
                // The crossing pass: one sprite of the whole unit at its heading and the high-air
                // scale. Its parts have no wrecked look there; a part a homing weapon wrecks on
                // this pass stays drawn intact until the second pass.
                drawCentred(batch, look.cross.get(sway % look.cross.size), x, y);
                batch.setColor(Color.WHITE);
                continue;
            }
            float scale = LevelRenderer.highAirScale(altitude);
            drawBody(batch, piece, look, x, y, sway, scale, true);
            batch.setColor(Color.WHITE);
            if (look.glow != null && !piece.partWrecked(look.glowPart)) {
                double pulse = 0.55 + 0.45 * Math.sin(2 * Math.PI * seconds / GLOW_PERIOD_SECONDS);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.setColor(1, 1, 1, (float) pulse * opacity);
                drawScaled(
                        batch,
                        look.glow,
                        x + piece.partOffsetX(look.glowPart) * scale,
                        y + piece.partOffsetY(look.glowPart) * scale,
                        scale);
                batch.setColor(Color.WHITE);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /** The second pass's body in sway frame {@code sway} with every part on it, intact or wrecked (a hit part flashing). */
    private void drawBody(
            SpriteBatch batch,
            SetPiece piece,
            SetPieceLooks look,
            double x,
            double y,
            int sway,
            float scale,
            boolean hitFlash) {
        drawScaled(batch, look.down.get(sway % look.down.size), x, y, scale);
        for (int p = 0; p < piece.partCount(); p++) {
            if (!look.drawn(p)) {
                continue;
            }
            double dx = look.pivoted(p) ? look.pivot(p, sway)[0] : piece.partOffsetX(p);
            double dy = look.pivoted(p) ? look.pivot(p, sway)[1] : piece.partOffsetY(p);
            AtlasRegion part = look.part(p, sway, piece.partWrecked(p));
            double px = x + dx * scale;
            double py = y + dy * scale;
            if (hitFlash && scale == 1 && piece == play.heatPiece && p == play.heatPart) {
                // M7 part A: the part the beam burns, in a steady heat tint.
                flash.draw(
                        batch, part, Math.round(X0 + px), Math.round(py), ArsenalLooks.HEAT, ArsenalLooks.HEAT_AMOUNT);
            } else if (hitFlash && scale == 1 && piece.partTicksSinceHit(p) < HIT_FLASH_TICKS) {
                flash.draw(batch, part, Math.round(X0 + px), Math.round(py), HIT_WHITE, play.whiteFlash);
            } else {
                drawScaled(batch, part, px, py, scale);
            }
        }
    }

    /**
     * The set pieces whose death is playing (their {@link SetPieceDeath break-up}): until the swap
     * the body where it died, as it was drawn there (without the glow of its destroyed vital part);
     * then, facing down, the chunks drifting apart from their offsets, sinking (drawn smaller),
     * darkening and fading out. A death on the crossing pass keeps its sprite until the swap and
     * has no chunks. Drawn on the play plane's layer, under the effects, whatever the altitude.
     */
    void drawWrecks(SpriteBatch batch, Sortie sortie, SetPieceWrecks wrecks, float alpha, double seconds) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPieceDeath death = death(k);
            SetPieceLooks look = setPieceLooks[k];
            boolean boss = look == null;
            if (!wrecks.active(k)) {
                continue;
            }
            if (death == null) {
                if (boss && bossLooks.hull != null) {
                    drawCarcass(batch, sortie.setPiece(k), wrecks, k, alpha);
                }
                continue;
            }
            float age = wrecks.age(k) + alpha;
            float x = wrecks.x(k);
            float y = wrecks.y(k);
            float scale = wrecks.scale(k);
            if (age < death.swap) {
                batch.setColor(1, 1, 1, wrecks.opacity(k));
                int sway = SetPieceLooks.sway(seconds);
                if (boss) {
                    bossLooks.drawWreck(batch, sortie.setPiece(k), x, y, scale);
                } else if (wrecks.crossing(k)) {
                    drawCentred(batch, look.cross.get(sway % look.cross.size), x, y);
                } else {
                    drawBody(batch, sortie.setPiece(k), look, x, y, sway, scale, false);
                }
            } else if (!wrecks.crossing(k) && age < death.end) {
                float t = death.progress(age);
                float s = scale * (1 - death.sink * t);
                float e = 1 - (1 - t) * (1 - t);
                float shade = 1 - death.darken * t;
                float fade = t < death.fadeFrom ? 1 : 1 - (t - death.fadeFrom) / (1 - death.fadeFrom);
                batch.setColor(shade, shade, shade, wrecks.opacity(k) * fade);
                for (SetPieceDeath.Chunk chunk : death.chunks) {
                    int f = Math.min(chunk.frames().size - 1, (int) ((age - death.swap) / death.frameSteps));
                    int[] offset = chunk.offsets()[f];
                    drawScaled(
                            batch,
                            chunk.frames().get(f),
                            x + s * (offset[0] + chunk.driftX() * e),
                            y + s * (offset[1] + chunk.driftY() * e),
                            s);
                }
            }
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * A hull boss without a break-up (its placeholder): the body where it died, darkening under its
     * chained death, then the dark carcass drifting slowly away (the Level 07 aftermath).
     */
    private void drawCarcass(SpriteBatch batch, SetPiece piece, SetPieceWrecks wrecks, int k, float alpha) {
        float age = wrecks.age(k) + alpha;
        int chain = LevelRenderer.chainTicks(piece);
        float t = Math.min(1, age / Math.max(1, chain));
        float shade = 1 - (1 - CARCASS_SHADE) * t;
        double drift = Math.max(0, age - chain) * SimStep.SECONDS;
        batch.setColor(shade, shade, shade, wrecks.opacity(k));
        bossLooks.drawWreck(
                batch,
                piece,
                wrecks.x(k) + CARCASS_DRIFT_X * drift,
                wrecks.y(k) + CARCASS_DRIFT_Y * drift,
                wrecks.scale(k));
        batch.setColor(Color.WHITE);
    }

    /** A hull boss's shadow on the play plane while it flies above it (design/art-direction, high-air rule). */
    void drawBossShadows(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.boss().isPresent() && piece.present() && piece.arena().isEmpty()) {
                bossLooks.drawShadow(batch, piece, alpha);
            }
        }
    }

    /** The boss bar at the top of the play field while a boss is on the screen (design/ui/hud). */
    void drawBossBar(SpriteBatch batch, Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.boss().isPresent() && piece.present()) {
                bossLooks.drawBar(batch, font, piece);
            }
        }
    }

    /**
     * M5 part E: the arena boss (the Harbour Kraken, {@link KrakenLooks}) on the surface: first
     * ({@code first}) its lanes' churn and its grip, head and arms; second, over the convoy and the
     * ground units, its lanes' marks and splashes. Scrolling in, alive, or dying where it died.
     */
    void drawKraken(
            SpriteBatch batch, Sortie sortie, SetPieceWrecks wrecks, float alpha, double scroll, boolean first) {
        if (kraken == null) {
            return;
        }
        long tick = sortie.tick();
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            int age = wrecks.active(k) ? wrecks.age(k) : -1;
            if (piece.arena().isEmpty() || !KrakenLooks.shown(piece, age >= 0)) {
                continue;
            }
            if (!first) {
                if (age < 0) {
                    kraken.drawLaneMarks(batch, piece, tick);
                }
                continue;
            }
            double y = age >= 0 ? kraken.deathY(piece, scroll) : piece.renderY(alpha);
            double x = age >= 0 ? kraken.deathX() : piece.renderX(alpha);
            if (age < 0) {
                kraken.update(piece, tick);
                kraken.drawLanes(batch, piece, tick);
            }
            kraken.drawSurface(batch, piece, x, y, tick, age, play.whiteFlash);
        }
    }

    /** Whether the arena boss (the Kraken) is drawn: scrolling in, present or dying. */
    static boolean krakenShown(Sortie sortie, SetPieceWrecks wrecks) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.arena().isPresent() && KrakenLooks.shown(piece, wrecks.active(k))) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part H: one of the Siege Spire's passes ({@link SpireLooks}): scrolling in, present, or after
     * its death (its death frames, its stump scrolling away).
     */
    void drawSpire(
            SpriteBatch batch, Sortie sortie, SetPieceWrecks wrecks, float alpha, double scroll, SpirePass pass) {
        if (spire == null) {
            return;
        }
        long tick = sortie.tick();
        double lag = sortie.groundScroll() - scroll;
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            int age = wrecks.active(k) ? wrecks.age(k) : -1;
            if (!SpireLooks.drawn(piece) || !SpireLooks.shown(piece, age >= 0)) {
                continue;
            }
            switch (pass) {
                case GROUND -> {
                    spire.update(piece, tick);
                    Ship ship = sortie.ship();
                    spire.drawGround(
                            batch, piece, alpha, tick, ship.renderX(alpha), ship.renderY(alpha), lag, play.whiteFlash);
                }
                case SHOCKWAVE -> spire.drawShockwave(batch, piece, tick, lag);
                case AIR -> spire.drawAir(batch, piece, alpha, tick, age, play.whiteFlash, lag);
                case SWEEP -> spire.drawSweep(batch, piece, alpha, tick);
            }
        }
    }

    /** M5 part H: seconds since the Siege Spire's death at the render time (its wreck's age); -1 while it lives. */
    double spireDeathSeconds(Sortie sortie, SetPieceWrecks wrecks, float alpha) {
        if (spire == null) {
            return -1;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (SpireLooks.drawn(sortie.setPiece(k)) && wrecks.active(k)) {
                return (wrecks.age(k) + alpha) * SimStep.SECONDS;
            }
        }
        return -1;
    }

    /** Set piece {@code k}'s break-up at its death, or null for none. */
    SetPieceDeath death(int k) {
        return setPieceLooks[k] != null ? setPieceLooks[k].death : bossLooks.death;
    }

    /** M5 part H: a mortar lobbed from (x, y) at step {@code tick}: the Siege Spire's mortar pod opens. */
    void lobbed(Sortie sortie, double x, double y, long tick) {
        if (spire == null) {
            return;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (SpireLooks.drawn(sortie.setPiece(k))) {
                spire.lobbed(sortie.setPiece(k), x, y, tick);
            }
        }
    }

    /** M5 part H: a boss's window launched a unit at step {@code tick}: the Siege Spire's maw flares. */
    void bossLaunched(long tick) {
        if (spire != null) {
            spire.launched(tick);
        }
    }

    /**
     * M5 part E: the Kraken's parts under the water in the {@code sub} pass (or, before it scrolls in,
     * its foreshadowing), scrolling in, alive, or dying where it died.
     */
    void drawKrakenSub(SpriteBatch batch, Sortie sortie, float alpha, SetPieceWrecks wrecks, double scroll) {
        if (kraken == null) {
            return;
        }
        long tick = sortie.tick();
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            int age = wrecks.active(k) ? wrecks.age(k) : -1;
            if (piece.arena().isEmpty() || !KrakenLooks.shown(piece, age >= 0)) {
                continue;
            }
            double y = age >= 0 ? kraken.deathY(piece, scroll) : piece.renderY(alpha);
            double x = age >= 0 ? kraken.deathX() : piece.renderX(alpha);
            kraken.drawSub(batch, piece, x, y, tick, age);
        }
    }

    /** M5 part H: the free spire's shadow, sliding out as it lifts (in the shadow pass). */
    void drawSpireShadows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (spire != null) {
            for (int k = 0; k < sortie.setPieceCount(); k++) {
                if (SpireLooks.drawn(sortie.setPiece(k))) {
                    spire.drawShadow(batch, sortie.setPiece(k), alpha, sortie.tick());
                }
            }
        }
    }
}
