package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * M7 part H (design/enemies/bosses/dust-colossus): a breaching arena boss's keys in its stat block ({@link EnemyData}):
 * the breach cycle ({@code movement.breach}), the suction toward an open mouth, phase 2's spine vents, phase 3's
 * eruption under the ship and the debris rocks ({@code boss.suction}, {@code vents}, {@code eruption}, {@code debris})
 * and the difficulties' changes to them.
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.Breach} and so
 * on, without growing that record's file.
 */
public sealed interface ColossusBlock permits EnemyData {
    /** The breach points a cycle knows, in the simulation's order (FRONT, LEFT, RIGHT, REAR). */
    List<String> EDGE_POINTS = List.of("front", "left", "right", "rear");

    /** Phase 3's point: under the ship (the eruption's). */
    String UNDER = "under";

    /**
     * A breaching boss's cycle (design: Breaches): the {@code tell} (the bulge), the {@code rise} ({@code buried} →
     * {@code ground} at its middle), {@code open} (the gullet open, the suction running), the bite ({@code bite_tell},
     * then the snap within {@code bite.radius} px), the {@code dive}, then {@code buried} s from the dive's end to the
     * next rise; a side or rear point warned {@code warn} s before its rise; the first tell {@code intro} s after the
     * arrival; the gullet's {@code glow} before each ring (0.5 s if not given). {@code points}: the mouth's centre at
     * {@link #EDGE_POINTS} ([x, y] px from the left and below the top edge, the screen's frame); {@code follow}: a
     * following phase's range of the ship's x (front, rear) or y below the top edge (the sides); {@code orders}: per
     * boss phase the points in turn.
     */
    record Breach(
            double tell,
            double rise,
            double open,
            double biteTell,
            double dive,
            double buried,
            double warn,
            double intro,
            Optional<Double> glow,
            Map<String, Point> points,
            Map<String, Span> follow,
            List<BreachOrder> orders,
            Bite bite) {
        /** The design's lime glow before each ring, s. */
        public static final double GLOW_SECONDS = 0.5;

        public Breach {
            points = Map.copyOf(points);
            follow = Map.copyOf(follow);
            orders = List.copyOf(orders);
            Check.positive("tell", tell);
            Check.positive("rise", rise);
            Check.positive("open", open);
            Check.positive("bite_tell", biteTell);
            Check.positive("dive", dive);
            Check.that(
                    buried >= tell, "buried: from the dive's end to the next rise, at least the tell, was " + buried);
            Check.notNegative("warn", warn);
            Check.notNegative("intro", intro);
            glow.ifPresent(g -> Check.notNegative("glow", g));
            Check.that(
                    points.keySet().equals(java.util.Set.copyOf(EDGE_POINTS)),
                    "points: front, left, right and rear, was " + points.keySet());
            Check.that(
                    follow.keySet().equals(java.util.Set.copyOf(EDGE_POINTS)),
                    "follow: front, left, right and rear, was " + follow.keySet());
            points.forEach((name, at) -> Check.that(
                    at.x() >= 0 && at.x() <= PlayField.WIDTH && at.y() >= 0 && at.y() <= PlayField.HEIGHT,
                    "points." + name + ": on the play field, was [" + at.x() + ", " + at.y() + "]"));
            Check.notEmpty("orders", orders);
        }

        /** The glow before each ring, s: its {@code glow}, {@link #GLOW_SECONDS} if not given. */
        public double glowSeconds() {
            return glow.orElse(GLOW_SECONDS);
        }

        /** The cycle's length from one rise to the next, s (the design's ≈ 12 s). */
        public double cycleSeconds() {
            return rise + open + biteTell + dive + buried;
        }

        /** This cycle with the difficulty's bite tell, if it changes it. */
        public Breach with(Optional<BreachChange> change) {
            return change.flatMap(BreachChange::biteTell)
                    .map(t -> new Breach(
                            tell, rise, open, t, dive, buried, warn, intro, glow, points, follow, orders, bite))
                    .orElse(this);
        }
    }

    /** The bite's snap: within {@code radius} px of the mouth's centre, the {@code bullet} class's damage, once. */
    record Bite(double radius, String bullet) {
        public Bite {
            Check.positive("radius", radius);
        }
    }

