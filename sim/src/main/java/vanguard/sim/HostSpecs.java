package vanguard.sim;

/**
 * The parts of an {@link EnemySpec} stat block that describe what it carries or brings along: its drop, brood, spawner cycle, flock, aura and unit part. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Drop} ...).
 */
public interface HostSpecs {
    /** Every {@code every}-th kill of this enemy in an attempt drops {@code pickup}. */
    public record Drop(PickupType pickup, int every) {}

    /**
     * A spawner (design/enemies/air/brood-pod): {@code afterSeconds} after its centre crosses the
     * top edge it bursts on its own (the pulse speeding up over the last {@code telegraphSeconds}),
     * paying {@code burstBounty} instead of its bounty, which is not a kill; destroyed or burst, it
     * releases {@code count} units of {@code enemy} from its centre, spread evenly over
     * {@code arcRadians} centred on the direction to the ship and flying straight out at
     * {@code speed} px/s.
     */
    public record Brood(
            EnemySpec enemy,
            int count,
            double afterSeconds,
            double telegraphSeconds,
            double arcRadians,
            double speed,
            int burstBounty) {}

    /**
     * A periodic spawner (design/enemies/ground/hive-node, M5 part C): every {@code everySeconds},
     * counted from when its centre crosses the top edge, its iris opens over {@code
     * telegraphSeconds} and then releases {@code count} units of {@code enemy} from its centre,
     * spread evenly over {@code arcRadians} centred on the direction to the ship and flying straight
     * out at {@code speed} px/s (as a {@link Brood}'s). An opening due while the ship's centre is
     * within {@code shutWithin} px of its own is skipped (no telegraph), and so is a release the ship
     * came that close to during the telegraph; the next one is due a whole cycle later. It never
     * bursts on its own.
     */
    public record Spawner(
            EnemySpec enemy,
            int count,
            double everySeconds,
            double telegraphSeconds,
            double arcRadians,
            double speed,
            double shutWithin) {
        public Spawner {
            if (count < 1 || !(everySeconds > telegraphSeconds) || telegraphSeconds < 0 || shutWithin < 0) {
                throw new IllegalArgumentException(
                        "a spawner releases at least one unit, its telegraph inside its cycle");
            }
        }
    }

    /**
     * M5 part D, a flock (design/enemies/air/mote-swarm, user decision D8 = a): the members of a
     * {@code swarm} wave steer round a leader point that flies the wave's route. Each member keeps
     * {@code separation} px from the others, matches the heading of ({@code alignment}) and closes on
     * ({@code cohesion}) the members within {@code radius} px, and is pulled toward the leader point
     * ({@code leader}); it turns at most {@code turnRate} radians per second and flies about
     * {@code speed} px/s, {@code diveSpeed} after a loop-back. At most {@code max} members ({@link
     * Flock#MAX_MEMBERS} at the most).
     *
     * <p>M8 part C (design/enemies/naval/glassfin): with a {@code sway} each member swings {@code amplitude} px across
     * the leader point's heading, one full swing per {@code periodSeconds}, each member a fixed phase behind the one
     * before it ({@link Flock#RIBBON_LAG}), so the school ripples as a ribbon; the steering works on the members'
     * unswayed places. With a {@code ram} its members break off and dive at the ship ({@link Ram}); a unit of the same
     * stat block flying a path (a {@code column}) rams by it too. Empty for none.
     */
    public record FlockSpec(
            double separation,
            double radius,
            double alignment,
            double cohesion,
            double leader,
            double speed,
            double diveSpeed,
            double turnRate,
            int max,
            java.util.Optional<FlightSpecs.Sine> sway,
            java.util.Optional<Ram> ram) {
        /** Without M8 part C's sway and ram (the Mote Swarm's). */
        public FlockSpec(
                double separation,
                double radius,
                double alignment,
                double cohesion,
                double leader,
                double speed,
                double diveSpeed,
                double turnRate,
                int max) {
            this(
                    separation,
                    radius,
                    alignment,
                    cohesion,
                    leader,
                    speed,
                    diveSpeed,
                    turnRate,
                    max,
                    java.util.Optional.empty(),
                    java.util.Optional.empty());
        }

        /** M8 part C: this flock with its members' sway and ram; empty for none. */
        public FlockSpec withRibbon(java.util.Optional<FlightSpecs.Sine> withSway, java.util.Optional<Ram> withRam) {
            return new FlockSpec(
                    separation,
                    radius,
                    alignment,
                    cohesion,
                    leader,
                    speed,
                    diveSpeed,
                    turnRate,
                    max,
                    withSway,
                    withRam);
        }

        public FlockSpec {
            java.util.Objects.requireNonNull(sway);
            java.util.Objects.requireNonNull(ram);
            if (sway.isPresent() && !(sway.get().periodSeconds() > 0)) {
                throw new IllegalArgumentException("a sway takes time: " + sway.get());
            }
            if (!(separation > 0)
                    || !(radius >= separation)
                    || !(alignment >= 0)
                    || !(cohesion >= 0)
                    || !(leader > 0)
                    || !(speed > 0)
                    || !(diveSpeed > 0)
                    || !(turnRate > 0)
                    || max < 1
                    || max > Flock.MAX_MEMBERS) {
                throw new IllegalArgumentException("a flock has a separation inside its radius, a pull toward"
                        + " its leader, speeds, a turn rate and 1 to " + Flock.MAX_MEMBERS + " members");
            }
        }
    }

