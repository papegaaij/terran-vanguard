package vanguard.game.render;

import java.util.Set;
import vanguard.sim.GroundObjectSpec;

/**
 * Which triggers break like a destructible on the hit that spends them (design/tech/architecture,
 * ground targets): those whose look's spent frame is a wreck, Level 08's billboard (intact, hit,
 * toppled). Its topple plays a destructible's break: the small explosion where it stands, the
 * small blast and the crumble of its size; its toppled frame stays as the wreck. The other triggers
 * are opened or switched off rather than destroyed and stay quiet: Level 06's survey cache is shot
 * open, its terminal releases its core, the beacon, the lifeboat lights and the sled's clamp go dark.
 */
public final class TriggerBreak {
    /** The trigger looks whose spent frame is a wreck. */
    private static final Set<String> WRECKED_WHEN_SPENT = Set.of("billboard");

    private TriggerBreak() {}

    /** Whether {@code spec} is a trigger that breaks apart on the hit that spends it. */
    public static boolean breaks(GroundObjectSpec spec) {
        return spec.trigger() && WRECKED_WHEN_SPENT.contains(spec.look());
    }

    /** M5 part H: a hit-by-hit trigger shows its cleared frame this long after its last hit, then its open one. */
    static final int CLEARED_TICKS = 12;

    /**
     * M5 part H: whether a trigger look of {@code frames} frames shows its hits one by one: a frame per
     * hit taken, then cleared and opened (Level 14's armoured car: 6 hits, 8 frames, tools/art/l14_props.py).
     */
    static boolean hitByHit(int frames, int hits) {
        return hits > 1 && frames == hits + 2;
    }

    /**
     * M5 part H: a hit-by-hit trigger's frame: one per hit taken ({@code hits - hitsLeft}), its
     * cleared frame for {@value #CLEARED_TICKS} steps after the hit that spends it, then its last (open).
     */
    static int hitFrame(int hits, int hitsLeft, int ticksSinceHit) {
        if (hitsLeft > 0) {
            return Math.clamp(hits - hitsLeft, 0, hits - 1);
        }
        return ticksSinceHit < CLEARED_TICKS ? hits : hits + 1;
    }

    /**
     * M7 part C: the trigger looks whose door bursts on the hit that spends them (Level 16's Candor relay hut,
     * tools/art/canyon_units.py): a destructible's small explosion at the door, the burst frame staying.
     */
    private static final Set<String> DOOR_BURSTS = Set.of("relay-hut");

    /** M7 part C: whether {@code spec} is a trigger whose door bursts open on the hit that spends it. */
    public static boolean doorBursts(GroundObjectSpec spec) {
        return spec.trigger() && DOOR_BURSTS.contains(spec.look());
    }

    /** M7 part C: whether {@code look} shows its door's state hit by hit ({@link #doorFrame}). */
    static boolean door(String look) {
        return DOOR_BURSTS.contains(look);
    }

    /**
     * M7 part C: a door's frame of four (sealed, one hit, two hits, burst): the hits taken spread over the
     * two damaged frames, so medium's 3 hits show 0, 1, 2, then 3 and hard's 5 show 0, 1, 1, 2, 2, then 3.
     */
    static int doorFrame(int hits, int hitsLeft, boolean spent) {
        if (spent || hitsLeft <= 0) {
            return 3;
        }
        int taken = hits - hitsLeft;
        return hits <= 1 ? 0 : Math.clamp((taken * 2 + hits - 2) / (hits - 1), 0, 2);
    }
}
