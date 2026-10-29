package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Layer;
import vanguard.sim.PartSpec;
import vanguard.sim.Pass;
import vanguard.sim.PlayField;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.WaveSpec;
import vanguard.sim.Waypoint;

/**
 * Set pieces and bosses for the simulation (design/enemies/bosses, {@link SimSpecs#setPiece}, {@link
 * SimSpecs#boss}): their parts with the HP lever, chains, attacks, phases, moves and poses, and an arena
 * boss's lanes, slam cycle and weak spots.
 */
final class BossRules {
    private BossRules() {}

    /**
     * A set piece at {@code difficulty} (design/enemies/space/leviathan): its parts with the HP
     * lever, their guns by their attack's name, and its passes. The parts of one attack fire
     * staggered: the n-th (from 0) first fires its attack's first-shot delay times n + 1 after the
     * unit reaches the player's layer, or one interval after it without a delay.
     */
    static SetPieceSpec setPiece(Content content, LevelData.SetPieceData piece, Difficulty difficulty) {
        return setPiece(content, piece, difficulty, InLevel.NOWHERE);
    }

    /** As {@link #setPiece(Content, LevelData.SetPieceData, Difficulty)}, a returning one with the act HP factor of its level. */
    static SetPieceSpec setPiece(
            Content content, LevelData.SetPieceData piece, Difficulty difficulty, InLevel inLevel) {
        EnemyData enemy = content.enemy(piece.enemy());
        double actHp = UnitRules.actHpFactor(content, enemy, inLevel);
        List<EnemyData.PartData> partList = enemy.partList()
                .orElseThrow(() -> new IllegalArgumentException(piece.enemy() + ": a set piece has a part_list"));
        List<PartSpec> parts = new ArrayList<>();
        Map<String, Integer> rank = new TreeMap<>();
        for (EnemyData.PartData part : partList) {
            Optional<EnemyGun> gun = Optional.empty();
            double first = 0;
            if (part.attack().isPresent()) {
                String name = part.attack().get();
                EnemyData.Attack attack = enemy.attack(name)
                        .orElseThrow(() -> new IllegalArgumentException(piece.enemy() + ": no attack '" + name + "'"));
                EnemyGun built = UnitRules.gun(content, enemy, attack, difficulty, Optional.empty());
                int n = rank.merge(name, 1, Integer::sum) - 1;
                first = attack.firstShotDelay().isPresent()
                        ? attack.firstShotDelay().get() * (n + 1)
                        : built.intervalSeconds();
                gun = Optional.of(built);
            }
            parts.add(new PartSpec(
                    part.name(),
                    part.offset().x(),
                    part.offset().y(),
                    SimSpecs.hitbox(part.hitbox()),
                    content.difficulty().enemyHp(part.hp() * actHp, difficulty),
                    part.kind().equals("vital"),
                    part.bounty(),
                    gun,
                    first));
        }
        List<Pass> passes = new ArrayList<>();
        for (LevelData.PassData pass : piece.passes()) {
            List<Waypoint> path = pass.path().stream()
                    .map(point -> new Waypoint(point.t(), point.x(), PlayField.HEIGHT - point.y()))
                    .toList();
            double heading = -Math.toRadians(pass.heading());
            Layer layer = Layers.of(pass.layer());
            if (pass.descend().isEmpty()) {
                passes.add(Pass.flight(pass.name(), layer, heading, path));
            } else {
                LevelData.Descent descent = pass.descend().get();
                passes.add(new Pass(
                        pass.name(),
                        layer,
                        heading,
                        path,
                        descent.at(),
                        descent.seconds(),
                        descent.at() + pass.holdOn(difficulty).orElseThrow(),
                        pass.leaveSpeed().orElseThrow()));
            }
        }
        return new SetPieceSpec(
                piece.enemy(),
                SimSpecs.hitbox(enemy.size()),
                SimSpecs.hitbox(enemy.hitbox()),
                content.enemyBasis().contactDamage().get(enemy.tier()),
                parts,
                passes,
                UnitRules.drop(enemy).map(EnemySpec.Drop::pickup));
    }

