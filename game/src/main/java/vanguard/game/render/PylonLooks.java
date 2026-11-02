package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.Domes;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part G, step G4: the Shield Pylon's looks beyond its fixed base (design/enemies/ground/shield-pylon; Level 41's
 * pylon gates; tools/art/shield_pylon.py's sprites and assets/pivots/shield-pylon.json), a look only; its dome is {@link
 * PylonDomeLooks}':
 *
 * <ul>
 *   <li>the mast and its gold crown over the base at 16 facings, the emitter leaning toward its dome (the facing from
 *       the pylon to its dome's centre, else the unit's own facing), a hit flashing both;
 *   <li>the emitter's lens lit with a slow pulse once the pylon wakes in its gate's halt ({@code -emitter}, additive,
 *       {@value #EMITTER_TICKS} steps a frame); dark while it sleeps (its dome already lit, the shots passing);
 *   <li>its death: the burnt base with the mast's stump ({@code -wreck}) and the crown toppling to the lower right over
 *       0.4 s ({@code -topple}, 15 fps, its last frame staying), both on the ground until they leave the screen; the
 *       medium vacuum burst and the dome's collapse into the crown are the screen's and {@link PylonDomeLooks}'.
 * </ul>
 *
 * Nothing is allocated per frame.
 */
final class PylonLooks {
    static final String SLUG = "shield-pylon";
    /** The crown's facings ({@code -crown_0..15}: k × 22.5° clockwise from straight down). */
    static final int FACINGS = 16;
    /** The emitter's pulse, steps a frame (the pivots' {@code steps_per_frame}). */
    static final int EMITTER_TICKS = 15;
    /** The topple, steps a frame (15 fps). */
    static final int TOPPLE_TICKS = SimStep.PER_SECOND / 15;
    /** A wreck is forgotten once this far below the bottom edge, px. */
    static final double GONE_BELOW = 60;

    private static final int WRECKS = 16;
    private static final double[] ORIGIN = {0, 0};

    private final boolean[] pylon;
    private final AtlasRegion base;
    private final Array<AtlasRegion> crown;
    private final Array<AtlasRegion> emitter;
    private final AtlasRegion wreck;
    private final Array<AtlasRegion> topple;
    /** Per facing the lens's place, px from the pylon's centre (the pivots' {@code lens}, y down). */
    private final double[][] lens;

    /** The wrecks: the death's step, x, the place on the ground (y + scroll then). */
    private final long[] wreckTick = new long[WRECKS];

    private final double[] wreckX = new double[WRECKS];
    private final double[] wreckGround = new double[WRECKS];
    private final boolean[] wreckUsed = new boolean[WRECKS];
    private int nextWreck;

    PylonLooks(Sprites sprites, List<EnemySpec> kinds, JsonValue pivots) {
        pylon = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            pylon[k] = kinds.get(k).slug().equals(SLUG);
        }
        base = sprites != null && sprites.has(SLUG) ? sprites.region(SLUG) : null;
        crown = optional(sprites, SLUG + "-crown");
        emitter = optional(sprites, SLUG + "-emitter");
        wreck = sprites != null && sprites.has(SLUG + "-wreck") ? sprites.region(SLUG + "-wreck") : null;
        topple = optional(sprites, SLUG + "-topple");
        lens = lens(pivots);
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static double[][] lens(JsonValue pivots) {
        JsonValue list = pivots == null ? null : pivots.get("lens");
        if (list == null || !list.isArray()) {
            return new double[0][];
        }
        double[][] points = new double[list.size][];
        int k = 0;
        for (JsonValue point = list.child; point != null; point = point.next, k++) {
            points[k] = point.asDoubleArray();
        }
        return points;
    }

    /** The level restarts: no wreck. */
    void restart() {
        Arrays.fill(wreckUsed, false);
        nextWreck = 0;
    }

    boolean isPylon(int kind) {
        return kind >= 0 && kind < pylon.length && pylon[kind];
    }

    /** Whether these looks draw {@code enemy} (a Shield Pylon, with its base and crown art). */
    boolean draws(Enemy enemy) {
        return isPylon(enemy.kind()) && base != null && crown.size == FACINGS;
    }

    /** The crown's facing for a heading of {@code radians} clockwise from straight down: round(h × 16 / 2π) mod 16. */
    static int facing(double radians) {
        return Math.floorMod((int) Math.round(radians * FACINGS / (2 * Math.PI)), FACINGS);
    }

    /** The heading from (x, y) to (toX, toY) on the play field (y up), radians clockwise from straight down. */
    static double heading(double x, double y, double toX, double toY) {
        return Math.atan2(-(toX - x), -(toY - y));
    }

    /** The topple's frame {@code age} steps after the death: its 6 frames at 15 fps, the last staying. */
    static int toppleFrame(long age, int frames) {
        return Math.clamp(age / TOPPLE_TICKS, 0, Math.max(0, frames - 1));
    }

    /** The emitter's frame at step {@code tick} for a pylon (its serial staggers the pulses); -1 while it sleeps. */
    static int emitterFrame(long tick, int serial, boolean dormant, int frames) {
        if (dormant || frames <= 0) {
            return -1;
        }
        return (int) Math.floorMod(tick / EMITTER_TICKS + serial, (long) frames);
    }

    /** {@code enemy}'s crown facing: toward its dome's centre, else its own facing. */
    int crownFacing(Sortie sortie, Enemy enemy) {
        Domes domes = sortie.domes();
        for (int slot = 0; domes != null && slot < domes.capacity(); slot++) {
            if (domes.tracked(slot) && domes.pylon(slot) == enemy.serial()) {
                return facing(heading(enemy.renderX(1), enemy.renderY(1), domes.x(slot), domes.y(slot)));
            }
        }
        return EnemyLooks.heading(enemy.facing(), FACINGS);
    }

    /** After each step: a pylon's death leaves its wreck and its toppling crown where it died. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED && isPylon(events.value(i))) {
                wreckTick[nextWreck] = sortie.tick();
                wreckX[nextWreck] = events.x(i);
                wreckGround[nextWreck] = events.y(i) + sortie.groundScroll();
                wreckUsed[nextWreck] = true;
                nextWreck = (nextWreck + 1) % WRECKS;
            }
        }
    }

    /** Under the ground units: the wrecks and their toppled crowns, scrolling with the ground. */
    void drawGround(SpriteBatch batch, Sortie sortie, double scroll) {
        for (int w = 0; w < WRECKS; w++) {
            if (!wreckUsed[w]) {
                continue;
            }
            double y = wreckGround[w] - scroll;
            if (y < -GONE_BELOW) {
                wreckUsed[w] = false;
                continue;
            }
            if (wreck != null) {
                drawCentred(batch, wreck, wreckX[w], y);
            }
            if (!topple.isEmpty()) {
                drawCentred(batch, topple.get(toppleFrame(sortie.tick() - wreckTick[w], topple.size)), wreckX[w], y);
            }
        }
    }

    /** The pylon: its base and its crown at its facing, a hit flashing both. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        flasher.draw(batch, enemy, base, x, y);
        flasher.draw(batch, enemy, crown.get(crownFacing(sortie, enemy)), x, y);
    }

    /** After the grade, additive: the awake pylons' emitters on their lenses. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (emitter.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            int frame = emitterFrame(sortie.tick(), enemy.serial(), enemy.dormant(), emitter.size);
            if (frame < 0) {
                continue;
            }
            int k = crownFacing(sortie, enemy);
            double[] at = k < lens.length ? lens[k] : ORIGIN;
            drawCentred(batch, emitter.get(frame), enemy.renderX(alpha) + at[0], enemy.renderY(alpha) - at[1]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** For the tests: the lens's place at facing {@code k}, px from the centre (y down). */
    double[] lens(int k) {
        return k < lens.length ? lens[k] : ORIGIN;
    }
}
