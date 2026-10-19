package vanguard.content;

import vanguard.sim.Layer;

/** The layer names of the design documents ({@code low-air}, …) and the simulation's layers. */
final class Layers {
    private Layers() {}

    static Layer of(String name) {
        return switch (name) {
            case "ground" -> Layer.GROUND;
            case "low-air" -> Layer.LOW_AIR;
            case "air" -> Layer.AIR;
            case "high-air" -> Layer.HIGH_AIR;
            case "sub" -> Layer.SUB;
            // M7 part B (M7 = a): under the sand, hit by nothing (a Burrower's current layer; the threat profile's
            // dominant layers)
            case "buried" -> Layer.BURIED;
            default -> throw new IllegalArgumentException("unknown layer '" + name + "'");
        };
    }
}
