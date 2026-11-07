package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 part G, step G2 (content's bridge, pending a sim step): {@link BreachDraw} is package-private in the simulation,
 * so content builds a breach draw's rules here, in the simulation's package. A later sim step makes {@link
 * BreachDraw.Spec} public and content builds it itself (then this class goes).
 */
public final class EchoDraws {
    private EchoDraws() {}

    /** {@code breach} drawing its points from {@code points} by the rules given (BreachDraw.Spec); unchanged if empty. */
    public static BossSpec.Breach withDraw(
            BossSpec.Breach breach,
            Optional<List<BreachArena.Pose>> points,
            int rearEvery,
            int eruptionEvery,
            boolean eruptionNotFirst) {
        return points.map(list -> breach.withDraw(
                        Optional.of(new BreachDraw.Spec(list, true, rearEvery, eruptionEvery, eruptionNotFirst))))
                .orElse(breach);
    }
}
