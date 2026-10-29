package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M9 part E (design/campaign Level 32; design/enemies/ground/sentinel-tower): how the {@link Autopilot} takes down a
 * Sentinel Tower. A tower's hardened collar glances every non-{@code anti-ground} shot outside its 28 px emitter, and
 * a beam reaches the emitter only where its column crosses it, so the pilot lines up under the emitter (within {@link
 * #LINE_UP} px) while a live tower stands above it, before the lowest air unit. A tower in a wall's notch whose emitter
 * the beam cannot reach from the tunnel (Level 32's gallery tower, 16 px beyond the wall's line) is taken from inside
 * the notch: the pilot climbs to {@link #NOTCH_BELOW} px under the tower once the tower is on the field, steps into
 * the notch's mouth when its hull's rows are inside the notch's, and follows the notch down the screen, firing up it at
 * the emitter, until the tower is down or the notch nears the bottom edge ({@link #LOWEST}); then it steps back out of
 * the notch before it leaves the notch's rows. While it is in or entering a notch, {@link HazardPlay#canyon} keeps it
 * off the rock with the notches open ({@link #notched}).
 */
final class TowerPlay {
    /** How far off the emitter's x the ship's centre may be for the beam's column to cross it, px. */
    static final double LINE_UP = 14;

    /** Its height under a tower in the tunnel, px: low, so the tower stays above it, in the beam's line, longer. */
    static final double LOW = 45;

    /** The share of its shield it holds its line under a tower with, at least. */
    static final double HOLD_SHIELD = 0.5;

    /** How long before a tower enters the pilot waits under its x, s. */
    static final double LEAD = 1.0;

    /** In a notch: the ship's centre this far under the tower's (the notch's middle row), px. */
    static final double NOTCH_BELOW = 30;

    /** The lowest the ship follows a notch down, px from the bottom edge (y up). */
    static final double LOWEST = 60;

    /** The hull's half width and height the rock checks use, px (HazardPlay's). */
    static final double HULL_HALF = 18;

    static final double HULL_HALF_HEIGHT = 22;

    /** What the pilot keeps between its hull and the rock, px (HazardPlay's margin). */
    static final double MARGIN = 8;

    /** The rows it keeps its hull inside a notch's rows by, beyond the hull's own, px. */
    static final double NOTCH_PAD = 6;

    private TowerPlay() {}

    /** Whether the level has towers (a kind with a ground sweep behind a core): the play changes no other level. */
    static boolean towers(Sortie sortie) {
        if (sortie.terrain() == null) {
            return false;
        }
        java.util.List<vanguard.sim.EnemySpec> kinds = sortie.enemyKinds();
        for (int i = 0; i < kinds.size(); i++) {
            if (kinds.get(i).groundSweep().isPresent() && kinds.get(i).core().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** Whether {@code enemy} is a live, woken Sentinel-like tower: a ground sweep behind a hardened collar's core. */
    static boolean tower(Enemy enemy) {
        return enemy.spec().groundSweep().isPresent()
                && enemy.spec().core().isPresent()
                && !enemy.dormant()
                && enemy.hp() > 0;
    }

    /**
     * Whether it holds its line under a tower's emitter, dodging only the closest bullets (as over a battery): while a
     * tower stands above it and its shield is at least {@link #HOLD_SHIELD} full.
     */
    static boolean holding(Sortie sortie, double shipY) {
        return sortie.ship().defences().shield()
                        >= HOLD_SHIELD * sortie.ship().defences().shieldCapacity()
                && target(sortie, shipY) != null;
    }

    /**
     * Its height under a tower: {@link #LOW} while its shield is at least {@link #HOLD_SHIELD} full, else its cruise (a
     * weakened ship low at a wall is cornered).
     */
    static double low(Sortie sortie) {
        return sortie.ship().defences().shield()
                        >= HOLD_SHIELD * sortie.ship().defences().shieldCapacity()
                ? LOW
                : Autopilot.CRUISE_Y;
    }

    /** The lowest tower on the field above the ship, null for none. */
    static Enemy target(Sortie sortie, double shipY) {
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (tower(enemy)
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 30
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    /**
     * The x of the next tower to enter at the top edge within {@link #LEAD} s (the level's script: its ground units' entry
     * times), where the pilot waits for it; NaN for none.
     */
    static double next(Sortie sortie) {
        double now = sortie.levelSeconds();
        java.util.List<vanguard.sim.GroundUnit> units = sortie.script().groundUnits();
        for (int i = 0; i < units.size(); i++) {
            vanguard.sim.GroundUnit unit = units.get(i);
            if (unit.t() > now
                    && unit.t() <= now + LEAD
                    && unit.enemy().groundSweep().isPresent()) {
                return unit.x();
            }
        }
        return Double.NaN;
    }

    /**
     * The notch the tower stands in when its emitter lies out of the beam's reach from the tunnel (the tunnel's span at
     * its row less the hull and the margin), else -1.
     */
    static int notchOf(Sortie sortie, Enemy tower) {
        Terrain terrain = sortie.terrain();
        if (terrain == null || terrain.notches().isEmpty()) {
            return -1;
        }
        double row = Terrain.row(sortie.groundScroll(), tower.renderY(1));
        double x = tower.renderX(1);
        double left = terrain.leftAt(row);
        double right = terrain.rightAt(row);
        double reach = HULL_HALF + MARGIN;
        boolean outOfReach = (!Double.isNaN(right) && x - LINE_UP > right - reach)
                || (!Double.isNaN(left) && x + LINE_UP < left + reach);
        if (!outOfReach) {
            return -1;
        }
        for (int k = 0; k < terrain.notches().size(); k++) {
            Terrain.Notch notch = terrain.notches().get(k);
            if (row > notch.low() && row < notch.high()) {
                return k;
            }
        }
        return -1;
    }

    /**
     * The notch whose rows the ship's are in and whose mouth its hull reaches past the wall's line into, else -1: it
     * must leave the notch along it.
     */
    static int inNotch(Sortie sortie) {
        Terrain terrain = sortie.terrain();
        if (terrain == null || terrain.notches().isEmpty()) {
            return -1;
        }
        double x = sortie.ship().x();
        double row = Terrain.row(sortie.groundScroll(), sortie.ship().y());
        for (int k = 0; k < terrain.notches().size(); k++) {
            Terrain.Notch notch = terrain.notches().get(k);
            if (row + HULL_HALF_HEIGHT + NOTCH_PAD < notch.low() || row - HULL_HALF_HEIGHT - NOTCH_PAD > notch.high()) {
                continue;
            }
            double line = notch.side() == Terrain.Side.LEFT ? terrain.leftAt(row) : terrain.rightAt(row);
            if (Double.isNaN(line)) {
                continue;
            }
            boolean past = notch.side() == Terrain.Side.LEFT ? x - HULL_HALF < line : x + HULL_HALF > line;
            if (past) {
                return k;
            }
        }
        return -1;
    }

    /** Whether the pilot flies a notch now (into it at a tower, or out of it): {@link HazardPlay#canyon} opens them. */
    static boolean notched(Sortie sortie) {
        if (!towers(sortie) || sortie.terrain().notches().isEmpty()) {
            return false;
        }
        if (inNotch(sortie) >= 0) {
            return true;
        }
        return notchTower(sortie) != null;
    }

    /**
     * The lowest live tower on the field standing in a notch out of the beam's reach from the tunnel whose notch the ship
     * can still follow down ({@link #followable}), null for none: wherever the ship is (it climbs to it).
     */
    static Enemy notchTower(Sortie sortie) {
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (tower(enemy)
                    && y < PlayField.HEIGHT - 10
                    && followable(sortie, enemy)
                    && (lowest == null || y < lowest.renderY(1))
                    && notchOf(sortie, enemy) >= 0) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    /** Whether the ship can still follow the tower's notch down: its place under the tower above {@link #LOWEST}. */
    private static boolean followable(Sortie sortie, Enemy tower) {
        return tower.renderY(1) - NOTCH_BELOW > LOWEST;
    }

    /**
     * The commands that take the ship into a tower's notch (or out of one), {@code commands}' fire and specials kept; the
     * pilot's own {@code commands} when it flies no notch.
     */
    static int move(Sortie sortie, int commands) {
        Terrain terrain = sortie.terrain();
        if (!towers(sortie) || terrain.notches().isEmpty()) {
            return commands;
        }
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double scroll = sortie.groundScroll();
        int keep = commands & ~(Command.LEFT.bit() | Command.RIGHT.bit() | Command.UP.bit() | Command.DOWN.bit());
        Enemy tower = notchTower(sortie);
        if (tower != null) {
            double x = tower.renderX(1);
            double y = tower.renderY(1) - NOTCH_BELOW;
            return keep | MovePlanner.steer(shipX, shipY, x, y);
        }
        int in = inNotch(sortie);
        if (in >= 0) {
            // Out of the notch along it: back to the tunnel's side of the wall's line, keeping its row in the notch.
            Terrain.Notch out = terrain.notches().get(in);
            double row = Terrain.row(scroll, shipY);
            double line = out.side() == Terrain.Side.LEFT ? terrain.leftAt(row) : terrain.rightAt(row);
            double x = out.side() == Terrain.Side.LEFT ? line + HULL_HALF + MARGIN + 2 : line - HULL_HALF - MARGIN - 2;
            double middle = Terrain.y(scroll, out.scroll());
            double y = Math.clamp(
                    shipY,
                    middle - out.width() / 2 + HULL_HALF_HEIGHT + NOTCH_PAD,
                    middle + out.width() / 2 - HULL_HALF_HEIGHT - NOTCH_PAD);
            return keep | MovePlanner.steer(shipX, shipY, x, y);
        }
        return commands;
    }
}
