package vanguard.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import vanguard.content.AlliesData;
import vanguard.content.Content;
import vanguard.content.EnemyData;
import vanguard.content.LevelData;

/**
 * Which atlases a sprite goes in (design/art-direction/production, Budgets): the game-wide sprites
 * go in the shared pages ({@code sprites}), which the game keeps loaded; a sprite only levels use
 * goes in the unit atlas ({@code level-NN}) of every level that uses it, duplicated in the build
 * output (user decision D10 = a, M5 part C), so the shared pages never grow with the levels' units
 * and a level finds all its units in its own atlas. The levels' use is derived from their data: the
 * enemies of their waves, ground targets, set pieces and boss (and the enemies those spawn), the
 * ground targets' looks, the escorted ally and the level's features the game draws with sprites of
 * its own (cranes, debris, sleds, rocks, the darkness's flares, the tows' lifeboat, pod and cable, the
 * collapse's dust puffs, the scripted loss's glow and lance, the weather's looks and sondes, the root
 * bursts' cracks, dust and roots; M7 part D: the ground allies' sprite sets and their pip, a trigger look's
 * contents, the militia flak's puffs; M7 part F: the furnace vents' grates and fire, the gantries and cradles of the
 * up-facing nests, a trigger look's surround, a foundry's ingots and rovers; M7 part G: the travelling ridge). A
 * sprite belongs to a root when its name is the root or starts with the root and a hyphen
 * ({@code leviathan} owns {@code leviathan-fin-left}). The shared roots are the weapons, the specials
 * and the game's own effects ({@link #GAME_WIDE}); an enemy no level uses yet goes in its first level's
 * unit atlas (its art can land before its level's data), as does an ally with a {@code first_level}; a sprite no root claims fails the build.
 */
final class SpriteUse {
    /** The shared atlas, always loaded. */
    static final String SHARED = "sprites";

    /**
     * The sprites the game draws in any level whatever its data: the ship and its engine flame, Rook's
     * craft with its flame and eject pod (he flies wherever the escort slot does), the pickups,
     * explosions and weapon effects, the wing-mount pods, the enemies' bullets and the loot targets'
     * beacon and glint; M5 part E: the generic water effects ({@code water-splash}, {@code
     * water-ripple}, {@code water-collar-*}, {@code water-foam-strip}, {@code water-wake}), drawn in every
     * level over water; M8 part A: the Bulwark heavy drone ({@code bulwark}, {@code bulwark-*}), flown in the escort
     * slot wherever Rook could fly.
     */
    static final List<String> GAME_WIDE = List.of(
            "ship",
            "engine-flame",
            "rook",
            "pickup",
            "explosion",
            "ballistic",
            "explosive",
            "launcher",
            "pulse",
            "pod",
            "orb",
            "needle",
            "beacon",
            "glint",
            "water",
            "bulwark");

    /**
     * The sprites an enemy's attack pattern draws besides the enemy (the game's names). M7 part C: a
     * {@code link} (the Tendril Anchor's) draws the tendril between a pair, {@code tendril_*} and its
     * {@code tendril-dormant}, {@code -flare}, {@code -wither}, {@code -end} and {@code -burn} frames.
     */
    private static final Map<String, String> PATTERN_ROOTS =
            Map.of("mortar", "mortar", "mine", "spore-mine", "link", "tendril");

    /**
     * M7 part C: the look of a level with {@code terrain} (Level 16, CanyonLooks): the wall contact's and
     * the stopped shots' spark, {@code wall-spark_*} and {@code wall-spark-dust_*} (tools/art/canyon_units.py).
     */
    static final String TERRAIN_ROOT = "wall-spark";

    /** The sprites of a level's tows: {@code lifeboat}, {@code lifeboat-pod}, {@code lifeboat-cable} and their {@code -glow}s. */
    static final String TOW_ROOT = "lifeboat";

    /** The dust puffs of a level's collapse: {@code collapse-puff_0..7} (Level 09's arcology). */
    static final String COLLAPSE_ROOT = "collapse-puff";

