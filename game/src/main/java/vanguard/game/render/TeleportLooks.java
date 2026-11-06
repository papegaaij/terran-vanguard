package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part A, step A3a (design/enemies, the {@code teleport} movement; round 84 A.2 a: the ring <b>shrinks</b> onto the
 * landing point): a teleporting unit's arrival ring while it is gone, drawn at the ring's point (which slides away
 * from the ship if it must: the ring plays on at its moving centre). ART-A's {@code teleport-ring_0..9} (the Rift
 * Skater's, 22 to 3 px) or {@code teleport-ring-large_0..9} (a unit at least {@value #LARGE_FROM} px wide: the Choir
 * Seraph's, 34 to 6 px), additive, the frame by how far the ring has run ({@link #ringFrame}) so it fits any ring time
 * (easy's 0.7 s); without the art a soft violet ring shrinking over the same radii ({@link #ringRadius}). The fade
 * before a jump is the unit's own opacity ({@link VrellLooks#opacity}) under the screen's {@code teleport-fade}
 * effect. Allocates nothing per frame.
 */
final class TeleportLooks {
    /** A unit at least this wide wears the large ring (the Choir Seraph, 48 px; the Rift Skater is 26). */
    static final double LARGE_FROM = 40;
    /** The ring's radius from its start to its landing, px (the art's: the Skater's ring, the Seraph's). */
    static final double RING_FROM = 22;

    static final double RING_TO = 3;
    static final double LARGE_RING_FROM = 34;
    static final double LARGE_RING_TO = 6;
    /** The stand-in ring: the event plane's violet, brightening as it shrinks. */
    static final Color VIOLET = Color.valueOf("B48CFF");

    private final Array<AtlasRegion> ring;
    private final Array<AtlasRegion> large;
    private final SoftGlow glow;

    /** @param ring the ring's frames, @param large the large ring's (either null without its art) */
    TeleportLooks(Array<AtlasRegion> ring, Array<AtlasRegion> large, SoftGlow glow) {
        this.ring = ring;
        this.large = large;
        this.glow = glow;
    }

    /** Whether any of the kinds teleports. */
    static boolean any(java.util.List<EnemySpec> kinds) {
        return kinds.stream().anyMatch(kind -> kind.teleport().isPresent());
    }

    /** Whether a unit of this size wears the large ring. */
    static boolean large(Enemy enemy) {
        return enemy.hitbox().width() >= LARGE_FROM;
    }

    /** The frame of {@code frames} frames a ring shows when it has run {@code share} (0..1) of its time. */
    static int ringFrame(double share, int frames) {
        return Math.clamp((int) (share * frames), 0, frames - 1);
    }

    /** The stand-in ring's radius at {@code share} of its time, px: shrinking onto the point. */
    static double ringRadius(double share, boolean large) {
        double from = large ? LARGE_RING_FROM : RING_FROM;
        double to = large ? LARGE_RING_TO : RING_TO;
        return from + (to - from) * Math.clamp(share, 0, 1);
    }

    /** Over the air units: every ring forming now, additive. */
    void draw(SpriteBatch batch, Sortie sortie) {
        boolean any = false;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().teleport().isEmpty() || enemy.teleport().phase() != Teleporting.Phase.GONE) {
                continue;
            }
            if (!any) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                any = true;
            }
            Teleporting jumps = enemy.teleport();
            double share = jumps.share(enemy.spec().teleport().get());
            boolean big = large(enemy);
            Array<AtlasRegion> frames = big ? large : ring;
            if (frames != null && frames.size > 0) {
                batch.setColor(Color.WHITE);
                PlayFrame.drawCentred(batch, frames.get(ringFrame(share, frames.size)), jumps.ringX(), jumps.ringY());
            } else {
                SoftGlow.tint(batch, VIOLET, (float) (0.3 + 0.6 * share));
                glow.ring(batch, jumps.ringX(), jumps.ringY(), ringRadius(share, big));
            }
        }
        if (any) {
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
}
