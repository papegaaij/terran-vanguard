package vanguard.content;

import java.util.ArrayList;
import java.util.List;

/**
 * The scroll and the atmosphere along a level's back-to-back sections ({@link LevelData#scrollAt}, {@link
 * LevelData#atmosphereStretches(List, boolean)}): the ground layer's scroll distance by level time with a timed halt's
 * and a speed change's eases, and the atmosphere stretches around the sections' peaks.
 */
final class LevelScroll {
    private LevelScroll() {}

    /** As {@link LevelData#scrollAt}: the ground layer's scroll distance at {@code t} over back-to-back {@code sections}. */
    static double scrollAt(List<LevelData.Section> sections, double scrollSpeed, double t) {
        double scroll = 0;
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            double start = sectionStart(sections, i);
            if (t < section.end() || i == sections.size() - 1) {
                return scroll + travelled(sections, scrollSpeed, i, start, t);
            }
            scroll += travelled(sections, scrollSpeed, i, start, section.end());
        }
        throw new IllegalStateException("a level has sections");
    }

    /** The time section {@code index} (0-based) starts. */
    private static double sectionStart(List<LevelData.Section> sections, int index) {
        return index == 0 ? 0 : sections.get(index - 1).end();
    }

    /** M5 part G: the px section {@code i} scrolls from {@code from} to {@code to} s (inside it, or past its ends). */
    private static double travelled(
            List<LevelData.Section> sections, double scrollSpeed, int i, double from, double to) {
        LevelData.Section section = sections.get(i);
        double speed = speedOf(section, scrollSpeed);
        double start = sectionStart(sections, i);
        double end = section.end();
        double px = (to - from) * speed;
        if (speed == 0 && section.held() && i > 0) {
            // M9 part B (Level 29, the rescue halt): into a held arena the scroll eases down over the whole section
            // from the speed before (smoothstep), as the simulation's LevelClock: before × (1 - S(u)).
            double before = speedOf(sections.get(i - 1), scrollSpeed);
            double span = end - start;
            return before * span * (easeOut(clampedU(to, start, span)) - easeOut(clampedU(from, start, span)));
        }
        if (speed == 0) {
            return px;
        }
        if (i + 1 < sections.size() && sections.get(i + 1).timedHalt()) {
            double ramp = Math.min(sections.get(i + 1).rampSeconds(), end - start);
            // eased down over [end - ramp, end]: speed × (1 - S(u)) instead of speed
            px -= speed * ramp * (easeIn(clampedU(to, end - ramp, ramp)) - easeIn(clampedU(from, end - ramp, ramp)));
        }
        if (i > 0 && sections.get(i - 1).timedHalt()) {
            double ramp = Math.min(sections.get(i - 1).rampSeconds(), end - start);
            // eased up over [start, start + ramp]: speed × S(u) instead of speed
            px -= speed * ramp * (easeOut(clampedU(to, start, ramp)) - easeOut(clampedU(from, start, ramp)));
        } else if (i > 0 && section.easeSeconds() > 0) {
            // M7 part B: eased from the speed before over [start, start + ramp]: before + (speed - before) × S(u)
            double ramp = Math.min(section.easeSeconds(), end - start);
            double before = speedOf(sections.get(i - 1), scrollSpeed);
            px -= (speed - before) * ramp * (easeOut(clampedU(to, start, ramp)) - easeOut(clampedU(from, start, ramp)));
        }
        return px;
    }

    private static double clampedU(double t, double from, double ramp) {
        return ramp > 0 ? Math.clamp((t - from) / ramp, 0, 1) : (t >= from ? 1 : 0);
    }

    /** ∫₀ᵘ S(v) dv for the smoothstep S(v) = 3v² − 2v³: what an ease down loses against the full speed. */
    private static double easeIn(double u) {
        return u * u * u - u * u * u * u / 2;
    }

    /** ∫₀ᵘ (1 − S(v)) dv: what an ease up loses against the full speed. */
    private static double easeOut(double u) {
        return u - u * u * u + u * u * u * u / 2;
    }

    private static double speedOf(LevelData.Section section, double scrollSpeed) {
        return section.speed().isPresent() ? section.speed().get() : scrollSpeed;
    }

    /** As {@link LevelData#atmosphereStretches(List, boolean)}. */
    static List<LevelData.Stretch> atmosphereStretches(List<LevelData.Section> sections, boolean easy) {
        List<LevelData.Stretch> out = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            double start = i == 0 ? 0 : sections.get(i - 1).end();
            if (section.peak().isPresent()) {
                // M5 part G: a peak may run to its section's end (Level 13's fog from t=140, an
                // atmosphere split at a time); no empty stretch then.
                LevelData.Peak peak =
                        easy ? section.peak().get().onEasy() : section.peak().get();
                if (peak.from() > start) {
                    out.add(new LevelData.Stretch(start, peak.from(), section.atmosphere()));
                }
                out.add(new LevelData.Stretch(peak.from(), peak.to(), peak.atmosphere()));
                if (peak.to() < section.end()) {
                    out.add(new LevelData.Stretch(peak.to(), section.end(), section.atmosphere()));
                }
            } else {
                out.add(new LevelData.Stretch(start, section.end(), section.atmosphere()));
            }
        }
        return out;
    }
}