    /**
     * The look of an escort's scripted loss (M5 part D, Level 10's Lifeline Three, LossLooks): the light
     * in the cloud deck, {@code cloud-glow_0..5}, and the lance with its flash, {@code lance_*} and
     * {@code lance-flash_*}.
     */
    static final List<String> LOSS_ROOTS = List.of("cloud-glow", "lance");

    /**
     * The weather looks of a level with a {@code weather} block (M5 part F, Level 12's storm, WeatherLooks):
     * the rain streaks, the gust telegraph's arrow and streaks, the lightning bolts, the cloud lit from
     * inside, the glow of a hidden low-air body and the weather sondes with their collect pop, all
     * {@code weather-*} (tools/art/l12_props.py).
     */
    static final String WEATHER_ROOT = "weather";

    /**
     * M5 part H: the look of a level's root bursts (Level 14, RootBurstLooks): the cracking paving's decal,
     * the dust puffs and the root erupting into its crater, {@code root-burst-decal_*}, {@code
     * root-burst-puff_*} and {@code root-burst-root_*} (tools/art/root_burst.py); the growths are their
     * enemies' own sprites.
     */
    static final String ROOT_BURST_ROOT = "root-burst";

    /**
     * M7 part D: the HUD pip of a level's ground allies (Level 17's DOMES tracker, MissionPanel's {@code GROUND_PIP}:
     * tools/art/siege_units.py's {@code dome-pip}, used where the ally has no {@code <slug>-pip}).
     */
    static final String GROUND_PIP = "dome-pip";

    /**
     * M7 part D: a level's militia flak (its presentation key {@code flak}, Level 17's spaceport guns) is drawn with
     * the escort frigate's flak puff, {@code escort-frigate-flak_*} (Level 11).
     */
    static final String FLAK_ROOT = "escort-frigate-flak";

    /**
     * M7 part D: a trigger look over contents with sprites of their own (LevelRenderer's {@code TRIGGER_CONTENTS}):
     * Level 17's collapsed greenhouse frame over the seed-vault capsule, {@code seed-capsule} and its {@code -glow}.
     */
    static final Map<String, String> TRIGGER_CONTENTS = Map.of("greenhouse-frame", "seed-capsule");

    /**
     * Round 44 G.c: a trigger look standing on a ground decal of its own (LevelRenderer's {@code TRIGGER_PADS}): Level
     * 17's greenhouse frame on its scorched pad, {@code greenhouse-pad}.
     */
    static final Map<String, String> TRIGGER_PADS = Map.of("greenhouse-frame", "greenhouse-pad");

    /**
     * M7 part F: a ground target look standing inside another sprite of its own (the game's {@code
     * FoundryLooks.TRIGGER_SURROUNDS}): Level 19's payroll safe inside the shift office.
     */
    static final Map<String, String> TRIGGER_SURROUNDS = Map.of("payroll-safe", "shift-office");

    /**
     * M7 part F: a level with furnace {@code vents} draws their grates, heat glow, flame and smoke, {@code
     * furnace-vent*} (tools/art/l19_props.py; the game's FoundryLooks); M8 part B: the vents' {@code look} names the
     * root (Level 22's {@code ice-geyser*}, tools/art/l22_props.py), the furnace vent if not given.
     */
    static final String VENT_ROOT = "furnace-vent";

    /** M8 part B: the ice geysers' crack, glow, steam and residue (the vents' {@code look: ice-geyser}). */
    static final String GEYSER_ROOT = "ice-geyser";

    /**
     * M7 part F: a level with up-facing nests ({@code arc: up}) draws the steel truss gantry under them and each
     * nest's cradle, {@code gantry} and {@code gantry-cradle}.
     */
    static final String GANTRY_ROOT = "gantry";

    /**
     * M7 part F: a level with conveyor {@code belts} is a foundry: the ingot stacks and the pallets riding its last
     * belt run, and the crews' rovers leaving its smelter tower ({@code ingot-*}, {@code crew-rover*}).
     */
    static final List<String> FOUNDRY_ROOTS = List.of("ingot", "crew-rover");

    /**
     * M7 part G: a level with a {@code ridge} key draws the travelling ridge (Level 20's, the game's RidgeLooks; the
     * Dust Colossus's body, Level 21's ridge pass and boss reuse it), {@code colossus-ridge*} (tools/art/l20_props.py;
     * round 43 Q.4: the key, no longer the {@code basin-rumble} ambience layer).
     */
    static final String RIDGE_ROOT = "colossus-ridge";

