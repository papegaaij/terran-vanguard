package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.CueTrigger;
import vanguard.sim.GroundUnit;
import vanguard.sim.HaltCycle;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.UplinkSpec;

/**
 * M10 part G, step G2 (design/campaign Level 41; design/enemies/ground/shield-pylon and halo-platform): the part's data
 * keys reach the simulation as steps G1a/G1b's fixtures build them: the Halo Platform a held, spoofable set piece on the
 * ring engine (its turrets' own attacks, the all-round bearing, the eased losses by difficulty, the tells, the barrier's
 * 0.5 s fade), Level 41's four gates with their own halt cycles and holders asleep until their halts, the three archive
 * cores' uplinks on their hatches, the files secondary and the gate events' cues.
 */
class FortressKeysTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;
    private static final String LEVEL = Level41Test.LEVEL;

    private static SetPieceSpec halo(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, LEVEL, difficulty).setPieces().getFirst();
    }

    @Test
    void theHaloIsAHeldSpoofableSetPieceOnTheRingEngine() {
        SetPieceSpec piece = halo(Difficulty.MEDIUM);
        BossSpec boss = piece.boss().orElseThrow();
        assertEquals("halo-platform", piece.slug());
        assertTrue(boss.held(), "a held set piece: no bar, warning or boss music");
        assertTrue(boss.spoofable(), "its guns fooled by the spoofer");
        assertEquals(113.98, boss.arriveSeconds(), 1e-9, "a step before its gate's end");
        assertEquals(
                List.of("core", "gun-1", "rail-1", "gun-2", "rail-2", "gun-3", "rail-3"),
                piece.parts().stream().map(vanguard.sim.PartSpec::name).toList());
        RingSpec rings = boss.rings().orElseThrow();
        RingSpec.Ring ring = rings.rings().getFirst();
        int gun = attack(boss, "gun");
        int rail = attack(boss, "rail");
        assertEquals(List.of(gun, rail, gun, rail, gun, rail), ring.attacks(), "alternating gun and rail turrets");
        assertEquals(Math.toRadians(20), ring.radiansPerSecond(), 1e-12);
        assertEquals(RingSpec.ALL_ROUND, rings.bearing(), "the arc all round, no clamp");
        assertEquals(Math.toRadians(120), rings.arcRadians(), 1e-12);
        RingSpec.Losses losses = rings.losses().orElseThrow();
        assertEquals(1.4, losses.factor(1), 1e-12, "+8°/s a turret lost");
        assertEquals(3.0, losses.factor(5), 1e-12, "60°/s with five lost");
        assertEquals(0.8, losses.easeSeconds(), 1e-12);
        assertEquals(0.3, rings.tellSeconds(gun), 1e-12, "the muzzles' glow");
        assertEquals(0.5, rings.tellSeconds(attack(boss, "core-ring")), 1e-12, "the eye's flare");
        assertEquals(0.5, rings.core().orElseThrow().openingSeconds(), 1e-12, "the barrier's fade");
        assertEquals(0.15, rings.cookoffSeconds(), 1e-12);
        BossSpec.Phase first = boss.phases().getFirst();
        assertEquals(3, first.left(), "the barrier drops at the third turret lost");
        assertEquals(6, first.untilParts().size());
    }

    @Test
    void theHalosDifficultiesChangeItsRingAndItsOpenCore() {
        BossSpec easy = halo(Difficulty.EASY).boss().orElseThrow();
        RingSpec easyRings = easy.rings().orElseThrow();
        assertEquals(Math.toRadians(15), easyRings.rings().getFirst().radiansPerSecond(), 1e-12);
        assertEquals((15 + 6.0) / 15, easyRings.losses().orElseThrow().factor(1), 1e-12, "+6°/s on easy");
        assertFalse(easy.phases().getLast().attacks().contains(attack(easy, "core-ring")), "no open-core ring on easy");
        BossSpec hard = halo(Difficulty.HARD).boss().orElseThrow();
        RingSpec hardRings = hard.rings().orElseThrow();
        assertEquals(Math.toRadians(24), hardRings.rings().getFirst().radiansPerSecond(), 1e-12);
        assertEquals((24 + 10.0) / 24, hardRings.losses().orElseThrow().factor(1), 1e-12, "+10°/s on hard");
        assertEquals(12, hard.attacks().get(attack(hard, "core-ring")).count(), "the open core's ring of 12");
    }

    @Test
    void level41HoldsFourGatesWithTheirOwnCyclesAndSleepingPylons() {
        LevelScript level = SimSpecs.level(CONTENT, LEVEL, Difficulty.MEDIUM);
        List<Optional<HaltCycle>> cycles = level.gateCycles();
        assertEquals(4, cycles.size());
        assertEquals(
                List.of(13.0, 13.0, 13.0, 17.0),
                cycles.stream().map(c -> c.orElseThrow().everySeconds()).toList());
        assertEquals(0, level.gatePiece(3), "the Halo holds the fourth gate");
        List<GroundUnit> holders =
                level.groundUnits().stream().filter(GroundUnit::holder).toList();
        assertEquals(8, holders.size(), "2, 3 and 3 pylons");
        assertTrue(holders.stream().allMatch(unit -> unit.dormantUntil() > 0), "asleep until their halts");
        long easy = SimSpecs.level(CONTENT, LEVEL, Difficulty.EASY).groundUnits().stream()
                .filter(GroundUnit::holder)
                .count();
        assertEquals(6, easy, "2 pylons a gate on easy");
        assertEquals(
                2,
                SimSpecs.level(CONTENT, LEVEL, Difficulty.EASY)
                        .gateCycles()
                        .getFirst()
                        .orElseThrow()
                        .waves()
                        .size(),
                "the cycles without their Talon waves on easy");
    }

    @Test
    void theArchiveCoresUploadOnTheirHatchesForTheFilesSecondary() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(CONTENT, LEVEL, difficulty);
            List<UplinkSpec> uplinks = level.uplinks();
            assertEquals(3, uplinks.size());
            for (UplinkSpec uplink : uplinks) {
                assertEquals(
                        "archive-core",
                        level.groundObjects().get(uplink.hatch()).look(),
                        "its hatch");
                assertEquals(40, uplink.radius(), 1e-12);
            }
            double seconds =
                    switch (difficulty) {
                        case EASY -> 1.5;
                        case MEDIUM -> 2.0;
                        case HARD -> 2.5;
                    };
            assertEquals(seconds, uplinks.getFirst().seconds(), 1e-12);
            assertEquals(3, level.objectives().secondary().files(), "FILES n / 3");
        }
    }

    @Test
    void theGateEventsCueTheirLines() {
        List<RadioCue> radio = SimSpecs.level(CONTENT, LEVEL, Difficulty.MEDIUM).radio();
        assertEquals(List.of("gate-1", "gate-2", "gate-3", "halo-platform"), subjects(radio, CueTrigger.GATE_HALT));
        assertEquals(List.of("1", "2", "3"), subjects(radio, CueTrigger.FILE_IN));
        assertEquals(1, subjects(radio, CueTrigger.HALO_DESTROYED).size());
        LevelData.RadioCue silence = CONTENT.level(LEVEL).radio().stream()
                .filter(cue -> cue.event().orElse(null) == LevelData.CueEvent.HALO_DESTROYED)
                .findFirst()
                .orElseThrow();
        assertTrue(silence.neverStale(), "the Silence line never goes stale");
        assertEquals(2, silence.delaySeconds(), 1e-12);
    }

    private static List<String> subjects(List<RadioCue> radio, CueTrigger trigger) {
        return radio.stream()
                .filter(cue -> cue.trigger() == trigger)
                .map(RadioCue::subject)
                .toList();
    }

    private static int attack(BossSpec boss, String name) {
        for (int a = 0; a < boss.attacks().size(); a++) {
            if (boss.attacks().get(a).name().equals(name)) {
                return a;
            }
        }
        throw new AssertionError("no attack " + name);
    }
}
