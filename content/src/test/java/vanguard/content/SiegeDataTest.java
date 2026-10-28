package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Intel;

/**
 * M7 part D's data (step D2; design/tech/architecture, Data file schemas): Level 17's {@code allies} block (the five
 * Ares Landing domes and the hook that goes for them), the {@code untouched} secondary, a pickup's {@code
 * dropped_by.enemy} and the presentation key {@code flak}; the Ares Landing dome's {@code hits}, {@code bombs},
 * {@code rolls} and {@code sprite}; the Choir Herald's {@code aura}; the Shellback's parts and {@code roll}; the Dust
 * Kite's {@code flock}; the intel's dome-section marks; loaded strictly and checked by the static validator ({@link
 * SiegeChecks}). The simulation's mapping of the new keys is step D4's.
 */
class SiegeDataTest {
    private static final String KEY = "act-3-red-dust/level-17-dome-siege";
    private static final String PATH = "campaign/" + KEY + "/data.yaml";
    private static final String LEVEL_16 = "campaign/act-3-red-dust/level-16-valles-canyon-run/data.yaml";
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final LevelData LEVEL = CONTENT.level(KEY);
    private static final LevelData.GroundAllies ALLIES = LEVEL.allies().orElseThrow();
    private static final AlliesData.Ally DOME = CONTENT.allies().allies().get("ares-landing-dome");

