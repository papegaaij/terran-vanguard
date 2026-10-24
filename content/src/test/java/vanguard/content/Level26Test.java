package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.LurePhase;
import vanguard.sim.MediumSpec;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.RookLossSpec;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.WaveSpec;
import vanguard.sim.Wingman;
import vanguard.sim.WingmanSpec;

/**
 * M8 part F, step F3: Level 26 Blackwater from its data (design/campaign/act-4-deep-water/level-26-blackwater;
 * design/enemies/naval/glow-angler; design/player/wingmen, Rook's loss): its count, its six sections under water from the
 * start, the dark as the look's keys only (the simulation knows no darkness), the fault scarp (one wall, three notches,
 * seven nests one at a time), the quiet windows, the Glow Angler (its lunge by difficulty, lunging and ringing in flight),
 * Rook's loss at t=163.5 (the trails, the spin on the screen, replayed on a retry; with the Bulwark or without an escort
 * the radio's only), the Choir's line, the carriers (the constellation's centre Angler), the kill-ratio secondary, the
 * credits of a perfect run, the radio, and whole runs of the autopilot with the balance plan's fit ({@link DarkPlay}:
 * every move planned off the bullets, out of the Anglers' triggers and the jellies' reach) with and without Rook. The
 * pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link PacingTest},
 * {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the lunge and the loss
 * themselves the simulation's ({@code GlowAnglerTest}, {@code RookLossTest}).
 */
class Level26Test {
    static final String LEVEL = "act-4-deep-water/level-26-blackwater";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The Siren nests' waves: one nest each, on the scarp's foot. */
    static final double[] NESTS = {61.2, 66.8, 72.4, 78, 83.6, 89.2, 94.8};
    /** Rook's three scripted lines (no {@code requires}: they play in every case): t, start of the line. */
    static final double[] ROOK_LINES = {13, 148, 161.2};

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 26 ({@code "26"}: the Harpoon Torpedoes to L4 on Level 25's): the harpoon L4 in
     * front, both Micro-missile Pods L3, the Depth Charges L3, the Sonar Pulse, the Pressure hull, the Mk II shield,
     * Composite III, and Rook's Autocannon.
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
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 4),
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

