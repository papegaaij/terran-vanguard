package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;
import vanguard.content.BowBlock.BatteryFireData;
import vanguard.content.BowBlock.BowPlaybook;
import vanguard.content.BowBlock.CueData;
import vanguard.content.BowBlock.EscapeData;
import vanguard.content.BowBlock.FedShieldData;
import vanguard.content.BowBlock.LastWordsData;
import vanguard.content.BowBlock.MirrorPlace;
import vanguard.content.BowBlock.PartShieldData;
import vanguard.content.BowBlock.RearSweepData;
import vanguard.content.BowBlock.RegenData;
import vanguard.content.BowBlock.RiseData;
import vanguard.content.BowBlock.RiseFireData;
import vanguard.content.BowBlock.RookOrderData;
import vanguard.content.BowBlock.WedgeData;
import vanguard.content.ColossusBlock.DebrisData;
import vanguard.content.ColossusBlock.Eruption;
import vanguard.content.ColossusBlock.Suction;
import vanguard.content.ColossusBlock.Vents;
import vanguard.content.EnemyAttackData.Mirror;
import vanguard.content.EnemyMovementData.Cone;
import vanguard.content.EnemyMovementData.Lance;
import vanguard.content.EnemyMovementData.Plate;
import vanguard.content.EnemyMovementData.Stomp;
import vanguard.content.MawBlock.CoilData;
import vanguard.content.RingBlock.CoreData;
import vanguard.content.RingBlock.FireArc;
import vanguard.content.RingBlock.RingData;
import vanguard.content.RingBlock.RingLosses;
import vanguard.content.RingBlock.TellData;
import vanguard.content.SquadronBlock.SquadronData;

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
     * @param squadron M10 part F: a squadron mid-boss's aces, joins, stations and playbook (the Honour Guard; {@link
     *     SquadronBlock})
     * @param losses M10 part G: a ringed piece's speed-up by its dead batteries, eased (the Halo Platform; {@link
     *     RingBlock})
     * @param tells M10 part G: a ringed piece's told attacks (the Halo's muzzle glow and eye flare)
     * @param wedge M10 part H: a bow boss's body as a wedge (the Ascendant; {@link BowBlock})
     * @param fedShield M10 part H: a part's hex shield fed by other parts (the bow cannon's, by the generators)
     * @param panel M10 part H: the mirror panels' reflect numbers (the unit {@code mirror}'s keys)
     * @param mirrors M10 part H: the parts behind a panel, their facings and turns
     * @param batteryFire M10 part H: the batteries' bursts' arbitration
     * @param playbooks M10 part H: the per-phase playbooks
     * @param cues M10 part H: the phase cues (the {@code boss-phase} radio subjects beside the phases' names)
     * @param regeneration M10 part H: the parts that regenerate
     * @param escape M10 part H: the hull's break and the lifeboat's escape (a boss that ends without dying)
     * @param rearSweep M11 part F, step F2: the boss's rear sweep (Vorne's Chimera; its playbooks' {@code rear-sweep}
     *     moves)
     * @param lastWords M11 part F, step F2: the boss dies on its last words (K: firing stops, its bullets fade, the
     *     burst {@code seconds} later)
     * @param rise M11 part F, step F2: its arrival rising out of a point (the spire well) instead of the descent
     * @param partShields M11 part F, step F2: plain hex shields on its parts (the capsule's)
     * @param rook M11 part F, step F2: Rook's part order per phase
     * @param bar M11 part F, step F2: {@code phases}, the mid-boss bar in segments by phase (the HUD's; part H reuses it)
     * @param echo M11 part G, step G2 (design/enemies/bosses, Boss rush; Level 49): the boss's echo, a stat block of its
     *     own read from the overlay {@code boss.echo} laid over this one ({@link EchoOverlay}); the original is read as
     *     before
     * @param dissolve M11 part G, step G2: an echo's dissolve into motes at its death chain's end, s (the presentation's
     *     look time; the sim's {@code EchoSpec}); only an echo has one
     * @param form M11 part G, step G2: an anchored echo's forming from its arrival, s (the Iron Sovereign's: untouchable
     *     and silent; a descending echo forms by its {@code rise}); 0 if not given
     * @param walls M11 part H, step H2 (the Choir Heart, {@link HeartBlock}): its arena walls in a phase
     * @param drift M11 part H, step H2: its drift in a halted arena's phase
     * @param partWindows M11 part H, step H2: its window parts (the membranes, open only while they sing)
     * @param dark M11 part H, step H2: its phase's dark (a look only: the game's ambient)
     * @param heartbeat M11 part H, step H2: the attacks whose shots raise the heartbeat's event (the pulse in time: the
     *     beat of phases 1–2, then the storm's faster one)
     * @param checkpoints M11 part H, step H2: {@code phases}: the boss checkpoint taken again at each later phase's
     *     start (where the difficulty has one)
     * @param lulls step PT2 (the user's Level 50 playthrough, 2026-11-12; {@link HeartBlock.LullsData}): its lulls in
     *     every phase (the Choir Heart's breaks)
     * @param formFire M11 part G, step G5 (round 93 L.3: "make sure it enters the screen firing real rounds at you"): an
     *     anchored echo's fire while it forms ({@code form}), as a rise's fire ({@link RiseFireData})
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
            Optional<CoreData> core,
            Optional<SquadronData> squadron,
            Optional<RingLosses> losses,
            Optional<List<TellData>> tells,
            Optional<WedgeData> wedge,
            Optional<FedShieldData> fedShield,
            Optional<Mirror> panel,
            Optional<List<MirrorPlace>> mirrors,
            Optional<BatteryFireData> batteryFire,
            Optional<List<BowPlaybook>> playbooks,
            Optional<List<CueData>> cues,
            Optional<List<RegenData>> regeneration,
            Optional<EscapeData> escape,
            Optional<RearSweepData> rearSweep,
            Optional<LastWordsData> lastWords,
            Optional<RiseData> rise,
            Optional<List<PartShieldData>> partShields,
            Optional<List<RookOrderData>> rook,
            Optional<String> bar,
            Optional<EnemyData> echo,
            Optional<Double> dissolve,
            Optional<Double> form,
            Optional<HeartBlock.WallsData> walls,
            Optional<HeartBlock.DriftData> drift,
            Optional<HeartBlock.PartWindowsData> partWindows,
            Optional<HeartBlock.DarkData> dark,
            Optional<List<String>> heartbeat,
            Optional<String> checkpoints,
            Optional<RiseFireData> formFire,
            Optional<HeartBlock.LullsData> lulls) {
        /**
         * M10 part G (design/enemies/ground/halo-platform; Level 41): the kind of a <b>held set piece</b>, not a boss
         * fight: it holds a held gate of its level (no bar, no boss warning, no boss music, no Boss rush bonus).
         */
        public static final String HELD = "held";

        /** M11 part F, step F2: {@code bar: phases}, the bar in segments by phase (Vorne's Chimera; the Heart's). */
        public static final String PHASE_BAR = "phases";

        public BossData {
            Check.that(
                    kind.equals("boss") || kind.equals("mid-boss") || kind.equals(HELD),
                    "kind must be boss, mid-boss or held, was '" + kind + "'");
            tells = tells.map(List::copyOf);
            mirrors = mirrors.map(List::copyOf);
            playbooks = playbooks.map(List::copyOf);
            cues = cues.map(List::copyOf);
            regeneration = regeneration.map(List::copyOf);
            partShields = partShields.map(List::copyOf);
            rook = rook.map(List::copyOf);
            Check.that(bar.isEmpty() || bar.get().equals(PHASE_BAR), "bar: phases (a bar in segments by phase)");
            dissolve.ifPresent(d -> Check.notNegative("dissolve", d));
            form.ifPresent(f -> Check.notNegative("form", f));
            Check.that(form.isEmpty() || dissolve.isPresent(), "only an echo (dissolve) forms in place (form)");
            Check.that(formFire.isEmpty() || form.isPresent(), "form_fire: an echo that forms in place (form) fires");
            Check.that(
                    echo.isEmpty()
                            || echo.get().boss().flatMap(BossData::dissolve).isPresent(),
                    "boss.echo: an echo dissolves at its death (its boss.dissolve)");
            Check.that(lastWords.isEmpty() || escape.isEmpty(), "a boss escapes or dies on its last words, not both");
            heartbeat = heartbeat.map(List::copyOf);
            heartbeat.ifPresent(list -> Check.that(
                    !list.isEmpty() && list.size() <= 2, "heartbeat: one or two attacks (the beat, the faster one)"));
            Check.that(
                    checkpoints.isEmpty() || checkpoints.get().equals(HeartBlock.PHASE_CHECKPOINTS),
                    "checkpoints: phases (a checkpoint at each phase's start)");
            Check.that(mirrors.isEmpty() || panel.isPresent(), "a boss with mirrors has their panel");
            Check.that(batteryFire.isEmpty() || mirrors.isPresent(), "only a boss with mirrors has a battery_fire");
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
                                    && core.isEmpty()
                                    && losses.isEmpty()
                                    && tells.isEmpty()),
                    "only a ringed boss (rings) has a fire_arc, spin_up, grind, speed_ups, cookoff, core, losses or"
                            + " tells");
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

        /** M10 part G: whether it is a held set piece ({@code kind: held}): no bar, warning or boss music. */
        public boolean held() {
            return kind.equals(HELD);
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
     * @param drop M5 part E: the pickup dropped as it begins (the shield cell); M10 part H: with {@code untouchable}, the
     *     opening's drop at {@code drop_at}
     * @param untouchable M10 part H: the phase opens untouchable for this long, s (the Ascendant's eruption)
     * @param dropAt M10 part H: where the opening drops its pickup, px from the piece's centre (y up)
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
            Optional<Pickup> drop,
            Optional<Double> untouchable,
            Optional<Point> dropAt) {
        public PhaseData {
            untouchable.ifPresent(u -> Check.notNegative("untouchable", u));
            Check.that(dropAt.isEmpty() || (untouchable.isPresent() && drop.isPresent()), "drop_at: an opening's drop");
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
     * {@code spawns} in turn from every group it opened. M11 part G, step G2 (the Brood Carrier echo): {@code opens_on:
     * move-end}, a window opens at each playbook move's end instead of on its timer.
     */
    record WindowData(
            List<List<String>> groups,
            double every,
            double open,
            Optional<Double> offset,
            Optional<Boolean> all,
            Optional<List<SpawnData>> spawns,
            Optional<String> opensOn) {
        /** M11 part G, step G2: windows opening at each playbook move's end. */
        public static final String MOVE_END = "move-end";

        public WindowData {
            opensOn.ifPresent(when ->
                    Check.that(MOVE_END.equals(when), "windows.opens_on: " + MOVE_END + ", was '" + when + "'"));
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
