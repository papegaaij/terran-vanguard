package vanguard.sim;

import java.util.List;
import java.util.Optional;
import vanguard.sim.WaveSpec.Edge;
import vanguard.sim.WaveSpec.Entry;

/**
 * Places the units of a wave by formation and entry edge (design/enemies, formation vocabulary)
 * and plans how each one flies. Runs once when a level is loaded; the paths it builds are shared
 * by every attempt, so stepping the level never allocates.
 *
 * <p>Positions are play-field pixels, y up from the bottom edge. The layout numbers here (where
 * a V hovers, the circle's centre, how far apart units fly) are not in the design yet: they are
 * first values, to be tuned after playing.
 */
final class Formations {
    private static final double WIDTH = PlayField.WIDTH;
    private static final double HEIGHT = PlayField.HEIGHT;
    /** Units enter this far outside the edge, so they fly in rather than appear. */
    static final double OUTSIDE = 40;

    /** Enters top left, curls towards the centre and leaves through the bottom. */
    private static final FlightPath CURL =
            FlightPath.through(100, 580, 100, 420, 170, 300, 300, 260, 360, 180, 330, 60, 300, -40);
    /** Dives from the top right into the lower third, hooks back and leaves through the left edge. */
    private static final FlightPath HOOK =
            FlightPath.through(330, 580, 330, 350, 280, 200, 180, 160, 90, 220, 40, 340, -40, 420);
    /** Enters from the left edge and sweeps down across the player's lane to the right edge. */
    private static final FlightPath SWEEP = FlightPath.through(-40, 470, 100, 420, 240, 260, 380, 200, 520, 230);
    /** A front stream unit from the left: down and across to the right. */
    private static final FlightPath FRONT_STREAM = FlightPath.through(90, HEIGHT + OUTSIDE, 330, -OUTSIDE);
    /** A side stream unit from the left: across and down to the right edge. */
    private static final FlightPath SIDE_STREAM = FlightPath.through(-OUTSIDE, 430, WIDTH + OUTSIDE, 170);
    /**
     * M5 part F: a rear stream unit from the left (chasers only, design/enemies/air/lamprey): up and
     * across to the right, the front stream's path turned upside down.
     */
    private static final FlightPath REAR_STREAM = FlightPath.through(90, -OUTSIDE, 330, HEIGHT + OUTSIDE);

    /** M5 part G: the gap between the two units of a column's pair (Level 13's hard Stingers), px. */
    static final double PAIR_SPACING = 50;
    /** Horizontal gap between the ranks of a V. */
    private static final double V_SPACING = 50;
    /** The largest height step between the ranks of a V. */
    private static final double V_RANK_STEP = 35;
    /** How far from its edge a pincer unit holds, and the height of the first pair. */
    private static final double PINCER_X = 60;

    private static final double PINCER_Y = 360;
    private static final double PINCER_RANK_STEP = 70;
    /** The circle orbits a point above the play field's centre. */
    private static final double CIRCLE_Y = 360;
    /** The gap between two break groups of a circle ("one by one"). */
    static final double BREAK_INTERVAL_SECONDS = 0.5;
    /** M9 part D: a circle of dashing units breaks off this far apart, so their tells come one at a time. */
    static final double DASH_BREAK_SECONDS = 0.4;
    /** Divers of a V go one after another this far apart (design/enemies/air/stinger), and the units of a column enter so. */
    static final double DIVE_STAGGER_SECONDS = 0.4;
    /** A convoy's units follow one another this far apart, and come down this far from their side edge. */
    static final double CONVOY_INTERVAL_SECONDS = 1.5;

    /** M9 part C (design/enemies/space/minelayer): a layers' column enters this far apart by default (Level 30's 2.5 s). */
    static final double LAYER_INTERVAL_SECONDS = 2.5;

    /** M9 part C: a layer's lay row without its wave's mine lines, px below the top edge. */
    static final double LAYER_ROW = 100;

    /** M5 part C: a pack's walkers enter this far apart, each on its own path (design/enemies, formation vocabulary). */
    static final double PACK_INTERVAL_SECONDS = 0.25;

    private static final double CONVOY_EDGE_GAP = 70;
    private static final double CONVOY_TURN = 60;
    /** A whirl cluster releases its units within this time. */
    static final double CLUSTER_RELEASE_SECONDS = 0.3;
    /** The gap between the stops of a column entering from a side edge, inwards from the edge. */
    private static final double COLUMN_STOP_SPACING = 70;
    /** M5 part D: the most units of a rear ambush (design/enemies, formation vocabulary). */
    static final int AMBUSH_MOST = 4;
    /**
     * M5 part D: how far a leaving ambush unit climbs while it slides out to its side lane, px; it
     * is straight in its lane from twice this above its hold point.
     */
    static final double AMBUSH_EXIT_RISE = 60;
    /** M8 part G: the gap between two hit boxes of a column rising from the rear, px (the Depth Hunters'). */
    static final double REAR_COLUMN_GAP = 16;
    /** The sunflower's turn between two points of a swarm's starting cloud. */
    private static final double GOLDEN_ANGLE = StrictMath.PI * (3 - Math.sqrt(5));
    /** M5 part F: about this far apart, px, the members of a swarm of chasers start in their cloud. */
    static final double CHASER_SPACING = 32;
    /** M5 part F: the most of a swarm of chasers' start delays, s (user decision F9 = a). */
    static final double CHASER_DELAY_MOST = 0.6;

    private Formations() {}

    /**
     * M7 part C (design/campaign Level 16; risk 1 of part C's plan, default pending the user's review): the
     * flyable span a wave's formation is laid out on in a canyon, at the wave's entry row: the play field's
     * width is mapped onto it, so entries and spreads stay off the rock; residuals are fixed by an authored
     * {@code x} or {@code lanes} and proven by the content's clearance test.
     *
     * @param left the span's left edge, px
     * @param right its right edge, px
     */
    record Span(double left, double right) {
        Span {
            if (!(right > left)) {
                throw new IllegalArgumentException("a span is wider than nothing: " + left + " to " + right);
            }
        }

        /** Whether it is the whole play field (then nothing is mapped). */
        boolean whole() {
            return left <= 0 && right >= WIDTH;
        }

        /** The play-field x {@code x} mapped onto the span from {@code left} to {@code right}. */
        static double map(double x, double left, double right) {
            return left + x * (right - left) / WIDTH;
        }

        /**
         * Whether a wave is laid out by its formation alone, so it is mapped onto the span: not with an
         * authored point, paths or lanes, not a field, nor a walker's, a chain's or a burrower's (ground).
         */
        static boolean maps(WaveSpec wave) {
            EnemySpec enemy = wave.enemy();
            return wave.at().isEmpty()
                    && wave.paths().isEmpty()
                    && wave.lanes().isEmpty()
                    && wave.field().isEmpty()
                    && enemy.walker().isEmpty()
                    && enemy.chain().isEmpty()
                    && enemy.burrow().isEmpty()
                    && enemy.layer() != Layer.GROUND
                    // M7 part G: a pursuit follows the ship; its refills find their flock by its route.
                    && wave.formation() != WaveSpec.Formation.PURSUIT;
        }
    }

    /**
     * As {@link #plan(WaveSpec, int, SplitMix64, List)}; M7 part C: with a {@code span} (a canyon's flyable
     * span at the wave's entry row) a wave laid out by its formation alone ({@link Span#maps}) is mapped
     * onto it. Without one, or with the whole field, it is planned exactly as before.
     */
    static void plan(WaveSpec wave, int kind, SplitMix64 rng, List<Spawn> out, Optional<Span> span) {
        int before = out.size();
        plan(wave, kind, rng, out);
        if (span.isEmpty() || span.get().whole() || !Span.maps(wave)) {
            return;
        }
        for (int i = before; i < out.size(); i++) {
            out.set(i, out.get(i).mappedX(span.get()));
        }
    }

    /**
     * A boss stream's unit (design/enemies/bosses: Skitter streams from the side edges), entering
     * from the left or the right edge on a side stream's path; its tick is not read.
     */
    static Spawn streamUnit(EnemySpec enemy, int kind, boolean left) {
        return new Spawn(
                0,
                kind,
                enemy,
                left ? SIDE_STREAM : SIDE_STREAM.mirrored(),
                enemy.streamSpeed().orElse(enemy.speed()),
                0,
                Optional.empty(),
                Spawn.Exit.DOWN,
                false,
                Optional.empty());
    }

