package vanguard.game.render;

import java.util.Optional;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M8 part I, step I2 (round 58 F.4, the user: "It needs to be timed exactly when this ship breaches the surface"): the
 * ice's broken lip round the drill shaft's mouth at a level's breakout, where the ship breaks out of the water (Level
 * 28; tools/art/backdrop_l28.py {@code shaft_mouth}: the bore's circle, its radius {@value #RADIUS} px, centred on x
 * {@value #CENTRE_X}, its far edge, the apex, on the breakout line: the ground row the ship's start height passes
 * as the level clock reaches the first section in the air after the water). The ship breaches when the lip, scrolling
 * down, reaches its sprite's nose at its x: earlier high on the screen and at the flanks (the circle curves down
 * there), later low and in the middle. The burst ({@link BreakoutLooks}) and its sound ({@code MawCues}) both start on
 * that step. Presentation only; nothing is allocated per step.
 *
 * @param apexRow the lip's apex, a ground row (the breakout line)
 */
public record BreachLine(double apexRow) {
    /** The bore's radius and centre x (backdrop_l28.py's {@code shaft_mouth}). */
    public static final double RADIUS = 160;

    public static final double CENTRE_X = 240;
    /** The ship sprite's nose above its centre (the 48 px frame's top opaque row). */
    public static final double NOSE = 21;

    /**
     * The lip of a level that leaves the water (its first section in the air after one under water: {@link
     * BreakoutLooks#breakAt}), empty otherwise; its apex the ground row the ship's start height passes then (the
     * backdrop's {@code BREAK_ROW}).
     */
    public static Optional<BreachLine> of(LevelData level) {
        double breakAt = BreakoutLooks.breakAt(level);
        return Double.isNaN(breakAt)
                ? Optional.empty()
                : Optional.of(new BreachLine(level.scrollAt(breakAt) + UnderwaterGrade.SHIP_ROW));
    }

    /** The lip's ground row at {@code x}: the apex in the middle, the circle's centre row at and past its flanks. */
    public double edgeRow(double x) {
        double dx = Math.min(Math.abs(x - CENTRE_X), RADIUS);
        return apexRow - RADIUS + Math.sqrt(RADIUS * RADIUS - dx * dx);
    }

    /** Whether the lip at {@code x} has reached a ship there, its centre {@code y} px above the bottom edge. */
    public boolean breached(double x, double y, double groundScroll) {
        return edgeRow(x) - groundScroll <= y + NOSE;
    }

    /** Whether the whole lip is still above the screen at this scroll (a retry before the breakout forgets the burst). */
    public boolean ahead(double groundScroll) {
        return apexRow - 2 * RADIUS - groundScroll > PlayField.HEIGHT;
    }
}
