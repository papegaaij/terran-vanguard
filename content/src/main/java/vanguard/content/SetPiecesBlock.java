package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Optional;

/**
 * The level's {@code boss} and {@code set_pieces}: where the boss arrives, and the huge set-piece units' passes.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.BossPlacement}).
 */
public sealed interface SetPiecesBlock permits LevelData {
    /**
     * The level's boss ({@code enemy}, a stat block with a {@code boss} script): it arrives at
     * {@code t} level seconds in section {@code section}, its centre at {@code x} px from the left. Round 41 (Level
     * 14's Siege Spire, the user's playtest 2026-10-19: "show the warning box before you reach it"): {@code incoming}
     * s before {@code t} the boss warning (the box, the klaxon and the warning track) opens instead of at the arrival;
     * left out, at the arrival.
     */
    public record BossPlacement(String enemy, double t, double x, int section, Optional<Double> incoming) {
        public BossPlacement {
            Check.notNegative("t", t);
            Check.positive("section", section);
            incoming.ifPresent(lead -> Check.that(lead > 0 && lead < t, "incoming: s before t, more than 0"));
        }

        /** Without round 41's incoming: the warning at the arrival. */
        public BossPlacement(String enemy, double t, double x, int section) {
            this(enemy, t, x, section, Optional.empty());
        }
    }

    /** A huge set-piece unit ({@code enemy}, a multi-part stat block) flying its {@code passes} in order. */
    public record SetPieceData(String enemy, List<PassData> passes) {
        public SetPieceData {
            Check.notEmpty("passes", passes);
        }
    }

    /**
     * A pass of a set piece: its centre flies along {@code path} on {@code layer} facing
     * {@code heading} (degrees from straight down, positive to the right; fixed for the pass). A
     * pass on the player's layer arrives on {@code high-air}, {@code descend}s, holds until
     * {@code hold} seconds after its descent began and then rises and leaves straight up through
     * the top edge at {@code leave_speed}; a pass without a descent ends with its path.
     *
     * @param section the section it belongs to (the README's waves table)
     * @param path one {@code [t, x, y]} per waypoint: level seconds, px from the left, px below the top edge
     */
    public record PassData(
            String name,
            int section,
            String layer,
            double heading,
            List<PathPoint> path,
            Optional<Descent> descend,
            Optional<Double> hold,
            Optional<Double> leaveSpeed,
            Optional<PassChange> easy,
            Optional<PassChange> hard) {
        public PassData {
            Layers.of(layer);
            Check.notEmpty("path", path);
            for (int i = 1; i < path.size(); i++) {
                Check.that(path.get(i).t() > path.get(i - 1).t(), "path[" + i + "]: waypoints are in time order");
            }
            Check.that(
                    descend.isPresent() == hold.isPresent() && hold.isPresent() == leaveSpeed.isPresent(),
                    "a descending pass gives descend, hold and leave_speed, a flight none of them");
            hold.ifPresent(h -> Check.that(h > descend.orElseThrow().seconds(), "hold: longer than the descent"));
            leaveSpeed.ifPresent(v -> Check.positive("leave_speed", v));
        }

        /** Its hold on {@code difficulty}, from the start of the descent. */
        public Optional<Double> holdOn(Difficulty difficulty) {
            Optional<PassChange> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case MEDIUM -> Optional.empty();
                        case HARD -> hard;
                    };
            return change.flatMap(PassChange::hold).or(() -> hold);
        }
    }

    /** A waypoint: level seconds, px from the left, px below the top edge. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record PathPoint(double t, double x, double y) {}

    /** The descent from {@code high-air} to the player's layer: starts {@code at} level seconds, takes {@code seconds}. */
    public record Descent(double at, double seconds) {
        public Descent {
            Check.positive("seconds", seconds);
        }
    }

    /** A difficulty's change to a pass: another hold. */
    public record PassChange(Optional<Double> hold) {}
}
