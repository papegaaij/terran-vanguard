package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * The level's hazards: the {@code cranes}, the mass-driver {@code sleds}, the low-gravity {@code rocks}, the {@code
 * tows}, the {@code debris} field, the conveyor {@code belts} and furnace {@code vents}, (M9 part H) the mass-relay
 * flash ({@code relay}) and (M10 part A) the Jovian {@code eruptions}.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Sleds}).
 */
public sealed interface HazardsBlock permits LevelData {
    /**
     * The mass-driver sleds (design/world/luna, hazards): from {@code first} s every {@code period}
     * s until {@code until} a sled shoots up the rail at {@code x}, a line {@code width} px wide, on
     * the screen for {@code run} s after the rail {@code lights} chased; contact {@code damage}. The
     * trigger that reveals the {@code clamp} secret can only be hit while the rail is dark.
     */
    public record Sleds(
            double x,
            double width,
            double first,
            double period,
            double until,
            double lights,
            double run,
            double damage,
            Optional<String> clamp,
            Optional<SledChange> easy,
            Optional<SledChange> hard) {
        public Sleds {
            Check.positive("width", width);
            Check.positive("period", period);
            Check.positive("run", run);
            Check.positive("damage", damage);
            Check.that(period > lights + run, "a sled's period holds its lights and its run");
        }
    }

    /** A difficulty's sled period. */
    public record SledChange(double period) {
        public SledChange {
            Check.positive("period", period);
        }
    }

    /**
     * Rocks a destroyed ground unit throws in low gravity (Level 05): {@code count} {@code [min,
     * max]} of them, drifting at {@code speed} {@code [min, max]} px/s in a random direction on the
     * air layer, gone after {@code life} s; {@code hp}, contact {@code damage}, no credits; none
     * within {@code clearance} px of the ship; none on easy unless {@code onEasy}.
     */
    public record Rocks(
            LevelData.Count count,
            Span speed,
            double life,
            Size size,
            double hp,
            double damage,
            double clearance,
            Optional<Boolean> onEasy) {
        public Rocks {
            Check.positive("life", life);
            Check.positive("hp", hp);
        }
    }

    /**
     * A crane hazard ({@code sim.HazardSpec.CraneSpec}): an arm of {@code length} x {@code width}
     * px hanging from ({@code x}, {@code y}) above the top edge, swinging between {@code from} and
     * {@code to} (degrees from straight down, positive to the right) at the {@code swings} times,
     * each swing {@code swing_seconds} long after a {@code telegraph} of blinking lights; contact
     * {@code damage}; a clamp at its tip that {@code reveals} a secret after {@code hits} hits.
     */
    public record CraneData(
            String name,
            double x,
            double y,
            double length,
            double width,
            double from,
            double to,
            List<Double> swings,
            double swingSeconds,
            double telegraph,
            double damage,
            Optional<Clamp> clamp,
            Optional<Swings> easy,
            Optional<Swings> hard) {
        public CraneData {
            Check.positive("length", length);
            Check.positive("width", width);
            Check.notEmpty("swings", swings);
            Check.positive("swing_seconds", swingSeconds);
            Check.positive("telegraph", telegraph);
            Check.positive("damage", damage);
        }
    }

    /** A crane's clamp: {@code hits} hits while the arm swings release the secret it {@code reveals}. */
    public record Clamp(int hits, String reveals) {
        public Clamp {
            Check.positive("hits", hits);
        }
    }

    /** A difficulty's swing times. */
    public record Swings(List<Double> swings) {
        public Swings {
            Check.notEmpty("swings", swings);
        }
    }

    /**
     * Part G: a friendly craft drifting on the air layer that tows a secret's crate on a cable
     * (Level 07's lifeboat tow). The {@code boat} (its sprite box, px) enters at the top edge at
     * {@code t} with its centre at {@code x} and drifts at {@code drift} {@code [x, y]} px/s (y up: a
     * negative y drifts down the screen); the {@code pod} (its box) hangs at {@code tether}
     * {@code [dx, dy]} px from the boat's centre (y up: a positive dy trails above it). Shots and
     * bullets pass the boat and the pod, and the ship flies under them; only the {@code cable}, a hit
     * box midway between the two, takes the player's shots: {@code hits} of them cut it and the pod
     * falls free as the crate of the secret it {@code reveals}. M9 (round 64 C.1 a, the Act 5 tows): with {@code
     * beam_touch: true} a beam touching the cable box (the housing) counts one hit per 0.25 s of contact too
     * (default false: Level 07's lifeboat counts shots only).
     */
    public record Tow(
            double t,
            double x,
            Point drift,
            Size boat,
            Size pod,
            Point tether,
            Size cable,
            int hits,
            String reveals,
            Optional<Boolean> beamTouch) {
        public Tow {
            Check.notNegative("t", t);
            Check.that(drift.y() < 0, "drift: a tow drifts down the screen (a negative y)");
            Check.positive("hits", hits);
        }
    }

