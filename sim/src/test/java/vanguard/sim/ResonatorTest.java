package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/player/weapons/choir-resonator, Behaviour): a wave front from the nose straight up,
 * widening from 32 px to its width at its reach; every hull it sweeps is met once, the nearest first, at × 0.75 a hull,
 * and at its count it is spent; a closed shell or a hardened unit takes nothing but spends a step; it is gone at its
 * reach.
 */
class ResonatorTest {
    /** L1: 2.5 waves a second of 30, 32 px at the nose to 160 px at 300 px, 520 px/s, 3 hulls, × 0.75. */
    static final WeaponSpec.Wave L1 = new WeaponSpec.Wave(32, 160, 300, 520, 3, 0.75);
    /** Overdrive: 3.5 a second, 260 px at 400 px, 6 hulls. */
    static final WeaponSpec.Wave OD = new WeaponSpec.Wave(32, 260, 400, 520, 6, 0.75);

    static final WeaponSpec.Muzzle NOSE = new WeaponSpec.Muzzle(0, 21, 0);

    static Armament.Mount mount() {
        return new Armament.Mount(
                Armament.Slot.FRONT,
                WeaponSpec.wave("choir-resonator", "wave", "resonator", 2.5, 30, L1, NOSE),
                WeaponSpec.wave("choir-resonator", "wave", "resonator", 3.5, 53.5, OD, NOSE));
    }

    /** Units riding the ground at {@code xs}, entering at {@code ts}, of {@code specs}. */
    private static Sortie sortie(List<EnemySpec> specs, double[] xs, double[] ts) {
        return sortie(mount(), specs, xs, ts);
    }

