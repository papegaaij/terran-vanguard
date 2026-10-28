package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Range;

/**
 * An enemy unit at a difficulty ({@link SimSpecs#enemy}): its HP with the HP lever and the act HP factor,
 * its movement, its gun by the fire-rate and bullet-speed levers and the stat block's hooks, and the
 * unit families' extras (brood, spawner, pounce, walker, sweep, proximity ring), handed on to the
 * parts' rules classes.
 */
final class UnitRules {
    private UnitRules() {}

    /**
     * An enemy at {@code difficulty}: HP by the HP lever, its aimed attack (or mine) by the
     * fire-rate and bullet-speed levers, a level's burst change and the stat block's hooks; a
     * spiral-out, a convoy's strafe height and a death burst where the stat block has them; a
     * spawner's brood and a walker's walk, armour and spit (Level 04), a level's speed factor
     * applying to the walk.
     */
    static EnemySpec enemy(
            Content content, String slug, Difficulty difficulty, Optional<LevelData.EnemyChange> change) {
        return enemy(content, slug, difficulty, change, InLevel.NOWHERE);
    }

    /**
     * As {@link #enemy(Content, String, Difficulty, Optional)} in Level {@code level} of Act {@code
     * act}: a returning unit of tier {@code medium} or larger (or a listed {@code small} one) gets the act HP factor there (see
     * {@link #actHpFactor}) on its HP and its chain's, rounded once with the HP lever.
     */
    static EnemySpec enemy(
            Content content,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            int act,
            int level) {
        return enemy(content, slug, difficulty, change, new InLevel(act, level));
    }

    /**
     * The act HP factor of a unit in Level {@code level} of Act {@code act} (design/enemies,
     * Balancing basis; user decision D5 = c of M5 part A): from Act 2 on, a unit of tier {@code
     * medium} or larger that returns from an earlier level (its {@code first_level}) gets the
     * reference DPS at the level over that at its first level, so its time to kill stays; a {@code
     * small} one listed in the basis's {@code small_act_hp} (the Needler and the Stinger) gets the same
     * capped at its {@code cap} in its {@code levels} (08–09; the user's Act 2 playtest, 2026-10-18); the
     * other {@code tiny} and
     * {@code small} units, Act 1's levels and a unit's own first level get 1. M7 part E (step E5): from the basis's
     * {@code beam_era_hp.from_level} on (Level 18, where the plan fits the Ion Beam) a unit of its tiers (not a boss)
     * takes its {@code factor} on top, its own first level included. Its bounty is unchanged
     * (the act factor scales the credits).
     */
    static double actHpFactor(Content content, String slug, int act, int level) {
        return actHpFactor(content, content.enemy(slug), new InLevel(act, level));
    }

    static double actHpFactor(Content content, EnemyData enemy, InLevel inLevel) {
        // M7 part E (step E5, the user's "tougher, not more" of 2026-10-19): the beam era's factor on top.
        double era =
                enemy.boss().isPresent() ? 1 : content.enemyBasis().beamEraHp().of(enemy.tier(), inLevel.level());
        return era * returningFactor(content, enemy, inLevel);
    }

    static double returningFactor(Content content, EnemyData enemy, InLevel inLevel) {
        EnemyBasisData.SmallActHp small = content.enemyBasis().smallActHp();
        boolean smallUnit = enemy.tier() == Tier.SMALL
                && small.units().stream()
                        .anyMatch(unit -> content.enemies().get(unit) == enemy && small.applies(unit, inLevel.level()));
        if (inLevel.act() < 2
                || (enemy.tier().compareTo(Tier.MEDIUM) < 0 && !smallUnit)
                || enemy.firstLevel() >= inLevel.level()) {
            return 1;
        }
        Map<Integer, Double> reference = content.enemyBasis().referenceDps();
        Double now = reference.get(inLevel.level());
        Double first = reference.get(enemy.firstLevel());
        if (now == null || first == null) {
            throw new IllegalArgumentException(enemy.name() + ": no reference DPS for Level " + inLevel.level()
                    + " or its first level " + enemy.firstLevel() + " (design/enemies/data.yaml)");
        }
        // The user's Act 2 playtest (2026-10-18, PT2-B 2c): the listed small units' factor is capped, so Needlers
        // and Stingers take two bolts and the Skitters (tiny) stay one-shot fodder.
        return smallUnit ? Math.min(small.cap(), now / first) : now / first;
    }

