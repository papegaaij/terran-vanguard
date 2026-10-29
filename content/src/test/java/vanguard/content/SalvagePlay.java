package vanguard.content;

import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;

/**
 * M9 part G (Level 34 Salvage Run): the autopilot's targets in a salvage level (see {@link Autopilot}; design/campaign
 * Level 34). The ground objects worth a shot come before the air units: the containers of an object escapes secondary
 * ({@code Strip the graveyard}: each one opened before it leaves the bottom edge), the destructible group that frees
 * the story pickup (the freighter's hold clamps) and the hidden crates' triggers, the lowest of them first, lined up
 * under (followed down as it nears the ship); the story pickup (the Helix core) waited
 * under while it rises out of the hold and then flown through. A level without an object escapes secondary or a story
 * pickup never asks it ({@link #salvage}), so nothing here changes another level's flight. Allocation-free.
 */
final class SalvagePlay {
    private SalvagePlay() {}

    /** The lowest it follows a low object down to, px: the object stays above it, in the guns' line, longer. */
    static final double LOW = 45;

    /** It keeps an object it lines up under this far above it, px (twice that once it follows it down). */
    static final double BELOW = 30;

    /** It waits this far below the rising story pickup, px, and flies through it once it is loose. */
    static final double UNDER_CORE = 70;

    /** Whether the level is a salvage level: an object escapes secondary or a story pickup. */
    static boolean salvage(Sortie sortie) {
        return sortie.storyPickup().isPresent()
                || sortie.script().objectives().secondary().objects();
    }

    /**
     * The ground object to line up under: of the containers the secondary counts, the story pickup's group and the
     * hidden crates' triggers on the screen above the ship, the lowest; null for none.
     */
    static GroundObject target(Sortie sortie, double shipY) {
        Secondary secondary = sortie.script().objectives().secondary();
        GroundObject lowest = null;
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            GroundObjectSpec spec = object.spec();
            double y = object.renderY(1);
            boolean worth = spec.trigger()
                    ? !object.spent() && spec.secretIndex() >= 0 && !object.shut()
                    : secondary.countsObject(spec.look()) || storyGroup(spec);
            if (worth
                    && !object.dormant()
                    && y > shipY + BELOW
                    && y < PlayField.HEIGHT - 6
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = object;
            }
        }
        return lowest;
    }

    /** Whether {@code spec} is one of a destructible group whose last one frees the story pickup. */
    private static boolean storyGroup(GroundObjectSpec spec) {
        return spec.group() >= 0 && spec.groupDrop().orElse(null) == PickupType.STORY;
    }

    /**
     * The height to line up under {@code object} from: its cruise, and once the object comes down near it, following it
     * down (at most to {@link #LOW}) so it stays in the guns' line. (Waiting low from the screen's middle cost a hard
     * run its armour at the bottom edge: measured in step G3.)
     */
    static double targetY(GroundObject object, double cruise) {
        return Math.clamp(object.renderY(1) - 2 * BELOW, LOW, Math.max(LOW, cruise));
    }

    /** Whether the story pickup is rising out of the hold (it is waited under) or loose (it is flown through). */
    static boolean coreOut(Sortie sortie) {
        StoryPickups story = sortie.storyPickup().orElse(null);
        return story != null && (story.rising() || story.state() == StoryPickups.State.LOOSE);
    }

    /** The story pickup's x: the rising crate's or the loose pickup's. */
    static double coreX(Sortie sortie) {
        Pickup loose = loose(sortie);
        return loose != null
                ? loose.renderX(1)
                : sortie.storyPickup().orElseThrow().renderX();
    }

    /** Where to fly for the story pickup: under it while it rises, through its middle once it is loose. */
    static double coreY(Sortie sortie) {
        Pickup loose = loose(sortie);
        if (loose != null) {
            return Math.max(LOW, loose.renderY(1));
        }
        return Math.max(LOW, sortie.storyPickup().orElseThrow().renderY(1) - UNDER_CORE);
    }

    /** The story pickup in the pool; null while it rises or once it is gone. */
    private static Pickup loose(Sortie sortie) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.STORY) {
                return sortie.pickup(i);
            }
        }
        return null;
    }
}
