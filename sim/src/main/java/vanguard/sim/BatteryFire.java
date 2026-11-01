package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, Fire rules: the batteries' bursts; m10h-gaps.md §1 #5): {@code
 * parts} fire {@code attack}'s aimed burst (its gun's burst, its {@link BossSpec.Attack#burstGapSeconds()} apart) on
 * their gun's interval each ({@link BatteryBursts}), each after a {@code tellSeconds} tell (the panel's red rim), only
 * while the part's mirror panel ({@link PartMirror}) faces within its fire angle of the ship; at most {@code atOnce}
 * telling or bursting together, their tells at least {@code apartSeconds} apart. A part without a panel fires whenever
 * it is due.
 *
 * @param parts the bursting parts (the four batteries)
 * @param attack the aimed attack they fire, an index into {@link BossSpec#attacks()}
 * @param atOnce the most telling or bursting at once (2)
 * @param apartSeconds the least time between two tells' starts (0.6 s)
 * @param tellSeconds the tell before each burst (0.5 s)
 */
public record BatteryFire(List<Integer> parts, int attack, int atOnce, double apartSeconds, double tellSeconds) {
    public BatteryFire {
        parts = List.copyOf(parts);
        if (parts.isEmpty() || attack < 0 || atOnce < 1 || !(apartSeconds >= 0) || !(tellSeconds >= 0)) {
            throw new IllegalArgumentException("a battery fire has parts, an attack, a limit, a spacing and a tell");
        }
    }
}
