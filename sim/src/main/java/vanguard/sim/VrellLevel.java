package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 part A, step A1b: a level's Act 7 parts ({@link LevelScript#vrell()}): the spore clouds (Level 45), the quiet
 * (Level 46) and the boss rush (Level 49); M11 part B, step B1: the gate transit (Level 43). {@link #NONE} for every
 * other level.
 *
 * @param spores the spore clouds ({@link SporeClouds}); empty for none
 * @param quiet the quiet's hold on Rook's pick; empty for none
 * @param rush the boss rush (several boss fights, each in a gate of its own); empty for none
 * @param transit the gate transit at the level's end ({@link GateTransit}); empty for none
 * @param pits M11 part C, step CD1 (Level 45, the living terrain): the spawning pits the level's {@code pit} waves rise
 *     out of ({@link Pits}); empty for none
 * @param crackCue M11 part D, step CD1 (Level 46, the quiet's end): the level's crack cue ({@link CrackCues}); empty
 *     for none
 * @param bottomThird M11 part C, step CD1 (Level 45, the organs): the HUD's leaving blink also counts the primary's
 *     targets in the field's bottom third ({@link Sortie#leaving()}); false for every other level
 * @param bottomBand M11 part F, step F1 (Level 48, the reactor cores' amber blink): the HUD's leaving blink also counts
 *     the secondary's escapes units (its {@code escapes} enemy) on the screen whose centre is within this many px of the
 *     field's bottom edge ({@link Sortie#leaving()}); 0 for every other level
 */
public record VrellLevel(
        Optional<Spores> spores,
        Optional<Quiet> quiet,
        Optional<Rush> rush,
        Optional<Transit> transit,
        Optional<PitSpec> pits,
        Optional<CrackCue> crackCue,
        boolean bottomThird,
        double bottomBand) {
    /** No spore clouds, no quiet, no rush, no transit, no pits, no crack cue. */
    public static final VrellLevel NONE = new VrellLevel(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            false,
            0);

    public VrellLevel {
        java.util.Objects.requireNonNull(spores);
        java.util.Objects.requireNonNull(quiet);
        java.util.Objects.requireNonNull(rush);
        java.util.Objects.requireNonNull(transit);
        java.util.Objects.requireNonNull(pits);
        java.util.Objects.requireNonNull(crackCue);
        if (!(bottomBand >= 0) || bottomBand > PlayField.HEIGHT) {
            throw new IllegalArgumentException("a leaving band lies on the field: " + bottomBand);
        }
    }

    /** M11 part F, step F1: these parts with the leaving blink's bottom band ({@link #bottomBand}; 0: none). */
    public VrellLevel withBottomBand(double bottomBand) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** These parts with another {@code spores}. */
    public VrellLevel withSpores(Optional<Spores> spores) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** These parts with another {@code quiet}. */
    public VrellLevel withQuiet(Optional<Quiet> quiet) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** These parts with another {@code rush}. */
    public VrellLevel withRush(Optional<Rush> rush) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** M11 part B, step B1: these parts with another {@code transit}. */
    public VrellLevel withTransit(Optional<Transit> transit) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** M11 part C, step CD1: these parts with another {@code pits}. */
    public VrellLevel withPits(Optional<PitSpec> pits) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** M11 part D, step CD1: these parts with another {@code crackCue}. */
    public VrellLevel withCrackCue(Optional<CrackCue> crackCue) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /** M11 part C, step CD1: these parts with the leaving blink counting the bottom third's targets, or not. */
    public VrellLevel withBottomThird(boolean bottomThird) {
        return new VrellLevel(spores, quiet, rush, transit, pits, crackCue, bottomThird, bottomBand);
    }

    /**
     * M11 part C, step CD1 (design/world/vrell-space, Hazards &amp; set pieces: living terrain; design/campaign Level 45,
     * Pit swarms): the level's spawning pits. Pit {@code i} lies {@code x} px from the left edge on the ground strip,
     * its centre at the top edge when the ground has scrolled {@code at} px (it scrolls with the ground). A {@code pit}
     * wave ({@link WaveSpec#pit()}) rises out of its pit at its time: the pit tells {@code tellSeconds} before (its
     * lips glow and part), opens and its units rise for {@code riseSeconds} (untouchable and harmless), and it closes
     * {@code closeSeconds} after it opened. A pit whose opening would put a rising unit within {@code holdWithin} px of
     * the ship holds closed until it would not: the whole wave comes late. The other pits are scenery (not here).
     */
    public record PitSpec(
            double tellSeconds, double riseSeconds, double closeSeconds, double holdWithin, List<Pit> pits) {
        public PitSpec {
            pits = List.copyOf(pits);
            if (!(tellSeconds >= 0)
                    || !(riseSeconds > 0)
                    || !(closeSeconds >= riseSeconds)
                    || !(holdWithin >= 0)
                    || pits.isEmpty()) {
                throw new IllegalArgumentException(
                        "the pits tell, rise, close after the rise and hold at a distance: " + pits);
            }
        }
    }

    /** A spawning pit at {@code x} px, its centre at the top edge when the ground has scrolled {@code at} px. */
    public record Pit(double x, double at) {
        public Pit {
            if (!(at >= 0) || !Double.isFinite(x)) {
                throw new IllegalArgumentException("a pit lies on the ground strip: " + x + ", " + at);
            }
        }
    }

    /**
     * M11 part D, step CD1 (design/campaign Level 46, the quiet's end; design/enemies/space/husk, the crack): at {@code
     * t} s on the level clock every unit on the screen with an armed crack ({@link EnemySpec#crack()}) cracks in turn,
     * the farthest from the ship first (the order fixed as the cue starts), {@code gapSeconds} apart, each with its own
     * tell; a unit already cracking, cracked or gone by its turn is skipped.
     */
    public record CrackCue(double t, double gapSeconds) {
        public CrackCue {
            if (!(t >= 0) || !(gapSeconds > 0)) {
                throw new IllegalArgumentException("the crack cue comes at a time, its cracks apart: " + gapSeconds);
            }
        }
    }

    /**
     * The spore clouds (design/world/vrell-space, Hazards & set pieces: spore clouds; J13): each of {@code clouds}
     * enters at its time above the top edge and drifts down with the scroll at {@code scrollFactor} of its speed (its
     * {@code high-air} layer's). While the ship's centre is inside one, its speed and acceleration ease to {@code slow}
     * over {@code easeSeconds} (and back out); Rook the same. An {@code area} hit whose blast touches a cloud or a
     * Plasma Arc strike through it burns it away over {@code burnSeconds} (it shrinks to nothing); other shots pass. A
     * cloud pays nothing. {@code veil} is the most a play-plane unit inside one is veiled (the look's). At most {@code
     * most} on the screen, never overlapping, none resting over the ship's band: the content's level checks.
     */
    public record Spores(
            double slow,
            double easeSeconds,
            double burnSeconds,
            int most,
            double veil,
            double scrollFactor,
            List<Cloud> clouds) {
        public Spores {
            clouds = List.copyOf(clouds);
            if (!(slow > 0 && slow <= 1)
                    || !(easeSeconds >= 0)
                    || !(burnSeconds > 0)
                    || most < 1
                    || !(veil >= 0 && veil <= 1)
                    || !(scrollFactor > 0)) {
                throw new IllegalArgumentException(
                        "spore clouds slow to a share, ease, burn, and drift with the scroll");
            }
            for (int i = 1; i < clouds.size(); i++) {
                if (clouds.get(i).t() < clouds.get(i - 1).t()) {
                    throw new IllegalArgumentException("the spore clouds are in time order: " + clouds);
                }
            }
        }
    }

    /** A spore cloud entering at {@code t} s on the level clock, its centre {@code x} px from the left edge. */
    public record Cloud(double t, double x, double radius) {
        public Cloud {
            if (!(t >= 0) || !(radius > 0)) {
                throw new IllegalArgumentException("a spore cloud enters at a time with a radius: " + radius);
            }
        }
    }

    /**
     * The quiet (design/world/vrell-space, The quiet; Level 46; design/player/wingmen, Rook in Act 7): from {@code from}
     * to {@code to} s on the level clock Rook's pick skips the units on the screen, until the player first hits one or
     * one first fires (then the hold is over for the level's attempt).
     */
    public record Quiet(double from, double to) {
        public Quiet {
            if (!(from >= 0) || !(to > from)) {
                throw new IllegalArgumentException("the quiet runs from a time to a later one: " + from + "–" + to);
            }
        }
    }

    /**
     * The boss rush (design/enemies/bosses, Boss rush; Q6 a; Level 49): the level's bosses fight one after another,
     * each holding a gate of its own. Between the fights (as each next boss's gate halts) the {@code refill}: armour
     * and shield to full, the special's charges back to the level's start, a cooldown special ready, Rook's armour
     * full; a boss checkpoint at each fight ({@code everyDifficulty}: offered on every difficulty, hard's included).
     * One par for the level, {@code parSeconds} from the first boss's arrival to the last's kill.
     */
    public record Rush(boolean refill, boolean everyDifficulty, double parSeconds) {
        public Rush {
            if (!(parSeconds > 0)) {
                throw new IllegalArgumentException("the rush has a par: " + parSeconds);
            }
        }
    }

    /**
     * M11 part B, step B1: the gate transit (design/world/vrell-space, Hazards & set pieces: gate transit; Q11 a;
     * Level 43, Layout): from {@code form} s on the level clock the ship and Rook fly in formation toward the ring's
     * centre, scripted (no input, no hits, Rook no AI and no gun); at {@code warp} s the warp of {@code length} s
     * starts (the renderer's post-effect). The level's {@code reach-end} lies inside or at the warp's end (the
     * content's check).
     */
    public record Transit(double form, double warp, double length) {
        public Transit {
            if (!(form >= 0) || !(warp > form) || !(length > 0)) {
                throw new IllegalArgumentException(
                        "the transit forms up, then warps for a time: " + form + ", " + warp + ", " + length);
            }
        }
    }
}
