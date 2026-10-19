package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Terrain;

/**
 * M7 part C's rules for the simulation (design/campaign Level 16 Valles Canyon Run, design/enemies/ground/
 * tendril-anchor, design/enemies/ground/threadcrawler; round 36's M1–M3, M9 = a, MA.1 = b, Q.16a; the gap check's
 * defaults pending the user's review in round 39): the level's {@code terrain} (the walls' and islands' collision
 * lines and the contact), its linked nests' gates ({@code link: pairs}), an anchor's tendril ({@code link}), a
 * Threadcrawler wave's {@code crawl} path with its pore wave and cut.
 */
final class TerrainRules {
    private TerrainRules() {}

    /** Whether {@code attack} is not a gun: a {@code link} (the tendril, {@link #anchor}) or a pore wave ({@link #crawl}). */
    static boolean notAGun(EnemyData.Attack attack) {
        return attack.link().isPresent() || attack.pores().isPresent();
    }

    /** The level's canyon as the simulation's {@link Terrain}: the walls' foot lines and the islands; heights not. */
    static Terrain terrain(TerrainBlock block) {
        return new Terrain(
                block.left().map(TerrainRules::wall),
                block.right().map(TerrainRules::wall),
                block.islands().orElse(List.of()).stream()
                        .map(TerrainRules::island)
                        .toList());
    }

