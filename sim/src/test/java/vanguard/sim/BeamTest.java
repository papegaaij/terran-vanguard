package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartASpecs.beamMount;
import static vanguard.sim.PartASpecs.dummy;
import static vanguard.sim.PartASpecs.sortie;
import static vanguard.sim.PartASpecs.unit;
import static vanguard.sim.PartASpecs.units;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The {@code beam} delivery (design/player/weapons/ion-beam, Beam rules; M7 part A): one column from
 * the nose straight up, its damage per second dealt as one sixtieth every step to the first target it
 * touches, a tie to the higher layer, hardened ground passed over with a glance, {@code high-air}
 * reached, kills paid as a shot's.
 */
class BeamTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;
    private static final double NOSE_Y = Ship.START_Y + 21;

    private static void run(Sortie sortie, int steps, int commands) {
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
        }
    }

    /** The unit on the field whose spec is {@code spec}. */
    private static Enemy find(Sortie sortie, EnemySpec spec) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == spec) {
                return sortie.enemy(i);
            }
        }
        throw new AssertionError("not on the field: " + spec.slug());
    }

    /** Steps with fire held until {@code target} burns under the beam (at most 6 s). */
    private static void burnUntilHit(Sortie sortie, EnemySpec target) {
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(FIRE);
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                if (enemy.spec() == target && enemy.hp() < target.hp()) {
                    return;
                }
            }
        }
        throw new AssertionError("the beam never reached " + target.slug());
    }

    @Test
    void aBeamIsOneMuzzleAtTheStepRateAndItsDamageIsPerStep() {
        WeaponSpec beam = PartASpecs.ionBeam(1);
        assertEquals(SimStep.PER_SECOND, beam.rate());
        assertEquals(45.0 / 60, beam.damage(), 1e-12);
        assertEquals(6, beam.size().width());
        assertEquals(1, beam.intervalTicks());
        assertTrue(WeaponSpec.Delivery.BEAM.reaches(Layer.HIGH_AIR));
        assertTrue(WeaponSpec.Delivery.BEAM.reaches(Layer.GROUND));
        assertFalse(WeaponSpec.Delivery.BEAM.reaches(Layer.SUB));
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponSpec(
                        "two-beams",
                        "beam",
                        "beam",
                        WeaponSpec.Delivery.BEAM,
                        false,
                        SimStep.PER_SECOND,
                        1,
                        0,
                        new Hitbox(6, 0),
                        TestSpecs.INFINITE,
                        0,
                        1,
                        0,
                        0,
                        0,
                        0,
                        0,
                        List.of(PartASpecs.NOSE, PartASpecs.NOSE)));
    }

    @Test
    void itComesOnWithFireFiresNoShotsAndGoesOffOnRelease() {
        var sortie = sortie(TestSpecs.level(10, List.of()), beamMount(1));
        sortie.step(FIRE);
        Beam beam = sortie.beam(0);
        assertNotNull(beam);
        assertTrue(beam.on());
        assertEquals(1, sortie.events().count(SimEvents.Type.BEAM_ON));
        assertEquals(0, sortie.events().count(SimEvents.Type.SHOT_FIRED));
        assertEquals(X, beam.x(), 1e-12);
        assertEquals(NOSE_Y, beam.fromY(), 1e-12);
        // It grows to its full length in 0.05 s: a third of its reach after the first step.
        assertEquals(NOSE_Y + PlayField.HEIGHT / 3.0, beam.endY(), 1e-9);
        assertEquals(Beam.Contact.NONE, beam.contact());
        run(sortie, Beam.GROW_TICKS, FIRE);
        assertEquals(NOSE_Y + PlayField.HEIGHT, beam.endY(), 1e-9);
        assertEquals(0, sortie.shotCount());
        assertEquals(0, sortie.ticksSinceShot(0), "its muzzle glows while it runs");

        sortie.step(Command.NONE);
        assertFalse(beam.on());
        assertEquals(1, sortie.events().count(SimEvents.Type.BEAM_OFF));
        sortie.step(Command.NONE);
        assertEquals(0, sortie.events().count(SimEvents.Type.BEAM_OFF));
    }

    @Test
    void onlyTheFirstTargetInTheColumnBurnsAtItsDamagePerStep() {
        EnemySpec near = dummy(Layer.AIR, 1000);
        EnemySpec far = dummy(Layer.AIR, 1000);
        var sortie = sortie(units(unit(0, X, near), unit(0.3, X, far)), beamMount(1));
        burnUntilHit(sortie, near);
        Enemy first = find(sortie, near);
        double before = first.hp();
        sortie.step(FIRE);
        assertEquals(45.0 / 60, before - first.hp(), 1e-9, "one sixtieth of 45 a step");
        run(sortie, 30, FIRE);
        assertEquals(1000, find(sortie, far).hp(), "it never pierces");
        Beam beam = sortie.beam(0);
        assertEquals(Beam.Contact.BURN, beam.contact());
        assertEquals(first.serial(), beam.target());
        assertEquals(first.y() - 10 + Beam.BORE, beam.endY(), 1e-9, "it bores 4 px into it");
    }

    @Test
    void aTieGoesToTheHigherLayer() {
        EnemySpec ground = dummy(Layer.GROUND, 1000);
        EnemySpec air = dummy(Layer.AIR, 1000);
        EnemySpec high = dummy(Layer.HIGH_AIR, 1000);
        var sortie = sortie(units(unit(0, X, ground), unit(0, X, air)), beamMount(1));
        burnUntilHit(sortie, air);
        run(sortie, 20, FIRE);
        assertEquals(1000, find(sortie, ground).hp());

        sortie = sortie(units(unit(0, X, air), unit(0, X, high)), beamMount(1));
        burnUntilHit(sortie, high);
        run(sortie, 20, FIRE);
        assertEquals(1000, find(sortie, air).hp(), "high-air is reached and wins the tie");
    }

    @Test
    void hardenedGroundIsPassedOverWithAGlance() {
        EnemySpec bunker = PartCSpecs.hardenedTarget(1000);
        EnemySpec air = dummy(Layer.AIR, 1000);
        var container = new GroundObjectSpec(0, X, new Hitbox(32, 24), 50, 5, Optional.empty(), 0, 0, "", true);
        var sortie = sortie(units(List.of(container), unit(0, X, bunker), unit(0.8, X, air)), beamMount(1));
        burnUntilHit(sortie, air);
        assertEquals(1000, find(sortie, bunker).hp());
        Beam beam = sortie.beam(0);
        assertFalse(Double.isNaN(beam.glanceY()), "a glance where it crosses the hardened target");
        assertTrue(beam.glanceY() < beam.endY());
        assertFalse(sortie.groundObject(0).damaged(), "a hardened container takes nothing");
    }

    @Test
    void aDestructibleGroundObjectBurnsAndAKillPaysAsAShot() {
        var container = new GroundObjectSpec(0, X, new Hitbox(32, 24), 3, 5, Optional.empty(), 0, 0, "", false);
        var sortie = sortie(units(List.of(container)), beamMount(1));
        int destroyed = 0;
        for (int i = 0; i < SimStep.ticks(6) && destroyed == 0; i++) {
            sortie.step(FIRE);
            destroyed = sortie.events().count(SimEvents.Type.GROUND_DESTROYED);
        }
        assertEquals(1, destroyed);

        EnemySpec weak = dummy(Layer.AIR, 3);
        sortie = sortie(units(unit(0, X, weak)), beamMount(1));
        int kills = 0;
        for (int i = 0; i < SimStep.ticks(6) && kills == 0; i++) {
            sortie.step(FIRE);
            kills = sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
        }
        assertEquals(1, kills);
        assertEquals(1, sortie.kills());
    }

    @Test
    void theWidthDecidesWhatTheColumnTouches() {
        // A 20 px unit 14 px off the centre line: outside a 6 px column (L1), inside a 10 px one (L3).
        EnemySpec beside = dummy(Layer.AIR, 1000);
        var narrow = sortie(units(unit(0, X + 14, beside)), beamMount(1));
        run(narrow, SimStep.ticks(4), FIRE);
        assertEquals(1000, find(narrow, beside).hp());

        var wide = sortie(units(unit(0, X + 14, beside)), beamMount(3));
        burnUntilHit(wide, beside);
        assertEquals(10, wide.beam(0).width());
    }

    @Test
    void anOverdriveBurnsWithTheNextLevelsWidthAndDamage() {
        EnemySpec target = dummy(Layer.AIR, 1000);
        var sortie = sortie(units(unit(0, X, target)), beamMount(5));
        burnUntilHit(sortie, target);
        sortie.overdrive(2);
        Enemy enemy = find(sortie, target);
        double before = enemy.hp();
        sortie.step(FIRE);
        assertEquals(160.0 / 60, before - enemy.hp(), 1e-9);
        assertEquals(18, sortie.beam(0).width());
    }

    @Test
    void otherMountsHaveNoBeam() {
        var sortie = sortie(
                TestSpecs.level(10, List.of()),
                new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2));
        assertNull(sortie.beam(0));
    }

    /** Steps with fire held, the ship steered to {@code x} (within 3 px), at most 3 s. */
    private static void steerTo(Sortie sortie, double x) {
        for (int i = 0; i < SimStep.ticks(3) && Math.abs(sortie.ship().x() - x) > 3; i++) {
            sortie.step(FIRE | (sortie.ship().x() < x ? Command.RIGHT.bit() : Command.LEFT.bit()));
        }
        assertEquals(x, sortie.ship().x(), 3);
    }

    /**
     * Round 37 R.4 (c): the beam reaches high air, so a high-air boss's hull stops it like an armoured
     * body (the Brood Carrier in its first phase): over the hull, clear of its parts, the column ends at
     * the hull's lower edge without damage instead of running on past it.
     */
    @Test
    void aHighAirBossesHullStopsTheBeam() {
        var sortie = new Sortie(
                1,
                TestSpecs.loadout(beamMount(3)),
                TestSpecs.carrierLevel(TestSpecs.carrier(false, 1)),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 20, FIRE);
        for (int sac : TestSpecs.SACS) {
            sortie.destroyPart(0, sac);
        }
        SetPiece carrier = sortie.setPiece(0);
        steerTo(sortie, carrier.x() + 40);
        sortie.step(FIRE);
        assertEquals(0, carrier.phase());
        assertEquals(Layer.HIGH_AIR, carrier.layer());
        Beam beam = sortie.beam(0);
        assertEquals(Beam.Contact.BLOCKED, beam.contact(), "the hull stops it");
        assertEquals(-1, beam.target(), "without damage");
        assertEquals(carrier.y() - carrier.body().height() / 2 + Beam.BORE, beam.endY(), 1e-9);
    }

    /**
     * Round 37 R.4 (c): a set piece's armour stops the beam on the ground layer too (the Siege Spire's
     * trunk, its maw and core before their phases: a boss is not hardened ground, which is passed over),
     * at the lowest of them in the column; a root in the column burns.
     */
    @Test
    void anArenaBossesShieldedPartsStopTheBeamOnTheGround() {
        var sortie = new Sortie(
                1,
                TestSpecs.loadout(beamMount(3)),
                PartHSpecs.level(PartHSpecs.spire()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 10, FIRE);
        SetPiece spire = sortie.setPiece(0);
        steerTo(sortie, spire.x());
        sortie.step(FIRE);
        assertEquals(Layer.GROUND, spire.partLayer(PartHSpecs.MAW));
        assertTrue(spire.partShielded(PartHSpecs.MAW), "the maw takes no damage in the first phase");
        Beam beam = sortie.beam(0);
        assertEquals(Beam.Contact.BLOCKED, beam.contact(), "the shielded maw stops it");
        assertEquals(spire.y() + 20 - 24 + Beam.BORE, beam.endY(), 1e-9, "at the maw's lower edge");
        assertTrue(Double.isNaN(beam.glanceY()), "not passed over");

        steerTo(sortie, spire.x() + 190);
        sortie.step(FIRE);
        assertEquals(Beam.Contact.BURN, sortie.beam(0).contact(), "a root burns");
        assertEquals(Beam.PART_TARGET + PartHSpecs.POD_4, sortie.beam(0).target(), "pod 4, the lowest in the column");
    }
}
