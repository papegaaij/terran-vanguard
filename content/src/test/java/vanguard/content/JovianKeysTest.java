package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Regrowing;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part A, step A2a (m10a-gaps.md §1 #1–#5, #8, #10, #11, content side): Act 6's unit keys loaded, checked and mapped
 * for the simulation (A1a's {@code EnemySpec.Mirror}, {@code Regeneration}, {@code Regrowth}, {@code Dome}, the {@code
 * spoofable} flag, the {@code death-burst} on contact, the prey link) on stand-ins where no Act 6 unit has its data yet:
 * a mirrored medium, a regenerating small, a Graft-like regrowing turret, a Harrow-like ring, a pylon with a covered
 * turret, a hunter/prey pair on a built level and the spoofable turrets; each loaded from data and flown on the test
 * range or in its level.
 */
class JovianKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final double SHIP_X = PlayField.WIDTH / 2.0;
    /** The test range's scroll ({@link ActThreeArsenalFlightTest#range}), px/s. */
    private static final double RANGE_SCROLL = 30;

    private static final String MIRROR = "enemies/air/test-mirror/data.yaml";
    private static final String CHIMERA = "enemies/air/test-chimera/data.yaml";
    private static final String HARROW = "enemies/air/test-harrow/data.yaml";
    private static final String GRAFT = "enemies/ground/test-graft/data.yaml";
    private static final String PYLON = "enemies/ground/test-pylon/data.yaml";
    private static final String TURRET = "enemies/ground/test-turret/data.yaml";
    private static final String LEVEL_30 = "campaign/act-5-the-belt/level-30-minefield/data.yaml";
    private static final String LEVEL_33 = "campaign/act-5-the-belt/level-33-leech-field/data.yaml";

    /** A stand-in Mirror Interceptor (the approved stat block's numbers: medium, 120 HP, its mirror and station). */
    private static final String MIRROR_YAML = """
            name: Test Mirror
            faction: Jovian Ascendancy
            layer: air
            tier: medium
            size: [72, 68]
            hitbox: [52, 48]
            parts: single
            orientation: 16 angles
            hp: 120
            armour: none
            speed: 180
            machine: true
            mirror: {arc: 120, heat: 130, turn: 60, down: 3.0, regrow: 0.4, gap: 0.15, cool: 0.5, cool_delay: 0.3, fire_within: 30, bullet: small, speed: 110}
            movement:
              hover: {seconds: 7, y: [160, 300], edge_x: 110, exit: back}
            attacks:
              - {pattern: aimed, name: pods, bullet: medium, interval: 2.4, speed: 160, burst: 3, burst_gap: 0.15}
            formations:
              - {name: pincer, size: [2, 4]}
            weak_points: []
            drops: []
            traits: [shield-breaker, side, rear, beam]
            bounty: 30
            first_level: 37
            difficulty:
              easy: {mirror: {arc: 100, turn: 45}, burst: 2, attacks: {pods: {interval: 2.8}}}
              hard: {mirror: {arc: 140, turn: 80}, burst: 4, attacks: {pods: {interval: 2.0}}}
            """;

    /** A stand-in Chimera (small, 20 HP, regenerating 20 % a second after 1.0 s). */
    private static final String CHIMERA_YAML = """
            name: Test Chimera
            faction: Hybrid
            layer: air
            tier: small
            size: [40, 36]
            hitbox: [28, 24]
            parts: single
            orientation: 16 angles
            hp: 20
            armour: none
            speed: 220
            machine: true
            regeneration: {rate: 0.20, delay: 1.0}
            movement:
              hover: {seconds: 10, y: [380, 460], drift: 12}
            attacks: []
            formations:
              - {name: V-wing, size: [5, 7]}
            weak_points: []
            drops: []
            traits: [beam, homing, forward, spread]
            bounty: 14
            first_level: 36
            difficulty:
              easy: {regeneration: {rate: 0.15, delay: 1.3}}
              hard: {regeneration: {rate: 0.25, delay: 0.8}}
            """;

    /** A stand-in Harrow (small, 8 HP, its ring of 8 on every death, a ram's included). */
    private static final String HARROW_YAML = """
            name: Test Harrow
            faction: Hybrid
            layer: air
            tier: small
            size: [34, 34]
            hitbox: [22, 22]
            parts: single
            orientation: 16 angles
            hp: 8
            armour: none
            speed: 110
            machine: true
            movement:
              straight: {speed: 110}
            attacks:
              - {pattern: death-burst, name: ring, bullet: small, speed: 90, count: 8, on_contact: true, between: true, grace: 0.25}
            formations:
              - {name: stream}
            weak_points: []
            drops: []
            traits: [homing, beam, spread, side, rear]
            bounty: 12
            first_level: 38
            difficulty:
              hard: {attacks: {ring: {speed: 100}}}
            """;

    /** A stand-in Graft Turret (small, 12 HP, its root 8 under it, the 5 s window). */
    private static final String GRAFT_YAML = """
            name: Test Graft
            faction: Hybrid
            layer: ground
            tier: small
            size: [44, 44]
            hitbox: [32, 32]
            parts: single
            orientation: 32 angles
            hp: 12
            armour: none
            speed: 0
            machine: true
            regrowth: {window: 5.0, knit: 0.8, wake: 0.5, root: {hp: 8, hitbox: [24, 24], bounty: 5}}
            movement:
              terrain: {}
            attacks: []
            formations:
              - {name: turret nest, size: [3, 6]}
            weak_points: []
            drops: []
            traits: [anti-ground, area, beam]
            bounty: 15
            first_level: 37
            difficulty:
              easy: {regrowth: {window: 6}}
              hard: {regrowth: {window: 4, knit: 0.6}}
            """;

    /** A stand-in Shield Pylon (medium, 240 HP, its dome of 110 px 140 px along its crown). */
    private static final String PYLON_YAML = """
            name: Test Pylon
            faction: Jovian Ascendancy
            layer: ground
            tier: medium
            size: [56, 56]
            hitbox: [40, 40]
            parts: single
            orientation: fixed
            hp: 240
            armour: none
            speed: 0
            machine: true
            dome: {radius: 110, offset: 140, breaker_drop: 2.0, back: 0.3}
            movement:
              terrain: {}
            attacks: []
            formations:
              - {name: turret nest, size: [2, 3]}
            weak_points: []
            drops: []
            traits: [anti-ground, area, piercing]
            bounty: 45
            first_level: 33
            difficulty:
              easy: {dome: {radius: 95}}
              hard: {dome: {radius: 125, breaker_drop: 1.0}}
            """;

    /** A stand-in spoofable Ascendancy turret (a covered gun under the dome). */
    private static final String TURRET_YAML = """
            name: Test Turret
            faction: Jovian Ascendancy
            layer: ground
            tier: medium
            size: [36, 36]
            hitbox: [30, 30]
            parts: single
            orientation: 32 angles
            hp: 60
            armour: none
            speed: 0
            machine: true
            spoofable: true
            movement:
              terrain: {}
            attacks:
              - {pattern: aimed, name: gun, bullet: medium, interval: 2.0, speed: 160}
            formations:
              - {name: turret nest, size: [1, 3]}
            weak_points: []
            drops: []
            traits: [anti-ground]
            bounty: 20
            first_level: 39
            """;

    /** The design tree with the stand-ins ({@code fixtures}: a stand-in's path → its text instead) and the edits. */
    private static List<DataFile> files(Map<String, String> fixtures, String... edits) {
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
        Map<String, String> standIns = new java.util.LinkedHashMap<>(Map.of(
                MIRROR, MIRROR_YAML,
                CHIMERA, CHIMERA_YAML,
                HARROW, HARROW_YAML,
                GRAFT, GRAFT_YAML,
                PYLON, PYLON_YAML,
                TURRET, TURRET_YAML));
        standIns.putAll(fixtures);
        standIns.forEach((path, text) -> files.add(new DataFile(path, text)));
        return files;
    }

    private static final Content STAND_INS = ContentLoader.load(files(Map.of()));

    private static Content content(String... edits) {
        return ContentLoader.load(files(Map.of(), edits));
    }

    private static List<String> problems(Map<String, String> fixtures, String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(fixtures, edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    private static EnemySpec unit(String slug, Difficulty difficulty, int act, int level) {
        return SimSpecs.enemy(STAND_INS, slug, difficulty, Optional.empty(), act, level);
    }

    private static Sortie range(GroundUnit... units) {
        return ActThreeArsenalFlightTest.range(
                ActThreeArsenalFlightTest.loadout(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1)),
                true,
                units);
    }

    // --- the mirror -------------------------------------------------------------------------------

    @Test
    void theMirrorTakesItsNumbersFromTheStatBlockAndItsHooks() {
        EnemySpec.Mirror medium =
                unit("test-mirror", Difficulty.MEDIUM, 6, 37).mirror().orElseThrow();
        assertEquals(new EnemySpec.Mirror(120, 130, 60, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30), medium);
        EnemySpec.Mirror easy =
                unit("test-mirror", Difficulty.EASY, 6, 37).mirror().orElseThrow();
        assertEquals(100, easy.arcDegrees());
        assertEquals(45, easy.turnDegreesPerSecond());
        assertEquals(STAND_INS.difficulty().enemyHp(130, Difficulty.EASY), easy.heat(), "the heat takes the HP lever");
        EnemySpec.Mirror hard =
                unit("test-mirror", Difficulty.HARD, 6, 37).mirror().orElseThrow();
        assertEquals(140, hard.arcDegrees());
        assertEquals(80, hard.turnDegreesPerSecond());
        assertEquals(110, hard.bulletSpeed(), "the bounce's slow 110 px/s, no bullet-speed lever");
        // A returning medium unit: the act factor on its heat, as on a shield (≈ 140 at L40, ≈ 148 at L42).
        assertEquals(
                Math.rint(130 * 160.0 / 148),
                unit("test-mirror", Difficulty.MEDIUM, 6, 40)
                        .mirror()
                        .orElseThrow()
                        .heat());
        assertEquals(
                Math.rint(130 * 168.0 / 148),
                unit("test-mirror", Difficulty.MEDIUM, 6, 42)
                        .mirror()
                        .orElseThrow()
                        .heat());
        // Its pods: the burst of 3 every 2.4 s (easy 2 every 2.8, hard 4 every 2.0); no hex shield beside the mirror.
        EnemySpec spec = unit("test-mirror", Difficulty.MEDIUM, 6, 37);
        assertEquals(3, spec.gun().orElseThrow().burst());
        assertEquals(
                2,
                unit("test-mirror", Difficulty.EASY, 6, 37).gun().orElseThrow().burst());
        assertEquals(
                4,
                unit("test-mirror", Difficulty.HARD, 6, 37).gun().orElseThrow().burst());
        assertTrue(spec.shield().isEmpty());
        assertTrue(spec.hover().isPresent(), "it holds its station (its facing turns only there)");
    }

    /** Flown: the front gun's shots on the mirror bounce back as slow {@code small} bullets; the hull takes nothing. */
    @Test
    void theMirrorBouncesTheShipsShotsOnTheRange() {
        EnemySpec mirrored = unit("test-mirror", Difficulty.MEDIUM, 6, 37)
                .withGun(Optional.empty())
                .withTerrain(true);
        Sortie sortie = range(ActThreeArsenalFlightTest.at(SHIP_X, mirrored));
        int bounced = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.FIRE.bit());
            for (int b = 0; b < sortie.bulletCount(); b++) {
                EnemyBullet bullet = sortie.bullet(b);
                assertTrue(bullet.bounced(), "the unit has no gun: every bullet is a bounced shot");
                assertEquals(4, bullet.damage());
                bounced++;
            }
        }
        Enemy unit = ActThreeArsenalFlightTest.find(sortie, mirrored).orElseThrow();
        assertTrue(bounced > 0, "shots bounced");
        assertEquals(120, unit.hp(), "the mirror took every shot");
        // The same unit without its mirror is hit.
        EnemySpec bare = mirrored.withMirror(Optional.empty());
        Sortie control = range(ActThreeArsenalFlightTest.at(SHIP_X, bare));
        for (int i = 0; i < SimStep.ticks(8); i++) {
            control.step(Command.FIRE.bit());
        }
        Optional<Enemy> hit = ActThreeArsenalFlightTest.find(control, bare);
        assertTrue(hit.isEmpty() || hit.get().hp() < 120, "without its mirror it is hit");
    }

    // --- regeneration -----------------------------------------------------------------------------

    @Test
    void regenerationTakesItsRateAndDelayByDifficulty() {
        assertEquals(
                Optional.of(new EnemySpec.Regeneration(0.20, 1.0)),
                unit("test-chimera", Difficulty.MEDIUM, 6, 36).regeneration());
        assertEquals(
                Optional.of(new EnemySpec.Regeneration(0.15, 1.3)),
                unit("test-chimera", Difficulty.EASY, 6, 36).regeneration());
        assertEquals(
                Optional.of(new EnemySpec.Regeneration(0.25, 0.8)),
                unit("test-chimera", Difficulty.HARD, 6, 36).regeneration());
        assertEquals(40, unit("test-chimera", Difficulty.MEDIUM, 6, 36).hp(), "20 with the beam era's ×2");
    }

    /** Flown: a short burst hurts it, it heals after its delay back to its maximum and never past it. */
    @Test
    void aRegeneratingUnitHealsOnTheRange() {
        EnemySpec chimera = unit("test-chimera", Difficulty.MEDIUM, 6, 36).withTerrain(true);
        Sortie sortie = range(ActThreeArsenalFlightTest.at(SHIP_X, chimera));
        Enemy unit = null;
        for (int i = 0; i < SimStep.ticks(20) && (unit == null || unit.hp() > 30); i++) {
            sortie.step(Command.FIRE.bit());
            unit = ActThreeArsenalFlightTest.find(sortie, chimera).orElse(null);
        }
        assertNotNull(unit);
        assertTrue(unit.hp() <= 30 && unit.hp() > 0, "hurt, not killed: " + unit.hp());
        double lowest = unit.hp();
        boolean healed = false;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            lowest = Math.min(lowest, unit.hp());
            assertTrue(unit.hp() <= 40, "never past its maximum");
            healed |= unit.hp() > lowest;
        }
        assertTrue(healed, "it healed");
        assertEquals(40, unit.hp(), "back to its maximum");
    }

    // --- regrowth ---------------------------------------------------------------------------------

    @Test
    void regrowthTakesItsWindowKnitAndRootByDifficulty() {
        EnemySpec.Regrowth medium =
                unit("test-graft", Difficulty.MEDIUM, 6, 37).regrowth().orElseThrow();
        assertEquals(5.0, medium.windowSeconds());
        assertEquals(0.8, medium.knitSeconds());
        assertEquals(0.5, medium.wakeSeconds());
        assertEquals(16, medium.rootHp(), "the root's 8 with the beam era's ×2, as the turret's");
        assertEquals(5, medium.rootBounty());
        assertEquals(24, medium.rootHitbox().width());
        assertEquals(24, unit("test-graft", Difficulty.MEDIUM, 6, 37).hp());
        assertEquals(
                6,
                unit("test-graft", Difficulty.EASY, 6, 37)
                        .regrowth()
                        .orElseThrow()
                        .windowSeconds());
        EnemySpec.Regrowth hard =
                unit("test-graft", Difficulty.HARD, 6, 37).regrowth().orElseThrow();
        assertEquals(4, hard.windowSeconds());
        assertEquals(0.6, hard.knitSeconds());
        assertEquals(Math.rint(16 * 1.3), hard.rootHp());
    }

    /** Flown: the turret's death exposes the root; left alone it knits back with its full HP; shot, the root dies. */
    @Test
    void aRegrowingTurretExposesItsRootOnTheRange() {
        EnemySpec graft = unit("test-graft", Difficulty.MEDIUM, 6, 37);
        Sortie sortie = range(ActThreeArsenalFlightTest.at(SHIP_X, graft));
        Enemy unit = null;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(Command.FIRE.bit());
            unit = ActThreeArsenalFlightTest.find(sortie, graft).orElse(null);
            if (unit != null && unit.regrowth().phase() == Regrowing.Phase.ROOT) {
                break;
            }
        }
        assertNotNull(unit, "the turret stays in play over its root");
        assertEquals(Regrowing.Phase.ROOT, unit.regrowth().phase());
        assertTrue(unit.regrowth().paid(), "its first death paid");
        // Left alone: the window, the knit, then a turret again at its full HP.
        boolean knit = false;
        for (int i = 0; i < SimStep.ticks(7); i++) {
            sortie.step(Command.NONE);
            knit |= unit.regrowth().phase() == Regrowing.Phase.KNIT;
        }
        assertTrue(knit, "it knitted back");
        assertEquals(Regrowing.Phase.TURRET, unit.regrowth().phase());
        assertEquals(24, unit.hp());
        // Shot through a second death and its window: the root dies and the unit is gone for good.
        boolean gone = false;
        for (int i = 0; i < SimStep.ticks(15) && !gone; i++) {
            sortie.step(Command.FIRE.bit());
            gone = ActThreeArsenalFlightTest.find(sortie, graft).isEmpty();
        }
        assertTrue(gone, "the root died in its window");
    }

    // --- the death-burst on contact ---------------------------------------------------------------

    @Test
    void aDeathBurstAttackIsTheUnitsRingOnContact() {
        assertEquals(
                Optional.of(new EnemySpec.DeathBurst(8, 90, 4, true, true, 0.25)),
                unit("test-harrow", Difficulty.MEDIUM, 6, 38).deathBurst());
        assertEquals(
                90 * STAND_INS.difficulty().enemyBulletSpeed().of(Difficulty.EASY),
                unit("test-harrow", Difficulty.EASY, 6, 38)
                        .deathBurst()
                        .orElseThrow()
                        .speed(),
                1e-9,
                "the bullet-speed lever");
        assertEquals(
                100,
                unit("test-harrow", Difficulty.HARD, 6, 38)
                        .deathBurst()
                        .orElseThrow()
                        .speed(),
                "hard's authored ring speed");
        assertTrue(unit("test-harrow", Difficulty.MEDIUM, 6, 38).gun().isEmpty(), "a death-burst is not a gun");
        // An Act 1–5 puff (the Whirl Seed's hard hook) keeps the old rule: clear of the ship, no grace.
        EnemySpec.DeathBurst puff = SimSpecs.enemy(CONTENT, "whirl-seed", Difficulty.HARD, Optional.empty())
                .deathBurst()
                .orElseThrow();
        assertFalse(puff.onContact());
        assertFalse(puff.between());
    }

    /** Flown: the shot-down unit releases its ring of 8 {@code small} bullets. */
    @Test
    void aShotDownRammerReleasesItsRingOnTheRange() {
        EnemySpec harrow = unit("test-harrow", Difficulty.MEDIUM, 6, 38).withTerrain(true);
        Sortie sortie = range(ActThreeArsenalFlightTest.at(SHIP_X, harrow));
        boolean seen = false;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Command.FIRE.bit());
            seen |= ActThreeArsenalFlightTest.find(sortie, harrow).isPresent();
            if (seen && ActThreeArsenalFlightTest.find(sortie, harrow).isEmpty()) {
                break;
            }
        }
        assertTrue(seen && ActThreeArsenalFlightTest.find(sortie, harrow).isEmpty(), "shot down");
        assertEquals(8, sortie.bulletCount());
        for (int b = 0; b < sortie.bulletCount(); b++) {
            assertEquals(4, sortie.bullet(b).damage());
        }
    }

    // --- the dome ---------------------------------------------------------------------------------

    @Test
    void theDomeTakesItsNumbersAndItsPlacementsCrown() {
        EnemySpec.Dome medium =
                unit("test-pylon", Difficulty.MEDIUM, 6, 41).dome().orElseThrow();
        assertEquals(new EnemySpec.Dome(110, 140, Math.PI, 2.0, 0.3), medium, "straight up the screen by default");
        assertEquals(
                95,
                unit("test-pylon", Difficulty.EASY, 6, 41).dome().orElseThrow().radius());
        EnemySpec.Dome hard = unit("test-pylon", Difficulty.HARD, 6, 41).dome().orElseThrow();
        assertEquals(125, hard.radius());
        assertEquals(1.0, hard.breakerDropSeconds());
        // A placed pylon turned by its nest's crown (90: the dome to the pylon's right).
        Content placed = content(LEVEL_33, "ground_targets:", """
                ground_targets:
                  - {target: Test pylon, section: 2, enemy: test-pylon, count: 1, crown: 90, at: [[33.5, 200]]}""");
        LevelScript level = SimSpecs.level(placed, placed.levelKey(33).orElseThrow(), Difficulty.MEDIUM);
        EnemySpec.Dome turned = level.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals("test-pylon"))
                .findFirst()
                .orElseThrow()
                .enemy()
                .dome()
                .orElseThrow();
        assertEquals(1.5 * Math.PI, turned.bearingRadians(), 1e-12);
        assertEquals(200 + 140, turned.centreX(200), 1e-9, "the dome's centre right of the pylon");
    }

    /** Flown: a turret under a standing pylon's dome takes nothing; without the pylon it is hit. */
    @Test
    void aCoveredTurretTakesNoDamageOnTheRange() {
        EnemySpec pylon = unit("test-pylon", Difficulty.MEDIUM, 6, 41);
        EnemySpec turret = unit("test-turret", Difficulty.MEDIUM, 6, 41).withGun(Optional.empty());
        // The turret 140 px up the screen from the pylon (it enters that much later) and 60 px right: under the dome.
        GroundUnit covered = new GroundUnit(140 / RANGE_SCROLL, SHIP_X, turret, -1);
        Sortie sortie = range(new GroundUnit(0, SHIP_X - 60, pylon, -1), covered);
        boolean sheen = false;
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(Command.FIRE.bit());
            Optional<Enemy> unit = ActThreeArsenalFlightTest.find(sortie, turret);
            if (unit.isPresent()) {
                sheen |= unit.get().covered();
                assertEquals(turret.hp(), unit.get().hp(), "covered: no damage");
            }
        }
        assertTrue(sheen, "covered by the dome");
        Sortie control = range(new GroundUnit(140 / RANGE_SCROLL, SHIP_X, turret, -1));
        for (int i = 0; i < SimStep.ticks(14); i++) {
            control.step(Command.FIRE.bit());
        }
        Optional<Enemy> bare = ActThreeArsenalFlightTest.find(control, turret);
        assertTrue(bare.isEmpty() || bare.get().hp() < turret.hp(), "without the pylon it is hit");
    }

    // --- the spoofer's flag -----------------------------------------------------------------------

    @Test
    void theAscendancysTurretsAreSpoofable() {
        for (String slug : List.of("sam-nest", "sentinel-tower", "rail-bunker", "warden-tank", "strider")) {
            assertTrue(CONTENT.enemy(slug).isSpoofable(), slug);
            assertTrue(SimSpecs.enemy(CONTENT, slug, Difficulty.MEDIUM, Optional.empty())
                    .spoofable());
        }
        for (String slug : List.of("talon", "gilded-gunship", "hornet", "skitter", "mantis")) {
            assertFalse(SimSpecs.enemy(CONTENT, slug, Difficulty.MEDIUM, Optional.empty())
                    .spoofable());
        }
        assertTrue(unit("test-turret", Difficulty.MEDIUM, 6, 39).spoofable());
        SystemsData.Utility spoofer = CONTENT.systems().utility().stream()
                .filter(module -> module.name().equals("Ascendancy IFF spoofer"))
                .findFirst()
                .orElseThrow();
        assertEquals(Optional.of(new SystemsData.Spoof(0.5)), spoofer.spoof());
        assertEquals(39, CONTENT.player().firstLevel(spoofer.available()));
        assertEquals(2, spoofer.draw());
    }

    // --- the three-way war ------------------------------------------------------------------------

    /**
     * Level 30 with a hunter/prey pair (a stand-in: the Talon line abreast at t=10 tagged as prey, the Skitter snake
     * at t=16 hunting it): the prey link reaches the simulation's wave and its units, flown into the level.
     */
    @Test
    void aHuntingWaveHuntsItsPreyWavesTag() {
        Content war = content(
                LEVEL_30,
                "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130}",
                "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130, tag: prey-wing}",
                LEVEL_30,
                "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right}",
                "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right, hunts: prey-wing}");
        String key = war.levelKey(30).orElseThrow();
        LevelScript level = SimSpecs.level(war, key, Difficulty.MEDIUM);
        List<WaveSpec> hunters = level.waves().stream()
                .filter(wave -> wave.hunts().equals("prey-wing"))
                .toList();
        assertEquals(1, hunters.size());
        assertEquals("skitter", hunters.getFirst().enemy().slug());
        assertTrue(level.waves().stream()
                .anyMatch(wave ->
                        wave.tag().equals("prey-wing") && wave.enemy().slug().equals("talon")));
        // Level 33's leeches keep their prey role, not a tag.
        LevelScript leeches = SimSpecs.level(CONTENT, CONTENT.levelKey(33).orElseThrow(), Difficulty.MEDIUM);
        assertTrue(leeches.waves().stream().allMatch(wave -> wave.hunts().isEmpty()));
        // Flown: the skitters enter hunting the talons' wave.
        vanguard.sim.Loadout loadout = ActThreeArsenalFlightTest.loadout(
                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1));
        Sortie sortie = new Sortie(
                1,
                loadout,
                level,
                SimSpecs.rules(war, key, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
        boolean hunting = false;
        for (int i = 0; i < SimStep.ticks(20) && !hunting; i++) {
            sortie.step(Command.NONE);
            for (int e = 0; e < sortie.enemyCount(); e++) {
                hunting |= sortie.enemy(e).hunts().equals("prey-wing");
            }
        }
        assertTrue(hunting, "a hunter flew");
    }

    // --- the checks -------------------------------------------------------------------------------

    @Test
    void theChecksCatchMisplacedActSixKeys() {
        // A mirror on a ground unit; spoofable on a Vrell unit; a regrowth hook without a regrowth.
        assertProblem(
                problems(Map.of(TURRET, TURRET_YAML.replace("spoofable: true", "spoofable: true\n" + """
                        mirror: {arc: 120, heat: 130, turn: 60, down: 3.0, regrow: 0.4, gap: 0.15, cool: 0.5, cool_delay: 0.3, fire_within: 30, bullet: small, speed: 110}"""))),
                "a mirror is a single air unit's");
        assertProblem(
                problems(Map.of(), "enemies/air/skitter/data.yaml", "bounty: 5", "spoofable: true\nbounty: 5"),
                "never aircraft, bosses or the Vrell");
        List<String> skitter = problems(
                Map.of(),
                "enemies/air/skitter/data.yaml",
                "first_level: 1",
                "first_level: 1\ndifficulty:\n  hard: {regrowth: {window: 4}}");
        assertProblem(skitter, "changes what the unit does not have");
        // A death-burst attack beside a hook's death_burst; two rings.
        assertProblem(
                problems(Map.of(
                        HARROW,
                        HARROW_YAML.replace(
                                "hard: {attacks: {ring: {speed: 100}}}",
                                "hard: {death_burst: {count: 3, speed: 90, bullet: small}}"))),
                "changes it by attacks.<name>");
        // A grace without on_contact.
        assertThrows(
                ContentException.class,
                () -> ContentLoader.load(files(Map.of(HARROW, HARROW_YAML.replace("on_contact: true, ", "")))));
        // A hunting wave naming no tag; Ascendancy hunters; a crown on a nest without a dome.
        assertProblem(
                problems(
                        Map.of(),
                        LEVEL_30,
                        "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right}",
                        "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right, hunts: nobody}"),
                "it names no other wave's tag");
        assertProblem(
                problems(
                        Map.of(),
                        LEVEL_30,
                        "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130}",
                        "{t: 10, formation: line abreast, enemy: talon, count: 4, from: front, speed: 130, hunts: rook}",
                        LEVEL_30,
                        "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right}",
                        "{t: 16, formation: snake, enemy: skitter, count: 6, from: front, edge: right, tag: rook}"),
                "the Vrell hunt");
        assertProblem(
                problems(
                        Map.of(),
                        LEVEL_33,
                        "ground_targets:",
                        "ground_targets:\n  - {target: Turned, section: 2, enemy: test-turret, count: 1, crown: 90, at: [[33.5, 200]]}"),
                "only a nest of a unit with a dome turns its crown");
        // The shop's core lists come in their order: the Crucible after the Mk VI is an error.
        assertProblem(
                problems(Map.of(), "player/generator/data.yaml", "available: L39", "available: L45"),
                "comes into the shop before the model above it");
    }

    // --- the Act 6 equipment rows -----------------------------------------------------------------

    @Test
    void actSixsEquipmentIsInTheShopOneItemAVisit() {
        WeaponData arc = CONTENT.weapon("plasma-arc");
        assertEquals(36, arc.unlock());
        assertEquals(WeaponData.CHAIN, arc.hits());
        assertEquals(Optional.of(0.75), arc.falloff());
        assertEquals(
                List.of(3, 4, 4, 5, 6),
                arc.levels().stream().map(level -> level.chain().orElseThrow()).toList());
        assertEquals(69.375, arc.volleyDps(arc.levels().getFirst()), 1e-9);
        assertFalse(SimSpecs.flies(CONTENT, "plasma-arc"), "the CHAIN delivery is mapped in step A2b");
        ShieldData.Model reflex = CONTENT.shields().models().getLast();
        assertEquals("Reflex Shield", reflex.name());
        assertEquals(37, CONTENT.player().firstLevel(reflex.available()));
        assertEquals(Optional.of(new ShieldData.Reflect(0.75, 0.5, 640, 2)), reflex.reflect());
        ArmourData.Plating composite = CONTENT.armour().plating().get(5);
        assertEquals("Composite V", composite.name());
        assertEquals(38, CONTENT.player().firstLevel(composite.available()));
        GeneratorData.Model crucible = CONTENT.generators().models().get(5);
        assertEquals("Mk V+ \"Crucible\"", crucible.name());
        assertEquals(25, crucible.output());
        assertEquals(24000, crucible.price());
        assertEquals(39, CONTENT.player().firstLevel(crucible.available()));
        assertEquals(
                "Mk VI \"Choir Core\"", CONTENT.generators().models().get(6).name());
    }
}
