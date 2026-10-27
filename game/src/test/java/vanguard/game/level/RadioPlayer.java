package vanguard.game.level;

import static vanguard.game.level.RadioRuns.holdRuns;
import static vanguard.game.level.RadioTimeline.ARENA;
import static vanguard.game.level.RadioTimeline.ASSETS;
import static vanguard.game.level.RadioTimeline.BARKS_BEFORE_END;
import static vanguard.game.level.RadioTimeline.CONTENT;
import static vanguard.game.level.RadioTimeline.ESCORT;
import static vanguard.game.level.RadioTimeline.FITS;
import static vanguard.game.level.RadioTimeline.LOW_ARMOUR;
import static vanguard.game.level.RadioTimeline.LOW_ARMOUR_EXPRESSION;
import static vanguard.game.level.RadioTimeline.NO_BARK;
import static vanguard.game.level.RadioTimeline.SIDES;
import static vanguard.game.level.RadioTimeline.VOICES;
import static vanguard.game.level.RadioTimeline.barkMessage;
import static vanguard.game.level.RadioTimeline.holdSeconds;
import static vanguard.game.level.RadioTimeline.longestBarkLine;
import static vanguard.game.level.RadioTimeline.text;
import static vanguard.game.level.RadioTimeline.voiceSeconds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.game.audio.VorbisFile;
import vanguard.game.level.RadioRuns.HoldRun;
import vanguard.game.level.RadioTimeline.Event;
import vanguard.game.level.RadioTimeline.Played;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.RadioCue;
import vanguard.sim.Section;
import vanguard.sim.SimStep;

/**
 * RF6: the radio timeline's player: steps the real radio queue over a level as the game does ({@link #play}), the
 * timed cues at their ticks, the runs' events at theirs, the holds and a halted arena on the level clock, the level
 * end and the outro; and every voiced run of a level to its end ({@link #endRuns}). The checks are in {@link
 * RadioTimeline}.
 */
final class RadioPlayer {
    private RadioPlayer() {}

    /**
     * One voiced run of the level to its end and through the outro: the run's settings, what played and
     * the real second the level clock reached the level's end ({@code ended}), when the flight ends
     * ({@code LEVEL_COMPLETE}) and the outro starts. {@code home} is the count of convoy units home the
     * level-end lines are chosen for.
     */
    record EndRun(
            Difficulty difficulty,
            List<Event> events,
            int fitted,
            String side,
            double hold,
            int home,
            List<Played> played,
            double ended,
            double bark,
            double handover) {
        /** Whether {@code cue} can start in this run: the fit allows it, and a level-end line is this run's count's. */
        boolean allows(RadioCue cue) {
            return cue.allowedWith(fitted)
                    && (cue.trigger() != CueTrigger.LEVEL_END || (home >= cue.alliesMin() && home <= cue.alliesMax()));
        }

        @Override
        public String toString() {
            return difficulty + ", " + events + ", fit " + fitted + ", " + side
                    + (Double.isNaN(hold) ? "" : ", holds " + hold + " s") + ", " + home + " home"
                    + (Double.isNaN(bark) ? "" : ", Rook's bark " + bark + " s before the end");
        }
    }

