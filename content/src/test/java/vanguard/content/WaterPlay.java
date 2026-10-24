package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part C (Level 23 Through the Ice): the autopilot's play under water (see {@link Autopilot}). The water's drag
 * slows the ship's acceleration and braking (design/world/europa, Under water rules), so a move planned with the dry
 * handling overshoots: under water it steers and keeps off the rock with its braking distance in mind (the drag at the
 * medium's blend now; the Pressure hull, which removes the drag, is not read: the estimate stays on the safe side). It
 * breaks the escapes objective's units (Level 23's siphon pumps) before they leave the screen, and sidesteps a
 * Glassfin's ram dive (design/enemies/naval/glassfin: a straight dive at where the ship was, no homing). In the air
 * (every level before Act 4) the drag is exactly 1 and nothing here changes a move.
 */
final class WaterPlay {
    private WaterPlay() {}

    /** A pump (an escapee) lower than this on the screen is shot first, px from the bottom edge. */
    static final double ESCAPEE_FIRST_BELOW = 360;

    /**
     * M8 part D (step E4): a spawning escapee of more HP than this (Level 24's spawning reefs on hard, 260;
     * design/enemies/ground/spawning-reef) is played hard: shot from its entry on ({@link #TOUGH_FIRST_BELOW}) and closed
     * in under ({@link #TOUGH_UNDER}; M8 AW: no longer kept under the stern once it passes under the ship, the Depth
     * Charges are tossed forward now). The plain play razes a reef of medium's 200 HP (easy's 150) in its window; on hard it left
     * two of the four standing (the fry soak up the harpoons from afar, the sidesteps carry the ship off its line).
     * Played hard on medium too it kept the fry on the screen longer (1.55-1.63× Level 10's small-unit time, the beam
     * era's most 1.5), so medium keeps the plain play.
     */
    static final double TOUGH_HP = 230;

    /** A tough spawning escapee is shot from this height on, px from the bottom edge: its entry. */
    static final double TOUGH_FIRST_BELOW = PlayField.HEIGHT - 10;

    /**
     * It closes in under a tough spawning escapee to this far below it, px: within the reef's 96 px it is shut (no fry
     * between the guns and the reef).
     */
    static final double TOUGH_UNDER = 60;

    /** ... but no higher than this on the screen, px. */
    static final double TOUGH_HIGHEST = 300;

    /** Whether {@code unit} is a tough spawning escapee (see {@link #TOUGH_HP}). */
    static boolean tough(Enemy unit) {
        return unit.spec().spawner().isPresent() && unit.spec().hp() > TOUGH_HP;
    }

    /**
     * The height to fly at for the escapes objective's unit {@code escapee}: close under a tough spawning one (see
     * {@link #TOUGH_HP}); else {@code cruise}.
     */
    static double escapeeY(Sortie sortie, Enemy escapee, double cruise) {
        if (!tough(escapee)) {
            return cruise;
        }
        return Math.clamp(escapee.renderY(1) - TOUGH_UNDER, 40, Math.max(cruise, TOUGH_HIGHEST));
    }

    /** How far ahead a ram dive is followed, s. */
    private static final double RAM_AHEAD = 0.7;

    /** The hull's half width a diving fry is kept off, px, with the fry's own half and a margin. */
    private static final double RAM_REACH = 34;

    /** The water's drag on the ship's acceleration and braking now: 1 in the air, the rules' factor under water. */
    static double drag(Sortie sortie) {
        double blend = sortie.waterBlend();
        if (blend == 0) {
            return 1;
        }
        return 1 + (sortie.script().medium().rules().acceleration() - 1) * blend;
    }

    /**
     * The distance the ship slides on at velocity {@code v} (px/s) after it lets go, px: v² over twice its braking
     * rate under the drag, plus a step's travel.
     */
    static double slide(Sortie sortie, double v, double drag) {
        double braking = sortie.ship().spec().speed() / sortie.ship().spec().stopSeconds() * drag;
        return v * v / (2 * braking) + Math.abs(v) * SimStep.SECONDS;
    }

