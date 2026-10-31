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
import org.junit.jupiter.api.Test;

/**
 * M7 part A, the Act 3 specials (design/player/specials, Act 3 specials in detail): the EMP Burst's
 * stun (each unit's own clock stops, air units drift on, nothing jumps when it ends, a machine longer,
 * chains whole, a latched Lamprey stays latched without draining), the Orbital Lance's steered column
 * and boss-part cap, the Shield Overcharge in the defences, and the cooldown special's rules.
 */
class Act3SpecialsTest {
    private static final int SPECIAL = Command.SPECIAL.bit();
    /** The numbers of design/player/specials/data.yaml. */
    static final EmpSpec EMP = new EmpSpec(0.4, 3, 1.5, 1.0);

    static final LanceSpec LANCE = new LanceSpec(0.6, 40, 3, 0.3, 240, 400, 200, 400, 1.0);
    static final OverchargeSpec OVERCHARGE = new OverchargeSpec(2, 8, 0.5);

    static SpecialSpec emp(int charges) {
        return new SpecialSpec("EMP Burst", charges, 3, 0.1, EMP);
    }

    static SpecialSpec lance(int charges) {
        return new SpecialSpec("Orbital Lance", charges, 2, 0.1, LANCE);
    }

    static SpecialSpec overcharge() {
        return SpecialSpec.cooldown("Shield Overcharge", 0.1, 45, 10, OVERCHARGE);
    }

    /** A Needler (hovering, then circling) firing a harmless thorn every 0.25 s. */
    private static final EnemySpec GUNNER = TestSpecs.needler(EnemyGun.aimed(0.25, 0, 1, 150, 0, false));