    /**
     * A boss phase's breach points in turn (repeating): {@link #EDGE_POINTS} or {@link #UNDER}; {@code follow}: they
     * follow the ship (false if not given).
     */
    record BreachOrder(String phase, List<String> points, Optional<Boolean> follow) {
        public BreachOrder {
            points = List.copyOf(points);
            Check.notEmpty("points", points);
            points.forEach(point -> Check.that(
                    EDGE_POINTS.contains(point) || UNDER.equals(point),
                    "points: front, left, right, rear or under, was '" + point + "'"));
        }

        /** Whether its points follow the ship. */
        public boolean follows() {
            return follow.orElse(false);
        }
    }

    /**
     * The suction (design: Suction and debris): while a mouth is open the ship and Rook drift toward its centre at
     * {@code speed} px/s within {@code inner} px, falling off linearly to 0 at {@code outer} px.
     */
    record Suction(double speed, double inner, double outer) {
        public Suction {
            Check.notNegative("speed", speed);
            Check.notNegative("inner", inner);
            Check.that(outer > inner, "outer: beyond inner, was " + outer);
        }

        /** This suction with the difficulty's speed, if it changes it. */
        public Suction with(Optional<SuctionChange> change) {
            return change.flatMap(SuctionChange::speed)
                    .map(s -> new Suction(s, inner, outer))
                    .orElse(this);
        }
    }

    /**
     * Phase {@code phase}'s spine vents: the {@code parts} rise with each mouth {@code distances} px from its centre
     * along the ridge toward the field's centre, fire {@code attack} while the mouth is open and sink with it; a
     * destroyed vent stays dead.
     */
    record Vents(List<String> parts, List<Double> distances, String attack, String phase) {
        public Vents {
            parts = List.copyOf(parts);
            distances = List.copyOf(distances);
            Check.notEmpty("parts", parts);
            Check.that(distances.size() == parts.size(), "distances: one per vent part");
            distances.forEach(d -> Check.positive("distances", d));
        }
    }

    /**
     * Phase {@code phase}'s eruption under the ship: a marker of {@code radius} px on the ship's position (at most
     * {@code inset} px inside the edges), not following, erupting after {@code marker} s with the {@code bullet}
     * class's damage; the mouth stands open {@code open} s, bites and dives, then {@code buried} s before the next.
     */
    record Eruption(
            double marker, double radius, String bullet, double inset, double open, double buried, String phase) {
        public Eruption {
            Check.positive("marker", marker);
            Check.positive("radius", radius);
            Check.notNegative("inset", inset);
            Check.positive("open", open);
            Check.notNegative("buried", buried);
        }

        /** This eruption with the difficulty's marker time, if it changes it. */
        public Eruption with(Optional<EruptionChange> change) {
            return change.flatMap(EruptionChange::marker)
                    .map(m -> new Eruption(m, radius, bullet, inset, open, buried, phase))
                    .orElse(this);
        }
    }

    /**
     * The debris rocks: one every {@code every} s by phase name, {@code size} px across, {@code hp}, on {@code layer},
     * pulled to the mouth from {@code speed} px/s accelerating at {@code acceleration} px/s², contact damage of the
     * {@code contact} size tier; hazards (no bounty, not in the density).
     */
    record DebrisData(
            Map<String, Double> every,
            Span size,
            double hp,
            double speed,
            double acceleration,
            String layer,
            Tier contact) {
        public DebrisData {
            every = Map.copyOf(every);
            Check.that(!every.isEmpty(), "every: at least one phase");
            every.values().forEach(e -> Check.positive("every", e));
            Check.positive("size", size.min());
            Check.positive("hp", hp);
            Check.positive("speed", speed);
            Check.notNegative("acceleration", acceleration);
            Layers.of(layer);
        }
    }

    /** A difficulty's change to a breach cycle: the bite's tell instead, s (easy 1.0, hard 0.6). */
    record BreachChange(Optional<Double> biteTell) {
        public BreachChange {
            biteTell.ifPresent(t -> Check.positive("bite_tell", t));
        }
    }

    /** A difficulty's change to a suction: its speed instead, px/s (easy 30, hard 55). */
    record SuctionChange(Optional<Double> speed) {
        public SuctionChange {
            speed.ifPresent(s -> Check.notNegative("speed", s));
        }
    }

    /** A difficulty's change to an eruption: its marker's time instead, s (easy 1.8, hard 1.2). */
    record EruptionChange(Optional<Double> marker) {
        public EruptionChange {
            marker.ifPresent(m -> Check.positive("marker", m));
        }
    }
}
