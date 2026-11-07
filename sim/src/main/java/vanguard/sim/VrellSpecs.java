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
     *
     * <p>M11 part E, step E1 (design/enemies/air/choir-seraph, the cycle): an optional {@link Cycle} ({@link
     * #withCycle}) changes how it enters, arrives and leaves; empty for every teleport before part E.
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
            int mostRings,
            java.util.Optional<Cycle> cycle) {
        /** A teleport without a cycle (every teleport before M11 part E). */
        public Teleport(
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
            this(
                    fadeSeconds,
                    ringSeconds,
                    minDistance,
                    jump,
                    angleDegrees,
                    firstGlideSeconds,
                    glideSeconds,
                    jumps,
                    fireAfterSeconds,
                    mostRings,
                    java.util.Optional.empty());
        }

        /** M11 part E, step E1: this teleport flying {@code withCycle}. */
        public Teleport withCycle(Cycle withCycle) {
            return new Teleport(
                    fadeSeconds,
                    ringSeconds,
                    minDistance,
                    jump,
                    angleDegrees,
                    firstGlideSeconds,
                    glideSeconds,
                    jumps,
                    fireAfterSeconds,
                    mostRings,
                    java.util.Optional.of(withCycle));
        }

        /**
         * M11 part E, step E1: the landings it makes before it leaves: its jumps, plus its entry's landing for a unit
         * that enters by its ring (a cycle's count: the Choir Seraph's 4 cycles are an entry and 3 jumps).
         */
        public int landings() {
            return jumps + (cycle.isPresent() && cycle.get().enterByRing() ? 1 : 0);
        }

        public Teleport {
            java.util.Objects.requireNonNull(jump);
            java.util.Objects.requireNonNull(angleDegrees);
            java.util.Objects.requireNonNull(cycle);
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

        /**
         * M11 part E, step E1 (design/enemies/air/choir-seraph, Movement: the cycle; m11e §4): how a cycling teleporter
         * enters, arrives and leaves. With {@code enterByRing} it never flies a path: it starts gone, its first ring
         * forming {@code firstRingSeconds} where its arrival rule puts it (the ring cap holds it gone until a ring is
         * free). Each jump starts with a <b>fold</b> of {@code foldSeconds} (it holds still, mirrors and fires nothing,
         * hittable: the jump's tell; {@link SimEvents.Type#JUMP_FOLD}), then the teleport's fade; the fold reserves its
         * ring (a starved jump waits on, mirroring). {@code arrive}: where each ring forms ({@link Arrival}); {@code exit}:
         * how it leaves after its last landing's glide ({@link Exit}). A leaving jump's fold is {@code leaveFoldSeconds}
         * long and rises {@code leaveRise} px up the screen over it (the leaving tell, {@link
         * SimEvents.Type#LEAVING_TELL}, still hittable), then it fades and is gone for good: it escaped. The teleport's
         * {@code glideSeconds} is the mirror's time at each arrival.
         */
        public record Cycle(
                boolean enterByRing,
                double firstRingSeconds,
                double foldSeconds,
                Arrival arrive,
                Exit exit,
                double leaveFoldSeconds,
                double leaveRise) {
            public Cycle {
                java.util.Objects.requireNonNull(arrive);
                java.util.Objects.requireNonNull(exit);
                if (!(firstRingSeconds >= 0.5)
                        || !(foldSeconds >= 0)
                        || !(leaveFoldSeconds >= 0)
                        || !(leaveRise >= 0)) {
                    throw new IllegalArgumentException(
                            "a cycle's first ring forms at least 0.5 s; its folds and rise are not negative");
                }
            }
        }

        /** M11 part E, step E1: where a cycling teleporter's rings form. */
        public enum Arrival {
            /** Its jump's distance and angle from where it faded (the built teleport's). */
            JUMP,
            /**
             * At the ship's mirrored point (the field's width minus the ship's x) on its row (design/enemies/air/
             * choir-seraph; m11e §4: the point as the ring starts, so it lands where its mirror would put it); its row
             * drawn per landing from its wave's seed (a pair's upper and lower rows) or its wave's fixed {@link
             * WaveSpec.Rows}, kept at least its mirror's row distance above the ship.
             */
            MIRROR_ROW
        }

        /** M11 part E, step E1: how a cycling teleporter leaves after its last landing's glide. */
        public enum Exit {
            /** Gliding out through the side edge downwind (the built teleport's). */
            GLIDE_OUT,
            /** A last jump out for good after its leaving tell (an escape at its fade's end). */
            JUMP
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

    /**
     * M11 part D, step CD1 (design/enemies/space/husk, the crack): the unit never fires whole; the first time its HP
     * falls to {@code at} of its whole (or the level's crack cue reaches it, {@link VrellLevel.CrackCue}) it tells for
     * {@code tellSeconds} (the hull flares; the EMP's stun holds the tell's clock), then fires {@code fan} once from its
     * centre at the ship (the built aimed fan); a unit killed in its tell fires nothing. Once per unit.
     */
    record Crack(double at, double tellSeconds, EnemyGun fan) {
        public Crack {
            java.util.Objects.requireNonNull(fan);
            if (!(at > 0 && at < 1) || !(tellSeconds >= 0)) {
                throw new IllegalArgumentException("a crack comes at a share of the HP after a tell: " + at);
            }
        }
    }
}
