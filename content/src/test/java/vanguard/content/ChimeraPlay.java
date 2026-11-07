package vanguard.content;

import vanguard.sim.BossSpec;
import vanguard.sim.Enemy;
import vanguard.sim.Lob;
import vanguard.sim.PlayField;
import vanguard.sim.RearSweep;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F2 (Level 48's Vorne's Chimera, design/enemies/bosses/vornes-chimera, The escort (Rook) and Duration
 * and HP; m11f-gaps.md §1 #15): the autopilot's play against a sweeping mid-boss (see {@link Autopilot}), Rook's rule for
 * the ship. Its line, the beam's column under one part at a time it can reach (no sealed or living part lower in its
 * column, {@link #aimX}): in phase 1 the Dart bay first, then the rack, then the batteries (the beam
 * heats a panel until it breaks and burns on into it while it is down), the least HP first among equals; in phase 2 the
 * sac with the least HP (held on it so its regeneration never starts); in phase 3 the capsule; a launched Dart or
 * Chimera diving at it in reach first. While the rear sweep flies it keeps to the side away from the hull (the marked
 * rising point's other half) and lets the rear guns work; every move is planned as {@link RingPlay}'s (the bullets, the
 * missiles, the launched units, the told lance through the boss's lines), out of a lob's marked impact and off the
 * hull's body box. From K it only dodges. Allocation-free and stateless.
 */
final class ChimeraPlay {
    private ChimeraPlay() {}

    /** Its height (y up): well under the station's body (its bottom at y 325), px. */
    static final double STAND_Y = 120;
    /** The column's half width over a part (the beam on it), px. */
    static final double COLUMN = 8;
    /** The cost of each sample with the hull on the boss's body box (with {@link #BODY_MARGIN}). */
    static final double ON_BODY = 600;
    /** The margin kept round the body box, px. */
    static final double BODY_MARGIN = 24;
    /** The cost of each sample with its centre inside a marked lob's impact (and {@link AscendantPlay#LOB_MARGIN}). */
    static final double IN_LOB = 300;
    /** It keeps off the side edges, px. */
    static final double EDGE = 40;
    /** The sway's lead on its target column, steps. */
    static final double LEAD_STEPS = 9;
    /** While the sweep flies it stands this far across from the hull's side (or its marked rising point), px. */
    static final double AWAY = 170;

    /** The sweeping boss present and not yet burst (Level 48's Chimera); null without one. */
    static SetPiece chimeraBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && piece.boss().isPresent() && piece.rearSweep().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie, SetPiece piece) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = piece.renderX(1);
        RearSweep sweep = piece.rearSweep().orElseThrow();
        boolean spoken = piece.lastWords().filter(words -> words.spoken()).isPresent();
        int part = -1;
        if (sweep.flying()) {
            // off the hull's side: the marked rising point while it is away, the hull's x as it drops and climbs
            double hullX = sweep.away() ? sweep.markX() : piece.renderX(1);
            targetX = hullX < PlayField.WIDTH / 2.0 ? hullX + AWAY : hullX - AWAY;
        } else if (!spoken && piece.engaged() && !piece.opening()) {
            Enemy unit = RingPlay.dartInReach(sortie, shipX, shipY);
            part = target(piece, shipX);
            if (unit != null) {
                targetX = unit.renderX(1);
            } else if (part >= 0) {
                // led by the sway's motion over the ship's reaction (the column is narrow past the capsule's flanks)
                targetX = aimX(piece, part, shipX) + (piece.renderX(1) - piece.renderX(0)) * LEAD_STEPS;
            }
        }
        targetX = Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double dx = MovePlanner.MOVE_X[m];
            double dy = MovePlanner.MOVE_Y[m];
            double cost = RingPlay.cost(sortie, piece, null, dx, dy, targetX, STAND_Y, COLUMN)
                    + hazards(sortie, piece, dx, dy);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /**
     * The part its beam lines up under, -1 for none: of the current phase's living parts its beam reaches, the lowest
     * rank first ({@link #rank}), then the least HP, then the nearer across.
     */
    static int target(SetPiece piece, double shipX) {
        BossSpec boss = piece.boss().orElseThrow();
        java.util.List<Integer> until = boss.phases().get(piece.phase()).untilParts();
        int best = -1;
        double least = Double.POSITIVE_INFINITY;
        for (int i = 0; i < until.size(); i++) {
            int p = until.get(i);
            if (piece.partHp(p) <= 0 || piece.partWrecked(p) || Double.isNaN(aimX(piece, p, shipX))) {
                continue;
            }
            double score = rank(piece, p) * 1e6 + piece.partHp(p) + 0.01 * Math.abs(piece.partX(p) - shipX);
            if (score < least) {
                least = score;
                best = p;
            }
        }
        return best;
    }

    /** Part {@code p}'s rank: its place in Rook's order for the phase (the bay, the rack, the batteries), else 0. */
    static int rank(SetPiece piece, int p) {
        BossSpec boss = piece.boss().orElseThrow();
        for (int i = 0; i < boss.rookOrders().size(); i++) {
            BossSpec.RookOrder order = boss.rookOrders().get(i);
            if (order.phase() == piece.phase()) {
                int at = order.parts().indexOf(p);
                return at < 0 ? order.parts().size() : at;
            }
        }
        return 0;
    }

    /** The beam's half width, px (the plan's Ion Beam L5: 14 px). */
    static final double BEAM_HALF = 7;

    /**
     * The column x where a beam from below reaches part {@code p}, the nearest to {@code shipX}: its centre, else just
     * inside one of its edges (the rack past the sealed capsule's flank: the spec's "hit from below past the capsule's
     * bulk"); NaN when none does (it is shielded, or another living part, a sealed one too, lies lower over every one).
     */
    static double aimX(SetPiece piece, int p, double shipX) {
        if (piece.partShielded(p)) {
            return Double.NaN;
        }
        double x = piece.partX(p);
        double edge = piece.spec().parts().get(p).box().width() / 2 - 4;
        double best = Double.NaN;
        for (int c = 0; c < 3; c++) {
            double at = c == 0 ? x : c == 1 ? x - edge : x + edge;
            if (clear(piece, p, at) && (Double.isNaN(best) || Math.abs(at - shipX) < Math.abs(best - shipX))) {
                best = at;
                if (c == 0) {
                    return best;
                }
            }
        }
        return best;
    }

    /** Whether a beam at column {@code x} meets no living part lower than part {@code p}. */
    private static boolean clear(SetPiece piece, int p, double x) {
        double y = piece.partY(p);
        for (int q = 0; q < piece.partCount(); q++) {
            if (q == p || piece.partHp(q) <= 0 || piece.partWrecked(q) || piece.partY(q) >= y) {
                continue;
            }
            double half = piece.spec().parts().get(q).box().width() / 2 + BEAM_HALF;
            if (Math.abs(piece.partX(q) - x) < half) {
                return false;
            }
        }
        return true;
    }

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs beyond {@link RingPlay#cost}: each sample with the
     * hull on the boss's body box (the climbing hull too), each with its centre in a marked lob's impact.
     */
    static double hazards(Sortie sortie, SetPiece piece, double dx, double dy) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double cx = piece.renderX(1);
        double cy = piece.renderY(1);
        double vx = piece.renderX(1) - piece.renderX(0);
        double vy = piece.renderY(1) - piece.renderY(0);
        double halfW = piece.spec().body().width() / 2 + DarkPlay.HULL_X + BODY_MARGIN;
        double halfH = piece.spec().body().height() / 2 + DarkPlay.HULL_Y + BODY_MARGIN;
        double cost = 0;
        for (int k = 1; k <= RingPlay.SAMPLES; k++) {
            double t = k * RingPlay.SAMPLE;
            double steps = t / vanguard.sim.SimStep.SECONDS;
            double x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            double y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double weight = 1 - 0.5 * (k - 1) / RingPlay.SAMPLES;
            if (Math.abs(x - (cx + vx * steps)) < halfW && Math.abs(y - (cy + vy * steps)) < halfH) {
                cost += weight * ON_BODY;
            }
            for (int i = 0; i < sortie.lobCount(); i++) {
                Lob lob = sortie.lob(i);
                double reach = lob.impactRadius() + AscendantPlay.LOB_MARGIN;
                double lx = lob.targetX() - x;
                double ly = lob.targetY() - y;
                if (lx * lx + ly * ly < reach * reach) {
                    cost += weight * IN_LOB;
                }
            }
        }
        return cost;
    }
}
