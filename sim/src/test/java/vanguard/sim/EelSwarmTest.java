package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part C, the Eel Swarm (design/enemies/naval/eel-swarm): an air chain in the water. Its head point sways about its
 * route ({@code snake}: the head's sine, 36 px at 0.8 Hz); a cut never regrows: the rear part scatters along the
 * cut's heading at 70 px/s for 3 s, hittable and firing nothing, then dies from the cut backwards; the head fires two
 * attacks on their own clocks, each after the stripe's tell; a head-first kill pays 48, the full cut-up 56; a
 * head-first pickup drops only with a head-first kill; 16 segments fit; nothing allocates.
 */
class EelSwarmTest {
    private static final int FIRE = Command.FIRE.bit();
    /** The head's aimed pulse: every 1.8 s, `small` (4) at 130 px/s, after a 0.3 s stripe. */
    static final EnemyGun AIMED = EnemyGun.aimed(1.8, 1.0, 1, 130, 4, false).withTell(0.3);
    /** Its 3-way fan: 40 degrees at 120 px/s every 5 s, after the same stripe. */
    static final EnemyGun FAN = new EnemyGun(
                    5, 2.0, 1, 120, 4, false, 3, Math.toRadians(40), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            .withTell(0.3);

    static final EnemySpec.Scatter SCATTER = new EnemySpec.Scatter(70, 3);
    /** The snake sway: 36 px, 0.8 Hz. */
    static final EnemySpec.Sine SWAY = new EnemySpec.Sine(36, 1 / 0.8);

    private static EnemySpec part(String slug, double hp, Hitbox box, int bounty) {
        return EnemySpec.of(slug, hp, box, Layer.AIR, 6, false, bounty, 0);
    }

    /** The member offsets: head 40, segments 24, tail 24 px long, 0.8 × the mean apart. */
    static List<Double> offsets(int segments) {
        List<Double> lengths = new ArrayList<>(List.of(40.0));
        for (int i = 0; i < segments; i++) {
            lengths.add(24.0);
        }
        lengths.add(24.0);
        List<Double> offsets = new ArrayList<>(List.of(0.0));
        for (int i = 1; i < lengths.size(); i++) {
            offsets.add(offsets.get(i - 1) + 0.8 * (lengths.get(i - 1) + lengths.get(i)) / 2);
        }
        return offsets;
    }

    /**
     * An Eel Swarm of {@code segments} at 110 px/s: head 100 HP (30 credits, 18 more head first), segments 10 HP (2),
     * tail 25 HP (2, 4 more first), pops 0.07 s apart, its cut parts scattering, with {@code attacks} and {@code sway}.
     */
    static EnemySpec eel(int segments, List<EnemyGun> attacks, Optional<EnemySpec.Sine> sway) {
        List<Hitbox> boxes = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            boxes.add(new Hitbox(28, 18));
        }
        EnemySpec.ChainSpec chain = new EnemySpec.ChainSpec(
                        part("eel-swarm-segment", 10, boxes.getFirst(), 2),
                        boxes,
                        part("eel-swarm-tail", 25, new Hitbox(24, 16), 2),
                        4,
                        null,
                        0,
                        0,
                        offsets(segments),
                        0.07,
                        1,
                        18)
                .withScatter(Optional.of(SCATTER))
                .withHeadAttacks(attacks);
        return EnemySpec.of("eel-swarm", 100, new Hitbox(36, 28), Layer.AIR, 6, false, 30, 110)
                .withSine(sway)
                .withChain(Optional.of(chain));
    }

    /** The x of the eel's route, clear of the ship's lane (its shots are not held back by the ship's nearness). */
    static final double LANE = 120;

    /** One eel at {@code t} straight down at {@link #LANE}, carrying {@code carried}. */
    static WaveSpec wave(double t, EnemySpec eel, List<WaveSpec.Carried> carried) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.SNAKE,
                eel,
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                carried,
                Optional.empty(),
                List.of(List.of(new WaveSpec.At(LANE, -60), new WaveSpec.At(LANE, 900))),
                Optional.empty());
    }

    static LevelScript level(List<WaveSpec> waves) {
        return LevelScript.builder(1, 3)
                .sections(List.of(new Section(60, 120)))
                .waves(waves)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    private static Sortie sortie(EnemySpec eel) {
        return sortie(eel, List.of());
    }

    private static Sortie sortie(EnemySpec eel, List<WaveSpec.Carried> carried) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                level(List.of(wave(0.5, eel, carried))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    private static int stepUntilChain(Sortie sortie) {
        int step = 0;
        while (sortie.chainCount() == 0) {
            sortie.step(Command.NONE);
            step++;
        }
        return step;
    }

    private static void steps(Sortie sortie, double seconds) {
        for (int step = 0; step < SimStep.ticks(seconds); step++) {
            sortie.step(Command.NONE);
        }
    }

    /** The pool index of the first chain's member {@code link} (in chain {@code chain}). */
    private static int member(Sortie sortie, Chain chain, int link) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).chain() == chain && sortie.enemy(i).link() == link) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void theHeadSwaysAboutItsRouteAndTheBodyFollowsItsTrail() {
        Sortie sortie = sortie(eel(12, List.of(), Optional.of(SWAY)));
        stepUntilChain(sortie);
        Chain chain = sortie.chain(0);
        double least = Double.POSITIVE_INFINITY;
        double most = Double.NEGATIVE_INFINITY;
        List<Double> headXs = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(5); step++) {
            sortie.step(Command.NONE);
            Enemy head = chain.member(0);
            headXs.add(head.x());
            least = Math.min(least, head.x());
            most = Math.max(most, head.x());
            for (int i = 0; i < chain.size(); i++) {
                assertTrue(Math.abs(chain.member(i).x() - LANE) <= 36 + 1e-6, "member " + i + " within the sway");
            }
        }
        assertEquals(LANE + 36, most, 0.5, "36 px to one side");
        assertEquals(LANE - 36, least, 0.5, "and to the other");
        int period = SimStep.ticks(1.25);
        for (int k = period; k < headXs.size(); k++) {
            assertEquals(headXs.get(k - period), headXs.get(k), 1e-6, "0.8 Hz: one swing per 1.25 s");
        }
        // The tail swings too, later: the body follows the head's trail.
        Sortie plain = sortie(eel(12, List.of(), Optional.empty()));
        stepUntilChain(plain);
        steps(plain, 3);
        for (int i = 0; i < plain.chain(0).size(); i++) {
            assertEquals(LANE, plain.chain(0).member(i).x(), 1e-9, "without a sway the chain keeps to its route");
        }
    }

    @Test
    void aCutPartScattersAlongItsHeadingFor3sHittableThenDiesFromTheCut() {
        Sortie sortie = sortie(eel(12, List.of(AIMED, FAN), Optional.empty()));
        stepUntilChain(sortie);
        steps(sortie, 3);
        Chain front = sortie.chain(0);
        int kind = sortie.enemy(member(sortie, front, 4)).kind();
        sortie.destroyEnemy(member(sortie, front, 4));
        assertEquals(1, sortie.events().count(SimEvents.Type.CHAIN_CUT), "the cut");
        assertEquals(2, sortie.chainCount(), "the rear part swims on as a chain of its own");
        Chain rear = sortie.chain(1);
        assertTrue(rear.scattering());
        assertFalse(front.scattering());
        assertEquals(4, front.size(), "the front keeps the head and three segments");
        assertEquals(10, rear.size(), "the rear: its empty head slot, eight segments and the tail");
        assertNull(rear.member(0));
        assertEquals(kind, rear.member(1).kind());
        int firstPop = -1;
        int pops = 0;
        for (int step = 1; step <= SimStep.ticks(4); step++) {
            double rearY = rear.member(9) == null ? Double.NaN : rear.member(9).y();
            double frontY = front.member(0).y();
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.CHAIN_REGROWN), "it never regrows");
            for (int k = 0; k < sortie.events().size(); k++) {
                if (sortie.events().type(k) == SimEvents.Type.ENEMY_FIRED) {
                    assertEquals(front.member(0).x(), sortie.events().x(k), 1e-9, "only the head fires");
                }
            }
            assertEquals(110 * SimStep.SECONDS, frontY - front.member(0).y(), 1e-6, "the front swims on");
            if (step < SimStep.ticks(3)) {
                assertEquals(LANE, rear.member(9).x(), 1e-9, "on the cut's heading (straight down)");
                if (step > 1) {
                    assertEquals(70 * SimStep.SECONDS, rearY - rear.member(9).y(), 1e-6, "at 70 px/s");
                }
                for (int i = 1; i < rear.size(); i++) {
                    assertFalse(Chain.doomed(rear.member(i)), "hittable while it scatters");
                }
            }
            int popped = sortie.events().count(SimEvents.Type.CHAIN_POP);
            if (popped > 0 && firstPop < 0) {
                firstPop = step;
                assertNull(rear.member(1), "the first behind the cut bursts first");
                assertTrue(rear.member(2) != null && Chain.doomed(rear.member(2)), "the rest waits for its burst");
            }
            pops += popped;
        }
        assertEquals(SimStep.ticks(3) + SimStep.ticks(0.07), firstPop, "3 s after the cut, then its pops");
        assertEquals(9, pops, "the rear part died: eight segments and the tail");
        for (int i = 0; i < 4; i++) {
            assertTrue(front.member(i) != null, "the front lives on");
        }
    }

    @Test
    void everyCutScattersAndACutOfAScatteringPartOnlyTakesThatMember() {
        Sortie sortie = sortie(eel(12, List.of(), Optional.empty()));
        stepUntilChain(sortie);
        steps(sortie, 3);
        Chain front = sortie.chain(0);
        sortie.destroyEnemy(member(sortie, front, 8));
        sortie.destroyEnemy(member(sortie, front, 3));
        assertEquals(3, sortie.chainCount(), "two cuts, two scattering parts");
        assertTrue(sortie.chain(1).scattering() && sortie.chain(2).scattering());
        Chain first = sortie.chain(1);
        sortie.destroyEnemy(member(sortie, first, 2));
        assertEquals(3, sortie.chainCount(), "no further split of a scattering part");
        assertNull(first.member(2));
        assertTrue(first.member(1) != null && first.member(3) != null, "the rest swims on");
    }

    @Test
    void aHeadFirstKillPays48AndTheFullCutUp56() {
        double factor = TestSpecs.SCORING.creditFactor();
        Sortie headFirst = sortie(eel(12, List.of(), Optional.empty()));
        stepUntilChain(headFirst);
        steps(headFirst, 2);
        int before = headFirst.credits();
        headFirst.destroyEnemy(member(headFirst, headFirst.chain(0), 0));
        steps(headFirst, 2);
        assertEquals(48 * factor, headFirst.credits() - before, 1, "head first: 30 + 18; the body's pops pay nothing");

        Sortie cutUp = sortie(eel(12, List.of(), Optional.empty()));
        stepUntilChain(cutUp);
        steps(cutUp, 2);
        before = cutUp.credits();
        Chain front = cutUp.chain(0);
        cutUp.destroyEnemy(member(cutUp, front, 1));
        Chain rear = cutUp.chain(1);
        for (int link = 1; link < rear.size(); link++) {
            cutUp.step(Command.NONE);
            cutUp.destroyEnemy(member(cutUp, rear, link));
        }
        cutUp.destroyEnemy(member(cutUp, front, 0));
        steps(cutUp, 4);
        assertEquals(56 * factor, cutUp.credits() - before, 1, "every part shot: 30 + 12 × 2 + 2");

        Sortie tailFirst = sortie(eel(12, List.of(), Optional.empty()));
        stepUntilChain(tailFirst);
        steps(tailFirst, 2);
        before = tailFirst.credits();
        tailFirst.destroyEnemy(member(tailFirst, tailFirst.chain(0), 13));
        assertEquals((2 + 4) * factor, tailFirst.credits() - before, 1, "the tail first: 6");
        assertEquals(1, tailFirst.chainCount(), "nothing behind it to scatter");
    }

    @Test
    void theHeadFiresItsTwoAttacksOnTheirOwnClocksEachAfterTheStripe() {
        Sortie sortie = sortie(eel(12, List.of(AIMED, FAN), Optional.empty()));
        stepUntilChain(sortie);
        Chain chain = sortie.chain(0);
        int step = 0;
        int on = -1;
        List<Integer> fired = new ArrayList<>();
        List<Integer> glints = new ArrayList<>();
        List<Integer> bullets = new ArrayList<>();
        while (chain.member(0) != null && step < SimStep.ticks(12)) {
            int before = sortie.bulletCount();
            sortie.step(Command.NONE);
            step++;
            Enemy head = chain.member(0);
            boolean onField = PlayField.overlaps(head.x(), head.y(), head.hitbox());
            if (!onField) {
                if (on >= 0) {
                    break;
                }
                continue;
            }
            if (on < 0) {
                on = step;
            }
            for (int k = 0; k < sortie.events().size(); k++) {
                if (sortie.events().type(k) == SimEvents.Type.ENEMY_FIRED) {
                    fired.add(step - on);
                    assertEquals(head.x(), sortie.events().x(k), 1e-9, "from the head");
                }
                if (sortie.events().type(k) == SimEvents.Type.EYE_GLINT) {
                    glints.add(step - on);
                    assertEquals(head.kind(), sortie.events().value(k), "the stripe's tell, its kind");
                }
            }
            bullets.add(sortie.bulletCount() - before);
        }
        List<Integer> expected = new ArrayList<>();
        int last = bullets.size() - 1;
        for (int t = SimStep.ticks(1.0); t <= last; t += SimStep.ticks(1.8)) {
            expected.add(t);
        }
        for (int t = SimStep.ticks(2.0); t <= last; t += SimStep.ticks(5)) {
            expected.add(t);
        }
        expected.sort(null);
        assertTrue(expected.size() >= 4, "a few volleys while it crossed: " + expected);
        assertEquals(expected, fired, "the aimed every 1.8 s from 1.0 s, the fan every 5 s from 2.0 s");
        List<Integer> tells = new ArrayList<>();
        for (int t : expected) {
            tells.add(t - SimStep.ticks(0.3));
        }
        assertEquals(tells, glints, "each volley 0.3 s after its stripe");
        assertEquals(1, bullets.get(SimStep.ticks(1.0)), "the aimed pulse: one bullet");
        assertEquals(3, bullets.get(SimStep.ticks(2.0)), "the fan: three");
    }

    @Test
    void aHeadFirstPickupDropsOnlyWithAHeadFirstKill() {
        List<WaveSpec.Carried> cell = List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, 0, true));
        Sortie headFirst = sortie(eel(12, List.of(), Optional.empty()), cell);
        stepUntilChain(headFirst);
        steps(headFirst, 2);
        headFirst.destroyEnemy(member(headFirst, headFirst.chain(0), 0));
        assertEquals(1, headFirst.pickupCount(), "a head-first kill drops the cell");
        assertEquals(PickupType.SHIELD_CELL, headFirst.pickup(0).type());

        Sortie cutFirst = sortie(eel(12, List.of(), Optional.empty()), cell);
        stepUntilChain(cutFirst);
        steps(cutFirst, 2);
        cutFirst.destroyEnemy(member(cutFirst, cutFirst.chain(0), 5));
        cutFirst.destroyEnemy(member(cutFirst, cutFirst.chain(0), 0));
        assertEquals(0, cutFirst.pickupCount(), "the head after a cut drops nothing");

        Sortie plain =
                sortie(eel(12, List.of(), Optional.empty()), List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, 0)));
        stepUntilChain(plain);
        steps(plain, 2);
        plain.destroyEnemy(member(plain, plain.chain(0), 5));
        plain.destroyEnemy(member(plain, plain.chain(0), 0));
        assertEquals(1, plain.pickupCount(), "a plain carried pickup drops however the head dies");

        assertThrows(
                IllegalArgumentException.class,
                () -> wave(1, PartDSpecs.MOTE, cell),
                "only a chain's head drops for a head-first kill");
    }

    @Test
    void sixteenSegmentsFitAndTheSameSeedSwimsTheSameEel() {
        EnemySpec hard =
                eel(16, List.of(EnemyGun.aimed(1.4, 1.0, 1, 130, 4, false).withTell(0.3)), Optional.of(SWAY));
        Sortie a = sortie(hard);
        Sortie b = sortie(hard);
        stepUntilChain(a);
        stepUntilChain(b);
        assertEquals(18, a.chain(0).size(), "head, 16 segments and the tail");
        assertTrue(a.chain(0).size() <= Chain.MAX_MEMBERS);
        assertEquals(18, a.enemyCount());
        for (int step = 0; step < SimStep.ticks(8); step++) {
            int command = FIRE | (step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertThrows(IllegalArgumentException.class, () -> eel(23, List.of(), Optional.empty()), "25 members");
        assertThrows(
                IllegalArgumentException.class,
                () -> hard.chain()
                        .orElseThrow()
                        .withScatter(Optional.of(SCATTER))
                        .withHeadAttacks(List.of(AIMED, AIMED, AIMED, AIMED, AIMED)),
                "at most four head attacks");
    }

    @Test
    void eelsDoNotAllocate() {
        EnemySpec spec = eel(14, List.of(AIMED, FAN), Optional.of(SWAY));
        LevelScript level = level(List.of(wave(0.5, spec, List.of()), wave(4, spec, List.of())));
        // The run covers what it measures: the sway, the head's attacks and their tells, cuts (shot, and two made at
        // once: the second cut of a front part) and scattering parts.
        java.util.function.Consumer<Sortie> run = sortie -> {
            for (int i = 0; i < 720; i++) {
                if (i == 150 || i == 160 || i == 400) {
                    cut(sortie, 6);
                }
                sortie.step(stalk(sortie, i) | FIRE);
            }
        };
        Sortie seen = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int cuts = 0;
        int glints = 0;
        int scattering = 0;
        for (int i = 0; i < 720; i++) {
            if (i == 150 || i == 160 || i == 400) {
                cut(seen, 6);
            }
            seen.step(stalk(seen, i) | FIRE);
            cuts += seen.events().count(SimEvents.Type.CHAIN_CUT);
            glints += seen.events().count(SimEvents.Type.EYE_GLINT);
            for (int c = 0; c < seen.chainCount(); c++) {
                scattering += seen.chain(c).scattering() ? 1 : 0;
            }
        }
        assertTrue(cuts >= 3 && glints > 0 && scattering > 0, "cuts " + cuts + ", tells " + glints);

        long allocated = Allocations.least(
                () -> new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR), run);
        assertEquals(0, allocated);
    }

    /** The stick that keeps the ship weaving under the eels' lane, off its centre: its shots meet the body too. */
    private static int stalk(Sortie sortie, int step) {
        double target = LANE + (step % 180 < 90 ? 30 : -30);
        double x = sortie.ship().x();
        return x > target + 4 ? Command.LEFT.bit() : x < target - 4 ? Command.RIGHT.bit() : 0;
    }

    /** Destroys member {@code link} of the first wave's chain that still has it (none when no chain has). */
    private static void cut(Sortie sortie, int link) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.chain() != null && enemy.chain().original() && enemy.link() == link) {
                sortie.destroyEnemy(i);
                return;
            }
        }
    }
}
