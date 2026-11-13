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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.ArenaWalls;
import vanguard.sim.BossPlaybook;
import vanguard.sim.LastWords;
import vanguard.sim.PartWindows;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H4 (design/enemies/bosses/choir-heart, Looks and Tells; design/campaign Level 50; ART-H's production
 * art, tools/art/choir_heart.py, {@code pivots/choir-heart.json}): the Choir Heart, drawn here rather than by {@link
 * BossLooks} (the {@link AscendantLooks}/{@link ChimeraLooks} pattern):
 *
 * <ul>
 *   <li>the crown: phase 1's sealed dome, the closing's four frames over its 4 s into phase 2's, the opening's eight
 *       along its spokes over 4 s into phase 3's, from K the collapse's twelve at 3 fps (the chain), held after; its
 *       halo under it and its <b>pulse</b> over it ({@link #pulse}: a smooth light, a raised cosine peaking on each
 *       {@code HEARTBEAT} of either beat, its period the beats' own: ≤ 0.5 Hz), drawn with the glows, under the
 *       bullets' cores (those are drawn last, over everything);
 *   <li>the six membranes on the rim: closed, irising open by their window ({@link PartWindows#iris}), open while they
 *       sing (their singers glowing over them), damaged below half their HP, dead (a burst, then its tatters);
 *   <li>the four wall nodes on the walls' faces ({@link ArenaWallLooks} draws the walls): whole, damaged, dead with
 *       their glow, burst and tatters; the core in phase 3 with its glow beating;
 *   <li>a part's fire at its port: a flare as its gun fires (the shot events to the living part whose muzzle it left:
 *       a node's {@link ArenaWalls#muzzleX}, any other part's centre), so a dead part's flare stops with its gun; the
 *       line's tell from the centre; a hit's flash on a living part only (a dead part never flashes);
 *   <li>step PT2 (the user's Level 50 playthrough): in a lull ({@link vanguard.sim.BossLulls}) the pulse dims to
 *       {@link #LULL_DIM} less over the lull's ease, the chamber's veins and pulse light with it (the membranes' irises
 *       open by their windows: the lull's tell);
 *   <li>K: the pulse fades over 0.5 s; the chain's bursts at the pivots' points and times; the act's last flash is the
 *       built screen flash on {@code BOSS_FLASH}.
 *   <li>step G6 (H2's attacks, H4's leftovers): the wall pits on the walls' faces ({@code -pit-l/-r}: shut, the lips
 *       swelling then parting over the pit brood's 0.8 s tell, open while it releases, closing after); the wall spit's
 *       flash at each vein point as it fires ({@code -spit}: the shot's frames, then its glow back down; its tell is
 *       the veins' glow, {@link #spitTelling}); the vacuum's debris rings ({@code -debris}, tumbling chunks thrown out
 *       in a ring) with every part's death burst and every burst of the chain.
 * </ul>
 *
 * Positions y up; the pivots' offsets y down from the piece's centre. Nothing is allocated per frame.
 */
public final class HeartLooks {
    /** The Choir Heart's slug. */
    public static final String SLUG = "choir-heart";
    /** The closing's and the opening's runs, s. */
    static final double CLOSING_SECONDS = 4;

    static final double OPENING_SECONDS = 4;
    /** The collapse's frame rate from K (12 frames over the 4 s chain). */
    static final double COLLAPSE_FPS = 3;
    /** A chain burst's, a part's death burst's and a singer's frame rate. */
    static final double BURST_FPS = 12;

    static final double SINGER_FPS = 12;
    /** The pulse fades out over this long from K, s. */
    static final double PULSE_FADE_SECONDS = 0.5;
    /**
     * Step PT2 (the Heart's lulls, {@link vanguard.sim.BossLulls}): the share of the pulse a lull takes away at its
     * deepest (the Heart's glow and the chamber's veins dim while it rests: the lull's tell).
     */
    static final double LULL_DIM = 0.6;
    /** The heartbeat's period before the first beat (phases 1-2's ring every 3 s). */
    static final double BEAT_SECONDS = 3;
    /** A shot this near a part's muzzle is its gun's, px. */
    static final double REACH = 8;
    /** A port flare: its frames, two steps each. */
    static final int FLARE_TICKS_PER_FRAME = 2;
    /** Step G6: a wall pit is centred this far inside its wall's face (the pivots' {@code pits}: into the rock), px. */
    static final double PIT_INSET = 8;
    /** Step G6: a pit closes over this long after its brood, s. */
    static final double PIT_CLOSE_SECONDS = 0.4;
    /** Step G6: the pits' frames (ART-H: 0 shut, 1-2 swelling, 3-4 parting, 5 open). */
    static final int PIT_OPEN = 5;
    /** Step G6: a wall spit's bullet starts this far in from its wall's face (the simulation's), px; matched within 2. */
    static final double SPIT_INSET = 8;
    /** Step G6: the spit's flash: its shot frames (4, 5) then its glow back down (3 .. 0), steps a frame. */
    static final int SPIT_TICKS_PER_FRAME = 3;
    /**
     * Step G6: the spit's flash is drawn at this scale on the wall's face (its vein point), 8 px behind the bullet it
     * fires: at 1× and on the bullet's place the bullet hid its glow (the capture's).
     */
    static final float SPIT_SCALE = 1.75f;
    /** Step G6: a debris ring's chunks, their speed (px/s, slowing to a stop over its life) and life, s. */
    static final int DEBRIS_CHUNKS = 8;

    static final double DEBRIS_SPEED = 90;
    static final double DEBRIS_SECONDS = 1.1;
    /** Step G6: the spit flashes kept at once (two walls x a volley's pairs). */
    private static final int SPITS = 16;
    /** A membrane's or a node's flare is drawn at this scale of the core's. */
    static final float PART_FLARE_SCALE = 0.6f;
    /** The halo's alpha: this plus the rest times the pulse. */
    static final float HALO_FLOOR = 0.4f;

    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final int MEMBRANES = 6;
    private static final int NODES = 4;

    private final FlashShader flash;
    private final HitFlash hits = new HitFlash();

    private final AtlasRegion body;
    private final Array<AtlasRegion> closing;
    private final Array<AtlasRegion> opening;
    private final Array<AtlasRegion> collapse;
    private final Array<AtlasRegion> pulseFrames;
    private final AtlasRegion halo;
    private final Array<AtlasRegion> openingLight;
    private final List<Array<AtlasRegion>> membrane = new ArrayList<>();
    private final List<Array<AtlasRegion>> singer = new ArrayList<>();
    private final Array<AtlasRegion> membraneDeath;
    private final Array<AtlasRegion> membraneTatters;
    private final Array<AtlasRegion> nodeLeft;
    private final Array<AtlasRegion> nodeRight;
    private final Array<AtlasRegion> nodeGlow;
    private final Array<AtlasRegion> nodeDeath;
    private final Array<AtlasRegion> nodeTatters;
    private final Array<AtlasRegion> core;
    private final Array<AtlasRegion> coreGlow;
    private final Array<AtlasRegion> coreFlare;
    private final Array<AtlasRegion> centreTell;
    private final Array<AtlasRegion> chain;
    /** Step G6: the wall pits (left and right), the wall spit's flash, the debris rings' chunks. */
    private final Array<AtlasRegion> pitLeft;

    private final Array<AtlasRegion> pitRight;
    private final Array<AtlasRegion> spit;
    private final Array<AtlasRegion> debris;
    /** Step G6: the wall pits: per pit its wall's sign (-1 left, +1 right) and its height (y up). */
    final double[][] pits;

    /** The body canvas's anchor (the piece's centre in it), px from its left and top. */
    private final float anchorX;

    private final float anchorY;
    /** The halo's offset from the piece's centre, y up. */
    private final double[] haloAt;
    /** The Heart's centre (the core's place), offset y up. */
    private final double[] centreAt;
    /** The chain's points (offsets y up) and their times from K, s. */
    private final double[][] chainAt;

    private final double[] chainT;
    /** The parts: the core, the membranes m1..m6, the nodes (n-lu, n-ll, n-ru, n-rl); -1 where missing. */
    private final int corePart;

    private final int[] membranes = new int[MEMBRANES];
    private final int[] nodes = new int[NODES];

    // Following the simulation.
    private long[] deathTick = new long[0];
    private long[] fired = new long[0];
    private boolean[] alive = new boolean[0];
    /** The phase and the step it started (NEVER before the Heart is seen). */
    private int phase = -1;

    private long phaseTick = NEVER;
    /** K (its last words spoken), NEVER before. */
    private long killTick = NEVER;
    /** The last heartbeat's step and the period between the last two, steps. */
    private long beatTick = NEVER;

    private long beatPeriod = SimStep.ticks(BEAT_SECONDS);
    /** Where it was at its last step on the field (its collapse's place from K), y up, and the ground's scroll at K. */
    private double lastX = Double.NaN;

    private double lastY = Double.NaN;
    private double groundAtKill;
    /** The step it was first seen (the pulse's clock before the first beat). */
    private long seenTick = NEVER;
    /** Step PT2: how deep in a lull the Heart was at the last step, 0-1 ({@link vanguard.sim.BossLulls#share}). */
    private double lull;
    /** The tick and its interpolation last drawn: the pulse's time. */
    private long drawnTick;

    private float drawnAlpha;
    /** Step G6: the last step a wall pit's brood ran (the pits close after it), NEVER before. */
    private long pitRunTick = NEVER;
    /** Step G6: the wall spit's flashes: their steps (NEVER: free), walls (-1/+1) and heights; the next slot. */
    private final long[] spitTick = new long[SPITS];

    private final int[] spitSide = new int[SPITS];
    private final double[] spitY = new double[SPITS];
    private int spitNext;

    private HeartLooks(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        this.flash = flash;
        body = sprites == null ? null : single(sprites, SLUG);
        closing = optional(sprites, SLUG + "-closing");
        opening = optional(sprites, SLUG + "-opening");
        collapse = optional(sprites, SLUG + "-collapse");
        pulseFrames = optional(sprites, SLUG + "-pulse");
        halo = sprites == null ? null : single(sprites, SLUG + "-halo");
        openingLight = optional(sprites, SLUG + "-opening-light");
        for (int i = 0; i < MEMBRANES; i++) {
            membrane.add(optional(sprites, SLUG + "-membrane-" + (i + 1)));
            singer.add(optional(sprites, SLUG + "-membrane-" + (i + 1) + "-singer"));
            membranes[i] = part(piece, "m" + (i + 1));
        }
        membraneDeath = optional(sprites, SLUG + "-membrane-death");
        membraneTatters = optional(sprites, SLUG + "-membrane-tatters");
        nodeLeft = optional(sprites, SLUG + "-node-l");
        nodeRight = optional(sprites, SLUG + "-node-r");
        nodeGlow = optional(sprites, SLUG + "-node-glow");
        nodeDeath = optional(sprites, SLUG + "-node-death");
        nodeTatters = optional(sprites, SLUG + "-node-tatters");
        core = optional(sprites, SLUG + "-core");
        coreGlow = optional(sprites, SLUG + "-core-glow");
        coreFlare = optional(sprites, SLUG + "-core-flare");
        centreTell = optional(sprites, SLUG + "-centre");
        chain = optional(sprites, SLUG + "-chain");
        pitLeft = optional(sprites, SLUG + "-pit-l");
        pitRight = optional(sprites, SLUG + "-pit-r");
        spit = optional(sprites, SLUG + "-spit");
        debris = optional(sprites, SLUG + "-debris");
        pits = wallPits(piece);
        Arrays.fill(spitTick, NEVER);
        String[] nodeNames = {"n-lu", "n-ll", "n-ru", "n-rl"};
        for (int i = 0; i < NODES; i++) {
            nodes[i] = part(piece, nodeNames[i]);
        }
        corePart = part(piece, "core");
        float[] anchor = pivots.get("body_centre").asFloatArray();
        anchorX = anchor[0];
        anchorY = anchor[1];
        haloAt = place(pivots.get("halo").get("at"));
        centreAt = place(pivots.get("centre"));
        JsonValue points = pivots.get("chain");
        chainAt = new double[points.size][];
        chainT = new double[points.size];
        for (int i = 0; i < points.size; i++) {
            chainAt[i] = place(points.get(i).get("at"));
            chainT[i] = points.get(i).getDouble("t");
        }
    }

    /** The Heart's looks for a level whose boss is the Choir Heart; null for any other. */
    static HeartLooks of(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        return drawn(piece) && pivots != null ? new HeartLooks(sprites, flash, piece, pivots) : null;
    }

    /** For the tests: the looks following a sortie of {@code piece} without its sprites (they draw nothing). */
    static HeartLooks following(SetPieceSpec piece, JsonValue pivots) {
        return new HeartLooks(null, null, piece, pivots);
    }

    /** Whether the set piece is drawn here (the Choir Heart), not by {@link BossLooks}. */
    public static boolean drawn(SetPieceSpec piece) {
        return piece != null && piece.slug().equals(SLUG);
    }

    /**
     * Step G6: the wall pits of the boss's playbooks' broods ({@code wall_pits}): per place its wall's sign (its {@code
     * dx}'s) and its height ({@code dy}, y up), each place once.
     */
    static double[][] wallPits(SetPieceSpec piece) {
        List<double[]> places = new ArrayList<>();
        piece.boss().ifPresent(boss -> boss.playbooks().forEach(book -> book.moves()
                .forEach(move -> move.bays().filter(PhasePlaybook.Bays::onWalls).ifPresent(bays -> bays.places()
                        .forEach(place -> {
                            double side = Math.signum(place.dx()) < 0 ? -1 : 1;
                            boolean known = places.stream().anyMatch(p -> p[0] == side && p[1] == place.dy());
                            if (!known) {
                                places.add(new double[] {side, place.dy()});
                            }
                        })))));
        return places.toArray(double[][]::new);
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
        return sprites.has(name) ? sprites.region(name) : null;
    }

    /** The level restarts (or the fight is retried from a checkpoint): no deaths, no K, no beats yet. */
    void reset() {
        Arrays.fill(deathTick, NEVER);
        Arrays.fill(fired, NEVER);
        Arrays.fill(alive, true);
        phase = -1;
        phaseTick = NEVER;
        killTick = NEVER;
        lastX = lastY = Double.NaN;
        beatTick = NEVER;
        beatPeriod = SimStep.ticks(BEAT_SECONDS);
        seenTick = NEVER;
        lull = 0;
        pitRunTick = NEVER;
        Arrays.fill(spitTick, NEVER);
    }

    /** The Choir Heart among the set pieces, or null. */
    static SetPiece heart(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (drawn(piece.spec())) {
                return piece;
            }
        }
        return null;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each simulation step: the phase's start, the parts' deaths and guns, the beats, K. */
    void stepped(Sortie sortie) {
        SetPiece piece = heart(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (!piece.present()) {
            // Destroyed at K (its last words at 0 s burst at once): K's step and place, the collapse from there.
            boolean spoken = piece.lastWords().map(LastWords::spoken).orElse(false);
            if (spoken && killTick == NEVER && seenTick != NEVER) {
                killTick = tick;
                groundAtKill = sortie.groundScroll();
            }
            return;
        }
        lastX = piece.renderX(1);
        lastY = piece.renderY(1);
        int count = piece.partCount();
        if (deathTick.length != count) {
            deathTick = new long[count];
            fired = new long[count];
            alive = new boolean[count];
            reset();
        }
        if (tick < seenTick || (killTick != NEVER && tick < killTick)) {
            // A retry from a checkpoint: back to its state then.
            reset();
        }
        if (seenTick == NEVER) {
            seenTick = tick;
        }
        if (piece.phase() != phase) {
            phaseTick = phase < 0 ? NEVER : tick;
            phase = piece.phase();
        }
        lull = piece.lulls().isPresent() ? piece.lulls().get().share() : 0;
        boolean spoken = piece.lastWords().map(LastWords::spoken).orElse(false);
        if (spoken && killTick == NEVER) {
            killTick = tick;
            groundAtKill = sortie.groundScroll();
        }
        ArenaWalls walls = sortie.arenaWalls();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type == SimEvents.Type.HEARTBEAT) {
                if (beatTick != NEVER && tick > beatTick) {
                    beatPeriod = tick - beatTick;
                }
                beatTick = tick;
            } else if (type == SimEvents.Type.ENEMY_FIRED
                    || type == SimEvents.Type.MISSILE_LAUNCHED
                    || type == SimEvents.Type.MORTAR_LOBBED) {
                int p = firer(piece, walls, events.x(i), events.y(i));
                if (p >= 0) {
                    fired[p] = tick;
                } else {
                    spat(walls, events.x(i), events.y(i), tick);
                }
            }
        }
        for (int p = 0; p < count; p++) {
            boolean now = piece.partHp(p) > 0;
            if (alive[p] && !now && !spoken) {
                deathTick[p] = tick;
            }
            alive[p] = now;
        }
        if (pitLane(piece) >= 0 && piece.playbook().orElseThrow().mode(pitLane(piece)) == BossPlaybook.Mode.RUN) {
            pitRunTick = tick;
        }
    }

    /** Step G6: a shot no part fired: a wall spit's when it leaves a standing wall's face at the spit's inset. */
    private void spat(ArenaWalls walls, double x, double y, long tick) {
        if (walls == null || !walls.standing()) {
            return;
        }
        int side = Math.abs(x - (walls.leftAt(y) + SPIT_INSET)) <= 2
                ? -1
                : Math.abs(x - (walls.rightAt(y) - SPIT_INSET)) <= 2 ? 1 : 0;
        if (side == 0) {
            return;
        }
        spitTick[spitNext] = tick;
        spitSide[spitNext] = side;
        spitY[spitNext] = y;
        spitNext = (spitNext + 1) % SPITS;
    }

    /** Step G6 (tests): the wall spit's flashes showing at {@code tick}. */
    int spitsShowing(long tick) {
        int n = 0;
        for (int i = 0; i < SPITS; i++) {
            n += spitFrame(tick - spitTick[i]) >= 0 ? 1 : 0;
        }
        return n;
    }

    /** Step G6: a spit flash's frame {@code since} steps after its shot: 4, 5, then 3 .. 0; -1 after (or before). */
    static int spitFrame(long since) {
        if (since < 0) {
            return -1;
        }
        int f = (int) (since / SPIT_TICKS_PER_FRAME);
        return f < 2 ? 4 + f : f < 6 ? 3 - (f - 2) : -1;
    }

    /** Step G6: the lane whose move is a wall pit's brood (told or running); -1 for none. */
    static int pitLane(SetPiece piece) {
        BossPlaybook book = piece.playbook().orElse(null);
        for (int l = 0; book != null && l < BossPlaybook.LANES; l++) {
            PhasePlaybook.Move move = book.moveSpec(l);
            if (move != null
                    && book.kind(l) == PhasePlaybook.Kind.BROOD
                    && move.bays().map(PhasePlaybook.Bays::onWalls).orElse(false)
                    && book.mode(l) != BossPlaybook.Mode.REST) {
                return l;
            }
        }
        return -1;
    }

    /** Step G6: whether a wall spit is told now (the walls' veins glow over its tell). */
    static boolean spitTelling(SetPiece piece) {
        BossPlaybook book = piece == null ? null : piece.playbook().orElse(null);
        for (int l = 0; book != null && l < BossPlaybook.LANES; l++) {
            if (book.kind(l) == PhasePlaybook.Kind.WALL_SPIT && book.mode(l) == BossPlaybook.Mode.TELL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Step G6: the pits' frame at {@code tick}: shut; over a brood's tell the lips swelling (1-2) then parting (3-4); open
     * while it releases; closing (4 .. 1) over {@value #PIT_CLOSE_SECONDS} s after.
     */
    int pitFrame(SetPiece piece, long tick) {
        int l = pitLane(piece);
        if (l >= 0) {
            BossPlaybook book = piece.playbook().orElseThrow();
            if (book.mode(l) == BossPlaybook.Mode.RUN) {
                return PIT_OPEN;
            }
            return (int) Math.clamp(1 + Math.floor(book.tellShare(l) * 4), 1, 4);
        }
        if (pitRunTick == NEVER || tick < pitRunTick) {
            return 0;
        }
        double since = seconds(tick - pitRunTick);
        return since >= PIT_CLOSE_SECONDS ? 0 : (int) Math.clamp(4 - Math.floor(since / PIT_CLOSE_SECONDS * 4), 1, 4);
    }

    /** The living part of {@code piece} whose gun fired a shot at (x, y): the nearest muzzle within {@value #REACH} px. */
    static int firer(SetPiece piece, ArenaWalls walls, double x, double y) {
        int best = -1;
        double nearest = REACH * REACH;
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.partHp(p) <= 0) {
                continue;
            }
            boolean node = walls != null && walls.rides(p);
            double dx = (node ? walls.muzzleX(p) : piece.partX(p)) - x;
            double dy = piece.partY(p) - y;
            double d = dx * dx + dy * dy;
            if (d <= nearest) {
                nearest = d;
                best = p;
            }
        }
        return best;
    }

    /** The steps since part {@code p}'s gun fired while its flare shows, else -1 (tests). */
    int flareTicks(int p, long tick) {
        if (p < 0 || p >= fired.length || fired[p] == NEVER || p >= alive.length || !alive[p]) {
            return -1;
        }
        long since = tick - fired[p];
        int frames = Math.max(1, coreFlare.size) * FLARE_TICKS_PER_FRAME;
        return since >= 0 && since < Math.max(frames, 6 * FLARE_TICKS_PER_FRAME) ? (int) since : -1;
    }

    /** K's step (the last words spoken), NEVER before. */
    long killTick() {
        return killTick;
    }

    // --- the pulse ---------------------------------------------------------------------------------

    /**
     * The pulse at {@code sinceBeat} s after a beat with the beats {@code period} s apart: a raised cosine, 1 on the beat,
     * 0 half-way, back to 1 at the next (smooth; at the slowest beat's 2 s period 0.5 Hz, never above 1 Hz).
     */
    static double pulse(double sinceBeat, double period) {
        double u = Math.clamp(sinceBeat / Math.max(period, 1e-6), 0, 1);
        return 0.5 + 0.5 * Math.cos(2 * Math.PI * u);
    }

    /** The pulse at the tick being drawn ({@code tick} plus {@code alpha} of a step): 0 before the Heart, fading at K. */
    double pulseAt(long tick, float alpha) {
        if (seenTick == NEVER) {
            return 0;
        }
        double now = tick + alpha;
        double period = beatPeriod * SimStep.SECONDS;
        double since = (now - (beatTick == NEVER ? seenTick : beatTick)) * SimStep.SECONDS;
        double p = pulse(since, period) * (1 - LULL_DIM * lull);
        if (killTick != NEVER) {
            p *= Math.clamp(1 - (now - killTick) * SimStep.SECONDS / PULSE_FADE_SECONDS, 0, 1);
        }
        return p;
    }

    /** The pulse at the tick last drawn (the backdrop's veins and pulse light, the dark's; 0 without the Heart). */
    public double pulseNow() {
        return pulseAt(drawnTick, drawnAlpha);
    }

    /** Whether the Heart has been seen (the backdrop's pulse follows it from then on, out at K). */
    boolean seen() {
        return seenTick != NEVER;
    }

    /** Whether the Heart is in the fight (seen and not yet dead): the backdrop's pulse follows its beats. */
    public boolean beating() {
        return seenTick != NEVER && killTick == NEVER;
    }

    /** Seconds since K at the tick being drawn; negative before K. */
    double sinceKill(long tick, float alpha) {
        return killTick == NEVER ? -1 : (tick + alpha - killTick) * SimStep.SECONDS;
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

    /** A frame {@code seconds} into a run of {@code frames} at {@code fps}, held at the last. */
    static int runFrame(double seconds, double fps, int frames) {
        return (int) Math.clamp(Math.floor(seconds * fps), 0, frames - 1);
    }

    /** A membrane's frame (ART-H's 13: closed 0, iris 1-4, open 5, damaged +6, dead 12) for its iris and state. */
    static int membraneFrame(double iris, boolean damaged, boolean dead) {
        if (dead) {
            return 12;
        }
        int open = iris <= 0 ? 0 : iris >= 1 ? 5 : (int) Math.clamp(1 + Math.floor(iris * 4), 1, 4);
        return open + (damaged ? 6 : 0);
    }

    /** The crown's body region now: phase 1 sealed, the closing, phase 2, the opening, phase 3, the collapse from K. */
    private TextureRegion bodyFrame(long tick, float alpha) {
        double sinceKill = sinceKill(tick, alpha);
        if (sinceKill >= 0 && !collapse.isEmpty()) {
            return collapse.get(runFrame(sinceKill, COLLAPSE_FPS, collapse.size));
        }
        double inPhase =
                phaseTick == NEVER ? Double.POSITIVE_INFINITY : seconds(tick - phaseTick) + alpha * SimStep.SECONDS;
        if (phase == 1 && !closing.isEmpty()) {
            return closing.get(runFrame(inPhase / CLOSING_SECONDS * closing.size, 1, closing.size));
        }
        if (phase >= 2 && !opening.isEmpty()) {
            return opening.get(runFrame(inPhase / OPENING_SECONDS * opening.size, 1, opening.size));
        }
        return body;
    }

    /** The body canvas at (cx, cy), its anchor on the piece's centre. */
    private void drawBody(SpriteBatch batch, TextureRegion region, double cx, double cy) {
        if (region == null) {
            return;
        }
        batch.draw(region, Math.round(X0 + cx - anchorX), Math.round(cy - (region.getRegionHeight() - anchorY)));
    }

    /**
     * The crown, its membranes, nodes and core, their deaths' tatters: the normal pass (darkened by the dark in phase
     * 2; the glows over it in {@link #drawGlows}).
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        SetPiece piece = heart(sortie);
        drawnTick = sortie.tick();
        drawnAlpha = alpha;
        if (piece == null || body == null || !piece.present() || killTick != NEVER) {
            // From K the collapse is drawn with the hive behind everything ({@link #drawDead}).
            return;
        }
        long tick = sortie.tick();
        double cx = piece.renderX(alpha);
        double cy = piece.renderY(alpha);
        double pulse = pulseAt(tick, alpha);
        if (halo != null && pulse > 0) {
            // The halo under the crown (ART-H: additive, 0.4 + 0.6 x the pulse; gone with the pulse after K).
            additive(batch);
            float share = (float) (HALO_FLOOR + (1 - HALO_FLOOR) * pulse);
            batch.setColor(1, 1, 1, killTick != NEVER ? (float) pulse * share : share);
            drawCentred(batch, halo, cx + haloAt[0], cy + haloAt[1]);
        }
        batch.setColor(Color.WHITE);
        normal(batch);
        drawBody(batch, bodyFrame(tick, alpha), cx, cy);
        boolean dead = killTick != NEVER;
        PartWindows windows = piece.partWindows().orElse(null);
        for (int i = 0; i < MEMBRANES; i++) {
            int p = membranes[i];
            if (p < 0 || membrane.get(i).isEmpty()) {
                continue;
            }
            double px = cx + piece.partOffsetX(p);
            double py = cy + piece.partOffsetY(p);
            boolean gone = piece.partHp(p) <= 0;
            double iris = windows == null || gone ? 0 : windows.iris(p);
            boolean damaged = piece.partHp(p) < piece.spec().parts().get(p).hp() / 2;
            AtlasRegion frame =
                    membrane.get(i).get(Math.min(membraneFrame(iris, damaged, gone), membrane.get(i).size - 1));
            drawPart(batch, piece, p, frame, px, py, tick, whiteFlash, !gone && !dead);
            if (gone) {
                drawRun(batch, membraneTatters, deathTick[p], tick, alpha, px, py, BURST_FPS, true);
            }
        }
        ArenaWalls walls = sortie.arenaWalls();
        for (int i = 0; i < NODES; i++) {
            int p = nodes[i];
            if (p < 0 || walls == null || !walls.standing()) {
                continue;
            }
            Array<AtlasRegion> frames = i < 2 ? nodeLeft : nodeRight;
            if (frames.isEmpty()) {
                continue;
            }
            double px = piece.partX(p);
            double py = piece.partY(p);
            boolean gone = piece.partHp(p) <= 0;
            int state = gone ? 2 : piece.partHp(p) < piece.spec().parts().get(p).hp() / 2 ? 1 : 0;
            drawPart(
                    batch,
                    piece,
                    p,
                    frames.get(Math.min(state, frames.size - 1)),
                    px,
                    py,
                    tick,
                    whiteFlash,
                    !gone && !dead);
            if (gone) {
                drawRun(batch, nodeTatters, deathTick[p], tick, alpha, px, py, BURST_FPS, true);
            }
        }
        if (walls != null && walls.standing() && !dead) {
            // Step G6: the wall pits on the walls' faces, in their brood's state.
            int f = pitFrame(piece, tick);
            for (double[] pit : pits) {
                Array<AtlasRegion> frames = pit[0] < 0 ? pitLeft : pitRight;
                if (frames.isEmpty()) {
                    continue;
                }
                double face = pit[0] < 0 ? walls.leftAt(pit[1]) - PIT_INSET : walls.rightAt(pit[1]) + PIT_INSET;
                drawCentred(batch, frames.get(Math.min(f, frames.size - 1)), face, pit[1]);
            }
        }
        if (phase >= 2 && corePart >= 0 && !core.isEmpty() && !dead) {
            double px = cx + piece.partOffsetX(corePart);
            double py = cy + piece.partOffsetY(corePart);
            boolean damaged =
                    piece.partHp(corePart) < piece.spec().parts().get(corePart).hp() / 2;
            drawPart(
                    batch,
                    piece,
                    corePart,
                    core.get(Math.min(damaged ? 1 : 0, core.size - 1)),
                    px,
                    py,
                    tick,
                    whiteFlash,
                    piece.partHp(corePart) > 0);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * From K: the collapse's frames at 3 fps where the Heart died, held after (the hive's: it rides the ground once the
     * scroll starts again), at alpha {@code a}; from K+10.5 greyed from the Heart outward by the closing gate's mask at
     * its progress {@code p} ({@code gate} null: never greyed). Drawn in the behind pass, under the race.
     */
    void drawDead(
            SpriteBatch batch, Sortie sortie, float alpha, double scroll, float a, ClosingGateLooks gate, float p) {
        if (killTick == NEVER || collapse.isEmpty() || Double.isNaN(lastX)) {
            return;
        }
        TextureRegion frame = collapse.get(runFrame(sinceKill(sortie.tick(), alpha), COLLAPSE_FPS, collapse.size));
        double cx = lastX;
        double cy = lastY - (scroll - groundAtKill);
        float left = (float) Math.round(cx - anchorX);
        float bottom = (float) Math.round(cy - (frame.getRegionHeight() - anchorY));
        if (bottom >= PlayField.HEIGHT || bottom + frame.getRegionHeight() <= 0) {
            return;
        }
        normal(batch);
        batch.setColor(1, 1, 1, a);
        batch.draw(frame, X0 + left, bottom);
        batch.setColor(Color.WHITE);
        if (gate != null && p > 0) {
            gate.greyHeart(batch, frame, left, bottom, p, a);
        }
    }

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

    /** A one-shot run of {@code frames} from {@code from}'s step at {@code fps}; held at its last when {@code hold}. */
    private static void drawRun(
            SpriteBatch batch,
            Array<AtlasRegion> frames,
            long from,
            long tick,
            float alpha,
            double x,
            double y,
            double fps,
            boolean hold) {
        if (frames.isEmpty() || from == NEVER) {
            return;
        }
        double since = (tick + alpha - from) * SimStep.SECONDS;
        int frame = (int) Math.floor(since * fps);
        if (since < 0 || (!hold && frame >= frames.size)) {
            return;
        }
        drawCentred(batch, frames.get(Math.min(frame, frames.size - 1)), x, y);
    }

    /**
     * The Heart's light, over the dark and under the bullets: its halo and pulse, the singers, the opening's light, the
     * nodes' and the core's glows, the line's tell at the centre, the parts' port flares, the parts' death bursts and,
     * from K, the chain's bursts.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = heart(sortie);
        if (piece != null && killTick != NEVER && !Double.isNaN(lastX)) {
            drawChain(batch, sortie.tick(), alpha);
            return;
        }
        if (piece == null || body == null || !piece.present()) {
            return;
        }
        long tick = sortie.tick();
        double cx = piece.renderX(alpha);
        double cy = piece.renderY(alpha);
        double pulse = pulseAt(tick, alpha);
        boolean dead = killTick != NEVER;
        additive(batch);
        if (!pulseFrames.isEmpty() && pulse > 0) {
            batch.setColor(1, 1, 1, (float) pulse);
            drawBody(batch, pulseFrames.get(Math.min(Math.max(phase, 0), pulseFrames.size - 1)), cx, cy);
        }
        if (phase == 2 && !dead && !openingLight.isEmpty() && phaseTick != NEVER) {
            double inPhase = seconds(tick - phaseTick) + alpha * SimStep.SECONDS;
            if (inPhase < OPENING_SECONDS) {
                batch.setColor(Color.WHITE);
                drawBody(
                        batch,
                        openingLight.get(runFrame(inPhase / OPENING_SECONDS * openingLight.size, 1, openingLight.size)),
                        cx,
                        cy);
            }
        }
        batch.setColor(Color.WHITE);
        PartWindows windows = piece.partWindows().orElse(null);
        double real = (tick + alpha) * SimStep.SECONDS;
        for (int i = 0; i < MEMBRANES && !dead; i++) {
            int p = membranes[i];
            if (p < 0
                    || piece.partHp(p) <= 0
                    || windows == null
                    || singer.get(i).isEmpty()) {
                continue;
            }
            double iris = windows.iris(p);
            if (iris <= 0) {
                continue;
            }
            batch.setColor(1, 1, 1, (float) iris);
            int f = (int) Math.floor(real * SINGER_FPS) % singer.get(i).size;
            drawCentred(batch, singer.get(i).get(f), cx + piece.partOffsetX(p), cy + piece.partOffsetY(p));
        }
        batch.setColor(Color.WHITE);
        ArenaWalls walls = sortie.arenaWalls();
        for (int i = 0; i < NODES && !dead; i++) {
            int p = nodes[i];
            if (p < 0 || walls == null || !walls.standing() || piece.partHp(p) <= 0 || nodeGlow.isEmpty()) {
                continue;
            }
            int f = (int) Math.floor(real * 4 + i * 2) % nodeGlow.size;
            drawCentred(batch, nodeGlow.get(f), piece.partX(p), piece.partY(p));
        }
        if (phase >= 2 && corePart >= 0 && !coreGlow.isEmpty() && !dead) {
            // ART-H: the glow's frame is the beat's phase x 8, its peak (frame 4) on the beat.
            double since = beatTick == NEVER ? 0 : (tick + alpha - beatTick) / (double) Math.max(1, beatPeriod);
            int f = (int) Math.floor(((since + 0.5) % 1.0) * coreGlow.size) % coreGlow.size;
            drawCentred(batch, coreGlow.get(f), cx + piece.partOffsetX(corePart), cy + piece.partOffsetY(corePart));
        }
        drawTell(batch, piece, cx, cy);
        for (int p = 0; p < piece.partCount() && !coreFlare.isEmpty() && !dead; p++) {
            int since = flareTicks(p, tick);
            if (since < 0) {
                continue;
            }
            int f = Math.min(since / FLARE_TICKS_PER_FRAME, coreFlare.size - 1);
            boolean node = walls != null && walls.rides(p);
            double px = node ? walls.muzzleX(p) : cx + piece.partOffsetX(p);
            double py = node ? piece.partY(p) : cy + piece.partOffsetY(p);
            AtlasRegion region = coreFlare.get(f);
            float scale = p == corePart ? 1 : PART_FLARE_SCALE;
            float w = region.getRegionWidth() * scale;
            float h = region.getRegionHeight() * scale;
            batch.draw(region, Math.round(X0 + px - w / 2), Math.round(py - h / 2), w, h);
        }
        if (walls != null && walls.standing() && !spit.isEmpty()) {
            // Step G6: the wall spit's flash at each vein point as its pair leaves the face.
            for (int i = 0; i < SPITS; i++) {
                int f = spitFrame(tick - spitTick[i]);
                if (f < 0) {
                    continue;
                }
                double y = spitY[i];
                double x = spitSide[i] < 0 ? walls.leftAt(y) : walls.rightAt(y);
                AtlasRegion region = spit.get(Math.min(f, spit.size - 1));
                float w = region.getRegionWidth() * SPIT_SCALE;
                float h = region.getRegionHeight() * SPIT_SCALE;
                batch.draw(region, Math.round(X0 + x - w / 2), Math.round(y - h / 2), w, h);
            }
        }
        normal(batch);
        for (int p = 0; p < deathTick.length; p++) {
            // Step G6: each part's death throws its debris ring (the vacuum's rule).
            if (deathTick[p] != NEVER) {
                boolean node = walls != null && walls.rides(p);
                double px = node ? piece.partX(p) : cx + piece.partOffsetX(p);
                double py = node ? piece.partY(p) : cy + piece.partOffsetY(p);
                drawDebris(batch, seconds(tick - deathTick[p]) + alpha * SimStep.SECONDS, px, py, p);
            }
        }
        additive(batch);
        for (int i = 0; i < MEMBRANES; i++) {
            int p = membranes[i];
            if (p >= 0 && deathTick.length > p) {
                drawRun(
                        batch,
                        membraneDeath,
                        deathTick[p],
                        tick,
                        alpha,
                        cx + piece.partOffsetX(p),
                        cy + piece.partOffsetY(p),
                        BURST_FPS,
                        false);
            }
        }
        for (int i = 0; i < NODES; i++) {
            int p = nodes[i];
            if (p >= 0 && deathTick.length > p) {
                drawRun(batch, nodeDeath, deathTick[p], tick, alpha, piece.partX(p), piece.partY(p), BURST_FPS, false);
            }
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    /** From K: the chain's bursts at the pivots' points and times round where the Heart died (additive). */
    private void drawChain(SpriteBatch batch, long tick, float alpha) {
        double sinceKill = sinceKill(tick, alpha);
        if (sinceKill < 0 || chain.isEmpty()) {
            return;
        }
        normal(batch);
        for (int i = 0; i < chainAt.length; i++) {
            // Step G6: each burst's debris ring (the vacuum's rule).
            drawDebris(batch, sinceKill - chainT[i], lastX + chainAt[i][0], lastY + chainAt[i][1], i);
        }
        additive(batch);
        batch.setColor(Color.WHITE);
        for (int i = 0; i < chainAt.length; i++) {
            double since = sinceKill - chainT[i];
            int f = (int) Math.floor(since * BURST_FPS);
            if (since >= 0 && f < chain.size) {
                drawCentred(batch, chain.get(f), lastX + chainAt[i][0], lastY + chainAt[i][1]);
            }
        }
        normal(batch);
    }

    /**
     * Step G6: a debris ring {@code since} s after its burst at (x, y): {@value #DEBRIS_CHUNKS} tissue chunks thrown out
     * evenly (turned by {@code seed}), slowing to a stop over {@value #DEBRIS_SECONDS} s, tumbling, fading over its last
     * half; nothing before or after.
     */
    private void drawDebris(SpriteBatch batch, double since, double x, double y, int seed) {
        if (debris.isEmpty() || since < 0 || since >= DEBRIS_SECONDS) {
            return;
        }
        double u = since / DEBRIS_SECONDS;
        double reach = DEBRIS_SPEED * DEBRIS_SECONDS * (u - u * u / 2);
        float fade = (float) Math.clamp(2 - 2 * u, 0, 1);
        batch.setColor(1, 1, 1, fade);
        for (int j = 0; j < DEBRIS_CHUNKS; j++) {
            double a = 2 * Math.PI * (j + 0.37 * seed) / DEBRIS_CHUNKS;
            double r = reach * (0.75 + 0.25 * ((j * 7 + seed * 3) % 5) / 4.0);
            int f = (int) Math.floor(since * BURST_FPS + j * 3 + seed) % debris.size;
            drawCentred(batch, debris.get(f), x + r * Math.cos(a), y + r * Math.sin(a));
        }
        batch.setColor(Color.WHITE);
    }

    /** The line's tell at the Heart's centre: its frames over the tell's 0.8 s, held at its last under the line. */
    private void drawTell(SpriteBatch batch, SetPiece piece, double cx, double cy) {
        BossPlaybook book = piece.playbook().orElse(null);
        if (book == null || centreTell.isEmpty() || killTick != NEVER) {
            return;
        }
        for (int l = 0; l < BossPlaybook.LANES; l++) {
            if (book.kind(l) != PhasePlaybook.Kind.LINE || book.mode(l) == BossPlaybook.Mode.REST) {
                continue;
            }
            int f = book.mode(l) == BossPlaybook.Mode.TELL
                    ? (int) Math.clamp(Math.floor(book.tellShare(l) * centreTell.size), 0, centreTell.size - 1)
                    : centreTell.size - 1;
            drawCentred(batch, centreTell.get(f), cx + centreAt[0], cy + centreAt[1]);
            return;
        }
    }
}
