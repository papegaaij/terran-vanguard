package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part D, the spawning reef's fry (design/enemies/ground/spawning-reef; m8d-gaps.md §1.4, §5 D4): a periodic
 * spawner whose kind is a flock's (the Glassfin) releases its fry as single units, not a school: each flies straight out
 * over the spawner's arc toward the ship, then runs the Glassfin's ram on its own (its draw, its glint, its straight
 * dive at the ship's lane). Fry already out outlive the reef, whose death stops its releases. Allocation-free.
 */
class ReefFryTest {
    /** A ram that every fry takes, 0.5 s after it came onto the field. */
    static final EnemySpec FRY = GlassfinTest.glassfin(GlassfinTest.flock(
            Optional.of(GlassfinTest.RIBBON), Optional.of(new EnemySpec.Ram(0.5, 1, 0.3, 0.5, 230))));

    /** The reef: 200 HP, a soft ground unit releasing 4 fry every 3.5 s over 120° at 160 px/s, shut within 96 px. */
    static final EnemySpec REEF = new EnemySpec(
            "spawning-reef",
            200,
            new Hitbox(80, 80),
            Layer.GROUND,
            15,
            false,
            40,
            0,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            true,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.of(new EnemySpec.Spawner(FRY, 4, 3.5, 0.5, Math.toRadians(120), 160, 96)),
            Optional.empty());

    private static LevelScript level() {
        return PartCSpecs.level(30, 30, List.of(), List.of(new GroundUnit(0, Ship.START_X, REEF, -1)));
    }

    private static Sortie sortie() {
        return new Sortie(1, TestSpecs.LOADOUT, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static void toRelease(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.SPAWN_RELEASED) > 0) {
                return;
            }
        }
        throw new AssertionError("no release");
    }

    @Test
    void itsFryFlyStraightOutThenEachGlintsAndDivesOnItsOwn() {
        Sortie sortie = sortie();
        toRelease(sortie);
        List<Enemy> fry = AbyssRayTest.all(sortie, "glassfin");
        assertEquals(4, fry.size(), "four single fry");
        assertEquals(0, sortie.flockCount(), "not a school");
        Map<Integer, double[]> velocity = new HashMap<>();
        for (Enemy one : fry) {
            velocity.put(one.serial(), new double[] {one.x(), one.y()});
        }
        sortie.step(Command.NONE);
        for (Enemy one : fry) {
            double[] was = velocity.get(one.serial());
            assertEquals(160 * SimStep.SECONDS, Math.hypot(one.x() - was[0], one.y() - was[1]), 1e-6, "straight out");
        }
        int glints = 0;
        int diving = 0;
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(Command.NONE);
            glints += sortie.events().count(SimEvents.Type.EYE_GLINT);
        }
        for (Enemy one : AbyssRayTest.all(sortie, "glassfin")) {
            if (one.ramDiving()) {
                diving++;
                double[] was = {one.x(), one.y()};
                sortie.step(Command.NONE);
                assertEquals(230 * SimStep.SECONDS, Math.hypot(one.x() - was[0], one.y() - was[1]), 1e-6, "its dive");
                break;
            }
        }
        assertEquals(4, glints, "each its own glint");
        assertTrue(diving > 0, "they dive");
    }

    @Test
    void theFryOutliveTheReefWhoseDeathStopsItsReleases() {
        Sortie sortie = sortie();
        toRelease(sortie);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals("spawning-reef")) {
                sortie.destroyEnemy(i);
                break;
            }
        }
        assertEquals(4, AbyssRayTest.all(sortie, "glassfin").size(), "the fry live on");
        int releases = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            releases += sortie.events().count(SimEvents.Type.SPAWN_RELEASED);
        }
        assertEquals(0, releases, "no more releases");
    }

    @Test
    void theSameSeedFliesTheSameFryWithoutAllocating() {
        Sortie a = sortie();
        Sortie b = sortie();
        for (int step = 0; step < SimStep.ticks(14); step++) {
            a.step(Command.FIRE.bit());
            b.step(Command.FIRE.bit());
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        java.util.function.Consumer<Sortie> run = sortie -> {
            for (int step = 0; step < SimStep.ticks(14); step++) {
                sortie.step(step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
            }
        };
        assertEquals(0, Allocations.least(ReefFryTest::sortie, run));
    }
}
