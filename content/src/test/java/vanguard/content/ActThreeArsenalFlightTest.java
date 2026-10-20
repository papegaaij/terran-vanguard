package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSpec;
import vanguard.sim.ThrusterSpec;
import vanguard.sim.WeaponSpec;

/**
 * M7 part A (step A4): Act 3's arsenal flown from the data through {@link SimSpecs}: the Ion Beam burns
 * its first target at its damage per second, the Light Drone Bay's drone fires its bay's gun at the
 * convergence point, the Deflector Pod catches enemy bullets and deals no damage, the EMP Burst stuns a
 * machine twice as long as a Vrell unit, the Orbital Lance burns what is under it at its DPS, the
 * Shield Overcharge doubles the shield on its cooldown and the Evasive thrusters dash. Each flies on a
 * test range: one endless section on a slow scroll with dummies that neither move nor fire (but
 * where a test says so) at the ship's x.
 */
class ActThreeArsenalFlightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final int FIRE = Command.FIRE.bit();
    private static final int SPECIAL = Command.SPECIAL.bit();
    /** The range's scroll, px/s: the dummies come down slowly. */
    private static final double SCROLL = 30;

    private static final double SHIP_X = PlayField.WIDTH / 2.0;

    /** A plain loadout (the first engine, the second shield and plating) with {@code weapons} fitted. */
    static Loadout loadout(SimSpecs.FittedWeapon... weapons) {
        return SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getFirst().name(),
                List.of(weapons),
                CONTENT.shields().models().get(1).name(),
                CONTENT.armour().plating().get(1).name(),
                0,
                Difficulty.MEDIUM);
    }

    /** The test range with {@code units}, flown with Level 01's rules ({@code invulnerable}: nothing hits the ship). */
    static Sortie range(Loadout loadout, boolean invulnerable, GroundUnit... units) {
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(3600, SCROLL)))
                .groundUnits(List.of(units))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(1, 0)))
                .build();
        vanguard.sim.Rules rules = SimSpecs.rules(CONTENT, Level01Test.LEVEL, Difficulty.MEDIUM);
        return new Sortie(
                1,
                loadout,
                level,
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /** A dummy on the scrolling ground: {@code hp}, {@code size} square, firing {@code gun} if any. */
    static EnemySpec dummy(double hp, double size, Optional<EnemyGun> gun) {
        return new EnemySpec(
                "dummy",
                hp,
                new Hitbox(size, size),
                Layer.GROUND,
                0,
                false,
                0,
                0,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                gun,
                Optional.empty(),
                Optional.empty(),
                true);
    }

    static GroundUnit at(double x, EnemySpec spec) {
        return new GroundUnit(0, x, spec, -1);
    }

    /** The unit on the field whose spec is {@code spec}; empty when it is not (yet, or any more). */
    static Optional<Enemy> find(Sortie sortie, EnemySpec spec) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == spec) {
                return Optional.of(sortie.enemy(i));
            }
        }
        return Optional.empty();
    }

    /** Steps up or down so the ship's centre keeps {@code distance} below the target (on the field), or none. */
    private static int follow(Sortie sortie, Optional<Enemy> target, double distance) {
        if (target.isEmpty()) {
            return Command.NONE;
        }
        double want = target.get().renderY(1) - distance;
        double y = sortie.ship().y();
        return y < want - 2 ? Command.UP.bit() : y > want + 2 ? Command.DOWN.bit() : Command.NONE;
    }

    /**
     * The single-target DPS of {@code weapon} at {@code level} in {@code slot} as flown: fire held, the
     * ship's centre {@code distance} below a dummy {@code single_target.width} wide at its x; the HP the
     * dummy loses over 4 s once the ship has settled (3 s).
     */
    static double flownDps(Armament.Slot slot, String weapon, int level, double distance) {
        double width = CONTENT.weaponRules().singleTarget().width();
        EnemySpec target = dummy(1e6, width, Optional.empty());
        Sortie sortie = range(loadout(new SimSpecs.FittedWeapon(slot, weapon, level)), true, at(SHIP_X, target));
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(FIRE | follow(sortie, find(sortie, target), distance));
        }
        double before = find(sortie, target).orElseThrow().hp();
        double seconds = 4;
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(FIRE | follow(sortie, find(sortie, target), distance));
        }
        return (before - find(sortie, target).orElseThrow().hp()) / seconds;
    }

    @Test
    void theIonBeamBurnsItsFirstTargetAtItsDamagePerSecond() {
        WeaponData beam = CONTENT.weapon("ion-beam");
        for (int level = 1; level <= 5; level++) {
            assertEquals(
                    beam.levels().get(level - 1).damage(),
                    flownDps(Armament.Slot.FRONT, "ion-beam", level, 100),
                    0.01,
                    "L" + level);
        }
    }

    @Test
    void theIonBeamComesOnWithFireAndOffOnRelease() {
        Sortie sortie = range(loadout(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 3)), true);
        sortie.step(FIRE);
        assertEquals(1, sortie.events().count(SimEvents.Type.BEAM_ON));
        assertTrue(sortie.beam(0).on());
        assertEquals(10, sortie.beam(0).width(), 1e-9, "L3: 10 px");
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.BEAM_OFF));
        assertFalse(sortie.beam(0).on());
    }

    @Test
    void theLightDroneOrbitsAndFiresItsBaysGunAtTheConvergencePoint() {
        WeaponData bay = CONTENT.weapon("light-drone-bay");
        double converge = bay.drone().orElseThrow().converge();
        for (int level = 1; level <= 5; level++) {
            WeaponData.Level numbers = bay.levels().get(level - 1);
            double expected = numbers.rate() * numbers.damage();
            // One volley (a drone shot) either way: the window's edges.
            assertEquals(
                    expected,
                    flownDps(Armament.Slot.LEFT_WING, "light-drone-bay", level, converge),
                    numbers.damage() / 4 + 1e-9,
                    "L" + level);
        }
        Sortie sortie = range(loadout(new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "light-drone-bay", 1)), true);
        sortie.step(Command.NONE);
        assertTrue(sortie.drones().any());
        assertTrue(sortie.drones().whole(0));
        assertEquals(8, sortie.drones().hitsLeft(0), "L1: 8 hits");
    }

    @Test
    void theDeflectorPodCatchesEnemyBulletsOnItsSideAndDealsNoDamage() {
        EnemyGun gun = new EnemyGun(
                0.6,
                0.3,
                1,
                110,
                1,
                false,
                1,
                0,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Optional.empty(),
                Optional.empty(),
                EnemyGun.BURST_GAP_SECONDS);
        EnemySpec shooter = dummy(50, 20, Optional.of(gun));
        Sortie sortie = range(
                loadout(new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "deflector-pod", 1)),
                false,
                at(SHIP_X - 80, shooter));
        assertTrue(sortie.deflectors().any());
        assertEquals(3, sortie.deflectors().charges(0), "L1: 3 charges");
        int caught = 0;
        int charged = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(FIRE);
            caught += sortie.events().count(SimEvents.Type.DEFLECTOR_CAUGHT);
            charged += sortie.events().count(SimEvents.Type.DEFLECTOR_CHARGED);
        }
        assertTrue(caught >= 5, "caught " + caught);
        assertTrue(charged >= 1, "charged " + charged);
        assertEquals(50, find(sortie, shooter).orElseThrow().hp(), "a deflector deals no damage");
        assertEquals(0, flownDps(Armament.Slot.RIGHT_WING, "deflector-pod", 5, 100));
    }

    @Test
    void theEmpBurstStunsAMachineTwiceAsLongAsAVrellUnit() {
        SpecialSpec emp = SimSpecs.special(CONTENT, SimSpecs.EMP_BURST, 2);
        assertEquals(SpecialSpec.Kind.EMP, emp.kind());
        assertEquals(2, emp.charges());
        assertEquals(3, emp.maxCharges());
        assertEquals(new vanguard.sim.EmpSpec(0.4, 3, 1.5, 1.0), emp.emp());
        EnemySpec vrell = dummy(50, 20, Optional.empty());
        EnemySpec machine = dummy(50, 20, Optional.empty()).withMachine(true);
        Sortie sortie = range(loadout().withSpecial(emp), true, at(SHIP_X - 60, vrell), at(SHIP_X + 60, machine));
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        sortie.step(SPECIAL);
        int bursts = sortie.events().count(SimEvents.Type.EMP_BURST);
        int stunned = 0;
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.NONE);
            bursts += sortie.events().count(SimEvents.Type.EMP_BURST);
            stunned += sortie.events().count(SimEvents.Type.EMP_STUNNED);
        }
        assertEquals(1, bursts);
        assertEquals(2, stunned);
        assertEquals(1, sortie.special().charges());
        assertTrue(find(sortie, vrell).orElseThrow().empStunned());
        assertTrue(find(sortie, machine).orElseThrow().empStunned());
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(Command.NONE);
        }
        assertFalse(find(sortie, vrell).orElseThrow().empStunned(), "a Vrell unit: 1.5 s");
        assertTrue(find(sortie, machine).orElseThrow().empStunned(), "a machine: 3 s");
    }

    @Test
    void aStatBlocksMachineFlagReachesTheSimulation() {
        assertFalse(SimSpecs.enemy(CONTENT, "skitter", Difficulty.MEDIUM, Optional.empty())
                .machine());
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals("enemies/air/skitter/data.yaml")
                        ? new DataFile(f.path(), f.text().replace("\nbounty: 5", "\nbounty: 5\nmachine: true"))
                        : f)
                .toList();
        assertTrue(SimSpecs.enemy(ContentLoader.load(files), "skitter", Difficulty.MEDIUM, Optional.empty())
                .machine());
    }

    @Test
    void theOrbitalLanceLocksThenBurnsWhatIsUnderItAtItsDps() {
        SpecialSpec lance = SimSpecs.special(CONTENT, SimSpecs.ORBITAL_LANCE, 5);
        assertEquals(SpecialSpec.Kind.LANCE, lance.kind());
        assertEquals(2, lance.charges(), "at most 2 carried");
        assertEquals(40, lance.lance().width());
        assertEquals(400, lance.lance().bossPartCap());
        EnemySpec target = dummy(1e6, 20, Optional.empty());
        Sortie sortie = range(loadout().withSpecial(lance), true, at(SHIP_X, target));
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.LANCE_LOCK));
        int lockTicks = 0;
        while (sortie.events().count(SimEvents.Type.LANCE_FIRED) == 0) {
            assertTrue(lockTicks++ < SimStep.ticks(1), "it fires after its lock");
            sortie.step(Command.NONE);
        }
        assertEquals(SimStep.ticks(0.6), lockTicks, 1, "a 0.6 s lock");
        int burnTicks = 0;
        while (sortie.events().count(SimEvents.Type.LANCE_ENDED) == 0) {
            assertTrue(burnTicks++ < SimStep.ticks(4), "it ends after its time");
            sortie.step(Command.NONE);
        }
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        double dealt = 1e6 - find(sortie, target).orElseThrow().hp();
        assertEquals(400 * 3, dealt, 400.0 / SimStep.PER_SECOND * 2, "400 per second for 3 s");
    }

    @Test
    void theShieldOverchargeDoublesTheShieldOnItsCooldown() {
        SpecialSpec overcharge = SimSpecs.special(CONTENT, SimSpecs.SHIELD_OVERCHARGE, 1);
        assertTrue(overcharge.cooldown());
        assertEquals(0, overcharge.charges());
        assertEquals(45, overcharge.cooldownSeconds());
        assertEquals(10, overcharge.cooldownPickupSeconds());
        assertEquals(new vanguard.sim.OverchargeSpec(2, 8, 0.5), overcharge.overcharge());
        Loadout loadout = loadout().withSpecial(overcharge);
        double capacity = loadout.shield().capacity();
        Sortie sortie = range(loadout, true);
        sortie.step(SPECIAL);
        int overcharges = sortie.events().count(SimEvents.Type.OVERCHARGE);
        sortie.step(Command.NONE);
        overcharges += sortie.events().count(SimEvents.Type.OVERCHARGE);
        assertEquals(1, overcharges);
        assertEquals(2 * capacity, sortie.ship().defences().shieldCapacity(), 1e-9);
        assertEquals(2 * capacity, sortie.ship().defences().shield(), 1e-9);
        int ended = 0;
        int ready = 0;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(i == SimStep.ticks(9) ? SPECIAL : Command.NONE);
            overcharges += sortie.events().count(SimEvents.Type.OVERCHARGE);
            ended += sortie.events().count(SimEvents.Type.OVERCHARGE_ENDED);
        }
        assertEquals(1, ended);
        assertEquals(1, overcharges, "denied while it cools");
        assertEquals(capacity, sortie.ship().defences().shieldCapacity(), 1e-9);
        for (int i = 0; i < SimStep.ticks(36); i++) {
            sortie.step(Command.NONE);
            ready += sortie.events().count(SimEvents.Type.SPECIAL_READY);
        }
        assertEquals(1, ready, "ready again 45 s after the call");
    }

    @Test
    void theEvasiveThrustersDashOnTheButtonAndOnADoubleTap() {
        ThrusterSpec dash = SimSpecs.thrusters(CONTENT);
        assertEquals(new ThrusterSpec(72, 0.12, 0.25, 3, 0.25), dash);
        Sortie sortie = range(loadout().withThrusters(dash), true);
        sortie.step(Command.NONE);
        assertTrue(sortie.ship().thrustersFitted());
        double x = sortie.ship().x();
        sortie.step(Command.DASH.bit() | Command.LEFT.bit());
        assertEquals(1, sortie.events().count(SimEvents.Type.DASH));
        assertEquals(6, sortie.events().value(firstDash(sortie.events())), "to the left");
        for (int i = 0; i < SimStep.ticks(0.2); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(72, x - sortie.ship().x(), 3, "72 px");
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(sortie.ship().dashReady());
        // A double-tap to the right: press, release, press within 0.25 s.
        sortie.step(Command.RIGHT.bit());
        sortie.step(Command.NONE);
        sortie.step(Command.NONE);
        sortie.step(Command.RIGHT.bit());
        assertEquals(1, sortie.events().count(SimEvents.Type.DASH));
        assertEquals(2, sortie.events().value(firstDash(sortie.events())), "to the right");
    }

    private static int firstDash(SimEvents events) {
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.DASH) {
                return i;
            }
        }
        throw new AssertionError("no dash");
    }

    /** The beam is a beam spec of its level's width and damage per step, no shots. */
    @Test
    void theIonBeamMountIsABeamOfItsLevel() {
        WeaponSpec beam =
                SimSpecs.mount(CONTENT, Armament.Slot.FRONT, "ion-beam", 1, 0).weapon();
        assertEquals(WeaponSpec.Delivery.BEAM, beam.delivery());
        assertEquals(45.0 / SimStep.PER_SECOND, beam.damage(), 1e-12);
        assertEquals(6, beam.size().width());
    }
}
