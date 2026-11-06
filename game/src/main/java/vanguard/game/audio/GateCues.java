package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.VrellLevel;

/**
 * M11 part A, step A3a: the sounds of Act 7's unit mechanics for {@link FlightSounds} (design/audio/sfx, round 84's
 * picks levelled in their files; design/enemies, Window shields and the {@code teleport} movement):
 *
 * <ul>
 *   <li>a window shell opening and closing (its line's window, a breaker's or the EMP's hold, the regrown shell): the
 *       slow stone grind ({@link Sfx#SHELL_GRIND}); a shot spent on the closed shell: the dull stone tick ({@link
 *       Sfx#SHELL_TICK}, which the simulation raises at most once every 0.15 s a unit; here once in {@value
 *       #TICK_GAP_SECONDS} s over all of them, under the fire), never the hex shield's chime;
 *   <li>a teleporting unit's jump away ({@link Sfx#JUMP_OUT}) and its landing at its ring's end ({@link Sfx#ARRIVE}).
 * </ul>
 *
 * <p>M11 part B, step B4 (design/audio/sfx, Act 7 sounds; Levels 43 and 44): the Gate Warden's line, its telegraph
 * (round 84 S.5 b, {@link Sfx#WARDEN_TELEGRAPH}) and its beam (S.6 a, {@link Sfx#WARDEN_BEAM}) in place of the rail's
 * ({@link LeechFieldCues} hands a gate unit's {@code LINE_TELEGRAPH} and {@code LINE_FIRED} here: {@link #line}), its
 * death's rockfall over the {@code medium} rung (S.7 a, {@link #destroyed}); a Rift Skater's burst once a burst at its
 * first shot (S.8 a, {@link #fired}); the gate transit's roar (S.34 a) timed to end with the level inside the warp
 * ({@link #transitDelaySteps}); each reversal's groan (S.37 a) over its run, cut to the run with a {@value
 * #GROAN_FADE_SECONDS} s fade ({@link #groanLevel}; the theme ducks under it: {@link #reversalRun}).
 */
