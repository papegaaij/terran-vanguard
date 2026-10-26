package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * M8 part H (design/enemies/bosses/abyssal-maw): a coiled arena boss's keys in its stat block ({@link EnemyData}): the
 * boss script's {@code coil} (the body round the arena's edges, the gill sacs, the jaw, the fan walls, the coil's
 * sweeps, the dive and the lunge, the implants and the shed Eel Swarms) and the difficulties' {@code coil_arena}
 * changes to it. Places are screen px (from the left, below the top edge: the level data's convention); content's
 * {@link MawRules} turns them into the simulation's frame (y up) once.
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.CoilData}
 * and so on, without growing that record's file.
 */
public sealed interface MawBlock permits EnemyData {
    /** The sides of the arena the coil lines, by their data names. */
    List<String> COIL_SIDES = List.of("left", "bottom", "right");

    /**
     * A coiled boss's script ({@code boss.coil}): from the arrival (the bar) {@code intro} s until the coil and the head
     * are up (invulnerable, nothing fires); its {@code body}, its gill {@code sacs}, the throat's {@code jaw}, the fan
     * {@code wall}s, the coil's {@code sweeps}, the {@code dive} and lunge, the {@code implants} and the {@code shed}
     * Eel Swarms (each but the body, sacs, jaw and sweeps optional).
     */
    record CoilData(
            double intro,
            CoilBody body,
            Sacs sacs,
            JawData jaw,
            Optional<WallData> wall,
            SweepsData sweeps,
            Optional<DiveData> dive,
            Optional<ImplantsData> implants,
            Optional<ShedData> shed) {
        public CoilData {
            Check.notNegative("intro", intro);
        }
    }

    /**
     * The coil's body: {@code segments} boxes of {@code hitbox}, {@code spacing} px apart along {@code path} (its
     * centre line from the head's end, {@code [x, y]} screen px; beyond its last point it runs on straight), each turned
     * along it; the wall it makes: the ship and the escort keep {@code clearance} px inside its {@code inner} flanks.
     */
    record CoilBody(int segments, double spacing, Size hitbox, List<Point> path, Inner inner, double clearance) {
        public CoilBody {
            path = List.copyOf(path);
            Check.positive("segments", segments);
            Check.positive("spacing", spacing);
            Check.that(path.size() >= 2, "path: two points or more");
            Check.notNegative("clearance", clearance);
        }
    }

    /**
     * The coil's inner flanks, screen px: {@code left} and {@code right} from the field's left edge, {@code bottom} and
     * {@code top} below its top edge ({@code top} none if not given: the ship reaches the top edge).
     */
    record Inner(double left, double right, double bottom, Optional<Double> top) {
        public Inner {
            Check.that(left >= 0 && right <= PlayField.WIDTH && left < right, "left and right: on the field, in order");
            Check.that(bottom > 0 && bottom <= PlayField.HEIGHT, "bottom: on the field, was " + bottom);
            top.ifPresent(t -> Check.that(t >= 0 && t < bottom, "top: above the bottom flank, was " + t));
        }
    }

    /** The gill sacs: its {@code parts}, each swelling {@code swell} s before its ring (the tell). */
    record Sacs(List<String> parts, double swell) {
        public Sacs {
            parts = List.copyOf(parts);
            Check.notEmpty("parts", parts);
            Check.notNegative("swell", swell);
        }
    }

    /** The throat's fan cycle: the throat glows {@code glow} s, then the fan; the jaw stays open {@code open} s. */
    record JawData(double glow, double open) {
        public JawData {
            Check.notNegative("glow", glow);
            Check.that(open >= glow, "open: at least the glow, was " + open);
        }
    }

    /**
     * The fan walls: its {@code fan} attack {@code attack} straight down from the throat after a {@code glow} s glow,
     * with {@code gaps} orbs left out: the first {@code near}–{@code far} px beside the ship's x at its height and within
     * {@code chain} px of the last wall's, a second (easy's two) the same on the ship's other side.
     */
    record WallData(String attack, double glow, int gaps, double near, double far, double chain) {
        public WallData {
            Check.notNegative("glow", glow);
            Check.that(gaps == 1 || gaps == 2, "gaps: 1 or 2, was " + gaps);
            Check.notNegative("near", near);
            Check.that(far >= near, "far: at least near, was " + far);
            Check.positive("chain", chain);
        }

        /** This wall with the difficulty's gaps, if it changes them. */
        public WallData with(Optional<Integer> changed) {
            return changed.map(g -> new WallData(attack, glow, g, near, far, chain))
                    .orElse(this);
        }
    }

    /**
     * The coil's sweeps: per boss phase (by name) every {@code every} s a bight of {@code segments} segments on the
     * next of its {@code sides}, centred on the ship's y below the top edge (the left and right ones, within {@code
     * along}) or its x (the bottom one, within {@code across}) as the tell starts: the {@code tell}, the {@code swing} in
     * by {@code reach} px ({@code bottom_reach} the bottom one), the {@code hold}, the swing {@code back}; what the bight
     * reaches during its swing takes the {@code bullet} class's damage once a sweep.
     */
    record SweepsData(
            int segments,
            double tell,
            double swing,
            double hold,
            double back,
            double reach,
            double bottomReach,
            String bullet,
            Span along,
            Span across,
            List<SweepPhase> phases) {
        public SweepsData {
            phases = List.copyOf(phases);
            Check.positive("segments", segments);
            Check.positive("tell", tell);
            Check.positive("swing", swing);
            Check.notNegative("hold", hold);
            Check.positive("back", back);
            Check.notNegative("reach", reach);
            Check.notNegative("bottom_reach", bottomReach);
        }

        /** These sweeps with the difficulty's tell, if it changes it. */
        public SweepsData with(Optional<Double> changed) {
            return changed.map(t -> new SweepsData(
                            segments, t, swing, hold, back, reach, bottomReach, bullet, along, across, phases))
                    .orElse(this);
        }
    }

