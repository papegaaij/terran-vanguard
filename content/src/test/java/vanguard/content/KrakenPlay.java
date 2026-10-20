package vanguard.content;

import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * The autopilot against an arena boss with slam lanes (M5 part E, Level 11's Harbour Kraken, see {@link Autopilot}):
 * the lanes kept clear and, played {@link Autopilot.ArenaPlay#ARMS arm-first}, the arm to cut and the bait.
 */
final class KrakenPlay {
    private KrakenPlay() {}

    /** M5 part E: its height in an arena with slam lanes, px (low: the fans have room to spread). */
    private static final double ARENA_Y = 70;

    /** M5 part E: the planner counts a lane as struck from this long before its impact, s (a margin). */
    static final double LANE_WARNING = 0.15;

    /** M5 part E: it keeps this far beside a telegraphed lane, px. */
    static final double LANE_GAP = 6;

    /** M5 part E: the present arena boss with slam lanes (engaged or arriving); null without one. */
    static SetPiece arenaBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present()
                    && piece.arena().isPresent()
                    && piece.arena().get().laneCount() > 0) {
                return piece;
            }
        }
        return null;
    }

    /**
     * M5 part E: the move in an arena with slam lanes: the boss planner toward what is exposed, kept
     * beside every telegraphed lane (out of the one it is in first), a move that is still in a lane
     * when it strikes counting as a hit.
     */
    static int arenaMove(Sortie sortie, SetPiece boss, Autopilot.ArenaPlay play) {
        var arena = boss.arena().orElseThrow();
        double shipX = sortie.ship().x();
        double half = MovePlanner.HULL_X + LANE_GAP;
        double wanted = arena.rooted()
                ? rootedX(sortie, boss, shipX, play)
                : play == Autopilot.ArenaPlay.ARMS ? armsFirstX(sortie, boss, shipX) : exposedX(boss, shipX);
        double targetX = clearOfLanes(arena, wanted, half, shipX);
        if (laneUnder(arena, arena.telegraphed(), shipX, half) > 0) {
            // Out of the lane first, toward the nearer side clear of it (the target's x is beside it).
            targetX = clearOfLanes(arena, shipX, half, shipX);
        }
        return MovePlanner.bossMove(sortie, targetX, ARENA_Y, arena);
    }

    /**
     * Round 40 (the rooted arms, design/enemies/bosses/harbour-kraken): what to line up under. Arm-first, the
     * nearest living root over a lane holding a convoy ship a slam can still sink, its lane not telegraphed; then
     * (both plays) the surfaced head's eye clear of the telegraphed lanes while it is up; else the nearest living
     * root clear of them (the roots are always on the surface); else the head (waiting for it).
     */
    private static double rootedX(Sortie sortie, SetPiece boss, double shipX, Autopilot.ArenaPlay play) {
        var arena = boss.arena().orElseThrow();
        if (play == Autopilot.ArenaPlay.ARMS) {
            double x = nearestRootX(boss, shipX, shipLanes(sortie));
            if (!Double.isNaN(x)) {
                return x;
            }
        }
        if (arena.surfaceState() == vanguard.sim.SlamArena.SurfaceState.UP) {
            return clearEyeX(boss, shipX);
        }
        double x = nearestRootX(boss, shipX, ~0);
        return Double.isNaN(x) ? exposedX(boss, shipX) : x;
    }

    /** Round 40: the x of the living root nearest the ship over one of {@code lanes} not telegraphed; NaN without. */
    private static double nearestRootX(SetPiece boss, double shipX, int lanes) {
        var arena = boss.arena().orElseThrow();
        double best = Double.NaN;
        for (int a = 0; a < arena.armCount(); a++) {
            int part = arena.armPart(a);
            if (boss.partWrecked(part)
                    || (arena.armLanes(a) & lanes) == 0
                    || (arena.armLanes(a) & arena.telegraphed()) != 0) {
                continue;
            }
            double x = boss.partX(part);
            if (Double.isNaN(best) || Math.abs(x - shipX) < Math.abs(best - shipX)) {
                best = x;
            }
        }
        return best;
    }

    /**
     * What to line up under: an arm out of the water (rising or awash) in a lane not about to be
     * slammed, the nearest; else the surfaced head's nearer eye; else the head (waiting for it).
     */
    private static double exposedX(SetPiece boss, double shipX) {
        var arena = boss.arena().orElseThrow();
        double best = Double.NaN;
        for (int a = 0; a < arena.armCount(); a++) {
            int lane = arena.armLane(a);
            if (lane == 0 || boss.partWrecked(arena.armPart(a)) || boss.partLayer(arena.armPart(a)) != Layer.GROUND) {
                continue;
            }
            double x = arena.laneWidth() * (lane - 0.5);
            if ((arena.telegraphed() & (1 << lane)) == 0
                    && (Double.isNaN(best) || Math.abs(x - shipX) < Math.abs(best - shipX))) {
                best = x;
            }
        }
        if (!Double.isNaN(best)) {
            return best;
        }
        int head = arena.surfacePart() >= 0 ? arena.surfacePart() : Autopilot.vitalPart(boss);
        if (head < 0) {
            return shipX;
        }
        double x = boss.partX(head);
        // The eyes sit either side of the head's centre (the data's weak spots).
        double eye = EYE_DX;
        return Math.abs(shipX - (x - eye)) <= Math.abs(shipX - (x + eye)) ? x - eye : x + eye;
    }

    /**
     * M5 part E, {@link Autopilot.ArenaPlay#ARMS}: what to line up under. The arm to cut is the living one
     * guarding the most ships afloat (the fewer HP on a tie). While it must be cut: an arm lying awash
     * past its impact, that one first; else (between single slams) beside the lane an arm is about to
     * strike (the clamp beside the telegraphed lanes puts it there), that one first; else, while the
     * head's window is open, its eye clear of the telegraphed lanes; else the bait: where the next slam
     * costs least. Once a safe place exists it only baits and plays the head (its eyes also while it
     * rises); with a cut out of reach, as {@link Autopilot.ArenaPlay#EYES}.
     */
    private static double armsFirstX(Sortie sortie, SetPiece boss, double shipX) {
        var arena = boss.arena().orElseThrow();
        int cut = armToCut(sortie, boss);
        if (cut < 0) {
            return exposedX(boss, shipX);
        }
        boolean volleys = volleys(boss);
        // Once some place keeps every slam off the ships (the other arm severed and a free lane), the
        // arm need not be cut: it baits there and shoots the head.
        boolean guard = !safePlace(sortie, boss, volleys);
        if (guard && !cuttable(sortie, boss, cut)) {
            // The ships in its half are lost anyway: as the first pilot (an arm when exposed, else the head).
            return exposedX(boss, shipX);
        }
        double x = guard ? awashX(boss, cut) : Double.NaN;
        for (int a = 0; guard && a < arena.armCount() && Double.isNaN(x); a++) {
            x = awashX(boss, a);
        }
        if (!Double.isNaN(x)) {
            return x;
        }
        if (guard && !volleys) {
            x = strikingX(boss, cut);
            for (int a = 0; a < arena.armCount() && Double.isNaN(x); a++) {
                x = strikingX(boss, a);
            }
            if (!Double.isNaN(x)) {
                return x;
            }
        }
        var head = arena.surfaceState();
        if ((head == vanguard.sim.SlamArena.SurfaceState.UP
                        || (head == vanguard.sim.SlamArena.SurfaceState.RISING && !guard))
                && (!volleys || arena.telegraphed() != 0)) {
            return clearEyeX(boss, shipX);
        }
        return baitX(sortie, boss, guard ? cut : -1, shipX);
    }

    /** Whether some place keeps the next slam (or volley) off every convoy ship afloat. */
    private static boolean safePlace(Sortie sortie, SetPiece boss, boolean volley) {
        var arena = boss.arena().orElseThrow();
        for (int lane = 1; lane <= arena.laneCount(); lane++) {
            for (int place = 0; place < 3; place++) {
                double x = (lane - 0.5) * arena.laneWidth()
                        + (place - 1) * (arena.laneWidth() / 2 - MovePlanner.HULL_X - LANE_GAP);
                double cost = volley ? volleyCost(sortie, boss, -1, x) : singleCost(sortie, boss, -1, x);
                if (cost <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The damage it reckons to deal an arm lying awash once, for {@link #cuttable}. */
    private static final double CUT_PER_SLAM = 70;
    /** ... and the slams beyond the ships' spare ones it allows for (the free lanes' and the last one's). */
    private static final double CUT_SLACK = 2;

    /**
     * Whether arm {@code cut} can be cut before its half's ships afloat have taken their last slam but
     * one: the slams its HP take at {@link #CUT_PER_SLAM} against the slams they can spare (each slam
     * in its half, all of them ships' lanes, is one the arm lies awash in). Otherwise the ships are lost
     * anyway and it plays the head.
     */
    private static boolean cuttable(Sortie sortie, SetPiece boss, int cut) {
        var arena = boss.arena().orElseThrow();
        double spare = 0;
        for (int k = 0; k < sortie.allyCount(); k++) {
            int lane = sortie.allyLane(k);
            if (lane > 0 && (arena.armLanes(cut) & (1 << lane)) != 0 && slammable(sortie, k)) {
                spare += Math.max(0, slamsLeft(sortie, k) - 1);
            }
        }
        return Math.ceil(boss.partHp(arena.armPart(cut)) / CUT_PER_SLAM) <= spare + CUT_SLACK;
    }

    /** The surfaced head's eye whose line is clear of the telegraphed lanes, the nearer if both are (or neither is). */
    private static double clearEyeX(SetPiece boss, double shipX) {
        var arena = boss.arena().orElseThrow();
        double left = eye(boss, -1);
        double right = eye(boss, 1);
        if (Double.isNaN(left) || Double.isNaN(right)) {
            return exposedX(boss, shipX);
        }
        double half = MovePlanner.HULL_X + LANE_GAP;
        boolean leftClear = laneUnder(arena, arena.telegraphed(), left, half) == 0;
        boolean rightClear = laneUnder(arena, arena.telegraphed(), right, half) == 0;
        if (leftClear != rightClear) {
            return leftClear ? left : right;
        }
        return Math.abs(shipX - left) <= Math.abs(shipX - right) ? left : right;
    }

    /** Whether the boss's phase now slams in volleys (the head stays up between them). */
    private static boolean volleys(SetPiece boss) {
        var phases = boss.boss().orElseThrow().phases();
        return boss.phase() < phases.size()
                && phases.get(boss.phase()).arena().slamming() == vanguard.sim.BossSpec.Slamming.VOLLEY;
    }

    /** The x of arm {@code a}'s lane while it lies out of the water past its impact; NaN otherwise. */
    private static double awashX(SetPiece boss, int a) {
        var arena = boss.arena().orElseThrow();
        int lane = arena.armLane(a);
        if (lane == 0
                || boss.partWrecked(arena.armPart(a))
                || boss.partLayer(arena.armPart(a)) != Layer.GROUND
                || (arena.telegraphed() & (1 << lane)) != 0) {
            return Double.NaN;
        }
        return arena.laneWidth() * (lane - 0.5);
    }

    /** The x of the lane arm {@code a} is about to strike (telegraphed or rising); NaN otherwise. */
    private static double strikingX(SetPiece boss, int a) {
        var arena = boss.arena().orElseThrow();
        var state = arena.armState(a);
        if (boss.partWrecked(arena.armPart(a))
                || (state != vanguard.sim.SlamArena.ArmState.TELEGRAPH
                        && state != vanguard.sim.SlamArena.ArmState.RISE)) {
            return Double.NaN;
        }
        return arena.laneWidth() * (arena.armFirstLane(a) - 0.5);
    }

    /**
     * The living slam arm guarding the most convoy ships afloat (the fewer HP on a tie); -1 when both
     * are severed or no living arm's half holds a ship afloat.
     */
    private static int armToCut(Sortie sortie, SetPiece boss) {
        var arena = boss.arena().orElseThrow();
        int best = -1;
        int bestShips = -1;
        for (int a = 0; a < arena.armCount(); a++) {
            int part = arena.armPart(a);
            if (boss.partWrecked(part)) {
                continue;
            }
            int ships = Integer.bitCount(shipLanes(sortie) & arena.armLanes(a));
            if (ships > bestShips || (ships == bestShips && boss.partHp(part) < boss.partHp(arena.armPart(best)))) {
                best = a;
                bestShips = ships;
            }
        }
        // With no ship afloat in a living arm's half there is nothing left to guard: the head first.
        return bestShips > 0 ? best : -1;
    }

    /** The lanes holding a convoy ship a slam can still sink, bit {@code n} for lane {@code n}. */
    private static int shipLanes(Sortie sortie) {
        int mask = 0;
        for (int k = 0; k < sortie.allyCount(); k++) {
            if (slammable(sortie, k) && sortie.allyLane(k) > 0) {
                mask |= 1 << sortie.allyLane(k);
            }
        }
        return mask;
    }

    private static boolean slammable(Sortie sortie, int k) {
        return sortie.ally(k).alive() && sortie.allySpec(k).slams() > 0;
    }

    /** The slams convoy ship {@code k} still takes before it sinks (0 when it cannot be slammed). */
    private static double slamsLeft(Sortie sortie, int k) {
        if (!slammable(sortie, k)) {
            return 0;
        }
        return (sortie.allySpec(k).hp() - sortie.ally(k).hitsTaken())
                / sortie.allySpec(k).slams();
    }

    /**
     * Where the next slam (or volley) costs least while arm {@code cut} lives. It weighs a few places
     * (each lane's centre and edges, the surfaced head's eyes): the lanes the boss's rules would slam
     * from there (design/enemies/bosses/harbour-kraken: a single slam takes the ship's lane or the
     * convoy ship's nearest it in turn, a choice in a severed half going to the other; a volley takes
     * both, an arm with no target its half's lane nearest the ship), each lane by what its ship afloat
     * would lose (a ship on its last slam far more), less a little for each slam of the arm to cut
     * (cut there when it lies awash), plus a little for the way there.
     */
    private static double baitX(Sortie sortie, SetPiece boss, int cut, double shipX) {
        var arena = boss.arena().orElseThrow();
        boolean volley = volleys(boss);
        double best = shipX;
        double bestScore = Double.POSITIVE_INFINITY;
        int lanes = arena.laneCount();
        for (int c = 0; c < 3 * lanes + 2; c++) {
            double x;
            if (c < 3 * lanes) {
                int lane = c / 3 + 1;
                int place = c % 3;
                x = (lane - 0.5) * arena.laneWidth()
                        + (place - 1) * (arena.laneWidth() / 2 - MovePlanner.HULL_X - LANE_GAP);
            } else {
                x = eye(boss, c == 3 * lanes ? -1 : 1);
                if (Double.isNaN(x)) {
                    continue;
                }
            }
            double score = volley ? volleyCost(sortie, boss, cut, x) : singleCost(sortie, boss, cut, x);
            score += Math.abs(x - shipX) / BAIT_TRAVEL;
            if (c >= 3 * lanes && arena.surfaceState() == vanguard.sim.SlamArena.SurfaceState.UP) {
                score -= BAIT_EYE;
            }
            if (score < bestScore) {
                bestScore = score;
                best = x;
            }
        }
        return best;
    }

    /** It weighs a place's way there at one point per this many px. */
    private static final double BAIT_TRAVEL = 120;
    /** An open eye to shoot while it waits is worth this many points. */
    private static final double BAIT_EYE = 2.5;
    /** A slam of the arm to cut is worth this many points (it lies awash, ready to be cut). */
    private static final double BAIT_CUT = 3;

    /** The x of the surfaced head's eye on {@code side} (-1 left, 1 right) while it is rising or up; NaN otherwise. */
    private static double eye(SetPiece boss, int side) {
        var arena = boss.arena().orElseThrow();
        var state = arena.surfaceState();
        if (arena.surfacePart() < 0
                || (state != vanguard.sim.SlamArena.SurfaceState.UP
                        && state != vanguard.sim.SlamArena.SurfaceState.RISING)) {
            return Double.NaN;
        }
        return boss.partX(arena.surfacePart()) + side * EYE_DX;
    }

    /** A single slam from x: the worse of its two choices (the alternate rule's turn is not known). */
    private static double singleCost(Sortie sortie, SetPiece boss, int cut, double x) {
        var arena = boss.arena().orElseThrow();
        int player = arena.laneOf(x);
        int ship = nearestShipLane(sortie, arena, x);
        double worst = Double.NEGATIVE_INFINITY;
        for (int turn = 0; turn < 2; turn++) {
            int first = turn == 0 || ship == 0 ? player : ship;
            int other = turn == 0 ? ship : player;
            int lane = first;
            int arm = livingOwner(boss, first);
            if (arm < 0 && other > 0 && livingOwner(boss, other) >= 0) {
                lane = other;
                arm = livingOwner(boss, other);
            }
            if (arm < 0) {
                arm = firstLivingArm(boss);
                lane = arm < 0 ? 0 : nearestOwnedLane(arena, arm, x);
            }
            double cost = slamCost(sortie, lane) - (arm == cut ? BAIT_CUT : 0);
            worst = Math.max(worst, cost);
        }
        return worst;
    }

    /** A volley from x: the ship's lane and the nearest convoy ship's, each by its living owner; an arm without one, its lane nearest x. */
    private static double volleyCost(Sortie sortie, SetPiece boss, int cut, double x) {
        var arena = boss.arena().orElseThrow();
        int player = arena.laneOf(x);
        int ship = nearestShipLane(sortie, arena, x);
        double cost = 0;
        for (int a = 0; a < arena.armCount(); a++) {
            if (boss.partWrecked(arena.armPart(a))) {
                continue;
            }
            int mine = arena.armLanes(a);
            int lane = (mine & (1 << player)) != 0 ? player : ship > 0 && (mine & (1 << ship)) != 0 ? ship : 0;
            if (lane == 0) {
                lane = nearestOwnedLane(arena, a, x);
            }
            cost += slamCost(sortie, lane) - (a == cut ? BAIT_CUT : 0);
        }
        return cost;
    }

    /** What a slam in {@code lane} costs: what its ship afloat loses, a ship on its last slam far more; 0 without one. */
    private static double slamCost(Sortie sortie, int lane) {
        double cost = 0;
        for (int k = 0; k < sortie.allyCount(); k++) {
            if (lane > 0 && sortie.allyLane(k) == lane && slammable(sortie, k)) {
                cost = Math.max(cost, slamsLeft(sortie, k) <= 1 ? 100 : 10 - slamsLeft(sortie, k));
            }
        }
        return cost;
    }

    /** The lane of the convoy ship afloat nearest x (the lower on a tie); 0 for none. */
    private static int nearestShipLane(Sortie sortie, vanguard.sim.SlamArena arena, double x) {
        int ships = shipLanes(sortie);
        int best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int lane = 1; lane <= arena.laneCount(); lane++) {
            double distance = Math.abs((lane - 0.5) * arena.laneWidth() - x);
            if ((ships & (1 << lane)) != 0 && distance < bestDistance) {
                bestDistance = distance;
                best = lane;
            }
        }
        return best;
    }

    /** Arm {@code a}'s lane whose centre is nearest x (the lower on a tie). */
    private static int nearestOwnedLane(vanguard.sim.SlamArena arena, int a, double x) {
        int best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int lane = 1; lane <= arena.laneCount(); lane++) {
            double distance = Math.abs((lane - 0.5) * arena.laneWidth() - x);
            if ((arena.armLanes(a) & (1 << lane)) != 0 && distance < bestDistance) {
                bestDistance = distance;
                best = lane;
            }
        }
        return best;
    }

    /** The living arm that owns {@code lane}; -1 for none. */
    private static int livingOwner(SetPiece boss, int lane) {
        var arena = boss.arena().orElseThrow();
        for (int a = 0; a < arena.armCount(); a++) {
            if ((arena.armLanes(a) & (1 << lane)) != 0 && !boss.partWrecked(arena.armPart(a))) {
                return a;
            }
        }
        return -1;
    }

    private static int firstLivingArm(SetPiece boss) {
        var arena = boss.arena().orElseThrow();
        for (int a = 0; a < arena.armCount(); a++) {
            if (!boss.partWrecked(arena.armPart(a))) {
                return a;
            }
        }
        return -1;
    }

    /** M5 part E: the Kraken's eyes lie this far either side of its head's centre, px (its data's spots). */
    private static final double EYE_DX = 41.5;

    /** A telegraphed lane the hull (half width {@code half}) at x overlaps; 0 for none. */
    private static int laneUnder(vanguard.sim.SlamArena arena, int mask, double x, double half) {
        for (int lane = 1; lane <= arena.laneCount(); lane++) {
            double left = (lane - 1) * arena.laneWidth();
            if ((mask & (1 << lane)) != 0 && x + half > left && x - half < left + arena.laneWidth()) {
                return lane;
            }
        }
        return 0;
    }

    /**
     * An x beside every telegraphed lane: x itself when clear, else beside the run of telegraphed
     * lanes it lies in, on the side nearer {@code from} that is on the play field.
     */
    private static double clearOfLanes(vanguard.sim.SlamArena arena, double x, double half, double from) {
        int mask = arena.telegraphed();
        int lane = laneUnder(arena, mask, x, half);
        if (lane == 0) {
            return x;
        }
        int lo = lane;
        int hi = lane;
        while (lo > 1 && (mask & (1 << (lo - 1))) != 0) {
            lo--;
        }
        while (hi < arena.laneCount() && (mask & (1 << (hi + 1))) != 0) {
            hi++;
        }
        double left = (lo - 1) * arena.laneWidth() - half;
        double right = hi * arena.laneWidth() + half;
        boolean leftOk = left >= 30;
        boolean rightOk = right <= PlayField.WIDTH - 30;
        if (leftOk && (!rightOk || Math.abs(left - from) <= Math.abs(right - from))) {
            return left;
        }
        return rightOk ? right : x;
    }
}
