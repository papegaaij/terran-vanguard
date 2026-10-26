package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.BossSpec;
import vanguard.sim.CoilSpec;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;

/**
 * M8 part H (design/enemies/bosses/abyssal-maw, design/campaign Level 28): a coiled boss's keys for the simulation
 * ({@link SimSpecs}, through {@link BossRules}): its {@code boss.coil} as a {@link CoilSpec} at a difficulty (the screen
 * places of the data, y below the top edge, turned once into the simulation's frame, y up; the parts, phases and attacks
 * by their indexes; the shed Eel Swarm built with the boss's segment count and the act HP factor of its level), and a
 * level's {@code cruisers} (the finale's run, Level 28) as a {@link CruiserRunSpec} at a difficulty.
 */
final class MawRules {
    private MawRules() {}

    /** {@code enemy}'s coil on {@code difficulty} for the simulation; empty if it does not coil. */
    static Optional<CoilSpec> coil(Content content, EnemyData enemy, Difficulty difficulty, InLevel inLevel) {
        return enemy.coilArena(difficulty).map(coil -> coil(content, enemy, coil, difficulty, inLevel));
    }

    private static CoilSpec coil(
            Content content, EnemyData enemy, EnemyData.CoilData coil, Difficulty difficulty, InLevel inLevel) {
        List<EnemyData.PhaseData> phases = enemy.boss().orElseThrow().phases();
        EnemyData.CoilBody body = coil.body();
        double clearance = body.clearance();
        EnemyData.Inner inner = body.inner();
        CoilSpec.Body spec = new CoilSpec.Body(
                body.segments(),
                body.spacing(),
                SimSpecs.hitbox(body.hitbox()),
                body.path().stream().map(MawRules::point).toList(),
                body.bend(),
                inner.left(),
                inner.right(),
                PlayField.HEIGHT - inner.bottom(),
                inner.top().map(top -> PlayField.HEIGHT - top).orElse(PlayField.HEIGHT + clearance),
                clearance);
        EnemyData.SweepsData sweeps = coil.sweeps();
        List<Double> every = new ArrayList<>();
        List<List<CoilSpec.Side>> orders = new ArrayList<>();
        for (EnemyData.PhaseData phase : phases) {
            Optional<EnemyData.SweepPhase> own = sweeps.phases().stream()
                    .filter(p -> p.phase().equals(phase.name()))
                    .findFirst();
            every.add(own.map(EnemyData.SweepPhase::every).orElse(0.0));
            orders.add(own.map(p -> p.sides().stream().map(MawRules::side).toList())
                    .orElse(List.of()));
        }
        CoilSpec.Sweeps sweepSpec = new CoilSpec.Sweeps(
                every,
                orders,
                sweeps.segments(),
                sweeps.tell(),
                sweeps.swing(),
                sweeps.hold(),
                sweeps.back(),
                sweeps.reach(),
                sweeps.bottomReach(),
                UnitRules.bulletDamage(content, sweeps.bullet()),
                // The side sweeps follow the ship's y: below the top edge in the data, up from the bottom edge here.
                new BossSpec.Clamp(
                        PlayField.HEIGHT - sweeps.along().max(),
                        PlayField.HEIGHT - sweeps.along().min()),
                new BossSpec.Clamp(sweeps.across().min(), sweeps.across().max()));
        return new CoilSpec(
                spec,
                coil.intro(),
                coil.wind(),
                coil.sacs().parts().stream().map(part -> part(enemy, part)).toList(),
                coil.sacs().swell(),
                new CoilSpec.Jaw(coil.jaw().glow(), coil.jaw().open()),
                coil.wall()
                        .map(wall -> new CoilSpec.Wall(
                                attack(enemy, wall.attack()),
                                wall.glow(),
                                wall.gaps(),
                                wall.near(),
                                wall.far(),
                                wall.chain())),
                sweepSpec,
                coil.dive()
                        .map(dive -> new CoilSpec.Dive(
                                phase(phases, dive.phase()),
                                dive.below(),
                                dive.slide(),
                                dive.quiet(),
                                dive.shadow(),
                                dive.lunge(),
                                dive.body(),
                                dive.back(),
                                dive.width(),
                                new BossSpec.Clamp(
                                        dive.column().min(), dive.column().max()),
                                UnitRules.bulletDamage(content, dive.bullet()))),
                coil.implants()
                        .map(implants -> new CoilSpec.Implants(
                                implants.parts().stream()
                                        .map(part -> part(enemy, part))
                                        .toList(),
                                phase(phases, implants.phase()),
                                attack(enemy, implants.attack()),
                                implants.tell(),
                                implants.open(),
                                implants.arc())),
                coil.shed().map(shed -> shed(content, phases, shed, difficulty, inLevel)));
    }

