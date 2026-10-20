package vanguard.sim;

import java.util.Optional;

/**
 * An enemy fixed to the ground layer (a turret), entering at the top edge at {@code t} at
 * {@code x}, in the secondary objective's group {@code group} (-1 for none). M5 part E: a unit
 * whose stat block drifts (a raft) drifts along its nest's current, {@code currentRadians} from
 * straight down, positive to the right. M5 part H: a unit grown by a {@link RootBurst} appears at
 * its burst's point instead ({@link #fromBurst}): {@code t} is then when it appears (the burst's
 * time plus its telegraph) and {@code x} its own x (the burst's x plus its place in the burst).
 */
public record GroundUnit(
        double t,
        double x,
        EnemySpec enemy,
        int group,
        double currentRadians,
        Optional<SideEntry> side,
        Optional<RootBurst> burst) {
    public GroundUnit {
        if (side.isPresent() && burst.isPresent()) {
            throw new IllegalArgumentException("a ground unit enters at a side or grows in a root burst, not both");
        }
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
