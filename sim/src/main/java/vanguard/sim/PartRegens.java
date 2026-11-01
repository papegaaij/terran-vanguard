package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1b (design/enemies, Regeneration; design/enemies/bosses/ascendant, phase 2; m10h-gaps.md §1 #8): a
 * boss's regenerating parts ({@link PartRegen}), Part A's {@link Regenerating} rule on a set piece's parts. A part's
 * quiet spell is its hit clock ({@link SetPiece#partTicksSinceHit}): every hit restarts it, the ones a shield or a
 * sealed part turned away too, so a held beam or steady fire denies the healing. After its delay a living part refills
 * its rate of its HP a second, never past it; a dead part stays dead. Stepped on the boss's engaged steps (a boss is
 * never stunned: the EMP never stops it). Healed HP pays nothing (a part pays once, at its death).
 *
 * <p>Its events, at the part: {@link SimEvents.Type#PART_HEALING} as the healing starts and {@link
 * SimEvents.Type#PART_HEALING_STOPPED} as it stops (a hit, full again, its death); value the part. Kept by its {@link
 * SetPiece}, made only for a boss with regenerating parts and hashed only then. Allocates nothing once made.
 */
public final class PartRegens {
    private final SetPiece piece;
    private final List<PartRegen> specs;
    /** Per part, its rule's index in {@link #specs}; -1 for a part that does not regenerate. */
    private final int[] index;
    /** Per part, whether it heals now. */
    private final boolean[] healing;
    /** Per part, the HP it healed in this attempt (for the measures: how often a sac grew back). */
    private final double[] healed;

    private SimEvents events = new SimEvents(0);

    PartRegens(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        specs = boss.regeneration();
        index = new int[parts];
        java.util.Arrays.fill(index, -1);
        for (int i = 0; i < specs.size(); i++) {
            index[specs.get(i).part()] = i;
        }
        healing = new boolean[parts];
        healed = new double[parts];
    }

    /** Whether {@code boss} has regenerating parts (then its set piece keeps a {@link PartRegens}). */
    static boolean any(BossSpec boss) {
        return boss != null && !boss.regeneration().isEmpty();
    }

    /** The sortie's events, where its events go. */
    void events(SimEvents sortieEvents) {
        events = sortieEvents;
    }

    /** Back to the level start (and the boss checkpoint): none healing. */
    void reset() {
        java.util.Arrays.fill(healing, false);
        java.util.Arrays.fill(healed, 0);
    }

    /** One engaged step: each living, hurt part past its quiet spell refills; a hit or its death stops the healing. */
    void step(double[] partHp, int[] sinceHit) {
        for (int p = 0; p < index.length; p++) {
            if (index[p] < 0) {
                continue;
            }
            PartRegen spec = specs.get(index[p]);
            double most = piece.spec().parts().get(p).hp();
            boolean quiet = sinceHit[p] > SimStep.ticks(spec.delaySeconds());
            if (!quiet || !(partHp[p] > 0) || !(partHp[p] < most)) {
                stop(p);
                continue;
            }
            double before = partHp[p];
            partHp[p] = Math.min(most, partHp[p] + spec.rate() * most * SimStep.SECONDS);
            healed[p] += partHp[p] - before;
            if (!healing[p] && partHp[p] < most) {
                healing[p] = true;
                events.add(SimEvents.Type.PART_HEALING, piece.partX(p), piece.partY(p), p);
            } else if (partHp[p] >= most) {
                stop(p);
            }
        }
    }

    private void stop(int p) {
        if (healing[p]) {
            healing[p] = false;
            events.add(SimEvents.Type.PART_HEALING_STOPPED, piece.partX(p), piece.partY(p), p);
        }
    }

    /** Whether part {@code p} regenerates. */
    public boolean fitted(int p) {
        return index[p] >= 0;
    }

    /** Whether part {@code p} heals now (the lime cores' healing pulse, the regrowth creak). */
    public boolean healing(int p) {
        return healing[p];
    }

    /** The HP part {@code p} healed in this attempt. */
    public double healed(int p) {
        return healed[p];
    }

    void addTo(StateHash hash) {
        for (int p = 0; p < index.length; p++) {
            if (index[p] >= 0) {
                hash.add(healing[p] ? 1 : 0).add(healed[p]);
            }
        }
    }
}
