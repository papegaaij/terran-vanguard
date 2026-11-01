package vanguard.sim;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, the mirror batteries; m10h-gaps.md §1 #4): a boss part behind
 * a mirror panel under the {@code reflect} rule ({@link PartMirrors}; the {@link EnemySpec.Mirror} numbers: its arc,
 * heat, turn rate, down and regrowth times, bounce gap, bounced bullet and the fire angle its gun keeps). The panel
 * rests facing {@code facingDegrees} and turns toward the ship within ±{@code turnDegrees} of that, at the mirror's
 * turn rate.
 *
 * @param part the part behind the panel
 * @param facingDegrees its rest facing, degrees clockwise from straight down the screen (as {@link Enemy#facing()}: the
 *     Ascendant's left face +45, down and to the left; its right face −45)
 * @param turnDegrees how far it turns either way from its rest facing toward the ship (20)
 * @param mirror the reflect rule's numbers
 */
public record PartMirror(int part, double facingDegrees, double turnDegrees, EnemySpec.Mirror mirror) {
    public PartMirror {
        java.util.Objects.requireNonNull(mirror);
        if (part < 0 || !Double.isFinite(facingDegrees) || !(turnDegrees >= 0 && turnDegrees < 180)) {
            throw new IllegalArgumentException("a part mirror is on a part, faces somewhere and turns less than 180°");
        }
    }

    /** Its rest facing, radians clockwise from straight down. */
    double facingRadians() {
        return StrictMath.toRadians(facingDegrees);
    }
}
