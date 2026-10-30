package vanguard.game.screen;

import java.util.Optional;
import vanguard.content.Channel;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SpecialsData;
import vanguard.game.GameServices;
import vanguard.game.audio.Voices;
import vanguard.game.level.Barks;
import vanguard.game.level.RadioQueue;
import vanguard.game.level.RadioSchedule;
import vanguard.sim.CueTrigger;
import vanguard.sim.RadioCue;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * The radio's side of {@link LevelScreen}'s events: the level's cued lines (the failure line kept for
 * the mission failed screen, the secondary's line dropped at a boss's warning), a special's call,
 * Okafor's low-armour warning, a phase's waiting line withdrawn at the boss's death, and Rook's barks
 * on kills, the boss's warning, his low armour and his ejection.
 */
final class RadioEvents {
    /**
     * The secondary objective's line once queued (M5 part H, step H7b): dropped if it still waits, or
     * has only just opened, when a boss's warning starts; null before.
     */
    private RadioQueue.Message secondaryLine;

    /** The level's line for its failed primary objective, once it failed in this attempt. */
    private Optional<RadioCue> failureLine = Optional.empty();

    /**
     * A secondary's line that opened at most this long before a boss's warning is still cut (M5 part
     * H, step H7b): the warning would otherwise talk over its first words.
     */
    private static final float SECONDARY_DROP_SECONDS = 1.5f;

    /**
     * M8 part F: the last line queued with its own music duck ({@code duck_db}, Level 26's Choir) and that duck, dB;
     * null and 0 before.
     */
    private RadioQueue.Message duckLine;

    private double duckDb;

    /** Round 41: the set pieces warned of ahead of their arrival ({@code BOSS_INCOMING}) in this attempt. */
    private final java.util.Set<Integer> announced = new java.util.HashSet<>();

    private final GameServices services;
    /** The level: its cues' delays (round 45 B.a). */
    private final LevelData level;

    private final Sortie sortie;
    private final RadioQueue radio;
    private final RadioSchedule radioSchedule;
    /** Rook's radio barks while he flies in the level (M5 part A); null without him. */
    private final Barks barks;

    RadioEvents(
            GameServices services,
            LevelData level,
            Sortie sortie,
            RadioQueue radio,
            RadioSchedule radioSchedule,
            Barks barks) {
        this.services = services;
        this.level = level;
        this.sortie = sortie;
        this.radio = radio;
        this.radioSchedule = radioSchedule;
        this.barks = barks;
    }

    /**
     * M8 part F: how far the music ducks under the line on the radio now, dB: its row's {@code duck_db}, or 0 for the
     * radio's usual duck (design/audio, Mix groups).
     */
    double radioDuckDb() {
        return duckLine != null && radio.current().orElse(null) == duckLine ? duckDb : 0;
    }

    /** The level's own line for the mission failed screen, when its primary objective failed (design/systems/retry). */
    Optional<RadioCue> failureLine() {
        return failureLine;
    }

    /** A retry or a restart: no failure line yet. */
    void clearFailureLine() {
        failureLine = Optional.empty();
    }

