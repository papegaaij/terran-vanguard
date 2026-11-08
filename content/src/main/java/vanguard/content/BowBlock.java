package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * M10 part H, step H2 (design/enemies/bosses/ascendant; m10h-gaps.md §1 rows 1–11 and 13): a phased bow-on boss's keys in
 * its stat block ({@link EnemyData}, {@code boss:}), flown by steps H1a/H1b's simulation: the body as a {@code wedge},
 * the generator-fed shield ({@code fed_shield}), the batteries' mirror {@code panel} and their places ({@code mirrors}),
 * the bursts' arbitration ({@code battery_fire}), the per-phase {@code playbooks}, the phase {@code cues} (the radio's
 * subjects), the parts' {@code regeneration} and the {@code escape} (the hull's break and the lifeboat's climb), and
 * the difficulties' {@code bow} changes. A phase's {@code untouchable}, {@code drop} and {@code drop_at} are the phase
 * opening ({@link EnemyBossData.PhaseData}). Content's {@link BowRules} turns them into the simulation's specs; {@link
 * BowChecks} checks them.
 *
 * <p>Places are the design's screen px (x from the left edge, y below the top edge) unless a record says otherwise; the
 * simulation's are y up from the piece's centre.
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.WedgeData}
 * and so on, without growing that record's file.
 */
public sealed interface BowBlock permits EnemyData {
    /** The playbook moves by their slugs, in the simulation's order ({@code PhasePlaybook.Kind}). */
    List<String> MOVES = List.of(
            "line",
            "volley",
            "salvo",
            "stream",
            "brood",
            "spiral",
            "lobs",
            "mounts",
            "sweep",
            "twin",
            "barrage",
            "rear-sweep",
            "spores",
            "wall-spit");

    /** M11 part F, step F2: the rear sweep's move (the boss's {@code rear_sweep}; it fires no attack of its own). */
    String REAR_SWEEP = "rear-sweep";

    /**
     * The body as a wedge (the Ascendant's bow): its {@code top} edge's x span at the top edge (y 0), its flat tip's
     * {@code tip} x span at {@code tip_y} px below the top edge; a touch pushes the ship {@code push} px out along the
     * face's normal (straight down under the tip). Contact is the stat block's tier's, once a second.
     */
    record WedgeData(Span top, Span tip, double tipY, double push) {
        public WedgeData {
            Check.that(top.min() < top.max(), "wedge.top: left before right");
            Check.that(tip.min() < tip.max(), "wedge.tip: left before right");
            Check.that(
                    top.min() <= tip.min() && tip.max() <= top.max(),
                    "wedge: the tip inside the top's span (a wedge narrows to its tip)");
            Check.positive("wedge.tip_y", tipY);
            Check.notNegative("wedge.push", push);
        }
    }

    /**
     * A part's hex shield of {@code hp} fed by the parts {@code fed_by}: full while every feeder lives, its share of them
     * as they die, none with none (the enemy shield rule: the delay, refill and breaker's hold are the shared ones).
     */
    record FedShieldData(String part, double hp, List<String> fedBy) {
        public FedShieldData {
            Check.positive("fed_shield.hp", hp);
            fedBy = List.copyOf(Check.notEmpty("fed_shield.fed_by", fedBy));
            Check.that(!fedBy.contains(part), "fed_shield: a part does not feed its own shield");
        }
    }

    /**
     * A part behind a mirror panel (the {@code panel}'s reflect numbers): it faces {@code facing}° off straight down
     * (+: toward the screen's left, the part's own side on the left face) and turns at most {@code turn}° either way of
     * it toward the ship.
     */
    record MirrorPlace(String part, double facing, double turn) {
        public MirrorPlace {
            Check.that(Double.isFinite(facing), "mirrors[].facing: degrees");
            Check.that(turn >= 0 && turn < 180, "mirrors[].turn: at least 0 and less than 180 degrees");
        }
    }

