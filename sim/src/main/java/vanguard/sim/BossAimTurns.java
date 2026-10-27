package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * Round 40 (the Siege Spire's lanes, proposal 4a): a boss's aim turns, stepped by its {@link SetPiece} in the style of
 * the {@link BreachArena}: per attack with {@link BossSpec.AimTurns}, the steps to its next aimed shot, the next part
 * in its turn order and the part whose turn it is (glowing; -1 for none). The glow before an aimed shot picks the next
 * living part in turn (its tell), the shot comes from it at the ship; a part that dies in its glow loses the shot. Its
 * state is hashed only for a boss with aim turns.
 */
final class BossAimTurns {
    private final SetPiece piece;
    /** The boss; null for a set piece flying passes (it then has no attacks). */
    private final BossSpec boss;
    /** Whether any of its attacks takes aim turns. */
    private final boolean any;

    private final int[] aimTicks;
    private final int[] aimNext;
    private final int[] aimPart;

    BossAimTurns(SetPiece piece, BossSpec boss) {
        this.piece = piece;
        this.boss = boss;
        int attacks = boss == null ? 0 : boss.attacks().size();
        boolean aiming = false;
        for (int a = 0; a < attacks; a++) {
            aiming |= boss.attacks().get(a).aimTurns().isPresent();
        }
        any = aiming;
        aimTicks = new int[attacks];
        aimNext = new int[attacks];
        aimPart = new int[attacks];
    }

    /** Back to the level start: no turn taken, no part glowing. */
    void reset() {
        Arrays.fill(aimTicks, 0);
        Arrays.fill(aimNext, 0);
        Arrays.fill(aimPart, -1);
    }

    /** An aim turn's glow ends with its phase. */
    void endGlows() {
        Arrays.fill(aimPart, -1);
    }

    /** Attack {@code a}'s phase engages: its first aimed shot one turn after the phase's {@code delay}, from the first part. */
    void arm(int a, BossSpec.AimTurns turns, int delay) {
        aimTicks[a] = delay + Math.max(1, SimStep.ticks(turns.everySeconds()));
        aimNext[a] = 0;
        aimPart[a] = -1;
    }

    /** One step of attack {@code a}'s aim turns. */
    void turn(int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        BossSpec.AimTurns turns = attack.aimTurns().orElseThrow();
        int glow = SimStep.ticks(turns.glowSeconds());
        aimTicks[a]--;
        if (aimPart[a] < 0 && aimTicks[a] <= glow) {
            List<Integer> parts = SetPieceGuns.firingParts(attack, current);
            for (int i = 0; i < parts.size(); i++) {
                int candidate = (aimNext[a] + i) % parts.size();
                int p = parts.get(candidate);
                if (piece.partHp(p) > 0) {
                    aimPart[a] = p;
                    aimNext[a] = (candidate + 1) % parts.size();
                    if (firing) {
                        actions.aimTell(p, piece.partX(p), piece.partY(p));
                    }
                    break;
                }
            }
        }
        if (aimTicks[a] > 0) {
            return;
        }
        aimTicks[a] = Math.max(1, SimStep.ticks(turns.everySeconds()));
        int p = aimPart[a];
        aimPart[a] = -1;
        if (p >= 0 && piece.partHp(p) > 0 && firing) {
            actions.aimed(piece.partX(p), piece.partY(p), attack.gun());
        }
    }

    /** The part glowing in attack {@code a}'s aim turn (its aimed shot comes next); -1 for none. */
    int part(int a) {
        return aimPart[a];
    }

    /** How far attack {@code a}'s aim glow has run, 0–1 (0 without a glowing part). */
    double glowShare(int a) {
        if (aimPart[a] < 0) {
            return 0;
        }
        int glow = Math.max(
                1, SimStep.ticks(boss.attacks().get(a).aimTurns().orElseThrow().glowSeconds()));
        return Math.clamp(1 - (double) aimTicks[a] / glow, 0, 1);
    }

    /** Whether part {@code p} glows in an aim turn. */
    boolean glowing(int p) {
        for (int a = 0; a < aimPart.length; a++) {
            if (aimPart[a] == p) {
                return true;
            }
        }
        return false;
    }

    /** Its state, in the order the boss's hash has always added it; nothing for a boss without aim turns. */
    void addTo(StateHash hash) {
        if (!any) {
            return;
        }
        for (int a = 0; a < aimTicks.length; a++) {
            hash.add(aimTicks[a]).add(aimNext[a]).add(aimPart[a]);
        }
    }
}
