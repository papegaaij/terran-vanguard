package vanguard.sim;

/**
 * M8 part A, the Sonar Pulse's numbers (design/player/specials, Sonar Pulse (L22)): a call is instant; a ring
 * expands from the ship to cover the whole play field in {@code ringSeconds} (the contacts ping as it reaches them),
 * and for {@code seconds} from the call the {@code sub} layer is open to every weapon and, under water, every weapon
 * does full damage ({@link Medium#sonar(boolean)}); a call while the window lasts restarts it (no stacking). The next
 * pulse can follow after {@code repeatSeconds}.
 */
public record SonarSpec(double ringSeconds, double seconds, double repeatSeconds) implements SpecialSpec.Effect {
    public SonarSpec {
        if (!(ringSeconds > 0) || !(seconds > 0) || !(repeatSeconds > 0)) {
            throw new IllegalArgumentException("a Sonar Pulse needs a ring time, a window and a repeat time");
        }
    }
}
