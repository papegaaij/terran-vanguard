package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * The level's {@code ground_targets}, {@code root_bursts} and {@code no_surface} zones: what stands or grows on the
 * ground and where it is placed.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.GroundTarget}).
 */
public sealed interface GroundTargetsBlock permits LevelData {
    /**
     * A ground target: a destructible ({@code hp}, {@code bounty}, {@code drop}; one that
     * {@code reveals} a secret drops its hidden crate when destroyed), a trigger hit
     * {@code hits} times that {@code reveals} a secret, or ground enemies of a stat block
     * ({@code enemy}, such as a Spine Turret nest), which may belong to a {@code group} of the
     * secondary objective. M5 part E: a destructible may belong to a {@code group} of its own (not
     * the objectives'), whose last one destroyed drops a placed pickup ({@code dropped_by: {group,
     * unit: last}}: Level 11's floating containers), and a trigger's {@code easy} / {@code hard}
     * change may give its {@code hits} (Level 11's sunken pod: hard 6), a destructible's its {@code hp} (round 44
     * Q.5: Level 21's survey crawler, hard 28).
     *
     * @param layer and {@code size}: a destructible's or trigger's layer and hit box in px; an
     *     enemy's come from its stat block
     * @param at where each of them is placed
     * @param hardened only {@code anti-ground} weapons damage it (design/enemies, layer rules)
     * @param bonusDrop a second pickup it drops with its {@code drop} (Level 04's supply drop: a
     *     special charge, only with a special fitted)
     * @param easy another placement list on easy (an enemy nest's size), a trigger's hits or a destructible's hp on easy
     * @param hard another placement list on hard, a trigger's hits or a destructible's hp on hard
     * @param sprite a destructible's sprite set: {@code <sprite>_0..2} (intact, damaged, wrecked)
     *     and {@code <sprite>-break_<n>}; Level 01's cargo container if left out
     * @param current M5 part E: an enemy nest's current, ° from straight down (positive to the
     *     right), along which units whose {@code terrain} drifts drift (Level 11's rafts); 0 if not given
     * @param drift M5 part F: a trigger on {@code low-air} drifting at {@code [x, y]} px/s in screen space
     *     (y up, as a tow's: negative drifts down the screen) from where it enters at the top edge,
     *     instead of scrolling with the ground (Level 12's canister)
     * @param side M5 part G (design/campaign Level 13, Ground targets): an enemy nest entering at a side
     *     edge and drifting across (the mortar floe); its {@code at} placements are then {@code [t, y]},
     *     y px from the bottom edge (y up), where each unit crosses the edge
     * @param link M7 part C (design/enemies/ground/tendril-anchor; Level 16's gates): {@code pairs}, an enemy nest of a
     *     unit with a {@code link} attack whose placements pair up in order, each two the anchors of one tendril
     *     (the same {@code t}, the left one first) on every difficulty's list
     * @param arc M7 part F (design/campaign Level 19, Up-facing turret nests): {@code up}, an enemy nest placed turned
     *     180°: its guns' silence arc measured from straight up the screen (silent above the ship, firing after it
     *     has passed)
     */
    public record GroundTarget(
            String target,
            int section,
            Optional<Integer> count,
            Optional<String> layer,
            Optional<Size> size,
            List<Placement> at,
            Optional<Double> hp,
            Optional<Integer> bounty,
            Optional<Pickup> drop,
            Optional<Integer> hits,
            Optional<String> reveals,
            Optional<Boolean> hardened,
            Optional<Pickup> bonusDrop,
            Optional<String> enemy,
            Optional<String> group,
            Optional<Placements> easy,
            Optional<Placements> hard,
            Optional<String> sprite,
            Optional<Boolean> dark,
            Optional<Double> current,
            Optional<Point> drift,
            Optional<SideEntry> side,
            Optional<String> link,
            Optional<String> arc) {
        public GroundTarget {
            arc.ifPresent(a -> Check.that(ARC_UP.equals(a), "arc: only 'up', was '" + a + "'"));
            Check.that(arc.isEmpty() || enemy.isPresent(), "only an enemy nest's guns are turned (arc: up)");
            link.ifPresent(kind -> Check.that(LINK_PAIRS.equals(kind), "link: only 'pairs', was '" + kind + "'"));
            Check.that(link.isEmpty() || enemy.isPresent(), "only an enemy nest is linked (link: pairs)");
            Check.that(link.isEmpty() || side.isEmpty(), "a linked nest stands on the ground, not in from a side");
            Check.that(side.isEmpty() || enemy.isPresent(), "only an enemy nest enters from a side (side)");
            Check.that(side.isEmpty() || current.isEmpty(), "a nest from a side drifts across it, no current");
            Check.that(
                    drift.isEmpty()
                            || (hits.isPresent()
                                    && layer.filter("low-air"::equals).isPresent()),
                    "only a trigger on low-air drifts");
            drift.ifPresent(d -> Check.that(d.y() < 0, "drift: it drifts down the screen (a negative y)"));
            layer.ifPresent(Layers::of);
            Check.that(current.isEmpty() || enemy.isPresent(), "only an enemy nest has a current");
            Check.that(at.size() == count.orElse(1), "at: one placement per target (count, or 1 for a trigger)");
            if (enemy.isPresent()) {
                Check.that(
                        hp.isEmpty() && hits.isEmpty() && bounty.isEmpty() && layer.isEmpty() && size.isEmpty(),
                        "an enemy's hp, bounty, layer and size come from its stat block");
                for (Optional<Placements> change : List.of(easy, hard)) {
                    Check.that(
                            change.isEmpty() || change.get().at().isPresent(),
                            "an enemy nest's easy/hard gives its at (its placements)");
                }
            } else {
                Check.that(layer.isPresent() && size.isPresent(), "a destructible or trigger needs layer and size");
                Check.that(hp.isPresent() != hits.isPresent(), "give hp (destructible) or hits (trigger)");
                Check.that(hits.isEmpty() || reveals.isPresent(), "a trigger (hits) reveals a secret");
                Check.that(bonusDrop.isEmpty() || drop.isPresent(), "a bonus_drop comes with a drop");
                Check.that(group.isEmpty() || hits.isEmpty(), "only enemies and destructibles have a group");
                for (Optional<Placements> change : List.of(easy, hard)) {
                    Check.that(
                            change.isEmpty() || change.get().at().isEmpty(),
                            "only enemies have easy/hard placements; a trigger's easy/hard gives its hits, a"
                                    + " destructible's its hp");
                    Check.that(
                            change.flatMap(Placements::hits).isEmpty() || hits.isPresent(),
                            "only enemies have easy/hard placements; a trigger's easy/hard gives its hits, a"
                                    + " destructible's its hp");
                    Check.that(
                            change.flatMap(Placements::hp).isEmpty() || hp.isPresent(),
                            "only a destructible's easy/hard gives hp");
                }
            }
            for (Optional<Placements> change : List.of(easy, hard)) {
                change.ifPresent(c -> Check.that(
                        (c.at().isPresent() ? 1 : 0) + (c.hits().isPresent() ? 1 : 0) + (c.hp().isPresent() ? 1 : 0)
                                == 1,
                        "an easy/hard change gives at (an enemy nest's placements), hits (a trigger's) or hp (a"
                                + " destructible's)"));
                change.flatMap(Placements::hits).ifPresent(n -> Check.positive("hits", n));
                change.flatMap(Placements::hp).ifPresent(n -> Check.positive("hp", n));
            }
            Check.that(
                    enemy.isPresent()
                            || easy.flatMap(Placements::at).isEmpty()
                                    && hard.flatMap(Placements::at).isEmpty(),
                    "only enemies have easy/hard placements");
        }
    }

