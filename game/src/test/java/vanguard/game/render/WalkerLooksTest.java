package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.Missiles;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * M7 part F: the Revenant Walker's looks (design/enemies/bosses/revenant-walker; tools/art/revenant_walker.py's sprites
 * and assets/pivots/revenant-walker.json): the legs' 8-frame walk at 8 headings, the vents shut, open and overheated,
 * the pods' 16 aims with the hatch, the flamer's 32 aims, the pilot light flaring over the tell, the topple's 6 steps
 * over the 2 s death and then the wreck, which scrolls with the ground and rides a belt; the pods on their shoulders
 * and the flamer and vents on its back; flown in its arena ({@link WalkerFixture}), it walks passes on and off the
 * screen, its feet come down, its pods' hatches open before their missiles and the looks' frames cover every state.
 */
class WalkerLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        int k = WalkerLooks.HEADINGS;
        assertEquals(k * WalkerLooks.WALK_FRAMES, frames("revenant-walker-legs"), "8 headings x the 8-frame walk");
        assertEquals(k, frames("revenant-walker"), "the hull");
        assertEquals(k, frames("revenant-walker-plate"), "the plate's glance");
        assertEquals(k * WalkerLooks.VENT_STATES, frames("revenant-walker-vents"));
        assertEquals(WalkerLooks.POD_AIMS * 2, frames("revenant-walker-pod"), "16 aims x the hatch");
        assertEquals(k, frames("revenant-walker-pod-stub"));
        assertEquals(WalkerLooks.FLAMER_AIMS, frames("revenant-walker-flamer"));
        assertEquals(WalkerLooks.PILOT_FRAMES, frames("revenant-walker-pilot"));
        assertEquals(k * WalkerLooks.TOPPLE_STEPS, frames("revenant-walker-topple"));
        assertEquals(k, frames("revenant-walker-wreck"));
        assertEquals(WalkerLooks.MISSILE_HEADINGS, frames("revenant-walker-missile"));
        assertEquals(6, frames("revenant-walker-smoke"), "the trail's puff over its age");
        assertEquals(8, frames("revenant-walker-flame"), "a flame puff over its age");
    }

    @Test
    void theFramesFollowHeadingWalkAndState() {
        assertEquals(0, WalkerLooks.legFrame(0, 0));
        assertEquals(7, WalkerLooks.legFrame(0, 0.99));
        assertEquals(2 * 8 + 4, WalkerLooks.legFrame(2, 0.5), "heading 2 (left), half way through its stride");
        assertEquals(63, WalkerLooks.legFrame(7, 0.999));
        assertEquals(0, WalkerLooks.ventState(false, true), "shut");
        assertEquals(1, WalkerLooks.ventState(true, false), "open after a burn: hot amber");
        assertEquals(2, WalkerLooks.ventState(true, true), "always open in its last phase: overheated");
        assertEquals(3 * 3 + 2, WalkerLooks.ventFrame(3, 2));
        assertEquals(5 * 2 + 1, WalkerLooks.podFrame(5, true), "the hatch open over the tell");
        assertEquals(31, WalkerLooks.podFrame(15, true));
        assertEquals(0, WalkerLooks.nearest(0, 32));
        assertEquals(8, WalkerLooks.nearest(Math.PI / 2, 32), "a quarter turn clockwise from down: left");
        assertEquals(16, WalkerLooks.nearest(Math.PI, 32), "against its heading");
        assertEquals(0, WalkerLooks.nearest(2 * Math.PI - 0.01, 16));
        assertEquals(0, WalkerLooks.pilotFrame(StrideArena.ConePhase.IDLE, 0), "dim while cold");
        assertEquals(1, WalkerLooks.pilotFrame(StrideArena.ConePhase.TELL, 0));
        assertEquals(3, WalkerLooks.pilotFrame(StrideArena.ConePhase.TELL, 0.99), "the blue-white flare");
        assertEquals(2, WalkerLooks.pilotFrame(StrideArena.ConePhase.BURN, 0.5));
        assertTrue(WalkerLooks.bursts(Missiles.SHOT_DOWN) && WalkerLooks.bursts(Missiles.STRUCK));
        assertFalse(WalkerLooks.bursts(Missiles.FIZZLED), "a spent missile only puffs out");
    }

    @Test
    void theBeamsHeatBuildsAndCoolsWithoutFlicker() {
        double heat = 0;
        for (int i = 0; i < WalkerLooks.HEAT_BUILD_TICKS; i++) {
            heat = WalkerLooks.heated(heat, true);
        }
        assertEquals(1, heat, 1e-9, "full after its build");
        double slipped = heat;
        for (int i = 0; i < 6; i++) {
            slipped = WalkerLooks.heated(slipped, false);
        }
        assertTrue(slipped > 0.85, "the beam slipping off for 0.1 s barely cools it: " + slipped);
        for (int i = 0; i < WalkerLooks.HEAT_COOL_TICKS; i++) {
            heat = WalkerLooks.heated(heat, false);
        }
        assertEquals(0, heat, 1e-9, "cold after its cooling");
        assertTrue(WalkerLooks.HEAT_COOL_TICKS > WalkerLooks.HEAT_BUILD_TICKS, "it cools slower than it heats");
    }

    @Test
    void aHitFlashesPartlyAndTheConeReads() {
        assertEquals(HitFlash.PART_FLASH, WalkerLooks.hitAmount(true, 1), 1e-6);
        assertTrue(WalkerLooks.hitAmount(true, 1) <= 0.35f, "a partial flash, not a white silhouette");
        assertEquals(0, WalkerLooks.hitAmount(false, 1));
        assertTrue(WalkerLooks.hitAmount(true, 0.3f) < WalkerLooks.hitAmount(true, 1), "the flash reduction");
        assertTrue(WalkerLooks.tellStrength(0) >= 0.4f, "the tell's outline reads from its start");
        assertTrue(WalkerLooks.tellStrength(1) > WalkerLooks.tellStrength(0));
        assertEquals(1, WalkerLooks.burnFade(0.5), 1e-6, "the burn held at full");
        assertTrue(WalkerLooks.burnFade(0.01) < 0.1f, "in quickly");
        assertTrue(WalkerLooks.MISSILE_SCALE >= 1.5f, "the missiles drawn larger");
        assertTrue(WalkerLooks.TRAIL_FRAME_TICKS * 6 * SimStep.SECONDS >= 0.8, "a trail that lasts most of a second");
    }

    @Test
    void itTopplesOverItsDeathAndLeavesItsWreck() {
        int death = SimStep.ticks(2);
        assertEquals(4 * 6, WalkerLooks.toppleFrame(4, 0, death), "heading 4's first step");
        assertEquals(4 * 6 + 5, WalkerLooks.toppleFrame(4, death - 1, death), "its last step");
        assertEquals(4 * 6 + 3, WalkerLooks.toppleFrame(4, death / 2, death), "half way: its fourth step");
        assertEquals(-1, WalkerLooks.toppleFrame(4, death, death), "then the wreck");
        assertEquals(300, WalkerLooks.wreckY(300, 1000, 1000, 0), 1e-9, "the clock halted: where it fell");
        assertEquals(200, WalkerLooks.wreckY(300, 1000, 1100, 0), 1e-9, "carried down by the scroll (the crawl)");
        assertEquals(240, WalkerLooks.wreckY(300, 1000, 1100, 40), 1e-9, "and up the belt it lies on");
    }

    @Test
    void thePodsSitOnTheirShouldersAndTheFlamerAndVentsOnItsBack() throws IOException {
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/revenant-walker.json")));
        SetPieceSpec boss = WalkerFixture.level().setPieces().getFirst();
        assertEquals("revenant-walker", boss.slug());
        double art0 = pivots.get("pods").get(0).get(0).getDouble(0);
        double art1 = pivots.get("pods").get(0).get(1).getDouble(0);
        for (int p : new int[] {WalkerFixture.LEFT_POD, WalkerFixture.RIGHT_POD}) {
            double dx = boss.parts().get(p).dx();
            int art = WalkerLooks.podIndex(dx, art0, art1);
            double drawn = art == 0 ? art0 : art1;
            assertEquals(
                    Math.signum(dx), Math.signum(drawn), boss.parts().get(p).name() + " on its own side");
        }
        // The pivots are y down: facing down (heading 0) its back is up the screen; facing up (4), down.
        assertTrue(pivots.get("flamer").get(0).getDouble(1) < -20, "the flamer behind it, facing down");
        assertTrue(pivots.get("vents").get(0).getDouble(1) < -10, "the vents on its back");
        assertTrue(pivots.get("flamer").get(4).getDouble(1) > 20, "facing up, behind it is down the screen");
        assertEquals(38.9, WalkerLooks.along(pivots.get("nozzle")), 1, "the nozzle along the flamer's aim");
        assertEquals(0.3, WalkerLooks.nozzleShare(45, 150), 1e-9, "the flame leaves the nozzle, 45 px down the cone");
        assertEquals(0.5, WalkerLooks.nozzleShare(140, 150), 1e-9, "at most half the cone");
        assertEquals(0, WalkerLooks.nozzleShare(-5, 150), 1e-9);
    }

    /**
     * Flown for 50 s: it arrives, walks passes on and off the screen at more than one heading, its feet come down
     * (the stomp and the dust) while its walk runs through all 8 frames, its pods' hatches open before their missiles,
     * the missiles fly; every frame the looks pick lies in its set.
     */
    @Test
    void flownItWalksItsPassesAndItsPodsFire() {
        Sortie sortie = WalkerFixture.sortie();
        Set<StrideArena.Gait> gaits = EnumSet.noneOf(StrideArena.Gait.class);
        Set<Integer> headings = new HashSet<>();
        Set<Integer> walkFrames = new HashSet<>();
        Set<StrideArena.ConePhase> cones = EnumSet.noneOf(StrideArena.ConePhase.class);
        int steps = 0;
        int hatches = 0;
        int launches = 0;
        boolean hatchOpenSeen = false;
        int missilesSeen = 0;
        for (int i = 0; i < SimStep.ticks(50); i++) {
            sortie.step(Command.NONE);
            SetPiece walker = WalkerFixture.walker(sortie);
            StrideArena stride = walker.stride().orElseThrow();
            gaits.add(stride.gait());
            if (!stride.offScreen()) {
                headings.add(stride.heading());
                if (stride.gait() == StrideArena.Gait.WALK) {
                    int frame = WalkerLooks.legFrame(stride.heading(), stride.walkPhase());
                    assertTrue(frame >= 0 && frame < 64, "leg frame " + frame);
                    walkFrames.add(frame % WalkerLooks.WALK_FRAMES);
                }
                cones.add(stride.conePhase());
                assertTrue(WalkerLooks.nearest(stride.coneHeading(), WalkerLooks.FLAMER_AIMS) < 32);
            }
            hatchOpenSeen |=
                    walker.partHatchOpen(WalkerFixture.LEFT_POD) || walker.partHatchOpen(WalkerFixture.RIGHT_POD);
            missilesSeen = Math.max(missilesSeen, sortie.missiles().size());
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case WALKER_STEP -> steps++;
                    case POD_HATCH -> hatches++;
                    case MISSILE_LAUNCHED -> launches++;
                    default -> {}
                }
            }
        }
        assertTrue(gaits.contains(StrideArena.Gait.WALK) && gaits.contains(StrideArena.Gait.OFF), "passes: " + gaits);
        assertTrue(headings.size() >= 2, "more than one heading: " + headings);
        assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6, 7), walkFrames, "the whole walk cycle");
        assertTrue(steps > 10, "its feet came down: " + steps);
        assertTrue(hatches > 0 && hatchOpenSeen, "the hatches opened (the tell)");
        assertTrue(launches >= hatches, "a salvo after each hatch: " + launches + " for " + hatches);
        assertTrue(missilesSeen > 0, "missiles in flight");
        assertTrue(cones.contains(StrideArena.ConePhase.IDLE), "cold between burns: " + cones);
    }

    private static int frames(String root) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.filter(file -> file.getFileName().toString().matches(root + "_\\d+\\.png"))
                    .count();
        }
    }
}
