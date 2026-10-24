package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part D, the Abyss Ray (design/enemies/naval/abyss-ray; m8d-gaps.md §1.2, §5 D2/D3): it enters as a shadow on
 * {@code sub} in the ship's lane, 200 px ahead of the ship, for 1.5 s (hit only by {@code anti-sub} fire), rises over
 * 0.9 s (its layer flipping to {@code air} at the middle, touching nothing until the rise's end), glides 5 s at 70 px/s
 * toward the far side firing its fans (three 1.2 s apart from the glide's start; easy two 7-way, hard four 11-way), and
 * sinks over 0.9 s gliding on (back to {@code sub} at the middle), gone at its end. Its four Barbfins are held until the
 * rise's middle and peel off from it to their holds above the ship. Its first shadow starts the radio's {@code
 * first-ray}. Deterministic and allocation-free.
 */
class AbyssRayTest {
    private static final double INF = Double.POSITIVE_INFINITY;
    static final EnemySpec.Rise RISE = new EnemySpec.Rise(1.5, 0.9, 5, 70, 0.9, 200);

    /** The fan: {@code count}-way over 110°, 110 px/s, {@code small} 4, {@code burst} fans 1.2 s apart, once a visit. */
    static EnemyGun fan(int count, int burst) {
        return new EnemyGun(
                30,
                0,
                burst,
                110,
                4,
                false,
                count,
                Math.toRadians(110),
                INF,
                INF,
                Optional.empty(),
                Optional.empty(),
                1.2);
    }

    static final EnemyGun MEDIUM = fan(9, 3);

    /** The Ray: 210 HP, 130×90, entering on {@code sub}, {@code large} contact 20, 60 credits. */
    static EnemySpec ray(EnemySpec.Rise rise, Optional<EnemyGun> gun) {
        return new EnemySpec(
                        "abyss-ray",
                        210,
                        new Hitbox(130, 90),
                        Layer.SUB,
                        20,
                        false,
                        60,
                        70,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        gun,
                        Optional.empty(),
                        Optional.empty(),
                        false)
                .withRise(Optional.of(rise));
    }

    static final EnemySpec RAY = ray(RISE, Optional.of(MEDIUM));

    static WaveSpec carrier(double t, EnemySpec enemy, int count) {
        return TestSpecs.wave(
                t, WaveSpec.Formation.CARRIER_ESCORTS, enemy, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    static Sortie sortie(List<WaveSpec> waves, boolean invulnerable, Armament.Mount... mounts) {
        LevelScript level = TestSpecs.level(40, waves);
        return sortie(level, invulnerable, mounts);
    }

    static Sortie sortie(LevelScript level, boolean invulnerable, Armament.Mount... mounts) {
        Loadout loadout = mounts.length == 0 ? TestSpecs.LOADOUT : TestSpecs.loadout(mounts);
        return new Sortie(
                1,
                loadout,
                level,
                invulnerable ? TestSpecs.RULES.withInvulnerableShip() : TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    static Enemy find(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    static List<Enemy> all(Sortie sortie, String slug) {
        List<Enemy> found = new ArrayList<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                found.add(sortie.enemy(i));
            }
        }
        return found;
    }

    /** Steps until a Ray's shadow takes its spot (its {@link SimEvents.Type#RISE_SHADOW}); returns the Ray. */
    static Enemy toShadow(Sortie sortie, int commands) {
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(commands);
            if (sortie.events().count(SimEvents.Type.RISE_SHADOW) > 0) {
                return find(sortie, "abyss-ray");
            }
        }
        throw new AssertionError("no shadow");
    }

    @Test
    void itsShadowTakesItsSpotAheadOfTheShipThenItRisesGlidesToTheFarSideAndSinksAway() {
        Sortie sortie = sortie(List.of(carrier(1.5, ray(RISE, Optional.empty()), 1)), true);
        // The ship moves left first, then holds still.
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.LEFT.bit());
        }
        Enemy ray = toShadow(sortie, Command.NONE);
        double shipX = sortie.ship().x();
        assertTrue(shipX < PlayField.WIDTH / 2.0 - 40, "the ship is left of the centre: " + shipX);
        assertEquals(Math.max(65, shipX), ray.x(), 1e-9, "in the ship's lane");
        assertEquals(sortie.ship().y() + 200, ray.y(), 1e-9, "200 px ahead of the ship");
        assertEquals(RisePhase.SHADOW, ray.risePhase());
        assertEquals(Layer.SUB, ray.layer());
        assertEquals(1, ray.riseDirection(), "it glides toward the far side: the right");
        double spotX = ray.x();
        double spotY = ray.y();

        int shadow = 1;
        while (ray.risePhase() == RisePhase.SHADOW) {
            sortie.step(Command.NONE);
            shadow++;
            assertEquals(spotX, ray.x(), 1e-9, "the shadow stays at its spot");
        }
        assertEquals(SimStep.ticks(1.5), shadow, "1.5 s a shadow");
        assertEquals(1, sortie.events().count(SimEvents.Type.RISE), "the rise starts");
        int rise = 0;
        int flip = -1;
        while (ray.risePhase() == RisePhase.RISE) {
            assertEquals(flip < 0 ? Layer.SUB : Layer.AIR, ray.layer(), "at " + rise);
            sortie.step(Command.NONE);
            rise++;
            if (flip < 0 && ray.layer() == Layer.AIR) {
                flip = rise;
            }
            assertEquals(spotY, ray.y(), 1e-9, "it rises at its spot");
        }
        assertEquals(SimStep.ticks(0.9), rise, "0.9 s rising");
        assertEquals(SimStep.ticks(0.9) / 2, flip, "on air from the rise's middle");
        assertEquals(RisePhase.GLIDE, ray.risePhase());

        int glide = 0;
        double before = ray.x();
        while (ray.risePhase() == RisePhase.GLIDE) {
            sortie.step(Command.NONE);
            glide++;
            assertEquals(70 * SimStep.SECONDS, ray.x() - before, 1e-9, "70 px/s to the right");
            assertEquals(spotY, ray.y(), 1e-9);
            assertEquals(Layer.AIR, ray.layer());
            before = ray.x();
        }
        assertEquals(SimStep.ticks(5), glide, "a 5 s glide");
        int sink = 0;
        int under = -1;
        while (find(sortie, "abyss-ray") != null) {
            assertEquals(RisePhase.SINK, ray.risePhase());
            sortie.step(Command.NONE);
            sink++;
            if (under < 0 && ray.layer() == Layer.SUB) {
                under = sink;
            }
        }
        assertEquals(SimStep.ticks(0.9) / 2, under, "back on sub at the sink's middle");
        assertEquals(SimStep.ticks(0.9), sink, "gone at the sink's end");
    }

    @Test
    void theSpotKeepsItsHitBoxOnTheFieldAndItGlidesAwayFromTheNearerEdge() {
        Sortie sortie = sortie(List.of(carrier(1, ray(RISE, Optional.empty()), 1)), true);
        Enemy ray = toShadow(sortie, Command.RIGHT.bit());
        assertTrue(
                sortie.ship().x() > PlayField.WIDTH - 65,
                "the ship far right: " + sortie.ship().x());
        assertEquals(PlayField.WIDTH - 65, ray.x(), 1e-9, "clamped");
        assertEquals(-1, ray.riseDirection(), "it glides to the left");
        // Off the field before the glide's end: gone (an escape).
        Sortie early = sortie(
                List.of(carrier(1, ray(new EnemySpec.Rise(0.2, 0.2, 20, 300, 0.9, 200), Optional.empty()), 1)), true);
        Enemy fast = toShadow(early, Command.NONE);
        assertNotNull(fast);
        int steps = 0;
        while (find(early, "abyss-ray") != null && steps < SimStep.ticks(10)) {
            early.step(Command.NONE);
            steps++;
        }
        assertNull(find(early, "abyss-ray"), "gone once off the play field");
        assertTrue(steps < SimStep.ticks(2), "well before its glide's end: " + steps);
    }

    @Test
    void itTouchesNothingBeforeItsGlideButItsBodyHitsInTheGlide() {
        // A spot on the ship (0 px ahead) and no gun: only its body can hit.
        Sortie sortie = sortie(
                List.of(carrier(1, ray(new EnemySpec.Rise(1.5, 0.9, 5, 70, 0.9, 0), Optional.empty()), 1)), false);
        Enemy ray = toShadow(sortie, Command.NONE);
        double shield = sortie.ship().defences().shield();
        int flipped = 0;
        while (true) {
            sortie.step(Command.NONE);
            if (ray.risePhase() == RisePhase.GLIDE) {
                break;
            }
            flipped += ray.layer() == Layer.AIR ? 1 : 0;
            assertEquals(0, sortie.events().count(SimEvents.Type.SHIELD_HIT), ray.risePhase() + " " + ray.layer());
            assertEquals(shield, sortie.ship().defences().shield(), 1e-9, "untouched through the shadow and the rise");
        }
        assertTrue(flipped > 0, "on air on the ship in the rise's second half");
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.SHIELD_HIT);
        }
        assertTrue(hits > 0, "gliding, its body rams the ship");
    }

    /** {@code level} under water. */
    private static LevelScript under(List<WaveSpec> waves) {
        return UnderwaterTest.under(TestSpecs.level(40, waves));
    }

    @Test
    void itsShadowIsHitByTheHarpoonAndNotByThePulseUntilItsLayerFlips() {
        EnemySpec slow = ray(new EnemySpec.Rise(3, 0.9, 5, 70, 0.9, 200), Optional.empty());
        // The Pulse (no anti-sub): nothing while a shadow and rising on sub, hits from the flip on.
        Sortie pulse = sortie(under(List.of(carrier(1, slow, 1))), true, UnderwaterTest.front(UnderwaterTest.BOLT));
        Enemy ray = toShadow(pulse, Command.FIRE.bit());
        while (ray.layer() == Layer.SUB) {
            pulse.step(Command.FIRE.bit());
            if (ray.layer() == Layer.SUB) {
                assertEquals(210, ray.hp(), 1e-9, "the Pulse passes over the shadow: " + ray.risePhase());
            }
        }
        for (int i = 0; i < SimStep.ticks(1) && find(pulse, "abyss-ray") != null; i++) {
            pulse.step(Command.FIRE.bit());
        }
        assertTrue(ray.hp() < 210, "on air the Pulse hits it");

        // The harpoon (anti-sub): full damage on the shadow.
        Sortie harpoon =
                sortie(under(List.of(carrier(1, slow, 1))), true, UnderwaterTest.front(HarpoonTorpedoTest.HARPOON));
        Enemy shadow = toShadow(harpoon, Command.NONE);
        double hp = shadow.hp();
        while (shadow.risePhase() == RisePhase.SHADOW) {
            harpoon.step(Command.FIRE.bit());
        }
        assertTrue(shadow.hp() < hp, "the harpoon strikes the shadow");
        assertEquals(0, (210 - shadow.hp()) % 37.5, 1e-9, "full damage a strike, its splash on nothing else");
    }

    @Test
    void itsShadowIsHitByADepthCharge() {
        // The shadow on its spot ahead of the ship for 6 s; the ship backs down to the bottom edge and (M8 AW) tosses
        // its charges forward onto it.
        EnemySpec still = ray(new EnemySpec.Rise(6, 0.9, 5, 70, 0.9, 0), Optional.empty());
        Sortie sortie =
                sortie(under(List.of(carrier(1, still, 1))), true, DepthChargeTest.rear(DepthChargeTest.CHARGES));
        Enemy shadow = toShadow(sortie, Command.NONE);
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.DOWN.bit());
        }
        int blasts = 0;
        while (shadow.risePhase() == RisePhase.SHADOW && blasts < 2) {
            sortie.step(Command.FIRE.bit());
            blasts += sortie.events().count(SimEvents.Type.BLAST);
        }
        assertTrue(blasts > 0, "the charges burst");
        assertEquals(RisePhase.SHADOW, shadow.risePhase());
        assertTrue(shadow.hp() < 210, "a charge's burst reaches the shadow");
    }

    /** The steps (from the glide's start) of the fans and their bullets. */
    private static List<int[]> fans(EnemyGun gun) {
        Sortie sortie = sortie(List.of(carrier(1, ray(RISE, Optional.of(gun)), 1)), true);
        // The ship left of the centre: the whole glide's fans are on the field.
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.LEFT.bit());
        }
        Enemy ray = toShadow(sortie, Command.NONE);
        int glideStart = -1;
        List<int[]> fans = new ArrayList<>();
        for (int step = 0; find(sortie, "abyss-ray") != null; step++) {
            int bullets = sortie.bulletCount();
            sortie.step(Command.NONE);
            if (glideStart < 0 && ray.risePhase() == RisePhase.GLIDE) {
                glideStart = step;
            }
            int fired = sortie.bulletCount() - bullets;
            if (fired > 0) {
                assertTrue(glideStart >= 0, "no fan before the glide");
                fans.add(new int[] {step - glideStart, fired});
            }
        }
        return fans;
    }

    @Test
    void itFiresThreeFansOnePointTwoSecondsApartFromItsGlidesStartEasyTwoHardFour() {
        int gap = SimStep.ticks(1.2);
        List<int[]> medium = fans(MEDIUM);
        assertEquals(3, medium.size(), "one salvo a visit");
        for (int k = 0; k < medium.size(); k++) {
            assertEquals(k * gap, medium.get(k)[0], "fan " + k);
            assertEquals(9, medium.get(k)[1], "9-way");
        }
        List<int[]> easy = fans(fan(7, 2));
        assertEquals(2, easy.size());
        assertEquals(7, easy.get(0)[1]);
        List<int[]> hard = fans(fan(11, 4));
        assertEquals(
                4,
                hard.size(),
                hard.stream().map(java.util.Arrays::toString).toList().toString());
        assertEquals(3 * gap, hard.get(3)[0]);
        assertEquals(11, hard.get(3)[1]);
    }

    // ---- the escorts ----

    /** The Barbfin with its hover above the ship: 9 s, 90–200 px above, drifting 6 px/s; circle 60 px. */
    static final EnemySpec BARBFIN = BarbfinTest.barbfin(EnemyGun.aimed(2.4, 0.9, 1, 150, 4, false));

    @Test
    void itsEscortsAreHeldUntilTheRisesMiddleThenPeelOffToTheirHoldsAboveTheShip() {
        Sortie sortie = sortie(List.of(carrier(1, RAY, 1), carrier(1, BARBFIN, 4)), true);
        Enemy ray = toShadow(sortie, Command.NONE);
        int enemyTotal = sortie.enemyTotal();
        assertEquals(5, enemyTotal, "the held escorts count among the level's enemies");
        while (ray.layer() == Layer.SUB) {
            assertEquals(0, all(sortie, "barbfin").size(), "held while the Ray is under: " + ray.risePhase());
            sortie.step(Command.NONE);
        }
        List<Enemy> escorts = all(sortie, "barbfin");
        assertEquals(4, escorts.size(), "they peel off at the flip");
        for (Enemy escort : escorts) {
            assertEquals(ray.x(), escort.x(), 1.5 * 120 * SimStep.SECONDS + 1e-9, "from the Ray's centre");
        }
        double shipY = sortie.ship().y();
        // They reach their holds, spread across, and hold there 9 s.
        for (int i = 0; i < SimStep.ticks(2.5); i++) {
            sortie.step(Command.NONE);
        }
        List<Double> xs = new ArrayList<>();
        for (Enemy escort : escorts) {
            double above = escort.y() - shipY;
            assertTrue(above >= 90 - 1e-9 && above <= 200 + 6 * 2.5 + 1e-9, "held above the ship: " + above);
            xs.add(escort.x());
        }
        xs.sort(Double::compare);
        assertTrue(xs.get(3) - xs.get(0) > 80, "spread across: " + xs);
        // They keep their cadence after the Ray left.
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            shots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertNull(find(sortie, "abyss-ray"), "the Ray sank away");
        assertEquals(4, all(sortie, "barbfin").size(), "the escorts stay");
        assertTrue(shots >= 4, "and fire on: " + shots);
    }

    @Test
    void aRayKilledAsAShadowLetsItsEscortsPeelOffAtOnce() {
        Sortie sortie = sortie(List.of(carrier(1, RAY, 1), carrier(1, BARBFIN, 4)), true);
        Enemy ray = toShadow(sortie, Command.NONE);
        sortie.step(Command.NONE);
        assertEquals(0, all(sortie, "barbfin").size());
        double x = ray.x();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == ray) {
                sortie.destroyEnemy(i);
                break;
            }
        }
        List<Enemy> escorts = all(sortie, "barbfin");
        assertEquals(4, escorts.size(), "released where it died");
        for (Enemy escort : escorts) {
            assertEquals(x, escort.x(), 1e-9);
        }
    }

    // ---- the radio ----

    private static final RadioCue FIRST_RAY =
            new RadioCue(CueTrigger.FIRST_RAY, 0, "", "Varga", "Big contact rising.", false, "neutral", "Varga");

    @Test
    void theFirstShadowStartsTheFirstRayLineOnce() {
        LevelScript level =
                TestSpecs.level(40, List.of(carrier(1, RAY, 1), carrier(12, RAY, 1)), List.of(), List.of(FIRST_RAY));
        Sortie sortie = sortie(level, true);
        int lines = 0;
        int firstLine = -1;
        int firstShadow = -1;
        for (int tick = 0; tick < SimStep.ticks(20); tick++) {
            sortie.step(Command.NONE);
            if (firstShadow < 0 && sortie.events().count(SimEvents.Type.RISE_SHADOW) > 0) {
                firstShadow = tick;
            }
            if (sortie.events().count(SimEvents.Type.RADIO) > 0) {
                lines++;
                firstLine = firstLine < 0 ? tick : firstLine;
            }
        }
        assertEquals(1, lines, "once");
        assertEquals(firstShadow, firstLine, "with the first shadow");
    }

    // ---- the stat block's checks, determinism, allocation ----

    @Test
    void aRiseIsAPlainSubUnitsAndTakesTime() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Rise(1.5, 0, 5, 70, 0.9, 200));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Rise(1.5, 0.9, 5, 70, 0.9, -1));
        assertThrows(
                IllegalArgumentException.class,
                () -> TestSpecs.NEEDLER.withRise(Optional.of(RISE)),
                "a rise enters on sub");
    }

    private static Sortie mixed() {
        LevelScript level = UnderwaterTest.under(TestSpecs.level(
                30, List.of(carrier(1, RAY, 1), carrier(1, BARBFIN, 4), carrier(11, RAY, 1), carrier(11, BARBFIN, 4))));
        return new Sortie(
                1,
                TestSpecs.loadout(
                        UnderwaterTest.front(HarpoonTorpedoTest.HARPOON),
                        DepthChargeTest.rear(DepthChargeTest.CHARGES)),
                level,
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    private static int command(int step) {
        return Command.FIRE.bit() | (step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
    }

    @Test
    void theSameSeedFliesTheSameRays() {
        Sortie a = mixed();
        Sortie b = mixed();
        for (int step = 0; step < SimStep.ticks(20); step++) {
            a.step(command(step));
            b.step(command(step));
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
    }

    @Test
    void raysTheirEscortsAndTheirFansDoNotAllocate() {
        Sortie seen = mixed();
        int shadows = 0;
        int fired = 0;
        int peeled = 0;
        for (int step = 0; step < SimStep.ticks(20); step++) {
            seen.step(command(step));
            shadows += seen.events().count(SimEvents.Type.RISE_SHADOW);
            fired += seen.events().count(SimEvents.Type.ENEMY_FIRED);
            peeled = Math.max(peeled, all(seen, "barbfin").size());
        }
        assertEquals(2, shadows);
        assertTrue(fired > 0 && peeled > 0, fired + " shots, " + peeled + " escorts");
        assertFalse(seen.events().size() < 0);
        java.util.function.Consumer<Sortie> run = sortie -> {
            for (int step = 0; step < SimStep.ticks(20); step++) {
                sortie.step(command(step));
            }
        };
        assertEquals(0, Allocations.least(AbyssRayTest::mixed, run));
    }
}
