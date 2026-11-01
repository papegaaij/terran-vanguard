package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.AcePlaybook;
import vanguard.sim.BossLines;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.PartShields;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.SquadronPlaybook;
import vanguard.sim.WarningEdge;

/**
 * M10 part F, step F4: the Honour Guard (design/enemies/bosses/honour-guard, Presentation, Tells, Death; Level 40's
 * mid-boss), its three aces in their production sprites (tools/art/honour_guard.py, pivots in {@code
 * assets/pivots/honour-guard.json}). The set piece is parked off the field ({@link Squadron}); each ace is drawn where
 * it flies:
 *
 * <ul>
 *   <li>its body at the nearest of its 64 headings ({@code honour-guard-<ace>}, frame k = k x 360/64 degrees clockwise
 *       from moving down), a hit flashing it white; Harker's crest sheen over it ({@code -harker-crest}, additive, at 1,
 *       {@code crest_dim} while he holds fire for the pincer);
 *   <li>its tells, additive, from its playbook: the sniper's barrel glint ({@code -barrel-glint}, the tell's progress x
 *       6, at the {@code barrel} pivot), the pair volley's and the fan's red glint at the twin muzzles (the same frames),
 *       Vesper's ribbons flaring over the spiral loop's tell and fading {@value #RIBBON_FADE} s after ({@code
 *       -vesper-ribbons}), her wingtip trails through the roll's and the ribbon's tell and pass ({@code -wingtip}, a puff
 *       every {@value #PUFF_STEPS} steps at the {@code wingtips}, living {@value #PUFF_SECONDS} s), Kestrel's engines
 *       flaring over a dive's tell and both flankers' over the pincer's ({@code -engine-flare}, flickering every 2 steps),
 *       its tail band over the rake's ({@code -kestrel-band}); each first entry's engines flare as it flies in;
 *   <li>its gold hex shield ({@code enemy-shield-medium}, the units' set: holding at its fill, a hit at its rim toward
 *       the ship, its break, its refill), shattering at its death;
 *   <li>its entry tell ({@link Squadron.State#TELLING}): the gold chevron at its edge pointing in, pulsing at {@value
 *       #CHEVRON_HZ} Hz ({@code -chevron-<edge>}, {@code -chevron-glow-<edge>}), its call sign beside it ({@code
 *       -callsign-<ace>}) and the engines' glint at the edge ({@code -edge-glint}), drawn over the field;
 *   <li>its death: the hull tumbling away ({@code -<ace>-wreck}, {@value #WRECK_FPS} fps from the frame nearest its
 *       heading, turning the way it spins) where the simulation drifts it, the cockpit burning ({@code -cockpit-fire} at
 *       the wreck's {@code cockpit} while {@code visible}); the flash, the debris ring and the last ace's chain are the
 *       level screen's ({@code DeathEffects}, the pivots' {@code chain});
 *   <li>Harker's sniper line ({@link BossLines}): over its telegraph a thin red line from his barrel that tracks the ship
 *       ({@value #TRACK_WIDTH} px, flickering) until it locks, then the locked line at the telegraph's width brightening
 *       to the shot; live, a red band as wide as its hit band round a white core.
 * </ul>
 *
 * And its bar ({@link BossBar#drawSegments}): one segment per ace in join order, each as wide as its HP share, its call
 * sign above it; dim and hatched until it joins, a white flash as it does, a hit flashing its own segment, dark with its
 * tag struck through once dead. Nothing is allocated per frame.
 */
