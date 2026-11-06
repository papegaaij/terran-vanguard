package vanguard.sim;

import java.util.List;

/**
 * M11 part C, step CD1 (design/world/vrell-space, Hazards &amp; set pieces: living terrain; design/campaign Level 45,
 * Pit swarms; m11c-gaps.md §1 #3, §4): the level's spawning pits ({@link VrellLevel.PitSpec}) and the waves that rise
 * out of them ({@link WaveSpec#pit()}). A pit wave is held out of the main plan ({@link EnemyEntries#spawnPit}); its pit
 * scrolls with the ground. {@code tellSeconds} before the wave's time the pit tells ({@link SimEvents.Type#PIT_TELL}:
 * its lips glow and part); at the wave's time it opens ({@link SimEvents.Type#PIT_OPEN}) unless the ship's centre is
 * within {@code holdWithin} px of the pit's centre: then it holds closed ({@link SimEvents.Type#PIT_HELD}, once) until
 * the ship is off, and the whole wave comes that late. Opening, the whole wave rises together out of the pit's mouth:
 * its plan moved so its first point lies on the pit's centre, each unit rising for {@code riseSeconds} (untouchable and
 * harmless: {@link ShotGeometry#onField}, {@link Enemy#bodyTouches}; it attacks nothing until risen; its look scales
 * from the ground's: {@link #rise}); the pit closes {@code closeSeconds} after it opened ({@link
 * SimEvents.Type#PIT_CLOSE}). A pit that scrolls off the bottom edge still held closes with its wave never come (its
 * units counted nowhere, as a pursuit's left-out motes). Fixed arrays: allocation-free. Absent ({@code null} in the
 * sortie) in a level without pits; a level with pits never reverses its scroll ({@link LevelScript}).
 */
public final class Pits implements Hashed {
    /** Where a pit is. */
    public enum State {
        /** Breathing, shut (no wave due, or its wave not near). */
        SHUT,
        /** Its tell runs: the lips glow and part. */
        TELL,
        /** Due to open, held closed while the ship is too near. */
        HELD,
        /** Open: its wave rose out of it. */
        OPEN,
        /** Closed again after its wave (or its wave lost). */
        CLOSED
    }

    private static final State[] STATES = State.values();

    private final VrellLevel.PitSpec spec;
    private final EnemyEntries entries;
    private final SimEvents events;
    private final int tellTicks;
    private final int riseTicks;
    private final int closeTicks;
    /** Per pit wave: its pit, its planned time (steps), its plan's first point. */
    private final int[] pitOf;

    private final int[] plannedTicks;
    private final double[] originX;
    private final double[] originY;
    /** Per pit wave: its {@link State} (ordinal) and the step it opened (-1 before). */
    private final int[] states;

    private final int[] openTicks;
    /** Per pit: the pit wave it serves now (its last to tell); -1 before its first. */
    private final int[] current;

    private double scroll;
    private double prevScroll;
    /** The level step of the last {@link #step}. */
    private int tick;

    Pits(VrellLevel.PitSpec spec, List<WaveSpec> waves, EnemyEntries entries, SimEvents events) {
        this.spec = spec;
        this.entries = entries;
        this.events = events;
        tellTicks = SimStep.ticks(spec.tellSeconds());
        riseTicks = Math.max(1, SimStep.ticks(spec.riseSeconds()));
        closeTicks = SimStep.ticks(spec.closeSeconds());
        int count = entries.pitWaves();
        pitOf = new int[count];
        plannedTicks = new int[count];
        originX = new double[count];
        originY = new double[count];
        states = new int[count];
        openTicks = new int[count];
        current = new int[spec.pits().size()];
        int p = 0;
        for (WaveSpec wave : waves) {
            if (wave.pit() < 0) {
                continue;
            }
            if (wave.pit() >= spec.pits().size()) {
                throw new IllegalArgumentException(
                        wave.enemy().slug() + ": a pit wave names one of the level's pits: " + wave.pit());
            }
            pitOf[p] = wave.pit();
            plannedTicks[p] = SimStep.ticks(wave.t());
            // Its plan's first point (a swarm's cloud lies round its route's): the point that lands on the pit.
            Spawn first = entries.pitPlan(p).first();
            if (first != null) {
                originX[p] = first.path().x(0, 0);
                originY[p] = first.path().y(0, 0);
            }
            p++;
        }
        reset();
    }

    /** A new attempt: every pit shut, no wave come. */
    void reset() {
        java.util.Arrays.fill(states, State.SHUT.ordinal());
        java.util.Arrays.fill(openTicks, -1);
        java.util.Arrays.fill(current, -1);
        scroll = prevScroll = 0;
        tick = 0;
    }

