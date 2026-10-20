package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.ROLL;
import static vanguard.sim.SiegeSpecs.SHELLBACK;
import static vanguard.sim.SiegeSpecs.VENT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D, the Shellback (design/enemies/ground/shellback): a walker with a destroyable mortar vent (a unit
 * part on a unit that is not a boss: tested before the body, turning with its 16 headings, a blast covering both
 * hurting both, its loss silencing the mortar for good and paying its part bounty), the vent's lob every 4 s
 * after its 0.5 s swell, and its curl-roll once at 40 % body HP: curling 0.6 s and rolling 2.5 s along its path
 * at 140 px/s armoured (every damage glances, its hit box the ball), uncurling 0.6 s, its belly taking ×1.5 for
 * 1.5 s, then walking on; an EMP stun pauses it. Bounties: body first the whole 48, vent first 12 + 40. Round 40:
 * a shot or beam from any side whose line crosses the vent hits the vent first (R.a); with a belly row a roll down
 * the screen ends early so its belly shows at that row (R.e).
 */
class ShellbackTest {
    private static final int FIRE = Command.FIRE.bit();

    /** A bolt that kills the vent in two hits (12 damage, 10 a second). */
    private static final WeaponSpec HEAVY =
            TestSpecs.bolt("heavy", 10, 12, 900, new Hitbox(4, 12), TestSpecs.INFINITE, 1, TestSpecs.muzzle(0, 21, 0));

    /** A Shellback spawned on the walk path through {@code path} (play-field points, y up). */
    private static Enemy walker(double... path) {
        Enemy enemy = new Enemy();
        enemy.spawn(
                new Spawn(
                        0,
                        0,
                        SHELLBACK,
                        FlightPath.through(path[0], path[1], path[0], path[1] - 1),
                        24,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(WalkPath.through(path))),
                0);
        return enemy;
    }

