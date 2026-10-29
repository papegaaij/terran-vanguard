package vanguard.content;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M9 part H's static checks (design/enemies/bosses/iron-sovereign, design/campaign Level 35): a ringed boss's keys
 * ({@link RingBlock}) and a level's placement of it.
 *
 * <p>A unit's: only a ringed boss ({@code boss.rings}) has phases that end on {@code locks}, the hook {@code ring_arena}
 * and attacks taken {@code from} a unit; a ringed boss is anchored, turns at most {@link RingBlock#MOST_RINGS} rings, each
 * fitting the field across (its diameter and a battery), its fire arc (and each difficulty's) {@link RingBlock#ARC_MIN}
 * to {@link RingBlock#ARC_MAX}°; each ring's batteries fire one of its attacks, a {@code laser-sweep} on a stepping ring
 * only, whose telegraph and sweep fit the step's hold; its batteries' names are new parts; its phases' {@code locks}
 * rise, at most its rings, and the last phase ends on none; a homing battery's hatch opens inside its staggered turn
 * (the interval over the batteries at each difficulty: the SAM hatch rule); the core is a part, no battery, which a
 * phase exposes, its spiral a {@code spiral}, its rail a {@code laser-line}, its bays launching a plain air unit.
 *
 * <p>A level's: a ringed boss's rings and batteries inside the field across at its placement's x.
 */
final class RingChecks {
    private RingChecks() {}

    /** A unit's M9 part H keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        boolean boss = enemy.boss().isPresent();
        for (int i = 0; i < enemy.attacks().size(); i++) {
            if (enemy.attacks().get(i).from().isPresent() && !boss) {
                problem.accept("attacks[" + i + "].from", "only a boss's attack is taken from a unit");
            }
        }
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            if (enemy.ringArena(difficulty).isPresent() && !enemy.ringed()) {
                problem.accept("difficulty.ring_arena", "only a ringed boss (boss.rings) has ring changes");
            }
        }
        if (!boss) {
            return;
        }
        EnemyData.BossData script = enemy.boss().get();
        List<EnemyData.PhaseData> phases = script.phases();
        if (!enemy.ringed()) {
            for (int f = 0; f < phases.size(); f++) {
                if (phases.get(f).until().locks().isPresent()) {
                    problem.accept(
                            "boss.phases[" + f + "].until.locks", "only a ringed boss's phases end on locked rings");
                }
            }
            return;
        }
        List<EnemyData.RingData> rings = script.rings().orElseThrow();
        if (enemy.movement().anchored().isEmpty()) {
            problem.accept("movement", "a ringed boss is anchored (movement anchored)");
        }
        if (rings.size() > RingBlock.MOST_RINGS) {
            problem.accept("boss.rings", "at most " + RingBlock.MOST_RINGS + " rings, were " + rings.size());
        }
        checkArc(enemy, script, problem);
        Set<String> batteries = new HashSet<>();
        for (int r = 0; r < rings.size(); r++) {
            checkRing(content, enemy, rings.get(r), "boss.rings[" + r + "]", batteries, problem);
        }
        checkLocks(phases, rings.size(), problem);
        script.core().ifPresent(core -> checkCore(content, enemy, core, batteries, problem));
    }

    private static void checkArc(EnemyData enemy, EnemyData.BossData script, BiConsumer<String, String> problem) {
        EnemyData.FireArc arc = script.fireArc().orElseThrow();
        checkArcDegrees("boss.fire_arc.degrees", arc.degrees(), problem);
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            enemy.ringArena(difficulty)
                    .flatMap(RingBlock.RingArenaChange::arc)
                    .ifPresent(degrees -> checkArcDegrees(
                            "difficulty." + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ".ring_arena.arc",
                            degrees,
                            problem));
        }
        if (arc.clamp().min() < 0 || arc.clamp().max() > 180) {
            problem.accept(
                    "boss.fire_arc.clamp",
                    "the bearing's clamp lies in the lower half, 0–180° (90° straight down), was ["
                            + arc.clamp().min() + ", " + arc.clamp().max() + "]");
        }
    }

    private static void checkArcDegrees(String field, double degrees, BiConsumer<String, String> problem) {
        if (degrees < RingBlock.ARC_MIN || degrees > RingBlock.ARC_MAX) {
            problem.accept(field, "a fire arc of " + RingBlock.ARC_MIN + "–" + RingBlock.ARC_MAX + "°, was " + degrees);
        }
    }

    private static void checkRing(
            Content content,
            EnemyData enemy,
            EnemyData.RingData ring,
            String field,
            Set<String> batteries,
            BiConsumer<String, String> problem) {
        EnemyData.Batteries battery = ring.batteries();
        double across = 2 * ring.radius() + battery.part().box().width();
        if (across > PlayField.WIDTH) {
            problem.accept(field, "the ring and its batteries fit the field across: " + across + " px");
        }
        for (int k = 0; k < battery.count(); k++) {
            String name = battery.partName(k);
            if (!batteries.add(name)) {
                problem.accept(field + ".batteries", "battery '" + name + "' is on two rings");
            }
        }
        for (int k = 0; k < battery.count(); k++) {
            String name = battery.partName(k);
            long named = enemy.partList().orElse(List.of()).stream()
                    .filter(part -> part.name().equals(name))
                    .count();
            if (named != 1) {
                problem.accept(field + ".batteries", "battery '" + name + "' is a part of its own (" + named + ")");
            }
        }
        Optional<EnemyData.Attack> attack = enemy.attack(battery.attack());
        if (attack.isEmpty()) {
            problem.accept(field + ".batteries.attack", "no attack named '" + battery.attack() + "'");
            return;
        }
        boolean sweep = attack.get().pattern().equals("laser-sweep");
        if (sweep != ring.step().isPresent()) {
            problem.accept(
                    field + ".batteries.attack",
                    "a stepping ring's batteries sweep in its holds (a laser-sweep), and only a stepping ring's");
        }
        if (sweep && ring.step().isPresent()) {
            EnemyData.Sweep beam = attack.get().sweep().orElseThrow();
            double hold = ring.step().get().hold();
            if (beam.telegraph() + beam.duration() > hold) {
                problem.accept(
                        field + ".step.hold",
                        "the sweep (telegraph " + beam.telegraph() + " s + " + beam.duration()
                                + " s) ends inside the hold of " + hold + " s");
            }
        }
        attack.get()
                .homing()
                .ifPresent(homing -> checkHatch(content, enemy, attack.get(), homing, battery, field, problem));
    }

    /**
     * The SAM hatch rule: a homing battery's hatch opens only on its turn, so it must open inside the staggered turn
     * (the interval over the batteries) at every difficulty, else no salvo fires.
     */
    private static void checkHatch(
            Content content,
            EnemyData enemy,
            EnemyData.Attack attack,
            EnemyData.Homing homing,
            EnemyData.Batteries battery,
            String field,
            BiConsumer<String, String> problem) {
        for (Difficulty difficulty : Difficulty.values()) {
            double interval = RingRules.interval(content, enemy, attack, UnitRules.hook(enemy, difficulty), difficulty);
            double turn = attack.staggered().orElse(false) ? interval / battery.count() : interval;
            if (!(homing.hatch() < turn)) {
                problem.accept(
                        field + ".batteries.attack",
                        "the hatch (" + homing.hatch() + " s) opens inside a battery's turn on "
                                + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ", was " + turn + " s");
            }
        }
    }

    private static void checkLocks(List<EnemyData.PhaseData> phases, int rings, BiConsumer<String, String> problem) {
        int last = 0;
        for (int f = 0; f < phases.size(); f++) {
            Optional<Integer> locks = phases.get(f).until().locks();
            if (locks.isEmpty()) {
                continue;
            }
            int n = locks.get();
            if (n <= last || n > rings) {
                problem.accept(
                        "boss.phases[" + f + "].until.locks",
                        "the phases end on more locked rings each, at most the " + rings + " rings, was " + n);
            }
            last = Math.max(last, n);
        }
        if (phases.getLast().until().locks().isPresent()) {
            problem.accept(
                    "boss.phases[" + (phases.size() - 1) + "].until", "the last phase does not end on locked rings");
        }
    }

    private static void checkCore(
            Content content,
            EnemyData enemy,
            EnemyData.CoreData core,
            Set<String> batteries,
            BiConsumer<String, String> problem) {
        if (enemy.partIndex(core.part()) < 0) {
            problem.accept("boss.core.part", "no part named '" + core.part() + "'");
        }
        if (batteries.contains(core.part())) {
            problem.accept("boss.core.part", "the core is no battery");
        }
        boolean exposed = enemy.boss().orElseThrow().phases().stream()
                .anyMatch(phase -> phase.exposes().orElse(List.of()).contains(core.part()));
        if (!exposed) {
            problem.accept("boss.core.part", "a phase exposes the core (it opens then)");
        }
        core.spiral()
                .ifPresent(spiral -> checkAttack(enemy, "boss.core.spiral.attack", spiral.attack(), "spiral", problem));
        core.rail()
                .ifPresent(rail -> checkAttack(
                        enemy, "boss.core.rail.attack", rail.attack(), EnemyData.Attack.LASER_LINE, problem));
        core.bays().ifPresent(bays -> {
            if (!content.enemies().containsKey(bays.from())) {
                problem.accept("boss.core.bays.from", "no unit '" + bays.from() + "'");
                return;
            }
            EnemyData unit = content.enemy(bays.from());
            if (!unit.layer().equals("air")
                    || unit.boss().isPresent()
                    || unit.segmentChain().isPresent()) {
                problem.accept("boss.core.bays.from", "the bays launch a plain air unit, not '" + bays.from() + "'");
            }
            if (bays.tell() > bays.every() - (bays.stream() - 1) * bays.gap()) {
                problem.accept("boss.core.bays.tell", "a bay's tell fits between two streams");
            }
        });
    }

    private static void checkAttack(
            EnemyData enemy, String field, String name, String pattern, BiConsumer<String, String> problem) {
        Optional<EnemyData.Attack> attack = enemy.attack(name);
        if (attack.isEmpty()) {
            problem.accept(field, "no attack named '" + name + "'");
        } else if (!attack.get().pattern().equals(pattern)) {
            problem.accept(field, "'" + name + "' is a " + attack.get().pattern() + ", not a " + pattern);
        }
    }

    /**
     * M9 part H (Level 35's mass-relay flash, m9h-gaps.md §1 #11): no wave enters from {@value #RELAY_QUIET_BEFORE} s
     * before the relay's telegraph to {@value #RELAY_QUIET_AFTER} s after its flash (medium's telegraph).
     */
    static final double RELAY_QUIET_BEFORE = 1.3;

    /** M9 part H: the relay's quiet window after its flash, s. */
    static final double RELAY_QUIET_AFTER = 2.5;

    /** Rounding room for the windows' ends, s. */
    private static final double EPSILON = 1e-6;

    /**
     * A level's placement of a ringed boss: its rings and batteries inside the field across at its x; M9 part H's
     * relay window (no wave entering, no line-firing unit on the ground at the flash on any difficulty) and the halt
     * boundary (a level with a ringed boss: nothing on the ground still on the field when its arena starts).
     */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        level.relay().ifPresent(relay -> checkRelay(content, level, relay, problem));
        checkHalt(content, level, problem);
        level.boss().ifPresent(placement -> {
            if (!content.enemies().containsKey(placement.enemy())) {
                return;
            }
            EnemyData enemy = content.enemy(placement.enemy());
            for (EnemyData.RingData ring :
                    enemy.boss().flatMap(EnemyData.BossData::rings).orElse(List.of())) {
                double reach = ring.radius() + ring.batteries().part().box().width() / 2;
                if (placement.x() - reach < 0 || placement.x() + reach > PlayField.WIDTH) {
                    problem.accept(
                            "boss.x",
                            "the ring '" + ring.name() + "' (" + reach + " px out) lies inside the field across at x "
                                    + placement.x());
                }
            }
        });
    }

    private static void checkRelay(
            Content content, LevelData level, HazardsBlock.Relay relay, BiConsumer<String, String> problem) {
        double flash = relay.t() + relay.telegraphOn(Difficulty.MEDIUM);
        double from = relay.t() - RELAY_QUIET_BEFORE;
        double to = flash + RELAY_QUIET_AFTER;
        for (int i = 0; i < level.waves().size(); i++) {
            double t = level.waves().get(i).t();
            if (t > from + EPSILON && t < to - EPSILON) {
                problem.accept(
                        "waves[" + i + "].t",
                        "no wave enters in the relay's quiet window (" + from + "-" + to + " s), was " + t);
            }
        }
        for (int g = 0; g < level.groundTargets().size(); g++) {
            LevelData.GroundTarget target = level.groundTargets().get(g);
            if (target.enemy().isEmpty()
                    || !content.enemies().containsKey(target.enemy().get())) {
                continue;
            }
            EnemyData unit = content.enemy(target.enemy().get());
            if (unit.attacks().stream().noneMatch(BeltRules::laserLine)) {
                continue;
            }
            double height = target.size().map(Size::height).orElse(unit.size().height());
            found:
            for (Difficulty difficulty : Difficulty.values()) {
                double fires = relay.t() + relay.telegraphOn(difficulty);
                double flashFrom = level.scrollAt(fires);
                double flashTo = level.scrollAt(fires + relay.live());
                for (GroundTargetsBlock.Placement at : placements(target)) {
                    double enters = level.scrollAt(at.t());
                    if (enters < flashTo && enters + PlayField.HEIGHT + height > flashFrom) {
                        problem.accept(
                                "ground_targets[" + g + "]",
                                "a line-firing unit (t " + at.t() + ") is on the field at the relay's flash (" + fires
                                        + " s, " + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ")");
                        break found;
                    }
                }
            }
        }
    }

    /**
     * M9 part H (m9h-gaps.md §4, the halt boundary): in a level whose boss is ringed, nothing placed on the ground
     * before the arena is still on the field when the arena starts (the ground stops under the station there).
     */
    private static void checkHalt(Content content, LevelData level, BiConsumer<String, String> problem) {
        boolean ringed = level.boss()
                .filter(placement -> content.enemies().containsKey(placement.enemy()))
                .map(placement -> content.enemy(placement.enemy()).ringed())
                .orElse(false);
        if (!ringed) {
            return;
        }
        double arena = -1;
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.sections().get(i).arena().orElse(false)) {
                arena = level.sectionStart(i);
                break;
            }
        }
        if (arena < 0) {
            return;
        }
        double halted = level.scrollAt(arena);
        for (int g = 0; g < level.groundTargets().size(); g++) {
            LevelData.GroundTarget target = level.groundTargets().get(g);
            double height = target.size()
                    .map(Size::height)
                    .or(() -> target.enemy()
                            .filter(content.enemies()::containsKey)
                            .map(name -> content.enemy(name).size().height()))
                    .orElse(0.0);
            for (GroundTargetsBlock.Placement at : placements(target)) {
                if (at.t() < arena && level.scrollAt(at.t()) + PlayField.HEIGHT + height > halted + EPSILON) {
                    problem.accept(
                            "ground_targets[" + g + "]",
                            "still on the field at the halt (t " + arena + "): placed at t " + at.t()
                                    + ", it leaves the bottom edge only after the ground stops");
                    break;
                }
            }
        }
    }

    /** A ground target's placements on every difficulty. */
    private static List<GroundTargetsBlock.Placement> placements(LevelData.GroundTarget target) {
        List<GroundTargetsBlock.Placement> all = new java.util.ArrayList<>(target.at());
        for (Optional<GroundTargetsBlock.Placements> change : List.of(target.easy(), target.hard())) {
            change.flatMap(GroundTargetsBlock.Placements::at).ifPresent(all::addAll);
        }
        return all;
    }
}
