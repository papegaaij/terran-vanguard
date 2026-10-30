package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import vanguard.sim.Layer;

/**
 * The level's {@code radio} chatter and its {@code prompts}: the cues, what triggers them, and the HUD's contextual
 * prompts.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.RadioLine}).
 */
public sealed interface RadioBlock permits LevelData {
    /**
     * A spoken line; {@code distorted} for transmissions such as the Choir's.
     *
     * @param expression the speaker's portrait expression, neutral when not given
     * @param requires M9 part F (Level 33's secret, Rook's line): what has to be fitted for it to play ({@code escort}:
     *     Rook flying), as a radio cue's {@code requires}
     */
    public record RadioLine(
            String speaker,
            String line,
            Optional<Boolean> distorted,
            Optional<Expression> expression,
            Optional<String> portrait,
            Optional<String> requires) {
        public RadioLine {
            requires.ifPresent(Requirement::check);
            Expression.checkSpeaker(speaker, expression);
        }

        /** Without M7 part B's generic portrait. */
        public RadioLine(String speaker, String line, Optional<Boolean> distorted, Optional<Expression> expression) {
            this(speaker, line, distorted, expression, Optional.empty());
        }

        /** Without M9 part F's requirement. */
        public RadioLine(
                String speaker,
                String line,
                Optional<Boolean> distorted,
                Optional<Expression> expression,
                Optional<String> portrait) {
            this(speaker, line, distorted, expression, portrait, Optional.empty());
        }
    }

