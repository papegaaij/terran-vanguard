package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/player/weapons/choir-resonator, Behaviour): a wave front from the nose straight up,
 * widening from 32 px to its width at its reach; every hull it sweeps is met once, the nearest first, at × 0.75 a hull,
 * and at its count it is spent; a closed shell or a hardened unit takes nothing but spends a step; it is gone at its
 * reach.
 *
 * <p>M11 part I, step I2 (the open rows): a standing mirror's face takes nothing, bounces nothing and spends a step, its
 * open flank is hit as a hull; motes and mines are hit and spend a step (a swarm in front soaks a wave); a set piece's
 * parts are one hull, and so are a unit and its unit parts (the first one the front reaches).
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

    // ---- M11 part I, step I2: mirrors, motes, mines and bodies of several parts ----

    @Test
    void aStandingMirrorsFaceTakesNothingBouncesNothingAndSpendsAStep() {
        double x = Ship.START_X;
        Sortie sortie =
                sortie(List.of(Act6ASpecs.mirrored(100), unit("a"), unit("b")), new double[] {x, x, x}, new double[] {
                    0, 0.4, 0.8
                });
        int absorbed = 0;
        int bounced = 0;
        double row = Ship.START_Y + 21 + 60;
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > row) {
            sortie.step(0);
        }
        sortie.step(Command.FIRE.bit());
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
            absorbed += sortie.events().count(SimEvents.Type.MIRROR_ABSORBED);
            bounced += sortie.events().count(SimEvents.Type.MIRROR_BOUNCED);
        }
        assertEquals(1, absorbed, "the face takes it");
        assertEquals(0, bounced, "no projectile to turn round");
        assertEquals(100, hp(sortie, "mirror-interceptor"), 1e-9, "the face takes nothing");
        assertEquals(100 - 22.5, hp(sortie, "a"), 1e-9, "a step spent on the face");
        assertEquals(100 - 16.875, hp(sortie, "b"), 1e-9, "the third and last hull");
    }

    @Test
    void aMirrorsOpenFlankIsHitAsAHull() {
        double x = Ship.START_X;
        Sortie sortie =
                sortie(List.of(Act6ASpecs.mirrored(100), unit("a")), new double[] {x, x}, new double[] {0, 0.4});
        double row = Ship.START_Y + 21 + 60;
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > row) {
            sortie.step(0);
        }
        // Turned a quarter away (holding its station, the ship far to its left): the wave comes in on its flank. It
        // turns back at 60°/s, so the 0.12 s the front takes to reach it leaves it far outside its ±60° arc.
        Enemy mirror = Act6BSpecs.find(sortie, "mirror-interceptor");
        Enemy.Phase phase = mirror.phase;
        mirror.phase = Enemy.Phase.HOLD;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            mirror.mirror().turn(Act6ASpecs.MIRROR, mirror, mirror.x() - 300, mirror.y());
        }
        mirror.phase = phase;
        assertEquals(Math.PI / 2, Math.abs(mirror.mirror().facing()), 1e-9);
        sortie.step(Command.FIRE.bit());
        int absorbed = 0;
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
            absorbed += sortie.events().count(SimEvents.Type.MIRROR_ABSORBED);
        }
        assertEquals(0, absorbed);
        assertEquals(100 - 30, hp(sortie, "mirror-interceptor"), 1e-9, "the flank is open: the full wave");
        assertEquals(100 - 22.5, hp(sortie, "a"), 1e-9, "and it passes on a step weaker");
    }

    @Test
    void motesAreHitAndEachSpendsAStepSoASwarmInFrontSoaksAWave() {
        double x = Ship.START_X;
        List<EnemySpec> swarm = List.of(
                Act6BSpecs.target("mote-1", Layer.AIR, 1).withSeekersIgnore(true),
                Act6BSpecs.target("mote-2", Layer.AIR, 1).withSeekersIgnore(true),
                Act6BSpecs.target("mote-3", Layer.AIR, 1).withSeekersIgnore(true),
                unit("target"));
        Sortie sortie = sortie(swarm, new double[] {x - 10, x + 10, x, x}, new double[] {0, 0.2, 0.4, 0.8});
        fireOnce(sortie, 60);
        assertEquals(0, left(sortie, swarm.subList(0, 3)), "every mote hit (the arc is no seeker)");
        assertEquals(100, hp(sortie, "target"), 1e-9, "the swarm soaked the wave: spent at its 3 hulls");
        assertEquals(3, sortie.kills());
    }

    /** A spore mine of {@code hp} that arms at once and holds its place on the screen. */
    private static EnemyGun mineGun(double hp) {
        return new EnemyGun(
                1,
                0,
                1,
                100,
                6,
                false,
                1,
                0,
                TestSpecs.INFINITE,
                TestSpecs.INFINITE,
                Optional.of(new EnemyGun.MineSpec(0, 30, 0, hp, 8, 4, false, 0)));
    }

    @Test
    void armedMinesAreHitAndEachSpendsAStep() {
        double x = Ship.START_X;
        Sortie sortie = sortie(List.of(unit("a")), new double[] {x}, new double[] {0});
        double nose = Ship.START_Y + 21;
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > nose + 200) {
            sortie.step(0);
        }
        // A tough mine nearest, a 1 HP one behind it, then the unit.
        sortie.dropMine(mineGun(100), x, nose + 50, 0);
        sortie.dropMine(mineGun(1), x, nose + 90, 0);
        sortie.step(0);
        assertEquals(2, sortie.mineCount());
        assertTrue(sortie.mine(0).armed() && sortie.mine(1).armed());
        sortie.step(Command.FIRE.bit());
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
        }
        assertEquals(1, sortie.mineCount(), "the 1 HP mine shot down at 22.5");
        assertEquals(100 - 16.875, hp(sortie, "a"), 1e-9, "two steps spent on the mines");
    }

    /** A set piece holding still on the player's layer at (x, y) with {@code parts}. */
    private static SetPieceSpec still(String slug, double x, double y, List<PartSpec> parts) {
        return new SetPieceSpec(
                slug,
                new Hitbox(120, 160),
                new Hitbox(40, 40),
                0,
                parts,
                List.of(Pass.flight("hold", Layer.AIR, 0, List.of(new Waypoint(0, x, y), new Waypoint(60, x, y)))),
                Optional.empty());
    }

    private static PartSpec part(String name, double dy, boolean vital) {
        return new PartSpec(name, 0, dy, new Hitbox(30, 30), 100, vital, 10, Optional.empty(), 0);
    }

    @Test
    void aSetPiecesPartsAreOneHullTheFirstPartReachedTakesIt() {
        double x = Ship.START_X;
        double nose = Ship.START_Y + 21;
        // Its front part 110 px up, its core 190 px up (on the wave's line, both); a second body behind at 260 px.
        SetPieceSpec body = still("body", x, nose + 150, List.of(part("front", -40, false), part("core", 40, true)));
        SetPieceSpec behind = still("behind", x, nose + 260, List.of(part("hull", 0, true)));
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(20, 130)))
                .setPieces(List.of(body, behind))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        Sortie sortie = new Sortie(
                1, TestSpecs.loadout(mount()), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(0);
        }
        assertTrue(sortie.setPiece(0).present() && sortie.setPiece(1).present());
        sortie.step(Command.FIRE.bit());
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
        }
        assertEquals(100 - 30, sortie.setPiece(0).partHp(0), 1e-9, "the first part reached");
        assertEquals(100, sortie.setPiece(0).partHp(1), 1e-9, "never a second part of the same body");
        assertEquals(100 - 22.5, sortie.setPiece(1).partHp(0), 1e-9, "one step spent on the whole body");
    }

    @Test
    void aUnitAndItsPartsAreOneHullTheFirstOneReachedTakesIt() {
        // A unit with a part on its nose (below its body on the screen) and one on its tail: the wave meets the nose
        // part first and passes on, never the body or the tail part; the next unit takes the second step.
        double x = Ship.START_X;
        EnemySpec twoParts = Act6BSpecs.target("two-parts", Layer.AIR, 100)
                .withParts(List.of(
                        new EnemySpec.UnitPart("nose", 50, new Hitbox(16, 16), 0, -20, 4, false, 0, 0),
                        new EnemySpec.UnitPart("tail", 50, new Hitbox(16, 16), 0, 20, 4, false, 0, 0)));
        Sortie sortie = sortie(List.of(twoParts, unit("next")), new double[] {x, x}, new double[] {0, 0.6});
        fireOnce(sortie, 80);
        Enemy unit = Act6BSpecs.find(sortie, "two-parts");
        assertEquals(100, unit.hp(), 1e-9, "the body untouched");
        assertEquals((50 - 30) / 50.0, unit.partShare(0), 1e-9, "the nose part reached first takes the wave");
        assertEquals(1, unit.partShare(1), 1e-9, "never a second part of the same unit");
        assertEquals(100 - 22.5, hp(sortie, "next"), 1e-9, "one step spent on the whole unit");
    }
}
