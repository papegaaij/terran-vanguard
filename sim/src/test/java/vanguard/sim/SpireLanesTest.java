package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Round 40 (the Siege Spire's lanes, proposal 4a of the Act 2 playtest, pending the user's review): the four pods
 * fire straight down their columns (safe lanes between them) and take {@link BossSpec.AimTurns aim turns}, one pod
 * at a time aiming one orb at the ship after a 0.6 s glow, every 2 s, in the pods' order; the maw launches one
 * Wraith a launch with at most two alive. On {@link PartHSpecs}'s test Spire, phase 1 firing only the orbs.
 */
class SpireLanesTest {
    static final List<Integer> PODS = List.of(PartHSpecs.POD_1, PartHSpecs.POD_2, PartHSpecs.POD_3, PartHSpecs.POD_4);
    static final double EVERY = 2;
    static final double GLOW = 0.6;

    /** The test Spire with the pods' lanes and aim turns, {@code wraiths} a launch and at most {@code most} alive. */
    static LevelScript.SetPieceSpec spire(int wraiths, int most) {
        LevelScript.SetPieceSpec base = PartHSpecs.spire(wraiths, 10, 120);
        BossSpec b = base.boss().orElseThrow();
        List<BossSpec.Attack> attacks = new ArrayList<>(b.attacks());
        attacks.set(
                PartHSpecs.ORBS,
                new BossSpec.Attack(
                        "orbs",
                        BossSpec.Pattern.AIMED,
                        PartHSpecs.ORB,
                        0.15,
                        false,
                        1,
                        0,
                        0,
                        0,
                        PODS,
                        true,
                        Optional.empty(),
                        PODS,
                        Optional.of(new BossSpec.AimTurns(EVERY, GLOW))));
        List<BossSpec.Phase> phases = new ArrayList<>(b.phases());
        BossSpec.Phase roots = phases.get(0);
        phases.set(
                0,
                new BossSpec.Phase(
                        roots.name(),
                        roots.untilParts(),
                        roots.left(),
                        List.of(PartHSpecs.ORBS),
                        false,
                        roots.stream(),
                        roots.exposes(),
                        roots.bendRadians(),
                        roots.seconds(),
                        roots.delaySeconds(),
                        roots.move(),
                        roots.windows(),
                        roots.arena()));
        BossSpec.Phase maw = phases.get(1);
        BossSpec.Windows windows = maw.windows().orElseThrow();
        BossSpec.Spawn spawn = windows.spawns().getFirst();
        phases.set(
                1,
                new BossSpec.Phase(
                        maw.name(),
                        maw.untilParts(),
                        maw.left(),
                        maw.attacks(),
                        maw.alternate(),
                        maw.stream(),
                        maw.exposes(),
                        maw.bendRadians(),
                        maw.seconds(),
                        maw.delaySeconds(),
                        maw.move(),
                        Optional.of(new BossSpec.Windows(
                                windows.groups(),
                                windows.everySeconds(),
                                windows.openSeconds(),
                                windows.offsetSeconds(),
                                windows.all(),
                                List.of(new BossSpec.Spawn(
                                        spawn.name(),
                                        spawn.enemy(),
                                        wraiths,
                                        spawn.speed(),
                                        spawn.arcRadians(),
                                        spawn.glideSeconds(),
                                        most)))),
                        maw.arena()));
        BossSpec boss = new BossSpec(
                b.arriveSeconds(),
                b.x(),
                b.hoverY(),
                b.descentSpeed(),
                b.sineAmplitude(),
                b.sinePeriod(),
                b.layer(),
                b.midBoss(),
                b.barName(),
                b.parSeconds(),
                b.chains(),
                attacks,
                phases,
                b.engagesOnArrival(),
                b.armoured(),
                b.poses(),
                b.deathSeconds(),
                b.arena());
        return new LevelScript.SetPieceSpec(
                base.slug(),
                base.size(),
                base.body(),
                base.contactDamage(),
                base.parts(),
                base.passes(),
                base.drop(),
                Optional.of(boss));
    }

    private static Sortie sortie(int wraiths, int most) {
        return PartHSpecs.sortie(PartHSpecs.level(spire(wraiths, most)), TestSpecs.RULES.withInvulnerableShip());
    }

    private static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    /** A pod's shot in the last step. */
    record Shot(int pod, boolean down) {}

