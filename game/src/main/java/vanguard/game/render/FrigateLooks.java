package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import vanguard.sim.CruiserRun;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;

/**
 * M10 part H, step H4a: the CDF frigate pair beside a cruiser run (design/campaign/act-6-jovian-storm/level-42-ascendant,
 * Layout: "a CDF frigate pair trading fire with cruiser A ... friendly marker"; the run's {@code frigates}; sprites
 * {@code cdf-frigate*} and assets/pivots/cdf-frigate.json from tools/art/l42_props.py). The two white frigates fly on
 * the flank across from the run's cruiser ({@link #FRIGATE_X} from the far edge, {@link #FRIGATE_DY} from the cruiser's
 * height, staggered so their hulls stay apart), overtaking and falling back with it, bow up, their engines and strobes
 * glowing ({@code -glow}, additive, {@value #GLOW_FPS} frames a second). While the cruiser runs solid their twin turrets
 * fire tracer bursts at its inner edge ({@code -muzzle} at the muzzles, {@code -tracer} dashes flying across, the
 * cruiser's {@code -spark} where they land) and the cruiser answers with its point-defence flash at its inner edge and a
 * spark on the frigate. Presentation only: the tracers hit nothing, and nothing hits them, so they carry no friendly
 * brackets (step R79c, the user's rule: only craft the player can hit and that must survive). Nothing is allocated per
 * frame.
 */
final class FrigateLooks {
    static final String SLUG = "cdf-frigate";
    /** The frigates' centres from the far edge, px, and from the cruiser's centre height, px (y up). */
    static final double[] FRIGATE_X = {108, 44};

    static final double[] FRIGATE_DY = {70, -60};
    /** The marks' half size round a frigate (its hull about 40 x 132 px). */
    static final int HALF_WIDTH = 20;

    static final int HALF_HEIGHT = 66;
    /** The engines' and strobes' glow, frames a second. */
    static final double GLOW_FPS = 4;
    /** A turret's burst: every this long, its dashes this far apart in time, flying this fast, px/s. */
    static final double BURST_SECONDS = 1.3;

    static final double DASH_SECONDS = 0.08;
    static final int DASHES = 3;
    static final double TRACER_SPEED = 600;
    /** The muzzle flash and the sparks at 20 fps. */
    static final double FLASH_FPS = 20;
    /** The cruiser's answer: every this long per frigate, its spark on the frigate this much later. */
    static final double ANSWER_SECONDS = 1.7;

    static final double ANSWER_SPARK = 0.25;

    private final CruiserRunSpec spec;
    private final AtlasRegion frigate;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> muzzle;
    private final AtlasRegion tracer;
    private final Array<AtlasRegion> spark;
    private final Array<AtlasRegion> pd;
    /** The muzzles in the sprite (px from the top left, y down): two per turret, the bow turret's first. */
    private final float[][] muzzles;

    private final float[] at = new float[2];

    private FrigateLooks(Sprites sprites, CruiserRunSpec spec, JsonValue pivots) {
        this.spec = spec;
        frigate = sprites.region(SLUG);
        glow = sprites.has(SLUG + "-glow") ? sprites.frames(SLUG + "-glow") : new Array<>(0);
        muzzle = sprites.has(SLUG + "-muzzle") ? sprites.frames(SLUG + "-muzzle") : new Array<>(0);
        tracer = sprites.has(SLUG + "-tracer") ? sprites.region(SLUG + "-tracer") : null;
        spark = sprites.has(CruiserLooks.SLUG + "-spark")
                ? sprites.frames(CruiserLooks.SLUG + "-spark")
                : new Array<>(0);
        pd = sprites.has(CruiserLooks.SLUG + "-pd") ? sprites.frames(CruiserLooks.SLUG + "-pd") : new Array<>(0);
        JsonValue list = pivots == null ? null : pivots.get("muzzles");
        muzzles = new float[list == null ? 0 : list.size][];
        int k = 0;
        for (JsonValue m = list == null ? null : list.child; m != null; m = m.next, k++) {
            muzzles[k] = m.asFloatArray();
        }
    }

    /** The level's frigates' looks: a cruiser run with its frigate pair and their sprites; else null. */
    static FrigateLooks of(Sprites sprites, LevelScript script, JsonValue pivots) {
        if (script.hazards().cruisers().isEmpty()
                || script.hazards().cruisers().get().runs().stream().noneMatch(CruiserRunSpec.Run::frigates)
                || !sprites.has(SLUG)) {
            return null;
        }
        return new FrigateLooks(sprites, script.hazards().cruisers().get(), pivots);
    }

