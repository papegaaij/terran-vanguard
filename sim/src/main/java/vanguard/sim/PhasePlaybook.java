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
 */
public record PhasePlaybook(
        int phase,
        List<Integer> sources,
        double tellSeconds,
        double restSeconds,
        double lowRestSeconds,
        double lowBelow,
        List<Move> moves,
        List<Integer> lowParts) {
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
        MOUNTS
    }

    /** M10 part H, step H1b: a bile lob's mark is never within this of the ship's x at the mark, px. */
    public static final double LOB_CLEAR_X = 60;

    /**
     * Hull bays: the launcher's units come out of each place (offsets from the boss's centre, px right and up), straight
     * down the screen, spread over the launcher's spread.
     */
    public record Bays(EnemySpec.Launcher launcher, List<BossSpec.Offset> places) {
        public Bays {
            java.util.Objects.requireNonNull(launcher);
            places = List.copyOf(places);
            if (places.isEmpty()) {
                throw new IllegalArgumentException("bays have places");
            }
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
     */
    public record Move(
            Kind kind,
            double weight,
            int attack,
            int count,
            double gapSeconds,
            int every,
            List<Integer> racks,
            Optional<Bays> bays) {
        public Move {
            racks = List.copyOf(racks);
            if (!(weight > 0)
                    || count < 0
                    || !(gapSeconds >= 0)
                    || every < 1
                    || (kind == Kind.STREAM || kind == Kind.BROOD) != bays.isPresent()
                    || (kind == Kind.STREAM || kind == Kind.BROOD) != (attack < 0)
                    || (kind != Kind.BROOD && (kind == Kind.SALVO || kind == Kind.MOUNTS) == racks.isEmpty())
                    || ((kind == Kind.VOLLEY || kind == Kind.SALVO || kind == Kind.LOBS || kind == Kind.MOUNTS)
                            && count < 1)) {
                throw new IllegalArgumentException(
                        "a move has a weight, an attack or bays, counts and a spacing: " + kind);
            }
        }

        /** A move of {@code kind} with an attack, drawn freely, without racks or bays. */
        public static Move of(Kind kind, double weight, int attack, int count, double gapSeconds) {
            return new Move(kind, weight, attack, count, gapSeconds, 1, List.of(), Optional.empty());
        }

        /** This move drawn at most every {@code draws}-th draw. */
        public Move every(int draws) {
            return new Move(kind, weight, attack, count, gapSeconds, draws, racks, bays);
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
        this(phase, sources, tellSeconds, restSeconds, lowRestSeconds, lowBelow, moves, List.of());
    }

    /** This playbook with its low rest by {@code parts}' HP. */
    public PhasePlaybook withLowParts(List<Integer> parts) {
        return new PhasePlaybook(phase, sources, tellSeconds, restSeconds, lowRestSeconds, lowBelow, moves, parts);
    }

    public PhasePlaybook {
        sources = List.copyOf(sources);
        moves = List.copyOf(moves);
        lowParts = List.copyOf(lowParts);
        if (phase < 0
                || sources.isEmpty()
                || moves.isEmpty()
                || !(tellSeconds >= 0)
                || !(restSeconds >= 0)
                || !(lowRestSeconds >= 0)
                || !(lowBelow >= 0 && lowBelow <= 1)) {
            throw new IllegalArgumentException("a phase playbook has a phase, sources, moves, a tell and rests");
        }
    }

    /** Checks it against its boss's {@code attacks} and {@code phases}: a line fires a line attack, a salvo homes. */
    void check(String barName, List<BossSpec.Attack> attacks, int phases) {
        if (phase >= phases) {
            throw new IllegalArgumentException(barName + ": a playbook runs in one of its phases: " + phase);
        }
        for (Move move : moves) {
            if (move.kind() == Kind.STREAM || move.kind() == Kind.BROOD) {
                continue;
            }
            if (move.attack() >= attacks.size()) {
                throw new IllegalArgumentException(barName + ": a move fires one of its attacks: " + move.attack());
            }
            BossSpec.Attack attack = attacks.get(move.attack());
            boolean line = attack.pattern() == BossSpec.Pattern.LINE;
            boolean homing = attack.gun().homing().isPresent();
            if ((move.kind() == Kind.LINE) != line || (move.kind() == Kind.SALVO) != homing) {
                throw new IllegalArgumentException(
                        barName + ": a line move fires a line attack, a salvo a homing one: " + move.kind());
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
    }
}
