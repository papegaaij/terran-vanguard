package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.content.LevelData;
import vanguard.sim.Sortie;

/**
 * M10 part G, step G4: the CDF beacon the fleet's bombers drop on Callisto's ice (design/campaign Level 41, Secrets and
 * pickups: an armour patch at t=20; tools/art/l41_props.py's {@code cdf-beacon*} sprites), presentation only: nobody can
 * hit it and it is not marked (no supply drone, no friendly marker, no rotor's launch). Its pod comes down over the
 * {@value #FALL_SECONDS} s before its drop, legs folded and turning slowly, scaled from {@value #FALL_SCALE} to 1 (seen
 * from above) on its retro burn's flicker (vacuum: no parachute); at the drop it lands where the simulation places the
 * patch, the ice dust kicked up in a ring, its cap springs open as the patch lifts out, and it stays on the ground with
 * its strobe and leg lamps blinking (the drones' convention), scrolling away with the ground. On the level clock;
 * nothing is allocated per frame.
 */
public final class CdfBeaconLooks {
    /** The drops' sprite this look draws ({@link #dropped}). */
    static final String SLUG = "cdf-beacon";
    /** The fall before the drop, s, and the pod's scale at its start. */
    static final double FALL_SECONDS = 1.2;

    static final float FALL_SCALE = 2;
    /** The fall's turn (its 4 frames a quarter turn), frames a second; the retro's flicker. */
    static final double TURN_FPS = 6;

    static final double RETRO_FPS = 20;
    /** The landing's dust (2 steps a frame) and the cap's opening (4 steps a frame), s a frame. */
    static final double LAND_FRAME_SECONDS = 2.0 / 60;

    static final double OPEN_FRAME_SECONDS = 4.0 / 60;
    /** A landed beacon is forgotten once this far below the bottom edge, px. */
    static final double GONE_BELOW = 40;

    private final List<LevelData.Drop> drops;
    private final AtlasRegion pod;
    private final Array<AtlasRegion> lights;
    private final Array<AtlasRegion> open;
    private final Array<AtlasRegion> fall;
    private final Array<AtlasRegion> retro;
    private final Array<AtlasRegion> land;
    /** Per drop the ground's scroll when it landed (NaN before). */
    private final double[] landScroll;

    private CdfBeaconLooks(Sprites sprites, List<LevelData.Drop> drops) {
        this.drops = drops;
        pod = sprites.region(SLUG);
        lights = optional(sprites, SLUG + "-lights");
        open = optional(sprites, SLUG + "-open");
        fall = optional(sprites, SLUG + "-fall");
        retro = optional(sprites, SLUG + "-retro");
        land = optional(sprites, SLUG + "-land");
        landScroll = new double[drops.size()];
        restart();
    }

    /** The level's beacons, or null for a level without a drop in the beacon's look (or without its sprites). */
    static CdfBeaconLooks of(Sprites sprites, LevelData level) {
        List<LevelData.Drop> drops =
                level.released().stream().filter(CdfBeaconLooks::dropped).toList();
        if (drops.isEmpty() || !sprites.has(SLUG)) {
            return null;
        }
        return new CdfBeaconLooks(sprites, drops);
    }

    /** Whether a drop is a beacon's (its sprite {@value #SLUG}): no supply drone carries it, it is not marked. */
    public static boolean dropped(LevelData.Drop drop) {
        return drop.sprite().filter(SLUG::equals).isPresent();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The level restarts: no beacon down. */
    void restart() {
        java.util.Arrays.fill(landScroll, Double.NaN);
    }

    /** The pod's scale {@code since} s after its drop (negative in its fall): {@value #FALL_SCALE} easing to 1, then 1. */
    static float fallScale(double since) {
        if (since >= 0) {
            return 1;
        }
        double share = Math.clamp(-since / FALL_SECONDS, 0, 1);
        return (float) (1 + (FALL_SCALE - 1) * share * share);
    }

    /** The beacons: falling, landing, opening and standing on the ice, at level time {@code seconds}. */
    void draw(SpriteBatch batch, Sortie sortie, double seconds) {
        for (int i = 0; i < drops.size(); i++) {
            LevelData.Drop drop = drops.get(i);
            double since = seconds - drop.t();
            if (since < -FALL_SECONDS) {
                landScroll[i] = Double.NaN;
                continue;
            }
            double x = drop.x();
            if (since < 0) {
                landScroll[i] = Double.NaN;
                float scale = fallScale(since);
                if (!fall.isEmpty()) {
                    int f = (int) Math.floorMod((long) Math.floor(seconds * TURN_FPS), (long) fall.size);
                    drawScaled(batch, fall.get(f), x, drop.y(), scale);
                }
                if (!retro.isEmpty()) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    int f = (int) Math.floorMod((long) Math.floor(seconds * RETRO_FPS), (long) retro.size);
                    drawScaled(batch, retro.get(f), x, drop.y(), scale);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
                continue;
            }
            if (Double.isNaN(landScroll[i])) {
                landScroll[i] = sortie.groundScroll();
            }
            double y = drop.y() - (sortie.groundScroll() - landScroll[i]);
            if (y < -GONE_BELOW) {
                continue;
            }
            int opening = (int) (since / OPEN_FRAME_SECONDS);
            AtlasRegion body = open.isEmpty() ? pod : open.get(Math.min(open.size - 1, opening));
            drawCentred(batch, body, x, y);
            int dust = (int) (since / LAND_FRAME_SECONDS);
            if (dust < land.size) {
                drawCentred(batch, land.get(dust), x, y);
            }
            int light = DroneLooks.lightsFrame(seconds, lights.size);
            if (light >= 0) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                drawCentred(batch, lights.get(light), x, y);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }
}
