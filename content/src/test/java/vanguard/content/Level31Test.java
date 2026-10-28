package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.AirLinks;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.FlightSpecs;
import vanguard.sim.GroundUnit;
import vanguard.sim.HookMode;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Missiles;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M9 part D, step D3: Level 31 Mining Rig Escort from its data (design/campaign/act-5-the-belt/level-31-mining-rig-escort;
 * the tug in design/allies/mining-tug; the units in design/enemies: Hornet, Shard Drone, Buzzsaw Drone, Dart Drone, Gilded
 * Gunship, Talon, Skitter, Mote Swarm, Asteroid Mite, SAM Nest): its count per difficulty, the hull escort (three
 * sections at their station with the sway, their HP per difficulty, each section home paid), what hurts it (the hooked
 * kinds only: the Hornets' aimed missiles at a section, shootable; the Darts' dives; the Shard links; the rings and the
 * unhooked waves pass over), m9d §6's entry rules (rear Darts cross the engines no sooner than 0.8 s after their entry,
 * side Darts enter above the hull, no ground target under the swayed hull), the Shard air links (the EMP drops them, a
 * death breaks them), the Buzzsaw dashes (a tell of at least 0.5 s, bounces, small rocks smashed), {@code CLEAN HULL},
 * Dacosta's claim (a tow, the secret), the radio's event lines, and whole runs of the autopilot with the balance plan's
 * fit, with Rook, without him ({@code --escort none}) and with the Bulwark ({@code --escort bulwark}): at least two
 * sections home on every difficulty, all three on medium with Rook. The pacing, the floor, the density, the beam era's
 * HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link
 * BeamEraHpTest}, {@link TypicalHaulTest}); the mechanics themselves the simulation's ({@code HullEscortTest}, {@code
 * AirLinksTest}, {@code DashTest}).
 */