    /**
     * {@link MovePlanner#steer} with the drag: under water it lets go of a direction once its slide would carry it to
     * the target, so it does not overshoot; in the air the plain steer.
     */
    static int steer(Sortie sortie, double shipX, double shipY, double targetX, double targetY) {
        int commands = MovePlanner.steer(shipX, shipY, targetX, targetY);
        double drag = drag(sortie);
        if (drag >= 1) {
            return commands;
        }
        double vx = sortie.ship().vx();
        double vy = sortie.ship().vy();
        if ((Command.LEFT.in(commands) && vx < 0 && shipX - slide(sortie, vx, drag) <= targetX)
                || (Command.RIGHT.in(commands) && vx > 0 && shipX + slide(sortie, vx, drag) >= targetX)) {
            commands &= ~(Command.LEFT.bit() | Command.RIGHT.bit());
        }
        if ((Command.DOWN.in(commands) && vy < 0 && shipY - slide(sortie, vy, drag) <= targetY)
                || (Command.UP.in(commands) && vy > 0 && shipY + slide(sortie, vy, drag) >= targetY)) {
            commands &= ~(Command.UP.bit() | Command.DOWN.bit());
        }
        return commands;
    }

    /**
     * Under water, the side of the flyable span ({@code left}, {@code right}: the canyon's, less the hull and the
     * margin) its slide would carry it past: the move toward it is dropped (it brakes) and, past it already, it
     * pushes back; {@code commands} unchanged in the air.
     */
    static int keepInSpan(Sortie sortie, int commands, double left, double right) {
        double drag = drag(sortie);
        if (drag >= 1) {
            return commands;
        }
        double shipX = sortie.ship().x();
        double vx = sortie.ship().vx();
        int horizontal = Command.LEFT.bit() | Command.RIGHT.bit();
        if (vx < 0 && shipX - slide(sortie, vx, drag) < left) {
            return (commands & ~horizontal) | (shipX < left ? Command.RIGHT.bit() : 0);
        }
        if (vx > 0 && shipX + slide(sortie, vx, drag) > right) {
            return (commands & ~horizontal) | (shipX > right ? Command.LEFT.bit() : 0);
        }
        return commands;
    }

    /**
     * The side to sidestep a ram to (design/enemies/naval/glassfin): a fry diving straight whose line passes within
     * the hull's reach of the ship within {@link #RAM_AHEAD} s; negative = left, positive = right, 0 = none.
     */
    static double ramThreat(Sortie sortie, double shipX, double shipY) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy fry = sortie.enemy(i);
            if (!fry.ramDiving()) {
                continue;
            }
            double x = fry.renderX(1);
            double y = fry.renderY(1);
            double vx = (x - fry.renderX(0)) / SimStep.SECONDS;
            double vy = (y - fry.renderY(0)) / SimStep.SECONDS;
            if (vy >= 0 || y < shipY - MovePlanner.HULL_Y) {
                continue; // not coming down at it, or past it
            }
            double t = (y - shipY) / -vy;
            if (t > RAM_AHEAD) {
                continue;
            }
            double atX = x + vx * t;
            double dx = atX - shipX;
            if (Math.abs(dx) < RAM_REACH) {
                if (shipX < 60) {
                    return 1;
                }
                if (shipX > PlayField.WIDTH - 60) {
                    return -1;
                }
                return dx >= 0 ? -1 : 1;
            }
        }
        return 0;
    }

    /**
     * The escapes objective's unit to break first (Level 23's siphon pumps): the lowest one on the screen in the lower
     * part ({@link #ESCAPEE_FIRST_BELOW}), above the ship (M8 part D: a tough spawning one from its entry, {@link
     * #TOUGH_HP}); null without one, for a hardened one ({@link
     * TargetPlay#hardenedEscapee} serves those: Level 22's rigs), out of the water or on a level with a destroy-targets
     * primary.
     */
    static Enemy escapee(Sortie sortie, double shipY) {
        String escapes = sortie.script().objectives().secondary().escapes();
        // under water only: the earlier levels' escapes (Levels 03-19) fly as they were measured
        if (escapes.isEmpty()
                || !sortie.underwater()
                || !sortie.script().objectives().targets().isEmpty()) {
            return null;
        }
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            boolean tough = tough(enemy);
            if (!enemy.spec().hardened()
                    && enemy.spec().slug().equals(escapes)
                    && y < (tough ? TOUGH_FIRST_BELOW : ESCAPEE_FIRST_BELOW)
                    && y > shipY + 20
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }
}
