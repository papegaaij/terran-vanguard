package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.A_T;
import static vanguard.sim.SiegeSpecs.A_X;
import static vanguard.sim.SiegeSpecs.SHELLBACK;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D, the Shellback's crush (design/enemies/ground/shellback, Curl and roll; design/allies, Ares Landing
 * dome): a rolling ball touching a standing dome's footprint (round 40: not only its hit box) deals the dome's {@code
 * rolls} hits once per roll,
 * whatever the hook says; destructible ground objects in its path are destroyed (a crate's drop still falls,
 * no bounty); never a hardened object or a trigger, never an enemy; a walking Shellback crushes nothing.
 */
class RollCrushTest {
    private static final GroundObjectSpec CRATE = new GroundObjectSpec(
            A_T, A_X, new Hitbox(32, 24), 10, 5, Optional.of(PickupType.SHIELD_CELL), 0, 0, "", false);
    private static final GroundObjectSpec BUNKER =
            new GroundObjectSpec(A_T, A_X, new Hitbox(32, 24), 10, 5, Optional.empty(), 0, 0, "", true);
    private static final GroundObjectSpec TRIGGER =
            new GroundObjectSpec(A_T, A_X, new Hitbox(12, 12), 0, 0, Optional.empty(), 99, 80, "cache", false);

    /**
     * The Shellback entering at dome A's time 300 px below the top edge at its x, walking up into it; it curls
     * at once ({@code curl}), so its roll meets dome A (and what enters with it) about 2 s into the roll.
     */
    private static WaveSpec intoDomeA() {
        return SiegeSpecs.walker(A_T, SHELLBACK, new WaveSpec.At(A_X, 300), new WaveSpec.At(A_X, -3000));
    }

    /** Dome A's level (with these allies, ground objects and ground units) and the Shellback walking into it. */
    private static LevelScript level(
            Optional<ObjectiveSpec.GroundAllies> allies, List<GroundObjectSpec> ground, List<GroundUnit> units) {
        return level(List.of(intoDomeA()), allies, ground, units);
    }

    private static LevelScript level(
            List<WaveSpec> waves,
            Optional<ObjectiveSpec.GroundAllies> allies,
            List<GroundObjectSpec> ground,
            List<GroundUnit> units) {
        return LevelScript.builder(17, 3)
                .sections(List.of(new Section(SiegeSpecs.LENGTH, SiegeSpecs.SPEED)))
                .waves(waves)
                .groundObjects(ground)
                .groundUnits(units)
                .secrets((int) ground.stream().filter(GroundObjectSpec::trigger).count())
                .objectives(ObjectiveSpec.NONE.withGroundAllies(allies))
                .build();
    }

    /** Runs to the Shellback's entry and, with {@code curl}, sets off its curl at once; returns it. */
    private static Enemy enter(Sortie sortie, boolean curl) {
        SiegeSpecs.runTo(sortie, A_T + 0.05);
        Enemy shellback = SiegeSpecs.find(sortie, SHELLBACK);
        assertNotNull(shellback);
        if (curl) {
            shellback.damage(72);
            assertSame(Enemy.CurlPhase.CURLING, shellback.curlPhase());
        }
        return shellback;
    }

