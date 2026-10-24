package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.SimSpecs;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.sim.Armament;
import vanguard.sim.Beam;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * The hangar's test fire (design/ui/hangar, Test fire): every weapon the simulation flies, in every
 * slot it fits, at every level, loops without errors and hits a dummy where that weapon should: a
 * forward gun ahead of the ship, a rear gun behind it, a side gun beside it (both sides), a bomb at
 * or behind the pod on its line, a mortar shell well ahead, a homing missile or a turret's shot
 * anywhere (a turret's on the dummies either side of its line), a mine's blast behind the ship, a
 * torpedo on the submerged dummies ahead; M7 part A: the Ion Beam burns a dummy ahead, the Light Drone
 * Bay's drone hits them ahead, and a Deflector Pod (which deals no damage) catches the dummies' slow
 * bullets on its side. The loop is deterministic and starts over, and stepping it allocates nothing.
 */
class TestFireTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /** Two whole loops and a bit, climbs included. */
    private static final int STEPS = 2 * (TestFire.CLIMB_TICKS + TestFire.MAX_LOOP_TICKS) + 60;
    /** Half the hull: a hit beyond it is ahead of, behind or beside the ship. */
    private static final double HULL = 24;

    /** Where a hit landed. */
    private record Hit(double x, double y) {}

    static Stream<TestFire.Shown> shown() {
        List<TestFire.Shown> all = new ArrayList<>();
        for (var entry : CONTENT.weapons().entrySet()) {
            if (!SimSpecs.flies(CONTENT, entry.getKey()) || entry.getValue().deflector()) {
                // A Deflector Pod hits nothing: its own test.
                continue;
            }
            List<Armament.Slot> slots =
                    switch (entry.getValue().slot()) {
                        case FRONT -> List.of(Armament.Slot.FRONT);
                        case REAR -> List.of(Armament.Slot.REAR);
                        case WING -> List.of(Armament.Slot.LEFT_WING, Armament.Slot.RIGHT_WING);
                    };
            for (Armament.Slot slot : slots) {
                for (int level = 1; level <= entry.getValue().levels().size(); level++) {
                    all.add(new TestFire.Shown(entry.getKey(), slot, level));
                }
            }
        }
        return all.stream();
    }

    @ParameterizedTest
    @MethodSource("shown")
    void everyWeaponHitsADummyWhereItShould(TestFire.Shown shown) {
        TestFire fire = new TestFire(CONTENT, shown);
        Sortie sortie = fire.sortie();
        WeaponSpec weapon = sortie.armament().mount(0).weapon();
        List<Hit> hits = new ArrayList<>();
        int restarts = 0;
        for (int i = 0; i < STEPS; i++) {
            restarts += fire.step() ? 1 : 0;
            collect(sortie, weapon, hits);
        }
        assertTrue(restarts >= 1, shown + ": the loop never started over");
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        assertEquals(TestFire.SHIP_Y, shipY, 8, shown + ": the ship holds the middle of the field");
        assertFalse(hits.isEmpty(), shown + ": no dummy was hit");
        String where = shown + " hits " + hits + " (ship at " + shipX + ", " + shipY + ")";
        switch (weapon.delivery()) {
            case BOLT -> {
                double angle = Math.toDegrees(
                        Math.abs(Math.IEEEremainder(weapon.muzzles().getFirst().angle(), 2 * Math.PI)));
                if (angle <= 45) {
                    assertTrue(hits.stream().anyMatch(hit -> hit.y() > shipY + HULL), "ahead: " + where);
                } else if (angle >= 135) {
                    assertTrue(hits.stream().anyMatch(hit -> hit.y() < shipY - HULL), "behind: " + where);
                } else {
                    // Beside the hull, on both sides (the side guns fire to both).
                    assertTrue(
                            hits.stream().anyMatch(hit -> beside(hit, shipX, shipY) && hit.x() < shipX),
                            "left: " + where);
                    assertTrue(
                            hits.stream().anyMatch(hit -> beside(hit, shipX, shipY) && hit.x() > shipX),
                            "right: " + where);
                }
            }
            case HOMING -> {}
            case TURRET -> {
                // The turret swings to the dummies either side of its line, not only along it.
                double line = shipX + TestFire.line(shown.slot());
                assertTrue(
                        hits.stream().anyMatch(hit -> hit.x() < line - TestFire.TARGET_SIZE),
                        "left of its line: " + where);
                assertTrue(
                        hits.stream().anyMatch(hit -> hit.x() > line + TestFire.TARGET_SIZE),
                        "right of its line: " + where);
            }
            case MINE -> assertTrue(hits.stream().anyMatch(hit -> hit.y() < shipY - HULL), "behind: " + where);
            case DROPPED -> {
                double line = shipX + TestFire.line(shown.slot());
                assertTrue(
                        hits.stream()
                                .anyMatch(hit -> hit.y() <= shipY && Math.abs(hit.x() - line) < TestFire.TARGET_SIZE),
                        "under the pod: " + where);
            }
            case LOBBED -> assertTrue(hits.stream().anyMatch(hit -> hit.y() > shipY + 100), "well ahead: " + where);
            // M5 part E: over the test range's water, on the submerged dummies ahead.
            case TORPEDO -> assertTrue(hits.stream().anyMatch(hit -> hit.y() > shipY + HULL), "ahead: " + where);
            // M7 part A: the column burns the dummy on its line ahead.
            case BEAM -> assertTrue(hits.stream().anyMatch(hit -> hit.y() > shipY + HULL), "ahead: " + where);
            // M8 AW: over the test range's water, the drums tossed forward burst among the submerged dummies ahead.
            case DEPTH_CHARGE -> assertTrue(hits.stream().anyMatch(hit -> hit.y() > shipY + HULL), "ahead: " + where);
        }
    }

    private static boolean beside(Hit hit, double shipX, double shipY) {
        return Math.abs(hit.y() - shipY) <= HULL && Math.abs(hit.x() - shipX) > HULL;
    }

    /** This step's hits: a shot on a dummy, a blast that reaches one, a dummy destroyed by a blast, a beam's burn. */
    private static void collect(Sortie sortie, WeaponSpec weapon, List<Hit> hits) {
        if (weapon.delivery() == WeaponSpec.Delivery.BEAM) {
            Beam beam = sortie.beam(0);
            if (beam.on() && beam.contact() == Beam.Contact.BURN) {
                hits.add(new Hit(beam.x(), beam.endY()));
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            switch (events.type(i)) {
                case ENEMY_HIT -> hits.add(new Hit(x, y));
                // A mine bursts only for a dummy: where it bursts counts.
                case BLAST -> {
                    if (weapon.delivery() == WeaponSpec.Delivery.MINE) {
                        hits.add(new Hit(x, y));
                    }
                    for (int j = 0; j < sortie.enemyCount(); j++) {
                        Enemy dummy = sortie.enemy(j);
                        double dx = Math.max(
                                Math.abs(x - dummy.renderX(1)) - dummy.hitbox().width() / 2, 0);
                        double dy = Math.max(
                                Math.abs(y - dummy.renderY(1)) - dummy.hitbox().height() / 2, 0);
                        if (dx * dx + dy * dy <= weapon.blast() * weapon.blast()) {
                            hits.add(new Hit(x, y));
                        }
                    }
                }
                case ENEMY_DESTROYED -> {
                    // M8 part A: a depth charge's burst destroys a dummy outright, so it is gone before it is seen.
                    if (weapon.delivery().landing() || weapon.delivery() == WeaponSpec.Delivery.DEPTH_CHARGE) {
                        hits.add(new Hit(x, y));
                    }
                }
                default -> {}
            }
        }
    }

    static Stream<TestFire.Shown> deflectors() {
        List<TestFire.Shown> all = new ArrayList<>();
        for (Armament.Slot slot : List.of(Armament.Slot.LEFT_WING, Armament.Slot.RIGHT_WING)) {
            for (int level = 1; level <= 5; level++) {
                all.add(new TestFire.Shown("deflector-pod", slot, level));
            }
        }
        return all.stream();
    }

    /**
     * M7 part A: a Deflector Pod's loop: the dummies fire slow bullets at the ship and its field catches
     * some on its side; nothing hits a dummy, and the loop still starts over.
     */
    @ParameterizedTest
    @MethodSource("deflectors")
    void theDeflectorCatchesTheDummiesBulletsOnItsSide(TestFire.Shown shown) {
        TestFire fire = new TestFire(CONTENT, shown);
        Sortie sortie = fire.sortie();
        assertTrue(sortie.armament().mount(0).deflects());
        int restarts = 0;
        int caught = 0;
        int fired = 0;
        for (int i = 0; i < STEPS; i++) {
            restarts += fire.step() ? 1 : 0;
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case DEFLECTOR_CAUGHT -> {
                        caught++;
                        double side = events.x(e) - sortie.ship().x();
                        assertTrue(
                                shown.slot() == Armament.Slot.LEFT_WING ? side <= 0 : side >= 0,
                                shown + ": caught on the other side at " + side);
                    }
                    case ENEMY_HIT -> throw new AssertionError(shown + ": a deflector hit a dummy");
                    default -> {}
                }
            }
            fired = Math.max(fired, sortie.bulletCount());
        }
        assertTrue(restarts >= 1, shown + ": the loop never started over");
        assertTrue(fired > 0, shown + ": the dummies never fired");
        assertTrue(caught > 0, shown + ": the field caught nothing");
    }

    @Test
    void theLoopIsDeterministic() {
        TestFire.Shown shown = new TestFire.Shown("scatter-vulcan", Armament.Slot.FRONT, 3);
        TestFire first = new TestFire(CONTENT, shown);
        TestFire second = new TestFire(CONTENT, shown);
        for (int i = 0; i < STEPS; i++) {
            assertEquals(first.step(), second.step());
            assertEquals(first.sortie().stateHash(), second.sortie().stateHash(), "step " + i);
        }
    }

    @Test
    void steppingTheLoopAllocatesNothing() {
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        // As the sim's Allocations: the least of several runs, so the JVM's one-off allocations drop out.
        // M7 part A: the beam, the drone bay and the deflector's firing dummies too.
        for (TestFire.Shown shown : List.of(
                new TestFire.Shown("micro-missile-pod", Armament.Slot.LEFT_WING, 2),
                new TestFire.Shown("ion-beam", Armament.Slot.FRONT, 2),
                new TestFire.Shown("light-drone-bay", Armament.Slot.RIGHT_WING, 2),
                new TestFire.Shown("deflector-pod", Armament.Slot.LEFT_WING, 2))) {
            long least = Long.MAX_VALUE;
            for (int run = 0; run < 10 && least > 0; run++) {
                TestFire fire = new TestFire(CONTENT, shown);
                long before = threads.getCurrentThreadAllocatedBytes();
                for (int i = 0; i < STEPS; i++) {
                    fire.step();
                }
                least = Math.min(least, threads.getCurrentThreadAllocatedBytes() - before);
            }
            assertEquals(0, least, shown + ": two loops allocated " + least + " bytes");
        }
    }

    @Test
    void onlyAWeaponTheSimulationFliesIsShownInItsSlotAtALevelItHas() {
        assertEquals(
                Optional.of(new TestFire.Shown("bomb-rack", Armament.Slot.RIGHT_WING, 2)),
                TestFire.of(CONTENT, LoadoutSlot.RIGHT_WING, "bomb-rack", 2));
        assertEquals(
                5,
                TestFire.of(CONTENT, LoadoutSlot.FRONT, "pulse-cannon", 6)
                        .orElseThrow()
                        .level());
        assertEquals(
                Optional.of(new TestFire.Shown("proximity-mines", Armament.Slot.REAR, 1)),
                TestFire.of(CONTENT, LoadoutSlot.REAR, "proximity-mines", 1));
        assertEquals(
                Optional.of(new TestFire.Shown("torpedo-pod", Armament.Slot.LEFT_WING, 1)),
                TestFire.of(CONTENT, LoadoutSlot.LEFT_WING, "torpedo-pod", 1));
        assertEquals(Optional.empty(), TestFire.of(CONTENT, LoadoutSlot.GENERATOR, "Mk I", 1));
        // M7 part A: the Act 3 beam, drone bay and deflector have their loops.
        assertEquals(
                Optional.of(new TestFire.Shown("ion-beam", Armament.Slot.FRONT, 3)),
                TestFire.of(CONTENT, LoadoutSlot.FRONT, "ion-beam", 3));
        assertEquals(
                Optional.of(new TestFire.Shown("light-drone-bay", Armament.Slot.LEFT_WING, 1)),
                TestFire.of(CONTENT, LoadoutSlot.LEFT_WING, "light-drone-bay", 1));
        assertEquals(
                Optional.of(new TestFire.Shown("deflector-pod", Armament.Slot.RIGHT_WING, 5)),
                TestFire.of(CONTENT, LoadoutSlot.RIGHT_WING, "deflector-pod", 5));
    }
}
