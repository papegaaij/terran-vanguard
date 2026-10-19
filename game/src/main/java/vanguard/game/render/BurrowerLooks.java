package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part B: the Burrower's looks (design/enemies/ground/burrower; tools/art/burrower.py, whose docstring
 * gives the timeline; user decision Q1 = a: it is fixed on the screen while it swims, bulges and is up, the
 * ground scrolling down under it). Everything is drawn on the ground layer with the ground units, no
 * shadow anywhere: buried, its ripple ({@code -ripple}, 16 headings by 4 frames, the heading its
 * ground-relative travel: the screen velocity minus the scroll's); the bulge's mound ({@code -bulge}) over
 * its 1.0 s tell; surfacing, the breach ({@code -breach}, 0.3 s) over the bow wave ({@code -bow}, a loop
 * under the body while it is up), then the body by its maw ({@code burrower_0..3}: shut, half, open,
 * firing as a ring goes) with the maw's glow added ({@code -glow}); the dive ({@code -dive}, its first
 * half over the bow). The hole a dive leaves ({@code -hole}, filling in over 1.6 s) and the crater of a
 * kill ({@code -crater}, kept) lie on the ground and scroll with it; the kill's burst, spout and tatters
 * are the generic death sets of its slug.
 */
final class BurrowerLooks {
    /** The ripple's head lies this far ahead of its sprite's centre along its heading, px (RIPPLE_HEAD). */
    static final double RIPPLE_HEAD = 12;
    /** The ripple's headings: 16, heading k travelling k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The ripple's frames a heading, at 12 fps (5 steps a frame). */
    static final int RIPPLE_FRAMES = 4;

    static final int RIPPLE_FRAME_TICKS = 5;
    /** The bow wave's loop at 15 fps (4 steps a frame). */
    static final int BOW_FRAME_TICKS = 4;
    /** The breach's frames at 20 fps over the surfaced window's first 0.3 s. */
    static final double BREACH_FRAME_SECONDS = 0.05;
    /** The breach's frames from which the bow wave lies under it. */
    static final int BREACH_BOW_FROM = 2;
    /** The dive's frames at 20 fps; the bow under its first four. */
    static final double DIVE_FRAME_SECONDS = 0.05;

    static final int DIVE_BOW_UNTIL = 4;
    /** The hole fills in at 5 fps (12 steps a frame), then is gone. */
    static final int HOLE_FRAME_TICKS = 12;
    /** Below this ground-relative travel, px a step, the ripple keeps the unit's facing. */
    private static final double STILL = 0.05;
    /** The maw's frames: shut, half open, open, open firing. */
    static final int SHUT = 0;

    static final int HALF = 1;
    static final int OPEN = 2;
    static final int FIRING = 3;
    /**
     * The maw's timeline round a ring (the docstring's 0.30 s half, 0.35 s open, 0.40–0.50 s firing for
     * the ring at 0.4 s; then open until 1.20 s, half until 1.30 s, shut), as offsets from the ring.
     */
    static final double HALF_BEFORE = 0.10;

    static final double OPEN_BEFORE = 0.05;
    static final double FIRING_FOR = 0.10;
    static final double OPEN_AFTER = 0.80;
    static final double HALF_AFTER = 0.90;
    /** The second ring's (hard) maw: half again 0.30 s after it fires (the docstring's 1.70 s). */
    static final double SECOND_HALF_AFTER = 0.30;

    /** The craters kept on the ground (a level's fields leave at most a few dozen; the oldest goes). */
    private static final int CRATERS = 32;
    /** The holes filling in at once. */
    private static final int HOLES = 16;
    /** A ground mark is drawn while its centre lies within this margin of the screen, px. */
    private static final double MARGIN = 60;

    private final List<EnemySpec> kinds;
    /** Per enemy kind its frames; null for a kind that does not burrow (or has no art). */
    private final Frames[] frames;

    private final float[] craterX = new float[CRATERS];
    /** A crater's height on the ground: its screen height plus the ground scroll as it was made. */
    private final double[] craterGround = new double[CRATERS];

    private final int[] craterKind = new int[CRATERS];
    private int craters;
    private int nextCrater;

    private final float[] holeX = new float[HOLES];
    private final float[] holeY = new float[HOLES];
    /** A hole's height on the ground once it is laid, as {@link #craterGround}. */
    private final double[] holeGround = new double[HOLES];
    /** The step its dive ends (the hole is laid then, the dive's last frame being the open hole). */
    private final long[] holeDue = new long[HOLES];

    private final int[] holeKind = new int[HOLES];
    /** Whether a slot holds a hole, waiting for its dive's end or filling in. */
    private final boolean[] holeUsed = new boolean[HOLES];
    /** Whether a slot's hole is laid (its dive ended), filling in from {@link #holeDue}. */
    private final boolean[] holeLaid = new boolean[HOLES];

    /** A burrowing kind's frames (tools/art/burrower.py). */
    record Frames(
            Array<AtlasRegion> body,
            Array<AtlasRegion> glow,
            Array<AtlasRegion> bow,
            Array<AtlasRegion> ripple,
            Array<AtlasRegion> bulge,
            Array<AtlasRegion> breach,
            Array<AtlasRegion> dive,
            Array<AtlasRegion> hole,
            AtlasRegion crater) {}

