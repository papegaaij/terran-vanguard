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
 * @param antiSub M8 part A: it has the {@code anti-sub} trait (design/world/europa, Under water rules): it reaches the
 *     {@code sub} layer and does full damage under water ({@link Medium})
 * @param splash M8 part A (design/player/weapons/harpoon-torpedoes): the share of a torpedo's damage its hit deals to
 *     the other units within its {@code blast} radius; 0 for none (M9 part A: an arc's sparks, the same rule)
 * @param shieldBreaker M9 part A (design/enemies, Enemy shields): it has the {@code shield-breaker} trait: a hit
 *     empties a unit's shield at once and holds its refill back
 * @param seek M9 part A: a homing weapon's target pick ({@link Seek#NEAREST} for every other weapon)
 * @param chain M10 part A (design/player/weapons/plasma-arc): a {@link Delivery#CHAIN} weapon's chain; empty for every
 *     other weapon
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
        boolean flanking,
        boolean antiSub,
        double splash,
        boolean shieldBreaker,
        Seek seek,
        java.util.Optional<Chain> chain) {
    public WeaponSpec {
        java.util.Objects.requireNonNull(seek);
        if (chain.isPresent() != (delivery == Delivery.CHAIN)) {
            throw new IllegalArgumentException(slug + ": a chain weapon has its chain, others none");
        }
        if (delivery == Delivery.CHAIN && (muzzles.size() != 1 || !(range > 0) || !(coneHalfAngle > 0))) {
            throw new IllegalArgumentException(slug + ": a chain has one muzzle, a reach and a cone");
        }
        if (seek != Seek.NEAREST && delivery != Delivery.HOMING) {
            throw new IllegalArgumentException(slug + ": only a homing weapon has a seek rule: " + seek);
        }
        if (delivery == Delivery.ARC && (muzzles.size() != 1 || !(range > 0) || !(coneHalfAngle > 0))) {
            throw new IllegalArgumentException(slug + ": an arc has one muzzle, a reach and a spread");
        }
        muzzles = List.copyOf(muzzles);
        if (muzzles.isEmpty()) {
            throw new IllegalArgumentException("a volley has at least one projectile");
        }
        if (!(rate > 0) || pierce < 1) {
            throw new IllegalArgumentException(slug + ": the rate must be > 0 and the pierce at least 1");
        }
        if (accelSeconds < 0 || delivery.laid() == mines.equals(Mines.NONE)) {
            throw new IllegalArgumentException(slug + ": a mine or depth-charge weapon has mine numbers, others none");
        }
        if (delivery == Delivery.DEPTH_CHARGE && !(mines.fuseSeconds() > 0 && mines.driftSeconds() > 0)) {
            throw new IllegalArgumentException(slug + ": a depth charge has a drift and a fuse");
        }
        if (!(splash >= 0 && splash <= 1)) {
            throw new IllegalArgumentException(slug + ": a splash is a share of the damage, 0..1: " + splash);
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

    /** A weapon without M8 part A's {@code anti-sub} trait and splash. */
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
            Mines mines,
            boolean flanking) {
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
                flanking,
                false,
                0,
                false,
                Seek.NEAREST,
                java.util.Optional.empty());
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
        return copy(damage, blast, muzzles, sideOrRear, antiSub, splash, shieldBreaker, seek, chain);
    }

    /** M8 part A: this weapon with the {@code anti-sub} trait ({@code antiSubTrait}) or without it. */
    public WeaponSpec antiSub(boolean antiSubTrait) {
        return copy(damage, blast, muzzles, flanking, antiSubTrait, splash, shieldBreaker, seek, chain);
    }

    /**
     * M8 part A (design/player/weapons/harpoon-torpedoes): this torpedo with a splash: its hit deals {@code share} of
     * its damage to the other units within {@code radius} px (its {@code blast}).
     */
    public WeaponSpec splash(double radius, double share) {
        return copy(damage, radius, muzzles, flanking, antiSub, share, shieldBreaker, seek, chain);
    }

    /**
     * M9 part A (design/enemies, Enemy shields): this weapon with the {@code shield-breaker} trait ({@code breaker}) or
     * without it: its hit on a shielded unit empties the shield at once, the whole hit going to the hull ({@link
     * Enemy#damageBreaking}).
     */
    public WeaponSpec withShieldBreaker(boolean breaker) {
        return copy(damage, blast, muzzles, flanking, antiSub, splash, breaker, seek, chain);
    }

    /**
     * M9 part A (design/player/weapons/swarm-tail): this homing weapon with its target pick {@code rule} ({@link
     * Seek#NEAREST} for every homing weapon before it).
     */
    public WeaponSpec withSeek(Seek rule) {
        return copy(damage, blast, muzzles, flanking, antiSub, splash, shieldBreaker, rule, chain);
    }

    /**
     * M9 part A, RF7: the one copy the {@code withX} copies go through: the components a copy may change, the others
     * this weapon's (the canonical constructor's checks run).
     */
    private WeaponSpec copy(
            double newDamage,
            double newBlast,
            List<Muzzle> newMuzzles,
            boolean newFlanking,
            boolean newAntiSub,
            double newSplash,
            boolean newShieldBreaker,
            Seek newSeek,
            java.util.Optional<Chain> newChain) {
        return new WeaponSpec(
                slug,
                vfx,
                sfx,
                delivery,
                antiGround,
                rate,
                newDamage,
                speed,
                size,
                range,
                lifetimeSeconds,
                pierce,
                newBlast,
                turnRate,
                coneHalfAngle,
                airSeconds,
                snap,
                newMuzzles,
                endSpeed,
                accelSeconds,
                mines,
                newFlanking,
                newAntiSub,
                newSplash,
                newShieldBreaker,
                newSeek,
                newChain);
    }

    /**
     * M9 part A (design/player/weapons/tesla-coil-pod, the {@code arc} delivery): {@code rate} arcs a second, each an
     * instant hit of {@code damage} from {@code pod} on the nearest target within {@code reach} px in a forward arc of
     * {@code spreadRadians} (everything but the wedge straight behind; a shielded unit first), and sparks: {@code
     * sparkShare} of the damage on the other units within {@code sparkRadius} px of the struck one (the splash).
     * Not {@code shield-breaker} until {@link #withShieldBreaker}; no projectile, speed or pierce.
     */
    public static WeaponSpec arc(
            String slug,
            String vfx,
            String sfx,
            double rate,
            double damage,
            double reach,
            double spreadRadians,
            double sparkRadius,
            double sparkShare,
            Muzzle pod) {
        return new WeaponSpec(
                        slug,
                        vfx,
                        sfx,
                        Delivery.ARC,
                        false,
                        rate,
                        damage,
                        0,
                        new Hitbox(1, 1),
                        reach,
                        0,
                        1,
                        0,
                        0,
                        spreadRadians / 2,
                        0,
                        0,
                        List.of(pod))
                .splash(sparkRadius, sparkShare);
    }

    /**
     * M10 part A (design/player/weapons/plasma-arc, the {@code chain} delivery; {@link ChainFire}): {@code rate}
     * discharges a second from {@code nose}, each an instant first strike of {@code damage} on the nearest valid target
     * within {@code reach} px in a forward cone {@code coneRadians} wide, then {@code chain}'s jumps. Not {@code
     * shield-breaker}; no projectile, speed, pierce or splash.
     */
    public static WeaponSpec chain(
            String slug,
            String vfx,
            String sfx,
            double rate,
            double damage,
            double reach,
            double coneRadians,
            Chain chain,
            Muzzle nose) {
        return new WeaponSpec(
                slug,
                vfx,
                sfx,
                Delivery.CHAIN,
                false,
                rate,
                damage,
                0,
                new Hitbox(1, 1),
                reach,
                0,
                1,
                0,
                0,
                coneRadians / 2,
                0,
                0,
                List.of(nose),
                0,
                0,
                Mines.NONE,
                false,
                false,
                0,
                false,
                Seek.NEAREST,
                java.util.Optional.of(chain));
    }

    /** M10 part A: this chain weapon with another {@code rule} (an overdrive's longer reach and jump: its own spec). */
    public WeaponSpec withChain(Chain rule) {
        return copy(
                damage, blast, muzzles, flanking, antiSub, splash, shieldBreaker, seek, java.util.Optional.of(rule));
    }

    /**
     * M10 part A (design/player/weapons/plasma-arc, Behaviour): a chain's jumps: from each struck unit to the nearest
     * unit this discharge has not struck within {@code jump} px of it, until {@code count} units are struck (the first
     * strike included) or none is in reach; each strike does {@code falloff} × the damage of the one before.
     */
    public record Chain(int count, double jump, double falloff) {
        public Chain {
            if (count < 1 || !(jump > 0) || !(falloff > 0 && falloff <= 1)) {
                throw new IllegalArgumentException(
                        "a chain strikes at least once, jumps some px and falls off by a share in (0, 1]: " + count
                                + ", " + jump + ", " + falloff);
            }
        }
    }

    /**
     * M9 part A: how a homing weapon's projectile picks its target (design/player/weapons/swarm-tail). Each re-picks
     * with its own rule when its target dies.
     */
    public enum Seek {
        /** The nearest target in its seek radius and its cone (every homing weapon before the Swarm Tail). */
        NEAREST,
        /**
         * The nearest enemy behind the ship's nose line (below the row of the ship's centre; above it in a reversed
         * scroll) within its seek radius, all round; with none there the nearest anywhere within it.
         */
        REAR_FIRST
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
    public record Mines(double driftSeconds, double armSeconds, double trigger, int maxLive, double fuseSeconds) {
        /** Not a mine weapon. */
        public static final Mines NONE = new Mines(0, 0, 0, 0);

        /** A proximity mine: no fuse. */
        public Mines(double driftSeconds, double armSeconds, double trigger, int maxLive) {
            this(driftSeconds, armSeconds, trigger, maxLive, 0);
        }

        /**
         * M8 part A (design/player/weapons/depth-charges): a {@link Delivery#DEPTH_CHARGE}'s numbers: it is tossed and
         * slows to a hover over {@code driftSeconds}, then sinks with the scroll, and bursts {@code fuseSeconds} after
         * its launch (M8 AW: or sooner on touching a swimmer under water); at most {@code maxLive} of a mount's charges are out at once.
         */
        public static Mines charge(double driftSeconds, double fuseSeconds, int maxLive) {
            return new Mines(driftSeconds, 0, 0, maxLive, fuseSeconds);
        }
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
        BEAM,
        /**
         * M8 part A (design/player/weapons/depth-charges, the {@code anti-sub} and {@code area} rear weapon): a barrel
         * leaves the muzzle at the weapon's speed on the muzzle's angle (M8 AW: tossed forward), slows to a hover over
         * its {@link Mines#driftSeconds()}, then sinks with the scroll, and bursts on its {@link Mines#fuseSeconds()}
         * or (M8 AW) under water on touching a swimmer in its blast: once on every target in the water ({@link
         * Medium#chargeReaches}; above water the {@code sub} layer and the naval ground), never the ship or Rook. It
         * damages nothing on its way and is hit by nothing. Only where there is water
         * (a level over water or under water): elsewhere its mount fires nothing. Appended last so the earlier
         * deliveries keep their ordinals.
         */
        DEPTH_CHARGE,
        /**
         * M9 part A (design/player/weapons/tesla-coil-pod, the {@code arc}): no projectile: on its fire step the pod
         * strikes its target at once ({@link ArcFire}), on {@code air}, {@code low-air} and {@code ground} as a bolt
         * (hardened ground glances it off; never {@code high-air} or {@code sub}); without a target it does not fire.
         * Appended last so the earlier deliveries keep their ordinals.
         */
        ARC,
        /**
         * M10 part A (design/player/weapons/plasma-arc, the {@code chain}): no projectile: on its fire step the arc
         * strikes the nearest valid target in its forward cone at once and jumps on along its {@link Chain} ({@link
         * ChainFire}), on {@code air}, {@code low-air} and {@code ground} as a bolt (hardened ground glances it off and
         * ends the chain; never {@code high-air} or {@code sub}); without a target it does not fire. Appended last so
         * the earlier deliveries keep their ordinals.
         */
        CHAIN;

        /** Whether its projectiles hit enemies on {@code layer} on their way (blasts aside). */
        public boolean reaches(Layer layer) {
            return switch (this) {
                case BOLT, ARC, CHAIN -> layer.hitByStandardShots();
                // M7 part B: nothing reaches the buried layer.
                case HOMING, TURRET, BEAM -> layer != Layer.SUB && layer.touchable();
                case TORPEDO -> layer == Layer.SUB || layer == Layer.GROUND;
                case DROPPED, LOBBED, MINE, DEPTH_CHARGE -> false;
            };
        }

        /**
         * M8 part A: whether its projectiles are laid in place and burst there, hitting nothing on their way: proximity
         * mines and depth charges.
         */
        public boolean laid() {
            return this == MINE || this == DEPTH_CHARGE;
        }

        /** M8 part A: whether it runs only where there is water (its mount reads NO WATER elsewhere). */
        public boolean needsWater() {
            return this == TORPEDO || this == DEPTH_CHARGE;
        }

        /**
         * M9 part C (design/campaign Level 30): whether it is a bomb delivery that needs a ground layer (dropped and
         * lobbed: they hit only the ground); in a level without one its mount reads NO GROUND and fires nothing.
         */
        public boolean needsGround() {
            return this == DROPPED || this == LOBBED;
        }

        /**
         * Whether its projectiles pass under what stops shots in flight (crane arms, a running sled,
         * tow cables, debris): bombs and shells on their way down, torpedoes (M8 part A: and
         * depth charges) under the water.
         */
        public boolean passesUnder() {
            return landing() || this == TORPEDO || this == DEPTH_CHARGE;
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
        return copy(damage * factor, blast, from, flanking, antiSub, splash, shieldBreaker, seek, chain);
    }

    int intervalTicks() {
        return Math.max(1, SimStep.ticks(1 / rate));
    }
}
