package vanguard.sim;

/**
 * M7 part D: one walker's curl-roll (design/enemies/ground/shellback): once its HP falls to its roll's share
 * it tucks in where it stands, rolls along its path at the roll's speed crushing ground targets, uncurls and
 * shows its belly, then walks on. From the curl to the end of the uncurl it is armoured and its hit box is
 * the ball. Each {@link Enemy} owns one, used only while its stat block has a roll (its fields are hashed
 * only then).
 */
final class CurlRoll {
    /** Where it is in its curl-roll; {@link Enemy.CurlPhase#WALK} for any other unit. */
    private Enemy.CurlPhase curlPhase = Enemy.CurlPhase.WALK;
    /** Steps into the curl-roll's current phase. */
    private int curlTicks;
    /** Whether it curled already (once per unit). */
    private boolean rolled;
    /** The ground allies this roll crushed already, one bit each (one hit per roll per ally). */
    private int crushed;

    /** Back to a unit that never curled. */
    void clear() {
        curlPhase = Enemy.CurlPhase.WALK;
        curlTicks = 0;
        rolled = false;
        crushed = 0;
    }

    /** Hashed only for a unit with a roll. */
    void addTo(StateHash hash) {
        hash.add(curlPhase.ordinal()).add(curlTicks).add(rolled ? 1 : 0).add(crushed);
    }

    /** Whether it is curled: armoured against every damage, its hit box the ball. */
    boolean armoured() {
        return curlPhase == Enemy.CurlPhase.CURLING
                || curlPhase == Enemy.CurlPhase.ROLLING
                || curlPhase == Enemy.CurlPhase.UNCURLING;
    }

    /** Whether it curled already. */
    boolean rolled() {
        return rolled;
    }

    /** It starts its curl where it stands (the caller reports it). */
    void curl() {
        rolled = true;
        curlPhase = Enemy.CurlPhase.CURLING;
        curlTicks = 0;
        crushed = 0;
    }

    /**
     * One step of the curl-roll of {@code unit} after its walk: curling, rolling along its path at {@code
     * roll}'s speed, uncurling, its belly; the roll's start and the uncurl are reported to {@code events}.
     */
    void step(Enemy unit, EnemySpec.Roll roll, SimEvents events) {
        curlTicks++;
        switch (curlPhase) {
            case CURLING -> {
                if (curlTicks >= SimStep.ticks(roll.curlSeconds())) {
                    curlPhase = Enemy.CurlPhase.ROLLING;
                    curlTicks = 0;
                    events.add(SimEvents.Type.ROLL_STARTED, unit.x(), unit.y(), unit.kind());
                }
            }
            case ROLLING -> {
                unit.walk(roll.rollSpeed());
                if (curlTicks >= SimStep.ticks(roll.rollSeconds())) {
                    curlPhase = Enemy.CurlPhase.UNCURLING;
                    curlTicks = 0;
                    events.add(SimEvents.Type.UNCURLED, unit.x(), unit.y(), unit.kind());
                }
            }
            case UNCURLING -> {
                if (curlTicks >= SimStep.ticks(roll.uncurlSeconds())) {
                    curlPhase = Enemy.CurlPhase.BELLY;
                    curlTicks = 0;
                }
            }
            case BELLY -> {
                if (curlTicks >= SimStep.ticks(roll.bellySeconds())) {
                    curlPhase = Enemy.CurlPhase.WALK;
                    curlTicks = 0;
                }
            }
            case WALK -> {
                // Not reached: the walk steps itself.
            }
        }
    }

    /** Where it is in its curl-roll. */
    Enemy.CurlPhase phase() {
        return curlPhase;
    }

    /** How far into the curl-roll's current phase it is, 0 to 1, with {@code roll}; 0 while it walks. */
    double progress(EnemySpec.Roll roll) {
        double seconds =
                switch (curlPhase) {
                    case CURLING -> roll.curlSeconds();
                    case ROLLING -> roll.rollSeconds();
                    case UNCURLING -> roll.uncurlSeconds();
                    case BELLY -> roll.bellySeconds();
                    case WALK -> 1;
                };
        return Math.min(1, (double) curlTicks / Math.max(1, SimStep.ticks(seconds)));
    }

    /** Whether this roll crushed ground ally {@code k} already. */
    boolean crushedAlly(int k) {
        return k < Integer.SIZE && (crushed & 1 << k) != 0;
    }

    /** This roll crushed ground ally {@code k}. */
    void markCrushed(int k) {
        if (k < Integer.SIZE) {
            crushed |= 1 << k;
        }
    }
}
