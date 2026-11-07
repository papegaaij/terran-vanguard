package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1b ({@link PartWindows}; design/enemies/bosses/choir-heart, the membranes; design/enemies, Window
 * shields): a membrane is closed (shielded: no shot, seeker or Rook takes it) except from the first frame of its song's
 * tell to its last bullet, its open and close events on those steps; a breaker hit holds one open 3 s, the EMP every one;
 * the Tesla arc picks a closed membrane when nothing else is in reach and opens it; Rook's order takes the nearest open
 * membrane first (a closed one is never first), then in phase 2 the node on his side; a run replays.
 */
class PartWindowsTest {
    private static Sortie engaged(SetPieceSpec heart) {
        Sortie sortie = Act7HPlaySpecs.invulnerable(1, heart);
        Act7HSpecs.untilEngaged(sortie);
        return sortie;
    }

    @Test
    void aMembraneIsOpenFromItsTellsFirstFrameToItsLastBullet() {
        Sortie sortie = engaged(Act7HPlaySpecs.heart(Act7HPlaySpecs.boss(false)));
        SetPiece heart = sortie.setPiece(0);
        BossPlaybook book = heart.playbook().orElseThrow();
        PartWindows windows = heart.partWindows().orElseThrow();
        int songs = 0;
        for (int i = 0; i < SimStep.ticks(120) && songs < 3; i++) {
            int draws = book.draws();
            sortie.step(0);
            if (book.draws() == draws || book.move() != Act7HPlaySpecs.SONG_HERALD) {
                continue;
            }
            // The Herald's song was drawn in this step: two volleys 0.6 s apart after its 0.8 s tell.
            assertEquals(1, count(sortie, SimEvents.Type.PART_WINDOW_OPEN, Act7HPlaySpecs.M1), "opens on the tell");
            int open = 0;
            boolean closed = false;
            while (!closed) {
                assertTrue(windows.open(Act7HPlaySpecs.M1));
                assertFalse(heart.partShielded(Act7HPlaySpecs.M1), "open: it takes damage");
                open++;
                int bullets = sortie.bulletCount();
                sortie.step(0);
                closed = count(sortie, SimEvents.Type.PART_WINDOW_CLOSE, Act7HPlaySpecs.M1) == 1;
                if (closed) {
                    assertTrue(sortie.bulletCount() > bullets, "it closes on its last volley's step");
                }
            }
            assertEquals(SimStep.ticks(0.8) + SimStep.ticks(0.6), open, "open over the tell and the song");
            assertTrue(windows.closed(Act7HPlaySpecs.M1));
            assertTrue(heart.partShielded(Act7HPlaySpecs.M1), "closed: shielded");
            assertTrue(heart.partWindowClosed(Act7HPlaySpecs.M1), "shielded by its window alone");
            songs++;
        }
        assertEquals(3, songs);
    }

