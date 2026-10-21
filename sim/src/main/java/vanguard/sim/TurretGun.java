package vanguard.sim;

/**
 * M7 part F: one walker's turret, turning independently of its hull (design/enemies/ground/warden-tank; see {@link
 * EnemySpec.Turret}): its turn toward its target and its gun's lock and bursts. Each {@link Enemy} owns one, used
 * only while its stat block has a turret; it turns the unit's aim and runs the unit's gun clock through the unit's
 * fields.
 */
final class TurretGun {
    private final Enemy unit;

    /**
     * Its lock: steps left before its burst while it has stopped turning (the tell); -1 while it turns. Hashed only
     * with a turret.
     */
    private int turretLock = -1;
    /** How far its aim was off its target in this step's turn, radians (set and read within the step). */
    private double turretOff = Double.POSITIVE_INFINITY;

    TurretGun(Enemy unit) {
        this.unit = unit;
    }

    /** A walker enters: its turret turns (no lock). */
    void start() {
        turretLock = -1;
    }

    /**
     * Its turret turns toward (targetX, targetY) by at most its turn rate, all round, while the unit is on the screen
     * and its gun is not quiet; it holds while it is locked for its burst (the tell) and through the burst.
     */
    void turn(double targetX, double targetY) {
        Enemy u = unit;
        turretOff = Double.POSITIVE_INFINITY;
        if (u.gunQuiet() || !PlayField.overlaps(u.x, u.y, u.box)) {
            return;
        }
        double wanted = Enemy.heading(targetX - u.x, targetY - u.y);
        if (turretLock < 0 && u.burstLeft == 0) {
            double most = u.spec.turret().orElseThrow().turnRate() * SimStep.SECONDS;
            double delta = Math.IEEEremainder(wanted - u.aim, 2 * StrictMath.PI);
            u.aim = Math.IEEEremainder(u.aim + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
        }
        turretOff = Math.abs(Math.IEEEremainder(wanted - u.aim, 2 * StrictMath.PI));
    }

    /**
     * Its gun: its interval counts down from its last burst's start less the lock; once it is ready and the turret's
     * aim is within the turret's angle of the target, the turret locks for its lock time (the tell) and then fires
     * the burst along its aim, its shots the burst gap apart; so a turret that stays on its target fires a burst
     * every interval. Silent while the unit is off the screen or its gun is quiet (its turret part shot off). Returns
     * whether a shot goes now.
     */
    boolean trigger() {
        Enemy u = unit;
        if (u.spec.gun().isEmpty() || u.gunQuiet() || !PlayField.overlaps(u.x, u.y, u.box)) {
            turretLock = -1;
            u.burstLeft = 0;
            return false;
        }
        EnemyGun gun = u.spec.gun().get();
        if (u.volleyTicks > 0) {
            // From its last burst's start (the burst's own steps count too).
            u.volleyTicks--;
        }
        if (u.burstLeft > 0) {
            if (--u.burstTicks > 0) {
                return false;
            }
            u.burstLeft--;
            u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
            return true;
        }
        EnemySpec.Turret turret = u.spec.turret().orElseThrow();
        if (turretLock < 0) {
            if (u.volleyTicks > 0 || turretOff > turret.withinRadians()) {
                return false;
            }
            turretLock = SimStep.ticks(turret.lockSeconds());
            if (turretLock > 0) {
                return false;
            }
        } else if (--turretLock > 0) {
            return false;
        }
        turretLock = -1;
        u.volleyTicks = Math.max(1, SimStep.ticks(gun.intervalSeconds()) - SimStep.ticks(turret.lockSeconds()));
        u.burstLeft = gun.burst() - 1;
        u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
        return true;
    }

    /** Whether it has stopped turning for its burst (the tell, then the burst). */
    boolean locked() {
        return turretLock >= 0 || unit.burstLeft > 0;
    }

    /** Its state, as the unit's hash has always added it. */
    void addTo(StateHash hash) {
        hash.add(turretLock);
    }
}
