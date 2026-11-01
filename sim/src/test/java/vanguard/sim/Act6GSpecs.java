package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part G, step G1a (design/campaign Level 41): fixtures for the gates' own halt cycles, a gate held by a set piece
 * (a minimal stand-in for the Halo Platform: one vital core, no guns) and the archive cores' uploads.
 */
final class Act6GSpecs {
    private Act6GSpecs() {}

    /** The stand-in piece's slug (the {@code halt} cue's subject for its gate). */
    static final String HALO = "halo-platform";

    /** A gate-holding set piece arriving at {@code arrive} s: one vital core of 100 HP, no attacks, one endless phase. */
    static SetPieceSpec piece(double arrive) {
        PartSpec core = new PartSpec("core", 0, 0, new Hitbox(60, 60), 100, true, 50, Optional.empty(), 0, 1);
        BossSpec.Phase hold = new BossSpec.Phase(
                "hold",
                List.of(),
                0,
                List.of(),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty());
        return new SetPieceSpec(
                HALO,
                new Hitbox(200, 200),
                new Hitbox(60, 60),
                0,
                List.of(core),
                List.of(),
                Optional.empty(),
                Optional.of(BossSpec.of(
                        arrive,
                        240,
                        420,
                        200,
                        0,
                        1,
                        Layer.AIR,
                        false,
                        "HALO",
                        60,
                        List.of(),
                        List.of(),
                        List.of(hold))));
    }

    /** A front wave of {@code count} {@code enemy} at {@code t} s of a cycle. */
    static WaveSpec front(double t, EnemySpec enemy, int count) {
        return TestSpecs.wave(t, WaveSpec.Formation.SNAKE, enemy, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.LEFT);
    }

    /** Gate 0's cycle: two Skitters at H+0.5, every 2 s. */
    static final HaltCycle SKITTERS = new HaltCycle(2, List.of(front(0.5, TestSpecs.SKITTER, 2)));
    /** Gate 1's cycle: one Needler at H+1, every 3 s. */
    static final HaltCycle NEEDLERS = new HaltCycle(
            3,
            List.of(TestSpecs.wave(
                    1, WaveSpec.Formation.SINGLE, TestSpecs.NEEDLER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)));

    /** A radio cue started by {@code trigger} with {@code subject}. */
    static RadioCue cue(CueTrigger trigger, String subject) {
        return new RadioCue(trigger, 0, subject, "varga", subject, false, "neutral");
    }

    /**
     * {@link HeldGatesTest#SECTIONS}' two gates: gate 0 held by two clamps, gate 1 by the piece arriving at 20.5 s;
     * {@code cycles} each gate's own; cues on gate 0's and the piece's halts and the piece's fall.
     */
    static LevelScript pieceLevel(List<Optional<HaltCycle>> cycles) {
        return LevelScript.builder(41, 6)
                .sections(HeldGatesTest.SECTIONS)
                .groundUnits(HeldGatesTest.holders().subList(0, 2))
                .setPieces(List.of(piece(20.5)))
                .objectives(ObjectiveSpec.NONE)
                .gateCycles(cycles)
                .radio(List.of(
                        cue(CueTrigger.GATE_HALT, "gate-1"),
                        cue(CueTrigger.GATE_HALT, HALO),
                        cue(CueTrigger.HALO_DESTROYED, "")))
                .build();
    }

    /** An archive core's hatch: a plain destructible of {@code hp} at ({@code t} s, {@code x}). */
    static GroundObjectSpec hatch(double t, double x, double hp) {
        return new GroundObjectSpec(
                t, x, new Hitbox(40, 40), hp, 0, Optional.empty(), 0, 0, "", false, -1, 0, Optional.empty());
    }

    /**
     * A 30 s level at {@code speed} px/s with a hatch per x at t = 0 (its core: 2 s within 40 px), the {@code files}
     * secondary for 100 credits (0: none) and the {@code file} cues for 1 and 2.
     */
    static LevelScript uplinkLevel(double speed, int files, double... xs) {
        List<GroundObjectSpec> hatches = new java.util.ArrayList<>();
        List<UplinkSpec> uplinks = new java.util.ArrayList<>();
        for (int i = 0; i < xs.length; i++) {
            hatches.add(hatch(0, xs[i], 30));
            uplinks.add(new UplinkSpec(i, 2, 40));
        }
        return LevelScript.builder(41, 6)
                .sections(List.of(new Section(30, speed)))
                .groundObjects(hatches)
                .uplinks(uplinks)
                .objectives(ObjectiveSpec.NONE.withSecondary(
                        files > 0 ? Secondary.files(files, 100, "FILES") : Secondary.NONE))
                .radio(List.of(cue(CueTrigger.FILE_IN, "1"), cue(CueTrigger.FILE_IN, "2")))
                .build();
    }

    /** Breaks the hatch of core {@code i} (its ground object) where it is now; false when it is not on the field. */
    static boolean breakHatch(Sortie sortie, int i) {
        int object = sortie.uplinks().hatch(i);
        for (int k = 0; k < sortie.ground.size(); k++) {
            if (sortie.ground.get(k).serial() == object) {
                sortie.groundPasses.demolish(k);
                return true;
            }
        }
        return false;
    }

    /** How many radio cues of index {@code cue} started in the last step. */
    static int radioed(Sortie sortie, int cue) {
        int n = 0;
        for (int e = 0; e < sortie.events().size(); e++) {
            n += sortie.events().type(e) == SimEvents.Type.RADIO
                            && sortie.events().value(e) == cue
                    ? 1
                    : 0;
        }
        return n;
    }
}
