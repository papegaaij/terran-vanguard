package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The cruisers (draft); design/campaign Level 28, sections 8–10):
 * the finale's two Ascendancy cruisers on the level clock ({@link CruiserRun}) with the spec's medium numbers: they
 * rise over 2 s, run 35 s (broadsides of 7 shells in a 70 px band told 1.2 s, every 3 s, two bands from 20 s on; two
 * flak markers every 4 s; aimed bursts of 3 every 2.5 s), then fall over 5 s; invulnerable, their hulls stop shots and
 * the beam.
 */
class CruiserRunTest {
    static final CruiserRunSpec.Broadside BROADSIDE = new CruiserRunSpec.Broadside(70, 1.2, 7, 260, 6, 3, 20, 100);
    static final CruiserRunSpec.Flak FLAK = new CruiserRunSpec.Flak(2, 4, 1, 45, 80, 6, 8, 120, 4);
    static final CruiserRunSpec.PointDefence DEFENCE =
            new CruiserRunSpec.PointDefence(3, 170, 4, 2.5, List.of(100.0, 270.0, 440.0));
    static final double RISE = 2;
    static final double RUN = 4;
    static final double END = 39;

    static CruiserRunSpec spec(CruiserRunSpec.Broadside broadside, CruiserRunSpec.Flak flak) {
        return new CruiserRunSpec(RISE, RUN, END, 5, new Hitbox(220, 640), 110, 370, broadside, flak, DEFENCE);
    }

    static final CruiserRunSpec SPEC = spec(BROADSIDE, FLAK);

    static LevelScript level(CruiserRunSpec spec) {
        return LevelScript.builder(28, 4)
                .sections(List.of(new Section(46, 200)))
                .hazards(HazardSpec.NONE.withCruisers(Optional.of(spec)))
                .build();
    }

    private static Sortie sortie(CruiserRunSpec spec) {
        return new Sortie(
                1, TestSpecs.LOADOUT, level(spec), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static CruiserRun run(Sortie sortie) {
        return sortie.cruisers().orElseThrow();
    }

    /** An event and the level clock step it came in. */
    record Event(int tick, SimEvents.Type type, double x, double y, int value) {}

    /** Steps {@code seconds} with {@code commands}, recording the events of {@code types}. */
    static List<Event> record(Sortie sortie, double seconds, int commands, SimEvents.Type... types) {
        List<Event> out = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                for (SimEvents.Type type : types) {
                    if (events.type(e) == type) {
                        out.add(new Event(sortie.levelTick, type, events.x(e), events.y(e), events.value(e)));
                    }
                }
            }
        }
        return out;
    }

    private static List<Event> of(List<Event> events, SimEvents.Type type) {
        return events.stream().filter(e -> e.type() == type).toList();
    }

    @Test
    void theyRiseThenRunOnTheFlanksThenFallOffTheBottomEdge() {
        Sortie sortie = sortie(SPEC);
        CruiserRun run = run(sortie);
        assertEquals(0, run.hullX(CruiserRun.LEFT), 1e-9, "the left hull's inner edge at x 110");
        assertEquals(480, run.hullX(CruiserRun.RIGHT), 1e-9, "the right hull's inner edge at x 370");
        List<Event> rise = record(sortie, RISE, 0, SimEvents.Type.CRUISER_RISE);
        assertEquals(1, rise.size());
        assertEquals(SimStep.ticks(RISE), rise.getFirst().tick());
        assertTrue(run.present(sortie.levelTick));
        assertFalse(run.solid(sortie.levelTick), "not solid while they rise");
        MawSpecs.steps(sortie, 1, 0);
        assertEquals(0.5, run.riseShare(sortie.levelTick), 0.01);
        MawSpecs.steps(sortie, 1, 0);
        assertEquals(1, run.riseShare(sortie.levelTick), 1e-9);
        assertTrue(run.solid(sortie.levelTick));
        assertEquals(PlayField.HEIGHT / 2.0, run.hullY(sortie.levelTick), 1e-9);
        MawSpecs.steps(sortie, END - RUN, 0);
        assertEquals(0, run.fallShare(sortie.levelTick), 1e-9, "the run's end");
        MawSpecs.steps(sortie, 2.5, 0);
        double half = run.hullY(sortie.levelTick);
        assertTrue(half < PlayField.HEIGHT / 2.0 && half > -320, "falling: " + half);
        assertTrue(run.solid(sortie.levelTick), "still solid as they fall");
        MawSpecs.steps(sortie, 2.6, 0);
        assertEquals(-320, run.hullY(sortie.levelTick), 1e-9, "off the bottom edge");
        assertFalse(run.present(sortie.levelTick));
        assertFalse(run.solid(sortie.levelTick));
    }

