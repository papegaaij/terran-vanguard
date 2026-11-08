package vanguard.content;

import vanguard.sim.ArenaWalls;
import vanguard.sim.BossSpec;
import vanguard.sim.Enemy;
import vanguard.sim.Hull;
import vanguard.sim.Mine;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H2 (Level 50's Choir Heart, design/enemies/bosses/choir-heart, The escort (Rook) and Duration and HP;
 * m11h-gaps.md §1 #21): the autopilot's play against the Heart (see {@link Autopilot}), Rook's rule for the ship. Its
 * line, the beam's column under one part at a time: in phase 1 an open membrane (the one singing: its window open from
 * the tell's first frame), the least HP first; while none is open, the living membrane nearest across (the coil's arc
 * may open it); in phase 2 a wall node its beam reaches (no living node lower on its face), standing just off the wall's
 * face (the hull a few px clear: the beam's column on the node's half that juts into the corridor), the least HP first;
 * in phase 3 the core; a released unit diving at it in reach first. Every move is planned as {@link RingPlay}'s (the
 * bullets, the told lines), off the walls (their faces now and over the look-ahead), out of an armed spore's reach and
 * below the Heart's body. From K it only dodges. Allocation-free and stateless.
 */
final class HeartPlay {
    private HeartPlay() {}

    /** Its height (y up): well under the membranes (the lowest at y-up 340) and the lower nodes (y-up 140), px. */
    static final double STAND_Y = 90;
    /** The column's half width over a part (the beam on it), px. */
    static final double COLUMN = 8;
    /** The hull's gap kept off a wall's face (its own beam's column then still on the node's jutting half), px. */
    static final double FACE_GAP = 4;
    /** The cost of each sample with the hull on a wall or within {@link #FACE_GAP} of its face. */
    static final double ON_WALL = 700;
    /** The cost of each sample with its centre within an armed spore's reach. */
    static final double IN_SPORE = 400;
    /** An armed spore's reach (its contact burst), px. */
    static final double SPORE_REACH = 30;
    /** The lowest y (y up) its hull's top may reach: the Heart's body's bottom (y-up 340) less a margin, px. */
    static final double BODY_FLOOR = 300;
    /** The cost of each sample with the hull's top over {@link #BODY_FLOOR}. */
    static final double ON_BODY = 600;
    /** Below this share of its armour it only dodges, px. */
    static final double LOW_ARMOUR = 0.45;

    /** It keeps off the side edges, px. */
    static final double EDGE = 30;

