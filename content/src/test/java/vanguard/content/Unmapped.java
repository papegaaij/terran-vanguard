package vanguard.content;

import java.util.Set;

/**
 * M7 part C (step C2): the levels whose data carries keys the simulation's mapping ({@link SimSpecs}) does not know
 * yet. The tests that map or fly every level leave these out until then. Step C4 mapped Level 16's terrain, gates,
 * crawlers and its {@code first-wall-contact} cue ({@link TerrainRules}). M7 part D (step D2): Level 17 Dome Siege
 * (its {@code allies} block, the {@code untouched} secondary, a pickup's {@code dropped_by.enemy}, {@code flak}) and
 * the Choir Herald ({@code aura}) and the Shellback ({@code roll}, a non-boss unit's parts), until step D4 mapped them
 * ({@link SiegeRules}). M7 part E (step E2): Level 18 Dust Storm (its {@code visibility}, the music's {@code motif})
 * and the Dust Devil ({@code vortex}, {@code pull}, the {@code while: open} spiral, the {@code vanes}) and the Ghost
 * Drone ({@code lane_strafe}, the pass {@code trigger}, {@code tell}, {@code reaim}, {@code second_pass}), until step
 * E4 mapped them ({@link StormRules}).
 */
final class Unmapped {
    /** Why a test leaves a level out. */
    static final String REASON = "not mapped yet";

    /** The level keys the mapping does not know yet. */
    static final Set<String> LEVELS = Set.of();

    /** The stat blocks the mapping does not know yet. */
    static final Set<String> ENEMIES = Set.of();

    private Unmapped() {}

    /** Whether the simulation's mapping knows the stat block's keys. */
    static boolean mappedEnemy(String slug) {
        return !ENEMIES.contains(slug);
    }

    /** Whether the simulation's mapping knows the level's keys. */
    static boolean mapped(String key) {
        return !LEVELS.contains(key);
    }
}
