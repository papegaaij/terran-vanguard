package vanguard.sim;

/**
 * M9 part E, the mining charges' bursts (design/world/belt, Mining charges; design/campaign Level 32): a destroyed
 * charge ({@link GroundObjectSpec#blast()}) queues its burst, and the sortie lets the queued bursts off once a step,
 * after the hit passes (so a charge shot inside a pass over the enemies does not change that pass's list): each deals
 * its damage to every enemy (air and ground; hardened ones, shields and unit parts as any blast) whose hit box lies in
 * its radius, the kills the player's. It never hurts the ship, Rook or a ground object (a charge sets off no other
 * charge). Only a level with charges has one. Allocates nothing after it is made.
 */
final class Charges {
    /** The most bursts queued in one step. */
    static final int MOST_QUEUED = 16;

    private final double[] xs = new double[MOST_QUEUED];
    private final double[] ys = new double[MOST_QUEUED];
    private final double[] radii = new double[MOST_QUEUED];
    private final double[] damages = new double[MOST_QUEUED];
    private int queued;

    /** Back to the level start: nothing queued. */
    void reset() {
        queued = 0;
    }

    /** A charge at (x, y) was destroyed: its burst goes off with the step's others (one past the most is lost). */
    void queue(double x, double y, GroundObjectSpec.Blast blast) {
        if (queued == MOST_QUEUED) {
            return;
        }
        xs[queued] = x;
        ys[queued] = y;
        radii[queued] = blast.radius();
        damages[queued] = blast.damage();
        queued++;
    }

    /** The queued bursts go off on {@code enemies}; the kills and lost parts are paid through {@code hits}. */
    void burst(Pool<Enemy> enemies, SimEvents events, PlayerFire.Hits hits) {
        for (int k = 0; k < queued; k++) {
            double x = xs[k];
            double y = ys[k];
            double radius = radii[k];
            double damage = damages[k];
            int destroyed = 0;
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                Layer layer = enemy.layer();
                if (layer == Layer.HIGH_AIR
                        || layer == Layer.SUB
                        || !layer.touchable()
                        // M10 part B: an uncounted hostile craft (a harvester) is hit; a neutral one is not.
                        || enemy.spec().neutral()
                        || !ShotGeometry.onField(enemy)
                        || !ShotGeometry.inBlast(x, y, radius, enemy.x(), enemy.y(), enemy.hitbox())) {
                    continue;
                }
                for (int p = 0; p < enemy.partCount(); p++) {
                    if (enemy.partInBlast(p, x, y, radius) && enemy.damagePart(p, damage, true)) {
                        hits.unitPartDestroyed(j, p);
                    }
                }
                if (enemy.damage(damage, true, true)) {
                    hits.enemyDestroyed(j);
                    destroyed++;
                }
            }
            events.add(SimEvents.Type.CHARGE_BLAST, x, y, destroyed);
        }
        queued = 0;
    }
}
