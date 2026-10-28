package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EscortSwap;
import vanguard.sim.GroundUnit;
import vanguard.sim.HaltCycle;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.Wingman;
import vanguard.sim.WingmanSpec;

/**
 * M9 part B, step B3: Level 29 Betrayal at Ceres from its data (design/campaign/act-5-the-belt/level-29-betrayal-at-ceres;
 * the units in design/enemies: Talon, SAM Nest, Asteroid Mite, clamp gun, wavering Stormhawk): its count, its seven
 * sections (the rescue a held arena whose holders are the four clamp guns), the halt cycle, Block C's dormant nests, the
 * V-wings' leaders and their scatter, the Asteroid Mites riding their rocks, the wavering Stormhawks (neutral craft) and
 * the "Hold fire" secondary, Rook's rescue (the escort swap: the loaner Bulwark leaves, Rook joins with the gun fitted at
 * his loss at full armour; Rook from the start opens the hold empty, no escort lets the Ember leave), the radio's event
 * lines, and whole runs of the autopilot with the balance plan's fit ({@link HoldFirePlay}: it never lines up under a
 * neutral craft and holds its fire while one is in its guns' band) with the loaner and without an escort. The pacing, the
 * floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link
 * ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the mechanics themselves the
 * simulation's ({@code HeldArenaTest}, {@code HaltCycleTest}, {@code DormantTest}, {@code SquadLeaderTest}, {@code
 * RockRidingTest}, {@code HoldFireTest}, {@code EscortSwapTest}).
 */
