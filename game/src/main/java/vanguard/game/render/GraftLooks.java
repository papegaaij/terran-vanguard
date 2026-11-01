package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.Regrowing;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C, step C4b (design/enemies/ground/graft-turret, Look; tools/art/aurelia_units.py, whose docstring gives the
 * sprites): a regrowing turret drawn from its production frames, a look only. Under the ground units: its fixed base
 * ({@code -base}) whatever its phase, and the scorched stump ({@code -stump}) a root's death leaves on the deck,
 * scrolling with the ground until it leaves the screen. In the unit pass: the turret at the nearest of its 32 aims ({@link
 * EnemyLooks}' frames), in its regrown, veined frames ({@code -regrown}) once it has died and knitted back; its burst
 * shell ({@code -wreck}) over the exposed root (the root and its pulse are {@link RegrowthLooks}'); the knit ({@code
 * -knit}, 8 frames over the knit's time) with the regrown turret of its aim fading in over its last quarter. Over the
 * units: the red glint on the muzzle ({@code talon-glint}, on {@code pivots/graft-turret.json}'s {@code muzzles}) over
 * the {@value #GLINT_SECONDS} s before each pair, timed from the gun's last shot like {@link CeresLooks}' glints, and
 * the root's crumble ({@code -crumble}, additive, {@value #CRUMBLE_FRAME_TICKS} steps a frame) over its stump.
 */
final class GraftLooks {
    /** The glint before each pair (the unit's 0.25 s tell). */
    static final double GLINT_SECONDS = 0.25;
    /** The crumble's frames show this many steps each. */
    static final int CRUMBLE_FRAME_TICKS = 2;
    /** A shot leaves the barrel within this of the turret's centre, px (the muzzle about 23 px out). */
    static final double MUZZLE_REACH = 32;
    /** The knit's regrown turret fades in over this last share of it. */
    static final double KNIT_FADE = 0.25;

    private static final int SLOTS = 16;
    private static final int STUMPS = 24;

    /** A kind's frames: base, turret and regrown by aim, wreck, knit, stump, crumble; the muzzle per aim. */
    record Art(
            AtlasRegion base,
            Array<AtlasRegion> regrown,
            AtlasRegion wreck,
            Array<AtlasRegion> knit,
            AtlasRegion stump,
            Array<AtlasRegion> crumble,
            double[][] muzzles) {}

    /** Per enemy kind its frames, null for a kind without them. */
    private final Art[] art;

    private final Array<AtlasRegion> glint;
    /** The guns followed (serials, -1 free): when each came into view and its last shot. */
    private final int[] gunSerial = new int[SLOTS];

    private final long[] gunSeen = new long[SLOTS];
    private final long[] gunShot = new long[SLOTS];
    private int nextGun;
    /** The stumps on the deck: the step the root died (-1 free), its kind, x and ground row (y plus the scroll then). */
    private final long[] stumpTick = new long[STUMPS];

    private final int[] stumpKind = new int[STUMPS];
    private final double[] stumpX = new double[STUMPS];
    private final double[] stumpRow = new double[STUMPS];
    private int nextStump;

    GraftLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        art = new Art[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec kind = kinds.get(k);
            String slug = kind.slug();
            if (kind.regrowth().isEmpty() || !sprites.has(slug + "-base")) {
                continue;
            }
            Array<AtlasRegion> none = new Array<>(0);
            art[k] = new Art(
                    sprites.region(slug + "-base"),
                    sprites.has(slug + "-regrown") ? sprites.frames(slug + "-regrown") : none,
                    sprites.has(slug + "-wreck") ? sprites.region(slug + "-wreck") : null,
                    sprites.has(slug + "-knit") ? sprites.frames(slug + "-knit") : none,
                    sprites.has(slug + "-stump") ? sprites.region(slug + "-stump") : null,
                    sprites.has(slug + "-crumble") ? sprites.frames(slug + "-crumble") : none,
                    muzzles(pivots.apply(slug)));
        }
        glint = sprites.has("talon-glint") ? sprites.frames("talon-glint") : new Array<>(0);
        restart();
    }

    /** The muzzle per aim from {@code muzzles[k]} ([x, y] px from the frame's top left), relative to its centre, y up. */
    static double[][] muzzles(JsonValue pivots) {
        JsonValue muzzles = pivots == null ? null : pivots.get("muzzles");
        if (muzzles == null) {
            return new double[0][];
        }
        double half = pivots.get("size").getDouble(0) / 2;
        double[][] at = new double[muzzles.size][2];
        for (int k = 0; k < muzzles.size; k++) {
            at[k][0] = muzzles.get(k).getDouble(0) - half;
            at[k][1] = half - muzzles.get(k).getDouble(1);
        }
        return at;
    }

    /** Whether any of the level's kinds has its frames here. */
    boolean any() {
        for (Art a : art) {
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    private Art artOf(int kind) {
        return kind >= 0 && kind < art.length ? art[kind] : null;
    }

    /** The level restarts: no stump lies, no gun is followed. */
    void restart() {
        Arrays.fill(gunSerial, -1);
        Arrays.fill(stumpTick, -1);
        nextGun = 0;
        nextStump = 0;
    }

    /** After each step: the guns' clocks (a turret coming into view, its shots), a root's death leaving its stump. */
    void stepped(Sortie sortie) {
        if (!any()) {
            return;
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (artOf(enemy.kind()) != null && slotOf(enemy.serial()) < 0) {
                int slot = nextGun;
                nextGun = (nextGun + 1) % SLOTS;
                gunSerial[slot] = enemy.serial();
                gunSeen[slot] = tick;
                gunShot[slot] = Long.MIN_VALUE / 2;
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            switch (events.type(e)) {
                case ENEMY_FIRED -> shot(sortie, events.x(e), events.y(e), tick);
                case REGROWN -> woke(sortie, events.x(e), events.y(e), tick);
                case ROOT_KILLED -> {
                    if (artOf(events.value(e)) != null) {
                        stumpTick[nextStump] = tick;
                        stumpKind[nextStump] = events.value(e);
                        stumpX[nextStump] = events.x(e);
                        stumpRow[nextStump] = events.y(e) + sortie.groundScroll();
                        nextStump = (nextStump + 1) % STUMPS;
                    }
                }
                default -> {}
            }
        }
    }

    /** A shot at (x, y): the last shot of the turret it left, if it is one of ours. */
    private void shot(Sortie sortie, double x, double y, long tick) {
        Enemy turret = nearest(sortie, x, y, MUZZLE_REACH);
        int slot = turret == null ? -1 : slotOf(turret.serial());
        if (slot >= 0) {
            gunShot[slot] = tick;
        }
    }

    /** A turret regrown at (x, y): its gun's clock starts again from its wake (its first volley then). */
    private void woke(Sortie sortie, double x, double y, long tick) {
        Enemy turret = nearest(sortie, x, y, 1);
        int slot = turret == null ? -1 : slotOf(turret.serial());
        if (slot >= 0) {
            EnemySpec.Regrowth regrowth = turret.spec().regrowth().orElseThrow();
            EnemyGun gun = turret.spec().gun().orElse(null);
            double interval = gun == null ? 0 : gun.intervalSeconds();
            // The next volley comes its wake after the knit: as a shot that long before the interval runs out.
            gunShot[slot] = tick + SimStep.ticks(regrowth.wakeSeconds()) - SimStep.ticks(interval);
        }
    }

    private Enemy nearest(Sortie sortie, double x, double y, double reach) {
        Enemy found = null;
        double best = reach;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (artOf(enemy.kind()) == null) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (d <= best) {
                best = d;
                found = enemy;
            }
        }
        return found;
    }

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (gunSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /**
     * Steps until the turret's next pair as its gun's clock predicts (its interval after its last shot, or its first-shot
     * delay after it came into view); {@link Long#MAX_VALUE} without a clock.
     */
    long untilShot(Enemy enemy, long tick) {
        int slot = slotOf(enemy.serial());
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (slot < 0 || gun == null) {
            return Long.MAX_VALUE;
        }
        long due = gunShot[slot] > Long.MIN_VALUE / 2
                ? gunShot[slot] + SimStep.ticks(gun.intervalSeconds())
                : gunSeen[slot] + SimStep.ticks(gun.firstShotDelay());
        return due - tick;
    }

    /** The knit frame at {@code share} of the knit (0 as it starts, 1 standing) of {@code frames}. */
    static int knitFrame(double share, int frames) {
        return Math.clamp((int) Math.floor(Math.clamp(share, 0, 1) * frames), 0, Math.max(0, frames - 1));
    }

    /** The regrown turret's opacity over the knit at {@code share}: none, then in over the last {@value #KNIT_FADE}. */
    static float knitTurret(double share) {
        return (float) Math.clamp((share - (1 - KNIT_FADE)) / KNIT_FADE, 0, 1);
    }

    /** The turret's frame in the unit pass: its regrown frame of its aim once it has knitted back; null for its own. */
    AtlasRegion frame(Enemy enemy, EnemyLooks look) {
        Art a = artOf(enemy.kind());
        if (a == null
                || a.regrown().size == 0
                || !enemy.regrowth().paid()
                || enemy.regrowth().phase() != Regrowing.Phase.TURRET) {
            return null;
        }
        return a.regrown().get(EnemyLooks.heading(enemy.facing(), a.regrown().size));
    }

    /** Whether it draws the unit's body in the unit pass: a turret of ours with its root out or knitting back. */
    boolean drawsBody(Enemy enemy) {
        return artOf(enemy.kind()) != null && RegrowthLooks.regrowing(enemy);
    }

    /** The body: the burst shell while the root shows; the knit, the regrown turret fading in over its end. */
    void drawBody(SpriteBatch batch, Enemy enemy, float alpha) {
        Art a = artOf(enemy.kind());
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        Regrowing cycle = enemy.regrowth();
        if (cycle.phase() == Regrowing.Phase.ROOT) {
            if (a.wreck() != null) {
                drawCentred(batch, a.wreck(), x, y);
            }
            return;
        }
        EnemySpec.Regrowth spec = enemy.spec().regrowth().orElseThrow();
        double share = RegrowthLooks.knitShare(cycle.knitTicks(), SimStep.ticks(spec.knitSeconds()));
        if (a.knit().size > 0) {
            drawCentred(batch, a.knit().get(knitFrame(share, a.knit().size)), x, y);
        }
        float turret = knitTurret(share);
        if (turret > 0 && a.regrown().size > 0) {
            batch.setColor(1, 1, 1, turret);
            drawCentred(batch, a.regrown().get(EnemyLooks.heading(enemy.facing(), a.regrown().size)), x, y);
            batch.setColor(Color.WHITE);
        }
    }

    /** Under the ground units: the stumps on the deck (scrolling with it), then the live turrets' bases. */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (!any()) {
            return;
        }
        for (int s = 0; s < STUMPS; s++) {
            Art a = stumpTick[s] < 0 ? null : artOf(stumpKind[s]);
            if (a == null || a.stump() == null) {
                continue;
            }
            double y = stumpRow[s] - scroll;
            if (y < -a.stump().getRegionHeight()) {
                stumpTick[s] = -1;
                continue;
            }
            drawCentred(batch, a.stump(), stumpX[s], y);
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Art a = artOf(enemy.kind());
            if (a != null) {
                drawCentred(batch, a.base(), enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
    }

    /** The red glints on the muzzles before each pair and the roots' crumbles, additive (the batch is in additive blending). */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (!any()) {
            return;
        }
        long tick = sortie.tick();
        for (int s = 0; s < STUMPS; s++) {
            Art a = stumpTick[s] < 0 ? null : artOf(stumpKind[s]);
            long age = tick - stumpTick[s];
            if (a == null || a.crumble().size == 0 || age < 0 || age >= (long) a.crumble().size * CRUMBLE_FRAME_TICKS) {
                continue;
            }
            drawCentred(batch, a.crumble().get((int) (age / CRUMBLE_FRAME_TICKS)), stumpX[s], stumpRow[s] - scroll);
        }
        if (glint.size == 0) {
            return;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Art a = artOf(enemy.kind());
            if (a == null
                    || a.muzzles().length == 0
                    || enemy.empStunned()
                    || enemy.regrowth().phase() != Regrowing.Phase.TURRET
                    || !onScreen(enemy)) {
                continue;
            }
            double share = CeresLooks.tellShare(untilShot(enemy, tick), GLINT_SECONDS);
            if (share < 0) {
                continue;
            }
            AtlasRegion frame = glint.get(Math.min(glint.size - 1, (int) (share * glint.size)));
            double[] muzzle = a.muzzles()[EnemyLooks.heading(enemy.facing(), a.muzzles().length)];
            drawCentred(batch, frame, enemy.renderX(alpha) + muzzle[0], enemy.renderY(alpha) + muzzle[1]);
        }
    }

    private static boolean onScreen(Enemy enemy) {
        double y = enemy.renderY(1);
        return y >= 0 && y <= PlayField.HEIGHT;
    }
}