    /** The look of a destructible ground target that names none (Level 01's cargo container). */
    private static final String DEFAULT_LOOK = "cargo-container";

    private SpriteUse() {}

    /**
     * The atlases of every sprite name: {@link #SHARED} alone, or the {@code level-NN} unit atlas of
     * every level that uses it.
     */
    static Map<String, Set<String>> atlases(Content content, Collection<String> sprites) {
        Set<String> shared = new TreeSet<>(GAME_WIDE);
        content.weapons().keySet().forEach(shared::add);
        content.specials().specials().forEach(special -> shared.add(slug(special.name())));
        Map<String, Set<String>> levels = new TreeMap<>();
        content.levels().forEach((key, level) -> levels.put(atlasOf(key), roots(content, level)));
        // An enemy no level's data uses yet (its art lands before its level) goes in the unit atlas
        // of its stat block's first level, where that level's waves will claim it.
        Set<String> used = new TreeSet<>();
        levels.values().forEach(used::addAll);
        content.enemies().forEach((slug, enemy) -> {
            if (!used.contains(slug)) {
                levels.computeIfAbsent(atlasOf(enemy.firstLevel()), level -> new TreeSet<>())
                        .add(slug);
            }
        });
        // An ally likewise, by its optional first_level (Level 10's shuttle before the level's data).
        content.allies().allies().forEach((slug, ally) -> {
            if (!used.contains(slug)) {
                ally.firstLevel().ifPresent(first -> levels.computeIfAbsent(atlasOf(first), level -> new TreeSet<>())
                        .add(slug));
            }
        });
        Map<String, Set<String>> atlases = new TreeMap<>();
        List<String> orphans = new ArrayList<>();
        for (String sprite : sprites) {
            Set<String> users = new TreeSet<>();
            levels.forEach((level, roots) -> {
                if (claims(roots, sprite)) {
                    users.add(level);
                }
            });
            if (claims(shared, sprite)) {
                atlases.put(sprite, Set.of(SHARED));
            } else if (!users.isEmpty()) {
                atlases.put(sprite, Collections.unmodifiableSet(users));
            } else {
                orphans.add(sprite);
            }
        }
        if (!orphans.isEmpty()) {
            throw new IllegalStateException("sprites no level, weapon, special or game effect uses (see "
                    + SpriteUse.class.getSimpleName() + "): " + orphans);
        }
        return atlases;
    }

    /** The unit atlas of a level key: {@code level-NN}. */
    static String atlasOf(String levelKey) {
        return atlasOf(Content.levelNumber(levelKey));
    }

    /** The unit atlas of level {@code number}: {@code level-NN}. */
    static String atlasOf(int number) {
        return String.format(Locale.ROOT, "level-%02d", number);
    }