    /** M7 part F: {@code arc: up}, an enemy nest placed turned 180° (Level 19's up-facing turret nests). */
    public static final String ARC_UP = "up";

    /** M7 part C: {@code link: pairs}, a ground target's anchors paired in order (Level 16's gates). */
    public static final String LINK_PAIRS = "pairs";

    /**
     * A difficulty's change to a ground target: an enemy nest's placements ({@code at}), (M5 part
     * E) a trigger's {@code hits} or (round 44 Q.5) a destructible's {@code hp}.
     */
    public record Placements(Optional<List<Placement>> at, Optional<Integer> hits, Optional<Double> hp) {
        /** An enemy nest's placements. */
        public Placements(List<Placement> at) {
            this(Optional.of(at), Optional.empty(), Optional.empty());
        }
    }

    /** A ground object entering at the top edge at {@code t} seconds, {@code x} px from the left. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record Placement(double t, double x) {
        public Placement {
            Check.notNegative("t", t);
        }
    }

    /**
     * M5 part G (design/campaign Level 13, Ground targets): an enemy nest's entry at a side edge: its units
     * appear just outside the {@code from} edge ({@code left} or {@code right}) and drift across toward the
     * other one at {@code drift} px/s on top of the scroll (the mortar floe), warned 3 s ahead as a side
     * wave's edge.
     */
    public record SideEntry(LevelData.WindSide from, double drift) {
        public SideEntry {
            Check.positive("drift", drift);
        }
    }

