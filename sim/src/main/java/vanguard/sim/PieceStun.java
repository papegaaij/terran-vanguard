package vanguard.sim;

/**
 * M10 part I, step I1 (design/enemies/ground/halo-platform, Behaviour "EMP"; Level 41): a held set piece's EMP stun.
 * The simulation never stuns a boss-engine piece (step G1b); a {@link BossSpec#held() held} one (the Halo Platform, a
 * machine) is the exception its document names: the EMP Burst's ring stuns it for the machine stun, restarted (not
 * added up) by a second burst. While stunned its turrets and its open core hold fire (no tells, no shots, no rail line:
 * a running line is cancelled, the spoofer's held volleys are lost), its ring keeps turning and its barrier stays. Kept
 * by its {@link SetPiece} only for a held piece; allocates nothing; hashed only while it runs.
 */
final class PieceStun {
    /** Steps left; 0 when it is not stunned. */
    private int ticks;

    /** The EMP Burst's ring reached it: stunned {@code stunTicks} steps from now (a running stun restarts). */
    void stun(int stunTicks) {
        ticks = Math.max(0, stunTicks);
    }

    /** One step of the stun. */
    void step() {
        if (ticks > 0) {
            ticks--;
        }
    }

    /** Whether it holds fire now. */
    boolean stunned() {
        return ticks > 0;
    }

    /** Steps left of the stun (0 when not stunned). */
    int ticks() {
        return ticks;
    }

    /** Back to the level start (and the checkpoint): not stunned. */
    void reset() {
        ticks = 0;
    }

    void addTo(StateHash hash) {
        if (ticks > 0) {
            hash.add(ticks);
        }
    }
}
