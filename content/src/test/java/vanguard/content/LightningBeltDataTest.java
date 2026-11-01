package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Secondary;
import vanguard.sim.WaveSpec;

/**
 * M10 part D, step D2: Level 38's data as the simulation gets it (design/campaign Level 38; the Harrow's stat block;
 * the Buzzsaw Drone's Act 6 return). The fixtures of step D1's sim tests ({@code Act6DSpecs}: the Harrow's ram, the
 * cargo pod, the canister, {@code HARROWS n / 23}) are what this data must map to; the new keys are loaded strictly and
 * checked by the static validator ({@link StormLayerChecks}).
 */
class LightningBeltDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static final String KEY = Level38Test.LEVEL;
    private static final String PATH = "campaign/" + KEY + "/data.yaml";
    private static final String HARROW = "enemies/air/harrow/data.yaml";

    private static LevelScript level(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    private static List<WaveSpec> waves(Difficulty difficulty, String slug) {
        return level(difficulty).waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .toList();
    }

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
    void theHarrowChasesLocksAtItsTellsEndAndRamsOutWithItsRing() {
        List<WaveSpec> harrows = waves(Difficulty.MEDIUM, "harrow");
        assertEquals(
                List.of(30.5, 42.5, 70.0, 94.0, 121.0, 137.0, 157.0, 175.0),
                harrows.stream().map(WaveSpec::t).toList());
        assertEquals(35, harrows.stream().mapToInt(WaveSpec::count).sum());
        EnemySpec harrow = harrows.getFirst().enemy();
        // Act6DSpecs.HARROW: 16 HP (beam era), 22 px hit box, air, 12 credits, 110 px/s, a machine, its ring
        assertEquals(16, harrow.hp(), "8 base, the beam era's ×2");
        assertEquals(new Hitbox(22, 22), harrow.hitbox());
        assertEquals(Layer.AIR, harrow.layer());
        assertEquals(12, harrow.bounty());
        assertTrue(harrow.machine());
        assertTrue(harrow.gun().isEmpty(), "its ring is no gun");
        // Act6DSpecs.RAM: chase 90°/s for a seeded 1.5–2.5 s, tell 0.6 s at 50 px/s, one ram at 300 px/s, out
        assertEquals(
                Optional.of(new EnemySpec.Dash(
                        Math.toRadians(90), 1.5, 0.6, 50, 300, 0, 0, 1, EnemySpec.Dash.Variant.ram(2.5))),
                harrow.dash());
        EnemySpec.DeathBurst ring = harrow.deathBurst().orElseThrow();
        assertEquals(8, ring.count());
        assertEquals(90, ring.speed(), 1e-9);
        assertTrue(ring.onContact());
        assertTrue(ring.between());
        assertEquals(0.25, ring.graceSeconds(), 1e-9);
        EnemySpec easy = waves(Difficulty.EASY, "harrow").getFirst().enemy();
        assertEquals(0.8, easy.dash().orElseThrow().tellSeconds(), 1e-9);
        assertEquals(240, easy.dash().orElseThrow().dashSpeed(), 1e-9);
        EnemySpec hard = waves(Difficulty.HARD, "harrow").getFirst().enemy();
        assertEquals(0.5, hard.dash().orElseThrow().tellSeconds(), 1e-9);
        assertEquals(340, hard.dash().orElseThrow().dashSpeed(), 1e-9);
        assertEquals(100, hard.deathBurst().orElseThrow().speed(), 1e-9);
        assertEquals(
                List.of(3, 4, 4, 4, 4, 4, 4, 4),
                waves(Difficulty.EASY, "harrow").stream().map(WaveSpec::count).toList(),
                "easy: at most 4 on the screen");
        assertEquals(
                List.of(3, 4, 4, 5, 6, 4, 5, 4),
                waves(Difficulty.HARD, "harrow").stream().map(WaveSpec::count).toList(),
                "hard: pinned at medium's (at most 6 on the screen)");
        assertEquals(WaveSpec.Entry.REAR, harrows.get(3).entry());
        assertEquals(WaveSpec.Formation.CROSS, harrows.get(5).formation());
    }

    @Test
    void theBuzzsawReturnsUnchangedAndBouncesOffTheEdgesOnly() {
        List<WaveSpec> buzzsaws = waves(Difficulty.MEDIUM, "buzzsaw-drone");
        assertEquals(
                List.of(62.0, 84.5, 128.5, 165.0),
                buzzsaws.stream().map(WaveSpec::t).toList());
        assertEquals(17, buzzsaws.stream().mapToInt(WaveSpec::count).sum());
        EnemySpec.Dash dash = buzzsaws.getFirst().enemy().dash().orElseThrow();
        assertEquals(EnemySpec.Dash.Variant.BUZZSAW, dash.variant());
        assertEquals(3, dash.bounces());
        assertEquals(20, buzzsaws.getFirst().enemy().hp(), "10 base, the beam era's ×2, no act factor");
        assertTrue(
                level(Difficulty.MEDIUM).hazards().debris().stream().noneMatch(chunk -> Double.isInfinite(chunk.hp())),
                "no large chunk: the dash bounces off the edges only");
    }

    @Test
    void theStrikesFollowTheDifficultyWithEightTrackingColumnsOnHard() {
        for (Difficulty difficulty : Difficulty.values()) {
            HazardSpec.Strikes strikes = level(difficulty)
                    .hazards()
                    .weather()
                    .flatMap(HazardSpec.Weather::strikes)
                    .orElseThrow();
            assertEquals(difficulty == Difficulty.EASY ? 13 : 26, strikes.at().size(), difficulty.name());
            assertEquals(
                    difficulty == Difficulty.HARD ? 8 : 0,
                    strikes.at().stream().filter(HazardSpec.Strike::track).count(),
                    difficulty.name());
            assertEquals(26, strikes.at().getFirst().t());
            assertTrue(strikes.at().getLast().t() <= 163, "none after t=163 (the breakout)");
        }
        HazardSpec.Weather hard = level(Difficulty.HARD).hazards().weather().orElseThrow();
        assertEquals(
                List.of(80.0, 86.0, 111.0, 140.0, 146.0),
                hard.gusts().stream().map(HazardSpec.Gust::t).sorted().toList());
        assertTrue(hard.gusts().stream().noneMatch(HazardSpec.Gust::fromLeft), "from the right");
    }

    @Test
    void thePodHoldsTheSecretAndTheCanisterItsArmourPatchUnderItsBrackets() {
        List<HazardSpec.DebrisSpec> debris = level(Difficulty.MEDIUM).hazards().debris();
        assertEquals(2, debris.size());
        HazardSpec.DebrisSpec canister = debris.stream()
                .filter(chunk -> chunk.chunk().equals("canister"))
                .findFirst()
                .orElseThrow();
        assertEquals(100, canister.t());
        assertEquals(240, canister.x());
        assertEquals(10, canister.hp());
        assertEquals(Optional.of(PickupType.ARMOUR_PATCH), canister.drop());
        assertTrue(canister.marked());
        assertFalse(canister.holdsSecret());
        // Round 76 L.1 b: no latch; it opens by itself 270 px from the top (y up in the simulation).
        assertTrue(canister.opens());
        assertEquals(PlayField.HEIGHT - 270, canister.opensAt());
        HazardSpec.DebrisSpec pod = debris.stream()
                .filter(chunk -> chunk.chunk().equals("cargo-pod"))
                .findFirst()
                .orElseThrow();
        assertEquals(118, pod.t());
        assertEquals(20, pod.hp());
        assertTrue(pod.holdsSecret());
        assertEquals(100, pod.crateCredits());
        assertFalse(pod.marked());
        assertTrue(pod.drop().isEmpty());
    }

    @Test
    void theHarrowsSecondaryIsAKillRatioOfTheHarrowsJudgedAtTheEnd() {
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary = level(difficulty).objectives().secondary();
            assertEquals(0.65, secondary.killRatio(), 1e-9);
            assertEquals("harrow", secondary.enemy());
            assertTrue(secondary.scoped());
            assertEquals("HARROWS", secondary.label());
            assertEquals(100, secondary.credits());
        }
        List<CueTrigger> events =
                level(Difficulty.MEDIUM).radio().stream().map(RadioCue::trigger).toList();
        assertTrue(events.contains(CueTrigger.FIRST_STRIKE_KILL));
        assertTrue(events.contains(CueTrigger.SECRET));
        assertTrue(events.contains(CueTrigger.SECONDARY_OBJECTIVE));
        assertTrue(level(Difficulty.MEDIUM).groundUnits().isEmpty(), "no ground");
    }

    @Test
    void theHuntsAndTheMusic() {
        LevelScript medium = level(Difficulty.MEDIUM);
        List<WaveSpec> hunters =
                medium.waves().stream().filter(wave -> !wave.hunts().isEmpty()).toList();
        assertEquals(List.of(74.0, 113.0), hunters.stream().map(WaveSpec::t).toList());
        assertEquals(Optional.empty(), medium.turn(), "live from the hunters' entry");
        LevelData.Music music = CONTENT.level(KEY).music();
        assertEquals(15, music.track());
        assertEquals(3, music.ambienceRise(2), 1e-9);
        assertEquals(3, music.ambienceRise(4), 1e-9);
        assertEquals(0, music.ambienceRise(5), 1e-9);
        assertEquals("jovian-sky", music.ambience());
    }

    @Test
    void theNewKeysAreChecked() {
        assertProblem(
                HARROW,
                "bounces: 0, dashes: 1, lock: tell-end, after: out}",
                "bounces: 1, dashes: 1, lock: tell-end, after: out}",
                "a ram out (after: out) has bounces: 0, dashes: 1");
        assertProblem(HARROW, "lock: tell-end", "lock: tell-middle", "lock: tell-start or tell-end");
        assertProblem(
                PATH,
                "{kill_ratio: 0.65, enemy: harrow,",
                "{kill_ratio: 0.65, enemy: harow,",
                "objectives.secondary.enemy: unknown enemy 'harow'");
        assertProblem(
                PATH,
                "{kill_ratio: 0.65, enemy: harrow,",
                "{kill_ratio: 0.65, enemy: gilded-gunship,",
                "objectives.secondary.enemy: no wave of 'gilded-gunship' to count (easy)");
        assertProblem(
                PATH,
                "drift: [0, -60], drop: armour patch, marked: true, opens_at: 270}",
                "drift: [0, -60], drop: armour patch, marked: true, opens_at: 270, reveals: Aurelia cargo pod}",
                "a chunk drops a pickup or reveals a secret, not both");
        assertProblem(
                PATH,
                "drift: [0, -60], drop: armour patch, marked: true, opens_at: 270}",
                "drift: [0, -60], marked: true, opens_at: 270}",
                "opens_at: only a chunk with a drop opens by itself");
        assertProblem(
                PATH,
                "drift: [0, -60], drop: armour patch, marked: true, opens_at: 270}",
                "drift: [0, -60], drop: armour patch, marked: true, opens_at: 540}",
                "opens_at: px from the top, 0 or more and under 540");
        assertProblem(
                PATH,
                "ambience_db: {2: 3, 3: 3, 4: 3}",
                "ambience_db: {2: 3, 6: 3}",
                "music.ambience_db: no section 6");
        assertProblem(
                PATH,
                "  - {t: 84.5, formation: stream, enemy: buzzsaw-drone,",
                "  - {t: 88.5, formation: stream, enemy: buzzsaw-drone,",
                "a wave of bouncing dashers and one of ramming dashers at least 7.0 s apart (easy: t=88.5 and t=94.0)");
        assertProblem(
                PATH,
                "      - {t: 140, from: right}",
                "      - {t: 139, from: right}",
                "no gust within 2.0 s of a strike's tell (easy: a gust at t=139.0, the strike at t=138.0)");
        assertProblem(
                PATH,
                "    - {t: 114, x: 240, skip: [easy]}",
                "    - {t: 114, x: 170, skip: [easy]}",
                "no column over a station of the t=111.0 hornet line while it holds (medium: the strike at t=114.0, x=170.0,"
                        + " a station at x=160.0)");
    }
}
