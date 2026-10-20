package vanguard.sim;

/** Where a set piece's centre is at {@code t} level seconds: play-field px, y up. */
public record Waypoint(double t, double x, double y) {}
