package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.FurnaceVents;
import vanguard.sim.GroundSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M7 part F: Level 19's foundry looks (design/campaign Level 19; tools/art/l19_props.py's sprites): the furnace
 * vents' grate, glow, flame and smoke frames over their tell, burn and the smoke after it; a cradle under every
 * up-facing nest where the simulation sets it down, a gantry under each group but the smelter stacks' nests; the shift
 * office round the payroll safe; the ingot stacks beside the last belt and the pallets riding it; the crews' rovers on
 * their road out of the tower. Flown at medium, the vents tell, burn and smoke in ripple order and the nests stand on
 * their cradles.
 */
class FoundryLooksTest {
    private static final Path SPRITES = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "sprites");

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(FoundryLooks.GRATE_FRAMES, frames("furnace-vent"), "shut and the tell's three steps");
        assertEquals(FoundryLooks.GLOW_FRAMES, frames("furnace-vent-glow"));
        assertEquals(FoundryLooks.FLAME_FRAMES, frames("furnace-vent-flame"));
        assertEquals(FoundryLooks.SMOKE_FRAMES, frames("furnace-vent-smoke"));
        assertEquals(16, frames("crew-rover"), "16 headings");
        assertEquals(16, frames("crew-rover-lights"));
        for (String single : List.of(
                "gantry",
                "gantry-cradle",
                "shift-office",
                "ingot-pallet",
                "ingot-stack-a",
                "ingot-stack-b",
                "ingot-stack-c")) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(single + ".png")), single);
        }
        for (String stack : FoundryLooks.INGOT_STACK) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(stack + ".png")), stack);
        }
        FoundryLooks.TRIGGER_SURROUNDS.forEach((look, surround) -> {
            assertTrue(Files.isRegularFile(SPRITES.resolve(look + "_0.png")), look);
            assertTrue(Files.isRegularFile(SPRITES.resolve(surround.sprite() + ".png")), surround.sprite());
        });
    }

    @Test
    void theGratesFramesFollowTheirCycle() {
        assertEquals(0, FoundryLooks.grateFrame(FurnaceVents.Phase.IDLE, 0), "shut");
        assertEquals(1, FoundryLooks.grateFrame(FurnaceVents.Phase.TELL, 0), "red");
        assertEquals(2, FoundryLooks.grateFrame(FurnaceVents.Phase.TELL, 0.5), "orange");
        assertEquals(3, FoundryLooks.grateFrame(FurnaceVents.Phase.TELL, 0.99), "white-hot");
        assertEquals(3, FoundryLooks.grateFrame(FurnaceVents.Phase.BURN, 0.5), "white-hot under the flame");
        assertEquals(0, FoundryLooks.tellStep(0.2));
        assertEquals(2, FoundryLooks.tellStep(1), "the last step at its end");
        assertEquals(0, FoundryLooks.flameFrame(0), "the ignition");
        assertEquals(4, FoundryLooks.flameFrame(0.5), "roaring");
        assertEquals(7, FoundryLooks.flameFrame(1), "collapsing");
        assertEquals(0, FoundryLooks.smokeFrame(0));
        assertEquals(5, FoundryLooks.smokeFrame(6L * FoundryLooks.SMOKE_FRAME_TICKS - 1));
        assertEquals(-1, FoundryLooks.smokeFrame(6L * FoundryLooks.SMOKE_FRAME_TICKS), "gone after 0.75 s");
        assertEquals(-1, FoundryLooks.smokeFrame(-1));
    }

    @Test
    void thePalletsRideTheBeltClearOfItsDrums() {
        assertEquals(1000 + 100, FoundryLooks.palletY(0, 1000, 2000, 40, 2.5), 1e-9, "40 px/s up the belt");
        assertEquals(1000 + 250, FoundryLooks.palletY(1, 1000, 2000, 40, 2.5), 1e-9, "150 px apart");
        assertTrue(Double.isNaN(FoundryLooks.palletY(0, 1000, 2000, 40, 0)), "under the drum at the run's start");
        assertTrue(Double.isNaN(FoundryLooks.palletY(0, 1000, 2000, 40, 24.6)), "under the drum at its end");
        assertEquals(1000 + 50, FoundryLooks.palletY(0, 1000, 2000, 40, 26.25), 1e-9, "and round again");
        assertEquals(6, FoundryLooks.pallets(1000));
    }

    @Test
    void theRoversDriveOutOfTheTowerAcrossAndUpTheField() {
        double[] at = new double[3];
        FoundryLooks.roverAt(0, at);
        assertEquals(FoundryLooks.ROVER_ROAD[0][0], at[0], 1e-9, "at the tower's door");
        assertEquals(11, EnemyLooks.heading(at[2], 16), "heading right (12), a little up the screen");
        FoundryLooks.roverAt(1000, at);
        assertEquals(FoundryLooks.ROVER_ROAD[1][0], at[0], 1e-9, "beside the belt");
        assertEquals(8, EnemyLooks.heading(at[2], 16), "then up the screen, toward the south gate");
        assertTrue(at[1] > FoundryLooks.ROVER_ROAD[1][1]);
    }

    @Test
    void theLevelsPropsStandWhereTheDataPutsThem() {
        LevelData level = FoundryFixture.level();
        LevelScript script = FoundryFixture.script(Difficulty.MEDIUM);
        int upFacing = 0;
        int gantryGroups = 0;
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.arc().isPresent()) {
                upFacing += target.at().size();
                gantryGroups += target.target().contains("stack") ? 0 : 1;
            }
        }
        List<double[]> cradles = FoundryLooks.cradlePlaces(level, script);
        assertEquals(upFacing, cradles.size(), "a cradle under every up-facing nest");
        assertFalse(FoundryLooks.upFacing(null), "no gun, no cradle");
        assertTrue(
                gantryGroups >= 8,
                "the ore, hall, conveyor, furnace, yard gate and outbound gantries: " + gantryGroups);
        double[] lines = FoundryLooks.gantryLines(level, FoundryLooks.nestHeight(script));
        assertEquals(gantryGroups, lines.length, "a gantry a group, none under the stacks' nests");
        double ore = FoundryLooks.entryY(level, 18, FoundryLooks.nestHeight(script)) - FoundryLooks.NEST_ABOVE_GANTRY;
        assertEquals(ore, lines[0], 1, "the ore gantry 26 px under its middle nest (t=18)");
        List<FoundryLooks.Place> places = FoundryLooks.places(level, script);
        FoundryLooks.Place office = places.stream()
                .filter(place -> place.sprite().equals("shift-office"))
                .findFirst()
                .orElseThrow();
        assertEquals(76, office.x(), 1e-9, "the office at the first hall's left wall (x 76)");
        GroundSpec.Belt last = FoundryLooks.lastBelt(script);
        assertEquals(240, last.x(), 1e-9, "the outbound belt");
        long stacks = places.stream()
                .filter(place -> place.sprite().startsWith("ingot-stack"))
                .peek(place -> {
                    assertTrue(Math.abs(place.x() - last.x()) >= last.width(), "beside the belt, not on it");
                    assertTrue(place.x() > 24 && place.x() < PlayField.WIDTH - 24, "on the field");
                    assertTrue(place.groundY() > last.from() + PlayField.HEIGHT / 2.0
                            && place.groundY() < last.to() + PlayField.HEIGHT / 2.0);
                })
                .count();
        assertEquals(FoundryLooks.INGOT_ALONG.length, stacks);
    }

    /**
     * Flown at medium for 72 s: every up-facing nest stands on its cradle (within 2 px of where the looks put it), and
     * the first vent rows' grates glow, burn and smoke, each ripple from its first grate.
     */
    @Test
    void flownTheNestsStandOnTheirCradlesAndTheVentsRipple() {
        LevelData level = FoundryFixture.level();
        Sortie sortie = FoundryFixture.sortie();
        List<double[]> cradles = FoundryLooks.cradlePlaces(level, sortie.script());
        Set<FurnaceVents.Phase> phases = EnumSet.noneOf(FurnaceVents.Phase.class);
        Set<Integer> grateFrames = new HashSet<>();
        Set<Integer> nestsSeen = new HashSet<>();
        int tells = 0;
        int bursts = 0;
        int firstTold = -1;
        int smokes = 0;
        FurnaceVents.Phase[] last = null;
        while (sortie.levelSeconds() < 72) {
            sortie.step(Command.NONE);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.grounded()
                        || !FoundryLooks.upFacing(enemy.spec().gun().orElse(null))) {
                    continue;
                }
                double groundY = enemy.renderY(1) + sortie.groundScroll();
                double nearest = Double.POSITIVE_INFINITY;
                for (double[] cradle : cradles) {
                    if (Math.abs(cradle[0] - enemy.renderX(1)) < 0.5) {
                        nearest = Math.abs(cradle[1] - groundY) < Math.abs(nearest) ? cradle[1] - groundY : nearest;
                    }
                }
                assertTrue(
                        Math.abs(nearest) <= 2,
                        "nest at x " + enemy.renderX(1) + " on its cradle: " + nearest + " px off at t="
                                + sortie.levelSeconds());
                nestsSeen.add(enemy.serial());
            }
            FurnaceVents vents = sortie.vents();
            if (last == null) {
                last = new FurnaceVents.Phase[vents.grates()];
                java.util.Arrays.fill(last, FurnaceVents.Phase.IDLE);
            }
            for (int g = 0; g < vents.grates(); g++) {
                if (!vents.present(g)) {
                    continue;
                }
                phases.add(vents.phase(g));
                grateFrames.add(FoundryLooks.grateFrame(vents.phase(g), vents.progress(g)));
                smokes += last[g] == FurnaceVents.Phase.BURN && vents.phase(g) != FurnaceVents.Phase.BURN ? 1 : 0;
                last[g] = vents.phase(g);
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.VENT_TELL) {
                    if (tells++ == 0) {
                        firstTold = events.value(e);
                    }
                } else if (events.type(e) == SimEvents.Type.VENT_BURST) {
                    bursts++;
                }
            }
        }
        assertTrue(nestsSeen.size() >= 10, "the nests up to the furnace row: " + nestsSeen.size());
        assertEquals(EnumSet.allOf(FurnaceVents.Phase.class), phases, "cold, telling and burning grates");
        assertEquals(Set.of(0, 1, 2, 3), grateFrames, "every grate frame");
        assertTrue(tells >= 4 && bursts >= 4, "a row's ripple: " + tells + " tells, " + bursts + " bursts");
        assertEquals(sortie.vents().grate(0, 0), firstTold, "the ripple starts at the row's first grate");
        assertTrue(smokes >= 4, "a smoke puff after each burn: " + smokes);
    }

    private static int frames(String root) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.filter(file -> file.getFileName().toString().matches(root + "_\\d+\\.png"))
                    .count();
        }
    }
}
