package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A level's hazards and environment (refactoring step LS-3, {@link LevelScript#hazards()}): cranes, debris, sleds,
 * rocks, furnace vents, darkness, weather and the weather sondes.
 *
 * @param cranes the crane hazards
 * @param debris the debris chunks drifting on the air layer, in time order
 * @param sled the mass-driver sleds (Level 05)
 * @param rocks the rocks a destroyed ground unit throws in low gravity (Level 05)
 * @param vents M7 part F: the furnace vent rows (Level 19), in time order; empty for none
 * @param darkness Level 06's darkness
 * @param weather M5 part F: the level's wind and lightning (Level 12's storm)
 * @param sondes M5 part F: the weather sondes to fly through (Level 12)
 * @param cruisers M8 part H, step H2: the finale's two Ascendancy cruisers (Level 28's run, {@link CruiserRun}); empty
 *     for none
 * @param relay M9 part H, step H1b: the mass relay's flash (Level 35, {@link RelayFlash}); empty for none
 */
public record HazardSpec(
        List<CraneSpec> cranes,
        List<DebrisSpec> debris,
        Optional<SledSpec> sled,
        Optional<RockSpec> rocks,
        List<VentRow> vents,
        Optional<Darkness> darkness,
        Optional<Weather> weather,
        Optional<Sondes> sondes,
        Optional<CruiserRunSpec> cruisers,
        Optional<Relay> relay) {
    /** No hazards: no cranes, debris, sleds, rocks, vents, darkness, weather, sondes, cruisers or relay. */
    public static final HazardSpec NONE = new HazardSpec(
            List.of(),
            List.of(),
            Optional.empty(),
            Optional.empty(),
            List.of(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    /** The hazards before M9 part H's relay (none). */
    public HazardSpec(
            List<CraneSpec> cranes,
            List<DebrisSpec> debris,
            Optional<SledSpec> sled,
            Optional<RockSpec> rocks,
            List<VentRow> vents,
            Optional<Darkness> darkness,
            Optional<Weather> weather,
            Optional<Sondes> sondes,
            Optional<CruiserRunSpec> cruisers) {
        this(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, Optional.empty());
    }

    /** The hazards before M8 part H's cruisers (none). */
    public HazardSpec(
            List<CraneSpec> cranes,
            List<DebrisSpec> debris,
            Optional<SledSpec> sled,
            Optional<RockSpec> rocks,
            List<VentRow> vents,
            Optional<Darkness> darkness,
            Optional<Weather> weather,
            Optional<Sondes> sondes) {
        this(cranes, debris, sled, rocks, vents, darkness, weather, sondes, Optional.empty());
    }

    public HazardSpec {
        cranes = List.copyOf(cranes);
        debris = List.copyOf(debris);
        vents = vents.stream()
                .sorted(java.util.Comparator.comparingDouble(VentRow::t))
                .toList();
        java.util.Objects.requireNonNull(cruisers);
        java.util.Objects.requireNonNull(relay);
    }

    /** These hazards with another {@code cranes}. */
    public HazardSpec withCranes(List<CraneSpec> cranes) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code debris}. */
    public HazardSpec withDebris(List<DebrisSpec> debris) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code sled}. */
    public HazardSpec withSled(Optional<SledSpec> sled) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code rocks}. */
    public HazardSpec withRocks(Optional<RockSpec> rocks) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code vents}. */
    public HazardSpec withVents(List<VentRow> vents) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code darkness}. */
    public HazardSpec withDarkness(Optional<Darkness> darkness) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code weather}. */
    public HazardSpec withWeather(Optional<Weather> weather) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** These hazards with another {@code sondes}. */
    public HazardSpec withSondes(Optional<Sondes> sondes) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /** M9 part H, step H1b: these hazards with another {@code relay}. */
    public HazardSpec withRelay(Optional<Relay> relay) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /**
     * M9 part H, step H1b (design/campaign Level 35, the mass-relay flash; design/world/belt): the turned mass relay fires
     * once. At {@code t} s on the level clock its thin red line is drawn across the whole field, horizontal through the
     * ship's height at that moment (locked; the relay lies off the right edge when {@code fromRight}, else the left);
     * after {@code telegraphSeconds} it flashes along the line, a band {@code bandPx} wide, live {@code liveSeconds}:
     * {@code damage} to the ship once. Nothing else is hurt (Rook and the enemies on the line are untouched). The
     * difficulty's telegraph (easy 1.5 s) is the content's.
     */
    public record Relay(
            double t, double telegraphSeconds, double liveSeconds, double bandPx, double damage, boolean fromRight) {
        public Relay {
            if (!(t >= 0) || !(telegraphSeconds > 0) || !(liveSeconds > 0) || !(bandPx > 0) || !(damage > 0)) {
                throw new IllegalArgumentException("a relay flash has a time, a telegraph, a live time, a band and"
                        + " damage: " + t + ", " + telegraphSeconds + ", " + liveSeconds + ", " + bandPx + ", "
                        + damage);
            }
        }
    }

    /** M8 part H: these hazards with another {@code cruisers}. */
    public HazardSpec withCruisers(Optional<CruiserRunSpec> cruisers) {
        return new HazardSpec(cranes, debris, sled, rocks, vents, darkness, weather, sondes, cruisers, relay);
    }

    /**
     * M7 part F, a row of furnace vent grates on the ground (design/campaign Level 19, Hazards; Level 05's sleds are
     * the model): the row's grates at {@code xs} (px from the left, in ripple order: the first fires first), each
     * {@code width} px square, entering at the top edge at {@code t} s on the level clock and scrolling with the
     * ground. From {@code t} on, every {@code periodSeconds}, the row fires as a ripple: grate i glows for {@code
     * tellSeconds} from {@code i × rippleSeconds} into the cycle, then its flame column stands for {@code
     * burnSeconds}, hitting the ship and Rook whatever their layer for {@code damage} (a collision), once per burst
     * each. The vents are cold from the arena on (a level's arena section) and once the row left the bottom edge.
     *
     * <p>M8 part G (design/campaign Level 27, Hazards: the heat plumes; m8g-gaps.md §1.2, §5 G5, G6): a {@code disc}
     * row's grates are plume mouths: each burns a disc {@code width} px across (not a square), hitting the ship, Rook
     * and the Bulwark whose hull it touches; it burns every {@code air} or {@code low-air} unit over it for {@code
     * enemyDamage} once per burst (the kill is the player's), and bends a shot, an enemy bullet or a homing missile
     * entering it {@code bendRadians} away from its centre once ({@link HeatPlumes}). Every row before Level 27 is
     * square, burns no unit and bends nothing.
     */
    public record VentRow(
            double t,
            double[] xs,
            double width,
            double tellSeconds,
            double burnSeconds,
            double periodSeconds,
            double rippleSeconds,
            double damage,
            boolean disc,
            double enemyDamage,
            double bendRadians) {
        /** A square row of grates that burns no unit and bends nothing (every row before M8 part G). */
        public VentRow(
                double t,
                double[] xs,
                double width,
                double tellSeconds,
                double burnSeconds,
                double periodSeconds,
                double rippleSeconds,
                double damage) {
            this(t, xs, width, tellSeconds, burnSeconds, periodSeconds, rippleSeconds, damage, false, 0, 0);
        }

        public VentRow {
            if (!disc && (enemyDamage != 0 || bendRadians != 0)) {
                throw new IllegalArgumentException("only a plume's disc burns units or bends shots");
            }
            if (!(enemyDamage >= 0) || !(bendRadians >= 0) || !(bendRadians < StrictMath.PI / 2)) {
                throw new IllegalArgumentException(
                        "a plume burns units for 0 or more and bends by 0 up to a right angle: " + enemyDamage + ", "
                                + bendRadians);
            }
            if (!(t >= 0)
                    || xs.length == 0
                    || !(width > 0)
                    || !(tellSeconds >= 0)
                    || !(burnSeconds > 0)
                    || !(rippleSeconds >= 0)
                    || !(periodSeconds >= tellSeconds + burnSeconds + rippleSeconds * (xs.length - 1))
                    || !(damage >= 0)) {
                throw new IllegalArgumentException(
                        "a vent row has grates, a tell, a burn and a period that holds its whole ripple");
            }
            xs = xs.clone();
        }

        @Override
        public double[] xs() {
            return xs.clone();
        }

        /** The x of grate {@code i}, px from the left (allocates nothing). */
        public double x(int i) {
            return xs[i];
        }

        /** Its grates. */
        public int grates() {
            return xs.length;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof VentRow row
                    && row.t == t
                    && java.util.Arrays.equals(row.xs, xs)
                    && row.width == width
                    && row.tellSeconds == tellSeconds
                    && row.burnSeconds == burnSeconds
                    && row.periodSeconds == periodSeconds
                    && row.rippleSeconds == rippleSeconds
                    && row.damage == damage
                    && row.disc == disc
                    && row.enemyDamage == enemyDamage
                    && row.bendRadians == bendRadians;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    t,
                    java.util.Arrays.hashCode(xs),
                    width,
                    tellSeconds,
                    burnSeconds,
                    periodSeconds,
                    rippleSeconds,
                    damage,
                    disc,
                    enemyDamage,
                    bendRadians);
        }

        @Override
        public String toString() {
            return "VentRow[t=" + t + ", xs=" + java.util.Arrays.toString(xs) + ", width=" + width
                    + (disc ? ", disc" : "") + "]";
        }
    }

    /**
     * A dark level (design/campaign Level 06, Darkness rules): only the ground layer and the
     * ground units are dark outside the light pools. The ship's headlight, a cone
     * {@code headlightLength} px long and {@code headlightAngle} radians wide ahead of it, is on
     * from {@code headlightFrom} s; each scripted flare falls {@code flareSeconds}, its pool of
     * {@code flareRadius} px drifting down the screen at {@code flareDrift} px/s from where it
     * starts; the {@code lights} are static pools on the ground (dome and rail lamps), entering at
     * the top edge with the scroll. {@code ambient} is how bright the ground stays outside the
     * light (presentation only). Triggers marked dark can only be hit while lit.
     */
    public record Darkness(
            double headlightFrom,
            double headlightLength,
            double headlightAngle,
            List<Flare> flares,
            double flareSeconds,
            double flareRadius,
            double flareDrift,
            List<Light> lights,
            double ambient) {
        public Darkness {
            flares = List.copyOf(flares);
            lights = List.copyOf(lights);
        }

        /** A flare fired at {@code t} s, starting {@code depth} px below the top edge at {@code x}. */
        public record Flare(double t, double x, double depth) {}

        /** Flare {@code i}'s pool centre's y at {@code levelSeconds} (meaningful while it burns). */
        public double flareY(int i, double levelSeconds) {
            Flare flare = flares.get(i);
            return PlayField.HEIGHT - flare.depth() - flareDrift * (levelSeconds - flare.t());
        }

        /** Whether flare {@code i} burns at {@code levelSeconds}. */
        public boolean flareBurning(int i, double levelSeconds) {
            double into = levelSeconds - flares.get(i).t();
            return into >= 0 && into <= flareSeconds;
        }

        /** A static light pool of {@code radius} px on the ground, entering at the top edge at {@code t} s at {@code x}. */
        public record Light(double t, double x, double radius) {}

        /** Whether the headlight is on at {@code levelSeconds}. */
        public boolean headlightOn(double levelSeconds) {
            return levelSeconds >= headlightFrom;
        }

        /**
         * Whether the box {@code size} around (x, y) is lit for the simulation: in the headlight
         * cone of the ship at (shipX, shipY) or in a burning flare's pool at {@code levelSeconds}.
         * The static pools light only the picture (no dark trigger lies in one).
         */
        public boolean lit(double x, double y, Hitbox size, double shipX, double shipY, double levelSeconds) {
            double reach = Math.max(size.width(), size.height()) / 2;
            if (headlightOn(levelSeconds)) {
                double dx = x - shipX;
                double dy = y - shipY;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d <= headlightLength + reach
                        && dy > -reach
                        && (d <= reach
                                || Math.abs(StrictMath.atan2(dx, dy))
                                        <= headlightAngle / 2 + StrictMath.atan2(reach, d))) {
                    return true;
                }
            }
            for (int i = 0; i < flares.size(); i++) {
                Flare flare = flares.get(i);
                double into = levelSeconds - flare.t();
                if (into < 0 || into > flareSeconds) {
                    continue;
                }
                double fx = flare.x();
                double fy = PlayField.HEIGHT - flare.depth() - flareDrift * into;
                if (near(x, y, fx, fy, flareRadius + reach)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean near(double x, double y, double cx, double cy, double radius) {
            double dx = x - cx;
            double dy = y - cy;
            return dx * dx + dy * dy <= radius * radius;
        }
    }

    /**
     * The mass-driver sleds (design/world/luna, hazards): every {@code periodSeconds} from
     * {@code firstSeconds} until {@code untilSeconds} a sled shoots up the rail at {@code x}, a line
     * {@code width} px wide across the field, for {@code runSeconds}, after its lights chased for
     * {@code lightsSeconds}. It hits the ship for {@code damage} (once per sled) and blocks shots
     * and enemy bullets while it runs; the trigger that reveals {@code clampSecret} can only be hit
     * while the rail is dark.
     */
    public record SledSpec(
            double x,
            double width,
            double firstSeconds,
            double periodSeconds,
            double untilSeconds,
            double lightsSeconds,
            double runSeconds,
            double damage,
            String clampSecret) {
        public SledSpec {
            if (!(periodSeconds > lightsSeconds + runSeconds) || !(width > 0) || !(damage > 0)) {
                throw new IllegalArgumentException("a sled's period holds its lights and its run");
            }
        }
    }

    /**
     * Rocks a destroyed ground unit throws in low gravity (design/campaign, Level 05): {@code min}
     * to {@code max} of them (from the simulation's random numbers) on the air layer, each drifting
     * at {@code minSpeed}..{@code maxSpeed} px/s in a random direction until it vanishes after
     * {@code lifeSeconds}; {@code hp}, contact {@code damage}, no credits. None is thrown when the
     * unit lies within {@code clearance} px of the ship.
     */
    public record RockSpec(
            int min,
            int max,
            double minSpeed,
            double maxSpeed,
            double lifeSeconds,
            Hitbox size,
            double hp,
            double damage,
            double clearance) {
        public RockSpec {
            if (min < 0 || max < min || !(lifeSeconds > 0)) {
                throw new IllegalArgumentException("rocks: min <= max, a life");
            }
        }

        /** The debris chunk a rock is. */
        public DebrisSpec chunk() {
            return new DebrisSpec(0, 0, "rock", size, 0, -1, hp, damage, clearance);
        }
    }

    /**
     * M5 part F, a level's {@code weather} at one difficulty (design/campaign Level 12, Weather): the
     * wind and the lightning. The wind is a push sideways, px/s, added to the positions (not the
     * velocities) of the ship, the escort, the units with a latch and the Whirl Seeds (their spiral's
     * centre too): each gust blows {@code push} from its side for {@code gustSeconds} from its time,
     * including {@code rampSeconds} up and down, telegraphed {@code telegraphSeconds} ahead; each
     * drift blows steadily over its span with the same ramps; they add up. A lightning flash raises
     * {@link SimEvents.Type#LIGHTNING} at its time and changes nothing else.
     *
     * @param gusts in time order
     * @param lightning the flash times, s, in order
     */
    public record Weather(
            List<Gust> gusts,
            double gustSeconds,
            double rampSeconds,
            double push,
            double telegraphSeconds,
            List<Drift> drifts,
            List<Double> lightning) {
        public Weather {
            gusts = List.copyOf(gusts);
            drifts = List.copyOf(drifts);
            lightning = List.copyOf(lightning);
            if (!(gustSeconds > 2 * rampSeconds) || rampSeconds < 0 || push < 0 || telegraphSeconds < 0) {
                throw new IllegalArgumentException(
                        "a gust lasts longer than its ramps, its push and telegraph not negative");
            }
            for (int i = 1; i < gusts.size(); i++) {
                if (gusts.get(i).t() < gusts.get(i - 1).t()) {
                    throw new IllegalArgumentException("gusts in time order");
                }
            }
            for (int i = 1; i < lightning.size(); i++) {
                if (lightning.get(i) < lightning.get(i - 1)) {
                    throw new IllegalArgumentException("lightning in time order");
                }
            }
        }
    }

    /** M5 part F: a gust starting to push at {@code t}, blowing from the left edge (to the right) or the right. */
    public record Gust(double t, boolean fromLeft) {}

    /**
     * M5 part F: a steady drift of {@code push} px/s from the left or the right from {@code start} to
     * {@code end} (Level 12's storm wall; the section's span), with the weather's ramps inside it.
     */
    public record Drift(double start, double end, double push, boolean fromLeft) {
        public Drift {
            if (!(end > start) || push < 0) {
                throw new IllegalArgumentException("a drift spans some time and pushes");
            }
        }
    }

    /**
     * M5 part F, the weather sondes (design/campaign Level 12, user decision F7 = b): each enters at
     * the top edge at its time and x and drifts straight down at {@code speed} px/s on the play plane,
     * its x held; it is not shot, not pushed by the wind, not pulled by the magnet and not collected by
     * the escort. The ship collects one when its centre comes within {@code radius} px of the sonde's.
     */
    public record Sondes(double speed, double radius, List<Sonde> at) {
        public Sondes {
            at = List.copyOf(at);
            if (!(speed > 0) || !(radius > 0) || at.isEmpty()) {
                throw new IllegalArgumentException("sondes drift down, are collected within a radius and are placed");
            }
            for (int i = 1; i < at.size(); i++) {
                if (at.get(i).t() < at.get(i - 1).t()) {
                    throw new IllegalArgumentException("sondes in time order");
                }
            }
        }
    }

    /** M5 part F: a weather sonde entering at the top edge at {@code t} at {@code x}. */
    public record Sonde(double t, double x) {}

    /**
     * A crane hazard (design/campaign, Level 02: Crane Four): an arm hanging from a pivot above the
     * play field that swings between two angles, blinking its lights for the telegraph before each
     * swing. It is lowered from along the gantry during the telegraph before its first swing and
     * raised back after its last one. The arm deals contact damage, at most once per
     * {@link #HIT_INTERVAL_SECONDS}, and blocks every shot; a clamp at its tip counts the player's
     * hits while the arm swings and releases a secret's hidden crate after enough of them.
     *
     * @param pivotX the pivot in play-field px
     * @param pivotY the pivot, above the top edge
     * @param length the arm, px
     * @param width the arm's thickness, px
     * @param fromRadians the angle the first swing starts at, from straight down, positive to the right
     * @param toRadians the angle the first swing ends at; swings alternate between the two
     * @param swings the times the swings start, s from the level start
     * @param swingSeconds how long a swing takes
     * @param telegraphSeconds how long the lights blink before a swing
     * @param damage contact damage (shield first)
     * @param clampHits the player's hits on the clamp that release the crate; 0 for no clamp
     * @param crateCredits the crate's credits
     * @param secret the secret's name, for its radio cue
     */
    public record CraneSpec(
            double pivotX,
            double pivotY,
            double length,
            double width,
            double fromRadians,
            double toRadians,
            List<Double> swings,
            double swingSeconds,
            double telegraphSeconds,
            double damage,
            int clampHits,
            int crateCredits,
            String secret) {
        /** The arm hits the ship at most once in this time. */
        public static final double HIT_INTERVAL_SECONDS = 1;

        public CraneSpec {
            swings = List.copyOf(swings);
            if (swings.isEmpty()) {
                throw new IllegalArgumentException("a crane swings at least once");
            }
        }
    }

    /**
     * A debris chunk (design/world/earth-orbit, hazards): it enters at the top edge at {@code t}
     * at {@code x} (moved sideways when that is too close to the ship) and drifts across the
     * screen at ({@code vx}, {@code vy}) px/s on the air layer, y up. It blocks every shot and
     * enemy bullet that touches it; a large chunk is indestructible and deals contact damage, at
     * most once per {@link #HIT_INTERVAL_SECONDS}; a small one breaks after {@code hp} damage and
     * pays nothing.
     *
     * @param chunk the chunk's name in the level ({@code large-a}); its sprite is {@code debris-<chunk>}
     * @param hp a small chunk's HP; infinite for a large one
     * @param damage a large chunk's contact damage; 0 for a small one
     * @param clearance the least distance from the ship it enters at, px
     */
    public record DebrisSpec(
            double t,
            double x,
            String chunk,
            Hitbox size,
            double vx,
            double vy,
            double hp,
            double damage,
            double clearance) {
        /** A large chunk hits the ship at most once in this time. */
        public static final double HIT_INTERVAL_SECONDS = 1;

        public DebrisSpec {
            if (!(vy < 0)) {
                throw new IllegalArgumentException("a debris chunk drifts down the screen");
            }
        }

        /** Whether it is a large, indestructible chunk. */
        public boolean large() {
            return Double.isInfinite(hp);
        }

        /** Its sprite's name: {@code debris-large-a}. */
        public String sprite() {
            return "debris-" + chunk;
        }
    }
}
