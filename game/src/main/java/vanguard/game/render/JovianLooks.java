package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * M10 part A: the looks of Act 6's unit mechanics on the play field, each family its own class, built with the first
 * sortie from the level's kinds (a family the level's units do not use stays null and costs nothing): the mirrors
 * ({@link MirrorLooks}), the regrowing turrets ({@link RegrowthLooks}), the regenerating hybrids' seams ({@link
 * RegenerationLooks}), the pylons' domes ({@link PylonDomeLooks}, in a level with a pylon) and the prey hits' and
 * spoofed turrets' flashes ({@link JovianFlashes}, every level: the spoofer fools Act 5's turrets too). The screen's
 * effects hand their events to it ({@code EventEffects}: {@code MirrorEffects}, {@code DomeEffects}).
 */
public final class JovianLooks implements Disposable {
    private final Sprites sprites;
    private final SoftGlow glow;
    private boolean built;
    private MirrorLooks mirrors;
    private RegrowthLooks regrowth;
    private RegenerationLooks regeneration;
    private PylonDomeLooks domes;
    private final JovianFlashes flashes;

    JovianLooks(Sprites sprites, SoftGlow glow) {
        this.sprites = sprites;
        this.glow = glow;
        flashes = new JovianFlashes(glow);
    }

    /** After each step (the first builds the families the level's kinds need). */
    void stepped(Sortie sortie) {
        if (!built) {
            build(sortie.enemyKinds(), sortie.domes() != null);
        }
        if (domes != null) {
            domes.stepped(sortie);
        }
    }

    private void build(List<EnemySpec> kinds, boolean pylons) {
        built = true;
        mirrors = MirrorLooks.any(kinds) ? new MirrorLooks(kinds, glow) : null;
        regrowth = RegrowthLooks.any(kinds) ? new RegrowthLooks(kinds, glow) : null;
        // M10 part B: a hybrid with its own seam frames (the Chimera's) wears them (CloudDiverLooks), not the light.
        regeneration = RegenerationLooks.any(kinds)
                ? new RegenerationLooks(kinds, glow, slug -> sprites.has(slug + "-seams"))
                : null;
        domes = pylons || kinds.stream().anyMatch(kind -> kind.dome().isPresent())
                ? new PylonDomeLooks(sprites, glow)
                : null;
    }

    /** The level restarts. */
    void restart() {
        if (mirrors != null) {
            mirrors.restart();
        }
        if (domes != null) {
            domes.restart();
        }
        flashes.restart();
    }

    /** The ground side, over the ground units: the roots, the domes and sheens, the ground plates and seams, stutters. */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        if (regrowth != null) {
            regrowth.draw(batch, sortie, alpha);
        }
        if (regeneration != null) {
            regeneration.draw(batch, sortie, alpha, true);
        }
        if (mirrors != null) {
            mirrors.draw(batch, sortie, alpha, seconds, true);
        }
        if (domes != null) {
            domes.draw(batch, sortie, alpha);
        }
    }

    /** The flyers' side, over the air units: the plates and their shards, the seams, the prey hits and the stutters. */
    void drawAir(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        if (regeneration != null) {
            regeneration.draw(batch, sortie, alpha, false);
        }
        if (mirrors != null) {
            mirrors.draw(batch, sortie, alpha, seconds, false);
        }
        flashes.draw(batch, sortie, alpha);
    }

    /** The regrowing units' bodies, drawn in the unit pass in their place; null in a level without them. */
    RegrowthLooks regrowth() {
        return regrowth;
    }

    /** A mirror broke at (x, y) ({@code how}: 1 shattered, 0 a heat break): its shards fly. */
    public void mirrorBroken(Sortie sortie, double x, double y, int how) {
        if (mirrors != null) {
            mirrors.shatter(sortie, x, y, how);
        }
    }

    /** A dome collapsed for good at (x, y): its ring shrinks to its pylon's crown. */
    public void domeCollapsed(Sortie sortie, double x, double y) {
        if (domes != null) {
            domes.collapse(sortie, x, y);
        }
    }

    /** The dome hit's spark frames; null without their art (the screen sparks with the shared glance instead). */
    public com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion> domeHit() {
        return domes == null ? null : domes.hit();
    }

    /** A hunter struck its prey at (x, y). */
    public void preyHit(double x, double y, long tick) {
        flashes.preyHit(x, y, tick);
    }

    /** A spoofable unit at (x, y) holds its volley: its muzzle stutters amber. */
    public void hesitate(Sortie sortie, double x, double y) {
        flashes.hesitate(sortie, x, y, sortie.tick());
    }

    @Override
    public void dispose() {
        if (mirrors != null) {
            mirrors.dispose();
        }
    }
}
