package vanguard.sim;

/**
 * M7 part A, the Evasive thrusters' dash (design/player/systems, Evasive thrusters (Act 3)): a dash
 * of {@code distance} px over {@code seconds} in one of 8 directions; the ship cannot be hurt for
 * {@code invulnerableSeconds} from its start (enemy bullets pass through it); the next dash can follow
 * {@code cooldownSeconds} after its start. A double-tap is a second press of a direction within
 * {@code doubleTapSeconds} of the first press's release, nothing else pressed in between.
 */
public record ThrusterSpec(
        double distance, double seconds, double invulnerableSeconds, double cooldownSeconds, double doubleTapSeconds) {
    public ThrusterSpec {
        if (!(distance > 0) || !(seconds > 0)) {
            throw new IllegalArgumentException("a dash needs a distance and a time");
        }
        if (invulnerableSeconds < 0 || cooldownSeconds < 0 || doubleTapSeconds < 0) {
            throw new IllegalArgumentException("a dash's times are 0 s or more");
        }
    }
}
