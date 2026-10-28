package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.ChainSpecs;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.IdleReason;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Mine;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WarningEdge;
import vanguard.sim.WaveSpec;
import vanguard.sim.Wingman;
import vanguard.sim.WingmanSpec;

/**
 * M9 part C, step C3: Level 30 Minefield from its data (design/campaign/act-5-the-belt/level-30-minefield; the units in
 * design/enemies: Minelayer, Gilded Gunship, Dart Drone, Rail Serpent, Talon, Asteroid Mite, Skitter, Mote Swarm): its
 * count, {@code ground: none} (the Bomb Rack idle with {@code NO GROUND}, the Airstrike called and bursting), the mine
 * lines (clusters of 4, the authored gaps, the column's staggered entries from the rear), a shot mine's chain clearing
 * its cluster and no more, a mine's proximity blink and blast, the Minelayer's rear entry under its bottom-edge warning,
 * the Gunship's facing armour and Dart streams, the Rail Serpent's turret cars and its wreck paying, the convoy
 * secondary ({@code CONVOY}: the tagged Gunships only, their escorts circling the right one), the rear and side share of
 * the waves, the radio's event lines, and whole runs of the autopilot ({@link MinePlay}: through the lines by a burned
 * cluster, clear of the rising layers' lanes) with the balance plan's fit, Rook in the slot and without him. The pacing,
 * the floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link
 * ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the mechanics themselves the
 * simulation's ({@code NoGroundTest}, {@code MineLineTest}, {@code MinelayerTest}, {@code GunshipArmourTest}, {@code
 * RailSerpentTest}, {@code RookMinesTest}).
 */
