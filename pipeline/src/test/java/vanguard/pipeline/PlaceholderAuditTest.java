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
            "M7 part D's Level 17 rings' collapse: round 40's proposal still standing (option a),"
                    + " synthesized by tools/concept/audio/sfx_r40.py";

    private static final String ROUND_40_DOME =
            "M7 part D's dome break: round 40's redo c, chosen in round 41 (S.h, user 2026-10-19): the concept"
                    + " file, mixed from CC0 Freesound originals by tools/concept/audio/sfx_r40_dome_break.py";

    private static final String ROUND_42 =
            "M7 part F's Level 19 belt loop: round 42's proposal (option a until the user's pick),"
                    + " synthesized by tools/concept/audio/sfx_r42.py";

    private static final String ROUND_43 =
            "round 43's picks (user 2026-10-20): Level 19's walker, vent and tank sounds and Level 20's, the"
                    + " concept files mixed from CC0/CC-BY Freesound originals (CREDITS.md)";

    private static final String ROUND_44 =
            "round 44's picks (user 2026-10-20): Level 21's Dust Colossus sounds and the Revenant Walker's lance"
                    + " and shockwave, the concept files mixed from CC0/CC-BY Freesound originals (CREDITS.md); the"
                    + " ridge pass a mirrored right to left by tools/concept/audio/sfx_r44.py --mirror";

    private static final String ROUND_46 =
            "round 46's picks (user 2026-10-21): Acts 2-3 sounds redone from the database, the concept files mixed"
                    + " from CC0/CC-BY Freesound originals (CREDITS.md) by tools/concept/audio/sfx_r46j.py and"
                    + " sfx_r46k.py";

    private static final String ROUND_51 =
            "M8 part B: round 51's picks for Level 22 (user 2026-10-22; the Freesound HQ previews of CC0/CC-BY"
                    + " sounds, CREDITS.md, by tools/concept/audio/sfx_r51b.py), to be rebuilt from the originals";
    private static final String ROUND_52 =
            "M8 part C: round 52's picks for Level 23 (user 2026-10-22; the Freesound HQ previews of CC0/CC-BY sounds,"
                    + " CREDITS rows from step AUD-C, by tools/concept/audio/sfx_r52c.py; the under-water explosion"
                    + " ladder's a until its pick)";
    private static final String ROUND_53 =
            "M8 part D: round 53's picks for Level 24 (user 2026-10-23; the Freesound HQ previews of CC0/CC-BY sounds,"
                    + " CREDITS rows from step AUD-D, by tools/concept/audio/sfx_r53d.py)";
    private static final String ROUND_54 =
            "M8 part E: round 54's picks for Level 25 (user 2026-10-23; the Freesound HQ previews of CC0/CC-BY sounds,"
                    + " CREDITS rows from step AUD-E, by tools/concept/audio/sfx_r54e.py)";
    private static final String ROUND_55 =
            "M8 part F: round 55's picks for Level 26 (user 2026-10-24; the Freesound HQ previews of CC0/CC-BY sounds,"
                    + " CREDITS rows from step AUD-F, by tools/concept/audio/sfx_r55f.py)";
    private static final String ROUND_57 =
            "M8 part G: round 57's picks for Level 27 (user 2026-10-25; S.2, S.6, S.7, S.9 and S.12 b, the rest a; the"
                    + " Freesound HQ previews of CC0/CC-BY sounds, CREDITS rows from step AUD-G; the scan ping"
                    + " synthesized; by tools/concept/audio/sfx_r57g.py)";
    private static final String ROUND_58 =
            "M8 part H: round 58's picks for Level 28 (the user, 2026-10-26: a but the implant's zap, the Maw's death and"
                    + " the cruisers' rise b), from the Freesound HQ previews of CC0/CC-BY sounds until the originals"
                    + " (CREDITS rows from step AUD-H; by tools/concept/audio/sfx_r58h.py)";
    private static final String ROUND_60 =
            "M9 part A: round 60's picks for the Act 5 systems (the user, 2026-10-27: S.1 a, S.2 b, S.3 a, S.4 b, S.5 a,"
                    + " S.6 b, S.10-S.13 a), from the Freesound HQ previews of CC0/CC-BY sounds until the originals (CREDITS"
                    + " rows from step AUD-P; by tools/concept/audio/sfx_r60.py)";
    private static final String ROUND_60_B =
            "M9 part B: round 60's pick for the belt station's ambience bed (the user, 2026-10-27: S.17 b), from the"
                    + " Freesound HQ previews of CC0 sounds until the originals (CREDITS rows from step AUD-P; by"
                    + " tools/concept/audio/sfx_r60.py)";
    private static final String ROUND_60_MUSIC =
            "M9 part B: track 31, Rook's return sting (round 60 M.2 a \"Horn Call\", the user's pick, synthesized by"
                    + " tools/concept/audio/music_r60.py), the concept file";
    private static final String ROUND_62 =
            "M9 part B, step B4: round 62's proposals for Level 29 (step AUD-B), their a variants until the user's picks,"
                    + " from the Freesound HQ previews of CC0/CC-BY sounds (the lock beep synthesized; CREDITS rows from"
                    + " step AUD-B; by tools/concept/audio/sfx_r62.py)";
    private static final String ROUND_60_C =
            "M9 part C, step C4: round 60's picks for Level 30 (the user, 2026-10-27: S.9 a, S.14 a, S.15 a, S.18 a), from"
                    + " the Freesound HQ previews of CC0 sounds until the originals (the blink synthesized; CREDITS rows"
                    + " from step AUD-P; by tools/concept/audio/sfx_r60.py)";
    private static final String ROUND_63 =
            "M9 part C, step C4: round 63's proposals for Level 30 (step AUD-C), their a variants until the user's picks,"
                    + " from the Freesound HQ previews of CC0/CC-BY sounds (the arm tick and the buoy tick synthesized;"
                    + " CREDITS rows from step AUD-C; by tools/concept/audio/sfx_r63.py)";
    private static final String ROUND_64 =
            "M9 part D, step D4: round 64's proposals for Level 31 (step AUD-D), their a variants until the user's picks,"
                    + " from the Freesound HQ previews of CC0/CC-BY sounds (CREDITS rows from step AUD-D; by"
                    + " tools/concept/audio/sfx_r64.py)";
    private static final String ROUND_58_MUSIC =
            "M8 part H: track 30, the reveal sting (round 58 a, the production of round 48's pick, synthesized by"
                    + " tools/concept/audio/music_r58h.py), a stand-in until the user's pick";
    private static final String ROUND_55_MUSIC =
            "M8 part F: track 10's dark stem (round 51 a, by tools/concept/audio/music_r51b.py) and its warm pad (round"
                    + " 55 b, the user's pick, by tools/concept/audio/sfx_r55f.py), the concept files on the theme's grid"
                    + " (Level 26's stem_times)";
    private static final String ROUND_50 =
            "M8 part A: round 50's picks for the Act 4 arsenal (user 2026-10-22; the Freesound HQ previews of"
                    + " CC0/CC-BY sounds, CREDITS.md, by tools/concept/audio/sfx_r50a.py; the limit chime synthesized),"
                    + " to be rebuilt from the originals";

    private static final String ROUND_47 =
            "round 47's and 49's picks (user 2026-10-22): the Herald's song, the Leviathan's cry, the launch rail"
                    + " and the domes' hit, the concept files cut from the Freesound previews of CC0/CC-BY sounds"
                    + " (CREDITS.md) by tools/concept/audio/sfx_r47l.py and sfx_r49m.py until their originals";

    private static final String ROUND_41_MUSIC =
            "track 9 \"Grey Signal\" and its base and motif stems (M7 part E, round 41 a, proposed): the concept"
                    + " files, rendered by tools/concept/audio/music_r41.py";

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
            Map.entry("sfx/ui-edge-warning-r11-b.ogg", SYNTHESIZED),
            Map.entry("sfx/ui-save-r27-b.ogg", SYNTHESIZED),
            Map.entry("sfx/weapon-mine-arm-r28-a.ogg", SYNTHESIZED),
            Map.entry("sfx/ambience-storm-rain-r34-a.ogg", ROUND_34),
            Map.entry("sfx/thunder-1-r34-b.ogg", ROUND_34),
            Map.entry("sfx/thunder-2-r34-b.ogg", ROUND_34),
            Map.entry("sfx/thunder-3-r34-b.ogg", ROUND_34),
            Map.entry("sfx/gust-r34-b.ogg", ROUND_34),
            Map.entry("sfx/lamprey-latch-r34-a.ogg", ROUND_34),
            Map.entry("sfx/lamprey-shake-r34-a.ogg", ROUND_34),
            Map.entry("sfx/relay-hit-r35-a.ogg", ROUND_34),
            Map.entry("sfx/integrity-alarm-r35-a.ogg", ROUND_34),
            Map.entry("sfx/relay-destroyed-r35-a.ogg", ROUND_34),
            Map.entry("sfx/grid-online-r35-b.ogg", ROUND_34),
            Map.entry("sfx/root-burst-r36-a.ogg", ROUND_34),
            Map.entry("sfx/root-growth-r36-a.ogg", ROUND_34),
            Map.entry("sfx/tear-free-roar-r36-b.ogg", ROUND_34),
            Map.entry("sfx/canopy-withering-r36-b.ogg", ROUND_34),
            // M7 part A, the Act 3 arsenal: the Ion Beam's loop chosen in round 36 (S.4 = a) and round
            // 37's picks (user, 2026-10-13: S.2, S.4, S.6-S.10 b, the rest a).
            Map.entry("sfx/ion-beam-loop-r36-a.ogg", ROUND_37),
            Map.entry("sfx/ion-beam-start-r37-a.ogg", ROUND_37),
            Map.entry("sfx/beam-impact-rock-r37-b.ogg", ROUND_37),
            Map.entry("sfx/deflector-tick-r37-a.ogg", ROUND_37),
            Map.entry("sfx/deflector-return-r37-b.ogg", ROUND_37),
            Map.entry("sfx/drone-break-r37-a.ogg", ROUND_37),
            Map.entry("sfx/lance-rumble-r37-b.ogg", ROUND_37),
            Map.entry("sfx/cooldown-ready-r37-a.ogg", ROUND_37),
            Map.entry("sfx/burrow-eruption-r36-a.ogg", ROUND_38),
            Map.entry("sfx/burrow-bulge-r38-b.ogg", ROUND_38),
            Map.entry("sfx/entry-roar-r38-a.ogg", ROUND_38),
            Map.entry("sfx/mars-dust-r38-a.ogg", ROUND_38),
            // M7 part C, Level 16: the scrape chosen in round 36 (S.3 = b), round 39's tendril hum (kept in round 46)
            Map.entry("sfx/canyon-scrape-r36-b.ogg", ROUND_38),
            Map.entry("sfx/tendril-hum-r39-a.ogg", ROUND_38),
            // M7 part D, Level 17: round 40's proposal still standing, synthesized by sfx_r40.py
            Map.entry("sfx/dome-break-r40-c.ogg", ROUND_40_DOME),
            Map.entry("sfx/aura-collapse-r40-a.ogg", ROUND_40),
            // M7 part E, Level 18: the `mars-storm` bed and track 9 with its two stems
            Map.entry(
                    "sfx/dust-storm-bed-r36-a.ogg",
                    "M7 part E's `mars-storm` bed, chosen in round 36 (S.5 = a): the concept file, built from"
                            + " CC0/CC-BY originals by tools/concept/audio/sfx_r36b.py"),
            // M7 part F, Level 19: round 42's belt loop
            Map.entry("sfx/belt-rumble-r42-a.ogg", ROUND_42),
            // round 43's picks: Level 19's walker, vent and tank sounds; Level 20's basin rumble, swarm hum and lunge
            Map.entry("sfx/walker-hatch-r43-b.ogg", ROUND_43),
            Map.entry("sfx/walker-flame-tell-r43-a.ogg", ROUND_43),
            Map.entry("sfx/walker-flame-r43-b.ogg", ROUND_43),
            Map.entry("sfx/walker-vent-hit-r43-a.ogg", ROUND_43),
            Map.entry("sfx/walker-death-r43-a.ogg", ROUND_43),
            Map.entry("sfx/walker-wreck-settle-r43-a.ogg", ROUND_43),
            Map.entry("sfx/vent-tell-r43-a.ogg", ROUND_43),
            Map.entry("sfx/vent-burst-r43-a.ogg", ROUND_43),
            Map.entry("sfx/tank-tracks-r43-a.ogg", ROUND_43),
            Map.entry("sfx/tank-turret-r43-a.ogg", ROUND_43),
            Map.entry("sfx/tank-burst-r43-a.ogg", ROUND_43),
            Map.entry("sfx/basin-rumble-r43-a.ogg", ROUND_43),
            Map.entry("sfx/swarm-hum-r43-a.ogg", ROUND_43),
            Map.entry("sfx/swarm-lunge-r43-a.ogg", ROUND_43),
            // round 44's picks: the Dust Colossus (Level 21), the walker's lance and shockwave
            Map.entry("sfx/colossus-bulge-r44-a.ogg", ROUND_44),
            Map.entry("sfx/colossus-eruption-r44-a.ogg", ROUND_44),
            Map.entry("sfx/colossus-suction-r44-a.ogg", ROUND_44),
            Map.entry("sfx/colossus-bite-r44-a.ogg", ROUND_44),
            Map.entry("sfx/colossus-death-r44-a.ogg", ROUND_44),
            Map.entry("sfx/colossus-ridge-r44-a.ogg", ROUND_44),
            Map.entry("sfx/walker-lance-charge-r44-b.ogg", ROUND_44),
            Map.entry("sfx/walker-lance-shot-r44-a.ogg", ROUND_44),
            Map.entry("sfx/walker-shockwave-r44-a.ogg", ROUND_44),
            // round 46's picks: the Act 2-3 sounds the user had redone from the database
            Map.entry("sfx/ghost-drone-drop-r46-b.ogg", ROUND_46),
            Map.entry("sfx/ghost-drone-glint-r46-b.ogg", ROUND_46),
            Map.entry("sfx/ghost-drone-burst-r46-a.ogg", ROUND_46),
            Map.entry("sfx/ghost-drone-death-r46-b.ogg", ROUND_46),
            Map.entry("sfx/devil-spin-r46-a.ogg", ROUND_46),
            Map.entry("sfx/devil-open-r46-a.ogg", ROUND_46),
            Map.entry("sfx/devil-spiral-r46-a.ogg", ROUND_46),
            Map.entry("sfx/shellback-curl-r46-b.ogg", ROUND_46),
            Map.entry("sfx/shellback-uncurl-r46-b.ogg", ROUND_46),
            Map.entry("sfx/shellback-roll-r46-b.ogg", ROUND_46),
            Map.entry("sfx/shellback-lob-r46-b.ogg", ROUND_46),
            Map.entry("sfx/shellback-walk-r46-b.ogg", ROUND_46),
            Map.entry("sfx/crawler-walk-r46-a.ogg", ROUND_46),
            Map.entry("sfx/tendril-burn-r46-a.ogg", ROUND_46),
            Map.entry("sfx/tendril-break-r46-b.ogg", ROUND_46),
            Map.entry("sfx/burrow-dive-r46-a.ogg", ROUND_46),
            Map.entry("sfx/herald-aura-r46-b.ogg", ROUND_46),
            Map.entry("sfx/aura-absorb-r46-b.ogg", ROUND_46),
            Map.entry("sfx/walker-stomp-r46-a.ogg", ROUND_46),
            Map.entry("sfx/tank-death-r46-a.ogg", ROUND_46),
            Map.entry("sfx/canopy-hum-r46-a.ogg", ROUND_46),
            Map.entry("sfx/mars-canyon-r46-a.ogg", ROUND_46),
            Map.entry("sfx/mars-colony-r46-b.ogg", ROUND_46),
            Map.entry("sfx/mars-foundry-r46-b.ogg", ROUND_46),
            Map.entry("sfx/driftjelly-pulse-r46-b.ogg", ROUND_46),
            Map.entry("sfx/lamprey-drain-r46-b.ogg", ROUND_46),
            Map.entry("sfx/sonde-chime-r46-a.ogg", ROUND_46),
            Map.entry("sfx/drone-launch-r46-a.ogg", ROUND_46),
            Map.entry("sfx/dash-r46-b.ogg", ROUND_46),
            Map.entry("sfx/overcharge-whoosh-r46-b.ogg", ROUND_46),
            Map.entry("sfx/overcharge-end-r46-b.ogg", ROUND_46),
            Map.entry("sfx/emp-ring-r46-b.ogg", ROUND_46),
            Map.entry("sfx/emp-powerdown-r46-a.ogg", ROUND_46),
            Map.entry("sfx/ion-beam-stop-r46-a.ogg", ROUND_46),
            Map.entry("sfx/beam-impact-r46-a.ogg", ROUND_46),
            Map.entry("sfx/drone-rebuild-r46-a.ogg", ROUND_46),
            // round 47's and 49's picks, from the Freesound previews until the originals
            Map.entry("sfx/herald-song-r47-a.ogg", ROUND_47),
            Map.entry("sfx/enemy-leviathan-cry-r47-b.ogg", ROUND_47),
            Map.entry("sfx/launch-rail-r47-b.ogg", ROUND_47),
            Map.entry("sfx/dome-hit-r49-b.ogg", ROUND_47),
            // M8 part A: round 50's picks, from the Freesound previews until the originals
            Map.entry("sfx/shot-harpoon-r50-b.ogg", ROUND_50),
            Map.entry("sfx/harpoon-run-r50-a.ogg", ROUND_50),
            Map.entry("sfx/harpoon-hit-r50-a.ogg", ROUND_50),
            Map.entry("sfx/shot-depth-charge-r50-b.ogg", ROUND_50),
            Map.entry("sfx/special-limit-r50-b.ogg", ROUND_50),
            // M8 part B: round 51's a variants, from the Freesound previews until the pick and the originals
            Map.entry("sfx/geyser-tell-r51-b.ogg", ROUND_51),
            Map.entry("sfx/geyser-burst-r51-a.ogg", ROUND_51),
            Map.entry("sfx/rig-collar-r51-b.ogg", ROUND_51),
            Map.entry("sfx/rig-ring-r51-b.ogg", ROUND_51),
            Map.entry("sfx/rig-death-r51-b.ogg", ROUND_51),
            Map.entry("sfx/europa-ice-r51-a.ogg", ROUND_51),
            Map.entry("sfx/conamara-station-r51-a.ogg", ROUND_51),
            // M8 part C: round 52's picks (the explosion ladder's a until its pick), from the Freesound previews until
            // the
            // originals
            Map.entry("sfx/waterline-cross-r52-a.ogg", ROUND_52),
            Map.entry("sfx/hydro-refit-r52-a.ogg", ROUND_52),
            Map.entry("sfx/scrape-under-r52-a.ogg", ROUND_52),
            Map.entry("sfx/eel-pulse-r52-b.ogg", ROUND_52),
            Map.entry("sfx/eel-segment-r52-b.ogg", ROUND_52),
            Map.entry("sfx/glassfin-shatter-r52-b.ogg", ROUND_52),
            Map.entry("sfx/pump-hum-r52-b.ogg", ROUND_52),
            Map.entry("sfx/pump-break-r52-b.ogg", ROUND_52),
            Map.entry("sfx/europa-under-r52-a.ogg", ROUND_52),
            Map.entry("sfx/explosion-underwater-tiny-r52-a.ogg", ROUND_52),
            Map.entry("sfx/explosion-underwater-small-r52-a.ogg", ROUND_52),
            Map.entry("sfx/explosion-underwater-medium-r52-a.ogg", ROUND_52),
            Map.entry("sfx/explosion-underwater-large-r52-a.ogg", ROUND_52),
            // M8 part D: round 53's picks, from the Freesound previews until the originals
            Map.entry("sfx/ray-rise-r53-b.ogg", ROUND_53),
            Map.entry("sfx/ray-glide-r53-a.ogg", ROUND_53),
            Map.entry("sfx/ray-fan-r53-b.ogg", ROUND_53),
            Map.entry("sfx/ray-death-r53-a.ogg", ROUND_53),
            Map.entry("sfx/barbfin-needle-r53-b.ogg", ROUND_53),
            Map.entry("sfx/reef-iris-r53-a.ogg", ROUND_53),
            Map.entry("sfx/reef-death-r53-b.ogg", ROUND_53),
            Map.entry("sfx/beacon-pulse-r53-a.ogg", ROUND_53),
            Map.entry("sfx/wreck-hatch-r53-b.ogg", ROUND_53),
            // M8 part E: round 54's picks (S.2 and S.6 b, the rest a), from the Freesound previews until the originals
            Map.entry("sfx/siren-ring-r54-a.ogg", ROUND_54),
            Map.entry("sfx/siren-slow-r54-b.ogg", ROUND_54),
            Map.entry("sfx/siren-death-r54-a.ogg", ROUND_54),
            Map.entry("sfx/nautilus-spin-r54-a.ogg", ROUND_54),
            Map.entry("sfx/nautilus-uncoil-r54-a.ogg", ROUND_54),
            Map.entry("sfx/nautilus-death-r54-b.ogg", ROUND_54),
            Map.entry("sfx/lure-hum-r55-a.ogg", ROUND_55),
            Map.entry("sfx/angler-lunge-r55-a.ogg", ROUND_55),
            Map.entry("sfx/crown-hum-r55-a.ogg", ROUND_55),
            Map.entry("sfx/torpedo-trails-r55-a.ogg", ROUND_55),
            Map.entry("sfx/rook-hit-r55-a.ogg", ROUND_55),
            Map.entry("sfx/rook-spin-r55-b.ogg", ROUND_55),
            Map.entry("sfx/headlight-click-r55-b.ogg", ROUND_55),
            Map.entry("sfx/choir-static-r55-b.ogg", ROUND_55),
            Map.entry("sfx/plume-tell-r57-b.ogg", ROUND_57),
            Map.entry("sfx/plume-eruption-r57-a.ogg", ROUND_57),
            Map.entry("sfx/bend-swish-r57-a.ogg", ROUND_57),
            Map.entry("sfx/torpedo-launch-r57-a.ogg", ROUND_57),
            Map.entry("sfx/torpedo-run-r57-b.ogg", ROUND_57),
            Map.entry("sfx/torpedo-impact-r57-b.ogg", ROUND_57),
            Map.entry("sfx/hunter-break-r57-a.ogg", ROUND_57),
            Map.entry("sfx/beacon-hum-r57-a.ogg", ROUND_57),
            Map.entry("sfx/beacon-break-r57-b.ogg", ROUND_57),
            Map.entry("sfx/marlin-crack-r57-a.ogg", ROUND_57),
            Map.entry("sfx/scan-ping-r57-b.ogg", ROUND_57),
            Map.entry("sfx/shaft-climb-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-arrival-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-gill-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-throat-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-sweep-groan-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-sweep-swing-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-shed-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-dive-r58-a.ogg", ROUND_58),
            Map.entry("sfx/maw-lunge-r58-a.ogg", ROUND_58),
            Map.entry("sfx/implant-spark-r58-a.ogg", ROUND_58),
            Map.entry("sfx/implant-zap-r58-b.ogg", ROUND_58),
            Map.entry("sfx/maw-death-r58-b.ogg", ROUND_58),
            Map.entry("sfx/breakout-r58-a.ogg", ROUND_58),
            Map.entry("sfx/cruiser-rise-r58-b.ogg", ROUND_58),
            Map.entry("sfx/cruiser-broadside-r58-a.ogg", ROUND_58),
            Map.entry("sfx/cruiser-flak-r58-a.ogg", ROUND_58),
            Map.entry("sfx/cruiser-pd-r58-a.ogg", ROUND_58),
            Map.entry("sfx/enemy-shield-hit-r60-a.ogg", ROUND_60),
            Map.entry("sfx/enemy-shield-break-r60-b.ogg", ROUND_60),
            Map.entry("sfx/enemy-shield-refill-r60-a.ogg", ROUND_60),
            Map.entry("sfx/homing-launch-r60-b.ogg", ROUND_60),
            Map.entry("sfx/homing-run-r60-a.ogg", ROUND_60),
            Map.entry("sfx/homing-down-r60-b.ogg", ROUND_60),
            Map.entry("sfx/brownout-hum-r60-a.ogg", ROUND_60),
            Map.entry("sfx/intercept-open-r60-a.ogg", ROUND_60),
            Map.entry("sfx/intercept-close-r60-a.ogg", ROUND_60),
            Map.entry("sfx/intercept-carrier-r60-a.ogg", ROUND_60),
            Map.entry("sfx/belt-station-r60-b.ogg", ROUND_60_B),
            Map.entry("music/rook-return.ogg", ROUND_60_MUSIC),
            Map.entry("sfx/station-klaxon-r62-a.ogg", ROUND_62),
            Map.entry("sfx/station-relays-r62-a.ogg", ROUND_62),
            Map.entry("sfx/nest-hatch-r62-b.ogg", ROUND_62),
            Map.entry("sfx/nest-lock-r62-b.ogg", ROUND_62),
            Map.entry("sfx/nest-wake-r62-ab.ogg", ROUND_62),
            Map.entry("sfx/clamp-gun-shot-r62-b.ogg", ROUND_62),
            Map.entry("sfx/clamp-release-r62-b.ogg", ROUND_62),
            Map.entry("sfx/ember-launch-r62-a.ogg", ROUND_62),
            Map.entry("sfx/bulwark-depart-r62-a.ogg", ROUND_62),
            Map.entry("sfx/talon-whine-r62-a.ogg", ROUND_62),
            Map.entry("sfx/talon-shot-r62-b.ogg", ROUND_62),
            Map.entry("sfx/leader-squelch-r62-b.ogg", ROUND_62),
            Map.entry("sfx/mite-tell-r62-a.ogg", ROUND_62),
            Map.entry("sfx/mite-leap-r62-a.ogg", ROUND_62),
            Map.entry("sfx/ascendancy-turbine-r60-a.ogg", ROUND_60_C),
            Map.entry("sfx/mine-blink-r60-a.ogg", ROUND_60_C),
            Map.entry("sfx/mine-burst-r60-a.ogg", ROUND_60_C),
            Map.entry("sfx/belt-open-r60-a.ogg", ROUND_60_C),
            Map.entry("sfx/mine-arm-r63-b.ogg", ROUND_63),
            Map.entry("sfx/mine-drop-r63-a.ogg", ROUND_63),
            Map.entry("sfx/mine-chain-r63-a.ogg", ROUND_63),
            Map.entry("sfx/serpent-shot-r63-a.ogg", ROUND_63),
            Map.entry("sfx/serpent-rumble-r63-a.ogg", ROUND_63),
            Map.entry("sfx/serpent-split-r63-b.ogg", ROUND_63),
            Map.entry("sfx/gunship-ring-r63-b.ogg", ROUND_63),
            Map.entry("sfx/gunship-doors-r63-b.ogg", ROUND_63),
            Map.entry("sfx/dart-launch-r63-a.ogg", ROUND_63),
            Map.entry("sfx/buoy-tick-r63-a.ogg", ROUND_63),
            Map.entry("sfx/link-hum-r64-a.ogg", ROUND_64),
            Map.entry("sfx/link-flicker-r64-a.ogg", ROUND_64),
            Map.entry("sfx/link-snap-r64-a.ogg", ROUND_64),
            Map.entry("sfx/saw-whine-r64-a.ogg", ROUND_64),
            Map.entry("sfx/saw-spinup-r64-a.ogg", ROUND_64),
            Map.entry("sfx/saw-ricochet-r64-a.ogg", ROUND_64),
            Map.entry("sfx/tug-hit-r64-a.ogg", ROUND_64),
            Map.entry("sfx/tug-alarm-r64-a.ogg", ROUND_64),
            Map.entry("sfx/tug-lost-r64-a.ogg", ROUND_64),
            Map.entry("sfx/tug-engine-r64-a.ogg", ROUND_64),
            Map.entry("music/reveal-sting.ogg", ROUND_58_MUSIC),
            Map.entry("music/thera-deep-dark.ogg", ROUND_55_MUSIC),
            Map.entry("music/thera-deep-warm.ogg", ROUND_55_MUSIC),
            Map.entry("music/grey-signal.ogg", ROUND_41_MUSIC),
            Map.entry("music/grey-signal-base.ogg", ROUND_41_MUSIC),
            Map.entry("music/grey-signal-motif.ogg", ROUND_41_MUSIC),
            Map.entry(
                    "music/debrief-theme.ogg",
                    "track 29 \"Stand Down\", chosen in round 40 (M.a = b): the concept file, rendered by"
                            + " tools/concept/audio/music_r40.py")));

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
