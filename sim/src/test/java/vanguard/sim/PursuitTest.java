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
 * M7 part G (design/campaign Level 20, The pursuit swarm; design/enemies/air/mote-swarm, {@code pursuit}): the
 * {@code pursuit} formation's leader point (band, lag, tell, dive, climb, break-off) in screen space, its TOP warning,
 * the flock contracting in the tell, the refills into the flock's free room (the left-out motes counted nowhere), the
 * survivors breaking off alive, Rook stepping out of the lunge lane, determinism and no allocation.
 */
class PursuitTest {
    private static final double TOP = PlayField.HEIGHT;
    private static final double EPSILON = 1e-9;

    /** The Mote Swarm's pursuit defaults at medium, with {@code lungeEvery} and the break-off at {@code breakAt}. */
    static WaveSpec.Pursuit block(double lungeEvery, int refill, double refillEvery, double breakAt) {
        return new WaveSpec.Pursuit(50, 150, 90, lungeEvery, 0.8, 60, 240, 140, 200, refill, refillEvery, breakAt, 200);
    }

    /** A pursuit of {@code count} motes at {@code t} with {@code block} ("top (behind)": a rear entry). */
    static WaveSpec pursuit(double t, int count, WaveSpec.Pursuit block) {
        return new WaveSpec(
                        t,
                        WaveSpec.Formation.SWARM,
                        PartDSpecs.MOTE,
                        count,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE,
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        List.of())
                .withPursuit(block);
    }

    /** A level of {@code seconds} with the scroll reversed throughout (−120 px/s, Level 20's after the turn). */
    static LevelScript reversed(double seconds, List<WaveSpec> waves) {
        return PartCSpecs.level(seconds, -120, waves, List.of());
    }

    private static Sortie sortie(LevelScript level) {
        return PartDSpecs.sortie(level);
    }

    private static Pursuit pursuit(Sortie sortie) {
        return sortie.pursuit().orElseThrow();
    }

    private static Flock flock(Sortie sortie) {
        for (int f = 0; f < sortie.flockCount(); f++) {
            if (sortie.flock(f).pursuing()) {
                return sortie.flock(f);
            }
        }
        throw new AssertionError("no pursuit in flight");
    }

    private static List<Enemy> members(Flock flock) {
        List<Enemy> members = new ArrayList<>();
        for (int i = 0; i < flock.size(); i++) {
            if (flock.member(i) != null) {
                members.add(flock.member(i));
            }
        }
        return members;
    }

    private static void runTo(Sortie sortie, double seconds, int commands) {
        while (sortie.levelTick < SimStep.ticks(seconds)) {
            sortie.step(commands);
        }
    }

