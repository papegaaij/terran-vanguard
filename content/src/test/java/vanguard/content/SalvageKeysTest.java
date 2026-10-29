package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Secondary;

/**
 * M9 part G, step G1: Level 34's data keys load, map to the simulation and are checked ({@link SalvageChecks}): an
 * {@code escapes} secondary over a ground object's look (the containers' "Strip the graveyard"), a {@code pickup: story}
 * dropped by a destructible group (the Helix core in the hold's clamps) with its {@code story-collected} / {@code
 * story-missed} cues, and a unit's {@code livery} (the Warden Tank's Act 5 look). Level 01 carries the level keys for
 * the test; the Warden Tank the livery.
 */
class SalvageKeysTest {
    private static final String LEVEL_01 = "campaign/act-1-first-contact/level-01-break-at-dawn/data.yaml";
    private static final String TANK = "enemies/ground/warden-tank/data.yaml";
    private static final String LEVEL_01_KEY = "act-1-first-contact/level-01-break-at-dawn";

    private static final String SECONDARY = "  secondary: {kill_ratio: 0.65, credits: 50}";
    private static final String BEACON = "  - target: beacon\n";
    private static final String PICKUPS = "  - {pickup: armour patch, dropped_by: {wave: 122, unit: last}}\n";
    private static final String FIRST_LINE = "  - {t: 2, speaker: Rook,";

    /** Level 01 with its ten cargo containers as an object escapes secondary (allowed 2, easy 3, hard 0). */
    private static final String[] CONTAINERS = {
        LEVEL_01,
        SECONDARY,
        "  secondary: {escapes: cargo-container, allowed: 2, easy: {allowed: 3}, hard: {allowed: 0}, credits: 50,"
                + " label: CONTAINERS}"
    };

    /** Level 01 with a hold of two clamps whose last drops the Helix core, and both core lines. */
    private static final String[] HOLD = {
        LEVEL_01,
        BEACON,
        "  - target: hold clamp\n    section: 3\n    count: 2\n    layer: ground\n    size: [16, 16]\n    hp: 15\n"
                + "    sprite: hold-clamp\n    group: helix-clamps\n    at: [[100, 220], [100, 260]]\n"
                + BEACON,
        LEVEL_01,
        PICKUPS,
        PICKUPS + "  - {pickup: story, story: helix-core, dropped_by: {group: helix-clamps, unit: last}}\n",
        LEVEL_01,
        FIRST_LINE,
        "  - {event: story-collected, speaker: Okafor, line: \"Helix core aboard.\"}\n"
                + "  - {event: story-missed, speaker: Okafor, line: \"Salvage tug has the core.\"}\n"
                + FIRST_LINE
    };

    /** The Warden Tank's livery line in its data (step G2: the Ascendancy's look from Act 5). */
    private static final String LIVERY = "livery: {from_act: 5, sprite: warden-tank-asc, faction: Jovian Ascendancy}";

    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i + 2 < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(
                            java.util.regex.Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return files;
    }

    private static String[] with(String[]... parts) {
        List<String> all = new ArrayList<>();
        for (String[] part : parts) {
            all.addAll(List.of(part));
        }
        return all.toArray(String[]::new);
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static boolean has(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(problem -> problem.contains(text)), text + " in " + problems);
        return true;
    }

