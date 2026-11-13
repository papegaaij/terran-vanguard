package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;
import vanguard.sim.WaveSpec;

/**
 * M10 part F, step F3: Level 40 Honour Guard from its data (design/campaign/act-6-jovian-storm/level-40-honour-guard: the
 * Callisto approach's pickets, the Honour Guard's duel in its arena as the primary's condition, the pickets' secondary
 * {@code kill_ratio} 0.65 judged at the end, Sentry Two's recorder, the overdrive and the shield cell carried, the two
 * supply drones' armour patches), flown by the autopilot (the approach as every level's; the duel {@link
 * GuardPlay}'s: the nearest ace above for the forward guns, out of the told sniper line and off a told dive's line, the
 * rear mount on Kestrel) with the balance plan's fit ({@link #planLoadout}), with Rook and without him ({@code --escort
 * none}): every run won, the duel won in it, the secondary met when its kills reach the requirement. Scripted beside it:
 * the plan's beam frees the recorder, the drones release their patches. The duel's length, together time, conductor and
 * calibration are {@link HonourGuardFightTest}'s; the pacing, the floor, the density, the beam era's HP and the haul are
 * the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link
 * TypicalHaulTest}).
 */
class Level40Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-40-honour-guard";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The Crucible's output, MW (design/player/generator). */
    static final double OUTPUT = 27;

    /** The fit's load, MW: Level 37's 22 and the IFF spoofer's 2. */
    static final double LOAD = 24;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 40 ({@code "40"} in design/player/balance-plan.yaml: the visit buys the Mk V+
     * "Crucible" (27 MW since 2026-11-13) and the IFF spoofer (2 MW), 24 of 27 MW, and fits Rook's Autocannon) with Rook in the slot
     * (his Autocannon L1 at full armour): Level 37's guns (the Ion Beam L5, the Tesla Coil Pod L2, the right
     * Micro-missile Pod L4, the Swarm Tail L3), the Shield Mk III, Composite IV, the EMP Burst, 1 MW spare. The spoofer
     * fools nothing here (no turret; the aces are pilots): it is only its load.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        String shield = content.shields().models().get(2).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(4).name(),
                        OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, OUTPUT, LOAD, shield, difficulty));
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
     * Step F2's waves: the units counted as enemies {@link #COUNTED} per difficulty (the waves' only: no ground; the
     * Gunships' launched Darts are not planned units), the Honour Guard the one boss (a mid-boss, three aces), the
     * recorder's tow, the two drops, the carried overdrive and shield cell, and the secondary's key.
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
            counted.put(difficulty, waves + level.groundUnits().size());
            assertEquals(0, level.groundUnits().size(), at + ": no ground (NO GROUND)");
            assertEquals(1, level.setPieces().stream().filter(p -> p.isBoss()).count(), at + ": one boss");
            assertTrue(GuardWatch.hasGuard(level), at + ": the squadron mid-boss");
            assertEquals(1, level.objectives().tows().size(), at + ": the recorder's tow");
        }
        LevelData data = content.level(LEVEL);
        assertEquals(2, data.released().size(), "two supply drones");
        assertEquals(2, data.pickups().size(), "the overdrive and the shield cell carried");
        System.out.println("Level 40 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /** Step F2's waves' counts (the README's 285 at medium: the secondary's planned units; no launched Darts). */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 228, Difficulty.MEDIUM, 285, Difficulty.HARD, 332);

    /** What a run saw, over every attempt (the losses) and in its completing one (the duel, the pickups). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new java.util.ArrayList<>();

        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The pickups placed (the drones' releases) and collected in the completing attempt, by type. */
        final Map<PickupType, Integer> placed = new EnumMap<>(PickupType.class);

        final Map<PickupType, Integer> collected = new EnumMap<>(PickupType.class);
        /** The ship's shield and armour lost in the completing attempt's duel. */
        double duelLost;
        /** Whether the recorder's secret was found in the completing attempt; its line's plays. */
        boolean secret;

        int secretLines;
        private double before = Double.NaN;

        @Override
        public void newAttempt() {
            events.clear();
            placed.clear();
            collected.clear();
            duelLost = 0;
            secret = false;
            secretLines = 0;
            before = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            if (!Double.isNaN(before) && GuardPlay.squadronBoss(sortie) != null) {
                duelLost += Math.max(0, before - now);
            }
            before = now;
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                SimEvents.Type type = seen.type(i);
                events.merge(type, 1, Integer::sum);
                switch (type) {
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), seen.x(i)));
                    case PICKUP_PLACED -> placed.merge(PickupType.values()[seen.value(i)], 1, Integer::sum);
                    case PICKUP_COLLECTED -> collected.merge(PickupType.values()[seen.value(i)], 1, Integer::sum);
                    case SECRET_FOUND -> secret = true;
                    case RADIO ->
                        secretLines +=
                                sortie.script().radio().get(seen.value(i)).trigger() == CueTrigger.SECRET ? 1 : 0;
                    default -> {}
                }
            }
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
            LevelMetrics.Dashers dashers,
            int kills,
            int required) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch();
        LevelMetrics.Stacking stacking = new LevelMetrics.Stacking(content);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch, stacking);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                watch,
                stacking.dashers(),
                sortie.secondaryKills(),
                sortie.requiredKills());
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
        LevelMetrics.BossFight duel = flight.run().bosses().getFirst();
        LevelMetrics.Dashers d = flight.dashers();
        return String.format(
                "%n    %s: %s, retries %d, losses %s; duel %.1f s%s, lost %.0f in it, line hits %d, ace contacts %d;"
                        + " pickets %d / %d %s, credits %d; placed %s, collected %s; recorder %b (line %d); caps:"
                        + " Harrows %d (%d in tell or ram)",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                watch.deaths,
                duel.seconds(),
                duel.killed() ? "" : " NOT KILLED",
                watch.duelLost,
                watch.count(SimEvents.Type.BOSS_LINE_HIT),
                watch.count(SimEvents.Type.ACE_CONTACT),
                flight.kills(),
                flight.required(),
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits(),
                watch.placed,
                watch.collected,
                watch.secret,
                watch.secretLines,
                d.harrows(),
                d.harrowsTelling());
    }

    /**
     * Each run's checks: completed with the duel won in it; the secondary met exactly when its kills reach the
     * requirement (judged at the end); both drones' armour patches placed in the completing attempt.
     */
    private static void check(Flight flight) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        assertTrue(flight.run().bosses().getFirst().killed(), at + ": the duel won");
        assertEquals(flight.kills() >= flight.required(), flight.run().secondaryMet(), at + ": the pickets' secondary");
        assertEquals(2, flight.watch().placed.getOrDefault(PickupType.ARMOUR_PATCH, 0), at + ": the drones' patches");
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 40 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            check(flight);
        }
    }

    /** The autopilot completes the level with the plan's fit and Rook on every difficulty and seed. */
    @Test
    void theAutopilotBreaksTheGuardWithRook() {
        report("Rook", flights("Rook", Level40Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotBreaksTheGuardWithoutRook() {
        report("--escort none", flights("--escort none", Level40Test::withoutRook));
    }

    /**
     * Sentry Two's recorder (design Secrets; Level 30's buoy rule, {@code beam_touch}): the plan's fit frees it on medium,
     * the autopilot flying the level, under the recorder from a second before it enters until it is freed, then under
     * the crate; not invulnerable. The crate (100 in Act 1 terms) is collected and Okafor's line plays once.
     */
    @Test
    void thePlansBeamFreesTheRecorder() {
        Sortie sortie = sortie(content, planLoadout(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, false);
        Tow tow = sortie.tow(0);
        assertTrue(tow.spec().beamTouch(), "beam_touch");
        assertEquals(4, tow.spec().hits(), "the housing's hits");
        assertEquals(100, tow.spec().crateCredits(), "the crate");
        int found = 0;
        int line = 0;
        int steps = 0;
        double freedAt = Double.NaN;
        boolean collected = false;
        while (!sortie.complete() && steps++ < MAX_STEPS && !collected) {
            Pickup crate = crate(sortie);
            if (sortie.levelSeconds() < tow.spec().t() - 1) {
                sortie.step(Autopilot.commands(sortie));
            } else if (tow.holding()) {
                sortie.step(
                        steer(sortie, tow.present() ? tow.cableX() : tow.spec().x(), true));
            } else if (crate != null) {
                sortie.step(steer(sortie, crate.renderX(), false));
            } else {
                sortie.step(Autopilot.commands(sortie));
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.SECRET_FOUND) {
                    found++;
                    freedAt = sortie.levelSeconds();
                }
                if (events.type(i) == SimEvents.Type.RADIO
                        && sortie.script().radio().get(events.value(i)).trigger() == CueTrigger.SECRET) {
                    line++;
                }
            }
            collected = sortie.result().credits().secrets() > 0;
        }
        System.out.printf(
                "Level 40 recorder (the plan's beam): found %d at t %.1f, collected at t %.1f%n",
                found, freedAt, sortie.levelSeconds());
        assertEquals(1, found, "the recorder's crate freed");
        assertTrue(collected, "and collected");
        assertEquals(1, line, "Okafor's line");
    }

    private static Pickup crate(Sortie sortie) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.HIDDEN_CRATE) {
                return sortie.pickup(i);
            }
        }
        return null;
    }

    /** The scripted pilot's height, px. */
    static final double LOW = 60;

    /** Under {@code x} at {@link #LOW}, firing when {@code fire}. */
    private static int steer(Sortie sortie, double x, boolean fire) {
        int commands = MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, LOW);
        return fire ? commands | Command.FIRE.bit() : commands;
    }

    /**
     * The two CDF supply drones (design Secrets and pickups; unmarked: nobody can hit them, round 79): flying silent
     * and invulnerable, each releases its armour patch at its point and time (t=97 before the duel, t=145 after it), on every difficulty.
     */
    @Test
    void theSupplyDronesReleaseTheirPatches() {
        List<LevelData.Drop> drops = content.level(LEVEL).released();
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, planLoadout(content, difficulty), 1, difficulty, true);
            int placed = 0;
            for (int step = 0; step < MAX_STEPS && !sortie.complete(); step++) {
                // aside and silent: nothing dies but the duel's aces, which the beam has to break for the level to go
                // on
                SetPiece guard = GuardPlay.squadronBoss(sortie);
                sortie.step(
                        guard != null
                                ? Command.FIRE.bit() | GuardPlay.move(sortie, guard)
                                : steer(sortie, PlayField.WIDTH - 40.0, false));
                SimEvents seen = sortie.events();
                for (int i = 0; i < seen.size(); i++) {
                    if (seen.type(i) == SimEvents.Type.PICKUP_PLACED
                            && seen.value(i) == PickupType.ARMOUR_PATCH.ordinal()) {
                        LevelData.Drop drop = drops.get(placed++);
                        assertEquals(drop.t(), sortie.levelSeconds(), 0.1, difficulty + ": drop " + placed + "'s time");
                        assertEquals(drop.x(), seen.x(i), 1, difficulty + ": drop " + placed + "'s x");
                    }
                }
            }
            assertTrue(sortie.complete(), difficulty + ": completes");
            assertEquals(2, placed, difficulty + ": both patches released");
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level40Test::flyToEnd);

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
