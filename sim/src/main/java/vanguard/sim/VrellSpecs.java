package vanguard.sim;

/**
 * M11 part A (Act 7, design/enemies: Window shields; the movement vocabulary's {@code teleport} and {@code mirror}): the
 * parts of an {@link EnemySpec} stat block that Act 7 adds. {@link EnemySpec} implements this interface only to inherit
 * them as its member types, so they keep their names ({@code EnemySpec.WindowShield} ...). The numbers come from the
 * content (the difficulty's choices made there).
 */
public interface VrellSpecs {
    /**
     * A window shield (design/enemies, Window shields; design/enemies/space/gate-warden): the unit is <b>closed</b>
     * (invulnerable: every hit sparks off, a beam stops on it, a blast does nothing, a Plasma Arc strike is absorbed and
     * ends its chain) except from its {@code laser-line}'s telegraph's first step to its beam's last. A {@code
     * shield-breaker} hit or the EMP Burst opens it for {@code holdSeconds} (another breaker hit restarts it; its own
     * window inside the hold keeps it open to the window's end); a broken shell regrows over {@code regrowSeconds}
     * (the shared regrowth shimmer) before it is closed again. The iris draws back and closes over {@code irisSeconds}
     * (the look); a closed shell's tick sounds at most every {@code tickGapSeconds}. No HP of its own.
     */
    record WindowShield(double holdSeconds, double regrowSeconds, double irisSeconds, double tickGapSeconds) {
        public WindowShield {
            if (!(holdSeconds > 0) || !(regrowSeconds >= 0) || !(irisSeconds >= 0) || !(tickGapSeconds >= 0)) {
                throw new IllegalArgumentException("a window shield has a hold, a regrowth, an iris and a tick gap");
            }
        }
    }

    /**
     * The {@code teleport} movement (the movement vocabulary's fairness rules; design/enemies/space/rift-skater): the
     * unit glides {@code firstGlideSeconds} on the play field along its path, then jumps; after each landing it glides
     * {@code glideSeconds} straight on along its jump and jumps again; after {@code jumps} jumps it glides out through
     * the side edge downwind. A jump: it fades out over {@code fadeSeconds} (hittable), then is <b>gone</b> for {@code
     * ringSeconds} while its arrival ring forms (not hittable, no contact, not on the screen for the EMP; the seekers
     * pick another target), then lands. The arrival lies {@code jump} px away, {@code angleDegrees} off its frame's
     * down toward the drift's side (alternating sides with no drift); never within {@code minDistance} px of the ship,
     * never in the {@link Teleporting#BAND} px band at the ship's edge of the field, never within {@link
     * Teleporting#SIDE_MARGIN} px of a side edge (checked as the ring starts and again at landing: a ring the ship
     * comes near slides away from it). Its gun fires {@code fireAfterSeconds} after each landing (one burst), never
     * else. At most {@code mostRings} rings on the screen at once: a jump waits for a free one.
     */
    record Teleport(
            double fadeSeconds,
            double ringSeconds,
            double minDistance,
            Range jump,
            Range angleDegrees,
            double firstGlideSeconds,
            double glideSeconds,
            int jumps,
            double fireAfterSeconds,
            int mostRings) {
        public Teleport {
            java.util.Objects.requireNonNull(jump);
            java.util.Objects.requireNonNull(angleDegrees);
            if (!(fadeSeconds >= 0)
                    || !(ringSeconds >= 0.5)
                    || !(minDistance >= 0)
                    || !(jump.min() > 0)
                    || !(angleDegrees.min() >= 0 && angleDegrees.max() <= 90)
                    || !(firstGlideSeconds >= 0)
                    || !(glideSeconds >= 0)
                    || jumps < 1
                    || !(fireAfterSeconds >= 0.25)
                    || mostRings < 1) {
                throw new IllegalArgumentException("a teleport fades, rings at least 0.5 s, jumps a distance at an "
                        + "angle, fires at least 0.25 s after landing and has a ring cap");
            }
        }
    }

    /**
     * The {@code mirror} movement (the movement vocabulary's fairness rules; design/enemies/air/choir-seraph): while it
     * mirrors, the unit's x follows the field's width minus the ship's x as it was {@code lagSeconds} ago, at most
     * {@code speed} px/s, never jumping; its row stays at least {@code minDy} px from the ship's y (pushed up the
     * screen away from it at the same cap); two mirroring units keep their rows a hull's height apart. It mirrors while
     * it holds its station (or, with a {@code teleport}, while it glides). Its gun, if any, is a spiral (unaimed), fired
     * only while it mirrors.
     */
    record Shadow(double lagSeconds, double speed, double minDy) {
        public Shadow {
            if (!(lagSeconds >= 0) || lagSeconds > Shadowing.MAX_LAG_SECONDS || !(speed > 0) || !(minDy >= 0)) {
                throw new IllegalArgumentException("a mirror follows with a lag of at most " + Shadowing.MAX_LAG_SECONDS
                        + " s, a speed cap and a row distance: " + lagSeconds);
            }
        }
    }
}