    /**
     * Where frigate {@code f} (0 or 1) flies at level clock step {@code at} beside the run of the hull on its far side,
     * into {@code out} (x, y up; beside the cruiser overtaking in open space, {@link CruiserLooks#overtakeY}): whether it
     * is on the screen then.
     */
    static boolean place(CruiserRun run, int at, int f, float[] out) {
        for (int side = 0; side < 2; side++) {
            if (!run.present(side, at) || !run.frigates(side, at)) {
                continue;
            }
            // The frigates fly on the other flank, across from their cruiser.
            out[0] = (float) (side == CruiserRun.LEFT ? vanguard.sim.PlayField.WIDTH - FRIGATE_X[f] : FRIGATE_X[f]);
            double y = CruiserLooks.overtakeY(
                    run.hullY(side, at),
                    run.riseShare(side, at),
                    run.spec().hull().height());
            out[1] = (float) (y + FRIGATE_DY[f]);
            return out[1] + HALF_HEIGHT > 0 && out[1] - HALF_HEIGHT < vanguard.sim.PlayField.HEIGHT;
        }
        return false;
    }

    /** The side of the cruiser the frigates trade fire with now, -1 for none (and whether it runs solid). */
    private static int target(CruiserRun run, int at) {
        for (int side = 0; side < 2; side++) {
            if (run.present(side, at) && run.frigates(side, at)) {
                return side;
            }
        }
        return -1;
    }

    /** The phase of a frigate's turret's burst at {@code seconds} on the level clock: s since its last burst began. */
    static double burstAge(double seconds, int f, int turret) {
        double phase = f * 0.55 + turret * 0.3;
        double age = (seconds - phase) % BURST_SECONDS;
        return age < 0 ? age + BURST_SECONDS : age;
    }

    /** On {@code low-air} over the cruisers: the frigates, their glows, their bursts and the cruiser's answer. */
    void draw(SpriteBatch batch, Sortie sortie) {
        CruiserRun run = sortie.cruisers().orElse(null);
        if (run == null) {
            return;
        }
        int at = CruiserLooks.levelTick(sortie);
        int side = target(run, at);
        if (side < 0) {
            return;
        }
        double seconds = sortie.levelSeconds();
        batch.setColor(Color.WHITE);
        for (int f = 0; f < 2; f++) {
            if (place(run, at, f, this.at)) {
                drawCentred(batch, frigate, this.at[0], this.at[1]);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        boolean firing = run.solid(side, at);
        double inner = side == CruiserRun.LEFT ? spec.leftInner() : spec.rightInner();
        // The frigates fire toward their cruiser: to the left when it is on the left flank.
        int toward = side == CruiserRun.LEFT ? -1 : 1;
        float w = frigate.getRegionWidth();
        float h = frigate.getRegionHeight();
        for (int f = 0; f < 2; f++) {
            if (!place(run, at, f, this.at)) {
                continue;
            }
            double fx = this.at[0];
            double fy = this.at[1];
            if (!glow.isEmpty()) {
                drawCentred(batch, glow.get((int) Math.floor(seconds * GLOW_FPS) % glow.size), fx, fy);
            }
            if (!firing) {
                continue;
            }
            for (int m = 0; m < muzzles.length; m += 2) {
                double age = burstAge(seconds, f, m / 2);
                double mx = fx - w / 2 + muzzles[m][0];
                double my = fy + h / 2 - muzzles[m][1];
                int flash = (int) Math.floor(age * FLASH_FPS);
                if (flash < muzzle.size) {
                    drawCentred(batch, muzzle.get(flash), mx, my);
                }
                double reach = Math.abs(mx - inner);
                for (int d = 0; d < DASHES; d++) {
                    double flown = (age - d * DASH_SECONDS) * TRACER_SPEED;
                    if (flown < 0) {
                        continue;
                    }
                    if (flown <= reach && tracer != null) {
                        float tw = tracer.getRegionWidth();
                        float th = tracer.getRegionHeight();
                        // The tracer's head is to its left: flipped when it flies right.
                        batch.draw(
                                tracer,
                                Math.round(X0 + mx + toward * flown - tw / 2),
                                Math.round(my - th / 2),
                                tw / 2,
                                th / 2,
                                tw,
                                th,
                                -toward,
                                1,
                                0);
                    } else {
                        int s = (int) Math.floor((flown - reach) / TRACER_SPEED * FLASH_FPS);
                        if (s < spark.size) {
                            drawCentred(batch, spark.get(s), inner, my);
                        }
                    }
                }
            }
            // The cruiser answers: its point defence at its inner edge, a spark on the frigate a moment later.
            double answer = (seconds - f * 0.8) % ANSWER_SECONDS;
            int pf = (int) Math.floor(answer * FLASH_FPS);
            if (answer >= 0 && pf < pd.size) {
                drawCentred(batch, pd.get(pf), inner - toward * 6, fy + 20);
            }
            int sf = (int) Math.floor((answer - ANSWER_SPARK) * FLASH_FPS);
            if (answer >= ANSWER_SPARK && sf < spark.size) {
                drawCentred(batch, spark.get(sf), fx + toward * HALF_WIDTH * 0.6, fy + 20);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