    /**
     * The batteries' bursts: {@code attack}'s parts fire it only while their panel faces within the panel's {@code
     * fire_within} of the ship, at most {@code at_once} at once, {@code apart} s apart, each after a {@code tell} s.
     */
    record BatteryFireData(String attack, int atOnce, double apart, double tell) {
        public BatteryFireData {
            Check.positive("battery_fire.at_once", atOnce);
            Check.notNegative("battery_fire.apart", apart);
            Check.notNegative("battery_fire.tell", tell);
        }
    }

    /**
     * A phase's playbook: in phase {@code phase} (its name) the first living part of {@code sources} draws its next
     * move after each {@code rest} ({@code low_rest} while {@code low_parts}', else the sources', HP is below {@code
     * low_below}), never the same twice running, each told {@code tell} s; M10 round 80 fixes, R80a: a told line (a
     * {@code line}, {@code sweep} or {@code twin}) at least every {@code lance_every}-th draw when given.
     */
    record BowPlaybook(
            String phase,
            List<String> sources,
            double tell,
            double rest,
            double lowRest,
            double lowBelow,
            Optional<List<String>> lowParts,
            List<BowMove> moves,
            Optional<Integer> lanceEvery,
            Optional<List<String>> fallback,
            Optional<ParallelData> parallel,
            Optional<Integer> coverEvery) {
        public BowPlaybook {
            // M11 part H, step H2 (the Choir Heart's membranes): each living source in any cover_every draws.
            coverEvery.ifPresent(n -> Check.that(n >= 2, "playbooks[].cover_every: 2 draws or more, was " + n));
            lanceEvery.ifPresent(n -> Check.positive("playbooks[].lance_every", n));
            sources = List.copyOf(Check.notEmpty("playbooks[].sources", sources));
            moves = List.copyOf(Check.notEmpty("playbooks[].moves", moves));
            lowParts = lowParts.map(List::copyOf);
            fallback = fallback.map(List::copyOf);
            Check.notNegative("playbooks[].tell", tell);
            Check.notNegative("playbooks[].rest", rest);
            Check.notNegative("playbooks[].low_rest", lowRest);
            Check.share("playbooks[].low_below", lowBelow);
        }
    }

    /**
     * M11 part F, step F5 (Vorne's Chimera, round 93 L.3: several parts firing at once): two moves at once, the second's
     * tell {@code gap} s after the first's; never two told lines (a rear sweep counts) together with {@code one_line},
     * at most one releasing units with {@code one_release}; with {@code from_half} only once half of the playbook's
     * sources are dead.
     */
    record ParallelData(
            double gap, Optional<Boolean> oneLine, Optional<Boolean> oneRelease, Optional<Boolean> fromHalf) {
        public ParallelData {
            Check.notNegative("playbooks[].parallel.gap", gap);
        }
    }

