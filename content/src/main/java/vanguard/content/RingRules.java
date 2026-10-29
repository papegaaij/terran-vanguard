package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.RingSpec;

/**
 * M9 part H (design/enemies/bosses/iron-sovereign, design/campaign Level 35): a ringed boss's keys for the simulation
 * ({@link SimSpecs}, through {@link BossRules}): its {@code boss.rings}, fire arc, times and core as a {@link RingSpec}
 * at a difficulty (the parts and attacks by their indexes; the hook {@code ring_arena}'s speed factor and fire arc), the
 * battery attacks' interval (the hook's battery factor instead of the fire-rate lever), a boss's rail line from a ground
 * unit's {@code laser-line}, the loader's resolving of an attack's {@code from} and a level's mass-relay flash.
 */
final class RingRules {
    private RingRules() {}

    /**
     * {@code enemy} with its {@code from} attacks taken from their ground units in {@code enemies} (the loader's step
     * before the content is built); each problem (an unknown unit, an ambiguous or missing attack, a pattern that is not
     * the unit's, a merged attack that breaks its rules) is added to {@code problems} with the file.
     */
    static EnemyData resolve(String file, EnemyData enemy, Map<String, EnemyData> enemies, List<String> problems) {
        if (enemy.attacks().stream().noneMatch(RingRules::shell)) {
            return enemy;
        }
        List<EnemyData.Attack> resolved = new ArrayList<>();
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            if (!shell(attack)) {
                resolved.add(attack);
                continue;
            }
            String field = "design/" + file + ": attacks[" + i + "].from: ";
            RingBlock.From from = attack.from().orElseThrow();
            EnemyData unit = enemies.get(from.unit());
            if (unit == null || unit.boss().isPresent()) {
                problems.add(field + "no ground unit '" + from.unit() + "'");
                resolved.add(attack);
                continue;
            }
            Optional<EnemyData.Attack> own = from.attack()
                    .flatMap(unit::attack)
                    .or(() -> from.attack().isEmpty() && unit.attacks().size() == 1
                            ? Optional.of(unit.attacks().getFirst())
                            : Optional.empty());
            if (own.isEmpty()) {
                problems.add(field
                        + from.attack()
                                .map(name -> "'" + from.unit() + "' has no attack '" + name + "'")
                                .orElse("name the attack of '" + from.unit() + "' (it has "
                                        + unit.attacks().size() + ")"));
                resolved.add(attack);
                continue;
            }
            if (!own.get().pattern().equals(attack.pattern())) {
                problems.add(
                        field + "'" + from.unit() + "' fires a " + own.get().pattern() + ", not a " + attack.pattern());
                resolved.add(attack);
                continue;
            }
            try {
                resolved.add(merge(attack, own.get()));
            } catch (IllegalArgumentException e) {
                problems.add(field + e.getMessage());
                resolved.add(attack);
            }
        }
        return enemy.withAttacks(resolved);
    }

    /** Whether {@code attack} is written as a shell to be taken from a unit ({@code from}, no bullet of its own). */
    static boolean shell(EnemyData.Attack attack) {
        return attack.from().isPresent() && attack.bullet().isEmpty();
    }

    /**
     * The unit's attack {@code own} as the boss's {@code shell}: the boss's name, interval (the unit's if not given),
     * first shot, staggering, turns, parts and difficulties; a sweep's beam at the {@code from}'s length and without the
     * unit's full turn (a battery sweeps its arc only).
     */
    private static EnemyData.Attack merge(EnemyData.Attack shell, EnemyData.Attack own) {
        Optional<EnemyData.Sweep> sweep = own.sweep()
                .map(s -> new EnemyData.Sweep(
                        s.arc(),
                        s.duration(),
                        s.telegraph(),
                        shell.from().flatMap(RingBlock.From::length).orElse(s.length()),
                        s.width(),
                        s.origin(),
                        s.lead(),
                        Optional.empty()));
        return new EnemyData.Attack(
                own.pattern(),
                shell.name(),
                own.bullet(),
                shell.interval().or(own::interval),
                own.speed(),
                shell.firstShotDelay().or(own::firstShotDelay),
                own.count(),
                own.spread(),
                own.turnRate(),
                own.arc(),
                own.mine(),
                own.aim(),
                own.away(),
                own.spawn(),
                own.mortar(),
                own.burst(),
                own.burstGap(),
                shell.rotate(),
                own.arms(),
                own.duration(),
                sweep,
                own.stagger(),
                own.pounce(),
                own.within(),
                own.rate(),
                shell.staggered(),
                own.link(),
                own.pores(),
                shell.only(),
                own.aura(),
                shell.aimTurns(),
                own.whileState(),
                own.trigger(),
                own.tell(),
                own.reaim(),
                shell.parts(),
                own.homing(),
                own.slow(),
                own.line(),
                own.airLink(),
                shell.from());
    }

    /** The hook of the unit a {@code from} attack was taken from, at {@code difficulty}; the boss's own otherwise. */
    static Optional<EnemyData.Hook> attackHook(
            Content content, EnemyData.Attack attack, Optional<EnemyData.Hook> bossHook, Difficulty difficulty) {
        return attack.from()
                .map(from -> UnitRules.hook(content.enemy(from.unit()), difficulty))
                .orElse(bossHook);
    }

    /** Whether attack {@code name} is a ring's battery attack of {@code enemy}. */
    static boolean battery(EnemyData enemy, String name) {
        return enemy.boss()
                .flatMap(EnemyData.BossData::rings)
                .map(rings -> rings.stream()
                        .anyMatch(ring -> ring.batteries().attack().equals(name)))
                .orElse(false);
    }

    /**
     * A boss attack's interval at {@code difficulty}: the hook's authored {@code attacks.<name>.interval} as it is; a
     * battery's interval times the hook's {@code ring_arena.interval} factor; otherwise by the fire-rate lever.
     */
    static double interval(
            Content content,
            EnemyData enemy,
            EnemyData.Attack attack,
            Optional<EnemyData.Hook> hook,
            Difficulty difficulty) {
        String name = attack.name().orElseThrow();
        Optional<Double> factor = battery(enemy, name)
                ? enemy.ringArena(difficulty).flatMap(RingBlock.RingArenaChange::interval)
                : Optional.empty();
        Optional<Double> authored = hook.flatMap(EnemyData.Hook::attacks)
                .map(changes -> changes.get(name))
                .flatMap(EnemyData.AttackChange::interval);
        double written = attack.interval().orElseThrow();
        return authored.orElseGet(() -> factor.map(f -> written * f)
                .orElseGet(() -> written / content.difficulty().enemyFireRate().of(difficulty)));
    }

    /**
     * A boss's {@code laser-line} attack ({@link BossSpec.Pattern#LINE}) fired by {@code firing}: the unit's line (its
     * telegraph by the unit's hook {@code telegraph}, its live time, band and hold, its bullet class's damage) every
     * {@code interval} s.
     */
    static BossSpec.Attack lineAttack(
            Content content,
            String name,
            EnemyData.Attack attack,
            Optional<EnemyData.Hook> unitHook,
            double interval,
            List<Integer> firing) {
        EnemyData.Line line = attack.line().orElseThrow();
        double damage = UnitRules.bulletDamage(content, attack.bullet().orElseThrow());
        EnemySpec.LaserLine laser = new EnemySpec.LaserLine(
                unitHook.flatMap(EnemyData.Hook::telegraph).orElse(line.telegraph()),
                line.live(),
                line.band(),
                damage,
                interval,
                attack.firstShotDelay().orElse(0.0),
                line.holdWithin());
        return new BossSpec.Attack(
                        name,
                        BossSpec.Pattern.AIMED,
                        EnemyGun.aimed(interval, 0, 1, 1, damage, false),
                        EnemyGun.BURST_GAP_SECONDS,
                        attack.rotate().orElse(false),
                        1,
                        0,
                        0,
                        0,
                        firing,
                        attack.staggered().orElse(false),
                        Optional.empty())
                .withLine(laser);
    }

    /** {@code enemy}'s rings on {@code difficulty} for the simulation; empty if it has none. */
    static Optional<RingSpec> rings(Content content, EnemyData enemy, Difficulty difficulty, InLevel inLevel) {
        EnemyData.BossData script = enemy.boss().orElseThrow();
        if (script.rings().isEmpty()) {
            return Optional.empty();
        }
        Optional<RingBlock.RingArenaChange> change = enemy.ringArena(difficulty);
        double factor = change.flatMap(RingBlock.RingArenaChange::speed).orElse(1.0);
        List<RingSpec.Ring> rings = new ArrayList<>();
        for (EnemyData.RingData ring : script.rings().get()) {
            EnemyData.Batteries batteries = ring.batteries();
            List<Integer> parts = new ArrayList<>();
            for (int k = 0; k < batteries.count(); k++) {
                parts.add(part(enemy, batteries.partName(k)));
            }
            rings.add(new RingSpec.Ring(
                    ring.name(),
                    ring.radius(),
                    ring.sense(),
                    ring.speed().map(speed -> Math.toRadians(speed) * factor).orElse(0.0),
                    Math.toRadians(ring.start().orElse(0.0)),
                    parts,
                    attack(enemy, batteries.attack()),
                    ring.step()
                            .map(step -> new RingSpec.Step(
                                    Math.toRadians(step.angle()), step.seconds() / factor, step.hold(), step.tell()))));
        }
        EnemyData.FireArc arc = script.fireArc().orElseThrow();
        List<Integer> locks = script.phases().stream()
                .map(phase -> phase.until().locks().orElse(0))
                .toList();
        RingSpec spec = new RingSpec(
                rings,
                Math.toRadians(change.flatMap(RingBlock.RingArenaChange::arc).orElse(arc.degrees())),
                // The doc's bearings (90° straight down, clockwise from the right) are the rings' convention + 90°.
                new BossSpec.Clamp(
                        Math.toRadians(arc.clamp().min() - 90),
                        Math.toRadians(arc.clamp().max() - 90)),
                script.spinUp().orElse(RingBlock.SPIN_UP),
                script.grind().orElse(RingBlock.GRIND),
                script.speedUps().orElse(RingBlock.SPEED_UPS),
                locks,
                script.cookoff().orElse(RingBlock.COOKOFF));
        return Optional.of(spec.withCore(script.core().map(core -> core(content, enemy, core, difficulty, inLevel))));
    }

    private static RingSpec.Core core(
            Content content, EnemyData enemy, EnemyData.CoreData core, Difficulty difficulty, InLevel inLevel) {
        EnemyBasisData.Shields shared = content.enemyBasis().shields();
        Optional<EnemySpec.Shield> shield = core.shield()
                .map(hp -> new EnemySpec.Shield(
                        content.difficulty().enemyHp(hp * UnitRules.actHpFactor(content, enemy, inLevel), difficulty),
                        shared.delay(),
                        shared.refill(),
                        shared.breakerHold()));
        // Easy has no core rail (its attack's `only`): the core then has none.
        Optional<EnemyData.CoreRail> rail = core.rail()
                .filter(r ->
                        enemy.attack(r.attack()).map(a -> a.firesOn(difficulty)).orElse(true));
        Optional<EnemyData.CoreSpiral> spiral = core.spiral();
        return new RingSpec.Core(
                part(enemy, core.part()),
                core.glow(),
                core.opening(),
                shield,
                spiral.map(s -> attack(enemy, s.attack())).orElse(-1),
                // Without a spiral the cycle is never read; the simulation wants a run above 0.
                spiral.map(EnemyData.CoreSpiral::run).orElse(1.0),
                spiral.map(EnemyData.CoreSpiral::rest).orElse(0.0),
                spiral.map(EnemyData.CoreSpiral::tell).orElse(0.0),
                rail.map(r -> attack(enemy, r.attack())).orElse(-1),
                rail.map(EnemyData.CoreRail::below).orElse(1.0),
                core.bays().map(bays -> bays(content, enemy, bays, difficulty, inLevel)));
    }

    private static RingSpec.Bays bays(
            Content content, EnemyData enemy, EnemyData.BaysData bays, Difficulty difficulty, InLevel inLevel) {
        EnemySpec.Launcher launcher = new EnemySpec.Launcher(
                UnitRules.enemy(content, bays.from(), difficulty, Optional.empty(), inLevel),
                // Hard's stream + 1: the boss's hook spawn_count.
                UnitRules.hook(enemy, difficulty)
                        .flatMap(EnemyData.Hook::spawnCount)
                        .orElse(bays.stream()),
                bays.every(),
                bays.gap(),
                bays.first(),
                bays.speed().orElse(content.enemy(bays.from()).speed()),
                Math.toRadians(bays.spread()),
                bays.straight(),
                bays.mostAlive());
        return new RingSpec.Bays(
                bays.count(),
                bays.radius(),
                Math.toRadians(bays.start().orElse(0.0)),
                launcher,
                bays.tell(),
                bays.skipWithin());
    }

    /** The level's mass-relay flash on {@code difficulty} for the simulation; empty without one. */
    static Optional<HazardSpec.Relay> relay(LevelData level, Difficulty difficulty) {
        return level.relay()
                .map(relay -> new HazardSpec.Relay(
                        relay.t(),
                        relay.telegraphOn(difficulty),
                        relay.live(),
                        relay.band(),
                        relay.damage(),
                        relay.fromRight()));
    }

    /** The index of part {@code name} in the boss's parts (the batteries included). */
    static int part(EnemyData enemy, String name) {
        int index = enemy.partIndex(name);
        if (index < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + name + "'");
        }
        return index;
    }

    /** The index of attack {@code name} among the boss's attacks (BossRules keeps their order). */
    static int attack(EnemyData enemy, String name) {
        for (int a = 0; a < enemy.attacks().size(); a++) {
            if (enemy.attacks().get(a).name().filter(name::equals).isPresent()) {
                return a;
            }
        }
        throw new IllegalArgumentException(enemy.name() + ": no attack '" + name + "'");
    }
}
