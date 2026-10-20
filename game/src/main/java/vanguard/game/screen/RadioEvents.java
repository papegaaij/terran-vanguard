package vanguard.game.screen;

import java.util.Optional;
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

    /** Round 41: the set pieces warned of ahead of their arrival ({@code BOSS_INCOMING}) in this attempt. */
    private final java.util.Set<Integer> announced = new java.util.HashSet<>();

    private final GameServices services;
    private final Sortie sortie;
    private final RadioQueue radio;
    private final RadioSchedule radioSchedule;
    /** Rook's radio barks while he flies in the level (M5 part A); null without him. */
    private final Barks barks;

    RadioEvents(GameServices services, Sortie sortie, RadioQueue radio, RadioSchedule radioSchedule, Barks barks) {
        this.services = services;
        this.sortie = sortie;
        this.radio = radio;
        this.radioSchedule = radioSchedule;
        this.barks = barks;
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
            // M5 part H, step H7b: a phase's line still waiting would come after the death.
            case BOSS_DESTROYED -> radio.withdrawAll(RadioQueue.Priority.PHASE);
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
            case RADIO -> {
                RadioCue cue = sortie.script().radio().get(events.value(i));
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
                        RadioSchedule.line(cue.line(), unit, allyName(unit), escortSide()),
                        cue.distorted(),
                        radioSchedule.priority(events.value(i)));
                if (cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                    secondaryLine = message;
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
        Optional<Voices.Voice> voice = services.voices.radio(speaker, line, expression);
        return radio.add(
                speaker,
                portrait,
                expression,
                line,
                distorted,
                priority,
                voice.map(Voices.Voice::path),
                voice.map(Voices.Voice::seconds).orElse(0f));
    }
}
