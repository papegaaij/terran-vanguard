package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * The level's {@code sections} ({@link LevelData#sections()}): the stretches of the scroll with their atmosphere and peaks,
 * the hold zones and the collapse that ease and stop it.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Stretch}).
 */
public sealed interface SectionsBlock permits LevelData {
    /** A stretch of the level with one atmosphere: a section, or the parts of it around its peak. */
    public record Stretch(double start, double end, Atmosphere atmosphere) {}

    /**
     * A stretch of the scroll; it starts where the one before it ends.
     *
     * @param tiles the backdrop's tile sets in this section, at most one per layer
     * @param ramp M5 part G, a timed halt's (speed 0 without an arena): s the scroll eases down to 0 before
     *     it and back up after it ({@link #rampSeconds()}, 2 if not given). M7 part B (Level 15's speed changes,
     *     "each eased over 1 s"): on a moving section after the first, s over which the scroll eases from the
     *     section before it's speed to its own from its start (smoothstep on the level clock; {@link
     *     #easeSeconds()}); none if not given, the change instant as before; M7 part F (Level 19's wreck crawl): also
     *     on the moving section right after an arena, easing up from the halt
     * @param holdAfterBoss M7 part F: an arena's hold after its boss's death chain, s ({@link #holdAfterBossSeconds()})
     * @param speed px/s; M7 part G (design/campaign Level 20, Reverse scroll; M5 = a): a moving section's speed may be
     *     negative, the ground then moving up the screen (a reversed section, {@link #reversed()}); an arena or a timed
     *     halt stays at 0
     * @param medium M8 part A (design/world/europa, Under water rules): the section's medium, {@code air} or {@code
     *     underwater}; the level's if not given. A section whose medium differs from the one before it is a
     *     waterline at its start (Level 23's descent, Level 28's breakout)
     * @param specialLimits M8 part A (design/player/specials, Setting restrictions): the specials it forbids, by their
     *     slugs ({@code orbital-lance}), replacing its setting's list; the level's if not given
     * @param underwaterLook M8 part C (the under-water render grade; part D's request: Level 24 darkens section by
     *     section): an under-water section's changes to the level's {@code underwater_look} ({@link
     *     LevelData#underwaterLookOf}); the level's look if not given
     */
    public record Section(
            String name,
            double end,
            Optional<Double> speed,
            Atmosphere atmosphere,
            Optional<Peak> peak,
            List<String> tiles,
            Optional<Boolean> arena,
            Optional<Double> ramp,
            Optional<Double> holdAfterBoss,
            Optional<Medium> medium,
            Optional<List<String>> specialLimits,
            Optional<UnderwaterLookChange> underwaterLook) {
        /** Without M8 part C's under-water look. */
        public Section(
                String name,
                double end,
                Optional<Double> speed,
                Atmosphere atmosphere,
                Optional<Peak> peak,
                List<String> tiles,
                Optional<Boolean> arena,
                Optional<Double> ramp,
                Optional<Double> holdAfterBoss,
                Optional<Medium> medium,
                Optional<List<String>> specialLimits) {
            this(
                    name,
                    end,
                    speed,
                    atmosphere,
                    peak,
                    tiles,
                    arena,
                    ramp,
                    holdAfterBoss,
                    medium,
                    specialLimits,
                    Optional.empty());
        }

        /** Without M8 part A's medium and special limits. */
        public Section(
                String name,
                double end,
                Optional<Double> speed,
                Atmosphere atmosphere,
                Optional<Peak> peak,
                List<String> tiles,
                Optional<Boolean> arena,
                Optional<Double> ramp,
                Optional<Double> holdAfterBoss) {
            this(
                    name,
                    end,
                    speed,
                    atmosphere,
                    peak,
                    tiles,
                    arena,
                    ramp,
                    holdAfterBoss,
                    Optional.empty(),
                    Optional.empty());
        }

        /** Without M7 part F's hold after the boss. */
        public Section(
                String name,
                double end,
                Optional<Double> speed,
                Atmosphere atmosphere,
                Optional<Peak> peak,
                List<String> tiles,
                Optional<Boolean> arena,
                Optional<Double> ramp) {
            this(name, end, speed, atmosphere, peak, tiles, arena, ramp, Optional.empty());
        }

        public Section {
            Check.positive("end", end);
            // M5 part E: 0 only on the arena of a level whose boss is anchored (ContentValidator);
            // M5 part G: or on a timed halt, only with a defend primary. M7 part G: negative on a moving section (the
            // retreat's reversed scroll), never on an arena (RetreatChecks keeps the rest of the level's features out).
            Check.that(
                    speed.filter(s -> s < 0).isEmpty() || !arena.orElse(false),
                    "speed: an arena stands (0); only a moving section scrolls backwards");
            ramp.ifPresent(r -> Check.positive("ramp", r));
            Check.that(
                    ramp.isEmpty() || speed.filter(s -> s == 0).isEmpty() || !arena.orElse(false),
                    "ramp: on a timed halt (speed 0 without an arena) or a moving section, not on an arena");
            holdAfterBoss.ifPresent(h -> Check.positive("hold_after_boss", h));
            Check.that(holdAfterBoss.isEmpty() || arena.orElse(false), "hold_after_boss: only on an arena");
            specialLimits = specialLimits.map(List::copyOf);
        }

        /**
         * M7 part F (design/campaign Level 19, the wreck beat): s the level clock stays halted after the arena's boss
         * has died, past its death chain; 0 if not given.
         */
        public double holdAfterBossSeconds() {
            return holdAfterBoss.orElse(0.0);
        }

        /**
         * M7 part B: a moving section's ease from the speed before it, s: its {@code ramp}; 0 (instant) on a timed
         * halt, a halted arena or a section without one. M7 part H (Level 21's crawling arena, 140 → 20 px/s over 2 s):
         * a moving arena's {@code ramp} too (only a breaching boss's arena gives one: ColossusChecks).
         */
        public double easeSeconds() {
            return timedHalt() || speed.filter(s -> s == 0).isPresent() || (isArena() && speed.isEmpty())
                    ? 0
                    : ramp.orElse(0.0);
        }

        /**
         * M5 part G (design/campaign Level 13, The halt): whether it is a timed halt, a section of speed 0
         * without an arena, where the level clock runs on and the scroll stands.
         */
        public boolean timedHalt() {
            return speed.filter(s -> s == 0).isPresent() && !isArena();
        }

        /** M5 part G: a timed halt's eases, s: its {@code ramp}, 2 if not given. */
        public double rampSeconds() {
            return ramp.orElse(vanguard.sim.Section.TIMED_HALT_RAMP_SECONDS);
        }

        /**
         * M7 part G (design/campaign Level 20, Reverse scroll): whether its speed is negative, the ground moving up the
         * screen and the direction of travel down it.
         */
        public boolean reversed() {
            return speed.filter(s -> s < 0).isPresent();
        }

        /**
         * Whether it is the boss's arena: the level clock halts at its end while the boss lives,
         * and the next section starts at the boss's death when it dies earlier.
         */
        public boolean isArena() {
            return arena.orElse(false);
        }
    }

