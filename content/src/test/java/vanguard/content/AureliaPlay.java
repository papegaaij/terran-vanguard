package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.Ally;
import vanguard.sim.Enemy;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Regrowing;
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
 * objectives, so nothing here moves another level's flight.
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
            if (!aimable(enemy, shipY)) {
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
            if (!aimable(enemy, shipY)) {
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

    /** Whether the ship's guns can take {@code enemy} now: on the screen above the ship, hittable, not neutral. */
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
