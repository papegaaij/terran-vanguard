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
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.Light;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.content.SimSpecs;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LurePhase;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part F's Level 26 looks (design/campaign Level 26, Darkness; tools/art/glow_angler.py, l26_glows.py): the art's
 * frames as the looks count them; the dark's ambient ramp (20 → 6 % over t=8–18) and the Dark levels assist's 20 %;
 * the hard headlight cone; the static lights on their rows; the units' rim and sonar pools and the kinds' glow pools; the
 * Glow Angler's heading, jaw, stalk and death steps; an Angler flown into its lunge and ring.
 */
class Level26LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(26).orElseThrow();

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static LevelData level() {
        return CONTENT.levels().get(KEY);
    }

    private static Sortie sortie(Difficulty difficulty) {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, difficulty),
                SimSpecs.level(CONTENT, KEY, difficulty),
                SimSpecs.rules(CONTENT, KEY, difficulty).withInvulnerableShip(),
                60);
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        String angler = AnglerLooks.SLUG;
        int frames = AnglerLooks.HEADINGS * AnglerLooks.JAWS;
        assertEquals(frames, frames(angler), "heading × jaw");
        assertEquals(frames, frames(angler + "-glow"));
        assertEquals(AnglerLooks.HEADINGS, frames(angler + "-stalk"));
        assertEquals(8, frames(angler + "-lure"));
        assertEquals(4, frames(angler + "-muzzle"));
        assertEquals(8, frames(angler + "-death"));
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + angler + ".json")));
        for (String point : List.of("lure", "mouth", "eyes")) {
            assertEquals(AnglerLooks.HEADINGS, pivots.get(point).size, point + " per heading");
        }
        assertEquals(AnglerLooks.FRAME, pivots.get("frame").getInt(0));
        JsonValue reveal = pivots.get("stalk_reveal");
        assertEquals(AnglerLooks.STALK_WITHIN, reveal.getDouble("within"), 1e-9);
        assertEquals(AnglerLooks.STALK_FADE, reveal.getDouble("fade"), 1e-9);
        // The bead hangs below the body at heading 0 (straight down), about 34 px ahead of its centre.
        double beadY = pivots.get("lure").get(0).getDouble(1);
        assertEquals(34, beadY - AnglerLooks.FRAME / 2.0, 1);
        assertEquals(frames("driftjelly-deep"), frames("driftjelly-glow"), "the veins at the pulse frames");
        assertEquals(EelLooks.HEADINGS * 3, frames(EelLooks.SLUG + "-glow"), "heading × part");
    }

    /** Level 26 is the dark level; Level 25 (no lights, no lamp) is not. */
    @Test
    void onlyLevel26IsDark() {
        assertTrue(DarkLight.dark(level()));
        assertFalse(DarkLight.dark(CONTENT.levels().get(CONTENT.levelKey(25).orElseThrow())));
        UnderwaterLook look = level().underwaterLookOf(1).orElseThrow();
        assertEquals(56, look.escortLamp().orElseThrow(), 1e-9, "Rook's lamp");
        assertEquals(10, look.lightList().size());
    }

    /** The lamps go out behind the ship: 20 % until t=8, eased to 6 % by t=18 and held; the assist holds 20 %. */
    @Test
    void theDarkRampsDownAndTheAssistHoldsItsFloor() {
        LevelData level = level();
        List<Optional<UnderwaterLook>> looks = new ArrayList<>();
        double[] starts = new double[level.sections().size()];
        for (int i = 0; i < starts.length; i++) {
            looks.add(level.underwaterLookOf(i));
            starts[i] = level.sectionStart(i);
        }
        float[] out = new float[UnderwaterGrade.LOOK_SIZE];
        UnderwaterGrade.lookAt(looks, starts, 0, out);
        assertEquals(0.20, out[UnderwaterGrade.AMBIENT], 1e-6, "the lamp stretch");
        UnderwaterGrade.lookAt(looks, starts, 8, out);
        assertEquals(0.20, out[UnderwaterGrade.AMBIENT], 1e-6, "the ramp starts");
        UnderwaterGrade.lookAt(looks, starts, 13, out);
        assertEquals(0.13, out[UnderwaterGrade.AMBIENT], 1e-6, "half-way (smoothstep)");
        UnderwaterGrade.lookAt(looks, starts, 18, out);
        assertEquals(0.06, out[UnderwaterGrade.AMBIENT], 1e-6, "the dark");
        for (double t : new double[] {21, 60, 100, 170}) {
            UnderwaterGrade.lookAt(looks, starts, t, out);
            assertEquals(0.06, out[UnderwaterGrade.AMBIENT], 1e-6, "held at t=" + t);
        }
        assertEquals(260, out[UnderwaterGrade.REACH], 1e-6, "the cone's reach");
        assertEquals(70, out[UnderwaterGrade.ANGLE], 1e-6, "the cone's sector");
        assertEquals(1, out[UnderwaterGrade.LIGHTS], 1e-6, "the world in the cone at its full colour");
        assertEquals(0.15, out[UnderwaterGrade.RIM], 1e-6, "the faint rim");
        UnderwaterGrade.lookAt(looks, starts, 100, DarkLight.ASSIST_AMBIENT, out);
        assertEquals(0.20, out[UnderwaterGrade.AMBIENT], 1e-6, "the assist's floor");
        UnderwaterGrade.lookAt(looks, starts, 0, DarkLight.ASSIST_AMBIENT, out);
        assertEquals(0.20, out[UnderwaterGrade.AMBIENT], 1e-6);
    }

    /** The cone (as drawn) lights ahead of a lamp within its reach and sector, nothing behind or beside it. */
    @Test
    void theConeLightsOnlyAheadOfTheLamps() {
        double wide = Math.tan(Math.toRadians(70 * UnderwaterGrade.CONE_SHARE / 2))
                / Math.tan(UnderwaterGrade.CONE_HALF_ANGLE);
        double tall = 260.0 / UnderwaterGrade.CONE_LENGTH;
        assertEquals(1, UnderwaterGrade.beamAt(0, 60, 0, wide, tall), 1e-6, "straight ahead, near");
        assertTrue(UnderwaterGrade.beamAt(0, 200, 0, wide, tall) > 0.1, "far ahead, fading");
        assertEquals(0, UnderwaterGrade.beamAt(0, 265, 0, wide, tall), 1e-6, "past its reach");
        assertEquals(0, UnderwaterGrade.beamAt(0, -20, 0, wide, tall), 1e-6, "behind");
        assertEquals(0, UnderwaterGrade.beamAt(100, 60, 0, wide, tall), 1e-6, "beside, outside the sector");
        // Toed out to the left (counter-clockwise), it lights more on its left than on its right.
        double toe = 70 * UnderwaterGrade.TOE_SHARE;
        assertTrue(
                UnderwaterGrade.beamAt(-60, 150, toe, wide, tall) > UnderwaterGrade.beamAt(60, 150, toe, wide, tall));
    }

    /** The static lights scroll with the ground: the first lamp post on the screen at the start, the barge's at t≈84. */
    @Test
    void theStaticLightsLieOnTheirRows() {
        LevelData level = level();
        List<Light> lights = level.underwaterLookOf(0).orElseThrow().lightList();
        double first = DarkLight.lightY(lights.getFirst().scroll(), level.scrollAt(0));
        assertTrue(first > 0 && first < PlayField.HEIGHT, "the first lamp post at the start: " + first);
        Light barge = lights.stream()
                .filter(light -> light.x() < 100 && light.radius() == 80)
                .findFirst()
                .orElseThrow();
        double at86 = DarkLight.lightY(barge.scroll(), level.scrollAt(86));
        assertTrue(at86 > 0 && at86 < PlayField.HEIGHT, "the farm barge's lamp in at t≈84: " + at86);
        assertTrue(DarkLight.lightY(barge.scroll(), level.scrollAt(80)) > PlayField.HEIGHT, "not yet at t=80");
    }

    @Test
    void theUnitsRimGlowAndSonarPools() {
        assertEquals(36.25, DarkLight.rimRadius(new Hitbox(30, 24)), 1e-4, "the Angler: its body and a faint halo");
        assertEquals(1, DarkLight.plateauAlpha(0), 1e-6);
        assertEquals(1, DarkLight.plateauAlpha(DarkLight.PLATEAU), 1e-6, "flat over the body");
        assertEquals(0.5, DarkLight.plateauAlpha((1 + DarkLight.PLATEAU) / 2), 1e-6);
        assertEquals(0, DarkLight.plateauAlpha(1), 1e-6);
        assertEquals(0, DarkLight.sonarLight(false, 3), 1e-6);
        assertEquals(1, DarkLight.sonarLight(true, 3), 1e-6, "every unit whole in the window");
        assertEquals(0.5, DarkLight.sonarLight(true, DarkLight.SONAR_FADE / 2), 1e-6, "fading at its end");
        float low = DarkLight.HALO_ALPHA - DarkLight.HALO_SWING;
        for (double t = 0; t < 2; t += 0.1) {
            float halo = DarkLight.haloAlpha(t, 3);
            assertTrue(halo >= low - 1e-6 && halo <= DarkLight.HALO_ALPHA + DarkLight.HALO_SWING + 1e-6);
        }
        // Every kind Level 26 flies has its glow pool.
        Sortie sortie = sortie(Difficulty.MEDIUM);
        List<EnemySpec> kinds = sortie.enemyKinds();
        int[] glows = DarkLight.glowsOf(kinds);
        for (int k = 0; k < kinds.size(); k++) {
            assertTrue(glows[k] >= 0, kinds.get(k).slug() + " has a glow pool");
            float radius = DarkLight.GLOWS.get(glows[k]).radius();
            assertTrue(radius >= 24 && radius <= 40, kinds.get(k).slug() + ": r 24-40");
        }
    }

    @Test
    void theAnglersStepsAndStalk() {
        assertEquals(0, AnglerLooks.heading(0), "straight down");
        assertEquals(4, AnglerLooks.heading(Math.PI / 2), "the ship to the left: heading 4");
        assertEquals(12, AnglerLooks.heading(-Math.PI / 2), "to the right: heading 12");
        assertEquals(9, AnglerLooks.frameIndex(4, true), "heading × 2 + jaw");
        assertTrue(AnglerLooks.open(LurePhase.LUNGE, -1), "the gape wide in the dive");
        assertTrue(AnglerLooks.open(LurePhase.RETREAT, 0), "and at its ring");
        assertTrue(AnglerLooks.open(LurePhase.RETREAT, 7));
        assertFalse(AnglerLooks.open(LurePhase.RETREAT, 8), "shut after the ring's flash");
        assertFalse(AnglerLooks.open(LurePhase.DRIFT, -1), "shut while it hangs");
        assertEquals(0, AnglerLooks.stalkOpacity(200), 1e-6);
        assertEquals(0, AnglerLooks.stalkOpacity(160), 1e-6, "shown within 160 px");
        assertEquals(0.5, AnglerLooks.stalkOpacity(140), 1e-6);
        assertEquals(1, AnglerLooks.stalkOpacity(120), 1e-6);
        assertEquals(1, AnglerLooks.stalkOpacity(0), 1e-6);
        assertEquals(1, AnglerLooks.deathStalkOpacity(0), 1e-6, "the stalk stays lit");
        assertEquals(1, AnglerLooks.deathStalkOpacity(0.6), 1e-6);
        assertEquals(0.5, AnglerLooks.deathStalkOpacity(0.8), 1e-6);
        assertEquals(0, AnglerLooks.deathStalkOpacity(1), 1e-6, "dark after 1 s");
        assertEquals(0, AnglerLooks.lureFrame(0, 0, 8));
        assertEquals(1, AnglerLooks.lureFrame(8, 0, 8), "the bead's pulse at 8 fps");
        assertEquals(0, AnglerLooks.lureFrame(SimStep.PER_SECOND, 0, 8), "a cycle a second");
    }

    /**
     * Flown at the first Anglers: the ship closes in on one; it triggers ({@code LURE_LUNGE}), dives with its gape wide
     * and rings ({@code LURE_RING}) from where it is; the looks' gape is wide through the dive.
     */
    @Test
    void anAnglerLungesAndRings() {
        Sortie sortie = sortie(Difficulty.MEDIUM);
        int serial = -1;
        boolean dived = false;
        int steps = 0;
        while (steps++ < 60 * SimStep.PER_SECOND) {
            Enemy target = null;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                double y = enemy.renderY(1);
                if (enemy.spec().slug().equals(AnglerLooks.SLUG)
                        && (serial < 0 ? y > 0 && y < PlayField.HEIGHT : enemy.serial() == serial)) {
                    target = enemy;
                    break;
                }
            }
            int commands = target == null
                    ? SirenTrenchFixture.hold(sortie, 240, 110)
                    : IceShelfFixture.toward(sortie, target.renderX(1), target.renderY(1) - 100) & ~Command.FIRE.bit();
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.LURE_LUNGE && serial < 0) {
                    Enemy lunging = at(sortie, events.x(e), events.y(e));
                    assertTrue(lunging != null, "the lunging Angler is at its event");
                    serial = lunging.serial();
                    assertEquals(LurePhase.LUNGE, lunging.lurePhase());
                    assertTrue(AnglerLooks.open(lunging.lurePhase(), -1), "its gape wide");
                    dived = true;
                }
                if (events.type(e) == SimEvents.Type.LURE_RING && dived) {
                    Enemy ringing = at(sortie, events.x(e), events.y(e));
                    if (ringing != null && ringing.serial() == serial) {
                        assertEquals(LurePhase.RETREAT, ringing.lurePhase(), "it rings as it backs away");
                        return;
                    }
                }
            }
        }
        throw new AssertionError(dived ? "no ring after the lunge" : "no lunge in the first 60 s");
    }

    /** The Angler at (x, y) after the step, or null. */
    private static Enemy at(Sortie sortie, double x, double y) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals(AnglerLooks.SLUG)
                    && Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y) < 8) {
                return enemy;
            }
        }
        return null;
    }
}
