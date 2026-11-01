package vanguard.content;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * M10 part E, step E2's static checks (design/campaign Level 39, The cracking tower and the pump station; the pipes
 * going dark): a backdrop piece's {@code dark_with} names one of the level's destroy-targets groups or one of its pump
 * stations ({@code P1} … in the order of the level's {@code pump-station} placements), and its piece has the {@code
 * -glow} piece that goes dark.
 */
final class RefineryChecks {
    /** The unit whose placements {@code P1} … name. */
    static final String PUMP_STATION = "pump-station";

    private RefineryChecks() {}

    static void level(LevelData level, BiConsumer<String, String> problem) {
        List<String> groups = level.objectives().targets().orElse(List.of());
        int pumps = pumpStations(level);
        List<BackdropData.PlacedPiece> placed = level.backdrop().placed();
        for (int i = 0; i < placed.size(); i++) {
            BackdropData.PlacedPiece piece = placed.get(i);
            if (piece.darkWith().isEmpty()) {
                continue;
            }
            String name = piece.darkWith().get();
            String field = "backdrop.placed[" + i + "].dark_with";
            if (!groups.contains(name) && pump(name) > pumps) {
                problem.accept(
                        field,
                        "'" + name + "' names neither a destroy-targets group " + groups + " nor a pump station (P1-P"
                                + pumps + ")");
            }
            if (!level.backdrop().pieces().containsKey(piece.piece() + "-glow")) {
                problem.accept(field, "its piece '" + piece.piece() + "' has no '" + piece.piece() + "-glow' piece");
            }
        }
    }

    /** How many pump stations the level places. */
    static int pumpStations(LevelData level) {
        return level.groundTargets().stream()
                .filter(target -> target.enemy().filter(PUMP_STATION::equals).isPresent())
                .mapToInt(target -> target.at().size())
                .sum();
    }

    /** {@code P<n>}'s n (1 …); {@link Integer#MAX_VALUE} for any other name. */
    static int pump(String name) {
        if (name.matches("P[1-9]\\d*")) {
            return Integer.parseInt(name.substring(1));
        }
        return Integer.MAX_VALUE;
    }
}
