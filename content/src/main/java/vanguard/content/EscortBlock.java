package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.content.AlliesBlock.HookMode;
import vanguard.sim.PlayField;

/**
 * The {@code escort} primary's convoy (on the {@code road} or in the air) and the naval {@code convoy} outside the
 * objectives.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Escort}).
 */
public sealed interface EscortBlock permits LevelData {
    /**
     * The {@code escort} primary objective (design/allies; Level 04): a convoy of {@code ally}
     * units, one per height in {@code y} (centres, px below the top edge, the leading unit first),
     * rolling in from the bottom edge as {@code enter} says and following the level's road. Each
     * unit alive at the end pays {@code credits}; the objective fails when every unit is lost.
     *
     * <p>M5 part D (Level 10, user decisions D1, D4 and D5 = a): an air escort (an ally that {@code
     * follows: lanes}) gives {@code stations} instead of {@code y}, an optional {@code liftoff}
     * instead of {@code enter} and no {@code hook}; an optional {@code climb} (the climb-out) and
     * {@code scripted_loss}. Then each <em>saveable</em> unit home (every unit but the scripted
     * loss's) pays {@code credits}, and the objective fails at once when every saveable unit is lost.
     *
     * @param y a ground convoy's heights
     * @param enter a ground convoy's roll-in
     * @param hook the target-the-objective hook: which enemies' aimed attacks go for the convoy (a
     *     ground convoy's; required there)
     * @param easy the units' HP on easy
     * @param hard the units' HP on hard
     * @param stations M5 part D: an air escort's stations, the leading unit first
     * @param liftoff M5 part D: an air escort's liftoff from its pads
     * @param climb M5 part D: an air escort's climb-out
     * @param scriptedLoss M5 part D: the unit lost in a scripted event (Level 10's Lifeline Three)
     * @param hull M9 part D (design/allies/mining-tug; Level 31): a hull escort (an ally that {@code follows: hull}) gives
     *     its {@code hull} instead of {@code y} or {@code stations}; each section home pays {@code credits}
     */
    public record Escort(
            String ally,
            Optional<List<Double>> y,
            int credits,
            Optional<Enter> enter,
            Optional<Hook> hook,
            Optional<AllyChange> easy,
            Optional<AllyChange> hard,
            Optional<List<Station>> stations,
            Optional<Liftoff> liftoff,
            Optional<Climb> climb,
            Optional<ScriptedLoss> scriptedLoss,
            Optional<Hull> hull) {
        public Escort {
            Check.notNegative("credits", credits);
            Check.that(
                    (y.isPresent() ? 1 : 0) + (stations.isPresent() ? 1 : 0) + (hull.isPresent() ? 1 : 0) == 1,
                    "give the convoy's y (on the road), its stations (in the air) or (M9 part D) its hull");
            if (hull.isPresent()) {
                Check.that(
                        enter.isEmpty()
                                && hook.isEmpty()
                                && easy.isEmpty()
                                && hard.isEmpty()
                                && liftoff.isEmpty()
                                && climb.isEmpty()
                                && scriptedLoss.isEmpty(),
                        "a hull escort gives its entry, climb-out, HP and hook in its hull block (no enter, hook, easy,"
                                + " hard, liftoff, climb or scripted_loss)");
            }
            y = y.map(List::copyOf);
            y.ifPresent(heights -> Check.notEmpty("y", heights));
            stations = stations.map(List::copyOf);
            stations.ifPresent(list -> Check.notEmpty("stations", list));
            if (y.isPresent()) {
                Check.that(enter.isPresent(), "a convoy on the road gives its enter");
                Check.that(hook.isPresent(), "a convoy on the road gives its hook");
                Check.that(
                        liftoff.isEmpty() && climb.isEmpty() && scriptedLoss.isEmpty(),
                        "liftoff, climb and scripted_loss belong to an air escort (stations)");
            } else if (stations.isPresent()) {
                Check.that(enter.isEmpty(), "an air escort lifts off (liftoff) instead of rolling in (enter)");
                Check.that(hook.isEmpty(), "an air escort has no hook: every enemy bullet and contact hurts it");
                int units = stations.orElseThrow().size();
                liftoff.ifPresent(l -> Check.that(
                        l.pads().size() == units,
                        "liftoff.pads: one per station, " + units + ", not "
                                + l.pads().size()));
                scriptedLoss.ifPresent(loss -> Check.that(
                        loss.unit() >= 1 && loss.unit() <= units,
                        "scripted_loss.unit: one of the units, 1 to " + units + ", was " + loss.unit()));
            }
        }

        /** The number of units (M9 part D: a hull's sections). */
        public int units() {
            return y.map(List::size).orElseGet(() -> stations.map(List::size)
                    .orElseGet(() -> hull.orElseThrow().hp().size()));
        }

        /** Whether it is an air escort (stations in the band). */
        public boolean air() {
            return stations.isPresent();
        }

        /** The units the player can save: every unit but the scripted loss's. */
        public int saveable() {
            return units() - (scriptedLoss.isPresent() ? 1 : 0);
        }
    }

