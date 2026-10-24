package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Armament;
import vanguard.sim.Bulwark;
import vanguard.sim.Enemy;
import vanguard.sim.Flares;
import vanguard.sim.Shot;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;
import vanguard.sim.WeaponSpec;

/**
 * M8 part A, the Act 4 systems in flight (round 48's picks, the production frames of tools/art/act4_gear.py, whose
 * header gives the layouts, blending, timings and anchors used here): the Harpoon Torpedoes' cable trail, motor light
 * and impact over its dark cavity; the Depth Charges' toss over the hull (M8 AW: thrown forward from the stern rack,
 * swelling on its arc), their blinking fuse lamp and their pressure-dome burst over its collapsing cavity, sized by
 * the level's burst radius (on the fuse or, under water, on touching a swimmer: the same burst); the Sonar Pulse's dashed double ring and the contact brackets
 * on every unit it reached; the Decoy Flares with their halos; the Bulwark heavy drone with its lure halo while it draws
 * fire, its pump-jets, muzzle light, hit sparks and its under-water loss. Over water (Levels 11–13) the cables, lamps
 * and bursts are drawn in the {@code sub} pass with the shots that run under it. Pure presentation: it follows the
 * simulation's state and events and keeps nothing the game needs.
 */
public final class DeepWaterLooks {
    /** The harpoon's cable: its length behind the shot and its slack wave (act4_gear.py). */
    static final double CABLE_LENGTH = 150;

    static final float CABLE_ALPHA = 0.92f;
    static final double CABLE_WAVE = 1.8;
    static final double CABLE_WAVE_K = 0.08;
    static final double CABLE_WAVE_RATE = 9;
    /** A cable fades out over this many steps after its harpoon struck (0.35 s). */
    static final int CABLE_FADE_TICKS = Math.round(0.35f * SimStep.PER_SECOND);
    /** A drawn cable piece is at most this long, px, so the wave reads smoothly. */
    private static final double CABLE_PIECE = 4;
    /** The cable's points: a step's travel is at most ~9 px, so 150 px needs about 20; spare for slow ones. */
    private static final int CABLE_POINTS = 48;

    private static final int MAX_CABLES = 24;
    /** The motor's light: this far behind the shot's centre along its heading, px. */
    private static final double GLOW_BEHIND = 11;
    /** The harpoon's impact and its cavity: 3 steps a frame. */
    private static final int HIT_FRAME_TICKS = 3;
    /** The depth charge's burst: 3 steps a frame; its cavity under burst frames 2..5. */
    private static final int BURST_FRAME_TICKS = 3;

    private static final int CAVITY_DELAY_TICKS = 2 * BURST_FRAME_TICKS;
    /** The burst sizes there are: the levels' burst radii (act4_gear.py). */
    static final int[] BURST_RADII = {40, 46, 52, 58, 64};
    /** The fuse lamp's blink: its period from 0.32 s down to 0.08 s at the fuse's end, lit in each period's first half. */
    static final double LAMP_PERIOD = 0.32;

    static final double LAMP_SPEED_UP = 0.24;
    /**
     * M8 AW: a depth charge is tossed forward from the stern rack over the hull: for this many steps after its launch
     * (0.15 s) it is drawn over the ship, swelling by up to {@value #TOSS_SWELL} of its size at the toss's top.
     */
    static final int TOSS_TICKS = Math.round(0.15f * SimStep.PER_SECOND);

    static final float TOSS_SWELL = 0.3f;
    /** The Sonar Pulse ring: 36 dashes of 6 degrees every 10, the outer ring at r, the inner at r - 7, dimmer. */
    static final int SONAR_DASHES = 36;

    private static final double DASH_DEGREES = 6;
    private static final double INNER_RING = 7;
    private static final float OUTER_ALPHA = 240 / 255f;
    private static final float INNER_ALPHA = 130 / 255f;
    /** A contact bracket: 3 px outside the sprite box, snapping in from 8 px further out over 0.3 s, pinging 0.4 s. */
    static final int BRACKET_GAP = 3;

