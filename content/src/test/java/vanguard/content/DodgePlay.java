package vanguard.content;

import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Layer;
import vanguard.sim.Lob;
import vanguard.sim.Mine;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * The autopilot's dodging (see {@link Autopilot}): bullets, rammers, mortar markers, a lit rail, rising spores and
 * large debris, a segment chain close by, and the side to slip to.
 */
final class DodgePlay {
    private DodgePlay() {}

    private static final double DANGER = 70;

    /** How far it keeps from a lit rail (Level 05's sleds), px. */
    private static final double RAIL_CLEARANCE = 44;

    /** How close a chain member may come before it slips aside (Level 06), px. */
    private static final double CHAIN_DX = 52;

    private static final double CHAIN_ABOVE = 120;
    private static final double CHAIN_BELOW = 70;

    /**
     * Level 06: with a member of a segment chain close above, beside or below, the side whose
     * position 50 px away lies farther from every member (negative = left); 0 when none is close.
     */
    static double chainThreat(Sortie sortie, double shipX, double shipY) {
        boolean close = false;
        for (int c = 0; c < sortie.chainCount() && !close; c++) {
            var chain = sortie.chain(c);
            if (chain.ground()) {
                // M7 part C: a crawling chain (the Threadcrawler) is on the ground and cannot ram.
                continue;
            }
            for (int k = 0; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                if (member != null
                        && Math.abs(member.renderX(1) - shipX) < CHAIN_DX
                        && member.renderY(1) - shipY > -CHAIN_BELOW
                        && member.renderY(1) - shipY < CHAIN_ABOVE) {
                    close = true;
                    break;
                }
            }
        }
        if (!close) {
            return 0;
        }
        if (shipX < 60) {
            return 1;
        }
        if (shipX > PlayField.WIDTH - 60) {
            return -1;
        }
        return chainClearance(sortie, shipX - 50, shipY) > chainClearance(sortie, shipX + 50, shipY) ? -1 : 1;
    }

    /** The distance from (x, y) to the nearest chain member, the vertical counted at 0.7. */
    private static double chainClearance(Sortie sortie, double x, double y) {
        double nearest = Double.POSITIVE_INFINITY;
        for (int c = 0; c < sortie.chainCount(); c++) {
            var chain = sortie.chain(c);
            for (int k = 0; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                if (member != null) {
                    nearest = Math.min(nearest, Math.hypot(member.renderX(1) - x, 0.7 * (member.renderY(1) - y)));
                }
            }
        }
        return nearest;
    }

    /** Level 05: while the rail is lit (lights or a sled), a target beside the rail rather than on it. */
    static double clearOfRail(Sortie sortie, double targetX) {
        if (sortie.sled().isEmpty() || !sortie.sled().get().lit()) {
            return targetX;
        }
        double rail = sortie.sled().get().spec().x();
        return Math.abs(targetX - rail) < RAIL_CLEARANCE ? rail - RAIL_CLEARANCE : targetX;
    }

    /**
     * Level 05: the side to slip to out of a mortar's marker, sideways only and toward the battery
     * it is shooting at if there is one (it keeps its line); 0 when no marker lies under it.
     */
    static double lobThreat(Sortie sortie, double shipX, double shipY) {
        for (int i = 0; i < sortie.lobCount(); i++) {
            Lob lob = sortie.lob(i);
            double dx = lob.targetX() - shipX;
            double dy = lob.targetY() - shipY;
            double reach = lob.impactRadius() + 30;
            if (dx * dx + dy * dy < reach * reach) {
                Enemy battery = TargetPlay.battery(sortie, shipY);
                if (battery != null && Math.abs(battery.renderX(1) - lob.targetX()) > reach) {
                    return battery.renderX(1) < shipX ? -1 : 1;
                }
                return away(dx, shipX);
            }
        }
        return 0;
    }

