package vanguard.content;

/**
 * M11 part A, step A2b: design/world/vrell-space/data.yaml, Vrell space's hazard numbers (design/world/vrell-space,
 * Hazards &amp; set pieces): the single definition of the spore clouds' rules; a level only places and times its clouds
 * ({@code spores}).
 */
public record VrellSpaceData(VrellSpaceData.Spores spores) {
    /**
     * The spore clouds (J13; Level 45).
     *
     * @param slow the share of its speed and acceleration the ship (and Rook) keeps while its centre is inside a cloud
     * @param ease s the slow eases in and out over
     * @param burn s an {@code area} hit or the Plasma Arc takes to burn a cloud away
     * @param most clouds on the screen at once at most (the level's authoring check)
     * @param veil the most a play-plane unit inside a cloud is veiled (the look's)
     * @param scrollFactor the share of the scroll a cloud drifts down with (its {@code high-air} layer's), unless the
     *     level's backdrop gives its own {@code high-air} scroll factor
     * @param radius the smallest and largest radius a level may place, px (120–200 px across)
     * @param shipBand px from the bottom edge: the ship's band, which no cloud rests over longer than {@code
     *     band_seconds}
     * @param bandSeconds s
     */
    public record Spores(
            double slow,
            double ease,
            double burn,
            int most,
            double veil,
            double scrollFactor,
            Span radius,
            double shipBand,
            double bandSeconds) {
        public Spores {
            Check.that(slow > 0 && slow <= 1, "spores.slow: a share in (0, 1], was " + slow);
            Check.notNegative("spores.ease", ease);
            Check.positive("spores.burn", burn);
            Check.positive("spores.most", most);
            Check.that(veil >= 0 && veil <= 1, "spores.veil: a share in [0, 1], was " + veil);
            Check.positive("spores.scroll_factor", scrollFactor);
            Check.positive("spores.radius", radius.min());
            Check.positive("spores.ship_band", shipBand);
            Check.notNegative("spores.band_seconds", bandSeconds);
        }
    }
}