public final class GateCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.SHELL_OPEN,
            SimEvents.Type.SHELL_CLOSE,
            SimEvents.Type.SHELL_TICK,
            SimEvents.Type.JUMP_OUT,
            SimEvents.Type.ARRIVE,
            SimEvents.Type.TRANSIT_FORM,
            SimEvents.Type.REVERSAL_RUN);

    /** The cues are levelled in their files and play as they are; the tick sits under the fire. */
    static final float CUE_LEVEL = 1f;

    static final float TICK_LEVEL = 0.7f * HITS;
    /** The shells' ticks play at most once in this long, all units together. */
    static final double TICK_GAP_SECONDS = 0.05;

    static final String WARDEN = "gate-warden";
    static final String SKATER = "rift-skater";
    /** The Skater's burst file holds the three snaps (0.3 s): one play a burst, none again within this, s. */
    static final double BURST_GAP_SECONDS = 0.4;
    /** The transit's roar, s (its file: the rise into the held pressure). */
    static final double TRANSIT_SECONDS = 3.5;
    /** A reversal's groan fades out over this at its run's end, s. */
    static final double GROAN_FADE_SECONDS = 0.5;

    private long now;
    private long lastTick = Long.MIN_VALUE / 2;
    private long lastBurst = Long.MIN_VALUE / 2;
    private boolean[] wardens;
    private boolean[] skaters;
    /** The groan's run: its start step (-1: none) and its length in steps. */
    private long groanStart = -1;

    private int groanSteps;

    GateCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            lastBurst = Long.MIN_VALUE / 2;
            groanStart = -1;
        }
        this.now = now;
    }

    private void noted() {
        if (wardens == null && mix.watched != null) {
            List<EnemySpec> kinds = mix.watched.enemyKinds();
            wardens = new boolean[kinds.size()];
            skaters = new boolean[kinds.size()];
            for (int k = 0; k < kinds.size(); k++) {
                wardens[k] = kinds.get(k).slug().equals(WARDEN);
                skaters[k] = kinds.get(k).slug().equals(SKATER);
            }
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case SHELL_OPEN, SHELL_CLOSE -> bank.play(Sfx.SHELL_GRIND, CUE_LEVEL, pitch(0.04), pan);
            case SHELL_TICK -> {
                if (JovianCues.due(lastTick, now, TICK_GAP_SECONDS)) {
                    lastTick = now;
                    bank.play(Sfx.SHELL_TICK, TICK_LEVEL, pitch(0.06), pan);
                }
            }
            case JUMP_OUT -> bank.play(Sfx.JUMP_OUT, CUE_LEVEL, pitch(0.04), pan);
            case ARRIVE -> bank.play(Sfx.ARRIVE, CUE_LEVEL, pitch(0.04), pan);
            case TRANSIT_FORM -> {
                Sortie sortie = mix.watched;
                if (sortie != null) {
                    sortie.script()
                            .vrell()
                            .transit()
                            .ifPresent(transit -> later(Sfx.GATE_TRANSIT, CUE_LEVEL, 1, 0, transitDelaySteps(transit)));
                }
            }
            case REVERSAL_RUN -> {
                Sortie sortie = mix.watched;
                if (sortie != null) {
                    groanStart = now;
                    groanSteps = SimStep.ticks(reversalRun(sortie, events.value(i), 0));
                }
            }
            default -> {}
        }
    }

    /**
     * A gate unit's laser line (from {@link LeechFieldCues}, which owns the events): its telegraph or its beam; false
     * for any other unit's line (the rail's).
     */
    boolean line(SimEvents.Type type, int kind, float pan) {
        noted();
        if (!is(wardens, kind)) {
            return false;
        }
        bank.play(type == SimEvents.Type.LINE_TELEGRAPH ? Sfx.WARDEN_TELEGRAPH : Sfx.WARDEN_BEAM, CUE_LEVEL, 1, pan);
        return true;
    }

    /** A unit of {@code kind} was destroyed (UnitCues, over its rung): a Warden's rockfall. */
    void destroyed(int kind, float pan) {
        noted();
        if (is(wardens, kind)) {
            bank.play(Sfx.WARDEN_DEATH, EXPLOSIONS, pitch(0.04), pan);
        }
    }

    /** An enemy fired (UnitCues): a Skater's burst once a burst (true), else false. */
    boolean fired(Enemy unit, float pan) {
        noted();
        if (unit == null || !is(skaters, unit.kind())) {
            return false;
        }
        if (JovianCues.due(lastBurst, now, BURST_GAP_SECONDS)) {
            lastBurst = now;
            bank.play(Sfx.SKATER_BURST, CUE_LEVEL, pitch(0.05), pan);
        }
        return true;
    }

    /**
     * The steps from the transit's formation to its roar: it starts {@value #TRANSIT_SECONDS} s before the warp's end
     * (the level's end inside it), so its held pressure fills the warp.
     */
    static int transitDelaySteps(VrellLevel.Transit transit) {
        return Math.max(0, SimStep.ticks(transit.warp() + transit.length() - TRANSIT_SECONDS - transit.form()));
    }

    /**
     * Reversal {@code index}'s own run, s (Level 44: 4 s, then 5 s; easy 3 s): the groan's length and the theme's duck;
     * {@code otherwise} without the level's current.
     */
    public static double reversalRun(Sortie sortie, int index, double otherwise) {
        return sortie.script()
                .hazards()
                .current()
                .map(HazardSpec.Current::reversals)
                .filter(reversals -> index >= 0 && index < reversals.size())
                .map(reversals -> reversals.get(index).runSeconds())
                .orElse(otherwise);
    }

    /**
     * The groan's level {@code steps} into a run of {@code runSteps}: full, fading over the last {@value
     * #GROAN_FADE_SECONDS} s to none at the run's end.
     */
    static float groanLevel(long steps, int runSteps) {
        if (steps < 0 || steps >= runSteps) {
            return 0;
        }
        double left = (runSteps - steps) / (double) SimStep.PER_SECOND;
        return (float) Math.min(1, left / GROAN_FADE_SECONDS);
    }

    /** Once a step: the groan follows its run (a loop of the one-shot's file, cut at the run's end). */
    void watch(Sortie sortie) {
        if (groanStart < 0) {
            return;
        }
        boolean flying = sortie.flying() && !sortie.complete();
        float level = flying ? groanLevel(now - groanStart, groanSteps) : 0;
        if (level <= 0) {
            groanStart = -1;
            if (bank.looping(Sfx.REVERSAL_GROAN)) {
                bank.stop(Sfx.REVERSAL_GROAN);
            }
            return;
        }
        if (!bank.looping(Sfx.REVERSAL_GROAN)) {
            bank.loop(Sfx.REVERSAL_GROAN, level);
        } else {
            bank.loopVolume(Sfx.REVERSAL_GROAN, level);
        }
    }

    /** The flight's loops stop: the groan with them. */
    void silence(Sfx loop) {
        if (loop == Sfx.REVERSAL_GROAN) {
            groanStart = -1;
        }
    }
}