    static final int BRACKET_SNAP = 8;
    static final int BRACKET_SNAP_TICKS = Math.round(0.3f * SimStep.PER_SECOND);
    static final int BRACKET_PING_TICKS = Math.round(0.4f * SimStep.PER_SECOND);
    /** In the window's last second the brackets blink: hidden every other 1/8 s. */
    static final double BRACKET_BLINK = 0.125;

    private static final int MAX_CONTACTS = 96;
    /** The flares' flicker: 2 steps a frame; they fade over the last eighth of their burn (0.5 s of 4). */
    private static final int FLARE_FRAME_TICKS = 2;

    private static final double FLARE_FADE = 0.125;
    /** The Bulwark: its hit frame for this many steps, its jets' flicker, its halo's breath, its muzzle and sparks. */
    static final int BULWARK_HIT_TICKS = 6;

    private static final int JETS_FRAME_TICKS = 2;
    private static final int LURE_FRAME_TICKS = 6;
    private static final int MUZZLE_TICKS = 6;
    private static final double MUZZLE_AHEAD = 18;
    private static final double SPARK_AHEAD = 6;
    private static final int SPARK_FRAME_TICKS = 4;
    private static final int LOSS_FRAME_TICKS = 3;
    /** Its frames: intact, hit, armour under half. */
    private static final int INTACT = 0;

    private static final int HIT = 1;
    private static final int SCORCHED = 2;

    private final WeaponLooks weapons;
    private final EnemyLooks[] looks;
    /** Whether the level is over water: the cables, lamps and bursts run in the {@code sub} pass. */
    private final boolean overWater;

    private final AtlasRegion cable;
    private final AtlasRegion glow;
    private final Array<AtlasRegion> hit;
    private final Array<AtlasRegion> hitCavity;
    private final AtlasRegion lamp;
    private final java.util.List<Array<AtlasRegion>> bursts = new java.util.ArrayList<>();
    private final java.util.List<Array<AtlasRegion>> burstCavities = new java.util.ArrayList<>();
    private final AtlasRegion dash;
    private final Array<AtlasRegion> corner;
    private final Array<AtlasRegion> flare;
    private final AtlasRegion flareGlow;
    private final Array<AtlasRegion> bulwark;
    private final Array<AtlasRegion> jets;
    private final Array<AtlasRegion> lure;
    private final AtlasRegion bulwarkMuzzle;
    private final Array<AtlasRegion> spark;
    private final Array<AtlasRegion> loss;
    private final Array<AtlasRegion> lossDebris;

    /** Under the effects: the cavities (solid); the impacts, bursts, sparks and loss (glowing); the loss's debris. */
    private final Effects under = Effects.solid();

    private final Effects over = Effects.glowing();
    private final Effects overSolid = Effects.solid();
    /** Over water, the depth charges' cavities and bursts under the surface. */
    private final Effects subUnder = Effects.solid();

    private final Effects subOver = Effects.glowing();

    /** The cables: per record the shot it follows (null for a free record), its points newest first. */
    private final Shot[] cableShot = new Shot[MAX_CABLES];

    private final int[] cableAge = new int[MAX_CABLES];
    /** Steps since its harpoon was gone (struck or left), -1 while it flies. */
    private final int[] cableFade = new int[MAX_CABLES];

    private final boolean[] cableSeen = new boolean[MAX_CABLES];
    private final double[][] cableX = new double[MAX_CABLES][CABLE_POINTS];
    private final double[][] cableY = new double[MAX_CABLES][CABLE_POINTS];
    private final int[] cablePoints = new int[MAX_CABLES];

    /** The Sonar Pulse's contacts: the units its ring reached (by serial) and the step it reached each. */
    private final int[] contactSerial = new int[MAX_CONTACTS];

