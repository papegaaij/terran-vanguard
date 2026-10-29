package vanguard.sim;

import java.util.Optional;

/**
 * An enemy fixed to the ground layer (a turret), entering at the top edge at {@code t} at
 * {@code x}, in the secondary objective's group {@code group} (-1 for none). M5 part E: a unit
 * whose stat block drifts (a raft) drifts along its nest's current, {@code currentRadians} from
 * straight down, positive to the right. M5 part H: a unit grown by a {@link RootBurst} appears at
 * its burst's point instead ({@link #fromBurst}): {@code t} is then when it appears (the burst's
 * time plus its telegraph) and {@code x} its own x (the burst's x plus its place in the burst).
 *
 * <p>M9 part B (design/campaign Level 29): a {@code holder} holds the level's arena (the rescue halt over Dock 9, held
 * by the clamp guns): the level clock stays held at the arena's end while one stands ({@link LevelClock}). A unit
 * {@code dormantUntil} a level time above 0 (Block C's SAM Nests, the SAM Nest's <em>dormant until t</em>) enters
 * dark: not hittable, not picked by any seeker, not firing, until that time on the level clock; then it wakes with a
 * {@link #WAKE_SECONDS} tell ({@link SimEvents.Type#WOKE}) and fires as its stat block says. 0 for none.
 */
public record GroundUnit(
        double t,
        double x,
        EnemySpec enemy,
        int group,
        double currentRadians,
        Optional<SideEntry> side,
        Optional<RootBurst> burst,
        boolean holder,
        double dormantUntil,
        Optional<Branch> branch) {
    /** M9 part B: a dormant unit's wake tell, s (design/enemies/ground/sam-nest: the rack lifts and lights red). */
    public static final double WAKE_SECONDS = 1;

    public GroundUnit {
        java.util.Objects.requireNonNull(branch);
        if (branch.isPresent() && (dormantUntil > 0 || side.isPresent() || burst.isPresent() || holder)) {
            throw new IllegalArgumentException(
                    "a branch's unit enters at the top edge with the ground, dormant unless chosen (M9 part E)");
        }
        if (!(dormantUntil >= 0) || (dormantUntil > 0 && !(dormantUntil > t))) {
            throw new IllegalArgumentException("a dormant unit wakes after it entered: " + dormantUntil + " s");
        }
        if (dormantUntil > 0 && (side.isPresent() || burst.isPresent())) {
            throw new IllegalArgumentException("a dormant unit enters at the top edge with the ground");
        }
        if (side.isPresent() && burst.isPresent()) {
            throw new IllegalArgumentException("a ground unit enters at a side or grows in a root burst, not both");
        }
    }

    /** Of no fork's branch (before M9 part E). */
    public GroundUnit(
            double t,
            double x,
            EnemySpec enemy,
            int group,
            double currentRadians,
            Optional<SideEntry> side,
            Optional<RootBurst> burst,
            boolean holder,
            double dormantUntil) {
        this(t, x, enemy, group, currentRadians, side, burst, holder, dormantUntil, Optional.empty());
    }

    /**
     * M9 part E (design/campaign Level 32, The forks): this unit as one of {@code fork}'s branch's: dormant (dark, not
     * hittable, never picked, not firing, no bounty) unless that side is chosen; chosen, it wakes at the choice (with
     * its {@link #WAKE_SECONDS} tell) or enters awake after it. An unchosen branch's units count nowhere.
     */
    public GroundUnit withBranch(Branch fork) {
        return new GroundUnit(t, x, enemy, group, currentRadians, side, burst, holder, dormantUntil, Optional.of(fork));
    }

    /** Without M9 part B's arena holder and dormancy. */
    public GroundUnit(
            double t,
            double x,
            EnemySpec enemy,
            int group,
            double currentRadians,
            Optional<SideEntry> side,
            Optional<RootBurst> burst) {
        this(t, x, enemy, group, currentRadians, side, burst, false, 0);
    }

    /** M9 part B: this unit as a holder of the level's arena (it holds the clock while it stands). */
    public GroundUnit asHolder() {
        return new GroundUnit(t, x, enemy, group, currentRadians, side, burst, true, dormantUntil, branch);
    }

    /** M9 part B: this unit dormant until {@code until} s on the level clock (0 for none). */
    public GroundUnit dormantUntil(double until) {
        return new GroundUnit(t, x, enemy, group, currentRadians, side, burst, holder, until, branch);
    }

    /** M9 part B: whether it enters dormant. */
    public boolean dormant() {
        return dormantUntil > 0;
    }

    /** Without M5 part H's root burst. */
    public GroundUnit(double t, double x, EnemySpec enemy, int group, double currentRadians, Optional<SideEntry> side) {
        this(t, x, enemy, group, currentRadians, side, Optional.empty());
    }

    /** Entering at the top edge (without M5 part G's side entry). */
    public GroundUnit(double t, double x, EnemySpec enemy, int group, double currentRadians) {
        this(t, x, enemy, group, currentRadians, Optional.empty());
    }

    /** A unit whose nest gives no current (straight down, with the scroll). */
    public GroundUnit(double t, double x, EnemySpec enemy, int group) {
        this(t, x, enemy, group, 0);
    }

    /**
     * M5 part G: a unit entering at a side edge ({@code from: left|right}) at {@code y} px from the
     * bottom edge (y up), drifting across at {@code drift} px/s (Level 13's mortar floe); {@code x} is
     * not read.
     */
    public static GroundUnit fromSide(double t, EnemySpec enemy, int group, SideEntry side) {
        return new GroundUnit(t, side.fromLeft() ? 0 : PlayField.WIDTH, enemy, group, 0, Optional.of(side));
    }

    /**
     * M5 part H (design/campaign Level 14, Hazards; decision H2 = a): a growth of {@code burst}, {@code
     * dx} px right of the burst's centre; it appears {@code burst.telegraphSeconds()} after the burst's
     * time {@code burst.t()}.
     */
    public static GroundUnit fromBurst(RootBurst burst, double dx, EnemySpec enemy, int group) {
        return new GroundUnit(
                burst.t() + burst.telegraphSeconds(),
                burst.x() + dx,
                enemy,
                group,
                0,
                Optional.empty(),
                Optional.of(burst));
    }
}
