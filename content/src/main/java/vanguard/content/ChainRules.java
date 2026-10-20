package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;

/**
 * A segment chain for the simulation (design/enemies/air/coilwyrm): the head's chain of tapering
 * segments, its tail and its regrown head, each part a unit of its own.
 */
final class ChainRules {
    private ChainRules() {}

    /**
     * A segment chain's head with its chain (design/enemies/air/coilwyrm): the segments taper
     * evenly from the first to the last size, with hit boxes their share of it; the members follow
     * one another their spacing × their mean length apart along the head's path (the head as long
     * as its sprite, the tail as its hit box over the share); the tail and the regrown head from the
     * part list and the regrow block, the regrown head firing the head's fan (the hook's count).
     */
    static EnemySpec withChain(Content content, EnemyData enemy, EnemySpec head, Difficulty difficulty, double actHp) {
        EnemyData.SegmentChain chain = enemy.segmentChain().orElseThrow();
        // M7 part C: a chain whose cut rear part dies (the Threadcrawler's cut) has no regrow.
        Optional<EnemyData.Regrow> regrow = chain.regrow();
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        int segments = hook.flatMap(EnemyData.Hook::segments).orElse(chain.segments());
        EnemyData.PartData tail = enemy.partList().orElseThrow().stream()
                .filter(part -> !part.kind().equals("vital"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a chain has a tail part"));
        double small = content.enemyBasis().contactDamage().get(chain.contact());
        boolean rammed = chain.contact().compareTo(Tier.SMALL) <= 0;
        List<Hitbox> boxes = new ArrayList<>();
        List<Double> lengths = new ArrayList<>();
        lengths.add(enemy.size().height());
        for (int i = 0; i < segments; i++) {
            double t = segments == 1 ? 0 : (double) i / (segments - 1);
            double size =
                    chain.size().get(0) + (chain.size().get(1) - chain.size().get(0)) * t;
            // M7 part C: a ground chain's segment is wider across its path than along it (the Threadcrawler's).
            boxes.add(new Hitbox(chain.width().orElse(size) * chain.hitboxShare(), size * chain.hitboxShare()));
            lengths.add(size);
        }
        lengths.add(tail.hitbox().height() / chain.hitboxShare());
        List<Double> offsets = new ArrayList<>();
        offsets.add(0.0);
        for (int i = 1; i < lengths.size(); i++) {
            offsets.add(offsets.get(i - 1) + chain.spacing() * (lengths.get(i - 1) + lengths.get(i)) / 2);
        }
        String slug = head.slug();
        EnemySpec segment = part(
                content,
                slug + "-segment",
                head,
                chain.hp() * actHp,
                boxes.getFirst(),
                small,
                rammed,
                chain.bounty(),
                Optional.empty(),
                head.speed(),
                difficulty);
        EnemySpec tailSpec = part(
                content,
                slug + "-tail",
                head,
                tail.hp() * actHp,
                SimSpecs.hitbox(tail.hitbox()),
                small,
                rammed,
                tail.bounty(),
                Optional.empty(),
                head.speed(),
                difficulty);
        Optional<EnemyGun> fan = head.gun().map(gun -> hook.flatMap(EnemyData.Hook::regrownFanCount)
                .map(count -> new EnemyGun(
                        gun.intervalSeconds(),
                        gun.firstShotDelay(),
                        gun.burst(),
                        gun.bulletSpeed(),
                        gun.damage(),
                        gun.leadsTargetInCircle(),
                        count,
                        gun.spreadRadians(),
                        gun.turnRate(),
                        gun.arcRadians(),
                        gun.mine(),
                        gun.mortar()))
                .orElse(gun));
        EnemySpec regrown = regrow.map(grow -> part(
                        content,
                        slug + "-regrown",
                        head,
                        grow.hp() * actHp,
                        head.hitbox(),
                        head.contactDamage(),
                        false,
                        grow.bounty(),
                        fan,
                        grow.speed(),
                        difficulty))
                .orElse(null);
        EnemySpec.ChainSpec spec = new EnemySpec.ChainSpec(
                segment,
                boxes,
                tailSpec,
                tail.firstBonus().orElse(0),
                regrown,
                regrow.map(EnemyData.Regrow::seconds).orElse(0.0),
                regrow.map(EnemyData.Regrow::speed).orElse(0.0),
                offsets,
                chain.popInterval(),
                enemy.partList().orElseThrow().stream()
                        .filter(part -> part.kind().equals("vital"))
                        .findFirst()
                        .flatMap(EnemyData.PartData::multiplier)
                        .orElse(1.0),
                // M7 part C: a head-first kill pays the whole (the Threadcrawler's head first_bonus).
                enemy.partList().orElseThrow().stream()
                        .filter(part -> part.kind().equals("vital"))
                        .findFirst()
                        .flatMap(EnemyData.PartData::firstBonus)
                        .orElse(0));
        return new EnemySpec(
                head.slug(),
                head.hp(),
                head.hitbox(),
                head.layer(),
                head.contactDamage(),
                head.destroyedByRamming(),
                head.bounty(),
                head.speed(),
                head.snake(),
                head.streamSpeed(),
                head.hover(),
                head.orbit(),
                head.gun(),
                head.drop(),
                head.dive(),
                head.terrain(),
                head.spiral(),
                head.strafe(),
                head.deathBurst(),
                head.sine(),
                head.brood(),
                head.walker(),
                head.sideHover(),
                head.sweep(),
                Optional.of(spec),
                head.hardened(),
                head.spawner(),
                head.pounce());
    }

    /** A chain's part as a unit of its own: its HP by the HP lever, on the head's layer. */
    private static EnemySpec part(
            Content content,
            String slug,
            EnemySpec head,
            double hp,
            Hitbox box,
            double contactDamage,
            boolean rammed,
            int bounty,
            Optional<EnemyGun> gun,
            double speed,
            Difficulty difficulty) {
        return new EnemySpec(
                slug,
                content.difficulty().enemyHp(hp, difficulty),
                box,
                head.layer(),
                contactDamage,
                rammed,
                bounty,
                speed,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                gun,
                Optional.empty(),
                Optional.empty(),
                false);
    }
}
