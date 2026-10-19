package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.AURA;
import static vanguard.sim.SiegeSpecs.HERALD;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D, the Choir Herald's {@code aura} (design/enemies/air/choir-herald; M8 = a): every other unit within
 * its radius gets a shield ring of its HP at the level, at most 40, full over 0.5 s; kept 1.0 s after leaving,
 * then it fades; refilled at 25 %/s after 2.0 s unhit; collapsed over 0.3 s at the Herald's death; stripped by
 * the EMP Burst, whose stun pauses the Herald's aura. The ring absorbs damage in {@link Enemy#damage}, so every
 * damage path spends it first: direct shots, beam ticks, blasts from above (the player's shells, the Smart
 * Bomb, the Airstrike), the Orbital Lance and the drones' contact; a chain's members never get one. The Herald
 * hovers, drifts, sings its ring of 10 and leaves up; its escorts orbit it at 0.8 × the radius.
 */
class AuraTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final int SPECIAL = Command.SPECIAL.bit();
    /** A Herald fixed to the ground on the air layer, which neither moves nor fires: the aura on its own. */
    private static final EnemySpec STILL_HERALD =
            PartASpecs.dummy(Layer.AIR, 1000, new Hitbox(52, 48), false).withAura(Optional.of(AURA));

    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(8, () -> new Enemy(events), Enemy[]::new);
    private int serials;

    /** A unit of {@code spec} rooted at {@code x} on the top edge (it scrolls in with {@link #step}). */
    private Enemy add(EnemySpec spec, double x) {
        Enemy enemy = pool.obtain();
        enemy.root(spec, 0, x, serials++, -1);
        return enemy;
    }

    /** The units scroll down by {@code scroll} px, then the auras step. */
    private void step(double scroll) {
        events.clear();
        for (int i = 0; i < pool.size(); i++) {
            pool.get(i).move(Ship.START_X, Ship.START_Y, scroll);
        }
        Auras.step(pool, events);
    }

    private void steps(int count) {
        for (int i = 0; i < count; i++) {
            step(0);
        }
    }

    private static int fillTicks() {
        return SimStep.ticks(AURA.fillSeconds());
    }

    @Test
    void aHeraldRingsTheOtherUnitsInItsRadiusFullOverHalfASecond() {
        Enemy herald = add(STILL_HERALD, 200);
        Enemy near = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        Enemy tiny = add(PartASpecs.dummy(Layer.AIR, 1), 200 + 80);
        Enemy far = add(PartASpecs.dummy(Layer.GROUND, 100), 200 + 95);
        Enemy other = add(STILL_HERALD, 140);
        step(300);
        assertEquals(
                2, events.count(SimEvents.Type.RING_SUNG), "round 40 K.a: a ring sung on each (the first-ring cue)");
        assertEquals(herald.serial(), near.ringFeeder());
        assertEquals(40, near.ringMax(), "its HP 100, capped at 40");
        assertEquals(40.0 / fillTicks(), near.ring(), 1e-9, "filling from the step it entered");
        assertEquals(1, tiny.ringMax(), "a tiny unit's ring absorbs its 1 HP: one extra hit");
        assertEquals(-1, far.ringFeeder(), "95 px off: outside the 90 px radius");
        assertEquals(-1, other.ringFeeder(), "never on another Herald");
        assertEquals(-1, herald.ringFeeder(), "never on itself");
        step(0);
        assertEquals(0, events.count(SimEvents.Type.RING_SUNG), "the rings held are not sung again");
        steps(fillTicks() / 2 - 2);
        assertEquals(0.5, near.ringShare(), 1e-9);
        steps(fillTicks() / 2);
        assertEquals(40, near.ring(), 1e-9, "full after 0.5 s");
        assertEquals(1, tiny.ring(), 1e-9);
        steps(SimStep.ticks(3));
        assertEquals(40, near.ring(), 1e-9, "it stays full");
        assertEquals(0, far.ring());
        assertEquals(0, far.ringShare());
    }

    @Test
    void aRingAbsorbsDamageFirstAndOnlyItsOverflowReachesTheHp() {
        add(STILL_HERALD, 200);
        Enemy unit = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        step(300);
        steps(fillTicks());
        assertEquals(40, unit.ring(), 1e-9);
        events.clear();
        assertFalse(unit.damage(30));
        assertEquals(10, unit.ring(), 1e-9);
        assertEquals(100, unit.hp(), "the ring took all of it");
        assertEquals(1, events.count(SimEvents.Type.RING_ABSORBED));
        assertEquals(0, unit.ticksSinceHit(), "a hit all the same (the targeting computer's bar)");
        assertFalse(unit.damage(20, true), "damage from above (blasts, the Airstrike, the Lance) too");
        assertEquals(0, unit.ring(), 1e-9);
        assertEquals(90, unit.hp(), 1e-9, "the overflow beyond the ring");
        assertEquals(
                1, events.count(SimEvents.Type.RING_ABSORBED), "absorb events are spaced (a beam hits every step)");
        assertFalse(unit.damage(5));
        assertEquals(85, unit.hp(), 1e-9, "an empty ring absorbs nothing");
        assertTrue(unit.damage(85), "and the unit dies as before");
    }

    @Test
    void anUnhitRingRefillsAQuarterASecondAfterTwoSecondsWhileTheHeraldLives() {
        add(STILL_HERALD, 200);
        Enemy unit = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        step(300);
        steps(fillTicks());
        unit.damage(40);
        assertEquals(0, unit.ring(), 1e-9);
        steps(SimStep.ticks(AURA.refillDelaySeconds()));
        assertEquals(0, unit.ring(), 1e-9, "no refill for 2.0 s after the hit");
        steps(SimStep.ticks(1));
        assertEquals(10, unit.ring(), 1e-9, "25 % of 40 in the next second");
        steps(SimStep.ticks(4));
        assertEquals(40, unit.ring(), 1e-9, "no more than its maximum");
    }

    @Test
    void aUnitLeavingTheRadiusKeepsItsRingASecondThenItFades() {
        Enemy herald = add(STILL_HERALD, 200);
        Enemy unit = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        step(300);
        steps(fillTicks());
        unit.clampX(200 + 120);
        steps(SimStep.ticks(AURA.keepSeconds()));
        assertEquals(40, unit.ring(), 1e-9, "kept for 1.0 s");
        assertEquals(herald.serial(), unit.ringFeeder());
        steps(fillTicks() / 2);
        assertEquals(20, unit.ring(), 1e-9, "then it fades");
        steps(fillTicks() / 2);
        assertEquals(0, unit.ring());
        assertEquals(-1, unit.ringFeeder(), "gone");
        unit.clampX(260);
        step(0);
        assertEquals(herald.serial(), unit.ringFeeder(), "back in the radius: a new ring");
        assertEquals(40.0 / fillTicks(), unit.ring(), 1e-9);
    }

    @Test
    void theRingsCollapseOverThreeTenthsOfASecondAtTheHeraldsDeath() {
        add(STILL_HERALD, 200);
        Enemy unit = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        step(300);
        steps(fillTicks());
        unit.collapseRing();
        int ticks = SimStep.ticks(AURA.collapseSeconds());
        steps(ticks / 2);
        assertEquals(0.5, unit.ringCollapse(), 1e-9);
        assertEquals(20, unit.ring(), 1e-9);
        steps(ticks / 2);
        assertEquals(0, unit.ring());
        assertEquals(-1, unit.ringFeeder());
        assertEquals(0, unit.ringCollapse());
    }

    @Test
    void theEmpStripsEveryRingAndTheHeraldsStunPausesItsAura() {
        Enemy herald = add(STILL_HERALD, 200);
        Enemy unit = add(PartASpecs.dummy(Layer.GROUND, 100), 260);
        step(300);
        steps(fillTicks());
        assertTrue(unit.stripRing());
        assertEquals(0, unit.ring());
        herald.empStun(SimStep.ticks(1.5));
        Enemy late = add(PartASpecs.dummy(Layer.GROUND, 100), 230);
        late.move(Ship.START_X, Ship.START_Y, 300);
        steps(SimStep.ticks(1.5) - 1);
        assertEquals(-1, late.ringFeeder(), "a stunned Herald rings nobody new");
        assertEquals(herald.serial(), unit.ringFeeder(), "its rings wait");
        steps(SimStep.ticks(1));
        assertEquals(herald.serial(), late.ringFeeder(), "it sings again after its stun");
        assertEquals(0, unit.ring(), "the stripped ring refills only 2.0 s after the stun");
        steps(SimStep.ticks(AURA.refillDelaySeconds()));
        assertTrue(unit.ring() > 0, "then it refills");
    }

    // --- in a sortie --------------------------------------------------------------------------

    /** A level at 60 px/s of these ground units and ground objects. */
    private static LevelScript level(List<LevelScript.GroundUnit> units) {
        return new LevelScript(
                1,
                1,
                0,
                List.of(new LevelScript.Section(40, 60)),
                List.of(),
                List.of(),
                units,
                0,
                List.of(),
                new LevelScript.Secondary(0.8, 50),
                List.of());
    }

    /** The target and a Herald 70 px to its right, both fixed to the ground and entering together. */
    private static LevelScript ringedTarget(EnemySpec target) {
        return level(List.of(
                new LevelScript.GroundUnit(0, Ship.START_X, target, -1),
                new LevelScript.GroundUnit(0, Ship.START_X + 70, STILL_HERALD, -1)));
    }

    private static EnemySpec target(Layer layer) {
        return PartASpecs.dummy(layer, 100, new Hitbox(100, 40), false);
    }

    private static Sortie sortie(LevelScript level, Loadout loadout) {
        return new Sortie(1, loadout, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /**
     * Waits until {@code spec}'s unit carries its full ring, then steps with {@code commands} (the first step) and
     * {@code then} (the others) until its ring or its HP drops: the ring must have absorbed first (its HP drops
     * only once the ring is empty), with a {@link SimEvents.Type#RING_ABSORBED}.
     */
    private static void assertRingFirst(Sortie sortie, EnemySpec spec, int commands, int then) {
        Enemy target = null;
        for (int i = 0; i < SimStep.ticks(4) && (target == null || target.ringShare() < 1); i++) {
            sortie.step(Command.NONE);
            target = SiegeSpecs.find(sortie, spec);
        }
        assertNotNull(target);
        assertEquals(40, target.ring(), 1e-9, "full before the attack");
        int command = commands;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(command);
            command = then;
            if (target.ring() < 40 || target.hp() < 100) {
                assertEquals(1, sortie.events().count(SimEvents.Type.RING_ABSORBED));
                assertTrue(target.hp() == 100 || target.ring() == 0, "the HP only after the ring");
                return;
            }
        }
        throw new AssertionError("nothing hit the target");
    }

    @Test
    void directShotsSpendTheRingFirst() {
        EnemySpec target = target(Layer.GROUND);
        assertRingFirst(sortie(ringedTarget(target), TestSpecs.LOADOUT), target, FIRE, FIRE);
    }

    @Test
    void beamTicksSpendTheRingFirst() {
        EnemySpec target = target(Layer.GROUND);
        assertRingFirst(sortie(ringedTarget(target), TestSpecs.loadout(PartASpecs.beamMount(1))), target, FIRE, FIRE);
    }

    @Test
    void theShellsBlastsFromAboveSpendTheRingFirst() {
        WeaponSpec mortar = new WeaponSpec(
                "hammer-mortar",
                "mortar",
                "mortar",
                WeaponSpec.Delivery.LOBBED,
                true,
                1.25,
                20,
                0,
                new Hitbox(10, 10),
                200,
                TestSpecs.INFINITE,
                1,
                28,
                0,
                Math.PI,
                0.6,
                48,
                List.of(TestSpecs.muzzle(0, 21, 0)));
        EnemySpec target = target(Layer.GROUND);
        assertRingFirst(
                sortie(
                        ringedTarget(target),
                        TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, mortar, mortar))),
                target,
                FIRE,
                FIRE);
    }

    @Test
    void theSmartBombSpendsTheRingFirst() {
        SmartBombSpec bomb = new SmartBombSpec(0.35, 120, 60, 1.0, 1.5, 0.1, 0.8, 0.25);
        EnemySpec target = target(Layer.GROUND);
        Sortie sortie = sortie(
                ringedTarget(target), TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Smart Bomb", 1, 3, 0.1, bomb)));
        assertRingFirst(sortie, target, SPECIAL, Command.NONE);
        Enemy hit = SiegeSpecs.find(sortie, target);
        assertEquals(0, hit.ring(), 1e-9);
        assertEquals(100 - (120 - 40), hit.hp(), 1e-9, "120 on the ring of 40, the rest on the HP");
    }

    @Test
    void theAirstrikesBlastsSpendTheRingFirst() {
        EnemySpec target = target(Layer.GROUND);
        assertRingFirst(
                sortie(
                        ringedTarget(target),
                        TestSpecs.LOADOUT.withSpecial(
                                new SpecialSpec("Airstrike", 1, 4, 0.1, AirstrikeTest.AIRSTRIKE))),
                target,
                SPECIAL,
                Command.NONE);
    }

    @Test
    void theOrbitalLanceSpendsTheRingFirst() {
        EnemySpec target = target(Layer.GROUND);
        assertRingFirst(
                sortie(ringedTarget(target), TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.lance(1))),
                target,
                SPECIAL,
                Command.NONE);
    }

    @Test
    void theDronesContactSpendsTheRingFirst() {
        EnemySpec target = target(Layer.AIR);
        assertRingFirst(
                sortie(ringedTarget(target), TestSpecs.loadout(PartASpecs.droneBay(Armament.Slot.LEFT_WING, 1))),
                target,
                Command.NONE,
                Command.NONE);
    }

    @Test
    void aChainsMembersNeverCarryARing() {
        WaveSpec herald =
                TestSpecs.wave(0, WaveSpec.Formation.SINGLE, HERALD, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        WaveSpec chain = new WaveSpec(
                1,
                WaveSpec.Formation.SNAKE,
                FarsideTest.COILWYRM,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of(List.of(
                        new WaveSpec.At(PlayField.WIDTH / 2.0, -60),
                        new WaveSpec.At(PlayField.WIDTH / 2.0, 300),
                        new WaveSpec.At(PlayField.WIDTH / 2.0, 700))),
                Optional.empty());
        Sortie sortie = SiegeSpecs.sortie(TestSpecs.level(20, List.of(herald, chain)));
        boolean passed = false;
        while (sortie.levelSeconds() < 10) {
            sortie.step(Command.NONE);
            Enemy singer = SiegeSpecs.find(sortie, HERALD);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                if (unit.chain() == null) {
                    continue;
                }
                assertEquals(-1, unit.ringFeeder(), "a chain's member");
                if (singer != null && Math.hypot(unit.x() - singer.x(), unit.y() - singer.y()) < AURA.radius()) {
                    passed = true;
                }
            }
        }
        assertTrue(passed, "the chain crossed the aura");
    }

    @Test
    void theHeraldsDeathCollapsesItsRingsAndBreaksItsEscortsOff() {
        Sortie sortie = SiegeSpecs.sortie(heraldLevel());
        SiegeSpecs.runTo(sortie, 5);
        Enemy herald = SiegeSpecs.find(sortie, HERALD);
        Enemy escort = escort(sortie);
        assertEquals(herald.serial(), escort.ringFeeder());
        sortie.destroyEnemy(SiegeSpecs.indexOf(sortie, herald));
        assertEquals(1, sortie.events().count(SimEvents.Type.RINGS_COLLAPSED));
        assertEquals(1, eventValue(sortie, SimEvents.Type.RINGS_COLLAPSED), "its one escort's ring");
        sortie.step(Command.NONE);
        assertTrue(escort.ringCollapse() > 0);
        for (int i = 0; i < SimStep.ticks(AURA.collapseSeconds()); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(-1, escort.ringFeeder());
        assertEquals(0, escort.ring());
    }

    @Test
    void aHeraldCarrierWaveHoversAndItsEscortsWaveCirclesIt() {
        EnemySpec needler = TestSpecs.needler(EnemyGun.aimed(2.5, 0.8, 1, 150, 1, false));
        Sortie sortie = SiegeSpecs.sortie(TestSpecs.level(
                30,
                List.of(
                        TestSpecs.wave(
                                0,
                                WaveSpec.Formation.CARRIER_ESCORTS,
                                HERALD,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE),
                        TestSpecs.wave(
                                0.1,
                                WaveSpec.Formation.CARRIER_ESCORTS,
                                needler,
                                3,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))));
        SiegeSpecs.runTo(sortie, 6);
        Enemy herald = SiegeSpecs.find(sortie, HERALD);
        assertEquals(PlayField.HEIGHT - 175, herald.y(), 1e-6, "hovering, not flying through as a spawner does");
        assertEquals(PlayField.WIDTH / 2.0, herald.x(), 32 + 1e-6);
        int ringed = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            if (unit.spec() == needler) {
                assertEquals(72, Math.hypot(unit.x() - herald.x(), unit.y() - herald.y()), 0.5);
                ringed += unit.ringFeeder() == herald.serial() ? 1 : 0;
            }
        }
        assertEquals(3, ringed);
    }

    /** The Herald at t=0 and one Needler escort after it, whose gun deals 1 (the song's bullets deal 4). */
    private static LevelScript heraldLevel() {
        EnemySpec needler = TestSpecs.needler(EnemyGun.aimed(2.5, 0.8, 1, 150, 1, false));
        return TestSpecs.level(
                30,
                List.of(
                        TestSpecs.wave(
                                0, WaveSpec.Formation.SINGLE, HERALD, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                        TestSpecs.wave(
                                0.1,
                                WaveSpec.Formation.CARRIER_ESCORTS,
                                needler,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)));
    }

    private static Enemy escort(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals("needler")) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    private static int eventValue(Sortie sortie, SimEvents.Type type) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                return events.value(i);
            }
        }
        throw new AssertionError("no " + type);
    }

    @Test
    void theHeraldHoversDriftsSingsItsRingAndLeavesUpItsEscortsOrbitingInsideItsAura() {
        Sortie sortie = SiegeSpecs.sortie(heraldLevel());
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        int songs = 0;
        double lastSong = -1;
        double left = -1;
        double exitX = Double.NaN;
        while (sortie.levelSeconds() < 20) {
            sortie.step(Command.NONE);
            Enemy herald = SiegeSpecs.find(sortie, HERALD);
            if (herald == null) {
                if (left < 0) {
                    left = sortie.levelSeconds();
                }
                continue;
            }
            Enemy escort = escort(sortie);
            double t = sortie.levelSeconds();
            if (t > 4 && t < 14) {
                assertEquals(PlayField.HEIGHT - 175, herald.y(), 1e-6, "hovering 175 px below the top edge");
                minX = Math.min(minX, herald.x());
                maxX = Math.max(maxX, herald.x());
                assertNotNull(escort);
                double distance = Math.hypot(escort.x() - herald.x(), escort.y() - herald.y());
                // Within the Herald's last step's drift: the escort may move before it in the step.
                assertEquals(AURA.radius() * AURA.escortRadiusShare(), distance, 0.5, "72 px: inside its aura");
                assertEquals(herald.serial(), escort.ringFeeder());
                assertEquals(4, escort.ringMax(), "the Needler's 4 HP");
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ENEMY_FIRED && events.value(i) == 4) {
                    songs++;
                    if (lastSong >= 0) {
                        assertEquals(4.0, t - lastSong, 1.5 / SimStep.PER_SECOND, "a song every 4.0 s");
                    }
                    lastSong = t;
                    int ring = 0;
                    for (int b = 0; b < sortie.bulletCount(); b++) {
                        EnemyBullet bullet = sortie.bullet(b);
                        if (Math.hypot(bullet.x() - herald.x(), bullet.y() - herald.y()) < 5) {
                            ring++;
                        }
                    }
                    assertEquals(10, ring, "a ring of 10");
                }
            }
            if (t > 13 && herald.y() > PlayField.HEIGHT - 175 + 1) {
                if (Double.isNaN(exitX)) {
                    exitX = herald.x();
                }
                assertEquals(exitX, herald.x(), 1e-9, "straight up");
            }
        }
        assertTrue(maxX - minX > 32, "its side-to-side drift: " + (maxX - minX));
        assertTrue(maxX - minX <= 64 + 1e-6, "at most 32 px either way: " + (maxX - minX));
        assertEquals(3, songs, "1.0 s, 5.0 s and 9.0 s into its 12 s hover");
        assertTrue(left > 14 && left < 17, "it left up after its 12 s hover: " + left);
        Enemy escort = escort(sortie);
        assertTrue(escort == null || escort.ringFeeder() < 0 || escort.ring() < 4, "its rings no longer fed");
    }

    @Test
    void theEmpBurstStripsTheRingsOnTheScreen() {
        LevelScript level = heraldLevel();
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(1)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        SiegeSpecs.runTo(sortie, 5);
        Enemy escort = escort(sortie);
        assertEquals(4, escort.ring(), 1e-9);
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.RINGS_STRIPPED));
        assertEquals(1, eventValue(sortie, SimEvents.Type.RINGS_STRIPPED));
        assertEquals(0, escort.ring());
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, escort.ring(), "the Herald stunned, then the refill's delay");
        }
    }
}
