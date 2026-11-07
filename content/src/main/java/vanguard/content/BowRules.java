package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.BatteryFire;
import vanguard.sim.BossSpec;
import vanguard.sim.BossSpecBuilder;
import vanguard.sim.EnemySpec;
import vanguard.sim.EscapeSpec;
import vanguard.sim.FedShield;
import vanguard.sim.LastWordsSpec;
import vanguard.sim.PartMirror;
import vanguard.sim.PartRegen;
import vanguard.sim.PartShield;
import vanguard.sim.PhaseCue;
import vanguard.sim.PhaseOpening;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.PlayField;
import vanguard.sim.RearSweepSpec;
import vanguard.sim.WedgeBody;

/**
 * M10 part H, step H2 (design/enemies/bosses/ascendant; m10h-gaps.md §1 rows 1–11 and 13): a bow boss's keys ({@link
 * BowBlock}) for the simulation at a difficulty, on the boss's builder: the wedge body, the fed shield, the panels, the
 * batteries' bursts, the playbooks, the phase cues and openings, the regeneration and the escape. The difficulty's
 * {@code bow} changes scale the rests and the regeneration and set a brood's stream and the climb; the boss's {@code
 * mirror} hook sets the panels' arc; the HP lever (and the act HP factor) sizes the fed shield, the panels' heat and the
 * lifeboat's cap (its part's HP).
 */
final class BowRules {
    /** A bay's launcher's timed fields, unused by a playbook's launches (its stream fires on the draw). */
    private static final double BAY_EVERY = 5;

    private static final double BAY_FIRST = 1;

    private BowRules() {}

    /**
     * Sets {@code enemy}'s bow keys on {@code builder} at {@code difficulty}; {@code attackNames} are its attacks in the
     * simulation's order, {@code hoverY} the centre's height below the top edge at rest.
     */
    static BossSpecBuilder apply(
            BossSpecBuilder builder,
            Content content,
            EnemyData enemy,
            Difficulty difficulty,
            InLevel inLevel,
            List<String> attackNames,
            double hoverY) {
        EnemyData.BossData script = enemy.boss().orElseThrow();
        Optional<BowBlock.BowChange> change = enemy.bowChange(difficulty);
        double actHp = UnitRules.actHpFactor(content, enemy, inLevel);
        List<String> phases =
                script.phases().stream().map(EnemyData.PhaseData::name).toList();
        builder.wedge(script.wedge().map(wedge -> wedge(wedge, hoverY)));
        EnemyBasisData.Shields shared = content.enemyBasis().shields();
        builder.fedShields(script.fedShield()
                .map(fed -> List.of(new FedShield(
                        part(enemy, fed.part()),
                        new EnemySpec.Shield(
                                UnitRules.enemyHp(content, enemy, fed.hp() * actHp, difficulty),
                                shared.delay(),
                                shared.refill(),
                                shared.breakerHold()),
                        parts(enemy, fed.fedBy()))))
                .orElse(List.of()));
        if (script.mirrors().isPresent()) {
            EnemySpec.Mirror panel = panel(content, enemy, script.panel().orElseThrow(), difficulty, actHp);
            builder.mirrors(script.mirrors().get().stream()
                    .map(place -> new PartMirror(part(enemy, place.part()), place.facing(), place.turn(), panel))
                    .toList());
        }
        builder.batteryFire(script.batteryFire()
                .map(fire -> new BatteryFire(
                        firing(enemy, fire.attack()),
                        attack(attackNames, fire.attack()),
                        fire.atOnce(),
                        fire.apart(),
                        fire.tell())));
        double rests = change.flatMap(BowBlock.BowChange::rests).orElse(1.0);
        builder.playbooks(script.playbooks().orElse(List.of()).stream()
                .map(book -> playbook(content, enemy, book, phases, attackNames, rests, change, difficulty, inLevel))
                .toList());
        builder.cues(script.cues().orElse(List.of()).stream()
                .map(cue -> new PhaseCue(
                        cue.subject(),
                        phase(phases, cue.phase()),
                        parts(enemy, cue.parts().orElse(List.of())),
                        cue.deaths().orElse(0),
                        cue.below().orElse(0.0),
                        cue.seconds().orElse(0.0)))
                .toList());
        List<PhaseOpening> openings = new ArrayList<>();
        for (int f = 0; f < script.phases().size(); f++) {
            EnemyData.PhaseData phase = script.phases().get(f);
            if (phase.untouchable().isPresent()) {
                Point at = phase.dropAt().orElse(new Point(0, 0));
                openings.add(new PhaseOpening(
                        f, phase.untouchable().get(), phase.drop().map(SimSpecs::pickup), at.x(), at.y()));
            }
        }
        builder.openings(openings);
        double regrowth = change.flatMap(BowBlock.BowChange::regeneration).orElse(1.0);
        builder.regeneration(script.regeneration().orElse(List.of()).stream()
                .map(regen ->
                        new PartRegen(part(enemy, regen.part()), Math.min(1, regen.rate() * regrowth), regen.delay()))
                .toList());
        builder.escape(script.escape().map(escape -> escape(enemy, escape)));
        // M11 part F, step F2 (Vorne's Chimera): the rear sweep, the last words, the rise out of the well, the
        // capsule's
        // own shield (sized as the fed shield) and Rook's order per phase.
        builder.rearSweep(script.rearSweep()
                .map(sweep -> new RearSweepSpec(
                        sweep.out(),
                        sweep.speed(),
                        sweep.offShip(),
                        sweep.clear(),
                        sweep.fire()
                                .map(fire -> attack(attackNames, fire.attack()))
                                .orElse(-1),
                        sweep.fire().map(EnemyData.SweepFire::every).orElse(0.0))));
        builder.lastWords(script.lastWords().map(words -> new LastWordsSpec(words.seconds(), words.bulletsFade())));
        builder.rise(script.rise()
                .map(rise -> new BossSpec.Rise(
                        rise.seconds(),
                        rise.from().x(),
                        PlayField.HEIGHT - rise.from().y())));
        builder.partShields(script.partShields().orElse(List.of()).stream()
                .map(shield -> new PartShield(
                        part(enemy, shield.part()),
                        new EnemySpec.Shield(
                                UnitRules.enemyHp(content, enemy, shield.hp() * actHp, difficulty),
                                shield.delay().orElse(shared.delay()),
                                shared.refill(),
                                shared.breakerHold())))
                .toList());
        builder.rookOrders(script.rook().orElse(List.of()).stream()
                .map(order -> new BossSpec.RookOrder(phase(phases, order.phase()), parts(enemy, order.order())))
                .toList());
        return builder;
    }

