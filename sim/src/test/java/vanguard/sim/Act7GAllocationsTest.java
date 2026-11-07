package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1: Level 49's new mechanics step without allocating: a rush's per-gate arenas (a crawling gate, a
 * breaching boss's crawling gate, an anchored boss's halted gate); the Brood Carrier echo (windows on a move's end, the
 * swept volley, the alternating brood, its dissolve with its brood and the projectiles' fade); the Dust Colossus echo
 * (the seeded draw, the vents staying up); the Iron Sovereign echo (one ring, the forming, the cover, the wreck throw
 * after two dead batteries); the kill ratio by the first warning; and the Seraph pair's lifted rows and dropped ring.
 */
class Act7GAllocationsTest {
    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    static Sortie rush() {
        return Act7GSpecs.sortie(Act7GSpecs.rush(
                Act7GSpecs.SECTIONS,
                List.of(
                        Act7GSpecs.carrierEcho(11.5),
                        Act7GSpecs.at(BreachArenaTest.colossus(false), 21),
                        Act7GSpecs.at(Act5HSpecs.QUIET, 39.5))));
    }

    static void flyRush(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(60); i++) {
            if (i == SimStep.ticks(25)) {
                Act7GSpecs.kill(sortie, 0);
            } else if (i == SimStep.ticks(40)) {
                Act7GSpecs.kill(sortie, 1);
            } else if (i == SimStep.ticks(55)) {
                Act7GSpecs.kill(sortie, 2);
            }
            sortie.step(command(i));
        }
    }

    @Test
    void theRushsArenasAndTheCarrierEchoStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7GAllocationsTest::rush, Act7GAllocationsTest::flyRush));
    }

    static Sortie colossus() {
        return Act7GSpecs.sortie(BreachHazardsTest.level(ColossusEchoTest.echo()));
    }

    static void fly(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(command(i));
        }
    }

    @Test
    void theColossusEchoStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7GAllocationsTest::colossus, sortie -> fly(sortie, 60)));
    }

    static Sortie sovereign() {
        return Act7GSpecs.sortie(Act5HSpecs.level(SovereignEchoTest.echo()));
    }

    static void flySovereign(Sortie sortie) {
        List<Integer> sams = Act5HSpecs.SAMS;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            if (i == SimStep.ticks(12)) {
                sortie.objectivePasses.wreck(0, sams.get(0));
                sortie.objectivePasses.wreck(0, sams.get(1));
            }
            sortie.step(command(i));
        }
    }

    @Test
    void theSovereignEchoStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7GAllocationsTest::sovereign, Act7GAllocationsTest::flySovereign));
    }

    @Test
    void theKillRatioByTheWarningStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(() -> KillRatioUntilTest.sortie(20), sortie -> fly(sortie, 25)));
    }

    static Sortie seraphs() {
        return Act7ASpecs.sortie(Act7ASpecs.level(
                40,
                List.of(
                        Act7ESpecs.station(0, Act7ESpecs.herald(90), 240, 70, 16),
                        Act7ESpecs.pair(1.5, Act7ESpecs.SERAPH, Optional.empty())),
                Optional.empty()));
    }

    static void flySeraphs(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(17); i++) {
            sortie.step(Command.UP.bit() | (i % 160 < 80 ? Command.LEFT.bit() : Command.RIGHT.bit()));
        }
    }

    @Test
    void theSeraphPairStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7GAllocationsTest::seraphs, Act7GAllocationsTest::flySeraphs));
    }

    @Test
    void theRushReplays() {
        Sortie one = rush();
        Sortie two = rush();
        flyRush(one);
        flyRush(two);
        assertEquals(one.stateHash(), two.stateHash());
    }
}
