package vanguard.sim;

/**
 * M7 part A: a Deflector Pod at one upgrade level (design/player/weapons/deflector-pod), built from its
 * {@code deflector} block by {@code vanguard.content.SimSpecs}: a half-disc field round the ship's centre
 * on the pod's side that catches enemy bullets while it holds a charge.
 *
 * @param radius the field's radius, px
 * @param charges how many bullets it catches before it is empty
 * @param rechargeSeconds how long one charge takes to come back
 */
public record DeflectorSpec(double radius, int charges, double rechargeSeconds) {
    public DeflectorSpec {
        if (!(radius > 0) || charges < 1 || !(rechargeSeconds > 0)) {
            throw new IllegalArgumentException("a deflector has a radius, at least one charge and a recharge time");
        }
    }

    int rechargeTicks() {
        return Math.max(1, SimStep.ticks(rechargeSeconds));
    }
}
