package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Road;
import vanguard.sim.SimStep;

/**
 * An escort objective's checks: its convoy's ally follows the road, fits the screen and is hooked by enemies of the
 * level.
 */
final class EscortChecks extends ContentChecks {
    EscortChecks(Content content, Problems problems) {
        super(content, problems);
    }

    /**
     * An escort objective's convoy (design/allies; Level 04): its ally exists and follows the
     * level's road, its column fits the screen without units overlapping, its hooked enemies fly in
     * the level, and the ally events and level-end ranges are used only with a convoy. The road keeps
     * its ribbon inside the play field and bends no further than the ally's rendered headings.
     */
    void checkEscort(LevelData level, Set<String> levelEnemies) {
        Optional<LevelData.Escort> escort = level.objectives().escort();
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            String field = "radio[" + i + "]";
            boolean allyEvent = cue.event()
                    .map(event -> event == LevelData.CueEvent.FIRST_ALLY_HIT
                            || event == LevelData.CueEvent.FIRST_ALLY_LOST
                            || event == LevelData.CueEvent.ALLY_LOST
                            || event == LevelData.CueEvent.ALLY_HIT)
                    .orElse(false);
            // M7 part D: the ally events name a ground ally of the level's allies block too (Level 17's domes).
            if ((allyEvent
                            && escort.isEmpty()
                            && level.convoy().isEmpty()
                            && level.allies().isEmpty())
                    || (cue.allies().isPresent()
                            && escort.isEmpty()
                            && level.convoy().isEmpty())) {
                problem(
                        level,
                        field,
                        "convoy events and allies ranges need an escort objective or a naval convoy (the ally events: or"
                                + " ground allies)");
            }
            // M5 part G: a defend primary fails at its ally's loss.
            boolean canFail = escort.isPresent()
                    || level.objectives().targets().isPresent()
                    || level.objectives().defend().isPresent();
            if (cue.event().orElse(null) == LevelData.CueEvent.MISSION_FAILED && !canFail) {
                problem(
                        level,
                        field,
                        "a mission-failed line needs a primary objective that can fail (escort, destroy-targets,"
                                + " defend)");
            }
            // M5 part D: a level-end cue counts the saveable units home (all but a scripted loss's);
            // M5 part E: a naval convoy's, its units that can be damaged afloat.
            int units = escort.map(LevelData.Escort::saveable)
                    .orElseGet(() -> level.convoy().map(this::damageable).orElse(0));
            String kind = escort.flatMap(LevelData.Escort::scriptedLoss).isPresent() ? " saveable units" : " units";
            cue.allies().ifPresent(range -> {
                if (range.max() > units) {
                    problem(level, field + ".allies", "the convoy has " + units + kind);
                }
            });
        }
        if (level.radio().stream()
                        .filter(cue -> cue.event().orElse(null) == LevelData.CueEvent.MISSION_FAILED)
                        .count()
                > 1) {
            problem(level, "radio", "at most one mission-failed line");
        }
        level.road().ifPresent(road -> checkRoad(level, road, escort));
        if (escort.isEmpty()) {
            return;
        }
        LevelData.Escort convoy = escort.get();
        AlliesData.Ally ally = content.allies().allies().get(convoy.ally());
        if (ally == null) {
            problem(level, "objectives.escort.ally", "unknown ally '" + convoy.ally() + "'");
            return;
        }
        if (ally.naval()) {
            // M5 part E: a naval ally sails in a level's convoy block, outside the objectives.
            problem(level, "objectives.escort.ally", "a " + convoy.ally() + " follows stations: give it in convoy");
            return;
        }
        if (ally.air() != convoy.air()) {
            problem(
                    level,
                    "objectives.escort",
                    ally.air()
                            ? "a " + convoy.ally() + " follows lanes: give its stations, not y"
                            : "a " + convoy.ally() + " follows the road: give its y, not stations");
            return;
        }
        if (convoy.air()) {
            checkAirEscort(level, convoy, ally);
            return;
        }
        if (level.road().isEmpty()) {
            problem(level, "objectives.escort", "a " + convoy.ally() + " follows the level's road: give road");
        }
        double length = ally.size().height();
        List<Double> heights = convoy.y().orElseThrow();
        for (int k = 0; k < heights.size(); k++) {
            double y = heights.get(k);
            if (y - length / 2 < 0 || y + length / 2 > PlayField.HEIGHT) {
                problem(level, "objectives.escort.y[" + k + "]", "y=" + y + " puts the unit off the screen");
            }
            if (k > 0 && y - heights.get(k - 1) < length) {
                problem(level, "objectives.escort.y[" + k + "]", "units overlap: less than " + length + " px apart");
            }
        }
        checkTime(
                level, "objectives.escort.enter.t", convoy.enter().orElseThrow().t());
        for (String slug : convoy.hook().orElseThrow().enemies()) {
            checkEnemyName(level, "objectives.escort.hook.enemies", slug);
            if (content.enemies().containsKey(slug) && !levelEnemies.contains(slug)) {
                problem(level, "objectives.escort.hook.enemies", "no '" + slug + "' in this level");
            }
        }
    }

    /**
     * M5 part D (design/allies, evacuation shuttle; Level 10): an air escort. Sampled every step of the
     * level, each unit's hit box stays inside the play field at its station with the sway and never
     * overlaps another's; the pads lie on the screen; the liftoff ends before the scripted loss's glow
     * starts and the climb-out starts after the loss, both inside the level; the level has no road.
     */
    private void checkAirEscort(LevelData level, LevelData.Escort convoy, AlliesData.Ally ally) {
        String field = "objectives.escort";
        if (level.road().isPresent()) {
            problem(level, "road", "an air escort follows no road");
        }
        List<ObjectiveSpec.Station> stations = convoy.stations().orElseThrow().stream()
                .map(PartDRules::station)
                .toList();
        double w = ally.hitbox().width();
        double h = ally.hitbox().height();
        int steps = SimStep.ticks(level.seconds());
        boolean[] reported = new boolean[stations.size()];
        boolean overlapReported = false;
        for (int tick = 0; tick <= steps; tick++) {
            double t = tick * SimStep.SECONDS;
            for (int k = 0; k < stations.size(); k++) {
                double x = stations.get(k).xAt(t);
                double y = stations.get(k).yAt(t);
                if (!reported[k]
                        && (x - w / 2 < 0
                                || x + w / 2 > PlayField.WIDTH
                                || y - h / 2 < 0
                                || y + h / 2 > PlayField.HEIGHT)) {
                    reported[k] = true;
                    problem(
                            level,
                            field + ".stations[" + k + "]",
                            "its hit box leaves the play field at t=" + round(t));
                }
                for (int j = 0; j < k && !overlapReported; j++) {
                    if (Math.abs(stations.get(j).xAt(t) - x) < w
                            && Math.abs(stations.get(j).yAt(t) - y) < h) {
                        overlapReported = true;
                        problem(
                                level,
                                field + ".stations[" + k + "]",
                                "its hit box overlaps station " + j + "'s at t=" + round(t));
                    }
                }
            }
        }
        double liftEnd = 0;
        if (convoy.liftoff().isPresent()) {
            LevelData.Liftoff liftoff = convoy.liftoff().get();
            liftEnd = liftoff.t() + liftoff.seconds();
            checkTime(level, field + ".liftoff.t", liftEnd);
            for (int k = 0; k < liftoff.pads().size(); k++) {
                Point pad = liftoff.pads().get(k);
                if (pad.x() < 0 || pad.x() > PlayField.WIDTH || pad.y() < 0 || pad.y() > PlayField.HEIGHT) {
                    problem(level, field + ".liftoff.pads[" + k + "]", "the pad is off the screen");
                }
            }
        }
        double lossAt = liftEnd;
        if (convoy.scriptedLoss().isPresent()) {
            LevelData.ScriptedLoss loss = convoy.scriptedLoss().get();
            checkTime(level, field + ".scripted_loss.t", loss.t());
            if (loss.t() - loss.glow() < liftEnd) {
                problem(
                        level,
                        field + ".scripted_loss",
                        "its glow starts before the liftoff ends at " + liftEnd + " s");
            }
            lossAt = loss.t();
        }
        if (convoy.climb().isPresent()) {
            LevelData.Climb climb = convoy.climb().get();
            checkTime(level, field + ".climb.t", climb.t() + climb.seconds());
            if (climb.t() <= lossAt) {
                problem(level, field + ".climb.t", "the climb-out starts after the liftoff and the scripted loss");
            }
        }
    }

    private static double round(double seconds) {
        return Math.round(seconds * 100) / 100.0;
    }

    /** The road's ribbon stays inside the play field and bends no further than the convoy's headings. */
    private void checkRoad(LevelData level, LevelData.Road data, Optional<LevelData.Escort> escort) {
        Road road = SimSpecs.road(level, data);
        double most = escort.map(e -> content.allies().allies().get(e.ally()))
                .map(ally -> (ally.headings().count() - 1) / 2 * ally.headings().step())
                .orElse(90.0);
        double half = data.width() / 2;
        for (double along = road.start(); along <= road.end(); along += 1) {
            double x = road.x(along);
            if (x - half < 0 || x + half > PlayField.WIDTH) {
                problem(level, "road.points", "the ribbon leaves the play field at x=" + Math.round(x));
                return;
            }
            if (Math.abs(road.headingDegrees(along)) > most + 1e-6) {
                problem(
                        level,
                        "road.points",
                        "bends " + Math.round(Math.abs(road.headingDegrees(along))) + "° from straight up, more than "
                                + most + "°");
                return;
            }
        }
    }
}
