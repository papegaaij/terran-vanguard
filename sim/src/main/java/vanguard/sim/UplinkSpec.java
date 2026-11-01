package vanguard.sim;

/**
 * M10 part G, step G1a (design/campaign Level 41, the secondary {@code FILES}; m10g-gaps.md §1 #6): an archive core
 * under an armoured hatch, Varga's uplink spike's target. The hatch is one of the level's ground objects, a plain
 * destructible ({@code hatch}, its index among them): broken (by anything), the core under it is live where the hatch
 * was and scrolls on with the ground; the ship's centre within {@code radius} px of it for {@code seconds} in total
 * (simulation steps while the ship flies; paused outside, never reset) uploads its file. A core that leaves the bottom
 * edge before its file is in (or whose hatch does, unbroken) is lost. Rook never uploads.
 *
 * @param hatch the index of its hatch among the level's ground objects ({@link LevelScript#groundObjects()})
 * @param seconds the time the ship's centre stays within {@code radius} in total, s (Level 41: 2.0; easy 1.5, hard 2.5)
 * @param radius px from the core's centre (Level 41: 40)
 */
public record UplinkSpec(int hatch, double seconds, double radius) {
    public UplinkSpec {
        if (hatch < 0 || !(seconds > 0) || !(radius > 0)) {
            throw new IllegalArgumentException("an uplink names its hatch and takes a time within a radius: " + hatch
                    + ", " + seconds + " s, " + radius + " px");
        }
    }
}
