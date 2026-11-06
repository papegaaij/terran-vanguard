package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a (the movement vocabulary's {@code teleport}; design/enemies/space/rift-skater): a Skater glides
 * 1.2 s, then each jump fades 0.2 s (hittable), is gone ≥ 0.5 s while its ring forms (untouchable by every path, no
 * contact, missed by the EMP), lands where the fairness rules hold (≥ 120 px from the ship, out of the 80 px band, 24 px
 * off the sides; a ring the ship chases slides away), fires its burst 0.25 s after landing, and after 3 jumps glides
 * out; at most its ring cap of rings at once; with a drift it jumps toward the drift's side.
 */
class TeleportTest {
    private static Sortie sortie(Loadout loadout, Optional<HazardSpec.Current> current, WaveSpec... waves) {
        return new Sortie(
                1,
                loadout,
                Act7ASpecs.level(40, List.of(waves), current),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void itGlidesFadesIsGoneLandsFiresAndAfterThreeJumpsGlidesOut() {
        Sortie sortie = sortie(TestSpecs.LOADOUT, Optional.empty(), Act7ASpecs.single(0, Act7ASpecs.SKATER));
        Enemy skater = null;
        int onField = 0;
        while (skater == null || !skater.teleport().fading()) {
            sortie.step(Command.NONE);
            skater = Act7ASpecs.find(sortie, "rift-skater");
            if (skater != null && PlayField.overlaps(skater.x(), skater.y(), skater.hitbox())) {
                onField++;
            }
        }
        assertEquals(SimStep.ticks(1.2), onField, 1, "its first glide on the play field");
        int arrivals = 0;
        int lastArrive = -1;
        int step = 0;
        int fade = 0;
        int gone = 0;
        while (Act7ASpecs.find(sortie, "rift-skater") != null && step < SimStep.ticks(20)) {
            if (skater.teleport().fading()) {
                fade++;
                assertTrue(ShotGeometry.onField(skater), "hittable while it fades");
            }
            if (skater.teleport().gone()) {
                gone++;
                assertFalse(ShotGeometry.onField(skater), "gone: no path reaches it");
                assertFalse(skater.bodyTouches(), "gone: no contact");
            }
            sortie.step(Command.NONE);
            step++;
            if (sortie.events().count(SimEvents.Type.ARRIVE) > 0) {
                arrivals++;
                lastArrive = step;
                double dx = skater.x() - sortie.ship().x();
                double dy = skater.y() - sortie.ship().y();
                assertTrue(Math.hypot(dx, dy) >= 120, "never within 120 px of the ship");
                assertTrue(skater.y() >= 80 && skater.x() >= 24 && skater.x() <= PlayField.WIDTH - 24);
            }
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                assertTrue(lastArrive >= 0 && step - lastArrive >= SimStep.ticks(0.25), "no earlier than 0.25 s");
            }
        }
        assertEquals(3, arrivals, "3 jumps");
        assertEquals(3 * SimStep.ticks(0.2), fade, 3);
        assertTrue(gone >= 3 * SimStep.ticks(0.5), "gone at least 0.5 s a jump");
        assertNull(Act7ASpecs.find(sortie, "rift-skater"), "it glided out");
        assertEquals(0, sortie.kills());
    }

    @Test
    void itsBurstFollowsEachLandingOnly() {
        Sortie sortie = sortie(TestSpecs.LOADOUT, Optional.empty(), Act7ASpecs.single(0, Act7ASpecs.SKATER));
        int shots = 0;
        int arrivals = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            shots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            arrivals += sortie.events().count(SimEvents.Type.ARRIVE);
        }
        assertEquals(3, arrivals);
        assertEquals(3 * 3, shots, "a burst of 3 after each landing, nothing else");
    }

    @Test
    void aRingTheShipChasesSlidesAwayAndItLandsOutOfReach() {
        Sortie sortie = sortie(TestSpecs.LOADOUT, Optional.empty(), Act7ASpecs.single(0, Act7ASpecs.SKATER));
        Enemy skater = null;
        int arrivals = 0;
        for (int i = 0; i < SimStep.ticks(14) && arrivals < 3; i++) {
            int command = Command.NONE;
            if (skater != null && skater.teleport().gone()) {
                double dx = skater.teleport().ringX() - sortie.ship().x();
                double dy = skater.teleport().ringY() - sortie.ship().y();
                command = (dx > 4 ? Command.RIGHT.bit() : dx < -4 ? Command.LEFT.bit() : 0)
                        | (dy > 4 ? Command.UP.bit() : dy < -4 ? Command.DOWN.bit() : 0);
            }
            sortie.step(command);
            skater = Act7ASpecs.find(sortie, "rift-skater");
            if (sortie.events().count(SimEvents.Type.ARRIVE) > 0) {
                arrivals++;
                double distance = Math.hypot(
                        skater.x() - sortie.ship().x(),
                        skater.y() - sortie.ship().y());
                assertTrue(distance >= 120 - 1e-9, "the landing checked again: " + distance);
            }
        }
        assertTrue(arrivals > 0);
    }

    @Test
    void theEmpMissesAGoneUnit() {
        Sortie sortie = sortie(
                TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(1)),
                Optional.empty(),
                Act7ASpecs.single(0, Act7ASpecs.SKATER));
        Enemy skater = null;
        while (skater == null || !skater.teleport().gone()) {
            sortie.step(Command.NONE);
            skater = Act7ASpecs.find(sortie, "rift-skater");
        }
        int stunned = 0;
        int command = Command.SPECIAL.bit();
        while (skater.teleport().gone()) {
            sortie.step(command);
            command = Command.NONE;
            stunned += sortie.events().count(SimEvents.Type.EMP_STUNNED);
            assertFalse(skater.empStunned());
        }
        assertEquals(0, stunned, "not on the screen for the EMP");
    }

    @Test
    void atMostItsRingCapOfRingsAtOnce() {
        EnemySpec.Teleport two =
                new EnemySpec.Teleport(0.2, 0.5, 120, new Range(90, 130), new Range(30, 60), 0.4, 0.3, 3, 0.25, 2);
        EnemySpec capped = Act7ASpecs.SKATER.withTeleport(Optional.of(two));
        Sortie sortie = sortie(TestSpecs.LOADOUT, Optional.empty(), Act7ASpecs.stream(0, capped, 6, 0.3));
        int most = 0;
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(Command.NONE);
            int rings = 0;
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Teleporting jump = sortie.enemy(k).teleport();
                rings += jump.fading() || jump.gone() ? 1 : 0;
            }
            most = Math.max(most, rings);
        }
        assertEquals(2, most, "the cap holds and binds");
    }

    @Test
    void withADriftItJumpsTowardTheDriftsSide() {
        Sortie sortie = sortie(
                TestSpecs.LOADOUT,
                Optional.of(Act7ASpecs.steady(15, List.of())),
                Act7ASpecs.single(
                        0,
                        Act7ASpecs.SKATER.withTeleport(Optional.of(new EnemySpec.Teleport(
                                0.2, 0.5, 0, new Range(90, 130), new Range(30, 60), 0.5, 0.5, 3, 0.25, 4)))));
        int rings = 0;
        int right = 0;
        double fromX = Double.NaN;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            Enemy skater = Act7ASpecs.find(sortie, "rift-skater");
            if (sortie.events().count(SimEvents.Type.JUMP_OUT) > 0) {
                fromX = skater.x();
            }
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.JUMP_RING) {
                    if (rings == 0) {
                        assertTrue(sortie.events().x(e) > fromX, "toward the drift's side (right)");
                    }
                    rings++;
                    right += sortie.events().x(e) > fromX ? 1 : 0;
                }
            }
        }
        assertTrue(rings >= 2);
        assertTrue(right >= 2, "the jumps go with the drift, but where a rule turns one: " + right + " of " + rings);
    }

    @Test
    void theBandLiesAtTheTopWhileTheScrollRunsReversed() {
        Teleporting jump = new Teleporting();
        Teleporting.Field field = new Teleporting.Field();
        jump.field(field);
        EnemySpec.Teleport spec = Act7ASpecs.JUMPS;
        assertFalse(jump.fits(spec, 240, 50, 240, 400), "the bottom band");
        assertTrue(jump.fits(spec, 240, PlayField.HEIGHT - 50, 240, 96));
        field.reversed = true;
        assertTrue(jump.fits(spec, 240, 50, 240, 400));
        assertFalse(jump.fits(spec, 240, PlayField.HEIGHT - 50, 240, 96), "the top band in a reversal");
        assertFalse(jump.fits(spec, 10, 300, 240, 96), "24 px off a side");
        assertFalse(jump.fits(spec, 240, 200, 240, 96), "within 120 px of the ship");
    }
}
