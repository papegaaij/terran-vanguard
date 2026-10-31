package vanguard.game.render;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import vanguard.content.AlliesData;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Draws a level's play field, interpolating every position between the last two simulation steps, and tells its
 * looks what happens in the level (hits, volleys, flak, a boss's launches) for the screens ({@code LevelScreen},
 * {@code EventEffects}, {@code BossCues}).
 *
 * <p>RF6: the looks live in {@link LevelLooks} (built, freed, restarted and stepped there), the pass order in {@link
 * DrawPasses}: the frame back to front in named passes, pinned by {@code PassOrderTest}. The parts' own passes live in
 * their looks: the ground objects' in {@link GroundPasses}, the enemy units' in {@link UnitPasses} (with the looks
 * that follow the sortie's enemy kinds in {@link KindLooks}), the set pieces' in {@link SetPiecePasses}, the ship's,
 * its shots' and the Airstrike's in {@link PlayerPasses}, the props' in {@link PropPasses}; they share the drawn
 * frame's state through {@link PlayFrame}.
 */
public final class LevelRenderer {

    /**
     * A set piece on high-air is drawn this much larger than on the play plane (the first pass's
     * sprites are drawn at it); descending and rising it scales between the two.
     */
    static final float HIGH_AIR_SCALE = 1.25f;
    /**
     * A set piece off the play plane is drawn at 75 % opacity (body, parts and glow alike); descending
     * and rising it eases between that and opaque by its altitude, so it is opaque when it reaches the
     * plane, where it switches below the ship and can collide.
     */
    private static final float HIGH_AIR_OPACITY = 0.75f;

    /** M5 part E: the strength a torpedo's lit back is added over the chop with ({@link PlayerPasses#drawTorpedoBacks}). */
    static final float TORPEDO_BACK = 0.9f;

    /**
     * M7 part D: a trigger look over contents with sprites of their own, whose {@code -glow} is the trigger's glow
     * where the look has none (Level 17's seed-vault capsule blinking under the collapsed greenhouse frame,
     * tools/art/siege_units.py; vanguard.pipeline.SpriteUse claims the contents with the look).
     */
    static final java.util.Map<String, String> TRIGGER_CONTENTS = java.util.Map.of("greenhouse-frame", "seed-capsule");

    /**
     * Round 44 G.c (step GK): a trigger look standing on a ground decal of its own, drawn under every state of it at the
     * same centre (Level 17's greenhouse frame on its scorched pad, tools/art/siege_units.py; vanguard.pipeline.SpriteUse
     * claims the pad with the look).
     */
    static final java.util.Map<String, String> TRIGGER_PADS = java.util.Map.of("greenhouse-frame", "greenhouse-pad");

    /** The level's looks and the passes that draw them. */
    private final DrawPasses passes;

    /** M8 part F: whether the level is a dark level (Level 26: {@link DarkLight}'s lights and lamps). */
    public static boolean darkLevel(LevelData level) {
        return UnderwaterGrade.hasLook(level) && DarkLight.blackWater(level);
    }

    /**
     * M9 part D4b: the look a level draws its tows in ({@link TowLooks#LOOKS}: Level 30's dead buoy, Level 31's claim
     * rock), empty for the lifeboat; the tow's break sound follows it.
     */
    public static java.util.Optional<String> towLook(int level) {
        return java.util.Optional.ofNullable(TowLooks.LOOKS.get(level));
    }

    /**
     * @param files the assets, for the pivot files of the cranes and set pieces
     * @param script the level's script, whose set pieces it draws
     * @param levelKey the level's key, {@code <act>/level-NN-<slug>}
     */
    public LevelRenderer(
            Sprites sprites,
            EnemyLooks[] looks,
            WeaponLooks weapons,
            Files files,
            FlashShader flash,
            BitmapFont font,
            LevelData level,
            LevelScript script,
            String levelKey) {
        passes = new DrawPasses(sprites, looks, weapons, files, flash, font, level, script, levelKey);
    }

    /**
     * M8 part C: whether a point at play-field height {@code y} lies in the water at the last frame (its kill the
     * under-water burst): under the grade (above the waterline's row while it is on the screen); false without one.
     */
    public boolean underwaterAt(double y) {
        return passes.grade != null && passes.grade.underwaterAt(y);
    }

    /**
     * M8 part F: the settings' Dark levels assist (design/campaign Level 26, Darkness): in a dark level the ambient is
     * held at least at {@value DarkLight#ASSIST_AMBIENT}; no change elsewhere.
     */
    public void darkAssist(boolean on) {
        if (passes.dark != null) {
            passes.grade.ambientFloor(on ? DarkLight.ASSIST_AMBIENT : 0);
        }
    }

    /** M8 part C: the kills below the waterline ({@link DeepDeaths}). */
    public DeepDeaths deepDeaths() {
        return passes.deepDeaths;
    }

    /** M9 part C: the play-field x of a backdrop strobe whose flash started in the last draw, NaN for none. */
    public double strobed() {
        return passes.backdrop.strobed();
    }

    /**
     * M9 part G: each enemy kind's sprite root ({@link EnemyLooks#root}: the Warden Tank's Ascendancy livery from Act 5),
     * for the looks beyond the hull (its turret and wreck); before the first draw.
     */
    public void liveries(java.util.function.Function<String, String> roots) {
        passes.kinds.roots(roots);
    }

    /** M9 part A: the kills in vacuum ({@link VacuumDeaths}). */
    public VacuumDeaths vacuumDeaths() {
        return passes.vacuumDeaths;
    }

    /**
     * M9 part C: whether a unit of {@code kind} breaks up in pieces of its own at its death (the Gilded Gunship's halves,
     * {@link MinefieldLooks}) instead of leaving the vacuum wreck.
     */
    public boolean breaksUp(Sortie sortie, int kind) {
        return passes.kinds.minefield(sortie).breaksUp(kind);
    }

    /**
     * M9 part A: the level's homing missiles are the Ascendancy's homing diamonds ({@link DiamondLooks#diamonds}): they
     * are drawn as diamonds, popping and fizzling as such.
     */
    public void diamonds(boolean diamonds) {
        passes.diamonds(diamonds);
    }

    /** M9 part A: the homing diamonds' looks; null in a level without them. */
    public DiamondLooks diamondLooks() {
        return passes.diamonds;
    }

    /**
     * M5 part G: the difficulty's presentation: on {@code easy} a peak with an easy length (Level 13's
     * whiteout, 6 s) in the backdrop and the snow; M7 part E: the dust storm's radii (Level 18).
     */
    public void difficulty(Difficulty difficulty) {
        passes.difficulty(difficulty);
    }

    /**
     * M7 part D: the level's ground allies' looks (Level 17's domes), each in its sprite set: its own {@code sprite}
     * in the level's {@code allies} block, else its ally's ({@code allies}); none without them.
     */
    public void groundAllies(LevelScript script, AlliesData allies) {
        passes.groundAllies(script, allies);
    }

    /**
     * The fitted utility modules' marks: the Targeting computer's (over the units, under the
     * bullets) and the Salvage scanner's glint (over the ground objects, cranes and tows, in the
     * dark as well); null for a module that is not fitted.
     */
    public void modules(TargetingOverlay targetingOverlay, SecretGlints glints) {
        passes.modules(targetingOverlay, glints);
    }

    /** Frees the light map and the generated textures. */
    public void dispose() {
        passes.dispose();
    }

    /** The level restarts: the boss's parts forget their opening animations. */
    public void restart() {
        passes.restart();
    }

    /** M7 part C: the canyon's presentation beyond its collision lines (the level data's by default). */
    void canyon(WallProjection.Looks looks) {
        passes.canyon(looks);
    }

    /**
     * M7 part A: after each simulation step, the Act 3 arsenal's looks follow its events (catches,
     * drone breaks, dashes, scorch marks; {@link ArsenalLooks}).
     */
    public void stepped(Sortie sortie) {
        passes.stepped(sortie);
    }

    /** M5 part E: the renderer's own effects advance one simulation step (with the screen's). */
    public void stepEffects() {
        passes.stepEffects();
    }

    /** M5 part E: whether the level is over water: its kills and blasts burst on the water ({@link #surfaceWater()}). */
    public boolean overWater() {
        return passes.water != null;
    }

    /** M5 part E: the surface's water effects (bursts, splashes, ripple trains), started on the ground. */
    public Effects surfaceWater() {
        return passes.surfaceWater;
    }

    /** M5 part E: the effects under the water (under-water bursts), through the {@code sub} pass. */
    public Effects underWater() {
        return passes.underWater;
    }

    /** M5 part E: the torpedoes' bubble trails, on the surface over the chop (not through the {@code sub} pass). */
    public Effects bubbles() {
        return passes.bubbles;
    }

    /** M5 part E: solid effects on low-air (the frigate's flak puffs). */
    public Effects lowAir() {
        return passes.lowAir;
    }

    /**
     * Step GFX2: a militia flak burst ({@link MilitiaFlak}) at the play-field point (x, y) at step {@code tick}: its
     * flash core over the puff ({@link FlakFlashes}).
     */
    public void militiaFlak(double x, double y, long tick) {
        passes.flakFlashes.start(x, y, tick);
    }

    /** M5 part E: the escort frigate (convoy unit {@code k}) fired a flak burst at step {@code tick}: its bow gun flashes. */
    public void flak(int k, long tick) {
        passes.convoy.flak(k, tick);
    }

    /**
     * M5 part E: where a slam arm's segments lie, px from the boss's centre ({@link KrakenLooks#armPoints}),
     * for its severed arm's pops; empty without the Kraken's looks.
     */
    public static double[] armPoints(SetPiece piece, int part) {
        return piece.arena().isPresent() ? KrakenLooks.armPoints(piece, part) : new double[0];
    }

    /** M5 part E: the Kraken's head's offset from its centre, px (y up); 0 without the Kraken's looks. */
    public double krakenHeadDy() {
        return passes.kraken == null ? 0 : passes.kraken.headDy();
    }

    /** M5 part E: an enemy fired a volley from (x, y) at step {@code tick} (a gun's recoil frame). */
    public void enemyFired(double x, double y, long tick) {
        passes.units.enemyFired(x, y, tick);
    }

    /** M5 part H: a mortar lobbed from (x, y) at step {@code tick}: the Siege Spire's mortar pod opens. */
    public void lobbed(Sortie sortie, double x, double y, long tick) {
        passes.setPieces.lobbed(sortie, x, y, tick);
    }

    /** M5 part H: a boss's window launched a unit at step {@code tick}: the Siege Spire's maw flares. */
    public void bossLaunched(long tick) {
        passes.setPieces.bossLaunched(tick);
    }

    /** Set piece {@code k}'s break-up at its death, or null for none. */
    public SetPieceDeath death(int k) {
        return passes.setPieces.death(k);
    }

    /** M10 part A: Act 6's unit mechanics' looks, for the screen's effects ({@link JovianLooks}). */
    public JovianLooks jovian() {
        return passes.jovian;
    }

    /**
     * M10 part A, step A3b: strike {@code i}'s column was told at step {@code tick} (the sky flash, the banks seen
     * through); nothing in a level without strikes.
     */
    public void strikeTold(int i, long tick) {
        if (passes.lightning != null) {
            passes.lightning.told(i, tick);
        }
    }

    /** M10 part A, step A3b: strike {@code i} struck at step {@code tick} (its bolt's frames run from it). */
    public void strikeStruck(int i, long tick) {
        if (passes.lightning != null) {
            passes.lightning.struck(i, tick);
        }
    }

    /**
     * M10 part A, step A3b: a vent at (x, y) erupted at step {@code tick}, the ground scrolled by {@code groundScroll}:
     * its umbrella of sulphur haze opens; nothing in a level without eruptions.
     */
    public void erupted(double x, double y, long tick, double groundScroll) {
        if (passes.eruptions != null) {
            passes.eruptions.erupted(x, y, tick, groundScroll);
        }
    }

    /** M10 part A, step A3b: whether the last shield hit was turned into a bolt by the Reflex (its ring gold). */
    public void shieldReflex(boolean reflected) {
        passes.shipLooks.reflexHit(reflected);
    }

    /** Rook was hit at step {@code tick}: his hull flashes white. */
    public void wingmanHit(long tick) {
        passes.wingmanLooks.hit(tick);
    }

    /** A unit's pivot file in assets/pivots/, or null when it has none. */
    static JsonValue pivots(Files files, String name) {
        FileHandle file = files.internal("pivots/" + name + ".json");
        return file.exists() ? new JsonReader().parse(file) : null;
    }

    /**
     * @param effects glowing animations over the flyers: explosions, impacts, death glows (those of
     *     ground units on the ground, see {@link Effects#startOnGround})
     * @param debris solid animations on the ground: wrecks, debris, husks
     * @param pieces solid death pieces over the flyers (those of ground units on the ground)
     * @param blasts glowing animations on the ground below the flyers: the Airstrike's blasts
     * @param wrecks the set pieces whose death is playing, drawn breaking up
     * @param alpha interpolation between the previous and the current step
     * @param shieldShimmer 0..1, how strongly the ship shows its last shield hit
     * @param flashReduction tone the white hit and invulnerability flashes down (Gameplay tab)
     * @param screenFlash an act boss's death flash over the play field
     */
    public void draw(
            SpriteBatch batch,
            Sortie sortie,
            Effects effects,
            Effects debris,
            Effects pieces,
            Effects blasts,
            SetPieceWrecks wrecks,
            CreditNumbers credits,
            EdgeWarnings warnings,
            float alpha,
            float shieldShimmer,
            boolean flashReduction,
            ScreenFlash screenFlash) {
        passes.draw(
                batch,
                sortie,
                effects,
                debris,
                pieces,
                blasts,
                wrecks,
                credits,
                warnings,
                alpha,
                shieldShimmer,
                flashReduction,
                screenFlash);
    }

    /**
     * The steps of a boss's chained death (design/enemies/bosses): an act boss's from tail to head
     * over its chain length, a mid-boss's {@value BossPose#LEGACY_CHAIN_STEP_TICKS} steps a part.
     */
    public static int chainTicks(SetPiece piece) {
        double seconds = BossPose.chainSeconds(piece);
        return Double.isNaN(seconds) ? piece.partCount() * BossPose.LEGACY_CHAIN_STEP_TICKS : SimStep.ticks(seconds);
    }

    /** Whether a boss's death is the act boss's chain from tail to head (else the frigate's per-part bursts). */
    public static boolean tailToHead(SetPiece piece) {
        return !Double.isNaN(BossPose.chainSeconds(piece));
    }

    /** Whether a boss's death flashes the screen: an act boss's (mid-bosses have none). */
    public static boolean flashesAtDeath(SetPiece piece) {
        return BossPose.actBoss(piece);
    }

    /**
     * An act boss's chained death from tail to head in the pose it died in (its hull's length and
     * width from its size), the bursts' offsets at the scale it was drawn at.
     */
    public static List<DeathChain.Burst> deathChain(SetPiece piece) {
        double angle = BossPose.angle(piece, BossPose.turn(piece, 1));
        double[] xs = new double[piece.partCount()];
        double[] ys = new double[piece.partCount()];
        for (int p = 0; p < xs.length; p++) {
            xs[p] = piece.partOffsetX(p);
            ys[p] = piece.partOffsetY(p);
        }
        return DeathChain.plan(
                xs, ys, angle, piece.spec().size().height(), piece.spec().size().width(), chainTicks(piece));
    }

    /** The head end of a boss's hull in the pose it died in, px from its centre (x right, y up). */
    public static double[] deathHead(SetPiece piece) {
        double angle = BossPose.angle(piece, BossPose.turn(piece, 1));
        double length = piece.spec().size().height();
        return new double[] {DeathChain.headX(angle, length), DeathChain.headY(angle, length)};
    }

    /** M7 part H: the 0-based index of the script's crawling arena ({@code Section.crawls}), -1 without one. */
    static int crawlingArena(LevelScript script) {
        for (int i = 0; i < script.sections().size(); i++) {
            if (script.sections().get(i).crawls()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * M7 part H: the play field's sideways shake now, px at the full screen-shake setting (the caller scales it by the
     * setting): the ridge pass's light tremble ({@link ScreenShake#ridge}), the Dust Colossus's death throe (step GK,
     * the stronger of the two), none without either.
     */
    public float shakeX(Sortie sortie, double realSeconds) {
        float ridgeShake =
                passes.ridge == null ? 0 : ScreenShake.ridge(passes.ridge.shake(sortie.levelSeconds()), realSeconds);
        // Step GK: the Dust Colossus's death throe shakes the field hard.
        float throe = passes.colossus == null ? 0 : passes.colossus.shakeX(sortie, realSeconds);
        return Math.abs(throe) > Math.abs(ridgeShake) ? throe : ridgeShake;
    }

    /**
     * The ground's scroll as drawn {@code alpha} of the way from the last step to the next: the simulation's, less the
     * ground speed's share still to come (step GK: in Level 21's crawling arena the speed the simulation eases to 0
     * while the Colossus is out, so the ground and everything on it stands still under a risen mouth).
     */
    static double drawnScroll(Sortie sortie, float alpha) {
        return sortie.groundScroll() - sortie.groundSpeed() * SimStep.SECONDS * (1 - alpha);
    }

    /**
     * Step GK: the steps from the Dust Colossus's kill to its throe's white flash, and to its credit shower
     * (briefs/l21-art-names.md "Round 44: the death throe"; the simulation's 3 s chain is unchanged).
     */
    public static int colossusFlashTicks() {
        return SimStep.ticks(ColossusThroe.FLASH_SECONDS);
    }

    public static int colossusShowerTicks() {
        return SimStep.ticks(ColossusThroe.SHOWER_SECONDS);
    }

    /**
     * M9 part H: the steps from a ringed boss's kill (the Iron Sovereign) to its core's blow, its white flash and its
     * credit shower: every battery's cook-off, then the implosion's blow ({@link SovereignLooks}).
     */
    public static int ringBlastTicks(SetPiece piece) {
        var rings = piece.boss().flatMap(vanguard.sim.BossSpec::rings).orElseThrow();
        int batteries = rings.rings().stream().mapToInt(r -> r.parts().size()).sum();
        return SimStep.ticks(batteries * rings.cookoffSeconds() + SovereignLooks.BLAST_AFTER);
    }

    /** M5 part H: whether a set piece is the Siege Spire, drawn by its own looks (its death too). */
    public static boolean spire(SetPiece piece) {
        return SpireLooks.drawn(piece);
    }

    /** A set piece's scale at {@code altitude} (0 on the play plane, 1 on high-air), for it and its effects. */
    public static float highAirScale(double altitude) {
        return (float) (1 + (HIGH_AIR_SCALE - 1) * altitude);
    }

    /** A set piece's opacity at {@code altitude} (0 on the play plane, 1 on high-air), for it and its effects. */
    public static float highAirOpacity(double altitude) {
        return (float) (1 - (1 - HIGH_AIR_OPACITY) * altitude);
    }
}
