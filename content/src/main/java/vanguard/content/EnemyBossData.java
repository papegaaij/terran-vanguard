package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;
import vanguard.content.ColossusBlock.DebrisData;
import vanguard.content.ColossusBlock.Eruption;
import vanguard.content.ColossusBlock.Suction;
import vanguard.content.ColossusBlock.Vents;
import vanguard.content.EnemyMovementData.Cone;
import vanguard.content.EnemyMovementData.Lance;
import vanguard.content.EnemyMovementData.Plate;
import vanguard.content.EnemyMovementData.Stomp;
import vanguard.content.MawBlock.CoilData;
import vanguard.content.RingBlock.CoreData;
import vanguard.content.RingBlock.FireArc;
import vanguard.content.RingBlock.RingData;

/**
 * A boss's keys in its stat block ({@link EnemyData}, {@code boss:}): its lanes, slams, surfacing, releases,
 * phases, poses, moves, windows, spawns and streams.
 *
 * <p>It only holds these types: {@link EnemyData} implements it, so callers keep naming them {@code EnemyData.X}
 * (Java inherits member types from superinterfaces, JLS 8.5) while the record's file stays small. New code may name
 * the interface directly; an import must (the canonical name: {@code import vanguard.content.EnemyBossData.X}).
 */
public sealed interface EnemyBossData permits EnemyData {
    /**
     * A boss's script: {@code kind} {@code boss} or {@code mid-boss} (the short bar), its bar's
     * name, the par time in s (the Boss rush bonus) and its phases in order.
     *
     * @param engagesOnArrival part G: its first phase starts when it arrives (the bar appearing),
     *     its entrance part of the fight; otherwise when it settles at its hover height
     * @param deathSeconds part G: how long its chained death runs, s (the act boss's 3 s, tail to
     *     head); left out, the mid-boss's quick chain
     * @param poses part G: its further part layouts (the {@code part_list} offsets and the
     *     {@code hitbox} are the arrival pose), which a phase's {@code move} turns it into
     * @param lanes M5 part E: an arena boss's slam lanes (the Harbour Kraken)
     * @param slam M5 part E: its slam cycle (only with lanes)
     * @param plate M7 part F: a walking boss's front plate (the Revenant Walker)
     * @param cone M7 part F: a walking boss's rear flame cone (the Revenant Walker)
     * @param hatchArmour round 44: a walking boss's pods (the parts firing its homing attacks) take damage only while
     *     their hatch is open
     * @param lance round 44: a walking boss's back-mounted rail lance
     * @param stomp round 44: a walking boss's stomp shockwave
     * @param suction M7 part H: a breaching boss's pull toward its open mouth (the Dust Colossus)
     * @param vents M7 part H: a breaching boss's spine vents
     * @param eruption M7 part H: a breaching boss's eruption under the ship
     * @param debris M7 part H: a breaching boss's debris rocks
     * @param coil M8 part H: a coiled arena boss's body and script (the Abyssal Maw; {@link MawBlock})
     * @param rings M9 part H: a ringed boss's rings, outer first (the Iron Sovereign; {@link RingBlock})
     * @param fireArc M9 part H: a ringed boss's fire arc
     * @param spinUp M9 part H: a ringed boss's spin-up from the halt, s ({@link RingBlock#SPIN_UP} if not given)
     * @param grind M9 part H: a locked ring's grind to a halt, s ({@link RingBlock#GRIND} if not given)
     * @param speedUps M9 part H: the survivors' speed factors by the locked rings ({@link RingBlock#SPEED_UPS})
     * @param cookoff M9 part H: the batteries' cook-offs' spacing at the death, s ({@link RingBlock#COOKOFF})
     * @param core M9 part H: a ringed boss's core (its opening, shield, spiral, rail and bays)
     */
    record BossData(
            String kind,
            String barName,
            double par,
            List<PhaseData> phases,
            Optional<Boolean> engagesOnArrival,
            Optional<Double> deathSeconds,
            Optional<List<PoseData>> poses,
            Optional<Lanes> lanes,
            Optional<SlamData> slam,
            Optional<Plate> plate,
            Optional<Cone> cone,
            Optional<Boolean> hatchArmour,
            Optional<Lance> lance,
            Optional<Stomp> stomp,
            Optional<Suction> suction,
            Optional<Vents> vents,
            Optional<Eruption> eruption,
            Optional<DebrisData> debris,
            Optional<CoilData> coil,
            Optional<List<RingData>> rings,
            Optional<FireArc> fireArc,
            Optional<Double> spinUp,
            Optional<Double> grind,
            Optional<List<Double>> speedUps,
            Optional<Double> cookoff,
            Optional<CoreData> core) {
        public BossData {
            Check.that(
                    kind.equals("boss") || kind.equals("mid-boss"),
                    "kind must be boss or mid-boss, was '" + kind + "'");
            Check.positive("par", par);
            Check.notEmpty("phases", phases);
            deathSeconds.ifPresent(d -> Check.notNegative("death_seconds", d));
            Check.that(lanes.isPresent() == slam.isPresent(), "a boss with lanes has a slam cycle, and only it");
            rings = rings.map(List::copyOf);
            speedUps = speedUps.map(List::copyOf);
            spinUp.ifPresent(s -> Check.notNegative("spin_up", s));
            grind.ifPresent(g -> Check.notNegative("grind", g));
            cookoff.ifPresent(c -> Check.notNegative("cookoff", c));
            speedUps.ifPresent(list -> {
                Check.notEmpty("speed_ups", list);
                list.forEach(f -> Check.positive("speed_ups[]", f));
            });
            rings.ifPresent(list -> Check.notEmpty("rings", list));
            Check.that(
                    rings.isPresent()
                            || (fireArc.isEmpty()
                                    && spinUp.isEmpty()
                                    && grind.isEmpty()
                                    && speedUps.isEmpty()
                                    && cookoff.isEmpty()
                                    && core.isEmpty()),
                    "only a ringed boss (rings) has a fire_arc, spin_up, grind, speed_ups, cookoff or core");
            Check.that(rings.isEmpty() || fireArc.isPresent(), "a ringed boss has its fire_arc");
            poses.ifPresent(list -> list.forEach(pose -> Check.that(
                    list.stream()
                                    .filter(other -> other.name().equals(pose.name()))
                                    .count()
                            == 1,
                    "pose names are unique, '" + pose.name() + "' is not")));
        }

        /** The index of pose {@code name} in the boss's poses (0 the arrival pose, the further ones from 1); -1 without one. */
        public int poseIndex(String name) {
            List<PoseData> list = poses.orElse(List.of());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).name().equals(name)) {
                    return i + 1;
                }
            }
            return -1;
        }

        /** Whether it is a mid-boss, with the short bar. */
        public boolean midBoss() {
            return kind.equals("mid-boss");
        }
    }

    /**
     * M5 part E, an arena boss's slam lanes (design/enemies/bosses/harbour-kraken, user decision E5 =
     * a): {@code count} lanes {@code width} px wide from the play field's left edge, lane 1 first;
     * {@code arms}, each slam arm's part name with the lanes (1-based) it owns.
     */
    record Lanes(
            int count,
            double width,
            Optional<java.util.Map<String, List<Integer>>> arms,
            Optional<Boolean> rooted,
            Optional<Double> raised) {
        public Lanes {
            Check.positive("count", count);
            Check.positive("width", width);
            Check.that(
                    count * width <= vanguard.sim.PlayField.WIDTH + 1e-9,
                    "the lanes lie on the play field: " + count + " × " + width + " px");
            arms.ifPresent(map -> map.values()
                    .forEach(owned -> owned.forEach(lane -> Check.that(
                            lane >= 1 && lane <= count, "arms: lane " + lane + " is not one of the " + count))));
            boolean isRooted = rooted.orElse(false);
            Check.that(raised.isEmpty() || isRooted, "only rooted arms take more damage while raised (raised)");
            raised.ifPresent(r -> Check.positive("raised", r));
            if (isRooted) {
                java.util.Set<Integer> owned = new java.util.HashSet<>();
                arms.orElse(java.util.Map.of())
                        .forEach((part, lanes) -> Check.that(
                                lanes.size() == 1 && owned.add(lanes.getFirst()),
                                "a rooted arm owns one lane of its own: '" + part + "' owns " + lanes));
            }
        }

        /** Lanes whose arms are not rooted (M5 part E). */
        public Lanes(int count, double width, Optional<java.util.Map<String, List<Integer>>> arms) {
            this(count, width, arms, Optional.empty(), Optional.empty());
        }

        /** Round 40: whether every arm is a rooted one-lane arm. */
        public boolean isRooted() {
            return rooted.orElse(false);
        }
    }

    /**
     * M5 part E, an arena boss's slam cycle (design/enemies/bosses/harbour-kraken): the lane's
     * {@code telegraph}, the arm's {@code rise}, {@code awash} and {@code sink} times (s); the
     * impact's {@code damage} class on the ship and the escort in the lane (once per slam); the
     * {@code splash} bullets from along the arm; {@code choose: alternate} (the ship's lane and the
     * nearest convoy ship's in turn, the only rule); the hooks {@code telegraph} and {@code lanes} change
     * the telegraph and the lanes a volley slams (one per arm when not given). Round 40: {@code volley}, the
     * lanes a volley slams at medium (the rooted Kraken's 2: the ship's lane and a convoy ship's), one per arm
     * when not given.
     */
    record SlamData(
            double telegraph,
            double rise,
            double awash,
            double sink,
            String damage,
            Splash splash,
            String choose,
            Optional<Integer> volley) {
        public SlamData {
            Check.positive("telegraph", telegraph);
            Check.positive("rise", rise);
            Check.positive("awash", awash);
            Check.positive("sink", sink);
            Check.that(choose.equals("alternate"), "choose must be alternate, was '" + choose + "'");
            volley.ifPresent(v -> Check.positive("volley", v));
        }
    }

    /** The slam's splash: {@code count} bullets of class {@code bullet} at {@code speed} px/s. */
    record Splash(int count, double speed, String bullet) {
        public Splash {
            Check.notNegative("count", count);
            Check.positive("speed", speed);
        }
    }

    /**
     * M5 part E, how a phase slams: {@code chain} (one at a time, the next telegraph when the arm has
     * sunk), {@code after_dive} (one after each dive) or {@code volley} with {@code every} s (one per
     * living arm at once); written as the mode alone or as {@code {mode, every}}.
     */
    record SlammingData(String mode, Optional<Double> every) {
        public SlammingData {
            Check.that(
                    mode.equals("chain") || mode.equals("after_dive") || mode.equals("volley"),
                    "slamming is chain, after_dive or volley, was '" + mode + "'");
            Check.that(every.isPresent() == mode.equals("volley"), "a volley, and only it, has its every");
            every.ifPresent(e -> Check.positive("every", e));
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        static SlammingData of(
                @com.fasterxml.jackson.annotation.JsonProperty("mode") String mode,
                @com.fasterxml.jackson.annotation.JsonProperty("every") Optional<Double> every) {
            Check.that(mode != null, "slamming has a mode");
            return new SlammingData(mode, every == null ? Optional.empty() : every);
        }

        /** The mode written alone. */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static SlammingData of(String mode) {
            return new SlammingData(mode, Optional.empty());
        }
    }

    /**
     * M5 part E, a phase's surfacing part (the Kraken's head): it rises crown first over {@code rise}
     * s (its layer flipping at the middle), stays {@code open} s (its weak spots open), dives over
     * {@code dive} s; {@code glow} s is the tell before each of the phase's attacks; {@code stay: true}
     * keeps it up (no open or dive time then).
     */
    record SurfaceData(
            String part,
            double rise,
            Optional<Double> open,
            Optional<Double> dive,
            Optional<Double> glow,
            Optional<Boolean> stay) {
        public SurfaceData {
            Check.positive("rise", rise);
            boolean stays = stay.orElse(false);
            Check.that(
                    stays ? open.isEmpty() && dive.isEmpty() : open.isPresent() && dive.isPresent(),
                    "a surfacing that stays up has no open or dive time; one that dives has both");
            open.ifPresent(o -> Check.positive("open", o));
            dive.ifPresent(d -> Check.positive("dive", d));
            glow.ifPresent(g -> Check.notNegative("glow", g));
        }
    }

    /**
     * M5 part E, units an arena boss releases once (the arena's Driftjelly field): {@code count}
     * units of {@code enemy} as a {@code field} over {@code lanes}, {@code on: first-surface} (its
     * surfacing part's first rise); they count among the level's enemies as they are released.
     */
    record ReleaseData(String enemy, int count, String formation, List<Integer> lanes, String on) {
        public ReleaseData {
            Check.positive("count", count);
            Check.notEmpty("lanes", lanes);
            Check.that(formation.equals("field"), "a release is a field, was '" + formation + "'");
            Check.that(on.equals("first-surface"), "a release comes on first-surface, was '" + on + "'");
        }
    }

    /**
     * A boss phase: it ends when at most {@code until.left} of {@code until.parts} are alive or
     * {@code until.seconds} after it engaged; it fires its {@code attacks} together or the
     * {@code alternate} ones in turn, sends its {@code streams}, {@code exposes} parts that took no
     * damage before it and bends the chains {@code bend} degrees.
     *
     * @param delay part G: s from the phase engaging to its first attack and window (a concurrent
     *     attack's first volley one interval after that); left out, 1 s for a later phase that
     *     alternates (the frigate's crown opening), else 0
     * @param move part G: its opening move, invulnerable: the glide to a station and a layer, then a
     *     turn into a pose
     * @param windows part G: the parts that open and shut in turn (vulnerable only while open), and
     *     what each opening releases
     * @param slamming M5 part E: how it slams
     * @param surface M5 part E: its surfacing part
     * @param release M5 part E: what it releases once
     * @param drop M5 part E: the pickup dropped as it begins (the shield cell)
     */
    record PhaseData(
            String name,
            Until until,
            Optional<List<String>> attacks,
            Optional<List<String>> alternate,
            Optional<StreamData> streams,
            Optional<List<String>> exposes,
            Optional<Double> bend,
            Optional<Double> delay,
            Optional<MoveData> move,
            Optional<WindowData> windows,
            Optional<SlammingData> slamming,
            Optional<SurfaceData> surface,
            Optional<ReleaseData> release,
            Optional<Pickup> drop) {
        public PhaseData {
            Check.that(attacks.isEmpty() || alternate.isEmpty(), "a phase has attacks or an alternate list, not both");
            bend.ifPresent(b -> Check.notNegative("bend", b));
            delay.ifPresent(d -> Check.notNegative("delay", d));
        }
    }

    /**
     * The end of a phase: at most {@code left} (0 when left out) of {@code parts} alive, or part G
     * {@code seconds} after the phase engaged (after its move), whichever comes first; at least one
     * of the two. M5 part E: or after {@code slams} impacts, or once {@code parts} hold less than the
     * share {@code below} of their HP (instead of {@code left}). M9 part H (a ringed boss, the Iron Sovereign): or, alone,
     * once {@code locks} of its rings are locked (every battery of a ring dead), in any order.
     */
    record Until(
            Optional<List<String>> parts,
            Optional<Integer> left,
            Optional<Double> seconds,
            Optional<Integer> slams,
            Optional<Double> below,
            Optional<Share> share,
            Optional<Integer> locks) {
        public Until {
            Check.that(
                    parts.isPresent() || seconds.isPresent() || slams.isPresent() || locks.isPresent(),
                    "until has parts, seconds, slams or locks");
            locks.ifPresent(n -> Check.positive("locks", n));
            Check.that(
                    locks.isEmpty() || (parts.isEmpty() && seconds.isEmpty() && slams.isEmpty()),
                    "until has its locks alone");
            parts.ifPresent(list -> Check.notEmpty("parts", list));
            left.ifPresent(l -> Check.notNegative("left", l));
            Check.that(left.isEmpty() || parts.isPresent(), "until has a left only with its parts");
            Check.that(
                    below.isEmpty() || (parts.isPresent() && left.isEmpty()),
                    "until has a below with its parts, not a left");
            seconds.ifPresent(s -> Check.positive("seconds", s));
            slams.ifPresent(n -> Check.positive("slams", n));
            below.ifPresent(b -> Check.that(b > 0 && b < 1, "below is a share between 0 and 1, was " + b));
            Check.that(
                    share.isEmpty() || (parts.isPresent() && below.isEmpty()),
                    "until has a share beside its parts (and a left), not with a below");
        }
    }

    /**
     * M7 part F (design/enemies/bosses/revenant-walker, Phases): a phase's second end beside its parts' {@code left}:
     * once {@code parts} hold less than {@code below} of their HP (the walker's first phase: both pods gone, or the
     * hull below 60 %).
     */
    record Share(List<String> parts, double below) {
        public Share {
            parts = List.copyOf(parts);
            Check.notEmpty("parts", parts);
            Check.that(below > 0 && below < 1, "below is a share between 0 and 1, was " + below);
        }
    }

    /**
     * Part G: a further part layout of a boss: its {@code name}, its armoured body's {@code hitbox}
     * (the stat block's when left out) and its parts' {@code offsets} by part name ({@code [dx, dy]}
     * px from the centre, dx right, dy up; a part left out keeps its {@code part_list} offset).
     */
    record PoseData(String name, Optional<Size> hitbox, java.util.Map<String, Point> offsets) {}

    /**
     * Part G: a phase's opening move: over {@code descend} s the boss glides to {@code to}
     * ({@code [x, y]}: px from the left edge, px below the top edge, as {@code hover.y}) and sinks or
     * rises to {@code layer}, then over {@code turn} s it turns in place into {@code pose}; each
     * left out keeps what it has (0 s for the times). It takes no damage and holds its fire until the
     * move ends.
     */
    record MoveData(
            Optional<Point> to,
            Optional<String> layer,
            Optional<Double> descend,
            Optional<String> pose,
            Optional<Double> turn) {
        public MoveData {
            layer.ifPresent(Layers::of);
            descend.ifPresent(d -> Check.notNegative("descend", d));
            turn.ifPresent(t -> Check.notNegative("turn", t));
        }
    }

    /**
     * Part G: a phase's windows: every {@code every} s the next of the {@code groups} (part names, in
     * the order the windows cycle) with a living part on the field opens for {@code open} s (every
     * such group at once when {@code all}), the first {@code offset} s after the phase's delay. A
     * part in a group takes damage only while open. Each opening releases the next of the
     * {@code spawns} in turn from every group it opened.
     */
    record WindowData(
            List<List<String>> groups,
            double every,
            double open,
            Optional<Double> offset,
            Optional<Boolean> all,
            Optional<List<SpawnData>> spawns) {
        public WindowData {
            Check.notEmpty("groups", groups);
            groups.forEach(group -> Check.notEmpty("groups[]", group));
            Check.positive("every", every);
            Check.positive("open", open);
            offset.ifPresent(o -> Check.notNegative("offset", o));
        }
    }

    /**
     * Part G: what an opened window group releases: {@code count} units of {@code enemy} (a group
     * with parts destroyed its share, rounded up) from its open parts, spread over {@code arc} °
     * centred on the ship, flying out at {@code speed} px/s; with a {@code glide} of s they then
     * hold for their hover time, firing, and leave down the screen. The difficulty hook
     * {@code spawns} changes the count by {@code name}.
     */
    record SpawnData(
            String name,
            String enemy,
            int count,
            double speed,
            Optional<Double> arc,
            Optional<Double> glide,
            Optional<AmbushLaunch> ambush) {
        public SpawnData {
            Check.notNegative("count", count);
            Check.positive("speed", speed);
            arc.ifPresent(a -> Check.notNegative("arc", a));
            glide.ifPresent(g -> Check.notNegative("glide", g));
            Check.that(
                    ambush.isEmpty() || (arc.isEmpty() && glide.isEmpty()),
                    "an ambush launch flies its enemy's rear path: no arc or glide");
        }
    }

    /**
     * M5 part H, a window spawn's ambush launch (design/enemies/bosses/siege-spire, the maw's Wraiths;
     * decision H3 = a): each unit flies its enemy's rear-ambush path from the part (cloaked down its lane
     * past the ship, back in at the bottom edge after its gap, warned 3 s ahead, the decloak, the hold,
     * out up a side lane); a launch that would put more than {@code most} of the boss's launched units on
     * the field leaves the rest out.
     */
    record AmbushLaunch(int most) {
        public AmbushLaunch {
            Check.positive("most", most);
        }
    }

    /**
     * Streams of {@code count} units of {@code enemy}, {@code interval} s apart, from the side
     * edges ({@code left}, {@code right} or {@code alternating}): the first when the boss settles,
     * then every {@code every} s while the phase lasts.
     */
    record StreamData(String enemy, int count, double every, double interval, String edge) {
        public StreamData {
            Check.positive("count", count);
            Check.positive("every", every);
            Check.positive("interval", interval);
            Check.that(
                    edge.equals("left") || edge.equals("right") || edge.equals("alternating"),
                    "edge must be left, right or alternating, was '" + edge + "'");
        }
    }
}
