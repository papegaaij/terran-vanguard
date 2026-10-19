package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import vanguard.sim.AllySpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;

/**
 * M7 part D's rules for the simulation (step D4; design/campaign Level 17 Dome Siege, design/allies/ares-landing-dome,
 * design/enemies/air/choir-herald, design/enemies/ground/shellback; round 36's M8 = a, M16 = a, Q.17, Q.22d/g,
 * Q.25b/c and the gap check's defaults pending the user's review in round 40): the level's {@code allies} block as
 * the simulation's ground allies (the domes and the hook per unit type), the {@code untouched} secondary, the Choir
 * Herald's {@code aura} and its song (a ring as a full-circle fan), the Shellback's unit part (the mortar vent) and
 * its curl-roll.
 */
final class SiegeRules {
    /** The pattern of an aura attack (the Choir Herald's): the aura, not a gun. */
    static final String AURA = "aura";

    /** The pattern of a ring fired as a gun (the Choir Herald's song): a fan round the whole circle. */
    static final String RING = "ring";

    private SiegeRules() {}

    /** Whether {@code attack} is not a gun: an {@code aura} ({@link #aura}). */
    static boolean notAGun(EnemyData.Attack attack) {
        return attack.pattern().equals(AURA) || attack.aura().isPresent();
    }

    /**
     * The level's ground allies at {@code difficulty} (the Ares Landing domes): each unit's ally with its footprint
     * (a unit's {@code size}, Eden's 144 px) and hit box, its hits (the difficulty's {@code hits}, else the ally's)
     * as its HP, the hits a bomb run, a lob and a roll deal; the hook's modes on that difficulty (a {@code none} left
     * out), the bomb row and slots. Empty without an {@code allies} block.
     */
    static Optional<LevelScript.GroundAllies> allies(Content content, LevelData level, Difficulty difficulty) {
        return level.allies().map(allies -> {
            Optional<Integer> hits = allies.change(difficulty).flatMap(LevelData.AlliesChange::hits);
            List<LevelScript.GroundAlly> units = new ArrayList<>();
            for (LevelData.GroundAlly unit : allies.units()) {
                AlliesData.Ally ally = content.allies().allies().get(unit.ally());
                if (ally == null) {
                    throw new IllegalArgumentException("no ally '" + unit.ally() + "'");
                }
                units.add(new LevelScript.GroundAlly(
                        ally(unit, ally, hits),
                        unit.name(),
                        unit.at().x(),
                        unit.at().t()));
            }
            Map<String, LevelScript.HookMode> modes = new TreeMap<>();
            allies.modes(difficulty).forEach((slug, mode) -> {
                if (mode != LevelData.HookMode.NONE) {
                    modes.put(slug, PartGRules.mode(mode));
                }
            });
            return new LevelScript.GroundAllies(units, modes, allies.bombRow(), allies.bombSlot());
        });
    }

    /** A ground ally's spec: its footprint and hit box, {@code hits} (or the ally's own) as its HP, its damage rules. */
    private static AllySpec ally(LevelData.GroundAlly unit, AlliesData.Ally ally, Optional<Integer> hits) {
        if (!ally.standing()) {
            throw new IllegalArgumentException(
                    "a level's ground ally follows the ground: '" + unit.ally() + "' does not");
        }
        AlliesData.DamagedBy damage = ally.damagedBy();
        return new AllySpec(
                unit.ally(),
                SimSpecs.hitbox(unit.size(ally)),
                SimSpecs.hitbox(unit.hitbox(ally)),
                hits.map(Integer::doubleValue).orElse(ally.hp()),
                damage.objectiveAimed(),
                0,
                ally.smokeBelow(),
                0,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                damage.diveDamage(),
                damage.hitByLobs(),
                damage.hitsPerBomb(),
                damage.hitsPerRoll());
    }

    /** {@code script} with the level's ground allies, when it has them. */
    static LevelScript script(Content content, LevelData level, Difficulty difficulty, LevelScript script) {
        Optional<LevelScript.GroundAllies> allies = allies(content, level, difficulty);
        return allies.isPresent() ? script.withGroundAllies(allies) : script;
    }

    /** The {@code untouched} secondary (Level 17's "No dome hit"): failed at the first hit on any ground ally. */
    static Optional<LevelScript.Secondary> secondary(LevelData.Secondary secondary) {
        if (!secondary.keepsAlliesUntouched()) {
            return Optional.empty();
        }
        return Optional.of(LevelScript.Secondary.untouched(
                secondary.credits(), secondary.label().orElseThrow()));
    }

