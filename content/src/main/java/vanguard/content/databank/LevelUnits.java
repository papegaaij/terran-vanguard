package vanguard.content.databank;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PartSpec;
import vanguard.sim.Rules;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * The units of a level at a difficulty as the simulation spawns them (step DB1, design/ui/databank, Where the data comes
 * from): the level's enemy kinds ({@link Sortie#enemyKinds()}: the waves', their leaders', the ground units', a boss's
 * streams and the broods' and launchers' units) and its set pieces (a boss among them), each by its unit slug with the
 * spec the simulation builds for it (the act HP factor, the beam era, the difficulty's levers: the content's own unit
 * rules, never a copy); the edges its waves enter by; the level's credit factor and bounty scale. Only units with a data
 * file are kept (a chain's members, an objective's pieces have none).
 *
 * @param level the level number
 * @param difficulty the difficulty
 * @param specs per unit slug, the first spec of it among the kinds (a wave's leader only when nothing else flies it)
 * @param pieces per set-piece slug (a boss's), its spec
 * @param edges per unit slug, the units its waves bring in from the front, the sides and the rear
 * @param creditFactor the difficulty's credit income times the act credit factor
 * @param bountyScale the level's bounty scale
 */
public record LevelUnits(
        int level,
        Difficulty difficulty,
        Map<String, EnemySpec> specs,
        Map<String, SetPieceSpec> pieces,
        Map<String, Edges> edges,
        double creditFactor,
        double bountyScale) {
    public LevelUnits {
        specs = Collections.unmodifiableMap(new LinkedHashMap<>(specs));
        pieces = Collections.unmodifiableMap(new LinkedHashMap<>(pieces));
        edges = Collections.unmodifiableMap(new LinkedHashMap<>(edges));
    }

    /** The units its waves bring in by edge (a cross's split over its four edges: one front, two sides, one rear). */
    public record Edges(double front, double sides, double rear) {
        Edges plus(Edges other) {
            return new Edges(front + other.front, sides + other.sides, rear + other.rear);
        }

        public double total() {
            return front + sides + rear;
        }
    }

    /** Level {@code key}'s units at {@code difficulty}. */
    public static LevelUnits of(Content content, String key, Difficulty difficulty) {
        LevelScript script = SimSpecs.level(content, key, difficulty);
        Rules rules = SimSpecs.rules(content, key, difficulty);
        Loadout loadout = SimSpecs.starterLoadout(content, difficulty);
        Sortie sortie = new Sortie(1, loadout, script, rules, loadout.plating().maxArmour());
        Set<EnemySpec> leaders = new java.util.HashSet<>();
        for (WaveSpec wave : script.waves()) {
            wave.leader().ifPresent(leaders::add);
        }
        Map<String, EnemySpec> specs = new LinkedHashMap<>();
        for (EnemySpec kind : sortie.enemyKinds()) {
            if (!content.enemies().containsKey(kind.slug())) {
                continue;
            }
            EnemySpec known = specs.get(kind.slug());
            if (known == null || (leaders.contains(known) && !leaders.contains(kind))) {
                specs.put(kind.slug(), kind);
            }
        }
        Map<String, SetPieceSpec> pieces = new LinkedHashMap<>();
        for (SetPieceSpec piece : script.setPieces()) {
            if (content.enemies().containsKey(piece.slug())) {
                pieces.putIfAbsent(piece.slug(), piece);
            }
        }
        Map<String, Edges> edges = new LinkedHashMap<>();
        for (WaveSpec wave : script.waves()) {
            String slug = wave.enemy().slug();
            if (!content.enemies().containsKey(slug)) {
                continue;
            }
            int n = wave.count();
            Edges in = wave.formation() == WaveSpec.Formation.CROSS
                    ? new Edges(n / 4.0, n / 2.0, n / 4.0)
                    : switch (wave.entry()) {
                        case FRONT -> new Edges(n, 0, 0);
                        case SIDES -> new Edges(0, n, 0);
                        case REAR -> new Edges(0, 0, n);
                    };
            edges.merge(slug, in, Edges::plus);
        }
        // The ground units scroll in from the front, or come in from a side.
        for (vanguard.sim.GroundUnit unit : script.groundUnits()) {
            String slug = unit.enemy().slug();
            if (content.enemies().containsKey(slug)) {
                edges.merge(slug, unit.side().isPresent() ? new Edges(0, 1, 0) : new Edges(1, 0, 0), Edges::plus);
            }
        }
        return new LevelUnits(
                Content.levelNumber(key),
                difficulty,
                specs,
                pieces,
                edges,
                rules.scoring().creditFactor(),
                rules.scoring().bountyScale());
    }

    /** Every unit and set-piece slug of the level, kinds first. */
    public Set<String> slugs() {
        Set<String> all = new LinkedHashSet<>(specs.keySet());
        all.addAll(pieces.keySet());
        return Collections.unmodifiableSet(all);
    }

    /** {@code slug}'s numbers here, if the level flies it. */
    public Optional<UnitNumbers> numbers(String slug) {
        SetPieceSpec piece = pieces.get(slug);
        if (piece != null) {
            List<UnitNumbers.Part> parts = piece.parts().stream()
                    .filter(part -> part.hp() > 0)
                    .map(part -> new UnitNumbers.Part(part.name(), part.hp(), part.vital(), credits(part)))
                    .toList();
            return Optional.of(new UnitNumbers(
                    level,
                    difficulty,
                    parts.stream().mapToDouble(UnitNumbers.Part::hp).sum(),
                    Optional.empty(),
                    Optional.empty(),
                    parts.stream().mapToInt(UnitNumbers.Part::bounty).sum(),
                    parts));
        }
        EnemySpec spec = specs.get(slug);
        if (spec == null) {
            return Optional.empty();
        }
        return Optional.of(new UnitNumbers(
                level,
                difficulty,
                spec.hp(),
                spec.shield().map(shield -> shield.hp()),
                spec.mirror().map(mirror -> mirror.heat()),
                credits(spec.bounty()),
                List.of()));
    }

    private int credits(PartSpec part) {
        return credits(part.bounty());
    }

    /**
     * The credits one kill of a {@code bounty} (Act 1 terms) pays here: times the credit factor and the bounty scale,
     * rounded once (the simulation's payout order, {@code Tally.bounty}).
     */
    public int credits(int bounty) {
        return (int) Math.rint(bounty * creditFactor * bountyScale);
    }
}