    /**
     * The level's boss at {@code difficulty} (design/enemies/bosses): a set piece with its parts (the
     * HP lever, weak-point multipliers), its chains, its attacks (the fire-rate and bullet-speed
     * levers, the stat block's per-attack changes) and its phases, arriving where the level places it.
     */
    static SetPieceSpec boss(Content content, LevelData.BossPlacement placement, Difficulty difficulty) {
        return boss(content, placement, difficulty, InLevel.NOWHERE);
    }

    /**
     * As {@link #boss(Content, LevelData.BossPlacement, Difficulty)} in a level: a returning boss's
     * parts and the units it releases get the act HP factor of that level.
     */
    static SetPieceSpec boss(
            Content content, LevelData.BossPlacement placement, Difficulty difficulty, InLevel inLevel) {
        String slug = placement.enemy();
        EnemyData enemy = content.enemy(slug);
        EnemyData.BossData script =
                enemy.boss().orElseThrow(() -> new IllegalArgumentException(slug + ": no boss script"));
        List<EnemyData.PartData> partList =
                enemy.partList().orElseThrow(() -> new IllegalArgumentException(slug + ": a boss has a part_list"));
        List<PartSpec> parts = new ArrayList<>();
        List<Integer> armoured = new ArrayList<>();
        for (int p = 0; p < partList.size(); p++) {
            EnemyData.PartData part = partList.get(p);
            if (part.armoured()) {
                // A fire-only part (part G): it never takes damage, so its HP only keeps it alive.
                armoured.add(p);
            }
            parts.add(new PartSpec(
                    part.name(),
                    part.offset().x(),
                    part.offset().y(),
                    SimSpecs.hitbox(part.hitbox()),
                    part.armoured()
                            ? 1
                            : content.difficulty()
                                    .enemyHp(part.hp() * UnitRules.actHpFactor(content, enemy, inLevel), difficulty),
                    part.kind().equals("vital"),
                    part.bounty(),
                    Optional.empty(),
                    0,
                    part.multiplier().orElse(1.0)));
        }
        List<BossSpec.Chain> chains = new ArrayList<>();
        for (EnemyData.ChainData chain : enemy.chains().orElse(List.of())) {
            chains.add(new BossSpec.Chain(
                    chain.name(),
                    chain.from().x(),
                    chain.from().y(),
                    partIndex(enemy, chain.to()),
                    chain.segments(),
                    SimSpecs.hitbox(chain.hitbox()),
                    chain.lag(),
                    Math.toRadians(chain.bend()),
                    chain.slams()));
        }
        List<String> attackNames = new ArrayList<>();
        List<BossSpec.Attack> attacks = new ArrayList<>();
        DifficultyData levers = content.difficulty();
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        Map<String, EnemyData.AttackChange> changes =
                hook.flatMap(EnemyData.Hook::attacks).orElse(Map.of());
        for (EnemyData.Attack attack : enemy.attacks()) {
            String name =
                    attack.name().orElseThrow(() -> new IllegalArgumentException(slug + ": a boss names its attacks"));
            Optional<EnemyData.AttackChange> change = Optional.ofNullable(changes.get(name));
            BossSpec.Pattern pattern =
                    switch (attack.pattern()) {
                        case "aimed" -> BossSpec.Pattern.AIMED;
                        case "ring" -> BossSpec.Pattern.RING;
                        case "spiral" -> BossSpec.Pattern.SPIRAL;
                        case "fan" -> BossSpec.Pattern.FAN;
                        // M5 part H (the Siege Spire; H5 = a): the mortar's lob and the laser sweep from a part.
                        case "mortar" -> BossSpec.Pattern.LOB;
                        case "laser-sweep" -> BossSpec.Pattern.SWEEP;
                        // M9 part H (the Iron Sovereign): a rail line from a part (RingRules).
                        case EnemyData.Attack.LASER_LINE -> BossSpec.Pattern.LINE;
                        default ->
                            throw new IllegalArgumentException(slug
                                    + ": a boss fires aimed, fan, ring, spiral, mortar, laser-sweep or laser-line"
                                    + " attacks, not " + attack.pattern());
                    };
            // M9 part H: an attack taken from a ground unit (from) keeps that unit's hooks on its shape (the SAM
            // Nest's burst and missiles, the Rail Bunker's telegraph, the Sentinel Tower's arc); the boss's otherwise.
            Optional<EnemyData.Hook> shapeHook = RingRules.attackHook(content, attack, hook, difficulty);
            int burst = change.flatMap(EnemyData.AttackChange::burst)
                    .or(() -> attack.from().isPresent() ? shapeHook.flatMap(EnemyData.Hook::burst) : Optional.empty())
                    .or(attack::burst)
                    .orElse(1);
            int count = change.flatMap(EnemyData.AttackChange::count)
                    .or(attack::count)
                    .orElse(1);
            List<Integer> firing = new ArrayList<>();
            // Round 34, P2: the parts with aim: down fire this (aimed) attack straight down.
            List<Integer> down = new ArrayList<>();
            // M7 part F: a walking boss's attack fired along its heading (aim: facing; the walker's first-phase chin
            // burst), which the simulation's "down" means on a walker.
            boolean facing =
                    enemy.walks() && attack.aim().filter("facing"::equals).isPresent();
            for (int p = 0; p < partList.size(); p++) {
                // M7 part F: or the parts the attack lists (the walker's pods fire two salvo patterns).
                String partName = partList.get(p).name();
                if (partList.get(p).attack().filter(name::equals).isPresent()
                        || attack.parts()
                                .filter(list -> list.contains(partName))
                                .isPresent()) {
                    firing.add(p);
                    if (partList.get(p).aim().isPresent() || facing) {
                        down.add(p);
                    }
                }
            }
            // Round 43: a walking boss's fan along its heading too (the walker's first-phase chin fan).
            if (!down.isEmpty() && pattern != BossSpec.Pattern.AIMED && !(facing && pattern == BossSpec.Pattern.FAN)) {
                throw new IllegalArgumentException(
                        slug + ": only a part firing an aimed attack aims down, not one firing " + name);
            }
            // M9 part H: an authored interval as it is, a ring battery's by the hook's ring_arena factor (RingRules).
            double interval = RingRules.interval(content, enemy, attack, hook, difficulty);
            double damage = UnitRules.bulletDamage(content, attack.bullet().orElseThrow());
            if (pattern == BossSpec.Pattern.LINE) {
                attackNames.add(name);
                attacks.add(RingRules.lineAttack(content, name, attack, shapeHook, interval, firing));
                continue;
            }
            if (pattern == BossSpec.Pattern.LOB || pattern == BossSpec.Pattern.SWEEP) {
                // M5 part H: the lob and the sweep (PartHRules), each part on its own clock when staggered.
                attackNames.add(name);
                attacks.add(PartHRules.bossAttack(
                        content, name, pattern, attack, shapeHook, interval, damage, firing, difficulty));
                continue;
            }
            double speed =
                    attack.speed().orElseThrow() * levers.enemyBulletSpeed().of(difficulty);
            EnemyGun gun = pattern == BossSpec.Pattern.FAN
                    ? new EnemyGun(
                            interval,
                            0,
                            // Round 43: a boss's fan may come in a burst (the walker's chin), burst_gap apart.
                            burst,
                            speed,
                            damage,
                            false,
                            count,
                            Math.toRadians(attack.spread().orElseThrow()),
                            Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY)
                    // M7 part F: the walker's pods' homing missiles (FoundryRules).
                    : FoundryRules.homing(attack, shapeHook, EnemyGun.aimed(interval, 0, burst, speed, damage, false));
            attackNames.add(name);
            attacks.add(new BossSpec.Attack(
                    name,
                    pattern,
                    gun,
                    attack.burstGap().orElse(EnemyGun.BURST_GAP_SECONDS),
                    attack.rotate().orElse(false),
                    count,
                    change.flatMap(EnemyData.AttackChange::arms)
                            .or(attack::arms)
                            .orElse(0),
                    Math.toRadians(attack.turnRate().orElse(0.0)),
                    attack.duration().orElse(0.0),
                    firing,
                    // M5 part H: the pods' orbs, each pod on its own clock.
                    attack.staggered().orElse(false),
                    Optional.empty(),
                    down,
                    // Round 40: the Siege Spire's pods take turns to aim, each after a glow.
                    attack.aimTurns().map(turns -> new BossSpec.AimTurns(turns.every(), turns.glow()))));
        }
        Map<String, Integer> spawnCounts = hook.flatMap(EnemyData.Hook::spawns).orElse(Map.of());
        List<BossSpec.Phase> phases = new ArrayList<>();
        for (int f = 0; f < script.phases().size(); f++) {
            EnemyData.PhaseData phase = script.phases().get(f);
            // M9 part H: an attack that does not fire on the difficulty (its `only`: the Sovereign's core rail on easy)
            // leaves the phase.
            List<String> fired = phase.alternate().or(phase::attacks).orElse(List.of()).stream()
                    .filter(name -> enemy.attack(name)
                            .map(attack -> attack.firesOn(difficulty))
                            .orElse(true))
                    .toList();
            List<Integer> firedIndexes = fired.stream()
                    .map(name -> {
                        int index = attackNames.indexOf(name);
                        if (index < 0) {
                            throw new IllegalArgumentException(slug + ": no attack '" + name + "'");
                        }
                        return index;
                    })
                    .toList();
            if (phase.alternate().isPresent()) {
                for (int a : firedIndexes) {
                    if (attacks.get(a).pattern() == BossSpec.Pattern.SPIRAL
                            && !(attacks.get(a).durationSeconds() > 0)) {
                        throw new IllegalArgumentException(
                                slug + ": spiral " + attacks.get(a).name() + " alternates without a duration");
                    }
                }
            }
            // A later phase that alternates holds its fire a beat by default (the frigate's crown).
            double delay =
                    phase.delay().orElse(f > 0 && phase.alternate().isPresent() ? BossSpec.PHASE_DELAY_SECONDS : 0.0);
            // M5 part E: a phase that ends on its parts' HP share names them for the share, not for a count.
            List<Integer> untilParts = phase.until().parts().orElse(List.of()).stream()
                    .map(part -> partIndex(enemy, part))
                    .toList();
            boolean share = phase.until().below().isPresent();
            // M7 part F: a share beside the parts' left (the walker's first phase: both pods, or the hull below 60 %).
            Optional<EnemyData.Share> besides = phase.until().share();
            List<Integer> shareParts = besides.map(both -> both.parts().stream()
                            .map(part -> partIndex(enemy, part))
                            .toList())
                    .orElse(share ? untilParts : List.of());
            double below = besides.map(EnemyData.Share::below)
                    .or(() -> phase.until().below())
                    .orElse(Double.NaN);
            phases.add(new BossSpec.Phase(
                    phase.name(),
                    share ? List.of() : untilParts,
                    phase.until().left().orElse(0),
                    firedIndexes,
                    phase.alternate().isPresent(),
                    phase.streams()
                            .map(stream -> new BossSpec.Stream(
                                    UnitRules.enemy(content, stream.enemy(), difficulty, Optional.empty(), inLevel),
                                    stream.count(),
                                    stream.every(),
                                    stream.interval(),
                                    switch (stream.edge()) {
                                        case "left" -> WaveSpec.Edge.LEFT;
                                        case "right" -> WaveSpec.Edge.RIGHT;
                                        default -> WaveSpec.Edge.ALTERNATING;
                                    })),
                    phase.exposes().orElse(List.of()).stream()
                            .map(part -> partIndex(enemy, part))
                            .toList(),
                    phase.bend().map(Math::toRadians).orElse(Double.NaN),
                    phase.until().seconds().orElse(Double.POSITIVE_INFINITY),
                    delay,
                    Optional.empty(),
                    phase.windows()
                            .map(windows -> bossWindows(content, enemy, windows, spawnCounts, difficulty, inLevel)),
                    phaseArena(content, enemy, phase, shareParts, below, difficulty, inLevel)));
        }
        EnemyData.Movement movement = enemy.movement();
        // M5 part E: an arena boss is anchored (its centre's height at the halt) instead of hovering.
        // M7 part F: a walking boss (the Revenant Walker) walks its passes: its station is not read.
        double hoverY = movement.anchored()
                .map(EnemyData.Anchored::y)
                .or(() -> movement.hover().map(hover -> hover.heightFromTop().min()))
                .or(() -> movement.stride().map(stride -> 0.0))
                // M7 part H: a breaching boss's mouth takes its breach points' places (ColossusRules).
                .or(() -> movement.breach().map(breach -> 0.0))
                .orElseThrow(
                        () -> new IllegalArgumentException(slug + ": a boss hovers, is anchored, walks or breaches"));
        // The moves need the station before them: walk the phases with the place, layer and pose so far.
        double stationX = placement.x();
        double stationY = PlayField.HEIGHT - hoverY;
        Layer stationLayer = Layers.of(enemy.layer());
        int stationPose = 0;
        for (int f = 0; f < phases.size(); f++) {
            Optional<EnemyData.MoveData> data = script.phases().get(f).move();
            if (data.isEmpty()) {
                continue;
            }
            EnemyData.MoveData move = data.get();
            if (move.to().isPresent()) {
                stationX = move.to().get().x();
                stationY = PlayField.HEIGHT - move.to().get().y();
            }
            stationLayer = move.layer().map(Layers::of).orElse(stationLayer);
            if (move.pose().isPresent()) {
                stationPose = script.poseIndex(move.pose().get());
                if (stationPose < 0) {
                    throw new IllegalArgumentException(
                            slug + ": no pose '" + move.pose().get() + "'");
                }
            }
            BossSpec.Phase plain = phases.get(f);
            phases.set(
                    f,
                    new BossSpec.Phase(
                            plain.name(),
                            plain.untilParts(),
                            plain.left(),
                            plain.attacks(),
                            plain.alternate(),
                            plain.stream(),
                            plain.exposes(),
                            plain.bendRadians(),
                            plain.seconds(),
                            plain.delaySeconds(),
                            Optional.of(new BossSpec.Move(
                                    stationX,
                                    stationY,
                                    stationLayer,
                                    move.descend().orElse(0.0),
                                    stationPose,
                                    move.turn().orElse(0.0))),
                            plain.windows(),
                            // M5 part H: a lift keeps its phase's arena keys (the shockwave's patch).
                            plain.arena()));
        }
        List<BossSpec.Pose> poses = new ArrayList<>();
        if (script.poses().isPresent()) {
            poses.add(new BossSpec.Pose(
                    "arrival",
                    SimSpecs.hitbox(enemy.hitbox()),
                    partList.stream()
                            .map(part -> new BossSpec.Offset(
                                    part.offset().x(), part.offset().y()))
                            .toList()));
            for (EnemyData.PoseData pose : script.poses().get()) {
                for (String name : pose.offsets().keySet()) {
                    partIndex(enemy, name);
                }
                poses.add(new BossSpec.Pose(
                        pose.name(),
                        SimSpecs.hitbox(pose.hitbox().orElse(enemy.hitbox())),
                        partList.stream()
                                .map(part -> {
                                    Point at = pose.offsets().getOrDefault(part.name(), part.offset());
                                    return new BossSpec.Offset(at.x(), at.y());
                                })
                                .toList()));
            }
        }
        return new SetPieceSpec(
                slug,
                SimSpecs.hitbox(enemy.size()),
                SimSpecs.hitbox(enemy.hitbox()),
                content.enemyBasis().contactDamage().get(enemy.tier()),
                parts,
                List.of(),
                UnitRules.drop(enemy).map(EnemySpec.Drop::pickup),
                Optional.of(BossSpec.builder(
                                placement.t(),
                                placement.x(),
                                PlayField.HEIGHT - hoverY,
                                enemy.arenaBoss()
                                        ? 0
                                        : movement.straight()
                                                .map(EnemyData.Straight::speed)
                                                .orElse(enemy.speed()),
                                movement.sine().map(EnemyData.Sine::amplitude).orElse(0.0),
                                movement.sine().map(EnemyData.Sine::period).orElse(1.0),
                                Layers.of(enemy.layer()),
                                script.midBoss(),
                                script.barName(),
                                script.par(),
                                chains,
                                attacks,
                                phases)
                        .engagesOnArrival(script.engagesOnArrival().orElse(false))
                        .armoured(armoured)
                        .poses(poses)
                        .deathSeconds(script.deathSeconds().orElse(0.0))
                        .arena(
                                enemy.arenaBoss()
                                        ? Optional.of(
                                                arena(content, enemy, script, movement.anchored(), hook, difficulty))
                                        : Optional.empty())
                        .incomingSeconds(placement.incoming().orElse(0.0))
                        // M7 part F: a walking boss's passes, front plate and flame cone (FoundryRules).
                        .stride(movement.stride()
                                .map(stride -> FoundryRules.stride(content, enemy, script, stride, hook)))
                        .plate(script.plate().map(plate -> FoundryRules.plate(enemy, plate)))
                        .cone(script.cone().map(cone -> FoundryRules.cone(content, cone, hook)))
                        // M7 part H: a breaching boss's cycle and suction (ColossusRules).
                        .breach(ColossusRules.breach(content, enemy, difficulty))
                        .suction(ColossusRules.suction(enemy, difficulty))
                        .vents(ColossusRules.vents(enemy))
                        .eruption(ColossusRules.eruption(content, enemy, difficulty))
                        .debris(ColossusRules.debris(content, enemy))
                        // M8 part H: a coiled boss's body and script (MawRules).
                        .coil(MawRules.coil(content, enemy, difficulty, inLevel))
                        // M9 part H: a ringed boss's rings, fire arc and core (RingRules).
                        .rings(RingRules.rings(content, enemy, difficulty, inLevel))
                        .build()));
    }

