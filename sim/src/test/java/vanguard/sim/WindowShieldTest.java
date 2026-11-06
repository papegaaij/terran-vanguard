package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a (design/enemies, Window shields; design/enemies/space/gate-warden): a shelled unit is closed
 * (invulnerable) except from its line's telegraph's first step to its beam's last; a breaker hit or the EMP opens it
 * 3 s (restarted by another hit, its own window inside it kept to its end), then it regrows 0.4 s; closed, shots spark
 * off it (flakes, a throttled tick), a beam stops on it, a breaker arc opens it and hits at full damage, a Plasma Arc
 * strike is absorbed and ends the chain; the seekers skip it while an open target is on the screen.
 */
class WindowShieldTest {
    private static final double SHIP_X = 60;
    private static final double SHIP_Y = 96;

    private final SimEvents events = new SimEvents(256);
    private final Pool<Enemy> pool = new Pool<>(4, () -> new Enemy(events), Enemy[]::new);

    private Enemy unit(EnemySpec spec, double x) {
        Enemy enemy = pool.obtain();
        enemy.root(spec, 0, x, pool.size(), -1);
        enemy.move(SHIP_X, SHIP_Y, 200);
        return enemy;
    }

    /** {@code count} steps of the pool's moves, lines and shells; returns the events of {@code type}. */
    private int steps(int count, SimEvents.Type type) {
        int counted = 0;
        for (int i = 0; i < count; i++) {
            events.clear();
            for (int j = 0; j < pool.size(); j++) {
                pool.get(j).move(SHIP_X, SHIP_Y, 0);
            }
            LineBeam.stepAll(pool, SHIP_X, SHIP_Y, events);
            WindowShell.stepAll(pool);
            counted += events.count(type);
        }
        return counted;
    }

    @Test
    void itIsClosedExceptFromItsTelegraphsFirstStepToItsBeamsLast() {
        Enemy warden = unit(Act7ASpecs.shelled(0.5), 300);
        assertTrue(warden.invulnerable());
        assertFalse(warden.damage(50), "closed: no damage");
        assertEquals(360, warden.hp());
        int closedSteps = 0;
        while (warden.window().closed()) {
            steps(1, SimEvents.Type.SHELL_OPEN);
            closedSteps++;
        }
        assertEquals(SimStep.ticks(0.5), closedSteps, "closed until its first telegraph");
        assertTrue(warden.lineBeam().telegraphing(), "it opens with the telegraph's first step");
        int open = 0;
        int closes = 0;
        do {
            open++;
            closes += steps(1, SimEvents.Type.SHELL_CLOSE);
        } while (!warden.window().closed());
        assertEquals(SimStep.ticks(0.8) + SimStep.ticks(1.0), open, "open over the telegraph and the live beam");
        assertEquals(1, closes);
        assertFalse(warden.lineBeam().running(), "closed with the beam's end, no regrowth without a break");
        warden.window().hold();
        assertFalse(warden.damage(50));
        assertEquals(310, warden.hp(), "open: full damage");
    }

    @Test
    void aBreakerHoldOpensItThreeSecondsRestartedByAnotherHitThenItRegrows() {
        Enemy warden = unit(Act7ASpecs.shelled(30), 300);
        steps(1, SimEvents.Type.SHELL_OPEN);
        events.clear();
        warden.window().hold();
        assertEquals(1, events.count(SimEvents.Type.SHELL_BROKEN));
        assertEquals(1, events.count(SimEvents.Type.SHELL_OPEN));
        assertFalse(warden.invulnerable());
        steps(SimStep.ticks(2), SimEvents.Type.SHELL_CLOSE);
        warden.window().hold();
        assertEquals(0, steps(SimStep.ticks(3) - 1, SimEvents.Type.SHELL_CLOSE), "restarted: 3 s from the 2nd hit");
        assertTrue(warden.window().held());
        steps(1, SimEvents.Type.SHELL_CLOSE);
        assertTrue(warden.window().regrowing(), "the hold over, the shell regrows (still open)");
        assertFalse(warden.invulnerable());
        assertEquals(0, steps(SimStep.ticks(0.4) - 1, SimEvents.Type.SHELL_CLOSE));
        assertEquals(1, steps(1, SimEvents.Type.SHELL_CLOSE), "regrown in 0.4 s");
        assertTrue(warden.window().closed());
    }

    @Test
    void itsOwnWindowInsideTheHoldKeepsItOpenToTheWindowsEnd() {
        Enemy warden = unit(Act7ASpecs.shelled(2.5), 300);
        warden.window().hold();
        int steps = 0;
        while (!warden.lineBeam().running()) {
            steps(1, SimEvents.Type.SHELL_CLOSE);
            steps++;
        }
        int windowEnd = steps + SimStep.ticks(0.8) + SimStep.ticks(1.0);
        assertTrue(windowEnd > SimStep.ticks(3), "the window runs past the hold");
        int open = steps;
        while (!warden.window().closed()) {
            steps(1, SimEvents.Type.SHELL_CLOSE);
            open++;
        }
        assertEquals(windowEnd + SimStep.ticks(0.4), open, 1, "open to the window's end, then the regrowth");
    }

    @Test
    void theEmpOpensItForItsHoldOnTheRealSteps() {
        Enemy warden = unit(Act7ASpecs.shelled(30), 300);
        steps(1, SimEvents.Type.SHELL_OPEN);
        events.clear();
        warden.empStun(SimStep.ticks(1.5));
        assertEquals(1, events.count(SimEvents.Type.SHELL_BROKEN));
        assertFalse(warden.window().closed());
        assertEquals(0, steps(SimStep.ticks(3), SimEvents.Type.SHELL_CLOSE), "3 s, the stun's 1.5 s included");
        assertEquals(1, steps(SimStep.ticks(0.4), SimEvents.Type.SHELL_CLOSE));
    }

