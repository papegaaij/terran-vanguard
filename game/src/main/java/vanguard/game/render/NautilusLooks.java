package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.CoilPhase;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M8 part E: the Spiral Nautilus (design/enemies/naval/spiral-nautilus; tools/art/spiral_nautilus.py, round 06 a)
 * through its coil ({@link Enemy#coilPhase}). Closed (spiralling in, breaking outward, leaving) its shell rolls: one
 * of its {@value #SPIN} spin frames a {@value #ROLL_PX} px it travels, the way of its orbit. Uncoiled, from the
 * opening's first step through the close, the shell holds still with its aperture toward the ship ({@code -open} at
 * its {@value #HEADINGS} headings) and the crown is drawn at the pivot file's {@code crown_offset} from the shell's
 * centre: emerging over the opening before the tell (steps 0-2), the tell (5) over the coil's tell, the open sway (3
 * and 4 at {@value #SWAY_FPS} fps) while its spiral fires, and emerging backwards over the close's first {@code
 * opening} seconds. After the grade, ungraded: the closed shell's violet lip ({@code -lip}, per spin frame) or the
 * crown's glow ({@code -glow}: open, tell, emerging) and the lime-grey flecks of a shot glancing off the closed shell
 * ({@code -fleck}, {@value #FLECK_FRAME_TICKS} steps a frame, at the hit). Leaving through a wall's rock it is drawn
 * under the walls ({@link #underWall}), so it swims in behind the wall's face. Its death ({@link DeepDeaths}): the
 * shell cracked into halves sinking ({@code -halves}, the two crack axes in turn, {@value #HALVES_FRAME_TICKS} steps a
 * frame) and, killed uncoiled, the dark crown ({@code -dark}) at its heading sinking with them. Presentation only.
 */
final class NautilusLooks {
    /** The unit's slug. */
    static final String SLUG = "spiral-nautilus";
    /** The closed shell's spin frames and the px rolled a frame. */
    static final int SPIN = 24;

    static final double ROLL_PX = 22;
    /** The open shell's and the crown's headings (toward the ship). */
    static final int HEADINGS = 16;
    /** The crown's steps a heading: emerging 0-2, the sway 3 and 4, the tell 5; the glow's: open, tell, emerging. */
    static final int CROWN_STEPS = 6;

    static final int EMERGED = 2;
    static final int SWAY = 3;
    static final int CROWN_TELL = 5;
    static final int GLOWS = 3;
    static final int GLOW_OPEN = 0;
    static final int GLOW_TELL = 1;
    static final int GLOW_EMERGING = 2;
    /** The open sway's rate. */
    static final double SWAY_FPS = 5;
    /** The flecks' and the halves' frames show this many steps each (20 and 10 fps); the halves' steps an axis. */
    static final int FLECK_FRAME_TICKS = 3;

    static final int HALVES_FRAME_TICKS = 6;
    static final int HALVES_STEPS = 8;
    /** The dark crown sinks with the halves: this many steps, at this speed, px/s. */
    static final int DARK_TICKS = HALVES_STEPS * HALVES_FRAME_TICKS;

    static final double DARK_SINK = 12;
    /** A glance's event lies within this distance beyond a closed shell's hit box, px. */
    static final double GLANCE_REACH = 6;
    /** The Nautili followed at once and the flecks shown at once. */
    static final int SLOTS = 16;

    static final int FLECKS = 16;
    /** A death's event lies within this distance of the Nautilus last seen there, px. */
    private static final double DEATH_REACH = 24;

    private final Array<AtlasRegion> closed;
    private final Array<AtlasRegion> lip;
    private final Array<AtlasRegion> open;
    private final Array<AtlasRegion> crown;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> fleck;
    /** The halves per crack axis, and the dark crown per heading as an animation of {@link #DARK_TICKS} (built once). */
    private final Array<Array<AtlasRegion>> halves = new Array<>();

    private final Array<Array<AtlasRegion>> dark = new Array<>();
    private final Array<AtlasRegion> none = new Array<>(0);
    private final boolean[] nautilus;
    /** Per kind its coil's opening and tell, s (for the crown's steps), and its spiral-in time. */
    private final double[] openingSeconds;

    private final double[] tellSeconds;
    private final double[] inSeconds;
    /** Per heading and emerging step (0-2) the crown's centre from the shell's (x right, y up), px. */
    private final float[][][] crownAt = new float[HEADINGS][EMERGED + 1][2];
    /** The Nautili followed by serial (-1 free): the distance rolled, the last place and heading, whether uncoiled. */
    private final int[] serials = new int[SLOTS];

    private final double[] rolled = new double[SLOTS];
    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final int[] lastHeading = new int[SLOTS];
    private final boolean[] opened = new boolean[SLOTS];
    private final boolean[] crownOut = new boolean[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    /** The flecks shown: their place and their first step (Long.MIN_VALUE: free). */
    private final double[] fleckX = new double[FLECKS];

    private final double[] fleckY = new double[FLECKS];
    private final long[] fleckTick = new long[FLECKS];
    /** The crack axis of the next death (they take turns). */
    private int axis;
    /** The dark crown's offset from the last death asked for it ({@link #darkCrown}). */
    private float darkDx;

    private float darkDy;

    NautilusLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        closed = optional(sprites, SLUG);
        lip = optional(sprites, SLUG + "-lip");
        open = optional(sprites, SLUG + "-open");
        crown = optional(sprites, SLUG + "-crown");
        glow = optional(sprites, SLUG + "-glow");
        fleck = optional(sprites, SLUG + "-fleck");
        Array<AtlasRegion> split = optional(sprites, SLUG + "-halves");
        for (int a = 0; a < 2 && split.size >= 2 * HALVES_STEPS; a++) {
            Array<AtlasRegion> steps = new Array<>(HALVES_STEPS);
            for (int k = 0; k < HALVES_STEPS; k++) {
                steps.add(split.get(a * HALVES_STEPS + k));
            }
            halves.add(steps);
        }
        Array<AtlasRegion> darkCrowns = optional(sprites, SLUG + "-dark");
        for (int h = 0; h < HEADINGS && darkCrowns.size >= HEADINGS; h++) {
            Array<AtlasRegion> held = new Array<>(HALVES_STEPS);
            for (int k = 0; k < HALVES_STEPS; k++) {
                held.add(darkCrowns.get(h));
            }
            dark.add(held);
        }
        nautilus = new boolean[kinds.size()];
        openingSeconds = new double[kinds.size()];
        tellSeconds = new double[kinds.size()];
        inSeconds = new double[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec kind = kinds.get(k);
            nautilus[k] = kind.slug().equals(SLUG) && kind.coil().isPresent();
            if (nautilus[k]) {
                openingSeconds[k] = kind.coil().get().openingSeconds();
                tellSeconds[k] = kind.coil().get().tellSeconds();
                inSeconds[k] = kind.coil().get().inSeconds();
            }
        }
        JsonValue pivot = pivots.apply(SLUG);
        JsonValue offsets = pivot == null ? null : pivot.get("crown_offset");
        for (int h = 0; offsets != null && h < HEADINGS && h < offsets.size; h++) {
            for (int s = 0; s <= EMERGED && s < offsets.get(h).size; s++) {
                crownAt[h][s][0] = (float) offsets.get(h).get(s).getDouble(0);
                crownAt[h][s][1] = (float) -offsets.get(h).get(s).getDouble(1);
            }
        }
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** A new attempt: none followed, no fleck. */
    void restart() {
        Arrays.fill(serials, -1);
        Arrays.fill(fleckTick, Long.MIN_VALUE);
        axis = 0;
    }

    /** Whether kind {@code kind} is the Spiral Nautilus. */
    boolean nautilus(int kind) {
        return kind >= 0 && kind < nautilus.length && nautilus[kind];
    }

    /** Whether {@code enemy} is a Spiral Nautilus with its art. */
    boolean draws(Enemy enemy) {
        return closed.size >= SPIN
                && open.size >= HEADINGS
                && crown.size >= HEADINGS * CROWN_STEPS
                && nautilus(enemy.kind());
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** The heading (of {@value #HEADINGS}, 0 down, clockwise) from (x, y) toward (toX, toY). */
    static int headingToward(double x, double y, double toX, double toY) {
        return EnemyLooks.heading(Math.atan2(-(toX - x), -(toY - y)), HEADINGS);
    }

    /** The spin frame after {@code rolled} px, rolling clockwise or not. */
    static int spinFrame(double rolled, boolean clockwise) {
        long step = (long) Math.floor(rolled / ROLL_PX);
        return (int) Math.floorMod(clockwise ? -step : step, (long) SPIN);
    }

    /**
     * The crown's step in the coil's {@code phase} at {@code progress} through it: emerging over the opening before
     * the tell ({@code opening} s, the last {@code tell} s of it the tell), the sway while open, emerging backwards
     * over the close's first {@code opening} s of the spiral-in ({@code in} s; only once it opened); -1 for the shell
     * closed.
     */
    static int crownStep(
            CoilPhase phase,
            double progress,
            boolean tell,
            boolean wasOpen,
            double opening,
            double tellSeconds,
            double in,
            long tick,
            int serial) {
        return switch (phase) {
            case OPENING -> {
                if (tell) {
                    yield CROWN_TELL;
                }
                double before = opening <= 0 ? 1 : Math.max(1e-6, 1 - tellSeconds / opening);
                yield Math.min(EMERGED, (int) (progress / before * (EMERGED + 1)));
            }
            case OPEN ->
                SWAY + (int) Math.floorMod((long) Math.floor(tick * SWAY_FPS / SimStep.PER_SECOND) + serial, 2L);
            case CLOSED -> {
                double since = progress * in;
                yield !wasOpen || opening <= 0 || since >= opening
                        ? -1
                        : EMERGED - Math.min(EMERGED, (int) (since / opening * (EMERGED + 1)));
            }
            default -> -1;
        };
    }

    /** The crown's glow for crown step {@code step}: open (the sway), the tell, emerging. */
    static int glowFor(int step) {
        return step == CROWN_TELL ? GLOW_TELL : step >= SWAY ? GLOW_OPEN : GLOW_EMERGING;
    }

    /** Its crown's step now (-1: its shell is closed). */
    private int crownStep(Sortie sortie, Enemy enemy) {
        int s = slot(enemy.serial());
        int k = enemy.kind();
        return crownStep(
                enemy.coilPhase(),
                enemy.coilProgress(),
                enemy.coilTell(),
                s >= 0 && opened[s],
                openingSeconds[k],
                tellSeconds[k],
                inSeconds[k],
                sortie.tick(),
                enemy.serial());
    }

    private static int heading(Sortie sortie, Enemy enemy, float alpha) {
        return headingToward(
                enemy.renderX(alpha),
                enemy.renderY(alpha),
                sortie.ship().renderX(alpha),
                sortie.ship().renderY(alpha));
    }

    private int spin(Enemy enemy) {
        int s = slot(enemy.serial());
        return spinFrame(s < 0 ? 0 : rolled[s], enemy.coilClockwise());
    }

    /** Its body's frame now (the closed shell's spin or the open shell; its shadow's too); null for another unit. */
    AtlasRegion frame(Sortie sortie, Enemy enemy, float alpha) {
        if (!draws(enemy)) {
            return null;
        }
        return crownStep(sortie, enemy) < 0 ? closed.get(spin(enemy)) : open.get(heading(sortie, enemy, alpha));
    }

    /** A Nautilus: its shell, and uncoiled its crown, flashing after a hit. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        int step = crownStep(sortie, enemy);
        if (step < 0) {
            flasher.draw(batch, enemy, closed.get(spin(enemy)), x, y);
            return;
        }
        int h = heading(sortie, enemy, alpha);
        flasher.draw(batch, enemy, open.get(h), x, y);
        float[] at = crownAt[h][Math.min(step, EMERGED)];
        flasher.draw(batch, enemy, crown.get(h * CROWN_STEPS + step), x + at[0], y + at[1]);
    }

    /** Whether it is leaving through rock now: drawn under the walls, behind their face ({@link #drawUnderWalls}). */
    boolean underWall(Sortie sortie, Enemy enemy, float alpha) {
        Terrain terrain = sortie.terrain();
        return terrain != null
                && draws(enemy)
                && enemy.coilPhase() == CoilPhase.LEAVING
                && terrain.rock(
                        sortie.groundScroll(),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha),
                        enemy.hitbox().width() / 2,
                        enemy.hitbox().height() / 2);
    }

    /** Under the walls: the Nautili leaving through the rock. */
    void drawUnderWalls(SpriteBatch batch, Sortie sortie, float alpha, BurrowerLooks.Flasher flasher) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (underWall(sortie, enemy, alpha)) {
                draw(batch, sortie, enemy, alpha, flasher);
            }
        }
    }

    /** After each step: the Nautili's travel, heading and coil, the shots glancing off a closed shell. */
    void stepped(Sortie sortie) {
        if (closed.isEmpty()) {
            return;
        }
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!nautilus(enemy.kind())) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            int s = slot(enemy.serial());
            if (s < 0) {
                s = slot(-1);
                if (s < 0) {
                    continue;
                }
                serials[s] = enemy.serial();
                rolled[s] = 0;
                opened[s] = false;
                lastX[s] = x;
                lastY[s] = y;
            }
            seen[s] = true;
            rolled[s] += Math.hypot(x - lastX[s], y - lastY[s]);
            lastX[s] = x;
            lastY[s] = y;
            lastHeading[s] = heading(sortie, enemy, 1);
            CoilPhase phase = enemy.coilPhase();
            opened[s] |= phase == CoilPhase.OPEN;
            crownOut[s] = phase == CoilPhase.OPENING || phase == CoilPhase.OPEN;
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
        for (int f = 0; f < FLECKS; f++) {
            if (fleckTick[f] != Long.MIN_VALUE
                    && sortie.tick() - fleckTick[f] >= (long) fleck.size * FLECK_FRAME_TICKS) {
                fleckTick[f] = Long.MIN_VALUE;
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.SHOT_GLANCED && glancedOffShell(sortie, events.x(e), events.y(e))) {
                fleck(events.x(e), events.y(e), sortie.tick());
            }
        }
    }

    /** Whether a glance at (x, y) is off a closed Nautilus's shell. */
    private boolean glancedOffShell(Sortie sortie, double x, double y) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (nautilus(enemy.kind())
                    && enemy.armoured()
                    && Math.abs(enemy.renderX(1) - x) <= enemy.hitbox().width() / 2 + GLANCE_REACH
                    && Math.abs(enemy.renderY(1) - y) <= enemy.hitbox().height() / 2 + GLANCE_REACH) {
                return true;
            }
        }
        return false;
    }

    private void fleck(double x, double y, long tick) {
        int oldest = 0;
        for (int f = 0; f < FLECKS; f++) {
            if (fleckTick[f] == Long.MIN_VALUE) {
                oldest = f;
                break;
            }
            if (fleckTick[f] < fleckTick[oldest]) {
                oldest = f;
            }
        }
        fleckX[oldest] = x;
        fleckY[oldest] = y;
        fleckTick[oldest] = tick;
    }

    /** The fleck's frame {@code ticks} steps after the glance: -1 once played (or before it). */
    static int fleckFrame(long ticks, int frames) {
        long frame = ticks / FLECK_FRAME_TICKS;
        return ticks < 0 || frame >= frames ? -1 : (int) frame;
    }

    /** After the grade, additive: the closed shells' lip, the crowns' glow, the glances' flecks. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (closed.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy) || underWall(sortie, enemy, alpha)) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            int step = crownStep(sortie, enemy);
            if (step < 0) {
                if (lip.size >= SPIN) {
                    drawCentred(batch, lip.get(spin(enemy)), x, y);
                }
            } else if (glow.size >= HEADINGS * GLOWS) {
                int h = heading(sortie, enemy, alpha);
                float[] at = crownAt[h][Math.min(step, EMERGED)];
                drawCentred(batch, glow.get(h * GLOWS + glowFor(step)), x + at[0], y + at[1]);
            }
        }
        for (int f = 0; f < FLECKS && !fleck.isEmpty(); f++) {
            if (fleckTick[f] == Long.MIN_VALUE) {
                continue;
            }
            int frame = fleckFrame(sortie.tick() - fleckTick[f], fleck.size);
            if (frame < 0) {
                continue;
            }
            drawCentred(batch, fleck.get(frame), fleckX[f], fleckY[f]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** A killed Nautilus's halves (the crack axes in turn); empty for another kind. */
    Array<AtlasRegion> halves(int kind) {
        if (!nautilus(kind) || halves.isEmpty()) {
            return none;
        }
        axis = 1 - axis;
        return halves.get(axis);
    }

    /**
     * A Nautilus killed uncoiled near (x, y): its dark crown at its last heading (its offset from the kill in {@link
     * #darkDx()}, {@link #darkDy()}); empty for another kind or a closed shell.
     */
    Array<AtlasRegion> darkCrown(int kind, double x, double y) {
        if (!nautilus(kind) || dark.isEmpty()) {
            return none;
        }
        int found = -1;
        double best = DEATH_REACH;
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] < 0) {
                continue;
            }
            double d = Math.hypot(lastX[s] - x, lastY[s] - y);
            if (d <= best) {
                best = d;
                found = s;
            }
        }
        if (found < 0 || !crownOut[found]) {
            return none;
        }
        int h = lastHeading[found];
        darkDx = crownAt[h][EMERGED][0];
        darkDy = crownAt[h][EMERGED][1];
        return dark.get(h);
    }

    float darkDx() {
        return darkDx;
    }

    float darkDy() {
        return darkDy;
    }
}
