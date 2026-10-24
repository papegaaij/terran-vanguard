package vanguard.game.level;

import java.util.ArrayList;
import java.util.List;
import vanguard.content.RadioBlock.RadioCue;

/**
 * M8 part F (design/campaign Level 26, The Choir; m8f-gaps.md §1.5): the radio rows' {@code static_lead}: the HUD
 * portrait frame's interference ({@code TransmissionStatic}) shows that many seconds before a timed line plays,
 * swelling from nothing to full by the line's time, where the transmission's own opening burst takes over; its sound
 * (the Choir's static) starts with it. Only the rows that set it have one (Level 14's Choir none). On the level clock,
 * so a retry replays it; no state.
 */
public final class StaticLeads {
    /** Per lead its start and its line's time, level seconds. */
    private final double[] starts;

    private final double[] lines;

    /** The leads of a level's radio rows, in row order. */
    public StaticLeads(List<RadioCue> radio) {
        List<double[]> leads = new ArrayList<>();
        for (RadioCue cue : radio) {
            if (cue.staticLead().isPresent() && cue.t().isPresent()) {
                double t = cue.t().get();
                leads.add(new double[] {t - cue.staticLeadSeconds(), t});
            }
        }
        starts = new double[leads.size()];
        lines = new double[leads.size()];
        for (int i = 0; i < leads.size(); i++) {
            starts[i] = leads.get(i)[0];
            lines[i] = leads.get(i)[1];
        }
    }

    /** Whether the level has any lead. */
    public boolean any() {
        return starts.length > 0;
    }

    /** The interference's strength at level time {@code seconds}, 0 to 1: rising smoothly over a lead, 0 outside. */
    public float strength(double seconds) {
        float best = 0;
        for (int i = 0; i < starts.length; i++) {
            if (seconds >= starts[i] && seconds < lines[i]) {
                double u = (seconds - starts[i]) / (lines[i] - starts[i]);
                best = Math.max(best, (float) (u * u * (3 - 2 * u)));
            }
        }
        return best;
    }

    /** Whether a lead starts after level time {@code before} and at or before {@code now} (its sound plays then). */
    public boolean started(double before, double now) {
        for (double start : starts) {
            if (before < start && start <= now) {
                return true;
            }
        }
        return false;
    }
}
