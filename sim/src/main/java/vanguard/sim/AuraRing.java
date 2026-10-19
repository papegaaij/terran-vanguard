package vanguard.sim;

/**
 * M7 part D: one unit's aura ring (design/enemies/air/choir-herald), fed by a Herald and stepped by {@link
 * Auras}: its fill, refill, keep and fade, its collapse at its Herald's death and the EMP Burst's strip.
 * Each {@link Enemy} owns one; its fields are in the state hash only once the unit carried a ring in its
 * life. The damage it absorbs is spent in {@link Enemy#damage} ({@link #spend}).
 */
final class AuraRing {
    /** Whether it carried a ring in this life (its fields are in the state hash only then). */
    private boolean ringed;
    /** The ring's damage left and its maximum. */
    private double ring;

    private double ringMax;
    /** The serial of the Herald that feeds its ring; -1 for none. */
    private int ringFeeder = -1;
    /** That Herald's aura (not hashed: it follows from the feeder's kind). */
    private EnemySpec.Aura ringAura;
    /** Whether its ring is filling (a unit that entered the radius). */
    private boolean ringFilling;
    /** Steps since its ring last absorbed damage or was stripped (the refill's delay). */
    private int ringIdle;
    /** Steps it was out of its feeder's reach while that one sang (the ring is kept, then fades). */
    private int ringAway;
    /** Steps into its ring's collapse at its Herald's death; -1 while none runs. */
    private int ringCollapse = -1;
    /** The ring when its collapse began. */
    private double ringFrom;
    /** Within {@link Auras#step}: whether its feeder reached it in this step, or is stunned (not hashed). */
    private boolean ringFed;

    private boolean ringPaused;

    /** A new life: no ring carried. */
    void reset() {
        ringed = false;
        clearRing();
    }

    /** Its ring back to none. */
    private void clearRing() {
        ring = 0;
        ringMax = 0;
        ringFeeder = -1;
        ringAura = null;
        ringFilling = false;
        ringIdle = 0;
        ringAway = 0;
        ringCollapse = -1;
        ringFrom = 0;
        ringFed = false;
        ringPaused = false;
    }

    /** Only once it carried a ring in this life, so the other units hash as before. */
    void addTo(StateHash hash) {
        if (ringed) {
            hash.add(ring)
                    .add(ringMax)
                    .add(ringFeeder)
                    .add(ringFilling ? 1 : 0)
                    .add(ringIdle)
                    .add(ringAway)
                    .add(ringCollapse)
                    .add(ringFrom);
        }
    }

    /** {@link Auras#step}: the step starts; nobody reached its ring yet. */
    void scratch() {
        ringFed = false;
        ringPaused = false;
    }

    /** {@link Auras#step}: its own feeder reached it in this step. */
    void feed() {
        ringFed = true;
    }

    /** {@link Auras#step}: its feeder is stunned: its ring's timers wait. */
    void pause() {
        ringPaused = true;
    }

    boolean fedNow() {
        return ringFed;
    }

    boolean pausedNow() {
        return ringPaused;
    }

    /**
     * {@link Auras#step}: the Herald {@code feeder} with {@code aura} rings it from now (it entered the radius,
     * or its old feeder lost it): its maximum is {@code hp}, its HP at the level, capped by the aura, and it
     * fills over the aura's fill time.
     */
    void claim(EnemySpec.Aura aura, int feeder, double hp) {
        ringed = true;
        ringFeeder = feeder;
        ringAura = aura;
        ringMax = Math.min(hp, aura.cap());
        ring = Math.min(ring, ringMax);
        ringFilling = ring < ringMax;
        ringIdle = 0;
        ringAway = 0;
        ringCollapse = -1;
        ringFed = true;
    }

    /** {@link Auras#step}: one step of its ring: a collapse, the fill and refill while fed, the keep and fade away. */
    void step() {
        EnemySpec.Aura aura = ringAura;
        if (ringCollapse >= 0) {
            int ticks = Math.max(1, SimStep.ticks(aura.collapseSeconds()));
            ringCollapse++;
            ring = ringFrom * Math.max(0, 1 - (double) ringCollapse / ticks);
            if (ringCollapse >= ticks) {
                clearRing();
            }
            return;
        }
        if (ringPaused) {
            return;
        }
        double fill = ringMax / Math.max(1, SimStep.ticks(aura.fillSeconds()));
        if (ringIdle < Integer.MAX_VALUE) {
            ringIdle++;
        }
        if (ringFed) {
            ringAway = 0;
            if (ringFilling) {
                ring = ring + fill >= ringMax - 1e-9 ? ringMax : ring + fill;
                ringFilling = ring < ringMax;
            } else if (ringIdle > SimStep.ticks(aura.refillDelaySeconds()) && ring < ringMax) {
                ring = Math.min(ringMax, ring + aura.refillShare() * ringMax * SimStep.SECONDS);
            }
            return;
        }
        ringFilling = false;
        if (++ringAway > SimStep.ticks(aura.keepSeconds())) {
            ring -= fill;
            if (ring <= 1e-9) {
                clearRing();
            }
        }
    }

    /** Its Herald died: its ring collapses over the aura's collapse time. */
    void collapse() {
        if (ringFeeder < 0) {
            return;
        }
        ringCollapse = 0;
        ringFrom = ring;
        ringFilling = false;
    }

    /**
     * The EMP Burst (design/player/specials): its ring is stripped to nothing (its Herald keeps it, the
     * refill waits its delay again); returns whether it had one.
     */
    boolean strip() {
        if (ringFeeder < 0) {
            return false;
        }
        if (ringCollapse >= 0) {
            clearRing();
            return true;
        }
        ring = 0;
        ringFilling = false;
        ringIdle = 0;
        return true;
    }

    /**
     * Spends its ring on {@code amount} of damage, which it must hold ({@link #ring()} &gt; 0); returns what is
     * left for the HP.
     */
    double spend(double amount) {
        double taken = Math.min(ring, amount);
        ring -= taken;
        ringIdle = 0;
        return amount - taken;
    }

    /** Its damage left; 0 without one. */
    double ring() {
        return ring;
    }

    /** Its maximum (the unit's HP at the level, capped by the aura); 0 without a ring. */
    double ringMax() {
        return ringMax;
    }

    /** Its share left, 0 to 1; 0 without a ring. */
    double ringShare() {
        return ringMax > 0 ? ring / ringMax : 0;
    }

    /** The serial of the Herald that feeds it; -1 for none. */
    int ringFeeder() {
        return ringFeeder;
    }

    /** How far its collapse at its Herald's death is, 0 to 1; 0 while none runs. */
    double ringCollapse() {
        if (ringCollapse < 0 || ringAura == null) {
            return 0;
        }
        return Math.min(1, (double) ringCollapse / Math.max(1, SimStep.ticks(ringAura.collapseSeconds())));
    }
}
