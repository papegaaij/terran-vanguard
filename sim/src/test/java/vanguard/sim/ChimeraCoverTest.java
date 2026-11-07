package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1c (design/enemies/bosses/vornes-chimera, The playbooks: no phase is quiet; Phase 1: the Dart stream
 * while the bay lives): the rear sweep's drop fires its pair as it starts and as its source leaves the top edge (the
 * cover through its second out of sight); a playbook with a rear sweep fires its told move's cover as each tell starts
 * too (one without does not); a stream from a bay on a part is drawn and launches only while that part lives.
 */
class ChimeraCoverTest {
    /** The spec's Dart stream from the bay (0, -50): 6 Darts 0.4 s apart. */
    static final EnemySpec.Launcher DARTS =
            new EnemySpec.Launcher(Act5CSpecs.DART, 6, 5, 0.4, 1, 240, Math.toRadians(30), 0.4, 16);

    /** Phase 1: the fan (covered) and the Dart stream on the bay's place; phases 2 and 3 as {@link Act7FSpecs}. */
    static SetPieceSpec chimera(boolean bayOnPart) {
        List<PhasePlaybook> books = new ArrayList<>(Act7FSpecs.playbooks(2));
        PhasePlaybook.Move stream = new PhasePlaybook.Move(
                PhasePlaybook.Kind.STREAM,
                1,
                -1,
                0,
                0,
                1,
                List.of(),
                Optional.of(new PhasePlaybook.Bays(
                        DARTS, List.of(bayOnPart ? new BossSpec.Offset(0, -50) : new BossSpec.Offset(-60, 40)))));
        books.set(
                0,
                new PhasePlaybook(
                        0,
                        List.of(Act7FSpecs.CAPSULE),
                        0.8,
                        1.0,
                        0.7,
                        0.5,
                        List.of(
                                PhasePlaybook.Move.of(PhasePlaybook.Kind.VOLLEY, 1, Act7FSpecs.FAN, 2, 0.5)
                                        .covered(Act7FSpecs.SPORE_PAIR),
                                stream)));
        PhasePlaybook two = books.get(1);
        books.set(
                1,
                new PhasePlaybook(
                                two.phase(),
                                two.sources(),
                                two.tellSeconds(),
                                two.restSeconds(),
                                two.lowRestSeconds(),
                                two.lowBelow(),
                                List.of(
                                        PhasePlaybook.Move.of(PhasePlaybook.Kind.SPIRAL, 1, Act7FSpecs.SPIRAL, 1, 0)
                                                .covered(Act7FSpecs.SPORE_PAIR),
                                        PhasePlaybook.Move.rearSweep(1).neverFirst()))
                        .withLanceEvery(2));
        return Act7FSpecs.chimera(books);
    }

    /** A rig that counts the Darts launched too. */
    static final class Rig extends Act7FSpecs.Rig {
        int darts;

        Rig(SetPieceSpec spec) {
            super(spec);
        }

        @Override
        public void bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {
            darts++;
        }
    }

    @Test
    void theDropFiresAsItStartsAndAsItLeavesTheTopEdgeAndNothingOutOfSight() {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.SWEEPING).settled().toPhase2();
        RearSweep sweep = rig.piece.rearSweep().orElseThrow();
        int dropping = 0;
        int dropShots = 0;
        int awayShots = 0;
        boolean first = false;
        double lastDropShotY = Double.NaN;
        for (int i = 0; i < SimStep.ticks(30) && sweep.sweeps() < 2; i++) {
            rig.step();
            if (sweep.mode() == RearSweep.Mode.DROP) {
                dropping++;
                if (!rig.shots.isEmpty()) {
                    dropShots++;
                    first |= dropping == 1;
                    lastDropShotY = rig.shots.get(0)[1];
                }
            } else if (sweep.mode() == RearSweep.Mode.AWAY) {
                awayShots += rig.shots.size();
            }
        }
        assertTrue(sweep.sweeps() >= 1, "a sweep was told");
        assertTrue(first, "the drop's first step fires (the told sweep's pair)");
        assertEquals(2, dropShots, "two shots in the drop: as it starts and as its source leaves the top edge");
        assertTrue(lastDropShotY > PlayField.HEIGHT - 10, "the second from the top edge: " + lastDropShotY);
        assertEquals(0, awayShots, "nothing out of sight");
    }

    @Test
    void aPlaybookWithARearSweepCoversItsTellsAndOneWithoutDoesNot() {
        Rig rig = new Rig(chimera(true));
        rig.settled();
        int phaseOneTells = 0;
        int phaseOneTellShots = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty()) {
                phaseOneTells++;
                phaseOneTellShots += rig.shots.size();
            }
        }
        assertTrue(phaseOneTells >= 2, "phase 1 told its moves");
        assertEquals(0, phaseOneTellShots, "phase 1 (no rear sweep): no cover as a tell starts");
        rig.toPhase2();
        int tells = 0;
        int covered = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            rig.step();
            List<Integer> told = rig.values(SimEvents.Type.BOSS_PLAY_TELL);
            if (!told.isEmpty() && told.get(0) == 0) {
                tells++;
                covered += rig.shots.isEmpty() ? 0 : 1;
            }
        }
        assertTrue(tells >= 2, "phase 2 told its spiral");
        assertEquals(tells, covered, "each covered tell fires its cover as it starts");
    }

    @Test
    void aStreamFromABayOnAPartStopsWithThatPart() {
        Rig rig = new Rig(chimera(true));
        rig.settled();
        // wait for the stream's run, kill the bay after its first Dart
        for (int i = 0; i < SimStep.ticks(20) && rig.darts == 0; i++) {
            rig.step();
        }
        assertEquals(1, rig.darts, "the stream started");
        rig.kill(Act7FSpecs.BAY);
        int streamTells = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            rig.step();
            List<Integer> told = rig.values(SimEvents.Type.BOSS_PLAY_TELL);
            streamTells += !told.isEmpty() && told.get(0) == 1 ? 1 : 0;
        }
        assertEquals(1, rig.darts, "no Dart after the bay's death");
        assertEquals(0, streamTells, "the stream is never drawn again");
        assertFalse(rig.piece.partHp(Act7FSpecs.BAY) > 0);
    }

    @Test
    void aStreamFromHullBaysKeepsLaunching() {
        Rig rig = new Rig(chimera(false));
        rig.settled();
        for (int i = 0; i < SimStep.ticks(20) && rig.darts == 0; i++) {
            rig.step();
        }
        assertEquals(1, rig.darts, "the stream started");
        rig.kill(Act7FSpecs.BAY);
        rig.steps(3);
        assertEquals(DARTS.stream(), rig.darts, "hull bays (no part there) launch the whole stream");
    }
}
