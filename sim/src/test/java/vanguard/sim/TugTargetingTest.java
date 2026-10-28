package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part D, step D1 (design/allies/mining-tug, Damaged by; design/campaign Level 31, What hurts the tug, Q-D2 = a):
 * only the hooked attacks hurt the tug, each on one section: a Hornet's missiles home on the nearest section and burst
 * in its box for 5 (shot down they do nothing), a hooked Dart's chase steers at the nearest section and its crossing
 * deals 3 once, a Shard Drone's lit link across a section deals 4 a touch with a 0.5 s cooldown per link and section;
 * an unhooked kind's fire and every plain shot pass over it.
 */
class TugTargetingTest {
    private static final int FIRE = Command.FIRE.bit();

    private static Sortie sortie(List<WaveSpec> waves, ObjectiveSpec.Escort escort, double seconds, Rules rules) {
        return new Sortie(
                11, TestSpecs.LOADOUT, Act5DSpecs.level(seconds, waves, escort), rules, TestSpecs.FULL_ARMOUR);
    }

    private static double lost(Sortie sortie, int k) {
        return sortie.ally(k).hitsTaken();
    }

    /** Flies {@code seconds} with {@code command}; returns the missiles that burst on a section. */
    private static int fly(Sortie sortie, double seconds, int command) {
        int onAlly = 0;
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(command);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MISSILE_DESTROYED && events.value(e) == Missiles.ON_ALLY) {
                    onAlly++;
                }
            }
        }
        return onAlly;
    }

    @Test
    void aHornetsSalvosHomeOnTheBowAndBurstThereForFive() {
        Sortie sortie = sortie(
                List.of(Act5DSpecs.hornets(7, 1)), Act5DSpecs.ESCORT, 30, TestSpecs.RULES.withInvulnerableShip());
        int bursts = fly(sortie, 20, 0);
        assertTrue(bursts >= 4, "a salvo burst on the tug: " + bursts);
        assertEquals(5.0 * bursts, lost(sortie, 0), 1e-9, "every missile on the bow, 5 each");
        assertEquals(0, lost(sortie, 1));
        assertEquals(0, lost(sortie, 2));
    }

    @Test
    void aHornetFiresSalvosOfFourAFifthOfASecondApart() {
        Sortie sortie = sortie(
                List.of(Act5DSpecs.hornets(7, 1)), Act5DSpecs.ESCORT, 30, TestSpecs.RULES.withInvulnerableShip());
        List<Integer> launches = new java.util.ArrayList<>();
        for (int i = 0; i < SimStep.ticks(22); i++) {
            sortie.step(0);
            for (int k = 0; k < sortie.events().count(SimEvents.Type.MISSILE_LAUNCHED); k++) {
                launches.add(i);
            }
        }
        assertTrue(launches.size() >= 8, "two salvos: " + launches);
        for (int k = 1; k < 4; k++) {
            assertEquals(SimStep.ticks(0.2), launches.get(k) - launches.get(k - 1), 1, "0.2 s apart: " + launches);
        }
        // A burst's interval counts from its last shot (every unit's burst): 5 s + 3 × 0.2 s salvo to salvo; D2 sets
        // the data's interval to 4.4 s for the stat block's "every 5 s".
        assertEquals(SimStep.ticks(5.6), launches.get(4) - launches.get(0), 1, "every 5.6 s: " + launches);
    }

    @Test
    void anUnhookedWavesHornetsLeaveTheTugAlone() {
        // Level 31's easy: only every second Hornet wave is hooked; here wave 0 is not, wave 1 is.
        ObjectiveSpec.Escort escort =
                Act5DSpecs.escort(Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 178, Act5DSpecs.HOOKED)
                        .withUnhookedWaves(List.of(0)));
        Sortie sortie = sortie(
                List.of(Act5DSpecs.hornets(7, 1), Act5DSpecs.hornets(19, 1)),
                escort,
                40,
                TestSpecs.RULES.withInvulnerableShip());
        assertEquals(0, fly(sortie, 12, 0), "the unhooked wave's salvos go for the ship");
        assertTrue(fly(sortie, 14, 0) >= 4, "the hooked wave's go for the bow");
    }

    @Test
    void anUnhookedHornetsMissilesGoForTheShipAndPassOverTheTug() {
        ObjectiveSpec.Escort escort = Act5DSpecs.escort(
                Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 178, Map.of("dart-drone", HookMode.ALWAYS)));
        Sortie sortie = sortie(List.of(Act5DSpecs.hornets(7, 1)), escort, 30, TestSpecs.RULES.withInvulnerableShip());
        assertEquals(0, fly(sortie, 20, 0));
        for (int k = 0; k < 3; k++) {
            assertEquals(0, lost(sortie, k));
        }
    }

    /** A rear stream of Darts climbing at x {@code x} before their chase. */
    private static WaveSpec rearDarts(double t, int count, double x) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.STREAM,
                Act5CSpecs.DART,
                count,
                WaveSpec.Entry.REAR,
                WaveSpec.Edge.LEFT,
                Optional.empty(),
                Optional.of(3.0),
                1,
                Optional.empty(),
                Optional.of(0.5),
                List.of(),
                Optional.empty(),
                List.of(List.of(new WaveSpec.At(x, 600), new WaveSpec.At(x, -60))),
                Optional.empty());
    }

    @Test
    void hookedDartsChaseTheNearestSectionAndHitItOnceEach() {
        // Climbing at x 50, the nearest section is the engines; at 60°/s the Darts swing round into the cargo or the
        // engines (which one is D2's tuning of the entry x and the turn, m9d-gaps.md §6).
        Sortie sortie =
                sortie(List.of(rearDarts(8, 4, 50)), Act5DSpecs.ESCORT, 30, TestSpecs.RULES.withInvulnerableShip());
        int firstHit = -1;
        int firstEntry = -1;
        int firstSection = -1;
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(0);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy dart = sortie.enemy(j);
                if (firstEntry < 0 && dart.y() >= 0) {
                    firstEntry = i;
                }
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ALLY_HIT && firstHit < 0) {
                    firstHit = i;
                    firstSection = events.value(e);
                }
            }
        }
        assertTrue(firstHit > 0, "a Dart crossed the engines");
        assertTrue(firstSection == 1 || firstSection == 2, "from below: " + firstSection);
        double lost = lost(sortie, 0) + lost(sortie, 1) + lost(sortie, 2);
        assertTrue(lost > 0 && lost <= 4 * 3 && lost % 3 == 0, "3 a Dart, once each: " + lost);
        double seconds = (firstHit - firstEntry) * SimStep.SECONDS;
        assertTrue(seconds >= 0.8, "a rear Dart entering at x 50 crosses the engines after 0.8 s or more: " + seconds);
    }

    @Test
    void unhookedDartsChaseTheShip() {
        ObjectiveSpec.Escort escort = Act5DSpecs.escort(
                Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 178, Map.of("hornet", HookMode.ALWAYS)));
        Sortie sortie = sortie(List.of(rearDarts(8, 4, 90)), escort, 30, TestSpecs.RULES.withInvulnerableShip());
        fly(sortie, 14, 0);
        for (int k = 0; k < 3; k++) {
            assertEquals(0, lost(sortie, k));
        }
    }

    @Test
    void aShardLinkSweepingTheHullTouchesEachSectionForFourEveryHalfSecondAtMost() {
        // Two drones 120 px apart: one link across the hull's middle, over every section in turn.
        Sortie sortie = sortie(
                List.of(Act5DSpecs.shardLine(7, 2)), Act5DSpecs.ESCORT, 40, TestSpecs.RULES.withInvulnerableShip());
        int[] last = {-1_000, -1_000, -1_000};
        int[] touches = new int[3];
        int cooldown = SimStep.ticks(0.5);
        for (int i = 0; i < SimStep.ticks(25); i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) != SimEvents.Type.AIR_LINK_TOUCH || events.value(e) < AirLinks.TOUCH_SECTION) {
                    continue;
                }
                int k = events.value(e) - AirLinks.TOUCH_SECTION;
                assertTrue(i - last[k] >= cooldown, "a link touches a section once a cooldown: " + (i - last[k]));
                last[k] = i;
                touches[k]++;
            }
        }
        for (int k = 0; k < 3; k++) {
            assertTrue(touches[k] >= 2, "the link crossed section " + k + ": " + touches[k]);
            assertEquals(4.0 * touches[k], lost(sortie, k), 1e-9, "4 a touch");
        }
    }

    @Test
    void plainShotsAndUnhookedFirePassOverTheTug() {
        EnemySpec gunner = EnemySpec.of("gunner", 500, new Hitbox(30, 30), Layer.AIR, 0, false, 1, 140)
                .withHover(Optional.of(new EnemySpec.Hover(new Range(12, 12), new Range(80, 80))))
                .withGun(Optional.of(EnemyGun.aimed(0.5, 0.2, 1, 200, 4, false)));
        WaveSpec wave = Act5DSpecs.wave(
                7,
                WaveSpec.Formation.LINE_ABREAST,
                gunner,
                2,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty());
        Sortie sortie = sortie(List.of(wave), Act5DSpecs.ESCORT, 30, TestSpecs.RULES);
        fly(sortie, 15, 0);
        assertTrue(sortie.ship().defences().armourLost() > 0, "the shots crossed the hull and hit the ship");
        for (int k = 0; k < 3; k++) {
            assertEquals(0, lost(sortie, k), "nothing unhooked hurts the tug");
        }
    }
}
