package vanguard.sim;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks and The lifeboat; m10h-gaps.md §1 #10, #11,
 * §4): a boss that ends without dying ({@link EscapeRun}). When its {@code breaksOn} part dies (K: the Ascendant's
 * heart) its hull breaks: every other part still alive bursts and pays, the hull is scenery from then on (no hit, no
 * contact), the flash comes {@code flashSeconds} later and the lifeboat ({@code part}) breaks out {@code
 * breakoutSeconds} after K at ({@code fromX}, {@code fromY}). It climbs to beyond the top edge in {@code climbSeconds},
 * its climb's speed easing up in the ratio {@code speedFrom} : {@code speedTo} and its path weaving up to {@code weave}
 * px either side on a seeded path, never below {@code floorY}. It cannot die: every hit's damage counts toward {@code
 * cap} (after the specials' own part caps), its bounty paid pro rata as the damage comes in, the whole of it at the cap;
 * past the cap hits spark. It drops a {@code wake} pair of spores every {@code wakeSeconds} ({@code wakeSpeed} px/s
 * straight down, {@code wakeDamage} each, {@code wakeGap} px apart) and its contact deals {@code contactDamage} once.
 * At the top edge it has escaped: the fight is over ({@link SimEvents.Type#BOSS_ESCAPED}).
 *
 * <p>Places are simulation px (y up). An escaping boss has no vital part: the {@code breaksOn} part's death ends its
 * hull instead.
 *
 * @param part the lifeboat, the boss's part that escapes (out of the bar; its own escape bar)
 * @param breaksOn the part whose death breaks the hull (K)
 * @param flashSeconds the hull's flash after K (the act boss's screen flash and credit shower), s
 * @param breakoutSeconds the lifeboat's breakout after K, s
 * @param fromX its breakout place, px from the left
 * @param fromY its breakout place, px up
 * @param climbSeconds its climb from the breakout to beyond the top edge, s (14; hard 12)
 * @param speedFrom its climb's speed at the start (20 px/s): the ease's first share
 * @param speedTo its climb's speed at the top (90 px/s): the ease's last share
 * @param weave how far its path swings either side of its start, px (60)
 * @param floorY the lowest it ever flies, px up
 * @param cap the damage it takes in all (1,400; easy 1,050)
 * @param wakeSeconds between two wake pairs, s (0.8)
 * @param wakeSpeed the wake spores' drift down, px/s (60)
 * @param wakeDamage a wake spore's damage ({@code small} 4)
 * @param wakeGap the pair's spores apart, px
 * @param contactDamage its contact's damage, once ({@code large} 20)
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
        double cap,
        double wakeSeconds,
        double wakeSpeed,
        double wakeDamage,
        double wakeGap,
        double contactDamage) {
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
                || !(fromY >= floorY)
                || !(cap > 0)
                || !(wakeSeconds > 0)
                || !(wakeSpeed > 0)
                || !(wakeDamage >= 0)
                || !(wakeGap >= 0)
                || !(contactDamage >= 0)) {
            throw new IllegalArgumentException("an escape breaks on a part, then its lifeboat climbs away under a cap: "
                    + part + ", " + breaksOn + ", " + climbSeconds + ", " + cap);
        }
    }
}
