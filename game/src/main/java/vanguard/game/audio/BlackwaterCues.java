package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M8 part F, Level 26's sounds for {@link FlightSounds} (design/audio/sfx, Act 4 rows; round 55's a variants until the
 * user's pick): the Glow Angler's lure hum, one quiet loop while an Angler is on the screen, panned to the nearest and
 * louder as the ship comes close; its lunge ({@code LURE_LUNGE}; the ring of 8 is the enemy small shot); in a dark level
 * the Spiral Nautilus's crown hum, one quiet loop while a Nautilus is on the screen (with its shell spin, {@link
 * TrenchCues}); Rook's loss: the torpedo trails rising ({@code ROOK_TRAILS}), the three hits and his lamp's flicker and
 * spin-down ({@code ROOK_LOST}); and in a dark level the headlight click as the ship's headlights come on at the start
 * and as the last lamps go out behind it ({@link #lampsOut}).
 */
final class BlackwaterCues extends SoundFamily {
    /** The units with sounds of their own, by their slugs. */
    static final String ANGLER = "glow-angler";

    static final String NAUTILUS = TrenchCues.NAUTILUS;
    /** The lure hum's level: {@link FlightSounds#UNIT_LOOP_LEVEL} within this many px of the ship. */
    static final double NEAR = 80;
    /** ... falling to {@link #FAR_LEVEL} of it at this many px and beyond. */
    static final double FAR = 400;

    static final float FAR_LEVEL = 0.35f;
    /** The one-shots sit at their files' levels (levelled beside round 46/47's imports). */
    static final float CUE_LEVEL = 1f;

    /** Per enemy kind (from the first note): a Glow Angler, a Spiral Nautilus. */
    private boolean[] anglers;

    private boolean[] nautili;
    /** The loops' levels now (their fades). */
    private float lureLevel;

    private float crownLevel;
    /** Whether the level is dark (its crown hum and headlight clicks); the lamps' going out, level s (NaN: none). */
    private boolean dark;

    private double lampsOut = Double.NaN;
    /** Whether the headlights' click has played in this attempt; the level clock at the last watch. */
    private boolean lit;

    private double lastSeconds = Double.NEGATIVE_INFINITY;

    BlackwaterCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(SimEvents.Type.LURE_LUNGE, SimEvents.Type.ROOK_TRAILS, SimEvents.Type.ROOK_LOST);
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (mix.resync) {
            return;
        }
        switch (events.type(i)) {
            case LURE_LUNGE -> bank.play(Sfx.ANGLER_LUNGE, CUE_LEVEL, pitch(0.04), pan);
            case ROOK_TRAILS -> bank.play(Sfx.TORPEDO_TRAILS, CUE_LEVEL, 1, pan);
            case ROOK_LOST -> {
                bank.play(Sfx.ROOK_HIT, EXPLOSIONS, 1, pan);
                bank.play(Sfx.ROOK_SPIN, CUE_LEVEL, 1, pan);
            }
            default -> {}
        }
    }

    /**
     * The level is dark (its look's static lights or escort's lamp, Level 26): the crown hum plays and the headlights
     * click on at the start and as the last lamps go out at level time {@code lampsOut} (its ambient ramp's end; NaN for
     * none).
     */
    void dark(double lampsOut) {
        dark = true;
        this.lampsOut = lampsOut;
    }

    /** Notes the level's kinds: which are the Glow Angler and the Spiral Nautilus. */
    void noteKinds(List<EnemySpec> kinds) {
        anglers = new boolean[kinds.size()];
        nautili = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            anglers[k] = slug.equals(ANGLER);
            nautili[k] = slug.equals(NAUTILUS);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** The lure hum's level for the nearest Angler {@code distance} px from the ship. */
    static float lureLevel(double distance) {
        double u = Math.clamp((distance - NEAR) / (FAR - NEAR), 0, 1);
        return (float) (UNIT_LOOP_LEVEL * (1 - (1 - FAR_LEVEL) * u));
    }

    /** Once a step: the lure hum and the crown hum for the nearest on the screen; the headlights' clicks. */
    void watch(Sortie sortie) {
        if (anglers == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearestLure = Double.POSITIVE_INFINITY;
        double nearestCrown = Double.POSITIVE_INFINITY;
        float lurePan = 0;
        float crownPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            boolean angler = is(anglers, enemy.kind());
            boolean nautilus = dark && is(nautili, enemy.kind());
            if (!angler && !nautilus) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y < 0 || y > PlayField.HEIGHT || x < 0 || x > PlayField.WIDTH) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (angler && distance < nearestLure) {
                nearestLure = distance;
                lurePan = pan(x);
            } else if (nautilus && distance < nearestCrown) {
                nearestCrown = distance;
                crownPan = pan(x);
            }
        }
        float lureTarget = flying && nearestLure < Double.POSITIVE_INFINITY ? lureLevel(nearestLure) : 0;
        lureLevel = fadeLoop(Sfx.LURE_HUM, lureLevel, lureTarget, lurePan);
        float crownTarget = flying && nearestCrown < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        crownLevel = fadeLoop(Sfx.CROWN_HUM, crownLevel, crownTarget, crownPan);
        watchLamps(sortie);
    }

    /** In a dark level the headlights' click as they come on (the first flying step of an attempt) and the lamps' out. */
    private void watchLamps(Sortie sortie) {
        double now = sortie.levelSeconds();
        if (now < lastSeconds) {
            // A retry: the attempt starts over.
            lit = false;
        }
        if (dark && !mix.resync) {
            if (!lit && sortie.flying()) {
                lit = true;
                bank.play(Sfx.HEADLIGHT_CLICK, CUE_LEVEL, 1, 0);
            }
            if (lastSeconds < lampsOut && lampsOut <= now) {
                bank.play(Sfx.HEADLIGHT_CLICK, CUE_LEVEL, 0.92f, 0);
            }
        }
        lastSeconds = now;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.LURE_HUM) {
            lureLevel = 0;
        } else if (loop == Sfx.CROWN_HUM) {
            crownLevel = 0;
        }
    }

    /** The loops' levels now (for the tests). */
    float lureLevel() {
        return lureLevel;
    }

    float crownLevel() {
        return crownLevel;
    }
}
