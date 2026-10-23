package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.AttackSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.FlightSpecs;
import vanguard.sim.GroundSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.SonarRings;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.WarningEdge;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part E, step E4: Level 25 Siren Trench from its data (design/campaign/act-4-deep-water/level-25-siren-trench;
 * design/enemies/naval/siren, spiral-nautilus): its count, its six sections under water from the start, the trench (its
 * walls, the pillar's fork, the narrows, the 25 notches, the contact) and no air unit in its rock, the quiet windows, the
 * Siren (its ring and slow by difficulty, the nests' phase rules in flight) and the Spiral Nautilus (its coil, none
 * holding over the narrows), the cave entries warned 3 s ahead at their wall's edge, the pickups, the kill-ratio
 * secondary, the credits of a perfect run, the radio, and whole runs of the autopilot with the balance plan's fit
 * ({@link SirenPlay}: the nests kept clear of, the trench looked at further ahead, the bullets further while slowed)
 * with and without Rook, which never enters a notch. The pacing, the floor, the density, the beam era's HP and the haul
 * are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest},
 * {@link TypicalHaulTest}); the ring and the coil themselves the simulation's ({@code SirenTest}, {@code
 * SpiralNautilusTest}).
 */
class Level25Test {
    static final String LEVEL = "act-4-deep-water/level-25-siren-trench";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The Siren nests' waves: t, nests at medium. */
    static final double[] NESTS = {18, 26.8, 36.5, 47, 56.2, 70, 79.5, 91.3, 98.5, 115, 131.3, 142, 152.3};
    /** The Spiral Nautilus pairs' waves. */
    static final double[] PAIRS = {62.5, 82, 120, 138.5, 154.5};

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 25 ({@code "25"}: the other pod's Micro-missiles to L3 on Level 24's): the Harpoon
     * Torpedoes L3 in front, both Micro-missile Pods L3, the Depth Charges L3, the Sonar Pulse, the Pressure hull, the Mk
     * II shield, Composite III, and Rook's Autocannon.
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
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
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
     * The README: 280 enemies at medium, the waves' 271 (Glassfin 151, Barbfin 79, Siren 31, Spiral Nautilus 10) and
     * the ground targets' 9 Reef Spitters; the survey sub is a trigger with its secret. Easy 225 (216 + 9), hard 335
     * (326 + 9) by the formation lever.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(271, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        String[] slugs = {"glassfin", "barbfin", "siren", "spiral-nautilus"};
        int[] counts = {151, 79, 31, 10};
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
        assertEquals(9, level.groundUnits().size());
        assertTrue(level.groundUnits().stream()
                .allMatch(unit -> unit.enemy().slug().equals("reef-spitter")));
        assertEquals(1, level.secrets());
        int[] waves = {216, 271, 326};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript at = SimSpecs.level(content, LEVEL, difficulty);
            assertEquals(
                    waves[difficulty.ordinal()],
                    at.waves().stream().mapToInt(LevelMetrics::planned).sum(),
                    difficulty.name());
            assertEquals(9, at.groundUnits().size(), difficulty.name());
        }
    }

    /**
     * Six sections, the whole level under water from its start (no waterline): the mouth, the walls and the cave mouths
     * at 140 px/s to 22, 60 and 95 s, the narrows at 120 to 130 s, the Song at 130 to 160 s, the floor at 140 to 173 s,
     * each change of speed eased over 2 s; under water the Airstrike and the Orbital Lance cannot reach and the Sonar
     * Pulse is free.
     */
    @Test
    void theWholeTrenchIsUnderWater() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(22.0, 60.0, 95.0, 130.0, 160.0, 173.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(140.0, 140.0, 140.0, 120.0, 130.0, 140.0),
                sections.stream().map(Section::speed).toList());
        for (int i = 3; i < 6; i++) {
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
     * The trench (Level 16's terrain rules under water): the walls 400 px apart by t=12, 320 by t=20, 260 from t=34 to
     * the fork; the pillar splits the fork into two 140 px channels; the narrows 190 px; 260 again in the Song; the
     * flyable width never under 180 px nor a channel under 130, a wall moving at most 0.5 px sideways per px of scroll
     * (the walls' lines, the notches aside); 25 notches, each 90 px along its wall and 70 deep; a touch costs 10 (easy 6)
     * and pushes 28 px over 0.12 s, then 0.5 s of cooldown.
     */
    @Test
    void theTrenchsWallsPillarAndNotches() {
        double[] damage = {6, 10, 10};
        for (Difficulty difficulty : Difficulty.values()) {
            GroundSpec.TerrainSpec spec = SimSpecs.level(content, LEVEL, difficulty)
                    .ground()
                    .terrain()
                    .orElseThrow();
            assertEquals(damage[difficulty.ordinal()], spec.damage(), difficulty.name());
            assertEquals(28, spec.push());
            assertEquals(0.12, spec.pushSeconds());
            assertEquals(0.5, spec.cooldownSeconds());
        }
        Terrain rock = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM)
                .ground()
                .terrain()
                .orElseThrow()
                .terrain();
        assertEquals(400, width(rock, 1_680), 1e-9, "t=12");
        assertEquals(320, width(rock, 2_800), 1e-9, "t=20");
        assertEquals(260, width(rock, 8_000), 1e-9, "the walls and the caves");
        assertEquals(330, width(rock, 14_000), 1e-9, "the fork");
        assertEquals(1, rock.islands());
        assertEquals(140, channel(rock, 14_000, 100), 1e-9, "the fork's left channel");
        assertEquals(140, channel(rock, 14_000, 380), 1e-9, "the fork's right channel");
        assertEquals(190, width(rock, 15_000), 1e-9, "the narrows");
        assertEquals(260, width(rock, 18_000), 1e-9, "the Song");
        double narrowest = Double.POSITIVE_INFINITY;
        double narrowestChannel = Double.POSITIVE_INFINITY;
        for (double row = 700; row <= 22_660; row += 5) {
            narrowest = Math.min(narrowest, width(rock, row));
            for (double x = rock.leftAt(row) + 1; x < rock.rightAt(row); x += 10) {
                double span = channel(rock, row, x);
                if (!Double.isNaN(span)) {
                    narrowestChannel = Math.min(narrowestChannel, span);
                }
            }
            if (row + 5 <= 22_660) {
                assertTrue(Math.abs(rock.leftAt(row + 5) - rock.leftAt(row)) <= 2.5 + 1e-9, "left wall at " + row);
                assertTrue(Math.abs(rock.rightAt(row + 5) - rock.rightAt(row)) <= 2.5 + 1e-9, "right wall at " + row);
            }
        }
        assertTrue(narrowest >= 180, "the narrowest width " + narrowest);
        assertTrue(narrowestChannel >= 130, "the narrowest channel " + narrowestChannel);
        List<Terrain.Notch> notches = rock.notches();
        assertEquals(25, notches.size());
        for (Terrain.Notch notch : notches) {
            assertEquals(90, notch.width());
            assertEquals(70, notch.depth());
        }
        assertEquals(Terrain.Side.LEFT, notches.get(11).side(), "the survey sub's cave");
    }

    private static double width(Terrain rock, double row) {
        return rock.rightAt(row) - rock.leftAt(row);
    }

    /** The flyable channel holding x at ground row {@code row} (the row at the top edge's scroll), px; NaN in rock. */
    private static double channel(Terrain rock, double row, double x) {
        return rock.spanRight(row, PlayField.HEIGHT, x) - rock.spanLeft(row, PlayField.HEIGHT, x);
    }

    /**
     * No air unit's centre on the play field lies in rock (the walls, the pillar; a notch is open water), sampled every
     * step of a run to the level's end on every difficulty: the cave units swim out of their notch's back, the schools
     * fly the trench's middle and the fork's left channel, the Nautilus's orbit is clamped to the flyable span, the
     * nests stand in their wall's foot band.
     *
     * <p>Step E4 found a hover's drift (6 px/s) carrying a Barbfin of a five-wide group on hard (the t=44 and t=77
     * V-wings, the t=54 line), laid out to the span's edge, up to 6 px past the wall's line; step E5 clamps the drift to
     * the flyable span, so nothing is let off.
     */
    @Test
    void noAirUnitFliesInRock() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, 2185, difficulty, true);
            Terrain terrain = sortie.terrain();
            Map<String, Integer> inRock = new TreeMap<>();
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                double scroll = sortie.groundScroll();
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    double x = enemy.renderX(1);
                    double y = enemy.renderY(1);
                    if (enemy.layer() == Layer.GROUND
                            || x < 0
                            || x > PlayField.WIDTH
                            || y < 0
                            || y > PlayField.HEIGHT) {
                        continue;
                    }
                    if (terrain.rock(scroll, x, y, 0.5, 0.5)) {
                        inRock.merge(
                                String.format("%s t=%.0f", enemy.spec().slug(), sortie.levelSeconds()),
                                1,
                                Integer::sum);
                    }
                }
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertEquals(Map.of(), inRock, difficulty + ": air units in rock (slug, level second: steps)");
        }
    }

    /**
     * The quiet windows: no other wave for 3.5 s after the first Siren (t=18) and after the first Nautilus pair
     * (t=62.5), on every difficulty.
     */
    @Test
    void theNewUnitsHaveTheirQuietWindows() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                double t = wave.t();
                assertFalse(t > 18 && t < 21.5, difficulty + ": a wave at t=" + t);
                assertFalse(t > 62.5 && t < 66, difficulty + ": a wave at t=" + t);
            }
        }
    }

    /**
     * The Siren: 90 HP (its own first level), {@code air}, a 48×40 hit box, nests on the walls' feet (staggered, the
     * t=98.5 pair opposite across the fork); its ring 1.2 s after it enters, told 0.8 s, every 4.5 s (easy 6, hard 3.6),
     * at 120 px/s to 230 px with a 10 px band, no damage, slowing to 60 % for 2 s (easy 1.5 s) with 3 s of immunity after
     * (hard 2 s).
     */
    @Test
    void theSirensSingSlowingRings() {
        double[] hp = {68, 90, 117};
        double[] interval = {6, 4.5, 3.6};
        double[] slow = {1.5, 2, 2};
        double[] immunity = {3, 3, 2};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> nests = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("siren"))
                    .toList();
            assertEquals(
                    NESTS.length,
                    nests.size(),
                    difficulty + ": " + nests.stream().map(WaveSpec::t).toList());
            for (int i = 0; i < NESTS.length; i++) {
                WaveSpec wave = nests.get(i);
                assertEquals(NESTS[i], wave.t(), difficulty.name());
                WaveSpec.Mount mount = wave.mount().orElseThrow();
                assertTrue(mount.wall(), "t=" + wave.t());
                assertEquals(wave.t() == 98.5, mount.opposite(), "t=" + wave.t());
            }
            EnemySpec siren = nests.getFirst().enemy();
            assertEquals(Layer.AIR, siren.layer());
            assertEquals(hp[difficulty.ordinal()], siren.hp(), 0.5, difficulty.name());
            assertEquals(48, siren.hitbox().width());
            assertEquals(40, siren.hitbox().height());
            int d = difficulty.ordinal();
            assertEquals(
                    new AttackSpecs.SlowRing(1.2, interval[d], 0.8, 120, 230, 10, 0.6, slow[d], immunity[d]),
                    siren.slowRing().orElseThrow(),
                    difficulty.name());
        }
    }

    /**
     * The nests' phase rules in flight on every difficulty (the content check asks them of the plan): at most four nests
     * on the screen, at most one ring in flight per wall, two rings leaving their sacs at least 1.5 s apart.
     */
    @Test
    void theNestsKeepTheirPhaseRulesInFlight() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, 1, difficulty, true);
            int mostNests = 0;
            int mostPerWall = 0;
            double lastRing = Double.NEGATIVE_INFINITY;
            double closest = Double.POSITIVE_INFINITY;
            int rings = 0;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                int nests = 0;
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (enemy.wallMounted() && PlayField.overlaps(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                        nests++;
                    }
                }
                mostNests = Math.max(mostNests, nests);
                SonarRings flying = sortie.sonarRings();
                int left = 0;
                int right = 0;
                for (int i = 0; i < flying.count(); i++) {
                    if (flying.x(i) < PlayField.WIDTH / 2.0) {
                        left++;
                    } else {
                        right++;
                    }
                }
                mostPerWall = Math.max(mostPerWall, Math.max(left, right));
                SimEvents events = sortie.events();
                for (int i = 0; i < events.size(); i++) {
                    if (events.type(i) == SimEvents.Type.RING) {
                        double t = sortie.levelSeconds();
                        closest = Math.min(closest, t - lastRing);
                        lastRing = t;
                        rings++;
                    }
                }
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertTrue(rings >= NESTS.length, difficulty + ": rings sung " + rings);
            assertTrue(mostNests <= 4, difficulty + ": nests on the screen " + mostNests);
            assertEquals(1, mostPerWall, difficulty + ": rings in flight on one wall");
            assertTrue(closest >= 1.5 - 1e-6, difficulty + ": two rings " + closest + " s apart");
        }
    }

    /**
     * The Spiral Nautilus: 100 HP (its own first level), in pairs ({@code circle}) held 15 s, orbiting from 220 px in to
     * 90 px over 3 s closed, opening 0.6 s (its tell the last 0.4 s), open 1.6 s (easy 1.8, hard 1.4), the pair's cycles
     * 2 s apart; no pair holds while the narrows reach the top edge (t=108–118).
     */
    @Test
    void theNautilusPairsCoilOutsideTheNarrows() {
        double[] hp = {75, 100, 130};
        double[] open = {1.8, 1.6, 1.4};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> pairs = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("spiral-nautilus"))
                    .toList();
            assertEquals(
                    List.of(62.5, 82.0, 120.0, 138.5, 154.5),
                    pairs.stream().map(WaveSpec::t).toList());
            for (WaveSpec pair : pairs) {
                assertEquals(WaveSpec.Formation.CIRCLE, pair.formation());
                double hold = pair.holdSeconds().orElseThrow();
                assertEquals(15, hold);
                assertTrue(pair.t() + hold <= 108 || pair.t() >= 118, difficulty + ": a pair over the narrows " + pair);
            }
            EnemySpec nautilus = pairs.getFirst().enemy();
            assertEquals(hp[difficulty.ordinal()], nautilus.hp(), 0.5, difficulty.name());
            FlightSpecs.Coil coil = nautilus.coil().orElseThrow();
            assertEquals(220, coil.outerRadius());
            assertEquals(90, coil.innerRadius());
            assertEquals(3, coil.inSeconds());
            assertEquals(0.6, coil.openingSeconds());
            assertEquals(0.4, coil.tellSeconds());
            assertEquals(open[difficulty.ordinal()], coil.openSeconds(), 1e-9, difficulty.name());
            assertEquals(2, coil.offsetSeconds());
        }
    }

    /**
     * The cave entries: each cave wave swims out of its notch on the wall of its side and is warned at that edge at least
     * 3 s ahead (flown at medium).
     */
    @Test
    void theCaveEntriesAreWarnedAtTheirWall() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        List<Terrain.Notch> notches = sortie.terrain().notches();
        List<WaveSpec> caves = level.waves().stream()
                .filter(wave -> wave.mount().isPresent() && !wave.mount().get().wall())
                .toList();
        assertEquals(10, caves.size());
        for (WaveSpec wave : caves) {
            Terrain.Notch notch = notches.get(wave.mount().get().cave());
            assertEquals(WaveSpec.Entry.SIDES, wave.entry());
            assertEquals(notch.side().name(), wave.edge().name(), "t=" + wave.t());
        }
        List<Double> warned = new ArrayList<>();
        int next = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS && next < caves.size()) {
            Level04Test.step(sortie);
            WaveSpec wave = caves.get(next);
            if (sortie.levelSeconds() >= wave.t() - 2.95) {
                WarningEdge edge = WarningEdge.valueOf(wave.edge().name());
                if (edge.in(sortie.edgeWarnings())) {
                    warned.add(wave.t());
                }
                next++;
            }
        }
        assertEquals(caves.stream().map(WaveSpec::t).toList(), warned, "the cave waves warned 3 s ahead");
    }

    /**
     * The pickups: the overdrive is the last Siren's of the t=91.3 trio, the shield cell the second Nautilus's of the
     * first pair, on every difficulty.
     */
    @Test
    void theSirensTrioAndTheFirstPairCarryThePickups() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                if (wave.t() == 91.3) {
                    assertEquals(1, wave.carried().size(), difficulty.name());
                    assertEquals(PickupType.OVERDRIVE, wave.carried().getFirst().pickup());
                    assertTrue(wave.carried().getFirst().lastUnit(), difficulty.name());
                } else if (wave.t() == 62.5) {
                    assertEquals(1, wave.carried().size(), difficulty.name());
                    assertEquals(
                            PickupType.SHIELD_CELL, wave.carried().getFirst().pickup());
                    assertEquals(1, wave.carried().getFirst().unit(), difficulty.name());
                } else {
                    assertEquals(List.of(), wave.carried(), difficulty + " t=" + wave.t());
                }
            }
        }
    }

    /**
     * The secondary: at least 65 % of the level's enemies destroyed, judged at the end, 100 credits (Act 1 terms); its
     * line Okafor's at the end. The radio: Varga's Siren line at t=20, Rook's joke only with him on the wing.
     */
    @Test
    void theKillRatioSecondaryAndTheRadio() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Secondary secondary = level.objectives().secondary();
            assertEquals(0.65, secondary.killRatio(), difficulty.name());
            assertEquals(100, secondary.credits());
            RadioCue met = level.radio().stream()
                    .filter(cue -> cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE)
                    .findFirst()
                    .orElseThrow();
            assertEquals("Okafor", met.speaker());
        }
        List<RadioCue> radio = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        assertEquals(
                1,
                radio.stream()
                        .filter(cue -> cue.speaker().equals("Rook") && cue.requires() == RadioCue.FITTED_ESCORT)
                        .count());
        RadioCue siren = radio.stream()
                .filter(cue -> cue.line().startsWith("A Siren."))
                .findFirst()
                .orElseThrow();
        assertEquals("Varga", siren.speaker());
        assertEquals(20, siren.t());
    }

    /**
     * The README's credit table ({@code bounty_scale} 0.48): a perfect run at medium in Act 1 terms, Glassfin 151 × 2,
     * Barbfin 79 × 12, Siren 31 × 28, Spiral Nautilus 10 × 30 and the Reef Spitters 9 × 14, against budget(25) = 3,551.
     */
    @Test
    void aPerfectRunAtMediumEarnsTheReadmesTotal() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int kills = 0;
        for (WaveSpec wave : level.waves()) {
            kills += wave.count() * wave.enemy().bounty();
            kills += wave.count()
                    * wave.enemy().parts().stream()
                            .mapToInt(EnemySpec.UnitPart::bounty)
                            .sum();
        }
        int ground = level.groundUnits().stream()
                .mapToInt(unit -> unit.enemy().bounty())
                .sum();
        assertEquals(151 * 2 + 79 * 12 + 31 * 28 + 10 * 30, kills, "Glassfin, Barbfin, Siren, Spiral Nautilus");
        assertEquals(9 * 14, ground, "the Reef Spitters");
        assertEquals(0.48, content.level(LEVEL).bounties());
        assertEquals(3_551, Math.round(content.economy().budget().of(level.number())), "700 × 1.07²⁴");
    }

    /**
     * The rings slow the ship and Rook in the level (flown at medium, invulnerable): each slowed hull stays slowed at
     * most the ring's 2 s and is not slowed again within its 3 s of immunity.
     */
    @Test
    void theRingsSlowTheShipAndRook() {
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        int[] slowed = new int[2];
        double[] last = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double closest = Double.POSITIVE_INFINITY;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.SLOWED) {
                    int hull = events.value(i);
                    slowed[hull]++;
                    if (hull == 0) {
                        closest = Math.min(closest, sortie.levelSeconds() - last[0]);
                    }
                    last[hull] = sortie.levelSeconds();
                }
            }
        }
        assertTrue(sortie.complete());
        assertTrue(slowed[0] > 0, "the ship slowed");
        assertTrue(slowed[1] > 0, "Rook slowed");
        assertTrue(closest >= 2 + 3 - 0.1, "slowed again after " + closest + " s");
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed ({@link SirenPlay}: the nests
     * kept clear of, the pillar's fork and the narrows looked at further ahead, the bullets further while slowed), never
     * entering a notch, and meets the kill ratio on most seeds. Printed: the secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 25 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                OutOfTheNotches notches = new OutOfTheNotches();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, notches), difficulty + " seed " + seed);
                assertEquals(0, notches.inside, difficulty + " seed " + seed + ": steps in a notch");
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries));
            assertTrue(met * 2 > SEEDS, difficulty + ": the kill ratio on most seeds: " + met);
        }
        System.out.println(report);
    }

    /** Counts the steps the ship's centre spends past a wall's collision line (inside a notch). */
    private static final class OutOfTheNotches implements LevelMetrics.Probe {
        int inside;

        @Override
        public void stepped(Sortie sortie, int step) {
            Terrain terrain = sortie.terrain();
            double row = Terrain.row(sortie.groundScroll(), sortie.ship().y());
            double left = terrain.leftAt(row);
            double right = terrain.rightAt(row);
            if ((!Double.isNaN(left) && sortie.ship().x() < left)
                    || (!Double.isNaN(right) && sortie.ship().x() > right)) {
                inside++;
            }
        }
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
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level25Test::flyToEnd);

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

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
