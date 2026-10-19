package vanguard.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;

/**
 * The placeholder audit of the Acts 1–2 release (M6 part D, design/art-direction/production): every
 * PNG and OGG the game ships in {@code assets/} is a production file (a {@code Source} chunk or a
 * {@code SOURCE} comment, {@link FinalArt}) or one of the known exceptions below, and every sprite
 * root Levels 01–14 use has frames, so no unit of theirs falls back to the shapes drawn in code
 * ({@code render/Placeholders}, the flat tow and convoy looks).
 */
class PlaceholderAuditTest {
    private static final Path ROOT = Path.of(System.getProperty("vanguard.rootDir", ".."));
    private static final Path ASSETS = ROOT.resolve("assets");

    /** The last level of the release (Acts 1–2). */
    private static final int LAST_LEVEL = 14;

    private static final String SYNTHESIZED =
            "synthesized concept sound, chosen; production keeps synthesized sounds as they are";

    private static final String ROUND_34 =
            "chosen in concept round 34 (Levels 12-14's sounds, user 2026-10-12): the concept file, already"
                    + " built from the Freesound originals or synthesized by tools/concept/audio/sfx_r34-36.py";

    private static final String ROUND_37 =
            "M7 part A's arsenal sounds (round 36's chosen beam loop, round 37's picks of 2026-10-13):"
                    + " the concept file, built from CC0 originals or synthesized by tools/concept/audio/sfx_r36b.py"
                    + " and sfx_r37.py";

    private static final String ROUND_40 =
            "M7 part D's Level 17 sounds: round 40's proposals (option a until the user's pick),"
                    + " synthesized by tools/concept/audio/sfx_r40.py";

    private static final String ROUND_38 =
            "M7 part B's Level 15 sounds: the Burrower's eruption chosen in round 36 and round 38's picks"
                    + " (user 2026-10-14), the concept files from CC0/CC-BY originals or"
                    + " synthesized by tools/concept/audio/sfx_r36b.py and sfx_r38.py";

