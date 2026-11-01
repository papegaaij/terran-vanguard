package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, Vorne; Level 42's radio; m10h-gaps.md §1 #13): a boss's radio
 * subject raised once in phase {@code phase} ({@link PhaseCues}; the {@code boss-phase} cue with this subject and {@link
 * SimEvents.Type#BOSS_CUE}) at the first of: {@code deaths} of its {@code parts} dead, its parts' HP below {@code below}
 * of their whole, or {@code seconds} into the phase (the Ascendant's {@code first-generator}: a generator's death or 25
 * s; {@code cannon-half}: the cannon below half). 0 leaves a trigger out.
 *
 * @param subject the radio subject
 * @param phase the phase it may come in (0 first)
 * @param parts the parts it watches
 * @param deaths how many of them dead raise it; 0 for none
 * @param below their HP share below which it is raised; 0 for none
 * @param seconds the time into the phase that raises it; 0 for none
 */
public record PhaseCue(String subject, int phase, List<Integer> parts, int deaths, double below, double seconds) {
    public PhaseCue {
        parts = List.copyOf(parts);
        if (subject.isEmpty()
                || phase < 0
                || deaths < 0
                || deaths > parts.size()
                || !(below >= 0 && below <= 1)
                || !(seconds >= 0)
                || (deaths == 0 && below == 0 && seconds == 0)
                || ((deaths > 0 || below > 0) && parts.isEmpty())) {
            throw new IllegalArgumentException("a phase cue has a subject, a phase and a trigger: " + subject);
        }
    }
}