    /**
     * M5 part E: an arena boss's lanes (from its hit box's lower edge at the halt, Platform Tiamat's
     * deck, down to the bottom edge), its slam cycle at {@code difficulty} (the hooks {@code telegraph}
     * and {@code lanes}, the bullet-speed lever on the splash), its parts' starting layers and their
     * weak spots.
     */
    private static BossSpec.Arena arena(
            Content content,
            EnemyData enemy,
            EnemyData.BossData script,
            Optional<EnemyData.Anchored> anchoredAt,
            Optional<EnemyData.Hook> hook,
            Difficulty difficulty) {
        List<EnemyData.PartData> partList = enemy.partList().orElseThrow();
        BossSpec.Lanes lanes = BossSpec.Lanes.NONE;
        BossSpec.Slam slam = null;
        if (script.lanes().isPresent()) {
            // Only an anchored boss has lanes (ContentValidator).
            EnemyData.Anchored anchored = anchoredAt.orElseThrow();
            EnemyData.Lanes data = script.lanes().get();
            List<BossSpec.Arm> arms = new ArrayList<>();
            data.arms().orElse(Map.of()).forEach((part, owned) -> {
                int mask = 0;
                for (int lane : owned) {
                    mask |= 1 << lane;
                }
                arms.add(new BossSpec.Arm(partIndex(enemy, part), mask));
            });
            double top = PlayField.HEIGHT - anchored.y() - enemy.hitbox().height() / 2;
            if (data.isRooted()) {
                // Round 40 (the reworked Harbour Kraken): the lanes run from the roots' lower edge down.
                double lowest = Double.NEGATIVE_INFINITY;
                for (String part : data.arms().orElse(Map.of()).keySet()) {
                    EnemyData.PartData root = partList.get(partIndex(enemy, part));
                    lowest = Math.max(
                            lowest,
                            anchored.y() - root.offset().y() + root.hitbox().height() / 2);
                }
                top = PlayField.HEIGHT - lowest;
            }
            lanes = new BossSpec.Lanes(
                    data.count(),
                    data.width(),
                    top,
                    arms,
                    data.isRooted(),
                    data.raised().orElse(1.0));
            EnemyData.SlamData cycle = script.slam().orElseThrow();
            DifficultyData levers = content.difficulty();
            slam = new BossSpec.Slam(
                    hook.flatMap(EnemyData.Hook::telegraph).orElse(cycle.telegraph()),
                    cycle.rise(),
                    cycle.awash(),
                    cycle.sink(),
                    UnitRules.bulletDamage(content, cycle.damage()),
                    cycle.splash().count(),
                    cycle.splash().speed() * levers.enemyBulletSpeed().of(difficulty),
                    UnitRules.bulletDamage(content, cycle.splash().bullet()),
                    hook.flatMap(EnemyData.Hook::lanes).or(cycle::volley).orElse(Math.max(1, arms.size())),
                    BossSpec.Slam.SECOND_SECONDS);
        }
        List<Layer> layers = new ArrayList<>();
        List<BossSpec.Spot> spots = new ArrayList<>();
        for (int p = 0; p < partList.size(); p++) {
            EnemyData.PartData part = partList.get(p);
            layers.add(Layers.of(part.layer().orElse(enemy.layer())));
            for (EnemyData.SpotData spot : part.spots().orElse(List.of())) {
                spots.add(new BossSpec.Spot(
                        spot.name(),
                        p,
                        spot.offset().x(),
                        spot.offset().y(),
                        SimSpecs.hitbox(spot.hitbox()),
                        spot.multiplier(),
                        spot.during().isPresent()));
            }
        }
        return new BossSpec.Arena(lanes, slam, layers, spots);
    }

