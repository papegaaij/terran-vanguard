package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.Flock;
import vanguard.sim.Pursuit;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Step GFX2, the round 43 capture's readability fixes for Level 20: the pursuit swarm's rim light, faint shadows,
 * tell glow and ring; the rim flak's flash core; the rear-guard drone's gold outline and gold shot apart from the Tail
 * Gun's cyan; the emergency cache's strobe halo, lit with its art's lamp.
 */
class ReadabilityLooksTest {
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");
    /** The basin's sand, about (200, 160, 110) in the capture. */
    private static final Color SAND = new Color(200 / 255f, 160 / 255f, 110 / 255f, 1);

    @Test
    void theMotesWearACoolRimLightThatGivesWayToTheTell() {
        assertEquals(1, PursuitLooks.rim(0), 1e-6, "the full rim light out of the tell");
        assertEquals(0, PursuitLooks.rim(PursuitLooks.TELL_GLOW), 1e-6, "gone at the tell's peak");
        assertTrue(PursuitLooks.rim(PursuitLooks.TELL_GLOW / 2) > 0);
        Color rim = PursuitLooks.RIM;
        assertTrue(rim.b > SAND.b + 0.4f && rim.g > SAND.g, "pale and cool: the sand's opposite");
        assertTrue(PursuitLooks.RIM_DIAMETER >= 16 + 4, "wider than the 16 px mote");
        assertTrue(PursuitLooks.RIM_ALPHA >= 0.4f);
        assertTrue(PursuitLooks.SHADOW_SHARE * Shadows.OPACITY <= 0.2f, "a faint shadow, not a second flock");
    }

    @Test
    void theTellGlowsReddishWhiteAndRingsOnceAtPlaySize() {
        Color disc = PursuitLooks.TELL_DISC;
        assertTrue(disc.r == 1 && disc.r > disc.g && disc.g > disc.b && disc.b > 0.5f, "reddish white");
        assertEquals(PursuitLooks.TELL_DISC_MIN, PursuitLooks.tellDisc(0), 1e-9);
        assertEquals(PursuitLooks.TELL_DISC_MAX, PursuitLooks.tellDisc(PursuitLooks.TELL_GLOW), 1e-9);
        assertTrue(
                PursuitLooks.TELL_DISC_MAX >= 1.75 * 16, "the ball's members glow near twice their size at the peak");
        assertEquals(0, PursuitLooks.tellDiscAlpha(0));
        assertEquals(PursuitLooks.TELL_DISC_ALPHA, PursuitLooks.tellDiscAlpha(PursuitLooks.TELL_GLOW), 1e-6);
        assertTrue(PursuitLooks.TELL_DISC_ALPHA < 0.6f, "a dozen overlapping discs below a white-out");
        assertEquals(0, PursuitLooks.ringAlpha(Pursuit.Phase.TELL, PursuitLooks.RING_FROM - 0.01));
        assertEquals(PursuitLooks.RING_ALPHA, PursuitLooks.ringAlpha(Pursuit.Phase.TELL, PursuitLooks.RING_FROM), 1e-6);
        assertTrue(PursuitLooks.ringAlpha(Pursuit.Phase.TELL, 0.8) > 0, "fading");
        assertEquals(0, PursuitLooks.ringAlpha(Pursuit.Phase.TELL, 1), 1e-6, "gone by the dive");
        for (Pursuit.Phase phase :
                new Pursuit.Phase[] {Pursuit.Phase.BAND, Pursuit.Phase.DIVE, Pursuit.Phase.CLIMB, Pursuit.Phase.BREAK
                }) {
            assertEquals(0, PursuitLooks.ringAlpha(phase, 0.7), phase + ": one ring, in the tell only");
        }
        assertTrue(PursuitLooks.ringRadius(0.6) > PursuitLooks.ringRadius(PursuitLooks.RING_FROM));
        assertEquals(PursuitLooks.RING_END, PursuitLooks.ringRadius(1), 1e-9);
        assertTrue(PursuitLooks.RING_START > 60 / 2.0 - 10, "round the 60 px ball");
    }

    /** Flown at medium without firing: Level 20's swarm wears the pursuit's look, no other flock does. */
    @Test
    void level20sSwarmWearsThePursuitLook() {
        assertFalse(PursuitLooks.pursuing(null));
        Sortie sortie = RetreatFixture.sortie();
        Flock flock = null;
        while (flock == null && sortie.levelSeconds() < 40) {
            sortie.step(Command.NONE);
            flock = RetreatFixture.pursuing(sortie);
        }
        assertTrue(flock != null, "a pursuit by t=40");
        Enemy member = null;
        for (int i = 0; i < flock.size() && member == null; i++) {
            member = flock.member(i);
        }
        assertTrue(member != null && PursuitLooks.pursuing(member.flock()));
    }

