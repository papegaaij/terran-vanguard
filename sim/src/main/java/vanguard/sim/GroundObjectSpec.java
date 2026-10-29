package vanguard.sim;

import java.util.Optional;

/**
 * An object on the ground layer, entering at the top edge at {@code t} and scrolling with the
 * ground: a destructible ({@code hp}, {@code bounty}, {@code drop}) or a trigger that releases a
 * secret's hidden crate after {@code hits} hits.
 *
 * @param x its position in the play field
 * @param size its hit box
 * @param crateCredits the hidden crate's credits (triggers only)
 * @param secret the secret's name (triggers only), for its radio cue
 * @param hardened only {@code anti-ground} weapons damage it; other shots glance off
 * @param secretIndex a trigger's secret, its index among the level's secrets
 * @param secretTriggers how many triggers reveal that secret together (Level 03's four lifeboat
 *     lights): the crate drops when the last of them is spent; 1 for a trigger of its own
 * @param bonusDrop a second pickup a destructible drops with its {@code drop} (Level 04's supply
 *     drop: a special charge, which drops only with a special fitted)
 * @param look a destructible's sprite set, which the game draws it with (the simulation does not
 *     read it)
 * @param submerged M5 part E: a trigger on the {@code sub} layer (Level 11's sunken supply pod):
 *     only torpedoes ({@link WeaponSpec.Delivery#TORPEDO}) hit it, and a Smart Bomb's ring spends
 *     it at once; every other shot, blast and strike passes over it
 * @param group M5 part E: a destructible's group (Level 11's floating containers), its index among
 *     the level's destructible groups, -1 for none
 * @param groupSize how many destructibles the group has
 * @param groupDrop the pickup the group's last destructible drops where it is destroyed, once
 *     every destructible of the group is (the containers' armour patch)
 */
