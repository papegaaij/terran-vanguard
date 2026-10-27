package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Step RF2: {@link BossSpecBuilder} carries every component of a {@link BossSpec}, so the {@code withX} builders, {@link
 * BossSpec#of} and {@link BossSpec#builder} change only the components they name. The sample bosses set every
 * component between them.
 */
class BossSpecBuilderTest {
    private static List<BossSpec> samples() {
        return List.of(
                        TestSpecs.carrier(true, 2),
                        BreachArenaTest.colossus(true),
                        BreachHazardsTest.colossus(List.of(0), List.of(1), List.of(2)),
                        MawSpecs.maw(MawSpecs.Fit.ALL),
                        KrakenRootsTest.kraken(2),
                        PartHSpecs.spire(),
                        NavalConvoyTest.boss(),
                        RevenantWalkerSimTest.walker(true, 2),
                        RevenantWalkerSimTest.striking(true))
                .stream()
                .map(piece -> piece.boss().orElseThrow())
                .map(boss -> boss.barName().equals("TEST CARRIER") ? boss.withIncomingSeconds(3) : boss)
                .toList();
    }

    @Test
    void theBuilderHasAFieldForEveryComponentInItsOrder() {
        List<String> components = Arrays.stream(BossSpec.class.getRecordComponents())
                .map(component -> component.getGenericType() + " " + component.getName())
                .toList();
        List<String> fields = Arrays.stream(BossSpecBuilder.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> field.getGenericType() + " " + field.getName())
                .toList();
        assertEquals(components, fields);
    }

    @Test
    void aCopyOfEverySampleBuildsTheSameBoss() throws ReflectiveOperationException {
        for (BossSpec spec : samples()) {
            BossSpecBuilder copy = new BossSpecBuilder(spec);
            for (RecordComponent component : BossSpec.class.getRecordComponents()) {
                Field field = BossSpecBuilder.class.getDeclaredField(component.getName());
                field.setAccessible(true);
                assertEquals(
                        component.getAccessor().invoke(spec), field.get(copy), spec.barName() + " " + field.getName());
            }
            assertEquals(spec, copy.build(), spec.barName());
            assertEquals(spec, spec.withDebris(spec.debris()), spec.barName());
        }
    }

    @Test
    void theSamplesSetEveryComponentBetweenThem() throws ReflectiveOperationException {
        BossSpec base = BossSpec.of(0, 0, 0, 1, 0, 1, Layer.AIR, false, "base", 1, List.of(), List.of(), phases());
        TreeSet<String> unset = new TreeSet<>();
        for (RecordComponent component : BossSpec.class.getRecordComponents()) {
            Object empty = component.getAccessor().invoke(base);
            boolean set = false;
            for (BossSpec spec : samples()) {
                set |= !component.getAccessor().invoke(spec).equals(empty);
            }
            if (!set) {
                unset.add(component.getName());
            }
        }
        assertEquals(new TreeSet<String>(), unset);
    }

    @Test
    void ofLeavesEveryOtherComponentOut() throws ReflectiveOperationException {
        BossSpec spec = BossSpec.of(1, 2, 3, 4, 5, 6, Layer.AIR, true, "base", 7, List.of(), List.of(), phases());
        for (RecordComponent component : BossSpec.class.getRecordComponents()) {
            Object value = component.getAccessor().invoke(spec);
            if (component.getType() == Optional.class) {
                assertEquals(Optional.empty(), value, component.getName());
            }
        }
        assertEquals(false, spec.engagesOnArrival());
        assertEquals(List.of(), spec.armoured());
        assertEquals(List.of(), spec.poses());
        assertEquals(0.0, spec.deathSeconds());
        assertEquals(0.0, spec.incomingSeconds());
    }

    private static List<BossSpec.Phase> phases() {
        return TestSpecs.carrier(true, 2).boss().orElseThrow().phases().subList(0, 1);
    }
}
