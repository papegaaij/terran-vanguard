package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Step RF2: {@link EnemySpecCopy} carries every component of an {@link EnemySpec}, so the {@code withX} builders and
 * {@link EnemySpec#of} change only the component they name. The sample specs set every component between them.
 */
class EnemySpecCopyTest {
    private static List<EnemySpec> samples() {
        return List.of(
                TestSpecs.SKITTER,
                TestSpecs.NEEDLER,
                WardenTankTest.TANK,
                LampreyTest.LAMPREY,
                DepthHunterTest.HUNTER,
                DriftjellyTest.jelly(),
                BurrowTest.burrower(),
                VortexTest.devil(),
                ReefFryTest.FRY,
                ReefFryTest.REEF,
                LinkTest.ANCHOR,
                ReefSpitterTest.spitter(5),
                FarsideTest.COILWYRM,
                FarsideTest.MANTIS,
                GlowAnglerTest.ANGLER,
                CraterNestTest.MORTAR,
                AbyssRayTest.RAY,
                AbyssRayTest.BARBFIN,
                GroundAndDiveTest.STINGER,
                GroundAndDiveTest.TURRET,
                PartDSpecs.wraith(),
                PartDSpecs.MOTE,
                SiegeSpecs.BOMBER,
                SiegeSpecs.MORTAR_WALKER,
                SiegeSpecs.HERALD,
                SiegeSpecs.SHELLBACK,
                SpiralNautilusTest.NAUTILUS,
                PartCSpecs.hiveNode(3, 1),
                PartCSpecs.ravager(0.3, 3.0),
                PartCSpecs.hardenedTarget(10),
                PartCSpecs.plainTarget(10, true),
                KiteSwoopTest.kite(KiteSwoopTest.SWOOP),
                GhostDroneTest.drone(),
                SirenTest.SIREN,
                DrillRigTest.RIG,
                ThreadcrawlerTest.crawler(4, ThreadcrawlerTest.PORES, new double[] {0, 5_000}, new double[] {100, 100}),
                EnemySpec.of("whirl-seed", 1, new Hitbox(14, 14), Layer.AIR, 4, true, 3, 150)
                        .withSpiral(Optional.of(new EnemySpec.Spiral(3, 0.5, 40, 60, 0)))
                        .withDeathBurst(Optional.of(new EnemySpec.DeathBurst(6, 120, 4)))
                        .withBrood(Optional.of(
                                new EnemySpec.Brood(TestSpecs.SKITTER, 6, 8, 3, Math.toRadians(120), 160, 8))));
    }

    @Test
    void theCopyHasAFieldForEveryComponentInItsOrder() {
        List<String> components = Arrays.stream(EnemySpec.class.getRecordComponents())
                .map(component -> component.getGenericType() + " " + component.getName())
                .toList();
        List<String> fields = Arrays.stream(EnemySpecCopy.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> field.getGenericType() + " " + field.getName())
                .toList();
        assertEquals(components, fields);
    }

    @Test
    void aCopyOfEverySampleBuildsTheSameSpecField() throws ReflectiveOperationException {
        for (EnemySpec spec : samples()) {
            EnemySpecCopy copy = new EnemySpecCopy(spec);
            for (RecordComponent component : EnemySpec.class.getRecordComponents()) {
                Field field = EnemySpecCopy.class.getDeclaredField(component.getName());
                assertEquals(
                        component.getAccessor().invoke(spec), field.get(copy), spec.slug() + " " + field.getName());
            }
            assertEquals(spec, copy.build(), spec.slug());
            assertEquals(spec, spec.withGun(spec.gun()), spec.slug());
        }
    }

    @Test
    void theSamplesSetEveryComponentBetweenThem() throws ReflectiveOperationException {
        EnemySpec base = EnemySpec.of("base", 1, new Hitbox(1, 1), Layer.AIR, 0, false, 0, 0);
        TreeSet<String> unset = new TreeSet<>();
        List<String> names = new ArrayList<>();
        for (RecordComponent component : EnemySpec.class.getRecordComponents()) {
            names.add(component.getName());
            Object empty = component.getAccessor().invoke(base);
            boolean set = false;
            for (EnemySpec spec : samples()) {
                set |= !component.getAccessor().invoke(spec).equals(empty);
            }
            if (!set) {
                unset.add(component.getName());
            }
        }
        assertEquals(new TreeSet<String>(), unset);
        assertTrue(names.size() > 50);
    }

    @Test
    void ofLeavesEveryOtherComponentOut() throws ReflectiveOperationException {
        EnemySpec spec = EnemySpec.of("base", 3, new Hitbox(4, 5), Layer.GROUND, 6, true, 7, 8);
        for (RecordComponent component : EnemySpec.class.getRecordComponents()) {
            Object value = component.getAccessor().invoke(spec);
            if (component.getType() == Optional.class) {
                assertEquals(Optional.empty(), value, component.getName());
            } else if (component.getType() == List.class) {
                assertEquals(List.of(), value, component.getName());
            } else if (component.getType() == boolean.class) {
                assertEquals(component.getName().equals("destroyedByRamming"), value, component.getName());
            }
        }
        assertEquals(0.0, spec.drift());
    }
}
