package vanguard.sim;

/**
 * M5 part D: one {@code rear ambush} unit's way (design/enemies/air/wraith, user decision D6 = a; see {@link
 * EnemySpec.Ambush} and {@link EnemySpec.Cloak}). Each {@link Enemy} owns one, idle ({@link Enemy.AmbushPhase#NONE})
 * for any unit that did not enter as an ambush; it flies its unit along the ambush's paths through the unit's
 * movement fields.
 */
final class Ambushing {
    private final Enemy unit;

    /** The ambush unit's plan; null for any other unit (its fields are hashed only with one). */
    private Spawn.Ambush ambush;

    private Enemy.AmbushPhase ambushPhase = Enemy.AmbushPhase.NONE;
    /** Steps since it entered, until its re-entry. */
    private int ambushTicks;
    /** Steps from its entry to its re-entry at the bottom edge (the end of its edge warning). */
    private int reentryTicks;
    /** Whether it decloaked in this step (not hashed: set and read within the step). */
    private boolean decloaked;
    /** The volleys it started in its hold. */
    private int ambushVolleys;

    Ambushing(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit that is no ambush unit. */
    void clear() {
        ambush = null;
        ambushPhase = Enemy.AmbushPhase.NONE;
        ambushTicks = 0;
        reentryTicks = 0;
        decloaked = false;
        ambushVolleys = 0;
    }

    /** It enters as an ambush unit on {@code plan}'s swoop, re-entering {@code reentry} steps after its entry. */
    void start(Spawn.Ambush plan, int reentry) {
        ambush = plan;
        ambushPhase = Enemy.AmbushPhase.SWOOP;
        ambushTicks = 0;
        reentryTicks = reentry;
    }

    /**
     * One step of its way: its swoop down its lane and off the bottom edge, the gap below it, its rise back up to its
     * hold point, the decloak flash and the hold there (its gun counting its first-shot delay from the stop), then
     * its exit up the side lane; returns false once it has left the top edge.
     */
    boolean step() {
        Enemy u = unit;
        decloaked = false;
        switch (ambushPhase) {
            case SWOOP, GAP -> {
                if (ambushPhase == Enemy.AmbushPhase.SWOOP) {
                    u.distance += u.speed * SimStep.SECONDS;
                    if (u.distance >= u.path.length()) {
                        u.distance = u.path.length();
                        ambushPhase = Enemy.AmbushPhase.GAP;
                    }
                    u.place();
                }
                if (++ambushTicks >= reentryTicks) {
                    // It re-enters its lane where its swoop left it, below the bottom edge.
                    ambushPhase = Enemy.AmbushPhase.RISE;
                    u.path = ambush.rise();
                    u.segment = 0;
                    u.distance = 0;
                    u.place();
                }
            }
            case RISE -> {
                u.distance += u.speed * SimStep.SECONDS;
                if (u.distance >= u.path.length()) {
                    u.distance = u.path.length();
                    u.place();
                    decloak();
                } else {
                    u.place();
                }
            }
            case DECLOAK -> {
                if (--u.holdTicks <= 0) {
                    ambushPhase = Enemy.AmbushPhase.HOLD;
                    u.holdTicks = Math.max(1, SimStep.ticks(ambush.holdSeconds()));
                }
            }
            case HOLD -> {
                if (--u.holdTicks <= 0) {
                    ambushPhase = Enemy.AmbushPhase.EXIT;
                    u.burstLeft = 0;
                    u.path = ambush.exit();
                    u.segment = 0;
                    u.distance = 0;
                    u.speed = ambush.exitSpeed();
                }
            }
            case EXIT -> {
                u.distance += u.speed * SimStep.SECONDS;
                if (u.distance >= u.path.length()) {
                    return false;
                }
                u.place();
            }
            case NONE -> throw new IllegalStateException("an ambush unit without its phase");
        }
        return true;
    }

    /** It stops at its hold point and decloaks: the flash starts, its gun counts from here. */
    private void decloak() {
        Enemy u = unit;
        decloaked = true;
        u.burstLeft = 0;
        // + 1: the gun already counts down in the step the unit stops, as a hover's (see Enemy.hold()).
        u.volleyTicks =
                u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        int flash = SimStep.ticks(ambush.flashSeconds());
        if (flash > 0) {
            ambushPhase = Enemy.AmbushPhase.DECLOAK;
            u.holdTicks = flash;
        } else {
            ambushPhase = Enemy.AmbushPhase.HOLD;
            u.holdTicks = Math.max(1, SimStep.ticks(ambush.holdSeconds()));
        }
    }

    /** Where it is on its way. */
    Enemy.AmbushPhase phase() {
        return ambushPhase;
    }

    /** Whether it decloaked in this step. */
    boolean decloakedNow() {
        return decloaked;
    }

    /** Whether it is at or past its decloak (its flash, hold and exit). */
    boolean decloakedPhase() {
        return ambushPhase.compareTo(Enemy.AmbushPhase.DECLOAK) >= 0;
    }

    /**
     * How far the decloak of a unit with a cloak is, between the previous and the current step ({@code alpha} in
     * [0, 1]): 0 while it is cloaked, rising evenly to 1 over the flash, 1 after it.
     */
    double decloak(double alpha) {
        if (ambushPhase.compareTo(Enemy.AmbushPhase.HOLD) >= 0) {
            return 1;
        }
        if (ambushPhase != Enemy.AmbushPhase.DECLOAK) {
            return 0;
        }
        int flash = Math.max(1, SimStep.ticks(ambush.flashSeconds()));
        return Math.clamp((flash - unit.holdTicks + alpha) / flash, 0.0, 1.0);
    }

    /**
     * It starts a volley in its hold: returns whether that is past {@link Enemy#AMBUSH_VOLLEYS} (the gun then stays
     * silent).
     */
    boolean volleysSpent() {
        return ++ambushVolleys > Enemy.AMBUSH_VOLLEYS;
    }

    /** Its state, in the order the unit's hash has always added it; nothing for a unit that is no ambush unit. */
    void addTo(StateHash hash) {
        if (ambush != null) {
            hash.add(ambushPhase.ordinal())
                    .add(ambushTicks)
                    .add(reentryTicks)
                    .add(unit.speed)
                    .add(ambushVolleys);
        }
    }
}
