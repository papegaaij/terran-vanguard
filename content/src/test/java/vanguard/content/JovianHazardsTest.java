package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * M10 part A, step A2b (m10a-gaps.md §1 #6, #7, #9, #11–#13, §4): the keys for A1b's features, loaded from data, checked
 * and flown: the told lightning strikes in a level's {@code weather} and the dry {@code eruptions}, their numbers the
 * Jovian world's (design/world/jovian/data.yaml); several held gates ({@code arena: true, holds: <group>}); the level's
 * {@code turn}; the foreground cloud banks' caps ({@code backdrop.banks}); the Plasma Arc's chain, the Reflex Shield's
 * reflect and the IFF spoofer from their rows. Each fixture is a built level with edits (a storm stretch on Level 30, a
 * vent on Level 29, a second gate on Level 29, a turn on Level 30).
 */
class JovianHazardsTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";
    private static final String LEVEL_30 = "campaign/act-5-the-belt/level-30-minefield/data.yaml";
    private static final String L30_LAUNCH = "launch_seconds: 5\n";
    private static final String L30_FACTORS =
            "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far, ground unused";

    /** The design tree with {@code edits} (file, text, replacement; the first occurrence). */
    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i < edits.length; i += 3) {
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

    private static Content content(String... edits) {
        return ContentLoader.load(files(edits));
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    private static String key(Content content, int level) {
        return content.levelKey(level).orElseThrow();
    }

    private static Sortie fly(Content content, int level, Difficulty difficulty) {
        String key = key(content, level);
        LevelScript script = SimSpecs.level(content, key, difficulty);
        Loadout loadout = ActThreeArsenalFlightTest.loadout(
                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1));
        return new Sortie(
                1,
                loadout,
                script,
                SimSpecs.rules(content, key, difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    /** Steps {@code sortie} until its level clock reaches {@code seconds}; returns the {@code type} events raised. */
    private static int until(Sortie sortie, double seconds, SimEvents.Type type) {
        int n = 0;
        for (int i = 0; i < SimStep.ticks(seconds * 3) && sortie.levelSeconds() < seconds; i++) {
            sortie.step(Command.NONE);
            n += sortie.events().count(type);
        }
        return n;
    }

    /** The first times from {@code from} s, 0.5 s apart, that lie at least 1.5 s from each other and 1 s from every wave. */
    private static List<Double> clearTimes(LevelData level, double from, int count) {
        List<Double> times = new ArrayList<>();
        for (double t = from; times.size() < count; t += 0.5) {
            double at = t;
            boolean clear = level.waves().stream().allMatch(wave -> Math.abs(wave.t() - at) >= 1.0)
                    && (times.isEmpty() || at - times.getLast() >= 1.5);
            if (clear) {
                times.add(at);
            }
        }
        return times;
    }

    // --- the world's numbers ----------------------------------------------------------------------

    @Test
    void theJovianWorldHoldsTheHazardsNumbersOnce() {
        JovianData.Strike strike = CONTENT.jovian().strike();
        assertEquals(1.0, strike.tell());
        assertEquals(0.25, strike.live());
        assertEquals(40, strike.width());
        assertEquals(10, strike.ship());
        assertEquals(60, strike.enemy());
        assertEquals(1.0, strike.entry());
        assertEquals(1.5, strike.spacing());
        assertEquals(60, strike.easyClearance());
        JovianData.Eruption eruption = CONTENT.jovian().eruption();
        assertEquals(1.6, eruption.tellOn(Difficulty.EASY));
        assertEquals(1.2, eruption.tellOn(Difficulty.MEDIUM));
        assertEquals(1.0, eruption.tellOn(Difficulty.HARD));
        assertEquals(new Span(3, 3), eruption.rocksOn(Difficulty.EASY));
        assertEquals(new Span(3, 5), eruption.rocksOn(Difficulty.MEDIUM));
        assertEquals(new Span(5, 5), eruption.rocksOn(Difficulty.HARD));
        assertEquals(120, eruption.speedOn(Difficulty.MEDIUM));
        assertEquals(150, eruption.speedOn(Difficulty.HARD));
        assertEquals(new Span(5, 7), eruption.period());
        assertEquals(6, eruption.hp());
        assertEquals(10, eruption.damage());
        JovianData.Banks banks = CONTENT.jovian().banks();
        assertEquals(1 / 3.0, banks.coverOn(Difficulty.MEDIUM), 1e-3);
        assertEquals(0.25, banks.coverOn(Difficulty.EASY));
        assertEquals(140, banks.band());
        assertEquals(2, banks.bandSeconds());
        // No level before Act 6 carries an Act 6 hazard: every one flies as before (M10 part B: Level 36's strikes).
        for (String key : CONTENT.levels().keySet()) {
            if (Content.levelNumber(key) >= 36) {
                continue;
            }
            for (Difficulty difficulty : Difficulty.values()) {
                HazardSpec hazards = SimSpecs.level(CONTENT, key, difficulty).hazards();
                assertTrue(hazards.eruptions().isEmpty(), key);
                assertTrue(
                        hazards.weather().flatMap(HazardSpec.Weather::strikes).isEmpty(), key);
            }
        }
    }

    // --- lightning strikes --------------------------------------------------------------------

    @Test
    void aStormStretchTellsItsStrikesWithTheWorldsNumbers() {
        List<Double> t = clearTimes(CONTENT.level(key(CONTENT, 30)), 12, 3);
        String strikes = "weather: {strikes: [{t: " + t.get(0) + ", x: 240}, {t: " + t.get(1)
                + ", x: 120, skip: [easy]}, {t: " + t.get(2) + ", x: 0, track: true, skip: [easy, medium]}]}\n";
        Content storm = content(LEVEL_30, L30_LAUNCH, L30_LAUNCH + strikes);
        String key = key(storm, 30);
        HazardSpec.Strikes medium = SimSpecs.level(storm, key, Difficulty.MEDIUM)
                .hazards()
                .weather()
                .flatMap(HazardSpec.Weather::strikes)
                .orElseThrow();
        assertEquals(
                List.of(new HazardSpec.Strike(t.get(0), 240, false), new HazardSpec.Strike(t.get(1), 120, false)),
                medium.at());
        assertEquals(1.0, medium.tellSeconds());
        assertEquals(0.25, medium.liveSeconds());
        assertEquals(40, medium.width());
        assertEquals(10, medium.shipDamage());
        assertEquals(60, medium.enemyDamage());
        assertEquals(0, medium.clearance(), "only easy keeps the column off the ship");
        assertEquals(1.0, medium.entrySeconds());
        HazardSpec.Strikes easy = SimSpecs.level(storm, key, Difficulty.EASY)
                .hazards()
                .weather()
                .flatMap(HazardSpec.Weather::strikes)
                .orElseThrow();
        assertEquals(1, easy.at().size(), "easy flies fewer");
        assertEquals(60, easy.clearance());
        HazardSpec.Strikes hard = SimSpecs.level(storm, key, Difficulty.HARD)
                .hazards()
                .weather()
                .flatMap(HazardSpec.Weather::strikes)
                .orElseThrow();
        assertEquals(3, hard.at().size());
        assertTrue(hard.at().getLast().track(), "hard's tracking column");
        // Calm weather round them (no gusts, drifts or flashes): Level 30 had none.
        HazardSpec.Weather weather = SimSpecs.level(storm, key, Difficulty.MEDIUM)
                .hazards()
                .weather()
                .orElseThrow();
        assertTrue(weather.gusts().isEmpty()
                && weather.drifts().isEmpty()
                && weather.lightning().isEmpty());
        // Flown: the first column is told, then strikes.
        Sortie sortie = fly(storm, 30, Difficulty.MEDIUM);
        assertTrue(sortie.lightning().isPresent());
        int told = until(sortie, t.get(0) + 0.5, SimEvents.Type.STRIKE_TOLD);
        assertEquals(1, told);
        assertEquals(1, until(sortie, t.get(0) + 1.5, SimEvents.Type.STRIKE));
    }

    @Test
    void theStrikesAuthoringIsChecked() {
        double wave = CONTENT.level(key(CONTENT, 30)).waves().getFirst().t();
        double clear = clearTimes(CONTENT.level(key(CONTENT, 30)), 12, 1).getFirst();
        // Two tells within 1.5 s; a tell on a wave's entry; a tracking column on medium.
        assertProblem(
                problems(
                        LEVEL_30,
                        L30_LAUNCH,
                        L30_LAUNCH + "weather: {strikes: [{t: " + clear + ", x: 240}, {t: " + (clear + 1)
                                + ", x: 100, skip: [easy, medium, hard]}, {t: " + (clear + 1.2) + ", x: 300}]}\n"),
                "never two strikes within 1.5 s");
        assertProblem(
                problems(LEVEL_30, L30_LAUNCH, L30_LAUNCH + "weather: {strikes: [{t: " + wave + ", x: 240}]}\n"),
                "no tell within 1.0 s of a wave's entry");
        assertProblem(
                problems(
                        LEVEL_30,
                        L30_LAUNCH,
                        L30_LAUNCH + "weather: {strikes: [{t: " + clear + ", x: 240, track: true, skip: [easy]}]}\n"),
                "a tracking column is hard's");
    }

    // --- eruptions ----------------------------------------------------------------------------

    /** Level 29's Block C: a vent entering at t=53 erupts first in the quiet window t=56–62 (a 3.5 s period). */
    private static final String VENT = "eruptions: [{t: 53, x: 240, period: [3.5, 3.5]}]\n";

    @Test
    void aVentRowLoadsAndErupts() {
        Content vents = content(LEVEL_29, "launch_seconds:", VENT + "launch_seconds:");
        String key = key(vents, 29);
        HazardSpec.EruptionSpec medium = SimSpecs.level(vents, key, Difficulty.MEDIUM)
                .hazards()
                .eruptions()
                .orElseThrow();
        assertEquals(List.of(new HazardSpec.Vent(53, 240, 3.5, 3.5, 3, 5)), medium.vents());
        assertEquals(1.2, medium.tellSeconds());
        assertEquals(0.5, medium.riseSeconds());
        assertEquals(2.5, medium.flySeconds());
        assertEquals(0.4, medium.fallSeconds());
        assertEquals(Math.toRadians(100), medium.fanRadians(), 1e-12);
        assertEquals(120, medium.speed());
        assertEquals(60, medium.endSpeed());
        assertEquals(new Hitbox(24, 24), medium.rock());
        assertEquals(6, medium.hp());
        assertEquals(10, medium.damage());
        HazardSpec.EruptionSpec hard = SimSpecs.level(vents, key, Difficulty.HARD)
                .hazards()
                .eruptions()
                .orElseThrow();
        assertEquals(1.0, hard.tellSeconds());
        assertEquals(150, hard.speed());
        assertEquals(5, hard.vents().getFirst().minRocks());
        HazardSpec.EruptionSpec easy = SimSpecs.level(vents, key, Difficulty.EASY)
                .hazards()
                .eruptions()
                .orElseThrow();
        assertEquals(1.6, easy.tellSeconds());
        assertEquals(3, easy.vents().getFirst().maxRocks());
        // Without its own period a vent erupts by the world's 5–7 s.
        Content plain = content(LEVEL_29, "launch_seconds:", "eruptions: [{t: 53, x: 240}]\nlaunch_seconds:");
        assertEquals(
                new HazardSpec.Vent(53, 240, 5, 7, 3, 5),
                SimSpecs.level(plain, key, Difficulty.MEDIUM)
                        .hazards()
                        .eruptions()
                        .orElseThrow()
                        .vents()
                        .getFirst());
        // Flown: the vent tells, then throws its rocks.
        Sortie sortie = fly(vents, 29, Difficulty.MEDIUM);
        assertTrue(sortie.eruptions().isPresent());
        assertEquals(1, until(sortie, 57, SimEvents.Type.ERUPTION_TELL));
        assertEquals(1, until(sortie, 58, SimEvents.Type.VENT_ERUPTED));
    }

    @Test
    void theFirstEruptionPlaysAlone() {
        // Entering at t=50 the vent's first tell falls at 55–57 (+1.2–1.6 s): Block C's quiet window starts at 56.
        assertProblem(
                problems(LEVEL_29, "launch_seconds:", "eruptions: [{t: 50, x: 240}]\nlaunch_seconds:"),
                "the level's first eruption plays alone");
    }

    // --- held gates ---------------------------------------------------------------------------

    /** Level 29 with a held gate after Block C (its holder a SAM Nest at t=74) before the rescue's, and no swap. */
    private static final String[] TWO_GATES = {
        LEVEL_29,
        "  - name: The Red Canyon\n",
        "  - name: Gate A\n    end: 78\n    speed: 0\n    arena: true\n    holds: gate-a\n    atmosphere: clear\n"
                + "    tiles: [belt-space, dock-canyon]\n  - name: The Red Canyon\n",
        LEVEL_29,
        "  - target: Clamp guns\n",
        "  - target: Gate A nest\n    section: 3\n    enemy: sam-nest\n    count: 1\n    group: gate-a\n"
                + "    at: [[74, 240]]\n  - target: Clamp guns\n",
        // The sections after the new gate move up by one.
        LEVEL_29,
        "    section: 5\n    enemy: clamp-gun",
        "    section: 6\n    enemy: clamp-gun",
        LEVEL_29,
        "    section: 7\n",
        "    section: 8\n",
        LEVEL_29,
        "    section: 7\n",
        "    section: 8\n",
        LEVEL_29,
        "    section: 7\n",
        "    section: 8\n",
        LEVEL_29,
        "    section: 4\n",
        "    section: 5\n",
        LEVEL_29,
        "    section: 4\n",
        "    section: 5\n",
        LEVEL_29,
        "    section: 4\n",
        "    section: 5\n",
        LEVEL_29,
        "    section: 4\n",
        "    section: 5\n",
        LEVEL_29,
        "escort_swap: {at: [240, 250], join: 1}",
        "",
        LEVEL_29,
        "{event: holders-down, swap: bulwark, ",
        "{event: holders-down, ",
        LEVEL_29,
        "{event: holders-down, swap: leaves, ",
        "{event: holders-down, "
    };

    @Test
    void aTwoGateLevelHoldsTheClockAtEachGate() {
        Content gates = content(TWO_GATES);
        LevelScript script = SimSpecs.level(gates, key(gates, 29), Difficulty.MEDIUM);
        assertEquals(
                2,
                script.sections().stream().filter(vanguard.sim.Section::arena).count());
        assertTrue(script.escortSwap().isEmpty());
        Sortie sortie = fly(gates, 29, Difficulty.MEDIUM);
        assertEquals(2, sortie.gates());
        assertEquals(0, sortie.gate());
        until(sortie, 77.99, SimEvents.Type.GATE_OPENED);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(sortie.halting(), "the first gate halts while its nest stands");
        assertEquals(0, sortie.gate());
        assertEquals(1, sortie.holders(), "gate 0's own holder");
        assertTrue(sortie.levelSeconds() < 78, "the clock stands at the first gate");
    }

    @Test
    void theGatesAuthoringIsChecked() {
        // The swap left in: a swap releases at the one held arena.
        String[] withSwap = java.util.Arrays.copyOf(TWO_GATES, 30);
        assertProblem(problems(withSwap), "an escort swap releases at the one held arena");
        // The rescue's clamp guns entering before the first gate's end would hold that gate.
        String[] early = TWO_GATES.clone();
        early[5] = early[5].replace(
                "  - target: Clamp guns\n",
                "  - target: Early clamps\n    section: 3\n"
                        + "    enemy: clamp-gun\n    count: 1\n    group: clamps\n    at: [[70, 100]]\n  - target: Clamp guns\n");
        assertProblem(problems(early), "a gate's holder enters after the gate before it ends");
    }

    // --- the turn -----------------------------------------------------------------------------

    @Test
    void theTurnMakesTheHuntsLive() {
        Content war = content(
                LEVEL_30,
                "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130}",
                "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130, tag: prey-wing}",
                LEVEL_30,
                "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right}",
                "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right, hunts: prey-wing}",
                LEVEL_30,
                L30_LAUNCH,
                L30_LAUNCH + "turn: 24\n");
        LevelScript script = SimSpecs.level(war, key(war, 30), Difficulty.MEDIUM);
        assertEquals(Optional.of(24.0), script.turn());
        assertEquals(
                Optional.empty(),
                SimSpecs.level(CONTENT, key(CONTENT, 30), Difficulty.MEDIUM).turn());
        Sortie sortie = fly(war, 30, Difficulty.MEDIUM);
        assertEquals(0, until(sortie, 23.9, SimEvents.Type.PREY_HIT), "side by side before the turn");
        // A turn without a hunting wave, and one after the level's end.
        assertProblem(problems(LEVEL_30, L30_LAUNCH, L30_LAUNCH + "turn: 24\n"), "the level has no hunts: <tag> wave");
        assertProblem(problems(LEVEL_30, L30_LAUNCH, L30_LAUNCH + "turn: 900\n"), "turn");
    }

    // --- the foreground banks -----------------------------------------------------------------

    @Test
    void theForegroundBanksAreCheckedAgainstTheCoverCapAndTheShipsBand() {
        // A bank in the lane's first section (120 px/s, high-air × 2.2): a tenth of the field, a second over the band.
        Content banks = content(LEVEL_30, L30_FACTORS, L30_FACTORS + "\n  banks: [{t: 10, x: 240, w: 200, h: 120}]");
        LevelData level = banks.level(key(banks, 30));
        assertEquals(1, level.backdrop().banksOn(Difficulty.EASY).size());
        double[] box = JovianHazardChecks.bankAt(
                level, level.backdrop().banks().orElseThrow().getFirst(), 10);
        assertEquals(PlayField.HEIGHT / 2.0, (box[1] + box[3]) / 2, 1e-9, "its centre passes mid-screen at its time");
        double[] later = JovianHazardChecks.bankAt(
                level, level.backdrop().banks().orElseThrow().getFirst(), 11);
        assertEquals(2.2 * 120, box[1] - later[1], 1e-6, "drifting with high-air's factor");
        // A wide bank: over a third of the field.
        assertProblem(
                problems(LEVEL_30, L30_FACTORS, L30_FACTORS + "\n  banks: [{t: 10, x: 240, w: 480, h: 300}]"),
                "of the play field under banks at a time (medium");
        // 0.3 of the field: over easy's quarter only, fine when left out on easy.
        assertProblem(
                problems(LEVEL_30, L30_FACTORS, L30_FACTORS + "\n  banks: [{t: 10, x: 240, w: 480, h: 162}]"), "(easy");
        content(LEVEL_30, L30_FACTORS, L30_FACTORS + "\n  banks: [{t: 10, x: 240, w: 480, h: 162, skip: [easy]}]");
        // A tall bank in the minefield (80 px/s): 2.5 s over the ship's band.
        assertProblem(
                problems(LEVEL_30, L30_FACTORS, L30_FACTORS + "\n  banks: [{t: 50, x: 240, w: 100, h: 300}]"),
                "never a bank over the ship's band");
        // The union, not the sum: two overlapping banks count their overlap once.
        assertEquals(
                200 * 100 + 100 * 100,
                JovianHazardChecks.coveredArea(List.of(new double[] {0, 0, 200, 100}, new double[] {100, 0, 300, 100})),
                1e-9);
    }

    // --- the equipment ------------------------------------------------------------------------

    @Test
    void thePlasmaArcChainsFromTheNose() {
        double[] reach = {200, 210, 220, 230, 240, 250};
        double[] jump = {90, 95, 100, 110, 120, 130};
        int[] chain = {3, 4, 4, 5, 6, 6};
        for (int level = 1; level <= 6; level++) {
            WeaponSpec arc = WeaponRules.weapon(CONTENT, Armament.Slot.FRONT, "plasma-arc", level);
            assertEquals(WeaponSpec.Delivery.CHAIN, arc.delivery());
            assertEquals(reach[level - 1], arc.range(), 1e-9);
            assertEquals(Math.toRadians(45), arc.coneHalfAngle(), 1e-9, "a 90° cone");
            assertEquals(
                    new WeaponSpec.Chain(chain[level - 1], jump[level - 1], 0.75),
                    arc.chain().orElseThrow());
            assertFalse(arc.shieldBreaker());
            assertEquals(1, arc.muzzles().size());
        }
        // The forward single-target DPS counts the first strike (1.19 × 144 at L36 is part B's check).
        WeaponData data = CONTENT.weapon("plasma-arc");
        assertEquals(30, BalanceTest.singleTarget(data, data.levels().getFirst()), 1e-9);
        // Flown on the range: one discharge strikes three dummies in a row.
        vanguard.sim.EnemySpec dummy = ActThreeArsenalFlightTest.dummy(1000, 24, Optional.empty());
        Sortie sortie = ActThreeArsenalFlightTest.range(
                ActThreeArsenalFlightTest.loadout(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "plasma-arc", 1)),
                true,
                new GroundUnit(0, 240, dummy, -1),
                new GroundUnit(0, 205, dummy, -1),
                new GroundUnit(0, 275, dummy, -1));
        int most = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Command.FIRE.bit() | (i < SimStep.ticks(3) ? Command.UP.bit() : 0));
            most = Math.max(most, sortie.events().count(SimEvents.Type.CHAIN_STRIKE));
        }
        assertEquals(3, most, "the first strike and two jumps");
    }

    @Test
    void theReflexShieldAndTheSpooferFly() {
        Loadout reflex = SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1)),
                "Reflex Shield",
                CONTENT.armour().plating().get(1).name(),
                0,
                Difficulty.MEDIUM);
        assertEquals(
                Optional.of(new vanguard.sim.ShieldModel.Reflect(0.75, 0.5, 640, 2)),
                reflex.shield().reflect());
        assertEquals(40, reflex.shield().capacity());
        assertTrue(ActThreeArsenalFlightTest.loadout().shield().reflect().isEmpty(), "a Mk shield reflects nothing");
        assertTrue(SimSpecs.fliesUtility(SimSpecs.IFF_SPOOFER));
        assertEquals(0.5, SimSpecs.spoofDelay(CONTENT));
        assertEquals(0.5, reflex.withSpoofer(SimSpecs.spoofDelay(CONTENT)).spoofDelay());
    }
}