class Level29Test {
    static final String LEVEL = "act-5-the-belt/level-29-betrayal-at-ceres";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The plan's load at the L29 visit (BalanceTest's sheet): 18 of the Mk IV's 20 MW. */
    static final double SPARE_POWER = 2;
    /** The halt: the clock stands at the rescue's end while a clamp gun stands. */
    static final double HALT = 120;
    /** The halt cycle's period, s. */
    static final double CYCLE = 16;
    /** Step B3b: the typical halt at medium with the plan's fit, s (the README's about 14 s; one cycle of its waves). */
    static final double HALT_LEAST = 10;

    static final double HALT_MOST = 18;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 29 ({@code "29"}: Level 28's with the Ion Beam L4 in front for the harpoon, the
     * EMP Burst for the Sonar Pulse and no Pressure hull): both Micro-missile Pods L3, the Tail Gun L3, the Mk III shield,
     * Composite III, the Mk IV's spare 2 MW, the loaner Bulwark in the escort slot and Rook waiting in the prison
     * tender's hold with his Autocannon (the gun fitted at his loss in Level 26) at full armour.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutBulwark(content, difficulty)
                .withBulwark(SimSpecs.bulwark(
                        content,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().bulwark().armour()))
                .withRescue(rook(content));
    }

    /** Rook as the plan has him at his loss (Level 26): the Autocannon L1, here at full armour. */
    static WingmanSpec rook(Content content) {
        return SimSpecs.wingman(
                content,
                "autocannon",
                1,
                WingmanSpec.Side.LEFT,
                content.wingmen().rook().armour());
    }

    /** The plan's fit without an escort ({@code --escort none}: no rescue, the Ember leaves). */
    static Loadout withoutBulwark(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 3)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(3).name(),
                        SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1));
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty, boolean invulnerable) {
        return sortie(content, planLoadout(content, difficulty), seed, difficulty, invulnerable);
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

    private static int count(List<WaveSpec> waves, String slug) {
        return waves.stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .mapToInt(WaveSpec::count)
                .sum();
    }

    private static long count(List<GroundUnit> ground, String slug, java.util.function.Predicate<GroundUnit> which) {
        return ground.stream()
                .filter(unit -> unit.enemy().slug().equals(slug) && which.test(unit))
                .count();
    }

    /**
     * The README's waves at medium (step B3b's rework, round 62's rock groups: step B6): Skitter 40, Mote Swarm 96 (most
     * on long loops), Asteroid Mite 48 (seven groups of 3-5 rocks, twelve waves on rocks: 1-2 riders a rock, a group of
     * mixed riders two waves), Talon 138 (seven V-wings led from the tip, the offbeat pairs and lines, all at 130 px/s)
     * and the six wavering Stormhawks (three pairs on every difficulty); the halt cycle's 22 a cycle (Skitter 12, Talon
     * 4, Mote Swarm 6). Easy and hard by the formation lever (the Mites' rocks × riders on every difficulty), easy
     * without the offbeat Talons in the wavering pairs' holds, easy's cycle without its Talons, hard's with a second
     * line (15 and 31). On the ground 27 SAM Nests (Block C's three
     * dormant until t=60, step B3b's six pairs on the canyon walls) and the four clamp guns, the held arena's holders,
     * dormant until the halt, on every difficulty; the supply cache's secret.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        String[] slugs = {"skitter", "mote-swarm", "asteroid-mite", "talon", "wavering-stormhawk"};
        int[][] counts = {{27, 80, 48, 106, 6}, {40, 96, 48, 138, 6}, {50, 112, 48, 163, 6}};
        int[] cycles = {15, 22, 31};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<WaveSpec> waves = level.waves();
            for (int i = 0; i < slugs.length; i++) {
                assertEquals(counts[difficulty.ordinal()][i], count(waves, slugs[i]), difficulty + " " + slugs[i]);
            }
            HaltCycle cycle = level.haltCycle().orElseThrow();
            assertEquals(CYCLE, cycle.everySeconds(), 1e-9);
            assertEquals(
                    cycles[difficulty.ordinal()],
                    cycle.waves().stream().mapToInt(WaveSpec::count).sum(),
                    difficulty.name());
            assertEquals(
                    7, waves.stream().filter(wave -> wave.leader().isPresent()).count(), difficulty + ": leaders");
            assertEquals(
                    12,
                    waves.stream().filter(wave -> wave.onRocks().isPresent()).count(),
                    difficulty + ": rocks");
            List<GroundUnit> ground = level.groundUnits();
            assertEquals(27, count(ground, "sam-nest", unit -> true), difficulty.name());
            assertEquals(3, count(ground, "sam-nest", GroundUnit::dormant), difficulty.name());
            assertEquals(4, count(ground, "clamp-gun", GroundUnit::holder), difficulty.name());
            assertEquals(4, count(ground, "clamp-gun", GroundUnit::dormant), difficulty + ": asleep until the halt");
            assertEquals(4, count(ground, "clamp-gun", unit -> true), difficulty.name());
            assertEquals(1, level.secrets());
        }
        EnemyData neutral = content.enemies().get("wavering-stormhawk");
        assertTrue(neutral != null, "the wavering Stormhawk's unit data");
        assertFalse(
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).waves().stream()
                        .filter(wave -> wave.enemy().slug().equals("wavering-stormhawk"))
                        .anyMatch(wave -> wave.enemy().countsAsEnemy()),
                "the wavering pairs are no enemies");
    }

    /**
     * Seven sections: the approach at 120 px/s to 28 s, the dock canyon at 100 to 56 s, Block C at 60 to 76 s, the red
     * canyon at 100 to 115 s, Dock 9 at 100 to 118 s, the rescue (a held arena: speed 0, its clock held by the clamp
     * guns) to 120 s and the breakout at 120 to 172 s, each change eased over 2 s.
     */
    @Test
    void theRescueIsAHeldArena() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(28.0, 56.0, 76.0, 115.0, 118.0, 120.0, 172.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(120.0, 100.0, 60.0, 100.0, 100.0, 0.0, 120.0),
                sections.stream().map(Section::speed).toList());
        Section rescue = sections.get(5);
        assertTrue(rescue.arena(), "the rescue is the arena");
        assertTrue(level.setPieces().stream().noneMatch(vanguard.sim.SetPieceSpec::isBoss), "no boss");
        assertEquals(2, sections.get(6).rampSeconds(), 1e-9, "the breakout's ramp");
        assertTrue(level.escortSwap().isPresent(), "Rook's rescue at the held arena");
    }

    /**
     * Flown without a shot (invulnerable): Block C's three nests stand dark (not hittable, no fire) until t=60 and wake
     * then; the clock stops at t=120 and stands while the clamp guns stand, the halt cycle's waves coming every 16 s on
     * the halt's own clock (the first at H+1); the loaner stays and Rook waits in the hold.
     */
    @Test
    void theNestsSleepUntilTheTurnAndTheClampsHoldTheClock() {
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        int steps = 0;
        boolean dormantSeen = false;
        while (sortie.levelSeconds() < 59.5 && steps++ < MAX_STEPS) {
            sortie.step(0);
            dormantSeen |= dormantNests(sortie) == 3;
        }
        assertTrue(dormantSeen, "three dormant nests on Block C before the turn");
        assertEquals(3, dormantNests(sortie), "still asleep at 59.5 s");
        int woke = 0;
        while (sortie.levelSeconds() < 62 && steps++ < MAX_STEPS) {
            sortie.step(0);
            woke += sortie.events().count(SimEvents.Type.WOKE);
        }
        assertEquals(3, woke, "the three wake at the turn");
        assertEquals(0, dormantNests(sortie));

        boolean clampsAsleep = false;
        while (!sortie.halting() && steps++ < MAX_STEPS) {
            sortie.step(0);
            clampsAsleep |= sortie.levelSeconds() > 119 && dormantNests(sortie) == 4;
        }
        assertTrue(clampsAsleep, "the four clamp guns asleep on the way in (step B3b)");
        assertEquals(HALT, sortie.levelSeconds(), 0.05, "the halt at the rescue's end");
        assertEquals(4, sortie.holders());
        // The halt cycle's Skitter snakes (H+1 and H+13 in each 16 s cycle) in three cycles.
        java.util.Set<Integer> skitters = new java.util.HashSet<>();
        int[] perCycle = new int[3];
        for (int i = 0; i < SimStep.ticks(48); i++) {
            sortie.step(0);
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                if (enemy.spec().slug().equals("skitter") && skitters.add(enemy.serial())) {
                    perCycle[Math.min(2, (int) (sortie.haltSeconds() / CYCLE))]++;
                }
            }
        }
        assertTrue(sortie.halting(), "held while the clamps stand");
        assertEquals(HALT, sortie.levelSeconds(), 0.05, "the clock stands");
        assertEquals(4, sortie.holdersStanding());
        assertEquals(0, dormantNests(sortie), "the clamp guns woke at the halt");
        assertEquals(
                List.of(12, 12, 12), java.util.Arrays.stream(perCycle).boxed().toList(), "two snakes a cycle");
        assertTrue(sortie.wingman().orElseThrow().waiting(), "Rook waits in the hold");
        assertFalse(sortie.bulwark().orElseThrow().departing(), "the loaner flies on");
    }

    private static int dormantNests(Sortie sortie) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            n += sortie.enemy(i).dormant() ? 1 : 0;
        }
        return n;
    }

    /** What one completing attempt saw: the events by type, the halt, Rook at the swap. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        double halt;
        double rookArmour = Double.NaN;
        String rookGun = "";
        int swapKind = -1;
        boolean riding;
        boolean leader;
        /** Step B4b: the lines the radio started at the last clamp ({@code holders-down}). */
        final List<String> holdersLines = new java.util.ArrayList<>();
        /** Step B4b: whether the Bulwark in the slot was destroyed before the last clamp fell. */
        boolean bulwarkLost;
        /**
         * Step B6 (round 62, the Rook-barks bug): the steps Rook flew in his slot (neither waiting in the hold nor
         * joining: the level screen's gate for his barks) before he joined, and the steps out of it after.
         */
        int inSlotBeforeJoin;

        int outOfSlotAfterJoin;
        private boolean joined;

        @Override
        public void newAttempt() {
            events.clear();
            halt = 0;
            rookArmour = Double.NaN;
            swapKind = -1;
            riding = false;
            leader = false;
            holdersLines.clear();
            bulwarkLost = false;
            inSlotBeforeJoin = 0;
            outOfSlotAfterJoin = 0;
            joined = false;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            boolean released = events.containsKey(SimEvents.Type.HOLDERS_DOWN);
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                if (seen.type(i) == SimEvents.Type.BULWARK_DESTROYED && !released) {
                    bulwarkLost = true;
                }
                if (seen.type(i) == SimEvents.Type.RADIO) {
                    RadioCue cue = sortie.script().radio().get(seen.value(i));
                    if (cue.trigger() == CueTrigger.HOLDERS_DOWN) {
                        holdersLines.add(cue.line());
                    }
                }
            }
            halt = Math.max(halt, sortie.haltSeconds());
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                riding |= enemy.riding();
                leader |= enemy.leader();
            }
            if (seen.count(SimEvents.Type.ESCORT_JOINED) > 0) {
                Wingman rook = sortie.wingman().orElseThrow();
                rookArmour = rook.armour();
                rookGun = rook.gun().slug();
                joined = true;
            }
            if (sortie.wingman().isPresent()) {
                Wingman rook = sortie.wingman().get();
                boolean inSlot = !rook.waiting() && !rook.joining();
                if (joined && !inSlot) {
                    outOfSlotAfterJoin++;
                } else if (!joined && inSlot) {
                    inSlotBeforeJoin++;
                }
            }
            if (seen.count(SimEvents.Type.ESCORT_SWAP) > 0) {
                swapKind = sortie.escortSwap().kind();
            }
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
        }
    }

    /**
     * The autopilot completes the level with the plan's fit, the loaner and Rook's rescue on every difficulty and seed,
     * within the retries the other levels take, and keeps the six wavering Stormhawks alive (Hold fire met) in most runs.
     * On the attempt that completes it: the halt ends at the last clamp gun, the loaner peels away and Rook joins at
     * full armour with his Autocannon; the leaders' wings scatter, the Mites ride and leap, the nests wake, the SAM
     * Nests' homing diamonds fly; at medium the halt lasts 10-18 s on average (the four clamp guns asleep until it, 1,000 HP
     * together: step B3b). Printed: the secondary's rate, the credits, the retries, the halt.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 29 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            double halts = 0;
            StringBuilder seen = new StringBuilder();
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                Watch watch = new Watch();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
                halts += watch.halt;
                String at = difficulty + " seed " + seed;
                assertEquals(1, watch.count(SimEvents.Type.HOLDERS_DOWN), at);
                assertEquals(EscortSwap.JOINS, watch.swapKind, at);
                assertEquals(1, watch.count(SimEvents.Type.ESCORT_JOINED), at + ": Rook joins");
                // Step B6 (round 62): his barks' gate (LevelScreen#rookInSlot) opens exactly as he joins his slot.
                assertEquals(0, watch.inSlotBeforeJoin, at + ": in the slot before he joined");
                assertEquals(0, watch.outOfSlotAfterJoin, at + ": out of the slot after he joined");
                if (difficulty == Difficulty.HARD) {
                    // Step B3b (pending round 62): hard's halt (17-21 s under the clamp guns' aimed pairs, the Bulwark
                    // drawing 30 % of the aimed fire) can wear the loaner down before the swap.
                    assertEquals(
                            1,
                            watch.count(SimEvents.Type.BULWARK_DEPARTED)
                                    + watch.count(SimEvents.Type.BULWARK_DESTROYED),
                            at + ": the loaner leaves or falls");
                } else {
                    assertEquals(1, watch.count(SimEvents.Type.BULWARK_DEPARTED), at + ": the loaner leaves");
                }
                // Step B4b: "Bulwark, RTB" only with the loaner still flying at the last clamp; none after its loss.
                assertEquals(watch.bulwarkLost ? List.of() : List.of(RTB), watch.holdersLines, at);
                assertEquals(content.wingmen().rook().armour(), watch.rookArmour, 1e-9, at + ": full armour");
                assertEquals(7, watch.count(SimEvents.Type.WOKE), at + ": three nests and four clamp guns");
                assertTrue(watch.riding && watch.leader, at);
                seen.append(String.format(
                        "%n    seed %d: scatter %d, tells %d, leaps %d, left %d, down %d, halt %.1f s, Rook %s",
                        seed,
                        watch.count(SimEvents.Type.SQUAD_SCATTER),
                        watch.count(SimEvents.Type.RIDER_TELL),
                        watch.count(SimEvents.Type.RIDER_LEAP),
                        watch.count(SimEvents.Type.NEUTRAL_LEFT),
                        watch.count(SimEvents.Type.NEUTRAL_DOWN),
                        watch.halt,
                        watch.rookGun));
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d, halt %.1f s%s",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries, halts / SEEDS, seen));
            assertTrue(met > SEEDS / 2, difficulty + ": Hold fire met in most runs, " + met + "/" + SEEDS);
            if (difficulty == Difficulty.MEDIUM) {
                double halt = halts / SEEDS;
                assertTrue(halt >= HALT_LEAST && halt <= HALT_MOST, "the typical halt at medium: " + halt + " s");
            }
        }
        System.out.println(report);
    }

    /**
     * Rook from the start ({@code --escort rook:}): the hold opens empty; without an escort ({@code --escort none}) the
     * Ember launches and leaves. The autopilot completes the level both ways, one seed per difficulty.
     */
    @Test
    void theDebugEscortsFlyTheLevel() {
        for (Difficulty difficulty : Difficulty.values()) {
            Loadout rook = withoutBulwark(content, difficulty).withWingman(rook(content));
            Loadout none = withoutBulwark(content, difficulty);
            int[] kinds = {EscortSwap.EMPTY, EscortSwap.LEAVES};
            Loadout[] loadouts = {rook, none};
            for (int k = 0; k < loadouts.length; k++) {
                Sortie sortie = sortie(content, loadouts[k], 1, difficulty, false);
                Watch watch = new Watch();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch), difficulty + " " + k);
                assertEquals(kinds[k], watch.swapKind, difficulty + " " + k);
                assertEquals(0, watch.count(SimEvents.Type.ESCORT_JOINED), difficulty + " " + k);
                // Step B4b: Rook from the start has no line at the last clamp; without an escort, Okafor's other one.
                assertEquals(k == 0 ? List.of() : List.of(CLEAR), watch.holdersLines, difficulty + " " + k);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                System.out.printf(
                        "Level 29 %s, %s: secondary met %s, credits %d, retries %d%n",
                        k == 0 ? "--escort rook" : "--escort none",
                        difficulty,
                        run.secondaryMet(),
                        run.credits(),
                        run.retries());
            }
        }
    }

    /** Step B4b: Okafor's line at the last clamp with the Bulwark flying in the slot. */
    static final String RTB = "Bulwark, RTB. Rook, form on Lancer.";

    /** Step B4b: his line at the last clamp with no Rook to join ({@code --escort none}: the Ember leaves upward). */
    static final String CLEAR = "Rook, you're clear. Form on the transport.";

    /**
     * Step B4b: a loaner worn down before the swap (here at 1 armour, so the first shots take it; medium) drops "Bulwark, RTB"
     * at the last clamp, and Rook still joins at full armour; the same flight with the loaner at its armour plays it.
     */
    @Test
    void theLastClampsLineFollowsTheLoaner() {
        for (double armour : new double[] {1, content.wingmen().bulwark().armour()}) {
            Loadout loadout = withoutBulwark(content, Difficulty.MEDIUM)
                    .withBulwark(SimSpecs.bulwark(content, WingmanSpec.Side.LEFT, armour))
                    .withRescue(rook(content));
            Sortie sortie = sortie(content, loadout, 1, Difficulty.MEDIUM, false);
            Watch watch = new Watch();
            assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch), "armour " + armour);
            String at = "the loaner at " + armour + " armour";
            assertEquals(armour == 1, watch.bulwarkLost, at + ": lost before the swap");
            assertEquals(armour == 1 ? List.of() : List.of(RTB), watch.holdersLines, at);
            assertEquals(1, watch.count(SimEvents.Type.ESCORT_JOINED), at + ": Rook joins");
        }
    }

    /**
     * The radio: no Rook line before the rescue (no {@code requires: escort} bark; his first line is on {@code
     * escort-joined}); the event lines of the part (the first homing launch, a wavering pair leaving, one destroyed, the
     * last clamp gun, Rook joining) each once; Harker's lines on the enemy channel (distorted).
     */
    @Test
    void theRadioHasThePartsEventLines() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<RadioCue> radio = SimSpecs.level(content, LEVEL, difficulty).radio();
            // Step B4b: two lines at the last clamp, one for the Bulwark flying and one for no Rook to join.
            assertEquals(
                    List.of(EscortSwap.SUBJECT_BULWARK, EscortSwap.SUBJECT_LEAVES),
                    radio.stream()
                            .filter(cue -> cue.trigger() == CueTrigger.HOLDERS_DOWN)
                            .map(RadioCue::subject)
                            .toList(),
                    difficulty + ": holders-down");
            for (CueTrigger trigger : List.of(
                    CueTrigger.FIRST_HOMING,
                    CueTrigger.NEUTRAL_LEFT,
                    CueTrigger.NEUTRAL_DOWN,
                    CueTrigger.ESCORT_JOINED,
                    CueTrigger.SECONDARY_OBJECTIVE,
                    CueTrigger.LEVEL_END)) {
                assertEquals(
                        1,
                        radio.stream().filter(cue -> cue.trigger() == trigger).count(),
                        difficulty + ": " + trigger);
            }
            RadioCue joined = radio.stream()
                    .filter(cue -> cue.trigger() == CueTrigger.ESCORT_JOINED)
                    .findFirst()
                    .orElseThrow();
            assertEquals("Rook", joined.speaker());
            for (RadioCue cue : radio) {
                if (cue.speaker().equals("Rook")) {
                    assertTrue(
                            cue.trigger() == CueTrigger.ESCORT_JOINED
                                    || (cue.trigger() == CueTrigger.TIME && cue.t() > HALT),
                            difficulty + ": a Rook line before the rescue: " + cue.line());
                    assertEquals(0, cue.requires() & RadioCue.FITTED_ESCORT, "his lines are scripted: " + cue.line());
                }
                // Harker's lines (and only his and Vorne's) come over the enemy channel (his voice's filter).
                assertEquals(
                        cue.speaker().equals("Harker"),
                        content.voices().channelOf(cue.speaker()) == Channel.ENEMY,
                        cue.speaker() + ": " + cue.line());
            }
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level29Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM, false);
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
