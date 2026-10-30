package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.AllySpec;
import vanguard.sim.CueTrigger;
import vanguard.sim.DropSpec;
import vanguard.sim.EscortSwapSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Road;
import vanguard.sim.Secondary;
import vanguard.sim.WaveSpec;

/**
 * A level's script at a difficulty ({@link SimSpecs#level}): it gathers the waves, ground units and
 * objects, set pieces and boss from their rules classes, and maps the level's own blocks (darkness,
 * sleds, rocks, road, escort, debris, radio) on the way.
 */
final class ScriptRules {
    private static final Pattern LEVEL_KEY = Pattern.compile("act-(\\d+)-[a-z0-9-]+/level-(\\d{2})-[a-z0-9-]+");

    private ScriptRules() {}

    /** The level's script at {@code difficulty}; {@code levelKey} as in {@link Content#level(String)}. */
    static LevelScript level(Content content, String levelKey, Difficulty difficulty) {
        LevelData level = content.level(levelKey);
        // M5 part D (Level 10, user decision D5 = a): a level may have no secondary objective.
        Optional<LevelData.Secondary> secondary = level.objectives().secondary();
        Optional<LevelData.Variant> variant =
                switch (difficulty) {
                    case EASY -> level.difficulty().easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> level.difficulty().hard();
                };
        List<LevelData.PlacedPickup> carried = new ArrayList<>(level.pickups());
        variant.flatMap(LevelData.Variant::extraPickups).ifPresent(carried::addAll);
        // Part G (LevelRules): a placed pickup may be left out on a difficulty.
        carried.removeIf(placed -> !placed.dropsOn(difficulty));
        // M5 part F: a pickup placed at a time and x is no carrier's (PartFRules.placedPickups).
        List<LevelData.PlacedPickup> placed =
                carried.stream().filter(p -> !p.carried()).toList();
        carried.removeIf(p -> !p.carried());
        Map<String, LevelData.EnemyChange> enemyChanges =
                variant.flatMap(LevelData.Variant::enemies).orElse(Map.of());
        InLevel inLevel = new InLevel(levelKey(levelKey, 1), levelKey(levelKey, 2));
        List<WaveSpec> waves = new ArrayList<>();
        for (LevelData.Wave wave : level.waves()) {
            if (!wave.fliesOn(difficulty)) {
                continue;
            }
            // M8 part C (gap check D2): a wave entering under water flies its units' underwater changes.
            WaveRules.addWave(
                    content, level, wave, difficulty, enemyChanges, carried, waves, inLevel.at(level, wave.t()));
        }
        LevelScript script = LevelScript.builder(levelKey(levelKey, 2), levelKey(levelKey, 1))
                .launchSeconds(level.launchSeconds())
                // M5 part G: a timed halt's sections with their ramp; M7 part B: a moving section's ease.
                .sections(MarsRules.sections(content, level))
                .waves(waves)
                .groundObjects(GroundRules.groundObjects(level, difficulty, carried))
                .groundUnits(GroundRules.groundUnits(content, level, difficulty, inLevel))
                .secrets(level.secrets().size())
                .radio(radio(content, level, difficulty))
                .setPieces(java.util.stream.Stream.concat(
                                level.setPieces().orElse(List.of()).stream()
                                        .map(piece -> BossRules.setPiece(content, piece, difficulty, inLevel)),
                                // M8 part H: a boss under water flies its shed units' underwater changes.
                                level
                                        .boss()
                                        .map(boss ->
                                                BossRules.boss(content, boss, difficulty, inLevel.at(level, boss.t())))
                                        .stream())
                        .toList())
                .objectives(new ObjectiveSpec(
                        secondary
                                .map(objective -> LevelRules.secondary(content, level, objective, difficulty))
                                .orElse(Secondary.NONE),
                        level.objectives().targets().orElse(List.of()),
                        // M9 part D (Level 31): a hull escort's unhooked waves by their index in the waves.
                        level.objectives()
                                .escort()
                                .map(escort -> TugRules.unhooked(
                                        escort(content, escort, difficulty), escort, waves, difficulty)),
                        level.objectives().defend().map(defend -> PartGRules.defend(content, defend, difficulty)),
                        level.convoy().map(convoy -> PartERules.convoy(content, level, convoy, difficulty)),
                        // M7 part D: the ground allies come after (SiegeRules).
                        Optional.empty(),
                        LevelRules.tows(level),
                        PartCRules.holds(level, difficulty),
                        PartCRules.collapse(level)))
                // M9 part C (Level 30): a level without a ground layer (`ground: none`): bomb deliveries idle.
                // M9 part E (Level 32): a forked terrain from the start (the branch waves' check needs its forks).
                .ground(DeepCoreRules.forkedTerrain(
                        level,
                        difficulty,
                        level.noGround()
                                ? GroundSpec.SPACE
                                : GroundSpec.NONE
                                        .withRoad(level.road().map(road -> road(level, road)))
                                        .withWater(level.overWater())))
                .hazards(HazardSpec.NONE
                        .withCranes(GroundRules.cranes(level, difficulty))
                        .withDebris(debris(level, difficulty))
                        .withSled(level.sleds().map(sleds -> sled(sleds, difficulty)))
                        .withRocks(level.rocks()
                                .filter(rocks -> difficulty != Difficulty.EASY
                                        || rocks.onEasy().orElse(false))
                                .map(ScriptRules::rocks))
                        .withDarkness(level.darkness().map(darkness -> darkness(darkness, difficulty)))
                        .withWeather(level.weather().flatMap(weather -> PartFRules.weather(level, weather, difficulty)))
                        .withSondes(level.sondes().map(PartFRules::sondes))
                        // M8 part H: the finale's cruiser run (Level 28).
                        .withCruisers(MawRules.cruisers(content, level, difficulty))
                        // M9 part H: the mass-relay flash (Level 35).
                        .withRelay(RingRules.relay(level, difficulty)))
                .drops(DropSpec.NONE
                        .withGroupDrops(groupDrops(level, carried))
                        .withPartDrops(LevelRules.partDrops(content, level, carried, SimSpecs::pickup))
                        .withDropFactor(level.drops())
                        // M5 part G: and the pickups released at a point (Level 13's supply drones).
                        .withPlacedPickups(PartGRules.placedPickups(
                                PartFRules.placedPickups(placed, SimSpecs::pickup),
                                level.released(),
                                SimSpecs::pickup)))
                // M8 part A: the level's medium and its special limits (air and none before Act 4).
                .medium(WaterRules.medium(content, level))
                .specialLimits(WaterRules.specialLimits(content, level))
                // M8 part F: Rook's scripted loss (Level 26).
                .rookLoss(DarkRules.rookLoss(level))
                // M8 part H: the armour floor from the boss's kill (Level 28's run cannot be lost).
                .armourFloor(level.armourFloor().orElse(0.0))
                // M9 part B (Level 29, the rescue halt): the held arena's halt cycle and the escort swap.
                .haltCycle(BeltRules.haltCycle(content, level, difficulty, enemyChanges, inLevel))
                .escortSwap(level.escortSwap()
                        .map(swap -> new EscortSwapSpec(
                                swap.at().x(), PlayField.HEIGHT - swap.at().y(), swap.join())))
                .build();
        // M7 part B: the no-surface zones and the drop launch (Level 15); M7 part C: the canyon and its gates (Level
        // 16); M7 part D: the ground allies (Level 17's domes).
        // M7 part F: the conveyor belts and the furnace vents (Level 19, FoundryRules).
        return FoundryRules.script(
                content,
                level,
                difficulty,
                SiegeRules.script(
                        content,
                        level,
                        difficulty,
                        TerrainRules.script(
                                level,
                                difficulty,
                                MarsRules.script(level, script),
                                slug -> UnitRules.enemy(content, slug, difficulty, Optional.empty(), inLevel))));
    }

