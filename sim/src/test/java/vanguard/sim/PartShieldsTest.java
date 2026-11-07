package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H1b ({@link PartShields}, {@link RingCore}; design/enemies, Enemy shields; the Iron Sovereign's open
 * core, Q1 a): the core opens over 3 s, untouchable; open, its hex shield of 80 takes every hit first at full damage
 * and refills after 2 s at 50 % a second; a breaker's hit empties it (the whole hit to the core) and holds the refill
 * 3 s; the EMP Burst strips it (never while the core is shut) and stuns nothing; a beam's step-by-step damage wears it
 * with spaced events.
 */
class PartShieldsTest {
    private static final double EPS = 1e-9;
    private static final int CORE = Act5HSpecs.CORE;

    /** The test Sovereign up, its rings killed, its core open; its shield events into {@code events}. */
    private static Act5HSpecs.Rig openRig(SimEvents events) {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        rig.piece.useEvents(events);
        rig.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 2);
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(rig.piece, ring);
        }
        rig.until(SimEvents.Type.CORE_OPEN, 4);
        return rig;
    }

    @Test
    void theCoreOpensOverThreeSecondsUntouchableAndSilentThenItsShieldIsUp() {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        rig.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 2);
        SetPiece boss = rig.piece;
        RingCore core = boss.ring().orElseThrow().core().orElseThrow();
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(boss, ring);
        }
        rig.until(SimEvents.Type.CORE_OPENING, 1);
        int start = rig.tick;
        assertTrue(core.opening());
        while (rig.values(SimEvents.Type.CORE_OPEN).isEmpty()) {
            assertTrue(boss.partShielded(CORE), "untouchable while it opens (the Smart Bomb too)");
            assertTrue(rig.shots.isEmpty(), "silent while it opens");
            rig.step();
        }
        assertEquals(SimStep.ticks(3), rig.tick - start, "3 s");
        assertTrue(core.open());
        assertFalse(boss.partShielded(CORE));
        PartShields shields = boss.shields().orElseThrow();
        assertTrue(shields.fitted(CORE));
        assertFalse(shields.fitted(Act5HSpecs.SAMS.getFirst()), "the batteries carry none");
        assertEquals(80, shields.shield(CORE), EPS, "its shield full");
    }

    @Test
    void itsShieldTakesTheHitsFirstAtFullDamageAndRefillsAfterTwoSeconds() {
        SimEvents events = new SimEvents(256);
        Act5HSpecs.Rig rig = openRig(events);
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        double hp = boss.partHp(CORE);
        boss.damagePartAt(CORE, 30, boss.partX(CORE), boss.partY(CORE));
        assertEquals(50, shields.shield(CORE), EPS);
        assertEquals(hp, boss.partHp(CORE), EPS, "the core untouched behind it");
        events.clear();
        boss.damagePartAt(CORE, 60, boss.partX(CORE), boss.partY(CORE));
        assertEquals(0, shields.shield(CORE), EPS);
        assertEquals(hp - 10, boss.partHp(CORE), EPS, "the overflow goes on");
        assertEquals(1, events.count(SimEvents.Type.PART_SHIELD_BROKEN));
        // 2 s without a hit, then 50 % a second: full 2 s later.
        events.clear();
        int refilled = -1;
        for (int i = 1; i <= SimStep.ticks(4.5) && refilled < 0; i++) {
            rig.step();
            if (i <= SimStep.ticks(2)) {
                assertEquals(0, shields.shield(CORE), EPS, "the delay");
            }
            if (events.count(SimEvents.Type.PART_SHIELD_REFILLED) > 0) {
                refilled = i;
            }
        }
        assertEquals(SimStep.ticks(4), refilled, 2, "full after its delay and two seconds' refill");
        assertEquals(80, shields.shield(CORE), EPS);
    }

    @Test
    void aBreakersHitEmptiesItAndHoldsTheRefillThreeSeconds() {
        Act5HSpecs.Rig rig = openRig(new SimEvents(256));
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        double hp = boss.partHp(CORE);
        boss.damagePartBreaking(CORE, 20, boss.partX(CORE), boss.partY(CORE));
        assertEquals(0, shields.shield(CORE), EPS);
        assertEquals(hp - 20, boss.partHp(CORE), EPS, "the whole hit to the core");
        assertTrue(shields.held(CORE));
        rig.steps(3);
        assertEquals(0, shields.shield(CORE), EPS, "held 3 s");
        rig.steps(0.5);
        assertTrue(shields.shield(CORE) > 0, "then it refills");
    }

    @Test
    void theEmpStripsTheOpenCoresShieldButNotTheShutOneAndStunsNothing() {
        Act5HSpecs.Rig shut = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        shut.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 2);
        shut.piece.stripShields(SimStep.ticks(3));
        assertEquals(80, shut.piece.shields().orElseThrow().shield(CORE), EPS, "the shut core's shield is not on");

        Act5HSpecs.Rig rig = openRig(new SimEvents(256));
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        boss.stripShields(SimStep.ticks(3));
        assertEquals(0, shields.shield(CORE), EPS);
        assertTrue(shields.held(CORE));
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            rig.step();
            shots += rig.shots.size();
        }
        assertTrue(shots > 0, "the core fires on: a boss is never stunned");
        assertEquals(0, shields.shield(CORE), EPS, "held for the stun");
    }

    @Test
    void aBeamsStepByStepDamageWearsItWithSpacedEvents() {
        SimEvents events = new SimEvents(256);
        Act5HSpecs.Rig rig = openRig(events);
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        double hp = boss.partHp(CORE);
        events.clear();
        int hits = 0;
        for (int i = 0; i < 30; i++) {
            boss.damagePartAt(CORE, 2, boss.partX(CORE), boss.partY(CORE), 0, 1);
            rig.step();
            hits += events.count(SimEvents.Type.PART_SHIELD_HIT);
            events.clear();
        }
        assertEquals(20, shields.shield(CORE), EPS, "60 of its 80 worn");
        assertEquals(hp, boss.partHp(CORE), EPS);
        assertTrue(hits >= 4 && hits <= 6, "spaced, not one a step: " + hits);
    }

    // M10 part H, step H1a (design/enemies/bosses/ascendant: the cannon's gold hex shield fed by the generators).

    @Test
    void theAscendantsCannonShieldIsFedByItsGeneratorsTwoFortyOneTwentyNone() {
        SimEvents events = new SimEvents(64);
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        rig.piece.useEvents(events);
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        int cannon = Act6HSpecs.CANNON;
        assertTrue(shields.fitted(cannon));
        assertFalse(shields.fitted(Act6HSpecs.GEN_L), "the generators carry none");
        assertEquals(240, shields.shield(cannon), EPS, "both generators: 240");
        rig.kill(Act6HSpecs.GEN_L);
        rig.step();
        assertEquals(120, shields.max(cannon), EPS, "one: 120");
        assertEquals(120, shields.shield(cannon), EPS, "cut at once");
        double hp = boss.partHp(cannon);
        boss.damagePartAt(cannon, 100, boss.partX(cannon), boss.partY(cannon));
        assertEquals(20, shields.shield(cannon), EPS);
        assertEquals(hp, boss.partHp(cannon), EPS, "spent first");
        rig.steps(4.5);
        assertEquals(120, shields.shield(cannon), EPS, "it refills to its feed");
        events.clear();
        rig.kill(Act6HSpecs.GEN_R);
        rig.step();
        assertEquals(0, shields.max(cannon), EPS, "none with neither");
        assertEquals(1, events.count(SimEvents.Type.PART_SHIELD_BROKEN), "it fails as the last feeder dies");
        boss.damagePartAt(cannon, 50, boss.partX(cannon), boss.partY(cannon));
        assertEquals(hp - 50, boss.partHp(cannon), EPS, "the whole hit to the cannon");
        rig.steps(5);
        assertEquals(0, shields.shield(cannon), EPS, "never back");
    }

    @Test
    void theAscendantsFedShieldEmptiesToABreakerAndTheEmpAndRefillsToItsFeed() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        SetPiece boss = rig.piece;
        PartShields shields = boss.shields().orElseThrow();
        int cannon = Act6HSpecs.CANNON;
        double hp = boss.partHp(cannon);
        boss.damagePartBreaking(cannon, 20, boss.partX(cannon), boss.partY(cannon));
        assertEquals(0, shields.shield(cannon), EPS, "the breaker empties it");
        assertEquals(hp - 20, boss.partHp(cannon), EPS);
        rig.steps(3);
        assertEquals(0, shields.shield(cannon), EPS, "held 3 s");
        rig.steps(2.5);
        assertEquals(240, shields.shield(cannon), EPS, "then back to 240");
        boss.stripShields(SimStep.ticks(3));
        assertEquals(0, shields.shield(cannon), EPS, "the EMP strips it");
        assertTrue(shields.held(cannon));
    }

    @Test
    void theChimerasCapsuleCarriesItsOwnShieldRefilledTwoSecondsAfterTheLastHit() {
        // M11 part F, step F1: a plain part shield (PartShield): 120, refilled 2 s after the last hit; a breaker and
        // the
        // EMP empty it.
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.chimera(java.util.List.of()))
                .settled()
                .toPhase2();
        for (int p : Act7FSpecs.SACS) {
            rig.kill(p);
        }
        rig.steps(2.05);
        SetPiece boss = rig.piece;
        int capsule = Act7FSpecs.CAPSULE;
        PartShields shields = boss.shields().orElseThrow();
        assertTrue(shields.fitted(capsule));
        assertFalse(shields.fitted(Act7FSpecs.SAC_L), "the sacs carry none");
        assertEquals(120, shields.shield(capsule), EPS);
        double hp = boss.partHp(capsule);
        boss.damagePartAt(capsule, 100, boss.partX(capsule), boss.partY(capsule));
        assertEquals(20, shields.shield(capsule), EPS);
        assertEquals(hp, boss.partHp(capsule), EPS, "spent first");
        rig.steps(1.9);
        assertEquals(20, shields.shield(capsule), EPS, "not before 2 s");
        rig.steps(1.2);
        assertEquals(120, shields.shield(capsule), EPS, "refilled");
        boss.damagePartBreaking(capsule, 20, boss.partX(capsule), boss.partY(capsule));
        assertEquals(0, shields.shield(capsule), EPS, "the breaker empties it");
        assertEquals(hp - 20, boss.partHp(capsule), EPS);
        rig.steps(6);
        boss.stripShields(SimStep.ticks(3));
        assertEquals(0, shields.shield(capsule), EPS, "the EMP strips it");
        assertTrue(shields.held(capsule));
    }
}
