package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Map;
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
     * @param headlights the hydro-kit's twin headlights ({@link Headlights}: a strength alone or {@code {strength,
     *     reach, angle}})
     * @param rim M8 step LX (round 54's F.2): a faint rim light along the walls' rock edges, the cave notches' and the
     *     islands', 0 (none, if not given) to 1, so the trench's shape reads outside the headlights
     * @param ramp M8 part F (design/campaign Level 26, the lights going out; m8f-gaps.md §5 F4): only a section's own look
     *     gives it: the ambient eases from the section's {@code ambient} to its {@code to} over level time {@code
     *     from}–{@code until}; none if not given
     * @param lights M8 part F (Level 26's lamp posts, beacons, the farm barge's lamp and the survey buoy): static pools of
     *     light on the ground, scrolling with it; only the level's look gives them; none if not given
     * @param escortLamp M8 part F (Level 26's Rook's lamp, design/campaign Level 26, Darkness): the radius of the light
     *     pool round the escort in his slot (Rook or the Bulwark), px; only the level's look gives it; none if not given
     */
    public record UnderwaterLook(
            double ambient,
            String tint,
            double caustics,
            int snow,
            Headlights headlights,
            Optional<Double> rim,
            Optional<AmbientRamp> ramp,
            Optional<List<Light>> lights,
            Optional<Double> escortLamp) {
        /** Without M8 part F's ramp, lights and escort lamp. */
        public UnderwaterLook(
                double ambient, String tint, double caustics, int snow, Headlights headlights, Optional<Double> rim) {
            this(ambient, tint, caustics, snow, headlights, rim, Optional.empty(), Optional.empty(), Optional.empty());
        }

        public UnderwaterLook {
            lights = lights.map(List::copyOf);
            escortLamp.ifPresent(r -> Check.positive("escort_lamp", r));
            Check.that(ambient > 0 && ambient <= 1, "ambient: above 0, at most 1, was " + ambient);
            Check.that(tint.matches("[0-9a-f]{6}"), "tint: rrggbb in lower case, was '" + tint + "'");
            Check.that(caustics >= 0 && caustics <= 1, "caustics: 0 to 1, was " + caustics);
            Check.notNegative("snow", snow);
            Check.that(headlights != null, "headlights: give them");
            rim.ifPresent(r -> Check.that(r >= 0 && r <= 1, "rim: 0 to 1, was " + r));
        }

        /** The rim light, 0 if not given. */
        public double rimLight() {
            return rim.orElse(0.0);
        }

        /** M8 part F: the static lights, none if not given. */
        public List<Light> lightList() {
            return lights.orElse(List.of());
        }

        /**
         * M8 part F: the ambient light at level time {@code t}: its {@code ambient}, eased (smoothstep) to its ramp's
         * {@code to} over the ramp's time and held there after it; its {@code ambient} without a ramp.
         */
        public double ambientAt(double t) {
            if (ramp.isEmpty() || t <= ramp.get().from()) {
                return ambient;
            }
            AmbientRamp r = ramp.get();
            if (t >= r.until()) {
                return r.to();
            }
            double u = (t - r.from()) / (r.until() - r.from());
            return ambient + (r.to() - ambient) * u * u * (3 - 2 * u);
        }

        /** This look with a section's {@code change} applied (each value it gives instead of this one's). */
        public UnderwaterLook with(UnderwaterLookChange change) {
            return new UnderwaterLook(
                    change.ambient().orElse(ambient),
                    change.tint().orElse(tint),
                    change.caustics().orElse(caustics),
                    change.snow().orElse(snow),
                    change.headlights().orElse(headlights),
                    change.rim().or(() -> rim),
                    // M8 part F: a section's ramp is its own; the lights and the escort's lamp are the level's.
                    change.ramp(),
                    lights,
                    escortLamp);
        }
    }

    /**
     * M8 part F (design/campaign Level 26, Layout: 20 % → 6 % over t=8–18; m8f-gaps.md §5 F4): the ambient light eases
     * from the section's {@code ambient} to {@code to} over level time {@code from}–{@code until}.
     */
    public record AmbientRamp(double to, double from, double until) {
        public AmbientRamp {
            Check.that(to > 0 && to <= 1, "ramp.to: above 0, at most 1, was " + to);
            Check.notNegative("ramp.from", from);
            Check.that(until > from, "ramp.until: after its from, was " + until);
        }
    }

    /**
     * M8 part F (design/campaign Level 26, Darkness: the light pools): a static pool of light on the ground, scrolling
     * with it: its centre on the terrain's row {@code scroll} (the ground scroll at which that row reaches the top edge,
     * {@link TerrainBlock}'s rows: a row below 0 is on the screen at the start) and {@code x} px from the left edge, its
     * {@code radius} px and its {@code colour}, {@code rrggbb} (presentation only).
     */
    public record Light(double scroll, double x, double radius, String colour) {
        public Light {
            Check.that(x >= 0 && x <= PlayField.WIDTH, "lights: x inside the play field, was " + x);
            Check.positive("radius", radius);
            Check.that(colour.matches("[0-9a-f]{6}"), "lights: colour rrggbb in lower case, was '" + colour + "'");
        }
    }

    /**
     * M8 step LX (round 54's F.2, "headlights that illuminate the world"): the hydro-kit's twin headlights, light that
     * brings the world inside their cones (the floor, the walls, the cave mouths, the units) up toward its full colour,
     * with a soft falloff along and across the beams; outside them the ambient. Written as a strength alone (the
     * default beam: {@value #REACH} px, {@value #ANGLE}°) or as {@code {strength, reach, angle}}.
     *
     * @param strength how near the world in the cones comes to its full colour, 0 (off) to 1
     * @param reach the beams' length ahead of the lamps, px
     * @param angle the lit sector ahead, both cones together, degrees
     */
    public record Headlights(double strength, double reach, double angle) {
        /** The default beam: the round-48 twin cones' length and the sector they cover together. */
        public static final double REACH = 200;

        public static final double ANGLE = 44;

        public Headlights {
            Check.that(strength >= 0 && strength <= 1, "headlights: strength 0 to 1, was " + strength);
            Check.that(reach >= 40 && reach <= PlayField.HEIGHT, "headlights: reach 40 to 540 px, was " + reach);
            Check.that(angle >= 10 && angle <= 120, "headlights: angle 10° to 120°, was " + angle);
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static Headlights of(Object value) {
            if (value instanceof Number strength) {
                return new Headlights(strength.doubleValue(), REACH, ANGLE);
            }
            if (value instanceof Map<?, ?> map) {
                for (Object key : map.keySet()) {
                    Check.that(
                            List.of("strength", "reach", "angle").contains(key),
                            "headlights: strength, reach and angle, was '" + key + "'");
                }
                Check.that(map.get("strength") instanceof Number, "headlights: give the strength");
                return new Headlights(
                        number(map, "strength", 0), number(map, "reach", REACH), number(map, "angle", ANGLE));
            }
            throw new IllegalArgumentException(
                    "headlights must be a strength or {strength, reach, angle}, was " + value);
        }

        private static double number(Map<?, ?> map, String key, double otherwise) {
            Object value = map.get(key);
            if (value == null) {
                return otherwise;
            }
            Check.that(value instanceof Number, "headlights: " + key + " a number, was " + value);
            return ((Number) value).doubleValue();
        }
    }

    /** M8 part C: a section's changes to the level's {@link UnderwaterLook}, each value instead of the level's. */
    public record UnderwaterLookChange(
            Optional<Double> ambient,
            Optional<String> tint,
            Optional<Double> caustics,
            Optional<Integer> snow,
            Optional<Headlights> headlights,
            Optional<Double> rim,
            Optional<AmbientRamp> ramp) {
        public UnderwaterLookChange {
            ambient.ifPresent(a -> Check.that(a > 0 && a <= 1, "ambient: above 0, at most 1, was " + a));
            tint.ifPresent(t -> Check.that(t.matches("[0-9a-f]{6}"), "tint: rrggbb in lower case, was '" + t + "'"));
            caustics.ifPresent(c -> Check.that(c >= 0 && c <= 1, "caustics: 0 to 1, was " + c));
            snow.ifPresent(n -> Check.notNegative("snow", n));
            rim.ifPresent(r -> Check.that(r >= 0 && r <= 1, "rim: 0 to 1, was " + r));
            Check.that(
                    ambient.isPresent()
                            || tint.isPresent()
                            || caustics.isPresent()
                            || snow.isPresent()
                            || headlights.isPresent()
                            || rim.isPresent()
                            || ramp.isPresent(),
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
