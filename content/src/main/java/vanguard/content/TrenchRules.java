package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.WaveSpec;

/**
 * M8 part E's rules for the simulation (design/campaign Level 25 Siren Trench, design/enemies/naval/siren and
 * spiral-nautilus; m8e-gaps.md §1.1–1.4, §5 E1–E9, pending the user's review in round 54): the Siren's slowing ring (a
 * {@code ring} attack with its {@code slow}: sound, not bullets, its speed not scaled by the bullet-speed lever), the
 * Spiral Nautilus's {@code coil} with the difficulty's open window (its {@code spiral} fired while open is {@link
 * StormRules}'), and a wave's mount: out of a cave ({@code cave}: its notch) or nests on the walls' feet ({@code wall},
 * entering at the front with the ground). The walls' notches are {@link TerrainRules#terrain}'s.
 */
final class TrenchRules {
    private TrenchRules() {}

    /** {@code spec} with the stat block's coil and slowing ring on {@code difficulty}; a unit without them as it is. */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        EnemySpec out = spec;
        Optional<EnemyData.Coil> coil = enemy.coil(difficulty);
        if (coil.isPresent()) {
            EnemyData.Coil c = coil.get();
            out = out.withCoil(Optional.of(new EnemySpec.Coil(
                    c.outer(), c.inner(), c.closing(), c.opening(), c.tell(), c.open(), c.openSpeed(), c.offset())));
        }
        Optional<EnemyData.Attack> ring = enemy.slowRing();
        if (ring.isPresent()) {
            EnemyData.Attack attack = ring.get();
            EnemyData.Slow slow = enemy.slow(difficulty).orElseThrow();
            out = out.withSlowRing(Optional.of(new EnemySpec.SlowRing(
                    attack.firstShotDelay().orElse(0.0),
                    UnitRules.interval(content, attack, UnitRules.hook(enemy, difficulty), difficulty),
                    attack.tell().orElseThrow(),
                    attack.speed().orElseThrow(),
                    slow.radius(),
                    slow.band(),
                    slow.factor(),
                    slow.seconds(),
                    slow.immunity())));
        }
        return out;
    }

    /** The simulation's entry of {@code wave}: a wall's nests come in with the ground at the front. */
    static WaveSpec.Entry entry(LevelData.Wave wave, WaveSpec.Entry entry) {
        return wave.onWalls() ? WaveSpec.Entry.FRONT : entry;
    }

    /** {@code spec} with {@code wave}'s mount: out of its cave's notch, or nests on the walls; as it is without one. */
    static WaveSpec mount(LevelData.Wave wave, WaveSpec spec) {
        if (wave.cave().isPresent()) {
            return spec.withCave(wave.cave().get());
        }
        if (wave.onWalls()) {
            return spec.withWall(wave.opposite());
        }
        return spec;
    }
}
