package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.Ally;
import vanguard.sim.Enemy;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Regrowing;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C, step C3 (design/campaign Level 37 Fall of Aurelia; see {@link Autopilot}): the level's plays, for a level
 * with air allies outside the objectives (the Halcyon shuttles). It covers the shuttles without guarding them: what
 * threatens a shuttle on its station first (a unit of a wave the target-the-objective hook aims at them, a body close to
 * one, a Graft Turret about to scroll under one, where its pair would start inside the shuttle's hull), then a Graft
 * Turret's exposed root before its window closes, then, while no shuttle holds its station, a Mirror Interceptor with its
 * mirror standing: one showing the
 * ship its flank first (on its way in, every gun reaches the hull), else one facing it (the beam burns the mirror, the
 * guns finish the hull while it is down; the other guns' shots into the face bounce). The level's Hornets make it a
 * hunted level, so its moves are {@link VentPlay}'s, its line this play's. Only Level 37 has air allies outside the
 * objectives, so nothing here moves another level's flight. Step C6a (round 75 F.3: the player's fire hits the
 * shuttles): it never shoots one: it keeps its line out of the guns' band below a shuttle the guns can hit ({@link
 * #clearX}) and holds its fire while one is in that band or in its seekers' line ({@link #holdFire}).
 */
final class AureliaPlay {
    private AureliaPlay() {}

    /** How close to a shuttle a body counts as a threat to it, px. */
    static final double NEAR = 90;

    /** How far either side of a shuttle a Graft Turret counts as coming under it, px. */
    static final double UNDER_X = 70;

    /** How far above a shuttle a Graft Turret counts as coming under it, px (about 2 s of the deck's scroll). */
    static final double UNDER_AHEAD = 220;

    /** Whether the level has air allies outside the objectives (Level 37's shuttles): the plays below apply. */
    static boolean aurelia(Sortie sortie) {
        return sortie.script().objectives().airAllies().isPresent();
    }

    /**
     * The unit to line up under now, above the ship ({@code shipY} + 40) and on the screen: the shuttles' threat
     * ({@link #cover}), else a Graft Turret's root in its window (the lowest), else, while no shuttle holds its station
     * ({@link #onStation}), a Mirror Interceptor whose mirror
     * stands (the lowest one showing the ship its flank first, else the lowest facing it: the beam burns its mirror); null
     * for none, or without air allies.
     */
    static Enemy target(Sortie sortie, double shipY) {
        if (!aurelia(sortie)) {
            return null;
        }
        Enemy threat = cover(sortie, shipY);
        if (threat != null) {
            return threat;
        }
        Enemy root = null;
        Enemy flank = null;
        Enemy face = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!aimable(sortie, enemy, shipY)) {
                continue;
            }
            double y = enemy.renderY(1);
            if (enemy.spec().regrowth().isPresent() && enemy.regrowth().phase() == Regrowing.Phase.ROOT) {
                if (root == null || y < root.renderY(1)) {
                    root = enemy;
                }
            } else if (enemy.spec().mirror().isPresent() && enemy.mirror().standing()) {
                if (flankOpen(enemy, sortie.ship().x(), shipY)) {
                    if (flank == null || y < flank.renderY(1)) {
                        flank = enemy;
                    }
                } else if (face == null || y < face.renderY(1)) {
                    face = enemy;
                }
            }
        }
        return root != null ? root : flank;
    }

    /** Whether a shuttle holds its station (touchable: off its pads and not yet climbing out). */
    static boolean onStation(Sortie sortie) {
        for (int k = 0; k < sortie.allyCount(); k++) {
            if (sortie.ally(k).touchable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a mirrored unit shows the ship at ({@code shipX}, {@code shipY}) its flank: the ship's bearing outside the
     * mirror's arc of its facing (on its way in it faces its flight), so every gun's shots reach its hull; inside the
     * arc the beam heats the mirror and the other guns' shots bounce.
     */
    static boolean flankOpen(Enemy enemy, double shipX, double shipY) {
        double bearing = StrictMath.atan2(-(shipX - enemy.renderX(1)), -(shipY - enemy.renderY(1)));
        double off = Math.abs(Math.IEEEremainder(bearing - enemy.mirror().facing(), 2 * StrictMath.PI));
        return off > enemy.spec().mirror().orElseThrow().halfArcRadians();
    }

    /**
     * What threatens a shuttle on its station (touchable): the unit nearest a shuttle among a hooked wave's units, the
     * bodies on the ship's plane within {@link #NEAR} px of one and the standing Graft Turrets coming under one (within
     * {@link #UNDER_X} px either side, at most {@link #UNDER_AHEAD} px above it), the turrets first; null for none.
     */
    static Enemy cover(Sortie sortie, double shipY) {
        Optional<ObjectiveSpec.Escort> allies = sortie.script().objectives().airAllies();
        if (allies.isEmpty() || allies.get().air().isEmpty()) {
            return null;
        }
        // read without Optional.map, which allocates
        List<Integer> hooked = allies.get().air().get().hookedWaves();
        Enemy best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!aimable(sortie, enemy, shipY)) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            boolean turret =
                    enemy.spec().regrowth().isPresent() && enemy.regrowth().phase() == Regrowing.Phase.TURRET;
            boolean plane = enemy.layer().collidesWithPlayer();
            boolean aimed = plane && hooked.contains(enemy.wave());
            for (int k = 0; k < sortie.allyCount(); k++) {
                Ally shuttle = sortie.ally(k);
                if (!shuttle.touchable()) {
                    continue;
                }
                double dx = x - shuttle.x();
                double dy = y - shuttle.y();
                double score;
                if (turret) {
                    if (Math.abs(dx) > UNDER_X || dy < -20 || dy > UNDER_AHEAD) {
                        continue;
                    }
                    // the turrets first: one coming under a shuttle fires from inside its hull
                    score = dy - PlayField.HEIGHT;
                } else if (!plane) {
                    continue;
                } else {
                    score = Math.hypot(dx, dy);
                    if (!aimed && score > NEAR) {
                        continue;
                    }
                }
                if (score < bestScore) {
                    best = enemy;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    /**
     * M10 part C, step C6a (round 75 F.3): how far ahead a shuttle's slide is followed for the guns' band, s (a bolt's
     * flight from the ship to the shuttles' band).
     */
    static final double LEAD = 0.5;

    /** M10 part C, step C6a: the homing shots' seek radius (the Micro-missile Pod's {@code range}), px. */
    static final double SEEK = 350;

    /** M10 part C, step C6a: the margin beside a shuttle's half hull a homing shot's line keeps, px. */
    static final double SEEK_MARGIN = 16;

    /**
     * M10 part C, step C6a (round 75 F.3: the player's fire hits the shuttles, {@code player_hits}): whether a shuttle
     * the guns can hit (on its station or sliding aside) is above the ship within the band of its x ({@link
     * HoldFirePlay#BAND} beyond its half hull) now or along its slide over the next {@link #LEAD} s: no shot then (the
     * beam included).
     */
    static boolean holdFire(Sortie sortie) {
        if (!playerHits(sortie)) {
            return false;
        }
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double half = half(sortie);
        for (int k = 0; k < sortie.allyCount(); k++) {
            Ally shuttle = sortie.ally(k);
            if (!shuttle.touchable() || shuttle.y() <= shipY) {
                continue;
            }
            double now = shuttle.x() - shipX;
            double ahead = now + (shuttle.renderX(1) - shuttle.renderX(0)) / SimStep.SECONDS * LEAD;
            if (Math.min(now, ahead) < half && Math.max(now, ahead) > -half) {
                return true;
            }
        }
        return seekerThroughShuttle(sortie);
    }

    /**
     * M10 part C, step C6a: the homing shots (the Micro-missile Pod's) go for the nearest unit within {@link #SEEK} px:
     * whether that unit, seen from the ship, lies behind a shuttle the guns can hit (the line to it passes within the
     * shuttle's half hull and {@link #SEEK_MARGIN} px of its centre, the unit farther than the shuttle).
     */
    private static boolean seekerThroughShuttle(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        Enemy nearest = null;
        double least = SEEK;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.hp() <= 0
                    || enemy.cloaked()
                    || HoldFirePlay.neutral(enemy)
                    || !enemy.layer().touchable()
                    || enemy.renderY(1) <= shipY) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (d < least) {
                least = d;
                nearest = enemy;
            }
        }
        if (nearest == null) {
            return false;
        }
        double ex = nearest.renderX(1) - shipX;
        double ey = nearest.renderY(1) - shipY;
        double reach =
                sortie.script().objectives().airAllies().get().ally().hitbox().width() / 2 + SEEK_MARGIN;
        for (int k = 0; k < sortie.allyCount(); k++) {
            Ally shuttle = sortie.ally(k);
            double sx = shuttle.x() - shipX;
            double sy = shuttle.y() - shipY;
            if (!shuttle.touchable() || sy <= 0 || ey < sy - 14) {
                continue;
            }
            // the distance from the shuttle's centre to the line from the ship to the unit
            double off = Math.abs(ex * sy - ey * sx) / least;
            if (off < reach) {
                return true;
            }
        }
        return false;
    }

    /**
     * M10 part C, step C6a: {@code targetX} moved out of the band below every shuttle the guns can hit above the ship,
     * to the band's nearer side that stays on the play field (as {@link HoldFirePlay#clearX} for a neutral craft); as it
     * is without such shuttles.
     */
    static double clearX(Sortie sortie, double targetX) {
        if (!playerHits(sortie)) {
            return targetX;
        }
        double shipY = sortie.ship().y();
        double half = half(sortie) + 2;
        double x = targetX;
        for (int k = 0; k < sortie.allyCount(); k++) {
            Ally shuttle = sortie.ally(k);
            if (!shuttle.touchable() || shuttle.y() <= shipY) {
                continue;
            }
            double dx = x - shuttle.x();
            if (Math.abs(dx) < half) {
                double left = shuttle.x() - half;
                double right = shuttle.x() + half;
                boolean goLeft = dx < 0 ? left > 20 : right > PlayField.WIDTH - 20;
                x = goLeft ? left : right;
            }
        }
        return x;
    }

    /** Whether the level's air allies take the player's fire (Level 37's shuttles since step C6a). */
    static boolean playerHits(Sortie sortie) {
        Optional<ObjectiveSpec.Escort> allies = sortie.script().objectives().airAllies();
        // read without Optional.map, which allocates
        return allies.isPresent()
                && allies.get().air().isPresent()
                && allies.get().air().get().playerHits();
    }

    /** Half the guns' band below a shuttle, px: its half hull and {@link HoldFirePlay#BAND}. */
    private static double half(Sortie sortie) {
        return sortie.script().objectives().airAllies().get().ally().hitbox().width() / 2 + HoldFirePlay.BAND;
    }

    /**
     * M10 part C, step C6a: whether a shuttle the guns can hit stands between the ship and {@code enemy}: above the ship,
     * the unit beyond it within the guns' band of its x (the guns would hit the shuttle first); false without such
     * shuttles.
     */
    static boolean blocked(Sortie sortie, Enemy enemy) {
        if (!playerHits(sortie)) {
            return false;
        }
        double shipY = sortie.ship().y();
        double half = half(sortie);
        double x = enemy.renderX(1);
        double y = enemy.renderY(1);
        for (int k = 0; k < sortie.allyCount(); k++) {
            Ally shuttle = sortie.ally(k);
            if (shuttle.touchable()
                    && shuttle.y() > shipY
                    && y > shuttle.y() - 14
                    && Math.abs(x - shuttle.x()) < half) {
                return true;
            }
        }
        return false;
    }

    /** Whether the ship's guns can take {@code enemy} now: on the screen above the ship, hittable, not neutral. */
    private static boolean aimable(Sortie sortie, Enemy enemy, double shipY) {
        return aimable(enemy, shipY) && !blocked(sortie, enemy);
    }

    private static boolean aimable(Enemy enemy, double shipY) {
        double y = enemy.renderY(1);
        return !enemy.cloaked()
                && !enemy.invulnerable()
                && !HoldFirePlay.neutral(enemy)
                && enemy.layer().touchable()
                && enemy.hp() > 0
                && y < PlayField.HEIGHT - 10
                && y > shipY + 40;
    }
}
