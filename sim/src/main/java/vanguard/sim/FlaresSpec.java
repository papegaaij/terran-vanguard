package vanguard.sim;

/**
 * M8 part A, the Decoy Flares' numbers (design/player/specials, Decoy Flares (L27)): a call launches {@code count}
 * flares from the rear muzzle in a {@code fanDegrees} fan backwards (down the screen), each starting at {@code speed}
 * px/s (× the water's bullet speed under water) and slowing to a stop over {@code driftSeconds}; each burns for
 * {@code burnSeconds}, and while any burns every enemy homing missile steers at the nearest burning flare instead of
 * the ship and bursts on it ({@link Flares}). The next launch can follow after {@code repeatSeconds}.
 */
public record FlaresSpec(
        int count, double fanDegrees, double speed, double driftSeconds, double burnSeconds, double repeatSeconds)
        implements SpecialSpec.Effect {
    public FlaresSpec {
        if (count < 1 || !(fanDegrees >= 0 && fanDegrees < 360)) {
            throw new IllegalArgumentException("the flares need a count and a fan below 360°");
        }
        if (!(speed >= 0) || !(driftSeconds > 0) || !(burnSeconds > 0) || !(repeatSeconds > 0)) {
            throw new IllegalArgumentException("the flares need a speed, a drift, a burn and a repeat time");
        }
    }
}
