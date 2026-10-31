package vanguard.content;

import java.util.Optional;

/**
 * M10 part A: design/world/jovian/data.yaml, the Jovian system's hazard numbers (design/world/jovian, Hazards &amp; set
 * pieces): the single definition of the told lightning strikes, the dry eruptions and the foreground cloud banks' caps;
 * a level only places and times them ({@code weather.strikes}, {@code eruptions}, {@code backdrop.banks}).
 */
public record JovianData(JovianData.Strike strike, JovianData.Eruption eruption, JovianData.Banks banks) {
    /**
     * The lightning strike (J3).
     *
     * @param tell s the column is told (the brightening line) before it strikes
     * @param live s the strike lasts down the whole play field
     * @param width the column's width, px
     * @param ship damage to the ship and Rook, once a strike each ({@code heavy})
     * @param enemy damage to every {@code air} enemy in the column as it strikes
     * @param entry s around a wave's entry within which a tell never starts on the ship's x (the simulation's shift, and
     *     the level's authoring check)
     * @param spacing s: never two strikes' tells closer than this on one difficulty (the level's authoring check)
     * @param easyClearance px: on easy a column is never told within this of the ship's x
     */
    public record Strike(
            double tell,
            double live,
            double width,
            double ship,
            double enemy,
            double entry,
            double spacing,
            double easyClearance) {
        public Strike {
            Check.positive("tell", tell);
            Check.positive("live", live);
            Check.positive("width", width);
            Check.notNegative("ship", ship);
            Check.notNegative("enemy", enemy);
            Check.notNegative("entry", entry);
            Check.notNegative("spacing", spacing);
            Check.notNegative("easy_clearance", easyClearance);
        }
    }

    /**
     * The dry eruption (J3): a vent's tell, its rocks' rise, flight in a fan up the screen and fall, the rocks' box, HP
     * and contact damage, the period between eruptions and the rocks a throw; {@code easy} / {@code hard} another tell,
     * rock count or start speed.
     *
     * @param tell s the vent's mouth glows before it throws
     * @param rise s a rock rises over the vent (harmless)
     * @param fly s a rock flies out on the play plane
     * @param fall s a rock falls back (harmless)
     * @param fan the fan's width, degrees, centred straight up the screen
     * @param speed px/s a rock flies out at
     * @param endSpeed px/s it eases to by the flight's end
     * @param rock a rock's hit box, {@code [width, height]} px
     * @param hp a rock's HP (no act or beam-era factor)
     * @param damage a rock's contact damage to the ship and Rook
     * @param period {@code [min, max]} s between a vent's eruptions while on the screen
     * @param rocks {@code [min, max]} rocks a throw
     */
    public record Eruption(
            double tell,
            double rise,
            double fly,
            double fall,
            double fan,
            double speed,
            double endSpeed,
            Size rock,
            double hp,
            double damage,
            Span period,
            Span rocks,
            Optional<EruptionChange> easy,
            Optional<EruptionChange> hard) {
        public Eruption {
            Check.positive("tell", tell);
            Check.notNegative("rise", rise);
            Check.positive("fly", fly);
            Check.notNegative("fall", fall);
            Check.that(fan >= 0 && fan < 180, "fan: degrees up the screen, 0-180");
            Check.positive("speed", speed);
            Check.positive("end_speed", endSpeed);
            Check.positive("hp", hp);
            Check.notNegative("damage", damage);
            Check.positive("period", period.min());
            Check.that(rocks.min() >= 1 && rocks.min() == Math.rint(rocks.min()), "rocks: whole rocks, at least 1");
            Check.that(rocks.max() == Math.rint(rocks.max()), "rocks: whole rocks");
        }

        private Optional<EruptionChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }

        /** The tell on {@code difficulty}, s. */
        public double tellOn(Difficulty difficulty) {
            return change(difficulty).flatMap(EruptionChange::tell).orElse(tell);
        }

        /** The rocks a throw on {@code difficulty}, {@code [min, max]}. */
        public Span rocksOn(Difficulty difficulty) {
            return change(difficulty).flatMap(EruptionChange::rocks).orElse(rocks);
        }

        /** The start speed on {@code difficulty}, px/s. */
        public double speedOn(Difficulty difficulty) {
            return change(difficulty).flatMap(EruptionChange::speed).orElse(speed);
        }
    }

    /** A difficulty's eruption: another {@code tell} s, {@code rocks} {@code [min, max]} or start {@code speed} px/s. */
    public record EruptionChange(Optional<Double> tell, Optional<Span> rocks, Optional<Double> speed) {
        public EruptionChange {
            tell.ifPresent(s -> Check.positive("tell", s));
            rocks.ifPresent(r -> Check.that(r.min() >= 1, "rocks: at least 1"));
            speed.ifPresent(s -> Check.positive("speed", s));
        }
    }

    /**
     * The foreground cloud banks' caps (Q2): at most {@code cover} of the play field's area under banks at a time
     * ({@code easyCover} on easy), never a bank over the ship's band (the bottom {@code band} px) for more than {@code
     * bandSeconds} s at a time.
     */
    public record Banks(double cover, double easyCover, double band, double bandSeconds) {
        public Banks {
            Check.share("cover", cover);
            Check.share("easy_cover", easyCover);
            Check.positive("band", band);
            Check.positive("band_seconds", bandSeconds);
        }

        /** The cover cap on {@code difficulty}, a share of the field's area. */
        public double coverOn(Difficulty difficulty) {
            return difficulty == Difficulty.EASY ? easyCover : cover;
        }
    }
}
