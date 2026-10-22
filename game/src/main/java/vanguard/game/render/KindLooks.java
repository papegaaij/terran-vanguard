package vanguard.game.render;

import com.badlogic.gdx.Files;
import java.util.List;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * The registry of the looks that follow the sortie's enemy kinds (M5 part F, M7 parts B to D): the
 * Lampreys' latch poses, the Burrowers, the Tendril Anchors, the Threadcrawler, the Choir Herald's aura
 * and the Shellbacks, M7 part E's Dust Devils and Ghost Drones, M7 part F's Warden Tanks, each built once from the
 * first sortie that asks for it.
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

    /** M7 part E: the Dust Devils' spin, rise, open core and grit swirls (built with the first sortie). */
    private VortexLooks vortices;

    /** M7 part E: the Ghost Drones' dark lamps under the EMP and their eye glints (built with the first sortie). */
    private GhostDroneLooks ghostDrones;

    /** M7 part F: the turret units' turrets, flashes and wrecks (built with the first sortie). */
    private TankLooks tanks;

    /** M8 part B: the Drill Rigs' pumps, decals, muzzle, remains and steam (built with the first sortie). */
    private DrillRigLooks rigs;

    /** M8 part C: the Eel Swarms' chains, tells, glows and husks (built with the first sortie). */
    private EelLooks eels;

    /** M8 part C: the Glassfins' glint frames and sparkle (built with the first sortie). */
    private GlassfinLooks glassfins;

    /** M8 part C: the siphon pumps' roots, glow, break and remains (built with the first sortie). */
    private SiphonPumpLooks pumps;

    /** M8 part C: the returning naval units' bodies under water (built with the first sortie). */
    private DeepBodyLooks deepBodies;

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

    /** M7 part E: the vortices' looks for the sortie's enemy kinds, built once. */
    VortexLooks vortices(Sortie sortie) {
        if (vortices == null) {
            vortices = new VortexLooks(sprites, sortie.enemyKinds());
        }
        return vortices;
    }

    /** M7 part E: the lane strafers' looks for the sortie's enemy kinds, built once. */
    GhostDroneLooks ghostDrones(Sortie sortie) {
        if (ghostDrones == null) {
            ghostDrones = new GhostDroneLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return ghostDrones;
    }

    /** M7 part F: the turret units' looks for the sortie's enemy kinds, built once. */
    TankLooks tanks(Sortie sortie) {
        if (tanks == null) {
            tanks = new TankLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return tanks;
    }

    /** M8 part B: the Drill Rigs' looks for the sortie's enemy kinds, built once. */
    DrillRigLooks rigs(Sortie sortie) {
        if (rigs == null) {
            rigs = new DrillRigLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return rigs;
    }

    /** M8 part C: the Eel Swarms' looks for the sortie's enemy kinds, built once. */
    EelLooks eels(Sortie sortie) {
        if (eels == null) {
            eels = new EelLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return eels;
    }

    /** M8 part C: the Glassfins' looks for the sortie's enemy kinds, built once. */
    GlassfinLooks glassfins(Sortie sortie) {
        if (glassfins == null) {
            glassfins = new GlassfinLooks(sprites, sortie.enemyKinds());
        }
        return glassfins;
    }

    /** M8 part C: the returning naval units' under-water bodies for the sortie's enemy kinds, built once. */
    DeepBodyLooks deepBodies(Sortie sortie) {
        if (deepBodies == null) {
            deepBodies = new DeepBodyLooks(sprites, sortie.enemyKinds());
        }
        return deepBodies;
    }

    /** M8 part C: the siphon pumps' looks for the sortie's enemy kinds, built once. */
    SiphonPumpLooks pumps(Sortie sortie) {
        if (pumps == null) {
            pumps = new SiphonPumpLooks(sprites, sortie.enemyKinds());
        }
        return pumps;
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
        if (vortices != null) {
            vortices.restart();
        }
        if (ghostDrones != null) {
            ghostDrones.restart();
        }
        if (tanks != null) {
            tanks.restart();
        }
        if (rigs != null) {
            rigs.restart();
        }
        if (eels != null) {
            eels.restart();
        }
        if (glassfins != null) {
            glassfins.restart();
        }
    }
}
