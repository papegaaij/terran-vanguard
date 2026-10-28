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
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.PowerState;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialLimits;
import vanguard.sim.SpecialSpec;
import vanguard.sim.WeaponSpec;

/**
 * M9 part A, step A2 (m9a-gaps.md §1 #1–13, content side): Act 5's keys loaded, checked and mapped for the simulation,
 * on fixtures where no Act 5 unit or level has its data yet: the Tesla Coil Pod's arc and the Swarm Tail's rear-first
 * missiles from their data, a stand-in Talon, Gunship, Rail Bunker and SAM Nest (the hex shield, the {@code
 * laser-line}, the homing diamond), the vacuum medium and the underground setting on a built level, the enemy channel
 * and the power budget under a drain on a test range.
 */
class BeltKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String TESLA = "tesla-coil-pod";
    private static final String SWARM = "swarm-tail";
    private static final double SHIP_X = PlayField.WIDTH / 2.0;

    private static final String TALON = "enemies/air/test-talon/data.yaml";
    private static final String GUNSHIP = "enemies/air/test-gunship/data.yaml";
    private static final String BUNKER = "enemies/ground/test-bunker/data.yaml";
    private static final String NEST = "enemies/air/test-nest/data.yaml";

    /** A stand-in Talon (the approved stat block's numbers: small, a shield of 2). */
    private static final String TALON_YAML = """
            name: Test Talon
            faction: Unmarked
            layer: air
            tier: small
            size: [40, 36]
            hitbox: [28, 24]
            parts: single
            orientation: 16 angles
            hp: 16
            armour: none
            speed: 200
            machine: true
            shield: 2
            movement:
              hover: {seconds: 10, y: [380, 460], drift: 12}
            attacks: []
            formations:
              - {name: V-wing, size: [5, 7]}
            weak_points: []
            drops: []
            traits: [spread, homing]
            bounty: 12
            first_level: 29
            """;

    /** A stand-in Gilded Gunship (medium, a shield of 12): the act HP factor reaches its shield. */
    private static final String GUNSHIP_YAML = TALON_YAML
            .replace("Test Talon", "Test Gunship")
            .replace("tier: small", "tier: medium")
            .replace("hp: 16", "hp: 88")
            .replace("shield: 2", "shield: 12")
            .replace("first_level: 29", "first_level: 30");

    /** A stand-in Rail Bunker (its laser-line with the difficulty hooks). */
    private static final String BUNKER_YAML = """
            name: Test Bunker
            faction: Unmarked
            layer: ground
            tier: medium
            size: [48, 48]
            hitbox: [40, 40]
            parts: single
            orientation: 32 angles
            hp: 60
            armour: none
            speed: 0
            machine: true
            movement:
              terrain: {}
            attacks:
              - pattern: laser-line
                name: rail
                bullet: heavy
                interval: 4.5
                first_shot_delay: 1.5
                line: {telegraph: 1.0, live: 0.2, band: 8, hold_within: 100}
            formations:
              - {name: turret nest, size: [1, 2]}
            weak_points: []
            drops: []
            traits: [anti-ground]
            bounty: 30
            first_level: 33
            difficulty:
              easy: {telegraph: 1.2, attacks: {rail: {interval: 6}}}
              hard: {telegraph: 0.8, attacks: {rail: {interval: 3.6}}}
            """;

    /**
     * A stand-in for the SAM Nest's missiles: the seekable homing diamond on a unit that fires its homing missiles along
     * its facing while it holds (the Depth Hunter's rule; the SAM Nest's turret on the ground is its part's).
     */
    private static final String NEST_YAML = """
            name: Test Nest
            faction: Unmarked
            layer: air
            tier: medium
            size: [48, 48]
            hitbox: [40, 40]
            parts: single
            orientation: 16 angles
            hp: 60
            armour: none
            speed: 80
            machine: true
            movement:
              hover: {seconds: 10, y: [380, 460], drift: 12}
            attacks:
              - pattern: aimed
                name: salvo
                bullet: medium
                interval: 4.5
                speed: 110
                aim: facing
                homing: {turn_rate: 80, life: 4, hp: 2, most: 8, hatch: 0.4, look: diamond, seekable: true}
            formations:
              - {name: column, size: [1, 2]}
            weak_points: []
            drops: []
            traits: [homing]
            bounty: 25
            first_level: 29
            """;

    /** The design tree with the stand-ins added and the given edits ({@code what} → {@code with}) to its files. */
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
        files.add(new DataFile(TALON, TALON_YAML));
        files.add(new DataFile(GUNSHIP, GUNSHIP_YAML));
        files.add(new DataFile(BUNKER, BUNKER_YAML));
        files.add(new DataFile(NEST, NEST_YAML));
        return files;
    }

    private static Content content(String... edits) {
        return ContentLoader.load(files(edits));
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    // --- the weapons ----------------------------------------------------------------------------

    @Test
    void theTeslaCoilPodArcsFromItsPodWithinItsReachAndBreaksShields() {
        WeaponData data = CONTENT.weapon(TESLA);
        assertTrue(SimSpecs.flies(CONTENT, TESLA));
        assertTrue(data.shieldBreaker());
        assertEquals(29, data.unlock());
        double[] reach = {140, 150, 160, 170, 180, 190};
        double[] sparks = {24, 24, 28, 32, 36, 40};
        double[] rate = {4, 4, 5, 5, 5, 6};
        double[] damage = {3, 4.25, 4.6, 6.2, 8, 8};
        for (int level = 1; level <= 6; level++) {
            for (Armament.Slot slot : List.of(Armament.Slot.LEFT_WING, Armament.Slot.RIGHT_WING)) {
                WeaponSpec arc = WeaponRules.weapon(CONTENT, slot, TESLA, level);
                assertEquals(WeaponSpec.Delivery.ARC, arc.delivery());
                assertEquals(reach[level - 1], arc.range(), 1e-9);
                assertEquals(Math.toRadians(120), arc.coneHalfAngle(), 1e-9, "a 240° arc that opens forward");
                assertEquals(sparks[level - 1], arc.blast(), 1e-9);
                assertEquals(0.5, arc.splash(), 1e-9);
                assertEquals(rate[level - 1], arc.rate(), 1e-9);
                assertEquals(damage[level - 1], arc.damage(), 1e-9);
                assertTrue(arc.shieldBreaker());
                assertFalse(arc.antiGround(), "the arc glances off hardened ground");
                assertEquals(1, arc.muzzles().size());
            }
        }
        // From the wing mounts, mirrored.
        WeaponSpec left = WeaponRules.weapon(CONTENT, Armament.Slot.LEFT_WING, TESLA, 1);
        WeaponSpec right = WeaponRules.weapon(CONTENT, Armament.Slot.RIGHT_WING, TESLA, 1);
        assertEquals(-left.muzzles().getFirst().dx(), right.muzzles().getFirst().dx(), 1e-9);
        assertTrue(right.muzzles().getFirst().dx() > 0);
    }

    @Test
    void theSwarmTailIsARearFirstHomingMissileLaunchedBackwards() {
        WeaponData data = CONTENT.weapon(SWARM);
        assertEquals(30, data.unlock());
        assertEquals(Optional.of(WeaponData.Pick.REAR_FIRST), data.pick());
        WeaponSpec missile = WeaponRules.weapon(CONTENT, Armament.Slot.REAR, SWARM, 1);
        assertEquals(WeaponSpec.Delivery.HOMING, missile.delivery());
        assertEquals(WeaponSpec.Seek.REAR_FIRST, missile.seek());
        assertTrue(missile.flanking());
        assertEquals(320, missile.range(), 1e-9);
        assertEquals(260, missile.speed(), 1e-9);
        assertEquals(480, missile.endSpeed(), 1e-9);
        assertEquals(0.3, missile.accelSeconds(), 1e-9);
        assertEquals(1.6, missile.lifetimeSeconds(), 1e-9);
        assertEquals(Math.PI, Math.abs(missile.muzzles().getFirst().angle()), 1e-9, "fired backwards");
        assertFalse(missile.shieldBreaker());
        assertEquals(
                List.of(1, 1, 2, 2, 3, 3),
                java.util.stream.IntStream.rangeClosed(1, 6)
                        .mapToObj(level -> WeaponRules.weapon(CONTENT, Armament.Slot.REAR, SWARM, level)
                                .muzzles()
                                .size())
                        .toList());
        // The Targeting computer's +20 % turn reaches it.
        assertEquals(
                missile.turnRate() * 1.2,
                WeaponRules.weapon(CONTENT, Armament.Slot.REAR, SWARM, 1, 0.2).turnRate(),
                1e-9);
        // Every other weapon keeps the nearest pick and no shield-breaker.
        for (String slug : CONTENT.weapons().keySet()) {
            if (!slug.equals(SWARM)
                    && !slug.equals(TESLA)
                    && SimSpecs.flies(CONTENT, slug)
                    && !CONTENT.weapon(slug).deflector()) {
                Armament.Slot slot =
                        switch (CONTENT.weapon(slug).slot()) {
                            case FRONT -> Armament.Slot.FRONT;
                            case REAR -> Armament.Slot.REAR;
                            case WING -> Armament.Slot.LEFT_WING;
                        };
                WeaponSpec spec = WeaponRules.weapon(CONTENT, slot, slug, 1);
                assertEquals(WeaponSpec.Seek.NEAREST, spec.seek(), slug);
                assertFalse(spec.shieldBreaker(), slug);
            }
        }
    }

    /**
     * The step's measure (the A1 note: at the draft's 240°/s and 480 px/s a missile launched backwards circled a target
     * ahead and never struck it): with the data's turn rates the L1 missile strikes a target ahead, beside and behind at
     * about its DPS (at least 0.6 of it in each band of its seek radius).
     */
    @Test
    void theSwarmTailStrikesATargetAheadBesideAndBehind() {
        WeaponSpec missile = WeaponRules.weapon(CONTENT, Armament.Slot.REAR, SWARM, 1);
        double nominal = missile.rate() * missile.damage();
        for (double dx : new double[] {0, 120}) {
            EnemySpec dummy = EnemySpec.of("dummy", 1e6, new Hitbox(36, 36), Layer.AIR, 0, false, 0, 0)
                    .withTerrain(true);
            Loadout loadout =
                    ActThreeArsenalFlightTest.loadout(new SimSpecs.FittedWeapon(Armament.Slot.REAR, SWARM, 1));
            Sortie sortie = ActThreeArsenalFlightTest.range(loadout, true, new GroundUnit(0, SHIP_X + dx, dummy, -1));
            double[] lost = new double[4];
            double[] seconds = new double[4];
            double hp = dummy.hp();
            for (int i = 0; i < SimStep.ticks(30); i++) {
                sortie.step(Command.FIRE.bit());
                Optional<Enemy> on = ActThreeArsenalFlightTest.find(sortie, dummy);
                if (on.isEmpty()) {
                    continue;
                }
                double above = on.get().renderY(1) - sortie.ship().y();
                int band = above > 300 ? -1 : above > 150 ? 0 : above > 0 ? 1 : above > -150 ? 2 : 3;
                if (band >= 0) {
                    lost[band] += hp - on.get().hp();
                    seconds[band] += SimStep.SECONDS;
                }
                hp = on.get().hp();
            }
            for (int band = 0; band < 3; band++) {
                double dps = lost[band] / seconds[band];
                assertTrue(
                        dps >= 0.6 * nominal, "dx " + dx + ", band " + band + ": " + dps + " of " + nominal + " DPS");
            }
        }
    }

    @Test
    void theHelixIsInTheShopFromTheL35Visit() {
        GeneratorData.Model helix = CONTENT.generators().models().get(4);
        assertEquals("Mk V \"Helix\"", helix.name());
        assertEquals(35, CONTENT.player().firstLevel(helix.available()));
        assertEquals(new GeneratorData.Drain(2, 6, 0.5), CONTENT.generators().drain());
    }

    // --- the hex shield, the laser line, the homing diamond ---------------------------------------

    @Test
    void aShieldTakesTheDifficultyLeverAndTheActFactorButNeverTheBeamEra() {
        Content content = content();
        assertEquals(
                new EnemyBasisData.Shields(2.0, 0.5, 3.0), content.enemyBasis().shields());
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec talon = UnitRules.enemy(content, "test-talon", difficulty, Optional.empty(), 5, 29);
            double lever = content.difficulty().enemyHp(1, difficulty);
            assertEquals(
                    Optional.of(new EnemySpec.Shield(content.difficulty().enemyHp(2, difficulty), 2.0, 0.5, 3.0)),
                    talon.shield(),
                    difficulty.name());
            assertEquals(content.difficulty().enemyHp(32, difficulty), talon.hp(), 1e-9, "the beam era's ×2 on HP");
            assertTrue(lever > 0);
        }
        // A returning medium unit: the act factor (the reference DPS at L31 over L30's) on its shield too.
        EnemySpec gunship = UnitRules.enemy(content, "test-gunship", Difficulty.MEDIUM, Optional.empty(), 5, 31);
        assertEquals(
                content.difficulty().enemyHp(12 * 128 / 124.0, Difficulty.MEDIUM),
                gunship.shield().orElseThrow().hp(),
                1e-9);
        // A small unit returning keeps its shield (no act factor below medium).
        assertEquals(
                content.difficulty().enemyHp(2, Difficulty.MEDIUM),
                UnitRules.enemy(content, "test-talon", Difficulty.MEDIUM, Optional.empty(), 5, 33)
                        .shield()
                        .orElseThrow()
                        .hp(),
                1e-9);
        // Units without one: none (every unit of Acts 1–4; M9 part B: Act 5's own units from Level 29 on).
        for (String slug : CONTENT.enemies().keySet()) {
            if (CONTENT.enemy(slug).firstLevel() < BeltChecks.firstLevel(CONTENT)) {
                assertEquals(Optional.empty(), CONTENT.enemy(slug).shield(), slug);
            }
        }
    }

    @Test
    void aLaserLineLocksItsTelegraphLiveBandAndHoldByDifficulty() {
        Content content = content();
        double heavy = UnitRules.bulletDamage(content, "heavy");
        assertEquals(
                Optional.of(new EnemySpec.LaserLine(1.0, 0.2, 8, heavy, 4.5, 1.5, 100)),
                UnitRules.enemy(content, "test-bunker", Difficulty.MEDIUM, Optional.empty(), 5, 33)
                        .laserLine());
        assertEquals(
                Optional.of(new EnemySpec.LaserLine(1.2, 0.2, 8, heavy, 6, 1.5, 100)),
                UnitRules.enemy(content, "test-bunker", Difficulty.EASY, Optional.empty(), 5, 33)
                        .laserLine());
        assertEquals(
                Optional.of(new EnemySpec.LaserLine(0.8, 0.2, 8, heavy, 3.6, 1.5, 100)),
                UnitRules.enemy(content, "test-bunker", Difficulty.HARD, Optional.empty(), 5, 33)
                        .laserLine());
        assertEquals(
                Optional.empty(),
                UnitRules.enemy(content, "test-bunker", Difficulty.MEDIUM, Optional.empty(), 5, 33)
                        .gun(),
                "a laser line is not a gun");
    }

    @Test
    void theSamNestsMissilesAreSeekableHomingDiamonds() {
        Content content = content();
        EnemyData nest = content.enemy("test-nest");
        assertEquals(
                Optional.of(EnemyData.Homing.DIAMOND),
                nest.attacks().getFirst().homing().orElseThrow().look());
        EnemyGun gun = UnitRules.enemy(content, "test-nest", Difficulty.MEDIUM, Optional.empty(), 5, 29)
                .gun()
                .orElseThrow();
        assertTrue(gun.homing().orElseThrow().seekable());
        // The Act 4 missiles stay unseekable (the Depth Hunter's torpedoes, the walker's pods).
        for (String slug : CONTENT.enemies().keySet()) {
            if (CONTENT.enemy(slug).firstLevel() >= BeltChecks.firstLevel(CONTENT)) {
                continue; // M9 part B: Act 5's own units (the SAM Nest's diamonds)
            }
            for (EnemyData.Attack attack : CONTENT.enemy(slug).attacks()) {
                attack.homing().ifPresent(homing -> {
                    assertEquals(Optional.empty(), homing.seekable(), slug);
                    assertEquals(Optional.empty(), homing.look(), slug);
                });
            }
        }
    }

    @Test
    void theChecksKeepTheAct5KeysInAct5() {
        String needler = "enemies/air/needler/data.yaml";
        assertTrue(problems(needler, "\nhp: ", "\nshield: 2\nhp: ").stream()
                .anyMatch(problem -> problem.contains("enemy shields come in Act 5")));
        assertTrue(problems("enemies/data.yaml", "  delay: 2.0", "  delay: -1").stream()
                .anyMatch(problem -> problem.contains("shields.delay")));
        // A laser line's interval holds its telegraph and its live time.
        assertTrue(assertThrows(
                        ContentException.class,
                        () -> ContentLoader.load(replaced(BUNKER, BUNKER_YAML.replace("interval: 4.5", "interval: 1"))))
                .problems()
                .stream()
                .anyMatch(problem -> problem.contains("interval holds its telegraph")));
        // The line's telegraph is never under the vocabulary's 0.8 s.
        assertTrue(assertThrows(
                        ContentException.class,
                        () -> ContentLoader.load(
                                replaced(BUNKER, BUNKER_YAML.replace("telegraph: 1.0", "telegraph: 0.5"))))
                .problems()
                .stream()
                .anyMatch(problem -> problem.contains("line.telegraph")));
    }

    /** The tree with the stand-ins, {@code path}'s text replaced by {@code text}. */
    private static List<DataFile> replaced(String path, String text) {
        return files().stream()
                .map(file -> file.path().equals(path) ? new DataFile(path, text) : file)
                .toList();
    }

    // --- vacuum and the underground setting ------------------------------------------------------

    private static final String LEVEL_21_KEY = "act-3-red-dust/level-21-dust-colossus";

    @Test
    void vacuumIsDryAndFliesAsAir() {
        assertEquals(
                Optional.of(SpecialsData.Setting.DRY), SpecialsData.Setting.of(LevelData.Medium.VACUUM, false, false));
        assertEquals(
                List.of("Sonar Pulse"),
                CONTENT.specials().forbidden(LevelData.Medium.VACUUM, false, false, Optional.empty()));
        // Only from Act 5: a vacuum level before it is a problem.
        String level01 = "campaign/act-1-first-contact/level-01-break-at-dawn/data.yaml";
        assertTrue(problems(level01, "\nwaves:", "\nmedium: vacuum\nwaves:").stream()
                .anyMatch(problem -> problem.contains("vacuum only from Act 5")));
    }

    @Test
    void insideRockTheAirstrikeAndTheLanceReadInRock() {
        SpecialsData specials = CONTENT.specials();
        assertEquals(
                List.of("Airstrike", "Orbital Lance"), specials.forbids(Optional.of(SpecialsData.Setting.UNDERGROUND)));
        assertEquals(
                Optional.of("IN ROCK"), specials.reason("Airstrike", Optional.of(SpecialsData.Setting.UNDERGROUND)));
        assertEquals(
                Optional.of("UNDER ICE"), specials.reason("Airstrike", Optional.of(SpecialsData.Setting.UNDERWATER)));
        assertEquals(Optional.of("UNDER ICE"), specials.reason("Airstrike"), "the first limit's word as before");
        assertEquals(
                List.of("Airstrike", "Orbital Lance"),
                specials.forbidden(LevelData.Medium.AIR, false, true, Optional.empty()));
        // A built level made Act 5's for the fixture (its availability moved), its second section underground: the
        // simulation's list from that section on reads IN ROCK; the intel's reason word too.
        String player = "player/data.yaml";
        assertFalse(CONTENT.level(LEVEL_21_KEY).undergroundOf(0));
        Content rock = content(
                player,
                "act 5: 29",
                "act 5: 21",
                "campaign/" + LEVEL_21_KEY + "/data.yaml",
                "\nsections:",
                "\nunderground: true\nsections:");
        LevelData under = rock.level(LEVEL_21_KEY);
        assertTrue(under.undergroundOf(0));
        assertEquals(List.of("Airstrike", "Orbital Lance"), under.forbiddenSpecials(0, rock.specials()));
        assertEquals(Optional.of("IN ROCK"), under.forbiddenReason(0, rock.specials(), "Airstrike"));
        assertEquals(Optional.empty(), under.forbiddenReason(0, rock.specials(), "EMP Burst"));
        SpecialLimits limits = SimSpecs.specialLimits(rock, under);
        assertEquals(1, limits.switches().size());
        SpecialLimits.Switch from = limits.switches().getFirst();
        assertEquals(0, from.atSeconds());
        assertEquals(
                Optional.of(java.util.Set.of(SpecialSpec.Kind.AIRSTRIKE, SpecialSpec.Kind.LANCE)), from.forbidden());
        assertEquals(SpecialLimits.Reason.IN_ROCK, from.reason());
        // The level as built: no list at all (its script as before).
        assertEquals(SpecialLimits.NONE, SimSpecs.specialLimits(CONTENT, CONTENT.level(LEVEL_21_KEY)));
    }

    // --- the enemy channel ----------------------------------------------------------------------

    @Test
    void vorneAndHarkerSpeakOnTheEnemyChannel() {
        VoiceData voices = CONTENT.voices();
        assertEquals(Channel.ENEMY, voices.channelOf("Vorne"));
        assertEquals(Channel.ENEMY, voices.channelOf("Harker"));
        assertEquals(Channel.FRIENDLY, voices.channelOf("Okafor"));
        assertEquals(Channel.FRIENDLY, voices.channelOf("Nobody"));
        // Every line and page of Acts 1–4 is friendly (M9 part B: Level 29's Harker is not).
        for (var entry : CONTENT.levels().entrySet()) {
            if (Content.levelNumber(entry.getKey()) >= BeltChecks.firstLevel(CONTENT)) {
                continue;
            }
            LevelData level = entry.getValue();
            level.radio().forEach(cue -> assertEquals(Channel.FRIENDLY, cue.channel(voices), cue.line()));
            level.briefing().pages().forEach(page -> assertEquals(Channel.FRIENDLY, page.onChannel(), page.line()));
        }
        BriefingPage page = new BriefingPage(
                "Vorne", "Citizens of the belt.", Optional.empty(), Optional.empty(), Optional.of(Channel.ENEMY));
        assertEquals(Channel.ENEMY, page.onChannel());
    }

    // --- the power budget and the drain ---------------------------------------------------------

    /**
     * The generator's hand plan's power table on a test range, the budget built by {@link SimSpecs#power}: the typical
     * plan at L29 (18 of 20) loses only its regeneration to two leeches; at L32–34 (20 of 20) three cut its fire to
     * 0.875; at L30 (19.5) to 14 ÷ 15.5; a small generator (15 of 16, Shield Mk II) to 10 ÷ 12.
     */
    @Test
    void theDrainFollowsTheHandPlansTable() {
        String mk3 = CONTENT.shields().models().get(2).name();
        String mk2 = CONTENT.shields().models().get(1).name();
        assertEquals(4, CONTENT.shields().models().get(2).draw());
        assertEquals(3, CONTENT.shields().models().get(1).draw());
        check(20, 18, mk3, 2, true, 1);
        check(20, 18, mk3, 3, true, 1);
        check(20, 19.5, mk3, 3, true, 14 / 15.5);
        check(20, 20, mk3, 2, true, 1);
        check(20, 20, mk3, 3, true, 0.875);
        check(20, 20, mk3, 4, true, 0.875);
        check(22, 21, mk3, 3, true, 16 / 17.0);
        check(16, 15, mk2, 3, true, 10 / 12.0);
        check(20, 16, mk3, 2, false, 1);
    }

    private static void check(
            double output, double load, String shield, int drainers, boolean regenStopped, double factor) {
        Loadout loadout = ActThreeArsenalFlightTest.loadout(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 1))
                .withPower(SimSpecs.power(CONTENT, output, load, shield, Difficulty.MEDIUM));
        Sortie sortie = ActThreeArsenalFlightTest.range(loadout, true);
        for (int i = 0; i < drainers; i++) {
            sortie.drain(+1);
        }
        sortie.step(Command.NONE);
        PowerState power = sortie.power().orElseThrow();
        String at = output + " MW, load " + load + ", " + drainers + " drainers";
        assertEquals(Math.min(drainers * 2, 6), power.drainedMw(), 1e-9, at);
        assertEquals(regenStopped, power.regenStopped(), at);
        assertEquals(factor, power.factor(), 1e-9, at);
    }

    @Test
    void theBudgetCarriesTheShieldsRegenWithoutTheSpareBonus() {
        ShieldData.Model mk3 = CONTENT.shields().models().get(2);
        Loadout.Power budget = SimSpecs.power(CONTENT, 20, 18, mk3.name(), Difficulty.HARD);
        assertEquals(
                new Loadout.Power(
                        20,
                        18,
                        4,
                        mk3.regen() * CONTENT.difficulty().shieldRegen().of(Difficulty.HARD),
                        0.1,
                        0.5,
                        new Loadout.Drain(2, 6, 0.5)),
                budget);
        // A load below the shield's own draw (a debug fit) still holds it.
        assertEquals(
                4, SimSpecs.power(CONTENT, 20, 1, mk3.name(), Difficulty.MEDIUM).load());
    }
}
