package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.AirLinks;
import vanguard.sim.Ally;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part D: Level 31's sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; step AUD-D's round 64
 * proposals, their a variants until the round's picks, and round 60's picks, levelled in their files and played as they
 * are until the production mix):
 *
 * <ul>
 *   <li>the Shard Drones' air links: one hum loop while any link is lit ({@link Sfx#LINK_HUM}, panned to the nearest
 *       lit link), a group's flicker tell before its links light and as they relight after the EMP ({@link
 *       Sfx#LINK_FLICKER}, cut short when the links light first: hard's 0.5 s tell), a link broken by a drone's death
 *       ({@link Sfx#LINK_SNAP}, one per dying drone: links snapping together share it);
 *   <li>the Buzzsaw Drone: the nearest one's whine pitched with its spin ({@link Sfx#SAW_WHINE}: 0.6 chasing, rising to
 *       1.0 over the tell, 1.0 in the dash), its spin-up tell ({@link Sfx#SAW_SPINUP}, played at 0.6 s / its tell) and
 *       each bounce ({@link Sfx#SAW_RICOCHET});
 *   <li>the Hornet: its hover hum for the nearest on the screen (the Ascendancy turbine pitched up, {@link
 *       Sfx#HORNET_HUM}), its pod hatch opening over its gun's tell (round 62's SAM Nest hatch pitched up, {@link
 *       Sfx#NEST_HATCH}) and its salvo's launch (the homing diamonds' launch pitched up, {@link #launchPitch});
 *   <li>the mining tug (a hull escort): its engine bed over the belt's while its engines section lives ({@link
 *       Sfx#TUG_ENGINE}), its klaxon as a section drops below 50 % ({@link Sfx#TUG_ALARM}); its hits and losses are
 *       {@link AllyCues}' ({@link #tugHit}, {@link #tugLost}).
 * </ul>
 */
final class MiningRigCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.AIR_LINK_LIT,
            SimEvents.Type.AIR_LINK_BROKEN,
            SimEvents.Type.DASH_TELL,
            SimEvents.Type.DASH_BOUNCE);

    static final String HORNET = "hornet";
    static final String SAW = "buzzsaw-drone";

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The Buzzsaw's whine: the file's pitch is its spun-up 6 turns/s; chasing at 2 turns/s plays it at this share. */
    static final float WHINE_CHASE_PITCH = 0.6f;
    /** The spin-up file's length: played at this / the unit's tell. */
    static final double SPINUP_SECONDS = 0.6;
    /** The flicker's file length: a lit link within this of its start cuts it short. */
    static final int FLICKER_STEPS = SimStep.ticks(0.8);
    /** The Hornet's turbine, its hatch and its launch, pitched up from the Gunship's and the SAM Nest's. */
    static final float HORNET_HUM_PITCH = 1.3f;

    static final float HATCH_PITCH = 1.2f;
    static final float LAUNCH_PITCH = 1.25f;
    /** A launch within this many px of a Hornet is its. */
    static final double LAUNCH_REACH = 40;
    /** Snaps within this many steps of the last share it (a dying drone breaks up to two links at once). */
    static final int SNAP_GAP_STEPS = 2;
    /** The tug's hits sound at most once per this many steps. */
    static final int TUG_HIT_GAP_STEPS = SimStep.ticks(0.2);

    private boolean[] hornets;
    private boolean[] saws;
    private long now;
    private long lastSnap = Long.MIN_VALUE / 2;
    private long lastFlicker = Long.MIN_VALUE / 2;
    private long lastTugHit = Long.MIN_VALUE / 2;
    /** Per link group: whether its first link was in its tell at the last watch. */
    private final boolean[] telling = new boolean[AirLinks.SLOTS / AirLinks.MEMBERS];
    /** Per hull section: whether it was below 50 % at the last watch. */
    private boolean[] below = new boolean[0];
    /** Per Hornet slot: its serial and the step its hatch last sounded. */
    private final int[] hornetSerial = new int[16];

    private final long[] hatchSounded = new long[16];
    private int nextHornet;

    private float humLevel;
    private float whineLevel;
    private float hoverLevel;
    private float engineLevel;

    MiningRigCues(SoundMix mix) {
        super(mix);
        java.util.Arrays.fill(hornetSerial, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The level's kinds: which are ours. */
    void noteKinds(List<EnemySpec> kinds) {
        hornets = new boolean[kinds.size()];
        saws = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            hornets[k] = slug.equals(HORNET);
            saws[k] = slug.equals(SAW);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    private void noted() {
        if (hornets == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            lastSnap = Long.MIN_VALUE / 2;
            lastFlicker = Long.MIN_VALUE / 2;
            lastTugHit = Long.MIN_VALUE / 2;
            java.util.Arrays.fill(telling, false);
            java.util.Arrays.fill(below, false);
            java.util.Arrays.fill(hornetSerial, -1);
        }
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        switch (events.type(i)) {
            case AIR_LINK_LIT -> {
                // Hard's 0.5 s tell: the flicker is cut short as the links light.
                if (now - lastFlicker < FLICKER_STEPS && bank.playing(Sfx.LINK_FLICKER) > 0) {
                    bank.stop(Sfx.LINK_FLICKER);
                }
            }
            case AIR_LINK_BROKEN -> {
                if (events.value(i) == 0 && snapDue(lastSnap, now)) {
                    bank.play(Sfx.LINK_SNAP, CUE_LEVEL, pitch(0.05), pan);
                    lastSnap = now;
                }
            }
            case DASH_TELL -> bank.play(Sfx.SAW_SPINUP, CUE_LEVEL, spinUpPitch(tellSeconds(events.value(i))), pan);
            case DASH_BOUNCE -> bank.play(Sfx.SAW_RICOCHET, CUE_LEVEL, pitch(0.06), pan);
            default -> {}
        }
    }

    /** Whether a link's snap sounds at step {@code now} after the last at {@code last}. */
    static boolean snapDue(long last, long now) {
        return now - last >= SNAP_GAP_STEPS || now < last;
    }

    /** The spin-up's pitch for a tell of {@code tellSeconds}: the 0.6 s file stretched or squeezed to it. */
    static float spinUpPitch(double tellSeconds) {
        return tellSeconds > 0 ? (float) (SPINUP_SECONDS / tellSeconds) : 1;
    }

    /** The whine's pitch: chasing, rising over the tell, spun up in the dash. */
    static float whinePitch(boolean dashing, boolean telling, double tellShare) {
        if (dashing) {
            return 1;
        }
        if (telling) {
            return (float) (WHINE_CHASE_PITCH + (1 - WHINE_CHASE_PITCH) * Math.clamp(tellShare, 0, 1));
        }
        return WHINE_CHASE_PITCH;
    }

    private double tellSeconds(int kind) {
        if (mix.watched == null || kind < 0 || kind >= mix.watched.enemyKinds().size()) {
            return SPINUP_SECONDS;
        }
        return mix.watched
                .enemyKinds()
                .get(kind)
                .dash()
                .map(dash -> dash.tellSeconds())
                .orElse(SPINUP_SECONDS);
    }

    /**
     * A homing missile was launched at (x, y): a Hornet's salvo plays the homing diamonds' launch at this pitch, any
     * other launcher's at its own (1).
     */
    float launchPitch(double x, double y) {
        noted();
        Sortie sortie = mix.watched;
        if (sortie == null || hornets == null) {
            return 1;
        }
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (is(hornets, enemy.kind()) && Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y) <= LAUNCH_REACH) {
                return LAUNCH_PITCH;
            }
        }
        return 1;
    }

    /** A hull escort's section was hit: the heavy hull impact, at most once per 0.2 s. */
    void tugHit(float pan) {
        if (now - lastTugHit >= TUG_HIT_GAP_STEPS || now < lastTugHit) {
            bank.play(Sfx.TUG_HIT, CUE_LEVEL, pitch(0.04), pan);
            lastTugHit = now;
        }
    }

    /** A hull escort's section was lost: the loss over the klaxon. */
    void tugLost(float pan) {
        bank.play(Sfx.TUG_ALARM, CUE_LEVEL, 1, 0);
        bank.play(Sfx.TUG_LOST, CUE_LEVEL, 1, pan);
    }

    /**
     * Once a step: the link hum, the flicker tells, the nearest Buzzsaw's whine and Hornet's hum, a Hornet's hatch, the
     * tug's engine bed and its sections' alarm below 50 %.
     */
    void watch(Sortie sortie) {
        if (hornets == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        watchLinks(sortie, flying, shipX, shipY);
        double whine = Double.POSITIVE_INFINITY;
        double hover = Double.POSITIVE_INFINITY;
        float whinePan = 0;
        float hoverPan = 0;
        float sawPitch = WHINE_CHASE_PITCH;
        for (int e = 0; flying && e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            int kind = enemy.kind();
            if (!is(saws, kind) && !is(hornets, kind)) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (!MinefieldCues.onScreen(x, y)) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (is(saws, kind) && distance < whine) {
                whine = distance;
                whinePan = pan(x);
                sawPitch = whinePitch(enemy.dashing(), enemy.dashTelling(), enemy.dashTellShare());
            } else if (is(hornets, kind)) {
                hatch(enemy, x);
                if (distance < hover) {
                    hover = distance;
                    hoverPan = pan(x);
                }
            }
        }
        float on = FlightSounds.UNIT_LOOP_LEVEL;
        whineLevel = fadeLoop(
                Sfx.SAW_WHINE, whineLevel, whine < Double.POSITIVE_INFINITY ? on : 0, whinePan, WHINE_CHASE_PITCH);
        if (whineLevel > 0) {
            bank.loopPitch(Sfx.SAW_WHINE, sawPitch);
        }
        hoverLevel = fadeLoop(
                Sfx.HORNET_HUM, hoverLevel, hover < Double.POSITIVE_INFINITY ? on : 0, hoverPan, HORNET_HUM_PITCH);
        watchTug(sortie, flying);
    }

    /** A Hornet's pod hatch opening as its gun's tell starts (its glow's first step). */
    private void hatch(Enemy hornet, double x) {
        if (hornet.gunTell() <= 0 || hornet.empStunned()) {
            return;
        }
        int slot = -1;
        for (int s = 0; s < hornetSerial.length; s++) {
            if (hornetSerial[s] == hornet.serial()) {
                slot = s;
            }
        }
        if (slot < 0) {
            slot = nextHornet;
            nextHornet = (nextHornet + 1) % hornetSerial.length;
            hornetSerial[slot] = hornet.serial();
            hatchSounded[slot] = Long.MIN_VALUE / 2;
        }
        // One hatch per tell: a tell lasts 0.5 s, a salvo's interval is seconds.
        if (now - hatchSounded[slot] > SimStep.ticks(1) || now < hatchSounded[slot]) {
            bank.play(Sfx.NEST_HATCH, CUE_LEVEL, HATCH_PITCH * pitch(0.03), pan(x));
            hatchSounded[slot] = now;
        }
    }

    /** The link hum while any link is lit (panned to the nearest), a group's flicker as its tell starts. */
    private void watchLinks(Sortie sortie, boolean flying, double shipX, double shipY) {
        AirLinks links = sortie.airLinks();
        double nearest = Double.POSITIVE_INFINITY;
        float humPan = 0;
        if (links != null && flying) {
            for (int g = 0; g < telling.length; g++) {
                int first = g * AirLinks.MEMBERS;
                boolean tell = links.shown(first) && !links.lit(first) && links.tell(first) < 1;
                if (tell && !telling[g]) {
                    double mx = (links.ax(first) + links.bx(first)) / 2;
                    bank.play(Sfx.LINK_FLICKER, CUE_LEVEL, pitch(0.03), pan(mx));
                    lastFlicker = now;
                }
                telling[g] = tell;
            }
            for (int slot = 0; slot < AirLinks.SLOTS; slot++) {
                if (!links.shown(slot) || !links.lit(slot)) {
                    continue;
                }
                double mx = (links.ax(slot) + links.bx(slot)) / 2;
                double my = (links.ay(slot) + links.by(slot)) / 2;
                double distance = Math.hypot(mx - shipX, my - shipY);
                if (distance < nearest) {
                    nearest = distance;
                    humPan = pan(mx);
                }
            }
        }
        humLevel = fadeLoop(
                Sfx.LINK_HUM, humLevel, nearest < Double.POSITIVE_INFINITY ? FlightSounds.UNIT_LOOP_LEVEL : 0, humPan);
    }

    /** The tug's engine bed while its engines live and it flies; a section's klaxon as it drops below 50 %. */
    private void watchTug(Sortie sortie, boolean flying) {
        boolean engine = false;
        if (sortie.hullEscort()) {
            int count = sortie.allyCount();
            if (below.length != count) {
                below = new boolean[count];
            }
            for (int k = 0; k < count; k++) {
                Ally section = sortie.ally(k);
                boolean low = section.alive() && section.hpShare() < 0.5;
                if (low && !below[k] && !mix.resync) {
                    bank.play(Sfx.TUG_ALARM, CUE_LEVEL, 1, 0);
                }
                below[k] = low;
                if (sortie.allyName(k).equals("engines")) {
                    Ally.State state = section.state();
                    engine = section.alive()
                            && (state == Ally.State.LIFTING
                                    || state == Ally.State.FLYING
                                    || state == Ally.State.CLIMBING);
                }
            }
        }
        float pan = sortie.hullEscort() ? pan(sortie.hullX(1)) : 0;
        engineLevel = fadeLoop(Sfx.TUG_ENGINE, engineLevel, flying && engine ? FlightSounds.UNIT_LOOP_LEVEL : 0, pan);
    }

    /** A loop stopped from outside (the level screen closing): its level is none. */
    void silence(Sfx loop) {
        switch (loop) {
            case LINK_HUM -> humLevel = 0;
            case SAW_WHINE -> whineLevel = 0;
            case HORNET_HUM -> hoverLevel = 0;
            case TUG_ENGINE -> engineLevel = 0;
            default -> {}
        }
    }
}
