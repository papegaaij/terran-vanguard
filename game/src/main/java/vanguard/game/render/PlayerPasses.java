package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.AirstrikeBomb;
import vanguard.sim.Ship;
import vanguard.sim.ShipSpec;
import vanguard.sim.Shot;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;
import vanguard.sim.WeaponSpec;

/**
 * The player's side for {@link LevelRenderer}'s passes: the ship with its wing pods, the shots (solid
 * or glowing, a torpedo's body under the water and its lit back over the chop), the muzzle flashes,
 * and the Airstrike's bombs, bombers and their shadows.
 */
final class PlayerPasses {
    /** The muzzle flash shows each of its three frames for two steps after a shot. */
    private static final int MUZZLE_FRAME_TICKS = 2;

    /** A bomb is drawn shrinking to this scale as it falls (design/player/weapons/bomb-rack). */
    private static final float BOMB_LANDING_SCALE = 0.6f;

    /** The bomber sprite's hull centre is this far above the sprite's centre, px. */
    private static final int BOMBER_HULL_DY = 4;

    /** The bomber's shadow on the ground, offset from the bomber (the concept's light from the upper left). */
    private static final int BOMBER_SHADOW_DX = 34;

    private static final int BOMBER_SHADOW_DY = -48;

    /** The bomber's engine flicker: each of its frames shows this many steps. */
    private static final int BOMBER_FRAME_TICKS = 2;

    /**
     * A shell grows by this much at the top of its arc (design/player/weapons/hammer-mortar: 1.0 -> 1.4
     * -> 1.0); a shorter lob (Rook's aimed Mortar) by its share of the full range.
     */
    private static final float SHELL_ARC_SCALE = 0.4f;

    /** Bolts with a range fade out over its last part (design/player/weapons/scatter-vulcan). */
    private static final double FADE_SHARE = 0.25;

    private static final Color SHIELD_BLUE = Color.valueOf("00C0FF");

    private static final float SHIELD_SHIMMER = 0.6f;

    /** The Airstrike's bomb: the Bomb Rack's (design/player/specials, Audio / VFX). */
    private final AtlasRegion airstrikeBomb;

    /** The Airstrike's CDF bomber, facing up, with its engine flicker, and its shadow. */
    private final Array<AtlasRegion> airstrikeBomber;

    private final AtlasRegion airstrikeShadow;

    private final Sprites sprites;
    private final WeaponLooks weapons;
    private final FlashShader flash;
    private final PlayFrame play;
    private final ShipLooks shipLooks;
    private final ArsenalLooks arsenal;
    private final CanyonLooks canyonLooks;
    /** M5 part E: whether the level is over water: its torpedoes run under it ({@link #runsUnder}). */
    private final boolean overWater;

    PlayerPasses(
            Sprites sprites,
            WeaponLooks weapons,
            FlashShader flash,
            PlayFrame play,
            ShipLooks shipLooks,
            ArsenalLooks arsenal,
            CanyonLooks canyonLooks,
            boolean overWater) {
        this.sprites = sprites;
        airstrikeBomb = sprites.region("bomb-rack-shot");
        airstrikeBomber = sprites.frames("airstrike-bomber");
        airstrikeShadow = sprites.region("airstrike-bomber-shadow");
        this.weapons = weapons;
        this.flash = flash;
        this.play = play;
        this.shipLooks = shipLooks;
        this.arsenal = arsenal;
        this.canyonLooks = canyonLooks;
        this.overWater = overWater;
    }

