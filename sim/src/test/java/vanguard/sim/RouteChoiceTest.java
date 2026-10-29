package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1 (design/campaign Level 32, The forks; m9e-gaps.md §1 #2–3): the branch is the channel the ship's
 * centre is in when the fork's island's tip reaches the ship's row; the chosen side's waves fly and its ground units
 * and objects wake (or enter awake), the other side's waves never come and its ground units and objects stay dormant
 * (never firing, never hit, never paying) and count nowhere; the choice's event and its {@code branch} radio cue; a
 * replay of each route is identical.
 */
class RouteChoiceTest {
    private static Sortie sortie() {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                Act5ESpecs.forkLevel(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** What a flight on one side saw. */
    private record Flight(
            int chosenAtTick,
            int chosenValue,
            int lines,
            int skitters,
            int skittersOnTheOtherSide,
            int awakeNests,
            int woke,
            int caches,
            int blasts,
            long hash,
            int enemyTotal) {}

    /** Flies 22 s holding the {@code side} channel, firing or not. */
    private static Flight fly(int side, boolean fire) {
        Sortie sortie = sortie();
        int chosenAt = -1;
        int value = -1;
        int lines = 0;
        int woke = 0;
        int caches = 0;
        int blasts = 0;
        int skitters = 0;
        int otherSide = 0;
        int seen = 0;
        boolean[] awakeSerials = new boolean[64];
        for (int step = 1; step <= SimStep.ticks(22); step++) {
            sortie.step(Act5ESpecs.steer(sortie, side, fire));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BRANCH_CHOSEN) {
                    chosenAt = step;
                    value = events.value(e);
                }
                lines += events.type(e) == SimEvents.Type.RADIO ? 1 : 0;
                woke += events.type(e) == SimEvents.Type.WOKE ? 1 : 0;
                caches += events.type(e) == SimEvents.Type.CACHE_OPENED ? 1 : 0;
                blasts += events.type(e) == SimEvents.Type.CHARGE_BLAST ? 1 : 0;
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec() == TestSpecs.SKITTER && enemy.serial() >= seen) {
                    skitters++;
                    seen = enemy.serial() + 1;
                    otherSide += (enemy.x() < 240) != (side == Branch.LEFT) ? 1 : 0;
                }
                if (enemy.spec() == Act5ESpecs.NEST && enemy.branch() >= 0 && sortie.forkSide(0) >= 0) {
                    boolean mine = enemy.branch() == Forks.code(0, side);
                    assertEquals(!mine, enemy.dormant(), "only the chosen side's turrets wake: " + enemy.x());
                }
                if (enemy.spec() == Act5ESpecs.NEST && !enemy.dormant()) {
                    awakeSerials[enemy.serial()] = true;
                }
            }
        }
        int awake = 0;
        for (boolean seenAwake : awakeSerials) {
            awake += seenAwake ? 1 : 0;
        }
        return new Flight(
                chosenAt,
                value,
                lines,
                skitters,
                otherSide,
                awake,
                woke,
                caches,
                blasts,
                sortie.stateHash(),
                sortie.enemyTotal());
    }

    @Test
    void theChannelTheShipIsInWhenTheTipReachesItsRowIsChosen() {
        Flight left = fly(Branch.LEFT, false);
        Flight right = fly(Branch.RIGHT, false);
        assertEquals(Forks.code(0, Branch.LEFT), left.chosenValue());
        assertEquals(Forks.code(0, Branch.RIGHT), right.chosenValue());
        // The tip (row 520) meets the ship's row (y 96) at scroll 964: 7.42 s at 130 px/s, after the launch.
        assertEquals(left.chosenAtTick(), right.chosenAtTick());
        assertTrue(
                left.chosenAtTick() > SimStep.ticks(7) && left.chosenAtTick() < SimStep.ticks(8),
                "" + left.chosenAtTick());
        assertEquals(1, left.lines(), "the branch line of the chosen side, once");
        assertEquals(1, right.lines());
    }

    @Test
    void onlyTheChosenSidesWavesFlyInItsChannel() {
        Flight left = fly(Branch.LEFT, false);
        Flight right = fly(Branch.RIGHT, false);
        assertEquals(3, left.skitters(), "the left wave's three");
        assertEquals(4, right.skitters(), "the right wave's four");
        assertEquals(0, left.skittersOnTheOtherSide(), "it flies its own channel");
        assertEquals(0, right.skittersOnTheOtherSide());
    }

    @Test
    void theOtherSidesGroundUnitsStayDormantAndCountNowhere() {
        Flight left = fly(Branch.LEFT, false);
        // The open turret, plus the chosen side's two (one woke at the choice, one entered awake); none shot.
        assertEquals(3, left.awakeNests());
        assertEquals(1, left.woke(), "the one that entered before the choice woke at it");
        // Planned: 7 Skitters + 5 turrets = 12; the right side's 4 Skitters and 2 turrets never come.
        assertEquals(12 - 6, left.enemyTotal());
        Flight right = fly(Branch.RIGHT, false);
        assertEquals(12 - 5, right.enemyTotal());
    }

    @Test
    void theOtherSidesObjectsCannotBeHit() {
        Flight left = fly(Branch.LEFT, true);
        assertEquals(1, left.caches(), "only the left cache's lock is shot open");
        assertEquals(1, left.blasts(), "only the left charge bursts");
    }

    @Test
    void eachRouteReplaysAndTheRoutesDiffer() {
        Flight one = fly(Branch.LEFT, true);
        Flight two = fly(Branch.LEFT, true);
        assertEquals(one.hash(), two.hash());
        assertNotEquals(one.hash(), fly(Branch.RIGHT, true).hash());
    }

    @Test
    void aBranchBelongsToOneOfTheTerrainsForksAndFliesFromTheFront() {
        LevelScript plain = TestSpecs.level(10, List.of());
        GroundUnit unit = new GroundUnit(1, 100, Act5ESpecs.NEST, -1).withBranch(Act5ESpecs.LEFT);
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.toBuilder().groundUnits(List.of(unit)).build());
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.wave(
                        1, WaveSpec.Formation.SNAKE, TestSpecs.SKITTER, 3, WaveSpec.Entry.SIDES, WaveSpec.Edge.LEFT)
                .withBranch(Act5ESpecs.LEFT));
        assertThrows(IllegalArgumentException.class, () -> unit.dormantUntil(4));
        assertThrows(IllegalArgumentException.class, () -> new Branch(Forks.MOST_FORKS, Branch.LEFT));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GroundSpec.TerrainSpec(Act5ESpecs.FORK, 10, 28, 0.12, 0.5).withForks(List.of(1)));
        assertEquals("2:right", Forks.subject(1, Branch.RIGHT));
    }

    @Test
    void aLevelWithoutForksHasNone() {
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, TestSpecs.level(10, List.of()), TestSpecs.RULES, 60);
        assertEquals(0, sortie.forkCount());
        assertFalse(Optional.ofNullable(sortie.forks).isPresent());
    }
}