    /** Every voiced run of {@code level} to its end ({@link RadioTimeline#endRuns()}): its {@code runs} in each setting. */
    static List<EndRun> endRuns(String level, List<List<Event>> runs) {
        List<EndRun> endRuns = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            int[] homes = script.radio().stream()
                    .filter(cue -> cue.trigger() == CueTrigger.LEVEL_END)
                    .mapToInt(RadioCue::alliesMin)
                    .distinct()
                    .toArray();
            if (homes.length == 0
                    && script.radio().stream().anyMatch(cue -> cue.trigger() == CueTrigger.BOSS_DESTROYED)) {
                // M8 part H (step H6): a boss level without level-end lines (Levels 07 and 28) is flown to its end too,
                // once: its boss's closing lines and the outro's wait for them are checked as everywhere.
                homes = new int[] {-1};
            }
            for (List<Event> events : runs) {
                if (events.stream().anyMatch(event -> event.t() > script.seconds())) {
                    continue;
                }
                for (int fitted : FITS) {
                    for (String side : SIDES) {
                        for (double hold : holdSeconds(script)) {
                            for (int home : homes) {
                                for (double bark : (fitted & ESCORT) != 0 ? BARKS_BEFORE_END : NO_BARK) {
                                    double[] ended = {Double.NaN, Double.NaN};
                                    List<Played> played = play(
                                            script,
                                            events,
                                            fitted,
                                            true,
                                            Double.NaN,
                                            new double[1],
                                            side,
                                            hold,
                                            home,
                                            ended,
                                            bark);
                                    endRuns.add(new EndRun(
                                            difficulty,
                                            events,
                                            fitted,
                                            side,
                                            hold,
                                            home,
                                            played,
                                            ended[0],
                                            bark,
                                            ended[1]));
                                }
                            }
                        }
                    }
                }
            }
        }
        return endRuns;
    }

    /** The voice length of {@code speaker}'s {@code text} in {@code expression} from its rendered file, 0 without one. */
    static float readVoiceSeconds(String speaker, String text, String expression) {
        Optional<String> path = VoiceLines.radioVoice(VOICES, CONTENT.voices(), speaker, text, expression);
        if (path.isEmpty()) {
            return 0;
        }
        try (VorbisFile file = new VorbisFile(java.nio.file.Files.readAllBytes(ASSETS.resolve(path.get())))) {
            return (float) file.frameCount() / file.sampleRate();
        } catch (java.io.IOException e) {
            return 0;
        }
    }

    /**
     * Whether one of {@code events} has set off the event cue by {@code scriptTick} (the level
     * clock), an event in a hold by {@code realTick} with hold {@code k} started at {@code holdStarts[k]}.
     */
    static boolean setOff(List<Event> events, RadioCue cue, int scriptTick, int realTick, int[] holdStarts) {
        return events.stream()
                .anyMatch(event -> event.trigger() == cue.trigger()
                        && event.subject().equals(cue.subject())
                        && happened(event, scriptTick, realTick, holdStarts));
    }

    /** Whether {@code event} has happened by {@code scriptTick} (the level clock), in a hold by {@code realTick}. */
    static boolean happened(Event event, int scriptTick, int realTick, int[] holdStarts) {
        // M8 part H: the arena's halt is the last slot of holdStarts.
        int hold = event.hold() == ARENA ? holdStarts.length - 1 : event.hold();
        return hold < 0
                ? SimStep.ticks(event.t()) <= scriptTick
                : holdStarts[hold] >= 0 && realTick - holdStarts[hold] >= SimStep.ticks(event.into());
    }

    /**
     * M8 part H: the level clock's step a halted arena holds it at (a step short of the arena section's end, as the
     * simulation's), -1 for a level without one.
     */
    static int arenaHaltTick(LevelScript script) {
        for (Section section : script.sections()) {
            if (section.arena() && section.speed() == 0 && !section.crawls()) {
                return SimStep.ticks(section.end()) - 1;
            }
        }
        return -1;
    }

    /** M8 part H: the seconds the clock stays halted after the arena boss's kill: its death chain and the hold after. */
    static double holdAfterKill(LevelScript script) {
        double hold = 0;
        for (Section section : script.sections()) {
            if (section.arena()) {
                hold = section.holdAfterBossSeconds();
            }
        }
        double death = script.setPieces().stream()
                .flatMap(piece -> piece.boss().stream())
                .mapToDouble(vanguard.sim.BossSpec::deathSeconds)
                .findFirst()
                .orElse(0);
        return hold > 0 ? death + hold : 0;
    }

    /** A cue queued in {@link #play}: its index, its text, when it was due and when its message opened. */
    private static final class Queued {
        final int cue;
        final String text;
        final double due;
        Double opened;

        Queued(int cue, String text, double due) {
            this.cue = cue;
            this.text = text;
            this.due = due;
        }
    }

    static double sectionSpeed(LevelScript script, double t) {
        for (Section section : script.sections()) {
            if (t < section.end()) {
                return section.speed();
            }
        }
        return script.sections().getLast().speed();
    }

    /**
     * Steps the queue over the level: the timed cues at their ticks and {@code events} at theirs,
     * each queued by its priority, the level-end cues at the end; the radio runs on after the end
     * until it is idle.
     */
    static List<Played> play(LevelScript script, List<Event> events) {
        return play(script, events, ESCORT, false);
    }

    /** As the game queues them with {@code fitted} on the ship: a cue it does not allow never starts. */
    static List<Played> play(LevelScript script, List<Event> events, int fitted, boolean voiced) {
        return play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
    }

    /**
     * As above, with the low-armour line queued urgent at {@code lowArmourAt} (NaN for never);
     * {@code lowArmourOpened[0]} gets the time its message opened.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened) {
        return play(script, events, fitted, voiced, lowArmourAt, lowArmourOpened, SIDES.getFirst(), 6);
    }

    /**
     * As above with Rook on {@code side} and each hold zone lasting {@code holdSeconds} real seconds
     * (NaN or a level without holds: the level clock runs with real time). The loop steps real time;
     * the level clock follows the holds.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds) {
        return play(script, events, fitted, voiced, lowArmourAt, lowArmourOpened, side, holdSeconds, -1, new double[1]);
    }

    /**
     * As above with {@code allies} convoy units home at the end (only the level-end cues for that many
     * start; -1: every level-end cue); {@code endedAt[0]} gets the real second the level clock reached
     * the level's end.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds,
            int allies,
            double[] endedAt) {
        return play(
                script,
                events,
                fitted,
                voiced,
                lowArmourAt,
                lowArmourOpened,
                side,
                holdSeconds,
                allies,
                endedAt,
                Double.NaN);
    }

    /**
     * As above, with Rook's {@link RadioTimeline#longestBarkLine} queued as an event line {@code barkBefore} seconds
     * before the level's end (level clock; NaN for none, and none without him flying). At the level's
     * end the radio plays out the closing lines ({@link RadioQueue#outro}) and the outro runs as the
     * game's ({@link Outro}): {@code endedAt[1]}, if there is room for it, gets the real second the
     * debrief takes over (NaN if not within a minute).
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds,
            int allies,
            double[] endedAt,
            double barkBefore) {
        Outro outro = new Outro();
        int barkTick = Double.isNaN(barkBefore) || (fitted & ESCORT) == 0
                ? Integer.MAX_VALUE
                : SimStep.ticks(script.seconds() - barkBefore);
        boolean barked = false;
        int lowArmourTick = Double.isNaN(lowArmourAt) ? -1 : SimStep.ticks(lowArmourAt);
        List<String> lowArmourLines = RadioQueue.wrap(LOW_ARMOUR.line());
        RadioSchedule schedule = new RadioSchedule(script, fitted & ~ESCORT);
        boolean escort = (fitted & ESCORT) != 0;
        RadioQueue radio = new RadioQueue();
        List<RadioCue> cues = script.radio();
        // Round 45 B.a: the cues' delays after their events, as the game reads them from the level's data
        Optional<LevelData> level = CONTENT.levelKey(script.number()).map(CONTENT::level);
        float[] delays = new float[cues.size()];
        for (int i = 0; i < delays.length; i++) {
            int cue = i;
            delays[i] = level.map(data -> (float) data.radioDelay(cue)).orElse(0f);
        }
        boolean[] fired = new boolean[cues.size()];
        // M7 part D: the events of repeating triggers, each setting off its cues once
        boolean[] eventFired = new boolean[events.size()];
        List<Queued> queued = new ArrayList<>();
        int end = SimStep.ticks(script.seconds());
        // M5 part D: the scripted loss's time (Level 10's lance), never without one
        int lanceTick = script.objectives()
                .escort()
                .flatMap(ObjectiveSpec.Escort::air)
                .flatMap(ObjectiveSpec.Air::scriptedLoss)
                .map(loss -> SimStep.ticks(loss.t()))
                .orElse(Integer.MAX_VALUE);
        List<HoldRun> holds = holdRuns(script, holdSeconds);
        // M8 part H: the last slot is the halted arena's start (real tick), for a run with events in it.
        int[] holdStarts = new int[holds.size() + 1];
        java.util.Arrays.fill(holdStarts, -1);
        int arena = holds.size();
        int haltTick = events.stream().anyMatch(event -> event.hold() == ARENA) ? arenaHaltTick(script) : -1;
        double killInto = events.stream()
                .filter(event -> event.hold() == ARENA && event.trigger() == CueTrigger.BOSS_DESTROYED)
                .mapToDouble(Event::into)
                .findFirst()
                .orElse(Double.POSITIVE_INFINITY);
        int haltTicks = Double.isInfinite(killInto)
                ? Integer.MAX_VALUE / 2
                : SimStep.ticks(killInto) + SimStep.ticks(holdAfterKill(script));
        boolean killed = false;
        // The level clock in fixed point, as the simulation's (1/65 536 of a step).
        long scriptFixed = 0;
        int scriptTick = 0;
        int afterEnd = -1;
        for (int tick = 1; afterEnd < 0 || tick <= afterEnd; tick++) {
            double seconds = (double) tick / SimStep.PER_SECOND;
            // The game's frame: the outro first, then the simulation's step and the radio.
            if (outro.update((float) SimStep.SECONDS, radio) && endedAt.length > 1 && Double.isNaN(endedAt[1])) {
                endedAt[1] = seconds - SimStep.SECONDS;
            }
            double rate = 1;
            boolean collapsing = false;
            for (int h = 0; h < holds.size(); h++) {
                HoldRun hold = holds.get(h);
                if (holdStarts[h] < 0 && scriptTick >= SimStep.ticks(hold.start())) {
                    holdStarts[h] = tick;
                }
                if (holdStarts[h] >= 0) {
                    double in = (tick - holdStarts[h]) * SimStep.SECONDS;
                    double out = in - hold.seconds();
                    double s = out < 0 ? Math.min(1, in / hold.ramp()) : Math.max(0, 1 - out / hold.ramp());
                    rate = Math.min(rate, 1 + (hold.ratio() - 1) * s * s * (3 - 2 * s));
                    collapsing |= hold.collapse() && in >= holdSeconds;
                }
            }
            // M8 part H: the halted arena: the clock waits a step short of the arena's end until the kill's hold ends.
            if (haltTick >= 0 && holdStarts[arena] < 0 && scriptTick >= haltTick) {
                holdStarts[arena] = tick;
            }
            boolean halted = holdStarts[arena] >= 0 && tick - holdStarts[arena] < haltTicks;
            if (halted) {
                rate = 0;
            } else {
                scriptFixed += Math.max(1, Math.round(rate * 65536));
            }
            scriptTick = (int) (scriptFixed >> 16);
            // M8 part H: the kill withdraws the phase lines still waiting (RadioEvents), its closing lines then queue.
            if (!killed) {
                for (Event event : events) {
                    if (event.trigger() == CueTrigger.BOSS_DESTROYED && happened(event, scriptTick, tick, holdStarts)) {
                        killed = true;
                        radio.withdrawAll(RadioQueue.Priority.PHASE);
                        break;
                    }
                }
            }
            if (scriptTick >= end && afterEnd < 0) {
                afterEnd = tick + SimStep.ticks(60);
                endedAt[0] = seconds;
                // LEVEL_COMPLETE: the outro starts and the radio plays out the closing lines.
                outro.start();
                radio.outro();
            }
            if (!barked && scriptTick >= barkTick && scriptTick < end) {
                barked = true;
                Barks.Line line = longestBarkLine();
                RadioQueue.Message bark = barkMessage(line);
                radio.add(
                        bark.speaker(),
                        bark.portrait(),
                        bark.expression(),
                        line.text(),
                        false,
                        line.priority(),
                        Optional.empty(),
                        voiced ? bark.voiceSeconds() : 0);
            }
            double scriptSeconds = (double) scriptFixed / 65536 * SimStep.SECONDS;
            for (int i = 0; i < cues.size(); i++) {
                RadioCue cue = cues.get(i);
                // a fired or disallowed cue never queues again: skip it before asking whether it is due; a
                // repeating trigger's cue is queued by each of its events below
                if (fired[i] || !cue.allowedWith(fitted) || cue.trigger().repeats()) {
                    continue;
                }
                boolean due =
                        switch (cue.trigger()) {
                            case TIME -> scriptTick <= end && SimStep.ticks(cue.t()) <= scriptTick;
                            case LEVEL_END ->
                                scriptTick >= end
                                        && (allies < 0 || (allies >= cue.alliesMin() && allies <= cue.alliesMax()));
                            case HOLD_START -> !holds.isEmpty() && holdStarts[0] >= 0;
                            case COLLAPSE -> collapsing;
                            // M5 part D: the lance comes at its time in every run (Level 10)
                            case SCRIPTED_LOSS -> scriptTick >= lanceTick;
                            default -> setOff(events, cue, scriptTick, tick, holdStarts);
                        };
                if (due) {
                    fired[i] = true;
                    double dueAt =
                            cue.trigger() == CueTrigger.TIME && holds.isEmpty() && haltTick < 0 ? cue.t() : seconds;
                    queued.add(queue(radio, schedule, cue, i, text(cue, side), dueAt, voiced, delays[i]));
                }
            }
            for (int e = 0; e < events.size(); e++) {
                Event event = events.get(e);
                if (eventFired[e] || !event.trigger().repeats() || !happened(event, scriptTick, tick, holdStarts)) {
                    continue;
                }
                eventFired[e] = true;
                for (int i = 0; i < cues.size(); i++) {
                    RadioCue cue = cues.get(i);
                    if (cue.trigger() == event.trigger()
                            && cue.subject().equals(event.subject())
                            && cue.allowedWith(fitted)) {
                        queued.add(queue(
                                radio, schedule, cue, i, text(cue, side, event.ally()), seconds, voiced, delays[i]));
                    }
                }
            }
            if (tick == lowArmourTick) {
                radio.add(
                        LOW_ARMOUR.speaker(),
                        LOW_ARMOUR.speaker(),
                        LOW_ARMOUR_EXPRESSION,
                        LOW_ARMOUR.line(),
                        LOW_ARMOUR.distorted().orElse(false),
                        RadioQueue.Priority.URGENT,
                        Optional.empty(),
                        voiced ? voiceSeconds(LOW_ARMOUR.speaker(), LOW_ARMOUR.line(), LOW_ARMOUR_EXPRESSION) : 0);
            }
            float untilTimed =
                    scriptTick < end ? schedule.untilTimed(scriptSeconds, rate, escort) : Float.POSITIVE_INFINITY;
            if (radio.update((float) SimStep.SECONDS, untilTimed) == RadioQueue.Change.OPENED) {
                RadioQueue.Message message = radio.current().orElseThrow();
                if (Double.isNaN(lowArmourOpened[0])
                        && message.speaker().equals(LOW_ARMOUR.speaker())
                        && message.lines().equals(lowArmourLines)) {
                    lowArmourOpened[0] = seconds;
                }
                for (Queued line : queued) {
                    if (line.opened == null
                            && cues.get(line.cue).speaker().equals(message.speaker())
                            && RadioQueue.wrap(line.text).equals(message.lines())) {
                        line.opened = seconds;
                        break;
                    }
                }
            }
        }
        List<Played> played = new ArrayList<>();
        for (int i = 0; i < cues.size(); i++) {
            boolean any = false;
            for (Queued line : queued) {
                if (line.cue == i) {
                    played.add(new Played(cues.get(i), Optional.ofNullable(line.opened), line.due, line.text));
                    any = true;
                }
            }
            if (!any) {
                played.add(new Played(cues.get(i), Optional.empty(), 0, text(cues.get(i), side)));
            }
        }
        return played;
    }

    /**
     * Queues cue {@code i} with {@code text} on {@code radio} as the game does, due at {@code due}, held {@code
     * delay} seconds (round 45 B.a).
     */
    private static Queued queue(
            RadioQueue radio,
            RadioSchedule schedule,
            RadioCue cue,
            int i,
            String text,
            double due,
            boolean voiced,
            float delay) {
        radio.add(
                cue.speaker(),
                cue.portrait(),
                cue.expression(),
                text,
                cue.distorted(),
                schedule.priority(i),
                Optional.empty(),
                voiced ? voiceSeconds(cue.speaker(), text, cue.expression()) : 0,
                delay);
        return new Queued(i, text, due);
    }
}
