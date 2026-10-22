package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.ENEMY_FIRE;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.decibels;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.PlayField;
import vanguard.sim.RisePhase;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M8 part D, Level 24's sounds for {@link FlightSounds} (design/audio/sfx, Act 4 rows; round 53's a variants until the
 * user's pick): the Abyss Ray's horn from its shadow ({@code RISE_SHADOW}) through the rise, its wing strokes once as it
 * levels out into its glide, its fan instead of the shots' report and its death over its burst; the Barbfin's needle
 * instead of the small shot's report; the spawning reef's iris grinding open with its telegraph (instead of the Hive
 * Node's, its release silent: the iris's grind ends as the fry dart out) and its collapse over its burst; the relay
 * beacons' pulse, a far sonar ping on each 2 s pulse while a beacon is on the screen, low under the music; the
 * <i>Meridian</i>'s hatch letting go as the wreck is shot open.
 */
final class AbyssCues extends SoundFamily {
    /** The units with sounds of their own, by their slugs. */
    static final String RAY = "abyss-ray";

    static final String BARBFIN = "barbfin";
    static final String REEF = "spawning-reef";
    /** The secret's wreck by its look, and the beacon pieces by their id. */
    static final String WRECK = "meridian-wreck";

    static final String BEACON = "relay-beacon";
    /** The beacons pulse every 2 s on the real clock (the pieces' 16 frames at 8 fps, the flash on frame 0). */
    static final double PULSE_SECONDS = 2;
    /** The horn, the strokes, the iris and the collapse sit at their files' levels (levelled beside their peers). */
    static final float RAY_LEVEL = 1f;
    /** The beacons' ping 6 dB under the Sonar Pulse in its file, played a further 6 dB down: under the music. */
    static final float BEACON_LEVEL = decibels(-6);

    /** Per enemy kind (from the first note): a Ray, a Barbfin, a reef. */
    private boolean[] rays;

    private boolean[] barbfins;
    private boolean[] reefs;
    /** Per ground object: the Meridian's wreck. */
    private boolean[] wrecks;
    /** The beacons' centres on the ground (scroll px at the screen's bottom) and x; empty for another level. */
    private double[] beaconY = new double[0];

    private double[] beaconX = new double[0];
    private double beaconHalf;
    /** The last Ray whose glide's strokes played; the pulse count at the last watch. */
    private int glided = -1;

    private long pulses = Long.MIN_VALUE;
    /** The attempt at the last watch (a retry forgets the Ray whose strokes played). */
    private int attempt = -1;

    AbyssCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(SimEvents.Type.RISE_SHADOW);
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (events.type(i) == SimEvents.Type.RISE_SHADOW && !mix.resync) {
            bank.play(Sfx.RAY_RISE, RAY_LEVEL, pitch(0.02), pan);
        }
    }

    /** Notes the level's kinds: which are the Ray, the Barbfin, the reef. */
    void noteKinds(List<EnemySpec> kinds) {
        rays = new boolean[kinds.size()];
        barbfins = new boolean[kinds.size()];
        reefs = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            rays[k] = slug.equals(RAY);
            barbfins[k] = slug.equals(BARBFIN);
            reefs[k] = slug.equals(REEF);
        }
    }

    /** Notes the level's ground objects: which is the Meridian's wreck. */
    void noteGround(List<GroundObjectSpec> objects) {
        wrecks = new boolean[objects.size()];
        for (int g = 0; g < wrecks.length; g++) {
            wrecks[g] = WRECK.equals(objects.get(g).look());
        }
    }

    /** Notes the level's relay beacons from its backdrop (Level 24's three; none elsewhere). */
    void noteBeacons(LevelData level) {
        BackdropData backdrop = level.backdrop();
        BackdropData.Piece beacon = backdrop.pieces().get(BEACON);
        if (beacon == null) {
            return;
        }
        List<BackdropData.PlacedPiece> placed = backdrop.placements().stream()
                .filter(piece -> piece.piece().equals(BEACON))
                .toList();
        beaconY = new double[placed.size()];
        beaconX = new double[placed.size()];
        for (int b = 0; b < placed.size(); b++) {
            beaconY[b] = level.pieceCentre(placed.get(b));
            beaconX[b] = placed.get(b).x();
        }
        beaconHalf = beacon.size().height() / 2;
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** A kill of kind {@code kind}: a Ray's dying groan or a reef's collapse over its burst; false for another kind. */
    boolean destroyed(int kind, float pan) {
        Sfx extra = is(rays, kind) ? Sfx.RAY_DEATH : is(reefs, kind) ? Sfx.REEF_DEATH : null;
        if (extra == null) {
            return false;
        }
        bank.play(extra, is(rays, kind) ? EXPLOSIONS : RAY_LEVEL, pitch(0.03), pan);
        return true;
    }

    /** A volley left {@code unit} (null: none found): a Ray's fan or a Barbfin's needle instead of a gun's report. */
    boolean fired(Enemy unit, float pan) {
        if (unit == null) {
            return false;
        }
        if (is(rays, unit.kind())) {
            bank.play(Sfx.RAY_FAN, ENEMY_FIRE, pitch(0.04), pan);
            return true;
        }
        if (is(barbfins, unit.kind())) {
            bank.play(Sfx.BARBFIN_NEEDLE, ENEMY_FIRE, pitch(0.06), pan);
            return true;
        }
        return false;
    }

    /** A periodic spawner's telegraph (value: its kind): a reef's iris grinding open; false for another kind. */
    boolean telegraph(int kind, float pan) {
        if (!is(reefs, kind)) {
            return false;
        }
        bank.play(Sfx.REEF_IRIS, RAY_LEVEL, pitch(0.04), pan);
        return true;
    }

    /** A periodic spawner's release (value: its kind): a reef's is silent (its iris's grind carries it). */
    boolean released(int kind) {
        return is(reefs, kind);
    }

    /** Once a step: a Ray's strokes as it starts its glide, the Meridian's hatch, the beacons' pulse. */
    void watch(Sortie sortie) {
        if (rays == null) {
            noteKinds(sortie.enemyKinds());
            noteGround(sortie.script().groundObjects());
        }
        if (sortie.attempt() != attempt) {
            attempt = sortie.attempt();
            glided = -1;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (is(rays, enemy.kind()) && enemy.risePhase() == RisePhase.GLIDE && enemy.serial() != glided) {
                glided = enemy.serial();
                if (!mix.resync) {
                    bank.play(Sfx.RAY_GLIDE, RAY_LEVEL, pitch(0.02), pan(enemy.renderX(1)));
                }
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.TRIGGER_SPENT && !mix.resync) {
                spent(events.value(e), pan(events.x(e)));
            }
        }
        long pulse = (long) Math.floor(sortie.realSeconds() / PULSE_SECONDS);
        if (pulse != pulses && pulses != Long.MIN_VALUE && sortie.flying() && !mix.resync) {
            int beacon = beaconOnScreen(sortie.groundScroll());
            if (beacon >= 0) {
                bank.play(Sfx.BEACON_PULSE, BEACON_LEVEL, 1, pan(beaconX[beacon]));
            }
        }
        pulses = pulse;
    }

    /** Ground object {@code index} took its last hit: the Meridian's hatch lets go; false for another object. */
    boolean spent(int index, float pan) {
        if (wrecks == null || index < 0 || index >= wrecks.length || !wrecks[index]) {
            return false;
        }
        bank.play(Sfx.WRECK_HATCH, RAY_LEVEL, pitch(0.02), pan);
        return true;
    }

    /** The beacon on the screen at the ground's scroll {@code scroll} (the lowest), -1 for none. */
    int beaconOnScreen(double scroll) {
        int found = -1;
        double lowest = Double.POSITIVE_INFINITY;
        for (int b = 0; b < beaconY.length; b++) {
            double y = beaconY[b] - scroll;
            if (y + beaconHalf > 0 && y - beaconHalf < PlayField.HEIGHT && y < lowest) {
                lowest = y;
                found = b;
            }
        }
        return found;
    }
}