    /**
     * The README: 223 enemies at medium, all in waves (Driftjelly 118, Glow Angler 78, Spiral Nautilus 12, Eel Swarm 8,
     * Siren 7); the farm barge is a ground trigger with its secret (224 with it). Easy 184, hard 262 by the formation
     * lever.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(223, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        String[] slugs = {"driftjelly", "glow-angler", "spiral-nautilus", "eel-swarm", "siren"};
        int[] counts = {118, 78, 12, 8, 7};
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
        assertEquals(List.of(), level.groundUnits());
        assertEquals(1, level.secrets());
        int[] waves = {184, 223, 262};
        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(
                    waves[difficulty.ordinal()],
                    SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                            .mapToInt(LevelMetrics::planned)
                            .sum(),
                    difficulty.name());
        }
    }

    /**
     * Six sections, the whole level under water from its start (no waterline): the last lamp at 130 px/s to 20 s, the
     * dark, the scarp, the Choir and the deep at 120 to 60, 100, 130 and 155 s, the silence at 110 to 188 s, each change
     * of speed eased over 2 s; under water the Airstrike and the Orbital Lance cannot reach and the Sonar Pulse is free.
     */
    @Test
    void theWholeLevelIsUnderWater() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(20.0, 60.0, 100.0, 130.0, 155.0, 188.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(130.0, 120.0, 120.0, 120.0, 120.0, 110.0),
                sections.stream().map(Section::speed).toList());
        assertEquals(2, sections.get(1).rampSeconds());
        assertEquals(2, sections.get(5).rampSeconds());
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
     * The dark is a look only (gap check F1, F3, F4; design/world/europa, Darkness): the light map's ambient 6 % (the
     * first section from 20 %, ramping down over t=8–18), the twin headlights' hard 70° cone 260 px long at full
     * strength, the 15 % rim, Rook's (and the Bulwark's) 56 px lamp, the static pools of the six lamp posts, the trench's
     * two last beacons, the farm barge's warm lamp and the survey buoy; the simulation has no darkness (Level 06's
     * hazard is not used).
     */
    @Test
    void theDarkIsTheLooksKeysOnly() {
        LevelData data = content.level(LEVEL);
        SectionsBlock.UnderwaterLook first = data.underwaterLookOf(0).orElseThrow();
        assertEquals(0.20, first.ambient());
        assertEquals(new SectionsBlock.AmbientRamp(0.06, 8, 18), first.ramp().orElseThrow());
        for (int i = 0; i < data.sections().size(); i++) {
            SectionsBlock.UnderwaterLook look = data.underwaterLookOf(i).orElseThrow();
            if (i > 0) {
                assertEquals(0.06, look.ambient(), "section " + (i + 1));
            }
            assertEquals(new SectionsBlock.Headlights(1.0, 260, 70), look.headlights(), "section " + (i + 1));
            assertEquals(0.15, look.rimLight(), "section " + (i + 1));
            assertEquals(56, look.escortLamp().orElseThrow(), "section " + (i + 1));
        }
        List<SectionsBlock.Light> lights = first.lightList();
        assertEquals(10, lights.size());
        assertEquals(
                6,
                lights.stream()
                        .filter(light -> light.colour().equals("ffc46e") && light.radius() == 90)
                        .count());
        assertEquals(
                2,
                lights.stream().filter(light -> light.colour().equals("ff6040")).count());
        assertTrue(lights.contains(new SectionsBlock.Light(10314, 64, 80, "ffc46e")), "the farm barge's lamp");
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(sortie(content, 1, difficulty, true).darkness().isEmpty(), difficulty.name());
        }
    }

    /**
     * The fault scarp (Level 25's terrain rules with one wall): the right wall only, rising at the field's edge from t=56,
     * 400 px of flyable width by t=61, 280 at its narrowest (t=92), gone by t=103; three cave mouths on it, each 90 px
     * along and 70 deep; a touch costs 10 (easy 6) and pushes 28 px over 0.12 s, then 0.5 s of cooldown. Its seven
     * Sirens stand one at a time on its foot, 5.6 s apart, on every difficulty.
     */
    @Test
    void theScarpItsNotchesAndItsNests() {
        double[] damage = {6, 10, 10};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            GroundSpec.TerrainSpec spec = level.ground().terrain().orElseThrow();
            assertEquals(damage[difficulty.ordinal()], spec.damage(), difficulty.name());
            assertEquals(28, spec.push());
            assertEquals(0.12, spec.pushSeconds());
            assertEquals(0.5, spec.cooldownSeconds());
            List<WaveSpec> nests = level.waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("siren"))
                    .toList();
            assertEquals(NESTS.length, nests.size(), difficulty.name());
            for (int i = 0; i < NESTS.length; i++) {
                WaveSpec nest = nests.get(i);
                assertEquals(NESTS[i], nest.t(), difficulty.name());
                assertEquals(1, nest.count(), difficulty + " t=" + nest.t());
                assertEquals("RIGHT", nest.edge().name(), "t=" + nest.t());
                assertTrue(nest.mount().orElseThrow().wall(), "t=" + nest.t());
            }
        }
        Terrain rock = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM)
                .ground()
                .terrain()
                .orElseThrow()
                .terrain();
        assertEquals(400, rock.rightAt(7_530), 1e-9, "t=61");
        assertEquals(280, rock.rightAt(11_250), 1e-9, "t=92");
        double narrowest = Double.POSITIVE_INFINITY;
        for (double row = 0; row <= 24_000; row += 10) {
            assertTrue(Double.isNaN(rock.leftAt(row)), "no left wall at " + row);
            double right = rock.rightAt(row);
            if (!Double.isNaN(right)) {
                narrowest = Math.min(narrowest, right);
            }
        }
        assertEquals(280, narrowest, 1e-9);
        List<Terrain.Notch> notches = rock.notches();
        assertEquals(
                List.of(7_500.0, 8_700.0, 10_380.0),
                notches.stream().map(Terrain.Notch::scroll).toList());
        for (Terrain.Notch notch : notches) {
            assertEquals(Terrain.Side.RIGHT, notch.side());
            assertEquals(90, notch.width());
            assertEquals(70, notch.depth());
        }
    }

    /**
     * No wave enters from a side or the rear, so no edge warning is due (design/ui/hud, edge warnings): the scarp's
     * nests stand on its foot and scroll in with it from the top (Level 25's nests; the README's cave mouths here hold
     * no cave waves), on every difficulty.
     */
    @Test
    void everyWaveEntersFromTheFront() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                assertEquals(WaveSpec.Entry.FRONT, wave.entry(), difficulty + " t=" + wave.t());
                assertTrue(wave.mount().isEmpty() || wave.mount().get().wall(), difficulty + " t=" + wave.t());
            }
        }
    }

    /**
     * The quiet windows: no other wave enters for 3.5 s after the Anglers' first sight (t=24), before the constellation
     * (t=104.5–108) or in the loss's silence (t=152–166, round 55's retime); after t=152 only the drifting jellies of
     * t=166 and t=172, on every difficulty.
     */
    @Test
    void theQuietWindows() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                double t = wave.t();
                assertFalse(t > 24 && t < 27.5, difficulty + ": a wave at t=" + t);
                assertFalse(t > 104.5 && t < 108, difficulty + ": a wave at t=" + t);
                assertFalse(t > 152 && t < 166, difficulty + ": a wave at t=" + t);
                if (t > 152) {
                    assertEquals("driftjelly", wave.enemy().slug(), difficulty + " t=" + t);
                    assertTrue(t == 166 || t == 172, difficulty + " t=" + t);
                }
            }
        }
    }

    /**
     * The Glow Angler (design/enemies/naval/glow-angler): 28 HP in the beam era at medium, {@code air}, a 30×24 hit box;
     * it lunges at a ship within 130 px (easy 100, hard 160) at 220 px/s for at most 0.6 s, stopping 60 px short, rings
     * 8 small bullets (easy 6, hard 10) at 100 px/s base (the bullet-speed lever), backs off at 100 px/s for 1.5 s and re-arms
     * 5 s after its ring (by the fire-rate lever).
     * In a flight it lunges and rings (invulnerable, every difficulty), never ringing more often than it lunged.
     */
    @Test
    void theGlowAnglerLungesFromItsLure() {
        double[] hp = {21, 28, 36.4};
        double[] trigger = {100, 130, 160};
        int[] ring = {6, 8, 10};
        double[] bulletSpeed = {0.8, 1, 1.15};
        double[] fireRate = {0.7, 1, 1.3};
        for (Difficulty difficulty : Difficulty.values()) {
            int d = difficulty.ordinal();
            EnemySpec angler = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .map(WaveSpec::enemy)
                    .filter(enemy -> enemy.slug().equals("glow-angler"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Layer.AIR, angler.layer());
            assertEquals(hp[d], angler.hp(), 0.5, difficulty.name());
            assertEquals(30, angler.hitbox().width());
            assertEquals(24, angler.hitbox().height());
            EnemySpec.Lunge lunge = angler.lunge().orElseThrow();
            assertEquals(trigger[d], lunge.trigger(), difficulty.name());
            assertEquals(220, lunge.speed());
            assertEquals(0.6, lunge.seconds());
            assertEquals(60, lunge.stopShort());
            assertEquals(ring[d], lunge.ring(), difficulty.name());
            assertEquals(100 * bulletSpeed[d], lunge.ringSpeed(), 1e-9, difficulty.name());
            assertEquals(4, lunge.ringDamage());
            assertEquals(100, lunge.retreatSpeed());
            assertEquals(1.5, lunge.retreatSeconds());
            assertEquals(5 / fireRate[d], lunge.rearmSeconds(), 1e-9, difficulty.name());

            Sortie sortie = sortie(content, 1, difficulty, true);
            int lunges = 0;
            int rings = 0;
            boolean seen = false;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                SimEvents events = sortie.events();
                lunges += events.count(SimEvents.Type.LURE_LUNGE);
                rings += events.count(SimEvents.Type.LURE_RING);
                for (int i = 0; i < sortie.enemyCount() && !seen; i++) {
                    seen = sortie.enemy(i).lurePhase() == LurePhase.LUNGE;
                }
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertTrue(seen, difficulty + ": an Angler seen lunging");
            assertTrue(lunges > 0 && rings > 0, difficulty + ": lunges " + lunges + ", rings " + rings);
            assertTrue(rings <= lunges, difficulty + ": lunges " + lunges + ", rings " + rings);
        }
    }

    /**
     * Rook's loss (design/player/wingmen, Rook's loss; gap check F6, F7; round 55's retime): its data (the trails from
     * t=162.5, the hits at 163.5 cutting his line of t=161.2, 2.5 s spinning down); in the level (invulnerable, medium)
     * his trails start at 162.5 and his barks end with them, he is lost at 163.5 (no pod), on the screen through his
     * spin and below the bottom edge by 166; a retry after the loss replays it at the same level time.
     */
    @Test
    void rooksLossAndItsReplayOnARetry() {
        assertEquals(
                new RookLossSpec(163.5, 162.5, 2.5),
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).rookLoss().orElseThrow());
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, true);
        Wingman rook = sortie.wingman().orElseThrow();
        Loss first = flyThroughTheLoss(sortie);
        assertEquals(162.5, first.trails, 0.05);
        assertEquals(163.5, first.lost, 0.05);
        assertTrue(first.offRadioAfterTrails, "his barks end with the trails");
        assertEquals(0, first.ejected, "not an eject");
        assertTrue(first.onScreen, "his centre on the screen through most of his spin");
        assertTrue(rook.loss().gone(), "below the bottom edge by t=166");

        sortie.retry(
                sortie.ship().defences().maxArmour(), content.wingmen().rook().armour());
        sortie.step(Autopilot.commands(sortie));
        assertEquals(2, sortie.attempt());
        assertFalse(rook.loss().lost(), "he flies again on the retry");
        Loss second = flyThroughTheLoss(sortie);
        assertEquals(first.trails, second.trails, 1e-9, "the trails again");
        assertEquals(first.lost, second.lost, 1e-9, "the loss again, at the same level time");
        assertTrue(rook.loss().gone());
    }

    /**
     * The trails' and the loss's level times of one flight through the loss (to t=166.5), and whether his centre stayed
     * above the bottom edge for the first 2.2 s of his spin.
     */
    private record Loss(double trails, double lost, boolean offRadioAfterTrails, int ejected, boolean onScreen) {}

    private static Loss flyThroughTheLoss(Sortie sortie) {
        double trails = Double.NaN;
        double lost = Double.NaN;
        boolean offRadio = true;
        int ejected = 0;
        boolean onScreen = true;
        Wingman rook = sortie.wingman().orElseThrow();
        int steps = 0;
        while (sortie.levelSeconds() < 166.5 && steps++ < MAX_STEPS) {
            sortie.step(Autopilot.commands(sortie));
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                switch (events.type(i)) {
                    case ROOK_TRAILS -> trails = sortie.levelSeconds();
                    case ROOK_LOST -> lost = sortie.levelSeconds();
                    case WINGMAN_EJECTED -> ejected++;
                    default -> {}
                }
            }
            if (!Double.isNaN(trails) && rook.onRadio()) {
                offRadio = false;
            }
            if (rook.loss().lost() && rook.loss().spinProgress() < 2.2 / 2.5 && !(rook.y() > 0)) {
                onScreen = false;
            }
        }
        return new Loss(trails, lost, offRadio, ejected, onScreen);
    }

    /**
     * Without Rook ({@code --escort none}) and with the Bulwark in the slot the loss is the radio's only (gap check F7):
     * no trails, no loss, the Bulwark untouched to the end; Rook's three scripted lines play in every case (none needs
     * an escort), the cut line at t=161.2 included.
     */
    @Test
    void withTheBulwarkOrNoEscortTheLossIsTheRadiosOnly() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Integer> rookCues = new java.util.ArrayList<>();
        for (int i = 0; i < level.radio().size(); i++) {
            RadioCue cue = level.radio().get(i);
            if (cue.speaker().equals("Rook")) {
                assertEquals(0, cue.requires() & RadioCue.FITTED_ESCORT, cue.line());
                rookCues.add(i);
            }
        }
        assertEquals(
                List.of(13.0, 148.0, 161.2),
                rookCues.stream().map(i -> level.radio().get(i).t()).toList());
        Loadout none = withoutRook(content, Difficulty.MEDIUM);
        Loadout bulwark = none.withBulwark(SimSpecs.bulwark(content, WingmanSpec.Side.LEFT, 120));
        for (Loadout loadout : List.of(none, bulwark)) {
            Sortie sortie = sortie(content, loadout, 1, Difficulty.MEDIUM, true);
            List<Integer> played = new java.util.ArrayList<>();
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                SimEvents events = sortie.events();
                assertEquals(0, events.count(SimEvents.Type.ROOK_TRAILS));
                assertEquals(0, events.count(SimEvents.Type.ROOK_LOST));
                for (int i = 0; i < events.size(); i++) {
                    if (events.type(i) == SimEvents.Type.RADIO && rookCues.contains(events.value(i))) {
                        played.add(events.value(i));
                    }
                }
            }
            assertTrue(sortie.complete());
            assertEquals(rookCues, played, "Rook's lines");
            assertTrue(sortie.wingman().isEmpty());
            sortie.bulwark().ifPresent(drone -> assertFalse(drone.destroyed(), "the Bulwark flies on"));
        }
    }

    /**
     * The Choir's fragment (round 48 Q8 a; gap check §1.5): at t=99, distorted, the HUD portrait's static 2 s before it
     * and the music 6 dB under it; Varga's "your kin" after it.
     */
    @Test
    void theChoirsLineAndItsKeys() {
        RadioBlock.RadioCue choir = content.level(LEVEL).radio().stream()
                .filter(cue -> cue.speaker().equals("The Choir"))
                .findFirst()
                .orElseThrow();
        assertEquals(99, choir.t().orElseThrow());
        assertEquals(true, choir.distorted().orElseThrow());
        assertEquals(2, choir.staticLeadSeconds());
        assertEquals(6, choir.musicDuckDb());
        RadioCue cue = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio().stream()
                .filter(radio -> radio.speaker().equals("The Choir"))
                .findFirst()
                .orElseThrow();
        assertTrue(cue.distorted());
        assertEquals(99, cue.t());
    }

    /**
     * The carriers: the overdrive is the first eel's of the t=96 pair, the shield cell the constellation's (t=108)
     * Angler nearest its field's centre (gap check F10), on every difficulty.
     */
    @Test
    void theFirstEelAndTheCentreAnglerCarryThePickups() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                if (wave.t() == 96) {
                    assertEquals(
                            List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, 0)), wave.carried(), difficulty.name());
                } else if (wave.t() == 108) {
                    assertEquals(WaveSpec.Formation.FIELD, wave.formation());
                    assertEquals(
                            List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, WaveSpec.Carried.CENTRE)),
                            wave.carried(),
                            difficulty.name());
                } else {
                    assertEquals(List.of(), wave.carried(), difficulty + " t=" + wave.t());
                }
            }
        }
    }

    /**
     * The secondary: at least 65 % of the level's enemies destroyed, judged at the end, 100 credits (Act 1 terms); its
     * line Okafor's at the end. The radio: Varga's lure warning at the Anglers' first sight (t=24.5), her shout before
     * the trails (t=155), Okafor's last line at the level's end (t=187).
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
        assertEquals(24.5, line(radio, "That glow is not a pickup.").t());
        assertEquals("Varga", line(radio, "That glow is not a pickup.").speaker());
        assertEquals(155, line(radio, "Contacts below!").t());
        assertEquals(187, line(radio, "Finish the dive, Lancer.").t());
        assertEquals(161.2, line(radio, "Lancer, something's below").t(), "Rook's cut line before the hits");
        assertEquals(
                "Lancer, something's below u—",
                line(radio, "Lancer, something's below").line());
    }

    private static RadioCue line(List<RadioCue> radio, String start) {
        return radio.stream()
                .filter(cue -> cue.line().startsWith(start))
                .findFirst()
                .orElseThrow();
    }

    /**
     * The README's credit table ({@code bounty_scale} 0.43): a perfect run at medium in Act 1 terms, Driftjelly 118 × 10,
     * Glow Angler 78 × 14, Spiral Nautilus 12 × 30 and Siren 7 × 28, and 8 Eel Swarms paid as a segment chain (the code's
     * count, gap check F11: {@link TypicalHaul}), against budget(26) = 3,799.
     */
    @Test
    void aPerfectRunAtMediumEarnsTheReadmesTotal() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int kills = 0;
        int eels = 0;
        for (WaveSpec wave : level.waves()) {
            if (wave.enemy().slug().equals("eel-swarm")) {
                eels += wave.count();
                continue;
            }
            kills += wave.count() * wave.enemy().bounty();
            kills += wave.count()
                    * wave.enemy().parts().stream()
                            .mapToInt(EnemySpec.UnitPart::bounty)
                            .sum();
        }
        assertEquals(118 * 10 + 78 * 14 + 12 * 30 + 7 * 28, kills, "Driftjelly, Glow Angler, Spiral Nautilus, Siren");
        assertEquals(8, eels);
        assertEquals(0.43, content.level(LEVEL).bounties());
        assertEquals(3_799, Math.round(content.economy().budget().of(level.number())), "700 × 1.07²⁵");
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed ({@link DarkPlay}) and meets the
     * kill ratio on most seeds at easy and medium; hard's is printed (its fields are larger and the harpoon's one torpedo
     * a second kills one unit a second at most: step F3 measured about 60–68 %). Printed: the secondary's rate, the
     * credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 26 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries));
            if (difficulty != Difficulty.HARD) {
                assertTrue(met * 2 > SEEDS, difficulty + ": the kill ratio on most seeds: " + met);
            }
        }
        System.out.println(report);
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
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level26Test::flyToEnd);

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
