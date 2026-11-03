package vanguard.content;

import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EscapeRun;
import vanguard.sim.Lob;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.WedgeBody;

/**
 * M10 part H, step H3 (Level 42's Ascendant, design/enemies/bosses/ascendant, The escort (Rook) and Duration and HP;
 * m10h-gaps.md §1 #12): the autopilot's play against an escaping bow boss (see {@link Autopilot}), Rook's rule for the
 * ship. Its line, the beam's column under one part at a time: in phase 1 the shield's feeders first (the generators:
 * the cannon's shield falls), then a mirrored battery (the one with the least HP: the beam heats its panel until it
 * breaks and burns on into it while it is down; the far-side shots of the wings pass the other panels), the cannon last;
 * a bay Dart diving at it in reach first. With the generators dead and three panels or more standing it fires the EMP
 * Burst under its battery (it shatters them all). In phase 2 the regenerating part with the least HP that its beam can
 * reach (a sealed part lower in the column hides the one above: the heart covers the keel's sac), held on it so its
 * regeneration never starts, a Chimera in reach first; once the heart opens, the heart. From the hull's break it only
 * dodges (round 80: the lifeboat is scenery). Every move is planned as {@link RingPlay}'s (the bullets, the missiles, the bay units, the told lance
 * through the boss's lines, nearest its line), and further off the bow's wedge (its contact and push) and out of a
 * lob's marked impact. Allocation-free and stateless.
 */
final class AscendantPlay {
    private AscendantPlay() {}

    /** Its height under the bow (y up; the bow's tip at 278), px. */
    static final double STAND_Y = 110;
    /** The column's half width over a part (the beam on it), px. */
    static final double COLUMN = 8;
    /** The cost of each px outside its target's column in each sample (the beam held on it). */
    static final double HOLD = 1.0;
    /** The cost of each sample with the hull touching the bow's wedge (with {@link #WEDGE_MARGIN}). */
    static final double IN_WEDGE = 500;
    /** The margin kept round the wedge, px. */
    static final double WEDGE_MARGIN = 10;
    /** The cost of each sample with its centre inside a marked lob's impact (and {@link #LOB_MARGIN}). */
    static final double IN_LOB = 300;
    /** The margin kept round a lob's impact, px. */
    static final double LOB_MARGIN = 26;
    /** It keeps off the side edges, px. */
    static final double EDGE = 40;
    /** The EMP Burst when this many panels stand (and its battery's panel). */
    static final int EMP_PANELS = 3;