    /**
     * A radio chatter cue, triggered at {@code t} seconds or by an {@code event}.
     *
     * @param enemy the enemy of a {@code first-kill} or {@code enemy-escaped} event
     * @param expression the speaker's portrait expression, neutral when not given
     * @param shout whether the voice shouts the line (design/audio/voice), apart from how it queues
     * @param easy changes on easy
     * @param hard changes on hard
     * @param requires what has to be fitted for it to play: {@code special} or (part G)
     *     {@code homing}, a weapon with homing delivery, or (M5 part C) {@code escort}: an escort
     *     flying (hired, fitted and not ejected; Rook's scripted lines), asked when it is due
     * @param requiresNot part G: what must not be fitted for it to play (Level 07's "You can't touch
     *     it up there" without a homing weapon)
     * @param timeout part G, a boss-phase cue: true plays it only when the phase before ended on
     *     its timeout with parts it waited for still alive (Level 07's "Forget the sacs")
     * @param below M5 part G, an {@code ally-below} cue: the share of the defended ally's integrity it
     *     dropped below (Level 13's 0.5 and 0.25)
     * @param delay round 45 B.a, an event cue: the seconds (at least 0) the radio holds the line after its event before
     *     it plays, the lines queued after it waiting behind it (Level 21's Choir under the Colossus's death roar)
     * @param notBefore step FIX6 (the user's playthrough 2026-10-30, Level 33's leech line), an event cue: the level
     *     second the radio holds the line until once its event came (Level 33's "Field's clean" in the calm before the
     *     end, not as the secondary turns safe while more leeches come); none if not given
     * @param staticLead M8 part F (design/campaign Level 26, The Choir; m8f-gaps.md §1.5): the HUD portrait frame's
     *     interference shows this many seconds before a timed line plays (Level 26's Choir, 2 s); none if not given
     * @param duckDb M8 part F: the music ducks this many dB under the line while it plays (Level 26's Choir, 6); none
     *     if not given
     * @param channel M9 part A (design/ui/hud, Act 5 additions): the channel the line comes over, overriding its
     *     speaker's ({@link #channel(VoiceData)})
     * @param swap M9 part B, step B4b: a {@code holders-down} cue in a level with an escort swap names what the swap
     *     does, the one it plays for: {@code bulwark} (Rook joins, the Bulwark in the slot still flies and peels away),
     *     {@code joins} (Rook joins, no Bulwark flying: the loaner lost), {@code leaves} (no Rook to join: the Ember
     *     leaves upward), {@code empty} (Rook flew from the start: the hold opens empty)
     */
    public record RadioCue(
            Optional<Double> t,
            Optional<CueEvent> event,
            Optional<String> enemy,
            Optional<String> group,
            String speaker,
            Optional<String> portrait,
            String line,
            Optional<Boolean> distorted,
            Optional<Expression> expression,
            Optional<Boolean> shout,
            Optional<RadioChange> easy,
            Optional<RadioChange> hard,
            Optional<String> requires,
            Optional<Count> allies,
            Optional<String> phase,
            Optional<String> requiresNot,
            Optional<Boolean> timeout,
            Optional<List<String>> parts,
            Optional<Double> below,
            Optional<Double> delay,
            Optional<Double> notBefore,
            Optional<Double> staticLead,
            Optional<Double> duckDb,
            Optional<Channel> channel,
            Optional<String> swap,
            Optional<Integer> fork,
            Optional<String> side) {
        /** Step B4b: the escort swap outcomes a {@code holders-down} cue's {@code swap} names. */
        public static final List<String> SWAPS = List.of(
                vanguard.sim.EscortSwap.SUBJECT_BULWARK,
                vanguard.sim.EscortSwap.SUBJECT_JOINS,
                vanguard.sim.EscortSwap.SUBJECT_LEAVES,
                vanguard.sim.EscortSwap.SUBJECT_EMPTY);

        public RadioCue {
            Expression.checkSpeaker(speaker, expression);
            // M9 part E (design/campaign Level 32, The forks): a branch cue names its fork (from 1) and side.
            Check.that(
                    (fork.isPresent() && side.isPresent()) == (event.orElse(null) == CueEvent.BRANCH),
                    "a branch event names its fork and side, other triggers do not");
            side.ifPresent(
                    s -> Check.that(s.equals("left") || s.equals("right"), "side: left or right, was '" + s + "'"));
            fork.ifPresent(f -> Check.that(f >= 1, "fork: from 1, was " + f));
            Check.that(
                    swap.isEmpty() || event.orElse(null) == CueEvent.HOLDERS_DOWN,
                    "only a holders-down event names an escort swap's outcome (swap)");
            swap.ifPresent(outcome ->
                    Check.that(SWAPS.contains(outcome), "swap: one of " + SWAPS + ", was '" + outcome + "'"));
            staticLead.ifPresent(seconds -> {
                Check.that(t.isPresent(), "static_lead: only on a timed line");
                Check.that(seconds > 0 && seconds <= 10, "static_lead: above 0 and at most 10 s, was " + seconds);
            });
            duckDb.ifPresent(db -> Check.that(db > 0 && db <= 24, "duck_db: above 0 and at most 24 dB, was " + db));
            Check.that(delay.isEmpty() || event.isPresent(), "only an event cue waits a delay");
            delay.ifPresent(seconds ->
                    Check.that(Double.isFinite(seconds) && seconds >= 0, "delay: seconds, at least 0, was " + seconds));
            Check.that(
                    notBefore.isEmpty() || event.isPresent(),
                    "only an event cue waits for a level second (not_before)");
            notBefore.ifPresent(seconds -> Check.that(
                    Double.isFinite(seconds) && seconds >= 0,
                    "not_before: a level second, at least 0, was " + seconds));
            Check.that(
                    below.isPresent() == (event.orElse(null) == CueEvent.ALLY_BELOW),
                    "an ally-below event names its share (below), other triggers do not");
            below.ifPresent(share -> Check.that(share > 0 && share < 1, "below: a share between 0 and 1"));
            Check.that(
                    parts.isPresent() == (event.orElse(null) == CueEvent.BOSS_PART_DESTROYED),
                    "a boss-part-destroyed event names its parts, other triggers do not");
            parts.ifPresent(list -> Check.notEmpty("parts", list));
            Check.that(
                    phase.isPresent() == (event.orElse(null) == CueEvent.BOSS_PHASE),
                    "a boss-phase event names its phase, other triggers do not");
            Check.that(t.isPresent() != event.isPresent(), "give the trigger as t or as event");
            requires.ifPresent(Requirement::check);
            requiresNot.ifPresent(Requirement::check);
            Check.that(
                    requires.isEmpty() || !requires.equals(requiresNot),
                    "requires and requires_not name different things");
            Check.that(
                    timeout.isEmpty() || event.orElse(null) == CueEvent.BOSS_PHASE,
                    "only a boss-phase cue waits for a timeout");
            Check.that(
                    allies.isEmpty() || event.orElse(null) == CueEvent.LEVEL_END,
                    "only a level-end cue names the allies home");
            // M9 part B (Level 29, Hold fire): a neutral craft's event names its kind too.
            boolean byEnemy = event.orElse(null) == CueEvent.FIRST_KILL
                    || event.orElse(null) == CueEvent.ENEMY_ESCAPED
                    || event.orElse(null) == CueEvent.NEUTRAL_LEFT
                    || event.orElse(null) == CueEvent.NEUTRAL_DOWN;
            Check.that(
                    enemy.isPresent() == byEnemy,
                    "a first-kill, enemy-escaped, neutral-left or neutral-down event names its enemy, other triggers do"
                            + " not");
            boolean byGroup = event.orElse(null) == CueEvent.GROUP_CLEARED || event.orElse(null) == CueEvent.GROUP_LOST;
            Check.that(
                    group.isPresent() == byGroup,
                    "a group-cleared or group-lost event names its group, other triggers do not");
        }

        /**
         * M9 part A: the channel the line comes over: its own {@code channel}, else its speaker's in the voice table
         * ({@code filter: enemy}: Vorne, Harker); friendly by default.
         */
        public Channel channel(VoiceData voices) {
            return channel.orElseGet(() -> voices.channelOf(speaker));
        }

        /** M8 part F: the portrait's interference before the line, s: its {@code static_lead}; 0 if not given. */
        public double staticLeadSeconds() {
            return staticLead.orElse(0.0);
        }

        /** M8 part F: how far the music ducks under the line, dB (positive): its {@code duck_db}; 0 if not given. */
        public double musicDuckDb() {
            return duckDb.orElse(0.0);
        }

        /** Round 45 B.a: the seconds the radio holds the line after its event, its {@code delay}; 0 if not given. */
        public double delaySeconds() {
            return delay.orElse(0.0);
        }

        /** Step FIX6: the level second the radio holds the line until after its event, its {@code not_before}; 0 if not given. */
        public double notBeforeSeconds() {
            return notBefore.orElse(0.0);
        }
    }

