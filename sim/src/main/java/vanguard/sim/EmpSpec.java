package vanguard.sim;

/**
 * M7 part A, the EMP Burst's numbers (design/player/specials, EMP Burst (L15)): a ring expands from the
 * ship to cover the whole play field in {@code ringSeconds}; each unit it reaches (its centre) is
 * stunned, {@code machineStunSeconds} a machine (its stat block's {@link EnemySpec#machine()}) and
 * {@code vrellStunSeconds} every other unit; the next burst can follow after {@code repeatSeconds}.
 */
public record EmpSpec(double ringSeconds, double machineStunSeconds, double vrellStunSeconds, double repeatSeconds)
        implements SpecialSpec.Effect {
    public EmpSpec {
        if (!(ringSeconds > 0) || !(repeatSeconds > 0)) {
            throw new IllegalArgumentException("an EMP Burst needs a ring time and a repeat time");
        }
        if (!(machineStunSeconds > 0) || !(vrellStunSeconds > 0)) {
            throw new IllegalArgumentException("an EMP Burst needs its stun times");
        }
    }
}
