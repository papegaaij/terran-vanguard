package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.content.campaign.Intel;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.WarningEdge;

/**
 * M7 part G (design/campaign Level 20, Reverse scroll; step GA): the looks that read the travel. The edge warnings'
 * labels, the Dust Kite's fade for a kite from the bottom edge, the intel strip's turn and lull, and the strip
 * backdrop's zones meeting the seam pieces placed by row. Level 20 is not mapped to a script yet (step G4), so these
 * read its data, not a flight.
 */
class ReverseScrollLooksTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(20).orElseThrow();

    @Test
    void theWarningsAreNamedByTravel() {
        assertEquals("! REAR", EdgeWarnings.label(WarningEdge.BOTTOM, false));
        assertEquals("! AHEAD", EdgeWarnings.label(WarningEdge.BOTTOM, true), "the lessons from ahead");
        assertEquals("! REAR", EdgeWarnings.label(WarningEdge.TOP, true), "behind, by travel");
        assertEquals("! REAR", EdgeWarnings.label(WarningEdge.TOP, false), "the turnabout's warning before the turn");
        assertEquals("! LEFT", EdgeWarnings.label(WarningEdge.LEFT, true));
        assertEquals("! RIGHT", EdgeWarnings.label(WarningEdge.RIGHT, true));
    }

    @Test
    void aKiteFromTheBottomEdgeFadesInAsItShows() {
        assertFalse(KiteFade.shows(-30), "below the bottom edge, not yet on the screen");
        assertTrue(KiteFade.shows(-5));
        assertTrue(KiteFade.shows(PlayField.HEIGHT / 2.0));
        assertTrue(KiteFade.shows(PlayField.HEIGHT + 5));
        assertFalse(KiteFade.shows(PlayField.HEIGHT + 30), "above the top edge");
    }

    /** "The wave strip marking the turn and the lull" (the threat profile at sensor L3). */
    @Test
    void theIntelStripMarksTheTurnAndTheLull() {
        LevelData level = CONTENT.level(KEY);
        List<Intel.Mark> marks = Intel.of(CONTENT, KEY, 3).marks();
        List<Intel.Mark> turns = marks.stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.TURN)
                .toList();
        assertEquals(1, turns.size(), String.valueOf(marks));
        double turn = turns.getFirst().from();
        assertTrue(turn > 21.5 && turn < 23, "the scroll through zero at about 22.1 s: " + turn);
        assertEquals(1, level.travelAt(turn - 0.2));
        assertEquals(-1, level.travelAt(turn + 0.2));
        assertEquals(
                List.of(new Intel.Mark(108, 124, Intel.Mark.Kind.LULL)),
                marks.stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.LULL)
                        .toList(),
                "the upper terrace, not the last section over the rim");
        assertTrue(
                Intel.of(CONTENT, CONTENT.levelKey(19).orElseThrow(), 3).marks().stream()
                        .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.TURN || mark.kind() == Intel.Mark.Kind.LULL),
                "a level without a reversed section marks neither");
    }

    /**
     * The strip's zones change their tile sets where the seam pieces placed by row are centred (ART-G1's exact rows):
     * the backdrop's rows for the tiles and the pieces' rows ({@link LevelData#pieceCentre}) agree.
     */
    @Test
    void theStripsZonesMeetTheirSeamPieces() {
        LevelData level = CONTENT.level(KEY);
        BackdropData backdrop = level.backdrop();
        BackdropData.Strip strip = backdrop.strip().orElseThrow();
        double factor = backdrop.factor(BackdropLayer.GROUND);
        for (int z = 1; z < strip.zones().size(); z++) {
            double from = strip.zones().get(z).from();
            long seam = Backdrop.stripSeam(strip, z, factor);
            List<BackdropData.PlacedPiece> centred = backdrop.placements().stream()
                    .filter(placed -> placed.byRow() && placed.row().orElseThrow() == from)
                    .toList();
            assertEquals(1, centred.size(), "a seam piece on the zone boundary at strip row " + from);
            assertEquals(seam, Math.round(level.pieceCentre(centred.getFirst())), "zone " + z);
        }
    }

    /**
     * Over the whole level, the outro included, the screen's rows lie inside the strip's zones in both directions, and
     * the strip row at the bottom edge falls from the origin to about row 0 at the end (the strip runs both ways).
     */
    @Test
    void theStripCoversTheScreenBothWays() {
        LevelData level = CONTENT.level(KEY);
        BackdropData.Strip strip = level.backdrop().strip().orElseThrow();
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        for (int step = 0; step <= SimStep.ticks(level.outroEnd()); step++) {
            double row = strip.row(level.scrollAt(step * SimStep.SECONDS));
            low = Math.min(low, row);
            high = Math.max(high, row + PlayField.HEIGHT);
        }
        assertTrue(strip.zones().getFirst().from() <= low, "below: " + low);
        assertTrue(strip.zones().getLast().to() >= high, "above: " + high);
        assertTrue(high > strip.origin() + 2_000, "the strike flies up the strip first");
        assertEquals(0, strip.row(level.scrollAt(level.seconds())), 40, "row 0 at the bottom edge at the end");
    }
}
