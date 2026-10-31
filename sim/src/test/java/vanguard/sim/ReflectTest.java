package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies, Reflect; design/enemies/air/mirror-interceptor): a mirror over a 120° frontal
 * arc. The player's non-beam shots arriving in the arc bounce back as slow magenta {@code small} bullets (4 at 110 px/s)
 * along the mirror image of their path, one per 0.15 s, the rest absorbed; the flanks and the rear are open. A beam
 * heats it (no hull damage) until it breaks at its heat, cooling at 50 % a second after 0.3 s; a {@code shield-breaker}
 * hit or the EMP shatters it at once (the breaker's hit on to the hull). Down 3 s, then a 0.4 s regrowth. Rook's shots
 * and a bounced bolt are absorbed, never bounced.
 */
class ReflectTest {
    private static final int FIRE = Command.FIRE.bit();

    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(4, () -> new Enemy(events), Enemy[]::new);

    /** A mirrored unit on its own, facing straight down (its first step faces its flight: a still unit's 0). */
    private Enemy unit() {
        Enemy enemy = pool.obtain();
        enemy.root(Act6ASpecs.MIRRORED, 0, 240, 1, -1);
        enemy.move(Ship.START_X, Ship.START_Y, 300);
        return enemy;
    }

    private void steps(Enemy unit, int count) {
        for (int i = 0; i < count; i++) {
            unit.move(Ship.START_X, Ship.START_Y, 0);
        }
    }

    @Test
    void theArcMeetsShotsFromAheadOnlyTheFlanksAndTheRearAreOpen() {
        Enemy unit = unit();
        Mirroring mirror = unit.mirror();
        EnemySpec.Mirror spec = Act6ASpecs.MIRROR;
        assertEquals(0, mirror.facing(), 1e-9, "facing straight down");
        assertTrue(mirror.meets(spec, 0, 900), "a shot flying straight up into it");
        assertTrue(mirror.meets(spec, Math.sin(Math.toRadians(59)), Math.cos(Math.toRadians(59))), "59° off");
        assertFalse(mirror.meets(spec, Math.sin(Math.toRadians(61)), Math.cos(Math.toRadians(61))), "61°: the flank");
        assertFalse(mirror.meets(spec, 900, 0), "across: the flank");
        assertFalse(mirror.meets(spec, 0, -900), "from behind");
        assertFalse(mirror.meets(spec, 0, 0), "a still shot");
    }

    @Test
    void itBouncesOnePlayerShotPerGapBackDownItsLineAsASlowSmallBulletAndAbsorbsTheRest() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X, Act6ASpecs.MIRRORED, -1)), List.of()),
                Act6ASpecs.front());
        Enemy unit = Act6ASpecs.until(sortie, "mirror-interceptor", 120);
        int bounced = 0;
        int absorbed = 0;
        int steps = SimStep.ticks(1.5);
        for (int i = 0; i < steps; i++) {
            sortie.step(FIRE);
            bounced += sortie.events().count(SimEvents.Type.MIRROR_BOUNCED);
            absorbed += sortie.events().count(SimEvents.Type.MIRROR_ABSORBED);
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_HIT), "nothing reaches the hull");
        }
        assertEquals(120, unit.hp(), "the mirror took every shot");
        assertTrue(bounced > 0 && absorbed > 0, bounced + " bounced, " + absorbed + " absorbed");
        assertTrue(bounced <= steps / SimStep.ticks(0.15) + 1, "at most one per 0.15 s: " + bounced);
        int seen = 0;
        for (int b = 0; b < sortie.bulletCount(); b++) {
            EnemyBullet bullet = sortie.bullet(b);
            if (bullet.bounced()) {
                seen++;
                assertEquals(110, Math.hypot(bullet.vx(), bullet.vy()), 1e-6, "slow: 110 px/s");
                assertEquals(0, bullet.vx(), 1e-6, "straight back down its line");
                assertTrue(bullet.vy() < 0);
                assertEquals(4, bullet.damage(), "small: 4 whatever the shot did");
            }
        }
        assertTrue(seen > 0);
    }

    @Test
    void aShotIntoItAtAnAngleComesBackAsItsMirrorImage() {
        Enemy unit = unit();
        Mirroring mirror = unit.mirror();
        double angle = Math.toRadians(30);
        mirror.reflect(Act6ASpecs.MIRROR, 900 * Math.sin(angle), 900 * Math.cos(angle));
        assertEquals(110 * Math.sin(angle), mirror.reflectedVx(), 1e-9, "the same way across");
        assertEquals(-110 * Math.cos(angle), mirror.reflectedVy(), 1e-9, "back down");
        assertFalse(mirror.bounceReady(), "its gap runs");
        steps(unit, SimStep.ticks(0.15));
        assertTrue(mirror.bounceReady());
    }

    @Test
    void rooksShotsAndABouncedBoltAreAbsorbedNeverBounced() {
        Armament armament = TestSpecs.LOADOUT.armament();
        int[] bounces = {0};
        PlayerFire.Hits hits = new PlayerFire.Hits() {
            @Override
            public void enemyDestroyed(int index, int mount) {}

            @Override
            public void groundDestroyed(int index) {}

            @Override
            public void triggerReleased(int index) {}

            @Override
            public void mineDestroyed(int index) {}

            @Override
            public void partDestroyed(int piece, int part, int mount) {}

            @Override
            public void unitPartDestroyed(int index, int part, int mount) {}

            @Override
            public boolean bounce(double x, double y, double vx, double vy, double damage) {
                bounces[0]++;
                return true;
            }
        };
        Reflections reflections = new Reflections(armament, events, hits);
        Enemy unit = unit();
        Shot rook = new Shot();
        rook.fire(TestSpecs.PULSE, armament.size(), 240, 300, 0);
        events.clear();
        assertTrue(reflections.meet(rook, unit));
        assertEquals(1, events.count(SimEvents.Type.MIRROR_ABSORBED), "Rook's shot sparks");
        Shot bolt = new Shot();
        bolt.fire(TestSpecs.PULSE, 0, 240, 300, 0);
        bolt.markBounced();
        assertTrue(reflections.meet(bolt, unit));
        assertEquals(2, events.count(SimEvents.Type.MIRROR_ABSORBED), "a bounced bolt never bounces twice");
        assertEquals(0, bounces[0]);
        Shot own = new Shot();
        own.fire(TestSpecs.PULSE, 0, 240, 300, 0);
        assertTrue(reflections.meet(own, unit));
        assertEquals(1, bounces[0], "the player's own shot bounces");
        Shot flank = new Shot();
        flank.fire(TestSpecs.PULSE, 0, 240, 300, Math.PI / 2);
        assertFalse(reflections.meet(flank, unit), "from the flank it reaches the hull");
    }

    @Test
    void aBeamHeatsItWithoutHullDamageAndBreaksItThenBurnsTheHullWhileItIsDown() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X, Act6ASpecs.MIRRORED, -1)), List.of()),
                PartASpecs.beamMount(5));
        Enemy unit = Act6ASpecs.until(sortie, "mirror-interceptor", 300);
        int broken = 0;
        int steps = 0;
        while (broken == 0 && steps++ < SimStep.ticks(3)) {
            sortie.step(FIRE);
            broken += sortie.events().count(SimEvents.Type.MIRROR_BROKEN);
            if (broken == 0) {
                assertEquals(120, unit.hp(), "no hull damage while the mirror stands");
            }
        }
        assertEquals(1, broken);
        // 130 heat at 130 a second (its reach grows over the beam's first steps): about 1 s.
        assertTrue(steps >= SimStep.ticks(1.0) && steps <= SimStep.ticks(1.4), "about 1 s: " + steps);
        assertTrue(unit.mirror().down());
        Act6ASpecs.run(sortie, SimStep.ticks(0.5), FIRE);
        assertTrue(unit.hp() < 120, "down: the beam burns the hull");
    }

    @Test
    void heatCoolsAtHalfItsMaximumASecondAfterAPause() {
        Enemy unit = unit();
        EnemySpec.Mirror spec = Act6ASpecs.MIRROR;
        assertFalse(unit.mirror().heat(spec, unit, 100));
        steps(unit, SimStep.ticks(0.3));
        assertEquals(100, unit.mirror().heat(), 1e-9, "no cooling for 0.3 s");
        steps(unit, SimStep.ticks(1.0));
        assertEquals(100 - 65, unit.mirror().heat(), 1.5, "then 65 (half of 130) a second");
        steps(unit, SimStep.ticks(1.0));
        assertEquals(0, unit.mirror().heat(), 1e-9);
    }

    @Test
    void aBreakersHitShattersItAndGoesOnToTheHullAtFullDamage() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X - 20, Act6ASpecs.MIRRORED, -1)), List.of()),
                new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA));
        Enemy unit = Act6ASpecs.until(sortie, "mirror-interceptor", 330);
        int shattered = 0;
        for (int i = 0; i < SimStep.ticks(3) && shattered == 0; i++) {
            sortie.step(FIRE);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.MIRROR_BROKEN) {
                    assertEquals(1, sortie.events().value(e), "shattered, not burnt");
                    shattered++;
                }
            }
        }
        assertEquals(1, shattered);
        assertEquals(120 - Act5Specs.TESLA.damage(), unit.hp(), 1e-9, "the breaker's whole hit on the hull");
        assertTrue(unit.mirror().down());
    }

    @Test
    void theEmpShattersItDownThreeSecondsThenItRegrows() {
        Enemy unit = unit();
        events.clear();
        unit.empStun(SimStep.ticks(3));
        assertEquals(1, events.count(SimEvents.Type.MIRROR_BROKEN));
        assertTrue(unit.mirror().down());
        assertFalse(unit.mirror().meets(Act6ASpecs.MIRROR, 0, 900), "down: every shot hits the hull");
        events.clear();
        steps(unit, SimStep.ticks(3.0) - 1);
        assertTrue(unit.mirror().down(), "3 s, on the real steps (the stun's own)");
        steps(unit, 1);
        assertEquals(1, events.count(SimEvents.Type.MIRROR_REGROWING));
        assertFalse(unit.mirror().standing(), "regrowing: still down");
        steps(unit, SimStep.ticks(0.4));
        assertTrue(unit.mirror().standing(), "back after its 0.4 s shimmer");
    }

    @Test
    void itTurnsTowardTheShipAtItsRateWhileItHoldsItsStation() {
        Enemy unit = unit();
        Mirroring mirror = unit.mirror();
        unit.phase = Enemy.Phase.HOLD;
        // The ship level with it far to its left: a quarter turn away. 60°/s is 1° a step.
        double before = mirror.facing();
        mirror.turn(Act6ASpecs.MIRROR, unit, unit.x() - 300, unit.y());
        assertEquals(Math.toRadians(1), Math.abs(mirror.facing() - before), 1e-9);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            mirror.turn(Act6ASpecs.MIRROR, unit, unit.x() - 300, unit.y());
        }
        assertEquals(
                Math.abs(Enemy.heading(-300, 0)), Math.abs(mirror.facing()), 1e-9, "on the ship's bearing after 1.5 s");
        assertTrue(mirror.aimed(Act6ASpecs.MIRROR, unit, unit.x() - 300, unit.y()));
        assertFalse(mirror.aimed(Act6ASpecs.MIRROR, unit, unit.x() + 300, unit.y()), "its gun waits off its facing");
    }

    @Test
    void aMirrorIsAPlainUnitsNeverBesideAShield() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act6ASpecs.MIRRORED.withShield(Optional.of(Act5Specs.shield(10))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.Mirror(0, 130, 60, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30));
    }
}
