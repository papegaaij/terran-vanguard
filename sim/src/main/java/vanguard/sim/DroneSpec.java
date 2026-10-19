package vanguard.sim;

/**
 * M7 part A: a light drone at one upgrade level (design/player/wingmen, Light drone; the shop item and
 * its per-level numbers in design/player/weapons/light-drone-bay), built from the bay's {@code drone}
 * block by {@code vanguard.content.SimSpecs}. Its gun is the bay mount's {@link WeaponSpec}.
 *
 * @param orbit the orbit's radius round the ship's centre, px
 * @param periodSeconds one clockwise turn of the orbit
 * @param converge the convergence point's distance ahead of the ship's centre, px: every shot is aimed
 *     at it
 * @param hits how many hits it absorbs before it breaks
 * @param rebuildSeconds how long the bay takes to build a new drone after one broke
 * @param fadeInSeconds how long a rebuilt drone fades in, moving from its bay to its orbit
 * @param contactHits what an {@code air} enemy touching it costs it, hits
 * @param smallContactHits what a {@code tiny} or {@code small} rammer touching it costs it (that rammer
 *     is destroyed), hits
 * @param laserHits what a laser (a sweeping beam) touching it costs it, hits; the laser goes on
 * @param contactDamage the damage an enemy touching it takes
 * @param box its hit box
 */
public record DroneSpec(
        double orbit,
        double periodSeconds,
        double converge,
        int hits,
        double rebuildSeconds,
        double fadeInSeconds,
        int contactHits,
        int smallContactHits,
        int laserHits,
        double contactDamage,
        Hitbox box) {
    /** The light drone's hit box (design/player/wingmen): 8×8 px. */
    public static final Hitbox BOX = new Hitbox(8, 8);

    /** What a laser touch costs (design/player/wingmen): 2 hits. */
    public static final int LASER_HITS = 2;

    public DroneSpec {
        if (!(orbit > 0) || !(periodSeconds > 0) || !(converge > 0) || hits < 1) {
            throw new IllegalArgumentException("a drone has an orbit, a period, a convergence point and hits");
        }
        if (!(rebuildSeconds > 0) || !(fadeInSeconds >= 0) || !(contactDamage >= 0)) {
            throw new IllegalArgumentException("a drone's rebuild time is > 0, its fade-in and contact damage >= 0");
        }
        if (contactHits < 1 || smallContactHits < 1 || laserHits < 1) {
            throw new IllegalArgumentException("a contact or a laser costs a drone at least one hit");
        }
    }

    /** A drone with the light drone's laser cost ({@link #LASER_HITS}) and hit box ({@link #BOX}). */
    public DroneSpec(
            double orbit,
            double periodSeconds,
            double converge,
            int hits,
            double rebuildSeconds,
            double fadeInSeconds,
            int contactHits,
            int smallContactHits,
            double contactDamage) {
        this(
                orbit,
                periodSeconds,
                converge,
                hits,
                rebuildSeconds,
                fadeInSeconds,
                contactHits,
                smallContactHits,
                LASER_HITS,
                contactDamage,
                BOX);
    }
}
