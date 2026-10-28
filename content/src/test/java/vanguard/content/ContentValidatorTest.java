package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Characterisation of {@link ContentValidator}'s checks that no other test reaches with broken data (the split per
 * concern, refactor step RF4): the full problem list of a real data file with one key broken, strings and order, and one
 * list across every concern to pin the order the checks run in.
 */
class ContentValidatorTest {
    private static final String BASIS = "enemies/data.yaml";
    private static final String WINGMEN = "player/wingmen/data.yaml";
    private static final String SHIELDS = "player/shields/data.yaml";
    private static final String COILWYRM = "enemies/air/coilwyrm/data.yaml";
    private static final String FRIGATE = "enemies/bosses/gorgon-frigate/data.yaml";
    private static final String ACT_1 = "campaign/act-1-first-contact/";
    private static final String LEVEL_01 = ACT_1 + "level-01-break-at-dawn/data.yaml";
    private static final String LEVEL_03 = ACT_1 + "level-03-spore-drift/data.yaml";
    private static final String LEVEL_07 = ACT_1 + "level-07-brood-carrier/data.yaml";
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";

    /** One change to a data file: its first {@code what} becomes {@code with}. */
    private record Edit(String path, String what, String with) {}

    private static List<String> problems(Edit... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (Edit edit : edits) {
                if (edit.path().equals(file.path())) {
                    int at = text.indexOf(edit.what());
                    assertTrue(at >= 0, file.path() + " has " + edit.what());
                    text = text.substring(0, at)
                            + edit.with()
                            + text.substring(at + edit.what().length());
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return assertThrows(ContentException.class, () -> ContentLoader.load(files))
                .problems();
    }

    private static String design(String path) {
        return "design/" + path + ": ";
    }

    @Test
    void aSmallActHpUnitIsSmall() {
        assertEquals(
                List.of(design(BASIS) + "small_act_hp.units[1]: leviathan is not a small unit"),
                problems(new Edit(BASIS, "units: [needler, stinger]", "units: [needler, leviathan]")));
    }

    @Test
    void theBeamEraLeavesTheHugeTierOut() {
        assertEquals(
                List.of(design(BASIS) + "beam_era_hp.tiers: one or more tiers below huge"),
                problems(new Edit(BASIS, "tiers: [small]", "tiers: [small, huge]")));
    }

    @Test
    void theStarterIsFreeAndAvailableFromTheStart() {
        assertEquals(
                List.of(design(SHIELDS) + "models[0]: the first entry is the starter: price 0, available start"),
                problems(new Edit(SHIELDS, "price: 0, available: start", "price: 10, available: start")));
    }

    @Test
    void anAvailabilityIsKnown() {
        assertEquals(
                List.of(design(WINGMEN)
                        + "bulwark.available: unknown availability 'someday' (known: Lnn or act 1, act 2,"
                        + " act 3, act 4, act 5, act 6, act 7, start)"),
                problems(new Edit(WINGMEN, "available: L22", "available: someday")));
    }

    @Test
    void rooksBarksHaveAVoice() {
        assertEquals(
                List.of(design(WINGMEN) + "barks.speaker: no voice speaker 'Rookie'"),
                problems(new Edit(WINGMEN, "  speaker: Rook", "  speaker: Rookie")));
    }

    @Test
    void anAttacksBulletIsKnown() {
        assertEquals(
                List.of(design(COILWYRM) + "attacks[0].bullet: unknown bullet class 'smal'"),
                problems(new Edit(COILWYRM, "bullet: small", "bullet: smal")));
    }

    @Test
    void thePartsBountiesAddUpToTheUnits() {
        assertEquals(
                List.of(design(FRIGATE) + "bounty: is 201, the parts' bounties add up to 200"),
                problems(new Edit(FRIGATE, "bounty: 200 ", "bounty: 201 ")));
    }

    @Test
    void aSetPiecesPassLiesInASection() {
        assertEquals(
                List.of(design(LEVEL_03) + "set_pieces[0].passes[0].section: no section 9"),
                problems(new Edit(
                        LEVEL_03, "section: 3\n        layer: high-air", "section: 9\n        layer: high-air")));
    }

    @Test
    void aDebrisChunkIsKnown() {
        assertEquals(
                List.of(design(LEVEL_03)
                        + "debris.placed[1].chunk: no chunk 'small-z' in debris.chunks [large-a, large-b, large-c,"
                        + " small-a, small-b]"),
                problems(new Edit(LEVEL_03, "{t: 33, x: 340, chunk: small-a", "{t: 33, x: 340, chunk: small-z")));
    }

    @Test
    void aPartsObjectiveNamesTheBossesPartsAndPhase() {
        assertEquals(
                List.of(
                        design(LEVEL_07) + "objectives.secondary.parts: 'brood-carrier' has no part 'bay 9 left'",
                        design(LEVEL_07) + "objectives.secondary.before: no boss phase 'Broadsides'"),
                problems(
                        new Edit(LEVEL_07, "secondary: {parts: [bay 1 left,", "secondary: {parts: [bay 9 left,"),
                        new Edit(LEVEL_07, "before: Broadside,", "before: Broadsides,")));
    }

    @Test
    void aPickupsWaveExists() {
        assertEquals(
                List.of(design(LEVEL_01) + "pickups[0].dropped_by.wave: no wave at t=121.0"),
                problems(new Edit(LEVEL_01, "dropped_by: {wave: 122,", "dropped_by: {wave: 121,")));
    }

    @Test
    void aLevelsActHasItsDataFile() {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .filter(file -> !file.path().equals(ACT_1 + "data.yaml"))
                .toList();

        // The levels come in the content's map order: compared sorted.
        assertEquals(
                List.of(
                                "level-01-break-at-dawn",
                                "level-02-shipyard-burning",
                                "level-03-spore-drift",
                                "level-04-tranquility-run",
                                "level-05-crater-nest",
                                "level-06-farside",
                                "level-07-brood-carrier")
                        .stream()
                        .map(level -> design(ACT_1 + level + "/data.yaml")
                                + "level: its act act-1-first-contact has no data file")
                        .toList(),
                assertThrows(ContentException.class, () -> ContentLoader.load(files)).problems().stream()
                        .sorted()
                        .toList());
    }

    /** Step B3b: a held arena's holder asleep until the arena's end would never wake: the clock stands a step before. */
    @Test
    void aHeldArenasHolderWakesByItsHalt() {
        assertEquals(
                List.of(design(LEVEL_29)
                        + "ground_targets: a held arena's holder wakes by its halt (the clock stands a step before"
                        + " 120.0): dormant_until 120.0"),
                problems(new Edit(LEVEL_29, "dormant_until: 119.98", "dormant_until: 120")));
    }

    /**
     * One broken key per concern (one enemy and one level: the content's maps keep no order across files): the
     * problems come in the order the checks run in.
     */
    @Test
    void theChecksRunInTheirOrder() {
        assertEquals(
                List.of(
                        design(SHIELDS) + "models[0]: the first entry is the starter: price 0, available start",
                        design(WINGMEN) + "barks.speaker: no voice speaker 'Rookie'",
                        design(BASIS) + "beam_era_hp.tiers: one or more tiers below huge",
                        design(COILWYRM) + "attacks[0].bullet: unknown bullet class 'smal'",
                        design(LEVEL_07) + "objectives.secondary.before: no boss phase 'Broadsides'",
                        design(LEVEL_07) + "pickups[3].dropped_by.parts: 'brood-carrier' has no part 'bay 9 left'"),
                problems(
                        new Edit(LEVEL_07, "before: Broadside,", "before: Broadsides,"),
                        new Edit(LEVEL_07, "dropped_by: {parts: [bay 1 left,", "dropped_by: {parts: [bay 9 left,"),
                        new Edit(COILWYRM, "bullet: small", "bullet: smal"),
                        new Edit(WINGMEN, "  speaker: Rook", "  speaker: Rookie"),
                        new Edit(BASIS, "tiers: [small]", "tiers: [small, huge]"),
                        new Edit(SHIELDS, "price: 0, available: start", "price: 10, available: start")));
    }
}