    /** The pods' shots in the last step: each fired orb found at its muzzle, straight down or not. */
    private static List<Shot> shots(Sortie sortie) {
        List<Shot> shots = new ArrayList<>();
        SetPiece boss = boss(sortie);
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            int pod = -1;
            for (int p : PODS) {
                if (Math.abs(boss.partX(p) - events.x(i)) < 1e-6 && Math.abs(boss.partY(p) - events.y(i)) < 1e-6) {
                    pod = p;
                }
            }
            assertTrue(pod >= 0, "only the pods fire in phase 1");
            for (int k = 0; k < sortie.bulletCount(); k++) {
                EnemyBullet bullet = sortie.bullet(k);
                if (Math.abs(bullet.renderX(0) - events.x(i)) < 1e-9
                        && Math.abs(bullet.renderY(0) - events.y(i)) < 1e-9) {
                    shots.add(new Shot(pod, Math.abs(Math.abs(bullet.heading()) - Math.PI) < 1e-9));
                }
            }
        }
        return shots;
    }

    @Test
    void thePodsFireDownTheirColumnsAndTakeTurnsToAimOneOrbAfterAGlow() {
        Sortie sortie = sortie(2, 4);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        SetPiece boss = boss(sortie);
        List<Integer> tells = new ArrayList<>();
        List<Integer> tellTicks = new ArrayList<>();
        List<Integer> aimed = new ArrayList<>();
        List<Integer> aimedTicks = new ArrayList<>();
        int down = 0;
        for (int tick = 0; tick < SimStep.ticks(4 * EVERY + 0.1); tick++) {
            int glowing = boss.aimPart(PartHSpecs.ORBS);
            sortie.step(0);
            for (int pod : PartHSpecs.values(sortie, SimEvents.Type.AIM_TELL)) {
                tells.add(pod);
                tellTicks.add(tick);
                assertTrue(boss.partAimGlowing(pod));
                assertEquals(pod, boss.aimPart(PartHSpecs.ORBS));
            }
            for (Shot shot : shots(sortie)) {
                if (shot.down()) {
                    down++;
                    assertNotEquals(glowing, shot.pod(), "a glowing pod holds its column fire, also at its aimed orb");
                } else {
                    aimed.add(shot.pod());
                    aimedTicks.add(tick);
                }
            }
        }
        assertEquals(PODS, tells, "one pod at a time, in turn");
        assertEquals(PODS, aimed, "each glowing pod aims one orb");
        for (int k = 0; k < 4; k++) {
            assertEquals(SimStep.ticks(GLOW), aimedTicks.get(k) - tellTicks.get(k), "the orb after the 0.6 s glow");
            if (k > 0) {
                assertEquals(SimStep.ticks(EVERY), aimedTicks.get(k) - aimedTicks.get(k - 1), "a turn every 2 s");
            }
        }
        assertTrue(down >= 15, "the columns keep firing straight down: " + down);
    }

    @Test
    void aPodKilledInItsGlowLosesItsOrbAndTheNextLivingPodTakesTheNextTurn() {
        Sortie sortie = sortie(2, 4);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        assertEquals(List.of(PartHSpecs.POD_1), PartHSpecs.until(sortie, SimEvents.Type.AIM_TELL, EVERY + 0.1, 0));
        PartHSpecs.until(sortie, SimEvents.Type.AIM_TELL, EVERY + 0.1, 0);
        assertEquals(PartHSpecs.POD_2, boss(sortie).aimPart(PartHSpecs.ORBS));
        sortie.destroyPart(0, PartHSpecs.POD_2);
        for (int tick = 0; tick < SimStep.ticks(GLOW + 0.1); tick++) {
            sortie.step(0);
            for (Shot shot : shots(sortie)) {
                assertTrue(shot.down(), "no aimed orb from the dead pod");
            }
        }
        sortie.destroyPart(0, PartHSpecs.POD_3);
        assertEquals(
                List.of(PartHSpecs.POD_4),
                PartHSpecs.until(sortie, SimEvents.Type.AIM_TELL, EVERY, 0),
                "the dead pods' turns pass to the next living one");
    }

    @Test
    void theMawLaunchesOneWraithAtATimeWithAtMostTwoAlive() {
        Sortie sortie = sortie(1, 2);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        PartHSpecs.killRoots(sortie);
        assertEquals(
                1, PartHSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0).size());
        assertEquals(1, PartHSpecs.values(sortie, SimEvents.Type.BOSS_LAUNCHED).size(), "one a launch");
        assertEquals(
                1,
                PartHSpecs.until(sortie, SimEvents.Type.BOSS_LAUNCHED, 8.1, 0).size());
        assertEquals(2, sortie.launchedAlive());

        Sortie capped = sortie(3, 2);
        PartHSpecs.until(capped, SimEvents.Type.BOSS_SETTLED, 5, 0);
        PartHSpecs.killRoots(capped);
        PartHSpecs.until(capped, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        assertEquals(2, PartHSpecs.values(capped, SimEvents.Type.BOSS_LAUNCHED).size(), "at most two alive");
        assertEquals(2, capped.launchedAlive());
    }

    @Test
    void aimTurnsAreForAnAimedAttackWithAShorterGlow() {
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.AimTurns(2, 2));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.AimTurns(0, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpec.Attack(
                        "spit",
                        BossSpec.Pattern.FAN,
                        PartHSpecs.FAN,
                        0.15,
                        false,
                        9,
                        0,
                        0,
                        0,
                        List.of(PartHSpecs.MAW),
                        false,
                        Optional.empty(),
                        List.of(),
                        Optional.of(new BossSpec.AimTurns(EVERY, GLOW))));
    }

    @Test
    void theLanesAreDeterministicAndAllocateNothing() {
        Sortie a = sortie(1, 2);
        Sortie b = sortie(1, 2);
        for (int i = 0; i < SimStep.ticks(20); i++) {
            int commands = (i / 50) % 2 == 0 ? Command.LEFT.bit() | Command.FIRE.bit() : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(1, 2);
                    PartHSpecs.steps(sortie, 5);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(20); i++) {
                        sortie.step((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
                    }
                });
        assertEquals(0, allocated, "the column fire and the aim turns allocate nothing per step");
    }
}
