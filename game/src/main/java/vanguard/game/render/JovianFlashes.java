package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Arrays;
import vanguard.sim.Enemy;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A: two small additive tells of Act 6, {@link SoftGlow}'s smooth light, drawn once:
 *
 * <ul>
 *   <li>the three-way war's hit (design/enemies, The three-way war: "a hunter's hit on a prey flashes teal where it
 *       lands"): a teal flash widening and fading over {@value #PREY_TICKS} steps at the hit ({@link #preyHit});
 *   <li>the IFF spoofer's stutter (design/player/systems, Act 6: "while a turret hesitates its muzzle glow flickers
 *       amber, a searching stutter"): from the held volley's {@code SPOOF_HESITATE} for the spoofer's
 *       {@value #STUTTER_SECONDS} s, an amber glow at the turret's muzzle, on and off in an uneven rhythm ({@link
 *       #stutterOn}), following the unit.
 * </ul>
 */
final class JovianFlashes {
    static final Color TEAL = RegrowthLooks.TEAL;
    static final Color AMBER = Color.valueOf("FFB020");
    /** A prey hit's flash: this long, steps, from this size to that, px across. */
    static final int PREY_TICKS = 12;

    static final double PREY_FROM = 14;
    static final double PREY_TO = 28;
    /** The spoofer's delay (design/player/systems: the module's {@code spoof} delay), the stutter's length. */
    static final double STUTTER_SECONDS = 0.5;
    /** The stutter's rhythm: on or off for {@value #STUTTER_STEP_TICKS} steps per entry, uneven (searching). */
    static final boolean[] STUTTER = {true, false, true, true, false, true, false, false, true, true};

    static final int STUTTER_STEP_TICKS = 3;
    /** The muzzle glow: this far along the turret's facing (a share of its larger hit-box side), this big. */
    static final double MUZZLE_REACH = 0.4;

    static final double MUZZLE_SIZE = 16;
    private static final int SLOTS = 16;

    private final SoftGlow glow;
    private final long[] preyTick = new long[SLOTS];
    private final double[] preyX = new double[SLOTS];
    private final double[] preyY = new double[SLOTS];
    private int nextPrey;
    private final int[] stutterSerial = new int[SLOTS];
    private final long[] stutterTick = new long[SLOTS];
    private int nextStutter;

    JovianFlashes(SoftGlow glow) {
        this.glow = glow;
        restart();
    }

    void restart() {
        Arrays.fill(preyTick, -1);
        Arrays.fill(stutterSerial, -1);
        nextPrey = nextStutter = 0;
    }

    /** A hunter struck a prey unit at (x, y) at step {@code tick} ({@code PREY_HIT}). */
    void preyHit(double x, double y, long tick) {
        preyTick[nextPrey] = tick;
        preyX[nextPrey] = x;
        preyY[nextPrey] = y;
        nextPrey = (nextPrey + 1) % SLOTS;
    }

    /** A spoofable unit's volley is held at step {@code tick} ({@code SPOOF_HESITATE} at the unit (x, y)). */
    void hesitate(Sortie sortie, double x, double y, long tick) {
        Enemy unit = null;
        double best = 4;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double d = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (d <= best) {
                best = d;
                unit = enemy;
            }
        }
        if (unit == null) {
            return;
        }
        for (int s = 0; s < SLOTS; s++) {
            if (stutterSerial[s] == unit.serial()) {
                stutterTick[s] = tick;
                return;
            }
        }
        stutterSerial[nextStutter] = unit.serial();
        stutterTick[nextStutter] = tick;
        nextStutter = (nextStutter + 1) % SLOTS;
    }

    /** A prey flash's opacity {@code age} steps after the hit; 0 once it has faded. */
    static float preyOpacity(long age) {
        if (age < 0 || age >= PREY_TICKS) {
            return 0;
        }
        double s = (double) age / PREY_TICKS;
        return (float) (1 - s * s);
    }

    /** Whether the stutter's glow shows {@code age} steps after the volley was held; never once the delay is over. */
    static boolean stutterOn(long age) {
        if (age < 0 || age >= SimStep.ticks(STUTTER_SECONDS)) {
            return false;
        }
        return STUTTER[(int) (age / STUTTER_STEP_TICKS % STUTTER.length)];
    }

    /** The prey flashes and the stutters, additive, over the units. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        long now = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int p = 0; p < SLOTS; p++) {
            if (preyTick[p] < 0) {
                continue;
            }
            long age = now - preyTick[p];
            float opacity = preyOpacity(age);
            if (opacity <= 0) {
                preyTick[p] = -1;
                continue;
            }
            double size = PREY_FROM + (PREY_TO - PREY_FROM) * age / PREY_TICKS;
            batch.setColor(TEAL.r, TEAL.g, TEAL.b, opacity);
            glow.disc(batch, preyX[p], preyY[p], size);
            batch.setColor(TEAL.r, TEAL.g, TEAL.b, 0.6f * opacity);
            glow.ring(batch, preyX[p], preyY[p], size * 0.6);
        }
        for (int s = 0; s < SLOTS; s++) {
            if (stutterSerial[s] < 0) {
                continue;
            }
            long age = now - stutterTick[s];
            if (age >= SimStep.ticks(STUTTER_SECONDS)) {
                stutterSerial[s] = -1;
                continue;
            }
            Enemy unit = bySerial(sortie, stutterSerial[s]);
            if (unit == null) {
                stutterSerial[s] = -1;
                continue;
            }
            if (!stutterOn(age)) {
                continue;
            }
            double reach =
                    MUZZLE_REACH * Math.max(unit.hitbox().width(), unit.hitbox().height());
            double x = unit.renderX(alpha) - Math.sin(unit.facing()) * reach;
            double y = unit.renderY(alpha) - Math.cos(unit.facing()) * reach;
            batch.setColor(AMBER.r, AMBER.g, AMBER.b, 0.9f);
            glow.disc(batch, x, y, MUZZLE_SIZE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }

    private static Enemy bySerial(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).serial() == serial) {
                return sortie.enemy(i);
            }
        }
        return null;
    }
}
