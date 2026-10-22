package vanguard.sim;

/**
 * How one {@link Enemy} takes damage (split out of {@link Enemy}): a growth's immunity, a curled shell's and a closed
 * vortex's glance, an aura ring's absorb, the chain head's, latch's and belly's factors, a unit part's HP, and the
 * curl it starts at its roll's share.
 */
final class EnemyDamage {
    /**
     * M7 part D: the fewest steps between two of a unit's {@link SimEvents.Type#RING_ABSORBED} (or armour
     * {@link SimEvents.Type#SHOT_GLANCED}) events from {@link #damage}, so a beam's or a lance's step-by-step
     * damage does not flood the events.
     */
    private static final int ABSORB_EVENT_GAP = 6;

    private final Enemy u;

    EnemyDamage(Enemy unit) {
        this.u = unit;
    }

    /**
     * Takes damage; returns whether it was destroyed. {@code fromAbove} marks damage that comes
     * down from above (bomb and shell blasts, the Airstrike): it ignores the frontal armour of a
     * walker such as the Scuttler (design/player/specials, decisions), which reads it once that
     * armour exists. M7 part E: {@code blast} marks the specials' blasts (the Smart Bomb's ring and the
     * Airstrike's bombs; Q.22e), which a vortex's closed vanes let through; a curled shell still glances them off.
     */
    boolean damage(double amount, boolean fromAbove, boolean blast) {
        if (u.growTicks > 0) {
            // M5 part H: a root burst's growth takes no damage while it grows.
            return false;
        }
        if (u.curlRoll.armoured() || (u.vanesClosed() && !blast)) {
            // M7 part D: a curled shell glances off everything, blasts and the Lance included; M7 part E: a
            // closed vortex's vanes glance off all but the specials' blasts.
            glancedOff();
            return false;
        }
        // M7 part D: an aura ring spends itself first; only the overflow reaches the HP.
        double left = absorb(amount);
        if (left <= 0) {
            u.ticksSinceHit = 0;
            return false;
        }
        // A segment chain's original head is a weak point (its spec carries the chain); a set
        // piece's parts multiply theirs in SetPiece.damagePart.
        u.hp -= u.spec.chain().isPresent() ? left * u.spec.chain().get().headMultiplier() : left;
        if (u.phase == Enemy.Phase.LATCHED) {
            // M5 part F: a latched Lamprey takes its latch's damage factor on top (x2).
            u.hp -= left * (u.spec.latch().orElseThrow().damageFactor() - 1);
        }
        if (u.curlRoll.phase() == Enemy.CurlPhase.BELLY) {
            // M7 part D: its belly shows after the uncurl: the body takes the belly factor.
            u.hp -= left * (u.spec.roll().orElseThrow().bellyFactor() - 1);
        }
        u.ticksSinceHit = 0;
        if (u.hp > 0 && curlDue()) {
            curl();
        }
        return u.hp <= 0;
    }

    /** Spends its ring on {@code amount} of damage; returns what is left for the HP. */
    private double absorb(double amount) {
        if (!(u.auraRing.ring() > 0)) {
            return amount;
        }
        double left = u.auraRing.spend(amount);
        if (u.absorbQuiet == 0) {
            u.events.add(SimEvents.Type.RING_ABSORBED, u.x, u.y(), u.kind);
            u.absorbQuiet = ABSORB_EVENT_GAP;
        }
        return left;
    }

    /**
     * M7 part D: its standing part {@code p} takes damage (an aura ring spends itself first; a curled shell glances
     * off); returns whether this destroyed it.
     */
    boolean damagePart(int p, double amount, boolean fromAbove) {
        if (!u.partAlive(p) || u.growTicks > 0) {
            return false;
        }
        if (u.armoured()) {
            glancedOff();
            return false;
        }
        double left = absorb(amount);
        u.ticksSinceHit = 0;
        if (left <= 0) {
            return false;
        }
        return u.partHp.take(p, left);
    }

    /** A damage glanced off its curled shell: the grey flash's event (spaced like the absorb's). */
    private void glancedOff() {
        if (u.absorbQuiet == 0) {
            u.events.add(SimEvents.Type.SHOT_GLANCED, u.x, u.y(), -1);
            u.absorbQuiet = ABSORB_EVENT_GAP;
        }
    }

    /** Whether its HP fell to its roll's share and its roll is still to come. */
    private boolean curlDue() {
        if (u.curlRoll.rolled()
                || u.spec.roll().isEmpty()
                || !u.walking.active()
                || u.curlRoll.phase() != Enemy.CurlPhase.WALK
                || u.pouncing.leaping()) {
            return false;
        }
        return u.hp <= u.spec.roll().get().belowShare() * u.spec.hp();
    }

    /** It starts its curl where it stands. */
    private void curl() {
        u.curlRoll.curl();
        u.events.add(SimEvents.Type.CURLED, u.x, u.y(), u.kind);
    }
}
