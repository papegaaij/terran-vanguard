package vanguard.sim;

import java.util.List;

/**
 * M10 part G, step G1a (design/campaign Level 41, the secondary {@code FILES}; m10g-gaps.md §1 #6, §4): the level's
 * archive cores and Varga's uploads, a pass of its own ({@link Sortie#uplinks()}, null in every other level). Each core
 * ({@link UplinkSpec}) waits under its hatch, a plain destructible ground object; the hatch's destruction (by anything:
 * a shot, the Smart Bomb, a splash, a roll) opens it ({@link SimEvents.Type#UPLINK_OPEN}) where the hatch was, and it
 * scrolls on with the ground. While the ship flies (not in its launch, wrecked, failed or past the end) its centre
 * within the core's radius counts a simulation step towards the upload (the level clock may stand in a halt: the steps
 * are real ones), paused outside, never reset; entering starts or resumes it ({@link SimEvents.Type#UPLINK_PROGRESS},
 * the presentation reads {@link #share}). The last step uploads the file ({@link SimEvents.Type#FILE_IN}, value: the
 * files in so far, and the radio's {@link CueTrigger#FILE_IN} with that count as its subject). A core whose centre
 * leaves the bottom edge before its file is in, or whose hatch leaves it unbroken, is lost ({@link
 * SimEvents.Type#FILE_LOST}). A retry starts every core over; the boss checkpoint keeps what was decided. Fixed arrays,
 * nothing allocated in a step.
 */
public final class Uplinks implements Hashed {
    /** A core's state: under its hatch, open (live), its file in, lost. */
    public static final int SHUT = 0;

    public static final int OPEN = 1;
    public static final int IN = 2;
    public static final int LOST = 3;

    /** {@link CueTrigger#FILE_IN}'s subjects by the files in so far ("1", "2", ...), made once. */
    private final String[] subjects;

    private final SimEvents events;
    private final Radio radio;
    private final int[] hatch;
    private final int[] needTicks;
    private final double[] radius2;
    private final int[] state;
    private final double[] x;
    private final double[] y;
    private final double[] prevY;
    private final int[] ticks;
    private final boolean[] inside;
    private int filesIn;

    /** @param events null (and {@code radio}) for a checkpoint's copy, which only keeps the state */
    Uplinks(List<UplinkSpec> specs, SimEvents events, Radio radio) {
        this.events = events;
        this.radio = radio;
        int n = specs.size();
        hatch = new int[n];
        needTicks = new int[n];
        radius2 = new double[n];
        state = new int[n];
        x = new double[n];
        y = new double[n];
        prevY = new double[n];
        ticks = new int[n];
        inside = new boolean[n];
        subjects = new String[n + 1];
        for (int i = 0; i < n; i++) {
            UplinkSpec spec = specs.get(i);
            hatch[i] = spec.hatch();
            needTicks[i] = Math.max(1, SimStep.ticks(spec.seconds()));
            radius2[i] = spec.radius() * spec.radius();
        }
        for (int f = 0; f <= n; f++) {
            subjects[f] = Integer.toString(f);
        }
    }

    /** Back to the level start: every core under its hatch, no file in. */
    void reset() {
        java.util.Arrays.fill(state, SHUT);
        java.util.Arrays.fill(ticks, 0);
        java.util.Arrays.fill(inside, false);
        filesIn = 0;
    }

    /** Takes over {@code other}'s state (a boss checkpoint's record); both are of the same level. */
    void copyFrom(Uplinks other) {
        System.arraycopy(other.state, 0, state, 0, state.length);
        System.arraycopy(other.ticks, 0, ticks, 0, ticks.length);
        System.arraycopy(other.x, 0, x, 0, x.length);
        System.arraycopy(other.y, 0, y, 0, y.length);
        System.arraycopy(other.prevY, 0, prevY, 0, prevY.length);
        java.util.Arrays.fill(inside, false);
        filesIn = other.filesIn;
    }

