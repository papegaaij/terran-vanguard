package vanguard.sim;

/**
 * M11 part B, step B1 (design/world/vrell-space, Hazards & set pieces: gate transit; Q11 a; design/campaign Level 43,
 * Layout; design/player/wingmen, Rook in Act 7): the gate transit at a level's end, owned by a {@link Sortie} only in a
 * level with a {@link VrellLevel.Transit} ({@link Sortie#transit()}).
 *
 * <p>From its form time on the level clock ({@link SimEvents.Type#TRANSIT_FORM}) the ship's commands are set aside (no
 * input: no flight, no fire, no special, no dash): it eases over {@link #FORM_SECONDS} from where it was to the
 * formation point ({@link #FORM_X}, {@link #FORM_Y}) and holds it; Rook, while he flies, eases the same way from where
 * he was to his wing slot beside it (no AI, no gun). Nothing hits the ship or the escort (the bullets in flight pass
 * through). At the warp's time {@link SimEvents.Type#TRANSIT_WARP} starts the renderer's warp ({@link #warp()}); the
 * level's {@code reach-end} ends it inside the warp. Allocates nothing.
 *
 * <p>M11 part H, step H1b (design/campaign Level 50, the closing gate; m11h-gaps.md §1 #11): the formation point and the
 * ease are the spec's ({@link VrellLevel.Transit#withTo}); a reversed transit's warp raises {@link
 * SimEvents.Type#WARP_HOME} (the warp home) instead; a transit timed from the kill ({@link
 * VrellLevel.Transit#fromKill()}) runs its form, its warp and its beats ({@link VrellLevel.Transit.Beat}: the gate
 * closing, the near side, the plane out, the glyphs going dark) on the steps since the level boss's K ({@link #killed}),
 * so they come at their times from K on every run while the hold after the boss halts the level clock.
 */
public final class GateTransit {
    /** The ease from where the ship (and Rook) were to the formation, s (default, pending the user's review). */
    static final double FORM_SECONDS = 2.0;
    /** The formation point: the field's centre column (toward the ring's centre ahead). */
    static final double FORM_X = PlayField.WIDTH / 2.0;
    /** The formation point's height: the launch's start height (the launch reversed). */
    static final double FORM_Y = Ship.START_Y;

    private final int formTicks;
    private final int warpTicks;
    private final int warpLengthTicks;
    private final int easeTicks;
    /** M11 part H, step H1b: the formation point (y up) and whether the warp is the warp home. */
    private final double toX;

    private final double toY;
    private final boolean reverse;
    /** M11 part H, step H1b: whether its clock is the steps since the boss's K ({@link #sinceKill}). */
    private final boolean fromKill;
    /** M11 part H, step H1b: the scene's beats and their steps on its clock. */
    private final java.util.List<VrellLevel.Transit.Beat> beats;

    private final int[] beatTicks;
    /** M11 part H, step H1b: steps since the boss's K (a transit timed from the kill); -1 before it. */
    private int sinceKill = -1;
    /** Whether the formation started in this attempt. */
    private boolean formed;
    /** The level tick of the last step. */
    private int tick;
    /** Where the ship and Rook were when the formation started (NaN for Rook out of the fight). */
    private double shipFromX;

    private double shipFromY;
    private double rookFromX = Double.NaN;
    private double rookFromY = Double.NaN;

    GateTransit(VrellLevel.Transit spec) {
        formTicks = SimStep.ticks(spec.form());
        warpTicks = SimStep.ticks(spec.warp());
        warpLengthTicks = Math.max(1, SimStep.ticks(spec.length()));
        easeTicks = Math.max(1, SimStep.ticks(spec.easeSeconds()));
        toX = spec.toX();
        toY = spec.toY();
        reverse = spec.reverse();
        fromKill = spec.fromKill();
        beats = spec.beats();
        beatTicks = new int[beats.size()];
        for (int i = 0; i < beatTicks.length; i++) {
            beatTicks[i] = Math.max(1, SimStep.ticks(beats.get(i).seconds()));
        }
    }

    /** Back to the level start: not formed. */
    void reset() {
        formed = false;
        tick = 0;
        rookFromX = rookFromY = Double.NaN;
        sinceKill = -1;
    }

