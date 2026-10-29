package vanguard.sim;

import java.util.List;

/**
 * A level's scroll as its script plans it, before play (split out of {@link Sortie}): the eases into an arena's halt
 * and timed halts, the direction of travel, the scroll at a level time, and what is placed by it (the ground allies'
 * stands, the waves' flyable spans in a canyon). Stateless.
 */
final class ScrollPlan {
    private ScrollPlan() {}

    /** The speed of the section before the arena: the scroll eases into an arena of speed 0 from it. */
    static double arenaFromSpeed(LevelScript script) {
        List<Section> sections = script.sections();
        for (int i = 1; i < sections.size(); i++) {
            if (sections.get(i).arena()) {
                return sections.get(i - 1).speed();
            }
        }
        return 0;
    }

    /**
     * M5 part E: the ground's scroll when it has halted in the arena: the scroll at the arena's start
     * plus the ease into the halt over {@link Sortie#ARENA_RAMP_SECONDS} ({@link #easeDistance(double)}).
     */
    static double haltScroll(LevelScript script, double fromSpeed) {
        double scroll = 0;
        List<Section> sections = script.sections();
        if (sections.stream().anyMatch(section -> section.easeSeconds() > 0)) {
            // M9 part H (step H3): a level whose sections ease into each other (Level 35's halt eased over 6 s before
            // its arena) scrolls by the eased speed, step by step as the clock does, up to the arena's start.
            int arenaStart = 0;
            for (int i = 0; i < sections.size() && !sections.get(i).arena(); i++) {
                arenaStart = SimStep.ticks(sections.get(i).end());
            }
            for (int tick = 0; tick < arenaStart; tick++) {
                scroll += timedHaltSpeed(sections, tick) * SimStep.SECONDS;
            }
            return scroll + easeDistance(fromSpeed);
        }
        for (int i = 0; i < sections.size() && !sections.get(i).arena(); i++) {
            int start = i == 0 ? 0 : SimStep.ticks(sections.get(i - 1).end());
            scroll += (SimStep.ticks(sections.get(i).end()) - start)
                    * sections.get(i).speed()
                    * SimStep.SECONDS;
        }
        return scroll + easeDistance(fromSpeed);
    }

    /**
     * M5 part E: how far the ground scrolls while it eases from {@code speed} to a halt at an arena of
     * speed 0, px: one step at {@code speed} × (n − 1 − j) / n for the arena's step j, n the ease's
     * steps (about half a second at {@code speed}: 68.8 px at 140 px/s). A level's backdrop pieces that
     * must lie under the halted boss (Level 11's Platform Tiamat) are placed with it.
     */
    static double easeDistance(double speed) {
        int n = SimStep.ticks(Sortie.ARENA_RAMP_SECONDS);
        double distance = 0;
        for (int j = 0; j < n - 1; j++) {
            distance += speed * (n - 1 - j) / n * SimStep.SECONDS;
        }
        return distance;
    }

    /** M7 part G: {@link Sortie#travelAt(LevelScript, double)} at level step {@code tick}. */
    static int travelAt(List<Section> sections, int tick) {
        boolean reverses = false;
        for (int i = 0; i < sections.size(); i++) {
            reverses |= sections.get(i).speed() < 0;
        }
        if (!reverses) {
            return 1;
        }
        for (int k = tick; k >= 0; k--) {
            double speed = timedHaltSpeed(sections, k);
            if (speed > 0) {
                return 1;
            }
            if (speed < 0) {
                return -1;
            }
        }
        return 1;
    }

    /**
     * M5 part G (design/campaign Level 13, The halt): the scroll's speed in px/s at level step {@code
     * tick} of a level without hold zones or an arena: its section's speed, 0 in a timed halt, eased
     * (smoothstep) down to 0 over the last {@link Section#rampSeconds()} of a timed halt
     * before the halt and back up to its section's speed over as long after it (the halt's ramp): step k
     * of an ease of n steps runs at the speed × (1 − s) going in and × s coming out, s = smoothstep((k +
     * 1) ÷ n), so the last step before the halt is at 0. Without a timed halt it is the section's speed.
     * A section's steps are [its start's ticks, its end's ticks). M7 part B: a moving section with an
     * {@link Section#easeSeconds()} after a moving section eases in from that section's speed
     * over its first ease (step k of n: the old speed + (new − old) × smoothstep((k + 1) ÷ n)).
     */
    static double timedHaltSpeed(List<Section> sections, int tick) {
        int start = 0;
        int last = sections.size() - 1;
        for (int i = 0; i <= last; i++) {
            Section section = sections.get(i);
            int end = SimStep.ticks(section.end());
            if (tick >= end && i < last) {
                start = end;
                continue;
            }
            if (section.timedHalt()) {
                return 0;
            }
            double speed = section.speed();
            if (i < last && sections.get(i + 1).timedHalt()) {
                int n = Math.max(1, SimStep.ticks(sections.get(i + 1).rampSeconds()));
                int k = tick - (end - n);
                if (k >= 0) {
                    speed *= 1 - smoothstep(Math.min(1, (k + 1.0) / n));
                }
            }
            if (i > 0 && sections.get(i - 1).timedHalt()) {
                int n = Math.max(1, SimStep.ticks(sections.get(i - 1).rampSeconds()));
                int k = tick - start;
                if (k < n) {
                    speed *= smoothstep((k + 1.0) / n);
                }
            } else if (i > 0
                    && section.easeSeconds() > 0
                    && !sections.get(i - 1).arena()) {
                double from = sections.get(i - 1).speed();
                int n = Math.max(1, SimStep.ticks(section.easeSeconds()));
                int k = tick - start;
                if (k < n) {
                    speed = from + (speed - from) * smoothstep((k + 1.0) / n);
                }
            }
            return speed;
        }
        return 0;
    }

