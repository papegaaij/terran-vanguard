package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.DataFile;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.WingmenData;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part G (design/campaign Level 20, Reverse scroll; design/player/wingmen, Radio barks): in a reversed section
 * "behind" is the top of the screen, so a rear wave there, and a re-entry from the top, gets one of the rear-wave
 * bark's "Up top!" variants ({@code top_lines}); elsewhere its usual lines.
 */
class BarksReverseTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final WingmenData.Barks DATA = CONTENT.wingmen().barks();
    private static final WingmenData.Bark REAR =
            DATA.bark(Barks.Trigger.REAR_WAVE.id()).orElseThrow();

    private final RadioQueue radio = new RadioQueue();
    private final List<Barks.Line> queued = new ArrayList<>();

    private RadioQueue.Message queue(Barks.Line line) {
        queued.add(line);
        return radio.add(
                line.speaker(),
                line.speaker(),
                line.expression(),
                line.text(),
                false,
                line.priority(),
                Optional.empty(),
                0);
    }

    @Test
    void theRearWaveBarkHasThreeTopVariants() {
        assertEquals(3, REAR.topLines().orElseThrow().size());
        assertTrue(REAR.topLines().orElseThrow().stream().allMatch(line -> line.contains("p top")));
    }

    @Test
    void aRearWaveInReversedTravelSaysUpTop() {
        Barks barks = new Barks(
                DATA,
                20,
                new double[0],
                new boolean[0],
                new double[] {24, 60},
                new boolean[] {true, false},
                new double[0],
                radio,
                this::queue);
        for (int tick = 0; tick <= SimStep.ticks(70); tick++) {
            barks.step(tick, tick * SimStep.SECONDS, 1, 0);
            radio.update((float) SimStep.SECONDS);
        }
        List<String> rear = queued.stream()
                .filter(line -> line.trigger() == Barks.Trigger.REAR_WAVE)
                .map(Barks.Line::text)
                .toList();
        assertEquals(2, rear.size(), String.valueOf(rear));
        assertTrue(REAR.topLines().orElseThrow().contains(rear.get(0)), rear.get(0));
        assertTrue(REAR.lines().contains(rear.get(1)), rear.get(1));
    }

    @Test
    void aReentryFromTheTopSaysUpTop() {
        Barks barks = new Barks(DATA, 20, new double[0], new double[0], new double[0], radio, this::queue);
        int warning = SimStep.ticks(52);
        barks.step(warning - 1, 1, 0);
        assertTrue(barks.reentry(warning, warning, true));
        barks.step(warning + SimStep.ticks(Barks.REENTRY_WARNING_SECONDS), 1, 0);
        assertEquals(1, queued.size());
        assertTrue(
                REAR.topLines().orElseThrow().contains(queued.getFirst().text()),
                queued.getFirst().text());
    }

    /**
     * Level 20 with its pursuits as plain rear waves (the pursuit formation is mapped in step G4): the first pursuit's
     * t=24 enters from the top after the turn, and Rook's bark 1.5 s ahead of it says "Up top!".
     */
    @Test
    void levelTwentysFirstPursuitGetsTheTopBark() {
        LevelScript script =
                SimSpecs.level(withoutPursuits(), CONTENT.levelKey(20).orElseThrow(), Difficulty.MEDIUM);
        assertTrue(script.reverses());
        WaveSpec first = script.waves().stream()
                .filter(wave -> wave.entry() == WaveSpec.Entry.REAR)
                .findFirst()
                .orElseThrow();
        assertEquals(24, first.t());
        assertEquals(-1, Sortie.travelAt(script, first.t()));
        Barks barks = Barks.of(DATA, script, 0, radio, this::queue);
        for (int tick = 0; tick <= SimStep.ticks(first.t()); tick++) {
            barks.step(tick, tick * SimStep.SECONDS, 1, 0);
            radio.update((float) SimStep.SECONDS);
        }
        List<String> rear = queued.stream()
                .filter(line -> line.trigger() == Barks.Trigger.REAR_WAVE)
                .map(Barks.Line::text)
                .toList();
        assertEquals(1, rear.size(), String.valueOf(rear));
        assertTrue(REAR.topLines().orElseThrow().contains(rear.getFirst()), rear.getFirst());
    }

    /** The content with Level 20's pursuits swapped for rear lines of Skitters. */
    private static Content withoutPursuits() {
        String level = "campaign/act-3-red-dust/level-20-retreat-from-hellas/data.yaml";
        List<DataFile> files = new ArrayList<>();
        boolean found = false;
        for (String path : read(ContentLoader.INDEX)
                .lines()
                .filter(line -> !line.isBlank())
                .toList()) {
            String text = read("/design/" + path);
            if (path.equals(level)) {
                found = true;
                String swapped = text.replaceAll(
                        "    formation: pursuit\\n    enemy: mote-swarm\\n    count: 10\\n    from: rear\\n"
                                + "    pursuit: \\{[^}\\n]*\\}",
                        "    formation: line abreast\n    enemy: skitter\n    count: 6\n    from: rear");
                assertTrue(!swapped.equals(text), "Level 20's pursuits");
                text = swapped;
            }
            files.add(new DataFile(path, text));
        }
        assertTrue(found, level);
        return ContentLoader.load(files);
    }

    private static String read(String resource) {
        try (InputStream in = ContentLoader.class.getResourceAsStream(resource)) {
            assertTrue(in != null, resource);
            try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n", "", "\n"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