    /**
     * M5 part D: an air escort unit's station: {@code at} ({@code [x, y]}, px from the play field's
     * left edge, px below the top edge) with the lane sway {@code sway} ({@code [ax, ay]} px): the
     * unit flies at {@code at} + (ax sin θ, ay sin 2θ), θ = 2π (t ÷ {@code period} + {@code phase}),
     * t the level clock.
     */
    public record Station(Point at, Point sway, double period, double phase) {
        public Station {
            Check.positive("period", period);
            Check.that(phase >= 0 && phase <= 1, "phase: 0 to 1, was " + phase);
            Check.notNegative("sway", sway.x());
            Check.notNegative("sway", sway.y());
        }
    }

    /**
     * M9 part D, a hull escort (design/allies/mining-tug; design/campaign Level 31, The tug in this level): the hull's
     * centre holds {@code station} (its {@code at} px below the top edge, with the sway), rising from below the bottom
     * edge into it over the first {@code enter} s (untouchable) and climbing off the top edge at {@code climb}
     * (untouchable; a section alive at its start is home). {@code hp}: each section's HP at medium, in the ally's
     * section order. {@code targeted_by}: per enemy slug the hook's mode ({@code always} or {@code none}: a hooked
     * Hornet's missiles home on the nearest section, a hooked Dart's chase crosses one, a hooked Shard Drone's links
     * sweep one; the others never hurt it). {@code unhooked}: the times of the waves of a hooked kind that are not hooked
     * (easy's every other Hornet wave, the Shard groups not sent across the hull). {@code easy} / {@code hard}: the HP,
     * the modes (merged over) and the unhooked waves instead.
     */
    public record Hull(
            Station station,
            double enter,
            Climb climb,
            List<Double> hp,
            Map<String, HookMode> targetedBy,
            Optional<List<Double>> unhooked,
            Optional<HullChange> easy,
            Optional<HullChange> hard) {
        public Hull {
            Check.positive("enter", enter);
            Check.that(climb.t() > enter, "climb.t: after the entry, was " + climb.t());
            hp = List.copyOf(hp);
            Check.notEmpty("hp", hp);
            hp.forEach(h -> Check.positive("hp", h));
            targetedBy = Map.copyOf(targetedBy);
            targetedBy
                    .values()
                    .forEach(mode -> Check.that(
                            mode == HookMode.ALWAYS || mode == HookMode.NONE,
                            "targeted_by: a hull's hook is always or none, was " + mode));
            unhooked = unhooked.map(List::copyOf);
            int sections = hp.size();
            easy.ifPresent(change -> change.check(sections));
            hard.ifPresent(change -> change.check(sections));
        }

        /** The change of {@code difficulty}, if any. */
        public Optional<HullChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }

        /** Each section's HP on {@code difficulty}. */
        public List<Double> hp(Difficulty difficulty) {
            return change(difficulty).flatMap(HullChange::hp).orElse(hp);
        }

        /** Per enemy slug its hook mode on {@code difficulty}: {@code targeted_by} with the difficulty's merged over it. */
        public Map<String, HookMode> modes(Difficulty difficulty) {
            Map<String, HookMode> modes = new java.util.TreeMap<>(targetedBy);
            change(difficulty).flatMap(HullChange::targetedBy).ifPresent(modes::putAll);
            return modes;
        }

