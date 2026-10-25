package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part C (design/art-direction, Explosion ladder under water; gap check D8; tools/art/explosions.py {@code --deep},
 * proposal a until round 52's pick): a kill below the waterline. In place of its air rung's fireball the under-water
 * burst of the same rung ({@code explosion-deep-<rung>}, additive: the pressure flash and ring) over its cavity ({@code
 * explosion-deep-cavity-<rung>}, solid, the same frames' timing: no smoke), with the unit's own death glow and pieces;
 * a Glassfin's glass shards ({@code glassfin-shards}), an eel part's husk sinking ({@link EelLooks#husk}), a siphon
 * pump's break ({@code siphon-pump-break}) and its remains on the ground (the dead pump over its withered roots).
 */
public final class DeepDeaths {
    /** The rungs of the ladder, as the air ladder picks them for a kind ({@link EnemyLooks#explosion}). */
    static final String[] RUNGS = {"tiny", "small", "medium", "large"};

    private final Sprites sprites;
    private final KindLooks kinds;
    private final List<Array<AtlasRegion>> flashes = new ArrayList<>();
    private final List<Array<AtlasRegion>> cavities = new ArrayList<>();
    private final Array<AtlasRegion> shards;
    private final Array<AtlasRegion> none = new Array<>(0);

    DeepDeaths(Sprites sprites, KindLooks kinds) {
        this.sprites = sprites;
        this.kinds = kinds;
        for (String rung : RUNGS) {
            flashes.add(optional("explosion-deep-" + rung));
            cavities.add(optional("explosion-deep-cavity-" + rung));
        }
        shards = optional(GlassfinLooks.SHARDS);
    }

    private Array<AtlasRegion> optional(String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether the under-water bursts exist (else a kill under water keeps the air ladder). */
    public boolean present() {
        return !flashes.getFirst().isEmpty();
    }

    /** The rung (an index of {@link #RUNGS}) of a kind whose air burst is {@code explosion}. */
    int rung(Array<AtlasRegion> explosion) {
        if (explosion == sprites.explosionTiny) {
            return 0;
        }
        if (explosion == sprites.explosionSmall) {
            return 1;
        }
        return explosion == sprites.explosionLarge ? 3 : 2;
    }

    /** The under-water burst of {@code look}'s rung (additive). */
    public Array<AtlasRegion> flash(EnemyLooks look) {
        return flashes.get(rung(look.explosion()));
    }

    /** The {@code small} rung's under-water burst (an eel's cut). */
    public Array<AtlasRegion> smallFlash() {
        return flashes.get(1);
    }

    /** M8 part G: the {@code small} rung's cavity (a torpedo shot down or burst on a hull). */
    public Array<AtlasRegion> smallCavity() {
        return cavities.get(1);
    }

    /** M8 part G: the {@code tiny} rung's under-water burst (a torpedo run out). */
    public Array<AtlasRegion> tinyFlash() {
        return flashes.get(0);
    }

    /** The under-water burst's cavity of {@code look}'s rung (solid, under the flash). */
    public Array<AtlasRegion> cavity(EnemyLooks look) {
        return cavities.get(rung(look.explosion()));
    }

    /** M8 part C: kind {@code kind}'s extra solid pieces at its death under water at (x, y): shards or a husk. */
    public Array<AtlasRegion> pieces(Sortie sortie, int kind, double x, double y) {
        String slug = sortie.enemyKinds().get(kind).slug();
        if (slug.equals(GlassfinLooks.SLUG)) {
            return shards;
        }
        EelLooks eels = kinds.eels(sortie);
        return eels.part(kind) ? eels.husk(sortie, kind, x, y) : none;
    }

    /** M8 part C: a siphon pump's break (solid), empty for another kind. */
    public Array<AtlasRegion> pumpBreak(Sortie sortie, int kind) {
        SiphonPumpLooks pumps = kinds.pumps(sortie);
        return pumps.pump(kind) ? pumps.breakFrames() : none;
    }

    /**
     * M8 part C: a siphon pump's remains on the ground, each a one-frame animation (the withered roots, then the dead
     * pump); none for another kind.
     */
    public Array<Array<AtlasRegion>> pumpRemains(Sortie sortie, int kind) {
        SiphonPumpLooks pumps = kinds.pumps(sortie);
        return pumps.pump(kind) ? pumps.remains() : noRemains;
    }

    private final Array<Array<AtlasRegion>> noRemains = new Array<>(0);

    /** The frames' steps of a husk's sinking and a break's. */
    public static int piecesTicks(Sortie sortie, int kind) {
        String slug = sortie.enemyKinds().get(kind).slug();
        return slug.equals(GlassfinLooks.SLUG) ? 3 : EelLooks.HUSK_FRAME_TICKS;
    }

    /** The break's frames' steps. */
    public static int breakTicks() {
        return SiphonPumpLooks.BREAK_FRAME_TICKS;
    }

    /** M8 part D: an Abyss Ray's break-up (solid) at its last heading; empty for another kind. */
    public Array<AtlasRegion> rayBreakUp(Sortie sortie, int kind) {
        return kinds.rays(sortie).breakUp(kind);
    }

    /** M8 part D: an Abyss Ray's bioluminescent bloom (additive); empty for another kind. */
    public Array<AtlasRegion> rayBloom(Sortie sortie, int kind) {
        return kinds.rays(sortie).bloom(kind);
    }

    /** M8 part D: how opaque a Ray's death is drawn (faint for a shadow killed in the deep water). */
    public float rayOpacity(Sortie sortie) {
        return kinds.rays(sortie).deathOpacity();
    }

    /** M8 part D: the break-up's and the bloom's frames' steps. */
    public static int rayBreakUpTicks() {
        return AbyssRayLooks.DEATH_FRAME_TICKS;
    }

    public static int rayBloomTicks() {
        return AbyssRayLooks.BLOOM_FRAME_TICKS;
    }

    /** M8 part G: a Depth Hunter's hull break (solid); empty for another kind. */
    public Array<AtlasRegion> hunterBreak(Sortie sortie, int kind) {
        return kinds.hunters(sortie).breakUp(kind);
    }

    /** M8 part G: a Depth Hunter's oil bloom (solid, under its break); empty for another kind. */
    public Array<AtlasRegion> hunterOil(Sortie sortie, int kind) {
        return kinds.hunters(sortie).oil(kind);
    }

    /** M8 part G: the hull break's and the oil bloom's frames' steps. */
    public static int hunterBreakTicks() {
        return HunterLooks.BREAK_FRAME_TICKS;
    }

    public static int hunterOilTicks() {
        return HunterLooks.OIL_FRAME_TICKS;
    }

    /** M8 part G: a relay beacon's lamp breaking (additive, at its lamp); empty for another kind. */
    public Array<AtlasRegion> beaconBreak(Sortie sortie, int kind) {
        return kinds.beacons(sortie).breakUp(kind);
    }

    /** M8 part G: the lamp's place from the beacon's centre, x and y (up), px. */
    public double beaconLampDx(Sortie sortie) {
        return kinds.beacons(sortie).lampDx();
    }

    public double beaconLampDy(Sortie sortie) {
        return kinds.beacons(sortie).lampDy();
    }

    /** M8 part G: the lamp's break's frames' steps. */
    public static int beaconBreakTicks() {
        return BeaconLooks.BREAK_FRAME_TICKS;
    }

    /** M8 part G: a relay beacon's dark mast left on the ground, a one-frame animation; none for another kind. */
    public Array<Array<AtlasRegion>> beaconRemains(Sortie sortie, int kind) {
        return kinds.beacons(sortie).remains(kind);
    }

    /** M8 part D: a Barbfin's shell sinking at its heading (solid); empty for another kind. */
    public Array<AtlasRegion> barbfinShell(Sortie sortie, int kind, double x, double y) {
        return kinds.barbfins(sortie).shell(kind, x, y);
    }

    /** M8 part D: a Barbfin's ink cloud (solid); empty for another kind. */
    public Array<AtlasRegion> barbfinInk(Sortie sortie, int kind) {
        return kinds.barbfins(sortie).ink(kind);
    }

    /** M8 part D: the shell's and the ink's frames' steps, the shell's sinking (px/s, down) and the ink's drop, px. */
    public static int shellTicks() {
        return BarbfinLooks.SHELL_FRAME_TICKS;
    }

    public static int inkTicks() {
        return BarbfinLooks.INK_FRAME_TICKS;
    }

    public static double shellSink() {
        return BarbfinLooks.SHELL_SINK;
    }

    public static double inkBelow() {
        return BarbfinLooks.INK_BELOW;
    }

    /** M8 part E: whether kind {@code kind}'s death plays on the ground (a Siren's nest scrolls with its wall). */
    public boolean mounted(Sortie sortie, int kind) {
        return kinds.sirens(sortie).siren(kind);
    }

    /** M8 part E: a Siren's collapse at its facing (solid; at {@code x}, its unit's centre); empty for another kind. */
    public Array<AtlasRegion> sirenCollapse(Sortie sortie, int kind, double x) {
        return kinds.sirens(sortie).collapse(kind, x);
    }

    /** M8 part E: a Siren's husk left on its wall, a one-frame animation; empty for another kind. */
    public Array<AtlasRegion> sirenHusk(Sortie sortie, int kind, double x) {
        return kinds.sirens(sortie).husk(kind, x);
    }

    /** M8 part E: a Siren's pale plume (solid, stepped translucency); empty for another kind. */
    public Array<AtlasRegion> sirenPlume(Sortie sortie, int kind) {
        return kinds.sirens(sortie).plume(kind);
    }

    /** M8 part E: the collapse's and the art's centre across from the unit's ({@code x}), and the sac's. */
    public double sirenBodyX(double x) {
        return SirenLooks.bodyX(x);
    }

    public double sirenSacX(Sortie sortie, double x) {
        return kinds.sirens(sortie).sacX(x);
    }

    /** M8 part E: the sac's height from the unit's centre, px. */
    public double sirenSacDy(Sortie sortie, double x) {
        return kinds.sirens(sortie).sacDy(x);
    }

    /** M8 part E: the collapse's frames' steps, the plume's, its rise (px/s, up) and the husk's wait (the collapse). */
    public static int sirenCollapseTicks() {
        return SirenLooks.DEATH_FRAME_TICKS;
    }

    public static int sirenPlumeTicks() {
        return SirenLooks.PLUME_FRAME_TICKS;
    }

    public static double sirenPlumeRise(int frames) {
        return SirenLooks.PLUME_RISE / Math.max(1, frames * SirenLooks.PLUME_FRAME_TICKS * SimStep.SECONDS);
    }

    public static int sirenHuskDelay() {
        return SirenLooks.DEATH_STEPS * SirenLooks.DEATH_FRAME_TICKS;
    }

    /** M8 part E: a Spiral Nautilus's shell cracked into halves (solid); empty for another kind. */
    public Array<AtlasRegion> nautilusHalves(Sortie sortie, int kind) {
        return kinds.nautili(sortie).halves(kind);
    }

    /**
     * M8 part E: a Spiral Nautilus killed uncoiled near (x, y): its dark crown (solid), offset by {@link
     * #nautilusDarkDx}, {@link #nautilusDarkDy}; empty for another kind or a closed shell.
     */
    public Array<AtlasRegion> nautilusDark(Sortie sortie, int kind, double x, double y) {
        return kinds.nautili(sortie).darkCrown(kind, x, y);
    }

    public double nautilusDarkDx(Sortie sortie) {
        return kinds.nautili(sortie).darkDx();
    }

    public double nautilusDarkDy(Sortie sortie) {
        return kinds.nautili(sortie).darkDy();
    }

    /** M8 part E: the halves' frames' steps and the dark crown's sinking, px/s. */
    public static int halvesTicks() {
        return NautilusLooks.HALVES_FRAME_TICKS;
    }

    public static double darkSink() {
        return NautilusLooks.DARK_SINK;
    }
}
