package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.DropSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.PowerState;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M9 part F, step F3: Level 33 Leech Field from its data (design/campaign/act-5-the-belt/level-33-leech-field; the units
 * in design/enemies: the Void Leech and the Rail Bunker (new), the Talon, Asteroid Mite, Stinger, Skitter, Mote Swarm):
 * its count per difficulty, the leech's and the bunker's stat blocks as built, the prey wings and their hunting streams,
 * {@code Purge the field} per difficulty, <em>Claim 114</em>, the nav buoy's drop and the Choir's line; and whole runs of
 * the autopilot ({@link LeechPlay}) with the balance plan's fit and its power budget, with Rook and without him: at most
 * {@link #MOST_FREE} free leeches on the screen, the prey wings (a preyed Talon's shield empty, no fire, its drift down,
 * both paying, the leech thrown off when its prey dies), the drain (−2 MW a leech, the shield's regrowth stopped, the
 * output back in the step a leech lets go), at most 2 rail lines telegraphed and a column left free between a pair's
 * crossed lines. The pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link
 * PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}: the hardened
 * bunkers at 0 %, {@link TypicalHaul#UNPAID_HARDENED}); the mechanics themselves the simulation's ({@code
 * LeechDrainTest}, {@code LeechChaseTest}, {@code PreyTest}, {@code LaserLineTest}).
 */
class Level33Test {
    static final String LEVEL = "act-5-the-belt/level-33-leech-field";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;
    /** The fit's generator output and load, MW: the Mk IV "Tokamak" fully loaded (the plan's L33 visit buys nothing). */
    static final double OUTPUT = 20;

    static final double LOAD = 20;
    /** A latched leech's drain, MW, and the most all of them drain (design/player/generator, Enemy drain effects). */
    static final double DRAIN_MW = 2;

    static final double MOST_DRAIN_MW = 6;
    /** The most free leeches on the screen at once (the README's cap; three latched slots on top). */
    static final int MOST_FREE = 6;
    /** The most rail lines telegraphed at once (design/enemies/ground/rail-bunker). */
    static final int MOST_TELEGRAPHING = 2;
    /** The leeches that may get away for {@code Purge the field}, by difficulty (a quarter of the count). */
    static final Map<Difficulty, Integer> ALLOWED =
            Map.of(Difficulty.EASY, 10, Difficulty.MEDIUM, 15, Difficulty.HARD, 18);
    /** <em>Claim 114</em>'s HP by difficulty and its crate (Act 1 terms). */
    static final Map<Difficulty, Double> CLAIM_HP =
            Map.of(Difficulty.EASY, 15.0, Difficulty.MEDIUM, 20.0, Difficulty.HARD, 28.0);

