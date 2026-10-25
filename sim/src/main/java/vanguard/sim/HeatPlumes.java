package vanguard.sim;

import java.util.Arrays;

/**
 * M8 part G, the heat plumes (design/campaign Level 27, Hazards; m8g-gaps.md §1.2, §5 G5, G6): a pass over the
 * {@link FurnaceVents}' disc rows ({@link HazardSpec.VentRow#disc()}), whose timing, tell, ripple and hits on the
 * ship, Rook and the Bulwark the vents keep. While a plume's flame stands (its burn):
 *
 * <ul>
 *   <li>it burns every {@code air} or {@code low-air} unit over its disc for its {@link
 *       HazardSpec.VentRow#enemyDamage()} once per burst ({@link SimEvents.Type#PLUME_BURN}); a unit it destroys is
 *       the player's kill (bounty, chain, kill ratio). Not ground units, not a unit under the surface ({@code sub}, the
 *       Ray's shadow), not the homing torpedoes (those are bent);
 *   <li>it bends a player's shot (bolts, homing missiles, turret shots, harpoons), an enemy bullet or a homing missile
 *       whose point is inside its disc by {@link HazardSpec.VentRow#bendRadians()} away from its centre, once per
 *       projectile ({@link SimEvents.Type#PLUME_BEND}); homing ones steer on and re-acquire. Beams, the Lance, bombs,
 *       shells, mines, depth charges and the mortars' lobs are not bent.
 * </ul>
 *
 * A burst's burned units are remembered per plume (at most {@link #BURNED_MOST}; past that a plume burns no more units
 * in that burst) and forgotten once its flame is down. Made only in a level with plumes; allocation-free per step.
 */
final class HeatPlumes implements Hashed {
    /** The most units one plume's burst remembers (and so burns). */
    static final int BURNED_MOST = 16;

    private final FurnaceVents vents;
    private final SimEvents events;
    /** Per grate, the serials of the units its current burst burned. */
    private final int[] burned;
    /** Per grate, how many units its current burst burned. */
    private final int[] burnedCount;

    HeatPlumes(FurnaceVents vents, SimEvents events) {
        this.vents = vents;
        this.events = events;
        burned = new int[vents.grates() * BURNED_MOST];
        burnedCount = new int[vents.grates()];
    }

    /** Back to the level start (or a checkpoint): no burst has burned a unit. */
    void reset() {
        Arrays.fill(burnedCount, 0);
    }

    /**
     * Bends what entered a burning plume in this step: the player's {@code shots}, the enemies' {@code bullets} and
     * the homing {@code missiles}.
     */
    void bend(Pool<Shot> shots, Pool<EnemyBullet> bullets, Missiles missiles) {
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.disc(g) || !vents.present(g) || !vents.burning(g)) {
                continue;
            }
            double cx = vents.x(g);
            double cy = vents.y(g);
            double r = vents.radius(g);
            double bend = vents.bend(g);
            if (!(bend > 0)) {
                continue;
            }
            for (int i = 0; i < shots.size(); i++) {
                Shot shot = shots.get(i);
                if (shot.bendable() && inside(shot.x(), shot.y(), cx, cy, r)) {
                    shot.bend(away(shot.vx(), shot.vy(), shot.x() - cx, shot.y() - cy, bend));
                    events.add(SimEvents.Type.PLUME_BEND, shot.x(), shot.y(), g);
                }
            }
            for (int i = 0; i < bullets.size(); i++) {
                EnemyBullet bullet = bullets.get(i);
                if (!bullet.bent() && inside(bullet.x(), bullet.y(), cx, cy, r)) {
                    bullet.bend(away(bullet.vx(), bullet.vy(), bullet.x() - cx, bullet.y() - cy, bend));
                    events.add(SimEvents.Type.PLUME_BEND, bullet.x(), bullet.y(), g);
                }
            }
            for (int i = 0; i < missiles.size(); i++) {
                Missile missile = missiles.get(i);
                if (!missile.bent() && inside(missile.x(), missile.y(), cx, cy, r)) {
                    missile.bend(away(missile.vx(), missile.vy(), missile.x() - cx, missile.y() - cy, bend));
                    events.add(SimEvents.Type.PLUME_BEND, missile.x(), missile.y(), g);
                }
            }
        }
    }

    /**
     * Burns the swimmers over a burning plume, once per burst each; a unit it destroys is paid as the player's kill by
     * {@code kills} (the sortie's {@link ObjectivePasses#destroy}).
     */
    void burn(Pool<Enemy> enemies, java.util.function.IntConsumer kills) {
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.disc(g)) {
                continue;
            }
            if (!vents.present(g) || !vents.burning(g)) {
                // Its flame is down: the next burst burns anew.
                burnedCount[g] = 0;
                continue;
            }
            double damage = vents.enemyDamage(g);
            if (!(damage > 0)) {
                continue;
            }
            double cx = vents.x(g);
            double cy = vents.y(g);
            double r = vents.radius(g);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                Layer layer = enemy.layer();
                if ((layer != Layer.AIR && layer != Layer.LOW_AIR)
                        || Chain.doomed(enemy)
                        || enemy.latched()
                        || !touches(enemy.x(), enemy.y(), enemy.hitbox(), cx, cy, r)
                        || !remember(g, enemy.serial())) {
                    continue;
                }
                events.add(SimEvents.Type.PLUME_BURN, enemy.x(), enemy.y(), enemy.kind());
                if (enemy.damage(damage)) {
                    kills.accept(j);
                }
            }
        }
    }

    /** Remembers {@code serial} as burned by grate {@code g}'s burst; false if it was already (or the list is full). */
    private boolean remember(int g, int serial) {
        int base = g * BURNED_MOST;
        int count = burnedCount[g];
        for (int k = 0; k < count; k++) {
            if (burned[base + k] == serial) {
                return false;
            }
        }
        if (count >= BURNED_MOST) {
            return false;
        }
        burned[base + count] = serial;
        burnedCount[g] = count + 1;
        return true;
    }

    /** Whether (x, y) lies inside the disc of radius {@code r} round (cx, cy). */
    private static boolean inside(double x, double y, double cx, double cy, double r) {
        double dx = x - cx;
        double dy = y - cy;
        return dx * dx + dy * dy <= r * r;
    }

    /** Whether {@code box} round (x, y) touches the disc of radius {@code r} round (cx, cy). */
    private static boolean touches(double x, double y, Hitbox box, double cx, double cy, double r) {
        double nx = Math.clamp(cx, x - box.width() / 2, x + box.width() / 2) - cx;
        double ny = Math.clamp(cy, y - box.height() / 2, y + box.height() / 2) - cy;
        return nx * nx + ny * ny <= r * r;
    }

    /**
     * The turn, radians clockwise on the screen, that takes velocity (vx, vy) {@code bend} away from a disc's centre,
     * the projectile at (dx, dy) from it: toward the side of its line of flight the projectile is on (a projectile on
     * the centre line turns clockwise).
     */
    static double away(double vx, double vy, double dx, double dy, double bend) {
        double cross = vx * dy - vy * dx;
        return cross > 0 ? -bend : bend;
    }

    @Override
    public void addTo(StateHash hash) {
        for (int g = 0; g < burnedCount.length; g++) {
            hash.add(burnedCount[g]);
            for (int k = 0; k < burnedCount[g]; k++) {
                hash.add(burned[g * BURNED_MOST + k]);
            }
        }
    }
}