    /** A whole-number range, written {@code [min, max]}. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record Count(int min, int max) {
        public Count {
            Check.that(
                    0 <= min && min <= max,
                    "a count range is written [min, max] from 0, was [" + min + ", " + max + "]");
        }
    }

    /** A difficulty's changes to a radio cue: another line, as when a wave enters elsewhere. */
    public record RadioChange(String line) {}

    public enum CueEvent {
        @JsonProperty("first-kill")
        FIRST_KILL,
        @JsonProperty("secondary-objective")
        SECONDARY_OBJECTIVE,
        @JsonProperty("level-end")
        LEVEL_END,
        @JsonProperty("group-cleared")
        GROUP_CLEARED,
        @JsonProperty("group-lost")
        GROUP_LOST,
        @JsonProperty("first-group-lost")
        FIRST_GROUP_LOST,
        /** The first unit of the enemy left the screen alive (a set piece: at the end of its last pass). */
        @JsonProperty("enemy-escaped")
        ENEMY_ESCAPED,
        /** The first hit on a convoy unit; the line may name it as {@code {ally}} ("Three"). */
        @JsonProperty("first-ally-hit")
        FIRST_ALLY_HIT,
        /** The first convoy unit lost; the line may name it as {@code {ally}}. */
        @JsonProperty("first-ally-lost")
        FIRST_ALLY_LOST,
        /**
         * M5 part B (design/player/wingmen, Scripted lines about him): the first kill in the attempt
         * whose killing shot or blast is the escort's (Rook's), while he flies; never after he ejects,
         * nor in a level he does not fly in. Names no enemy.
         */
        @JsonProperty("escort-first-kill")
        ESCORT_FIRST_KILL,
        /** The primary objective failed: the line on the mission failed screen (not on the radio). */
        @JsonProperty("mission-failed")
        MISSION_FAILED,
        /** The boss entered a later phase; the cue names the phase as {@code phase}. */
        @JsonProperty("boss-phase")
        BOSS_PHASE,
        /** The boss was destroyed. */
        @JsonProperty("boss-destroyed")
        BOSS_DESTROYED,
        /** M5 part C: the level's first hold zone starts to ease down (once). */
        @JsonProperty("hold-start")
        HOLD_START,
        /** M5 part C: the attempt's first pounce takes off. Names no enemy. */
        @JsonProperty("first-pounce")
        FIRST_POUNCE,
        /** M5 part C: the collapse's shadow starts. */
        @JsonProperty("collapse")
        COLLAPSE,
        /**
         * M5 part D: every loss of a convoy unit the player could have saved (the first too, after the
         * {@code first-ally-lost} cue); the line may name it as {@code {ally}}.
         */
        @JsonProperty("ally-lost")
        ALLY_LOST,
        /** M5 part D: the scripted loss's lance took its unit. */
        @JsonProperty("scripted-loss")
        SCRIPTED_LOSS,
        /** M5 part D: the attempt's first decloak (a cloaked enemy). */
        @JsonProperty("first-decloak")
        FIRST_DECLOAK,
        /** M5 part D: the attempt's first swarm loop-back. */
        @JsonProperty("first-loop-back")
        FIRST_LOOP_BACK,
        /**
         * M5 part E: each convoy unit's first hit (it starts again for each unit, as {@code
         * ally-lost}); the line may name it as {@code {ally}}, a naval convoy unit by its name.
         */
        @JsonProperty("ally-hit")
        ALLY_HIT,
        /** M5 part E: the attempt's first lane telegraph of the level's arena boss (once). */
        @JsonProperty("first-telegraph")
        FIRST_TELEGRAPH,
        /**
         * Round 40 (the reworked Harbour Kraken): the attempt's first lane telegraph of the level's arena boss on a
         * lane holding a convoy ship a slam hurts (the teaching line: cut that lane's root); once.
         */
        @JsonProperty("first-ship-telegraph")
        FIRST_SHIP_TELEGRAPH,
        /** M5 part E: the first of the level boss's {@code parts} the cue names shot off (once). */
        @JsonProperty("boss-part-destroyed")
        BOSS_PART_DESTROYED,
        /** M5 part F: the attempt's first latch of a Lamprey (once); a scripted escort line for the bark spacing. */
        @JsonProperty("first-latch")
        FIRST_LATCH,
        /**
         * M5 part G: the defend primary's ally dropped below the cue's {@code below} share of its integrity
         * for the first time in the attempt (Level 13's relay below 50 % and 25 %; once each).
         */
        @JsonProperty("ally-below")
        ALLY_BELOW,
        /** M7 part B: the attempt's first Burrower breaking the surface (once); only with a burrowing enemy. */
        @JsonProperty("first-surfacing")
        FIRST_SURFACING,
        /**
         * M7 part C (design/campaign Level 16, Radio chatter): the attempt's first touch of rock by the ship (once);
         * it plays in the next free gap or goes stale; only in a level with {@code terrain}.
         */
        @JsonProperty("first-wall-contact")
        FIRST_WALL_CONTACT,
        /**
         * M7 part D, round 40 K.a (design/campaign Level 17, Radio chatter): the attempt's first aura ring a Choir
         * Herald sings onto a unit, as soon as it is on the play field with its escorts round it (once); only in a
         * level with an aura enemy.
         */
        @JsonProperty("first-ring")
        FIRST_RING,
        /**
         * M8 part B (design/campaign Level 22, Radio chatter): the attempt's first vent tell (a geyser's crack starting
         * to glow; once); only in a level with {@code vents}.
         */
        @JsonProperty("first-geyser")
        FIRST_GEYSER,
        /**
         * M8 part B (design/campaign Level 22, Radio chatter): the attempt's first unit of the secondary's {@code
         * escapes} enemy destroyed (Level 22's first rig core; once); only with an escapes secondary.
         */
        @JsonProperty("target-destroyed")
        TARGET_DESTROYED,
        /**
         * M8 part C (design/campaign Level 23, Radio chatter; gap check D5): the waterline crossed, the start of the
         * level's first section whose medium differs from the one before it; content resolves it to that time on the
         * level clock, so it plays as a timed line (no simulation event); only in a level with a waterline.
         */
        @JsonProperty("waterline")
        WATERLINE,
        /**
         * M8 part D (design/campaign Level 24, Radio chatter; gap check m8d-gaps.md §1.5, §5 D2): the attempt's first
         * Abyss Ray's shadow (the first unit with a {@code rise} entering, its wave's time: the README's t≈57.6 is the
         * same beat); once; only in a level with a rising unit. The simulation's sighting ({@code CueTrigger.FIRST_RAY});
         * its time for the radio timeline is the first rising wave's ({@code AbyssChecks.firstRise}).
         */
        @JsonProperty("first-ray")
        FIRST_RAY,
        /**
         * M8 part H (design/campaign Level 28; gap check m8h-gaps.md §5 H8): the level's boss arrives (its bar; the
         * arena's clock is halted there, so a timed line cannot follow it); once; only in a level with a boss. The
         * simulation's {@code CueTrigger.BOSS_ARRIVAL}.
         */
        @JsonProperty("boss-arrival")
        BOSS_ARRIVAL,
        /**
         * M8 part H (design/enemies/bosses/abyssal-maw, the dive): the level's coiled boss dives; once; only in a level
         * with a boss. The simulation's {@code CueTrigger.BOSS_DIVE}; the presentation queues it as a phase line.
         */
        @JsonProperty("boss-dive")
        BOSS_DIVE,
        /** M9 part B (design/campaign Level 29): the attempt's first enemy homing missile launched; once. */
        @JsonProperty("first-homing")
        FIRST_HOMING,
        /** M9 part B (Level 29, Hold fire): the first neutral craft of its {@code enemy} left the screen unharmed. */
        @JsonProperty("neutral-left")
        NEUTRAL_LEFT,
        /** M9 part B (Level 29, Hold fire): the first neutral craft of its {@code enemy} destroyed. */
        @JsonProperty("neutral-down")
        NEUTRAL_DOWN,
        /** M9 part B (Level 29, the rescue halt): the held arena's last holder fell; once. */
        @JsonProperty("holders-down")
        HOLDERS_DOWN,
        /** M9 part B (design/player/wingmen, Rook's rescue): the rescued Rook reached his slot; once. */
        @JsonProperty("escort-joined")
        ESCORT_JOINED,
        /** M9 part E (design/campaign Level 32, The forks): its {@code fork}'s {@code side} was chosen; once a fork. */
        @JsonProperty("branch")
        BRANCH,
        /** M9 part E (design/enemies/ground/strider, The topple): the attempt's first topple crushed a ground unit. */
        @JsonProperty("topple-crush")
        TOPPLE_CRUSH,
        /** M9 part G (design/campaign Level 34, The freighter): the ship collected the story pickup; once. */
        @JsonProperty("story-collected")
        STORY_COLLECTED,
        /** M9 part G: the story pickup left the screen or ran out uncollected (the salvage tug's line); once. */
        @JsonProperty("story-missed")
        STORY_MISSED,
        /**
         * Step BOSS70 (design/enemies/bosses/iron-sovereign, the core's playbook): the open core's first rail volley
         * drawn (its tell); once; only with a ringed boss whose pool has it.
         */
        @JsonProperty("first-rail-volley")
        FIRST_RAIL_VOLLEY,
        /** Step BOSS70: the open core's first mine ring drawn; once. */
        @JsonProperty("first-mine-ring")
        FIRST_MINE_RING,
        /** Step BOSS70: the open core's first wreck throw drawn; once. */
        @JsonProperty("first-wreck-throw")
        FIRST_WRECK_THROW,
        /** Step BOSS70: the open core's first draw below its playbook's low share (its rests shorten); once. */
        @JsonProperty("core-low")
        CORE_LOW
    }