    /**
     * The shipped files without a production mark, each with why (path relative to {@code assets/}).
     * A new placeholder fails the audit until it is listed here; a listed file that turned final or
     * left {@code assets/} fails it too, so the list never goes stale.
     */
    private static final Map<String, String> KNOWN = new TreeMap<>(Map.ofEntries(
            // The act titles' lettering: tools/concept/ui_assets.py, as the production plan says
            // (pipeline/build.gradle.kts, importPlaceholders); the title cards' stills are final.
            Map.entry("ui/act-1-first-contact-title.png", "act title lettering from tools/concept/ui_assets.py"),
            Map.entry("ui/act-2-homefront-title.png", "act title lettering from tools/concept/ui_assets.py"),
            Map.entry("ui/act-3-red-dust-title.png", "act title lettering from tools/concept/ui_assets.py"),
            Map.entry("voice/choir/voice-choir-sings-r29-b.ogg", "the Choir's sung sting, round 29 b (synthesized)"),
            Map.entry("sfx/pickup-r01-a.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-r01-b.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-r01-c.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-armour-patch-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-salvage-large-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-salvage-small-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-shield-cell-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/pickup-special-charge-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-equip-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-grade-stamp-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-menu-back-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-menu-confirm-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-menu-move-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-shop-buy-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-shop-denied-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-shop-sell-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-tally-tick-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-tally-total-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-typewriter-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-upgrade-r08-a.ogg", SYNTHESIZED),
            Map.entry("sfx/launch-rail-r11-b.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-edge-warning-r11-b.ogg", SYNTHESIZED),
            Map.entry("sfx/enemy-leviathan-cry-r16-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-save-r27-b.ogg", SYNTHESIZED),
            Map.entry("sfx/weapon-mine-arm-r28-a.ogg", SYNTHESIZED),
            Map.entry("sfx/driftjelly-pulse-r33-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ambience-storm-rain-r34-a.ogg", ROUND_34),
            Map.entry("sfx/thunder-1-r34-b.ogg", ROUND_34),
            Map.entry("sfx/thunder-2-r34-b.ogg", ROUND_34),
            Map.entry("sfx/thunder-3-r34-b.ogg", ROUND_34),
            Map.entry("sfx/gust-r34-b.ogg", ROUND_34),
            Map.entry("sfx/lamprey-latch-r34-a.ogg", ROUND_34),
            Map.entry("sfx/lamprey-drain-r34-a.ogg", ROUND_34),
            Map.entry("sfx/lamprey-shake-r34-a.ogg", ROUND_34),
            Map.entry("sfx/sonde-chime-r34-a.ogg", ROUND_34),
            Map.entry("sfx/relay-hit-r35-a.ogg", ROUND_34),
            Map.entry("sfx/integrity-alarm-r35-a.ogg", ROUND_34),
            Map.entry("sfx/relay-destroyed-r35-a.ogg", ROUND_34),
            Map.entry("sfx/drone-launch-r35-a.ogg", ROUND_34),
            Map.entry("sfx/grid-online-r35-b.ogg", ROUND_34),
            Map.entry("sfx/root-burst-r36-a.ogg", ROUND_34),
            Map.entry("sfx/root-growth-r36-a.ogg", ROUND_34),
            Map.entry("sfx/tear-free-roar-r36-b.ogg", ROUND_34),
            Map.entry("sfx/canopy-withering-r36-b.ogg", ROUND_34),
            Map.entry("sfx/canopy-hum-r36-a.ogg", ROUND_34),
            // M7 part A, the Act 3 arsenal: the Ion Beam's loop chosen in round 36 (S.4 = a) and round
            // 37's picks (user, 2026-10-13: S.2, S.4, S.6-S.10 b, the rest a).
            Map.entry("sfx/ion-beam-loop-r36-a.ogg", ROUND_37),
            Map.entry("sfx/ion-beam-start-r37-a.ogg", ROUND_37),
            Map.entry("sfx/ion-beam-stop-r37-b.ogg", ROUND_37),
            Map.entry("sfx/beam-impact-r37-a.ogg", ROUND_37),
            Map.entry("sfx/beam-impact-rock-r37-b.ogg", ROUND_37),
            Map.entry("sfx/deflector-tick-r37-a.ogg", ROUND_37),
            Map.entry("sfx/deflector-return-r37-b.ogg", ROUND_37),
            Map.entry("sfx/drone-break-r37-a.ogg", ROUND_37),
            Map.entry("sfx/drone-rebuild-r37-a.ogg", ROUND_37),
            Map.entry("sfx/emp-ring-r37-b.ogg", ROUND_37),
            Map.entry("sfx/emp-powerdown-r37-b.ogg", ROUND_37),
            Map.entry("sfx/lance-rumble-r37-b.ogg", ROUND_37),
            Map.entry("sfx/overcharge-whoosh-r37-b.ogg", ROUND_37),
            Map.entry("sfx/overcharge-end-r37-a.ogg", ROUND_37),
            Map.entry("sfx/cooldown-ready-r37-a.ogg", ROUND_37),
            Map.entry("sfx/dash-r37-a.ogg", ROUND_37),
            Map.entry("sfx/burrow-eruption-r36-a.ogg", ROUND_38),
            Map.entry("sfx/burrow-bulge-r38-b.ogg", ROUND_38),
            Map.entry("sfx/burrow-dive-r38-b.ogg", ROUND_38),
            Map.entry("sfx/entry-roar-r38-a.ogg", ROUND_38),
            Map.entry("sfx/mars-dust-r38-a.ogg", ROUND_38),
            // M7 part C, Level 16: the scrape chosen in round 36 (S.3 = b), round 39's proposals (a until the pick)
            Map.entry("sfx/canyon-scrape-r36-b.ogg", ROUND_38),
            Map.entry("sfx/tendril-hum-r39-a.ogg", ROUND_38),
            Map.entry("sfx/tendril-burn-r39-a.ogg", ROUND_38),
            Map.entry("sfx/tendril-break-r39-a.ogg", ROUND_38),
            Map.entry("sfx/crawler-walk-r39-a.ogg", ROUND_38),
            Map.entry("sfx/mars-canyon-r39-a.ogg", ROUND_38),
            // M7 part D, Level 17: round 40's proposals (a until the pick), synthesized by sfx_r40.py
            Map.entry("sfx/dome-hit-r40-a.ogg", ROUND_40),
            Map.entry("sfx/dome-break-r40-a.ogg", ROUND_40),
            Map.entry("sfx/herald-song-r40-a.ogg", ROUND_40),
            Map.entry("sfx/herald-aura-r40-a.ogg", ROUND_40),
            Map.entry("sfx/aura-absorb-r40-a.ogg", ROUND_40),
            Map.entry("sfx/aura-collapse-r40-a.ogg", ROUND_40),
            Map.entry("sfx/shellback-walk-r40-a.ogg", ROUND_40),
            Map.entry("sfx/shellback-lob-r40-a.ogg", ROUND_40),
            Map.entry("sfx/shellback-curl-r40-a.ogg", ROUND_40),
            Map.entry("sfx/shellback-roll-r40-a.ogg", ROUND_40),
            Map.entry("sfx/shellback-uncurl-r40-a.ogg", ROUND_40),
            Map.entry("sfx/mars-colony-r40-a.ogg", ROUND_40)));

