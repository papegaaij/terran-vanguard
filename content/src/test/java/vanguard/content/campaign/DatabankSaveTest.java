package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.databank.Databank;

/**
 * Step DB1 (design/ui/databank, The record in the save): save format version 7's {@code databank}, the version 6
 * migration (a Level 40 save), the record's merges, and the unreadable cases.
 */
class DatabankSaveTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Databank DATABANK = Databank.of(CONTENT);

    /** The v0.3 release's version 6 save, its next level moved from 36 to 40 (the Level 40 hangar). */
    private static String level40() throws Exception {
        try (var in = getResource("/saves/v0.3-medium-level-36-hangar.json")) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"version\" : 6"));
            return json.replace("\"nextLevel\" : 36", "\"nextLevel\" : 40");
        }
    }

    private static java.io.InputStream getResource(String name) {
        return DatabankSaveTest.class.getResourceAsStream(name);
    }

    @Test
    void aVersion6SaveAtTheLevel40HangarKnowsActsOneToSixThroughLevel39() throws Exception {
        SaveGame save = SaveFormat.read(level40(), DATABANK.roster());

        assertEquals(SaveFormat.VERSION, save.version());
        Map<String, DatabankRecord.Met> met = save.databank().met();
        assertEquals(new DatabankRecord.Met(1, 0), met.get("needler"));
        assertEquals(new DatabankRecord.Met(3, 0), met.get("leviathan"));
        assertEquals(new DatabankRecord.Met(35, 0), met.get("iron-sovereign"));
        assertEquals(new DatabankRecord.Met(37, 0), met.get("mirror-interceptor"));
        assertFalse(met.containsKey("honour-guard"), "Level 40 not flown yet");
        assertTrue(met.values().stream().allMatch(unit -> unit.kills() == 0));
        assertTrue(met.values().stream().allMatch(unit -> unit.first() < 40));
        // Read: every unit met, and the gear and concepts its level knows (no flood of NEW tags).
        assertTrue(save.databank().read().containsAll(met.keySet()));
        assertTrue(save.databank().read().containsAll(List.of("concept:mirrors", "ion-beam", "utility:sensor-suite")));
        assertFalse(save.databank().read().contains("generator:mk-vi"));
        // Written as version 8, it reads back unchanged.
        String json = SaveFormat.write(save);
        assertTrue(json.contains("\"version\" : 8"), json);
        assertTrue(json.contains("\"databank\""), json);
        assertEquals(save, SaveFormat.read(json));
    }

    @Test
    void theClasspathRosterIsTheDefault() throws Exception {
        assertEquals(SaveFormat.read(level40(), DATABANK.roster()), SaveFormat.read(level40()));
    }

    @Test
    void theMigrationFollowsTheSavesDifficulty() throws Exception {
        String easy = level40().replace("\"difficulty\" : \"MEDIUM\"", "\"difficulty\" : \"EASY\"");
        SaveGame save = SaveFormat.read(easy, DATABANK.roster());
        assertEquals(Difficulty.EASY, save.difficulty());
        assertEquals(
                DATABANK.roster().firstMet(40, Difficulty.EASY).keySet(),
                save.databank().met().keySet());
    }

    @Test
    void unreadableDatabanks() throws Exception {
        String json = SaveFormat.write(SaveFormat.read(level40(), DATABANK.roster()));
        ObjectNode tree = (ObjectNode) JsonMapper.builder().build().readTree(json);

        // A version 8 save without its databank.
        ObjectNode without = tree.deepCopy();
        without.remove("databank");
        assertThrows(SaveException.class, () -> SaveFormat.read(without.toString()));
        // A version 6 save that already has one.
        ObjectNode six = tree.deepCopy();
        six.put("version", 6);
        assertThrows(SaveException.class, () -> SaveFormat.read(six.toString(), DATABANK.roster()));
        // A first level out of range, negative kills, a blank read id, an unknown field, a missing one.
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"first\" : 1,", "\"first\" : 0,")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replaceFirst("\"kills\" : 0", "\"kills\" : -1")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"read\" : [ ", "\"read\" : [ \"\", ")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replaceFirst("\"kills\" : 0", "\"kils\" : 0")));
        ObjectNode noRead = tree.deepCopy();
        ((ObjectNode) noRead.get("databank")).remove("read");
        assertThrows(SaveException.class, () -> SaveFormat.read(noRead.toString()));
    }

    @Test
    void anAttemptMergesIntoTheCampaignsRecordAndIsSaved() throws Exception {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        assertEquals(DatabankRecord.EMPTY, campaign.databank());

        campaign.recordAttempt(1, List.of("needler", "skitter"), Map.of("needler", 3));
        campaign.recordAttempt(2, List.of("needler", "stinger"), Map.of("needler", 2, "stinger", 1));
        campaign.openedEntry("needler");
        campaign.openedEntry("needler");

        DatabankRecord record = campaign.databank();
        assertEquals(new DatabankRecord.Met(1, 5), record.met().get("needler"));
        assertEquals(new DatabankRecord.Met(1, 0), record.met().get("skitter"));
        assertEquals(new DatabankRecord.Met(2, 1), record.met().get("stinger"));
        assertEquals(List.of("needler"), record.read());
        SaveGame save = campaign.save(Instant.EPOCH);
        assertEquals(record, SaveFormat.read(SaveFormat.write(save)).databank());
        // A replay's record goes into a save as it is; a better grade keeps it (and the bays).
        assertEquals(
                record,
                Missions.withGrade(save, 1, "A+", List.of("A+", "A", "B")).databank());
        // A kill counted earlier than the first sighting moves the first level down.
        assertEquals(
                1,
                record.attempt(3, List.of(), Map.of("whirl-seed", 1))
                        .attempt(1, List.of("whirl-seed"), Map.of())
                        .met()
                        .get("whirl-seed")
                        .first());
        assertThrows(IllegalArgumentException.class, () -> record.attempt(1, List.of(), Map.of("needler", -1)));
    }
}
