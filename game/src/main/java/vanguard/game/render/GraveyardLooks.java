package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import vanguard.sim.Cracking;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;

/**
 * M11 part D, step D4 (design/campaign Level 46; design/enemies/space/husk; tools/art/act7_deadfleet.py {@code husk}):
 * the Husk on the play field, built at the first use ({@link KindLooks#graveyard}).
 *
 * <ul>
 *   <li>Three hull shapes ({@code husk}, {@code husk-b}, {@code husk-c}: one hole, two, three), one per unit by its
 *       serial, each in {@value #HEADINGS} headings (frame k turned k × 11.25° clockwise from nose down): its slow spin
 *       is presentation only ({@link Enemy#driftSpin}, its drift line is its sim heading);
 *   <li>whole while it is armed, its {@code -cracked} frames from its tell on (the tell shows the cracked hull);
 *   <li>its tell's flare over the cracked hull ({@code husk-flare}, additive, 8 frames over the tell by how far it has
 *       run: 0.5 s, hard 0.4 s), peaking at frame 3 and gone by frame 7;
 *   <li>its collapse is its unit look's ({@code -slabs} and {@code -death}, {@link EnemyLooks}), its shards and death orbs
 *       {@link BulletLooks}' own looks ({@code husk-shard}, {@code husk-orb}).
 * </ul>
 *
 * <p>The seed casket draws as any fixed unit ({@code seed-casket}, its {@code -tatters} and {@code -death}). Nothing is
 * allocated per frame.
 */
final class GraveyardLooks {
    static final String HUSK = "husk";
    /** The hull shapes' sprite roots, by {@link #shape}. */
    static final String[] SHAPES = {HUSK, HUSK + "-b", HUSK + "-c"};

    static final int HEADINGS = 32;
    static final String CRACKED = "-cracked";
    static final String FLARE = HUSK + "-flare";

    /** Per enemy kind whether it is a Husk with its production frames. */
    private final boolean[] husks;
    /** Per shape its whole and cracked frames ({@value #HEADINGS} each). */
    private final List<Array<AtlasRegion>> whole = new ArrayList<>();

    private final List<Array<AtlasRegion>> cracked = new ArrayList<>();
    private final Array<AtlasRegion> flare;

    GraveyardLooks(Sprites sprites, List<EnemySpec> kinds) {
        husks = new boolean[kinds.size()];
        boolean frames = sprites.has(HUSK) && sprites.has(HUSK + CRACKED);
        for (int s = 0; s < SHAPES.length && frames; s++) {
            String root = sprites.has(SHAPES[s]) && sprites.has(SHAPES[s] + CRACKED) ? SHAPES[s] : HUSK;
            whole.add(sprites.frames(root));
            cracked.add(sprites.frames(root + CRACKED));
            frames = whole.get(s).size == HEADINGS && cracked.get(s).size == HEADINGS;
        }
        for (int k = 0; k < husks.length; k++) {
            husks[k] = frames
                    && kinds.get(k).slug().equals(HUSK)
                    && kinds.get(k).crack().isPresent();
        }
        flare = sprites.has(FLARE) ? sprites.frames(FLARE) : new Array<>(0);
    }

    /** Whether {@code enemy} is a Husk drawn here. */
    boolean husk(Enemy enemy) {
        int kind = enemy.kind();
        return kind >= 0 && kind < husks.length && husks[kind];
    }

    /** A unit's hull shape by its serial: 0, 1 or 2. */
    static int shape(int serial) {
        return Math.floorMod(serial, SHAPES.length);
    }

    /** The heading frame nearest a spin of {@code degrees} clockwise from nose down. */
    static int heading(double degrees) {
        return Math.floorMod((int) Math.round(degrees * HEADINGS / 360), HEADINGS);
    }

    /** Whether a Husk shows its cracked hull: from its tell on. */
    static boolean cracked(Enemy enemy) {
        return enemy.crack().phase() != Cracking.Phase.ARMED;
    }

    /** The tell's flare frame at {@code share} (0 to 1) of the tell, of {@code frames}. */
    static int flareFrame(double share, int frames) {
        return Math.clamp((int) Math.floor(share * frames), 0, frames - 1);
    }

    /** A Husk's frame: its shape, whole or cracked, at its spin's heading; null for another unit. */
    AtlasRegion frame(Enemy enemy, float alpha) {
        if (!husk(enemy)) {
            return null;
        }
        int s = shape(enemy.serial());
        Array<AtlasRegion> set = cracked(enemy) ? cracked.get(s) : whole.get(s);
        return set.get(heading(enemy.driftSpin(alpha)));
    }

    /** Over a Husk's body in its tell: the flare, additive. */
    void drawFlare(SpriteBatch batch, Enemy enemy, float alpha) {
        if (flare.isEmpty() || !husk(enemy) || enemy.crack().phase() != Cracking.Phase.TELLING) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawCentred(
                batch,
                flare.get(flareFrame(enemy.crack().tell(alpha), flare.size)),
                enemy.renderX(alpha),
                enemy.renderY(alpha));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
