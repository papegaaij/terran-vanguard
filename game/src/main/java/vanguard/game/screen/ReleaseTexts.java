package vanguard.game.screen;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import vanguard.content.ActData;
import vanguard.content.Content;

/**
 * M7 part H (the user's round 43 answer H.7d): the release end's texts from the acts' data, so the end moves with the
 * last built act without a hard-coded act number: the release's last act is the one whose data names the act after it
 * ({@code next_act}; else the act with the highest levels), its title card's number heads the end card ("ACTS I-III
 * COMPLETE") and Continue's line ("... AT THE END OF ACT III"), and the next act's own {@link ActData.NextAct} gives the
 * coming-soon lines of the end card ("ACT IV - DEEP WATER. EUROPA. COMING SOON."), the outro's NEXT panel and the
 * hangar before its first level ("ACT IV - DEEP WATER: COMING SOON").
 *
 * <p>M9 part B (step B4): a release may end inside an act (the branch's Act 5 ended after Level 29, its first level, until
 * Level 30 was built; M9 part C: after Level 30 until Level 31 was; M9 part D: after Level 31 until Level 32 is). Then the open act is the one holding the last built level but not ending with it: the end card
 * heads the acts completed before it ("ACTS I-IV COMPLETE") and the coming-soon lines name the open act's next level
 * ("ACT V - THE BELT. LEVEL 31 COMING SOON."), Continue's line the hangar before it (pending round 62).
 */
final class ReleaseTexts {
    /** Without a {@code next_act} in the data: the coming-soon line names no act. */
    static final String MORE = "MORE";

    private ReleaseTexts() {}

    /** The highest level number the content builds. */
    static int lastLevel(Content content) {
        return content.levels().keySet().stream()
                .mapToInt(Content::levelNumber)
                .max()
                .orElse(0);
    }

    /** M9 part B: the act the release ends inside: it holds the last built level, but its own last level is not built. */
    static Optional<ActData> openAct(Content content) {
        int last = lastLevel(content);
        return content.actOf(last)
                .map(Map.Entry::getValue)
                .filter(act -> act.levels().last() > last);
    }

    /**
     * The release's last act: the one that names the act after it, else the last act whose levels are all built (the
     * act before an {@link #openAct}), else the one with the highest levels.
     */
    static Optional<ActData> lastAct(Content content) {
        Optional<ActData> named = content.acts().values().stream()
                .filter(act -> act.nextAct().isPresent())
                .max(Comparator.comparingInt(act -> act.levels().last()));
        if (named.isPresent()) {
            return named;
        }
        int last = lastLevel(content);
        Optional<ActData> complete = content.acts().values().stream()
                .filter(act -> act.levels().last() <= last)
                .max(Comparator.comparingInt(act -> act.levels().last()));
        return complete.isPresent()
                ? complete
                : content.acts().values().stream()
                        .max(Comparator.comparingInt(act -> act.levels().last()));
    }

    /** The act after the release's last, as its data names it. */
    static Optional<ActData.NextAct> nextAct(Content content) {
        return lastAct(content).flatMap(ActData::nextAct);
    }

    /** M9 part B: the open act and its next level, {@code ACT V - THE BELT. LEVEL 31}; empty without an open act. */
    private static Optional<String> openNext(Content content, String separator) {
        return openAct(content)
                .map(act -> act.titleCard().act() + " - " + act.titleCard().name() + separator
                        + String.format(Locale.ROOT, "LEVEL %02d", lastLevel(content) + 1));
    }

    /** The end card's heading: {@code ACTS I-III COMPLETE} ({@code ACT I COMPLETE} with one act). */
    static String title(Content content) {
        String act = lastAct(content).map(data -> data.titleCard().act()).orElse("ACT I");
        String numeral = act.startsWith("ACT ") ? act.substring("ACT ".length()) : act;
        return numeral.equals("I") ? "ACT I COMPLETE" : "ACTS I-" + numeral + " COMPLETE";
    }

    /** The end card's line under "TO BE CONTINUED": {@code ACT IV - DEEP WATER. EUROPA. COMING SOON.} */
    static String comingSoon(Content content) {
        return openNext(content, ". ").map(next -> next + " COMING SOON.").orElseGet(() -> nextAct(content)
                .map(ActData.NextAct::comingSoon)
                .orElse(MORE + " COMING SOON."));
    }

    /** The end card's last line: Continue returns to the hangar at the end of the release's last act. */
    static String continueLine(Content content) {
        if (openAct(content).isPresent()) {
            return String.format(
                    Locale.ROOT,
                    "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR BEFORE LEVEL %02d.",
                    lastLevel(content) + 1);
        }
        String act = lastAct(content).map(data -> data.titleCard().act()).orElse("THIS RELEASE");
        return "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF " + act + ".";
    }

    /** The hangar's mission line before the first level that is not built: {@code ACT IV - DEEP WATER: COMING SOON}. */
    static String hangarLine(Content content) {
        return openNext(content, ": ").map(next -> next + " COMING SOON").orElseGet(() -> nextAct(content)
                .map(ActData.NextAct::hangarLine)
                .orElse(MORE + ": COMING SOON"));
    }

    /** The NEXT panel of the last built act's outro. */
    static String outroNext(Content content) {
        String next = openNext(content, ": ").orElseGet(() -> nextAct(content)
                .map(act -> act.act() + " - " + act.name())
                .orElse(MORE));
        return "THE END OF THIS RELEASE AND THE CREDITS. " + next + " IS COMING SOON.";
    }
}