    @Test
    void theRimFlakOpensWithABrightFlashCore() {
        FlakFlashes flashes = new FlakFlashes();
        assertEquals(0, flashes.flashing(0));
        flashes.start(MilitiaFlak.x(0), MilitiaFlak.y(0, vanguard.content.LevelData.FlakBand.TOP), 100);
        assertEquals(1, flashes.flashing(100));
        assertEquals(1, FlakFlashes.strength(0), 1e-6, "full at the burst");
        assertTrue(FlakFlashes.strength(FlakFlashes.SECONDS / 2) > 0);
        assertEquals(0, FlakFlashes.strength(FlakFlashes.SECONDS));
        int gone = SimStep.ticks(FlakFlashes.SECONDS) + 1;
        assertEquals(0, flashes.flashing(100 + gone), "a short flash, then only the puff");
        assertTrue(FlakFlashes.coreDiameter(0) > FlakFlashes.coreDiameter(FlakFlashes.SECONDS / 2), "shrinking");
        assertTrue(FlakFlashes.coreDiameter(0) >= 16, "reads at play size");
        Color core = FlakFlashes.CORE;
        assertTrue(core.r > 0.9f && core.g > 0.9f && core.b > 0.75f, "a white-hot core");
        flashes.start(10, 600, 200);
        flashes.clear();
        assertEquals(0, flashes.flashing(200), "a restart clears it");
    }

    /** The drone's gold bolt (the light drone's art times its tint) is warm, the Tail Gun's cyan bolt cool. */
    @Test
    void theRearGuardDronesShotIsGoldApartFromTheTailGunsCyan() throws IOException {
        Color gold = WeaponLooks.shotRecolour("rear-guard-drone-bay");
        assertTrue(gold != null && gold.r > gold.g && gold.g > gold.b + 0.3f, "gold");
        assertEquals(null, WeaponLooks.shotRecolour("tail-gun"), "the Tail Gun keeps its art's colour");
        assertEquals(null, WeaponLooks.shotRecolour("light-drone-bay"), "the light drone keeps its cyan");
        float[] tail = brightest(SPRITES.resolve("tail-gun-shot_180.png"));
        assertTrue(tail[2] >= tail[0], "the Tail Gun's bolt is cyan");
        // The drone's bolt as the recolour shader draws it: its art's cyan body blended RECOLOUR of the way to gold
        // times its light (its brightest channel), its dark halo staying dark.
        float[] body = {63 / 255f, 195 / 255f, 238 / 255f};
        float light = Math.max(body[0], Math.max(body[1], body[2]));
        float k = WeaponLooks.RECOLOUR;
        float r = body[0] + (gold.r * light - body[0]) * k;
        float b = body[2] + (gold.b * light - body[2]) * k;
        assertTrue(r > b + 0.3f, "the drone's bolt body drawn warm: " + r + " vs " + b);
        assertEquals(4, ArsenalLooks.TRAIL_OUTLINE_OFFSETS.length, "its outline: 1 px up, down, left and right");
        for (int[] offset : ArsenalLooks.TRAIL_OUTLINE_OFFSETS) {
            assertEquals(1, Math.abs(offset[0]) + Math.abs(offset[1]));
        }
        assertTrue(ArsenalLooks.TRAIL_OUTLINE_ALPHA >= 0.5f);
    }

    /** The cache's halo lights exactly when its art's strobe does, round its lamp. */
    @Test
    void theEmergencyCachesHaloFollowsItsStrobe() throws IOException {
        StrobeHalo.Strobe strobe = StrobeHalo.of("emergency-cache");
        assertTrue(strobe != null);
        assertEquals(null, StrobeHalo.of("survey-cache"), "only a strobe gets a halo");
        int lit = 0;
        for (int k = 0; k < 8; k++) {
            BufferedImage frame = ImageIO.read(
                    SPRITES.resolve("emergency-cache-glow_" + k + ".png").toFile());
            int best = 0;
            int bestX = 0;
            int bestY = 0;
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int argb = frame.getRGB(x, y);
                    int sum = (argb >> 16 & 255) + (argb >> 8 & 255) + (argb & 255);
                    if ((argb >>> 24) > 0 && sum > best) {
                        best = sum;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            boolean lamp = best > 100;
            assertEquals(lamp, strobe.strength(k) > 0, "glow frame " + k);
            if (lamp) {
                lit++;
                assertEquals(bestX + 0.5 - frame.getWidth() / 2.0, strobe.dx(), 1, "the lamp's x in frame " + k);
                assertEquals(frame.getHeight() / 2.0 - bestY - 0.5, strobe.dy(), 1, "the lamp's y in frame " + k);
            }
        }
        assertTrue(lit >= 2, "it strobes");
        assertTrue(StrobeHalo.BLOOM >= 30, "a halo that reads at play scale round a 3 px lamp");
        assertEquals(0, strobe.strength(8), "past the table: dark");
    }

    /** The brightest pixel's colour (0..1) of a sprite file. */
    private static float[] brightest(Path file) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        int best = -1;
        int colour = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int sum = (argb >> 16 & 255) + (argb >> 8 & 255) + (argb & 255);
                if ((argb >>> 24) > 128 && sum > best) {
                    best = sum;
                    colour = argb;
                }
            }
        }
        return new float[] {(colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f, (colour & 255) / 255f};
    }
}
