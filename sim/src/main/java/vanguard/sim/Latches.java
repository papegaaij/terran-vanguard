package vanguard.sim;

import java.util.Arrays;

/**
 * M5 part F (design/enemies/air/lamprey): the units latched onto the ship, the shake-off that throws them
 * off and their drain on the shield, for a {@link Sortie}. It exists in every sortie; without a unit with a
 * latch in the level ({@link #present()} false) the sortie never steps it and it hashes nothing.
 */
final class Latches {
    private final Sortie s;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    /** The latch of the level's first unit with one (the Lamprey); null without one. */
    private final EnemySpec.Latch latch;
    /** The steps of the last reversals of the commanded horizontal direction while latched, a ring. */
    private final int[] reversals;

    private int reversalNext;
    /** The last commanded horizontal direction, -1 left, 1 right, 0 none yet. */
    private int lastDirection;
    /** The units latched onto the ship now. */
    private int latchedCount;
    /** Whether this attempt's first latch has happened (the radio's first-latch line). */
    private boolean latchSeen;

    Latches(Sortie s, Ship ship, EnemyForce force, SimEvents events, Radio radio) {
        this.s = s;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        EnemySpec.Latch firstLatch = null;
        for (EnemySpec kind : force.kinds()) {
            if (firstLatch == null && kind.latch().isPresent()) {
                firstLatch = kind.latch().get();
            }
        }
        latch = firstLatch;
        reversals = new int[latch == null ? 0 : latch.reversals()];
        clearReversals();
    }

    /** Whether a unit of the level can latch on. */
    boolean present() {
        return latch != null;
    }

    /** The units latched onto the ship after the last step. */
    int count() {
        return latchedCount;
    }

    /** A new attempt: nothing latched, no reversal remembered. */
    void reset() {
        if (latch != null) {
            clearReversals();
        }
        lastDirection = 0;
        latchedCount = 0;
        latchSeen = false;
    }

    /** Hashed only in a level with a latch, so the other levels hash as before. */
    void addTo(StateHash hash) {
        if (latch != null) {
            hash.add(lastDirection).add(latchedCount).add(latchSeen ? 1 : 0).add(reversalNext);
            for (int reversal : reversals) {
                hash.add(reversal);
            }
        }
    }

    /**
     * Whether something drains the shield: the HUD's {@code SHIELD DRAIN} and the bar's flicker (M7 part A: a
     * latched unit the EMP Burst stuns does not).
     */
    boolean draining() {
        if (latchedCount == 0) {
            return false;
        }
        Pool<Enemy> enemies = force.enemies();
        for (int j = 0; j < enemies.size(); j++) {
            if (enemies.get(j).draining()) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part F (design/enemies/air/lamprey): {@code enemy} latches onto the hull at the spot it touched
     * from: its event, and the attempt's first latch's radio line. Reversals count from the first latch
     * on while anything stays latched.
     */
    void latchOn(Enemy enemy) {
        int spot = EnemySpec.Latch.spotFor(enemy.x() - ship.x(), enemy.y() - ship.y());
        if (latchedCount == 0) {
            clearReversals();
        }
        enemy.latch(spot, ship.x(), ship.y());
        latchedCount++;
        events.add(SimEvents.Type.LAMPREY_LATCHED, enemy.x(), enemy.y(), spot);
        if (!latchSeen) {
            latchSeen = true;
            radio.cue(CueTrigger.FIRST_LATCH, "");
        }
    }

    /** M5 part F: counts the units latched onto the ship now. */
    void countLatched() {
        Pool<Enemy> enemies = force.enemies();
        int count = 0;
        for (int j = 0; j < enemies.size(); j++) {
            if (enemies.get(j).latched()) {
                count++;
            }
        }
        latchedCount = count;
    }

    /**
     * M5 part F, the shake-off (design/enemies/air/lamprey): a reversal of the commanded horizontal
     * direction ({@code LEFT} against {@code RIGHT}; neutral steps, and both at once, ignored) while
     * something is latched is remembered; the latch's number of them within its time throws off every
     * latched unit. It reads the commands, so recorded inputs replay it.
     */
    void shake(int commands) {
        boolean left = Command.LEFT.in(commands);
        boolean right = Command.RIGHT.in(commands);
        if (left == right) {
            return;
        }
        int direction = left ? -1 : 1;
        boolean reversal = lastDirection != 0 && direction != lastDirection;
        lastDirection = direction;
        if (!reversal) {
            return;
        }
        countLatched();
        if (latchedCount == 0) {
            return;
        }
        reversals[reversalNext] = s.realTick;
        reversalNext = (reversalNext + 1) % reversals.length;
        // The oldest of the last reversals is the next to be overwritten.
        if (s.realTick - reversals[reversalNext] > SimStep.ticks(latch.withinSeconds())) {
            return;
        }
        Pool<Enemy> enemies = force.enemies();
        int thrown = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.latched()) {
                enemy.shakeOff();
                thrown++;
            }
        }
        latchedCount = 0;
        clearReversals();
        events.add(SimEvents.Type.LAMPREY_SHAKEN, ship.x(), ship.y(), thrown);
    }

    /** M5 part F: forgets the remembered reversals. */
    private void clearReversals() {
        Arrays.fill(reversals, Integer.MIN_VALUE / 2);
        reversalNext = 0;
    }

    /** M5 part F: every latched unit drains the shield at its rate (design/enemies/air/lamprey). */
    void drainShield() {
        Pool<Enemy> enemies = force.enemies();
        int count = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.latched()) {
                count++;
                if (enemy.draining()) {
                    // M7 part A: a latched unit the EMP Burst stuns stays latched, its drain paused.
                    double rate = enemy.spec().latch().orElseThrow().drainRate();
                    ship.defences().drain(rate * SimStep.SECONDS, events, ship.x(), ship.y());
                }
            }
        }
        latchedCount = count;
    }
}
