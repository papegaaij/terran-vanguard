package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Optional;

/**
 * M11 part A, a level's {@code current} (design/enemies, Distorted space; design/world/vrell-space, Hazards &amp; set
 * pieces; Level 44): the scroll's turning direction and its reversals' tells.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Current}).
 */
public sealed interface CurrentBlock permits LevelData {
    /**
     * The current: its {@code profile}, {@code [t, degrees]} points (degrees off straight down, positive: the space
     * slides to the right, the current comes from the left), eased (smoothstep) from point to point and held before the
     * first and after the last; its {@code reversals} ({@link Reversal}: Level 20's sections through zero, which the
     * level's {@code sections} hold, each told {@link #TOLD_SECONDS} ahead); its {@code easy} profile ({@link
     * EasyCurrent}). The limits are {@link VrellChecks}'.
     */
    record Current(List<Bend> profile, Optional<List<Reversal>> reversals, Optional<EasyCurrent> easy) {
        /** Each reversal is told this long ahead (Rook's call, the HUD's chevron; design/enemies, Distorted space). */
        public static final double TOLD_SECONDS = 2;

        /** A reversal's ease into and out of its reversed run, s (Level 20's turnabout). */
        public static final double EASE_SECONDS = 1;

        public Current {
            profile = List.copyOf(profile);
            reversals = reversals.map(List::copyOf);
            Check.notEmpty("current.profile", profile);
            for (int i = 1; i < profile.size(); i++) {
                Check.that(
                        profile.get(i).t() > profile.get(i - 1).t(),
                        "current.profile[" + i + "]: in time order, one point a time");
            }
            reversals.ifPresent(list -> {
                for (int i = 1; i < list.size(); i++) {
                    Check.that(list.get(i).t() > list.get(i - 1).t(), "current.reversals[" + i + "]: in time order");
                }
            });
        }

        /** Its reversals, in time order; none if not given. */
        public List<Reversal> reversalList() {
            return reversals.orElse(List.of());
        }

        /** The reversals' runs on {@code difficulty}: easy's {@code run} instead of each reversal's. */
        public double runOn(Reversal reversal, Difficulty difficulty) {
            return difficulty == Difficulty.EASY
                    ? easy.flatMap(EasyCurrent::run).orElse(reversal.run())
                    : reversal.run();
        }

        /** The profile's angles on {@code difficulty}: easy's {@code scale} times medium's (hard as medium). */
        public double scaleOn(Difficulty difficulty) {
            return difficulty == Difficulty.EASY ? easy.map(EasyCurrent::scale).orElse(1.0) : 1.0;
        }

        /** The profile's angle at level time {@code t} s at medium, degrees (smoothstep between the points). */
        public double degreesAt(double t) {
            if (t <= profile.getFirst().t()) {
                return profile.getFirst().degrees();
            }
            for (int i = 1; i < profile.size(); i++) {
                Bend to = profile.get(i);
                if (t < to.t()) {
                    Bend from = profile.get(i - 1);
                    double share = (t - from.t()) / (to.t() - from.t());
                    return from.degrees() + (to.degrees() - from.degrees()) * share * share * (3 - 2 * share);
                }
            }
            return profile.getLast().degrees();
        }
    }

    /** A point of a current's profile: {@code degrees} at {@code t} s on the level clock, written {@code [t, degrees]}. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    record Bend(double t, double degrees) {
        public Bend {
            Check.notNegative("current.profile: t", t);
            Check.that(Math.abs(degrees) < 90, "current.profile: degrees off straight down, under 90, was " + degrees);
        }
    }

    /**
     * A reversal: the scroll eases through zero from {@code t} s over {@link Current#EASE_SECONDS}, runs reversed {@code
     * run} s and eases back over {@link Current#EASE_SECONDS}; the level's sections hold it (a section with a negative
     * speed from {@code t} to {@code t} + 1 + {@code run}, its ramp 1 s, the next one's ramp 1 s).
     */
    record Reversal(double t, double run) {
        public Reversal {
            Check.notNegative("current.reversals: t", t);
            Check.positive("current.reversals: run", run);
        }

        /** The run's end on {@code difficulty}'s run, level time: the reversed section's end. */
        public double runEnd(double runSeconds) {
            return t + Current.EASE_SECONDS + runSeconds;
        }
    }

    /**
     * Easy's gentler profile (design/world/vrell-space, Distorted space: at most ±15°, one reversal a minute): every
     * angle times {@code scale}; each reversal running {@code run} s instead (its reversed section shortened).
     */
    record EasyCurrent(double scale, Optional<Double> run) {
        public EasyCurrent {
            Check.that(scale > 0 && scale <= 1, "current.easy.scale: in (0, 1], was " + scale);
            run.ifPresent(r -> Check.positive("current.easy.run", r));
        }
    }
}
