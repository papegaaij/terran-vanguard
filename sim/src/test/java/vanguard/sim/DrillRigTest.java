package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part B, step B1: the Drill Rig (design/enemies/ground/drill-rig; design/campaign Level 22): a hardened ground
 * structure with a vital core and four destroyable pumps at its collar's corners, built like the Shellback's unit part
 * (several parts now) with the Hive Node's hardened armour. Its collar fires a ring of 10 shards every 3.5 s while any
 * pump stands, 0.5 s slower for each pump lost, silent once all four are down; hard adds an aimed shard to each ring.
 * The parts stay where they are (the collar does not turn), every part is hardened, a core-first kill pays the whole
 * 56 and so does any order. The rigs are ground targets an escapes objective counts ("3 of 4"), the first destroyed
 * starts the radio's {@code target-destroyed}, and the first geyser's crack its {@code first-geyser}.
 */
class DrillRigTest {
    private static final int FIRE = Command.FIRE.bit();

    /** The collar's ring at medium: 10 lime shards ({@code medium} 6) at 110 px/s every 3.5 s, the first 1.0 s in. */
    static final EnemyGun RING = new EnemyGun(
            3.5,
            1.0,
            1,
            110,
            6,
            false,
            10,
            2 * Math.PI * 9 / 10,
            INFINITE,
            INFINITE,
            Optional.empty(),
            Optional.empty());

    /** Pump {@code p}: 24 HP, 24 px, 4 credits (and 4 of the whole), carrying the ring and slowing it 0.5 s when lost. */
    private static EnemySpec.UnitPart pump(double dx, double dy) {
        return new EnemySpec.UnitPart("pump", 24, new Hitbox(24, 24), dx, dy, 4, true, 4, 0.5);
    }

    /** The pumps: lower left, lower right, upper left, upper right (dy towards the tail: up the screen). */
    static final List<EnemySpec.UnitPart> PUMPS = List.of(pump(-44, -44), pump(44, -44), pump(-44, 44), pump(44, 44));

    /** The Drill Rig at medium: core 240 HP, 96 px, 40 credits, hardened, fixed to the ground, with its four pumps. */
    static final EnemySpec RIG = rig(RING);

    static EnemySpec rig(EnemyGun ring) {
        return EnemySpec.of("drill-rig", 240, new Hitbox(96, 96), Layer.GROUND, 15, false, 40, 0)
                .withGun(Optional.of(ring))
                .withTerrain(true)
                .withHardened(true)
                .withParts(PUMPS);
    }

    private static LevelScript level(
            double speed,
            List<GroundUnit> units,
            Secondary secondary,
            List<RadioCue> radio,
            List<HazardSpec.VentRow> vents) {
        return LevelScript.builder(22, 4)
                .sections(List.of(new Section(40, speed)))
                .groundUnits(units)
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .radio(radio)
                .hazards(HazardSpec.NONE.withVents(vents))
                .build();
    }