    /**
     * A stretch inside a section whose atmosphere peaks at {@code atmosphere} (Level 02's coolant cloud).
     *
     * @param easy M5 part G: the peak's length on easy, s, centred on {@code from}–{@code to} (Level 13's
     *     whiteout: 6 s); the authored stretch on every difficulty if not given (presentation only)
     * @param ambienceDb M5 part G: the setting's ambience is this much louder through the peak, dB, ramping
     *     in and out with the atmosphere (Level 13's wind in the whiteout, +3); none if not given
     */
    public record Peak(
            Atmosphere atmosphere, double from, double to, Optional<Double> easy, Optional<Double> ambienceDb) {
        public Peak {
            Check.that(to > from, "a peak ends after it starts");
            easy.ifPresent(seconds -> Check.that(
                    seconds > 0 && seconds <= to - from, "easy: a peak's easy length is within it, was " + seconds));
        }

        /** Without M5 part G's easy length and ambience level. */
        public Peak(Atmosphere atmosphere, double from, double to) {
            this(atmosphere, from, to, Optional.empty(), Optional.empty());
        }

        /** The peak as easy shows it: its {@code easy} length centred on it, or itself without one. */
        public Peak onEasy() {
            if (easy.isEmpty()) {
                return this;
            }
            double centre = (from + to) / 2;
            double half = easy.get() / 2;
            return new Peak(atmosphere, centre - half, centre + half, easy, ambienceDb);
        }

        /** The ambience's rise through the peak, dB: its {@code ambience_db}, 0 if not given. */
        public double ambienceRise() {
            return ambienceDb.orElse(0.0);
        }
    }

