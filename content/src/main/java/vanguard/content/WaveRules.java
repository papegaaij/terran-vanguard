package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.EnemySpec;
import vanguard.sim.WaveSpec;

/**
 * A level's air waves for the simulation ({@link SimSpecs#level}): each group of a wave at the difficulty
 * with its formation, entry, carried pickups and a field wave's area.
 */
final class WaveRules {
    private WaveRules() {}

    static void addWave(
            Content content,
            LevelData level,
            LevelData.Wave wave,
            Difficulty difficulty,
            Map<String, LevelData.EnemyChange> enemyChanges,
            List<LevelData.PlacedPickup> carried,
            List<WaveSpec> out,
            InLevel inLevel) {
        Optional<LevelData.Change> change =
                switch (difficulty) {
                    case EASY -> wave.easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> wave.hard();
                };
        LevelData.Entry entry = change.flatMap(LevelData.Change::from).orElse(wave.from());
        Optional<LevelData.Edge> edge = change.flatMap(LevelData.Change::edge).or(wave::edge);
        Optional<Double> warning = change.flatMap(LevelData.Change::warning).or(wave::warning);
        int breakGroup = change.flatMap(LevelData.Change::breakGroup)
                .or(wave::breakGroup)
                .orElse(1);
        List<LevelData.Group> groups = wave.groupList();
        for (int g = 0; g < groups.size(); g++) {
            LevelData.Group group = groups.get(g);
            // An authored count replaces the formation size lever.
            // M9 part B: a wave on rocks keeps its rocks × riders on every difficulty.
            int count = wave.onRocks().isPresent()
                    ? group.count()
                    : change.flatMap(LevelData.Change::count)
                            .orElseGet(() -> content.difficulty().formationSize(group.count(), difficulty));
            boolean first = g == 0;
            boolean last = g == groups.size() - 1;
            // M7 part D: a carrier naming its enemy is that enemy's first group's (Level 17's t=124 Choir Herald).
            int ofEnemy = g;
            for (int h = 0; h < g; h++) {
                if (groups.get(h).enemy().equals(group.enemy())) {
                    ofEnemy = h;
                    break;
                }
            }
            boolean enemyFirst = ofEnemy == g;
            List<WaveSpec.Carried> pickups = carried.stream()
                    .filter(p -> p.carrier().waveT() == wave.t())
                    .filter(p -> p.carrier()
                            .enemy()
                            .map(slug -> slug.equals(group.enemy()) && enemyFirst)
                            .orElse(p.carrier().unit() == LevelData.CarrierUnit.LAST ? last : first))
                    .map(p -> new WaveSpec.Carried(
                            SimSpecs.pickup(p.pickup()),
                            // M8 part F: or a field's unit nearest its centre (Level 26's constellation).
                            DarkRules.carrier(p.carrier().unit()),
                            // M8 part C (Level 23's shield cell): only a head-first kill of the chain drops it.
                            p.carrier().dropsHeadFirst()))
                    .toList();
            EnemySpec mapped = UnitRules.enemy(
                    content, group.enemy(), difficulty, Optional.ofNullable(enemyChanges.get(group.enemy())), inLevel);
            // M7 part C: a crawl wave's chain crawls its ground path (the Threadcrawler).
            EnemySpec unit = wave.crawl()
                    .map(path -> TerrainRules.crawl(content, content.enemy(group.enemy()), difficulty, mapped, path))
                    .orElse(mapped);
            WaveSpec spec = PartGRules.lanesAndPairs(
                    wave,
                    change,
                    new WaveSpec(
                                    wave.t(),
                                    formation(group.formation()),
                                    unit,
                                    count,
                                    // M5 part G: a cross enters from all four edges, by its lanes; M8 part E: a
                                    // wall's nests at the front, with the ground.
                                    TrenchRules.entry(wave, PartGRules.entry(entry)),
                                    edge.map(WaveRules::edge).orElse(WaveSpec.Edge.NONE),
                                    change.flatMap(LevelData.Change::hold).or(wave::hold),
                                    warning,
                                    breakGroup,
                                    wave.speed(),
                                    wave.interval(),
                                    pickups,
                                    wave.at().map(at -> new WaveSpec.At(at.x(), at.y())),
                                    wave.paths().orElse(List.of()).stream()
                                            .map(path -> path.stream()
                                                    .map(point -> new WaveSpec.At(point.x(), point.y()))
                                                    .toList())
                                            .toList(),
                                    wave.loopBack()
                                            .map(back -> new WaveSpec.LoopBack(
                                                    back.after(),
                                                    back.path().orElse(List.of()).stream()
                                                            .map(point -> new WaveSpec.At(point.x(), point.y()))
                                                            .toList(),
                                                    // M5 part D: a swarm's loop-backs, a difficulty's loops instead.
                                                    change.flatMap(LevelData.Change::loops)
                                                            .orElse(back.count().orElse(1)))),
                                    wave.tag().orElse(""),
                                    field(wave, unit))
                            // M5 part F: a circle's sense of orbit (Level 12's eye).
                            .clockwise(wave.turnsClockwise()));
            // M7 part G: a pursuit wave's block (Level 20's Mote Swarm, RetreatRules); M8 part E: a cave's or the
            // walls'
            // mount (TrenchRules).
            // M9 part B (Level 29): a V-wing's leader at its tip, a wave's rocks with their riders (BeltRules).
            // M9 part C (Level 30): a mine layers' wave's lines, set last (MinefieldRules).
            out.add(MinefieldRules.wave(
                    wave,
                    BeltRules.wave(
                            content,
                            level,
                            wave,
                            group.enemy(),
                            difficulty,
                            inLevel,
                            TrenchRules.mount(
                                    wave,
                                    wave.pursues()
                                            ? spec.withPursuit(
                                                    RetreatRules.pursuit(content, wave, group.enemy(), difficulty))
                                            : spec))));
        }
    }

