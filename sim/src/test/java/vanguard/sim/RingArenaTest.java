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
 * M9 part H, step H1a: the ringed arena boss ({@link RingArena}; design/enemies/bosses/iron-sovereign) on the test
 * Sovereign of {@link Act5HSpecs}: the scroll-in and halt, the spin-up ring by ring (invulnerable, silent), the rings'
 * speeds and directions, the batteries' places on them, the inner ring's tell, step and hold, the fire arc following
 * the ship (clamped) and gating the fire (the hitboxes live outside it), the laser sweeps only at a hold's start from
 * an armed battery (the wedge static), the death's cook-offs, the checkpoint, determinism, allocation and the specs'
 * checks.
 */
class RingArenaTest {
    private static final double EPS = 1e-6;
    private static final int FIRE = Command.FIRE.bit();

    private static Act5HSpecs.Rig upRig(SetPieceSpec spec) {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(spec);
        rig.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 1);
        return rig;
    }

    private static double degrees(double radians) {
        return Math.toDegrees(Math.IEEEremainder(radians, 2 * Math.PI));
    }

    @Test
    void itScrollsInWithTheGroundAndHaltsBeforeItsSpinUp() {
        Sortie sortie = Act5HSpecs.sortie(Act5HSpecs.QUIET, TestSpecs.RULES);
        SetPiece boss = Act5HSpecs.boss(sortie);
        boolean seen = false;
        while (sortie.levelSeconds() < Act5HSpecs.ARRIVE - SimStep.SECONDS) {
            sortie.step(0);
            seen |= boss.approaching();
            assertFalse(boss.present());
        }
        assertTrue(seen, "it scrolls in with the hull before it arrives");
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, 0.1, 0);
        assertTrue(sortie.bossCheckpoint(), "the checkpoint at its arrival");
        MawSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 1.1, 0);
        assertEquals(Act5HSpecs.Y, boss.y(), EPS, "its centre 160 px below the top edge once halted");
        assertEquals(Act5HSpecs.X, boss.x(), EPS);
        assertTrue(boss.engaged());
        assertTrue(Act5HSpecs.rings(sortie).spinningUp());
        assertTrue(boss.partShielded(Act5HSpecs.SAMS.getFirst()), "invulnerable while the rings spin up");
        for (int p = 0; p < boss.partCount(); p++) {
            assertEquals(Layer.GROUND, boss.partLayer(p));
        }
        Act5HSpecs.untilUp(sortie);
        assertFalse(boss.partShielded(Act5HSpecs.SAMS.getFirst()));
        assertTrue(boss.partShielded(Act5HSpecs.CORE), "the core armoured until it opens");
    }

    @Test
    void theRingsLightOneAfterTheOtherAndSpinUpFromRestInvulnerableAndSilent() {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        List<Integer> lit = new ArrayList<>();
        List<Integer> litAt = new ArrayList<>();
        int up = -1;
        double lastOuter = 0;
        for (int i = 0; i < SimStep.ticks(Act5HSpecs.SPIN_UP + 0.5); i++) {
            rig.step();
            for (int ring : rig.values(SimEvents.Type.RING_LIT)) {
                lit.add(ring);
                litAt.add(i);
            }
            if (!rig.values(SimEvents.Type.RINGS_UP).isEmpty()) {
                up = i;
            }
            if (up < 0) {
                assertTrue(rig.shots.isEmpty() && rig.sweeps.isEmpty(), "silent in the spin-up");
                for (int p = 0; p < rig.piece.partCount(); p++) {
                    assertTrue(rig.piece.partShielded(p), "invulnerable in the spin-up: " + p);
                    assertFalse(rig.piece.partArmed(p) && rig.rings().battery(p) >= 0);
                }
                double v = rig.rings().velocity(Act5HSpecs.OUTER);
                assertTrue(v >= lastOuter - EPS, "the outer ring speeds up from rest");
                lastOuter = v;
                assertEquals(0, rig.rings().velocity(Act5HSpecs.INNER), EPS, "the stepping ring waits");
            }
        }
        assertEquals(List.of(0, 1, 2), lit, "outer, middle, inner");
        int third = SimStep.ticks(Act5HSpecs.SPIN_UP / 3);
        assertEquals(List.of(0, third, SimStep.ticks(2 * Act5HSpecs.SPIN_UP / 3)), litAt);
        assertEquals(SimStep.ticks(Act5HSpecs.SPIN_UP), up, "up after the spin-up");
        assertEquals(Math.toRadians(6), rig.rings().velocity(Act5HSpecs.OUTER), EPS, "at speed");
        assertEquals(-Math.toRadians(10), rig.rings().velocity(Act5HSpecs.MIDDLE), EPS);
        assertFalse(rig.piece.partShielded(Act5HSpecs.SAMS.getFirst()));
    }

    @Test
    void theEvenRingsTurnAtTheirSpeedsAndCarryTheirBatteries() {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.QUIET);
        RingArena rings = rig.rings();
        double outer = rings.angle(Act5HSpecs.OUTER);
        double middle = rings.angle(Act5HSpecs.MIDDLE);
        rig.steps(5);
        assertEquals(30, degrees(rings.angle(Act5HSpecs.OUTER) - outer), 1e-6, "6°/s clockwise");
        assertEquals(-50, degrees(rings.angle(Act5HSpecs.MIDDLE) - middle), 1e-6, "10°/s the other way");
        SetPiece boss = rig.piece;
        for (int p = 1; p < boss.partCount(); p++) {
            int ring = rings.battery(p);
            double r = rings.spec().rings().get(ring).radius();
            double a = rings.mountRadians(p);
            assertEquals(
                    0,
                    degrees(rings.spec().rings().get(ring).mountRadians(rings.slot(p)) + rings.angle(ring) - a),
                    1e-9,
                    "" + p);
            assertEquals(Act5HSpecs.X - r * Math.sin(a), boss.partX(p), 1e-4, "on its ring (Trig): " + p);
            assertEquals(Act5HSpecs.Y - r * Math.cos(a), boss.partY(p), 1e-4, "on its ring (Trig): " + p);
        }
        assertEquals(Act5HSpecs.X, boss.partX(Act5HSpecs.CORE), EPS, "the core at the centre");
        assertEquals(Act5HSpecs.Y, boss.partY(Act5HSpecs.CORE), EPS);
        // A clockwise ring on the screen: the battery straight down moves to the left (y up, x right).
        rig = upRig(Act5HSpecs.QUIET);
        int first = Act5HSpecs.SAMS.getFirst();
        double x = rig.piece.partX(first);
        rig.steps(1);
        assertTrue(rig.piece.partX(first) < x, "clockwise: the bottom battery moves left");
    }

    @Test
    void theInnerRingTellsStepsAndHolds() {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.QUIET);
        RingArena rings = rig.rings();
        assertEquals(List.of(Act5HSpecs.INNER), rig.until(SimEvents.Type.RING_STEP_TELL, Act5HSpecs.SPIN_UP + 0.1));
        assertTrue(rig.values(SimEvents.Type.RINGS_UP).size() == 1, "the first tell as the rings come up");
        assertEquals(RingArena.StepState.TELL, rings.stepState(Act5HSpecs.INNER));
        int tell = 0;
        while (rig.values(SimEvents.Type.RING_STEP).isEmpty()) {
            rig.step();
            tell++;
        }
        assertEquals(SimStep.ticks(0.4), tell, "a 0.4 s tell");
        for (int cycle = 0; cycle < 3; cycle++) {
            double from = rings.angle(Act5HSpecs.INNER);
            int steps = 0;
            while (rig.values(SimEvents.Type.RING_HOLD).isEmpty()) {
                rig.step();
                steps++;
                assertTrue(rings.velocity(Act5HSpecs.INNER) > 0, "turning clockwise in its step");
            }
            assertEquals(SimStep.ticks(4), steps, "120° in 4 s");
            assertEquals(120, degrees(rings.angle(Act5HSpecs.INNER) - from), 1e-9, "exactly its step");
            double held = rings.angle(Act5HSpecs.INNER);
            int hold = 0;
            while (rig.values(SimEvents.Type.RING_STEP).isEmpty()) {
                rig.step();
                hold++;
                assertEquals(held, rings.angle(Act5HSpecs.INNER), 0, "still in the hold and the tell");
                if (!rig.values(SimEvents.Type.RING_STEP_TELL).isEmpty()) {
                    assertEquals(SimStep.ticks(3.6), hold, "the tell the hold's last 0.4 s");
                }
            }
            assertEquals(SimStep.ticks(4), hold, "a 4 s hold, the tell included");
        }
    }

    @Test
    void theFireArcFollowsTheShipClampedToTheLowerHalf() {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.QUIET);
        RingArena rings = rig.rings();
        rig.step();
        assertEquals(0, rings.bearing(), EPS, "the ship straight below");
        rig.shipX = Act5HSpecs.X - 200;
        rig.shipY = Act5HSpecs.Y - 330;
        rig.step();
        assertEquals(Math.toDegrees(Math.atan2(200, 330)), Math.toDegrees(rings.bearing()), 1e-9, "toward the ship");
        rig.shipX = 0;
        rig.shipY = Act5HSpecs.Y;
        rig.step();
        assertEquals(60, Math.toDegrees(rings.bearing()), 1e-9, "clamped: 30° above the horizontal on the left");
        rig.shipX = 470;
        rig.shipY = 530;
        rig.step();
        assertEquals(-60, Math.toDegrees(rings.bearing()), 1e-9, "clamped on the right, the ship above the centre");
        // Armed: alive, its ring turning, inside the arc; the others' hitboxes still live.
        for (int p = 1; p < rig.piece.partCount(); p++) {
            double off = Math.abs(Math.IEEEremainder(rings.mountRadians(p) - rings.bearing(), 2 * Math.PI));
            assertEquals(off <= Math.toRadians(60), rings.armed(p), "armed inside the arc: " + p);
            assertEquals(rings.armed(p), rig.piece.partArmed(p));
            assertFalse(rig.piece.partShielded(p), "the arc gates its weapons, not its hitbox: " + p);
        }
        assertTrue(rig.piece.partArmed(Act5HSpecs.CORE), "the core is no battery");
    }

    @Test
    void onlyArmedBatteriesFireAndTheLaserSweepsOnlyAtAHoldsStart() {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.SOVEREIGN);
        SetPiece boss = rig.piece;
        RingArena rings = rig.rings();
        int[] fired = new int[boss.partCount()];
        int sweeps = 0;
        int holds = 0;
        for (int i = 0; i < SimStep.ticks(60); i++) {
            // The ship wanders below the station, so the arc swings.
            rig.shipX = 240 + 200 * Math.sin(i * 0.004);
            rig.shipY = 60;
            rig.step();
            boolean hold = !rig.values(SimEvents.Type.RING_HOLD).isEmpty();
            holds += hold ? 1 : 0;
            for (double[] shot : rig.shots) {
                int from = -1;
                for (int p = 0; p < boss.partCount(); p++) {
                    if (Math.abs(boss.partX(p) - shot[0]) < 1e-9 && Math.abs(boss.partY(p) - shot[1]) < 1e-9) {
                        from = p;
                    }
                }
                assertTrue(from >= 0, "fired from a part");
                if (from != Act5HSpecs.CORE) {
                    assertTrue(rings.armed(from), "a battery fires only inside the arc: " + from);
                }
                fired[from]++;
            }
            for (int p : rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH)) {
                // Step H1b: the rail batteries' lines (BossLines) start only from an armed battery.
                assertTrue(rings.armed(p), "a rail line only inside the arc: " + p);
                fired[p]++;
            }
            for (double[] sweep : rig.sweeps) {
                if (sweep[1] == 1) {
                    assertEquals(Act5HSpecs.LASER, (int) sweep[0]);
                    assertTrue(hold, "a sweep's telegraph only at a hold's start");
                    sweeps++;
                }
            }
            if (rings.stepState(Act5HSpecs.INNER) == RingArena.StepState.STEP) {
                assertFalse(
                        boss.sweepTelegraphing(Act5HSpecs.LASER) || boss.sweepBeaming(Act5HSpecs.LASER),
                        "no sweep while the inner ring turns");
            }
            if (boss.sweepBeaming(Act5HSpecs.LASER)) {
                assertTrue(rings.stepState(Act5HSpecs.INNER) == RingArena.StepState.HOLD);
            }
        }
        assertTrue(holds >= 6, "holds: " + holds);
        assertEquals(holds, sweeps, "one sweep at each hold (one laser battery always in a 120° arc)");
        int sams = 0;
        for (int p : Act5HSpecs.SAMS) {
            sams += fired[p];
        }
        int rails = 0;
        for (int p : Act5HSpecs.RAILS) {
            rails += fired[p];
        }
        assertTrue(sams > 0 && sams < 60, "a SAM battery's turn every 6 s, skipped outside the arc: " + sams);
        assertTrue(rails > 0 && rails < 48, "a rail battery's turn every 5 s, skipped outside the arc: " + rails);
        assertTrue(fired[Act5HSpecs.CORE] > 0, "the shut core's fan");
    }

    @Test
    void aSweepsWedgeStaysStillThroughItsHold() {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.SOVEREIGN);
        rig.until(SimEvents.Type.RING_HOLD, 6);
        SetPiece boss = rig.piece;
        assertTrue(boss.sweepTelegraphing(Act5HSpecs.LASER));
        double x = boss.sweepOriginX(Act5HSpecs.LASER);
        double y = boss.sweepOriginY(Act5HSpecs.LASER);
        int armed = -1;
        for (int p : Act5HSpecs.LASERS) {
            armed = rig.rings().armed(p) ? p : armed;
        }
        assertEquals(boss.partX(armed), x, EPS, "from the armed laser battery");
        while (boss.sweepTelegraphing(Act5HSpecs.LASER) || boss.sweepBeaming(Act5HSpecs.LASER)) {
            assertEquals(x, boss.sweepOriginX(Act5HSpecs.LASER), 0);
            assertEquals(y, boss.sweepOriginY(Act5HSpecs.LASER), 0);
            assertTrue(rig.rings().stepState(Act5HSpecs.INNER) != RingArena.StepState.STEP);
            rig.step();
        }
        assertEquals(300, Act5HSpecs.SWEEP.length(), 0, "the Sentinel's sweep with a 300 px beam");
    }

    @Test
    void itsDeathCooksTheBatteriesOffRingByRing() {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.QUIET);
        SetPiece boss = rig.piece;
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(boss, ring);
            rig.step();
        }
        assertEquals(3, boss.phase(), "the core's phase");
        boss.damagePartFlat(Act5HSpecs.CORE, Act5HSpecs.CORE_HP + 1);
        boss.destroy();
        double[] angles = new double[3];
        for (int i = 0; i < 3; i++) {
            angles[i] = rig.rings().angle(i);
        }
        List<Integer> values = new ArrayList<>();
        List<Integer> at = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(3); i++) {
            rig.step();
            for (int k = 0; k < rig.types.size(); k++) {
                if (rig.types.get(k) == SimEvents.Type.RING_COOKOFF) {
                    values.add(rig.values.get(k));
                    at.add(i);
                    int p = expectedPart(values.size() - 1);
                    assertEquals(boss.partX(p), rig.places.get(k)[0], EPS, "at the battery");
                    assertEquals(boss.partY(p), rig.places.get(k)[1], EPS);
                }
            }
        }
        List<Integer> expected = new ArrayList<>();
        List<Integer> times = new ArrayList<>();
        for (int n = 0; n < 13; n++) {
            int p = expectedPart(n);
            expected.add(rig.rings().cookoffValue(p));
            times.add(SimStep.ticks(n * 0.15));
        }
        assertEquals(expected, values, "outer, middle, inner, each in its order");
        assertEquals(times, at, "0.15 s apart from the kill");
        assertEquals(RingArena.COOKOFF_RING + 2, rig.rings().cookoffValue(Act5HSpecs.RAILS.get(2)));
        for (int i = 0; i < 3; i++) {
            assertEquals(angles[i], rig.rings().angle(i), 0, "the rings stop with the kill");
        }
    }

    private static int expectedPart(int n) {
        List<Integer> all = new ArrayList<>(Act5HSpecs.SAMS);
        all.addAll(Act5HSpecs.RAILS);
        all.addAll(Act5HSpecs.LASERS);
        return all.get(n);
    }

    @Test
    void aRetryFromTheCheckpointStartsTheRingsFresh() {
        Sortie sortie = Act5HSpecs.sortie(Act5HSpecs.SOVEREIGN, TestSpecs.RULES.withInvulnerableShip());
        Act5HSpecs.untilUp(sortie);
        MawSpecs.steps(sortie, 10, FIRE);
        Act5HSpecs.killRing(Act5HSpecs.boss(sortie), Act5HSpecs.OUTER);
        MawSpecs.steps(sortie, 1, FIRE);
        assertEquals(1, Act5HSpecs.rings(sortie).lockedCount());
        sortie.retryFromBoss();
        sortie.step(0);
        RingArena rings = Act5HSpecs.rings(sortie);
        assertEquals(0, rings.lockedCount(), "no ring locked");
        assertTrue(rings.spinningUp(), "the spin-up again");
        for (int i = 0; i < 3; i++) {
            assertFalse(rings.locked(i));
        }
        assertEquals(0, Act5HSpecs.boss(sortie).phase());
        assertEquals(1, Act5HSpecs.boss(sortie).barShare(), EPS, "every battery whole");
        Act5HSpecs.untilUp(sortie);
        assertEquals(0, rings.angle(Act5HSpecs.INNER), EPS, "the inner ring from its start");
    }

    @Test
    void aRingedFightRunsTheSameTwiceAndAllocatesNothingPerStep() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = Act5HSpecs.sortie(Act5HSpecs.SOVEREIGN, TestSpecs.RULES.withInvulnerableShip());
            for (int i = 0; i < SimStep.ticks(40); i++) {
                sortie.step(FIRE | ((i / 90) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                if (i == SimStep.ticks(15)) {
                    Act5HSpecs.killRing(Act5HSpecs.boss(sortie), Act5HSpecs.MIDDLE);
                }
            }
            assertEquals(1, Act5HSpecs.boss(sortie).phase());
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = Act5HSpecs.sortie(Act5HSpecs.SOVEREIGN, TestSpecs.RULES.withInvulnerableShip());
                    Act5HSpecs.untilUp(sortie);
                    MawSpecs.steps(sortie, 1, 0);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(30); i++) {
                        sortie.step(FIRE | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                        if (i == SimStep.ticks(10)) {
                            Act5HSpecs.killRing(Act5HSpecs.boss(sortie), Act5HSpecs.OUTER);
                        }
                        if (i == SimStep.ticks(18)) {
                            Act5HSpecs.killRing(Act5HSpecs.boss(sortie), Act5HSpecs.INNER);
                            Act5HSpecs.killRing(Act5HSpecs.boss(sortie), Act5HSpecs.MIDDLE);
                        }
                        if (i == SimStep.ticks(25)) {
                            SetPiece boss = Act5HSpecs.boss(sortie);
                            boss.damagePartFlat(Act5HSpecs.CORE, boss.partHp(Act5HSpecs.CORE) - 1);
                        }
                    }
                });
        assertEquals(0, allocated, "the rings, the arc, the locks, the sweeps and the cook-offs allocate nothing");
    }

    @Test
    void aRingedBossNeedsAnArenaWithoutLanesAndItsRingsMustFit() {
        BossSpec boss = Act5HSpecs.QUIET.boss().orElseThrow();
        RingSpec rings = boss.rings().orElseThrow();
        assertTrue(boss.ringed());
        BossSpec plain = boss.withRings(Optional.empty());
        assertFalse(plain.ringed());
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpecBuilder(boss)
                        .breach(Optional.of(BreachArenaTest.BREACH))
                        .build(),
                "no breach and rings");
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpecBuilder(plain)
                        .arena(Optional.empty())
                        .rings(Optional.of(rings))
                        .build(),
                "an arena");
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withLocks(rings, List.of(1, 2, 3)))),
                "a lock count per phase");
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withLocks(rings, List.of(2, 1, 3, 0)))),
                "rising");
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withLocks(rings, List.of(1, 2, 3, 3)))),
                "the last phase on its parts");
        RingSpec.Ring inner = rings.rings().get(Act5HSpecs.INNER);
        RingSpec.Ring samOnSteps = new RingSpec.Ring(
                inner.name(), inner.radius(), inner.direction(), 0, 0, inner.parts(), Act5HSpecs.SAM, inner.step());
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withInner(rings, samOnSteps))),
                "a stepping ring sweeps");
        RingSpec.Ring shortHold = new RingSpec.Ring(
                inner.name(),
                inner.radius(),
                inner.direction(),
                0,
                0,
                inner.parts(),
                inner.attack(),
                Optional.of(new RingSpec.Step(Math.toRadians(120), 4, 2, 0.4)));
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withInner(rings, shortHold))),
                "the sweep inside the hold");
        RingSpec.Ring shared = new RingSpec.Ring(
                inner.name(),
                inner.radius(),
                inner.direction(),
                0,
                0,
                List.of(Act5HSpecs.SAMS.getFirst()),
                inner.attack(),
                inner.step());
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withRings(Optional.of(withInner(rings, shared))),
                "a battery on one ring only");
        assertThrows(
                IllegalArgumentException.class,
                () -> new RingSpec.Step(Math.toRadians(120), 4, 0.3, 0.4),
                "the tell inside the hold");
        assertThrows(
                IllegalArgumentException.class,
                () -> new RingSpec(List.of(), 1, rings.bearing(), 4, 2, List.of(1.0), List.of(0), 0.15),
                "a ring at least");
    }

    private static RingSpec withLocks(RingSpec rings, List<Integer> locks) {
        return new RingSpec(
                rings.rings(),
                rings.arcRadians(),
                rings.bearing(),
                rings.spinUpSeconds(),
                rings.grindSeconds(),
                rings.speedUps(),
                locks,
                rings.cookoffSeconds());
    }

    private static RingSpec withInner(RingSpec rings, RingSpec.Ring inner) {
        return new RingSpec(
                List.of(rings.rings().get(0), rings.rings().get(1), inner),
                rings.arcRadians(),
                rings.bearing(),
                rings.spinUpSeconds(),
                rings.grindSeconds(),
                rings.speedUps(),
                rings.phaseLocks(),
                rings.cookoffSeconds());
    }

    // ---- Step H1b: the SAM batteries' hatches and cap, the core's bays ----

    /** The test Sovereign up, its rings killed, its core open, the ship at (shipX, shipY). */
    private static Act5HSpecs.Rig openRig(double shipX, double shipY) {
        Act5HSpecs.Rig rig = upRig(Act5HSpecs.SOVEREIGN);
        rig.shipX = shipX;
        rig.shipY = shipY;
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(rig.piece, ring);
        }
        rig.until(SimEvents.Type.CORE_OPEN, 4);
        return rig;
    }

    @Test
    void aSamSalvoFollowsItsBatterysHatchAndTheStationKeepsAtMostEightMissilesAlive() {
        // Every battery armed (a full-turn arc) and a salvo every 3 s staggered: a turn every 0.5 s, past the 0.4 s
        // tell.
        SetPieceSpec spec = Act5HSpecs.sovereign(true, Act5HSpecs.rings(360, 1), 3);
        Sortie sortie = Act5HSpecs.sortie(spec, TestSpecs.RULES.withInvulnerableShip());
        Act5HSpecs.untilUp(sortie);
        SetPiece boss = Act5HSpecs.boss(sortie);
        int[] hatchAt = new int[boss.partCount()];
        java.util.Arrays.fill(hatchAt, -1000);
        int most = 0;
        int launched = 0;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.POD_HATCH) {
                    int p = events.value(e);
                    assertTrue(
                            Act5HSpecs.SAMS.contains(p)
                                    && boss.ring().orElseThrow().armed(p),
                            "an armed SAM");
                    hatchAt[p] = i;
                }
                if (events.type(e) == SimEvents.Type.MISSILE_LAUNCHED) {
                    launched++;
                    int from = -1;
                    for (int p : Act5HSpecs.SAMS) {
                        if (Math.abs(boss.partX(p) - events.x(e)) < 1e-9
                                && Math.abs(boss.partY(p) - events.y(e)) < 1e-9) {
                            from = p;
                        }
                    }
                    assertTrue(from >= 0, "from a SAM battery");
                    assertTrue(boss.partHatchOpen(from), "through its open hatch");
                    assertTrue(i - hatchAt[from] >= SimStep.ticks(0.4), "after its 0.4 s tell: " + (i - hatchAt[from]));
                }
            }
            int alive = sortie.missiles().inFlightOf(Missiles.STATION);
            assertTrue(alive <= 8, "the station's cap: " + alive);
            most = Math.max(most, alive);
        }
        assertTrue(launched > 8, "salvos: " + launched);
        assertEquals(8, most, "the cap is reached");
    }

    @Test
    void theOpenCoresBaysLaunchDartStreamsFromTheBayNearestTheShipAfterTheirTell() {
        Act5HSpecs.Rig rig = openRig(240, 40);
        RingCore core = rig.piece.ring().orElseThrow().core().orElseThrow();
        int open = rig.tick;
        assertEquals(List.of(0), rig.until(SimEvents.Type.BAY_TELL, 1), "the bay straight down, nearest the ship");
        assertEquals(SimStep.ticks(0.5), rig.tick - open, 1, "its tell 0.5 s before the stream, 1 s after the opening");
        assertEquals(0, core.bayOpen());
        List<double[]> launches = new ArrayList<>();
        List<Integer> at = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(4.9); i++) {
            rig.step();
            assertTrue(rig.values(SimEvents.Type.BAY_TELL).isEmpty(), "one bay every 5 s");
            for (double[] launch : rig.launches) {
                launches.add(launch);
                at.add(rig.tick);
            }
        }
        assertEquals(4, launches.size(), "a stream of 4");
        assertEquals(SimStep.ticks(0.5), at.getFirst() - open - SimStep.ticks(0.5), 1);
        for (int k = 0; k < 4; k++) {
            assertEquals(core.bayX(0), launches.get(k)[0], EPS, "out of its bay");
            assertEquals(core.bayY(0), launches.get(k)[1], EPS);
            assertEquals(-90 - 15 + 10 * k, Math.toDegrees(launches.get(k)[2]), 1e-6, "outward, spread ±15°");
            if (k > 0) {
                assertEquals(SimStep.ticks(0.4), at.get(k) - at.get(k - 1), 1, "0.4 s apart");
            }
        }
        assertEquals(List.of(0), rig.until(SimEvents.Type.BAY_TELL, 1), "the next turn");
    }

    @Test
    void aBayWithin72PxOfTheShipSkipsItsTurnAndNoneLaunchesWhenAllAre() {
        Act5HSpecs.Rig rig = openRig(240, Act5HSpecs.Y - 35);
        // The ship over the bay straight down: it and the two beside it lie within 72 px; the far one launches.
        assertEquals(List.of(2), rig.until(SimEvents.Type.BAY_TELL, 1));
        rig.shipY = Act5HSpecs.Y;
        int launches = 0;
        int tells = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            rig.step();
            tells += rig.values(SimEvents.Type.BAY_TELL).size();
            launches += rig.launches.size();
        }
        assertEquals(0, tells, "the ship over the core: every bay within 72 px, the turns pass");
        assertEquals(4, launches, "only the stream already told");
    }

    @Test
    void theBaysDartsAreCappedAndCountOnlyWhereTheKillRatioCountsTheBosses() {
        for (boolean counted : new boolean[] {true, false}) {
            Secondary secondary = new Secondary(0.65, 100).withBossesCounted(counted);
            // A cap of 2 Darts alive (the design's 16 is the content's), so it is reached.
            RingSpec.Core core = Act5HSpecs.CORE_BLOCK;
            RingSpec.Bays bays = core.bays().orElseThrow();
            EnemySpec.Launcher l = bays.launcher();
            RingSpec.Core capped = new RingSpec.Core(
                    core.part(),
                    core.glowSeconds(),
                    core.openingSeconds(),
                    core.shield(),
                    core.spiral(),
                    core.spiralSeconds(),
                    core.restSeconds(),
                    core.spiralTellSeconds(),
                    core.rail(),
                    core.railBelow(),
                    Optional.of(new RingSpec.Bays(
                            bays.count(),
                            bays.radius(),
                            bays.startRadians(),
                            new EnemySpec.Launcher(
                                    l.unit(),
                                    l.stream(),
                                    l.everySeconds(),
                                    l.gapSeconds(),
                                    l.firstSeconds(),
                                    l.speed(),
                                    l.spreadRadians(),
                                    l.straightSeconds(),
                                    2),
                            bays.tellSeconds(),
                            bays.skipWithinPx())));
            LevelScript level = Act5HSpecs.level(
                    Act5HSpecs.sovereign(true, Act5HSpecs.rings(120, 1).withCore(Optional.of(capped))));
            level = level.toBuilder()
                    .objectives(level.objectives().withSecondary(secondary))
                    .build();
            Sortie sortie = new Sortie(
                    1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
            Act5HSpecs.untilUp(sortie);
            SetPiece boss = Act5HSpecs.boss(sortie);
            for (int ring = 0; ring < 3; ring++) {
                Act5HSpecs.killRing(boss, ring);
            }
            int total = sortie.enemyTotal();
            int launched = 0;
            int most = 0;
            for (int i = 0; i < SimStep.ticks(40); i++) {
                sortie.step(Command.NONE);
                launched += sortie.events().count(SimEvents.Type.BOSS_LAUNCHED);
                int alive = 0;
                for (int e = 0; e < sortie.enemyCount(); e++) {
                    alive += sortie.enemy(e).launchedBy() == Launching.BOSS ? 1 : 0;
                }
                assertTrue(alive <= 2, "the Dart cap: " + alive);
                most = Math.max(most, alive);
            }
            assertTrue(launched >= 8, "streams: " + launched);
            assertEquals(2, most, "the cap reached");
            assertEquals(counted ? total + launched : total, sortie.enemyTotal(), "counted: " + counted);
        }
    }
}