public record GroundObjectSpec(
        double t,
        double x,
        Hitbox size,
        double hp,
        int bounty,
        Optional<PickupType> drop,
        int hits,
        int crateCredits,
        String secret,
        boolean hardened,
        int secretIndex,
        int secretTriggers,
        Optional<PickupType> bonusDrop,
        String look,
        boolean dark,
        Optional<LevelResult.DataCore> core,
        boolean submerged,
        int group,
        int groupSize,
        Optional<PickupType> groupDrop,
        boolean lowAir,
        double driftX,
        double driftY,
        Optional<Blast> blast,
        Optional<Branch> branch) {
    /** Level 01's cargo container, the look of a destructible that names none. */
    public static final String CARGO_CONTAINER = "cargo-container";

    /**
     * M9 part E, a mining charge's burst (design/world/belt, Hazards &amp; set pieces; design/campaign Level 32): the
     * destructible bursts when it is destroyed, by anything, dealing {@code damage} to every enemy (air and ground,
     * hardened ones and unit parts included: a blast) whose hit box lies within {@code radius} px of its centre; the
     * kills are the player's. It never hurts the ship, Rook or a ground object (a charge sets off no other charge).
     */
    public record Blast(double radius, double damage) {
        public Blast {
            if (!(radius > 0) || !(damage > 0)) {
                throw new IllegalArgumentException(
                        "a charge bursts over a radius with a damage: " + radius + ", " + damage);
            }
        }
    }

    /** Without M9 part E's charge burst and fork branch. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop,
            String look,
            boolean dark,
            Optional<LevelResult.DataCore> core,
            boolean submerged,
            int group,
            int groupSize,
            Optional<PickupType> groupDrop,
            boolean lowAir,
            double driftX,
            double driftY) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                group,
                groupSize,
                groupDrop,
                lowAir,
                driftX,
                driftY,
                Optional.empty(),
                Optional.empty());
    }

    /**
     * M9 part E (design/campaign Level 32, Mining charges): this destructible as a mining charge bursting with {@code
     * burst} when it is destroyed.
     */
    public GroundObjectSpec withBlast(Blast burst) {
        return new GroundObjectSpec(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                group,
                groupSize,
                groupDrop,
                lowAir,
                driftX,
                driftY,
                Optional.of(burst),
                branch);
    }

    /**
     * M9 part E (design/campaign Level 32, The forks): this object as one of {@code fork}'s branch's: dormant (dark, not
     * hittable) unless that side is chosen, woken at the choice or entering awake after it (a branch's blast door
     * lock, its charges).
     */
    public GroundObjectSpec withBranch(Branch fork) {
        return new GroundObjectSpec(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                group,
                groupSize,
                groupDrop,
                lowAir,
                driftX,
                driftY,
                blast,
                Optional.of(fork));
    }

    /**
     * M9 part E (design/campaign Level 32, Blast doors): whether it is a destructible that frees a crate of {@link
     * #crateCredits()} without a secret (a branch's cache behind its blast door lock).
     */
    public boolean cache() {
        return !trigger() && secretIndex < 0 && crateCredits > 0;
    }

    /** Without M5 part F's drifting trigger on {@code low-air}. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop,
            String look,
            boolean dark,
            Optional<LevelResult.DataCore> core,
            boolean submerged,
            int group,
            int groupSize,
            Optional<PickupType> groupDrop) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                group,
                groupSize,
                groupDrop,
                false,
                0,
                0);
    }

    /**
     * M5 part F (design/campaign Level 12's canister, user decision F2 = a): this trigger on
     * {@code low-air}, drifting at ({@code vx}, {@code vy}) px/s in screen space (y up) from where it
     * enters at the top edge instead of scrolling with the ground; never pushed by the wind, always
     * hittable by what reaches {@code low-air} (not by bombs or shells bursting on the ground, nor by
     * torpedoes).
     */
    public GroundObjectSpec drifting(double vx, double vy) {
        if (!trigger() || submerged || !(vy < 0)) {
            throw new IllegalArgumentException("only a trigger above the water drifts, down the screen");
        }
        return new GroundObjectSpec(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                group,
                groupSize,
                groupDrop,
                true,
                vx,
                vy);
    }

    /** Without M5 part E's destructible group. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop,
            String look,
            boolean dark,
            Optional<LevelResult.DataCore> core,
            boolean submerged) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                submerged,
                -1,
                0,
                Optional.empty());
    }

    /** On the ground layer: without M5 part E's sunken trigger. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop,
            String look,
            boolean dark,
            Optional<LevelResult.DataCore> core) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                dark,
                core,
                false);
    }

    /** Without Level 06's darkness and data core. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop,
            String look) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                look,
                false,
                Optional.empty());
    }

    public GroundObjectSpec {
        java.util.Objects.requireNonNull(blast);
        java.util.Objects.requireNonNull(branch);
        if (blast.isPresent() && (hits > 0 || submerged || lowAir || secretIndex >= 0 || crateCredits > 0)) {
            throw new IllegalArgumentException("a mining charge is a plain destructible on the ground (M9 part E)");
        }
        if (branch.isPresent() && (lowAir || submerged)) {
            throw new IllegalArgumentException("a branch's object lies on the ground (M9 part E)");
        }
        if (hits > 0 && (secretTriggers < 1 || secretIndex < 0)) {
            throw new IllegalArgumentException("a trigger reveals a secret, alone or with others");
        }
        if (submerged && hits <= 0) {
            throw new IllegalArgumentException("only a trigger lies under the water");
        }
        if (group >= 0 && (hits > 0 || groupSize < 1)) {
            throw new IllegalArgumentException("a group is of destructibles, at least one");
        }
        if (drop.filter(PickupType.STORY::equals).isPresent()
                || bonusDrop.filter(PickupType.STORY::equals).isPresent()) {
            throw new IllegalArgumentException("a story pickup is only a group's drop (M9 part G)");
        }
        if (group < 0 && groupDrop.isPresent()) {
            throw new IllegalArgumentException("only a group's last destructible drops the group's pickup");
        }
    }

    /** With the cargo container's look. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers,
            Optional<PickupType> bonusDrop) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                bonusDrop,
                CARGO_CONTAINER);
    }

    /** Without a bonus drop. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened,
            int secretIndex,
            int secretTriggers) {
        this(
                t,
                x,
                size,
                hp,
                bounty,
                drop,
                hits,
                crateCredits,
                secret,
                hardened,
                secretIndex,
                secretTriggers,
                Optional.empty());
    }

    /** A destructible, or a trigger that reveals the level's first secret on its own. */
    public GroundObjectSpec(
            double t,
            double x,
            Hitbox size,
            double hp,
            int bounty,
            Optional<PickupType> drop,
            int hits,
            int crateCredits,
            String secret,
            boolean hardened) {
        this(t, x, size, hp, bounty, drop, hits, crateCredits, secret, hardened, hits > 0 ? 0 : -1, 1);
    }

    /** Whether it is a trigger rather than a destructible. */
    public boolean trigger() {
        return hits > 0;
    }
}
