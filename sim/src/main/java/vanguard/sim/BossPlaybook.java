package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, The playbooks and Tells; round 70's lesson; m10h-gaps.md §1 #6):
 * a boss's phase playbooks ({@link PhasePlaybook}), kept by its {@link SetPiece} when its spec has any. In a phase with
 * a playbook its first living source rests, then draws its next move with the shared {@link Playbook} draw (the
 * simulation's seeded random through {@link BossActions#draw}: a replay draws the same): never the move it drew last, a
 * spaced move at most every {@link PhasePlaybook.Move#every}-th draw, a salvo only with a living rack, a line only with
 * the boss's {@link BossLines} (when the rules leave nothing, the spacing gives way first, then the repeat). It tells
 * the move ({@link SimEvents.Type#BOSS_PLAY_TELL}: the muzzle glow, the gun ports, the rack hatches, the bay doors), runs
 * it ({@link SimEvents.Type#BOSS_PLAY}; both at the source, value the move's index) and rests: its rest, or its low rest
 * while the sources hold less than their low share. A new phase starts its own playbook afresh (a rest first); with no
 * source alive none runs.
 *
 * <p>The moves: a told line through the boss's {@link BossLines} (the spinal lance: its telegraph, its lock before it
 * fires, its band; one told line at a time); volleys of an attack's gun (the gold fan); a homing salvo from each living
 * rack under its gun's cap; streams from hull bays under their launcher's cap. Its state is hashed with its piece's
 * (only for a boss with a playbook). Allocates nothing once made.
 */
public final class BossPlaybook {
    /** What the playbook does now. */
    public enum Mode {
        /** Resting (or no playbook in this phase, or no source alive). */
        REST,
        /** Telling its drawn move. */
        TELL,
        /** Running it. */
        RUN
    }

    private static final Mode[] MODES = Mode.values();

    private final SetPiece piece;
    private final List<PhasePlaybook> books;
    private final List<BossSpec.Attack> attacks;
    private final Playbook drawn;
    /** Per move of the running playbook, the draw it was drawn at last ({@link Playbook#NEVER}: not yet). */
    private final int[] drawnAt;

    /** The running playbook's index in {@link #books}; -1 in a phase without one. */
    private int book;

    private int mode;
    private int modeTicks;
    private int restTicks;
    /** The move told or running, its index; -1 while it rests. */
    private int move;
    /** The move drawn last (never twice running); -1 for none. */
    private int last;
    /** The draws so far in this playbook. */
    private int draws;
    /** The running move's volleys, missiles a rack or units a bay so far. */
    private int shots;
    /** A requested line not yet seen live. */
    private boolean pending;

    BossPlaybook(SetPiece piece, BossSpec boss) {
        this.piece = piece;
        books = boss.playbooks();
        attacks = boss.attacks();
        int most = 0;
        for (PhasePlaybook b : books) {
            most = Math.max(most, b.moves().size());
        }
        drawn = new Playbook(most);
        drawnAt = new int[most];
        reset();
    }

    /** Whether {@code boss} has phase playbooks (then its set piece keeps a {@link BossPlaybook}). */
    static boolean any(BossSpec boss) {
        return boss != null && !boss.playbooks().isEmpty();
    }

    /** Back to the level start (and the boss checkpoint): no playbook running. */
    void reset() {
        book = -1;
        enter(-1);
    }

    /** Playbook {@code b} starts (-1: none): resting, nothing drawn. */
    private void enter(int b) {
        book = b;
        mode = Mode.REST.ordinal();
        modeTicks = 0;
        restTicks = b < 0 ? 0 : SimStep.ticks(books.get(b).restSeconds());
        move = -1;
        last = -1;
        draws = 0;
        java.util.Arrays.fill(drawnAt, Playbook.NEVER);
        shots = 0;
        pending = false;
    }

    /**
     * One engaged step with the ship at (shipX, shipY): the phase's playbook rests, draws, tells or runs; its fire
     * reaches the level only while {@code firing}.
     */
    void step(BossActions actions, boolean firing) {
        int b = bookFor(piece.phase());
        if (b != book) {
            enter(b);
        }
        if (book < 0) {
            return;
        }
        PhasePlaybook playbook = books.get(book);
        int src = source(playbook);
        if (src < 0) {
            // Every source dead: no playbook runs (the batteries fire on).
            mode = Mode.REST.ordinal();
            move = -1;
            return;
        }
        if (mode == Mode.REST.ordinal()) {
            if (restTicks > 0) {
                restTicks--;
                return;
            }
            draw(playbook, src, actions);
            return;
        }
        if (mode == Mode.TELL.ordinal()) {
            if (++modeTicks < SimStep.ticks(playbook.tellSeconds())) {
                return;
            }
            mode = Mode.RUN.ordinal();
            modeTicks = 0;
            actions.ring(SimEvents.Type.BOSS_PLAY, piece.partX(src), piece.partY(src), move);
        }
        boolean over = run(playbook.moves().get(move), src, actions, firing);
        modeTicks++;
        if (over) {
            mode = Mode.REST.ordinal();
            modeTicks = 0;
            move = -1;
            shots = 0;
            pending = false;
            restTicks = SimStep.ticks(low(playbook) ? playbook.lowRestSeconds() : playbook.restSeconds());
        }
    }

    private int bookFor(int phase) {
        for (int b = 0; b < books.size(); b++) {
            if (books.get(b).phase() == phase) {
                return b;
            }
        }
        return -1;
    }

    /** The first living source of {@code playbook}; -1 for none. */
    private int source(PhasePlaybook playbook) {
        List<Integer> sources = playbook.sources();
        for (int i = 0; i < sources.size(); i++) {
            if (piece.partHp(sources.get(i)) > 0) {
                return sources.get(i);
            }
        }
        return -1;
    }

    /** Whether the sources hold less than the playbook's low share of their HP (its short rest). */
    private boolean low(PhasePlaybook playbook) {
        List<Integer> sources = playbook.sources();
        double left = 0;
        double full = 0;
        for (int i = 0; i < sources.size(); i++) {
            int p = sources.get(i);
            left += Math.max(0, piece.partHp(p));
            full += piece.spec().parts().get(p).hp();
        }
        return left < playbook.lowBelow() * full;
    }

    /** The next move, by weight among those allowed now; its tell starts (none allowed: it waits a step). */
    private void draw(PhasePlaybook playbook, int src, BossActions actions) {
        List<PhasePlaybook.Move> moves = playbook.moves();
        // The rules in full; then without the spacing; then the repeat too (a pool that would stall).
        for (int pass = 0; pass < 3; pass++) {
            drawn.clear();
            for (int i = 0; i < moves.size(); i++) {
                PhasePlaybook.Move candidate = moves.get(i);
                boolean allowed = available(candidate)
                        && (pass >= 2 || i != last)
                        && (pass >= 1 || Playbook.spaced(draws, drawnAt[i], candidate.every()));
                drawn.offer(candidate.weight(), allowed);
            }
            if (drawn.total() > 0) {
                break;
            }
        }
        if (!(drawn.total() > 0)) {
            return;
        }
        int picked = drawn.pick(actions.draw() * drawn.total());
        draws++;
        drawnAt[picked] = draws;
        last = picked;
        move = picked;
        mode = Mode.TELL.ordinal();
        modeTicks = 0;
        shots = 0;
        pending = false;
        actions.ring(SimEvents.Type.BOSS_PLAY_TELL, piece.partX(src), piece.partY(src), picked);
    }

    /** Whether {@code move} can run now: a line with the boss's lines, a salvo with a living rack. */
    private boolean available(PhasePlaybook.Move move) {
        return switch (move.kind()) {
            case LINE -> piece.lines != null;
            case SALVO -> racksAlive(move) > 0;
            case VOLLEY, STREAM -> true;
        };
    }

    private int racksAlive(PhasePlaybook.Move move) {
        int alive = 0;
        for (int i = 0; i < move.racks().size(); i++) {
            alive += piece.partHp(move.racks().get(i)) > 0 ? 1 : 0;
        }
        return alive;
    }

    /** The running move's step {@link #modeTicks} (0 first) from {@code src}; returns whether it is over. */
    private boolean run(PhasePlaybook.Move now, int src, BossActions actions, boolean firing) {
        int run = modeTicks;
        int gap = Math.max(1, SimStep.ticks(now.gapSeconds()));
        switch (now.kind()) {
            case LINE -> {
                return line(now, src, run, firing);
            }
            case VOLLEY -> {
                if (run == shots * gap && shots < now.count()) {
                    shots++;
                    if (firing) {
                        actions.aimed(
                                piece.partX(src),
                                piece.partY(src),
                                attacks.get(now.attack()).gun());
                    }
                }
                return shots >= now.count();
            }
            case SALVO -> {
                if (run == shots * gap && shots < now.count()) {
                    shots++;
                    EnemyGun gun = attacks.get(now.attack()).gun();
                    for (int i = 0; i < now.racks().size() && firing; i++) {
                        int rack = now.racks().get(i);
                        if (piece.partHp(rack) > 0) {
                            actions.stationMissile(piece.partX(rack), piece.partY(rack), gun);
                        }
                    }
                }
                return shots >= now.count();
            }
            case STREAM -> {
                PhasePlaybook.Bays bays = now.bays().orElseThrow();
                EnemySpec.Launcher launcher = bays.launcher();
                int n = launcher.stream();
                int every = Math.max(1, SimStep.ticks(launcher.gapSeconds()));
                if (run == shots * every && shots < n) {
                    double spread = launcher.spreadRadians();
                    double angle = -StrictMath.PI / 2 + (n == 1 ? 0 : -spread / 2 + shots * spread / (n - 1));
                    shots++;
                    for (int k = 0; k < bays.places().size() && firing; k++) {
                        BossSpec.Offset place = bays.places().get(k);
                        actions.bayLaunch(launcher, piece.x() + place.dx(), piece.y() + place.dy(), angle);
                    }
                }
                return shots >= n;
            }
        }
        return true;
    }

    /**
     * The told line: requested at once from the source; over once it has gone live and run out, or lapsed (its part
     * died, its telegraph cancelled).
     */
    private boolean line(PhasePlaybook.Move now, int src, int run, boolean firing) {
        BossLines lines = piece.lines;
        if (run == 0) {
            if (!firing) {
                return true;
            }
            lines.request(now.attack(), src);
            pending = true;
            return false;
        }
        if (pending && lines.part() == src && lines.live()) {
            pending = false;
        }
        if (pending) {
            return !lines.due(src) && lines.part() != src;
        }
        return lines.part() != src;
    }

    /** What it does now. */
    public Mode mode() {
        return MODES[mode];
    }

    /** The move told or running (its index in its playbook's moves); -1 while it rests. */
    public int move() {
        return move;
    }

    /** The kind of the move told or running; null while it rests. */
    public PhasePlaybook.Kind kind() {
        return move < 0 ? null : books.get(book).moves().get(move).kind();
    }

    /** How far the tell has run, 0–1 (the glows brightening); 0 when it does not tell. */
    public double tellShare() {
        if (mode != Mode.TELL.ordinal()) {
            return 0;
        }
        return Math.min(
                1,
                (double) modeTicks / Math.max(1, SimStep.ticks(books.get(book).tellSeconds())));
    }

    /** The firing source now (the first living source of the phase's playbook); -1 without one. */
    public int source() {
        return book < 0 ? -1 : source(books.get(book));
    }

    /** The draws so far in this phase's playbook. */
    public int draws() {
        return draws;
    }

    void addTo(StateHash hash) {
        hash.add(book)
                .add(mode)
                .add(modeTicks)
                .add(restTicks)
                .add(move)
                .add(last)
                .add(draws)
                .add(shots)
                .add(pending ? 1 : 0);
        for (int at : drawnAt) {
            hash.add(at);
        }
    }
}
