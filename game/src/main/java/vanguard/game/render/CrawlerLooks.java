package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Chain;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M7 part C: the Threadcrawler on the canyon floor (design/enemies/ground/threadcrawler; tools/art/
 * canyon_units.py): a ground chain ({@link Chain#ground()}) drawn with the ground units, no shadow, from its tail
 * to its head. Each part at its heading's nearest of 16; the head's jaws at 8 fps; the segments' legs ripple
 * down the body (4 leg frames at 10 fps, each segment one frame behind the one in front), a cut part's stopped
 * segments writhing twice as fast; the pores' lime glow over the tell ({@code threadcrawler-pore} 0-2,
 * additive) and the shot's flash (3). A killed crawler's curled husk ({@code threadcrawler-husk}) stays on the
 * ground where its head died for the rest of the level.
 */
final class CrawlerLooks {
    /** The head's jaws run at 8 fps. */
    private static final int JAW_TICKS = SimStep.PER_SECOND / 8;
    /** The legs run at 10 fps; a stopped (cut) part's writhe twice as fast. */
    private static final int LEG_TICKS = SimStep.PER_SECOND / 10;

    private static final int WRITHE_TICKS = LEG_TICKS / 2;
    /** A pore's shot flash, steps. */
    static final int FLASH_TICKS = 4;
    /** The husks kept on the ground (one crawler per wave; the oldest goes). */
    private static final int HUSKS = 8;
    /** The pore flashes in flight at once. */
    private static final int FLASHES = 16;

    private final boolean[] heads;
    private final Array<AtlasRegion> pore;
    private final AtlasRegion husk;
    private final double[] huskX = new double[HUSKS];
    private final double[] huskRow = new double[HUSKS];
    private int husks;
    private final double[] flashX = new double[FLASHES];
    private final double[] flashRow = new double[FLASHES];
    private final long[] flashTick = new long[FLASHES];
    private int flashes;

    /** The level's enemy kinds ({@link Sortie#enemyKinds()}): those with a {@code crawl} are crawlers' heads. */
    CrawlerLooks(Sprites sprites, List<EnemySpec> kinds) {
        heads = new boolean[kinds.size()];
        boolean any = false;
        for (int k = 0; k < kinds.size(); k++) {
            heads[k] = kinds.get(k).crawl().isPresent();
            any |= heads[k];
        }
        pore = any && sprites.has("threadcrawler-pore") ? sprites.frames("threadcrawler-pore") : null;
        husk = any && sprites.has("threadcrawler-husk") ? sprites.region("threadcrawler-husk") : null;
        restart();
    }

    /** A new attempt: no husks, no flashes. */
    void restart() {
        husks = 0;
        flashes = 0;
        java.util.Arrays.fill(flashTick, Long.MIN_VALUE / 2);
    }

    /** After each simulation step: a killed head leaves its husk, a fired pore flashes. */
    void stepped(Sortie sortie) {
        if (pore == null && husk == null) {
            return;
        }
        SimEvents events = sortie.events();
        double scroll = sortie.groundScroll();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type == SimEvents.Type.ENEMY_DESTROYED && head(events.value(i)) && husk != null) {
                int slot = husks % HUSKS;
                huskX[slot] = events.x(i);
                huskRow[slot] = Terrain.row(scroll, events.y(i));
                husks++;
            } else if (type == SimEvents.Type.PORE_FIRED) {
                int slot = flashes % FLASHES;
                flashX[slot] = events.x(i);
                flashRow[slot] = Terrain.row(scroll, events.y(i));
                flashTick[slot] = sortie.tick();
                flashes++;
            }
        }
    }

    /** Whether {@code kind} is a crawler's head. */
    boolean head(int kind) {
        return kind >= 0 && kind < heads.length && heads[kind];
    }

    /** The husks on the ground, under the ground units; {@code scroll} the ground's render scroll. */
    void drawHusks(SpriteBatch batch, double scroll) {
        if (husk == null) {
            return;
        }
        for (int i = Math.max(0, husks - HUSKS); i < husks; i++) {
            int slot = i % HUSKS;
            centred(batch, husk, huskX[slot], Terrain.y(scroll, huskRow[slot]));
        }
    }

    /** The ground chains, from tail to head, each part through {@code flasher} (its hit flash). */
    void drawChains(SpriteBatch batch, Sortie sortie, float alpha, EnemyLooks[] looks, BurrowerLooks.Flasher flasher) {
        long tick = sortie.tick();
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (!chain.ground()) {
                continue;
            }
            for (int k = chain.size() - 1; k >= 0; k--) {
                Enemy member = chain.member(k);
                if (member == null) {
                    continue;
                }
                EnemyLooks look = looks[member.kind()];
                AtlasRegion frame = look.frame(member.facing(), step(chain, member, k, tick, look));
                flasher.draw(batch, member, frame, member.renderX(alpha), member.renderY(alpha));
            }
        }
    }

    /** A part's animation step: the head's jaws, a segment's leg phase one behind the part in front. */
    static long step(Chain chain, Enemy member, int k, long tick, EnemyLooks look) {
        int phases = look.frames().size / look.headings();
        if (phases <= 1) {
            return 0;
        }
        if (member.spec() != chain.spec().segment()
                && member.spec() != chain.spec().tail()) {
            return tick / JAW_TICKS;
        }
        long base = tick / (chain.stopped(k) ? WRITHE_TICKS : LEG_TICKS);
        return Math.floorMod(base - k, (long) phases);
    }

    /** The pores' glow and their shots' flashes, additive, over the ground units. */
    void drawPores(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (pore == null) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        int tells = Math.min(3, pore.size - 1);
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (!chain.ground()) {
                continue;
            }
            for (int k = 1; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                double glow = member == null ? 0 : chain.poreGlow(k);
                if (glow <= 0) {
                    continue;
                }
                batch.setColor(1, 1, 1, (float) (0.4 + 0.6 * glow));
                AtlasRegion frame = pore.get(Math.min(tells - 1, (int) (glow * tells)));
                centred(batch, frame, member.renderX(alpha), member.renderY(alpha));
            }
        }
        batch.setColor(Color.WHITE);
        AtlasRegion shot = pore.get(pore.size - 1);
        long tick = sortie.tick();
        for (int i = Math.max(0, flashes - FLASHES); i < flashes; i++) {
            int slot = i % FLASHES;
            if (tick - flashTick[slot] < FLASH_TICKS) {
                centred(batch, shot, flashX[slot], Terrain.y(scroll, flashRow[slot]));
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void centred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
