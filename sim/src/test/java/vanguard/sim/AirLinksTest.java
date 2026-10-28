package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part D, step D1 (design/enemies, Air links; design/enemies/space/shard-drone): a wave of air-linked units is one
 * group, linked in order (a ring closed); the links follow their units, light after the tell once the group is in,
 * drop under the EMP's stun and relight after the tell, break when an end dies, touch the ship with a cooldown and never
 * Rook; a line flies its spacing apart, a ring's centre descends and drifts toward the ship.
 */
class AirLinksTest {
    private static LevelScript level(List<WaveSpec> waves) {
        return LevelScript.builder(31, 5)
                .sections(List.of(new Section(40, 120)))
                .waves(waves)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    private static Sortie sortie(List<WaveSpec> waves, Rules rules) {
        return new Sortie(5, TestSpecs.LOADOUT, level(waves), rules, TestSpecs.FULL_ARMOUR);
    }

    private static int shown(AirLinks links) {
        int n = 0;
        for (int slot = 0; slot < AirLinks.SLOTS; slot++) {
            n += links.shown(slot) ? 1 : 0;
        }
        return n;
    }

    private static int lit(AirLinks links) {
        int n = 0;
        for (int slot = 0; slot < AirLinks.SLOTS; slot++) {
            n += links.lit(slot) ? 1 : 0;
        }
        return n;
    }

    private static Enemy unit(Sortie sortie, int place) {
        int seen = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy u = sortie.enemy(i);
            if (u.spec().airLink().isPresent()) {
                // The pool keeps them in their wave's order.
                if (seen++ == place) {
                    return u;
                }
            }
        }
        throw new AssertionError("no unit " + place);
    }

