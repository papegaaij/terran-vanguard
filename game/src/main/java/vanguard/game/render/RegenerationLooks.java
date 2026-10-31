package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies, Regeneration; design/enemies/air/chimera, The seams): a regenerating hybrid's tissue seams.
 * {@link #seamGlow} is the hook the hybrids' own seam layers read (the Chimera's {@code -seams} frames, part B; the
 * Ascendant's sacs): while it heals a teal pulse runs along the seams {@value #PULSE_HZ} times a second and they glow
 * brighter the closer it is to full; at full health they glow faintly, just hit they are dark. Until that art exists the
 * stand-in sprite wears the glow as a soft teal light over its body ({@link #draw}); M10 part B: a kind with its own
 * {@code <slug>-seams} frames (the Chimera's) is drawn by its looks ({@link CloudDiverLooks}) instead. The Targeting
 * computer's bar grows back with a teal edge ({@link TargetingOverlay}).
 */
final class RegenerationLooks {
    static final double PULSE_HZ = 2;
    /** The seams at full health, and just hit (dark), and the healing glow's range from empty to full. */
    static final double FULL = 0.25;

    static final double HIT = 0.05;
    static final double HEAL_LOW = 0.35;
    /** The stand-in light: this share of the unit's larger side across, at this share of the seam glow. */
    static final double LIGHT_SIZE = 1.1;

    static final float LIGHT_OPACITY = 0.55f;

    private final SoftGlow glow;
    private final boolean[] regenerates;

    RegenerationLooks(List<EnemySpec> kinds, SoftGlow glow) {
        this(kinds, glow, slug -> false);
    }

    /** @param ownSeams whether a kind (by its slug) has its own seam frames, drawn by its looks, not the stand-in light */
    RegenerationLooks(List<EnemySpec> kinds, SoftGlow glow, java.util.function.Predicate<String> ownSeams) {
        this.glow = glow;
        regenerates = new boolean[kinds.size()];
        for (int k = 0; k < regenerates.length; k++) {
            regenerates[k] = kinds.get(k).regeneration().isPresent()
                    && !ownSeams.test(kinds.get(k).slug());
        }
    }

    /** Whether any of the level's kinds regenerates. */
    static boolean any(List<EnemySpec> kinds) {
        return kinds.stream().anyMatch(kind -> kind.regeneration().isPresent());
    }

    /**
     * The seams' glow (0..1) of a unit {@code healing} or not, at {@code share} of its HP, {@code seconds} into the level:
     * healing, a pulse {@value #PULSE_HZ} a second over a glow rising from {@value #HEAL_LOW} empty to 1 full; at full
     * health {@value #FULL}; hurt and not healing (just hit, or waiting out its delay) {@value #HIT}.
     */
    static double seamGlow(boolean healing, double share, double seconds) {
        if (healing) {
            double level = HEAL_LOW + (1 - HEAL_LOW) * Math.clamp(share, 0, 1);
            double pulse = 0.55 + 0.45 * Math.cos(2 * Math.PI * PULSE_HZ * seconds);
            return level * pulse;
        }
        return share >= 1 ? FULL : HIT;
    }

    /** {@link #seamGlow} of {@code enemy} at step {@code tick}. */
    static double seamGlow(Enemy enemy, double ticks) {
        return seamGlow(
                enemy.regeneration().healing(), enemy.hp() / enemy.spec().hp(), ticks * SimStep.SECONDS);
    }

    /** The stand-in seams' light over the regenerating units on one side of the layers ({@code ground} or the flyers). */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, boolean ground) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.kind() >= regenerates.length
                    || !regenerates[enemy.kind()]
                    || enemy.cloaked()
                    || (DrawPasses.Depth.of(enemy) == DrawPasses.Depth.GROUND) != ground) {
                continue;
            }
            float light = (float) seamGlow(enemy, sortie.tick() + alpha) * LIGHT_OPACITY;
            batch.setColor(RegrowthLooks.TEAL.r, RegrowthLooks.TEAL.g, RegrowthLooks.TEAL.b, light);
            double size =
                    LIGHT_SIZE * Math.max(enemy.hitbox().width(), enemy.hitbox().height());
            glow.disc(batch, enemy.renderX(alpha), enemy.renderY(alpha), size);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }
}
