package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M10 part E, step E3: Level 39 Io Flyby from its data (design/campaign/act-6-jovian-storm/level-39-io-flyby: the six
 * cracking towers in three holds, a tower passing failing the mission at once; the eruptions; the pump stations'
 * secondary {@code PUMPS n / 6}; the Striders' topples onto a tower; the survey hut's lock and the depot's latch), flown
 * by the autopilot ({@link RefineryPlay}'s line on {@link VentPlay}'s planned moves: the lowest tower first, from the
 * side away from a telling vent, never above a telling vent in its fan, the rocks as bodies, out of the Sentinels' told
 * arcs, the pumps with no tower on the screen, the EMP Burst into a crowded hold) with the balance plan's fit ({@link
 * #planLoadout}), with Rook and without him ({@code --escort none}): every run won, all six towers down in every
 * attempt, {@code PUMPS} met at its sixth pump station down (failed at the third away: {@link
 * #pumpsFailsAtTheThirdAway}), Rook's {@code first-target} line with him flying. Scripted beside it: a topple onto Pele
 * B, the hut's lock on 30 damage, the depot's armour patch, the plume field's ripple, the holds' windows, eruptions and
 * caps. The pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link
 * PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level39Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-39-io-flyby";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 39 ({@code "39"} in design/player/balance-plan.yaml: the visit buys nothing, saving
     * for the Crucible and the spoofer; Level 38's fit, 22 of 22 MW) with Rook in the slot flying his owned Mortar (L1)
     * for Io's refinery targets.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "mortar",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 38's. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level38Test.withoutRook(content, difficulty);
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

    /**
     * Step E2's waves and ground: the units counted as enemies 258 on easy, 310 at medium, 362 on hard (the waves' 232,
     * 284 and 336 and the 26 ground units: 6 cracking towers, 8 pump stations, 10 SAM Nests, 2 Sentinel Towers), the five
     * Striders as walker waves, the six towers each its own destroy-targets group.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            String at = difficulty.name();
            int waves = level.waves().stream()
                    .filter(wave -> wave.enemy().countsAsEnemy())
                    .mapToInt(WaveSpec::count)
                    .sum();
            long ground = level.groundUnits().stream()
                    .filter(unit -> unit.enemy().countsAsEnemy())
                    .count();
            counted.put(difficulty, waves + (int) ground);
            assertEquals(26, ground, at + ": ground units");
            assertEquals(6, ground(level, "cracking-tower"), at + ": towers");
            assertEquals(8, ground(level, "pump-station"), at + ": pump stations");
            assertEquals(10, ground(level, "sam-nest"), at + ": SAM Nests");
            assertEquals(2, ground(level, "sentinel-tower"), at + ": Sentinel Towers");
            assertEquals(5, count(level, "strider"), at + ": Striders");
            assertEquals(
                    List.of("Loki A", "Loki B", "Pele A", "Pele B", "Prometheus A", "Prometheus B"),
                    level.groups(),
                    at + ": the six towers' groups");
            assertEquals(3, level.objectives().holds().size(), at + ": three holds");
        }
        System.out.println("Level 39 counted enemies: " + counted);
        assertEquals(Map.of(Difficulty.EASY, 258, Difficulty.MEDIUM, 310, Difficulty.HARD, 362), counted);
    }

    /** What a run saw, over every attempt (the failed primaries, the losses) and in its completing one. */
    static final class Watch implements LevelMetrics.Probe {
        /** The mission's failures over every attempt: "t group" each. */
        final List<String> failures = new ArrayList<>();
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();
        /** Rocks that broke on the ship's hull over every attempt. */
        int rocksStruck;

        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** When each tower fell in the completing attempt, s. */
        final List<String> towers = new ArrayList<>();
        /** When the secondary was met or failed, s; -1 for not. */
        double met = -1;

        double failed = -1;
        /** The pumps destroyed when it was met, the pumps away when it failed. */
        int metAt = -1;

        int failedAt = -1;
        /** Whether the survey hut's secret was found; the depot's armour patch released and taken. */
        boolean secret;

        boolean patchReleased;
        boolean patchTaken;
        /** Rook's {@code first-target} line in the completing attempt; whether he flew (not ejected) at the first tower. */
        int firstTargetLines;

        boolean rookAtFirst;

        private final LevelScript level;
        private final int armourPatch = PickupType.ARMOUR_PATCH.ordinal();
        /** The depot's latch: its index among the level's ground objects. */
        private final int latch;

        Watch(Sortie sortie) {
            level = sortie.script();
            latch = level.groundObjects()
                    .indexOf(level.groundObjects().stream()
                            .filter(object -> object.drop().isPresent())
                            .findFirst()
                            .orElseThrow());
        }

        @Override
        public void newAttempt() {
            events.clear();
            towers.clear();
            met = -1;
            failed = -1;
            metAt = -1;
            failedAt = -1;
            secret = false;
            patchReleased = false;
            patchTaken = false;
            firstTargetLines = 0;
            rookAtFirst = false;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            for (int i = 0; i < seen.size(); i++) {
                SimEvents.Type type = seen.type(i);
                events.merge(type, 1, Integer::sum);
                switch (type) {
                    case PRIMARY_FAILED ->
                        failures.add(String.format("%.1f %s", t, level.groups().get(seen.value(i))));
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", t, seen.x(i)));
                    case ROCK_STRUCK -> rocksStruck++;
                    case GROUP_CLEARED -> {
                        if (towers.isEmpty()) {
                            rookAtFirst = sortie.wingman()
                                    .map(rook -> !rook.ejected())
                                    .orElse(false);
                        }
                        towers.add(String.format("%s %.1f", level.groups().get(seen.value(i)), t));
                    }
                    case OBJECTIVE_MET -> {
                        if (met < 0) {
                            met = t;
                            metAt = sortie.escapesDestroyed();
                        }
                    }
                    case OBJECTIVE_FAILED -> {
                        failed = t;
                        failedAt = sortie.escapesLost();
                    }
                    case SECRET_FOUND -> secret = true;
                    case GROUND_DESTROYED -> patchReleased |= seen.value(i) == latch;
                    case PICKUP_COLLECTED -> patchTaken |= seen.value(i) == armourPatch;
                    case RADIO ->
                        firstTargetLines +=
                                level.radio().get(seen.value(i)).trigger() == CueTrigger.FIRST_TARGET ? 1 : 0;
                    default -> {}
                }
            }
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
        }
    }

    /** One whole flight's measures. */
    record Flight(String fit, Difficulty difficulty, long seed, boolean complete, LevelMetrics.Run run, Watch watch) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch(sortie);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(fit, difficulty, seed, complete, LevelMetrics.Run.of(sortie), watch);
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

    private static String summary(Flight flight) {
        Watch watch = flight.watch();
        return String.format(
                "%n    %s: %s, retries %d, failed %s, losses %s, rocks struck %d; towers %s; PUMPS %s (met %.1f at %d,"
                        + " failed %.1f at %d; %d met events), credits %d; secret %b, patch %b/%b; Rook at the first"
                        + " tower %b, its line %d",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                watch.failures,
                watch.deaths,
                watch.rocksStruck,
                watch.towers,
                flight.run().secondaryMet() ? "MET" : "failed",
                watch.met,
                watch.metAt,
                watch.failed,
                watch.failedAt,
                watch.count(SimEvents.Type.OBJECTIVE_MET),
                flight.run().credits(),
                watch.secret,
                watch.patchReleased,
                watch.patchTaken,
                watch.rookAtFirst,
                watch.firstTargetLines);
    }

    /**
     * Each run's checks: completed, every tower down in every attempt (no failed primary); the secondary met at its
     * sixth pump station down, failed at its third away.
     */
    private static void check(Flight flight) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertEquals(List.of(), watch.failures, at + ": a tower passed");
        assertEquals(6, watch.towers.size(), at + ": all six towers");
        if (watch.met >= 0) {
            assertEquals(6, watch.metAt, at + ": PUMPS met at the sixth down");
        }
        if (watch.failed >= 0) {
            assertEquals(3, watch.failedAt, at + ": PUMPS failed at the third away");
        }
        assertEquals(flight.run().secondaryMet(), watch.met >= 0, at + ": the met event with the met");
        // Rook's line with him flying at the first tower (requires: escort; an ejected Rook's is dropped)
        assertEquals(watch.rookAtFirst ? 1 : 0, watch.firstTargetLines, at + ": Rook's first-target line");
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 39 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            check(flight);
        }
    }

    /**
     * The autopilot ({@link RefineryPlay}) completes the level with the plan's fit and Rook on every difficulty and
     * seed, all six towers down in every attempt.
     */
    @Test
    void theAutopilotBurnsTheRefineriesWithRook() {
        report("Rook", flights("Rook", Level39Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotBurnsTheRefineriesWithoutRook() {
        report("--escort none", flights("--escort none", Level39Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level39Test::flyToEnd);

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

    static int count(LevelScript level, String slug) {
        return level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .mapToInt(WaveSpec::count)
                .sum();
    }

    static long ground(LevelScript level, String slug) {
        return level.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals(slug))
                .count();
    }

    /** The front Ion Beam alone (L5, the plan's), for the scripted checks: its column reaches what it lines up under. */
    static Loadout beamFit(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                content.shields().models().get(2).name(),
                content.armour().plating().get(4).name(),
                Level32Test.SPARE_POWER,
                difficulty);
    }

    /** The scripted pilot's height, px: low, under everything it lines up on. */
    static final double LOW = 40;

    /** Under {@code x} at {@link #LOW}, firing when {@code fire} and lined up (the steering's dead zone). */
    private static int under(Sortie sortie, double x, boolean fire) {
        return under(sortie, x, LOW, fire);
    }

    /** Under {@code x} at {@code y}, firing when {@code fire} and lined up. */
    private static int under(Sortie sortie, double x, double y, boolean fire) {
        int commands = MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, Math.max(LOW, y));
        return fire && Math.abs(sortie.ship().x() - x) <= MovePlanner.DEAD_ZONE + 1
                ? commands | Command.FIRE.bit()
                : commands;
    }

    /** Where the scripted pilot waits, px from the left: the right edge, out of every line it does not want. */
    static final double ASIDE = PlayField.WIDTH - 40.0;

    private static Enemy groupUnit(Sortie sortie, int group) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.groupIndex() == group && enemy.hp() > 0) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * The Pele hold's topple (design Layout and Secrets: a Strider toppled onto a tower destroys it and counts): an
     * invulnerable ship with the beam alone waits aside until a Strider of the t=122.5 pair is level with Pele B (the
     * tower, held at 40 px/s, catches up with the Striders walking up the yard), then cuts its leg nearer the tower; the
     * Strider falls toward it and its crush clears Pele B's group in that step, paying the tower's bounty (30, as a shot
     * one pays) besides the Strider's whole, and Okafor's {@code topple-target} line plays once.
     */
    @Test
    void aStriderToppledOntoPeleBTakesTheTower() {
        Sortie sortie = sortie(content, beamFit(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        LevelScript level = sortie.script();
        int peleB = level.groups().indexOf("Pele B");
        Rules rules = SimSpecs.rules(content, LEVEL, Difficulty.MEDIUM);
        // a bounty's credits: times the credit factor and the level's bounty scale (Tally)
        double pay = rules.scoring().creditFactor() * rules.scoring().bountyScale();
        EnemySpec towerKind = null;
        int toppleLines = 0;
        int crushes = 0;
        boolean cleared = false;
        int paid = -1;
        int expected = -1;
        int whole = 0;
        Enemy cut = null;
        double crushAt = -1;
        for (int step = 0; step < MAX_STEPS && !sortie.complete() && sortie.levelSeconds() < 150; step++) {
            Enemy tower = groupUnit(sortie, peleB);
            Enemy strider = null;
            int leg = -1;
            if (tower != null && cut == null) {
                towerKind = tower.spec();
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (enemy.spec().slug().equals("strider")
                            && enemy.walking()
                            && sortie.levelSeconds() > 120
                            && (strider == null
                                    || Math.abs(enemy.renderY(1) - tower.renderY(1))
                                            < Math.abs(strider.renderY(1) - tower.renderY(1)))) {
                        strider = enemy;
                    }
                }
            }
            int commands = under(sortie, ASIDE, false);
            if (strider != null) {
                for (int p = 0; p < strider.partCount(); p++) {
                    if (strider.spec().parts().get(p).topples() && (leg < 0 || strider.partX(p) < strider.partX(leg))) {
                        leg = p;
                    }
                }
                double dy = tower.renderY(1) - strider.renderY(1);
                // just under it, over the Strider walking ahead of it in the same column
                commands = under(sortie, strider.partX(leg), strider.renderY(1) - BENEATH, dy > 0 && dy < LEVEL_DY);
            }
            int before = sortie.credits();
            if (strider != null) {
                whole = strider.spec().bounty();
                for (int p = 0; p < strider.partCount(); p++) {
                    whole += p != leg && strider.partAlive(p)
                            ? strider.spec().parts().get(p).firstBonus()
                            : 0;
                }
            }
            sortie.step(commands);
            SimEvents seen = sortie.events();
            boolean crushed = seen.count(SimEvents.Type.TOPPLE_CRUSH) > 0;
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case TOPPLE_TELL -> cut = strider;
                    case TOPPLE_CRUSH -> {
                        crushes++;
                        crushAt = sortie.levelSeconds();
                        paid = sortie.credits() - before;
                        expected = (int) Math.rint(towerKind.bounty() * pay) + (int) Math.rint(whole * pay);
                    }
                    case GROUP_CLEARED -> cleared |= seen.value(i) == peleB && crushed;
                    case RADIO ->
                        toppleLines += level.radio().get(seen.value(i)).trigger() == CueTrigger.TOPPLE_TARGET ? 1 : 0;
                    default -> {}
                }
            }
        }
        System.out.printf(
                "Level 39 topple onto Pele B: crush at t=%.1f, cleared %b, paid %d (tower %d + Strider %d), lines %d%n",
                crushAt, cleared, paid, towerKind == null ? -1 : towerKind.bounty(), whole, toppleLines);
        assertEquals(1, crushes, "one topple");
        assertTrue(cleared, "the crush clears Pele B's group");
        assertEquals(expected, paid, "the crush pays the tower and the Strider's whole");
        assertEquals(30, content.enemy("cracking-tower").bounty(), "the tower's bounty");
        assertEquals(1, toppleLines, "Okafor's topple-target line once");
    }

    /** How level a Strider and Pele B are when it cuts the leg, px (the fall line is 48 px wide, the tower's box 72). */
    static final double LEVEL_DY = 30;

    /** How far under the Strider it cuts the leg from, px. */
    static final double BENEATH = 70;

    /**
     * The survey hut's lock (design Secrets: a trigger spent by damage, 30 HP): the beam wears it down over several
     * steps, and only the step that takes its last HP finds the secret (the crate, 100 in Act 1 terms).
     */
    @Test
    void theSurveyHutsLockOpensOnThirtyDamage() {
        Sortie sortie = sortie(content, beamFit(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        GroundObjectSpec lock = sortie.script().groundObjects().stream()
                .filter(GroundObjectSpec::damageSpent)
                .findFirst()
                .orElseThrow();
        assertEquals(30, lock.hp(), 1e-9, "the lock's HP");
        assertEquals(100, lock.crateCredits(), "the crate");
        int worn = 0;
        double share = 1;
        double foundAt = -1;
        int found = 0;
        for (int step = 0; step < MAX_STEPS && !sortie.complete() && sortie.levelSeconds() < 110; step++) {
            GroundObject object = live(sortie, lock);
            double y = object == null ? -1 : object.renderY(1);
            boolean onField = y > 0 && y < PlayField.HEIGHT;
            sortie.step(under(sortie, onField ? object.renderX() : ASIDE, onField && !object.spent()));
            if (object != null && object.hpShare() < share) {
                worn++;
                share = object.hpShare();
            }
            int now = sortie.events().count(SimEvents.Type.SECRET_FOUND);
            if (foundAt < 0 && now > 0) {
                foundAt = sortie.levelSeconds();
                assertEquals(0, share, 1e-9, "found with its last HP");
            }
            found += now;
        }
        System.out.printf("Level 39 survey hut: worn down in %d steps, found at t=%.1f%n", worn, foundAt);
        assertEquals(1, found, "the secret found once");
        assertTrue(worn >= 3, "worn down over " + worn + " steps, not by one hit");
    }

    /** The ground object of {@code spec} on the field now; null for none. */
    private static GroundObject live(Sortie sortie, GroundObjectSpec spec) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            if (sortie.groundObject(i).spec() == spec) {
                return sortie.groundObject(i);
            }
        }
        return null;
    }

    /** The depot's latch (design Secrets: 10 HP): shot open, it lets the armour patch fall. */
    @Test
    void theDepotsLatchDropsTheArmourPatch() {
        Sortie sortie = sortie(content, beamFit(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        GroundObjectSpec latch = sortie.script().groundObjects().stream()
                .filter(object -> object.drop().isPresent())
                .findFirst()
                .orElseThrow();
        int index = sortie.script().groundObjects().indexOf(latch);
        assertEquals(PickupType.ARMOUR_PATCH, latch.drop().orElseThrow());
        boolean destroyed = false;
        boolean patch = false;
        for (int step = 0; step < MAX_STEPS && !sortie.complete() && sortie.levelSeconds() < 115; step++) {
            GroundObject object = live(sortie, latch);
            double y = object == null ? -1 : object.renderY(1);
            boolean onField = !destroyed && y > 0 && y < PlayField.HEIGHT;
            sortie.step(under(sortie, onField ? object.renderX() : ASIDE, onField));
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                destroyed |= seen.type(i) == SimEvents.Type.GROUND_DESTROYED && seen.value(i) == index;
            }
            for (int k = 0; destroyed && k < sortie.pickupCount(); k++) {
                patch |= sortie.pickup(k).type() == PickupType.ARMOUR_PATCH;
            }
        }
        assertTrue(destroyed, "the latch shot");
        assertTrue(patch, "the armour patch falls");
    }

    /**
     * The plume field (design Hazards: rows of three, rippling 0.4 s a vent): flying silent and invulnerable, each row's
     * three vents tell 0.4 s apart (one step either way) and each tells once (on the field 4.2 s, under the shortest
     * period); every other vent tells while it is on the field.
     */
    @Test
    void thePlumeFieldRipples() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, beamFit(content, difficulty), 1, difficulty, true);
            List<HazardSpec.Vent> vents =
                    sortie.eruptions().orElseThrow().spec().vents();
            Map<Integer, List<Double>> tells = new java.util.TreeMap<>();
            for (int step = 0; step < MAX_STEPS && !sortie.complete(); step++) {
                sortie.step(under(sortie, ASIDE, false));
                SimEvents seen = sortie.events();
                for (int i = 0; i < seen.size(); i++) {
                    if (seen.type(i) == SimEvents.Type.ERUPTION_TELL) {
                        tells.computeIfAbsent(seen.value(i), v -> new ArrayList<>())
                                .add(sortie.realSeconds());
                    }
                }
            }
            String at = difficulty.name();
            for (int v = 0; v < vents.size(); v++) {
                assertTrue(
                        tells.containsKey(v),
                        at + ": vent " + v + " (t=" + vents.get(v).t() + ") tells");
            }
            int rows = 0;
            for (double row : new double[] {145, 150, 155, 160}) {
                List<Integer> members = new ArrayList<>();
                for (int v = 0; v < vents.size(); v++) {
                    if (vents.get(v).t() == row) {
                        members.add(v);
                    }
                }
                assertEquals(3, members.size(), at + ": row t=" + row);
                members.sort(
                        java.util.Comparator.comparingDouble(v -> tells.get(v).getFirst()));
                for (int k = 0; k < 3; k++) {
                    assertEquals(1, tells.get(members.get(k)).size(), at + ": row t=" + row + " vent " + k + " once");
                    if (k > 0) {
                        double gap = tells.get(members.get(k)).getFirst()
                                - tells.get(members.get(k - 1)).getFirst();
                        assertEquals(0.4, gap, 1.5 * vanguard.sim.SimStep.SECONDS, at + ": row t=" + row + " ripple");
                    }
                }
                rows++;
            }
            assertEquals(4, rows, at);
        }
    }

    /**
     * The holds (design Layout; the level report's {@code refinery} part, an invulnerable pilot without Rook firing at
     * the lowest tower once it is low): three holds; each window (its start to a tower leaving the screen without a
     * shot) at least the measured one (step E2: 10.9, 8.3, 6.7 s; the README's 12.9/9.7/7.8 count the 1 s ease and the
     * tower's edge, round 77); at medium every works' vent on the field in a hold tells at least twice in it; the caps
     * (2 Striders walking, 2 Sentinels sweeping, 3 SAM Nests awake, 6 Harrows, no two medium hulls overlapping).
     */
    @Test
    void theHoldsKeepTheirWindowsAndEruptions() {
        Map<Difficulty, Double> least = Map.of(Difficulty.EASY, 10.5, Difficulty.MEDIUM, 8.0, Difficulty.HARD, 6.5);
        for (Difficulty difficulty : Difficulty.values()) {
            RefineryWatch.Refinery refinery =
                    RefineryWatch.measure(content, LEVEL, difficulty, withoutRook(content, difficulty));
            String at = difficulty.name();
            System.out.println(
                    "Level 39 holds " + at + ": windows " + refinery.windows() + ", holds " + refinery.holds());
            assertEquals(3, refinery.windows().size(), at + ": three windows");
            refinery.windows()
                    .forEach(window -> assertTrue(window >= least.get(difficulty), at + ": a window of " + window));
            assertEquals(3, refinery.holds().size(), at + ": three holds");
            if (difficulty == Difficulty.MEDIUM) {
                refinery.holds()
                        .forEach(hold -> assertTrue(
                                hold.fewest() >= 2, at + ": a works' vent told " + hold.fewest() + " times in a hold"));
            }
            assertTrue(refinery.striders() <= 2, at + ": Striders walking " + refinery.striders());
            assertTrue(refinery.sentinels() <= 2, at + ": Sentinels sweeping " + refinery.sentinels());
            assertTrue(refinery.sams() <= 3, at + ": SAM Nests awake " + refinery.sams());
            assertTrue(refinery.harrows() <= 6, at + ": Harrows " + refinery.harrows());
            assertEquals(0, refinery.bigHullOverlaps(), at + ": medium hulls overlapping");
        }
    }

    /**
     * The secondary {@code PUMPS n / 6} failed at the third pump station away: flying silent and invulnerable, every pump
     * station passes, and the step that loses the third fails it (not the second).
     */
    @Test
    void pumpsFailsAtTheThirdAway() {
        Sortie sortie = sortie(content, beamFit(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        assertEquals(8, sortie.escapesTotal(), "8 pump stations");
        int lostAt = -1;
        int failures = 0;
        for (int step = 0; step < MAX_STEPS && !sortie.complete(); step++) {
            sortie.step(under(sortie, ASIDE, false));
            if (sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED) > 0) {
                failures++;
                lostAt = sortie.escapesLost();
            }
        }
        assertEquals(1, failures, "failed once");
        assertEquals(3, lostAt, "failed at the third away");
        assertTrue(sortie.secondaryFailed());
    }
}
