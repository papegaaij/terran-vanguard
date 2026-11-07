package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.BatteryBursts;
import vanguard.sim.BossLines;
import vanguard.sim.BossPlaybook;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.LastWords;
import vanguard.sim.PartMirrors;
import vanguard.sim.PartRegens;
import vanguard.sim.PartShields;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.PlayField;
import vanguard.sim.RearSweep;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4: Vorne's Chimera's looks (design/enemies/bosses/vornes-chimera, Presentation; Level 48's mid-boss;
 * tools/art/act7_citadel.py's sprites and assets/pivots/vornes-chimera.json), following the simulation's set piece as
 * {@link AscendantLooks} follows the Ascendant (its grafts are the Ascendant's parts: the panels' frames and pivots are
 * the same). The body is one fixed piece (nose down) drawn with the canvas's centre on the piece's centre, in one of five
 * banks (±12°) by its sideways speed; every part sits at its pivot's place, so the sway, the rise and the rear sweep move
 * them together.
 *
 * <p>Over the air units ({@link #draw}), back to front: <b>the rise</b> out of the spire well (scaled up from the
 * well's depth and out of its dark over {@link SetPiece#riseShare()}); <b>phase 1</b> (the gold): the body, the
 * batteries (whole, damaged below half, dead) behind their mirror panels (MirrorLooks' frames: down, regrowing), the
 * spine rack's hatches over the salvo's tell, the belly bay's doors over the Dart stream's; <b>the fusion</b> (phase 2's
 * 3 s opening): the gold body with its parts dead for the tell's second, then the tissue body with the sacs growing out of
 * the dead sockets; <b>phase 2</b> (the tissue): the sacs at rest, swelling over the brood's tell, split while it runs,
 * dead husks; <b>the capsule opening</b> (phase 3's 2 s, six frames at bank 0) and <b>phase 3</b>'s open body (Vorne's
 * silhouette in the seat); <b>K</b> ({@link LastWords}): it hangs convulsing while the tissue closes over the capsule
 * (nine frames over the last words' seconds), until the burst.
 *
 * <p>After the grade, additive ({@link #drawGlows}): the panels' heat, cracks, burst rims and sweep (as the Ascendant's),
 * the tells (the nose's gold-white glow over the lance's tell and line, the gun ports over the gold fan's, the bay's gold
 * light, the rack's red lock light, the centre's pulse over the spore spiral's, the far flank's flare over the rear
 * sweep's), the fusion's light, the sacs' cores, healing and brood flare, a sac's burst at its death, the capsule's hex
 * shield (dimming with its share) and its cracks below half its HP, the rear sweep's mark glowing on the bottom edge while
 * it is out of sight, and at the burst the gold-white burst with gold plates tumbling out. With the bullets ({@link
 * #drawLines}): the lance's red line (unsteady until its lock) and its white-gold beam. The bar is the phases' ({@link
 * BossLooks#drawBar}). Presentation only; nothing is allocated per frame.
 */
final class ChimeraLooks {
    static final String SLUG = "vornes-chimera";
    /** The body's banks (the pivots' {@code banks}): -12, -6, 0, +6, +12 degrees; the middle one upright. */
    static final int BANKS = 5;
    /** One bank step (6 degrees) per this much sideways speed, px/s (the sway's 90 px/s at most is the full bank). */
    static final double BANK_SPEED = 40;
    /** The rise starts at this share of the body's size (the well's depth) and in this shade (its dark). */
    static final float RISE_SCALE = 0.55f;

    static final float RISE_SHADE = 0.3f;
    /** The fusion: the tell's second (frames 0-1), then the tissue's burst; the sacs grow over the rest (3 fps). */
    static final double FUSION_TELL_SECONDS = 1;

    static final double FUSION_FPS = 4;
    static final double GROW_FPS = 3;
    /** The capsule's opening: six frames over its 2 s (3 fps). */
    static final double OPENING_FPS = 3;
    /** The burst at 12 fps, the shield's shimmer at 10, the flank's flare and the pulse at their tells' share. */
    static final double BURST_FPS = 12;

    static final double SHIELD_FPS = 10;
    /** The convulsing hang from K: a px jitter this often a second. */
    static final double CONVULSE_HZ = 9;
    /** The body stays under the burst this long before it is gone, s. */
    static final double BODY_UNDER_BURST = 0.25;
    /** The panels' specular sweep (the Ascendant's). */
    static final double SWEEP_SECONDS = 2.5;

    static final float SWEEP_GLINT = 0.45f;
    static final int PORT_TICKS = 2;
    /** The lance's beam colour (white-gold, the Ascendant's). */
    static final Color LANCE = AscendantLooks.LANCE;

    private static final Color HEAT = new Color();
    private static final Color CRACK_DARK = new Color(40 / 255f, 40 / 255f, 40 / 255f, 1);
    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final int SAC_SPLIT = 4;
    private static final int RACK_DEAD = 5;
    private static final int BAY_DEAD = 5;

    private final FlashShader flash;
    private final TextureRegion pixel;
    private final BossSpec spec;
    private final int regrowTicks;
    /** The parts by name (-1 when the spec lacks one). */
    final int capsule;

    final int[] batteries;
    final int rack;
    final int bay;
    final int[] sacs;
    /** The body's anchor in its canvas (px from the top left, y down). */
    private final float anchorX;

    private final float anchorY;
    /** Pivot places as offsets from the centre, y up. */
    final double[][] batteryAt;

    final double[][] sacAt;
    final double[] rackAt;
    final double[] bayAt;
    final double[] capsuleAt;
    final double[] noseAt;
    final double[][] ports;
    final double[][] flankAt;
    private final double[] rackLockAt;
    /** The panels' frames: their headings, and per frame its arc's tips' angles (y up) and radius. */
    private final int[] panelHeadings;

    private final double[] panelFrom;
    private final double[] panelTurn;
    private final double[] panelRadius;

    private final Array<AtlasRegion> gold;
    private final Array<AtlasRegion> tissue;
    private final Array<AtlasRegion> open;
    private final Array<AtlasRegion> opening;
    private final Array<AtlasRegion> battery;
    private final Array<AtlasRegion> panel;
    private final Array<AtlasRegion> panelGlow;
    private final Array<AtlasRegion> panelCracks;
    private final Array<AtlasRegion> panelRim;
    private final Array<AtlasRegion> panelDown;
    private final AtlasRegion panelSweep;
    private final Array<AtlasRegion> rackFrames;
    private final AtlasRegion rackLock;
    private final Array<AtlasRegion> bayFrames;
    private final AtlasRegion bayLight;
    private final Array<AtlasRegion> sac;
    private final AtlasRegion sacDead;
    private final AtlasRegion sacCore;
    private final AtlasRegion sacHeal;
    private final AtlasRegion sacFlare;
    private final Array<AtlasRegion> sacBurst;
    private final Array<AtlasRegion> sacGrow;
    private final Array<AtlasRegion> fusion;
    private final Array<AtlasRegion> closing;
    private final AtlasRegion capsuleCracks;
    private final Array<AtlasRegion> shield;
    private final Array<AtlasRegion> muzzle;
    private final Array<AtlasRegion> portFlash;
    private final Array<AtlasRegion> pulse;
    private final Array<AtlasRegion> flareL;
    private final Array<AtlasRegion> flareR;
    private final Array<AtlasRegion> sweepMark;
    private final Array<AtlasRegion> burst;
    private final Array<AtlasRegion> plate;

    private final HitFlash hits = new HitFlash();
    /** Per part: the step it died (a sac's burst), NEVER while it lives; whether it lived last step. */
    private long[] deathTick = new long[0];

    private boolean[] alive = new boolean[0];
    /** The burst: its step and the capsule's place then (NaN before). */
    private long burstTick = NEVER;

    private double burstX = Double.NaN;
    private double burstY = Double.NaN;

    private ChimeraLooks(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        this.flash = flash;
        pixel = sprites == null ? null : sprites.pixel;
        spec = piece.boss().orElseThrow();
        regrowTicks = spec.mirrors().isEmpty()
                ? 1
                : Math.max(1, SimStep.ticks(spec.mirrors().getFirst().mirror().regrowSeconds()));
        capsule = part(piece, "capsule");
        batteries = new int[] {part(piece, "battery-l"), part(piece, "battery-r")};
        rack = part(piece, "rack");
        bay = part(piece, "bay");
        sacs = new int[] {part(piece, "sac-l"), part(piece, "sac-r"), part(piece, "sac-spine")};
        float[] centre = pivots.get("centre").asFloatArray();
        anchorX = centre[0];
        anchorY = centre[1];
        JsonValue parts = pivots.get("parts");
        batteryAt = new double[][] {place(parts.get("battery-l")), place(parts.get("battery-r"))};
        sacAt = new double[][] {place(parts.get("sac-l")), place(parts.get("sac-r")), place(parts.get("sac-spine"))};
        rackAt = place(parts.get("rack"));
        bayAt = place(parts.get("bay"));
        capsuleAt = place(parts.get("capsule"));
        noseAt = place(pivots.get("nose"));
        JsonValue portList = pivots.get("ports");
        ports = new double[portList.size][];
        int k = 0;
        for (JsonValue port = portList.child; port != null; port = port.next, k++) {
            ports[k] = place(port);
        }
        flankAt = new double[][] {
            place(pivots.get("flank").get("l")), place(pivots.get("flank").get("r"))
        };
        // The lock light sits at the rack's lock point (px from the rack's place, y down).
        float[] lock = pivots.get("rack_frames").get("lock").asFloatArray();
        rackLockAt = new double[] {rackAt[0] + lock[0], rackAt[1] - lock[1]};
        JsonValue frames = pivots.get("panels").get("frames");
        panelHeadings = new int[frames.size];
        panelFrom = new double[frames.size];
        panelTurn = new double[frames.size];
        panelRadius = new double[frames.size];
        k = 0;
        for (JsonValue f = frames.child; f != null; f = f.next, k++) {
            panelHeadings[k] = f.getInt("heading");
            float[] t0 = f.get("tips").get(0).asFloatArray();
            float[] t1 = f.get("tips").get(1).asFloatArray();
            double half = MirrorLooks.PLATE_SIZE / 2.0;
            double a0 = Math.atan2(-(t0[1] - half), t0[0] - half);
            double a1 = Math.atan2(-(t1[1] - half), t1[0] - half);
            panelFrom[k] = a0;
            panelTurn[k] = Math.IEEEremainder(a1 - a0, 2 * Math.PI);
            panelRadius[k] = f.getDouble("radius");
        }
        String r = SLUG;
        gold = optional(sprites, r);
        tissue = optional(sprites, r + "-tissue");
        open = optional(sprites, r + "-open");
        opening = optional(sprites, r + "-opening");
        battery = optional(sprites, r + "-battery");
        panel = optional(sprites, r + "-panel");
        panelGlow = optional(sprites, r + "-panel-glow");
        panelCracks = optional(sprites, r + "-panel-cracks");
        panelRim = optional(sprites, r + "-panel-rim");
        panelDown = optional(sprites, r + "-panel-down");
        panelSweep = single(sprites, r + "-panel-sweep");
        rackFrames = optional(sprites, r + "-rack");
        rackLock = single(sprites, r + "-rack-lock");
        bayFrames = optional(sprites, r + "-bay");
        bayLight = single(sprites, r + "-bay-light");
        sac = optional(sprites, r + "-sac");
        sacDead = single(sprites, r + "-sac-dead");
        sacCore = single(sprites, r + "-sac-core");
        sacHeal = single(sprites, r + "-sac-heal");
        sacFlare = single(sprites, r + "-sac-flare");
        sacBurst = optional(sprites, r + "-sac-burst");
        sacGrow = optional(sprites, r + "-sac-grow");
        fusion = optional(sprites, r + "-fusion");
        closing = optional(sprites, r + "-closing");
        capsuleCracks = single(sprites, r + "-capsule-cracks");
        shield = optional(sprites, r + "-shield");
        muzzle = optional(sprites, r + "-muzzle");
        portFlash = optional(sprites, r + "-port-flash");
        pulse = optional(sprites, r + "-pulse");
        flareL = optional(sprites, r + "-flare-l");
        flareR = optional(sprites, r + "-flare-r");
        sweepMark = optional(sprites, r + "-sweep-mark");
        burst = optional(sprites, r + "-burst");
        plate = optional(sprites, r + "-plate");
        reset();
    }

    /** The looks of the level's boss when it is Vorne's Chimera (its pivots read), else null. */
    static ChimeraLooks of(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        if (piece == null
                || pivots == null
                || pivots.get("parts") == null
                || !drawn(piece)
                || piece.boss().isEmpty()
                || !sprites.has(SLUG)) {
            return null;
        }
        return new ChimeraLooks(sprites, flash, piece, pivots);
    }

    /** For the tests: the looks following a sortie of {@code piece} without its sprites (they draw nothing). */
    static ChimeraLooks following(SetPieceSpec piece, JsonValue pivots) {
        return new ChimeraLooks(null, null, piece, pivots);
    }

    /** Whether the set piece is drawn here (Vorne's Chimera), not by {@link BossLooks}. */
    static boolean drawn(SetPieceSpec piece) {
        return piece != null && piece.slug().equals(SLUG);
    }

    private static int part(SetPieceSpec piece, String name) {
        for (int p = 0; p < piece.parts().size(); p++) {
            if (piece.parts().get(p).name().equals(name)) {
                return p;
            }
        }
        return -1;
    }

    /** A pivot offset (y down) as an offset y up. */
    private static double[] place(JsonValue at) {
        return new double[] {at.getDouble(0), -at.getDouble(1)};
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.region(name) : null;
    }

    /** The level restarts (or the fight is retried from its checkpoint): no deaths, no burst. */
    void reset() {
        Arrays.fill(deathTick, NEVER);
        Arrays.fill(alive, true);
        burstTick = NEVER;
        burstX = burstY = Double.NaN;
        hits.reset();
    }

    // --- the rules ---------------------------------------------------------------------------------

    /** The bank frame (0..4, 2 upright, 4 the +12 degrees of moving right) at sideways speed {@code vx}, px/s. */
    static int bankFrame(double vx) {
        return Math.clamp(Math.round(vx / BANK_SPEED), -2, 2) + 2;
    }

    /** A battery's, the rack's or the bay's damage: 0 whole, 1 damaged (below half), 2 dead. */
    static int damageState(double hp, double max) {
        return AscendantLooks.damageState(hp, max);
    }

    /** The rise's scale and shade at {@code share} of it (eased): from the well's depth to full. */
    static float riseScale(double share) {
        return (float) (RISE_SCALE + (1 - RISE_SCALE) * eased(share));
    }

    static float riseShade(double share) {
        return (float) (RISE_SHADE + (1 - RISE_SHADE) * eased(share));
    }

    private static double eased(double share) {
        double t = Math.clamp(share, 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** The fusion's frame {@code seconds} into it (of {@code frames}): 0-1 over the tell's second, then 2.. at 4 fps; -1 after. */
    static int fusionFrame(double seconds, int frames) {
        if (seconds < 0) {
            return -1;
        }
        if (seconds < FUSION_TELL_SECONDS) {
            return seconds < FUSION_TELL_SECONDS / 2 ? 0 : 1;
        }
        int frame = 2 + (int) Math.floor((seconds - FUSION_TELL_SECONDS) * FUSION_FPS);
        return frame < frames ? frame : -1;
    }

    /** A sac's growing frame {@code seconds} into the fusion (of {@code frames}): from the tell's end at 3 fps, held. */
    static int growFrame(double seconds, int frames) {
        if (seconds < FUSION_TELL_SECONDS) {
            return -1;
        }
        return (int) Math.min(frames - 1, Math.floor((seconds - FUSION_TELL_SECONDS) * GROW_FPS));
    }

    /** The capsule's opening frame {@code seconds} into its 2 s (of {@code frames}), held on the last. */
    static int openingFrame(double seconds, int frames) {
        return (int) Math.clamp(Math.floor(seconds * OPENING_FPS), 0, frames - 1);
    }

    /** The tissue closing over the capsule from K: its frames evenly over the last words' {@code total} s, held. */
    static int closingFrame(double seconds, double total, int frames) {
        if (seconds < 0 || frames == 0) {
            return -1;
        }
        return (int) Math.min(frames - 1, Math.floor(seconds / total * frames));
    }

    /** The frame {@code seconds} into a set of {@code frames} at {@code fps}; -1 before it or once it has run. */
    static int runFrame(double seconds, double fps, int frames) {
        return AscendantLooks.runFrame(seconds, fps, frames);
    }

    /** The rear sweep's bottom-edge mark frame at {@code share} of its time out of sight (brightening). */
    static int markFrame(double share, int frames) {
        return (int) Math.clamp(Math.floor(share * frames), 0, frames - 1);
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The Chimera among the set pieces, or null. */
    static SetPiece chimera(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (drawn(piece.spec())) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: the parts' deaths (a sac's burst), the burst at the last words' end. */
    void stepped(Sortie sortie) {
        SetPiece piece = chimera(sortie);
        if (piece == null) {
            return;
        }
        int count = piece.partCount();
        if (deathTick.length != count) {
            deathTick = new long[count];
            alive = new boolean[count];
            Arrays.fill(deathTick, NEVER);
            Arrays.fill(alive, true);
        }
        long tick = sortie.tick();
        if (burstTick != NEVER && tick < burstTick) {
            // A retry from the boss checkpoint brings it back whole.
            reset();
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.LAST_BURST) {
                burstTick = tick;
                burstX = events.x(i) + capsuleAt[0];
                burstY = events.y(i) + capsuleAt[1];
            }
        }
        boolean spoken = spoken(piece);
        for (int p = 0; p < count; p++) {
            boolean now = piece.partHp(p) > 0;
            if (alive[p] && !now && !spoken) {
                deathTick[p] = tick;
            }
            alive[p] = now;
        }
    }

    private static boolean spoken(SetPiece piece) {
        LastWords words = piece.lastWords().orElse(null);
        return words != null && words.spoken();
    }

    /** The burst's step, for the tests (NEVER before). */
    long burstTick() {
        return burstTick;
    }

    // --- drawing -----------------------------------------------------------------------------------

    private static void additive(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    private static void normal(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static double seconds(long ticks) {
        return ticks * SimStep.SECONDS;
    }

    private boolean drawable() {
        return !gold.isEmpty();
    }

    /** The body's canvas at (cx, cy), its anchor on the centre, scaled about it. */
    private void drawBody(SpriteBatch batch, TextureRegion region, double cx, double cy, float scale) {
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        float originY = h - anchorY;
        batch.draw(
                region,
                Math.round(X0 + cx - anchorX),
                Math.round(cy - originY),
                anchorX,
                originY,
                w,
                h,
                scale,
                scale,
                0);
    }

    private static Array<AtlasRegion> orFirst(Array<AtlasRegion> set, Array<AtlasRegion> fallback) {
        return set.isEmpty() ? fallback : set;
    }

    /** Whether the boss shows now: present, or its burst's first moments (the body under the burst). */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        SetPiece piece = chimera(sortie);
        if (piece == null || !drawable() || !piece.present()) {
            return;
        }
        long tick = sortie.tick();
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        LastWords words = piece.lastWords().orElse(null);
        batch.setColor(Color.WHITE);
        if (words != null && words.spoken()) {
            drawLastWords(batch, piece, words, cx, cy, tick, alpha);
            return;
        }
        int bank = bankFrame((piece.renderX(1) - piece.renderX(0)) / SimStep.SECONDS);
        double rise = piece.riseShare();
        if (rise < 1) {
            float shade = riseShade(rise);
            batch.setColor(shade, shade, shade, 1);
            drawBody(batch, gold.get(Math.min(2, gold.size - 1)), cx, cy, riseScale(rise));
            batch.setColor(Color.WHITE);
            return;
        }
        int phase = piece.phase();
        if (phase == 0) {
            drawGold(batch, piece, cx, cy, tick, whiteFlash, bank);
        } else if (phase == 1) {
            drawTissue(batch, piece, cx, cy, tick, whiteFlash, bank);
        } else {
            drawOpen(batch, piece, cx, cy, tick, whiteFlash, bank);
        }
    }

    private void drawGold(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, float white, int bank) {
        var parts = piece.spec().parts();
        drawBody(batch, gold.get(Math.min(bank, gold.size - 1)), cx, cy, 1);
        PartMirrors mirrors = piece.mirrors().orElse(null);
        for (int i = 0; i < batteries.length; i++) {
            int b = batteries[i];
            if (b < 0 || battery.isEmpty()) {
                continue;
            }
            double px = cx + batteryAt[i][0];
            double py = cy + batteryAt[i][1];
            int state = damageState(piece.partHp(b), parts.get(b).hp());
            drawPart(batch, piece, b, battery.get(Math.min(state, battery.size - 1)), px, py, tick, white, state < 2);
            if (state == 2 || mirrors == null || !mirrors.fitted(b) || panel.isEmpty()) {
                continue;
            }
            int j = AscendantLooks.panelFrame(mirrors.facing(b), panelHeadings);
            if (mirrors.down(b)) {
                if (j < panelDown.size) {
                    drawCentred(batch, panelDown.get(j), px, py);
                }
                continue;
            }
            int regrow = mirrors.regrowTicks(b);
            if (regrow > 0) {
                batch.setColor(1, 1, 1, (float) MirrorLooks.regrowShare(regrow, regrowTicks));
            }
            drawCentred(batch, panel.get(j), px, py);
            batch.setColor(Color.WHITE);
        }
        BossPlaybook book = piece.playbook().orElse(null);
        if (rack >= 0 && !rackFrames.isEmpty()) {
            int state = damageState(piece.partHp(rack), parts.get(rack).hp());
            int frame =
                    state == 2 ? RACK_DEAD : state == 1 ? RACK_DEAD - 1 : moveOpening(book, PhasePlaybook.Kind.SALVO);
            drawPart(
                    batch,
                    piece,
                    rack,
                    rackFrames.get(Math.min(frame, rackFrames.size - 1)),
                    cx + rackAt[0],
                    cy + rackAt[1],
                    tick,
                    white,
                    state < 2);
        }
        if (bay >= 0 && !bayFrames.isEmpty()) {
            int state = damageState(piece.partHp(bay), parts.get(bay).hp());
            int frame =
                    state == 2 ? BAY_DEAD : state == 1 ? BAY_DEAD - 1 : moveOpening(book, PhasePlaybook.Kind.STREAM);
            drawPart(
                    batch,
                    piece,
                    bay,
                    bayFrames.get(Math.min(frame, bayFrames.size - 1)),
                    cx + bayAt[0],
                    cy + bayAt[1],
                    tick,
                    white,
                    state < 2);
        }
    }

    /** The fusion's seconds while phase 2's opening runs, else NaN. */
    private static double fusionSeconds(SetPiece piece) {
        return piece.phase() == 1 && piece.opening() ? piece.phaseSeconds() : Double.NaN;
    }

    /** The capsule's opening's seconds while phase 3's opening runs, else NaN. */
    private static double openingSeconds(SetPiece piece) {
        return piece.phase() == 2 && piece.opening() ? piece.phaseSeconds() : Double.NaN;
    }

    private void drawTissue(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, float white, int bank) {
        double fusing = fusionSeconds(piece);
        if (!Double.isNaN(fusing) && fusing < FUSION_TELL_SECONDS) {
            // The fusion's tell: the gold body, its phase-1 parts dead in their sockets (teal light leaking: the
            // glows).
            drawBody(batch, gold.get(Math.min(bank, gold.size - 1)), cx, cy, 1);
            for (double[] at : batteryAt) {
                if (battery.size > 2) {
                    drawCentred(batch, battery.get(2), cx + at[0], cy + at[1]);
                }
            }
            if (rackFrames.size > RACK_DEAD) {
                drawCentred(batch, rackFrames.get(RACK_DEAD), cx + rackAt[0], cy + rackAt[1]);
            }
            drawDeadBay(batch, cx, cy);
            return;
        }
        drawBody(batch, orFirst(tissue, gold).get(Math.min(bank, orFirst(tissue, gold).size - 1)), cx, cy, 1);
        drawDeadBay(batch, cx, cy);
        if (!Double.isNaN(fusing)) {
            int grow = growFrame(fusing, sacGrow.size);
            for (double[] at : sacAt) {
                if (grow >= 0) {
                    drawCentred(batch, sacGrow.get(grow), cx + at[0], cy + at[1]);
                }
            }
            return;
        }
        drawSacs(batch, piece, cx, cy, tick, white);
    }

    private void drawDeadBay(SpriteBatch batch, double cx, double cy) {
        if (bayFrames.size > BAY_DEAD) {
            drawCentred(batch, bayFrames.get(BAY_DEAD), cx + bayAt[0], cy + bayAt[1]);
        }
    }

    private void drawSacs(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, float white) {
        int brooding = broodingSac(piece);
        BossPlaybook book = piece.playbook().orElse(null);
        for (int s = 0; s < sacs.length; s++) {
            int p = sacs[s];
            if (p < 0 || sac.isEmpty()) {
                continue;
            }
            double px = cx + sacAt[s][0];
            double py = cy + sacAt[s][1];
            if (piece.partHp(p) <= 0) {
                if (sacDead != null) {
                    drawCentred(batch, sacDead, px, py);
                }
                continue;
            }
            int frame = 0;
            if (s == brooding && book != null) {
                frame = book.mode() == BossPlaybook.Mode.RUN
                        ? SAC_SPLIT
                        : 1 + (int) Math.min(2, Math.floor(book.tellShare() * 3));
            }
            drawPart(batch, piece, p, sac.get(Math.min(frame, sac.size - 1)), px, py, tick, white, true);
        }
    }

    private void drawOpen(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, float white, int bank) {
        double opened = openingSeconds(piece);
        if (!Double.isNaN(opened) && !opening.isEmpty()) {
            drawBody(batch, opening.get(openingFrame(opened, opening.size)), cx, cy, 1);
        } else {
            Array<AtlasRegion> set = orFirst(open, gold);
            drawBody(batch, set.get(Math.min(bank, set.size - 1)), cx, cy, 1);
        }
        drawDeadBay(batch, cx, cy);
        for (double[] at : sacAt) {
            if (sacDead != null) {
                drawCentred(batch, sacDead, cx + at[0], cy + at[1]);
            }
        }
    }

    /** From K: the open body hanging, convulsing, the tissue closing over the capsule; gone under the burst. */
    private void drawLastWords(
            SpriteBatch batch, SetPiece piece, LastWords words, double cx, double cy, long tick, float alpha) {
        if (burstTick != NEVER && seconds(tick - burstTick) + alpha * SimStep.SECONDS > BODY_UNDER_BURST) {
            return;
        }
        double k = words.spokenSeconds() + alpha * SimStep.SECONDS;
        double phase = Math.floor(k * CONVULSE_HZ);
        double jx = (phase % 3) - 1;
        double jy = ((phase * 7) % 3) - 1;
        Array<AtlasRegion> set = orFirst(open, gold);
        drawBody(batch, set.get(Math.min(2, set.size - 1)), cx + jx, cy + jy, 1);
        drawDeadBay(batch, cx + jx, cy + jy);
        for (double[] at : sacAt) {
            if (sacDead != null) {
                drawCentred(batch, sacDead, cx + jx + at[0], cy + jy + at[1]);
            }
        }
        int frame = closingFrame(k, words.spec().seconds(), closing.size);
        if (frame >= 0) {
            drawCentred(batch, closing.get(frame), cx + jx + capsuleAt[0], cy + jy + capsuleAt[1]);
        }
    }

    /**
     * How far a move's doors or hatches stand open (0 shut .. 3 open): opening over the move's tell while it is of {@code
     * kind}, open while it runs.
     */
    private static int moveOpening(BossPlaybook book, PhasePlaybook.Kind kind) {
        if (book == null || book.kind() != kind) {
            return 0;
        }
        if (book.mode() == BossPlaybook.Mode.RUN) {
            return 3;
        }
        if (book.mode() != BossPlaybook.Mode.TELL) {
            return 0;
        }
        return (int) Math.min(3, Math.floor(book.tellShare() * 4));
    }

    /** A part's sprite at its place, flashing white on a hit ({@code flashes}: a living part). */
    private void drawPart(
            SpriteBatch batch,
            SetPiece piece,
            int p,
            TextureRegion region,
            double px,
            double py,
            long tick,
            float whiteFlash,
            boolean flashes) {
        if (flashes && flash != null && hits.on(p, tick, piece.partTicksSinceHit(p))) {
            flash.draw(batch, region, Math.round(X0 + px), Math.round(py), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
            return;
        }
        drawCentred(batch, region, px, py);
    }

    /** The sac (0..2) swelling or splitting for the brood now (the first living one), -1 for none. */
    private int broodingSac(SetPiece piece) {
        BossPlaybook book = piece.playbook().orElse(null);
        if (book == null || book.kind() != PhasePlaybook.Kind.BROOD || book.mode() == BossPlaybook.Mode.REST) {
            return -1;
        }
        for (int s = 0; s < sacs.length; s++) {
            if (sacs[s] >= 0 && piece.partHp(sacs[s]) > 0) {
                return s;
            }
        }
        return -1;
    }

    // --- the glows -------------------------------------------------------------------------------------

    /** After the grade, additive: the panels' glows, the tells, the fusion, the sacs, the shield, the mark, the burst. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = chimera(sortie);
        if (piece == null || !drawable()) {
            return;
        }
        long tick = sortie.tick();
        double now = seconds(tick) + alpha * SimStep.SECONDS;
        additive(batch);
        batch.setColor(Color.WHITE);
        if (piece.present() && !spoken(piece) && piece.riseShare() >= 1) {
            double cx = Math.round(piece.renderX(alpha));
            double cy = Math.round(piece.renderY(alpha));
            BossPlaybook book = piece.playbook().orElse(null);
            if (piece.phase() == 0) {
                drawPanelGlows(batch, piece, cx, cy, now, book);
                drawPhase1Tells(batch, piece, cx, cy, tick, book);
            } else {
                drawFusion(batch, piece, cx, cy);
                drawSacGlows(batch, piece, cx, cy, now, book);
                drawCapsule(batch, piece, cx, cy, tick, now);
            }
            drawSharedTells(batch, piece, cx, cy, tick, book);
            drawSacBursts(batch, cx, cy, tick);
        }
        if (piece.present()) {
            drawSweepMark(batch, piece);
        }
        drawBurst(batch, tick, alpha);
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    private void drawPanelGlows(
            SpriteBatch batch, SetPiece piece, double cx, double cy, double now, BossPlaybook book) {
        PartMirrors mirrors = piece.mirrors().orElse(null);
        BatteryBursts bursts = piece.bursts().orElse(null);
        if (mirrors == null || panel.isEmpty()) {
            return;
        }
        float barrage =
                book != null && book.mode() == BossPlaybook.Mode.TELL && book.kind() == PhasePlaybook.Kind.BARRAGE
                        ? (float) book.tellShare()
                        : 0;
        for (int i = 0; i < batteries.length; i++) {
            int b = batteries[i];
            if (b < 0 || !mirrors.fitted(b) || piece.partHp(b) <= 0 || mirrors.down(b)) {
                continue;
            }
            int j = AscendantLooks.panelFrame(mirrors.facing(b), panelHeadings);
            double px = cx + batteryAt[i][0];
            double py = cy + batteryAt[i][1];
            double heat = mirrors.heatShare(b);
            if (heat > 0 && j < panelGlow.size) {
                MirrorLooks.heatColour(heat, HEAT);
                batch.setColor(HEAT.r, HEAT.g, HEAT.b, MirrorLooks.heatOpacity(heat));
                drawCentred(batch, panelGlow.get(j), px, py);
            }
            float cracks = MirrorLooks.crackOpacity(heat);
            if (cracks > 0 && j < panelCracks.size) {
                normal(batch);
                batch.setColor(CRACK_DARK.r, CRACK_DARK.g, CRACK_DARK.b, cracks);
                drawCentred(batch, panelCracks.get(j), px, py);
                additive(batch);
            }
            if (bursts != null && (bursts.telling(b) || barrage > 0) && j < panelRim.size) {
                batch.setColor(1, 1, 1, Math.max(barrage, (float) bursts.tellShare(b)));
                drawCentred(batch, panelRim.get(j), px, py);
            }
            if (panelSweep != null) {
                boolean regrowing = mirrors.regrowTicks(b) > 0;
                double u = regrowing
                        ? MirrorLooks.regrowShare(mirrors.regrowTicks(b), regrowTicks)
                        : ((now + i * 0.6) / SWEEP_SECONDS) % 1.0;
                double angle = panelFrom[j] + panelTurn[j] * Math.clamp(u, 0, 1);
                batch.setColor(1, 1, 1, regrowing ? 1 : SWEEP_GLINT);
                drawCentred(
                        batch,
                        panelSweep,
                        px + Math.cos(angle) * panelRadius[j],
                        py + Math.sin(angle) * panelRadius[j]);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** Phase 1's own tells: the rack's red lock light over the salvo's, the bay's gold light over the Dart stream's. */
    private void drawPhase1Tells(
            SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, BossPlaybook book) {
        if (book != null
                && book.mode() == BossPlaybook.Mode.TELL
                && book.kind() == PhasePlaybook.Kind.SALVO
                && rackLock != null
                && rack >= 0
                && piece.partHp(rack) > 0) {
            batch.setColor(1, 1, 1, (float) book.tellShare());
            drawCentred(batch, rackLock, cx + rackLockAt[0], cy + rackLockAt[1]);
        }
        int doors = moveOpening(book, PhasePlaybook.Kind.STREAM);
        if (doors > 0 && bayLight != null && bay >= 0 && piece.partHp(bay) > 0) {
            batch.setColor(1, 1, 1, doors / 3f);
            drawCentred(batch, bayLight, cx + bayAt[0], cy + bayAt[1]);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * The tells of every phase: the nose's glow over the lance's tell and its told line, the gun ports over the gold
     * fan's, the centre's pulse over the spore spiral's, the far flank's flare over the rear sweep's.
     */
    private void drawSharedTells(
            SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, BossPlaybook book) {
        boolean telling = book != null && book.mode() == BossPlaybook.Mode.TELL && book.kind() != null;
        float share = telling ? (float) book.tellShare() : 0;
        if (telling && book.kind() == PhasePlaybook.Kind.VOLLEY && !portFlash.isEmpty()) {
            batch.setColor(1, 1, 1, share);
            int frame = (int) (tick / PORT_TICKS % portFlash.size);
            for (double[] port : ports) {
                drawCentred(batch, portFlash.get(frame), cx + port[0], cy + port[1]);
            }
        }
        if (telling && book.kind() == PhasePlaybook.Kind.SPIRAL && !pulse.isEmpty()) {
            batch.setColor(1, 1, 1, Math.max(0.4f, share));
            drawCentred(batch, pulse.get(markFrame(share, pulse.size)), cx + capsuleAt[0], cy + capsuleAt[1]);
        }
        BossLines lines = piece.lines().orElse(null);
        boolean line = lines != null && lines.running() && lines.telegraphing();
        if (!muzzle.isEmpty() && (line || (telling && PhasePlaybook.lance(book.kind())))) {
            int frame = line ? muzzle.size - 1 : markFrame(share, muzzle.size);
            batch.setColor(Color.WHITE);
            drawCentred(batch, muzzle.get(frame), cx + noseAt[0], cy + noseAt[1]);
        }
        RearSweep sweep = piece.rearSweep().orElse(null);
        if (sweep != null && sweep.mode() == RearSweep.Mode.TELL) {
            Array<AtlasRegion> flare = sweep.flank() > 0 ? flareR : flareL;
            double[] at = flankAt[sweep.flank() > 0 ? 1 : 0];
            float told = telling && book.kind() == PhasePlaybook.Kind.REAR_SWEEP ? share : 1;
            if (!flare.isEmpty()) {
                batch.setColor(1, 1, 1, Math.max(0.5f, told));
                drawCentred(batch, flare.get(markFrame(told, flare.size)), cx + at[0], cy + at[1]);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** The fusion's light over the body: the tell's teal seams, then the burst out of the sockets, fading. */
    private void drawFusion(SpriteBatch batch, SetPiece piece, double cx, double cy) {
        double fusing = fusionSeconds(piece);
        if (Double.isNaN(fusing) || fusion.isEmpty()) {
            return;
        }
        int frame = fusionFrame(fusing, fusion.size);
        if (frame < 0) {
            return;
        }
        batch.setColor(Color.WHITE);
        drawBody(batch, fusion.get(frame), cx, cy, 1);
        if (frame >= 2 && !plate.isEmpty()) {
            // Gold plates blown out of the sockets with the burst.
            double t = fusing - FUSION_TELL_SECONDS;
            drawPlates(batch, cx + capsuleAt[0], cy + capsuleAt[1], t, 6, 40);
        }
    }

    /** {@code count} gold plates tumbling out from (x, y) {@code t} s after a burst, {@code speed} px/s outward. */
    private void drawPlates(SpriteBatch batch, double x, double y, double t, int count, double speed) {
        normal(batch);
        float fade = (float) Math.clamp(1 - t / 1.5, 0, 1);
        if (fade > 0) {
            batch.setColor(1, 1, 1, fade);
            double reach = 30 + speed * t;
            for (int j = 0; j < count; j++) {
                double angle = j * 2.399 + 0.3;
                int frame = (int) (Math.floor(t * BURST_FPS) + j) % plate.size;
                drawCentred(batch, plate.get(frame), x + Math.cos(angle) * reach, y + Math.sin(angle) * reach);
            }
        }
        batch.setColor(Color.WHITE);
        additive(batch);
    }

    private void drawSacGlows(SpriteBatch batch, SetPiece piece, double cx, double cy, double now, BossPlaybook book) {
        if (piece.phase() != 1 || !Double.isNaN(fusionSeconds(piece))) {
            return;
        }
        PartRegens regens = piece.regens().orElse(null);
        int brooding = broodingSac(piece);
        for (int s = 0; s < sacs.length; s++) {
            int p = sacs[s];
            if (p < 0 || piece.partHp(p) <= 0) {
                continue;
            }
            double px = cx + sacAt[s][0];
            double py = cy + sacAt[s][1];
            if (sacCore != null) {
                batch.setColor(1, 1, 1, (float) (0.7 + 0.3 * Math.sin(now * 6 + s)));
                drawCentred(batch, sacCore, px, py);
            }
            if (regens != null && regens.fitted(p) && regens.healing(p) && sacHeal != null) {
                batch.setColor(1, 1, 1, (float) (0.5 + 0.5 * Math.sin(now * 9)));
                drawCentred(batch, sacHeal, px, py);
            }
            if (s == brooding && book.mode() == BossPlaybook.Mode.TELL && sacFlare != null) {
                batch.setColor(1, 1, 1, (float) book.tellShare());
                drawCentred(batch, sacFlare, px, py);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** Phase 3's capsule: its gold hex shield (dimming with its share), its cracks below half its HP, a hit's flash. */
    private void drawCapsule(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, double now) {
        if (piece.phase() != 2 || capsule < 0) {
            return;
        }
        double px = cx + capsuleAt[0];
        double py = cy + capsuleAt[1];
        double max = piece.spec().parts().get(capsule).hp();
        boolean opened = Double.isNaN(openingSeconds(piece));
        if (opened && capsuleCracks != null) {
            float cracks = piece.partHp(capsule) < max / 2 ? (float) (0.6 + 0.3 * Math.sin(now * 5)) : 0;
            float hit = hits.on(capsule, tick, piece.partTicksSinceHit(capsule)) ? 0.5f : 0;
            if (cracks + hit > 0) {
                batch.setColor(1, 1, 1, Math.min(1, cracks + hit));
                drawCentred(batch, capsuleCracks, px, py);
            }
        }
        PartShields shields = piece.shields().orElse(null);
        if (opened && shields != null && shields.fitted(capsule) && shields.shield(capsule) > 0 && !shield.isEmpty()) {
            batch.setColor(1, 1, 1, EnemyShieldLooks.holdOpacity(shields.share(capsule)));
            drawCentred(batch, shield.get((int) (Math.floor(now * SHIELD_FPS) % shield.size)), px, py);
        }
        batch.setColor(Color.WHITE);
    }

    /** A sac's burst where it died. */
    private void drawSacBursts(SpriteBatch batch, double cx, double cy, long tick) {
        for (int s = 0; s < sacs.length; s++) {
            int p = sacs[s];
            if (p < 0 || p >= deathTick.length || deathTick[p] == NEVER) {
                continue;
            }
            int frame = runFrame(seconds(tick - deathTick[p]), BURST_FPS, sacBurst.size);
            if (frame >= 0) {
                drawCentred(batch, sacBurst.get(frame), cx + sacAt[s][0], cy + sacAt[s][1]);
            }
        }
    }

    /** The rear sweep's rising point glowing on the bottom edge while it is out of sight. */
    private void drawSweepMark(SpriteBatch batch, SetPiece piece) {
        RearSweep sweep = piece.rearSweep().orElse(null);
        if (sweep == null || !sweep.away() || sweepMark.isEmpty()) {
            return;
        }
        double share = sweep.awayShare();
        batch.setColor(1, 1, 1, (float) (0.4 + 0.6 * share));
        drawCentred(batch, sweepMark.get(markFrame(share, sweepMark.size)), sweep.markX(), 0);
        batch.setColor(Color.WHITE);
    }

    /** The burst at the last words' end: the gold-white burst with plates tumbling out (the vacuum chain is built). */
    private void drawBurst(SpriteBatch batch, long tick, float alpha) {
        if (burstTick == NEVER || Double.isNaN(burstX)) {
            return;
        }
        double t = seconds(tick - burstTick) + alpha * SimStep.SECONDS;
        int frame = runFrame(t, BURST_FPS, burst.size);
        if (frame >= 0) {
            drawCentred(batch, burst.get(frame), burstX, burstY);
        }
        if (!plate.isEmpty() && t < 1.5) {
            drawPlates(batch, burstX, burstY, t, 10, 110);
        }
    }

    // --- the lance --------------------------------------------------------------------------------------

    /** With the bullets: the lance's red line over its telegraph (unsteady until its lock), then its beam. */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = chimera(sortie);
        if (piece == null || pixel == null || !piece.present() || spoken(piece)) {
            return;
        }
        BossLines lines = piece.lines().orElse(null);
        if (lines == null || !lines.running() || lines.attack() < 0) {
            return;
        }
        EnemySpec.LaserLine line = spec.attacks().get(lines.attack()).line().orElse(null);
        if (line == null) {
            return;
        }
        long tick = sortie.tick();
        double ox = lines.fromX() - lines.stepX() * (1 - alpha);
        double oy = lines.fromY() - lines.stepY() * (1 - alpha);
        additive(batch);
        Color red = LineLooks.TELEGRAPH;
        if (lines.telegraphing()) {
            boolean locked = !line.tracks() || lines.charge() >= HonourGuardLooks.lockShare(line);
            float opacity = LineLooks.telegraphOpacity(lines.charge(), tick);
            if (!locked) {
                opacity *= tick / 3 % 2 == 0 ? 0.8f : 0.55f;
            }
            batch.setColor(red.r, red.g, red.b, opacity);
            stroke(batch, ox, oy, lines.dirX(), lines.dirY(), locked ? LineLooks.TELEGRAPH_WIDTH : 1);
        } else if (lines.live()) {
            batch.setColor(LANCE.r, LANCE.g, LANCE.b, LineLooks.RAIL_OPACITY);
            stroke(batch, ox, oy, lines.dirX(), lines.dirY(), (float)
                    Math.max(LineLooks.CORE_WIDTH + 2, line.bandPx()));
            batch.setColor(LineLooks.CORE);
            stroke(batch, ox, oy, lines.dirX(), lines.dirY(), LineLooks.CORE_WIDTH + 2);
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    private void stroke(SpriteBatch batch, double x, double y, double dx, double dy, float width) {
        float length = (float) (2 * (PlayField.WIDTH + PlayField.HEIGHT));
        batch.draw(pixel, (float) (X0 + x), (float) (y - width / 2), 0, width / 2, length, width, 1, 1, (float)
                Math.toDegrees(Math.atan2(dy, dx)));
    }
}
