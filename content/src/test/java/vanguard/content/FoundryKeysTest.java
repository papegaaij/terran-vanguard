package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.WaveSpec;

/**
 * M7 part F (step F2): the loader's records, {@link FoundryChecks} and {@link FoundryRules} for Level 19 Foundry of
 * Tharsis's keys (design/campaign Level 19: the conveyor {@code belts}, the furnace {@code vents}, a nest's {@code arc:
 * up}, the arena's {@code hold_after_boss} and the crawl's ease, the secondary's {@code allowed}, the music's {@code
 * stems: motif}) and its two units' (design/enemies/ground/warden-tank: {@code turret}, {@code rear_plate};
 * design/enemies/bosses/revenant-walker: {@code movement.stride}, {@code boss.plate}, {@code boss.cone}, an attack's
 * {@code homing}, {@code parts} and {@code aim: facing}, {@code until.share}), as the simulation gets them.
 */
class FoundryKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_KEY = "act-3-red-dust/level-19-foundry-of-tharsis";
    private static final String LEVEL = "campaign/act-3-red-dust/level-19-foundry-of-tharsis/data.yaml";
    private static final String TANK = "enemies/ground/warden-tank/data.yaml";
    private static final String WALKER = "enemies/bosses/revenant-walker/data.yaml";

    private static LevelData level() {
        return CONTENT.level(LEVEL_KEY);
    }

    @Test
    void theWardenTanksTurretAndRearPlateReachTheSimulation() {
        EnemySpec medium = SimSpecs.enemy(CONTENT, "warden-tank", Difficulty.MEDIUM, java.util.Optional.empty());
        EnemySpec.Turret turret = medium.turret().orElseThrow();
        assertEquals(Math.toRadians(120), turret.turnRate(), 1e-12);
        assertEquals(0.3, turret.lockSeconds());
        assertEquals(Math.toRadians(10), turret.withinRadians(), 1e-12);
        EnemySpec.RearPlate plate = medium.rearPlate().orElseThrow();
        assertEquals(Math.toRadians(50), plate.halfArcRadians(), 1e-12);
        assertEquals(1.5, plate.multiplier());
        EnemySpec.UnitPart part = medium.part().orElseThrow();
        assertEquals("turret", part.name());
        assertEquals(20, part.hp());
        assertEquals(8, part.bounty());
        assertEquals(6, part.firstBonus(), "a hull-first kill pays the whole 26");
        assertTrue(part.silencesGun());
        assertEquals(64, medium.hp(), "the hull's HP: medium, its first level, no beam-era factor");
        assertEquals(20, medium.bounty());
        assertTrue(medium.machine());
        EnemyGun gun = medium.gun().orElseThrow();
        assertEquals(3, gun.burst());
        assertEquals(2.5, gun.intervalSeconds(), 1e-12);
        assertEquals(0.15, gun.burstGapSeconds(), 1e-12);

        EnemySpec hard = SimSpecs.enemy(CONTENT, "warden-tank", Difficulty.HARD, java.util.Optional.empty());
        assertEquals(Math.toRadians(160), hard.turret().orElseThrow().turnRate(), 1e-12);
        assertEquals(4, hard.gun().orElseThrow().burst());
        assertEquals(2.0, hard.gun().orElseThrow().intervalSeconds(), 1e-12);
        EnemySpec easy = SimSpecs.enemy(CONTENT, "warden-tank", Difficulty.EASY, java.util.Optional.empty());
        assertEquals(2, easy.gun().orElseThrow().burst());
        assertEquals(3.0, easy.gun().orElseThrow().intervalSeconds(), 1e-12);
    }

    @Test
    void theWalkerWalksItsPassesBehindItsPlateWithItsConeAndHomingPods() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, LEVEL_KEY, difficulty);
            SetPieceSpec piece = script.setPieces().stream()
                    .filter(spec -> spec.slug().equals("revenant-walker"))
                    .findFirst()
                    .orElseThrow();
            BossSpec boss = piece.boss().orElseThrow();
            assertTrue(boss.walks(), difficulty + ": it walks");
            assertTrue(boss.midBoss());
            assertEquals(132, boss.arriveSeconds(), 1e-9, "at the arena's start");
            assertEquals(2, boss.deathSeconds());
            BossSpec.Stride stride = boss.stride().orElseThrow();
            assertEquals(List.of(120.0, 240.0, 360.0), toList(stride.laneXs()));
            assertEquals(
                    List.of(PlayField.HEIGHT - 200.0, PlayField.HEIGHT - 330.0),
                    toList(stride.laneYs()),
                    "below the top edge in the data, up from the bottom edge in the simulation");
            assertEquals(
                    List.of(
                            WaveSpec.Edge.TOP,
                            WaveSpec.Edge.BOTTOM,
                            WaveSpec.Edge.LEFT,
                            WaveSpec.Edge.TOP,
                            WaveSpec.Edge.BOTTOM,
                            WaveSpec.Edge.RIGHT),
                    stride.edgeCycle());
            assertEquals(3, stride.offSeconds());
            assertEquals(1.5, stride.turnSeconds());
            assertEquals(List.of(45.0, 55.0, 60.0), toList(stride.phaseSpeeds()));
            assertTrue(stride.besideLaneFirstPhase());
            assertTrue(stride.turnInPlaceLastPhase());

            BossSpec.Plate plate = boss.plate().orElseThrow();
            assertEquals(0, plate.part(), "on the hull");
            assertEquals(110, plate.box().width());
            assertEquals(30, plate.box().height());
            assertEquals(45, plate.ahead());

            BossSpec.Cone cone = boss.cone().orElseThrow();
            // Round 43 (K.a of round 42: "the flame needs to be bigger"): 240 px ±32°, easy 210, hard 270.
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 210;
                        case MEDIUM -> 240;
                        case HARD -> 270;
                    },
                    cone.length());
            assertEquals(Math.toRadians(32), cone.halfAngleRadians(), 1e-12);
            assertEquals(40, cone.reachBehind());
            assertEquals(3, cone.cooldownSeconds());
            assertEquals(difficulty == Difficulty.EASY ? 0.8 : 0.6, cone.tellSeconds());
            assertEquals(1.5, cone.burnSeconds());
            assertEquals(2, cone.ventSeconds());
            assertEquals(Math.toRadians(difficulty == Difficulty.HARD ? 45 : 35), cone.swivelRadians(), 1e-12);
            assertEquals(Math.toRadians(40), cone.trackRadiansPerSecond(), 1e-12, "the burn tracks from phase 2");
            assertEquals(10, cone.damage(), "heavy");
            assertEquals(50, cone.originBehind());

            BossSpec.Arena arena = boss.arena().orElseThrow();
            assertEquals(BossSpec.Lanes.NONE, arena.lanes());
            BossSpec.Spot vents = arena.spots().getFirst();
            assertEquals("vents", vents.name());
            assertEquals(0, vents.part());
            assertEquals(45, vents.dy());
            assertEquals(2, vents.multiplier());
            assertTrue(vents.whileOpen());

            BossSpec.Attack strideSalvo = attack(boss, "salvo-stride");
            assertEquals(List.of(1, 2), strideSalvo.parts(), "both pods");
            assertTrue(strideSalvo.rotate(), "one pod at a time");
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 2;
                        case MEDIUM -> 3;
                        case HARD -> 4;
                    },
                    strideSalvo.gun().burst());
            EnemyGun.HomingSpec homing = strideSalvo.gun().homing().orElseThrow();
            assertEquals(
                    Math.toRadians(difficulty == Difficulty.HARD ? 120 : 90), homing.turnRadiansPerSecond(), 1e-12);
            assertEquals(4, homing.lifeSeconds());
            assertEquals(2, homing.hp());
            assertEquals(8, homing.mostAlive());
            assertEquals(0.4, homing.hatchSeconds());
            BossSpec.Attack salvo = attack(boss, "salvo");
            assertTrue(salvo.stagger(), "each pod on its own clock");
            assertTrue(salvo.gun().homing().isPresent());
            BossSpec.Attack chinStride = attack(boss, "chin-stride");
            assertEquals(List.of(0), chinStride.parts());
            assertEquals(List.of(0), chinStride.down(), "along its heading in phase 1");
            // Round 43: the chin's fans come in bursts; the flank guns fire square to its heading.
            assertEquals(BossSpec.Pattern.FAN, chinStride.pattern());
            assertEquals(3, chinStride.gun().fan());
            assertEquals(difficulty == Difficulty.EASY ? 1 : 2, chinStride.gun().burst());
            assertEquals(List.of(), attack(boss, "chin").down(), "aimed from phase 2");
            assertEquals(
                    difficulty == Difficulty.HARD ? 5 : 3,
                    attack(boss, "chin").gun().fan());
            assertTrue(attack(boss, "chin").gun().homing().isEmpty());
            BossSpec.Attack broadside = attack(boss, "broadside");
            assertEquals(List.of(0), broadside.down(), "along its heading: its fan of 2 over 180° leaves its flanks");
            assertEquals(2, broadside.gun().fan());
            assertEquals(Math.PI, broadside.gun().spreadRadians(), 1e-12);

            List<BossSpec.Phase> phases = boss.phases();
            assertEquals(3, phases.size());
            assertEquals(List.of(1, 2), phases.get(0).untilParts(), "both pods gone");
            assertEquals(0, phases.get(0).left());
            assertEquals(List.of(0), phases.get(0).arena().belowParts(), "or the hull ...");
            assertEquals(0.6, phases.get(0).arena().below(), "... below 60 %");
            assertEquals(0.3, phases.get(1).arena().below());
            assertEquals(List.of(0), phases.get(2).untilParts());
            assertTrue(phases.get(2).arena().drop().isPresent(), "the shield cell");
        }
    }

    @Test
    void theBeltsCarryTheGroundWhereTheBackdropDrawsThem() {
        LevelData level = level();
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM);
        assertEquals(6, script.ground().belts().size());
        GroundSpec.Belt first = script.ground().belts().getFirst();
        assertEquals(140, first.x());
        assertEquals(48, first.width());
        assertEquals(40, first.speed());
        assertEquals(level.scrollAt(26.77) + PlayField.HEIGHT / 2.0, first.from(), 1e-9);
        assertEquals(level.scrollAt(67.85) - PlayField.HEIGHT / 2.0, first.to(), 1e-9);
        // A tank of the t=30.5 convoy enters 40 px above the top edge on the belt at x 140.
        assertTrue(first.carries(140, PlayField.HEIGHT + 40, level.scrollAt(30.5)));
        assertFalse(first.carries(240, PlayField.HEIGHT + 40, level.scrollAt(30.5)), "off its column");
    }

    @Test
    void theVentsRippleByDifficultyAndEndBeforeTheArena() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<HazardSpec.VentRow> rows =
                    SimSpecs.level(CONTENT, LEVEL_KEY, difficulty).hazards().vents();
            assertEquals(7, rows.size());
            HazardSpec.VentRow first = rows.getFirst();
            assertEquals(64.5, first.t());
            assertEquals(4, first.grates());
            assertEquals(44, first.width());
            assertEquals(10, first.damage(), "heavy");
            assertEquals(1.0, first.burnSeconds());
            switch (difficulty) {
                case EASY -> {
                    assertEquals(1.6, first.tellSeconds());
                    assertEquals(5, first.periodSeconds());
                }
                case MEDIUM -> {
                    assertEquals(1.2, first.tellSeconds());
                    assertEquals(3.5, first.periodSeconds());
                    assertEquals(0.4, first.rippleSeconds());
                }
                case HARD -> {
                    assertEquals(0.9, first.tellSeconds());
                    assertEquals(3, first.periodSeconds());
                    assertEquals(0.3, first.rippleSeconds());
                }
            }
            assertTrue(rows.getLast().t() < 132, "cold from the arena on");
        }
    }

    @Test
    void theGantryNestsFaceUpTheLine() {
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM);
        List<GroundUnit> turrets = script.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals("spine-turret"))
                .toList();
        assertEquals(26, turrets.size());
        for (GroundUnit unit : turrets) {
            assertEquals(Math.PI, unit.enemy().gun().orElseThrow().arcFacingRadians(), 1e-12, "arc: up");
        }
        assertTrue(script.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals("polyp-mortar"))
                .allMatch(unit -> unit.enemy().gun().orElseThrow().arcFacingRadians() == 0));
        assertEquals(
                29,
                SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.HARD).groundUnits().stream()
                        .filter(unit -> unit.enemy().slug().equals("spine-turret"))
                        .count(),
                "hard's extra nest of 3");
    }

    @Test
    void theArenaHoldsAfterTheWalkerAndTheCrawlEasesUpFromTheHalt() {
        List<Section> sections =
                SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM).sections();
        Section arena = sections.get(4);
        assertTrue(arena.arena());
        assertEquals(1, arena.holdAfterBossSeconds());
        Section crawl = sections.get(5);
        assertEquals(20, crawl.speed());
        assertEquals(2, crawl.easeSeconds(), "eased up from the halt over its ramp");
        assertEquals(2, sections.get(6).easeSeconds());
        // Level 11's open water after the Kraken keeps the 1 s ramp (no ease of its own).
        assertEquals(
                0,
                SimSpecs.level(CONTENT, "act-2-homefront/level-11-atlantic-convoy", Difficulty.MEDIUM)
                        .sections()
                        .get(5)
                        .easeSeconds());
    }

    @Test
    void twelveOfTheFourteenGreyTanksAndTheMotifAloneOverTheCrawl() {
        Secondary secondary = SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM)
                .objectives()
                .secondary();
        assertEquals("warden-tank", secondary.escapes());
        assertEquals(2, secondary.escapesAllowed());
        LevelData.Music music = level().music();
        assertTrue(music.motifAlone(6), "the crawl");
        assertFalse(music.motifAlone(5));
        assertFalse(music.full(6));
        assertTrue(music.motifOn(5, 132.5), "under the walker fight");
        assertFalse(music.motifOn(7, 150), "not in the run-out");
        assertEquals("mars-foundry", music.ambience());
    }

    @Test
    void theChecksRejectKeysWithoutWhatTheyNeed() {
        assertProblem(
                replace(LEVEL, "  hard: {tell: 0.9, period: 3, ripple: 0.3}", "  hard: {tell: 0.9, period: 3}"),
                "ripple");
        assertProblem(replace(LEVEL, "    at: [[46, 70], [46.4, 410]]", "    at: [[46, 140], [46.4, 410]]"), "ride");
        assertProblem(
                replace(LEVEL, "  stems: {6: motif, 7: base}", "  stems: {6: motif, 7: base}\n  sound_off: 1"),
                "sound_off");
        assertProblem(replace(LEVEL, "  motif: {from: 132, sections: {7: false}}", ""), "the music needs its motif");
        assertProblem(replace(LEVEL, "allowed: 2,", "allowed: 14,"), "fewer may get away");
        assertProblem(
                replace(LEVEL, "    arc: up\n    at: [[17.6, 120]", "    arc: down\n    at: [[17.6, 120]"), "arc");
        assertProblem(
                replace(LEVEL, "    hold_after_boss: 1 ", "    hold_after_boss: 1\n    ramp: 1 "), "not on an arena");
        assertProblem(
                replace(TANK, "  walk: {speed: 40, turn_rate: 60, stride: 18}", "  straight: {speed: 40}"), "turret");
        assertProblem(replace(TANK, "  hard: {burst: 4,", "  hard: {homing: {turn_rate: 1}, burst: 4,"), "homing");
        assertProblem(replace(WALKER, "  plate: {part: hull,", "  plate: {part: chest,"), "chest");
        assertProblem(replace(WALKER, "    edges: [top, bottom,", "    edges: [bottom, top,"), "the first pass");
        assertProblem(replace(WALKER, "  stride:\n", "  stroll:\n"), "stroll");
        assertProblem(
                replace(WALKER, "share: {parts: [hull], below: 0.6}", "share: {parts: [torso], below: 0.6}"), "torso");
        assertProblem(
                replace(
                        WALKER,
                        "    parts: [hull]\n    notes:\n      text: 'phase 2: `aimed`",
                        "    parts: [head]\n    notes:\n      text: 'phase 2: `aimed`"),
                "head");
    }

    private static BossSpec.Attack attack(BossSpec boss, String name) {
        return boss.attacks().stream()
                .filter(attack -> attack.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static List<Double> toList(double[] values) {
        return java.util.Arrays.stream(values).boxed().toList();
    }

    private static void assertProblem(List<DataFile> files, String message) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    private static List<DataFile> replace(String path, String from, String to) {
        assertTrue(DesignTree.dataFiles().stream().anyMatch(f -> f.path().equals(path)), path);
        return DesignTree.dataFiles().stream()
                .map(f -> {
                    if (!f.path().equals(path)) {
                        return f;
                    }
                    assertTrue(f.text().contains(from), path + " has " + from);
                    return new DataFile(path, f.text().replace(from, to));
                })
                .toList();
    }
}
