package vanguard.sim;

/**
 * M9 part H, step H1b (design/campaign Level 35, the mass-relay flash; {@link HazardSpec.Relay}): the turned mass relay
 * fires once. At its time on the level clock its line locks horizontally through the ship's height ({@link
 * SimEvents.Type#RELAY_TELEGRAPH}); after the telegraph it flashes along the line ({@link SimEvents.Type#RELAY_FIRED}),
 * live for its time: a band across the whole field that deals its damage to the ship once ({@link
 * SimEvents.Type#RELAY_HIT}, in the sortie's hit pass). Rook and every enemy on the line are untouched. A restart of the
 * level brings it back; the boss checkpoint (after it) leaves it spent. Made only for a level with a relay, so the
 * other levels hash as before. Allocates nothing.
 */
public final class RelayFlash implements Hashed {
    private final HazardSpec.Relay spec;
    private final int startTick;
    private final int telegraphTicks;
    private final int liveTicks;

    /** Steps into the telegraph and the flash; -1 before it started. */
    private int ticks;
    /** Whether it is over (it fires once). */
    private boolean spent;
    /** Whether the flash struck the ship. */
    private boolean hit;
    /** The locked line's height, px up. */
    private double lineY;

    RelayFlash(HazardSpec.Relay spec) {
        this.spec = spec;
        startTick = SimStep.ticks(spec.t());
        telegraphTicks = Math.max(1, SimStep.ticks(spec.telegraphSeconds()));
        liveTicks = Math.max(1, SimStep.ticks(spec.liveSeconds()));
        reset();
    }

    /** Back to the level start: not yet fired. */
    void reset() {
        ticks = -1;
        spent = false;
        hit = false;
        lineY = 0;
    }

    /** The boss checkpoint lies after it: spent, no line. */
    void finish() {
        ticks = -1;
        spent = true;
        hit = false;
    }

    /**
     * One step at {@code levelTick} with the ship at height {@code shipY}: from its time the line locks through the ship;
     * after the telegraph the flash is live, then it is spent.
     */
    void step(int levelTick, double shipY, SimEvents events) {
        if (spent) {
            return;
        }
        if (ticks < 0) {
            if (levelTick < startTick) {
                return;
            }
            ticks = 0;
            lineY = shipY;
            hit = false;
            events.add(SimEvents.Type.RELAY_TELEGRAPH, edgeX(), lineY, 0);
            return;
        }
        ticks++;
        if (ticks == telegraphTicks) {
            events.add(SimEvents.Type.RELAY_FIRED, edgeX(), lineY, 0);
        } else if (ticks >= telegraphTicks + liveTicks) {
            ticks = -1;
            spent = true;
        }
    }

    private double edgeX() {
        return spec.fromRight() ? PlayField.WIDTH : 0;
    }

    /** Whether the flash is live and has not struck yet, and {@code hull} at (x, y) touches its band. */
    boolean strikes(Hull hull, double x, double y) {
        return live() && !hit && hull.touchesSegment(x, y, 0, lineY, PlayField.WIDTH, lineY, spec.bandPx() / 2);
    }

    /** The flash struck the ship: once. */
    void markHit() {
        hit = true;
    }

    /** Its spec. */
    public HazardSpec.Relay spec() {
        return spec;
    }

    /** Whether the line is drawn and brightening now (the telegraph). */
    public boolean telegraphing() {
        return ticks >= 0 && ticks < telegraphTicks;
    }

    /** Whether the flash is live now. */
    public boolean live() {
        return ticks >= telegraphTicks;
    }

    /** Whether it has fired and is over. */
    public boolean spent() {
        return spent;
    }

    /** The telegraph's progress, 0 to 1 (the line brightening); 1 while live, 0 before and after. */
    public double charge() {
        if (ticks < 0) {
            return 0;
        }
        return live() ? 1 : (double) ticks / telegraphTicks;
    }

    /** The locked line's height, px up (valid from the telegraph's start). */
    public double lineY() {
        return lineY;
    }

    /** The edge the relay lies off, px from the left (the line's origin). */
    public double originX() {
        return edgeX();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(ticks).add(spent ? 1 : 0).add(hit ? 1 : 0).add(lineY);
    }
}
