package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part G, step G1 (design/campaign Level 34, "Strip the graveyard"; m9g-gaps.md §1 #3): an escapes secondary over
 * ground objects: the destructibles of a look (and its variants) count, a destroyed one as opened, one leaving the bottom
 * edge as away; met as soon as all but the allowed are opened, failed by the away one past the allowed (medium 1, easy 2,
 * hard 0).
 */
class ObjectEscapesTest {
    private static final int FIRE = Command.FIRE.bit();

    /** A cargo container of {@code look} at {@code t}, {@code x}: 12 HP, no bounty, a medium salvage pickup. */
    static GroundObjectSpec container(double t, double x, String look) {
        return new GroundObjectSpec(
                t,
                x,
                new Hitbox(24, 24),
                12,
                0,
                Optional.of(PickupType.MEDIUM_SALVAGE),
                0,
                0,
                "",
                false,
                -1,
                1,
                Optional.empty(),
                look);
    }

    /**
     * Six containers (CDF and a Helix variant), {@code shot} of them at x 240 in the ship's line of fire, the rest at x
     * 420, one a second from 1 s; a clamp of another look among them; 130 px/s for 20 s.
     */
    static LevelScript graveyard(int shot, Secondary secondary) {
        List<GroundObjectSpec> objects = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String look = i % 2 == 0 ? GroundObjectSpec.CARGO_CONTAINER : "cargo-container-helix";
            objects.add(container(1 + i, i < shot ? 240 : 420, look));
        }
        objects.add(container(1.5, 240, "hold-clamp"));
        return LevelScript.builder(34, 5)
                .sections(List.of(new Section(20, 130)))
                .groundObjects(objects)
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .build();
    }

    static Secondary containers(int allowed) {
        return Secondary.objectEscapes(GroundObjectSpec.CARGO_CONTAINER, allowed, 100, "CONTAINERS");
    }

    /** Flies the level to its end firing from x 240; returns the sortie. */
    static Sortie fly(int shot, Secondary secondary, List<int[]> counts) {
        Sortie sortie = new Sortie(
                1,
                Act5ESpecs.loadout(Act5ESpecs.bolt(20, false)),
                graveyard(shot, secondary),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        for (int step = 0; step < SimStep.ticks(21) && !sortie.complete(); step++) {
            sortie.step(FIRE);
            if (sortie.events().count(SimEvents.Type.OBJECTIVE_MET) > 0) {
                counts.add(new int[] {1, sortie.escapesDestroyed(), sortie.escapesLost()});
            }
            if (sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED) > 0) {
                counts.add(new int[] {0, sortie.escapesDestroyed(), sortie.escapesLost()});
            }
        }
        assertTrue(sortie.complete(), "the level's end");
        return sortie;
    }

    @Test
    void itCountsTheLooksDestructibles_variantsIncluded_notTheClamp() {
        Sortie sortie = fly(6, containers(1), new ArrayList<>());
        assertTrue(sortie.secondaryByEscapes());
        assertEquals(6, sortie.escapesTotal());
        assertEquals(6, sortie.escapesDestroyed(), "all six opened (the clamp shot too, not counted)");
        assertEquals(0, sortie.escapesLost());
    }

    @Test
    void mediumFiveOfSix_metAtTheFifth_theSixthAwayFailsNothing() {
        List<int[]> outcomes = new ArrayList<>();
        Sortie sortie = fly(5, containers(1), outcomes);
        assertEquals(1, outcomes.size(), "one outcome");
        assertEquals(1, outcomes.get(0)[0], "met");
        assertEquals(5, outcomes.get(0)[1], "at the fifth opened");
        assertTrue(sortie.secondaryMet());
        assertFalse(sortie.secondaryFailed());
        assertEquals(5, sortie.escapesDestroyed());
        assertEquals(1, sortie.escapesLost(), "the sixth away, still counted");
    }

    @Test
    void mediumFourOfSix_failedByTheSecondAway() {
        List<int[]> outcomes = new ArrayList<>();
        Sortie sortie = fly(4, containers(1), outcomes);
        assertEquals(1, outcomes.size());
        assertEquals(0, outcomes.get(0)[0], "failed");
        assertEquals(2, outcomes.get(0)[2], "by the second away");
        assertFalse(sortie.secondaryMet());
        assertTrue(sortie.secondaryFailed());
    }

    @Test
    void easyFourOfSixMeetsIt_hardNeedsAllSix() {
        Sortie easy = fly(4, containers(2), new ArrayList<>());
        assertTrue(easy.secondaryMet(), "easy: 4 of 6");
        List<int[]> outcomes = new ArrayList<>();
        Sortie hard = fly(5, containers(0), outcomes);
        assertFalse(hard.secondaryMet());
        assertEquals(0, outcomes.get(0)[0], "hard: the first away fails it");
        assertEquals(1, outcomes.get(0)[2]);
        assertTrue(fly(6, containers(0), new ArrayList<>()).secondaryMet(), "hard: all six");
    }

    @Test
    void itPaysTheSecondaryOnce() {
        int none = fly(5, Secondary.NONE, new ArrayList<>()).credits();
        int met = fly(5, containers(1), new ArrayList<>()).credits();
        assertTrue(met > none, "the secondary's credits");
    }

    @Test
    void anObjectEscapesObjectiveCountsNoUnitAndNothingElse() {
        Secondary secondary = containers(1);
        assertFalse(secondary.counts(GroundObjectSpec.CARGO_CONTAINER));
        assertFalse(secondary.counts(GroundObjectSpec.CARGO_CONTAINER, ""));
        assertTrue(secondary.countsObject("cargo-container"));
        assertTrue(secondary.countsObject("cargo-container-helix"));
        assertFalse(secondary.countsObject("cargo-containers"));
        assertFalse(secondary.countsObject("hold-clamp"));
        assertFalse(new Secondary(0, 100, List.of(), "cargo-container").countsObject("cargo-container"));
        assertTrue(secondary.withBossesCounted(true).objects(), "kept by the copies");
        assertEquals(2, secondary.withEscapesAllowed(2).escapesAllowed());
        assertTrue(secondary.withEscapesAllowed(2).objects());
        assertThrows(IllegalArgumentException.class, () -> Secondary.objectEscapes("", 1, 100, "CONTAINERS"));
    }
}
