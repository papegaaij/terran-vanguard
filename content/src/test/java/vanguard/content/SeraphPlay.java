package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M11 part E, step E2 (Level 47 Seraph Gauntlet): the autopilot's play among the Choir Seraphs (see {@link Autopilot};
 * design/enemies/air/choir-seraph). A mirroring Seraph sits at the field's width minus the ship's x, so the forward guns
 * line it up only at the centre: while a Seraph is on the screen (mirroring, folding, in its leaving tell or its ring
 * forming) it holds the centre line, low, every move planned among the spirals, the bodies and the rings ({@link
 * StopPlanner}); the beam held there burns the Seraph above it and a Herald at the top centre, the missiles take the
 * arrivals anywhere. The Swarm Call goes in when a Seraph shows its leaving tell (the secondary's last chance), or
 * when a pair has landed and nothing else uses it. No level before Act 7 sends a mirror-row teleporter, so nothing here
 * changes another level's flight. Allocation-free.
 */
final class SeraphPlay {
    private SeraphPlay() {}

    /** Where it holds while a Seraph is on the screen: the centre line, px from the left edge. */
    static final double CENTRE = PlayField.WIDTH / 2.0;

    /** How high it holds there, px above the bottom edge (the cruise, under the rows' 140 px). */
    static final double LOW = Autopilot.CRUISE_Y;

    /** Whether the level sends a mirroring teleporter and one is alive now (on the screen or its ring forming). */
    static boolean active(Sortie sortie) {
        return mirrorRows(sortie) && count(sortie, false) > 0;
    }

    /** Whether the level sends a mirroring teleporter (Level 47's Choir Seraph). */
    static boolean mirrorRows(Sortie sortie) {
        var kinds = sortie.enemyKinds();
        for (int i = 0; i < kinds.size(); i++) {
            EnemySpec kind = kinds.get(i);
            if (kind.teleport().isPresent()
                    && kind.shadow().isPresent()
                    && kind.teleport().get().cycle().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The Seraphs alive now: on the screen or gone while their ring forms (not those still waiting to enter); with
     * {@code landed}, only those on the screen (not gone).
     */
    static int count(Sortie sortie, boolean landed) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.hp() <= 0
                    || enemy.spec().shadow().isEmpty()
                    || enemy.spec().teleport().isEmpty()
                    || enemy.teleport().phase() == vanguard.sim.Teleporting.Phase.ENTER
                    || (landed && enemy.teleport().gone())) {
                continue;
            }
            n++;
        }
        return n;
    }

    /** Whether a Seraph shows its leaving tell now. */
    static boolean leaving(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.hp() > 0 && enemy.spec().shadow().isPresent() && enemy.leavingTell()) {
                return true;
            }
        }
        return false;
    }

    /** One step's move (the fire bit is the caller's): the Swarm Call when due, else the planned move to the centre. */
    static int move(Sortie sortie) {
        if (sortie.special().ready()
                && sortie.special().name().equals(SimSpecs.SWARM_CALL)
                && (leaving(sortie) || count(sortie, true) >= 2)) {
            return Command.SPECIAL.bit();
        }
        return StopPlanner.move(sortie, CENTRE, LOW);
    }
}