    private static boolean happened(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type) > 0;
    }

    @Test
    void itsBlockCountsItsRefillsBeforeTheBreakOff() {
        // Level 20: pursuit 1 at t=24, refills at 34 … 104, drops back at 108; pursuit 2 at 124, refills 134–154, 163.
        assertEquals(8, block(6, 3, 10, 108).refills(24));
        assertEquals(3, block(6, 3, 10, 163).refills(124));
        assertEquals(0, block(6, 0, 10, 163).refills(124));
        assertEquals(1, block(6, 3, 10, 20).refills(0), "a refill due at the break-off itself does not come");
        assertThrows(IllegalArgumentException.class, () -> pursuit(5, 10, block(6, 3, 10, 4)), "breaks off after t");
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveSpec.Pursuit(150, 50, 90, 6, 0.8, 60, 240, 140, 200, 3, 10, 100, 200));
    }

    @Test
    void itEntersAtTheTopUnderATopWarningNeverFlippedAndHoldsTheBand() {
        Sortie sortie = sortie(reversed(30, List.of(pursuit(4, 10, block(6, 3, 10, 25)))));
        assertEquals(10 + 2 * 3, sortie.enemyTotal(), "10 motes and 2 refills of 3 (at t=14 and 24) planned");
        int warned = 0;
        while (sortie.levelTick < SimStep.ticks(4)) {
            sortie.step(Command.NONE);
            if (WarningEdge.TOP.in(sortie.edgeWarnings())) {
                warned++;
            }
            assertFalse(WarningEdge.BOTTOM.in(sortie.edgeWarnings()), "nothing comes from the bottom");
        }
        assertTrue(warned >= SimStep.ticks(3) - 2, "the TOP warning 3 s ahead: " + warned + " steps");

        runTo(sortie, 6, Command.NONE);
        Flock flock = flock(sortie);
        assertFalse(flock.flipped(), "the pursuit flies in screen space");
        assertEquals(10, members(flock).size());
        for (Enemy mote : members(flock)) {
            assertFalse(mote.flipped());
        }
        runTo(sortie, 6.5, Command.NONE);
        int inBand = 0;
        while (sortie.levelTick < SimStep.ticks(9.9)) {
            sortie.step(Command.NONE);
            Pursuit pursuit = pursuit(sortie);
            assertEquals(Pursuit.Phase.BAND, pursuit.phase());
            assertTrue(
                    pursuit.leaderY() <= TOP - 50 + EPSILON && pursuit.leaderY() >= TOP - 150 - EPSILON,
                    "the leader point holds the band 50–150 px below the top: " + pursuit.leaderY());
            assertEquals(sortie.ship().x(), pursuit.leaderX(), 1e-6, "over the idle ship");
            for (Enemy mote : members(flock)) {
                double toLeader = Math.hypot(mote.x() - pursuit.leaderX(), mote.y() - pursuit.leaderY());
                assertTrue(toLeader <= 2 * PartDSpecs.FLOCK.radius(), "a cloud swirling round it: " + toLeader);
            }
            inBand++;
        }
        assertTrue(inBand > SimStep.ticks(3) - 30);
    }

    @Test
    void itFollowsTheShipsXAtItsLag() {
        Sortie sortie = sortie(reversed(30, List.of(pursuit(1, 10, block(60, 3, 10, 25)))));
        runTo(sortie, 4, Command.NONE);
        double most = 90 * SimStep.SECONDS;
        int atLag = 0;
        double before = pursuit(sortie).leaderX();
        while (sortie.levelTick < SimStep.ticks(5)) {
            sortie.step(Command.RIGHT.bit());
            double x = pursuit(sortie).leaderX();
            assertTrue(x - before <= most + EPSILON, "never faster than its lag: " + (x - before));
            atLag += Math.abs(x - before - most) < EPSILON ? 1 : 0;
            before = x;
        }
        assertTrue(atLag > SimStep.ticks(0.5), "behind the ship it follows at its lag: " + atLag);
        assertTrue(sortie.ship().x() - pursuit(sortie).leaderX() > 30, "it lags behind the ship");
        runTo(sortie, 9, Command.NONE);
        assertEquals(sortie.ship().x(), pursuit(sortie).leaderX(), 1e-6, "and catches up once it stops");
    }

    @Test
    void aLungeTellsContractsThenDivesStraightDownTheLaneAndClimbsBack() {
        Sortie sortie = sortie(reversed(40, List.of(pursuit(4, 12, block(6, 3, 10, 35)))));
        Flock flock = null;
        int tellTick = -1;
        int lungeTick = -1;
        double shipX = 0;
        double shipY = 0;
        double spreadAtTell = 0;
        double spreadAtLunge = 0;
        double farthestAtLunge = 0;
        List<Integer> tells = new ArrayList<>();
        int climbTick = -1;
        int bandTick = -1;
        while (sortie.levelTick < SimStep.ticks(18)) {
            // The ship side-steps left once the tell starts: the dive does not follow it.
            boolean dodge = tellTick >= 0 && sortie.levelTick < tellTick + SimStep.ticks(1.2);
            Pursuit.Phase phase = sortie.pursuit().map(Pursuit::phase).orElse(Pursuit.Phase.BAND);
            double fromX = sortie.pursuit().map(Pursuit::leaderX).orElse(0.0);
            double fromY = sortie.pursuit().map(Pursuit::leaderY).orElse(0.0);
            double tellShipX = sortie.ship().x();
            double tellShipY = sortie.ship().y();
            sortie.step(dodge ? Command.LEFT.bit() : Command.NONE);
            if (sortie.pursuit().isEmpty()) {
                continue;
            }
            flock = flock(sortie);
            Pursuit pursuit = pursuit(sortie);
            if (happened(sortie, SimEvents.Type.PURSUIT_TELL)) {
                tells.add(sortie.levelTick);
                if (tellTick < 0) {
                    tellTick = sortie.levelTick;
                    shipX = tellShipX;
                    shipY = tellShipY;
                    spreadAtTell = spread(flock, pursuit);
                    assertEquals(Pursuit.Phase.TELL, pursuit.phase());
                    assertTrue(pursuit.lunging());
                }
            }
            if (happened(sortie, SimEvents.Type.PURSUIT_LUNGE) && lungeTick < 0) {
                lungeTick = sortie.levelTick;
                spreadAtLunge = spread(flock, pursuit);
                for (Enemy mote : members(flock)) {
                    farthestAtLunge = Math.max(
                            farthestAtLunge, Math.hypot(mote.x() - pursuit.leaderX(), mote.y() - pursuit.leaderY()));
                }
            }
            if (phase == Pursuit.Phase.TELL && pursuit.phase() == Pursuit.Phase.TELL) {
                assertEquals(fromX, pursuit.leaderX(), EPSILON, "it holds still in the tell");
                assertEquals(fromY, pursuit.leaderY(), EPSILON);
            }
            if (phase == Pursuit.Phase.DIVE && pursuit.phase() == Pursuit.Phase.DIVE) {
                double moved = Math.hypot(pursuit.leaderX() - fromX, pursuit.leaderY() - fromY);
                assertEquals(240 * SimStep.SECONDS, moved, 1e-6, "the dive at 240 px/s");
            }
            if (phase == Pursuit.Phase.DIVE && pursuit.phase() == Pursuit.Phase.CLIMB && climbTick < 0) {
                climbTick = sortie.levelTick;
                assertEquals(shipX, pursuit.laneX(), EPSILON, "the lane is the ship's x at the tell's start");
                assertEquals(shipX, pursuit.leaderX(), EPSILON, "it dives to the lane, not to the ship now");
                assertEquals(shipY - 140, pursuit.leaderY(), EPSILON, "140 px below where the ship was");
                assertTrue(Math.abs(sortie.ship().x() - shipX) > 60, "the ship had side-stepped");
            }
            if (phase == Pursuit.Phase.CLIMB && pursuit.phase() == Pursuit.Phase.CLIMB) {
                assertEquals(200 * SimStep.SECONDS, pursuit.leaderY() - fromY, 1e-6, "the climb at 200 px/s");
            }
            if (phase == Pursuit.Phase.CLIMB && pursuit.phase() == Pursuit.Phase.BAND && bandTick < 0) {
                bandTick = sortie.levelTick;
                assertTrue(pursuit.leaderY() >= TOP - 150 - EPSILON, "back in the band");
            }
        }
        assertEquals(SimStep.ticks(10), tellTick, 2, "the first tell 6 s after the entry");
        assertEquals(SimStep.ticks(0.8), lungeTick - tellTick, "the dive after the 0.8 s tell");
        assertTrue(spreadAtLunge < spreadAtTell, "the flock contracts: " + spreadAtTell + " → " + spreadAtLunge);
        assertTrue(farthestAtLunge <= 60, "into a ball about 60 px across: " + farthestAtLunge);
        assertTrue(climbTick > lungeTick && bandTick > climbTick);
        assertTrue(tells.size() >= 2);
        assertEquals(SimStep.ticks(6), tells.get(1) - tells.get(0), "every 6 s from one tell to the next");
    }

    /** The members' mean distance from the leader point. */
    private static double spread(Flock flock, Pursuit pursuit) {
        return members(flock).stream()
                .mapToDouble(m -> Math.hypot(m.x() - pursuit.leaderX(), m.y() - pursuit.leaderY()))
                .average()
                .orElseThrow();
    }

    @Test
    void refillsFillOnlyTheFlocksRoomAndTheLeftOutMotesCountNowhere() {
        // 10 + 8 refills of 3 planned (34); no lunges, no fire: the flock fills to 24, 10 motes are left out.
        Sortie sortie = sortie(reversed(100, List.of(pursuit(2, 10, block(500, 3, 10, 90)))));
        assertEquals(34, sortie.enemyTotal(), "every planned mote counts in the density at first");
        assertEquals(28, sortie.requiredKills(), "80 % of 34");
        int most = 0;
        List<Integer> sizes = new ArrayList<>();
        while (sortie.levelTick < SimStep.ticks(88)) {
            sortie.step(Command.NONE);
            if (sortie.pursuit().isPresent()) {
                int alive = members(flock(sortie)).size();
                most = Math.max(most, alive);
                if (sortie.levelTick % SimStep.ticks(10) == SimStep.ticks(2) + 1) {
                    sizes.add(alive);
                }
            }
            assertTrue(PartDSpecs.all(sortie, "mote-swarm").size() <= Flock.MAX_MEMBERS);
        }
        assertEquals(List.of(10, 13, 16, 19, 22, 24, 24, 24, 24), sizes.subList(0, 9));
        assertEquals(Flock.MAX_MEMBERS, most);
        assertEquals(24, sortie.enemyTotal(), "the 10 left-out motes count nowhere");
        assertEquals(20, sortie.requiredKills(), "the kill ratio counts the 24 that entered");
    }

    @Test
    void theSurvivorsBreakOffAliveOffTheTop() {
        Sortie sortie = sortie(reversed(100, List.of(pursuit(2, 10, block(500, 3, 10, 30)))));
        int breakTick = -1;
        while (sortie.levelTick < SimStep.ticks(42)) {
            sortie.step(Command.NONE);
            if (happened(sortie, SimEvents.Type.PURSUIT_BREAK)) {
                assertEquals(-1, breakTick, "it breaks off once");
                breakTick = sortie.levelTick;
                assertEquals(Pursuit.Phase.BREAK, pursuit(sortie).phase());
            }
            if (breakTick >= 0 && sortie.pursuit().isPresent()) {
                for (Enemy mote : members(flock(sortie))) {
                    assertTrue(mote.y() > TOP - 260, "they climb away: " + mote.y());
                }
            }
        }
        assertEquals(SimStep.ticks(30), breakTick, 1);
        assertEquals(0, PartDSpecs.all(sortie, "mote-swarm").size(), "gone off the top");
        assertEquals(0, sortie.flockCount());
        assertTrue(sortie.pursuit().isEmpty());
        assertEquals(0, sortie.kills(), "not killed");
        assertEquals(10 + 2 * 3, sortie.enemyTotal(), "they count as units the player did not kill");
    }

    @Test
    void aThinnedFlockStillGetsItsRefills() {
        // Two motes over the firing ship die soon; the pursuit lives on empty until its next refill joins.
        Sortie sortie = sortie(reversed(60, List.of(pursuit(2, 2, block(500, 2, 10, 50)))));
        boolean emptyButAlive = false;
        boolean refilledAfter = false;
        while (sortie.levelTick < SimStep.ticks(24)) {
            sortie.step(Command.FIRE.bit());
            if (sortie.pursuit().isEmpty()) {
                continue;
            }
            int alive = members(flock(sortie)).size();
            if (alive == 0 && sortie.levelTick > SimStep.ticks(3)) {
                emptyButAlive = true;
            } else if (alive > 0 && emptyButAlive) {
                refilledAfter = true;
            }
        }
        assertTrue(emptyButAlive, "the pursuit outlives its motes while refills are due");
        assertTrue(refilledAfter, "a refill joins a thinned flock");
        assertTrue(sortie.kills() >= 4, "the refills are ordinary motes: " + sortie.kills());
    }

    @Test
    void rookStepsOutOfTheLungeLane() {
        // Rook flies on the right (64 px out). Once the tell starts the ship side-steps 64 px left, which would put his
        // slot in the lunge's lane: he keeps out of it until the dive is over.
        LevelScript level = reversed(40, List.of(pursuit(2, 10, block(6, 3, 10, 35))));
        Sortie sortie = WingmanTest.sortie(level, WingmanSpec.Side.RIGHT);
        int tellTick = -1;
        double laneX = 0;
        int checked = 0;
        boolean slotInLane = false;
        while (sortie.levelTick < SimStep.ticks(16)) {
            boolean dodge = tellTick >= 0 && sortie.ship().x() > laneX - 64;
            sortie.step(dodge ? Command.LEFT.bit() : Command.NONE);
            if (happened(sortie, SimEvents.Type.PURSUIT_TELL) && tellTick < 0) {
                tellTick = sortie.levelTick;
                laneX = pursuit(sortie).laneX();
            }
            Optional<Pursuit> chase = sortie.pursuit();
            if (tellTick < 0 || chase.isEmpty() || chase.get().phase() != Pursuit.Phase.DIVE) {
                continue;
            }
            Wingman rook = sortie.wingman().orElseThrow();
            double half = WingmanTest.CRAFT.hitbox().width() / 2;
            double slotX = sortie.ship().x() + 64;
            slotInLane |= slotX + half > chase.get().laneLeft()
                    && slotX - half < chase.get().laneRight();
            assertTrue(
                    rook.x() + half <= chase.get().laneLeft()
                            || rook.x() - half >= chase.get().laneRight(),
                    "Rook out of the lane during the dive: " + rook.x() + " vs "
                            + chase.get().laneLeft() + "–" + chase.get().laneRight());
            checked++;
        }
        assertTrue(checked > 10, "the dive was checked: " + checked);
        assertTrue(slotInLane, "his slot lay in the lane: he stepped out of it");
    }

    @Test
    void theSameSeedFliesTheSamePursuit() {
        LevelScript level = reversed(40, List.of(pursuit(2, 12, block(3, 3, 4, 30))));
        Sortie a = WingmanTest.sortie(level, WingmanSpec.Side.LEFT);
        Sortie b = WingmanTest.sortie(level, WingmanSpec.Side.LEFT);
        for (int step = 0; step < SimStep.ticks(34); step++) {
            int commands = Command.FIRE.bit() | (step % 150 < 75 ? Command.LEFT.bit() : Command.RIGHT.bit());
            a.step(commands);
            b.step(commands);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
    }

    @Test
    void aPursuitDoesNotAllocate() {
        LevelScript level = reversed(40, List.of(pursuit(1, 12, block(3, 3, 2, 14))));
        long allocated = Allocations.least(() -> WingmanTest.sortie(level, WingmanSpec.Side.RIGHT), sortie -> {
            for (int step = 0; step < SimStep.ticks(18); step++) {
                sortie.step(
                        step % 120 < 60
                                ? Command.FIRE.bit() | Command.LEFT.bit()
                                : Command.FIRE.bit() | Command.RIGHT.bit());
            }
        });

        assertEquals(0, allocated);
    }
}
