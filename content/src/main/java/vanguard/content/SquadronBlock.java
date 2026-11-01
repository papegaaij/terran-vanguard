package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M10 part F (design/enemies/bosses/honour-guard; m10f-gaps.md §1 rows 1–7): a squadron mid-boss's keys in its stat
 * block ({@link EnemyData}): the boss script's {@code squadron} (its aces on its parts, their joins and stations, the
 * entry tell, the hulls' separation, each ace's hex shield and contact, and the {@code playbook}: the pools, rests, the
 * death cuts, the pincer and the conductor) and the difficulties' {@code squadron} changes. Content's {@link
 * SquadronRules} turns them into the simulation's {@code SquadronSpec}; {@link SquadronChecks} checks them.
 *
 * <p>Heights are the design's: px below the top edge (the spec's "y 70–170"); the simulation's are px up.
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.SquadronData}
 * and so on, without growing that record's file.
 */
public sealed interface SquadronBlock permits EnemyData {
    /** The roles by their slugs, in the simulation's order ({@code SquadronSpec.Role}). */
    List<String> ROLES = List.of("sniper", "dancer", "feinter");

    /** The join edges by their slugs ({@code SquadronSpec.Edge}). */
    List<String> EDGES = List.of("top", "side", "bottom");

    /** The plays by their slugs, in the simulation's order ({@code SquadronPlaybook.Kind}). */
    List<String> PLAYS = List.of(
            "sniper-line",
            "pair-volley",
            "fan",
            "slide",
            "spiral-loop",
            "roll-fan",
            "ribbon",
            "feint-dive",
            "rear-rake",
            "overhead");

    /** The plays each role flies (the others' it only borrows as the last ace, its pool's own). */
    Map<String, List<String>> ROLE_PLAYS = Map.of(
            "sniper", List.of("sniper-line", "pair-volley", "fan", "slide"),
            "dancer", List.of("spiral-loop", "roll-fan", "ribbon"),
            "feinter", List.of("feint-dive", "rear-rake", "overhead"));

    /** How far above the top edge the piece itself is parked, px (its parts are the aces, each flying its own place). */
    double PARK_ABOVE = 400;

    /**
     * The squadron ({@code boss.squadron}): its {@code aces} in their join order (the bar's segments), each first entry
     * told {@code tell} s ahead at its edge, their hulls ({@code hull} px, the sprite's airframe) always {@code
     * separation} px apart edge to edge, each with a hex {@code shield} of that many HP (the enemy shield rule: the HP
     * lever on it), its {@code contact} with the ship, and its {@code playbook}.
     */
    record SquadronData(
            List<AceData> aces,
            double tell,
            double separation,
            Size hull,
            double shield,
            ContactData contact,
            Optional<PlaybookData> playbook) {
        public SquadronData {
            Check.that(aces != null && !aces.isEmpty(), "squadron.aces: one ace at least");
            aces = List.copyOf(aces);
            Check.notNegative("squadron.tell", tell);
            Check.notNegative("squadron.separation", separation);
            Check.positive("squadron.shield", shield);
        }

        /** The index of the ace on part {@code part}; -1 if none. */
        public int aceOn(String part) {
            for (int a = 0; a < aces.size(); a++) {
                if (aces.get(a).part().equals(part)) {
                    return a;
                }
            }
            return -1;
        }
    }

    /**
     * An ace: the boss part it flies ({@code part}), its {@code call_sign} (the bar's tag, at most 7 characters), its
     * {@code role} ({@code sniper}, {@code dancer}, {@code feinter}), its {@code join} and its {@code station}.
     */
    record AceData(String part, String callSign, String role, JoinData join, StationData station) {
        public AceData {
            Check.that(
                    callSign != null && !callSign.isEmpty() && callSign.length() <= 7,
                    "call_sign: 1 to 7 characters, was '" + callSign + "'");
            Check.that(ROLES.contains(role), "role: one of " + ROLES + ", was '" + role + "'");
        }
    }

    /**
     * When an ace's tell starts: the first ace's {@code after} s after the boss's arrival (the sting); a later ace's
     * once the ace before it crossed its edge and {@code after} s passed, that ace is down to {@code at_share} of its HP
     * (none if not given) or ({@code on_first_death}) an ace died, whichever first. It enters by {@code from}: {@code
     * top}, {@code side} (the one away from the leader) or {@code bottom} (the rear).
     */
    record JoinData(String from, double after, Optional<Double> atShare, Optional<Boolean> onFirstDeath) {
        public JoinData {
            Check.that(EDGES.contains(from), "join.from: one of " + EDGES + ", was '" + from + "'");
            Check.notNegative("join.after", after);
            atShare.ifPresent(s -> Check.that(s > 0 && s < 1, "join.at_share: a share above 0, below 1, was " + s));
        }
    }

