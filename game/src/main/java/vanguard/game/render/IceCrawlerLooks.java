package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import java.util.Set;
import vanguard.content.LevelData;

/**
 * M8 part B: a drop carried by a ground vehicle (design/campaign Level 22: Conamara's ice-crawler, a tracked survey
 * vehicle in the CDF supply drone's role; tools/art/l22_props.py's sprites and assets/pivots/ice-crawler.json): its
 * {@code drops:} row's {@code sprite} names the vehicle. Presentation only, nobody can hit it. It drives up the screen
 * from {@value #BELOW_EDGE} px below the bottom edge under the drop's point over the {@value #DRIVE_SECONDS} s before
 * the drop, the armour patch in its cargo bed (at the bed's pivot), releases it there as the simulation places the
 * pickup, then turns and drives back down off the bottom edge. Its treads run at {@value #TRACK_FPS} fps while it
 * drives, its amber roof beacon flashes ({@value #BEACON_SECONDS} s every {@value #BEACON_EVERY} s) over its
 * headlights, which point the way it drives (additive). On the ground, under the ground units; all on the level clock.
 */
public final class IceCrawlerLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The drops carried by a ground vehicle: the vehicles' sprite roots (others fly, {@link DroneLooks}). */
    static final Set<String> VEHICLES = Set.of("ice-crawler");
    /** Each way, s: up to the drop's point before the drop, back down after it. */
    static final double DRIVE_SECONDS = 4;
    /** Where the drive starts and ends below the bottom edge, px (its 48 px sprite off the screen). */
    static final float BELOW_EDGE = 30;
    /** The treads' 3 frames a heading run at this rate while it drives. */
    static final double TRACK_FPS = 12;

    static final int TRACK_FRAMES = 3;
    /** The beacon's flash: this long, every this often, s. */
    static final double BEACON_EVERY = 0.9;

    static final double BEACON_SECONDS = 0.15;
    /** The patch in the bed is the pickup's sprite at this scale. */
    static final float PATCH_SCALE = 0.75f;
    /** The art's headings: 0 driving up the screen, 1 down. */
    static final int UP = 0;

    static final int DOWN = 1;

    private final List<LevelData.Drop> drops;
    private final Array<AtlasRegion> body;
    private final Array<AtlasRegion> lights;
    private final AtlasRegion patch;
    /** Per heading the cargo bed's centre from the frame's centre, px, y up ({@code [heading][0]} x). */
    private final double[][] bed;

    private IceCrawlerLooks(
            List<LevelData.Drop> drops,
            Array<AtlasRegion> body,
            Array<AtlasRegion> lights,
            AtlasRegion patch,
            double[][] bed) {
        this.drops = drops;
        this.body = body;
        this.lights = lights;
        this.patch = patch;
        this.bed = bed;
    }

    /** Whether a drop is carried by a ground vehicle (its {@code sprite} one of {@link #VEHICLES}). */
    public static boolean carried(LevelData.Drop drop) {
        return drop.sprite().filter(VEHICLES::contains).isPresent();
    }

    /** The level's ground-carried drops' looks, or null without such drops (or without the vehicle's sprites). */
    static IceCrawlerLooks of(Sprites sprites, LevelData level, JsonValue pivots) {
        List<LevelData.Drop> drops = level.released().stream()
                .filter(IceCrawlerLooks::carried)
                .filter(drop -> drop.sprite().filter(sprites::has).isPresent())
                .toList();
        if (drops.isEmpty()) {
            return null;
        }
        String slug = drops.getFirst().sprite().orElseThrow();
        Array<AtlasRegion> body = sprites.frames(slug);
        return new IceCrawlerLooks(
                drops,
                body,
                sprites.has(slug + "-lights") ? sprites.frames(slug + "-lights") : new Array<>(0),
                sprites.armourPatch.first(),
                bedOffsets(pivots, body.isEmpty() ? 48 : body.first().getRegionWidth()));
    }

    /** The bed's centre per heading from the frame's centre, px, y up, from the pivot file ({@code bed}). */
    static double[][] bedOffsets(JsonValue pivots, double frame) {
        double[][] offsets = new double[2][2];
        JsonValue bed = pivots == null ? null : pivots.get("bed");
        if (bed == null) {
            return offsets;
        }
        for (int h = 0; h < 2 && h < bed.size; h++) {
            offsets[h][0] = bed.get(h).getDouble(0) - frame / 2;
            offsets[h][1] = frame / 2 - bed.get(h).getDouble(1);
        }
        return offsets;
    }

    /**
     * Where the vehicle is {@code since} s after its drop (negative before it), y up: from {@link #BELOW_EDGE} px below
     * the bottom edge up to the drop's point ({@code dropX}, {@code dropY}) over {@link #DRIVE_SECONDS} s, easing in
     * and out, then back down the same way. Returns NaN outside the drive.
     */
    static double y(double since, double dropY) {
        if (!(since >= -DRIVE_SECONDS) || since > DRIVE_SECONDS) {
            return Double.NaN;
        }
        double u = since <= 0 ? (since + DRIVE_SECONDS) / DRIVE_SECONDS : 1 - since / DRIVE_SECONDS;
        u = u * u * (3 - 2 * u);
        return -BELOW_EDGE + u * (dropY + BELOW_EDGE);
    }

    /** Its heading {@code since} s after its drop: up the screen before it, down after it. */
    static int heading(double since) {
        return since <= 0 ? UP : DOWN;
    }

    /** Its body's frame at real {@code seconds}: its heading's 3 tread frames at {@link #TRACK_FPS}. */
    static int bodyFrame(int heading, double seconds, int frames) {
        int perHeading = Math.max(1, frames / 2);
        return heading * perHeading + (int) Math.floorMod((long) Math.floor(seconds * TRACK_FPS), (long) perHeading);
    }

    /** Its lights' frame at real {@code seconds}: the beacon's flash with the headlights, else the headlights alone. */
    static int lightsFrame(int heading, double seconds, int frames) {
        if (frames < 2) {
            return frames - 1;
        }
        double into = seconds - Math.floor(seconds / BEACON_EVERY) * BEACON_EVERY;
        int perHeading = Math.max(1, frames / 2);
        return Math.min(frames - 1, heading * perHeading + (into < BEACON_SECONDS ? 0 : perHeading - 1));
    }

    /** The vehicles driving at level time {@code seconds}, on the ground. */
    void draw(SpriteBatch batch, double seconds) {
        for (LevelData.Drop drop : drops) {
            double since = seconds - drop.t();
            double y = y(since, drop.y());
            if (Double.isNaN(y)) {
                continue;
            }
            int heading = heading(since);
            AtlasRegion frame = body.get(bodyFrame(heading, seconds, body.size) % body.size);
            float x = Math.round(X0 + drop.x() - frame.getRegionWidth() / 2f);
            float bottom = Math.round(y - frame.getRegionHeight() / 2f);
            batch.draw(frame, x, bottom);
            if (since < 0) {
                float w = patch.getRegionWidth() * PATCH_SCALE;
                float h = patch.getRegionHeight() * PATCH_SCALE;
                batch.draw(
                        patch,
                        Math.round(X0 + drop.x() + bed[heading][0] - w / 2),
                        Math.round(y + bed[heading][1] - h / 2),
                        w,
                        h);
            }
            int light = lightsFrame(heading, seconds, lights.size);
            if (light >= 0) {
                AtlasRegion glow = lights.get(light);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.draw(
                        glow,
                        Math.round(X0 + drop.x() - glow.getRegionWidth() / 2f),
                        Math.round(y - glow.getRegionHeight() / 2f));
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /** The drops it carries (for the tests). */
    List<LevelData.Drop> drops() {
        return drops;
    }
}
