package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.Range;

/**
 * M8 part D's units for the simulation (design/enemies/naval/abyss-ray, barbfin; gap check m8d-gaps.md §1.2–1.3): the
 * Abyss Ray's {@code rise} and its fans in a burst, the Barbfin's hover {@code above_ship} with its drift. The spawning
 * reef's fry need no mapping of their own: its periodic spawn of a flock kind (the Glassfin) is the Hive Node's spawner,
 * and the simulation releases the fry as single rammers; the {@code first-ray} cue is {@link ScriptRules}'.
 */
final class AbyssRules {
    /**
     * A hover above the ship's hover depth below the top edge, px: never higher than its min; a path from the top edge
     * ends at its max at the latest (the simulation's {@code EnemySpec.Hover#aboveShip}): 460 is 84 px above the
     * ship's start row, below the lowest stop of 90 px above it.
     */
    static final Range ABOVE_SHIP_DEPTH = new Range(60, 460);

    private AbyssRules() {}

    /**
     * A stat block's hover with its time {@code seconds}: at its {@code y}, or above the ship with its drift; M8 part G:
     * at its {@code y} with its drift (the Depth Hunter's).
     */
    static EnemySpec.Hover hover(EnemyData.Hover hover, Range seconds) {
        // Step FIX7 (design/enemies/air/hornet): a drifting hold kept apart from the others like it.
        return hoverAt(hover, seconds).keptApart(hover.keepApart().orElse(false));
    }

    private static EnemySpec.Hover hoverAt(EnemyData.Hover hover, Range seconds) {
        return hover.aboveShip()
                .map(above -> new EnemySpec.Hover(seconds, ABOVE_SHIP_DEPTH)
                        .aboveShip(UnitRules.range(above), hover.drift().orElse(0.0)))
                .orElseGet(() -> new EnemySpec.Hover(
                        seconds,
                        UnitRules.range(hover.heightFromTop()),
                        Optional.empty(),
                        hover.drift().orElse(0.0)));
    }

    /** Whether {@code attack} is a rising unit's fan, which comes in its {@code burst} (the Abyss Ray's). */
    static boolean fanBursts(EnemyData enemy, EnemyData.Attack attack) {
        return enemy.movement().rise().isPresent() && attack.pattern().equals("fan");
    }

    /** {@code spec} with the stat block's rise (the Abyss Ray's); a unit without one as it is. */
    static EnemySpec unit(EnemyData enemy, EnemySpec spec) {
        Optional<EnemyData.Rise> rise = enemy.movement().rise();
        if (rise.isEmpty()) {
            return spec;
        }
        EnemyData.Rise data = rise.get();
        return spec.withRise(Optional.of(
                new EnemySpec.Rise(data.shadow(), data.rise(), data.glide(), data.speed(), data.sink(), data.ahead())));
    }
}
