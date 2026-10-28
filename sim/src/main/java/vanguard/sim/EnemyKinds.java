package vanguard.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * The distinct enemy kinds of a level (out of {@link EnemyForce}): the waves', the ground units', the extra ones (a
 * boss's streams), the halt cycle's and the waves' leaders, then what the spawners release and the chains' parts;
 * {@link Enemy#kind()} indexes the list.
 */
final class EnemyKinds {
    private EnemyKinds() {}

    /** The level's kinds, in their index order. */
    static List<EnemySpec> of(
            List<WaveSpec> waveSpecs,
            List<GroundUnit> groundUnits,
            List<EnemySpec> extraKinds,
            List<WaveSpec> haltSpecs) {
        List<EnemySpec> distinct = new ArrayList<>(
                waveSpecs.stream().map(WaveSpec::enemy).distinct().toList());
        for (GroundUnit unit : groundUnits) {
            if (!distinct.contains(unit.enemy())) {
                distinct.add(unit.enemy());
            }
        }
        for (EnemySpec extra : extraKinds) {
            if (!distinct.contains(extra)) {
                distinct.add(extra);
            }
        }
        // M9 part B: the halt cycle's units and the waves' leaders are kinds too.
        for (WaveSpec wave : haltSpecs) {
            if (!distinct.contains(wave.enemy())) {
                distinct.add(wave.enemy());
            }
        }
        for (List<WaveSpec> list : List.of(waveSpecs, haltSpecs)) {
            for (WaveSpec wave : list) {
                if (wave.leader().isPresent()
                        && !distinct.contains(wave.leader().get())) {
                    distinct.add(wave.leader().get());
                }
            }
        }
        // The units a spawner releases are among the kinds too (a brood, or M5 part C's periodic spawn).
        for (int k = 0; k < distinct.size(); k++) {
            EnemySpec spec = distinct.get(k);
            if (spec.brood().isPresent()
                    && !distinct.contains(spec.brood().get().enemy())) {
                distinct.add(spec.brood().get().enemy());
            }
            if (spec.spawner().isPresent()
                    && !distinct.contains(spec.spawner().get().enemy())) {
                distinct.add(spec.spawner().get().enemy());
            }
            // M9 part C: and the units a launcher launches (the Gilded Gunship's Darts).
            if (spec.launcher().isPresent()
                    && !distinct.contains(spec.launcher().get().unit())) {
                distinct.add(spec.launcher().get().unit());
            }
        }
        // So are a chain's segments, tail and regrown head.
        for (int k = 0; k < distinct.size(); k++) {
            EnemySpec spec = distinct.get(k);
            if (spec.chain().isPresent()) {
                EnemySpec.ChainSpec chain = spec.chain().get();
                // M9 part C: a train's turret car too.
                EnemySpec turret = chain.cars().isPresent() ? chain.cars().get().turret() : null;
                for (EnemySpec part : java.util.Arrays.asList(chain.segment(), chain.tail(), chain.regrown(), turret)) {
                    // M7 part C: a ground chain's regrown head may be null (it never regrows).
                    if (part != null && !distinct.contains(part)) {
                        distinct.add(part);
                    }
                }
            }
        }
        return List.copyOf(distinct);
    }

    /** Per kind, the kind a spawner (a brood, a periodic spawner) releases; -1 for others. */
    static int[] broods(List<EnemySpec> kinds) {
        int[] broodKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec spec = kinds.get(k);
            broodKinds[k] = spec.brood().isPresent()
                    ? kinds.indexOf(spec.brood().get().enemy())
                    : spec.spawner().isPresent()
                            ? kinds.indexOf(spec.spawner().get().enemy())
                            : -1;
        }
        return broodKinds;
    }
}
