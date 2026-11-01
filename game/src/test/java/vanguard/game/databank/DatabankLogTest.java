package vanguard.game.databank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.DatabankRecord;
import vanguard.content.campaign.DebugFit;
import vanguard.content.databank.Databank;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Step DB2 (design/ui/databank, The record in the save): a flown attempt marks the units that were on the screen as met
 * at its level, with the kills per unit; the next attempt starts empty and its kills add up.
 */
class DatabankLogTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Databank DATABANK = Databank.of(CONTENT);

    /** Flies {@code seconds} of Level {@code level} firing, logging; returns the kills per slug the events counted. */
    private static Map<String, Integer> fly(Sortie sortie, DatabankLog log, double seconds) {
        Map<String, Integer> kills = new HashMap<>();
        int fire = Command.FIRE.bit();
        for (int tick = 0; tick < SimStep.ticks(seconds); tick++) {
            sortie.step(fire);
            log.watch(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                log.react(events, i);
                if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED) {
                    String slug = sortie.enemyKinds().get(events.value(i)).slug();
                    if (DATABANK.unit(slug).isPresent()) {
                        kills.merge(slug, 1, Integer::sum);
                    }
                }
            }
        }
        return kills;
    }

    private static Sortie sortie(int level) {
        String key = CONTENT.levelKey(level).orElseThrow();
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, key, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, key, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    @Test
    void aFlownAttemptMarksTheUnitsOnTheScreenAndItsKills() {
        Sortie sortie = sortie(1);
        DatabankLog log = DatabankLog.of(sortie, DATABANK);
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 1);
        assertTrue(campaign.databank().met().isEmpty());

        Map<String, Integer> kills = fly(sortie, log, 40);
        List<String> seen = log.seen();
        assertFalse(seen.isEmpty(), "Level 01's first units come on the screen");
        assertFalse(kills.isEmpty(), "the starter gun kills a few");
        assertEquals(kills, log.kills());
        assertTrue(seen.containsAll(kills.keySet()), "a unit killed was met");
        for (String slug : seen) {
            assertTrue(DATABANK.unit(slug).isPresent(), slug + " has an entry");
        }

        log.mergeInto(campaign, 1);
        DatabankRecord record = campaign.databank();
        assertEquals(java.util.Set.copyOf(seen), record.met().keySet());
        record.met().values().forEach(met -> assertEquals(1, met.first(), "first met at Level 01"));
        kills.forEach(
                (slug, count) -> assertEquals(count, record.met().get(slug).kills(), slug));
        assertTrue(log.seen().isEmpty() && log.kills().isEmpty(), "the next attempt starts empty");

        // A second attempt (a retry): its kills add up, the first level kept.
        sortie.retry(campaign.armour());
        Map<String, Integer> again = fly(sortie, log, 40);
        log.mergeInto(campaign, 1);
        again.forEach((slug, count) -> assertEquals(
                kills.getOrDefault(slug, 0) + count,
                campaign.databank().met().get(slug).kills(),
                slug));
    }

    @Test
    void anAttemptThatMetNothingChangesNothing() {
        Sortie sortie = sortie(1);
        DatabankLog log = DatabankLog.of(sortie, DATABANK);
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 1);
        DatabankRecord before = campaign.databank();
        log.mergeInto(campaign, 1);
        assertEquals(before, campaign.databank());
    }

    @Test
    void aBossIsMetOnceItIsPresent() {
        // Level 07's Brood Carrier: a set piece whose slug has a databank entry, logged per set piece.
        Sortie sortie = sortie(7);
        boolean hasBoss = false;
        for (int p = 0; p < sortie.setPieceCount(); p++) {
            hasBoss |= DATABANK.unit(sortie.setPiece(p).slug()).isPresent();
        }
        assertTrue(hasBoss, "Level 07 has a boss with an entry");
        DatabankLog log = DatabankLog.of(sortie, DATABANK);
        fly(sortie, log, 2);
        assertFalse(log.seen().contains("brood-carrier"), "not before it arrives");
    }
}