    /** The wedge about the piece's centre ({@code hoverY} below the top edge): its half-widths and heights, y up. */
    static WedgeBody wedge(EnemyData.WedgeData wedge, double hoverY) {
        return new WedgeBody(
                (wedge.top().max() - wedge.top().min()) / 2,
                hoverY,
                (wedge.tip().max() - wedge.tip().min()) / 2,
                hoverY - wedge.tipY(),
                wedge.push());
    }

    /** The panels' reflect numbers at the difficulty: the boss's {@code mirror} hook's arc and turn, the heat by the HP lever. */
    private static EnemySpec.Mirror panel(
            Content content, EnemyData enemy, EnemyData.Mirror mirror, Difficulty difficulty, double actHp) {
        Optional<EnemyData.MirrorChange> change =
                UnitRules.hook(enemy, difficulty).flatMap(EnemyData.Hook::mirror);
        return new EnemySpec.Mirror(
                change.flatMap(EnemyData.MirrorChange::arc).orElse(mirror.arc()),
                UnitRules.enemyHp(content, enemy, mirror.heat() * actHp, difficulty),
                change.flatMap(EnemyData.MirrorChange::turn).orElse(mirror.turn()),
                mirror.down(),
                mirror.regrow(),
                mirror.gap(),
                mirror.cool(),
                mirror.coolDelay(),
                mirror.speed(),
                UnitRules.bulletDamage(content, mirror.bullet()),
                mirror.fireWithin());
    }

