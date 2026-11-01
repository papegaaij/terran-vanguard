package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.audio.Sfx;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part E, step E4 (design/campaign Level 39; tools/art/refinery_units.py, backdrop_l39.py): Level 39's looks and HUD
 * by their rules: the tower's flare and fall clocks, the eruption's production frames (the tell by its progress, the
 * throw and the umbrella on their clocks, the rocks' tumble), the pipes' lights fading out when they go dark, the {@code
 * TOWERS n / 6} tracker (a counted line: the six groups share no first word); flown on medium, invulnerable with the
 * Ion Beam over every ground target: each pump station's death darkens the rack segment of its own number (the backdrop
 * piece with its {@code dark_with} under it), each tower group cleared its own pipe run, and the dead leave wrecks.
 */
class Level39LooksTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(39).orElseThrow();

    @Test
    void theClocksOfTheTowerAndTheEruption() {
        assertEquals(0, RefineryLooks.fallFrame(0, 12));
        assertEquals(6, RefineryLooks.fallFrame(0.5, 12), "12 fps");
        assertEquals(-1, RefineryLooks.fallFrame(1.0, 12), "the wreck after 1 s");
        assertEquals(
                RefineryLooks.flareFrame(0, 2, 8), RefineryLooks.flareFrame(SimStep.ticks(0.8), 2, 8), "a 0.8 s loop");
        assertTrue(RefineryLooks.flareFrame(0, 1, 8) != RefineryLooks.flareFrame(0, 2, 8), "each tower its own frame");
        assertEquals(0, EruptionLooks.tellFrame(0, 12));
        assertEquals(6, EruptionLooks.tellFrame(0.5, 12));
        assertEquals(11, EruptionLooks.tellFrame(1, 12), "the last frame at the throw");
        assertEquals(0, EruptionLooks.frameAt(0, EruptionLooks.BURST_FPS, 8));
        assertEquals(-1, EruptionLooks.frameAt(0.5, EruptionLooks.BURST_FPS, 8), "the throw over in 0.5 s");
        assertEquals(11, EruptionLooks.frameAt(3.9, EruptionLooks.UMBRELLA_FPS, 12));
        assertEquals(-1, EruptionLooks.frameAt(EruptionLooks.UMBRELLA_SECONDS, EruptionLooks.UMBRELLA_FPS, 12));
        assertEquals(-1, EruptionLooks.frameAt(-0.1, EruptionLooks.UMBRELLA_FPS, 12), "not before the eruption");
        assertEquals(
                EruptionLooks.rockFrame(3, 0, 16),
                EruptionLooks.rockFrame(3, SimStep.ticks(16 / EruptionLooks.ROCK_FPS), 16),
                "a whole tumble in 16 frames");
        assertTrue(EruptionLooks.rockFrame(0, 0, 16) != EruptionLooks.rockFrame(1, 0, 16), "each rock its own");
        assertEquals(1, BackdropPieces.litShare(Double.NaN, 5), 1e-6, "lit");
        assertEquals(1, BackdropPieces.litShare(5, 5), 1e-6);
        assertEquals(0.5, BackdropPieces.litShare(5, 5 + BackdropPieces.DARK_SECONDS / 2), 1e-6, "fading");
        assertEquals(0, BackdropPieces.litShare(5, 5 + BackdropPieces.DARK_SECONDS), 1e-6, "dark");
    }

    @Test
    void theTrackerCountsTheTowersAndThePumps() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        assertEquals("TOWERS", MissionPanel.countedLabel(script.groups(), script.groundUnits()));
        assertEquals("0 / 6", MissionPanel.countedTargets(0, 6, false));
        assertEquals("FAILED", MissionPanel.countedTargets(3, 6, true));
        assertEquals("PUMPS", script.objectives().secondary().label());
        assertEquals("4 / 6", MissionPanel.escapesCount(4, 8, 2), "the pumps count to their target");
        LevelScript level05 = SimSpecs.level(CONTENT, CONTENT.levelKey(5).orElseThrow(), Difficulty.MEDIUM);
        assertEquals("", MissionPanel.countedLabel(level05.groups(), level05.groundUnits()), "Level 05's marks");
        LevelScript level09 = SimSpecs.level(CONTENT, CONTENT.levelKey(9).orElseThrow(), Difficulty.MEDIUM);
        assertEquals("", MissionPanel.countedLabel(level09.groups(), level09.groundUnits()), "Level 09's marks");
    }

    @Test
    void theRefinerysOwnSounds() {
        assertEquals(Sfx.AMBIENCE_IO_REFINERY, Sfx.ambience("io-refinery"));
        assertEquals("sfx/io-refinery-r72-a.ogg", Sfx.AMBIENCE_IO_REFINERY.path());
        assertEquals("sfx/tower-collapse-r77-a.ogg", Sfx.TOWER_COLLAPSE.path());
        assertEquals("sfx/pump-blast-r77-a.ogg", Sfx.PUMP_BLAST.path());
    }

    /**
     * Flown on medium, invulnerable with the Ion Beam, over the lowest tower or pump station on the screen: each pump's
     * death darkens the rack segment under it (its number is the data's order), each tower group cleared its own pipes.
     */
    @Test
    void thePipesGoDarkWithTheirTowerOrPump() {
        LevelData level = CONTENT.level(KEY);
        Sortie sortie = sortie();
        RefineryLooks looks =
                new RefineryLooks(null, sortie.enemyKinds(), sortie.script().groups());
        List<String> dark = new ArrayList<>();
        int pumpChecks = 0;
        long limit = SimStep.ticks(260);
        while (!sortie.complete() && sortie.tick() < limit) {
            sortie.step(steer(sortie) | Command.FIRE.bit());
            looks.stepped(sortie);
            for (int d = 0; d < looks.pendingDark(); d++) {
                String name = looks.pendingDark(d);
                dark.add(name);
                if (name.matches(RefineryLooks.PUMP_PREFIX + "\\d+")) {
                    pumpChecks += pumpUnderItsRack(level, sortie, name) ? 1 : 0;
                }
            }
            looks.clearDark();
        }
        Set<String> names = new LinkedHashSet<>(dark);
        assertEquals(dark.size(), names.size(), "each light goes dark once: " + dark);
        long pumps = names.stream().filter(n -> n.matches("P\\d+")).count();
        long towers = names.size() - pumps;
        assertTrue(pumps >= 4, "pump racks dark: " + names);
        assertEquals(pumps, pumpChecks, "each pump's rack under it: " + names);
        assertTrue(towers >= 2, "tower pipes dark: " + names);
        Set<String> darkWith = new LinkedHashSet<>();
        for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
            placed.darkWith().ifPresent(darkWith::add);
        }
        assertTrue(darkWith.containsAll(names), "every name is a dark_with of the backdrop: " + names);
    }

    /** The rack segment of pump {@code name} lies under the pump station that died in this step. */
    private static boolean pumpUnderItsRack(LevelData level, Sortie sortie, String name) {
        SimEvents events = sortie.events();
        for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
            if (!placed.darkWith().filter(name::equals).isPresent()) {
                continue;
            }
            double rackY = level.pieceCentre(placed) - sortie.groundScroll();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED
                        && sortie.enemyKinds().get(events.value(i)).slug().equals(RefineryLooks.PUMP)
                        && Math.abs(events.x(i) - placed.x()) <= 25
                        && Math.abs(events.y(i) - rackY) <= 120) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Low on the field under the lowest tower or pump station on the screen, else the middle. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double lowest = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            String slug = sortie.enemyKinds().get(enemy.kind()).slug();
            if ((slug.equals(RefineryLooks.TOWER) || slug.equals(RefineryLooks.PUMP))
                    && enemy.renderY(1) < 520
                    && enemy.renderY(1) > 120
                    && enemy.renderY(1) < lowest) {
                lowest = enemy.renderY(1);
                target = enemy.renderX(1);
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 57) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 63) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    /** Medium, the ship invulnerable with the Ion Beam. */
    static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }
}