    private final Content content = ContentLoader.fromClasspath();

    @Test
    void everyShippedImageAndSoundIsFinalOrAKnownException() throws IOException {
        List<String> unmarked = new ArrayList<>();
        Set<String> notFinal = new TreeSet<>();
        try (Stream<Path> files = Files.walk(ASSETS)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                String name = file.getFileName().toString();
                String path = ASSETS.relativize(file).toString().replace('\\', '/');
                boolean done;
                if (name.endsWith(".png")) {
                    done = FinalArt.isFinal(file);
                } else if (name.endsWith(".ogg")) {
                    done = FinalArt.isFinalSound(file);
                } else {
                    continue;
                }
                if (!done) {
                    notFinal.add(path);
                    if (!KNOWN.containsKey(path)) {
                        unmarked.add(path);
                    }
                }
            }
        }
        assertEquals(List.of(), unmarked, "shipped files without a Source mark, not listed as known exceptions");
        assertEquals(KNOWN.keySet(), notFinal, "the known exceptions (a listed file turned final or is gone)");
    }

    /**
     * Every sprite root a level of the release uses (its units, allies, set pieces and effects; {@link
     * SpriteUse#roots}) has frames in {@code assets/sprites}, and every unit's own sprite exists, so
     * the game never draws it as a placeholder shape (a hull boss or a set piece: its parts).
     */
    @Test
    void everyLookOfLevels01To14HasProductionFrames() throws IOException {
        Set<String> names = new TreeSet<>();
        try (Stream<Path> files = Files.walk(ASSETS.resolve("sprites"))) {
            files.filter(file -> file.getFileName().toString().endsWith(".png"))
                    .forEach(file -> names.add(file.getFileName().toString().replaceFirst("(_\\d+)?\\.png$", "")));
        }
        Path sprites = ASSETS.resolve("sprites");
        List<String> missing = new ArrayList<>();
        Set<Integer> audited = new TreeSet<>();
        for (Map.Entry<String, LevelData> level : content.levels().entrySet()) {
            int number = Content.levelNumber(level.getKey());
            if (number > LAST_LEVEL) {
                continue;
            }
            audited.add(number);
            // A hull boss and a set piece are drawn from their parts (BossLooks: hull, bell, neck, ...;
            // the Leviathan's down and cross views with its fins and vents), not one sprite each.
            Set<String> composite = new TreeSet<>();
            level.getValue().boss().ifPresent(boss -> composite.add(boss.enemy()));
            level.getValue().setPieces().ifPresent(pieces -> pieces.forEach(piece -> composite.add(piece.enemy())));
            for (String root : SpriteUse.roots(content, level.getValue())) {
                boolean drawn = names.stream().anyMatch(name -> name.equals(root) || name.startsWith(root + "-"));
                boolean unit = !composite.contains(root)
                        && (content.enemies().containsKey(root)
                                || content.allies().allies().containsKey(root));
                if (!drawn || (unit && !FinalArt.exists(sprites, root))) {
                    missing.add(level.getKey() + ": " + root);
                }
            }
        }
        assertEquals(List.of(), missing, "level looks without production frames (drawn as placeholders)");
        assertEquals(LAST_LEVEL, audited.size(), "levels audited: " + audited);
    }
}