    /**
     * M8 part C, a member's ram (design/enemies/naval/glassfin): {@code afterSeconds} after it first came onto the play
     * field each member draws once, by its own seed, whether it breaks off ({@code share} of them do: the difficulty's
     * dive share). One that does glints for {@code tellSeconds} (the tell, {@link SimEvents.Type#EYE_GLINT}, value its
     * kind), {@code waitSeconds} after the tell's end dives straight at where the ship is then at {@code speed} px/s
     * (straight down when the ship is not below it), without homing, and is gone once it leaves the play field (an
     * escape); none loops back.
     */
    public record Ram(double afterSeconds, double share, double tellSeconds, double waitSeconds, double speed) {
        public Ram {
            if (!(afterSeconds >= 0)
                    || !(share >= 0 && share <= 1)
                    || !(tellSeconds >= 0)
                    || !(waitSeconds >= 0)
                    || !(speed > 0)) {
                throw new IllegalArgumentException("a ram has a delay, a share of 0 to 1, a tell, a wait and a speed: "
                        + afterSeconds + ", " + share + ", " + tellSeconds + ", " + waitSeconds + ", " + speed);
            }
        }
    }

    /**
     * M7 part D, a Choir Herald's {@code aura} (design/enemies/air/choir-herald; M8 = a): while it lives, is on
     * the play field and the EMP Burst does not stun it, every other unit whose centre is within {@code radius}
     * px of its centre carries a shield ring of at most its HP at the level, capped at {@code cap}, which
     * absorbs damage before its HP. A unit entering the radius gets its full ring over {@code fillSeconds};
     * one that left it keeps its ring {@code keepSeconds}, then the ring fades (over {@code fillSeconds}); a
     * ring untouched for {@code refillDelaySeconds} refills at {@code refillShare} of its maximum a second
     * while the unit stays in range. At the Herald's death every ring it fed collapses over {@code
     * collapseSeconds}. Its escorts circle it at {@code escortRadiusShare} × the radius (the gap check's
     * default 0.8, pending round 40) so that they stay ringed. Never on another Herald, a chain's member,
     * a unit buried or under the water, nor a boss or set piece (those are no units).
     */
    public record Aura(
            double radius,
            double cap,
            double refillShare,
            double refillDelaySeconds,
            double fillSeconds,
            double keepSeconds,
            double collapseSeconds,
            double escortRadiusShare) {
        public Aura {
            if (!(radius > 0) || !(cap > 0) || !(refillShare >= 0) || !(refillDelaySeconds >= 0)) {
                throw new IllegalArgumentException("an aura has a radius, a cap and a refill: " + radius + ", " + cap);
            }
            if (!(fillSeconds > 0) || !(keepSeconds >= 0) || !(collapseSeconds > 0) || !(escortRadiusShare > 0)) {
                throw new IllegalArgumentException("an aura's fill and collapse take time: " + fillSeconds);
            }
        }
    }

    /**
     * M7 part D, a destroyable part on a unit that is not a boss (design/enemies/ground/shellback, the mortar
     * vent; part F's Warden Tank turret may reuse it): {@code hp} of its own, its {@code hitbox} centred
     * {@code dx} px to the unit's right and {@code dy} px towards its tail in its own frame (facing down the
     * screen), turning with a walker's facing in its 16 headings (M8 part B: a unit that does not walk keeps its
     * parts where they are, the Drill Rig's fixed collar). Shots test it before the body; a blast
     * covering both hurts both. Destroyed, it pays {@code bounty} (as a chain's part does). With {@code
     * silencesGun} the unit's gun is bound to it: M8 part B (design/enemies/ground/drill-rig, the pumps), the gun
     * fires while any of the unit's parts that carry it stands and stops for good once all of them are gone, its
     * interval {@code slowsGunSeconds} longer for each of them lost (the Shellback's one vent: silenced with it).
     * Killing the body pays the unit's bounty plus the {@code firstBonus} of every part still standing (the
     * whole: the Shellback's 40 + 8; the Drill Rig's 40 + 4 a pump).
     */
    public record UnitPart(
            String name,
            double hp,
            Hitbox hitbox,
            double dx,
            double dy,
            int bounty,
            boolean silencesGun,
            int firstBonus,
            double slowsGunSeconds) {
        public UnitPart {
            if (!(hp > 0)) {
                throw new IllegalArgumentException(name + ": a unit part has HP: " + hp);
            }
            if (!(slowsGunSeconds >= 0) || (slowsGunSeconds > 0 && !silencesGun)) {
                throw new IllegalArgumentException(
                        name + ": only a part that carries the gun slows it, by 0 s or more: " + slowsGunSeconds);
            }
        }

        /** A part whose loss does not slow the gun (M7 part D). */
        public UnitPart(
                String name,
                double hp,
                Hitbox hitbox,
                double dx,
                double dy,
                int bounty,
                boolean silencesGun,
                int firstBonus) {
            this(name, hp, hitbox, dx, dy, bounty, silencesGun, firstBonus, 0);
        }

        /** A part without a whole-kill bonus. */
        public UnitPart(String name, double hp, Hitbox hitbox, double dx, double dy, int bounty, boolean silencesGun) {
            this(name, hp, hitbox, dx, dy, bounty, silencesGun, 0);
        }
    }
}
