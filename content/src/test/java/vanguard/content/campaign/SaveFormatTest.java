package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vanguard.content.Difficulty;

class SaveFormatTest {
    static SaveGame save() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.HARD);
        campaign.markActIntroShown(1); // M7 part B: as the new game's intro briefing does
        campaign.complete(CampaignTest.won("A", 80, 12_000), 41.5);
        campaign.play(300);
        return campaign.save(Instant.parse("2026-10-02T10:00:00Z"));
    }

    @Test
    void aSaveSurvivesTheRoundTrip() throws SaveException {
        SaveGame save = save();

        String json = SaveFormat.write(save);

        assertTrue(json.contains("\"version\" : 5"), json);
        assertTrue(json.contains("\"bays\" : 0"), json);
        assertEquals(save, SaveFormat.read(json));
        assertEquals(
                Map.of(1, new SaveGame.LevelStats(480, 80)),
                SaveFormat.read(json).stats().levels());
    }

    /** Format version 1 (before M4 part G) had no level stats: such a save loads with none recorded. */
    @Test
    void aVersion1SaveLoadsWithoutLevelStats() throws SaveException {
        SaveGame save = save();
        String json = SaveFormat.write(save);
        ObjectNode tree = (ObjectNode) JsonMapper.builder().build().readTree(json);
        tree.put("version", 1);
        ((ObjectNode) tree.get("stats")).remove("levels");
        tree.remove("escort");
        tree.remove("bays");
        String old = tree.toString();
        assertTrue(!old.contains("\"levels\""), old);

        SaveGame loaded = SaveFormat.read(old);

        assertEquals(SaveFormat.VERSION, loaded.version());
        assertEquals(Map.of(), loaded.stats().levels());
        assertEquals(save.stats().kills(), loaded.stats().kills());
        assertEquals(save.credits(), loaded.credits());
        Campaign campaign = Campaign.load(CampaignTest.RULES, loaded);
        assertEquals(Optional.empty(), campaign.levelStats(1));
    }

    @Test
    void aVersion1SaveWithLevelStatsIsRejected() {
        ObjectNode tree = (ObjectNode) JsonMapper.builder().build().readTree(SaveFormat.write(save()));
        tree.put("version", 1);
        tree.remove("escort");
        tree.remove("bays");
        // Its stats.levels (version 2) is what makes it invalid.

        SaveException e = assertThrows(SaveException.class, () -> SaveFormat.read(tree.toString()));
        assertTrue(e.getMessage().contains("stats.levels"), e.getMessage());
    }

    /** M7 part A (Q1 = a): a version 3 save (M5 to M6) has no bays bought; it loads with none. */
    @Test
    void aVersion3SaveLoadsWithNoBayBought() throws SaveException {
        SaveGame save = save();
        ObjectNode tree = (ObjectNode) JsonMapper.builder().build().readTree(SaveFormat.write(save));
        tree.put("version", 3);
        tree.remove("bays");

        SaveGame loaded = SaveFormat.read(tree.toString());

        assertEquals(SaveFormat.VERSION, loaded.version());
        assertEquals(0, loaded.bays());
        assertEquals(save, loaded);
        assertEquals(0, Campaign.load(CampaignTest.RULES, loaded).gear().bays());
    }

    /**
     * M7 part B (L.6 of round 36): a version 3 save (the M6 release, Acts 1–2) has seen the intros of the acts it
     * reached: the migration marks {@code act-intro:1}, and {@code act-intro:2} from Level 08 on, never Act 3's.
     */
    @Test
    void aVersion3SaveGetsTheReleasedActsIntrosAsShown() throws SaveException {
        assertEquals(
                List.of("act-intro:1"), SaveFormat.read(version3(3, List.of())).storyFlags());
        assertEquals(
                List.of("act-intro:1", "act-intro:2"),
                SaveFormat.read(version3(8, List.of())).storyFlags());
        assertEquals(
                List.of("free-charges:Airstrike", "act-intro:1", "act-intro:2"),
                SaveFormat.read(version3(15, List.of("free-charges:Airstrike"))).storyFlags());
        assertEquals(
                List.of("act-intro:1", "act-intro:2"),
                SaveFormat.read(version3(15, List.of("act-intro:1"))).storyFlags(),
                "a flag already there is not added twice");
    }

    private static String version3(int nextLevel, List<String> flags) {
        ObjectNode tree = (ObjectNode) JsonMapper.builder().build().readTree(SaveFormat.write(save()));
        tree.put("version", 3);
        tree.remove("bays");
        tree.put("nextLevel", nextLevel);
        flags.forEach(tree.putArray("storyFlags")::add);
        if (flags.isEmpty()) {
            tree.putArray("storyFlags");
        }
        return tree.toString();
    }

    @Test
    void aVersion3SaveWithBaysOrAVersion4SaveWithoutThemIsRejected() {
        String json = SaveFormat.write(save());

        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"version\" : 5", "\"version\" : 3")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"bays\" : 0,", "")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"bays\" : 0", "\"bays\" : -1")));
    }

    /** M7 part A: the third bay's slot holds a module only once the bay is bought; the bought bay survives. */
    @Test
    void aModuleInTheThirdBayNeedsItBought() throws SaveException {
        Campaign campaign = Campaign.load(CampaignTest.RULES, save());
        Gear gear = campaign.gear();
        java.util.Map<LoadoutSlot, Fitted> loadout = new java.util.EnumMap<>(gear.loadout());
        loadout.put(LoadoutSlot.UTILITY_3, new Fitted("Pickup magnet", 1));
        campaign.gear(
                new Gear(gear.credits(), loadout, gear.inventory(), gear.specials(), gear.armour(), gear.escort(), 1));
        String json = SaveFormat.write(campaign.save(Instant.EPOCH));

        SaveGame loaded = SaveFormat.read(json);
        assertEquals(1, loaded.bays());
        assertEquals(new Fitted("Pickup magnet", 1), loaded.loadout().get(LoadoutSlot.UTILITY_3));
        assertEquals(1, Campaign.load(CampaignTest.RULES, loaded).gear().bays());
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"bays\" : 1", "\"bays\" : 0")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Gear(
                        gear.credits(), loadout, gear.inventory(), gear.specials(), gear.armour(), gear.escort()));
    }

    /**
     * The power dial (the user, 2026-10-21): format version 5 writes a weapon's power setting only below full power, and
     * reads it back; a setting outside 1 up to the level is rejected.
     */
    @Test
    void aPowerSettingSurvivesTheRoundTrip() throws SaveException {
        Campaign campaign = Campaign.load(CampaignTest.RULES, save());
        Gear gear = campaign.gear();
        java.util.Map<LoadoutSlot, Fitted> loadout = new java.util.EnumMap<>(gear.loadout());
        loadout.put(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 4, 2));
        loadout.put(LoadoutSlot.REAR, new Fitted("tail-gun", 3));
        campaign.gear(new Gear(
                gear.credits(),
                loadout,
                Map.of(ItemKind.WING, List.of(new Fitted("autocannon-pod", 2, 1))),
                gear.specials(),
                gear.armour(),
                gear.escort()));
        String json = SaveFormat.write(campaign.save(Instant.EPOCH));
        assertEquals(2, json.split("\"power\"", -1).length - 1, "only the turned-down items carry a setting: " + json);

        SaveGame loaded = SaveFormat.read(json);

        assertEquals(new Fitted("pulse-cannon", 4, 2), loaded.loadout().get(LoadoutSlot.FRONT));
        assertEquals(new Fitted("tail-gun", 3, 3), loaded.loadout().get(LoadoutSlot.REAR));
        assertEquals(
                List.of(new Fitted("autocannon-pod", 2, 1)), loaded.inventory().get(ItemKind.WING));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"power\" : 2", "\"power\" : 5")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"power\" : 2", "\"power\" : 0")));
    }

    /** The power dial: a version 4 save (no power settings) loads with every item at full power. */
    @Test
    void aVersion4SaveLoadsAtFullPower() throws SaveException {
        Campaign campaign = Campaign.load(CampaignTest.RULES, save());
        Gear gear = campaign.gear();
        java.util.Map<LoadoutSlot, Fitted> loadout = new java.util.EnumMap<>(gear.loadout());
        loadout.put(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 3));
        campaign.gear(
                new Gear(gear.credits(), loadout, gear.inventory(), gear.specials(), gear.armour(), gear.escort()));
        SaveGame save = campaign.save(Instant.EPOCH);
        String old = SaveFormat.write(save).replace("\"version\" : 5", "\"version\" : 4");
        assertTrue(!old.contains("\"power\""), old);

        SaveGame loaded = SaveFormat.read(old);

        assertEquals(SaveFormat.VERSION, loaded.version());
        assertEquals(save, loaded);
        assertTrue(loaded.loadout().values().stream().allMatch(Fitted::fullPower));
        assertEquals(3, loaded.loadout().get(LoadoutSlot.FRONT).power());
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, Campaign.load(CampaignTest.RULES, loaded));
        assertEquals(
                3,
                HangarTest.row(hangar, LoadoutSlot.FRONT, "Pulse Cannon", Hangar.State.FITTED)
                        .power());
    }

    @Test
    void anOldTopGradeSLoadsAsAPlus() throws SaveException {
        String json = SaveFormat.write(save());
        String old = json.replaceFirst("(\"grades\" : \\{\\s*\"1\" : )\"A\"", "$1\"S\"");
        assertTrue(old.contains("\"S\""), old);

        assertEquals("A+", SaveFormat.read(old).grades().get(1));
    }

    @Test
    void aNewerFormatVersionIsRejected() {
        String json = SaveFormat.write(save()).replace("\"version\" : 5", "\"version\" : 6");

        SaveException e = assertThrows(SaveException.class, () -> SaveFormat.read(json));
        assertTrue(e.getMessage().contains("newer version"), e.getMessage());
    }

    @Test
    void anUnknownOrMissingVersionIsRejected() {
        String json = SaveFormat.write(save());

        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"version\" : 5", "\"version\" : 0")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"version\" : 5,", "")));
        assertThrows(
                SaveException.class, () -> SaveFormat.read(json.replace("\"version\" : 5", "\"version\" : \"5\"")));
    }

    @Test
    void aMalformedOrIncompleteSaveIsRejected() {
        String json = SaveFormat.write(save());

        assertThrows(SaveException.class, () -> SaveFormat.read("{not json"));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"credits\"", "\"cash\"")));
        assertThrows(SaveException.class, () -> SaveFormat.read(json.replace("\"difficulty\" : \"HARD\",", "")));
        assertThrows(
                SaveException.class, () -> SaveFormat.read(json.replace("\"nextLevel\" : 2", "\"nextLevel\" : 0")));
    }
}