    /**
     * A playbook move ({@link #MOVES}): its {@code attack} (none for a {@code stream} or {@code brood}), its {@code
     * count} and the {@code gap} between them, its draw {@code weight}, at most every {@code every}-th draw; a salvo's
     * or the grafted guns' {@code racks}, a brood's sacs ({@code racks}: the first living one broods); a stream's or a
     * brood's {@code bays}. M10 round 80 fixes, R80a: a {@code sweep}'s turn {@code sweep} (degrees over its live
     * time, away from the ship's side); a {@code twin}'s {@code flank} (px beside the source) and its volley {@code with}
     * (an attack, fired as the first line does), its lines {@code gap} s apart; a {@code barrage}'s batteries ({@code
     * racks}, in turn {@code gap} s apart, each its own told burst).
     */
    record BowMove(
            String move,
            Optional<String> attack,
            Optional<Integer> count,
            Optional<Double> gap,
            Optional<Double> weight,
            Optional<Integer> every,
            Optional<List<String>> racks,
            Optional<LaunchData> bays,
            Optional<String> with,
            Optional<Double> sweep,
            Optional<Double> flank,
            Optional<String> cover,
            Optional<Boolean> notFirst,
            Optional<List<String>> only,
            Optional<String> source,
            Optional<Double> clear,
            Optional<Boolean> last) {
        public BowMove {
            // M11 part H, step H2 (the Choir Heart): a spore drop's or a wall spit's clearance from the ship (px);
            // the last living source's own moves (the last membrane's song and the Herald's by turns).
            clear.ifPresent(c -> Check.notNegative("moves[].clear", c));
            Check.that(
                    clear.isEmpty() || move.equals("spores") || move.equals("wall-spit"),
                    "moves[]: only a spores or a wall-spit move keeps a clearance (clear)");
            Check.that(MOVES.contains(move), "moves[].move: one of " + MOVES + ", was '" + move + "'");
            boolean launches = move.equals("stream") || move.equals("brood");
            // M11 part F, step F2: a move never drawn first in its phase (the rear sweep's rule), a move drawn only on
            // some difficulties (the Chimera's easy phase 3 without its rear sweep).
            only = only.map(List::copyOf);
            only.ifPresent(list -> list.forEach(name -> Check.that(
                    name.equals("easy") || name.equals("medium") || name.equals("hard"),
                    "moves[].only: difficulties (easy, medium, hard), was '" + name + "'")));
            Check.that(
                    (launches || move.equals("barrage") || move.equals(REAR_SWEEP)) != attack.isPresent(),
                    "moves[]: a stream or a brood launches its bays, a barrage cues its batteries, every other move"
                            + " fires an attack");
            Check.that(move.equals("twin") == with.isPresent(), "moves[]: a twin, and only it, has a volley (with)");
            // R81a: a covering volley as the move ends (a told line's also as its line is drawn).
            Check.that(cover.isEmpty() || !move.equals("brood"), "moves[]: a brood has no cover");
            // M11 part F, step F5 (round 93 L.3: a destroyed part's guns go silent): a move fired from a part of its
            // own,
            // drawn only while that part lives (a rear sweep moves the whole boss: never).
            Check.that(source.isEmpty() || !move.equals(REAR_SWEEP), "moves[]: a rear sweep has no source of its own");
            Check.that(move.equals("twin") == flank.isPresent(), "moves[]: a twin, and only it, has a flank");
            // M11 part G, step G2 (the Brood Carrier echo's mandible sweep): a volley's centre may step away too.
            Check.that(
                    move.equals("sweep") ? sweep.isPresent() : sweep.isEmpty() || move.equals("volley"),
                    "moves[]: a sweep has a sweep, a volley may (its centre stepping away), no other move");
            Check.that(!move.equals("barrage") || racks.isPresent(), "moves[]: a barrage names its batteries (racks)");
            sweep.ifPresent(d -> Check.positive("moves[].sweep", d));
            flank.ifPresent(d -> Check.positive("moves[].flank", d));
            Check.that(launches == bays.isPresent(), "moves[]: a stream or a brood, and only it, has bays");
            count.ifPresent(n -> Check.positive("moves[].count", n));
            gap.ifPresent(g -> Check.notNegative("moves[].gap", g));
            weight.ifPresent(w -> Check.positive("moves[].weight", w));
            every.ifPresent(n -> Check.positive("moves[].every", n));
            racks = racks.map(List::copyOf);
        }

        /** M11 part F, step F2: whether the move is drawn on {@code difficulty} (its {@code only}; every one without). */
        public boolean drawnOn(Difficulty difficulty) {
            return only.map(list -> list.contains(difficulty.name().toLowerCase(java.util.Locale.ROOT)))
                    .orElse(true);
        }
    }

    /**
     * A stream's or a brood's bays: {@code stream} units of {@code from} each, {@code gap} s apart, flying out at {@code
     * speed} px/s (the unit's own if not given) within {@code spread}° and straight for {@code straight} s, at most
     * {@code most_alive} launched alive; from {@code places} (offsets from the piece's centre for a stream, from the
     * brooding part for a brood: px, y up).
     */
    record LaunchData(
            String from,
            int stream,
            double gap,
            Optional<Double> speed,
            double spread,
            double straight,
            int mostAlive,
            List<Point> places,
            Optional<List<TurnData>> alternate,
            Optional<Boolean> fromOpen,
            Optional<Boolean> towardShip,
            Optional<Boolean> wallPits) {
        public LaunchData {
            alternate = alternate.map(List::copyOf);
            alternate.ifPresent(list -> Check.notEmpty("bays.alternate", list));
            Check.positive("bays.stream", stream);
            Check.notNegative("bays.gap", gap);
            speed.ifPresent(s -> Check.positive("bays.speed", s));
            Check.notNegative("bays.spread", spread);
            Check.notNegative("bays.straight", straight);
            Check.positive("bays.most_alive", mostAlive);
            places = List.copyOf(Check.notEmpty("bays.places", places));
        }
    }