    @Test
    void whileAnOpenTargetIsOnTheScreenTheSeekersSkipAClosedShell() {
        Enemy warden = unit(Act7ASpecs.shelled(30), 300);
        Enemy other = unit(Act6ASpecs.air(10), 200);
        steps(1, SimEvents.Type.SHELL_OPEN);
        assertTrue(warden.shunned, "closed, beside an open target");
        assertFalse(other.shunned);
        warden.window().hold();
        steps(1, SimEvents.Type.SHELL_OPEN);
        assertFalse(warden.shunned, "open");
        pool.free(1);
        Enemy alone = unit(Act7ASpecs.shelled(30), 100);
        steps(SimStep.ticks(4), SimEvents.Type.SHELL_OPEN);
        assertTrue(alone.window().closed() && warden.window().closed());
        assertFalse(alone.shunned || warden.shunned, "nothing open on the screen: a closed one is picked");
    }

    /** A sortie with {@code mount} and a closed shelled unit at x {@code x} (its first line late). */
    private static Sortie shelledSortie(Armament.Mount mount, double x, List<GroundUnit> more) {
        List<GroundUnit> units = new ArrayList<>(List.of(new GroundUnit(0, x, Act7ASpecs.shelled(30), -1)));
        units.addAll(more);
        return Act6ASpecs.sortie(Act6ASpecs.level(30, units, List.of()), mount);
    }

    @Test
    void shotsSparkOffAClosedShellAndPayNothing() {
        Sortie sortie = shelledSortie(Act6ASpecs.front(), Ship.START_X, List.of());
        int flakes = 0;
        int ticks = 0;
        int lastTick = -100;
        int closest = Integer.MAX_VALUE;
        for (int i = 0; i < SimStep.ticks(3.5); i++) {
            sortie.step(Command.FIRE.bit());
            Enemy warden = Act6BSpecs.find(sortie, "shelled");
            assertTrue(warden == null || warden.hp() == 360, "no damage");
            flakes += sortie.events().count(SimEvents.Type.SHELL_FLAKE);
            if (sortie.events().count(SimEvents.Type.SHELL_TICK) > 0) {
                ticks++;
                closest = Math.min(closest, i - lastTick);
                lastTick = i;
            }
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_HIT));
        }
        assertTrue(flakes > 10, "every hit a flake: " + flakes);
        assertTrue(ticks > 0 && ticks < flakes, "the tick throttled");
        assertTrue(closest >= SimStep.ticks(0.15), "a tick every 0.15 s at most: " + closest);
        assertEquals(0, sortie.credits());
    }

    @Test
    void aBreakerArcOpensItAndHitsTheHullAtFullDamage() {
        Sortie sortie = shelledSortie(
                new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                Ship.START_X - 20,
                List.of());
        int broken = Act7ASpecs.count(sortie, SimStep.ticks(6), Command.FIRE.bit(), SimEvents.Type.SHELL_BROKEN);
        assertTrue(broken > 0, "the arc picks the closed shell when nothing else is in reach");
        Enemy warden = Act6BSpecs.find(sortie, "shelled");
        assertTrue(warden == null || warden.hp() < 360, "full damage through the open shell");
    }

    @Test
    void theArcPrefersAnOpenTargetOverAClosedShell() {
        Sortie sortie = shelledSortie(
                new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                Ship.START_X - 20,
                List.of(new GroundUnit(0, Ship.START_X + 40, Act6ASpecs.air(400), -1)));
        int broken = Act7ASpecs.count(sortie, SimStep.ticks(4), Command.FIRE.bit(), SimEvents.Type.SHELL_BROKEN);
        assertEquals(0, broken, "an open target in reach comes first");
    }

    @Test
    void aBeamStopsOnAClosedShellHarmless() {
        Sortie sortie = shelledSortie(PartASpecs.beamMount(5), Ship.START_X, List.of());
        int flakes = Act7ASpecs.count(sortie, SimStep.ticks(4), Command.FIRE.bit(), SimEvents.Type.SHELL_FLAKE);
        assertTrue(flakes > 0);
        assertEquals(360, Act6BSpecs.find(sortie, "shelled").hp(), "a beam on a closed shell stops there, harmless");
    }

    @Test
    void aPlasmaArcStrikeIsAbsorbedAndEndsTheChain() {
        double row = Ship.START_Y + 21 + 100;
        List<GroundUnit> units = List.of(
                new GroundUnit(0, Ship.START_X, Act7ASpecs.shelled(30), -1),
                new GroundUnit(0, Ship.START_X + 60, Act6BSpecs.target("behind", Layer.AIR, 100), -1));
        Sortie sortie = Act6ASpecs.sortie(Act6ASpecs.level(20, units, List.of()), Act6BSpecs.arcMount());
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > row) {
            sortie.step(0);
        }
        sortie.step(Command.FIRE.bit());
        assertEquals(1, sortie.events().count(SimEvents.Type.CHAIN_STRIKE), "absorbed: the chain ends there");
        assertEquals(1, sortie.events().count(SimEvents.Type.SHELL_FLAKE));
        assertEquals(100, Act6BSpecs.find(sortie, "behind").hp(), 1e-9);
        assertEquals(360, Act6BSpecs.find(sortie, "shelled").hp(), 1e-9);
    }
}
