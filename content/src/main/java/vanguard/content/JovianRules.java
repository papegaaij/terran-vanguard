package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.WaveSpec;

/**
 * M10 part A, Act 6's unit rules for the simulation ({@link UnitRules}; design/enemies, Reflect, Regeneration, The
 * three-way war; the Graft Turret's regrowth, the Shield Pylon's dome, the Harrow's death-burst on contact, the IFF
 * spoofer's flag): the stat block's {@code mirror}, {@code regeneration}, {@code regrowth}, {@code dome}, {@code
 * spoofable} and {@code death-burst} attack at a difficulty, a placed pylon's crown and a wave's prey link.
 */
final class JovianRules {
    private JovianRules() {}

    /** The dome's bearing with no {@code crown}: straight up the screen (a heading clockwise from straight down). */
    static final double CROWN_UP = Math.PI;

    /**
     * {@code spec} with the unit's Act 6 keys at {@code difficulty}: its mirror (the hook's arc and turn; the heat by the
     * HP lever and a returning unit's act factor, as a shield's), its regeneration (the hook's rate and delay), its
     * regrowth (the hook's window and knit; the root's HP as the unit's: the lever and the act factor, the beam era's
     * included), its dome (the hook's radius and breaker drop; its crown straight up until a placement turns it:
     * {@link #crowned}), its spoofer flag and its {@code death-burst} ring (the bullet-speed lever, or the hook's
     * authored speed); a unit without them as it is.
     */
    static EnemySpec unit(
            Content content, EnemyData enemy, Difficulty difficulty, UnitRules.InLevel inLevel, EnemySpec spec) {
        Optional<EnemyData.Attack> ring =
                enemy.attacks().stream().filter(EnemyData.Attack::deathBurst).findFirst();
        if (enemy.mirror().isEmpty()
                && enemy.regeneration().isEmpty()
                && enemy.regrowth().isEmpty()
                && enemy.dome().isEmpty()
                && !enemy.isSpoofable()
                && ring.isEmpty()) {
            return spec;
        }
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        EnemySpec out = spec.withMirror(enemy.mirror().map(mirror -> mirror(content, enemy, difficulty, inLevel, hook)))
                .withRegeneration(enemy.regeneration().map(regeneration -> {
                    Optional<EnemyData.RegenerationChange> change = hook.flatMap(EnemyData.Hook::regeneration);
                    return new EnemySpec.Regeneration(
                            change.flatMap(EnemyData.RegenerationChange::rate).orElse(regeneration.rate()),
                            change.flatMap(EnemyData.RegenerationChange::delay).orElse(regeneration.delay()));
                }))
                .withRegrowth(enemy.regrowth().map(regrowth -> {
                    Optional<EnemyData.RegrowthChange> change = hook.flatMap(EnemyData.Hook::regrowth);
                    return new EnemySpec.Regrowth(
                            change.flatMap(EnemyData.RegrowthChange::window).orElse(regrowth.window()),
                            change.flatMap(EnemyData.RegrowthChange::knit).orElse(regrowth.knit()),
                            regrowth.wake(),
                            content.difficulty()
                                    .enemyHp(
                                            regrowth.root().hp() * UnitRules.actHpFactor(content, enemy, inLevel),
                                            difficulty),
                            SimSpecs.hitbox(regrowth.root().hitbox()),
                            regrowth.root().bounty());
                }))
                .withDome(enemy.dome().map(dome -> {
                    Optional<EnemyData.DomeChange> change = hook.flatMap(EnemyData.Hook::dome);
                    return new EnemySpec.Dome(
                            change.flatMap(EnemyData.DomeChange::radius).orElse(dome.radius()),
                            dome.offset(),
                            CROWN_UP,
                            change.flatMap(EnemyData.DomeChange::breakerDrop).orElse(dome.breakerDrop()),
                            dome.back());
                }))
                .withSpoofable(enemy.isSpoofable());
        return ring.isEmpty()
                ? out
                : out.withDeathBurst(Optional.of(deathBurst(content, ring.get(), hook, difficulty)));
    }

    private static EnemySpec.Mirror mirror(
            Content content,
            EnemyData enemy,
            Difficulty difficulty,
            UnitRules.InLevel inLevel,
            Optional<EnemyData.Hook> hook) {
        EnemyData.Mirror mirror = enemy.mirror().orElseThrow();
        Optional<EnemyData.MirrorChange> change = hook.flatMap(EnemyData.Hook::mirror);
        return new EnemySpec.Mirror(
                change.flatMap(EnemyData.MirrorChange::arc).orElse(mirror.arc()),
                content.difficulty()
                        .enemyHp(mirror.heat() * UnitRules.returningFactor(content, enemy, inLevel), difficulty),
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

    /**
     * A {@code death-burst} attack as the unit's ring: its count, its speed by the bullet-speed lever (or the hook's
     * {@code attacks.<name>.speed}, as authored), its bullet class, on contact (with its grace) and turned between.
     */
    static EnemySpec.DeathBurst deathBurst(
            Content content, EnemyData.Attack attack, Optional<EnemyData.Hook> hook, Difficulty difficulty) {
        Optional<Double> authored = hook.flatMap(EnemyData.Hook::attacks)
                .flatMap(changes -> attack.name().map(changes::get))
                .flatMap(EnemyData.AttackChange::speed);
        return new EnemySpec.DeathBurst(
                attack.count().orElseThrow(),
                authored.orElseGet(() -> attack.speed().orElseThrow()
                        * content.difficulty().enemyBulletSpeed().of(difficulty)),
                UnitRules.bulletDamage(content, attack.bullet().orElseThrow()),
                attack.onContact().orElse(false),
                attack.between().orElse(false),
                attack.grace().orElse(0.0));
    }

    /**
     * A placed unit of {@code target} with its dome's crown turned by the target's {@code crown} (° from straight up the
     * screen, positive to the right; the bearing the simulation's dome takes, {@link EnemySpec.Dome}); a unit without a
     * dome, or a target without a crown, as it is.
     */
    static EnemySpec crowned(LevelData.GroundTarget target, EnemySpec unit) {
        if (unit.dome().isEmpty() || target.crown().isEmpty()) {
            return unit;
        }
        EnemySpec.Dome dome = unit.dome().get();
        return unit.withDome(Optional.of(new EnemySpec.Dome(
                dome.radius(),
                dome.offset(),
                CROWN_UP + Math.toRadians(target.crown().get()),
                dome.breakerDropSeconds(),
                dome.backSeconds())));
    }

    /**
     * The three-way war (design/enemies): a wave whose {@code hunts} names another wave's {@code tag} sends its units at
     * that prey wave ({@link WaveSpec#hunts()}); Level 33's {@code hunts: prey} stays the leeches' role ({@link
     * PartFRules#preyRole}); a wave without either as it is.
     */
    static WaveSpec wave(LevelData.Wave wave, WaveSpec spec) {
        return wave.hunts()
                .filter(JovianRules::preyTag)
                .map(tag -> spec.toBuilder().hunts(tag).build())
                .orElse(spec);
    }

    /** Whether a wave's {@code hunts} names a prey wave's tag (Act 6), not Level 33's {@code prey} role. */
    static boolean preyTag(String hunts) {
        return !hunts.equals(PartFRules.HUNTS_PREY);
    }
}
