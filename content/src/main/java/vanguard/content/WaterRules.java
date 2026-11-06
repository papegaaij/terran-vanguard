package vanguard.content;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import vanguard.sim.MediumSpec;
import vanguard.sim.SpecialLimits;
import vanguard.sim.SpecialSpec;

/**
 * M8 part A: a level's medium and its special limits for the simulation ({@link SimSpecs}): the under-water rules of
 * design/world/europa/data.yaml ({@code underwater}), the level's {@code medium} and its sections' (a waterline is a
 * section's start, the level clock's end of the one before), and the level's and its sections' {@code
 * special_limits}. A level without water flies {@link MediumSpec#AIR} and {@link SpecialLimits#NONE}, so every level
 * before Act 4 builds the same script as before.
 */
final class WaterRules {
    private WaterRules() {}

    /** The under-water rules' numbers and their weapon lists (design/world/europa, Under water rules). */
    static MediumSpec.Rules rules(Content content) {
        EuropaData.Underwater water = content.europa().underwater();
        return new MediumSpec.Rules(
                water.bulletSpeed(),
                water.acceleration(),
                water.damage(),
                water.energyDamage(),
                water.reach(),
                water.torpedoRange(),
                water.ease(),
                Set.copyOf(water.energyWeapons()),
                Set.copyOf(water.torpedoWeapons()));
    }

    /**
     * The level's medium: a switch where a section's medium differs from the one before it (at 0 s when the first
     * section is under water); {@link MediumSpec#AIR} when no part of the level is.
     */
    static MediumSpec medium(Content content, LevelData level) {
        if (!level.underwater()) {
            return MediumSpec.AIR;
        }
        List<MediumSpec.Switch> switches = new ArrayList<>();
        boolean under = false;
        for (int i = 0; i < level.sections().size(); i++) {
            boolean now = level.mediumOf(i) == SectionsBlock.Medium.UNDERWATER;
            if (now != under) {
                switches.add(new MediumSpec.Switch(start(level, i), now));
                under = now;
            }
        }
        return new MediumSpec(rules(content), switches);
    }

    /**
     * The debug option's medium ({@code --medium underwater[@<s>]}, design D1 of M8 part A): a level flown under water
     * from {@code atSeconds} on its clock (from its start at 0, without an ease).
     */
    static MediumSpec underwaterFrom(Content content, double atSeconds) {
        return new MediumSpec(rules(content), List.of(new MediumSpec.Switch(atSeconds, true)));
    }

    /**
     * The level's and its sections' lists of forbidden specials: a switch where a section's list (its own, else the
     * level's) differs from the one before it; {@link SpecialLimits#NONE} when no list is given anywhere (the specials'
     * own settings decide by the medium). M9 part A: an underground section without its own list forbids the
     * underground setting's specials, which read {@code IN ROCK}. M11 part A, step A2b: a level beyond the gate
     * ({@code beyond_gate: true}) without its own list forbids the beyond-gate setting's, which read {@code NO COVER}.
     */
    static SpecialLimits specialLimits(Content content, LevelData level) {
        List<SpecialLimits.Switch> switches = new ArrayList<>();
        Optional<Set<SpecialSpec.Kind>> before = Optional.empty();
        SpecialLimits.Reason reasonBefore = SpecialLimits.Reason.NONE;
        for (int i = 0; i < level.sections().size(); i++) {
            Optional<Set<SpecialSpec.Kind>> now = level.specialLimitsOf(i).map(slugs -> kinds(content, slugs));
            SpecialLimits.Reason reason = SpecialLimits.Reason.NONE;
            if (now.isEmpty() && level.undergroundOf(i)) {
                // M9 part A (design/player/specials, Act 5 specials): inside rock the underground setting's list,
                // read IN ROCK.
                now = Optional.of(kinds(
                        content,
                        content.specials().forbids(Optional.of(SpecialsData.Setting.UNDERGROUND)).stream()
                                .map(SpecialsData::slug)
                                .toList()));
                reason = SpecialLimits.Reason.IN_ROCK;
            } else if (now.isEmpty() && level.beyondTheGate()) {
                // M11 part A, step A2b (design/player/specials, Act 7 specials): beyond the gate the beyond-gate
                // setting's list, read NO COVER (the Sonar Pulse's NO WATER stays the simulation's dry rule).
                now = Optional.of(kinds(
                        content,
                        content.specials().forbids(Optional.of(SpecialsData.Setting.BEYOND_GATE)).stream()
                                .map(SpecialsData::slug)
                                .toList()));
                reason = SpecialLimits.Reason.NO_COVER;
            }
            if (!Objects.equals(now, before) || reason != reasonBefore) {
                switches.add(new SpecialLimits.Switch(start(level, i), now, reason));
                before = now;
                reasonBefore = reason;
            }
        }
        return switches.isEmpty() ? SpecialLimits.NONE : new SpecialLimits(switches);
    }

    /** The specials of these slugs ({@code orbital-lance}) as the simulation's kinds. */
    private static Set<SpecialSpec.Kind> kinds(Content content, List<String> slugs) {
        Set<SpecialSpec.Kind> kinds = new LinkedHashSet<>();
        for (String slug : slugs) {
            kinds.add(LoadoutRules.kind(content.specials().named(slug)));
        }
        return kinds;
    }

    /** Section {@code index}'s start on the level clock: the end of the one before, 0 for the first. */
    private static double start(LevelData level, int index) {
        return index == 0 ? 0 : level.sections().get(index - 1).end();
    }
}
