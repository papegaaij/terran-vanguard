package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.DeflectorSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.SimStep;
import vanguard.sim.WeaponSpec;

/**
 * M7 part A (step A2): Act 3's arsenal in the data: the Ion Beam ({@code hits: beam}), the Rear Lance,
 * the Deflector Pod ({@code hits: deflector}), the Light Drone Bay ({@code drone}), the EMP Burst, the
 * Orbital Lance and the Shield Overcharge (a cooldown special), the Evasive thrusters' dash, the
 * stat blocks' {@code machine} flag and the reference DPS to Level 21; their schema rules reject
 * what does not fit.
 */
class ActThreeArsenalDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String BEAM = "player/weapons/ion-beam/data.yaml";
    private static final String DEFLECTOR = "player/weapons/deflector-pod/data.yaml";
    private static final String DRONE = "player/weapons/light-drone-bay/data.yaml";
    private static final String SPECIALS = "player/specials/data.yaml";
    private static final String SKITTER = "enemies/air/skitter/data.yaml";

    @Test
    void theIonBeamBurnsItsWidthAtItsDamagePerSecond() {
        WeaponData beam = CONTENT.weapon("ion-beam");

        assertTrue(beam.beam());
        assertEquals(WeaponData.Slot.FRONT, beam.slot());
        assertEquals(List.of("beam"), beam.traits());
        assertEquals(5000, beam.price());
        assertEquals(Optional.of(2500), beam.upgradeBase());
        assertEquals(15, beam.unlock());
        assertEquals(new Span(4, 8), beam.draw());
        assertEquals(Optional.empty(), beam.size());
        assertEquals(Optional.empty(), beam.speed());
        assertEquals(
                List.of(6.0, 8.0, 10.0, 12.0, 14.0),
                beam.levels().stream().map(level -> level.width().orElseThrow()).toList());
        assertEquals(
                List.of(45.0, 60.0, 80.0, 105.0, 130.0),
                beam.levels().stream().map(beam::volleyDps).toList());
        assertEquals(18, beam.overdrive().width().orElseThrow());
        assertEquals(160, beam.volleyDps(beam.overdrive()));
        assertEquals(WeaponData.Level.Kind.BEAM, beam.overdrive().kind());
    }

    @Test
    void theRearLanceIsALanceLaserFiringBackwards() {
        WeaponData lance = CONTENT.weapon("rear-lance");

        assertEquals(WeaponData.Slot.REAR, lance.slot());
        assertEquals(List.of("rear", "piercing"), lance.traits());
        assertEquals(2800, lance.price());
        assertEquals(17, lance.unlock());
        assertEquals(
                List.of(18.0, 24.0, 36.0, 48.0, 60.0),
                lance.levels().stream().map(lance::volleyDps).toList());
        assertEquals(
                List.of(2, 3, 3, 4, 5),
                lance.levels().stream()
                        .map(level -> level.pierce().orElseThrow())
                        .toList());
        assertTrue(lance.levels().stream()
                .flatMap(level -> level.pattern().stream())
                .allMatch(shot -> shot.angle() == 180));
        assertTrue(SimSpecs.flies(CONTENT, "rear-lance"), "a plain bolt weapon: it flies already");
    }

    @Test
    void theDeflectorPodHoldsAFieldPerLevelAndDealsNoDamage() {
        WeaponData pod = CONTENT.weapon("deflector-pod");

        assertTrue(pod.deflector());
        assertEquals(Optional.of(true), pod.pod());
        assertEquals(1600, pod.price());
        assertEquals(18, pod.unlock());
        assertEquals(
                List.of(22.0, 24.0, 26.0, 28.0, 30.0),
                pod.levels().stream().map(level -> level.radius().orElseThrow()).toList());
        assertEquals(
                List.of(3, 3, 4, 4, 5),
                pod.levels().stream()
                        .map(level -> level.charges().orElseThrow())
                        .toList());
        assertEquals(
                List.of(0.67, 0.55, 0.5, 0.45, 0.4),
                pod.levels().stream()
                        .map(level -> level.recharge().orElseThrow())
                        .toList());
        assertEquals(
                new WeaponData.Level(
                        List.of(),
                        0,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(32.0),
                        Optional.of(6),
                        Optional.of(0.35),
                        Optional.empty(),
                        Optional.empty()),
                pod.overdrive());
        assertTrue(pod.levels().stream().allMatch(level -> pod.volleyDps(level) == 0));
    }

    @Test
    void theLightDroneBaysDroneOrbitsAndBreaksAfterItsLevelsHits() {
        WeaponData bay = CONTENT.weapon("light-drone-bay");
        WeaponData.Drone drone = bay.drone().orElseThrow();

        assertEquals(new Size(14, 14), drone.size());
        assertEquals(new Size(8, 8), drone.hitbox());
        assertEquals(java.util.Optional.of(36.0), drone.orbit());
        assertEquals(java.util.Optional.of(2.4), drone.period());
        assertEquals(java.util.Optional.of(140.0), drone.converge());
        assertEquals(0.3, drone.fadeIn());
        assertEquals(new WeaponData.Contact(4, 2, 2, 10), drone.contact());
        assertEquals(
                List.of(8, 9, 10, 11, 12),
                bay.levels().stream()
                        .map(level -> level.droneHits().orElseThrow())
                        .toList());
        assertEquals(
                List.of(6.0, 5.5, 5.0, 4.5, 4.0),
                bay.levels().stream()
                        .map(level -> level.rebuild().orElseThrow())
                        .toList());
        assertEquals(10.2, bay.volleyDps(bay.levels().getFirst()), 1e-9);
        assertEquals(35, bay.volleyDps(bay.overdrive()), 1e-9);
    }

    /** Step A4: the three fly as their own kinds of mount, from the data. */
    @Test
    void theBeamTheDeflectorAndTheDroneFlyAsTheirMounts() {
        assertTrue(SimSpecs.flies(CONTENT, "ion-beam"));
        assertTrue(SimSpecs.flies(CONTENT, "deflector-pod"));
        assertTrue(SimSpecs.flies(CONTENT, "light-drone-bay"));
        Armament.Mount beam = SimSpecs.mount(CONTENT, Armament.Slot.FRONT, "ion-beam", 2, 0);
        assertEquals(WeaponSpec.Delivery.BEAM, beam.weapon().delivery());
        assertEquals(8, beam.weapon().size().width());
        assertEquals(60, beam.weapon().damage() * SimStep.PER_SECOND, 1e-9);
        assertEquals(10, beam.overdrive().size().width());
        assertEquals(
                18,
                SimSpecs.mount(CONTENT, Armament.Slot.FRONT, "ion-beam", 5, 0)
                        .overdrive()
                        .size()
                        .width());
        Armament.Mount pod = SimSpecs.mount(CONTENT, Armament.Slot.LEFT_WING, "deflector-pod", 5, 0);
        assertTrue(pod.deflects());
        assertFalse(pod.fires());
        assertEquals(new DeflectorSpec(30, 5, 0.4), pod.deflector());
        assertEquals(new DeflectorSpec(32, 6, 0.35), pod.deflectorOverdrive());
        Armament.Mount bay = SimSpecs.mount(CONTENT, Armament.Slot.RIGHT_WING, "light-drone-bay", 1, 0);
        assertTrue(bay.bay());
        assertEquals(WeaponSpec.Delivery.BOLT, bay.weapon().delivery());
        assertEquals(1.7, bay.weapon().damage());
        assertEquals(36, bay.drone().orbit());
        assertEquals(8, bay.drone().hits());
        assertEquals(6, bay.drone().rebuildSeconds());
        assertEquals(9, bay.droneOverdrive().hits());
        assertEquals(new Hitbox(8, 8), bay.drone().box());
    }

    @Test
    void aBeamLevelWithAPatternIsRejected() {
        assertProblem(
                BEAM,
                text -> text.replace("{width: 6, damage: 45}", "{width: 6, damage: 45, pattern: [[0, 0]], rate: 5}"),
                "design/" + BEAM,
                "a beam level has a width");
    }

    @Test
    void aBeamWithAProjectileIsRejected() {
        assertProblem(
                BEAM,
                text -> text.replace("hits: beam\n", "hits: beam\nspeed: 900\n"),
                "design/" + BEAM,
                "has no speed, size, range, lifetime or drone");
    }

    @Test
    void aDeflectorThatIsNoPodOrMissesItsChargesIsRejected() {
        assertProblem(DEFLECTOR, text -> text.replace("pod: true\n", ""), "design/" + DEFLECTOR, "is a wing pod");
        assertProblem(
                DEFLECTOR,
                text -> text.replace("{radius: 22, charges: 3, recharge: 0.67}", "{radius: 22, recharge: 0.67}"),
                "design/" + DEFLECTOR,
                "needs a radius, charges and a recharge");
    }

    @Test
    void aDroneLevelWithoutItsHitsIsRejected() {
        assertProblem(
                DRONE,
                text -> text.replace("drone_hits: 8, rebuild: 6}", "rebuild: 6}"),
                "design/" + DRONE,
                "need drone_hits and rebuild");
    }

    @Test
    void theActThreeSpecialsAreInTheData() {
        SpecialsData specials = CONTENT.specials();
        SpecialsData.Special emp = special("EMP Burst");
        SpecialsData.Special lance = special("Orbital Lance");
        SpecialsData.Special overcharge = special("Shield Overcharge");

        assertEquals(Optional.of(350), emp.chargePrice());
        assertEquals(Optional.of(3), emp.maxCharges());
        assertEquals(15, emp.unlock());
        assertEquals(1, emp.free());
        assertEquals(new SpecialsData.Emp(0.4, new SpecialsData.Stun(3, 1.5), 1.0), specials.emp());
        assertEquals(Optional.of(600), lance.chargePrice());
        assertEquals(Optional.of(2), lance.maxCharges());
        assertEquals(17, lance.unlock());
        assertEquals(0, lance.free());
        SpecialsData.OrbitalLance column = specials.orbitalLance();
        assertEquals(0.6, column.lock());
        assertEquals(40, column.width());
        assertEquals(3, column.seconds());
        assertEquals(0.3, column.fade());
        assertEquals(240, column.follow());
        assertEquals(400, column.dps());
        assertEquals(new SpecialsData.BossPart(200, 400), column.bossPart());
        assertEquals(1.0, column.repeat());
        assertEquals(
                new SpecialsData.Radio("Lance Control", "generic-cdf", "Lance uplink locked. Firing!"), column.radio());
        assertTrue(overcharge.cooldownSpecial());
        assertEquals(Optional.of(5000), overcharge.price());
        assertEquals(Optional.of(45.0), overcharge.cooldown());
        assertEquals(Optional.empty(), overcharge.maxCharges());
        assertEquals(19, overcharge.unlock());
        assertEquals(new SpecialsData.Overcharge(2, 8, 0.5), specials.overcharge());
        assertEquals(10, specials.cooldownPickup());
        assertFalse(emp.cooldownSpecial());
    }

    @Test
    void aSpecialWithChargesAndACooldownIsRejected() {
        assertProblem(
                SPECIALS,
                text -> text.replace(
                        "{name: Shield Overcharge, price: 5000, cooldown: 45, unlock: 19}",
                        "{name: Shield Overcharge, price: 5000, cooldown: 45, max_charges: 2, unlock: 19}"),
                "design/" + SPECIALS,
                "Shield Overcharge: a special has charge_price and max_charges");
    }

    @Test
    void theEvasiveThrustersDash() {
        SystemsData.Utility thrusters = CONTENT.systems().utility().stream()
                .filter(module -> module.name().equals("Evasive thrusters"))
                .findFirst()
                .orElseThrow();

        assertEquals("approved", thrusters.design());
        assertEquals(
                new SystemsData.Dash(72, 0.12, 0.25, 3, 0.25), thrusters.dash().orElseThrow());
        assertEquals(2, thrusters.draw());
        assertEquals(List.of(4000), thrusters.prices());
    }

    @Test
    void noUnitOfActsOneAndTwoIsAMachineButAStatBlockCanSayOne() {
        // M7 part E: the Ghost Drone (Level 18) is the first machine; no unit of Acts 1 and 2 is one.
        assertTrue(CONTENT.enemies().values().stream()
                .filter(enemy -> enemy.firstLevel() <= 14)
                .noneMatch(EnemyData::isMachine));
        assertTrue(CONTENT.enemy("ghost-drone").isMachine());

        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(SKITTER)
                        ? new DataFile(SKITTER, f.text().replace("\nbounty: 5", "\nbounty: 5\nmachine: true"))
                        : f)
                .toList();
        assertTrue(ContentLoader.load(files).enemy("skitter").isMachine());
    }

    @Test
    void theReferenceDpsRunsToLevel21() {
        var reference = CONTENT.enemyBasis().referenceDps();

        assertEquals(
                List.of(76.0, 80.0, 84.0, 88.0, 92.0, 96.0, 100.0),
                java.util.stream.IntStream.rangeClosed(15, 21)
                        .mapToObj(level -> reference.get(level).doubleValue())
                        .toList());
        // The act HP factor beyond Level 14: the Mantis (L06) at Level 16, ×80/52.
        assertEquals(80.0 / 52, SimSpecs.actHpFactor(CONTENT, "mantis", 3, 16), 1e-9);
    }

    private static SpecialsData.Special special(String name) {
        return CONTENT.specials().specials().stream()
                .filter(special -> special.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    /** Loads the design tree with one file edited; exactly one problem is reported, containing every part. */
    private static void assertProblem(String path, UnaryOperator<String> edit, String... parts) {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(path, edit.apply(f.text())) : f)
                .toList();
        String edited = files.stream()
                .filter(f -> f.path().equals(path))
                .findFirst()
                .orElseThrow()
                .text();
        DataFile original = DesignTree.dataFiles().stream()
                .filter(f -> f.path().equals(path))
                .findFirst()
                .orElseThrow();
        assertFalse(edited.equals(original.text()), "the edit applies to " + path);

        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));

        assertEquals(1, e.problems().size(), e.getMessage());
        for (String part : parts) {
            assertTrue(
                    e.problems().getFirst().contains(part),
                    "'" + part + "' in " + e.problems().getFirst());
        }
    }
}