    /**
     * M5 part H, a level's root bursts (design/campaign Level 14, Hazards; design/enemies, Boss attacks and
     * root bursts; decision H2 = a): each burst's paving cracks and its dust rises for {@code telegraph} s
     * from its {@code t} at its point, then its growths (ground units of its {@code growths} on the
     * difficulty) stand there {@code spacing} px apart, centred on the point, and scroll with the ground;
     * a growth takes no damage and fires nothing for {@code first_shot_delay} s after it appeared (hard's
     * own). The growths count in the objectives' group {@code group}.
     */
    public record RootBursts(
            double telegraph,
            double firstShotDelay,
            double spacing,
            String group,
            Optional<BurstChange> easy,
            Optional<BurstChange> hard,
            List<RootBurst> bursts) {
        public RootBursts {
            Check.notNegative("telegraph", telegraph);
            Check.notNegative("first_shot_delay", firstShotDelay);
            Check.positive("spacing", spacing);
            Check.notEmpty("bursts", bursts);
            bursts = List.copyOf(bursts);
            for (int i = 1; i < bursts.size(); i++) {
                Check.that(bursts.get(i).t() >= bursts.get(i - 1).t(), "bursts[" + i + "]: listed in time order");
            }
        }

        /** The first shot's delay on {@code difficulty}: hard's own if given. */
        public double firstShotDelay(Difficulty difficulty) {
            Optional<BurstChange> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case MEDIUM -> Optional.empty();
                        case HARD -> hard;
                    };
            return change.flatMap(BurstChange::firstShotDelay).orElse(firstShotDelay);
        }
    }

    /** M5 part H: a difficulty's change to the root bursts: the growths' first-shot delay (hard's 1.0 s). */
    public record BurstChange(Optional<Double> firstShotDelay) {
        public BurstChange {
            firstShotDelay.ifPresent(d -> Check.notNegative("first_shot_delay", d));
        }
    }

    /**
     * M5 part H, one root burst: at {@code t} (its telegraph's start) at ({@code x}, {@code y}), px from the
     * left edge and below the top edge then (100–400 px below it); its {@code growths} by enemy slug, left
     * to right, and an easy or hard list instead.
     */
    public record RootBurst(
            double t,
            double x,
            double y,
            List<String> growths,
            Optional<Growths> easy,
            Optional<Growths> hard,
            Optional<String> notes) {
        public RootBurst {
            Check.notNegative("t", t);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: on the play field, was " + x);
            Check.notEmpty("growths", growths);
            growths = List.copyOf(growths);
        }

        /** Its growths on {@code difficulty}. */
        public List<String> growthsOn(Difficulty difficulty) {
            Optional<Growths> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case MEDIUM -> Optional.empty();
                        case HARD -> hard;
                    };
            return change.map(Growths::growths).orElse(growths);
        }
    }

    /** M5 part H: a root burst's growths on a difficulty. */
    public record Growths(List<String> growths) {
        public Growths {
            Check.notEmpty("growths", growths);
            growths = List.copyOf(growths);
        }
    }

    /**
     * M7 part B: an area of the ground where no Burrower surfaces (Level 15's station pads, its hab and the rail
     * line): {@code size} px wide and tall, its centre at {@code x} px from the left edge, passing the middle of the
     * screen at {@code t} s (as a backdrop piece is placed); it scrolls with the ground.
     */
    public record NoSurface(double t, double x, Size size) {
        public NoSurface {
            Check.notNegative("t", t);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: inside the play field, was " + x);
        }
    }
}
