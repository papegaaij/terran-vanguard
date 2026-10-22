package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part B's Level 22 looks (design/campaign Level 22; tools/art/drill_rig.py and l22_props.py): the ice geysers drawn
 * from the vents' {@code look} (crack, glow, steam, residue), the Drill Rig's collar, pumps, decals, steam and muzzle
 * as the art gives them, its ring foreseen from the last one for the muzzle's tell, and Conamara's ice-crawler carrying
 * the armour patch up the road and back.
 */
class Level22LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static JsonValue pivots(String name) throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + name + ".json")));
    }

    @Test
    void theGeysersDrawTheIceGeyserSpritesAndTheFurnaceItsOwn() throws IOException {
        assertEquals("ice-geyser", FoundryLooks.ventLook(IceShelfFixture.level()));
        assertEquals("furnace-vent", FoundryLooks.ventLook(FoundryFixture.level()), "Level 19 names no look");
        assertArrayEquals(
                new String[] {"ice-geyser", "ice-geyser-glow", "ice-geyser-steam", "ice-geyser-residue"},
                FoundryLooks.ventSprites("ice-geyser"));
        assertArrayEquals(
                new String[] {"furnace-vent", "furnace-vent-glow", "furnace-vent-flame", "furnace-vent-smoke"},
                FoundryLooks.ventSprites("furnace-vent"));
        assertTrue(FoundryLooks.isGeyser("ice-geyser"), "steam, normal blending");
        // The art's frames as the looks count them: the crack's dormant state and 3 tell steps, the glow's 3 steps,
        // the steam's 8 over the burn, the residue's 6 at 8 fps.
        assertEquals(FoundryLooks.GRATE_FRAMES, frames("ice-geyser"));
        assertEquals(FoundryLooks.GLOW_FRAMES, frames("ice-geyser-glow"));
        assertEquals(FoundryLooks.FLAME_FRAMES, frames("ice-geyser-steam"));
        assertEquals(FoundryLooks.SMOKE_FRAMES, frames("ice-geyser-residue"));
    }

    @Test
    void theRigsArtIsWhatItsLooksDraw() throws IOException {
        JsonValue pivot = pivots(DrillRigLooks.SLUG);
        assertEquals(pivot.get("spin").getInt("frames"), frames("drill-rig"), "the collar's loop");
        assertEquals(frames("drill-rig"), frames("drill-rig-glow"), "the glow indexed like the collar");
        assertEquals(EnemyLooks.RIG_FPS, pivot.get("spin").getDouble("fps"), "12 fps: 180 degrees/s");
        assertEquals(4, frames("drill-rig-pump"));
        assertEquals(8, frames("drill-rig-steam"));
        assertEquals(DrillRigLooks.TELL_FRAMES + DrillRigLooks.FLASH_FRAMES, frames("drill-rig-muzzle"));
        for (String single : List.of("drill-rig-pump-dead", "drill-rig-base", "drill-rig-crack", "drill-rig-stump")) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(single + ".png")), single);
        }
        assertEquals(32, DrillRigLooks.steamAboveBore(pivot), "the bore at (64, 112) of the 128x160 frame");
        assertEquals(DrillRigLooks.STEAM_ABOVE_BORE, DrillRigLooks.steamAboveBore(null));
        // The pumps stand where the stat block puts its parts: the pivot file's offsets (dx right, dy up).
        EnemySpec rig = IceShelfFixture.script(vanguard.content.Difficulty.MEDIUM).groundUnits().stream()
                .map(unit -> unit.enemy())
                .filter(spec -> spec.slug().equals(DrillRigLooks.SLUG))
                .findFirst()
                .orElseThrow();
        JsonValue offsets = pivot.get("pumps").get("offsets");
        assertEquals(offsets.size, rig.parts().size());
        for (int p = 0; p < rig.parts().size(); p++) {
            assertEquals(offsets.get(p).getDouble(0), rig.parts().get(p).dx(), "pump " + p + " dx");
            assertEquals(offsets.get(p).getDouble(1), rig.parts().get(p).dy(), "pump " + p + " dy");
        }
    }

    @Test
    void theMuzzleGathersOverTheTellThenFlashes() {
        long ring = 1000;
        assertEquals(-1, DrillRigLooks.muzzleFrame(ring - DrillRigLooks.TELL_TICKS - 1, Long.MIN_VALUE / 2, ring));
        assertEquals(0, DrillRigLooks.muzzleFrame(ring - DrillRigLooks.TELL_TICKS, Long.MIN_VALUE / 2, ring));
        assertEquals(3, DrillRigLooks.muzzleFrame(ring - 1, Long.MIN_VALUE / 2, ring), "the last tell step");
        assertEquals(4, DrillRigLooks.muzzleFrame(ring, ring, ring + 210), "the release flash");
        assertEquals(5, DrillRigLooks.muzzleFrame(ring + DrillRigLooks.FLASH_FRAME_TICKS, ring, ring + 210));
        assertEquals(-1, DrillRigLooks.muzzleFrame(ring + 2 * DrillRigLooks.FLASH_FRAME_TICKS, ring, ring + 210));
        assertEquals(-1, DrillRigLooks.muzzleFrame(ring - 5, Long.MIN_VALUE / 2, Long.MIN_VALUE / 2), "a silent rig");
        for (int p = 0; p < 4; p++) {
            assertEquals(p, DrillRigLooks.pumpFrame(0, p, 4), "the pumps heave a frame apart");
        }
        assertEquals(1, DrillRigLooks.pumpFrame(SimStep.ticks(1 / DrillRigLooks.PUMP_FPS), 0, 4));
        assertEquals(1, DrillRigLooks.steamFrame(SimStep.ticks(1 / DrillRigLooks.STEAM_FPS), 8));
    }

    /**
     * Flown under the first rig and then behind it: each ring comes exactly when the looks foresee it, the first its
     * first shot delay after the rig came in sight, each next one the stat block's 3.5 s (0.5 s longer for each pump lost
     * by then) after the one before, waiting a step for each step the ship is outside the gun's arc (behind the rig),
     * so the muzzle's tell leads the real ring.
     */
    @Test
    void eachRingComesWhenTheLooksForeseeIt() {
        Sortie sortie = IceShelfFixture.sortie();
        int checked = 0;
        long foreseen = Long.MIN_VALUE;
        int serial = -1;
        boolean behind = false;
        while (sortie.levelSeconds() < 56) {
            Enemy rig = IceShelfFixture.rig(sortie);
            // Under the rig until its third ring, then above it (outside its arc) for a while.
            double y = behind ? Math.min(500, rig == null ? 100 : rig.renderY(1) + 120) : 100;
            int commands = rig == null ? Command.NONE : IceShelfFixture.toward(sortie, rig.renderX(1), y);
            sortie.step(commands);
            rig = IceShelfFixture.rig(sortie);
            if (rig == null || (serial >= 0 && rig.serial() != serial)) {
                continue;
            }
            if (serial < 0) {
                serial = rig.serial();
                foreseen = sortie.tick()
                        + SimStep.ticks(rig.spec().gun().orElseThrow().firstShotDelay());
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_FIRED
                        && Math.hypot(events.x(e) - rig.renderX(1), events.y(e) - rig.renderY(1))
                                < DrillRigLooks.FIRED_REACH) {
                    assertEquals(foreseen, sortie.tick(), "ring " + (checked + 1) + " when the looks foresaw it");
                    checked++;
                    behind = checked >= 2;
                    foreseen = sortie.tick() + DrillRigLooks.interval(rig);
                }
            }
            if (!DrillRigLooks.inArc(
                    rig, sortie.ship().renderX(1), sortie.ship().renderY(1))) {
                foreseen++;
            }
        }
        assertTrue(checked >= 3, "rings foreseen: " + checked);
    }

    /** The ice-crawler drives up the road to the drop's point over 4 s, the patch in its bed, and back down after. */
    @Test
    void theIceCrawlerCarriesThePatchUpTheRoadAndBack() throws IOException {
        LevelData.Drop drop = IceShelfFixture.level().released().getFirst();
        assertTrue(IceCrawlerLooks.carried(drop), "Level 22's drop rides the ice-crawler");
        assertTrue(drop.sprite().isPresent());
        assertEquals(2 * 3, frames("ice-crawler"), "2 headings of 3 tread frames");
        assertEquals(2 * 2, frames("ice-crawler-lights"));
        assertEquals(-IceCrawlerLooks.BELOW_EDGE, IceCrawlerLooks.y(-IceCrawlerLooks.DRIVE_SECONDS, drop.y()), 1e-9);
        assertEquals(drop.y(), IceCrawlerLooks.y(0, drop.y()), 1e-9, "at the drop's point as the patch is placed");
        assertEquals(-IceCrawlerLooks.BELOW_EDGE, IceCrawlerLooks.y(IceCrawlerLooks.DRIVE_SECONDS, drop.y()), 1e-9);
        assertTrue(Double.isNaN(IceCrawlerLooks.y(IceCrawlerLooks.DRIVE_SECONDS + 0.1, drop.y())), "gone");
        assertEquals(IceCrawlerLooks.UP, IceCrawlerLooks.heading(-1));
        assertEquals(IceCrawlerLooks.DOWN, IceCrawlerLooks.heading(1));
        assertEquals(3, IceCrawlerLooks.bodyFrame(IceCrawlerLooks.DOWN, 0, 6), "heading 1's first tread frame");
        assertEquals(0, IceCrawlerLooks.lightsFrame(IceCrawlerLooks.UP, 0, 4), "the beacon's flash");
        assertEquals(1, IceCrawlerLooks.lightsFrame(IceCrawlerLooks.UP, 0.5, 4), "the headlights alone");
        assertEquals(3, IceCrawlerLooks.lightsFrame(IceCrawlerLooks.DOWN, 0.5, 4));
        double[][] bed = IceCrawlerLooks.bedOffsets(pivots("ice-crawler"), 48);
        assertArrayEquals(new double[] {0, -7}, bed[IceCrawlerLooks.UP], "the bed behind the cab, driving up");
        assertArrayEquals(new double[] {0, 7}, bed[IceCrawlerLooks.DOWN]);
        assertTrue(
                IceShelfFixture.level().released().stream().noneMatch(d -> !IceCrawlerLooks.carried(d)),
                "no drone drops in Level 22");
    }
}
