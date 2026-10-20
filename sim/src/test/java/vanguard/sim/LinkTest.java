package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C, the Tendril Anchor's {@code link} (design/enemies/ground/tendril-anchor; M9 = a): a gate's two
 * anchors enter as ground units and span a tendril between their centres, dormant for 1.0 s, then touching
 * the ship for 6 with its own 0.5 s cooldown; an anchor's death breaks it for good; only a beam damages it and
 * goes on past it (risk 6: it stops at rock, not at the tendril, and an anchor at the rock's foot is hit
 * first); a tendril leaving the screen standing fails a links objective; Rook holds below a standing tendril
 * and targets its nearer anchor; nothing allocates.
 */
class LinkTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final int UP = Command.UP.bit();
    static final EnemySpec.Link LINK = new EnemySpec.Link(40, 1.0, 6, 0.5, 6, 8);
    static final EnemySpec ANCHOR = anchor(Optional.empty());

    /** A Tendril Anchor: 12 HP, 30 × 30, on the ground, bounty 12, fixed to the ground; {@code gun} its hard orb. */
    static EnemySpec anchor(Optional<EnemyGun> gun) {
        return new EnemySpec(
                        "tendril-anchor",
                        12,
                        new Hitbox(30, 30),
                        Layer.GROUND,
                        0,
                        false,
                        12,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        gun,
                        Optional.empty(),
                        Optional.empty(),
                        true)
                .withLink(Optional.of(LINK));
    }

    static LevelScript level(double speed, Secondary secondary, GroundSpec.Gate... gates) {
        return LevelScript.builder(1, 3)
                .sections(List.of(new Section(60, speed)))
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .build()
                .withGates(List.of(gates));
    }

    private static Sortie sortie(LevelScript level, Armament.Mount mount) {
        return PartASpecs.sortie(level, mount);
    }

    private static Armament.Mount pulse() {
        return new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2);
    }

    private static int count(Sortie sortie, SimEvents.Type type, int value) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type && events.value(i) == value) {
                n++;
            }
        }
        return n;
    }

    /** The index of the anchor at {@code x} among the sortie's enemies; -1 for none. */
    private static int anchorAt(Sortie sortie, double x) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().link().isPresent() && enemy.x() == x) {
                return i;
            }
        }
        return -1;
    }

    private static double damageTaken(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        return defences.armourLost() - defences.shield();
    }

    @Test
    void theGatesAnchorsJoinTheGroundUnitsInTimeOrder() {
        EnemySpec turret = PartASpecs.dummy(Layer.GROUND, 10);
        LevelScript level = PartASpecs.units(PartASpecs.unit(0.5, 50, turret), PartASpecs.unit(3, 60, turret))
                .withGates(List.of(new GroundSpec.Gate(1, 100, 380, ANCHOR)));
        List<GroundUnit> units = level.groundUnits();
        assertEquals(4, units.size());
        assertEquals(
                List.of(0.5, 1.0, 1.0, 3.0), units.stream().map(GroundUnit::t).toList());
        assertEquals(100, units.get(1).x());
        assertEquals(380, units.get(2).x());
        assertEquals(1, level.ground().gates().size());
        // A later rebuild keeps them.
        assertEquals(
                1, level.toBuilder().launchDrop(true).build().ground().gates().size());
        assertNull(PartASpecs.sortie(PartASpecs.units(), pulse()).links(), "no links without gates");
    }

    @Test
    void aTendrilIsDormantForASecondThenTouchesTheShipFor6WithItsCooldown() {
        // At 30 px/s the band reaches the ship's hull (held at the top) 0.93 s after the pair entered.
        Sortie sortie = sortie(level(30, new Secondary(0.8, 50), new GroundSpec.Gate(2, 100, 380, ANCHOR)), pulse());
        int entered = -1;
        int flared = -1;
        List<Integer> touches = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(6); step++) {
            double before = damageTaken(sortie);
            sortie.step(UP);
            Links links = sortie.links();
            if (entered < 0 && links.size() > 0) {
                entered = step;
                Tendril tendril = links.get(0);
                assertTrue(tendril.dormant());
                assertEquals(0, tendril.flare());
                assertEquals(100, tendril.ax());
                assertEquals(380, tendril.bx());
                assertEquals(tendril.ay(), tendril.by());
                assertEquals(sortie.enemy(anchorAt(sortie, 100)).y(), tendril.ay());
            }
            if (count(sortie, SimEvents.Type.TENDRIL_FLARE, 0) > 0) {
                flared = step;
                assertFalse(links.get(0).dormant());
            }
            if (sortie.events().count(SimEvents.Type.TENDRIL_TOUCH) > 0) {
                assertEquals(1, count(sortie, SimEvents.Type.TENDRIL_TOUCH, Links.TOUCH_SHIP));
                touches.add(step);
                assertEquals(6, damageTaken(sortie) - before, 0.1, "a medium touch, the ship not pushed");
            }
        }
        assertTrue(entered >= 0 && flared >= 0);
        assertEquals(SimStep.ticks(1.0), flared - entered, "dormant for 1.0 s");
        assertEquals(flared, (int) touches.getFirst(), "it touches from its flare on (the hull was in its band)");
        assertTrue(touches.size() >= 3, "touches: " + touches);
        for (int k = 1; k < touches.size(); k++) {
            assertEquals(SimStep.ticks(0.5), touches.get(k) - touches.get(k - 1), "the touch's own cooldown");
        }
    }

    @Test
    void killingAnAnchorBreaksItForGoodAndMeetsTheLinksObjective() {
        Sortie sortie = sortie(
                level(130, Secondary.links(80, "BARRIERS"), new GroundSpec.Gate(0.5, 100, 380, ANCHOR)), pulse());
        assertEquals(1, sortie.escapesTotal(), "a links objective counts the gate's pairs");
        while (sortie.links().size() == 0 || sortie.links().get(0).dormant()) {
            sortie.step(Command.NONE);
        }
        int credits = sortie.credits();
        sortie.destroyEnemy(anchorAt(sortie, 100));
        Tendril tendril = sortie.links().get(0);
        assertTrue(tendril.broken());
        assertFalse(tendril.burnt());
        assertFalse(tendril.aAlive());
        assertTrue(tendril.bAlive());
        assertEquals(1, count(sortie, SimEvents.Type.TENDRIL_BROKEN, 0));
        assertTrue(sortie.secondaryMet());
        assertEquals(1, sortie.escapesDestroyed());
        double factor = TestSpecs.SCORING.creditFactor();
        assertEquals((12 + 80) * factor, sortie.credits() - credits, 1, "the anchor's bounty and the objective");
        int touches = 0;
        for (int step = 0; step < SimStep.ticks(5); step++) {
            sortie.step(Command.NONE);
            touches += sortie.events().count(SimEvents.Type.TENDRIL_TOUCH);
        }
        assertEquals(0, touches, "a broken tendril never touches");
        assertEquals(0, sortie.links().size(), "withered and gone");
        assertTrue(sortie.secondaryMet());
    }

    @Test
    void aBeamBurnsItThroughAndGoesOnPastIt() {
        Sortie sortie = sortie(
                level(130, new Secondary(0.8, 50), new GroundSpec.Gate(0.5, 100, 380, ANCHOR)),
                PartASpecs.beamMount(1));
        int burns = 0;
        int burning = 0;
        int broken = -1;
        int credits = 0;
        for (int step = 0; step < SimStep.ticks(3) && broken < 0; step++) {
            credits = sortie.credits();
            sortie.step(FIRE);
            burns += sortie.events().count(SimEvents.Type.TENDRIL_BURN);
            Beam beam = sortie.beam(0);
            if (sortie.links().size() > 0 && sortie.links().get(0).burning()) {
                burning++;
                assertSame(Beam.Contact.NONE, beam.contact(), "the beam goes on past the tendril");
                assertTrue(beam.endY() > sortie.links().get(0).ay());
            }
            if (count(sortie, SimEvents.Type.TENDRIL_BROKEN, 1) > 0) {
                broken = step;
            }
        }
        assertTrue(broken >= 0, "burnt through");
        Tendril tendril = sortie.links().get(0);
        assertTrue(tendril.broken() && tendril.burnt());
        assertTrue(tendril.aAlive() && tendril.bAlive(), "the anchors stand on");
        assertEquals(0, tendril.hpShare());
        assertEquals(1, burns, "one burn started");
        // 40 HP at 45 DPS (0.75 a step): 54 steps.
        assertEquals((int) Math.ceil(40 / (45.0 / SimStep.PER_SECOND)), burning);
        assertEquals(8 * TestSpecs.SCORING.creditFactor(), sortie.credits() - credits, 1, "its burnt bounty");
    }

    @Test
    void otherShotsPassThroughIt() {
        Sortie sortie = sortie(level(130, new Secondary(0.8, 50), new GroundSpec.Gate(0.5, 100, 380, ANCHOR)), pulse());
        boolean past = false;
        for (int step = 0; step < SimStep.ticks(3); step++) {
            sortie.step(FIRE);
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_HIT));
            if (sortie.links().size() > 0) {
                assertEquals(1, sortie.links().get(0).hpShare());
                for (int i = 0; i < sortie.shotCount(); i++) {
                    past |= sortie.shot(i).y() > sortie.links().get(0).ay() + 10;
                }
            }
        }
        assertTrue(past, "shots flew on past it");
    }

    @Test
    void theBeamStopsAtRockButNotAtTheTendril() {
        // The tendril (row 145) runs 55 px below the block's foot (row 200) over the beam's column; the beam
        // burns it from t = 1.0 s (it enters) to 1.9 s, the block's foot enters at 1.54 s.
        Terrain block = new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(new double[] {200, 280, 280, 200}, new double[] {200, 200, 2_200, 2_200})));
        LevelScript level = TestSpecs.withGround(
                level(130, new Secondary(0.8, 50), new GroundSpec.Gate(1, 100, 380, ANCHOR)),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(block, 10, 28, 0.12, 0.5))));
        assertEquals(1, level.ground().gates().size(), "the terrain keeps the gates");
        Sortie sortie = sortie(level, PartASpecs.beamMount(1));
        boolean both = false;
        for (int step = 0; step < SimStep.ticks(3); step++) {
            sortie.step(FIRE);
            if (sortie.links().size() > 0 && sortie.links().get(0).burning()) {
                Beam beam = sortie.beam(0);
                both |= beam.contact() == Beam.Contact.ROCK;
                assertTrue(beam.endY() > sortie.links().get(0).ay());
            }
        }
        assertTrue(both, "the beam burnt the tendril and ended at the rock in one step");
    }

    @Test
    void anAnchorAtTheRocksFootIsHitBeforeTheRockEndsTheBeam() {
        // The right anchor (x 400, row 145 at its centre) stands with its top 4 px under a block's foot.
        double foot = 145 + 15 + 4;
        Terrain block = new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(
                        new double[] {340, 480, 480, 340}, new double[] {foot, foot, foot + 2_000, foot + 2_000})));
        Sortie sortie = sortie(
                TestSpecs.withGround(
                        level(130, new Secondary(0.8, 50), new GroundSpec.Gate(1, 100, 400, ANCHOR)),
                        g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(block, 10, 28, 0.12, 0.5)))),
                PartASpecs.beamMount(1));
        while (sortie.ship().x() < 400) {
            sortie.step(Command.RIGHT.bit());
        }
        int broken = -1;
        for (int step = 0; step < SimStep.ticks(3) && broken < 0; step++) {
            sortie.step(FIRE);
            if (count(sortie, SimEvents.Type.TENDRIL_BROKEN, 0) > 0) {
                broken = step;
            }
        }
        assertTrue(broken >= 0, "the beam killed the anchor at the rock's foot");
        assertTrue(sortie.links().get(0).aAlive() && !sortie.links().get(0).bAlive());
    }

    @Test
    void aTendrilLeavingTheScreenStandingFailsTheLinksObjective() {
        Sortie sortie = sortie(
                level(
                        130,
                        Secondary.links(80, "BARRIERS"),
                        new GroundSpec.Gate(0.5, 100, 380, ANCHOR),
                        new GroundSpec.Gate(2, 120, 360, ANCHOR)),
                pulse());
        assertEquals(2, sortie.escapesTotal());
        int failed = 0;
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(Command.NONE);
            failed += sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED);
            Links links = sortie.links();
            for (int i = 0; i < links.size(); i++) {
                assertTrue(Math.max(links.get(i).ay(), links.get(i).by()) > -20, "gone once it left");
            }
        }
        assertEquals(1, failed, "failed at the first tendril that left standing");
        assertFalse(sortie.secondaryMet());
        assertEquals(0, sortie.escapesDestroyed());
    }

    @Test
    void rookHoldsBelowAStandingTendril() {
        Sortie sortie = WingmanTest.sortie(
                level(130, new Secondary(0.8, 50), new GroundSpec.Gate(1.5, 60, 420, ANCHOR)), WingmanSpec.Side.RIGHT);
        Hitbox box = sortie.wingman().orElseThrow().hull().parts().getFirst().box();
        // He flies before the ground scrolls in a step: the tendril may have come one step's scroll closer.
        double lag = 130 * SimStep.SECONDS;
        boolean held = false;
        for (int step = 0; step < SimStep.ticks(5); step++) {
            sortie.step(UP);
            Links links = sortie.links();
            Wingman rook = sortie.wingman().orElseThrow();
            if (links.size() == 0 || !links.get(0).standing()) {
                continue;
            }
            double line = links.get(0).ay();
            if (line - box.height() / 2 - Wingman.TENDRIL_CLEARANCE > 40) {
                assertTrue(
                        rook.y() + box.height() / 2 + Wingman.TENDRIL_CLEARANCE <= line + lag + 1e-9,
                        "below the tendril: " + rook.y() + " under " + line);
                assertEquals(0, count(sortie, SimEvents.Type.TENDRIL_TOUCH, Links.TOUCH_ROOK), "never touched");
                held |= rook.y() + box.height() / 2 + Wingman.TENDRIL_CLEARANCE > line - lag - 1;
            }
        }
        assertTrue(held, "the tendril pushed him down");
    }

    @Test
    void rookTargetsTheNearerAnchorFirst() {
        // A decoy nearer to him enters after the gate.
        EnemySpec decoy = PartASpecs.dummy(Layer.GROUND, 1e6);
        LevelScript level = LevelScript.builder(1, 3)
                .sections(List.of(new Section(60, 130)))
                .groundUnits(List.of(PartASpecs.unit(1.3, 290, decoy)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build()
                .withGates(List.of(new GroundSpec.Gate(1, 60, 320, ANCHOR)));
        Sortie sortie = WingmanTest.sortie(level, WingmanSpec.Side.RIGHT);
        int onAnchor = 0;
        int onDecoy = 0;
        for (int step = 0; step < SimStep.ticks(4); step++) {
            sortie.step(Command.NONE);
            Wingman rook = sortie.wingman().orElseThrow();
            int right = anchorAt(sortie, 320);
            if (right < 0 || rook.target() < 0) {
                continue;
            }
            if (rook.target() == sortie.enemy(right).serial()) {
                onAnchor++;
            } else {
                onDecoy++;
            }
        }
        assertTrue(onAnchor > 0, "he targets the nearer anchor");
        assertEquals(0, onDecoy, "and not the decoy while the tendril stands");
    }

    @Test
    void onHardTheAnchorsFireTheirAimedOrbs() {
        EnemySpec armed = anchor(Optional.of(TestSpecs.NEEDLER_GUN));
        Sortie sortie = sortie(level(130, new Secondary(0.8, 50), new GroundSpec.Gate(0.5, 100, 380, armed)), pulse());
        int fired = 0;
        for (int step = 0; step < SimStep.ticks(3); step++) {
            sortie.step(Command.NONE);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertTrue(fired >= 2, "both anchors fired: " + fired);
        assertNotNull(sortie.links());
    }

    @Test
    void gatesDoNotAllocate() {
        Terrain canyon = new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {40, 40})),
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {440, 440})),
                List.of());
        LevelScript level = TestSpecs.withGround(
                level(
                        130,
                        Secondary.links(80, "BARRIERS"),
                        new GroundSpec.Gate(0.5, 80, 400, ANCHOR),
                        new GroundSpec.Gate(2, 90, 390, ANCHOR),
                        new GroundSpec.Gate(3.5, 80, 400, anchor(Optional.of(TestSpecs.NEEDLER_GUN))),
                        new GroundSpec.Gate(5, 90, 390, ANCHOR)),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(canyon, 10, 28, 0.12, 0.5))));
        long allocated = Allocations.least(
                () -> new Sortie(
                        1,
                        TestSpecs.loadout(PartASpecs.beamMount(1)).withWingman(RookCanyonTest.rook()),
                        level,
                        TestSpecs.RULES,
                        TestSpecs.FULL_ARMOUR),
                sortie -> {
                    for (int i = 0; i < 600; i++) {
                        int stick = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
                        sortie.step(stick | (i % 200 < 100 ? FIRE : 0));
                    }
                });
        assertEquals(0, allocated);
    }
}
