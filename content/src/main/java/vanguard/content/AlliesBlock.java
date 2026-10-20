package vanguard.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * The {@code defend} primary's ally and the level's ground {@code allies}, with the target-the-objective hook's modes
 * that go for them.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Defend}).
 */
public sealed interface AlliesBlock permits LevelData {
    /**
     * M5 part G, the {@code defend} primary (design/campaign Level 13, Objective; design/allies, Nansen
     * Relay; user decisions G1, G2 and G6 = a): one {@code ally} (one that {@code follows: ground}) at
     * {@code at} {@code [x, y]} (px from the left edge, px from the bottom edge, y up) when the level clock
     * reaches {@code from}: it scrolls in with the ground before and stands there because the ground halts
     * (a timed halt). It must survive the {@code duration} s from then (the synchronisation); its loss fails
     * the mission; it pays no credits and does not regenerate. {@code targeted_by}: per enemy slug the
     * target-the-objective hook's mode ({@code nearest}, {@code alternate}, {@code in-arc}, {@code always} or
     * {@code none}; enemies not listed are not hooked); {@code easy} / {@code hard}: its integrity ({@code
     * hp}) and modes changed on that difficulty (merged over {@code targeted_by}).
     */
    public record Defend(
            String ally,
            Point at,
            double from,
            double duration,
            Map<String, HookMode> targetedBy,
            Optional<DefendChange> easy,
            Optional<DefendChange> hard) {
        public Defend {
            Check.notNegative("from", from);
            Check.positive("duration", duration);
            Check.that(
                    at.x() >= 0 && at.x() <= PlayField.WIDTH && at.y() >= 0 && at.y() <= PlayField.HEIGHT,
                    "at: on the play field, was [" + at.x() + ", " + at.y() + "]");
            targetedBy = Map.copyOf(targetedBy);
        }

        /** When the synchronisation completes, s on the level clock. */
        public double end() {
            return from + duration;
        }

        /** The change of {@code difficulty}, if any. */
        public Optional<DefendChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }

