package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * design/player/weapons/&lt;slug&gt;/data.yaml: one weapon with its five upgrade levels and the
 * overdrive pattern.
 *
 * @param price the purchase price; 0 for the starter
 * @param upgradeBase what the upgrade cost factors multiply; the price when absent
 * @param unlock the level from whose hangar visit the weapon is in the shop
 * @param draw the power draw at L1 and L5 in MW
 * @param hits which targets it can hit: {@code standard}, {@code homing}, {@code ground-only},
 *     {@code area} (proximity mines), {@code anti-sub} (torpedoes, M5 part E); M7 part A: {@code beam}
 *     (the Ion Beam: a continuous column, every level a {@code width} and a damage per second) and
 *     {@code deflector} (the Deflector Pod: no shots, every level a field); M8 part A: {@code depth-charge} (the
 *     Depth Charges: a barrel rolled out of the stern that bursts on its level's {@code fuse} for its damage in its
 *     {@code blast} radius)
 * @param speed the projectile speed in px/s; none for lobbed and dropped projectiles, a beam and a
 *     deflector
 * @param size the projectile's hit box; none for a beam and a deflector
 * @param range how far the projectiles fly (or a torpedo runs); none for mines, which have a
 *     lifetime instead; a homing weapon's (or a torpedo's) seek radius
 * @param lifetime how long a projectile lives, in seconds
 * @param converge degrees a pod's shots turn in towards the ship's centre line
 * @param fall seconds a dropped bomb falls to the ground
 * @param flight seconds a lobbed shell flies to its landing point
 * @param snap how far from its landing point a lobbed shell finds a ground target, px
 * @param cone the angle ahead in which a homing shot (or a torpedo) picks its target; a turret's forward cone,
 *     outside which a target wins when it is nearly as close as the nearest, degrees
 * @param accelerate seconds an accelerating shot takes from its start to its end speed
 * @param ports px either side of the muzzle the shots of a volley leave from, in turn (left first)
 * @param slew a turret's turn rate, °/s: the weapon aims its straight shots at the nearest enemy
 * @param drift seconds over which a mine's drift (at the speed) decays to nothing
 * @param arm seconds before a mine arms
 * @param trigger how close an enemy sets off an armed mine, px
 * @param mirrored fires the pattern to the left too (numbers per side)
 * @param pod a wing pod (numbers per pod)
 * @param seek homing, lobbed or dropped: every projectile counts as a single-target hit
 * @param splash M8 part A, a torpedo's (the Harpoon Torpedoes'): a hit blasts its level's {@code blast} radius for
 *     this share of its damage on the others there (not the struck target); none if not given
 * @param drone M7 part A: the Light Drone Bay's drone, which fires the levels' shots from its orbit;
 *     every level then has its {@code drone_hits} and {@code rebuild}
 * @param pick M9 part A: how a homing weapon's projectile picks its target ({@code rear-first}: the Swarm Tail's, the
 *     nearest enemy behind the ship's nose line first); the nearest in its cone when absent
 */