    /**
     * Where a unit flies, for the act HP factor: a level's act and number; {@link #NOWHERE} outside a level. M8 part C
     * (design/enemies/naval, Returning units under water; gap check D2): {@code water} when the unit enters under water
     * (its wave's or its nest's time lies in a section with {@code medium: underwater}): its stat block's {@code
     * underwater} block applies ({@link EnemyData#inWater}).
     */
    record InLevel(int act, int level, boolean water) {
        static final InLevel NOWHERE = new InLevel(1, 0);

        /** In the air. */
        InLevel(int act, int level) {
            this(act, level, false);
        }

        /** The same level, the unit entering under water ({@code true}) or not. */
        InLevel inWater(boolean under) {
            return under == water ? this : new InLevel(act, level, under);
        }

        /** The same level, the unit entering at {@code t} s of {@code level}: under water if its section is. */
        InLevel at(LevelData data, double t) {
            if (!data.underwater()) {
                return inWater(false);
            }
            // sectionAt is 1-based, 0 after the level's end (the last section's medium then)
            int section = data.sectionAt(t);
            int index = section == 0 ? data.sections().size() - 1 : section - 1;
            return inWater(data.mediumOf(index) == SectionsBlock.Medium.UNDERWATER);
        }
    }

    static EnemySpec enemy(
            Content content,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            InLevel inLevel) {
        return enemy(content, slug, difficulty, change, inLevel, 0);
    }

    /**
     * As {@link #enemy(Content, String, Difficulty, Optional, InLevel)} with its gun's facing {@code arcFacing}
     * radians clockwise from straight down (M7 part F: π for a nest placed {@code arc: up}; 0 as the stat block has it).
     */
    static EnemySpec enemy(
            Content content,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            InLevel inLevel,
            double arcFacing) {
        return enemy(content, slug, difficulty, change, inLevel, arcFacing, Optional.empty());
    }

    /**
     * As {@link #enemy(Content, String, Difficulty, Optional, InLevel, double)} with its flock's ram drawn {@code
     * ramAfter} s after it came onto the field instead of the stat block's (M8 part D: a spawning reef's fry).
     */
    static EnemySpec enemy(
            Content content,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            InLevel inLevel,
            double arcFacing,
            Optional<Double> ramAfter) {
        return enemy(content, content.enemy(slug), slug, difficulty, change, inLevel, arcFacing, ramAfter);
    }

