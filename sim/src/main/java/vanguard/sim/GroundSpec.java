package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * What lies on a level's ground layer (refactoring step LS-3, {@link LevelScript#ground()}): the road, water, the
 * canyon's terrain, the tendril gates, the ground where a Burrower never surfaces and the conveyor belts.
 *
 * @param road the road on the ground layer that a convoy follows
 * @param water M5 part E (user decision E1 = a): the whole play field is water (Levels 11–13): the torpedo ({@link
 *     WeaponSpec.Delivery#TORPEDO}) runs only here; elsewhere its mount fires nothing
 * @param terrain M7 part C: the canyon's walls and rock islands and what touching them does (Level 16); empty for none
 * @param gates M7 part C: the tendril gates' anchor pairs (Level 16), each anchor also one of the level's ground units
 *     ({@link LevelScript#withGates}); empty for none
 * @param noSurface M7 part B: the ground where a Burrower never surfaces (design/campaign Level 15: the station's pads,
 *     the hab, the rail line); empty for none. Not with holds or an arena.
 * @param belts M7 part F: the conveyor belts on the ground (Level 19), which carry the ground units standing or walking
 *     on them; empty for none
 * @param none M9 part C (design/campaign Level 30, {@code ground: none}): the level has no ground layer at all (space
 *     only): the bomb deliveries ({@link WeaponSpec.Delivery#needsGround()}) fire nothing there and read {@code NO
 *     GROUND} ({@link PlayerFire#idleReason}); no road, water, terrain, gates, no-surface zones or belts with it
 */
public record GroundSpec(
        Optional<Road> road,
        boolean water,
        Optional<TerrainSpec> terrain,
        List<Gate> gates,
        List<NoSurface> noSurface,
        List<Belt> belts,
        boolean none) {
    /** Plain land: no road, terrain, gates, no-surface zones or belts. */
    public static final GroundSpec NONE =
            new GroundSpec(Optional.empty(), false, Optional.empty(), List.of(), List.of(), List.of());

    /** M9 part C: no ground layer at all (Level 30, space only). */
    public static final GroundSpec SPACE =
            new GroundSpec(Optional.empty(), false, Optional.empty(), List.of(), List.of(), List.of(), true);

    public GroundSpec {
        gates = List.copyOf(gates);
        noSurface = List.copyOf(noSurface);
        belts = List.copyOf(belts);
        if (none
                && (road.isPresent()
                        || water
                        || terrain.isPresent()
                        || !gates.isEmpty()
                        || !noSurface.isEmpty()
                        || !belts.isEmpty())) {
            throw new IllegalArgumentException("a level without a ground layer has nothing on it (M9 part C)");
        }
    }

    /** A level with a ground layer (every level before M9 part C). */
    public GroundSpec(
            Optional<Road> road,
            boolean water,
            Optional<TerrainSpec> terrain,
            List<Gate> gates,
            List<NoSurface> noSurface,
            List<Belt> belts) {
        this(road, water, terrain, gates, noSurface, belts, false);
    }

    /** These ground with another {@code road}. */
    public GroundSpec withRoad(Optional<Road> road) {
        return new GroundSpec(road, water, terrain, gates, noSurface, belts, none);
    }

    /** These ground with another {@code water}. */
    public GroundSpec withWater(boolean water) {
        return new GroundSpec(road, water, terrain, gates, noSurface, belts, none);
    }

    /** These ground with another {@code terrain}. */
    public GroundSpec withTerrain(Optional<TerrainSpec> terrain) {
        return new GroundSpec(road, water, terrain, gates, noSurface, belts, none);
    }

    /** These ground with another {@code noSurface}. */
    public GroundSpec withNoSurface(List<NoSurface> noSurface) {
        return new GroundSpec(road, water, terrain, gates, noSurface, belts, none);
    }

    /** These ground with another {@code belts}. */
    public GroundSpec withBelts(List<Belt> belts) {
        return new GroundSpec(road, water, terrain, gates, noSurface, belts, none);
    }

    /**
     * M7 part C, a tendril gate's anchor pair (design/enemies/ground/tendril-anchor; design/campaign Level 16,
     * Gates): two {@code anchor} units entering at the top edge at {@code t} at {@code leftX} and {@code
     * rightX}, scrolling with the ground at the walls' foot, and the tendril of the anchor's {@link
     * EnemySpec.Link} between their centres. A gate of several pairs at different heights is several of these.
     */
    public record Gate(double t, double leftX, double rightX, EnemySpec anchor) {
        public Gate {
            if (anchor.link().isEmpty()) {
                throw new IllegalArgumentException(anchor.slug() + " spans no tendril: a gate's anchor has a link");
            }
            if (!(leftX < rightX) || !(t >= 0)) {
                throw new IllegalArgumentException(
                        "a gate's left anchor is left of its right one: " + leftX + ", " + rightX);
            }
        }
    }

    /**
     * M7 part F, a conveyor belt on the ground (design/campaign Level 19, Conveyors): a column {@code width} px wide
     * centred on {@code x}, from ground distance {@code from} to {@code to} (a ground point at screen height y, y
     * up, lies at distance {@code groundScroll + y - PlayField.HEIGHT / 2}, as on a {@link Road}), running up the
     * screen at {@code speed} px/s relative to the ground (negative: down). A ground unit whose centre stands on it
     * rides it; the ship, Rook, pickups and bullets fly above it.
     */
    public record Belt(double x, double width, double from, double to, double speed) {
        public Belt {
            if (!(width > 0) || !(to > from) || !Double.isFinite(speed) || !Double.isFinite(x)) {
                throw new IllegalArgumentException("a belt has a width, a stretch of ground and a speed");
            }
        }

        /** Whether a ground point at (px, py), the ground scrolled {@code groundScroll} px, lies on it. */
        public boolean carries(double px, double py, double groundScroll) {
            double distance = groundScroll + py - PlayField.HEIGHT / 2.0;
            return Math.abs(px - x) <= width / 2 && distance >= from && distance <= to;
        }
    }

    /**
     * M7 part C (design/campaign Level 16, Terrain; M1 = a): the canyon's rock and what touching it does.
     *
     * @param terrain the walls' collision lines and the rock islands
     * @param damage what a touch deals the ship (shield first, the overflow to armour) and Rook (his
     *     armour): {@code heavy} 10, easy {@code medium} 6
     * @param push how far a touch pushes the hull back out of the rock along its normal, px (28)
     * @param pushSeconds over how long (0.12 s)
     * @param cooldownSeconds after a touch, how long further contact only clamps the hull, without
     *     damage or push (0.5 s)
     */
    public record TerrainSpec(Terrain terrain, double damage, double push, double pushSeconds, double cooldownSeconds) {
        public TerrainSpec {
            java.util.Objects.requireNonNull(terrain);
            if (!(damage >= 0) || !(push >= 0) || !(pushSeconds > 0) || !(cooldownSeconds >= 0)) {
                throw new IllegalArgumentException("a wall contact has a damage, a push over some time and a cooldown");
            }
        }
    }

    /**
     * M7 part B, a no-surface zone (design/campaign Level 15: the station's pads and hab, the rail line):
     * the ground that crosses the top edge between the level times {@code from} and {@code to} s, from
     * {@code left} to {@code right} px across; it scrolls with the ground. A Burrower never surfaces in one
     * ({@link EnemySpec.SurfaceSpot}).
     */
    public record NoSurface(double from, double to, double left, double right) {
        public NoSurface {
            if (!(from >= 0) || !(to > from) || !(right > left)) {
                throw new IllegalArgumentException("a no-surface zone spans a time and a width");
            }
        }
    }
}