    /**
     * M5 part E: a boss phase's arena keys: its slams, its HP share over {@code shareParts}, how it
     * slams, its surfacing, its release (the units at {@code difficulty}) and its drop.
     */
    private static BossSpec.PhaseArena phaseArena(
            Content content,
            EnemyData enemy,
            EnemyData.PhaseData phase,
            List<Integer> shareParts,
            double below,
            Difficulty difficulty,
            InLevel inLevel) {
        if (phase.slamming().isEmpty()
                && phase.surface().isEmpty()
                && phase.release().isEmpty()
                && phase.drop().isEmpty()
                && phase.until().slams().isEmpty()
                && Double.isNaN(below)) {
            return BossSpec.PhaseArena.NONE;
        }
        BossSpec.Slamming slamming = phase.slamming()
                .map(data -> switch (data.mode()) {
                    case "chain" -> BossSpec.Slamming.CHAIN;
                    case "after_dive" -> BossSpec.Slamming.AFTER_DIVE;
                    default -> BossSpec.Slamming.VOLLEY;
                })
                .orElse(BossSpec.Slamming.NONE);
        return new BossSpec.PhaseArena(
                phase.until().slams().orElse(0),
                shareParts,
                below,
                slamming,
                phase.slamming().flatMap(EnemyData.SlammingData::every).orElse(0.0),
                phase.surface()
                        .map(surface -> new BossSpec.Surface(
                                partIndex(enemy, surface.part()),
                                surface.rise(),
                                surface.open().orElse(0.0),
                                surface.dive().orElse(0.0),
                                surface.glow().orElse(0.0),
                                surface.stay().orElse(false))),
                phase.release()
                        .map(release -> new BossSpec.Release(
                                UnitRules.enemy(content, release.enemy(), difficulty, Optional.empty(), inLevel),
                                release.count(),
                                release.lanes())),
                phase.drop().map(SimSpecs::pickup));
    }