final class HonourGuardLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    static final String ROOT = "honour-guard";
    /** The aces' headings and wreck frames. */
    static final int HEADINGS = 64;

    static final int WRECK_FRAMES = 16;
    static final double WRECK_FPS = 12;
    /** The entry chevron's pulse. */
    static final double CHEVRON_HZ = 2;
    /** Vesper's ribbons fade this long after the spiral loop's tell. */
    static final double RIBBON_FADE = 0.3;
    /** A wingtip puff every this many steps, living this long. */
    static final int PUFF_STEPS = 2;

    static final double PUFF_SECONDS = 0.4;
    /** The cockpit fire's frames a second. */
    static final double FIRE_FPS = 12;
    /** The tracking line's width (the locked line's is {@link LineLooks#TELEGRAPH_WIDTH}). */
    static final float TRACK_WIDTH = 1;
    /**
     * A top entry's chevron sits this much lower than the inset (under the boss bar's plate, its tags and its name: the
     * step F4 capture had Harker's chevron and call sign over the bar).
     */
    static final int TOP_UNDER_BAR = 60;
    /** A segment's white flash as its ace joins, s. */
    static final double JOIN_FLASH_SECONDS = 0.4;

    private static final long NEVER = Long.MIN_VALUE / 4;
    private static final int PUFFS = 48;
    private static final String[] EDGE_NAMES = new String[WarningEdge.values().length];

    static {
        for (WarningEdge edge : WarningEdge.values()) {
            EDGE_NAMES[edge.ordinal()] = edge.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final FlashShader flash;
    private final TextureRegion pixel;
    private final BossBar bar;
    private final BossBar.Segments segments;
    private final int aces;
    /** Per ace (in the squadron's order): its part, its body and wreck frames, its own overlay (or null). */
    private final int[] part;

    private final java.util.List<Array<AtlasRegion>> body = new java.util.ArrayList<>();
    private final java.util.List<Array<AtlasRegion>> wreck = new java.util.ArrayList<>();
    private final java.util.List<Array<AtlasRegion>> overlay = new java.util.ArrayList<>();
    private final AtlasRegion[] callsign;
    private final Array<AtlasRegion> barrelGlint;
    private final Array<AtlasRegion> engineFlare;
    private final Array<AtlasRegion> wingtip;
    private final Array<AtlasRegion> cockpitFire;
    private final Array<AtlasRegion> edgeGlint;
    private final AtlasRegion[] chevron = new AtlasRegion[WarningEdge.values().length];
    private final AtlasRegion[] chevronGlow = new AtlasRegion[WarningEdge.values().length];
    private final Array<AtlasRegion> shieldHold;
    private final Array<AtlasRegion> shieldBreak;
    private final Array<AtlasRegion> shieldRefill;
    private final Array<AtlasRegion> shieldHit;
    /** The pivots per ace and heading, px from the frame's top left (y down): engines, wingtips, muzzles, barrel. */
    private final float[][][] engines;

    private final float[][][] wingtips;
    private final float[][][] muzzles;
    private final float[][] barrel;
    /** The wreck frames' cockpit and whether it shows. */
    private final float[][] wreckCockpit;

    private final boolean[] wreckVisible;
    private final float half;
    private final float crestDim;
    private final int inset;
    private final int callsignGap;
    /** The last ace's chain: per burst its time, its offset (px, y down) and whether it is {@code large}. */
    private final double[] chainT;

    private final double[] chainDx;
    private final double[] chainDy;
    private final boolean[] chainLarge;

    /** Per ace: its death's step, first wreck frame and turn; its shield's moments; its join; its tell's run start. */
    private final long[] downTick;

    private final int[] downFrame;
    private final int[] downTurn;
    private final long[] shieldHitTick;
    private final double[] shieldHitAngle;
    private final long[] shieldBreakTick;
    private final long[] shieldRefillTick;
    private final long[] joinTick;
    private final long[] runTick;
    private final int[] lastMode;
    private final long[] lastPuff;
    /** The wingtip puffs: where and when laid (a ring). */
    private final float[] puffX = new float[PUFFS];

    private final float[] puffY = new float[PUFFS];
    private final long[] puffTick = new long[PUFFS];
    private int nextPuff;
    private long seen = -1;

    private HonourGuardLooks(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        this.flash = flash;
        this.pixel = sprites.pixel;
        this.bar = new BossBar(sprites);
        var squadron = boss.boss().flatMap(BossSpec::squadron).orElseThrow();
        aces = squadron.aces().size();
        segments = new BossBar.Segments(aces);
        part = new int[aces];
        callsign = new AtlasRegion[aces];
        engines = new float[aces][][];
        wingtips = new float[aces][][];
        muzzles = new float[aces][][];
        barrel = new float[HEADINGS][];
        for (int a = 0; a < aces; a++) {
            part[a] = squadron.aces().get(a).part();
            String name = boss.parts().get(part[a]).name();
            segments.tag(a, squadron.aces().get(a).callSign());
            body.add(sprites.frames(ROOT + "-" + name));
            wreck.add(optional(sprites, ROOT + "-" + name + "-wreck"));
            Array<AtlasRegion> own = null;
            for (String mark : new String[] {"crest", "ribbons", "band"}) {
                if (sprites.has(ROOT + "-" + name + "-" + mark)) {
                    own = sprites.frames(ROOT + "-" + name + "-" + mark);
                }
            }
            overlay.add(own);
            callsign[a] = single(sprites, ROOT + "-callsign-" + name);
            segments.sign(a, callsign[a]);
            JsonValue headings = pivots == null ? null : pivots.get("aces").get(name);
            engines[a] = new float[HEADINGS][];
            wingtips[a] = new float[HEADINGS][];
            muzzles[a] = new float[HEADINGS][];
            for (int k = 0; k < HEADINGS; k++) {
                JsonValue at = headings == null ? null : headings.get(k);
                engines[a][k] = pairs(at, "engines");
                wingtips[a][k] = pairs(at, "wingtips");
                muzzles[a][k] = pairs(at, "muzzles");
                if (at != null && at.get("barrel") != null) {
                    barrel[k] = at.get("barrel").asFloatArray();
                }
            }
        }
        barrelGlint = optional(sprites, ROOT + "-barrel-glint");
        engineFlare = optional(sprites, ROOT + "-engine-flare");
        wingtip = optional(sprites, ROOT + "-wingtip");
        cockpitFire = optional(sprites, ROOT + "-cockpit-fire");
        edgeGlint = optional(sprites, ROOT + "-edge-glint");
        for (WarningEdge edge : WarningEdge.values()) {
            chevron[edge.ordinal()] = single(sprites, ROOT + "-chevron-" + EDGE_NAMES[edge.ordinal()]);
            chevronGlow[edge.ordinal()] = single(sprites, ROOT + "-chevron-glow-" + EDGE_NAMES[edge.ordinal()]);
        }
        shieldHold = optional(sprites, EnemyShieldLooks.ROOT + "-medium");
        shieldBreak = optional(sprites, EnemyShieldLooks.ROOT + "-medium-break");
        shieldRefill = optional(sprites, EnemyShieldLooks.ROOT + "-medium-refill");
        shieldHit = optional(sprites, EnemyShieldLooks.ROOT + "-hit");
        half = pivots == null ? 36 : pivots.get("centre").asFloatArray()[0];
        crestDim = pivots == null ? 0.15f : pivots.getFloat("crest_dim", 0.15f);
        JsonValue chev = pivots == null ? null : pivots.get("chevron");
        inset = chev == null ? 10 : chev.getInt("inset", 10);
        callsignGap = chev == null ? 3 : chev.getInt("callsign_gap", 3);
        JsonValue wrecks = pivots == null ? null : pivots.get("wreck");
        wreckCockpit = new float[WRECK_FRAMES][];
        wreckVisible = new boolean[WRECK_FRAMES];
        for (int f = 0; f < WRECK_FRAMES && wrecks != null && f < wrecks.size; f++) {
            wreckCockpit[f] = wrecks.get(f).get("cockpit").asFloatArray();
            wreckVisible[f] = wrecks.get(f).getBoolean("visible", true);
        }
        JsonValue chain = pivots == null ? null : pivots.get("chain");
        int bursts = chain == null ? 0 : chain.size;
        chainT = new double[bursts];
        chainDx = new double[bursts];
        chainDy = new double[bursts];
        chainLarge = new boolean[bursts];
        for (int i = 0; i < bursts; i++) {
            JsonValue burst = chain.get(i);
            chainT[i] = burst.getDouble("t");
            chainDx[i] = burst.getDouble("dx");
            chainDy[i] = burst.getDouble("dy");
            chainLarge[i] = "large".equals(burst.getString("size", "medium"));
        }
        downTick = new long[aces];
        downFrame = new int[aces];
        downTurn = new int[aces];
        shieldHitTick = new long[aces];
        shieldHitAngle = new double[aces];
        shieldBreakTick = new long[aces];
        shieldRefillTick = new long[aces];
        joinTick = new long[aces];
        runTick = new long[aces];
        lastMode = new int[aces];
        lastPuff = new long[aces];
        reset();
    }

    /** The looks of the level's boss when it is a squadron (its data's {@code squadron}) with its sprites; else null. */
    static HonourGuardLooks of(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        if (boss == null || boss.boss().flatMap(BossSpec::squadron).isEmpty() || pivots == null) {
            return null;
        }
        var squadron = boss.boss().flatMap(BossSpec::squadron).orElseThrow();
        for (var ace : squadron.aces()) {
            if (!sprites.has(ROOT + "-" + boss.parts().get(ace.part()).name())) {
                return null;
            }
        }
        return new HonourGuardLooks(sprites, flash, boss, pivots);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.region(name) : null;
    }

    /** A pivot entry's two points as {x0, y0, x1, y1}; null without it. */
    private static float[] pairs(JsonValue at, String name) {
        JsonValue points = at == null ? null : at.get(name);
        if (points == null || points.size < 2) {
            return null;
        }
        float[] a = points.get(0).asFloatArray();
        float[] b = points.get(1).asFloatArray();
        return new float[] {a[0], a[1], b[0], b[1]};
    }

    /** The level restarts (or the duel is retried): no death, no shield moment, no tell, no puff. */
    void reset() {
        Arrays.fill(downTick, NEVER);
        Arrays.fill(shieldHitTick, NEVER);
        Arrays.fill(shieldBreakTick, NEVER);
        Arrays.fill(shieldRefillTick, NEVER);
        Arrays.fill(joinTick, NEVER);
        Arrays.fill(runTick, NEVER);
        Arrays.fill(lastMode, -1);
        Arrays.fill(lastPuff, NEVER);
        Arrays.fill(puffTick, NEVER);
        seen = -1;
    }

    /** The heading frame (of {@link #HEADINGS}) nearest {@code facing}, radians clockwise from moving down. */
    static int headingFrame(double facing) {
        return Math.floorMod((int) Math.round(facing / (2 * Math.PI) * HEADINGS), HEADINGS);
    }

    /**
     * The wreck's frame {@code age} s after its death: from the frame nearest its heading at the death ({@code first}),
     * {@value #WRECK_FPS} fps the way it spins ({@code turn} +1 or -1).
     */
    static int wreckFrame(int first, int turn, double age) {
        return Math.floorMod(first + turn * (int) Math.floor(Math.max(0, age) * WRECK_FPS), WRECK_FRAMES);
    }

    /** The squadron piece of the sortie, or null. */
    private static SetPiece guard(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k).squadron().isPresent()) {
                return sortie.setPiece(k);
            }
        }
        return null;
    }

    /** After each simulation step: the deaths, the shields' moments, the joins, the tells' runs and the puffs. */
    void stepped(Sortie sortie) {
        SetPiece piece = guard(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (tick < seen) {
            // A retry from the boss checkpoint brings the aces back.
            reset();
        }
        seen = tick;
        Squadron squadron = piece.squadron().orElseThrow();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case ACE_DOWN -> {
                    int a = events.value(i);
                    if (a >= 0 && a < aces) {
                        downTick[a] = tick;
                        downFrame[a] = Math.floorMod(
                                (int) Math.round(squadron.facing(a) / (2 * Math.PI) * WRECK_FRAMES), WRECK_FRAMES);
                        downTurn[a] = squadron.spin(a) < 0 ? -1 : 1;
                        shieldBreakTick[a] = tick;
                    }
                }
                case ACE_JOINED -> {
                    int a = events.value(i);
                    if (a >= 0 && a < aces) {
                        joinTick[a] = tick;
                    }
                }
                case PART_SHIELD_HIT -> {
                    int a = squadron.ace(events.value(i));
                    if (a >= 0) {
                        shieldHitTick[a] = tick;
                        shieldHitAngle[a] = Math.atan2(
                                sortie.ship().y() - squadron.y(a), sortie.ship().x() - squadron.x(a));
                    }
                }
                case PART_SHIELD_BROKEN -> {
                    int a = squadron.ace(events.value(i));
                    if (a >= 0) {
                        shieldBreakTick[a] = tick;
                    }
                }
                case PART_SHIELD_REFILLED -> {
                    int a = squadron.ace(events.value(i));
                    if (a >= 0) {
                        shieldRefillTick[a] = tick;
                    }
                }
                default -> {}
            }
        }
        AcePlaybook book = squadron.playbook().orElse(null);
        for (int a = 0; a < aces; a++) {
            int mode = book == null ? -1 : book.mode(a).ordinal();
            if (mode == AcePlaybook.Mode.RUN.ordinal() && lastMode[a] != mode) {
                runTick[a] = tick;
            }
            lastMode[a] = mode;
            if (trailing(squadron, book, a) && tick - lastPuff[a] >= PUFF_STEPS) {
                lastPuff[a] = tick;
                layPuffs(squadron, a, tick);
            }
        }
    }

    /** Whether ace {@code a} trails its wingtips now: telling or flying the roll fan or the ribbon. */
    private static boolean trailing(Squadron squadron, AcePlaybook book, int a) {
        if (book == null || squadron.state(a) != Squadron.State.FLYING) {
            return false;
        }
        SquadronPlaybook.Kind kind = book.kind(a);
        return kind == SquadronPlaybook.Kind.ROLL_FAN || kind == SquadronPlaybook.Kind.RIBBON;
    }

    private void layPuffs(Squadron squadron, int a, long tick) {
        int k = headingFrame(squadron.facing(a));
        float[] tips = wingtips[a][k];
        if (tips == null) {
            return;
        }
        for (int t = 0; t < 2; t++) {
            puffX[nextPuff] = (float) (squadron.x(a) + tips[2 * t] - half);
            puffY[nextPuff] = (float) (squadron.y(a) + half - tips[2 * t + 1]);
            puffTick[nextPuff] = tick;
            nextPuff = (nextPuff + 1) % PUFFS;
        }
    }

    /** The aces' bodies, shields and wrecks, with the flyers. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        SetPiece piece = guard(sortie);
        if (piece == null || !piece.present()) {
            return;
        }
        Squadron squadron = piece.squadron().orElseThrow();
        long tick = sortie.tick();
        batch.setColor(Color.WHITE);
        for (int a = 0; a < aces; a++) {
            double x = squadron.renderX(a, alpha);
            double y = squadron.renderY(a, alpha);
            Squadron.State state = squadron.state(a);
            if (state == Squadron.State.DOWN) {
                if (squadron.tumbling(a) && wreck.get(a).size > 0 && downTick[a] != NEVER) {
                    double age = (tick - downTick[a] + alpha) * SimStep.SECONDS;
                    int f = wreckFrame(downFrame[a], downTurn[a], age);
                    drawCentred(batch, wreck.get(a).get(f % wreck.get(a).size), x, y);
                }
                continue;
            }
            if (state != Squadron.State.ENTERING && state != Squadron.State.FLYING) {
                continue;
            }
            AtlasRegion frame = body.get(a).get(headingFrame(squadron.facing(a)) % body.get(a).size);
            float hit = HullBossLooks.hitTint(piece.partTicksSinceHit(part[a]));
            if (hit > 0 && flash != null) {
                flash.draw(batch, frame, Math.round(X0 + x), Math.round(y), Color.WHITE, hit * whiteFlash);
            } else {
                drawCentred(batch, frame, x, y);
            }
        }
        // The shields over the hulls.
        PartShields shields = piece.shields().orElse(null);
        for (int a = 0; a < aces; a++) {
            drawShield(batch, squadron, shields, a, tick, alpha);
        }
        batch.setColor(Color.WHITE);
    }

    /** Ace {@code a}'s hex shield: holding (by its fill), a hit's rim, its break and its refill. */
    private void drawShield(SpriteBatch batch, Squadron squadron, PartShields shields, int a, long tick, float alpha) {
        if (shields == null || !shields.fitted(part[a]) || shieldHold.isEmpty()) {
            return;
        }
        Squadron.State state = squadron.state(a);
        double cx = squadron.renderX(a, alpha);
        double cy = squadron.renderY(a, alpha);
        long broke = tick - shieldBreakTick[a];
        if (shieldBreakTick[a] != NEVER
                && broke >= 0
                && broke < (long) shieldBreak.size * SovereignLooks.SHIELD_BREAK_TICKS) {
            batch.setColor(Color.WHITE);
            drawCentred(batch, shieldBreak.get((int) (broke / SovereignLooks.SHIELD_BREAK_TICKS)), cx, cy);
        }
        if (state != Squadron.State.FLYING || shields.shield(part[a]) <= 0) {
            return;
        }
        long refill = tick - shieldRefillTick[a];
        if (shieldRefillTick[a] != NEVER
                && refill >= 0
                && refill < (long) shieldRefill.size * SovereignLooks.SHIELD_REFILL_TICKS) {
            batch.setColor(Color.WHITE);
            drawCentred(batch, shieldRefill.get((int) (refill / SovereignLooks.SHIELD_REFILL_TICKS)), cx, cy);
        } else {
            batch.setColor(1, 1, 1, EnemyShieldLooks.holdOpacity(shields.share(part[a])));
            drawCentred(
                    batch,
                    shieldHold.get((int) ((tick / SovereignLooks.SHIELD_HOLD_TICKS + a) % shieldHold.size)),
                    cx,
                    cy);
        }
        long hit = tick - shieldHitTick[a];
        if (shieldHitTick[a] != NEVER && hit >= 0 && hit < (long) shieldHit.size * SovereignLooks.SHIELD_HIT_TICKS) {
            double r = shieldHold.first().getRegionWidth() / 2.0 - 6;
            batch.setColor(Color.WHITE);
            drawCentred(
                    batch,
                    shieldHit.get((int) (hit / SovereignLooks.SHIELD_HIT_TICKS)),
                    cx + r * Math.cos(shieldHitAngle[a]),
                    cy + r * Math.sin(shieldHitAngle[a]));
        }
        batch.setColor(Color.WHITE);
    }

    /** The aces' lights, additive over their bodies: the crest, the tells, the trails and the cockpit fires. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = guard(sortie);
        if (piece == null || !piece.present()) {
            return;
        }
        Squadron squadron = piece.squadron().orElseThrow();
        AcePlaybook book = squadron.playbook().orElse(null);
        long tick = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        // The wingtip trails, oldest first.
        for (int i = 0; i < PUFFS && wingtip.size > 0; i++) {
            int p = (nextPuff + i) % PUFFS;
            double age = (tick - puffTick[p] + alpha) * SimStep.SECONDS;
            if (puffTick[p] == NEVER || age < 0 || age >= PUFF_SECONDS) {
                continue;
            }
            int f = Math.min(wingtip.size - 1, (int) (age / PUFF_SECONDS * wingtip.size));
            batch.setColor(Color.WHITE);
            drawCentred(batch, wingtip.get(f), puffX[p], puffY[p]);
        }
        for (int a = 0; a < aces; a++) {
            double x = squadron.renderX(a, alpha);
            double y = squadron.renderY(a, alpha);
            Squadron.State state = squadron.state(a);
            if (state == Squadron.State.DOWN) {
                drawFire(batch, squadron, a, tick, alpha, x, y);
                continue;
            }
            if (state != Squadron.State.ENTERING && state != Squadron.State.FLYING) {
                continue;
            }
            int k = headingFrame(squadron.facing(a));
            float own = overlayLevel(book, a, tick, alpha);
            if (overlay.get(a) != null && own > 0) {
                batch.setColor(own, own, own, 1);
                drawCentred(batch, overlay.get(a).get(k % overlay.get(a).size), x, y);
            }
            batch.setColor(Color.WHITE);
            if (state == Squadron.State.ENTERING || flaring(book, a)) {
                drawAt(batch, engineFlare, (int) (tick / 2 % Math.max(1, engineFlare.size)), engines[a][k], x, y);
            }
            SquadronPlaybook.Kind kind = book == null ? null : book.kind(a);
            boolean telling = book != null && book.mode(a) == AcePlaybook.Mode.TELL;
            if (telling && barrelGlint.size > 0) {
                int f = Math.min(barrelGlint.size - 1, (int) (book.tellShare(a) * barrelGlint.size));
                if (kind == SquadronPlaybook.Kind.SNIPER_LINE && barrel[k] != null) {
                    drawCentred(batch, barrelGlint.get(f), x + barrel[k][0] - half, y + half - barrel[k][1]);
                } else if (kind == SquadronPlaybook.Kind.PAIR_VOLLEY || kind == SquadronPlaybook.Kind.FAN) {
                    drawAt(batch, barrelGlint, f, muzzles[a][k], x, y);
                }
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Whether ace {@code a}'s engines flare now: a dive's tell, or a flanker's in the pincer's tell. */
    private static boolean flaring(AcePlaybook book, int a) {
        if (book == null || book.mode(a) != AcePlaybook.Mode.TELL) {
            return false;
        }
        SquadronPlaybook.Kind kind = book.kind(a);
        return book.pincer(a) || kind == SquadronPlaybook.Kind.FEINT_DIVE || kind == SquadronPlaybook.Kind.OVERHEAD;
    }

    /**
     * The strength of ace {@code a}'s own overlay now: Harker's crest at 1 (dimmed while he holds fire for the pincer),
     * Vesper's ribbons over the spiral loop's tell and fading after it, Kestrel's band over the rake's tell and run.
     */
    private float overlayLevel(AcePlaybook book, int a, long tick, float alpha) {
        if (book == null) {
            return a == 0 ? 1 : 0;
        }
        SquadronPlaybook.Kind kind = book.kind(a);
        boolean telling = book.mode(a) == AcePlaybook.Mode.TELL;
        boolean running = book.mode(a) == AcePlaybook.Mode.RUN;
        if (kind == SquadronPlaybook.Kind.SPIRAL_LOOP) {
            if (telling) {
                return (float) book.tellShare(a);
            }
            if (running && runTick[a] != NEVER) {
                double since = (tick - runTick[a] + alpha) * SimStep.SECONDS;
                return (float) Math.max(0, 1 - since / RIBBON_FADE);
            }
            return 0;
        }
        if (kind == SquadronPlaybook.Kind.REAR_RAKE) {
            return telling ? (float) book.tellShare(a) : running ? 1 : 0;
        }
        // Harker's crest (the leader's, the squadron's first ace): lit, dimmed while he holds fire.
        if (a == 0) {
            return book.holdingFire() ? crestDim : 1;
        }
        return 0;
    }

    /** A dead ace's cockpit burning on its tumbling wreck. */
    private void drawFire(SpriteBatch batch, Squadron squadron, int a, long tick, float alpha, double x, double y) {
        if (!squadron.tumbling(a) || cockpitFire.size == 0 || downTick[a] == NEVER) {
            return;
        }
        double age = (tick - downTick[a] + alpha) * SimStep.SECONDS;
        int f = wreckFrame(downFrame[a], downTurn[a], age);
        if (!wreckVisible[f] || wreckCockpit[f] == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        int fire = (int) Math.floorMod((long) Math.floor(age * FIRE_FPS), (long) cockpitFire.size);
        drawCentred(batch, cockpitFire.get(fire), x + wreckCockpit[f][0] - half, y + half - wreckCockpit[f][1]);
    }

    /** {@code frames}' frame {@code f} at both points of a pivot pair (px from the frame's top left). */
    private void drawAt(SpriteBatch batch, Array<AtlasRegion> frames, int f, float[] points, double x, double y) {
        if (points == null || frames.size == 0) {
            return;
        }
        AtlasRegion region = frames.get(Math.min(f, frames.size - 1));
        for (int t = 0; t < 2; t++) {
            drawCentred(batch, region, x + points[2 * t] - half, y + half - points[2 * t + 1]);
        }
    }

    /** The aces' entry tells at their edges: the chevron pulsing, the call sign, the engines' glint; over the field. */
    void drawTells(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = guard(sortie);
        if (piece == null || !piece.present()) {
            return;
        }
        Squadron squadron = piece.squadron().orElseThrow();
        long tick = sortie.tick();
        double seconds = (tick + alpha) * SimStep.SECONDS;
        float pulse = (float) (0.5 + 0.5 * Math.sin(2 * Math.PI * CHEVRON_HZ * seconds));
        for (int a = 0; a < aces; a++) {
            if (squadron.state(a) != Squadron.State.TELLING) {
                continue;
            }
            WarningEdge edge = squadron.edge(a);
            AtlasRegion mark = chevron[edge.ordinal()];
            if (mark == null) {
                continue;
            }
            float[] at = chevronAt(edge, squadron.x(a), squadron.y(a), mark.getRegionWidth(), mark.getRegionHeight());
            double cx = at[0];
            double cy = at[1];
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            if (edgeGlint.size > 0) {
                batch.setColor(Color.WHITE);
                double ex = edge == WarningEdge.LEFT ? 0 : edge == WarningEdge.RIGHT ? PlayField.WIDTH : cx;
                double ey = edge == WarningEdge.BOTTOM ? 0 : cy;
                drawCentred(batch, edgeGlint.get((int) (tick / 3 % edgeGlint.size)), ex, ey);
            }
            AtlasRegion glow = chevronGlow[edge.ordinal()];
            if (glow != null) {
                batch.setColor(pulse, pulse, pulse, 1);
                drawCentred(batch, glow, cx, cy);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(Color.WHITE);
            drawCentred(batch, mark, cx, cy);
            AtlasRegion sign = callsign[a];
            if (sign != null) {
                double sx = cx;
                double sy = cy;
                float w = sign.getRegionWidth();
                float h = sign.getRegionHeight();
                switch (edge) {
                    case TOP -> sy = cy - mark.getRegionHeight() / 2.0 - callsignGap - h / 2.0;
                    case BOTTOM -> sy = cy + mark.getRegionHeight() / 2.0 + callsignGap + h / 2.0;
                    case LEFT -> sx = cx + mark.getRegionWidth() / 2.0 + callsignGap + w / 2.0;
                    case RIGHT -> sx = cx - mark.getRegionWidth() / 2.0 - callsignGap - w / 2.0;
                }
                sx = Math.clamp(sx, w / 2.0 + 2, PlayField.WIDTH - w / 2.0 - 2);
                drawCentred(batch, sign, sx, sy);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private final float[] chevronAt = new float[2];

    /**
     * Where an entry chevron {@code w} x {@code h} goes for an ace waiting at ({@code x}, {@code y}) beyond {@code edge}:
     * its centre {@link #inset} px inside the edge at the ace's crossing (a top one {@value #TOP_UNDER_BAR} px lower, under
     * the boss bar), kept wholly on the field.
     */
    float[] chevronAt(WarningEdge edge, double x, double y, int w, int h) {
        double cx = Math.clamp(x, w / 2.0 + 2, PlayField.WIDTH - w / 2.0 - 2);
        double cy = Math.clamp(y, h / 2.0 + 2, PlayField.HEIGHT - h / 2.0 - 2);
        switch (edge) {
            case TOP -> cy = PlayField.HEIGHT - inset - TOP_UNDER_BAR;
            case BOTTOM -> cy = inset;
            case LEFT -> cx = inset;
            case RIGHT -> cx = PlayField.WIDTH - inset;
        }
        chevronAt[0] = (float) cx;
        chevronAt[1] = (float) cy;
        return chevronAt;
    }

    /** Harker's sniper line: tracking, locked, live; with the bullets. */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = guard(sortie);
        if (piece == null || !piece.present() || piece.destroyed()) {
            return;
        }
        BossLines lines = piece.lines().orElse(null);
        if (lines == null || !lines.running() || lines.attack() < 0) {
            return;
        }
        EnemySpec.LaserLine line =
                piece.boss().orElseThrow().attacks().get(lines.attack()).line().orElse(null);
        if (line == null) {
            return;
        }
        Squadron squadron = piece.squadron().orElseThrow();
        int a = squadron.ace(lines.part());
        double ox = lines.fromX() - lines.stepX() * (1 - alpha);
        double oy = lines.fromY() - lines.stepY() * (1 - alpha);
        double in = 0;
        if (a >= 0) {
            float[] at = barrel[headingFrame(squadron.facing(a))];
            in = at == null ? half * 0.9 : Math.hypot(at[0] - half, at[1] - half);
        }
        double fx = ox + lines.dirX() * in;
        double fy = oy + lines.dirY() * in;
        long tick = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Color red = LineLooks.TELEGRAPH;
        if (lines.telegraphing()) {
            double charge = lines.charge();
            boolean locked = !line.tracks() || charge >= lockShare(line);
            float opacity = LineLooks.telegraphOpacity(charge, tick);
            if (!locked) {
                // Tracking: thin and unsteady until the lock.
                opacity *= tick / 3 % 2 == 0 ? 0.8f : 0.55f;
            }
            batch.setColor(red.r, red.g, red.b, opacity);
            stroke(batch, fx, fy, lines.dirX(), lines.dirY(), locked ? LineLooks.TELEGRAPH_WIDTH : TRACK_WIDTH);
        } else if (lines.live()) {
            batch.setColor(red.r, red.g, red.b, LineLooks.RAIL_OPACITY);
            stroke(batch, fx, fy, lines.dirX(), lines.dirY(), (float)
                    Math.max(LineLooks.CORE_WIDTH + 2, line.bandPx()));
            batch.setColor(LineLooks.CORE);
            stroke(batch, fx, fy, lines.dirX(), lines.dirY(), LineLooks.CORE_WIDTH);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The share of a tracking line's telegraph at which it locks ({@code lock_before} before it fires). */
    static double lockShare(EnemySpec.LaserLine line) {
        double telegraph = line.telegraphSeconds();
        return telegraph <= 0 ? 0 : Math.clamp((telegraph - line.lockBeforeSeconds()) / telegraph, 0, 1);
    }

    /** A line {@code width} px wide from (x, y) along (dx, dy) past the screen's edge. */
    private void stroke(SpriteBatch batch, double x, double y, double dx, double dy, float width) {
        float length = (float) (2 * (PlayField.WIDTH + PlayField.HEIGHT));
        batch.draw(pixel, (float) (X0 + x), (float) (y - width / 2), 0, width / 2, length, width, 1, 1, (float)
                Math.toDegrees(Math.atan2(dy, dx)));
    }

    /** Whether it draws the level's boss bar (the squadron's, in segments). */
    boolean drawsBar(Sortie sortie) {
        return guard(sortie) != null;
    }

    /** The bar in its three segments while the duel runs (the boss bar's rule: while the piece is present). */
    void drawBar(SpriteBatch batch, BitmapFont font, Sortie sortie) {
        SetPiece piece = guard(sortie);
        if (piece == null || !piece.present() || piece.boss().isEmpty()) {
            return;
        }
        Squadron squadron = piece.squadron().orElseThrow();
        long tick = sortie.tick();
        for (int a = 0; a < aces; a++) {
            double max = piece.spec().parts().get(part[a]).hp();
            segments.hp(a, Math.max(0, piece.partHp(part[a])), Math.max(max, piece.partHp(part[a])));
            Squadron.State state = squadron.state(a);
            segments.state(
                    a,
                    state == Squadron.State.DOWN
                            ? BossBar.Segments.DEAD
                            : squadron.joined(a) ? BossBar.Segments.LIVE : BossBar.Segments.WAITING);
            double joined = joinTick[a] == NEVER ? Double.POSITIVE_INFINITY : (tick - joinTick[a]) * SimStep.SECONDS;
            float join = joined < JOIN_FLASH_SECONDS ? (float) (1 - joined / JOIN_FLASH_SECONDS) : 0;
            float hit = state == Squadron.State.DOWN ? 0 : HullBossLooks.hitTint(piece.partTicksSinceHit(part[a]));
            segments.flash(a, Math.max(join, hit));
        }
        bar.drawSegments(batch, font, piece.boss().orElseThrow().barName(), segments);
    }

    /** The last ace's chain (the pivots' {@code chain}): its bursts. */
    int chainCount() {
        return chainT.length;
    }

    /** Burst {@code i}'s time after the kill, s. */
    double chainT(int i) {
        return chainT[i];
    }

    /** Burst {@code i}'s offset from the hull, px right. */
    double chainDx(int i) {
        return chainDx[i];
    }

    /** Burst {@code i}'s offset from the hull, px up (the pivots' y runs down). */
    double chainDy(int i) {
        return -chainDy[i];
    }

    /** Whether burst {@code i} is {@code large}. */
    boolean chainLarge(int i) {
        return chainLarge[i];
    }

    private static void drawCentred(SpriteBatch batch, TextureRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(X0 + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
