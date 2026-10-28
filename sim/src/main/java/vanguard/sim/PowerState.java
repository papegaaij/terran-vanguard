package vanguard.sim;

/**
 * M9 part A, the power budget in flight (design/player/generator, Enemy drain effects; {@link Loadout.Power}): the
 * latched drainers, the effective output E = output − their drain (each {@link Loadout.Drain#perDrainer()} MW, at most
 * {@link Loadout.Drain#most()}), and what a load over E sheds: first the shield's regeneration (its delay keeps
 * running), then, once the shortfall exceeds the shield's draw, every fitted weapon's rate of fire × f = E ÷ (load −
 * shield draw), never below the floor. Below the load the spare-power bonus follows E. The rate factor is applied by a
 * fractional accumulator per mount ({@link #runs}): a mount's clocks (its cooldown, a Deflector Pod's charge return) run
 * on a step only when its credit reaches a whole step, so a mount fires at f times its rate, deterministically and
 * without allocating; a beam's damage per step is × f ({@link #factor()}). Armour, the engine, the utility modules, the
 * specials and the escort are never affected. Owned by the {@link PlayerFire}; with no drainer latched nothing changes,
 * and nothing of it is hashed until the first drain of an attempt (the levels without a drainer hash as before).
 */
public final class PowerState {
    private final Loadout.Power power;
    /** Per mount: its credit of steps (the factor added each step) and whether its clocks run in this step. */
    private final double[] credit;

    private final boolean[] runs;
    /** The drainers latched now. */
    private int drainers;
    /** Whether a drainer latched in this attempt (its fields are hashed only then). */
    private boolean drained;
    /** The weapons' rate factor now, f; 1 without a shortfall beyond the shield's draw. */
    private double factor = 1;

    PowerState(Loadout.Power power, int mounts) {
        this.power = power;
        credit = new double[mounts];
        runs = new boolean[mounts];
        reset();
    }

    /** Back to the level start: nothing latched. */
    void reset() {
        drainers = 0;
        drained = false;
        factor = 1;
        java.util.Arrays.fill(credit, 0);
        java.util.Arrays.fill(runs, true);
    }

    /**
     * A drainer latched ({@code delta} +1) or let go (−1: shaken off or killed); returns the drainers latched now. A
     * fourth latches but drains nothing more (the cap).
     */
    int drain(int delta) {
        drainers = Math.max(0, drainers + delta);
        drained |= drainers > 0;
        factor = rateFactor();
        if (factor >= 1) {
            java.util.Arrays.fill(credit, 0);
        }
        return drainers;
    }

    /** One step: each mount's credit grows by f and its clocks run when it holds a whole step. */
    void step() {
        if (factor >= 1) {
            return;
        }
        for (int m = 0; m < credit.length; m++) {
            credit[m] += factor;
            runs[m] = credit[m] >= 1;
            if (runs[m]) {
                credit[m] -= 1;
            }
        }
    }

    /** Whether mount {@code m}'s clocks (its cooldown, a Deflector Pod's return) run in this step. */
    boolean runs(int m) {
        return factor >= 1 || runs[m];
    }

    /** Only once a drainer latched in this attempt. */
    void addTo(StateHash hash) {
        if (drained) {
            hash.add(drainers).add(factor);
            for (double c : credit) {
                hash.add(c);
            }
        }
    }

    /** The drainers latched now. */
    public int drainers() {
        return drainers;
    }

    /** The MW drained now: each drainer's, at most the cap. */
    public double drainedMw() {
        return Math.min(drainers * power.drain().perDrainer(), power.drain().most());
    }

    /** The effective output E now, MW. */
    public double effectiveOutput() {
        return power.output() - drainedMw();
    }

    /** The fitted load, MW (an overdrive's extra never counts). */
    public double load() {
        return power.load();
    }

    /** The generator's output, MW. */
    public double output() {
        return power.output();
    }

    /** Whether the load is over E now: the shield's regeneration stops (the HUD's flash, the brown-out hum). */
    public boolean overLoad() {
        return drainers > 0 && power.load() > effectiveOutput() + 1e-9;
    }

    /** Whether the shield's regeneration is stopped now (the load over E). */
    public boolean regenStopped() {
        return overLoad();
    }

    /** The weapons' rate factor now, f (the HUD's {@code FIRE nn%}); 1 without a shortfall beyond the shield's draw. */
    public double factor() {
        return factor;
    }

    /** Whether a drainer is latched now: the shield's regen then follows {@link #regenPerSecond()}. */
    boolean draining() {
        return drainers > 0;
    }

    /** The shield's regen per second now: its own with the spare-power bonus of E − load (0 when over the load). */
    double regenPerSecond() {
        if (overLoad()) {
            return 0;
        }
        double spare = effectiveOutput() - power.load();
        return power.regenPerSecond() * (1 + Math.clamp(spare * power.regenBonusPerMw(), 0, power.maxRegenBonus()));
    }

    private double rateFactor() {
        if (drainers == 0) {
            return 1;
        }
        double e = effectiveOutput();
        double shortfall = power.load() - e;
        if (!(shortfall > power.shieldDraw())) {
            return 1;
        }
        double weapons = power.load() - power.shieldDraw();
        return Math.max(power.drain().floor(), Math.min(1, e / weapons));
    }
}
