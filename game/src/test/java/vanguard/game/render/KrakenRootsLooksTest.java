package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.SlamArena;
import vanguard.sim.Sortie;

/**
 * Round 40 (the reworked Harbour Kraken, proposal 3a, the r40 a look; pending the user's pick): the rooted
 * arms' looks on {@link RoundFortyFixture}'s hand-built Kraken: the look is chosen only for rooted arms, the
 * sprites and pivots are what the looks read, the sockets rear and glow while their arm is raised, flash at a
 * hit, recoil and ooze once cut and go under at the death, and a rooted arm runs from its socket to its lane
 * and down it, with the lanes' churn, marks and splash from the roots' lower edge.
 */
class KrakenRootsLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    private static JsonValue r40() {
        JsonValue pivots = new JsonReader()
                .parse(new FileHandle(
                        ASSETS.resolve("pivots/harbour-kraken.json").toFile()));
        return pivots.get(KrakenLooks.R40);
    }

    @Test
    void onlyARootedKrakenGetsTheRoundFortyLook() {
        assertTrue(KrakenLooks.rooted(RoundFortyFixture.kraken()));
        // Level 11's data is rooted since step CB (2026-10-19): the level's Kraken takes the round 40 look.
        var levelFight = ArenaFixture.level().setPieces().getFirst();
        assertTrue(KrakenLooks.rooted(levelFight), "Level 11's Kraken has the rooted arms");
    }

    @Test
    void theSocketSpritesAndPivotsAreWhatTheLooksRead() {
        JsonValue r40 = r40();
        JsonValue frames = r40.get("root_frames");
        assertEquals(KrakenLooks.ROOT_FRAMES, frames.getInt("root"));
        assertEquals(KrakenLooks.ROOT_RAISED_FRAMES, frames.getInt("root-raised"));
        assertEquals(1, frames.getInt("root-hit"));
        assertEquals(KrakenLooks.ROOT_CUT_FRAMES, frames.getInt("root-cut"));
        assertEquals(KrakenLooks.ROOT_SINK_FRAMES, frames.getInt("root-sink"));
        JsonValue steps = r40.get("steps");
        assertEquals(KrakenLooks.ROOT_TICKS, steps.getInt("root"));
        assertEquals(KrakenLooks.RAISED_TICKS, steps.getInt("raised"));
        assertEquals(KrakenLooks.RAISED_HOLD_TICKS, steps.getInt("raised_hold"));
        assertEquals(KrakenLooks.CUT_TICKS, steps.getInt("cut"));
        assertEquals(KrakenLooks.OOZE_TICKS, steps.getInt("ooze"));
        assertEquals(KrakenLooks.ROOT_SINK_TICKS, steps.getInt("root_sink"));
        assertEquals(KrakenLooks.GLOW_TICKS, steps.getInt("glow"));
        assertEquals(KrakenLooks.REACH_TICKS, steps.getInt("reach"));
        JsonValue sprites = r40.get("sprites");
        assertEquals(KrakenLooks.ROOT_SPRITE, sprites.getString("root"));
        assertFrames(sprites.getString("root"), 4 * KrakenLooks.ROOT_FRAMES);
        assertFrames(sprites.getString("root-hit"), 4);
        assertFrames(sprites.getString("root-raised"), 4 * KrakenLooks.ROOT_RAISED_FRAMES);
        assertFrames(sprites.getString("root-cut"), 4 * KrakenLooks.ROOT_CUT_FRAMES);
        assertFrames(sprites.getString("root-sink"), 4 * KrakenLooks.ROOT_SINK_FRAMES);
        assertFrames(sprites.getString("root-glow"), KrakenLooks.ROOT_GLOW_FRAMES);
        assertFrames(sprites.getString("reach"), 4);
        // A lane's arm turns down its lane 34 px below its socket (socket y 210, lane centre y 244).
        assertEquals(34, KrakenLooks.laneDrop(r40), 1e-6);
        // The fixture's roots sit on the pivots' sockets: the contract for the reworked data.
        JsonValue roots = r40.get("roots");
        for (int lane = 1; lane <= 4; lane++) {
            JsonValue socket = roots.get(lane - 1).get("socket");
            assertEquals(socket.getFloat(0), RoundFortyFixture.SOCKET_DX[lane - 1], 1e-6, "lane " + lane);
            assertEquals(-socket.getFloat(1), RoundFortyFixture.SOCKET_DY, 1e-6);
        }
    }

    private static void assertFrames(String name, int count) {
        for (int i = 0; i < count; i++) {
            assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/" + name + "_" + i + ".png")), name + " " + i);
        }
        assertFalse(Files.exists(ASSETS.resolve("sprites/" + name + "_" + count + ".png")), name + ": " + count);
    }

    @Test
    void aSocketLoopsRearsOnceAndHoldsRecoilsAndOozesAndGoesUnder() {
        assertEquals(KrakenLooks.RootLook.INTACT, KrakenLooks.rootLook(false, false, false, false));
        assertEquals(KrakenLooks.RootLook.HIT, KrakenLooks.rootLook(false, false, false, true));
        assertEquals(
                KrakenLooks.RootLook.RAISED,
                KrakenLooks.rootLook(false, false, true, true),
                "a raised hit flashes its rear");
        assertEquals(KrakenLooks.RootLook.CUT, KrakenLooks.rootLook(false, true, false, false));
        assertEquals(KrakenLooks.RootLook.SINK, KrakenLooks.rootLook(true, false, false, false));
        assertEquals(
                KrakenLooks.RootLook.CUT, KrakenLooks.rootLook(true, true, false, false), "a stump keeps its ooze");
        for (long tick = 0; tick < 200; tick++) {
            int frame = KrakenLooks.rootFrame(KrakenLooks.RootLook.INTACT, 0, tick);
            assertEquals(tick / KrakenLooks.ROOT_TICKS % 4, frame);
        }
        // The rear: frames 0-3 once, then 4-5 in a loop until the arm has sunk.
        assertEquals(0, KrakenLooks.rootFrame(KrakenLooks.RootLook.RAISED, 0, 0));
        assertEquals(3, KrakenLooks.rootFrame(KrakenLooks.RootLook.RAISED, 4L * KrakenLooks.RAISED_TICKS - 1, 0));
        for (long since = 4L * KrakenLooks.RAISED_TICKS; since < 300; since++) {
            int frame = KrakenLooks.rootFrame(KrakenLooks.RootLook.RAISED, since, 0);
            assertTrue(frame == 4 || frame == 5, "the hold: " + frame);
        }
        // The cut: the recoil's two frames once, then the ooze's four in a loop.
        assertEquals(0, KrakenLooks.rootFrame(KrakenLooks.RootLook.CUT, 0, 0));
        assertEquals(1, KrakenLooks.rootFrame(KrakenLooks.RootLook.CUT, KrakenLooks.CUT_TICKS, 0));
        for (long since = 2L * KrakenLooks.CUT_TICKS; since < 300; since++) {
            int frame = KrakenLooks.rootFrame(KrakenLooks.RootLook.CUT, since, 0);
            assertTrue(frame >= 2 && frame <= 5, "the ooze: " + frame);
        }
        // The death: the sink's five once, then gone.
        assertEquals(4, KrakenLooks.rootFrame(KrakenLooks.RootLook.SINK, 5L * KrakenLooks.ROOT_SINK_TICKS - 1, 0));
        assertEquals(-1, KrakenLooks.rootFrame(KrakenLooks.RootLook.SINK, 5L * KrakenLooks.ROOT_SINK_TICKS, 0));
        // (lane - 1) × frames + frame.
        assertEquals(0, KrakenLooks.rootIndex(1, KrakenLooks.ROOT_FRAMES, 0));
        assertEquals(
                3 * KrakenLooks.ROOT_RAISED_FRAMES + 5, KrakenLooks.rootIndex(4, KrakenLooks.ROOT_RAISED_FRAMES, 5));
        assertEquals(2, KrakenLooks.glowFrame(2L * KrakenLooks.GLOW_TICKS));
        assertEquals(0, KrakenLooks.glowFrame(4L * KrakenLooks.GLOW_TICKS), "a pulse loop");
    }

    @Test
    void aRaisedArmsSocketGlowsAndItsArmRunsFromTheSocketDownItsLane() {
        Sortie sortie = RoundFortyFixture.kraken(1);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 6);
        SetPiece boss = sortie.setPiece(0);
        SlamArena arena = boss.arena().orElseThrow();
        assertTrue(arena.rooted());
        int lane = until(sortie, SimEvents.Type.TELEGRAPH, 2);
        int arm = lane - 1;
        assertEquals(1 << lane, arena.armLanes(arm), "the root over lane n holds arm n - 1");
        assertTrue(arena.armRaised(arm), "raised from its telegraph: it rears and glows");
        assertEquals(KrakenLooks.RootLook.RAISED, KrakenLooks.rootLook(false, false, arena.armRaised(arm), false));
        assertEquals(SlamArena.ArmState.TELEGRAPH, arena.armState(arm), "not drawn yet: its root is the tell");
        // The lanes' churn, marks and splash start at the root's lower edge, 20 px below its socket.
        double socketY = boss.renderY(1) + RoundFortyFixture.SOCKET_DY;
        assertEquals(socketY - 20, KrakenLooks.laneTop(arena, lane), 1e-6);
        for (int other = 1; other <= 4; other++) {
            assertEquals(boss.partY(other) - 20, KrakenLooks.laneTop(arena, other), 1e-6, "lane " + other);
        }
        // Its path: the socket, the lane's centre 34 px below it, down the lane to the tip, in its lane.
        float[] xs = new float[1 + KrakenLooks.LANE_POINTS];
        float[] ys = new float[1 + KrakenLooks.LANE_POINTS];
        double socketX = boss.renderX(1) + RoundFortyFixture.SOCKET_DX[arm];
        double laneX = arena.laneWidth() * (lane - 0.5);
        double sign = KrakenLooks.rootedSign(lane, arena.laneCount());
        for (double w = 0; w <= 2.5; w += 0.25) {
            KrakenLooks.rootedPath(socketX, socketY, laneX, socketY - 34, w, sign, xs, ys);
            assertEquals(socketX, xs[0], 1e-3);
            assertEquals(socketY, ys[0], 1e-3);
            assertEquals(laneX, xs[1], 1e-3, "the lane's centre where it turns down");
            assertEquals(socketY - 34, ys[1], 1e-3);
            assertEquals(SlamArena.TIP_MARGIN, ys[KrakenLooks.LANE_POINTS], 1e-3, "the tip");
            for (int i = 1; i < xs.length; i++) {
                assertTrue(Math.abs(xs[i] - laneX) < arena.laneWidth() / 2, "inside its lane");
            }
        }
        assertEquals(1, KrakenLooks.rootedSign(2, 4), 1e-9);
        assertEquals(-1, KrakenLooks.rootedSign(3, 4), 1e-9, "the right half mirrored");
        // It rises, lies awash and sinks; its second half under the water, fading into the deep.
        steps(sortie, RoundFortyFixture.TELEGRAPH + 0.05);
        assertEquals(SlamArena.ArmState.RISE, arena.armState(arm));
        assertTrue(arena.armRaised(arm), "still raised while it slams");
        while (arena.armState(arm) != SlamArena.ArmState.SINK) {
            sortie.step(0);
        }
        float last = 1;
        while (arena.armState(arm) == SlamArena.ArmState.SINK) {
            float fade = KrakenLooks.sinkingFade(arena.armState(arm), arena.armShare(arm));
            assertTrue(fade <= last + 1e-6, "fading");
            last = fade;
            sortie.step(0);
        }
        assertTrue(last < 0.1, "into the deep: " + last);
        assertFalse(arena.armRaised(arm), "at rest again: intact");
    }

    @Test
    void aShotRootIsARootHitAndACutRootsArmIsGone() {
        Sortie sortie = RoundFortyFixture.kraken(1);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 6);
        SetPiece boss = sortie.setPiece(0);
        int root = 3;
        double x = boss.partX(root);
        while (sortie.ship().x() < x - 1) {
            sortie.step(vanguard.sim.Command.RIGHT.bit());
        }
        int hit = untilFiring(sortie, SimEvents.Type.ROOT_HIT, 3);
        assertEquals(root, hit);
        assertEquals(0, boss.partTicksSinceHit(root), "its flash: the hit frame");
        assertEquals(root, untilFiring(sortie, SimEvents.Type.PART_DESTROYED, 30), "cut by the stream");
        assertTrue(boss.partWrecked(root), "cut: its stump recoils and oozes, its arm is not drawn");
        assertEquals(KrakenLooks.RootLook.CUT, KrakenLooks.rootLook(false, boss.partWrecked(root), false, false));
    }

    private static int until(Sortie sortie, SimEvents.Type type, double seconds) {
        return until(sortie, type, seconds, 0);
    }

    private static int untilFiring(Sortie sortie, SimEvents.Type type, double seconds) {
        return until(sortie, type, seconds, vanguard.sim.Command.FIRE.bit());
    }

    private static int until(Sortie sortie, SimEvents.Type type, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == type) {
                    return events.value(e);
                }
            }
        }
        throw new AssertionError(type + " within " + seconds + " s");
    }

    private static void steps(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }
}
