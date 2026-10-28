package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * M9 part B (Level 29, round 62; the Rook-barks bug of step B5): the rescued Rook barks only once he is in his slot. The
 * screen's gate ({@link LevelScreen#rookInSlot}) is shut while he waits in the prison tender's hold and open for Rook
 * from the start; his "Miss me?" as he joins counts as a scripted Rook line for the barks' spacing. That the gate opens
 * exactly at his join on a flown level is {@code Level29Test#theAutopilotFliesTheLevel}'s; the barks' side of it is
 * {@code BarksTest#outOfHisSlotHeBarksNothingAndHisKillsCountForNoStreak}.
 */
class LevelScreenRescueTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(29).orElseThrow();

    private static Sortie sortie(Loadout loadout) {
        return new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                loadout.plating().maxArmour());
    }

    private static WingmanSpec rook() {
        return SimSpecs.wingman(
                CONTENT,
                "autocannon",
                1,
                WingmanSpec.Side.LEFT,
                CONTENT.wingmen().rook().armour());
    }

    @Test
    void theRescuedRookIsOutOfHisSlotUntilHeJoinsIt() {
        Loadout starter = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM);
        Sortie rescue = sortie(starter.withRescue(rook()));
        assertTrue(rescue.wingman().orElseThrow().waiting(), "Rook waits in the hold");
        assertFalse(LevelScreen.rookInSlot(rescue), "no barks from the hold");

        assertTrue(
                LevelScreen.rookInSlot(sortie(starter.withWingman(rook()))), "--escort rook: he barks from the start");
        assertTrue(LevelScreen.rookInSlot(sortie(starter)), "no Rook: the gate is open (no barks are built)");
    }

    @Test
    void hisJoiningLineCountsForHisBarksSpacing() {
        assertTrue(LevelScreen.scriptedForBarks(CueTrigger.ESCORT_JOINED));
    }
}
