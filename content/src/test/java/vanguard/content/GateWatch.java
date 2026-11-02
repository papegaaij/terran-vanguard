package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.sim.HaltCycle;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part G, step G2 (design/campaign Level 41, the halts and the clock): a held-gates level's gates over one run of the
 * autopilot: per gate its halt (real seconds from {@code GATE_HALTED} to {@code GATE_OPENED}; open if it never opened),
 * its own halt cycle's waves that came in that time, the files taken in it, and for a gate held by a set piece (the
 * Halo Platform) the order its parts fell in and when (s into the halt). The level report's {@code gates} part.
 */
final class GateWatch implements LevelMetrics.Probe {
    /** A gate: its halt in s (NaN: never halted; its open time unknown: {@code opened} false), waves, files, parts. */
    record Gate(int gate, double halt, boolean opened, int waves, int files, List<String> fallen) {
        @Override
        public String toString() {
            String parts = fallen.isEmpty() ? "" : "; fell: " + String.join(", ", fallen);
            return Double.isNaN(halt)
                    ? "gate " + (gate + 1) + " never halted"
                    : String.format(
                            Locale.ROOT,
                            "gate %d %.1f s%s, %d cycle waves, %d file%s%s",
                            gate + 1,
                            halt,
                            opened ? "" : " (not opened)",
                            waves,
                            files,
                            files == 1 ? "" : "s",
                            parts);
        }
    }

    /** A run: its seed, the gates, the files in by the end and whether the level was won. */
    record Gates(long seed, List<Gate> gates, int files, boolean won) {
        @Override
        public String toString() {
            StringBuilder out = new StringBuilder("seed " + seed + (won ? "" : " (not won)") + ": ");
            for (int g = 0; g < gates.size(); g++) {
                out.append(g == 0 ? "" : "; ").append(gates.get(g));
            }
            return out.append("; files ").append(files).toString();
        }
    }

    private final long seed;
    private final LevelScript level;
    private final int[] halted;
    private final int[] opened;
    private final int[] files;
    private final List<List<String>> fallen = new ArrayList<>();
    private int step;
    private int lastGate = -1;
    private int filesIn;

    private GateWatch(long seed, LevelScript level, int gates) {
        this.seed = seed;
        this.level = level;
        halted = new int[gates];
        opened = new int[gates];
        files = new int[gates];
        java.util.Arrays.fill(halted, -1);
        java.util.Arrays.fill(opened, -1);
        for (int g = 0; g < gates; g++) {
            fallen.add(new ArrayList<>());
        }
    }

    /** A retry (from the start or the Halo's checkpoint) flies the gates again: the last attempt's are kept. */
    @Override
    public void newAttempt() {
        java.util.Arrays.fill(halted, -1);
        java.util.Arrays.fill(opened, -1);
        java.util.Arrays.fill(files, 0);
        fallen.forEach(List::clear);
        lastGate = -1;
        filesIn = 0;
    }

    /** A retry from the Halo's checkpoint: the gates before it stand as flown. */
    @Override
    public void bossRetry() {
        int g = lastGate;
        if (g >= 0) {
            halted[g] = -1;
            opened[g] = -1;
            fallen.get(g).clear();
        }
    }

    @Override
    public void stepped(Sortie sortie, int index) {
        step = index;
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            SimEvents.Type type = events.type(e);
            int value = events.value(e);
            if (type == SimEvents.Type.GATE_HALTED && value >= 0 && value < halted.length && halted[value] < 0) {
                halted[value] = index;
                lastGate = value;
            } else if (type == SimEvents.Type.GATE_OPENED && value >= 0 && value < opened.length) {
                opened[value] = index;
            } else if (type == SimEvents.Type.FILE_IN) {
                filesIn = Math.max(filesIn, value);
                if (lastGate >= 0) {
                    files[lastGate]++;
                }
            } else if (type == SimEvents.Type.PART_DESTROYED && lastGate >= 0 && level.gatePiece(lastGate) >= 0) {
                SetPieceSpec piece = level.setPieces().get(level.gatePiece(lastGate));
                String name = value >= 0 && value < piece.parts().size()
                        ? piece.parts().get(value).name()
                        : "part " + value;
                fallen.get(lastGate)
                        .add(String.format(
                                Locale.ROOT, "%s %.1f s", name, (index - halted[lastGate]) * SimStep.SECONDS));
            }
        }
    }

    private Gates gates(boolean won) {
        List<Gate> out = new ArrayList<>();
        List<Optional<HaltCycle>> cycles = level.gateCycles();
        for (int g = 0; g < halted.length; g++) {
            boolean open = opened[g] >= 0;
            double halt = halted[g] < 0 ? Double.NaN : ((open ? opened[g] : step) - halted[g]) * SimStep.SECONDS;
            int waves = 0;
            if (!Double.isNaN(halt) && g < cycles.size() && cycles.get(g).isPresent()) {
                waves = flown(cycles.get(g).get(), halt);
            }
            out.add(new Gate(g, halt, open, waves, files[g], List.copyOf(fallen.get(g))));
        }
        return new Gates(seed, out, filesIn, won);
    }

    /** The cycle's waves that come in a halt of {@code seconds} (it repeats every its period; cut at the gate's fall). */
    static int flown(HaltCycle cycle, double seconds) {
        int n = 0;
        for (double start = 0; start < seconds; start += cycle.everySeconds()) {
            for (vanguard.sim.WaveSpec wave : cycle.waves()) {
                n += start + wave.t() < seconds ? 1 : 0;
            }
        }
        return n;
    }

    /** The level's held gates (its arena sections) when it has several; 0 otherwise. */
    static int gates(LevelScript level) {
        long arenas =
                level.sections().stream().filter(vanguard.sim.Section::arena).count();
        return arenas > 1 && !level.gateCycles().isEmpty() ? (int) arenas : 0;
    }

    /** Flies {@code key} with the autopilot and {@code loadout} on {@code seed} and returns its gates. */
    static Gates fly(Content content, String key, vanguard.sim.Loadout loadout, Difficulty difficulty, long seed) {
        LevelScript level = SimSpecs.level(content, key, difficulty);
        Sortie sortie = LevelMetrics.sortie(content, key, difficulty, loadout, seed);
        GateWatch watch = new GateWatch(seed, level, gates(level));
        return watch.gates(LevelMetrics.fly(sortie, LevelReport.AUTOPILOT_STEPS, watch));
    }
}