    BurrowerLooks(Sprites sprites, List<EnemySpec> kinds) {
        this.kinds = kinds;
        frames = new Frames[kinds.size()];
        for (int k = 0; k < frames.length; k++) {
            EnemySpec kind = kinds.get(k);
            if (kind.burrow().isPresent()) {
                frames[k] = frames(sprites, kind.slug());
            }
        }
    }

    /** A burrowing kind's frames, or null while its slug has no full set. */
    static Frames frames(Sprites sprites, String slug) {
        for (String part : List.of("", "-glow", "-bow", "-ripple", "-bulge", "-breach", "-dive", "-hole", "-crater")) {
            if (!sprites.has(slug + part)) {
                return null;
            }
        }
        return new Frames(
                sprites.frames(slug),
                sprites.frames(slug + "-glow"),
                sprites.frames(slug + "-bow"),
                sprites.frames(slug + "-ripple"),
                sprites.frames(slug + "-bulge"),
                sprites.frames(slug + "-breach"),
                sprites.frames(slug + "-dive"),
                sprites.frames(slug + "-hole"),
                sprites.region(slug + "-crater"));
    }

    /** Whether a burrowing unit of {@code kind} has its looks (else it is drawn as any unit while up, not at all while buried). */
    boolean has(int kind) {
        return kind < frames.length && frames[kind] != null;
    }

    /** A new attempt: the ground marks are gone. */
    void restart() {
        craters = 0;
        nextCrater = 0;
        java.util.Arrays.fill(holeUsed, false);
    }

