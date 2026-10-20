package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part F: the Warden Tank's looks (design/enemies/ground/warden-tank; tools/art/warden_tank.py's sprites and
 * assets/pivots/warden-tank.json): the hull's 16 headings with 2 tread frames (drawn as a walker by EnemyLooks), the
 * turret's 32 aims with its recoil frame, the muzzle flash's 2 ages at the muzzle, the torn turret ring, the wreck's
 * 16 headings. Flown in Level 19 under the halls' first belt: a tank's turret turns on its own, stops for its burst
 * (the tell) and fires along its aim; shot down, its wreck rides the belt up the screen at 40 px/s on top of the
 * scroll ({@link BeltWrecks}).
 */
class TankLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(TankLooks.HEADINGS * 2, frames("warden-tank"), "16 headings x 2 tread frames");
        assertEquals(TankLooks.AIMS * 2, frames("warden-tank-turret"), "32 aims x rest and recoil");
        assertEquals(TankLooks.AIMS * 2, frames("warden-tank-flash"), "32 aims x 2 ages");
        assertEquals(TankLooks.HEADINGS, frames("warden-tank-wreck"));
        assertTrue(Files.isRegularFile(SPRITES.resolve("warden-tank-turret-stub.png")), "the torn ring");
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/warden-tank.json")));
        assertEquals(37.3, pivots.getDouble("muzzle"), 1e-9);
        assertTrue(pivots.getDouble("muzzle_recoil") < pivots.getDouble("muzzle"), "the barrel back in the recoil");
        assertEquals(
                vanguard.game.audio.Sfx.TANK_DEATH,
                EnemyLooks.explosionSound("warden-tank", vanguard.content.Tier.MEDIUM, false),
                "its hull explosion with the metal clang, the medium rung's Warden Tank entry");
    }

    @Test
    void theFramesFollowTheAimTheShotAndTheHeading() {
        assertEquals(0, TankLooks.turretFrame(0, false), "aiming down, at rest");
        assertEquals(17, TankLooks.turretFrame(Math.PI / 2, true), "a quarter turn clockwise (left), recoiled");
        assertEquals(32, TankLooks.turretFrame(Math.PI, false), "up the screen: the aim it fires back with");
        assertEquals(0, TankLooks.turretFrame(2 * Math.PI - 0.05, false));
        assertTrue(TankLooks.recoiled(0) && TankLooks.recoiled(TankLooks.RECOIL_TICKS - 1));
        assertFalse(TankLooks.recoiled(TankLooks.RECOIL_TICKS), "back at rest after 0.1 s");
        assertFalse(TankLooks.recoiled(-1), "no shot");
        assertEquals(32, TankLooks.flashFrame(Math.PI, 0), "the shot's flash");
        assertEquals(33, TankLooks.flashFrame(Math.PI, TankLooks.FLASH_AGE_TICKS), "fading");
        assertEquals(-1, TankLooks.flashFrame(Math.PI, 2L * TankLooks.FLASH_AGE_TICKS), "gone");
        assertEquals(8, TankLooks.wreckFrame(Math.PI), "the wreck keeps the hull's heading");
        assertEquals(100, TankLooks.alongX(100, 0, 37.3), 1e-9);
        assertEquals(200 - 37.3, TankLooks.alongY(200, 0, 37.3), 1e-9, "aiming down: the muzzle below");
        assertEquals(200 + 37.3, TankLooks.alongY(200, Math.PI, 37.3), 1e-9, "aiming up: above");
        assertEquals(100 - 37.3, TankLooks.alongX(100, Math.PI / 2, 37.3), 1e-9, "aiming left");
    }

    /**
     * Flown in Level 19 under the halls' first belt (x 140) from t=28, firing: the first tank's turret turns (more than
     * one aim), stops for its bursts and fires them near the tank; shot down, it leaves its wreck, which rides the belt.
     */
    @Test
    void flownATankTurnsLocksFiresAndItsWreckRidesTheBelt() {
        Sortie sortie = FoundryFixture.sortie();
        while (sortie.levelSeconds() < 28) {
            sortie.step(FoundryFixture.toward(sortie, 240));
        }
        BeltWrecks wrecks = new BeltWrecks(4);
        Set<Integer> aims = new HashSet<>();
        boolean locked = false;
        int shotsNearTank = 0;
        int downs = 0;
        double onBelt = 0;
        while (sortie.levelSeconds() < 50 && (downs == 0 || onBelt < 1)) {
            sortie.step(FoundryFixture.toward(sortie, 140));
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().turret().isPresent()) {
                    aims.add(TankLooks.turretFrame(enemy.turretAim(), false) / 2);
                    locked |= enemy.turretLocked();
                }
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_FIRED) {
                    for (int i = 0; i < sortie.enemyCount(); i++) {
                        Enemy enemy = sortie.enemy(i);
                        if (enemy.spec().turret().isPresent()
                                && Math.hypot(enemy.renderX(1) - events.x(e), enemy.renderY(1) - events.y(e))
                                        <= TankLooks.FIRED_REACH) {
                            shotsNearTank++;
                        }
                    }
                } else if (events.type(e) == SimEvents.Type.WALKER_DOWN
                        && sortie.enemyKinds()
                                .get(SimEvents.walkerKind(events.value(e)))
                                .turret()
                                .isPresent()) {
                    downs++;
                    wrecks.add(0, events.x(e), events.y(e), 0, sortie.groundScroll());
                }
            }
            double[] before = new double[wrecks.slots()];
            for (int w = 0; w < before.length; w++) {
                before[w] = wrecks.kind(w) < 0 ? 0 : wrecks.carried(w);
            }
            wrecks.stepped(sortie);
            for (int w = 0; w < before.length; w++) {
                double moved = wrecks.kind(w) < 0 ? 0 : wrecks.carried(w) - before[w];
                if (moved > 0) {
                    onBelt += SimStep.SECONDS;
                    assertEquals(40 * SimStep.SECONDS, moved, 1e-9, "the belt's 40 px/s");
                }
            }
        }
        assertTrue(aims.size() > 1, "the turret turns on its own: " + aims);
        assertTrue(locked, "it stops for its burst (the tell)");
        assertTrue(shotsNearTank > 0, "its shots leave the tank");
        assertTrue(downs > 0, "shot down");
        assertTrue(onBelt >= 1, "its wreck rode the belt: " + onBelt + " s");
    }

    @Test
    void aWreckScrollsWithTheGroundAndGoesBelowTheScreen() {
        BeltWrecks wrecks = new BeltWrecks(2);
        wrecks.add(3, 100, 300, 5, 1000);
        assertEquals(1, wrecks.count());
        assertEquals(3, wrecks.kind(0));
        assertEquals(5, wrecks.frame(0));
        assertEquals(300, wrecks.y(0, 1000), 1e-9, "where it died");
        assertEquals(200, wrecks.y(0, 1100), 1e-9, "carried down by the scroll");
        wrecks.add(3, 50, 50, 0, 1000);
        wrecks.add(4, 60, 60, 0, 1000);
        assertEquals(2, wrecks.count(), "the oldest gives way");
        assertEquals(4, wrecks.kind(0));
        wrecks.clear();
        assertEquals(0, wrecks.count());
    }

    private static int frames(String root) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.filter(file -> file.getFileName().toString().matches(root + "_\\d+\\.png"))
                    .count();
        }
    }
}
