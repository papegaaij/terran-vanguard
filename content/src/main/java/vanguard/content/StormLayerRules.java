package vanguard.content;

import vanguard.sim.HazardSpec;

/**
 * M10 part D's level rules for the simulation (design/campaign Level 38 "Lightning Belt"; gap check m10d-gaps.md §1 #8),
 * used by {@link ScriptRules}: a placed debris chunk's {@code drop} (the pickup its latch lets fall when the player's
 * shot breaks it: the CDF supply canister's armour patch) and its {@code marked} friendly brackets. The Harrow's dash
 * variant is {@link TugRules#variant}'s; the scoped kill ratio's {@code enemy} is {@link LevelRules}'.
 */
final class StormLayerRules {
    private StormLayerRules() {}

    /** {@code chunk} (the placed chunk's spec) with its drop and brackets; one without them as it is. */
    static HazardSpec.DebrisSpec debris(LevelData.PlacedChunk placed, HazardSpec.DebrisSpec chunk) {
        HazardSpec.DebrisSpec out = chunk;
        if (placed.drop().isPresent()) {
            out = out.withDrop(SimSpecs.pickup(placed.drop().get()));
        }
        return placed.isMarked() ? out.withMarked(true) : out;
    }
}
