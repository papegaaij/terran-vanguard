package vanguard.content;

import java.util.Optional;

/**
 * M11 part A (Act 7; design/enemies, Window shields; the movement vocabulary's {@code teleport} and {@code mirror}): the
 * stat block's Act 7 keys. It only holds the block's types: {@link EnemyData} implements it, so the rest of the game
 * names them as members of {@code EnemyData} ({@code EnemyData.WindowShield}). The simulation's numbers are A1a's
 * ({@code EnemySpec.WindowShield}, {@code Teleport}, {@code Shadow}); {@link VrellRules} maps them, {@link VrellChecks}
 * checks them.
 */
public sealed interface VrellUnitData permits EnemyData {
    /**
     * A window shield (design/enemies, Window shields; design/enemies/space/gate-warden): closed (invulnerable) except
     * from its {@code laser-line}'s telegraph's first frame to its beam's last; a {@code shield-breaker} hit or the EMP
     * opens it {@code hold} s on the level's clock; a broken shell regrows over {@code regrow} s; the iris draws back and
     * closes over {@code iris} s; a closed shell's tick sounds at most every {@code tick_gap} s. No HP of its own.
     */
    record WindowShield(double hold, double regrow, double iris, double tickGap) {
        public WindowShield {
            Check.positive("window_shield.hold", hold);
            Check.notNegative("window_shield.regrow", regrow);
            Check.notNegative("window_shield.iris", iris);
            Check.notNegative("window_shield.tick_gap", tickGap);
        }
    }

    /**
     * The {@code teleport} movement ({@code movement.teleport}; the vocabulary's fairness rules,
     * design/enemies/space/rift-skater): it glides {@code first_glide} s along its path, then jumps; after each landing
     * it glides {@code glide} s and jumps again, {@code jumps} times, then glides out downwind. A jump fades out over
     * {@code fade} s (hittable), is gone {@code ring} s while the arrival ring forms (at least 0.5), and lands {@code
     * jump} px away ({@code [min, max]}), {@code angle} degrees off its frame's down ({@code [min, max]}, 0–90), never
     * within {@code min_distance} px of the ship; its gun fires {@code fire_after} s after a landing (at least 0.25; the
     * floor when not given, a mirroring unit's spiral fires while it mirrors instead); at most {@code most_rings} rings
     * on the screen at once (the shared rule's 4).
     *
     * <p>M11 part E, step E2 (design/enemies/air/choir-seraph, the cycle): an optional {@code cycle} ({@link Cycle}).
     * The {@code jump} and {@code angle} are needed unless its cycle arrives on its mirror row, the {@code first_glide}
     * unless it enters by its ring.
     */
    record Teleport(
            double fade,
            double ring,
            double minDistance,
            Optional<Span> jump,
            Optional<Span> angle,
            Optional<Double> firstGlide,
            double glide,
            int jumps,
            Optional<Double> fireAfter,
            int mostRings,
            Optional<Cycle> cycle) {
        /** The vocabulary's shortest ring, s. */
        public static final double MIN_RING = 0.5;

        /** The vocabulary's earliest fire after a landing, s. */
        public static final double MIN_FIRE_AFTER = 0.25;

        /** The vocabulary's least distance of an arrival from the ship, px. */
        public static final double MIN_DISTANCE = 120;

        /** The vocabulary's most rings on the screen at once. */
        public static final int MOST_RINGS = 4;

        public Teleport {
            Check.notNegative("teleport.fade", fade);
            Check.that(ring >= MIN_RING, "teleport.ring: at least " + MIN_RING + " s, was " + ring);
            Check.that(
                    minDistance >= MIN_DISTANCE,
                    "teleport.min_distance: at least " + MIN_DISTANCE + " px, was " + minDistance);
            boolean mirrorRow =
                    cycle.filter(c -> c.arrive() == Cycle.Arrive.MIRROR).isPresent();
            boolean byRing = cycle.filter(c -> c.enter() == Cycle.Enter.RING).isPresent();
            Check.that(
                    mirrorRow || (jump.isPresent() && angle.isPresent()),
                    "teleport.jump and teleport.angle: a jump's distance and angle (unless its cycle arrives: mirror)");
            Check.that(byRing || firstGlide.isPresent(), "teleport.first_glide (unless its cycle enters: ring)");
            jump.ifPresent(span -> Check.positive("teleport.jump", span.min()));
            angle.ifPresent(span -> Check.that(
                    span.min() >= 0 && span.max() <= 90, "teleport.angle: degrees off its frame's down, 0 to 90"));
            firstGlide.ifPresent(seconds -> Check.notNegative("teleport.first_glide", seconds));
            Check.notNegative("teleport.glide", glide);
            Check.positive("teleport.jumps", jumps);
            fireAfter.ifPresent(seconds -> Check.that(
                    seconds >= MIN_FIRE_AFTER,
                    "teleport.fire_after: at least " + MIN_FIRE_AFTER + " s after a landing, was " + seconds));
            Check.that(
                    mostRings >= 1 && mostRings <= MOST_RINGS,
                    "teleport.most_rings: 1 to " + MOST_RINGS + ", was " + mostRings);
        }

        /**
         * M11 part E, step E2 (design/enemies/air/choir-seraph, Movement: the cycle; the simulation's {@code
         * Teleport.Cycle}): {@code enter: ring}, it never flies a path: its first ring forms {@code first_ring} s where
         * its arrival rule puts it ({@code path}: it flies its wave's path first, as a Rift Skater); each jump starts
         * with a {@code fold} s (still, silent, hittable: the jump's tell); {@code arrive: mirror}, its rings form at the
         * ship's mirrored point on its row ({@code jump}: its jump's distance and angle); {@code exit: jump}, after its
         * last landing it jumps out for good after its leaving tell, a {@code leave_fold} s fold rising {@code
         * leave_rise} px (an escape; {@code glide}: it glides out downwind). Its cycles are its {@code jumps} + 1 with
         * {@code enter: ring}; its {@code glide} is the mirror's time at each arrival.
         */
        record Cycle(
                Enter enter,
                double firstRing,
                double fold,
                Arrive arrive,
                Exit exit,
                double leaveFold,
                double leaveRise) {
            public Cycle {
                Check.that(
                        firstRing >= MIN_RING,
                        "teleport.cycle.first_ring: at least " + MIN_RING + " s, was " + firstRing);
                Check.notNegative("teleport.cycle.fold", fold);
                Check.notNegative("teleport.cycle.leave_fold", leaveFold);
                Check.notNegative("teleport.cycle.leave_rise", leaveRise);
            }

            /** How it comes on: by its ring, or along its wave's path. */
            public enum Enter {
                RING,
                PATH
            }

            /** Where its rings form: on its mirror row, or a jump away. */
            public enum Arrive {
                MIRROR,
                JUMP
            }

            /** How it leaves: a last jump (an escape), or gliding out. */
            public enum Exit {
                JUMP,
                GLIDE
            }
        }
    }

