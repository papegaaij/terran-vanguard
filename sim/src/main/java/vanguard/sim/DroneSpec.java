package vanguard.sim;

/**
 * M7 part A: a light drone at one upgrade level (design/player/wingmen, Light drone; the shop item and
 * its per-level numbers in design/player/weapons/light-drone-bay), built from the bay's {@code drone}
 * block by {@code vanguard.content.SimSpecs}. Its gun is the bay mount's {@link WeaponSpec}. M7 part G: or a
 * rear-guard drone on its trail station (design/player/wingmen, Rear-guard drone; {@link #trail}).
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
 * @param station M7 part G: where it flies: the light drone's orbit, or the rear-guard drone's trail behind the ship
 *     (design/player/wingmen, Rear-guard drone; the bay in design/player/weapons/rear-guard-drone-bay); {@code orbit},
 *     {@code periodSeconds} and {@code converge} are the orbit's, {@code behind}, {@code side}, {@code lagSeconds} and
 *     {@code fireBack} the trail's (0 and false for the other station)
 * @param behind M7 part G: a trail station's distance behind the ship's centre, px: below it on the screen, whatever the
 *     scroll's direction (40)
 * @param side M7 part G: a trail station's distance to its bay's side of the ship's centre, px (12: a pair flies side by
 *     side, 24 px apart)
 * @param lagSeconds M7 part G: how far a trailing drone lags behind its station as the ship moves, s (0.15: it swings
 *     out in a turn)
 * @param fireBack M7 part G: whether it fires straight back (180°, down the screen) instead of at a convergence point
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
        Hitbox box,
        Station station,
        double behind,
        double side,
        double lagSeconds,
        boolean fireBack) {
    /** M7 part G: where a drone flies (design/player/wingmen, Light drone and Rear-guard drone). */
    public enum Station {
        /** Round the ship's centre at its orbit radius (the light drone). */
        ORBIT,
        /** Behind the ship at its bay's side, following with a lag (the rear-guard drone). */
        TRAIL
    }

    /** The light drone's hit box (design/player/wingmen): 8×8 px. */
    public static final Hitbox BOX = new Hitbox(8, 8);

    /** What a laser touch costs (design/player/wingmen): 2 hits. */
    public static final int LASER_HITS = 2;

    public DroneSpec {
        if (station == null) {
            throw new IllegalArgumentException("a drone has a station");
        }
        if (hits < 1) {
            throw new IllegalArgumentException("a drone has hits");
        }
        if (station == Station.ORBIT && (!(orbit > 0) || !(periodSeconds > 0) || !(converge > 0))) {
            throw new IllegalArgumentException("an orbiting drone has an orbit, a period and a convergence point");
        }
        if (station == Station.TRAIL && (!(behind >= 0) || !(side >= 0) || !(lagSeconds >= 0))) {
            throw new IllegalArgumentException("a trailing drone is behind and to the side >= 0 px, its lag >= 0 s");
        }
        if (station == Station.ORBIT && (behind != 0 || side != 0 || lagSeconds != 0 || fireBack)) {
            throw new IllegalArgumentException("an orbiting drone has no trail station");
        }
        if (!(rebuildSeconds > 0) || !(fadeInSeconds >= 0) || !(contactDamage >= 0)) {
            throw new IllegalArgumentException("a drone's rebuild time is > 0, its fade-in and contact damage >= 0");
        }
        if (contactHits < 1 || smallContactHits < 1 || laserHits < 1) {
            throw new IllegalArgumentException("a contact or a laser costs a drone at least one hit");
        }
    }

    /** An orbiting drone (the light drone's station; without M7 part G's trail). */
    public DroneSpec(
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
        this(
                orbit,
                periodSeconds,
                converge,
                hits,
                rebuildSeconds,
                fadeInSeconds,
                contactHits,
                smallContactHits,
                laserHits,
                contactDamage,
                box,
                Station.ORBIT,
                0,
                0,
                0,
                false);
    }

    /**
     * M7 part G: a trailing drone (the rear-guard drone, design/player/wingmen): {@code behind} px behind the ship's
     * centre and {@code side} px to its bay's side, lagging {@code lagSeconds}, firing straight back when {@code
     * fireBack}; block, contact, break and rebuild as an orbiting drone's.
     */
    public static DroneSpec trail(
            double behind,
            double side,
            double lagSeconds,
            boolean fireBack,
            int hits,
            double rebuildSeconds,
            double fadeInSeconds,
            int contactHits,
            int smallContactHits,
            int laserHits,
            double contactDamage,
            Hitbox box) {
        return new DroneSpec(
                0,
                0,
                0,
                hits,
                rebuildSeconds,
                fadeInSeconds,
                contactHits,
                smallContactHits,
                laserHits,
                contactDamage,
                box,
                Station.TRAIL,
                behind,
                side,
                lagSeconds,
                fireBack);
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
