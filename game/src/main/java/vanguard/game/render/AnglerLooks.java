package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.LurePhase;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part F: the Glow Angler (design/enemies/naval/glow-angler; tools/art/glow_angler.py, round 48 a "rust angler"), the
 * lure ambusher of Level 26's black water. Its frame is its heading's (16, {@code k} × 22.5° clockwise from straight
 * down, its facing: toward the ship in a lunge and its retreat) × 2 + its jaw: shut while it hangs ({@link
 * LurePhase#DRIFT}), the gape wide through its dive ({@link LurePhase#LUNGE}) and while its ring's muzzle flash plays.
 * The whole unit (stalk and bead included) is what the light map shows (the headlight cone, a light pool, the Sonar
 * Pulse; in the dark the faint rim pool, {@link DarkLight}). After the grade, ungraded and additive ({@link #drawGlows}):
 * its glow-only frame (the bead's teal and the crimson eyes), the stalk lit teal by its bead fading in while the bead is
 * within {@value #STALK_WITHIN} px of the ship (over the last {@value #STALK_FADE} px), the bead's pulse ({@code -lure},
 * {@value #LURE_FPS} fps, its own phase) at the pivot file's {@code lure} of its heading, flaring for {@value
 * #FLARE_SECONDS} s when it triggers ({@code LURE_LUNGE}), the ring's flash at its gape ({@code -muzzle}, {@value
 * #MUZZLE_FRAME_TICKS} steps a frame, from {@code LURE_RING}); its death: the lure blown out at the bead ({@code -death},
 * {@value #DEATH_FRAME_TICKS} steps a frame) and the stalk lit {@value #DEATH_STALK_SECONDS} s, going dark over its
 * last {@value #DEATH_STALK_FADE} s, over the under-water burst at the body ({@link DeepDeaths}). Presentation only;
 * nothing is allocated per frame or step.
 */
final class AnglerLooks {
    /** The unit's slug. */
    static final String SLUG = "glow-angler";
    /** The art's headings and jaws (0 shut, 1 the gape wide). */
    static final int HEADINGS = 16;

    static final int JAWS = 2;
    /** The art's frame, px (the body's centre on the frame's centre). */
    static final int FRAME = 80;
    /** The stalk shows while the bead is within this of the ship, fading in over the last this many px. */
    static final double STALK_WITHIN = 160;

    static final double STALK_FADE = 40;
    /** The bead's pulse rate and its flare at a trigger, s. */
    static final double LURE_FPS = 8;

    static final double FLARE_SECONDS = 0.3;
    /** The ring's flash: 4 frames, this many steps each. */
    static final int MUZZLE_FRAME_TICKS = 2;
    /** The death's lure blow-out at 12 fps; the stalk lit this long, fading over its last part, s. */
    static final int DEATH_FRAME_TICKS = 5;

    static final double DEATH_STALK_SECONDS = 1;
    static final double DEATH_STALK_FADE = 0.4;
    /** The Anglers followed at once, and the deaths and flashes playing at once. */
    static final int SLOTS = 48;

    static final int DEATHS = 16;
    /** An event lies at the unit within this, px. */
    private static final double REACH = 12;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> stalk;
    private final Array<AtlasRegion> lure;
    private final Array<AtlasRegion> muzzle;
    private final Array<AtlasRegion> death;
    private final boolean[] angler;
    /** Per heading the bead, the gape and the eyes from the frame's centre (x right, y up), px. */
    private final float[][] lureAt = new float[HEADINGS][2];

    private final float[][] mouthAt = new float[HEADINGS][2];
    private final float[][] eyesAt = new float[HEADINGS][2];
    /** Per followed Angler its serial (-1 free), its last place and heading, its last trigger and ring steps. */
    private final int[] serials = new int[SLOTS];

    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final int[] lastHeading = new int[SLOTS];
    private final long[] lunged = new long[SLOTS];
    private final long[] rang = new long[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    /** Per death playing its bead's place, its heading, its body's place and its step (Long.MIN_VALUE: free). */
    private final double[] deathX = new double[DEATHS];

    private final double[] deathY = new double[DEATHS];
    private final int[] deathHeading = new int[DEATHS];
    private final long[] deathTick = new long[DEATHS];

    AnglerLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = optional(sprites, SLUG);
        glow = optional(sprites, SLUG + "-glow");
        stalk = optional(sprites, SLUG + "-stalk");
        lure = optional(sprites, SLUG + "-lure");
        muzzle = optional(sprites, SLUG + "-muzzle");
        death = optional(sprites, SLUG + "-death");
        angler = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            angler[k] = kinds.get(k).slug().equals(SLUG);
        }
        JsonValue pivot = pivots.apply(SLUG);
        read(pivot, "lure", lureAt, 0, 34);
        read(pivot, "mouth", mouthAt, 0, 20);
        read(pivot, "eyes", eyesAt, 0, 8);
        restart();
    }

    /** Per heading the pivot file's point {@code name} from the frame's centre (y up); without it straight down. */
    private static void read(JsonValue pivot, String name, float[][] out, float dx, float down) {
        JsonValue points = pivot == null ? null : pivot.get(name);
        double half = FRAME / 2.0;
        for (int k = 0; k < HEADINGS; k++) {
            if (points != null && k < points.size) {
                out[k][0] = (float) (points.get(k).getDouble(0) - half);
                out[k][1] = (float) (half - points.get(k).getDouble(1));
            } else {
                double angle = k * 2 * Math.PI / HEADINGS;
                // Heading k turns k × 22.5° clockwise from straight down (y up: down is -y).
                out[k][0] = (float) (-Math.sin(angle) * down + dx);
                out[k][1] = (float) (-Math.cos(angle) * down);
            }
        }
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** A new attempt: nothing followed, no death or flash playing. */
    void restart() {
        Arrays.fill(serials, -1);
        Arrays.fill(lunged, Long.MIN_VALUE);
        Arrays.fill(rang, Long.MIN_VALUE);
        Arrays.fill(deathTick, Long.MIN_VALUE);
    }

    /** Whether kind {@code kind} is the Glow Angler. */
    boolean angler(int kind) {
        return kind >= 0 && kind < angler.length && angler[kind];
    }

    /** Whether {@code enemy} is a Glow Angler with its art. */
    boolean draws(Enemy enemy) {
        return frames.size >= HEADINGS * JAWS && angler(enemy.kind());
    }

    /** The heading drawn for a facing (the simulation's, radians, 0 straight down, counter-clockwise on the screen). */
    static int heading(double facing) {
        return EnemyLooks.heading(facing, HEADINGS);
    }

    /** The frame for {@code heading} with the gape wide ({@code open}) or shut. */
    static int frameIndex(int heading, boolean open) {
        return heading * JAWS + (open ? 1 : 0);
    }

    /** Whether the gape is wide: through the dive, and while the ring's flash plays ({@code sinceRing} steps). */
    static boolean open(LurePhase phase, long sinceRing) {
        return phase == LurePhase.LUNGE || (sinceRing >= 0 && sinceRing < 4L * MUZZLE_FRAME_TICKS);
    }

    /** The stalk's opacity with the bead {@code distance} px from the ship: 0 beyond 160, full within 120. */
    static float stalkOpacity(double distance) {
        return (float) Math.clamp((STALK_WITHIN - distance) / STALK_FADE, 0, 1);
    }

    /** The dead Angler's stalk's opacity {@code seconds} after its death: lit, then dark over its last part. */
    static float deathStalkOpacity(double seconds) {
        if (seconds < 0 || seconds >= DEATH_STALK_SECONDS) {
            return 0;
        }
        return (float) Math.min(1, (DEATH_STALK_SECONDS - seconds) / DEATH_STALK_FADE);
    }

    /** The bead's pulse frame at step {@code tick} for the unit {@code serial} (its own phase) of {@code count}. */
    static int lureFrame(long tick, int serial, int count) {
        return (int) Math.floorMod((long) Math.floor(tick * LURE_FPS / SimStep.PER_SECOND) + serial * 3L, (long) count);
    }

    /** The bead's centre from the unit's centre at {@code heading}, x px. */
    float lureDx(int heading) {
        return lureAt[heading][0];
    }

    /** The bead's centre from the unit's centre at {@code heading}, y px (up). */
    float lureDy(int heading) {
        return lureAt[heading][1];
    }

    /** The eyes' midpoint from the unit's centre at {@code heading}, x px. */
    float eyesDx(int heading) {
        return eyesAt[heading][0];
    }

    /** The eyes' midpoint from the unit's centre at {@code heading}, y px (up). */
    float eyesDy(int heading) {
        return eyesAt[heading][1];
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** Its frame index now. */
    int frameIndex(Sortie sortie, Enemy enemy) {
        int s = slot(enemy.serial());
        long sinceRing = s < 0 || rang[s] == Long.MIN_VALUE ? -1 : sortie.tick() - rang[s];
        return frameIndex(heading(enemy.facing()), open(enemy.lurePhase(), sinceRing));
    }

    /** Its frame now; null for another unit. */
    AtlasRegion frame(Sortie sortie, Enemy enemy) {
        return draws(enemy) ? frames.get(frameIndex(sortie, enemy)) : null;
    }

    /** An Angler, flashing after a hit. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        flasher.draw(batch, enemy, frames.get(frameIndex(sortie, enemy)), enemy.renderX(alpha), enemy.renderY(alpha));
    }

    /**
     * After each step: its triggers and rings (each the followed Angler at the event), its deaths (the bead's place at
     * its last heading), then every Angler on the field followed at its place and heading.
     */
    void stepped(Sortie sortie) {
        if (frames.isEmpty()) {
            return;
        }
        SimEvents events = sortie.events();
        long tick = sortie.tick();
        for (int e = 0; e < events.size(); e++) {
            SimEvents.Type type = events.type(e);
            if (type != SimEvents.Type.LURE_LUNGE
                    && type != SimEvents.Type.LURE_RING
                    && type != SimEvents.Type.ENEMY_DESTROYED) {
                continue;
            }
            if (!angler(events.value(e))) {
                continue;
            }
            int s = nearest(events.x(e), events.y(e), type == SimEvents.Type.ENEMY_DESTROYED ? 2 * REACH : REACH);
            if (type == SimEvents.Type.LURE_LUNGE && s >= 0) {
                lunged[s] = tick;
            } else if (type == SimEvents.Type.LURE_RING && s >= 0) {
                rang[s] = tick;
            } else if (type == SimEvents.Type.ENEMY_DESTROYED) {
                died(events.x(e), events.y(e), s < 0 ? 0 : lastHeading[s], tick);
            }
        }
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!angler(enemy.kind())) {
                continue;
            }
            int s = slot(enemy.serial());
            if (s < 0) {
                s = slot(-1);
                if (s < 0) {
                    continue;
                }
                serials[s] = enemy.serial();
                lunged[s] = Long.MIN_VALUE;
                rang[s] = Long.MIN_VALUE;
            }
            seen[s] = true;
            lastX[s] = enemy.renderX(1);
            lastY[s] = enemy.renderY(1);
            lastHeading[s] = heading(enemy.facing());
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
    }

    /** The followed Angler last seen nearest (x, y) within {@code reach}, or -1. */
    private int nearest(double x, double y, double reach) {
        int best = -1;
        double bestDistance = reach * reach;
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] < 0) {
                continue;
            }
            double dx = lastX[s] - x;
            double dy = lastY[s] - y;
            double d = dx * dx + dy * dy;
            if (d <= bestDistance) {
                best = s;
                bestDistance = d;
            }
        }
        return best;
    }

    private void died(double x, double y, int heading, long tick) {
        // A free slot, else the oldest death's.
        int free = 0;
        for (int d = 0; d < DEATHS; d++) {
            if (deathTick[d] == Long.MIN_VALUE) {
                free = d;
                break;
            }
            if (deathTick[d] < deathTick[free]) {
                free = d;
            }
        }
        deathX[free] = x;
        deathY[free] = y;
        deathHeading[free] = heading;
        deathTick[free] = tick;
    }

    /**
     * After the grade, additive: each Angler's glow-only frame, its stalk by the ship's distance to its bead, its bead's
     * pulse (flaring at a trigger), its ring's flash; the deaths' blow-outs and lit stalks.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (frames.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long tick = sortie.tick();
        boolean flying = sortie.flying();
        double shipX = flying ? sortie.ship().renderX(alpha) : 0;
        double shipY = flying ? sortie.ship().renderY(alpha) : 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            int frame = frameIndex(sortie, enemy);
            int heading = frame / JAWS;
            if (frame < glow.size) {
                drawCentred(batch, glow.get(frame), x, y);
            }
            double beadX = x + lureAt[heading][0];
            double beadY = y + lureAt[heading][1];
            float shown = flying ? stalkOpacity(Math.hypot(beadX - shipX, beadY - shipY)) : 0;
            if (shown > 0 && heading < stalk.size) {
                batch.setColor(1, 1, 1, shown);
                drawCentred(batch, stalk.get(heading), x, y);
                batch.setColor(Color.WHITE);
            }
            int s = slot(enemy.serial());
            if (!lure.isEmpty()) {
                drawCentred(batch, lure.get(lureFrame(tick, enemy.serial(), lure.size)), beadX, beadY);
                double flare = s < 0 || lunged[s] == Long.MIN_VALUE ? -1 : (tick - lunged[s]) * SimStep.SECONDS;
                if (flare >= 0 && flare < FLARE_SECONDS) {
                    batch.setColor(1, 1, 1, (float) (1 - flare / FLARE_SECONDS));
                    drawCentred(batch, lure.get(lureFrame(tick, enemy.serial(), lure.size)), beadX, beadY);
                    batch.setColor(Color.WHITE);
                }
            }
            long sinceRing = s < 0 || rang[s] == Long.MIN_VALUE ? -1 : tick - rang[s];
            if (sinceRing >= 0 && sinceRing / MUZZLE_FRAME_TICKS < muzzle.size) {
                drawCentred(
                        batch,
                        muzzle.get((int) (sinceRing / MUZZLE_FRAME_TICKS)),
                        x + mouthAt[heading][0],
                        y + mouthAt[heading][1]);
            }
        }
        drawDeaths(batch, tick);
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawDeaths(SpriteBatch batch, long tick) {
        for (int d = 0; d < DEATHS; d++) {
            if (deathTick[d] == Long.MIN_VALUE) {
                continue;
            }
            long since = tick - deathTick[d];
            int heading = deathHeading[d];
            float lit = deathStalkOpacity(since * SimStep.SECONDS);
            int frame = (int) (since / DEATH_FRAME_TICKS);
            if (since < 0 || (lit <= 0 && frame >= death.size)) {
                // Played out (or from before a restart): the slot is free again.
                deathTick[d] = Long.MIN_VALUE;
                continue;
            }
            if (lit > 0 && heading < stalk.size) {
                batch.setColor(1, 1, 1, lit);
                drawCentred(batch, stalk.get(heading), deathX[d], deathY[d]);
                batch.setColor(Color.WHITE);
            }
            if (frame < death.size) {
                drawCentred(batch, death.get(frame), deathX[d] + lureAt[heading][0], deathY[d] + lureAt[heading][1]);
            }
        }
    }

    /** The heading last drawn for an Angler (for the light map's pools: its bead and eyes). */
    int heading(Enemy enemy) {
        return heading(enemy.facing());
    }
}
