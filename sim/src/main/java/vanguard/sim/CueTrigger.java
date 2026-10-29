package vanguard.sim;

public enum CueTrigger {
    TIME,
    FIRST_KILL,
    SECRET,
    SECONDARY_OBJECTIVE,
    LEVEL_END,
    /** A group of the secondary objective was cleared; the subject is its name. */
    GROUP_CLEARED,
    /** A group was lost; the subject is its name. */
    GROUP_LOST,
    /** The first group of the attempt was lost. */
    FIRST_GROUP_LOST,
    /** The first unit of an enemy left the screen alive (a set piece: at the end of its last pass); the subject is its slug. */
    ENEMY_ESCAPED,
    /** The first hit on a convoy unit in the attempt; its line may name the unit ({@code {ally}}). */
    FIRST_ALLY_HIT,
    /** The first convoy unit lost in the attempt; its line may name the unit ({@code {ally}}). */
    FIRST_ALLY_LOST,
    /**
     * M5 part B: the first kill in the attempt by the wingman's shot or blast ({@link
     * Sortie#wingmanMount()}), while he flies; never after he ejects. No subject.
     */
    ESCORT_FIRST_KILL,
    /** The primary objective failed: the line the mission failed screen shows, not played on the radio. */
    MISSION_FAILED,
    /** A boss entered a phase after its first; the subject is the phase's name. */
    BOSS_PHASE,
    /** A boss was destroyed; the subject is its slug. */
    BOSS_DESTROYED,
    /**
     * Part G: a boss entered a phase because the phase before it ran out of time with parts it
     * waited for still alive (Level 07's broadside phase timing out); the subject is the name of
     * the phase it entered. Its {@link #BOSS_PHASE} cues start as well.
     */
    BOSS_TIMEOUT,
    /** M5 part C: the level's first hold zone starts to ease down; once. No subject. */
    HOLD_START,
    /** M5 part C: the attempt's first pounce takes off ({@link SimEvents.Type#POUNCE}). No subject. */
    FIRST_POUNCE,
    /** M5 part C: the collapse's shadow starts. No subject. */
    COLLAPSE,
    /**
     * M5 part D: a convoy unit the player could have saved was lost (every such loss, the first
     * too, after its {@link #FIRST_ALLY_LOST} cues); its line may name the unit ({@code {ally}},
     * {@link Sortie#lastAllyLost()}). Unlike the other events it starts again on every loss.
     */
    ALLY_LOST,
    /** M5 part D: the scripted loss's lance took its unit ({@link SimEvents.Type#SCRIPTED_LOSS}). No subject. */
    SCRIPTED_LOSS,
    /** M5 part D: the attempt's first decloak ({@link SimEvents.Type#DECLOAK}). No subject. */
    FIRST_DECLOAK,
    /** M5 part D: the attempt's first swarm loop-back ({@link SimEvents.Type#LOOP_BACK}). No subject. */
    FIRST_LOOP_BACK,
    /**
     * M5 part E: a convoy unit's first hit (each unit's; Level 11's cargo ships); its line may name
     * the unit ({@code {ally}}, {@link Sortie#lastAllyHit()}). It starts again for each unit, as
     * {@link #ALLY_LOST}.
     */
    ALLY_HIT,
    /**
     * M5 part E: the attempt's first lane telegraph of an arena boss ({@link SimEvents.Type#TELEGRAPH});
     * once. No subject.
     */
    FIRST_TELEGRAPH,
    /**
     * M5 part E: the first of some of the level boss's parts shot off (not lost in its death); once.
     * The cue's subject names the parts it waits for, separated by {@link #PART_SEPARATOR}; the
     * event's subject is the part shot off.
     */
    BOSS_PART_DESTROYED,
    /**
     * M5 part F: the attempt's first latch of a Lamprey ({@link SimEvents.Type#LAMPREY_LATCHED}); once.
     * No subject.
     */
    FIRST_LATCH,
    /**
     * M5 part G: the defend primary's ally dropped below a share of its integrity for the first time
     * in the attempt (Level 13's relay below 50 % and 25 %); the subject is the share as a decimal
     * ("0.5"), and each share's cues start once.
     */
    ALLY_BELOW,
    /**
     * M7 part B: the attempt's first Burrower surfaced ({@link SimEvents.Type#BURROW_SURFACED}); once. No
     * subject (the radio event {@code first-surfacing}).
     */
    FIRST_SURFACING,
    /**
     * M7 part C: the attempt's first wall contact of the ship ({@link SimEvents.Type#WALL_CONTACT}, not
     * Rook's); once. No subject. Only in a level with terrain.
     */
    FIRST_WALL_CONTACT,
    /**
     * Round 40 (the reworked Harbour Kraken, proposal 3a): the attempt's first lane telegraph of an arena
     * boss on a lane holding a convoy ship a slam hurts (the teaching line: cut that lane's root); once.
     * No subject. Its {@link #FIRST_TELEGRAPH} cues start as well when it is the first telegraph.
     */
    FIRST_SHIP_TELEGRAPH,
    /**
     * M7 part D, round 40 K.a (design/campaign Level 17, the Herald's line): the attempt's first aura ring a
     * Choir Herald sings onto a unit ({@link SimEvents.Type#RING_SUNG}: as soon as it is on the play field with
     * its escorts round it); once. No subject.
     */
    FIRST_RING,
    /**
     * M8 part B (design/campaign Level 22, the geysers): the attempt's first vent tell ({@link
     * SimEvents.Type#VENT_TELL}: a geyser's crack); once. No subject (the radio event {@code first-geyser}).
     */
    FIRST_GEYSER,
    /**
     * M8 part B (design/campaign Level 22, the drill rigs): the attempt's first destroyed unit that the secondary
     * objective counts (an escapes or kill-all objective's); once, at that first one only. No subject (the radio event
     * {@code target-destroyed}).
     */
    TARGET_DESTROYED,
    /**
     * M8 part D (design/campaign Level 24, the Abyss Ray): the attempt's first rising unit's shadow ({@link
     * SimEvents.Type#RISE_SHADOW}, its wave's entry); once. No subject (the radio event {@code first-ray}).
     */
    FIRST_RAY,
    /**
     * M8 part H (design/campaign Level 28; gap check §5 H8): a boss arrived (its bar; the arena's clock is halted, so a
     * timed line cannot follow it); once. No subject (the radio event {@code boss-arrival}).
     */
    BOSS_ARRIVAL,
    /**
     * M8 part H (design/enemies/bosses/abyssal-maw, the dive): a coiled boss dives ({@link SimEvents.Type#MAW_DIVE});
     * once. No subject (the radio event {@code boss-dive}; the presentation queues it as a phase line).
     */
    BOSS_DIVE,
    /**
     * M9 part B (design/campaign Level 29, radio): the attempt's first enemy homing missile launched ({@link
     * SimEvents.Type#MISSILE_LAUNCHED}); once. No subject (the radio event {@code first-homing}).
     */
    FIRST_HOMING,
    /**
     * M9 part B (Level 29, Hold fire): the attempt's first neutral craft left the screen unharmed ({@link
     * SimEvents.Type#NEUTRAL_LEFT}); once. Its subject is the craft's slug (the radio event {@code neutral-left}).
     */
    NEUTRAL_LEFT,
    /**
     * M9 part B: the attempt's first neutral craft destroyed ({@link SimEvents.Type#NEUTRAL_DOWN}); once. Its subject
     * is the craft's slug (the radio event {@code neutral-down}).
     */
    NEUTRAL_DOWN,
    /**
     * M9 part B (Level 29, the rescue halt): the held arena's last holder fell ({@link SimEvents.Type#HOLDERS_DOWN});
     * once. Its subject (step B4b) is what the level's escort swap does ({@code EscortSwap#radioSubject}: {@code
     * bulwark}, {@code joins}, {@code leaves}, {@code empty}; none without a swap) (the radio event {@code
     * holders-down}; the clock resumes, so a timed line can follow too).
     */
    HOLDERS_DOWN,
    /**
     * M9 part B (design/player/wingmen, Rook's rescue): the rescued Rook reached his slot at the escort swap's end
     * ({@link SimEvents.Type#ESCORT_JOINED}); once. No subject (the radio event {@code escort-joined}: his first line).
     */
    ESCORT_JOINED,
    /**
     * M9 part E (design/campaign Level 32, The forks): a fork's branch was chosen ({@link SimEvents.Type#BRANCH_CHOSEN});
     * once per fork. Its subject names the fork (from 1) and the side: {@code 1:left}, {@code 1:right}, {@code 2:left}
     * ... (the radio event {@code branch} with its {@code fork} and {@code side}).
     */
    BRANCH,
    /**
     * M9 part E (design/enemies/ground/strider, The topple): the attempt's first topple crushed a ground unit ({@link
     * SimEvents.Type#TOPPLE_CRUSH} with a value above 0); once. No subject (the radio event {@code topple-crush}).
     */
    TOPPLE_CRUSH,
    /**
     * M9 part G (design/campaign Level 34, The freighter): the ship collected the story pickup ({@link
     * SimEvents.Type#STORY_COLLECTED}); once. No subject (the radio event {@code story-collected}).
     */
    STORY_COLLECTED,
    /**
     * M9 part G: the story pickup left the screen or ran out of time uncollected ({@link SimEvents.Type#STORY_MISSED}:
     * the salvage tug's line); once. No subject (the radio event {@code story-missed}).
     */
    STORY_MISSED;

    /** Between the part names of a {@link #BOSS_PART_DESTROYED} cue's subject. */
    public static final String PART_SEPARATOR = "|";

    /** M5 part G: the share an {@link #ALLY_BELOW} cue's subject names; NaN when it names none. */
    public static double share(String subject) {
        try {
            return Double.parseDouble(subject);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** Whether its cues start again each time the event happens, not once per attempt. */
    public boolean repeats() {
        return this == ALLY_LOST || this == ALLY_HIT;
    }
}
