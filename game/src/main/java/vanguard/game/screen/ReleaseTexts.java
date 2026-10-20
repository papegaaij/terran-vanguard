package vanguard.game.screen;

import java.util.Comparator;
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
 */
final class ReleaseTexts {
    /** Without a {@code next_act} in the data: the coming-soon line names no act. */
    static final String MORE = "MORE";

    private ReleaseTexts() {}

    /** The release's last act: the one that names the act after it, else the one with the highest levels. */
    static Optional<ActData> lastAct(Content content) {
        Optional<ActData> named = content.acts().values().stream()
                .filter(act -> act.nextAct().isPresent())
                .max(Comparator.comparingInt(act -> act.levels().last()));
        return named.isPresent()
                ? named
                : content.acts().values().stream()
                        .max(Comparator.comparingInt(act -> act.levels().last()));
    }

    /** The act after the release's last, as its data names it. */
    static Optional<ActData.NextAct> nextAct(Content content) {
        return lastAct(content).flatMap(ActData::nextAct);
    }

    /** The end card's heading: {@code ACTS I-III COMPLETE} ({@code ACT I COMPLETE} with one act). */
    static String title(Content content) {
        String act = lastAct(content).map(data -> data.titleCard().act()).orElse("ACT I");
        String numeral = act.startsWith("ACT ") ? act.substring("ACT ".length()) : act;
        return numeral.equals("I") ? "ACT I COMPLETE" : "ACTS I-" + numeral + " COMPLETE";
    }

    /** The end card's line under "TO BE CONTINUED": {@code ACT IV - DEEP WATER. EUROPA. COMING SOON.} */
    static String comingSoon(Content content) {
        return nextAct(content).map(ActData.NextAct::comingSoon).orElse(MORE + " COMING SOON.");
    }

    /** The end card's last line: Continue returns to the hangar at the end of the release's last act. */
    static String continueLine(Content content) {
        String act = lastAct(content).map(data -> data.titleCard().act()).orElse("THIS RELEASE");
        return "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF " + act + ".";
    }

    /** The hangar's mission line before the first level that is not built: {@code ACT IV - DEEP WATER: COMING SOON}. */
    static String hangarLine(Content content) {
        return nextAct(content).map(ActData.NextAct::hangarLine).orElse(MORE + ": COMING SOON");
    }

    /** The NEXT panel of the last built act's outro. */
    static String outroNext(Content content) {
        String next =
                nextAct(content).map(act -> act.act() + " - " + act.name()).orElse(MORE);
        return "THE END OF THIS RELEASE AND THE CREDITS. " + next + " IS COMING SOON.";
    }
}