    /**
     * M8 part A (design/world/europa, Under water rules): what the play plane is in: {@code air} (every level before
     * Act 4) or {@code underwater} (the water's factors, the hydro-kit, the special limits under the ice).
     */
    public enum Medium {
        AIR,
        UNDERWATER
    }

    /**
     * M8 part C (design/world/europa, Palette and lighting; the round-48 grade a, lamp-lit to black): how the game grades
     * the play field under water (presentation only; the simulation never reads it). Bullets, glows and the HUD stay
     * ungraded.
     *
     * @param ambient the ambient light, 0 (black) to 1 (unlit scene), multiplied over the ground and the units
     * @param tint the ambient light's colour, {@code rrggbb}
     * @param caustics the animated caustics' strength on the floor and the walls, 0 (none) to 1
     * @param snow the marine snow, particles on the screen at once (0: none)
     * @param headlights the hydro-kit's twin headlight cones' strength, 0 (off) to 1
     */
    public record UnderwaterLook(double ambient, String tint, double caustics, int snow, double headlights) {
        public UnderwaterLook {
            Check.that(ambient > 0 && ambient <= 1, "ambient: above 0, at most 1, was " + ambient);
            Check.that(tint.matches("[0-9a-f]{6}"), "tint: rrggbb in lower case, was '" + tint + "'");
            Check.that(caustics >= 0 && caustics <= 1, "caustics: 0 to 1, was " + caustics);
            Check.notNegative("snow", snow);
            Check.that(headlights >= 0 && headlights <= 1, "headlights: 0 to 1, was " + headlights);
        }

        /** This look with a section's {@code change} applied (each value it gives instead of this one's). */
        public UnderwaterLook with(UnderwaterLookChange change) {
            return new UnderwaterLook(
                    change.ambient().orElse(ambient),
                    change.tint().orElse(tint),
                    change.caustics().orElse(caustics),
                    change.snow().orElse(snow),
                    change.headlights().orElse(headlights));
        }
    }

    /** M8 part C: a section's changes to the level's {@link UnderwaterLook}, each value instead of the level's. */
    public record UnderwaterLookChange(
            Optional<Double> ambient,
            Optional<String> tint,
            Optional<Double> caustics,
            Optional<Integer> snow,
            Optional<Double> headlights) {
        public UnderwaterLookChange {
            ambient.ifPresent(a -> Check.that(a > 0 && a <= 1, "ambient: above 0, at most 1, was " + a));
            tint.ifPresent(t -> Check.that(t.matches("[0-9a-f]{6}"), "tint: rrggbb in lower case, was '" + t + "'"));
            caustics.ifPresent(c -> Check.that(c >= 0 && c <= 1, "caustics: 0 to 1, was " + c));
            snow.ifPresent(n -> Check.notNegative("snow", n));
            headlights.ifPresent(h -> Check.that(h >= 0 && h <= 1, "headlights: 0 to 1, was " + h));
            Check.that(
                    ambient.isPresent()
                            || tint.isPresent()
                            || caustics.isPresent()
                            || snow.isPresent()
                            || headlights.isPresent(),
                    "underwater_look: a section's change gives at least one value");
        }
    }

    /** Low-air cloud and haze intensity (design/art-direction/README.md). */
    public enum Atmosphere {
        CLEAR,
        LIGHT,
        MEDIUM,
        HEAVY
    }

    /** An event-triggered atmosphere peak: {@code atmosphere} at once, ramping back out over {@code seconds}. */
    public record Dust(Atmosphere atmosphere, double seconds) {
        public Dust {
            Check.positive("seconds", seconds);
        }
    }

