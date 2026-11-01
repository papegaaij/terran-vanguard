package vanguard.sim;

import java.util.List;

/**
 * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The cruisers (draft); design/campaign Level 28, sections 8–10;
 * m8h-gaps.md §1.3): the finale's two Ascendancy cruisers, invulnerable {@code low-air} set pieces on the level clock
 * ({@link CruiserRun}), at one difficulty (the content resolves the difficulty hooks). Times are level clock seconds,
 * places simulation px (y up).
 *
 * <p>They rise from {@code riseAt} to {@code runAt} (the presentation's growth; no hull box, no fire), keep pace on the
 * flanks and fire until {@code endAt} (every shot of an attack started before it is out by then), then fall off the
 * bottom edge over {@code fallSeconds}, their hulls still solid. Each hull is {@code hull} in size, its inner edge at
 * {@code leftInner} (the left one's right edge) and {@code rightInner} (the right one's left edge), centred on the
 * field's height while they run.
 *
 * <p>M10 part H, step H1b (design/campaign Level 42, The home fleet's cruisers; m10h-gaps.md §1 #15): its {@code runs},
 * each of both hulls (Level 28's one run) or of one flank at a time (Level 42's: A on the left, then B on the right),
 * each on its own times; {@code riseAt} to {@code fallSeconds} are then the first rise, the first run, the last end and
 * the last run's fall. The flak is optional ({@link Flak#none()}) and so is the double band (never: an infinite {@link
 * Broadside#doubleAfterSeconds()}).
 *
 * @param riseAt the rise's start (the presentation's dim-to-full growth), s
 * @param runAt the run's start: the hulls solid, the first attacks' tells, s
 * @param endAt the run's end: no shot after it, s
 * @param fallSeconds their fall off the bottom edge after {@code endAt}, s
 * @param hull a hull's box (≈ 220 × 640)
 * @param leftInner the left hull's inner (right) edge, px from the left
 * @param rightInner the right hull's inner (left) edge, px from the left
 * @param broadside the broadsides
 * @param flak the flak
 * @param pointDefence the point defence
 * @param runs M10 part H, step H1b: its runs, in time order (Level 28's: one run of both hulls)
 */