    /**
     * {@code gun} as a ring (the Choir Herald's song): the hook's {@code attacks.<name>.count} (hard's 12) instead of
     * the stat block's, spread round the circle ({@code 2π × (n − 1) / n}, so the n bullets lie evenly apart); another
     * attack's gun as it is.
     */
    static EnemyGun ring(EnemyData enemy, EnemyData.Attack attack, Difficulty difficulty, EnemyGun gun) {
        if (!attack.pattern().equals(RING)) {
            return gun;
        }
        int count = attack.name()
                .flatMap(name -> SimSpecs.hook(enemy, difficulty)
                        .flatMap(EnemyData.Hook::attacks)
                        .map(changes -> changes.get(name)))
                .flatMap(EnemyData.AttackChange::count)
                .orElse(attack.count()
                        .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a ring has a count")));
        return new EnemyGun(
                gun.intervalSeconds(),
                gun.firstShotDelay(),
                gun.burst(),
                gun.bulletSpeed(),
                gun.damage(),
                gun.leadsTargetInCircle(),
                count,
                2 * Math.PI * (count - 1) / count,
                gun.turnRate(),
                gun.arcRadians(),
                gun.mine(),
                gun.mortar(),
                gun.burstGapSeconds(),
                gun.up());
    }

    /**
     * A unit's body (its {@code vital} part) when it carries a unit part ({@link #part}): its HP and bounty are the
     * unit's in the simulation (the Shellback's 120 and 40); empty for another unit.
     */
    static Optional<EnemyData.PartData> body(EnemyData enemy) {
        if (!carriesPart(enemy)) {
            return Optional.empty();
        }
        return enemy.partList().orElseThrow().stream()
                .filter(part -> part.kind().equals("vital"))
                .findFirst();
    }

    /** Whether it is a unit (not a boss, not a chain) whose part list holds a destroyable part (the Shellback's vent). */
    private static boolean carriesPart(EnemyData enemy) {
        return enemy.tier() != Tier.HUGE
                && enemy.segmentChain().isEmpty()
                && enemy.boss().isEmpty()
                && enemy.partList().isPresent();
    }

    /**
     * {@code spec} with the stat block's aura (the Choir Herald's, the hook's radius and refill; the ring at most
     * the unit's HP, so {@code share} must be 1), its unit part (the Shellback's vent: its HP by the HP lever and the
     * act factor, its bounty, its offset and hit box, silencing the gun when it fires the unit's attack; the body's
     * {@code first_bonus}) and its curl-roll (the hook's roll time). A unit without them is returned as it is.
     */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, double actHp, EnemySpec spec) {
        Optional<EnemyData.Hook> hook = SimSpecs.hook(enemy, difficulty);
        EnemySpec out = spec;
        Optional<EnemyData.Aura> aura = enemy.aura();
        if (aura.isPresent()) {
            out = out.withAura(Optional.of(aura(aura.get(), hook.flatMap(EnemyData.Hook::aura), enemy)));
        }
        if (carriesPart(enemy)) {
            out = out.withPart(Optional.of(part(content, enemy, difficulty, actHp)));
        }
        Optional<EnemyData.Roll> roll = enemy.roll();
        if (roll.isPresent()) {
            out = out.withRoll(Optional.of(roll(roll.get(), hook.flatMap(EnemyData.Hook::roll))));
        }
        return out;
    }

    private static EnemySpec.Aura aura(EnemyData.Aura aura, Optional<EnemyData.AuraChange> change, EnemyData enemy) {
        if (aura.share() != 1) {
            throw new IllegalArgumentException(enemy.name() + ": an aura's ring is 1x the unit's HP (share: 1)");
        }
        return new EnemySpec.Aura(
                change.flatMap(EnemyData.AuraChange::radius).orElse(aura.radius()),
                aura.cap(),
                change.flatMap(EnemyData.AuraChange::refill).orElse(aura.refill()),
                aura.refillDelay(),
                aura.fill(),
                aura.keep(),
                aura.collapse(),
                aura.escortRadius());
    }

    private static EnemySpec.UnitPart part(Content content, EnemyData enemy, Difficulty difficulty, double actHp) {
        List<EnemyData.PartData> parts = enemy.partList().orElseThrow();
        List<EnemyData.PartData> vital =
                parts.stream().filter(p -> p.kind().equals("vital")).toList();
        List<EnemyData.PartData> destroyable =
                parts.stream().filter(p -> p.kind().equals("destroyable")).toList();
        if (vital.size() != 1 || destroyable.size() != 1 || parts.size() != 2) {
            throw new IllegalArgumentException(enemy.name() + ": a unit's parts are its body (vital) and one part");
        }
        EnemyData.PartData body = vital.getFirst();
        EnemyData.PartData part = destroyable.getFirst();
        return new EnemySpec.UnitPart(
                part.name(),
                content.difficulty().enemyHp(part.hp() * actHp, difficulty),
                SimSpecs.hitbox(part.hitbox()),
                part.offset().x(),
                part.offset().y(),
                part.bounty(),
                part.attack().isPresent(),
                body.firstBonus().orElse(0));
    }

    private static EnemySpec.Roll roll(EnemyData.Roll roll, Optional<EnemyData.RollChange> change) {
        return new EnemySpec.Roll(
                roll.below(),
                roll.curl(),
                change.flatMap(EnemyData.RollChange::seconds).orElse(roll.seconds()),
                roll.speed(),
                roll.uncurl(),
                roll.bellySeconds(),
                roll.bellyFactor(),
                new Hitbox(roll.ball().width(), roll.ball().height()));
    }
}
