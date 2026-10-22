package vanguard.sim;

/**
 * The ship's regenerating shield over non-regenerating armour (design/player/shields,
 * design/player/armor): damage goes to the shield first and its overflow to armour; collisions
 * deal half to the shield and half straight to armour. After armour damage the ship ignores all
 * damage for a short mercy time (design/player/ship, Hitbox); shield hits give none.
 *
 * <p>The first armour hit of an attempt that leaves the armour at or below {@link #CRITICAL_SHARE}
 * of its maximum (and above zero) sets off {@link SimEvents.Type#ARMOUR_CRITICAL}, Okafor's
 * low-armour radio line (design/player/armor); repairs do not set it off again until the next
 * attempt.
 */
public final class Defences {
    /** The share of the max armour at or below which the armour is critical: sparks, the fast beep, the radio line. */
    public static final double CRITICAL_SHARE = 0.15;

    private final ShieldModel model;
    private final Plating plating;
    private final int mercyTicks;
    private final int delayTicks;
    private final int breakTicks;
    private final double regenPerStep;
    private double shield;
    private double armour;
    private double armourLost;
    private int regenWait;
    private int mercy;
    private boolean broken;
    /** Whether this attempt's armour has been critical: the low-armour radio line has been set off. */
    private boolean critical;
    /** M7 part A, the Shield Overcharge: the capacity's factor while overcharged; 1 otherwise. */
    private double overFactor = 1;
    /** Steps the capacity stays raised; 0 when it is not. */
    private int overTicks;
    /** Steps the excess still bleeds away over; 0 when none bleeds. */
    private int bleedTicks;
    /** The shield the excess bleeds off per step. */
    private double bleedPerStep;
    /** Steps the excess bleeds over at the overcharge's end (from the last activation). */
    private int bleedSteps = 1;
    /** M8 part A, the Auto-repair nanites: steps between repaired points; 0 without them (nothing of them is hashed). */
    private int repairTicks;
    /** The nanites' cap: they never lift the armour above it. */
    private double repairCap;
    /** Steps a hit pauses the nanites for. */
    private int pauseTicks;
    /** Steps the nanites have worked towards the next point. */
    private int repairProgress;
    /** Steps the nanites still pause after a hit. */
    private int repairPause;
    /** The points the nanites repaired since the last restore. */
    private int repaired;

    Defences(ShieldModel model, Plating plating, double mercySeconds) {
        this.model = model;
        this.plating = plating;
        this.mercyTicks = SimStep.ticks(mercySeconds);
        this.delayTicks = SimStep.ticks(model.regenDelaySeconds());
        this.breakTicks = SimStep.ticks(model.breakSeconds());
        this.regenPerStep = model.regenPerSecond() * SimStep.SECONDS;
        restore(plating.maxArmour());
    }

    /**
     * M8 part A: the fitted Auto-repair nanites (design/player/systems, Auto-repair nanites (L24)): from now on {@link
     * #step()} repairs a point every interval up to the cap, paused by a hit.
     */
    void nanites(NaniteSpec spec) {
        repairTicks = Math.max(1, SimStep.ticks(spec.intervalSeconds()));
        repairCap = spec.capShare() * plating.maxArmour();
        pauseTicks = SimStep.ticks(spec.pauseSeconds());
        repairProgress = 0;
        repairPause = 0;
    }

    /** A full shield over {@code armour} points, as at the start of an attempt. */
    void restore(double armour) {
        if (!(armour > 0 && armour <= plating.maxArmour())) {
            throw new IllegalArgumentException("armour " + armour + " outside (0, " + plating.maxArmour() + "]");
        }
        shield = model.capacity();
        this.armour = armour;
        armourLost = 0;
        regenWait = 0;
        mercy = 0;
        broken = false;
        critical = false;
        overFactor = 1;
        overTicks = 0;
        bleedTicks = 0;
        bleedPerStep = 0;
        repairProgress = 0;
        repairPause = 0;
        repaired = 0;
    }

    /** Back to a boss checkpoint's shield and armour, with the armour lost in the level until then. */
    void restore(double shield, double armour, double armourLost) {
        restore(armour);
        this.shield = Math.min(shield, model.capacity());
        this.armourLost = armourLost;
    }

