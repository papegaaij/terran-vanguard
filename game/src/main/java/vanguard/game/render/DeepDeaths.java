package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
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
}
