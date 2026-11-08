package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G5b (design/enemies/bosses/dust-colossus, Echo: a steadier fight): on the test colossus's echo, the
 * mouth seals for the rest of a breach once it has lost its flinch's HP since the rise (it opens again with the next
 * rise), the vents rise in turn (each with its own breach, staying where it rose, straight into the field from its
 * edge) and count in the bar. The Level 21 colossus has none of it (the golden flights).
 */
class ColossusEchoSealTest {
    /** The flinch: HP the mouth loses in one breach before it seals. */
    static final double FLINCH = 30;

    static SetPieceSpec echo() {
        SetPieceSpec piece = ColossusEchoTest.echo();
        BossSpec boss = piece.boss().orElseThrow();
        return EchoEntryFireTest.with(
                piece,
                boss.withBreach(Optional.of(boss.breach().orElseThrow().withFlinch(FLINCH)))
                        .withVents(Optional.of(boss.vents().orElseThrow().inTurn(List.of(1, 1, 2)))));
    }

    /** Steps until the mouth is open in its {@code breach}-th breach. */
    static void untilOpen(Sortie sortie, BreachArena breach, int n) {
        for (int i = 0; i < SimStep.ticks(120); i++) {
            if (breach.breaches() == n && breach.phase() == BreachArena.Phase.OPEN) {
                return;
            }
            sortie.step(0);
        }
        throw new AssertionError("no open in breach " + n);
    }

    @Test
    void theMouthSealsOnceHurtEnoughInABreachAndOpensWithTheNextRise() {
        Sortie sortie = Act7GSpecs.sortie(BreachHazardsTest.level(echo()));
        SetPiece piece = sortie.setPiece(0);
        BreachArena breach = piece.breach().orElseThrow();
        int mouth = breach.mouth();
        untilOpen(sortie, breach, 1);
        assertFalse(piece.partShielded(mouth), "open to shots");
        piece.damagePartFlat(mouth, FLINCH - 10);
        sortie.step(0);
        assertFalse(breach.sealed(), "not yet");
        piece.damagePartFlat(mouth, 15);
        sortie.step(0);
        assertTrue(breach.sealed(), "sealed after its flinch");
        assertTrue(piece.partShielded(mouth), "a sealed mouth takes nothing (its shots spark)");
        assertEquals(BreachArena.Phase.OPEN, breach.phase(), "it stays up, open and firing");
        untilOpen(sortie, breach, 2);
        assertFalse(breach.sealed(), "open again with the next rise");
        assertFalse(piece.partShielded(mouth));
    }

    @Test
    void theVentsRiseInTurnStayWhereTheyRoseAndCountInTheBar() {
        Sortie sortie = Act7GSpecs.sortie(BreachHazardsTest.level(echo()));
        SetPiece piece = sortie.setPiece(0);
        BreachArena breach = piece.breach().orElseThrow();
        int[] vents = BreachHazardsTest.VENTS;
        untilOpen(sortie, breach, 1);
        assertEquals(Layer.GROUND, piece.partLayer(vents[0]));
        assertEquals(Layer.GROUND, piece.partLayer(vents[1]));
        assertEquals(Layer.BURIED, piece.partLayer(vents[2]), "the third with the second breach");
        assertFalse(breach.ventRaised(2));
        double x = breach.ventX(0);
        double y = breach.ventY(0);
        boolean across = breach.pose() == BreachArena.Pose.FRONT || breach.pose() == BreachArena.Pose.REAR;
        assertEquals(across ? breach.mouthX() : breach.mouthY(), across ? x : y, 1e-9, "straight into the field");
        untilOpen(sortie, breach, 2);
        assertEquals(Layer.GROUND, piece.partLayer(vents[2]));
        assertEquals(x, breach.ventX(0), 1e-9, "the first stays where it rose");
        assertEquals(y, breach.ventY(0), 1e-9);
        double full = piece.barShare();
        piece.wreckPart(vents[0]);
        assertTrue(piece.barShare() < full, "the vents count in the bar");
    }
}
