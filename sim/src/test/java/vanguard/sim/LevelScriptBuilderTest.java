package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Refactoring step LS-1: {@link LevelScript.Builder} builds the same levels as the canonical constructor and the withers. */
class LevelScriptBuilderTest {
    private static final List<Section> SECTIONS = List.of(new Section(60, 40));

    @Test
    void builderDefaultsAreAnEmptyLevel() {
        LevelScript built = LevelScript.builder(4, 1)
                .launchSeconds(3)
                .sections(SECTIONS)
                .secrets(2)
                .build();
        LevelScript constructed = new LevelScript(
                4,
                1,
                3,
                false,
                SECTIONS,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                2,
                List.of(),
                ObjectiveSpec.NONE,
                GroundSpec.NONE,
                HazardSpec.NONE,
                DropSpec.NONE);
        assertEquals(constructed, built);
        assertEquals(constructed, constructed.toBuilder().build());
    }

    @Test
    void buildValidatesAndSortsOnce() {
        assertThrows(
                IllegalArgumentException.class, () -> LevelScript.builder(1, 1).build());
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(1, 1)
                .sections(SECTIONS)
                .drops(DropSpec.NONE.withDropFactor(0))
                .build());
        DropSpec.PlacedPickup late = new DropSpec.PlacedPickup(20, 100, PickupType.SMALL_SALVAGE);
        DropSpec.PlacedPickup early = new DropSpec.PlacedPickup(10, 100, PickupType.SMALL_SALVAGE);
        LevelScript level = LevelScript.builder(1, 1)
                .sections(SECTIONS)
                .drops(DropSpec.NONE.withPlacedPickups(List.of(late, early)).withDropFactor(1.5))
                .build();
        assertEquals(List.of(early, late), level.drops().placedPickups());
        assertEquals(1.5, level.drops().dropFactor());
    }

    @Test
    void subRecordsHoldTheirParts() {
        DropSpec.PlacedPickup late = new DropSpec.PlacedPickup(20, 100, PickupType.SMALL_SALVAGE);
        DropSpec.PlacedPickup early = new DropSpec.PlacedPickup(10, 100, PickupType.SMALL_SALVAGE);
        DropSpec drops = DropSpec.NONE.withDropFactor(1.5).withPlacedPickups(List.of(late, early));
        assertEquals(List.of(early, late), drops.placedPickups());
        assertThrows(IllegalArgumentException.class, () -> DropSpec.NONE.withDropFactor(0));
        LevelScript level = LevelScript.builder(1, 1)
                .sections(SECTIONS)
                .drops(drops)
                .ground(GroundSpec.NONE.withWater(true))
                .build();
        assertEquals(drops, level.drops());
        assertEquals(true, level.ground().water());
        assertEquals(level, level.toBuilder().build());
        assertEquals(ObjectiveSpec.NONE, level.objectives());
        assertEquals(HazardSpec.NONE, level.hazards());
        // A sub-record wither changes its sub-record only.
        LevelScript dry =
                level.toBuilder().ground(level.ground().withWater(false)).build();
        assertEquals(GroundSpec.NONE, dry.ground());
        assertEquals(drops, dry.drops());
    }
}
