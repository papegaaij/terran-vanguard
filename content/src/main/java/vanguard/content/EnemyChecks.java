package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * A unit's checks across files: its formations, bullets, spawns and parts resolve, and its movement keys fit its
 * attacks and layer.
 */
final class EnemyChecks extends ContentChecks {
    private final BossChecks bosses;

    EnemyChecks(Content content, Problems problems, BossChecks bosses) {
        super(content, problems);
        this.bosses = bosses;
    }

    void checkEnemy(EnemyData enemy) {
        // M5 part E: a unit is under the water only in its dives (E2b's submerge), never by its stat block.
        // M8 part D: a rising unit (the Abyss Ray) enters as a shadow on sub (AbyssChecks).
        if (enemy.layer().equals(SUB) && enemy.movement().rise().isEmpty()) {
            problem(
                    enemy,
                    "layer",
                    "a unit's stat block is not on sub: it dives from ground (or rises: movement.rise)");
        }
        // M7 part B: nor under the sand: a Burrower is buried only by its burrow movement. M7 part H: a breaching
        // boss's body is (the Dust Colossus: ColossusChecks).
        if (enemy.layer().equals("buried") && !enemy.breaches()) {
            problem(enemy, "layer", "a unit's stat block is not on buried: it burrows from ground");
        }
        enemy.cloak().ifPresent(cloak -> {
            if (cloak.layer().equals(SUB)) {
                problem(enemy, "cloak.layer", "a unit does not decloak under the water");
            }
        });
        for (int i = 0; i < enemy.formations().size(); i++) {
            checkFormation(
                    enemy,
                    "formations[" + i + "].name",
                    enemy.formations().get(i).name());
        }
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            String field = "attacks[" + i + "]";
            attack.bullet().ifPresent(bullet -> checkBullet(enemy, field + ".bullet", bullet));
            boolean walker = enemy.movement().walk().isPresent();
            if (attack.stagger().isPresent() && !walker) {
                // M5 part B: only a walker wave shares a staggered volley clock so far.
                problem(enemy, field + ".stagger", "only a walker's fan is staggered");
            }
            if (walker
                    && attack.pattern().equals("fan")
                    && attack.aim().filter("down"::equals).isPresent()) {
                problem(enemy, field + ".aim", "a walker's fan aims at the target or along its facing");
            }
            if (attack.pounce().isPresent() && !walker) {
                // M5 part C: a pounce leaps off a walker's path and rejoins it.
                problem(enemy, field + ".pounce", "only a walker pounces");
            }
            attack.spawn().ifPresent(spawn -> checkEnemyName(enemy, field + ".spawn.enemy", spawn.enemy()));
            attack.mine().ifPresent(mine -> checkBullet(enemy, field + ".mine.ring_bullet", mine.ringBullet()));
            attack.mortar().ifPresent(mortar -> checkBullet(enemy, field + ".mortar.ring_bullet", mortar.ringBullet()));
            attack.sweep().flatMap(EnemyData.Sweep::origin).ifPresent(origin -> {
                if (Math.abs(origin.x()) > enemy.size().width() / 2
                        || Math.abs(origin.y()) > enemy.size().height() / 2) {
                    problem(
                            enemy,
                            field + ".sweep.origin",
                            "the eye lies outside the " + enemy.size().width() + "×"
                                    + enemy.size().height() + " sprite");
                }
            });
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            hook.flatMap(EnemyData.Hook::deathBurst)
                    .ifPresent(puff -> checkBullet(enemy, "difficulty.death_burst.bullet", puff.bullet()));
        }
        if (enemy.attacks().stream()
                        .filter(attack -> attack.spawn().isPresent())
                        .count()
                > 1) {
            // M5 part C: a brood or a periodic spawn, one per unit.
            problem(enemy, "attacks", "a unit has at most one spawn");
        }
        if (enemy.attacks().stream()
                        .filter(attack -> attack.pounce().isPresent())
                        .count()
                > 1) {
            problem(enemy, "attacks", "a unit has at most one pounce");
        }
        checkParts(enemy);
        bosses.checkBoss(enemy);
        checkCloakAndAmbush(enemy);
        checkNavalUnit(enemy);
        checkLatch(enemy);
        PartHChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message));
        TerrainChecks.enemy(enemy, (field, message) -> problem(enemy, field, message));
        SiegeChecks.enemy(enemy, (field, message) -> problem(enemy, field, message));
        StormChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message));
        FoundryChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message));
        ColossusChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message));
        MawChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message));
        RetreatChecks.enemy(enemy, (field, message) -> problem(enemy, field, message));
        enemy.difficulty()
                .flatMap(EnemyData.Hooks::hard)
                .flatMap(EnemyData.Hook::leadsTargetIn)
                .ifPresent(
                        names -> names.forEach(name -> checkFormation(enemy, "difficulty.hard.leads_target_in", name)));
    }

    private void checkBullet(Object file, String field, String bullet) {
        if (!content.enemyBasis().knowsBullet(bullet)) {
            problem(file, field, "unknown bullet class '" + bullet + "'");
        }
    }

    /**
     * A multi-part unit lists its parts: their HP and bounties add up to the unit's, a part's
     * attack names one of the unit's attacks, and it has a vital part.
     */
    private void checkParts(EnemyData enemy) {
        if (enemy.multiPart() != enemy.partList().isPresent()) {
            problem(enemy, "part_list", "a unit with parts: multi lists its parts, a single one none");
        }
        if (enemy.partList().isEmpty()) {
            return;
        }
        List<EnemyData.PartData> parts = enemy.partList().get();
        // A segment chain's body adds its segments to the parts.
        // M9 part C: a train's turret cars (every every-th car) carry their own HP and bounty.
        double hp = parts.stream().mapToDouble(EnemyData.PartData::hp).sum()
                + enemy.segmentChain()
                        .map(c -> (c.segments() - turretCars(c)) * c.hp()
                                + turretCars(c)
                                        * c.cars().map(EnemyData.Cars::hp).orElse(0.0))
                        .orElse(0.0);
        if (hp != enemy.hp()) {
            problem(enemy, "hp", "is " + enemy.hp() + ", the parts' HP add up to " + hp);
        }
        int bounty = parts.stream().mapToInt(EnemyData.PartData::bounty).sum()
                + enemy.segmentChain()
                        .map(c -> (c.segments() - turretCars(c)) * c.bounty()
                                + turretCars(c)
                                        * c.cars().map(EnemyData.Cars::bounty).orElse(0))
                        .orElse(0);
        if (bounty != enemy.bounty()) {
            problem(enemy, "bounty", "is " + enemy.bounty() + ", the parts' bounties add up to " + bounty);
        }
        if (parts.stream().noneMatch(part -> part.kind().equals("vital"))) {
            problem(enemy, "part_list", "has no vital part");
        }
        for (int i = 0; i < parts.size(); i++) {
            EnemyData.PartData part = parts.get(i);
            int index = i;
            // M8 part C: or each of its attacks (the Eel Swarm's head).
            String key = part.attacks().isPresent() ? ".attacks" : ".attack";
            part.attackNames().forEach(name -> {
                if (enemy.attack(name).isEmpty()) {
                    problem(enemy, "part_list[" + index + "]" + key, "no attack named '" + name + "'");
                }
            });
        }
    }

    /** M9 part C: a train's turret cars among its segments (every {@code every}-th car behind the engine). */
    private static int turretCars(EnemyData.SegmentChain chain) {
        return chain.cars().map(cars -> chain.segments() / cars.every()).orElse(0);
    }

    /**
     * M5 part D's units (design/enemies/air/wraith, mote-swarm): a cloak is flown on an ambush's path
     * and decloaks to another layer; an ambush unit hovers a while (its hold) and leaves at its
     * straight speed; the hard hook's hover time needs a hover; an ambush unit flies {@code rear
     * ambush}, a flock {@code swarm}, among its formations.
     */
    private void checkCloakAndAmbush(EnemyData enemy) {
        EnemyData.Movement movement = enemy.movement();
        enemy.cloak().ifPresent(cloak -> {
            if (movement.ambush().isEmpty()) {
                problem(enemy, "cloak", "a cloak is flown on an ambush's path (movement.ambush)");
            }
            if (cloak.layer().equals(enemy.layer())) {
                problem(enemy, "cloak.layer", "it decloaks to another layer than its cloaked " + enemy.layer());
            }
        });
        movement.ambush().ifPresent(ambush -> {
            if (movement.hover().flatMap(EnemyData.Hover::seconds).isEmpty()) {
                problem(enemy, "movement.ambush", "an ambush holds its hover's seconds at its hover's y");
            }
            if (movement.straight().isEmpty()) {
                problem(enemy, "movement.ambush", "an ambush leaves at its straight speed");
            }
            if (ambush.lane() > PlayField.WIDTH / 2.0) {
                problem(enemy, "movement.ambush.lane", "its exit lane lies inside the play field's nearer half");
            }
            checkFlies(enemy, "rear ambush");
        });
        movement.flock().ifPresent(flock -> checkFlies(enemy, "swarm"));
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::hoverSeconds).isPresent()
                    && movement.hover().isEmpty()) {
                problem(enemy, "difficulty.hover_seconds", "a hover time without a hover");
            }
        }
    }

    /**
     * M5 part E's unit keys (design/enemies/naval): a submerging unit is on the ground layer (the
     * sea's surface); a proximity ring is its unit's only attack, and only a ring's hook changes its
     * reach; a {@code field} unit drifts (movement {@code drift}); a boss's lanes are owned by its parts.
     */
    private void checkNavalUnit(EnemyData enemy) {
        if (enemy.submerge().isPresent() && !enemy.layer().equals("ground")) {
            problem(enemy, "submerge", "only a unit on the ground layer (the sea's surface) submerges");
        }
        boolean ring = enemy.attacks().stream().anyMatch(SimSpecs::proximityRing);
        if (ring && enemy.attacks().size() > 1) {
            problem(enemy, "attacks", "a ring fired within a reach is its unit's only attack");
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            hook.flatMap(EnemyData.Hook::attacks)
                    .ifPresent(changes -> changes.forEach((name, change) -> {
                        if (change.within().isPresent()
                                && enemy.attack(name)
                                        .filter(SimSpecs::proximityRing)
                                        .isEmpty()) {
                            problem(
                                    enemy,
                                    "difficulty.attacks." + name + ".within",
                                    "only a ring fired within a reach has one");
                        }
                    }));
        }
        if (enemy.formations().stream().anyMatch(use -> use.name().equals(LevelData.Wave.FIELD))
                && enemy.movement().drift().isEmpty()
                && enemy.movement().burrow().isEmpty()) {
            problem(enemy, "formations", "a field's units drift or burrow: give movement.drift or movement.burrow");
        }
        checkBurrow(enemy);
        enemy.boss()
                .flatMap(EnemyData.BossData::lanes)
                .flatMap(EnemyData.Lanes::arms)
                .ifPresent(arms -> arms.keySet().forEach(part -> checkPartName(enemy, "boss.lanes.arms", part)));
        bosses.checkArenaBoss(enemy);
    }

    /**
     * M7 part B's unit keys (design/enemies/ground/burrower, design/enemies/air/dust-kite): a burrowing unit is a
     * {@code ground} unit (the layer it surfaces on; {@code buried} is its current layer under the sand) whose only
     * attack is the ring it fires while surfaced (no reach); the hook {@code burrow} needs the burrow, {@code
     * swoop_speed} a triggered swoop.
     */
    private void checkBurrow(EnemyData enemy) {
        Optional<EnemyData.Burrow> burrow = enemy.movement().burrow();
        burrow.ifPresent(b -> {
            if (!enemy.layer().equals("ground")) {
                problem(enemy, "movement.burrow", "a burrowing unit surfaces on the ground layer: layer ground");
            }
            if (enemy.attacks().size() != 1
                    || !enemy.attacks().getFirst().pattern().equals("ring")
                    || enemy.attacks().getFirst().within().isPresent()
                    || enemy.attacks().getFirst().interval().isPresent()) {
                problem(
                        enemy,
                        "attacks",
                        "a burrowing unit's only attack is its ring, fired at the burrow's rings (no reach, no interval)");
            }
        });
        boolean triggered =
                enemy.movement().swoop().flatMap(EnemyData.Swoop::trigger).isPresent();
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            hook.flatMap(EnemyData.Hook::burrow).ifPresent(change -> {
                if (burrow.isEmpty()) {
                    problem(enemy, "difficulty.burrow", "a burrow change without a burrow");
                }
                double surfaced = change.surfaced()
                        .orElse(burrow.map(EnemyData.Burrow::surfaced).orElse(0.0));
                change.rings().ifPresent(rings -> rings.stream()
                        .filter(ring -> ring.at() >= surfaced)
                        .forEach(ring -> problem(
                                enemy,
                                "difficulty.burrow.rings",
                                "a ring at " + ring.at() + " s fires after the dive")));
            });
            if (hook.flatMap(EnemyData.Hook::swoopSpeed).isPresent() && !triggered) {
                problem(
                        enemy,
                        "difficulty.swoop_speed",
                        "a swoop speed without a triggered swoop (movement.swoop.trigger)");
            }
        }
    }

    /**
     * M5 part F's unit keys (design/enemies/air/lamprey): a latch comes with a chase and a drain attack,
     * a drain only with a latch, the hooks {@code drain} and {@code latch_slots} only on a unit that
     * latches; its hull spots lie near the ship (within 60 px of its centre).
     */
    private void checkLatch(EnemyData enemy) {
        boolean drain =
                enemy.attacks().stream().anyMatch(attack -> attack.pattern().equals(PartFRules.DRAIN));
        enemy.latch().ifPresent(latch -> {
            if (enemy.movement().chase().isEmpty()) {
                problem(enemy, "latch", "a unit that latches chases the ship first (movement.chase)");
            }
            if (!drain) {
                problem(enemy, "latch", "a unit that latches drains the shield (an attack with pattern drain)");
            }
            EnemyData.Spots spots = latch.spots();
            for (var spot : java.util.Map.of("left", spots.left(), "right", spots.right(), "tail", spots.tail())
                    .entrySet()) {
                if (Math.hypot(spot.getValue().x(), spot.getValue().y()) > 60) {
                    problem(enemy, "latch.spots." + spot.getKey(), "lies more than 60 px from the ship's centre");
                }
            }
        });
        if (drain && enemy.latch().isEmpty()) {
            problem(enemy, "attacks", "only a unit that latches drains (a latch block)");
        }
        if (drain && enemy.attacks().size() > 1) {
            problem(enemy, "attacks", "a drain is its unit's only attack");
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if ((hook.flatMap(EnemyData.Hook::drain).isPresent()
                            || hook.flatMap(EnemyData.Hook::latchSlots).isPresent())
                    && enemy.latch().isEmpty()) {
                problem(enemy, "difficulty", "drain and latch_slots change a latch the unit does not have");
            }
        }
    }

    /** {@code enemy}'s formations list {@code formation}. */
    private void checkFlies(EnemyData enemy, String formation) {
        if (enemy.formations().stream().noneMatch(use -> use.name().equals(formation))) {
            problem(enemy, "formations", "its movement flies '" + formation + "', which its formations do not list");
        }
    }
}
