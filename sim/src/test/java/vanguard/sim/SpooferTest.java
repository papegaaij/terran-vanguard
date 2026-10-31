package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/player/systems, the IFF spoofer; the turret side): while the ship carries the module
 * every aimed volley of a spoofable unit fires 0.5 s late, aimed where the ship was when it came due; its cycle is
 * unchanged and a burst's later shots keep their spacing after the delayed first; a told attack ({@code laser-line})
 * starts its tell 0.5 s late, the tell keeping its length. A unit that is not spoofable, and every unit without the
 * module, fires as before.
 */
class SpooferTest {
    private static final double TURRET_X = Ship.START_X + 100;

    private static Sortie sortie(EnemySpec turret, double delay) {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, 40, List.of(new GroundUnit(0, TURRET_X, turret, -1)), List.of()));
        sortie.force.spoof(delay);
        return sortie;
    }

    /** The steps (from 1) of {@code type}'s events over {@code seconds} of a still ship. */
    private static List<Integer> times(Sortie sortie, SimEvents.Type type, double seconds) {
        List<Integer> times = new ArrayList<>();
        for (int i = 1; i <= SimStep.ticks(seconds); i++) {
            sortie.step(Command.NONE);
            for (int k = 0; k < sortie.events().count(type); k++) {
                times.add(i);
            }
        }
        return times;
    }

    @Test
    void eachVolleyFiresHalfASecondLateOnAnUnchangedCycle() {
        EnemySpec burst = Act6ASpecs.TURRET.withGun(java.util.Optional.of(EnemyGun.aimed(2.0, 0.5, 3, 160, 6, false)));
        List<Integer> plain = times(sortie(burst, 0), SimEvents.Type.ENEMY_FIRED, 7);
        Sortie spoofed = sortie(burst, 0.5);
        List<Integer> late = times(spoofed, SimEvents.Type.ENEMY_FIRED, 7);
        assertTrue(plain.size() >= 6, "two bursts of 3 at least: " + plain);
        for (int k = 0; k < Math.min(plain.size(), late.size()); k++) {
            assertEquals(plain.get(k) + SimStep.ticks(0.5), late.get(k), "shot " + k + ": " + plain + " / " + late);
        }
        List<Integer> hesitations = times(sortie(burst, 0.5), SimEvents.Type.SPOOF_HESITATE, 7);
        assertEquals(plain.subList(0, hesitations.size()), hesitations, "the stutter as each shot came due");
    }

    @Test
    void itAimsWhereTheShipWasWhenTheVolleyCameDue() {
        Sortie sortie = sortie(Act6ASpecs.TURRET, 0.5);
        int due = -1;
        for (int i = 0; i < SimStep.ticks(6) && due < 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.SPOOF_HESITATE) > 0) {
                due = i;
            }
        }
        assertTrue(due >= 0);
        double staleX = sortie.ship().x();
        double staleY = sortie.ship().y();
        Enemy turret = sortie.enemy(0);
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(0.5) && fired == 0; i++) {
            sortie.step(Command.LEFT.bit());
            fired = sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertEquals(1, fired);
        assertTrue(sortie.ship().x() < staleX - 20, "the ship moved on");
        EnemyBullet bullet = sortie.bullet(sortie.bulletCount() - 1);
        double stale = Math.atan2(staleY - turret.y(), staleX - turret.x());
        assertEquals(stale, Math.atan2(bullet.vy(), bullet.vx()), 1e-6, "at its stale aim");
    }

    @Test
    void aToldAttackStartsItsTellLate() {
        EnemySpec rail = Act5Specs.RAIL_BUNKER.withSpoofable(true);
        List<Integer> plain = times(sortie(rail, 0), SimEvents.Type.LINE_TELEGRAPH, 10);
        List<Integer> late = times(sortie(rail, 0.5), SimEvents.Type.LINE_TELEGRAPH, 10);
        assertFalse(plain.isEmpty());
        for (int k = 0; k < Math.min(plain.size(), late.size()); k++) {
            assertEquals(plain.get(k) + SimStep.ticks(0.5), late.get(k), plain + " / " + late);
        }
        List<Integer> plainFired = times(sortie(rail, 0), SimEvents.Type.LINE_FIRED, 10);
        List<Integer> lateFired = times(sortie(rail, 0.5), SimEvents.Type.LINE_FIRED, 10);
        assertEquals(
                plainFired.getFirst() - plain.getFirst(),
                lateFired.getFirst() - late.getFirst(),
                "the tell keeps its length");
    }

    @Test
    void withoutTheModuleOrOnAUnitThatIsNotSpoofableNothingChanges() {
        Sortie off = sortie(Act6ASpecs.TURRET, 0);
        Sortie never = sortie(Act6ASpecs.TURRET.withSpoofable(false), 0.5);
        Sortie plain = sortie(Act6ASpecs.TURRET.withSpoofable(false), 0);
        for (int i = 0; i < SimStep.ticks(6); i++) {
            off.step(Command.NONE);
            never.step(Command.NONE);
            plain.step(Command.NONE);
            assertEquals(plain.stateHash(), never.stateHash());
            assertEquals(
                    plain.events().count(SimEvents.Type.ENEMY_FIRED),
                    off.events().count(SimEvents.Type.ENEMY_FIRED));
            assertEquals(0, never.events().count(SimEvents.Type.SPOOF_HESITATE));
        }
    }
}
