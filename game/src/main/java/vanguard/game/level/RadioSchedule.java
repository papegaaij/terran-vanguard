package vanguard.game.level;

import java.util.Comparator;
import java.util.List;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;
import vanguard.sim.WingmanSpec;

/**
 * The level script's radio cues as the {@link RadioQueue} needs them: how each queues, and how long
 * until the next timed line is due, so an event line only takes a gap that ends before it. M5 part
 * C: the timed lines are in script time, which slows in a hold zone, while the queue runs on real
 * time, so the gap is measured in real seconds at the level clock's current rate; and a line that
 * requires an escort ({@code requires: escort}) counts only while he flies.
 */
public final class RadioSchedule {
    private final LevelScript script;
    /** The timed cues' ticks, in order. */
    private final int[] timed;
    /** Per timed cue (same order) whether it plays only while the escort flies. */
    private final boolean[] escortOnly;
    /** M9 part H: per timed cue (same order) whether it plays only while no escort flies ({@code requires_not: escort}). */
    private final boolean[] escortNever;

    public RadioSchedule(LevelScript script) {
        this(script, false);
    }

    /** @param specialFitted whether a special is fitted: the timed cues that require one play only then */
    public RadioSchedule(LevelScript script, boolean specialFitted) {
        this(script, specialFitted ? RadioCue.FITTED_SPECIAL : 0);
    }

    /**
     * @param fitted what is fitted ({@link RadioCue#FITTED_SPECIAL}, {@link
     *     RadioCue#FITTED_HOMING} bits): the timed cues that require something else never
     *     play; those that require the escort are asked about at each {@link #untilTimed(double,
     *     double, boolean)}
     */
    public RadioSchedule(LevelScript script, int fitted) {
        this.script = script;
        int fittedOnly = fitted & ~RadioCue.FITTED_ESCORT;
        int withEscort = fittedOnly | RadioCue.FITTED_ESCORT;
        // M9 part H: a cue that plays only without the escort (requires_not: escort, Level 35's Okafor) is a timed cue
        // too while he does not fly, as one that requires him is while he does.
        List<RadioCue> cues = script.radio().stream()
                .filter(cue -> cue.trigger() == CueTrigger.TIME)
                .filter(cue -> cue.allowedWith(withEscort) || cue.allowedWith(fittedOnly))
                .sorted(Comparator.comparingDouble(RadioCue::t))
                .toList();
        timed = cues.stream().mapToInt(cue -> SimStep.ticks(cue.t())).toArray();
        escortOnly = new boolean[cues.size()];
        escortNever = new boolean[cues.size()];
        for (int i = 0; i < escortOnly.length; i++) {
            escortOnly[i] = needsEscort(cues.get(i));
            escortNever[i] = refusesEscort(cues.get(i));
        }
    }

    /** Whether a cue plays only while an escort flies ({@code requires: escort}). */
    public static boolean needsEscort(RadioCue cue) {
        return (cue.requires() & RadioCue.FITTED_ESCORT) != 0;
    }

    /** M9 part H: whether a cue plays only while no escort flies ({@code requires_not: escort}). */
    public static boolean refusesEscort(RadioCue cue) {
        return (cue.requiresNot() & RadioCue.FITTED_ESCORT) != 0;
    }

    /**
     * M9 part H: whether a cue plays with the escort flying or not ({@code escortFlying}) as far as the escort goes: one
     * that requires him only with him, one that requires his absence only without him.
     */
    public static boolean escortAllows(RadioCue cue, boolean escortFlying) {
        return escortFlying ? !refusesEscort(cue) : !needsEscort(cue);
    }