    /**
     * The debris field (design/world/earth-orbit, hazards): drifting wreck chunks on the air layer
     * that block shots and enemy bullets. Each placed chunk enters at the top edge at its time.
     *
     * @param chunks the chunk kinds by name; the sprite of {@code large-a} is {@code debris-large-a}
     * @param clearance a chunk never enters closer to the ship than this, px (it moves sideways)
     * @param placed in time order
     * @param maxLarge the most large chunks on the screen at once (checked by the loader on every difficulty)
     * @param easy which large chunks are left out on easy
     * @param hard how much faster they drift on hard
     */
    public record DebrisField(
            Map<String, Chunk> chunks,
            double clearance,
            List<PlacedChunk> placed,
            Optional<Integer> maxLarge,
            Optional<DebrisEasy> easy,
            Optional<DebrisHard> hard) {
        public DebrisField {
            Check.notEmpty("chunks", new ArrayList<>(chunks.keySet()));
            Check.notNegative("clearance", clearance);
            for (int i = 1; i < placed.size(); i++) {
                Check.that(
                        placed.get(i).t() >= placed.get(i - 1).t(),
                        "placed[" + i + "]: chunks are listed in time order");
            }
        }

        /** The chunks entering on {@code difficulty}, in time order. */
        public List<PlacedChunk> placedOn(Difficulty difficulty) {
            if (difficulty != Difficulty.EASY || easy.isEmpty()) {
                return placed;
            }
            int every = easy.get().leaveOutLarge();
            List<PlacedChunk> kept = new ArrayList<>();
            int large = 0;
            for (PlacedChunk chunk : placed) {
                if (chunks.get(chunk.chunk()).large() && ++large % every == 0) {
                    continue;
                }
                kept.add(chunk);
            }
            return kept;
        }

        /** The factor on the drift on {@code difficulty}. */
        public double driftFactor(Difficulty difficulty) {
            return difficulty == Difficulty.HARD
                    ? hard.map(DebrisHard::driftFactor).orElse(1.0)
                    : 1;
        }
    }

    /**
     * A chunk kind: a large one is indestructible and deals contact {@code damage} (once per
     * second), a small one breaks after {@code hp} damage and pays nothing.
     */
    public record Chunk(Size size, Optional<Double> damage, Optional<Double> hp) {
        public Chunk {
            Check.that(damage.isPresent() != hp.isPresent(), "give damage (a large chunk) or hp (a small one)");
            damage.ifPresent(d -> Check.positive("damage", d));
            hp.ifPresent(h -> Check.positive("hp", h));
        }

        public boolean large() {
            return damage.isPresent();
        }
    }

    /**
     * A chunk entering at the top edge at {@code t} at {@code x}, drifting at {@code drift} [x, y] px/s (y up: negative
     * is down). M10 part B (design/campaign Level 36, the pathfinder pod): a small chunk that {@code reveals} a secret of
     * the level frees its hidden crate when the player's shot breaks it; none if not given.
     */
    public record PlacedChunk(double t, double x, String chunk, Point drift, Optional<String> reveals) {
        public PlacedChunk {
            Check.notNegative("t", t);
            Check.that(drift.y() < 0, "drift: a chunk drifts down the screen (a negative y)");
        }

        /** A chunk revealing no secret (every chunk before M10 part B). */
        public PlacedChunk(double t, double x, String chunk, Point drift) {
            this(t, x, chunk, drift, Optional.empty());
        }
    }

    /** On easy every {@code leaveOutLarge}-th large chunk is left out. */
    public record DebrisEasy(int leaveOutLarge) {
        public DebrisEasy {
            Check.that(leaveOutLarge >= 2, "leave_out_large: 2 or more");
        }
    }

    /** On hard the chunks drift {@code driftFactor} times as fast. */
    public record DebrisHard(double driftFactor) {
        public DebrisHard {
            Check.positive("drift_factor", driftFactor);
        }
    }

