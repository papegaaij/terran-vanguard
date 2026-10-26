package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.FurnaceVents;
import vanguard.sim.GroundObject;
import vanguard.sim.Layer;
import vanguard.sim.Missile;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;

/**
 * M8 part G (Level 27 Hydrothermal Vents): the autopilot's play in a level with heat plumes (see {@link Autopilot}). The
 * vent field crowds three things that each want their own move: the plumes' discs (a tell, then a burn that hits the
 * hull; design/campaign/act-4-deep-water/level-27-hydrothermal-vents, Hazards), the Depth Hunters' torpedoes rising and
 * homing from below (design/enemies/naval/depth-hunter), and the Barbfins holding above it. So every move is planned
 * ({@link #move}), as {@link DarkPlay} does in the dark: of the eight ways and standing still, the one whose look-ahead
 * keeps the hull off the bullets, the torpedoes (each flown on along its last step) and the bodies (not the ground's,
 * which cannot ram), out of a plume's disc in its tell or burn (the disc scrolling with the ground; the earlier inside,
 * the dearer), and nearest its line. Its line: a relay beacon (the secondary's escapes unit) on the screen first, under
 * it with the harpoon or, once it has scrolled past, over it with the rear gun (the plan's Tail Gun, the user's Q.1 = a
 * of round 57); else the lowest unit above it (the Barbfins' hold, a Hunter column holding above it); else the nearest
 * unit below it, for the rear gun; else a pickup or the ground. While a Hunter is on the screen it cruises low, under
 * the column's hold band, so the harpoon reaches the Hunters and their torpedoes, launched up along their facing, have
 * to turn round to reach it; the rear gun shoots those that do. With the Decoy Flares fitted and ready it launches them
 * when a torpedo closes in. No level before it has a plume, so nothing here changes another level's flight.
 * Allocation-free.
 */
final class VentPlay {
    private VentPlay() {}

    /** How far ahead each move is followed, in samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 14;

    static final double SAMPLE = 0.05;
    /** The cost of the hull meeting a bullet, a torpedo or a body in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet, a torpedo or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The cost of each sample with the hull in a burning disc (or one whose tell is past {@link #TELL_HOT}). */
    static final double BURN = 300;
    /** The cost of each sample with the hull in a disc early in its tell: leave it while it is cheap. */
    static final double TELL = 40;
    /** A tell this far through counts as burning in the look-ahead (the tell lasts 1.0–1.8 s by difficulty). */
    static final double TELL_HOT = 0.4;
    /** The margin kept round a hot disc, px. */
    static final double DISC_MARGIN = 6;
    /** Its cruise height, px. */
    static final double CRUISE_Y = 100;
    /** Its cruise height while a Depth Hunter is on the screen: under the column's hold band (80–160 px), px. */
    static final double UNDER_HUNTERS_Y = 45;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.5;

    static final double ALONG_Y = 0.1;
    /** The cost of each px off a beacon's line: it pushes harder for the secondary's targets. */
    static final double BEACON_X = 1.5;
    /** M8 part H: the gap it keeps beside a unit rising at it, px. */
    static final double BESIDE = 10;
    /** It keeps off the side edges, px. */
    static final double EDGE = 40;
    /** A torpedo this close launches the Decoy Flares (if fitted and ready), px. */
    static final double FLARE_RANGE = 140;

    /** Whether the level has heat plumes: a vent row whose flame is a disc (Level 27). */
    static boolean plumes(Sortie sortie) {
        FurnaceVents vents = sortie.vents();
        for (int g = 0; g < vents.grates(); g++) {
            if (vents.disc(g)) {
                return true;
            }
        }
        return false;
    }

