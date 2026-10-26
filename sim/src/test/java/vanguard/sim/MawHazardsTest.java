package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The escort; design/campaign Level 28, Rook and the escort):
 * {@link MawHazards}, shared by the Bulwark's and Rook's hazards: the escort holds off the coil, keeps out of a told or
 * swinging bight and out of the lunge's column, keeps out of a told broadside band in the run, and the Bulwark aims at
 * a gill sac while the Maw is coiled, an open implant, else the head.
 */
class MawHazardsTest {
    private static Sortie withBulwark(MawSpecs.Fit fit, Rules rules) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK),
                MawSpecs.level(fit, List.of()),
                rules,
                TestSpecs.FULL_ARMOUR);
    }

    private static int escortStruck(Sortie sortie, SimEvents.Type type) {
        int struck = 0;
        for (int value : MawSpecs.values(sortie, type)) {
            struck += (value & CoilArena.STRUCK_ESCORT) != 0 ? 1 : 0;
        }
        return struck;
    }

    @Test
    void theBulwarkHoldsOffTheCoilAndKeepsOutOfAToldBight() {
        Sortie sortie = withBulwark(MawSpecs.Fit.QUIET.withSweeps(), TestSpecs.RULES);
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        // It flies on the ship's right: with the ship by the right wall it holds the gap off the wall.
        MawSpecs.steerTo(sortie, 380, 250);
        MawSpecs.steps(sortie, 2, 0);
        assertEquals(coil.innerRight() - MawHazards.GAP, bulwark.x(), 0.1, "off the coil");
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        double armour = bulwark.armour();
        List<Integer> side = List.of();
        while (!side.equals(List.of(CoilSpec.Side.RIGHT.ordinal()))) {
            side = MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP_TELL, 15, 0);
        }
        int escort = 0;
        int ship = 0;
        for (int i = 0; i < SimStep.ticks(1.2 + 0.5 + 0.8); i++) {
            sortie.step(0);
            escort += escortStruck(sortie, SimEvents.Type.COIL_STRIKE);
            for (int value : MawSpecs.values(sortie, SimEvents.Type.COIL_STRIKE)) {
                ship += (value & CoilArena.STRUCK_SHIP) != 0 ? 1 : 0;
            }
        }
        assertEquals(1, ship, "the ship at the wall is caught");
        assertEquals(0, escort, "the Bulwark left the bight in its tell");
        assertEquals(armour, bulwark.armour(), 1e-9);
    }

    @Test
    void theBulwarkStepsOutOfTheLungesColumn() {
        Sortie sortie = withBulwark(MawSpecs.Fit.QUIET, TestSpecs.RULES);
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.until(sortie, SimEvents.Type.MAW_DIVE, 0.1, 0);
        MawSpecs.steerTo(sortie, 240, 100);
        MawSpecs.until(sortie, SimEvents.Type.LUNGE_TELL, 6, 0);
        int escort = 0;
        boolean lunged = false;
        for (int i = 0; i < SimStep.ticks(2 + 0.8 + 1); i++) {
            sortie.step(0);
            escort += escortStruck(sortie, SimEvents.Type.LUNGE_HIT);
            if (coil.diveState() == CoilArena.DiveState.LUNGE) {
                lunged = true;
                assertTrue(
                        Math.abs(bulwark.x() - coil.columnX()) >= coil.columnWidth() / 2 + coil.clearance(),
                        "out of the column: " + bulwark.x());
            }
        }
        assertTrue(lunged);
        assertEquals(0, escort, "the lunge missed the Bulwark");
    }

    @Test
    void theBulwarkAimsAtASacWhileItIsCoiledThenAnOpenImplant() {
        Sortie sortie = withBulwark(MawSpecs.Fit.QUIET, TestSpecs.RULES.withInvulnerableShip());
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        SetPiece boss = MawSpecs.boss(sortie);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        assertAimsAt(sortie, bulwark, boss.partX(4), boss.partY(4), "the nearest living sac (sac-4)");
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.steps(sortie, 0.1, 0);
        MawSpecs.headTo(sortie, 0.25);
        MawSpecs.untilPhase(sortie, 2, 30);
        int open = -1;
        for (int i = 0; i < SimStep.ticks(4) && open < 0; i++) {
            sortie.step(0);
            for (int k = 0; k < coil.implantCount(); k++) {
                open = coil.implantOpen(k) ? k : open;
            }
        }
        assertTrue(open >= 0, "an implant open");
        assertAimsAt(sortie, bulwark, coil.implantX(open), coil.implantY(open), "the open implant");
    }

    /** The Bulwark's next shell (fire held) flies toward (x, y). */
    private static void assertAimsAt(Sortie sortie, Bulwark bulwark, double x, double y, String what) {
        for (int i = 0; i < SimStep.ticks(3); i++) {
            double fromX = bulwark.x();
            double fromY = bulwark.y();
            sortie.step(Command.FIRE.bit());
            for (int s = 0; s < sortie.shotCount(); s++) {
                Shot shot = sortie.shot(s);
                if (shot.mount() == sortie.wingmanMount() && shot.age() <= 1) {
                    double want = StrictMath.atan2(x - fromX, y - fromY);
                    assertEquals(want, shot.heading(), 0.05, what);
                    return;
                }
            }
        }
        throw new AssertionError("no shell from the Bulwark");
    }

    @Test
    void theEscortKeepsOutOfAToldBroadsideBand() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK),
                CruiserRunTest.level(CruiserRunTest.SPEC),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        CruiserRun run = sortie.cruisers().orElseThrow();
        MawSpecs.steerTo(sortie, 240, 200);
        MawSpecs.steps(sortie, CruiserRunTest.RUN - sortie.levelSeconds() - SimStep.SECONDS, 0);
        assertTrue(Math.abs(bulwark.y() - sortie.ship().y()) < 35, "in its slot, in the band to come");
        sortie.step(0);
        assertTrue(run.bandTelling(0));
        double band = run.bandY(0);
        while (sortie.events().count(SimEvents.Type.BROADSIDE) == 0) {
            sortie.step(0);
        }
        assertTrue(Math.abs(bulwark.y() - band) >= 35 + 7, "out of the band at its volley: " + bulwark.y());
    }

    @Test
    void rooksHazardsShareTheCoilsAndTheBands() {
        Sortie sortie = withBulwark(MawSpecs.Fit.QUIET.withSweeps(), TestSpecs.RULES);
        CoilArena coil = MawSpecs.coil(sortie);
        SetPiece[] pieces = {MawSpecs.boss(sortie)};
        RookHazards hazards = new RookHazards(WingmanTest.CRAFT.hitbox(), WingmanTest.CRAFT.edgeLimit());
        MawSpecs.untilUp(sortie);
        assertTrue(hazards.clear(470, 250, pieces, null), "off the coil");
        assertEquals(coil.innerRight() - MawHazards.GAP, hazards.x(), 1e-9);
        MawSpecs.steerTo(sortie, 380, 250);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        List<Integer> side = List.of();
        while (!side.equals(List.of(CoilSpec.Side.RIGHT.ordinal()))) {
            side = MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP_TELL, 15, 0);
        }
        assertTrue(hazards.clear(400, 250, pieces, null), "out of the told bight");
        assertTrue(hazards.x() < 400 - 60, "in from the right run: " + hazards.x());
        assertFalse(hazards.clear(240, 250, pieces, null), "clear of it in the middle");

        Sortie finale = new Sortie(
                1,
                TestSpecs.LOADOUT,
                CruiserRunTest.level(CruiserRunTest.SPEC),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        CruiserRun run = finale.cruisers().orElseThrow();
        MawSpecs.steps(finale, CruiserRunTest.RUN, 0);
        assertTrue(run.bandTelling(0));
        double band = run.bandY(0);
        assertTrue(hazards.clear(240, band, new SetPiece[0], null, run));
        assertTrue(
                Math.abs(hazards.y() - band) >= 35 + WingmanTest.CRAFT.hitbox().width() / 2, "out of the band");
        assertFalse(hazards.clear(240, band, new SetPiece[0], null, null), "without the cruisers nothing moves");
    }
}
