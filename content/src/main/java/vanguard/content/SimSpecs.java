package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.Armament;
import vanguard.sim.BulwarkSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Magnet;
import vanguard.sim.MediumSpec;
import vanguard.sim.NaniteSpec;
import vanguard.sim.PickupType;
import vanguard.sim.Road;
import vanguard.sim.Rules;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SpecialLimits;
import vanguard.sim.SpecialSpec;
import vanguard.sim.ThrusterSpec;
import vanguard.sim.WeaponSpec;
import vanguard.sim.WingmanSpec;

/**
 * Builds the simulation's specs from the loaded content at one difficulty. The dependency points
 * this way ({@code content → sim}) so that the simulation knows nothing about data files, YAML or
 * Jackson: it defines the records it needs, and this class fills them, applying the difficulty
 * levers (design/systems/difficulty) and the level's easy/hard changes on the way.
 *
 * <p>This class is the facade: the mapping lives in the package's rules classes, one per family —
 * {@link LoadoutRules} (the fit, utility modules, specials, Rook), {@link WeaponRules} (the player's
 * weapons), {@link RewardRules} (bullet budget, pickups, scoring, credits), {@link ScriptRules} (a
 * level's script), {@link WaveRules} (air waves), {@link GroundRules} (ground units, destructibles,
 * cranes), {@link BossRules} (set pieces and bosses), {@link UnitRules} (an enemy unit) and {@link
 * ChainRules} (a segment chain) — next to the parts' rules ({@code MarsRules}, {@code TerrainRules},
 * {@code SiegeRules}, {@code StormRules}, {@code FoundryRules}, …) and (M8 part A) {@link WaterRules} (the medium and the special limits);
 * M8 part F's {@link DarkRules}: the Glow Angler's lunge, Rook's scripted loss and a field's centre carrier.
 */
public final class SimSpecs {
    /** The starter front gun's slug. */
    public static final String PULSE_CANNON = "pulse-cannon";

    /** The utility modules the simulation flies (design/player/systems); M8 part A: the hull and the nanites too. */
    public static final String PICKUP_MAGNET = "Pickup magnet";

    public static final String TARGETING_COMPUTER = "Targeting computer";

    public static final String SALVAGE_SCANNER = "Salvage scanner";

    /** M7 part A: the Evasive thrusters (Act 3), the dash. */
    public static final String EVASIVE_THRUSTERS = "Evasive thrusters";

    /**
     * The specials the simulation flies (design/player/specials): the Airstrike and the Smart Bomb, and
     * (M7 part A) the EMP Burst, the Orbital Lance and the Shield Overcharge (a cooldown special).
     */
    public static final String AIRSTRIKE = "Airstrike";

    public static final String SMART_BOMB = "Smart Bomb";

    public static final String EMP_BURST = "EMP Burst";

    public static final String ORBITAL_LANCE = "Orbital Lance";

    public static final String SHIELD_OVERCHARGE = "Shield Overcharge";

    /** M8 part A: the Sonar Pulse and the Decoy Flares (design/player/specials). */
    public static final String SONAR_PULSE = "Sonar Pulse";

    public static final String DECOY_FLARES = "Decoy Flares";

    /** M8 part A: the Pressure hull and the Auto-repair nanites (design/player/systems). */
    public static final String PRESSURE_HULL = "Pressure hull";

    public static final String AUTO_REPAIR_NANITES = "Auto-repair nanites";

    private SimSpecs() {}

    /** A weapon fitted in one of the Stormhawk's weapon slots at an upgrade level (1–5). */
    public record FittedWeapon(Armament.Slot slot, String weapon, int level) {}

    /**
     * The starting fit: the hull with the starter engine, the Pulse Cannon at L1, the starter
     * shield (with the difficulty's regen lever and the spare-power bonus of the starter generator)
     * and plating.
     */
    public static Loadout starterLoadout(Content content, Difficulty difficulty) {
        return LoadoutRules.starterLoadout(content, difficulty);
    }

    /**
     * A fit of the parts the simulation flies: the hull with an engine, the weapons at their
     * levels, a shield (with the difficulty's regen lever and the bonus of {@code sparePower} MW,
     * design/player/generator) and a plating, each by its name in its data file.
     */
    public static Loadout loadout(
            Content content,
            String engine,
            List<FittedWeapon> weapons,
            String shield,
            String plating,
            double sparePower,
            Difficulty difficulty) {
        return LoadoutRules.loadout(content, engine, weapons, shield, plating, sparePower, difficulty);
    }

    /**
     * As {@link #loadout(Content, String, List, String, String, double, Difficulty)}, the weapons'
     * one turn rate (a homing missile's turn, a turret's slew) raised by {@code turnBonus}: the
     * fitted Targeting computer's share (design/player/systems), 0 without one. Only the
     * Stormhawk's own weapons: Rook's guns are built by {@link #wingman} at their base turn.
     */
    public static Loadout loadout(
            Content content,
            String engine,
            List<FittedWeapon> weapons,
            String shield,
            String plating,
            double sparePower,
            Difficulty difficulty,
            double turnBonus) {
        return LoadoutRules.loadout(content, engine, weapons, shield, plating, sparePower, difficulty, turnBonus);
    }