    /**
     * M11 part H, step H1b: the level boss died or spoke its last words (K) in this step: a transit timed from the kill
     * starts its clock (once an attempt).
     */
    void killed() {
        if (fromKill && sinceKill < 0) {
            sinceKill = 0;
        }
    }

    /** Back to a boss checkpoint at {@code levelTick}: one before the formation starts it afresh. */
    void restore(int levelTick) {
        if (fromKill) {
            // M11 part H, step H1b: every boss checkpoint lies before K: the scene starts afresh.
            tick = 0;
            sinceKill = -1;
            formed = false;
            rookFromX = rookFromY = Double.NaN;
            return;
        }
        tick = levelTick;
        if (levelTick < formTicks) {
            formed = false;
            rookFromX = rookFromY = Double.NaN;
        }
    }

    /**
     * At the step's start, the level clock at {@code levelTick}: the formation starts at its time (the ship flying;
     * where it and Rook are is remembered), the warp at its time.
     */
    void step(int levelTick, boolean flying, Ship ship, Wingman wingman, SimEvents events) {
        if (fromKill) {
            // M11 part H, step H1b: the clock is the steps since K; nothing before it.
            if (sinceKill < 0) {
                return;
            }
            sinceKill++;
            tick = sinceKill;
            for (int i = 0; i < beatTicks.length; i++) {
                if (tick == beatTicks[i]) {
                    VrellLevel.Transit.Beat beat = beats.get(i);
                    events.add(beat.kind().type(), PlayField.WIDTH / 2.0, PlayField.HEIGHT / 2.0, beat.value());
                }
            }
        } else {
            tick = levelTick;
        }
        int now = tick;
        if (!formed && flying && now >= formTicks) {
            formed = true;
            shipFromX = ship.x();
            shipFromY = ship.y();
            boolean rook = wingman != null && !wingman.out();
            rookFromX = rook ? wingman.x() : Double.NaN;
            rookFromY = rook ? wingman.y() : Double.NaN;
            events.add(SimEvents.Type.TRANSIT_FORM, ship.x(), ship.y());
        }
        if (formed && now == warpTicks) {
            // M11 part H, step H1b: a reversed transit's warp is the warp home.
            events.add(reverse ? SimEvents.Type.WARP_HOME : SimEvents.Type.TRANSIT_WARP, toX, toY);
        }
    }

    /** Whether the formation holds: the ship's commands set aside, nothing hits it or the escort. */
    public boolean active() {
        return formed;
    }

    /** The eased share of the way to the formation now, 0–1 (smoothstep). */
    private double share() {
        double t = Math.min(1, (double) (tick - formTicks) / easeTicks);
        return t * t * (3 - 2 * t);
    }

    /** One step of the ship toward the formation point. */
    void flyShip(Ship ship) {
        double share = share();
        ship.formTo(shipFromX + (toX - shipFromX) * share, shipFromY + (toY - shipFromY) * share);
    }

    /** One step of Rook toward his wing slot beside the ship; false when he was out at the formation (his own step). */
    boolean flyRook(Wingman wingman, Ship ship) {
        if (Double.isNaN(rookFromX)) {
            return false;
        }
        wingman.transit(rookFromX, rookFromY, ship.x(), ship.y(), share());
        return true;
    }

    /** The warp's share now, 0 before it to 1 at its end (the renderer's post-effect). */
    public double warp() {
        if (!formed || tick < warpTicks) {
            return 0;
        }
        return Math.min(1, (double) (tick - warpTicks) / warpLengthTicks);
    }

    /** The formation's share now, 0 at its start to 1 in formation (eased); 0 before it. */
    public double formation() {
        return formed ? share() : 0;
    }

    /** M11 part H, step H1b: whether its warp is the warp home (the renderer's warp reversed). */
    public boolean reverse() {
        return reverse;
    }

    /** M11 part H, step H1b: seconds since the level boss's K on a transit timed from the kill; -1 before (or none). */
    public double sinceKillSeconds() {
        return sinceKill < 0 ? -1 : sinceKill * SimStep.SECONDS;
    }

    void addTo(StateHash hash) {
        hash.add(formed ? 1 : 0);
        if (formed) {
            hash.add(shipFromX).add(shipFromY).add(rookFromX).add(rookFromY);
        }
        if (fromKill) {
            // M11 part H, step H1b: only a transit timed from the kill (Level 50's).
            hash.add(sinceKill);
        }
    }
}
