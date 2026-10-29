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

    /** M8 part D: the Abyss Rays' shadow, rise, glide, sink, glow and mouth flash (built with the first sortie). */
    private AbyssRayLooks rays;

    /** M8 part D: the Barbfins' swim, tell and snout sparkle (built with the first sortie). */
    private BarbfinLooks barbfins;

    /** M8 part E: the Sirens' nests, glow, rings and the slowed hulls' shimmer (built with the first sortie). */
    private SirenLooks sirens;

    /** M8 part E: the Spiral Nautili's shell, crown, glow and flecks (built with the first sortie). */
    private NautilusLooks nautili;

    /** M8 part F: the Glow Anglers' heading, jaw, lure, stalk, ring flash and death (built with the first sortie). */
    private AnglerLooks anglers;
    /** M8 part G: the Depth Hunters' rise, flicker, glow, death and torpedoes (built with the first sortie). */
    private HunterLooks hunters;
    /** M8 part G: the relay beacons' pulse, light, break and dark mast (built with the first sortie). */
    private BeaconLooks beacons;

    /** M9 part B: Level 29's units' looks (the Talon, SAM Nest, Asteroid Mite, wavering Stormhawk, clamp gun). */
    private CeresLooks ceres;

    /** M9 part C: the line mines and the Minelayer's laying (Level 30). */
    private MineLooks mines;

    /** M9 part C: Level 30's Gilded Gunships and Rail Serpents. */
    private MinefieldLooks minefield;

    /** M9 part D: Level 31's Hornets, Shard Drones (and their air links) and Buzzsaw Drones. */
    private MiningRigLooks rig;
    /** M9 part E: Level 32's Sentinel Towers, Striders and ground targets; built at the first use. */
    private DeepCoreLooks deepCore;
    /** M9 part F: Level 33's Rail Bunkers, prey Talons and their leeches, Claim 114; built at the first use. */
    private LeechFieldLooks leechField;

    /** M8 part F: whether the level is a dark one ({@link DarkLight}): the units' glow-only frames are drawn. */
    private boolean dark;

    private final Sprites sprites;
    private final Files files;
    /** M9 part G: each kind's sprite root (a livery's from its act on, {@link EnemyLooks#root}); its slug by default. */
    private java.util.function.Function<String, String> roots = java.util.function.Function.identity();

    KindLooks(Sprites sprites, Files files) {
        this.sprites = sprites;
        this.files = files;
    }

    /** M9 part G: the kinds' sprite roots (a livery's), before the first look that reads them is built. */
    void roots(java.util.function.Function<String, String> roots) {
        this.roots = roots;
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

    /** M9 part B: Level 29's units' looks for the sortie's enemy kinds, built once. */
    CeresLooks ceres(Sortie sortie) {
        if (ceres == null) {
            ceres = new CeresLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return ceres;
    }

    /** M9 part C: the line mines' looks for the sortie's enemy kinds, built once. */
    MineLooks mines(Sortie sortie) {
        if (mines == null) {
            mines = new MineLooks(sprites, sortie.enemyKinds());
        }
        return mines;
    }

    /** M9 part C: Level 30's units' looks for the sortie's enemy kinds, built once. */
    MinefieldLooks minefield(Sortie sortie) {
        if (minefield == null) {
            minefield = new MinefieldLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return minefield;
    }

    /** M9 part D: Level 31's units' looks for the sortie's enemy kinds, built once. */
    MiningRigLooks rig(Sortie sortie) {
        if (rig == null) {
            rig = new MiningRigLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return rig;
    }

    /** M9 part E: Level 32's units' and ground targets' looks for the sortie's enemy kinds, built once. */
    DeepCoreLooks deepCore(Sortie sortie) {
        if (deepCore == null) {
            deepCore = new DeepCoreLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return deepCore;
    }

    /** M9 part F: Level 33's units' and ground target's looks for the sortie's enemy kinds, built once. */
    LeechFieldLooks leechField(Sortie sortie) {
        if (leechField == null) {
            leechField = new LeechFieldLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return leechField;
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
            tanks = new TankLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name), roots);
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
            eels.dark(dark);
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

    /** M8 part D: the Abyss Rays' looks for the sortie's enemy kinds, built once. */
    AbyssRayLooks rays(Sortie sortie) {
        if (rays == null) {
            rays = new AbyssRayLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return rays;
    }

    /** M8 part D: the Barbfins' looks for the sortie's enemy kinds, built once. */
    BarbfinLooks barbfins(Sortie sortie) {
        if (barbfins == null) {
            barbfins = new BarbfinLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return barbfins;
    }

    /** M8 part E: the Sirens' looks for the sortie's enemy kinds, built once. */
    SirenLooks sirens(Sortie sortie) {
        if (sirens == null) {
            sirens = new SirenLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return sirens;
    }

    /** M8 part E: the Spiral Nautili's looks for the sortie's enemy kinds, built once. */
    NautilusLooks nautili(Sortie sortie) {
        if (nautili == null) {
            nautili = new NautilusLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return nautili;
    }

    /** M8 part F: the Glow Anglers' looks for the sortie's enemy kinds, built once. */
    AnglerLooks anglers(Sortie sortie) {
        if (anglers == null) {
            anglers = new AnglerLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return anglers;
    }

    /** M8 part G: the Depth Hunters' looks for the sortie's enemy kinds, built once. */
    HunterLooks hunters(Sortie sortie) {
        if (hunters == null) {
            hunters = new HunterLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return hunters;
    }

    /** M8 part G: the relay beacons' looks for the sortie's enemy kinds, built once. */
    BeaconLooks beacons(Sortie sortie) {
        if (beacons == null) {
            beacons = new BeaconLooks(sprites, sortie.enemyKinds(), name -> LevelRenderer.pivots(files, name));
        }
        return beacons;
    }

    /** M8 part F: a dark level ({@link DarkLight}): the Driftjellies' veins and the eels' resting stripe are drawn. */
    void dark(boolean on) {
        dark = on;
        if (eels != null) {
            eels.dark(on);
        }
    }

    /** M8 part F: whether the level is a dark one. */
    boolean dark() {
        return dark;
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
        if (ceres != null) {
            ceres.restart();
        }
        if (mines != null) {
            mines.restart();
        }
        if (minefield != null) {
            minefield.restart();
        }
        if (rig != null) {
            rig.restart();
        }
        if (deepCore != null) {
            deepCore.restart();
        }
        if (leechField != null) {
            leechField.restart();
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
        if (rays != null) {
            rays.restart();
        }
        if (barbfins != null) {
            barbfins.restart();
        }
        if (sirens != null) {
            sirens.restart();
        }
        if (nautili != null) {
            nautili.restart();
        }
        if (anglers != null) {
            anglers.restart();
        }
        if (hunters != null) {
            hunters.restart();
        }
    }
}
