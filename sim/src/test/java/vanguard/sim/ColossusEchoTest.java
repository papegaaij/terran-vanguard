package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/enemies/bosses/dust-colossus, Echo; m11g-gaps.md §1 #6): the Dust Colossus echo's keys
 * in play on the test colossus: its points by the seeded draw (the eruption's marker among them, never first) and its
 * vents staying up through the dive and the buried stretch, firing their burst there, a dead vent staying dead. The
 * Level 21 colossus (its orders, its vents) flies as recorded (the golden flights).
 */
class ColossusEchoTest {
    static SetPieceSpec echo() {
        List<Integer> fire = List.of(BreachHazardsTest.RING, BreachHazardsTest.BURST);
        SetPieceSpec piece = BreachHazardsTest.colossus(fire, fire, fire);
        BossSpec boss = piece.boss().orElseThrow();
        BossSpec echo = boss.withBreach(
                        Optional.of(boss.breach().orElseThrow().withDraw(Optional.of(BreachDrawTest.SPEC))))
                .withVents(Optional.of(boss.vents().orElseThrow().stayingUp()));
        return new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                Optional.of(echo));
    }

    static Sortie sortie() {
        return Act7GSpecs.sortie(BreachHazardsTest.level(echo()));
    }

    @Test
    void itsPointsComeByTheDrawAndTheEruptionsMarkerAmongThem() {
        Sortie sortie = sortie();
        BreachArena breach = sortie.setPiece(0).breach().orElseThrow();
        List<BreachArena.Pose> poses = new ArrayList<>();
        int markers = 0;
        for (int i = 0; i < SimStep.ticks(150); i++) {
            sortie.step(0);
            for (int e = 0; e < sortie.events().size(); e++) {
                SimEvents.Type type = sortie.events().type(e);
                if (type == SimEvents.Type.BREACH_TELL || type == SimEvents.Type.ERUPTION_MARKER) {
                    poses.add(breach.pose());
                    markers += type == SimEvents.Type.ERUPTION_MARKER ? 1 : 0;
                }
            }
        }
        assertTrue(poses.size() >= 8, "breaches: " + poses);
        assertNotEquals(BreachArena.Pose.UNDER, poses.getFirst());
        for (int i = 1; i < poses.size(); i++) {
            assertNotEquals(poses.get(i - 1), poses.get(i), "never twice running: " + poses);
        }
        assertTrue(markers > 0, "the eruption's marker comes: " + poses);
        assertEquals(
                markers, poses.stream().filter(p -> p == BreachArena.Pose.UNDER).count());
    }

    @Test
    void itsVentsStayUpThroughTheDiveAndFireThere() {
        Sortie sortie = sortie();
        SetPiece piece = sortie.setPiece(0);
        BreachArena breach = piece.breach().orElseThrow();
        int vent = BreachHazardsTest.VENTS[0];
        boolean upWhileBuried = false;
        boolean firedWhileBuried = false;
        for (int i = 0; i < SimStep.ticks(60); i++) {
            sortie.step(0);
            if (breach.phase() == BreachArena.Phase.BURIED && breach.breaches() > 0) {
                upWhileBuried |= piece.partLayer(vent) == Layer.GROUND;
                firedWhileBuried |= breach.ventsFiring();
            }
        }
        assertTrue(upWhileBuried, "a vent stays up while the mouth is under");
        assertTrue(firedWhileBuried, "and fires there");
        piece.wreckPart(vent);
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(0);
        }
        assertEquals(Layer.BURIED, piece.partLayer(vent), "a dead vent goes down and stays dead");
        assertTrue(piece.partHp(vent) <= 0);
    }
}
