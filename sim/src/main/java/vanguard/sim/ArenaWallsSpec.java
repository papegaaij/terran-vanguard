package vanguard.sim;

import java.util.List;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, Arena: the walls of phase 2; m11h-gaps.md §1 #3): a boss's
 * arena walls ({@link ArenaWalls}), two walls that grow out of the field's sides in one of its phases and stand as terrain
 * under the canyon-wall rule (Act 3's: contact, push, cooldown; they stop shots, beams and missiles). Each wall is two
 * halves (upper and lower) that move on their own; at most one {@link Node} rides each half's inner face, and its death
 * sends that half back to the field's edge. Screen space in the simulation's frame: x 0–480, y up from the bottom edge.
 *
 * @param phase the boss phase the walls stand in (0 the first): they are told and grow at its start (its opening)
 * @param corridor the corridor between the grown walls, px, centred on the field (x (480 − corridor) / 2 on)
 * @param tellSeconds every move's tell (the veins' glow) before it starts, s
 * @param growSeconds the grow from the field's sides to the corridor after its tell, s
 * @param breatheAmplitude the breath's swing about the corridor's edge, px (±)
 * @param breatheHz the breath's rate, Hz
 * @param mostSpeed the walls' top speed but in the grow (the breath, the retreat), px/s
 * @param clear a half never moves inward while a ship's hull (the player's or Rook's) is nearer its face than this, px
 * @param nodes the wall nodes, at most one a half
 * @param damage the contact's damage per touch
 * @param push how far a touch pushes the hull back, px
 * @param pushSeconds over how long, s
 * @param cooldownSeconds the touch's cooldown (contact in it only clamps), s
 */
public record ArenaWallsSpec(
        int phase,
        double corridor,
        double tellSeconds,
        double growSeconds,
        double breatheAmplitude,
        double breatheHz,
        double mostSpeed,
        double clear,
        List<Node> nodes,
        double damage,
        double push,
        double pushSeconds,
        double cooldownSeconds) {
    public ArenaWallsSpec {
        nodes = List.copyOf(nodes);
        if (phase < 0
                || !(corridor > 0 && corridor < PlayField.WIDTH)
                || !(tellSeconds >= 0)
                || !(growSeconds > 0)
                || !(breatheAmplitude >= 0)
                || !(breatheHz > 0)
                || !(mostSpeed > 0)
                || !(clear >= 0)
                || !(damage >= 0)
                || !(push >= 0)
                || !(pushSeconds > 0)
                || !(cooldownSeconds >= 0)) {
            throw new IllegalArgumentException(
                    "arena walls stand in a phase, leave a corridor, grow, breathe and touch");
        }
        double split = split(nodes);
        boolean[] taken = new boolean[ArenaWalls.HALVES];
        for (Node node : nodes) {
            int half = node.half(split);
            if (taken[half]) {
                throw new IllegalArgumentException("arena walls carry at most one node a half: " + nodes);
            }
            taken[half] = true;
        }
    }

    /**
     * Where a wall's upper half meets its lower half, y up: midway between the lowest and the highest node (the
     * field's middle without two nodes at different heights).
     */
    static double split(List<Node> nodes) {
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        for (Node node : nodes) {
            low = Math.min(low, node.y());
            high = Math.max(high, node.y());
        }
        return high > low ? (low + high) / 2 : PlayField.HEIGHT / 2.0;
    }

    /** These walls closing to another {@code width} (the difficulty's corridor: easy 360, hard 300). */
    public ArenaWallsSpec withCorridor(double width) {
        return new ArenaWallsSpec(
                phase,
                width,
                tellSeconds,
                growSeconds,
                breatheAmplitude,
                breatheHz,
                mostSpeed,
                clear,
                nodes,
                damage,
                push,
                pushSeconds,
                cooldownSeconds);
    }

    /** These walls with another contact {@code damage} (easy 6). */
    public ArenaWallsSpec withDamage(double contact) {
        return new ArenaWallsSpec(
                phase,
                corridor,
                tellSeconds,
                growSeconds,
                breatheAmplitude,
                breatheHz,
                mostSpeed,
                clear,
                nodes,
                contact,
                push,
                pushSeconds,
                cooldownSeconds);
    }

    /**
     * A wall node: the boss's part {@code part} riding the inner face of the left wall ({@code left}) or the right one,
     * its centre on the face at height {@code y} (y up); which half it rides follows from its height.
     */
    public record Node(int part, boolean left, double y) {
        public Node {
            if (part < 0 || !(y >= 0 && y <= PlayField.HEIGHT)) {
                throw new IllegalArgumentException("a wall node is a part on the field: " + part + ", " + y);
            }
        }

        /** Its half: 0 the left wall's upper, 1 its lower, 2 the right wall's upper, 3 its lower. */
        int half(double split) {
            return (left ? 0 : 2) + (y > split ? 0 : 1);
        }
    }
}