    /** Adds the units of {@code wave} to {@code out}; {@code kind} is the wave's enemy kind. */
    static void plan(WaveSpec wave, int kind, SplitMix64 rng, List<Spawn> out) {
        Planner planner = new Planner(wave, kind, rng, out);
        if (!wave.lanes().isEmpty()) {
            planner.lanes();
            return;
        }
        if (wave.enemy().laneStrafe().isPresent()) {
            planner.laneStrafes();
            return;
        }
        if (wave.enemy().mineLayer().isPresent()) {
            // M9 part C (design/enemies/space/minelayer): up a side lane from behind, then across laying its line.
            planner.mineLayers();
            return;
        }
        if (wave.enemy().vortex().isPresent()) {
            planner.vortices();
            return;
        }
        if (wave.enemy().coil().isPresent()) {
            planner.coils();
            return;
        }
        if (wave.enemy().walker().isPresent()) {
            planner.walkers();
            return;
        }
        if (wave.enemy().chain().isPresent()) {
            planner.chains();
            return;
        }
        if (wave.enemy().sideHover().isPresent() && wave.entry() == Entry.SIDES) {
            planner.sideHovers();
            return;
        }
        if (wave.enemy().swoopPath().isPresent() && Planner.swoops(wave.formation())) {
            // M9 part B (design/enemies/air/talon): the wave flies its curve across the upper half.
            planner.swoopPaths();
            return;
        }
        switch (wave.formation()) {
            case SINGLE -> planner.single();
            case CONVOY -> planner.convoy();
            case WHIRL_CLUSTER -> planner.whirlCluster();
            case COLUMN -> planner.column();
            case SNAKE -> planner.snake();
            case V_WING -> planner.vWing();
            case LINE_ABREAST -> planner.lineAbreast();
            case STREAM -> planner.stream();
            case PINCER -> planner.pincer();
            case CIRCLE -> planner.circle();
            case CARRIER_ESCORTS -> planner.carrierEscorts();
            case PACK ->
                throw new IllegalArgumentException("a pack is a formation of walkers, not of "
                        + wave.enemy().slug());
            case SWARM -> planner.swarm();
            case REAR_AMBUSH -> planner.rearAmbush();
            case FIELD -> planner.field();
            case CROSS -> planner.cross();
            case PURSUIT -> planner.pursuit();
        }
    }

    /** The planning of one wave. */
    private static final class Planner {
        private final WaveSpec wave;
        private final int kind;
        private final SplitMix64 rng;
        private final List<Spawn> out;

        Planner(WaveSpec wave, int kind, SplitMix64 rng, List<Spawn> out) {
            this.wave = wave;
            this.kind = kind;
            this.rng = rng;
            this.out = out;
        }

        private EnemySpec enemy() {
            return wave.enemy();
        }

        private double speed(double own) {
            return wave.speed().orElse(own);
        }

