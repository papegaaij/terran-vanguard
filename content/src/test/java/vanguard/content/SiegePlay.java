package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * The autopilot round Level 17's domes (M7 part D, see {@link Autopilot}): a Shellback's roll kept off the domes and
 * the unit to shoot first round them.
 */
final class SiegePlay {
    private SiegePlay() {}

    /** M7 part D: px of margin round a Shellback's roll's reach of a dome's footprint. */
    private static final double ROLL_MARGIN = 8;

    /** M7 part D, round 40 R.a: the ship within this many px of a standing vent's x shoots the vent only. */
    private static final double VENT_ALIGNED = 6;

    /** M7 part D: half the width of the ship's fire column the autopilot keeps a Shellback out of, px. */
    private static final double FIRE_COLUMN = 70;

    /**
     * M7 part D (design/campaign Level 17; Varga: "when it curls up, wait"): whether a Shellback that has not rolled
     * yet stands in the ship's fire column above it while a standing dome lies within its roll's reach, so that
     * shooting it would set off a roll into the dome.
     */
    static boolean rollThreat(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!rollPending(enemy)
                    || Math.abs(enemy.renderX(1) - shipX) > FIRE_COLUMN
                    || enemy.renderY(1) < shipY
                    || enemy.renderY(1) > PlayField.HEIGHT + 40) {
                continue;
            }
            if (domeInReach(sortie, enemy) && !(enemy.partAlive() && Math.abs(enemy.partX() - shipX) < VENT_ALIGNED)) {
                // round 40 R.a: under its standing vent the shots hit the vent first, which sets off no roll
                return true;
            }
        }
        return false;
    }

    /**
     * M7 part D: whether a standing dome lies within {@code enemy}'s roll's reach if it curled now: its ball (rolling
     * down its path, here taken as straight down the screen) would touch the dome's footprint before the roll ends,
     * at its full length or (round 40 R.e) where its belly row ends it near the bottom.
     */
    private static boolean domeInReach(Sortie sortie, Enemy enemy) {
        EnemySpec.Roll roll = enemy.spec().roll().orElseThrow();
        double scroll = sortie.groundSpeed();
        double start = enemy.renderY(1) - scroll * roll.curlSeconds();
        double drop = roll.rollSeconds() * (roll.rollSpeed() + scroll);
        if (roll.bellyRow() > 0) {
            drop = Math.min(drop, Math.max(0, start - roll.bellyRow() - scroll * roll.uncurlSeconds()));
        }
        double length = drop * roll.rollSpeed() / (roll.rollSpeed() + scroll);
        for (int k = 0; k < sortie.allyCount(); k++) {
            if (!sortie.ally(k).alive()) {
                continue;
            }
            Hitbox footprint = sortie.allySpec(k).size();
            double reachX = (roll.ball().width() + footprint.width()) / 2 + ROLL_MARGIN;
            double reachY = (roll.ball().height() + footprint.height()) / 2 + ROLL_MARGIN;
            double dx = sortie.ally(k).renderX(1) - enemy.renderX(1);
            double dy = sortie.ally(k).renderY(1) - enemy.renderY(1);
            if (Math.abs(dx) < reachX && dy < reachY && dy > -(length + reachY)) {
                return true;
            }
        }
        return false;
    }

    /** M7 part D: a walker with a curl-roll still to come (above its roll's share of its HP, walking). */
    private static boolean rollPending(Enemy enemy) {
        return enemy.spec().roll().isPresent()
                && enemy.hp() > 0
                && enemy.curlPhase() == Enemy.CurlPhase.WALK
                && enemy.hp()
                        > enemy.spec().roll().get().belowShare() * enemy.spec().hp();
    }

    /** M7 part D: a hooked bomber whose dome is within this many px of its row is shot before the Herald. */
    private static final double BOMB_URGENT = 200;

    /**
     * M7 part D (design/campaign Level 17): the unit to shoot first round the domes: a hooked bomber whose dome is
     * within {@link #BOMB_URGENT} px of reaching its row, then the Choir Herald (its rings on everything near it),
     * then the hooked bomber whose dome comes nearest, then a Shellback walking or showing its belly (its vent sits
     * on its back, so shooting the body from below takes the vent first); a curled or rolling one is waited out.
     * Null when none of them is on the screen above the ship.
     */
    static Enemy siegeTarget(Sortie sortie, double shipY) {
        Enemy herald = null;
        Enemy bomber = null;
        double bomberGap = Double.POSITIVE_INFINITY;
        Enemy shellback = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (x < 8 || x > PlayField.WIDTH - 8 || y > PlayField.HEIGHT - 10 || y < shipY + 20 || enemy.hp() <= 0) {
                continue;
            }
            if (enemy.spec().aura().isPresent()) {
                herald = enemy;
            } else if (enemy.bombRun() >= 0 && !enemy.bombSpent()) {
                // when it drops: once its dome's hit box reaches its row and it has steered over the dome
                int dome = enemy.bombRun();
                double reach = sortie.ally(dome).renderY(1)
                        - sortie.allySpec(dome).hitbox().height() / 2
                        - y;
                double across = Math.abs(sortie.ally(dome).renderX(1) - x)
                        - sortie.allySpec(dome).hitbox().width() / 2;
                double gap = Math.max(
                        reach,
                        across * sortie.groundSpeed() / Math.max(1, enemy.spec().speed()));
                if (gap < bomberGap) {
                    bomberGap = gap;
                    bomber = enemy;
                }
            } else if (enemy.spec().roll().isPresent()
                    && !enemy.armoured()
                    && !(rollPending(enemy) && domeInReach(sortie, enemy) && !enemy.partAlive())
                    && (shellback == null || y < shellback.renderY(1))) {
                shellback = enemy;
            }
        }
        if (bomber != null && bomberGap < BOMB_URGENT) {
            return bomber;
        }
        if (herald != null) {
            return herald;
        }
        return bomber != null ? bomber : shellback;
    }
}