public record CruiserRunSpec(
        double riseAt,
        double runAt,
        double endAt,
        double fallSeconds,
        Hitbox hull,
        double leftInner,
        double rightInner,
        Broadside broadside,
        Flak flak,
        PointDefence pointDefence,
        List<Run> runs) {
    /** M10 part H, step H1b: which hulls a run flies. */
    public enum Flank {
        /** Both hulls, one on each flank (Level 28's). */
        BOTH,
        /** The left hull alone. */
        LEFT,
        /** The right hull alone. */
        RIGHT;

        /** Whether this run flies the hull on side {@code side} ({@link CruiserRun#LEFT}, {@link CruiserRun#RIGHT}). */
        public boolean flies(int side) {
            return this == BOTH || (side == CruiserRun.LEFT ? this == LEFT : this == RIGHT);
        }
    }

    /**
     * M10 part H, step H1b: one run: its hulls rise from {@code riseAt}, run (solid, firing) from {@code runAt} to
     * {@code endAt} and fall off the bottom edge over {@code fallSeconds}; with {@code frigates} the CDF frigate pair
     * trades fire with it (friendly scenery, marked; their tracers presentation only).
     *
     * @param flank which hulls it flies
     * @param riseAt its rise's start, s
     * @param runAt its run's start, s
     * @param endAt its run's end, s
     * @param fallSeconds its fall after the end, s
     * @param frigates whether the CDF frigate pair flies beside it (presentation only)
     */
    public record Run(Flank flank, double riseAt, double runAt, double endAt, double fallSeconds, boolean frigates) {
        public Run {
            java.util.Objects.requireNonNull(flank);
            if (!(riseAt >= 0) || !(runAt >= riseAt) || !(endAt > runAt) || !(fallSeconds >= 0)) {
                throw new IllegalArgumentException("a cruiser run rises, then runs, then falls: " + riseAt + ", "
                        + runAt + ", " + endAt + ", " + fallSeconds);
            }
        }
    }

    /** The cruisers before M10 part H: one run of both hulls (Level 28's). */
    public CruiserRunSpec(
            double riseAt,
            double runAt,
            double endAt,
            double fallSeconds,
            Hitbox hull,
            double leftInner,
            double rightInner,
            Broadside broadside,
            Flak flak,
            PointDefence pointDefence) {
        this(
                riseAt,
                runAt,
                endAt,
                fallSeconds,
                hull,
                leftInner,
                rightInner,
                broadside,
                flak,
                pointDefence,
                List.of(new Run(Flank.BOTH, riseAt, runAt, endAt, fallSeconds, false)));
    }

    /** M10 part H, step H1b: the cruisers flying {@code runs} in time order (one flank at a time, or both). */
    public static CruiserRunSpec ofRuns(
            List<Run> runs,
            Hitbox hull,
            double leftInner,
            double rightInner,
            Broadside broadside,
            Flak flak,
            PointDefence pointDefence) {
        if (runs.isEmpty()) {
            throw new IllegalArgumentException("the cruisers fly at least one run");
        }
        Run first = runs.getFirst();
        Run last = runs.getLast();
        return new CruiserRunSpec(
                first.riseAt(),
                first.runAt(),
                last.endAt(),
                last.fallSeconds(),
                hull,
                leftInner,
                rightInner,
                broadside,
                flak,
                pointDefence,
                runs);
    }

    public CruiserRunSpec {
        runs = List.copyOf(runs);
        if (runs.isEmpty()
                || runs.getFirst().riseAt() != riseAt
                || runs.getFirst().runAt() != runAt
                || runs.getLast().endAt() != endAt
                || runs.getLast().fallSeconds() != fallSeconds) {
            throw new IllegalArgumentException(
                    "the cruisers' times are their runs' first rise and run, last end and fall");
        }
        for (int i = 1; i < runs.size(); i++) {
            Run before = runs.get(i - 1);
            Run run = runs.get(i);
            if (!(run.runAt() >= before.runAt()) || !(run.endAt() >= before.endAt())) {
                throw new IllegalArgumentException("the cruisers' runs come in time order");
            }
            for (int j = 0; j < i; j++) {
                Run other = runs.get(j);
                boolean shared =
                        run.flank() == Flank.BOTH || other.flank() == Flank.BOTH || run.flank() == other.flank();
                if (shared && run.riseAt() <= other.endAt() + other.fallSeconds()) {
                    throw new IllegalArgumentException("a hull flies one run at a time");
                }
            }
        }
        if (!(riseAt >= 0) || !(runAt >= riseAt) || !(endAt > runAt) || !(fallSeconds >= 0)) {
            throw new IllegalArgumentException("the cruisers rise, then run, then fall: " + riseAt + ", " + runAt + ", "
                    + endAt + ", " + fallSeconds);
        }
        if (!(hull.width() > 0) || !(hull.height() > 0)) {
            throw new IllegalArgumentException("a cruiser's hull has a size: " + hull);
        }
        if (!(leftInner >= 0) || !(rightInner <= PlayField.WIDTH) || !(rightInner > leftInner)) {
            throw new IllegalArgumentException("the cruisers' inner edges lie on the field, the left before the right: "
                    + leftInner + ", " + rightInner);
        }
    }

    /**
     * A broadside: a band {@code band} px tall across the field, told for {@code tellSeconds}, then a volley of {@code
     * shells} shells (spread evenly over the band's height) crosses it at {@code speed} px/s from the cruiser's side
     * (from the field's edge), each dealing {@code damage}. One every {@code everySeconds} from the run's start, the
     * flanks in turn (left first), the band on the ship's y and then a random one in turn; from {@code
     * doubleAfterSeconds} into the run two bands at once, the second from the other flank, at a random place leaving at
     * least {@code freeBand} px free between them ({@link Double#POSITIVE_INFINITY}: never, easy's).
     */
    public record Broadside(
            double band,
            double tellSeconds,
            int shells,
            double speed,
            double damage,
            double everySeconds,
            double doubleAfterSeconds,
            double freeBand) {
        public Broadside {
            if (!(band > 0)
                    || !(tellSeconds >= 0)
                    || shells < 1
                    || !(speed > 0)
                    || !(damage >= 0)
                    || !(everySeconds > 0)
                    || !(doubleAfterSeconds >= 0)
                    || !(freeBand >= 0)) {
                throw new IllegalArgumentException("a broadside's numbers: band " + band + ", tell " + tellSeconds
                        + ", shells " + shells + ", speed " + speed + ", damage " + damage + ", every " + everySeconds
                        + ", double after " + doubleAfterSeconds + ", free " + freeBand);
            }
            if (Double.isFinite(doubleAfterSeconds) && !(2 * band + freeBand <= PlayField.HEIGHT)) {
                throw new IllegalArgumentException("two bands and the free band between them fit the field's height");
            }
        }
    }

    /**
     * Flak: {@code markers} markers ({@code radius} px, the boss lob rule) every {@code everySeconds} from the run's
     * start, the first on the ship and the others within {@code scatter} px of it; after {@code fuseSeconds} each
     * bursts: a ship inside its marker takes {@code damage}, and a ring of {@code ring} bullets ({@code ringDamage},
     * {@code speed} px/s) spreads from it.
     */
    public record Flak(
            int markers,
            double everySeconds,
            double fuseSeconds,
            double radius,
            double scatter,
            double damage,
            int ring,
            double speed,
            double ringDamage) {
        /** M10 part H, step H1b: no flak (Level 42's cruisers). */
        public static Flak none() {
            return new Flak(0, 1, 1, 0, 0, 0, 0, 1, 0);
        }

        public Flak {
            if (markers < 0
                    || !(everySeconds > 0)
                    || !(fuseSeconds > 0)
                    || !(radius >= 0)
                    || !(scatter >= 0)
                    || !(damage >= 0)
                    || ring < 0
                    || !(speed > 0)
                    || !(ringDamage >= 0)) {
                throw new IllegalArgumentException("flak's numbers: markers " + markers + ", every " + everySeconds
                        + ", fuse " + fuseSeconds + ", radius " + radius + ", scatter " + scatter + ", ring " + ring);
            }
        }
    }

    /**
     * Point defence: each cruiser's turret line fires an aimed burst of {@code burst} bullets ({@link
     * EnemyGun#BURST_GAP_SECONDS} apart, {@code damage}, {@code speed} px/s) every {@code everySeconds}, the right one
     * half an interval after the left; each burst from the next of the hull's {@code turrets} (heights on its inner
     * edge, px from the bottom) in turn.
     */
    public record PointDefence(int burst, double speed, double damage, double everySeconds, List<Double> turrets) {
        public PointDefence {
            turrets = List.copyOf(turrets);
            if (burst < 0 || !(speed > 0) || !(damage >= 0) || !(everySeconds > 0) || turrets.isEmpty()) {
                throw new IllegalArgumentException("point defence's numbers: burst " + burst + ", speed " + speed
                        + ", every " + everySeconds + ", turrets " + turrets);
            }
        }
    }
}