    /**
     * M9 part B: as {@link #enemy(Content, String, Difficulty, Optional, InLevel, double, Optional)} of the stat block
     * {@code stat} (a wave leader's airframe, {@link EnemyData#asLeader}) flown as {@code slug}.
     */
    static EnemySpec enemy(
            Content content,
            EnemyData stat,
            String slug,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change,
            InLevel inLevel,
            double arcFacing,
            Optional<Double> ramAfter) {
        double actHp = actHpFactor(content, stat, inLevel);
        // M8 part C (gap check D2): a unit entering under water flies its stat block's underwater changes.
        EnemyData enemy = inLevel.water() ? stat.inWater() : stat;
        EnemyData.Movement movement = enemy.movement();
        Optional<EnemyData.Hook> hook = hook(enemy, difficulty);
        Optional<EnemySpec.Brood> brood = enemy.attacks().stream()
                .flatMap(attack -> attack.spawn().stream())
                .filter(spawn -> !spawn.periodic())
                .findFirst()
                .map(spawn -> new EnemySpec.Brood(
                        enemy(content, spawn.enemy(), difficulty, Optional.empty(), inLevel),
                        hook.flatMap(EnemyData.Hook::spawnCount).orElse(spawn.count()),
                        hook.flatMap(EnemyData.Hook::spawnAfter)
                                .orElse(spawn.after().orElseThrow()),
                        spawn.telegraph(),
                        Math.toRadians(spawn.arc()),
                        spawn.speed(),
                        spawn.burstBounty().orElseThrow()));
        // M5 part C: a periodic spawn (the Hive Node) with the hook's count.
        Optional<EnemySpec.Spawner> spawner = enemy.attacks().stream()
                .flatMap(attack -> attack.spawn().stream())
                .filter(EnemyData.Spawn::periodic)
                // M9 part C: a stream launcher is a launcher (EnemySpec.launcher, MinefieldRules), not a spawner.
                .filter(spawn -> spawn.stream().isEmpty())
                .findFirst()
                .map(spawn -> new EnemySpec.Spawner(
                        // M8 part D: a reef's fry draw their ram sooner than a school's (the spawn's ram_after).
                        enemy(content, spawn.enemy(), difficulty, Optional.empty(), inLevel, 0, spawn.ramAfter()),
                        hook.flatMap(EnemyData.Hook::spawnCount).orElse(spawn.count()),
                        spawn.every().orElseThrow(),
                        spawn.telegraph(),
                        Math.toRadians(spawn.arc()),
                        spawn.speed(),
                        spawn.shutWithin().orElse(0.0)));
        // M5 part C: a pounce (the Ravager), its interval by the hook's authored one or the fire-rate lever.
        Optional<EnemySpec.Pounce> pounce = enemy.attacks().stream()
                .filter(attack -> attack.pounce().isPresent())
                .findFirst()
                .map(attack -> {
                    EnemyData.Pounce leap = attack.pounce().orElseThrow();
                    return new EnemySpec.Pounce(
                            leap.range(),
                            leap.leap(),
                            leap.air(),
                            leap.scale(),
                            interval(content, attack, hook, difficulty));
                });
        double speedFactor = change.flatMap(LevelData.EnemyChange::speedFactor).orElse(1.0);
        Optional<EnemySpec.Walker> walker = movement.walk()
                .map(walk -> new EnemySpec.Walker(
                        walk.speed() * speedFactor,
                        Math.toRadians(walk.turnRate()),
                        walk.stride(),
                        Math.toRadians(enemy.armour().frontArc().orElse(0.0)),
                        enemy.attacks().stream()
                                .filter(attack -> attack.away().isPresent())
                                .findFirst()
                                .map(attack -> gun(content, enemy, attack, difficulty, change)),
                        Math.toRadians(enemy.attacks().stream()
                                .flatMap(attack -> attack.away().stream())
                                .findFirst()
                                .orElse(0.0)),
                        // M5 part B: a walker's fan with aim target (the default) goes at the ship.
                        walkerFan(enemy)
                                .map(fan -> fan.aim().orElse("target").equals("target"))
                                .orElse(false),
                        walkerFan(enemy).flatMap(EnemyData.Attack::stagger).orElse(0.0)));
        // A segment chain's unit is its head: the head part's HP and bounty (the unit's include the body).
        Optional<EnemyData.PartData> head = enemy.segmentChain()
                .flatMap(chain -> enemy.partList().flatMap(parts -> parts.stream()
                        .filter(part -> part.kind().equals("vital"))
                        .findFirst()))
                // M7 part D: a unit with a unit part (the Shellback's vent) is its body (SiegeRules.body).
                .or(() -> SiegeRules.body(enemy));
        EnemySpec spec = EnemySpec.of(
                        slug,
                        content.difficulty()
                                .enemyHp(head.map(EnemyData.PartData::hp).orElse(enemy.hp()) * actHp, difficulty),
                        SimSpecs.hitbox(enemy.hitbox()),
                        Layers.of(enemy.layer()),
                        // M9 part B (design/campaign Level 29): a neutral craft has no contact (the ship passes under).
                        enemy.isNeutral()
                                ? 0
                                : content.enemyBasis().contactDamage().get(enemy.tier()),
                        enemy.tier().compareTo(Tier.SMALL) <= 0,
                        head.map(EnemyData.PartData::bounty).orElse(enemy.bounty()),
                        movement.drift()
                                        .map(EnemyData.Drift::speed)
                                        .or(() -> movement.path().map(EnemyData.PathMove::speed))
                                        .orElse(enemy.speed())
                                * speedFactor)
                .withSnake(movement.snake().map(snake -> new EnemySpec.Snake(snake.spacing())))
                .withStreamSpeed(movement.straight().map(EnemyData.Straight::speed))
                .withHover(movement.hover()
                        .map(hover -> AbyssRules.hover(
                                hover,
                                // M5 part D: the hook's hover time (the Wraith's hard hold) instead.
                                hook.flatMap(EnemyData.Hook::hoverSeconds)
                                        .map(seconds -> new Range(seconds, seconds))
                                        .or(() -> hover.seconds().map(UnitRules::range))
                                        .orElse(new Range(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)))))
                .withOrbit(movement.orbit().map(orbit -> new EnemySpec.Orbit(orbit.radius(), orbit.turnRate())))
                .withGun(gun(content, enemy, difficulty, change)
                        .map(gun -> arcFacing != 0 ? gun.withArcFacing(arcFacing) : gun))
                .withDrop(drop(enemy))
                .withDive(movement.dive()
                        .map(dive -> new EnemySpec.Dive(
                                range(dive.y()),
                                hook(enemy, difficulty)
                                        .flatMap(EnemyData.Hook::divePause)
                                        .orElse(dive.pause()),
                                dive.speed(),
                                dive.fireAfter())))
                .withTerrain(movement.terrain().isPresent())
                .withSpiral(movement.spiralOut()
                        .map(spiral -> new EnemySpec.Spiral(
                                spiral.seconds(), spiral.turns(), spiral.growth(), spiral.drift(), spiral.ricochets())))
                .withStrafe(movement.strafe().map(strafe -> range(strafe.y())))
                .withDeathBurst(hook(enemy, difficulty)
                        .flatMap(EnemyData.Hook::deathBurst)
                        .map(puff -> new EnemySpec.DeathBurst(
                                puff.count(),
                                puff.speed()
                                        * content.difficulty()
                                                .enemyBulletSpeed()
                                                .of(difficulty),
                                bulletDamage(content, puff.bullet()))))
                .withSine(movement.sine()
                        .map(sine -> new EnemySpec.Sine(sine.amplitude(), sine.period()))
                        // M8 part C (design/enemies/naval/eel-swarm, snake): a chain head's sway about its path.
                        .or(() -> movement.path()
                                .flatMap(EnemyData.PathMove::sway)
                                .map(sway -> new EnemySpec.Sine(sway.amplitude(), 1 / sway.frequency()))))
                .withBrood(brood)
                .withWalker(walker)
                .withSideHover(movement.hover()
                        .flatMap(EnemyData.Hover::edgeX)
                        .map(edgeX -> new EnemySpec.SideHover(
                                edgeX,
                                movement.hover().flatMap(EnemyData.Hover::exit).isPresent())))
                .withSweep(sweep(content, enemy, difficulty))
                .withHardened(enemy.armour().hardened())
                .withSpawner(spawner)
                .withPounce(pounce)
                .withCloak(enemy.cloak().map(cloak -> new EnemySpec.Cloak(Layers.of(cloak.layer()), cloak.flash())))
                .withAmbush(movement.ambush()
                        .map(ambush -> new EnemySpec.Ambush(
                                ambush.gap(),
                                ambush.lane(),
                                movement.straight()
                                        .map(EnemyData.Straight::speed)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                enemy.name() + ": an ambush leaves at its straight speed")))))
                .withFlock(movement.flock().map(flock -> new EnemySpec.FlockSpec(
                                flock.separation(),
                                flock.radius(),
                                flock.alignment(),
                                flock.cohesion(),
                                flock.leader(),
                                flock.speed(),
                                flock.diveSpeed(),
                                Math.toRadians(flock.turnRate()),
                                flock.max())
                        // M8 part C (design/enemies/naval/glassfin): the ribbon's sway and the ram, the
                        // difficulty's share.
                        .withRibbon(
                                flock.sway().map(sway -> new EnemySpec.Sine(sway.amplitude(), 1 / sway.frequency())),
                                flock.ram()
                                        .map(ram -> new EnemySpec.Ram(
                                                ramAfter.orElse(ram.after()),
                                                hook.flatMap(EnemyData.Hook::ram)
                                                        .flatMap(EnemyData.RamChange::share)
                                                        .orElse(ram.share()),
                                                ram.tell(),
                                                ram.delay(),
                                                ram.speed())))))
                // M5 part E: surfacing and submerging, a proximity ring and a raft's drift.
                .withSubmerge(enemy.submerge()
                        .map(submerge -> new EnemySpec.Submerge(
                                submerge.every().min(), submerge.every().max(), submerge.swap(), submerge.start())))
                .withRing(proximityRing(content, enemy, difficulty))
                .withDrift(movement.terrain().flatMap(EnemyData.Terrain::drift).orElse(0.0));
        // M5 part F: a chaser's chase and its latch with the drain (the Lamprey).
        // M7 part A: a machine (its stat block's machine: true) is stunned longest by the EMP Burst.
        EnemySpec machine = PartFRules.chaser(
                        enemy,
                        hook,
                        enemy.segmentChain().isPresent()
                                ? ChainRules.withChain(content, enemy, spec, difficulty, actHp, change)
                                : spec)
                .withMachine(enemy.isMachine())
                // M7 part G (user decision 2026-10-20): a unit the seekers ignore (the Mote Swarm's motes).
                .withSeekersIgnore(enemy.ignoredBySeekers());
        // M7 part B: a burrow (the Burrower) and a triggered swoop (the Dust Kite); M7 part C: a link (the Tendril
        // Anchor).
        // M7 part D: an aura (the Choir Herald), a unit part and a curl-roll (the Shellback).
        // M7 part E: a spin cycle and a pull (the Dust Devil), a lane strafe (the Ghost Drone) (StormRules).
        // M7 part F: a turret and a rear plate (the Warden Tank) (FoundryRules).
        // M8 part D: a rise (the Abyss Ray) (AbyssRules).
        // M8 part E: a coil (the Spiral Nautilus) and a slowing ring (the Siren) (TrenchRules).
        // M8 part F: a lunge from a lure (the Glow Angler) (DarkRules).
        // M9 part A: the hex shield and a laser-line (BeltRules).
        // M9 part C: a mine layer, facing armour, a launcher and a train's turret cars (MinefieldRules).
        // M9 part D: a dash, air links and an aimed salvo's hatch tell (TugRules).
        return TugRules.unit(
                content,
                enemy,
                difficulty,
                MinefieldRules.unit(
                        content,
                        enemy,
                        difficulty,
                        inLevel,
                        BeltRules.unit(
                                content,
                                enemy,
                                difficulty,
                                inLevel,
                                DarkRules.unit(
                                        content,
                                        enemy,
                                        difficulty,
                                        TrenchRules.unit(
                                                content,
                                                enemy,
                                                difficulty,
                                                AbyssRules.unit(
                                                        enemy,
                                                        FoundryRules.unit(
                                                                enemy,
                                                                difficulty,
                                                                StormRules.unit(
                                                                        enemy,
                                                                        difficulty,
                                                                        SiegeRules.unit(
                                                                                content,
                                                                                enemy,
                                                                                difficulty,
                                                                                actHp,
                                                                                TerrainRules.anchor(
                                                                                        content,
                                                                                        enemy,
                                                                                        difficulty,
                                                                                        MarsRules.kite(
                                                                                                enemy,
                                                                                                hook,
                                                                                                MarsRules.burrower(
                                                                                                        content,
                                                                                                        enemy,
                                                                                                        hook,
                                                                                                        difficulty,
                                                                                                        machine))))))))))));
    }

    /** M5 part E: whether {@code attack} is a proximity ring (a {@code ring} fired within a reach, the Driftjelly's). */
    static boolean proximityRing(EnemyData.Attack attack) {
        return attack.pattern().equals("ring") && attack.within().isPresent();
    }

    /**
     * M5 part E, a proximity ring at {@code difficulty} (design/enemies/naval/driftjelly): its count
     * and reach by the hook's {@code attacks.<name>} (hard's 12 and 110 px), its cooldown as an
     * attack's interval (the fire-rate lever, or the hook's authored interval), its bullets by the
     * bullet-speed lever.
     */
    private static Optional<EnemySpec.ProximityRing> proximityRing(
            Content content, EnemyData enemy, Difficulty difficulty) {
        Optional<EnemyData.Hook> hook = hook(enemy, difficulty);
        return enemy.attacks().stream()
                .filter(UnitRules::proximityRing)
                .findFirst()
                .map(attack -> {
                    Optional<EnemyData.AttackChange> change = attack.name()
                            .flatMap(name ->
                                    hook.flatMap(EnemyData.Hook::attacks).map(changes -> changes.get(name)));
                    return new EnemySpec.ProximityRing(
                            change.flatMap(EnemyData.AttackChange::count)
                                    .orElse(attack.count().orElseThrow()),
                            attack.speed().orElseThrow()
                                    * content.difficulty().enemyBulletSpeed().of(difficulty),
                            bulletDamage(content, attack.bullet().orElseThrow()),
                            change.flatMap(EnemyData.AttackChange::within)
                                    .orElse(attack.within().orElseThrow()),
                            interval(content, attack, hook, difficulty));
                });
    }

    /** A walker's fan: its attack with the {@code fan} pattern. */
    private static Optional<EnemyData.Attack> walkerFan(EnemyData enemy) {
        return enemy.attacks().stream()
                .filter(attack -> attack.pattern().equals("fan"))
                .findFirst();
    }

    /**
     * An attack's interval at {@code difficulty}: the hook's authored {@code attacks.<name>.interval}
     * as it is (the fire-rate lever does not apply on top of it: the Mantis's beam, the Creeper's
     * hard fan), otherwise its interval by the fire-rate lever; infinite without one.
     */
    static double interval(
            Content content, EnemyData.Attack attack, Optional<EnemyData.Hook> hook, Difficulty difficulty) {
        Optional<Double> authored = attack.name()
                .flatMap(name -> hook.flatMap(EnemyData.Hook::attacks).map(changes -> changes.get(name)))
                .flatMap(EnemyData.AttackChange::interval);
        return authored.orElseGet(() -> attack.interval()
                .map(i -> i / content.difficulty().enemyFireRate().of(difficulty))
                .orElse(Double.POSITIVE_INFINITY));
    }

    /**
     * A laser sweep at {@code difficulty} (design/enemies/air/mantis): the stat block's sweep with
     * the hooks' arc and interval (an authored interval is final; otherwise the fire-rate lever
     * applies), the beam's bullet class as its damage and its origin at the eye.
     */
    private static Optional<EnemySpec.Sweep> sweep(Content content, EnemyData enemy, Difficulty difficulty) {
        Optional<EnemyData.Hook> hook = hook(enemy, difficulty);
        return enemy.attacks().stream()
                .filter(attack -> attack.pattern().equals("laser-sweep"))
                .findFirst()
                .map(attack -> {
                    EnemyData.Sweep sweep = attack.sweep().orElseThrow();
                    double interval = interval(content, attack, hook, difficulty);
                    return new EnemySpec.Sweep(
                            Math.toRadians(
                                    hook.flatMap(EnemyData.Hook::sweepArc).orElse(sweep.arc())),
                            sweep.duration(),
                            sweep.telegraph(),
                            sweep.length(),
                            sweep.width(),
                            interval,
                            attack.firstShotDelay().orElse(0.0),
                            bulletDamage(content, attack.bullet().orElseThrow()),
                            sweep.origin().map(Point::x).orElse(0.0),
                            sweep.origin().map(Point::y).orElse(0.0));
                });
    }

    static double bulletDamage(Content content, String bullet) {
        return content.enemyBasis().bulletDamage().stream()
                .filter(b -> b.bullet().equals(bullet))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown bullet class '" + bullet + "'"))
                .damage();
    }

    static Optional<EnemyData.Hook> hook(EnemyData enemy, Difficulty difficulty) {
        return enemy.difficulty().flatMap(hooks -> switch (difficulty) {
            case EASY -> hooks.easy();
            case MEDIUM -> Optional.empty();
            case HARD -> hooks.hard();
        });
    }

    static Optional<EnemySpec.Drop> drop(EnemyData enemy) {
        if (enemy.drops().size() > 1) {
            throw new IllegalArgumentException(enemy.name() + ": only one drop rule is implemented");
        }
        return enemy.drops().stream()
                .findFirst()
                .map(drop -> new EnemySpec.Drop(SimSpecs.pickup(drop.pickup()), drop.every()));
    }

    /**
     * An enemy's attack at {@code difficulty}: its interval by the fire-rate lever (or the hook's
     * authored interval, M5 part B), its bullets by the bullet-speed lever, a level's burst change or the stat block's hooks (burst, fan size,
     * leading the target in circles, a mine's ring and whether it bursts).
     */
    private static Optional<EnemyGun> gun(
            Content content, EnemyData enemy, Difficulty difficulty, Optional<LevelData.EnemyChange> change) {
        // A spawner's spawn is its brood, a walker's spit (aimed while facing away) its second attack.
        List<EnemyData.Attack> attacks = enemy.attacks().stream()
                .filter(attack -> !attack.pattern().equals("spawn"))
                .filter(attack -> !attack.pattern().equals("laser-sweep"))
                // M9 part A: a laser-line is the unit's line (EnemySpec.laserLine, BeltRules), not a gun.
                .filter(attack -> !BeltRules.laserLine(attack))
                .filter(attack -> !attack.pattern().equals("pounce"))
                // M5 part E: a proximity ring is its own attack (EnemySpec.ring), not a gun.
                .filter(attack -> !proximityRing(attack))
                // M5 part F: a drain is its latch's (EnemySpec.latch), not a gun.
                .filter(attack -> !attack.pattern().equals(PartFRules.DRAIN))
                // M7 part B: a burrowing unit's ring is its burrow's (EnemySpec.burrow), not a gun.
                .filter(attack -> !MarsRules.burrowRing(enemy, attack))
                // M7 part C: a link (the tendril) and a pore wave are not guns; an attack fires only on its `only`.
                .filter(attack -> !TerrainRules.notAGun(attack))
                // M7 part D: an aura (the Choir Herald's) is not a gun (EnemySpec.aura, SiegeRules).
                .filter(attack -> !SiegeRules.notAGun(attack))
                // M8 part E: a slowing ring (the Siren's song) is not a gun (EnemySpec.slowRing, TrenchRules).
                .filter(attack -> attack.slow().isEmpty())
                // M8 part F: a lunging unit's ring is its lunge's (EnemySpec.lunge, DarkRules), not a gun.
                .filter(attack ->
                        enemy.lungeRing().filter(ring -> ring == attack).isEmpty())
                // M9 part C: a train's turret cars fire their attack (EnemySpec.Cars, MinefieldRules), not the engine.
                .filter(attack -> !MinefieldRules.carAttack(enemy, attack))
                // M9 part D: an air link is the unit's links (EnemySpec.airLink, TugRules), not a gun.
                .filter(attack -> !TugRules.airLink(attack))
                .filter(attack -> attack.firesOn(difficulty))
                .filter(attack ->
                        attack.away().isEmpty() || enemy.movement().walk().isEmpty())
                .toList();
        if (attacks.isEmpty()) {
            return Optional.empty();
        }
        // M8 part C (design/enemies/naval/eel-swarm): a chain head's list of attacks are its head attacks
        // (ChainRules.headAttacks), each on its own clock; the head's own gun is empty.
        if (ChainRules.headAttackPart(enemy).isPresent()) {
            return Optional.empty();
        }
        if (attacks.size() > 1) {
            throw new IllegalArgumentException(enemy.name() + ": only a single attack is implemented");
        }
        return Optional.of(gun(content, enemy, attacks.getFirst(), difficulty, change));
    }

    static EnemyGun gun(
            Content content,
            EnemyData enemy,
            EnemyData.Attack attack,
            Difficulty difficulty,
            Optional<LevelData.EnemyChange> change) {
        DifficultyData levers = content.difficulty();
        double damage = bulletDamage(content, attack.bullet().orElseThrow());
        Optional<EnemyData.Hook> hook = hook(enemy, difficulty);
        // A burst is of aimed shots: a fan or a mine keeps one volley; M8 part D: a rising unit's fan comes in its
        // burst (the Abyss Ray's three fans).
        int burst = attack.pattern().equals("aimed") || AbyssRules.fanBursts(enemy, attack)
                ? change.flatMap(LevelData.EnemyChange::burst)
                        .or(() -> hook.flatMap(EnemyData.Hook::burst))
                        // M5 part D: the stat block's own burst (the Wraith's 5).
                        .or(attack::burst)
                        .orElse(1)
                : 1;
        int fan = attack.count()
                .map(count -> hook.flatMap(EnemyData.Hook::fanCount).orElse(count))
                .orElse(1);
        Optional<EnemyGun.MineSpec> mine = attack.mine().map(spore -> new EnemyGun.MineSpec(
                        // M9 part C: hard's quicker arming of a line mine (the hook mine.arm).
                        hook.flatMap(EnemyData.Hook::mine)
                                .flatMap(EnemyData.MineChange::arm)
                                .orElse(spore.arm()),
                        spore.life(),
                        spore.drift(),
                        spore.hp(),
                        hook.flatMap(EnemyData.Hook::ring).orElse(spore.ring()),
                        bulletDamage(content, spore.ringBullet()),
                        // M9 part C: a line mine never bursts on its own.
                        !spore.lineMine()
                                && hook.flatMap(EnemyData.Hook::mineBursts).orElse(true),
                        spore.credits())
                // M9 part C (design/enemies/space/minelayer): a line mine's proximity, blink, blast and chain.
                .withLine(MinefieldRules.lineMine(content, spore)));
        Optional<EnemyGun.MortarSpec> mortar = attack.mortar()
                .map(lob -> new EnemyGun.MortarSpec(
                        lob.marker(),
                        lob.impact() / 2,
                        hook.flatMap(EnemyData.Hook::ring).orElse(lob.ring()),
                        bulletDamage(content, lob.ringBullet())));
        // M7 part D: a ring (the Choir Herald's song) is a fan round the whole circle (SiegeRules.ring).
        // M7 part E: a spiral fired while open (the Dust Devil's grit) and a pass-triggered burst (the Ghost Drone's).
        // M8 part G: a unit's homing torpedoes along its facing (the Depth Hunter's; FoundryRules.homing; a boss's
        // homing guns are BossRules').
        return unitHoming(
                enemy,
                attack,
                hook,
                StormRules.gun(
                        content,
                        enemy,
                        attack,
                        difficulty,
                        SiegeRules.ring(
                                enemy,
                                attack,
                                difficulty,
                                new EnemyGun(
                                        interval(content, attack, hook, difficulty),
                                        attack.firstShotDelay().orElse(0.0),
                                        burst,
                                        attack.speed().orElseThrow()
                                                * levers.enemyBulletSpeed().of(difficulty),
                                        damage,
                                        hook.flatMap(EnemyData.Hook::leadsTargetIn)
                                                .map(in -> in.contains("circle"))
                                                .orElse(false),
                                        fan,
                                        Math.toRadians(attack.spread().orElse(0.0)),
                                        attack.turnRate().map(Math::toRadians).orElse(Double.POSITIVE_INFINITY),
                                        attack.arc().map(Math::toRadians).orElse(Double.POSITIVE_INFINITY),
                                        mine,
                                        mortar,
                                        // M5 part D: the stat block's burst gap (the Wraith's 0.12 s).
                                        attack.burstGap().orElse(EnemyGun.BURST_GAP_SECONDS),
                                        // M5 part D: its bursts straight up the screen as a fixed fan (the Wraith's).
                                        attack.aim().filter("up"::equals).isPresent()))));
    }

    /** M8 part G: {@code gun} with a unit's homing torpedoes (not a boss's: those are its parts', BossRules). */
    private static EnemyGun unitHoming(
            EnemyData enemy, EnemyData.Attack attack, Optional<EnemyData.Hook> hook, EnemyGun gun) {
        return enemy.boss().isPresent() ? gun : FoundryRules.homing(attack, hook, gun);
    }

    static Range range(Span span) {
        return new Range(span.min(), span.max());
    }
}
