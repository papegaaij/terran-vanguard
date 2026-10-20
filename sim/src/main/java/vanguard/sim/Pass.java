package vanguard.sim;

import java.util.List;

/**
 * One pass of a set piece: from the first waypoint's time it flies along {@code path} on
 * {@code layer} with a fixed heading. A pass on the player's layer arrives on {@code high-air}
 * and descends at {@code descendAt} over {@code descentSeconds}; at {@code leaveAt} it rises
 * back to {@code high-air} over the same time and leaves straight up through the top edge at
 * {@code leaveSpeed}. Until then it holds at the last waypoint once the path is flown. A pass
 * that does not descend ends with its path.
 *
 * @param headingRadians where it faces, radians clockwise from straight down the screen (as
 *     {@link Enemy#facing()}: facing right is -pi/2)
 * @param descendAt level seconds; infinite for a pass that does not descend
 * @param leaveAt level seconds; infinite for a pass that does not descend
 */
public record Pass(
        String name,
        Layer layer,
        double headingRadians,
        List<Waypoint> path,
        double descendAt,
        double descentSeconds,
        double leaveAt,
        double leaveSpeed) {
    public Pass {
        path = List.copyOf(path);
        if (path.isEmpty()) {
            throw new IllegalArgumentException(name + ": a pass has a path");
        }
        for (int i = 1; i < path.size(); i++) {
            if (!(path.get(i).t() > path.get(i - 1).t())) {
                throw new IllegalArgumentException(name + ": the waypoints are in time order");
            }
        }
        if (Double.isFinite(descendAt) && !(leaveAt > descendAt + descentSeconds && leaveSpeed > 0)) {
            throw new IllegalArgumentException(name + ": it leaves after its descent, at a speed");
        }
    }

    /** A pass that flies its path on one layer and ends with it. */
    public static Pass flight(String name, Layer layer, double headingRadians, List<Waypoint> path) {
        return new Pass(name, layer, headingRadians, path, Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY, 0);
    }

    /** Whether it descends to the player's layer and leaves again. */
    public boolean descends() {
        return Double.isFinite(descendAt);
    }

    /** When it starts, level seconds. */
    public double start() {
        return path.getFirst().t();
    }
}