    /** One rig at t=0 at {@code x}, scrolling in at {@code speed} px/s; the ship invulnerable. */
    private static Sortie one(EnemySpec rig, double x, double speed, Loadout loadout) {
        LevelScript level = level(speed, List.of(new GroundUnit(0, x, rig, -1)), Secondary.NONE, List.of(), List.of());
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static Enemy rig(Sortie sortie) {
        Enemy rig = PartCSpecs.find(sortie, "drill-rig");
        assertNotNull(rig);
        return rig;
    }

    /** The steps on which the rig fired over {@code seconds}, after {@code lost} pumps were lost at the start. */
    private static List<Integer> volleys(int lost, double seconds) {
        Sortie sortie = one(RIG, Ship.START_X, 15, TestSpecs.LOADOUT);
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        for (int p = 0; p < lost; p++) {
            assertTrue(rig.damagePart(p, 100, false));
        }
        assertEquals(4 - lost, rig.partsStanding());
        List<Integer> volleys = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(seconds); tick++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                volleys.add(tick);
            }
        }
        return volleys;
    }

    @Test
    void itsRingComesEveryThreeAndAHalfSecondsAndHalfASecondSlowerForEachPumpLost() {
        for (int lost = 0; lost < 4; lost++) {
            List<Integer> volleys = volleys(lost, 16);
            assertTrue(volleys.size() >= 3, lost + " lost: " + volleys);
            for (int v = 1; v < volleys.size(); v++) {
                assertEquals(
                        SimStep.ticks(3.5 + 0.5 * lost),
                        volleys.get(v) - volleys.get(v - 1),
                        lost + " pumps lost: every " + (3.5 + 0.5 * lost) + " s");
            }
        }
        assertEquals(List.of(), volleys(4, 16), "silent with all four pumps down");
    }

    @Test
    void aPumpLostBetweenTwoRingsSlowsTheNextAndTheLastSilencesIt() {
        Sortie sortie = one(RIG, Ship.START_X, 15, TestSpecs.LOADOUT);
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        List<Integer> volleys = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(25); tick++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                volleys.add(tick);
                // The pump goes after this ring: its interval (set as it fired) runs on, the next one is longer.
                rig.damagePart(volleys.size() - 1, 100, false);
            }
        }
        assertEquals(4, volleys.size(), "the fourth pump went after the fourth ring: none since");
        assertEquals(SimStep.ticks(3.5), volleys.get(1) - volleys.get(0));
        assertEquals(SimStep.ticks(4.0), volleys.get(2) - volleys.get(1));
        assertEquals(SimStep.ticks(4.5), volleys.get(3) - volleys.get(2));
        assertEquals(0, rig.partsStanding());
        assertFalse(rig.partAlive(3));
    }

    /** A sortie just after the rig's second volley (its first leaves from above the top edge). */
    private static Sortie secondVolley(EnemySpec spec) {
        Sortie sortie = one(spec, Ship.START_X + 120, 15, TestSpecs.LOADOUT);
        int volleys = 0;
        while (volleys < 2) {
            sortie.step(Command.NONE);
            volleys += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        return sortie;
    }

    /** The bullets of the volley just fired: those still within a step's flight of the rig. */
    private static List<EnemyBullet> justFired(Sortie sortie, Enemy rig) {
        List<EnemyBullet> shards = new ArrayList<>();
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (Math.hypot(bullet.x() - rig.x(), bullet.y() - rig.y()) < 4) {
                shards.add(bullet);
            }
        }
        return shards;
    }

    @Test
    void itsRingIsTenMediumShardsRoundItAndHardAddsOneAimedAtTheShip() {
        Sortie medium = secondVolley(RIG);
        Enemy rig = rig(medium);
        double toShip = Math.atan2(medium.ship().y() - rig.y(), medium.ship().x() - rig.x());
        List<EnemyBullet> ring = justFired(medium, rig);
        assertEquals(10, ring.size());
        for (EnemyBullet shard : ring) {
            assertEquals(110, Math.hypot(shard.vx(), shard.vy()), 1e-5);
            assertEquals(6, shard.damage());
            double off = Math.IEEEremainder(Math.atan2(shard.vy(), shard.vx()) - toShip, 2 * Math.PI);
            assertTrue(Math.abs(off) > Math.toRadians(17), "the ring leaves a gap on the ship's line: " + off);
        }

        Sortie hard = secondVolley(rig(RING.withAimedShards(1)));
        Enemy hardRig = rig(hard);
        List<EnemyBullet> hardRing = justFired(hard, hardRig);
        assertEquals(11, hardRing.size(), "the ring and one aimed shard");
        double line = Math.atan2(hard.ship().y() - hardRig.y(), hard.ship().x() - hardRig.x());
        int aimed = 0;
        for (EnemyBullet shard : hardRing) {
            assertEquals(110, Math.hypot(shard.vx(), shard.vy()), 1e-5);
            double off = Math.IEEEremainder(Math.atan2(shard.vy(), shard.vx()) - line, 2 * Math.PI);
            aimed += Math.abs(off) < 1e-3 ? 1 : 0;
        }
        assertEquals(1, aimed, "one shard straight at the ship");
        assertEquals(1, hard.events().count(SimEvents.Type.ENEMY_FIRED), "one volley");

        assertThrows(IllegalArgumentException.class, () -> RING.withAimedShards(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.UnitPart("pump", 24, new Hitbox(24, 24), 0, 0, 4, false, 4, 0.5),
                "only a part that carries the gun slows it");
    }

    @Test
    void itsCollarDoesNotTurnItsPumpsStayAtTheCorners() {
        Sortie sortie = one(RIG, Ship.START_X, 30, TestSpecs.LOADOUT);
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        assertEquals(4, rig.partCount());
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.LEFT.bit());
        }
        assertTrue(Math.abs(rig.facing()) > 0.1, "its ring's aim follows the ship: " + rig.facing());
        double[][] corners = {{-44, -44}, {44, -44}, {-44, 44}, {44, 44}};
        for (int p = 0; p < 4; p++) {
            assertEquals(rig.x() + corners[p][0], rig.partX(p), 1e-9);
            assertEquals(rig.y() + corners[p][1], rig.partY(p), 1e-9);
            assertEquals(24, rig.partBox(p).width());
            assertEquals(1, rig.partShare(p));
        }
        assertEquals(rig.partX(0), rig.partX(), "the first part's, as the Shellback's vent");
        assertEquals(rig.x(), rig.partX(4), "a part it does not have stands at its centre");
        assertFalse(rig.partAlive(4));
    }

    /** One rig with the left pumps over the ship's column; the ship fires {@code bolt}. */
    private static Sortie overTheLeftPumps(WeaponSpec bolt) {
        Loadout loadout = TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, bolt, bolt));
        return one(RIG, Ship.START_X + 44, 60, loadout);
    }

    @Test
    void everyPartIsHardenedShotsWithoutAntiGroundGlanceOff() {
        Sortie sortie = overTheLeftPumps(PartCSpecs.bolt(false));
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        int glanced = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(FIRE);
            glanced += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
        }
        assertTrue(glanced > 10, "the pumps spark: " + glanced);
        for (int p = 0; p < 4; p++) {
            assertEquals(1, rig.partShare(p), "pump " + p);
        }
        assertEquals(240, rig.hp());
        assertEquals(0, sortie.credits());
    }

    @Test
    void antiGroundShotsTakeTheNearerPumpThenTheOneBehindItAndAnyOrderPaysTheWhole() {
        Sortie sortie = overTheLeftPumps(PartCSpecs.bolt(true));
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        List<Integer> order = new ArrayList<>();
        boolean[] standing = {true, true, true, true};
        for (int i = 0; i < SimStep.ticks(4) && order.size() < 2; i++) {
            sortie.step(FIRE);
            for (int p = 0; p < 4; p++) {
                if (standing[p] && !rig.partAlive(p)) {
                    standing[p] = false;
                    order.add(p);
                }
            }
        }
        assertEquals(List.of(0, 2), order, "the lower left pump, then the upper left one over the core");
        assertTrue(rig.partAlive(1) && rig.partAlive(3));
        assertEquals(240, rig.hp(), "a shot over the core towards a pump hits the pump");
        assertEquals(8, sortie.credits(), "4 a pump");
        assertEquals(0, sortie.kills(), "a pump is no kill");
        sortie.destroyEnemy(SiegeSpecs.indexOf(sortie, rig));
        assertEquals(56, sortie.credits(), "40 + the two standing pumps' 4 each: the whole 56 in any order");
        assertEquals(1, sortie.kills());

        Sortie first = one(RIG, Ship.START_X, 60, TestSpecs.LOADOUT);
        first.step(Command.NONE);
        first.destroyEnemy(SiegeSpecs.indexOf(first, rig(first)));
        assertEquals(56, first.credits(), "core first: the whole 56");
    }

    @Test
    void aSmartBombTakesEveryPumpAndSilencesTheRing() {
        SmartBombSpec bomb = new SmartBombSpec(0.35, 120, 60, 1.0, 1.5, 0.1, 0.8, 0.25);
        Sortie sortie = one(
                RIG, Ship.START_X, 60, TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Smart Bomb", 1, 3, 0.1, bomb)));
        sortie.step(Command.NONE);
        Enemy rig = rig(sortie);
        while (rig.y() > 330) {
            sortie.step(Command.NONE);
        }
        sortie.step(Command.SPECIAL.bit());
        int off = 0;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
            off += sortie.events().count(SimEvents.Type.UNIT_PART_DESTROYED);
        }
        assertEquals(4, off, "the Smart Bomb reaches hardened ground: every pump");
        assertEquals(0, rig.partsStanding());
        assertTrue(rig.hp() < 240 && rig.hp() > 0, "the core takes it too: " + rig.hp());
        assertEquals(16, sortie.credits());
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_FIRED), "no ring without its pumps");
        }
    }

    // ---- the objective and the radio ----

    private static final RadioCue TARGET =
            new RadioCue(CueTrigger.TARGET_DESTROYED, 0, "", "Varga", "One down.", false, "neutral", "Varga");
    private static final RadioCue GEYSER =
            new RadioCue(CueTrigger.FIRST_GEYSER, 0, "", "Brandt", "Mind the cracks.", false, "neutral", "Brandt");

    /** Four rigs at t=0..3 across the shelf, "destroy 3 of 4" (one may get away), the target-destroyed line. */
    private static Sortie fourRigs() {
        List<GroundUnit> rigs = List.of(
                new GroundUnit(0, 130, RIG, -1),
                new GroundUnit(1, 340, RIG, -1),
                new GroundUnit(2, 150, RIG, -1),
                new GroundUnit(3, 330, RIG, -1));
        Secondary secondary = new Secondary(0, 100, List.of(), "drill-rig").withEscapesAllowed(1);
        LevelScript level = level(120, rigs, secondary, List.of(TARGET), List.of());
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Destroys the first {@code destroy} rigs once on the field; returns the steps of the radio lines. */
    private static List<Integer> fly(Sortie sortie, int destroy) {
        List<Integer> lines = new ArrayList<>();
        int destroyed = 0;
        for (int tick = 0; !sortie.complete(); tick++) {
            sortie.step(Command.NONE);
            for (int i = sortie.enemyCount() - 1; i >= 0 && destroyed < destroy; i--) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.y() < PlayField.HEIGHT - 60) {
                    sortie.destroyEnemy(i);
                    destroyed++;
                }
            }
            if (sortie.events().count(SimEvents.Type.RADIO) > 0) {
                lines.add(tick);
            }
        }
        return lines;
    }

    @Test
    void theRigsAreGroundTargetsTheEscapesObjectiveCountsAndOneMayGetAway() {
        Sortie met = fourRigs();
        assertEquals(4, met.escapesTotal(), "the ground units of the objective's slug");
        List<Integer> lines = fly(met, 3);
        assertEquals(3, met.escapesDestroyed());
        assertTrue(met.escapesLost() <= 1, "met at the third, whether the fourth left before or after");
        assertTrue(met.secondaryMet());
        assertFalse(met.secondaryFailed());
        assertEquals(1, lines.size(), "the target-destroyed line once, at the first rig: " + lines);

        Sortie failed = fourRigs();
        fly(failed, 2);
        assertEquals(2, failed.escapesLost(), "the rigs that left the bottom edge escaped");
        assertTrue(failed.secondaryFailed(), "two away: failed");
        assertFalse(failed.secondaryMet());
    }

    @Test
    void theTargetDestroyedLineStartsAtTheFirstRigDestroyed() {
        Sortie sortie = fourRigs();
        while (sortie.enemyCount() == 0) {
            sortie.step(Command.NONE);
        }
        long before = sortie.stateHash();
        sortie.destroyEnemy(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.RADIO), "at once");
        assertTrue(sortie.objectivePasses.targetKilled);
        assertTrue(before != sortie.stateHash());
    }

    @Test
    void theFirstGeyserLineStartsWithTheFirstVentTellOnce() {
        HazardSpec.VentRow row = new HazardSpec.VentRow(2, new double[] {100, 240, 380}, 40, 1.2, 0.8, 4.0, 0.4, 10);
        LevelScript level = level(120, List.of(), Secondary.NONE, List.of(GEYSER), List.of(row, row));
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        int tells = 0;
        int lines = 0;
        int firstTell = -1;
        int firstLine = -1;
        for (int tick = 0; tick < SimStep.ticks(15); tick++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.VENT_TELL) > 0) {
                tells++;
                firstTell = firstTell < 0 ? tick : firstTell;
            }
            if (sortie.events().count(SimEvents.Type.RADIO) > 0) {
                lines++;
                firstLine = firstLine < 0 ? tick : firstLine;
            }
        }
        assertTrue(tells > 3, "the grates tell again and again: " + tells);
        assertEquals(1, lines, "once");
        assertEquals(firstTell, firstLine, "with the first crack");
    }

    @Test
    void theRigsAreDeterministicAndAllocateNothing() {
        EnemySpec hard = rig(RING.withAimedShards(1));
        Loadout loadout = TestSpecs.loadout(
                new Armament.Mount(Armament.Slot.FRONT, PartCSpecs.bolt(true), PartCSpecs.bolt(true)));
        LevelScript level = level(
                40,
                List.of(new GroundUnit(0, Ship.START_X + 44, hard, -1), new GroundUnit(2, 120, RIG, -1)),
                new Secondary(0, 100, List.of(), "drill-rig").withEscapesAllowed(1),
                List.of(TARGET),
                List.of());
        Sortie a = new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        Sortie b = new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        for (int i = 0; i < SimStep.ticks(8); i++) {
            a.step(FIRE);
            b.step(FIRE);
        }
        Enemy shot = a.enemy(0).x() == Ship.START_X + 44 ? a.enemy(0) : a.enemy(1);
        assertTrue(shot.partsStanding() < 4, "pumps went: " + shot.partsStanding());
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = new Sortie(
                            1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
                    sortie.step(Command.NONE);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(8); i++) {
                        sortie.step(FIRE);
                    }
                });
        assertEquals(0, allocated, "rings, shards and pumps allocate nothing per step");
    }
}
