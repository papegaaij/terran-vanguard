package vanguard.sim;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks and The lifeboat; m10h-gaps.md §1 #10, #11,
 * §4): a boss that ends without dying ({@link EscapeRun}). When its {@code breaksOn} part dies (K: the Ascendant's
 * heart) its hull breaks: every other part still alive bursts and pays, the hull is scenery from then on (no hit, no
 * contact), the flash comes {@code flashSeconds} later and the lifeboat ({@code part}) breaks out {@code
 * breakoutSeconds} after K at ({@code fromX}, {@code fromY}), hangs there {@code holdSeconds} (round 81: a moment torn
 * free before it runs) and then climbs to beyond the top edge in {@code climbSeconds},
 * its climb's speed easing up in the ratio {@code speedFrom} : {@code speedTo} and its path weaving up to {@code weave}
 * px either side on a seeded path, never below {@code floorY}. Round 80 (B.1/A.4): the escape is a short scene; the
 * lifeboat is scenery (no hit, no pay, no fire, no contact: its part is fire-only and never on the field). At the top
 * edge it has escaped: the fight is over ({@link SimEvents.Type#BOSS_ESCAPED}).
 *
 * <p>Places are simulation px (y up). An escaping boss has no vital part: the {@code breaksOn} part's death ends its
 * hull instead.
 *
 * @param part the lifeboat, the boss's part that escapes (fire-only: out of the bar, never hit)
 * @param breaksOn the part whose death breaks the hull (K)
 * @param flashSeconds the hull's flash after K (the act boss's screen flash and credit shower), s
 * @param breakoutSeconds the lifeboat's breakout after K, s
 * @param fromX its breakout place, px from the left
 * @param fromY its breakout place, px up
 * @param climbSeconds its climb from the end of its hold to beyond the top edge, s
 * @param speedFrom its climb's speed at the start: the ease's first share
 * @param speedTo its climb's speed at the top: the ease's last share
 * @param weave how far its path swings either side of its start, px
 * @param floorY the lowest it ever flies, px up
 * @param holdSeconds how long it hangs at its breakout place before the climb, s (0: it climbs at once)
 */
public record EscapeSpec(
        int part,
        int breaksOn,
        double flashSeconds,
        double breakoutSeconds,
        double fromX,
        double fromY,
        double climbSeconds,
        double speedFrom,
        double speedTo,
        double weave,
        double floorY,
        double holdSeconds) {
    public EscapeSpec {
        if (part < 0
                || breaksOn < 0
                || part == breaksOn
                || !(flashSeconds >= 0)
                || !(breakoutSeconds >= flashSeconds)
                || !(climbSeconds > 0)
                || !(speedFrom > 0)
                || !(speedTo >= speedFrom)
                || !(weave >= 0)
                || !(holdSeconds >= 0)
                || !(fromY >= floorY)) {
            throw new IllegalArgumentException("an escape breaks on a part, then its lifeboat climbs away: " + part
                    + ", " + breaksOn + ", " + climbSeconds);
        }
    }
}