    private final long[] contactTick = new long[MAX_CONTACTS];
    private int contacts;

    private long bulwarkHitTick = Long.MIN_VALUE / 2;

    DeepWaterLooks(Sprites sprites, EnemyLooks[] looks, WeaponLooks weapons, boolean overWater) {
        this.weapons = weapons;
        this.looks = looks;
        this.overWater = overWater;
        cable = optionalRegion(sprites, "harpoon-torpedoes-cable");
        glow = optionalRegion(sprites, "harpoon-torpedoes-glow");
        hit = optional(sprites, "harpoon-torpedoes-hit");
        hitCavity = optional(sprites, "harpoon-torpedoes-cavity");
        lamp = optionalRegion(sprites, "depth-charges-lamp");
        for (int radius : BURST_RADII) {
            bursts.add(optional(sprites, "depth-charges-burst-" + radius));
            burstCavities.add(optional(sprites, "depth-charges-cavity-" + radius));
        }
        dash = optionalRegion(sprites, "sonar-pulse-dash");
        corner = optional(sprites, "sonar-pulse-corner");
        flare = optional(sprites, "decoy-flares-flare");
        flareGlow = optionalRegion(sprites, "decoy-flares-glow");
        bulwark = optional(sprites, "bulwark");
        jets = optional(sprites, "bulwark-jets");
        lure = optional(sprites, "bulwark-lure");
        bulwarkMuzzle = optionalRegion(sprites, "bulwark-muzzle");
        spark = optional(sprites, "bulwark-spark");
        loss = optional(sprites, "bulwark-loss");
        lossDebris = optional(sprites, "bulwark-debris");
        Arrays.fill(cableFade, -1);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion optionalRegion(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.region(name) : null;
    }

    /** The level restarts: no cable, contact, effect or flash left. */
    void restart() {
        Arrays.fill(cableShot, null);
        Arrays.fill(cableFade, -1);
        Arrays.fill(cablePoints, 0);
        contacts = 0;
        bulwarkHitTick = Long.MIN_VALUE / 2;
        under.clear();
        over.clear();
        overSolid.clear();
        subUnder.clear();
        subOver.clear();
    }

    /** One simulation step of its effects (with the screen's). */
    void stepEffects() {
        under.step();
        over.step();
        overSolid.step();
        subUnder.step();
        subOver.step();
    }

    /** After each simulation step: the cables follow their harpoons, the step's events start their effects. */
    void stepped(Sortie sortie) {
        followCables(sortie);
        SimEvents events = sortie.events();
        double scroll = sortie.groundScroll();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            int value = events.value(i);
            switch (events.type(i)) {
                case ENEMY_HIT -> {
                    if (weapons.ownImpact(value)) {
                        harpoonHit(x, y, false, scroll);
                    }
                }
                case GROUND_HIT -> {
                    if (weapons.ownImpact(value)) {
                        harpoonHit(x, y, true, scroll);
                    }
                }
                case BLAST -> {
                    if (weapons.depthCharge(value)) {
                        burst(burstIndex(chargeRadius(sortie, value)), x, y, scroll);
                    }
                }
                case SONAR_CONTACT -> contact(value, sortie.tick());
                case SONAR_ENDED -> contacts = 0;
                case BULWARK_HIT -> {
                    bulwarkHitTick = sortie.tick();
                    if (spark.size > 0) {
                        over.start(spark, SPARK_FRAME_TICKS, x, y + SPARK_AHEAD);
                    }
                }
                case BULWARK_DESTROYED -> {
                    if (loss.size > 0) {
                        over.start(loss, LOSS_FRAME_TICKS, x, y);
                    }
                    if (lossDebris.size > 0) {
                        overSolid.start(lossDebris, LOSS_FRAME_TICKS, x, y);
                    }
                }
                default -> {}
            }
        }
    }

