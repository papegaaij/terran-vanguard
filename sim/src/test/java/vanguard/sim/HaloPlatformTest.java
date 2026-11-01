package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part G, step G1b (design/enemies/ground/halo-platform; Level 41; m10g-gaps.md §1 #3–#5): the Halo Platform as a
 * held set piece on the ring engine ({@link Act6GHaloSpecs}): its ring's speed-ups by dead turrets, eased; the barrier
 * on the core until the third turret falls (the EMP never opens it); the alternating guns, the gun turrets' told
 * bursts, one rail line at a time and none within 100 px; the open core's ring after the eye's flare; the cook-off of
 * the turrets left; the pay; the IFF spoofer's delay on the held piece only; in a level, its anchor at its gate's halt,
 * no boss cues, its checkpoint and the gate opening at its death with the armour patch; allocation and replay.
 */
class HaloPlatformTest {
    private static final double EPS = 1e-9;

    private static Act6GHaloSpecs.Rig rig(boolean spoofable) {
        return new Act6GHaloSpecs.Rig(Act6GHaloSpecs.halo(Act5HSpecs.ARRIVE, spoofable)).up();
    }

    private static double degrees(double radians) {
        return Math.toDegrees(radians);
    }

    @Test
    void itsTurretsAlternateGunAndRailOnOneRingTurningClockwiseAllRound() {
        Act6GHaloSpecs.Rig rig = rig(false);
        RingArena ring = rig.rings();
        assertEquals(1, ring.ringCount());
        for (int p : Act6GHaloSpecs.TURRETS) {
            assertEquals(
                    Act6GHaloSpecs.GUNS.contains(p) ? Act6GHaloSpecs.GUN : Act6GHaloSpecs.RAIL, ring.batteryAttack(p));
        }
        assertEquals(-1, ring.batteryAttack(Act6GHaloSpecs.CORE));
        assertEquals(20, degrees(ring.velocity(0)), EPS, "20°/s clockwise");
        // The arc follows the ship all round: straight above the centre, no clamp.
        rig.shipX = Act5HSpecs.X;
        rig.shipY = Act5HSpecs.Y + 150;
        rig.step();
        assertEquals(180, Math.abs(degrees(ring.bearing())), 1e-6, "the bearing straight up, unclamped");
        assertTrue(rig.piece.held());
    }

    @Test
    void eachTurretLostSpeedsTheRingUpEasedOver08Seconds() {
        Act6GHaloSpecs.Rig rig = rig(false);
        RingArena ring = rig.rings();
        Act6GHaloSpecs.kill(rig.piece, 1);
        rig.step();
        assertEquals(1, ring.deadBatteries());
        assertEquals(20, degrees(ring.velocity(0)), EPS, "the ease starts from the speed it had");
        double last = ring.velocity(0);
        for (int i = 0; i < SimStep.ticks(0.4); i++) {
            rig.step();
            assertTrue(ring.velocity(0) > last, "it speeds up step by step, no jump");
            assertTrue(ring.velocity(0) - last < Math.toRadians(8) / SimStep.ticks(0.8) + EPS);
            last = ring.velocity(0);
        }
        assertEquals(24, degrees(ring.velocity(0)), 1e-6, "half way at 0.4 s");
        rig.steps(0.4);
        assertEquals(28, degrees(ring.velocity(0)), 1e-6, "20 + 8 after 0.8 s");
        rig.steps(0.5);
        assertEquals(28, degrees(ring.velocity(0)), 1e-6, "and holds");
        Act6GHaloSpecs.kill(rig.piece, 2);
        Act6GHaloSpecs.kill(rig.piece, 4);
        rig.steps(0.8 + SimStep.SECONDS);
        assertEquals(3, ring.deadBatteries());
        assertEquals(44, degrees(ring.velocity(0)), 1e-6, "20 + 3 × 8");
        Act6GHaloSpecs.kill(rig.piece, 3);
        Act6GHaloSpecs.kill(rig.piece, 5);
        rig.steps(0.8 + SimStep.SECONDS);
        assertEquals(60, degrees(ring.velocity(0)), 1e-6, "60°/s with five lost");
    }

    @Test
    void theBarrierHoldsUntilTheThirdTurretFallsTheEmpNeverOpensItAndItFades() {
        Act6GHaloSpecs.Rig rig = rig(false);
        SetPiece halo = rig.piece;
        int core = Act6GHaloSpecs.CORE;
        assertTrue(halo.partShielded(core), "the barrier: the core takes nothing");
        assertTrue(halo.shields().isEmpty(), "no hex shield anywhere: nothing for a shield-breaker to empty");
        halo.stripShields(SimStep.ticks(3));
        rig.step();
        assertTrue(halo.partShielded(core), "the EMP leaves it");
        Act6GHaloSpecs.kill(halo, 1);
        Act6GHaloSpecs.kill(halo, 2);
        rig.steps(1);
        assertEquals(0, halo.phase());
        assertTrue(halo.partShielded(core), "two lost: still up");
        assertFalse(halo.partShielded(3), "the turrets take hits");
        Act6GHaloSpecs.kill(halo, 3);
        rig.step();
        assertEquals(List.of(1), rig.phases, "the third loss: the core's phase");
        int opening = 0;
        if (rig.values(SimEvents.Type.CORE_OPENING).isEmpty()) {
            rig.until(SimEvents.Type.CORE_OPENING, 0.1);
        }
        RingCore barrier = rig.rings().core().orElseThrow();
        while (rig.values(SimEvents.Type.CORE_OPEN).isEmpty()) {
            assertTrue(halo.partShielded(core), "untouchable while it fades");
            rig.step();
            opening++;
        }
        assertEquals(SimStep.ticks(0.5), opening, "the barrier fades over 0.5 s");
        assertEquals(1, barrier.openShare(), EPS);
        assertFalse(halo.partShielded(core), "the core takes hits");
    }

    @Test
    void theGunTurretsTellTheirBurstsAndFireOnlyInsideTheArc() {
        Act6GHaloSpecs.Rig rig = rig(false);
        RingArena ring = rig.rings();
        List<Integer> told = rig.until(SimEvents.Type.TURRET_TELL, 3);
        for (int p : told) {
            assertTrue(Act6GHaloSpecs.GUNS.contains(p), "a gun turret's muzzles: " + p);
            assertTrue(ring.armed(p), "inside the arc: " + p);
        }
        for (int i = 1; i < SimStep.ticks(0.3); i++) {
            rig.step();
            assertTrue(rig.shots.isEmpty(), "nothing before the tell's end");
        }
        rig.step();
        assertEquals(told.size(), rig.shots.size(), "each told turret's burst starts after its 0.3 s tell");
        int shots = rig.shots.size();
        for (int i = 0; i < SimStep.ticks(0.3); i++) {
            rig.step();
            shots += rig.shots.size();
        }
        assertEquals(3 * told.size(), shots, "bursts of 3, 0.12 s apart");
        // Over a while every shot leaves an armed gun turret.
        for (int i = 0; i < SimStep.ticks(10); i++) {
            rig.step();
            for (double[] shot : rig.shots) {
                assertTrue(fromArmedGun(rig, shot), "a shot from an armed gun turret");
            }
        }
    }

    private static boolean fromArmedGun(Act6GHaloSpecs.Rig rig, double[] shot) {
        for (int p : Act6GHaloSpecs.GUNS) {
            if (Math.abs(rig.piece.partX(p) - shot[0]) < 1e-6
                    && Math.abs(rig.piece.partY(p) - shot[1]) < 1e-6
                    && rig.rings().armed(p)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void oneRailLineAtATimeAtLeast15SecondsApartFromArmedRailTurrets() {
        Act6GHaloSpecs.Rig rig = rig(false);
        List<Integer> starts = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(30); i++) {
            rig.step();
            for (int p : rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH)) {
                assertTrue(Act6GHaloSpecs.RAILS.contains(p), "a rail turret's line: " + p);
                assertTrue(rig.rings().armed(p), "from inside the arc: " + p);
                starts.add(i);
            }
        }
        assertTrue(starts.size() >= 4, "lines come: " + starts.size());
        for (int k = 1; k < starts.size(); k++) {
            assertTrue(starts.get(k) - starts.get(k - 1) >= SimStep.ticks(BossLines.GAP_SECONDS), "≥ 1.5 s apart");
            assertTrue(
                    starts.get(k) - starts.get(k - 1)
                            >= SimStep.ticks(Act5Specs.RAIL.telegraphSeconds() + Act5Specs.RAIL.liveSeconds()),
                    "one line at a time");
        }
    }

    @Test
    void theRailTurretsHoldTheirLinesWhileTheShipIsWithin100Px() {
        Act6GHaloSpecs.Rig rig = rig(false);
        // At the centre every rail turret is 96 px away.
        rig.shipX = Act5HSpecs.X;
        rig.shipY = Act5HSpecs.Y;
        for (int i = 0; i < SimStep.ticks(15); i++) {
            rig.step();
            assertTrue(rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).isEmpty(), "no line on a ship this close");
        }
    }

    @Test
    void theOpenCoresRingFollowsItsEyesFlareEvery3Seconds() {
        Act6GHaloSpecs.Rig rig = rig(false);
        for (int p : Act6GHaloSpecs.TURRETS) {
            Act6GHaloSpecs.kill(rig.piece, p);
        }
        rig.until(SimEvents.Type.CORE_OPEN, 1);
        int core = Act6GHaloSpecs.CORE;
        List<Integer> flares = new ArrayList<>();
        List<Integer> rings = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(10); i++) {
            rig.step();
            if (rig.values(SimEvents.Type.TURRET_TELL).contains(core)) {
                flares.add(i);
            }
            for (double[] shot : rig.shots) {
                assertEquals(Act5HSpecs.X, shot[0], 1e-6, "from the core");
                assertEquals(Act5HSpecs.Y, shot[1], 1e-6);
                rings.add(i);
            }
        }
        assertEquals(3, rings.size(), "a ring every 3.0 s");
        assertEquals(rings.size(), flares.size());
        for (int k = 0; k < rings.size(); k++) {
            assertEquals(SimStep.ticks(0.5), rings.get(k) - flares.get(k), "the eye flares 0.5 s before");
            if (k > 0) {
                assertEquals(SimStep.ticks(3.0), rings.get(k) - rings.get(k - 1));
            }
        }
    }

    @Test
    void itsDeathCooksOffTheTurretsLeft015SecondsApart() {
        Act6GHaloSpecs.Rig rig = rig(false);
        SetPiece halo = rig.piece;
        for (int p : List.of(1, 2, 3)) {
            Act6GHaloSpecs.kill(halo, p);
        }
        rig.until(SimEvents.Type.CORE_OPEN, 1);
        rig.step();
        for (int p = 0; p < halo.partCount(); p++) {
            halo.wreckPart(p);
        }
        halo.destroy();
        List<Integer> values = new ArrayList<>();
        List<Integer> at = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(1); i++) {
            rig.step();
            for (int value : rig.values(SimEvents.Type.RING_COOKOFF)) {
                values.add(value);
                at.add(i);
            }
        }
        RingArena ring = rig.rings();
        assertEquals(List.of(ring.cookoffValue(4), ring.cookoffValue(5), ring.cookoffValue(6)), values);
        assertEquals(List.of(0, SimStep.ticks(0.15), SimStep.ticks(0.3)), at, "in turn round the ring");
    }

    @Test
    void theSpooferDelaysTheHaloVolleysHalfASecondAtTheirStaleAim() {
        Act6GHaloSpecs.Rig plain = rig(true);
        Act6GHaloSpecs.Rig fooled = rig(true);
        fooled.piece.spoof(SimStep.ticks(0.5));
        int firstPlain = firstShot(plain);
        Act6GHaloSpecs.Rig again = rig(true);
        again.piece.spoof(SimStep.ticks(0.5));
        int held = -1;
        int fired = -1;
        double[] staleAt = null;
        for (int i = 0; i < SimStep.ticks(5) && fired < 0; i++) {
            if (held >= 0) {
                again.shipX = 200;
            }
            again.step();
            if (held < 0 && !again.values(SimEvents.Type.BOSS_SPOOFED).isEmpty()) {
                held = i;
                staleAt = new double[] {again.shipX, again.shipY};
                assertTrue(again.shots.isEmpty(), "held, not fired");
                assertTrue(again.piece.spoofHolding());
            }
            if (!again.aims.isEmpty()) {
                fired = i;
                assertEquals(staleAt[0], again.aims.getFirst()[0], EPS, "at the ship's stale place");
                assertEquals(staleAt[1], again.aims.getFirst()[1], EPS);
            }
        }
        assertEquals(firstPlain, held, "the volley came due on time");
        assertEquals(SimStep.ticks(0.5), fired - held, "and fires 0.5 s late");
        // The rail lines' telegraphs start 0.5 s late too.
        assertEquals(firstLine(rig(true)) + SimStep.ticks(0.5), firstLine(fooled), "the line's telegraph 0.5 s late");
    }

    @Test
    void theSpooferFoolsNoOtherBossAndOnlyAHeldPieceIsSpoofable() {
        Act5HSpecs.Rig sovereign = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        sovereign.piece.spoof(SimStep.ticks(0.5));
        for (int i = 0; i < SimStep.ticks(15); i++) {
            sovereign.step();
            assertTrue(sovereign.values(SimEvents.Type.BOSS_SPOOFED).isEmpty(), "a boss fight is never fooled");
        }
        Act6GHaloSpecs.Rig unflagged = rig(false);
        unflagged.piece.spoof(SimStep.ticks(0.5));
        for (int i = 0; i < SimStep.ticks(5); i++) {
            unflagged.step();
            assertTrue(unflagged.values(SimEvents.Type.BOSS_SPOOFED).isEmpty(), "nor a piece without the flag");
        }
        BossSpec halo = Act6GHaloSpecs.halo(3, false).boss().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpecBuilder(halo).held(false).spoofable(true).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpecBuilder(halo).incomingSeconds(2).build());
    }

    private static int firstShot(Act6GHaloSpecs.Rig rig) {
        for (int i = 0; i < SimStep.ticks(5); i++) {
            rig.step();
            if (!rig.shots.isEmpty()) {
                return i;
            }
        }
        throw new AssertionError("a shot within 5 s");
    }

    private static int firstLine(Act6GHaloSpecs.Rig rig) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).isEmpty()) {
                return i;
            }
        }
        throw new AssertionError("a line within 10 s");
    }

    @Test
    void inItsLevelItLiesAtItsGatesHaltHoldsItAndItsFallOpensTheGateWithThePatch() {
        Sortie sortie = Act6GHaloSpecs.sortie(false);
        SetPiece halo = sortie.setPiece(0);
        int bossCues = 0;
        int haltLines = 0;
        boolean seen = false;
        for (int i = 0; i < SimStep.ticks(30) && !(sortie.halting() && sortie.gate() == 1); i++) {
            sortie.step(0);
            if (sortie.halting() && sortie.gate() == 0) {
                HeldArenaTest.killHolders(sortie);
            }
            seen |= halo.approaching();
            bossCues += bossCues(sortie);
            haltLines += Act6GSpecs.radioed(sortie, 0);
        }
        assertTrue(sortie.halting() && sortie.gate() == 1, "the Halo's gate halts");
        assertTrue(seen, "it scrolls in with the ground");
        assertEquals(Act6GHaloSpecs.HOVER_Y, halo.y(), 1e-6, "its core at its place when the scroll halts");
        assertEquals(240, halo.x(), EPS);
        sortie.step(0);
        haltLines += Act6GSpecs.radioed(sortie, 0);
        assertEquals(1, haltLines, "its gate's halt line");
        assertTrue(sortie.bossCheckpoint(), "its checkpoint at the halt");
        double clock = sortie.levelSeconds();
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(0);
            bossCues += bossCues(sortie);
        }
        assertEquals(Act6GHaloSpecs.HOVER_Y, halo.y(), 1e-6, "it stays put");
        assertTrue(halo.engaged() && !halo.ring().orElseThrow().spinningUp(), "engaged at the halt");
        assertEquals(clock, sortie.levelSeconds(), "the clock stands while it lives");
        sortie.destroyPart(0, 1);
        assertEquals(1, sortie.events().count(SimEvents.Type.PART_DESTROYED));
        sortie.destroyPart(0, 2);
        sortie.step(0);
        bossCues += bossCues(sortie);
        sortie.destroyPart(0, Act6GHaloSpecs.CORE);
        assertEquals(1 + 4, sortie.events().count(SimEvents.Type.PART_DESTROYED), "the core pays every turret left");
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_DESTROYED), "the event stays (the looks filter it)");
        assertEquals(1, sortie.events().count(SimEvents.Type.GATE_OPENED), "its fall opens its gate");
        assertTrue(patchDropped(sortie), "the armour patch at its death");
        int halo1 = Act6GSpecs.radioed(sortie, 1);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(0);
            halo1 += Act6GSpecs.radioed(sortie, 1);
            bossCues += bossCues(sortie);
        }
        assertEquals(1, halo1, "the halo-destroyed line");
        assertEquals(0, bossCues, "no boss arrival, phase or death line for a held piece");
        assertTrue(sortie.levelSeconds() > clock + 1, "the clock resumes");
        assertEquals(LevelResult.BossTime.NONE, sortie.result().bossTime(), "no Boss rush for a held piece");
    }

    private static int bossCues(Sortie sortie) {
        return Act6GSpecs.radioed(sortie, 2) + Act6GSpecs.radioed(sortie, 3) + Act6GSpecs.radioed(sortie, 4);
    }

    private static boolean patchDropped(Sortie sortie) {
        for (int i = 0; i < sortie.pickups.size(); i++) {
            if (sortie.pickups.get(i).type() == PickupType.ARMOUR_PATCH) {
                return true;
            }
        }
        return false;
    }

    @Test
    void theCheckpointRestoresItInItsHaltAtItsPlace() {
        Sortie sortie = Act6GHaloSpecs.sortie(false);
        Act6GHaloSpecs.untilHaloHalt(sortie);
        sortie.step(0);
        assertTrue(sortie.bossCheckpoint());
        double clock = sortie.levelSeconds();
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(0);
        }
        sortie.destroyPart(0, 1);
        sortie.retryFromBoss();
        sortie.step(0);
        SetPiece halo = sortie.setPiece(0);
        assertEquals(clock, sortie.levelSeconds(), "in its halt");
        assertEquals(1, sortie.gate());
        assertEquals(Act6GHaloSpecs.TURRET_HP, halo.partHp(1), EPS, "whole again");
        assertEquals(Act6GHaloSpecs.HOVER_Y, halo.y(), 1e-6, "at its place");
    }

    @Test
    void theSpooferHoldsItsVolleysInTheLevel() {
        Sortie sortie = Act6GHaloSpecs.sortie(true);
        Act6GHaloSpecs.untilHaloHalt(sortie);
        int spoofed = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(0);
            spoofed += sortie.events().count(SimEvents.Type.BOSS_SPOOFED);
        }
        assertTrue(spoofed > 0, "its volleys held");
        Sortie plain = Act6GHaloSpecs.sortie(false);
        Act6GHaloSpecs.untilHaloHalt(plain);
        for (int i = 0; i < SimStep.ticks(6); i++) {
            plain.step(0);
            assertEquals(0, plain.events().count(SimEvents.Type.BOSS_SPOOFED), "not without the spoofer");
        }
    }

    @Test
    void theHeldPiecesAndTheirRingsAreChecked() {
        // A held piece holds a gate of a held-gates level.
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(41, 6)
                .sections(List.of(new Section(10, 100), new Section(12, 0, true), new Section(30, 100)))
                .setPieces(List.of(Act6GHaloSpecs.halo(11, false)))
                .objectives(ObjectiveSpec.NONE)
                .build());
        // A battery's own attack is fired by it.
        RingSpec.Ring ring = Act6GHaloSpecs.rings().rings().getFirst();
        RingSpec swapped = new RingSpec(
                List.of(ring.withAttacks(List.of(
                        Act6GHaloSpecs.RAIL,
                        Act6GHaloSpecs.RAIL,
                        Act6GHaloSpecs.GUN,
                        Act6GHaloSpecs.RAIL,
                        Act6GHaloSpecs.GUN,
                        Act6GHaloSpecs.RAIL))),
                Math.toRadians(120),
                RingSpec.ALL_ROUND,
                1,
                1,
                List.of(1.0),
                List.of(0, 0),
                0.15,
                Act6GHaloSpecs.rings().core());
        BossSpec boss = Act6GHaloSpecs.halo(3, false).boss().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> boss.withRings(Optional.of(swapped)));
        assertTrue(boss.withRings(Optional.of(Act6GHaloSpecs.rings())).ringed(), "the right attacks pass");
        // A tell is of an attack that has no tell of its own.
        assertThrows(
                IllegalArgumentException.class,
                () -> boss.withRings(Optional.of(
                        Act6GHaloSpecs.rings().withTells(List.of(new RingSpec.Tell(Act6GHaloSpecs.RAIL, 0.3))))));
        assertThrows(IllegalArgumentException.class, () -> new RingSpec.Losses(List.of(), 0.8));
        assertThrows(
                IllegalArgumentException.class,
                () -> ring.withAttacks(List.of(Act6GHaloSpecs.GUN, Act6GHaloSpecs.RAIL)),
                "an attack for each battery");
    }

    @Test
    void itStepsWithoutAllocatingAndReplays() {
        assertEquals(0, Allocations.least(() -> Act6GHaloSpecs.sortie(true), HaloPlatformTest::flyHalo));
        Sortie one = Act6GHaloSpecs.sortie(true);
        Sortie two = Act6GHaloSpecs.sortie(true);
        flyHalo(one);
        flyHalo(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertTrue(one.setPiece(0).destroyed());
    }

    /** Through gate 0 into the Halo's halt; a turret down every 2 s from its spin-up, then the core. */
    static void flyHalo(Sortie sortie) {
        int halted = 0;
        for (int i = 0; i < SimStep.ticks(48); i++) {
            sortie.step(Act6BAllocationsTest.command(i));
            if (sortie.halting() && sortie.gate() == 0) {
                HeldArenaTest.killHolders(sortie);
            }
            halted = sortie.halting() && sortie.gate() == 1 ? halted + 1 : 0;
            SetPiece halo = sortie.setPiece(0);
            if (halted > 0 && halted % SimStep.ticks(2) == 0 && !halo.destroyed()) {
                int p = halted / SimStep.ticks(2);
                if (p <= 6) {
                    sortie.destroyPart(0, p);
                } else {
                    sortie.destroyPart(0, Act6GHaloSpecs.CORE);
                }
            }
        }
    }
}