    /**
     * M9 part A (design/player/generator, Enemy drain effects): the power budget in flight of a fit ({@link
     * Loadout#withPower}): the generator's {@code output}, the fitted {@code load} (MW, the shield's draw in it), the
     * fitted {@code shield} by its name; the drain's numbers from the generator data.
     */
    public static Loadout.Power power(
            Content content, double output, double load, String shield, Difficulty difficulty) {
        return LoadoutRules.power(content, output, load, shield, difficulty);
    }

    /** The shield regen bonus of {@code sparePower} spare MW (design/player/generator): +10 % per MW, at most +50 %. */
    public static double regenBonus(Content content, double sparePower) {
        return LoadoutRules.regenBonus(content, sparePower);
    }

    /** Whether the simulation flies the weapon (see {@link WeaponRules#flies}). */
    public static boolean flies(Content content, String weapon) {
        return WeaponRules.flies(content, weapon);
    }

    /**
     * M7 part A: a weapon fitted in a slot at an upgrade level as the simulation's mount, with its
     * overdrive; the turn rate raised by {@code turnBonus} (see {@link WeaponRules#mount}).
     */
    public static Armament.Mount mount(
            Content content, Armament.Slot slot, String slug, int upgradeLevel, double turnBonus) {
        return WeaponRules.mount(content, slot, slug, upgradeLevel, turnBonus);
    }

    /** A weapon in a slot at an upgrade level, 6 being its overdrive pattern (see {@link WeaponRules#weapon}). */
    static WeaponSpec weapon(Content content, Armament.Slot slot, String slug, int upgradeLevel) {
        return WeaponRules.weapon(content, slot, slug, upgradeLevel);
    }

    /** As {@link #weapon(Content, Armament.Slot, String, int)}, its turn rate raised by {@code turnBonus} (a share). */
    static WeaponSpec weapon(Content content, Armament.Slot slot, String slug, int upgradeLevel, double turnBonus) {
        return WeaponRules.weapon(content, slot, slug, upgradeLevel, turnBonus);
    }

    /** Whether the simulation flies the utility module of this name. */
    public static boolean fliesUtility(String name) {
        return LoadoutRules.fliesUtility(name);
    }

    /** M7 part A: the Evasive thrusters' dash from design/player/systems/data.yaml. */
    public static ThrusterSpec thrusters(Content content) {
        return LoadoutRules.thrusters(content);
    }

    /** The Targeting computer's numbers from design/player/systems/data.yaml. */
    public static SystemsData.Targeting targeting(Content content) {
        return LoadoutRules.targeting(content);
    }

    /** The Salvage scanner's credit bonus at {@code level} (1–2): the share added to salvage and hidden crates. */
    public static double salvageBonus(Content content, int level) {
        return LoadoutRules.salvageBonus(content, level);
    }

    /** The Pickup magnet at {@code level} (1–3) from design/player/systems/data.yaml. */
    public static Magnet magnet(Content content, int level) {
        return LoadoutRules.magnet(content, level);
    }

    /**
     * Rook in the escort slot (design/player/wingmen) with his gun {@code gunId} at {@code level}
     * (1–5), on {@code side}, starting the level with {@code armour} (see {@link LoadoutRules#wingman}).
     */
    public static WingmanSpec wingman(Content content, String gunId, int level, WingmanSpec.Side side, double armour) {
        return LoadoutRules.wingman(content, gunId, level, side, armour);
    }

    /**
     * M8 part A: the Bulwark heavy drone in the escort slot (design/player/wingmen) on {@code side}, starting the level
     * with {@code armour} (see {@link LoadoutRules#bulwark}).
     */
    public static BulwarkSpec bulwark(Content content, WingmanSpec.Side side, double armour) {
        return LoadoutRules.bulwark(content, side, armour);
    }

    /** M8 part A: the Auto-repair nanites at {@code level} (1–2) from design/player/systems/data.yaml. */
    public static NaniteSpec nanites(Content content, int level) {
        return LoadoutRules.nanites(content, level);
    }

    /** M8 part A: whether the Pressure hull removes the water's drag on the ship (design/player/systems/data.yaml). */
    public static boolean pressureHull(Content content) {
        return LoadoutRules.pressureHull(content);
    }

    /** M8 part A: the under-water rules (design/world/europa/data.yaml, see {@link WaterRules#rules}). */
    public static MediumSpec.Rules waterRules(Content content) {
        return WaterRules.rules(content);
    }

    /** M8 part A: the level's medium with its waterlines at its sections' starts (see {@link WaterRules#medium}). */
    public static MediumSpec medium(Content content, LevelData level) {
        return WaterRules.medium(content, level);
    }