    /** Units riding the ground at {@code xs}, entering at {@code ts}, of {@code specs}, the ship with {@code mount}. */
    private static Sortie sortie(Armament.Mount mount, List<EnemySpec> specs, double[] xs, double[] ts) {
        List<GroundUnit> units = new ArrayList<>();
        for (int i = 0; i < xs.length; i++) {
            units.add(new GroundUnit(ts[i], xs[i], specs.get(i), -1));
        }
        return new Sortie(
                1,
                TestSpecs.loadout(mount),
                Act6BSpecs.level(20, HazardSpec.NONE, units, List.of()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the first unit is {@code above} px above the nose, one step with fire held, then 0.8 s without. */
    private static int fireOnce(Sortie sortie, double above) {
        double row = Ship.START_Y + 21 + above;
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > row) {
            sortie.step(0);
        }
        sortie.step(Command.FIRE.bit());
        int clangs = sortie.events().count(SimEvents.Type.WAVE_HIT);
        assertEquals(1, sortie.events().count(SimEvents.Type.WAVE_FIRED));
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
            clangs += sortie.events().count(SimEvents.Type.WAVE_HIT);
        }
        return clangs;
    }

    private static EnemySpec unit(String slug) {
        return Act6BSpecs.target(slug, Layer.AIR, 100);
    }

    private static double hp(Sortie sortie, String slug) {
        return Act6BSpecs.find(sortie, slug).hp();
    }

    @Test
    void aWaveMeetsTheNearestHullsOnceAtThreeQuartersEachUntilItsCount() {
        double x = Ship.START_X;
        Sortie sortie =
                sortie(List.of(unit("a"), unit("b"), unit("c"), unit("d")), new double[] {x, x, x, x}, new double[] {
                    0, 0.4, 0.8, 1.2
                });
        int clangs = fireOnce(sortie, 60);
        assertEquals(100 - 30, hp(sortie, "a"), 1e-9, "the nearest at full damage");
        assertEquals(100 - 22.5, hp(sortie, "b"), 1e-9, "the second at 0.75");
        assertEquals(100 - 16.875, hp(sortie, "c"), 1e-9, "the third at 0.75²");
        assertEquals(100, hp(sortie, "d"), 1e-9, "spent at its 3 hulls");
        assertTrue(clangs >= 1 && clangs <= 3, "the clang, throttled to 0.12 s: " + clangs);
        assertEquals(0, sortie.fire.waves().count(), "gone");
    }

    @Test
    void aClosedShellOrAHardenedUnitTakesNothingButSpendsAStep() {
        double x = Ship.START_X;
        Sortie sortie = sortie(
                List.of(unit("h").withHardened(true), Act7ASpecs.shelled(30), unit("a"), unit("b")),
                new double[] {x, x, x, x},
                new double[] {0, 0.4, 0.8, 1.2});
        fireOnce(sortie, 60);
        assertEquals(100, hp(sortie, "h"), 1e-9, "hardened: a glance");
        assertEquals(360, hp(sortie, "shelled"), 1e-9, "a closed shell: a flake");
        assertEquals(100 - 30 * 0.75 * 0.75, hp(sortie, "a"), 1e-9, "two steps spent before it");
        assertEquals(100, hp(sortie, "b"), 1e-9, "spent");
    }

    @Test
    void narrowNearTheNoseWideNearItsReach() {
        double x = Ship.START_X;
        List<EnemySpec> row = List.of(unit("left"), unit("mid"), unit("right"));
        double[] xs = {x - 60, x, x + 60};
        double[] ts = {0, 0, 0};
        Sortie near = sortie(row, xs, ts);
        fireOnce(near, 100);
        assertEquals(70, hp(near, "mid"), 1e-9);
        assertEquals(100, hp(near, "left"), 1e-9, "outside the narrow arc");
        assertEquals(100, hp(near, "right"), 1e-9);
        Sortie far = sortie(row, xs, ts);
        fireOnce(far, 330);
        assertTrue(hp(far, "left") < 100 && hp(far, "right") < 100 && hp(far, "mid") < 100, "the whole row far out");
        assertEquals(70 + 100 - 22.5 + 100 - 16.875, hp(far, "left") + hp(far, "mid") + hp(far, "right"), 1e-9);
    }

    /** How many units {@code sortie} has left of {@code slugs}. */
    private static int left(Sortie sortie, List<EnemySpec> specs) {
        int n = 0;
        for (EnemySpec spec : specs) {
            n += Act6BSpecs.find(sortie, spec.slug()) != null ? 1 : 0;
        }
        return n;
    }

    @Test
    void oneFrontKillsAWholeRowInOneStep() {
        // M11 FIXA: three kills in one step; a kill frees its pool slot (the last unit moves there), which once left
        // the step's later candidates pointing at stale indices (IndexOutOfBounds in Level 36).
        double x = Ship.START_X;
        List<EnemySpec> row = List.of(
                Act6BSpecs.target("left", Layer.AIR, 5),
                Act6BSpecs.target("mid", Layer.AIR, 5),
                Act6BSpecs.target("right", Layer.AIR, 5));
        Sortie sortie = sortie(row, new double[] {x - 60, x, x + 60}, new double[] {0, 0, 0});
        fireOnce(sortie, 330);
        assertEquals(0, left(sortie, row), "the whole row down");
    }

    @Test
    void aPierceOfFiveThroughAPackedLine() {
        // M11 FIXA: a level-5 front (5 hulls) through five packed units, all killed in the same step, plus a sixth
        // behind them that it is spent before.
        WeaponSpec.Wave five = new WeaponSpec.Wave(32, 160, 300, 520, 5, 0.75);
        Armament.Mount mount = new Armament.Mount(
                Armament.Slot.FRONT,
                WeaponSpec.wave("choir-resonator", "wave", "resonator", 2.5, 30, five, NOSE),
                WeaponSpec.wave("choir-resonator", "wave", "resonator", 3.5, 53.5, OD, NOSE));
        double x = Ship.START_X;
        List<EnemySpec> line = new ArrayList<>();
        double[] xs = new double[6];
        double[] ts = new double[6];
        for (int u = 0; u < 5; u++) {
            line.add(Act6BSpecs.target("u" + u, Layer.AIR, 5));
            xs[u] = x - 44 + 22 * u;
        }
        line.add(unit("behind"));
        xs[5] = x;
        ts[5] = 0.4;
        Sortie sortie = sortie(mount, line, xs, ts);
        fireOnce(sortie, 330);
        assertEquals(0, left(sortie, line.subList(0, 5)), "all five packed units down");
        assertEquals(100, hp(sortie, "behind"), 1e-9, "spent at its 5 hulls");
    }

    @Test
    void theFrontsAreHashedAndDeterministic() {
        double x = Ship.START_X;
        Sortie a = sortie(List.of(unit("a")), new double[] {x}, new double[] {0});
        Sortie b = sortie(List.of(unit("a")), new double[] {x}, new double[] {0});
        for (int i = 0; i < SimStep.ticks(3); i++) {
            a.step(Command.FIRE.bit());
            b.step(Command.FIRE.bit());
        }
        assertEquals(a.stateHash(), b.stateHash());
        assertTrue(a.fire.waves().count() > 0);
    }
}
