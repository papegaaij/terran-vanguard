package vanguard.sim;

import java.util.List;

/**
 * One fitted weapon at one upgrade level, from design/player/weapons/&lt;slug&gt;/data.yaml (built
 * by {@code vanguard.content.SimSpecs}): how often it fires, what each projectile does, and where
 * the projectiles of a volley leave the ship.
 *
 * @param slug the weapon's directory name, e.g. {@code scatter-vulcan}
 * @param vfx its projectile family (design/player/weapons, concept round 08), for the presentation
 * @param sfx its sound family (design/audio/sfx), for the presentation
 * @param delivery how its projectiles travel, which decides the layers they reach
 * @param antiGround whether it has the {@code anti-ground} trait: it damages hardened ground
 *     targets, and a {@link Delivery#BOLT} or {@link Delivery#HOMING} weapon does double damage on
 *     the ground layer (a ground-only weapon's damage already includes it)
 * @param rate volleys per second
 * @param damage damage per projectile; M7 part A: a {@link Delivery#BEAM}'s damage per simulation step
 *     (its rate is {@link SimStep#PER_SECOND}, so rate times damage is its damage per second)
 * @param speed px/s; 0 for dropped and lobbed projectiles
 * @param size a projectile's hit box; M7 part A: a {@link Delivery#BEAM}'s column width (its height
 *     is not used)
 * @param range px a bolt (or a turret's shot, a torpedo) flies before it is gone ({@link
 *     Double#POSITIVE_INFINITY}: until it leaves the screen); a homing weapon's seek radius (a
 *     torpedo's too) and a turret's reach; how far ahead of the muzzle a lobbed shell lands
 * @param lifetimeSeconds how long a homing projectile flies, how long a mine waits before it fizzles
 * @param pierce how many targets a bolt passes through before it is gone (1: the first stops it)
 * @param blast the blast radius of a dropped or lobbed projectile, px
 * @param turnRate a homing projectile's (or a torpedo's) turn rate or a turret's slew rate, radians per second: the one
 *     number a turn bonus (the Targeting computer, design/player/systems) scales
 * @param coneHalfAngle a homing projectile (or a torpedo) picks targets within this angle either side of its heading
 * @param airSeconds how long a dropped bomb falls or a lobbed shell flies
 * @param snap a lobbed shell lands on the nearest destructible ground target this close to its
 *     landing point, px
 * @param muzzles one per projectile of a volley
 * @param endSpeed px/s an accelerating projectile reaches ({@code speed} when it does not accelerate)
 * @param accelSeconds how long it takes to get from {@code speed} to {@code endSpeed}; 0 for none
 * @param mines a {@link Delivery#MINE} weapon's mine numbers; {@link Mines#NONE} for other weapons
 * @param flanking M5 part F: it has the {@code side} or the {@code rear} trait: its shots hit a latched
 *     Lamprey (design/enemies/air/lamprey, user decision F3 = a), as a rear mount's do
 */