    /**
     * M8 part H: whether the level has Depth Hunters without heat plumes (Level 28's approach): its waves hold a unit
     * whose gun launches homing torpedoes. The approach is flown as the vent field is, without the discs.
     */
    static boolean hunted(Sortie sortie) {
        var waves = sortie.script().waves();
        for (int i = 0; i < waves.size(); i++) {
            var gun = waves.get(i).enemy().gun();
            if (gun.isPresent() && gun.get().homing().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** One step's move and, when due, the special (the fire bit is the caller's). */
    static int move(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = shipX;
        boolean hunters = false;
        Enemy beacon = null;
        Enemy above = null;
        Enemy below = null;
        String escapes = sortie.script().objectives().secondary().escapes();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (x < 0 || x > PlayField.WIDTH || y > PlayField.HEIGHT - 10 || y < 10) {
                continue;
            }
            if (!escapes.isEmpty() && enemy.spec().slug().equals(escapes)) {
                if (beacon == null || y < beacon.renderY(1)) {
                    beacon = enemy;
                }
                continue;
            }
            if (enemy.cloaked() || !enemy.layer().touchable() || enemy.layer() == Layer.GROUND) {
                continue;
            }
            if (hunter(enemy)) {
                hunters = true;
            }
            if (y > shipY + 30) {
                if (above == null || y < above.renderY(1)) {
                    above = enemy;
                }
            } else if (y < shipY - 20 && (below == null || Math.abs(x - shipX) < Math.abs(below.renderX(1) - shipX))) {
                below = enemy;
            }
        }
        double targetY = hunters ? UNDER_HUNTERS_Y : CRUISE_Y;
        Pickup pickup = sortie.pickupCount() > 0 ? sortie.pickup(0) : null;
        boolean plumes = plumes(sortie);
        if (beacon != null) {
            targetX = beacon.renderX(1);
        } else if (pickup != null && (above == null || pickup.renderY(1) < 300)) {
            targetX = pickup.renderX();
            targetY = Math.max(40, pickup.renderY(1) - 10);
        } else if (above != null) {
            targetX = above.renderX(1);
        } else if (below != null) {
            targetX = below.renderX(1);
            if (!plumes && below.renderY(1) > below.renderY(0)) {
                // M8 part H: without plumes (Level 28's approach) beside a unit rising at it (a rear ambush), not on
                // its
                // lane: lined up on it, the planner outran it upward to the top edge, the ambush stopped 90-200 px
                // above
                // it there (out of the pods' reach) and lived 9-14 s (BeamEraHpTest).
                double beside = DarkPlay.HULL_X + below.hitbox().width() / 2 + BESIDE;
                targetX += shipX >= targetX ? beside : -beside;
            }
        } else {
            GroundObject ground = TargetPlay.lowestGround(sortie, shipY);
            if (ground != null) {
                targetX = ground.renderX();
            }
        }
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(
                    sortie,
                    MovePlanner.MOVE_X[m],
                    MovePlanner.MOVE_Y[m],
                    targetX,
                    targetY,
                    beacon != null ? BEACON_X : ALONG_X);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best] | (flaresDue(sortie) ? Command.SPECIAL.bit() : 0);
    }

    /** Whether {@code enemy} is a Depth Hunter: a unit (not a boss's part) whose gun launches homing torpedoes. */
    static boolean hunter(Enemy enemy) {
        return enemy.spec().gun().isPresent()
                && enemy.spec().gun().get().homing().isPresent();
    }

    /** Whether the Decoy Flares are fitted, ready and charged and a torpedo is within {@link #FLARE_RANGE}. */
    static boolean flaresDue(Sortie sortie) {
        SpecialSlot special = sortie.special();
        if (!special.fitted() || !special.name().equals("Decoy Flares") || !special.ready() || special.charges() <= 0) {
            return false;
        }
        for (int i = 0; i < sortie.missileCount(); i++) {
            Missile missile = sortie.missile(i);
            if (Math.hypot(
                            missile.x() - sortie.ship().x(),
                            missile.y() - sortie.ship().y())
                    < FLARE_RANGE) {
                return true;
            }
        }
        return false;
    }

    /** What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the hot discs, the line. */
    static double cost(Sortie sortie, double dx, double dy, double targetX, double targetY, double alongX) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double groundSpeed = sortie.groundSpeed();
        FurnaceVents vents = sortie.vents();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, DarkPlay.HULL_X, DarkPlay.HULL_Y));
            }
            for (int i = 0; i < sortie.missileCount(); i++) {
                Missile missile = sortie.missile(i);
                double mx = missile.renderX(1) + (missile.renderX(1) - missile.renderX(0)) * steps;
                double my = missile.renderY(1) + (missile.renderY(1) - missile.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                mx - x,
                                my - y,
                                DarkPlay.HULL_X + Missile.BOX.width() / 2,
                                DarkPlay.HULL_Y + Missile.BOX.height() / 2));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.layer().touchable() || enemy.layer() == Layer.GROUND || enemy.cloaked()) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ex - x,
                                ey - y,
                                DarkPlay.HULL_X + enemy.hitbox().width() / 2,
                                DarkPlay.HULL_Y + enemy.hitbox().height() / 2));
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
            for (int g = 0; g < vents.grates(); g++) {
                if (!vents.disc(g) || !vents.present(g)) {
                    continue;
                }
                FurnaceVents.Phase phase = vents.phase(g);
                if (phase == FurnaceVents.Phase.IDLE) {
                    continue;
                }
                double reach = vents.radius(g) + DISC_MARGIN;
                double gx = vents.x(g) - x;
                double gy = vents.y(g) - groundSpeed * t - y;
                if (MovePlanner.gap(gx, gy, DarkPlay.HULL_X + reach, DarkPlay.HULL_Y + reach) <= 0
                        && Math.hypot(
                                        Math.max(0, Math.abs(gx) - DarkPlay.HULL_X),
                                        Math.max(0, Math.abs(gy) - DarkPlay.HULL_Y))
                                < reach) {
                    boolean burning = phase == FurnaceVents.Phase.BURN || vents.progress(g) >= TELL_HOT;
                    cost += weight * (burning ? BURN : TELL);
                }
            }
        }
        cost += alongX * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        return cost;
    }
}