    /** The Heart (a boss with part windows) present; null without one. */
    static SetPiece heartBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present()
                    && piece.boss().isPresent()
                    && piece.boss().get().partWindows().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie, SetPiece piece) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = PlayField.WIDTH / 2.0;
        double column = COLUMN;
        double standY = STAND_Y;
        boolean dead = piece.destroyed()
                || piece.lastWords().filter(words -> words.spoken()).isPresent();
        if (!dead && piece.engaged()) {
            Enemy unit = RingPlay.dartInReach(sortie, shipX, shipY);
            int part = target(sortie, piece, shipX);
            Pickup patch = patch(sortie);
            if (patch != null) {
                // an armour patch (the closing's, the opening's) first: it goes for it under the Heart's body
                targetX = patch.renderX(1);
                standY = Math.min(patch.renderY(1), BODY_FLOOR - 30);
            } else if (unit != null) {
                targetX = unit.renderX(1);
            } else if (part >= 0) {
                targetX = aimX(sortie, piece, part);
                column = column(sortie, piece, part);
                standY = standY(sortie, piece, part);
            }
        }
        double hull = half(sortie.ship().spec().hull(), true);
        if (sortie.ship().defences().armour()
                < LOW_ARMOUR * sortie.ship().defences().maxArmour()) {
            // low on armour (no checkpoint on hard): it only dodges where it is, its wing guns, Rook and the swarm on
            // the parts, as a careful player would
            targetX = sortie.ship().x();
            column = PlayField.WIDTH;
        }
        targetX = Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double dx = MovePlanner.MOVE_X[m];
            double dy = MovePlanner.MOVE_Y[m];
            double cost =
                    RingPlay.cost(sortie, piece, null, dx, dy, targetX, standY, column) + hazards(sortie, hull, dx, dy);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /**
     * The part its beam lines up under, -1 for none: phase 1 an open membrane (the least HP), else the living membrane
     * nearest across; phase 2 a node its beam reaches (the least HP, then the nearer); phase 3 the core.
     */
    static int target(Sortie sortie, SetPiece piece, double shipX) {
        BossSpec boss = piece.boss().orElseThrow();
        java.util.List<Integer> until = boss.phases().get(piece.phase()).untilParts();
        ArenaWalls walls = sortie.arenaWalls();
        int best = -1;
        double least = Double.POSITIVE_INFINITY;
        for (int i = 0; i < until.size(); i++) {
            int p = until.get(i);
            if (piece.partHp(p) <= 0 || piece.partWrecked(p)) {
                continue;
            }
            double score;
            if (walls != null && walls.rides(p)) {
                if (!reachable(sortie, piece, p)) {
                    continue;
                }
                score = piece.partHp(p) + 0.5 * Math.abs(aimX(sortie, piece, p) - shipX);
            } else {
                boolean open = !piece.partShielded(p);
                score = (open ? 0 : 1e6) + (open ? piece.partHp(p) : 0) + Math.abs(piece.partX(p) - shipX);
            }
            if (score < least) {
                least = score;
                best = p;
            }
        }
        return best;
    }

    /** The hull's half width ({@code across}) or half height about the ship's centre: its parts' extent, px. */
    static double half(Hull hull, boolean across) {
        double most = 0;
        for (int i = 0; i < hull.parts().size(); i++) {
            Hull.Part part = hull.parts().get(i);
            most = Math.max(
                    most,
                    across
                            ? Math.abs(part.dx()) + part.box().width() / 2
                            : Math.abs(part.dy()) + part.box().height() / 2);
        }
        return most;
    }

    /** Whether a beam from below reaches node {@code p}: no living node lower on the same wall. */
    static boolean reachable(Sortie sortie, SetPiece piece, int p) {
        ArenaWalls walls = sortie.arenaWalls();
        boolean left = walls.nodeX(p) < PlayField.WIDTH / 2.0;
        for (int q = 0; q < piece.partCount(); q++) {
            if (q == p || !walls.rides(q) || piece.partHp(q) <= 0 || piece.partWrecked(q)) {
                continue;
            }
            boolean sameSide = (walls.nodeX(q) < PlayField.WIDTH / 2.0) == left;
            if (sameSide && walls.nodeY(q) < walls.nodeY(p)) {
                return false;
            }
        }
        return true;
    }

    /** The beam's half width, px (the plan's Ion Beam L5: 14 px). */
    static final double BEAM_HALF = 7;

    /** The column's tolerance kept inside a part's box, px. */
    static final double INSIDE = 6;

    /**
     * The column's half width about {@link #aimX} where its beam still meets part {@code p}: a node's few px (its wall's
     * face on one side), any other part's box half width and the beam's, less {@link #INSIDE}.
     */
    static double column(Sortie sortie, SetPiece piece, int p) {
        ArenaWalls walls = sortie.arenaWalls();
        if (walls != null && walls.rides(p)) {
            return COLUMN;
        }
        return Math.max(COLUMN, piece.spec().parts().get(p).box().width() / 2 + BEAM_HALF - INSIDE);
    }

    /** An armour patch on the field (the Heart's phases' drops), null for none. */
    static Pickup patch(Sortie sortie) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            Pickup pickup = sortie.pickup(i);
            if (pickup.type() == PickupType.ARMOUR_PATCH) {
                return pickup;
            }
        }
        return null;
    }

    /** How far under a wall node it stands, px (its burst's flight time to dodge in). */
    static final double UNDER_NODE = 120;

    /** The lowest it stands, y up, px. */
    static final double LOWEST = 70;

    /** Its height for part {@code p}: {@link #UNDER_NODE} under a wall node, else {@link #STAND_Y}. */
    static double standY(Sortie sortie, SetPiece piece, int p) {
        ArenaWalls walls = sortie.arenaWalls();
        if (walls != null && walls.rides(p)) {
            return Math.max(LOWEST, Math.min(STAND_Y, walls.nodeY(p) - UNDER_NODE));
        }
        return STAND_Y;
    }

    /** The column x it stands in for part {@code p}: a node's, just off its wall's face; any other part's centre. */
    static double aimX(Sortie sortie, SetPiece piece, int p) {
        ArenaWalls walls = sortie.arenaWalls();
        if (walls != null && walls.rides(p)) {
            double face = walls.nodeX(p);
            double hull = half(sortie.ship().spec().hull(), true);
            double off = hull + FACE_GAP + 2;
            return face < PlayField.WIDTH / 2.0 ? face + off : face - off;
        }
        return piece.partX(p);
    }

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs beyond {@link RingPlay#cost}: each sample with the
     * hull on a wall (or within {@link #FACE_GAP} of its face), its centre in an armed spore's reach, its top over the
     * Heart's body.
     */
    static double hazards(Sortie sortie, double hull, double dx, double dy) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double halfH = half(sortie.ship().spec().hull(), false);
        ArenaWalls walls = sortie.arenaWalls();
        double cost = 0;
        for (int k = 1; k <= RingPlay.SAMPLES; k++) {
            double t = k * RingPlay.SAMPLE;
            double x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            double y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double weight = 1 - 0.5 * (k - 1) / RingPlay.SAMPLES;
            if (walls != null && walls.standing()) {
                double lo = Math.max(walls.leftAt(y - halfH), walls.leftAt(y + halfH));
                double hi = Math.min(walls.rightAt(y - halfH), walls.rightAt(y + halfH));
                if (x - hull < lo + FACE_GAP || x + hull > hi - FACE_GAP) {
                    cost += weight * ON_WALL;
                }
            }
            if (y + halfH > BODY_FLOOR) {
                cost += weight * ON_BODY;
            }
            for (int i = 0; i < sortie.mineCount(); i++) {
                Mine mine = sortie.mine(i);
                if (!mine.armed()) {
                    continue;
                }
                double mx = mine.renderX(1) - x;
                double my = mine.renderY(1) - y;
                if (mx * mx + my * my < SPORE_REACH * SPORE_REACH) {
                    cost += weight * IN_SPORE;
                }
            }
        }
        return cost;
    }
}
