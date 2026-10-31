package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A: the sounds of Act 6's unit mechanics for {@link FlightSounds} (design/audio/sfx, Act 6 sounds): round
 * 72's picks, levelled in their files, and, for the sounds round 73 still proposes (QA-1), the nearest built sound until
 * the user's pick ({@code *_STAND_IN}):
 *
 * <ul>
 *   <li>the mirrors: a bounce's glassy ping ({@link Sfx#MIRROR_PING}, once per {@value #PING_GAP_SECONDS} s), an
 *       absorbed shot's dry tick (a Reflex bolt, Rook's shot, a Plasma Arc earthed on the face: {@link #ABSORBED_STAND_IN}
 *       quietly, once per {@value #TICK_GAP_SECONDS} s), the break ({@link #BREAK_STAND_IN}), the regrowth's chime
 *       ({@link #REGROW_STAND_IN} lowered), and while a beam heats a mirror one hiss loop for the nearest heating,
 *       pitched from {@value #HEAT_PITCH_LOW} cold to {@value #HEAT_PITCH_HIGH} at the break ({@link #HEAT_STAND_IN});
 *   <li>regeneration: the healing creak ({@link Sfx#REGROWTH_CREAK}) repeated every {@value #CREAK_SECONDS} s for the
 *       nearest healing unit while any heals;
 *   <li>regrowth: a regrown turret's shell bursting (its first death's is the kill's rung) in the small rung, the root's
 *       death ({@link #ROOT_STAND_IN}: the tissue tear comes with the Graft Turret, part C), the knit's creak and knock
 *       ({@link Sfx#GRAFT_REGROWTH});
 *   <li>the domes: dropping and collapsing ({@link Sfx#PYLON_DOME_DOWN}), coming back ({@link Sfx#PYLON_DOME_UP}), a hit
 *       spent on one ({@link #DOME_HIT_STAND_IN}, once per {@value #DOME_HIT_GAP_SECONDS} s; never the Level 17 colony
 *       dome's glass boom).
 * </ul>
 *
 * The spoofer's stutter is seen, not heard; the war's kills play the prey's rung ({@link UnitCues}).
 */
final class JovianCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.MIRROR_BOUNCED,
            SimEvents.Type.MIRROR_ABSORBED,
            SimEvents.Type.MIRROR_BROKEN,
            SimEvents.Type.MIRROR_REGROWING,
            SimEvents.Type.ROOT_EXPOSED,
            SimEvents.Type.ROOT_KILLED,
            SimEvents.Type.KNITTING,
            SimEvents.Type.DOME_HIT,
            SimEvents.Type.DOME_DOWN,
            SimEvents.Type.DOME_UP,
            SimEvents.Type.DOME_COLLAPSED);

    /** Round 73 QA-1 stand-ins (the nearest built sounds) until the user picks the proposals. */
    static final Sfx HEAT_STAND_IN = Sfx.TENDRIL_BURN;

    static final Sfx BREAK_STAND_IN = Sfx.GLASSFIN_SHATTER;
    static final Sfx REGROW_STAND_IN = Sfx.SONDE_CHIME;
    static final Sfx DOME_HIT_STAND_IN = Sfx.ENEMY_SHIELD_HIT;
    static final Sfx ABSORBED_STAND_IN = Sfx.DEFLECTOR_TICK;
    /** The root's death until the tissue tear is imported (round 72 S.2, with the Graft Turret): a cable's dry snap. */
    static final Sfx ROOT_STAND_IN = Sfx.TENDRIL_BREAK;
    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The stand-in chime, lower (a low soft chime) and a little under its own level. */
    static final float REGROW_PITCH = 0.8f;

    static final float REGROW_LEVEL = 0.8f;
    /** The absorbed tick: quiet, under the fire. */
    static final float TICK_LEVEL = 0.5f * HITS;
    /** The heat loop's pitch, cold to the break. */
    static final float HEAT_PITCH_LOW = 0.85f;

    static final float HEAT_PITCH_HIGH = 1.35f;
    static final double PING_GAP_SECONDS = 0.08;
    static final double TICK_GAP_SECONDS = 0.08;
    static final double DOME_HIT_GAP_SECONDS = 0.15;
    /** The creak's file is 1.2 s: it starts again this often while a unit heals. */
    static final double CREAK_SECONDS = 1.2;
    /** The serials of the mirrors watched for heating at once. */
    private static final int WATCHED = 16;

    private long now;
    private long lastPing = Long.MIN_VALUE / 2;
    private long lastTick = Long.MIN_VALUE / 2;
    private long lastDomeHit = Long.MIN_VALUE / 2;
    private long lastCreak = Long.MIN_VALUE / 2;
    private float heatLevel;
    private final int[] heatSerial = new int[WATCHED];
    private final double[] lastHeat = new double[WATCHED];
    /** Whether each watched serial was seen in this watch (a slot not seen is freed: its unit is gone). */
    private final boolean[] seen = new boolean[WATCHED];

    JovianCues(SoundMix mix) {
        super(mix);
        java.util.Arrays.fill(heatSerial, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    /** Whether a throttled cue last played at step {@code last} may play at {@code now}. */
    static boolean due(long last, long now, double gapSeconds) {
        return now - last >= SimStep.ticks(gapSeconds) || now < last;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case MIRROR_BOUNCED -> {
                if (due(lastPing, now, PING_GAP_SECONDS)) {
                    lastPing = now;
                    bank.play(Sfx.MIRROR_PING, CUE_LEVEL, pitch(0.04), pan);
                }
            }
            case MIRROR_ABSORBED -> {
                if (due(lastTick, now, TICK_GAP_SECONDS)) {
                    lastTick = now;
                    bank.play(ABSORBED_STAND_IN, TICK_LEVEL, 1.2f * pitch(0.05), pan);
                }
            }
            case MIRROR_BROKEN -> bank.play(BREAK_STAND_IN, EXPLOSIONS, 0.85f * pitch(0.03), pan);
            case MIRROR_REGROWING -> bank.play(REGROW_STAND_IN, REGROW_LEVEL, REGROW_PITCH, pan);
            case ROOT_EXPOSED -> {
                if (!killedHere(events, i)) {
                    bank.play(alternate(Sfx.EXPLOSION_SMALL_A, Sfx.EXPLOSION_SMALL_B), EXPLOSIONS, pitch(0.04), pan);
                }
            }
            case ROOT_KILLED -> bank.play(ROOT_STAND_IN, EXPLOSIONS, 0.85f * pitch(0.04), pan);
            case KNITTING -> bank.play(Sfx.GRAFT_REGROWTH, CUE_LEVEL, pitch(0.03), pan);
            case DOME_HIT -> {
                if (due(lastDomeHit, now, DOME_HIT_GAP_SECONDS)) {
                    lastDomeHit = now;
                    bank.play(DOME_HIT_STAND_IN, HITS, pitch(0.05), pan);
                }
            }
            case DOME_DOWN, DOME_COLLAPSED -> bank.play(Sfx.PYLON_DOME_DOWN, CUE_LEVEL, pitch(0.02), pan);
            case DOME_UP -> bank.play(Sfx.PYLON_DOME_UP, CUE_LEVEL, pitch(0.02), pan);
            default -> {}
        }
    }

    /**
     * Whether event {@code i}'s regrowing unit was killed (its paid first death) in the same step: an {@code
     * ENEMY_DESTROYED} of its kind at its place before it, whose rung plays the shell's burst.
     */
    static boolean killedHere(SimEvents events, int i) {
        for (int e = 0; e < i; e++) {
            if (events.type(e) == SimEvents.Type.ENEMY_DESTROYED
                    && events.value(e) == events.value(i)
                    && events.x(e) == events.x(i)
                    && events.y(e) == events.y(i)) {
                return true;
            }
        }
        return false;
    }

    /** The heat loop's pitch at {@code share} of a mirror's heat. */
    static float heatPitch(double share) {
        return (float) (HEAT_PITCH_LOW + (HEAT_PITCH_HIGH - HEAT_PITCH_LOW) * Math.clamp(share, 0, 1));
    }

    /** Once a step: the healing creak while any unit heals, the heat loop while a beam heats a mirror. */
    void watch(Sortie sortie) {
        long tick = sortie.tick();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Enemy healer = null;
        Enemy heating = null;
        double healerDistance = Double.POSITIVE_INFINITY;
        double heatingDistance = Double.POSITIVE_INFINITY;
        java.util.Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (enemy.spec().regeneration().isPresent() && enemy.regeneration().healing() && d < healerDistance) {
                healer = enemy;
                healerDistance = d;
            }
            if (enemy.spec().mirror().isPresent() && heated(enemy) && d < heatingDistance) {
                heating = enemy;
                heatingDistance = d;
            }
        }
        for (int w = 0; w < WATCHED; w++) {
            if (!seen[w]) {
                heatSerial[w] = -1;
            }
        }
        if (healer != null && due(lastCreak, tick, CREAK_SECONDS)) {
            lastCreak = tick;
            bank.play(Sfx.REGROWTH_CREAK, CUE_LEVEL, pitch(0.03), pan(healer.renderX(1)));
        }
        float pan = heating == null ? 0 : pan(heating.renderX(1));
        heatLevel = fadeLoop(HEAT_STAND_IN, heatLevel, heating == null ? 0 : UNIT_LOOP_LEVEL, pan);
        if (heating != null && heatLevel > 0) {
            bank.loopPitch(
                    HEAT_STAND_IN,
                    heatPitch(heating.mirror().heat()
                            / heating.spec().mirror().get().heat()));
        }
    }

    /** Whether a mirrored unit's heat rose since the last watch (a beam on its face); notes its heat now. */
    private boolean heated(Enemy enemy) {
        double heat = enemy.mirror().heat();
        int free = -1;
        for (int w = 0; w < WATCHED; w++) {
            if (heatSerial[w] == enemy.serial()) {
                seen[w] = true;
                boolean rose = heat > lastHeat[w];
                lastHeat[w] = heat;
                if (heat <= 0) {
                    heatSerial[w] = -1;
                }
                return rose;
            }
            if (free < 0 && heatSerial[w] < 0) {
                free = w;
            }
        }
        if (heat > 0 && free >= 0) {
            seen[free] = true;
            heatSerial[free] = enemy.serial();
            lastHeat[free] = heat;
            return true;
        }
        return false;
    }
}
