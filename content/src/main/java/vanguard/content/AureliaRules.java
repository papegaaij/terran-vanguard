package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.WaveSpec;

/**
 * M10 part C's level rules for the simulation (design/campaign Level 37, The shuttles; design/allies, Evacuation
 * shuttle), used by {@link ScriptRules}: a level's air allies outside the objectives, lifting in groups, the waves at
 * their {@code hooked} times aiming at them.
 */
final class AureliaRules {
    private AureliaRules() {}

    /** {@code objectives} with the level's air allies on {@code difficulty} (as it is without any). */
    static ObjectiveSpec airAllies(
            Content content, LevelData level, Difficulty difficulty, List<WaveSpec> waves, ObjectiveSpec objectives) {
        if (level.airAllies().isEmpty()) {
            return objectives;
        }
        LevelData.AirAllies allies = level.airAllies().get();
        List<ObjectiveSpec.Station> stations =
                allies.stations().stream().map(PartDRules::station).toList();
        List<ObjectiveSpec.Lift> groups =
                allies.groups().stream().map(AureliaRules::lift).toList();
        ObjectiveSpec.Escort escort = ObjectiveSpec.Escort.air(
                ScriptRules.allySpec(content, allies.ally(), allies.change(difficulty)),
                ObjectiveSpec.Air.lifting(stations, groups, hooked(allies, waves))
                        .withPlayerHits(allies.playerHits().orElse(false)),
                0);
        return objectives.withAirAllies(Optional.of(escort));
    }

    /**
     * A lift group for the simulation: lifting, holding and climbing out at its climb's time, or (M10 part C, step C6a)
     * holding, sliding aside and climbing out along its edge ({@code ObjectiveSpec.Lift.holding}).
     */
    static ObjectiveSpec.Lift lift(LevelData.LiftGroup group) {
        ObjectiveSpec.Liftoff liftoff = new ObjectiveSpec.Liftoff(
                group.liftoff().t(),
                group.liftoff().seconds(),
                group.liftoff().pads().stream()
                        .map(pad -> new ObjectiveSpec.Pad(pad.x(), PlayField.HEIGHT - pad.y()))
                        .toList());
        if (group.aside().isPresent()) {
            LevelData.Aside aside = group.aside().get();
            return ObjectiveSpec.Lift.holding(
                    liftoff,
                    group.hold().orElseThrow(),
                    new ObjectiveSpec.Aside(aside.x(), aside.drop(), aside.seconds()),
                    group.climb().seconds());
        }
        return new ObjectiveSpec.Lift(
                liftoff,
                new ObjectiveSpec.Climb(group.climbStart(), group.climb().seconds()));
    }

    /** The indexes in {@code waves} (the level's on the difficulty) of the waves entering at the hooked times. */
    static List<Integer> hooked(LevelData.AirAllies allies, List<WaveSpec> waves) {
        List<Double> times = allies.hooked().orElse(List.of());
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < waves.size(); i++) {
            if (times.contains(waves.get(i).t())) {
                indexes.add(i);
            }
        }
        return indexes;
    }
}
