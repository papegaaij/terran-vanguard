package vanguard.sim;

/**
 * M8 part A, the Auto-repair nanites (design/player/systems, Auto-repair nanites (L24)): a fitted module repairs one
 * armour point every {@code intervalSeconds} (its level's: 4 s, 2 s at L2) while the ship flies, but only up to {@code
 * capShare} of the armour's maximum; a hit pauses it for {@code pauseSeconds}; nothing repairs while the ship is
 * wrecked. Its HUD pip follows {@link Defences#repairing()}.
 */
public record NaniteSpec(double intervalSeconds, double capShare, double pauseSeconds) {
    public NaniteSpec {
        if (!(intervalSeconds > 0) || !(capShare > 0 && capShare <= 1) || !(pauseSeconds >= 0)) {
            throw new IllegalArgumentException("the nanites need an interval, a cap share in (0, 1] and a pause");
        }
    }
}
