package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * M11 part A, step A2b, a level's Act 7 keys (design/world/vrell-space, Hazards &amp; set pieces; design/enemies/bosses,
 * Boss rush; design/player/specials, Act 7 specials): the {@code spores} it places, its {@code quiet}, its {@code rush}
 * of boss fights; the flags {@code breather} and {@code beyond_gate} are plain booleans on {@link LevelData}.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Rush}).
 */
public sealed interface VrellBlock permits LevelData {
    /**
     * A spore cloud (Level 45): it enters at {@code t} s on the level clock above the top edge, its centre {@code x} px
     * from the left, {@code radius} px (a cloud is 120–200 px across); the world's numbers ({@link VrellSpaceData}) slow
     * the ship inside it and burn it.
     */
    record SporeCloud(double t, double x, double radius) {
        public SporeCloud {
            Check.notNegative("spores: t", t);
            Check.positive("spores: radius", radius);
        }
    }

    /**
     * The quiet (Level 46; design/world/vrell-space, The quiet): from {@code from} to {@code to} s on the level clock
     * Rook's pick skips the units on the screen until the player first hits one or one first fires.
     */
    record Quiet(double from, double to) {
        public Quiet {
            Check.notNegative("quiet.from", from);
            Check.that(to > from, "quiet: from a time to a later one, was " + from + "–" + to);
        }
    }

    /**
     * M11 part B, step B2, the gate transit (design/world/vrell-space, Hazards &amp; set pieces; Level 43, Layout): from
     * {@code form} s on the level clock the ship and Rook fly in formation toward the ring's centre (no input, no hits);
     * at {@code warp} s the warp of {@code length} s starts; the level ends inside it or at its end.
     */
    record Transit(double form, double warp, double length) {
        public Transit {
            Check.notNegative("transit.form", form);
            Check.that(warp > form, "transit: the warp after the formation, was " + form + " and " + warp);
            Check.positive("transit.length", length);
        }
    }

    /**
     * The boss rush (Level 49; design/enemies/bosses, Boss rush; Q6 a): its {@code bosses} fight one after another, each
     * arriving in a held gate of its own (an arena section of speed 0; the level gives no {@code boss}); between the
     * fights the {@code refill} (armour and shield to full, the special's charges back to the level's start, a cooldown
     * special ready, Rook's armour full); a boss checkpoint at each fight, offered on every difficulty when {@code
     * checkpoint} is {@value #EVERY_DIFFICULTY}, else by the difficulty's {@code boss_checkpoint}; one {@code par} for the
     * level, s from the first boss's bar to the last kill, the gaps included.
     */
    record Rush(boolean refill, String checkpoint, double par, List<SetPiecesBlock.BossPlacement> bosses) {
        /** The checkpoint offered at every fight on every difficulty, hard's included (the rush's exception). */
        public static final String EVERY_DIFFICULTY = "every-difficulty";

        /** The checkpoint by the difficulty's {@code boss_checkpoint} (none on hard), as any boss level's. */
        public static final String BY_DIFFICULTY = "difficulty";

        public Rush {
            bosses = List.copyOf(bosses);
            Check.that(
                    EVERY_DIFFICULTY.equals(checkpoint) || BY_DIFFICULTY.equals(checkpoint),
                    "rush.checkpoint: '" + EVERY_DIFFICULTY + "' or '" + BY_DIFFICULTY + "', was '" + checkpoint + "'");
            Check.positive("rush.par", par);
            Check.that(bosses.size() >= 2, "rush.bosses: two fights or more");
            for (int i = 1; i < bosses.size(); i++) {
                Check.that(bosses.get(i).t() > bosses.get(i - 1).t(), "rush.bosses[" + i + "]: in time order");
            }
        }

        /** Whether the checkpoint is offered on every difficulty ({@value #EVERY_DIFFICULTY}). */
        public boolean everyDifficulty() {
            return EVERY_DIFFICULTY.equals(checkpoint);
        }
    }

    /**
     * M11 part C, step C2 (design/world/vrell-space, living terrain; Level 45): a spawning pit that opens, named {@code
     * id} (a wave's {@code pit}), at {@code x} px from the left on the ground strip, its centre at the top edge when the
     * ground has scrolled {@code at} px.
     */
    record Pit(String id, double x, double at) {
        public Pit {
            Check.that(id != null && !id.isBlank(), "pits: a pit has an id");
            Check.notNegative("pits: at", at);
            Check.that(x >= 0 && x <= vanguard.sim.PlayField.WIDTH, "pits: x inside the field, was " + x);
        }
    }

    /**
     * M11 part C, step C2 (design/campaign Level 45; design/ui/hud, Act 7 additions): {@code bottom_third: true}, the
     * tracker's mark blinks amber while a counted target of the primary is in the field's bottom third.
     */
    record Leaving(boolean bottomThird) {}

    /**
     * M11 part D, step D2 (design/campaign Level 46, the quiet's end; design/enemies/space/husk, the crack): at {@code t}
     * s on the level clock every unit on the screen with an armed crack cracks in turn, the farthest from the ship
     * first, {@code gap} s apart (a difficulty's {@code gap} instead: hard's 0.5), each with its own tell.
     */
    record CrackCue(double t, double gap, Optional<CrackCueChange> easy, Optional<CrackCueChange> hard) {
        public CrackCue {
            Check.notNegative("crack_cue.t", t);
            Check.positive("crack_cue.gap", gap);
        }

        /** Its gap on {@code difficulty}. */
        public double gapOn(Difficulty difficulty) {
            Optional<CrackCueChange> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case MEDIUM -> Optional.empty();
                        case HARD -> hard;
                    };
            return change.flatMap(CrackCueChange::gap).orElse(gap);
        }
    }

    /** A difficulty's crack cue: its {@code gap} instead. */
    record CrackCueChange(Optional<Double> gap) {
        public CrackCueChange {
            gap.ifPresent(g -> Check.positive("crack_cue.gap", g));
        }
    }
}