    /**
     * M5 part C (user decisions D2 and D3 = a; Level 09): a hold zone. When the first unit of its
     * {@code groups} (ground-target groups of the objectives) reaches {@code y} px below the top
     * edge, the scroll eases over {@code ramp} s to {@code speed} px/s ({@code easy} / {@code hard}
     * {@code speed}) and stays there until every unit of those groups is gone, then eases back. No
     * timeout. The level clock advances at the current speed ÷ the section's, so every time in the
     * level file is script time.
     */
    public record Hold(
            List<String> groups,
            double y,
            double speed,
            Optional<HoldChange> easy,
            Optional<HoldChange> hard,
            double ramp) {
        public Hold {
            Check.notEmpty("groups", groups);
            groups = List.copyOf(groups);
            Check.that(y > 0 && y < PlayField.HEIGHT, "y: inside the play field, was " + y);
            Check.positive("speed", speed);
            Check.positive("ramp", ramp);
        }

        /** Its speed on {@code difficulty}, px/s. */
        public double speedOn(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy.map(HoldChange::speed).orElse(speed);
                case MEDIUM -> speed;
                case HARD -> hard.map(HoldChange::speed).orElse(speed);
            };
        }
    }

    /** A difficulty's hold speed, px/s. */
    public record HoldChange(double speed) {
        public HoldChange {
            Check.positive("speed", speed);
        }
    }

    /**
     * M5 part C (user decision D5 = a; Level 09; round 31's look c, 2026-10-07): the collapse. When
     * every unit of its {@code groups} is destroyed, the backdrop's {@code tower} (placed exactly
     * once) leans for {@code warning} s (the warning), then drops straight down in {@code drop} s; at
     * the impact its {@code blast} rolls out from the tower's foot: a ring growing from {@code
     * blast.from} to {@code blast.to} px round the footprint's centre in {@code blast.seconds} (eased
     * 1 − (1 − t)²), destroying every ground unit in the band (the tower's footprint along the scroll,
     * see {@link LevelData#collapseBand}) when it reaches it, paid and scored as an Airstrike kill; air units and
     * the player are untouched. Presentation: the {@code dust} atmosphere it raises, ramping back out
     * over its {@code seconds} after the impact, and the {@code rubble} backdrop piece it leaves; a
     * hold over its groups lasts until that dust has settled (user decision, 2026-10-07).
     */
    public record Collapse(
            List<String> groups, double warning, double drop, Blast blast, String tower, Dust dust, String rubble) {
        public Collapse {
            Check.notEmpty("groups", groups);
            groups = List.copyOf(groups);
            Check.notNegative("warning", warning);
            Check.positive("drop", drop);
            Check.that(blast != null && dust != null, "collapse: needs its blast and its dust");
            Check.that(dust.seconds() >= blast.seconds(), "collapse: the dust settles after the blast");
        }

        /** Seconds from the warning's start to the impact (the end of the drop). */
        public double impact() {
            return warning + drop;
        }

        /** Seconds from the warning's start to the blast's end (the collapse's end). */
        public double end() {
            return warning + drop + blast.seconds();
        }

        /** Seconds from the warning's start to the dust's settling (a hold over its groups ends). */
        public double settled() {
            return warning + drop + dust.seconds();
        }
    }

    /**
     * The collapse's blast: its kill ring's radius grows from {@code from} to {@code to} px round the
     * tower's footprint's centre over {@code seconds} from the impact.
     */
    public record Blast(double seconds, double from, double to) {
        public Blast {
            Check.positive("seconds", seconds);
            Check.notNegative("from", from);
            Check.that(to > from, "blast: the ring grows (to > from)");
        }
    }

    /**
     * A collapse's band on the ground: {@code bottom} to {@code top}, ground positions (px from the
     * screen's bottom edge at the level start, as {@link LevelData#pieceCentre}), under a tower whose
     * footprint's centre is {@code x} px from the play field's left edge.
     */
    public record Band(double bottom, double top, double x) {
        /** The footprint's centre on the ground, a ground position. */
        public double centre() {
            return (bottom + top) / 2;
        }
    }
}