    private static Terrain.Wall wall(List<TerrainBlock.WallPoint> points) {
        double[] scrolls = new double[points.size()];
        double[] xs = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            scrolls[i] = points.get(i).scroll();
            xs[i] = points.get(i).x();
        }
        return new Terrain.Wall(scrolls, xs);
    }

    private static Terrain.Island island(TerrainBlock.Island island) {
        List<Point> points = island.points();
        double[] xs = new double[points.size()];
        double[] scrolls = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xs[i] = points.get(i).x();
            scrolls[i] = points.get(i).y();
        }
        return new Terrain.Island(xs, scrolls);
    }

    /** The level's terrain and its contact at {@code difficulty} (easy's 6, else 10), or empty without one. */
    static Optional<LevelScript.TerrainSpec> terrain(LevelData level, Difficulty difficulty) {
        return level.terrain().map(block -> {
            TerrainBlock.Contact contact = block.contact();
            return new LevelScript.TerrainSpec(
                    terrain(block),
                    contact.damage().of(difficulty),
                    contact.push(),
                    contact.pushSeconds(),
                    contact.cooldown());
        });
    }

    /** Whether a ground target is a linked nest (the gates' anchor pairs), mapped as {@link LevelScript.Gate}s. */
    static boolean linked(LevelData.GroundTarget target) {
        return target.link().isPresent();
    }

    /**
     * {@code spec} with the stat block's {@code link} (the Tendril Anchor's tendril): its HP by the HP lever, its
     * dormancy, its touch as the attack's bullet class, its cooldown, band and burnt bounty. A unit without a link
     * is returned as it is.
     */
    static EnemySpec anchor(Content content, EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        Optional<EnemyData.Attack> attack =
                enemy.attacks().stream().filter(a -> a.link().isPresent()).findFirst();
        if (attack.isEmpty()) {
            return spec;
        }
        EnemyData.Link link = attack.get().link().orElseThrow();
        return spec.withLink(Optional.of(new EnemySpec.Link(
                content.difficulty().enemyHp(link.hp(), difficulty),
                link.dormant(),
                SimSpecs.bulletDamage(content, attack.get().bullet().orElseThrow()),
                link.cooldown(),
                link.band(),
                link.burntBounty())));
    }

    /**
     * The level's gates at {@code difficulty}: each linked nest's placements (the difficulty's list) paired in order,
     * left before right at the same time, with the nest's unit ({@code anchors} maps a slug to its spec).
     */
    static List<LevelScript.Gate> gates(LevelData level, Difficulty difficulty, Function<String, EnemySpec> anchors) {
        List<LevelScript.Gate> gates = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (!linked(target)) {
                continue;
            }
            EnemySpec anchor = anchors.apply(target.enemy().orElseThrow());
            List<LevelData.Placement> at =
                    switch (difficulty) {
                        case EASY ->
                            target.easy().flatMap(LevelData.Placements::at).orElse(target.at());
                        case MEDIUM -> target.at();
                        case HARD ->
                            target.hard().flatMap(LevelData.Placements::at).orElse(target.at());
                    };
            if (at.size() % 2 != 0) {
                throw new IllegalArgumentException(target.target() + ": a linked nest's placements pair up");
            }
            for (int i = 0; i < at.size(); i += 2) {
                LevelData.Placement left = at.get(i);
                LevelData.Placement right = at.get(i + 1);
                if (left.t() != right.t()) {
                    throw new IllegalArgumentException(target.target() + ": a pair's anchors enter together");
                }
                gates.add(new LevelScript.Gate(left.t(), left.x(), right.x(), anchor));
            }
        }
        return gates;
    }

    /**
     * {@code head} (a ground chain's head, its chain mapped) crawling the wave's {@code path} (points {@code [scroll,
     * x]} in the walls' rows) at the stat block's {@code crawl} speed, its segments firing the pore wave: the attack's
     * interval, the hook's rest and every other pore instead, its glow, its bullets by the bullet-speed lever, the
     * stat block's cut delay.
     */
    static EnemySpec crawl(
            Content content,
            EnemyData enemy,
            Difficulty difficulty,
            EnemySpec head,
            List<TerrainBlock.GroundPoint> path) {
        EnemyData.Crawl crawl = enemy.movement()
                .crawl()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a crawl wave's unit crawls"));
        if (head.chain().isEmpty()) {
            throw new IllegalArgumentException(enemy.name() + ": a crawling unit is a segment chain");
        }
        EnemyData.Attack attack = enemy.attacks().stream()
                .filter(a -> a.pores().isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a crawling chain fires its pores"));
        EnemyData.Pores pores = attack.pores().orElseThrow();
        Optional<EnemyData.PoresChange> change =
                SimSpecs.hook(enemy, difficulty).flatMap(EnemyData.Hook::pores);
        double[] scrolls = new double[path.size()];
        double[] xs = new double[path.size()];
        for (int i = 0; i < path.size(); i++) {
            scrolls[i] = path.get(i).scroll();
            xs[i] = path.get(i).x();
        }
        double cut = enemy.segmentChain()
                .flatMap(EnemyData.SegmentChain::cut)
                .map(EnemyData.Cut::delay)
                .orElse(1.0);
        return head.withCrawl(Optional.of(new EnemySpec.Crawl(
                crawl.speed(),
                scrolls,
                xs,
                new EnemySpec.PoreWave(
                        attack.interval().orElseThrow(),
                        change.flatMap(EnemyData.PoresChange::rest).orElse(pores.rest()),
                        pores.glow(),
                        attack.speed().orElseThrow()
                                * content.difficulty().enemyBulletSpeed().of(difficulty),
                        SimSpecs.bulletDamage(content, attack.bullet().orElseThrow()),
                        change.flatMap(EnemyData.PoresChange::everyOther).orElse(pores.alternate())),
                cut)));
    }

    /** {@code script} with the level's terrain and gates, when it has them. */
    static LevelScript script(
            LevelData level, Difficulty difficulty, LevelScript script, Function<String, EnemySpec> anchors) {
        LevelScript out = script;
        Optional<LevelScript.TerrainSpec> terrain = terrain(level, difficulty);
        if (terrain.isPresent()) {
            out = out.withTerrain(terrain);
        }
        List<LevelScript.Gate> gates = gates(level, difficulty, anchors);
        if (!gates.isEmpty()) {
            out = out.withGates(gates);
        }
        return out;
    }
}
