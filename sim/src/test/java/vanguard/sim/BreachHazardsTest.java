package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.BreachArenaTest.ARENA_END;
import static vanguard.sim.BreachArenaTest.ARRIVE;
import static vanguard.sim.BreachArenaTest.BREACH;
import static vanguard.sim.BreachArenaTest.CRAWL;
import static vanguard.sim.BreachArenaTest.FAN5;
import static vanguard.sim.BreachArenaTest.FRONT_X;
import static vanguard.sim.BreachArenaTest.FRONT_Y;
import static vanguard.sim.BreachArenaTest.MOUTH;
import static vanguard.sim.BreachArenaTest.MOUTH_HP;
import static vanguard.sim.BreachArenaTest.RING12;
import static vanguard.sim.BreachArenaTest.SIDE_Y;
import static vanguard.sim.BreachArenaTest.SPEED;
import static vanguard.sim.BreachArenaTest.SUCTION;
import static vanguard.sim.BreachArenaTest.boss;
import static vanguard.sim.BreachArenaTest.breach;
import static vanguard.sim.BreachArenaTest.steerTo;
import static vanguard.sim.BreachArenaTest.steps;
import static vanguard.sim.BreachArenaTest.until;
import static vanguard.sim.BreachArenaTest.untilPhase;
import static vanguard.sim.BreachArenaTest.values;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part H, step H3: the breaching boss's hazards (design/enemies/bosses/dust-colossus, Phases, Suction and debris;
 * the user's round 43 answers H.7b, c, h) on {@link BreachArenaTest}'s test colossus with three spine vents of 280 at
 * 120, 200 and 280 px toward the field's centre (an aimed 3-round burst every 3 s), phase 3's eruption (a 90 px marker
 * for 1.5 s, 10 damage, 80 px inside the edges, open 5 s, buried 1.5 s) with a two-arm spiral (4 a second per arm), the
 * debris (every 1.2 s in phase 2, 0.8 s in phase 3; 20–28 px, 2 HP, from 60 px/s gaining 60 each second, contact 6)
 * and the armour patch at phase 3's start.
 */
class BreachHazardsTest {
    static final int[] VENTS = {1, 2, 3};
    static final double VENT_HP = 280;
    static final double[] DISTANCES = {120, 200, 280};

    static final int RING = 0;
    static final int RING14 = 1;
    static final int FAN = 2;
    static final int BURST = 3;
    static final int SPIRAL = 4;

    static final EnemyGun VENT_GUN = EnemyGun.aimed(3, 0, 3, 120, 4, false);
    static final EnemyGun SPIRAL_GUN = EnemyGun.aimed(0.25, 0, 1, 120, 4, false);
    static final BossSpec.Eruption ERUPTION = new BossSpec.Eruption(1.5, 45, 10, 80, 5, 1.5);
    static final BossSpec.DebrisFlow DEBRIS = new BossSpec.DebrisFlow(List.of(0.0, 1.2, 0.8), 20, 28, 2, 60, 60, 6);

