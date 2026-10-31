package vanguard.sim;

/**
 * M10 part A (design/enemies, The three-way war): scripted prey, generalised from Level 33's. A wave that hunts
 * ({@link WaveSpec#hunts()}: the tag of a prey wave) sends each of its units at the nearest unit of that prey wave on
 * the play field while there is one ({@link #target}), else at the ship: its chase, dive or ram steer at the prey and
 * its aimed volleys go at it ({@link EnemyMoves}). A hunter's bullets stay ordinary enemy bullets that also hit the prey
 * wave's units at their damage and pass every other unit ({@link #bullets}); a rammer touching a prey unit does its
 * contact damage to it and dies on the impact ({@link #rams}); a latching hunter latches on a prey unit as Level 33's
 * leeches do ({@link PreyLatches}). A prey unit the war kills and a hunter dead on its prey are no player kills: they
 * pay nothing ({@link EnemyForce.Escapes#lostToWar}). Every hit reports {@link SimEvents.Type#PREY_HIT} (the teal
 * flash). Owned by an {@link EnemyForce} only in a level with a hunting wave; its state lives in the units (their
 * {@link Enemy#hunts()}, hashed there). Allocates nothing.
 */
final class PreyWar {
    private final Pool<Enemy> enemies;
    private final Pool<EnemyBullet> bullets;
    private final SimEvents events;
    private final EnemyForce.Escapes escapes;
    /**
     * M10 part A, step A1b: whether the hunting links are live now (from the level's turn, {@link LevelScript#turn()};
     * always without one). Follows the level clock (not hashed).
     */
    private boolean live = true;

    PreyWar(Pool<Enemy> enemies, Pool<EnemyBullet> bullets, SimEvents events, EnemyForce.Escapes escapes) {
        this.enemies = enemies;
        this.bullets = bullets;
        this.events = events;
        this.escapes = escapes;
    }

    /**
     * The prey {@code hunter} goes for: the nearest unit of its prey wave on the play field that no leech rides (a tie
     * to the earlier in the pool); null for a unit that hunts nothing or when none is left (it goes for the ship).
     */
    Enemy target(Enemy hunter) {
        if (hunter.hunts.isEmpty() || !live) {
            return null;
        }
        return target(hunter.hunts, hunter, hunter.x(), hunter.y());
    }

    /** M10 part A, step A1b: whether the hunting links are live now (the level's turn has come, or it has none). */
    void live(boolean turned) {
        live = turned;
    }

    /** M10 part A, step A1b: whether the hunting links are live now. */
    boolean live() {
        return live;
    }

    /**
     * M10 part A, step A1b (a Mote Swarm's lunge, The three-way war's "ram"): the prey a pursuit flock whose wave hunts
     * {@code hunts} goes for: the nearest unit of that prey wave on the play field to its leader point (x, y) that no
     * leech rides; null while the links are not live or none is left (it goes for the ship).
     */
    Enemy target(String hunts, double x, double y) {
        if (hunts.isEmpty() || !live) {
            return null;
        }
        return target(hunts, null, x, y);
    }

    private Enemy target(String hunts, Enemy hunter, double x, double y) {
        Enemy best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy prey = enemies.get(i);
            if (prey == hunter || !hunts.equals(prey.tag()) || !ShotGeometry.onField(prey) || prey.preyed()) {
                continue;
            }
            double dx = prey.x() - x;
            double dy = prey.y() - y;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = prey;
            }
        }
        return best;
    }

    /**
     * After the units' moves: every hunting rammer (a unit a ram destroys) touching a unit of its prey wave does its
     * contact damage to it and dies on the impact; a prey unit it kills is lost to the war too.
     */
    void rams() {
        if (!live) {
            return;
        }
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (i >= enemies.size()) {
                continue;
            }
            Enemy hunter = enemies.get(i);
            EnemySpec spec = hunter.spec();
            if (hunter.hunts.isEmpty()
                    || !spec.destroyedByRamming()
                    || spec.latch().isPresent()
                    || hunter.empStunned()
                    || !hunter.bodyTouches()
                    || !ShotGeometry.onField(hunter)) {
                continue;
            }
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy prey = enemies.get(j);
                if (!preyOf(hunter, prey)
                        || !prey.hitbox().overlaps(prey.x(), prey.y(), hunter.hitbox(), hunter.x(), hunter.y())) {
                    continue;
                }
                events.add(SimEvents.Type.PREY_HIT, hunter.x(), hunter.y(), prey.kind());
                if (prey.damage(spec.contactDamage())) {
                    lose(j);
                }
                // The rammer dies on the impact (it may have moved in the pool).
                lose(indexOf(hunter));
                break;
            }
        }
    }

    /**
     * After the bullets moved: a hunter's bullet touching a unit of its prey wave on the play field hits it at its
     * damage and is spent; a prey unit it kills is lost to the war.
     */
    void bullets() {
        for (int b = bullets.size() - 1; b >= 0; b--) {
            EnemyBullet bullet = bullets.get(b);
            String tag = bullet.preyTag();
            if (tag == null) {
                continue;
            }
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy prey = enemies.get(j);
                if (!tag.equals(prey.tag())
                        || !ShotGeometry.onField(prey)
                        || !prey.hitbox().overlaps(prey.x(), prey.y(), EnemyGun.BULLET, bullet.x(), bullet.y())) {
                    continue;
                }
                events.add(SimEvents.Type.PREY_HIT, bullet.x(), bullet.y(), prey.kind());
                bullets.free(b);
                if (prey.damage(bullet.damage())) {
                    lose(j);
                }
                break;
            }
        }
    }

    /** Whether {@code unit} belongs to {@code hunter}'s prey wave and is on the play field (never the hunter itself). */
    private static boolean preyOf(Enemy hunter, Enemy unit) {
        return unit != hunter
                && !hunter.hunts.isEmpty()
                && hunter.hunts.equals(unit.tag())
                && ShotGeometry.onField(unit);
    }

    /** The unit at {@code index} is lost to the war: its outcome, then out of the pool. */
    private void lose(int index) {
        if (index < 0) {
            return;
        }
        escapes.lostToWar(enemies.get(index));
        enemies.free(index);
    }

    private int indexOf(Enemy unit) {
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i) == unit) {
                return i;
            }
        }
        return -1;
    }
}