class Level31Test {
    static final String LEVEL = "act-5-the-belt/level-31-mining-rig-escort";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The plan's load at the L31 visit (BalanceTest's sheet): 19.5 of the Mk IV's 20 MW. */
    static final double SPARE_POWER = 0.5;
    /** The rear and side waves' share of all waves at most (design/campaign, Act 5's direction rule). */
    static final double NON_FRONT_MOST = 0.35;
    /** The fewest sections the autopilot keeps home on any difficulty (the README's tuning rule). */
    static final int LEAST_HOME = 2;
    /** m9d §6: a rear Dart crosses a section no sooner than this after it is on the screen, s. */
    static final double REAR_WINDOW = 0.8;
    /** m9d §6: a side Dart enters at most this far below the top edge (above the hull), px. */
    static final double SIDE_ENTRY_LOWEST = 200;
    /** m9d §6: the swayed hull spans x 130–350; a SAM Nest (44 px) stands at x ≤ 100 or ≥ 380. */
    static final double NEST_LEFT_MOST = 100;

    static final double NEST_RIGHT_LEAST = 380;
    /** The field's height, px (sim y is up: y-down = HEIGHT − y). */
    static final double HEIGHT = 540;
    /** Each section home pays 40 in Act 1 terms (262 in Act 5 at medium: the credit factor applies). */
    static final int SECTION_PAY = 40;
    /** {@code CLEAN HULL}: 100 in Act 1 terms (655 in Act 5 at medium). */
    static final int CLEAN_HULL_PAY = 100;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 31 ({@code "31"}: Level 30's with Composite IV): the Ion Beam L4, both
     * Micro-missile Pods L3, the Swarm Tail L2 on the rear, the EMP Burst, the Mk III shield, Composite IV, the Mk IV's
     * spare 0.5 MW, and Rook in the escort slot with his Autocannon at full armour.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(rook(content));
    }

    /** Rook in the slot: the Autocannon L1 (the plan's gun), at full armour. */
    static WingmanSpec rook(Content content) {
        return SimSpecs.wingman(
                content,
                "autocannon",
                1,
                WingmanSpec.Side.LEFT,
                content.wingmen().rook().armour());
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 2)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(4).name(),
                        SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1));
    }

    /** The plan's fit with the Bulwark in the slot ({@code --escort bulwark}), at full armour. */
    static Loadout withBulwark(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withBulwark(SimSpecs.bulwark(
                        content,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().bulwark().armour()));
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

    private static ObjectiveSpec.Hull hull(LevelScript level) {
        return level.objectives().escort().flatMap(ObjectiveSpec.Escort::hull).orElseThrow();
    }

    /**
     * The README's waves at medium (its totals: Skitter 90, Talon 66, Dart Drone 45, Mote Swarm 50, Hornet 10, Buzzsaw
     * Drone 24, Asteroid Mite 38, Shard Drone 22, Gilded Gunship 5); easy and hard by the formation lever and the Shard
     * waves' own counts (groups of 3 on easy, 4 on hard); the six SAM Nests on the outcrops; one secret, Dacosta's claim
     * as a tow.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        String[] slugs = {
            "skitter",
            "talon",
            "dart-drone",
            "mote-swarm",
            "hornet",
            "buzzsaw-drone",
            "asteroid-mite",
            "shard-drone",
            "gilded-gunship"
        };
        List<List<Integer>> expected = List.of(
                List.of(75, 56, 35, 40, 9, 18, 38, 16, 5),
                List.of(90, 66, 45, 50, 10, 24, 38, 22, 5),
                List.of(105, 76, 55, 60, 11, 30, 38, 28, 5));
        List<List<Integer>> all = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<Integer> counts = new ArrayList<>();
            for (String slug : slugs) {
                counts.add(count(level.waves(), slug));
            }
            all.add(counts);
        }
        assertEquals(expected, all, List.of(slugs).toString());
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            assertEquals(6, level.groundUnits().size(), difficulty + ": two SAM Nest trios");
            assertTrue(
                    level.groundUnits().stream()
                            .allMatch(unit -> unit.enemy().slug().equals("sam-nest")),
                    difficulty.name());
            assertEquals(1, level.secrets());
            assertEquals(1, level.objectives().tows().size(), difficulty + ": Dacosta's claim");
        }
    }

    /**
     * The tug as the hull escort: 200×300, three sections (bow, cargo, engines; the bow ahead, y up), its station (240,
     * 380) y down = (240, 160) y up with the ±10 / ±2.5 px sway over 14 s, rising in for 6 s, climbing out from t=178;
     * each section's HP per difficulty (easy 320 / 220 / 220, medium 240 / 160 / 160, hard 200 / 150 / 150) and its pay
     * (262); the damage the allies' data gives (a missile 5, a dive 3, a link touch 4 with a 0.5 s cooldown).
     */
    @Test
    void theTugIsAHullOfThreeSections() {
        double[][] hp = {{320, 220, 220}, {240, 160, 160}, {200, 150, 150}};
        for (Difficulty difficulty : Difficulty.values()) {
            ObjectiveSpec.Hull hull = hull(SimSpecs.level(content, LEVEL, difficulty));
            assertEquals(200, hull.size().width(), 1e-9);
            assertEquals(300, hull.size().height(), 1e-9);
            assertEquals(
                    List.of("bow", "cargo", "engines"),
                    hull.sections().stream()
                            .map(ObjectiveSpec.HullSection::name)
                            .toList());
            for (int k = 0; k < 3; k++) {
                ObjectiveSpec.HullSection section = hull.sections().get(k);
                assertEquals(hp[difficulty.ordinal()][k], section.hp(), 1e-9, difficulty + " " + section.name());
                assertEquals(SECTION_PAY, section.credits(), section.name());
                assertTrue(
                        Math.abs(section.dx()) + section.box().width() / 2
                                        <= hull.size().width() / 2 + 1e-9
                                && Math.abs(section.dy()) + section.box().height() / 2
                                        <= hull.size().height() / 2 + 1e-9,
                        section.name() + " inside the hull");
            }
            assertTrue(
                    hull.sections().get(0).dy() > hull.sections().get(1).dy()
                            && hull.sections().get(1).dy()
                                    > hull.sections().get(2).dy(),
                    "the bow ahead, the engines behind");
            ObjectiveSpec.Station station = hull.station();
            assertEquals(240, station.x(), 1e-9);
            assertEquals(HEIGHT - 380, station.y(), 1e-9);
            assertEquals(10, station.swayX(), 1e-9);
            assertEquals(2.5, station.swayY(), 1e-9);
            assertEquals(14, station.period(), 1e-9);
            assertEquals(6, hull.enterSeconds(), 1e-9);
            assertEquals(178, hull.climb().t(), 1e-9);
            assertEquals(6, hull.climb().seconds(), 1e-9);
            assertEquals(5, hull.missile(), 1e-9);
            assertEquals(3, hull.dive(), 1e-9);
            assertEquals(4, hull.link(), 1e-9);
            assertEquals(0.5, hull.linkCooldownSeconds(), 1e-9);
        }
    }

    /**
     * Who is sent at the tug: the Hornets, the Darts and the Shard Drones ({@code always}; easy: the Darts unhooked and
     * only the Hornet waves of t=40.5 and 104.5), the Shard rings of t=82 and 137 unhooked (not sent across the hull)
     * except on hard. The Hornet's gun homes aimed, per shooter, with its 0.5 s hatch glow; its missiles are shootable.
     */
    @Test
    void theHookedKindsAreTheHornetsTheDartsAndTheShards() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            ObjectiveSpec.Hull hull = hull(level);
            assertEquals(HookMode.ALWAYS, hull.targetedBy().get("hornet"), difficulty.name());
            assertEquals(HookMode.ALWAYS, hull.targetedBy().get("shard-drone"), difficulty.name());
            assertEquals(
                    difficulty == Difficulty.EASY ? HookMode.NONE : HookMode.ALWAYS,
                    hull.targetedBy().getOrDefault("dart-drone", HookMode.NONE),
                    difficulty.name());
            List<String> unhooked = hull.unhookedWaves().stream()
                    .map(i -> level.waves().get(i))
                    .map(wave -> wave.enemy().slug() + "@" + wave.t())
                    .sorted()
                    .toList();
            List<String> expected =
                    switch (difficulty) {
                        case EASY ->
                            List.of(
                                    "hornet@134.0",
                                    "hornet@24.0",
                                    "hornet@74.5",
                                    "shard-drone@137.0",
                                    "shard-drone@82.0");
                        case MEDIUM -> List.of("shard-drone@137.0", "shard-drone@82.0");
                        case HARD -> List.of();
                    };
            assertEquals(expected, unhooked, difficulty.name());
            EnemyGun gun = level.waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("hornet"))
                    .findFirst()
                    .orElseThrow()
                    .enemy()
                    .gun()
                    .orElseThrow();
            EnemyGun.HomingSpec homing = gun.homing().orElseThrow();
            assertTrue(homing.aimed(), "aimed at its target");
            assertTrue(homing.perShooter(), "its cap per shooter");
            assertTrue(homing.seekable() || homing.hp() > 0, "shootable");
            assertEquals(0.5, gun.tellSeconds(), 1e-9, "the hatch glow");
        }
    }

    /**
     * The Shard Drone's air link and the Buzzsaw's dash per difficulty: the links' tell (easy 0.8 s, hard 0.5 s), the
     * dash's tell never under 0.5 s and at least one bounce.
     */
    @Test
    void theShardsLinkAndTheBuzzsawsDashAsTheirStatBlocks() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec shard = enemy(level, "shard-drone");
            EnemySpec buzzsaw = enemy(level, "buzzsaw-drone");
            assertTrue(shard.airLink().isPresent(), difficulty + ": the Shard's link");
            FlightSpecs.Dash dash = buzzsaw.dash().orElseThrow();
            assertTrue(dash.tellSeconds() >= FlightSpecs.Dash.LEAST_TELL_SECONDS, difficulty + ": " + dash);
            assertTrue(dash.bounces() >= 1, difficulty + ": " + dash);
            assertTrue(buzzsaw.gun().isEmpty(), "a Buzzsaw has no gun");
            System.out.printf(
                    "Level 31 %s: Shard link tell %.2f s, band %.0f px, damage %.0f; Buzzsaw tell %.2f s, %.0f px/s,"
                            + " %d bounces, %d dashes%n",
                    difficulty,
                    shard.airLink().orElseThrow().tellSeconds(),
                    shard.airLink().orElseThrow().band(),
                    shard.airLink().orElseThrow().damage(),
                    dash.tellSeconds(),
                    dash.dashSpeed(),
                    dash.bounces(),
                    dash.dashes());
        }
    }

    private static EnemySpec enemy(LevelScript level, String slug) {
        return level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .findFirst()
                .orElseThrow()
                .enemy();
    }

    /**
     * m9d §6's entries: every side and rear wave warned 3 s ahead and a few of all; the rear waves are Dart streams only;
     * no SAM Nest under the swayed hull (x ≤ 100 or ≥ 380).
     */
    @Test
    void theEntriesKeepClearOfTheHull() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<WaveSpec> waves = level.waves();
            List<WaveSpec> other = waves.stream()
                    .filter(wave -> wave.entry() != WaveSpec.Entry.FRONT)
                    .toList();
            for (WaveSpec wave : other) {
                assertEquals(3.0, wave.warningSeconds().orElse(0.0), 1e-9, difficulty + " t=" + wave.t());
                if (wave.entry() == WaveSpec.Entry.REAR) {
                    assertEquals("dart-drone", wave.enemy().slug(), difficulty + " t=" + wave.t());
                }
            }
            double share = other.size() / (double) waves.size();
            assertTrue(share <= NON_FRONT_MOST, difficulty + ": " + share);
            for (GroundUnit unit : level.groundUnits()) {
                assertTrue(
                        unit.x() <= NEST_LEFT_MOST || unit.x() >= NEST_RIGHT_LEAST,
                        difficulty + ": a nest under the hull at x " + unit.x());
            }
        }
    }

    /** The objective's secondary: {@code CLEAN HULL}, every section at half its HP or more, 655 credits. */
    @Test
    void cleanHullIsTheSecondary() {
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary =
                    SimSpecs.level(content, LEVEL, difficulty).objectives().secondary();
            assertEquals("CLEAN HULL", secondary.label());
            assertEquals(0.5, secondary.integrity(), 1e-9);
            assertEquals(CLEAN_HULL_PAY, secondary.credits(), difficulty.name());
        }
    }

    /** What one completing attempt saw, beside {@link LevelMetrics.HullWatch}'s numbers. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The radio cues that started, by trigger. */
        final Map<CueTrigger, Integer> radio = new EnumMap<>(CueTrigger.class);
        /** The missiles that burst on the ship, shot down, or burst on a section. */
        int shotDown;
        /** The radio lines Rook spoke. */
        int rookLines;
        /** Steps whose hull damage is not the hooked attacks' (missiles, link touches and dives at their damage). */
        int strayDamage;
        /** A side wave's Darts entering lower than {@link #SIDE_ENTRY_LOWEST} (y down). */
        int sideLow;
        /** The side Darts seen entering. */
        int sideSeen;
        /** The lowest a side Dart entered, px from the top. */
        double sideLowest;
        /** The shortest tell before a Buzzsaw's dash, s (infinity for none). */
        double shortestTell = Double.POSITIVE_INFINITY;

        private final LevelScript level;
        private final ObjectiveSpec.Hull hull;
        private final Map<Integer, Integer> telling = new HashMap<>();
        private final Map<Integer, Boolean> entered = new HashMap<>();
        private double[] hp = new double[0];

        Watch(LevelScript level) {
            this.level = level;
            this.hull = hull(level);
        }

        @Override
        public void newAttempt() {
            events.clear();
            radio.clear();
            shotDown = 0;
            rookLines = 0;
            strayDamage = 0;
            sideLow = 0;
            sideSeen = 0;
            sideLowest = 0;
            shortestTell = Double.POSITIVE_INFINITY;
            telling.clear();
            entered.clear();
            hp = new double[0];
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            int missiles = 0;
            int links = 0;
            int hits = 0;
            boolean lost = false;
            for (int i = 0; i < seen.size(); i++) {
                SimEvents.Type type = seen.type(i);
                events.merge(type, 1, Integer::sum);
                if (type == SimEvents.Type.RADIO) {
                    RadioCue cue = sortie.script().radio().get(seen.value(i));
                    radio.merge(cue.trigger(), 1, Integer::sum);
                    rookLines += cue.speaker().equals("Rook") ? 1 : 0;
                } else if (type == SimEvents.Type.MISSILE_DESTROYED) {
                    shotDown += seen.value(i) == Missiles.SHOT_DOWN ? 1 : 0;
                    missiles += seen.value(i) == Missiles.ON_ALLY ? 1 : 0;
                } else if (type == SimEvents.Type.AIR_LINK_TOUCH && seen.value(i) >= AirLinks.TOUCH_SECTION) {
                    links++;
                } else if (type == SimEvents.Type.ALLY_HIT) {
                    hits++;
                } else if (type == SimEvents.Type.ALLY_LOST) {
                    lost = true;
                }
            }
            int n = sortie.allyCount();
            if (hp.length != n) {
                hp = new double[n];
                for (int k = 0; k < n; k++) {
                    hp[k] = sortie.hullSection(k).hp();
                }
            }
            double drop = 0;
            for (int k = 0; k < n; k++) {
                double now = sortie.ally(k).hpShare() * sortie.hullSection(k).hp();
                drop += hp[k] - now;
                hp[k] = now;
            }
            if (!lost && drop > 1e-6) {
                double rest = drop - missiles * hull.missile() - links * hull.link();
                double dives = rest / hull.dive();
                if (rest < -1e-6 || Math.abs(dives - Math.rint(dives)) > 1e-6 || hits < 1) {
                    strayDamage++;
                }
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                String slug = enemy.spec().slug();
                if (slug.equals("buzzsaw-drone")) {
                    if (enemy.dashTelling()) {
                        telling.putIfAbsent(enemy.serial(), step);
                    } else if (enemy.dashing() && telling.containsKey(enemy.serial())) {
                        shortestTell =
                                Math.min(shortestTell, (step - telling.remove(enemy.serial())) * SimStep.SECONDS);
                    }
                } else if (slug.equals("dart-drone")
                        && enemy.wave() >= 0
                        && level.waves().get(enemy.wave()).entry() == WaveSpec.Entry.SIDES
                        && !entered.containsKey(enemy.serial())) {
                    double x = enemy.renderX(1);
                    double y = enemy.renderY(1);
                    if (x >= 0 && x <= 480 && y >= 0 && y <= HEIGHT) {
                        entered.put(enemy.serial(), true);
                        double down = HEIGHT - y;
                        sideSeen++;
                        sideLowest = Math.max(sideLowest, down);
                        sideLow += down > SIDE_ENTRY_LOWEST ? 1 : 0;
                    }
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
            LevelMetrics.HullRun hull,
            Watch watch,
            int escortCredits,
            int escortHome,
            double creditFactor) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch(sortie.script());
        LevelMetrics.HullWatch hull = new LevelMetrics.HullWatch();
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch, hull);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                hull.run(),
                watch,
                complete ? sortie.result().escort().credits() : 0,
                complete ? sortie.result().escort().home() : 0,
                SimSpecs.rules(content, LEVEL, difficulty).scoring().creditFactor());
    }

    /** The flights of a fit over the seeds and difficulties, flown in parallel, in their order. */
    private List<Flight> flights(
            String fit, int seeds, java.util.function.BiFunction<Content, Difficulty, Loadout> loadout) {
        Difficulty[] difficulties = Difficulty.values();
        return IntStream.range(0, difficulties.length * seeds)
                .parallel()
                .mapToObj(i -> fly(
                        fit, loadout.apply(content, difficulties[i / seeds]), difficulties[i / seeds], 1 + i % seeds))
                .toList();
    }

    /**
     * Each run's checks: completed, at least two sections home (all three on medium with Rook: the README's tuning rule),
     * each section home paid, {@code CLEAN HULL} met exactly when every section kept half its HP, only the hooked
     * attacks hurting the tug, the rear Darts' window, the side Darts above the hull, the Buzzsaws' tell; the radio's
     * event lines as the hull fared (the first hit once if hit, one loss line per section lost, one closing line), Rook's
     * lines only with Rook flying.
     */
    private static String check(Flight flight, boolean rook) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        LevelMetrics.HullRun hull = flight.hull();
        Watch watch = flight.watch();
        int home = hull.home();
        assertTrue(home >= LEAST_HOME, at + ": " + home + " sections home");
        if (rook && flight.difficulty() == Difficulty.MEDIUM) {
            assertEquals(3, home, at + ": all three home on medium");
        }
        assertEquals(home, flight.escortHome(), at);
        assertEquals(
                home * (int) Math.rint(SECTION_PAY * flight.creditFactor()),
                flight.escortCredits(),
                at + ": each section home paid");
        assertEquals(hull.clean(), flight.run().secondaryMet(), at + ": CLEAN HULL");
        assertEquals(0, watch.strayDamage, at + ": hull damage from no hooked attack");
        if (flight.difficulty() != Difficulty.EASY) {
            // The rear Darts are sent at the tug (easy's chase the ship and pass over it).
            assertTrue(
                    Double.isNaN(hull.rearFirst()) || hull.rearFirst() >= REAR_WINDOW - 1e-9,
                    at + ": a rear Dart crossed after " + hull.rearFirst() + " s");
        }
        assertEquals(0, watch.sideLow, at + ": side Darts entering low, lowest " + watch.sideLowest);
        assertTrue(watch.shortestTell >= FlightSpecs.Dash.LEAST_TELL_SECONDS - 1e-9, at + ": " + watch.shortestTell);
        boolean hit = watch.count(SimEvents.Type.ALLY_HIT) > 0;
        assertEquals(hit ? 1 : 0, watch.radio.getOrDefault(CueTrigger.FIRST_ALLY_HIT, 0), at + ": the first hit");
        assertEquals(3 - home, watch.radio.getOrDefault(CueTrigger.ALLY_LOST, 0), at + ": a line per section lost");
        assertEquals(1, watch.radio.getOrDefault(CueTrigger.LEVEL_END, 0), at + ": the closing line");
        if (!rook) {
            assertEquals(0, watch.rookLines, at + ": Rook's lines need him flying");
        }
        return String.format(
                "%n    %s: retries %d, home %d %s, clean %s, credits %d; missiles %d on the hull, %d shot down; links %d,"
                        + " dives %.0f; rear Darts %d/%d crossed (first %.2f s); side Darts %d (lowest y %.0f);"
                        + " links lit %d, broken %d, dropped %d; dashes %d (tell %.2f s), bounces %d, rocks smashed %d",
                at,
                flight.run().retries(),
                home,
                java.util.Arrays.toString(hull.share()).replaceAll("(\\d\\.\\d\\d)\\d+", "$1"),
                hull.clean(),
                flight.run().credits(),
                hull.missiles(),
                watch.shotDown,
                hull.links(),
                hull.dives(),
                hull.rearCrossed(),
                hull.rearDarts(),
                hull.rearFirst(),
                watch.sideSeen,
                watch.sideLowest,
                watch.count(SimEvents.Type.AIR_LINK_LIT),
                watch.count(SimEvents.Type.AIR_LINK_BROKEN),
                watch.count(SimEvents.Type.AIR_LINK_DROPPED),
                watch.count(SimEvents.Type.DASH_START),
                watch.shortestTell,
                watch.count(SimEvents.Type.DASH_BOUNCE),
                watch.count(SimEvents.Type.DASH_SMASH));
    }

    /**
     * The autopilot completes the level with the plan's fit and Rook on every difficulty and seed and keeps at least two
     * sections home (all three on medium); over the runs the Hornets' missiles are shot down and some burst on the hull,
     * a Shard group's link is broken by a death, the Buzzsaws dash and bounce. Printed per run.
     */
    @Test
    void theAutopilotKeepsTheTugHomeWithRook() {
        List<Flight> flights = flights("Rook", SEEDS, Level31Test::planLoadout);
        StringBuilder report = new StringBuilder("Level 31 autopilot with Rook over " + SEEDS + " seeds:");
        int shotDown = 0;
        int broken = 0;
        int bounces = 0;
        int smashed = 0;
        int burst = 0;
        int clean = 0;
        int dashes = 0;
        int lit = 0;
        for (Flight flight : flights) {
            report.append(check(flight, true));
            dashes += flight.watch().count(SimEvents.Type.DASH_START);
            lit += flight.watch().count(SimEvents.Type.AIR_LINK_LIT);
            shotDown += flight.watch().shotDown;
            broken += flight.watch().count(SimEvents.Type.AIR_LINK_BROKEN);
            bounces += flight.watch().count(SimEvents.Type.DASH_BOUNCE);
            smashed += flight.watch().count(SimEvents.Type.DASH_SMASH);
            burst += flight.hull().missiles();
            clean += flight.difficulty() == Difficulty.MEDIUM && flight.hull().clean() ? 1 : 0;
        }
        System.out.println(report);
        assertTrue(shotDown > 0, "the Hornets' missiles are shootable");
        assertTrue(burst > 0, "and some reach the hull");
        assertTrue(broken > 0, "a Shard's death breaks its links");
        assertTrue(dashes > 0, "the Buzzsaws dash");
        assertTrue(lit > 0, "the Shards link");
        assertTrue(bounces > 0, "the Buzzsaws bounce");
        assertTrue(clean > SEEDS / 2, "CLEAN HULL met on medium in most runs: " + clean);
    }

    /**
     * Without Rook ({@code --escort none}) and with the Bulwark in his slot ({@code --escort bulwark}) the autopilot
     * completes the level and keeps at least two sections home on every difficulty; Rook's lines stay silent.
     */
    @Test
    void theAutopilotKeepsTheTugHomeWithoutRook() {
        StringBuilder report = new StringBuilder("Level 31 autopilot without Rook:");
        for (Flight flight : flights("--escort none", SEEDS, Level31Test::withoutRook)) {
            report.append(check(flight, false));
        }
        for (Flight flight : flights("--escort bulwark", 1, Level31Test::withBulwark)) {
            report.append(check(flight, false));
        }
        System.out.println(report);
    }

    /**
     * The Buzzsaws' dashes left alone (the ship invulnerable, holding fire, the autopilot's moves): each dash after a tell
     * of at least 0.5 s (the stat block's per difficulty), bouncing off the edges and the big rocks, smashing the small
     * rocks in its way.
     */
    @Test
    void theBuzzsawsDashBounceAndSmashSmallRocks() {
        StringBuilder report = new StringBuilder("Level 31 Buzzsaws, fire held:");
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, withoutRook(content, difficulty), 1, difficulty, true);
            Watch watch = new Watch(sortie.script());
            int rockBounces = 0;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(Autopilot.commands(sortie) & ~Command.FIRE.bit());
                watch.stepped(sortie, steps);
                SimEvents events = sortie.events();
                for (int i = 0; i < events.size(); i++) {
                    rockBounces += events.type(i) == SimEvents.Type.DASH_BOUNCE && events.value(i) == 1 ? 1 : 0;
                }
            }
            double tell = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("buzzsaw-drone"))
                    .findFirst()
                    .orElseThrow()
                    .enemy()
                    .dash()
                    .orElseThrow()
                    .tellSeconds();
            String at = difficulty.name();
            assertTrue(watch.count(SimEvents.Type.DASH_TELL) > 0, at + ": tells");
            assertTrue(watch.count(SimEvents.Type.DASH_START) > 0, at + ": dashes");
            assertTrue(watch.shortestTell >= tell - SimStep.SECONDS - 1e-9, at + ": tell " + watch.shortestTell);
            assertTrue(watch.shortestTell >= FlightSpecs.Dash.LEAST_TELL_SECONDS - 1e-9, at);
            assertTrue(watch.count(SimEvents.Type.DASH_BOUNCE) > 0, at + ": bounces");
            assertTrue(rockBounces > 0, at + ": off the big rocks");
            assertTrue(watch.count(SimEvents.Type.DASH_SMASH) > 0, at + ": small rocks smashed");
            report.append(String.format(
                    "%n  %s: %d tells (shortest %.2f s), %d dashes, %d bounces (%d off rocks), %d small rocks smashed",
                    difficulty,
                    watch.count(SimEvents.Type.DASH_TELL),
                    watch.shortestTell,
                    watch.count(SimEvents.Type.DASH_START),
                    watch.count(SimEvents.Type.DASH_BOUNCE),
                    rockBounces,
                    watch.count(SimEvents.Type.DASH_SMASH)));
        }
        System.out.println(report);
    }

    /**
     * The EMP Burst drops the lit Shard links (the drones live), which relight after the stun: flown with the autopilot
     * until a line's links are lit, then the special called.
     */
    @Test
    void theEmpDropsTheShardLinks() {
        Sortie sortie = sortie(content, withoutRook(content, Difficulty.MEDIUM), 3, Difficulty.MEDIUM, true);
        int steps = 0;
        double calledAt = Double.NaN;
        boolean[] dropped = new boolean[AirLinks.SLOTS];
        int drops = 0;
        double relitAt = Double.NaN;
        while (!sortie.complete() && steps++ < MAX_STEPS && Double.isNaN(relitAt)) {
            int commands = Autopilot.commands(sortie);
            if (Double.isNaN(calledAt) && anyLit(sortie)) {
                commands |= Command.SPECIAL.bit();
                calledAt = sortie.levelSeconds();
            } else if (!Double.isNaN(calledAt)) {
                // Hold fire after the call, so the stunned drones live to relight their links.
                commands &= ~Command.FIRE.bit();
            }
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                int slot = events.value(i);
                if (events.type(i) == SimEvents.Type.AIR_LINK_DROPPED) {
                    dropped[slot] = true;
                    drops++;
                } else if (events.type(i) == SimEvents.Type.AIR_LINK_LIT && dropped[slot]) {
                    relitAt = sortie.levelSeconds();
                }
            }
        }
        System.out.printf(
                "Level 31 EMP: called at t %.1f, %d links dropped, one relit at t %.1f%n", calledAt, drops, relitAt);
        assertFalse(Double.isNaN(calledAt), "a line's links lit");
        assertTrue(drops >= 2, "the EMP drops them");
        assertFalse(Double.isNaN(relitAt), "a dropped link relights after the stun");
    }

    /**
     * Whether a whole line's links are lit (two or more): the ring takes about 0.3 s to reach the top of the field, so
     * a lone link of a group being shot apart may break before it.
     */
    private static boolean anyLit(Sortie sortie) {
        int lit = 0;
        for (int slot = 0; slot < AirLinks.SLOTS; slot++) {
            lit += sortie.airLinks().lit(slot) ? 1 : 0;
        }
        return lit >= 2;
    }

    /**
     * Dacosta's claim is a tow (t=100, 6 hits): its housing shot through frees the hidden crate ({@code SECRET_FOUND},
     * the Bellwether's secret line) and a ship that flies under it collects it. Flown with the Pulse Cannon L3 in front:
     * a tow's cable takes the player's shots, not a beam (the simulation's tow rule since Level 07), so the plan's Ion
     * Beam passes it and only the pods' stray missiles hit it (2 of 6 in a pass; Level 30's buoy alike; pending the
     * user's review).
     */
    @Test
    void dacostasClaimFreesItsCrate() {
        Loadout bolts = SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 3)),
                content.shields().models().get(2).name(),
                content.armour().plating().get(4).name(),
                0,
                Difficulty.MEDIUM);
        Sortie sortie = sortie(content, bolts, 1, Difficulty.MEDIUM, true);
        Tow tow = sortie.tow(0);
        int found = 0;
        int line = 0;
        int steps = 0;
        boolean collected = false;
        while (!sortie.complete() && steps++ < MAX_STEPS && !collected) {
            int commands;
            Pickup crate = crate(sortie);
            if (sortie.levelSeconds() < tow.spec().t() - 1) {
                commands = Autopilot.commands(sortie);
            } else if (tow.holding()) {
                commands =
                        steer(sortie, tow.present() ? tow.cableX() : tow.spec().x(), Command.FIRE.bit());
            } else if (crate != null) {
                commands = steer(sortie, crate.renderX(), 0);
            } else {
                commands = Autopilot.commands(sortie);
            }
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                found += events.type(i) == SimEvents.Type.SECRET_FOUND ? 1 : 0;
                if (events.type(i) == SimEvents.Type.RADIO
                        && sortie.script().radio().get(events.value(i)).trigger() == CueTrigger.SECRET) {
                    line++;
                }
            }
            collected = sortie.result().credits().secrets() > 0;
        }
        System.out.printf("Level 31 Dacosta's claim: found %d, collected at t %.1f%n", found, sortie.levelSeconds());
        assertEquals(1, found, "the claim's crate freed");
        assertTrue(collected, "and collected");
        assertEquals(1, line, "the Bellwether's line");
    }

    private static Pickup crate(Sortie sortie) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.HIDDEN_CRATE) {
                return sortie.pickup(i);
            }
        }
        return null;
    }

    /** Steers the ship to {@code targetX} low on the screen, with {@code fire}. */
    private static int steer(Sortie sortie, double targetX, int fire) {
        int commands = fire;
        if (sortie.ship().x() < targetX - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > targetX + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 57) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 63) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    /**
     * The radio: the hull's event lines once each (the first hit and a section lost, naming it as {@code {section}}; the
     * mission failed line; two closing lines by the sections home: all three, one or two); Rook's two lines need him
     * flying ({@code requires: escort}); no line on the enemy channel.
     */
    @Test
    void theRadioHasTheLevelsEventLines() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<RadioCue> radio = SimSpecs.level(content, LEVEL, difficulty).radio();
            for (CueTrigger trigger :
                    List.of(CueTrigger.FIRST_ALLY_HIT, CueTrigger.ALLY_LOST, CueTrigger.MISSION_FAILED)) {
                assertEquals(
                        1,
                        radio.stream().filter(cue -> cue.trigger() == trigger).count(),
                        difficulty + ": " + trigger);
            }
            for (CueTrigger trigger : List.of(CueTrigger.FIRST_ALLY_HIT, CueTrigger.ALLY_LOST)) {
                RadioCue cue = radio.stream()
                        .filter(c -> c.trigger() == trigger)
                        .findFirst()
                        .orElseThrow();
                assertEquals("Bellwether", cue.speaker());
                assertTrue(cue.line().contains("{section}"), cue.line());
            }
            List<String> ends = radio.stream()
                    .filter(cue -> cue.trigger() == CueTrigger.LEVEL_END)
                    .map(cue -> cue.alliesMin() + "-" + cue.alliesMax())
                    .sorted()
                    .toList();
            assertEquals(List.of("1-2", "3-3"), ends, difficulty.name());
            int barks = 0;
            for (RadioCue cue : radio) {
                if ((cue.requires() & RadioCue.FITTED_ESCORT) != 0) {
                    assertEquals("Rook", cue.speaker(), cue.line());
                    barks++;
                }
                assertFalse(
                        content.voices().channelOf(cue.speaker()) == Channel.ENEMY, cue.speaker() + ": " + cue.line());
            }
            assertEquals(2, barks, difficulty + ": Rook's two lines");
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level31Test::flyToEnd);

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
