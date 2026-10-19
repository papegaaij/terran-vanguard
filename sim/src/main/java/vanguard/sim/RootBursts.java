package vanguard.sim;

import java.util.List;

/**
 * M5 part H, the root bursts of an {@link EnemyForce} (design/campaign Level 14, Hazards; decision H2 = a):
 * each burst's telegraph, moved away from the ship, and its growths, which stand at the burst's point as
 * they enter with the ground units.
 */
final class RootBursts {
    private final List<LevelScript.GroundUnit> groundUnits;
    private final SimEvents events;
    /**
     * M5 part H, the root bursts (design/campaign Level 14, Hazards; decision H2 = a): per ground unit
     * its slot in the burst arrays, -1 for a unit no burst grows; per slot its ground unit, the level
     * step its telegraph starts, whether it started in this attempt, the burst's move away from the ship
     * (px, the same for every growth of a burst), its point's height in ground space (the screen height
     * plus the ground's scroll at the telegraph), the half width its growths need from the field's edges,
     * and whether it is its burst's first growth (which raises the burst's events).
     */
    private final int[] burstSlot;

    private final int[] burstUnit;
    private final int[] burstTelegraphTicks;
    private final boolean[] burstTelegraphed;
    private final double[] burstShift;
    private final double[] burstBase;
    private final double[] burstFoot;
    private final boolean[] burstFirst;
    /** M5 part H: the ship's x, the ground's scroll and its last step, as {@link #bursts} last saw them. */
    private double burstShipX = PlayField.WIDTH / 2.0;

    private double burstScroll;
    private double burstScrollStep;

    RootBursts(List<LevelScript.GroundUnit> groundUnits, SimEvents events) {
        this.groundUnits = groundUnits;
        this.events = events;
        burstSlot = new int[groundUnits.size()];
        java.util.Arrays.fill(burstSlot, -1);
        int bursting = (int)
                groundUnits.stream().filter(unit -> unit.burst().isPresent()).count();
        burstUnit = new int[bursting];
        burstTelegraphTicks = new int[bursting];
        burstTelegraphed = new boolean[bursting];
        burstShift = new double[bursting];
        burstBase = new double[bursting];
        burstFoot = new double[bursting];
        burstFirst = new boolean[bursting];
        int b = 0;
        for (int i = 0; i < groundUnits.size(); i++) {
            LevelScript.GroundUnit unit = groundUnits.get(i);
            if (unit.burst().isEmpty()) {
                continue;
            }
            LevelScript.RootBurst burst = unit.burst().get();
            burstSlot[i] = b;
            burstUnit[b] = i;
            burstTelegraphTicks[b] = SimStep.ticks(burst.t());
            boolean first = true;
            double foot = 0;
            for (int j = 0; j < groundUnits.size(); j++) {
                LevelScript.GroundUnit other = groundUnits.get(j);
                if (other.burst().isPresent() && other.burst().get().index() == burst.index()) {
                    first &= j >= i;
                    foot = Math.max(
                            foot,
                            Math.abs(other.x() - burst.x())
                                    + other.enemy().hitbox().width() / 2);
                }
            }
            burstFirst[b] = first;
            burstFoot[b] = foot;
            b++;
        }
    }

    /** Back to the level start: no telegraph started. */
    void reset() {
        java.util.Arrays.fill(burstTelegraphed, false);
        java.util.Arrays.fill(burstShift, 0);
        java.util.Arrays.fill(burstBase, 0);
    }

    /**
     * M5 part H, the root bursts' telegraphs (design/campaign Level 14, Hazards; decision H2 = a), before
     * {@link EnemyForce#spawn} in a step: every burst whose time has come and whose telegraph has not started in
     * this attempt starts it now, moved away from the ship at {@code shipX} when its centre lies closer
     * than {@link LevelScript.RootBurst#SHIP_CLEARANCE} (its event once per burst); {@code groundScroll}
     * is the ground's scroll after this step's {@code scrollStep}.
     */
    void bursts(int levelTick, double shipX, double groundScroll, double scrollStep, int nextGround) {
        burstShipX = shipX;
        burstScroll = groundScroll;
        burstScrollStep = scrollStep;
        for (int b = 0; b < burstUnit.length; b++) {
            if (!burstTelegraphed[b] && burstUnit[b] >= nextGround && levelTick >= burstTelegraphTicks[b]) {
                telegraph(b);
            }
        }
    }

