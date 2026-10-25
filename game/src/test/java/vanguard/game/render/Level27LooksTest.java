package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.HazardsBlock;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.FurnaceVents;
import vanguard.sim.Missile;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G's Level 27 looks (design/campaign Level 27; tools/art/depth_hunter.py, relay_beacon.py, l27_props.py): the
 * art's frames as the looks count them; the heat plumes' mouth, tell, eruption, settling and pool by their phase; the
 * vent field lit but not black water (its units keep their plain looks and sounds); the Depth Hunter's heading,
 * flicker, rise and torpedoes; the relay beacons' pulse; flown: the Hunters hold facing up and launch their torpedoes up
 * the screen from their centres, the plumes bend shots at their burning discs.
 */
class Level27LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = VentFieldFixture.CONTENT;

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static LevelData level(int number) {
        return CONTENT.levels().get(CONTENT.levelKey(number).orElseThrow());
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        String hunter = HunterLooks.SLUG;
        int frames = HunterLooks.HEADINGS * HunterLooks.FLICKERS;
        assertEquals(frames, frames(hunter), "heading × flicker");
        assertEquals(frames, frames(hunter + "-glow"));
        assertEquals(frames, frames(hunter + "-torpedo"), "the torpedo's heading × motor");
        assertEquals(frames, frames(hunter + "-torpedo-glow"));
        assertEquals(6, frames(hunter + "-wake"));
        assertEquals(6, frames(hunter + "-launch"));
        assertEquals(8, frames(hunter + "-break"));
        assertEquals(8, frames(hunter + "-oil"));
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + hunter + ".json")));
        assertEquals(HunterLooks.FRAME, pivots.get("frame").getInt(0));
        assertEquals(HunterLooks.HEADINGS, pivots.get("tubes").size, "two tubes per heading");
        assertEquals(2, pivots.get("tubes").get(8).size);
        String beacon = BeaconLooks.SLUG;
        assertEquals(16, frames(beacon), "the 4 s pulse");
        assertEquals(16, frames(beacon + "-glow"));
        assertEquals(8, frames(beacon + "-break"));
        assertTrue(Files.isRegularFile(SPRITES.resolve(beacon + "-dead.png")), "the dark mast");
        JsonValue mast = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + beacon + ".json")));
        assertEquals(BeaconLooks.FRAME_WIDTH, mast.get("frame").getInt(0));
        assertEquals(BeaconLooks.FRAME_HEIGHT, mast.get("frame").getInt(1));
        String plume = HazardsBlock.Vents.HEAT_PLUME;
        assertEquals(PlumeLooks.MOUTH_FRAMES, frames(plume), "cold and the tell's three steps");
        assertEquals(PlumeLooks.GLOW_FRAMES, frames(plume + "-glow"));
        assertEquals(8, frames(plume + "-silt"));
        assertEquals(PlumeLooks.COLUMN_FRAMES, frames(plume + "-column"));
        assertEquals(PlumeLooks.COLUMN_FRAMES, frames(plume + "-heat"), "the column's clock");
        assertEquals(6, frames(plume + "-settle"));
        assertEquals(6, frames(plume + "-bend"));
        assertEquals(3, frames("marlin-hauler"), "lying, hit, opened");
        assertEquals(8, frames("marlin-hauler-glow"), "the stern lamp's half and the resin's");
    }

    /** Level 27's vents are heat plumes (Level 22's geysers and Level 19's vents are not). */
    @Test
    void onlyLevel27sVentsArePlumes() {
        assertTrue(PlumeLooks.plumes(VentFieldFixture.level()));
        assertFalse(PlumeLooks.plumes(level(22)));
        assertFalse(PlumeLooks.plumes(level(19)));
        assertEquals(HazardsBlock.Vents.HEAT_PLUME, FoundryLooks.ventLook(VentFieldFixture.level()));
    }

    /**
     * The vent field has static lights (the smokers' amber pools, the gate's teal) but is lit (45–62 %): not black
     * water, so its units keep their plain looks and the dark's sounds stay off (gap check §5 G10); Level 26 is black
     * water.
     */
    @Test
    void theVentFieldIsLitNotBlackWater() {
        LevelData vents = VentFieldFixture.level();
        assertTrue(DarkLight.dark(vents), "its light pools are drawn");
        assertFalse(DarkLight.blackWater(vents));
        assertFalse(LevelRenderer.darkLevel(vents));
        assertTrue(DarkLight.blackWater(level(26)));
        assertTrue(LevelRenderer.darkLevel(level(26)));
        assertFalse(DarkLight.blackWater(level(25)), "no lights");
    }

    @Test
    void thePlumesMouthTellEruptionAndPoolFollowTheirPhase() {
        assertEquals(0, PlumeLooks.mouthFrame(FurnaceVents.Phase.IDLE, 0));
        assertEquals(1, PlumeLooks.mouthFrame(FurnaceVents.Phase.TELL, 0.1));
        assertEquals(2, PlumeLooks.mouthFrame(FurnaceVents.Phase.TELL, 0.5));
        assertEquals(3, PlumeLooks.mouthFrame(FurnaceVents.Phase.TELL, 0.99));
        assertEquals(3, PlumeLooks.mouthFrame(FurnaceVents.Phase.BURN, 0.5), "the last step under the eruption");
        assertEquals(0, PlumeLooks.columnFrame(0));
        assertEquals(8, PlumeLooks.columnFrame(0.5));
        assertEquals(15, PlumeLooks.columnFrame(1), "its 16 frames over the burn");
        assertEquals(0, PlumeLooks.settleFrame(0, 6));
        assertEquals(5, PlumeLooks.settleFrame(SimStep.ticks(5.5 / PlumeLooks.SETTLE_FPS), 6));
        assertEquals(-1, PlumeLooks.settleFrame(SimStep.ticks(6.5 / PlumeLooks.SETTLE_FPS), 6), "settled");
        assertEquals(0, PlumeLooks.poolLight(FurnaceVents.Phase.IDLE, 0), "cold: no pool");
        float early = PlumeLooks.poolLight(FurnaceVents.Phase.TELL, 0.1f);
        float late = PlumeLooks.poolLight(FurnaceVents.Phase.TELL, 0.9f);
        assertTrue(early > 0 && late > early, "the pool rises through the tell");
        assertEquals(PlumeLooks.BURN_LIGHT, PlumeLooks.poolLight(FurnaceVents.Phase.BURN, 0.3), 1e-6);
        assertTrue(PlumeLooks.poolLight(FurnaceVents.Phase.BURN, 1) < PlumeLooks.BURN_LIGHT, "fading at its end");
        assertTrue(PlumeLooks.poolLight(FurnaceVents.Phase.BURN, 0.3) > late, "brightest while it burns");
    }

    @Test
    void theHunterHoldsFacingUpAndRisesOutOfTheDeep() {
        assertEquals(8, HunterLooks.heading(Math.PI), "facing up is heading 8");
        assertEquals(0, HunterLooks.heading(0), "straight down is heading 0");
        assertEquals(HunterLooks.RISE_FROM, HunterLooks.riseOpacity(0), 1e-6);
        assertTrue(HunterLooks.riseOpacity(0.6) > HunterLooks.RISE_FROM && HunterLooks.riseOpacity(0.6) < 1);
        assertEquals(1, HunterLooks.riseOpacity(HunterLooks.RISE_SECONDS), 1e-6);
        // The nozzle flickers twice as fast while it moves as while it holds.
        int movingChanges = 0;
        int holdingChanges = 0;
        for (long tick = 1; tick <= SimStep.PER_SECOND; tick++) {
            movingChanges += HunterLooks.flicker(tick, 0, true) != HunterLooks.flicker(tick - 1, 0, true) ? 1 : 0;
            holdingChanges += HunterLooks.flicker(tick, 0, false) != HunterLooks.flicker(tick - 1, 0, false) ? 1 : 0;
        }
        assertEquals(8, movingChanges);
        assertEquals(4, holdingChanges);
        assertEquals(0, BeaconLooks.pulseFrame(0, 16));
        assertEquals(1, BeaconLooks.pulseFrame(SimStep.ticks(0.25), 16));
        assertEquals(0, BeaconLooks.pulseFrame(SimStep.ticks(4), 16), "the 4 s pulse");
    }

    /**
     * Flown at medium with the Bulwark (invulnerable): a Hunter holding faces up (heading 8; turning to leave it is still a
     * few steps), each torpedo leaves at
     * a Hunter's centre (where the launch puff finds its tube) and flies up the screen at first, and every bend lies on a
     * burning plume's disc (where its ripple plays).
     */
    @Test
    void theHuntersLaunchUpAndThePlumesBendAtTheirDiscs() {
        Sortie sortie = VentFieldFixture.sortie(true);
        int holds = 0;
        int upward = 0;
        int launches = 0;
        int bends = 0;
        while (!sortie.complete() && sortie.levelSeconds() < 140) {
            sortie.step(IceShelfFixture.toward(sortie, 240, 110));
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().slug().equals(HunterLooks.SLUG)
                        && Math.abs(enemy.renderY(1) - enemy.renderY(0)) <= HunterLooks.MOVING_STEP
                        && enemy.renderY(1) > 0) {
                    holds++;
                    if (HunterLooks.heading(enemy.facing()) == 8) {
                        upward++;
                    }
                }
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MISSILE_LAUNCHED) {
                    launches++;
                    assertTrue(hunterAt(sortie, events.x(e), events.y(e)), "at a Hunter's centre");
                    Missile torpedo = newest(sortie, events.value(e));
                    if (torpedo != null) {
                        assertEquals(8, HunterLooks.heading(torpedo.heading()), "launched up the screen");
                    }
                } else if (events.type(e) == SimEvents.Type.PLUME_BEND) {
                    bends++;
                    assertTrue(onBurningDisc(sortie, events.x(e), events.y(e), events.value(e)), "on its disc");
                }
            }
        }
        assertTrue(holds > 0, "the Hunters hold");
        // Still but turning (to leave downward) a few steps; holding they face up.
        assertTrue(upward > 0.9 * holds, "holding, facing up: " + upward + " of " + holds);
        assertTrue(launches > 0, "they launch");
        assertTrue(bends > 0, "the plumes bend shots");
    }

    private static boolean hunterAt(Sortie sortie, double x, double y) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals(HunterLooks.SLUG)
                    && Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y) < 6) {
                return true;
            }
        }
        return false;
    }

    private static Missile newest(Sortie sortie, int serial) {
        for (int m = 0; m < sortie.missileCount(); m++) {
            if (sortie.missile(m).serial() == serial) {
                return sortie.missile(m);
            }
        }
        return null;
    }

    private static boolean onBurningDisc(Sortie sortie, double x, double y, int grate) {
        FurnaceVents vents = sortie.vents();
        return grate >= 0
                && grate < vents.grates()
                && vents.phase(grate) == FurnaceVents.Phase.BURN
                && Math.hypot(vents.x(grate) - x, vents.y(grate) - y) <= vents.radius(grate) + 12;
    }
}