        void snake() {
            double spacing = required(enemy().snake(), "snake").spacingSeconds();
            if (!wave.paths().isEmpty()) {
                // M5 part D (the Skitter's authored-paths item): every unit flies the wave's route.
                FlightPath route = route(wave.paths().getFirst());
                for (int i = 0; i < wave.count(); i++) {
                    add(i, wave.t() + i * spacing, route, speed(enemy().speed()), 0, Optional.empty(), Spawn.Exit.DOWN);
                }
                return;
            }
            FlightPath path =
                    switch (wave.entry()) {
                        case FRONT ->
                            switch (wave.edge()) {
                                case LEFT -> CURL;
                                case RIGHT -> CURL.mirrored();
                                default -> HOOK;
                            };
                        case SIDES ->
                            switch (wave.edge()) {
                                case LEFT -> SWEEP;
                                case RIGHT -> SWEEP.mirrored();
                                default -> throw unsupported("a snake enters from one side");
                            };
                        case REAR -> throw unsupported("snakes enter from the rear only on their paths");
                    };
            for (int i = 0; i < wave.count(); i++) {
                add(i, wave.t() + i * spacing, path, speed(enemy().speed()), 0, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        /**
         * M7 part E (design/enemies/air/ghost-drone): lane strafers entering together, whatever the formation, unit
         * i on lane i ({@link EnemySpec.LaneStrafe#depth}). From the front: a single unit at the authored point (its
         * x and its lane's depth) or in the front column, more evenly across ((i + 1) × 480 ÷ (n + 1)), each just
         * above the top edge; a unit left of the centre strafes to the right, one right of it to the left, one on it
         * by its place (even: right), so a line abreast splits half and half, crossing. From the sides: alternately
         * from the left and the right edge (from one edge for {@code left} or {@code right}), just outside it at its
         * lane. Its path runs from its start to its lane's point beyond its bank, where {@link LaneStrafing} reads
         * its lane and its direction; it flies by its strafe, not along the path.
         */
        void laneStrafes() {
            EnemySpec.LaneStrafe strafe = enemy().laneStrafe().orElseThrow();
            int count = wave.count();
            double halfWidth = enemy().hitbox().width() / 2;
            double halfHeight = enemy().hitbox().height() / 2;
            for (int i = 0; i < count; i++) {
                FlightPath path;
                if (wave.entry() == Entry.SIDES) {
                    boolean left =
                            switch (wave.edge()) {
                                case LEFT -> true;
                                case RIGHT -> false;
                                case NONE, ALTERNATING, TOP, BOTTOM -> i % 2 == 0;
                            };
                    double y = HEIGHT - strafe.depth(i, count);
                    double startX = left ? -halfWidth - 1 : WIDTH + halfWidth + 1;
                    path = FlightPath.through(startX, y, WIDTH - startX, y);
                } else {
                    requireFront();
                    double x;
                    double depth = strafe.depth(i, count);
                    if (count == 1 && wave.at().isPresent()) {
                        x = wave.at().get().x();
                        depth = wave.at().get().depth();
                    } else if (count == 1) {
                        x = frontColumn();
                    } else {
                        x = (i + 1) * WIDTH / (count + 1);
                    }
                    int dir = x < WIDTH / 2 ? 1 : x > WIDTH / 2 ? -1 : i % 2 == 0 ? 1 : -1;
                    double y = HEIGHT - depth;
                    double r = EnemySpec.LaneStrafe.BANK_RADIUS;
                    path = FlightPath.through(x, HEIGHT + halfHeight, x, y + r, x + dir * r, y);
                }
                add(i, wave.t(), path, strafe.descentSpeed(), 0, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        /**
         * M9 part C (design/enemies/space/minelayer; design/campaign Level 30): mine layers from behind, whatever the
         * formation, unit i entering {@code i} × the wave's interval (default {@link #LAYER_INTERVAL_SECONDS}: the
         * staggered entry) after the wave's t, just below the bottom edge in the side lane of its edge (left, right;
         * alternating from the left for any other), {@link EnemySpec.MineLayer#laneInset()} in from it. Its path runs
         * up to its lay row at the gap point of its wave's {@link WaveSpec.MineLines} (unit i's; without them the row
         * {@link #LAYER_ROW} below the top edge and the gap at the field's middle), where {@link MineLaying} reads its
         * lay row and its gap; it flies by its lay path, not along the path.
         */
        void mineLayers() {
            if (wave.entry() != WaveSpec.Entry.REAR) {
                throw new IllegalArgumentException(enemy().slug() + ": a mine layer enters from the rear");
            }
            EnemySpec.MineLayer layer = enemy().mineLayer().orElseThrow();
            double half = enemy().hitbox().height() / 2;
            double interval = wave.intervalSeconds().orElse(LAYER_INTERVAL_SECONDS);
            for (int i = 0; i < wave.count(); i++) {
                boolean left =
                        switch (wave.edge()) {
                            case LEFT -> true;
                            case RIGHT -> false;
                            case NONE, ALTERNATING, TOP, BOTTOM -> i % 2 == 0;
                        };
                double laneX = left ? layer.laneInset() : WIDTH - layer.laneInset();
                double row =
                        wave.mineLines().isPresent() ? wave.mineLines().get().row(i) : LAYER_ROW;
                double gapX =
                        wave.mineLines().isPresent() ? wave.mineLines().get().gapX(i) : WIDTH / 2.0;
                FlightPath path = FlightPath.through(laneX, -half - 1, laneX, HEIGHT - row, gapX, HEIGHT - row);
                add(i, wave.t() + i * interval, path, layer.riseSpeed(), 0, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        /**
         * M7 part E (design/enemies/ground/dust-devil): vortices entering together at the top edge, whatever the
         * formation, each on its own centre line: its path's first x when the wave has paths (one per unit, the
         * last reused), else the authored point's x for a single unit, else the front column for one and evenly
         * across for more ((i + 1) × 480 ÷ (n + 1)); its centre just above the top edge. Its {@link VortexCycle}
         * moves it from there (its swirl, its cycle offset by its place in the wave).
         */
        void vortices() {
            requireFront();
            int count = wave.count();
            double half = enemy().hitbox().height() / 2;
            for (int i = 0; i < count; i++) {
                double x;
                if (!wave.paths().isEmpty()) {
                    x = wave.paths()
                            .get(Math.min(i, wave.paths().size() - 1))
                            .getFirst()
                            .x();
                } else if (count == 1 && wave.at().isPresent()) {
                    x = wave.at().get().x();
                } else if (count == 1) {
                    x = frontColumn();
                } else {
                    x = (i + 1) * WIDTH / (count + 1);
                }
                add(
                        i,
                        wave.t(),
                        FlightPath.through(x, HEIGHT + half, x, -half),
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        /**
         * M8 part E (design/enemies/naval/spiral-nautilus; §5 E7): coiling units entering together at the top edge,
         * whatever the formation (a {@code circle} pair in the level), evenly across ((i + 1) × 480 ÷ (n + 1), mapped
         * onto a canyon's span) or, a single unit, at the authored point's x; their centres just above the top edge.
         * Their {@link Coiling} moves them from there round the ship for the wave's hold (required), each cycle offset
         * by its place, a pair orbiting in opposite directions.
         */
        void coils() {
            requireFront();
            double hold = required(wave.holdSeconds(), "hold");
            int count = wave.count();
            double half = enemy().hitbox().height() / 2;
            for (int i = 0; i < count; i++) {
                double x = count == 1 && wave.at().isPresent() ? wave.at().get().x() : (i + 1) * WIDTH / (count + 1);
                add(
                        i,
                        wave.t(),
                        FlightPath.through(x, HEIGHT + half, x, HEIGHT - half),
                        speed(enemy().speed()),
                        hold,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        void single() {
            requireFront();
            double x = frontColumn();
            if (!stops()) {
                add(
                        0,
                        wave.t(),
                        FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE),
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
                return;
            }
            stopAt(0, wave.t(), FlightPath.through(x, HEIGHT + OUTSIDE, x, HEIGHT - pathEndDepth()), x);
        }

        /**
         * A column from the top near one side edge that comes down to its strafe height and turns
         * across the screen to leave through the other side, {@link #CONVOY_INTERVAL_SECONDS} apart.
         */
        void convoy() {
            requireFront();
            Range strafe = required(enemy().strafe(), "strafe");
            boolean fromLeft = wave.edge() != Edge.RIGHT;
            double x = fromLeft ? CONVOY_EDGE_GAP : WIDTH - CONVOY_EDGE_GAP;
            double y = HEIGHT - strafe.at(0.5);
            double out = fromLeft ? WIDTH + OUTSIDE * 2 : -OUTSIDE * 2;
            double turn = fromLeft ? CONVOY_TURN : -CONVOY_TURN;
            // The turn starts and ends this far from the corner, so the curve hardly dips below the strafe height.
            FlightPath path = FlightPath.through(x, HEIGHT + OUTSIDE * 2, x, y + CONVOY_TURN, x + turn, y, out, y);
            for (int i = 0; i < wave.count(); i++) {
                add(
                        i,
                        wave.t() + i * CONVOY_INTERVAL_SECONDS,
                        path,
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        /**
         * The units of a whirl cluster released from its point within {@link #CLUSTER_RELEASE_SECONDS}
         * at 360/n degrees apart, so the spiral reads as one shape (design/enemies/air/whirl-seed).
         */
        void whirlCluster() {
            WaveSpec.At at = required(wave.at(), "a release point (at)");
            double x = at.x();
            double y = HEIGHT - at.depth();
            for (int i = 0; i < wave.count(); i++) {
                double angle = 2 * StrictMath.PI * i / wave.count();
                double t = wave.t() + CLUSTER_RELEASE_SECONDS * i / wave.count();
                out.add(new Spawn(
                        SimStep.ticks(t),
                        kind,
                        enemy(),
                        FlightPath.through(x, y, x, y - 1),
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.of(new Spawn.Release(x, y, angle))));
            }
        }

        /**
         * One behind the other on the same path, {@link #DIVE_STAGGER_SECONDS} apart: from the front
         * down to where the units stop (or through the screen for units that do not), or from a side
         * edge along the row they stop at, each stopping further in than the one after it.
         */
        void column() {
            double gap = DIVE_STAGGER_SECONDS;
            if (wave.entry() == Entry.SIDES) {
                boolean left =
                        switch (wave.edge()) {
                            case LEFT -> true;
                            case RIGHT -> false;
                            default -> throw unsupported("a column enters from one side");
                        };
                double y = HEIGHT - stopDepth().at(0.5);
                for (int i = 0; i < wave.count(); i++) {
                    double in = COLUMN_STOP_SPACING * (wave.count() - i);
                    double x = left ? in : WIDTH - in;
                    double edge = left ? -OUTSIDE : WIDTH + OUTSIDE;
                    stopAt(i, wave.t() + i * gap, FlightPath.through(edge, y, x, y), x);
                }
                return;
            }
            if (wave.entry() == Entry.REAR) {
                rearColumn();
                return;
            }
            requireFront();
            if (wave.pairSeconds().isPresent()) {
                pairs(wave.pairSeconds().get());
                return;
            }
            double x = frontColumn();
            for (int i = 0; i < wave.count(); i++) {
                if (stops()) {
                    stopAt(
                            i,
                            wave.t() + i * gap,
                            FlightPath.through(x, HEIGHT + OUTSIDE, x, HEIGHT - pathEndDepth()),
                            x);
                } else {
                    add(
                            i,
                            wave.t() + i * gap,
                            FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE),
                            speed(enemy().speed()),
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
            }
        }

        /**
         * M8 part G (design/enemies/naval/depth-hunter, Movement; design/campaign Level 27): a column of hovering units
         * rising from below the bottom edge in the column's x ({@link #frontColumn()}), one behind the other ({@link
         * #REAR_COLUMN_GAP} px between their hit boxes at their speed, at least {@link #DIVE_STAGGER_SECONDS} apart),
         * each to its own hold height: unit i of n at the hover depth's min + i ÷ (n − 1) of its span below the top edge
         * (the first, which rises furthest, highest; a lone one at the span's middle). Each holds its hover time and
         * leaves straight down; a hover with a drift drifts sideways while it holds ({@link HoverHold#driftUp}).
         */
        private void rearColumn() {
            EnemySpec.Hover hover = required(enemy().hover(), "a hover (a column from the rear stops)");
            if (hover.aboveShip().isPresent() || enemy().dive().isPresent()) {
                throw unsupported("a column from the rear holds at its hover's depth");
            }
            double speed = speed(enemy().speed());
            double gap = Math.max(DIVE_STAGGER_SECONDS, (enemy().hitbox().height() + REAR_COLUMN_GAP) / speed);
            double x = frontColumn();
            Range depth = hover.depth();
            int count = wave.count();
            for (int i = 0; i < count; i++) {
                double share = count == 1 ? 0.5 : (double) i / (count - 1);
                double holdY = HEIGHT - depth.at(share);
                add(
                        i,
                        wave.t() + i * gap,
                        FlightPath.through(x, -OUTSIDE, x, holdY),
                        speed,
                        hover.seconds().pick(rng),
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        /**
         * M5 part G (design/campaign Level 13, hard's t=210 Stingers): a column from the front in pairs:
         * the units of a pair side by side {@link #PAIR_SPACING} apart about the column's x (a last unit
         * on its own on it), entering together, the pairs {@code gap} s apart; each stops (or flies
         * through) as in a column.
         */
        private void pairs(double gap) {
            double centre = frontColumn();
            for (int i = 0; i < wave.count(); i++) {
                boolean alone = i % 2 == 0 && i == wave.count() - 1;
                double x = alone ? centre : centre + (i % 2 == 0 ? -PAIR_SPACING / 2 : PAIR_SPACING / 2);
                double t = wave.t() + (i / 2) * gap;
                if (stops()) {
                    stopAt(i, t, FlightPath.through(x, HEIGHT + OUTSIDE, x, HEIGHT - pathEndDepth()), x);
                } else {
                    add(
                            i,
                            t,
                            FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE),
                            speed(enemy().speed()),
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
            }
        }

        /**
         * M5 part G (design/enemies/naval/skimmer, user decision G4 = a): lane walkers, each on its lane
         * of the wave ({@link WaveSpec#lanes()}): unit i on lane i (wrapping); a pincer with one lane
         * mirrors it for every second unit and sends each further pair {@link #CONVOY_INTERVAL_SECONDS}
         * later, a pack sends them {@link #PACK_INTERVAL_SECONDS} apart, a {@code cross} together (a lane
         * per unit), any other formation one after another {@link #CONVOY_INTERVAL_SECONDS} apart. A lane
         * is in ground space: it scrolls with the ground from the unit's entry.
         */
        void lanes() {
            if (enemy().layer() != Layer.GROUND
                    || enemy().walker().isPresent()
                    || enemy().chain().isPresent()) {
                throw unsupported("only a ground unit that is not a walker follows lanes");
            }
            int lanes = wave.lanes().size();
            if (wave.formation() == WaveSpec.Formation.CROSS && lanes < wave.count()) {
                throw unsupported("a cross needs a lane per unit: " + lanes + " for " + wave.count());
            }
            double speed = speed(enemy().speed());
            for (int i = 0; i < wave.count(); i++) {
                boolean mirror = wave.formation() == WaveSpec.Formation.PINCER && lanes == 1 && i % 2 == 1;
                FlightPath lane = FlightPath.through(points(wave.lanes().get(i % lanes), mirror));
                double delay =
                        switch (wave.formation()) {
                            case CROSS -> 0;
                            case PINCER -> (i / 2) * CONVOY_INTERVAL_SECONDS;
                            case PACK -> i * PACK_INTERVAL_SECONDS;
                            default -> i * CONVOY_INTERVAL_SECONDS;
                        };
                out.add(new Spawn(
                        SimStep.ticks(wave.t() + delay),
                        kind,
                        enemy(),
                        lane,
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        0,
                        true));
            }
        }

        /**
         * M5 part G, a {@code cross} without lanes (design/enemies, formation vocabulary): {@code count}
         * ÷ 4 units from each edge, unit i from the top, the right, the bottom and the left edge in turn,
         * evenly spaced along its edge and flying straight across the play field together.
         */
        void cross() {
            if (wave.count() % 4 != 0) {
                throw unsupported("a cross sends as many units from each of the four edges: " + wave.count());
            }
            int perEdge = wave.count() / 4;
            double speed = speed(enemy().speed());
            for (int i = 0; i < wave.count(); i++) {
                int slot = i / 4;
                double x = (slot + 1) * WIDTH / (perEdge + 1);
                double y = (slot + 1) * HEIGHT / (perEdge + 1);
                FlightPath path =
                        switch (i % 4) {
                            case 0 -> FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE);
                            case 1 -> FlightPath.through(WIDTH + OUTSIDE, y, -OUTSIDE, y);
                            case 2 -> FlightPath.through(x, -OUTSIDE, x, HEIGHT + OUTSIDE);
                            default -> FlightPath.through(-OUTSIDE, y, WIDTH + OUTSIDE, y);
                        };
                add(i, wave.t(), path, speed, 0, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        private double frontColumn() {
            return switch (wave.edge()) {
                case LEFT -> WIDTH * 0.3;
                case RIGHT -> WIDTH * 0.7;
                default -> WIDTH / 2;
            };
        }

        void vWing() {
            requireFront();
            double centre = frontColumn();
            if (!stops()) {
                // M7 part B (the Dust Kite's glide): units that neither hover nor dive fly the V down the
                // screen, the leader first and each rank V_RANK_STEP px behind the one before it.
                double speed = speed(enemy().speed());
                for (int i = 0; i < wave.count(); i++) {
                    int rank = (i + 1) / 2;
                    int side = i == 0 ? 0 : (i % 2 == 1 ? -1 : 1);
                    double x = centre + side * rank * V_SPACING;
                    add(
                            i,
                            wave.t() + rank * V_RANK_STEP / speed,
                            FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE),
                            speed,
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
                return;
            }
            Range depth = stopDepth();
            int ranks = wave.count() / 2;
            double rankStep = Math.min(V_RANK_STEP, (depth.max() - depth.min()) / Math.max(1, ranks));
            for (int i = 0; i < wave.count(); i++) {
                int rank = (i + 1) / 2;
                int side = i == 0 ? 0 : (i % 2 == 1 ? -1 : 1);
                double x = centre + side * rank * V_SPACING;
                double y = HEIGHT - (depth.max() - rank * rankStep);
                FlightPath path = FlightPath.through(x + side * 40, HEIGHT + OUTSIDE, x + side * 20, y + 90, x, y);
                stopAt(i, wave.t(), path, x);
            }
        }

        /** M9 part B: whether a swoop path's wave of {@code formation} flies its curve (the others as before). */
        static boolean swoops(WaveSpec.Formation formation) {
            return formation == WaveSpec.Formation.V_WING
                    || formation == WaveSpec.Formation.LINE_ABREAST
                    || formation == WaveSpec.Formation.COLUMN
                    || formation == WaveSpec.Formation.PINCER;
        }

        /**
         * M9 part B (design/enemies/air/talon; {@link EnemySpec.SwoopPath}): the wave's units on its curve, the lowest
         * point drawn once for the wave (then, from the front and centred, the side it sweeps toward). From the front
         * each unit's curve enters across / 2 px before its formation place and bottoms out there: a V-wing's ranks
         * one after another as in its glide, a line abreast together, a column {@link #DIVE_STAGGER_SECONDS} apart; a
         * wave from the left edge's column sweeps right, from the right edge's left. A pincer's wings enter at their
         * side edges, each wing's units {@link #V_SPACING} px apart along its curve. Nothing stops: each is gone at
         * its curve's end.
         */
        void swoopPaths() {
            EnemySpec.SwoopPath swoop = enemy().swoopPath().orElseThrow();
            double speed = speed(enemy().speed());
            double lowest = swoop.lowest().pick(rng);
            if (wave.formation() == WaveSpec.Formation.PINCER) {
                if (wave.entry() != Entry.SIDES || wave.edge() != Edge.NONE) {
                    throw unsupported("a pincer enters from both sides");
                }
                FlightPath fromLeft = SwoopPaths.fromLeft(swoop.across(), lowest);
                FlightPath fromRight = fromLeft.mirrored();
                for (int i = 0; i < wave.count(); i++) {
                    add(
                            i,
                            wave.t() + (i / 2) * V_SPACING / speed,
                            i % 2 == 0 ? fromLeft : fromRight,
                            speed,
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
                return;
            }
            requireFront();
            int side =
                    switch (wave.edge()) {
                        case LEFT -> 1;
                        case RIGHT -> -1;
                        default -> rng.nextDouble() < 0.5 ? -1 : 1;
                    };
            for (int i = 0; i < wave.count(); i++) {
                double place;
                double t;
                switch (wave.formation()) {
                    case V_WING -> {
                        int rank = (i + 1) / 2;
                        int wing = i == 0 ? 0 : (i % 2 == 1 ? -1 : 1);
                        place = frontColumn() + wing * rank * V_SPACING;
                        t = wave.t() + rank * V_RANK_STEP / speed;
                    }
                    case LINE_ABREAST -> {
                        place = (i + 1) * WIDTH / (wave.count() + 1);
                        t = wave.t();
                    }
                    default -> {
                        place = frontColumn();
                        t = wave.t() + i * DIVE_STAGGER_SECONDS;
                    }
                }
                add(
                        i,
                        t,
                        SwoopPaths.front(place - side * swoop.across() / 2, side, swoop.across(), lowest),
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        /** Whether its units stop on the screen: they hover, or pause before a dive. */
        private boolean stops() {
            return enemy().hover().isPresent() || enemy().dive().isPresent();
        }

        /**
         * How far below the top edge the path of a unit coming down from the top edge ends: its stop depth's middle;
         * M8 part D: for a hover held above the ship the depth's max (it stops at its height above the ship on the way,
         * {@link EnemySpec.Hover#aboveShip()}, its path's end only at the latest).
         */
        private double pathEndDepth() {
            if (enemy().dive().isEmpty()
                    && enemy().hover().isPresent()
                    && enemy().hover().get().aboveShip().isPresent()) {
                return enemy().hover().get().depth().max();
            }
            return stopDepth().at(0.5);
        }

        /** How far below the top edge its units stop: where they hover, or pause before a dive. */
        private Range stopDepth() {
            if (enemy().dive().isPresent()) {
                return enemy().dive().get().depth();
            }
            return required(enemy().hover(), "hover or dive").depth();
        }

        /**
         * A unit that flies {@code path} and stops at its end: a hover for its hover time, then away
         * from the centre; or a diver's pause (the units of a V one after another), then the dive.
         */
        private void stopAt(int unit, double t, FlightPath path, double x) {
            if (enemy().dive().isPresent()) {
                EnemySpec.Dive dive = enemy().dive().get();
                double stagger = wave.formation() == WaveSpec.Formation.V_WING ? unit * DIVE_STAGGER_SECONDS : 0;
                add(
                        unit,
                        t,
                        path,
                        speed(enemy().speed()),
                        dive.pauseSeconds() + stagger,
                        Optional.empty(),
                        Spawn.Exit.TOWARD_SHIP);
            } else {
                EnemySpec.Hover hover = required(enemy().hover(), "hover or dive");
                add(
                        unit,
                        t,
                        path,
                        speed(enemy().speed()),
                        hover.seconds().pick(rng),
                        Optional.empty(),
                        // M7 part D: a Herald's hover ends straight up (its stat block's exit: back).
                        enemy().aura().isPresent() ? Spawn.Exit.UP : Spawn.Exit.awayFromCentre(x));
            }
        }

        void lineAbreast() {
            int count = wave.count();
            if (enemy().hover().isPresent() && wave.entry() == Entry.FRONT) {
                EnemySpec.Hover hover = enemy().hover().orElseThrow();
                double y = HEIGHT - pathEndDepth();
                for (int i = 0; i < count; i++) {
                    double x = (i + 1) * WIDTH / (count + 1);
                    stopAt(i, wave.t(), FlightPath.through(x, HEIGHT + OUTSIDE, x, y), x);
                }
                return;
            }
            boolean rear =
                    switch (wave.entry()) {
                        case FRONT -> false;
                        case REAR -> true;
                        case SIDES -> throw unsupported("a line abreast enters from the front or the rear");
                    };
            // M9 part D (design/enemies/space/shard-drone): a linked line flies its link's spacing apart, centred on
            // its wave's at (else the field's middle).
            double centre = wave.at().isPresent() ? wave.at().get().x() : WIDTH / 2;
            for (int i = 0; i < count; i++) {
                double x = enemy().airLink().isPresent()
                        ? centre
                                + (i - (count - 1) / 2.0)
                                        * enemy().airLink().get().spacing()
                        : (i + 0.5) * WIDTH / count;
                FlightPath path = rear
                        ? FlightPath.through(x, -OUTSIDE, x, HEIGHT + OUTSIDE)
                        : FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE);
                add(i, wave.t(), path, speed(enemy().speed()), 0, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        void stream() {
            FlightPath fromLeft =
                    switch (wave.entry()) {
                        case FRONT -> FRONT_STREAM;
                        case SIDES -> SIDE_STREAM;
                        case REAR -> {
                            // M5 part F: only chasers stream in from the rear (Level 12's Lampreys).
                            if (enemy().chase().isEmpty()) {
                                throw unsupported("only chasers stream in from the rear");
                            }
                            yield REAR_STREAM;
                        }
                    };
            if (!wave.paths().isEmpty()) {
                // M5 part D (Level 10, around the shuttle band): a unit from the left flies the wave's
                // route, one from the right its mirror image.
                fromLeft = route(wave.paths().getFirst());
            }
            FlightPath fromRight = fromLeft.mirrored();
            double interval = required(wave.intervalSeconds(), "interval");
            double speed = speed(enemy().streamSpeed().orElse(enemy().speed()));
            for (int i = 0; i < wave.count(); i++) {
                boolean left =
                        switch (wave.edge()) {
                            case LEFT -> true;
                            case RIGHT -> false;
                            case NONE, ALTERNATING, TOP, BOTTOM -> i % 2 == 0;
                        };
                add(
                        i,
                        wave.t() + i * interval,
                        left ? fromLeft : fromRight,
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN);
            }
        }

        void pincer() {
            if (wave.entry() != Entry.SIDES || wave.edge() != Edge.NONE) {
                throw unsupported("a pincer enters from both sides");
            }
            double hold = required(wave.holdSeconds(), "hold");
            for (int i = 0; i < wave.count(); i++) {
                int rank = i / 2;
                double y = PINCER_Y - rank * PINCER_RANK_STEP;
                FlightPath fromLeft = FlightPath.through(-OUTSIDE, y + 30, PINCER_X, y);
                FlightPath path = i % 2 == 0 ? fromLeft : fromLeft.mirrored();
                add(i, wave.t(), path, speed(enemy().speed()), hold, Optional.empty(), Spawn.Exit.DOWN);
            }
        }

        void circle() {
            requireFront();
            EnemySpec.Orbit orbit = required(enemy().orbit(), "orbit");
            double hold = required(wave.holdSeconds(), "hold");
            double radius = orbit.radius();
            // M5 part F: a clockwise circle (Level 12's eye) turns the other way round (y is up).
            double rate = StrictMath.toRadians(orbit.degreesPerSecond()) * (wave.clockwise() ? -1 : 1);
            // M5 part G: a circle's centre is its wave's at (Level 13's Needlers above the relay).
            double centreX = wave.at().isPresent() ? wave.at().get().x() : WIDTH / 2;
            double centreY = wave.at().isPresent() ? HEIGHT - wave.at().get().depth() : CIRCLE_Y;
            // Every unit flies the same distance straight down to its place, so the circle arrives whole.
            double drop = HEIGHT + OUTSIDE - (centreY - radius);
            for (int i = 0; i < wave.count(); i++) {
                double angle = StrictMath.PI / 2 + 2 * StrictMath.PI * i / wave.count();
                double x = centreX + radius * StrictMath.cos(angle);
                double y = centreY + radius * StrictMath.sin(angle);
                // M9 part D (design/enemies/air/buzzsaw-drone): dashing units break off 0.4 s apart (their tells).
                double breakDelay = (double) (i / wave.breakGroup())
                        * (enemy().dash().isPresent() ? DASH_BREAK_SECONDS : BREAK_INTERVAL_SECONDS);
                var place = Optional.of(new Spawn.Orbit(centreX, centreY, radius, angle, rate));
                add(
                        i,
                        wave.t(),
                        FlightPath.through(x, y + drop, x, y),
                        speed(enemy().speed()),
                        hold + breakDelay,
                        place,
                        Spawn.Exit.TOWARD_SHIP);
            }
        }

        /**
         * A {@code carrier + escorts} group (design/enemies/air/brood-pod): the carriers (spawners)
         * drift straight down, side by side; the escorts circle the carrier that entered last
         * (the wave lists the carrier's group first) at their orbit's radius and rate, evenly
         * spaced, and break off toward the ship {@link #BREAK_INTERVAL_SECONDS} apart when it ends.
         */
        void carrierEscorts() {
            requireFront();
            if (enemy().aura().isPresent() && stops()) {
                // M7 part D: a Herald carrier (design/enemies/air/choir-herald) comes down to its hover, spread
                // across like a line abreast (one: at the wave's front column); the escorts' wave circles it.
                for (int i = 0; i < wave.count(); i++) {
                    double x = wave.count() == 1 ? frontColumn() : (i + 1) * WIDTH / (wave.count() + 1);
                    stopAt(i, wave.t(), FlightPath.through(x, HEIGHT + OUTSIDE, x, HEIGHT - pathEndDepth()), x);
                }
                return;
            }
            if (enemy().rise().isPresent()) {
                // M8 part D (design/enemies/naval/abyss-ray): a rising carrier takes its spot ahead of the ship on its
                // first step (its path is never flown); the escorts' wave is held until its rise's middle.
                for (int i = 0; i < wave.count(); i++) {
                    add(
                            i,
                            wave.t(),
                            FlightPath.through(WIDTH / 2, HEIGHT / 2, WIDTH / 2, HEIGHT / 2 - 1),
                            speed(enemy().speed()),
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
                return;
            }
            if (enemy().brood().isPresent()) {
                for (int i = 0; i < wave.count(); i++) {
                    double x = (i + 1) * WIDTH / (wave.count() + 1);
                    add(
                            i,
                            wave.t(),
                            FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE * 2),
                            speed(enemy().speed()),
                            0,
                            Optional.empty(),
                            Spawn.Exit.DOWN);
                }
                return;
            }
            EnemySpec.Orbit orbit = required(enemy().orbit(), "orbit");
            double rate = StrictMath.toRadians(orbit.degreesPerSecond());
            double x = WIDTH / 2;
            for (int i = 0; i < wave.count(); i++) {
                double angle = StrictMath.PI / 2 + 2 * StrictMath.PI * i / wave.count();
                out.add(new Spawn(
                        SimStep.ticks(wave.t()),
                        kind,
                        enemy(),
                        FlightPath.through(x, HEIGHT + OUTSIDE, x, HEIGHT),
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.TOWARD_SHIP,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.of(new Spawn.Escort(orbit.radius(), angle, rate, i * BREAK_INTERVAL_SECONDS)),
                        Optional.empty()));
            }
        }

        /**
         * Walkers (design/enemies/ground/scuttler) on the wave's ground paths: unit i walks path
         * i (wrapping); a pincer with a single path mirrors it about the centre line for every
         * second unit and sends each further pair {@link #CONVOY_INTERVAL_SECONDS} later; a pack (M5
         * part C) needs a path per unit and sends them {@link #PACK_INTERVAL_SECONDS} apart; any other
         * formation (a convoy, a single) sends its units one after another its wave's interval apart
         * ({@link #CONVOY_INTERVAL_SECONDS} without one; M7 part D). These planners are separate from the air formations of the
         * same names.
         */
        void walkers() {
            if (wave.paths().isEmpty()) {
                throw unsupported("a walker wave needs its paths");
            }
            if (wave.formation() == WaveSpec.Formation.PACK && wave.paths().size() < wave.count()) {
                throw unsupported(
                        "a pack needs a path per unit: " + wave.paths().size() + " for " + wave.count());
            }
            for (int i = 0; i < wave.count(); i++) {
                WalkPath path = walkPath(wave.paths().get(i % wave.paths().size()));
                double delay;
                if (wave.formation() == WaveSpec.Formation.PINCER) {
                    if (wave.paths().size() == 1 && i % 2 == 1) {
                        path = path.mirrored();
                    }
                    delay = (i / 2) * CONVOY_INTERVAL_SECONDS;
                } else if (wave.formation() == WaveSpec.Formation.PACK) {
                    delay = i * PACK_INTERVAL_SECONDS;
                } else {
                    // M7 part D, round 40: a convoy's own interval (Level 17's Shellback pair, 3 s apart).
                    delay = i * wave.intervalSeconds().orElse(CONVOY_INTERVAL_SECONDS);
                }
                double x = path.x(0);
                double y = path.y(0);
                out.add(new Spawn(
                        SimStep.ticks(wave.t() + delay),
                        kind,
                        enemy(),
                        FlightPath.through(x, y, x, y - 1),
                        enemy().walker().orElseThrow().speed(),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(path)));
            }
        }

        /**
         * Units that enter from a side edge and hover there (design/enemies/air/mantis): a
         * {@code single} from its edge (left when none is given), a {@code pincer} one per edge in
         * turn; each flies in to its edge distance at a height picked within its hover range,
         * hovers its hover time and leaves through its edge (or down and out, without exit back).
         */
        void sideHovers() {
            EnemySpec.SideHover side = enemy().sideHover().orElseThrow();
            EnemySpec.Hover hover = required(enemy().hover(), "hover");
            boolean pincer = wave.formation() == WaveSpec.Formation.PINCER;
            if (!pincer && wave.formation() != WaveSpec.Formation.SINGLE) {
                throw unsupported("a side hover enters as a single or a pincer");
            }
            for (int i = 0; i < wave.count(); i++) {
                boolean left = pincer ? i % 2 == 0 : wave.edge() != Edge.RIGHT;
                double y = HEIGHT - hover.depth().pick(rng);
                double x = left ? side.edgeX() : WIDTH - side.edgeX();
                double outside = left ? -OUTSIDE : WIDTH + OUTSIDE;
                Spawn.Exit exit =
                        side.exitBack() ? new Spawn.Exit(false, left ? -1 : 1, 0) : Spawn.Exit.awayFromCentre(x);
                add(
                        i,
                        wave.t(),
                        FlightPath.through(outside, y, x, y),
                        speed(enemy().speed()),
                        wave.holdSeconds().orElseGet(() -> hover.seconds().pick(rng)),
                        Optional.empty(),
                        exit);
            }
        }

        /**
         * Segment chains (design/enemies/air/coilwyrm): unit i's head flies the wave's path i
         * (wrapping; with a single path every second unit flies it mirrored, so a pair crosses),
         * in (x, depth below the top edge) points, from the front, the rear or a side as the path
         * starts; units after the first follow {@link #DIVE_STAGGER_SECONDS} apart. A loop-back
         * re-enters its gap after the path's end on its own path shifted to start at the head's x
         * (straight up without one), the bottom edge warned ahead.
         */
        void chains() {
            if (enemy().crawl().isPresent()) {
                crawlers();
                return;
            }
            if (wave.paths().isEmpty()) {
                throw unsupported("a segment chain flies its wave's paths");
            }
            for (int i = 0; i < wave.count(); i++) {
                List<WaveSpec.At> points = wave.paths().get(i % wave.paths().size());
                boolean mirror = wave.paths().size() == 1 && i % 2 == 1;
                double[] xy = points(points, mirror);
                FlightPath path = FlightPath.through(xy);
                Optional<Spawn.Loop> loop = wave.loopBack().map(back -> loop(back, xy[xy.length - 2], mirror));
                out.add(new Spawn(
                        SimStep.ticks(wave.t() + i * DIVE_STAGGER_SECONDS),
                        kind,
                        enemy(),
                        path,
                        speed(enemy().speed()),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        loop));
            }
        }

        /**
         * M7 part C, ground chains (design/enemies/ground/threadcrawler): unit i's head crawls its {@link
         * EnemySpec.Crawl} path in ground space from the wave's time on (units after the first {@link
         * #DIVE_STAGGER_SECONDS} apart); the wave's paths are not read, and its spawn's path is a stand-in.
         */
        private void crawlers() {
            EnemySpec.Crawl crawl = enemy().crawl().orElseThrow();
            double x = crawl.x(0);
            FlightPath standIn = FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE);
            for (int i = 0; i < wave.count(); i++) {
                out.add(new Spawn(
                        SimStep.ticks(wave.t() + i * DIVE_STAGGER_SECONDS),
                        kind,
                        enemy(),
                        standIn,
                        crawl.speed(),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
            }
        }

        /** A loop-back's path, shifted so it starts at {@code headX} (clamped into the field). */
        private Spawn.Loop loop(WaveSpec.LoopBack back, double headX, boolean mirror) {
            double x0 = Math.clamp(headX, OUTSIDE, WIDTH - OUTSIDE);
            double[] xy;
            if (back.path().isEmpty()) {
                xy = new double[] {x0, -OUTSIDE * 2, x0, HEIGHT + OUTSIDE * 4};
            } else {
                List<WaveSpec.At> points = back.path();
                xy = new double[Math.max(2, points.size()) * 2];
                double first = mirror
                        ? WIDTH - points.getFirst().x()
                        : points.getFirst().x();
                for (int k = 0; k < points.size(); k++) {
                    double x =
                            mirror ? WIDTH - points.get(k).x() : points.get(k).x();
                    xy[2 * k] = x - first + x0;
                    xy[2 * k + 1] = HEIGHT - points.get(k).depth();
                }
                if (points.size() == 1) {
                    xy[2] = xy[0];
                    xy[3] = HEIGHT + OUTSIDE * 4;
                }
            }
            return new Spawn.Loop(
                    FlightPath.through(xy),
                    back.afterSeconds(),
                    Math.max(
                            WaveSchedule.EDGE_WARNING_SECONDS,
                            wave.warningSeconds().orElse(0.0)));
        }

        /**
         * M5 part D, a swarm (design/enemies/air/mote-swarm): {@code count} members of a flock round a
         * leader point that flies the wave's route (its first path, in (x, depth below the top edge)
         * points, starting outside the play field) at the unit's path speed and its loop-backs at
         * the flock's dive speed, each re-entering {@code after} s past the previous path's end on
         * the loop-back's path shifted to start at that end's x (straight up without one), warned at
         * the bottom edge ahead. The members start together in a seeded cloud round the route's first
         * point, about the flock's separation apart.
         */
        void swarm() {
            if (enemy().chase().isPresent() && enemy().flock().isEmpty()) {
                chasers();
                return;
            }
            EnemySpec.FlockSpec flock = required(enemy().flock(), "a flock");
            if (wave.paths().isEmpty()) {
                throw unsupported("a swarm flies its wave's route (paths)");
            }
            if (wave.count() > flock.max()) {
                throw unsupported("a flock has at most " + flock.max() + " members, not " + wave.count());
            }
            double[] xy = points(wave.paths().getFirst(), false);
            FlightPath path = FlightPath.through(xy);
            List<FlightPath> loops = new java.util.ArrayList<>();
            double warning = Math.max(
                    WaveSchedule.EDGE_WARNING_SECONDS, wave.warningSeconds().orElse(0.0));
            double gap = 0;
            if (wave.loopBack().isPresent()) {
                WaveSpec.LoopBack back = wave.loopBack().get();
                gap = back.afterSeconds();
                double endX = xy[xy.length - 2];
                for (int k = 0; k < back.count(); k++) {
                    Spawn.Loop loop = loop(back, endX, false);
                    loops.add(loop.path());
                    endX = loop.path().endX();
                }
            }
            Spawn.Route route = new Spawn.Route(path, loops, speed(enemy().speed()), flock.diveSpeed(), gap, warning);
            double startX = xy[0];
            double startY = xy[1];
            // A sunflower cloud, about the separation apart, each point jittered by the seed.
            for (int i = 0; i < wave.count(); i++) {
                double r = flock.separation() * 0.6 * Math.sqrt(i + 0.5);
                double angle = i * GOLDEN_ANGLE + rng.range(-0.3, 0.3);
                double x = startX + r * StrictMath.cos(angle);
                double y = startY + r * StrictMath.sin(angle);
                out.add(new Spawn(
                        SimStep.ticks(wave.t()),
                        kind,
                        enemy(),
                        path,
                        route.speed(),
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Spawn.Swarm(route, i, x, y))));
            }
        }

        /**
         * M7 part G, a pursuit (design/campaign Level 20, The pursuit swarm): {@code count} members of a flock in a
         * seeded sunflower cloud above the top edge (a quarter of the width in from the wave's edge, the middle without
         * one), its leader point coming down into the band at the climb speed; then the {@link Pursuit} flies it. Every
         * {@code refillEvery} s after the wave's time, before its break-off, {@code refill} more members are planned as
         * refills (counted in the density): each joins the flock from the top edge at the leader point's x as it
         * enters, in a cloud of its own, and only while the flock has room. Planned in screen space: never flipped.
         */
        void pursuit() {
            EnemySpec.FlockSpec flock = required(enemy().flock(), "a flock");
            WaveSpec.Pursuit block = wave.pursuit().orElseThrow();
            if (wave.count() > flock.max()) {
                throw unsupported("a flock has at most " + flock.max() + " members, not " + wave.count());
            }
            double startX =
                    switch (wave.edge()) {
                        case LEFT -> WIDTH / 4;
                        case RIGHT -> WIDTH * 3 / 4;
                        case NONE, ALTERNATING, TOP, BOTTOM -> WIDTH / 2;
                    };
            double reach = flock.separation() * 0.6 * Math.sqrt(Math.max(wave.count(), block.refill()));
            double startY = HEIGHT + OUTSIDE + reach;
            double bandY = HEIGHT - (block.bandTop() + block.bandBottom()) / 2;
            FlightPath path = FlightPath.through(startX, startY, startX, bandY);
            double warning = Math.max(
                    WaveSchedule.EDGE_WARNING_SECONDS, wave.warningSeconds().orElse(0.0));
            Spawn.Route route = new Spawn.Route(
                    path, List.of(), block.climbSpeed(), flock.diveSpeed(), 0, warning, Optional.of(block));
            for (int i = 0; i < wave.count(); i++) {
                double r = flock.separation() * 0.6 * Math.sqrt(i + 0.5);
                double angle = i * GOLDEN_ANGLE + rng.range(-0.3, 0.3);
                out.add(member(
                        wave.t(),
                        path,
                        route,
                        i,
                        startX + r * StrictMath.cos(angle),
                        startY + r * StrictMath.sin(angle),
                        false));
            }
            int refills = block.refills(wave.t());
            int member = wave.count();
            for (int k = 1; k <= refills; k++) {
                double t = wave.t() + k * block.refillEvery();
                for (int i = 0; i < block.refill(); i++) {
                    double r = flock.separation() * 0.6 * Math.sqrt(i + 0.5);
                    double angle = i * GOLDEN_ANGLE + rng.range(-0.3, 0.3);
                    out.add(member(
                            t,
                            path,
                            route,
                            member++,
                            r * StrictMath.cos(angle),
                            startY + r * StrictMath.sin(angle),
                            true));
                }
            }
        }

        /** M7 part G: a pursuit's member {@code member} entering at {@code t} (a refill: see {@link Spawn.Swarm}). */
        private Spawn member(
                double t, FlightPath path, Spawn.Route route, int member, double x, double y, boolean refill) {
            return new Spawn(
                    SimStep.ticks(t),
                    kind,
                    enemy(),
                    path,
                    route.speed(),
                    0,
                    Optional.empty(),
                    Spawn.Exit.DOWN,
                    false,
                    // The wave's carried pickups ride on the members entering with it.
                    refill ? Optional.empty() : carried(member),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Spawn.Swarm(route, member, x, y, refill)));
        }

        /**
         * M5 part F, a swarm of chasers (design/enemies/air/lamprey, user decision F9 = a): no flock; the
         * members start together at the wave's time in a seeded sunflower cloud about
         * {@link #CHASER_SPACING} apart just outside the entry edge (the front or the rear: a quarter
         * of the width in from the wave's edge, the middle without one; a side: two thirds up it, both
         * sides taking turns without an edge) and fly straight in at their path speed; each starts its
         * chase a seeded 0 to {@link #CHASER_DELAY_MOST} s after its centre crossed the edge.
         */
        private void chasers() {
            int count = wave.count();
            double reach = CHASER_SPACING * 0.6 * Math.sqrt(count);
            double laneX =
                    switch (wave.edge()) {
                        case LEFT -> WIDTH / 4;
                        case RIGHT -> WIDTH * 3 / 4;
                        case NONE, ALTERNATING, TOP, BOTTOM -> WIDTH / 2;
                    };
            double across = HEIGHT + 2 * (OUTSIDE + reach);
            double speed = speed(enemy().speed());
            for (int i = 0; i < count; i++) {
                double r = CHASER_SPACING * 0.6 * Math.sqrt(i + 0.5);
                double angle = i * GOLDEN_ANGLE + rng.range(-0.3, 0.3);
                double delay = rng.range(0, CHASER_DELAY_MOST);
                double cx;
                double cy;
                double dx = 0;
                double dy = 0;
                switch (wave.entry()) {
                    case FRONT -> {
                        cx = laneX;
                        cy = HEIGHT + OUTSIDE + reach;
                        dy = -1;
                    }
                    case REAR -> {
                        cx = laneX;
                        cy = -OUTSIDE - reach;
                        dy = 1;
                    }
                    case SIDES -> {
                        boolean left =
                                switch (wave.edge()) {
                                    case LEFT -> true;
                                    case RIGHT -> false;
                                    case NONE, ALTERNATING, TOP, BOTTOM -> i % 2 == 0;
                                };
                        cx = left ? -OUTSIDE - reach : WIDTH + OUTSIDE + reach;
                        cy = HEIGHT * 2 / 3;
                        dx = left ? 1 : -1;
                    }
                    default -> throw unsupported("a swarm enters from an edge");
                }
                double x = cx + r * StrictMath.cos(angle);
                double y = cy + r * StrictMath.sin(angle);
                out.add(new Spawn(
                        SimStep.ticks(wave.t()),
                        kind,
                        enemy(),
                        FlightPath.through(x, y, x + dx * across, y + dy * across),
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        delay));
            }
        }

        /**
         * M8 part G (design/campaign Level 27, the rear Barbfin pairs; §5 G9): a rear ambush of units that hold above
         * the ship without an ambush path ({@link EnemySpec.Hover#aboveShip()}, the Barbfin): {@code count} (1–4)
         * units, unit i in the lane x = (i + 1) × 480 ÷ (n + 1), entering together from below the bottom edge at the
         * wave's time and rising up the lane at their speed until they are their own height above the ship ({@link
         * HoverHold#stopsNow}; the hover depth's min below the top edge at the latest, its path's end); they hold as
         * from the front (their drift, their facing on the ship) and leave down and out to their side. Warned at the
         * bottom edge as a rear wave.
         */
        private void rearSwoops() {
            EnemySpec.Hover hover = enemy().hover().orElseThrow();
            if (wave.count() < 1 || wave.count() > AMBUSH_MOST) {
                throw unsupported("a rear ambush has 1 to " + AMBUSH_MOST + " units, not " + wave.count());
            }
            double speed = speed(enemy().speed());
            double top = HEIGHT - hover.depth().min();
            for (int i = 0; i < wave.count(); i++) {
                double x = (i + 1) * WIDTH / (wave.count() + 1);
                add(
                        i,
                        wave.t(),
                        FlightPath.through(x, -OUTSIDE, x, top),
                        speed,
                        hover.seconds().pick(rng),
                        Optional.empty(),
                        Spawn.Exit.awayFromCentre(x));
            }
        }

        /**
         * M5 part D, a rear ambush (design/enemies/air/wraith): {@code count} (1–4) units, unit i in
         * the lane x = (i + 1) × 480 ÷ (n + 1) (a lone one in the middle); each enters at the top edge
         * at the wave's time and flies its {@link EnemySpec.Ambush}: down its lane at its speed past
         * the ship and off the bottom edge, its gap below it, back up to its hold point (a height in
         * its hover range), the decloak and the hold, then out up the nearer side lane.
         */
        void rearAmbush() {
            if (enemy().ambush().isEmpty()
                    && enemy().hover().flatMap(EnemySpec.Hover::aboveShip).isPresent()) {
                rearSwoops();
                return;
            }
            EnemySpec.Ambush ambush = required(enemy().ambush(), "an ambush path");
            EnemySpec.Hover hover = required(enemy().hover(), "hover");
            if (wave.count() < 1 || wave.count() > AMBUSH_MOST) {
                throw unsupported("a rear ambush has 1 to " + AMBUSH_MOST + " units, not " + wave.count());
            }
            double flash = enemy().cloak().map(EnemySpec.Cloak::flashSeconds).orElse(0.0);
            double warning = Math.max(
                    WaveSchedule.EDGE_WARNING_SECONDS, wave.warningSeconds().orElse(0.0));
            double speed = speed(enemy().speed());
            for (int i = 0; i < wave.count(); i++) {
                double x = (i + 1) * WIDTH / (wave.count() + 1);
                double holdY = HEIGHT - hover.depth().pick(rng);
                boolean left = x <= WIDTH / 2;
                double laneX = left ? ambush.lane() : WIDTH - ambush.lane();
                FlightPath down = FlightPath.through(x, HEIGHT + OUTSIDE, x, -OUTSIDE);
                FlightPath rise = FlightPath.through(x, -OUTSIDE, x, holdY);
                // Out to its side lane while it climbs, then straight up the lane.
                FlightPath exit = FlightPath.through(
                        x,
                        holdY,
                        laneX,
                        holdY + AMBUSH_EXIT_RISE,
                        laneX,
                        holdY + 2 * AMBUSH_EXIT_RISE,
                        laneX,
                        HEIGHT + OUTSIDE);
                out.add(new Spawn(
                        SimStep.ticks(wave.t()),
                        kind,
                        enemy(),
                        down,
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Spawn.Ambush(
                                rise,
                                ambush.gapSeconds(),
                                flash,
                                hover.seconds().pick(rng),
                                exit,
                                ambush.exitSpeed(),
                                warning)),
                        Optional.empty()));
            }
        }

        /**
         * M5 part E, a field (design/enemies/naval/driftjelly; the stated defaults of 2026-10-08): {@code
         * count} units scattered over the wave's area by the level's seeded scatter, a jittered grid:
         * the area is cut into whole cells of at least the spacing, the units take distinct cells in a
         * seeded order and sit in them jittered by the room the cell leaves beyond the spacing, so no
         * two are closer than the spacing. The area's leading (bottom) edge enters at the top edge at
         * the wave's time: every unit appears then, above the top edge, and scrolls in with the ground,
         * drifting at the enemy's speed along the current. With a {@link EnemySpec.Submerge} the
         * share {@code start} of them (rounded) begins submerged, in the seeded order; each unit's swap
         * timer gets a seed of its own.
         */
        void field() {
            WaveSpec.Field field = required(wave.field(), "a field's area");
            double spacing = field.spacing();
            int columns = (int) Math.floor(field.width() / spacing);
            int rows = (int) Math.floor(field.height() / spacing);
            if ((long) columns * rows < wave.count()) {
                throw unsupported("a field of " + field.width() + "×" + field.height() + " px holds at most "
                        + field.capacity() + " units " + spacing + " px apart, not " + wave.count());
            }
            double cellWidth = field.width() / columns;
            double cellHeight = field.height() / rows;
            int[] cells = new int[columns * rows];
            for (int c = 0; c < cells.length; c++) {
                cells[c] = c;
            }
            double speed = enemy().speed();
            double vx = speed * StrictMath.sin(field.currentRadians());
            double vy = -speed * StrictMath.cos(field.currentRadians());
            int submerged = enemy().submerge()
                    .map(submerge -> (int) Math.round(submerge.start() * wave.count()))
                    .orElse(0);
            double left = field.x() - field.width() / 2;
            // The scatter first (the seeded draws in the order they always had), then the units: M8 part F's centre
            // pickup goes to the unit nearest the area's centre (WaveSpec.Carried.CENTRE).
            int count = wave.count();
            double[] xs = new double[count];
            double[] aboves = new double[count];
            long[] seeds = new long[count];
            for (int i = 0; i < count; i++) {
                int pick = i + rng.nextInt(cells.length - i);
                int cell = cells[pick];
                cells[pick] = cells[i];
                cells[i] = cell;
                double jitterX = (cellWidth - spacing) / 2;
                double jitterY = (cellHeight - spacing) / 2;
                xs[i] = left + (cell % columns + 0.5) * cellWidth + rng.range(-jitterX, jitterX);
                aboves[i] = (cell / columns + 0.5) * cellHeight + rng.range(-jitterY, jitterY);
                seeds[i] = rng.nextLong();
            }
            int centre = -1;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < count; i++) {
                double distance = Math.hypot(xs[i] - field.x(), aboves[i] - field.height() / 2);
                if (distance < nearest) {
                    nearest = distance;
                    centre = i;
                }
            }
            Optional<PickupType> centrePickup = wave.carried().stream()
                    .filter(WaveSpec.Carried::centreUnit)
                    .map(WaveSpec.Carried::pickup)
                    .findFirst();
            for (int i = 0; i < count; i++) {
                double x = xs[i];
                double above = aboves[i];
                out.add(new Spawn(
                        SimStep.ticks(wave.t()),
                        kind,
                        enemy(),
                        FlightPath.through(x, HEIGHT + above, x, -OUTSIDE),
                        speed,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        i == centre && centrePickup.isPresent() ? centrePickup : carried(i),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Spawn.Field(x, HEIGHT + above, vx, vy, i < submerged, seeds[i]))));
            }
        }

        /** A route of (x, depth below the top edge) points as play-field points; one point: straight down through it. */
        private static FlightPath route(List<WaveSpec.At> points) {
            return FlightPath.through(points(points, false));
        }

        /** (x, depth) points as play-field x, y pairs, mirrored left to right with {@code mirror}. */
        private static double[] points(List<WaveSpec.At> points, boolean mirror) {
            double[] xy = new double[Math.max(2, points.size()) * 2];
            for (int k = 0; k < points.size(); k++) {
                double x = points.get(k).x();
                xy[2 * k] = mirror ? WIDTH - x : x;
                xy[2 * k + 1] = HEIGHT - points.get(k).depth();
            }
            if (points.size() == 1) {
                xy[2] = xy[0];
                xy[3] = -OUTSIDE * 4;
            }
            return xy;
        }

        private static WalkPath walkPath(List<WaveSpec.At> points) {
            double[] xy = new double[points.size() * 2];
            for (int i = 0; i < points.size(); i++) {
                xy[2 * i] = points.get(i).x();
                xy[2 * i + 1] = HEIGHT - points.get(i).depth();
            }
            return WalkPath.through(xy);
        }

        private void add(
                int unit,
                double t,
                FlightPath path,
                double speed,
                double hold,
                Optional<Spawn.Orbit> orbit,
                Spawn.Exit exit) {
            boolean leads = wave.formation() == WaveSpec.Formation.CIRCLE
                    && unit % 2 == 1
                    && enemy().gun().map(EnemyGun::leadsTargetInCircle).orElse(false);
            out.add(new Spawn(SimStep.ticks(t), kind, enemy(), path, speed, hold, orbit, exit, leads, carried(unit)));
        }

        private Optional<PickupType> carried(int unit) {
            for (WaveSpec.Carried carried : wave.carried()) {
                if (!carried.centreUnit()
                        && unit
                                == (carried.lastUnit()
                                        ? wave.count() - 1
                                        : Math.min(carried.unit(), wave.count() - 1))) {
                    return Optional.of(carried.pickup());
                }
            }
            return Optional.empty();
        }

        private void requireFront() {
            if (wave.entry() != Entry.FRONT) {
                throw unsupported("a " + wave.formation() + " enters from the front");
            }
        }

        private <T> T required(Optional<T> value, String what) {
            return value.orElseThrow(() -> unsupported("needs " + what));
        }

        private IllegalArgumentException unsupported(String message) {
            return new IllegalArgumentException(
                    "wave at t=" + wave.t() + " (" + wave.formation() + " of " + enemy().slug() + "): " + message);
        }
    }
}