    /**
     * Where and how fast an ace flies ({@code SquadronSpec.Station}): its band {@code y} [top, bottom] px below the top
     * edge; {@code x} px (the leader's strafe's centre from the left edge, the dancer's eight's centre from the field's
     * middle, the feinter's side lane from its edge); {@code speed} px/s on its station, {@code dash} px/s (the leader's
     * slide, the dancer's roll and ribbon, the feinter's dive; every first entry), {@code climb} px/s (the feinter's rake;
     * the others' speed if not given); {@code span} [least, most] px (the leader's strafe, the dancer's eight across;
     * [0, 0] if not given); {@code keep_off} px from the ship's centre (the dancer's, the feinter's pull-out; 0: none) and
     * {@code committed_keep_off} (the feinter's once an ace died; {@code keep_off} if not given); {@code loop} s (the
     * dancer's full loop; 0 if not given).
     */
    record StationData(
            Span y,
            double x,
            double speed,
            double dash,
            Optional<Double> climb,
            Optional<Span> span,
            Optional<Double> keepOff,
            Optional<Double> committedKeepOff,
            Optional<Double> loop) {
        public StationData {
            Check.that(y.min() >= 0 && y.max() <= vanguard.sim.PlayField.HEIGHT, "station.y: on the play field");
            Check.positive("station.speed", speed);
            Check.positive("station.dash", dash);
            climb.ifPresent(c -> Check.positive("station.climb", c));
            span.ifPresent(s -> Check.notNegative("station.span", s.min()));
            keepOff.ifPresent(k -> Check.notNegative("station.keep_off", k));
            committedKeepOff.ifPresent(k -> Check.notNegative("station.committed_keep_off", k));
            loop.ifPresent(l -> Check.notNegative("station.loop", l));
        }
    }

    /**
     * Touching the ship: {@code damage} (a size tier's contact damage, {@code medium}: 15) to the ship once a {@code
     * interval} s, the ship pushed {@code push} px out, the ace taking {@code self} and flying on.
     */
    record ContactData(Tier damage, double self, double push, double interval) {
        public ContactData {
            Check.notNegative("contact.self", self);
            Check.notNegative("contact.push", push);
            Check.positive("contact.interval", interval);
        }
    }

    /**
     * The aces' playbook ({@code squadron.playbook}): after each play and its rest an ace draws its next from its
     * {@code pools} entry (seeded, never the same twice running), each told {@code tell} s ahead; its {@code rests}
     * alone and in company, the {@code losses} cuts once an ace died, the team move {@code pincer} and the {@code
     * conductor}'s limits.
     */
    record PlaybookData(
            double tell,
            RestsData rests,
            LossesData losses,
            Optional<PincerData> pincer,
            ConductorData conductor,
            List<PoolEntry> pools) {
        public PlaybookData {
            Check.notNegative("playbook.tell", tell);
            Check.that(pools != null && !pools.isEmpty(), "playbook.pools: a pool per ace");
            pools = List.copyOf(pools);
        }
    }

    /** The rest after a play: {@code alone} s while the ace flies alone, {@code company} s with two or three. */
    record RestsData(double alone, double company) {
        public RestsData {
            Check.notNegative("rests.alone", alone);
            Check.notNegative("rests.company", company);
        }
    }

    /** Once an ace died: the survivors' rests shorter by {@code rest} (0.3: −30 %), the last ace's by {@code last_rest}. */
    record LossesData(double rest, double lastRest) {
        public LossesData {
            Check.that(rest >= 0 && rest < 1, "losses.rest: a share below 1, was " + rest);
            Check.that(lastRest >= 0 && lastRest < 1, "losses.last_rest: a share below 1, was " + lastRest);
        }
    }

    /**
     * The team move: the dancer and the feinter cross from both sides at once, each firing {@code attack}'s fan, {@code
     * gap} s apart, while the leader holds fire; drawn with {@code weight} among a flanker's plays, only with every ace
     * flying, at most every {@code every}-th draw.
     */
    record PincerData(double weight, String attack, double gap, int every) {
        public PincerData {
            Check.positive("pincer.weight", weight);
            Check.notNegative("pincer.gap", gap);
            Check.positive("pincer.every", every);
        }
    }

    /** With two or more flying: at most {@code told} told lines or dives at once and {@code firing} aces firing. */
    record ConductorData(int told, int firing) {
        public ConductorData {
            Check.positive("conductor.told", told);
            Check.positive("conductor.firing", firing);
        }
    }

