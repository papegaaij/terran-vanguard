package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks and The lifeboat; m10h-gaps.md §1 #10, #11,
 * §4, §5), on the test Ascendant ({@link Act6HSpecs}): the heart's death breaks the hull (K): the surviving sacs burst
 * and pay, {@code HULL_BROKEN} and never {@code BOSS_DESTROYED}, the wreck scenery; the flash at K+2.5, the breakout at
 * K+3; the lifeboat climbs from the screen's (240, 175) beyond the top edge in exactly 4 s, never below the screen's y
 * 200, weaving within 40 px on a seeded path; round 80 (B.1/A.4): it is scenery, never hit, paying, firing or touching
 * the ship; at the top edge {@code BOSS_ESCAPED}: the piece gone, the clock running again, the fight's time its kill time, the {@code
 * boss-escaped} cue; a retry and the boss checkpoint start it afresh; its state is hashed.
 */
class EscapeRunTest {
    private static final double EPS = 1e-9;

    /** A rig at K: every part but the lifeboat dead, the hull broken. */
    private static Act6HSpecs.Rig broken(SetPieceSpec spec) {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(spec).settled().toEruption().pastEruption();
        for (int p : Act6HSpecs.PHASE_2) {
            rig.kill(p);
        }
        assertTrue(rig.piece.breakHull(123));
        return rig;
    }

    @Test
    void theLifeboatBreaksOutAtKPlusThreeAndClimbsBeyondTheTopEdgeInFourSeconds() {
        Act6HSpecs.Rig rig = broken(Act6HSpecs.QUIET);
        SetPiece boss = rig.piece;
        EscapeRun run = boss.escape().orElseThrow();
        int k = rig.tick;
        int flash = -1;
        int away = -1;
        int escaped = -1;
        double lowest = Double.MAX_VALUE;
        double widest = 0;
        while (escaped < 0 && rig.tick - k < SimStep.ticks(30)) {
            rig.step();
            if (rig.values(SimEvents.Type.HULL_FLASH).size() == 1) {
                flash = rig.tick - k;
                assertEquals(123, rig.values(SimEvents.Type.HULL_FLASH).getFirst(), "the shower's credits");
            }
            if (!rig.values(SimEvents.Type.LIFEBOAT_AWAY).isEmpty()) {
                away = rig.tick - k;
                assertEquals("lifeboat-away", rig.cues.getLast());
            }
            if (run.out()) {
                lowest = Math.min(lowest, run.y());
                widest = Math.max(widest, Math.abs(run.x() - 240));
            }
            assertTrue(boss.partShielded(Act6HSpecs.LIFEBOAT), "never hurt");
            assertFalse(boss.partOnField(Act6HSpecs.LIFEBOAT), "never a target");
            assertFalse(
                    boss.partTouches(Act6HSpecs.LIFEBOAT, new Hitbox(4, 12), run.x(), run.y()),
                    "shots pass through it");
            assertFalse(boss.partInBlast(Act6HSpecs.LIFEBOAT, run.x(), run.y(), 50), "blasts pass it");
            if (run.escapedNow()) {
                escaped = rig.tick - k;
            }
        }
        assertEquals(SimStep.ticks(2.5), flash, "the flash at K+2.5");
        assertEquals(SimStep.ticks(3), away, "the breakout at K+3");
        assertEquals(SimStep.ticks(3) + SimStep.ticks(4), escaped, "beyond the top edge 4 s after the breakout");
        assertTrue(lowest >= PlayField.HEIGHT - 200, "never below the screen's y 200: " + lowest);
        assertTrue(widest <= 40 + EPS, "within its weave: " + widest);
        assertTrue(widest > 5, "it weaves: " + widest);
        assertTrue(run.y() - 30 >= PlayField.HEIGHT, "wholly beyond the top edge: " + run.y());
    }

    @Test
    void itsClimbEasesUpInTheSpecsRatio() {
        Act6HSpecs.Rig rig = broken(Act6HSpecs.QUIET);
        EscapeRun run = rig.piece.escape().orElseThrow();
        rig.steps(3.05);
        double y0 = run.y();
        rig.step();
        double early = (run.y() - y0) / SimStep.SECONDS;
        rig.steps(3.7);
        double y1 = run.y();
        rig.step();
        double late = (run.y() - y1) / SimStep.SECONDS;
        assertTrue(late > 3.5 * early, "20 → 90: " + early + " → " + late);
    }

