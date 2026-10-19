package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * design/allies/data.yaml: the allies' specs, one entry per ally slug; the simulation flies them as
 * an escort objective's convoy (Level 04's crawlers on the road, M5 part D's shuttles in the air).
 *
 * @param allies the allies by slug
 */
public record AlliesData(Map<String, Ally> allies) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public AlliesData {
        allies = Map.copyOf(new TreeMap<>(allies));
    }

    /**
     * An ally unit.
     *
     * @param size the sprite, px
     * @param hp at medium; the level sets the difficulty variants
     * @param follows what it moves along ({@code road}: the level's road at the scroll speed; M5
     *     part D, {@code lanes}: its station in the level's band with the lane sway; M5 part E, {@code
     *     stations}: a naval convoy's; M5 part G, {@code ground}: a defend primary's ally, standing on the
     *     ground)
     * @param smokeBelow the share of its HP under which it smokes
     * @param firstLevel the level it is introduced in, optional: an ally no level's data escorts yet
     *     (its art lands before its level) goes in that level's sprite atlas
     * @param banks M5 part D, presentation: its rendered banking frames
     * @param glide M5 part D, presentation: s of a lost unit's glide into {@code far}
     * @param flak M5 part E, presentation (the escort frigate): its flak bursts over the convoy
     * @param hits M7 part D (design/allies, Ares Landing dome): the hits it breaks on, given instead of {@code hp}
     *     (its {@code hp} is then this count): every attack its {@code damaged_by} names deals the hits given there, a
     *     hooked lob's direct hit one; empty for an ally with HP
     * @param sprite M7 part D, presentation: its sprites' base name (the Ares Landing dome's {@code ares-dome}); the
     *     slug if not given
     */
    public record Ally(
            String name,
            String layer,
            Size size,
            Size hitbox,
            double hp,
            DamagedBy damagedBy,
            String follows,
            Headings headings,
            double smokeBelow,
            Optional<Integer> firstLevel,
            Optional<Banks> banks,
            Optional<Double> glide,
            Optional<Flak> flak,
            Optional<Integer> hits,
            Optional<String> sprite) {
        /** What a ground ally follows. */
        public static final String ROAD = "road";
        /** M5 part D: what an air ally follows, its station in the level's band. */
        public static final String LANES = "lanes";
        /**
         * M5 part E: what a naval convoy's ally follows (a level's {@code convoy} block): its
         * screen-space station at the scroll speed, its arena lane at the halt.
         */
        public static final String STATIONS = "stations";
        /**
         * M5 part G: what a defend primary's ally follows (Level 13's Nansen Relay): the ground, with which
         * it scrolls in and where it stands while the ground halts.
         */
        public static final String GROUND = "ground";

        /** Without M7 part D's hits and sprite. */
        public Ally(
                String name,
                String layer,
                Size size,
                Size hitbox,
                double hp,
                DamagedBy damagedBy,
                String follows,
                Headings headings,
                double smokeBelow,
                Optional<Integer> firstLevel,
                Optional<Banks> banks,
                Optional<Double> glide,
                Optional<Flak> flak) {
            this(
                    name,
                    layer,
                    size,
                    hitbox,
                    hp,
                    damagedBy,
                    follows,
                    headings,
                    smokeBelow,
                    firstLevel,
                    banks,
                    glide,
                    flak,
                    Optional.empty(),
                    Optional.empty());
        }

        /** M7 part D: an ally as written, with its {@code hp} or (the Ares Landing dome) its {@code hits}. */
        @JsonCreator
        static Ally of(
                @JsonProperty("name") String name,
                @JsonProperty("layer") String layer,
                @JsonProperty("size") Size size,
                @JsonProperty("hitbox") Size hitbox,
                @JsonProperty("hp") Optional<Double> hp,
                @JsonProperty("damaged_by") DamagedBy damagedBy,
                @JsonProperty("follows") String follows,
                @JsonProperty("headings") Headings headings,
                @JsonProperty("smoke_below") double smokeBelow,
                @JsonProperty("first_level") Optional<Integer> firstLevel,
                @JsonProperty("banks") Optional<Banks> banks,
                @JsonProperty("glide") Optional<Double> glide,
                @JsonProperty("flak") Optional<Flak> flak,
                @JsonProperty("hits") Optional<Integer> hits,
                @JsonProperty("sprite") Optional<String> sprite) {
            Check.that(hp.isPresent() != hits.isPresent(), "give the ally's hp or (M7 part D) its hits, one of them");
            return new Ally(
                    name,
                    layer,
                    size,
                    hitbox,
                    hp.orElseGet(() -> (double) hits.orElseThrow()),
                    damagedBy,
                    follows,
                    headings,
                    smokeBelow,
                    firstLevel,
                    banks,
                    glide,
                    flak,
                    hits,
                    sprite);
        }

        public Ally {
            Layers.of(layer);
            Check.positive("hp", hp);
            hits.ifPresent(n -> {
                Check.positive("hits", n);
                Check.that(hp == n, "hits: its hp counts the hits");
            });
            sprite.ifPresent(base -> Check.that(!base.isBlank(), "sprite: a sprite name"));
            Check.share("smoke_below", smokeBelow);
            firstLevel.ifPresent(level -> Check.positive("first_level", level));
            Check.that(
                    follows.equals(ROAD) || follows.equals(LANES) || follows.equals(STATIONS) || follows.equals(GROUND),
                    "follows: 'road', 'lanes', 'stations' or 'ground', was '" + follows + "'");
            boolean air = follows.equals(LANES);
            boolean naval = follows.equals(STATIONS);
            boolean standing = follows.equals(GROUND);
            Check.that(
                    standing
                            || (damagedBy.dives().isEmpty()
                                    && damagedBy.lobs().isEmpty()
                                    && damagedBy.bombs().isEmpty()
                                    && damagedBy.rolls().isEmpty()),
                    "only a ground ally (follows: ground) is hurt by dives, lobs, bomb runs and rolls");
            Check.that(
                    hits.isPresent()
                            == (damagedBy.bombs().isPresent()
                                    || damagedBy.rolls().isPresent()),
                    "an ally hurt by bomb runs or rolls counts hits (hits), one that counts hits is hurt by them");
            Check.that(
                    !standing
                            || (damagedBy.claws() == 0
                                    && !damagedBy.hitByBullets()
                                    && !damagedBy.hitByContact()
                                    && damagedBy.slams().isEmpty()),
                    "a ground ally (follows: ground) is hurt only by objective-aimed attacks, dives, lobs, bomb runs"
                            + " and rolls");
            Check.that(
                    air == layer.equals("air"),
                    "an ally on the air layer follows lanes, one on the ground follows the road or stations");
            Check.that(naval || damagedBy.slams().isEmpty(), "only a naval convoy's ally (stations) is hurt by slams");
            Check.that(
                    !naval
                            || (!damagedBy.objectiveAimed()
                                    && damagedBy.claws() == 0
                                    && !damagedBy.hitByBullets()
                                    && !damagedBy.hitByContact()),
                    "a naval convoy's ally (stations) is hurt only by slams");
            Check.that(naval || flak.isEmpty(), "only a naval convoy's ally (stations) fires flak");
            Check.that(
                    !air || !damagedBy.objectiveAimed() && damagedBy.claws() == 0,
                    "an air ally is hurt by bullets and contact, not by objective-aimed shots or claws");
            Check.that(
                    air || !damagedBy.hitByBullets() && !damagedBy.hitByContact(),
                    "bullets and contact hurt an air ally only (a ground ally lies below the player's plane)");
            glide.ifPresent(seconds -> Check.positive("glide", seconds));
        }

        /** M5 part D: whether it flies in the air (its station in the band), not on the road. */
        public boolean air() {
            return follows.equals(LANES);
        }

        /** M5 part E: whether it sails in a naval convoy (its screen-space station), not on a road. */
        public boolean naval() {
            return follows.equals(STATIONS);
        }

        /** M5 part G: whether it is a defend primary's ally, standing on the ground (Level 13's relay). */
        public boolean standing() {
            return follows.equals(GROUND);
        }

        /** M7 part D: whether it counts hits ({@code hits}) instead of HP (the Ares Landing dome). */
        public boolean countsHits() {
            return hits.isPresent();
        }

        /** M7 part D, presentation: its sprites' base name, its {@code sprite} or the slug. */
        public String spriteName(String slug) {
            return sprite.orElse(slug);
        }
    }

    /** M5 part E, presentation (the escort frigate): a flak burst every {@code every} s, hitting nothing. */
    public record Flak(double every) {
        public Flak {
            Check.positive("every", every);
        }
    }

    /**
     * M5 part D, presentation: its rendered banking frames, {@code frames} of them (odd: level in
     * the middle), the full bank at {@code full} px/s of sideways speed.
     */
    public record Banks(int frames, double full) {
        public Banks {
            Check.positive("frames", frames);
            Check.that(frames % 2 == 1, "frames: an odd number (level in the middle), was " + frames);
            Check.positive("full", full);
        }
    }

    /**
     * What hurts an ally.
     *
     * @param objectiveAimed only enemy shots aimed at it by the target-the-objective hook
     * @param claws damage per second while a walker's hitbox overlaps it
     * @param bullets M5 part D (user decision D2 = a): every enemy bullet touching its hit box hurts
     *     it by its class and is spent
     * @param contact M5 part D: an {@code air} enemy's body hurts it by the enemy's tier once per
     *     contact, a {@code tiny} or {@code small} one destroyed by the impact
     * @param slams M5 part E: the hits one boss slam in its lane deals (its {@code hp} then counts
     *     slams: the cargo ship's {@code hp: 2}, {@code slams: 1}); an ally whose {@code damaged_by}
     *     names nothing (the frigate) cannot be damaged and is never lost
     * @param dives M5 part G (design/allies, Nansen Relay; user decision G1 = a): the damage a diver the
     *     hook sends at it deals once as it crosses its hit box (the relay's 10)
     * @param lobs M5 part G: a mortar's lob the hook aims at it deals its direct hit to it when it lands on
     *     it (the ring hurts only the ship); M7 part D: one hit to an ally that counts hits
     * @param bombs M7 part D (design/allies, Ares Landing dome): the hits a hooked bomber's drop deals as the ally's
     *     footprint passes beneath it (once per bomber, its load spent)
     * @param rolls M7 part D: the hits a rolling unit's roll deals as it crosses the ally's footprint (once per roll;
     *     the Shellback's)
     */
    public record DamagedBy(
            boolean objectiveAimed,
            double claws,
            Optional<Boolean> bullets,
            Optional<Boolean> contact,
            Optional<Double> slams,
            Optional<Double> dives,
            Optional<Boolean> lobs,
            Optional<Double> bombs,
            Optional<Double> rolls) {
        /** Without M7 part D's bombs and rolls. */
        public DamagedBy(
                boolean objectiveAimed,
                double claws,
                Optional<Boolean> bullets,
                Optional<Boolean> contact,
                Optional<Double> slams,
                Optional<Double> dives,
                Optional<Boolean> lobs) {
            this(objectiveAimed, claws, bullets, contact, slams, dives, lobs, Optional.empty(), Optional.empty());
        }

        public DamagedBy {
            bombs.ifPresent(n -> Check.positive("bombs", n));
            rolls.ifPresent(n -> Check.positive("rolls", n));
            Check.notNegative("claws", claws);
            slams.ifPresent(n -> Check.positive("slams", n));
            dives.ifPresent(d -> Check.positive("dives", d));
            Check.that(lobs.orElse(true), "lobs: true, or leave it out");
        }

        /** M5 part G: the damage of a hooked diver crossing it; 0 when dives do not hurt it. */
        public double diveDamage() {
            return dives.orElse(0.0);
        }

        /** M7 part D: the hits a hooked bomber's drop deals; 0 when bomb runs do not hurt it. */
        public double hitsPerBomb() {
            return bombs.orElse(0.0);
        }

        /** M7 part D: the hits a roll crossing it deals; 0 when rolls do not hurt it. */
        public double hitsPerRoll() {
            return rolls.orElse(0.0);
        }

        /** M5 part G: whether a hooked mortar's lob landing on it hurts it. */
        public boolean hitByLobs() {
            return lobs.orElse(false);
        }

        /** M5 part E: the hits a slam in its lane deals; 0 when slams do not hurt it. */
        public double hitsPerSlam() {
            return slams.orElse(0.0);
        }

        /** Whether every enemy bullet hurts it. */
        public boolean hitByBullets() {
            return bullets.orElse(false);
        }

        /** Whether enemy bodies on its plane hurt it. */
        public boolean hitByContact() {
            return contact.orElse(false);
        }
    }

    /** The rendered headings: {@code count} of them, {@code step} ° apart. */
    public record Headings(int count, double step) {
        public Headings {
            Check.positive("count", count);
            Check.positive("step", step);
        }
    }
}
