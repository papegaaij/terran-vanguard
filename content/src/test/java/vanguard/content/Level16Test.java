package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Allocations;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.LevelResult;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.WaveSpec;

/**
 * M7 part C, step C4: Level 16 Valles Canyon Run from its data (design/campaign/act-3-red-dust/level-16-valles-canyon-
 * run; round 36's M1–M3, M9 = a, Q.16a and the gap check's defaults, pending the user's review in round 39): the
 * terrain and its contact per difficulty, the gates (7 / 9 / 10 pairs) and their times, the anchors' tendril and hard
 * orbs, the Threadcrawlers' crawl, pore wave and segments, the relay hut's hits, the cargo rovers, a beam reaching
 * every anchor from a flyable place, and the autopilot flying the level to its end in either channel (printed: the
 * wall touches and the armour they cost, the README's ≤ ≈ 30).
 */
class Level16Test {
    static final String LEVEL = "act-3-red-dust/level-16-valles-canyon-run";
    private static final int MAX_STEPS = 60 * 60 * 30;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 16: Level 15's (the L16 visit buys the Mk III generator, which only a beam
     * needs: the plan's fit carries none).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level15Test.planLoadout(content, difficulty);
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty) {
        Loadout loadout = planLoadout(content, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                SimSpecs.rules(content, LEVEL, difficulty),
                loadout.plating().maxArmour());
    }

    @Test
    void theCanyonAndItsContactPerDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            GroundSpec.TerrainSpec terrain = level.ground().terrain().orElseThrow();
            assertEquals(difficulty == Difficulty.EASY ? 6 : 10, terrain.damage(), difficulty + ": heavy, easy medium");
            assertEquals(28, terrain.push());
            assertEquals(0.12, terrain.pushSeconds());
            assertEquals(0.5, terrain.cooldownSeconds());
            Terrain rock = terrain.terrain();
            assertEquals(180, rock.rightAt(27_650) - rock.leftAt(27_650), 1e-9, "the narrows: 180 px");
            assertEquals(2, rock.islands(), "the fork mesa and the rock pillar");
        }
        assertEquals(
                CueTrigger.FIRST_WALL_CONTACT,
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio().stream()
                        .map(RadioCue::trigger)
                        .filter(trigger -> trigger == CueTrigger.FIRST_WALL_CONTACT)
                        .findFirst()
                        .orElseThrow());
    }

    /** The gates: 9 pairs at medium, 7 on easy, 10 on hard, at the README's times; the anchors not mapped twice. */
    @Test
    void theGatesPerDifficulty() {
        Map<Difficulty, List<Double>> times = new TreeMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            times.put(
                    difficulty,
                    level.ground().gates().stream().map(GroundSpec.Gate::t).toList());
            long anchors = level.groundUnits().stream()
                    .filter(unit -> unit.enemy().link().isPresent())
                    .count();
            assertEquals(2L * level.ground().gates().size(), anchors, difficulty + ": each gate's two anchors once");
            assertTrue(level.objectives().secondary().links(), "the secondary: every tendril broken");
        }
        assertEquals(List.of(34.0, 46.0, 57.0, 132.0, 133.5, 144.0, 152.0), times.get(Difficulty.EASY));
        assertEquals(List.of(34.0, 46.0, 57.0, 58.5, 132.0, 133.5, 144.0, 152.0, 153.5), times.get(Difficulty.MEDIUM));
        assertEquals(
                List.of(34.0, 46.0, 47.5, 57.0, 58.5, 132.0, 133.5, 144.0, 152.0, 153.5), times.get(Difficulty.HARD));
    }

    /** The Tendril Anchor: 12 HP each, the tendril 40 HP (easy 30, hard 52), medium 6 per touch; orbs on hard only. */
    @Test
    void theAnchorsTendril() {
        Map<Difficulty, Double> tendril = new TreeMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec anchor = SimSpecs.level(content, LEVEL, difficulty)
                    .ground()
                    .gates()
                    .getFirst()
                    .anchor();
            EnemySpec.Link link = anchor.link().orElseThrow();
            tendril.put(difficulty, link.tendrilHp());
            assertEquals(6, link.contactDamage());
            assertEquals(1.0, link.dormantSeconds());
            assertEquals(0.5, link.cooldownSeconds());
            assertEquals(6, link.band());
            assertEquals(8, link.burntBounty());
            assertEquals(12, anchor.bounty());
            assertEquals(difficulty == Difficulty.HARD, anchor.gun().isPresent(), difficulty + ": the orbs on hard");
        }
        assertEquals(30, tendril.get(Difficulty.EASY), 0.5);
        assertEquals(40, tendril.get(Difficulty.MEDIUM), 1e-9);
        assertEquals(52, tendril.get(Difficulty.HARD), 0.5);
    }

    /**
     * The Threadcrawlers: two (hard three) crawl waves of one, 70 px/s, the pore wave 0.25 s apart with 0.3 s glow,
     * rest 2.0 (hard 1.5), easy every other pore; 10 / 12 / 16 segments; the head pays 18 more first; each one's
     * body (≈ 345 px of path) lies above the top edge as it enters.
     */
    @Test
    void theThreadcrawlersCrawl() {
        LevelData data = content.level(LEVEL);
        List<String> below = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> crawlers = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().crawl().isPresent())
                    .toList();
            assertEquals(difficulty == Difficulty.HARD ? 3 : 2, crawlers.size(), difficulty + ": crawl waves");
            for (WaveSpec wave : crawlers) {
                assertEquals(1, wave.count());
                EnemySpec head = wave.enemy();
                EnemySpec.Crawl crawl = head.crawl().orElseThrow();
                EnemySpec.ChainSpec chain = head.chain().orElseThrow();
                assertEquals(70, crawl.speed());
                assertEquals(1.0, crawl.cutSeconds());
                assertEquals(0.25, crawl.pores().interval());
                assertEquals(0.3, crawl.pores().glow());
                assertEquals(
                        difficulty == Difficulty.HARD ? 1.5 : 2.0, crawl.pores().rest());
                assertEquals(difficulty == Difficulty.EASY, crawl.pores().everyOther());
                assertEquals(
                        switch (difficulty) {
                            case EASY -> 10;
                            case MEDIUM -> 12;
                            case HARD -> 16;
                        },
                        chain.segmentBoxes().size());
                assertEquals(18, chain.headFirstBonus());
                assertEquals(4, chain.tailFirstBonus());
                assertEquals(36, chain.segmentBoxes().getFirst().width());
                assertEquals(24, chain.segmentBoxes().getFirst().height());
                assertTrue(chain.regrown() == null, "a ground chain never regrows");
                double body = chain.offsets().getLast();
                double top = data.scrollAt(wave.t());
                if (crawl.scrolls()[0] - body < top) {
                    below.add(String.format(
                            "%s t=%.0f: the tail on row %.0f, the top edge's %.0f",
                            difficulty, wave.t(), crawl.scrolls()[0] - body, top));
                }
                if (difficulty == Difficulty.MEDIUM) {
                    assertEquals(344.5, body, 1, "≈ 345 px of path behind the head");
                }
            }
        }
        assertEquals(List.of(), below, "each crawler's body enters from above the top edge");
    }

    /** The relay hut takes 3 hits (hard 5) and reveals the secret; the four cargo rovers, 3 HP, drop small salvage. */
    @Test
    void theHutAndTheRovers() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<GroundObjectSpec> huts = level.groundObjects().stream()
                    .filter(object -> object.hits() > 0)
                    .toList();
            assertEquals(1, huts.size());
            assertEquals(difficulty == Difficulty.HARD ? 5 : 3, huts.getFirst().hits());
            assertEquals("relay-hut", huts.getFirst().look());
            List<GroundObjectSpec> rovers = level.groundObjects().stream()
                    .filter(object -> object.look().equals("canyon-rover"))
                    .toList();
            assertEquals(4, rovers.size());
            rovers.forEach(rover -> {
                assertEquals(3, rover.hp());
                assertTrue(rover.drop().isPresent());
            });
        }
    }

    /**
     * A beam reaches every gate's anchors: while the anchor comes down the screen there is a place for the ship at
     * its cruise height (its hull 4 px clear of rock) from which the narrowest beam (6 px) meets the anchor's hit box before
     * the rock in its column ends it, for at least half a second (an anchor dies in 0.27 s to it).
     */
    @Test
    void aBeamReachesEveryAnchor() {
        LevelData data = content.level(LEVEL);
        StringBuilder report = new StringBuilder();
        List<String> unreachable = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Terrain rock = level.ground().terrain().orElseThrow().terrain();
            for (GroundSpec.Gate gate : level.ground().gates()) {
                double half = gate.anchor().hitbox().width() / 2;
                for (double x : new double[] {gate.leftX(), gate.rightX()}) {
                    double seconds = reachableSeconds(data, rock, gate.t(), x, half);
                    if (difficulty == Difficulty.MEDIUM) {
                        report.append(String.format(" t=%.1f x=%.0f: %.1f s;", gate.t(), x, seconds));
                    }
                    if (seconds < 0.5) {
                        unreachable.add(difficulty + " t=" + gate.t() + " x=" + x + ": " + seconds + " s");
                    }
                }
            }
        }
        System.out.println("Level 16 beam reach per anchor (medium):" + report);
        assertEquals(List.of(), unreachable);
    }

    /** The ship's cruise height, px; the beam starts at its nose, 20 px above. */
    private static final double SHIP_Y = 110;

    /** How long a beam can reach the anchor entering at {@code t} at {@code x} (centre on its first rows), s. */
    private static double reachableSeconds(LevelData data, Terrain rock, double t, double x, double half) {
        double entry = data.scrollAt(t);
        double seconds = 0;
        double step = 0.05;
        for (double now = t; now < t + 6; now += step) {
            double scroll = data.scrollAt(now);
            double y = PlayField.HEIGHT + half - (scroll - entry);
            if (y < SHIP_Y + 40) {
                break;
            }
            if (y - half > PlayField.HEIGHT) {
                continue;
            }
            for (double shipX = x - half - 3; shipX <= x + half + 3; shipX += 1) {
                // Its 36 px wings with 4 px to spare: not scraping the wall.
                if (rock.rock(scroll, shipX, SHIP_Y, 22, 20)) {
                    continue;
                }
                if (rock.ceiling(scroll, shipX, 3, SHIP_Y + 20) >= y - half) {
                    seconds += step;
                    break;
                }
            }
        }
        return seconds;
    }

    /**
     * The autopilot flies the level to its end with the plan's fit and Rook on every difficulty, in either channel.
     * Printed: the attempts, the wall touches (the ship's) and the armour lost, the gates broken, the secret, the
     * secondary, the credits and the grade.
     */
    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void theAutopilotFliesTheLevelToItsEnd(Difficulty difficulty) {
        for (Autopilot.Channel channel : Autopilot.Channel.values()) {
            Flight flight = fly(content, difficulty, channel);
            System.out.printf(
                    "Level 16 %s %s channel: attempt %d%s, real %.1f s, wall touches %d (t=%s; Rook %d), armour lost %.0f,"
                            + " tendril touches %d, gates broken %d, kills %d/%d, secondary %s, hut %s, credits %d (%s),"
                            + " Rook %s, grade %s%n",
                    difficulty,
                    channel,
                    flight.sortie.attempt(),
                    flight.deaths.isEmpty() ? "" : " (lost at t=" + flight.deaths.strip() + ")",
                    flight.sortie.realSeconds(),
                    flight.wallTouches,
                    flight.touchTimes.strip(),
                    flight.rookTouches,
                    flight.armourLost,
                    flight.tendrilTouches,
                    flight.broken,
                    flight.result.kills(),
                    flight.result.enemies(),
                    flight.result.secondaryMet() ? "met" : "failed",
                    flight.secrets > 0 ? "found" : "missed",
                    flight.result.credits().total(),
                    flight.result.credits(),
                    flight.sortie
                            .wingman()
                            .map(rook -> rook.ejected() ? "ejected" : "home")
                            .orElse("none"),
                    flight.result.grade().letter());
            assertTrue(flight.sortie.complete(), difficulty + " " + channel);
            if (channel == Autopilot.Channel.LEFT) {
                assertTrue(flight.secrets > 0, difficulty + ": the left channel passes the relay hut");
            }
        }
    }

    /** One autopilot flight's counts (the completed attempt's). */
    static final class Flight {
        Sortie sortie;
        LevelResult result;
        String deaths = "";
        String touchTimes = "";
        int wallTouches;
        int rookTouches;
        int tendrilTouches;
        int broken;
        int secrets;
        double armourLost;
    }

    static Flight fly(Content content, Difficulty difficulty, Autopilot.Channel channel) {
        Flight flight = new Flight();
        Sortie sortie = sortie(content, 2185, difficulty);
        flight.sortie = sortie;
        int steps = 0;
        int attempt = sortie.attempt();
        double armour = sortie.ship().defences().armour();
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            double t = sortie.levelSeconds();
            if (!sortie.flying() || sortie.primaryFailed()) {
                sortie.retry(sortie.ship().defences().maxArmour());
            }
            sortie.step(Autopilot.commands(sortie, channel));
            if (sortie.attempt() != attempt) {
                attempt = sortie.attempt();
                flight.deaths += String.format("%.0f ", t);
                flight.wallTouches = flight.rookTouches = flight.tendrilTouches = flight.broken = flight.secrets = 0;
                flight.armourLost = 0;
                flight.touchTimes = "";
                armour = sortie.ship().defences().armour();
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                switch (events.type(i)) {
                    case WALL_CONTACT -> {
                        if (events.value(i) == 2) {
                            flight.rookTouches++;
                        } else {
                            flight.wallTouches++;
                            flight.touchTimes += String.format("%.0f ", t);
                        }
                    }
                    case TENDRIL_TOUCH -> flight.tendrilTouches += events.value(i) == 0 ? 1 : 0;
                    case TENDRIL_BROKEN -> flight.broken++;
                    case SECRET_FOUND -> flight.secrets++;
                    default -> {}
                }
            }
            double now = sortie.ship().defences().armour();
            if (now < armour) {
                flight.armourLost += armour - now;
            }
            armour = now;
        }
        flight.result = sortie.result();
        return flight;
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD), Level16Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM);
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
