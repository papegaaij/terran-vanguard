package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.SwarmCall;

/**
 * M11 part A, step A3b: the Vrell Swarm Call as the player sees it (design/player/specials, Vrell Swarm Call (L46); round
 * 88 QA-1; tools/art/act7_gear.py {@code swarm}, whose docstring gives the sprites). The seed-pod bloom opens behind the
 * ship ({@code -bloom}, additive, its frames over the call's bloom share); each drone is its soft white-cyan glow
 * (additive, under it), its faint trail turned away from its heading (additive, its point on the drone's centre) and its
 * body ({@code -drone}: {@value #HEADINGS} headings × {@value #CLAWS} claw frames, the claws opening {@value
 * #CLAW_FRAME_TICKS} steps a frame, snapping shut for {@value #SNAP_TICKS} steps as a thorn leaves); as the drones wither
 * their alpha fades and the {@code -wither} frames play over each. A drone shot down bursts into the wither frames
 * played twice as fast (the screen's effects, {@link #burst}). The drones carry no brackets and no banner. Between two
 * steps the drones are drawn where they were and where they are, in proportion.
 */
public final class SwarmLooks {
    static final String ROOT = "vrell-swarm-call";
    static final int HEADINGS = 16;
    static final int CLAWS = 4;
    static final int CLAW_FRAME_TICKS = 8;
    static final int SNAP_TICKS = 4;
    /** The trail's point on the drone's centre: its top middle (12 × 30, y down (6, 0)). */
    static final float TRAIL_X = 6;
    /** A drone that moved further than this in one step (a new call) is drawn where it is. */
    static final double JUMP = 40;
    /** A burst's frames run this many steps each (the wither's 4, twice as fast). */
    public static final int BURST_FRAME_TICKS = 2;

    private final Array<AtlasRegion> drones;
    private final AtlasRegion glow;
    private final AtlasRegion trail;
    private final Array<AtlasRegion> wither;
    private final Array<AtlasRegion> bloom;
    private final long[] snapped = new long[8];
    private final double[] lastX = new double[8];
    private final double[] lastY = new double[8];
    private final double[] nowX = new double[8];
    private final double[] nowY = new double[8];

    SwarmLooks(
            Array<AtlasRegion> drones,
            AtlasRegion glow,
            AtlasRegion trail,
            Array<AtlasRegion> wither,
            Array<AtlasRegion> bloom) {
        this.drones = drones;
        this.glow = glow;
        this.trail = trail;
        this.wither = wither;
        this.bloom = bloom;
        restart();
    }

    /** The looks for a sortie with the Swarm Call fitted; null without it or its art. */
    static SwarmLooks of(Sprites sprites, Sortie sortie) {
        if (sortie.special().swarm() == null || !sprites.has(ROOT + "-drone")) {
            return null;
        }
        return new SwarmLooks(
                sprites.frames(ROOT + "-drone"),
                sprites.has(ROOT + "-drone-glow") ? sprites.region(ROOT + "-drone-glow") : null,
                sprites.has(ROOT + "-trail") ? sprites.region(ROOT + "-trail") : null,
                sprites.has(ROOT + "-wither") ? sprites.frames(ROOT + "-wither") : null,
                sprites.has(ROOT + "-bloom") ? sprites.frames(ROOT + "-bloom") : null);
    }

    /** A drone's burst as it is shot down: the wither frames twice as fast; null without them. */
    public Array<AtlasRegion> burst() {
        return wither;
    }

    /** The drone frame for a heading in radians clockwise from straight up (the art's: from straight down). */
    static int heading(double radians) {
        return EnemyLooks.heading(radians - Math.PI, HEADINGS);
    }

    /** The claw frame {@code age} steps after the drone's last thorn: the snap, then the claws opening on their cycle. */
    static int claw(long age) {
        if (age >= 0 && age < SNAP_TICKS) {
            return CLAWS - 1;
        }
        return (int) (Math.floorMod(age, (long) (CLAWS - 1) * CLAW_FRAME_TICKS) / CLAW_FRAME_TICKS);
    }

    /** The level restarts. */
    void restart() {
        Arrays.fill(snapped, Long.MIN_VALUE / 2);
        Arrays.fill(nowX, Double.NaN);
    }

    /** After each simulation step: the thorns' snaps; the drones' places before and after it. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.SWARM_THORN && events.value(e) < snapped.length) {
                snapped[events.value(e)] = sortie.tick();
            }
        }
        SwarmCall call = sortie.special().swarm();
        for (int d = 0; d < call.drones() && d < nowX.length; d++) {
            double x = call.x(d);
            double y = call.y(d);
            boolean jumped = Double.isNaN(nowX[d]) || Math.abs(x - nowX[d]) + Math.abs(y - nowY[d]) > JUMP;
            lastX[d] = jumped ? x : nowX[d];
            lastY[d] = jumped ? y : nowY[d];
            nowX[d] = x;
            nowY[d] = y;
        }
    }

    /** Over the shots, under the ship (the caller's normal blend, restored): the bloom, the drones and their wither. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        SwarmCall call = sortie.special().swarm();
        if (call == null || !call.active()) {
            return;
        }
        long tick = sortie.tick();
        float fade = (float) (1 - call.withered());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (bloom != null && call.blooming()) {
            int frame = Math.min(bloom.size - 1, (int) (call.bloom() * bloom.size));
            drawCentred(batch, bloom.get(frame), call.bloomX(), call.bloomY(), 0);
        }
        for (int d = 0; d < call.drones() && d < nowX.length; d++) {
            if (!call.alive(d)) {
                continue;
            }
            double x = x(d, alpha);
            double y = y(d, alpha);
            batch.setColor(fade, fade, fade, 1);
            if (trail != null) {
                batch.draw(
                        trail,
                        (float) (PixelScreen.PLAY_FIELD_X + x - TRAIL_X),
                        (float) (y - trail.getRegionHeight()),
                        TRAIL_X,
                        trail.getRegionHeight(),
                        trail.getRegionWidth(),
                        trail.getRegionHeight(),
                        1,
                        1,
                        (float) -Math.toDegrees(call.heading(d)));
            }
            if (glow != null) {
                drawCentred(batch, glow, x, y, 0);
            }
            batch.setColor(Color.WHITE);
            if (wither != null && call.withered() > 0) {
                int frame = Math.min(wither.size - 1, (int) (call.withered() * wither.size));
                drawCentred(batch, wither.get(frame), x, y, 0);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1, 1, 1, fade);
        for (int d = 0; d < call.drones() && d < nowX.length; d++) {
            if (!call.alive(d)) {
                continue;
            }
            int frame = heading(call.heading(d)) * CLAWS + claw(tick - snapped[d]);
            drawCentred(batch, drones.get(frame % drones.size), x(d, alpha), y(d, alpha), 0);
        }
        batch.setColor(Color.WHITE);
    }

    private double x(int d, float alpha) {
        return Double.isNaN(nowX[d]) ? 0 : lastX[d] + (nowX[d] - lastX[d]) * alpha;
    }

    private double y(int d, float alpha) {
        return Double.isNaN(nowY[d]) ? 0 : lastY[d] + (nowY[d] - lastY[d]) * alpha;
    }

    private static void drawCentred(SpriteBatch batch, TextureRegion region, double x, double y, int dy) {
        batch.draw(region, (float) Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2.0), (float)
                Math.round(y + dy - region.getRegionHeight() / 2.0));
    }
}