    /**
     * M7 part F (design/campaign Level 19, Conveyors): the level's conveyor belts, each run {@code width} px wide
     * running up the screen at {@code speed} px/s relative to the ground (negative: down).
     */
    public record Belts(double width, double speed, List<BeltRun> runs) {
        public Belts {
            runs = List.copyOf(runs);
            Check.positive("width", width);
            Check.notEmpty("runs", runs);
            Check.that(speed != 0, "speed: a belt moves");
        }
    }

    /**
     * M7 part F: a belt run centred on {@code x} px from the left edge, from where its bottom end enters at the top
     * edge (level time {@code from}) to where its top end leaves at the bottom edge (level time {@code to}), as the
     * backdrop's belt pieces lie (tools/art/backdrop_l19.py's proposal lists them).
     */
    public record BeltRun(double x, double from, double to) {
        public BeltRun {
            Check.that(x > 0 && x < PlayField.WIDTH, "x: on the play field, was " + x);
            Check.notNegative("from", from);
            Check.that(to > from, "to: after from");
        }
    }

    /**
     * M7 part F (design/campaign Level 19, Hazards): the furnace vents: grates {@code width} px square in {@code rows};
     * each row, from the time it enters at the top edge, fires every {@code period} s as a ripple, grate i glowing
     * {@code tell} s from {@code i × ripple} s into the cycle, then its flame column standing {@code burn} s, hitting
     * the ship and Rook whatever their layer once per burst for the {@code bullet} class's damage; {@code easy} and
     * {@code hard} change the tell, period and ripple. Cold from the arena on. M8 part B (design/campaign Level 22,
     * Hazards: the ice geysers): a row may set its own period and ripple ({@link VentRowData}), and {@code look} names
     * how the game draws and sounds the rows ({@link #FURNACE_VENT} if not given, {@link #ICE_GEYSER}); the simulation
     * never reads it. M8 part G (design/campaign Level 27, Hazards: the heat plumes; m8g-gaps.md §1.2): {@code look:
     * heat-plume} with {@code shape: disc} (each grate's flame a disc {@code width} px across, not a square), {@code
     * enemy_damage} (each {@code air} or {@code low-air} unit over a burning disc takes it once per burst, the kill the
     * player's) and {@code bend} (degrees: a shot, an enemy bullet or a homing torpedo entering a burning disc turns
     * this far away from its centre, once).
     */
    public record Vents(
            double width,
            double tell,
            double burn,
            double period,
            double ripple,
            String bullet,
            Optional<VentChange> easy,
            Optional<VentChange> hard,
            List<VentRowData> rows,
            Optional<String> look,
            Optional<String> shape,
            Optional<Double> enemyDamage,
            Optional<Double> bend) {
        /** M7 part F: Level 19's furnace vents, the look if not given. */
        public static final String FURNACE_VENT = "furnace-vent";

        /** M8 part B: Level 22's ice geysers (a crack that glows, then a steam column). */
        public static final String ICE_GEYSER = "ice-geyser";

        /** M8 part G: Level 27's heat plumes (a glowing mouth, then a shimmering column; {@code shape: disc}). */
        public static final String HEAT_PLUME = "heat-plume";

        /** M8 part G: a plume's flame shape: a disc {@code width} px across (the default is a square grate). */
        public static final String DISC = "disc";

        /** M8 part G: a square grate's flame (the default). */
        public static final String SQUARE = "square";

        /** Without M8 part G's shape, burns and bend. */
        public Vents(
                double width,
                double tell,
                double burn,
                double period,
                double ripple,
                String bullet,
                Optional<VentChange> easy,
                Optional<VentChange> hard,
                List<VentRowData> rows,
                Optional<String> look) {
            this(
                    width,
                    tell,
                    burn,
                    period,
                    ripple,
                    bullet,
                    easy,
                    hard,
                    rows,
                    look,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());
        }

        /** Without M8 part B's look.
         * public Vents(
         * double width,
         * double tell,
         * double burn,
         * double period,
         * double ripple,
         * String bullet,
         * Optional<VentChange> easy,
         * Optional<VentChange> hard,
         * List<VentRowData> rows) {
         * this(width, tell, burn, period, ripple, bullet, easy, hard, rows, Optional.empty());
         * }
         *
         * public Vents {
         * look.ifPresent(name -> Check.that(
         * name.equals(FURNACE_VENT) || name.equals(ICE_GEYSER) || name.equals(HEAT_PLUME),
         * "look: " + FURNACE_VENT + ", " + ICE_GEYSER + " or " + HEAT_PLUME + ", was '" + name + "'"));
         * shape.ifPresent(name -> Check.that(
         * name.equals(DISC) || name.equals(SQUARE),
         * "shape: " + DISC + " or " + SQUARE + ", was '" + name + "'"));
         * enemyDamage.ifPresent(d -> Check.notNegative("enemy_damage", d));
         * bend.ifPresent(b -> Check.that(b >= 0 && b < 90, "bend: 0 up to (not) 90 degrees, was " + b));
         * rows = List.copyOf(rows);
         * Check.positive("width", width);
         * Check.notNegative("tell", tell);
         * Check.positive("burn", burn);
         * Check.positive("period", period);
         * Check.notNegative("ripple", ripple);
         * Check.notEmpty("rows", rows);
         * for (int i = 1; i < rows.size(); i++) {
         * Check.that(rows.get(i).t() >= rows.get(i - 1).t(), "rows[" + i + "]: listed in time order");
         * }
         * }
         *
         * /** M8 part B: how the game draws and sounds the rows ({@link #FURNACE_VENT} if not given). */
        public String lookOrDefault() {
            return look.orElse(FURNACE_VENT);
        }

        /** M8 part G: whether each grate's flame is a disc ({@code shape: disc}, the heat plumes). */
        public boolean disc() {
            return shape.filter(DISC::equals).isPresent();
        }

        /**
         * M8 part B: {@code row}'s tell on {@code difficulty}, s: its own difficulty's change, else its own value, else
         * the rows' ({@link #tellOn(Difficulty)}).
         */
        public double tellOn(VentRowData row, Difficulty difficulty) {
            return row.change(difficulty)
                    .flatMap(VentChange::tell)
                    .or(row::tell)
                    .orElseGet(() -> tellOn(difficulty));
        }

        /** M8 part B: {@code row}'s period on {@code difficulty}, s (as {@link #tellOn(VentRowData, Difficulty)}). */
        public double periodOn(VentRowData row, Difficulty difficulty) {
            return row.change(difficulty)
                    .flatMap(VentChange::period)
                    .or(row::period)
                    .orElseGet(() -> periodOn(difficulty));
        }

        /** M8 part B: {@code row}'s ripple on {@code difficulty}, s (as {@link #tellOn(VentRowData, Difficulty)}). */
        public double rippleOn(VentRowData row, Difficulty difficulty) {
            return row.change(difficulty)
                    .flatMap(VentChange::ripple)
                    .or(row::ripple)
                    .orElseGet(() -> rippleOn(difficulty));
        }

        /** The tell on {@code difficulty}, s. */
        public double tellOn(Difficulty difficulty) {
            return change(difficulty).flatMap(VentChange::tell).orElse(tell);
        }

        /** The period on {@code difficulty}, s. */
        public double periodOn(Difficulty difficulty) {
            return change(difficulty).flatMap(VentChange::period).orElse(period);
        }

        /** The ripple on {@code difficulty}, s. */
        public double rippleOn(Difficulty difficulty) {
            return change(difficulty).flatMap(VentChange::ripple).orElse(ripple);
        }

        private Optional<VentChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }
    }

    /** M7 part F: a difficulty's change to the vents: their tell, period or ripple instead, s. */
    public record VentChange(Optional<Double> tell, Optional<Double> period, Optional<Double> ripple) {
        public VentChange {
            tell.ifPresent(v -> Check.notNegative("tell", v));
            period.ifPresent(v -> Check.positive("period", v));
            ripple.ifPresent(v -> Check.notNegative("ripple", v));
        }
    }

    /**
     * M7 part F: a vent row entering at the top edge at level time {@code t}, its grates at {@code xs} px from the
     * left in ripple order (the first fires first). M8 part B (design/campaign Level 22, the geysers that tremble faster
     * near Conamara): the row's own {@code tell}, {@code period} and {@code ripple} and its own {@code easy} and {@code
     * hard} changes of them, over the rows' ({@link Vents#periodOn(VentRowData, Difficulty)}).
     */
    public record VentRowData(
            double t,
            List<Double> xs,
            Optional<Double> tell,
            Optional<Double> period,
            Optional<Double> ripple,
            Optional<VentChange> easy,
            Optional<VentChange> hard) {
        /** Without M8 part B's own timing. */
        public VentRowData(double t, List<Double> xs) {
            this(t, xs, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public VentRowData {
            xs = List.copyOf(xs);
            Check.notNegative("t", t);
            Check.notEmpty("xs", xs);
            xs.forEach(x -> Check.that(x > 0 && x < PlayField.WIDTH, "xs: on the play field, was " + x));
            tell.ifPresent(v -> Check.notNegative("tell", v));
            period.ifPresent(v -> Check.positive("period", v));
            ripple.ifPresent(v -> Check.notNegative("ripple", v));
        }

        /** M8 part B: the row's own change on {@code difficulty}; none on medium. */
        public Optional<VentChange> change(Difficulty difficulty) {
            return switch (difficulty) {
                case EASY -> easy;
                case MEDIUM -> Optional.empty();
                case HARD -> hard;
            };
        }
    }

    /**
     * M8 part H (design/enemies/bosses/abyssal-maw, The cruisers; design/campaign Level 28, sections 8–10): the finale's
     * two Ascendancy cruisers, invulnerable {@code low-air} set pieces on the level clock: they {@code rise} (the
     * presentation's growth) until {@code run}, fire until {@code end} and fall off the bottom edge over {@code fall}
     * s; each hull {@code hull} px, the inner edges at {@code inner} ([left, right] px from the left edge); the {@code
     * broadside}, {@code flak} and {@code point_defence} at medium, {@code easy} and {@code hard} changing them. No
     * bounty, no bar; the bullets above water.
     */
    record Cruisers(
            double rise,
            double run,
            double end,
            double fall,
            Size hull,
            Span inner,
            Broadside broadside,
            FlakData flak,
            PointDefenceData pointDefence,
            Optional<CruiserChange> easy,
            Optional<CruiserChange> hard) {
        public Cruisers {
            Check.notNegative("rise", rise);
            Check.that(run >= rise, "run: at or after the rise, was " + run);
            Check.that(end > run, "end: after the run's start, was " + end);
            Check.notNegative("fall", fall);
            Check.that(
                    inner.min() >= 0 && inner.max() <= PlayField.WIDTH && inner.min() < inner.max(),
                    "inner: the hulls' inner edges on the field, left before right");
        }

        /** This run on {@code difficulty}: medium's with the difficulty's changes. */
        public Cruisers on(Difficulty difficulty) {
            Optional<CruiserChange> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case MEDIUM -> Optional.empty();
                        case HARD -> hard;
                    };
            if (change.isEmpty()) {
                return this;
            }
            CruiserChange c = change.get();
            Broadside b = new Broadside(
                    broadside.band(),
                    c.tell().orElse(broadside.tell()),
                    broadside.shells(),
                    broadside.speed(),
                    broadside.bullet(),
                    broadside.every(),
                    c.doubleBands().orElse(true) ? broadside.doubleAfter() : Optional.empty(),
                    broadside.free());
            FlakData f = new FlakData(
                    c.markers().orElse(flak.markers()),
                    c.flakEvery().orElse(flak.every()),
                    flak.fuse(),
                    flak.radius(),
                    flak.scatter(),
                    flak.bullet(),
                    flak.ring(),
                    flak.speed(),
                    flak.ringBullet());
            PointDefenceData pd = new PointDefenceData(
                    c.burst().orElse(pointDefence.burst()),
                    pointDefence.speed(),
                    pointDefence.bullet(),
                    pointDefence.every(),
                    pointDefence.turrets());
            return new Cruisers(rise, run, end, fall, hull, inner, b, f, pd, Optional.empty(), Optional.empty());
        }
    }

    /**
     * A broadside: a {@code band} px tall told {@code tell} s, then {@code shells} shells of the {@code bullet} class
     * cross it at {@code speed} px/s from the cruiser's side; one every {@code every} s, the flanks in turn; from {@code
     * double_after} s into the run two bands at once with at least {@code free} px free between them (none if not
     * given: never).
     */
    record Broadside(
            double band,
            double tell,
            int shells,
            double speed,
            String bullet,
            double every,
            Optional<Double> doubleAfter,
            double free) {
        public Broadside {
            Check.positive("band", band);
            Check.notNegative("tell", tell);
            Check.positive("shells", shells);
            Check.positive("speed", speed);
            Check.positive("every", every);
            doubleAfter.ifPresent(d -> Check.notNegative("double_after", d));
            Check.notNegative("free", free);
        }
    }

    /**
     * Flak: {@code markers} markers of {@code radius} px (the first on the ship, the others within {@code scatter} px)
     * every {@code every} s; after the {@code fuse} the burst: the {@code bullet} class's damage inside a marker and a
     * ring of {@code ring} {@code ring_bullet} bullets at {@code speed} px/s.
     */
    record FlakData(
            int markers,
            double every,
            double fuse,
            double radius,
            double scatter,
            String bullet,
            int ring,
            double speed,
            String ringBullet) {
        public FlakData {
            Check.notNegative("markers", markers);
            Check.positive("every", every);
            Check.positive("fuse", fuse);
            Check.notNegative("radius", radius);
            Check.notNegative("scatter", scatter);
            Check.notNegative("ring", ring);
            Check.positive("speed", speed);
        }
    }

    /**
     * Point defence: each cruiser's turret line fires an aimed burst of {@code burst} {@code bullet} bullets at {@code
     * speed} px/s every {@code every} s, from its {@code turrets} in turn (heights on the hull's inner edge, px below the
     * top edge while it runs).
     */
    record PointDefenceData(int burst, double speed, String bullet, double every, List<Double> turrets) {
        public PointDefenceData {
            turrets = List.copyOf(turrets);
            Check.positive("burst", burst);
            Check.positive("speed", speed);
            Check.positive("every", every);
            Check.notEmpty("turrets", turrets);
        }
    }

    /**
     * A difficulty's changes to the cruisers: the broadside's {@code tell}, {@code double_bands: false} (never two bands,
     * easy's), flak's {@code markers} and {@code flak_every}, point defence's {@code burst}.
     */
    record CruiserChange(
            Optional<Double> tell,
            Optional<Boolean> doubleBands,
            Optional<Integer> markers,
            Optional<Double> flakEvery,
            Optional<Integer> burst) {
        public CruiserChange {
            tell.ifPresent(t -> Check.notNegative("tell", t));
            markers.ifPresent(m -> Check.notNegative("markers", m));
            flakEvery.ifPresent(e -> Check.positive("flak_every", e));
            burst.ifPresent(b -> Check.positive("burst", b));
        }
    }

    /**
     * M9 part H (design/campaign Level 35, the mass-relay flash): once, at {@code t} s on the level clock, a horizontal
     * line is locked through the ship's y and telegraphed {@code telegraph} s from the {@code from} edge ({@code right}
     * or {@code left}); then it flashes, live {@code live} s, a band {@code band} px high that hits the ship once for
     * {@code damage} (Rook and the enemies untouched). The difficulties' {@code easy} and {@code hard} change the
     * telegraph (easy 1.5 s).
     */
    record Relay(
            double t,
            double telegraph,
            double live,
            double band,
            double damage,
            String from,
            Optional<RelayChange> easy,
            Optional<RelayChange> hard) {
        public Relay {
            Check.notNegative("t", t);
            Check.positive("telegraph", telegraph);
            Check.positive("live", live);
            Check.positive("band", band);
            Check.positive("damage", damage);
            Check.that(from.equals("right") || from.equals("left"), "from: right or left, was '" + from + "'");
        }

        /** Whether it is telegraphed from the right edge. */
        public boolean fromRight() {
            return from.equals("right");
        }

        /** Its telegraph on {@code difficulty}, s. */
        public double telegraphOn(Difficulty difficulty) {
            Optional<RelayChange> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case HARD -> hard;
                        case MEDIUM -> Optional.empty();
                    };
            return change.flatMap(RelayChange::telegraph).orElse(telegraph);
        }

        /** The window, s on the level clock, from its telegraph's start to the end of its flash. */
        public Span window(Difficulty difficulty) {
            return new Span(t, t + telegraphOn(difficulty) + live);
        }
    }

    /** A difficulty's change to the relay flash: its {@code telegraph} instead, s. */
    record RelayChange(Optional<Double> telegraph) {
        public RelayChange {
            telegraph.ifPresent(s -> Check.positive("telegraph", s));
        }
    }
    /**
     * M10 part A (design/world/jovian, Hazards &amp; set pieces: eruptions; Level 39): a ground vent entering at the top
     * edge at {@code t} s at {@code x} px from the left and scrolling with the ground; while on the screen it erupts
     * every {@code period} {@code [min, max]} s (the world's 5–7 if not given, {@link JovianData.Eruption}). Its tell,
     * rocks and their flight are the world's, by difficulty.
     */
    record EruptionVent(double t, double x, Optional<Span> period) {
        public EruptionVent {
            Check.notNegative("t", t);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: on the play field (0-" + PlayField.WIDTH + "), was " + x);
            period.ifPresent(range -> Check.positive("period", range.min()));
        }
    }
}
