package vanguard.sim;

/**
 * M10 part H, step H1b (design/enemies, Regeneration; design/enemies/bosses/ascendant, phase 2; m10h-gaps.md §1 #8): a
 * boss part that regenerates ({@link PartRegens}): once it has gone {@code delaySeconds} without a hit it refills
 * {@code rate} of its HP a second (the sacs 4 % after 1.5 s, the heart 2 % after 2.0 s at medium), never past it. Any
 * hit restarts the delay (a held beam or steady fire denies it); a boss is never stunned, so the EMP never stops it.
 *
 * @param part the part
 * @param rate its HP's share refilled a second (0.04)
 * @param delaySeconds the quiet spell before it refills, s
 */
public record PartRegen(int part, double rate, double delaySeconds) {
    public PartRegen {
        if (part < 0 || !(rate > 0) || !(delaySeconds >= 0)) {
            throw new IllegalArgumentException("a part regenerates at a rate after a delay: " + part);
        }
    }
}