        /** The times of the unhooked waves on {@code difficulty}. */
        public List<Double> unhooked(Difficulty difficulty) {
            return change(difficulty).flatMap(HullChange::unhooked).orElse(unhooked.orElse(List.of()));
        }
    }

    /** M9 part D: a difficulty's change to a hull escort: its sections' HP, its hook's modes, its unhooked waves. */
    public record HullChange(
            Optional<List<Double>> hp, Optional<Map<String, HookMode>> targetedBy, Optional<List<Double>> unhooked) {
        public HullChange {
            hp = hp.map(List::copyOf);
            targetedBy = targetedBy.map(Map::copyOf);
            unhooked = unhooked.map(List::copyOf);
            targetedBy.ifPresent(modes -> modes.values()
                    .forEach(mode -> Check.that(
                            mode == HookMode.ALWAYS || mode == HookMode.NONE,
                            "targeted_by: a hull's hook is always or none, was " + mode)));
        }

        private void check(int sections) {
            hp.ifPresent(list -> {
                Check.that(list.size() == sections, "hp: one per section, " + sections + ", was " + list.size());
                list.forEach(h -> Check.positive("hp", h));
            });
        }
    }

    /**
     * M5 part D: an air escort's liftoff: at {@code t} each unit stands on its pad (one {@code [x, y]}
     * screen point per unit, px below the top edge) and eases to its station over {@code seconds}.
     */
    public record Liftoff(double t, double seconds, List<Point> pads) {
        public Liftoff {
            Check.notNegative("t", t);
            Check.positive("seconds", seconds);
            pads = List.copyOf(pads);
        }
    }

    /** M5 part D: the climb-out: from {@code t} the units alive climb off the top edge over {@code seconds}, home. */
    public record Climb(double t, double seconds) {
        public Climb {
            Check.positive("t", t);
            Check.positive("seconds", seconds);
        }
    }

    /**
     * M5 part D: the scripted loss: unit {@code unit} (1-based) is untouchable until {@code t}; its
     * glow starts {@code glow} s before, the lance takes it at {@code t}.
     */
    public record ScriptedLoss(int unit, double t, double glow) {
        public ScriptedLoss {
            Check.positive("t", t);
            Check.notNegative("glow", glow);
            Check.that(glow <= t, "glow: starts in the level, at most t");
        }
    }

    /** The convoy rolls in: the first unit at {@code t} s, then one every {@code interval} s, at {@code speed} px/s up the screen. */
    public record Enter(double t, double interval, double speed) {
        public Enter {
            Check.notNegative("t", t);
            Check.notNegative("interval", interval);
            Check.positive("speed", speed);
        }
    }

    /**
     * The target-the-objective hook (design/enemies): in {@code mode} (only {@code nearest} so far)
     * the aimed attacks of the {@code enemies} go at the ship or the nearest convoy unit.
     */
    public record Hook(String mode, List<String> enemies) {
        public Hook {
            Check.that(mode.equals("nearest"), "mode: only 'nearest' is implemented, was '" + mode + "'");
            Check.notEmpty("enemies", enemies);
            enemies = List.copyOf(enemies);
        }
    }

    /** A difficulty's change to the convoy units: their HP. */
    public record AllyChange(double hp) {
        public AllyChange {
            Check.positive("hp", hp);
        }
    }

    /**
     * The road on the ground layer (Level 04): a curve through {@code points}, one {@code [t, x]}
     * each ({@code t} when it passes the middle of the screen, as for placed backdrop pieces),
     * drawn as a ribbon {@code width} px wide with the backdrop image {@code texture}
     * ({@code assets/backdrop/level-NN/<texture>.png}; a flat placeholder colour without one).
     */
    public record Road(double width, Optional<String> texture, List<RoadPoint> points) {
        public Road {
            Check.positive("width", width);
            Check.that(points.size() >= 2, "points: a road has at least two");
            for (int i = 1; i < points.size(); i++) {
                Check.that(points.get(i).t() > points.get(i - 1).t(), "points: in time order, point " + i);
            }
            points = List.copyOf(points);
        }
    }

    /** A road point: {@code t} when it passes the middle of the screen (may lie before the start), its {@code x}. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record RoadPoint(double t, double x) {}

    /**
     * M5 part E: a naval convoy outside the objectives (design/allies, convoy cargo ship and escort
     * frigate; Level 11): its {@code units} in order; {@code glide} s of the moves at the arena's
     * halt and after the boss (on the real steps); {@code lane_y} px below the top edge, a unit's
     * centre in its lane; {@code hold_clear} the px the sea scrolls after the halt before the units
     * glide back to their stations (Level 11: Platform Tiamat passes them; 0 or absent: at once),
     * each holding in its lane, away or at its {@code hold} point meanwhile; {@code easy} / {@code
     * hard} the HP of its units that can be damaged.
     */
    public record Convoy(
            List<ConvoyUnit> units,
            double glide,
            Optional<Double> laneY,
            Optional<Double> holdClear,
            Optional<AllyChange> easy,
            Optional<AllyChange> hard) {
        public Convoy {
            Check.notEmpty("units", units);
            units = List.copyOf(units);
            Check.positive("glide", glide);
            holdClear.ifPresent(px -> Check.that(px >= 0, "hold_clear: 0 px or more, was " + px));
            laneY.ifPresent(y -> Check.that(y > 0 && y < PlayField.HEIGHT, "lane_y: on the play field, was " + y));
            Check.that(
                    units.stream().noneMatch(unit -> unit.lane().isPresent()) || laneY.isPresent(),
                    "a convoy whose units take lanes gives their lane_y");
        }
    }

    /**
     * M5 part E: a naval convoy's unit: an {@code ally} slug (one that {@code follows: stations}), its
     * {@code name} ({@code {ally}} in a radio line: "Halvorsen"), its {@code station} {@code [x, y]}
     * (px from the play field's left edge, px below the top edge, held at the scroll speed) and either
     * the arena {@code lane} it glides to at the halt (1-based, of the boss's lanes) or {@code leaves:
     * true} (it drops back off the bottom edge at the halt and returns after the boss); its {@code
     * hold} point {@code [x, y]} (as the station's; below the bottom edge for off it) it glides to when
     * the halt ends, holding where it is without one, until its convoy's {@code hold_clear}.
     */
    public record ConvoyUnit(
            String ally,
            String name,
            Point station,
            Optional<Integer> lane,
            Optional<Boolean> leaves,
            Optional<Point> hold) {
        public ConvoyUnit {
            Check.that(!name.isBlank(), "name: the unit's name");
            Check.that(lane.isPresent() != leaves.orElse(false), "give the unit's lane or leaves: true, one of them");
            lane.ifPresent(l -> Check.positive("lane", l));
        }
    }

    /**
     * M8 part F (design/campaign Level 26, Rook's loss; design/player/wingmen, Rook's loss and absence; m8f-gaps.md §1.2,
     * §5 F6, F7): at {@code trails} s on the level clock the torpedo trails rise and Rook's barks end; at {@code t} s he
     * is hit, stops firing and taking hits and spirals down off the bottom edge in {@code spin} s, gone for the attempt.
     * Not an eject (no pod, no repair). Only Rook flying in the escort slot plays it; ejected, without an escort or with
     * the Bulwark it is the radio's alone. A retry replays it.
     */
    public record RookLoss(double t, double trails, double spin) {
        public RookLoss {
            Check.notNegative("trails", trails);
            Check.that(t > trails, "rook_loss.t: after its trails, was " + t);
            Check.positive("spin", spin);
        }
    }

    /**
     * M9 part B (design/player/wingmen, Rook's rescue and return; design/campaign Level 29): the mid-level escort swap at
     * the held arena's release: the craft in the prison tender's hold launches from {@code at} ({@code [x, y]}, y px
     * below the top edge of the halted screen) and Rook joins his slot over {@code join} s (the loaner peels away).
     */
    public record EscortSwap(Point at, double join) {
        public EscortSwap {
            Check.positive("escort_swap.join", join);
            Check.that(
                    at.x() >= 0
                            && at.x() <= vanguard.sim.PlayField.WIDTH
                            && at.y() >= 0
                            && at.y() <= vanguard.sim.PlayField.HEIGHT,
                    "escort_swap.at: on the screen");
        }
    }
}
