package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B1 (design/player/weapons/choir-resonator, Terrain: "a rock or wall stops the part of the front that
 * meets it; the rest flies on"): a rock column under one unit stops that slice of the wave; the units beside it are met
 * as before.
 */
class ResonatorTerrainTest {
    private static final double X = Ship.START_X;

    /**
     * A rock slab over x {@code from}–{@code to} and ground rows −200 to −40: when the wave fires it lies between the
     * nose and the units (40 rows below them; they enter at about row 10 with the ground), clear of the ship.
     */
    private static Terrain slab(double from, double to) {
        return new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(new double[] {from, to, to, from}, new double[] {-200, -200, -40, -40})));
    }

    /** A row of three units (left, mid, right 60 px apart) riding the ground, with {@code terrain} (or none). */
    private static Sortie sortie(Optional<Terrain> terrain) {
        List<GroundUnit> units = new ArrayList<>();
        String[] slugs = {"left", "mid", "right"};
        for (int i = 0; i < 3; i++) {
            units.add(new GroundUnit(0, X - 60 + 60 * i, Act6BSpecs.target(slugs[i], Layer.AIR, 100), -1));
        }
        LevelScript level = Act6BSpecs.level(20, HazardSpec.NONE, units, List.of());
        if (terrain.isPresent()) {
            level = TestSpecs.withGround(
                    level,
                    g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(terrain.get(), 10, 28, 0.12, 0.5))));
        }
        return new Sortie(
                1,
                TestSpecs.loadout(ResonatorTest.mount()),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** The last share of its reach the front was seen at (by {@link #fireOnce}). */
    private static double lastProgress;

    /** One wave fired when the row is 330 px above the nose; returns whether a slice of it stopped in flight. */
    private static boolean fireOnce(Sortie sortie) {
        double row = Ship.START_Y + 21 + 330;
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > row) {
            sortie.step(0);
        }
        sortie.step(Command.FIRE.bit());
        boolean stopped = false;
        WaveFire waves = sortie.fire.waves();
        while (waves.count() > 0) {
            lastProgress = waves.progress(0);
            for (int k = 0; k < WaveFire.SLICES; k++) {
                stopped |= !waves.sliceLive(0, k);
            }
            sortie.step(0);
        }
        return stopped;
    }

    private static double hp(Sortie sortie, String slug) {
        return Act6BSpecs.find(sortie, slug).hp();
    }

    @Test
    void rockUnderOneUnitStopsThatPartTheRestFliesOn() {
        Sortie rock = sortie(Optional.of(slab(X - 72, X - 48)));
        assertTrue(fireOnce(rock), "a slice stopped at the rock's face");
        assertEquals(100, hp(rock, "left"), 1e-9, "behind the rock");
        assertEquals(100 - 30 + 100 - 22.5, hp(rock, "mid") + hp(rock, "right"), 1e-9, "the rest met as before");

        Sortie open = sortie(Optional.empty());
        assertFalse(fireOnce(open), "no rock: the whole front flies");
        assertTrue(hp(open, "left") < 100, "the same front meets the left unit without the rock");
    }

    @Test
    void aFrontWhoseWholeWidthMeetsRockIsGone() {
        Sortie sortie = sortie(Optional.of(slab(0, PlayField.WIDTH)));
        fireOnce(sortie);
        assertTrue(lastProgress < 0.5, "gone at the slab's face, far short of its reach: " + lastProgress);
        assertEquals(300, hp(sortie, "left") + hp(sortie, "mid") + hp(sortie, "right"), 1e-9, "nothing met");
    }
}
