package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M9 part A, step A1 (design/player/weapons/swarm-tail; QA-1): a rear-first homing missile, launched backwards, picks
 * the nearest enemy behind the ship's nose line within its 320 px seek radius, all round; with none there the nearest
 * anywhere within it; with no unit at all a seekable missile (the homing diamond). A nearest-in-cone homing shot (the
 * Micro-missile Pod's) picks a seekable missile only when no unit is in its cone; an Act 4 missile never; Rook's
 * homing shots the same.
 */
class SwarmTailTest {
    private static final int FIRE = Command.FIRE.bit();

    private static Sortie sortie(LevelScript level, WeaponSpec weapon) {
        Armament.Mount mount = new Armament.Mount(Armament.Slot.REAR, weapon, weapon);
        return new Sortie(
                1, TestSpecs.loadout(mount), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static GroundUnit unit(double time, double x) {
        return new GroundUnit(time, x, TorpedoTest.dummy(Layer.AIR, 500), -1);
    }

    /** Runs until the unit of serial-order {@code first} has passed below {@code y}. */
    private static void until(Sortie sortie, double y) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            if (sortie.enemyCount() > 0 && sortie.enemy(0).y() < y) {
                return;
            }
            sortie.step(Command.NONE);
        }
        throw new AssertionError("no unit came down");
    }

    private static Enemy at(Sortie sortie, double x) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (Math.abs(sortie.enemy(i).x() - x) < 1) {
                return sortie.enemy(i);
            }
        }
        throw new AssertionError("no unit at " + x);
    }

    /** Fires one volley and lets its missiles pick; returns the first missile's lock. */
    private static int firstLock(Sortie sortie) {
        sortie.step(FIRE);
        sortie.step(Command.NONE);
        assertTrue(sortie.shotCount() > 0);
        return sortie.shot(0).target();
    }

    @Test
    void itPicksTheNearestBehindTheNoseLineBeforeANearerOneAhead() {
        // One unit 200 px to the side, already below the ship's centre row; one straight ahead, nearer.
        Sortie sortie = sortie(TorpedoTest.units(false, unit(0, 40), unit(1.2, Ship.START_X)), Act5Specs.SWARM_TAIL);
        until(sortie, Ship.START_Y - 40);
        Enemy behind = at(sortie, 40);
        Enemy ahead = at(sortie, Ship.START_X);
        double shipY = sortie.ship().y();
        assertTrue(behind.y() < shipY && ahead.y() > shipY);
        assertTrue(Math.hypot(ahead.x() - sortie.ship().x(), ahead.y() - shipY)
                < Math.hypot(behind.x() - sortie.ship().x(), behind.y() - shipY));
        assertEquals(behind.serial(), firstLock(sortie), "rear first");
    }

    @Test
    void withNoneBehindItTakesTheNearestAnywhereInReachAndCurvesBackUp() {
        Sortie sortie = sortie(TorpedoTest.units(false, unit(0, Ship.START_X + 40)), Act5Specs.SWARM_TAIL);
        until(sortie, Ship.START_Y + 200);
        Enemy ahead = sortie.enemy(0);
        assertEquals(ahead.serial(), firstLock(sortie));
        boolean up = false;
        for (int i = 0; i < SimStep.ticks(1.0) && sortie.shotCount() > 0; i++) {
            sortie.step(Command.NONE);
            up |= Math.abs(sortie.shot(0).heading()) < Math.PI / 2;
        }
        assertTrue(up, "launched backwards, it curves back up past the ship");
    }

    @Test
    void withNoUnitItTakesASeekableMissileAndNeverAnActFourOne() {
        Sortie sortie = sortie(TorpedoTest.units(false), Act5Specs.SWARM_TAIL);
        sortie.step(Command.NONE);
        sortie.missiles().launch(Ship.START_X, Ship.START_Y + 150, Act5Specs.DIAMOND, Ship.START_X, Ship.START_Y);
        int lock = firstLock(sortie);
        assertEquals(PlayerFire.MISSILE_SERIAL + sortie.missiles().get(0).serial(), lock);

        Sortie walker = sortie(TorpedoTest.units(false), Act5Specs.SWARM_TAIL);
        walker.step(Command.NONE);
        EnemyGun plain = Act5Specs.DIAMOND.withHoming(Act5Specs.DIAMOND.homing().map(spec -> spec.withSeekable(false)));
        walker.missiles().launch(Ship.START_X, Ship.START_Y + 150, plain, Ship.START_X, Ship.START_Y);
        assertEquals(-1, firstLock(walker));
    }

    @Test
    void aNearestInConeHomingShotTakesASeekableMissileOnlyWithNoUnitInItsCone() {
        Sortie lone = sortie(TorpedoTest.units(false), front(TestSpecs.HOMING));
        lone.step(Command.NONE);
        lone.missiles().launch(Ship.START_X, Ship.START_Y + 200, Act5Specs.DIAMOND, Ship.START_X, Ship.START_Y);
        assertEquals(PlayerFire.MISSILE_SERIAL + lone.missiles().get(0).serial(), firstLock(lone));
        TorpedoTest.run(lone, SimStep.ticks(1.0), Command.NONE);
        assertEquals(0, lone.missiles().size(), "shot down");

        Sortie mixed = sortie(TorpedoTest.units(false, unit(0, Ship.START_X + 60)), front(TestSpecs.HOMING));
        until(mixed, Ship.START_Y + 300);
        mixed.missiles().launch(Ship.START_X, Ship.START_Y + 150, Act5Specs.DIAMOND, Ship.START_X, Ship.START_Y);
        assertEquals(mixed.enemy(0).serial(), firstLock(mixed), "the unit first (QA-1)");
    }

    private static Sortie sortie(LevelScript level, Armament.Mount mount) {
        return new Sortie(
                1, TestSpecs.loadout(mount), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static Armament.Mount front(WeaponSpec weapon) {
        return new Armament.Mount(Armament.Slot.FRONT, weapon, weapon);
    }

    @Test
    void rookLocksASeekableMissileOnlyWithNoUnitInHisCone() {
        WeaponSpec homing = TestSpecs.HOMING.scaled(1, java.util.List.of(TestSpecs.muzzle(0, 18, 0)));
        WingmanSpec rook = new WingmanSpec(WingmanSpec.Side.LEFT, 80, WingmanTest.CRAFT, WingmanTest.AI, homing);
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(rook),
                TorpedoTest.units(false),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        TorpedoTest.run(sortie, SimStep.ticks(1.0), Command.NONE);
        Wingman wingman = sortie.wingman().orElseThrow();
        sortie.missiles().launch(wingman.x(), wingman.y() + 220, Act5Specs.DIAMOND, wingman.x(), wingman.y());
        int target = -1;
        for (int i = 0; i < SimStep.ticks(0.6) && target < 0; i++) {
            sortie.step(Command.NONE);
            target = wingman.target();
        }
        assertEquals(PlayerFire.MISSILE_SERIAL + sortie.missiles().get(0).serial(), target);
    }
}