    /** Event {@code i}'s radio lines and barks. */
    void react(SimEvents events, int i) {
        switch (events.type(i)) {
            case ENEMY_DESTROYED -> {
                if (barks != null) {
                    barks.kill(sortie.realSeconds());
                }
            }
            // M5 part H, step H7b: a phase's line still waiting would come after the death; step GK: so would one
            // playing again after an urgent line cut it (its voice stops with it).
            case BOSS_DESTROYED -> {
                if (radio.withdrawAll(RadioQueue.Priority.PHASE)) {
                    services.voices.stop();
                }
            }
            // Round 41: a boss with a lead time is warned of at its BOSS_INCOMING, and its arrival then
            // warns no more (a retry from the boss checkpoint has no lead warning: its arrival warns).
            case BOSS_INCOMING -> {
                announced.add(events.value(i));
                bossWarning();
            }
            case BOSS_ARRIVED -> {
                if (!announced.contains(events.value(i))) {
                    bossWarning();
                }
            }
            case SPECIAL_CALLED -> {
                // The Airstrike's call answers the player at once: an urgent line that interrupts
                // whatever is on the radio, which plays again after it (design/ui/hud, priority
                // interrupts); M7 part A: the Orbital Lance's Lance Control likewise (her voice cast
                // in round 37). The Smart Bomb, the EMP Burst and the Shield Overcharge
                // have no call.
                SpecialsData.Radio call =
                        LevelScreen.specialCall(sortie.special().kind(), services.content.specials());
                if (call != null) {
                    queue(call.speaker(), call.portrait(), "neutral", call.line(), false, RadioQueue.Priority.URGENT);
                }
            }
            case ARMOUR_CRITICAL -> {
                // Okafor's low-armour warning (design/player/armor), once per attempt: urgent, so it
                // plays at once, interrupting whatever is on the radio, and is never dropped as stale.
                LevelData.RadioLine warning = services.content.armour().radio();
                queue(
                        warning.speaker(),
                        warning.speaker(),
                        warning.expression().orElse(Expression.NEUTRAL).slug(),
                        warning.line(),
                        warning.distorted().orElse(false),
                        RadioQueue.Priority.URGENT);
            }
            case RADIO -> cueLine(events.value(i), 0);
            // M9 part B (design/campaign Level 29, Radio): with no Rook to join the swap (an `--escort none` run: the
            // Ember launches and leaves upward) Rook's rescue line plays all the same as the Ember leaves the hold,
            // a join's length after the release (he flies elsewhere in the formation).
            case ESCORT_SWAP -> {
                if (events.value(i) == vanguard.sim.EscortSwap.LEAVES) {
                    for (int c = 0; c < sortie.script().radio().size(); c++) {
                        if (sortie.script().radio().get(c).trigger() == CueTrigger.ESCORT_JOINED) {
                            cueLine(c, LEAVING_LINE_DELAY);
                        }
                    }
                }
            }
            case WINGMAN_CRITICAL -> {
                if (barks != null) {
                    barks.rookCritical(sortie.realSeconds());
                }
            }
            case WINGMAN_EJECTED -> {
                if (barks != null) {
                    barks.rookEjected(sortie.realSeconds());
                }
            }
            // M8 part F: his loss's trails end his barks (Level 26); his scripted lines play on.
            case ROOK_TRAILS -> {
                if (barks != null) {
                    barks.rookLost();
                }
            }
            case SORTIE_RESTARTED -> {
                announced.clear();
                if (barks != null) {
                    barks.reset(SimStep.ticks(sortie.levelSeconds()));
                }
                failureLine = Optional.empty();
            }
            default -> {}
        }
    }

    /** M9 part B: Rook's rescue line waits a join's length after the release when the Ember leaves (s). */
    static final float LEAVING_LINE_DELAY = 1;

    /** Radio cue {@code line} set off, held {@code extraDelay} s past its own delay: its line queued. */
    private void cueLine(int line, float extraDelay) {
        RadioCue cue = sortie.script().radio().get(line);
        if (cue.trigger() == CueTrigger.MISSION_FAILED) {
            failureLine = Optional.of(failedGroupLine(cue));
            return;
        }
        int unit = LevelScreen.cueUnit(cue.trigger(), sortie);
        if (barks != null && LevelScreen.scriptedForBarks(cue.trigger())) {
            // Rook's first kill (M5 part B), the first loop-back (M5 part D): a scripted
            // Rook line for the barks' spacing.
            barks.scripted(cue.speaker(), sortie.realSeconds());
        }
        RadioQueue.Message message = queue(
                cue.speaker(),
                cue.portrait(),
                cue.expression(),
                sectionLine(RadioSchedule.line(cue.line(), unit, allyName(unit), escortSide()), unit),
                cue.distorted(),
                radioSchedule.priority(line),
                // Round 45 B.a: a line held after its event (the Choir's words past the Colossus's death roar); step
                // FIX6: and until its level second (Level 33's leech line in the calm before the end)
                (float) (level.radioDelay(line) + Math.max(0, level.radioNotBefore(line) - sortie.levelSeconds()))
                        + extraDelay,
                // M9 part A: the line's channel (its own, else its speaker's).
                line < level.radio().size()
                        ? level.radio().get(line).channel(services.content.voices()) == Channel.ENEMY
                        : services.content.voices().channelOf(cue.speaker()) == Channel.ENEMY);
        if (cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
            secondaryLine = message;
        }
        double duck = line < level.radio().size() ? level.radio().get(line).musicDuckDb() : 0;
        if (duck > 0) {
            // M8 part F: the music ducks this much under the line while it plays (Level 26's Choir).
            duckLine = message;
            duckDb = duck;
        }
    }

