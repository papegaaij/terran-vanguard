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
            "line", "volley", "salvo", "stream", "brood", "spiral", "lobs", "mounts", "sweep", "twin", "barrage");

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
            Optional<List<String>> fallback) {
        public BowPlaybook {
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
            Optional<String> cover) {
        public BowMove {
            Check.that(MOVES.contains(move), "moves[].move: one of " + MOVES + ", was '" + move + "'");
            boolean launches = move.equals("stream") || move.equals("brood");
            Check.that(
                    (launches || move.equals("barrage")) != attack.isPresent(),
                    "moves[]: a stream or a brood launches its bays, a barrage cues its batteries, every other move"
                            + " fires an attack");
            Check.that(move.equals("twin") == with.isPresent(), "moves[]: a twin, and only it, has a volley (with)");
            // R81a: a covering volley as the move ends (a told line's also as its line is drawn).
            Check.that(cover.isEmpty() || !move.equals("brood"), "moves[]: a brood has no cover");
            Check.that(move.equals("twin") == flank.isPresent(), "moves[]: a twin, and only it, has a flank");
            Check.that(move.equals("sweep") == sweep.isPresent(), "moves[]: a sweep, and only it, has a sweep");
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
            List<Point> places) {
        public LaunchData {
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