    /** One Shellback at t=0 walking up from {@code depth} below the top edge at x 240 (its vent towards the ship). */
    private static Sortie walkingUp(double depth, Loadout loadout) {
        LevelScript level = SiegeSpecs.level(
                List.of(SiegeSpecs.walker(0, SHELLBACK, new WaveSpec.At(240, depth), new WaveSpec.At(240, -3000))),
                List.of(),
                Secondary.NONE,
                Optional.empty());
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static Enemy shellback(Sortie sortie) {
        sortie.step(Command.NONE);
        Enemy enemy = SiegeSpecs.find(sortie, SHELLBACK);
        assertNotNull(enemy);
        return enemy;
    }

    @Test
    void theVentSitsAtTheRearAndTurnsWithTheSixteenHeadings() {
        Enemy down = walker(240, 400, 240, 100);
        assertTrue(down.partAlive());
        assertEquals(1, down.partShare());
        assertEquals(240, down.partX(), 1e-9);
        assertEquals(430, down.partY(), 1e-9, "facing down, its tail is up the screen");
        Enemy up = walker(240, 100, 240, 400);
        assertEquals(240, up.partX(), 1e-9);
        assertEquals(70, up.partY(), 1e-9);
        Enemy left = walker(240, 300, 0, 300);
        assertEquals(270, left.partX(), 1e-9, "facing left, its tail is to the right");
        assertEquals(300, left.partY(), 1e-9);
        Enemy skew = walker(240, 400, 270, 100);
        assertEquals(240, skew.partX(), 1e-9, "a heading 6° off straight down draws (and hits) as straight down");
        assertEquals(84, skew.hitbox().width(), "walking, its hit box is the body's");
    }

    @Test
    void theVentLobsEveryFourSecondsAfterItsHalfSecondSwell() {
        Sortie sortie = walkingUp(100, TestSpecs.LOADOUT);
        long lastTell = -1;
        long lastLob = -1;
        int lobs = 0;
        while (sortie.levelSeconds() < 9) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.MORTAR_TELL) > 0) {
                lastTell = sortie.tick();
            }
            if (sortie.events().count(SimEvents.Type.MORTAR_LOBBED) > 0) {
                assertEquals(SimStep.ticks(Enemy.MORTAR_TELL_SECONDS), sortie.tick() - lastTell, "the swell first");
                if (lastLob >= 0) {
                    assertEquals(SimStep.ticks(4), sortie.tick() - lastLob, "every 4.0 s");
                }
                lastLob = sortie.tick();
                lobs++;
            }
        }
        assertEquals(2, lobs);
    }

    @Test
    void shotsFromBehindHitTheVentFirstAndItsLossSilencesTheMortarForGood() {
        Sortie sortie = walkingUp(200, TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, HEAVY, HEAVY)));
        Enemy shellback = shellback(sortie);
        boolean off = false;
        for (int i = 0; i < SimStep.ticks(1.5) && !off; i++) {
            sortie.step(FIRE);
            off = sortie.events().count(SimEvents.Type.UNIT_PART_DESTROYED) > 0;
            if (shellback.partShare() < 1 && !off) {
                assertEquals(120, shellback.hp(), "the vent takes the shots first");
            }
        }
        assertTrue(off);
        assertFalse(shellback.partAlive());
        assertEquals(0, shellback.partShare());
        assertEquals(120, shellback.hp());
        assertEquals(12, sortie.credits(), "the vent's part bounty");
        assertEquals(0, sortie.kills(), "a part is no kill");
        while (sortie.levelSeconds() < 5.5) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.MORTAR_TELL));
            assertEquals(0, sortie.events().count(SimEvents.Type.MORTAR_LOBBED), "no lob without its vent");
        }
        sortie.destroyEnemy(SiegeSpecs.indexOf(sortie, shellback));
        assertEquals(52, sortie.credits(), "vent first: 12 + 40");
        assertEquals(1, sortie.kills());
    }

    @Test
    void aBeamFromBehindBurnsTheVentFirst() {
        Sortie sortie = walkingUp(200, TestSpecs.loadout(PartASpecs.beamMount(5)));
        Enemy shellback = shellback(sortie);
        for (int i = 0; i < SimStep.ticks(2) && shellback.partAlive(); i++) {
            sortie.step(FIRE);
        }
        assertFalse(shellback.partAlive());
        assertTrue(shellback.hp() > 110, "the body burns only once the vent is gone: " + shellback.hp());
    }

    /**
     * One Shellback at t=0 walking down from {@code depth} below the top edge at x 240 (its vent on its back, away from
     * the ship), with {@code spec}.
     */
    private static Sortie walkingDown(double depth, EnemySpec spec, Loadout loadout) {
        LevelScript level = SiegeSpecs.level(
                List.of(SiegeSpecs.walker(0, spec, new WaveSpec.At(240, depth), new WaveSpec.At(240, 3000))),
                List.of(),
                Secondary.NONE,
                Optional.empty());
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Round 40 R.a: walking down at the ship, its vent behind its head; a shot from below still hits the vent first. */
    @Test
    void shotsFromBelowHitTheVentOnItsBackFirst() {
        Sortie sortie =
                walkingDown(60, SHELLBACK, TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, HEAVY, HEAVY)));
        Enemy shellback = shellback(sortie);
        assertTrue(shellback.partY() > shellback.y(), "the vent lies behind it, away from the ship");
        boolean off = false;
        for (int i = 0; i < SimStep.ticks(1.5) && !off; i++) {
            sortie.step(FIRE);
            off = sortie.events().count(SimEvents.Type.UNIT_PART_DESTROYED) > 0;
            assertEquals(120, shellback.hp(), "the vent takes the shots first");
        }
        assertTrue(off, "the vent is shot off from below");
        sortie.step(FIRE);
        for (int i = 0; i < SimStep.ticks(0.5) && shellback.hp() == 120; i++) {
            sortie.step(FIRE);
        }
        assertTrue(shellback.hp() < 120, "then the body takes them");
    }

    /** Round 40 R.a: a shot over the body that will not cross the vent hits the body. */
    @Test
    void aShotBesideTheVentHitsTheBody() {
        Sortie sortie = walkingDown(60, SHELLBACK, TestSpecs.LOADOUT);
        Enemy shellback = shellback(sortie);
        assertTrue(PlayerFire.segmentCrossesBox(0, 0, 0, 100, 10, 50, 14, 14), "straight up through the box");
        assertFalse(PlayerFire.segmentCrossesBox(0, 0, 0, 100, 20, 50, 14, 14), "beside it");
        assertFalse(PlayerFire.segmentCrossesBox(0, 0, 0, 30, 0, 50, 14, 14), "short of it");
        assertTrue(PlayerFire.segmentCrossesBox(0, 0, 100, 100, 50, 50, 14, 14), "diagonally");
        assertTrue(PlayerFire.segmentCrossesBox(100, 50, -100, 0, 0, 50, 14, 14), "from the side");
        assertEquals(1, shellback.partShare());
    }

    /** Round 40 R.a: a beam from below burns over the body to the vent first. */
    @Test
    void aBeamFromBelowBurnsTheVentFirst() {
        Sortie sortie = walkingDown(60, SHELLBACK, TestSpecs.loadout(PartASpecs.beamMount(5)));
        Enemy shellback = shellback(sortie);
        for (int i = 0; i < SimStep.ticks(2) && shellback.partAlive(); i++) {
            sortie.step(FIRE);
        }
        assertFalse(shellback.partAlive());
        assertTrue(shellback.hp() > 110, "the body burns only once the vent is gone: " + shellback.hp());
    }

    /**
     * Round 40 R.e: with a belly row, a roll drifting down the screen ends early where the scroll alone would carry it
     * below that row before its uncurl ends, so its belly shows at the row; one walking up rolls its full time (above).
     */
    @Test
    void aRollDownTheScreenEndsEarlySoItsBellyShowsAtItsRow() {
        EnemySpec.Roll roll = new EnemySpec.Roll(0.4, 0.6, 2.5, 140, 0.6, 1.5, 1.5, new Hitbox(72, 72), 200);
        EnemySpec spec = SHELLBACK.withRoll(Optional.of(roll));
        Sortie sortie = walkingDown(60, spec, TestSpecs.LOADOUT);
        sortie.step(Command.NONE);
        Enemy shellback = SiegeSpecs.find(sortie, spec);
        assertNotNull(shellback);
        shellback.damage(72);
        assertSame(Enemy.CurlPhase.CURLING, shellback.curlPhase());
        int rolling = 0;
        while (shellback.curlPhase() != Enemy.CurlPhase.UNCURLING) {
            sortie.step(Command.NONE);
            rolling += shellback.curlPhase() == Enemy.CurlPhase.ROLLING ? 1 : 0;
        }
        assertTrue(rolling > 0 && rolling < SimStep.ticks(roll.rollSeconds()), "a shorter roll: " + rolling);
        double scroll = SiegeSpecs.SPEED / SimStep.PER_SECOND;
        double belly = shellback.y() - scroll * SimStep.ticks(roll.uncurlSeconds());
        assertTrue(belly <= 200 && belly > 200 - 2 * (140 + SiegeSpecs.SPEED) / SimStep.PER_SECOND, "at " + belly);
        while (shellback.curlPhase() != Enemy.CurlPhase.BELLY) {
            sortie.step(Command.NONE);
        }
        assertEquals(belly, shellback.y(), 1e-6, "its belly shows at the row");
    }

    @Test
    void aBodyFirstKillPaysTheWhole() {
        Sortie sortie = walkingUp(200, TestSpecs.LOADOUT);
        Enemy shellback = shellback(sortie);
        sortie.destroyEnemy(SiegeSpecs.indexOf(sortie, shellback));
        assertEquals(48, sortie.credits(), "the body's 40 and its whole-kill bonus 8");
    }

    @Test
    void aBlastCoveringTheVentAndTheBodyHurtsBoth() {
        SmartBombSpec bomb = new SmartBombSpec(0.35, 120, 60, 1.0, 1.5, 0.1, 0.8, 0.25);
        Sortie sortie = walkingUp(200, TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Smart Bomb", 1, 3, 0.1, bomb)));
        shellback(sortie);
        sortie.step(Command.SPECIAL.bit());
        for (int i = 0; i < SimStep.ticks(1) && sortie.kills() == 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, sortie.events().count(SimEvents.Type.UNIT_PART_DESTROYED), "in the same step as the body");
        assertEquals(1, sortie.kills());
        assertEquals(52, sortie.credits(), "the vent first, then the body");
    }

    @Test
    void itCurlsOnceAtFortyPercentRollsAlongItsPathUncurlsAndShowsItsBelly() {
        Sortie sortie = walkingUp(400, TestSpecs.LOADOUT);
        Enemy shellback = shellback(sortie);
        SiegeSpecs.runTo(sortie, 0.5);
        assertSame(Enemy.CurlPhase.WALK, shellback.curlPhase());
        assertFalse(shellback.damage(71));
        assertSame(Enemy.CurlPhase.WALK, shellback.curlPhase(), "49 HP: above 40 %");
        assertFalse(shellback.damage(1));
        assertEquals(48, shellback.hp());
        assertSame(Enemy.CurlPhase.CURLING, shellback.curlPhase(), "48 HP: 40 %");
        assertEquals(1, sortie.events().count(SimEvents.Type.CURLED));
        assertTrue(shellback.armoured());
        assertEquals(ROLL.ball(), shellback.hitbox(), "curled, its hit box is the ball");

        assertFalse(shellback.damage(50));
        assertFalse(shellback.damage(50, true));
        assertFalse(shellback.damagePart(50, true));
        assertEquals(48, shellback.hp(), "everything glances off, blasts too");
        assertEquals(1, shellback.partShare());
        assertEquals(1, sortie.events().count(SimEvents.Type.SHOT_GLANCED), "glances are spaced");
        assertTrue(shellback.glances(0, 1), "direct shots spark off");

        double x = shellback.x();
        double walked = shellback.walked();
        int lobs = 0;
        int curl = SimStep.ticks(ROLL.curlSeconds());
        for (int i = 1; i <= curl; i++) {
            double y = shellback.y();
            sortie.step(Command.NONE);
            lobs += sortie.events().count(SimEvents.Type.MORTAR_LOBBED);
            assertEquals(x, shellback.x(), 1e-9, "it stands while it curls");
            assertEquals(y - SiegeSpecs.SPEED / SimStep.PER_SECOND, shellback.y(), 1e-9, "scrolling with the ground");
            if (i == curl / 2) {
                assertEquals(0.5, shellback.curlProgress(), 1e-9);
            }
        }
        assertSame(Enemy.CurlPhase.ROLLING, shellback.curlPhase());
        assertEquals(1, sortie.events().count(SimEvents.Type.ROLL_STARTED));
        assertEquals(walked, shellback.walked(), 1e-9);
        int roll = SimStep.ticks(ROLL.rollSeconds());
        for (int i = 1; i < roll; i++) {
            sortie.step(Command.NONE);
            lobs += sortie.events().count(SimEvents.Type.MORTAR_LOBBED);
            assertTrue(shellback.armoured());
        }
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.UNCURLED));
        assertSame(Enemy.CurlPhase.UNCURLING, shellback.curlPhase());
        assertEquals(walked + ROLL.rollSpeed() * ROLL.rollSeconds(), shellback.walked(), 1e-6, "350 px along its path");
        for (int i = 0; i < SimStep.ticks(ROLL.uncurlSeconds()); i++) {
            assertTrue(shellback.armoured(), "still curled while it uncurls");
            sortie.step(Command.NONE);
            lobs += sortie.events().count(SimEvents.Type.MORTAR_LOBBED);
        }
        assertSame(Enemy.CurlPhase.BELLY, shellback.curlPhase());
        assertFalse(shellback.armoured());
        assertEquals(84, shellback.hitbox().width());
        assertFalse(shellback.damage(10));
        assertEquals(48 - 15, shellback.hp(), 1e-9, "its belly takes ×1.5");
        for (int i = 0; i < SimStep.ticks(ROLL.bellySeconds()); i++) {
            sortie.step(Command.NONE);
            lobs += sortie.events().count(SimEvents.Type.MORTAR_LOBBED);
        }
        assertEquals(0, lobs, "no lob while curled or on its back");
        assertSame(Enemy.CurlPhase.WALK, shellback.curlPhase());
        assertEquals(0, shellback.curlProgress());
        double before = shellback.walked();
        sortie.step(Command.NONE);
        assertEquals(24.0 / SimStep.PER_SECOND, shellback.walked() - before, 1e-9, "walking on at 24 px/s");
        assertFalse(shellback.damage(10));
        assertEquals(23, shellback.hp(), 1e-9);
        assertSame(Enemy.CurlPhase.WALK, shellback.curlPhase(), "it curls once");
    }

    @Test
    void anEmpStunPausesItsRoll() {
        Sortie sortie = walkingUp(400, TestSpecs.LOADOUT);
        Enemy shellback = shellback(sortie);
        shellback.damage(72);
        for (int i = 0; i < SimStep.ticks(ROLL.curlSeconds()) + 30; i++) {
            sortie.step(Command.NONE);
        }
        assertSame(Enemy.CurlPhase.ROLLING, shellback.curlPhase());
        double progress = shellback.curlProgress();
        double walked = shellback.walked();
        double x = shellback.x();
        shellback.empStun(SimStep.ticks(1));
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(progress, shellback.curlProgress(), 1e-12, "its roll's timer waits");
        assertEquals(walked, shellback.walked(), 1e-12, "it does not roll on");
        assertEquals(x, shellback.x(), 1e-12);
        sortie.step(Command.NONE);
        assertTrue(shellback.walked() > walked, "it rolls on after the stun");
    }

    @Test
    void theSpecsCheckTheirParts() {
        assertEquals(0, new EnemySpec.UnitPart("vent", 24, new Hitbox(28, 28), 0, 30, 12, true).firstBonus());
        assertEquals(8, VENT.firstBonus());
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> TestSpecs.NEEDLER.withRoll(Optional.of(ROLL)));
    }
}