    /**
     * M11 part G, step G2 (the Brood Carrier echo's brood: "4 Skitters or 2 Needlers, alternating"): a brood's next
     * launch in turn after its bays' own, {@code stream} units of {@code from} (the bays' other numbers; its speed the
     * unit's own if not given). A brood with {@code alternate} takes its bays' {@code from_open} too: it launches from
     * the open window group's parts.
     */
    record TurnData(String from, int stream, Optional<Double> speed) {
        public TurnData {
            Check.positive("bays.alternate[].stream", stream);
            speed.ifPresent(s -> Check.positive("bays.alternate[].speed", s));
        }
    }

    /**
     * A phase cue: the {@code boss-phase} radio subject {@code subject}, raised once in phase {@code phase} (its name) at
     * the first of: {@code deaths} of {@code parts} dead, their HP below the share {@code below}, {@code seconds} into
     * the phase.
     */
    record CueData(
            String subject,
            String phase,
            Optional<List<String>> parts,
            Optional<Integer> deaths,
            Optional<Double> below,
            Optional<Double> seconds) {
        public CueData {
            Check.that(!subject.isBlank(), "cues[].subject: a name");
            parts = parts.map(List::copyOf);
            deaths.ifPresent(n -> Check.positive("cues[].deaths", n));
            below.ifPresent(b -> Check.that(b > 0 && b < 1, "cues[].below: a share between 0 and 1"));
            seconds.ifPresent(s -> Check.positive("cues[].seconds", s));
            Check.that(
                    deaths.isPresent() || below.isPresent() || seconds.isPresent(),
                    "cues[]: a trigger (deaths, below or seconds)");
            Check.that(
                    (deaths.isEmpty() && below.isEmpty()) || parts.isPresent(),
                    "cues[]: deaths and below count their parts");
        }
    }

    /** A part's regeneration: {@code rate} of its HP a second, {@code delay} s after its last hit. */
    record RegenData(String part, double rate, double delay) {
        public RegenData {
            Check.that(rate > 0 && rate <= 1, "regeneration[].rate: a share a second in (0, 1]");
            Check.notNegative("regeneration[].delay", delay);
        }
    }

    /**
     * The escape: {@code breaks_on}'s death breaks the hull (K); the act boss's flash at K + {@code flash} s, the
     * lifeboat ({@code part}, which cannot die) breaks out at K + {@code breakout} s from {@code from}, hangs there
     * {@code hold} s (round 81; none if left out) and climbs to the top edge in {@code seconds} s, its speed rising in the ratio {@code speed} ([start, end]), weaving up to {@code
     * weave} px either side, never below {@code floor_y} px from the top edge. Round 80: a short scene, the lifeboat (an
     * {@code armoured} part) never hit, paying, firing or touching the ship.
     */
    record EscapeData(
            String part,
            String breaksOn,
            double flash,
            double breakout,
            Point from,
            double seconds,
            Span speed,
            double weave,
            double floorY,
            Optional<Double> hold) {
        public EscapeData {
            Check.notNegative("escape.flash", flash);
            Check.that(breakout >= flash, "escape.breakout: at or after the flash");
            Check.positive("escape.seconds", seconds);
            Check.that(speed.min() > 0 && speed.max() >= speed.min(), "escape.speed: [start, end], rising");
            Check.notNegative("escape.weave", weave);
            Check.notNegative("escape.hold", hold.orElse(0.0));
            Check.that(from.y() <= floorY, "escape: the breakout at or above its floor");
            Check.that(!part.equals(breaksOn), "escape: the lifeboat is not the part the hull breaks on");
        }
    }

