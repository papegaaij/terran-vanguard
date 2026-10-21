package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * Terrain walls stop the Lance Laser (design/player/weapons/lance-laser, Behaviour; design/campaign Level 16,
 * What walls stop): with the real lance data at level 5 (pierce 5), a lance pierces the unit in front of a
 * rock island and ends at the rock with a spark; the unit behind the rock, in the same column, takes nothing.
 * Without the rock the same lances reach it, so the rock is what stops them.
 */
class LanceWallTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = PlayField.WIDTH / 2.0;
    private static final double UNIT_HEIGHT = 20;
    /** The behind unit enters this long after the front one: about 260 ground rows above it (the ground scrolls 130 px/s). */
    private static final double GAP_SECONDS = 2;
    /** 3 s: both units on the screen above the ship's nose, the rock above the front one. */
    private static final int STEPS = 180;

    /** A ground target that neither moves on its own nor fires, too tough to die. */
    private static final EnemySpec DUMMY = new EnemySpec(
            "dummy",
            1e6,
            new Hitbox(40, UNIT_HEIGHT),
            Layer.GROUND,
            0,
            false,
            0,
            0,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            true);

    private static LevelScript level(Optional<Terrain> rock) {
        GroundSpec ground = GroundSpec.NONE.withTerrain(
                rock.map(terrain -> new GroundSpec.TerrainSpec(terrain, 10, 28, 0.12, 0.5)));
        return LevelScript.builder(16, 3)
                .sections(List.of(new Section(12, 130)))
                .groundUnits(List.of(new GroundUnit(0, X, DUMMY, -1), new GroundUnit(GAP_SECONDS, X, DUMMY, -1)))
                .objectives(ObjectiveSpec.NONE)
                .ground(ground)
                .build();
    }

    private static Sortie sortie(LevelScript level) {
        Loadout loadout = SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getLast().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "lance-laser", 5)),
                CONTENT.shields().models().get(1).name(),
                CONTENT.armour().plating().get(1).name(),
                0,
                Difficulty.MEDIUM);
        return new Sortie(
                16,
                loadout,
                level,
                SimSpecs.rules(CONTENT, Level16Test.LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    /** The ground scroll at which each unit entered, from a flight without rock and without fire. */
    private static double[] entryScrolls() {
        Sortie probe = sortie(level(Optional.empty()));
        double[] scrolls = new double[2];
        int seen = 0;
        for (int step = 0; step < STEPS && seen < 2; step++) {
            probe.step(Command.NONE);
            while (seen < probe.enemyCount() && seen < 2) {
                scrolls[seen++] = probe.groundScroll();
            }
        }
        assertEquals(2, seen, "both units entered");
        return scrolls;
    }

    /** Flies {@code level} with fire held: {front hp lost, behind hp lost, rock sparks, shots fired}. */
    private static double[] fly(LevelScript level) {
        Sortie sortie = sortie(level);
        double sparks = 0;
        double fired = 0;
        for (int step = 0; step < STEPS; step++) {
            sortie.step(FIRE);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ROCK_SPARK && events.value(i) == 0) {
                    sparks++;
                }
            }
            fired += events.count(SimEvents.Type.SHOT_FIRED);
        }
        assertEquals(2, sortie.enemyCount(), "both units alive on the screen");
        return new double[] {1e6 - sortie.enemy(0).hp(), 1e6 - sortie.enemy(1).hp(), sparks, fired};
    }

    @Test
    void aRockIslandStopsTheLanceAndTheUnitBehindItTakesNoDamage() {
        double[] entry = entryScrolls();
        // A unit enters with its centre at y 540 + 10, so at row (entry scroll + 10); its rows span ±10.
        double frontTop = entry[0] + UNIT_HEIGHT;
        double behindFoot = entry[1];
        assertTrue(behindFoot - frontTop > 80, "room for the rock between the units: " + (behindFoot - frontTop));
        double low = frontTop + 25;
        double high = behindFoot - 25;
        Terrain block = new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(
                        new double[] {X - 60, X + 60, X + 60, X - 60}, new double[] {low, low, high, high})));

        double[] open = fly(level(Optional.empty()));
        assertTrue(open[1] > 0, "without rock the lances pierce the front unit and reach the one behind");

        double[] walled = fly(level(Optional.of(block)));
        assertTrue(walled[3] > 10, "the lance fired: " + walled[3]);
        assertTrue(walled[0] > 0, "the unit in front of the rock is hit");
        assertEquals(0, walled[1], "the unit behind the rock takes no damage");
        assertTrue(walled[2] > 0, "the lances end at the rock with a spark");
    }
}
