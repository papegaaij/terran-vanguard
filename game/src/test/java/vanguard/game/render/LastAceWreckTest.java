package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part F, step F6g (design/enemies/bosses/honour-guard, Death; F5's capture: after the last kill the last ace's wreck
 * was not drawn, the piece being destroyed and no longer present, and its chain followed the ace's flight at the kill out
 * of the field in about 0.6 s): the last ace's wreck tumbles on the field for at least the 2 s of its chain, drawn though
 * the piece is gone, and the chain's bursts lie round it, spread over the 2 s.
 */
class LastAceWreckTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    /** Flown to the kill: the wreck shows on the field from the kill for at least 2 s; the chain's bursts lie round it. */
    @Test
    void theLastAcesWreckTumblesUnderItsChainForTwoSeconds() throws Exception {
        Sortie sortie = Level40Fixture.sortie();
        SetPiece piece = guard(sortie);
        LastAceWreck last = new LastAceWreck();
        long kill = -1;
        for (int step = 0; step < SimStep.ticks(320) && kill < 0; step++) {
            sortie.step(Level40Fixture.steer(sortie) | Command.FIRE.bit());
            if (last.stepped(sortie, piece)) {
                kill = sortie.tick();
            }
        }
        assertTrue(kill > 0, "the duel was won");
        assertTrue(last.ace() >= 0, "the last ace");
        assertFalse(piece.present(), "the piece is gone at the kill");
        assertTrue(HonourGuardLooks.drawsAces(piece), "its aces are drawn on after the kill");
        double x0 = last.x0();
        double y0 = last.y0();
        assertTrue(PlayField.WIDTH > x0 && x0 > 0 && PlayField.HEIGHT > y0 && y0 > 0, "it died on the field");
        int shown = 0;
        for (int t = 0; t < SimStep.ticks(3); t++) {
            long now = kill + t;
            if (!last.shows(now, 0)) {
                continue;
            }
            shown++;
            double x = last.x(now, 0);
            double y = last.y(now, 0);
            assertTrue(x >= Math.min(x0, LastAceWreck.MARGIN) - 1e-9, "x " + x);
            assertTrue(x <= Math.max(x0, PlayField.WIDTH - LastAceWreck.MARGIN) + 1e-9, "x " + x);
            assertTrue(y >= Math.min(y0, LastAceWreck.MARGIN) - 1e-9 && y <= PlayField.HEIGHT, "y " + y);
            if (t > 0) {
                assertTrue(y <= last.y(now - 1, 0), "it falls");
            }
        }
        assertTrue(shown >= SimStep.ticks(2), "the wreck shows for at least 2 s after the kill: " + shown + " steps");
        assertFalse(last.shows(kill + SimStep.ticks(2.5), 0), "the chain's last burst takes it");

        // The chain (the pivots' bursts, as the level screen starts them): over 2 s, each round the hull at its time.
        JsonValue chain = new JsonReader()
                .parse(Files.readString(ASSETS.resolve("pivots/honour-guard.json")))
                .get("chain");
        double first = Double.POSITIVE_INFINITY;
        double end = 0;
        double before = 0;
        for (int i = 0; i < chain.size; i++) {
            JsonValue burst = chain.get(i);
            double t = burst.getDouble("t");
            assertTrue(t - before <= 0.3, "no gap over 0.3 s in the chain at " + t);
            before = t;
            first = Math.min(first, t);
            end = Math.max(end, t);
            double bx = LastAceWreck.x(x0, y0, t) + burst.getDouble("dx");
            double by = LastAceWreck.y(x0, y0, t) - burst.getDouble("dy");
            assertTrue(LastAceWreck.shows(t), "the hull is there at the burst at " + t);
            double hx = last.x(kill + SimStep.ticks(t), 0);
            double hy = last.y(kill + SimStep.ticks(t), 0);
            assertTrue(Math.hypot(bx - hx, by - hy) <= 40, "burst " + i + " round the hull");
            // On the field, or as near its edge as the hull (an ace killed at a side edge stays there).
            double reach = 24;
            assertTrue(bx > Math.min(0, hx) - reach && bx < Math.max(PlayField.WIDTH, hx) + reach, "burst " + i + " x");
            assertTrue(by > Math.min(0, hy) - reach && by < PlayField.HEIGHT + reach, "burst " + i + " y");
        }
        assertEquals(0, first, 1e-9);
        assertEquals(LastAceWreck.CHAIN_SECONDS, end, 1e-9, "the chain spreads over 2 s");
        assertTrue(chain.get(chain.size - 1).getString("size").equals("large"), "it ends large");
    }

    /** At the simulation's tumble speed where there is room; slowed near the bottom or a side, never out before 2.1 s. */
    @Test
    void itTumblesAsTheOthersUnlessAnEdgeIsNear() {
        assertEquals(1, LastAceWreck.pace(220, 450), 1e-9, "room: the others' tumble");
        assertEquals(220 - 80 * 2, LastAceWreck.x(220, 450, 2), 1e-9, "to the nearer side");
        assertEquals(450 - 160 * 2, LastAceWreck.y(220, 450, 2), 1e-9, "down");
        assertEquals(260 + 80, LastAceWreck.x(260, 450, 1), 1e-9, "the right half to the right");
        for (double[] at : new double[][] {{240, 120}, {60, 300}, {440, 80}, {20, 20}, {240, 36}}) {
            double end = LastAceWreck.SHOWN_SECONDS;
            double x = LastAceWreck.x(at[0], at[1], end);
            double y = LastAceWreck.y(at[0], at[1], end);
            assertTrue(LastAceWreck.pace(at[0], at[1]) < 1, "slowed at " + at[0] + "," + at[1]);
            assertTrue(y >= Math.min(at[1], LastAceWreck.MARGIN) - 1e-9, "on the field at its end: y " + y);
            assertTrue(x >= Math.min(at[0], LastAceWreck.MARGIN) - 1e-9, "x " + x);
            assertTrue(x <= Math.max(at[0], PlayField.WIDTH - LastAceWreck.MARGIN) + 1e-9, "x " + x);
        }
    }

    private static SetPiece guard(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k).squadron().isPresent()) {
                return sortie.setPiece(k);
            }
        }
        throw new AssertionError("no squadron");
    }
}
