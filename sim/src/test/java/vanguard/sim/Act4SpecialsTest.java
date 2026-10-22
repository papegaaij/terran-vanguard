package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part A, step A3 (design/player/specials, Act 4 specials in detail; m8a-gaps.md §1.4, §1.5): the setting limits
 * (the reason words {@code UNDER ICE} and {@code NO WATER}, a refused press spends no charge, a waterline changes the
 * state with its event, a level's or section's list replaces the medium's rule), the Sonar Pulse (its window opens
 * the medium's sonar: full damage under water, {@code sub} open to every weapon above water; its ring's contacts; a
 * call restarts the window), the Decoy Flares (the fan from the rear muzzle, the drift to a stop, the burn, the homing
 * missiles bursting on them), the Pressure hull (no drag on the ship) and the Auto-repair nanites (a point per
 * interval up to half the armour, a hit pausing them).
 */
class Act4SpecialsTest {
    private static final int SPECIAL = Command.SPECIAL.bit();
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;

    /** The numbers of design/player/specials (Sonar Pulse (L22), Decoy Flares (L27)). */
    static final SonarSpec SONAR = new SonarSpec(0.5, 6, 1.5);

    static final FlaresSpec FLARES = new FlaresSpec(4, 120, 300, 0.6, 4, 1.0);

    static SpecialSpec sonar(int charges) {
        return new SpecialSpec("Sonar Pulse", charges, 4, 0.1, SONAR).withSetting(SpecialSpec.Setting.WATER_ONLY);
    }

    static SpecialSpec flares(int charges) {
        return new SpecialSpec("Decoy Flares", charges, 6, 0.1, FLARES);
    }

    static SpecialSpec airstrike(int charges) {
        return new SpecialSpec("Airstrike", charges, 4, 0.1, AirstrikeTest.AIRSTRIKE)
                .withSetting(SpecialSpec.Setting.NOT_UNDER_WATER);
    }

    /** An empty dry level of 40 s. */
    private static LevelScript dry() {
        return TorpedoTest.units(false);
    }