    private static CoilSpec.Shed shed(
            Content content,
            List<EnemyData.PhaseData> phases,
            EnemyData.ShedData shed,
            Difficulty difficulty,
            InLevel inLevel) {
        EnemySpec enemy = UnitRules.enemy(
                content,
                shed.enemy(),
                difficulty,
                Optional.of(
                        new LevelData.EnemyChange(Optional.empty(), Optional.empty(), Optional.of(shed.segments()))),
                inLevel);
        List<Double> every = new ArrayList<>();
        List<Integer> most = new ArrayList<>();
        for (EnemyData.PhaseData phase : phases) {
            Optional<EnemyData.ShedPhase> own = shed.phases().stream()
                    .filter(p -> p.phase().equals(phase.name()))
                    .findFirst();
            every.add(own.map(EnemyData.ShedPhase::every).orElse(0.0));
            most.add(own.map(EnemyData.ShedPhase::most).orElse(0));
        }
        List<CoilSpec.ShedPoint> points = shed.points().stream()
                .map(point -> {
                    CoilSpec.Side side = side(point.side());
                    // A side's point is its y (below the top edge in the data), the bottom's its x.
                    return new CoilSpec.ShedPoint(
                            side, side == CoilSpec.Side.BOTTOM ? point.at() : PlayField.HEIGHT - point.at());
                })
                .toList();
        return new CoilSpec.Shed(enemy, points, every, most, shed.tell());
    }

    /** A screen point ([x, y] px below the top edge) in the simulation's frame. */
    private static BossSpec.Point point(Point at) {
        return new BossSpec.Point(at.x(), PlayField.HEIGHT - at.y());
    }

    /** A coil side by its data name ({@link MawBlock#COIL_SIDES}). */
    static CoilSpec.Side side(String name) {
        return switch (name) {
            case "left" -> CoilSpec.Side.LEFT;
            case "bottom" -> CoilSpec.Side.BOTTOM;
            case "right" -> CoilSpec.Side.RIGHT;
            default -> throw new IllegalArgumentException("no coil side '" + name + "'");
        };
    }

    private static int part(EnemyData enemy, String name) {
        int index = enemy.partIndex(name);
        if (index < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + name + "'");
        }
        return index;
    }

    /** The index of attack {@code name} among the boss's attacks (BossRules keeps their order). */
    private static int attack(EnemyData enemy, String name) {
        for (int a = 0; a < enemy.attacks().size(); a++) {
            if (enemy.attacks().get(a).name().filter(name::equals).isPresent()) {
                return a;
            }
        }
        throw new IllegalArgumentException(enemy.name() + ": no attack '" + name + "'");
    }

    private static int phase(List<EnemyData.PhaseData> phases, String name) {
        for (int p = 0; p < phases.size(); p++) {
            if (phases.get(p).name().equals(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("no boss phase '" + name + "'");
    }

    /**
     * The level's cruiser run on {@code difficulty} for the simulation (Level 28's finale): its times, hulls and inner
     * edges, and its broadside, flak and point defence with the difficulty's changes; the bullets above water at their
     * written speeds (no difficulty lever: the run is a set piece's, as the boss spec writes its numbers per difficulty).
     */
    static Optional<CruiserRunSpec> cruisers(Content content, LevelData level, Difficulty difficulty) {
        return level.cruisers().map(run -> {
            LevelData.Cruisers c = run.on(difficulty);
            LevelData.Broadside b = c.broadside();
            LevelData.FlakData f = c.flak();
            LevelData.PointDefenceData pd = c.pointDefence();
            return new CruiserRunSpec(
                    c.rise(),
                    c.run(),
                    c.end(),
                    c.fall(),
                    SimSpecs.hitbox(c.hull()),
                    c.inner().min(),
                    c.inner().max(),
                    new CruiserRunSpec.Broadside(
                            b.band(),
                            b.tell(),
                            b.shells(),
                            b.speed(),
                            UnitRules.bulletDamage(content, b.bullet()),
                            b.every(),
                            b.doubleAfter().orElse(Double.POSITIVE_INFINITY),
                            b.free()),
                    new CruiserRunSpec.Flak(
                            f.markers(),
                            f.every(),
                            f.fuse(),
                            f.radius(),
                            f.scatter(),
                            UnitRules.bulletDamage(content, f.bullet()),
                            f.ring(),
                            f.speed(),
                            UnitRules.bulletDamage(content, f.ringBullet())),
                    new CruiserRunSpec.PointDefence(
                            pd.burst(),
                            pd.speed(),
                            UnitRules.bulletDamage(content, pd.bullet()),
                            pd.every(),
                            // The turrets' heights below the top edge in the data, up from the bottom edge here.
                            pd.turrets().stream().map(y -> PlayField.HEIGHT - y).toList()));
        });
    }
}