    private static int count(Sortie sortie, SimEvents.Type type, int value) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            n += events.type(i) == type && events.value(i) == value ? 1 : 0;
        }
        return n;
    }

    @Test
    void aBreakerHoldsOneOpenThreeSecondsTheEmpEveryOne() {
        Sortie sortie = engaged(Act7HPlaySpecs.HEART);
        SetPiece heart = sortie.setPiece(0);
        PartWindows windows = heart.partWindows().orElseThrow();
        windows.breaker(Act7HPlaySpecs.M2);
        int held = 0;
        while (windows.held(Act7HPlaySpecs.M2)) {
            assertTrue(windows.open(Act7HPlaySpecs.M2));
            assertFalse(heart.partShielded(Act7HPlaySpecs.M2));
            sortie.step(0);
            held++;
        }
        assertEquals(SimStep.ticks(3), held, "a breaker's hold of 3 s");
        heart.stripShields(SimStep.ticks(1));
        for (int p : Act7HPlaySpecs.MEMBRANES) {
            assertEquals(SimStep.ticks(3), windows.holdTicks(p), "the EMP holds every membrane open 3 s");
            assertTrue(windows.open(p));
        }
        Act7HSpecs.steps(sortie, 3);
        for (int p : Act7HPlaySpecs.MEMBRANES) {
            assertFalse(windows.held(p));
            assertEquals(windows.singing(p), windows.open(p), "after the hold open only while it sings");
        }
    }

    @Test
    void theTeslaArcPicksAClosedMembraneWhenNothingElseIsInReachAndOpensIt() {
        Loadout loadout =
                TestSpecs.loadout(new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA));
        Sortie sortie = new Sortie(
                1,
                loadout,
                Act7HPlaySpecs.level(Act7HPlaySpecs.heart(Act7HPlaySpecs.boss(false))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Act7HSpecs.untilEngaged(sortie);
        PartWindows windows = sortie.setPiece(0).partWindows().orElseThrow();
        boolean opened = false;
        for (int i = 0; i < SimStep.ticks(12) && !opened; i++) {
            sortie.step(Command.UP.bit() | Command.FIRE.bit());
            for (int p : Act7HPlaySpecs.MEMBRANES) {
                opened |= windows.held(p);
            }
        }
        assertTrue(opened, "the arc opened a closed membrane");
    }

    @Test
    void rooksOrderTakesTheNearestOpenMembraneThenTheNodeOnHisSide() {
        Sortie sortie = engaged(Act7HPlaySpecs.HEART);
        SetPiece heart = sortie.setPiece(0);
        PartWindows windows = heart.partWindows().orElseThrow();
        assertTrue(PartPicks.applies(heart));
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(0);
            int open = -1;
            for (int p : Act7HPlaySpecs.MEMBRANES) {
                if (windows.open(p)) {
                    open = open < 0 ? p : open;
                }
            }
            for (int p : Act7HPlaySpecs.MEMBRANES) {
                int rank = PartPicks.rank(heart, p, heart.partX(p), 100);
                if (windows.closed(p)) {
                    assertTrue(rank != PartPicks.FIRST, "a closed membrane is never first");
                } else if (rank == PartPicks.FIRST) {
                    assertTrue(windows.open(p));
                }
            }
            if (open >= 0) {
                // From right under it, the open membrane is his first.
                assertEquals(PartPicks.FIRST, PartPicks.rank(heart, open, heart.partX(open), 100));
            }
        }
        Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.MEMBRANES);
        Act7HSpecs.untilPhase(sortie, 1);
        Act7HSpecs.steps(sortie, 6);
        assertEquals(PartPicks.FIRST, PartPicks.rank(heart, Act7HPlaySpecs.N_LL, 100, 60), "the node on his side");
        assertEquals(PartPicks.FIRST, PartPicks.rank(heart, Act7HPlaySpecs.N_RL, 380, 60));
        Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.N_LL, Act7HPlaySpecs.N_LU);
        assertEquals(PartPicks.FIRST, PartPicks.rank(heart, Act7HPlaySpecs.N_RL, 100, 60), "else the nearest");
    }

    @Test
    void rookTakesAnOpenMembraneAndAClosedOneOnlyWhenNothingElseIsHis() {
        WingmanSpec.Ai wide = new WingmanSpec.Ai(
                WingmanTest.AI.wing(),
                WingmanTest.AI.wide(),
                WingmanTest.AI.trail(),
                WingmanTest.AI.glideSeconds(),
                WingmanTest.AI.swapSeconds(),
                WingmanTest.AI.flankDistance(),
                WingmanTest.AI.reactionSeconds(),
                WingmanTest.AI.dodgeInterval(),
                WingmanTest.AI.lookAhead(),
                WingmanTest.AI.clearance(),
                WingmanTest.AI.dodgeStep(),
                WingmanTest.AI.reacts(),
                Math.toRadians(75),
                700,
                WingmanTest.AI.recentHitSeconds());
        WingmanSpec rook = new WingmanSpec(WingmanSpec.Side.RIGHT, 80, WingmanTest.CRAFT, wide, WingmanTest.GUN);
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(rook),
                Act7HPlaySpecs.level(Act7HPlaySpecs.heart(Act7HPlaySpecs.boss(false))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Act7HSpecs.untilEngaged(sortie);
        SetPiece heart = sortie.setPiece(0);
        PartWindows windows = heart.partWindows().orElseThrow();
        Wingman wingman = sortie.wingman().orElseThrow();
        int open = 0;
        int closed = 0;
        for (int i = 0; i < SimStep.ticks(20) && !wingman.out(); i++) {
            sortie.step(0);
            int part = wingman.target() - PlayerFire.PART_SERIAL;
            if (wingman.target() < 0 || part < 0 || part >= SetPieceSpec.MAX_PARTS || !windows.fitted(part)) {
                continue;
            }
            if (windows.open(part)) {
                open++;
            } else {
                closed++;
            }
        }
        assertTrue(open > 0, "an open membrane is his");
        assertTrue(closed > 0, "a closed one when nothing else is");
    }

    @Test
    void aRunReplays() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = engaged(Act7HPlaySpecs.HEART);
            for (int i = 0; i < SimStep.ticks(20); i++) {
                if (i == SimStep.ticks(5)) {
                    sortie.setPiece(0).stripShields(SimStep.ticks(1));
                }
                sortie.step(Command.FIRE.bit());
            }
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1]);
    }
}
