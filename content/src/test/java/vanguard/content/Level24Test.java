package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Range;
import vanguard.sim.RisePhase;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part D, step D3: Level 24 Abyssal Plain from its data (design/campaign/act-4-deep-water/level-24-abyssal-plain;
 * design/enemies/naval/abyss-ray, barbfin; design/enemies/ground/spawning-reef): its count, its four sections under
 * water from the start, the quiet windows, the Abyss Ray (its rise, its fans in a burst by difficulty, one on the screen
 * at a time, its four Barbfins held until its rise, the overdrive and the shield cell it carries), the Barbfin's hold
 * above the ship, the spawning reefs (their fry, which draw their ram as they leave the iris, and the REEFS secondary by
 * difficulty), the radio ({@code first-ray} at the first shadow), and whole runs of the autopilot with the balance
 * plan's fit ({@link AbyssPlay}: the shadow, the Barbfins) with and without Rook. The pacing, the floor, the density,
 * the beam era's HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link
 * DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level24Test {
    static final String LEVEL = "act-4-deep-water/level-24-abyssal-plain";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The three Rays' waves. */
    static final List<Double> RAYS = List.of(58.0, 112.0, 150.0);

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 24 ({@code "24"}: the Pressure hull and the left wing's Micro-missile Pod to L3 on
     * Level 23's): the Harpoon Torpedoes L3 in front, the Micro-missile Pods L3 (left) and L2 (right), the Depth Charges
     * L3, the Sonar Pulse, the Mk II shield, Composite III, and Rook's Autocannon.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "autocannon",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "depth-charges", 3)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(3).name(),
                        0,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "Sonar Pulse", 1))
                .withPressureHull();
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty, boolean invulnerable) {
        Loadout loadout = planLoadout(content, difficulty);
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /**
     * The README: 265 enemies at medium, the waves' 255 (Glassfin 120, Barbfin 59, Driftjelly 32, Lamprey 21, Scuttler
     * 12, Eel Swarm 8, Abyss Ray 3) and the ground targets' 10 (4 spawning reefs, 6 Reef Spitters); the wreck
     * <i>Meridian</i> is a trigger with its secret.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(255, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        String[] slugs = {"glassfin", "barbfin", "driftjelly", "lamprey", "scuttler", "eel-swarm", "abyss-ray"};
        int[] counts = {120, 59, 32, 21, 12, 8, 3};
        for (int i = 0; i < slugs.length; i++) {
            String slug = slugs[i];
            assertEquals(
                    counts[i],
                    level.waves().stream()
                            .filter(wave -> wave.enemy().slug().equals(slug))
                            .mapToInt(WaveSpec::count)
                            .sum(),
                    slug);
        }
        assertEquals(10, level.groundUnits().size());
        assertEquals(
                4,
                level.groundUnits().stream()
                        .filter(unit -> unit.enemy().slug().equals("spawning-reef"))
                        .count());
        assertEquals(1, level.secrets());
    }

    /**
     * Four sections, the whole level under water from its start (no waterline): the plain's edge at 100 px/s to 30 s,
     * the reef field at 80 to 95 s, the beacon line at 100 to 135 s, the Ray crossing at 90 to 172 s, each change of
     * speed eased over 2 s; under water the Airstrike and the Orbital Lance cannot reach and the Sonar Pulse is free.
     */
    @Test
    void theWholePlainIsUnderWater() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(30.0, 95.0, 135.0, 172.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(100.0, 80.0, 100.0, 90.0),
                sections.stream().map(Section::speed).toList());
        for (int i = 1; i < 4; i++) {
            assertEquals(2, sections.get(i).rampSeconds(), "section " + (i + 1));
        }
        MediumSpec medium = level.medium();
        assertEquals(List.of(new MediumSpec.Switch(0, true)), medium.switches());
        LevelData data = content.level(LEVEL);
        assertEquals(List.of(), data.waterlines());
        for (int i = 0; i < data.sections().size(); i++) {
            List<String> forbidden = data.forbiddenSpecials(i, content.specials());
            assertTrue(forbidden.containsAll(List.of("Airstrike", "Orbital Lance")), "section " + (i + 1));
            assertFalse(forbidden.contains("Sonar Pulse"), "section " + (i + 1));
        }
    }

    /**
     * The quiet windows: no other wave enters t=6–9.5 (the first Barbfins) nor 3 s before to 4 s after each Ray's wave
     * (t=55–62, 109–116, 147–154), on every difficulty.
     */
    @Test
    void theNewUnitsHaveTheirQuietWindows() {
        double[][] windows = {{6, 9.5}, {55, 62}, {109, 116}, {147, 154}};
        double[] own = {6, 58, 112, 150};
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                for (int w = 0; w < windows.length; w++) {
                    double t = wave.t();
                    assertFalse(
                            t >= windows[w][0] && t < windows[w][1] && t != own[w], difficulty + ": a wave at t=" + t);
                }
            }
        }
    }

    /**
     * The Abyss Ray: 210 HP (its own first level, by the HP lever), 130×90, entering on {@code sub} with its rise (a
     * 1.5 s shadow 200 px ahead of the ship, a 0.9 s rise, a 5 s glide at 70 px/s, a 0.9 s sink); its 9-way fan over
     * 110° (easy 7, hard 11) in a burst of three 1.2 s apart (easy two, hard four) from the glide's start, once a visit;
     * in each of its waves it comes first, its four Barbfins after it as its escorts.
     */
    @Test
    void theAbyssRayRisesAndFansInABurst() {
        int[] fan = {7, 9, 11};
        int[] burst = {2, 3, 4};
        double[] hp = {158, 210, 273};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> waves = SimSpecs.level(content, LEVEL, difficulty).waves();
            for (double t : RAYS) {
                List<WaveSpec> groups =
                        waves.stream().filter(wave -> wave.t() == t).toList();
                assertEquals(
                        List.of("abyss-ray", "barbfin"),
                        groups.stream().map(wave -> wave.enemy().slug()).toList());
                assertEquals(1, groups.getFirst().count());
                assertEquals(
                        content.difficulty().formationSize(4, difficulty),
                        groups.get(1).count(),
                        difficulty + " t=" + t);
                for (WaveSpec group : groups) {
                    assertEquals(WaveSpec.Formation.CARRIER_ESCORTS, group.formation());
                }
            }
            EnemySpec ray = waves.stream()
                    .filter(wave -> wave.t() == RAYS.getFirst())
                    .findFirst()
                    .orElseThrow()
                    .enemy();
            assertEquals(Layer.SUB, ray.layer());
            assertEquals(
                    new EnemySpec.Rise(1.5, 0.9, 5, 70, 0.9, 200), ray.rise().orElseThrow());
            assertEquals(hp[difficulty.ordinal()], ray.hp(), 0.5, difficulty.name());
            assertEquals(130, ray.hitbox().width());
            assertEquals(90, ray.hitbox().height());
            EnemyGun gun = ray.gun().orElseThrow();
            assertEquals(fan[difficulty.ordinal()], gun.fan(), difficulty.name());
            assertEquals(burst[difficulty.ordinal()], gun.burst(), difficulty.name());
            assertEquals(1.2, gun.burstGapSeconds());
            assertEquals(Math.toRadians(110), gun.spreadRadians(), 1e-12);
            assertEquals(0, gun.firstShotDelay());
            assertTrue(gun.intervalSeconds() > 1.5 + 0.9 + 5 + 0.9, "one salvo a visit: " + gun.intervalSeconds());
        }
    }

    /**
     * The Barbfin: 24 HP (12 by the beam era's ×2, gap check D5), held 9 s 90–200 px above the ship drifting 6 px/s;
     * its aimed needle every 2.4 s by the fire-rate lever (easy every 3 s, hard a 2-needle burst 0.12 s apart every 2.6
     * s: step D3, the approved 2.1 lost the plan's fit without Rook on hard), the first 0.9 s after it stops.
     */
    @Test
    void theBarbfinHoldsAboveTheShipAndSpitsNeedles() {
        double rate = content.difficulty().enemyFireRate().of(Difficulty.MEDIUM);
        double[] interval = {3, 2.4 / rate, 2.6};
        int[] burst = {1, 1, 2};
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec barbfin = wave(difficulty, 6).enemy();
            assertEquals("barbfin", barbfin.slug());
            assertEquals(Layer.AIR, barbfin.layer());
            EnemySpec.Hover hover = barbfin.hover().orElseThrow();
            assertEquals(new Range(9, 9), hover.seconds());
            assertEquals(new Range(90, 200), hover.aboveShip().orElseThrow());
            assertEquals(6, hover.drift());
            assertEquals(AbyssRules.ABOVE_SHIP_DEPTH, hover.depth());
            EnemyGun gun = barbfin.gun().orElseThrow();
            assertEquals(1, gun.fan());
            assertEquals(interval[difficulty.ordinal()], gun.intervalSeconds(), 1e-9, difficulty.name());
            assertEquals(burst[difficulty.ordinal()], gun.burst(), difficulty.name());
            assertEquals(0.9, gun.firstShotDelay());
            assertEquals(0.12, gun.burstGapSeconds());
        }
        assertEquals(24, wave(Difficulty.MEDIUM, 6).enemy().hp());
    }

    /**
     * The spawning reefs: four soft ground units (t=36, 48, 64, 80) releasing 4 Glassfin fry every 3.5 s (easy 3) over
     * 120° at 160 px/s, shut while the ship is within 96 px; each fry draws its ram as it leaves the iris (the
     * Glassfin's share by difficulty, its glint and its dive). REEFS: at least 3 of the 4 razed at medium, 2 on easy,
     * 3 on hard too (round 53 C.1, the user's "allow 1"); 100 credits.
     */
    @Test
    void theReefsBreedFryAndTheSecondaryCountsThem() {
        int[] count = {3, 4, 4};
        int[] allowed = {2, 1, 1};
        double[] share = {0.15, 0.25, 0.40};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<GroundUnit> reefs = level.groundUnits().stream()
                    .filter(unit -> unit.enemy().slug().equals("spawning-reef"))
                    .toList();
            assertEquals(
                    List.of(36.0, 48.0, 64.0, 80.0),
                    reefs.stream().map(GroundUnit::t).toList());
            EnemySpec reef = reefs.getFirst().enemy();
            assertEquals(Layer.GROUND, reef.layer());
            assertFalse(reef.hardened());
            EnemySpec.Spawner spawner = reef.spawner().orElseThrow();
            assertEquals(count[difficulty.ordinal()], spawner.count(), difficulty.name());
            assertEquals(3.5, spawner.everySeconds());
            assertEquals(96, spawner.shutWithin());
            assertEquals(160, spawner.speed());
            assertEquals(Math.toRadians(120), spawner.arcRadians(), 1e-12);
            assertEquals("glassfin", spawner.enemy().slug());
            assertEquals(
                    new EnemySpec.Ram(0, share[difficulty.ordinal()], 0.3, 0.5, 230),
                    spawner.enemy().flock().orElseThrow().ram().orElseThrow());
            Secondary secondary = level.objectives().secondary();
            assertEquals("spawning-reef", secondary.escapes());
            assertEquals(allowed[difficulty.ordinal()], secondary.escapesAllowed(), difficulty.name());
            assertEquals("REEFS", secondary.label());
            assertEquals(100, secondary.credits());
        }
    }

    /**
     * The reefs' fry draw their ram before they leave (step D3: with the school's 3 s on the field none did; they are
     * gone in about a second): flown at medium, invulnerable, some fry released by a reef glint and dive.
     */
    @Test
    void theReefsFryRamBeforeTheyLeave() {
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        Set<Integer> fry = new HashSet<>();
        Set<Integer> seen = new HashSet<>();
        Set<Integer> rammed = new HashSet<>();
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                if (!unit.spec().slug().equals("glassfin")) {
                    continue;
                }
                if (seen.add(unit.serial()) && nearAReef(sortie, unit)) {
                    fry.add(unit.serial());
                }
                if (fry.contains(unit.serial()) && (unit.ramGlinting() || unit.ramDiving())) {
                    rammed.add(unit.serial());
                }
            }
        }
        assertTrue(sortie.complete());
        assertTrue(fry.size() >= 4, "fry released: " + fry.size());
        assertFalse(rammed.isEmpty(), "of " + fry.size() + " fry none rammed");
    }

    private static boolean nearAReef(Sortie sortie, Enemy fry) {
        for (int k = 0; k < sortie.enemyCount(); k++) {
            Enemy reef = sortie.enemy(k);
            if (reef.spec().slug().equals("spawning-reef")
                    && Math.hypot(reef.renderX(1) - fry.renderX(1), reef.renderY(1) - fry.renderY(1)) < 80) {
                return true;
            }
        }
        return false;
    }

    /**
     * The pickups: {@code dropped_by.unit: first} of a Ray's wave is the Ray (its wave's first group), not one of its
     * Barbfins: the first Ray carries the overdrive, the second the shield cell (the {@code unit: first} carrier check).
     */
    @Test
    void theRaysCarryTheOverdriveAndTheShieldCell() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> waves = SimSpecs.level(content, LEVEL, difficulty).waves();
            PickupType[] carried = {PickupType.OVERDRIVE, PickupType.SHIELD_CELL};
            for (int r = 0; r < 2; r++) {
                double t = RAYS.get(r);
                for (WaveSpec wave : waves) {
                    if (wave.t() != t) {
                        continue;
                    }
                    assertEquals(
                            wave.enemy().slug().equals("abyss-ray")
                                    ? List.of(new WaveSpec.Carried(carried[r], 0, false))
                                    : List.of(),
                            wave.carried(),
                            difficulty + " t=" + t + " " + wave.enemy().slug());
                }
            }
            assertTrue(waves.stream().filter(wave -> wave.t() == RAYS.get(2)).allMatch(wave -> wave.carried()
                    .isEmpty()));
        }
    }

    /**
     * The radio: Varga's {@code first-ray} line on the first Ray's shadow (the simulation's sighting, at t=58), Rook's
     * two lines only with him on the wing, the secondary's line true on every difficulty (the third reef razed, easy the
     * second; hard the third too, round 53 C.1).
     */
    @Test
    void theFirstRayLinePlaysAtItsShadow() {
        List<RadioCue> radio = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        int first = -1;
        for (int i = 0; i < radio.size(); i++) {
            if (radio.get(i).trigger() == CueTrigger.FIRST_RAY) {
                first = i;
            }
        }
        assertTrue(first >= 0);
        assertEquals("Varga", radio.get(first).speaker());
        assertEquals(
                2,
                radio.stream()
                        .filter(cue -> cue.speaker().equals("Rook") && cue.requires() == RadioCue.FITTED_ESCORT)
                        .count());
        String[] lines = {"Second reef down.", "Third reef down.", "Third reef down."};
        for (Difficulty difficulty : Difficulty.values()) {
            RadioCue met = SimSpecs.level(content, LEVEL, difficulty).radio().stream()
                    .filter(cue -> cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE)
                    .findFirst()
                    .orElseThrow();
            assertTrue(met.line().startsWith(lines[difficulty.ordinal()]), difficulty + ": " + met.line());
        }
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        double heard = -1;
        double shadow = -1;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS && heard < 0) {
            Level04Test.step(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.RADIO && events.value(i) == first) {
                    heard = sortie.levelSeconds();
                }
                if (events.type(i) == SimEvents.Type.RISE_SHADOW && shadow < 0) {
                    shadow = sortie.levelSeconds();
                }
            }
        }
        assertTrue(shadow >= 58 && shadow < 58.5, "the first shadow at its wave's time: " + shadow);
        assertTrue(heard >= shadow && heard < shadow + 1, "the first-ray line at the shadow: " + heard);
    }

    /**
     * Never two Rays on the screen at once (their waves a visit apart, the validator's check on every difficulty, and in
     * flight on hard); each Ray's four Barbfins appear only from its rise on.
     */
    @Test
    void neverTwoRaysOnTheScreen() {
        Sortie sortie = sortie(content, 1, Difficulty.HARD, true);
        int most = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            int rays = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.risePhase() != RisePhase.NONE
                        && PlayField.overlaps(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    rays++;
                }
            }
            most = Math.max(most, rays);
        }
        assertTrue(sortie.complete());
        assertEquals(1, most, "Rays on the screen at once");
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed ({@link AbyssPlay}: the
     * torpedoes into the Ray's shadow, the Barbfins before the fry) and meets REEFS on most seeds on every difficulty
     * (hard allows one reef away since round 53 C.1, as medium; step E4: on hard the reefs' 260 HP are played from their
     * entry, close under them where they are shut and over them for the Depth Charges, {@link WaterPlay#TOUGH_HP}).
     * Printed: the secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 24 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries));
            assertTrue(met * 2 > SEEDS, difficulty + ": the REEFS secondary on most seeds: " + met);
        }
        System.out.println(report);
    }

    /** The autopilot completes the level without Rook too ({@code --escort none}), one seed per difficulty. */
    @Test
    void theAutopilotFliesTheLevelWithoutRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run =
                    LevelMetrics.autopilot(content, LEVEL, difficulty, withoutRook(content, difficulty), 1, MAX_STEPS);
            assertTrue(run.complete(), difficulty.name());
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level24Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
    }

    private WaveSpec wave(Difficulty difficulty, double t) {
        return SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                .filter(wave -> wave.t() == t)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no wave at t=" + t));
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
