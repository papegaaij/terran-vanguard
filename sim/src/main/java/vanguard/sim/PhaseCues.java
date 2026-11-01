package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, Vorne; m10h-gaps.md §1 #13): a boss's radio subjects raised in
 * its phases ({@link PhaseCue}), each once: in its phase, at the first of its triggers (deaths among its parts, their
 * HP below a share, a time into the phase), the {@code boss-phase} cue with its subject ({@link BossActions#bossCue})
 * and {@link SimEvents.Type#BOSS_CUE} (at the boss, value the cue's index). Kept by its {@link SetPiece} when its spec
 * has any; hashed only then. Allocates nothing once made.
 */
public final class PhaseCues {
    private final SetPiece piece;
    private final List<PhaseCue> cues;
    private final boolean[] fired;

    PhaseCues(SetPiece piece, BossSpec boss) {
        this.piece = piece;
        cues = boss.cues();
        fired = new boolean[cues.size()];
    }

    /** Whether {@code boss} has phase cues (then its set piece keeps a {@link PhaseCues}). */
    static boolean any(BossSpec boss) {
        return boss != null && !boss.cues().isEmpty();
    }

    /** Back to the level start (and the boss checkpoint): none raised. */
    void reset() {
        java.util.Arrays.fill(fired, false);
    }

    /** One engaged step: each cue of the current phase not raised yet is raised when one of its triggers holds. */
    void step(BossActions actions) {
        int phase = piece.phase();
        for (int i = 0; i < cues.size(); i++) {
            PhaseCue cue = cues.get(i);
            if (fired[i] || cue.phase() != phase || !due(cue)) {
                continue;
            }
            fired[i] = true;
            actions.ring(SimEvents.Type.BOSS_CUE, piece.x(), piece.y(), i);
            actions.bossCue(cue.subject());
        }
    }

    private boolean due(PhaseCue cue) {
        List<Integer> parts = cue.parts();
        int dead = 0;
        double left = 0;
        double full = 0;
        for (int k = 0; k < parts.size(); k++) {
            int p = parts.get(k);
            dead += piece.partHp(p) > 0 ? 0 : 1;
            left += Math.max(0, piece.partHp(p));
            full += piece.spec().parts().get(p).hp();
        }
        return (cue.deaths() > 0 && dead >= cue.deaths())
                || (cue.below() > 0 && left < cue.below() * full)
                || (cue.seconds() > 0 && piece.phaseSeconds() >= cue.seconds() - 1e-9);
    }

    /** Whether cue {@code i} was raised in this attempt. */
    public boolean fired(int i) {
        return fired[i];
    }

    void addTo(StateHash hash) {
        for (boolean f : fired) {
            hash.add(f ? 1 : 0);
        }
    }
}
