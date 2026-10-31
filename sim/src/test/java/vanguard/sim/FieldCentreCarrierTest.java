package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part F (design/campaign Level 26, the shield cell of the constellation; m8f-gaps.md §5 F10): a field's centre
 * pickup ({@link WaveSpec.Carried#CENTRE}) rides the unit nearest the field's centre where the scatter put it, and the
 * scatter is the one it always was.
 */
class FieldCentreCarrierTest {
    private static final WaveSpec.Field AREA = new WaveSpec.Field(240, 400, 300, 60, 0);

    private static WaveSpec field(List<WaveSpec.Carried> carried, WaveSpec.Formation formation) {
        return WaveSpec.builder(108, formation, GlowAnglerTest.ANGLER, 8, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .carried(carried)
                .field(Optional.of(AREA))
                .build();
    }

    private static List<Spawn> plan(WaveSpec wave, long seed) {
        List<Spawn> out = new ArrayList<>();
        Formations.plan(wave, 0, new SplitMix64(seed), out);
        return out;
    }

    @Test
    void theUnitNearestTheCentreCarriesIt() {
        for (long seed = 1; seed <= 20; seed++) {
            List<Spawn> plain = plan(field(List.of(), WaveSpec.Formation.FIELD), seed);
            List<Spawn> carried = plan(
                    field(
                            List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, WaveSpec.Carried.CENTRE)),
                            WaveSpec.Formation.FIELD),
                    seed);
            assertEquals(plain.size(), carried.size());
            int nearest = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int i = 0; i < carried.size(); i++) {
                Spawn.Field at = carried.get(i).field().orElseThrow();
                assertEquals(plain.get(i).field().orElseThrow(), at, "the same scatter with a centre pickup");
                double distance = Math.hypot(at.x() - AREA.x(), at.y() - PlayField.HEIGHT - AREA.height() / 2);
                if (distance < best) {
                    best = distance;
                    nearest = i;
                }
            }
            for (int i = 0; i < carried.size(); i++) {
                assertEquals(
                        i == nearest ? Optional.of(PickupType.SHIELD_CELL) : Optional.empty(),
                        carried.get(i).carried(),
                        "seed " + seed + ", unit " + i);
            }
            assertTrue(nearest >= 0);
        }
    }

    @Test
    void onlyAFieldHasACentre() {
        assertThrows(
                IllegalArgumentException.class,
                () -> field(
                        List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, WaveSpec.Carried.CENTRE)),
                        WaveSpec.Formation.SINGLE));
    }
}
