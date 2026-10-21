package vanguard.sim;

import java.util.Optional;

/**
 * The parts of an {@link EnemySpec} stat block that describe the units that change their layer: burrows, submerges, cloaks and vortices. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Burrow} ...).
 */
public interface LayerSpecs {
    /**
     * M7 part B, the {@code burrow} movement (design/enemies/ground/burrower; user decision Q1 = a: it is
     * fixed on the screen, not on the ground, while it swims, bulges and is up). A {@code field} wave's
     * unit appears buried above the top edge and scrolls in with the ground until it is {@link
     * SurfaceSpot#edgeMargin()} inside the top edge and no higher than {@link SurfaceSpot#aheadMax()} above the ship;
     * then it cycles: it picks a spot ({@link SurfaceSpot}) and swims to it under the sand at {@code swimSpeed}
     * px/s for a seeded {@code travelSeconds} (on until it arrives; it waits buried there when it
     * arrives early); its wave's stagger lets it bulge ({@code staggerSeconds} after the wave's last
     * bulge, and only while fewer than {@code maxUp} of its wave would be up when it surfaces); it bulges
     * for {@code bulgeSeconds} at the spot, is up ({@link Layer#GROUND}) for {@code surfacedSeconds},
     * firing {@code ring} (and {@code secondRing}, hard) at their times after it surfaced, then dives for
     * {@code diveSeconds}, buried again from the dive's middle. After {@code surfacings} surfacings, or
     * once it has lived {@code lifeSeconds} (not while up), it swims off the bottom edge under the sand
     * at its swim speed on top of the scroll: an escape, no bounty.
     */
    public record Burrow(
            double swimSpeed,
            SurfaceSpot spot,
            Range travelSeconds,
            double bulgeSeconds,
            double surfacedSeconds,
            double diveSeconds,
            int surfacings,
            BurrowRing ring,
            Optional<BurrowRing> secondRing,
            double staggerSeconds,
            int maxUp,
            double lifeSeconds) {
        public Burrow {
            if (!(swimSpeed > 0)
                    || !(travelSeconds.min() > 0)
                    || !(bulgeSeconds > 0)
                    || !(surfacedSeconds > 0)
                    || !(diveSeconds > 0)
                    || surfacings < 1
                    || !(staggerSeconds >= 0)
                    || maxUp < 1
                    || !(lifeSeconds > 0)) {
                throw new IllegalArgumentException(
                        "a burrow has a swim speed, travel, bulge, surfaced and dive times, surfacings, a stagger,"
                                + " a cap on units up and a life");
            }
            if (!(ring.atSeconds() < surfacedSeconds)
                    || (secondRing.isPresent() && !(secondRing.get().atSeconds() < surfacedSeconds))) {
                throw new IllegalArgumentException("a burrow's rings fire while it is up");
            }
        }
    }

    /**
     * M7 part B: where a Burrower surfaces (design/enemies/ground/burrower, Choosing a spot), picked as it
     * starts to swim: {@code aheadMin}–{@code aheadMax} px above the ship's centre and within ±{@code
     * across} px of its x (seeded), kept {@code edgeMargin} px inside the play field's edges and at least
     * {@code clearance} px from the ship's centre (checked again as the bulge starts); outside the level's
     * no-surface zones ({@link GroundSpec.NoSurface}): the nearest allowed point, else that surfacing is
     * skipped (it still counts).
     */
    public record SurfaceSpot(double aheadMin, double aheadMax, double across, double edgeMargin, double clearance) {
        public SurfaceSpot {
            if (!(aheadMin >= 0)
                    || !(aheadMax >= aheadMin)
                    || !(across >= 0)
                    || !(edgeMargin >= 0)
                    || !(clearance >= 0)) {
                throw new IllegalArgumentException(
                        "a spot has a band ahead of the ship, a spread, a margin and a clearance");
            }
        }
    }

    /**
     * M7 part B: a Burrower's ring: {@code count} bullets at {@code bulletSpeed} px/s, each dealing {@code
     * damage}, {@code atSeconds} after it surfaced (none while the ship is closer than bullets may spawn).
     */
    public record BurrowRing(int count, double bulletSpeed, double damage, double atSeconds) {
        public BurrowRing {
            if (count < 1 || !(bulletSpeed > 0) || !(damage >= 0) || !(atSeconds >= 0)) {
                throw new IllegalArgumentException("a burrow ring has bullets, a speed, a damage and a time");
            }
        }
    }

    /**
     * M5 part E, surfacing and submerging (design/enemies/naval/driftjelly; the stated defaults of
     * 2026-10-08): every {@code everyMin}–{@code everyMax} s, drawn per unit from its own seed on the
     * simulation's real steps (so it keeps going in a halted arena), the unit swaps between the
     * surface ({@link Layer#GROUND}) and {@link Layer#SUB} over {@code swapSeconds}, its current
     * layer flipping at the swap's middle; a {@code field} wave starts the share {@code start} of its
     * units submerged.
     */
    public record Submerge(double everyMin, double everyMax, double swapSeconds, double start) {
        public Submerge {
            if (!(everyMin > 0) || !(everyMax >= everyMin) || !(swapSeconds > 0) || !(start >= 0 && start <= 1)) {
                throw new IllegalArgumentException(
                        "a submerge swaps every [min, max] s (0 < min <= max) over a swap time, a share starting submerged");
            }
        }
    }

    /**
     * M5 part D, a cloak (design/enemies/air/wraith): the unit flies on its stat block's layer (the
     * cloaked one, {@code high-air}: only homing hits it, no contact, the specials skip it) until it
     * decloaks at its hold point; from the start of the {@code flashSeconds} flash on its current
     * layer is {@code layer}, and its gun stays silent until the flash ends.
     */
    public record Cloak(Layer layer, double flashSeconds) {
        public Cloak {
            if (!(flashSeconds >= 0)) {
                throw new IllegalArgumentException("a decloak flash lasts 0 s or more: " + flashSeconds);
            }
        }
    }

    /**
     * M7 part E, a vortex cycle (design/enemies/ground/dust-devil): a cycle of {@code closedSeconds} closed on
     * {@code ground} (its first part sinking after an opening), {@code riseSeconds} rising (the tell), then {@code
     * openSeconds} open on {@link Layer#LOW_AIR}, its core hittable and its spiral gun firing; then it closes and
     * sinks again. The units of a wave start their cycles {@code offsetSeconds} apart (by their place in the
     * wave). It wanders a seeded swirl at {@code wanderSpeed} px/s at most {@code wanderAmplitude} px either side
     * of its centre line, which scrolls with the ground, and never comes closer than {@code keepAway} px to the
     * ship's centre (it slides round the ship).
     */
    public record Vortex(
            double closedSeconds,
            double riseSeconds,
            double openSeconds,
            double offsetSeconds,
            double wanderSpeed,
            double wanderAmplitude,
            double keepAway) {
        public Vortex {
            if (!(closedSeconds > 0) || !(riseSeconds >= 0) || !(openSeconds > 0)) {
                throw new IllegalArgumentException(
                        "a vortex is closed and open a while: " + closedSeconds + ", " + openSeconds);
            }
            if (!(offsetSeconds >= 0) || !(wanderSpeed >= 0) || !(wanderAmplitude >= 0) || !(keepAway >= 0)) {
                throw new IllegalArgumentException("a vortex's offset, wander and keep-away are 0 or more");
            }
        }

        /** The whole cycle, s. */
        public double cycleSeconds() {
            return closedSeconds + riseSeconds + openSeconds;
        }
    }
}
