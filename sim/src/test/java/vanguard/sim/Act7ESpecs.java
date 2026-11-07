package vanguard.sim;

import java.util.Optional;

/**
 * M11 part E, step E1: the Level 47 enemy-side fixtures, after the approved docs (design/enemies/air/choir-seraph; the
 * Level 47 waves; m11e §1 #1–#5) at medium unless named. The content's own fixtures are step E2's.
 */
final class Act7ESpecs {
    private Act7ESpecs() {}

    /** The Seraph's cycle: in by its ring (1.0 s), a 0.3 s fold, mirror-row arrivals, out by a jump after 0.8 s, 40 px. */
    static final EnemySpec.Teleport.Cycle CYCLE = new EnemySpec.Teleport.Cycle(
            true, 1.0, 0.3, EnemySpec.Teleport.Arrival.MIRROR_ROW, EnemySpec.Teleport.Exit.JUMP, 0.8, 40);

    /**
     * The Seraph's teleport: fade 0.2 s, later rings 0.5 s, never within 140 px of the ship, {@code mirror} s mirroring
     * at each arrival, {@code cycles} cycles (an entry and cycles − 1 jumps), at most {@code rings} rings.
     */
    static EnemySpec.Teleport jumps(double mirror, int cycles, int rings) {
        return new EnemySpec.Teleport(
                        0.2, 0.5, 140, new Range(90, 130), new Range(0, 0), 0, mirror, cycles - 1, 0.25, rings)
                .withCycle(CYCLE);
    }

    /** A Choir Seraph of {@code hp} mirroring {@code mirror} s for {@code cycles} cycles, the ring cap {@code rings}. */
    static EnemySpec seraph(double hp, double mirror, int cycles, int rings) {
        return EnemySpec.of("choir-seraph", hp, new Hitbox(48, 52), Layer.AIR, 15, false, 35, 260)
                .withGun(Optional.of(Act7ASpecs.SPIRAL))
                .withTeleport(Optional.of(jumps(mirror, cycles, rings)))
                .withShadow(Optional.of(Act7ASpecs.SHADOW));
    }

    /** Medium: HP 90, mirror 2.5 s, 4 cycles. */
    static final EnemySpec SERAPH = seraph(90, 2.5, 4, 4);
    /** Easy: mirror 2.0 s, 3 cycles. */
    static final EnemySpec SERAPH_EASY = seraph(68, 2.0, 3, 4);
    /** Hard: mirror 3.0 s, 5 cycles. */
    static final EnemySpec SERAPH_HARD = seraph(117, 3.0, 5, 4);

    /** A pair of {@code spec} from the front at {@code t} s, on {@code rows} (empty: drawn per landing). */
    static WaveSpec pair(double t, EnemySpec spec, Optional<WaveSpec.Rows> rows) {
        return WaveSpec.builder(t, WaveSpec.Formation.PAIR, spec, 2, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .rows(rows)
                .build();
    }

    /** The Choir Herald with an aura of {@code radius} px (medium 90, easy 75, hard 105). */
    static EnemySpec herald(double radius) {
        return SiegeSpecs.HERALD.withAura(Optional.of(new EnemySpec.Aura(radius, 40, 0.25, 2.0, 0.5, 1.0, 0.3, 0.8)));
    }

    /** A hovering single of {@code spec} at its station (x, depth below the top edge) for {@code hold} s. */
    static WaveSpec station(double t, EnemySpec spec, double x, double depth, double hold) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, spec, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .at(Optional.of(new WaveSpec.At(x, depth)))
                .holdSeconds(Optional.of(hold))
                .build();
    }

    /** The units of {@code slug} in the sortie now, in their pool order (a pair's by its place). */
    static Enemy[] all(Sortie sortie, String slug) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            n += sortie.enemy(i).spec().slug().equals(slug) ? 1 : 0;
        }
        Enemy[] found = new Enemy[n];
        for (int i = 0, k = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                found[k++] = sortie.enemy(i);
            }
        }
        java.util.Arrays.sort(found, java.util.Comparator.comparingInt((Enemy e) -> e.teleport()
                .place()));
        return found;
    }
}
