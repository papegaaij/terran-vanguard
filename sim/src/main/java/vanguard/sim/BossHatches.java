package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * M7 part F: a walking boss's hatches (design/enemies/bosses: its homing salvos' tell, {@link EnemyGun.HomingSpec}),
 * kept by its {@link SetPiece} in the style of the {@link BreachArena}: per part the steps its hatch stays open (the
 * tell and the salvo; 0 shut) and, round 44 (the user in round 43, W.a), whether it takes damage only while its hatch
 * is open (a walking boss with {@link BossSpec.Stride#hatchArmour()}: the parts that fire a homing attack, the pods).
 * Its state is hashed with the walking boss's.
 */
final class BossHatches {
    private final SetPiece piece;
    /** Per part, the steps its hatch stays open; 0 shut. */
    private final int[] hatchTicks;
    /** Per part, whether it takes damage only while its hatch is open; null for none. */
    private final boolean[] hatchArmoured;

    BossHatches(SetPiece piece, BossSpec boss, int parts, boolean armour) {
        this.piece = piece;
        hatchTicks = new int[parts];
        hatchArmoured = armour ? hatchArmoured(boss, parts) : null;
    }

    /** Round 44: the parts that fire a homing attack (a walking boss's pods). */
    private static boolean[] hatchArmoured(BossSpec boss, int parts) {
        boolean[] out = new boolean[parts];
        for (BossSpec.Attack attack : boss.attacks()) {
            if (attack.gun().homing().isPresent()) {
                for (int p : attack.parts()) {
                    if (p >= 0 && p < parts) {
                        out[p] = true;
                    }
                }
            }
        }
        return out;
    }

    /** Back to the level start: every hatch shut. */
    void reset() {
        Arrays.fill(hatchTicks, 0);
    }

    /** One step of the open hatches' time. */
    void tick() {
        for (int p = 0; p < hatchTicks.length; p++) {
            if (hatchTicks[p] > 0) {
                hatchTicks[p]--;
            }
        }
    }

    /**
     * A homing attack's salvo is its gun's hatch time away ({@code ticks} its attack's steps to its next volley,
     * {@code next} its next part in turn): the hatch of each part that will fire it opens (the tell), until its
     * salvo's last missile has left: the next part in turn of a staggered or rotating attack, else every living one;
     * a part off the field does not.
     */
    void tell(
            BossSpec.Attack attack, BossSpec.Phase current, int ticks, int next, BossActions actions, boolean firing) {
        int hatch = SimStep.ticks(attack.gun().homing().orElseThrow().hatchSeconds());
        if (hatch <= 0 || ticks != hatch || !firing) {
            return;
        }
        int open = hatch + (attack.gun().burst() - 1) * Math.max(1, SimStep.ticks(attack.burstGapSeconds())) + 1;
        List<Integer> parts = SetPieceGuns.firingParts(attack, current);
        if (attack.stagger()) {
            openHatch(parts.get(next % parts.size()), open, actions);
            return;
        }
        for (int i = 0; i < parts.size(); i++) {
            int p = parts.get((attack.rotate() ? next + i : i) % parts.size());
            if (piece.partHp(p) > 0) {
                openHatch(p, open, actions);
                if (attack.rotate()) {
                    return;
                }
            }
        }
    }

    private void openHatch(int p, int open, BossActions actions) {
        // M9 part H, step H1b: a ringed boss's battery only while armed (any other boss's part: while it lives).
        if (piece.partArmed(p) && piece.partOnField(p)) {
            hatchTicks[p] = open;
            actions.hatch(p, piece.partX(p), piece.partY(p));
        }
    }

    /** Whether part {@code p}'s hatch is open. */
    boolean open(int p) {
        return hatchTicks[p] > 0;
    }

    /** Whether part {@code p} takes damage only while its hatch is open. */
    boolean armoured(int p) {
        return hatchArmoured != null && hatchArmoured[p];
    }

    /** Its state, as the walking boss's hash has always added it. */
    void addTo(StateHash hash) {
        for (int ticks : hatchTicks) {
            hash.add(ticks);
        }
    }
}