    /**
     * M11 part F, step F2 (design/enemies/bosses/vornes-chimera, The rear sweep): out of the top edge at {@code speed}
     * px/s, {@code out} s out of sight, up from the bottom edge at least {@code off_ship} px off the ship's x, climbing
     * with its body box {@code clear} px off the ship, firing {@code fire}'s attack every {@code fire.every} s.
     */
    record RearSweepData(double out, double speed, double offShip, double clear, Optional<SweepFire> fire) {
        public RearSweepData {
            Check.notNegative("rear_sweep.out", out);
            Check.positive("rear_sweep.speed", speed);
            Check.notNegative("rear_sweep.off_ship", offShip);
            Check.notNegative("rear_sweep.clear", clear);
        }
    }

    /** M11 part F, step F2: the rear sweep's climbing fire: {@code attack} every {@code every} s. */
    record SweepFire(String attack, double every) {
        public SweepFire {
            Check.positive("rear_sweep.fire.every", every);
        }
    }

    /**
     * M11 part F, step F2 (design/enemies/bosses/vornes-chimera, Death): at K its fire stops, its bullets fade over
     * {@code bullets_fade} s, it bursts {@code seconds} after K (the last line's length).
     */
    record LastWordsData(double seconds, double bulletsFade, Optional<Double> flashAt) {
        public LastWordsData {
            // M11 part H, step H2 (the Choir Heart): 0 s bursts at once (the fade without a hang), the flash at
            // flash_at.
            Check.notNegative("last_words.seconds", seconds);
            flashAt.ifPresent(f -> Check.notNegative("last_words.flash_at", f));
            Check.notNegative("last_words.bullets_fade", bulletsFade);
        }

        /** Last words without a flash of their own (step F2's). */
        public LastWordsData(double seconds, double bulletsFade) {
            this(seconds, bulletsFade, Optional.empty());
        }
    }

    /**
     * M11 part F, step F2 (design/enemies/bosses/vornes-chimera, Arena: the intro): it rises over {@code seconds} from
     * {@code from} (screen px, y below the top edge: the spire well) to its station, untouchable until in place.
     */
    record RiseData(double seconds, Point from, Optional<RiseFireData> fire) {
        public RiseData {
            Check.positive("rise.seconds", seconds);
        }
    }

    /**
     * M11 part F, step F5 (round 93 L.3: "make sure it enters the screen firing real rounds at you"): while it rises, one
     * of {@code attack}'s parts in turn fires it at the ship every {@code every} s, never at a ship within {@code clear}
     * px of that part.
     */
    record RiseFireData(String attack, double every, Optional<Double> clear) {
        public RiseFireData {
            Check.positive("rise.fire.every", every);
            clear.ifPresent(c -> Check.notNegative("rise.fire.clear", c));
        }
    }

    /**
     * M11 part F, step F2: a plain hex shield of {@code hp} on {@code part} (the enemy shield rule; refilled {@code
     * delay} s after its last hit, the shared delay when left out).
     */
    record PartShieldData(String part, double hp, Optional<Double> delay) {
        public PartShieldData {
            Check.positive("part_shields[].hp", hp);
            delay.ifPresent(d -> Check.notNegative("part_shields[].delay", d));
        }
    }

    /** M11 part F, step F2: Rook's part {@code order} in {@code phase} (the first living one he can reach). */
    record RookOrderData(String phase, List<String> order, Optional<String> pick) {
        /** M11 part H, step H2: Rook's picks ({@code pick}): the listed order, an open window first, his own side's. */
        public static final List<String> PICKS = List.of("listed", "open-window", "own-side");

        public RookOrderData {
            order = List.copyOf(Check.notEmpty("rook[].order", order));
            pick.ifPresent(p -> Check.that(PICKS.contains(p), "rook[].pick: one of " + PICKS + ", was '" + p + "'"));
        }
    }

    /**
     * A difficulty's changes to a bow boss ({@code bow} in its hook): the playbooks' rests × {@code rests}, the parts'
     * regeneration rates × {@code regeneration}, a brood's stream {@code brood}.
     */
    record BowChange(Optional<Double> rests, Optional<Double> regeneration, Optional<Integer> brood) {
        public BowChange {
            rests.ifPresent(r -> Check.positive("bow.rests", r));
            regeneration.ifPresent(r -> Check.positive("bow.regeneration", r));
            brood.ifPresent(n -> Check.positive("bow.brood", n));
        }
    }
}