    @Test
    void broadsidesTellTheirBandThenCrossItFromTheFlanksInTurn() {
        Sortie sortie = sortie(SPEC);
        MawSpecs.steerTo(sortie, 240, 200);
        MawSpecs.steps(sortie, RUN - sortie.levelSeconds() - 0.1, 0);
        double shipY = sortie.ship().y();
        List<Event> events =
                record(sortie, 15, 0, SimEvents.Type.BROADSIDE_TELL, SimEvents.Type.BROADSIDE, SimEvents.Type.FLAK);
        List<Event> tells = of(events, SimEvents.Type.BROADSIDE_TELL);
        List<Event> volleys = of(events, SimEvents.Type.BROADSIDE);
        int run = SimStep.ticks(RUN);
        assertEquals(run, tells.getFirst().tick(), "the first tell at the run's start");
        assertEquals(CruiserRun.LEFT, tells.getFirst().value(), "the left flank first");
        assertEquals(shipY, tells.getFirst().y(), 1e-9, "on the ship's y");
        assertEquals(run + SimStep.ticks(1.2), volleys.getFirst().tick(), "the volley after the 1.2 s tell");
        assertEquals(CruiserRun.RIGHT, tells.get(1).value(), "then the right flank");
        assertEquals(run + SimStep.ticks(3), tells.get(1).tick(), "every 3 s");
        assertNotEquals(shipY, tells.get(1).y(), "then a random band");
        assertEquals(5, tells.size(), "one every 3 s");
        for (int k = 0; k < tells.size(); k++) {
            assertEquals(k % 2, tells.get(k).value(), "the flanks in turn");
            double y = tells.get(k).y();
            assertTrue(y >= 35 && y <= PlayField.HEIGHT - 35, "the band on the field: " + y);
        }
    }

