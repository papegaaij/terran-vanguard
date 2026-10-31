package vanguard.sim;

import java.util.List;

/**
 * The player's shots' static geometry (RF9c, out of {@link PlayerFire}): a beam's column contact, a shot's line of flight
 * into a unit part, a core or a box, a blast's reach, the layer factor, whether a unit is on the field and whether the
 * chain has targets. Used by the hit tests, the specials, the escorts and the bosses. Allocates nothing.
 */
final class ShotGeometry {
    private ShotGeometry() {}

    /**
     * M7 part A: where a beam's column centred on {@code cx}, {@code halfWidth} either side and running
     * up from {@code fromY}, first touches a box of {@code box} around (bx, by): its bottom edge, or the
     * column's start when the box reaches below it; {@link Double#POSITIVE_INFINITY} when the column
     * misses it (touching edges do not count, as for {@link Hitbox#overlaps}).
     */
    static double columnContact(double cx, double halfWidth, double fromY, double bx, double by, Hitbox box) {
        if (Math.abs(bx - cx) * 2 >= box.width() + 2 * halfWidth || by + box.height() / 2 <= fromY) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(fromY, by - box.height() / 2);
    }

    /**
     * M7 part D, round 40 R.a (design/enemies/ground/shellback, the lime vent): whether {@code shot}, of {@code
     * size}, over {@code enemy}'s body, flies on into the box of its standing unit part before it leaves the body:
     * its line of flight from where it is, as far as the body's box is wide and tall, crosses the part's box grown by
     * the shot's half size. Such a shot hits the part first, from any side (the vent on the back of the shell is seen
     * from above). Allocates nothing.
     */
    static boolean towardPart(Shot shot, Hitbox size, Enemy enemy, int p) {
        double vx = shot.vx();
        double vy = shot.vy();
        double speed = Math.hypot(vx, vy);
        if (speed == 0) {
            return false;
        }
        Hitbox body = enemy.hitbox();
        double reach = body.width() + body.height();
        Hitbox part = enemy.partBox(p);
        return segmentCrossesBox(
                shot.x(),
                shot.y(),
                vx / speed * reach,
                vy / speed * reach,
                enemy.partX(p),
                enemy.partY(p),
                (part.width() + size.width()) / 2,
                (part.height() + size.height()) / 2);
    }

    /**
     * M9 part E (design/enemies/ground/sentinel-tower, the hardened collar): whether {@code shot}, of {@code size}, over
     * a hardened unit with a core lands on the core (the emitter) or flies on into it: it overlaps the core's box, or
     * its line of flight as far as the unit's box is wide and tall crosses it (as {@link #towardPart}). False for a
     * unit without a core. Allocates nothing.
     */
    static boolean onCore(Shot shot, Hitbox size, Enemy enemy) {
        if (enemy.spec().core().isEmpty()) {
            return false;
        }
        Hitbox core = enemy.spec().core().get();
        if (size.overlaps(shot.x(), shot.y(), core, enemy.x(), enemy.y())) {
            return true;
        }
        double vx = shot.vx();
        double vy = shot.vy();
        double speed = Math.hypot(vx, vy);
        if (speed == 0) {
            return false;
        }
        Hitbox body = enemy.hitbox();
        double reach = body.width() + body.height();
        return segmentCrossesBox(
                shot.x(),
                shot.y(),
                vx / speed * reach,
                vy / speed * reach,
                enemy.x(),
                enemy.y(),
                (core.width() + size.width()) / 2,
                (core.height() + size.height()) / 2);
    }

    /**
     * M7 part D: the standing unit part of {@code enemy} that {@code shot}, of {@code size}, hits first; -1 for none
     * (it hits the body, or nothing). A part it overlaps comes first; else, with the shot over the body ({@code
     * onBody}), the part its line of flight runs into ({@link #towardPart}); M8 part B: of several (a Drill Rig's
     * pumps), the one whose centre is nearest the shot, the first in the stat block's order on a tie. Allocates
     * nothing.
     */
    static int struckPart(Shot shot, Hitbox size, Enemy enemy, boolean onBody) {
        int best = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        boolean bestOverlaps = false;
        for (int p = 0; p < enemy.partCount(); p++) {
            if (!enemy.partAlive(p)) {
                continue;
            }
            double px = enemy.partX(p);
            double py = enemy.partY(p);
            boolean overlaps = size.overlaps(shot.x(), shot.y(), enemy.partBox(p), px, py);
            if (!overlaps && (bestOverlaps || !onBody || !towardPart(shot, size, enemy, p))) {
                continue;
            }
            double distance = (px - shot.x()) * (px - shot.x()) + (py - shot.y()) * (py - shot.y());
            if ((overlaps && !bestOverlaps) || distance < bestDistance) {
                best = p;
                bestDistance = distance;
                bestOverlaps = overlaps;
            }
        }
        return best;
    }

