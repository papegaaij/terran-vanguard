package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Armament;
import vanguard.sim.Beam;
import vanguard.sim.Chain;
import vanguard.sim.Defences;
import vanguard.sim.Deflectors;
import vanguard.sim.Drones;
import vanguard.sim.EmpSpec;
import vanguard.sim.Enemy;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Ship;
import vanguard.sim.ShipSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;
import vanguard.sim.WeaponSpec;

/**
 * M7 part A: the Act 3 arsenal in flight (design/player/weapons, design/player/specials,
 * design/player/systems; the production art of tools/art/ion_beam.py, light_drone.py, deflector_pod.py
 * and act3_specials.py):
 *
 * <ul>
 *   <li>the Ion Beam's column from the nose up, tile on tile, per level width (the overdrive's own), cut
 *       at its contact where its tip bores in under the contact flare (large on a target of 50 px or
 *       more, small on a smaller one, the rock-scorch flare on a ground object), the faint glance spark
 *       where it passes over a hardened ground target, a muzzle glow at the nose, its 0.08 s fade on
 *       release, and the scorch marks it leaves where it burns on the ground (a ground unit or object),
 *       scrolling with the ground and fading over 0.8 s; the steady heat tint on its target is
 *       {@link LevelRenderer}'s ({@link #heatTarget});
 *   <li>the Light Drone Bay's drone on its orbit, facing its convergence point, flashing white as it
 *       absorbs a hit, fading in while its bay rebuilds it, its muzzle flash, its break and rebuild;
 *   <li>the Deflector Pod's field on its side while it holds a charge, brighter for a moment as one
 *       returns, and the catch flash where it catches a bullet;
 *   <li>the EMP Burst's ring (a band of quads round the circle like the Smart Bomb's, the 60×128 strip
 *       across it and along the arc) and the crackle on every stunned unit;
 *   <li>the Orbital Lance's red targeting line (dashed, with chevrons), its column (white-hot core, blue
 *       halo, bright rings travelling down: round 37's b, "descending rings") and the scorch line it burns
 *       into the ground with its embers;
 *   <li>the Shield Overcharge's white double shell round the ship while it lasts and its activation
 *       flare;
 *   <li>the Evasive thrusters' three fading afterimages along a dash, the thruster burst behind the
 *       ship, and the hull's flicker while the dash's invulnerability holds ({@link #evadingFlicker}).
 * </ul>
 */