    @Test
    void aVolleyIsSevenShellsAcrossTheBandFromTheCruisersSide() {
        Sortie sortie = sortie(SPEC);
        MawSpecs.steerTo(sortie, 240, 200);
        MawSpecs.steps(sortie, RUN - sortie.levelSeconds() + 1.2 - SimStep.SECONDS, 0);
        double shipY = sortie.ship().y();
        int before = sortie.bulletCount();
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.BROADSIDE));
        int shells = 0;
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (bullet.vy() == 0 && bullet.vx() == 260) {
                shells++;
                assertEquals(6, bullet.damage(), 1e-9, "a medium shell");
                assertEquals(260, bullet.vx(), 1e-9);
                assertTrue(Math.abs(bullet.y() - shipY) < 35, "in the band: " + bullet.y());
                assertTrue(bullet.x() < 10, "from the left edge: " + bullet.x());
            }
        }
        assertEquals(7, shells);
        assertTrue(sortie.bulletCount() >= before + 7);
    }

    @Test
    void fromTwentySecondsTwoBandsComeAtOnceWithAFreeBandBetweenThem() {
        Sortie sortie = sortie(SPEC);
        MawSpecs.steerTo(sortie, 240, 270);
        List<Event> tells = record(sortie, END + 1, 0, SimEvents.Type.BROADSIDE_TELL);
        int run = SimStep.ticks(RUN);
        int doubles = 0;
        for (int i = 0; i < tells.size(); i++) {
            Event tell = tells.get(i);
            if (i + 1 < tells.size() && tells.get(i + 1).tick() == tell.tick()) {
                Event other = tells.get(i + 1);
                assertTrue(tell.tick() - run >= SimStep.ticks(20), "not before 20 s: " + (tell.tick() - run));
                assertNotEquals(tell.value(), other.value(), "one from each side");
                assertTrue(Math.abs(tell.y() - other.y()) >= 70 + 100 - 1e-9, "a 100 px free band between them");
                doubles++;
                i++;
            } else {
                assertTrue(tell.tick() - run < SimStep.ticks(20), "single bands before 20 s only");
            }
        }
        assertEquals(5, doubles, "at 21, 24, 27, 30 and 33 s");
        Event last = tells.getLast();
        assertTrue(last.tick() + SimStep.ticks(1.2) <= SimStep.ticks(END), "the last volley by the run's end");
        assertEquals(run + SimStep.ticks(33), last.tick(), "none whose volley would come after the end");
    }

    @Test
    void easyHasNoDoubleBandsAndOneFlakMarker() {
        Sortie sortie = sortie(spec(
                new CruiserRunSpec.Broadside(70, 1.5, 7, 260, 6, 3, Double.POSITIVE_INFINITY, 100),
                new CruiserRunSpec.Flak(1, 4, 1, 45, 80, 6, 8, 120, 4)));
        List<Event> events = record(sortie, END + 1, 0, SimEvents.Type.BROADSIDE_TELL, SimEvents.Type.FLAK);
        List<Event> tells = of(events, SimEvents.Type.BROADSIDE_TELL);
        for (int i = 1; i < tells.size(); i++) {
            assertNotEquals(tells.get(i - 1).tick(), tells.get(i).tick(), "one band at a time");
        }
        assertEquals(9, of(events, SimEvents.Type.FLAK).size(), "one marker every 4 s from 4 s to 36 s");
    }

    @Test
    void flakSetsMarkersByTheShipThatBurstIntoRingsAfterASecond() {
        Sortie sortie = sortie(SPEC);
        MawSpecs.steerTo(sortie, 240, 200);
        MawSpecs.steps(sortie, RUN - sortie.levelSeconds() - SimStep.SECONDS, 0);
        sortie.step(0);
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        List<Integer> markers = BreachArenaTest.values(sortie, SimEvents.Type.FLAK);
        assertEquals(List.of(0, 1), markers, "two markers");
        assertEquals(2, sortie.lobCount());
        Lob first = sortie.lob(0);
        assertEquals(shipX, first.targetX(), 1e-9, "the first on the ship");
        assertEquals(shipY, first.targetY(), 1e-9);
        Lob second = sortie.lob(1);
        double dx = second.targetX() - shipX;
        double dy = second.targetY() - shipY;
        assertTrue(Math.sqrt(dx * dx + dy * dy) <= 80 + 1e-9, "the other within 80 px of it");
        int bullets = sortie.bulletCount();
        int fuse = 0;
        while (sortie.events().count(SimEvents.Type.MORTAR_IMPACT) == 0) {
            sortie.step(Command.LEFT.bit());
            fuse++;
        }
        assertEquals(2, sortie.events().count(SimEvents.Type.MORTAR_IMPACT));
        assertTrue(Math.abs(fuse - SimStep.ticks(1)) <= 1, "after 1 s: " + fuse);
        assertTrue(sortie.bulletCount() >= bullets + 16, "two rings of 8");
        List<Event> flak = record(sortie, 9, 0, SimEvents.Type.FLAK);
        assertEquals(
                List.of(SimStep.ticks(RUN + 4), SimStep.ticks(RUN + 8)),
                flak.stream().map(Event::tick).distinct().toList(),
                "every 4 s");
    }

    @Test
    void pointDefenceFiresAimedBurstsOfThreeTheRightHalfAnIntervalLater() {
        Sortie sortie = sortie(SPEC);
        MawSpecs.steps(sortie, RUN - SimStep.SECONDS, 0);
        List<Event> shots = record(sortie, 5.1, 0, SimEvents.Type.POINT_DEFENCE);
        List<Event> left =
                shots.stream().filter(e -> e.value() == CruiserRun.LEFT).toList();
        List<Event> right =
                shots.stream().filter(e -> e.value() == CruiserRun.RIGHT).toList();
        int run = SimStep.ticks(RUN);
        int gap = SimStep.ticks(EnemyGun.BURST_GAP_SECONDS);
        assertEquals(
                List.of(run, run + gap, run + 2 * gap, run + 150, run + 150 + gap, run + 150 + 2 * gap),
                left.stream().map(Event::tick).toList().subList(0, 6),
                "bursts of 3 every 2.5 s");
        assertEquals(run + 75, right.getFirst().tick(), "the right half an interval later");
        assertEquals(110, left.getFirst().x(), 1e-9, "from the left hull's inner edge");
        assertEquals(100, left.getFirst().y(), 1e-9, "its first turret");
        assertEquals(270, left.get(3).y(), 1e-9, "then the next");
        assertEquals(370, right.getFirst().x(), 1e-9);
    }

    @Test
    void shotsSparkOffTheHullsAndTheBeamStopsOnThem() {
        Sortie sortie = sortie(SPEC);
        CruiserRun run = run(sortie);
        MawSpecs.steerTo(sortie, 60, 150);
        MawSpecs.steps(sortie, RISE + 0.5 - sortie.levelSeconds(), 0);
        assertEquals(Double.POSITIVE_INFINITY, run.column(60, 4, 160), "not solid while they rise");
        int glanced = 0;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.FIRE.bit());
            glanced += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
        }
        assertEquals(0, glanced, "the shots fly over the rising hull");
        MawSpecs.steps(sortie, RUN + 0.5 - sortie.levelSeconds(), 0);
        assertEquals(160, run.column(60, 4, 160), 1e-9, "a beam over the hull stops at once");
        assertEquals(Double.POSITIVE_INFINITY, run.column(240, 4, 160), "between the hulls it runs on");
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.FIRE.bit());
            glanced += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
            for (int s = 0; s < sortie.shotCount(); s++) {
                assertTrue(sortie.shot(s).x() > 110, "no shot flies on over the hull");
            }
        }
        assertTrue(glanced > 0, "the shots spark off it");
    }

    @Test
    void aRetryStartsTheRunAfreshAndItRunsTheSameTwiceWithoutAllocating() {
        long[] hashes = new long[3];
        for (int k = 0; k < 2; k++) {
            Sortie sortie = sortie(SPEC);
            for (int i = 0; i < SimStep.ticks(END + 4); i++) {
                sortie.step((i / 50) % 2 == 0 ? Command.UP.bit() | Command.LEFT.bit() : Command.DOWN.bit());
            }
            hashes[k] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        Sortie sortie = sortie(SPEC);
        MawSpecs.steps(sortie, RUN + 2, 0);
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(0);
        assertEquals(2, sortie.attempt());
        assertFalse(run(sortie).bandTelling(0));
        long allocated = Allocations.least(
                () -> {
                    Sortie fresh = sortie(SPEC);
                    MawSpecs.steps(fresh, RUN - 0.5, 0);
                    return fresh;
                },
                fresh -> {
                    for (int i = 0; i < SimStep.ticks(25); i++) {
                        fresh.step((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
                    }
                });
        assertEquals(0, allocated, "the run allocates nothing per step");
    }

    @Test
    void theSpecAndTheLevelCheckTheirNumbers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CruiserRunSpec(5, 4, 39, 5, new Hitbox(220, 640), 110, 370, BROADSIDE, FLAK, DEFENCE));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CruiserRunSpec(2, 4, 39, 5, new Hitbox(220, 640), 370, 110, BROADSIDE, FLAK, DEFENCE));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CruiserRunSpec.Broadside(300, 1.2, 7, 260, 6, 3, 20, 100),
                "two bands and the free band must fit");
        assertThrows(IllegalArgumentException.class, () -> new CruiserRunSpec.PointDefence(3, 170, 4, 2.5, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> LevelScript.builder(28, 4)
                        .sections(List.of(new Section(39, 200)))
                        .hazards(HazardSpec.NONE.withCruisers(Optional.of(SPEC)))
                        .build(),
                "the run ends before the level's end");
    }
}
