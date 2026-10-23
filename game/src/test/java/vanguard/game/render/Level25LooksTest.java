package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.sim.CoilPhase;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.WallMounts;

/**
 * M8 part E's Level 25 looks (design/campaign Level 25; tools/art/siren.py, spiral_nautilus.py, l25_props.py,
 * backdrop_l25.py): the art's frames as the looks count them; the trench under its grade; the Siren's steps, its place
 * on its wall's foot, its ring's fade and the rock a ring's lines stop at; the Spiral Nautilus's roll, crown steps and
 * headings as it flies its coil; the survey buoy moored at the drop's point; the cave mouths at the notches.
 */
class Level25LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-25");

    private static long frames(Path dir, String name) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static long frames(String name) throws IOException {
        return frames(SPRITES, name);
    }

    private static JsonValue pivots(String name) throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + name + ".json")));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        String siren = SirenLooks.SLUG;
        assertEquals(SirenLooks.FACINGS * SirenLooks.STEPS, frames(siren), "facing × step");
        assertEquals(SirenLooks.FACINGS * SirenLooks.STEPS, frames(siren + "-glow"));
        assertEquals(SirenLooks.FACINGS * SirenLooks.DEATH_STEPS, frames(siren + "-death"));
        assertEquals(SirenLooks.FACINGS, frames(siren + "-dead"));
        assertEquals(8, frames(siren + "-plume"));
        assertEquals(8, frames(siren + "-ring"));
        assertEquals(8, frames(siren + "-shimmer"));
        assertEquals(SirenLooks.FACINGS, pivots(siren).get("sac").size, "a sac per facing");
        assertEquals(7, pivots(siren).get("ring").getInt("front_row"));
        String shell = NautilusLooks.SLUG;
        assertEquals(NautilusLooks.SPIN, frames(shell));
        assertEquals(NautilusLooks.SPIN, frames(shell + "-lip"));
        assertEquals(NautilusLooks.HEADINGS, frames(shell + "-open"));
        assertEquals(NautilusLooks.HEADINGS * NautilusLooks.CROWN_STEPS, frames(shell + "-crown"));
        assertEquals(NautilusLooks.HEADINGS * NautilusLooks.GLOWS, frames(shell + "-glow"));
        assertEquals(NautilusLooks.HEADINGS, frames(shell + "-dark"));
        assertEquals(2 * NautilusLooks.HALVES_STEPS, frames(shell + "-halves"));
        assertEquals(6, frames(shell + "-fleck"));
        JsonValue crown = pivots(shell).get("crown_offset");
        assertEquals(NautilusLooks.HEADINGS, crown.size);
        assertEquals(NautilusLooks.EMERGED + 1, crown.get(0).size, "the three emerging steps' offsets");
        assertEquals(3, frames("survey-sub"), "lying, hit, opened");
        assertEquals(8, frames("survey-sub-glow"), "the pinger's and the sparks' halves");
        assertEquals(3, frames("survey-buoy"), "shut, opening, open");
        assertEquals(4, frames("survey-buoy-glow"), "the strobe");
        for (String side : List.of("left", "right")) {
            assertTrue(Files.isRegularFile(BACKDROP.resolve(Backdrop.CAVE_MOUTH + side + ".png")), side);
        }
    }

    /** The trench darkens to 0.14 under its grade from the start, 0.10 over the floor (the lights going out). */
    @Test
    void theTrenchIsDarkUnderItsGrade() {
        LevelData level = SirenTrenchFixture.level();
        assertTrue(UnderwaterGrade.hasLook(level));
        List<Optional<UnderwaterLook>> looks = new ArrayList<>();
        double[] starts = new double[level.sections().size()];
        for (int i = 0; i < starts.length; i++) {
            looks.add(level.underwaterLookOf(i));
            starts[i] = level.sectionStart(i);
        }
        float[] out = new float[7];
        UnderwaterGrade.lookAt(looks, starts, 40, out);
        assertEquals(0.14, out[0], 1e-6, "the walls");
        UnderwaterGrade.lookAt(looks, starts, 120, out);
        assertEquals(0.14, out[0], 1e-6, "the narrows");
        UnderwaterGrade.lookAt(looks, starts, 170, out);
        assertEquals(0.1, out[0], 1e-6, "the floor");
        assertEquals(0, out[4], 1e-6, "no caustics in the trench");
    }

    @Test
    void theSirensStepsAndPlace() {
        assertEquals(0, SirenLooks.step(0, false, 0, 0), "idle");
        assertEquals(1, SirenLooks.step(0, false, SimStep.ticks(0.5), 0), "the breath at 2 fps");
        assertEquals(SirenLooks.TELL, SirenLooks.step(0.01, false, 0, 0));
        assertEquals(SirenLooks.TELL + 1, SirenLooks.step(0.5, false, 0, 0));
        assertEquals(SirenLooks.TELL + 2, SirenLooks.step(1, false, 0, 0), "the tell's last step");
        assertEquals(SirenLooks.SONG, SirenLooks.step(0, true, 0, 0));
        assertEquals(0, SirenLooks.facing(120), "the left wall's nests face right");
        assertEquals(1, SirenLooks.facing(360));
        double offset = WallMounts.FOOT_INSET - 6;
        assertEquals(offset, SirenLooks.BODY_OFFSET, 1e-9, "the art's centre 6 px off the foot line");
        assertEquals(120 - offset, SirenLooks.bodyX(120), 1e-9);
        assertEquals(360 + offset, SirenLooks.bodyX(360), 1e-9);
        assertEquals(1, SirenLooks.ringOpacity(100, 230), 1e-6);
        assertEquals(1, SirenLooks.ringOpacity(172.5, 230), 1e-6, "its last quarter starts");
        assertEquals(0.5, SirenLooks.ringOpacity(201.25, 230), 1e-6);
        assertEquals(0, SirenLooks.ringOpacity(230, 230), 1e-6);
        assertEquals(12, SirenLooks.segments(5));
        assertEquals(91, SirenLooks.segments(230), "about 16 px of arc a segment");
        assertEquals(SirenLooks.ANGLES, SirenLooks.segments(400), "at most one a line");
        assertEquals(1, SirenLooks.shimmerOpacity(1), 1e-6);
        assertEquals(0.5, SirenLooks.shimmerOpacity(SirenLooks.SHIMMER_FADE / 2), 1e-6);
        assertEquals(0, SirenLooks.shimmerOpacity(0), 1e-6);
    }

    /**
     * Flown into the trench: the first ring leaves a nest on its wall's foot; the lines from its sac toward its own
     * wall meet rock within a few px, those across the trench run clear to the far half, as the simulation's line.
     */
    @Test
    void aRingsLinesStopAtTheRock() {
        Sortie sortie = SirenTrenchFixture.sortie();
        int steps = 0;
        while (steps++ < 40 * SimStep.PER_SECOND) {
            sortie.step(SirenTrenchFixture.hold(sortie, 240, 110));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) != SimEvents.Type.RING) {
                    continue;
                }
                Terrain terrain = sortie.terrain();
                double x = events.x(e);
                double y = events.y(e);
                double[] blocked = new double[SirenLooks.ANGLES];
                SirenLooks.lookUp(terrain, sortie.groundScroll(), x, y, 230, blocked);
                boolean left = SirenLooks.facing(x) == 0;
                int toWall = left ? SirenLooks.ANGLES / 2 : 0;
                int across = left ? 0 : SirenLooks.ANGLES / 2;
                // Its own wall's rock (a notch's back at most 70 px deeper), the open trench across.
                assertTrue(blocked[toWall] <= WallMounts.FOOT_INSET + 80, "its own wall: " + blocked[toWall]);
                assertTrue(blocked[across] > 150, "across the trench: " + blocked[across]);
                return;
            }
        }
        throw new AssertionError("no ring in the first 40 s");
    }

    @Test
    void theNautilusRollsAndUncoils() {
        assertEquals(0, NautilusLooks.spinFrame(21.9, false));
        assertEquals(1, NautilusLooks.spinFrame(22, false), "a frame a 22 px rolled");
        assertEquals(NautilusLooks.SPIN - 1, NautilusLooks.spinFrame(22, true), "clockwise the other way");
        assertEquals(0, NautilusLooks.headingToward(0, 100, 0, 0), "the ship below: heading 0");
        assertEquals(4, NautilusLooks.headingToward(100, 0, 0, 0), "the ship to the left: heading 4");
        assertEquals(12, NautilusLooks.headingToward(0, 0, 100, 0), "the ship to the right: heading 12");
        // The opening (0.6 s, its tell the last 0.4 s): emerging 0-2 over the first 0.2 s, then the tell.
        assertEquals(0, crown(CoilPhase.OPENING, 0, false, true));
        assertEquals(1, crown(CoilPhase.OPENING, 0.15, false, true));
        assertEquals(2, crown(CoilPhase.OPENING, 0.3, false, true));
        assertEquals(NautilusLooks.CROWN_TELL, crown(CoilPhase.OPENING, 0.5, true, true));
        int sway = crown(CoilPhase.OPEN, 0.5, false, true);
        assertTrue(sway == NautilusLooks.SWAY || sway == NautilusLooks.SWAY + 1, "the sway");
        // The close: emerging backwards over the spiral-in's first 0.6 s (3 s), only once it opened.
        assertEquals(2, crown(CoilPhase.CLOSED, 0, false, true));
        assertEquals(0, crown(CoilPhase.CLOSED, 0.5 / 3, false, true));
        assertEquals(-1, crown(CoilPhase.CLOSED, 0.21, false, true), "closed");
        assertEquals(-1, crown(CoilPhase.CLOSED, 0, false, false), "never opened: closed");
        assertEquals(-1, crown(CoilPhase.LEAVING, 0, false, true));
        assertEquals(NautilusLooks.GLOW_TELL, NautilusLooks.glowFor(NautilusLooks.CROWN_TELL));
        assertEquals(NautilusLooks.GLOW_OPEN, NautilusLooks.glowFor(NautilusLooks.SWAY + 1));
        assertEquals(NautilusLooks.GLOW_EMERGING, NautilusLooks.glowFor(1));
        assertEquals(0, NautilusLooks.fleckFrame(0, 6));
        assertEquals(5, NautilusLooks.fleckFrame(17, 6));
        assertEquals(-1, NautilusLooks.fleckFrame(18, 6), "played out at 20 fps");
    }

    private static int crown(CoilPhase phase, double progress, boolean tell, boolean opened) {
        return NautilusLooks.crownStep(phase, progress, tell, opened, 0.6, 0.4, 3, 0, 0);
    }

    /**
     * Flown to the first pair (t=62.5): each Nautilus coils closed, opens with its tell and fires open, closes again,
     * and leaves after its hold; the crown shows exactly while it is uncoiled or closing.
     */
    @Test
    void theFirstPairFliesItsCoil() {
        Sortie sortie = SirenTrenchFixture.sortie();
        List<CoilPhase> seen = new ArrayList<>();
        int serial = -1;
        int steps = 0;
        while (steps++ < 110 * SimStep.PER_SECOND && !seen.contains(CoilPhase.LEAVING)) {
            sortie.step(SirenTrenchFixture.hold(sortie, 240, 110));
            Enemy shell = null;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().slug().equals(NautilusLooks.SLUG) && (serial < 0 || enemy.serial() == serial)) {
                    shell = enemy;
                    break;
                }
            }
            if (shell == null) {
                assertTrue(serial < 0, "the followed Nautilus left before its hold was over");
                continue;
            }
            serial = shell.serial();
            CoilPhase phase = shell.coilPhase();
            if (phase != CoilPhase.NONE && (seen.isEmpty() || seen.getLast() != phase)) {
                seen.add(phase);
            }
            assertTrue(sortie.levelSeconds() >= 62.5, "the first pair at t=62.5: " + sortie.levelSeconds());
        }
        assertEquals(CoilPhase.CLOSED, seen.getFirst());
        assertTrue(seen.contains(CoilPhase.OPENING), "it uncoils: " + seen);
        assertTrue(seen.contains(CoilPhase.OPEN), "it fires open: " + seen);
        assertTrue(seen.indexOf(CoilPhase.OPEN) > seen.indexOf(CoilPhase.OPENING));
        assertEquals(CoilPhase.LEAVING, seen.getLast(), "it leaves after its hold: " + seen);
    }

    /** The survey buoy stands at its drop's point at the drop's time, shut before it, opening, then open. */
    @Test
    void theSurveyBuoyIsMooredAtItsDrop() {
        LevelData level = SirenTrenchFixture.level();
        List<LevelData.Drop> drops =
                level.released().stream().filter(BuoyLooks::moored).toList();
        assertEquals(1, drops.size(), "the narrows' buoy");
        LevelData.Drop drop = drops.getFirst();
        assertEquals(118, drop.t(), 1e-9);
        assertEquals(BuoyLooks.SHUT, BuoyLooks.frame(-1));
        assertEquals(BuoyLooks.OPENING, BuoyLooks.frame(0.1));
        assertEquals(BuoyLooks.OPEN, BuoyLooks.frame(0.3));
        assertFalse(level.released().stream().anyMatch(other -> IceCrawlerLooks.carried(other)), "no vehicle");
        double at = level.scrollAt(drop.t());
        assertEquals(drop.y(), BuoyLooks.y(drop.y(), at, at), 1e-9, "at the drop's point at its time");
        assertEquals(drop.y() - 100, BuoyLooks.y(drop.y(), at, at + 100), 1e-9, "moved down with the floor after it");
    }

    /** Every notch lies on a wall at its middle row, so its mouth has its foot line there. */
    @Test
    void everyNotchHasItsMouthOnItsWall() {
        Terrain terrain = SirenTrenchFixture.sortie().terrain();
        assertEquals(25, terrain.notches().size());
        for (Terrain.Notch notch : terrain.notches()) {
            double foot = notch.side() == Terrain.Side.LEFT
                    ? terrain.leftAt(notch.scroll())
                    : terrain.rightAt(notch.scroll());
            assertFalse(Double.isNaN(foot), notch.toString());
            double image =
                    notch.side() == Terrain.Side.LEFT ? foot - Backdrop.CAVE_FOOT[0] : foot - Backdrop.CAVE_FOOT[1];
            assertTrue(image > -112 && image < PlayField.WIDTH, notch + ": the mouth's image on the field");
        }
    }
}
