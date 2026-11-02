package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Uplinks;

/**
 * M10 part G (step G4), Level 41's sounds (design/campaign Level 41, Music &amp; ambience; design/audio/sfx, the Callisto
 * fortress's rows; round 79's a's until the picks): track 15 "Storm Wall", its base on the ice, the full mix from the
 * causeways (t=20), Vorne's motif out after the Silence line (t≈116); the {@code callisto-fortress} bed with the fortress
 * machinery layer over sections 2–10; flown, Gate One's halt: the nearest dome's hum, a pylon's death, the hatch's break,
 * the uplink's seat tone, chatter and chirp, the blast doors at the gate's opening.
 */
class Level41SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(41).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheMotifTheBedAndTheFortresssSoundsAreThere() {
        LevelData level = CONTENT.level(KEY);
        LevelData.Music data = level.music();
        assertEquals(15, data.track(), "Act 6 B, Storm Wall");
        assertEquals(1, data.startSection(), "the base stem on the ice field");
        assertEquals(2, data.fullSection(), "the full mix from the causeways (t=20)");
        assertFalse(data.full(1));
        assertTrue(data.full(2) && data.full(10), "held through the halts");
        assertTrue(data.motifOn(8, 110), "Vorne's motif under his headquarters");
        assertFalse(data.motifOn(10, 116.5), "out after the Silence line");
        assertEquals(Sfx.AMBIENCE_CALLISTO, Sfx.ambience(data.ambience()));
        assertEquals(Sfx.FORTRESS_MACHINERY, Sfx.ambience("fortress-machinery"));
        for (String file :
                new String[] {Tracks.path(15), Tracks.basePath("storm-wall"), Tracks.motifPath("storm-wall")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.AMBIENCE_CALLISTO, "callisto-fortress-r72-b.ogg"),
                Map.entry(Sfx.FORTRESS_MACHINERY, "fortress-machinery-r79-a.ogg"),
                Map.entry(Sfx.PYLON_DOME_HUM, "pylon-dome-hum-r79-a.ogg"),
                Map.entry(Sfx.PYLON_DEATH, "pylon-death-r79-a.ogg"),
                Map.entry(Sfx.HALO_BARRIER_COLLAPSE, "halo-barrier-collapse-r79-a.ogg"),
                Map.entry(Sfx.HALO_CORE_WHINE, "halo-core-whine-r79-a.ogg"),
                Map.entry(Sfx.GATE_DOORS, "gate-doors-r79-a.ogg"),
                Map.entry(Sfx.HATCH_BREAK, "hatch-break-r79-a.ogg"),
                Map.entry(Sfx.VAULT_CRACK, "vault-crack-r79-a.ogg"),
                Map.entry(Sfx.UPLINK_SEAT, "uplink-seat-r79-a.ogg"),
                Map.entry(Sfx.UPLINK_CHATTER, "uplink-chatter-r79-a.ogg"),
                Map.entry(Sfx.UPLINK_CHIRP, "uplink-chirp-r79-a.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals(Sfx.PYLON_DEATH, UnitCues.refineryDeath("shield-pylon"));
        assertEquals(1f, SovereignCues.haloPitch(0), 1e-6f, "the Halo's grind at its own pitch");
        assertTrue(SovereignCues.haloPitch(3) > SovereignCues.haloPitch(2), "pitched up with each turret lost");
        FortressCues doors = new FortressCues(new SoundMix(bank));
        doors.doors(new int[] {0, 1, 2});
        assertTrue(doors.hasDoors(0) && doors.hasDoors(2), "the three pylon gates' doors");
        assertFalse(doors.hasDoors(3), "the Halo's gate has none");
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable through Gate One's halt: over the archive core once its hatch is shot
     * open, else under the lowest awake pylon, the Ion Beam and the pods firing: the dome's hum in the halt, the hatch's
     * break, the seat tone, the chatter and the chirp, a pylon's death and the blast doors as the gate opens.
     */
    @Test
    void gateOneSoundsItsDomesPylonsUplinkAndDoors() {
        Sortie sortie = sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        sounds.gateDoors(new int[] {0, 1, 2});
        boolean hum = false;
        boolean hatch = false;
        boolean seat = false;
        boolean chatter = false;
        boolean chirp = false;
        boolean death = false;
        boolean doors = false;
        boolean opened = false;
        int last = SimStep.ticks(90);
        for (int step = 0; step < last && !sortie.complete() && !opened; step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                opened |= events.type(i) == SimEvents.Type.GATE_OPENED;
            }
            hum |= bank.looping(Sfx.PYLON_DOME_HUM);
            hatch |= bank.playing(Sfx.HATCH_BREAK) > 0;
            seat |= bank.playing(Sfx.UPLINK_SEAT) > 0;
            chatter |= bank.looping(Sfx.UPLINK_CHATTER);
            chirp |= bank.playing(Sfx.UPLINK_CHIRP) > 0;
            death |= bank.playing(Sfx.PYLON_DEATH) > 0;
            doors |= bank.playing(Sfx.GATE_DOORS) > 0;
        }
        sounds.stopLoops();
        assertTrue(opened, "Gate One opens");
        assertTrue(hum, "the dome's hum in the halt");
        assertTrue(death, "a pylon's death");
        assertTrue(doors, "the blast doors");
        assertTrue(hatch, "the hatch shot open");
        assertTrue(seat && chatter && chirp, "the uplink's seat tone, chatter and chirp");
        assertFalse(bank.looping(Sfx.PYLON_DOME_HUM) || bank.looping(Sfx.UPLINK_CHATTER), "the loops stop");
        bank.dispose();
    }

    /** Medium, the ship invulnerable with the Ion Beam and two Autocannon Pods. */
    private static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "autocannon-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "autocannon-pod", 3)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /**
     * Over an open archive core (its file uploading); else under its shut hatch (to shoot it open); else under the
     * lowest awake, uncovered pylon on the screen; else the middle.
     */
    private static int steer(Sortie sortie) {
        double targetX = 240;
        double targetY = 60;
        Uplinks uplinks = sortie.uplinks();
        boolean found = false;
        for (int c = 0; uplinks != null && c < uplinks.count() && !found; c++) {
            if (uplinks.state(c) == Uplinks.OPEN && uplinks.y(c) > 20 && uplinks.y(c) < 520) {
                targetX = uplinks.x(c);
                targetY = uplinks.y(c);
                found = true;
            }
        }
        if (!found && sortie.halting()) {
            for (int i = 0; i < sortie.groundObjectCount() && !found; i++) {
                var object = sortie.groundObject(i);
                if (object.spec().look().equals("archive-core") && object.renderY(1) < 520) {
                    targetX = object.renderX();
                    found = true;
                }
            }
        }
        if (!found) {
            double lowest = Double.POSITIVE_INFINITY;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                String slug = sortie.enemyKinds().get(enemy.kind()).slug();
                double y = enemy.renderY(1);
                if (slug.equals("shield-pylon") && !enemy.dormant() && y > 120 && y < 520 && y < lowest) {
                    lowest = y;
                    targetX = enemy.renderX(1);
                }
            }
        }
        int commands = 0;
        if (sortie.ship().x() < targetX - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > targetX + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < targetY - 3) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > targetY + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
