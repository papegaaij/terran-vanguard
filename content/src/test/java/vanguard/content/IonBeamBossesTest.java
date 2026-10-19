package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Beam;
import vanguard.sim.Command;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Round 37 R.4 (c), M7 part A: the Ion Beam (L3, from the data) stops on the Brood Carrier in its first,
 * high-air phase (Level 07's boss: its hull, between its bays and core) and on the Siege Spire (Level 14's
 * boss: its maw, trunk and core in the root phase) instead of running past them, each in an arena of its
 * own (SiegeSpireTest's) with nothing hitting the ship.
 */
class IonBeamBossesTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final int FIRE = Command.FIRE.bit();

    /** An arena with only {@code slug}, arriving at the centre (SiegeSpireTest's sections). */
    private static Sortie arena(String slug) {
        double arena = SiegeSpireTest.ARENA;
        LevelScript level = new LevelScript(
                14,
                2,
                0,
                List.of(
                        new LevelScript.Section(arena, 140),
                        new LevelScript.Section(arena + 200, 0, true),
                        new LevelScript.Section(arena + 212, 140)),
                List.of(),
                List.of(),
                List.of(),
                0,
                List.of(),
                LevelScript.Secondary.NONE,
                List.of(),
                List.of(),
                List.of(SimSpecs.boss(
                        CONTENT,
                        new LevelData.BossPlacement(slug, arena, PlayField.WIDTH / 2.0, 2),
                        Difficulty.MEDIUM)));
        Loadout loadout =
                ActThreeArsenalFlightTest.loadout(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 3));
        return new Sortie(
                1,
                loadout,
                level,
                SimSpecs.rules(CONTENT, Level13Test.LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    /** Steps with fire held until the boss has settled (at most 30 s). */
    private static void settle(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(FIRE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BOSS_SETTLED) {
                    return;
                }
            }
        }
        throw new AssertionError("the boss never settled");
    }

    /** Steps with fire held, the ship steered to {@code x} (within 3 px), at most 3 s. */
    private static void steerTo(Sortie sortie, double x) {
        for (int i = 0; i < SimStep.ticks(3) && Math.abs(sortie.ship().x() - x) > 3; i++) {
            sortie.step(FIRE | (sortie.ship().x() < x ? Command.RIGHT.bit() : Command.LEFT.bit()));
        }
        sortie.step(FIRE);
    }

    @Test
    void theBeamStopsOnTheBroodCarriersHullInItsFirstPhase() {
        Sortie sortie = arena("brood-carrier");
        settle(sortie);
        SetPiece carrier = sortie.setPiece(0);
        // 40 px right of its centre line: between the core (60 px wide) and the bays (±86–95 px).
        steerTo(sortie, carrier.renderX(1) + 40);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(FIRE);
            assertEquals(0, carrier.phase(), "the first, high-air phase");
            assertEquals(Layer.HIGH_AIR, carrier.layer());
            Beam beam = sortie.beam(0);
            assertEquals(Beam.Contact.BLOCKED, beam.contact(), "the hull stops it without damage");
            assertTrue(beam.endY() < carrier.renderY(1), "at the hull's lower half");
        }
    }

    @Test
    void theBeamStopsOnTheSiegeSpireInItsRootPhase() {
        Sortie sortie = arena("siege-spire");
        settle(sortie);
        SetPiece spire = sortie.setPiece(0);
        steerTo(sortie, spire.renderX(1));
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(FIRE);
            assertEquals(0, spire.phase(), "the root phase");
            Beam beam = sortie.beam(0);
            assertEquals(Beam.Contact.BLOCKED, beam.contact(), "its shielded maw and armoured trunk stop it");
            assertTrue(Double.isNaN(beam.glanceY()), "not passed over as hardened ground");
            assertTrue(beam.endY() < spire.renderY(1) + 100, "at the trunk");
        }
    }
}
