package vanguard.game.render;

import com.badlogic.gdx.Files;
import java.util.List;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * The registry of the looks that follow the sortie's enemy kinds (M5 part F, M7 parts B to D): the
 * Lampreys' latch poses, the Burrowers, the Tendril Anchors, the Threadcrawler, the Choir Herald's aura
 * and the Shellbacks, each built once from the first sortie that asks for it.
 */
final class KindLooks {
    /** M5 part F: per enemy kind its latch poses (the Lamprey's), null for a kind without; built at the first draw. */
    private LampreyLooks[] lampreys;

    /** M7 part B: the Burrowers' ripples, bulges, bodies and ground marks; built at the first step or draw. */
    private BurrowerLooks burrowers;

    /** M7 part C: the Tendril Anchors' facing and pulse and their tendrils (built with the first sortie). */
    private TendrilLooks tendrils;

    /** M7 part C: the Threadcrawler on the ground, its pores and husks (built with the first sortie). */
    private CrawlerLooks crawlers;

    /** M7 part D: the Choir Herald's aura, rings, threads and crest; null without one (built with the first sortie). */
    private AuraLooks aura;

    private boolean auraBuilt;

    /** M7 part D: the Shellback's curl, roll, belly and vent (built with the first sortie). */
    private ShellbackLooks shellbacks;

    private final Sprites sprites;
    private final Files files;

    KindLooks(Sprites sprites, Files files) {
        this.sprites = sprites;
        this.files = files;
    }

    /** M7 part B: the Burrowers' looks for the sortie's enemy kinds, built once. */
    BurrowerLooks burrowers(Sortie sortie) {
        if (burrowers == null) {
            burrowers = new BurrowerLooks(sprites, sortie.enemyKinds());
        }
        return burrowers;
    }

    TendrilLooks tendrils(Sortie sortie) {
        if (tendrils == null) {
            tendrils = new TendrilLooks(sprites, sortie.enemyKinds());
        }
        return tendrils;
    }

    CrawlerLooks crawlers(Sortie sortie) {
        if (crawlers == null) {
            crawlers = new CrawlerLooks(sprites, sortie.enemyKinds());
        }
        return crawlers;
    }

    /** M7 part D: the Choir Herald's aura looks for the sortie's enemy kinds, built once; null without a Herald. */
    AuraLooks aura(Sortie sortie) {
        if (!auraBuilt) {
            aura = AuraLooks.of(sprites, sortie.enemyKinds());
            auraBuilt = true;
        }
        return aura;
    }

    /** M7 part D: the Shellbacks' curl-roll looks for the sortie's enemy kinds, built once. */
    ShellbackLooks shellbacks(Sortie sortie) {
        if (shellbacks == null) {
            shellbacks = new ShellbackLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return shellbacks;
    }

    /** M5 part F: the latch poses of the sortie's enemy kinds (the Lamprey's), null where a kind has none. */
    private LampreyLooks[] lampreyLooks(Sortie sortie) {
        List<EnemySpec> kinds = sortie.enemyKinds();
        LampreyLooks[] found = new LampreyLooks[kinds.size()];
        for (int k = 0; k < found.length; k++) {
            EnemySpec kind = kinds.get(k);
            if (kind.latch().isPresent()) {
                found[k] = LampreyLooks.of(sprites, kind.slug(), LevelRenderer.pivots(files, kind.slug()));
            }
        }
        return found;
    }

    /** M5 part F: the latch poses by enemy kind ({@link #lampreyLooks}), null before the first draw. */
    LampreyLooks[] lampreys() {
        return lampreys;
    }

    /** M5 part F: builds the latch poses at the first draw. */
    void buildLampreys(Sortie sortie) {
        if (lampreys == null) {
            lampreys = lampreyLooks(sortie);
        }
    }

    /** The level restarts: the looks built so far forget their animations. */
    void restart() {
        if (burrowers != null) {
            burrowers.restart();
        }
        if (crawlers != null) {
            crawlers.restart();
        }
        if (aura != null) {
            aura.restart();
        }
        if (shellbacks != null) {
            shellbacks.restart();
        }
    }
}
