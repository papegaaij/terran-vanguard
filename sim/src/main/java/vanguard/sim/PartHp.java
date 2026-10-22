package vanguard.sim;

import java.util.List;

/**
 * M7 part D: one unit's unit parts (design/enemies/ground/shellback, the mortar vent; M8 part B:
 * design/enemies/ground/drill-rig, the four pumps): their hit points and their centres, each part's offset turned
 * with a walker's facing (the nearest of its 16 headings). Each {@link Enemy} owns one; without parts in its stat
 * block it holds none and stands nowhere (its HP are hashed only with parts). Allocates nothing after it is made.
 */
final class PartHp {
    /** The parts' HP left, by their place in the stat block's list. */
    private final double[] hp = new double[EnemySpec.MOST_PARTS];
    /** The number of parts; 0 without. */
    private int count;

    /** A new life of a unit of {@code spec}: its parts at full HP, none without. */
    void start(EnemySpec spec) {
        List<EnemySpec.UnitPart> parts = spec.parts();
        count = parts.size();
        for (int p = 0; p < count; p++) {
            hp[p] = parts.get(p).hp();
        }
    }

    /** No parts. */
    void clear() {
        count = 0;
    }

    /** Each part's HP in turn (one number for the Shellback's vent, as before M8). */
    void addTo(StateHash hash) {
        for (int p = 0; p < count; p++) {
            hash.add(hp[p]);
        }
    }

    /** The number of parts. */
    int count() {
        return count;
    }

    /** Whether part {@code p} stands; false for a part it does not have. */
    boolean alive(int p) {
        return p < count && hp[p] > 0;
    }

    /** The number of its parts still standing. */
    int standing() {
        int standing = 0;
        for (int p = 0; p < count; p++) {
            standing += hp[p] > 0 ? 1 : 0;
        }
        return standing;
    }

    /** Part {@code p}'s HP share left of {@code part}'s, 0 to 1; 0 for a part it does not have. */
    double share(int p, EnemySpec.UnitPart part) {
        return p < count ? Math.max(0, hp[p] / part.hp()) : 0;
    }

    /** Part {@code p} takes {@code amount} (after the ring); returns whether this destroyed it. */
    boolean take(int p, double amount) {
        hp[p] -= amount;
        return hp[p] <= 0;
    }

    /**
     * M8 part B: whether the gun is silenced for good: the unit has parts that carry it ({@link
     * EnemySpec.UnitPart#silencesGun()}) and every one of them is gone. False without parts.
     */
    boolean silenced(List<EnemySpec.UnitPart> parts) {
        boolean carried = false;
        for (int p = 0; p < count; p++) {
            if (parts.get(p).silencesGun()) {
                if (hp[p] > 0) {
                    return false;
                }
                carried = true;
            }
        }
        return carried;
    }

    /**
     * M8 part B (design/enemies/ground/drill-rig): how much longer the gun's interval is, the lost parts' {@link
     * EnemySpec.UnitPart#slowsGunSeconds()} added up; exactly 0 without parts or losses.
     */
    double slowSeconds(List<EnemySpec.UnitPart> parts) {
        double slow = 0;
        for (int p = 0; p < count; p++) {
            if (hp[p] <= 0) {
                slow += parts.get(p).slowsGunSeconds();
            }
        }
        return slow;
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
