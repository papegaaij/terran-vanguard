package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialLimits;
import vanguard.sim.SpecialSpec;
import vanguard.sim.SwarmSpec;
import vanguard.sim.VrellLevel;
import vanguard.sim.WeaponSpec;

/**
 * M11 part A, step A2b (m11a-gaps.md §1 #6–#8, #10–#12, content side): Act 7's level keys loaded, checked and flown on
 * fixtures where no Act 7 level has its data yet: a spore level (Level 30's data with the world's clouds, the quiet,
 * {@code beyond_gate} and {@code breather}) and a rush level (Level 07's data turned into two held gates, the Gorgon
 * Frigate's and the Brood Carrier's, with the refill and a checkpoint per fight); the Vrell Swarm Call's row, the
 * beyond-gate limit and the Choir Resonator's wave mapped for the simulation.
 */
class VrellLevelKeysTest {
    private static final String LEVEL_30 = "campaign/act-5-the-belt/level-30-minefield/data.yaml";
    private static final String LEVEL_07 = "campaign/act-1-first-contact/level-07-brood-carrier/data.yaml";

    /** Two clouds as the minefield opens (the first over the ship's start), the quiet, beyond the gate, the breather. */
    private static final String SPORE_KEYS = """
            spores:
              - {t: 40, x: 240, radius: 80}
              - {t: 41, x: 60, radius: 70}
            quiet: {from: 50, to: 74}
            beyond_gate: true
            breather: true
            """;

    /** The rush: Level 07's approach, a gate for the frigate, a moving stretch, a gate for the carrier, the aftermath. */
    private static final String RUSH_SECTIONS = """
            sections:
              - {name: Picket Line, end: 30, atmosphere: light, tiles: [stars, ice-streaks]}
              - {name: Escort Screen, end: 70, atmosphere: medium, tiles: [stars]}
              - {name: Echo One, end: 72, speed: 0, arena: true, atmosphere: clear, tiles: [stars]}
              - {name: Shadow of the Carrier, end: 100, ramp: 2, atmosphere: medium, tiles: [stars]}
              - {name: Echo Two, end: 102, speed: 0, arena: true, atmosphere: clear, tiles: [stars]}
              - {name: Drift, end: 235, speed: 20, ramp: 2, atmosphere: clear, tiles: [stars]}
              - {name: Aftermath, end: 270, speed: 40, atmosphere: light, tiles: [stars]}
            """;

    private static final String RUSH = """
            rush:
              refill: true
              checkpoint: every-difficulty
              par: 120
              bosses:
                - {enemy: gorgon-frigate, t: 71.5, x: 240, section: 3}
                - {enemy: brood-carrier, t: 101.5, x: 240, section: 5}
            """;

    private static final String RUSH_WAVES = """
            waves:
              - {t: 5.5, formation: stream, enemy: skitter, count: 4, from: front, edge: right, interval: 1.5}
              - {t: 8, formation: V-wing, enemy: needler, count: 5, from: front}
            """;

