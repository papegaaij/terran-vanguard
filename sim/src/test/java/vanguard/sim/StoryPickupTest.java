package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part G, step G1 (design/campaign Level 34, The freighter; m9g-gaps.md §1 #4, QG-2 a): the hold's four clamps are a
 * destructible group whose drop is a story pickup: the last clamp frees it at the clamps' centre, it rises for 1 s
 * untouchable, then drifts as a pickup until the ship flies through its 60 × 40 box (collected) or it leaves or runs out
 * (missed); it pays nothing, is never a secret, and cues its own lines.
 */
class StoryPickupTest {
    private static final int FIRE = Command.FIRE.bit();

    private static final RadioCue COLLECTED_LINE =
            new RadioCue(CueTrigger.STORY_COLLECTED, 0, "", "Okafor", "Helix core aboard.", false, "neutral");
    private static final RadioCue MISSED_LINE =
            new RadioCue(CueTrigger.STORY_MISSED, 0, "", "Okafor", "Salvage tug has the core.", false, "neutral");

    /** A hold clamp at {@code x}, entering at 1 s: 15 HP, no bounty, one of the four whose last frees the core. */
    static GroundObjectSpec clamp(double x) {
        return new GroundObjectSpec(
                1.0,
                x,
                new Hitbox(16, 16),
                15,
                0,
                Optional.empty(),
                0,
                0,
                "",
                false,
                -1,
                1,
                Optional.empty(),
                "hold-clamp",
                false,
                Optional.empty(),
                false,
                0,
                4,
                Optional.of(PickupType.STORY),
                false,
                0,
                0);
    }

    /** Level 34's hold in small: 130 px/s for 30 s, the four clamps at 1 s round x 240, both core lines. */
    static LevelScript hold() {
        return LevelScript.builder(34, 5)
                .sections(List.of(new Section(30, 130)))
                .groundObjects(List.of(clamp(200), clamp(220), clamp(260), clamp(280)))
                .radio(List.of(COLLECTED_LINE, MISSED_LINE))
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    static Sortie sortie() {
        return new Sortie(
                1,
                Act5ESpecs.loadout(Act5ESpecs.bolt(20, false)),
                hold(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** The commands that move the ship toward ({@code x}, {@code y}) (y NaN: across only), firing or not. */
    static int toward(Sortie sortie, double x, double y, boolean fire) {
        Ship ship = sortie.ship();
        int command = fire ? FIRE : 0;
        if (ship.x() < x - 3) {
            command |= Command.RIGHT.bit();
        } else if (ship.x() > x + 3) {
            command |= Command.LEFT.bit();
        }
        if (!Double.isNaN(y)) {
            if (ship.y() < y - 3) {
                command |= Command.UP.bit();
            } else if (ship.y() > y + 3) {
                command |= Command.DOWN.bit();
            }
        }
        return command;
    }

    /** The lowest clamp still standing; null once all fell. */
    static GroundObject lowestClamp(Sortie sortie) {
        GroundObject lowest = null;
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (lowest == null || object.renderY(1) < lowest.renderY(1)) {
                lowest = object;
            }
        }
        return lowest;
    }

    /** The story pickup in the pool; null while none. */
    static Pickup storyPickup(Sortie sortie) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.STORY) {
                return sortie.pickup(i);
            }
        }
        return null;
    }

    /** Shoots the clamps down one by one; returns the y the clamps stood at in the step before the last fell. */
    static double freeTheCore(Sortie sortie) {
        double lastY = Double.NaN;
        for (int step = 0; step < SimStep.ticks(8); step++) {
            GroundObject clamp = lowestClamp(sortie);
            if (clamp != null) {
                lastY = clamp.renderY(1);
            }
            sortie.step(clamp == null ? 0 : toward(sortie, clamp.renderX(), Double.NaN, true));
            if (sortie.events().count(SimEvents.Type.STORY_RISE) > 0) {
                return lastY;
            }
        }
        throw new AssertionError("the core never rose");
    }

    private static int radioIndex(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.RADIO) {
                return events.value(e);
            }
        }
        return -1;
    }

    @Test
    void theLastClampFreesItAtTheClampsCentre_risingUntouchableForASecond() {
        Sortie sortie = sortie();
        StoryPickups story = sortie.storyPickup().orElseThrow();
        assertEquals(StoryPickups.State.WAITING, story.state());
        double clampsY = freeTheCore(sortie);
        assertEquals(StoryPickups.State.RISING, story.state());
        assertEquals(240, story.renderX(), 1e-9, "the four clamps' centre across");
        assertEquals(clampsY - 130 * SimStep.SECONDS, story.renderY(1), 0.5, "where the clamps stood");
        assertEquals(0, story.riseShare(0), 1e-9);
        assertEquals(0, sortie.groundObjectCount(), "the clamps are down");
        for (int step = 1; step < StoryPickups.RISE_TICKS; step++) {
            double y = story.renderY(1);
            sortie.step(0);
            assertTrue(story.rising(), "rising for its second");
            assertNull(storyPickup(sortie), "untouchable while it rises: not a pickup yet");
            assertEquals(y - 130 * SimStep.SECONDS, story.renderY(1), 1e-6, "it rises with the ground");
        }
        sortie.step(0);
        assertEquals(StoryPickups.State.LOOSE, story.state());
        assertEquals(1, story.riseShare(0), 1e-9);
        Pickup core = storyPickup(sortie);
        assertNotNull(core, "loose: a story pickup drifting");
        assertEquals(0, core.credits(), "it pays nothing");
    }

    @Test
    void flyingThroughItCollectsIt_payingNothing_noSecret_withItsLine() {
        Sortie sortie = sortie();
        freeTheCore(sortie);
        int credits = sortie.credits();
        boolean collected = false;
        int line = -1;
        for (int step = 0; step < SimStep.ticks(8) && !collected; step++) {
            Pickup core = storyPickup(sortie);
            sortie.step(core == null ? 0 : toward(sortie, core.renderX(), core.renderY(1), false));
            collected = sortie.events().count(SimEvents.Type.STORY_COLLECTED) > 0;
            if (collected) {
                assertEquals(1, sortie.events().count(SimEvents.Type.PICKUP_COLLECTED));
                line = radioIndex(sortie);
            }
        }
        assertTrue(collected, "the ship flew through it");
        StoryPickups story = sortie.storyPickup().orElseThrow();
        assertTrue(story.collected());
        assertFalse(story.missed());
        assertEquals(0, line, "Okafor's collected line");
        assertEquals(credits, sortie.credits(), "no credits");
        assertEquals(0, sortie.result().secretsFound(), "not a secret");
        assertNull(storyPickup(sortie));
        for (int step = 0; step < SimStep.ticks(8); step++) {
            sortie.step(0);
            assertEquals(0, sortie.events().count(SimEvents.Type.STORY_MISSED), "collected is never missed");
        }
    }

    @Test
    void leftAloneItIsMissed_theTugsLine() {
        Sortie sortie = sortie();
        freeTheCore(sortie);
        int line = -1;
        boolean missed = false;
        for (int step = 0; step < SimStep.ticks(12) && !missed; step++) {
            sortie.step(toward(sortie, 30, Double.NaN, false));
            missed = sortie.events().count(SimEvents.Type.STORY_MISSED) > 0;
            if (missed) {
                line = radioIndex(sortie);
            }
        }
        assertTrue(missed, "it drifted off or ran out");
        assertTrue(sortie.storyPickup().orElseThrow().missed());
        assertEquals(1, line, "the tug's line");
        assertNull(storyPickup(sortie));
        assertEquals(0, sortie.events().count(SimEvents.Type.STORY_COLLECTED));
    }

    /**
     * Step FIX2 (pending round 67): clamps never shot leave the bottom edge with the core never released: the last one
     * gone misses it once (the tug's line), as a released core that leaves; the level still runs to its end.
     */
    @Test
    void clampsNeverShotMissItOnceAsTheLastLeaves() {
        Sortie sortie = sortie();
        StoryPickups story = sortie.storyPickup().orElseThrow();
        boolean seen = false;
        int line = -1;
        for (int step = 0; step < SimStep.ticks(29) && !sortie.complete(); step++) {
            int standing = sortie.groundObjectCount();
            seen |= standing == 4;
            sortie.step(toward(sortie, 30, Double.NaN, false));
            if (sortie.events().count(SimEvents.Type.STORY_MISSED) > 0) {
                assertEquals(-1, line, "once");
                assertTrue(seen, "after the clamps came");
                assertEquals(0, sortie.groundObjectCount(), "as the last clamp leaves, not before");
                assertTrue(standing > 0);
                line = radioIndex(sortie);
            }
        }
        assertEquals(1, line, "the tug's line");
        assertTrue(story.missed());
        assertEquals(0, sortie.events().count(SimEvents.Type.STORY_RISE), "never released");
        assertNull(storyPickup(sortie));
        for (int step = 0; step < SimStep.ticks(4) && !sortie.complete(); step++) {
            sortie.step(0);
            assertEquals(0, sortie.events().count(SimEvents.Type.STORY_MISSED), "no second miss");
        }
        assertTrue(sortie.complete(), "the level runs to its end");
    }

    /** Step FIX2: the level ends with the clamps still standing: the core is missed then, once. */
    @Test
    void theLevelEndingWithTheClampsStandingMissesIt() {
        LevelScript shortHold = LevelScript.builder(34, 5)
                .sections(List.of(new Section(3, 130)))
                .groundObjects(List.of(clamp(200), clamp(220), clamp(260), clamp(280)))
                .radio(List.of(COLLECTED_LINE, MISSED_LINE))
                .objectives(ObjectiveSpec.NONE)
                .build();
        Sortie sortie = new Sortie(
                1,
                Act5ESpecs.loadout(Act5ESpecs.bolt(20, false)),
                shortHold,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        int missed = 0;
        for (int step = 0; step < SimStep.ticks(6) && !sortie.complete(); step++) {
            sortie.step(toward(sortie, 30, Double.NaN, false));
            missed += sortie.events().count(SimEvents.Type.STORY_MISSED);
        }
        assertTrue(sortie.complete());
        assertTrue(sortie.groundObjectCount() > 0, "the clamps still stand at the end");
        assertEquals(1, missed, "missed once, at the end");
        assertTrue(sortie.storyPickup().orElseThrow().missed());
    }

    @Test
    void theShipTouchesItsBoxNotJustItsCentre() {
        double radius = 36;
        assertTrue(StoryPickups.touches(240, 300, 240 + 30 + radius - 1, 300, radius), "beside the box");
        assertFalse(StoryPickups.touches(240, 300, 240 + 30 + radius + 1, 300, radius));
        assertTrue(StoryPickups.touches(240, 300, 240, 300 - 20 - radius + 1, radius), "under the box");
        assertFalse(StoryPickups.touches(240, 300, 240, 300 - 20 - radius - 1, radius));
    }

    @Test
    void itReplaysAndALevelWithoutOneHasNone() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = sortie();
            freeTheCore(sortie);
            for (int step = 0; step < SimStep.ticks(3); step++) {
                sortie.step(Command.UP.bit());
            }
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1]);
        Sortie plain = new Sortie(
                1,
                TestSpecs.LOADOUT,
                Act5ESpecs.slow(List.of(), List.of(), List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        assertTrue(plain.storyPickup().isEmpty());
    }

    @Test
    void onlyAGroupDropsIt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GroundObjectSpec(
                        1, 240, new Hitbox(16, 16), 15, 0, Optional.of(PickupType.STORY), 0, 0, "", false, -1, 1));
    }
}
