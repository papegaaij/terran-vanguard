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
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.sim.Enemy;
import vanguard.sim.RisePhase;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part D's Level 24 looks (design/campaign Level 24; tools/art/abyss_ray.py, barbfin.py, spawning_reef.py,
 * l24_props.py, backdrop_l24.py, enemy_bullets.py violet): the art's frames as the looks count them; the plain
 * darkening section by section under the grade; the Ray's shadow, rise, glide and sink as it flies them (under the
 * flyers on {@code sub}), its beat, glow and break-up headings; the Barbfin's tell before its needles; the walkers' silt.
 */
class Level24LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-24");

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
        String ray = AbyssRayLooks.SLUG;
        assertEquals(AbyssRayLooks.HEADINGS * AbyssRayLooks.BEATS, frames(ray), "heading × beat");
        assertEquals(AbyssRayLooks.HEADINGS, frames(ray + "-glow"), "the glow per heading");
        assertEquals(AbyssRayLooks.STEPS, frames(ray + "-shadow"));
        assertEquals(AbyssRayLooks.STEPS, frames(ray + "-rise"));
        assertEquals(2 * AbyssRayLooks.STEPS, frames(ray + "-sink"), "two sides");
        assertEquals(4, frames(ray + "-mouth"));
        assertEquals(AbyssRayLooks.DEATH_HEADINGS.length * AbyssRayLooks.STEPS, frames(ray + "-death"));
        assertEquals(10, frames(ray + "-bloom"));
        assertEquals(AbyssRayLooks.HEADINGS, pivots(ray).get("mouth").size, "a mouth per heading");
        JsonValue beat = pivots(ray).get("beat");
        for (int b = 0; b < AbyssRayLooks.BEAT.length; b++) {
            assertEquals(AbyssRayLooks.BEAT[b], beat.getInt(b), "the beat order");
        }
        assertEquals(AbyssRayLooks.BEAT_SECONDS, pivots(ray).getDouble("beat_seconds"), 1e-9);
        String fin = BarbfinLooks.SLUG;
        assertEquals(BarbfinLooks.HEADINGS * BarbfinLooks.STATES, frames(fin), "heading × (swim, swim, tell)");
        assertEquals(BarbfinLooks.HEADINGS, frames(fin + "-glow"));
        assertEquals(4, frames(fin + "-snout"));
        assertEquals(BarbfinLooks.SHELL_HEADINGS * BarbfinLooks.SHELL_STEPS, frames(fin + "-shell"));
        assertEquals(8, frames(fin + "-ink"));
        assertEquals(BarbfinLooks.HEADINGS, pivots(fin).get("snout").size, "a snout per heading");
        assertEquals(16, frames(BulletLooks.OWN.get(fin)), "the violet needle's 16 headings");
        String reef = EnemyLooks.SPAWNING_REEF;
        assertEquals(EnemyLooks.IRIS_STATES * EnemyLooks.PULSE_FRAMES, frames(reef), "the Hive Node's layout");
        assertEquals(frames(reef), frames(reef + "-glow"));
        assertEquals(9, frames(reef + "-creep"), "alive, then its wither");
        assertEquals(16, frames(reef + "-death"));
        assertEquals(16, frames(reef + "-tatters"));
        assertTrue(Files.isRegularFile(SPRITES.resolve(reef + "-stump.png")));
        assertEquals(3, frames("meridian-wreck"), "lying, hit, opened");
        assertEquals(8, frames("meridian-wreck-glow"), "the lamp's and the sparks' halves");
        assertEquals(8, frames(PlainLooks.WALKER + "-silt"));
        assertEquals(16, frames(BACKDROP, "relay-beacon"));
        assertEquals(16, frames(BACKDROP, "relay-beacon" + Backdrop.GLOW), "the light per pulse frame");
    }

    /** The beacons' light is declared beside them with their frames and clock, and placed nowhere itself. */
    @Test
    void theBeaconsLightGoesWithThem() {
        BackdropData backdrop = AbyssalPlainFixture.level().backdrop();
        BackdropData.Piece beacon = backdrop.pieces().get("relay-beacon");
        BackdropData.Piece light = backdrop.pieces().get("relay-beacon" + Backdrop.GLOW);
        assertEquals(beacon.imageCount(), light.imageCount());
        assertEquals(beacon.fps(), light.fps());
        assertEquals(beacon.size(), light.size());
        assertEquals(beacon.layer(), light.layer());
        assertEquals(
                3,
                backdrop.placements().stream()
                        .filter(placed -> placed.piece().equals("relay-beacon"))
                        .count());
        assertTrue(backdrop.placements().stream()
                .noneMatch(placed -> placed.piece().equals("relay-beacon-glow")));
    }

    /** The plain darkens by section ("warm behind, dark ahead"): 0.50, 0.32, 0.18, 0.14, each eased in over 2 s. */
    @Test
    void thePlainDarkensSectionBySection() {
        LevelData level = AbyssalPlainFixture.level();
        assertTrue(UnderwaterGrade.hasLook(level));
        List<Optional<UnderwaterLook>> looks = new ArrayList<>();
        double[] starts = new double[level.sections().size()];
        for (int i = 0; i < starts.length; i++) {
            looks.add(level.underwaterLookOf(i));
            starts[i] = level.sectionStart(i);
        }
        float[] out = new float[7];
        double[] mids = {15, 60, 115, 160};
        double[] ambient = {0.5, 0.32, 0.18, 0.14};
        for (int s = 0; s < mids.length; s++) {
            UnderwaterGrade.lookAt(looks, starts, mids[s], out);
            assertEquals(ambient[s], out[0], 1e-6, "section " + (s + 1));
        }
        UnderwaterGrade.lookAt(looks, starts, starts[1] + 1, out);
        assertEquals((0.5 + 0.32) / 2, out[0], 1e-6, "halfway through the reef field's ease");
        UnderwaterGrade.lookAt(looks, starts, 115, out);
        assertEquals(0, out[4], 1e-6, "no caustics along the beacon line");
    }

    @Test
    void theRaysPhasesStepThroughTheirFrames() {
        assertEquals(0, AbyssRayLooks.step(0));
        assertEquals(3, AbyssRayLooks.step(0.49));
        assertEquals(7, AbyssRayLooks.step(1), "the last held");
        int ticks = (int) Math.round(AbyssRayLooks.BEAT_SECONDS / 4 * SimStep.PER_SECOND);
        int[] order = new int[4];
        for (int b = 0; b < 4; b++) {
            order[b] = AbyssRayLooks.beatFrame((long) b * ticks + 1, 0);
        }
        assertEquals(List.of(0, 1, 0, 2), List.of(order[0], order[1], order[2], order[3]));
        assertEquals(0, AbyssRayLooks.sinkSide(-1), "gliding left: heading 4");
        assertEquals(1, AbyssRayLooks.sinkSide(1), "gliding right: heading 12");
        assertEquals(0, AbyssRayLooks.glowOpacity(RisePhase.SHADOW, 0.9), 1e-6, "a shadow shows no glow");
        assertEquals(0.5, AbyssRayLooks.glowOpacity(RisePhase.RISE, 0.5), 1e-6);
        assertEquals(1, AbyssRayLooks.glowOpacity(RisePhase.GLIDE, 0.5), 1e-6);
        assertEquals(0.25, AbyssRayLooks.glowOpacity(RisePhase.SINK, 0.75), 1e-6);
        assertEquals(0, AbyssRayLooks.deathHeading(0));
        assertEquals(1, AbyssRayLooks.deathHeading(5), "nearest heading 4");
        assertEquals(2, AbyssRayLooks.deathHeading(11), "nearest heading 12");
        assertEquals(0, AbyssRayLooks.deathHeading(15), "nearest heading 0");
        assertEquals(0, AbyssRayLooks.mouthFrame(0, 4));
        assertEquals(3, AbyssRayLooks.mouthFrame(7, 4));
        assertEquals(-1, AbyssRayLooks.mouthFrame(8, 4), "played out");
    }

    /**
     * Flown to the first Ray: its shadow at t=58 on {@code sub} (drawn under the flyers), the rise flipping it to
     * {@code air} at its middle, the glide on the play plane with its fans, the sink back to {@code sub}.
     */
    @Test
    void theFirstRayRisesGlidesAndSinks() {
        Sortie sortie = AbyssalPlainFixture.sortie();
        List<RisePhase> seen = new ArrayList<>();
        boolean deepShadow = true;
        boolean airGlide = true;
        boolean fan = false;
        double shadowAt = -1;
        int steps = 0;
        while (steps++ < 80 * SimStep.PER_SECOND && !(seen.contains(RisePhase.SINK) && seenGone(sortie, seen))) {
            sortie.step(IceShelfFixtureCommands.hold(sortie));
            Enemy ray = AbyssalPlainFixture.first(sortie, AbyssRayLooks.SLUG);
            if (ray == null) {
                continue;
            }
            RisePhase phase = ray.risePhase();
            if (seen.isEmpty() || seen.getLast() != phase) {
                seen.add(phase);
            }
            if (phase == RisePhase.SHADOW) {
                deepShadow &= AbyssRayLooks.deep(ray);
                shadowAt = shadowAt < 0 ? sortie.levelSeconds() : shadowAt;
            }
            if (phase == RisePhase.GLIDE) {
                airGlide &= !AbyssRayLooks.deep(ray);
                assertEquals(AbyssRayLooks.drawnHeading(ray), EnemyLooks.heading(ray.facing(), AbyssRayLooks.HEADINGS));
                SimEvents events = sortie.events();
                for (int e = 0; e < events.size(); e++) {
                    fan |= events.type(e) == SimEvents.Type.ENEMY_FIRED
                            && Math.abs(events.x(e) - ray.renderX(1)) < 8
                            && Math.abs(events.y(e) - ray.renderY(1)) < 8;
                }
            }
        }
        assertEquals(List.of(RisePhase.SHADOW, RisePhase.RISE, RisePhase.GLIDE, RisePhase.SINK), seen);
        assertTrue(shadowAt >= 58 && shadowAt < 58.5, "the shadow at its wave: " + shadowAt);
        assertTrue(deepShadow, "the shadow under the flyers");
        assertTrue(airGlide, "the glide on the play plane");
        assertTrue(fan, "its fans in the glide");
    }

    private static boolean seenGone(Sortie sortie, List<RisePhase> seen) {
        return AbyssalPlainFixture.first(sortie, AbyssRayLooks.SLUG) == null && seen.size() >= 4;
    }

    /** The Barbfin tells for the last 0.25 s before its next needle, foreseen from its last; never before its first. */
    @Test
    void theBarbfinTellsBeforeItsNeedle() {
        int interval = SimStep.ticks(2.4);
        long shot = 1000;
        int tell = SimStep.ticks(BarbfinLooks.TELL_SECONDS);
        assertFalse(BarbfinLooks.telling(shot + interval - tell - 1, shot, interval));
        assertTrue(BarbfinLooks.telling(shot + interval - tell, shot, interval));
        assertTrue(BarbfinLooks.telling(shot + interval - 1, shot, interval));
        assertFalse(BarbfinLooks.telling(shot + interval, shot, interval), "the needle");
        assertFalse(BarbfinLooks.telling(500, Long.MIN_VALUE, interval), "no needle yet");
        assertEquals(0, BarbfinLooks.sparkleFrame(0, 4));
        assertEquals(-1, BarbfinLooks.sparkleFrame(8, 4));
        assertEquals(-1, BarbfinLooks.sparkleFrame(-1, 4));
        assertTrue(BarbfinLooks.swimFrame(0, 0, false) != BarbfinLooks.swimFrame(10, 0, false), "6 fps moving");
        assertEquals(BarbfinLooks.swimFrame(0, 0, true), BarbfinLooks.swimFrame(10, 0, true), "2 fps holding");
    }

    @Test
    void aWalkerRaisesSiltOncePerStride() {
        assertEquals(0, PlainLooks.strideOf(39.9, 40));
        assertEquals(1, PlainLooks.strideOf(40, 40));
        assertEquals(2, PlainLooks.strideOf(95, 40));
    }

    /**
     * The ship held low on the left, not firing (the Ray lives to sink: from the left it glides right across the field
     * and sinks before its far edge), the level flown invulnerable.
     */
    static final class IceShelfFixtureCommands {
        private IceShelfFixtureCommands() {}

        static int hold(Sortie sortie) {
            return IceShelfFixture.toward(sortie, 120, 100) & ~vanguard.sim.Command.FIRE.bit();
        }
    }
}
