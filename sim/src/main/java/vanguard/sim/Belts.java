package vanguard.sim;

import java.util.List;

/**
 * M7 part F, the level's conveyor belts (design/campaign Level 19, Conveyors; {@link GroundSpec.Belt}): a ground unit
 * whose centre stands on a belt rides it, moved its speed up the screen on top of the scroll and its own walk. Belts
 * never touch the ship, Rook, pickups or bullets. They are fixed to the ground, so where one runs follows from the
 * ground's scroll alone (no state). The presentation moves a wreck on a belt by {@link #speedAt} too.
 */
public final class Belts {
    private final GroundSpec.Belt[] belts;

    Belts(List<GroundSpec.Belt> levelBelts) {
        belts = levelBelts.toArray(GroundSpec.Belt[]::new);
    }

    /** Whether the level has belts. */
    public boolean any() {
        return belts.length > 0;
    }

    /**
     * The speed of the belt under the ground point at (x, y) (y up), the ground scrolled {@code groundScroll} px:
     * px/s up the screen; 0 off every belt. Where belts overlap, the first of the level's wins.
     */
    public double speedAt(double x, double y, double groundScroll) {
        for (GroundSpec.Belt belt : belts) {
            if (belt.carries(x, y, groundScroll)) {
                return belt.speed();
            }
        }
        return 0;
    }

    /** Carries the ground units standing or walking on a belt (the ground scrolled {@code groundScroll} px) one step. */
    void carry(Pool<Enemy> enemies, double groundScroll) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            // A unit the EMP Burst stunned still rides: the belt runs on under it.
            if (!enemy.rides()) {
                continue;
            }
            double speed = speedAt(enemy.x(), enemy.y(), groundScroll);
            if (speed != 0) {
                enemy.ride(speed * SimStep.SECONDS);
            }
        }
    }
}
