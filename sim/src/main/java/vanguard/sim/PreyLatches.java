package vanguard.sim;

/**
 * M9 part F, the uneasy alliance (design/enemies/space/void-leech; design/campaign Level 33): chasers whose latch takes
 * prey ({@link EnemySpec.Latch#hunts()}) latch on a touched free {@link EnemySpec.PreyRole#PREY} unit instead of the
 * ship, and a {@link EnemySpec.PreyRole#HUNTER} drifts and chases the nearest free prey on the play field. A preyed
 * unit's shield is emptied for good, it no longer fires and it leaves its path to drift down at the latch's prey drift
 * until it is gone ({@link Enemy#preyed()}); its leech rides it at a latch spot ({@link Enemy.LatchPhase#PREY}), hit by
 * any shot, and never moves on to the ship. Carried off the screen it counts as away; its prey destroyed under it, it is
 * thrown off (stunned, then it flees). Owned by an {@link EnemyForce} only in a level with a prey unit; its state lives
 * in the units (hashed there). Allocates nothing.
 */
final class PreyLatches {
    /**
     * M10 part A (design/enemies, The three-way war): the drift down of a prey unit a hunting wave's leech latched on,
     * px/s (a latch without its own prey drift).
     */
    static final double WAR_DRIFT = 60;

    private final Pool<Enemy> enemies;
    private final SimEvents events;

    PreyLatches(Pool<Enemy> enemies, SimEvents events) {
        this.enemies = enemies;
        this.events = events;
    }

    /**
     * The prey {@code hunter} steers at: the nearest free prey unit on the play field (a tie to the earlier in the pool);
     * null when there is none (it hunts the ship).
     */
    Enemy target(Enemy hunter) {
        Enemy best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy prey = enemies.get(i);
            if (!free(hunter, prey)) {
                continue;
            }
            double dx = prey.x() - hunter.x();
            double dy = prey.y() - hunter.y();
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = prey;
            }
        }
        return best;
    }

    /**
     * One step after the units' moves: every rider placed on its prey (or carried off with it, or thrown off a
     * destroyed one), then every hunting chaser touching a free prey latches on it.
     */
    void step() {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy rider = enemies.get(i);
            int serial = rider.preySerial();
            if (serial < 0) {
                continue;
            }
            Enemy prey = bySerial(serial);
            if (prey == null || !prey.preyed()) {
                rider.chasing.preyGone();
                continue;
            }
            double nextY = prey.y() - prey.preyDrift * SimStep.SECONDS;
            rider.chasing.ride(prey.x(), prey.y(), nextY + prey.box.height() / 2 <= 0);
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy leech = enemies.get(i);
            EnemySpec spec = leech.spec();
            // M10 part A (The three-way war): a leech of a hunting wave latches on its prey wave's units.
            if (spec.latch().isEmpty()
                    || (!spec.latch().get().hunts() && leech.hunts.isEmpty())
                    || !leech.latchable()
                    || leech.empStunned()) {
                continue;
            }
            for (int j = 0; j < enemies.size(); j++) {
                Enemy prey = enemies.get(j);
                if (free(leech, prey)
                        && prey.hitbox().overlaps(prey.x(), prey.y(), leech.hitbox(), leech.x(), leech.y())) {
                    latch(leech, prey);
                    break;
                }
            }
        }
    }

    /** {@code leech} latches on {@code prey}: at the spot it touched from, the prey's shield emptied for good. */
    private void latch(Enemy leech, Enemy prey) {
        EnemySpec.Latch latch = leech.spec().latch().orElseThrow();
        int spot = EnemySpec.Latch.spotFor(leech.x() - prey.x(), leech.y() - prey.y());
        // M10 part A (The three-way war): a hunting wave's leech without a prey drift of its own: the war's.
        prey.preyDrift = latch.hunts() ? latch.preyDrift() : WAR_DRIFT;
        if (prey.hexShield.fitted() && prey.spec().shield().isPresent()) {
            prey.hexShield.empty(
                    prey.spec().shield().get(), Integer.MAX_VALUE, events, prey.x(), prey.y(), prey.kind());
        }
        leech.chasing.latchPrey(prey, spot);
        events.add(SimEvents.Type.PREY_LATCHED, leech.x(), leech.y(), spot);
        events.add(SimEvents.Type.PREY_DRIFT, prey.x(), prey.y(), prey.kind());
    }

    /**
     * Whether {@code unit} is {@code hunter}'s prey on the play field that no chaser rides yet: a {@link
     * EnemySpec.PreyRole#PREY} unit (Level 33), or (M10 part A) for a unit of a hunting wave a unit of its prey wave.
     */
    private static boolean free(Enemy hunter, Enemy unit) {
        boolean prey = hunter.hunts.isEmpty()
                ? unit.spec().preyRole() == EnemySpec.PreyRole.PREY
                : unit != hunter && hunter.hunts.equals(unit.tag());
        return prey && !unit.preyed() && PlayField.overlaps(unit.x(), unit.y(), unit.hitbox());
    }

    private Enemy bySerial(int serial) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.serial() == serial) {
                return enemy;
            }
        }
        return null;
    }
}
