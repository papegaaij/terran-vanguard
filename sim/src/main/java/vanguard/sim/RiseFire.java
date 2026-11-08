package vanguard.sim;

import java.util.List;

/**
 * M11 part F, step F5 (design/enemies/bosses/vornes-chimera, Movement; round 93 L.3, the user: "I like the boss flying in
 * from below, but make sure it enters the screen firing real rounds at you"): a boss's fire while it rises to its station
 * ({@link BossSpec.Rise#fireAttack}). Every {@link BossSpec.Rise#fireEverySeconds} of the rise (the first at its first
 * step) the next of the attack's parts in turn that lives and is on the field fires one aimed volley of the attack at the
 * ship, unless the ship is within {@link BossSpec.Rise#clearPx} of it. Stateless: the turn and the clock follow from the
 * rise's own steps (hashed with the piece's motion), so a boss without a firing rise hashes as before.
 */
final class RiseFire {
    private RiseFire() {}

    /**
     * One step of {@code piece}'s rise ({@code ticks} its steps since it arrived, 1 first) with the ship at (shipX,
     * shipY); shots reach the level only while {@code firing}.
     */
    static void step(
            SetPiece piece, BossSpec boss, int ticks, double shipX, double shipY, BossActions actions, boolean firing) {
        BossSpec.Rise rise = boss.rise().orElseThrow();
        volleys(
                piece,
                boss,
                rise.fireAttack(),
                rise.fireEverySeconds(),
                rise.clearPx(),
                ticks,
                shipX,
                shipY,
                actions,
                firing);
    }

    /**
     * M11 part G, step G5 (the echoes enter firing; round 93 L.3's rule for every boss): one step of {@code attack}'s
     * entry volleys, {@code ticks} the steps since the entry began (1 first): every {@code everySeconds} the next of its
     * parts in turn that lives and is on the field fires one aimed volley at the ship, unless the ship is within {@code
     * clearPx} of it. The rise's fire and an anchored echo's forming fire ({@link EchoSpec#fireAttack()}) both.
     */
    static void volleys(
            SetPiece piece,
            BossSpec boss,
            int attackIndex,
            double everySeconds,
            double clearPx,
            int ticks,
            double shipX,
            double shipY,
            BossActions actions,
            boolean firing) {
        int every = Math.max(1, SimStep.ticks(everySeconds));
        if (attackIndex < 0 || ticks < 1 || (ticks - 1) % every != 0 || !firing) {
            return;
        }
        BossSpec.Attack attack = boss.attacks().get(attackIndex);
        List<Integer> parts = attack.parts();
        int volley = (ticks - 1) / every;
        for (int k = 0; k < parts.size(); k++) {
            int p = parts.get((volley + k) % parts.size());
            if (piece.partHp(p) <= 0 || !piece.parts.partOnField(p)) {
                continue;
            }
            double dx = shipX - piece.partX(p);
            double dy = shipY - piece.partY(p);
            if (dx * dx + dy * dy >= clearPx * clearPx) {
                actions.aimed(piece.partX(p), piece.partY(p), attack.gun());
            }
            return;
        }
    }
}