    /**
     * M8 part A, the debug option {@code --medium underwater[@<s>]}: under water from {@code atSeconds} on the level
     * clock (see {@link WaterRules#underwaterFrom}); give it to a level's script with {@code toBuilder().medium(..)}.
     */
    public static MediumSpec underwaterFrom(Content content, double atSeconds) {
        return WaterRules.underwaterFrom(content, atSeconds);
    }

    /** M8 part A: the level's and its sections' forbidden specials (see {@link WaterRules#specialLimits}). */
    public static SpecialLimits specialLimits(Content content, LevelData level) {
        return WaterRules.specialLimits(content, level);
    }

    /**
     * Whether the simulation flies the special of this name; M8 part A: the Sonar Pulse and the Decoy Flares too (the
     * flares decoy the enemies' homing missiles).
     */
    public static boolean fliesSpecial(String name) {
        return LoadoutRules.fliesSpecial(name);
    }

    /**
     * The special of this name with {@code charges} carried into the level (at most its most); it must
     * fly (see {@link LoadoutRules#special}).
     */
    public static SpecialSpec special(Content content, String name, int charges) {
        return LoadoutRules.special(content, name, charges);
    }

    /** The rules that do not depend on the level's script: bullet budget, pickups, scoring and credits. */
    public static Rules rules(Content content, String levelKey, Difficulty difficulty) {
        return RewardRules.rules(content, levelKey, difficulty);
    }

    /** The level's script at {@code difficulty}; {@code levelKey} as in {@link Content#level(String)}. */
    public static LevelScript level(Content content, String levelKey, Difficulty difficulty) {
        return ScriptRules.level(content, levelKey, difficulty);
    }

    /**
     * The level's road for the simulation: each point's {@code t} turned into the ground distance
     * that passes the middle of the screen then.
     */
    public static Road road(LevelData level, LevelData.Road road) {
        return ScriptRules.road(level, road);
    }

    /**
     * M5 part E: the groups of the level's destructibles (not the objectives' groups of enemies), in
     * the order they first appear among its ground targets.
     */
    static List<String> destructibleGroups(LevelData level) {
        return GroundRules.destructibleGroups(level);
    }

    /** M5 part E: a field's default spacing for units of hit box width {@code width}: 1.5 × it. */
    static double fieldSpacing(double width) {
        return WaveRules.fieldSpacing(width);
    }

    /** A set piece at {@code difficulty} (design/enemies/space/leviathan; see {@link BossRules#setPiece}). */
    public static SetPieceSpec setPiece(Content content, LevelData.SetPieceData piece, Difficulty difficulty) {
        return BossRules.setPiece(content, piece, difficulty);
    }

    /**
     * The level's boss at {@code difficulty} (design/enemies/bosses): a set piece with its parts, its
     * chains, its attacks and its phases, arriving where the level places it (see {@link BossRules#boss}).
     */
    public static SetPieceSpec boss(Content content, LevelData.BossPlacement placement, Difficulty difficulty) {
        return BossRules.boss(content, placement, difficulty);
    }

    /** An enemy at {@code difficulty} outside a level (see {@link UnitRules#enemy}). */
    public static EnemySpec enemy(
            Content content, String slug, Difficulty difficulty, Optional<LevelData.EnemyChange> change) {
        return UnitRules.enemy(content, slug, difficulty, change);
    }

    /**
     * As {@link #enemy(Content, String, Difficulty, Optional)} in Level {@code level} of Act {@code
     * act}, with the act HP factor there (see {@link #actHpFactor}).
     */
    public static EnemySpec enemy(
            Content content,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            int act,
            int level) {
        return UnitRules.enemy(content, slug, difficulty, change, act, level);
    }

    /** The act HP factor of a unit in Level {@code level} of Act {@code act} (see {@link UnitRules#actHpFactor}). */
    public static double actHpFactor(Content content, String slug, int act, int level) {
        return UnitRules.actHpFactor(content, slug, act, level);
    }

    /** M5 part E: whether {@code attack} is a proximity ring (a {@code ring} fired within a reach, the Driftjelly's). */
    static boolean proximityRing(EnemyData.Attack attack) {
        return UnitRules.proximityRing(attack);
    }

    static Hitbox hitbox(Size size) {
        return new Hitbox(size.width(), size.height());
    }

    static PickupType pickup(Pickup pickup) {
        return switch (pickup) {
            case SMALL_SALVAGE -> PickupType.SMALL_SALVAGE;
            case MEDIUM_SALVAGE -> PickupType.MEDIUM_SALVAGE;
            case LARGE_SALVAGE -> PickupType.LARGE_SALVAGE;
            case OVERDRIVE -> PickupType.OVERDRIVE;
            case SHIELD_CELL -> PickupType.SHIELD_CELL;
            case ARMOUR_PATCH -> PickupType.ARMOUR_PATCH;
            case SPECIAL_CHARGE -> PickupType.SPECIAL_CHARGE;
            case DATA_CORE -> PickupType.DATA_CORE;
            // M9 part G (Level 34): the Helix core, a group's story pickup.
            case STORY -> PickupType.STORY;
            default -> throw new IllegalArgumentException("the pickup " + pickup + " is not implemented yet");
        };
    }
}
