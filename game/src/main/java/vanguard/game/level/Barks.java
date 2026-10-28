package vanguard.game.level;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import vanguard.content.WingmenData;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * Rook's radio barks (design/player/wingmen, Radio barks): eight triggers in priority order, each
 * with its line variants, queued on the {@link RadioQueue} as event lines (they wait for a gap, go
 * stale after 6 s and never push a timed line), except his eject bark.
 *
 * <ul>
 *   <li><b>Spacing:</b> a bark is dropped when it fires less than {@code spacing} (8 s) after the
 *       start of any Rook line on the radio (a bark or a scripted line), or within that time of a
 *       timed Rook line of the level script: the scripted line says it.
 *   <li><b>The eject bark</b> is never dropped: it ignores the spacing and the scripted lines, cuts
 *       a bark of his that waits or plays, and queues as an urgent line, so it plays at once (an
 *       urgent line already on the radio finishes first) and nothing replaces it.
 *   <li><b>Priority:</b> only one bark waits at a time; one of a higher priority (earlier in the
 *       data) replaces the waiting one, one of the same or a lower priority is dropped.
 *   <li><b>Variants:</b> the <i>n</i>-th bark of a trigger in an attempt (from 0) says variant
 *       (level number + <i>n</i>) mod the trigger's count, so levels open on different lines and a
 *       retry replays the same ones.
 *   <li>After he ejects his low-armour bark can no longer fire; the others still do (he watches the
 *       scope from his pod).
 * </ul>
 *
 * The triggers come from the level's events (a boss's arrival, his low armour and ejection, kills),
 * from the level script (a rear wave 1.5 s before it enters, a sides wave as it enters; M5 part D: a
 * re-entry at the bottom edge 1.5 s before it, {@link #reentry}) and from the
 * state the screen reads each step (the player's armour, overdrive pickups on the field). The waves'
 * barks go by the level's clock (script time), so a retry plays the same barks; the spacing and the
 * streak's window are measured in real seconds (M5 part C: in a hold zone the level clock slows to a
 * fifth, see {@link #clock}), and a timed Rook line that requires the escort ({@code requires:
 * escort}) counts only while he flies. Nothing here allocates per step except a queued bark.
 *
 * <p>M7 part G (design/campaign Level 20, Reverse scroll; design/player/wingmen, Radio barks): in a reversed section
 * "behind" is the top of the screen. A rear wave whose time falls in reversed travel enters at the top and its bark
 * says one of the rear-wave bark's {@code top_lines} ("Up top!"); a re-entry from the top (a TOP warning: a Wraith, a
 * swarm's loop-back) too ({@link #reentry(int, int, boolean)}). The waves from ahead, which enter at the bottom edge
 * there, stay silent, the lessons' BOTTOM warnings included (the screen does not report them).
 */
public final class Barks {
    /** The triggers, by their id in design/player/wingmen/data.yaml. */
    public enum Trigger {
        BOSS_WARNING("boss-warning"),
        REAR_WAVE("rear-wave"),
        SIDES_WAVE("sides-wave"),
        PLAYER_ARMOUR("player-armour"),
        ROOK_ARMOUR(WingmenData.ROOK_ARMOUR),
        ROOK_EJECTS("rook-ejects"),
        KILL_STREAK("kill-streak"),
        OVERDRIVE("overdrive");

        private final String id;

        Trigger(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    /**
     * A bark to queue.
     *
     * @param expression the portrait expression's slug ({@code grim})
     * @param shout whether it is shouted (its voice file is the shout row's)
     */
    public record Line(Trigger trigger, String speaker, String expression, boolean shout, String text) {
        /** How it queues: the eject bark is urgent, the others are event lines. */
        public RadioQueue.Priority priority() {
            return trigger == Trigger.ROOK_EJECTS ? RadioQueue.Priority.URGENT : RadioQueue.Priority.EVENT;
        }
    }

    private static final double REAR_AHEAD_SECONDS = 1.5;
    /**
     * M5 part D: the bottom edge's warning ahead of a re-entry (a Wraith's rear ambush, a swarm's or a
     * chain's loop-back) runs this long (the sim's 3 s edge warning); the rear bark fires {@link
     * #REAR_AHEAD_SECONDS} before its end.
     */
    static final double REENTRY_WARNING_SECONDS = 3;
    /** The re-entry barks waiting at once, at most. */
    private static final int REENTRIES = 8;

    private final WingmenData.Barks data;
    private final int level;
    private final RadioQueue radio;
    private final Function<Line, RadioQueue.Message> queue;
    /** Per trigger its bark (null when the data has none) and its priority (0 the highest). */
    private final WingmenData.Bark[] barks = new WingmenData.Bark[Trigger.values().length];

    private final int[] priorities = new int[Trigger.values().length];
    /** The level times (s, script time) of the timed Rook lines of the level script. */
    private final double[] timedRook;
    /** Per timed Rook line whether it plays only while he flies ({@code requires: escort}). */
    private final boolean[] timedEscortOnly;
    /** The steps at which a rear wave's and a sides wave's bark fires, sorted. */
    private final int[] rearTicks;
    /** M7 part G: per rear wave of {@link #rearTicks} whether it enters in reversed travel, from the top. */
    private final boolean[] rearTop;

    private final int[] sidesTicks;
    /** M5 part D: the steps at which a re-entry's rear bark is due, {@link #reentryCount} of them. */
    private final int[] reentryTicks = new int[REENTRIES];
    /** M7 part G: per re-entry of {@link #reentryTicks} whether it comes from the top (reversed travel). */
    private final boolean[] reentryTop = new boolean[REENTRIES];

    private int reentryCount;
    /** The steps between a re-entry's warning start and its bark. */
    private final int reentryLead;
    /** The steps of a rear wave's warning: a bottom warning starting this soon before a rear wave's bark is that wave's. */
    private final int rearWarningTicks;

    private final int streakKills;
    private final double streakSeconds;
    private final double playerBelow;

    private final int[] counts = new int[Trigger.values().length];
    private double lastRookStart;
    private RadioQueue.Message waiting;
    private int waitingPriority;
    private boolean ejected;
    /**
     * M8 part F: his loss's trails have started (Level 26's {@code rook_loss}, {@link vanguard.sim.Wingman#onRadio}):
     * no bark of his fires for the rest of the attempt.
     */
    private boolean lost;
    /** The level is won: the outro's radio is the closing lines', so no bark fires (see {@link #levelComplete}). */
    private boolean over;
    /**
     * M9 part B (Level 29, round 62): he is not yet in his slot (the rescued Rook in the tender's hold or flying into
     * the slot, {@link #inSlot}): no bark fires and no kill counts for a streak.
     */
    private boolean benched;
    /** The player's armour latch: below the share since the last bark; null before the first step. */
    private Boolean playerLow;

    private int overdrives;
    private int lastTick;
    private final Deque<Double> kills = new ArrayDeque<>();
    /** The level clock at the last {@link #clock} (script seconds), NaN before: script time is real time. */
    private double scriptNow = Double.NaN;
    /** The real seconds at the last {@link #clock}. */
    private double realNow;
    /** The level clock's rate at the last {@link #clock}, script seconds per real second. */
    private double rate = 1;

    /**
     * @param level the level number, which picks the first variant
     * @param timedRook the level times of the script's timed Rook lines
     * @param timedEscortOnly per timed Rook line whether it requires the escort ({@code requires:
     *     escort}): it no longer counts once he ejected
     * @param rearWaves the level times at which rear waves enter
     * @param sidesWaves the level times at which sides waves enter
     * @param queue queues a bark as an event line on {@code radio} and returns its message
     */
    public Barks(
            WingmenData.Barks data,
            int level,
            double[] timedRook,
            boolean[] timedEscortOnly,
            double[] rearWaves,
            double[] sidesWaves,
            RadioQueue radio,
            Function<Line, RadioQueue.Message> queue) {
        this(
                data,
                level,
                timedRook,
                timedEscortOnly,
                rearWaves,
                new boolean[rearWaves.length],
                sidesWaves,
                radio,
                queue);
    }

    /**
     * As above, M7 part G: {@code rearWavesTop} per rear wave whether it enters in reversed travel, at the top
     * edge: its bark says one of the {@code top_lines}.
     */
    public Barks(
            WingmenData.Barks data,
            int level,
            double[] timedRook,
            boolean[] timedEscortOnly,
            double[] rearWaves,
            boolean[] rearWavesTop,
            double[] sidesWaves,
            RadioQueue radio,
            Function<Line, RadioQueue.Message> queue) {
        if (timedEscortOnly.length != timedRook.length) {
            throw new IllegalArgumentException("one escort flag per timed Rook line");
        }
        if (rearWavesTop.length != rearWaves.length) {
            throw new IllegalArgumentException("one top flag per rear wave");
        }
        this.data = data;
        this.level = level;
        this.radio = radio;
        this.queue = queue;
        for (Trigger trigger : Trigger.values()) {
            Optional<WingmenData.Bark> bark = data.bark(trigger.id());
            barks[trigger.ordinal()] = bark.orElse(null);
            priorities[trigger.ordinal()] = bark.map(data.triggers()::indexOf).orElse(Integer.MAX_VALUE);
        }
        this.timedRook = timedRook.clone();
        this.timedEscortOnly = timedEscortOnly.clone();
        double ahead = bark(Trigger.REAR_WAVE).flatMap(WingmenData.Bark::ahead).orElse(REAR_AHEAD_SECONDS);
        Integer[] order = new Integer[rearWaves.length];
        Arrays.setAll(order, i -> i);
        Arrays.sort(order, java.util.Comparator.comparingDouble(i -> rearWaves[i]));
        rearTicks = new int[rearWaves.length];
        rearTop = new boolean[rearWaves.length];
        for (int k = 0; k < order.length; k++) {
            rearTicks[k] = SimStep.ticks(Math.max(0, rearWaves[order[k]] - ahead));
            rearTop[k] = rearWavesTop[order[k]];
        }
        sidesTicks = Arrays.stream(sidesWaves).mapToInt(SimStep::ticks).sorted().toArray();
        reentryLead = SimStep.ticks(Math.max(0, REENTRY_WARNING_SECONDS - ahead));
        rearWarningTicks = SimStep.ticks(REENTRY_WARNING_SECONDS);
        WingmenData.Bark streak = barks[Trigger.KILL_STREAK.ordinal()];
        streakKills = streak == null ? Integer.MAX_VALUE : streak.kills().orElse(Integer.MAX_VALUE);
        streakSeconds = streak == null ? 0 : streak.seconds().orElse(0.0);
        playerBelow =
                bark(Trigger.PLAYER_ARMOUR).flatMap(WingmenData.Bark::below).orElse(0.0);
        reset(0);
    }

    /** Without a level clock of its own: script time is real time. */
    public Barks(
            WingmenData.Barks data,
            int level,
            double[] timedRook,
            double[] rearWaves,
            double[] sidesWaves,
            RadioQueue radio,
            Function<Line, RadioQueue.Message> queue) {
        this(data, level, timedRook, new boolean[timedRook.length], rearWaves, sidesWaves, radio, queue);
    }

    /**
     * Rook's barks in a level: the script's timed Rook lines (those that play with what is fitted) and
     * its rear and sides waves.
     */
    public static Barks of(
            WingmenData.Barks data,
            LevelScript script,
            boolean specialFitted,
            RadioQueue radio,
            Function<Line, RadioQueue.Message> queue) {
        return of(data, script, specialFitted ? RadioCue.FITTED_SPECIAL : 0, radio, queue);
    }

    /**
     * As above with what is fitted as the radio's bits ({@link RadioSchedule#fitted}); the lines that
     * require the escort count while he flies.
     */
    public static Barks of(
            WingmenData.Barks data,
            LevelScript script,
            int fitted,
            RadioQueue radio,
            Function<Line, RadioQueue.Message> queue) {
        List<RadioCue> rook = script.radio().stream()
                .filter(cue -> cue.trigger() == CueTrigger.TIME)
                .filter(cue -> cue.speaker().equals(data.speaker()))
                .filter(cue -> cue.allowedWith(fitted | RadioCue.FITTED_ESCORT))
                .toList();
        double[] timed = rook.stream().mapToDouble(RadioCue::t).toArray();
        boolean[] escortOnly = new boolean[rook.size()];
        for (int i = 0; i < escortOnly.length; i++) {
            escortOnly[i] = RadioSchedule.needsEscort(rook.get(i));
        }
        // M5 part D: a rear ambush enters at the top; its re-entry behind the ship is its own flight: no warning and
        // no bark (2026-10-21, user).
        List<WaveSpec> rear = script.waves().stream()
                .filter(wave -> wave.entry() == WaveSpec.Entry.REAR)
                .filter(wave -> wave.formation() != WaveSpec.Formation.REAR_AMBUSH)
                .toList();
        // M7 part G: a rear wave whose time falls in reversed travel enters at the top ("Up top!").
        boolean[] top = new boolean[rear.size()];
        for (int i = 0; i < top.length; i++) {
            top[i] = Sortie.travelAt(script, rear.get(i).t()) < 0;
        }
        return new Barks(
                data,
                script.number(),
                timed,
                escortOnly,
                rear.stream().mapToDouble(WaveSpec::t).toArray(),
                top,
                waves(script.waves(), WaveSpec.Entry.SIDES),
                radio,
                queue);
    }

    private static double[] waves(List<WaveSpec> waves, WaveSpec.Entry entry) {
        return waves.stream()
                .filter(wave -> wave.entry() == entry)
                .mapToDouble(WaveSpec::t)
                .toArray();
    }

    private Optional<WingmenData.Bark> bark(Trigger trigger) {
        return Optional.ofNullable(barks[trigger.ordinal()]);
    }

    /**
     * The clocks after a simulation step (M5 part C): the level clock {@code scriptSeconds}, the real
     * clock {@code realSeconds} that every {@code t} given to the barks is on, and the level clock's
     * {@code scriptRate} (script seconds per real second; 0.2 in a hold zone at 30 of 150 px/s), so a
     * timed Rook line's distance is measured in real seconds.
     */
    public void clock(double scriptSeconds, double realSeconds, double scriptRate) {
        scriptNow = scriptSeconds;
        realNow = realSeconds;
        rate = scriptRate;
    }

    /** Real seconds between {@code t} (real) and a timed line due at script time {@code due}. */
    private double realDistance(double due, double t) {
        if (Double.isNaN(scriptNow)) {
            return Math.abs(due - t);
        }
        double script = due - scriptNow;
        if (script == 0) {
            return Math.abs(realNow - t);
        }
        if (rate >= 1) {
            return Math.abs(script + realNow - t);
        }
        return rate > 0 ? Math.abs(script / rate + realNow - t) : Double.POSITIVE_INFINITY;
    }

    /** A new attempt (or the boss checkpoint) starting at {@code levelTick}: the counts and latches start over. */
    public void reset(int levelTick) {
        Arrays.fill(counts, 0);
        lastRookStart = Double.NEGATIVE_INFINITY;
        if (waiting != null) {
            radio.withdraw(waiting);
        }
        waiting = null;
        ejected = false;
        lost = false;
        over = false;
        reentryCount = 0;
        playerLow = null;
        overdrives = -1;
        lastTick = levelTick;
        kills.clear();
    }

    /** A line opened on the radio at {@code t} (real seconds): a Rook line starts the spacing. */
    public void opened(String speaker, double t) {
        if (speaker.equals(data.speaker())) {
            lastRookStart = t;
        }
    }

    /**
     * A scripted line about Rook fired at level time {@code t} (M5 part B: the radio's {@code
     * escort-first-kill} cue): a Rook line counts as one opened now for the spacing, and a bark of his
     * queued at the same time (the kill that made a streak) is withdrawn, so the scripted line says it.
     */
    public void scripted(String speaker, double t) {
        if (!speaker.equals(data.speaker())) {
            return;
        }
        lastRookStart = t;
        if (waiting != null && radio.waiting(waiting) && waitingPriority >= 0) {
            radio.withdraw(waiting);
            waiting = null;
        }
    }

    /**
     * The level is won and its outro starts (the user's playtest of 2026-10-19: the last radio message cut
     * short at the end of Level 15 or 16): a bark that still waits is withdrawn and none fires after it, so
     * no bark of his (a kill streak in the last seconds, an overdrive pickup) holds the level-end lines back
     * towards the outro's cap, where the debrief cuts the voice that still speaks. A bark already on the
     * radio finishes.
     */
    public void levelComplete() {
        over = true;
        if (waiting != null && radio.waiting(waiting)) {
            radio.withdraw(waiting);
        }
        waiting = null;
    }

    /** A boss's warning starts (an act boss's warning, a mid-boss's sting). */
    public boolean bossWarning(double t) {
        return fire(Trigger.BOSS_WARNING, t);
    }

    /** Rook's armour fell below his low-armour share (once per attempt, while he flies). */
    public boolean rookCritical(double t) {
        return !ejected && fire(Trigger.ROOK_ARMOUR, t);
    }

    /**
     * Rook ejects: his low-armour bark can no longer fire, and his eject bark cuts the bark of his that
     * waits or plays (it would come too late after it) and is queued whatever the spacing.
     */
    public boolean rookEjected(double t) {
        ejected = true;
        if (waiting != null) {
            radio.cancel(waiting);
            waiting = null;
        }
        return fire(Trigger.ROOK_EJECTS, t);
    }

    /**
     * M8 part F: his loss's trails start ({@code ROOK_TRAILS}, design/player/wingmen, Rook's loss): his barks end. A bark
     * of his that still waits is withdrawn and none fires after it (one already on the radio finishes); his {@code
     * requires: escort} lines no longer count for the spacing. His scripted lines are the radio's, not barks.
     */
    public void rookLost() {
        lost = true;
        ejected = true;
        if (waiting != null && radio.waiting(waiting)) {
            radio.withdraw(waiting);
        }
        waiting = null;
    }

    /**
     * M9 part B (Level 29, round 62; the user's playtest of step B5): whether he flies in his slot, set by the screen
     * before each step's events ({@link vanguard.sim.Wingman#waiting} or {@link vanguard.sim.Wingman#joining} is out
     * of it). Out of it his barks are off: a kill streak in the rescue halt (the Bulwark in the slot) would hold
     * Okafor's "Bulwark, RTB" back and leave his own "Miss me?" stale. A bark of his that still waits is withdrawn and
     * the streak's kills start over, so his barks begin once he is in the slot.
     */
    public void inSlot(boolean inSlot) {
        if (!inSlot && !benched) {
            if (waiting != null && radio.waiting(waiting)) {
                radio.withdraw(waiting);
            }
            waiting = null;
            kills.clear();
        }
        benched = !inSlot;
    }

    /** M8 part F: whether his barks have ended with his loss's trails in this attempt. */
    public boolean silenced() {
        return lost;
    }

    /** An enemy was destroyed (by the player or by him): ten within five seconds is a streak, then the count starts over. */
    public boolean kill(double t) {
        if (benched) {
            return false;
        }
        while (!kills.isEmpty() && t - kills.peekFirst() > streakSeconds) {
            kills.removeFirst();
        }
        kills.addLast(t);
        if (kills.size() < streakKills) {
            return false;
        }
        kills.clear();
        return fire(Trigger.KILL_STREAK, t);
    }

    /**
     * After each simulation step: the waves whose barks are due by {@code levelTick}, the player's
     * armour share (a bark when it falls below 30 %, again only after it was back at 30 % or more) and
     * the overdrive pickups on the field (a bark when one appears).
     */
    public void step(int levelTick, double playerArmourShare, int overdrivePickups) {
        step(levelTick, levelTick * SimStep.SECONDS, playerArmourShare, overdrivePickups);
    }

    /**
     * As above with the real clock: the barks fire at {@code realSeconds} (M5 part C), the waves' by
     * the level clock's {@code levelTick}.
     */
    public void step(int levelTick, double realSeconds, double playerArmourShare, int overdrivePickups) {
        double t = realSeconds;
        if (levelTick > lastTick) {
            int rear = dueRear(levelTick);
            if (rear >= 0) {
                fire(Trigger.REAR_WAVE, t, rear > 0);
            }
            if (due(sidesTicks, levelTick)) {
                fire(Trigger.SIDES_WAVE, t);
            }
        }
        // M5 part H: a re-entry's bark is due on the real clock, so it fires while the level clock is
        // halted (the Siege Spire's maw launches in its arena).
        int reentry = dueReentry(SimStep.ticks(realSeconds));
        if (reentry >= 0) {
            fire(Trigger.REAR_WAVE, t, reentry > 0);
        }
        lastTick = levelTick;
        boolean low = playerArmourShare < playerBelow;
        if (playerLow == null) {
            playerLow = low;
        } else if (low && !playerLow) {
            playerLow = true;
            fire(Trigger.PLAYER_ARMOUR, t);
        } else if (!low) {
            playerLow = false;
        }
        if (overdrives >= 0 && overdrivePickups > overdrives) {
            fire(Trigger.OVERDRIVE, t);
        }
        overdrives = overdrivePickups;
    }

    /**
     * M5 part D: the bottom edge's warning started at step {@code levelTick}: unless it is a rear wave's own warning
     * (its bark due within the warning), its rear bark is due {@link #REAR_AHEAD_SECONDS} before the entry, at the
     * warning's end. Since 2026-10-21 (user) a unit's own re-entry (a Wraith's, a loop-back) and a boss warn no more,
     * so this is a first entry no rear wave's bark covers (a cross unit's from the bottom edge).
     *
     * @return whether a bark was planned
     */
    public boolean reentry(int levelTick) {
        return reentry(levelTick, levelTick);
    }

    /**
     * As {@link #reentry(int)}, with the real clock's step {@code realTick} the bark is timed from (M5
     * part H: in a halted arena the level clock stands still while the warnings run on real steps).
     */
    public boolean reentry(int levelTick, int realTick) {
        return reentry(levelTick, realTick, false);
    }

    /**
     * As {@link #reentry(int, int)}, M7 part G: {@code top} for a re-entry from the top edge in reversed travel (a TOP
     * warning: a Wraith's, a swarm's loop-back), whose bark says one of the {@code top_lines}.
     */
    public boolean reentry(int levelTick, int realTick, boolean top) {
        for (int tick : rearTicks) {
            if (tick >= levelTick && tick <= levelTick + rearWarningTicks) {
                return false;
            }
        }
        if (reentryCount == REENTRIES) {
            return false;
        }
        reentryTop[reentryCount] = top;
        reentryTicks[reentryCount++] = realTick + reentryLead;
        return true;
    }

    /**
     * Whether a re-entry's bark is due by {@code realTick}: -1 for none, 1 when one of the due ones comes from the top,
     * else 0; the due ones are dropped.
     */
    private int dueReentry(int realTick) {
        int due = -1;
        for (int i = reentryCount - 1; i >= 0; i--) {
            if (reentryTicks[i] <= realTick) {
                due = Math.max(due, reentryTop[i] ? 1 : 0);
                reentryCount--;
                reentryTicks[i] = reentryTicks[reentryCount];
                reentryTop[i] = reentryTop[reentryCount];
            }
        }
        return due;
    }

    /** As {@link #due}, for the rear waves: -1 for none, 1 when one of the due ones enters from the top, else 0. */
    private int dueRear(int levelTick) {
        int due = -1;
        for (int k = 0; k < rearTicks.length; k++) {
            if (rearTicks[k] > lastTick && rearTicks[k] <= levelTick) {
                due = Math.max(due, rearTop[k] ? 1 : 0);
            }
        }
        return due;
    }

    /** Whether one of the steps lies after the last step seen, up to {@code levelTick}. */
    private boolean due(int[] ticks, int levelTick) {
        for (int tick : ticks) {
            if (tick > lastTick && tick <= levelTick) {
                return true;
            }
        }
        return false;
    }

    /**
     * A trigger at {@code t} (real seconds; the level time where no {@link #clock} runs): queued when the spacing and the priority allow it (the eject
     * bark always).
     *
     * @return whether the bark was queued
     */
    public boolean fire(Trigger trigger, double t) {
        return fire(trigger, t, false);
    }

    /**
     * As {@link #fire(Trigger, double)}, M7 part G: {@code top} for a rear wave from the top in reversed travel: the
     * bark's {@code top_lines} ({@link WingmenData.Bark#linesReversed}).
     */
    public boolean fire(Trigger trigger, double t, boolean top) {
        WingmenData.Bark bark = barks[trigger.ordinal()];
        if (bark == null || over || lost || benched) {
            return false;
        }
        boolean eject = trigger == Trigger.ROOK_EJECTS;
        if (!eject && t - lastRookStart < data.spacing()) {
            return false;
        }
        for (int i = 0; i < timedRook.length && !eject; i++) {
            if (!(timedEscortOnly[i] && ejected) && realDistance(timedRook[i], t) < data.spacing()) {
                return false;
            }
        }
        // Nothing outranks the eject bark while it waits.
        int priority = eject ? -1 : priorities[trigger.ordinal()];
        if (waiting != null && radio.waiting(waiting)) {
            if (priority >= waitingPriority) {
                return false;
            }
            radio.withdraw(waiting);
        }
        int n = counts[trigger.ordinal()]++;
        List<String> lines = top ? bark.linesReversed() : bark.lines();
        String text = lines.get(Math.floorMod(level + n, lines.size()));
        waiting =
                queue.apply(new Line(trigger, data.speaker(), bark.expression().slug(), bark.shouted(), text));
        waitingPriority = priority;
        return true;
    }
}