    @Test
    void anObjectEscapesSecondaryMapsByDifficulty() {
        Content content = ContentLoader.load(files(CONTAINERS));
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary = SimSpecs.level(content, LEVEL_01_KEY, difficulty)
                    .objectives()
                    .secondary();
            assertTrue(secondary.objects(), difficulty.name());
            assertEquals(GroundObjectSpec.CARGO_CONTAINER, secondary.escapes());
            assertEquals("CONTAINERS", secondary.label());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 3;
                        case MEDIUM -> 2;
                        case HARD -> 0;
                    },
                    secondary.escapesAllowed());
        }
    }

    @Test
    void anObjectEscapesSecondaryIsChecked() {
        assertTrue(has(
                problems(with(
                        CONTAINERS,
                        new String[] {LEVEL_01, "allowed: 2, easy: {allowed: 3}", "allowed: 10, easy: {allowed: 3}"})),
                "fewer may get away than the 10"));
        assertTrue(has(
                problems(
                        LEVEL_01,
                        SECONDARY,
                        "  secondary: {escapes: cargo-crate, allowed: 1, credits: 50, label: CONTAINERS}"),
                "no wave, ground target or object look of 'cargo-crate'"));
        assertTrue(has(
                problems(with(CONTAINERS, new String[] {LEVEL_01, "label: CONTAINERS}", "label: CONTAINERS, tag: x}"})),
                "counts objects, no wave"));
    }

    @Test
    void theStoryPickupLoadsAndIsTheClampsGroupDrop() {
        Content content = ContentLoader.load(files(HOLD));
        LevelData level = content.levels().get(LEVEL_01_KEY);
        LevelData.PlacedPickup story = level.pickups().stream()
                .filter(p -> p.pickup() == Pickup.STORY)
                .findFirst()
                .orElseThrow();
        assertEquals("helix-core", story.story().orElseThrow());
        LevelScript script = SimSpecs.level(content, LEVEL_01_KEY, Difficulty.MEDIUM);
        List<GroundObjectSpec> clamps = script.groundObjects().stream()
                .filter(o -> o.look().equals("hold-clamp"))
                .toList();
        assertEquals(2, clamps.size());
        for (GroundObjectSpec clamp : clamps) {
            assertEquals(PickupType.STORY, clamp.groupDrop().orElseThrow());
            assertEquals(2, clamp.groupSize());
        }
        assertTrue(script.radio().stream()
                .map(RadioCue::trigger)
                .toList()
                .containsAll(List.of(vanguard.sim.CueTrigger.STORY_COLLECTED, vanguard.sim.CueTrigger.STORY_MISSED)));
    }

    @Test
    void theStoryPickupIsChecked() {
        assertTrue(has(
                problems(
                        with(HOLD, new String[] {LEVEL_01, "{pickup: story, story: helix-core, ", "{pickup: story, "})),
                "names its story"));
        assertTrue(has(
                problems(with(HOLD, new String[] {
                    LEVEL_01, "dropped_by: {group: helix-clamps, unit: last}}\n", "at: [100, 240]}\n"
                })),
                "dropped_by a ground-target group"));
        assertTrue(has(
                problems(with(HOLD, new String[] {
                    LEVEL_01,
                    "{pickup: story, story: helix-core, dropped_by: {group: helix-clamps, unit: last}}\n",
                    "{pickup: story, story: helix-core, dropped_by: {group: helix-clamps, unit: last}}\n"
                            + "  - {pickup: story, story: other-core, dropped_by: {group: helix-clamps, unit:"
                            + " last}}\n"
                })),
                "at most one story pickup"));
        assertTrue(has(
                problems(
                        LEVEL_01,
                        FIRST_LINE,
                        "  - {event: story-missed, speaker: Okafor, line: \"Tug.\"}\n" + FIRST_LINE),
                "a story cue in a level without a story pickup"));
        assertTrue(has(problems(LEVEL_01, "    drop: small salvage\n", "    drop: story\n"), "only a group's drop"));
    }

    @Test
    void aLiveryLoadsAndStartsInALaterAct() {
        Content content = ContentLoader.fromClasspath();
        EnemyData tank = content.enemies().get("warden-tank");
        EnemyData.Livery livery = tank.livery().orElseThrow();
        assertEquals(5, livery.fromAct());
        assertEquals("warden-tank-asc", tank.liverySprite(5).orElseThrow());
        assertFalse(tank.liverySprite(3).isPresent(), "its own look in Act 3");
        assertEquals("Jovian Ascendancy", tank.factionIn(5));
        assertEquals(tank.faction(), tank.factionIn(4));
        assertTrue(has(problems(TANK, LIVERY, "livery: {from_act: 3, sprite: x, faction: Y}"), "from a later act"));
        assertTrue(has(problems(TANK, LIVERY, "livery: {from_act: 9, sprite: x, faction: Y}"), "livery.from_act"));
    }

    /** M9 part G (step G2): the music's motif window ends at {@code to} (Level 34's t=106-124), after {@code from}. */
    @Test
    void aMotifWindowEndsAtItsTo() {
        LevelData.Music.Motif window = new LevelData.Music.Motif(106, java.util.Map.of(), java.util.Optional.of(124.0));
        assertFalse(window.on(4, 105.9));
        assertTrue(window.on(4, 106));
        assertTrue(window.on(4, 123.9));
        assertFalse(window.on(4, 124), "out again at its to");
        assertTrue(new LevelData.Music.Motif(106, java.util.Map.of()).on(6, 180), "to the end without a to");
        assertThrows(
                IllegalArgumentException.class,
                () -> new LevelData.Music.Motif(106, java.util.Map.of(), java.util.Optional.of(100.0)));
        assertTrue(has(
                problems(
                        "campaign/act-5-the-belt/level-34-salvage-run/data.yaml",
                        "motif: {from: 106, to: 124",
                        "motif: {from: 106, to: 100"),
                "motif.to"));
    }
}
