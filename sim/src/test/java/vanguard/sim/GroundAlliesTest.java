package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.A_T;
import static vanguard.sim.SiegeSpecs.A_X;
import static vanguard.sim.SiegeSpecs.EDEN_T;
import static vanguard.sim.SiegeSpecs.count;
import static vanguard.sim.SiegeSpecs.domeY;
import static vanguard.sim.SiegeSpecs.runTo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D's ground allies (design/allies, Ares Landing dome; design/campaign Level 17, The Ares Landing
 * domes and What goes for the domes): the domes enter the top edge at their times and scroll through with the
 * ground, each with its own footprint; only hooked attacks hurt them, as hits; they never fail the mission;
 * the {@code alternate} hook sends every second lob at the nearest dome closer than the ship, its marker in
 * ground space; a walker with a mortar lobs.
 */
class GroundAlliesTest {
    @Test
    void theDomesEnterAtTheirTimesAndScrollThroughWithTheGround() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of()));
        assertTrue(sortie.groundAllies());
        assertEquals(3, sortie.allyCount());
        assertEquals("Bradbury", sortie.allyName(0));
        assertEquals("Eden", sortie.allyName(2));
        runTo(sortie, A_T);
        Ally a = sortie.ally(0);
        assertEquals(A_X, a.x());
        assertEquals(PlayField.HEIGHT, a.y() - 60, 1.5, "its footprint's lower edge at the top edge at its time");
        runTo(sortie, 8);
        assertEquals(domeY(A_T, 120, 8), a.y(), 1.5, "it scrolls with the ground at 80 px/s");
        runTo(sortie, 14);
        assertTrue(a.y() + 60 < 0, "past the bottom edge at t≈13.3");
        assertTrue(a.alive(), "nothing went for it");
        runTo(sortie, EDEN_T);
        assertEquals(PlayField.HEIGHT, sortie.ally(2).y() - 72, 1.5, "Eden has its own 144 px footprint");
    }

    @Test
    void onlyHookedAttacksHurtThemNotShotsAimedAtTheShip() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of()));
        runTo(sortie, 8);
        Ally a = sortie.ally(0);
        sortie.fireEnemyBullet(a.x() + 2, a.y(), -60, 0, 20);
        sortie.step(Command.NONE);
        assertEquals(0, a.hitsTaken(), "it passes over the dome");
        assertEquals(0, count(sortie, SimEvents.Type.ALLY_HIT));
    }

    @Test
    void brokenDomesNeverFailTheMission() {
        // Three hooked bombers on each dome break all three.
        LevelScript level = SiegeSpecs.level(List.of(
                SiegeSpecs.bombers(0, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                SiegeSpecs.bombers(8, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                SiegeSpecs.bombers(17, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)));
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int lost = 0;
        while (sortie.levelSeconds() < SiegeSpecs.LENGTH + 1 && !sortie.complete()) {
            sortie.step(Command.NONE);
            lost += count(sortie, SimEvents.Type.ALLY_LOST);
            assertEquals(0, count(sortie, SimEvents.Type.PRIMARY_FAILED));
        }
        assertEquals(3, lost, "every dome broke");
        assertFalse(sortie.primaryFailed());
        assertTrue(sortie.complete(), "the level ends as any reach-end level");
        assertEquals(0, sortie.alliesUntouched());
    }

    @Test
    void alternateSendsEverySecondLobAtTheNearestDomeItsMarkerScrollingWithIt() {
        // A mortar at the dome's x, entering at t=2: its first lob goes at the ship, its second at the dome.
        LevelScript.GroundUnit mortar = new LevelScript.GroundUnit(2, A_X, CraterNestTest.MORTAR, -1);
        LevelScript level = SiegeSpecs.level(
                List.of(),
                List.of(mortar),
                LevelScript.Secondary.NONE,
                Optional.of(SiegeSpecs.domes(Map.of("polyp-mortar", LevelScript.HookMode.ALTERNATE))));
        Sortie sortie = SiegeSpecs.sortie(level);
        // The ship keeps to the bottom right, far from the dome's lane.
        int away = Command.of(Command.RIGHT, Command.DOWN);
        int lobs = 0;
        int aimed = 0;
        double lastY = Double.NaN;
        boolean hit = false;
        while (sortie.levelSeconds() < 10) {
            sortie.step(away);
            if (count(sortie, SimEvents.Type.MORTAR_LOBBED) > 0) {
                lobs++;
                Lob lob = sortie.lob(sortie.lobCount() - 1);
                assertEquals(lobs == 2, lob.objectiveAimed(), "the first at the ship, the second at the dome");
            }
            for (int i = 0; i < sortie.lobCount(); i++) {
                Lob lob = sortie.lob(i);
                if (!lob.objectiveAimed()) {
                    continue;
                }
                aimed++;
                assertEquals(A_X, lob.targetX(), 1e-9, "the marker lies on the dome");
                assertEquals(sortie.ally(0).y(), lob.targetY(), 1.5, "in ground space: it scrolls with the dome");
                if (!Double.isNaN(lastY)) {
                    assertEquals(SiegeSpecs.SPEED * SimStep.SECONDS, lastY - lob.targetY(), 1e-9);
                }
                lastY = lob.targetY();
            }
            if (count(sortie, SimEvents.Type.ALLY_HIT) > 0) {
                hit = true;
                assertEquals(1, sortie.ally(0).hitsTaken(), "the direct hit is one hit");
            }
        }
        assertEquals(2, lobs);
        assertTrue(aimed > 0);
        assertTrue(hit, "the dome-aimed lob landed on the dome");
    }

    @Test
    void aWalkerWithAMortarLobsInsteadOfFiringAFan() {
        LevelScript level = SiegeSpecs.level(List.of(SiegeSpecs.walker(1, 240)));
        Sortie sortie = SiegeSpecs.sortie(level);
        int lobbed = 0;
        while (sortie.levelSeconds() < 12) {
            sortie.step(Command.NONE);
            lobbed += count(sortie, SimEvents.Type.MORTAR_LOBBED);
            assertEquals(0, count(sortie, SimEvents.Type.ENEMY_FIRED), "no fan");
        }
        assertTrue(lobbed >= 1, "it lobs: " + lobbed);
    }

    @Test
    void groundAlliesStandBesideNoOtherConvoyAndNotWithHoldsOrAnArena() {
        LevelScript level = SiegeSpecs.level(List.of());
        assertThrows(
                IllegalArgumentException.class,
                () -> level.withDefend(PartGSpecs.defend(PartGSpecs.RELAY, Map.of()), LevelScript.Secondary.NONE));
        LevelScript arena = new LevelScript(
                17,
                3,
                0,
                List.of(new LevelScript.Section(10, 80), new LevelScript.Section(20, 80, true, 0)),
                List.of(),
                List.of(),
                List.of(),
                0,
                List.of(),
                LevelScript.Secondary.NONE,
                List.of(),
                List.of(),
                List.of());
        assertThrows(
                IllegalArgumentException.class, () -> arena.withGroundAllies(Optional.of(SiegeSpecs.hookedDomes())));
        assertThrows(IllegalArgumentException.class, () -> new LevelScript.GroundAlly(SiegeSpecs.DOME, "Off", -1, 5));
        assertThrows(IllegalArgumentException.class, () -> new LevelScript.GroundAllies(List.of(), Map.of(), 160, 40));
    }

    @Test
    void aLevelWithGroundAlliesStepsDeterministicallyWithoutAllocating() {
        LevelScript.GroundUnit mortar = new LevelScript.GroundUnit(2, A_X, CraterNestTest.MORTAR, -1);
        LevelScript level = SiegeSpecs.level(
                List.of(
                        SiegeSpecs.bombers(
                                1, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                        SiegeSpecs.walker(4, 300)),
                List.of(mortar),
                LevelScript.Secondary.untouched(100, "DOMES"),
                Optional.of(SiegeSpecs.domes(Map.of(
                        "spore-bomber", LevelScript.HookMode.ALWAYS,
                        "polyp-mortar", LevelScript.HookMode.ALTERNATE,
                        "shellback", LevelScript.HookMode.ALTERNATE))));
        Sortie one = SiegeSpecs.sortie(level);
        Sortie two = SiegeSpecs.sortie(level);
        for (int i = 0; i < SimStep.ticks(25); i++) {
            one.step(Command.NONE);
            two.step(Command.NONE);
        }
        assertEquals(one.stateHash(), two.stateHash());
        long allocated = Allocations.least(() -> SiegeSpecs.sortie(level), sortie -> {
            for (int i = 0; i < SimStep.ticks(25); i++) {
                sortie.step(Command.NONE);
            }
        });
        assertEquals(0, allocated);
    }
}