    private static Sortie sortie(SpecialSpec special, LevelScript level) {
        return new Sortie(
                1,
                TestSpecs.loadout(UnderwaterTest.front(UnderwaterTest.BOLT)).withSpecial(special),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    private static void run(Sortie sortie, double seconds, int commands) {
        TorpedoTest.run(sortie, SimStep.ticks(seconds), commands);
    }

    /** The values of the events of {@code type} in the next {@code seconds}. */
    private static List<Integer> values(Sortie sortie, SimEvents.Type type, double seconds, int commands) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == type) {
                    values.add(events.value(e));
                }
            }
        }
        return values;
    }

    private static double maxShotDamage(Sortie sortie) {
        double most = 0;
        for (int i = 0; i < sortie.shotCount(); i++) {
            most = Math.max(most, sortie.shot(i).damage());
        }
        return most;
    }

    // ----- The specs -----

    @Test
    void theSpecsKeepTheirKindsSettingsAndReasonWords() {
        assertEquals(SpecialSpec.Kind.SONAR, sonar(1).kind());
        assertEquals(SpecialSpec.Kind.FLARES, flares(1).kind());
        assertEquals(SONAR, sonar(1).sonar());
        assertEquals(FLARES, flares(1).flares());
        assertNull(flares(1).sonar());
        assertEquals(SpecialSpec.Setting.ANYWHERE, flares(1).setting(), "the Acts 1–3 constructors work anywhere");
        assertEquals(SpecialSpec.Setting.WATER_ONLY, sonar(3).withCharges(1).setting(), "a charge change keeps it");
        assertEquals("UNDER ICE", SpecialSpec.Setting.NOT_UNDER_WATER.reason().word());
        assertEquals("NO WATER", SpecialSpec.Setting.WATER_ONLY.reason().word());
        assertEquals("", SpecialLimits.Reason.NONE.word());
        assertThrows(IllegalArgumentException.class, () -> new SonarSpec(0, 6, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new FlaresSpec(0, 120, 300, 0.6, 4, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpecialLimits(List.of(
                        new SpecialLimits.Switch(5, Optional.empty()), new SpecialLimits.Switch(5, Optional.empty()))));
        assertThrows(
                IllegalArgumentException.class,
                () -> dry().toBuilder()
                        .specialLimits(new SpecialLimits(List.of(new SpecialLimits.Switch(99, Optional.empty()))))
                        .build(),
                "a switch before the level's end");
    }

    // ----- The setting limits -----

    @Test
    void theSonarReadsNoWaterOnADryLevelAndARefusedPressSpendsNoCharge() {
        Sortie sortie = sortie(sonar(2), dry());
        sortie.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NO_WATER, sortie.special().limit());
        assertFalse(sortie.special().ready());
        List<Integer> denied = values(sortie, SimEvents.Type.SPECIAL_DENIED, 0.1, SPECIAL);
        assertEquals(List.of(SpecialLimits.Reason.NO_WATER.ordinal()), denied, "refused with its reason");
        assertEquals(2, sortie.special().charges());
        assertEquals(0, sortie.special().used());
        assertEquals(0, sortie.events().count(SimEvents.Type.SONAR_PULSE));

        Sortie surface = sortie(sonar(2), TorpedoTest.units(true));
        surface.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NONE, surface.special().limit(), "a level over water has water");
        Sortie under = sortie(sonar(2), UnderwaterTest.under(dry()));
        under.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NONE, under.special().limit());
    }

    @Test
    void theAirstrikeReadsUnderIceUnderWaterAndWorksAboveIt() {
        Sortie under = sortie(airstrike(2), UnderwaterTest.under(dry()));
        assertEquals(
                List.of(SpecialLimits.Reason.UNDER_ICE.ordinal()),
                values(under, SimEvents.Type.SPECIAL_DENIED, 0.1, SPECIAL));
        assertEquals(2, under.special().charges());
        assertEquals(SpecialLimits.Reason.UNDER_ICE, under.special().limit());

        Sortie air = sortie(airstrike(2), dry());
        assertEquals(List.of(), values(air, SimEvents.Type.SPECIAL_DENIED, 0.1, SPECIAL));
        assertEquals(1, air.special().charges(), "called");
        Sortie any = sortie(airstrike(2).withSetting(SpecialSpec.Setting.ANYWHERE), UnderwaterTest.under(dry()));
        any.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NONE, any.special().limit(), "only its setting limits it");
    }

    @Test
    void aWaterlineChangesTheStateOnceWithItsEvent() {
        LevelScript waterline = dry().toBuilder()
                .medium(new MediumSpec(UnderwaterTest.EUROPA, List.of(new MediumSpec.Switch(5, true))))
                .build();
        Sortie strike = sortie(airstrike(2), waterline);
        assertEquals(List.of(), values(strike, SimEvents.Type.SPECIAL_LIMIT, 4.9, Command.NONE), "none at the start");
        assertEquals(SpecialLimits.Reason.NONE, strike.special().limit());
        assertEquals(
                List.of(SpecialLimits.Reason.UNDER_ICE.ordinal()),
                values(strike, SimEvents.Type.SPECIAL_LIMIT, 1, Command.NONE));
        assertEquals(SpecialLimits.Reason.UNDER_ICE, strike.special().limit());

        Sortie sonar = sortie(sonar(2), waterline);
        sonar.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NO_WATER, sonar.special().limit(), "the dry shaft above the waterline");
        assertEquals(
                List.of(SpecialLimits.Reason.NONE.ordinal()),
                values(sonar, SimEvents.Type.SPECIAL_LIMIT, 5, Command.NONE),
                "usable from the waterline");
        assertTrue(sonar.special().ready());

        // A retry reads the limit afresh without an event.
        sonar.retry(TestSpecs.FULL_ARMOUR);
        assertEquals(List.of(), values(sonar, SimEvents.Type.SPECIAL_LIMIT, 1, Command.NONE));
        assertEquals(SpecialLimits.Reason.NO_WATER, sonar.special().limit());
    }

    @Test
    void aLevelsOrSectionsListReplacesTheMediumsRuleButTheSonarNeedsWaterAlways() {
        Set<SpecialSpec.Kind> strikes = Set.of(SpecialSpec.Kind.AIRSTRIKE, SpecialSpec.Kind.LANCE);
        Sortie listed = sortie(
                airstrike(2),
                dry().toBuilder().specialLimits(SpecialLimits.level(strikes)).build());
        listed.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.UNDER_ICE, listed.special().limit(), "forbidden by the list in the air");

        Sortie section = sortie(
                airstrike(2),
                dry().toBuilder()
                        .specialLimits(new SpecialLimits(List.of(
                                new SpecialLimits.Switch(0, Optional.of(strikes)),
                                new SpecialLimits.Switch(5, Optional.empty()))))
                        .build());
        assertEquals(
                List.of(SpecialLimits.Reason.NONE.ordinal()),
                values(section, SimEvents.Type.SPECIAL_LIMIT, 6, Command.NONE),
                "the next section has no list: the rule (dry) allows it");

        Sortie allowed = sortie(
                airstrike(2),
                UnderwaterTest.under(dry()).toBuilder()
                        .specialLimits(SpecialLimits.level(Set.of()))
                        .build());
        allowed.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NONE, allowed.special().limit(), "a list naming nothing allows it");

        Sortie ice = sortie(
                sonar(2),
                dry().toBuilder().specialLimits(SpecialLimits.level(Set.of())).build());
        ice.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NO_WATER, ice.special().limit(), "L22's ice: nothing listed, still dry");
        Sortie sonarListed = sortie(
                sonar(2),
                UnderwaterTest.under(dry()).toBuilder()
                        .specialLimits(SpecialLimits.level(Set.of(SpecialSpec.Kind.SONAR)))
                        .build());
        sonarListed.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.NO_WATER, sonarListed.special().limit(), "its own setting's word");
    }

    // ----- The Sonar Pulse -----

    @Test
    void thePulseOpensSixSecondsOfFullDamageUnderWaterAndACallRestartsThem() {
        Sortie sortie = sortie(sonar(3), UnderwaterTest.under(dry()));
        run(sortie, 1, FIRE);
        assertEquals(7, maxShotDamage(sortie), 1e-12, "× 0.7 before the pulse");
        sortie.step(SPECIAL | FIRE);
        assertEquals(1, sortie.events().count(SimEvents.Type.SPECIAL_CALLED));
        assertEquals(1, sortie.events().count(SimEvents.Type.SONAR_PULSE));
        assertTrue(sortie.special().sonarActive());
        assertEquals(2, sortie.special().charges());
        run(sortie, 0.5, FIRE);
        assertEquals(10, maxShotDamage(sortie), 1e-12, "full damage in the window");
        assertEquals(5.5, sortie.special().sonarSeconds(), 1e-9);

        // A second call after the repeat (1.5 s) restarts the 6 s; no stacking.
        run(sortie, 1, Command.NONE);
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.SONAR_PULSE));
        assertEquals(6, sortie.special().sonarSeconds(), 1e-9);
        List<Integer> ended = values(sortie, SimEvents.Type.SONAR_ENDED, 6, Command.NONE);
        assertEquals(1, ended.size(), "it closes once, 6 s after the second call");
        assertFalse(sortie.special().sonarActive());
        run(sortie, 2, Command.NONE);
        run(sortie, 0.3, FIRE);
        assertEquals(7, maxShotDamage(sortie), 1e-12, "the water's rule is back");
    }

    @Test
    void aPressWithinTheRepeatWaitsForItOrIsDenied() {
        Sortie sortie = sortie(sonar(3), UnderwaterTest.under(dry()));
        sortie.step(SPECIAL);
        sortie.step(Command.NONE);
        assertEquals(List.of(0), values(sortie, SimEvents.Type.SPECIAL_DENIED, 0.5, SPECIAL), "busy: no reason");
        assertEquals(2, sortie.special().charges());
    }

    @Test
    void abovetheWaterThePulseOpensTheSubLayerToEveryWeaponAndItsRingFindsTheContacts() {
        Supplier<Sortie> make = () ->
                sortie(sonar(2), TorpedoTest.units(true, new GroundUnit(0, X, TorpedoTest.dummy(Layer.SUB, 1000), -1)));
        Sortie closed = make.get();
        assertEquals(0, TorpedoTest.count(closed, SimStep.ticks(3), FIRE, SimEvents.Type.ENEMY_HIT));

        Sortie open = make.get();
        run(open, 0.5, Command.NONE);
        open.step(SPECIAL);
        List<Integer> contacts = values(open, SimEvents.Type.SONAR_CONTACT, 0.5, Command.NONE);
        assertEquals(List.of(open.enemy(0).serial()), contacts, "its contact pings once as the ring reaches it");
        assertTrue(TorpedoTest.count(open, SimStep.ticks(2), FIRE, SimEvents.Type.ENEMY_HIT) > 0);
        double dealt = 1000 - open.enemy(0).hp();
        assertEquals(0, dealt % 10, 1e-9, "above water the damage is untouched: " + dealt);
    }

    @Test
    void thePulseReplaysToTheSameHashAndDoesNotAllocate() {
        Supplier<Sortie> make = () -> sortie(
                sonar(4),
                UnderwaterTest.under(
                        TorpedoTest.units(false, new GroundUnit(0, X, TorpedoTest.dummy(Layer.AIR, 1000), -1))));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < 1200; i++) {
            int command = (i % 300 == 20 ? SPECIAL : 0) | FIRE;
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        Sortie unfitted = sortie(flares(1), UnderwaterTest.under(TorpedoTest.units(false)));
        assertNotEquals(make.get().stateHash(), unfitted.stateHash());
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step((i % 250 == 20 ? SPECIAL : 0) | FIRE);
            }
        });
        assertEquals(0, allocated);
    }

    // ----- The Decoy Flares -----

    @Test
    void theFlaresFanOutBackwardsSlowToAStopAndBurnFourSeconds() {
        Sortie sortie = sortie(flares(6), dry());
        run(sortie, 0.2, Command.NONE);
        double x = sortie.ship().x();
        double y = sortie.ship().y() - Flares.REAR_OFFSET;
        List<Integer> launched = values(sortie, SimEvents.Type.FLARES_LAUNCHED, 1.0 / 120, SPECIAL);
        assertEquals(List.of(4), launched);
        assertEquals(5, sortie.special().charges());
        Flares flares = sortie.special().flares();
        assertEquals(4, flares.size());
        run(sortie, 0.6, Command.NONE);
        double[] expected = {-60, -20, 20, 60};
        for (int k = 0; k < 4; k++) {
            Flares.Flare flare = flares.get(k);
            double angle = Math.toRadians(expected[k]);
            // 300 px/s slowing to 0 over 0.6 s: 90 px from the rear muzzle, down the screen.
            assertEquals(x - Math.sin(angle) * 90, flare.x(), 1e-5, "flare " + k);
            assertEquals(y - Math.cos(angle) * 90, flare.y(), 1e-5, "flare " + k);
        }
        double restX = flares.get(0).x();
        run(sortie, 1, Command.NONE);
        assertEquals(restX, flares.get(0).x(), 1e-12, "at rest after its drift");
        run(sortie, 2.3, Command.NONE);
        assertEquals(4, flares.size(), "still burning just before 4 s");
        run(sortie, 0.1, Command.NONE);
        assertEquals(0, flares.size(), "burnt out at 4 s");

        Sortie water = sortie(flares(6), UnderwaterTest.under(dry()));
        run(water, 0.2, Command.NONE);
        double wy = water.ship().y() - Flares.REAR_OFFSET;
        water.step(SPECIAL);
        run(water, 1, Command.NONE);
        assertEquals(
                wy - Math.cos(Math.toRadians(-60)) * 63,
                water.special().flares().get(0).y(),
                1e-6,
                "× 0.7");
    }

    private static final EnemyGun POD = EnemyGun.aimed(2, 0, 1, 200, 6, false)
            .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.PI, 4, 2, 4, 0.4)));

    private static List<Integer> missileEnds(Sortie sortie, double seconds) {
        return values(sortie, SimEvents.Type.MISSILE_DESTROYED, seconds, Command.NONE);
    }

    @Test
    void aHomingMissileGoesForTheNearestBurningFlareAndBurstsOnIt() {
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(flares(6)), dry(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        run(sortie, 0.2, Command.NONE);
        sortie.step(SPECIAL);
        run(sortie, 0.6, Command.NONE);
        Missiles missiles = sortie.missiles();
        assertTrue(
                missiles.launch(440, 300, POD, sortie.ship().x(), sortie.ship().y()));
        assertEquals(List.of(Missiles.DECOYED), missileEnds(sortie, 3));
        assertEquals(4, sortie.special().flares().size(), "the flare burns on");
        assertEquals(TestSpecs.FULL_ARMOUR, sortie.ship().defences().armour());

        // Once the flares burnt out the missiles go for the ship again.
        run(sortie, 3.5, Command.NONE);
        assertEquals(0, sortie.special().flares().size());
        missiles.launch(440, 300, POD, sortie.ship().x(), sortie.ship().y());
        assertEquals(List.of(Missiles.STRUCK), missileEnds(sortie, 4));
    }

    @Test
    void theFlaresReplayToTheSameHashAndDoNotAllocate() {
        Supplier<Sortie> make = () ->
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(flares(6)), dry(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < 900; i++) {
            int command = i % 150 == 10 ? SPECIAL : 0;
            if (i % 200 == 100) {
                a.missiles().launch(440, 300, POD, a.ship().x(), a.ship().y());
                b.missiles().launch(440, 300, POD, b.ship().x(), b.ship().y());
            }
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < 900; i++) {
                if (i % 200 == 100) {
                    sortie.missiles()
                            .launch(
                                    440,
                                    300,
                                    POD,
                                    sortie.ship().x(),
                                    sortie.ship().y());
                }
                sortie.step(i % 150 == 10 ? SPECIAL : 0);
            }
        });
        assertEquals(0, allocated);
    }

    // ----- The Pressure hull and the Auto-repair nanites -----

    @Test
    void thePressureHullTakesTheDragOffTheShipOnly() {
        Supplier<Loadout> loadout = () -> TestSpecs.loadout(UnderwaterTest.front(UnderwaterTest.BOLT));
        Sortie air = new Sortie(1, loadout.get(), dry(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Sortie hull = new Sortie(
                1,
                loadout.get().withPressureHull(),
                UnderwaterTest.under(dry()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Sortie bare = new Sortie(1, loadout.get(), UnderwaterTest.under(dry()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int right = Command.RIGHT.bit();
        for (Sortie sortie : List.of(air, hull, bare)) {
            run(sortie, 0.1, Command.NONE);
            sortie.step(right);
        }
        double step = TestSpecs.SHIP.speed() / TestSpecs.SHIP.accelerationSeconds() * SimStep.SECONDS;
        assertEquals(step, air.ship().vx(), 1e-9);
        assertEquals(step, hull.ship().vx(), 1e-9, "as on the surface");
        assertEquals(step * 0.6, bare.ship().vx(), 1e-9);
        assertEquals(
                new Sortie(1, loadout.get(), UnderwaterTest.under(dry()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR)
                        .stateHash(),
                new Sortie(
                                1,
                                loadout.get().withPressureHull(),
                                UnderwaterTest.under(dry()),
                                TestSpecs.RULES,
                                TestSpecs.FULL_ARMOUR)
                        .stateHash(),
                "the hull hashes nothing of its own (its effect shows in the ship's speed)");
    }

    private static Defences nanites(double interval) {
        Defences defences = new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25);
        defences.nanites(new NaniteSpec(interval, 0.5, 1));
        return defences;
    }

    private static void steps(Defences defences, int steps) {
        for (int i = 0; i < steps; i++) {
            defences.step();
        }
    }

    @Test
    void theNanitesRepairAPointPerIntervalUpToHalfTheArmour() {
        Defences defences = nanites(4);
        defences.restore(20);
        assertTrue(defences.repairing());
        steps(defences, SimStep.ticks(4) - 1);
        assertEquals(20, defences.armour());
        defences.step();
        assertEquals(21, defences.armour());
        assertEquals(1, defences.repaired());

        Defences fast = nanites(2);
        fast.restore(20);
        steps(fast, SimStep.ticks(4));
        assertEquals(22, fast.armour(), "level 2: a point per 2 s");

        Defences capped = nanites(2);
        capped.restore(29.5);
        steps(capped, SimStep.ticks(10));
        assertEquals(30, capped.armour(), "never above half of 60");
        assertFalse(capped.repairing());
        Defences above = nanites(2);
        above.restore(45);
        steps(above, SimStep.ticks(10));
        assertEquals(45, above.armour(), "nothing above half");
    }

    @Test
    void aHitPausesTheNanitesForASecondAndAWreckIsNotRepaired() {
        SimEvents events = new SimEvents(16);
        Defences defences = nanites(4);
        defences.restore(20);
        steps(defences, SimStep.ticks(2));
        defences.takeShot(1, events, 0, 0);
        assertFalse(defences.repairing(), "paused");
        steps(defences, SimStep.ticks(1));
        assertTrue(defences.repairing());
        steps(defences, SimStep.ticks(2) - 1);
        assertEquals(20, defences.armour(), "the progress held through the pause");
        defences.step();
        assertEquals(21, defences.armour());

        Defences wreck = nanites(4);
        wreck.restore(20);
        wreck.takeShot(1000, events, 0, 0);
        assertEquals(0, wreck.armour());
        steps(wreck, SimStep.ticks(10));
        assertEquals(0, wreck.armour());
        assertFalse(wreck.repairing());
    }

    @Test
    void theNanitesWorkInFlightAndHashOnlyWhenFitted() {
        Loadout fitted = TestSpecs.LOADOUT.withNanites(new NaniteSpec(4, 0.5, 1));
        Sortie sortie = new Sortie(1, fitted, dry(), TestSpecs.RULES, 20);
        run(sortie, 4, Command.NONE);
        assertEquals(21, sortie.ship().defences().armour());
        assertNotEquals(
                new Sortie(1, fitted, dry(), TestSpecs.RULES, 20).stateHash(),
                new Sortie(1, TestSpecs.LOADOUT, dry(), TestSpecs.RULES, 20).stateHash());
        long allocated = Allocations.least(() -> new Sortie(1, fitted, dry(), TestSpecs.RULES, 20), s -> {
            for (int i = 0; i < 900; i++) {
                s.step(Command.NONE);
            }
        });
        assertEquals(0, allocated);
    }
}