    private static Sortie sortie(SpecialSpec special, EnemySpec enemy) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(special),
                TestSpecs.level(
                        40,
                        List.of(TestSpecs.wave(
                                0,
                                WaveSpec.Formation.LINE_ABREAST,
                                enemy,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    private static void runTo(Sortie sortie, double seconds) {
        while (sortie.levelSeconds() < seconds) {
            sortie.step(Command.NONE);
        }
    }

    /** Steps until the first unit is stunned (the EMP pressed first); returns that unit. */
    private static Enemy burstAndWait(Sortie sortie) {
        sortie.step(SPECIAL);
        for (int i = 0; i < 60; i++) {
            if (sortie.events().count(SimEvents.Type.EMP_STUNNED) > 0) {
                return sortie.enemy(0);
            }
            sortie.step(Command.NONE);
        }
        throw new AssertionError("nothing stunned");
    }

    @Test
    void theSpecsKeepTheirKindsAndACooldownSpecialCarriesNoCharges() {
        assertEquals(SpecialSpec.Kind.EMP, emp(1).kind());
        assertEquals(SpecialSpec.Kind.LANCE, lance(1).kind());
        assertEquals(SpecialSpec.Kind.OVERCHARGE, overcharge().kind());
        assertEquals(
                SpecialSpec.Kind.AIRSTRIKE, new SpecialSpec("Airstrike", 1, 4, 0.1, AirstrikeTest.AIRSTRIKE).kind());
        assertTrue(overcharge().cooldown());
        assertFalse(emp(1).cooldown());
        assertNull(emp(1).smartBomb());
        assertEquals(overcharge(), overcharge().withCharges(3));
        assertThrows(IllegalArgumentException.class, () -> new SpecialSpec("x", 1, 0, 0.1, 45, 10, OVERCHARGE));
        assertThrows(IllegalArgumentException.class, () -> new SpecialSpec("x", 4, 3, 0.1, EMP));
    }

    @Test
    void aStunnedUnitFiresNothingWhileItsHoldWaitsAndThenFiresAgain() {
        Sortie sortie = sortie(emp(1), GUNNER);
        runTo(sortie, 2.5);
        Enemy gunner = burstAndWait(sortie);
        assertTrue(gunner.empStunned());
        assertEquals(SimStep.ticks(1.5), gunner.empStunTicks());
        double stepX = gunner.x() - gunner.renderX(0);
        double stepY = gunner.y() - gunner.renderY(0);
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            double x = gunner.x();
            double y = gunner.y();
            double facing = gunner.facing();
            sortie.step(Command.NONE);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            assertEquals(x + stepX, gunner.x(), 1e-9, "it drifts on along its last step");
            assertEquals(y + stepY, gunner.y(), 1e-9);
            assertEquals(facing, gunner.facing(), "it does not turn");
        }
        assertEquals(0, fired);
        assertFalse(gunner.empStunned());
        double most = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            double x = gunner.x();
            double y = gunner.y();
            sortie.step(Command.NONE);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            most = Math.max(most, Math.hypot(gunner.x() - x, gunner.y() - y));
        }
        assertTrue(fired > 0, "it fires again");
        assertTrue(most < 120 * SimStep.SECONDS * 1.5, "no jump when the stun ends: " + most);
    }

    @Test
    void aUnitOnItsPathDriftsOffItAndResumesItShiftedWithoutAJump() {
        Sortie sortie = sortie(emp(1), TestSpecs.SKITTER);
        runTo(sortie, 1.6);
        Enemy skitter = burstAndWait(sortie);
        double stepX = skitter.x() - skitter.renderX(0);
        double stepY = skitter.y() - skitter.renderY(0);
        assertTrue(Math.hypot(stepX, stepY) > 1, "it was flying its path");
        double most = 0;
        for (int i = 0; i < SimStep.ticks(2.5) && sortie.enemyCount() > 0; i++) {
            double x = skitter.x();
            double y = skitter.y();
            sortie.step(Command.NONE);
            most = Math.max(most, Math.hypot(skitter.x() - x, skitter.y() - y));
        }
        assertTrue(most < 190 * SimStep.SECONDS * 1.6, "no jump when the stun ends: " + most);
    }

    @Test
    void aMachineIsStunnedThreeSecondsAndANewBurstRestartsTheStun() {
        Sortie sortie = sortie(emp(2), GUNNER.withMachine(true));
        runTo(sortie, 2.5);
        Enemy gunner = burstAndWait(sortie);
        assertEquals(SimStep.ticks(3), gunner.empStunTicks());
        runTo(sortie, sortie.levelSeconds() + 1.2);
        int left = gunner.empStunTicks();
        assertTrue(left < SimStep.ticks(2));
        burstAndWait(sortie);
        assertEquals(SimStep.ticks(3), gunner.empStunTicks(), "restarted, not added up");
    }

    @Test
    void aBurstUsesAChargeIsBusyForItsRepeatAndLeavesTheUnitsUnharmed() {
        Sortie sortie = sortie(emp(1), GUNNER);
        runTo(sortie, 2.5);
        double hp = sortie.enemy(0).hp();
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.EMP_BURST));
        assertEquals(0, sortie.special().charges());
        assertTrue(sortie.special().busy());
        runTo(sortie, sortie.levelSeconds() + 0.5);
        assertTrue(sortie.special().empRadius() == 0 && sortie.special().busy());
        runTo(sortie, sortie.levelSeconds() + 0.6);
        assertFalse(sortie.special().busy());
        assertEquals(hp, sortie.enemy(0).hp());
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.SPECIAL_DENIED));
    }

    @Test
    void aLatchedLampreyStaysLatchedAndItsDrainPauses() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(emp(1)),
                TestSpecs.level(
                        30,
                        List.of(TestSpecs.wave(
                                0,
                                WaveSpec.Formation.SINGLE,
                                LampreyTest.LAMPREY,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        int steps = 0;
        while (sortie.latched() == 0 && steps++ < 600) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, sortie.latched());
        for (int i = 0; i < 30; i++) {
            sortie.step(Command.NONE);
        }
        Enemy lamprey = burstAndWait(sortie);
        assertTrue(lamprey.latched());
        assertFalse(sortie.draining());
        double shield = sortie.ship().defences().shield();
        for (int i = 0; i < SimStep.ticks(1.5) - 1; i++) {
            sortie.step(Command.of(Command.RIGHT));
            assertTrue(lamprey.latched());
            assertEquals(sortie.ship().x() - 21, lamprey.x(), 1e-9, "on its spot");
        }
        assertEquals(shield, sortie.ship().defences().shield(), 1e-9, "no drain");
        sortie.step(Command.NONE);
        sortie.step(Command.NONE);
        assertTrue(sortie.draining());
        assertTrue(sortie.ship().defences().shield() < shield);
    }

    private static Sortie chainSortie(SpecialSpec special) {
        WaveSpec chain = WaveSpec.builder(
                        1, WaveSpec.Formation.SNAKE, FarsideTest.COILWYRM, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(
                        new WaveSpec.At(PlayField.WIDTH / 2.0, -60),
                        new WaveSpec.At(PlayField.WIDTH / 2.0, 300),
                        new WaveSpec.At(PlayField.WIDTH / 2.0, 700))))
                .build();
        LevelScript level = LevelScript.builder(6, 1)
                .sections(List.of(new Section(40, 130)))
                .waves(List.of(chain))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0, 50, List.of(), "mantis", List.of(), "")))
                .build();
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(special),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void aChainStopsWholeAndFliesOnAfterItsStun() {
        Sortie sortie = chainSortie(emp(1));
        runTo(sortie, 3);
        Chain chain = sortie.chain(0);
        burstAndWait(sortie);
        assertTrue(chain.empStunned());
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        for (int i = 0; i < chain.size(); i++) {
            if (chain.member(i) != null) {
                assertTrue(chain.member(i).empStunned(), "member " + i);
                xs.add(chain.member(i).x());
                ys.add(chain.member(i).y());
            }
        }
        double head = chain.headY();
        for (int i = 0; i < SimStep.ticks(1.5) - 1; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(head, chain.headY());
        for (int i = 0, k = 0; i < chain.size(); i++) {
            if (chain.member(i) != null) {
                assertEquals(xs.get(k), chain.member(i).x(), 1e-9);
                assertEquals(ys.get(k++), chain.member(i).y(), 1e-9);
            }
        }
        sortie.step(Command.NONE);
        sortie.step(Command.NONE);
        assertFalse(chain.empStunned());
        assertNotEquals(head, chain.headY(), "it flies on");
    }

    @Test
    void aBurstAndItsStunsDoNotAllocate() {
        long allocated = Allocations.least(() -> sortie(emp(3), GUNNER), sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step(i % 200 == 150 ? SPECIAL : Command.NONE);
            }
        });
        assertEquals(0, allocated);
        long chains = Allocations.least(() -> chainSortie(emp(3)), sortie -> {
            for (int i = 0; i < 600; i++) {
                sortie.step(i % 150 == 120 ? SPECIAL : Command.NONE);
            }
        });
        assertEquals(0, chains);
    }

    @Test
    void aBurstReplaysToTheSameHash() {
        Sortie a = sortie(emp(3), GUNNER);
        Sortie b = sortie(emp(3), GUNNER);
        for (int i = 0; i < 900; i++) {
            int command = i % 200 == 150 ? SPECIAL : Command.NONE;
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
    }

    // ----- The Orbital Lance -----

    /** A target fixed to the ground on {@code layer} at x, with plenty of hit points. */
    private static EnemySpec target(String slug, Layer layer, boolean hardened) {
        EnemySpec plain = EnemySpec.of(slug, 10000, new Hitbox(20, 20), layer, 0, false, 10, 0)
                .withTerrain(true);
        if (!hardened) {
            return plain;
        }
        return plain.withHardened(true);
    }

    private static LevelScript targets(List<SetPieceSpec> pieces) {
        double t = 0;
        List<GroundUnit> units = List.of(
                new GroundUnit(t, Ship.START_X, target("in-air", Layer.AIR, false), -1),
                new GroundUnit(t, Ship.START_X + 10, target("in-hardened", Layer.GROUND, true), -1),
                new GroundUnit(t, Ship.START_X - 10, target("in-high", Layer.HIGH_AIR, false), -1),
                new GroundUnit(t, Ship.START_X + 60, target("out", Layer.GROUND, false), -1));
        return LevelScript.builder(4, 1)
                .sections(List.of(new Section(60, 30)))
                .groundUnits(units)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .setPieces(pieces)
                .build();
    }

    private static Sortie lanceSortie(List<SetPieceSpec> pieces) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(lance(2)),
                targets(pieces),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    private static double damage(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return 10000 - sortie.enemy(i).hp();
            }
        }
        throw new AssertionError("no " + slug);
    }

    @Test
    void afterItsLockTheLanceBurnsEveryLayerButSubIn40PxAt400PerSecond() {
        Sortie sortie = lanceSortie(List.of());
        runTo(sortie, 2);
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.LANCE_LOCK));
        assertTrue(sortie.special().lanceLocking());
        int fired = -1;
        for (int i = 1; i <= SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.LANCE_FIRED) > 0) {
                fired = i;
            }
        }
        assertEquals(SimStep.ticks(0.6), fired);
        assertEquals(1200, damage(sortie, "in-air"), 1e-6);
        assertEquals(1200, damage(sortie, "in-hardened"), 1e-6, "from above, hardened included");
        assertEquals(1200, damage(sortie, "in-high"), 1e-6);
        assertEquals(0, damage(sortie, "out"));
        assertFalse(sortie.special().busy(), "1 s after its fade");
        assertEquals(1, sortie.special().charges());
    }

    @Test
    void itsCentreFollowsTheShipAtUpTo240PxPerSecond() {
        Sortie sortie = lanceSortie(List.of());
        runTo(sortie, 1);
        sortie.step(SPECIAL);
        double x = sortie.special().lanceX(1);
        for (int i = 0; i < 40; i++) {
            sortie.step(Command.RIGHT.bit());
            double now = sortie.special().lanceX(1);
            assertTrue(now - x <= 240 * SimStep.SECONDS + 1e-9);
            x = now;
        }
        assertTrue(sortie.ship().x() - x > 10, "it lags a fast move");
    }

    private static void steps(Sortie sortie, int count) {
        for (int i = 0; i < count; i++) {
            sortie.step(Command.NONE);
        }
    }

    /** A set piece holding over the start: a weak point (×2) right above the ship, its vital core aside. */
    private static SetPieceSpec gunship() {
        return new SetPieceSpec(
                "gunship",
                new Hitbox(300, 100),
                new Hitbox(260, 60),
                0,
                List.of(
                        new PartSpec("eye", 0, 0, new Hitbox(30, 30), 5000, false, 10, Optional.empty(), 0, 2),
                        new PartSpec("core", 120, 0, new Hitbox(30, 30), 5000, true, 10, Optional.empty(), 0, 1)),
                List.of(Pass.flight(
                        "hold",
                        Layer.AIR,
                        0,
                        List.of(new Waypoint(0, Ship.START_X, 400), new Waypoint(60, Ship.START_X, 400)))),
                Optional.empty());
    }

    @Test
    void aBossPartTakes200PerSecondWithoutItsWeakPointAndAtMost400PerLance() {
        Sortie sortie = lanceSortie(List.of(gunship()));
        runTo(sortie, 1);
        SetPiece piece = sortie.setPiece(0);
        assertTrue(piece.present());
        int eye = 0;
        assertEquals(Ship.START_X, piece.partX(eye), 1e-6);
        sortie.step(SPECIAL);
        // The call's step is the lock's first; the burn's first comes 36 steps later.
        steps(sortie, 95);
        assertEquals(5000 - 200, piece.partHp(eye), 1e-6, "60 steps at 200 a second, no ×2");
        steps(sortie, 200);
        assertFalse(sortie.special().busy());
        assertEquals(5000 - 400, piece.partHp(eye), 1e-6, "the cap");
        assertEquals(5000, piece.partHp(1), "outside the column");
        sortie.step(SPECIAL);
        steps(sortie, 300);
        assertEquals(5000 - 800, piece.partHp(eye), 1e-6, "a new lance, a new cap");
    }

    @Test
    void aLanceDoesNotAllocate() {
        long allocated = Allocations.least(() -> lanceSortie(List.of(gunship())), sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step(i % 400 == 10 ? SPECIAL : (i / 50) % 2 == 0 ? Command.RIGHT.bit() : Command.LEFT.bit());
            }
        });
        assertEquals(0, allocated);
    }

    // ----- The Shield Overcharge and the cooldown -----

    private final Defences defences = new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25);
    private final SimEvents events = new SimEvents(16);

    @Test
    void anOverchargeFillsToTwiceTheCapacityRegeneratesTowardsItAndBleedsTheExcess() {
        defences.takeShot(30, events, 0, 0);
        assertTrue(defences.broken());
        double armour = defences.armour();
        defences.overcharge(2, 480, 30);
        assertEquals(40, defences.shield());
        assertFalse(defences.broken(), "the break cancelled");
        assertTrue(defences.overcharged());
        assertEquals(20, defences.maxShield(), "the HUD keeps its scale");
        assertEquals(40, defences.shieldCapacity());
        assertEquals(armour, defences.armour(), "no repair");
        stepDefences(20);
        defences.takeShot(10, events, 0, 0);
        assertEquals(30, defences.shield(), 1e-9);
        // 2 s regen delay, then 10 points at 2 a second.
        stepDefences(440);
        assertEquals(40, defences.shield(), 1e-9, "regenerated towards twice the capacity");
        stepDefences(20);
        assertFalse(defences.overcharged());
        assertTrue(defences.bleeding());
        assertTrue(defences.shield() > 20);
        stepDefences(30);
        assertEquals(20, defences.shield(), 1e-9, "the excess bled away over 0.5 s");
        assertFalse(defences.bleeding());
    }

    private void stepDefences(int count) {
        for (int i = 0; i < count; i++) {
            defences.step();
        }
    }

    @Test
    void theOverchargeEndsAtAnAttemptsStartAndAShieldCellFillsOnlyToTheRaisedCapacity() {
        defences.overcharge(2, 480, 30);
        defences.takeShot(5, events, 0, 0);
        defences.restoreShield(10);
        assertEquals(40, defences.shield());
        defences.restore(60);
        assertFalse(defences.overcharged());
        assertEquals(20, defences.shield());
    }

    private static Sortie overchargeSortie() {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(overcharge()),
                TestSpecs.level(120, List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void theCooldownSpecialIsReadyAtTheStartCoolsFortyFiveSecondsAndDeniesPressesMeanwhile() {
        Sortie sortie = overchargeSortie();
        runTo(sortie, 1);
        assertTrue(sortie.special().ready());
        assertTrue(sortie.special().cooldown());
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.OVERCHARGE));
        assertTrue(sortie.ship().defences().overcharged());
        assertEquals(40, sortie.ship().defences().shield());
        assertEquals(0, sortie.special().charges());
        assertEquals(0, sortie.special().used(), "nothing for the campaign to take off");
        assertEquals(45, sortie.special().cooldownSeconds(), 1e-9);
        int ended = -1;
        int ready = -1;
        for (int i = 1; i <= SimStep.ticks(45); i++) {
            sortie.step(i == SimStep.ticks(10) ? SPECIAL : Command.NONE);
            if (i == SimStep.ticks(10)) {
                assertEquals(1, sortie.events().count(SimEvents.Type.SPECIAL_DENIED), "cooling: denied at once");
            }
            if (sortie.events().count(SimEvents.Type.OVERCHARGE_ENDED) > 0) {
                ended = i;
            }
            if (sortie.events().count(SimEvents.Type.SPECIAL_READY) > 0) {
                ready = i;
            }
        }
        assertEquals(SimStep.ticks(8), ended);
        assertEquals(SimStep.ticks(45), ready);
        assertTrue(sortie.special().ready());
        assertEquals(1, sortie.special().cooldownShare(), 1e-9);
    }

    @Test
    void aPressWithinTheBufferBeforeItIsReadyActivatesItWhenReady() {
        Sortie sortie = overchargeSortie();
        runTo(sortie, 1);
        sortie.step(SPECIAL);
        steps(sortie, SimStep.ticks(45) - 6);
        // 5 steps left: within the 0.1 s buffer.
        sortie.step(SPECIAL);
        assertEquals(0, sortie.events().count(SimEvents.Type.SPECIAL_DENIED));
        int activated = -1;
        for (int i = 1; i <= 10; i++) {
            sortie.step(SPECIAL);
            if (sortie.events().count(SimEvents.Type.OVERCHARGE) > 0) {
                activated = i;
            }
        }
        assertEquals(5, activated);
    }

    @Test
    void aSpecialChargePickupTakesTenSecondsOffARunningCooldownAndNothingWhenReady() {
        Sortie sortie = overchargeSortie();
        runTo(sortie, 1);
        assertTrue(
                sortie.drop(
                        PickupType.SPECIAL_CHARGE,
                        sortie.ship().x(),
                        sortie.ship().y()),
                "dropped");
        runTo(sortie, 2);
        assertEquals(0, sortie.special().found());
        assertTrue(sortie.special().ready());
        sortie.step(SPECIAL);
        runTo(sortie, 3);
        double before = sortie.special().cooldownSeconds();
        sortie.drop(PickupType.SPECIAL_CHARGE, sortie.ship().x(), sortie.ship().y());
        runTo(sortie, 3.5);
        assertEquals(before - 0.5 - 10, sortie.special().cooldownSeconds(), 0.05);
        assertEquals(0, sortie.special().found(), "no charge for the campaign");
    }

    @Test
    void aRetryStartsWithTheCooldownSpecialReady() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(overcharge()),
                TestSpecs.level(120, List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        runTo(sortie, 1);
        sortie.step(SPECIAL);
        assertFalse(sortie.special().ready());
        sortie.special().reset();
        sortie.ship().reset(TestSpecs.FULL_ARMOUR);
        assertTrue(sortie.special().ready());
        assertFalse(sortie.ship().defences().overcharged());
    }

    @Test
    void anOverchargedRunReplaysAndDoesNotAllocate() {
        Sortie a = overchargeSortie();
        Sortie b = overchargeSortie();
        for (int i = 0; i < 1200; i++) {
            int command = i % 300 == 60 ? SPECIAL : Command.NONE;
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(Act3SpecialsTest::overchargeSortie, sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step(i % 300 == 60 ? SPECIAL : Command.NONE);
            }
        });
        assertEquals(0, allocated);
    }
}
