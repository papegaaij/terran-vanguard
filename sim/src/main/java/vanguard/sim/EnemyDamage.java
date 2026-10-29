package vanguard.sim;

/**
 * How one {@link Enemy} takes damage (split out of {@link Enemy}): a growth's immunity, a curled shell's, a closed
 * vortex's and a closed coil's glance, an aura ring's absorb, the chain head's, latch's and belly's factors, a unit part's HP, and the
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
     * Airstrike's bombs; Q.22e), which a vortex's closed vanes let through; a curled shell still glances them off. M8
     * part E: {@code antiSub} marks {@code anti-sub} fire (the Harpoon and Torpedo Pod, the Depth Charges, the
     * proximity mines, an {@code anti-sub} wingman gun), which cracks a coil's closed shell as a blast does (§5 E8).
     */
    boolean damage(double amount, boolean fromAbove, boolean blast, boolean antiSub) {
        return damage(amount, fromAbove, blast, antiSub, 1);
    }

    /**
     * As {@link #damage(double, boolean, boolean, boolean)}; M9 part C (design/enemies/air/gilded-gunship): a direct
     * hit on facing armour, whose overflow past the hex shield takes {@code plating} (the shield spends itself at full
     * damage first); exactly 1 for none.
     */
    boolean damage(double amount, boolean fromAbove, boolean blast, boolean antiSub, double plating) {
        if (u.growTicks > 0 || u.toppling.active()) {
            // M9 part E: nor does a toppling walker: it falls and is destroyed on landing.
            // M5 part H: a root burst's growth takes no damage while it grows.
            return false;
        }
        if (u.curlRoll.armoured() || (u.vanesClosed() && !blast) || (u.shellClosed() && !blast && !antiSub)) {
            // M7 part D: a curled shell glances off everything, blasts and the Lance included; M7 part E: a
            // closed vortex's vanes glance off all but the specials' blasts; M8 part E: a coil's closed shell all but
            // those and anti-sub fire.
            glancedOff();
            return false;
        }
        // M9 part A: a hex shield spends itself first, then (M7 part D) an aura ring; only the overflow reaches the HP.
        double left = absorb(shielded(amount));
        if (left <= 0) {
            u.ticksSinceHit = 0;
            return false;
        }
        if (plating != 1) {
            // M9 part C: the armour plate halves it, the engines double it (after the shield).
            plated(plating);
            left *= plating;
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

    /** M9 part A: spends its hex shield on {@code amount} of damage (none for most units); returns what is left. */
    private double shielded(double amount) {
        if (!u.hexShield.fitted()) {
            return amount;
        }
        return u.hexShield.spend(amount, u.events, u.x, u.y(), u.kind, ABSORB_EVENT_GAP);
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
        if (!u.partAlive(p) || u.growTicks > 0 || u.toppling.active()) {
            return false;
        }
        if (u.armoured()) {
            glancedOff();
            return false;
        }
        double left = absorb(shielded(amount));
        u.ticksSinceHit = 0;
        if (left <= 0) {
            return false;
        }
        return u.partHp.take(p, left);
    }

    /** M9 part C: a hit on its facing armour: the plate's or the engines' flash (spaced like the absorb's). */
    private void plated(double plating) {
        if (u.absorbQuiet == 0) {
            u.events.add(SimEvents.Type.PLATING_HIT, u.x, u.y(), plating < 1 ? ArmourArcs.FRONT : ArmourArcs.REAR);
            u.absorbQuiet = ABSORB_EVENT_GAP;
        }
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