    /** A dark level's light at {@code difficulty} (Level 06): easy's longer headlight and flares, the flares it fires. */
    static HazardSpec.Darkness darkness(LevelData.Darkness darkness, Difficulty difficulty) {
        boolean easy = difficulty == Difficulty.EASY;
        LevelData.Headlight headlight = darkness.headlight();
        LevelData.FlareFall fall = darkness.flare();
        return new HazardSpec.Darkness(
                headlight.from(),
                easy ? headlight.easy().orElse(headlight.length()) : headlight.length(),
                Math.toRadians(headlight.angle()),
                darkness.flares().stream()
                        .filter(flare -> flare.firedOn(difficulty))
                        .map(flare -> new HazardSpec.Darkness.Flare(flare.t(), flare.x(), flare.y()))
                        .toList(),
                easy ? fall.easySeconds().orElse(fall.seconds()) : fall.seconds(),
                fall.radius(),
                fall.drift(),
                darkness.lights().orElse(List.of()).stream()
                        .map(light -> new HazardSpec.Darkness.Light(light.t(), light.x(), light.radius()))
                        .toList(),
                darkness.ambient());
    }

    /** The level's sleds at {@code difficulty}: the period of easy or hard. */
    static HazardSpec.SledSpec sled(LevelData.Sleds sleds, Difficulty difficulty) {
        Optional<LevelData.SledChange> change =
                switch (difficulty) {
                    case EASY -> sleds.easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> sleds.hard();
                };
        return new HazardSpec.SledSpec(
                sleds.x(),
                sleds.width(),
                sleds.first(),
                change.map(LevelData.SledChange::period).orElse(sleds.period()),
                sleds.until(),
                sleds.lights(),
                sleds.run(),
                sleds.damage(),
                sleds.clamp().orElse(""));
    }

