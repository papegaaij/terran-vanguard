package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part B, step B3 (Level 44 Crossing): the autopilot's reading of distorted space (see {@link Autopilot};
 * design/enemies, Distorted space; design/enemies/space/rift-skater). The drift carries the units and their bullets
 * sideways and the generic play's dodge reads them as they move (a bullet's step includes the drift); its line under
 * a target leads the target by the drift ({@link #LEAD}), and a landing ring lower than its target comes first ({@link
 * #targetX}): a Rift Skater gone in its jump lands on its ring's point and bursts 0.25 s later, so the beam waits on
 * the point and meets it as it lands (no ring lands in the band at the ship's edge, so none lands on the ship's cruise
 * line; Level44Test counts them). In a reversal the scroll runs
 * back and the generic play's retreat ({@link RetreatPlay}) turns the rear guns on the rear wave from the bottom edge.
 * No level before Act 7 has a current, so nothing here changes another level's flight. Allocation-free.
 */
final class DriftPlay {
    private DriftPlay() {}

    /** How far ahead it leads a drifting target's x by the current's drift, s (the ship's turn to its line). */
    static final double LEAD = 0.2;

    /**
     * Where to line up for {@code lowest} (the generic play's target, or null) in a level with a current: under the
     * point of a landing ring lower on the screen than it (a gone Rift Skater lands there: the beam is on it as it lands,
     * the first-landing-kill), else under the target led by the drift; NaN without a current or a target.
     */
    static double targetX(Sortie sortie, double shipY, Enemy lowest) {
        if (sortie.current() == null) {
            return Double.NaN;
        }
        double ringY = lowest == null ? PlayField.HEIGHT - 10 : lowest.renderY(1);
        double ringX = Double.NaN;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().teleport().isEmpty()
                    || enemy.hp() <= 0
                    || enemy.teleport().phase() != Teleporting.Phase.GONE) {
                continue;
            }
            double y = enemy.teleport().ringY();
            if (y > shipY + 40 && y < ringY) {
                ringY = y;
                ringX = enemy.teleport().ringX();
            }
        }
        if (!Double.isNaN(ringX)) {
            return ringX;
        }
        return lowest == null
                ? Double.NaN
                : lowest.renderX(1) + sortie.current().drift() * LEAD;
    }
}
