package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.ALARM_LEVEL;
import static vanguard.game.audio.FlightSounds.BOMB_DROP_LEVEL;
import static vanguard.game.audio.FlightSounds.DOME_BREAK_LEVEL;
import static vanguard.game.audio.FlightSounds.DOME_HIT_LEVEL;
import static vanguard.game.audio.FlightSounds.DRONE_LEVEL;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.GRID_ONLINE_LEVEL;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.RELAY_DESTROYED_LEVEL;
import static vanguard.game.audio.FlightSounds.RELAY_HIT_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;

/**
 * The allies' sounds for {@link FlightSounds}: the convoy's hits and losses (trucks, M5 part E's ships,
 * M5 part G's defended relay with its alarm, grid online and supply drones, M7 part D's domes and the
 * bombers' drops over them), the frigate's and the militia's flak, Rook's hits and ejection, the
 * scripted loss's lance and a tow's cable snapping.
 */
final class AllyCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.ALLY_HIT,
            SimEvents.Type.ALLY_LOST,
            SimEvents.Type.ALLY_BELOW,
            SimEvents.Type.DEFEND_COMPLETE,
            SimEvents.Type.BOMB_DROPPED,
            SimEvents.Type.ALLY_FLAK,
            SimEvents.Type.WINGMAN_HIT,
            SimEvents.Type.WINGMAN_EJECTED);

    /** Whether the level's convoy is naval (M5 part E): ships hit and sink, not trucks. */
    private boolean naval;
    /** M5 part G: whether the level's convoy is a defend primary's ally (the relay). */
    private boolean defend;
    /** M7 part D: whether the level's convoy is ground allies (Level 17's domes): their hits and breaks are glass. */
    private boolean ground;
    /** M5 part G: the level steps the supply drones lift off at, and the level step of the last watch. */
    private long[] launchTicks = new long[0];

    private long watchedTick = -1;
    /** The level clock at the last {@link #watchLance}, for the lance's start; NaN before the first. */
    private double lastSeconds = Double.NaN;
    /** The scripted loss's hit on the level clock, from the first {@link #watchLance}; infinite without one, NaN before. */
    private double lanceHit = Double.NaN;
    /** What the last watch saw: the tows holding. */
    private boolean[] wasHolding;

    AllyCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    void naval(boolean naval) {
        this.naval = naval;
    }

    void ground(boolean ground) {
        this.ground = ground;
    }

    /** See {@link FlightSounds#defend}. */
    void defend(boolean defend, double[] dropTimes, double arcSeconds) {
        this.defend = defend;
        this.launchTicks = new long[dropTimes.length];
        for (int i = 0; i < dropTimes.length; i++) {
            launchTicks[i] = SimStep.ticks(Math.max(0, dropTimes[i] - arcSeconds));
        }
    }

    long[] launchTicks() {
        return launchTicks.clone();
    }

    /** See {@link FlightSounds#militiaFlak}. */
    void militiaFlak(double x) {
        bank.play(Sfx.FRIGATE_FLAK, 1, pitch(0.05), pan(x));
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            // A convoy unit: metal hit and a small explosion; the convoy lost plays the sting as a wreck does.
            // M5 part E: a naval convoy's ship hit by a slam, and a ship sinking from its loss (round 33 a).
            // M5 part G: the defended relay struck (round 35 a), and destroyed: its power-down.
            case ALLY_HIT -> {
                if (ground) {
                    bank.play(Sfx.DOME_HIT, DOME_HIT_LEVEL, pitch(0.04), pan);
                } else if (defend) {
                    bank.play(Sfx.RELAY_HIT, RELAY_HIT_LEVEL, pitch(0.04), pan);
                } else {
                    bank.play(
                            naval ? Sfx.SHIP_HIT : alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B),
                            naval ? EXPLOSIONS : HITS,
                            0.9f * pitch(0.05),
                            pan);
                }
            }
            case ALLY_LOST -> {
                if (ground) {
                    bank.play(Sfx.DOME_BREAK, DOME_BREAK_LEVEL, 1, pan);
                } else if (defend) {
                    bank.play(Sfx.RELAY_DESTROYED, RELAY_DESTROYED_LEVEL, 1, pan);
                } else {
                    bank.play(
                            naval ? Sfx.SHIP_SINK : alternate(Sfx.EXPLOSION_SMALL_A, Sfx.EXPLOSION_SMALL_B),
                            EXPLOSIONS,
                            1,
                            pan);
                }
            }
            // M5 part G (round 35, the user's picks): the relay's integrity alarm at 50 % and 25 %, and grid
            // online as its synchronisation completes and its lights come on.
            case ALLY_BELOW -> bank.play(Sfx.INTEGRITY_ALARM, ALARM_LEVEL, 1, 0);
            case DEFEND_COMPLETE -> bank.play(Sfx.GRID_ONLINE, GRID_ONLINE_LEVEL, 1, pan);
            // M7 part D: a hooked bomber drops its load over a dome: the Bomb Rack's falling-bomb whistle
            // (shot-bomb-r03-a, reused), at the enemy shots' level; the dome's hit follows when the load hurts it.
            case BOMB_DROPPED -> bank.play(Sfx.BOMB_SHOT, BOMB_DROP_LEVEL, 0.9f * pitch(0.04), pan);
            // M5 part E: the frigate's distant flak, quiet (the file is levelled low).
            case ALLY_FLAK -> bank.play(Sfx.FRIGATE_FLAK, 1, pitch(0.05), pan);
            // Rook (M5 part A): a metal hit on his hull, quieter than the ship's; his craft's
            // medium explosion as he ejects.
            case WINGMAN_HIT ->
                bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), 0.7f * HITS, 0.9f * pitch(0.05), pan);
            case WINGMAN_EJECTED ->
                bank.play(alternate(Sfx.EXPLOSION_MEDIUM_A, Sfx.EXPLOSION_MEDIUM_B), PLAYER_DAMAGE, 1, pan);
            default -> {}
        }
    }

    /**
     * The scripted loss's lance (M5 part D, Level 10): its sound starts {@link FlightSounds#LANCE_IMPACT_SECONDS}
     * before the hit, panned to its unit; not after a restart that skipped past it.
     */
    void watchLance(Sortie sortie) {
        if (Double.isNaN(lanceHit)) {
            lanceHit = sortie.script()
                    .escort()
                    .flatMap(vanguard.sim.LevelScript.Escort::air)
                    .flatMap(vanguard.sim.LevelScript.Air::scriptedLoss)
                    .map(vanguard.sim.LevelScript.ScriptedLoss::t)
                    .orElse(Double.POSITIVE_INFINITY);
        }
        double now = sortie.levelSeconds();
        double before = mix.resync ? now : lastSeconds;
        lastSeconds = now;
        int unit = sortie.scriptedAlly();
        if (unit >= 0 && !Double.isNaN(before) && FlightSounds.lanceDue(before, now, lanceHit)) {
            bank.play(Sfx.LANCE_STRIKE, PLAYER_DAMAGE, 1, pan(sortie.ally(unit).x()));
        }
    }

    /**
     * M5 part G: a supply drone's launch as it lifts off the relay's helipad, its arc's length before its
     * drop, while the relay stands (not after a restart that skipped past it).
     */
    void watchDrones(Sortie sortie) {
        long now = SimStep.ticks(sortie.levelSeconds());
        // M7 part D: Level 17's drone comes in from the bottom edge (no relay, its domes are no helipad).
        if (!mix.resync
                && defend
                && sortie.allyCount() > 0
                && sortie.ally(0).alive()
                && FlightSounds.launched(watchedTick, now, launchTicks)) {
            bank.play(Sfx.DRONE_LAUNCH, DRONE_LEVEL, 1, pan(sortie.ally(0).x()));
        }
        watchedTick = now;
    }

    /** At the first watch: the tows to follow. */
    void noteTows(Sortie sortie) {
        wasHolding = new boolean[sortie.towCount()];
    }

    /** A tow's cable snapping as its pod falls free. */
    void watchTows(Sortie sortie) {
        for (int i = 0; i < wasHolding.length; i++) {
            Tow tow = sortie.tow(i);
            boolean holding = tow.holding();
            if (!mix.resync && wasHolding[i] && !holding) {
                bank.play(Sfx.CABLE_SNAP, EXPLOSIONS, pitch(0.03), pan(tow.podX()));
            }
            wasHolding[i] = holding;
        }
    }
}