    static double smoothstep(double s) {
        return s * s * (3 - 2 * s);
    }

    /**
     * M5 part G: how far the ground has scrolled when the level clock reaches {@code t} s in a level
     * without hold zones or an arena, px: the sum of {@link #timedHaltSpeed} × a step over the steps
     * before it, added up as the sortie scrolls. A defend primary's ally stands at its place at its
     * start; Level 13's backdrop places the relay's floe with it.
     */
    static double scrollAt(LevelScript script, double t) {
        if (!script.objectives().holds().isEmpty() || script.sections().stream().anyMatch(Section::arena)) {
            throw new IllegalArgumentException("the scroll of a level with holds or an arena depends on play");
        }
        double scroll = 0;
        int ticks = SimStep.ticks(t);
        for (int tick = 0; tick < ticks; tick++) {
            scroll += timedHaltSpeed(script.sections(), tick) * SimStep.SECONDS;
        }
        return scroll;
    }

    /** M7 part D: the ground allies' convoy, each unit standing so its footprint enters the top edge at its time. */
    static Convoy groundAllies(LevelScript script, ObjectiveSpec.GroundAllies allies) {
        double[] standY = new double[allies.units().size()];
        for (int k = 0; k < standY.length; k++) {
            ObjectiveSpec.GroundAlly unit = allies.units().get(k);
            standY[k] = Convoy.standY(unit, scrollAt(script, unit.enterSeconds()));
        }
        return new Convoy(allies, standY);
    }

    /**
     * M7 part C (design/campaign Level 16, Formations in a canyon; risk 1, default pending the user's
     * review): per wave, the flyable span at the top edge when the wave enters, by its time (the scroll at
     * it, {@link #scrollAt}); the channel holding the field's centre, or the widest channel there when the
     * centre is rock. Empty for every wave of a level without terrain (or with holds or an arena, whose
     * scroll depends on play), so those plan as before.
     */
    static java.util.function.Function<WaveSpec, java.util.Optional<Formations.Span>> spans(LevelScript script) {
        if (script.ground().terrain().isEmpty()
                || !script.objectives().holds().isEmpty()
                || script.sections().stream().anyMatch(Section::arena)) {
            return wave -> java.util.Optional.empty();
        }
        Terrain terrain = script.ground().terrain().get().terrain();
        return wave -> {
            double scroll = scrollAt(script, wave.t());
            double y = PlayField.HEIGHT;
            double centre = PlayField.WIDTH / 2.0;
            double left = terrain.spanLeft(scroll, y, centre);
            double right = terrain.spanRight(scroll, y, centre);
            if (wave.branch().isPresent()) {
                // M9 part E: a fork's branch's wave flies its own channel: the widest span on its side of the island.
                java.util.List<Integer> forks = script.ground().terrain().get().forks();
                double middle =
                        terrain.islandMiddle(forks.get(wave.branch().get().fork()));
                boolean leftSide = wave.branch().get().side() == Branch.LEFT;
                left = Double.NaN;
                right = Double.NaN;
                double best = 0;
                for (double x = 5; x < PlayField.WIDTH; x += 10) {
                    double from = terrain.spanLeft(scroll, y, x);
                    double to = terrain.spanRight(scroll, y, x);
                    if (!Double.isNaN(from) && (from + to) / 2 < middle == leftSide && to - from > best) {
                        best = to - from;
                        left = from;
                        right = to;
                    }
                }
            } else if (Double.isNaN(left)) {
                double best = 0;
                for (double x = 5; x < PlayField.WIDTH; x += 10) {
                    double from = terrain.spanLeft(scroll, y, x);
                    double to = terrain.spanRight(scroll, y, x);
                    if (!Double.isNaN(from) && to - from > best) {
                        best = to - from;
                        left = from;
                        right = to;
                    }
                }
            }
            return Double.isNaN(left)
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(new Formations.Span(left, right));
        };
    }
}
