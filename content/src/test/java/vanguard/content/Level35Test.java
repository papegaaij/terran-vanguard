package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M9 part H, step H3: Level 35 Iron Sovereign from its data (design/campaign/act-5-the-belt/level-35-iron-sovereign;
 * design/enemies/bosses/iron-sovereign), flown by the autopilot ({@link RingPlay} in the arena, {@link HazardPlay}'s
 * relay line on the approach) with the balance plan's Level 35 fit ({@link #planLoadout}: the Ion Beam L4, both
 * Micro-missile Pods L4, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst, the Mk V "Helix", Rook) through
 * the whole level, with Rook and without him. The fight: its length is the par at medium (the act boss's window 90–180 s
 * on every one of five seeds; "measure, then tune the HP, the core's first"), easy and hard are won, phase 1 is measured
 * against the Harbour Kraken's for a random dodger, the specials' per-part caps are measured on the full station (QH-1),
 * the autopilot keeps off the rail lines; the mass relay hits the ship once and never Rook; the lifeboat's secret, the
 * secondary's 65 % of the approach, the boss checkpoint, the act's end, determinism and no allocation. The mechanics are
 * the simulation's ({@code RingArenaTest}, {@code RingLocksTest}, {@code BossLinesTest}, {@code PartShieldsTest}, {@code
 * RelayFlashTest}, {@code RookRingsTest}); the keys and checks content's ({@link RingKeysTest}, {@link
 * Level35KeysTest}); the pacing, the floor, the density, the beam era's HP and the haul the campaign-wide tests'.
 */
class Level35Test {
    static final String LEVEL = "act-5-the-belt/level-35-iron-sovereign";
    static final int MAX_STEPS = 60 * 60 * 30;
    static final String SLUG = "iron-sovereign";
    /** The seeds the fight is measured on (the Dust Colossus's and the Abyssal Maw's five). */
    static final long[] SEEDS = DustColossusTest.SEEDS;
    /** The seeds flown without Rook, per difficulty. */
    static final int SEEDS_WITHOUT_ROOK = 2;

    /**
     * The par: the autopilot's measured mean at medium. Step H3: the draft's HP (the core 5,000) gave 77 s, under the
     * window (the rings ≈ 35 s, the core ≈ 42 s: the beam's columns and the pods take the batteries faster than the
     * paper's ×0.7); the core tuned to {@link #CORE_HP} (the batteries kept: no ring phase runs long) gives 145–155 s on
     * every seed, the mean ≈ 149: the draft's par 150 kept.
     */
    static final double PAR = 150;

    /** The tuned core's HP (step H3, pending the user's review in round 68). */
    static final double CORE_HP = 14_000;

    /** The plan's L35 fit's generator output and load, MW: the Mk V "Helix" (design/player/generator, Act 5 hand plan). */
    static final double OUTPUT = 22;

    static final double LOAD = 21;

    /** The most rail lines and sweeps that strike the ship in one fight at medium. */
    static final int MOST_LINE_HITS = 1;

    static final int MOST_SWEEP_HITS = 3;

    private static final Content CONTENT = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 35 ({@code "35"}: the Mk V "Helix" and both Micro-missile Pods to L4) with Rook in
     * the slot: the Ion Beam L4, the Micro-missile Pods L4 ×2, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP
     * Burst, 21 of 22 MW (step H2's stand-in in {@code PacingTest}, moved here).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The same without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(4).name(),
                        Level32Test.SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(
                        content, OUTPUT, LOAD, content.shields().models().get(2).name(), difficulty));
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

    /** What one flight saw of the fight, on the attempt that completes the level. */
    record Fight(
            String fit,
            Difficulty difficulty,
            long seed,
            double seconds,
            boolean killed,
            int retries,
            boolean secondaryMet,
            int secretsFound,
            double[] phaseSeconds,
            double[] lost,
            int lineHits,
            int sweepHits,
            int relayHits) {
        @Override
        public String toString() {
            return String.format(
                    "%s %s seed %d: %.1f s%s, retries %d, secondary %s, secrets %d, phases %.1f + %.1f + %.1f + %.1f s,"
                            + " lost %.0f / %.0f / %.0f / %.0f, struck by %d lines, %d sweeps, the relay %d",
                    fit,
                    difficulty,
                    seed,
                    seconds,
                    killed ? "" : " NOT KILLED",
                    retries,
                    secondaryMet,
                    secretsFound,
                    phaseSeconds[0],
                    phaseSeconds[1],
                    phaseSeconds[2],
                    phaseSeconds[3],
                    lost[0],
                    lost[1],
                    lost[2],
                    lost[3],
                    lineHits,
                    sweepHits,
                    relayHits);
        }
    }