    /** Steps through the curl-roll's curl and roll; returns the {@code ROLL_CRUSHED} values in order. */
    private static int[] rollThrough(Sortie sortie, Enemy shellback) {
        int[] crushed = new int[8];
        int n = 0;
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(ROLL_SECONDS); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ROLL_CRUSHED) {
                    crushed[n++] = events.value(e);
                    if (events.value(e) >= 0) {
                        assertEquals(1, events.count(SimEvents.Type.ALLY_HIT), "the dome's hit in the same step");
                        assertSame(Enemy.CurlPhase.ROLLING, shellback.curlPhase());
                    }
                }
            }
            hits += sortie.events().count(SimEvents.Type.ALLY_HIT);
        }
        assertEquals(java.util.Arrays.stream(crushed, 0, n).filter(v -> v >= 0).count(), hits);
        return java.util.Arrays.copyOf(crushed, n);
    }

    /** The curl and the roll, and a little more (the ball meets dome A about 2 s into the roll). */
    private static final double ROLL_SECONDS = SiegeSpecs.ROLL.curlSeconds() + SiegeSpecs.ROLL.rollSeconds() + 0.5;

    @Test
    void aRollingBallCrushesADomeOncePerRoll() {
        Sortie sortie = SiegeSpecs.sortie(level(Optional.of(SiegeSpecs.hookedDomes()), List.of(), List.of()));
        Enemy shellback = enter(sortie, true);
        int[] crushed = rollThrough(sortie, shellback);
        assertEquals(1, crushed.length, "one hit per roll, though the ball stays over the dome for several steps");
        assertEquals(0, crushed[0], "dome A");
        assertEquals(1, sortie.ally(0).hitsTaken(), "its rolls: one hit");
        assertTrue(sortie.ally(0).alive());
    }

    /**
     * Round 40 R.c: walking up 90 px beside dome A (its body clear of the glass, the hit box), the ball touches the
     * dome's footprint (its ring): one hit; 100 px beside it, the ball misses the footprint.
     */
    @Test
    void aRollBesideADomeGrazesItsFootprint() {
        for (double dx : new double[] {90, 100}) {
            WaveSpec beside =
                    SiegeSpecs.walker(A_T, SHELLBACK, new WaveSpec.At(A_X + dx, 300), new WaveSpec.At(A_X + dx, -3000));
            Sortie sortie = SiegeSpecs.sortie(
                    level(List.of(beside), Optional.of(SiegeSpecs.hookedDomes()), List.of(), List.of()));
            Enemy shellback = enter(sortie, true);
            int[] crushed = rollThrough(sortie, shellback);
            assertEquals(dx == 90 ? 1 : 0, crushed.length, dx + " px beside it");
            assertEquals(dx == 90 ? 1 : 0, sortie.ally(0).hitsTaken());
        }
    }

    @Test
    void theCrushIsIndependentOfTheHook() {
        Sortie sortie = SiegeSpecs.sortie(level(Optional.of(SiegeSpecs.domes(Map.of())), List.of(), List.of()));
        Enemy shellback = enter(sortie, true);
        assertEquals(1, rollThrough(sortie, shellback).length, "easy's unhooked Shellbacks crush too");
        assertEquals(1, sortie.ally(0).hitsTaken());
    }

    @Test
    void aWalkingShellbackCrushesNothing() {
        Sortie sortie = SiegeSpecs.sortie(level(Optional.of(SiegeSpecs.domes(Map.of())), List.of(CRATE), List.of()));
        Enemy shellback = enter(sortie, false);
        assertEquals(0, rollThrough(sortie, shellback).length);
        assertEquals(0, sortie.ally(0).hitsTaken(), "it walked over the dome");
        assertEquals(1, sortie.groundObjectCount(), "and past the crate");
    }

    @Test
    void theRollDestroysACrateWhoseDropStillFallsWithoutItsBounty() {
        Sortie sortie = SiegeSpecs.sortie(level(Optional.empty(), List.of(CRATE), List.of()));
        Enemy shellback = enter(sortie, true);
        int pickups = sortie.pickupCount();
        int[] crushed = rollThrough(sortie, shellback);
        assertEquals(1, crushed.length);
        assertEquals(-1, crushed[0], "a ground object");
        assertEquals(0, sortie.groundObjectCount());
        assertEquals(pickups + 1, sortie.pickupCount(), "its shield cell falls");
        assertEquals(0, sortie.credits(), "the player did not destroy it");
    }

    @Test
    void neitherAHardenedObjectNorATriggerNorAnEnemyIsCrushed() {
        EnemySpec nest = PartASpecs.dummy(Layer.GROUND, 50, new Hitbox(40, 40), false);
        Sortie sortie = SiegeSpecs.sortie(
                level(Optional.empty(), List.of(BUNKER, TRIGGER), List.of(new GroundUnit(A_T, A_X, nest, -1))));
        Enemy shellback = enter(sortie, true);
        assertEquals(0, rollThrough(sortie, shellback).length);
        assertEquals(2, sortie.groundObjectCount());
        assertFalse(sortie.groundObject(0).damaged());
        assertFalse(sortie.groundObject(1).spent());
        Enemy unit = SiegeSpecs.find(sortie, nest);
        assertNotNull(unit, "never an enemy");
        assertEquals(50, unit.hp());
    }

    @Test
    void theSiegeUnitsRunDeterministicallyWithoutAllocating() {
        LevelScript level = level(
                List.of(
                        TestSpecs.wave(
                                0,
                                WaveSpec.Formation.SINGLE,
                                SiegeSpecs.HERALD,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE),
                        TestSpecs.wave(
                                0.1,
                                WaveSpec.Formation.CARRIER_ESCORTS,
                                TestSpecs.NEEDLER,
                                2,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE),
                        intoDomeA()),
                Optional.of(SiegeSpecs.hookedDomes()),
                List.of(CRATE),
                List.of());
        Sortie one = SiegeSpecs.sortie(level);
        Sortie two = SiegeSpecs.sortie(level);
        for (int i = 0; i < SimStep.ticks(12); i++) {
            one.step(SortieTest.Pilot.commands(i));
            two.step(SortieTest.Pilot.commands(i));
            if (i == SimStep.ticks(A_T + 0.1)) {
                SiegeSpecs.find(one, SHELLBACK).damage(72);
                SiegeSpecs.find(two, SHELLBACK).damage(72);
            }
        }
        assertEquals(one.stateHash(), two.stateHash());
        long allocated = Allocations.least(() -> SiegeSpecs.sortie(level), sortie -> {
            for (int i = 0; i < SimStep.ticks(12); i++) {
                sortie.step(SortieTest.Pilot.commands(i));
                if (i == SimStep.ticks(A_T + 0.1)) {
                    Enemy shellback = SiegeSpecs.find(sortie, SHELLBACK);
                    if (shellback != null) {
                        shellback.damage(72);
                    }
                }
            }
        });
        assertEquals(0, allocated);
    }
}
