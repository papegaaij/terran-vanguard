package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.Beam;
import vanguard.sim.BossSpec;
import vanguard.sim.Missile;
import vanguard.sim.Missiles;
import vanguard.sim.PartSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Ship;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * M7 part F: the walking arena boss's looks (the Revenant Walker, design/enemies/bosses/revenant-walker;
 * tools/art/revenant_walker.py's sprites and assets/pivots/revenant-walker.json), drawn on the ground layer under
 * the ground units ({@link #drawGround}): its reverse-knee legs in their 8-frame walk at its 8 headings (stepping on
 * the spot in a turn), the hull, the reactor vents shut, open hot amber (after each burn) or overheated white-orange
 * (its last phase), the tail flamer turned along the cone's axis (32 aims), the shoulder pods each turned toward the
 * ship (16 headings) with the hatch open over its salvo's tell, a destroyed pod's torn stub, a hit part flashing a
 * third of the way to white at most once each 0.25 s ({@link HitFlash}, as the Kraken's parts: round 42, it turned
 * into a white silhouette under fire), the beam's heat building and cooling smoothly on the part it burns (on the hull
 * a glow where the beam bores in, a pod tinted: round 42, the whole hull flickered tan), a shot glancing off the
 * front plate lighting the plate; the footfall's
 * dust at its feet. At its death the topple over its 2 s death (6 steps a heading), then the wreck (flank up, the
 * filed serial plate) that stays as scenery: carried down with the ground's scroll once it resumes (the crawl) and
 * up a conveyor belt it lies on, which the simulation does not do for the game (risk 7), smoking.
 *
 * <p>Over the ground units ({@link #drawCone}): the flame cone at the simulation's length and angle, over the tell
 * its outline (both edges and the far arc) with a faint fill and the pilot light flaring blue-white, then the burn: a
 * dark smoke underlay so it reads over the orange foundry floor, the whole cone filled with flame (hot near the
 * nozzle, deep orange at its end) and the flame puffs streaming from the nozzle along the axis over it (round 42: the
 * puffs alone read as sparse sparks). With the bullets ({@link #drawMissiles}): the homing missiles at the nearest of
 * their 16 headings, drawn half again as large and brightened with a hot exhaust, over their long grey smoke trails
 * (round 42: tiny grey streaks). Round 44: the rail lance and the stomp ({@link StrikeLooks}), the stomp's rings with
 * the cone, the lance with the missiles.
 */
final class WalkerLooks {
    /** The body's headings (45° steps clockwise from straight down). */
    static final int HEADINGS = 8;
    /** The walk cycle's frames per heading (two footfalls). */
    static final int WALK_FRAMES = 8;
    /** The vents' states per heading: shut, open, overheated. */
    static final int VENT_STATES = 3;
    /** The pods' aim headings (22.5° steps) and the flamer's (11.25° steps). */
    static final int POD_AIMS = 16;

    static final int FLAMER_AIMS = 32;
    /** The topple's steps per heading over the death. */
    static final int TOPPLE_STEPS = 6;
    /** The missile's headings. */
    static final int MISSILE_HEADINGS = 16;
    /** The pilot light's frames: dim, two steps, the blue-white flare. */
    static final int PILOT_FRAMES = 4;
    /** A shot glancing off the plate lights it for this many steps, fading. */
    static final int GLANCE_TICKS = 6;
    /** A shot glances off the plate when it lands within this of the plate's box, px. */
    static final double GLANCE_REACH = 14;
    /** A smoke or dust puff's frame shows this many steps (6 frames: 0.5 s). */
    static final int PUFF_FRAME_TICKS = 5;
    /** A missile's trail puff's frame shows this many steps (6 frames: 0.9 s, a long trail). */
    static final int TRAIL_FRAME_TICKS = 9;
    /** A missile leaves a smoke puff every this many steps. */
    static final int TRAIL_EVERY_TICKS = 2;
    /** A missile is drawn this much larger than its sprite, and brightened by this much of itself added over it. */
    static final float MISSILE_SCALE = 1.5f;

    static final float MISSILE_SHINE = 0.35f;
    /** A smoke puff sits this far behind the missile's centre, px (its exhaust a little nearer). */
    static final double TRAIL_BEHIND = 14;

    private static final double EXHAUST_BEHIND = 12;
    /**
     * The beam's heat on a part: it builds to full over this many steps under the beam and cools over this many off it,
     * so a beam slipping on and off for a few steps no longer flickers the tint (round 42).
     */
    static final int HEAT_BUILD_TICKS = SimStep.ticks(0.5);

    static final int HEAT_COOL_TICKS = SimStep.ticks(1.0);
    /** On the hull the heat is a glow this large where the beam bores in, at most this strong (additive). */
    static final double HEAT_RADIUS = 26;

    static final float HEAT_GLOW = 0.8f;
    /** A part this hot shows its heat instead of the hit flash (the beam hits it every step). */
    static final double HEAT_QUIET = 0.05;
    /** A footfall's dust: the feet this far beside the centre, px, a few puffs each. */
    static final double FOOT_SIDE = 36;

    private static final int FOOT_PUFFS = 3;
    /** The wreck smokes: a puff every this many steps while it is on the screen. */
    static final int WRECK_SMOKE_TICKS = 9;
    /** The burn's flame puffs at once at step F3's cone length (the smoke underlay's puffs count by these). */
    static final int FLAME_PUFFS = 30;
    /** The burn's flame puffs over its fill. */
    static final int BURN_PUFFS = 48;
    /** The flame sprite's bright frames (its later ones are dark smoke). */
    static final int BRIGHT_FLAME_FRAMES = 5;

    static final double FLAME_LIFE_SECONDS = 0.45;
    /** The tell's outline: a flame puff every this many px along each edge. */
    private static final double OUTLINE_STEP = 14;
    /** The puffs remembered at once (the missiles' trails, the feet's dust, the wreck's smoke). */
    private static final int PUFFS = 512;
    /** The cone's fill: slices across it, rings along it (round 43: finer for the bigger cone). */
    private static final int CONE_SLICES = 14;

    private static final int CONE_RINGS = 10;
    /** The cone length its puffs' sizes and counts were made for (step F3's 150 px), px. */
    private static final double DRAWN_CONE_LENGTH = 150;
    /** The tell's outline: its width, px. */
    private static final double OUTLINE_WIDTH = 2.5;

    private static final Color DUST = new Color(0.78f, 0.52f, 0.38f, 0.75f);
    private static final Color SMOKE = new Color(0.7f, 0.68f, 0.66f, 0.8f);
    private static final Color EXHAUST = new Color(1f, 0.62f, 0.25f, 1f);
    private static final Color EXHAUST_CORE = new Color(1f, 0.95f, 0.8f, 1f);
    private static final Color HEAT_CORE = new Color(1f, 0.9f, 0.7f, 1f);
    private static final Color FLAME = new Color(1f, 0.55f, 0.15f, 1f);
    private static final Color WRECK_SMOKE = new Color(0.28f, 0.26f, 0.25f, 0.55f);
    /** The heat's tint under a beam, as the other set pieces'. */
    private static final long NEVER = Long.MIN_VALUE / 2;

    private final FlashShader flash;
    private final VertexFills fills;
    private final ConeShade coneShade = new ConeShade();
    private final HitFlash hits = new HitFlash();
    /** Round 44: its rail lance and stomp. */
    private final StrikeLooks strikes;

    private final Array<AtlasRegion> legs;
    private final Array<AtlasRegion> hull;
    private final Array<AtlasRegion> plate;
    private final Array<AtlasRegion> vents;
    private final Array<AtlasRegion> pod;
    private final Array<AtlasRegion> podStub;
    private final Array<AtlasRegion> flamer;
    private final Array<AtlasRegion> pilot;
    private final Array<AtlasRegion> topple;
    private final Array<AtlasRegion> wreck;
    private final Array<AtlasRegion> missile;
    private final Array<AtlasRegion> smoke;
    private final Array<AtlasRegion> flame;
    /** Per heading the pivots, px from its centre, y up: the pods (by part), the vents, the flamer. */
    private final double[][][] podAt;

    private final double[][] ventAt;
    private final double[][] flamerAt;
    /** The nozzle's distance along the flamer's aim, px. */
    private final double nozzle;
    /** Per part the art's pod index (0 or 1), -1 for the hull. */
    private final int[] podIndex;
    /** The cone's length and half angle (radians), from the boss's data. */
    private final double coneLength;

    private final double coneHalf;
    /**
     * Round 43 (the bigger cone, K.a of round 42): its puffs grow with its length over {@link #DRAWN_CONE_LENGTH} (1–2),
     * and their counts with its area.
     */
    private final double puffScale;

    private final int flamePuffs;
    private final int burnPuffs;
    private final int hullPart;
    /** The steps its death takes (the topple). */
    private final int deathTicks;

    /** The step the last shot glanced off the plate. */
    private long glanceTick = NEVER;
    /** Per part the beam's heat (0–1), and where it bores into the hull, px from its centre, y up. */
    private final double[] heat;

    private double heatX;
    private double heatY;
    /** The step a foot came down last, and which foot. */
    private int foot;
    /** Its death: the step, where (and the ground's scroll there), its heading; NEVER while it lives. */
    private long deathTick = NEVER;

    private double deathX;
    private double deathY;
    private double deathScroll;
    private int deathHeading;
    /** How far a belt has carried the wreck up the screen since its death, px. */
    private double beltCarried;
    /** The puffs: where (y on the ground when {@code puffGround}: plus the scroll), born when, of which kind. */
    private final double[] puffX = new double[PUFFS];

    private final double[] puffY = new double[PUFFS];
    private final long[] puffTick = new long[PUFFS];
    private final byte[] puffKind = new byte[PUFFS];
    private int nextPuff;

    private static final byte TRAIL = 0;
    private static final byte FOOT = 1;
    private static final byte WRECK = 2;

    private WalkerLooks(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        this.flash = flash;
        fills = new VertexFills(sprites.pixel);
        strikes = new StrikeLooks(fills);
        heat = new double[boss.parts().size()];
        String slug = boss.slug();
        legs = sprites.frames(slug + "-legs");
        hull = sprites.frames(slug);
        plate = sprites.frames(slug + "-plate");
        vents = sprites.frames(slug + "-vents");
        pod = sprites.frames(slug + "-pod");
        podStub = sprites.frames(slug + "-pod-stub");
        flamer = sprites.frames(slug + "-flamer");
        pilot = sprites.frames(slug + "-pilot");
        topple = sprites.frames(slug + "-topple");
        wreck = sprites.frames(slug + "-wreck");
        missile = sprites.frames(slug + "-missile");
        smoke = sprites.frames(slug + "-smoke");
        flame = sprites.frames(slug + "-flame");
        podAt = new double[HEADINGS][2][];
        ventAt = new double[HEADINGS][];
        flamerAt = new double[HEADINGS][];
        for (int k = 0; k < HEADINGS; k++) {
            podAt[k][0] = point(pivots.get("pods").get(k).get(0));
            podAt[k][1] = point(pivots.get("pods").get(k).get(1));
            ventAt[k] = point(pivots.get("vents").get(k));
            flamerAt[k] = point(pivots.get("flamer").get(k));
        }
        nozzle = along(pivots.get("nozzle"));
        int hullAt = 0;
        podIndex = new int[boss.parts().size()];
        for (int p = 0; p < podIndex.length; p++) {
            PartSpec part = boss.parts().get(p);
            if (part.vital()) {
                podIndex[p] = -1;
                hullAt = p;
            } else {
                podIndex[p] = podIndex(part.dx(), podAt[0][0][0], podAt[0][1][0]);
            }
        }
        hullPart = hullAt;
        BossSpec spec = boss.boss().orElseThrow();
        coneLength = spec.cone().map(BossSpec.Cone::length).orElse(0.0);
        coneHalf = spec.cone().map(BossSpec.Cone::halfAngleRadians).orElse(0.0);
        puffScale = puffScale(coneLength);
        flamePuffs = (int) Math.round(FLAME_PUFFS * puffScale * puffScale);
        burnPuffs = (int) Math.round(BURN_PUFFS * puffScale * puffScale);
        deathTicks = Math.max(1, SimStep.ticks(spec.deathSeconds()));
        reset();
    }

    /**
     * The looks of the level's boss when it walks (its data's {@code movement.stride}) and its sprites and pivots are
     * there; null otherwise.
     */
    static WalkerLooks of(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        if (boss == null
                || pivots == null
                || boss.boss().flatMap(BossSpec::stride).isEmpty()
                || !sprites.has(boss.slug() + "-legs")
                || !sprites.has(boss.slug() + "-topple")
                || !pivots.has("pods")) {
            return null;
        }
        return new WalkerLooks(sprites, flash, boss, pivots);
    }

    /** Whether set piece {@code piece} is a walking boss, which these looks draw. */
    static boolean drawn(SetPiece piece) {
        return piece.stride().isPresent();
    }

    /** A distance along an aim in the pivot file ({@code [px, "what"]}, or the bare number); 0 without it. */
    static double along(JsonValue value) {
        return value == null ? 0 : value.isArray() ? value.getDouble(0) : value.asDouble();
    }

    /** A pivot's [dx, dy] (y down) as px from the centre, y up. */
    private static double[] point(JsonValue value) {
        return new double[] {value.getDouble(0), -value.getDouble(1)};
    }

    /**
     * The art's pod (0 or 1) for a part at {@code dx} from the centre as it faces down (the data's offset): the one on
     * the same side at heading 0 ({@code art0X}, {@code art1X} its pods' x there).
     */
    static int podIndex(double dx, double art0X, double art1X) {
        return Math.signum(dx) == Math.signum(art0X) || Math.signum(dx) != Math.signum(art1X) ? 0 : 1;
    }

    /** The level restarts (or the fight is retried): no glance, no puff, no wreck. */
    void reset() {
        glanceTick = NEVER;
        deathTick = NEVER;
        beltCarried = 0;
        foot = 0;
        Arrays.fill(puffTick, NEVER);
        nextPuff = 0;
        Arrays.fill(heat, 0);
        hits.reset();
        strikes.reset();
    }

    // --- the frames --------------------------------------------------------------------------------

    /** The legs' frame at {@code heading} (0–7) and {@code walkPhase} (0–1): 8 walk frames a heading. */
    static int legFrame(int heading, double walkPhase) {
        int step = Math.floorMod((int) Math.floor(walkPhase * WALK_FRAMES), WALK_FRAMES);
        return Math.floorMod(heading, HEADINGS) * WALK_FRAMES + step;
    }

    /** The vents' state: 0 shut, 1 open (hot amber), 2 overheated (its last phase, white-orange). */
    static int ventState(boolean open, boolean lastPhase) {
        return !open ? 0 : lastPhase ? 2 : 1;
    }

    /** The vents' frame: 3 states a heading. */
    static int ventFrame(int heading, int state) {
        return Math.floorMod(heading, HEADINGS) * VENT_STATES + state;
    }

    /** A pod's frame: its aim (0–15) and its hatch (open over its salvo's tell). */
    static int podFrame(int aim, boolean hatchOpen) {
        return Math.floorMod(aim, POD_AIMS) * 2 + (hatchOpen ? 1 : 0);
    }

    /** The nearest of {@code count} headings to {@code radians} clockwise from straight down. */
    static int nearest(double radians, int count) {
        return Math.floorMod((int) Math.rint(radians / (2 * Math.PI / count)), count);
    }

    /**
     * The pilot light's frame: dim while cold, its two steps and the blue-white flare over the tell (by its share),
     * the second step while it burns.
     */
    static int pilotFrame(StrideArena.ConePhase phase, double share) {
        return switch (phase) {
            case IDLE -> 0;
            case TELL -> 1 + Math.min(2, (int) Math.floor(share * 3));
            case BURN -> 2;
        };
    }

    /**
     * The topple's frame {@code ageTicks} steps into a death of {@code deathTicks} at {@code heading}: 6 steps a
     * heading; -1 once it is over (the wreck).
     */
    static int toppleFrame(int heading, long ageTicks, int deathTicks) {
        if (ageTicks >= deathTicks) {
            return -1;
        }
        int step = (int) Math.min(TOPPLE_STEPS - 1, Math.max(0, ageTicks) * TOPPLE_STEPS / deathTicks);
        return Math.floorMod(heading, HEADINGS) * TOPPLE_STEPS + step;
    }

    /**
     * The wreck's place on the screen, y up: where it died, carried down by the ground's scroll since ({@code scroll}
     * against {@code deathScroll}) and up by the belt it lies on ({@code carried}).
     */
    static double wreckY(double deathY, double deathScroll, double scroll, double carried) {
        return deathY - (scroll - deathScroll) + carried;
    }

    /** A part's heat after one more step, {@code burning} under the beam or not: building, or cooling (0–1). */
    static double heated(double heat, boolean burning) {
        return burning ? Math.min(1, heat + 1.0 / HEAT_BUILD_TICKS) : Math.max(0, heat - 1.0 / HEAT_COOL_TICKS);
    }

    /** The flame puff {@code i} of {@code count}'s share of its life at real seconds {@code seconds} (0–1). */
    static double flameAge(int i, int count, double seconds) {
        double t = seconds / FLAME_LIFE_SECONDS + (double) i / count;
        return t - Math.floor(t);
    }

    // --- following the simulation ------------------------------------------------------------------

    /**
     * After each simulation step: the plate's glance, the footfalls' dust, the missiles' smoke, its death (where and
     * when, for the topple and the wreck) and the belt carrying the wreck.
     */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        SetPiece piece = walker(sortie);
        if (piece == null) {
            return;
        }
        StrideArena stride = piece.stride().orElseThrow();
        heat(sortie, piece);
        strikes.stepped(sortie, piece);
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case SHOT_GLANCED -> {
                    if (!piece.destroyed() && onPlate(stride, events.x(i), events.y(i))) {
                        glanceTick = tick;
                    }
                }
                case WALKER_STEP -> footfall(stride, events.x(i), events.y(i), tick);
                default -> {}
            }
        }
        Missiles missiles = sortie.missiles();
        if (tick % TRAIL_EVERY_TICKS == 0) {
            for (int m = 0; m < missiles.size(); m++) {
                Missile flying = missiles.get(m);
                double heading = flying.heading();
                puff(
                        flying.x() + Math.sin(heading) * TRAIL_BEHIND,
                        flying.y() + Math.cos(heading) * TRAIL_BEHIND,
                        tick,
                        TRAIL);
            }
        }
        if (piece.destroyed()) {
            if (deathTick == NEVER) {
                deathTick = tick;
                deathX = piece.renderX(1);
                deathY = piece.renderY(1);
                deathScroll = sortie.groundScroll();
                deathHeading = stride.heading();
                beltCarried = 0;
            }
            double y = wreckY(deathY, deathScroll, sortie.groundScroll(), beltCarried);
            beltCarried += sortie.beltSpeedAt(deathX, y) * SimStep.SECONDS;
            if (tick - deathTick >= deathTicks / 2
                    && (tick - deathTick) % WRECK_SMOKE_TICKS == 0
                    && y > -100
                    && y < vanguard.sim.PlayField.HEIGHT + 100) {
                int n = (int) ((tick - deathTick) / WRECK_SMOKE_TICKS);
                puffOnGround(deathX + ((n * 37) % 41) - 20, y + ((n * 23) % 31) - 15, sortie.groundScroll(), tick);
            }
        }
    }

    /**
     * The beam's heat after a step: building on the part a beam burns (the first beam that burns one of the walker's
     * parts), cooling on the others; where it bores into the hull, followed smoothly as the beam sweeps.
     */
    private void heat(Sortie sortie, SetPiece piece) {
        int burning = -1;
        double bx = 0;
        double by = 0;
        if (!piece.destroyed()) {
            for (int m = 0; m < sortie.armament().size() && burning < 0; m++) {
                Beam beam = sortie.beam(m);
                if (beam != null
                        && beam.on()
                        && beam.contact() == Beam.Contact.BURN
                        && beam.target() >= Beam.PART_TARGET
                        && ArsenalLooks.heatPiece(beam.target()) == index(sortie, piece)) {
                    burning = ArsenalLooks.heatPart(beam.target());
                    bx = beam.x() - piece.renderX(1);
                    by = beam.endY() + Beam.BORE - piece.renderY(1);
                }
            }
        }
        if (burning == hullPart) {
            boolean cold = heat[hullPart] < HEAT_QUIET;
            heatX = cold ? bx : heatX + (bx - heatX) * 0.4;
            heatY = cold ? by : heatY + (by - heatY) * 0.4;
        }
        for (int p = 0; p < heat.length; p++) {
            heat[p] = heated(heat[p], p == burning);
        }
    }

    /** The set piece's index of {@code piece} in the sortie. */
    private static int index(Sortie sortie, SetPiece piece) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k) == piece) {
                return k;
            }
        }
        return -1;
    }

    /** The level's walking boss, or null without one. */
    static SetPiece walker(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (drawn(sortie.setPiece(k))) {
                return sortie.setPiece(k);
            }
        }
        return null;
    }

    /** Whether a glance at (x, y) is on the walker's front plate (within its box and a little). */
    static boolean onPlate(StrideArena stride, double x, double y) {
        var box = stride.plateBox();
        if (box == null) {
            return false;
        }
        return Math.abs(x - stride.plateX()) <= box.width() / 2 + GLANCE_REACH
                && Math.abs(y - stride.plateY()) <= box.height() / 2 + GLANCE_REACH;
    }

    /** A foot came down: dust at that foot (they take turns), beside its centre across its heading. */
    private void footfall(StrideArena stride, double x, double y, long tick) {
        double heading = stride.headingRadians();
        // Across the heading (its left as it faces down is the screen's right).
        double side = foot == 0 ? 1 : -1;
        foot = 1 - foot;
        double fx = x + Math.cos(heading) * FOOT_SIDE * side;
        double fy = y - Math.sin(heading) * FOOT_SIDE * side;
        for (int n = 0; n < FOOT_PUFFS; n++) {
            puff(fx + (n - 1) * 7, fy + ((n * 5) % 9) - 4, tick - n, FOOT);
        }
    }

    private void puff(double x, double y, long tick, byte kind) {
        puffX[nextPuff] = x;
        puffY[nextPuff] = y;
        puffTick[nextPuff] = tick;
        puffKind[nextPuff] = kind;
        nextPuff = (nextPuff + 1) % PUFFS;
    }

    private void puffOnGround(double x, double y, double scroll, long tick) {
        puff(x, y + scroll, tick, WRECK);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * On the ground, under the ground units: alive, its legs, hull, vents, flamer and pods with the plate's glance and
     * the footfalls' dust; dead, its topple and then its wreck with its smoke.
     *
     * @param scroll the ground's scroll at the render time
     * @param play the frame's flash reduction
     */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double scroll, PlayFrame play) {
        SetPiece piece = walker(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        StrideArena stride = piece.stride().orElseThrow();
        if (piece.destroyed()) {
            if (deathTick != NEVER) {
                drawDead(batch, tick, alpha, scroll);
            }
        } else if (!stride.offScreen()) {
            Ship ship = sortie.ship();
            drawAlive(batch, piece, stride, alpha, tick, ship.renderX(alpha), ship.renderY(alpha), play.whiteFlash);
        }
        drawPuffs(batch, tick, alpha, scroll, FOOT);
        drawPuffs(batch, tick, alpha, scroll, WRECK);
    }

    private void drawAlive(
            SpriteBatch batch,
            SetPiece piece,
            StrideArena stride,
            float alpha,
            long tick,
            double shipX,
            double shipY,
            float whiteFlash) {
        double x = piece.renderX(alpha);
        double y = piece.renderY(alpha);
        int k = stride.heading();
        drawCentred(batch, legs.get(legFrame(k, stride.walkPhase())), x, y);
        drawPart(batch, piece, hullPart, hull.get(k), x, y, tick, whiteFlash);
        float glow = (float) heat[hullPart];
        if (glow > 0) {
            // The heat where the beam bores in, not the whole hull: a hot spot that builds and cools.
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            fills.disc(batch, x + heatX, y + heatY, HEAT_RADIUS, ArsenalLooks.HEAT, HEAT_GLOW * glow);
            fills.disc(batch, x + heatX, y + heatY, HEAT_RADIUS * 0.4, HEAT_CORE, 0.6f * glow * glow);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        if (tick - glanceTick < GLANCE_TICKS) {
            float share = 1 - (float) (tick - glanceTick) / GLANCE_TICKS;
            flash.draw(batch, plate.get(k), Math.round(X0 + x), Math.round(y), HIT_WHITE, 0.6f * share * whiteFlash);
        }
        boolean last = piece.phase() >= piece.boss().orElseThrow().phases().size() - 1;
        int state = ventState(stride.ventsOpen(), last);
        drawCentred(batch, vents.get(ventFrame(k, state)), x + ventAt[k][0], y + ventAt[k][1]);
        drawCentred(
                batch, flamer.get(nearest(stride.coneHeading(), FLAMER_AIMS)), x + flamerAt[k][0], y + flamerAt[k][1]);
        for (int p = 0; p < podIndex.length; p++) {
            if (podIndex[p] < 0) {
                continue;
            }
            double[] at = podAt[k][podIndex[p]];
            double px = x + at[0];
            double py = y + at[1];
            if (piece.partWrecked(p)) {
                drawCentred(batch, podStub.get(k), px, py);
                continue;
            }
            int aim = StrideArena.bearing16(piece.partX(p), piece.partY(p), shipX, shipY);
            drawPart(batch, piece, p, pod.get(podFrame(aim, piece.partHatchOpen(p))), px, py, tick, whiteFlash);
        }
    }

    /**
     * A part's sprite at (x, y): a pod in the beam's heat tint while it is hot, else a part flashing a third of the way
     * to white after a hit, at most once each 0.25 s ({@link HitFlash}); a hot part (the beam hits it every step) does
     * not flash.
     */
    private void drawPart(
            SpriteBatch batch,
            SetPiece piece,
            int p,
            AtlasRegion region,
            double x,
            double y,
            long tick,
            float whiteFlash) {
        boolean hit = hits.on(p, tick, piece.partTicksSinceHit(p)) && heat[p] < HEAT_QUIET;
        float amount = hitAmount(hit, whiteFlash);
        if (p != hullPart && heat[p] > 0) {
            flash.draw(batch, region, Math.round(X0 + x), Math.round(y), ArsenalLooks.HEAT, (float)
                    (ArsenalLooks.HEAT_AMOUNT * heat[p]));
        } else if (amount > 0) {
            flash.draw(batch, region, Math.round(X0 + x), Math.round(y), HIT_WHITE, amount);
        } else {
            drawCentred(batch, region, x, y);
        }
    }

    /** The hit flash's blend towards white: {@link HitFlash#PART_FLASH} of the frame's white flash, 0 without a hit. */
    static float hitAmount(boolean hit, float whiteFlash) {
        return hit ? HitFlash.PART_FLASH * whiteFlash : 0;
    }

    private void drawDead(SpriteBatch batch, long tick, float alpha, double scroll) {
        double x = deathX;
        double y = wreckY(deathY, deathScroll, scroll, beltCarried);
        int frame = toppleFrame(deathHeading, tick - deathTick, deathTicks);
        drawCentred(batch, frame >= 0 ? topple.get(frame) : wreck.get(Math.floorMod(deathHeading, HEADINGS)), x, y);
    }

    /**
     * The puffs of one kind: grey smoke (a trail, the wreck's) or reddish dust, their frames by their age (a trail's
     * longer).
     */
    private void drawPuffs(SpriteBatch batch, long tick, float alpha, double scroll, byte kind) {
        Color tint = kind == FOOT ? DUST : kind == WRECK ? WRECK_SMOKE : SMOKE;
        float grow = kind == TRAIL ? 1.5f : kind == FOOT ? 1.6f : 2.4f;
        int frameTicks = kind == TRAIL ? TRAIL_FRAME_TICKS : PUFF_FRAME_TICKS;
        for (int i = 0; i < PUFFS; i++) {
            if (puffKind[i] != kind) {
                continue;
            }
            double age = tick + alpha - puffTick[i];
            int frame = (int) (age / frameTicks);
            if (age < 0 || frame >= smoke.size) {
                continue;
            }
            float share = (float) (age / (frameTicks * smoke.size));
            double y = kind == WRECK ? puffY[i] - scroll + age * 0.35 : puffY[i];
            batch.setColor(tint.r, tint.g, tint.b, tint.a * (1 - share * 0.6f));
            drawScaled(batch, smoke.get(frame), puffX[i], y, grow * (1 + share * 0.8f));
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Over the ground units: the flame cone at the simulation's length and half angle from its apex, along its axis.
     * Over the tell its outline (both edges and the far arc, shimmering in faint flame, stronger as the tell runs out)
     * over a faint fill and the pilot light flaring; over the burn a dark smoke underlay, the cone filled with flame
     * (additive: hot near the nozzle, deep orange at its end, softer at its edges) and the flame puffs streaming along
     * its axis over it.
     */
    void drawCone(SpriteBatch batch, Sortie sortie, float alpha, double realSeconds) {
        SetPiece piece = walker(sortie);
        if (piece == null || piece.destroyed()) {
            return;
        }
        StrideArena stride = piece.stride().orElseThrow();
        // Round 44: the stomp's ground ring and its shockwave, under the flame.
        strikes.drawStomp(
                batch,
                sortie,
                piece,
                alpha,
                piece.renderX(alpha) - piece.renderX(1),
                piece.renderY(alpha) - piece.renderY(1));
        if (stride.offScreen() || coneLength <= 0) {
            return;
        }
        double axis = stride.coneHeading();
        double dirX = -Math.sin(axis);
        double dirY = -Math.cos(axis);
        double dx = piece.renderX(alpha) - piece.renderX(1);
        double dy = piece.renderY(alpha) - piece.renderY(1);
        // The cone's apex (the simulation's) and the flamer's nozzle (the art's): the flame leaves the nozzle and
        // ends where the burn stops hurting, the apex's length along the axis.
        double ax = stride.coneX() + dx;
        double ay = stride.coneY() + dy;
        int k = stride.heading();
        double nx = piece.renderX(alpha) + flamerAt[k][0] + dirX * nozzle;
        double ny = piece.renderY(alpha) + flamerAt[k][1] + dirY * nozzle;
        double from = nozzleShare((nx - ax) * dirX + (ny - ay) * dirY, coneLength);
        StrideArena.ConePhase phase = stride.conePhase();
        double share = stride.coneShare();
        double near = from * coneLength;
        if (phase == StrideArena.ConePhase.BURN) {
            float fade = burnFade(share);
            // The smoke underlay: the cone darkened, so the flame reads over the orange floor.
            coneShade.set(ConeShade.UNDERLAY, 0.45f * fade, realSeconds);
            fills.sector(batch, ax, ay, axis, coneHalf, near, coneLength, CONE_SLICES, CONE_RINGS, coneShade);
            for (int i = 0; i < flamePuffs / 2; i++) {
                double t = flameAge(i * 2 + 1, flamePuffs, realSeconds * 0.6);
                double[] at = conePoint(ax, ay, dirX, dirY, Math.max(from, 0.35 + 0.65 * t), i * 2 + 1, 0.9);
                batch.setColor(0.18f, 0.15f, 0.13f, (float) (0.35 * Math.sin(Math.PI * t)));
                drawScaled(batch, smoke.get(Math.min(smoke.size - 1, (int) (t * smoke.size))), at[0], at[1], (float)
                        (3.2 * puffScale));
            }
            batch.setColor(Color.WHITE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (phase == StrideArena.ConePhase.TELL) {
            float strength = tellStrength(share);
            // A faint fill shows the ground it will burn; the outline marks its edges and its reach.
            coneShade.set(ConeShade.TELL, 0.1f + 0.12f * (float) share, realSeconds);
            fills.sector(batch, ax, ay, axis, coneHalf, near, coneLength, CONE_SLICES, CONE_RINGS, coneShade);
            float line = Color.toFloatBits(FLAME.r, FLAME.g, FLAME.b, strength);
            for (int side = -1; side <= 1; side += 2) {
                edge(batch, ax, ay, axis + side * coneHalf, near, coneLength, line);
            }
            coneShade.set(ConeShade.LINE, strength, realSeconds);
            fills.sector(
                    batch, ax, ay, axis, coneHalf, coneLength - OUTLINE_WIDTH, coneLength, CONE_SLICES, 1, coneShade);
            // The outline shimmers in faint flame along both edges.
            for (double d = Math.max(OUTLINE_STEP, near); d <= coneLength; d += OUTLINE_STEP) {
                for (int side = -1; side <= 1; side += 2) {
                    double wobble = 0.5 + 0.5 * Math.sin(realSeconds * 23 + d * 0.21 + side);
                    double angle = axis + side * coneHalf;
                    double ex = ax - Math.sin(angle) * d;
                    double ey = ay - Math.cos(angle) * d;
                    batch.setColor(1, 1, 1, strength * (float) (0.4 + 0.4 * wobble));
                    drawScaled(batch, flame.get(4 + (int) (wobble * 1.99)), ex, ey, 0.55f);
                }
            }
        } else if (phase == StrideArena.ConePhase.BURN) {
            float fade = burnFade(share);
            coneShade.set(ConeShade.BURN, fade, realSeconds);
            fills.sector(batch, ax, ay, axis, coneHalf, near, coneLength, CONE_SLICES, CONE_RINGS, coneShade);
            for (int i = 0; i < burnPuffs; i++) {
                double t = flameAge(i, burnPuffs, realSeconds);
                double[] at = conePoint(ax, ay, dirX, dirY, from + (1 - from) * t, i, 0.95);
                // The flame sprite's bright frames only: its last ones are dark smoke, nothing when added.
                int frame = Math.min(BRIGHT_FLAME_FRAMES - 1, (int) (t * BRIGHT_FLAME_FRAMES));
                batch.setColor(1, 1, 1, fade * (float) (t < 0.1 ? t / 0.1 : 1 - 0.4 * t));
                drawScaled(batch, flame.get(frame), at[0], at[1], (float) ((0.9 + 2.2 * t) * puffScale));
            }
        }
        batch.setColor(Color.WHITE);
        drawCentred(batch, pilot.get(Math.min(pilot.size - 1, pilotFrame(phase, share))), nx, ny);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Round 43: how much larger than step F3's the puffs of a cone {@code length} px long are drawn (1–2). */
    static double puffScale(double length) {
        return Math.clamp(length / DRAWN_CONE_LENGTH, 1, 2);
    }

    /** The burn's strength {@code share} through it: in quickly, held, out over its last sixth. */
    static float burnFade(double share) {
        return (float) Math.min(1, Math.min(share * 8, (1 - share) * 6 + 0.25));
    }

    /** The tell's outline's strength {@code share} through it: faint at first, stronger as it runs out. */
    static float tellStrength(double share) {
        return (float) (0.45 + 0.5 * share);
    }

    /** One edge of the cone along {@code angle} from {@code from} to {@code to} px out, a thin line in {@code colour}. */
    private void edge(SpriteBatch batch, double ax, double ay, double angle, double from, double to, float colour) {
        double ex = -Math.sin(angle);
        double ey = -Math.cos(angle);
        double nx = -ey * OUTLINE_WIDTH / 2;
        double ny = ex * OUTLINE_WIDTH / 2;
        double x0 = ax + ex * from;
        double y0 = ay + ey * from;
        double x1 = ax + ex * to;
        double y1 = ay + ey * to;
        fills.quad(
                batch, x0 - nx, y0 - ny, colour, x0 + nx, y0 + ny, colour, x1 + nx, y1 + ny, colour, x1 - nx, y1 - ny,
                colour);
    }

    /**
     * The cone's fill colours by its corner: the dark underlay, the tell's faint fill and outline, the burn's flame
     * (hot yellow-white near the nozzle to deep orange at its end, softer toward its edges, a slow shimmer along it).
     */
    static final class ConeShade implements VertexFills.Shade {
        static final int UNDERLAY = 0;
        static final int TELL = 1;
        static final int LINE = 2;
        static final int BURN = 3;

        private int kind;
        private float strength;
        private double seconds;

        void set(int kind, float strength, double seconds) {
            this.kind = kind;
            this.strength = strength;
            this.seconds = seconds;
        }

        @Override
        public float at(double along, double across) {
            double edge = across * across;
            return switch (kind) {
                case UNDERLAY -> Color.toFloatBits(0.16f, 0.13f, 0.11f, strength * (float) (1 - 0.35 * edge));
                case TELL -> Color.toFloatBits(FLAME.r, FLAME.g, FLAME.b, strength * (float) (0.6 + 0.4 * edge));
                case LINE -> Color.toFloatBits(FLAME.r, FLAME.g, FLAME.b, strength);
                default -> {
                    float t = (float) Math.clamp(along, 0, 1);
                    float shimmer = (float) (0.88 + 0.12 * Math.sin(seconds * 9 - along * 7 + across * 2));
                    float a = strength * shimmer * (0.75f - 0.3f * t) * (float) (1 - 0.4 * edge);
                    yield Color.toFloatBits(1f, 0.85f - 0.45f * t, 0.45f - 0.4f * t, a);
                }
            };
        }
    }

    /**
     * Where along the cone the nozzle lies ({@code along} px from the apex on its axis), as a share of its {@code
     * length}: the flame is drawn from there; at most half the cone.
     */
    static double nozzleShare(double along, double length) {
        return length <= 0 ? 0 : Math.clamp(along / length, 0, 0.5);
    }

    /**
     * A point {@code t} (0–1) of the way along the cone from its apex, set off from the axis by puff {@code i}'s own
     * share of the cone's width there ({@code spread} of it).
     */
    private double[] conePoint(double nx, double ny, double dirX, double dirY, double t, int i, double spread) {
        double distance = t * coneLength;
        double lateral = (((i * 0.618034) % 1.0) * 2 - 1) * Math.tan(coneHalf) * distance * spread;
        return new double[] {nx + dirX * distance - dirY * lateral, ny + dirY * distance + dirX * lateral};
    }

    /**
     * With the bullets: the missiles' smoke trails, then the missiles at their headings, half again as large, their
     * hot exhaust behind them and their bodies brightened (additive), so they read as threats to shoot down.
     */
    void drawMissiles(SpriteBatch batch, Sortie sortie, float alpha) {
        drawPuffs(batch, sortie.tick(), alpha, 0, TRAIL);
        Missiles missiles = sortie.missiles();
        for (int m = 0; m < missiles.size(); m++) {
            Missile flying = missiles.get(m);
            drawScaled(
                    batch,
                    missile.get(nearest(flying.heading(), MISSILE_HEADINGS)),
                    flying.renderX(alpha),
                    flying.renderY(alpha),
                    MISSILE_SCALE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int m = 0; m < missiles.size(); m++) {
            Missile flying = missiles.get(m);
            double x = flying.renderX(alpha);
            double y = flying.renderY(alpha);
            double heading = flying.heading();
            batch.setColor(1, 1, 1, MISSILE_SHINE);
            drawScaled(batch, missile.get(nearest(heading, MISSILE_HEADINGS)), x, y, MISSILE_SCALE);
            double ex = x + Math.sin(heading) * EXHAUST_BEHIND;
            double ey = y + Math.cos(heading) * EXHAUST_BEHIND;
            fills.disc(batch, ex, ey, 10, EXHAUST, 0.85f);
            fills.disc(batch, ex, ey, 4.5, EXHAUST_CORE, 0.9f);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        // Round 44: the rail lance's charge and shot, and its hits' flashes, with the bullets.
        strikes.drawLance(batch, sortie, walker(sortie), realSeconds(sortie));
    }

    /** The real seconds the lance's charge pulses by: the simulation's own clock (the screen's is not at hand here). */
    private static double realSeconds(Sortie sortie) {
        return sortie.tick() * SimStep.SECONDS;
    }

    /** The cause a missile's end shows: a burst where it was shot down or struck, a puff of smoke where it fizzled. */
    static boolean bursts(int cause) {
        return cause != Missiles.FIZZLED;
    }
}