    private static HazardSpec.RockSpec rocks(LevelData.Rocks rocks) {
        return new HazardSpec.RockSpec(
                rocks.count().min(),
                rocks.count().max(),
                rocks.speed().min(),
                rocks.speed().max(),
                rocks.life(),
                SimSpecs.hitbox(rocks.size()),
                rocks.hp(),
                rocks.damage(),
                rocks.clearance());
    }

    /**
     * The pickups a ground-target group's last unit drops when the group is cleared (Level 05's
     * battery C); not a destructible group's (M5 part E, carried by its destructibles).
     */
    private static List<DropSpec.GroupDrop> groupDrops(LevelData level, List<LevelData.PlacedPickup> carried) {
        List<String> groups = level.objectives().groups();
        return carried.stream()
                .filter(p -> p.carrier().group().filter(groups::contains).isPresent())
                .map(p -> new DropSpec.GroupDrop(
                        groups.indexOf(p.carrier().group().get()), SimSpecs.pickup(p.pickup())))
                .toList();
    }

    /**
     * The level's road for the simulation: each point's {@code t} turned into the ground distance
     * that passes the middle of the screen then.
     */
    static Road road(LevelData level, LevelData.Road road) {
        List<LevelData.RoadPoint> points = road.points();
        double[] distances = new double[points.size()];
        double[] xs = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            distances[i] = level.scrollAt(points.get(i).t());
            xs[i] = points.get(i).x();
        }
        return new Road(road.width(), distances, xs);
    }

    /**
     * The escort objective's convoy at {@code difficulty}: the ally's spec with the level's HP for it;
     * M5 part D: an air escort's stations, liftoff, climb-out and scripted loss ({@link PartDRules}).
     */
    static ObjectiveSpec.Escort escort(Content content, LevelData.Escort escort, Difficulty difficulty) {
        AlliesData.Ally ally = content.allies().allies().get(escort.ally());
        if (ally == null) {
            throw new IllegalArgumentException("no ally '" + escort.ally() + "'");
        }
        Optional<LevelData.AllyChange> change =
                switch (difficulty) {
                    case EASY -> escort.easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> escort.hard();
                };
        AllySpec spec = new AllySpec(
                escort.ally(),
                SimSpecs.hitbox(ally.size()),
                SimSpecs.hitbox(ally.hitbox()),
                change.map(LevelData.AllyChange::hp).orElse(ally.hp()),
                ally.damagedBy().objectiveAimed(),
                ally.damagedBy().claws(),
                ally.smokeBelow(),
                (ally.headings().count() - 1) / 2 * ally.headings().step(),
                ally.damagedBy().hitByBullets(),
                ally.damagedBy().hitByContact(),
                ally.banks().map(AlliesData.Banks::frames).orElse(0),
                ally.banks().map(AlliesData.Banks::full).orElse(0.0),
                ally.glide().orElse(0.0));
        if (escort.air()) {
            return ObjectiveSpec.Escort.air(spec, PartDRules.air(escort), escort.credits());
        }
        if (escort.hull().isPresent()) {
            // M9 part D (Level 31): the mining tug's hull escort (TugRules).
            return TugRules.hull(ally, escort, spec, difficulty);
        }
        LevelData.Enter enter = escort.enter().orElseThrow();
        return new ObjectiveSpec.Escort(
                spec,
                escort.y().orElseThrow().stream().map(y -> PlayField.HEIGHT - y).toList(),
                enter.t(),
                enter.interval(),
                enter.speed(),
                escort.credits(),
                escort.hook().orElseThrow().enemies());
    }

    /** The level's debris chunks at {@code difficulty}: every second large one left out on easy, faster on hard. */
    private static List<HazardSpec.DebrisSpec> debris(LevelData level, Difficulty difficulty) {
        if (level.debris().isEmpty()) {
            return List.of();
        }
        LevelData.DebrisField field = level.debris().get();
        double factor = field.driftFactor(difficulty);
        return field.placedOn(difficulty).stream()
                .map(placed -> {
                    LevelData.Chunk chunk = field.chunks().get(placed.chunk());
                    if (chunk == null) {
                        throw new IllegalArgumentException("no debris chunk '" + placed.chunk() + "'");
                    }
                    return new HazardSpec.DebrisSpec(
                            placed.t(),
                            placed.x(),
                            placed.chunk(),
                            SimSpecs.hitbox(chunk.size()),
                            placed.drift().x() * factor,
                            placed.drift().y() * factor,
                            chunk.hp().orElse(Double.POSITIVE_INFINITY),
                            chunk.damage().orElse(0.0),
                            field.clearance());
                })
                .toList();
    }

    /** A number of the level key: group 1 is the act, group 2 the level. */
    static int levelKey(String key, int group) {
        Matcher matcher = LEVEL_KEY.matcher(key);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a level key: " + key);
        }
        return Integer.parseInt(matcher.group(group));
    }

    private static List<RadioCue> radio(Content content, LevelData level, Difficulty difficulty) {
        List<RadioCue> cues = new ArrayList<>();
        for (LevelData.RadioCue cue : level.radio()) {
            Optional<LevelData.RadioChange> change =
                    switch (difficulty) {
                        case EASY -> cue.easy();
                        case MEDIUM -> Optional.empty();
                        case HARD -> cue.hard();
                    };
            CueTrigger trigger = cue.event()
                    .map(event -> switch (event) {
                        case FIRST_KILL -> CueTrigger.FIRST_KILL;
                        case SECONDARY_OBJECTIVE -> CueTrigger.SECONDARY_OBJECTIVE;
                        case LEVEL_END -> CueTrigger.LEVEL_END;
                        case GROUP_CLEARED -> CueTrigger.GROUP_CLEARED;
                        case GROUP_LOST -> CueTrigger.GROUP_LOST;
                        case FIRST_GROUP_LOST -> CueTrigger.FIRST_GROUP_LOST;
                        case ENEMY_ESCAPED -> CueTrigger.ENEMY_ESCAPED;
                        case FIRST_ALLY_HIT -> CueTrigger.FIRST_ALLY_HIT;
                        case FIRST_ALLY_LOST -> CueTrigger.FIRST_ALLY_LOST;
                        case ESCORT_FIRST_KILL -> CueTrigger.ESCORT_FIRST_KILL;
                        case MISSION_FAILED -> CueTrigger.MISSION_FAILED;
                        case BOSS_PHASE -> CueTrigger.BOSS_PHASE;
                        case BOSS_DESTROYED -> CueTrigger.BOSS_DESTROYED;
                        case HOLD_START -> CueTrigger.HOLD_START;
                        case FIRST_POUNCE -> CueTrigger.FIRST_POUNCE;
                        case COLLAPSE -> CueTrigger.COLLAPSE;
                        case ALLY_LOST -> CueTrigger.ALLY_LOST;
                        case SCRIPTED_LOSS -> CueTrigger.SCRIPTED_LOSS;
                        case FIRST_DECLOAK -> CueTrigger.FIRST_DECLOAK;
                        case FIRST_LOOP_BACK -> CueTrigger.FIRST_LOOP_BACK;
                        case ALLY_HIT -> CueTrigger.ALLY_HIT;
                        case FIRST_TELEGRAPH -> CueTrigger.FIRST_TELEGRAPH;
                        case FIRST_SHIP_TELEGRAPH -> CueTrigger.FIRST_SHIP_TELEGRAPH;
                        case BOSS_PART_DESTROYED -> CueTrigger.BOSS_PART_DESTROYED;
                        case FIRST_LATCH -> CueTrigger.FIRST_LATCH;
                        case ALLY_BELOW -> CueTrigger.ALLY_BELOW;
                        case FIRST_SURFACING -> CueTrigger.FIRST_SURFACING;
                        // M7 part C: the canyon's first wall touch (Level 16).
                        case FIRST_WALL_CONTACT -> CueTrigger.FIRST_WALL_CONTACT;
                        // M7 part D, round 40 K.a: the first Herald's first ring (Level 17).
                        case FIRST_RING -> CueTrigger.FIRST_RING;
                        // M8 part B: the first geyser's tell and the first rig destroyed (Level 22).
                        case FIRST_GEYSER -> CueTrigger.FIRST_GEYSER;
                        case TARGET_DESTROYED -> CueTrigger.TARGET_DESTROYED;
                        // M8 part C (gap check D5): the waterline plays as a timed line at its time (Level 23's 25.5
                        // s).
                        case WATERLINE -> CueTrigger.TIME;
                        // M8 part D (gap check §5 D2): the first Ray's shadow (the simulation's sighting); its time
                        // below is its wave's, for the radio timeline (Level 24's 58 s).
                        case FIRST_RAY -> CueTrigger.FIRST_RAY;
                        // M8 part H (gap check §5 H8): the boss's arrival and a coiled boss's dive (Level 28).
                        case BOSS_ARRIVAL -> CueTrigger.BOSS_ARRIVAL;
                        case BOSS_DIVE -> CueTrigger.BOSS_DIVE;
                        // M9 part B (Level 29): the first SAM salvo, the wavering craft, the clamps, Rook's join.
                        case FIRST_HOMING -> CueTrigger.FIRST_HOMING;
                        case NEUTRAL_LEFT -> CueTrigger.NEUTRAL_LEFT;
                        case NEUTRAL_DOWN -> CueTrigger.NEUTRAL_DOWN;
                        case HOLDERS_DOWN -> CueTrigger.HOLDERS_DOWN;
                        case ESCORT_JOINED -> CueTrigger.ESCORT_JOINED;
                        // M9 part E (Level 32): a fork's branch chosen, the first topple's crush.
                        case BRANCH -> CueTrigger.BRANCH;
                        case TOPPLE_CRUSH -> CueTrigger.TOPPLE_CRUSH;
                        // M9 part G (Level 34): the Helix core collected or missed.
                        case STORY_COLLECTED -> CueTrigger.STORY_COLLECTED;
                        case STORY_MISSED -> CueTrigger.STORY_MISSED;
                        // Step BOSS70 (Level 35): the core's first rail volley, mine ring, wreck throw; its low share.
                        case FIRST_RAIL_VOLLEY, FIRST_MINE_RING, FIRST_WRECK_THROW -> CueTrigger.CORE_PLAY;
                        case CORE_LOW -> CueTrigger.CORE_LOW;
                    })
                    .orElse(CueTrigger.TIME);
            // Part G (LevelRules): a boss-destroyed cue's subject is the boss, a timeout cue its own
            // trigger, and the requirements bits (a special, a homing weapon; or none of them).
            cues.add(new RadioCue(
                    LevelRules.trigger(cue, trigger),
                    cue.t()
                            .or(() -> cue.event()
                                    .filter(LevelData.CueEvent.WATERLINE::equals)
                                    .flatMap(event -> level.firstWaterline()))
                            .or(() -> cue.event()
                                    .filter(LevelData.CueEvent.FIRST_RAY::equals)
                                    .flatMap(event -> AbyssChecks.firstRise(content, level)))
                            .orElse(0.0),
                    // M5 part E: a boss-part-destroyed cue waits for any of its parts.
                    cue.parts()
                            .map(parts -> String.join(CueTrigger.PART_SEPARATOR, parts))
                            // M5 part G: an ally-below cue's subject is its share.
                            .or(() -> cue.below().map(PartGRules::share))
                            .orElseGet(() -> LevelRules.subject(level, cue)),
                    cue.speaker(),
                    change.map(LevelData.RadioChange::line).orElse(cue.line()),
                    cue.distorted().orElse(false),
                    cue.expression().orElse(Expression.NEUTRAL).slug(),
                    cue.portrait().orElse(cue.speaker()),
                    false,
                    cue.allies().map(LevelData.Count::min).orElse(0),
                    cue.allies().map(LevelData.Count::max).orElse(Integer.MAX_VALUE),
                    LevelRules.requirement(cue.requires()),
                    LevelRules.requirement(cue.requiresNot())));
        }
        for (LevelData.Secret secret : level.secrets()) {
            LevelData.RadioLine line = secret.radio();
            cues.add(new RadioCue(
                    CueTrigger.SECRET,
                    0,
                    secret.name(),
                    line.speaker(),
                    line.line(),
                    line.distorted().orElse(false),
                    line.expression().orElse(Expression.NEUTRAL).slug(),
                    // M7 part B: a generic portrait for a speaker without one of his own (Level 15's Ares Control).
                    line.portrait().orElse(line.speaker()),
                    false,
                    0,
                    Integer.MAX_VALUE,
                    // M9 part F: Level 33's secret line is Rook's: it needs him flying (requires: escort).
                    LevelRules.requirement(line.requires()),
                    0));
        }
        return cues;
    }
}
