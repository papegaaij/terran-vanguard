package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part F, step F1b (design/enemies/bosses/honour-guard, The playbook; m10f-gaps.md §1 rows 4–6): a squadron's
 * playbook ({@link AcePlaybook}), built per difficulty (the numbers are the difficulty's: easy's rests ×1.3, its single
 * line, its 1-arm spiral; hard's). Each ace, after each play and its rest, draws its next play from its own {@link
 * Pool} with the simulation's seeded random, never the same twice running, each told {@code tellSeconds} ahead (a
 * play may tell longer: easy's dives). The {@link Conductor} spaces the aces; each death changes the survivors (their
 * rests shorter by {@code lossRest}, each its {@link Play#edge}); the last ace rests shorter by {@code lastRest} and
 * also draws the dead aces' signature plays. The {@link Pincer} is the team move.
 *
 * @param pools per ace (the squadron's order), its pool
 * @param tellSeconds a play's tell (0.8 s)
 * @param restAlone the rest after a play while the ace flies alone (1.2 s)
 * @param restCompany the rest after a play with two or three flying (2.0 s)
 * @param lossRest how much shorter the survivors' rests are once an ace died (0.3: −30 %)
 * @param lastRest how much shorter the last ace's rests are (0.5: −50 %)
 * @param pincer the team move; empty: none
 * @param conductor the limits on the aces together
 */
public record SquadronPlaybook(
        List<Pool> pools,
        double tellSeconds,
        double restAlone,
        double restCompany,
        double lossRest,
        double lastRest,
        Optional<Pincer> pincer,
        Conductor conductor) {
    /** What a play does (its ace's flight and fire); the role's own plays first. */
    public enum Kind {
        /**
         * The leader's sniper line: its {@code attack}'s {@code laser-line} ({@link BossSpec.Pattern#LINE}, tracking
         * until its lock), then {@code count} taps of {@code follow}'s line, each requested {@code gapSeconds} after the
         * line before it fired ({@code edge} taps once an ace died). Flown holding its station.
         */
        SNIPER_LINE(false, true, true),
        /** {@code count} volleys of {@code attack}'s aimed gun (the Talon's pair), {@code gapSeconds} apart, holding. */
        PAIR_VOLLEY(false, true, false),
        /** {@code count} volleys of {@code attack}'s fan gun (5-way over 40°), {@code gapSeconds} apart, holding. */
        FAN(false, true, false),
        /** The leader's side-step across the top band ({@link AceMove#SLIDE}); no fire; drawn only with company. */
        SLIDE(true, false, false),
        /**
         * The dancer's full loop ({@link AceMove#LOOP}) firing {@code attack}'s {@link BossSpec.Pattern#SPIRAL} from her
         * tail ({@code edge} arms once an ace died, else the attack's); an ace without a loop of its own (the last ace
         * borrowing it) fires it {@code seconds} holding its station.
         */
        SPIRAL_LOOP(true, true, false),
        /** The dancer's roll past the ship's side ({@link AceMove#ROLL}), {@code attack}'s fan at the roll's middle. */
        ROLL_FAN(true, true, false),
        /** The dancer's pass across the field ({@link AceMove#RIBBON}), laying {@code trail}'s bullets of {@code attack}. */
        RIBBON(true, true, false),
        /**
         * The feinter's dive at the ship's spot, pulling out ({@link AceMove#DIVE}), a burst of {@code count} ({@code
         * edge} once an ace died) aimed shots of {@code attack}, {@code gapSeconds} apart, from its pull-out.
         */
        FEINT_DIVE(true, true, true),
        /** The feinter's climb up a side ({@link AceMove#RAKE}), {@code attack}'s aimed pairs every {@code gapSeconds}. */
        REAR_RAKE(true, true, false),
        /** The feinter's loop out of the top edge and dive from the front ({@link AceMove#OVERHEAD}), as the dive's burst. */
        OVERHEAD(true, true, true);

        private final boolean moves;
        private final boolean fires;
        private final boolean told;

        Kind(boolean moves, boolean fires, boolean told) {
            this.moves = moves;
            this.fires = fires;
            this.told = told;
        }

        /** Whether the ace flies a move for it (else it holds its station). */
        public boolean moves() {
            return moves;
        }

        /** Whether it fires (the conductor's "firing" ace). */
        public boolean fires() {
            return fires;
        }

        /** Whether it is a told line or dive (the conductor's one at a time). */
        public boolean told() {
            return told;
        }
    }

    /**
     * One play of a pool.
     *
     * @param kind what it does
     * @param weight its weight in the draw
     * @param attack the boss attack it fires (an index into {@link BossSpec#attacks()}); -1 for none (the slide)
     * @param follow the sniper line's taps' attack (a shorter telegraph); -1 for none
     * @param count the volleys, the taps, the dive's burst (see {@link Kind})
     * @param edge the same once an ace died (the triple tap: 2 taps; the spiral's arms: 3; the committed burst: 5); 0:
     *     as {@code count} (the spiral: the attack's arms)
     * @param gapSeconds between two volleys, two shots of a burst, the line and its tap
     * @param seconds a borrowed spiral's time while holding (the dancer's loop, 2.5 s); 0 for the others
     * @param tellSeconds its own tell (easy's dives 1.0 s); NaN: the playbook's
     * @param trail the ribbon's bullets; empty for the others
     */
    public record Play(
            Kind kind,
            double weight,
            int attack,
            int follow,
            int count,
            int edge,
            double gapSeconds,
            double seconds,
            double tellSeconds,
            Optional<Trail> trail) {
        public Play {
            if (!(weight > 0)
                    || count < 0
                    || edge < 0
                    || !(gapSeconds >= 0)
                    || !(seconds >= 0)
                    || (!Double.isNaN(tellSeconds) && !(tellSeconds >= 0))
                    || (kind.fires() != (attack >= 0))
                    || (kind != Kind.RIBBON && trail.isPresent())) {
                throw new IllegalArgumentException(
                        "a play has a weight, an attack when it fires, counts and gaps: " + kind);
            }
        }

        /** A play with the playbook's tell. */
        public static Play of(
                Kind kind, double weight, int attack, int follow, int count, int edge, double gapSeconds) {
            return new Play(kind, weight, attack, follow, count, edge, gapSeconds, 0, Double.NaN, Optional.empty());
        }

        /** This play with its own tell. */
        public Play withTell(double seconds) {
            return new Play(kind, weight, attack, follow, count, edge, gapSeconds, this.seconds, seconds, trail);
        }

        /** This play with a borrowed spiral's time ({@link #seconds}). */
        public Play withSeconds(double time) {
            return new Play(kind, weight, attack, follow, count, edge, gapSeconds, time, tellSeconds, trail);
        }

        /** This play laying {@code withTrail}. */
        public Play withTrail(Trail withTrail) {
            return new Play(
                    kind,
                    weight,
                    attack,
                    follow,
                    count,
                    edge,
                    gapSeconds,
                    seconds,
                    tellSeconds,
                    Optional.of(withTrail));
        }
    }

    /**
     * An ace's pool: its plays and its signature (the play the last ace borrows when this ace is dead: the leader's
     * line, the dancer's loop, the feinter's dive).
     */
    public record Pool(List<Play> plays, int signature) {
        public Pool {
            plays = List.copyOf(plays);
            if (plays.isEmpty() || signature < 0 || signature >= plays.size()) {
                throw new IllegalArgumentException("a pool has plays and a signature among them");
            }
        }
    }

    /**
     * The ribbon: {@code count} bullets (at {@code speed} px/s straight down) laid along the pass at even spacing,
     * leaving {@code gaps} clear gaps of {@code gapPx} px each, placed at seeded points between two bullets.
     */
    public record Trail(int count, double speed, int gaps, double gapPx) {
        public Trail {
            if (count < 2 || !(speed > 0) || gaps < 0 || gaps > count - 1 || !(gapPx >= 0)) {
                throw new IllegalArgumentException("a trail has bullets, a speed and gaps between them");
            }
        }
    }

    /**
     * The team move: the dancer and the feinter cross from both sides at once ({@link AceMove#PINCER}), each firing
     * {@code attack}'s fan from its pass, {@code gapSeconds} apart, while the leader holds fire; drawn with {@code
     * weight} among a flanker's plays only with every ace flying, at most every {@code every}-th draw.
     */
    public record Pincer(double weight, int attack, double gapSeconds, int every) {
        public Pincer {
            if (!(weight > 0) || attack < 0 || !(gapSeconds >= 0) || every < 1) {
                throw new IllegalArgumentException("a pincer has a weight, an attack, a gap and a spacing");
            }
        }
    }

    /**
     * The conductor, with two or more aces flying: at most {@code told} told lines or dives at a time (the later is not
     * drawn), at most {@code firing} aces in a firing play (the third only slides or waits), and no ace draws while
     * another's tell runs.
     */
    public record Conductor(int told, int firing) {
        public Conductor {
            if (told < 1 || firing < 1) {
                throw new IllegalArgumentException("a conductor lets one told play and one firing ace at least");
            }
        }
    }

    public SquadronPlaybook {
        pools = List.copyOf(pools);
        if (pools.isEmpty()
                || !(tellSeconds >= 0)
                || !(restAlone >= 0)
                || !(restCompany >= 0)
                || !(lossRest >= 0 && lossRest < 1)
                || !(lastRest >= 0 && lastRest < 1)) {
            throw new IllegalArgumentException("a playbook has pools, a tell, rests and their cuts below 1");
        }
    }

    /** Checks it against its squadron's {@code aces} and its boss's {@code attacks}: a pool an ace, the attacks fit. */
    void check(String barName, int aces, List<BossSpec.Attack> attacks) {
        if (pools.size() != aces) {
            throw new IllegalArgumentException(barName + ": a playbook has a pool per ace");
        }
        for (Pool pool : pools) {
            for (Play play : pool.plays()) {
                if (play.kind() == Kind.RIBBON && play.trail().isEmpty()) {
                    throw new IllegalArgumentException(barName + ": a ribbon lays a trail");
                }
                checkAttack(barName, play.kind() + "", play.attack(), attacks, pattern(play.kind()));
                if (play.kind() == Kind.SNIPER_LINE && (play.count() > 0 || play.edge() > 0)) {
                    checkAttack(barName, "a tap", play.follow(), attacks, BossSpec.Pattern.LINE);
                }
            }
        }
        if (pincer.isPresent()) {
            checkAttack(barName, "the pincer", pincer.get().attack(), attacks, null);
        }
    }

    private static BossSpec.Pattern pattern(Kind kind) {
        return switch (kind) {
            case SNIPER_LINE -> BossSpec.Pattern.LINE;
            case SPIRAL_LOOP -> BossSpec.Pattern.SPIRAL;
            default -> null;
        };
    }

    private static void checkAttack(
            String barName, String what, int attack, List<BossSpec.Attack> attacks, BossSpec.Pattern pattern) {
        if (attack < 0) {
            return;
        }
        if (attack >= attacks.size()
                || (pattern != null && attacks.get(attack).pattern() != pattern)
                || (pattern == null && attacks.get(attack).pattern() == BossSpec.Pattern.LINE)) {
            throw new IllegalArgumentException(barName + ": " + what + " fires a boss attack of its kind: " + attack);
        }
    }
}