    /**
     * The side to dodge a lit rail, a rising spore or large debris to: negative = left, positive =
     * right, 0 = nothing close.
     */
    static double hazard(Sortie sortie, double shipX, double shipY) {
        if (sortie.sled().isPresent() && sortie.sled().get().lit()) {
            double dx = sortie.sled().get().spec().x() - shipX;
            if (Math.abs(dx) < RAIL_CLEARANCE) {
                return dx >= 0 ? -1 : 1;
            }
        }
        for (int i = 0; i < sortie.mineCount(); i++) {
            Mine mine = sortie.mine(i);
            double dx = mine.renderX(1) - shipX;
            double dy = mine.renderY(1) - shipY;
            if (mine.rising() > 0.5 && Math.abs(dx) < 26 && dy > -16 && dy < DANGER) {
                return away(dx, shipX);
            }
        }
        for (int i = 0; i < sortie.debrisCount(); i++) {
            Debris chunk = sortie.debris(i);
            double dx = chunk.renderX(1) - shipX;
            double dy = chunk.renderY(1) - shipY;
            double halfW = chunk.spec().size().width() / 2;
            double halfH = chunk.spec().size().height() / 2;
            if ((chunk.large() || chunk.thrown())
                    && Math.abs(dx) < halfW + 30
                    && dy > -halfH - 20
                    && dy < halfH + DANGER) {
                return away(dx, shipX);
            }
        }
        return 0;
    }

    /** The side to dodge bullets and rammers to: negative = left, positive = right, 0 = nothing close. */
    static double threat(Sortie sortie, double shipX, double shipY) {
        // Level 05: with a battery unit on the screen it holds its line and dodges only the closest bullets.
        boolean holding = TargetPlay.battery(sortie, shipY) != null || TargetPlay.hardenedTarget(sortie) != null;
        double bulletDx = holding ? 18 : 24;
        double bulletDy = holding ? 40 : DANGER;
        boolean fromBelow = sortie.airEscort();
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            double dx = bullet.renderX(1) - shipX;
            double dy = bullet.renderY(1) - shipY;
            if (Math.abs(dx) < bulletDx && dy > -10 && dy < bulletDy) {
                return away(dx, shipX);
            }
            // Level 10: a Wraith's burst comes up the screen from below.
            if (fromBelow
                    && Math.abs(dx) < bulletDx
                    && dy <= -10
                    && dy > -DANGER
                    && bullet.renderY(1) > bullet.renderY(0)) {
                return away(dx, shipX);
            }
        }
        // Level 05: the batteries' units lie on the ground and cannot ram, so it flies over them; M5 part G:
        // so do Level 13's boats and walkers round the relay.
        boolean overGround = !sortie.script().objectives().targets().isEmpty() || sortie.defending();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            // M7 part B: a buried Burrower touches nothing; it is not dodged.
            if ((overGround && enemy.layer() == Layer.GROUND)
                    || !enemy.layer().touchable()
                    || enemy.cloaked()
                    || TargetPlay.holding(enemy)) {
                continue;
            }
            double dx = enemy.renderX(1) - shipX;
            double dy = enemy.renderY(1) - shipY;
            // Level 10: a wide body (a Wraith on its way out) is dodged by its width.
            double reach = fromBelow ? Math.max(30, enemy.hitbox().width() / 2 + MovePlanner.HULL_X + 4) : 30;
            if (Math.abs(dx) < reach && Math.abs(dy) < DANGER) {
                if (enemy.ambushPhase() == Enemy.AmbushPhase.EXIT) {
                    // it leaves for the side lane nearer its hold point: toward the middle, out of its way
                    return enemy.renderX(1) <= PlayField.WIDTH / 2.0 ? 1 : -1;
                }
                return away(dx, shipX);
            }
        }
        return 0;
    }

    /** Level 10: whether a rammer or a rising bullet is close below the ship (then it dodges sideways only). */
    static boolean fromBelow(Sortie sortie, double shipX, double shipY) {
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            double dy = bullet.renderY(1) - shipY;
            if (Math.abs(bullet.renderX(1) - shipX) < 30 && dy < 0 && dy > -DANGER) {
                return true;
            }
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double dy = enemy.renderY(1) - shipY;
            if (!enemy.cloaked()
                    && enemy.layer().touchable()
                    && Math.abs(enemy.renderX(1) - shipX) < 40
                    && dy < 0
                    && dy > -DANGER - 30) {
                return true;
            }
        }
        return false;
    }

    private static double away(double dx, double shipX) {
        if (shipX < 60) {
            return 1;
        }
        if (shipX > PlayField.WIDTH - 60) {
            return -1;
        }
        return dx >= 0 ? -1 : 1;
    }
}
