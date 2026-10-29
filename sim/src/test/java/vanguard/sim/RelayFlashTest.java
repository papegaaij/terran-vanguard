package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H1b ({@link RelayFlash}; design/campaign Level 35, the mass-relay flash): at its time the line locks
 * through the ship's height, the flash follows its 1.2 s telegraph, 16 px wide, live 0.2 s, 20 damage to the ship once;
 * a sidestep out of the band in the telegraph misses it; it fires once; Rook is untouched; a level without it hashes
 * as before and a replay repeats it exactly.
 */
class RelayFlashTest {
    private static final double EPS = 1e-9;
    private static final double T = 3;
    private static final HazardSpec.Relay RELAY = new HazardSpec.Relay(T, 1.2, 0.2, 16, 20, true);

    private static LevelScript level(Optional<HazardSpec.Relay> relay) {
        LevelScript plain = TestSpecs.level(30, List.of());
        return plain.toBuilder().hazards(plain.hazards().withRelay(relay)).build();
    }

    private static Sortie sortie(Loadout loadout) {
        return new Sortie(1, loadout, level(Optional.of(RELAY)), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    private static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }

    @Test
    void itsLineLocksThroughTheShipAndItsFlashHitsTheShipOnce() {
        Sortie sortie = sortie(TestSpecs.LOADOUT);
        RelayFlash relay = sortie.relay().orElseThrow();
        while (count(sortie, SimEvents.Type.RELAY_TELEGRAPH) == 0) {
            assertFalse(relay.telegraphing());
            sortie.step(Command.NONE);
        }
        assertEquals(SimStep.ticks(T), sortie.levelTick, 1, "at its time on the level clock");
        double lineY = relay.lineY();
        assertEquals(sortie.ship().y(), lineY, EPS, "through the ship's height");
        assertEquals(PlayField.WIDTH, sortie.events().x(0), EPS, "from the right edge");
        double lost = 0;
        int fired = 0;
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(5); i++) {
            double before =
                    sortie.ship().defences().armour() + sortie.ship().defences().shield();
            sortie.step(Command.NONE);
            fired += count(sortie, SimEvents.Type.RELAY_FIRED);
            if (count(sortie, SimEvents.Type.RELAY_HIT) > 0) {
                lost = before
                        - sortie.ship().defences().armour()
                        - sortie.ship().defences().shield();
            }
            hits += count(sortie, SimEvents.Type.RELAY_HIT);
            assertEquals(lineY, relay.lineY(), EPS, "locked");
            if (i == SimStep.ticks(1.2) - 2) {
                assertTrue(relay.telegraphing(), "the telegraph runs 1.2 s");
            }
        }
        assertEquals(1, fired, "once");
        assertEquals(1, hits, "the ship once");
        assertTrue(relay.spent());
        assertEquals(20, lost, 1e-6, "20 damage");
    }

    @Test
    void aSidestepOutOfTheBandInTheTelegraphMissesIt() {
        Sortie sortie = sortie(TestSpecs.LOADOUT);
        while (count(sortie, SimEvents.Type.RELAY_TELEGRAPH) == 0) {
            sortie.step(Command.NONE);
        }
        double lineY = sortie.relay().orElseThrow().lineY();
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(i < SimStep.ticks(0.6) ? Command.UP.bit() : Command.NONE);
            hits += count(sortie, SimEvents.Type.RELAY_HIT);
        }
        assertTrue(
                Math.abs(sortie.ship().y() - lineY) > 8 + 10,
                "out of the band: " + (sortie.ship().y() - lineY));
        assertEquals(0, hits);
    }

    @Test
    void rookOnTheLineIsUntouchedWhileTheShipStepsOff() {
        Sortie sortie = sortie(TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)));
        Wingman rook = sortie.wingman().orElseThrow();
        RelayFlash relay = sortie.relay().orElseThrow();
        double armour = rook.armour();
        boolean onLine = false;
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(T + 3); i++) {
            int commands = Command.NONE;
            if (relay.telegraphing()) {
                // The ship steers so that Rook, in his slot, comes onto the locked line (and the ship off it).
                commands = rook.y() < relay.lineY() - 2
                        ? Command.UP.bit()
                        : rook.y() > relay.lineY() + 2 ? Command.DOWN.bit() : Command.NONE;
            }
            sortie.step(commands);
            hits += count(sortie, SimEvents.Type.RELAY_HIT);
            onLine |= relay.live()
                    && rook.hull()
                            .touchesSegment(rook.x(), rook.y(), 0, relay.lineY(), PlayField.WIDTH, relay.lineY(), 8);
        }
        assertTrue(onLine, "Rook on the live line");
        assertEquals(armour, rook.armour(), EPS, "untouched");
        assertEquals(0, hits, "the ship off it");
    }

    @Test
    void aLevelWithoutItHashesAsBeforeAndAReplayRepeatsIt() {
        Sortie plain =
                new Sortie(1, TestSpecs.LOADOUT, level(Optional.empty()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Sortie none = new Sortie(
                1, TestSpecs.LOADOUT, TestSpecs.level(30, List.of()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        assertTrue(plain.relay().isEmpty());
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = sortie(TestSpecs.LOADOUT);
            for (int i = 0; i < SimStep.ticks(T + 2); i++) {
                sortie.step((i / 30) % 2 == 0 ? Command.UP.bit() : Command.DOWN.bit());
            }
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        for (int i = 0; i < SimStep.ticks(T + 2); i++) {
            plain.step(Command.NONE);
            none.step(Command.NONE);
        }
        assertEquals(none.stateHash(), plain.stateHash(), "opt-in: nothing hashed without a relay");
        assertThrows(IllegalArgumentException.class, () -> new HazardSpec.Relay(1, 0, 0.2, 16, 20, true));
    }
}
