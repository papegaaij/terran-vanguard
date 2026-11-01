package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1b (design/enemies/bosses/honour-guard, The escort): Rook's goal steps out of a squadron's told
 * sniper line like the ship and keeps {@link AcePlaybook#DIVE_KEEP_OFF} px off a told or running dive's line ({@link
 * RookHazards}); a goal clear of both stays.
 */
class SquadronRookTest {
    private static final Hitbox ROOK = new Hitbox(30, 26);
    private static final double LIMIT = 20;
    private static final double HALF = 15;

    /** The goal's distance from the line from (ox, oy) through (tx, ty). */
    private static double off(double gx, double gy, double ox, double oy, double tx, double ty) {
        double d = Math.hypot(tx - ox, ty - oy);
        return Math.abs((gx - ox) * (ty - oy) - (gy - oy) * (tx - ox)) / d;
    }

    @Test
    void rookStepsOutOfATelegraphingSniperLine() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 3);
        BossLines lines = rig.piece.lines().orElseThrow();
        for (int i = 0; i < SimStep.ticks(40) && !lines.telegraphing(); i++) {
            rig.step();
        }
        assertTrue(lines.telegraphing(), "a sniper line telegraphs");
        RookHazards hazards = new RookHazards(ROOK, LIMIT);
        double gx = lines.fromX() + lines.dirX() * 150;
        double gy = lines.fromY() + lines.dirY() * 150;
        hazards.at(gx, gy);
        assertTrue(hazards.clear(gx, gy, new SetPiece[] {rig.piece}, null), "on the line: he moves");
        double tx = lines.fromX() + lines.dirX();
        double ty = lines.fromY() + lines.dirY();
        assertTrue(
                off(hazards.x(), hazards.y(), lines.fromX(), lines.fromY(), tx, ty)
                        >= lines.bandPx() / 2 + HALF + RookHazards.GAP,
                "his hull's edge outside the band");
        double fx = Math.clamp(lines.fromX() - lines.dirY() * 200, LIMIT, PlayField.WIDTH - LIMIT);
        double fy = Math.clamp(lines.fromY() + lines.dirX() * 200, LIMIT, PlayField.HEIGHT - LIMIT);
        if (off(fx, fy, lines.fromX(), lines.fromY(), tx, ty) > 60) {
            hazards.at(fx, fy);
            assertFalse(hazards.clear(fx, fy, new SetPiece[] {rig.piece}, null), "off the line: he stays");
        }
    }

    @Test
    void rookKeepsFortyPixelsOffAToldDiveAndItsRun() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 2);
        AcePlaybook book = rig.squadron.playbook().orElseThrow();
        int k = Act6FSpecs.KESTREL;
        for (int i = 0; i < SimStep.ticks(120) && !book.diveTold(k); i++) {
            rig.step();
        }
        assertTrue(book.diveTold(k), "a told dive");
        RookHazards hazards = new RookHazards(ROOK, LIMIT);
        boolean ran = false;
        boolean checked = false;
        while (book.diveTold(k)) {
            double ox = book.diveFromX(k);
            double oy = book.diveFromY(k);
            double tx = book.diveToX(k);
            double ty = book.diveToY(k);
            double gx = (ox + tx) / 2;
            double gy = (oy + ty) / 2;
            if (gx > LIMIT + 60 && gx < PlayField.WIDTH - LIMIT - 60 && Math.hypot(tx - ox, ty - oy) > 20) {
                hazards.at(gx, gy);
                assertTrue(hazards.clear(gx, gy, new SetPiece[] {rig.piece}, null), "on the dive's line: he moves");
                assertTrue(
                        off(hazards.x(), hazards.y(), ox, oy, tx, ty) >= AcePlaybook.DIVE_KEEP_OFF + HALF,
                        "40 px off it");
                checked = true;
            }
            ran |= rig.squadron.leg(k) == AceLeg.RUN;
            rig.step();
        }
        assertTrue(checked, "the line was checked");
        assertTrue(ran, "through its run");
    }
}
