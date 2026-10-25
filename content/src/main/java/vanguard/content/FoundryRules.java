package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.WaveSpec;

/**
 * M7 part F's rules for the simulation (step F2; design/campaign Level 19 Foundry of Tharsis,
 * design/enemies/ground/warden-tank, design/enemies/bosses/revenant-walker; the gap check's defaults pending the user's
 * review in round 42): the Warden Tank's independent {@code turret} and its {@code rear_plate}; a nest's {@code arc:
 * up}; the level's conveyor {@code belts} and furnace {@code vents}; the Revenant Walker's {@code stride} (its passes),
 * front {@code plate}, rear flame {@code cone} and its pods' {@code homing} missiles. The section keys ({@code
 * hold_after_boss}, the crawl's ease after the arena) are {@link MarsRules#sections}', the {@code allowed} escapes
 * {@link LevelRules#secondary}'s; the music's {@code stems: motif} is presentation (the simulation never reads it).
 */
final class FoundryRules {
    /** A ground point at the top edge lies this far ahead of the scroll ({@code PlayField.HEIGHT / 2}), px. */
    private static final double HALF_FIELD = PlayField.HEIGHT / 2.0;

    private FoundryRules() {}

    /**
     * {@code spec} with the stat block's turret (the Warden Tank's: its turn rate by difficulty, its lock and its
     * aim window) and its rear plate. A unit without them is returned as it is.
     */
    static EnemySpec unit(EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        EnemySpec out = spec;
        Optional<EnemyData.Turret> turret = enemy.turret(difficulty);
        if (turret.isPresent()) {
            EnemyData.Turret t = turret.get();
            out = out.withTurret(Optional.of(
                    new EnemySpec.Turret(Math.toRadians(t.turnRate()), t.lock(), Math.toRadians(t.within()))));
        }
        Optional<EnemyData.RearPlate> plate = enemy.rearPlate();
        if (plate.isPresent()) {
            out = out.withRearPlate(Optional.of(new EnemySpec.RearPlate(
                    Math.toRadians(plate.get().arc()), plate.get().multiplier())));
        }
        return out;
    }

    /** A nest's guns' facing, radians clockwise from straight down: π for {@code arc: up}, else 0. */
    static double arcFacing(LevelData.GroundTarget target) {
        return target.arc().filter(LevelData.ARC_UP::equals).isPresent() ? Math.PI : 0;
    }

    /** {@code script} with the level's belts and vent rows at {@code difficulty}; as it is without them. */
    static LevelScript script(Content content, LevelData level, Difficulty difficulty, LevelScript script) {
        LevelScript out = script;
        if (level.belts().isPresent()) {
            out = out.toBuilder()
                    .ground(out.ground().withBelts(belts(level, level.belts().get())))
                    .build();
        }
        if (level.vents().isPresent()) {
            out = out.toBuilder()
                    .hazards(
                            out.hazards().withVents(vents(content, level.vents().get(), difficulty)))
                    .build();
        }
        return out;
    }

    /**
     * The belts as ground stretches: a run's bottom end enters at the top edge at its {@code from} (the ground there
     * lies {@code PlayField.HEIGHT / 2} ahead of the scroll), its top end leaves at the bottom edge at its {@code to}
     * ({@code PlayField.HEIGHT / 2} behind it), on the level's scroll ({@link LevelData#scrollAt}, as the backdrop's
     * belt pieces lie).
     */
    static List<GroundSpec.Belt> belts(LevelData level, LevelData.Belts belts) {
        List<GroundSpec.Belt> out = new ArrayList<>();
        for (LevelData.BeltRun run : belts.runs()) {
            out.add(new GroundSpec.Belt(
                    run.x(),
                    belts.width(),
                    level.scrollAt(run.from()) + HALF_FIELD,
                    level.scrollAt(run.to()) - HALF_FIELD,
                    belts.speed()));
        }
        return out;
    }

    /**
     * The vent rows at {@code difficulty}: the tell, period and ripple of its change (M8 part B: a row's own over the
     * rows'), the damage of the bullet class; M8 part G: a plume's disc, its burn of the units over it and its bend
     * (degrees to radians).
     */
    static List<HazardSpec.VentRow> vents(Content content, LevelData.Vents vents, Difficulty difficulty) {
        double damage = UnitRules.bulletDamage(content, vents.bullet());
        List<HazardSpec.VentRow> out = new ArrayList<>();
        for (LevelData.VentRowData row : vents.rows()) {
            out.add(new HazardSpec.VentRow(
                    row.t(),
                    row.xs().stream().mapToDouble(Double::doubleValue).toArray(),
                    vents.width(),
                    vents.tellOn(row, difficulty),
                    vents.burn(),
                    vents.periodOn(row, difficulty),
                    vents.rippleOn(row, difficulty),
                    damage,
                    vents.disc(),
                    vents.disc() ? vents.enemyDamage().orElse(0.0) : 0,
                    vents.disc() ? Math.toRadians(vents.bend().orElse(0.0)) : 0));
        }
        return out;
    }

