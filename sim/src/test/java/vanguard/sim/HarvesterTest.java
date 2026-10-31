package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1 (design/campaign Level 36, the harvesters; gap check m10b §1 #3, #4, §4): a harvester pair enters
 * at the top edge 160 px apart and drifts down at 50 px/s, 30 px/s sideways, the two hulls mirrored, out at the bottom
 * edge. It is not counted as an enemy ({@link EnemySpec#countsAsEnemy()}: not in the enemy total, no kill), but picked
 * and paid, with the ground targets' share. Its escorts circle the living pair's midpoint, the one left, and break off
 * once both are gone.
 */
class HarvesterTest {
    private static Sortie sortie(List<WaveSpec> waves) {
        return CloudDiversSpecs.sortie(CloudDiversSpecs.level(40, waves));
    }

    private static void fly(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.NONE);
        }
    }

    private static List<WaveSpec> harvestersOnly() {
        return List.of(TestSpecs.wave(
                0,
                WaveSpec.Formation.CARRIER_ESCORTS,
                CloudDiversSpecs.HARVESTER,
                2,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE));
    }

    @Test
    void aPairDriftsDownMirroredAndLeavesAtTheBottomEdge() {
        Sortie sortie = sortie(harvestersOnly());
        sortie.step(Command.NONE);
        List<Enemy> pair = CloudDiversSpecs.all(sortie, "harvester");
        assertEquals(2, pair.size());
        Enemy left = pair.get(0).x() < pair.get(1).x() ? pair.get(0) : pair.get(1);
        Enemy right = left == pair.get(0) ? pair.get(1) : pair.get(0);
        assertEquals(160, right.x() - left.x(), 2, "160 px apart as they enter");
        assertEquals(PlayField.WIDTH / 2.0, (left.x() + right.x()) / 2, 1e-6, "about the field's middle");
        fly(sortie, 1);
        double lx = left.x();
        double ly = left.y();
        double rx = right.x();
        fly(sortie, 4);
        assertEquals(-200, left.y() - ly, 1, "down the screen at 50 px/s");
        assertEquals(120, left.x() - lx, 1, "the left one 30 px/s to the right");
        assertEquals(-120, right.x() - rx, 1, "the right one mirrored");
        fly(sortie, 10);
        assertEquals(0, CloudDiversSpecs.all(sortie, "harvester").size(), "out at the bottom edge");
    }

    @Test
    void itIsNotCountedButPickedAndPaidWithTheGroundTargets() {
        EnemySpec harvester = CloudDiversSpecs.HARVESTER;
        assertFalse(harvester.countsAsEnemy());
        assertFalse(harvester.neutral(), "a hostile craft, not a neutral one");
        assertFalse(harvester.seekersIgnore(), "the seekers, Rook and the Targeting computer pick it");
        Sortie sortie = sortie(CloudDiversSpecs.pair(0, harvester, 2));
        assertEquals(2, sortie.enemyTotal(), "the escorts only");
        fly(sortie, 2);
        CloudDiversSpecs.kill(sortie, CloudDiversSpecs.all(sortie, "harvester").get(0));
        assertEquals(0, sortie.kills(), "no kill");
        assertEquals(0, sortie.tally.credits(CreditSource.KILLS));
        assertTrue(sortie.tally.credits(CreditSource.GROUND_TARGETS) > 0, "paid with the ground targets");
        assertEquals(sortie.credits(), sortie.tally.credits(CreditSource.GROUND_TARGETS));
        assertEquals(1, sortie.chain(), "it keeps the chain going");
    }

    /** The midpoint of the harvesters on the field. */
    private static double[] middle(Sortie sortie) {
        List<Enemy> pair = CloudDiversSpecs.all(sortie, "harvester");
        double x = 0;
        double y = 0;
        for (Enemy hull : pair) {
            x += hull.x() / pair.size();
            y += hull.y() / pair.size();
        }
        return new double[] {x, y};
    }

    private static void assertCircling(Sortie sortie, double[] centre) {
        List<Enemy> escorts = CloudDiversSpecs.all(sortie, "talon");
        assertEquals(2, escorts.size());
        for (Enemy escort : escorts) {
            assertEquals(Enemy.Phase.ESCORT, escort.phase);
            assertEquals(90, Math.hypot(escort.x() - centre[0], escort.y() - centre[1]), 2);
        }
    }

    @Test
    void theEscortsCircleTheLivingPairsMidpointThenTheOneLeftThenBreakOff() {
        Sortie sortie = sortie(CloudDiversSpecs.pair(0, CloudDiversSpecs.HARVESTER, 2));
        fly(sortie, 2);
        assertCircling(sortie, middle(sortie));
        CloudDiversSpecs.kill(sortie, CloudDiversSpecs.all(sortie, "harvester").get(0));
        fly(sortie, 1);
        assertEquals(1, CloudDiversSpecs.all(sortie, "harvester").size());
        assertCircling(sortie, middle(sortie));
        CloudDiversSpecs.kill(sortie, CloudDiversSpecs.all(sortie, "harvester").get(0));
        fly(sortie, 1);
        for (Enemy escort : CloudDiversSpecs.all(sortie, "talon")) {
            assertEquals(Enemy.Phase.LEAVE, escort.phase, "both gone: they break off");
        }
    }

    @Test
    void thePairsEscortsBreakOffOnceBothLeftTheScreen() {
        Sortie sortie = sortie(CloudDiversSpecs.pair(0, CloudDiversSpecs.HARVESTER, 2));
        boolean left = false;
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(Command.NONE);
            if (CloudDiversSpecs.all(sortie, "harvester").isEmpty()) {
                left = true;
                break;
            }
            for (Enemy escort : CloudDiversSpecs.all(sortie, "talon")) {
                assertEquals(Enemy.Phase.ESCORT, escort.phase, "while a harvester flies");
            }
        }
        assertTrue(left);
        fly(sortie, 1);
        for (Enemy escort : CloudDiversSpecs.all(sortie, "talon")) {
            assertEquals(Enemy.Phase.LEAVE, escort.phase);
        }
    }

    @Test
    void aDriftingCarrierIsAPlainFlyerPaidAsAKillOrAGroundTarget() {
        EnemySpec plain = EnemySpec.of("x", 10, new Hitbox(10, 10), Layer.AIR, 0, false, 1, 50);
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withHaul(Optional.of(CreditSource.SALVAGE)),
                "a bounty is a kill's or a ground target's");
        assertThrows(IllegalArgumentException.class, () -> plain.withTerrain(true)
                .withCarrierDrift(Optional.of(CloudDiversSpecs.DRIFT)));
        assertThrows(
                IllegalArgumentException.class,
                () -> PartDSpecs.MOTE.withCarrierDrift(Optional.of(CloudDiversSpecs.DRIFT)),
                "not a flock's");
        assertEquals(CreditSource.GROUND_TARGETS, plain.bountySource(true));
        assertEquals(CreditSource.KILLS, plain.bountySource(false));
        assertEquals(CreditSource.GROUND_TARGETS, CloudDiversSpecs.HARVESTER.bountySource(false));
        assertEquals(30, CloudDiversSpecs.DRIFT.vxAt(100), 1e-9);
        assertEquals(-30, CloudDiversSpecs.DRIFT.vxAt(300), 1e-9);
        assertEquals(30, new EnemySpec.CarrierDrift(30, false).vxAt(300), 1e-9);
    }
}
