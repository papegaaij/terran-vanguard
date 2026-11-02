package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Domes;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Uplinks;

/**
 * M10 part G, step G4: Level 41's sounds for {@link FlightSounds} (design/campaign Level 41, Music &amp; ambience;
 * design/audio/sfx, the Callisto fortress's rows; round 79's picks: S.8 and S.10 b, the rest a):
 *
 * <ul>
 *   <li>a gate's blast doors parting at its {@code GATE_OPENED} ({@link Sfx#GATE_DOORS}), only the gates with doors
 *       ({@link #doors}: the backdrop's {@code doors}, not the Halo's halt);
 *   <li>the nearest standing pylon dome's hum while a gate's halt holds ({@link Sfx#PYLON_DOME_HUM}, a loop panned to
 *       it, faded in and out);
 *   <li>Varga's uplink: an archive core's hatch shot open ({@code UPLINK_OPEN}, {@link Sfx#HATCH_BREAK}), the spike
 *       seating as the ship comes over an open core ({@code UPLINK_PROGRESS}, {@link Sfx#UPLINK_SEAT}), the data chatter
 *       while it uploads ({@link Sfx#UPLINK_CHATTER}, a loop), the file in ({@code FILE_IN}, {@link
 *       Sfx#UPLINK_CHIRP});
 *   <li>the lab vault broken open (its trigger spent, {@link Sfx#VAULT_CRACK}), heard beside the secret's chime.
 * </ul>
 *
 * The pylon's death is {@link UnitCues}' (over its rung), the Halo's {@link SovereignCues}'. Nothing is allocated per
 * step.
 */
final class FortressCues extends SoundFamily {
    /** The one-shots sit at their files' levels (levelled by step AUD-G). */
    static final float CUE_LEVEL = 1f;
    /** The lab vault's look: its trigger's death is the vault crack. */
    static final String VAULT = "lab-vault";

    /** Per held gate (from 0) whether it has blast doors (the backdrop's {@code doors}: gate n is index n - 1). */
    private boolean[] doors = new boolean[0];

    private float humLevel;
    private float chatterLevel;

    FortressCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(
                SimEvents.Type.GATE_OPENED,
                SimEvents.Type.UPLINK_OPEN,
                SimEvents.Type.UPLINK_PROGRESS,
                SimEvents.Type.FILE_IN);
    }

    /** The held gates (from 0) whose blast doors part as they open (Level 41's three pylon gates). */
    void doors(int[] gates) {
        int size = 0;
        for (int gate : gates) {
            size = Math.max(size, gate + 1);
        }
        doors = new boolean[size];
        for (int gate : gates) {
            doors[gate] = true;
        }
    }

    /** Whether held gate {@code gate} (from 0) has blast doors. */
    boolean hasDoors(int gate) {
        return gate >= 0 && gate < doors.length && doors[gate];
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case GATE_OPENED -> {
                if (hasDoors(events.value(i))) {
                    bank.play(Sfx.GATE_DOORS, CUE_LEVEL, 1, pan);
                }
            }
            case UPLINK_OPEN -> bank.play(Sfx.HATCH_BREAK, CUE_LEVEL, pitch(0.04), pan);
            case UPLINK_PROGRESS -> bank.play(Sfx.UPLINK_SEAT, CUE_LEVEL, 1, pan);
            case FILE_IN -> {
                bank.play(Sfx.UPLINK_CHIRP, CUE_LEVEL, 1, pan);
                if (bank.looping(Sfx.UPLINK_CHATTER)) {
                    bank.stop(Sfx.UPLINK_CHATTER);
                    chatterLevel = 0;
                }
            }
            default -> {}
        }
    }

    /**
     * Once a step: the dome's hum on the nearest standing dome while a gate holds; the chatter while a file uploads; the
     * vault's crack as its trigger is spent.
     */
    void watch(Sortie sortie) {
        humLevel = fadeLoop(Sfx.PYLON_DOME_HUM, humLevel, humTarget(sortie), pan(nearestDome(sortie)));
        Uplinks uplinks = sortie.uplinks();
        boolean uploading = false;
        double x = 240;
        if (uplinks != null && sortie.flying() && !sortie.complete()) {
            for (int c = 0; c < uplinks.count(); c++) {
                if (uplinks.uploading(c)) {
                    uploading = true;
                    x = uplinks.x(c);
                }
            }
        }
        chatterLevel = fadeLoop(Sfx.UPLINK_CHATTER, chatterLevel, uploading ? FlightSounds.UNIT_LOOP_LEVEL : 0, pan(x));
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.TRIGGER_SPENT && !mix.resync && vault(sortie, events.value(e))) {
                bank.play(Sfx.VAULT_CRACK, CUE_LEVEL, 1, pan(events.x(e)));
            }
        }
    }

    /** The hum's level: full while a gate's halt holds with a dome standing, else none. */
    private static float humTarget(Sortie sortie) {
        Domes domes = sortie.domes();
        if (domes == null || !sortie.halting() || !sortie.flying() || sortie.complete()) {
            return 0;
        }
        for (int slot = 0; slot < domes.capacity(); slot++) {
            if (domes.tracked(slot) && domes.standing(slot)) {
                return FlightSounds.UNIT_LOOP_LEVEL;
            }
        }
        return 0;
    }

    /** The x of the standing dome nearest the ship (the middle without one). */
    private static double nearestDome(Sortie sortie) {
        Domes domes = sortie.domes();
        double best = Double.POSITIVE_INFINITY;
        double x = 240;
        for (int slot = 0; domes != null && slot < domes.capacity(); slot++) {
            if (!domes.tracked(slot) || !domes.standing(slot)) {
                continue;
            }
            double d = Math.hypot(
                    domes.x(slot) - sortie.ship().x(),
                    domes.y(slot) - sortie.ship().y());
            if (d < best) {
                best = d;
                x = domes.x(slot);
            }
        }
        return x;
    }

    /** Whether ground object {@code index} of the script is the lab vault. */
    private static boolean vault(Sortie sortie, int index) {
        var objects = sortie.script().groundObjects();
        return index >= 0
                && index < objects.size()
                && VAULT.equals(objects.get(index).look());
    }

    /** The flight's loops are stopped as the level screen closes. */
    void silence() {
        humLevel = 0;
        chatterLevel = 0;
    }
}