    private static List<DataFile> files(String path, UnaryOperator<String> change) {
        return DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(f.path(), change.apply(f.text())) : f)
                .toList();
    }

    private static void assertProblem(String path, String what, String with, String message) {
        Exception e = assertThrows(
                Exception.class,
                () -> ContentLoader.load(files(path, text -> {
                    assertTrue(text.contains(what), path + " has " + what);
                    return text.replace(what, with);
                })));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    @Test
    void theFiveDomesAreTheLevelsGroundAllies() {
        assertEquals(
                List.of("Bradbury", "Lowell", "Schiaparelli", "Burroughs", "Eden"),
                ALLIES.units().stream().map(LevelData.GroundAlly::name).toList());
        assertEquals(
                List.of(38.0, 46.0, 104.0, 110.0, 126.0),
                ALLIES.units().stream().map(unit -> unit.at().t()).toList());
        assertEquals(
                List.of(130.0, 350.0, 110.0, 370.0, 240.0),
                ALLIES.units().stream().map(unit -> unit.at().x()).toList());
        LevelData.GroundAlly bradbury = ALLIES.units().getFirst();
        LevelData.GroundAlly eden = ALLIES.units().getLast();
        assertEquals(new Size(120, 120), bradbury.size(DOME));
        assertEquals(new Size(88, 88), bradbury.hitbox(DOME));
        assertEquals("ares-dome", bradbury.spriteName(DOME));
        assertEquals(new Size(144, 144), eden.size(DOME));
        assertEquals(new Size(112, 112), eden.hitbox(DOME), "the hit box keeps the 16 px margin");
        assertEquals("ares-dome-eden", eden.spriteName(DOME));
        assertEquals(LEVEL.groundAllies(), ALLIES.units());
    }

    @Test
    void theHookGoesForTheDomesPerUnitType() {
        assertEquals(
                Map.of("spore-bomber", LevelData.HookMode.ALWAYS, "shellback", LevelData.HookMode.ALTERNATE),
                ALLIES.modes(Difficulty.MEDIUM));
        assertEquals(LevelData.HookMode.NONE, ALLIES.modes(Difficulty.EASY).get("shellback"));
        assertTrue(ALLIES.hooks("spore-bomber", Difficulty.EASY));
        assertFalse(ALLIES.hooks("shellback", Difficulty.EASY), "easy: the Shellbacks lob only at the ship");
        assertTrue(ALLIES.hooks("shellback", Difficulty.HARD));
        assertFalse(ALLIES.hooks("needler", Difficulty.MEDIUM), "not listed: never hooked");
        assertEquals(220, ALLIES.bombRow(), "round 40 R.b: from 160");
        assertEquals(40, ALLIES.bombSlot());
        assertEquals(Optional.of(4), ALLIES.change(Difficulty.EASY).flatMap(LevelData.AlliesChange::hits));
        assertEquals(Optional.of(2), ALLIES.change(Difficulty.HARD).flatMap(LevelData.AlliesChange::hits));
        assertTrue(ALLIES.change(Difficulty.MEDIUM).isEmpty());
    }

    @Test
    void theDomeCountsHitsFromBombRunsLobsAndRolls() {
        assertTrue(DOME.countsHits());
        assertEquals(Optional.of(3), DOME.hits());
        assertEquals(3, DOME.hp(), "its hp is the count of hits");
        assertTrue(DOME.standing(), "follows: ground");
        assertEquals(1, DOME.damagedBy().hitsPerBomb());
        assertEquals(1, DOME.damagedBy().hitsPerRoll());
        assertTrue(DOME.damagedBy().hitByLobs());
        assertFalse(DOME.damagedBy().objectiveAimed());
        assertFalse(DOME.damagedBy().hitByBullets());
        assertEquals(Optional.of(17), DOME.firstLevel());
        // the allies with HP are unchanged
        AlliesData.Ally relay = CONTENT.allies().allies().get("nansen-relay");
        assertFalse(relay.countsHits());
        assertEquals(260, relay.hp());
        assertEquals("nansen-relay", relay.spriteName("nansen-relay"));
        assertEquals(0, relay.damagedBy().hitsPerBomb());
    }

    @Test
    void theSecondaryKeepsTheDomesStanding() {
        LevelData.Secondary secondary = LEVEL.objectives().secondary().orElseThrow();
        assertTrue(secondary.keepsAlliesStanding(), "round 41: \"No dome lost\" (was untouched, \"No dome hit\")");
        assertFalse(secondary.keepsAlliesUntouched());
        assertEquals(Optional.of("No dome lost"), secondary.name());
        assertEquals(Optional.of(2.5), LEVEL.allies().orElseThrow().bombRunIn(), "round 41: the bombers' run-in");
        assertEquals(Optional.of("DOMES"), secondary.label());
        assertEquals(100, secondary.credits());
        assertEquals("reach-end", LEVEL.objectives().primary());
        assertTrue(LEVEL.objectives().defend().isEmpty(), "the domes never fail the mission");
    }

    @Test
    void theLevelsOtherNewKeysLoad() {
        LevelData.PlacedPickup cell = LEVEL.pickups().getLast();
        assertEquals(Pickup.SHIELD_CELL, cell.pickup());
        assertEquals(124, cell.carrier().waveT());
        assertEquals(Optional.of("choir-herald"), cell.carrier().enemy());
        assertTrue(LEVEL.pickups().getFirst().carrier().enemy().isEmpty());
        assertEquals(new LevelData.Flak(160, 1.5), LEVEL.flak().orElseThrow());
        assertEquals("mars-colony", LEVEL.music().ambience());
        assertEquals(0.40, LEVEL.bounties(), "step PE17: from 0.60 with the reworked waves (TypicalHaulTest)");
        assertEquals(172, LEVEL.seconds());
        LevelData.Drop drop = LEVEL.released().getFirst();
        assertEquals(100, drop.t());
        assertEquals(Pickup.ARMOUR_PATCH, drop.pickup());
    }

    @Test
    void theChoirHeraldSingsAnAura() {
        EnemyData herald = CONTENT.enemy("choir-herald");
        assertEquals(
                new EnemyData.Aura(90, 1.0, 40, 0.25, 2.0, 0.5, 1.0, 0.3, 0.8),
                herald.aura().orElseThrow());
        assertEquals(40, herald.aura().orElseThrow().ring(63), "a hooked bomber's 63 HP: a 40 ring");
        assertEquals(4, herald.aura().orElseThrow().ring(4), "a Needler's 4 HP: a 4 ring");
        EnemyData.Hook easy = herald.difficulty().flatMap(EnemyData.Hooks::easy).orElseThrow();
        EnemyData.Hook hard = herald.difficulty().flatMap(EnemyData.Hooks::hard).orElseThrow();
        assertEquals(Optional.of(new EnemyData.AuraChange(Optional.of(75.0), Optional.of(0.15))), easy.aura());
        assertEquals(Optional.of(new EnemyData.AuraChange(Optional.of(105.0), Optional.empty())), hard.aura());
        EnemyData.Attack song = herald.attack("song").orElseThrow();
        assertEquals("ring", song.pattern());
        assertEquals(Optional.of(10), song.count());
        assertEquals(Optional.of(4.0), song.interval());
        assertEquals(Optional.of(12), hard.attacks().orElseThrow().get("song").count());
        assertEquals(60, herald.hp());
        assertEquals(30, herald.bounty());
        assertTrue(CONTENT.enemy("needler").aura().isEmpty());
    }

    @Test
    void theShellbackHasAVentAndRolls() {
        EnemyData shellback = CONTENT.enemy("shellback");
        assertTrue(shellback.multiPart());
        assertTrue(shellback.boss().isEmpty(), "parts on a unit that is not a boss");
        List<EnemyData.PartData> parts = shellback.partList().orElseThrow();
        assertEquals(
                List.of("body", "vent"),
                parts.stream().map(EnemyData.PartData::name).toList());
        assertEquals(
                List.of("vital", "destroyable"),
                parts.stream().map(EnemyData.PartData::kind).toList());
        assertEquals(Optional.of(8), parts.getFirst().firstBonus(), "a body-first kill pays the whole 48");
        assertEquals(Optional.of("mortar"), parts.getLast().attack(), "the vent's loss silences the mortar");
        assertEquals(106, shellback.hp(), "round 40 K.a: body 90, vent 16");
        assertEquals(52, shellback.bounty());
        EnemyData.Roll roll = shellback.roll().orElseThrow();
        assertEquals(new EnemyData.Roll(0.4, 0.4, 2.5, 140, 0.4, 1.5, 1.5, new Size(72, 72), Optional.of(200.0)), roll);
        assertEquals(350, roll.length());
        EnemyData.Hook hard =
                shellback.difficulty().flatMap(EnemyData.Hooks::hard).orElseThrow();
        assertEquals(Optional.of(new EnemyData.RollChange(Optional.of(3.0))), hard.roll());
        assertEquals(Optional.of(10), hard.ring());
        assertEquals(
                Optional.of(3.2), hard.attacks().orElseThrow().get("mortar").interval());
        assertTrue(CONTENT.enemy("scuttler").roll().isEmpty());
    }

    @Test
    void theDustKiteFlocksInLevel17sSwarm() {
        EnemyData kite = CONTENT.enemy("dust-kite");
        EnemyData.Flock flock = kite.movement().flock().orElseThrow();
        assertEquals(0.5, flock.alignment());
        assertEquals(0.3, flock.cohesion());
        assertEquals(0.6, flock.leader());
        assertEquals(130, flock.speed());
        LevelData.Wave swarm = LEVEL.waves().stream()
                .filter(wave -> wave.t() == 77)
                .findFirst()
                .orElseThrow();
        assertEquals("swarm", swarm.groupList().getFirst().formation());
        assertEquals("dust-kite", swarm.groupList().getFirst().enemy());
        assertEquals(8, swarm.groupList().getFirst().count());
        // its streams, V-wings and snakes still fly (Levels 15-17)
        assertTrue(LEVEL.waves().stream()
                .anyMatch(wave -> wave.groupList().getFirst().enemy().equals("dust-kite")
                        && wave.groupList().getFirst().formation().equals("stream")));
    }

    @Test
    void theIntelMarksTheTwoDomeSections() {
        Intel intel = Intel.of(CONTENT, KEY, 3);
        assertEquals(
                List.of(List.of(30.0, 72.0), List.of(98.0, 140.0)),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.PHASE)
                        .map(mark -> List.of(mark.from(), mark.to()))
                        .toList());
    }

    @Test
    void theValidatorChecksTheDomes() {
        assertProblem(PATH, "name: Lowell, at: [46, 350]", "name: Lowell, at: [39, 150]", "overlaps Bradbury's");
        assertProblem(
                PATH, "name: Bradbury, at: [38, 130]", "name: Bradbury, at: [38, 40]", "lies inside the play field");
        assertProblem(PATH, "bomb_row: 220", "bomb_row: 600", "the bombers' row lies on the play field");
        assertProblem(PATH, "{spore-bomber: always,", "{spore-bombr: always,", "unknown enemy 'spore-bombr'");
        assertProblem(
                PATH, "unit: first, enemy: choir-herald", "unit: first, enemy: mantis", "'mantis' flies in no wave");
        assertProblem(
                LEVEL_16,
                "secondary: {links: true,",
                "secondary: {untouched: true,",
                "an untouched secondary judges the level's ground allies");
        assertProblem(
                LEVEL_16,
                "secondary: {links: true,",
                "secondary: {standing: true,",
                "a standing secondary judges the level's ground allies");
        assertProblem(
                PATH,
                "secondary: {standing: true,",
                "secondary: {standing: true, untouched: true,",
                "give kill_ratio, groups, escapes, kill_all, parts, afloat, collect, integrity, links, spared,"
                        + " untouched or standing");
        assertProblem(
                PATH,
                "{piece: helix-compound, t: 81.5, x: 380}",
                "{piece: helix-compound, t: 92, x: 220}",
                "within 100.0 px of the Helix compound");
        // Round 40 R.c: the walks pass between the domes; Lowell's old lane ran across its glass.
        assertProblem(
                PATH,
                "paths: [[[260, -150], [260, -70], [220, 0], [220, 640]]]",
                "paths: [[[350, -180], [350, 640]]]",
                "walks over Lowell's hit box");
        // the convoy's second unit enters 3 s later, its path 210 px higher on the ground
        assertProblem(
                PATH,
                "paths: [[[280, -120], [280, -40], [240, 30], [240, 640]]]",
                "paths: [[[300, -120], [300, 640]]]",
                "entering at t=111.0 walks over Burroughs's hit box");
        // Round 40 K.a: a first-ring line needs an aura enemy.
        assertProblem(
                LEVEL_16,
                "(hired, fitted and not ejected)\n",
                "(hired, fitted and not ejected)\n  - {event: first-ring, speaker: Varga, line: \"Rings.\"}\n",
                "first-ring in a level without an aura enemy");
    }

    @Test
    void theValidatorChecksTheNewUnitAndAllyKeys() {
        assertProblem(
                "enemies/ground/shellback/data.yaml", "ball: [72, 72]", "ball: [120, 120]", "the ball lies inside");
        assertProblem(
                "enemies/ground/shellback/data.yaml",
                "roll: {seconds: 3.0}}",
                "roll: {seconds: 3.0}, aura: {radius: 80}}",
                "an aura hook without an aura attack");
        assertProblem("allies/data.yaml", "  hits: 3 ", "  hp: 3 ", "an ally hurt by bomb runs or rolls counts hits");
        assertProblem(
                "enemies/air/dust-kite/data.yaml",
                "  - {name: stream}\n  - {name: V-wing, size: [4, 7]}\n  - {name: snake, size: [4, 6]}\n",
                "",
                "'dust-kite' flies only a swarm");
    }
}