    /** The design tree with Level 30 the spore level and Level 07 the rush, then {@code edits} ([path, from, to]...). */
    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            if (file.path().equals(LEVEL_30)) {
                text = text + SPORE_KEYS;
            } else if (file.path().equals(LEVEL_07)) {
                text = rushLevel(text);
            }
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return files;
    }

    /** Level 07's data as the rush: its sections, waves, boss, tow, secrets, pickups, radio and objectives replaced. */
    private static String rushLevel(String text) {
        String out = block(text, "sections", RUSH_SECTIONS);
        out = block(out, "waves", RUSH_WAVES);
        out = block(out, "boss", "");
        out = block(out, "tows", "");
        out = block(out, "secrets", "secrets: []\n");
        out = block(out, "pickups", "pickups: []\n");
        out = block(out, "radio", "radio:\n  - {t: 1, speaker: Okafor, line: \"Aegis, the echoes.\"}\n");
        out = block(out, "objectives", "objectives:\n  primary: reach-end\n");
        return out + RUSH;
    }

    /** {@code text} with its top-level {@code key} block (to the next top-level key) replaced by {@code with}. */
    private static String block(String text, String key, String with) {
        Matcher start = Pattern.compile("(?m)^" + key + ":").matcher(text);
        assertTrue(start.find(), "a top-level " + key);
        Matcher next = Pattern.compile("(?m)^[a-z_]+:").matcher(text);
        int end = next.find(start.end()) ? next.start() : text.length();
        return text.substring(0, start.start()) + with + text.substring(end);
    }

    private static final Content FIXTURES = ContentLoader.load(files());

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    private static String key(int level) {
        return FIXTURES.levelKey(level).orElseThrow();
    }

    private static Sortie sortie(int level, Difficulty difficulty, Loadout loadout) {
        return new Sortie(
                7,
                loadout,
                SimSpecs.level(FIXTURES, key(level), difficulty),
                SimSpecs.rules(FIXTURES, key(level), difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    private static Loadout pulse() {
        return ActThreeArsenalFlightTest.loadout(
                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1));
    }

    // --- the spore clouds, the quiet, beyond the gate, the breather ------------------------------

    @Test
    void theSporeLevelsKeysReachTheSimulation() {
        LevelData data = FIXTURES.level(key(30));
        assertTrue(data.breathes() && data.beyondTheGate());
        VrellLevel vrell = SimSpecs.level(FIXTURES, key(30), Difficulty.MEDIUM).vrell();
        VrellLevel.Spores spores = vrell.spores().orElseThrow();
        assertEquals(0.7, spores.slow());
        assertEquals(0.3, spores.easeSeconds());
        assertEquals(1.0, spores.burnSeconds());
        assertEquals(2, spores.most());
        assertEquals(0.25, spores.veil());
        assertEquals(2.2, spores.scrollFactor(), "the level's backdrop high-air factor over the world's 1.7");
        assertEquals(List.of(new VrellLevel.Cloud(40, 240, 80), new VrellLevel.Cloud(41, 60, 70)), spores.clouds());
        assertEquals(Optional.of(new VrellLevel.Quiet(50, 74)), vrell.quiet());
        assertTrue(vrell.rush().isEmpty());
        assertEquals(
                VrellLevel.NONE,
                SimSpecs.level(LevelMetrics.CONTENT, key(30), Difficulty.MEDIUM).vrell());
    }

    @Test
    void theCloudsDriftOverTheShipAndSlowIt() {
        Sortie sortie = sortie(30, Difficulty.MEDIUM, pulse());
        double least = 1;
        int most = 0;
        for (int i = 0; i < SimStep.ticks(60) && sortie.levelSeconds() < 48; i++) {
            sortie.step(0);
            most = Math.max(most, sortie.spores().count());
            least = Math.min(least, sortie.ship().slow().cloudShare());
        }
        assertEquals(2, most, "both clouds on the screen");
        assertEquals(0.7, least, 1e-9, "the ship slowed inside the first cloud");
    }

    @Test
    void theQuietHoldsUntilThePlayersFirstHit() {
        Sortie sortie = sortie(30, Difficulty.MEDIUM, pulse());
        assertNotNull(sortie.quiet());
        int broken = 0;
        for (int i = 0; i < SimStep.ticks(120) && sortie.levelSeconds() < 74; i++) {
            boolean before = sortie.levelSeconds() < 50;
            sortie.step(before ? 0 : Command.FIRE.bit());
            broken += sortie.events().count(SimEvents.Type.QUIET_BROKEN);
            if (before) {
                assertFalse(sortie.quiet().broken(), "nothing breaks it before it begins");
            }
        }
        assertEquals(1, broken, "the player's first hit breaks it, once");
        assertTrue(sortie.quiet().broken());
    }

    @Test
    void beyondTheGateForbidsTheCoverSpecials() {
        LevelData data = FIXTURES.level(key(30));
        SpecialsData specials = FIXTURES.specials();
        assertEquals(
                Set.of("Airstrike", "Orbital Lance", "Sonar Pulse"), Set.copyOf(data.forbiddenSpecials(0, specials)));
        assertEquals(Optional.of("NO COVER"), data.forbiddenReason(0, specials, "Airstrike"));
        assertEquals(Optional.of("NO COVER"), data.forbiddenReason(0, specials, "Orbital Lance"));
        assertEquals(Optional.of("NO WATER"), data.forbiddenReason(0, specials, "Sonar Pulse"));
        assertEquals(Optional.empty(), data.forbiddenReason(0, specials, "EMP Burst"));
        SpecialLimits limits =
                SimSpecs.level(FIXTURES, key(30), Difficulty.MEDIUM).specialLimits();
        SpecialLimits.Switch first = limits.switches().getFirst();
        assertEquals(0, first.atSeconds());
        assertEquals(Optional.of(Set.of(SpecialSpec.Kind.AIRSTRIKE, SpecialSpec.Kind.LANCE)), first.forbidden());
        assertEquals(SpecialLimits.Reason.NO_COVER, first.reason());
        // The built Level 30 is not beyond the gate.
        assertEquals(
                List.of("Sonar Pulse"),
                LevelMetrics.CONTENT.level(key(30)).forbiddenSpecials(0, LevelMetrics.CONTENT.specials()));
    }

    @Test
    void theSporeLevelsRulesAreChecked() {
        assertProblem(
                problems(LEVEL_30, "{t: 41, x: 60, radius: 70}", "{t: 41, x: 60, radius: 30}"),
                "a spore cloud's radius is 60.0–100.0 px");
        assertProblem(
                problems(LEVEL_30, "{t: 41, x: 60, radius: 70}", "{t: 40.5, x: 220, radius: 70}"),
                "never two spore clouds overlapping");
        assertProblem(
                problems(LEVEL_30, "  - {t: 41, x: 60, radius: 70}\n", """
                          - {t: 41, x: 60, radius: 70}
                          - {t: 42, x: 420, radius: 60}
                        """),
                "at most 2 spore clouds on the screen at once");
        assertProblem(problems(LEVEL_30, "quiet: {from: 50, to: 74}", "quiet: {from: 50, to: 400}"), "the quiet ends");
        assertProblem(problems(LEVEL_30, "medium: vacuum", "medium: air"), "beyond the gate is Vrell space");
    }

    // --- the rush ---------------------------------------------------------------------------------

    @Test
    void theRushPlacesItsBossesInTheirGates() {
        LevelScript script = SimSpecs.level(FIXTURES, key(7), Difficulty.HARD);
        VrellLevel.Rush rush = script.vrell().rush().orElseThrow();
        assertEquals(new VrellLevel.Rush(true, true, 120), rush);
        assertEquals(
                List.of("gorgon-frigate", "brood-carrier"),
                script.setPieces().stream().map(piece -> piece.slug()).toList());
        Sortie sortie = sortie(7, Difficulty.HARD, pulse());
        assertTrue(sortie.bosses().rush());
        assertEquals(2, sortie.bosses().fights());
        assertTrue(sortie.bosses().everyDifficulty(), "the checkpoint on hard too");
        assertEquals(
                new VrellLevel.Rush(true, false, 120),
                SimSpecs.level(
                                ContentLoader.load(files(LEVEL_07, "every-difficulty", "difficulty")),
                                key(7),
                                Difficulty.HARD)
                        .vrell()
                        .rush()
                        .orElseThrow());
    }

    /**
     * The rush flown on hard: the frigate's fight records the checkpoint, its kill opens the way to the carrier's gate,
     * whose fight comes with the refill; a retry from there restarts the carrier's fight with the frigate dead.
     */
    @Test
    void theRushRefillsBetweenTheFightsAndARetryKeepsTheDeadDead() {
        Loadout loadout = ActThreeArsenalFlightTest.loadout(
                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 5),
                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5));
        Sortie sortie = sortie(7, Difficulty.HARD, loadout);
        int refills = 0;
        int fight = -1;
        for (int i = 0; i < SimStep.ticks(600) && fight < 1; i++) {
            sortie.step(Autopilot.commands(sortie));
            refills += sortie.events().count(SimEvents.Type.BOSS_REFILL);
            fight = sortie.bosses().fight();
            if (fight == 0) {
                assertTrue(sortie.bossCheckpoint(), "the first fight's checkpoint, on hard");
            }
        }
        assertEquals(1, fight, "the carrier's fight began");
        assertEquals(1, refills, "the refill before the second fight");
        assertTrue(sortie.setPiece(0).destroyed());
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_RETRY));
        assertEquals(1, sortie.bosses().fight(), "back to the carrier's fight");
        assertTrue(sortie.setPiece(0).destroyed(), "the frigate stays dead");
        assertFalse(sortie.setPiece(1).destroyed());
    }

    @Test
    void theRushsRulesAreChecked() {
        assertProblem(
                problems(LEVEL_07, "{enemy: brood-carrier, t: 101.5, x: 240, section: 5}", """
                        {enemy: brood-carrier, t: 71.8, x: 240, section: 3}"""), "one boss a gate");
        assertProblem(
                problems(LEVEL_07, "{enemy: brood-carrier, t: 101.5, x: 240, section: 5}", """
                        {enemy: brood-carrier, t: 90, x: 240, section: 4}"""),
                "a rush's boss arrives in its own held gate");
        assertProblem(
                problems(LEVEL_07, "{enemy: gorgon-frigate, t: 71.5", "{enemy: skitter, t: 71.5"),
                "a rush's boss fights with its bar");
        assertProblem(problems(LEVEL_07, "checkpoint: every-difficulty", "checkpoint: always"), "rush.checkpoint");
    }

    // --- the Swarm Call and the Choir Resonator ---------------------------------------------------

    @Test
    void theSwarmCallFliesItsRow() {
        SpecialSpec call = SimSpecs.special(FIXTURES, SimSpecs.SWARM_CALL, 0);
        assertEquals(SpecialSpec.Kind.SWARM, call.kind());
        assertEquals(new SwarmSpec(6, 24, 30, 300, 270, 90, 0.5, 8, 520, 10, 0.5, 0.3, 400, 2), call.swarm());
        SpecialsData.Special row = FIXTURES.specials().specials().stream()
                .filter(special -> special.name().equals(SimSpecs.SWARM_CALL))
                .findFirst()
                .orElseThrow();
        assertTrue(row.cooldownSpecial());
        assertEquals(Optional.of(10_000), row.price());
        assertEquals(Optional.of(75.0), row.cooldown());
        assertEquals(46, row.unlock());
    }

    @Test
    void theResonatorFliesItsWave() {
        WeaponSpec first = WeaponRules.weapon(FIXTURES, Armament.Slot.FRONT, "choir-resonator", 1);
        assertEquals(WeaponSpec.Delivery.WAVE, first.delivery());
        assertEquals(2.5, first.rate());
        assertEquals(30, first.damage());
        assertEquals(Optional.of(new WeaponSpec.Wave(32, 105, 260, 520, 2, 0.4)), first.wave());
        WeaponSpec overdrive = WeaponRules.weapon(FIXTURES, Armament.Slot.FRONT, "choir-resonator", 6);
        assertEquals(Optional.of(new WeaponSpec.Wave(32, 130, 300, 520, 3, 0.4)), overdrive.wave());
        assertTrue(SimSpecs.flies(FIXTURES, "choir-resonator"));
    }
}
