package vanguard.sim;

import java.util.List;

/**
 * M11 part H, step H1b (design/enemies/bosses/choir-heart, Armour / shield: the membranes; design/enemies, Window
 * shields; m11h-gaps.md §1 #1): a boss's part windows ({@link PartWindows}): each of {@code parts} is closed
 * (invulnerable, no target for Rook, the Swarm Call or the seekers) except while it sings (from the first frame of its
 * playbook move's tell to the move's last bullet: {@link BossPlaybook}), while a {@code shield-breaker} hit's hold lasts
 * ({@code breakerSeconds}, the Tesla arc: that part) and while the EMP Burst's lasts ({@code empSeconds}: every part).
 *
 * @param parts the windowed parts (the membranes)
 * @param breakerSeconds a breaker hit's hold on the part it struck, s (3)
 * @param empSeconds the EMP Burst's hold on every windowed part, s (3)
 */
public record PartWindowsSpec(List<Integer> parts, double breakerSeconds, double empSeconds) {
    public PartWindowsSpec {
        parts = List.copyOf(parts);
        if (parts.isEmpty() || !(breakerSeconds >= 0) || !(empSeconds >= 0)) {
            throw new IllegalArgumentException("part windows hold parts and a breaker's and the EMP's hold: " + parts);
        }
        for (int i = 0; i < parts.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (parts.get(i).equals(parts.get(j))) {
                    throw new IllegalArgumentException("a part has one window: " + parts.get(i));
                }
            }
        }
    }

    /** Checks its parts against the boss's {@code parts}. */
    void checkParts(String slug, int count) {
        for (int p : parts) {
            BossSpec.checkPart(slug, "a part window", p, count);
        }
    }
}