    /**
     * After each simulation step: a dive lays its hole at the dive's end (where the unit sank, then
     * scrolling with the ground), a kill its crater; a unit killed in its dive's first half leaves the
     * crater only.
     */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        long tick = sortie.tick();
        for (int i = 0; i < events.size(); i++) {
            int kind = events.value(i);
            switch (events.type(i)) {
                case BURROW_DIVE -> {
                    if (has(kind)) {
                        int diveTicks = SimStep.ticks(
                                kinds.get(kind).burrow().orElseThrow().diveSeconds());
                        dive(kind, events.x(i), events.y(i), tick + diveTicks);
                    }
                }
                case ENEMY_DESTROYED -> {
                    if (has(kind)) {
                        crater(kind, events.x(i), events.y(i) + sortie.groundScroll());
                        cancelHole(events.x(i), events.y(i));
                    }
                }
                default -> {}
            }
        }
        for (int h = 0; h < HOLES; h++) {
            if (holeUsed[h] && !holeLaid[h] && tick >= holeDue[h]) {
                holeLaid[h] = true;
                holeGround[h] = holeY[h] + sortie.groundScroll();
            }
            if (holeUsed[h]
                    && holeLaid[h]
                    && tick - holeDue[h] >= (long) HOLE_FRAME_TICKS * frames[holeKind[h]].hole.size) {
                holeUsed[h] = false;
            }
        }
    }

    private void dive(int kind, double x, double y, long due) {
        for (int h = 0; h < HOLES; h++) {
            if (!holeUsed[h]) {
                holeUsed[h] = true;
                holeLaid[h] = false;
                holeKind[h] = kind;
                holeX[h] = (float) x;
                holeY[h] = (float) y;
                holeDue[h] = due;
                return;
            }
        }
    }

    private void cancelHole(double x, double y) {
        for (int h = 0; h < HOLES; h++) {
            if (holeUsed[h] && !holeLaid[h] && Math.abs(holeX[h] - x) < 1 && Math.abs(holeY[h] - y) < 1) {
                holeUsed[h] = false;
            }
        }
    }

    private void crater(int kind, double x, double ground) {
        craterX[nextCrater] = (float) x;
        craterGround[nextCrater] = ground;
        craterKind[nextCrater] = kind;
        nextCrater = (nextCrater + 1) % CRATERS;
        craters = Math.min(CRATERS, craters + 1);
    }

    /** On the ground, under the ground units: the craters, then the holes filling in, at the render's ground {@code scroll}. */
    void drawGround(SpriteBatch batch, long tick, float alpha, double scroll) {
        for (int c = 0; c < craters; c++) {
            double y = craterGround[c] - scroll;
            if (y > -MARGIN && y < PlayField.HEIGHT + MARGIN) {
                centred(batch, frames[craterKind[c]].crater, craterX[c], y);
            }
        }
        for (int h = 0; h < HOLES; h++) {
            if (!holeUsed[h] || !holeLaid[h]) {
                continue;
            }
            Array<AtlasRegion> hole = frames[holeKind[h]].hole;
            int frame = (int) ((tick - holeDue[h]) / HOLE_FRAME_TICKS);
            if (frame >= 0 && frame < hole.size) {
                centred(batch, hole.get(frame), holeX[h], holeGround[h] - scroll);
            }
        }
    }

    /**
     * A burrowing unit on the ground layer, by its phase: the ripple while buried and swimming, the bulge,
     * the breach, the body in the bow wave with its maw's glow, the dive. {@code flash} draws a frame
     * flashing white just after a hit (only while it is up, the only time it can be hit).
     */
    void drawUnit(SpriteBatch batch, Enemy enemy, long tick, float alpha, double groundSpeed, Flasher flash) {
        Frames look = frames[enemy.kind()];
        EnemySpec.Burrow burrow = enemy.spec().burrow().orElseThrow();
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        double progress = enemy.burrowProgress(alpha);
        switch (enemy.burrowPhase()) {
            case BULGE -> centred(batch, look.bulge.get(frame(progress, look.bulge.size)), x, y);
            case SURFACED -> {
                double t = progress * burrow.surfacedSeconds();
                int breach = (int) (t / BREACH_FRAME_SECONDS);
                if (breach < look.breach.size) {
                    if (breach >= BREACH_BOW_FROM) {
                        centred(batch, bow(look, tick), x, y);
                    }
                    flash.draw(batch, enemy, look.breach.get(breach), x, y);
                    return;
                }
                centred(batch, bow(look, tick), x, y);
                int maw = Math.min(look.body.size - 1, maw(t, burrow));
                flash.draw(batch, enemy, look.body.get(maw), x, y);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                centred(batch, look.glow.get(Math.min(look.glow.size - 1, maw)), x, y);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            case DIVE -> {
                int dive = Math.min(look.dive.size - 1, (int) (progress * burrow.diveSeconds() / DIVE_FRAME_SECONDS));
                if (dive < DIVE_BOW_UNTIL) {
                    centred(batch, bow(look, tick), x, y);
                }
                flash.draw(batch, enemy, look.dive.get(dive), x, y);
            }
            case NONE -> {}
            default -> {
                // Buried (entering, swimming, waiting, escaping): the ripple on its ground-relative heading.
                double dx = enemy.renderX(1) - enemy.renderX(0);
                double dy = enemy.renderY(1) - enemy.renderY(0) + groundSpeed * SimStep.SECONDS;
                int heading = heading(dx, dy, enemy.facing());
                double angle = heading * 2 * Math.PI / HEADINGS;
                int frame = (int) (tick / RIPPLE_FRAME_TICKS % RIPPLE_FRAMES);
                centred(
                        batch,
                        look.ripple.get(Math.min(look.ripple.size - 1, heading * RIPPLE_FRAMES + frame)),
                        x + RIPPLE_HEAD * Math.sin(angle),
                        y + RIPPLE_HEAD * Math.cos(angle));
            }
        }
    }

    private static TextureRegion bow(Frames look, long tick) {
        return look.bow.get((int) (tick / BOW_FRAME_TICKS % look.bow.size));
    }

    /** The frame {@code progress} (0 to 1) through a phase whose frames span it. */
    static int frame(double progress, int frames) {
        return Math.clamp((int) Math.floor(progress * frames), 0, frames - 1);
    }

    /**
     * The ripple's heading for a ground-relative travel of (dx, dy) px a step (y up): heading k travels k x
     * 22.5 degrees clockwise from straight down (4 left, 8 up, 12 right); {@code facing} (radians clockwise
     * from straight down) while it hardly moves on the ground.
     */
    static int heading(double dx, double dy, double facing) {
        double angle = Math.hypot(dx, dy) < STILL ? facing : Math.atan2(-dx, -dy);
        int k = (int) Math.round(angle / (2 * Math.PI) * HEADINGS);
        return Math.floorMod(k, HEADINGS);
    }

    /**
     * The maw {@code t} s after it surfaced (burrower.py's timeline): shut, half open just before the
     * ring, open, firing for 0.1 s as the ring goes, open, half, shut; a hard second ring opens it again
     * (half 0.1 s before, firing as it goes, half 0.3 s after).
     */
    static int maw(double t, EnemySpec.Burrow burrow) {
        double first = burrow.ring().atSeconds();
        if (burrow.secondRing().isPresent()) {
            double second = burrow.secondRing().get().atSeconds();
            if (t >= first + OPEN_AFTER) {
                if (t < second - HALF_BEFORE) {
                    return HALF;
                }
                if (t < second) {
                    return OPEN;
                }
                if (t < second + FIRING_FOR) {
                    return FIRING;
                }
                return t < second + SECOND_HALF_AFTER ? OPEN : HALF;
            }
        }
        if (t < first - HALF_BEFORE) {
            return SHUT;
        }
        if (t < first - OPEN_BEFORE) {
            return HALF;
        }
        if (t < first) {
            return OPEN;
        }
        if (t < first + FIRING_FOR) {
            return FIRING;
        }
        if (t < first + OPEN_AFTER) {
            return OPEN;
        }
        return t < first + HALF_AFTER ? HALF : SHUT;
    }

    /** Draws a unit's frame centred at (x, y), flashing white just after a hit. */
    interface Flasher {
        void draw(SpriteBatch batch, Enemy enemy, AtlasRegion frame, double x, double y);
    }

    private static void centred(SpriteBatch batch, TextureRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
