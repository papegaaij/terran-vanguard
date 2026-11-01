package vanguard.content;

import vanguard.sim.HazardSpec;
import vanguard.sim.PlayField;

/**
 * M10 part D's level rules for the simulation (design/campaign Level 38 "Lightning Belt"; gap check m10d-gaps.md §1 #8),
 * used by {@link ScriptRules}: a placed debris chunk's {@code drop} (the pickup its latch lets fall when the player's
 * shot breaks it), its {@code opens_at} (round 76 L.1 b: the CDF supply canister has no latch; it opens by itself over
 * the middle and lets its armour patch fall; y from the top, as {@link BeltRules} turns a rock's) and its {@code marked}
 * friendly brackets. The Harrow's dash
 * variant is {@link TugRules#variant}'s; the scoped kill ratio's {@code enemy} is {@link LevelRules}'.
 */
final class StormLayerRules {
    private StormLayerRules() {}

    /** {@code chunk} (the placed chunk's spec) with its drop, its opening and brackets; one without them as it is. */
    static HazardSpec.DebrisSpec debris(LevelData.PlacedChunk placed, HazardSpec.DebrisSpec chunk) {
        HazardSpec.DebrisSpec out = chunk;
        if (placed.drop().isPresent()) {
            out = out.withDrop(SimSpecs.pickup(placed.drop().get()));
        }
        if (placed.opensAt().isPresent()) {
            out = out.withOpensAt(PlayField.HEIGHT - placed.opensAt().get());
        }
        return placed.isMarked() ? out.withMarked(true) : out;
    }
}