    /**
     * The ship: its smoke trail and engine flames below the hull, the hull (blinking white in its
     * mercy time, shimmering blue after a shield hit) with its wing pods, then its sparks and the
     * shield ring over it.
     */
    void drawShip(SpriteBatch batch, Sortie sortie, float alpha, float shieldShimmer) {
        Ship craft = sortie.ship();
        TextureRegion hull = sprites.ship.get(craft.bank() + ShipSpec.HARD_BANK);
        float x = Math.round(X0 + craft.renderX(alpha));
        float y = Math.round(craft.renderY(alpha));
        // M7 part A: a dash's afterimages and the drones under the hull.
        arsenal.drawBehindShip(batch, sortie, alpha, play.whiteFlash);
        shipLooks.drawBelow(batch, craft, sortie.tick(), alpha, x, y);
        int mercy = craft.defences().mercyTicks();
        boolean flicker = ArsenalLooks.evadingFlicker(craft, sortie.tick());
        // The hull blinks white in steps of three frames while the mercy invulnerability lasts.
        if (flicker) {
            // M7 part A: see-through every other two steps while a dash's invulnerability holds.
            batch.setColor(1, 1, 1, ArsenalLooks.FLICKER_ALPHA);
            batch.draw(hull, x - hull.getRegionWidth() / 2f, y - hull.getRegionHeight() / 2f);
            batch.setColor(Color.WHITE);
        } else if (mercy > 0 && (mercy + 2) / 3 % 2 == 1 || canyonLooks.shipFlash(sortie.tick())) {
            // M7 part C: a wall touch flashes the hull white for a moment (its cooldown's start).
            flash.draw(batch, hull, x, y, HIT_WHITE, play.whiteFlash);
        } else if (shieldShimmer > 0) {
            flash.draw(batch, hull, x, y, SHIELD_BLUE, shieldShimmer * SHIELD_SHIMMER);
        } else {
            batch.draw(hull, x - hull.getRegionWidth() / 2f, y - hull.getRegionHeight() / 2f);
        }
        drawPods(
                batch,
                craft.bank() + ShipSpec.HARD_BANK,
                x - hull.getRegionWidth() / 2f,
                y + hull.getRegionHeight() / 2f);
        shipLooks.drawAbove(batch, sortie.tick(), alpha, x, y, shieldShimmer);
    }

    /** The fitted wing pods over the hull, whose top-left is at (left, top). */
    private void drawPods(SpriteBatch batch, int bank, float left, float top) {
        for (int m = 0; m < weapons.size(); m++) {
            WeaponLooks.Look look = weapons.look(m);
            if (look.pod != null) {
                AtlasRegion pod = look.pod.get(bank);
                int[] offset = look.podOffsets[bank];
                batch.draw(pod, left + offset[0], top - offset[1] - pod.getRegionHeight());
            }
        }
    }