    static final int CLAIM_CRATE = 100;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 33 with Rook in the slot: the plan's L33 visit buys nothing, so Level 32's fit (the
     * Ion Beam L4, the Micro-missile Pods L3 ×2, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst) with
     * its power budget, 20 of 20 MW (design/player/generator, Act 5 hand plan): one latched leech stops the shield's
     * regrowth, the third slows the fire.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level32Test.planLoadout(content, difficulty).withPower(power(content, difficulty));
    }

    /** The same without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level32Test.withoutRook(content, difficulty).withPower(power(content, difficulty));
    }

    private static Loadout.Power power(Content content, Difficulty difficulty) {
        return SimSpecs.power(
                content, OUTPUT, LOAD, content.shields().models().get(2).name(), difficulty);
    }

    static Sortie sortie(Content content, Loadout loadout, long seed, Difficulty difficulty, boolean invulnerable) {
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    private static int count(LevelScript level, String slug) {
        int waves = level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .mapToInt(WaveSpec::count)
                .sum();
        int ground = (int) level.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals(slug))
                .count();
        return waves + ground;
    }

    private static EnemySpec enemy(LevelScript level, String slug) {
        return level.waves().stream()
                .map(WaveSpec::enemy)
                .filter(enemy -> enemy.slug().equals(slug))
                .findFirst()
                .orElseGet(() -> level.groundUnits().stream()
                        .map(GroundUnit::enemy)
                        .filter(enemy -> enemy.slug().equals(slug))
                        .findFirst()
                        .orElseThrow());
    }

    /**
     * The README's waves at medium (its credit table: Asteroid Mite 53, Talon 138, Skitter 50, Mote Swarm 61, Stinger
     * 20, Void Leech 63, Rail Bunker 8) and per difficulty (the formation lever, the leech waves' own counts, the prey
     * wings never cut, the bunker trios on hard).
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        String[] slugs = {"asteroid-mite", "talon", "skitter", "mote-swarm", "stinger", "void-leech", "rail-bunker"};
        Map<Difficulty, List<Integer>> counted = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<Integer> counts = new ArrayList<>();
            for (String slug : slugs) {
                counts.add(count(level, slug));
            }
            counted.put(difficulty, counts);
        }
        System.out.println("Level 33 counts " + List.of(slugs) + ": " + counted);
        assertEquals(List.of(53, 138, 50, 61, 20, 63, 8), counted.get(Difficulty.MEDIUM), "medium");
        assertEquals(40, counted.get(Difficulty.EASY).get(5), "easy: 40 leeches (the secondary's 30 of 40)");
        assertEquals(74, counted.get(Difficulty.HARD).get(5), "hard: 74 leeches (the secondary's 56 of 74)");
        assertEquals(10, counted.get(Difficulty.HARD).get(6), "hard: the t≈86 and t≈132 pairs become trios");
        assertEquals(8, counted.get(Difficulty.EASY).get(6), "easy: the bunkers as at medium");
    }

    /**
     * The Void Leech as its stat block (design/enemies/space/void-leech): a latch that drains the generator (no shield
     * points), thrown off by the EMP, riding a prey down at 60 px/s; a drift at 50 px/s until within 220 px, then the
     * chase; easy latches on 2 spots. The Rail Bunker (design/enemies/ground/rail-bunker): hardened, on the ground, its
     * locked line gated at 2 telegraphs. The prey wings' Talons are PREY (no swoop path, their hold), the streams aimed at
     * them HUNTER, a staggered pair's first lines half the interval apart.
     */
    @Test
    void theLeechAndTheBunkerAreBuiltAsTheirStatBlocks() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec leech = enemy(level, "void-leech");
            EnemySpec.Latch latch = leech.latch().orElseThrow();
            assertTrue(latch.drainsPower(), difficulty + ": the leech drains the generator");
            assertEquals(0, latch.drainRate(), difficulty + ": no shield points");
            assertTrue(latch.empThrows(), difficulty + ": the EMP throws it off");
            assertEquals(60, latch.preyDrift(), difficulty + ": a prey drifts down at 60 px/s");
            assertEquals(difficulty == Difficulty.EASY ? 2 : 3, latch.slots(), difficulty + ": latch slots");
            EnemySpec.Chase chase = leech.chase().orElseThrow();
            assertEquals(220, chase.within(), difficulty.name());
            assertEquals(50, chase.drift(), difficulty.name());
            EnemySpec bunker = enemy(level, "rail-bunker");
            assertTrue(bunker.hardened(), difficulty + ": the bunker is hardened");
            assertEquals(MOST_TELEGRAPHING, bunker.laserLine().orElseThrow().mostTelegraphing(), difficulty.name());
            int prey = 0;
            int hunters = 0;
            for (WaveSpec wave : level.waves()) {
                if (wave.enemy().preyRole() == EnemySpec.PreyRole.PREY) {
                    prey++;
                    assertEquals("talon", wave.enemy().slug(), difficulty + ": a prey wing is Talons");
                    assertTrue(wave.enemy().swoopPath().isEmpty(), difficulty + ": a prey wing holds, no swoop");
                    assertTrue(wave.enemy().hover().isPresent(), difficulty + ": a prey wing's refuelling hold");
                }
                if (wave.enemy().preyRole() == EnemySpec.PreyRole.HUNTER) {
                    hunters++;
                    assertEquals("void-leech", wave.enemy().slug(), difficulty.name());
                }
            }
            assertEquals(2, prey, difficulty + ": the two prey wings (t=62.5 and t=95)");
            assertEquals(2, hunters, difficulty + ": the two streams aimed at them");
            // the bunker pairs' stagger: placement i of n first fires i × interval / n later
            double interval = bunker.laserLine().orElseThrow().intervalSeconds();
            Map<Double, List<Double>> delays = new LinkedHashMap<>();
            for (GroundUnit unit : level.groundUnits()) {
                if (unit.enemy().slug().equals("rail-bunker")) {
                    delays.computeIfAbsent(Math.floor(unit.t()), t -> new ArrayList<>())
                            .add(unit.enemy().laserLine().orElseThrow().firstDelaySeconds());
                }
            }
            for (List<Double> group : delays.values()) {
                group.sort(Double::compare);
                for (int i = 1; i < group.size(); i++) {
                    assertEquals(
                            interval / group.size(),
                            group.get(i) - group.get(i - 1),
                            1e-9,
                            difficulty + ": a bunker group's stagger " + delays);
                }
            }
        }
    }

    /**
     * {@code Purge the field}: {@code escapes: void-leech}, label {@code LEECHES}, 100 in Act 1 terms; a quarter of each
     * difficulty's leeches may get away (10, 15, 18).
     */
    @Test
    void purgeTheFieldIsTheSecondaryPerDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary =
                    SimSpecs.level(content, LEVEL, difficulty).objectives().secondary();
            assertEquals("void-leech", secondary.escapes(), difficulty.name());
            assertEquals(ALLOWED.get(difficulty), secondary.escapesAllowed(), difficulty.name());
            assertEquals("LEECHES", secondary.label());
            assertEquals(100, secondary.credits());
        }
    }

    /**
     * <em>Claim 114</em>: a ground trigger at (t 104, x 380), 20 HP (easy 15, hard 28), its crate 100 in Act 1 terms and
     * Rook's secret line; the CDF nav buoy releases an armour patch at (240, 380) at t=160 ({@code sprite: nav-buoy});
     * the leaders of the t=31 and t=126 V-wings carry the shield cell and the overdrive.
     */
    @Test
    void claim114TheBuoysDropAndTheCarriers() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<GroundObjectSpec> claims = level.groundObjects().stream()
                    .filter(object -> !object.secret().isEmpty())
                    .toList();
            assertEquals(1, claims.size(), difficulty + ": the one secret trigger");
            GroundObjectSpec claim = claims.getFirst();
            assertEquals("Claim 114", claim.secret());
            assertEquals(104, claim.t(), 1e-9);
            assertEquals(380, claim.x(), 1e-9);
            assertEquals(CLAIM_HP.get(difficulty), claim.hp(), 1e-9, difficulty.name());
            assertFalse(claim.hardened(), "the plan's beam opens it");
            assertEquals(
                    List.of(new DropSpec.PlacedPickup(160, 240, 380, PickupType.ARMOUR_PATCH)),
                    level.drops().placedPickups(),
                    difficulty + ": the buoy's armour patch");
            Map<Double, List<WaveSpec.Carried>> carried = new LinkedHashMap<>();
            for (WaveSpec wave : level.waves()) {
                if (!wave.carried().isEmpty()) {
                    carried.put(wave.t(), wave.carried());
                }
            }
            assertEquals(
                    Map.of(
                            31.0, List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, 0)),
                            126.0, List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, 0))),
                    carried,
                    difficulty + ": the V-wings' leaders carry the pickups");
        }
        LevelData data = content.level(LEVEL);
        assertEquals(1, data.secrets().size());
        assertEquals(CLAIM_CRATE, data.secrets().getFirst().crate());
        assertEquals("Rook", data.secrets().getFirst().radio().speaker());
        assertEquals(1, data.released().size());
        assertEquals("nav-buoy", data.released().getFirst().sprite().orElseThrow(), "BuoyLooks' moored buoy");
    }

    /**
     * The Choir's one line in Act 5 (t=118): distorted, the HUD portrait's static 2 s before it, the music 6 dB under it
     * (Level 26's keys); Varga's answer at t=130 after it; Rook's three lines need him flying ({@code requires: escort});
     * the secondary's line once.
     */
    @Test
    void theChoirsLineAndTheRadio() {
        RadioBlock.RadioCue choir = content.level(LEVEL).radio().stream()
                .filter(cue -> cue.speaker().equals("The Choir"))
                .findFirst()
                .orElseThrow();
        assertEquals(118, choir.t().orElseThrow());
        assertEquals(true, choir.distorted().orElseThrow());
        assertEquals(2, choir.staticLeadSeconds());
        assertEquals(6, choir.musicDuckDb());
        for (Difficulty difficulty : Difficulty.values()) {
            List<RadioCue> radio = SimSpecs.level(content, LEVEL, difficulty).radio();
            RadioCue cue = radio.stream()
                    .filter(line -> line.speaker().equals("The Choir"))
                    .findFirst()
                    .orElseThrow();
            assertTrue(cue.distorted(), difficulty.name());
            assertEquals(118, cue.t());
            assertTrue(
                    radio.stream().anyMatch(line -> line.speaker().equals("Varga") && line.t() == 130),
                    difficulty + ": Varga's answer after the Choir's line and its lead");
            int rook = 0;
            for (RadioCue line : radio) {
                if (line.speaker().equals("Rook") && line.trigger() == CueTrigger.TIME) {
                    rook++;
                    assertTrue((line.requires() & RadioCue.FITTED_ESCORT) != 0, line.line());
                }
            }
            assertEquals(3, rook, difficulty + ": Rook's three timed lines");
            assertEquals(
                    1,
                    radio.stream()
                            .filter(line -> line.trigger() == CueTrigger.SECONDARY_OBJECTIVE)
                            .count(),
                    difficulty.name());
        }
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The most free leeches on the screen at once, and the most latched on the ship. */
        int mostFree;

        int mostLatched;
        /** When the most free leeches were on the screen, s. */
        double mostFreeAt;
        /** Steps where the power budget did not follow the leeches latched on the ship (−2 MW each, the regrowth). */
        int drainOff;
        /** Steps with a leech latched (the regrowth stopped) and with the fire slowed. */
        int drainedSteps;

        int slowedSteps;
        /** The most rail lines telegraphed at once, and steps where two lines ran at once. */
        int mostTelegraphing;

        int twoLines;
        /** Steps where two lines ran at once and left no column free at the ship's height. */
        int noColumn;
        /** A preyed Talon with its shield up, firing, or not drifting down; a rider not on a preyed Talon. */
        int preyShielded;

        int preyFired;
        int preyRising;
        int riderOff;
        /** Riders thrown off as their prey died (stunned or fleeing), and those left riding a dead prey. */
        int riderThrown;

        int riderStuck;
        /** Kills: preyed Talons, leeches on a prey, leeches latched on the ship; those that paid nothing. */
        int preyKills;

        int riderKills;
        int latchedKills;
        int unpaid;
        /** The radio lines Rook spoke. */
        int rookLines;
        /** The leeches the shake-offs threw. */
        int shakenLeeches;

        private final int talon;
        private final int leech;
        private final Map<Integer, double[]> last = new HashMap<>();
        private final Map<Integer, Integer> riding = new HashMap<>();
        private final Map<Integer, Boolean> wasPreyed = new HashMap<>();
        private final Map<Integer, Boolean> wasLatched = new HashMap<>();
        /** Riders whose prey was killed in the last step, and riders off their prey in the last step. */
        private final Map<Integer, Integer> preyGone = new HashMap<>();

        private final Map<Integer, Boolean> offBefore = new HashMap<>();
        private final Map<Integer, Boolean> killedPrey = new HashMap<>();
        private int credits;

        Watch(Sortie sortie) {
            talon = kind(sortie, "talon");
            leech = kind(sortie, "void-leech");
        }

        private static int kind(Sortie sortie, String slug) {
            List<EnemySpec> kinds = sortie.enemyKinds();
            for (int k = 0; k < kinds.size(); k++) {
                if (kinds.get(k).slug().equals(slug)) {
                    return k;
                }
            }
            throw new IllegalStateException(slug);
        }

        @Override
        public void newAttempt() {
            events.clear();
            mostFree = 0;
            mostLatched = 0;
            mostFreeAt = 0;
            drainOff = 0;
            drainedSteps = 0;
            slowedSteps = 0;
            mostTelegraphing = 0;
            twoLines = 0;
            noColumn = 0;
            preyShielded = 0;
            preyFired = 0;
            preyRising = 0;
            riderOff = 0;
            riderThrown = 0;
            riderStuck = 0;
            preyKills = 0;
            riderKills = 0;
            latchedKills = 0;
            unpaid = 0;
            rookLines = 0;
            shakenLeeches = 0;
            forget();
        }

        /** Forgets the units followed (a new attempt's serials start over). */
        void forget() {
            last.clear();
            riding.clear();
            wasPreyed.clear();
            wasLatched.clear();
            preyGone.clear();
            offBefore.clear();
            killedPrey.clear();
            credits = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                shakenLeeches += seen.type(i) == SimEvents.Type.LAMPREY_SHAKEN ? seen.value(i) : 0;
                if (seen.type(i) == SimEvents.Type.RADIO) {
                    RadioCue cue = sortie.script().radio().get(seen.value(i));
                    // the secret's line (Rook's) has no requires key in the data (a secret's radio line has none)
                    rookLines += cue.speaker().equals("Rook") && cue.trigger() != CueTrigger.SECRET ? 1 : 0;
                }
            }
            Map<Integer, Enemy> now = new HashMap<>();
            int free = 0;
            int latched = 0;
            int telegraphing = 0;
            List<Enemy> lines = new ArrayList<>();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                now.put(enemy.serial(), enemy);
                if (enemy.kind() == leech) {
                    free += enemy.loose() ? 1 : 0;
                    latched += enemy.latched() ? 1 : 0;
                }
                if (enemy.spec().laserLine().isPresent() && enemy.lineBeam().running()) {
                    lines.add(enemy);
                    telegraphing += enemy.lineBeam().telegraphing() ? 1 : 0;
                }
            }
            if (free > mostFree) {
                mostFree = free;
                mostFreeAt = sortie.levelSeconds();
            }
            mostLatched = Math.max(mostLatched, latched);
            mostTelegraphing = Math.max(mostTelegraphing, telegraphing);
            power(sortie, latched);
            if (lines.size() >= 2) {
                twoLines++;
                noColumn += column(sortie, lines) ? 0 : 1;
            }
            // the prey: a rider's Talon preyed (shield empty), drifting down, never firing
            // (a prey killed in this step lets its rider go in the next: one step's grace)
            Map<Integer, Boolean> off = new HashMap<>();
            for (Enemy enemy : now.values()) {
                if (enemy.preySerial() >= 0) {
                    Enemy prey = now.get(enemy.preySerial());
                    if (prey == null || !prey.preyed()) {
                        riderOff += offBefore.containsKey(enemy.serial()) ? 1 : 0;
                        off.put(enemy.serial(), true);
                    }
                }
                if (enemy.preyed()) {
                    preyShielded += enemy.shield() > 0 ? 1 : 0;
                    double[] before = last.get(enemy.serial());
                    if (before != null
                            && wasPreyed.getOrDefault(enemy.serial(), false)
                            && enemy.renderY(1) >= before[1]) {
                        preyRising++;
                    }
                }
            }
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.ENEMY_FIRED) {
                    for (Enemy enemy : now.values()) {
                        // (preyed in the step before: a Talon latched this step may have fired before the latch)
                        if (enemy.preyed()
                                && wasPreyed.getOrDefault(enemy.serial(), false)
                                && Math.abs(enemy.renderX(1) - seen.x(i)) < 1e-6
                                && Math.abs(enemy.renderY(1) - seen.y(i)) < 1e-6) {
                            preyFired++;
                        }
                    }
                }
            }
            // the units gone in this step: killed (an ENEMY_DESTROYED of its kind next to it) or away
            int paid = sortie.credits() - credits;
            int watched = 0;
            for (Map.Entry<Integer, double[]> gone : last.entrySet()) {
                if (now.containsKey(gone.getKey())) {
                    continue;
                }
                double[] at = gone.getValue();
                boolean killed = false;
                for (int i = 0; i < seen.size(); i++) {
                    if (seen.type(i) == SimEvents.Type.ENEMY_DESTROYED
                            && seen.value(i) == (int) at[2]
                            && Math.hypot(seen.x(i) - at[0], seen.y(i) - at[1]) <= 16) {
                        killed = true;
                    }
                }
                if (!killed) {
                    continue;
                }
                int serial = gone.getKey();
                if (wasPreyed.getOrDefault(serial, false)) {
                    preyKills++;
                    killedPrey.put(serial, true);
                    watched++;
                } else if (riding.containsKey(serial)) {
                    riderKills++;
                    watched++;
                } else if (wasLatched.getOrDefault(serial, false)) {
                    latchedKills++;
                    watched++;
                }
            }
            if (watched > 0 && paid <= 0) {
                unpaid++;
            }
            // a rider whose prey died (in the step before: its one step's grace): thrown off (stunned, then fleeing),
            // never riding on
            for (Map.Entry<Integer, Integer> ride : preyGone.entrySet()) {
                Enemy rider = now.get(ride.getKey());
                if (rider == null) {
                    continue;
                }
                if (rider.preySerial() < 0
                        && (rider.latchPhase() == Enemy.LatchPhase.STUNNED
                                || rider.latchPhase() == Enemy.LatchPhase.FLEE)) {
                    riderThrown++;
                } else {
                    riderStuck++;
                }
            }
            preyGone.clear();
            for (Map.Entry<Integer, Integer> ride : riding.entrySet()) {
                if (now.containsKey(ride.getKey())
                        && !now.containsKey(ride.getValue())
                        && killedPrey.containsKey(ride.getValue())) {
                    preyGone.put(ride.getKey(), ride.getValue());
                }
            }
            offBefore.clear();
            offBefore.putAll(off);
            killedPrey.clear();
            preyGone.clear();
            for (Map.Entry<Integer, Integer> ride : riding.entrySet()) {
                if (now.containsKey(ride.getKey())
                        && !now.containsKey(ride.getValue())
                        && killedPrey.containsKey(ride.getValue())) {
                    preyGone.put(ride.getKey(), ride.getValue());
                }
            }
            offBefore.clear();
            offBefore.putAll(off);
            killedPrey.clear();
            last.clear();
            riding.clear();
            wasPreyed.clear();
            wasLatched.clear();
            for (Enemy enemy : now.values()) {
                last.put(enemy.serial(), new double[] {enemy.renderX(1), enemy.renderY(1), enemy.kind()});
                if (enemy.preySerial() >= 0) {
                    riding.put(enemy.serial(), enemy.preySerial());
                }
                wasPreyed.put(enemy.serial(), enemy.preyed());
                wasLatched.put(enemy.serial(), enemy.latched());
            }
            credits = sortie.credits();
        }

        /** The power budget follows the leeches latched on the ship: −2 MW each (at most 6), the regrowth stopped. */
        private void power(Sortie sortie, int latched) {
            PowerState power = sortie.power().orElseThrow();
            double drained = Math.min(latched * DRAIN_MW, MOST_DRAIN_MW);
            boolean off = power.drainers() != latched
                    || Math.abs(power.drainedMw() - drained) > 1e-9
                    || Math.abs(power.effectiveOutput() - (OUTPUT - drained)) > 1e-9
                    || power.regenStopped() != (latched > 0)
                    || (latched == 0 && power.factor() != 1);
            drainOff += off ? 1 : 0;
            drainedSteps += latched > 0 ? 1 : 0;
            slowedSteps += power.factor() < 1 ? 1 : 0;
        }

        /**
         * Whether the running lines leave a column free at the ship's height: an x on the play field where the hull
         * (its width) stands clear of every line's band.
         */
        private static boolean column(Sortie sortie, List<Enemy> lines) {
            double y = sortie.ship().y();
            double half = sortie.ship().spec().size() / 2.0;
            double margin = sortie.ship().spec().edgeMargin() + half;
            List<double[]> covered = new ArrayList<>();
            for (Enemy unit : lines) {
                var beam = unit.lineBeam();
                double band = unit.spec().laserLine().orElseThrow().bandPx() / 2 + half;
                if (Math.abs(beam.dirY()) < 1e-6) {
                    if (Math.abs(beam.fromY() - y) <= band) {
                        return false;
                    }
                    continue;
                }
                double s = (y - beam.fromY()) / beam.dirY();
                if (s < 0) {
                    continue;
                }
                double x = beam.fromX() + beam.dirX() * s;
                // the band's half width along x at this height
                double spread = band / Math.abs(beam.dirY());
                covered.add(new double[] {x - spread, x + spread});
            }
            covered.sort((a, b) -> Double.compare(a[0], b[0]));
            double from = margin;
            for (double[] stretch : covered) {
                if (stretch[0] > from) {
                    return true;
                }
                from = Math.max(from, stretch[1]);
            }
            return from < PlayField.WIDTH - margin;
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
        }
    }

    /** One whole flight's measures. */
    record Flight(
            String fit,
            Difficulty difficulty,
            long seed,
            boolean complete,
            LevelMetrics.Run run,
            Watch watch,
            int escapesLost) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch(sortie);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(fit, difficulty, seed, complete, LevelMetrics.Run.of(sortie), watch, sortie.escapesLost());
    }

    /** The flights of a fit over the seeds and difficulties, flown in parallel, in their order. */
    List<Flight> flights(String fit, BiFunction<Content, Difficulty, Loadout> loadout) {
        Difficulty[] difficulties = Difficulty.values();
        return IntStream.range(0, difficulties.length * SEEDS)
                .parallel()
                .mapToObj(i -> {
                    Difficulty difficulty = difficulties[i / SEEDS];
                    return fly(fit, loadout.apply(content, difficulty), difficulty, 1 + i % SEEDS);
                })
                .toList();
    }

    /**
     * Each run's checks: completed; at most {@link #MOST_FREE} free leeches on the screen; the power budget follows the
     * leeches latched on the ship in every step; at most {@link #MOST_TELEGRAPHING} lines telegraphed and a column free at
     * the ship's height whenever two run; a preyed Talon's shield empty, never firing, drifting down; a rider always on a
     * preyed Talon, thrown off when its prey dies; each such kill paid; Rook's lines only with Rook.
     */
    private static void check(Flight flight, boolean rook) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertTrue(watch.mostFree <= MOST_FREE, at + ": " + watch.mostFree + " free leeches at t " + watch.mostFreeAt);
        assertEquals(0, watch.drainOff, at + ": steps where the power budget missed the latched leeches");
        assertTrue(watch.mostTelegraphing <= MOST_TELEGRAPHING, at + ": lines telegraphed " + watch.mostTelegraphing);
        assertEquals(0, watch.noColumn, at + ": two lines leaving no column at the ship's height");
        assertEquals(0, watch.preyShielded, at + ": a preyed Talon's shield up");
        assertEquals(0, watch.preyFired, at + ": a preyed Talon fired");
        assertEquals(0, watch.preyRising, at + ": a preyed Talon not drifting down");
        assertEquals(0, watch.riderOff, at + ": a rider on no preyed Talon");
        assertEquals(0, watch.riderStuck, at + ": a rider left on a dead prey");
        assertEquals(0, watch.unpaid, at + ": a preyed Talon's or a latched leech's kill paid nothing");
        if (!rook) {
            assertEquals(0, watch.rookLines, at + ": Rook's lines need him flying");
        }
    }

    private static String summary(Flight flight) {
        Watch watch = flight.watch();
        return String.format(
                "%n    %s: retries %d, LEECHES %s (away %d), credits %d; free ≤ %d (t %.1f), latched ≤ %d, latches %d,"
                        + " shaken %d, EMP throws %d, latched kills %d; drained %.1f s, fire slowed %.1f s; lines %d"
                        + " (≤ %d telegraphed, two at once %.1f s), hits %d; preyed %d, prey kills %d, rider kills %d,"
                        + " riders thrown %d; secret %d",
                flight.at(),
                flight.run().retries(),
                flight.run().secondaryMet(),
                flight.escapesLost(),
                flight.run().credits(),
                watch.mostFree,
                watch.mostFreeAt,
                watch.mostLatched,
                watch.count(SimEvents.Type.LAMPREY_LATCHED),
                watch.count(SimEvents.Type.LAMPREY_SHAKEN),
                watch.count(SimEvents.Type.LATCH_THROWN),
                watch.latchedKills,
                watch.drainedSteps * SimStep.SECONDS,
                watch.slowedSteps * SimStep.SECONDS,
                watch.count(SimEvents.Type.LINE_TELEGRAPH),
                watch.mostTelegraphing,
                watch.twoLines * SimStep.SECONDS,
                watch.count(SimEvents.Type.LINE_HIT),
                watch.count(SimEvents.Type.PREY_DRIFT),
                watch.preyKills,
                watch.riderKills,
                watch.riderThrown,
                watch.count(SimEvents.Type.SECRET_FOUND));
    }

    /**
     * The autopilot ({@link LeechPlay}) completes the level with the plan's fit and Rook on every difficulty and seed;
     * over each difficulty's runs the leeches latch and drain, the rear guns kill latched leeches, the prey wings are
     * preyed, a preyed Talon and a rider are killed, the rail lines fire, <em>Claim 114</em> is found and {@code Purge
     * the field} is met (the tally per run printed).
     */
    @Test
    void theAutopilotFliesTheFieldWithRook() {
        report("Rook", flights("Rook", Level33Test::planLoadout), true);
    }

    /** Without Rook ({@code --escort none}) the same; his lines stay silent. */
    @Test
    void theAutopilotFliesTheFieldWithoutRook() {
        report("--escort none", flights("--escort none", Level33Test::withoutRook), false);
    }

    private static void report(String fit, List<Flight> flights, boolean rook) {
        StringBuilder report = new StringBuilder("Level 33 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        Map<Difficulty, int[]> tally = new EnumMap<>(Difficulty.class);
        int[] all = new int[4];
        for (Flight flight : flights) {
            check(flight, rook);
            Watch watch = flight.watch();
            int[] sum = tally.computeIfAbsent(flight.difficulty(), d -> new int[7]);
            sum[0] += watch.drainedSteps;
            sum[1] += watch.count(SimEvents.Type.PREY_DRIFT);
            sum[2] += watch.preyKills + watch.riderKills;
            sum[3] += watch.count(SimEvents.Type.LINE_FIRED);
            sum[4] += watch.count(SimEvents.Type.SECRET_FOUND);
            sum[5] += flight.run().secondaryMet() ? 1 : 0;
            sum[6] += watch.count(SimEvents.Type.LINE_HIT);
            all[0] += watch.latchedKills;
            all[1] += watch.count(SimEvents.Type.LAMPREY_SHAKEN);
            all[2] += watch.count(SimEvents.Type.LATCH_THROWN);
            all[3] += watch.slowedSteps;
        }
        tally.forEach((difficulty, sum) -> {
            String at = fit + " " + difficulty;
            assertTrue(sum[0] > 0, at + ": the leeches latch and drain");
            assertTrue(sum[1] > 0, at + ": the prey wings are preyed");
            assertTrue(sum[2] > 0, at + ": a preyed Talon or its rider killed");
            assertTrue(sum[3] > 0, at + ": the rail lines fire");
            assertTrue(sum[4] > 0, at + ": Claim 114 found");
            assertEquals(SEEDS, sum[5], at + ": LEECHES met on every run");
            assertTrue(sum[6] <= SEEDS, at + ": the rail lines hit " + sum[6] + " times (it steps off them)");
        });
        assertTrue(all[0] > 0, fit + ": the rear guns kill a ridden leech");
        assertTrue(all[1] > 0, fit + ": leeches shaken off");
        assertTrue(all[2] > 0, fit + ": the EMP Burst throws latched leeches off");
        assertTrue(all[3] > 0, fit + ": a third leech slows the fire");
    }

    /**
     * A preyed Talon killed under its leech throws the leech off (stunned, then fleeing; it never moves on to the ship):
     * flown invulnerable at medium with a Pulse Cannon L1 alone, holding fire until a prey wing is preyed, then under a
     * preyed Talon whose rider sits on a side spot, so the bolts hit the Talon and not the leech.
     */
    @Test
    void aLeechIsThrownOffWhenItsPreyDies() {
        Loadout bolts = SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(vanguard.sim.Armament.Slot.FRONT, "pulse-cannon", 1)),
                content.shields().models().get(2).name(),
                content.armour().plating().get(4).name(),
                0,
                Difficulty.MEDIUM);
        Sortie sortie = sortie(content, bolts, 1, Difficulty.MEDIUM, true);
        Map<Integer, Integer> riding = new HashMap<>();
        int thrown = 0;
        int stuck = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS && thrown == 0) {
            Enemy prey = null;
            for (int i = 0; i < sortie.enemyCount() && prey == null; i++) {
                Enemy rider = sortie.enemy(i);
                if (rider.preySerial() >= 0 && rider.spot() != EnemySpec.Latch.TAIL) {
                    for (int j = 0; j < sortie.enemyCount(); j++) {
                        if (sortie.enemy(j).serial() == rider.preySerial()) {
                            prey = sortie.enemy(j);
                        }
                    }
                }
            }
            double x = prey != null ? prey.renderX(1) : PlayField.WIDTH / 2.0;
            int fire = prey != null && Math.abs(prey.renderX(1) - sortie.ship().x()) < 6 ? Command.FIRE.bit() : 0;
            sortie.step(
                    fire | MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, 60));
            Map<Integer, Enemy> now = new HashMap<>();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                now.put(sortie.enemy(i).serial(), sortie.enemy(i));
            }
            for (Map.Entry<Integer, Integer> ride : riding.entrySet()) {
                Enemy rider = now.get(ride.getKey());
                if (rider != null && !now.containsKey(ride.getValue()) && rider.preySerial() < 0) {
                    if (rider.latchPhase() == Enemy.LatchPhase.STUNNED) {
                        thrown++;
                    } else {
                        stuck++;
                    }
                }
            }
            riding.clear();
            for (Enemy enemy : now.values()) {
                if (enemy.preySerial() >= 0) {
                    riding.put(enemy.serial(), enemy.preySerial());
                }
            }
        }
        System.out.printf(
                "Level 33 a prey killed under its leech: thrown %d at t %.1f%n", thrown, sortie.levelSeconds());
        assertTrue(thrown > 0, "a leech thrown off its dead prey");
        assertEquals(0, stuck, "a leech off its dead prey not stunned");
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level33Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
        assertTrue(first.complete());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