    /**
     * Counts down the mercy time and regenerates the shield once the delay has passed (M7 part A:
     * towards the overcharged capacity while it lasts; at its end the excess bleeds away).
     */
    void step() {
        if (mercy > 0) {
            mercy--;
        }
        if (overTicks > 0 || bleedTicks > 0) {
            overchargeStep();
        }
        if (repairTicks > 0) {
            repairStep();
        }
        double capacity = capacity();
        if (regenWait > 0) {
            regenWait--;
        } else if (shield < capacity) {
            broken = false;
            shield = Math.min(capacity, shield + regenPerStep);
        }
    }

    /**
     * M7 part A, the Shield Overcharge (design/player/specials, Shield Overcharge (L19)): the shield
     * fills to {@code factor} times its capacity at once, a broken shield included (its break and regen
     * delay cancelled), and the capacity stays raised for {@code ticks} steps; the excess then bleeds
     * away over {@code bleedTicks}. Armour, the mercy time and the regen rate are untouched; a new
     * activation starts it afresh.
     */
    void overcharge(double factor, int ticks, int bleed) {
        overFactor = factor;
        overTicks = Math.max(1, ticks);
        bleedTicks = 0;
        bleedPerStep = 0;
        bleedSteps = Math.max(1, bleed);
        shield = capacity();
        broken = false;
        regenWait = 0;
    }

    /**
     * M8 part A: one step of the nanites: while the armour is above zero and below the cap and no hit pauses them,
     * a point every interval (never above the cap); a pause holds the progress towards the next point.
     */
    private void repairStep() {
        if (repairPause > 0) {
            repairPause--;
            return;
        }
        if (armour <= 0 || armour >= repairCap) {
            repairProgress = 0;
            return;
        }
        if (++repairProgress >= repairTicks) {
            repairProgress = 0;
            armour = Math.min(repairCap, armour + 1);
            repaired++;
        }
    }

    private void overchargeStep() {
        if (overTicks > 0) {
            if (--overTicks > 0) {
                return;
            }
            overFactor = 1;
            double excess = shield - model.capacity();
            if (excess > 0) {
                bleedTicks = bleedSteps;
                bleedPerStep = excess / bleedSteps;
            }
            return;
        }
        shield = Math.max(model.capacity(), shield - bleedPerStep);
        if (--bleedTicks == 0 || shield <= model.capacity()) {
            bleedTicks = 0;
            bleedPerStep = 0;
        }
    }

    /** The shield's capacity now: its model's, M7 part A: times the overcharge's factor while that lasts. */
    private double capacity() {
        return overTicks > 0 ? model.capacity() * overFactor : model.capacity();
    }

    /** A bullet: everything to the shield, the overflow to armour. Returns whether armour reached zero. */
    boolean takeShot(double damage, SimEvents events, double x, double y) {
        return absorb(damage, 0, events, x, y);
    }

    /** Contact with an enemy: half to the shield, half to armour. Returns whether armour reached zero. */
    boolean takeCollision(double damage, SimEvents events, double x, double y) {
        return absorb(damage / 2, damage / 2, events, x, y);
    }

    private boolean absorb(double shieldDamage, double armourDamage, SimEvents events, double x, double y) {
        if (mercy > 0 || armour <= 0) {
            return false;
        }
        regenWait = Math.max(regenWait, delayTicks);
        if (repairTicks > 0) {
            // M8 part A: a hit pauses the nanites.
            repairPause = pauseTicks;
        }
        double absorbed = Math.min(shield, shieldDamage);
        if (absorbed > 0) {
            shield -= absorbed;
            events.add(SimEvents.Type.SHIELD_HIT, x, y);
            if (shield <= 0) {
                shield = 0;
                broken = true;
                regenWait = breakTicks + delayTicks;
                events.add(SimEvents.Type.SHIELD_BROKEN, x, y);
            }
        }
        double toArmour = armourDamage + shieldDamage - absorbed;
        if (toArmour > 0) {
            armourLost += Math.min(armour, toArmour);
            armour = Math.max(0, armour - toArmour);
            mercy = mercyTicks;
            events.add(SimEvents.Type.ARMOUR_HIT, x, y);
            if (!critical && armour > 0 && critical(armour, plating.maxArmour())) {
                critical = true;
                events.add(SimEvents.Type.ARMOUR_CRITICAL, x, y);
            }
        }
        return armour <= 0;
    }