public final class ArsenalLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The beam's column tiles: one frame a step, 8 frames (tools/art/ion_beam.py). */
    private static final int BEAM_FRAMES = 8;
    /** The beam's widths per sprite level 1..5 and the overdrive's (6). */
    private static final int[] BEAM_WIDTHS = {6, 8, 10, 12, 14, 18};
    /** A beam's fade after its release, steps (0.08 s). */
    private static final int BEAM_FADE_TICKS = SimStep.ticks(0.08);
    /** The contact flares show a frame two steps; the glance spark three. */
    private static final int CONTACT_FRAME_TICKS = 2;

    private static final int GLANCE_FRAME_TICKS = 3;
    /** A target this large or larger (px, its hit box's longer side) gets the large contact flare. */
    private static final double LARGE_TARGET = 50;
    /** The flares' hit point: (32, 22) of the 64x52 frame, from its top-left. */
    private static final float FLARE_HIT_Y = 22f / 52f;
    /** A scorch mark is laid every two steps where the beam burns on the ground, fading over 0.8 s. */
    private static final int SCORCH_EVERY = 2;

    private static final int SCORCH_TICKS = SimStep.ticks(0.8);
    private static final int MAX_SCORCH = SCORCH_TICKS / SCORCH_EVERY + 4;
    /** The beam's muzzle glow at the nose: the pulse cannon's muzzle frames, looped, smaller. */
    private static final float BEAM_MUZZLE_SCALE = 0.7f;

    private static final int MUZZLE_FRAME_TICKS = 2;
    /** A drone's muzzle flash: the pulse muzzle at half size, 6 px ahead of it. */
    private static final float DRONE_MUZZLE_SCALE = 0.5f;

    private static final double DRONE_MUZZLE_AHEAD = 6;
    /** The drone faces 16 headings; its break shows a frame every 2 steps, its rebuild every 3. */
    private static final int DRONE_HEADINGS = 16;

    private static final int BREAK_FRAME_TICKS = 2;
    private static final int REBUILD_FRAME_TICKS = 3;
    /** A drone's white flash as it absorbs a hit, steps. */
    private static final int DRONE_FLASH_TICKS = 2;
    /** The catch flash: 16 headings of 6 frames, 2 steps a frame (tools/art/deflector_pod.py). */
    private static final int CATCH_HEADINGS = 16;

    private static final int CATCH_FRAMES = 6;
    private static final int CATCH_FRAME_TICKS = 2;
    /** A field's alpha while it holds a charge, and how long it shines brighter as one returns. */
    static final float FIELD_ALPHA = 0.7f;

    static final int CHARGED_TICKS = 8;
    /** The field sprites' radii per level 1..5 and the overdrive's (6). */
    private static final int[] FIELD_RADII = {22, 24, 26, 28, 30, 32};
    /** The EMP ring's strip: 40 px inside its leading edge to 20 px beyond, 128 px of arc a repeat. */
    private static final double EMP_INSIDE = 40;

    private static final double EMP_REPEAT = 128;
    /** The quads per repeat of the strip along the arc. */
    private static final int EMP_QUADS_PER_REPEAT = 8;

    private static final int EMP_FRAME_TICKS = 2;
    /** The ring fades to 65 % as it spreads. */
    private static final float EMP_FADE_TO = 0.65f;
    /** A stunned unit's crackle shows a frame three steps; the small set under 32 px, the medium under 64. */
    private static final int CRACKLE_FRAME_TICKS = 3;

    private static final double SMALL_UNIT = 32;
    private static final double MEDIUM_UNIT = 64;
    /** A stunned Vrell unit's glow dims to this; a stunned machine's running lights go out. */
    static final float STUNNED_GLOW = 0.35f;
    /** The lance's targeting line and its column tile down the field; the line 2 steps a frame, the column 1. */
    private static final int LINE_FRAME_TICKS = 2;
    /** The lance lays a scorch column when it has moved 16 px across or the ground 32 px since the last. */
    private static final double LANCE_SCORCH_ACROSS = 16;

    private static final double LANCE_SCORCH_SCROLL = 32;
    private static final int MAX_LANCE_SCORCH = 96;
    /** A lance scorch column fades over 4 s (pending the user's review), its embers over the first 1.2 s. */
    private static final int LANCE_SCORCH_TICKS = SimStep.ticks(4);

    private static final int EMBER_TICKS = SimStep.ticks(1.2);
    private static final int EMBER_FRAME_TICKS = 4;
    /** The overcharge shell shows a frame three steps, its activation flare too. */
    private static final int SHELL_FRAME_TICKS = 3;
    /** The shell fades out over the excess's bleed. */
    private static final float SHELL_BLEED_ALPHA = 0.5f;
    /** A dash leaves three afterimages along its path, one at its start and two 3 and 6 steps in. */
    private static final int AFTERIMAGES = 3;

    private static final int AFTERIMAGE_EVERY = 3;
    private static final int AFTERIMAGE_TICKS = 15;
    private static final float AFTERIMAGE_ALPHA = 0.55f;
    private static final Color AFTERIMAGE = Color.valueOf("80D8FF");
    /** The hull's flicker while the dash's invulnerability holds: half-transparent every other 2 steps. */
    private static final int FLICKER_TICKS = 2;

    static final float FLICKER_ALPHA = 0.45f;
    /** The heat tint of a target under the beam: a steady blend towards a hot orange-white. */
    static final Color HEAT = Color.valueOf("FFB070");

    static final float HEAT_AMOUNT = 0.45f;
    /** Mounts the looks track (four slots). */
    private static final int MAX_MOUNTS = 4;

    private final Sprites sprites;
    private final FlashShader flash;
    private final Array<Array<AtlasRegion>> beamColumns = new Array<>();
    private final Array<Array<AtlasRegion>> beamTips = new Array<>();
    private final Array<AtlasRegion> contactLarge;
    private final Array<AtlasRegion> contactSmall;
    private final Array<AtlasRegion> contactRock;
    private final Array<AtlasRegion> glance;
    private final AtlasRegion scorch;
    private final AtlasRegion scorchGlow;
    private final Array<AtlasRegion> droneFrames;
    private final Array<AtlasRegion> droneBreak;
    private final Array<AtlasRegion> droneRebuild;
    private final Array<Array<AtlasRegion>> catches = new Array<>();
    private final AtlasRegion[] fieldsLeft = new AtlasRegion[FIELD_RADII.length];
    private final AtlasRegion[] fieldsRight = new AtlasRegion[FIELD_RADII.length];
    private final Array<AtlasRegion> empRing;
    private final Array<AtlasRegion> crackleSmall;
    private final Array<AtlasRegion> crackleMedium;
    private final Array<AtlasRegion> crackleLarge;
    private final Array<AtlasRegion> lanceLine;
    private final Array<AtlasRegion> lanceColumn;
    private final AtlasRegion lanceScorch;
    private final Array<AtlasRegion> lanceEmbers;
    private final Array<AtlasRegion> shell;
    private final Array<AtlasRegion> shellFlare;
    private final Array<AtlasRegion> burst;
    /** The effects that stay where they started: catch flashes, drone breaks, the thruster burst. */
    private final Effects effects = Effects.glowing();

    private final TextureRegion cut = new TextureRegion();
    private final float[] quad = new float[20];

    /** Per mount: the field's charge glow, the drone's flash and its rebuild's age (-1 none). */
    private final int[] charged = new int[MAX_MOUNTS];

    private final int[] droneFlash = new int[MAX_MOUNTS];
    private final int[] rebuildAge = new int[MAX_MOUNTS];
    /** Per mount: a released beam's fade, steps left, and where it was. */
    private final int[] beamFade = new int[MAX_MOUNTS];

    private final double[] fadeX = new double[MAX_MOUNTS];
    private final double[] fadeFrom = new double[MAX_MOUNTS];
    private final double[] fadeEnd = new double[MAX_MOUNTS];
    private final double[] fadeWidth = new double[MAX_MOUNTS];
    /** The beam's scorch marks on the ground: x, y plus the ground's scroll when laid, age in steps. */
    private final double[] scorchX = new double[MAX_SCORCH];

    private final double[] scorchY = new double[MAX_SCORCH];
    private final int[] scorchAge = new int[MAX_SCORCH];
    private int nextScorch;
    /** The lance's scorch columns: x, the ground's scroll when laid, age in steps. */
    private final double[] lanceScorchX = new double[MAX_LANCE_SCORCH];

    private final double[] lanceScorchScroll = new double[MAX_LANCE_SCORCH];
    private final int[] lanceScorchAge = new int[MAX_LANCE_SCORCH];
    private int nextLanceScorch;
    private double lastLanceX = Double.NaN;
    private double lastLanceScroll;
    /** The overcharge's activation flare's age, -1 for none. */
    private int flareAge = -1;
    /** A dash's afterimages: where, the hull's banking frame, age in steps (-1 unused); the dash's steps so far. */
    private final double[] imageX = new double[AFTERIMAGES];

    private final double[] imageY = new double[AFTERIMAGES];
    private final int[] imageBank = new int[AFTERIMAGES];
    private final int[] imageAge = new int[AFTERIMAGES];
    private int dashStep = -1;

    ArsenalLooks(Sprites sprites, FlashShader flash) {
        this.sprites = sprites;
        this.flash = flash;
        for (int level = 1; level <= BEAM_WIDTHS.length; level++) {
            beamColumns.add(optional("ion-beam-column-" + level));
            beamTips.add(optional("ion-beam-tip-" + level));
            fieldsLeft[level - 1] = optionalRegion("deflector-pod-field-" + level + "-left");
            fieldsRight[level - 1] = optionalRegion("deflector-pod-field-" + level + "-right");
        }
        contactLarge = optional("ion-beam-contact");
        contactSmall = optional("ion-beam-contact-small");
        contactRock = optional("ion-beam-rock");
        glance = optional("ion-beam-glance");
        scorch = optionalRegion("ion-beam-scorch");
        scorchGlow = optionalRegion("ion-beam-scorch-glow");
        droneFrames = optional("light-drone-bay-drone");
        droneBreak = optional("light-drone-bay-break");
        droneRebuild = optional("light-drone-bay-rebuild");
        Array<AtlasRegion> flashes = optional("deflector-pod-catch");
        for (int heading = 0; heading < CATCH_HEADINGS && flashes.size >= CATCH_HEADINGS * CATCH_FRAMES; heading++) {
            Array<AtlasRegion> frames = new Array<>(CATCH_FRAMES);
            for (int frame = 0; frame < CATCH_FRAMES; frame++) {
                frames.add(flashes.get(heading * CATCH_FRAMES + frame));
            }
            catches.add(frames);
        }
        empRing = optional("emp-burst-ring");
        crackleSmall = optional("emp-burst-crackle");
        crackleMedium = optional("emp-burst-crackle-medium");
        crackleLarge = optional("emp-burst-crackle-large");
        lanceLine = optional("orbital-lance-line");
        lanceColumn = optional("orbital-lance-column");
        lanceScorch = optionalRegion("orbital-lance-scorch");
        lanceEmbers = optional("orbital-lance-embers");
        shell = optional("shield-overcharge-shell");
        shellFlare = optional("shield-overcharge-flare");
        burst = sprites.has("launcher-muzzle") ? sprites.frames("launcher-muzzle") : sprites.pulseMuzzle;
        restart();
    }

    private Array<AtlasRegion> optional(String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private AtlasRegion optionalRegion(String name) {
        return sprites.has(name) ? sprites.frames(name).first() : null;
    }

    /** The attempt started over: nothing of the last one shows. */
    void restart() {
        effects.clear();
        Arrays.fill(charged, 0);
        Arrays.fill(droneFlash, 0);
        Arrays.fill(rebuildAge, -1);
        Arrays.fill(beamFade, 0);
        Arrays.fill(scorchAge, SCORCH_TICKS);
        Arrays.fill(lanceScorchAge, LANCE_SCORCH_TICKS);
        Arrays.fill(imageAge, -1);
        lastLanceX = Double.NaN;
        flareAge = -1;
        dashStep = -1;
    }

    /** After each simulation step: the counters advance and the step's events start their looks. */
    void stepped(Sortie sortie) {
        effects.step();
        for (int m = 0; m < MAX_MOUNTS; m++) {
            charged[m] = Math.max(0, charged[m] - 1);
            droneFlash[m] = Math.max(0, droneFlash[m] - 1);
            beamFade[m] = Math.max(0, beamFade[m] - 1);
            if (rebuildAge[m] >= 0 && ++rebuildAge[m] >= droneRebuild.size * REBUILD_FRAME_TICKS) {
                rebuildAge[m] = -1;
            }
        }
        for (int i = 0; i < MAX_SCORCH; i++) {
            scorchAge[i] = Math.min(SCORCH_TICKS, scorchAge[i] + 1);
        }
        for (int i = 0; i < MAX_LANCE_SCORCH; i++) {
            lanceScorchAge[i] = Math.min(LANCE_SCORCH_TICKS, lanceScorchAge[i] + 1);
        }
        if (flareAge >= 0 && ++flareAge >= shellFlare.size * SHELL_FRAME_TICKS) {
            flareAge = -1;
        }
        for (int i = 0; i < AFTERIMAGES; i++) {
            if (imageAge[i] >= 0 && ++imageAge[i] >= AFTERIMAGE_TICKS) {
                imageAge[i] = -1;
            }
        }
        Ship ship = sortie.ship();
        if (dashStep >= 0) {
            dashStep++;
            int image = dashStep / AFTERIMAGE_EVERY;
            if (dashStep % AFTERIMAGE_EVERY == 0 && image < AFTERIMAGES) {
                afterimage(image, ship.x(), ship.y(), ship.bank());
            }
            if (!ship.dashing() && dashStep >= AFTERIMAGES * AFTERIMAGE_EVERY) {
                dashStep = -1;
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            int value = events.value(i);
            switch (events.type(i)) {
                case BEAM_OFF -> released(sortie, value);
                case DEFLECTOR_CAUGHT -> {
                    if (catches.size > 0) {
                        // The flash's heading: from the ship's centre to the catch, clockwise from up.
                        double heading = Math.atan2(x - ship.x(), y - ship.y());
                        effects.start(catches.get(headingIndex(heading, CATCH_HEADINGS)), CATCH_FRAME_TICKS, x, y);
                    }
                }
                case DEFLECTOR_CHARGED -> {
                    if (value >= 0 && value < MAX_MOUNTS) {
                        charged[value] = CHARGED_TICKS;
                    }
                }
                case DRONE_HIT -> {
                    if (value >= 0 && value < MAX_MOUNTS) {
                        droneFlash[value] = DRONE_FLASH_TICKS;
                    }
                }
                case DRONE_BROKEN -> {
                    if (droneBreak.size > 0) {
                        effects.start(droneBreak, BREAK_FRAME_TICKS, x, y);
                    }
                }
                case DRONE_REBUILT -> {
                    if (value >= 0 && value < MAX_MOUNTS) {
                        rebuildAge[value] = 0;
                    }
                }
                case OVERCHARGE -> flareAge = 0;
                case DASH -> {
                    dashStep = 0;
                    Arrays.fill(imageAge, -1);
                    afterimage(0, x, y, ship.bank());
                    // The thruster burst: behind the ship, against the dash's direction.
                    effects.start(burst, MUZZLE_FRAME_TICKS, x - ship.dashDirX() * 14, y - ship.dashDirY() * 14 - 6);
                }
                default -> {}
            }
        }
        layScorch(sortie);
        layLanceScorch(sortie);
    }

    private void afterimage(int image, double x, double y, int bank) {
        imageX[image] = x;
        imageY[image] = y;
        imageBank[image] = bank;
        imageAge[image] = 0;
    }

    /** A beam went off: it fades where it last was. */
    private void released(Sortie sortie, int m) {
        Beam beam = m >= 0 && m < MAX_MOUNTS ? sortie.beam(m) : null;
        if (beam == null) {
            return;
        }
        beamFade[m] = BEAM_FADE_TICKS;
        fadeX[m] = beam.x();
        fadeFrom[m] = beam.fromY();
        fadeEnd[m] = beam.endY();
        fadeWidth[m] = beam.width();
    }

    /** Every other step a beam burning on the ground (a ground unit or object) leaves a scorch mark there. */
    private void layScorch(Sortie sortie) {
        if (scorch == null || sortie.tick() % SCORCH_EVERY != 0) {
            return;
        }
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam beam = sortie.beam(m);
            if (beam == null || !beam.on() || beam.contact() != Beam.Contact.BURN || !onGround(sortie, beam)) {
                continue;
            }
            scorchX[nextScorch] = beam.x();
            scorchY[nextScorch] = beam.endY() - Beam.BORE + sortie.groundScroll();
            scorchAge[nextScorch] = 0;
            nextScorch = (nextScorch + 1) % MAX_SCORCH;
        }
    }

    /** The Orbital Lance burns a scorch column down the field as it moves across or the ground under it. */
    private void layLanceScorch(Sortie sortie) {
        SpecialSlot special = sortie.special();
        if (lanceScorch == null || !special.lanceBurning()) {
            lastLanceX = Double.NaN;
            return;
        }
        double x = special.lanceX(1);
        double scroll = sortie.groundScroll();
        if (!Double.isNaN(lastLanceX)
                && Math.abs(x - lastLanceX) < LANCE_SCORCH_ACROSS
                && Math.abs(scroll - lastLanceScroll) < LANCE_SCORCH_SCROLL) {
            return;
        }
        lastLanceX = x;
        lastLanceScroll = scroll;
        lanceScorchX[nextLanceScorch] = x;
        lanceScorchScroll[nextLanceScorch] = scroll;
        lanceScorchAge[nextLanceScorch] = 0;
        nextLanceScorch = (nextLanceScorch + 1) % MAX_LANCE_SCORCH;
    }

    /** Whether the beam burns on the ground: a ground object (no unit), or a unit on the ground layer. */
    private static boolean onGround(Sortie sortie, Beam beam) {
        if (beam.target() < 0) {
            return true;
        }
        Enemy enemy = target(sortie, beam.target());
        return enemy != null && enemy.layer() == Layer.GROUND;
    }

    /** The enemy whose serial is {@code serial}; null for none (a set piece's part, or gone). */
    private static Enemy target(Sortie sortie, int serial) {
        if (serial < 0 || serial >= Beam.PART_TARGET) {
            return null;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).serial() == serial) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /**
     * The target a beam burns now, for the steady heat tint: an enemy's serial, a set piece's part
     * ({@link Beam#PART_TARGET} + piece × parts + part), {@link #GROUND_TARGET} for a ground object, or
     * -1 for none (with two beams, the first's).
     */
    static int heatTarget(Sortie sortie) {
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam beam = sortie.beam(m);
            if (beam != null && beam.on() && beam.contact() == Beam.Contact.BURN) {
                return beam.target() < 0 ? GROUND_TARGET : beam.target();
            }
        }
        return -1;
    }

    /** {@link #heatTarget} for a ground object (the beam's own target is -1 for one). */
    static final int GROUND_TARGET = Integer.MIN_VALUE;

    /** The set piece's index of a {@link #heatTarget} on a part, else -1. */
    static int heatPiece(int target) {
        return target >= Beam.PART_TARGET ? (target - Beam.PART_TARGET) / LevelScript.SetPieceSpec.MAX_PARTS : -1;
    }

    /** The part's index of a {@link #heatTarget} on a part, else -1. */
    static int heatPart(int target) {
        return target >= Beam.PART_TARGET ? (target - Beam.PART_TARGET) % LevelScript.SetPieceSpec.MAX_PARTS : -1;
    }

    /** The x a beam burning a ground object burns at, for the object's heat tint; NaN for none. */
    static double groundHeatX(Sortie sortie) {
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam beam = sortie.beam(m);
            if (beam != null && beam.on() && beam.contact() == Beam.Contact.BURN && beam.target() < 0) {
                return beam.x();
            }
        }
        return Double.NaN;
    }

    /** Whether the hull shows its see-through flicker now: while the dash's invulnerability holds. */
    static boolean evadingFlicker(Ship ship, long tick) {
        return ship.evading() && tick / FLICKER_TICKS % 2 == 0;
    }

    /** On the ground, under the ground units: the beam's scorch marks and the lance's scorch line. */
    void drawGround(SpriteBatch batch, double scroll) {
        if (lanceScorch != null) {
            int height = lanceScorch.getRegionHeight();
            for (int i = 0; i < MAX_LANCE_SCORCH; i++) {
                int age = lanceScorchAge[i];
                if (age >= LANCE_SCORCH_TICKS) {
                    continue;
                }
                batch.setColor(1, 1, 1, 1 - (float) age / LANCE_SCORCH_TICKS);
                double shift = lanceScorchScroll[i] - scroll;
                float x = Math.round(X0 + lanceScorchX[i] - lanceScorch.getRegionWidth() / 2f);
                // Tiles fixed on the ground: the column runs down the field, scrolling with it.
                double first = ((shift % height) + height) % height - height;
                for (double y = first; y < PlayField.HEIGHT; y += height) {
                    batch.draw(lanceScorch, x, Math.round(y));
                }
            }
            batch.setColor(Color.WHITE);
        }
        if (scorch != null) {
            for (int i = 0; i < MAX_SCORCH; i++) {
                int age = scorchAge[i];
                if (age >= SCORCH_TICKS) {
                    continue;
                }
                batch.setColor(1, 1, 1, 1 - (float) age / SCORCH_TICKS);
                drawCentred(batch, scorch, scorchX[i], scorchY[i] - scroll);
            }
            batch.setColor(Color.WHITE);
        }
    }

    /** Additive, over the ground marks: the fresh scorch's glow and the lance scorch's embers. */
    void drawGroundGlow(SpriteBatch batch, double scroll, long tick) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (scorchGlow != null) {
            for (int i = 0; i < MAX_SCORCH; i++) {
                int age = scorchAge[i];
                if (age < SCORCH_TICKS) {
                    batch.setColor(1, 1, 1, 1 - (float) age / SCORCH_TICKS);
                    drawCentred(batch, scorchGlow, scorchX[i], scorchY[i] - scroll);
                }
            }
        }
        if (lanceEmbers.size > 0) {
            AtlasRegion ember = lanceEmbers.get((int) (tick / EMBER_FRAME_TICKS % lanceEmbers.size));
            int height = ember.getRegionHeight();
            for (int i = 0; i < MAX_LANCE_SCORCH; i++) {
                int age = lanceScorchAge[i];
                if (age >= EMBER_TICKS) {
                    continue;
                }
                batch.setColor(1, 1, 1, 1 - (float) age / EMBER_TICKS);
                double shift = lanceScorchScroll[i] - scroll;
                float x = Math.round(X0 + lanceScorchX[i] - ember.getRegionWidth() / 2f);
                double first = ((shift % height) + height) % height - height;
                for (double y = first; y < PlayField.HEIGHT; y += height) {
                    batch.draw(ember, x, Math.round(y));
                }
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Under the hull, solid: the drones on their orbits; additive: a dash's afterimages. Called with
     * the batch in normal blending, left so.
     */
    void drawBehindShip(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        boolean images = false;
        for (int i = 0; i < AFTERIMAGES; i++) {
            images |= imageAge[i] >= 0;
        }
        if (images) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int i = AFTERIMAGES - 1; i >= 0; i--) {
                if (imageAge[i] < 0) {
                    continue;
                }
                float fade = AFTERIMAGE_ALPHA * (1 - (float) imageAge[i] / AFTERIMAGE_TICKS);
                batch.setColor(AFTERIMAGE.r, AFTERIMAGE.g, AFTERIMAGE.b, fade);
                drawCentred(batch, sprites.ship.get(imageBank[i] + ShipSpec.HARD_BANK), imageX[i], imageY[i]);
            }
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        drawDrones(batch, sortie, alpha, whiteFlash);
    }

    /** The drone of each drone bay on its orbit, facing its convergence point; nothing while broken. */
    private void drawDrones(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        Drones drones = sortie.drones();
        if (!drones.any() || droneFrames.size == 0) {
            return;
        }
        Ship ship = sortie.ship();
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (!drones.bay(m) || drones.broken(m)) {
                continue;
            }
            double dx = drones.offsetX(m);
            double dy = drones.offsetY(m);
            AtlasRegion frame = droneFrames.get(headingIndex(droneHeading(drones, m), DRONE_HEADINGS));
            float x = Math.round(X0 + ship.renderX(alpha) + dx);
            float y = Math.round(ship.renderY(alpha) + dy);
            float fade = (float) drones.fadeIn(m);
            if (m < MAX_MOUNTS && droneFlash[m] > 0) {
                flash.draw(batch, frame, x, y, Color.WHITE, whiteFlash);
            } else {
                batch.setColor(1, 1, 1, fade);
                batch.draw(frame, x - frame.getRegionWidth() / 2f, y - frame.getRegionHeight() / 2f);
                batch.setColor(Color.WHITE);
            }
        }
    }

    /** A drone's heading, radians clockwise from up: towards its convergence point ahead of the ship. */
    private static double droneHeading(Drones drones, int m) {
        return Math.atan2(-drones.offsetX(m), drones.spec(m).converge() - drones.offsetY(m));
    }

    /**
     * Over the ship, additive (the batch is in additive blending and stays so): the Deflector Pods'
     * fields, the drones' rebuild rings and muzzle flashes, the beams with their muzzle glow, contact
     * and glance, the overcharge's shell and flare, and the step's catch flashes, breaks and bursts.
     */
    void drawOverShip(SpriteBatch batch, Sortie sortie, float alpha) {
        Ship ship = sortie.ship();
        double shipX = ship.renderX(alpha);
        double shipY = ship.renderY(alpha);
        drawFields(batch, sortie, shipX, shipY);
        drawDroneLights(batch, sortie, shipX, shipY);
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam beam = sortie.beam(m);
            if (beam == null) {
                continue;
            }
            if (beam.on()) {
                drawBeam(batch, sortie, beam, 1);
            } else if (m < MAX_MOUNTS && beamFade[m] > 0) {
                drawColumn(
                        batch,
                        sortie.tick(),
                        fadeX[m],
                        fadeFrom[m],
                        fadeEnd[m],
                        fadeWidth[m],
                        false,
                        (float) beamFade[m] / (BEAM_FADE_TICKS + 1));
            }
        }
        drawShell(batch, ship.defences(), shipX, shipY, sortie.tick());
        effects.draw(batch, 0);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(Color.WHITE);
    }

    private void drawFields(SpriteBatch batch, Sortie sortie, double shipX, double shipY) {
        Deflectors fields = sortie.deflectors();
        if (!fields.any()) {
            return;
        }
        float x = (float) (X0 + shipX);
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (!fields.pod(m) || fields.charges(m) == 0) {
                continue;
            }
            int level = level(FIELD_RADII, fields.spec(m).radius());
            boolean left = fields.side(m) < 0;
            AtlasRegion field = left ? fieldsLeft[level] : fieldsRight[level];
            if (field == null) {
                continue;
            }
            float glow = m < MAX_MOUNTS ? (float) charged[m] / CHARGED_TICKS : 0;
            batch.setColor(1, 1, 1, Math.min(1, FIELD_ALPHA + (1 - FIELD_ALPHA) * glow));
            // The ship's centre is the middle of the sprite's inner edge.
            batch.draw(
                    field,
                    Math.round(left ? x - field.getRegionWidth() : x),
                    Math.round((float) shipY - field.getRegionHeight() / 2f));
        }
        batch.setColor(Color.WHITE);
    }

    /** The drones' muzzle flashes after a shot and the rebuild ring closing on each bay. */
    private void drawDroneLights(SpriteBatch batch, Sortie sortie, double shipX, double shipY) {
        Drones drones = sortie.drones();
        if (!drones.any()) {
            return;
        }
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (!drones.bay(m)) {
                continue;
            }
            Armament.Mount mount = sortie.armament().mount(m);
            if (m < MAX_MOUNTS && rebuildAge[m] >= 0 && droneRebuild.size > 0) {
                WeaponSpec.Muzzle bay = mount.weapon().muzzles().getFirst();
                AtlasRegion ring =
                        droneRebuild.get(Math.min(droneRebuild.size - 1, rebuildAge[m] / REBUILD_FRAME_TICKS));
                drawCentred(batch, ring, shipX + bay.dx(), shipY + bay.dy());
            }
            int frame = sortie.ticksSinceShot(m) / MUZZLE_FRAME_TICKS;
            if (drones.whole(m) && frame < sprites.pulseMuzzle.size) {
                double heading = droneHeading(drones, m);
                drawScaled(
                        batch,
                        sprites.pulseMuzzle.get(frame),
                        shipX + drones.offsetX(m) + Math.sin(heading) * DRONE_MUZZLE_AHEAD,
                        shipY + drones.offsetY(m) + Math.cos(heading) * DRONE_MUZZLE_AHEAD,
                        DRONE_MUZZLE_SCALE);
            }
        }
    }

    /** A running beam: its column, the muzzle glow at the nose, its contact flare and glance spark. */
    private void drawBeam(SpriteBatch batch, Sortie sortie, Beam beam, float strength) {
        long tick = sortie.tick();
        boolean burning = beam.contact() == Beam.Contact.BURN;
        double blocked = blockedEdge(beam);
        double end = Double.isNaN(blocked) ? beam.endY() : blocked;
        drawColumn(batch, tick, beam.x(), beam.fromY(), end, beam.width(), burning, strength);
        drawScaled(
                batch,
                sprites.pulseMuzzle.get((int) (tick / MUZZLE_FRAME_TICKS % sprites.pulseMuzzle.size)),
                beam.x(),
                beam.fromY(),
                BEAM_MUZZLE_SCALE);
        if (burning) {
            Array<AtlasRegion> flares = contactFlare(sortie, beam);
            if (flares.size > 0) {
                AtlasRegion flare = flares.get((int) (tick / CONTACT_FRAME_TICKS % flares.size));
                float hitY = flare.getRegionHeight() * FLARE_HIT_Y;
                batch.draw(
                        flare,
                        Math.round(X0 + beam.x() - flare.getRegionWidth() / 2f),
                        Math.round(beam.endY() - (flare.getRegionHeight() - hitY)));
            }
        }
        if (!Double.isNaN(beam.glanceY()) && glance.size > 0) {
            drawCentred(batch, glance.get((int) (tick / GLANCE_FRAME_TICKS % glance.size)), beam.x(), beam.glanceY());
        }
        if (!Double.isNaN(blocked) && glance.size > 0) {
            // M7 part A (round 37, R.4 b): stopped by what it cannot damage (an armoured hull, a shielded
            // part), the column ends at its edge in the glance spark, not bored in and without a burn's flare.
            drawCentred(batch, glance.get((int) (tick / GLANCE_FRAME_TICKS % glance.size)), beam.x(), blocked);
        }
    }

    /**
     * M7 part A: where a beam that something it cannot damage stops ({@link Beam.Contact#BLOCKED}) shows
     * its glance spark: that thing's edge, the contact without the {@link Beam#BORE} a burn bores in; NaN
     * for any other contact.
     */
    static double blockedEdge(Beam beam) {
        return beam.contact() == Beam.Contact.BLOCKED ? beam.endY() - Beam.BORE : Double.NaN;
    }

    /** The contact flare for what the beam burns: rock on a ground object, large or small by the target's size. */
    private Array<AtlasRegion> contactFlare(Sortie sortie, Beam beam) {
        if (beam.target() < 0) {
            return contactRock.size > 0 ? contactRock : contactLarge;
        }
        if (beam.target() >= Beam.PART_TARGET) {
            return contactLarge;
        }
        Enemy enemy = target(sortie, beam.target());
        boolean large =
                enemy != null && Math.max(enemy.hitbox().width(), enemy.hitbox().height()) >= LARGE_TARGET;
        return large || contactSmall.size == 0 ? contactLarge : contactSmall;
    }

    /** The column from {@code from} up to {@code end}, tile on tile, its tip bored in where it burns. */
    private void drawColumn(
            SpriteBatch batch,
            long tick,
            double centre,
            double from,
            double end,
            double width,
            boolean burning,
            float strength) {
        int level = level(BEAM_WIDTHS, width);
        Array<AtlasRegion> columns = beamColumns.get(level);
        Array<AtlasRegion> tips = beamTips.get(level);
        if (columns.size == 0) {
            return;
        }
        int frame = (int) (tick % BEAM_FRAMES);
        AtlasRegion tile = columns.get(frame % columns.size);
        AtlasRegion tip = tips.size > 0 ? tips.get(frame % tips.size) : null;
        boolean tipped = burning && tip != null;
        float top =
                (float) Math.min(end, PlayField.HEIGHT + tile.getRegionHeight()) - (tipped ? tip.getRegionHeight() : 0);
        float x = (float) (X0 + centre - tile.getRegionWidth() / 2f);
        int height = tile.getRegionHeight();
        batch.setColor(1, 1, 1, strength);
        for (float y = (float) from; y < top; y += height) {
            int shown = (int) Math.min(height, Math.ceil(top - y));
            // The tile's lower rows, so the column runs on seamlessly to its cut end.
            cut.setRegion(tile, 0, height - shown, tile.getRegionWidth(), shown);
            batch.draw(cut, Math.round(x), Math.round(y));
        }
        if (tipped) {
            batch.draw(tip, Math.round(X0 + centre - tip.getRegionWidth() / 2f), Math.round(top));
        }
        batch.setColor(Color.WHITE);
    }

    /** The overcharge's white double shell while it lasts (fading over the bleed) and its activation flare. */
    private void drawShell(SpriteBatch batch, Defences defences, double x, double y, long tick) {
        if (shell.size > 0 && (defences.overcharged() || defences.bleeding())) {
            batch.setColor(1, 1, 1, defences.overcharged() ? 1 : SHELL_BLEED_ALPHA);
            drawCentred(batch, shell.get((int) (tick / SHELL_FRAME_TICKS % shell.size)), x, y);
            batch.setColor(Color.WHITE);
        }
        if (flareAge >= 0 && shellFlare.size > 0) {
            drawCentred(batch, shellFlare.get(Math.min(shellFlare.size - 1, flareAge / SHELL_FRAME_TICKS)), x, y);
        }
    }

    /**
     * Over every unit, additive (the batch is in additive blending and stays so): the crackle on each
     * stunned unit (a chain's members while it is stunned), by its size.
     */
    void drawStunned(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sortie.special().emp() == null || crackleSmall.size == 0) {
            return;
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Chain chain = enemy.chain();
            if (!(chain != null ? chain.empStunned() : enemy.empStunned())) {
                continue;
            }
            double size = Math.max(enemy.hitbox().width(), enemy.hitbox().height());
            Array<AtlasRegion> crackle = size < SMALL_UNIT || crackleMedium.size == 0
                    ? crackleSmall
                    : size < MEDIUM_UNIT || crackleLarge.size == 0 ? crackleMedium : crackleLarge;
            int frame = (int) ((tick / CRACKLE_FRAME_TICKS + enemy.serial()) % crackle.size);
            drawCentred(batch, crackle.get(frame), enemy.renderX(alpha), enemy.renderY(alpha));
        }
    }

    /** A stunned unit's glow: dimmed for a Vrell unit, out for a machine; 1 for one not stunned. */
    static float glowShare(Enemy enemy) {
        Chain chain = enemy.chain();
        boolean stunned = chain != null ? chain.empStunned() : enemy.empStunned();
        if (!stunned) {
            return 1;
        }
        return enemy.spec().machine() ? 0 : STUNNED_GLOW;
    }

    /**
     * The specials over the air layer, additive (the batch is in additive blending and stays so): the
     * EMP Burst's ring, the Orbital Lance's targeting line and its column.
     */
    void drawSpecials(SpriteBatch batch, Sortie sortie, float alpha) {
        SpecialSlot special = sortie.special();
        if (special.emp() != null) {
            drawEmpRing(batch, special, alpha);
        }
        if (special.lance() != null) {
            drawLance(batch, sortie, special, alpha);
        }
    }

    private void drawLance(SpriteBatch batch, Sortie sortie, SpecialSlot special, float alpha) {
        long tick = sortie.tick();
        double x = special.lanceLocking() ? sortie.ship().renderX(alpha) : special.lanceX(alpha);
        if (special.lanceLocking() && lanceLine.size > 0) {
            tileDown(batch, lanceLine.get((int) (tick / LINE_FRAME_TICKS % lanceLine.size)), x, 1);
        }
        float strength = (float) special.lanceStrength(alpha);
        if (strength > 0 && lanceColumn.size > 0) {
            tileDown(batch, lanceColumn.get((int) (tick % lanceColumn.size)), x, strength);
        }
    }

    /** A tile repeated over the play field's height, centred on {@code x}. */
    private static void tileDown(SpriteBatch batch, AtlasRegion tile, double x, float strength) {
        batch.setColor(1, 1, 1, strength);
        float left = Math.round(X0 + x - tile.getRegionWidth() / 2f);
        for (int y = 0; y < PlayField.HEIGHT; y += tile.getRegionHeight()) {
            batch.draw(tile, left, y);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * The EMP Burst's ring as a band of quads round the circle: the strip's x across the ring (40 px
     * inside its leading edge to 20 px beyond), its y along the arc, a whole number of 128 px repeats
     * round the circle; it fades to 65 % as it spreads.
     */
    private void drawEmpRing(SpriteBatch batch, SpecialSlot special, float alpha) {
        double radius = special.empRadius(alpha);
        if (radius <= 0 || empRing.size == 0) {
            return;
        }
        EmpSpec emp = special.emp();
        double seconds = Math.max(0, special.empSeconds(alpha));
        AtlasRegion strip = empRing.get((int) (seconds * SimStep.PER_SECOND / EMP_FRAME_TICKS % empRing.size));
        double share = Math.min(1, seconds / Math.max(SimStep.SECONDS, emp.ringSeconds()));
        float light = (float) (1 - (1 - EMP_FADE_TO) * share);
        float colour = Color.toFloatBits(light, light, light, 1f);
        double inner = Math.max(0, radius - EMP_INSIDE);
        double outer = radius - EMP_INSIDE + strip.getRegionWidth();
        float u0 = strip.getU()
                + (strip.getU2() - strip.getU()) * (float) ((inner - (radius - EMP_INSIDE)) / strip.getRegionWidth());
        float u1 = strip.getU2();
        int repeats = Math.max(1, (int) Math.round(2 * Math.PI * radius / EMP_REPEAT));
        int quads = repeats * EMP_QUADS_PER_REPEAT;
        double cx = X0 + special.bombX();
        double cy = special.bombY();
        for (int k = 0; k < quads; k++) {
            double a0 = 2 * Math.PI * k / quads;
            double a1 = 2 * Math.PI * (k + 1) / quads;
            int along = k % EMP_QUADS_PER_REPEAT;
            float v0 = strip.getV() + (strip.getV2() - strip.getV()) * along / EMP_QUADS_PER_REPEAT;
            float v1 = strip.getV() + (strip.getV2() - strip.getV()) * (along + 1) / EMP_QUADS_PER_REPEAT;
            corner(0, cx + Math.sin(a0) * inner, cy + Math.cos(a0) * inner, colour, u0, v0);
            corner(1, cx + Math.sin(a0) * outer, cy + Math.cos(a0) * outer, colour, u1, v0);
            corner(2, cx + Math.sin(a1) * outer, cy + Math.cos(a1) * outer, colour, u1, v1);
            corner(3, cx + Math.sin(a1) * inner, cy + Math.cos(a1) * inner, colour, u0, v1);
            batch.draw(strip.getTexture(), quad, 0, quad.length);
        }
    }

    private void corner(int i, double x, double y, float colour, float u, float v) {
        quad[i * 5] = (float) x;
        quad[i * 5 + 1] = (float) y;
        quad[i * 5 + 2] = colour;
        quad[i * 5 + 3] = u;
        quad[i * 5 + 4] = v;
    }

    /** The sprite level (0-based) whose size is nearest {@code size}. */
    static int level(int[] sizes, double size) {
        int best = 0;
        for (int i = 1; i < sizes.length; i++) {
            if (Math.abs(sizes[i] - size) < Math.abs(sizes[best] - size)) {
                best = i;
            }
        }
        return best;
    }

    /** The nearest of {@code headings} headings to {@code radians} (clockwise from up). */
    static int headingIndex(double radians, int headings) {
        return Math.floorMod((int) Math.round(radians / (2 * Math.PI) * headings), headings);
    }

    private static void drawScaled(SpriteBatch batch, TextureRegion region, double x, double y, float scale) {
        float width = region.getRegionWidth();
        float height = region.getRegionHeight();
        batch.draw(
                region,
                Math.round(X0 + x - width / 2),
                Math.round(y - height / 2),
                width / 2,
                height / 2,
                width,
                height,
                scale,
                scale,
                0);
    }

    private static void drawCentred(SpriteBatch batch, TextureRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(X0 + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