    /**
     * A contextual prompt (design/ui/hud, control prompts): shown from {@code t} for {@code seconds}
     * in the prompts' well, the {@code action} on the left and its {@code keys} on the right.
     *
     * @param skip a layer: the prompt leaves early, done what it says, once an enemy on that layer
     *     is destroyed (Level 03's {@code LOW-AIR} prompt); without it, it stays for its seconds
     */
    public record Prompt(
            double t,
            String action,
            String keys,
            double seconds,
            Optional<String> skip,
            Optional<String> requires,
            Optional<PromptEvent> event) {
        public Prompt {
            Check.that(event.isPresent() == Double.isNaN(t), "a prompt is shown at a t or on an event, not both");
            if (event.isEmpty()) {
                Check.notNegative("t", t);
            }
            Check.positive("seconds", seconds);
            skip.ifPresent(Layers::of);
            requires.ifPresent(Requirement::checkPrompt);
        }

        /** A timed prompt. */
        public Prompt(
                double t,
                String action,
                String keys,
                double seconds,
                Optional<String> skip,
                Optional<String> requires) {
            this(t, action, keys, seconds, skip, requires, Optional.empty());
        }

        /**
         * From the data: a timed prompt's {@code t}, or (M5 part F) an event prompt's {@code event}
         * instead, its {@code t} then NaN (it is never due by the clock).
         */
        @com.fasterxml.jackson.annotation.JsonCreator
        public static Prompt of(
                @JsonProperty("t") Optional<Double> t,
                @JsonProperty("action") String action,
                @JsonProperty("keys") String keys,
                @JsonProperty("seconds") double seconds,
                @JsonProperty("skip") Optional<String> skip,
                @JsonProperty("requires") Optional<String> requires,
                @JsonProperty("event") Optional<PromptEvent> event) {
            Optional<Double> time = t == null ? Optional.empty() : t;
            Optional<PromptEvent> on = event == null ? Optional.empty() : event;
            Check.that(time.isPresent() != on.isPresent(), "a prompt has its t or its event");
            return new Prompt(
                    time.orElse(Double.NaN),
                    action,
                    keys,
                    seconds,
                    skip == null ? Optional.empty() : skip,
                    requires == null ? Optional.empty() : requires,
                    on);
        }

        /** M5 part F: whether it is shown on an event (the shake-off prompt) rather than at its time. */
        public boolean onEvent() {
            return event.isPresent();
        }

        /** Whether it shows only with a special fitted (Level 04's {@code SPECIAL} · {@code CALL HAMMER}). */
        public boolean requiresSpecial() {
            return requires.isPresent();
        }

        /** The layer whose first destroyed enemy makes the prompt leave, if any. */
        public Optional<Layer> skipLayer() {
            return skip.map(Layers::of);
        }
    }

    /** M5 part F: the run-time events a prompt can be shown on (until the event ends or for its seconds). */
    public enum PromptEvent {
        /** The attempt's first latch of a Lamprey, shown until the latch ends. */
        @JsonProperty("first-latch")
        FIRST_LATCH
    }
}
