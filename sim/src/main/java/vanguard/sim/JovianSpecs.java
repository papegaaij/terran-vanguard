package vanguard.sim;

/**
 * M10 part A (Act 6, design/enemies: Reflect, Regeneration; the Graft Turret's regrowth; the Shield Pylon's dome): the
 * parts of an {@link EnemySpec} stat block that Act 6 adds. {@link EnemySpec} implements this interface only to inherit
 * them as its member types, so they keep their names ({@code EnemySpec.Mirror} ...). The numbers come scaled by the
 * content (the difficulty's lever and the act factor on the heat, as on a shield).
 */
public interface JovianSpecs {
    /**
     * The {@code reflect} mirror (design/enemies, Reflect; design/enemies/air/mirror-interceptor): a plate over a
     * frontal arc of {@code arcDegrees} (±half of its facing). The facing turns at {@code turnDegreesPerSecond} toward
     * the ship while the unit holds its station, along its flight before and after. A non-beam player shot meeting it
     * bounces back as an enemy bullet of {@code bulletDamage} at {@code bulletSpeed} px/s, at most one every {@code
     * gapSeconds} (the rest absorbed); a beam heats it by its damage up to {@code heat}, which breaks it (cooling at
     * {@code coolShare} of the heat a second after {@code coolDelaySeconds} without the beam); a {@code
     * shield-breaker} hit or the EMP Burst shatters it. Broken or shattered it is down {@code downSeconds}, then regrows
     * over {@code regrowSeconds}. Its gun fires only while its facing is within {@code fireWithinDegrees} of the ship
     * (it holds its facing through a burst).
     */
    record Mirror(
            double arcDegrees,
            double heat,
            double turnDegreesPerSecond,
            double downSeconds,
            double regrowSeconds,
            double gapSeconds,
            double coolShare,
            double coolDelaySeconds,
            double bulletSpeed,
            double bulletDamage,
            double fireWithinDegrees) {
        public Mirror {
            if (!(arcDegrees > 0 && arcDegrees <= 360)
                    || !(heat > 0)
                    || !(turnDegreesPerSecond > 0)
                    || !(downSeconds >= 0)
                    || !(regrowSeconds >= 0)
                    || !(gapSeconds >= 0)
                    || !(coolShare >= 0)
                    || !(coolDelaySeconds >= 0)
                    || !(bulletSpeed > 0)
                    || !(bulletDamage > 0)
                    || !(fireWithinDegrees > 0)) {
                throw new IllegalArgumentException("a mirror has an arc, a heat, a turn rate and a bounced bullet");
            }
        }

        /** Half its arc, radians. */
        public double halfArcRadians() {
            return StrictMath.toRadians(arcDegrees / 2);
        }
    }

    /**
     * Regeneration (design/enemies, Regeneration; design/enemies/air/chimera): {@code rate} of its maximum HP a
     * second once it has gone {@code delaySeconds} without any damage, up to the maximum.
     */
    record Regeneration(double rate, double delaySeconds) {
        public Regeneration {
            if (!(rate > 0) || !(delaySeconds >= 0)) {
                throw new IllegalArgumentException(
                        "a regeneration has a rate and a delay: " + rate + ", " + delaySeconds);
            }
        }
    }

    /**
     * Regrowth (design/enemies/ground/graft-turret, Regrowth): its turret's death exposes its root ({@code rootHp} HP
     * in {@code rootHitbox} on its centre, a {@code rootBounty} bonus when it dies) for {@code windowSeconds}; the
     * window closed, the turret knits back over {@code knitSeconds} (not hittable) and fires again {@code wakeSeconds}
     * after that, with its full HP. Only the first death pays and counts.
     */
    record Regrowth(
            double windowSeconds,
            double knitSeconds,
            double wakeSeconds,
            double rootHp,
            Hitbox rootHitbox,
            int rootBounty) {
        public Regrowth {
            java.util.Objects.requireNonNull(rootHitbox);
            if (!(windowSeconds > 0) || !(knitSeconds >= 0) || !(wakeSeconds >= 0) || !(rootHp > 0) || rootBounty < 0) {
                throw new IllegalArgumentException("a regrowth has a window, a knit, a wake and a root");
            }
        }
    }

    /**
     * The Shield Pylon's dome (design/enemies/ground/shield-pylon, The dome): a circle of {@code radius} px centred
     * {@code offset} px from the pylon along its crown's {@code bearingRadians} (clockwise from straight down the
     * screen, as a heading; the level's placement sets it per pylon). Every other ground unit whose centre lies in it
     * takes no damage. A {@code shield-breaker} hit on the pylon drops it {@code breakerDropSeconds}, the EMP Burst for
     * the pylon's stun; a dropped dome comes back over {@code backSeconds}; the pylon's death drops it for good.
     */
    record Dome(double radius, double offset, double bearingRadians, double breakerDropSeconds, double backSeconds) {
        public Dome {
            if (!(radius > 0)
                    || !(offset >= 0)
                    || !Double.isFinite(bearingRadians)
                    || !(breakerDropSeconds >= 0)
                    || !(backSeconds >= 0)) {
                throw new IllegalArgumentException("a dome has a radius, an offset, a bearing and its drop times");
            }
        }

        /** The dome's centre across from a pylon at {@code x}. */
        public double centreX(double x) {
            return x - offset * Trig.sin(bearingRadians);
        }

        /** The dome's centre up from a pylon at {@code y}. */
        public double centreY(double y) {
            return y - offset * Trig.cos(bearingRadians);
        }
    }
}
