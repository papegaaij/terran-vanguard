package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, The playbooks; round 70's lesson; m10h-gaps.md §1 #6): a boss
 * phase's playbook ({@link BossPlaybook}). In phase {@code phase} the first living part of {@code sources} rests, then
 * draws its next move by weight with the simulation's seeded random ({@link Playbook}), never the move it drew last and
 * a spaced move at most every {@link Move#every}-th draw, tells it {@code tellSeconds}, runs it and rests again: {@code
 * restSeconds}, or {@code lowRestSeconds} while the sources hold less than {@code lowBelow} of their HP. With every
 * source dead no playbook runs (the Ascendant's cannon dead first: the batteries fire on). M10 part H, step H1b: the
 * low rest goes by {@code lowParts}' HP when it names any (phase 2's: below half of the sacs' and the heart's HP).
 *
 * @param phase the phase it runs in (0 first)
 * @param sources the parts it draws from, the first living one firing (the cannon; phase 2's heart, then its sacs)
 * @param tellSeconds every move's tell (0.8 s)
 * @param restSeconds the rest after a move (1.5 s)
 * @param lowRestSeconds the rest below {@code lowBelow} (0.8 s)
 * @param lowBelow the sources' HP share below which the rest is short (0.5)
 * @param moves its moves
 * @param lowParts M10 part H, step H1b: the parts whose HP share sets the low rest; empty: the sources'
 * @param lanceEvery M10 round 80 fixes, R80a: a told line ({@link #lance}) drawn at least every {@code lanceEvery}-th
 *     draw (0: no such rule; the Ascendant's phase 1: 2)
 * @param fallback M10 round 81 fixes, R81a: with every source dead, the first living of these parts draws on, but only
 *     the moves that do not fire from the source itself (a salvo from its racks, a stream from its bays, a barrage);
 *     empty: no playbook runs with its sources dead (the Ascendant's phase 1: the generators, then the batteries, so a
 *     cannon killed first leaves no quiet hull)
 * @param ensemble M11 part H, step H1b (design/enemies/bosses/choir-heart, The playbooks): the rules of a playbook whose
 *     moves fire from parts of their own ({@link Move#source}: the membranes' songs): each living source at least once in
 *     any {@link Ensemble#coverEvery} draws, two moves at once ({@link Parallel}), the last source's moves ({@link
 *     Ensemble#lastMoves}); {@link Ensemble#NONE} for every other playbook
 */
public record PhasePlaybook(
        int phase,
        List<Integer> sources,
        double tellSeconds,
        double restSeconds,
        double lowRestSeconds,
        double lowBelow,
        List<Move> moves,
        List<Integer> lowParts,
        int lanceEvery,
        List<Integer> fallback,
        Ensemble ensemble) {
    /** What a move does. */
    public enum Kind {
        /**
         * A told line: {@code attack}'s {@code laser-line} ({@link BossSpec.Pattern#LINE}, its telegraph, lock and band:
         * the spinal lance) requested from the source through the boss's {@link BossLines}; over once it has fired and
         * run out (or lapsed).
         */
        LINE,
        /** {@code count} volleys of {@code attack}'s gun from the source (its fan: the gold fan), {@code gapSeconds} apart. */
        VOLLEY,
        /**
         * A homing salvo: {@code count} of {@code attack}'s missiles from each living part of {@code racks} (a dead
         * generator's rack is dead), {@code gapSeconds} apart, under its gun's cap alive.
         */
        SALVO,
        /** A launch: each of {@code bays} sends a stream of its launcher's units (its stream, its gap, its cap). */
        STREAM,
        /**
         * M10 part H, step H1b (the Chimera brood): a part swells and splits: the first living part of {@code racks}
         * (a sac), else the source (the heart, with no sac left), sends its launcher's stream from each of {@code
         * bays}' places (offsets from that part) under the launcher's cap of boss-launched units alive.
         */
        BROOD,
        /**
         * M10 part H, step H1b (the spore spiral): {@code attack}'s {@code spiral} from the source (its arms, their
         * turn and its gun's rate) for its duration.
         */
        SPIRAL,
        /**
         * M10 part H, step H1b (the bile lobs): {@code count} of {@code attack}'s mortar lobs from the source, {@code
         * gapSeconds} apart, each at a marked point of the play plane never within {@link #LOB_CLEAR_X} px of the ship's
         * x (its flight the mark's time; its burst a ring).
         */
        LOBS,
        /**
         * M10 part H, step H1b (the grafted guns): {@code count} volleys of {@code attack}'s gun from each of {@code
         * racks}' places, living or dead (the dead hull's shoulder mounts), {@code gapSeconds} apart.
         */
        MOUNTS,
        /**
         * M10 round 80 fixes, R80a (the lance sweep): {@code attack}'s told line as {@link #LINE}, its beam then turning
         * {@link Move#sweepRadians} over its live time toward the side of the source the ship was not on at the draw.
         */
        SWEEP,
        /**
         * M10 round 80 fixes, R80a (twin lances): {@code attack}'s told line from {@link Move#flankPx} beside the source,
         * the ship's side first, and a second from the other flank {@code gapSeconds} later (re-aimed at the ship then);
         * as the first fires, one volley of the {@link Move#with} attack's gun from the source.
         */
        TWIN,
        /**
         * M10 round 80 fixes, R80a (the battery barrage): each living part of {@code racks} in turn, {@code gapSeconds}
         * apart, tells and fires its battery burst ({@link BatteryBursts#cue}); over once they are all done.
         */
        BARRAGE,
        /**
         * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The rear sweep): the boss's {@link RearSweep} (its
         * {@link BossSpec#rearSweep()}): out of the top edge, up from the bottom edge on the side the ship is not on,
         * climbing to its station while firing; over once it is back in its sway. It counts as a told line for {@link
         * PhasePlaybook#lanceEvery} ({@link PhasePlaybook#countsAsLance}).
         */
        REAR_SWEEP,
        /**
         * M11 part H, step H1b (design/enemies/bosses/choir-heart, the spore drop): {@code count} of {@code attack}'s
         * spores (its gun's mine) thrown from the source, {@code gapSeconds} apart, each to a point of the play plane
         * never within {@link Move#clearPx} px of the ship's x (drawn with the seeded random) near its height; there it
         * holds, bursting on contact or at its life's end into its ring.
         */
        SPORES,
        /**
         * M11 part H, step H1b (design/enemies/bosses/choir-heart, the wall spit): {@code count} volleys, {@code
         * gapSeconds} apart, of {@code attack}'s gun (the aimed pair) from a vein point on each arena wall's face ({@link
         * ArenaWalls}), each at a height never within {@link Move#clearPx} px of the ship's; drawn only while the walls
         * stand.
         */
        WALL_SPIT
    }

    /** M10 round 80 fixes, R80a: whether a move of {@code kind} fires a told line (the lance, its sweep, the twins). */
    public static boolean lance(Kind kind) {
        return kind == Kind.LINE || kind == Kind.SWEEP || kind == Kind.TWIN;
    }

    /**
     * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, phase 3: "a lance or the rear sweep at least every
     * third draw"): whether a move of {@code kind} counts for {@link #lanceEvery}: a told line or the rear sweep.
     */
    public static boolean countsAsLance(Kind kind) {
        return lance(kind) || kind == Kind.REAR_SWEEP;
    }

    /**
     * R81a: whether a move of {@code kind} fires from racks, bays or batteries rather than from its source (what a
     * {@link #fallback} part may still draw).
     */
    public static boolean offSource(Kind kind) {
        return kind == Kind.SALVO || kind == Kind.STREAM || kind == Kind.BARRAGE;
    }

    /** M11 part H, step H1b: whether a move of {@code kind} releases units (a stream, a brood): one of a pair at most. */
    public static boolean releases(Kind kind) {
        return kind == Kind.STREAM || kind == Kind.BROOD;
    }

    /** M11 part H, step H1b: a spore's throw from its source to its point, s (pending the user's review in round 95). */
    public static final double SPORE_THROW_SECONDS = 0.6;

    /** M10 part H, step H1b: a bile lob's mark is never within this of the ship's x at the mark, px. */
    public static final double LOB_CLEAR_X = 60;

    /**
     * Hull bays: the launcher's units come out of each place (offsets from the boss's centre, px right and up), straight
     * down the screen, spread over the launcher's spread.
     *
     * <p>M11 part G, step G1 (design/enemies/bosses/brood-carrier, Echo: "4 Skitters or 2 Needlers alternating, from
     * the open pair"): a brood's {@code turns} alternate with its {@code launcher}, one launcher a brood in turn (the
     * launcher first); with {@code fromOpen} it splits from each of its racks whose window is open now (the open sac
     * pair; else the first living rack, as before).
     *
     * <p>M11 part H, step H1b (design/enemies/bosses/choir-heart, the seed scatter, the mote cloud, the pit brood): with
     * {@code toShip} each place's fan is centred on the direction from it to the ship instead of straight down; with
     * {@code onWalls} the places are wall pits on the boss's {@link ArenaWalls}: a place's {@code dx} sign picks the wall
     * (below 0 the left, else the right), its size how far in from the wall's face, its {@code dy} its height (px up from
     * the bottom edge), so a pit rides its wall (drawn only while the walls stand).
     *
     * @param turns M11 part G, step G1: the launchers taking turns after {@code launcher}; empty: the launcher alone
     * @param fromOpen M11 part G, step G1: a brood splits from each open rack
     * @param toShip M11 part H, step H1b: each place's fan aims at the ship
     * @param onWalls M11 part H, step H1b: the places are wall pits
     */
    public record Bays(
            EnemySpec.Launcher launcher,
            List<BossSpec.Offset> places,
            List<EnemySpec.Launcher> turns,
            boolean fromOpen,
            boolean toShip,
            boolean onWalls) {
        public Bays {
            java.util.Objects.requireNonNull(launcher);
            places = List.copyOf(places);
            turns = List.copyOf(turns);
            if (places.isEmpty()) {
                throw new IllegalArgumentException("bays have places");
            }
        }

        /** Bays of one launcher (without M11 part G's turns). */
        public Bays(EnemySpec.Launcher launcher, List<BossSpec.Offset> places) {
            this(launcher, places, List.of(), false);
        }

        /** Bays straight down from the boss (without M11 part H's aim and wall pits). */
        public Bays(
                EnemySpec.Launcher launcher,
                List<BossSpec.Offset> places,
                List<EnemySpec.Launcher> turns,
                boolean fromOpen) {
            this(launcher, places, turns, fromOpen, false, false);
        }

        /** M11 part G, step G1: these bays' broods alternating {@code launchers} after their own, from the open racks. */
        public Bays alternating(List<EnemySpec.Launcher> launchers, boolean open) {
            return new Bays(launcher, places, launchers, open, toShip, onWalls);
        }

        /** M11 part H, step H1b: these bays' fans aimed at the ship ({@link #toShip}). */
        public Bays towardShip() {
            return new Bays(launcher, places, turns, fromOpen, true, onWalls);
        }

        /** M11 part H, step H1b: these bays as wall pits ({@link #onWalls}). */
        public Bays wallPits() {
            return new Bays(launcher, places, turns, fromOpen, toShip, true);
        }

        /** M11 part G, step G1: the launcher of brood {@code n} (0 first): its own, then its turns in order. */
        public EnemySpec.Launcher launcher(int n) {
            int k = n % (turns.size() + 1);
            return k == 0 ? launcher : turns.get(k - 1);
        }
    }

    /**
     * One move.
     *
     * @param kind what it does
     * @param weight its weight in the draw
     * @param attack its boss attack (an index into {@link BossSpec#attacks()}); -1 for a stream
     * @param count the volleys, or the missiles from each rack
     * @param gapSeconds between two volleys or two missiles of a rack
     * @param every drawn at most every {@code every}-th draw (1: no limit; the Dart stream 3)
     * @param racks a salvo's racks; empty for the others
     * @param bays a stream's bays; empty for the others
     * @param with R80a: a twin's volley attack (an index into {@link BossSpec#attacks()}); -1 for none
     * @param sweepRadians R80a: a sweep's turn over its live time, radians (0 for the others); M11 part G, step G1 (the
     *     Brood Carrier echo's mandible sweep): a volley's centre stepping this far over its volleys from the ship
     *     toward the side of the source the ship was not on at the draw (its first volley at the ship)
     * @param flankPx R80a: a twin's lines' origins beside the source's centre, px (0 for the others)
     * @param cover R81a: a covering volley attack (an index into {@link BossSpec#attacks()}) fired from the source
     *     as the move ends (a told line's also as its line is drawn), so the field is never quiet through the rest, the
     *     next tell and a telegraph (from a fallback part, its own gun); -1 for none
     * @param notFirst M11 part F, step F1 (the rear sweep's "never the first of the phase"): never its playbook's first
     *     draw in a phase
     * @param source M11 part H, step H1b (design/enemies/bosses/choir-heart, a membrane's song): the part it fires from,
     *     tells at and (with {@link PartWindows}) opens from its tell's first frame to its last bullet; drawn only while
     *     it lives (or, as one of the {@link Ensemble#lastMoves}, from the last living source); -1: the playbook's source
     * @param clearPx M11 part H, step H1b: a spore's point never within this of the ship's x, a wall spit's vein point
     *     never within this of the ship's height, px (0 for the others)
     */
    public record Move(
            Kind kind,
            double weight,
            int attack,
            int count,
            double gapSeconds,
            int every,
            List<Integer> racks,
            Optional<Bays> bays,
            int with,
            double sweepRadians,
            double flankPx,
            int cover,
            boolean notFirst,
            int source,
            double clearPx) {
        public Move {
            racks = List.copyOf(racks);
            boolean launches = kind == Kind.STREAM || kind == Kind.BROOD;
            boolean attackless = launches || kind == Kind.BARRAGE || kind == Kind.REAR_SWEEP;
            if (!(weight > 0)
                    || count < 0
                    || !(gapSeconds >= 0)
                    || every < 1
                    || launches != bays.isPresent()
                    || attackless != (attack < 0)
                    || (kind != Kind.BROOD
                            && (kind == Kind.SALVO || kind == Kind.MOUNTS || kind == Kind.BARRAGE) == racks.isEmpty())
                    || ((kind == Kind.VOLLEY
                                    || kind == Kind.SALVO
                                    || kind == Kind.LOBS
                                    || kind == Kind.MOUNTS
                                    || kind == Kind.SPORES
                                    || kind == Kind.WALL_SPIT)
                            && count < 1)
                    || source < -1
                    || !(clearPx >= 0)
                    || (kind == Kind.TWIN) != (with >= 0)
                    || (kind == Kind.SWEEP && !(sweepRadians > 0))
                    || !(sweepRadians >= 0)
                    || (sweepRadians > 0 && kind != Kind.SWEEP && kind != Kind.VOLLEY)
                    || (kind == Kind.TWIN) != (flankPx > 0)) {
                throw new IllegalArgumentException(
                        "a move has a weight, an attack or bays, counts and a spacing: " + kind);
            }
        }

        /** A move from the playbook's source (without M11 part H's own source and clearance). */
        public Move(
                Kind kind,
                double weight,
                int attack,
                int count,
                double gapSeconds,
                int every,
                List<Integer> racks,
                Optional<Bays> bays,
                int with,
                double sweepRadians,
                double flankPx,
                int cover,
                boolean notFirst) {
            this(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    notFirst,
                    -1,
                    0);
        }

        /** M11 part F, step F1: a move that may be drawn first ({@link #notFirst}). */
        public Move(
                Kind kind,
                double weight,
                int attack,
                int count,
                double gapSeconds,
                int every,
                List<Integer> racks,
                Optional<Bays> bays,
                int with,
                double sweepRadians,
                double flankPx,
                int cover) {
            this(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    false);
        }

        /** R80a: a move without a cover ({@link #cover}). */
        public Move(
                Kind kind,
                double weight,
                int attack,
                int count,
                double gapSeconds,
                int every,
                List<Integer> racks,
                Optional<Bays> bays,
                int with,
                double sweepRadians,
                double flankPx) {
            this(kind, weight, attack, count, gapSeconds, every, racks, bays, with, sweepRadians, flankPx, -1);
        }

        /** R81a: this move covered by {@code attack}'s volley as it ends (a told line's also as its line is drawn). */
        public Move covered(int attack) {
            return new Move(
                    kind,
                    weight,
                    this.attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    attack,
                    notFirst,
                    source,
                    clearPx);
        }

        /** M11 part F, step F1: this move never drawn first in its phase ({@link #notFirst}). */
        public Move neverFirst() {
            return new Move(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    true,
                    source,
                    clearPx);
        }

        /** M11 part F, step F1: the boss's rear sweep ({@link RearSweep}) drawn with {@code weight}. */
        public static Move rearSweep(double weight) {
            return new Move(Kind.REAR_SWEEP, weight, -1, 0, 0, 1, List.of(), Optional.empty());
        }

        /** A move without a twin's volley, a sweep or flanks. */
        public Move(
                Kind kind,
                double weight,
                int attack,
                int count,
                double gapSeconds,
                int every,
                List<Integer> racks,
                Optional<Bays> bays) {
            this(kind, weight, attack, count, gapSeconds, every, racks, bays, -1, 0, 0);
        }

        /** A move of {@code kind} with an attack, drawn freely, without racks or bays. */
        public static Move of(Kind kind, double weight, int attack, int count, double gapSeconds) {
            return new Move(kind, weight, attack, count, gapSeconds, 1, List.of(), Optional.empty());
        }

        /** R80a: a lance sweep of {@code attack} turning {@code radians} over its live time. */
        public static Move sweep(double weight, int attack, double radians) {
            return new Move(Kind.SWEEP, weight, attack, 1, 0, 1, List.of(), Optional.empty(), -1, radians, 0);
        }

        /**
         * M11 part G, step G1 (the Brood Carrier echo's mandible sweep): {@code count} volleys of {@code attack}, {@code
         * gapSeconds} apart, their centre stepping {@code radians} away from the ship's side over them.
         */
        public static Move sweptVolley(double weight, int attack, int count, double gapSeconds, double radians) {
            return new Move(
                    Kind.VOLLEY, weight, attack, count, gapSeconds, 1, List.of(), Optional.empty(), -1, radians, 0);
        }

        /** R80a: twin lances of {@code attack}, {@code flankPx} beside the source, {@code apartSeconds} apart. */
        public static Move twin(double weight, int attack, int with, double apartSeconds, double flankPx) {
            return new Move(
                    Kind.TWIN, weight, attack, 1, apartSeconds, 1, List.of(), Optional.empty(), with, 0, flankPx);
        }

        /** R80a: a battery barrage from {@code racks} in turn, {@code gapSeconds} apart. */
        public static Move barrage(double weight, List<Integer> racks, double gapSeconds) {
            return new Move(Kind.BARRAGE, weight, -1, racks.size(), gapSeconds, 1, racks, Optional.empty());
        }

        /** This move drawn at most every {@code draws}-th draw. */
        public Move every(int draws) {
            return new Move(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    draws,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    notFirst,
                    source,
                    clearPx);
        }

        /** M11 part H, step H1b: this move fired from part {@code part} ({@link #source}; -1: the playbook's source). */
        public Move from(int part) {
            return new Move(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    notFirst,
                    part,
                    clearPx);
        }

        /** M11 part H, step H1b: this move's clearance from the ship, px ({@link #clearPx}). */
        public Move clear(double px) {
            return new Move(
                    kind,
                    weight,
                    attack,
                    count,
                    gapSeconds,
                    every,
                    racks,
                    bays,
                    with,
                    sweepRadians,
                    flankPx,
                    cover,
                    notFirst,
                    source,
                    px);
        }

        /**
         * M11 part H, step H1b: {@code count} spores of {@code attack} thrown {@code gapSeconds} apart, each never within
         * {@code clearPx} of the ship's x ({@link Kind#SPORES}).
         */
        public static Move spores(double weight, int attack, int count, double gapSeconds, double clearPx) {
            return of(Kind.SPORES, weight, attack, count, gapSeconds).clear(clearPx);
        }

        /**
         * M11 part H, step H1b: {@code count} wall spits of {@code attack} {@code gapSeconds} apart, their vein points
         * never within {@code clearPx} of the ship's height ({@link Kind#WALL_SPIT}).
         */
        public static Move wallSpit(double weight, int attack, int count, double gapSeconds, double clearPx) {
            return of(Kind.WALL_SPIT, weight, attack, count, gapSeconds).clear(clearPx);
        }
    }

    /** A playbook whose low rest goes by its sources' HP. */
    public PhasePlaybook(
            int phase,
            List<Integer> sources,
            double tellSeconds,
            double restSeconds,
            double lowRestSeconds,
            double lowBelow,
            List<Move> moves) {
        this(phase, sources, tellSeconds, restSeconds, lowRestSeconds, lowBelow, moves, List.of(), 0, List.of());
    }

    /** A playbook without M11 part H's {@link Ensemble} rules. */
    public PhasePlaybook(
            int phase,
            List<Integer> sources,
            double tellSeconds,
            double restSeconds,
            double lowRestSeconds,
            double lowBelow,
            List<Move> moves,
            List<Integer> lowParts,
            int lanceEvery,
            List<Integer> fallback) {
        this(
                phase,
                sources,
                tellSeconds,
                restSeconds,
                lowRestSeconds,
                lowBelow,
                moves,
                lowParts,
                lanceEvery,
                fallback,
                Ensemble.NONE);
    }

    /** This playbook with its low rest by {@code parts}' HP. */
    public PhasePlaybook withLowParts(List<Integer> parts) {
        return new PhasePlaybook(
                phase,
                sources,
                tellSeconds,
                restSeconds,
                lowRestSeconds,
                lowBelow,
                moves,
                parts,
                lanceEvery,
                fallback,
                ensemble);
    }

    /** R80a: this playbook with a told line at least every {@code draws}-th draw (0: none). */
    public PhasePlaybook withLanceEvery(int draws) {
        return new PhasePlaybook(
                phase,
                sources,
                tellSeconds,
                restSeconds,
                lowRestSeconds,
                lowBelow,
                moves,
                lowParts,
                draws,
                fallback,
                ensemble);
    }

    /** R81a: this playbook drawing on from {@code parts} with its sources dead ({@link #fallback}). */
    public PhasePlaybook withFallback(List<Integer> parts) {
        return new PhasePlaybook(
                phase,
                sources,
                tellSeconds,
                restSeconds,
                lowRestSeconds,
                lowBelow,
                moves,
                lowParts,
                lanceEvery,
                parts,
                ensemble);
    }

    /** M11 part H, step H1b: this playbook with the {@code rules} of moves from parts of their own ({@link Ensemble}). */
    public PhasePlaybook withEnsemble(Ensemble rules) {
        return new PhasePlaybook(
                phase,
                sources,
                tellSeconds,
                restSeconds,
                lowRestSeconds,
                lowBelow,
                moves,
                lowParts,
                lanceEvery,
                fallback,
                rules);
    }

    /**
     * M11 part H, step H1b (design/enemies/bosses/choir-heart, The playbooks; m11h-gaps.md §1 #2): the rules of a
     * playbook whose moves fire from parts of their own ({@link Move#source}), the content's {@code cover_every}, {@code
     * parallel} and {@code fallback}.
     *
     * @param coverEvery each living source of its moves is drawn at least once in any {@code coverEvery} draws
     *     (the membranes: 8); 0: no such rule. The spacing gives way to it, the repeat rule does not.
     * @param parallel two moves at once ({@link Parallel}); empty: one at a time
     * @param lastMoves the moves (indexes into the playbook's moves) that the last living source of the moves draws as
     *     its own once theirs is dead (the last membrane: its own song and the Herald's by turns): the content's
     *     {@code fallback}
     */
    public record Ensemble(int coverEvery, Optional<Parallel> parallel, List<Integer> lastMoves) {
        /** No such rules (every playbook before M11 part H). */
        public static final Ensemble NONE = new Ensemble(0, Optional.empty(), List.of());

        public Ensemble {
            java.util.Objects.requireNonNull(parallel);
            lastMoves = List.copyOf(lastMoves);
            if (coverEvery < 0 || coverEvery == 1) {
                throw new IllegalArgumentException("a source is covered in 2 draws or more: " + coverEvery);
            }
        }

        /** Whether it has any rule (its playbook's draw and state follow them). */
        public boolean any() {
            return coverEvery > 0 || parallel.isPresent() || !lastMoves.isEmpty();
        }

        /** Whether {@code move} (its index) is one of the last source's moves. */
        public boolean lastMove(int move) {
            for (int i = 0; i < lastMoves.size(); i++) {
                if (lastMoves.get(i) == move) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * M11 part H, step H1b: two moves at once, the second's tell {@code gapSeconds} after the first's (0.4 s): never two
     * told lines together with {@code oneLine}, at most one releasing units with {@code oneRelease}, never two moves of
     * one source, never the pair drawn last again; from {@code fromHalf}: only once half of the playbook's sources are
     * dead (phase 1's "from the phase's half"), else always (phase 3). The rest follows when both are over; each counts
     * as a draw of its own (the spacing, the lines' rule, the cover).
     */
    public record Parallel(double gapSeconds, boolean oneLine, boolean oneRelease, boolean fromHalf) {
        public Parallel {
            if (!(gapSeconds >= 0)) {
                throw new IllegalArgumentException(
                        "the second move's tell comes a gap after the first's: " + gapSeconds);
            }
        }
    }

    public PhasePlaybook {
        java.util.Objects.requireNonNull(ensemble);
        sources = List.copyOf(sources);
        moves = List.copyOf(moves);
        lowParts = List.copyOf(lowParts);
        fallback = List.copyOf(fallback);
        if (phase < 0
                || sources.isEmpty()
                || moves.isEmpty()
                || !(tellSeconds >= 0)
                || !(restSeconds >= 0)
                || !(lowRestSeconds >= 0)
                || !(lowBelow >= 0 && lowBelow <= 1)
                || lanceEvery < 0
                || (lanceEvery > 0 && moves.stream().noneMatch(move -> countsAsLance(move.kind())))) {
            throw new IllegalArgumentException("a phase playbook has a phase, sources, moves, a tell and rests");
        }
    }

    /** Checks it against its boss's {@code attacks} and {@code phases}: a line fires a line attack, a salvo homes. */
    void check(String barName, List<BossSpec.Attack> attacks, int phases) {
        if (phase >= phases) {
            throw new IllegalArgumentException(barName + ": a playbook runs in one of its phases: " + phase);
        }
        for (Move move : moves) {
            if (move.kind() == Kind.STREAM
                    || move.kind() == Kind.BROOD
                    || move.kind() == Kind.BARRAGE
                    || move.kind() == Kind.REAR_SWEEP) {
                continue;
            }
            if (move.with() >= attacks.size()
                    || (move.with() >= 0
                            && (attacks.get(move.with()).pattern() == BossSpec.Pattern.LINE
                                    || attacks.get(move.with()).gun().homing().isPresent()))) {
                throw new IllegalArgumentException(barName + ": a twin's volley fires one of its plain attacks");
            }
            if (move.attack() >= attacks.size()) {
                throw new IllegalArgumentException(barName + ": a move fires one of its attacks: " + move.attack());
            }
            BossSpec.Attack attack = attacks.get(move.attack());
            boolean line = attack.pattern() == BossSpec.Pattern.LINE;
            boolean homing = attack.gun().homing().isPresent();
            if (lance(move.kind()) != line || (move.kind() == Kind.SALVO) != homing) {
                throw new IllegalArgumentException(
                        barName + ": a line move fires a line attack, a salvo a homing one: " + move.kind());
            }
            // M11 part H, step H1b: a spore drop throws a mine layer's spores.
            if (move.kind() == Kind.SPORES && attack.gun().mine().isEmpty()) {
                throw new IllegalArgumentException(barName + ": a spore drop throws a mine layer's spores");
            }
            // M10 part H, step H1b: a spiral move fires a spiral attack, a lobs move a mortar's lob.
            if ((move.kind() == Kind.SPIRAL) != (attack.pattern() == BossSpec.Pattern.SPIRAL)
                    || (move.kind() == Kind.LOBS) != (attack.pattern() == BossSpec.Pattern.LOB)) {
                throw new IllegalArgumentException(
                        barName + ": a spiral move fires a spiral attack, a lobs move a lob: " + move.kind());
            }
        }
    }

    /** Checks its parts against the boss's {@code parts}. */
    void checkParts(String slug, int parts) {
        for (int p : sources) {
            BossSpec.checkPart(slug, "a playbook's source", p, parts);
        }
        for (Move move : moves) {
            for (int p : move.racks()) {
                BossSpec.checkPart(slug, "a salvo's rack", p, parts);
            }
        }
        for (int p : lowParts) {
            BossSpec.checkPart(slug, "a playbook's low part", p, parts);
        }
        for (int p : fallback) {
            BossSpec.checkPart(slug, "a playbook's fallback part", p, parts);
        }
        // M11 part H, step H1b: a move's own source is one of its parts; the last source's moves are its moves.
        for (Move move : moves) {
            if (move.source() >= 0) {
                BossSpec.checkPart(slug, "a move's source", move.source(), parts);
            }
        }
        for (int m : ensemble.lastMoves()) {
            if (m < 0 || m >= moves.size()) {
                throw new IllegalArgumentException(slug + ": the last source's moves are its playbook's: " + m);
            }
        }
    }
}
