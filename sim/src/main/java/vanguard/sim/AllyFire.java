package vanguard.sim;

/**
 * M10 part C, step C6s (design/campaign Level 37, the shuttles; round 75 F.3, user decision 2026-11-01): the player's
 * fire on the air allies that take it ({@link ObjectiveSpec.Air#playerHits()}), as on a {@link EnemySpec#neutral()
 * neutral} craft. The player's shots (bolts, missiles, homing shots: the fitted mounts' and their drones') hit the first
 * unit they touch on a layer they reach, a beam stops on a unit in its column, the player's blasts (a mine's burst, a
 * splash, the Tesla's sparks) and the specials that deal damage (the Smart Bomb's ring, the Airstrike's blasts, the
 * Orbital Lance's column) hurt the units they reach; the escort's shots and the Reflex Shield's bolts pass, and the
 * seekers (homing locks, the Tesla's and the Plasma Arc's targets, Rook) never pick a unit, as they only look at
 * enemies. A unit takes the damage as from an enemy ({@link AllyPasses#playerHit}): a unit destroyed so is a lost unit
 * (its event, cues and the secondary's loss count), and pays nothing. A unit in its untouchable windows (the liftoff,
 * the climb-out) lets everything pass. Made only for such an escort; it keeps no state and allocates nothing.
 */
final class AllyFire {
    /** The struck-list and ledger key of unit 0; unit k's is this minus k, far from the enemies' and parts' keys. */
    private static final int KEY = Integer.MIN_VALUE / 2;

    private final Convoy convoy;
    private final AllyPasses allies;
    private final PlayerFire fire;
    /** The fitted mounts: a shot or blast of a mount at or past this is the escort's (or a Reflex bolt): it passes. */
    private final int mounts;

    private final Hitbox box;

    AllyFire(Convoy convoy, AllyPasses allies, PlayerFire fire, int mounts) {
        this.convoy = convoy;
        this.allies = allies;
        this.fire = fire;
        this.mounts = mounts;
        box = convoy.airHitbox();
    }

    /** The key of unit {@code k} in a shot's struck list or a special's ledger. */
    static int key(int k) {
        return KEY - k;
    }

    /** The air units. */
    int units() {
        return convoy.size();
    }

    /** Whether unit {@code k} can be hit now: alive, on its station or sliding aside, its hit box on the play field. */
    boolean hittable(int k) {
        Ally ally = convoy.get(k);
        return ally.touchable() && PlayField.overlaps(ally.x(), ally.y(), box);
    }

    double x(int k) {
        return convoy.get(k).x();
    }

    double y(int k) {
        return convoy.get(k).y();
    }

    /** The units' hit box. */
    Hitbox box() {
        return box;
    }

    /** Whether a shot or blast of {@code mount} is the player's (-1: a special's); the escort's pass. */
    boolean players(int mount) {
        return mount < mounts;
    }

    /** Unit {@code k} takes {@code damage} from the player's fire. */
    void hurt(int k, double damage) {
        allies.playerHit(k, damage);
    }

    /**
     * The player's shots that reach the air (not the landing or laid deliveries) hit the first unit they touch: it
     * takes the shot's damage and the shot is spent, or (a piercing shot) passes on, striking each unit once.
     */
    void hitShots() {
        Pool<Shot> shots = fire.shots();
        Medium medium = fire.medium();
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (!players(shot.mount())
                    || weapon.delivery().landing()
                    || weapon.delivery().laid()
                    || !medium.reaches(weapon, Layer.AIR)) {
                continue;
            }
            for (int k = 0; k < units(); k++) {
                if (!hittable(k)
                        || !weapon.size().overlaps(shot.x(), shot.y(), box, x(k), y(k))
                        || (weapon.pierce() > 1 && shot.struck(key(k)))) {
                    continue;
                }
                boolean spent = shot.pierced();
                hurt(k, shot.damage());
                if (spent) {
                    shots.free(i);
                    break;
                }
            }
        }
    }

    /**
     * A blast of the player's (a mine's burst, a splash, the Tesla's sparks) of {@code radius} around (x, y) from
     * {@code mount}: every unit it reaches takes {@code damage}.
     */
    void blast(double x, double y, double radius, double damage, int mount) {
        if (!players(mount)) {
            return;
        }
        for (int k = 0; k < units(); k++) {
            if (hittable(k) && ShotGeometry.inBlast(x, y, radius, x(k), y(k), box)) {
                hurt(k, damage);
            }
        }
    }

    /**
     * Where a column centred on {@code cx}, {@code half} either side, running up from {@code fromY}, first touches unit
     * {@code k} ({@link ShotGeometry#columnContact}); {@link Double#POSITIVE_INFINITY} when it misses it or the unit
     * cannot be hit.
     */
    double column(int k, double cx, double half, double fromY) {
        return hittable(k) ? ShotGeometry.columnContact(cx, half, fromY, x(k), y(k), box) : Double.POSITIVE_INFINITY;
    }
}