    /**
     * A walking boss's passes for the simulation: the lanes ({@code lanes_y} turned from px below the top edge to px
     * up from the bottom edge), the entry edges, the off and turn times, the speeds by phase; round 44: with its
     * script's hatch armour, rail lance and stomp at the hook's difficulty (its charge, intervals, tell and cooldown
     * instead).
     */
    static BossSpec.Stride stride(
            Content content,
            EnemyData enemy,
            EnemyData.BossData script,
            EnemyData.Stride stride,
            Optional<EnemyData.Hook> hook) {
        Optional<BossSpec.Lance> lance = script.lance().map(data -> lance(content, enemy, script, data, hook));
        Optional<BossSpec.Stomp> stomp = script.stomp().map(data -> {
            Optional<EnemyData.StompChange> change = hook.flatMap(EnemyData.Hook::stomp);
            return new BossSpec.Stomp(
                    data.reach(),
                    data.radius(),
                    change.flatMap(EnemyData.StompChange::tell).orElse(data.tell()),
                    data.expand(),
                    change.flatMap(EnemyData.StompChange::cooldown).orElse(data.cooldown()),
                    UnitRules.bulletDamage(content, data.bullet()));
        });
        return passes(stride).withStrikes(script.hatchArmour().orElse(false), lance, stomp);
    }

    private static BossSpec.Lance lance(
            Content content,
            EnemyData enemy,
            EnemyData.BossData script,
            EnemyData.Lance data,
            Optional<EnemyData.Hook> hook) {
        Optional<EnemyData.LanceChange> change = hook.flatMap(EnemyData.Hook::lance);
        List<Integer> parts = new ArrayList<>();
        for (String part : data.after()) {
            int index = enemy.partIndex(part);
            if (index < 0) {
                throw new IllegalArgumentException(enemy.name() + ": no part '" + part + "' for its lance");
            }
            parts.add(index);
        }
        int from = script.phases().size();
        if (data.fromPhase().isPresent()) {
            for (int f = 0; f < script.phases().size(); f++) {
                if (script.phases().get(f).name().equals(data.fromPhase().get())) {
                    from = f;
                }
            }
        }
        return new BossSpec.Lance(
                parts,
                from,
                change.flatMap(EnemyData.LanceChange::charge).orElse(data.charge()),
                data.fire(),
                change.flatMap(EnemyData.LanceChange::interval).orElse(data.interval()),
                change.flatMap(EnemyData.LanceChange::lastInterval).orElse(data.lastInterval()),
                data.width(),
                UnitRules.bulletDamage(content, data.bullet()),
                data.behind());
    }

    private static BossSpec.Stride passes(EnemyData.Stride stride) {
        return new BossSpec.Stride(
                stride.lanesX().stream().mapToDouble(Double::doubleValue).toArray(),
                stride.lanesY().stream().mapToDouble(y -> PlayField.HEIGHT - y).toArray(),
                stride.edges().stream().map(FoundryRules::edge).toList(),
                stride.offScreen(),
                stride.turn(),
                stride.speeds().stream().mapToDouble(Double::doubleValue).toArray(),
                stride.besideLane(),
                stride.turnInPlace());
    }

    private static WaveSpec.Edge edge(String edge) {
        return switch (edge) {
            case "top" -> WaveSpec.Edge.TOP;
            case "bottom" -> WaveSpec.Edge.BOTTOM;
            case "left" -> WaveSpec.Edge.LEFT;
            case "right" -> WaveSpec.Edge.RIGHT;
            default -> throw new IllegalArgumentException("a stride enters from top, bottom, left or right: " + edge);
        };
    }

    /** A walking boss's front plate over its part's index. */
    static BossSpec.Plate plate(EnemyData enemy, EnemyData.Plate plate) {
        int part = enemy.partIndex(plate.part());
        if (part < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + plate.part() + "' for its plate");
        }
        return new BossSpec.Plate(
                part, new Hitbox(plate.hitbox().width(), plate.hitbox().height()), plate.ahead());
    }

    /** A walking boss's flame cone at {@code difficulty}: the hook's length, swivel and tell instead. */
    static BossSpec.Cone cone(Content content, EnemyData.Cone cone, Optional<EnemyData.Hook> hook) {
        Optional<EnemyData.ConeChange> change = hook.flatMap(EnemyData.Hook::cone);
        return new BossSpec.Cone(
                change.flatMap(EnemyData.ConeChange::length).orElse(cone.length()),
                Math.toRadians(cone.halfAngle()),
                cone.reachBehind(),
                cone.cooldown(),
                change.flatMap(EnemyData.ConeChange::tell).orElse(cone.tell()),
                cone.burn(),
                cone.vents(),
                Math.toRadians(change.flatMap(EnemyData.ConeChange::swivel).orElse(cone.swivel())),
                UnitRules.bulletDamage(content, cone.bullet()),
                cone.behind(),
                Math.toRadians(cone.track().orElse(0.0)));
    }

    /** {@code gun} firing the attack's homing missiles (their turn rate by the hook), or as it is without them. */
    static EnemyGun homing(EnemyData.Attack attack, Optional<EnemyData.Hook> hook, EnemyGun gun) {
        if (attack.homing().isEmpty()) {
            return gun;
        }
        EnemyData.Homing homing = attack.homing().get();
        double turn = hook.flatMap(EnemyData.Hook::homing)
                .flatMap(EnemyData.HomingChange::turnRate)
                .orElse(homing.turnRate());
        return gun.withHoming(Optional.of(new EnemyGun.HomingSpec(
                Math.toRadians(turn), homing.life(), homing.hp(), homing.most(), homing.hatch())));
    }
}