    /** The sprite roots a level uses. */
    static Set<String> roots(Content content, LevelData level) {
        Set<String> enemies = new TreeSet<>();
        level.waves().forEach(wave -> wave.groupList().forEach(group -> enemies.add(group.enemy())));
        level.setPieces().ifPresent(pieces -> pieces.forEach(piece -> enemies.add(piece.enemy())));
        level.boss().ifPresent(boss -> enemies.add(boss.enemy()));
        level.bursts().forEach(burst -> enemies.addAll(burst.growths())); // M5 part H: the root bursts' growths
        Set<String> roots = new TreeSet<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            target.enemy().ifPresent(enemies::add);
            if (target.enemy().isEmpty() && target.hits().isEmpty()) {
                roots.add(target.sprite().orElse(DEFAULT_LOOK));
            } else if (target.enemy().isEmpty()) {
                // A trigger with a look of its own (Level 06's survey cache and terminal); others
                // are drawn as the beacon or the level's trigger light.
                target.sprite().ifPresent(roots::add);
            }
            // M7 part D: a look over contents with sprites of their own (Level 17's seed-vault capsule).
            target.sprite().map(TRIGGER_CONTENTS::get).ifPresent(roots::add);
            target.sprite().map(TRIGGER_PADS::get).ifPresent(roots::add);
            // M7 part F: a look inside a surround of its own (Level 19's payroll safe in the shift office).
            target.sprite().map(TRIGGER_SURROUNDS::get).ifPresent(roots::add);
            if (target.arc().isPresent()) {
                roots.add(GANTRY_ROOT);
            }
        }
        List<String> open = new ArrayList<>(enemies);
        while (!open.isEmpty()) {
            EnemyData enemy = content.enemy(open.removeLast());
            for (EnemyData.Attack attack : enemy.attacks()) {
                attack.spawn().map(EnemyData.Spawn::enemy).filter(enemies::add).ifPresent(open::add);
                String root = PATTERN_ROOTS.get(attack.pattern());
                if (root != null) {
                    roots.add(root);
                }
            }
        }
        roots.addAll(enemies);
        level.objectives().escort().ifPresent(escort -> roots.add(escort.ally()));
        // M5 part G: a defend primary's ally (Level 13's relay) and the supply drones carrying the drops.
        level.objectives().defend().ifPresent(defend -> roots.add(defend.ally()));
        level.released().forEach(drop -> drop.sprite().ifPresent(roots::add));
        level.cranes().ifPresent(cranes -> roots.add("crane-four"));
        level.debris().ifPresent(debris -> debris.chunks().keySet().forEach(chunk -> roots.add("debris-" + chunk)));
        level.sleds().ifPresent(sleds -> {
            roots.add("sled");
            roots.add("ore-canister");
        });
        level.rocks().ifPresent(rocks -> roots.add("rock"));
        // M5 part C: a collapse's dust is drawn with its puff sprites (CollapseLooks).
        level.collapse().ifPresent(collapse -> roots.add(COLLAPSE_ROOT));
        // M5 part D: a scripted loss is drawn with the glow and the lance (LossLooks).
        level.objectives().escort().flatMap(LevelData.Escort::scriptedLoss).ifPresent(loss -> roots.addAll(LOSS_ROOTS));
        level.darkness().ifPresent(darkness -> {
            roots.add("flare");
            roots.add("headlight");
        });
        // A tow is drawn as the lifeboat, its cargo pod and the cable (TowLooks), whatever its data.
        level.tows().filter(tows -> !tows.isEmpty()).ifPresent(tows -> roots.add(TOW_ROOT));
        // M5 part F: a level with weather draws its rain, gusts, lightning, hidden bodies' glow and sondes.
        level.weather().ifPresent(weather -> roots.add(WEATHER_ROOT));
        // M7 part C: a level with terrain draws the sparks of its rock.
        level.terrain().ifPresent(terrain -> roots.add(TERRAIN_ROOT));
        // M7 part D: a level's ground allies (Level 17's domes) in their sprite sets, the DOMES tracker's pip, and
        // its militia flak with the frigate's puffs.
        level.allies().ifPresent(allies -> {
            allies.units().forEach(unit -> {
                roots.add(unit.ally());
                AlliesData.Ally ally = content.allies().allies().get(unit.ally());
                roots.add(ally == null ? unit.sprite().orElse(unit.ally()) : unit.spriteName(ally));
            });
            roots.add(GROUND_PIP);
        });
        level.flak().ifPresent(flak -> roots.add(FLAK_ROOT));
        // M7 part F: the furnace vents' looks, a foundry's ingots and rovers.
        level.vents()
                .ifPresent(vents -> roots.add(GEYSER_ROOT.equals(vents.lookOrDefault()) ? GEYSER_ROOT : VENT_ROOT));
        level.belts().ifPresent(belts -> roots.addAll(FOUNDRY_ROOTS));
        // M7 part G: the travelling ridge (its ridge key).
        level.ridge().ifPresent(ridge -> roots.add(RIDGE_ROOT));
        // M5 part H: a level with root bursts draws their cracks, dust and roots.
        if (!level.bursts().isEmpty()) {
            roots.add(ROOT_BURST_ROOT);
        }
        return roots;
    }

    private static boolean claims(Set<String> roots, String sprite) {
        return roots.stream().anyMatch(root -> sprite.equals(root) || sprite.startsWith(root + "-"));
    }

    /** A display name as a sprite root: {@code Airstrike} is {@code airstrike}. */
    private static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replace(' ', '-');
    }
}