    private static BossSpec.Phase phase(String name, List<Integer> attacks, double below, boolean patch) {
        return new BossSpec.Phase(
                name,
                List.of(),
                0,
                attacks,
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        Double.isNaN(below) ? List.of() : List.of(MOUTH),
                        below,
                        BossSpec.Slamming.NONE,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        patch ? Optional.of(PickupType.ARMOUR_PATCH) : Optional.empty()));
    }

    /** The test colossus with its vents, eruption and debris; the phases' attacks as given. */
    static SetPieceSpec colossus(List<Integer> one, List<Integer> two, List<Integer> three) {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("mouth", 0, 0, new Hitbox(120, 120), MOUTH_HP, true, 264, Optional.empty(), 0, 0.5));
        for (int i = 0; i < VENTS.length; i++) {
            parts.add(new PartSpec("vent-" + i, 0, 0, new Hitbox(40, 40), VENT_HP, false, 12, Optional.empty(), 0, 1));
        }
        List<Integer> vents = List.of(VENTS[0], VENTS[1], VENTS[2]);
        var attacks = List.of(
                new BossSpec.Attack("ring", BossSpec.Pattern.RING, RING12, 0, false, 12, 0, 0, 0, List.of(MOUTH)),
                new BossSpec.Attack("ring-14", BossSpec.Pattern.RING, RING12, 0, false, 14, 0, 0, 0, List.of(MOUTH)),
                new BossSpec.Attack("fan", BossSpec.Pattern.FAN, FAN5, 0, false, 5, 0, 0, 0, List.of(MOUTH)),
                new BossSpec.Attack("vent-burst", BossSpec.Pattern.AIMED, VENT_GUN, 0.15, false, 1, 0, 0, 0, vents),
                new BossSpec.Attack(
                        "spiral", BossSpec.Pattern.SPIRAL, SPIRAL_GUN, 0, false, 1, 2, 1.5, 0, List.of(MOUTH)));
        var arena = new BossSpec.Arena(
                BossSpec.Lanes.NONE,
                null,
                List.of(Layer.BURIED, Layer.BURIED, Layer.BURIED, Layer.BURIED),
                List.of(new BossSpec.Spot("gullet", MOUTH, 0, 0, new Hitbox(60, 60), 2, true)));
        BossSpec boss = new BossSpec(
                        ARRIVE,
                        PlayField.WIDTH / 2.0,
                        PlayField.HEIGHT / 2.0,
                        0,
                        0,
                        1,
                        Layer.BURIED,
                        false,
                        "TEST COLOSSUS",
                        130,
                        List.of(),
                        attacks,
                        List.of(
                                phase("Circling", one, 0.6, false),
                                phase("Hunting", two, 0.25, false),
                                phase("Breaching anywhere", three, Double.NaN, true)),
                        false,
                        List.of(),
                        List.of(),
                        3,
                        Optional.of(arena))
                .withBreach(Optional.of(BREACH))
                .withSuction(Optional.of(SUCTION))
                .withVents(Optional.of(new BossSpec.Vents(vents, List.of(120.0, 200.0, 280.0), BURST)))
                .withEruption(Optional.of(ERUPTION))
                .withDebris(Optional.of(DEBRIS));
        return new SetPieceSpec(
                "test-colossus",
                new Hitbox(150, 150),
                new Hitbox(120, 120),
                0,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    static LevelScript level(SetPieceSpec colossus) {
        return LevelScript.builder(21, 3)
                .sections(List.of(
                        new Section(ARRIVE, SPEED),
                        new Section(ARENA_END, CRAWL, true, 0, 2, 0, true),
                        new Section(ARENA_END + 20, SPEED)))
                .setPieces(List.of(colossus))
                .build();
    }

    /** No attacks at all. */
    static Sortie quiet(Rules rules) {
        return new Sortie(
                1, TestSpecs.LOADOUT, level(colossus(List.of(), List.of(), List.of())), rules, TestSpecs.FULL_ARMOUR);
    }

    /** Only phase 2's vent burst (the vents rise in the phases that fire it). */
    static Sortie vents() {
        return withAttacks(List.of(), List.of(BURST), List.of(), TestSpecs.RULES.withInvulnerableShip());
    }

    static Sortie withAttacks(List<Integer> one, List<Integer> two, List<Integer> three, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level(colossus(one, two, three)), rules, TestSpecs.FULL_ARMOUR);
    }

    static Sortie withRook(List<Integer> one, List<Integer> two, List<Integer> three) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level(colossus(one, two, three)),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Into phase 2 on the first (front) breach's open; returns when the phase changed. */
    static void toPhaseTwo(Sortie sortie) {
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.5);
        assertEquals(List.of(1), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
    }

    /** Into phase 3 on the first (front) breach's open. */
    static void toPhaseThree(Sortie sortie) {
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.8);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
    }

    private static double health(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        return defences.shield() + defences.armour();
    }

    @Test
    void phaseTwosVentsRiseWithTheMouthOnItsSpineAndSinkWithIt() {
        Sortie sortie = vents();
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        assertEquals(3, breach.ventCount());
        // The front breach of phase 1 raises no vents.
        until(sortie, SimEvents.Type.BREACH_RISE, 12, 0);
        for (int i = 0; i < 3; i++) {
            assertFalse(breach.ventRaised(i), "no vents in phase 1");
        }
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        for (int i = 0; i < 3; i++) {
            assertEquals(Layer.BURIED, boss.partLayer(VENTS[i]));
        }
        boss.damagePartFlat(MOUTH, MOUTH_HP * 0.5);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        // Phase 2's first breach (the rear point).
        until(sortie, SimEvents.Type.BREACH_RISE, 20, 0);
        assertEquals(breach.mouthX(), breach.ridgeX(BreachArena.RIDGE_POINTS - 1), 1e-9, "the curve ends at the mouth");
        assertEquals(breach.mouthY(), breach.ridgeY(BreachArena.RIDGE_POINTS - 1), 1e-9);
        for (int i = 0; i < 3; i++) {
            assertTrue(breach.ventRaised(i));
            assertFalse(breach.ventUp(i), "buried until the open");
            // Step SIM-K (round 44, K.b): on the body, its distance along the ridge's curve back from the mouth.
            double[] spine = alongSpine(breach, DISTANCES[i]);
            assertEquals(spine[0], breach.ventX(i), 0.5, "on the spine");
            assertEquals(spine[1], breach.ventY(i), 0.5);
            assertEquals(1, Math.hypot(breach.ventDirX(i), breach.ventDirY(i)), 1e-9, "a unit heading");
            double toMouthX = breach.mouthX() - breach.ventX(i);
            double toMouthY = breach.mouthY() - breach.ventY(i);
            assertTrue(breach.ventDirX(i) * toMouthX + breach.ventDirY(i) * toMouthY > 0, "heading for the mouth");
            assertEquals(breach.ventX(i), boss.partX(VENTS[i]), 1e-9, "the part stands at the vent");
            assertEquals(breach.ventY(i), boss.partY(VENTS[i]), 1e-9);
            assertEquals(VENTS[i], breach.ventPart(i));
        }
        int middle = 0;
        while (boss.partLayer(MOUTH) == Layer.BURIED) {
            sortie.step(0);
            middle++;
        }
        assertEquals(SimStep.ticks(0.75), middle, 1);
        // Step SV2: the mouth is up from the rise's middle, the vents only from the open: the presentation's reveal
        // runs up the spine over the whole rise.
        int rise = middle;
        while (breach.phase() == BreachArena.Phase.RISE) {
            for (int i = 0; i < 3; i++) {
                assertFalse(breach.ventUp(i), "not hittable while the spine is still rising");
                assertEquals(Layer.BURIED, boss.partLayer(VENTS[i]));
                assertTrue(boss.partShielded(VENTS[i]));
            }
            sortie.step(0);
            rise++;
        }
        assertEquals(BreachArena.Phase.OPEN, breach.phase());
        assertEquals(SimStep.ticks(1.5), rise, 1);
        for (int i = 0; i < 3; i++) {
            assertTrue(breach.ventUp(i), "up once the rise is over");
            assertEquals(Layer.GROUND, boss.partLayer(VENTS[i]));
            assertFalse(boss.partShielded(VENTS[i]), "up and hittable");
        }
        untilPhase(sortie, BreachArena.Phase.DIVE, 10);
        // Step SV2: symmetric: the vents sink as the dive starts, the mouth at its middle.
        assertEquals(Layer.GROUND, boss.partLayer(MOUTH));
        for (int i = 0; i < 3; i++) {
            assertTrue(breach.ventRaised(i), "still drawn, sinking with the mouth");
            assertFalse(breach.ventUp(i), "not hittable once the dive starts");
            assertEquals(Layer.BURIED, boss.partLayer(VENTS[i]));
            assertTrue(boss.partShielded(VENTS[i]));
        }
        while (boss.partLayer(MOUTH) == Layer.GROUND) {
            sortie.step(0);
        }
        for (int i = 0; i < 3; i++) {
            assertFalse(breach.ventUp(i), "sunk with the mouth");
            assertEquals(Layer.BURIED, boss.partLayer(VENTS[i]));
            assertTrue(boss.partShielded(VENTS[i]));
        }
    }

    /**
     * Step SVF: the point {@code d} px back from the mouth along the ridge's curve as the presentation lays the spine
     * (game's {@code ColossusSpine.onTrack}): along the {@link BreachArena#RIDGE_POINTS} samples from the mouth back,
     * straight on along the first stretch past the curve's start.
     */
    static double[] alongSpine(BreachArena breach, double d) {
        double left = d;
        for (int k = BreachArena.RIDGE_POINTS - 1; k > 0; k--) {
            double x = breach.ridgeX(k);
            double y = breach.ridgeY(k);
            double dx = breach.ridgeX(k - 1) - x;
            double dy = breach.ridgeY(k - 1) - y;
            double length = Math.hypot(dx, dy);
            if (length >= left || k == 1) {
                return new double[] {x + dx * left / length, y + dy * left / length};
            }
            left -= length;
        }
        throw new AssertionError("no curve");
    }

    /**
     * Step SVF: with the ship in a bottom corner, phase 2's rear, left and right points bend the ridge into a hairpin
     * through the field's centre (and rear to rear at the phase change): each vent still sits on the curve the spine
     * is drawn along, its segment's place, within 2 px (the old straight run left vent 3 up to 137 px off it).
     */
    @Test
    void theVentsFollowTheRidgesCurveRoundAHairpin() {
        for (int corner : new int[] {Command.LEFT.bit(), Command.RIGHT.bit()}) {
            for (boolean late : new boolean[] {false, true}) {
                Sortie sortie = vents();
                BreachArena breach = breach(sortie);
                if (late) {
                    // The phase change during phase 1's rear breach: phase 2 starts rear again.
                    for (int n = 0; n < 4; n++) {
                        until(sortie, SimEvents.Type.BREACH_RISE, 20, 0);
                    }
                }
                toPhaseTwo(sortie);
                double offStraight = 0;
                int rises = 0;
                while (rises < 5) {
                    sortie.step(corner | Command.DOWN.bit());
                    if (values(sortie, SimEvents.Type.BREACH_RISE).isEmpty()) {
                        continue;
                    }
                    rises++;
                    int last = BreachArena.RIDGE_POINTS - 1;
                    double backX = breach.ridgeX(last - 1) - breach.mouthX();
                    double backY = breach.ridgeY(last - 1) - breach.mouthY();
                    double back = Math.hypot(backX, backY);
                    for (int i = 0; i < 3; i++) {
                        double[] spot = alongSpine(breach, DISTANCES[i]);
                        String where = breach.pose() + " vent " + i;
                        assertTrue(spot[0] >= 20 && spot[0] <= PlayField.WIDTH - 20, where + " on the field");
                        assertTrue(spot[1] >= 20 && spot[1] <= PlayField.HEIGHT - 20, where + " on the field");
                        assertEquals(0, Math.hypot(spot[0] - breach.ventX(i), spot[1] - breach.ventY(i)), 2, where);
                        offStraight = Math.max(
                                offStraight,
                                Math.hypot(
                                        breach.mouthX() + backX / back * DISTANCES[i] - spot[0],
                                        breach.mouthY() + backY / back * DISTANCES[i] - spot[1]));
                    }
                }
                assertTrue(offStraight > 40, "a hairpin among them: " + offStraight);
            }
        }
    }

    @Test
    void phaseThreesVentsRiseOnTheSpineOfAnUnderBreachWithinTheField() {
        Sortie sortie = withAttacks(List.of(), List.of(BURST), List.of(BURST), TestSpecs.RULES.withInvulnerableShip());
        toPhaseThree(sortie);
        BreachArena breach = breach(sortie);
        for (int n = 0; n < 2; n++) {
            until(sortie, SimEvents.Type.ERUPTION, 20, 0);
            assertEquals(BreachArena.Pose.UNDER, breach.pose());
            for (int i = 0; i < 3; i++) {
                assertTrue(breach.ventUp(i), "up with the erupted mouth");
                double toMouth = Math.hypot(breach.mouthX() - breach.ventX(i), breach.mouthY() - breach.ventY(i));
                assertTrue(toMouth <= DISTANCES[i] + 1e-9, "no farther than its way along the body");
                // Step SVF: on the ridge's curve as the spine is laid (an under point's curve may turn back on itself),
                // held 20 px inside the field.
                double[] spot = alongSpine(breach, DISTANCES[i]);
                double x = Math.clamp(spot[0], 20, PlayField.WIDTH - 20);
                double y = Math.clamp(spot[1], 20, PlayField.HEIGHT - 20);
                assertEquals(0, Math.hypot(x - breach.ventX(i), y - breach.ventY(i)), 2, "on the curve");
                assertTrue(breach.ventX(i) >= 20 && breach.ventX(i) <= PlayField.WIDTH - 20, "on the field");
                assertTrue(breach.ventY(i) >= 20 && breach.ventY(i) <= PlayField.HEIGHT - 20, "on the field");
            }
        }
    }

    @Test
    void anEruptionsRingExpandsAndCatchesTheShipParkedOnTheMouthOnce() {
        BossSpec.Eruption ringed = ERUPTION.withRing(Optional.of(new BossSpec.Shockwave(100, 0.3, 7)));
        SetPieceSpec piece = colossus(List.of(), List.of(), List.of());
        // No rocks, so only the ring hurts the ship here.
        BossSpec boss =
                piece.boss().orElseThrow().withEruption(Optional.of(ringed)).withDebris(Optional.empty());
        SetPieceSpec spec = new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                Optional.of(boss));
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level(spec), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        toPhaseThree(sortie);
        BreachArena breach = breach(sortie);
        assertEquals(100, breach.shockReach(), 0);
        steerTo(sortie, 240, 270);
        until(sortie, SimEvents.Type.ERUPTION_MARKER, 20, 0);
        // The ship moves 60 px off the marker: out of the eruption's 45 px, inside the ring's 100.
        double off = breach.markerX() < PlayField.WIDTH / 2.0 ? 60 : -60;
        steerTo(sortie, breach.markerX() + off, breach.markerY());
        assertTrue(breach.marker(), "still before the eruption");
        assertEquals(List.of(0), until(sortie, SimEvents.Type.ERUPTION, 3, 0), "out of the eruption's circle");
        assertTrue(breach.ringing());
        List<Integer> caught = new ArrayList<>();
        double lost = 0;
        int ticks = 0;
        while (breach.ringing()) {
            caught.addAll(values(sortie, SimEvents.Type.ERUPTION_RING));
            double before = health(sortie);
            sortie.step(0);
            if (!values(sortie, SimEvents.Type.ERUPTION_RING).isEmpty()) {
                lost = before - health(sortie);
            }
            ticks++;
        }
        caught.addAll(values(sortie, SimEvents.Type.ERUPTION_RING));
        assertEquals(SimStep.ticks(0.3) + 1, ticks, 1, "it expands over its 0.3 s");
        assertEquals(List.of(BreachArena.BITE_SHIP), caught, "the ship once");
        assertEquals(7, lost, 0.1, "its damage");
        assertEquals(0, breach.shockRadius(), 0);
    }

    @Test
    void theRisenVentsFireTheirBurstOneSecondAfterTheOpenThenEveryThreeSeconds() {
        Sortie sortie = withAttacks(List.of(), List.of(BURST), List.of(), TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        toPhaseTwo(sortie);
        until(sortie, SimEvents.Type.BREACH_RISE, 20, 0);
        List<Integer> volleys = new ArrayList<>();
        int tick = 0;
        int opened = -1;
        while (breach.phase() != BreachArena.Phase.DIVE) {
            int before = sortie.bulletCount();
            sortie.step(0);
            tick++;
            if (opened < 0 && breach.phase() == BreachArena.Phase.OPEN) {
                opened = tick;
            }
            int fired = sortie.bulletCount() - before;
            if (fired > 0) {
                assertEquals(3, fired, "one bullet from each of the three vents");
                assertTrue(opened >= 0, "nothing before the open");
                volleys.add(tick - opened);
            }
        }
        int first = SimStep.ticks(BreachArena.VENT_FIRST_SECONDS);
        int gap = SimStep.ticks(0.15);
        int interval = SimStep.ticks(3);
        assertEquals(
                List.of(
                        first,
                        first + gap,
                        first + 2 * gap,
                        first + interval,
                        first + interval + gap,
                        first + interval + 2 * gap),
                volleys,
                "two 3-round bursts in the 7 s open (at 1 s and 4 s; 7 s is the bite)");
    }

    @Test
    void aDestroyedVentStaysDeadAndTheVentsAreOutOfTheBar() {
        Sortie sortie = vents();
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        toPhaseTwo(sortie);
        untilPhase(sortie, BreachArena.Phase.RISE, 20);
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        double bar = boss.barShare();
        boss.damagePartFlat(VENTS[0], VENT_HP);
        assertTrue(boss.partWrecked(VENTS[0]));
        assertEquals(bar, boss.barShare(), 1e-12, "the vents are out of the bar");
        sortie.step(0);
        assertFalse(breach.ventUp(0));
        until(sortie, SimEvents.Type.BREACH_DIVE, 10, 0);
        until(sortie, SimEvents.Type.BREACH_RISE, 10, 0);
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        assertFalse(breach.ventRaised(0), "dead stays dead");
        assertFalse(breach.ventUp(0));
        assertEquals(Layer.BURIED, boss.partLayer(VENTS[0]));
        assertTrue(breach.ventUp(1) && breach.ventUp(2), "the living ones rise again");
    }

    @Test
    void phaseThreesMarkerLiesWhereTheShipWasErupts15sLaterAndHitsOnce() {
        Sortie sortie = quiet(TestSpecs.RULES);
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        assertTrue(breach.erupting());
        assertEquals(45, breach.markerRadius());
        toPhaseThree(sortie);
        // The front breach under way runs out (its bite out of reach), then the marker under the ship.
        steerTo(sortie, 300, 200);
        List<Integer> marker = until(sortie, SimEvents.Type.ERUPTION_MARKER, 20, 0);
        assertEquals(List.of(BreachArena.Pose.UNDER.ordinal()), marker);
        assertTrue(values(sortie, SimEvents.Type.BREACH_TELL).isEmpty(), "the marker replaces the tell");
        assertTrue(breach.marker());
        assertEquals(BreachArena.Pose.UNDER, breach.pose());
        assertEquals(sortie.ship().x(), breach.markerX(), 3);
        assertEquals(sortie.ship().y(), breach.markerY(), 3);
        double mx = breach.markerX();
        double my = breach.markerY();
        assertEquals(Layer.BURIED, boss.partLayer(MOUTH));
        // It does not follow the ship.
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.RIGHT.bit());
            assertEquals(mx, breach.markerX(), 1e-12);
            assertEquals(my, breach.markerY(), 1e-12);
        }
        assertTrue(breach.markerProgress() > 0.3 && breach.markerProgress() < 0.4, "a third of the way");
        steerTo(sortie, mx, my);
        double before = health(sortie);
        int toEruption = 0;
        while (values(sortie, SimEvents.Type.ERUPTION).isEmpty()) {
            sortie.step(0);
            toEruption++;
            assertTrue(toEruption < SimStep.ticks(2), "it erupts");
        }
        assertEquals(List.of(BreachArena.BITE_SHIP), values(sortie, SimEvents.Type.ERUPTION), "the ship inside");
        assertTrue(values(sortie, SimEvents.Type.BREACH_RISE).isEmpty(), "the eruption replaces the rise");
        assertEquals(before - 10, health(sortie), 0.2, "heavy = 10, once");
        assertFalse(breach.marker());
        assertEquals(BreachArena.Phase.OPEN, breach.phase(), "up and open at once");
        assertEquals(Layer.GROUND, boss.partLayer(MOUTH));
        assertEquals(mx, breach.mouthX(), 1e-12);
        assertEquals(my, breach.mouthY(), 1e-12);
    }

    @Test
    void anEruptionCycleIsMarkerOpenFiveBiteDiveBuriedThenTheNextMarker() {
        Sortie sortie = quiet(TestSpecs.RULES.withInvulnerableShip());
        toPhaseThree(sortie);
        steerTo(sortie, 20, 520);
        until(sortie, SimEvents.Type.ERUPTION_MARKER, 20, 0);
        BreachArena breach = breach(sortie);
        assertEquals(ERUPTION.inset(), breach.markerX(), 1e-9, "80 px inside the left edge");
        assertEquals(PlayField.HEIGHT - ERUPTION.inset(), breach.markerY(), 1e-9, "and the top");
        SimEvents.Type[] order = {
            SimEvents.Type.ERUPTION, SimEvents.Type.BITE_TELL, SimEvents.Type.BITE_SNAP, SimEvents.Type.ERUPTION_MARKER
        };
        double[] seconds = {1.5, 5, 0.8, 1.5 + 1.5};
        for (int k = 0; k < order.length; k++) {
            int ticks = 0;
            while (values(sortie, order[k]).isEmpty()) {
                sortie.step(0);
                ticks++;
                assertTrue(ticks < SimStep.ticks(8), order[k] + " in time");
            }
            assertEquals(SimStep.ticks(seconds[k]), ticks, 1, order[k] + " after " + seconds[k] + " s");
        }
    }

    @Test
    void theSpiralFiresTwoArmsFourTimesASecondFromTheGlowsEndWhileOpen() {
        Sortie sortie = withAttacks(List.of(), List.of(), List.of(SPIRAL), TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        toPhaseThree(sortie);
        steerTo(sortie, 240, 150);
        until(sortie, SimEvents.Type.ERUPTION_MARKER, 20, 0);
        // Off the marker: nothing fires at a ship on the muzzle.
        steerTo(sortie, 240, 350);
        until(sortie, SimEvents.Type.ERUPTION, 2, 0);
        int glow = SimStep.ticks(BossSpec.Breach.GLOW_SECONDS);
        List<Integer> volleys = new ArrayList<>();
        int glowing = breach.glowing() ? 1 : 0;
        for (int tick = 1; breach.phase() == BreachArena.Phase.OPEN; tick++) {
            int before = sortie.bulletCount();
            // Away from the mouth, against the pull (nothing fires at a ship at the muzzle).
            sortie.step(Command.UP.bit());
            glowing += breach.glowing() ? 1 : 0;
            int fired = sortie.bulletCount() - before;
            if (fired > 0) {
                assertEquals(2, fired, "two arms");
                volleys.add(tick);
            }
        }
        assertEquals(glow, glowing, "the gullet glows before the spiral");
        assertEquals(glow, volleys.getFirst(), "its first bullets at the glow's end");
        int interval = SimStep.ticks(0.25);
        for (int k = 1; k < volleys.size(); k++) {
            assertEquals(interval, volleys.get(k) - volleys.get(k - 1));
        }
        assertEquals(18, volleys.size(), "4.5 s of four a second");
    }

    @Test
    void rocksAreTornLooseFromTheFarHalfWhileTheMouthDrawsAndSwallowed() {
        Sortie sortie = quiet(TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        // None in phase 1.
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        steps(sortie, 3, 0);
        assertEquals(0, breach.rocksTorn());
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.5);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        until(sortie, SimEvents.Type.BREACH_RISE, 20, 0);
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        int torn = breach.rocksTorn();
        double mx = breach.mouthX();
        double my = breach.mouthY();
        int tracked = -1;
        double distance = Double.NaN;
        double speed = 0;
        boolean swallowed = false;
        while (breach.phase() != BreachArena.Phase.DIVE) {
            int count = sortie.debrisCount();
            sortie.step(0);
            for (int i = 0; i < sortie.debrisCount(); i++) {
                Debris rock = sortie.debris(i);
                assertTrue(rock.pulled());
                assertTrue(
                        rock.spec().size().width() >= 20 && rock.spec().size().width() <= 28);
                if (rock.serial() == tracked) {
                    double d = Math.hypot(mx - rock.x(), my - rock.y());
                    assertTrue(d < distance, "toward the mouth");
                    assertTrue(rock.pullSpeed() > speed, "faster and faster");
                    distance = d;
                    speed = rock.pullSpeed();
                }
            }
            if (sortie.debrisCount() > count && tracked < 0) {
                Debris rock = sortie.debris(sortie.debrisCount() - 1);
                tracked = rock.serial();
                distance = Math.hypot(mx - rock.x(), my - rock.y());
                speed = rock.pullSpeed();
                // Torn loose on an edge, moved one step since.
                double step = 2;
                boolean edge = rock.x() < step
                        || rock.x() > PlayField.WIDTH - step
                        || rock.y() < step
                        || rock.y() > PlayField.HEIGHT - step;
                assertTrue(edge, "on an edge: " + rock.x() + ", " + rock.y());
                double far = (rock.x() - PlayField.WIDTH / 2.0) * (mx - PlayField.WIDTH / 2.0)
                        + (rock.y() - PlayField.HEIGHT / 2.0) * (my - PlayField.HEIGHT / 2.0);
                assertTrue(far <= 0, "in the far half from the mouth");
                assertEquals(60, speed, 2, "from 60 px/s");
            } else if (tracked >= 0 && !swallowed) {
                boolean present = false;
                for (int i = 0; i < sortie.debrisCount(); i++) {
                    present |= sortie.debris(i).serial() == tracked;
                }
                if (!present) {
                    swallowed = true;
                    assertTrue(distance < PulledDebris.SWALLOW + speed * SimStep.SECONDS * 2, "at the mouth");
                }
            }
        }
        assertEquals(6, breach.rocksTorn() - torn, "every 1.2 s through 7 s open and the 0.8 s bite");
        assertTrue(swallowed, "swallowed");
    }

    @Test
    void aRockStopsTheShipsShotsNotTheEnemysBulletsAndBreaksOnTheHull() {
        Sortie sortie = quiet(TestSpecs.RULES);
        steps(sortie, 1, 0);
        PulledDebris rocks = new PulledDebris(DEBRIS);
        double sx = sortie.ship().x();
        double sy = sortie.ship().y();
        // A rock that hangs still above the ship (far target, no speed to speak of).
        Debris rock = rocks.tear(sortie.debris, new SplitMix64(7), FRONT_X, FRONT_Y);
        assertNotNull(rock);
        rock.pull(rock.spec(), 1, sx, sy + 150, sx, sy + 5000, 1e-6, 0);
        // An enemy bullet flying down through it.
        EnemyBullet bullet = sortie.force.bullets().obtain();
        bullet.fire(sx, sy + 200, 0, -120, 4);
        boolean hit = false;
        boolean broke = false;
        for (int i = 0; i < SimStep.ticks(1) && !broke; i++) {
            sortie.step(i > SimStep.ticks(0.6) ? Command.FIRE.bit() : 0);
            hit |= !values(sortie, SimEvents.Type.DEBRIS_HIT).isEmpty();
            broke |= !values(sortie, SimEvents.Type.DEBRIS_DESTROYED).isEmpty();
            if (i == SimStep.ticks(0.6)) {
                assertTrue(bullet.y() < sy + 150 - 20, "the bullet passed through: " + bullet.y());
            }
        }
        assertTrue(hit && broke, "the shots hit it and it broke (2 HP)");
        // A rock on the hull: contact 6 and it breaks.
        Debris onHull = rocks.tear(sortie.debris, new SplitMix64(7), FRONT_X, FRONT_Y);
        onHull.pull(onHull.spec(), 2, sortie.ship().x(), sortie.ship().y(), sx, sy + 5000, 1e-6, 0);
        double before = health(sortie);
        sortie.step(0);
        assertEquals(before - 6, health(sortie), 0.2, "tiny = 6");
        assertEquals(0, sortie.debrisCount(), "broken on the hull");
    }

    @Test
    void phaseThreeDropsTheArmourPatchInTheOpenField() {
        Sortie sortie = quiet(TestSpecs.RULES.withInvulnerableShip());
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.5);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        assertEquals(0, sortie.pickupCount(), "no drop in phase 2");
        untilPhase(sortie, BreachArena.Phase.OPEN, 20);
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.3);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        assertEquals(1, sortie.pickupCount());
        Pickup patch = sortie.pickup(0);
        assertEquals(PickupType.ARMOUR_PATCH, patch.type());
        assertEquals(PlayField.WIDTH / 2.0, patch.x(), 1);
        assertEquals(PlayField.HEIGHT / 2.0, patch.y(), 3);
        // The suction pulls no pickups (H.7b): it drifts as any pickup.
        double y = patch.y();
        steps(sortie, 1, 0);
        assertEquals(PlayField.WIDTH / 2.0, sortie.pickup(0).x(), 1e-9, "not drawn to the mouth");
        assertTrue(sortie.pickup(0).y() <= y);
    }

    @Test
    void rookKeepsOutOfTheBiteCircleAndTheMarker() {
        Sortie sortie = withRook(List.of(), List.of(), List.of());
        Wingman rook = sortie.wingman().orElseThrow();
        BreachArena breach = breach(sortie);
        until(sortie, SimEvents.Type.BREACH_TELL, 10, 0);
        // The ship under the front mouth: Rook's slot beside it lies in the bite circle.
        steerTo(sortie, FRONT_X + 30, FRONT_Y - 60);
        untilPhase(sortie, BreachArena.Phase.BITE, 12);
        double clear = breach.biteRadius() + RookHazards.GAP;
        assertTrue(
                Math.hypot(rook.x() - breach.mouthX(), rook.y() - breach.mouthY()) > clear,
                "out of the circle before the snap");
        List<Integer> snap = until(sortie, SimEvents.Type.BITE_SNAP, 1, 0);
        assertEquals(0, snap.getFirst() & BreachArena.BITE_WINGMAN, "the bite missed him");
        // Phase 3: the marker under the ship.
        untilPhase(sortie, BreachArena.Phase.OPEN, 20);
        boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.8);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        steerTo(sortie, 240, 200);
        until(sortie, SimEvents.Type.ERUPTION_MARKER, 20, 0);
        List<Integer> eruption = until(sortie, SimEvents.Type.ERUPTION, 2, 0);
        assertEquals(0, eruption.getFirst() & BreachArena.BITE_WINGMAN, "the eruption missed him");
        assertTrue(
                Math.hypot(rook.x() - breach.mouthX(), rook.y() - breach.mouthY())
                        > Math.max(breach.markerRadius(), breach.biteRadius()),
                "out of the marker");
    }

    @Test
    void rookHoldsHisGoalAgainstThePull() {
        Sortie sortie = withRook(List.of(), List.of(), List.of());
        Wingman rook = sortie.wingman().orElseThrow();
        BreachArena breach = breach(sortie);
        until(sortie, SimEvents.Type.BREACH_DIVE, 25, 0);
        // The left mouth at (30, 280 from the top): the ship out of the suction's reach, Rook (on its left) in it.
        steerTo(sortie, 400, SIDE_Y);
        untilPhase(sortie, BreachArena.Phase.RISE, 5);
        double x = rook.x();
        double y = rook.y();
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        steps(sortie, 2, 0);
        assertEquals(0, breach.pullX(), 1e-12, "the ship is not pulled");
        assertTrue(Math.hypot(breach.pullXAt(rook.x(), rook.y()), breach.pullYAt(rook.x(), rook.y())) > 5, "he is");
        assertEquals(x, rook.x(), 0.3, "he holds his slot");
        assertEquals(y, rook.y(), 0.3);
    }

    @Test
    void aRetryFromTheBossRaisesTheDeadVentsAgainAndClearsTheRocks() {
        Sortie sortie = vents();
        toPhaseTwo(sortie);
        untilPhase(sortie, BreachArena.Phase.RISE, 20);
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        boss(sortie).damagePartFlat(VENTS[1], VENT_HP);
        steps(sortie, 3, 0);
        assertTrue(sortie.debrisCount() > 0);
        assertTrue(sortie.bossCheckpoint());
        sortie.retryFromBoss();
        sortie.step(0);
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        assertEquals(0, sortie.debrisCount());
        assertEquals(0, breach.rocksTorn());
        for (int i = 0; i < 3; i++) {
            assertFalse(boss.partWrecked(VENTS[i]));
            assertFalse(breach.ventRaised(i));
            assertEquals(Layer.BURIED, boss.partLayer(VENTS[i]));
        }
        assertEquals(1, boss.barShare(), 1e-12);
    }

    @Test
    void theHazardsRunTheSameTwiceAndAllocateNothingPerStep() {
        List<Integer> one = List.of(RING);
        List<Integer> two = List.of(RING14, FAN, BURST);
        List<Integer> three = List.of(SPIRAL);
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = withRook(one, two, three);
            for (int i = 0; i < SimStep.ticks(70); i++) {
                sortie.step(Command.FIRE.bit() | ((i / 90) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                if (i == SimStep.ticks(15) || i == SimStep.ticks(40)) {
                    boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.36);
                }
            }
            assertTrue(breach(sortie).rocksTorn() > 0);
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = withRook(one, two, three);
                    steps(sortie, 12, 0);
                    boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.45);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(
                                Command.FIRE.bit() | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                        if (i == SimStep.ticks(20)) {
                            boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.3);
                        }
                    }
                });
        assertEquals(0, allocated, "the vents, the eruption, the spiral and the rocks allocate nothing per step");
    }

    @Test
    void theHazardsAreValidated() {
        BossSpec boss = colossus(List.of(), List.of(), List.of()).boss().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> boss.withVents(Optional.of(new BossSpec.Vents(List.of(1), List.of(120.0), SPIRAL))),
                "a spiral is no burst");
        assertThrows(
                IllegalArgumentException.class,
                () -> boss.withVents(Optional.of(new BossSpec.Vents(List.of(1), List.of(120.0), 9))),
                "the burst is an attack");
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Vents(List.of(1, 2), List.of(120.0), BURST));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Eruption(0, 45, 10, 80, 5, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Shockwave(0, 0.3, 7));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Shockwave(100, 0, 7));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.DebrisFlow(List.of(), 20, 28, 2, 60, 60, 6));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.DebrisFlow(List.of(1.2), 28, 20, 2, 60, 60, 6));
        BossSpec plain = BreachArenaTest.colossus(false).boss().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withSuction(Optional.empty())
                        .withEruption(Optional.of(ERUPTION))
                        .withBreach(Optional.empty()),
                "only a breaching boss erupts");
        assertEquals(0.8, DEBRIS.every(2), 1e-12);
        assertEquals(0.8, DEBRIS.every(5), 1e-12, "the last for later phases");
    }
}
