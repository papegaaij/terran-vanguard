package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import vanguard.sim.AllySpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.HookMode;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.WaveSpec;

/**
 * M9 part D, Level 31's rules for the simulation ({@link UnitRules}, {@link ScriptRules}; design/campaign Level 31): the
 * Buzzsaw Drone's dash (design/enemies/air/buzzsaw-drone), the Shard Drone's air links (design/enemies/space/shard-drone),
 * the Hornet's hatch tell on its aimed homing salvo (design/enemies/air/hornet) and the mining tug's hull escort
 * (design/allies/mining-tug): its sections turned y up, the hook per kind, the unhooked waves by their index.
 */
final class TugRules {
    private TugRules() {}

    /** {@code spec} with the unit's part D components at {@code difficulty}; a unit without them as it is. */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        Optional<EnemyData.Dash> dash = enemy.movement().dash();
        Optional<EnemyData.Attack> link =
                enemy.attacks().stream().filter(TugRules::airLink).findFirst();
        boolean aimedHoming = spec.gun()
                .flatMap(EnemyGun::homing)
                .map(EnemyGun.HomingSpec::aimed)
                .orElse(false);
        if (dash.isEmpty() && link.isEmpty() && !aimedHoming) {
            return spec;
        }
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        EnemySpec out = spec;
        if (dash.isPresent()) {
            EnemyData.Dash d = dash.get();
            Optional<EnemyData.DashChange> change = hook.flatMap(EnemyData.Hook::dash);
            out = out.withDash(Optional.of(new EnemySpec.Dash(
                    Math.toRadians(d.turnRate()),
                    d.chase(),
                    change.flatMap(EnemyData.DashChange::tell).orElse(d.tell()),
                    d.tellSpeed(),
                    change.flatMap(EnemyData.DashChange::speed).orElse(d.speed()),
                    change.flatMap(EnemyData.DashChange::bounces).orElse(d.bounces()),
                    d.slow(),
                    change.flatMap(EnemyData.DashChange::dashes).orElse(d.dashes()))));
        }
        if (link.isPresent()) {
            EnemyData.AirLink air = link.get().airLink().orElseThrow();
            double tell = hook.flatMap(EnemyData.Hook::airLink)
                    .flatMap(EnemyData.AirLinkChange::tell)
                    .orElse(air.tell());
            out = out.withAirLink(Optional.of(new EnemySpec.AirLink(
                    air.band(),
                    UnitRules.bulletDamage(content, link.get().bullet().orElseThrow()),
                    air.cooldown(),
                    tell,
                    air.spacing(),
                    air.ring().map(EnemyData.AirRing::descend).orElse(0.0),
                    air.ring().map(EnemyData.AirRing::follow).orElse(0.0))));
        }
        if (aimedHoming) {
            // The pod hatches glow before a salvo (the Hornet's tell): the gun's tell is the homing's hatch.
            out = out.withGun(
                    out.gun().map(gun -> gun.withTell(gun.homing().orElseThrow().hatchSeconds())));
        }
        return out;
    }

    /** Whether {@code attack} is an air link (the unit's links, {@link EnemySpec#airLink()}), not a gun. */
    static boolean airLink(EnemyData.Attack attack) {
        return attack.pattern().equals(EnemyData.Attack.AIR_LINK);
    }

    /**
     * The mining tug's hull escort at {@code difficulty}: {@code tug} (the whole hull's spec) flying the level's {@code
     * hull} block, its sections from the ally's data (offsets turned y up, each with its HP on the difficulty and the
     * escort's credits), the damage per hooked attack from the ally's {@code damaged_by}, the hook per kind.
     */
    static ObjectiveSpec.Escort hull(
            AlliesData.Ally ally, LevelData.Escort escort, AllySpec tug, Difficulty difficulty) {
        LevelData.Hull hull = escort.hull().orElseThrow();
        List<AlliesData.HullSection> sections = ally.sections().orElseThrow();
        List<Double> hp = hull.hp(difficulty);
        List<ObjectiveSpec.HullSection> out = new ArrayList<>();
        for (int k = 0; k < sections.size(); k++) {
            AlliesData.HullSection section = sections.get(k);
            out.add(new ObjectiveSpec.HullSection(
                    section.name(),
                    section.offset().x(),
                    -section.offset().y(),
                    SimSpecs.hitbox(section.box()),
                    hp.get(k),
                    escort.credits()));
        }
        Map<String, HookMode> modes = new TreeMap<>();
        hull.modes(difficulty).forEach((slug, mode) -> modes.put(slug, mode(mode)));
        AlliesData.DamagedBy damage = ally.damagedBy();
        return ObjectiveSpec.Escort.hull(
                tug,
                new ObjectiveSpec.Hull(
                        SimSpecs.hitbox(ally.size()),
                        PartDRules.station(hull.station()),
                        out,
                        hull.enter(),
                        new ObjectiveSpec.Climb(hull.climb().t(), hull.climb().seconds()),
                        damage.missiles().orElse(0.0),
                        damage.dives().orElse(0.0),
                        damage.links().orElse(0.0),
                        damage.linkCooldown().orElse(1.0),
                        modes));
    }

    /**
     * {@code escort} with its hull's unhooked waves: the indexes in {@code waves} (the level's list on the difficulty)
     * of the waves at the hull block's {@code unhooked} times whose kind it hooks; another escort as it is.
     */
    static ObjectiveSpec.Escort unhooked(
            ObjectiveSpec.Escort escort, LevelData.Escort data, List<WaveSpec> waves, Difficulty difficulty) {
        if (escort.hull().isEmpty()) {
            return escort;
        }
        ObjectiveSpec.Hull hull = escort.hull().get();
        List<Double> times = data.hull().orElseThrow().unhooked(difficulty);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < waves.size(); i++) {
            WaveSpec wave = waves.get(i);
            if (times.contains(wave.t())
                    && hull.targetedBy().containsKey(wave.enemy().slug())) {
                indexes.add(i);
            }
        }
        if (indexes.isEmpty()) {
            return escort;
        }
        return ObjectiveSpec.Escort.hull(escort.ally(), hull.withUnhookedWaves(indexes));
    }

    private static HookMode mode(AlliesBlock.HookMode mode) {
        return switch (mode) {
            case ALWAYS -> HookMode.ALWAYS;
            case NONE -> HookMode.NONE;
            default -> throw new IllegalArgumentException("a hull's hook is always or none: " + mode);
        };
    }
}
