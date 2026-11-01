package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Range;
import vanguard.sim.Secondary;
import vanguard.sim.WaveSpec;

/**
 * M10 part C, step C2: Level 37's data as the simulation gets it (design/campaign Level 37; the Mirror Interceptor's and
 * the Graft Turret's stat blocks; the allies' Aurelia livery). The fixtures of step C1's and part A's sim tests
 * ({@code Act6CSpecs}, {@code Act6ASpecs}) are what this data must map to.
 */
class FallOfAureliaDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static final String KEY = Level37Test.LEVEL;

    private static LevelScript level(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    private static List<WaveSpec> waves(Difficulty difficulty, String slug) {
        return level(difficulty).waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .toList();
    }

    @Test
    void theMirrorInterceptorFliesToItsStationsWithItsMirror() {
        List<WaveSpec> mirrors = waves(Difficulty.MEDIUM, "mirror-interceptor");
        assertEquals(
                List.of(38.0, 89.0, 131.0, 145.0),
                mirrors.stream().map(WaveSpec::t).toList());
        assertEquals(List.of(2, 4, 2, 2), mirrors.stream().map(WaveSpec::count).toList());
        assertEquals(WaveSpec.Formation.REAR_AMBUSH, mirrors.get(2).formation());
        EnemySpec mirror = mirrors.getFirst().enemy();
        assertEquals(120, mirror.hp());
        assertEquals(new Hitbox(52, 48), mirror.hitbox());
        assertEquals(30, mirror.bounty());
        assertTrue(mirror.machine());
        // Act6ASpecs.MIRROR: arc 120, heat 130, turn 60, down 3, regrow 0.4, gap 0.15, cool 0.5 after 0.3, 110 px/s, 4,
        // 30°
        assertEquals(
                Optional.of(new EnemySpec.Mirror(120, 130, 60, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30)), mirror.mirror());
        // Act6CSpecs.STATION: 90–130 px in, 90–120 px above the bottom, a 60 px strafe at 40 px/s, gap 80
        assertEquals(
                Optional.of(new EnemySpec.Station(new Range(90, 130), new Range(90, 120), 60, 40, 80)),
                mirror.station());
        EnemySpec.Hover hover = mirror.hover().orElseThrow();
        assertEquals(new Range(7, 7), hover.seconds());
        assertEquals(new Range(160, 300), hover.depth());
        assertTrue(mirror.sideHover().orElseThrow().exitBack());
        EnemyGun burst = mirror.gun().orElseThrow();
        assertEquals(3, burst.burst());
        assertEquals(0.15, burst.burstGapSeconds(), 1e-9);
        assertEquals(2.1, burst.intervalSeconds(), 1e-9);
        EnemySpec easy = waves(Difficulty.EASY, "mirror-interceptor").getFirst().enemy();
        assertEquals(100, easy.mirror().orElseThrow().arcDegrees());
        assertEquals(45, easy.mirror().orElseThrow().turnDegreesPerSecond());
        assertEquals(2, easy.gun().orElseThrow().burst());
        EnemySpec hard = waves(Difficulty.HARD, "mirror-interceptor").getFirst().enemy();
        assertEquals(140, hard.mirror().orElseThrow().arcDegrees());
        assertEquals(4, hard.gun().orElseThrow().burst());
        assertEquals(
                List.of(2, 2, 1, 2),
                waves(Difficulty.EASY, "mirror-interceptor").stream()
                        .map(WaveSpec::count)
                        .toList());
        assertEquals(
                List.of(2, 4, 2, 4),
                waves(Difficulty.HARD, "mirror-interceptor").stream()
                        .map(WaveSpec::count)
                        .toList());
    }

    @Test
    void theGraftTurretsRegrowOverTheirRootsInNineNests() {
        List<GroundUnit> grafts = level(Difficulty.MEDIUM).groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals("graft-turret"))
                .toList();
        assertEquals(27, grafts.size());
        EnemySpec graft = grafts.getFirst().enemy();
        assertEquals(24, graft.hp(), "12 base, the beam era's ×2");
        // Act6ASpecs.REGROWTH: window 5 s, knit 0.8 s, wake 0.5 s, its root 16 HP (8 × 2) in 24×24, bonus 5
        assertEquals(Optional.of(new EnemySpec.Regrowth(5.0, 0.8, 0.5, 16, new Hitbox(24, 24), 5)), graft.regrowth());
        EnemyGun pair = graft.gun().orElseThrow();
        assertEquals(2.2, pair.intervalSeconds(), 1e-9);
        assertEquals(2, pair.fan());
        assertEquals(Math.toRadians(4), pair.spreadRadians(), 1e-9);
        assertEquals(Math.toRadians(120), pair.turnRate(), 1e-9);
        assertEquals(Math.toRadians(110), pair.arcRadians(), 1e-9);
        assertEquals(15, graft.bounty());
        assertFalse(graft.spoofable(), "a hybrid: the IFF spoofer fools Ascendancy turrets only");
        EnemySpec hard = level(Difficulty.HARD).groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals("graft-turret"))
                .findFirst()
                .orElseThrow()
                .enemy();
        assertEquals(4.0, hard.regrowth().orElseThrow().windowSeconds());
        assertEquals(3, hard.gun().orElseThrow().fan());
        assertEquals(
                8,
                level(Difficulty.MEDIUM).groundUnits().stream()
                        .filter(unit -> unit.enemy().slug().equals("sam-nest"))
                        .count());
    }

    @Test
    void theShuttlesLiftInThreePairsAndTheSecondaryCountsThemHome() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = level(difficulty);
            ObjectiveSpec.Escort shuttles = level.objectives().airAllies().orElseThrow();
            assertEquals("evacuation-shuttle", shuttles.ally().slug());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 180.0;
                        case MEDIUM -> 120.0;
                        case HARD -> 90.0;
                    },
                    shuttles.ally().hp());
            ObjectiveSpec.Air air = shuttles.air().orElseThrow();
            assertEquals(6, air.stations().size());
            assertEquals(PlayField.HEIGHT - 170, air.stations().getFirst().y());
            assertEquals(PlayField.HEIGHT - 240, air.stations().get(1).y());
            assertEquals(
                    List.of(44.0, 72.0, 127.0),
                    air.groups().stream().map(lift -> lift.liftoff().t()).toList());
            assertEquals(
                    List.of(57.0, 85.0, 140.0),
                    air.groups().stream().map(lift -> lift.climb().t()).toList());
            assertEquals(
                    List.of(new ObjectiveSpec.Pad(190, 160), new ObjectiveSpec.Pad(290, 160)),
                    air.groups().getFirst().liftoff().pads());
            List<WaveSpec> waves = level.waves();
            List<WaveSpec> hooked = air.hookedWaves().stream().map(waves::get).toList();
            assertEquals(
                    List.of(46.0, 74.0, 130.0), hooked.stream().map(WaveSpec::t).toList());
            assertTrue(hooked.stream().allMatch(wave -> wave.enemy().slug().equals("talon")));
            Secondary saved = level.objectives().secondary();
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 4;
                        case MEDIUM -> 5;
                        case HARD -> 6;
                    },
                    saved.saved());
            assertEquals("SHUTTLES", saved.label());
            assertEquals(100, saved.credits());
        }
    }

    @Test
    void theVrellTurnAtTheHundredthSecondLinksFourPreyWaves() {
        LevelScript level = level(Difficulty.MEDIUM);
        assertEquals(Optional.of(100.0), level.turn());
        List<WaveSpec> hunters =
                level.waves().stream().filter(wave -> !wave.hunts().isEmpty()).toList();
        assertEquals(
                List.of(104.0, 118.0, 134.0, 144.0, 170.0),
                hunters.stream().map(WaveSpec::t).toList());
        assertTrue(
                level.waves().stream()
                        .filter(wave -> wave.t() > 96 && wave.t() < 103)
                        .findAny()
                        .isEmpty(),
                "the turn's window t=97–103 holds no new wave");
        List<CueTrigger> events = level.radio().stream().map(RadioCue::trigger).toList();
        assertTrue(events.contains(CueTrigger.FIRST_BOUNCE));
        assertTrue(events.contains(CueTrigger.FIRST_PREY_KILL));
        assertTrue(events.contains(CueTrigger.FIRST_ALLY_LOST));
        assertTrue(events.contains(CueTrigger.SECONDARY_FAILED));
    }

    @Test
    void theMusicChangesItsThemeAtTheTurnAndTheBackdropCarriesTheFall() {
        LevelData data = CONTENT.level(KEY);
        LevelData.Music music = data.music();
        assertEquals(14, music.trackAt(102));
        assertEquals(15, music.trackAt(103));
        assertEquals(Optional.of(100.0), music.motif().flatMap(LevelData.Music.Motif::to));
        assertEquals(
                List.of("none", "fall"),
                music.stemTimeList().stream()
                        .map(time -> time.stems().orElseThrow())
                        .toList());
        FallingCityData.Fall fall = data.backdrop().fall().orElseThrow();
        assertEquals(160, fall.t());
        assertEquals(2, fall.halves().size());
        assertEquals(100, data.backdrop().growth().orElseThrow().t());
        assertEquals(
                List.of("aurelia"),
                CONTENT.allies().allies().get("evacuation-shuttle").liveries().orElseThrow());
        assertEquals(Optional.of("aurelia"), data.airAllies().flatMap(LevelData.AirAllies::livery));
    }
}
