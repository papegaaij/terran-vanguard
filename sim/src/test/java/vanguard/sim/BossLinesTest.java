package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H1b ({@link BossLines}; design/enemies/bosses/iron-sovereign, Fire rules): the rail batteries' and
 * the core's rail lines on the test Sovereign ({@link Act5HSpecs#SOVEREIGN}): the line's direction locks at its
 * telegraph's start, its origin rides with its battery on the turning ring (round 68's row 9, pending), the direction
 * led for the ring's turn so the live line passes through the ship's locked point (round 69's B.1), one telegraphs at a time, at least 1.5 s apart, none while the ship is within 100 px of its battery; the core's rail
 * only below half its HP; a line hits the ship once and nothing else; the EMP Burst does nothing to it.
 */
class BossLinesTest {
    private static final double EPS = 1e-9;
    private static final int SPECIAL = Command.SPECIAL.bit();

    private static Act5HSpecs.Rig upRig() {
        Act5HSpecs.Rig rig = new Act5HSpecs.Rig(Act5HSpecs.SOVEREIGN);
        rig.until(SimEvents.Type.RINGS_UP, Act5HSpecs.SPIN_UP + 2);
        return rig;
    }

    @Test
    void aLinesDirectionLocksAtItsTelegraphsStartAndItsOriginRidesWithItsBattery() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        BossLines lines = boss.lines().orElseThrow();
        rig.shipX = 240;
        rig.shipY = 40;
        int p = rig.until(SimEvents.Type.BOSS_LINE_TELEGRAPH, 20).getFirst();
        assertTrue(Act5HSpecs.RAILS.contains(p), "a rail battery's line: " + p);
        assertTrue(rig.rings().armed(p), "from an armed battery");
        assertEquals(p, lines.part());
        assertTrue(lines.telegraphing());
        double fromX = lines.fromX();
        double fromY = lines.fromY();
        assertEquals(boss.partX(p), fromX, EPS, "from the battery's centre");
        assertEquals(boss.partY(p), fromY, EPS);
        double length = Math.hypot(240 - fromX, 40 - fromY);
        double dirX = lines.dirX();
        double dirY = lines.dirY();
        assertEquals(1, Math.hypot(dirX, dirY), EPS, "a unit direction");
        assertTrue(
                RailBunkerTest.across(fromX, fromY, dirX, dirY, 240, 40) > 5,
                "led: from the battery now it passes beside the ship");
        assertTrue(Math.abs((240 - fromX) / length - dirX) > 1e-3, "round 69's B.1: led, not straight at the ship");
        rig.shipX = 420;
        int telegraph = SimStep.ticks(Act5Specs.RAIL.telegraphSeconds());
        for (int i = 1; i < telegraph; i++) {
            rig.step();
            assertTrue(lines.telegraphing(), "the telegraph runs its second");
            assertTrue(rig.values(SimEvents.Type.BOSS_LINE_FIRED).isEmpty());
            assertEquals(boss.partX(p), lines.fromX(), EPS, "the origin rides with its battery");
            assertEquals(boss.partY(p), lines.fromY(), EPS);
            assertEquals(dirX, lines.dirX(), EPS, "the direction locked, not tracking");
            assertEquals(dirY, lines.dirY(), EPS);
        }
        assertTrue(
                Math.hypot(lines.fromX() - fromX, lines.fromY() - fromY) > 10, "the ring turned the line's origin on");
        double stepX = lines.stepX();
        double stepY = lines.stepY();
        assertTrue(Math.hypot(stepX, stepY) > 0, "its last step's move (the autopilot's look-ahead)");
        rig.step();
        assertEquals(boss.partX(p), lines.fromX(), EPS, "live, still on its battery");
        double miss = RailBunkerTest.across(lines.fromX(), lines.fromY(), dirX, dirY, 240, 40);
        assertTrue(miss < 1, "round 69's B.1: the live line passes through the ship's locked point: " + miss);
        Hull hull = TestSpecs.HULL;
        assertTrue(lines.strikes(hull, 240, 40), "a ship still on its locked point is hit");
        assertFalse(lines.strikes(hull, 420, 40), "the sidestep in the telegraph dodged it");
        double atX = lines.fromX() + dirX * 150;
        double atY = lines.fromY() + dirY * 150;
        assertFalse(lines.strikes(hull, atX - dirY * 30, atY + dirX * 30), "30 px off the riding band is clear");
        assertTrue(lines.strikes(hull, atX, atY), "the band runs from the battery where it is now");
        assertTrue(lines.live(), "then it fires along its line");
        assertEquals(List.of(p), rig.values(SimEvents.Type.BOSS_LINE_FIRED));
        assertEquals(1, lines.charge(), EPS);
        rig.steps(Act5Specs.RAIL.liveSeconds());
        assertFalse(lines.running(), "live its 0.2 s");
    }

    @Test
    void oneLineTelegraphsAtATimeAtLeastOneAndAHalfSecondsApartAndNoneWhileTheShipIsClose() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        BossLines lines = boss.lines().orElseThrow();
        int last = -1;
        int started = 0;
        int waited = 0;
        for (int i = 0; i < SimStep.ticks(90); i++) {
            // The ship wanders just below the middle ring, so the batteries pass close over it.
            rig.shipX = 240 + 180 * Math.sin(i * 0.003);
            rig.shipY = 200 + 40 * Math.sin(i * 0.011);
            rig.step();
            for (int p : rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH)) {
                started++;
                assertTrue(last < 0 || i - last >= SimStep.ticks(BossLines.GAP_SECONDS), "staggered: " + (i - last));
                last = i;
                double distance = Math.hypot(rig.shipX - boss.partX(p), rig.shipY - boss.partY(p));
                assertTrue(distance > Act5Specs.RAIL.holdWithinPx(), "it holds within 100 px: " + distance);
            }
            assertTrue(rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).size() <= 1, "one at a time");
            for (int p : Act5HSpecs.RAILS) {
                if (lines.due(p)
                        && Math.hypot(rig.shipX - boss.partX(p), rig.shipY - boss.partY(p))
                                <= Act5Specs.RAIL.holdWithinPx()) {
                    waited++;
                }
            }
        }
        assertTrue(started >= 10, "lines: " + started);
        assertTrue(waited > 0, "a due line waits while the ship is close");
    }

    @Test
    void theOpenCoresRailFiresOnlyBelowHalfItsHp() {
        Act5HSpecs.Rig rig = upRig();
        SetPiece boss = rig.piece;
        rig.shipX = 240;
        rig.shipY = 40;
        Act5HSpecs.killRing(boss, Act5HSpecs.OUTER);
        Act5HSpecs.killRing(boss, Act5HSpecs.MIDDLE);
        Act5HSpecs.killRing(boss, Act5HSpecs.INNER);
        rig.step();
        assertEquals(3, boss.phase());
        rig.steps(4);
        assertTrue(boss.ring().orElseThrow().core().orElseThrow().open());
        for (int i = 0; i < SimStep.ticks(15); i++) {
            rig.step();
            assertTrue(rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).isEmpty(), "no rail above half");
        }
        // Through its shield, to 40 % of its HP.
        boss.damagePartFlat(Act5HSpecs.CORE, 0.6 * Act5HSpecs.CORE_HP + Act5HSpecs.CORE_SHIELD.hp());
        assertEquals(0.4 * Act5HSpecs.CORE_HP, boss.partHp(Act5HSpecs.CORE), 1e-6);
        assertEquals(List.of(Act5HSpecs.CORE), rig.until(SimEvents.Type.BOSS_LINE_TELEGRAPH, 6 + 1));
        BossLines lines = boss.lines().orElseThrow();
        double length = Math.hypot(240 - lines.fromX(), 40 - lines.fromY());
        assertEquals((240 - lines.fromX()) / length, lines.dirX(), EPS, "the still core aims straight at the ship");
        assertEquals((40 - lines.fromY()) / length, lines.dirY(), EPS);
        int first = rig.tick;
        assertEquals(List.of(Act5HSpecs.CORE), rig.until(SimEvents.Type.BOSS_LINE_TELEGRAPH, 6 + 1));
        assertEquals(SimStep.ticks(6), rig.tick - first, 1, "every 6 s");
    }

    @Test
    void aLiveLineHitsTheShipOnceAndTheEmpDoesNothingToIt() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(3)),
                Act5HSpecs.level(Act5HSpecs.SOVEREIGN),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Act5HSpecs.untilUp(sortie);
        BossLines lines = Act5HSpecs.boss(sortie).lines().orElseThrow();
        int fired = 0;
        int hits = 0;
        boolean burst = false;
        for (int i = 0; i < SimStep.ticks(20) && !sortie.failed; i++) {
            boolean press = !burst && lines.telegraphing();
            burst |= press;
            sortie.step(press ? SPECIAL : Command.NONE);
            fired += sortie.events().count(SimEvents.Type.BOSS_LINE_FIRED);
            int hit = sortie.events().count(SimEvents.Type.BOSS_LINE_HIT);
            assertTrue(hit <= 1);
            hits += hit;
            assertTrue(hits <= fired, "once per line");
            if (press) {
                assertTrue(lines.running(), "the EMP cancels no boss line");
            }
        }
        assertTrue(burst, "the EMP went off in a telegraph");
        assertTrue(fired >= 3 && hits >= 1, "fired " + fired + ", hit " + hits);
    }
}
