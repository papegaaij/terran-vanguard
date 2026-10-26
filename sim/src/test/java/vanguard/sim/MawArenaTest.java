package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part H, step H1: the coiled arena boss ({@link CoilArena}; design/enemies/bosses/abyssal-maw) on the test Maw of
 * {@link MawSpecs}: its intro and the coil's geometry, the wall, the armoured coil, the sacs' breaths and the throat's
 * fan, the phases' ends, the fan walls' gaps, the sweeps, the dive and the lunge, the implants on the head's bar, the
 * shed Eel Swarms, easy and hard, the radio's {@code boss-arrival} and {@code boss-dive}, determinism and allocation.
 */
class MawArenaTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final MawSpecs.Fit QUIET = MawSpecs.Fit.QUIET;

    private static Sortie invulnerable(MawSpecs.Fit fit) {
        return MawSpecs.sortie(fit, TestSpecs.RULES.withInvulnerableShip());
    }

    private static double health(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        return defences.shield() + defences.armour();
    }

    private static int bulletsNear(Sortie sortie, double x, double y, double within) {
        int count = 0;
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (Math.hypot(bullet.x() - x, bullet.y() - y) < within) {
                count++;
            }
        }
        return count;
    }

    /** Steps until the dive's state is {@code state}, at most {@code seconds}; returns the steps taken. */
    private static int untilDive(Sortie sortie, CoilArena.DiveState state, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            if (MawSpecs.coil(sortie).diveState() == state) {
                return i;
            }
            sortie.step(commands);
        }
        throw new AssertionError(state + " within " + seconds + " s");
    }

    @Test
    void itRisesOverItsIntroThenEngagesWithItsCoilRoundTheEdges() {
        Sortie sortie = invulnerable(QUIET);
        SetPiece boss = MawSpecs.boss(sortie);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, MawSpecs.ARRIVE + 1, 0);
        assertTrue(boss.present());
        assertFalse(coil.up());
        assertEquals(Layer.SUB, boss.partLayer(MawSpecs.HEAD), "under the water in its intro");
        assertTrue(boss.partShielded(MawSpecs.HEAD), "invulnerable until up");
        assertFalse(boss.onPlane());
        int steps = 0;
        while (!boss.engaged()) {
            sortie.step(0);
            steps++;
        }
        assertTrue(Math.abs(steps - SimStep.ticks(6)) <= 1, "a 6 s intro: " + steps);
        assertTrue(coil.up());
        assertTrue(boss.onPlane());
        for (int p = 0; p < boss.partCount(); p++) {
            assertEquals(Layer.AIR, boss.partLayer(p), "up on the play plane: " + p);
        }
        assertFalse(boss.partShielded(MawSpecs.HEAD));
        assertEquals(MawSpecs.REST_X, boss.x(), 1e-9);
        assertEquals(MawSpecs.REST_Y, boss.y(), 1e-9);
        // The coil: 3 along the top, 10 down the left, 8 along the bottom, 10 up the right.
        assertEquals(31, coil.segmentCount());
        int[] runs = new int[4];
        for (int i = 0; i < coil.segmentCount(); i++) {
            int side = coil.segmentSide(i);
            runs[side + 1]++;
            switch (side) {
                case CoilArena.TOP -> assertEquals(522, coil.segmentY(i), 1e-9);
                case 0 -> assertEquals(18, coil.segmentX(i), 1e-9);
                case 1 -> assertEquals(3, coil.segmentY(i), 1e-9);
                default -> assertEquals(462, coil.segmentX(i), 1e-9);
            }
        }
        assertEquals(3, runs[0], "top");
        assertEquals(10, runs[1 + CoilSpec.Side.LEFT.ordinal()], "left");
        assertEquals(8, runs[1 + CoilSpec.Side.BOTTOM.ordinal()], "bottom");
        assertEquals(10, runs[1 + CoilSpec.Side.RIGHT.ordinal()], "right");
        assertEquals(185, coil.segmentX(0), 1e-9);
        // The implants ride their segments where the design places them; the bar is the head's alone.
        assertEquals(110, boss.partX(MawSpecs.NECK), 1e-9);
        assertEquals(480, boss.partY(MawSpecs.NECK), 1e-9);
        assertEquals(40, boss.partX(MawSpecs.LEFT_IMPLANT), 1e-9);
        assertEquals(240, boss.partY(MawSpecs.LEFT_IMPLANT), 1e-9);
        assertEquals(440, boss.partX(MawSpecs.RIGHT_IMPLANT), 1e-9);
        assertTrue(boss.partShielded(MawSpecs.NECK), "a shut implant is armoured");
        boss.damagePartFlat(MawSpecs.SACS.getFirst(), 100);
        assertEquals(1, boss.barShare(), 1e-12, "the sacs are out of the bar");
        MawSpecs.headTo(sortie, 0.8);
        assertEquals(0.8, boss.barShare(), 1e-12);
    }

    @Test
    void theCoilIsAWallForTheShipOnceItIsUp() {
        Sortie sortie = invulnerable(QUIET);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, MawSpecs.ARRIVE + 1, 0);
        int downLeft = Command.LEFT.bit() | Command.DOWN.bit();
        MawSpecs.steps(sortie, 2, downLeft);
        Ship ship = sortie.ship();
        assertEquals(sortie.ship().spec().edgeLimit(), ship.x(), 1e-9, "no wall before it is up");
        MawSpecs.untilUp(sortie);
        MawSpecs.steps(sortie, 1, downLeft);
        assertEquals(62, ship.x(), 1e-9, "12 px inside the left flank (x 50)");
        assertEquals(47, ship.y(), 1e-9, "12 px inside the bottom flank (y 35)");
        MawSpecs.steps(sortie, 3, Command.RIGHT.bit() | Command.UP.bit());
        assertEquals(418, ship.x(), 1e-9, "12 px inside the right flank (x 430)");
        assertEquals(PlayField.HEIGHT - ship.spec().edgeLimit(), ship.y(), 1e-9, "no wall at the top");
    }

    @Test
    void shotsGlanceOffTheArmouredCoilAndTheBeamStopsOnIt() {
        Sortie sortie = invulnerable(QUIET);
        MawSpecs.untilUp(sortie);
        SetPiece boss = MawSpecs.boss(sortie);
        MawSpecs.steerTo(sortie, 150, 120);
        int glanced = 0;
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(FIRE);
            glanced += MawSpecs.values(sortie, SimEvents.Type.SHOT_GLANCED).size();
            hits += MawSpecs.values(sortie, SimEvents.Type.ENEMY_HIT).size();
        }
        assertTrue(glanced > 3, "the top of the coil turns the shots: " + glanced);
        assertEquals(0, hits);
        assertEquals(MawSpecs.HEAD_HP, boss.partHp(MawSpecs.HEAD), 1e-9);
        assertEquals(522 - 32, boss.plateColumn(150, 5, 100), 1e-9, "the beam stops on the coil's lower edge");
        assertEquals(Double.POSITIVE_INFINITY, boss.plateColumn(240, 5, 100), "under the head the coil is no wall");
    }

    @Test
    void phaseOneBreathesTheSacsInTurnAndOpensTheJawForTheThroatsFan() {
        Sortie sortie = invulnerable(QUIET.withAttacks());
        SetPiece boss = MawSpecs.boss(sortie);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        List<Integer> swells = new ArrayList<>();
        List<Integer> swellTicks = new ArrayList<>();
        List<Integer> ringTicks = new ArrayList<>();
        int glow = -1;
        int fan = -1;
        int open = 0;
        for (int tick = 0; tick < SimStep.ticks(8); tick++) {
            sortie.step(0);
            for (int value : MawSpecs.values(sortie, SimEvents.Type.SAC_SWELL)) {
                swells.add(value);
                swellTicks.add(tick);
            }
            if (MawSpecs.values(sortie, SimEvents.Type.ENEMY_FIRED).contains(4)) {
                ringTicks.add(tick);
            }
            if (!MawSpecs.values(sortie, SimEvents.Type.THROAT_GLOW).isEmpty() && glow < 0) {
                glow = tick;
                assertEquals(List.of(0), MawSpecs.values(sortie, SimEvents.Type.THROAT_GLOW), "the fan's glow");
            }
            if (glow >= 0 && fan < 0 && bulletsNear(sortie, 240, 405, 5) == 5) {
                fan = tick;
            }
            open += coil.jawOpen() ? 1 : 0;
        }
        assertEquals(List.of(1, 2, 3, 4, 1), swells, "one sac every 1.6 s in turn");
        for (int k = 1; k < swellTicks.size(); k++) {
            assertEquals(SimStep.ticks(1.6), swellTicks.get(k) - swellTicks.get(k - 1));
        }
        for (int k = 0; k < ringTicks.size(); k++) {
            assertEquals(SimStep.ticks(0.6), ringTicks.get(k) - swellTicks.get(k), "its ring after a 0.6 s swell");
        }
        assertEquals(SimStep.ticks(5) - SimStep.ticks(0.6), glow + 1, "the first glow 5 s - 0.6 s after it engaged");
        assertEquals(SimStep.ticks(0.6), fan - glow, "the fan of 5 after the glow");
        assertEquals(SimStep.ticks(3), open, "the jaw open 3 s");
        // The throat ×2 while the jaw is open, the skull ×0.5.
        double hp = boss.partHp(MawSpecs.HEAD);
        boss.damagePartAt(MawSpecs.HEAD, 10, 240, 405, 0, 0);
        assertEquals(hp - 5, boss.partHp(MawSpecs.HEAD), 1e-9, "the skull while shut");
        MawSpecs.until(sortie, SimEvents.Type.THROAT_GLOW, 6, 0);
        hp = boss.partHp(MawSpecs.HEAD);
        boss.damagePartAt(MawSpecs.HEAD, 10, 240, 405, 0, 0);
        assertEquals(hp - 20, boss.partHp(MawSpecs.HEAD), 1e-9, "the throat while open");
        // A destroyed sac's turn passes silently.
        sortie.destroyPart(0, 2);
        swells.clear();
        for (int tick = 0; tick < SimStep.ticks(6.4); tick++) {
            sortie.step(0);
            swells.addAll(MawSpecs.values(sortie, SimEvents.Type.SAC_SWELL));
        }
        assertEquals(3, swells.size(), "three breaths in four turns: " + swells);
        assertFalse(swells.contains(2));
    }

    @Test
    void phaseOneEndsOnItsSacsOrTheHeadsShareAndTheDivesPhaseWaitsForItsDive() {
        Sortie sortie = invulnerable(QUIET);
        MawSpecs.untilUp(sortie);
        for (int sac : MawSpecs.SACS) {
            sortie.destroyPart(0, sac);
        }
        assertEquals(List.of(1), MawSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        Sortie other = invulnerable(QUIET);
        MawSpecs.untilUp(other);
        MawSpecs.headTo(other, 0.71);
        MawSpecs.steps(other, 0.5, 0);
        assertEquals(0, MawSpecs.boss(other).phase());
        MawSpecs.headTo(other, 0.69);
        assertEquals(List.of(1), MawSpecs.until(other, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        // Below 30 % at once: the dive comes first, and the phase ends only once it is over.
        MawSpecs.headTo(other, 0.25);
        MawSpecs.until(other, SimEvents.Type.MAW_DIVE, 0.1, 0);
        int steps = 0;
        while (MawSpecs.coil(other).diveState() != CoilArena.DiveState.DONE) {
            assertEquals(1, MawSpecs.boss(other).phase(), "no phase end in the dive");
            other.step(0);
            steps++;
        }
        assertEquals(
                SimStep.ticks(2)
                        + SimStep.ticks(4)
                        + SimStep.ticks(2)
                        + SimStep.ticks(0.8)
                        + SimStep.ticks(3)
                        + SimStep.ticks(2),
                steps,
                "the dive's length");
        assertEquals(List.of(2), MawSpecs.values(other, SimEvents.Type.BOSS_PHASE), "the phase ends as it is over");
    }

    @Test
    void theFanWallsLeaveAGapBesideTheShipThatChains() {
        Sortie sortie = invulnerable(QUIET.withAttacks());
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.steerTo(sortie, 240, 100);
        MawSpecs.until(sortie, SimEvents.Type.THROAT_GLOW, 5, 0);
        assertTrue(coil.wallGlowing());
        assertTrue(coil.jawOpen(), "the jaw open through a wall's glow");
        List<Integer> first = MawSpecs.until(sortie, SimEvents.Type.FAN_WALL, 1, 0);
        assertEquals(1, first.size());
        assertEquals(16, bulletsNear(sortie, 240, 405, 4), "17 orbs less the gap");
        double gap = coil.lastGap();
        double off = Math.abs(gap - 240);
        assertTrue(off >= 40 && off <= 90, "40-90 px beside the ship: " + gap);
        assertFalse(coil.jawOpen(), "shut at the wall");
        MawSpecs.steerTo(sortie, 300, 100);
        MawSpecs.until(sortie, SimEvents.Type.FAN_WALL, 5, 0);
        double next = coil.lastGap();
        double nextOff = Math.abs(next - 300);
        assertTrue(nextOff >= 40 && nextOff <= 90, "beside the ship again: " + next);
        assertTrue(Math.abs(next - gap) <= 140, "within 140 px of the last gap: " + gap + " -> " + next);
        // Easy: 13 orbs with two gaps, one either side of the ship.
        Sortie easy = invulnerable(QUIET.withAttacks().easyFit());
        MawSpecs.untilUp(easy);
        MawSpecs.headTo(easy, 0.65);
        MawSpecs.untilPhase(easy, 1, 0.1);
        MawSpecs.steerTo(easy, 240, 100);
        MawSpecs.until(easy, SimEvents.Type.FAN_WALL, 6, 0);
        assertEquals(11, bulletsNear(easy, 240, 405, 4));
        CoilArena easyCoil = MawSpecs.coil(easy);
        assertTrue((easyCoil.lastGap() - 240) * (easyCoil.secondGap() - 240) < 0, "either side of the ship");
    }

    @Test
    void aSweepSwingsABightInOnTheShipsHeightHitsItOnceAndPushesItOut() {
        Sortie sortie = MawSpecs.sortie(QUIET.withSweeps(), TestSpecs.RULES);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.steerTo(sortie, 62, 250);
        assertEquals(
                List.of(CoilSpec.Side.LEFT.ordinal()), MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP_TELL, 8, 0));
        int centre = -1;
        for (int i = 0; i < coil.segmentCount(); i++) {
            if (coil.segmentSide(i) == CoilSpec.Side.LEFT.ordinal()
                    && Math.abs(coil.segmentY(i) - 241) < 1e-9
                    && coil.inBight(i)) {
                centre = i;
            }
        }
        assertTrue(centre >= 0, "the bight on the left segment nearest the ship's y (241 for 250)");
        int tell = 0;
        while (MawSpecs.values(sortie, SimEvents.Type.COIL_SWEEP).isEmpty()) {
            sortie.step(0);
            tell++;
        }
        assertEquals(SimStep.ticks(1.2), tell, "the 1.2 s tell");
        double before = health(sortie);
        int strikes = 0;
        for (int i = 0; i < SimStep.ticks(0.5 + 0.7); i++) {
            sortie.step(Command.LEFT.bit());
            List<Integer> struck = MawSpecs.values(sortie, SimEvents.Type.COIL_STRIKE);
            if (!struck.isEmpty()) {
                assertEquals(List.of(CoilArena.STRUCK_SHIP), struck);
                assertEquals(before - 10, health(sortie), 1e-9, "heavy: 10");
            }
            strikes += struck.size();
        }
        assertEquals(1, strikes, "once a sweep");
        assertTrue(
                sortie.ship().x() > 160,
                "pushed out of the bight (110 px): " + sortie.ship().x());
        assertEquals(CoilArena.SweepState.HOLD, coil.sweepState());
        MawSpecs.steps(sortie, 1.5, Command.LEFT.bit());
        assertEquals(62, sortie.ship().x(), 1e-9, "back at the wall once the bight has swung back");
        assertEquals(
                List.of(CoilSpec.Side.RIGHT.ordinal()), MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP_TELL, 7, 0));
    }

    @Test
    void theDiveGoesUnderTheLungeRisesUpTheColumnAndTheCoilComesBack() {
        List<RadioCue> radio = List.of(
                new RadioCue(CueTrigger.BOSS_ARRIVAL, 0, "", "Varga", "There it is.", false, "neutral", "Varga"),
                new RadioCue(CueTrigger.BOSS_DIVE, 0, "", "Varga", "It's gone under!", false, "neutral", "Varga"));
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, MawSpecs.level(QUIET, radio), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        SetPiece boss = MawSpecs.boss(sortie);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, MawSpecs.ARRIVE + 1, 0);
        assertEquals(List.of(0), MawSpecs.values(sortie, SimEvents.Type.RADIO), "boss-arrival at the arrival");
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.steps(sortie, 1, 0);
        assertEquals(CoilArena.DiveState.READY, coil.diveState(), "not above 50 %");
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.until(sortie, SimEvents.Type.MAW_DIVE, 0.1, 0);
        assertEquals(List.of(1), MawSpecs.values(sortie, SimEvents.Type.RADIO), "boss-dive at the dive");
        assertFalse(coil.walled(), "the walls go with the slide");
        MawSpecs.steps(sortie, 1.1, 0);
        assertEquals(Layer.SUB, boss.partLayer(MawSpecs.HEAD), "under from the slide's middle");
        assertFalse(boss.onPlane());
        MawSpecs.steps(sortie, 1, Command.LEFT.bit());
        assertTrue(
                sortie.ship().x() < 62, "no wall in the dive: " + sortie.ship().x());
        MawSpecs.steerTo(sortie, 300, 100);
        List<Integer> column = MawSpecs.until(sortie, SimEvents.Type.LUNGE_TELL, 5, 0);
        assertEquals(1, column.size());
        assertEquals(sortie.ship().x(), coil.columnX(), 1e-9, "the column at the ship's x");
        assertEquals((int) Math.round(coil.columnX()), column.getFirst());
        double before = health(sortie);
        int shadow = untilDive(sortie, CoilArena.DiveState.LUNGE, 3, 0);
        assertTrue(Math.abs(shadow - SimStep.ticks(2)) <= 1, "the 2 s shadow: " + shadow);
        assertEquals(Layer.AIR, boss.partLayer(MawSpecs.HEAD), "the head bursts up through the play plane");
        assertTrue(coil.jawOpen(), "the throat's window as it passes");
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(0);
            hits += MawSpecs.values(sortie, SimEvents.Type.LUNGE_HIT).size();
            assertEquals(coil.columnX(), boss.x(), 1e-9);
        }
        assertEquals(1, hits, "caught once in the column");
        assertEquals(before - 10, health(sortie), 1e-9);
        assertEquals(CoilArena.DiveState.BODY, coil.diveState());
        MawSpecs.steps(sortie, 0.5, 0);
        assertTrue(Math.abs(sortie.ship().x() - coil.columnX()) >= 45 + 12 - 1e-9, "pushed out of the body's column");
        MawSpecs.until(sortie, SimEvents.Type.COIL_RETURN, 3, 0);
        untilDive(sortie, CoilArena.DiveState.DONE, 2.1, 0);
        assertTrue(coil.walled(), "the walls back");
        assertEquals(MawSpecs.REST_X, boss.x(), 1e-9);
        // Once in the fight.
        MawSpecs.headTo(sortie, 0.35);
        MawSpecs.steps(sortie, 2, 0);
        assertEquals(CoilArena.DiveState.DONE, coil.diveState());
    }

    @Test
    void theImplantsSparkOpenInTurnFireTheirBurstsAndCountOnTheHeadsBar() {
        Sortie sortie = invulnerable(QUIET.withAttacks());
        SetPiece boss = MawSpecs.boss(sortie);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        untilDive(sortie, CoilArena.DiveState.DONE, 20, 0);
        MawSpecs.steerTo(sortie, 240, 100);
        MawSpecs.headTo(sortie, 0.25);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        assertEquals(2, boss.phase());
        assertEquals(1, sortie.pickupCount(), "the armour patch as the phase begins");
        assertEquals(List.of(0), MawSpecs.until(sortie, SimEvents.Type.IMPLANT_SPARK, 0.1, 0), "the neck sparks first");
        assertFalse(coil.jawOpen(), "the jaw clamped shut");
        assertTrue(boss.partShielded(MawSpecs.NECK));
        int tell = 0;
        while (MawSpecs.values(sortie, SimEvents.Type.IMPLANT_OPEN).isEmpty()) {
            sortie.step(0);
            tell++;
        }
        assertTrue(Math.abs(tell - SimStep.ticks(1)) <= 1, "the 1 s tell: " + tell);
        assertFalse(boss.partShielded(MawSpecs.NECK), "open");
        assertTrue(boss.partShielded(MawSpecs.LEFT_IMPLANT), "the others shut");
        double head = boss.partHp(MawSpecs.HEAD);
        boss.damagePartAt(MawSpecs.NECK, 10, 110, 480, 0, 1);
        assertEquals(head - 20, boss.partHp(MawSpecs.HEAD), 1e-9, "×2 on the head's HP");
        assertEquals(1, boss.partHp(MawSpecs.NECK), 1e-9, "the implant itself untouched");
        assertEquals(boss.partHp(MawSpecs.HEAD) / MawSpecs.HEAD_HP, boss.barShare(), 1e-12);
        // Its bursts: 3 bolts after each 0.4 s arc, every 1.5 s.
        int shots = 0;
        int arcs = 0;
        for (int i = 0; i < SimStep.ticks(3.9); i++) {
            sortie.step(0);
            shots += bulletsNear(sortie, 110, 480, 3.5);
            arcs += MawSpecs.values(sortie, SimEvents.Type.IMPLANT_ARC).size();
        }
        assertEquals(2, arcs, "the arcs after the first, at the open");
        assertEquals(9, shots, "three bursts of 3 in its 4 s");
        assertEquals(List.of(0), MawSpecs.until(sortie, SimEvents.Type.IMPLANT_SHUT, 0.2, 0));
        assertEquals(List.of(1), MawSpecs.values(sortie, SimEvents.Type.IMPLANT_SPARK), "then the left one");
        MawSpecs.until(sortie, SimEvents.Type.IMPLANT_OPEN, 1.1, 0);
        assertFalse(boss.partShielded(MawSpecs.LEFT_IMPLANT));
        // A hit on an open implant that takes the head's last HP kills the boss.
        sortie.destroyPart(0, MawSpecs.LEFT_IMPLANT);
        assertTrue(boss.destroyed());
        assertFalse(coil.walled());
    }

    @Test
    void itShedsEelSwarmsFromTheCoilAwayFromTheShipAtMostThePhasesMostAlive() {
        Sortie sortie = invulnerable(QUIET.withSheds());
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.steerTo(sortie, 80, 270);
        int total = sortie.enemyTotal();
        List<Integer> tell = MawSpecs.until(sortie, SimEvents.Type.SHED_TELL, 10.5, 0);
        assertEquals(List.of(1), tell, "the left point skipped (nearest the ship): the bottom one");
        assertTrue(coil.shedTelling());
        int flare = 0;
        while (MawSpecs.values(sortie, SimEvents.Type.BOSS_LAUNCHED).isEmpty()) {
            sortie.step(0);
            flare++;
        }
        assertTrue(Math.abs(flare - SimStep.ticks(1)) <= 1, "the 1 s flare: " + flare);
        assertEquals(1, sortie.chainCount());
        assertEquals(10, sortie.chain(0).size(), "head, 8 segments and the tail");
        assertEquals(total + 1, sortie.enemyTotal(), "counted among the level's enemies");
        assertEquals(0, sortie.edgeWarnings(), "no edge warning");
        int tells = 1;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(0);
            List<Integer> points = MawSpecs.values(sortie, SimEvents.Type.SHED_TELL);
            if (!points.isEmpty()) {
                tells++;
                assertEquals(0, sortie.chainCount(), "at most one alive in phase 1");
                assertTrue(points.getFirst() != 0, "never the point nearest the ship");
            }
        }
        assertTrue(tells >= 3, "one about every 10 s: " + tells);
    }

    @Test
    void aKillRatioThatLeavesTheBossOutLeavesItsShedSwarmsOutButTheyStillPay() {
        // Step H2 (m8h-gaps.md §5 H7): kill_ratio with boss_counted: false.
        LevelScript level = MawSpecs.level(QUIET.withSheds(), List.of()).toBuilder()
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.65, 100).withBossesCounted(false)))
                .build();
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), 60);
        MawSpecs.untilUp(sortie);
        MawSpecs.steerTo(sortie, 80, 270);
        int total = sortie.enemyTotal();
        MawSpecs.until(sortie, SimEvents.Type.BOSS_LAUNCHED, 12, 0);
        assertEquals(1, sortie.chainCount());
        assertEquals(total, sortie.enemyTotal(), "the shed swarm is not among the enemies");
        int head = -1;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).chain() != null && sortie.enemy(i).link() == 0) {
                head = i;
            }
        }
        assertTrue(head >= 0);
        int kills = sortie.kills();
        int credits = sortie.credits();
        sortie.destroyEnemy(head);
        assertEquals(kills, sortie.kills(), "its kill does not count");
        assertTrue(sortie.credits() > credits, "it still pays");
    }

    @Test
    void hardCastsAShorterShadowAndOpensTheImplantsShorter() {
        Sortie sortie = invulnerable(QUIET.hardFit());
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.until(sortie, SimEvents.Type.LUNGE_TELL, 7, 0);
        int shadow = untilDive(sortie, CoilArena.DiveState.LUNGE, 3, 0);
        assertTrue(Math.abs(shadow - SimStep.ticks(1.6)) <= 1, "hard: 1.6 s: " + shadow);
        untilDive(sortie, CoilArena.DiveState.DONE, 7, 0);
        MawSpecs.headTo(sortie, 0.25);
        MawSpecs.until(sortie, SimEvents.Type.IMPLANT_OPEN, 1.2, 0);
        int open = 0;
        while (MawSpecs.values(sortie, SimEvents.Type.IMPLANT_SHUT).isEmpty()) {
            sortie.step(0);
            open++;
        }
        assertTrue(Math.abs(open - SimStep.ticks(3)) <= 1, "hard: open 3 s: " + open);
    }

    @Test
    void theBulwarkIsKeptInsideTheCoilAndASweepCatchesIt() {
        // Step H2: the Bulwark holds the gap off the coil and leaves a told bight (MawHazardsTest); one too slow to
        // leave it in the tell is caught at the wall.
        BulwarkSpec slow = new BulwarkSpec(
                WingmanSpec.Side.RIGHT,
                120,
                120,
                36,
                new Hitbox(14, 14),
                10,
                new WingmanSpec.Offset(56, 20),
                BulwarkTest.CANNON,
                150,
                0.3);
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withBulwark(slow),
                MawSpecs.level(QUIET.withSweeps(), List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        MawSpecs.untilUp(sortie);
        // It flies on the ship's right: with the ship by the right wall it is held off the wall.
        MawSpecs.steerTo(sortie, 380, 250);
        MawSpecs.steps(sortie, 25, 0);
        assertEquals(418 - MawHazards.GAP, bulwark.x(), 0.1, "inside the right wall");
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        double armour = bulwark.armour();
        List<Integer> side = List.of();
        while (!side.equals(List.of(CoilSpec.Side.RIGHT.ordinal()))) {
            side = MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP, 15, 0);
        }
        int escort = 0;
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(0);
            for (int value : MawSpecs.values(sortie, SimEvents.Type.COIL_STRIKE)) {
                escort += (value & CoilArena.STRUCK_ESCORT) != 0 ? 1 : 0;
            }
        }
        assertEquals(1, escort, "the bight caught the Bulwark by the wall, once");
        assertEquals(armour - 10, bulwark.armour(), 1e-9);
        assertTrue(bulwark.x() < 410 - 50, "pushed out of the bight: " + bulwark.x());
    }

    @Test
    void aCoiledFightRunsTheSameTwiceAndAllocatesNothingPerStep() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = withBulwark();
            for (int i = 0; i < SimStep.ticks(70); i++) {
                sortie.step(FIRE | ((i / 90) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                if (i == SimStep.ticks(15)) {
                    MawSpecs.headTo(sortie, 0.45);
                }
                if (i == SimStep.ticks(40)) {
                    MawSpecs.headTo(sortie, 0.25);
                }
            }
            assertEquals(2, MawSpecs.boss(sortie).phase(), "through the dive into phase 3");
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = withBulwark();
                    MawSpecs.untilUp(sortie);
                    MawSpecs.headTo(sortie, 0.65);
                    MawSpecs.steps(sortie, 1, 0);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(FIRE | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                        if (i == SimStep.ticks(20)) {
                            MawSpecs.headTo(sortie, 0.45);
                        }
                    }
                });
        assertEquals(0, allocated, "the coil, its walls, sweeps, dive, sheds and fire allocate nothing per step");
    }

    private static Sortie withBulwark() {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK),
                MawSpecs.level(MawSpecs.Fit.ALL, List.of()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void aCoilNeedsAnArenaWithoutLanesBreachOrMovesAndItsAttacksMustFit() {
        BossSpec boss = MawSpecs.maw(QUIET).boss().orElseThrow();
        CoilSpec coil = boss.coil().orElseThrow();
        assertTrue(boss.coils());
        BossSpec plain = boss.withCoil(Optional.empty());
        assertThrows(
                IllegalArgumentException.class,
                () -> plain.withBreach(Optional.of(BreachArenaTest.BREACH)).withCoil(Optional.of(coil)),
                "no breach and coil");
        CoilSpec wrongWall = new CoilSpec(
                coil.body(),
                coil.introSeconds(),
                coil.sacs(),
                coil.swellSeconds(),
                coil.jaw(),
                Optional.of(new CoilSpec.Wall(MawSpecs.RING_1, 0.8, 1, 40, 90, 140)),
                coil.sweeps(),
                coil.dive(),
                coil.implants(),
                coil.shed());
        assertThrows(
                IllegalArgumentException.class, () -> plain.withCoil(Optional.of(wrongWall)), "a wall is a fan attack");
        assertThrows(
                IllegalArgumentException.class,
                () -> new CoilSpec.Sweeps(
                        List.of(7.0),
                        List.of(List.of()),
                        5,
                        1.2,
                        0.5,
                        0.8,
                        0.8,
                        110,
                        90,
                        10,
                        new BossSpec.Clamp(120, 380),
                        new BossSpec.Clamp(140, 340)),
                "a phase that sweeps has sides");
        assertThrows(IllegalArgumentException.class, () -> new CoilSpec.Jaw(0.6, 0.3));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CoilSpec.Shed(TestSpecs.SKITTER, MawSpecs.SHED_POINTS, List.of(10.0), List.of(1), 1),
                "a shed unit is a chain");
        assertTrue(boss.spawnKinds().isEmpty(), "no shed without sheds");
        assertEquals(
                1,
                MawSpecs.maw(QUIET.withSheds())
                        .boss()
                        .orElseThrow()
                        .spawnKinds()
                        .size(),
                "the shed Eel Swarm among its kinds");
    }
}
