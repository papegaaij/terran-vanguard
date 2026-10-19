package vanguard.sim;

/**
 * M7 part A, the Orbital Lance's numbers (design/player/specials, Orbital Lance (L17)): a targeting line
 * at the ship's x for {@code lockSeconds}, then a column {@code width} px wide over the play field's
 * height for {@code seconds} and a {@code fadeSeconds} fade; its centre follows the ship's x at up to
 * {@code follow} px/s from the call on. While it burns it deals {@code dps} per second to every enemy
 * whose hit box overlaps it (hardened included, never the {@code sub} layer) and {@code bossPartDps} to
 * each boss part, at most {@code bossPartCap} per part per lance. The next lance can be called
 * {@code repeatSeconds} after the fade.
 */
public record LanceSpec(
        double lockSeconds,
        double width,
        double seconds,
        double fadeSeconds,
        double follow,
        double dps,
        double bossPartDps,
        double bossPartCap,
        double repeatSeconds)
        implements SpecialSpec.Effect {
    public LanceSpec {
        if (!(width > 0) || !(seconds > 0) || !(follow > 0) || !(dps > 0)) {
            throw new IllegalArgumentException("an Orbital Lance needs a width, a time, a follow speed and a DPS");
        }
        if (lockSeconds < 0 || fadeSeconds < 0 || repeatSeconds < 0 || bossPartDps < 0 || bossPartCap < 0) {
            throw new IllegalArgumentException("an Orbital Lance's times and boss numbers are 0 or more");
        }
    }
}
