package vanguard.sim;

/**
 * M7 part D: one unit's unit part (design/enemies/ground/shellback, the mortar vent): its hit points and its
 * centre, the part's offset turned with the unit's facing (the nearest of its 16 headings). Each {@link
 * Enemy} owns one; without a part in its stat block it holds 0 and stands nowhere (its HP is hashed only with
 * one).
 */
final class PartHp {
    /** The part's HP left; 0 without one. */
    private double hp;

    /** A new life of a unit of {@code spec}: its part at full HP, 0 without one. */
    void start(EnemySpec spec) {
        hp = spec.part().isPresent() ? spec.part().get().hp() : 0;
    }

    /** No part. */
    void clear() {
        hp = 0;
    }

    void addTo(StateHash hash) {
        hash.add(hp);
    }

    /** Whether the part stands. */
    boolean alive() {
        return hp > 0;
    }

    /** Whether its HP is used up (also true for a unit without a part). */
    boolean gone() {
        return hp <= 0;
    }

    /** Its HP share left of {@code part}'s, 0 to 1. */
    double share(EnemySpec.UnitPart part) {
        return Math.max(0, hp / part.hp());
    }

    /** It takes {@code amount} (after the ring); returns whether this destroyed it. */
    boolean take(double amount) {
        hp -= amount;
        return hp <= 0;
    }

    /** The facing a part's offset turns with: the nearest of the unit's 16 headings. */
    private static double heading(double facing) {
        double step = StrictMath.PI / 8;
        return Math.round(facing / step) * step;
    }

    /** The centre of {@code part} of a unit at {@code x} facing {@code facing}. */
    static double x(EnemySpec.UnitPart part, double x, double facing) {
        double f = heading(facing);
        return x + part.dx() * Trig.cos(f) + part.dy() * Trig.sin(f);
    }

    /** The centre's height of {@code part} of a unit at {@code y} facing {@code facing}. */
    static double y(EnemySpec.UnitPart part, double y, double facing) {
        double f = heading(facing);
        return y - part.dx() * Trig.sin(f) + part.dy() * Trig.cos(f);
    }
}
