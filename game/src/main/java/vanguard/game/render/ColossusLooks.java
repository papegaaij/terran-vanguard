package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
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
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.Debris;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part H: the breaching arena boss's looks (the Dust Colossus, design/enemies/bosses/dust-colossus;
 * tools/art/dust_colossus.py's sprites and assets/pivots/dust-colossus.json; briefs/l21-art-names.md), following the
 * simulation's {@link BreachArena}. On the ground layer ({@link #drawGround}), back to front: the craters its dives left
 * (the funnel's last frame, scrolling on with the ground), the funnel at a risen mouth (collapsing at the rise, a loop
 * while it is up, refilling in the dive), after its death the ridge's sunken furrow and the collapsed crater (Level
 * 21's {@code colossus-furrow} and {@code colossus-sinkhole}, laid at the kill and scrolling with the ground), its
 * body under the sand between breaches along the breach's curve with the travelling ridge's sprites ({@link
 * RidgeBody}, shared; the head pushing toward the next point, behind a risen mouth its last stretch up to the neck,
 * sinking with the dive), the bulge and crack ring of a tell, an eruption's marker, the mouth in its five poses (the
 * rise's three steps, half open, open with the gullet lit, the bite's tell with the teeth lit, the snap, the dive's two
 * steps; a hit flashing it at most once each 0.25 s, {@link HitFlash}), the gullet's glow before a ring (additive,
 * pulsing), the bite's circle over the mouth through its tell (round 44 Q.10, step GFX4), behind a risen edge mouth
 * its back with the spine vents as organs on its segments (round 44 K.b, {@link ColossusSpine}: rising, up, venting
 * as they fire, destroyed), and after the kill the ground half of its death throe ({@link ColossusThroe}: the fissure ring, the
 * split ridge with its geysers; drawn from the kill on, though the kill takes the boss off the field). The tells'
 * cracks glow lime and the eruption marker's ring is traced (step GFX3). Over the ground units ({@link #drawStreaks}):
 * the suction's sand streaks, each with a dark shadow, running into an open mouth; the throe's body and shockwave.
 * With the debris ({@link #drawRocks}): the rocks it tears loose, tumbling; the throe's plume and rock rain. It shows
 * no edge warning (a boss never warns, 2026-10-21: its bulge and ridge are its tells); the throe's shake is {@link
 * #shakeX}. Presentation only; nothing is allocated per frame.
 */
final class ColossusLooks {
    /** The breach points, its sprites' poses: front, left, right, rear, under ({@link BreachArena.Pose}). */
    static final int POSES = BreachArena.POSES;
    /** The mouth's shut frames a pose: the rise's three steps, the snap, the dive's two. */
    static final int SHUT_STEPS = 6;
    /** The mouth's open frames a pose: half open, open (the gullet lit), the bite's tell (the teeth lit). */
    static final int OPEN_STEPS = 3;
    /** The open's first this long shows the half-open frame, s. */
    static final double HALF_OPEN_SECONDS = 0.3;
    /** The snap shows the shut frame this long at the dive's start, s. */
    static final double SNAP_SECONDS = 0.2;
    /** The funnel's loop while the mouth is up, frames a second; its frames: 0 rise, 1–3 loop, 4 dive, 5 crater. */
    static final double FUNNEL_FPS = 6;

    static final int FUNNEL_CRATER = 5;
    /** The bulge's and the marker's frames over their tell. */
    static final int TELL_FRAMES = 4;
    /**
     * Round 44 Q.10 (step GK): the bite's circle during its tell, its bright ring's opacity and its dark edge's; step
     * GFX4: drawn over the mouth, a soft additive glow of this many rings each side at most this opacity.
     */
    static final float BITE_ALPHA = 0.95f;

    static final float BITE_EDGE_ALPHA = 0.4f;
    static final int BITE_GLOW_RINGS = 3;
    static final float BITE_GLOW_ALPHA = 0.3f;
    private static final Color BITE_WHITE = new Color(1f, 0.98f, 0.92f, 1f);
    /** A vent shows its venting frame this long after it fires, s. */
    static final double VENT_SECONDS = 0.25;
    /** A shot from within this of a vent's centre is its burst, px. */
    static final double VENT_REACH = 16;
    /** The rock's shapes, and their tumble steps (each this many steps). */
    static final int ROCK_SHAPES = 4;

    static final int ROCK_TUMBLE = 4;
    static final int ROCK_TUMBLE_TICKS = 6;
    /** The rock sprite's width its sizes scale from, px. */
    static final double ROCK_WIDTH = 26;
    /** The craters remembered at once (one a dive; the oldest gives way). */
    static final int CRATERS = 16;
    /** The suction's streaks: how many, their length and width, px, their speed inward, px/s, their opacity. */
    static final int STREAKS = 22;

    static final double STREAK_LENGTH = 16;
    static final float STREAK_WIDTH = 2;
    static final double STREAK_SPEED = 170;
    static final float STREAK_ALPHA = 0.85f;
    /** The streaks fade in over the open's first this long, s. */
    static final double STREAK_IN_SECONDS = 0.3;
    /** The design's suction, px/s: the streaks' opacity scales with the difficulty's against it. */
    static final double DESIGN_SUCTION = 45;

    private static final Color STREAK = new Color(1f, 0.93f, 0.78f, 1f);
    /** Step GFX3: each streak's dark shadow a pixel under it (the pale streaks vanished on the sand). */
    private static final Color STREAK_SHADOW = new Color(0.30f, 0.20f, 0.12f, 1f);

    static final float STREAK_SHADOW_ALPHA = 0.55f;
    static final float STREAK_SHADOW_WIDTH = 3;
    /** Step GFX3: the tells' crack glow (the Vrell lime), the eruption ring's trace and its dark edge. */
    private static final Color LIME = Color.valueOf("A8FF3A");

    private static final Color RING_EDGE = new Color(0.22f, 0.14f, 0.10f, 1f);
    static final float RING_ALPHA = 0.7f;
    static final float RING_EDGE_ALPHA = 0.45f;
    static final float RING_WIDTH = 2;
    static final int RING_SEGMENTS = 48;
    /** The crack glow's floor at a tell's start, the pulse's dip, and the one-pixel spread's share. */
    static final double GLOW_LEAST = 0.45;

    static final double GLOW_DIP = 0.6;
    static final float CRACK_SPREAD = 0.4f;
    private static final long NEVER = Long.MIN_VALUE / 2;

    private final FlashShader flash;
    private final HitFlash hits = new HitFlash();
    private final RidgeBody body;
    private final TextureRegion pixel;
    private final Array<AtlasRegion> shut;
    private final Array<AtlasRegion> mouth;
    private final Array<AtlasRegion> gullet;
    private final Array<AtlasRegion> vent;
    private final Array<AtlasRegion> bulge;
    private final Array<AtlasRegion> marker;
    private final Array<AtlasRegion> funnel;
    private final Array<AtlasRegion> rock;
    /** Round 44 K.b (step GK): the back behind a risen edge mouth, and the death throe. */
    private final ColossusSpine spine;

    private final ColossusThroe throe;
    /** Level 21's backdrop pieces laid at the kill: the collapsed crater, the furrow's 16 headings (null without). */
    private final AtlasRegion sinkhole;

    private final Array<AtlasRegion> furrow;
    /** Per pose: how far behind the mouth's centre the body leaves the sand (the pivots' neck), px. */
    private final double[] neck = new double[POSES];
    /** The breach's timings, s: the rise, the open (an eruption's), the dive, the death throe. */
    private final double riseSeconds;

    private final double openSeconds;
    private final double eruptOpenSeconds;
    private final double diveSeconds;
    private final double deathSeconds;
    /** The streaks' opacity: the difficulty's suction against the design's (none without a suction). */
    private final float streakShare;

    private final double suctionOuter;
    private final double suctionInner;

    /** The curve behind the current (or last) mouth, kept through its dive (the next point's curve replaces it). */
    private final double[] heldX = new double[BreachArena.RIDGE_POINTS];

    private final double[] heldY = new double[BreachArena.RIDGE_POINTS];
    private boolean held;
    /** The live curve, sampled this frame. */
    private final double[] curveX = new double[BreachArena.RIDGE_POINTS];

    private final double[] curveY = new double[BreachArena.RIDGE_POINTS];
    /** The body's points this frame, head first. */
    private final double[] xs = new double[RidgeLooks.MAX_POINTS];

    private final double[] ys = new double[RidgeLooks.MAX_POINTS];
    private final int[] headings = new int[RidgeLooks.MAX_POINTS];
    /** The craters: x and ground row (y up plus the scroll then), ring buffer. */
    private final double[] craterX = new double[CRATERS];

    private final double[] craterRow = new double[CRATERS];
    private int craters;
    private int nextCrater;
    /** The breach phase after the last step, to notice a dive's end. */
    private BreachArena.Phase lastPhase;
    /** Per vent: the step it last fired, the breach it was destroyed in (-1 alive). */
    private final long[] ventFired;

    private final int[] ventDied;
    /** The death: its step, the mouth's place (x and ground row), NEVER while it lives. */
    private long deathTick = NEVER;

    private double deathX;
    private double deathRow;
    /** The furrow at the kill: the body's last points (x, ground row, heading), head first. */
    private final double[] furrowX = new double[RidgeLooks.MAX_POINTS];

    private final double[] furrowRow = new double[RidgeLooks.MAX_POINTS];
    private final int[] furrowHeading = new int[RidgeLooks.MAX_POINTS];
    private int furrowPoints;
    /** Step GFX4: how far the furrow reaches from the mouth's place at the kill, px. */
    private double furrowReach;
    /** The body's point count drawn last (the furrow copies it at the kill). */
    private int points;

    private ColossusLooks(
            Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots, String backdropFolder) {
        this.flash = flash;
        String slug = boss.slug();
        body = sprites != null && RidgeBody.drawable(sprites) ? RidgeBody.of(sprites) : RidgeBody.none();
        pixel = sprites == null ? null : sprites.pixel;
        shut = optional(sprites, slug + "-shut");
        mouth = optional(sprites, slug + "-mouth");
        gullet = optional(sprites, slug + "-gullet");
        vent = optional(sprites, slug + "-vent");
        bulge = optional(sprites, slug + "-bulge");
        marker = optional(sprites, slug + "-marker");
        funnel = optional(sprites, slug + "-funnel");
        rock = optional(sprites, slug + "-rock");
        spine = new ColossusSpine(
                optional(sprites, slug + "-spine"), optional(sprites, RidgeLooks.ROOT + "-dust"), flash, pivots);
        throe = new ColossusThroe(
                optional(sprites, slug + "-rear"),
                optional(sprites, slug + "-collapse"),
                optional(sprites, slug + "-death"),
                optional(sprites, slug + "-geyser"),
                optional(sprites, slug + "-fissure"),
                optional(sprites, slug + "-plume"),
                optional(sprites, slug + "-debris"),
                optional(sprites, RidgeLooks.ROOT + "-dust"),
                pixel,
                flash);
        sinkhole = sprites != null && sprites.hasBackdrop(backdropFolder + SINKHOLE)
                ? sprites.backdrop(backdropFolder + SINKHOLE, 1).first()
                : null;
        furrow = sprites != null && sprites.hasBackdrop(backdropFolder + FURROW)
                ? sprites.backdrop(backdropFolder + FURROW, RidgeLooks.HEADINGS)
                : new Array<>(0);
        JsonValue necks = pivots == null ? null : pivots.get("neck");
        for (int p = 0; p < POSES; p++) {
            neck[p] = necks == null || necks.size <= p
                    ? 0
                    : Math.hypot(necks.get(p).getDouble(0), necks.get(p).getDouble(1));
        }
        BossSpec spec = boss.boss().orElseThrow();
        BossSpec.Breach breach = spec.breach().orElseThrow();
        riseSeconds = breach.riseSeconds();
        openSeconds = breach.openSeconds();
        diveSeconds = breach.diveSeconds();
        eruptOpenSeconds = spec.eruption().map(BossSpec.Eruption::openSeconds).orElse(openSeconds);
        deathSeconds = Math.max(spec.deathSeconds(), ColossusThroe.THROE_SECONDS);
        streakShare = (float) spec.suction()
                .map(suction -> Math.clamp(suction.speed() / DESIGN_SUCTION, 0, 1.5))
                .orElse(0.0)
                .doubleValue();
        suctionOuter = spec.suction().map(BossSpec.Suction::outerRadius).orElse(0.0);
        suctionInner = spec.suction().map(BossSpec.Suction::innerRadius).orElse(0.0);
        int vents = spec.vents().map(v -> v.parts().size()).orElse(0);
        ventFired = new long[vents];
        ventDied = new int[vents];
        reset();
    }

    /** Level 21's backdrop pieces the game lays at the kill (no placed entry, H.6). */
    static final String SINKHOLE = "colossus-sinkhole";

    static final String FURROW = "colossus-furrow";

    /**
     * The looks of the level's boss when it breaches (its data's {@code movement.breach}) and its mouth's sprites are
     * there; null otherwise.
     */
    static ColossusLooks of(
            Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots, String backdropFolder) {
        if (boss == null
                || boss.boss().flatMap(BossSpec::breach).isEmpty()
                || !sprites.has(boss.slug() + "-shut")
                || !sprites.has(boss.slug() + "-mouth")) {
            return null;
        }
        return new ColossusLooks(sprites, flash, boss, pivots, backdropFolder);
    }

    /**
     * Step GFX3, for the tests: the looks following a sortie of {@code boss} without its sprites (they draw nothing):
     * the death's state through the kill ({@link #drawsDeath}, {@link #deathAge}, the furrow and the crater's place).
     */
    static ColossusLooks following(SetPieceSpec boss, JsonValue pivots) {
        return new ColossusLooks(null, null, boss, pivots, "");
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The level restarts (or the fight is retried): no crater, no death, every vent alive. */
    void reset() {
        craters = 0;
        nextCrater = 0;
        lastPhase = null;
        held = false;
        deathTick = NEVER;
        furrowPoints = 0;
        points = 0;
        Arrays.fill(ventFired, NEVER);
        Arrays.fill(ventDied, -1);
        hits.reset();
    }

    // --- the frames --------------------------------------------------------------------------------

    /**
     * The mouth's shut step (0–5) {@code elapsed} s into {@code phase}: the rise's three steps over its length, the
     * snap for {@link #SNAP_SECONDS} at the dive's start, then the dive's two steps over the rest; -1 in another phase.
     */
    static int shutStep(BreachArena.Phase phase, double elapsed, double rise, double dive) {
        return switch (phase) {
            case RISE -> Math.clamp((int) Math.floor(elapsed / (rise / 3)), 0, 2);
            case DIVE -> elapsed < SNAP_SECONDS ? 3 : elapsed < SNAP_SECONDS + (dive - SNAP_SECONDS) / 2 ? 4 : 5;
            default -> -1;
        };
    }

    /** The mouth's open step (0–2) {@code elapsed} s into {@code phase}: half open, then open; the bite's tell; else -1. */
    static int openStep(BreachArena.Phase phase, double elapsed) {
        return switch (phase) {
            case OPEN -> elapsed < HALF_OPEN_SECONDS ? 0 : 1;
            case BITE -> 2;
            default -> -1;
        };
    }

    /** A tell's frame (the bulge's, the marker's) at {@code progress} (0–1) of it. */
    static int tellFrame(double progress) {
        return Math.clamp((int) Math.floor(progress * TELL_FRAMES), 0, TELL_FRAMES - 1);
    }

    /** The funnel's frame in {@code phase} at real {@code seconds}: 0 at the rise, 1–3 looping while up, 4 in the dive. */
    static int funnelFrame(BreachArena.Phase phase, double seconds) {
        return switch (phase) {
            case RISE -> 0;
            case OPEN, BITE -> 1 + (int) Math.floorMod((long) Math.floor(seconds * FUNNEL_FPS), 3L);
            case DIVE -> 4;
            default -> -1;
        };
    }

    /** Round 44 Q.10: whether the bite's circle shows on the sand in {@code phase}: through the bite's tell only. */
    static boolean biteCircle(BreachArena.Phase phase) {
        return phase == BreachArena.Phase.BITE;
    }

    /**
     * The body along a breach's curve ({@code samples} points from the last breach point to the next), head first, a
     * point every {@link RidgeLooks#SPACING} px back from the head at {@code head} (0–1) of the curve's length less
     * {@code trim} px, at most {@link RidgeLooks#LENGTH} px and {@code xs.length} points, each with the curve's heading
     * there (toward the head); returns how many.
     */
    static int layOut(
            double[] cx, double[] cy, int samples, double head, double trim, double[] xs, double[] ys, int[] headings) {
        double total = 0;
        for (int i = 1; i < samples; i++) {
            total += Math.hypot(cx[i] - cx[i - 1], cy[i] - cy[i - 1]);
        }
        double front = Math.clamp(head, 0, 1) * total - trim;
        if (!(front > 0)) {
            return 0;
        }
        int n = 0;
        int segment = samples - 1;
        double segmentStart = total - Math.hypot(cx[segment] - cx[segment - 1], cy[segment] - cy[segment - 1]);
        for (double s = front; s >= 0 && front - s <= RidgeLooks.LENGTH && n < xs.length; s -= RidgeLooks.SPACING) {
            while (segment > 1 && s < segmentStart) {
                segment--;
                segmentStart -= Math.hypot(cx[segment] - cx[segment - 1], cy[segment] - cy[segment - 1]);
            }
            double dx = cx[segment] - cx[segment - 1];
            double dy = cy[segment] - cy[segment - 1];
            double length = Math.hypot(dx, dy);
            double u = length > 0 ? Math.clamp((s - segmentStart) / length, 0, 1) : 0;
            xs[n] = cx[segment - 1] + dx * u;
            ys[n] = cy[segment - 1] + dy * u;
            headings[n] = RidgeLooks.heading(dx, dy);
            n++;
        }
        return n;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The breaching boss among the set pieces, or null. */
    private static SetPiece colossus(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.breach().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: a dive's crater, the curve behind the mouth, the vents' bursts and deaths, the kill. */
    void stepped(Sortie sortie) {
        SetPiece piece = colossus(sortie);
        if (piece == null) {
            return;
        }
        if (piece.destroyed()) {
            // Step GFX3: the kill takes it off the field at once (not present any more): its death plays on here.
            if (deathTick == NEVER) {
                died(sortie, piece);
            }
            return;
        }
        if (!piece.present()) {
            return;
        }
        BreachArena breach = piece.breach().orElseThrow();
        long tick = sortie.tick();
        BreachArena.Phase phase = breach.phase();
        if (lastPhase == BreachArena.Phase.DIVE && phase != BreachArena.Phase.DIVE) {
            addCrater(breach.mouthX(), breach.mouthY() + sortie.groundScroll());
        }
        lastPhase = phase;
        boolean up = phase == BreachArena.Phase.TELL
                || phase == BreachArena.Phase.RISE
                || phase == BreachArena.Phase.OPEN
                || phase == BreachArena.Phase.BITE;
        if ((up || (phase == BreachArena.Phase.DIVE && !breach.nextFixed())) && breach.breaches() > 0) {
            for (int i = 0; i < BreachArena.RIDGE_POINTS; i++) {
                heldX[i] = breach.ridgeX(i);
                heldY[i] = breach.ridgeY(i);
            }
            held = true;
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.ENEMY_FIRED) {
                for (int v = 0; v < ventFired.length; v++) {
                    if (breach.ventUp(v)
                            && Math.hypot(events.x(i) - breach.ventX(v), events.y(i) - breach.ventY(v)) <= VENT_REACH) {
                        ventFired[v] = tick;
                    }
                }
            }
        }
        for (int v = 0; v < ventDied.length; v++) {
            if (ventDied[v] < 0 && piece.partWrecked(breach.ventPart(v))) {
                ventDied[v] = breach.breaches();
            }
        }
    }

    /**
     * The kill: the mouth's place and the furrow along the body's last stretch behind it (the held curve up to the
     * neck, as {@link #drawBody} lays it behind a risen mouth; else the body drawn last), on the ground's rows.
     */
    private void died(Sortie sortie, SetPiece piece) {
        BreachArena breach = piece.breach().orElseThrow();
        double scroll = sortie.groundScroll();
        deathTick = sortie.tick();
        deathX = piece.renderX(1);
        deathRow = piece.renderY(1) + scroll;
        if (held) {
            furrowPoints = layOut(
                    heldX,
                    heldY,
                    BreachArena.RIDGE_POINTS,
                    1,
                    neck[breach.pose().ordinal()],
                    furrowX,
                    furrowRow,
                    furrowHeading);
        } else {
            furrowPoints = points;
            System.arraycopy(xs, 0, furrowX, 0, points);
            System.arraycopy(ys, 0, furrowRow, 0, points);
            System.arraycopy(headings, 0, furrowHeading, 0, points);
        }
        furrowReach = 0;
        for (int i = 0; i < furrowPoints; i++) {
            furrowReach = Math.max(furrowReach, Math.hypot(furrowX[i] - deathX, furrowRow[i] - deathRow + scroll));
            furrowRow[i] += scroll;
        }
    }

    /** Whether its death (the chain, then the furrow and the collapsed crater) is drawn: from the kill on. */
    boolean drawsDeath(Sortie sortie) {
        SetPiece piece = colossus(sortie);
        return piece != null && piece.destroyed() && deathTick != NEVER;
    }

    /** How long ago it was killed, s, between the last step and the next ({@code alpha}); -1 while it lives. */
    double deathAge(long tick, float alpha) {
        return deathTick == NEVER ? -1 : (tick + alpha - deathTick) * SimStep.SECONDS;
    }

    /** How far the furrow and the collapsed crater have faded in {@code age} s after the kill (0–1). */
    float settled(double age) {
        return ColossusThroe.settled(age);
    }

    /** The death throe's length, s (the boss's death or the throe's, the longer). */
    double deathSeconds() {
        return deathSeconds;
    }

    /** The furrow's points laid at the kill. */
    int furrowPoints() {
        return furrowPoints;
    }

    /** The collapsed crater's centre: x, and its ground row (y up plus the scroll at the kill). */
    double deathX() {
        return deathX;
    }

    double deathRow() {
        return deathRow;
    }

    /** The furrow's point {@code i} (head first): x and ground row. */
    double furrowX(int i) {
        return furrowX[i];
    }

    double furrowRow(int i) {
        return furrowRow[i];
    }

    private void addCrater(double x, double row) {
        craterX[nextCrater] = x;
        craterRow[nextCrater] = row;
        nextCrater = (nextCrater + 1) % CRATERS;
        craters = Math.min(CRATERS, craters + 1);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * On the ground layer, after the backdrop's ground pieces and before the ground units: the craters, the funnel,
     * after the kill the furrow and the collapsed crater, the body, a tell's bulge or marker, the mouth (or its death),
     * the gullet's glow and the vents.
     */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double scroll, double realSeconds) {
        SetPiece piece = colossus(sortie);
        drawCraters(batch, scroll);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (piece.destroyed()) {
            // Step GFX3: drawn though the kill took it off the field (not present).
            drawDeath(batch, tick, alpha, scroll, realSeconds);
            return;
        }
        if (!piece.present()) {
            return;
        }
        BreachArena breach = piece.breach().orElseThrow();
        BreachArena.Phase phase = breach.phase();
        int pose = breach.pose().ordinal();
        double x = breach.mouthX();
        double y = breach.mouthY();
        double elapsed = elapsed(breach, phase, pose, alpha);
        int funnelAt = funnelFrame(phase, realSeconds);
        if (funnelAt >= 0 && funnel.size > funnelAt) {
            drawCentred(batch, funnel.get(funnelAt), x, y);
        }
        drawBody(batch, breach, phase, elapsed, realSeconds);
        boolean back = held && ColossusSpine.shows(breach.pose()) && spine.drawable();
        if (back) {
            // Step GSR: the back rises out of the ridge drawn under it, on the ridge's own track.
            spine.lay(heldX, heldY, BreachArena.RIDGE_POINTS);
            spine.at(phase, elapsed, riseSeconds, diveSeconds);
            spine.draw(batch, realSeconds);
        }
        if (breach.marker()) {
            if (marker.size >= TELL_FRAMES) {
                double progress = breach.markerProgress();
                AtlasRegion ring = marker.get(tellFrame(progress));
                drawCentred(batch, ring, breach.markerX(), breach.markerY());
                drawTellGlow(
                        batch, ring, breach.markerX(), breach.markerY(), progress, breach.markerRadius(), realSeconds);
            }
        } else if (phase == BreachArena.Phase.TELL && bulge.size >= TELL_FRAMES) {
            AtlasRegion swell = bulge.get(tellFrame(breach.progress()));
            drawCentred(batch, swell, x, y);
            drawTellGlow(batch, swell, x, y, breach.progress(), 0, realSeconds);
        }
        drawVents(batch, piece, breach, phase, tick, back, true);
        int open = openStep(phase, elapsed);
        int closed = shutStep(phase, elapsed, riseSeconds, diveSeconds);
        AtlasRegion frame = open >= 0
                ? mouth.get(Math.min(mouth.size - 1, pose * OPEN_STEPS + open))
                : closed >= 0 ? shut.get(Math.min(shut.size - 1, pose * SHUT_STEPS + closed)) : null;
        if (frame != null) {
            int part = breach.mouth();
            if (hits.on(part, tick, piece.partTicksSinceHit(part))) {
                flash.draw(batch, frame, Math.round(X0 + x), Math.round(y), HIT_WHITE, HitFlash.PART_FLASH);
            } else {
                drawCentred(batch, frame, x, y);
            }
        }
        if (breach.glowing() && gullet.size > pose) {
            float pulse = (float) (0.65 + 0.35 * Math.sin(realSeconds * 2 * Math.PI * 6));
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(1, 1, 1, pulse);
            drawCentred(batch, gullet.get(pose), x, y);
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        if (biteCircle(phase) && pixel != null) {
            drawBiteCircle(batch, x, y, breach.biteRadius(), realSeconds);
        }
        drawVents(batch, piece, breach, phase, tick, back, false);
    }

    /**
     * Round 44 Q.10, step GFX4: the bite's reach through its tell, over the mouth (the risen body, about 95 px, covered
     * a ring on the sand): a dark edge outside for the sand, a soft glow either side and a thin bright ring, pulsing.
     */
    private void drawBiteCircle(SpriteBatch batch, double x, double y, double radius, double seconds) {
        float pulse = (float) (0.8 + 0.2 * Math.sin(seconds * 2 * Math.PI * 5));
        drawRing(batch, x, y, radius + 2 * RING_WIDTH, RING_EDGE, BITE_EDGE_ALPHA);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int k = 1; k <= BITE_GLOW_RINGS; k++) {
            float soft = BITE_GLOW_ALPHA * pulse * (BITE_GLOW_RINGS + 1 - k) / BITE_GLOW_RINGS;
            drawRing(batch, x, y, radius + k * RING_WIDTH, STREAK, soft);
            drawRing(batch, x, y, radius - k * RING_WIDTH, STREAK, soft * 0.6f);
        }
        drawRing(batch, x, y, radius, BITE_WHITE, BITE_ALPHA * pulse);
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Step GFX3 (the H5 capture: the marker read as a thin dark ring, easy to miss): a tell's cracks glowing lime over
     * its frame, brighter and pulsing faster as it nears its end ({@link #tellGlow}), widened by a pixel each way; for
     * the eruption's marker ({@code radius} > 0) its 90 px crack ring traced in lime with a dark edge outside it.
     */
    private void drawTellGlow(
            SpriteBatch batch, AtlasRegion frame, double x, double y, double progress, double radius, double seconds) {
        if (flash == null) {
            return;
        }
        float glow = tellGlow(progress, seconds);
        if (radius > 0) {
            drawRing(batch, x, y, radius + RING_WIDTH, RING_EDGE, RING_EDGE_ALPHA * Math.min(1, glow * 1.5f));
            batch.setColor(Color.WHITE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        flash.beginCracks(batch, LIME, glow);
        drawCentred(batch, frame, x, y);
        flash.beginCracks(batch, LIME, glow * CRACK_SPREAD);
        drawCentred(batch, frame, x - 1, y);
        drawCentred(batch, frame, x + 1, y);
        drawCentred(batch, frame, x, y - 1);
        drawCentred(batch, frame, x, y + 1);
        flash.end(batch);
        if (radius > 0) {
            drawRing(batch, x, y, radius, LIME, RING_ALPHA * glow);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** A circle {@value #RING_WIDTH} px wide of {@value #RING_SEGMENTS} short lengths round (x, y). */
    private void drawRing(SpriteBatch batch, double x, double y, double radius, Color colour, float opacity) {
        batch.setColor(colour.r, colour.g, colour.b, opacity);
        double step = 2 * Math.PI / RING_SEGMENTS;
        float length = (float) (2 * radius * Math.tan(step / 2) + 1);
        for (int k = 0; k < RING_SEGMENTS; k++) {
            double angle = k * step;
            double cx = x + Math.cos(angle) * radius;
            double cy = y + Math.sin(angle) * radius;
            batch.draw(
                    pixel,
                    (float) (X0 + cx - length / 2),
                    (float) (cy - RING_WIDTH / 2),
                    length / 2,
                    RING_WIDTH / 2,
                    length,
                    RING_WIDTH,
                    1,
                    1,
                    (float) Math.toDegrees(angle + Math.PI / 2));
        }
    }

    /**
     * A tell's crack glow (0–1) at {@code progress} (0–1) of it at real {@code seconds}: from {@link #GLOW_LEAST} to
     * full over the tell, pulsing from 3 to 8 times a second down to {@link #GLOW_DIP} of it.
     */
    static float tellGlow(double progress, double seconds) {
        double p = Math.clamp(progress, 0, 1);
        double level = GLOW_LEAST + (1 - GLOW_LEAST) * p;
        double rate = 3 + 5 * p;
        double pulse = 0.5 + 0.5 * Math.sin(seconds * 2 * Math.PI * rate);
        return (float) (level * (GLOW_DIP + (1 - GLOW_DIP) * pulse));
    }

    /** How long the breach has been in its phase, s (the open's length an eruption's at an erupting point). */
    private double elapsed(BreachArena breach, BreachArena.Phase phase, int pose, float alpha) {
        double length =
                switch (phase) {
                    case RISE -> riseSeconds;
                    case OPEN ->
                        pose == BreachArena.Pose.UNDER.ordinal() && breach.erupting() ? eruptOpenSeconds : openSeconds;
                    case DIVE -> diveSeconds;
                    default -> 0;
                };
        // Step GSR: between two steps too ({@code alpha}), so the spine's front glides.
        return Math.min(length, breach.progress() * length + (length > 0 ? alpha * SimStep.SECONDS : 0));
    }

    private void drawCraters(SpriteBatch batch, double scroll) {
        if (funnel.size <= FUNNEL_CRATER) {
            return;
        }
        AtlasRegion crater = funnel.get(FUNNEL_CRATER);
        double half = crater.getRegionHeight() / 2.0;
        for (int i = 0; i < craters; i++) {
            double y = craterRow[i] - scroll;
            if (y + half > 0 && y - half < PlayField.HEIGHT) {
                drawCentred(batch, crater, craterX[i], y);
            }
        }
    }

    /**
     * Step GSR: the body's opacity {@code elapsed} s into a dive {@code dive} s long: full while the back sinks into it
     * (the spine's front running back to the collar, {@link ColossusSpine#DIVE_SHARE} of the dive), then fading out by
     * the dive's end, as the buried ridge goes on from the next point.
     */
    static float diveBody(double elapsed, double dive) {
        double sunk = dive * ColossusSpine.DIVE_SHARE;
        return (float) Math.clamp(1 - (elapsed - sunk) / (dive - sunk), 0, 1);
    }

    /**
     * The body under the sand: before a rise (buried, the tell) along the next point's curve up to its head; behind a
     * risen mouth its last stretch up to the neck (the mouth is its head), the back rising out of it; in the dive that
     * stretch staying while the back sinks into it, then fading ({@link #diveBody}).
     */
    private void drawBody(
            SpriteBatch batch, BreachArena breach, BreachArena.Phase phase, double elapsed, double realSeconds) {
        points = 0;
        float alpha = 1;
        boolean head = true;
        switch (phase) {
            case BURIED, TELL -> {
                if (breach.nextFixed()) {
                    for (int i = 0; i < BreachArena.RIDGE_POINTS; i++) {
                        curveX[i] = breach.ridgeX(i);
                        curveY[i] = breach.ridgeY(i);
                    }
                    points = layOut(curveX, curveY, BreachArena.RIDGE_POINTS, breach.ridgeHead(), 0, xs, ys, headings);
                }
            }
            case RISE, OPEN, BITE, DIVE -> {
                if (held) {
                    double trim = neck[breach.pose().ordinal()];
                    points = layOut(heldX, heldY, BreachArena.RIDGE_POINTS, 1, trim, xs, ys, headings);
                    head = false;
                    if (phase == BreachArena.Phase.DIVE) {
                        alpha = diveBody(elapsed, diveSeconds);
                    }
                }
            }
        }
        body.draw(batch, xs, ys, headings, points, realSeconds, head, RidgeLooks.pulse(realSeconds), alpha);
    }

    /**
     * The spine vents. On a risen edge mouth's back ({@code back}, step GK) each sits on its segment (at the
     * simulation's place, where it is hit): shut while the segment rises or sinks, up, venting for {@link
     * #VENT_SECONDS} after a burst, a hit flashing it; a vent destroyed in this or an earlier breach leaves its scar on
     * its segment (otherwise the segment's sealed pore shows); step GSR: each shown up to the spine's front as its
     * segment is. Without the back (an under breach) the vents rise from the sand as before. {@code wrecks}
     * draws the wrecks (under the mouth), else the living vents (over it).
     */
    private void drawVents(
            SpriteBatch batch,
            SetPiece piece,
            BreachArena breach,
            BreachArena.Phase phase,
            long tick,
            boolean back,
            boolean wrecks) {
        if (vent.size < 4) {
            return;
        }
        boolean shown = phase == BreachArena.Phase.RISE
                || phase == BreachArena.Phase.OPEN
                || phase == BreachArena.Phase.BITE
                || phase == BreachArena.Phase.DIVE;
        if (!shown) {
            return;
        }
        for (int v = 0; v < breach.ventCount(); v++) {
            boolean onBack = back && v < ColossusSpine.SEGMENTS;
            // Step GSR: an organ shows as its segment does, up to the spine's front; shut until the segment is up.
            boolean out = !onBack || spine.out(v);
            boolean up = !onBack || spine.emergence(v) >= 1;
            if (wrecks) {
                boolean scar = onBack
                        ? ventDied[v] >= 0 && !breach.ventRaised(v) && out
                        : ventDied[v] == breach.breaches() && ventDied[v] > 0;
                if (scar && onBack) {
                    spine.drawOn(batch, vent.get(3), spine.x(v), spine.y(v), v);
                } else if (scar) {
                    drawCentred(batch, vent.get(3), breach.ventX(v), breach.ventY(v));
                }
                continue;
            }
            if (!breach.ventRaised(v) || !out) {
                continue;
            }
            double x = breach.ventX(v);
            double y = breach.ventY(v);
            int frame = !breach.ventUp(v) || !up ? 0 : tick - ventFired[v] < SimStep.ticks(VENT_SECONDS) ? 2 : 1;
            int part = breach.ventPart(v);
            if (frame > 0 && hits.on(part, tick, piece.partTicksSinceHit(part))) {
                flash.draw(batch, vent.get(frame), Math.round(X0 + x), Math.round(y), HIT_WHITE, HitFlash.PART_FLASH);
            } else if (onBack) {
                spine.drawOn(batch, vent.get(frame), x, y, v);
            } else {
                drawCentred(batch, vent.get(frame), x, y);
            }
        }
    }

    /**
     * Its death on the ground (step GK, the throe, {@link ColossusThroe}): the fissure ring opening round the mouth's
     * place; the furrow along the body's last track and the collapsed crater fading in and staying, scrolling with the
     * ground; the split ridge glowing at full until they have settled. The body itself and the geysers are drawn over
     * the ground units ({@link #drawStreaks}).
     */
    private void drawDeath(SpriteBatch batch, long tick, float alpha, double scroll, double realSeconds) {
        if (deathTick == NEVER) {
            return;
        }
        double age = deathAge(tick, alpha);
        throe.drawFissure(batch, age, deathX, deathRow - scroll);
        float settled = settled(age);
        if (settled > 0) {
            batch.setColor(1, 1, 1, settled);
            if (furrow.size >= RidgeLooks.HEADINGS) {
                for (int i = furrowPoints - 1; i >= 0; i--) {
                    double y = furrowRow[i] - scroll;
                    if (y > -64 && y < PlayField.HEIGHT + 64) {
                        drawCentred(batch, furrow.get(furrowHeading[i] % furrow.size), furrowX[i], y);
                    }
                }
            }
            if (sinkhole != null) {
                drawCentred(batch, sinkhole, deathX, deathRow - scroll);
            }
            batch.setColor(Color.WHITE);
        }
        if (age < deathSeconds && furrowPoints > 0) {
            // The body's last stretch through the throe, its crest cracks glowing at full (the art's death).
            for (int i = 0; i < furrowPoints; i++) {
                xs[i] = furrowX[i];
                ys[i] = furrowRow[i] - scroll;
                headings[i] = furrowHeading[i];
            }
            body.draw(batch, xs, ys, headings, furrowPoints, realSeconds, false, 1, 1 - settled);
        }
    }

    /**
     * Over the ground units after the kill: the throe's body rearing, collapsing and sinking, and its shockwave; then
     * the geysers rolling along the split ridge from its tail to its head, or round the mouth's place where the ridge
     * is too short to carry them (step GFX4: over the body, which hid them on the ground; H6's capture).
     */
    private void drawThroeBody(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (deathTick == NEVER || !drawsDeath(sortie)) {
            return;
        }
        double age = deathAge(sortie.tick(), alpha);
        throe.drawBody(batch, age, deathX, deathRow - scroll);
        if (age >= deathSeconds) {
            return;
        }
        if (ColossusThroe.alongRidge(furrowPoints, furrowReach)) {
            for (int i = 0; i < furrowPoints; i++) {
                xs[i] = furrowX[i];
                ys[i] = furrowRow[i] - scroll;
            }
            throe.drawGeysers(batch, age, xs, ys, furrowPoints);
        } else {
            throe.drawGeysersRound(batch, age, deathX, deathRow - scroll);
        }
    }

    /**
     * Step GK: the play field's sideways shake now from the death throe ({@link ColossusThroe#shakeX}), px at the full
     * screen-shake setting; 0 while it lives and after the throe.
     */
    float shakeX(Sortie sortie, double realSeconds) {
        return deathTick == NEVER ? 0 : ColossusThroe.shakeX(deathAge(sortie.tick(), 0), realSeconds);
    }

    /**
     * Over the ground units: while the mouth draws (open, the bite's tell) sand streaks run into it from the suction's
     * outer radius to its inner one along {@value #STREAKS} lanes, fading in at the outer and out at the inner end,
     * their opacity by the difficulty's suction. After the kill: the throe's body ({@link #drawThroeBody}).
     */
    void drawStreaks(SpriteBatch batch, Sortie sortie, float alpha, double scroll, double realSeconds) {
        drawThroeBody(batch, sortie, alpha, scroll);
        SetPiece piece = colossus(sortie);
        if (piece == null || !piece.present() || piece.destroyed() || streakShare <= 0) {
            return;
        }
        BreachArena breach = piece.breach().orElseThrow();
        BreachArena.Phase phase = breach.phase();
        if (phase != BreachArena.Phase.OPEN && phase != BreachArena.Phase.BITE) {
            return;
        }
        int pose = breach.pose().ordinal();
        float in = phase == BreachArena.Phase.OPEN
                ? (float) Math.clamp(elapsed(breach, phase, pose, alpha) / STREAK_IN_SECONDS, 0, 1)
                : 1;
        double cx = breach.mouthX();
        double cy = breach.mouthY();
        double inner = Math.max(suctionInner * 0.5, 40);
        double span = suctionOuter - inner;
        if (!(span > 0)) {
            return;
        }
        for (int k = 0; k < STREAKS; k++) {
            float share = Math.min(1, streakAlpha(k, realSeconds, span) * STREAK_ALPHA * streakShare * in);
            if (share <= 0) {
                continue;
            }
            double angle = streakAngle(k);
            double r = inner + streakDistance(k, realSeconds, span);
            double x = cx + Math.cos(angle) * r;
            double y = cy + Math.sin(angle) * r;
            if (x < 0 || x > PlayField.WIDTH || y < 0 || y > PlayField.HEIGHT) {
                continue;
            }
            float degrees = (float) Math.toDegrees(angle);
            batch.setColor(STREAK_SHADOW.r, STREAK_SHADOW.g, STREAK_SHADOW.b, share * STREAK_SHADOW_ALPHA);
            batch.draw(
                    pixel,
                    (float) (X0 + x),
                    (float) (y - 1 - STREAK_SHADOW_WIDTH / 2),
                    0,
                    STREAK_SHADOW_WIDTH / 2,
                    (float) STREAK_LENGTH,
                    STREAK_SHADOW_WIDTH,
                    1,
                    1,
                    degrees);
            batch.setColor(STREAK.r, STREAK.g, STREAK.b, share);
            batch.draw(
                    pixel,
                    (float) (X0 + x),
                    (float) (y - STREAK_WIDTH / 2),
                    0,
                    STREAK_WIDTH / 2,
                    (float) STREAK_LENGTH,
                    STREAK_WIDTH,
                    1,
                    1,
                    degrees);
        }
        batch.setColor(Color.WHITE);
    }

    /** Streak {@code k}'s lane: evenly round the mouth, nudged by a fixed amount each. */
    static double streakAngle(int k) {
        return 2 * Math.PI * (k + 0.37 * Math.sin(k * 12.9898)) / STREAKS;
    }

    /** Streak {@code k}'s distance from the inner radius at real {@code seconds}, running inward over {@code span}. */
    static double streakDistance(int k, double seconds, double span) {
        double travelled = seconds * STREAK_SPEED + span * ((k * 0.618034) % 1);
        return span - Math.floorMod((long) Math.floor(travelled), Math.max(1, Math.round(span)));
    }

    /** Streak {@code k}'s opacity share at real {@code seconds}: fading in at the outer end, out at the inner. */
    static float streakAlpha(int k, double seconds, double span) {
        double d = streakDistance(k, seconds, span) / span;
        return (float) Math.clamp(Math.min((1 - d) * 4, d * 3), 0, 1);
    }

    /**
     * With the debris: the rocks it tears loose, one of the four shapes (by the rock's serial) tumbling, scaled to the
     * rock's size; a hit flashes it white. After the kill: the throe's dust plume and rock rain ({@link
     * ColossusThroe#drawAir}).
     */
    void drawRocks(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (deathTick != NEVER && drawsDeath(sortie)) {
            throe.drawAir(batch, deathAge(sortie.tick(), alpha), deathX, deathRow - scroll);
        }
        if (rock.size < ROCK_SHAPES * ROCK_TUMBLE) {
            return;
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.debrisCount(); i++) {
            Debris chunk = sortie.debris(i);
            if (!chunk.pulled()) {
                continue;
            }
            AtlasRegion frame = rock.get(rockFrame(chunk.serial(), tick));
            double x = chunk.renderX(alpha);
            double y = chunk.renderY(alpha);
            if (chunk.ticksSinceHit() < HIT_FLASH_TICKS) {
                flash.draw(batch, frame, Math.round(X0 + x), Math.round(y), HIT_WHITE, 1);
            } else {
                drawScaled(batch, frame, x, y, (float) (chunk.spec().size().width() / ROCK_WIDTH));
            }
        }
    }

    /** A rock's frame: its shape by its serial, its tumble step by the step (each rock its own phase). */
    static int rockFrame(int serial, long tick) {
        int shape = Math.floorMod(serial, ROCK_SHAPES);
        int tumble = (int) Math.floorMod(tick / ROCK_TUMBLE_TICKS + serial, (long) ROCK_TUMBLE);
        return shape * ROCK_TUMBLE + tumble;
    }

    /** Whether set piece {@code piece} is a breaching boss, which these looks draw. */
    static boolean drawn(SetPiece piece) {
        return piece.breach().isPresent();
    }
}
