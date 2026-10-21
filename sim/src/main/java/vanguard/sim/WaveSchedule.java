package vanguard.sim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The level's waves planned unit by unit ({@link Formations}), in the order they enter, and the
 * edge warnings for the waves that enter from the sides or the rear (design/enemies, bullet
 * readability rules: an arrow at that edge at least 3 s ahead). Only a wave's first entry warns: a
 * unit's own re-entry (a chain's or a swarm's loop-back, a rear ambush's pop-up behind the ship) is
 * its flight pattern and gets none (2026-10-21, user). Built once per level; an attempt walks
 * through it with a cursor.
 *
 * <p>M7 part G (design/campaign Level 20, Reverse scroll; defaults pending the user's review): the units of a wave
 * whose time falls in a reversed scroll ({@link Sortie#travelAt}{@code < 0}) are <em>flipped</em> ({@link
 * TravelFrame}): planned as ever, they live in the mirrored frame, so a front entry comes in at the bottom edge and a
 * rear one at the top. The edges warn by the screen: a flipped wave from the rear and a flipped cross unit from beyond
 * the frame's bottom warn {@link WarningEdge#TOP}; a flipped front wave warns {@link WarningEdge#BOTTOM} only when it
 * carries a {@code warningSeconds} (Level 20's lessons). A
 * walker with frontal armour ({@code frontArc}) and a segment chain are never flipped, nor a pursuit, which enters at the
 * top edge under a TOP warning whatever the scroll's direction.
 */
final class WaveSchedule {
    /** The shortest edge warning. */
    static final double EDGE_WARNING_SECONDS = 3;

    private final List<EnemySpec> kinds;
    private final Spawn[] spawns;
    /** Per planned unit, the edge its wave enters from. */
    private final WaveSpec.Entry[] entries;
    /**
     * Per planned unit, its wave's volley clock (M5 part B: a walker wave with a fan stagger, design/
     * enemies/ground/creeper) and its place in the wave; -1 and 0 for a unit without one.
     */
    private final int[] volleyGroups;

    private final int[] volleyUnits;
    /** M5 part C: per planned unit, its wave's tag ("" for none). */
    private final String[] tags;
    /** The number of volley clocks: one per staggered walker wave. */
    private final int volleyClocks;
    /** M7 part B: per planned unit, its place in its wave (from 0, in planning order). */
    private final int[] places;
    /** M7 part D: per planned unit, the number of units its wave planned (a bomb run's slots). */
    private final int[] waveSizes;
    /** M7 part B: per planned unit, its wave's burrow stagger clock (a Burrower wave's); -1 for none. */
    private final int[] burrowGroups;
    /** M7 part B: the number of burrow stagger clocks: one per Burrower wave. */
    private final int burrowClocks;

    /** M7 part G: per planned unit, whether it is flipped (its wave's time falls in a reversed scroll). */
    private final boolean[] flips;

    private final int[] warningStarts;
    private final int[] warningEnds;
    private final int[] warningEdges;
    private int next;

    /** @param kinds the level's distinct enemies, every wave's among them; a spawn's kind indexes it */
    WaveSchedule(List<WaveSpec> waves, List<EnemySpec> kinds, SplitMix64 rng) {
        this(waves, kinds, rng, wave -> java.util.Optional.empty());
    }

    /**
     * @param spans M7 part C: per wave, the canyon's flyable span at its entry row ({@link
     *     Formations.Span}); empty for the whole field (every wave of a level without terrain)
     */
    WaveSchedule(
            List<WaveSpec> waves,
            List<EnemySpec> kinds,
            SplitMix64 rng,
            java.util.function.Function<WaveSpec, java.util.Optional<Formations.Span>> spans) {
        this(waves, kinds, rng, spans, t -> false);
    }

    /**
     * @param reversedAt M7 part G: whether the scroll runs reversed at a level time, s ({@link Sortie#travelAt}{@code
     *     < 0}); a wave at such a time is flipped
     */
    WaveSchedule(
            List<WaveSpec> waves,
            List<EnemySpec> kinds,
            SplitMix64 rng,
            java.util.function.Function<WaveSpec, java.util.Optional<Formations.Span>> spans,
            java.util.function.DoublePredicate reversedAt) {
        this.kinds = kinds;
        List<Spawn> planned = new ArrayList<>();
        List<WaveSpec> warned = new ArrayList<>();
        List<Integer> warnedEdges = new ArrayList<>();
        Map<Spawn, WaveSpec.Entry> entryOf = new IdentityHashMap<>();
        Map<Spawn, int[]> volleyOf = new IdentityHashMap<>();
        Map<Spawn, String> tagOf = new IdentityHashMap<>();
        Map<Spawn, int[]> placeOf = new IdentityHashMap<>();
        Map<Spawn, Boolean> flipOf = new IdentityHashMap<>();
        int clocks = 0;
        int burrows = 0;
        for (WaveSpec wave : waves) {
            int before = planned.size();
            Formations.plan(wave, kinds.indexOf(wave.enemy()), rng, planned, spans.apply(wave));
            boolean cross = wave.formation() == WaveSpec.Formation.CROSS;
            boolean flipped = flips(wave) && reversedAt.test(wave.t());
            int crossEdges = 0;
            for (int i = before; i < planned.size(); i++) {
                // M5 part G: a cross's unit enters from the edge its path (its lane) starts beyond.
                int edge = cross ? startEdge(planned.get(i).path()) : 0;
                // M7 part G: a flipped unit's edges are the frame's: its frame's bottom is the screen's top.
                crossEdges |= flipped ? onScreen(edge) : edge;
                if (flipped) {
                    flipOf.put(planned.get(i), Boolean.TRUE);
                }
                entryOf.put(planned.get(i), cross ? entryAt(edge) : wave.entry());
                tagOf.put(planned.get(i), wave.tag());
                placeOf.put(planned.get(i), new int[] {
                    i - before, wave.enemy().burrow().isPresent() ? burrows : -1, planned.size() - before
                });
            }
            if (wave.enemy().burrow().isPresent()) {
                burrows++;
            }
            if (staggered(wave)) {
                for (int i = before; i < planned.size(); i++) {
                    volleyOf.put(planned.get(i), new int[] {clocks, i - before});
                }
                clocks++;
            }
            // M5 part D: a rear ambush enters at the top; its re-entry behind the ship is its flight: no warning.
            if (wave.formation() == WaveSpec.Formation.PURSUIT) {
                // M7 part G: a pursuit comes in at the top edge whatever the scroll's direction (screen space).
                warned.add(wave);
                warnedEdges.add(WarningEdge.TOP.bit());
            } else if (cross) {
                if (crossEdges != 0) {
                    warned.add(wave);
                    warnedEdges.add(crossEdges);
                }
            } else if (wave.entry() != WaveSpec.Entry.FRONT && wave.formation() != WaveSpec.Formation.REAR_AMBUSH) {
                warned.add(wave);
                warnedEdges.add(flipped ? onScreen(edges(wave)) : edges(wave));
            } else if (flipped
                    && wave.entry() == WaveSpec.Entry.FRONT
                    && wave.warningSeconds().isPresent()) {
                // M7 part G: a front wave of a reversed scroll comes in at the bottom; warned only as a lesson.
                warned.add(wave);
                warnedEdges.add(WarningEdge.BOTTOM.bit());
            }
        }
        planned.sort(Comparator.comparingInt(Spawn::tick));
        spawns = planned.toArray(Spawn[]::new);
        entries = new WaveSpec.Entry[spawns.length];
        volleyGroups = new int[spawns.length];
        volleyUnits = new int[spawns.length];
        tags = new String[spawns.length];
        places = new int[spawns.length];
        waveSizes = new int[spawns.length];
        burrowGroups = new int[spawns.length];
        flips = new boolean[spawns.length];
        for (int i = 0; i < spawns.length; i++) {
            flips[i] = flipOf.containsKey(spawns[i]);
            int[] place = placeOf.getOrDefault(spawns[i], new int[] {0, -1, 1});
            places[i] = place[0];
            waveSizes[i] = place[2];
            burrowGroups[i] = place[1];
            tags[i] = tagOf.getOrDefault(spawns[i], "");
            entries[i] = entryOf.getOrDefault(spawns[i], WaveSpec.Entry.FRONT);
            int[] volley = volleyOf.getOrDefault(spawns[i], new int[] {-1, 0});
            volleyGroups[i] = volley[0];
            volleyUnits[i] = volley[1];
        }
        volleyClocks = clocks;
        burrowClocks = burrows;
        // 2026-10-21 (user): a unit's own re-entry (a chain's or a swarm's loop-back, a rear ambush's pop-up behind the
        // ship) is its flight pattern, not a wave's entry: it gets no edge warning (nor one after the unit's kill).
        int warnings = warned.size();
        warningStarts = new int[warnings];
        warningEnds = new int[warnings];
        warningEdges = new int[warnings];
        for (int i = 0; i < warned.size(); i++) {
            WaveSpec wave = warned.get(i);
            double lead = Math.max(EDGE_WARNING_SECONDS, wave.warningSeconds().orElse(0.0));
            warningStarts[i] = SimStep.ticks(wave.t() - lead);
            warningEnds[i] = SimStep.ticks(wave.t());
            warningEdges[i] = warnedEdges.get(i);
        }
    }

    /**
     * M7 part G: whether a wave's units may be flipped: not a walker with frontal armour (its glance arc reads the
     * screen's shot directions), a segment chain (its members are placed by its own frame's history), a pursuit or a
     * diver.
     */
    private static boolean flips(WaveSpec wave) {
        EnemySpec enemy = wave.enemy();
        boolean armoured = enemy.walker().isPresent() && enemy.walker().get().frontArc() > 0;
        // M7 part G: nor a pursuit: its flock flies in screen space (its band below the top edge).
        // 2026-10-21 (user): "when flying backwards, the stingers should still enter from the top": a diver (the
        // Stinger) flies as in a forward level, in from the top and diving at the ship; its warning a top/front one.
        return !armoured
                && enemy.chain().isEmpty()
                && enemy.dive().isEmpty()
                && wave.formation() != WaveSpec.Formation.PURSUIT;
    }

    /** M7 part G: a flipped unit's frame edges as the screen's: its frame's bottom is the screen's top. */
    private static int onScreen(int frameEdges) {
        return WarningEdge.BOTTOM.in(frameEdges)
                ? (frameEdges & ~WarningEdge.BOTTOM.bit()) | WarningEdge.TOP.bit()
                : frameEdges;
    }

    /**
     * Whether a wave's units share a staggered volley clock (a walker with a fan stagger); its last
     * unit's turn must come before the next volley, so a volley never overlaps the next.
     */
    private static boolean staggered(WaveSpec wave) {
        EnemySpec enemy = wave.enemy();
        if (enemy.walker().isEmpty()
                || !enemy.walker().get().staggered()
                || enemy.gun().isEmpty()) {
            return false;
        }
        double stagger = enemy.walker().get().staggerSeconds();
        double interval = enemy.gun().get().intervalSeconds();
        if (SimStep.ticks((wave.count() - 1) * stagger) >= SimStep.ticks(interval)) {
            throw new IllegalArgumentException(enemy.slug() + ": a wave of " + wave.count() + " staggered " + stagger
                    + " s apart does not fit its " + interval + " s volley interval");
        }
        return true;
    }

    /**
     * M5 part G: the edge a path starts beyond, as a {@link WarningEdge} bit: the left or right edge, the
     * bottom one, or 0 for the top edge (or a start inside the play field).
     */
    static int startEdge(FlightPath path) {
        if (path.startX() < 0) {
            return WarningEdge.LEFT.bit();
        }
        if (path.startX() > PlayField.WIDTH) {
            return WarningEdge.RIGHT.bit();
        }
        return path.startY() < 0 ? WarningEdge.BOTTOM.bit() : 0;
    }

    /** M5 part G: the entry a unit starting beyond {@code edge} ({@link #startEdge(FlightPath)}) has. */
    private static WaveSpec.Entry entryAt(int edge) {
        if (edge == WarningEdge.BOTTOM.bit()) {
            return WaveSpec.Entry.REAR;
        }
        return edge == 0 ? WaveSpec.Entry.FRONT : WaveSpec.Entry.SIDES;
    }

    private static int edges(WaveSpec wave) {
        if (wave.entry() == WaveSpec.Entry.REAR) {
            return WarningEdge.BOTTOM.bit();
        }
        return switch (wave.edge()) {
            case LEFT -> WarningEdge.LEFT.bit();
            case RIGHT -> WarningEdge.RIGHT.bit();
            case NONE, ALTERNATING, TOP, BOTTOM -> WarningEdge.LEFT.bit() | WarningEdge.RIGHT.bit();
        };
    }

    /** Back to the level start. */
    void reset() {
        next = 0;
    }

    /** The next unit's index in the plan. */
    int next() {
        return next;
    }

    /** Back to the plan's unit {@code index} (a boss checkpoint). */
    void next(int index) {
        next = index;
    }

    /** The next unit if it enters at or before {@code tick}, advancing past it; otherwise {@code null}. */
    Spawn due(int tick) {
        return next < spawns.length && spawns[next].tick() <= tick ? spawns[next++] : null;
    }

    /** The edge the wave of the unit {@link #due} returned last enters from. */
    WaveSpec.Entry lastEntry() {
        return entries[next - 1];
    }

    /** M7 part G: whether the unit {@link #due} returned last is flipped (its wave's time is in a reversed scroll). */
    boolean lastFlipped() {
        return flips[next - 1];
    }

    /** The volley clock of the unit {@link #due} returned last; -1 without one. */
    int lastVolleyGroup() {
        return volleyGroups[next - 1];
    }

    /** M5 part C: the tag of the wave of the unit {@link #due} returned last ("" for none). */
    String lastTag() {
        return tags[next - 1];
    }

    /** The place in its wave (from 0, in entry order) of the unit {@link #due} returned last. */
    int lastVolleyUnit() {
        return volleyUnits[next - 1];
    }

    /** The number of volley clocks: one per staggered walker wave. */
    int volleyClocks() {
        return volleyClocks;
    }

    /** M7 part B: the place in its wave (from 0) of the unit {@link #due} returned last. */
    int lastPlace() {
        return places[next - 1];
    }

    /** M7 part D: the number of units the wave of the unit {@link #due} returned last planned. */
    int lastWaveSize() {
        return waveSizes[next - 1];
    }

    /** M7 part B: the burrow stagger clock of the unit {@link #due} returned last; -1 without one. */
    int lastBurrowGroup() {
        return burrowGroups[next - 1];
    }

    /** M7 part B: the number of burrow stagger clocks: one per Burrower wave. */
    int burrowClocks() {
        return burrowClocks;
    }

    /** The edges with a warning showing at {@code tick}, as {@link WarningEdge} bits. */
    int warnings(int tick) {
        int edges = 0;
        for (int i = 0; i < warningStarts.length; i++) {
            if (tick >= warningStarts[i] && tick < warningEnds[i]) {
                edges |= warningEdges[i];
            }
        }
        return edges;
    }

    /** The units of the enemy {@code slug} the level sends. */
    int unitsOf(String slug) {
        int count = 0;
        for (Spawn spawn : spawns) {
            count += spawn.enemy().slug().equals(slug) ? 1 : 0;
        }
        return count;
    }

    /** M5 part C: the units of the enemy {@code slug} of the waves tagged {@code tag} the level sends. */
    int unitsOf(String slug, String tag) {
        int count = 0;
        for (int i = 0; i < spawns.length; i++) {
            count += spawns[i].enemy().slug().equals(slug) && tags[i].equals(tag) ? 1 : 0;
        }
        return count;
    }

    /** Every unit the level sends. */
    int units() {
        return spawns.length;
    }

    /** The distinct enemies of the level; a spawn's kind indexes this list. */
    List<EnemySpec> kinds() {
        return kinds;
    }
}