    /**
     * The {@code mirror} movement ({@code movement.mirror}; the vocabulary's fairness rules,
     * design/enemies/air/choir-seraph): its x follows the field's width minus the ship's x with a {@code lag} (s), at
     * most {@code speed} px/s, its row at least {@code min_dy} px from the ship's y (the vocabulary's 140). It mirrors
     * while it holds its hover's station (or, with a teleport, while it glides) and fires only a {@code spiral}.
     */
    record MirrorMove(double lag, double speed, double minDy) {
        /** The vocabulary's least distance of its row from the ship's y, px. */
        public static final double MIN_DY = 140;

        public MirrorMove {
            Check.notNegative("mirror.lag", lag);
            Check.positive("mirror.speed", speed);
            Check.that(minDy >= MIN_DY, "mirror.min_dy: at least " + MIN_DY + " px, was " + minDy);
        }
    }

    /**
     * A difficulty's teleport changes (design/enemies/space/rift-skater: easy ring 0.7 s and 2 jumps, hard 4 jumps): its
     * {@code ring}, {@code jumps} and {@code fire_after} instead; M11 part E, step E2 (design/enemies/air/choir-seraph:
     * easy mirror 2.0 s, hard 3.0 s): its {@code glide} too.
     */
    record TeleportChange(
            Optional<Double> ring, Optional<Integer> jumps, Optional<Double> fireAfter, Optional<Double> glide) {
        public TeleportChange {
            glide.ifPresent(s -> Check.notNegative("teleport.glide", s));
            ring.ifPresent(r -> Check.that(r >= Teleport.MIN_RING, "teleport.ring: at least " + Teleport.MIN_RING));
            jumps.ifPresent(n -> Check.positive("teleport.jumps", n));
            fireAfter.ifPresent(s -> Check.that(
                    s >= Teleport.MIN_FIRE_AFTER, "teleport.fire_after: at least " + Teleport.MIN_FIRE_AFTER + " s"));
        }
    }

    /**
     * M11 part D, step D2 (design/enemies/space/husk, {@code movement.line_drift}; design/enemies/space/seed-casket): it
     * drifts straight along its wave's {@code lines} at a speed drawn from {@code speed} px/s ({@code [min, max]}) and
     * spins at a rate drawn from {@code spin} degrees a second (presentation only; {@code [0, 0]} for none), entering
     * unwarned from the line's first point and leaving through the far edge past its second (it left: never a kill).
     * The simulation's {@code EnemySpec.Drift}; a wave of such a unit names its lines (step CD1's {@code
     * WaveSpec.lines}).
     */
    record LineDrift(Span speed, Span spin) {
        public LineDrift {
            Check.positive("line_drift.speed", speed.min());
            Check.notNegative("line_drift.spin", spin.min());
        }
    }

    /**
     * M11 part D, step D2 (design/enemies/space/husk, the crack): the unit never fires whole; the first time its HP falls
     * to {@code at} of its whole (or the level's {@code crack_cue} reaches it) it tells {@code tell} s (the difficulty's
     * {@code telegraph} hook instead: hard's 0.4), then fires its {@code attack} (a {@code fan} of the unit's attacks,
     * its bullets {@code spread} ÷ (count − 1) apart however the {@code fan_count} hook sizes it) once from its centre at
     * the ship. The simulation's {@code EnemySpec.Crack}.
     */
    record Crack(double at, double tell, String attack) {
        public Crack {
            Check.that(at > 0 && at < 1, "crack.at: a share of its HP between 0 and 1, was " + at);
            Check.notNegative("crack.tell", tell);
            Check.that(attack != null && !attack.isBlank(), "crack.attack: names its fan");
        }
    }
}