public record WeaponSpec(
        String slug,
        String vfx,
        String sfx,
        Delivery delivery,
        boolean antiGround,
        double rate,
        double damage,
        double speed,
        Hitbox size,
        double range,
        double lifetimeSeconds,
        int pierce,
        double blast,
        double turnRate,
        double coneHalfAngle,
        double airSeconds,
        double snap,
        List<Muzzle> muzzles,
        double endSpeed,
        double accelSeconds,
        Mines mines,
        boolean flanking) {
    public WeaponSpec {
        muzzles = List.copyOf(muzzles);
        if (muzzles.isEmpty()) {
            throw new IllegalArgumentException("a volley has at least one projectile");
        }
        if (!(rate > 0) || pierce < 1) {
            throw new IllegalArgumentException(slug + ": the rate must be > 0 and the pierce at least 1");
        }
        if (accelSeconds < 0 || (delivery == Delivery.MINE) == mines.equals(Mines.NONE)) {
            throw new IllegalArgumentException(slug + ": a mine weapon has mine numbers, others none");
        }
        if (delivery == Delivery.BEAM
                && (muzzles.size() != 1 || rate != SimStep.PER_SECOND || pierce != 1 || !(size.width() > 0))) {
            throw new IllegalArgumentException(
                    slug + ": a beam has one muzzle, a width, pierce 1 and the rate " + SimStep.PER_SECOND);
        }
    }

    /**
     * M7 part A: a {@link Delivery#BEAM} weapon (design/player/weapons/ion-beam): one column of {@code
     * width} px from {@code nose} straight up, dealing {@code damagePerSecond} as one sixtieth every
     * simulation step to the first target it touches. Its rate is {@link SimStep#PER_SECOND} and its
     * damage the damage per step; it has no speed, range, pierce, blast or mines.
     */
    public static WeaponSpec beam(
            String slug, String vfx, String sfx, double width, double damagePerSecond, Muzzle nose) {
        return new WeaponSpec(
                slug,
                vfx,
                sfx,
                Delivery.BEAM,
                false,
                SimStep.PER_SECOND,
                damagePerSecond / SimStep.PER_SECOND,
                0,
                new Hitbox(width, 0),
                Double.POSITIVE_INFINITY,
                0,
                1,
                0,
                0,
                0,
                0,
                0,
                List.of(nose));
    }

    /** A weapon without M5 part F's {@code side} or {@code rear} trait. */
    public WeaponSpec(
            String slug,
            String vfx,
            String sfx,
            Delivery delivery,
            boolean antiGround,
            double rate,
            double damage,
            double speed,
            Hitbox size,
            double range,
            double lifetimeSeconds,
            int pierce,
            double blast,
            double turnRate,
            double coneHalfAngle,
            double airSeconds,
            double snap,
            List<Muzzle> muzzles,
            double endSpeed,
            double accelSeconds,
            Mines mines) {
        this(
                slug,
                vfx,
                sfx,
                delivery,
                antiGround,
                rate,
                damage,
                speed,
                size,
                range,
                lifetimeSeconds,
                pierce,
                blast,
                turnRate,
                coneHalfAngle,
                airSeconds,
                snap,
                muzzles,
                endSpeed,
                accelSeconds,
                mines,
                false);
    }

    /** This weapon with M5 part F's {@code side} or {@code rear} trait ({@code flanking}) or without it. */
    public WeaponSpec flanking(boolean sideOrRear) {
        return new WeaponSpec(
                slug,
                vfx,
                sfx,
                delivery,
                antiGround,
                rate,
                damage,
                speed,
                size,
                range,
                lifetimeSeconds,
                pierce,
                blast,
                turnRate,
                coneHalfAngle,
                airSeconds,
                snap,
                muzzles,
                endSpeed,
                accelSeconds,
                mines,
                sideOrRear);
    }

    /** A weapon of the Act 1 kind: no acceleration, no mines. */
    public WeaponSpec(
            String slug,
            String vfx,
            String sfx,
            Delivery delivery,
            boolean antiGround,
            double rate,
            double damage,
            double speed,
            Hitbox size,
            double range,
            double lifetimeSeconds,
            int pierce,
            double blast,
            double turnRate,
            double coneHalfAngle,
            double airSeconds,
            double snap,
            List<Muzzle> muzzles) {
        this(
                slug,
                vfx,
                sfx,
                delivery,
                antiGround,
                rate,
                damage,
                speed,
                size,
                range,
                lifetimeSeconds,
                pierce,
                blast,
                turnRate,
                coneHalfAngle,
                airSeconds,
                snap,
                muzzles,
                speed,
                0,
                Mines.NONE);
    }

    /**
     * What a {@link Delivery#MINE} weapon's mines do (design/player/weapons/proximity-mines): a mine
     * leaves the muzzle at the weapon's speed, which decays to nothing over {@code driftSeconds}, then
     * holds its screen position; it arms after {@code armSeconds} and bursts (in the weapon's blast)
     * when an enemy on {@code air} or {@code low-air} comes within {@code trigger} px; it fizzles after
     * the weapon's lifetime. While {@code maxLive} of a mount's mines are out it drops no more.
     */
    public record Mines(double driftSeconds, double armSeconds, double trigger, int maxLive) {
        /** Not a mine weapon. */
        public static final Mines NONE = new Mines(0, 0, 0, 0);
    }

    /**
     * Where a projectile leaves the ship and where it heads.
     *
     * @param dx px right of the ship's centre
     * @param dy px above the ship's centre
     * @param angle radians clockwise from straight up the screen (pi / 2 = right)
     */
    public record Muzzle(double dx, double dy, double angle) {}

    /** How projectiles travel (design/enemies, layer rules). */
    public enum Delivery {
        /** Flies straight; hits {@code air}, {@code low-air} and {@code ground}. */
        BOLT,
        /**
         * Seeks the nearest enemy in its cone; hits every layer a bolt does and {@code high-air}, never
         * {@code sub}.
         */
        HOMING,
        /** Falls onto the ground below its release point; hits only the ground layer, in a blast. */
        DROPPED,
        /** Lobbed onto the ground ahead; hits only the ground layer, in a blast. */
        LOBBED,
        /**
         * A turret on its pod aims at the nearest enemy all round and fires straight shots its way;
         * they hit every layer a homing missile does.
         */
        TURRET,
        /**
         * Dropped as a mine that holds its screen position and bursts when an enemy comes close; it
         * hits nothing on its way, its blast hits the {@code air}, {@code low-air} and {@code ground}
         * layers (the {@code area} rule).
         */
        MINE,
        /**
         * M5 part E (design/player/weapons/torpedo-pod; the {@code anti-sub} delivery): dropped from
         * its pod, it runs under the water's surface, speeding up from its speed to its end speed and
         * steering at most its turn rate towards the nearest {@code sub} or {@code ground} unit in its
         * cone, until it has run its range; it strikes the first {@code sub} or {@code ground} target
         * it touches (units, destructibles, a sunken trigger) at full damage, never a flyer. Only in a
         * level over water ({@link GroundSpec#water()}): elsewhere its mount fires nothing.
         */
        TORPEDO,
        /**
         * M7 part A (design/player/weapons/ion-beam, the {@code beam} delivery): no projectiles; while
         * fire is held a column of the weapon's width runs from its muzzle straight up and ends at the
         * first target in it, nearest the ship, on {@code air}, {@code low-air}, {@code ground} or
         * {@code high-air} (a tie goes to the higher layer), which takes the weapon's damage every
         * simulation step. Hardened ground targets do not stop it; it never pierces. Appended last so
         * the earlier deliveries keep their ordinals.
         */
        BEAM;

        /** Whether its projectiles hit enemies on {@code layer} on their way (blasts aside). */
        public boolean reaches(Layer layer) {
            return switch (this) {
                case BOLT -> layer.hitByStandardShots();
                // M7 part B: nothing reaches the buried layer.
                case HOMING, TURRET, BEAM -> layer != Layer.SUB && layer.touchable();
                case TORPEDO -> layer == Layer.SUB || layer == Layer.GROUND;
                case DROPPED, LOBBED, MINE -> false;
            };
        }

        /**
         * Whether its projectiles pass under what stops shots in flight (crane arms, a running sled,
         * tow cables, debris): bombs and shells on their way down, torpedoes under the water.
         */
        public boolean passesUnder() {
            return landing() || this == TORPEDO;
        }

        /** Whether its projectiles seek a target: homing missiles and torpedoes. */
        public boolean seeks() {
            return this == HOMING || this == TORPEDO;
        }

        /** Whether its projectiles land and burst on the ground rather than fly through. */
        public boolean landing() {
            return this == DROPPED || this == LOBBED;
        }
    }

    /**
     * This weapon with each projectile's damage times {@code factor}, fired from {@code from}
     * (design/player/wingmen: Rook's guns are the player weapons' tables, scaled, from his nose).
     */
    public WeaponSpec scaled(double factor, List<Muzzle> from) {
        return new WeaponSpec(
                slug,
                vfx,
                sfx,
                delivery,
                antiGround,
                rate,
                damage * factor,
                speed,
                size,
                range,
                lifetimeSeconds,
                pierce,
                blast,
                turnRate,
                coneHalfAngle,
                airSeconds,
                snap,
                from,
                endSpeed,
                accelSeconds,
                mines,
                flanking);
    }

    int intervalTicks() {
        return Math.max(1, SimStep.ticks(1 / rate));
    }
}
