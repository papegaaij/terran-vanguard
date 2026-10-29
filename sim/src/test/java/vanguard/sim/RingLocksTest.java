package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H1a: a ringed boss's locks ({@link RingArena}; design/enemies/bosses/iron-sovereign, Phases): a ring
 * locks when its every battery is dead and grinds to a halt in 2 s; the phases follow the locked rings' count in any
 * order (two locks in one step pass two phases), each with its {@code boss-phase} cue by name; the survivors turn ×1.25
 * after one lock and ×1.5 after two (the inner ring's step in 3.2 s and 2.67 s, its hold unchanged); the core opens to
 * damage in the last phase; the bar is the station's total.
 */
class RingLocksTest {
    private static final double EPS = 1e-9;

    private static Act5HSpecs.Rig upRig() {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.QUIET);
        rig.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 1);
        return rig;
    }

    @Test
    void aRingLocksWhenItsLastBatteryDiesAndGrindsToAHalt() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        RingArena rings = rig.rings();
        List<Integer> sams = Act5HSpecs.SAMS;
        for (int k = 0; k < sams.size() - 1; k++) {
            boss.damagePartFlat(sams.get(k), Act5HSpecs.SAM_HP);
        }
        rig.step();
        assertFalse(rings.locked(Act5HSpecs.OUTER), "one battery still lives");
        assertEquals(0, boss.phase());
        double speed = rings.velocity(Act5HSpecs.OUTER);
        boss.damagePartFlat(sams.getLast(), Act5HSpecs.SAM_HP);
        rig.step();
        assertEquals(List.of(Act5HSpecs.OUTER), rig.values(SimEvents.Type.RING_LOCKED));
        assertEquals(List.of(1), rig.phases, "the first lock ends phase 1 at once");
        assertTrue(rings.locked(Act5HSpecs.OUTER) && rings.grinding(Act5HSpecs.OUTER));
        assertEquals(1, rings.lockedCount());
        double last = speed;
        int steps = 0;
        while (rig.values(SimEvents.Type.RING_HALTED).isEmpty()) {
            double v = rings.velocity(Act5HSpecs.OUTER);
            assertTrue(v <= last + EPS && v >= 0, "slowing evenly");
            last = v;
            rig.step();
            steps++;
        }
        assertEquals(SimStep.ticks(2) - 1, steps, "a 2 s grind");
        assertEquals(List.of(Act5HSpecs.OUTER), rig.values(SimEvents.Type.RING_HALTED));
        double still = rings.angle(Act5HSpecs.OUTER);
        rig.steps(3);
        assertEquals(still, rings.angle(Act5HSpecs.OUTER), 0, "a locked ring stays still");
        assertEquals(0, rings.velocity(Act5HSpecs.OUTER), 0);
        assertFalse(rings.armed(sams.getFirst()));
        assertFalse(boss.partArmed(sams.getFirst()));
    }

    @Test
    void theSurvivorsSpeedUpWithEachLockAndTheInnerStepsFaster() {
        Act5HSpecs.Rig rig = upRig();
        RingArena rings = rig.rings();
        assertEquals(-Math.toRadians(10), rings.velocity(Act5HSpecs.MIDDLE), EPS);
        Act5HSpecs.killRing(rig.piece, Act5HSpecs.OUTER);
        rig.step();
        assertEquals(-Math.toRadians(10) * 1.25, rings.velocity(Act5HSpecs.MIDDLE), EPS, "×1.25 after one lock");
        assertEquals(SimStep.ticks(3.2), stepTicks(rig), "the inner ring's step in 3.2 s");
        Act5HSpecs.killRing(rig.piece, Act5HSpecs.OUTER);
        assertEquals(1, rings.lockedCount(), "a lock counts once");
        rig.step();
        assertEquals(1, rig.piece.phase());
        // Any order: the middle ring locks second.
        Act5HSpecs.killRing(rig.piece, Act5HSpecs.MIDDLE);
        rig.step();
        assertEquals(List.of(2), rig.phases);
        assertEquals(SimStep.ticks(4 / 1.5), stepTicks(rig), "×1.5 after two locks");
    }

    /** Steps from the next step's start to its hold, returning its length; the hold measured too (unchanged). */
    private static int stepTicks(Act5HSpecs.Rig rig) {
        rig.until(SimEvents.Type.RING_STEP, 10);
        int steps = 0;
        while (rig.values(SimEvents.Type.RING_HOLD).isEmpty()) {
            rig.step();
            steps++;
        }
        int hold = 0;
        while (rig.values(SimEvents.Type.RING_STEP).isEmpty()) {
            rig.step();
            hold++;
        }
        assertEquals(SimStep.ticks(4), hold, "its hold unchanged");
        return steps;
    }

    @Test
    void thePhasesFollowTheLockedRingsInAnyOrderToTheOpenCore() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        assertTrue(boss.partShielded(Act5HSpecs.CORE), "the core armoured while shut");
        Act5HSpecs.killRing(boss, Act5HSpecs.INNER);
        rig.step();
        assertEquals(1, boss.phase());
        assertEquals(
                "Two rings",
                boss.boss().orElseThrow().phases().get(boss.phase()).name());
        assertTrue(boss.partShielded(Act5HSpecs.CORE));
        // Two rings at once: two phases in one step.
        Act5HSpecs.killRing(boss, Act5HSpecs.OUTER);
        Act5HSpecs.killRing(boss, Act5HSpecs.MIDDLE);
        rig.step();
        assertEquals(List.of(2, 3), rig.phases);
        assertEquals(List.of(Act5HSpecs.OUTER, Act5HSpecs.MIDDLE), rig.values(SimEvents.Type.RING_LOCKED));
        assertEquals(3, boss.phase());
        assertEquals("The core", boss.boss().orElseThrow().phases().get(3).name());
        rig.step();
        assertTrue(boss.partShielded(Act5HSpecs.CORE), "step H1b: untouchable while it opens");
        rig.steps(3);
        assertFalse(boss.partShielded(Act5HSpecs.CORE), "the core opens to damage in the last phase");
        rig.steps(30);
        assertEquals(3, boss.phase(), "the last phase ends on the core only");
    }

    @Test
    void theBarIsTheStationsTotal() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        assertEquals(1, boss.barShare(), EPS);
        boss.damagePartFlat(Act5HSpecs.SAMS.getFirst(), Act5HSpecs.SAM_HP);
        assertEquals(1 - Act5HSpecs.SAM_HP / Act5HSpecs.TOTAL_HP, boss.barShare(), EPS, "a battery's share");
        Act5HSpecs.killRing(boss, Act5HSpecs.MIDDLE);
        double left = Act5HSpecs.TOTAL_HP - Act5HSpecs.SAM_HP - 4 * Act5HSpecs.RAIL_HP;
        assertEquals(left / Act5HSpecs.TOTAL_HP, boss.barShare(), EPS, "a ring's share");
        assertEquals(8960, Act5HSpecs.TOTAL_HP, 0, "the design's total");
    }

    @Test
    void eachLockCuesItsPhaseByNameOnTheRadio() {
        List<RadioCue> radio = List.of(
                new RadioCue(
                        CueTrigger.BOSS_PHASE, 0, "Two rings", "Varga", "Such dedication.", false, "neutral", "Varga"),
                new RadioCue(CueTrigger.BOSS_PHASE, 0, "One ring", "Varga", "One left!", false, "neutral", "Varga"),
                new RadioCue(CueTrigger.BOSS_PHASE, 0, "The core", "Varga", "Enough.", false, "neutral", "Varga"));
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                Act5HSpecs.level(Act5HSpecs.QUIET, radio),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Act5HSpecs.untilUp(sortie);
        SetPiece boss = Act5HSpecs.boss(sortie);
        Act5HSpecs.killRing(boss, Act5HSpecs.MIDDLE);
        assertEquals(List.of(1), MawSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        assertEquals(List.of(0), MawSpecs.values(sortie, SimEvents.Type.RADIO), "Two rings");
        MawSpecs.steps(sortie, 8, 0);
        Act5HSpecs.killRing(boss, Act5HSpecs.OUTER);
        assertEquals(List.of(2), MawSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        assertEquals(List.of(1), MawSpecs.values(sortie, SimEvents.Type.RADIO), "One ring");
        MawSpecs.steps(sortie, 8, 0);
        Act5HSpecs.killRing(boss, Act5HSpecs.INNER);
        assertEquals(List.of(3), MawSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        assertEquals(List.of(2), MawSpecs.values(sortie, SimEvents.Type.RADIO), "The core");
    }
}
