package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.EnemySpec;

/**
 * M8 part C: the returning naval units' looks under water (design/enemies/naval, Returning units under water;
 * tools/art/l23_props.py), chosen by the medium where the unit is (the simulation's specs differ only by the raft's
 * drift): the Driftjelly on the play plane, its whole body at its four pulse frames ({@code driftjelly-deep}, no foam
 * collar, no {@code sub} pass; its {@code -pulse} bloom over it after the grade), and the Reef Spitter on its {@code
 * floor} base ({@code reef-spitter-floor}, on the ledges and racks, no raft) with its gun at the base's centre ({@value
 * #FLOOR_GUN_DY} px above the frame's middle). A kind without such art keeps its usual look.
 */
final class DeepBodyLooks {
    /** The floor base's gun sits this far above its frame's centre, px (the art's (28, 27) of 56 × 56). */
    static final float FLOOR_GUN_DY = 1;

    private final Array<Array<AtlasRegion>> deep = new Array<>();
    /** M8 part F: per kind with a deep body its veined look in the dark ({@code -glow} at its pulse frames), or null. */
    private final Array<Array<AtlasRegion>> veins = new Array<>();

    private final AtlasRegion[] floor;

    DeepBodyLooks(Sprites sprites, List<EnemySpec> kinds) {
        floor = new AtlasRegion[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            deep.add(sprites.has(slug + "-deep") ? sprites.frames(slug + "-deep") : null);
            veins.add(
                    sprites.has(slug + "-deep") && sprites.has(slug + "-glow") ? sprites.frames(slug + "-glow") : null);
            floor[k] = sprites.has(slug + "-floor")
                    ? sprites.frames(slug + "-floor").first()
                    : null;
        }
    }

    /** Kind {@code kind}'s whole body under water at its pulse frames (a jelly's), null for none. */
    Array<AtlasRegion> deep(int kind) {
        return kind >= 0 && kind < deep.size ? deep.get(kind) : null;
    }

    /**
     * M8 part F: kind {@code kind}'s veined look in the dark at its pulse frames (the Driftjelly's {@code -glow},
     * tools/art/l26_glows.py), drawn ungraded over its deep body in a dark level; null for none.
     */
    Array<AtlasRegion> veins(int kind) {
        return kind >= 0 && kind < veins.size ? veins.get(kind) : null;
    }

    /** Kind {@code kind}'s floor base under water (a raft's in the air), null for none. */
    AtlasRegion floor(int kind) {
        return kind >= 0 && kind < floor.length ? floor[kind] : null;
    }
}
