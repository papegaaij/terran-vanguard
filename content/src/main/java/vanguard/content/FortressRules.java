package vanguard.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.HaltCycle;
import vanguard.sim.UplinkSpec;
import vanguard.sim.WaveSpec;

/**
 * M10 part G's rules for the simulation (design/campaign Level 41 Callisto Fortress; design/enemies/ground/shield-pylon,
 * The pylon gates; design/enemies/ground/halo-platform): each held gate's own halt cycle ({@code halt_cycle} on its
 * section) and the archive cores' uplinks (a destructible's {@code uplink}: its hatch among the level's ground objects,
 * the upload's time on the difficulty and its radius). The held set piece (the boss block's {@code kind: held}) and its
 * guns' spoofer flag are {@link BossRules}'; the files secondary {@link LevelRules}'.
 */
final class FortressRules {
    private FortressRules() {}

    /**
     * The held gates' own halt cycles at {@code difficulty}, one per arena section in the level's order (empty for a gate
     * without one); an empty list when no section has one (every level but Level 41: Level 29's one {@code halt_waves}
     * stays the level's).
     */
    static List<Optional<HaltCycle>> gateCycles(
            Content content,
            LevelData level,
            Difficulty difficulty,
            Map<String, LevelData.EnemyChange> enemyChanges,
            UnitRules.InLevel inLevel) {
        if (level.sections().stream().noneMatch(section -> section.haltCycle().isPresent())) {
            return List.of();
        }
        List<Optional<HaltCycle>> cycles = new ArrayList<>();
        for (LevelData.Section section : level.sections()) {
            if (!section.isArena()) {
                continue;
            }
            cycles.add(section.haltCycle().map(halt -> {
                List<WaveSpec> waves = new ArrayList<>();
                for (LevelData.Wave wave : halt.waves()) {
                    if (wave.fliesOn(difficulty)) {
                        WaveRules.addWave(content, level, wave, difficulty, enemyChanges, List.of(), waves, inLevel);
                    }
                }
                return new HaltCycle(halt.every(), waves);
            }));
        }
        return cycles;
    }

    /** A destructible's placement: when it enters and its uplink, if it is an archive core's hatch. */
    private record Placed(double t, Optional<LevelData.UplinkData> uplink) {}

    /**
     * The level's archive cores at {@code difficulty}: each destructible with an {@code uplink}, its hatch's index among
     * the ground objects ({@link GroundRules#groundObjects}: the destructibles' placements in data order, sorted by their
     * entry), in the cores' entry order; none in every other level.
     */
    static List<UplinkSpec> uplinks(LevelData level, Difficulty difficulty) {
        List<Placed> placed = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isPresent()) {
                continue;
            }
            for (LevelData.Placement placement : target.at()) {
                placed.add(new Placed(placement.t(), target.uplink()));
            }
        }
        if (placed.stream().allMatch(p -> p.uplink().isEmpty())) {
            return List.of();
        }
        // The same stable sort as the ground objects' (by entry), so the indexes match.
        List<Placed> sorted = new ArrayList<>(placed);
        sorted.sort(Comparator.comparingDouble(Placed::t));
        List<UplinkSpec> uplinks = new ArrayList<>();
        for (int k = 0; k < sorted.size(); k++) {
            Optional<LevelData.UplinkData> uplink = sorted.get(k).uplink();
            if (uplink.isPresent()) {
                uplinks.add(new UplinkSpec(
                        k, uplink.get().secondsOn(difficulty), uplink.get().radius()));
            }
        }
        return uplinks;
    }
}