    private void harpoonHit(double x, double y, boolean onGround, double scroll) {
        if (onGround) {
            if (hitCavity.size > 0) {
                under.startOnGround(hitCavity, HIT_FRAME_TICKS, x, y, 0, scroll);
            }
            over.startOnGround(hit, HIT_FRAME_TICKS, x, y, 0, scroll);
        } else {
            if (hitCavity.size > 0) {
                under.start(hitCavity, HIT_FRAME_TICKS, x, y);
            }
            over.start(hit, HIT_FRAME_TICKS, x, y);
        }
    }

    /** A depth charge's burst of size {@code k} on the ground (it sinks with the scroll) with its cavity under it. */
    private void burst(int k, double x, double y, double scroll) {
        Effects light = overWater ? subOver : over;
        Effects dark = overWater ? subUnder : under;
        if (burstCavities.get(k).size > 0) {
            dark.startOnGround(burstCavities.get(k), BURST_FRAME_TICKS, x, y, CAVITY_DELAY_TICKS, scroll);
        }
        if (bursts.get(k).size > 0) {
            light.startOnGround(bursts.get(k), BURST_FRAME_TICKS, x, y, 0, scroll);
        }
    }

    /** The burst radius of mount {@code m}'s depth charges now (its overdrive's while one runs). */
    private static double chargeRadius(Sortie sortie, int m) {
        if (m < 0 || m >= sortie.armament().size()) {
            return BURST_RADII[0];
        }
        Armament.Mount mount = sortie.armament().mount(m);
        WeaponSpec weapon = sortie.overdriveSeconds() > 0 ? mount.overdrive() : mount.weapon();
        return weapon.blast();
    }

    /** The burst sprite nearest a burst radius of {@code radius} px. */
    static int burstIndex(double radius) {
        int best = 0;
        for (int k = 1; k < BURST_RADII.length; k++) {
            if (Math.abs(BURST_RADII[k] - radius) < Math.abs(BURST_RADII[best] - radius)) {
                best = k;
            }
        }
        return best;
    }

    private void contact(int serial, long tick) {
        for (int i = 0; i < contacts; i++) {
            if (contactSerial[i] == serial) {
                contactTick[i] = tick;
                return;
            }
        }
        if (contacts < MAX_CONTACTS) {
            contactSerial[contacts] = serial;
            contactTick[contacts] = tick;
            contacts++;
        }
    }