    /**
     * What a sortie's ship has fitted, as the radio's {@link RadioCue#FITTED_SPECIAL} and
     * {@link RadioCue#FITTED_HOMING} bits (a weapon with homing or turret delivery), as the
     * simulation's radio asks for them.
     */
    public static int fitted(Sortie sortie) {
        int fitted = sortie.special().fitted() ? RadioCue.FITTED_SPECIAL : 0;
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (!sortie.armament().mount(m).fires()) {
                // M7 part A: a Deflector Pod fires nothing.
                continue;
            }
            WeaponSpec.Delivery delivery = sortie.armament().mount(m).weapon().delivery();
            if (delivery == WeaponSpec.Delivery.HOMING || delivery == WeaponSpec.Delivery.TURRET) {
                fitted |= RadioCue.FITTED_HOMING;
            }
        }
        return fitted;
    }

    /**
     * Whether the sortie's escort flies now (hired, fitted and not ejected; M8 part F: nor past his loss's trails, {@link
     * vanguard.sim.Wingman#onRadio}): a {@code requires: escort} cue plays.
     */
    public static boolean escortFlying(Sortie sortie) {
        return sortie.wingman().map(vanguard.sim.Wingman::onRadio).orElse(false);
    }

    /** How cue {@code index} of the script queues. */
    public RadioQueue.Priority priority(int index) {
        return switch (script.radio().get(index).trigger()) {
            case TIME -> RadioQueue.Priority.TIMED;
            // A boss's closing lines (Level 07's four after the kill) queue together and must not go stale.
            case LEVEL_END, SECONDARY_OBJECTIVE, BOSS_DESTROYED -> RadioQueue.Priority.CLOSING;
            // M5 part H, step H7b: a phase's lines queue together (Okafor's, then Rook's reply as the
            // Siege Spire tears loose); the second must not go stale behind the first.
            // Step FXL11: the arena's teaching line on a ship's lane (Level 11) comes in the same step as the
            // first telegraph's line when that telegraph falls on a ship's lane, so it waits out that voiced line;
            // as an event line it went stale before it closed and was never heard.
            // M8 part H (design/campaign Level 28, Radio chatter): on the halted arena's clock the boss's arrival and a
            // coiled boss's dive queue as phase lines too: they wait for a gap, go stale after 20 s, drop at the kill.
            // M9 part G (design/campaign Level 34, The freighter): the Helix core's line, collected or missed, often
            // comes while Varga's call at the hold still plays (t=108 to ≈119): it waits out that line, as a phase line
            // does, instead of going stale behind it; the core's fate is never left untold.
            // Step BOSS70 (Level 35, the core's playbook): the first volley, mine ring and wreck throw and the core's
            // low share come on the halted arena as the phases' lines do.
            case BOSS_PHASE,
                    FIRST_SHIP_TELEGRAPH,
                    BOSS_ARRIVAL,
                    BOSS_DIVE,
                    STORY_COLLECTED,
                    STORY_MISSED,
                    CORE_PLAY,
                    CORE_LOW -> RadioQueue.Priority.PHASE;
            default -> RadioQueue.Priority.EVENT;
        };
    }

    /** The placeholder in a convoy line that names the unit it is about ("Crawler {ally} is hit!"). */
    public static final String ALLY = VoiceLines.ALLY;

    /** A cue's line with {@link #ALLY} filled in as the convoy unit's number word ({@code unit} from 0). */
    public static String line(String line, int unit) {
        return VoiceLines.allyLine(line, unit);
    }

    /**
     * A cue's line as shown and spoken: {@link #ALLY} as the convoy unit's number word ({@code unit}
     * from 0, -1 for none) and (M5 part B) {@link VoiceLines#SIDE} as the escort's side, {@code
     * left} or {@code right} ({@code side}: the save's, or {@code --escort}'s {@code side=}).
     */
    public static String line(String line, int unit, WingmanSpec.Side side) {
        return VoiceLines.sideLine(line(line, unit), side == WingmanSpec.Side.LEFT ? "left" : "right");
    }

    /**
     * As {@link #line(String, int, WingmanSpec.Side)}; M5 part E: a naval convoy's unit is named by its
     * {@code name} (Level 11's ships, "The {ally} is hit!" reads "The Mbeki is hit!"); an empty name
     * keeps the number word.
     */
    public static String line(String line, int unit, String name, WingmanSpec.Side side) {
        String named = unit >= 0 && !name.isEmpty() ? line.replace(ALLY, name) : line;
        return line(named, unit, side);
    }

    /** Seconds from {@code levelSeconds} until the next timed cue starts; infinite after the last. */
    public float untilTimed(double levelSeconds) {
        return untilTimed(levelSeconds, 1, true);
    }

    /**
     * Real seconds from {@code levelSeconds} (script time) until the next timed cue starts, the level
     * clock running on at {@code scriptRate} script seconds per real second (M5 part C: 0.2 in a
     * hold zone at 30 of 150 px/s, 0 while an arena halts it); infinite after the last.
     *
     * @param escortFlying whether the escort flies: without him the cues that require him are skipped, with him those
     *     that require his absence (M9 part H)
     */
    public float untilTimed(double levelSeconds, double scriptRate, boolean escortFlying) {
        int levelTick = SimStep.ticks(levelSeconds);
        for (int i = 0; i < timed.length; i++) {
            if (timed[i] > levelTick && (escortFlying ? !escortNever[i] : !escortOnly[i])) {
                double script = (timed[i] - levelTick) * SimStep.SECONDS;
                if (scriptRate >= 1) {
                    return (float) script;
                }
                return scriptRate > 0 ? (float) (script / scriptRate) : Float.POSITIVE_INFINITY;
            }
        }
        return Float.POSITIVE_INFINITY;
    }
}
