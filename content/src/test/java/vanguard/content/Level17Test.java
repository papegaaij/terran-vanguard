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
import vanguard.sim.AllySpec;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part D, step D4: Level 17 Dome Siege from its data (design/campaign/act-3-red-dust/level-17-dome-siege;
 * design/allies/ares-landing-dome, design/enemies/air/choir-herald, design/enemies/ground/shellback; round 36's M8 =
 * a, M16 = a, Q.17, Q.22d/g, Q.25b/c and the gap check's defaults, pending the user's review in round 40): the five
 * domes per difficulty (hits 4 / 3 / 2, Eden's 144 px, the hook per unit type), their entry under their backdrop
 * aprons, the {@code untouched} secondary, the Choir Herald's aura and song, the Shellback's vent and curl-roll, the
 * hooked bombers' runs and the Shellbacks' lobs and rolls hurting the domes, the quiet windows, the radio's data.
 */
class Level17Test {
    static final String LEVEL = "act-3-red-dust/level-17-dome-siege";
    private static final int MAX_STEPS = 60 * 60 * 30;
    private static final List<String> DOMES = List.of("Bradbury", "Lowell", "Schiaparelli", "Burroughs", "Eden");

    private final Content content = ContentLoader.fromClasspath();

    /** The balance plan's fit at Level 17: Level 16's (the plan buys nothing at the L17 visit: {@code "17": {}}). */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level16Test.planLoadout(content, difficulty);
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty) {
        return sortie(content, seed, difficulty, false);
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

    /** The five domes: hits 4 / 3 / 2, Eden 144 px, a bomb run, a lob and a roll one hit each; the hook per unit type. */
    @Test
    void theDomesPerDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            LevelScript.GroundAllies allies = level.groundAllies().orElseThrow();
            assertEquals(
                    DOMES,
                    allies.units().stream().map(LevelScript.GroundAlly::name).toList());
            assertEquals(
                    List.of(38.0, 46.0, 104.0, 110.0, 126.0),
                    allies.units().stream()
                            .map(LevelScript.GroundAlly::enterSeconds)
                            .toList());
            assertEquals(
                    List.of(130.0, 350.0, 110.0, 370.0, 240.0),
                    allies.units().stream().map(LevelScript.GroundAlly::x).toList());
            for (LevelScript.GroundAlly unit : allies.units()) {
                AllySpec dome = unit.ally();
                boolean eden = unit.name().equals("Eden");
                assertEquals("ares-landing-dome", dome.slug());
                assertEquals(
                        switch (difficulty) {
                            case EASY -> 4;
                            case MEDIUM -> 3;
                            case HARD -> 2;
                        },
                        dome.hp(),
                        difficulty + ": hits");
                assertEquals(eden ? 144 : 120, dome.size().width());
                assertEquals(eden ? 112 : 88, dome.hitbox().width());
                assertEquals(1, dome.bombs());
                assertEquals(1, dome.rolls());
                assertTrue(dome.lobs());
                assertEquals(0, dome.dives());
                assertFalse(dome.objectiveAimed() || dome.bullets() || dome.contact());
            }
            assertEquals(LevelScript.HookMode.ALWAYS, allies.mode("spore-bomber"));
            assertEquals(
                    difficulty == Difficulty.EASY ? LevelScript.HookMode.NONE : LevelScript.HookMode.ALTERNATE,
                    allies.mode("shellback"));
            assertEquals(LevelScript.HookMode.NONE, allies.mode("choir-herald"));
            assertEquals(220, allies.bombRow(), "round 40 R.b: from 160");
            assertEquals(40, allies.bombSlot());
            LevelScript.Secondary secondary = level.secondary();
            assertTrue(secondary.untouched());
            assertEquals(100, secondary.credits());
            assertEquals("DOMES", secondary.label());
            assertTrue(level.convoy().isEmpty()
                    && level.escort().isEmpty()
                    && level.defend().isEmpty());
        }
    }

    /**
     * Each dome's footprint enters the top edge at its time (its lower edge there, ±1 step) and its backdrop apron
     * (the {@code dome-apron-*} pieces, placed by their centre passing the middle of the screen) lies under it.
     */
    @Test
    void theDomesEnterAtTheirTimesUnderTheirAprons() {
        LevelData data = content.level(LEVEL);
        List<BackdropData.PlacedPiece> aprons = data.backdrop().placements().stream()
                .filter(p -> p.piece().startsWith("dome-apron"))
                .sorted(java.util.Comparator.comparingDouble(BackdropData.PlacedPiece::t))
                .toList();
        assertEquals(5, aprons.size());
        Sortie sortie = sortie(content, 17, Difficulty.MEDIUM, true);
        double[] entered = new double[5];
        java.util.Arrays.fill(entered, -1);
        double[] underApron = new double[5];
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            sortie.step(0);
            double t = sortie.levelSeconds();
            for (int k = 0; k < 5; k++) {
                double half = sortie.allySpec(k).size().height() / 2;
                double bottom = sortie.ally(k).y() - half;
                if (entered[k] < 0 && bottom <= PlayField.HEIGHT) {
                    entered[k] = t;
                }
                if (Math.abs(t - aprons.get(k).t()) < SimStep.SECONDS / 2) {
                    underApron[k] = sortie.ally(k).y();
                }
            }
        }
        List<Double> times = List.of(38.0, 46.0, 104.0, 110.0, 126.0);
        for (int k = 0; k < 5; k++) {
            assertEquals(times.get(k), entered[k], SimStep.SECONDS * 1.5, DOMES.get(k) + " enters");
            assertEquals(PlayField.HEIGHT / 2.0, underApron[k], 1.5, DOMES.get(k) + "'s centre at its apron's");
            assertEquals(sortie.ally(k).x(), aprons.get(k).x(), 1e-9, DOMES.get(k) + "'s apron's x");
            assertEquals(
                    k == 4 ? "dome-apron-eden" : "dome-apron-" + (k % 2 == 0 ? "west" : "east"),
                    aprons.get(k).piece());
        }
    }

    /**
     * The hooked waves and the quiet windows: no other wave flies 30.5–34 (the first Shellback) nor 55.5–59 (the first
     * Herald); the t=124 bombers are a plain line abreast beside the Herald (a bomb run never applies to escorts), and
     * the Herald carries the shield cell ({@code dropped_by.enemy}).
     */
    @Test
    void theHookedWavesAndTheQuietWindows() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<String> quiet = level.waves().stream()
                    .filter(w -> (w.t() > 30.5 && w.t() < 34) || (w.t() > 55.5 && w.t() < 59))
                    .map(w -> w.t() + " " + w.enemy().slug())
                    .toList();
            assertEquals(List.of(), quiet, difficulty + ": the quiet windows");
            List<WaveSpec> at124 =
                    level.waves().stream().filter(w -> w.t() == 124).toList();
            assertEquals(2, at124.size());
            assertEquals("choir-herald", at124.get(0).enemy().slug());
            assertEquals(WaveSpec.Formation.CARRIER_ESCORTS, at124.get(0).formation());
            assertEquals(
                    List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, 0)),
                    at124.get(0).carried());
            assertEquals("spore-bomber", at124.get(1).enemy().slug());
            assertEquals(WaveSpec.Formation.LINE_ABREAST, at124.get(1).formation());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 2;
                        case MEDIUM -> 3;
                        case HARD -> 4;
                    },
                    at124.get(1).count());
            assertEquals(List.of(), at124.get(1).carried());
            long herald55 = level.waves().stream()
                    .filter(w -> w.t() == 55.5 && w.enemy().slug().equals("needler"))
                    .filter(w -> w.formation() == WaveSpec.Formation.CARRIER_ESCORTS)
                    .count();
            assertEquals(1, herald55, "the first Herald's Needlers are its escorts");
            assertEquals(
                    difficulty == Difficulty.HARD,
                    level.waves().stream().anyMatch(w -> w.t() == 126),
                    "the extra pair on hard only");
        }
    }

    /** The units per kind at medium, as the README's credit budget counts them. */
    @Test
    void theWavesFlyTheReadmesUnits() {
        Map<String, Integer> units = new TreeMap<>();
        for (WaveSpec wave : SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).waves()) {
            units.merge(wave.enemy().slug(), wave.count(), Integer::sum);
        }
        assertEquals(
                Map.of(
                        "dust-kite", 72,
                        "mote-swarm", 30,
                        "skitter", 24,
                        "needler", 18,
                        "stinger", 12,
                        "spore-bomber", 12,
                        "shellback", 5,
                        "mantis", 4,
                        "choir-herald", 2),
                units);
    }

    /** The Choir Herald: 60 HP, its aura 75 / 90 / 105 px, cap 40, refill 15 / 25 / 25 %; its song a ring of 10 (hard 12). */
    @Test
    void theChoirHeraldsAuraAndSong() {
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec herald = enemy(SimSpecs.level(content, LEVEL, difficulty), "choir-herald");
            EnemySpec.Aura aura = herald.aura().orElseThrow();
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 75;
                        case MEDIUM -> 90;
                        case HARD -> 105;
                    },
                    aura.radius());
            assertEquals(40, aura.cap());
            assertEquals(difficulty == Difficulty.EASY ? 0.15 : 0.25, aura.refillShare());
            assertEquals(2.0, aura.refillDelaySeconds());
            assertEquals(0.5, aura.fillSeconds());
            assertEquals(1.0, aura.keepSeconds());
            assertEquals(0.3, aura.collapseSeconds());
            assertEquals(0.8, aura.escortRadiusShare());
            assertEquals(content.difficulty().enemyHp(60, difficulty), herald.hp(), 1e-9);
            assertEquals(30, herald.bounty());
            EnemyGun song = herald.gun().orElseThrow();
            int count = difficulty == Difficulty.HARD ? 12 : 10;
            assertEquals(count, song.fan());
            assertEquals(2 * Math.PI * (count - 1) / count, song.spreadRadians(), 1e-12);
            if (difficulty == Difficulty.HARD) {
                assertEquals(3.2, song.intervalSeconds(), 1e-9);
            } else {
                assertEquals(4.0 / content.difficulty().enemyFireRate().of(difficulty), song.intervalSeconds(), 1e-9);
            }
            assertTrue(song.mortar().isEmpty() && song.mine().isEmpty());
        }
    }

    /**
     * The Shellback (round 40 K.a: weaker): the body 90 HP and 40 credits (whole 48 by its first bonus 8), the vent 16
     * HP and 12 at 15 px towards its tail, silencing the mortar; the roll once at 40 %: curl 0.4 s, roll 2.5 s (hard
     * 3.0) at 140 px/s, uncurl 0.4 s, the belly 1.5 s at ×1.5 shown at the 200 px row or higher (R.e), a 72 px ball;
     * the mortar lobs every 4.0 s (easy 5.0, hard 3.2).
     */
    @Test
    void theShellbacksVentAndRoll() {
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec shellback = enemy(SimSpecs.level(content, LEVEL, difficulty), "shellback");
            assertEquals(content.difficulty().enemyHp(90, difficulty), shellback.hp(), 1e-9);
            assertEquals(40, shellback.bounty());
            EnemySpec.UnitPart vent = shellback.part().orElseThrow();
            assertEquals("vent", vent.name());
            assertEquals(content.difficulty().enemyHp(16, difficulty), vent.hp(), 1e-9);
            assertEquals(12, vent.bounty());
            assertEquals(8, vent.firstBonus());
            assertEquals(0, vent.dx());
            assertEquals(15, vent.dy(), "the production sprite's vent, 14.6 px towards the tail");
            assertEquals(28, vent.hitbox().width());
            assertTrue(vent.silencesGun());
            EnemySpec.Roll roll = shellback.roll().orElseThrow();
            assertEquals(0.4, roll.belowShare());
            assertEquals(0.4, roll.curlSeconds());
            assertEquals(difficulty == Difficulty.HARD ? 3.0 : 2.5, roll.rollSeconds());
            assertEquals(140, roll.rollSpeed());
            assertEquals(0.4, roll.uncurlSeconds());
            assertEquals(1.5, roll.bellySeconds());
            assertEquals(200, roll.bellyRow());
            assertEquals(1.5, roll.bellyFactor());
            assertEquals(72, roll.ball().width());
            assertTrue(shellback.walker().isPresent());
            EnemyGun mortar = shellback.gun().orElseThrow();
            assertTrue(mortar.mortar().isPresent());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 6;
                        case MEDIUM -> 8;
                        case HARD -> 10;
                    },
                    mortar.mortar().orElseThrow().ring());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 5.0;
                        case MEDIUM -> 4.0;
                        case HARD -> 3.2;
                    },
                    mortar.intervalSeconds(),
                    1e-9);
            assertTrue(enemy(SimSpecs.level(content, LEVEL, difficulty), "needler")
                    .part()
                    .isEmpty());
        }
    }

    /**
     * Round 40 R.d and K.a: the first Herald (t=55.5) comes in at 50 px/s (half its own 100) with its Needlers, and
     * Varga's line about the rings is the {@code first-ring} cue: it starts in the step the Herald first rings a unit,
     * as it reaches the top edge, over a second before the autopilot (the Herald first) can shoot it down. Printed per
     * seed: the first ring, the line's start and the Herald's death.
     */
    @Test
    void theFirstHeraldComesInSlowlyAndVargasLineStartsAtItsFirstRing() {
        LevelScript script = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<WaveSpec> at55 = script.waves().stream().filter(w -> w.t() == 55.5).toList();
        assertEquals(2, at55.size());
        at55.forEach(wave -> assertEquals(
                java.util.Optional.of(50.0), wave.speed(), wave.enemy().slug()));
        int cue = -1;
        for (int i = 0; i < script.radio().size(); i++) {
            if (script.radio().get(i).trigger() == LevelScript.CueTrigger.FIRST_RING) {
                cue = i;
            }
        }
        assertTrue(cue >= 0 && script.radio().get(cue).line().contains("See the rings?"));
        StringBuilder report = new StringBuilder("Level 17 first Herald (first ring, Varga's line, death):");
        for (long seed = 1; seed <= 4; seed++) {
            Sortie sortie = sortie(content, seed, Difficulty.MEDIUM);
            int heraldKind =
                    sortie.enemyKinds().stream().map(EnemySpec::slug).toList().indexOf("choir-herald");
            double ring = -1;
            double line = -1;
            double death = -1;
            int steps = 0;
            while (death < 0 && steps++ < MAX_STEPS && sortie.levelSeconds() < 80) {
                sortie.step(Autopilot.commands(sortie));
                SimEvents events = sortie.events();
                for (int i = 0; i < events.size(); i++) {
                    if (events.type(i) == SimEvents.Type.RING_SUNG && ring < 0) {
                        ring = sortie.levelSeconds();
                    } else if (events.type(i) == SimEvents.Type.RADIO && events.value(i) == cue) {
                        line = sortie.levelSeconds();
                    } else if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED && events.value(i) == heraldKind) {
                        death = sortie.levelSeconds();
                    }
                }
            }
            report.append(String.format(" seed %d: %.2f, %.2f, %.2f;", seed, ring, line, death));
            assertTrue(ring > 55.5 && ring < 58, "seed " + seed + ": the first ring as the Herald comes in: " + ring);
            assertEquals(ring, line, 1e-9, "seed " + seed + ": the line at the first ring");
            assertTrue(death < 0 || death - line > 1, "seed " + seed + ": the line before its death: " + death);
        }
        System.out.println(report);
    }

    /**
     * Round 40 R.c: every Shellback walks between the domes on every difficulty (the autopilot flying, so they walk
     * their whole paths or die): while it walks its body's hit box never overlaps a standing dome's hit box (glass).
     */
    @Test
    void theShellbacksWalkBetweenTheDomes() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, 3, difficulty, true);
            int steps = 0;
            int walkSteps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(0);
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy e = sortie.enemy(i);
                    if (!e.spec().slug().equals("shellback") || e.curlPhase() != Enemy.CurlPhase.WALK) {
                        continue;
                    }
                    walkSteps++;
                    for (int k = 0; k < sortie.allyCount(); k++) {
                        AllySpec dome = sortie.allySpec(k);
                        boolean over = Math.abs(e.renderX(1) - sortie.ally(k).renderX(1))
                                        < (e.hitbox().width() + dome.hitbox().width()) / 2
                                && Math.abs(e.renderY(1) - sortie.ally(k).renderY(1))
                                        < (e.hitbox().height() + dome.hitbox().height()) / 2;
                        assertFalse(
                                over,
                                difficulty + ": a Shellback at t=" + sortie.levelSeconds() + " walks over "
                                        + DOMES.get(k));
                    }
                }
            }
            assertTrue(walkSteps > 1000, difficulty + ": the Shellbacks walked");
        }
    }

    /** Hits on the domes in an idle flight (nothing shot), by source. */
    record Hits(
            int bombs, int lobs, int bombHits, int lobHits, int rollHits, int lost, boolean failed, int untouched) {}

    /**
     * Left alone (the ship never fires, nothing hits it), the hooked bombers fly their runs and break domes (each dome
     * before a hooked Shellback's second lob, its first at a dome, could land), every dome takes at most its hits, the
     * mission never fails and the secondary fails at the first hit.
     */
    @Test
    void leftAloneTheBombersAndShellbacksBreakTheDomes() {
        Map<Difficulty, Hits> all = new TreeMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, 17, difficulty, true);
            int bombs = 0;
            int lobs = 0;
            int bombHits = 0;
            int lobHits = 0;
            int rollHits = 0;
            int lost = 0;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(0);
                SimEvents events = sortie.events();
                boolean bomb = false;
                boolean crush = false;
                for (int i = 0; i < events.size(); i++) {
                    switch (events.type(i)) {
                        case BOMB_DROPPED -> {
                            bombs++;
                            bomb = true;
                        }
                        case ROLL_CRUSHED -> crush = events.value(i) >= 0;
                        case MORTAR_LOBBED -> lobs++;
                        case ALLY_LOST -> lost++;
                        case ALLY_HIT -> {
                            if (bomb) {
                                bombHits++;
                            } else if (crush) {
                                rollHits++;
                            } else {
                                lobHits++;
                            }
                        }
                        default -> {}
                    }
                }
            }
            for (int k = 0; k < 5; k++) {
                assertTrue(sortie.ally(k).hitsTaken() <= sortie.allySpec(k).hp(), difficulty + " " + DOMES.get(k));
            }
            Hits hits = new Hits(
                    bombs, lobs, bombHits, lobHits, rollHits, lost, sortie.untouchedFailed(), sortie.alliesUntouched());
            all.put(difficulty, hits);
            assertTrue(sortie.complete(), difficulty + ": the domes never fail the mission");
            assertTrue(bombs >= 4, difficulty + ": the bomb runs reach their domes: " + hits);
            assertTrue(bombHits >= 4, difficulty + ": " + hits);
            assertTrue(lobs > 0, difficulty + ": the Shellbacks lob: " + hits);
            // Left alone the bombers break each dome before a Shellback's second lob (its first at a dome) lands.
            assertTrue(difficulty != Difficulty.EASY || lobHits == 0, difficulty + ": unhooked on easy: " + hits);
            assertTrue(hits.failed() && !sortie.result().secondaryMet(), difficulty + ": the secondary fails");
            assertTrue(hits.untouched() < 5);
            assertEquals(0, rollHits, difficulty + ": nothing is shot, nothing curls");
        }
        System.out.println("Level 17 left alone (bomb runs, their hits, lob hits, roll hits, domes lost): " + all);
    }

    /**
     * The t=46.5 Shellback walking down beside Lowell (round 40 R.c: 90 px to its left, the autopilot keeping the
     * bombers off Lowell until it is on the screen), shot to 40 % and then left alone, curls (0.4 s), rolls (2.5 s,
     * or round 40 R.e shorter: it uncurls where its belly shows at the 200 px row) and its ball grazes Lowell's
     * footprint: one hit. Walking it never overlaps Lowell's glass (its hit box).
     */
    @Test
    void aShellbacksRollCrushesTheDomeAheadOfIt() {
        Sortie sortie = sortie(content, 17, Difficulty.MEDIUM, true);
        Enemy shellback = null;
        int serial = -1;
        Map<Enemy.CurlPhase, Double> starts = new java.util.EnumMap<>(Enemy.CurlPhase.class);
        StringBuilder trace = new StringBuilder();
        List<Integer> crushed = new ArrayList<>();
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS && sortie.levelSeconds() < 70) {
            double t = sortie.levelSeconds();
            if (shellback == null && t > 46.5) {
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy e = sortie.enemy(i);
                    if (e.spec().slug().equals("shellback") && e.hp() > 0 && e.renderY(1) < PlayField.HEIGHT - 20) {
                        shellback = e;
                        serial = e.serial();
                    }
                }
            }
            // The autopilot until the Shellback is on the screen (it shoots the bombers going for Lowell), then
            // under the Shellback, firing until it curls, then nothing.
            int commands = shellback == null ? Autopilot.commands(sortie) : 0;
            if (shellback != null
                    && shellback.curlPhase() == Enemy.CurlPhase.WALK
                    && !starts.containsKey(Enemy.CurlPhase.CURLING)) {
                commands = steer(sortie, shellback.renderX(1)) | Command.FIRE.bit();
            }
            sortie.step(commands);
            if (shellback != null && shellback.serial() != serial) {
                // gone (rolled off the bottom edge): its pooled object flies another unit now
                trace.append(String.format(" gone at t=%.2f", sortie.levelSeconds()));
                break;
            }
            if (shellback != null) {
                Enemy.CurlPhase phase = shellback.curlPhase();
                if (!starts.containsKey(phase) && phase == Enemy.CurlPhase.BELLY) {
                    trace.append(String.format(" belly at y %.0f", shellback.renderY(1)));
                    assertTrue(
                            shellback.renderY(1) > 190, "its belly shows at the 200 px row: " + shellback.renderY(1));
                }
                starts.putIfAbsent(phase, sortie.levelSeconds());
                if (phase == Enemy.CurlPhase.WALK) {
                    AllySpec lowell = sortie.allySpec(1);
                    boolean over = Math.abs(shellback.renderX(1)
                                            - sortie.ally(1).renderX(1))
                                    < (shellback.hitbox().width()
                                                    + lowell.hitbox().width())
                                            / 2
                            && Math.abs(shellback.renderY(1) - sortie.ally(1).renderY(1))
                                    < (shellback.hitbox().height()
                                                    + lowell.hitbox().height())
                                            / 2;
                    assertFalse(over, "it walks beside Lowell's glass");
                }
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ROLL_CRUSHED) {
                    crushed.add(events.value(i));
                }
            }
        }
        System.out.println(
                "Level 17 Shellback behind Lowell, phase starts: " + starts + ", crushed " + crushed + ";" + trace);
        double curl = starts.get(Enemy.CurlPhase.CURLING);
        assertEquals(0.4, starts.get(Enemy.CurlPhase.ROLLING) - curl, 2 * SimStep.SECONDS);
        // Round 40 R.e: rolling down the screen near the bottom, it uncurls early, so its belly shows.
        double rolled = starts.get(Enemy.CurlPhase.UNCURLING) - starts.get(Enemy.CurlPhase.ROLLING);
        assertTrue(rolled > 0 && rolled < 2.5, "a shorter roll: " + rolled);
        assertTrue(starts.containsKey(Enemy.CurlPhase.BELLY));
        assertEquals(List.of(1), crushed, "Lowell, once");
    }

    /** Steers the ship along the bottom of the field under {@code x}. */
    private static int steer(Sortie sortie, double x) {
        double dx = x - sortie.ship().x();
        int commands = 0;
        if (dx < -3) {
            commands |= Command.LEFT.bit();
        } else if (dx > 3) {
            commands |= Command.RIGHT.bit();
        }
        if (sortie.ship().y() > 90) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    /** The radio's data: the timed lines in order and at least 4 s apart; Rook's three lines only while he flies. */
    @Test
    void theRadiosData() {
        List<LevelScript.RadioCue> radio =
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        List<LevelScript.RadioCue> timed = radio.stream()
                .filter(cue -> cue.trigger() == LevelScript.CueTrigger.TIME)
                .toList();
        for (int i = 1; i < timed.size(); i++) {
            assertTrue(
                    timed.get(i).t() - timed.get(i - 1).t() >= 4,
                    timed.get(i - 1).t() + " and " + timed.get(i).t());
        }
        List<Double> rook = timed.stream()
                .filter(cue -> (cue.requires() & LevelScript.RadioCue.FITTED_ESCORT) != 0)
                .map(LevelScript.RadioCue::t)
                .toList();
        assertEquals(List.of(14.0, 95.0, 143.0), rook);
        radio.forEach(cue -> assertFalse(cue.line().isBlank()));
        assertEquals(
                List.of(
                        LevelScript.CueTrigger.FIRST_RING,
                        LevelScript.CueTrigger.ALLY_HIT,
                        LevelScript.CueTrigger.ALLY_LOST,
                        LevelScript.CueTrigger.SECONDARY_OBJECTIVE,
                        LevelScript.CueTrigger.LEVEL_END),
                radio.stream()
                        .map(LevelScript.RadioCue::trigger)
                        .filter(trigger -> trigger != LevelScript.CueTrigger.TIME)
                        .filter(trigger -> trigger != LevelScript.CueTrigger.SECRET)
                        .toList());
        assertTrue(radio.stream()
                .filter(cue -> cue.trigger() == LevelScript.CueTrigger.ALLY_HIT)
                .allMatch(cue -> cue.line().contains("{ally}")));
    }

    /** Seeds per difficulty for the dome secondary's rate. */
    static final int SEEDS = 8;

    /**
     * The autopilot (the Herald first, the hooked bombers before their dome, a Shellback's vent and belly, never a
     * curled one) flies the level to its end with the plan's fit on every difficulty, over {@link #SEEDS} seeds.
     * Printed per difficulty: the dome secondary's rate (the README's "about half the runs"), the domes kept, the
     * rings' absorbs, the credits, the attempts, the domes' hits by source and the vents shot. Rook does not fly (the
     * plan's fit has no escort). On easy (the Shellbacks unhooked) the domes are kept at least once.
     */
    @Test
    void theAutopilotFliesTheLevelAndKeepsTheDomesAboutHalfTheTime() {
        StringBuilder report = new StringBuilder("Level 17 autopilot over " + SEEDS + " seeds:");
        int easyMet = 0;
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int kept = 0;
            int absorbs = 0;
            int retries = 0;
            long credits = 0;
            int bombHits = 0;
            int rollHits = 0;
            int lobHits = 0;
            int vents = 0;
            int shellbacks = 0;
            int shellbacksKilled = 0;
            StringBuilder seedOne = new StringBuilder();
            StringBuilder firsts = new StringBuilder();
            Map<Long, int[]> byEntry = new TreeMap<>();
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty);
                int shellbackKind = sortie.enemyKinds().stream()
                        .map(EnemySpec::slug)
                        .toList()
                        .indexOf("shellback");
                shellbacks += SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                        .filter(wave -> wave.enemy().slug().equals("shellback"))
                        .mapToInt(WaveSpec::count)
                        .sum();
                int steps = 0;
                int attempt = sortie.attempt();
                Map<Integer, double[]> walkers = new java.util.HashMap<>();
                double firstHit = -1;
                String firstBy = "";
                while (!sortie.complete() && steps++ < MAX_STEPS) {
                    if (!sortie.flying() || sortie.primaryFailed()) {
                        sortie.retry(sortie.ship().defences().maxArmour());
                    }
                    sortie.step(Autopilot.commands(sortie));
                    trackShellbacks(sortie, walkers, byEntry);
                    SimEvents events = sortie.events();
                    boolean bomb = false;
                    boolean crush = false;
                    for (int i = 0; i < events.size(); i++) {
                        switch (events.type(i)) {
                            case RING_ABSORBED -> absorbs++;
                            case UNIT_PART_DESTROYED -> vents++;
                            case ENEMY_DESTROYED -> shellbacksKilled += events.value(i) == shellbackKind ? 1 : 0;
                            case BOMB_DROPPED -> bomb = true;
                            case ROLL_CRUSHED -> crush = events.value(i) >= 0;
                            case ALLY_HIT -> {
                                if (bomb) {
                                    bombHits++;
                                } else if (crush) {
                                    rollHits++;
                                } else {
                                    lobHits++;
                                }
                                if (seed == 1) {
                                    seedOne.append(String.format(
                                            " %.1f %s %s;",
                                            sortie.levelSeconds(),
                                            bomb ? "bomb" : crush ? "roll" : "lob",
                                            DOMES.get(events.value(i))));
                                }
                                if (firstHit < 0) {
                                    firstHit = sortie.levelSeconds();
                                    firstBy = (bomb ? "bomb " : crush ? "roll " : "lob ") + DOMES.get(events.value(i));
                                }
                            }
                            default -> {}
                        }
                    }
                    if (sortie.attempt() != attempt) {
                        attempt = sortie.attempt();
                        firstHit = -1;
                    }
                }
                if (firstHit >= 0) {
                    firsts.append(String.format(" %.0f %s;", firstHit, firstBy));
                }
                assertTrue(sortie.complete(), difficulty + " seed " + seed);
                met += sortie.result().secondaryMet() ? 1 : 0;
                kept += sortie.alliesUntouched();
                retries += sortie.attempt() - 1;
                credits += sortie.result().credits().total();
            }
            if (difficulty == Difficulty.EASY) {
                easyMet = met;
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, domes untouched %.1f of 5, ring absorbs %.0f per run, credits %.0f,"
                            + " retries %d; dome hits by bombs %d, lobs %d, rolls %d; vents shot %d; Shellbacks killed"
                            + " %d/%d (killed/walked by entry second %s); first hits:%s; seed 1's hits:%s",
                    difficulty,
                    met,
                    SEEDS,
                    kept / (double) SEEDS,
                    absorbs / (double) SEEDS,
                    credits / (double) SEEDS,
                    retries,
                    bombHits,
                    lobHits,
                    rollHits,
                    vents,
                    shellbacksKilled,
                    shellbacks,
                    byEntry.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue()[0] + "/" + e.getValue()[1])
                            .toList(),
                    firsts,
                    seedOne));
        }
        System.out.println(report);
        assertTrue(easyMet > 0, "easy: the domes can be kept untouched (the Shellbacks unhooked)");
    }

    /**
     * Follows the Shellbacks of {@code sortie} by serial ({entry second, last height}); one that is gone counts in
     * {@code byEntry} under its entry second as {killed, walked}: killed when it was last seen on the screen.
     */
    static void trackShellbacks(Sortie sortie, Map<Integer, double[]> walkers, Map<Long, int[]> byEntry) {
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy e = sortie.enemy(i);
            if (e.spec().slug().equals("shellback") && e.hp() > 0) {
                walkers.computeIfAbsent(e.serial(), k -> new double[] {sortie.levelSeconds(), 0})[1] = e.renderY(1);
                seen.add(e.serial());
            }
        }
        for (var it = walkers.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            if (!seen.contains(entry.getKey())) {
                int[] counts = byEntry.computeIfAbsent(Math.round(entry.getValue()[0]), k -> new int[2]);
                counts[0] += entry.getValue()[1] > -30 ? 1 : 0;
                counts[1]++;
                it.remove();
            }
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD), Level17Test::flyToEnd);

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

    private static EnemySpec enemy(LevelScript level, String slug) {
        return level.waves().stream()
                .map(WaveSpec::enemy)
                .filter(enemy -> enemy.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }
}
