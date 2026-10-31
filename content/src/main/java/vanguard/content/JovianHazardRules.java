package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;

/**
 * M10 part A, step A2b (design/world/jovian, Hazards &amp; set pieces): a level's Jovian hazards for the simulation at a
 * difficulty: the told lightning {@code weather.strikes} (A1b's {@link HazardSpec.Strikes}) and the dry {@code
 * eruptions} (A1b's {@link HazardSpec.EruptionSpec}), each placed and timed by the level, its numbers the world's
 * ({@link JovianData}). A level without them flies exactly as before.
 */
final class JovianHazardRules {
    private JovianHazardRules() {}

    /**
     * The level's weather at {@code difficulty} ({@link PartFRules#weather}) with its strikes on that difficulty: a
     * level with strikes and no wind or flashes flies calm weather with them. Easy's clearance off the ship's x is the
     * world's; the entry window round a wave's entry is on every difficulty.
     */
    static Optional<HazardSpec.Weather> weather(Content content, LevelData level, Difficulty difficulty) {
        Optional<HazardSpec.Weather> weather =
                level.weather().flatMap(block -> PartFRules.weather(level, block, difficulty));
        Optional<HazardSpec.Strikes> strikes = strikes(content, level, difficulty);
        if (strikes.isEmpty()) {
            return weather;
        }
        return Optional.of(weather.orElseGet(JovianHazardRules::calm).withStrikes(strikes.get()));
    }

    /** No wind and no flashes (a level whose weather is its strikes alone). */
    private static HazardSpec.Weather calm() {
        return new HazardSpec.Weather(List.of(), 1, 0, 0, 0, List.of(), List.of());
    }

    /** The strikes flown on {@code difficulty} with the world's numbers; none without any. */
    static Optional<HazardSpec.Strikes> strikes(Content content, LevelData level, Difficulty difficulty) {
        List<HazardSpec.Strike> at = strikesOn(level, difficulty).stream()
                .map(strike -> new HazardSpec.Strike(strike.t(), strike.x(), strike.tracks()))
                .toList();
        if (at.isEmpty()) {
            return Optional.empty();
        }
        JovianData.Strike numbers = content.jovian().strike();
        return Optional.of(new HazardSpec.Strikes(
                at,
                numbers.tell(),
                numbers.live(),
                numbers.width(),
                numbers.ship(),
                numbers.enemy(),
                difficulty == Difficulty.EASY ? numbers.easyClearance() : 0,
                numbers.entry()));
    }

    /** The level's strikes told on {@code difficulty}, in time order. */
    static List<LevelData.StrikeAt> strikesOn(LevelData level, Difficulty difficulty) {
        return level.weather().flatMap(LevelData.Weather::strikes).orElse(List.of()).stream()
                .filter(strike -> strike.strikesOn(difficulty))
                .toList();
    }

    /**
     * The level's eruptions at {@code difficulty}: each vent's period (its own, else the world's) and the world's tell,
     * rocks and start speed on that difficulty; none without vents.
     */
    static Optional<HazardSpec.EruptionSpec> eruptions(Content content, LevelData level, Difficulty difficulty) {
        if (level.eruptions().isEmpty()) {
            return Optional.empty();
        }
        JovianData.Eruption numbers = content.jovian().eruption();
        Span rocks = numbers.rocksOn(difficulty);
        List<HazardSpec.Vent> vents = level.eruptions().get().stream()
                .map(vent -> {
                    Span period = vent.period().orElse(numbers.period());
                    return new HazardSpec.Vent(
                            vent.t(), vent.x(), period.min(), period.max(), (int) rocks.min(), (int) rocks.max());
                })
                .toList();
        return Optional.of(new HazardSpec.EruptionSpec(
                vents,
                numbers.tellOn(difficulty),
                numbers.rise(),
                numbers.fly(),
                numbers.fall(),
                Math.toRadians(numbers.fan()),
                numbers.speedOn(difficulty),
                numbers.endSpeed(),
                new Hitbox(numbers.rock().width(), numbers.rock().height()),
                numbers.hp(),
                numbers.damage()));
    }
}
