package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * The autopilot against the Siege Spire (M5 part H, Level 14, see {@link Autopilot}): its parts low first, the
 * launched Wraiths, the sweeps crossed.
 */
final class SpirePlay {
    private SpirePlay() {}

    /** M5 part H: its height under the airborne Siege Spire, px (the sweep reaches it since round 41). */
    private static final double SWEEP_Y = 60;

    /** M5 part H: the present arena boss without lanes (Level 14's Siege Spire), alive; null without one. */
    static SetPiece rootedBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present()
                    && !piece.destroyed()
                    && piece.arena().isPresent()
                    && piece.arena().get().laneCount() == 0) {
                return piece;
            }
        }
        return null;
    }

    /**
     * M5 part H, the Siege Spire (decisions H1–H5 = a): the boss planner toward its lowest part that takes
     * damage now (a root, then the maw, then the free core), below it; a launched Wraith holding (or about to
     * hold) below the ship first, from above it, for the rear gun; under the airborne spire it keeps low and
     * (round 41) crosses each sweep's telegraph line first. The lobs' markers are sidestepped before this (as
     * Level 05's).
     */
    static int spireMove(Sortie sortie, SetPiece boss) {
        int dodge = sweepDodge(sortie, boss);
        if (dodge != 0) {
            return dodge;
        }
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int lobber = lobbingPart(boss);
        double targetX = boss.lifting() ? shipX : lobber >= 0 ? boss.partX(lobber) : Autopilot.vitalX(boss);
        Enemy behind = holdingBelow(sortie, shipY, targetX);
        if (behind != null) {
            return MovePlanner.bossMove(sortie, behind.renderX(1), behind.renderY(1) + Autopilot.AMBUSH_ABOVE);
        }
        double lowest = lobber >= 0 ? boss.partY(lobber) : lowestPart(boss);
        double targetY = boss.lifted() ? SWEEP_Y : Math.min(Autopilot.CRUISE_Y, lowest - 70);
        return MovePlanner.bossMove(sortie, Math.clamp(targetX, 40, PlayField.WIDTH - 40), Math.max(40, targetY));
    }

    /** Round 41: how far behind a sweep's telegraph line the ship stops, px (its hull, the beam's half width, some). */
    private static final double SWEEP_CLEAR = 40;

    /**
     * Round 41 (the Siege Spire's leading sweep, the user's playtest 2026-10-19: "forced to keep moving"): while a
     * sweep telegraphs or beams, the way of the eight that crosses its telegraph's line soonest (the side the beam does
     * not go), until the ship is {@link #SWEEP_CLEAR} behind it; 0 without a sweep or once behind it (as sim's
     * {@code SpirePlaytestTest}).
     */
    private static int sweepDodge(Sortie sortie, SetPiece boss) {
        var attacks = boss.boss().orElseThrow().attacks();
        // Indexed: a for-each would allocate an iterator in the allocation measurements.
        for (int a = 0; a < attacks.size(); a++) {
            if (attacks.get(a).sweep().isEmpty() || !(boss.sweepTelegraphing(a) || boss.sweepBeaming(a))) {
                continue;
            }
            double from = boss.sweepFrom(a);
            double direction = Math.signum(boss.sweepTo(a) - from);
            double nx = direction * Math.cos(from);
            double ny = -direction * Math.sin(from);
            double shipX = sortie.ship().x();
            double shipY = sortie.ship().y();
            double behind = (shipX - boss.sweepOriginX(a)) * nx + (shipY - boss.sweepOriginY(a)) * ny;
            if (behind < SWEEP_CLEAR) {
                return MovePlanner.escape(sortie, nx, ny);
            }
        }
        return 0;
    }

    /**
     * M5 part H: the living part that lobs (a mortar root) that takes damage now, the lowest first: the lobs'
     * markers and rings cost the most, so those roots go first; -1 without one.
     */
    private static int lobbingPart(SetPiece boss) {
        var attacks = boss.boss().orElseThrow().attacks();
        int best = -1;
        // Indexed: a for-each would allocate an iterator in the allocation measurements.
        for (int a = 0; a < attacks.size(); a++) {
            var attack = attacks.get(a);
            if (attack.pattern() != vanguard.sim.BossSpec.Pattern.LOB) {
                continue;
            }
            for (int k = 0; k < attack.parts().size(); k++) {
                int p = attack.parts().get(k);
                if (!boss.partWrecked(p) && !boss.partShielded(p) && (best < 0 || boss.partY(p) < boss.partY(best))) {
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * M5 part H: a launched Wraith in its flash or hold below the ship, the one nearest {@code x} (the part it
     * shoots at), to line up over for the rear gun; null without one.
     */
    private static Enemy holdingBelow(Sortie sortie, double shipY, double x) {
        Enemy nearest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (TargetPlay.holding(enemy)
                    && enemy.renderY(1) < shipY
                    && (nearest == null || Math.abs(enemy.renderX(1) - x) < Math.abs(nearest.renderX(1) - x))) {
                nearest = enemy;
            }
        }
        return nearest;
    }

    /** M5 part H: the height of the lowest part that takes damage now, px up; the boss's centre without one. */
    private static double lowestPart(SetPiece boss) {
        double lowest = Double.POSITIVE_INFINITY;
        for (int p = 0; p < boss.partCount(); p++) {
            if (!boss.partWrecked(p) && !boss.partShielded(p) && !boss.partArmoured(p)) {
                lowest = Math.min(lowest, boss.partY(p));
            }
        }
        return Double.isInfinite(lowest) ? boss.renderY(1) : lowest;
    }
}