    /**
     * A boss phase's windows at {@code difficulty} (part G): the groups by part index, the spawns'
     * units at the difficulty and their counts after the hook's {@code spawns}.
     */
    private static BossSpec.Windows bossWindows(
            Content content,
            EnemyData enemy,
            EnemyData.WindowData windows,
            Map<String, Integer> spawnCounts,
            Difficulty difficulty,
            InLevel inLevel) {
        return new BossSpec.Windows(
                windows.groups().stream()
                        .map(group -> group.stream()
                                .map(part -> partIndex(enemy, part))
                                .toList())
                        .toList(),
                windows.every(),
                windows.open(),
                windows.offset().orElse(0.0),
                windows.all().orElse(false),
                windows.spawns().orElse(List.of()).stream()
                        .map(spawn -> new BossSpec.Spawn(
                                spawn.name(),
                                UnitRules.enemy(content, spawn.enemy(), difficulty, Optional.empty(), inLevel),
                                spawnCounts.getOrDefault(spawn.name(), spawn.count()),
                                spawn.speed(),
                                Math.toRadians(spawn.arc().orElse(0.0)),
                                spawn.glide().orElse(0.0),
                                // M5 part H: an ambush launch (the maw's Wraiths), at most this many alive.
                                spawn.ambush().map(EnemyData.AmbushLaunch::most).orElse(0)))
                        .toList());
    }

    private static int partIndex(EnemyData enemy, String part) {
        int index = enemy.partIndex(part);
        if (index < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + part + "'");
        }
        return index;
    }
}
