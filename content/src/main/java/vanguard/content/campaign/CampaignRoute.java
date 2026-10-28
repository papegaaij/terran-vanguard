package vanguard.content.campaign;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vanguard.content.ActData;
import vanguard.content.Content;

/**
 * Where the campaign goes between levels (design/ui, Screen flow): a new game opens with the
 * intro briefing, a won level, a Continue and a mission select's replay lead to the level's briefing,
 * and every briefing leads to the hangar, from which the level launches. A won act's last level adds
 * the act summary to its debrief and then plays the act's outro before that (design/campaign, Act
 * intro and outro; user decision D2 of M4 part G); a replay never does. While the next level is not built yet, a won
 * level goes straight back to the hangar and the hangar cannot launch, except after an act's outro:
 * then the release has ended (M6 part B, Q1 = a: the end card, the name entry and the credits come
 * first, and Continue reaches that hangar).
 */
public final class CampaignRoute {
    /** A step between levels. */
    public sealed interface Step {
        /** The briefing, then the hangar. */
        record Briefing(BriefingScript script) implements Step {}

        /** The act outro, then the step {@link #beforeNextLevel} gives. */
        record Outro(OutroScript script) implements Step {}

        record Hangar() implements Step {}
    }

    /**
     * The act whose last level the campaign has just won.
     *
     * @param actDirectory the act's directory under design/campaign
     * @param level the level that ended it: the act's last, or an earlier one with {@code --act-end}
     */
    public record ActEnd(String actDirectory, ActData act, int level) {}

    private CampaignRoute() {}

    /**
     * The step before the campaign's next level: after the difficulty select of a new game (the
     * intro briefing: the act intro and the Level 01 briefing) and after a won level's debrief. M7 part B
     * (user decision L.6 of round 36): a briefing that opens an act (its title card and intro pages) records
     * the act's intro as shown ({@link Campaign#markActIntroShown}), saved with the hangar's autosave.
     */
    public static Step beforeNextLevel(Content content, Campaign campaign) {
        Optional<BriefingScript> briefing = Briefings.before(content, campaign.nextLevel());
        briefing.filter(script -> script.titleCard().isPresent())
                .flatMap(script -> actNumber(script.actDirectory()))
                .ifPresent(campaign::markActIntroShown);
        return briefing.<Step>map(Step.Briefing::new).orElseGet(Step.Hangar::new);
    }

    /**
     * The step a Continue (or a slot's load) opens: the next level's mission briefing, then the hangar (the
     * user's decision of 2026-10-18: every mission starts with its briefing). M7 part B (user decision L.6 of
     * round 36): a save whose next level opens an act whose intro it has not shown yet (a save at the Level 15
     * hangar from the M6 release, written before Act 3 existed) plays that act's intro first (the title card
     * and the intro pages before the level's briefing, {@link #beforeNextLevel}, which records it); every other
     * save gets the level's own briefing without its act's intro ({@link #missionBriefing}). The hangar when
     * the next level has no briefing yet (it is not built). A briefing with a title card is the act intro.
     */
    public static Step onContinue(Content content, Campaign campaign) {
        int next = campaign.nextLevel();
        boolean unseen = campaign.replay().isEmpty()
                && content.levelKey(next).isPresent()
                && content.actOf(next)
                        .filter(act -> act.getValue().levels().first() == next)
                        .flatMap(act -> actNumber(act.getKey()))
                        .filter(act -> !campaign.actIntroShown(act))
                        .isPresent();
        if (unseen) {
            return beforeNextLevel(content, campaign);
        }
        return missionBriefing(content, next).<Step>map(Step.Briefing::new).orElseGet(Step.Hangar::new);
    }

    /**
     * The step a mission select's replay opens (the user's decision of 2026-10-18): that level's mission
     * briefing without its act's intro, then the replay hangar; the hangar when the level has no briefing.
     */
    public static Step beforeReplay(Content content, int level) {
        return missionBriefing(content, level).<Step>map(Step.Briefing::new).orElseGet(Step.Hangar::new);
    }

    /**
     * The level's own briefing ({@link Briefings#before}) without its act's title card and intro pages, for a
     * level whose act intro has been shown: a Continue's and a replay's.
     */
    public static Optional<BriefingScript> missionBriefing(Content content, int number) {
        return Briefings.before(content, number).map(script -> {
            if (script.titleCard().isEmpty()) {
                return script;
            }
            int intro =
                    content.actOf(number).orElseThrow().getValue().briefing().size();
            return new BriefingScript(
                    Optional.empty(),
                    script.actDirectory(),
                    script.act(),
                    script.mission(),
                    script.missionName(),
                    script.pages().subList(intro, script.pages().size()),
                    script.objectives(),
                    script.teaser());
        });
    }

    private static final Pattern ACT_DIRECTORY = Pattern.compile("act-(\\d+)-[a-z0-9-]+");

    /** M7 part B: an act's number from its directory ({@code act-3-red-dust} is 3). */
    static Optional<Integer> actNumber(String actDirectory) {
        Matcher matcher = ACT_DIRECTORY.matcher(actDirectory);
        return matcher.matches() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();
    }

    /**
     * The step after a won level's debrief: the act outro when the level ended its act and the act
     * has one, otherwise {@link #beforeNextLevel}.
     */
    public static Step afterLevel(Content content, Campaign campaign) {
        return actEnd(content, campaign)
                .flatMap(end -> Briefings.outro(end, campaign.nextLevel()))
                .<Step>map(Step.Outro::new)
                .orElseGet(() -> beforeNextLevel(content, campaign));
    }

    /**
     * The act the level just won ended (called after {@link Campaign#complete}): the act's last
     * level, or the level of the {@code --act-end} debug option. Never in a replay.
     */
    public static Optional<ActEnd> actEnd(Content content, Campaign campaign) {
        int won = campaign.nextLevel() - 1;
        if (campaign.replay().isPresent() || won < 1) {
            return Optional.empty();
        }
        Optional<Map.Entry<String, ActData>> act = content.actOf(won);
        boolean last = act.map(entry -> entry.getValue().levels().last() == won).orElse(false)
                || campaign.debugActEndLevel().equals(Optional.of(won));
        return act.filter(entry -> last).map(entry -> new ActEnd(entry.getKey(), entry.getValue(), won));
    }

    /**
     * Whether an act outro that has just finished ends the release (M6 part B): the content has no
     * next level, so the end card, the name entry and the credits follow instead of the hangar. A
     * generic rule: {@code --act-end} on an earlier level is followed by its next level as before,
     * and a replay never shows an outro. M9 part B (step B3): the same after a won level that ends no act
     * ({@link #afterLevel} gives the hangar) while the act's next level is not built: Level 29's win ends the
     * release until Level 30 is built (the game's end card after its debrief is step B4's).
     */
    public static boolean releaseEnd(Content content, Campaign campaign) {
        return campaign.replay().isEmpty()
                && content.levelKey(campaign.nextLevel()).isEmpty();
    }

    /** The level the hangar launches, if it is built yet. */
    public static Optional<String> launch(Content content, Campaign campaign) {
        return content.levelKey(campaign.nextLevel());
    }
}
