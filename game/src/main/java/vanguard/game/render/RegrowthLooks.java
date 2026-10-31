package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Regrowing;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies/ground/graft-turret, Regrowth): a regrowing turret's cycle as the player sees it, on the
 * stand-in sprite until the Graft Turret's own art (part C) brings the wreck, the root and the knit frames. While the
 * root is exposed the turret's sprite lies dark as its burst-open shell, the root a knot of teal tissue under it pulsing
 * from {@value #PULSE_FROM_HZ} to {@value #PULSE_TO_HZ} Hz as the window closes (the timer is the pulse: {@link #pulse})
 * with a thin teal ring round it shrinking to nothing over the window ({@link #ringRadius}); as the window closes the
 * turret knits back ({@link #drawKnit}: it grows in from the wreck, teal-veined, over the knit). The glows are {@link
 * SoftGlow}'s, smooth and drawn once.
 */
final class RegrowthLooks {
    static final double PULSE_FROM_HZ = 1;
    static final double PULSE_TO_HZ = 4;
    /** The tissue's teal (the hybrids' {@code 00FF9A}). */
    static final Color TEAL = Color.valueOf("00FF9A");
    /** The knot's dark tissue under the glow. */
    static final Color KNOT = new Color(0.05f, 0.22f, 0.17f, 0.92f);
    /** The burst shell (the wreck the root shows under): the turret's sprite this dark. */
    static final Color SHELL = new Color(0.3f, 0.28f, 0.27f, 0.85f);
    /** The ring starts this many times the root's larger side out from its centre (as a radius). */
    static final double RING_START = 1.1;
    /** A knitting turret grows from this scale and opacity to full. */
    static final float KNIT_FROM = 0.55f;

    private final SoftGlow glow;
    /** Per enemy kind: whether it regrows. */
    private final boolean[] regrows;

    RegrowthLooks(List<EnemySpec> kinds, SoftGlow glow) {
        this.glow = glow;
        regrows = new boolean[kinds.size()];
        for (int k = 0; k < regrows.length; k++) {
            regrows[k] = kinds.get(k).regrowth().isPresent();
        }
    }

    /** Whether any of the level's kinds regrows. */
    static boolean any(List<EnemySpec> kinds) {
        return kinds.stream().anyMatch(kind -> kind.regrowth().isPresent());
    }

    /**
     * The root's pulse (0..1) {@code share} of the way through a window of {@code windowSeconds}: a heartbeat whose rate
     * rises linearly from {@value #PULSE_FROM_HZ} to {@value #PULSE_TO_HZ} Hz, its phase the rate's integral, so it
     * quickens smoothly; 1 at the window's start.
     */
    static double pulse(double share, double windowSeconds) {
        double s = Math.clamp(share, 0, 1);
        double cycles = windowSeconds * (PULSE_FROM_HZ * s + (PULSE_TO_HZ - PULSE_FROM_HZ) * s * s / 2);
        double beat = 0.5 + 0.5 * Math.cos(2 * Math.PI * cycles);
        return beat * beat;
    }

    /** The pulse's rate {@code share} of the way through the window, Hz. */
    static double pulseHz(double share) {
        return PULSE_FROM_HZ + (PULSE_TO_HZ - PULSE_FROM_HZ) * Math.clamp(share, 0, 1);
    }

    /** The window ring's radius {@code share} of the way through it, for a root {@code size} px across: to nothing. */
    static double ringRadius(double share, double size) {
        return RING_START * size * (1 - Math.clamp(share, 0, 1));
    }

    /** How far a knit has run (0 as it starts, 1 standing) with {@code left} of its {@code total} steps. */
    static double knitShare(int left, int total) {
        return total <= 0 ? 1 : Math.clamp(1 - (double) left / total, 0, 1);
    }

    /** Whether a unit's turret is not standing (its root shows, or it knits): its body is drawn here, not as a unit. */
    static boolean regrowing(Enemy enemy) {
        return enemy.spec().regrowth().isPresent() && enemy.regrowth().phase() != Regrowing.Phase.TURRET;
    }

    /**
     * A regrowing unit's body in the unit pass, its {@code frame} at (x, y): the burst shell while the root shows; the
     * turret knitting back over it (growing in, teal-veined) while it knits.
     */
    static void drawBody(SpriteBatch batch, SoftGlow glow, Enemy enemy, TextureRegion frame, double x, double y) {
        Regrowing cycle = enemy.regrowth();
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        if (cycle.phase() == Regrowing.Phase.ROOT) {
            batch.setColor(SHELL);
            batch.draw(frame, Math.round(PixelScreen.PLAY_FIELD_X + x - w / 2), Math.round(y - h / 2));
            batch.setColor(Color.WHITE);
            return;
        }
        EnemySpec.Regrowth spec = enemy.spec().regrowth().orElseThrow();
        double share = knitShare(cycle.knitTicks(), SimStep.ticks(spec.knitSeconds()));
        float grown = (float) (KNIT_FROM + (1 - KNIT_FROM) * share);
        batch.setColor(SHELL);
        batch.draw(frame, Math.round(PixelScreen.PLAY_FIELD_X + x - w / 2), Math.round(y - h / 2));
        batch.setColor(1, 1, 1, grown);
        batch.draw(
                frame,
                Math.round(PixelScreen.PLAY_FIELD_X + x - w / 2),
                Math.round(y - h / 2),
                w / 2,
                h / 2,
                w,
                h,
                grown,
                grown,
                0);
        // The tissue's teal veins over the knitting turret, fading as it stands.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(TEAL.r, TEAL.g, TEAL.b, 0.55f * (float) (1 - share));
        glow.disc(batch, x, y, Math.max(w, h) * grown);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }

    /** The exposed roots over the ground units: the knot, its pulse and its window's ring. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.kind() >= regrows.length
                    || !regrows[enemy.kind()]
                    || enemy.regrowth().phase() != Regrowing.Phase.ROOT) {
                continue;
            }
            EnemySpec.Regrowth spec = enemy.spec().regrowth().orElseThrow();
            double size = Math.max(spec.rootHitbox().width(), spec.rootHitbox().height());
            double share = enemy.regrowth().windowShare();
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            batch.setColor(KNOT);
            glow.disc(batch, x, y, size * 1.5);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            float beat = (float) pulse(share, spec.windowSeconds());
            batch.setColor(TEAL.r, TEAL.g, TEAL.b, 0.25f + 0.65f * beat);
            glow.disc(batch, x, y, size * (1.0 + 0.25 * beat));
            double ring = ringRadius(share, size);
            if (ring > 1) {
                batch.setColor(TEAL.r, TEAL.g, TEAL.b, 0.8f);
                glow.ring(batch, x, y, ring);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(Color.WHITE);
    }
}