    /** A boss phase's sweeps: every {@code every} s, the {@code sides} ({@link #COIL_SIDES}) in turn. */
    record SweepPhase(String phase, double every, List<String> sides) {
        public SweepPhase {
            sides = List.copyOf(sides);
            Check.positive("every", every);
            Check.notEmpty("sides", sides);
            sides.forEach(
                    side -> Check.that(COIL_SIDES.contains(side), "sides: left, bottom or right, was '" + side + "'"));
        }
    }

    /**
     * The dive and the lunge, once, in boss phase {@code phase} once the head holds less than {@code below} of its HP:
     * the {@code slide} into the dark, the {@code quiet}, the {@code shadow} up a {@code width} px column at the ship's
     * x (within {@code column}), the {@code lunge} up it (the {@code bullet} class's damage once to what it passes), the
     * {@code body} a wall in the column, the slide back ({@code return}).
     */
    record DiveData(
            String phase,
            double below,
            double slide,
            double quiet,
            double shadow,
            double lunge,
            double body,

            @com.fasterxml.jackson.annotation.JsonProperty("return")
            double back,

            double width,
            Span column,
            String bullet) {
        public DiveData {
            Check.that(below > 0 && below < 1, "below: a share between 0 and 1, was " + below);
            Check.positive("slide", slide);
            Check.notNegative("quiet", quiet);
            Check.positive("shadow", shadow);
            Check.positive("lunge", lunge);
            Check.notNegative("body", body);
            Check.positive("return", back);
            Check.positive("width", width);
        }

        /** This dive with the difficulty's shadow, if it changes it. */
        public DiveData with(Optional<Double> changed) {
            return changed.map(
                            s -> new DiveData(phase, below, slide, quiet, s, lunge, body, back, width, column, bullet))
                    .orElse(this);
        }
    }

    /**
     * The implants: its {@code parts} (on the coil) spark open in turn from boss phase {@code phase} on: a {@code tell},
     * then {@code open} s (a hit counts on the head's HP at the implant's multiplier; shut it is armoured), firing its
     * {@code aimed} attack {@code attack} after an {@code arc} before each volley.
     */
    record ImplantsData(List<String> parts, String phase, String attack, double tell, double open, double arc) {
        public ImplantsData {
            parts = List.copyOf(parts);
            Check.notEmpty("parts", parts);
            Check.notNegative("tell", tell);
            Check.positive("open", open);
            Check.notNegative("arc", arc);
        }

        /** These implants with the difficulty's open time, if it changes it. */
        public ImplantsData with(Optional<Double> changed) {
            return changed.map(o -> new ImplantsData(parts, phase, attack, tell, o, arc))
                    .orElse(this);
        }
    }

    /**
     * The shed units: {@code enemy} (a segment chain: an Eel Swarm) of {@code segments} segments, out of the coil at the
     * next of its {@code points} (in turn, the one nearest the ship skipped) after a {@code tell} s plate flare; per boss
     * phase every {@code every} s while fewer than {@code most} of them live. No edge warning.
     */
    record ShedData(String enemy, int segments, double tell, List<ShedPointData> points, List<ShedPhase> phases) {
        public ShedData {
            points = List.copyOf(points);
            phases = List.copyOf(phases);
            Check.positive("segments", segments);
            Check.notNegative("tell", tell);
            Check.notEmpty("points", points);
            Check.notEmpty("phases", phases);
        }

        /** This shed with the difficulty's segment count, if it changes it. */
        public ShedData with(Optional<Integer> changed) {
            return changed.map(n -> new ShedData(enemy, n, tell, points, phases))
                    .orElse(this);
        }
    }

    /**
     * A shed point on the coil's {@code side} flank ({@link #COIL_SIDES}) {@code at} px: below the top edge on a side,
     * from the left edge on the bottom.
     */
    record ShedPointData(String side, double at) {
        public ShedPointData {
            Check.that(COIL_SIDES.contains(side), "side: left, bottom or right, was '" + side + "'");
            Check.that(
                    at >= 0 && at <= (side.equals("bottom") ? PlayField.WIDTH : PlayField.HEIGHT),
                    "at: on the field's flank, was " + at);
        }
    }

    /** A boss phase's shedding: every {@code every} s while fewer than {@code most} shed units live. */
    record ShedPhase(String phase, double every, int most) {
        public ShedPhase {
            Check.positive("every", every);
            Check.that(most >= 1 && most <= vanguard.sim.CoilSpec.Shed.MOST, "most: 1 to 4, was " + most);
        }
    }

    /**
     * A difficulty's changes to a coiled boss ({@code coil_arena}): the sweeps' tell instead (easy 1.5, hard 0.9 s), the
     * lunge's shadow instead (easy 2.4, hard 1.6 s), the implants' open time instead (easy 5, hard 3 s), the shed units'
     * segments instead (hard 10) and the fan walls' gaps instead (easy 2).
     */
    record CoilArenaChange(
            Optional<Double> sweepTell,
            Optional<Double> shadow,
            Optional<Double> implantOpen,
            Optional<Integer> shedSegments,
            Optional<Integer> wallGaps) {
        public CoilArenaChange {
            sweepTell.ifPresent(t -> Check.positive("sweep_tell", t));
            shadow.ifPresent(s -> Check.positive("shadow", s));
            implantOpen.ifPresent(o -> Check.positive("implant_open", o));
            shedSegments.ifPresent(n -> Check.positive("shed_segments", n));
            wallGaps.ifPresent(g -> Check.that(g == 1 || g == 2, "wall_gaps: 1 or 2, was " + g));
        }
    }
}