    private static PhasePlaybook playbook(
            Content content,
            EnemyData enemy,
            EnemyData.BowPlaybook book,
            List<String> phases,
            List<String> attackNames,
            double rests,
            Optional<BowBlock.BowChange> change,
            Difficulty difficulty,
            InLevel inLevel) {
        List<PhasePlaybook.Move> moves = new ArrayList<>();
        for (EnemyData.BowMove play : book.moves()) {
            if (!play.drawnOn(difficulty)) {
                // M11 part F, step F2: a move left out on this difficulty (the Chimera's easy phase 3: no rear sweep).
                continue;
            }
            PhasePlaybook.Kind kind = PhasePlaybook.Kind.valueOf(
                    play.move().toUpperCase(Locale.ROOT).replace('-', '_'));
            boolean launches = kind == PhasePlaybook.Kind.STREAM || kind == PhasePlaybook.Kind.BROOD;
            Optional<PhasePlaybook.Bays> bays = play.bays().map(data -> {
                int stream = kind == PhasePlaybook.Kind.BROOD
                        ? change.flatMap(BowBlock.BowChange::brood).orElse(data.stream())
                        : data.stream();
                EnemySpec.Launcher launcher = new EnemySpec.Launcher(
                        UnitRules.enemy(content, data.from(), difficulty, Optional.empty(), inLevel),
                        stream,
                        BAY_EVERY,
                        data.gap(),
                        BAY_FIRST,
                        data.speed().orElse(content.enemy(data.from()).speed()),
                        Math.toRadians(data.spread()),
                        data.straight(),
                        data.mostAlive());
                return new PhasePlaybook.Bays(
                        launcher,
                        data.places().stream()
                                .map(place -> new BossSpec.Offset(place.x(), place.y()))
                                .toList());
            });
            List<Integer> racks = parts(enemy, play.racks().orElse(List.of()));
            boolean sweep = kind == PhasePlaybook.Kind.REAR_SWEEP;
            PhasePlaybook.Move move = new PhasePlaybook.Move(
                    kind,
                    play.weight().orElse(1.0),
                    play.attack().map(name -> attack(attackNames, name)).orElse(-1),
                    play.count().orElse(launches || sweep ? 0 : kind == PhasePlaybook.Kind.BARRAGE ? racks.size() : 1),
                    play.gap().orElse(0.0),
                    play.every().orElse(1),
                    racks,
                    bays,
                    play.with().map(name -> attack(attackNames, name)).orElse(-1),
                    Math.toRadians(play.sweep().orElse(0.0)),
                    play.flank().orElse(0.0),
                    play.cover().map(name -> attack(attackNames, name)).orElse(-1));
            moves.add(play.notFirst().orElse(false) ? move.neverFirst() : move);
        }
        return new PhasePlaybook(
                phase(phases, book.phase()),
                parts(enemy, book.sources()),
                book.tell(),
                book.rest() * rests,
                book.lowRest() * rests,
                book.lowBelow(),
                moves,
                parts(enemy, book.lowParts().orElse(List.of())),
                book.lanceEvery().orElse(0),
                parts(enemy, book.fallback().orElse(List.of())));
    }

    /** The escape (round 80: a short scene, the same at every difficulty). */
    private static EscapeSpec escape(EnemyData enemy, EnemyData.EscapeData escape) {
        return new EscapeSpec(
                part(enemy, escape.part()),
                part(enemy, escape.breaksOn()),
                escape.flash(),
                escape.breakout(),
                escape.from().x(),
                PlayField.HEIGHT - escape.from().y(),
                escape.seconds(),
                escape.speed().min(),
                escape.speed().max(),
                escape.weave(),
                PlayField.HEIGHT - escape.floorY(),
                escape.hold().orElse(0.0));
    }

    /** The parts firing {@code attack}: those naming it, or those it lists. */
    static List<Integer> firing(EnemyData enemy, String attack) {
        List<EnemyData.PartData> list = enemy.partList().orElseThrow();
        List<String> listed =
                enemy.attack(attack).flatMap(EnemyData.Attack::parts).orElse(List.of());
        List<Integer> firing = new ArrayList<>();
        for (int p = 0; p < list.size(); p++) {
            EnemyData.PartData part = list.get(p);
            if (part.attack().filter(attack::equals).isPresent() || listed.contains(part.name())) {
                firing.add(p);
            }
        }
        return firing;
    }

    private static int attack(List<String> attackNames, String name) {
        int index = attackNames.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("no attack '" + name + "'");
        }
        return index;
    }

    private static int phase(List<String> phases, String name) {
        int index = phases.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("no boss phase '" + name + "'");
        }
        return index;
    }

    private static int part(EnemyData enemy, String name) {
        int index = enemy.partIndex(name);
        if (index < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + name + "'");
        }
        return index;
    }

    private static List<Integer> parts(EnemyData enemy, List<String> names) {
        return names.stream().map(name -> part(enemy, name)).toList();
    }
}