    /** An ace's pool: the {@code ace} (its part's name), its {@code plays} and its {@code signature} play (the last ace borrows it). */
    record PoolEntry(String ace, String signature, List<PlayEntry> plays) {
        public PoolEntry {
            Check.that(plays != null && !plays.isEmpty(), "pools." + ace + ".plays: one play at least");
            plays = List.copyOf(plays);
        }

        /** The index of its play {@code play}; -1 if none. */
        public int playIndex(String play) {
            for (int i = 0; i < plays.size(); i++) {
                if (plays.get(i).play().equals(play)) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * A play of a pool ({@code SquadronPlaybook.Play}): {@code play} one of {@link #PLAYS}, its {@code weight} (1 if not
     * given), the boss {@code attack} it fires (none for the slide), the sniper line's taps' {@code follow} attack, its
     * {@code count} (the volleys, the taps, the dive's burst; 0 if not given) and {@code edge} (the same once an ace
     * died; the spiral's arms then), {@code gap} s between two volleys, shots or taps, a borrowed spiral's {@code seconds}
     * held, its own {@code tell} s (the playbook's if not given) and the ribbon's {@code trail}.
     */
    record PlayEntry(
            String play,
            Optional<Double> weight,
            Optional<String> attack,
            Optional<String> follow,
            Optional<Integer> count,
            Optional<Integer> edge,
            Optional<Double> gap,
            Optional<Double> seconds,
            Optional<Double> tell,
            Optional<TrailData> trail) {
        public PlayEntry {
            Check.that(PLAYS.contains(play), "play: one of " + PLAYS + ", was '" + play + "'");
            weight.ifPresent(w -> Check.positive("weight", w));
            count.ifPresent(n -> Check.notNegative("count", n));
            edge.ifPresent(n -> Check.notNegative("edge", n));
            gap.ifPresent(g -> Check.notNegative("gap", g));
            seconds.ifPresent(s -> Check.notNegative("seconds", s));
            tell.ifPresent(s -> Check.notNegative("tell", s));
            Check.that(trail.isEmpty() || play.equals("ribbon"), "only a ribbon lays a trail");
        }

        /** The simulation's play. */
        vanguard.sim.SquadronPlaybook.Kind kind() {
            return vanguard.sim.SquadronPlaybook.Kind.values()[PLAYS.indexOf(play)];
        }
    }

    /** The ribbon: {@code count} bullets falling at {@code speed} px/s, laid along the pass with {@code gaps} gaps of {@code gap} px. */
    record TrailData(int count, double speed, int gaps, double gap) {
        public TrailData {
            Check.that(count >= 2, "trail.count: 2 or more, was " + count);
            Check.positive("trail.speed", speed);
            Check.that(gaps >= 0 && gaps <= count - 1, "trail.gaps: between the bullets, was " + gaps);
            Check.notNegative("trail.gap", gap);
        }
    }

    /**
     * A difficulty's changes to a squadron ({@code squadron} in the hook): its aces' {@code joins} by part ({@code after},
     * {@code at_share}), every rest × {@code rests}, the feinter's pull-out {@code pull_out} px, the sniper line's {@code
     * taps} and {@code edge_taps} (once an ace died), the dives' own {@code dive_tell} s (the feint dive and the overhead).
     */
    record SquadronChange(
            Optional<Map<String, JoinChange>> joins,
            Optional<Double> rests,
            Optional<Double> pullOut,
            Optional<Integer> taps,
            Optional<Integer> edgeTaps,
            Optional<Double> diveTell) {
        public SquadronChange {
            joins = joins.map(Map::copyOf);
            rests.ifPresent(r -> Check.positive("squadron.rests", r));
            pullOut.ifPresent(p -> Check.positive("squadron.pull_out", p));
            taps.ifPresent(n -> Check.notNegative("squadron.taps", n));
            edgeTaps.ifPresent(n -> Check.notNegative("squadron.edge_taps", n));
            diveTell.ifPresent(t -> Check.notNegative("squadron.dive_tell", t));
        }
    }

    /** An ace's join on a difficulty: {@code after} s and {@code at_share} instead of its own (each kept if not given). */
    record JoinChange(Optional<Double> after, Optional<Double> atShare) {
        public JoinChange {
            after.ifPresent(a -> Check.notNegative("joins.after", a));
            atShare.ifPresent(s -> Check.that(s > 0 && s < 1, "joins.at_share: a share above 0, below 1, was " + s));
        }
    }
}