public record WeaponData(
        String name,
        Slot slot,
        List<String> traits,
        int price,
        Optional<Integer> upgradeBase,
        int unlock,
        Span draw,
        String sfx,
        String vfx,
        String hits,
        Optional<Speed> speed,
        Optional<Size> size,
        Optional<Range> range,
        Optional<Double> lifetime,
        Optional<Double> converge,
        Optional<Double> fall,
        Optional<Double> flight,
        Optional<Double> snap,
        Optional<Double> cone,
        Optional<Double> accelerate,
        Optional<Double> ports,
        Optional<Double> slew,
        Optional<Double> drift,
        Optional<Double> arm,
        Optional<Double> trigger,
        Optional<Boolean> mirrored,
        Optional<Boolean> pod,
        Optional<Boolean> seek,
        Optional<Double> splash,
        List<Level> levels,
        Level overdrive,
        Optional<Drone> drone,
        Optional<Pick> pick) {
    public WeaponData {
        Check.notNegative("price", price);
        Check.positive("unlock", unlock);
        Check.that(levels.size() == 5, "levels: one entry for each of L1–L5, found " + levels.size());
        upgradeBase.ifPresent(base -> Check.positive("upgrade_base", base));
        if (hits.equals(ARC)) {
            // M9 part A (design/player/weapons/tesla-coil-pod): an instant arc from a pod, no projectile; every level a
            // reach and a spark radius (blast), the sparks' share in splash, the forward arc in cone.
            Check.that(
                    pod.orElse(false)
                            && speed.isEmpty()
                            && size.isEmpty()
                            && range.isEmpty()
                            && lifetime.isEmpty()
                            && drone.isEmpty()
                            && splash.isPresent()
                            && cone.filter(degrees -> degrees > 0 && degrees <= 360)
                                    .isPresent(),
                    "an arc (hits: arc) is a wing pod with a cone (0-360) and a splash, and no speed, size, range,"
                            + " lifetime or drone");
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level -> level.kind() == Level.Kind.SHOTS
                                    && level.pattern().size() == 1
                                    && level.reach().filter(px -> px > 0).isPresent()
                                    && level.blast().filter(px -> px > 0).isPresent()),
                    "an arc (hits: arc): every level and the overdrive have one shot in its pattern, a rate, a damage,"
                            + " a reach and a blast (the sparks' radius)");
        } else if (hits.equals(BEAM) || hits.equals(DEFLECTOR)) {
            // M7 part A: a beam and a deflector fire no projectiles.
            Check.that(
                    speed.isEmpty() && size.isEmpty() && range.isEmpty() && lifetime.isEmpty() && drone.isEmpty(),
                    "a weapon with hits: " + hits + " has no speed, size, range, lifetime or drone");
            Level.Kind kind = hits.equals(BEAM) ? Level.Kind.BEAM : Level.Kind.FIELD;
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive)).allMatch(level -> level.kind() == kind),
                    hits.equals(BEAM)
                            ? "a beam (hits: beam): every level and the overdrive have a width and a damage per second"
                                    + " and no pattern or rate"
                            : "a deflector (hits: deflector): every level and the overdrive have a radius, charges"
                                    + " and a recharge and no pattern, rate or damage");
            Check.that(
                    !hits.equals(DEFLECTOR) || pod.orElse(false),
                    "a deflector (hits: deflector) is a wing pod (pod: true)");
        } else {
            Check.that(size.isPresent(), "size is required");
            // M8 part A: depth charges burst on a fuse per level instead.
            Check.that(
                    range.isPresent() || lifetime.isPresent() || hits.equals(DEPTH_CHARGE),
                    "range or lifetime is required");
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level -> level.kind() == Level.Kind.SHOTS),
                    "every level and the overdrive have a pattern, a rate and a damage (hits: " + hits + ")");
        }
        drone.ifPresent(numbers -> {
            Check.that(
                    hits.equals("standard") && pod.orElse(false),
                    "a drone bay (drone) fires standard shots from a wing pod (pod: true)");
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level -> level.droneHits().isPresent()
                                    && level.rebuild().isPresent()),
                    "a drone bay (drone): every level and the overdrive need drone_hits and rebuild");
        });
        if (drone.isEmpty()) {
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .noneMatch(level -> level.droneHits().isPresent()
                                    || level.rebuild().isPresent()),
                    "drone_hits and rebuild belong to a drone bay (drone)");
        }
        accelerate.ifPresent(seconds -> {
            Check.positive("accelerate", seconds);
            Check.that(speed.isPresent(), "accelerate needs a speed [start, end]");
        });
        slew.ifPresent(rate -> {
            Check.positive("slew", rate);
            Check.that(range.isPresent() && speed.isPresent(), "a turret (slew) needs a range and a speed");
        });
        if (hits.equals(MINES)) {
            Check.that(
                    speed.isPresent()
                            && lifetime.isPresent()
                            && drift.isPresent()
                            && arm.isPresent()
                            && trigger.isPresent(),
                    "mines (hits: area) need a speed, a lifetime, drift, arm and trigger");
            Check.positive("drift", drift.orElseThrow());
            Check.notNegative("arm", arm.orElseThrow());
            Check.positive("trigger", trigger.orElseThrow());
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level ->
                                    level.blast().isPresent() && level.maxLive().isPresent()),
                    "mines (hits: area): every level and the overdrive need a blast and max_live");
        }
        if (hits.equals(DEPTH_CHARGE)) {
            // M8 part A: rolled out at its speed, drifting to a hover, a fuse per level instead of a range.
            Check.that(
                    speed.filter(s -> s.start() == s.end()).isPresent()
                            && drift.isPresent()
                            && range.isEmpty()
                            && lifetime.isEmpty(),
                    "depth charges (hits: depth-charge) need one speed and a drift, and no range or lifetime");
            Check.positive("drift", drift.orElseThrow());
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level ->
                                    level.fuse().isPresent() && level.blast().isPresent()),
                    "depth charges (hits: depth-charge): every level and the overdrive need a fuse and a blast");
        } else {
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .noneMatch(level -> level.fuse().isPresent()),
                    "a fuse belongs to depth charges (hits: depth-charge)");
        }
        Check.that(
                hits.equals(ARC)
                        || Stream.concat(levels.stream(), Stream.of(overdrive))
                                .noneMatch(level -> level.reach().isPresent()),
                "a reach belongs to an arc (hits: arc)");
        pick.ifPresent(rule -> Check.that(
                hits.equals("homing") && slew.isEmpty(), "a pick rule belongs to a homing missile (hits: homing)"));
        splash.ifPresent(share -> {
            Check.share("splash", share);
            Check.that(
                    hits.equals(ARC)
                            || hits.equals(TORPEDO)
                                    && Stream.concat(levels.stream(), Stream.of(overdrive))
                                            .allMatch(level -> level.blast().isPresent()),
                    "a splash is a torpedo's (hits: anti-sub) or an arc's whose every level and overdrive has a blast"
                            + " radius");
        });
        if (hits.equals(TORPEDO)) {
            Check.that(
                    speed.isPresent()
                            && range.filter(r -> r.kind() == Range.Kind.DISTANCE)
                                    .isPresent()
                            && cone.isPresent()
                            && lifetime.isEmpty(),
                    "a torpedo (hits: anti-sub) needs a speed, a range in px and a cone, and no lifetime");
            Check.that(
                    speed.orElseThrow().start() == speed.orElseThrow().end() || accelerate.isPresent(),
                    "a torpedo (hits: anti-sub) that speeds up needs accelerate");
            Check.that(
                    cone.orElseThrow() > 0 && cone.orElseThrow() <= 360,
                    "a torpedo's cone (hits: anti-sub) is more than 0 and at most 360 degrees");
            Check.that(
                    Stream.concat(levels.stream(), Stream.of(overdrive))
                            .allMatch(level ->
                                    level.turn().filter(turn -> turn > 0).isPresent()),
                    "a torpedo (hits: anti-sub): every level and the overdrive need a turn");
        }
    }

    /**
     * M8 part A: the {@code hits} of the Depth Charges (design/player/weapons/depth-charges): a barrel rolled out of the
     * stern that bursts on its level's {@code fuse} for its damage on every target in its {@code blast} radius in the
     * water; idle with {@code NO WATER} in a dry level.
     */
    public static final String DEPTH_CHARGE = "depth-charge";

    /** M8 part A: the trait of the weapons that keep their full damage under water (design/world/europa). */
    public static final String ANTI_SUB = "anti-sub";

    /** M8 part A: whether it keeps its full damage under water: it has the {@value #ANTI_SUB} trait. */
    public boolean antiSub() {
        return traits.contains(ANTI_SUB);
    }

    /** The {@code hits} of proximity mines: they burst in a blast that reaches air, low-air and ground. */
    public static final String MINES = "area";

    /**
     * M5 part E: the {@code hits} of torpedoes, which run under the water and strike {@code sub} and
     * {@code ground} targets (design/player/weapons/torpedo-pod), only in a level over water.
     */
    public static final String TORPEDO = "anti-sub";

    /**
     * M7 part A: the {@code hits} of the Ion Beam (design/player/weapons/ion-beam): a continuous column
     * from the nose that damages the first target in it every step; each level a width and a damage
     * per second.
     */
    public static final String BEAM = "beam";

    /**
     * M9 part A: the {@code hits} of the Tesla Coil Pod (design/player/weapons/tesla-coil-pod): an instant arc from the
     * pod to the nearest enemy within its level's {@code reach} in its forward {@code cone}, with sparks ({@code
     * splash} of its damage within its level's {@code blast} radius); no projectile.
     */
    public static final String ARC = "arc";

    /** M9 part A: the trait whose hit empties an enemy shield at once (design/enemies, Enemy shields). */
    public static final String SHIELD_BREAKER = "shield-breaker";

    /** M9 part A: whether its hit empties an enemy shield at once (the {@value #SHIELD_BREAKER} trait). */
    public boolean shieldBreaker() {
        return traits.contains(SHIELD_BREAKER);
    }

    /**
     * M7 part A: the {@code hits} of the Deflector Pod (design/player/weapons/deflector-pod): no shots,
     * a field on the pod's side that catches enemy bullets; each level a radius, charges and a
     * recharge.
     */
    public static final String DEFLECTOR = "deflector";

    /** M7 part A: whether it is a beam ({@value #BEAM}). */
    public boolean beam() {
        return hits.equals(BEAM);
    }

    /** M7 part A: whether it is a deflector ({@value #DEFLECTOR}), which deals no damage. */
    public boolean deflector() {
        return hits.equals(DEFLECTOR);
    }

    /**
     * The damage per second of one level's volley (per side of a mirrored weapon, per pod): every
     * projectile of the pattern at the rate; M7 part A: a beam's damage per second, a deflector's 0.
     */
    public double volleyDps(Level level) {
        return switch (level.kind()) {
            case SHOTS -> level.pattern().size() * level.damage() * level.rate();
            case BEAM -> level.damage();
            case FIELD -> 0;
        };
    }

    /**
     * M7 part A, the Light Drone Bay's drone (design/player/wingmen, Light drone): its sprite {@code
     * size} and {@code hitbox}, its {@code orbit} radius round the ship's centre and the {@code
     * period} of one turn (clockwise), the {@code converge} point px ahead of the ship's centre its
     * shots aim at, the {@code fade_in} s of a rebuilt drone and what an {@code air} enemy's touch
     * does ({@code contact}). The hits it takes before it breaks and its rebuild time are per level.
     *
     * <p>M7 part G, the Rear-guard Drone Bay's drone (design/player/wingmen, Rear-guard drone): its {@code station}
     * {@code trail} instead of the orbit: it flies {@code behind} px behind the ship's centre and {@code side} px to its
     * bay's side, following the ship with a {@code lag} of that many s, and fires straight back ({@code fire: back});
     * an orbiting drone ({@code station: orbit}, the default) has its {@code orbit}, {@code period} and {@code
     * converge} instead.
     */
    public record Drone(
            Size size,
            Size hitbox,
            Optional<Double> orbit,
            Optional<Double> period,
            Optional<Double> converge,
            double fadeIn,
            Contact contact,
            Optional<Station> station,
            Optional<Double> behind,
            Optional<Double> side,
            Optional<Double> lag,
            Optional<Fire> fire) {
        public Drone {
            orbit.ifPresent(r -> Check.positive("orbit", r));
            period.ifPresent(t -> Check.positive("period", t));
            converge.ifPresent(d -> Check.positive("converge", d));
            Check.notNegative("fade_in", fadeIn);
            behind.ifPresent(d -> Check.positive("behind", d));
            side.ifPresent(d -> Check.notNegative("side", d));
            lag.ifPresent(t -> Check.notNegative("lag", t));
            boolean trails = station.orElse(Station.ORBIT) == Station.TRAIL;
            Check.that(
                    trails || (orbit.isPresent() && period.isPresent() && converge.isPresent()),
                    "an orbiting drone has its orbit, period and converge");
            Check.that(
                    trails || (behind.isEmpty() && side.isEmpty() && lag.isEmpty() && fire.isEmpty()),
                    "behind, side, lag and fire are a trailing drone's (station: trail)");
            Check.that(
                    !trails || (behind.isPresent() && side.isPresent() && lag.isPresent() && fire.isPresent()),
                    "a trailing drone has its behind, side, lag and fire");
            Check.that(
                    !trails || (orbit.isEmpty() && period.isEmpty() && converge.isEmpty()),
                    "orbit, period and converge are an orbiting drone's");
        }

        /** M7 part G: whether it trails the ship ({@code station: trail}) rather than orbiting it. */
        public boolean trails() {
            return station.orElse(Station.ORBIT) == Station.TRAIL;
        }
    }

    /** M7 part G: where a drone flies: round the ship ({@code orbit}, the light drone) or behind it ({@code trail}). */
    public enum Station {
        ORBIT,
        TRAIL
    }

    /** M7 part G: where a trailing drone fires: straight back (180°, the rear-guard drone). */
    public enum Fire {
        BACK
    }

    /**
     * What an {@code air} enemy touching a drone does: it costs the drone {@code hits} ({@code
     * small_hits} for a {@code tiny} or {@code small} rammer, which is destroyed) and the enemy takes
     * {@code damage}; a {@code laser} touch costs {@code laser_hits}.
     */
    public record Contact(int hits, int smallHits, int laserHits, double damage) {
        public Contact {
            Check.positive("hits", hits);
            Check.positive("small_hits", smallHits);
            Check.positive("laser_hits", laserHits);
            Check.notNegative("damage", damage);
        }
    }

    /** M9 part A: how a homing projectile picks its target (design/player/weapons/swarm-tail). */
    public enum Pick {
        /** The nearest enemy in its cone and seek radius (every homing weapon before the Swarm Tail). */
        @JsonProperty("nearest")
        NEAREST,
        /** The nearest enemy behind the ship's nose line within the seek radius first, else the nearest anywhere. */
        @JsonProperty("rear-first")
        REAR_FIRST
    }

    public enum Slot {
        FRONT,
        REAR,
        WING
    }

    /** Projectile speed in px/s, written as one number or {@code [start, end]} for accelerating shots. */
    public record Speed(double start, double end) {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static Speed of(Object value) {
            if (value instanceof Number n) {
                return new Speed(n.doubleValue(), n.doubleValue());
            }
            if (value instanceof List<?> list
                    && list.size() == 2
                    && list.get(0) instanceof Number start
                    && list.get(1) instanceof Number end) {
                return new Speed(start.doubleValue(), end.doubleValue());
            }
            throw new IllegalArgumentException("speed must be px/s or [start, end], was " + value);
        }
    }

    /** How far a weapon's projectiles fly: to the screen edge, straight down, or a distance. */
    public record Range(Kind kind, double px) {
        public enum Kind {
            SCREEN,
            DROP,
            DISTANCE
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static Range of(Object value) {
            return switch (value) {
                case Number n -> new Range(Kind.DISTANCE, Check.positive("range", n.doubleValue()));
                case String s when s.equals("screen") -> new Range(Kind.SCREEN, 0);
                case String s when s.equals("drop") -> new Range(Kind.DROP, 0);
                default -> throw new IllegalArgumentException("range must be screen, drop or px, was " + value);
            };
        }
    }

    /**
     * One upgrade level: shots (a pattern at a rate), or (M7 part A) a beam's width and damage per
     * second, or a deflector's field.
     *
     * @param pattern one shot per projectile of a volley; empty for a beam and a field
     * @param rate volleys per second; 0 for a beam and a field
     * @param damage damage per projectile; a beam's damage per second; 0 for a field
     * @param blast blast radius in px
     * @param turn homing turn rate in °/s
     * @param maxLive most mines alive at once
     * @param width M7 part A: a beam's hit width and drawn core, px
     * @param radius M7 part A: a deflector's field radius round the ship's centre, px
     * @param charges M7 part A: the bullets a deflector catches before it is empty
     * @param recharge M7 part A: seconds until a deflector gets one charge back
     * @param droneHits M7 part A: the hits a drone takes before it breaks
     * @param rebuild M7 part A: seconds the bay takes to build a new drone
     * @param fuse M8 part A: seconds from a depth charge's launch to its burst
     * @param reach M9 part A: an arc's reach from its pod, px (hits: arc)
     */
    public record Level(
            List<Shot> pattern,
            double rate,
            double damage,
            Optional<Integer> pierce,
            Optional<Double> blast,
            Optional<Double> turn,
            Optional<Integer> maxLive,
            Optional<Double> width,
            Optional<Double> radius,
            Optional<Integer> charges,
            Optional<Double> recharge,
            Optional<Integer> droneHits,
            Optional<Double> rebuild,
            Optional<Double> fuse,
            Optional<Double> reach) {
        /** A level without an arc's reach (the levels before M9 part A). */
        public Level(
                List<Shot> pattern,
                double rate,
                double damage,
                Optional<Integer> pierce,
                Optional<Double> blast,
                Optional<Double> turn,
                Optional<Integer> maxLive,
                Optional<Double> width,
                Optional<Double> radius,
                Optional<Integer> charges,
                Optional<Double> recharge,
                Optional<Integer> droneHits,
                Optional<Double> rebuild,
                Optional<Double> fuse) {
            this(
                    pattern,
                    rate,
                    damage,
                    pierce,
                    blast,
                    turn,
                    maxLive,
                    width,
                    radius,
                    charges,
                    recharge,
                    droneHits,
                    rebuild,
                    fuse,
                    Optional.empty());
        }

        /** A level without a depth charge's fuse (the levels before M8 part A). */
        public Level(
                List<Shot> pattern,
                double rate,
                double damage,
                Optional<Integer> pierce,
                Optional<Double> blast,
                Optional<Double> turn,
                Optional<Integer> maxLive,
                Optional<Double> width,
                Optional<Double> radius,
                Optional<Integer> charges,
                Optional<Double> recharge,
                Optional<Integer> droneHits,
                Optional<Double> rebuild) {
            this(
                    pattern,
                    rate,
                    damage,
                    pierce,
                    blast,
                    turn,
                    maxLive,
                    width,
                    radius,
                    charges,
                    recharge,
                    droneHits,
                    rebuild,
                    Optional.empty(),
                    Optional.empty());
        }

        /** What a level is: a volley of shots, a beam or a deflector's field. */
        public enum Kind {
            SHOTS,
            BEAM,
            FIELD
        }

        public Level {
            pattern = List.copyOf(pattern);
            width.ifPresent(px -> Check.positive("width", px));
            radius.ifPresent(px -> Check.positive("radius", px));
            charges.ifPresent(count -> Check.positive("charges", count));
            recharge.ifPresent(seconds -> Check.positive("recharge", seconds));
            droneHits.ifPresent(count -> Check.positive("drone_hits", count));
            rebuild.ifPresent(seconds -> Check.positive("rebuild", seconds));
            fuse.ifPresent(seconds -> Check.positive("fuse", seconds));
            reach.ifPresent(px -> Check.positive("reach", px));
            boolean field = radius.isPresent() || charges.isPresent() || recharge.isPresent();
            if (width.isPresent()) {
                Check.that(
                        pattern.isEmpty() && rate == 0 && !field,
                        "a beam level has a width and a damage per second, no pattern, rate or field");
                Check.positive("damage", damage);
            } else if (field) {
                Check.that(
                        radius.isPresent() && charges.isPresent() && recharge.isPresent(),
                        "a deflector level needs a radius, charges and a recharge");
                Check.that(
                        pattern.isEmpty() && rate == 0 && damage == 0,
                        "a deflector level has no pattern, rate or damage");
            } else {
                Check.notEmpty("pattern", pattern);
                Check.positive("rate", rate);
                Check.positive("damage", damage);
            }
        }

        /**
         * Reads a level from its data: the pattern, rate and damage are optional here so a beam level
         * (width, damage) and a field level (radius, charges, recharge) can leave them out; the
         * constructor checks what each kind needs.
         */
        @JsonCreator
        static Level of(
                @JsonProperty("pattern") Optional<List<Shot>> pattern,
                @JsonProperty("rate") Optional<Double> rate,
                @JsonProperty("damage") Optional<Double> damage,
                @JsonProperty("pierce") Optional<Integer> pierce,
                @JsonProperty("blast") Optional<Double> blast,
                @JsonProperty("turn") Optional<Double> turn,
                @JsonProperty("max_live") Optional<Integer> maxLive,
                @JsonProperty("width") Optional<Double> width,
                @JsonProperty("radius") Optional<Double> radius,
                @JsonProperty("charges") Optional<Integer> charges,
                @JsonProperty("recharge") Optional<Double> recharge,
                @JsonProperty("drone_hits") Optional<Integer> droneHits,
                @JsonProperty("rebuild") Optional<Double> rebuild,
                @JsonProperty("fuse") Optional<Double> fuse,
                @JsonProperty("reach") Optional<Double> reach) {
            return new Level(
                    pattern.orElse(List.of()),
                    rate.orElse(0.0),
                    damage.orElse(0.0),
                    pierce,
                    blast,
                    turn,
                    maxLive,
                    width,
                    radius,
                    charges,
                    recharge,
                    droneHits,
                    rebuild,
                    fuse,
                    reach);
        }

        /** Whether it fires shots, burns a beam or holds a deflector's field. */
        public Kind kind() {
            if (width.isPresent()) {
                return Kind.BEAM;
            }
            return radius.isPresent() ? Kind.FIELD : Kind.SHOTS;
        }
    }

    /**
     * A projectile of a volley, written {@code [x, angle]}: x offset from the muzzle in px, angle in
     * degrees (0 = straight up the screen, 90 = right, 180 = straight back).
     */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record Shot(double x, double angle) {}
}
