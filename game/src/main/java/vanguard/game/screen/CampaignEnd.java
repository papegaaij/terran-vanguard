package vanguard.game.screen;

import java.util.Locale;
import java.util.Map;
import vanguard.content.ActData;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.campaign.SaveGame;

/**
 * M11 part A, step A3b (design/ui/debrief, The campaign end; design/systems/saves, The completed campaign;
 * design/ui/mission-select and design/ui/main-menu, After the campaign): the campaign end's texts and the one rule the
 * menus share for a <b>completed campaign</b>: a save whose next level is one past the content's last act's last level,
 * that act naming no next act ({@code nextLevel} 51 at v1.0, {@link SaveGame#COMPLETED}; a fixture act set's end
 * likewise). A build short of the campaign's last level never writes one (its last won level ends the release).
 */
final class CampaignEnd {
    static final String TITLE = "CAMPAIGN COMPLETE";
    static final String DARK = "THE GATE IS DARK.";
    static final String REPLAY = "EVERY MISSION IS OPEN FOR REPLAY IN MISSIONS.";
    /** The slot's and the mission select header's word for a completed campaign. */
    static final String COMPLETE = "CAMPAIGN COMPLETE";

    private CampaignEnd() {}

    /** The card's line under its heading: {@code MEDIUM - ALL 50 MISSIONS FLOWN}. */
    static String flown(Difficulty difficulty, int missions) {
        return difficulty.name() + " - ALL " + missions + " MISSIONS FLOWN";
    }

    /** {@code DATA CORES FOUND}'s value: {@code 38 OF 47}. */
    static String cores(int found, int total) {
        return String.format(Locale.ROOT, "%d OF %d", found, total);
    }

    /** The data cores the game holds: the secrets of the built levels (50 at v1.0). */
    static int secrets(Content content) {
        return content.levels().values().stream()
                .mapToInt(level -> level.secrets().size())
                .sum();
    }

    /** The content's last level: its last act's last (the act with the highest levels). */
    static int lastLevel(Content content) {
        return content.acts().values().stream()
                .mapToInt(act -> act.levels().last())
                .max()
                .orElse(0);
    }

    /** Whether a campaign going on with {@code nextLevel} is complete: every level of the content's last act won. */
    static boolean completed(Content content, int nextLevel) {
        if (nextLevel >= SaveGame.COMPLETED) {
            return true;
        }
        int won = nextLevel - 1;
        return won >= 1
                && content.levelKey(nextLevel).isEmpty()
                && content.levelKey(won).isPresent()
                && won == lastLevel(content)
                && content.actOf(won)
                        .map(Map.Entry::getValue)
                        .map(ActData::nextAct)
                        .filter(java.util.Optional::isEmpty)
                        .isPresent();
    }

    /** Whether {@code save} holds a completed campaign. */
    static boolean completed(Content content, SaveGame save) {
        return completed(content, save.nextLevel());
    }

    /** The mission select's header for a save: {@code REPLAY - MEDIUM}, {@code - CAMPAIGN COMPLETE} once it is. */
    static String missionsHeader(Content content, SaveGame save) {
        return "REPLAY - " + save.difficulty().name() + (completed(content, save) ? " - " + COMPLETE : "");
    }

    /** The ending's last page's image as a path in the assets (the dark gate), if it has one. */
    static java.util.Optional<String> lastImage(java.util.List<vanguard.content.BriefingPage> pages) {
        return pages.isEmpty()
                ? java.util.Optional.empty()
                : pages.getLast().image().map(name -> "ui/briefing/" + name + ".png");
    }
}