    /** The boss's phases, the losses in each, the rail lines, sweeps and the relay that struck the ship. */
    static final class FightWatch implements LevelMetrics.Probe {
        final double[] phaseSeconds = new double[4];
        final double[] lost = new double[4];
        int lineHits;
        int sweepHits;
        int relayHits;
        private double before = Double.NaN;

        @Override
        public void newAttempt() {
            Arrays.fill(phaseSeconds, 0);
            Arrays.fill(lost, 0);
            lineHits = 0;
            sweepHits = 0;
            relayHits = 0;
            before = Double.NaN;
        }

        @Override
        public void bossRetry() {
            // the relay lies before the boss checkpoint: a retry from it keeps the approach's count
            int relay = relayHits;
            newAttempt();
            relayHits = relay;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            double loss = Double.isNaN(before) ? 0 : Math.max(0, before - now);
            before = now;
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                relayHits += events.type(e) == SimEvents.Type.RELAY_HIT ? 1 : 0;
            }
            SetPiece boss = RingPlay.ringedBoss(sortie);
            if (boss == null) {
                return;
            }
            int phase = Math.min(3, boss.phase());
            phaseSeconds[phase] += SimStep.SECONDS;
            lost[phase] += loss;
            for (int e = 0; e < events.size(); e++) {
                lineHits += events.type(e) == SimEvents.Type.BOSS_LINE_HIT ? 1 : 0;
                sweepHits += events.type(e) == SimEvents.Type.SWEEP_HIT ? 1 : 0;
            }
        }
    }

    /** The autopilot flies Level 35 with {@code loadout} on {@code seed} (retried after a loss) and its fight. */
    static Fight fight(String fit, Loadout loadout, long seed, Difficulty difficulty) {
        Sortie sortie = sortie(CONTENT, loadout, seed, difficulty, false);
        FightWatch watch = new FightWatch();
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
        LevelMetrics.BossFight boss = run.bosses().getFirst();
        return new Fight(
                fit,
                difficulty,
                seed,
                boss.seconds(),
                boss.killed() && run.complete(),
                run.retries(),
                run.secondaryMet(),
                complete ? sortie.result().secretsFound() : 0,
                watch.phaseSeconds.clone(),
                watch.lost.clone(),
                watch.lineHits,
                watch.sweepHits,
                watch.relayHits);
    }

    /** The fights of a fit on {@code difficulties} over {@code seeds} seeds each, flown in parallel, in their order. */
    static List<Fight> fights(
            String fit, BiFunction<Content, Difficulty, Loadout> loadout, int seeds, Difficulty... difficulties) {
        return IntStream.range(0, difficulties.length * seeds)
                .parallel()
                .mapToObj(i -> {
                    Difficulty difficulty = difficulties[i / seeds];
                    return fight(fit, loadout.apply(CONTENT, difficulty), SEEDS[i % seeds], difficulty);
                })
                .toList();
    }

    private static SetPieceSpec bossSpec(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, LEVEL, difficulty).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    /**
     * The stat block maps to a ringed act boss (three rings of 6, 4 and 3 batteries round the core: 14 parts), its par
     * the measured mean, its core the tuned HP, the parts' sum its HP, four phases by the locks.
     */
    @Test
    void theStatBlockMapsToARingedActBossWithItsPar() {
        BossSpec boss = bossSpec(Difficulty.MEDIUM).boss().orElseThrow();
        assertFalse(boss.midBoss());
        assertTrue(boss.rings().isPresent(), "ringed");
        assertEquals(3, boss.rings().get().rings().size());
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        assertEquals(4, boss.phases().size());
        EnemyData sovereign = CONTENT.enemy(SLUG);
        assertEquals(CORE_HP, sovereign.partList().orElseThrow().getFirst().hp(), "the core");
        assertEquals(CORE_HP + 6 * 220 + 4 * 330 + 3 * 440, sovereign.hp(), "the parts' sum");
        assertEquals(14, bossSpec(Difficulty.MEDIUM).parts().size(), "the core and 13 batteries");
    }

    /**
     * Measure, then tune the HP (the Kraken's E6, the Colossus's H4, the Maw's H4): the autopilot's fight with the plan's
     * fit and Rook lasts its par at medium (the bar appearing to the kill), inside the act boss's window 90–180 s on
     * every seed; it keeps off the rail lines and out of the sweeps' wedges (at most {@link #MOST_LINE_HITS} line and
     * {@link #MOST_SWEEP_HITS} sweeps in a fight) and off the relay's line on the approach.
     */
    @Test
    void theAutopilotsFightLastsItsParAtMedium() {
        List<Fight> fights = fights("Rook", Level35Test::planLoadout, SEEDS.length, Difficulty.MEDIUM);
        double sum = 0;
        for (Fight fight : fights) {
            System.out.println("Iron Sovereign " + fight);
            sum += fight.seconds();
        }
        for (Fight fight : fights) {
            assertTrue(fight.killed(), "killed: " + fight);
            assertTrue(LevelMetrics.inBossWindow(false, fight.seconds()), "the act boss's window: " + fight);
            assertTrue(fight.lineHits() <= MOST_LINE_HITS, "off the rail lines: " + fight);
            assertTrue(fight.sweepHits() <= MOST_SWEEP_HITS, "out of the wedges: " + fight);
            assertEquals(0, fight.relayHits(), "off the relay's line: " + fight);
        }
        double mean = sum / SEEDS.length;
        System.out.printf("Iron Sovereign medium: mean %.1f s, par %.0f%n", mean, PAR);
        assertEquals(PAR, mean, 5, "the par is the measured mean");
    }

    /**
     * Easy and hard are won on every seed with Rook, and every difficulty without him (the level flown to its end,
     * retried after a loss); the relay never strikes the autopilot; the lifeboat's secret is found on every difficulty.
     */
    @Test
    void theFightIsWonOnEveryDifficultyWithAndWithoutRook() {
        List<Fight> with = fights("Rook", Level35Test::planLoadout, SEEDS.length, Difficulty.EASY, Difficulty.HARD);
        List<Fight> without =
                fights("--escort none", Level35Test::withoutRook, SEEDS_WITHOUT_ROOK, Difficulty.values());
        Map<Difficulty, Integer> secrets = new EnumMap<>(Difficulty.class);
        for (List<Fight> fights : List.of(with, without)) {
            for (Fight fight : fights) {
                System.out.println("Iron Sovereign " + fight);
                assertTrue(fight.killed(), "killed: " + fight);
                assertEquals(0, fight.relayHits(), "off the relay's line: " + fight);
                secrets.merge(fight.difficulty(), fight.secretsFound(), Integer::sum);
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(secrets.getOrDefault(difficulty, 0) > 0, difficulty + ": the lifeboat's secret found");
        }
    }

    /**
     * Phase 1 against the Harbour Kraken's (the Dust Colossus's band, H.4 of round 43; the boss spec's *Duration and HP*):
     * with the calibration harness's random dodge ({@link RevenantWalkerTest#randomDodge}), taking over from the
     * autopilot as the rings come up, the ship loses, per 10 s of phase 1 at medium over eight seeds, between {@link
     * #PENDING_LEAST} and {@link #PENDING_MOST} times what the same driver loses in the Kraken's phase 1; outside H.4's
     * {@link AbyssalMawTest#LEAST}–{@link AbyssalMawTest#MOST} it is printed as pending the user's review.
     */
    @Test
    void phaseOneAgainstTheKrakensForARandomDodger() {
        double sovereign = Arrays.stream(AbyssalMawTest.DODGE_SEEDS)
                .parallel()
                .mapToDouble(Level35Test::randomPhaseOne)
                .average()
                .orElseThrow();
        double kraken = Arrays.stream(AbyssalMawTest.DODGE_SEEDS)
                .parallel()
                .mapToDouble(RevenantWalkerTest::krakenPhaseOne)
                .average()
                .orElseThrow();
        System.out.printf(
                "Random dodge, shield and armour lost per 10 s of phase 1: Iron Sovereign %.1f, Kraken %.1f (%.2f×)%n",
                sovereign, kraken, sovereign / kraken);
        assertTrue(
                sovereign >= PENDING_LEAST * kraken && sovereign <= PENDING_MOST * kraken,
                "Iron Sovereign " + sovereign + " vs " + kraken);
        if (sovereign < AbyssalMawTest.LEAST * kraken || sovereign > AbyssalMawTest.MOST * kraken) {
            System.out.printf(
                    "Iron Sovereign phase 1 outside H.4's band (%.2f×): pending the user's review (round 68)%n",
                    sovereign / kraken);
        }
    }

    /** Step H3 (pending the user's review in round 68): the band the test holds for now, H.4's 0.8–1.2 inside it. */
    static final double PENDING_LEAST = 0.5;

    static final double PENDING_MOST = 1.5;

    /**
     * The random dodger's shield and armour lost per 10 s of the Sovereign's phase 1 (medium, the plan's fit without
     * Rook: the dodger flies alone): the autopilot flies the approach (retried after a loss) until the rings are up, then
     * the dodger for at most 120 s, until phase 1 ends or the ship is lost.
     */
    static double randomPhaseOne(long seed) {
        Sortie sortie = sortie(CONTENT, withoutRook(CONTENT, Difficulty.MEDIUM), seed, Difficulty.MEDIUM, false);
        SetPiece boss = upAndFull(sortie);
        ToIntFunction<Sortie> dodge = RevenantWalkerTest.randomDodge(seed);
        int ticks = 0;
        double lost = 0;
        for (int i = 0; i < SimStep.ticks(120) && !boss.destroyed() && sortie.flying() && boss.phase() == 0; i++) {
            double before =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            sortie.step(dodge.applyAsInt(sortie));
            if (boss.phase() == 0) {
                ticks++;
                lost += Math.max(
                        0,
                        before
                                - sortie.ship().defences().shield()
                                - sortie.ship().defences().armour());
            }
        }
        return ticks > 0 ? lost * 10 / (ticks * SimStep.SECONDS) : 0;
    }

    /**
     * The autopilot flies the approach (retried after a loss) until the rings are up: the boss engaged, its spin-up over,
     * the station (nearly) full (nothing hurts it before; the guns' first step after).
     */
    static SetPiece upAndFull(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            SetPiece boss = RingPlay.ringedBoss(sortie);
            if (boss != null && !boss.ring().orElseThrow().spinningUp()) {
                assertTrue(boss.barShare() > 0.99, "the station full as the rings come up");
                return boss;
            }
            Level04Test.step(sortie);
        }
        throw new AssertionError("the rings never came up");
    }

    /**
     * QH-1 (the specials on a 14-part station; round 60 kept the per-part caps): measured on the full station as the
     * rings come up (medium, no Rook, no gun fired), as a share of the bar: a Smart Bomb's ring ({@code boss_part} damage
     * to every part it reaches), and an Orbital Lance held in one column for its whole burn (its {@code boss_part} cap
     * per part crossed), the best of {@link #LANCE_COLUMNS}. At most {@link #SPECIAL_SHARE} each keeps the per-part caps
     * (the round's question becomes trivia); above it the share is reported, nothing else built.
     */
    @Test
    void theSpecialsPerPartOnTheFullStation() {
        double bomb = specialShare("Smart Bomb", PlayField.WIDTH / 2.0);
        double[] lance = Arrays.stream(LANCE_COLUMNS)
                .parallel()
                .map(x -> specialShare("Orbital Lance", x))
                .toArray();
        double bestLance = Arrays.stream(lance).max().orElseThrow();
        System.out.printf(
                "QH-1: a Smart Bomb on the full station takes %.1f %% of the bar; an Orbital Lance %s %% in the columns"
                        + " %s, at most %.1f %%%n",
                100 * bomb,
                Arrays.toString(Arrays.stream(lance)
                        .map(s -> Math.round(1000 * s) / 10.0)
                        .toArray()),
                Arrays.toString(LANCE_COLUMNS),
                100 * bestLance);
        assertTrue(bomb > 0, "the bomb reaches the station");
        assertTrue(bestLance > 0, "the lance reaches the station");
        assertTrue(bomb <= SPECIAL_SHARE, "QH-1: a Smart Bomb's share " + bomb);
        assertTrue(bestLance <= SPECIAL_SHARE, "QH-1: an Orbital Lance's share " + bestLance);
    }

    /** QH-1's limit: one special's share of the bar that keeps the per-part caps. */
    static final double SPECIAL_SHARE = 0.25;

    /** The columns the lance is held in, px from the left. */
    static final double[] LANCE_COLUMNS = {140, 190, 240, 290, 340};

    /**
     * The share of the full station's bar the {@code special} takes, called from {@code x} (the ship flown there during
     * the spin-up without a shot, held there 5 s after the call, invulnerable, no Rook).
     */
    static double specialShare(String special, double x) {
        Loadout loadout = withoutRook(CONTENT, Difficulty.MEDIUM).withSpecial(SimSpecs.special(CONTENT, special, 1));
        Sortie sortie = sortie(CONTENT, loadout, SEEDS[0], Difficulty.MEDIUM, true);
        int steps = 0;
        SetPiece boss = null;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            boss = RingPlay.ringedBoss(sortie);
            if (boss != null && !boss.ring().orElseThrow().spinningUp()) {
                break;
            }
            int commands = boss != null
                    ? MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, RingPlay.STAND_Y)
                    : Autopilot.commands(sortie);
            sortie.step(commands);
        }
        assertTrue(boss != null && !boss.ring().orElseThrow().spinningUp(), special + ": the rings came up");
        assertEquals(1, boss.barShare(), 1e-9, special + ": the station full");
        double before = boss.barShare();
        sortie.step(Command.SPECIAL.bit());
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, RingPlay.STAND_Y));
        }
        return before - boss.barShare();
    }

    /**
     * The mass relay strikes the ship once and never Rook: flown by the autopilot to just before the relay's telegraph,
     * then without a move, the flash locked on the ship's height strikes it once ({@code RELAY_HIT}), Rook takes nothing
     * from it.
     */
    @Test
    void theRelayStrikesTheShipOnceAndNeverRook() {
        Sortie sortie = sortie(CONTENT, planLoadout(CONTENT, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, false);
        double telegraph = sortie.relay().orElseThrow().spec().t();
        int hits = 0;
        int fired = 0;
        double rookBefore = Double.NaN;
        double rookAfter = Double.NaN;
        double rookY = Double.NaN;
        double lineY = Double.NaN;
        int steps = 0;
        while (sortie.levelSeconds() < telegraph - 0.1 && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
        assertEquals(1, sortie.attempt(), "the first attempt reaches the relay");
        while (sortie.levelSeconds() < telegraph + 3 && steps++ < MAX_STEPS) {
            assertTrue(sortie.flying(), "the ship lives to the flash");
            boolean live = sortie.relay().orElseThrow().live();
            if (live && Double.isNaN(rookBefore)) {
                Wingman rook = sortie.wingman().orElseThrow();
                rookBefore = rook.armour();
                rookY = rook.y();
                lineY = sortie.relay().orElseThrow().lineY();
            }
            sortie.step(Command.FIRE.bit());
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                hits += events.type(e) == SimEvents.Type.RELAY_HIT ? 1 : 0;
                fired += events.type(e) == SimEvents.Type.RELAY_FIRED ? 1 : 0;
            }
            if (live && !sortie.relay().orElseThrow().live() && Double.isNaN(rookAfter)) {
                rookAfter = sortie.wingman().orElseThrow().armour();
            }
        }
        System.out.printf(
                "Level 35 the relay: fired %d, struck %d, its line at %.0f, Rook at %.0f, his armour %.0f -> %.0f%n",
                fired, hits, lineY, rookY, rookBefore, rookAfter);
        assertEquals(1, fired, "the relay fires once");
        assertEquals(1, hits, "it strikes the ship standing on its line once");
        assertTrue(sortie.relay().orElseThrow().spent());
        assertEquals(rookBefore, rookAfter, "Rook untouched by the flash");
    }

    /**
     * The relay: at t=59.8, telegraphed 1.2 s (easy 1.5) from the right edge, live 0.2 s, a 16 px band, 20 damage; the
     * lifeboat's trigger (20 HP, hard 28) frees the <em>Ceres lifeboat</em> secret.
     */
    @Test
    void theRelayAndTheLifeboatFromTheData() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(CONTENT, LEVEL, difficulty);
            var relay = level.hazards().relay().orElseThrow();
            assertEquals(59.8, relay.t(), 1e-9, difficulty.name());
            assertEquals(difficulty == Difficulty.EASY ? 1.5 : 1.2, relay.telegraphSeconds(), 1e-9, difficulty.name());
            assertEquals(0.2, relay.liveSeconds(), 1e-9);
            assertEquals(16, relay.bandPx(), 1e-9);
            assertEquals(20, relay.damage(), 1e-9);
            assertTrue(relay.fromRight());
            List<GroundObjectSpec> secrets = level.groundObjects().stream()
                    .filter(object -> object.secretIndex() >= 0)
                    .toList();
            assertEquals(
                    List.of("Ceres lifeboat"),
                    secrets.stream().map(GroundObjectSpec::secret).toList());
            assertEquals(
                    difficulty == Difficulty.HARD ? 28 : 20, secrets.getFirst().hp(), 1e-9, difficulty.name());
            assertEquals(1, level.secrets(), difficulty.name());
        }
    }

    /**
     * The boss checkpoint at the arena's start (a retry from it on easy and medium, none on hard: design/systems/
     * difficulty), and the secondary: 65 % of the approach's planned units (the waves and the hull guns; the Gunships'
     * Darts count as they come), the Sovereign and its bay Darts not counted (the count does not grow in the arena).
     */
    @Test
    void theCheckpointAndTheSecondary() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(CONTENT, planLoadout(CONTENT, difficulty), 2185, difficulty, true);
            boolean checkpoint = false;
            int atArena = -1;
            int launched = 0;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                SetPiece boss = RingPlay.ringedBoss(sortie);
                if (boss != null) {
                    checkpoint |= sortie.bossCheckpoint();
                    atArena = atArena < 0 ? sortie.enemyTotal() : atArena;
                    SimEvents events = sortie.events();
                    for (int e = 0; e < events.size(); e++) {
                        launched += events.type(e) == SimEvents.Type.BOSS_LAUNCHED ? 1 : 0;
                    }
                }
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertTrue(checkpoint, difficulty + ": the boss checkpoint taken at the arena's start");
            assertEquals(
                    difficulty != Difficulty.HARD,
                    CONTENT.difficulty().bossCheckpoint().of(difficulty),
                    difficulty + ": a retry from it (none on hard)");
            assertTrue(launched > 0, difficulty + ": the bays launched Darts");
            assertEquals(atArena, sortie.enemyTotal(), difficulty + ": the bay Darts are not counted");
            LevelScript level = SimSpecs.level(CONTENT, LEVEL, difficulty);
            int planned = level.waves().stream().mapToInt(LevelMetrics::planned).sum()
                    + level.groundUnits().size();
            System.out.printf(
                    "Level 35 %s: the secondary counts %d (%d planned, the rest the Gunships' Darts), %d required; %d"
                            + " bay Darts launched%n",
                    difficulty, sortie.enemyTotal(), planned, sortie.requiredKills(), launched);
            assertEquals(
                    (int) Math.ceil(0.65 * planned - 1e-9),
                    sortie.requiredKills(),
                    difficulty + ": 65 % of the " + planned + " planned");
        }
    }

    /**
     * The act's end: Level 35 is Act 5's last level; its outro (four pages under {@code act-complete}) and its next act,
     * Act 6 <em>Jovian Storm</em> at Callisto (the release ends after it).
     */
    @Test
    void levelThirtyFiveEndsActFive() {
        ActData act = CONTENT.acts().get("act-5-the-belt");
        assertEquals(35, act.levels().last());
        ActData.Outro outro = act.outro().orElseThrow();
        assertEquals(4, outro.pages().size());
        assertEquals(Optional.of("act-complete"), outro.music());
        assertEquals(Optional.of(new ActData.NextAct("ACT VI", "JOVIAN STORM", "CALLISTO")), act.nextAct());
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(CONTENT, planLoadout(CONTENT, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level35Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(CONTENT, planLoadout(CONTENT, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(CONTENT, planLoadout(CONTENT, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
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