    /**
     * Whether the segment from (x, y) to (x + dx, y + dy) crosses the box of half sizes {@code halfW} by {@code
     * halfH} round (bx, by) (the slab test; touching edges do not count).
     */
    static boolean segmentCrossesBox(
            double x, double y, double dx, double dy, double bx, double by, double halfW, double halfH) {
        double enter = 0;
        double leave = 1;
        if (dx == 0) {
            if (Math.abs(x - bx) >= halfW) {
                return false;
            }
        } else {
            double a = (bx - halfW - x) / dx;
            double b = (bx + halfW - x) / dx;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        if (dy == 0) {
            if (Math.abs(y - by) >= halfH) {
                return false;
            }
        } else {
            double a = (by - halfH - y) / dy;
            double b = (by + halfH - y) / dy;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        return enter < leave;
    }

    /**
     * Whether a living part on the field lies ahead of the shot in its line of flight: a shot over
     * the armoured body flies on over the unit's back towards it (the vents and the blowhole sit on
     * the body); a shot with nothing ahead of it glances off the armour.
     */
    static boolean partAhead(SetPiece piece, Shot shot) {
        double dirX = Trig.sin(shot.heading());
        double dirY = Trig.cos(shot.heading());
        List<PartSpec> parts = piece.spec().parts();
        for (int p = 0; p < parts.size(); p++) {
            Hitbox box = parts.get(p).box();
            double px = piece.partX(p);
            double py = piece.partY(p);
            if (piece.partWrecked(p) || !PlayField.overlaps(px, py, box)) {
                continue;
            }
            double relX = px - shot.x();
            double relY = py - shot.y();
            double reach =
                    (Math.max(box.width(), box.height()) + shot.weapon().size().width()) / 2;
            if (relX * dirX + relY * dirY > 0 && Math.abs(relX * dirY - relY * dirX) < reach) {
                return true;
            }
        }
        return false;
    }

    /** Bolts and missiles of an {@code anti-ground} weapon do double damage on the ground layer. */
    static double groundFactor(WeaponSpec weapon, Layer layer) {
        return layer == Layer.GROUND
                        && weapon.antiGround()
                        && !weapon.delivery().landing()
                ? 2
                : 1;
    }

    /** Whether a blast of {@code radius} around (x, y) reaches a box around (bx, by). */
    static boolean inBlast(double x, double y, double radius, double bx, double by, Hitbox box) {
        double dx = Math.max(Math.abs(x - bx) - box.width() / 2, 0);
        double dy = Math.max(Math.abs(y - by) - box.height() / 2, 0);
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Whether anything that keeps the chain going is on screen (design/systems/scoring, Chain
     * multiplier): a live enemy on a layer the standard shots reach (not high air) whose hit box
     * overlaps the play field, or a set piece or boss with a part on such a layer that is on the
     * field, not wrecked and not shielded (a boss's descent). Otherwise the chain window pauses.
     */
    static boolean chainTargets(Pool<Enemy> enemies, SetPiece[] setPieces) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            // M9 part B: a neutral craft is no target of the chain.
            if (enemy.layer().hitByStandardShots()
                    && onField(enemy)
                    && enemy.spec().countsAsEnemy()) {
                return true;
            }
        }
        for (SetPiece piece : setPieces) {
            if (!piece.present()) {
                continue;
            }
            List<PartSpec> parts = piece.spec().parts();
            for (int p = 0; p < parts.size(); p++) {
                // M5 part E: on the part's own layer (an arena boss's part under the water is none).
                if (piece.partLayer(p).hitByStandardShots()
                        && !piece.partWrecked(p)
                        && !piece.partShielded(p)
                        && piece.partOnField(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether shots, blasts and specials can reach {@code enemy}: on the play field, and not a chain
     * member waiting for its burst ({@link Chain#doomed}).
     */
    static boolean onField(Enemy enemy) {
        return PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox()) && !Chain.doomed(enemy);
    }

    static double distanceSquared(double x, double y, double ox, double oy) {
        return (x - ox) * (x - ox) + (y - oy) * (y - oy);
    }
}
