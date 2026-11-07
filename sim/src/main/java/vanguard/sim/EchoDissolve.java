package vanguard.sim;

/**
 * M11 part G, step G1 (design/campaign Level 49, the echoes; m11g-gaps.md §1 #4): an echo's forming and dissolve ({@link
 * EchoSpec}), kept by its {@link SetPiece} (made only for an echo's spec; {@link SetPiece#echo()}). It counts its
 * steps from its arrival (the forming) and from its kill (the death chain, then the dissolve, then the projectiles'
 * fade); the sortie acts on what it says is due ({@code ObjectivePasses#echoStep}). Its state is hashed with its piece's
 * (an echo's only). Allocates nothing once made.
 */
public final class EchoDissolve {
    private final EchoSpec spec;
    private final int formTicks;
    private final int deathTicks;
    private final int fadeTicks;
    /** Whether its forming was raised ({@link SimEvents.Type#ECHO_FORM}) in this attempt. */
    private boolean formed;
    /** Steps since its kill; -1 while it lives (or once its dissolve and fade are over). */
    private int since = -1;
    /** Whether it dissolved in this attempt (or was restored dead: an earlier fight's echo). */
    private boolean dissolved;

    EchoDissolve(EchoSpec spec, BossSpec boss) {
        this.spec = spec;
        formTicks = SimStep.ticks(spec.formSeconds());
        deathTicks = SimStep.ticks(boss.deathSeconds());
        fadeTicks = Math.max(1, SimStep.ticks(spec.fadeSeconds()));
    }

    /** Back to the level start (and its fight's checkpoint): not formed, alive. */
    void reset() {
        formed = false;
        since = -1;
        dissolved = false;
    }

    /** An earlier fight's echo restored dead (after {@link #reset}): long dissolved, nothing more to do. */
    void restoredDead() {
        formed = true;
        dissolved = true;
        since = -1;
    }

    /** Whether it forms now (the first step it is present): its {@link SimEvents.Type#ECHO_FORM}. */
    boolean formsNow(boolean present) {
        if (formed || !present) {
            return false;
        }
        formed = true;
        return true;
    }

    /**
     * One step after its piece acted, {@code destroyed} whether it is dead: the death chain's steps counted from the
     * kill; returns {@link #DISSOLVE} at the chain's end, {@link #FADE_END} at the projectiles' fade's end, else 0.
     */
    int step(boolean destroyed) {
        if (!destroyed || (dissolved && since < 0)) {
            return 0;
        }
        since++;
        if (!dissolved && since >= deathTicks) {
            dissolved = true;
            since = 0;
            return DISSOLVE;
        }
        if (dissolved && since >= fadeTicks) {
            since = -1;
            return FADE_END;
        }
        return 0;
    }

    /** {@link #step}: the dissolve is due now. */
    static final int DISSOLVE = 1;
    /** {@link #step}: the projectiles' fade ends now (the fading ones are freed). */
    static final int FADE_END = 2;

    /**
     * Whether an anchored echo still forms ({@code bossTicks} steps from its arrival): untouchable and silent until
     * {@link EchoSpec#formSeconds()} have passed.
     */
    boolean forming(int bossTicks) {
        return bossTicks < formTicks;
    }

    /** Its spec. */
    public EchoSpec spec() {
        return spec;
    }

    /** Whether it has formed in this attempt. */
    public boolean formed() {
        return formed;
    }

    /** Whether it has dissolved (its death chain over). */
    public boolean dissolved() {
        return dissolved;
    }

    /** How far its projectiles' fade has run from the dissolve, 1 to 0 (the presentation's alpha); 1 before. */
    public double fadeAlpha() {
        if (!dissolved || since < 0) {
            return dissolved ? 0 : 1;
        }
        return Math.max(0, 1 - (double) since / fadeTicks);
    }

    void addTo(StateHash hash) {
        hash.add(formed ? 1 : 0).add(since).add(dissolved ? 1 : 0);
    }
}