    /**
     * Each harpoon in flight keeps a record of the points it passed (its cable); a record whose harpoon is gone fades
     * out, and a pooled shot that starts again is a new harpoon.
     */
    private void followCables(Sortie sortie) {
        Arrays.fill(cableSeen, false);
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            if (!harpoon(shot)) {
                continue;
            }
            int record = -1;
            for (int r = 0; r < MAX_CABLES; r++) {
                if (cableShot[r] == shot && cableFade[r] < 0 && shot.age() > cableAge[r]) {
                    record = r;
                    break;
                }
            }
            if (record < 0) {
                record = freeCable();
                if (record < 0) {
                    continue;
                }
                cableShot[record] = shot;
                cablePoints[record] = 0;
                cableFade[record] = -1;
                // Its first point: where it left the nose.
                push(record, shot.renderX(0), shot.renderY(0));
            }
            cableAge[record] = shot.age();
            cableSeen[record] = true;
            push(record, shot.renderX(1), shot.renderY(1));
        }
        for (int r = 0; r < MAX_CABLES; r++) {
            if (cableShot[r] == null || cableSeen[r]) {
                continue;
            }
            if (cableFade[r] < 0) {
                cableFade[r] = 0;
            } else if (++cableFade[r] >= CABLE_FADE_TICKS) {
                cableShot[r] = null;
                cableFade[r] = -1;
            }
        }
    }

    private boolean harpoon(Shot shot) {
        return shot.mount() < weapons.size()
                && shot.weapon().delivery() == WeaponSpec.Delivery.TORPEDO
                && weapons.ownImpact(shot.mount());
    }

    private int freeCable() {
        int oldest = -1;
        for (int r = 0; r < MAX_CABLES; r++) {
            if (cableShot[r] == null) {
                return r;
            }
            if (cableFade[r] >= 0 && (oldest < 0 || cableFade[r] > cableFade[oldest])) {
                oldest = r;
            }
        }
        return oldest;
    }

    /** Adds a point to a cable's front, newest first, dropping its oldest past the cable's length. */
    private void push(int r, double x, double y) {
        double[] xs = cableX[r];
        double[] ys = cableY[r];
        int n = Math.min(cablePoints[r] + 1, CABLE_POINTS);
        System.arraycopy(xs, 0, xs, 1, n - 1);
        System.arraycopy(ys, 0, ys, 1, n - 1);
        xs[0] = x;
        ys[0] = y;
        cablePoints[r] = n;
    }

    /** The share of a cable's strength {@code fade} steps after its harpoon was gone (-1: flying). */
    static float cableStrength(int fade) {
        return fade < 0 ? 1 : Math.max(0, 1 - (float) fade / CABLE_FADE_TICKS);
    }

    /** The cable's sideways slack at {@code l} px behind the harpoon at {@code seconds}, px. */
    static double cableWave(double l, double seconds) {
        return CABLE_WAVE * (l / CABLE_LENGTH) * Math.sin(CABLE_WAVE_K * l - CABLE_WAVE_RATE * seconds);
    }

    /**
     * The harpoons' cables and motor lights (additive), under the harpoons: in the main pass, or over water in the
     * {@code sub} pass ({@link #drawSub}). Leaves the batch in its normal blending.
     */
    void drawUnderShots(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!overWater) {
            drawCables(batch, sortie, alpha);
        }
    }

    private void drawCables(SpriteBatch batch, Sortie sortie, float alpha) {
        if (cable == null) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        double seconds = (sortie.tick() - 1 + alpha) * SimStep.SECONDS;
        for (int r = 0; r < MAX_CABLES; r++) {
            Shot shot = cableShot[r];
            if (shot == null || cablePoints[r] < 1) {
                continue;
            }
            float strength = cableStrength(cableFade[r]);
            // The head: the harpoon drawn now while it flies, else where it struck.
            double hx = cableFade[r] < 0 ? shot.renderX(alpha) : cableX[r][0];
            double hy = cableFade[r] < 0 ? shot.renderY(alpha) : cableY[r][0];
            drawCable(batch, r, hx, hy, strength, seconds);
            if (cableFade[r] < 0 && glow != null) {
                double heading = shot.heading();
                batch.setColor(1, 1, 1, 1);
                drawCentred(batch, glow, hx - Math.sin(heading) * GLOW_BEHIND, hy - Math.cos(heading) * GLOW_BEHIND);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** One cable from its head back along its points, in pieces of at most {@value #CABLE_PIECE} px. */
    private void drawCable(SpriteBatch batch, int r, double hx, double hy, float strength, double seconds) {
        double[] xs = cableX[r];
        double[] ys = cableY[r];
        double l = 0;
        double ax = hx;
        double ay = hy;
        int start = cableFade[r] < 0 ? 0 : 1;
        for (int p = start; p < cablePoints[r] && l < CABLE_LENGTH; p++) {
            double bx = xs[p];
            double by = ys[p];
            double length = Math.hypot(bx - ax, by - ay);
            if (length < 1e-6) {
                continue;
            }
            int pieces = Math.max(1, (int) Math.ceil(length / CABLE_PIECE));
            // Across the segment, for the slack's sideways offset.
            double nx = -(by - ay) / length;
            double ny = (bx - ax) / length;
            for (int k = 0; k < pieces && l < CABLE_LENGTH; k++) {
                double t0 = (double) k / pieces;
                double t1 = (double) (k + 1) / pieces;
                double l0 = l + length * t0;
                double l1 = Math.min(CABLE_LENGTH, l + length * t1);
                double w0 = cableWave(l0, seconds);
                double w1 = cableWave(l1, seconds);
                double x0 = ax + (bx - ax) * t0 + nx * w0;
                double y0 = ay + (by - ay) * t0 + ny * w0;
                double x1 = ax + (bx - ax) * t1 + nx * w1;
                double y1 = ay + (by - ay) * t1 + ny * w1;
                float a = CABLE_ALPHA * strength * (float) (1 - (l0 + l1) / 2 / CABLE_LENGTH);
                if (a > 0) {
                    batch.setColor(1, 1, 1, a);
                    piece(batch, x0, y0, x1, y1);
                }
            }
            l += length;
            ax = bx;
            ay = by;
        }
    }

    /** The cable sprite stretched from (x0, y0) to (x1, y1): its x across, its y along. */
    private void piece(SpriteBatch batch, double x0, double y0, double x1, double y1) {
        float width = cable.getRegionWidth();
        float along = (float) Math.hypot(x1 - x0, y1 - y0);
        // The sprite's up (its y) along the piece: the angle from up, counter-clockwise.
        float rotation = (float) Math.toDegrees(Math.atan2(-(x1 - x0), y1 - y0));
        batch.draw(cable, (float) (X0 + x0 - width / 2), (float) y0, width / 2, 0, width, along, 1, 1, rotation);
    }

    /** M8 AW: whether {@code shot} is a depth charge still in its toss over the hull (drawn by {@link #drawTossed}). */
    static boolean tossed(Shot shot) {
        return shot.weapon().delivery() == WeaponSpec.Delivery.DEPTH_CHARGE && shot.age() <= TOSS_TICKS;
    }

    /**
     * M8 AW: a tossed depth charge's scale {@code ticks} steps (a fraction at render time) after its launch: it swells
     * by up to {@value #TOSS_SWELL} on its arc over the hull and is back to its size as it drops in ahead.
     */
    static float tossScale(double ticks) {
        double p = Math.clamp(ticks / TOSS_TICKS, 0, 1);
        return 1 + TOSS_SWELL * (float) Math.sin(Math.PI * p);
    }

    /** The scale of a tossed {@code shot} drawn at {@code alpha} (its position runs a step behind its age). */
    static float tossScale(Shot shot, float alpha) {
        return tossScale(shot.age() - 1 + alpha);
    }

    /**
     * M8 AW, over the ship (normal blending): the depth charges in their toss, thrown from the stern rack over the
     * hull, swelling on their arc; the shot passes and the {@code sub} pass leave them out until they drop in ahead.
     */
    void drawTossed(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            if (!tossed(shot) || shot.mount() >= weapons.size()) {
                continue;
            }
            PlayFrame.drawScaled(
                    batch, weapons.sprite(shot), shot.renderX(alpha), shot.renderY(alpha), tossScale(shot, alpha));
        }
    }

    /** The depth charges' fuse lamps and the Decoy Flares (additive; the batch is in additive blending). */
    void drawOverShots(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!overWater) {
            drawLamps(batch, sortie, alpha);
        }
        drawFlares(batch, sortie, alpha);
    }

    private void drawLamps(SpriteBatch batch, Sortie sortie, float alpha) {
        if (lamp == null) {
            return;
        }
        for (int i = 0; i < sortie.shotCount(); i++) {
            Shot shot = sortie.shot(i);
            // M8 AW: lit once it dropped in ahead of the hull.
            if (shot.weapon().delivery() != WeaponSpec.Delivery.DEPTH_CHARGE || tossed(shot)) {
                continue;
            }
            if (lampLit(shot.age() * SimStep.SECONDS, shot.weapon().mines().fuseSeconds())) {
                drawCentred(batch, lamp, shot.renderX(alpha), shot.renderY(alpha));
            }
        }
    }

    /**
     * Whether a depth charge's fuse lamp is lit {@code seconds} after its launch with a fuse of {@code fuse} s: its
     * blink's period falls from {@value #LAMP_PERIOD} s by {@value #LAMP_SPEED_UP} s over the fuse, lit in each
     * period's first half (the phase is the period's integral, so the blink speeds up smoothly).
     */
    static boolean lampLit(double seconds, double fuse) {
        double f = Math.max(SimStep.SECONDS, fuse);
        double p = Math.clamp(seconds / f, 0, 1);
        // ∫ dt / (P - S t / f) = -(f / S) ln(1 - S p / P)
        double phase = -(f / LAMP_SPEED_UP) * Math.log(1 - LAMP_SPEED_UP * p / LAMP_PERIOD);
        return phase - Math.floor(phase) < 0.5;
    }

    private void drawFlares(SpriteBatch batch, Sortie sortie, float alpha) {
        Flares flares = sortie.special().flares();
        if (flares == null || flare.size == 0) {
            return;
        }
        for (int i = 0; i < flares.size(); i++) {
            Flares.Flare f = flares.get(i);
            float strength = (float) Math.min(1, (1 - f.age()) / FLARE_FADE);
            double x = f.renderX(alpha);
            double y = f.renderY(alpha);
            batch.setColor(1, 1, 1, strength);
            if (flareGlow != null) {
                drawCentred(batch, flareGlow, x, y);
            }
            int frame = (int) ((sortie.tick() / FLARE_FRAME_TICKS + f.serial()) % flare.size);
            drawCentred(batch, flare.get(frame), x, y);
        }
        batch.setColor(Color.WHITE);
    }

    /** Over water, in the {@code sub} pass after the shots under it: the cables, the lamps, the bursts. */
    void drawSub(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (!overWater) {
            return;
        }
        drawCables(batch, sortie, alpha);
        subUnder.draw(batch, scroll);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawLamps(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        subOver.draw(batch, scroll);
    }

    /** The impacts over their cavities, the bursts, sparks and the loss with its debris over it. */
    void drawEffects(SpriteBatch batch, double scroll) {
        under.draw(batch, scroll);
        over.draw(batch, scroll);
        overSolid.draw(batch, scroll);
    }

    /**
     * The Bulwark (normal blending in, normal out): its lure halo under the hull while it draws fire, the hull (the hit
     * frame for a moment after a hit, scorched under half armour), its pump-jets and its muzzle light.
     */
    void drawBulwark(SpriteBatch batch, Sortie sortie, float alpha) {
        Bulwark drone = sortie.bulwark().orElse(null);
        if (drone == null || drone.destroyed() || bulwark.size == 0) {
            return;
        }
        double x = drone.renderX(alpha);
        double y = drone.renderY(alpha);
        long tick = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (lure.size > 0 && drone.drawing(sortie.ship().x(), sortie.ship().y())) {
            drawCentred(batch, lure.get((int) (tick / LURE_FRAME_TICKS % lure.size)), x, y);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawCentred(batch, bulwark.get(bulwarkFrame(drone.armour(), drone.maxArmour(), tick - bulwarkHitTick)), x, y);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (jets.size > 0) {
            drawCentred(batch, jets.get((int) (tick / JETS_FRAME_TICKS % jets.size)), x, y);
        }
        if (bulwarkMuzzle != null && drone.ticksSinceShot() < MUZZLE_TICKS) {
            drawCentred(batch, bulwarkMuzzle, x, y + MUZZLE_AHEAD);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The Bulwark's frame: hit for a moment after a hit, scorched under half its armour, else intact. */
    static int bulwarkFrame(double armour, double maxArmour, long sinceHit) {
        if (sinceHit >= 0 && sinceHit < BULWARK_HIT_TICKS) {
            return HIT;
        }
        return armour < maxArmour / 2 ? SCORCHED : INTACT;
    }

    /** The Bulwark's shadow on the ground (between {@link Shadows#begin} and {@link Shadows#end}). */
    void drawBulwarkShadow(SpriteBatch batch, Sortie sortie, float alpha) {
        Bulwark drone = sortie.bulwark().orElse(null);
        if (drone == null || drone.destroyed() || bulwark.size == 0) {
            return;
        }
        Shadows.draw(
                batch,
                bulwark.get(INTACT),
                Math.round(X0 + drone.renderX(alpha)),
                Math.round(drone.renderY(alpha)),
                Shadows.AIR_DX,
                Shadows.AIR_DY,
                1);
    }

    /**
     * The Sonar Pulse (additive blending in and out): its dashed double ring while it sweeps the field, and a bracket
     * round every unit it reached while the window is open.
     */
    void drawSonar(SpriteBatch batch, Sortie sortie, float alpha) {
        SpecialSlot special = sortie.special();
        if (special.sonar() == null) {
            return;
        }
        double radius = special.sonarRadius(alpha);
        if (radius > 0 && dash != null) {
            drawRing(batch, special.bombX(), special.bombY(), radius, OUTER_ALPHA);
            if (radius > INNER_RING) {
                drawRing(batch, special.bombX(), special.bombY(), radius - INNER_RING, INNER_ALPHA);
            }
        }
        if (contacts > 0 && corner.size > 0 && special.sonarActive()) {
            boolean hidden =
                    special.sonarSeconds() < 1 && (int) Math.floor(special.sonarSeconds() / BRACKET_BLINK) % 2 == 1;
            if (!hidden) {
                drawBrackets(batch, sortie, alpha);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawRing(SpriteBatch batch, double cx, double cy, double radius, float strength) {
        batch.setColor(1, 1, 1, strength);
        float width = dash.getRegionWidth();
        float height = dash.getRegionHeight();
        float stretch = (float) (radius * Math.toRadians(DASH_DEGREES) / width);
        for (int k = 0; k < SONAR_DASHES; k++) {
            double angle = Math.toRadians(k * 360.0 / SONAR_DASHES);
            double x = X0 + cx + Math.sin(angle) * radius;
            double y = cy + Math.cos(angle) * radius;
            batch.draw(
                    dash,
                    (float) (x - width / 2),
                    (float) (y - height / 2),
                    width / 2,
                    height / 2,
                    width,
                    height,
                    stretch,
                    1,
                    (float) -Math.toDegrees(angle));
        }
    }

    private void drawBrackets(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < contacts; i++) {
            Enemy unit = find(sortie, contactSerial[i]);
            if (unit == null) {
                continue;
            }
            long age = tick - contactTick[i];
            AtlasRegion box = looks[unit.kind()].frames().first();
            double off = BRACKET_GAP + bracketSnap(age);
            double hw = box.getRegionWidth() / 2.0 + off;
            double hh = box.getRegionHeight() / 2.0 + off;
            AtlasRegion c = corner.get(age < BRACKET_PING_TICKS || corner.size == 1 ? 0 : 1);
            float size = c.getRegionWidth();
            float cx = (float) (X0 + unit.renderX(alpha));
            float cy = (float) unit.renderY(alpha);
            float left = Math.round(cx - hw);
            float right = Math.round(cx + hw);
            float top = Math.round(cy + hh);
            float bottom = Math.round(cy - hh);
            // The top-left corner as drawn, the others mirrored.
            batch.draw(c, left, top - size, size, size);
            batch.draw(c, right, top - size, -size, size);
            batch.draw(c, left, bottom + size, size, -size);
            batch.draw(c, right, bottom + size, -size, -size);
        }
    }

    /** How far out a bracket still is {@code age} steps after its ping: from 8 px to 0 over 0.3 s. */
    static double bracketSnap(long age) {
        return BRACKET_SNAP * Math.max(0, 1 - (double) Math.max(0, age) / BRACKET_SNAP_TICKS);
    }

    private static Enemy find(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            if (unit.serial() == serial) {
                return unit;
            }
        }
        return null;
    }
}