    /** M5 part H: burst slot {@code b}'s telegraph starts: its move away from the ship, its point, its event. */
    private void telegraph(int b) {
        LevelScript.RootBurst burst = groundUnits.get(burstUnit[b]).burst().orElseThrow();
        burstShift[b] = clearOfShip(burst.x(), burstFoot[b], burstShipX);
        burstBase[b] = PlayField.HEIGHT - burst.depth() + burstScroll;
        burstTelegraphed[b] = true;
        if (burstFirst[b]) {
            events.add(
                    SimEvents.Type.ROOT_BURST_TELEGRAPH,
                    burst.x() + burstShift[b],
                    PlayField.HEIGHT - burst.depth(),
                    burst.index());
        }
    }

    /**
     * M5 part H: how far a burst centred at {@code x}, its growths reaching {@code foot} px either side,
     * moves sideways so that it lies {@link LevelScript.RootBurst#SHIP_CLEARANCE} px from the ship at
     * {@code shipX}: 0 when it does already; else away from the ship (a burst right under it toward the
     * field's middle), to the other side when the field has no room there, kept on the field.
     */
    static double clearOfShip(double x, double foot, double shipX) {
        double clearance = LevelScript.RootBurst.SHIP_CLEARANCE;
        double d = x - shipX;
        if (Math.abs(d) >= clearance) {
            return 0;
        }
        double away = d > 0 ? 1 : d < 0 ? -1 : shipX <= PlayField.WIDTH / 2.0 ? 1 : -1;
        double low = Math.min(foot, PlayField.WIDTH / 2.0);
        double high = PlayField.WIDTH - low;
        double to = shipX + away * clearance;
        if (to < low || to > high) {
            to = shipX - away * clearance;
        }
        return Math.clamp(to, low, high) - x;
    }

    /** M5 part H: the root bursts' state, hashed only in a level with them (Act 1's and Levels 08–13's hashes stay). */
    void addBurstsTo(StateHash hash) {
        for (int b = 0; b < burstUnit.length; b++) {
            hash.add(burstTelegraphed[b] ? 1 : 0).add(burstShift[b]).add(burstBase[b]);
        }
    }

    /** M5 part H: the root bursts' growths in the level (each growth a slot; a burst's first is its burst's). */
    int burstSlots() {
        return burstUnit.length;
    }

    /** M5 part H: whether slot {@code b} is its burst's first growth (the burst's own state and events go by it). */
    boolean burstFirst(int b) {
        return burstFirst[b];
    }

    /** M5 part H: slot {@code b}'s burst. */
    LevelScript.RootBurst burstOf(int b) {
        return groundUnits.get(burstUnit[b]).burst().orElseThrow();
    }

    /** M5 part H: whether slot {@code b}'s telegraph has started in this attempt. */
    boolean burstTelegraphed(int b) {
        return burstTelegraphed[b];
    }

    /** M5 part H: whether slot {@code b}'s growth has appeared in this attempt (or the attempt restarted past it). */
    boolean burstGrown(int b, int nextGround) {
        return burstUnit[b] < nextGround;
    }

    /** M5 part H: slot {@code b}'s burst's centre x after its move away from the ship. */
    double burstX(int b) {
        return burstOf(b).x() + burstShift[b];
    }

    /** M5 part H: slot {@code b}'s burst's point's height in ground space: minus the ground's scroll, its screen height. */
    double burstBase(int b) {
        return burstBase[b];
    }

    /**
     * M5 part H: ground unit {@code index} ({@code unit}, a root burst's growth) enters as {@code enemy} of kind
     * {@code kind} with the serial {@code serial}.
     */
    void grow(int index, LevelScript.GroundUnit unit, Enemy enemy, int kind, int serial) {
        // M5 part H: a root burst's growth stands at its burst's point, which scrolled since the telegraph.
        int b = burstSlot[index];
        if (!burstTelegraphed[b]) {
            // Its telegraph fell before a boss checkpoint the attempt restarted from: it starts now.
            telegraph(b);
        }
        LevelScript.RootBurst burst = unit.burst().get();
        enemy.root(unit.enemy(), kind, unit.x(), serial, unit.group());
        // The ground units scroll by this step's distance after they are let in.
        double y = burstBase[b] - burstScroll + burstScrollStep;
        enemy.grow(burst.index(), unit.x() + burstShift[b], y, SimStep.ticks(burst.firstShotSeconds()));
        if (burstFirst[b]) {
            events.add(
                    SimEvents.Type.ROOT_BURST_GREW,
                    burst.x() + burstShift[b],
                    burstBase[b] - burstScroll,
                    burst.index());
        }
    }
}
