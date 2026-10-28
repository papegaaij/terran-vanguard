package vanguard.sim;

import java.util.List;

/**
 * M7 part E (design/enemies/ground/dust-devil, Pull; out of {@link EnemyForce}): the pull of the units with a pull on
 * the ship in this step. Exactly 0 without a unit with a pull in the level.
 */
final class EnemyPull {
    private final Pool<Enemy> enemies;
    /** Whether a kind of the level has a pull (the ship and the pickups are pulled only then). */
    private final boolean pulls;
    /** The pull on the ship in this step, px/s (y up); 0 without one. */
    private double pullX;

    private double pullY;

    EnemyPull(List<EnemySpec> kinds, Pool<Enemy> enemies) {
        this.enemies = enemies;
        pulls = kinds.stream().anyMatch(kind -> kind.pull().isPresent());
    }

    /** Whether a kind of the level has a pull. */
    boolean pulls() {
        return pulls;
    }

    /** See {@link EnemyForce#pull}. */
    void pull(double shipX, double shipY) {
        pullX = 0;
        pullY = 0;
        if (!pulls) {
            return;
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.spec().pull().isEmpty()
                    || enemy.empStunned()
                    || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            double dx = enemy.x() - shipX;
            double dy = enemy.y() - shipY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed = enemy.spec().pull().get().shipSpeedAt(distance);
            if (speed > 0 && distance > 0) {
                pullX += dx / distance * speed;
                pullY += dy / distance * speed;
            }
        }
    }

    /** No pull on the ship in this step (it does not fly). */
    void none() {
        pullX = 0;
        pullY = 0;
    }

    double x() {
        return pullX;
    }

    double y() {
        return pullY;
    }
}