        /** Per enemy slug its hook mode on {@code difficulty}: {@code targeted_by} with the difficulty's merged over it. */
        public Map<String, HookMode> modes(Difficulty difficulty) {
            Map<String, HookMode> modes = new java.util.TreeMap<>(targetedBy);
            change(difficulty).flatMap(DefendChange::targetedBy).ifPresent(modes::putAll);
            return modes;
        }
    }

    /** M5 part G: a difficulty's change to a defend primary: the ally's integrity and hook modes. */
    public record DefendChange(Optional<Double> hp, Optional<Map<String, HookMode>> targetedBy) {
        public DefendChange {
            hp.ifPresent(h -> Check.positive("hp", h));
            targetedBy = targetedBy.map(Map::copyOf);
        }
    }

    /**
     * M5 part G, the target-the-objective hook's modes (design/enemies, Target-the-objective hook; design/
     * campaign Level 13, What hurts the relay; user decision G1 = a).
     */
    public enum HookMode {
        @JsonProperty("none")
        NONE,
        @JsonProperty("nearest")
        NEAREST,
        @JsonProperty("alternate")
        ALTERNATE,
        @JsonProperty("in-arc")
        IN_ARC,
        @JsonProperty("always")
        ALWAYS
    }

    /**
     * M7 part D, the level's {@code allies} block (design/campaign Level 17, The Ares Landing domes; design/allies,
     * Ares Landing dome; Q.25b/c of round 36; the defaults pending the user's review in round 40): ground allies that
     * scroll through with the ground (an ally that {@code follows: ground}; the scroll never halts for them), outside
     * the objectives: they never fail the mission, and an {@code untouched} secondary judges them. {@code units}: each
     * ally's slug, its {@code name} ({@code {ally}} in a radio line) and {@code at} {@code [t, x]}, when it enters at
     * the top edge (its bottom edge there) and its x, as a ground target's placement. {@code targeted_by}: per enemy
     * slug the target-the-objective hook's mode (as a defend's; enemies not listed are not hooked: the hook is per unit
     * type, every wave of a listed enemy is hooked). {@code bomb_row}: the row, px below the top edge, a hooked bomber
     * holds while it steers to the next ally coming down; {@code bomb_slot}: px between the x slots of a hooked wave's
     * bombers (a wave of three at −slot, 0, +slot round the ally's x). Round 41 (the user's playtest 2026-10-19):
     * {@code bomb_run_in}, the s a hooked bomber flies on the play field before it may drop (over an ally before then
     * it keeps its load and steers on); left out, 0 (it drops at once). {@code easy} / {@code hard}: the hits the
     * allies break on and the hook's modes on that difficulty (merged over {@code targeted_by}).
     */
    public record GroundAllies(
            List<GroundAlly> units,
            Map<String, HookMode> targetedBy,
            double bombRow,
            double bombSlot,
            Optional<Double> bombRunIn,
            Optional<AlliesChange> easy,
            Optional<AlliesChange> hard) {
        public GroundAllies {
            Check.notEmpty("units", units);
            units = List.copyOf(units);
            targetedBy = Map.copyOf(targetedBy);
            Check.that(
                    units.stream().map(GroundAlly::name).distinct().count() == units.size(),
                    "units: every ally has its own name");
            for (int i = 1; i < units.size(); i++) {
                Check.that(
                        units.get(i).at().t() >= units.get(i - 1).at().t(),
                        "units[" + i + "]: listed in the order they enter");
            }
            Check.positive("bomb_slot", bombSlot);
            bombRunIn.ifPresent(seconds -> Check.notNegative("bomb_run_in", seconds));
        }

        /** The change of {@code difficulty}, if any. */
        public Optional<AlliesChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }

        /** Per enemy slug its hook mode on {@code difficulty}: {@code targeted_by} with the difficulty's merged over it. */
        public Map<String, HookMode> modes(Difficulty difficulty) {
            Map<String, HookMode> modes = new java.util.TreeMap<>(targetedBy);
            change(difficulty).flatMap(AlliesChange::targetedBy).ifPresent(modes::putAll);
            return modes;
        }

        /** Whether {@code enemy} goes for the allies on {@code difficulty} (a mode other than {@code none}). */
        public boolean hooks(String enemy, Difficulty difficulty) {
            HookMode mode = modes(difficulty).get(enemy);
            return mode != null && mode != HookMode.NONE;
        }
    }

    /**
     * M7 part D: a ground ally of a level's {@code allies} block: its {@code ally} slug, its {@code name} and {@code
     * at} {@code [t, x]}, when it enters at the top edge (its bottom edge there) and its x; optional {@code size}, its
     * footprint instead of the ally's (Level 17's Eden, 144 px: its hit box keeps the ally's margin, {@link
     * #hitbox}), and {@code sprite}, its sprites' base name instead of the ally's (Eden's {@code ares-dome-eden}).
     */
    public record GroundAlly(
            String ally, String name, LevelData.Placement at, Optional<Size> size, Optional<String> sprite) {
        public GroundAlly {
            Check.that(!name.isBlank(), "name: the ally's name");
            Check.that(at.x() >= 0 && at.x() <= PlayField.WIDTH, "at: x inside the play field, was " + at.x());
            sprite.ifPresent(base -> Check.that(!base.isBlank(), "sprite: a sprite name"));
        }

        /** Its footprint: its {@code size}, else the ally's. */
        public Size size(AlliesData.Ally spec) {
            return size.orElse(spec.size());
        }

        /** Its hit box: the ally's, or with a {@code size} that size less the ally's margin round its hit box. */
        public Size hitbox(AlliesData.Ally spec) {
            return size.map(s -> new Size(
                            s.width() - (spec.size().width() - spec.hitbox().width()),
                            s.height() - (spec.size().height() - spec.hitbox().height())))
                    .orElse(spec.hitbox());
        }

        /** Its sprites' base name: its {@code sprite}, else the ally's ({@link AlliesData.Ally#spriteName}). */
        public String spriteName(AlliesData.Ally spec) {
            return sprite.orElseGet(() -> spec.spriteName(ally));
        }
    }

    /**
     * M7 part D: a difficulty's change to a level's ground allies: the {@code hits} they break on (the ally's own
     * {@code hits} at medium) and the hook's modes (merged over {@code targeted_by}).
     */
    public record AlliesChange(Optional<Integer> hits, Optional<Map<String, HookMode>> targetedBy) {
        public AlliesChange {
            hits.ifPresent(n -> Check.positive("hits", n));
            targetedBy = targetedBy.map(Map::copyOf);
        }
    }
}