    /** A boss's warning starts: the radio's side of it. */
    private void bossWarning() {
        // M5 part H, step H7b: the secondary's line that waited out the timed lines before
        // the boss (Level 14's root grid behind the Choir and Varga) is dropped, not
        // played over the warning; the HUD shows the objective done.
        if (secondaryLine != null && radio.drop(secondaryLine, SECONDARY_DROP_SECONDS)) {
            services.voices.stop();
        }
        secondaryLine = null;
        if (barks != null) {
            barks.bossWarning(sortie.realSeconds());
        }
    }

    /** The failure line with the lost group's name in it ("Battery C is behind you"), when a group failed the level. */
    private RadioCue failedGroupLine(RadioCue cue) {
        int group = sortie.failedGroup();
        if (group < 0) {
            return cue;
        }
        return new RadioCue(
                cue.trigger(),
                cue.t(),
                cue.subject(),
                cue.speaker(),
                vanguard.content.voice.VoiceLines.groupLine(
                        cue.line(), sortie.script().groups().get(group)),
                cue.distorted(),
                cue.expression(),
                cue.portrait(),
                cue.requiresSpecial(),
                cue.alliesMin(),
                cue.alliesMax(),
                cue.requires(),
                cue.requiresNot());
    }

    /**
     * M9 part D: a hull escort's line with {@code {section}} as the hull section {@code unit}'s name ("The bow's
     * hit!"; the voice lookup finds that section's take); other lines as they are.
     */
    private String sectionLine(String line, int unit) {
        return unit >= 0 && sortie.hullEscort()
                ? vanguard.content.voice.VoiceLines.sectionLine(line, sortie.allyName(unit))
                : line;
    }

    /** M5 part E: the name {@code {ally}} reads for convoy unit {@code unit}: a naval convoy's ship ("Halvorsen"); "" otherwise. */
    private String allyName(int unit) {
        return unit >= 0 && (sortie.navalConvoy() || sortie.groundAllies()) ? sortie.allyName(unit) : "";
    }

    /**
     * Rook's side for a radio line's {@code {side}} (M5 part B): his flight's side setting (the
     * save's, or {@code --escort}'s {@code side=}), left when he does not fly.
     */
    private WingmanSpec.Side escortSide() {
        return sortie.wingman().map(wingman -> wingman.spec().side()).orElse(WingmanSpec.Side.LEFT);
    }

    /** Queues a radio line with its voice file, if it has one; without one it shows as text only. */
    RadioQueue.Message queue(
            String speaker,
            String portrait,
            String expression,
            String line,
            boolean distorted,
            RadioQueue.Priority priority) {
        return queue(
                speaker,
                portrait,
                expression,
                line,
                distorted,
                priority,
                0,
                services.content.voices().channelOf(speaker) == Channel.ENEMY);
    }

    /** As above, the line held {@code delay} seconds before it may open (round 45 B.a). */
    private RadioQueue.Message queue(
            String speaker,
            String portrait,
            String expression,
            String line,
            boolean distorted,
            RadioQueue.Priority priority,
            float delay,
            boolean intercepted) {
        Optional<Voices.Voice> voice = services.voices.radio(speaker, line, expression);
        return radio.add(
                speaker,
                portrait,
                expression,
                line,
                distorted,
                // M9 part A (design/ui/hud, Act 5 additions): an intercepted line is never urgent.
                intercepted && priority == RadioQueue.Priority.URGENT ? RadioQueue.Priority.EVENT : priority,
                voice.map(Voices.Voice::path),
                voice.map(Voices.Voice::seconds).orElse(0f),
                delay,
                intercepted);
    }
}
