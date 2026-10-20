package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part B, step B1: a moving section's ease (design/campaign Level 15: "each change eased over 1 s")
 * and the drop launch (the ship and Rook drop from the transport's bay; default pending the user's
 * review). Without an ease a section's speed changes at once and the launch rises from below, as before.
 */
class ScrollEaseAndDropTest {
    private static final List<Section> EASED = List.of(
            new Section(10, 190),
            new Section(20, 170, false, Section.TIMED_HALT_RAMP_SECONDS, 1),
            new Section(30, 150, false, Section.TIMED_HALT_RAMP_SECONDS, 1));

    private static LevelScript level(List<Section> sections, double launch) {
        return LevelScript.builder(15, 3)
                .launchSeconds(launch)
                .sections(sections)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    @Test
    void onlyAMovingSectionEasesIn() {
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 0, false, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 0, true, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 150, false, 2, -1));
        assertEquals(0, new Section(10, 150).easeSeconds(), "no ease by default");
    }

    @Test
    void aSectionWithAnEaseGoesFromTheLastSpeedToItsOwnOverItsFirstEaseBySmoothstep() {
        int start = SimStep.ticks(10);
        int n = SimStep.ticks(1);
        assertEquals(190, Sortie.timedHaltSpeed(EASED, start - 1));
        double previous = 190;
        for (int k = 0; k < n; k++) {
            double s = (k + 1.0) / n;
            double expected = 190 + (170 - 190) * s * s * (3 - 2 * s);
            double speed = Sortie.timedHaltSpeed(EASED, start + k);
            assertEquals(expected, speed, 1e-9, "step " + k);
            assertTrue(speed <= previous);
            previous = speed;
        }
        assertEquals(170, Sortie.timedHaltSpeed(EASED, start + n));
        assertEquals(170, Sortie.timedHaltSpeed(EASED, start + n - 1), 1e-9, "at its speed by the ease's end");

        List<Section> instant = List.of(new Section(10, 190), new Section(20, 170));
        assertEquals(170, Sortie.timedHaltSpeed(instant, start), "without an ease at once");
    }

    @Test
    void theSortieScrollsAsTheEasesSayAndScrollAtAgrees() {
        LevelScript level = level(EASED, 0);
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        double before = 0;
        for (int i = 0; i < SimStep.ticks(25); i++) {
            sortie.step(Command.NONE);
            double step = sortie.groundScroll() - before;
            before = sortie.groundScroll();
            assertEquals(Sortie.timedHaltSpeed(EASED, i) * SimStep.SECONDS, step, 1e-9, "step " + i);
        }
        assertEquals(Sortie.scrollAt(level, 25), sortie.groundScroll(), 1e-6);
        // The eases cost a few px against instant changes: 20 × 0.5 × 1 s each.
        double instant = 190 * 10 + 170 * 10 + 150 * 5;
        assertEquals(instant + 20 * 0.5 + 20 * 0.5, Sortie.scrollAt(level, 25), 1.0);
    }

    @Test
    void aDropLaunchComesDownFromAboveTheTopEdgeToTheStartWithTheWingmanInHisSlot() {
        LevelScript level = level(List.of(new Section(30, 190)), 5).toBuilder()
                .launchDrop(true)
                .build();
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        assertTrue(
                sortie.ship().y() > PlayField.HEIGHT,
                "above the top edge at the start: " + sortie.ship().y());
        double y = sortie.ship().y();
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.UP.bit() | Command.LEFT.bit());
            assertTrue(sortie.ship().y() <= y, "coming down, the controls ignored");
            assertEquals(Ship.START_X, sortie.ship().x());
            y = sortie.ship().y();
            Wingman rook = sortie.wingman().orElseThrow();
            assertTrue(Math.abs(rook.y() - sortie.ship().y()) < 60, "Rook in his slot beside the ship");
        }
        sortie.step(Command.NONE);
        assertEquals(Ship.START_Y, sortie.ship().y(), 1e-6);

        Sortie rail = new Sortie(
                1, TestSpecs.LOADOUT, level(List.of(new Section(30, 190)), 5), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        assertTrue(rail.ship().y() < 0, "without the drop it rises from below");
    }
}
