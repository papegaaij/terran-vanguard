package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part B, step B4 (design/enemies/space/rift-skater, Look; round 84 A.2 a "manta"): the Rift Skater's lights on
 * ART-B's production frames (tools/art/gate_units.py). Its body turns by heading and fades for a jump through the
 * shared looks ({@link EnemyLooks}' 16 headings, {@link VrellLooks#drawBody}); its death is its {@code -tatters} and
 * {@code -death} ({@link EnemyLooks}' death pieces and glow), no tumbling wreck ({@link VrellLooks#breaksUp}). Here,
 * additive over the air units: the violet core's soft light ({@code rift-skater-glow}, faded with the body) and the
 * burst's tell, the muzzle glint growing and fading over the {@code fire_after} after each landing ({@code -glint_0..5}).
 * Nothing is allocated per frame.
 */
final class SkaterLooks {
    static final String SLUG = "rift-skater";
    /** The units whose landing is followed at once. */
    private static final int UNITS = 24;

    private final boolean[] skaters;
    private final AtlasRegion glow;
    private final Array<AtlasRegion> glint;
    private final SerialClock landings = new SerialClock(UNITS);

    private SkaterLooks(Sprites sprites, List<EnemySpec> kinds) {
        skaters = new boolean[kinds.size()];
        for (int k = 0; k < skaters.length; k++) {
            skaters[k] =
                    kinds.get(k).slug().equals(SLUG) && kinds.get(k).teleport().isPresent();
        }
        glow = sprites.region(SLUG + "-glow");
        glint = sprites.frames(SLUG + "-glint");
    }

    /** The Skater's lights for a level whose kinds hold it and whose atlas has its frames; null otherwise. */
    static SkaterLooks of(Sprites sprites, List<EnemySpec> kinds) {
        boolean any = kinds.stream().anyMatch(kind -> kind.slug().equals(SLUG));
        if (!any || sprites == null || !sprites.has(SLUG + "-glow") || !sprites.has(SLUG + "-glint")) {
            return null;
        }
        return new SkaterLooks(sprites, kinds);
    }

    private boolean is(Enemy enemy) {
        return enemy.kind() < skaters.length && skaters[enemy.kind()];
    }

    /** Whether a Skater has landed and glides on after a jump (the landing's clock runs from its first such step). */
    static boolean landed(Enemy enemy) {
        Teleporting jumps = enemy.teleport();
        return jumps.jumps() > 0
                && (jumps.phase() == Teleporting.Phase.GLIDE || jumps.phase() == Teleporting.Phase.OUT);
    }

    /** After each step: the landings' starts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        landings.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (is(enemy) && landed(enemy)) {
                landings.touch(enemy.serial(), tick);
            }
        }
        landings.end();
    }

    /** The level restarts. */
    void restart() {
        landings.clear();
    }

    /** The glint's frame {@code ticks} after a landing with the burst {@code fireAfter} s later; -1 outside it. */
    static int glintFrame(long ticks, double fireAfter, int frames) {
        int total = Math.max(1, SimStep.ticks(fireAfter));
        if (ticks < 0 || ticks >= total) {
            return -1;
        }
        return (int) Math.min(frames - 1, ticks * frames / total);
    }

    /** Over the air units (additive): each Skater's core light and its burst's glint after a landing. */
    void drawLights(SpriteBatch batch, Sortie sortie, float alpha) {
        long now = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!is(enemy)) {
                continue;
            }
            float light = VrellLooks.opacity(enemy) * ArsenalLooks.glowShare(enemy);
            if (light <= 0) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            batch.setColor(1, 1, 1, light);
            drawCentred(batch, glow, x, y);
            if (landed(enemy)) {
                double fireAfter = enemy.spec().teleport().orElseThrow().fireAfterSeconds();
                int frame = glintFrame(now - landings.since(enemy.serial(), now), fireAfter, glint.size);
                if (frame >= 0) {
                    batch.setColor(Color.WHITE);
                    drawCentred(batch, glint.get(frame), x, y);
                }
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