    /** The escaping boss present and not yet escaped (Level 42's Ascendant); null without one. */
    static SetPiece escapingBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && piece.boss().isPresent() && piece.escape().isPresent() && !piece.escaped()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's), and the EMP Burst when it is time. */
    static int move(Sortie sortie, SetPiece piece) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = piece.renderX(1);
        EscapeRun run = piece.escape().orElseThrow();
        int part = -1;
        if (!run.broken() && !piece.opening()) {
            Enemy unit = RingPlay.dartInReach(sortie, shipX, shipY);
            part = target(piece, shipX);
            if (unit != null) {
                targetX = unit.renderX(1);
            } else if (part >= 0) {
                targetX = piece.partX(part);
            }
        }
        targetX = Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double dx = MovePlanner.MOVE_X[m];
            double dy = MovePlanner.MOVE_Y[m];
            double cost = RingPlay.cost(sortie, piece, null, dx, dy, targetX, STAND_Y, COLUMN)
                    + hazards(sortie, piece, dx, dy, part >= 0 ? targetX : Double.NaN);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        boolean emp = part >= 0 && empNow(sortie, piece, part, shipX);
        return MovePlanner.MOVES[best] | (emp ? Command.SPECIAL.bit() : 0);
    }

    /**
     * The part its beam lines up under, -1 for none: of the current phase's parts its beam reaches (living, not
     * shielded, no sealed or living part lower in its column), a shield's feeder first, then a mirrored part, then the
     * rest, the least HP first (in phase 2 the regenerating parts by their HP), the nearer across on a tie.
     */
    static int target(SetPiece piece, double shipX) {
        BossSpec boss = piece.boss().orElseThrow();
        BossSpec.Phase phase = boss.phases().get(piece.phase());
        int best = -1;
        double least = Double.POSITIVE_INFINITY;
        java.util.List<Integer> until = phase.untilParts();
        for (int i = 0; i < until.size(); i++) {
            int p = until.get(i);
            if (piece.partHp(p) <= 0 || piece.partWrecked(p) || !reachable(piece, p)) {
                continue;
            }
            double score = rank(boss, p) * 1e6 + piece.partHp(p) + 0.01 * Math.abs(piece.partX(p) - shipX);
            if (score < least) {
                least = score;
                best = p;
            }
        }
        return best;
    }

    /** Part {@code p}'s rank in the pick: 0 a shield's feeder, 1 a regenerating or mirrored part, 2 the rest. */
    static int rank(BossSpec boss, int p) {
        if (feeder(boss, p)) {
            return 0;
        }
        for (int i = 0; i < boss.mirrors().size(); i++) {
            if (boss.mirrors().get(i).part() == p) {
                return 1;
            }
        }
        for (int i = 0; i < boss.regeneration().size(); i++) {
            if (boss.regeneration().get(i).part() == p) {
                return 1;
            }
        }
        return 2;
    }

    /** Whether part {@code p} feeds one of the boss's shields. */
    static boolean feeder(BossSpec boss, int p) {
        for (int i = 0; i < boss.fedShields().size(); i++) {
            java.util.List<Integer> feeders = boss.fedShields().get(i).feeders();
            for (int k = 0; k < feeders.size(); k++) {
                if (feeders.get(k) == p) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a beam from below reaches part {@code p}: it is not shielded and no other living part (a sealed one too)
     * lies lower in its column.
     */
    static boolean reachable(SetPiece piece, int p) {
        if (piece.partShielded(p)) {
            return false;
        }
        double x = piece.partX(p);
        double y = piece.partY(p);
        for (int q = 0; q < piece.partCount(); q++) {
            if (q == p || piece.partHp(q) <= 0 || piece.partWrecked(q) || piece.partY(q) >= y) {
                continue;
            }
            double half = piece.spec().parts().get(q).box().width() / 2;
            if (Math.abs(piece.partX(q) - x) < half && q != lifeboat(piece)) {
                return false;
            }
        }
        return true;
    }

    /** The escape's lifeboat part (scenery: shots pass through it). */
    static int lifeboat(SetPiece piece) {
        return piece.escape().orElseThrow().spec().part();
    }

    /**
     * The EMP Burst now: fitted, ready and charged, no shield feeder left, under its target {@code part} (a mirrored
     * battery) with at least {@link #EMP_PANELS} panels standing.
     */
    static boolean empNow(Sortie sortie, SetPiece piece, int part, double shipX) {
        var mirrors = piece.mirrors().orElse(null);
        if (mirrors == null || !mirrors.fitted(part) || Math.abs(piece.partX(part) - shipX) > COLUMN) {
            return false;
        }
        BossSpec boss = piece.boss().orElseThrow();
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.partHp(p) > 0 && feeder(boss, p)) {
                return false;
            }
        }
        int standing = 0;
        for (int p = 0; p < piece.partCount(); p++) {
            if (mirrors.standing(p)) {
                standing++;
            }
        }
        return standing >= EMP_PANELS && mirrors.standing(part) && GatePlay.empReady(sortie);
    }

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs beyond {@link RingPlay#cost}: each sample with the
     * hull on the bow's wedge, each with its centre in a marked lob's impact.
     */
    static double hazards(Sortie sortie, SetPiece piece, double dx, double dy, double holdX) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        WedgeBody wedge = piece.boss().orElseThrow().wedge().orElse(null);
        double cx = piece.renderX(1);
        double cy = piece.renderY(1);
        double cost = 0;
        double nearest = Double.POSITIVE_INFINITY;
        for (int k = 1; k <= RingPlay.SAMPLES; k++) {
            double t = k * RingPlay.SAMPLE;
            double x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            double y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double weight = 1 - 0.5 * (k - 1) / RingPlay.SAMPLES;
            if (!Double.isNaN(holdX)) {
                // the nearest it has come so far (it stops there: no overshoot counted)
                nearest = Math.min(nearest, Math.max(0, Math.abs(x - holdX) - COLUMN));
                cost += weight * HOLD * nearest;
            }
            if (wedge != null && onWedge(wedge, x - cx, y - cy)) {
                cost += weight * IN_WEDGE;
            }
            for (int i = 0; i < sortie.lobCount(); i++) {
                Lob lob = sortie.lob(i);
                double reach = lob.impactRadius() + LOB_MARGIN;
                double lx = lob.targetX() - x;
                double ly = lob.targetY() - y;
                if (lx * lx + ly * ly < reach * reach) {
                    cost += weight * IN_LOB;
                }
            }
        }
        return cost;
    }

    /** Whether the hull centred (dx, dy) from the boss's centre (y up) touches the wedge, with {@link #WEDGE_MARGIN}. */
    static boolean onWedge(WedgeBody wedge, double dx, double dy) {
        double halfW = DarkPlay.HULL_X + WEDGE_MARGIN;
        double halfH = DarkPlay.HULL_Y + WEDGE_MARGIN;
        if (!(dy + halfH > wedge.tipDy()) || !(dy - halfH < wedge.topDy())) {
            return false;
        }
        double top = Math.min(dy + halfH, wedge.topDy());
        double share = Math.clamp((top - wedge.tipDy()) / (wedge.topDy() - wedge.tipDy()), 0, 1);
        double half = wedge.tipHalfWidth() + (wedge.topHalfWidth() - wedge.tipHalfWidth()) * share;
        return Math.abs(dx) < half + halfW;
    }
}