    private static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }

    @Test
    void aLineIsLinkedInOrderSpacedAndLitAfterItsTell() {
        Sortie sortie = sortie(List.of(Act5DSpecs.shardLine(1, 4)), TestSpecs.RULES.withInvulnerableShip());
        int enteredAll = -1;
        int litAt = -1;
        for (int i = 0; i < SimStep.ticks(6) && litAt < 0; i++) {
            sortie.step(0);
            if (enteredAll < 0 && sortie.enemyCount() == 4) {
                boolean all = true;
                for (int k = 0; k < 4; k++) {
                    Enemy u = unit(sortie, k);
                    all &= PlayField.overlaps(u.x(), u.y(), u.hitbox());
                }
                enteredAll = all ? i : -1;
            }
            if (count(sortie, SimEvents.Type.AIR_LINK_LIT) > 0) {
                litAt = i;
                assertEquals(3, count(sortie, SimEvents.Type.AIR_LINK_LIT), "a line of 4: 3 links");
            }
        }
        AirLinks links = sortie.airLinks();
        assertEquals(3, shown(links));
        assertEquals(3, lit(links));
        assertEquals(SimStep.ticks(0.8), litAt - enteredAll, 1, "lit 0.8 s after the last drone is in");
        for (int k = 0; k < 3; k++) {
            assertEquals(unit(sortie, k).x(), links.ax(k), 1e-9, "the ends follow their drones");
            assertEquals(unit(sortie, k + 1).x(), links.bx(k), 1e-9);
            assertEquals(120, unit(sortie, k + 1).x() - unit(sortie, k).x(), 1e-6, "120 px apart");
        }
        assertEquals(240, (unit(sortie, 0).x() + unit(sortie, 3).x()) / 2, 30, "centred, swaying ±30 px");
    }

    @Test
    void killingADroneBreaksItsLinks() {
        Sortie sortie = sortie(List.of(Act5DSpecs.shardLine(1, 4)), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(0);
        }
        assertEquals(3, lit(sortie.airLinks()));
        Enemy second = unit(sortie, 1);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == second) {
                sortie.destroyEnemy(i);
                break;
            }
        }
        assertEquals(2, count(sortie, SimEvents.Type.AIR_LINK_BROKEN), "the middle drone's two links snap");
        sortie.step(0);
        assertEquals(1, lit(sortie.airLinks()), "the other end's link stands");
        Enemy last = unit(sortie, 2);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == last) {
                sortie.destroyEnemy(i);
                break;
            }
        }
        sortie.step(0);
        assertEquals(0, shown(sortie.airLinks()), "no link left: the group is gone");
    }

    @Test
    void theEmpDropsTheLinksForItsStunThenTheyRelightAfterTheTell() {
        Sortie sortie = sortie(List.of(Act5DSpecs.shardLine(1, 3)), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(0);
        }
        assertEquals(2, lit(sortie.airLinks()));
        int stun = SimStep.ticks(3);
        for (int k = 0; k < 3; k++) {
            unit(sortie, k).empStun(stun);
        }
        sortie.step(0);
        assertEquals(2, count(sortie, SimEvents.Type.AIR_LINK_DROPPED));
        assertEquals(0, lit(sortie.airLinks()));
        int relit = -1;
        for (int i = 1; i < SimStep.ticks(6) && relit < 0; i++) {
            sortie.step(0);
            if (lit(sortie.airLinks()) == 2) {
                relit = i;
            }
        }
        assertEquals(stun + SimStep.ticks(0.8), relit, 2, "dark for the stun, then the tell");
    }

    @Test
    void aLitLinkTouchesTheShipOnceACooldownAndRookNever() {
        Loadout loadout = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        WaveSpec quiet = Act5DSpecs.wave(
                1,
                WaveSpec.Formation.LINE_ABREAST,
                Act5DSpecs.SHARD.withGun(Optional.empty()),
                4,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty());
        Sortie sortie = new Sortie(5, loadout, level(List.of(quiet)), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int touches = 0;
        int last = -1_000;
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.AIR_LINK_TOUCH) {
                    assertEquals(AirLinks.TOUCH_SHIP, events.value(e), "only the ship");
                    assertTrue(i - last >= SimStep.ticks(0.5), "once a cooldown");
                    last = i;
                    touches++;
                }
            }
        }
        assertTrue(touches >= 1, "the line swept over the ship: " + touches);
    }

    @Test
    void aRingIsClosedAndItsCentreDescendsTowardTheShip() {
        Sortie sortie = sortie(List.of(Act5DSpecs.shardRing(1)), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(9); i++) {
            sortie.step(Command.LEFT.bit());
        }
        AirLinks links = sortie.airLinks();
        assertEquals(4, shown(links), "a ring of 4: 4 links");
        double cx = 0;
        double cy = 0;
        for (int k = 0; k < 4; k++) {
            cx += unit(sortie, k).x() / 4;
            cy += unit(sortie, k).y() / 4;
        }
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.LEFT.bit());
        }
        double cx2 = 0;
        double cy2 = 0;
        for (int k = 0; k < 4; k++) {
            cx2 += unit(sortie, k).x() / 4;
            cy2 += unit(sortie, k).y() / 4;
        }
        assertEquals(-80, cy2 - cy, 2, "40 px/s down");
        assertEquals(-20, cx2 - cx, 2, "10 px/s toward the ship on the left");
        double radius = Math.hypot(unit(sortie, 0).x() - cx2, unit(sortie, 0).y() - cy2);
        assertEquals(64, radius, 1, "still a ring of 64 px");
        // It never breaks off: it leaves through the bottom edge.
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(0);
        }
        assertEquals(0, sortie.enemyCount(), "gone past the bottom edge");
        assertFalse(sortie.airLinks().shown(0));
    }

    @Test
    void anAirLinkJoinsPlainAirUnits() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5DSpecs.SHARD.withDash(Optional.of(Act5DSpecs.DASH)),
                "not with a dash");
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.AirLink(6, 6, 0, 0.8, 120, 0, 0),
                "a touch has a cooldown");
    }

    @Test
    void itReplays() {
        Sortie one = sortie(
                List.of(Act5DSpecs.shardLine(1, 4), Act5DSpecs.shardRing(6)), TestSpecs.RULES.withInvulnerableShip());
        Sortie two = sortie(
                List.of(Act5DSpecs.shardLine(1, 4), Act5DSpecs.shardRing(6)), TestSpecs.RULES.withInvulnerableShip());
        for (Sortie sortie : List.of(one, two)) {
            for (int i = 0; i < SimStep.ticks(14); i++) {
                sortie.step(Command.FIRE.bit());
            }
        }
        assertEquals(one.stateHash(), two.stateHash());
    }
}
