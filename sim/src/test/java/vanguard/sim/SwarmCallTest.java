package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/player/specials, Vrell Swarm Call (L46); Act 7's {@code beyond-gate} limit): a call
 * blooms behind the ship and six drones hunt for 10 s, at most two on a target, spitting thorns that pay as the ship's;
 * enemy bullets that touch a drone hit it and are gone; a boss takes at most 400 from a call; then they wither. Beyond
 * the gate the Airstrike and the Orbital Lance read {@code NO COVER}.
 */
class SwarmCallTest {
    /** The doc's numbers: 6 drones of 24 px, 30 HP, 300 px/s, 270°/s, orbit 90, a thorn of 8 every 0.5 s at 520. */
    static final SwarmSpec SWARM = new SwarmSpec(6, 24, 30, 300, 270, 90, 0.5, 8, 520, 10, 0.5, 0.3, 400, 2);

    static final SpecialSpec CALL = SpecialSpec.cooldown("Vrell Swarm Call", 0.5, 75, 10, SWARM);

    private static final int SPECIAL = Command.SPECIAL.bit();

    /** Targets riding the ground at {@code xs}, 400 px above the ship's row once in. */
    static Sortie sortie(double hp, double... xs) {
        List<GroundUnit> units = new ArrayList<>();
        for (double x : xs) {
            units.add(new GroundUnit(0, x, Act6BSpecs.target("t", Layer.AIR, hp), -1));
        }
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(CALL),
                Act6BSpecs.level(30, HazardSpec.NONE, units, List.of()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the first unit is on the screen, then calls. */
    static SwarmCall call(Sortie sortie) {
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > PlayField.HEIGHT - 60) {
            sortie.step(0);
        }
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.SWARM_CALLED));
        return sortie.special().swarm();
    }

    static void run(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }

    @Test
    void theBloomThenSixDronesAtMostTwoOnATarget() {
        Sortie sortie = sortie(10_000, 140, 240, 340);
        SwarmCall swarm = call(sortie);
        assertTrue(swarm.blooming());
        assertEquals(0, swarm.drones(), "none before the bloom has opened");
        assertTrue(swarm.bloomY() < sortie.ship().y(), "behind the ship");
        run(sortie, 0.4);
        assertEquals(6, swarm.drones());
        int[] hunted = new int[64];
        for (int d = 0; d < 6; d++) {
            assertTrue(swarm.alive(d));
            assertTrue(swarm.target(d) >= 0, "each has a target");
            hunted[swarm.target(d)]++;
        }
        for (int count : hunted) {
            assertTrue(count <= 2, "at most two hunt one");
        }
        assertFalse(sortie.special().ready(), "its cooldown runs");
    }

    @Test
    void theThornsPayAsTheShipsAndTheDronesWitherAfterTenSeconds() {
        Sortie sortie = sortie(60, 240);
        SwarmCall swarm = call(sortie);
        int thorns = 0;
        int hits = 0;
        int kills = 0;
        int withers = 0;
        for (int i = 0; i < SimStep.ticks(11); i++) {
            sortie.step(0);
            thorns += sortie.events().count(SimEvents.Type.SWARM_THORN);
            withers += sortie.events().count(SimEvents.Type.SWARM_WITHER);
            kills += sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.ENEMY_HIT
                        && sortie.events().value(e) == sortie.fire.swarmMount()) {
                    hits++;
                }
            }
        }
        assertTrue(thorns > 0 && hits > 0, "thorns spat and hitting: " + thorns + ", " + hits);
        assertEquals(1, kills, "8 a thorn: the unit's 60 HP down");
        assertTrue(sortie.kills() == 1 && sortie.credits() > 0, "the kill counts and pays as the ship's");
        assertEquals(1, withers);
        assertFalse(swarm.active(), "gone after 10 s and the wither");
    }

    @Test
    void anEnemyBulletThatTouchesADroneHitsItAndIsGone() {
        Sortie sortie = sortie(10_000, 240);
        SwarmCall swarm = call(sortie);
        run(sortie, 0.4);
        int lost = 0;
        for (int shot = 0; shot < 3; shot++) {
            EnemyBullet bullet = sortie.force.bullets().obtain();
            bullet.fire(swarm.x(0), swarm.y(0), 0, 0, 10);
            sortie.step(0);
            assertEquals(1, sortie.events().count(SimEvents.Type.SWARM_DRONE_HIT));
            lost += sortie.events().count(SimEvents.Type.SWARM_DRONE_LOST);
        }
        assertEquals(1, lost, "30 HP: three bullets of 10");
        assertFalse(swarm.alive(0));
    }

    @Test
    void aBossTakesAtMost400FromACall() {
        SetPieceSpec small = Act6GSpecs.piece(0.5);
        PartSpec core = new PartSpec("core", 0, 0, new Hitbox(60, 60), 5000, true, 50, Optional.empty(), 0, 1);
        SetPieceSpec boss = new SetPieceSpec(
                "boss", small.size(), small.body(), 0, List.of(core), small.passes(), small.drop(), small.boss());
        LevelScript level = Act6BSpecs.level(30, HazardSpec.NONE, List.of(), List.of()).toBuilder()
                .setPieces(List.of(boss))
                .build();
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(CALL), level, TestSpecs.RULES.withInvulnerableShip(), 60);
        run(sortie, 4);
        assertTrue(sortie.setPiece(0).present());
        double before = sortie.setPiece(0).barShare();
        sortie.step(SPECIAL);
        run(sortie, 11);
        double taken = (before - sortie.setPiece(0).barShare()) * 5000;
        assertTrue(taken > 100, "the drones hunt a boss's open part: " + taken);
        assertTrue(taken <= 400 + 1e-6, "at most 400 a call: " + taken);
    }

    @Test
    void aCallIsDeterministicAndHashed() {
        Sortie a = sortie(10_000, 200, 280);
        Sortie b = sortie(10_000, 200, 280);
        call(a);
        call(b);
        run(a, 2);
        run(b, 2);
        assertEquals(a.stateHash(), b.stateHash());
    }

    @Test
    void beyondTheGateTheAirstrikeReadsNoCover() {
        SpecialSpec airstrike = Act4SpecialsTest.airstrike(2);
        SpecialLimits gate = new SpecialLimits(List.of(new SpecialLimits.Switch(
                0,
                Optional.of(Set.of(SpecialSpec.Kind.AIRSTRIKE, SpecialSpec.Kind.LANCE)),
                SpecialLimits.Reason.NO_COVER)));
        LevelScript level =
                TorpedoTest.units(false).toBuilder().specialLimits(gate).build();
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(airstrike), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        sortie.step(0);
        assertEquals(SpecialLimits.Reason.NO_COVER, sortie.special().limit());
        assertEquals("NO COVER", sortie.special().limit().word());
        sortie.step(SPECIAL);
        assertEquals(1, sortie.events().count(SimEvents.Type.SPECIAL_DENIED), "refused, no charge spent");
        Sortie swarm =
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(CALL), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        swarm.step(0);
        assertEquals(SpecialLimits.Reason.NONE, swarm.special().limit(), "the Swarm Call works beyond the gate");
    }
}