class Level30Test {
    static final String LEVEL = "act-5-the-belt/level-30-minefield";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The plan's load at the L30 visit (BalanceTest's sheet): 19.5 of the Mk IV's 20 MW. */
    static final double SPARE_POWER = 0.5;
    /** The rear and side waves' share of all waves at most (design/campaign, Act 5's direction rule). */
    static final double NON_FRONT_MOST = 0.35;
    /** The convoy escorts' orbit round their Gunship, px (the Talon's {@code orbit}). */
    static final double ORBIT = 90;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 30 ({@code "30"}: Level 29's with the Swarm Tail L2 on the rear in place of the
     * Tail Gun L3): the Ion Beam L4, both Micro-missile Pods L3, the Swarm Tail L2, the EMP Burst, the Mk III shield,
     * Composite III, the Mk IV's spare 0.5 MW, and Rook back in the escort slot with his Autocannon at full armour.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(rook(content));
    }

    /** Rook back in the slot: the Autocannon L1 (the plan's gun at his loss), at full armour. */
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
                        content.armour().plating().get(3).name(),
                        SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1));
    }

    /** The beam alone in front (no pods to hit other mines, no Rook), for the scripted mine flights. */
    private Loadout beamOnly(Difficulty difficulty, String leftWing, String special) {
        List<SimSpecs.FittedWeapon> weapons = leftWing.isEmpty()
                ? List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4))
                : List.of(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                        new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, leftWing, 1));
        Loadout loadout = SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                weapons,
                content.shields().models().get(2).name(),
                content.armour().plating().get(3).name(),
                0,
                difficulty);
        return special.isEmpty() ? loadout : loadout.withSpecial(SimSpecs.special(content, special, 2));
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

    /**
     * The README's waves at medium: Skitter 74, Mote Swarm 96, Talon 69 (the convoy's escorts among them), Asteroid Mite
     * 56 (on rocks), Minelayer 15 (eight columns from the rear: one alone, then pairs), Gilded Gunship 8 (three of them
     * the convoy, tagged) and Rail Serpent 4 (one from the right, one from behind, the pincer's two); easy and hard by
     * the formation lever (hard's columns of 3, 23 layers, and the convoy's escorts 3; easy's pincer one train; the
     * Mites' rocks on every difficulty). Nothing on the ground, one secret.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        String[] slugs = {
            "skitter", "mote-swarm", "talon", "asteroid-mite", "minelayer", "gilded-gunship", "rail-serpent"
        };
        int[][] counts = {{61, 72, 59, 56, 15, 8, 3}, {74, 96, 69, 56, 15, 8, 4}, {87, 120, 82, 56, 23, 8, 4}};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<WaveSpec> waves = level.waves();
            for (int i = 0; i < slugs.length; i++) {
                assertEquals(counts[difficulty.ordinal()][i], count(waves, slugs[i]), difficulty + " " + slugs[i]);
            }
            assertEquals(
                    3,
                    waves.stream()
                            .filter(wave -> wave.tag().equals("convoy"))
                            .mapToInt(WaveSpec::count)
                            .sum(),
                    difficulty + ": the convoy's three Gunships");
            assertTrue(level.groundUnits().isEmpty(), difficulty + ": nothing on the ground");
            assertTrue(level.ground().none(), difficulty + ": ground: none");
            assertEquals(1, level.secrets());
        }
    }
    /**
     * {@code ground: none}: a Bomb Rack fitted reads {@code NO GROUND} and drops nothing while the beam fires on; the
     * Airstrike is called (no {@code SPECIAL_DENIED}) and its bombs burst over the lane.
     */
    @Test
    void theBombsIdleAndTheAirstrikeWorks() {
        Sortie sortie =
                sortie(content, beamOnly(Difficulty.MEDIUM, "bomb-rack", "Airstrike"), 1, Difficulty.MEDIUM, true);
        assertEquals(IdleReason.NO_GROUND, sortie.mountIdleReason(1));
        assertEquals("NO GROUND", sortie.mountIdleReason(1).word());
        assertEquals(IdleReason.NONE, sortie.mountIdleReason(0));
        int bombs = 0;
        int called = 0;
        int denied = 0;
        int blasts = 0;
        int steps = 0;
        while (sortie.levelSeconds() < 30 && steps++ < MAX_STEPS) {
            boolean strike = sortie.levelSeconds() > 12 && called == 0;
            sortie.step(Command.FIRE.bit() | (strike ? Command.SPECIAL.bit() : 0));
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.SHOT_FIRED) {
                    bombs += events.value(i) == 1 ? 1 : 0;
                }
                called += type == SimEvents.Type.SPECIAL_CALLED ? 1 : 0;
                denied += type == SimEvents.Type.SPECIAL_DENIED ? 1 : 0;
                blasts += type == SimEvents.Type.AIRSTRIKE_BLAST ? 1 : 0;
            }
        }
        assertEquals(0, bombs, "no bombs without ground");
        assertEquals(1, called, "the Airstrike is called");
        assertEquals(0, denied);
        assertTrue(blasts > 0, "its bombs burst");
    }

    /**
     * The mine lines: every Minelayer wave enters from the rear with a 3 s warning, its column's layers 2.5 s apart, each
     * laying three clusters of 4 (32 px apart, 56 px between clusters) with one gap of 112 px (easy 144, the cluster beside
     * it 3) at the authored cluster boundary (160 or 320 px); the chain reaches a cluster's neighbour and not the next
     * cluster; hard arms in 0.8 s.
     */
    @Test
    void theMineLinesAreLaidInClustersWithTheirAuthoredGaps() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> layers = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().mineLayer().isPresent())
                    .toList();
            assertEquals(8, layers.size(), difficulty + ": eight columns");
            for (WaveSpec wave : layers) {
                String at = difficulty + " t=" + wave.t();
                assertEquals(WaveSpec.Entry.REAR, wave.entry(), at);
                assertEquals(3.0, wave.warningSeconds().orElse(0.0), 1e-9, at);
                WaveSpec.MineLines lines = wave.mineLines().orElseThrow();
                for (double gap : lines.gapXs()) {
                    assertTrue(gap == 160 || gap == 320, at + ": a gap at a cluster boundary " + gap);
                }
                assertTrue(lines.rows().size() >= wave.count(), at + ": a row for each layer");
                if (wave.count() > 1) {
                    assertEquals(2.5, wave.intervalSeconds().orElse(2.5), 1e-9, at + ": staggered 2.5 s");
                }
            }
            EnemySpec layer = layers.getFirst().enemy();
            EnemySpec.MineLayer lay = layer.mineLayer().orElseThrow();
            EnemyGun.MineSpec mine = layer.gun().orElseThrow().mine().orElseThrow();
            EnemyGun.LineMine line = mine.line().orElseThrow();
            assertEquals(3, lay.clusters(), difficulty.name());
            assertEquals(4, lay.perCluster(), difficulty.name());
            assertEquals(32, lay.spacing(), 1e-9);
            assertEquals(56, lay.clusterGap(), 1e-9);
            assertEquals(difficulty == Difficulty.EASY ? 144 : 112, lay.gap(), 1e-9, difficulty.name());
            assertEquals(difficulty == Difficulty.EASY ? 1 : 0, lay.middleShort(), difficulty.name());
            assertEquals(48, line.proximity(), 1e-9);
            assertEquals(40, line.blastRadius(), 1e-9);
            assertTrue(line.chainRadius() >= lay.spacing() && line.chainRadius() < lay.clusterGap(), "one cluster");
            assertTrue(lay.gap() / 2 > line.proximity(), "the gap's middle is outside both edge mines' proximity");
            assertEquals(difficulty == Difficulty.HARD ? 0.8 : 1.0, mine.armSeconds(), 1e-9, difficulty.name());
        }
    }

    /** One step toward (x, y), firing or not (the scripted flights). */
    private static void steer(Sortie sortie, double x, double y, boolean fire) {
        sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, y) | (fire ? Command.FIRE.bit() : 0));
    }

    private static int lineMines(Sortie sortie) {
        int n = 0;
        for (int i = 0; i < sortie.mineCount(); i++) {
            n += sortie.mine(i).line() ? 1 : 0;
        }
        return n;
    }

    private static boolean allArmed(Sortie sortie) {
        for (int i = 0; i < sortie.mineCount(); i++) {
            if (sortie.mine(i).line() && !sortie.mine(i).armed()) {
                return false;
            }
        }
        return true;
    }

    /** Steps the sortie toward (x, 40) without firing until the first line's 12 mines are laid (and armed). */
    private static int untilTheFirstLine(Sortie sortie, double x, boolean armed, int steps) {
        while (!(lineMines(sortie) == 12 && (!armed || allArmed(sortie))) && steps++ < MAX_STEPS) {
            steer(sortie, x, 40, false);
        }
        assertTrue(sortie.levelSeconds() < 30, "the first line is laid by " + sortie.levelSeconds());
        return steps;
    }

    /** The first line's mines' x, sorted. */
    private static double[] lineXs(Sortie sortie) {
        double[] xs = new double[lineMines(sortie)];
        int n = 0;
        for (int i = 0; i < sortie.mineCount(); i++) {
            if (sortie.mine(i).line()) {
                xs[n++] = sortie.mine(i).renderX(1);
            }
        }
        java.util.Arrays.sort(xs);
        return xs;
    }

    /**
     * The first line (medium, the t=20 layer alone, up the left lane 70 px in from below the bottom edge, which warned 3 s
     * ahead): laid from the left, its gap at the centre-left boundary (112 px); laid and armed, one shot from below at its
     * right cluster's lowest mine sets off that cluster's 3 others and no more (the middle cluster's 4 stay). Laid at the
     * first section's 120 px/s, a cluster's neighbours lie 41 px apart on the slope: step C3's 44 px chain reach.
     */
    @Test
    void aShotMineClearsItsCluster() {
        Sortie sortie = sortie(content, beamOnly(Difficulty.MEDIUM, "", ""), 1, Difficulty.MEDIUM, true);
        int steps = 0;
        boolean warned = false;
        double enteredX = Double.NaN;
        double enteredY = Double.NaN;
        while (lineMines(sortie) < 12 && steps++ < MAX_STEPS) {
            steer(sortie, 372, 40, false);
            warned |= sortie.levelSeconds() > 16.5
                    && sortie.levelSeconds() < 20
                    && WarningEdge.BOTTOM.in(sortie.edgeWarnings());
            for (int i = 0; i < sortie.enemyCount() && Double.isNaN(enteredX); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().mineLayer().isPresent()) {
                    enteredX = enemy.renderX(1);
                    enteredY = enemy.renderY(1);
                }
            }
        }
        assertTrue(warned, "the bottom edge warned ahead of the first layer");
        assertTrue(enteredY < 0, "the layer enters below the bottom edge: " + enteredY);
        assertEquals(70, enteredX, 1, "up the left lane, 70 px in");
        steps = untilTheFirstLine(sortie, 372, true, steps);
        double[] xs = lineXs(sortie);
        assertEquals(220 - 108, xs[4] - xs[3], 1, "the gap at the centre-left boundary");
        assertEquals(56, xs[8] - xs[7], 1, "the space between the middle and the right cluster");
        Mine target = null;
        for (int i = 0; i < sortie.mineCount(); i++) {
            Mine mine = sortie.mine(i);
            if (mine.renderX(1) >= xs[8] - 1 && (target == null || mine.renderY(1) < target.renderY(1))) {
                target = mine;
            }
        }
        double targetX = target.renderX(1);
        int destroyed = 0;
        int chained = 0;
        for (int k = 0; k < SimStep.ticks(0.5) && steps++ < MAX_STEPS; k++) {
            steer(sortie, targetX, 40, destroyed == 0);
            destroyed += sortie.events().count(SimEvents.Type.MINE_DESTROYED);
            chained += sortie.events().count(SimEvents.Type.MINE_CHAINED);
        }
        assertEquals(1, destroyed, "one mine shot");
        assertEquals(3, chained, "its cluster's three others set off");
        int middle = 0;
        int right = 0;
        for (int i = 0; i < sortie.mineCount(); i++) {
            double x = sortie.mine(i).renderX(1);
            middle += x > xs[4] - 1 && x < xs[7] + 1 ? 1 : 0;
            right += x > xs[8] - 1 ? 1 : 0;
        }
        assertEquals(4, middle, "the middle cluster stays");
        assertEquals(0, right, "the right cluster is gone");
    }

    /**
     * The first line again, the ship waiting in its gap 40 px beside the gap's right edge mine (outside the others'
     * proximity): the mine blinks as it comes level and bursts with its ring and its 40 px blast, which hits the hull
     * once (heavy 10).
     */
    @Test
    void aBlinkingMineBlasts() {
        Sortie sortie = sortie(content, beamOnly(Difficulty.MEDIUM, "", ""), 1, Difficulty.MEDIUM, false);
        int steps = untilTheFirstLine(sortie, 160, false, 0);
        double waitX = lineXs(sortie)[4] - 40;
        int blinks = 0;
        int proximityBursts = 0;
        int blastHits = 0;
        double blastDamage = 0;
        for (int k = 0; k < SimStep.ticks(8) && steps++ < MAX_STEPS && blastHits == 0; k++) {
            steer(sortie, waitX, 40, false);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                blinks += events.type(i) == SimEvents.Type.MINE_BLINK ? 1 : 0;
                proximityBursts += events.type(i) == SimEvents.Type.MINE_BURST && events.value(i) == 1 ? 1 : 0;
                if (events.type(i) == SimEvents.Type.MINE_BLAST_HIT) {
                    blastHits++;
                    blastDamage = events.value(i);
                }
            }
        }
        assertEquals(1, blinks, "the edge mine blinks");
        assertEquals(1, proximityBursts, "and bursts");
        assertEquals(1, blastHits, "its blast hits the hull once");
        assertEquals(10, blastDamage, 1e-9, "heavy 10");
    }

    /**
     * The units' data as the level flies them: the Gunship's plating (front ±45° ×0.5, engines ±60° ×2) and its Dart
     * streams; the Rail Serpent's turret car every third car and its cut a wreck; easy's 8 cars, medium's 10, hard's 14
     * (behind the engine: its members' offsets are the engine's and the cars').
     */
    @Test
    void theGunshipAndTheTrainAreBuiltAsTheirStatBlocks() {
        int[] cars = {8, 10, 14};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> waves = SimSpecs.level(content, LEVEL, difficulty).waves();
            EnemySpec gunship = waves.stream()
                    .map(WaveSpec::enemy)
                    .filter(enemy -> enemy.slug().equals("gilded-gunship"))
                    .findFirst()
                    .orElseThrow();
            EnemySpec.FacingArmour plating = gunship.facingArmour().orElseThrow();
            assertEquals(Math.toRadians(45), plating.frontHalfArcRadians(), 1e-9);
            assertEquals(0.5, plating.frontFactor(), 1e-9);
            assertEquals(Math.toRadians(60), plating.rearHalfArcRadians(), 1e-9);
            assertEquals(2, plating.rearFactor(), 1e-9);
            assertEquals("dart-drone", gunship.launcher().orElseThrow().unit().slug());
            EnemySpec train = waves.stream()
                    .map(WaveSpec::enemy)
                    .filter(enemy -> enemy.slug().equals("rail-serpent"))
                    .findFirst()
                    .orElseThrow();
            ChainSpecs.ChainSpec chain = train.chain().orElseThrow();
            assertEquals(3, chain.cars().orElseThrow().every(), difficulty.name());
            assertEquals("rail-serpent-turret", chain.cars().get().turret().slug());
            assertTrue(chain.scatter().orElseThrow().wreck(), "a cut wrecks the rear part");
            assertEquals(cars[difficulty.ordinal()] + 1, chain.offsets().size(), difficulty + ": cars");
        }
    }

    /**
     * Rail Serpent's wave entries and the rear/side share: every wave from the rear or a side edge is warned 3 s ahead,
     * and those waves are at most 35 % of all (the README: 17 % at medium).
     */
    @Test
    void theRearAndSideEntriesAreWarnedAndAFewOfAll() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> waves = SimSpecs.level(content, LEVEL, difficulty).waves();
            List<WaveSpec> other = waves.stream()
                    .filter(wave -> wave.entry() != WaveSpec.Entry.FRONT)
                    .toList();
            for (WaveSpec wave : other) {
                assertEquals(3.0, wave.warningSeconds().orElse(0.0), 1e-9, difficulty + " t=" + wave.t());
            }
            double share = other.size() / (double) waves.size();
            System.out.printf(
                    "Level 30 %s: %d of %d waves from the rear or a side (%.0f %%)%n",
                    difficulty, other.size(), waves.size(), 100 * share);
            assertTrue(share <= NON_FRONT_MOST, difficulty + ": " + share);
        }
    }

    /** The convoy secondary: the tagged Gunships, none may escape (easy one), the tracker's {@code CONVOY}. */
    @Test
    void theConvoyIsTheSecondary() {
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary =
                    SimSpecs.level(content, LEVEL, difficulty).objectives().secondary();
            assertEquals("gilded-gunship", secondary.escapes());
            assertEquals("convoy", secondary.tag());
            assertEquals("CONVOY", secondary.label());
            assertEquals(difficulty == Difficulty.EASY ? 1 : 0, secondary.escapesAllowed(), difficulty.name());
        }
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The levels seconds each enemy serial was first in the pool, and its slug and tag. */
        final Map<Integer, Double> born = new HashMap<>();
        /** Each convoy Talon escort's mean distance to the convoy Gunship born with it, and to the others. */
        final Map<Integer, double[]> escorts = new HashMap<>();
        /** The radio cues' triggers that started, by trigger. */
        final Map<CueTrigger, Integer> radio = new EnumMap<>(CueTrigger.class);
        /** The level second the secondary's met line started (NaN for none). */
        double metAt = Double.NaN;
        /** Steps Rook's centre was within an armed live line mine's proximity. */
        int rookInProximity;
        /** Steps a wreck's car blew and the credits rose. */
        int wreckPaid;

        private int wrecks;
        private int credits;

        @Override
        public void newAttempt() {
            events.clear();
            born.clear();
            escorts.clear();
            radio.clear();
            metAt = Double.NaN;
            rookInProximity = 0;
            wreckPaid = 0;
            wrecks = 0;
            credits = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                if (seen.type(i) == SimEvents.Type.RADIO) {
                    RadioCue cue = sortie.script().radio().get(seen.value(i));
                    radio.merge(cue.trigger(), 1, Integer::sum);
                    if (cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                        metAt = sortie.levelSeconds();
                    }
                }
            }
            double now = sortie.levelSeconds();
            int wreckCars = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                born.putIfAbsent(enemy.serial(), now);
                if (enemy.chain() != null && enemy.chain().wreck()) {
                    wreckCars++;
                }
            }
            if (wreckCars < wrecks && sortie.credits() > credits) {
                wreckPaid++;
            }
            wrecks = wreckCars;
            credits = sortie.credits();
            // The convoy's escorts: Talons born with a convoy Gunship, their distance to each convoy Gunship.
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy talon = sortie.enemy(i);
                if (!talon.spec().slug().equals("talon")) {
                    continue;
                }
                double from = born.get(talon.serial());
                for (int j = 0; j < sortie.enemyCount(); j++) {
                    Enemy gunship = sortie.enemy(j);
                    if (!gunship.tag().equals("convoy") || Math.abs(born.get(gunship.serial()) - from) > 0.01) {
                        continue;
                    }
                    if (now - from > 1 && now - from < 4 && talon.renderY(1) < 540) {
                        double own = Math.hypot(
                                talon.renderX(1) - gunship.renderX(1), talon.renderY(1) - gunship.renderY(1));
                        double others = Double.POSITIVE_INFINITY;
                        for (int k = 0; k < sortie.enemyCount(); k++) {
                            Enemy other = sortie.enemy(k);
                            if (other != gunship && other.tag().equals("convoy")) {
                                others = Math.min(
                                        others,
                                        Math.hypot(
                                                talon.renderX(1) - other.renderX(1),
                                                talon.renderY(1) - other.renderY(1)));
                            }
                        }
                        double[] d = escorts.computeIfAbsent(talon.serial(), s -> new double[3]);
                        d[0] += own;
                        d[1] = Math.min(d[1] == 0 ? Double.POSITIVE_INFINITY : d[1], others);
                        d[2]++;
                    }
                }
            }
            if (sortie.wingman().isPresent()) {
                Wingman rook = sortie.wingman().get();
                for (int i = 0; i < sortie.mineCount() && !rook.ejected(); i++) {
                    Mine mine = sortie.mine(i);
                    if (mine.line()
                            && mine.armed()
                            && !mine.setOff()
                            && !mine.blinking()
                            && Math.hypot(mine.renderX(1) - rook.x(), mine.renderY(1) - rook.y()) <= 48) {
                        rookInProximity++;
                    }
                }
            }
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
        }
    }

    /**
     * The autopilot completes the level with the plan's fit and Rook on every difficulty and seed within the retries the
     * other levels take, and meets the convoy secondary in most runs. On the attempt that completes it: the lines are
     * laid and some of their mines shot and chained, the Gunships' Darts launched, the trains' turret cars fire, the
     * convoy's escorts circle their own Gunship (about 90 px from it, nearer it than any other convoy Gunship), Rook keeps
     * out of the mines' proximity (no step with his centre in an armed mine's disc). Printed: the secondary's rate, the
     * credits, the retries, the events, when the secondary's line started.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 30 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            StringBuilder seen = new StringBuilder();
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, planLoadout(content, difficulty), seed, difficulty, false);
                Watch watch = new Watch();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
                String at = difficulty + " seed " + seed;
                assertTrue(watch.count(SimEvents.Type.MINE_DROPPED) > 0, at + ": lines laid");
                assertTrue(watch.count(SimEvents.Type.MINE_CHAINED) > 0, at + ": clusters set off");
                assertTrue(watch.count(SimEvents.Type.LAUNCHED) > 0, at + ": Darts launched");
                assertTrue(watch.count(SimEvents.Type.CAR_TELL) > 0, at + ": turret cars fire");
                assertEquals(0, watch.rookInProximity, at + ": Rook in a mine's proximity");
                assertEquals(1, watch.radio.getOrDefault(CueTrigger.LEVEL_END, 0), at + ": the closing line");
                assertEquals(
                        run.secondaryMet() ? 1 : 0,
                        watch.radio.getOrDefault(CueTrigger.SECONDARY_OBJECTIVE, 0),
                        at + ": the convoy line");
                int escortsSeen = 0;
                for (double[] d : watch.escorts.values()) {
                    double mean = d[0] / d[2];
                    assertEquals(ORBIT, mean, 15, at + ": an escort's orbit");
                    assertTrue(mean < d[1], at + ": nearer its own Gunship");
                    escortsSeen++;
                }
                assertTrue(escortsSeen >= (difficulty == Difficulty.HARD ? 6 : 4), at + ": escorts " + escortsSeen);
                seen.append(String.format(
                        "%n    seed %d: dropped %d, shot %d, chained %d, blinks %d, blasts %d, launched %d, tells %d,"
                                + " wrecks %d (paid %d), met at %.1f, escorts %d",
                        seed,
                        watch.count(SimEvents.Type.MINE_DROPPED),
                        watch.count(SimEvents.Type.MINE_DESTROYED),
                        watch.count(SimEvents.Type.MINE_CHAINED),
                        watch.count(SimEvents.Type.MINE_BLINK),
                        watch.count(SimEvents.Type.MINE_BLAST_HIT),
                        watch.count(SimEvents.Type.LAUNCHED),
                        watch.count(SimEvents.Type.CAR_TELL),
                        watch.count(SimEvents.Type.CHAIN_WRECK),
                        watch.wreckPaid,
                        watch.metAt,
                        escortsSeen));
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d%s",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries, seen));
            assertTrue(met > SEEDS / 2, difficulty + ": the convoy stopped in most runs, " + met + "/" + SEEDS);
        }
        System.out.println(report);
    }

    /** A cut train's rear part wrecks and its cars pay as they blow: flown on the seeds until one is seen. */
    @Test
    void aWreckedTrainPays() {
        int paid = 0;
        int wrecks = 0;
        for (Difficulty difficulty : Difficulty.values()) {
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, planLoadout(content, difficulty), seed, difficulty, false);
                Watch watch = new Watch();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch));
                paid += watch.wreckPaid;
                wrecks += watch.count(SimEvents.Type.CHAIN_WRECK);
            }
        }
        System.out.printf("Level 30: %d wrecks, %d steps a wreck's car blew and paid%n", wrecks, paid);
        assertTrue(wrecks > 0, "a train cut");
        assertTrue(paid > 0, "its cars pay");
    }

    /** Without Rook ({@code --escort none}) the autopilot completes the level, one seed per difficulty. */
    @Test
    void theLevelFliesWithoutRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, withoutRook(content, difficulty), 1, difficulty, false);
            assertTrue(LevelMetrics.fly(sortie, MAX_STEPS), difficulty.name());
            LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
            System.out.printf(
                    "Level 30 --escort none, %s: secondary met %s, credits %d, retries %d%n",
                    difficulty, run.secondaryMet(), run.credits(), run.retries());
        }
    }

    /**
     * The radio: the event lines once each (the convoy met, a Gunship escaped, the closing line; the secret's line is
     * the secret's); Rook's bark needs him flying ({@code requires: escort}), his scripted lines do not; no line on the
     * enemy channel.
     */
    @Test
    void theRadioHasTheLevelsEventLines() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<RadioCue> radio = SimSpecs.level(content, LEVEL, difficulty).radio();
            for (CueTrigger trigger :
                    List.of(CueTrigger.SECONDARY_OBJECTIVE, CueTrigger.ENEMY_ESCAPED, CueTrigger.LEVEL_END)) {
                assertEquals(
                        1,
                        radio.stream().filter(cue -> cue.trigger() == trigger).count(),
                        difficulty + ": " + trigger);
            }
            assertEquals(
                    "gilded-gunship",
                    radio.stream()
                            .filter(cue -> cue.trigger() == CueTrigger.ENEMY_ESCAPED)
                            .findFirst()
                            .orElseThrow()
                            .subject());
            int barks = 0;
            for (RadioCue cue : radio) {
                boolean bark = (cue.requires() & RadioCue.FITTED_ESCORT) != 0;
                if (bark) {
                    assertEquals("Rook", cue.speaker(), cue.line());
                    barks++;
                }
                assertFalse(
                        content.voices().channelOf(cue.speaker()) == Channel.ENEMY, cue.speaker() + ": " + cue.line());
            }
            assertEquals(1, barks, difficulty + ": Rook's one bark");
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level30Test::flyToEnd);

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