    /**
     * M5 part F, a latched Lamprey's drain (design/enemies/air/lamprey): {@code points} off the shield
     * only, never the armour, and the regeneration paused (the step counts as a hit for its delay, also
     * once the shield is down, so it stays down while anything is latched). At zero the break rule
     * applies and nothing more is drained. It drains on through the mercy time after an armour hit
     * (round 34, 12.7b.b = no): the mercy time spares the ship damage, not a latched unit's drain.
     */
    void drain(double points, SimEvents events, double x, double y) {
        if (armour <= 0) {
            return;
        }
        regenWait = Math.max(regenWait, delayTicks);
        if (shield <= 0) {
            return;
        }
        shield -= Math.min(shield, points);
        if (shield <= 0) {
            shield = 0;
            broken = true;
            regenWait = breakTicks + delayTicks;
            events.add(SimEvents.Type.SHIELD_BROKEN, x, y);
        }
    }

    /** A shield cell: adds {@code points} up to the capacity (M7 part A: the overcharged one while it lasts). */
    void restoreShield(double points) {
        shield = Math.max(shield, Math.min(capacity(), shield + points));
    }

    /** An armour patch: adds {@code points} up to the plating's maximum. */
    void repair(double points) {
        armour = Math.min(plating.maxArmour(), armour + points);
    }

    void addTo(StateHash hash) {
        hash.add(shield).add(armour).add(armourLost).add(regenWait).add(mercy).add(broken ? 1 : 0);
        if (critical) {
            // Only once set, so the hash of a run whose armour never gets this low stays as it was.
            hash.add(1);
        }
        if (repairTicks > 0) {
            // M8 part A: only with the nanites fitted, so other runs hash as before.
            hash.add(repairProgress).add(repairPause).add(repaired);
        }
        if (overTicks > 0 || bleedTicks > 0) {
            // M7 part A: only while an overcharge or its bleed runs, so other runs hash as before.
            hash.add(overFactor)
                    .add(overTicks)
                    .add(bleedTicks)
                    .add(bleedPerStep)
                    .add(bleedSteps);
        }
    }

    /** Whether {@code armour} of {@code maxArmour} is at or below {@link #CRITICAL_SHARE}. */
    public static boolean critical(double armour, double maxArmour) {
        return armour / maxArmour <= CRITICAL_SHARE;
    }

    /** Whether this attempt's armour has dropped to the critical share (the low-armour radio line was set off). */
    public boolean wasCritical() {
        return critical;
    }

    /** Armour points lost since the defences were last restored (repairs do not undo it). */
    public double armourLost() {
        return armourLost;
    }

    public double shield() {
        return shield;
    }

    /** The shield's capacity (the HUD's scale); M7 part A: without an overcharge's raise, see {@link #shieldCapacity()}. */
    public double maxShield() {
        return model.capacity();
    }

    /** M7 part A: the shield's capacity now, raised while overcharged. */
    public double shieldCapacity() {
        return capacity();
    }

    /** M7 part A: whether the Shield Overcharge raises the capacity now (the white double shell). */
    public boolean overcharged() {
        return overTicks > 0;
    }

    /** M7 part A: steps the overcharge still lasts; 0 when none does. */
    public int overchargeTicks() {
        return overTicks;
    }

    /** M7 part A: whether the excess of an ended overcharge is bleeding away. */
    public boolean bleeding() {
        return bleedTicks > 0;
    }

    public double armour() {
        return armour;
    }

    public double maxArmour() {
        return plating.maxArmour();
    }

    /** Whether the shield is down after a break and not regenerating yet. */
    public boolean broken() {
        return broken;
    }

    /** The ship takes no damage for the next {@code ticks} steps (the Smart Bomb's), as in its mercy time. */
    void guard(int ticks) {
        mercy = Math.max(mercy, ticks);
    }

    /**
     * M8 part A: whether the Auto-repair nanites repair now (the HUD's {@code +} pip on the armour bar): fitted, the
     * armour above zero and below their cap, and no hit pausing them.
     */
    public boolean repairing() {
        return repairTicks > 0 && repairPause == 0 && armour > 0 && armour < repairCap;
    }

    /** M8 part A: the armour points the nanites repaired since the defences were last restored. */
    public int repaired() {
        return repaired;
    }

    /** Steps of mercy invulnerability left; the hull flashes white meanwhile. */
    public int mercyTicks() {
        return mercy;
    }
}
