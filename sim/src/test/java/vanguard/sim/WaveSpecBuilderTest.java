package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/**
 * Step RF9b: {@link WaveSpecBuilder} carries every component of a {@link WaveSpec}, so {@link WaveSpec#toBuilder}
 * copies a wave whole, the {@code withX} builders change only the component they name (and keep the later ones, which
 * the older copies left out) and {@link WaveSpec#of} and {@link WaveSpec#builder} leave the rest at their defaults. The
 * sample waves set every component between them.
 */
class WaveSpecBuilderTest {
    private static final List<WaveSpec.At> LANE = List.of(new WaveSpec.At(100, -40), new WaveSpec.At(100, 900));

    /** A circle with every plain component set, a fork's branch's. */
    private static final WaveSpec CIRCLE = WaveSpec.builder(
                    12, WaveSpec.Formation.CIRCLE, TestSpecs.SKITTER, 6, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
            .holdSeconds(Optional.of(3.0))
            .warningSeconds(Optional.of(2.5))
            .breakGroup(2)
            .speed(Optional.of(150.0))
            .intervalSeconds(Optional.of(0.4))
            .carried(List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, WaveSpec.Carried.LAST)))
            .at(Optional.of(new WaveSpec.At(240, 160)))
            .paths(List.of(LANE))
            .loopBack(Optional.of(new WaveSpec.LoopBack(1.5, LANE, 2)))
            .tag("bridge")
            .clockwise(true)
            .lanes(List.of(LANE))
            .pairSeconds(Optional.of(0.4))
            .branch(Optional.of(Act5ESpecs.LEFT))
            .build();

    /** A field of jellies. */
    private static final WaveSpec FIELD = WaveSpec.builder(
                    20, WaveSpec.Formation.FIELD, TestSpecs.SKITTER, 8, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
            .field(Optional.of(new WaveSpec.Field(240, 300, 200, 40, 0.2)))
            .build();

    private static final WaveSpec PURSUIT = PursuitTest.pursuit(10, 12, PursuitTest.block(6, 3, 10, 60));

    private static final WaveSpec CAVE = TestSpecs.wave(
                    5, WaveSpec.Formation.SINGLE, TestSpecs.SKITTER, 2, WaveSpec.Entry.SIDES, WaveSpec.Edge.LEFT)
            .withCave(0);

    private static final WaveSpec LED = Act5BSpecs.vWing(7, Act5BSpecs.WING_TALON);

    private static final WaveSpec ON_ROCKS = Act5BSpecs.onRocks(9, 2, new WaveSpec.Rock(120, 900, 10, -60));

    private static final WaveSpec LAYERS = Act5CSpecs.layers(
            30,
            Act5CSpecs.MINELAYER,
            2,
            WaveSpec.Edge.ALTERNATING,
            new WaveSpec.MineLines(List.of(100.0), List.of(Act5CSpecs.GAP_LEFT)));

    /** M10 part A (The three-way war): a hunting wave. */
    private static final WaveSpec HUNTERS = WaveSpec.builder(
                    1, WaveSpec.Formation.SINGLE, PreyWarTest.SHOOTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
            .hunts("prey")
            .build();

    private static List<WaveSpec> samples() {
        return List.of(CIRCLE, FIELD, PURSUIT, CAVE, LED, ON_ROCKS, LAYERS, HUNTERS);
    }

    @Test
    void theBuilderHasAFieldForEveryComponentInItsOrder() {
        List<String> components = Arrays.stream(WaveSpec.class.getRecordComponents())
                .map(component -> component.getGenericType() + " " + component.getName())
                .toList();
        List<String> fields = Arrays.stream(WaveSpecBuilder.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> field.getGenericType() + " " + field.getName())
                .toList();
        assertEquals(components, fields);
    }

    @Test
    void aCopyOfEverySampleBuildsTheSameWave() throws ReflectiveOperationException {
        for (WaveSpec spec : samples()) {
            WaveSpecBuilder copy = spec.toBuilder();
            for (RecordComponent component : WaveSpec.class.getRecordComponents()) {
                Field field = WaveSpecBuilder.class.getDeclaredField(component.getName());
                field.setAccessible(true);
                assertEquals(
                        component.getAccessor().invoke(spec),
                        field.get(copy),
                        spec.enemy().slug() + " " + field.getName());
            }
            assertEquals(spec, copy.build(), spec.enemy().slug());
        }
    }

    @Test
    void theSamplesSetEveryComponentBetweenThem() throws ReflectiveOperationException {
        WaveSpec base = WaveSpec.builder(
                        0, WaveSpec.Formation.SINGLE, TestSpecs.SKITTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .build();
        TreeSet<String> unset = new TreeSet<>();
        for (RecordComponent component : WaveSpec.class.getRecordComponents()) {
            Object empty = component.getAccessor().invoke(base);
            boolean set = false;
            for (WaveSpec spec : samples()) {
                set |= !component.getAccessor().invoke(spec).equals(empty);
            }
            if (!set) {
                unset.add(component.getName());
            }
        }
        assertEquals(new TreeSet<String>(), unset);
    }

    @Test
    void ofAndTheBuilderLeaveEveryOtherComponentOut() throws ReflectiveOperationException {
        List<WaveSpec.Carried> carried = List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, 0));
        WaveSpec spec = WaveSpec.of(
                4,
                WaveSpec.Formation.COLUMN,
                TestSpecs.SKITTER,
                3,
                WaveSpec.Entry.SIDES,
                WaveSpec.Edge.RIGHT,
                Optional.of(1.0),
                Optional.of(2.0),
                3,
                Optional.of(100.0),
                Optional.of(0.5),
                carried);
        Set<String> base = Set.of(
                "t",
                "formation",
                "enemy",
                "count",
                "entry",
                "edge",
                "holdSeconds",
                "warningSeconds",
                "breakGroup",
                "speed",
                "intervalSeconds",
                "carried");
        for (RecordComponent component : WaveSpec.class.getRecordComponents()) {
            if (base.contains(component.getName())) {
                continue;
            }
            Object value = component.getAccessor().invoke(spec);
            Object expected = component.getType() == Optional.class
                    ? Optional.empty()
                    : component.getType() == List.class ? List.of() : null;
            if (expected != null) {
                assertEquals(expected, value, component.getName());
            }
        }
        assertEquals("", spec.tag());
        assertEquals(false, spec.clockwise());
        assertEquals(
                spec,
                WaveSpec.builder(
                                4,
                                WaveSpec.Formation.COLUMN,
                                TestSpecs.SKITTER,
                                3,
                                WaveSpec.Entry.SIDES,
                                WaveSpec.Edge.RIGHT)
                        .holdSeconds(Optional.of(1.0))
                        .warningSeconds(Optional.of(2.0))
                        .breakGroup(3)
                        .speed(Optional.of(100.0))
                        .intervalSeconds(Optional.of(0.5))
                        .carried(carried)
                        .build());
        WaveSpec plain = TestSpecs.wave(
                4, WaveSpec.Formation.COLUMN, TestSpecs.SKITTER, 3, WaveSpec.Entry.SIDES, WaveSpec.Edge.RIGHT);
        assertEquals(Optional.empty(), plain.holdSeconds());
        assertEquals(Optional.empty(), plain.warningSeconds());
        assertEquals(1, plain.breakGroup());
        assertEquals(Optional.empty(), plain.speed());
        assertEquals(Optional.empty(), plain.intervalSeconds());
        assertEquals(List.of(), plain.carried());
    }

    @Test
    void eachWithXChangesOnlyItsComponent() throws ReflectiveOperationException {
        WaveSpec.MineLines lines = new WaveSpec.MineLines(List.of(120.0, 200.0), List.of(Act5CSpecs.GAP_RIGHT));
        WaveSpec.OnRocks rocks = new WaveSpec.OnRocks(
                List.of(new WaveSpec.Rock(300, 900, -10, -60)),
                2,
                ON_ROCKS.onRocks().orElseThrow().chunk());
        EnemySpecCopy copy = new EnemySpecCopy(LED.leader().orElseThrow());
        copy.hp = 60;
        EnemySpec leader = copy.build();
        WaveSpec.Pursuit block = PursuitTest.block(4.5, 4, 10, 60);
        List<WaveSpec.At> lane = List.of(new WaveSpec.At(300, -40), new WaveSpec.At(200, 900));

        changesOnly(CIRCLE, "branch", spec -> spec.withBranch(Act5ESpecs.RIGHT));
        changesOnly(LAYERS, "mineLines", spec -> spec.withMineLines(lines));
        changesOnly(LED, "leader", spec -> spec.withLeader(leader));
        changesOnly(ON_ROCKS, "onRocks", spec -> spec.withRocks(rocks));
        changesOnly(CAVE, "mount", spec -> spec.withCave(1));
        changesOnly(CIRCLE, "mount", spec -> spec.withWall(true));
        changesOnly(CIRCLE, "mount", spec -> spec.withGroundNests(List.of(100.0, 380.0)));
        changesOnly(PURSUIT, "pursuit", spec -> spec.withPursuit(block));
        changesOnly(CIRCLE, "lanes", spec -> spec.withLanes(List.of(lane)));
        changesOnly(CIRCLE, "pairSeconds", spec -> spec.withPairs(0.8));
        changesOnly(CIRCLE, "at", spec -> spec.withAt(new WaveSpec.At(100, 100)));
        changesOnly(CIRCLE, "clockwise", spec -> spec.clockwise(false));

        WaveSpec swarm = PursuitTest.pursuit(10, 12, block).toBuilder()
                .formation(WaveSpec.Formation.SWARM)
                .pursuit(Optional.empty())
                .build();
        WaveSpec pursued = swarm.withPursuit(block);
        assertEquals(WaveSpec.Formation.PURSUIT, pursued.formation());
        assertEquals(Optional.of(block), pursued.pursuit());
    }

    /** {@code change} applied to {@code spec} changes {@code name} and keeps every other component. */
    private static void changesOnly(WaveSpec spec, String name, UnaryOperator<WaveSpec> change)
            throws ReflectiveOperationException {
        WaveSpec changed = change.apply(spec);
        for (RecordComponent component : WaveSpec.class.getRecordComponents()) {
            Object before = component.getAccessor().invoke(spec);
            Object after = component.getAccessor().invoke(changed);
            if (component.getName().equals(name)) {
                assertNotEquals(before, after, name);
            } else {
                assertEquals(before, after, name + " kept " + component.getName());
            }
        }
    }
}
