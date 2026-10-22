package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Intel;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * M8 part C (step C2): the keys of Level 23 and its units as loaded and checked by content: the Eel Swarm's attack list,
 * sway and scattering cut, the Glassfin's ribbon and ram, the siphon pump, the returning units' {@code underwater}
 * blocks, the level's five sections with the waterline, its {@code underwater_look} per section, the {@code waterline}
 * cue as a timed line, the head-first shield cell and the intel's waterline mark. How the simulation flies them is
 * step C3's (Level23Test).
 */
class DeepWaterKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String LEVEL = "act-4-deep-water/level-23-through-the-ice";
    private static final String LEVEL_23 = "campaign/" + LEVEL + "/data.yaml";
    private static final String LEVEL_22 = "campaign/act-4-deep-water/level-22-ice-shelf/data.yaml";
    private static final String GLASSFIN = "enemies/naval/glassfin/data.yaml";
    private static final String EEL = "enemies/naval/eel-swarm/data.yaml";

    @Test
    void theEelSwarmFiresTwoHeadAttacksSwaysAndScattersWhenCut() {
        EnemyData eel = CONTENT.enemy("eel-swarm");
        EnemyData.PartData head = eel.partList().orElseThrow().getFirst();
        assertEquals(List.of("head-aimed", "head-fan"), head.attackNames());
        assertEquals(Optional.of(18), head.firstBonus(), "a head-first kill pays the whole 48");
        EnemyData.SegmentChain chain = eel.segmentChain().orElseThrow();
        assertEquals(12, chain.segments());
        EnemyData.Cut cut = chain.cut().orElseThrow();
        assertTrue(cut.scatters());
        assertEquals(Optional.of(70.0), cut.scatter());
        assertEquals(Optional.of(3.0), cut.seconds());
        assertTrue(chain.regrow().isEmpty(), "no regrowth");
        assertEquals(
                Optional.of(new EnemyData.Sway(36, 0.8)), eel.movement().path().flatMap(EnemyData.PathMove::sway));
        assertEquals(245, eel.hp(), "head 100, 12 segments × 10, tail 25");
        assertEquals(56, eel.bounty());
        assertEquals(
                List.of("medium", "hard"),
                eel.attack("head-fan").orElseThrow().only().orElseThrow());
        EnemyData.Hook hard = eel.difficulty().flatMap(EnemyData.Hooks::hard).orElseThrow();
        assertEquals(Optional.of(16), hard.segments());
        assertEquals(
                Optional.of(1.4),
                hard.attacks().map(changes -> changes.get("head-aimed")).flatMap(EnemyData.AttackChange::interval));
        assertEquals(
                Optional.of(10), eel.difficulty().flatMap(EnemyData.Hooks::easy).flatMap(EnemyData.Hook::segments));
    }

    @Test
    void theGlassfinFlocksInARibbonAndRams() {
        EnemyData fry = CONTENT.enemy("glassfin");
        EnemyData.Flock flock = fry.movement().flock().orElseThrow();
        assertEquals(Optional.of(new EnemyData.Sway(14, 1.4)), flock.sway());
        assertEquals(Optional.of(new EnemyData.Ram(3, 0.25, 0.3, 0.5, 230)), flock.ram());
        assertEquals(
                Optional.of(0.15),
                fry.difficulty()
                        .flatMap(EnemyData.Hooks::easy)
                        .flatMap(EnemyData.Hook::ram)
                        .flatMap(EnemyData.RamChange::share));
        assertEquals(
                Optional.of(0.40),
                fry.difficulty()
                        .flatMap(EnemyData.Hooks::hard)
                        .flatMap(EnemyData.Hook::ram)
                        .flatMap(EnemyData.RamChange::share));
        assertTrue(fry.ignoredBySeekers());
        assertEquals(Tier.TINY, fry.tier());
    }

    @Test
    void theSiphonPumpIsAGroundTargetWithoutAnAttack() {
        EnemyData pump = CONTENT.enemy("siphon-pump");
        assertEquals("ground", pump.layer());
        assertEquals(40, pump.hp());
        assertEquals(20, pump.bounty());
        assertTrue(pump.attacks().isEmpty());
        assertEquals(23, pump.firstLevel());
    }

    @Test
    void theReturningUnitsChangeUnderWater() {
        EnemyData jelly = CONTENT.enemy("driftjelly");
        EnemyData wet = jelly.inWater();
        assertEquals("ground", jelly.layer());
        assertTrue(jelly.submerge().isPresent());
        assertEquals("air", wet.layer(), "on the play plane");
        assertTrue(wet.submerge().isEmpty(), "no surface/submerge swap");
        assertEquals(jelly.attacks(), wet.attacks(), "the ring unchanged");
        EnemyData spitter = CONTENT.enemy("reef-spitter");
        assertEquals(Optional.of(10.0), spitter.movement().terrain().flatMap(EnemyData.Terrain::drift));
        EnemyData floor = spitter.inWater();
        assertEquals(Optional.empty(), floor.movement().terrain().flatMap(EnemyData.Terrain::drift), "no drift");
        assertEquals("ground", floor.layer());
        assertEquals(Optional.of("floor"), spitter.underwater().flatMap(EnemyData.Underwater::look));
        EnemyData lamprey = CONTENT.enemy("lamprey");
        assertTrue(lamprey == lamprey.inWater(), "unchanged without a block");
    }

    @Test
    void level23DivesAtItsWaterlineWithALookPerSection() {
        LevelData level = CONTENT.level(LEVEL);
        assertEquals(5, level.sections().size(), "the README's four, the waterline its own (gap check D1)");
        assertEquals(List.of(25.5), level.waterlines());
        assertEquals(Optional.of(25.5), level.firstWaterline());
        assertEquals(LevelData.Medium.AIR, level.mediumOf(0));
        assertTrue(level.specialLimits().isEmpty(), "the medium forbids the Airstrike and the Lance (D9)");
        assertEquals(Optional.empty(), level.underwaterLookOf(0), "the dry shaft");
        LevelData.UnderwaterLook shaft = level.underwaterLookOf(2).orElseThrow();
        assertEquals(level.underwaterLook().orElseThrow(), shaft);
        assertEquals(0.5, shaft.ambient());
        LevelData.UnderwaterLook farms = level.underwaterLookOf(3).orElseThrow();
        assertEquals(0.58, farms.ambient());
        assertEquals("ccf2ff", farms.tint());
        assertEquals(shaft.snow() > 0, farms.snow() > 0);
        assertTrue(level.underwaterLookOf(4).orElseThrow().ambient() < farms.ambient(), "the plain's edge darker");
    }

    @Test
    void theWaterlineCueIsATimedLineAtTheWaterline() {
        LevelScript script = SimSpecs.level(CONTENT, LEVEL, Difficulty.MEDIUM);
        List<vanguard.sim.RadioCue> waterline = script.radio().stream()
                .filter(cue -> cue.line().startsWith("Under the ice."))
                .toList();
        assertEquals(1, waterline.size());
        assertEquals(CueTrigger.TIME, waterline.getFirst().trigger());
        assertEquals(25.5, waterline.getFirst().t());
    }

    @Test
    void theFirstEelsHeadFirstKillDropsTheShieldCell() {
        LevelData level = CONTENT.level(LEVEL);
        LevelData.PlacedPickup cell = level.pickups().stream()
                .filter(pickup -> pickup.carried() && pickup.carrier().dropsHeadFirst())
                .findFirst()
                .orElseThrow();
        assertEquals(42, cell.carrier().waveT());
        assertEquals(LevelData.CarrierUnit.FIRST, cell.carrier().unit());
        assertFalse(level.pickups().stream()
                .filter(pickup -> pickup.carried() && pickup.carrier().waveT() == 54.5)
                .anyMatch(pickup -> pickup.carrier().dropsHeadFirst()));
    }

    @Test
    void theIntelStripMarksTheWaterline() {
        Intel intel = Intel.of(CONTENT, LEVEL, 3);
        assertEquals(
                List.of(25.5),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE)
                        .map(Intel.Mark::from)
                        .toList());
        assertTrue(Intel.of(CONTENT, "act-4-deep-water/level-22-ice-shelf", 3).marks().stream()
                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE));
    }

    @Test
    void theNewKeysAreCheckedWhereTheyBelong() {
        assertProblem(
                LEVEL_22,
                text -> text
                        + "underwater_look: {ambient: 0.5, tint: bfe0ff, caustics: 0.4, snow: 60, headlights: 0.8}\n",
                "only on a level that is under water somewhere");
        assertProblem(
                LEVEL_22,
                text -> text.replace(
                        "{pickup: overdrive, dropped_by: {wave: 20, unit: first}}",
                        "{pickup: overdrive, dropped_by: {wave: 20, unit: first, head_first: true}}"),
                "only a segment chain's wave drops on a head-first kill");
        assertProblem(
                LEVEL_22,
                text -> text.replace("{event: first-geyser,", "{event: waterline,")
                        .replace("{t: 29, speaker: Varga", "{event: first-geyser, speaker: Varga"),
                "waterline in a level without a waterline");
        assertProblem(
                LEVEL_23,
                text -> text.replace(
                        "    tiles: [shaft-dry]\n", "    tiles: [shaft-dry]\n    underwater_look: {ambient: 0.4}\n"),
                "only on a section under water");
        assertProblem(GLASSFIN, text -> text.replace("    ram:\n", "    ram_off:\n"), "ram_off");
        assertProblem(
                EEL,
                text -> text.replace("cut: {scatter: 70, seconds: 3}", "cut: {scatter: 70}"),
                "a scattering cut has its scatter and its seconds");
    }

    private static List<DataFile> edited(String path, UnaryOperator<String> edit) {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(path, edit.apply(f.text())) : f)
                .toList();
        assertTrue(files.stream().anyMatch(f -> f.path().equals(path)), path);
        return files;
    }

    /** Loads the design tree with one file edited; a problem containing {@code part} is reported. */
    private static void assertProblem(String path, UnaryOperator<String> edit, String part) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(edited(path, edit)));
        assertTrue(
                e.problems().stream().anyMatch(problem -> problem.contains(part)), "'" + part + "' in " + e.problems());
    }
}
