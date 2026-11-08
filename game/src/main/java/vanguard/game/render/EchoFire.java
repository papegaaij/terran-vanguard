package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.BreachArena;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G6 (design/enemies/bosses: the Brood Carrier's, the Dust Colossus's and the Iron Sovereign's
 * <i>Echo</i>, step G5's two stages to the user's boss recipe of round 93 L.3: each subtarget its own gun, silent at its
 * death): Level 49's echoes' fire at its port, the {@link ChimeraFire}/{@link HeartLooks} pattern. Each shot event
 * ({@link SimEvents.Type#ENEMY_FIRED}, {@link SimEvents.Type#MISSILE_LAUNCHED}, {@link SimEvents.Type#MORTAR_LOBBED})
 * goes to the living part of an echo whose port lies within {@value #REACH} px of it: a part's centre (the Carrier's
 * sacs and mandibles, the Colossus's vents, the Sovereign's turrets and core), the breaching mouth's own place for the
 * Colossus's mouth (its burrow volley leaves the bulge while the mouth is still under way), and any shot no other part
 * fired from the body's ridge is the mouth's too (step G5b: with the ship on the mouth's lip the spine spits its burrow
 * volley instead); a dead part fires nothing, so it never flashes. The forming volleys (each echo's from its parts)
 * flash the same way. A breaching echo's flash shows where its shot left (the bulge, the spine), the others' at their
 * parts' ports as drawn.
 *
 * <p>Drawn after the echoes and outside {@link EchoTint} (fire, like the bullets: bright, never dulled by the memory):
 * the shared gun muzzle star ({@code ballistic-muzzle}, always loaded) at {@value #SCALE}× with its core again at {@value #CORE_SCALE}×
 * in white (the bullet it fires sits on its centre: the rays and the white core read round it),
 * additive, tinted per echo (the Carrier's lime-teal spores, the Colossus's hot dust, the Sovereign's white-gold), on
 * the Sovereign's turrets at their muzzle tips ({@link SovereignLooks#muzzleAt}). Nothing is allocated per step once
 * the pieces are counted.
 */
final class EchoFire {
    /** A shot this near a part's port is its gun's, px. */
    static final double REACH = 8;
    /** A flash: the muzzle star's frames, this many steps each. */
    static final int TICKS_PER_FRAME = 3;
    /** The star's scale (20 px → 50 px) and its white core's over it. */
    static final float SCALE = 2.5f;

    static final float CORE_SCALE = 1.25f;
    /** The flash's frames when the sprites are missing (the tests). */
    static final int FRAMES = 3;
    /** The shared muzzle star. */
    static final String SPRITE = "ballistic-muzzle";
    /** The Carrier's flash: lime-teal (Vrell spores, as the Chimera's sacs'). */
    static final Color SPORE = ChimeraLooks.SPORE_FLASH;
    /** The Colossus's flash: hot dust. */
    static final Color DUST = new Color(1f, 0.84f, 0.6f, 1f);
    /** The Sovereign's flash: white-gold. */
    static final Color STATION = new Color(1f, 0.95f, 0.82f, 1f);

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final Array<AtlasRegion> frames;
    /** Per set piece (null for one that is no echo) per part the step its gun last fired, NEVER before. */
    private long[][] fired = new long[0][];
    /** Per set piece per part where its last shot left (the simulation's place): a breaching echo's flashes. */
    private double[][] firedX = new double[0][];

    private double[][] firedY = new double[0][];

    private final double[] port = new double[2];

    private EchoFire(Sprites sprites) {
        frames = sprites != null && sprites.has(SPRITE) ? sprites.frames(SPRITE) : new Array<>(0);
    }

    /** The echoes' fire for a level that flies echoes (its rush, Level 49); null for any other. */
    static EchoFire of(Sprites sprites, LevelScript script) {
        return EchoTint.wanted(script) ? new EchoFire(sprites) : null;
    }

    /** For the tests: following a sortie without the sprites (it draws nothing). */
    static EchoFire following() {
        return new EchoFire(null);
    }

    /** The level restarts (or a fight is retried): no flash. */
    void reset() {
        for (long[] parts : fired) {
            if (parts != null) {
                Arrays.fill(parts, NEVER);
            }
        }
    }

    /** After a simulation step: the echoes' parts whose guns fired in it. */
    void stepped(Sortie sortie) {
        int count = sortie.setPieceCount();
        if (fired.length != count) {
            fired = new long[count][];
            firedX = new double[count][];
            firedY = new double[count][];
        }
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int k = 0; k < count; k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.echo().isEmpty()) {
                continue;
            }
            if (fired[k] == null || fired[k].length != piece.partCount()) {
                fired[k] = new long[piece.partCount()];
                firedX[k] = new double[piece.partCount()];
                firedY[k] = new double[piece.partCount()];
                Arrays.fill(fired[k], NEVER);
            }
            long[] parts = fired[k];
            for (long t : parts) {
                if (t > tick) {
                    // A retry from a fight's checkpoint.
                    Arrays.fill(parts, NEVER);
                    break;
                }
            }
            if (!piece.present()) {
                continue;
            }
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.ENEMY_FIRED
                        || type == SimEvents.Type.MISSILE_LAUNCHED
                        || type == SimEvents.Type.MORTAR_LOBBED) {
                    int p = firer(piece, events.x(i), events.y(i));
                    if (p >= 0) {
                        parts[p] = tick;
                        firedX[k][p] = events.x(i);
                        firedY[k][p] = events.y(i);
                    }
                }
            }
        }
    }

    /** The living part of {@code piece} whose gun fired a shot at (x, y): the nearest port within {@value #REACH} px; -1. */
    static int firer(SetPiece piece, double x, double y) {
        int best = -1;
        double nearest = REACH * REACH;
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.partHp(p) <= 0) {
                continue;
            }
            double dx = portX(piece, p) - x;
            double dy = portY(piece, p) - y;
            double d = dx * dx + dy * dy;
            if (d <= nearest) {
                nearest = d;
                best = p;
            }
        }
        BreachArena breach = piece.breach().orElse(null);
        if (best < 0 && breach != null && piece.partHp(breach.mouth()) > 0 && onRidge(breach, x, y)) {
            // Step G5b: the spine spits the mouth's burrow volley while the ship is on its lip.
            return breach.mouth();
        }
        return best;
    }

    /** Whether (x, y) lies within {@value #REACH} px of a breaching body's ridge (its back, from the tail to the mouth). */
    static boolean onRidge(BreachArena breach, double x, double y) {
        for (int i = 1; i < BreachArena.RIDGE_POINTS; i++) {
            double ax = breach.ridgeX(i - 1);
            double ay = breach.ridgeY(i - 1);
            double dx = breach.ridgeX(i) - ax;
            double dy = breach.ridgeY(i) - ay;
            double length = dx * dx + dy * dy;
            double u = length < 1e-9 ? 0 : Math.clamp(((x - ax) * dx + (y - ay) * dy) / length, 0, 1);
            double ex = ax + u * dx - x;
            double ey = ay + u * dy - y;
            if (ex * ex + ey * ey <= REACH * REACH) {
                return true;
            }
        }
        return false;
    }

    /** Part {@code p}'s port in the simulation: a breaching mouth's place (its bulge while under way), else its centre. */
    static double portX(SetPiece piece, int p) {
        BreachArena breach = piece.breach().orElse(null);
        return breach != null && breach.mouth() == p ? breach.mouthX() : piece.partX(p);
    }

    /** As {@link #portX}, its height. */
    static double portY(SetPiece piece, int p) {
        BreachArena breach = piece.breach().orElse(null);
        return breach != null && breach.mouth() == p ? breach.mouthY() : piece.partY(p);
    }

    /** The steps since part {@code p} of set piece {@code k} last fired at {@code tick}; -1 when its flash is over. */
    int flashTicks(int k, int p, long tick) {
        if (k < 0 || k >= fired.length || fired[k] == null || p < 0 || p >= fired[k].length || fired[k][p] == NEVER) {
            return -1;
        }
        long since = tick - fired[k][p];
        int frames = Math.max(FRAMES, this.frames.size);
        return since >= 0 && since < (long) frames * TICKS_PER_FRAME ? (int) since : -1;
    }

    /**
     * The flashes, additive, over the echoes and outside the tint: each at its part's port ({@code sovereign}'s turrets
     * at their muzzle tips; null: the parts' centres).
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, SovereignLooks sovereign) {
        if (frames.isEmpty()) {
            return;
        }
        long tick = sortie.tick();
        boolean any = false;
        for (int k = 0; k < fired.length && k < sortie.setPieceCount() && !any; k++) {
            for (int p = 0; fired[k] != null && p < fired[k].length && !any; p++) {
                any = flashTicks(k, p, tick) >= 0;
            }
        }
        if (!any) {
            return;
        }
        // Drawn scaled, so linearly filtered (the page's own filter back after them).
        Texture texture = frames.first().getTexture();
        Texture.TextureFilter min = texture.getMinFilter();
        Texture.TextureFilter mag = texture.getMagFilter();
        batch.flush();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int k = 0; k < fired.length && k < sortie.setPieceCount(); k++) {
            if (fired[k] == null) {
                continue;
            }
            SetPiece piece = sortie.setPiece(k);
            for (int p = 0; p < fired[k].length; p++) {
                int since = flashTicks(k, p, tick);
                if (since < 0) {
                    continue;
                }
                port(k, piece, p, alpha, sovereign);
                AtlasRegion region = frames.get(Math.min(frames.size - 1, since / TICKS_PER_FRAME));
                batch.setColor(tint(piece));
                drawScaled(batch, region, port[0], port[1], SCALE);
                batch.setColor(Color.WHITE);
                drawScaled(batch, region, port[0], port[1], CORE_SCALE);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.flush();
        texture.setFilter(min, mag);
    }

    /** The echo's flash colour, by its original. */
    static Color tint(SetPiece piece) {
        if (piece.breach().isPresent()) {
            return DUST;
        }
        if (piece.ring().isPresent()) {
            return STATION;
        }
        return SPORE;
    }

    /**
     * Part {@code p} of set piece {@code k}'s port as drawn, into {@link #port}: a breaching echo's where its shot left;
     * a turret's muzzle tip; else the part interpolated with the piece.
     */
    private void port(int k, SetPiece piece, int p, float alpha, SovereignLooks sovereign) {
        if (piece.breach().isPresent()) {
            port[0] = firedX[k][p];
            port[1] = firedY[k][p];
            return;
        }
        if (sovereign != null && piece.ring().isPresent() && sovereign.muzzleAt(piece, p, alpha, port)) {
            return;
        }
        port[0] = piece.renderX(alpha) + piece.partOffsetX(p);
        port[1] = piece.renderY(alpha) + piece.partOffsetY(p);
    }

    private static void drawScaled(SpriteBatch batch, AtlasRegion region, double x, double y, float scale) {
        float w = region.getRegionWidth() * scale;
        float h = region.getRegionHeight() * scale;
        batch.draw(region, (float) (X0 + x - w / 2), (float) (y - h / 2), w, h);
    }
}
