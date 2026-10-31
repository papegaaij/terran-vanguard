package vanguard.game.screen;

import java.util.Locale;
import vanguard.content.LevelData;
import vanguard.content.campaign.Campaign;
import vanguard.game.GameServices;
import vanguard.sim.LevelResult;
import vanguard.sim.Sortie;

/**
 * The won level's debrief (RF9e, from {@link LevelScreen}): the level banked in the campaign, and the secondary's count
 * its row shows.
 */
final class DebriefTally {
    private DebriefTally() {}

    /** Banks the won level in the campaign and shows its debrief. */
    static DebriefScreen debrief(
            GameServices services, Campaign campaign, Sortie sortie, LevelData level, String name) {
        LevelResult result = sortie.result();
        int launchBalance = campaign.credits();
        boolean newBest = campaign.complete(
                result,
                sortie.ship().defences().armour(),
                sortie.special().used(),
                sortie.special().found(),
                sortie.wingman()
                        .map(rook -> java.util.OptionalDouble.of(rook.armour()))
                        // M8 part A: the Bulwark's armour left, for its repair.
                        .or(() -> sortie.bulwark().map(bulwark -> java.util.OptionalDouble.of(bulwark.armour())))
                        .orElse(java.util.OptionalDouble.empty()));
        if (newBest) {
            // A replay's better grade goes into the save it was started from (design/ui/mission-select).
            campaign.replay()
                    .ifPresent(replay -> MissionSelectScreen.keepGrade(
                            services, replay, result.grade().letter()));
        }
        return new DebriefScreen(
                services,
                campaign,
                result,
                sortie.script().number(),
                name,
                launchBalance,
                newBest,
                !sortie.script().objectives().secondary().none(),
                count(sortie),
                level.music().debriefJingle());
    }

    /**
     * The secondary's count the debrief's row shows: a naval convoy's hulls afloat ({@code HULLS
     * AFLOAT n / 3}, M5 part E), the weather sondes collected ({@code SONDES n / 3}, M5 part F), the relay's
     * integrity ({@code RELAY n %}, M5 part G), a labelled group's units destroyed ({@code ROOTS n / 8}, M5
     * part H), the tendrils broken ({@code BARRIERS BROKEN n / 9}, M7 part C), the ground allies never hit ({@code
     * DOMES UNTOUCHED n / 5}, M7 part D) or, for a standing secondary, still standing ({@code DOMES STANDING n /
     * 5}, round 41); none else.
     */
    static DebriefScreen.Hulls count(Sortie sortie) {
        if (sortie.groundAllies()) {
            // M7 part D: the domes never hit of all (Level 17), after the tracker's label; round 41: the domes
            // still standing of all when the secondary is a standing one ("No dome lost").
            String label = sortie.script().objectives().secondary().label();
            boolean standing = sortie.script().objectives().secondary().standing();
            return new DebriefScreen.Hulls(
                    (label.isEmpty() ? "DOMES" : label.toUpperCase(Locale.ROOT)) + " "
                            + (standing ? DebriefScreen.STANDING : DebriefScreen.UNTOUCHED),
                    standing ? sortie.alliesStanding() : sortie.alliesUntouched(),
                    sortie.allyCount());
        }
        if (sortie.navalConvoy()) {
            return new DebriefScreen.Hulls(sortie.alliesAfloat(), sortie.damageableAllies());
        }
        if (sortie.sondes().count() > 0
                && sortie.script().objectives().secondary().sondes()) {
            return new DebriefScreen.Hulls(
                    DebriefScreen.SONDES,
                    sortie.sondes().collected(),
                    sortie.sondes().count());
        }
        if (sortie.script().objectives().secondary().links() && sortie.escapesTotal() > 0) {
            // M7 part C: the gates' tendrils broken of all the level's (Level 16's BARRIERS BROKEN n / 9).
            return new DebriefScreen.Hulls(
                    DebriefScreen.BARRIERS_BROKEN, sortie.escapesDestroyed(), sortie.escapesTotal());
        }
        if (sortie.defending() && sortie.script().objectives().secondary().integrity() > 0) {
            // M5 part G: the relay's integrity left (Level 13's RELAY n %).
            return DebriefScreen.Hulls.share(
                    DebriefScreen.RELAY, percent(sortie.ally(0).hpShare()));
        }
        String label = sortie.script().objectives().secondary().label();
        if (sortie.script().objectives().secondary().byGroups() && !label.isEmpty() && sortie.groupUnits(0) > 0) {
            // M5 part H: a labelled group of units destroyed of all (Level 14's ROOTS n / 8).
            return new DebriefScreen.Hulls(
                    label.toUpperCase(Locale.ROOT), sortie.groupUnitsDestroyed(0), sortie.groupUnits(0));
        }
        return DebriefScreen.Hulls.NONE;
    }

    /**
     * M5 part G: a share as the debrief's whole percent, rounded down so a relay under half never reads
     * 50 % (the secondary's threshold).
     */
    static int percent(double share) {
        return (int) Math.floor(share * 100 + 1e-9);
    }
}