    /**
     * One step at {@code levelTick}, the ground scrolled {@code groundScroll} px, the ship at (shipX, shipY) ({@code
     * flying}: else it holds no pit): the tells, holds, openings (the waves let in) and closings.
     */
    void step(int levelTick, double groundScroll, double shipX, double shipY, boolean flying) {
        prevScroll = scroll;
        scroll = groundScroll;
        tick = levelTick;
        for (int p = 0; p < states.length; p++) {
            int pit = pitOf[p];
            double x = spec.pits().get(pit).x();
            double y = y(pit, groundScroll);
            State state = STATES[states[p]];
            switch (state) {
                case SHUT -> {
                    if (levelTick >= plannedTicks[p] - tellTicks) {
                        states[p] = State.TELL.ordinal();
                        current[pit] = p;
                        events.add(SimEvents.Type.PIT_TELL, x, y, pit);
                    }
                }
                case TELL, HELD -> {
                    if (levelTick < plannedTicks[p]) {
                        break;
                    }
                    if (y < 0) {
                        // Off the bottom edge still held: its wave never comes.
                        states[p] = State.CLOSED.ordinal();
                        entries.pitLost(p);
                        events.add(SimEvents.Type.PIT_CLOSE, x, y, pit);
                    } else if (flying && near(x, y, shipX, shipY)) {
                        if (state == State.TELL) {
                            states[p] = State.HELD.ordinal();
                            events.add(SimEvents.Type.PIT_HELD, x, y, pit);
                        }
                    } else {
                        states[p] = State.OPEN.ordinal();
                        openTicks[p] = levelTick;
                        current[pit] = p;
                        events.add(SimEvents.Type.PIT_OPEN, x, y, pit);
                        // The whole wave rises together out of the pit's mouth.
                        entries.spawnPit(p, Integer.MAX_VALUE, x - originX[p], y - originY[p], riseTicks);
                    }
                }
                case OPEN -> {
                    if (levelTick - openTicks[p] >= closeTicks) {
                        states[p] = State.CLOSED.ordinal();
                        events.add(SimEvents.Type.PIT_CLOSE, x, y, pit);
                    }
                }
                case CLOSED -> {
                    // Done.
                }
            }
        }
    }

    private boolean near(double x, double y, double shipX, double shipY) {
        double dx = shipX - x;
        double dy = shipY - y;
        return dx * dx + dy * dy < spec.holdWithin() * spec.holdWithin();
    }

    private double y(int pit, double groundScroll) {
        return PlayField.HEIGHT + spec.pits().get(pit).at() - groundScroll;
    }

    @Override
    public void addTo(StateHash hash) {
        for (int p = 0; p < states.length; p++) {
            hash.add(states[p]).add(openTicks[p]);
        }
    }

    /** The number of the level's pits (the scenery pits that never open are the backdrop's, not here). */
    public int count() {
        return current.length;
    }

    /** Pit {@code pit}'s centre, x px. */
    public double x(int pit) {
        return spec.pits().get(pit).x();
    }

    /** Pit {@code pit}'s centre between the previous and the current step, y px (y up; {@code alpha} in [0, 1]). */
    public double renderY(int pit, double alpha) {
        return y(pit, prevScroll + (scroll - prevScroll) * alpha);
    }

    /** Where pit {@code pit} is now: its current wave's state ({@link State#SHUT} before its first tell). */
    public State state(int pit) {
        return current[pit] < 0 ? State.SHUT : STATES[states[current[pit]]];
    }

    /**
     * How far pit {@code pit}'s tell has run, 0 to 1 (its lips' glow and parting), between the previous and the current
     * step ({@code alpha} in [0, 1]); 1 while it is held or open, 0 shut or closed.
     */
    public double tell(int pit, double alpha) {
        int p = current[pit];
        if (p < 0) {
            return 0;
        }
        return switch (STATES[states[p]]) {
            case TELL ->
                tellTicks <= 0 ? 1 : Math.clamp((tick - 1 + alpha - (plannedTicks[p] - tellTicks)) / tellTicks, 0, 1);
            case HELD, OPEN -> 1;
            case SHUT, CLOSED -> 0;
        };
    }

    /**
     * How far {@code unit} has risen out of its pit, 0 (the ground's scale) to 1 (the play plane), between the previous
     * and the current step ({@code alpha} in [0, 1]); 1 for a unit not rising.
     */
    public double rise(Enemy unit, double alpha) {
        if (unit.pitRise <= 0) {
            return 1;
        }
        return Math.clamp((riseTicks - unit.pitRise + alpha) / riseTicks, 0, 1);
    }

    /** Whether {@code unit} is rising out of a pit now (untouchable and harmless). */
    public boolean rising(Enemy unit) {
        return unit.pitRise > 0;
    }
}
