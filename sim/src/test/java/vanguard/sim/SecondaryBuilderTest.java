package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.RecordComponent;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part C, step C1 (RF9b's pattern): {@link Secondary#builder()} and {@link Secondary#toBuilder()} carry every
 * component, so the factories and the {@code withX} copies change only what they name; the factories build what the
 * telescoping constructors built.
 */
class SecondaryBuilderTest {
    /** Objectives that set every component between them. */
    private static List<Secondary> samples() {
        return List.of(
                Secondary.NONE,
                new Secondary(0.8, 50),
                new Secondary(0, 50, List.of(), "skitter", List.of(), "", "", List.of(), -1, "bridge")
                        .withEscapesAllowed(2),
                Secondary.builder()
                        .credits(40)
                        .killAll(List.of("polyp-mortar"))
                        .label("NEST")
                        .build(),
                Secondary.builder()
                        .credits(80)
                        .partsOf("brood-carrier")
                        .parts(List.of(0, 1))
                        .beforePhase(1)
                        .build(),
                Secondary.builder().credits(25).groups(List.of("Dock One")).build(),
                new Secondary(0.65, 100).withBossesCounted(false),
                Secondary.afloat(100, "CONVOY"),
                Secondary.sondes(100, "SONDES"),
                Secondary.integrity(0.5, 100, "RELAY"),
                Secondary.links(100, "BARRIERS"),
                Secondary.untouched(100, "DOMES"),
                Secondary.standing(100, "DOMES"),
                Secondary.spared("freighter", 100, "HOLD FIRE"),
                Secondary.killRatioOf("harrow", 0.65, 100, "HARROWS"),
                Secondary.objectEscapes("cargo-container", 2, 100, "CONTAINERS"),
                Secondary.saved(5, 100, "SHUTTLES"));
    }

    @Test
    void toBuilderRoundTripsEveryComponent() throws ReflectiveOperationException {
        for (Secondary secondary : samples()) {
            assertEquals(secondary, secondary.toBuilder().build());
        }
        // Every component differs from its default in at least one sample.
        Secondary none = Secondary.builder().build();
        assertEquals(Secondary.NONE, none);
        for (RecordComponent component : Secondary.class.getRecordComponents()) {
            Object empty = component.getAccessor().invoke(none);
            boolean set = false;
            for (Secondary secondary : samples()) {
                set |= !component.getAccessor().invoke(secondary).equals(empty);
            }
            assertEquals(true, set, component.getName() + " is set by a sample");
        }
    }

    @Test
    void theFactoriesAndCopiesKeepWhatTheyDoNotName() {
        Secondary scoped = new Secondary(0, 50, List.of(), "skitter", List.of(), "", "", List.of(), -1, "bridge");
        Secondary allowed = scoped.withEscapesAllowed(2);
        assertEquals("bridge", allowed.tag());
        assertEquals(2, allowed.escapesAllowed());
        assertEquals(allowed, allowed.withBossesCounted(true));
        assertNotEquals(Secondary.untouched(100, "DOMES"), Secondary.standing(100, "DOMES"));
        Secondary objects = Secondary.objectEscapes("cargo-container", 2, 100, "CONTAINERS");
        assertEquals(true, objects.objects());
        assertEquals(true, objects.countsBosses());
        assertEquals(2, objects.escapesAllowed());
        assertEquals(5, Secondary.saved(5, 100, "SHUTTLES").saved());
    }
}