    /**
     * Back to a boss checkpoint's {@code record} on an empty field (the ground objects let in before it are gone): a
     * file in stays in; a core open then, or whose hatch had entered by {@code nextGroundObject}, is lost (no event);
     * a core still to come waits under its hatch.
     */
    void restore(Uplinks record, int nextGroundObject) {
        copyFrom(record);
        for (int i = 0; i < state.length; i++) {
            if (state[i] == OPEN || (state[i] == SHUT && hatch[i] < nextGroundObject)) {
                state[i] = LOST;
            }
        }
    }

    /**
     * Ground object {@code object} (its index among the level's) was destroyed at (x, y): a core's hatch opens it there.
     */
    void hatchBroken(int object, double atX, double atY) {
        for (int i = 0; i < hatch.length; i++) {
            if (hatch[i] == object && state[i] == SHUT) {
                state[i] = OPEN;
                x[i] = atX;
                y[i] = prevY[i] = atY;
                events.add(SimEvents.Type.UPLINK_OPEN, atX, atY, i);
            }
        }
    }

    /** Ground object {@code object} left the screen unbroken at (x, y): a core under it is lost. */
    void hatchGone(int object, double atX, double atY) {
        for (int i = 0; i < hatch.length; i++) {
            if (hatch[i] == object && state[i] == SHUT) {
                state[i] = LOST;
                events.add(SimEvents.Type.FILE_LOST, atX, atY, i);
            }
        }
    }

    /**
     * The step's uploads, after the ground scrolled {@code scrollStep} px: the open cores scroll with it and one whose
     * centre left the bottom edge is lost; then the ship at ({@code shipX}, {@code shipY}) counts towards a core it is
     * within while {@code uploading} (the ship flies).
     */
    void step(double scrollStep, boolean uploading, double shipX, double shipY) {
        for (int i = 0; i < state.length; i++) {
            if (state[i] != OPEN) {
                continue;
            }
            prevY[i] = y[i];
            y[i] -= scrollStep;
            if (y[i] < 0) {
                state[i] = LOST;
                inside[i] = false;
                events.add(SimEvents.Type.FILE_LOST, x[i], y[i], i);
                continue;
            }
            double dx = shipX - x[i];
            double dy = shipY - y[i];
            boolean within = uploading && dx * dx + dy * dy <= radius2[i];
            if (within && !inside[i]) {
                events.add(SimEvents.Type.UPLINK_PROGRESS, x[i], y[i], i);
            }
            inside[i] = within;
            if (within && ++ticks[i] >= needTicks[i]) {
                state[i] = IN;
                inside[i] = false;
                filesIn++;
                events.add(SimEvents.Type.FILE_IN, x[i], y[i], filesIn);
                radio.cue(CueTrigger.FILE_IN, subjects[filesIn]);
            }
        }
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(filesIn);
        for (int i = 0; i < state.length; i++) {
            hash.add(state[i]).add(ticks[i]).add(y[i]).add(inside[i] ? 1 : 0);
        }
    }

    /** The level's archive cores. */
    public int count() {
        return state.length;
    }

    /** Core {@code i}'s state: {@link #SHUT}, {@link #OPEN}, {@link #IN} or {@link #LOST}. */
    public int state(int i) {
        return state[i];
    }

    /** The files uploaded in this attempt (the tracker's {@code FILES n / 3}). */
    public int filesIn() {
        return filesIn;
    }

    /** The cores lost in this attempt. */
    public int lost() {
        int lost = 0;
        for (int s : state) {
            lost += s == LOST ? 1 : 0;
        }
        return lost;
    }

    /** Core {@code i}'s upload so far, 0 to 1 (the teal ring filling); 1 once its file is in. */
    public double share(int i) {
        return state[i] == IN ? 1 : (double) ticks[i] / needTicks[i];
    }

    /** Whether the ship is uploading core {@code i}'s file now (within its radius, the core open). */
    public boolean uploading(int i) {
        return state[i] == OPEN && inside[i];
    }

    /** An open core's centre across, px (where its hatch broke). */
    public double x(int i) {
        return x[i];
    }

    /** Core {@code i}'s centre up the screen between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(int i, double alpha) {
        return prevY[i] + (y[i] - prevY[i]) * alpha;
    }

    /** An open core's centre up the screen now, px. */
    public double y(int i) {
        return y[i];
    }

    /** The hatch of core {@code i}: its index among the level's ground objects. */
    public int hatch(int i) {
        return hatch[i];
    }
}