    @Test
    void theWeaveIsSeededTheSameDrawsTheSamePath() {
        double[] first = path(Act6HSpecs.QUIET);
        double[] again = path(Act6HSpecs.QUIET);
        assertEquals(first.length, again.length);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], again[i], EPS);
        }
    }

    private static double[] path(SetPieceSpec spec) {
        Act6HSpecs.Rig rig = broken(spec);
        EscapeRun run = rig.piece.escape().orElseThrow();
        double[] xs = new double[SimStep.ticks(7)];
        for (int i = 0; i < xs.length; i++) {
            rig.step();
            xs[i] = run.x();
        }
        return xs;
    }

    @Test
    void itIsSceneryNoDamageNoPayNoFire() {
        Act6HSpecs.Rig rig = broken(Act6HSpecs.QUIET);
        SetPiece boss = rig.piece;
        double hp = boss.partHp(Act6HSpecs.LIFEBOAT);
        assertFalse(boss.damagePart(Act6HSpecs.LIFEBOAT, 500), "before its breakout");
        rig.steps(3.1);
        assertTrue(boss.escape().orElseThrow().out());
        assertFalse(boss.damagePart(Act6HSpecs.LIFEBOAT, 500), "out");
        assertFalse(boss.damagePartFlat(Act6HSpecs.LIFEBOAT, 400), "the Lance's");
        assertEquals(hp, boss.partHp(Act6HSpecs.LIFEBOAT), EPS, "never hurt");
        assertFalse(boss.partWrecked(Act6HSpecs.LIFEBOAT), "never dies");
        assertEquals(0, boss.barShare(), EPS, "the hull's bar is empty; the lifeboat is in none");
        for (int i = 0; i < SimStep.ticks(4); i++) {
            rig.step();
            assertTrue(rig.bullets.isEmpty(), "no wake");
        }
    }

    @Test
    void theHullBreaksOnceTheSacsPayAndNoBossDestroyedTheFightEndsAtTheTopEdge() {
        LevelScript level = Act6HSpecs.level(
                Act6HSpecs.QUIET,
                List.of(
                        new RadioCue(
                                CueTrigger.BOSS_PHASE, 0, EscapeRun.HULL_BREAKS, "Okafor", "It breaks!", false, "n"),
                        new RadioCue(CueTrigger.BOSS_ESCAPED, 0, "ascendant", "Varga", "He's gone.", false, "n")));
        Sortie sortie = new Sortie(
                1, Act5ESpecs.loadout(Act5ESpecs.bolt(1, false)), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        SetPiece boss = Act6HSpecs.settle(sortie, 0);
        for (int p : Act6HSpecs.PHASE_1) {
            sortie.destroyPart(0, p);
        }
        stepSeconds(sortie, 4.1);
        sortie.destroyPart(0, Act6HSpecs.SAC_1);
        sortie.destroyPart(0, Act6HSpecs.SAC_2);
        sortie.step(0);
        assertEquals(2, boss.phase(), "the heart open");
        int before = sortie.credits();
        int radioBefore = sortie.events().count(SimEvents.Type.RADIO);
        sortie.destroyPart(0, Act6HSpecs.HEART);
        // The kill's events join this step's (the next step clears them).
        assertEquals(1, sortie.events().count(SimEvents.Type.HULL_BROKEN));
        assertEquals(0, sortie.events().count(SimEvents.Type.BOSS_DESTROYED));
        assertEquals(radioBefore + 1, sortie.events().count(SimEvents.Type.RADIO), "the hull-breaks line");
        EscapeRun run = boss.escape().orElseThrow();
        assertTrue(run.broken(), "K");
        assertTrue(boss.partWrecked(Act6HSpecs.SAC_3), "the surviving sac burst with the heart");
        assertTrue(sortie.credits() - before > 0, "the heart and the sac paid");
        int heartAndSac = sortie.credits() - before;
        assertFalse(boss.destroyed());
        assertTrue(boss.present());
        assertFalse(boss.bodyTouchesHull(TestSpecs.SHIP.hull(), 120, PlayField.HEIGHT - 100), "the wreck is scenery");
        double atK = sortie.levelSeconds();
        int destroyed = 0;
        int escapedAt = -1;
        int radioLines = 0;
        int afterK = sortie.credits();
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            destroyed += events.count(SimEvents.Type.BOSS_DESTROYED);
            radioLines += events.count(SimEvents.Type.RADIO);
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BOSS_ESCAPED) {
                    escapedAt = i;
                }
            }
            if (escapedAt < 0) {
                assertTrue(sortie.arenaHalted(), "the clock holds until the escape");
                assertEquals(atK, sortie.levelSeconds(), EPS);
            }
        }
        assertEquals(0, destroyed, "never BOSS_DESTROYED");
        assertEquals(SimStep.ticks(7) - 1, escapedAt, "at the top edge");
        assertFalse(boss.present(), "gone");
        assertTrue(boss.escaped());
        assertFalse(boss.destroyed());
        assertFalse(sortie.arenaHalted(), "the clock resumes");
        assertTrue(sortie.levelSeconds() > atK, "the level goes on");
        assertTrue(boss.killSeconds() > 0, "the fight's time ends at the escape");
        assertEquals(afterK, sortie.credits(), "the escape pays nothing");
        assertEquals(1, radioLines, "boss-escaped");
        assertTrue(heartAndSac > 0);
    }

    @Test
    void itsClimbNeverTouchesTheShip() {
        Sortie sortie = toK();
        Ship ship = sortie.ship();
        // Up under the breakout place before the lifeboat tears free (the wreck has no contact, nor the lifeboat).
        for (int i = 0; i < SimStep.ticks(2.9); i++) {
            int up = ship.y() < PlayField.HEIGHT - 190 ? Command.UP.bit() : 0;
            int side = ship.x() < 236 ? Command.RIGHT.bit() : ship.x() > 244 ? Command.LEFT.bit() : 0;
            sortie.step(up | side);
        }
        int strikes = 0;
        double lost = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            double was = ship.defences().shield() + ship.defences().armour();
            sortie.step(0);
            double now = ship.defences().shield() + ship.defences().armour();
            if (now < was - 1e-9) {
                strikes++;
                lost = was - now;
            }
        }
        assertEquals(0, strikes, "no contact: " + lost);
    }

    @Test
    void aRetryAndTheCheckpointStartItAfreshAndItIsHashed() {
        Act6HSpecs.Rig rig = broken(Act6HSpecs.QUIET);
        rig.steps(3.5);
        SetPiece boss = rig.piece;
        StateHash a = new StateHash();
        boss.addTo(a);
        rig.step();
        StateHash b = new StateHash();
        boss.addTo(b);
        assertTrue(a.value() != b.value(), "the climb is in the hash");
        boss.reset();
        EscapeRun run = boss.escape().orElseThrow();
        assertFalse(run.broken());
        assertFalse(run.out());
        assertFalse(boss.partWrecked(Act6HSpecs.HEART));
    }

    @Test
    void anEscapingBossHasNoVitalPart() {
        List<PartSpec> parts = new ArrayList<>(Act6HSpecs.parts());
        PartSpec heart = parts.get(Act6HSpecs.HEART);
        parts.set(
                Act6HSpecs.HEART,
                new PartSpec(
                        heart.name(),
                        heart.dx(),
                        heart.dy(),
                        heart.box(),
                        heart.hp(),
                        true,
                        heart.bounty(),
                        heart.gun(),
                        0,
                        1));
        SetPieceSpec spec = Act6HSpecs.QUIET;
        try {
            new SetPieceSpec(
                    spec.slug(),
                    spec.size(),
                    spec.body(),
                    spec.contactDamage(),
                    parts,
                    List.of(),
                    spec.drop(),
                    spec.boss());
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("no vital part"), expected.getMessage());
            return;
        }
        throw new AssertionError("an escaping boss with a vital part was accepted");
    }

    /** A sortie stepped to K (the heart destroyed), the lifeboat not out yet. */
    static Sortie toK() {
        Sortie sortie =
                Act6HSpecs.sortie(Act6HSpecs.QUIET, Act5ESpecs.loadout(Act5ESpecs.bolt(1, false)), TestSpecs.RULES);
        Act6HSpecs.settle(sortie, 0);
        for (int p : Act6HSpecs.PHASE_1) {
            sortie.destroyPart(0, p);
        }
        stepSeconds(sortie, 4.1);
        for (int p : Act6HSpecs.PHASE_2) {
            sortie.destroyPart(0, p);
            sortie.step(0);
        }
        assertTrue(sortie.setPiece(0).escape().orElseThrow().broken());
        return sortie;
    }

    static void stepSeconds(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }
}
