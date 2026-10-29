package vanguard.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import vanguard.sim.Branch;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M9 part E (design/campaign Level 32, The forks): the route the {@link Autopilot} takes through a level's forks. A
 * route is a side per fork, {@code L} or {@code R}, fork one first ("LR": fork one's left channel, fork two's right);
 * {@link #NONE} (the empty route) leaves the forks to the autopilot's own {@link Autopilot.Channel} (the left channel
 * by default: "LL"). A route is fixed on a sortie ({@link #fly}) and holds for its whole flight, retries included, so
 * flights in parallel each fly their own; the tests, the level report ({@code levelReport -Proute=LR}; without it the
 * report flies every route) and the campaign playthrough ({@link #PLAYTHROUGH}) fix it. The choice itself is the
 * simulation's (the ship's side when an island's tip reaches its row): {@link HazardPlay#canyon} takes the route's side
 * of a fork from {@link #AHEAD} rows before its tip, so the ship is in that channel when the choice falls. The
 * per-route counts ({@link LevelMetrics#density}, {@link TypicalHaul}) leave out the branches a route does not fly.
 */
public final class RoutePlay {
    /** No route fixed: the autopilot's own channel at every fork. */
    public static final String NONE = "";

    /**
     * The route the campaign playthrough ({@code ActPlaythroughTest}) flies a forked level on: the mixed route (one
     * channel safer, one richer) whose typical haul lies nearest the routes' mean, fixed so the playthrough's credits
     * do not move with a data edit (Level 32: RR, the charge seam and the ore chute, +0.9 % of the mean; LL −0.9 %).
     * {@code Level32Test} checks it is still the nearest.
     */
    public static final Map<String, String> PLAYTHROUGH = Map.of("act-5-the-belt/level-32-deep-core", "RR");

    /** How far above the ship's row a fork's island tip makes the route's side the channel, rows (5.4 s at 130). */
    static final double AHEAD = 700;

    private static final Map<Sortie, String> ROUTES = Collections.synchronizedMap(new WeakHashMap<>());

    private RoutePlay() {}

    /** Fixes {@code route} on {@code sortie} for its whole flight ({@link #NONE}: the autopilot's own); returns it. */
    public static Sortie fly(Sortie sortie, String route) {
        check(route);
        if (route.isEmpty()) {
            ROUTES.remove(sortie);
        } else {
            ROUTES.put(sortie, route);
        }
        return sortie;
    }

    /** The route fixed on {@code sortie}, {@link #NONE} without one. */
    public static String route(Sortie sortie) {
        String route = ROUTES.get(sortie);
        return route == null ? NONE : route;
    }

    /** The level's forks (0 for a level without them). */
    public static int forks(LevelScript level) {
        return level.ground().terrain().map(terrain -> terrain.forks().size()).orElse(0);
    }

    /** Every route through the level, fork one's side first (LL, LR, RL, RR for two forks); {@link #NONE} alone without forks. */
    public static List<String> routes(LevelScript level) {
        List<String> routes = List.of(NONE);
        for (int f = 0; f < forks(level); f++) {
            List<String> next = new ArrayList<>();
            for (String route : routes) {
                next.add(route + "L");
                next.add(route + "R");
            }
            routes = next;
        }
        return List.copyOf(routes);
    }

    /** Every route through level {@code key} as medium builds it (its forks are the same on every difficulty). */
    static List<String> routes(Content content, String key) {
        return routes(SimSpecs.level(content, key, Difficulty.MEDIUM));
    }

    /** The route the playthrough flies level {@code key} on: {@link #PLAYTHROUGH}'s, {@link #NONE} for the others. */
    public static String playthrough(String key) {
        return PLAYTHROUGH.getOrDefault(key, NONE);
    }

    /** Whether a wave, unit or object of {@code branch} flies on {@code route} (always without a branch or a route). */
    static boolean on(String route, Optional<Branch> branch) {
        if (route.isEmpty() || branch.isEmpty()) {
            return true;
        }
        int fork = branch.get().fork();
        return fork < route.length()
                && (route.charAt(fork) == 'L') == (branch.get().side() == Branch.LEFT);
    }

    /**
     * The channel the autopilot takes at its row {@code row} now: the route's side of the last fork whose island's tip
     * lies at most {@link #AHEAD} rows above it (fork f is island f), else {@code channel}.
     */
    static Autopilot.Channel channel(Sortie sortie, Autopilot.Channel channel, double row) {
        if (sortie.forkCount() == 0) {
            return channel;
        }
        String route = route(sortie);
        Terrain terrain = sortie.terrain();
        if (route.isEmpty() || terrain == null) {
            return channel;
        }
        int fork = -1;
        for (int f = 0; f < Math.min(sortie.forkCount(), terrain.islands()); f++) {
            if (terrain.islandTip(f) <= row + AHEAD) {
                fork = f;
            }
        }
        if (fork < 0 || fork >= route.length()) {
            return channel;
        }
        return route.charAt(fork) == 'L' ? Autopilot.Channel.LEFT : Autopilot.Channel.RIGHT;
    }

    /** The route a sortie flew: a side per fork chosen, '-' for one not reached. */
    public static String flown(Sortie sortie) {
        StringBuilder flown = new StringBuilder();
        for (int f = 0; f < sortie.forkCount(); f++) {
            int side = sortie.forkSide(f);
            flown.append(side < 0 ? '-' : side == Branch.LEFT ? 'L' : 'R');
        }
        return flown.toString();
    }

    private static void check(String route) {
        if (!route.matches("[LR]*")) {
            throw new IllegalArgumentException("a route is a side per fork, L or R: " + route);
        }
    }
}