    /** The glowing shots (tracers, bolts, lances) or the solid rounds (missiles, bombs, shells). */
    void drawShots(SpriteBatch batch, Sortie sortie, float alpha, boolean glowing) {
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            if (weapons.look(shot.mount()).glowingShot != glowing || runsUnder(shot)) {
                continue;
            }
            AtlasRegion sprite = weapons.sprite(shot);
            double x = shot.renderX(alpha);
            double y = shot.renderY(alpha);
            switch (shot.weapon().delivery()) {
                case DROPPED ->
                    drawScaled(batch, sprite, x, y, 1 - (1 - BOMB_LANDING_SCALE) * (float) shot.airProgress(alpha));
                case LOBBED ->
                    drawScaled(
                            batch,
                            sprite,
                            x,
                            y,
                            1 + SHELL_ARC_SCALE * (float) (shot.arc() * Math.sin(Math.PI * shot.airProgress(alpha))));
                case MINE -> drawCentred(batch, sprite, x, y);
                case BOLT, HOMING, TURRET, TORPEDO -> {
                    double left = shot.rangeLeft();
                    if (left < FADE_SHARE) {
                        batch.setColor(1, 1, 1, (float) (left / FADE_SHARE));
                        drawCentred(batch, sprite, x, y);
                        batch.setColor(Color.WHITE);
                    } else {
                        drawCentred(batch, sprite, x, y);
                    }
                }
            }
        }
    }

    /** The muzzle flashes of the mounts that just fired, at each muzzle of the pattern they fire. */
    void drawMuzzles(SpriteBatch batch, Sortie sortie, float alpha, boolean glowing) {
        Ship ship = sortie.ship();
        double shipX = ship.renderX(alpha);
        double shipY = ship.renderY(alpha);
        boolean overdrive = sortie.overdriveSeconds() > 0;
        for (int m = 0; m < weapons.size(); m++) {
            WeaponLooks.Look look = weapons.look(m);
            int frame = sortie.ticksSinceShot(m) / MUZZLE_FRAME_TICKS;
            // M7 part A: a Deflector Pod fires nothing (no weapon); a beam's and a drone's look has no muzzle.
            if (look.glowingMuzzle != glowing
                    || frame >= look.muzzle.size
                    || !sortie.armament().mount(m).fires()) {
                continue;
            }
            WeaponSpec weapon = overdrive
                    ? sortie.armament().mount(m).overdrive()
                    : sortie.armament().mount(m).weapon();
            for (int i = 0; i < weapon.muzzles().size(); i++) {
                WeaponSpec.Muzzle muzzle = weapon.muzzles().get(i);
                drawCentred(batch, look.muzzle.get(frame), shipX + muzzle.dx(), shipY + muzzle.dy());
            }
        }
    }

    /**
     * The Airstrike: its bombs shrinking as they fall to the ground point they burst on (the Bomb
     * Rack's bomb), and the two bombers (their production sprite) above them on their way up.
     */
    void drawAirstrike(SpriteBatch batch, Sortie sortie, float alpha) {
        SpecialSlot special = sortie.special();
        if (!special.fitted()) {
            return;
        }
        for (int i = 0; i < special.bombCount(); i++) {
            AirstrikeBomb bomb = special.bomb(i);
            float scale = 1 - (1 - BOMB_LANDING_SCALE) * (float) bomb.progress(alpha);
            drawScaled(batch, airstrikeBomb, bomb.renderX(), bomb.renderY(alpha), scale);
        }
        if (!special.bombersIn()) {
            return;
        }
        double y = special.bomberRenderY(alpha) - BOMBER_HULL_DY;
        int frame = (int) (sortie.tick() / BOMBER_FRAME_TICKS % airstrikeBomber.size);
        for (int b = 0; b < SpecialSlot.BOMBERS; b++) {
            drawCentred(batch, airstrikeBomber.get(frame), special.bomberX(b), y);
        }
    }

    /** The bombers' shadows on the ground, below the low-air layer (the 50 % opacity is in the sprite). */
    void drawBomberShadows(SpriteBatch batch, Sortie sortie, float alpha) {
        SpecialSlot special = sortie.special();
        if (!special.fitted() || !special.bombersIn()) {
            return;
        }
        double y = special.bomberRenderY(alpha) - BOMBER_HULL_DY + BOMBER_SHADOW_DY;
        for (int b = 0; b < SpecialSlot.BOMBERS; b++) {
            drawCentred(batch, airstrikeShadow, special.bomberX(b) + BOMBER_SHADOW_DX, y);
        }
    }

    /**
     * M5 part E: a torpedo's lit back over the chop (its body is drawn in the {@code sub} pass, where
     * the water's tint takes it to the sea's own colour): the sprite added at {@value LevelRenderer#TORPEDO_BACK}
     * strength, so it reads as a pale streak running just under the surface; fading with its range.
     */
    void drawTorpedoBacks(SpriteBatch batch, Sortie sortie, float alpha) {
        boolean any = false;
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            if (!runsUnder(shot)) {
                continue;
            }
            if (!any) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                any = true;
            }
            double left = shot.rangeLeft();
            batch.setColor(1, 1, 1, LevelRenderer.TORPEDO_BACK * (left < FADE_SHARE ? (float) (left / FADE_SHARE) : 1));
            drawCentred(batch, weapons.sprite(shot), shot.renderX(alpha), shot.renderY(alpha));
        }
        if (any) {
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** Whether a shot runs under the water and is drawn in the {@code sub} pass (a torpedo over water). */
    private boolean runsUnder(Shot shot) {
        return overWater && shot.weapon().delivery() == WeaponSpec.Delivery.TORPEDO;
    }

    /** M5 part E: the torpedoes' bodies in the {@code sub} pass, under the water, fading with their range. */
    void drawUnderWater(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            if (!runsUnder(shot)) {
                continue;
            }
            double left = shot.rangeLeft();
            batch.setColor(1, 1, 1, left < FADE_SHARE ? (float) (left / FADE_SHARE) : 1);
            drawCentred(batch, weapons.sprite(shot), shot.renderX(alpha), shot.renderY(alpha));
            batch.setColor(Color.WHITE);
        }
    }
}
