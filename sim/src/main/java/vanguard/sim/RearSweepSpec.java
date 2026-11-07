package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The rear sweep; m11f-gaps.md §1 #2, §4): a boss's rear
 * sweep ({@link RearSweep}), run by its playbooks' {@link PhasePlaybook.Kind#REAR_SWEEP} move. After the move's tell the
 * piece drops out of the top edge at {@code speed}, stays out of sight {@code outSeconds} (its bottom-edge point marked),
 * rises from the bottom edge on the side the ship is not on (its centre at least {@code offShip} px off the ship's x)
 * and climbs that side at {@code speed} to its station's height, its body box always {@code clear} px off the ship (it
 * slides away sideways when the ship comes over), firing {@code fireAttack}'s gun from its playbook's source every
 * {@code fireEverySeconds} of the climb (step F1c: and once as the drop starts and once as the source leaves the top
 * edge); then it glides back into its sway.
 *
 * @param outSeconds out of sight above the top edge, s (1.0)
 * @param speed the drop's and the climb's speed, px/s (200)
 * @param offShip the rising centre's least distance from the ship's x as it is marked, px (160)
 * @param clear the least distance from the ship's centre to the body box while it climbs, px (100)
 * @param fireAttack the climb's fire (and the drop's two shots) (an index into {@link BossSpec#attacks()}: the spore pair); -1 for none
 * @param fireEverySeconds between two of the climb's shots, s (0.4)
 */
public record RearSweepSpec(
        double outSeconds, double speed, double offShip, double clear, int fireAttack, double fireEverySeconds) {
    public RearSweepSpec {
        if (!(outSeconds >= 0)
                || !(speed > 0)
                || !(offShip >= 0)
                || !(clear >= 0)
                || fireAttack < -1
                || (fireAttack >= 0 && !(fireEverySeconds > 0))) {
            throw new IllegalArgumentException(
                    "a rear sweep has a time out of sight, a speed, its distances and a spacing for its fire");
        }
    }
}
