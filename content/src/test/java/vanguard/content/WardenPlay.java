package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M11 part B, step B3 (Level 43 The Tether Gate): the autopilot's play among the Gate Wardens (see {@link Autopilot};
 * design/enemies/space/gate-warden, design/enemies Window shields). While a Warden is on the play field it focuses one
 * ({@link #focus}: the one in its leaving tell first, the secondary's; else the one at its station longest) and climbs
 * to hold the Tesla Coil Pod's arc on it: within the arc's reach below it, where its closed shell is the arc's pick
 * when nothing else is nearer, so a breaker hit opens it for its hold and the Ion Beam and the missiles go in; it times
 * a window too (the shell opens with its line's telegraph: under it in the telegraph, off its band before it goes
 * live). Every move is planned ({@link StopPlanner}): the look-ahead off the bullets, the bodies and a running line
 * (an aimed line is locked on the ship: it stands off it in the telegraph), the edges kept. The EMP Burst goes in when
 * the focused Warden shows its leaving tell with its shell closed (it opens every shell 3 s and stuns them 1.5 s). No
 * level before Act 7 sends a window-shielded unit, so nothing here changes another level's flight. Allocation-free.
 */
final class WardenPlay {
    private WardenPlay() {}

    /** How far below the focused Warden it holds, px: inside the Tesla Coil Pod's reach at L4 (170) with a margin. */
    static final double BELOW = 100;
    /** The lowest it holds at, px (the cruise). */
    static final double LOWEST = 110;

    /** Whether the level sends a window-shielded unit and one is on the play field now. */
    static boolean active(Sortie sortie) {
        return windowed(sortie) && focus(sortie) != null;
    }

    /** Whether the level sends a unit with a window shield (Level 43's Gate Warden). */
    static boolean windowed(Sortie sortie) {
        var kinds = sortie.enemyKinds();
        for (int i = 0; i < kinds.size(); i++) {
            EnemySpec kind = kinds.get(i);
            if (kind.windowShield().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The Warden it plays: of the window-shielded units alive on the play field, one in its leaving tell first (the
     * secondary), else the one with the least HP left (the one the arc and the beam have opened up), else the lowest.
     */
    static Enemy focus(Sortie sortie) {
        Enemy best = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().windowShield().isEmpty()
                    || enemy.hp() <= 0
                    || !LevelMetrics.onScreen(
                            enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                continue;
            }
            if (best == null || better(enemy, best)) {
                best = enemy;
            }
        }
        return best;
    }

    private static boolean better(Enemy a, Enemy b) {
        if (a.leavingTell() != b.leavingTell()) {
            return a.leavingTell();
        }
        if (a.hp() != b.hp()) {
            return a.hp() < b.hp();
        }
        return a.renderY(1) < b.renderY(1);
    }

    /** One step's move (the fire bit is the caller's): the EMP for a leaving Warden, else the planned move. */
    static int move(Sortie sortie) {
        Enemy warden = focus(sortie);
        if (warden.leavingTell() && warden.window().closed() && LeechPlay.emp(sortie)) {
            return Command.SPECIAL.bit();
        }
        double targetX = warden.renderX(1);
        double targetY = Math.max(LOWEST, Math.min(PlayField.HEIGHT - 60, warden.renderY(1) - BELOW));
        return StopPlanner.move(sortie, targetX, targetY);
    }
}
