package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 part B, step B1: the Level 43/44 sim fixtures (design/world/vrell-space, gate transit; design/campaign Levels 43
 * and 44, radio), beside part A's {@link Act7ASpecs}.
 */
final class Act7BSpecs {
    private Act7BSpecs() {}

    /** The transit of a 16 s level: formation at 10 s, the 1.5 s warp from 14.5 s, the level's end at 16 s. */
    static final VrellLevel.Transit TRANSIT = new VrellLevel.Transit(10, 14.5, 1.5);

    /** A 16 s level with {@code waves} and the {@link #TRANSIT} (or none). */
    static LevelScript transitLevel(List<WaveSpec> waves, Optional<VrellLevel.Transit> transit) {
        return Act6ASpecs.level(16, List.of(), waves).toBuilder()
                .vrell(VrellLevel.NONE.withTransit(transit))
                .build();
    }

    /** A sortie of {@code level} with Rook on the left, the ship hit as in play. */
    static Sortie withRook(LevelScript level) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    /** How often radio cue {@code cue} started in the sortie's last step. */
    static int started(Sortie sortie, int cue) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int e = 0; e < events.size(); e++) {
            n += events.type(e) == SimEvents.Type.RADIO && events.value(e) == cue ? 1 : 0;
        }
        return n;
    }

    /** The index of the first unit of {@code slug} in the sortie now; -1 for none. */
    static int index(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return i;
            }
        }
        return -1;
    }
}