    /**
     * M5 part E: a {@code field} wave's area (design/enemies/naval/driftjelly): its {@code x} and
     * {@code size}, its {@code spacing} (1.5 × the unit's hit box width if not given) and its
     * {@code current}; none for another wave.
     */
    static Optional<WaveSpec.Field> field(LevelData.Wave wave, EnemySpec unit) {
        if (wave.size().isEmpty()) {
            return Optional.empty();
        }
        Size size = wave.size().get();
        return Optional.of(new WaveSpec.Field(
                wave.x().orElseThrow(),
                size.width(),
                size.height(),
                wave.spacing().orElse(fieldSpacing(unit.hitbox().width())),
                Math.toRadians(wave.current().orElse(0.0))));
    }

    /** M5 part E: a field's default spacing for units of hit box width {@code width}: 1.5 × it. */
    static double fieldSpacing(double width) {
        return 1.5 * width;
    }

    private static WaveSpec.Edge edge(LevelData.Edge edge) {
        return switch (edge) {
            case LEFT -> WaveSpec.Edge.LEFT;
            case RIGHT -> WaveSpec.Edge.RIGHT;
            case ALTERNATING -> WaveSpec.Edge.ALTERNATING;
        };
    }

    private static WaveSpec.Formation formation(String name) {
        return switch (name) {
            case "snake" -> WaveSpec.Formation.SNAKE;
            case "V-wing" -> WaveSpec.Formation.V_WING;
            case "line abreast" -> WaveSpec.Formation.LINE_ABREAST;
            case "stream" -> WaveSpec.Formation.STREAM;
            case "pincer" -> WaveSpec.Formation.PINCER;
            case "circle" -> WaveSpec.Formation.CIRCLE;
            case "single" -> WaveSpec.Formation.SINGLE;
            // M8 part E: a wall's nests (the Sirens of Level 25), one by one on their walls (WallMounts).
            case "turret nest" -> WaveSpec.Formation.SINGLE;
            case "column" -> WaveSpec.Formation.COLUMN;
            case "convoy" -> WaveSpec.Formation.CONVOY;
            case "whirl cluster" -> WaveSpec.Formation.WHIRL_CLUSTER;
            case "carrier + escorts" -> WaveSpec.Formation.CARRIER_ESCORTS;
            case "pack" -> WaveSpec.Formation.PACK;
            case "swarm" -> WaveSpec.Formation.SWARM;
            case "rear ambush" -> WaveSpec.Formation.REAR_AMBUSH;
            case LevelData.Wave.FIELD -> WaveSpec.Formation.FIELD;
            case "cross" -> WaveSpec.Formation.CROSS;
            // M7 part G: built as a swarm, then given its pursuit block (withPursuit sets the formation).
            case LevelData.Wave.PURSUIT -> WaveSpec.Formation.SWARM;
            default -> throw new IllegalArgumentException("the formation '" + name + "' is not implemented yet");
        };
    }
}
