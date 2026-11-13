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
 * rack under its gun's cap; streams from hull bays under their launcher's cap. M10 part H, step H1b (the Vrell
 * playbook): a brood from a swelling part (the first living sac, else the source) under its launcher's cap of
 * boss-launched units alive; an attack's spiral from the source for its duration; mortar lobs at marked points never
 * within {@link PhasePlaybook#LOB_CLEAR_X} px of the ship's x (drawn with the seeded random); volleys from fixed mounts,
 * living or dead (the grafted guns). Its state is hashed with its piece's (only for a boss with a playbook). Allocates
 * nothing once made.
 *
 * <p>M11 part H, step H1b (design/enemies/bosses/choir-heart, The playbooks; m11h-gaps.md §1 #2, #6): a move with a
 * source of its own ({@link PhasePlaybook.Move#source}: a membrane's song) fires, tells and covers from that part, is
 * drawn only while it lives (or from the last living source, one of the {@link PhasePlaybook.Ensemble#lastMoves}) and
 * opens its part window from its tell's first frame to its last bullet ({@link PartWindows}); each living source at least
 * once in any {@link PhasePlaybook.Ensemble#coverEvery} draws (the earliest due first; the spacing gives way to it); two
 * moves at once in two <em>lanes</em> ({@link PhasePlaybook.Parallel}: the second's tell a gap after the first's, never
 * two told lines, at most one releasing, never two of one source; the rest once both are over); the spore drop (spores
 * thrown to points clear of the ship), the wall spit (aimed pairs from the arena walls' vein points clear of the ship's
 * height) and bays as wall pits or aimed at the ship. A playbook without them draws and runs one lane as before, its
 * state hashed as before.
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

    /** M11 part H, step H1b: the lanes (two moves at once at most). */
    public static final int LANES = 2;
    /** M11 part H, step H1b: a lane's state while its drawn move waits for its tell (the second of a pair). */
    private static final int WAIT = 3;
    /** M11 part H, step H1b: no source is due ({@link PhasePlaybook.Ensemble#coverEvery}). */
    private static final int NONE_DUE = Integer.MAX_VALUE;

    private final SetPiece piece;
    private final List<PhasePlaybook> books;
    private final List<BossSpec.Attack> attacks;
    private final Playbook drawn;
    /** Per move of the running playbook, the draw it was drawn at last ({@link Playbook#NEVER}: not yet). */
    private final int[] drawnAt;

    /** The running playbook's index in {@link #books}; -1 in a phase without one. */
    private int book;

    private int restTicks;
    /** The move drawn last (never twice running); -1 for none. M11 part H, step H1b: a pair's first. */
    private int last;
    /** The draws so far in this playbook. */
    private int draws;
    /** M10 round 80 fixes, R80a: the draws since the last told line ({@link PhasePlaybook#lanceEvery}). */
    private int sinceLance;
    /**
     * M11 part G, step G1: the broods run so far in this playbook (the launchers' turns, {@link
     * PhasePlaybook.Bays#launcher(int)}); hashed only for a boss whose bays take turns.
     */
    private int broods;
    /** M11 part G, step G1: whether a playbook's bays take turns or split from the open racks. */
    private final boolean alternates;

    // The lanes (lane 0 is every playbook's one lane; its state hashed in the order it always was).
    private final int[] mode = new int[LANES];
    private final int[] modeTicks = new int[LANES];
    /** Per lane the move told or running, its index; -1 while it rests. */
    private final int[] move = new int[LANES];
    /** Per lane the running move's volleys, missiles a rack or units a bay so far. */
    private final int[] shots = new int[LANES];
    /** Per lane a requested line not yet seen live. */
    private final boolean[] pending = new boolean[LANES];
    /** M10 part H, step H1b: per lane the running spiral's arm angle, radians, and its steps to the next bullets. */
    private final double[] spiralAngle = new double[LANES];

    private final int[] spiralTicks = new int[LANES];
    /** R80a: per lane the drawn sweep's side, +1 toward the right (the ship was on the left at the draw), -1 the left. */
    private final int[] side = new int[LANES];
    /**
     * M11 part G, step G1: per lane the running brood's racks (bits by their place in its racks) that were open as it
     * began: it splits from them to its end (its window may shut meanwhile); 0: from its one part.
     */
    private final int[] broodFrom = new int[LANES];
    /**
     * M11 part H, step H1b: per lane the part its move fires from when the move has a source of its own (fixed at the
     * draw); -1: the playbook's first living source, each step.
     */
    private final int[] laneSource = new int[LANES];
    /** M11 part H, step H1b: per lane the part whose window it holds open (its song); -1 for none. */
    private final int[] laneSinger = new int[LANES];
    /** M11 part H, step H1b: per lane the steps left before its tell (a pair's second). */
    private final int[] waitTicks = new int[LANES];

    // M11 part H, step H1b: the ensemble's state (hashed only for a boss with its rules, own sources or part windows).
    private final boolean ensembles;
    /** The other move of the pair drawn last; -1 for none (a single draw). */
    private int lastOther;
    /** Per part, the draw a move of its own was drawn at last (0: the phase's start). */
    private final int[] sourceDrawnAt;
    /** Scratch: the due draws of the living sources (derived, not hashed). */
    private final int[] deadlines;
    /** Per playbook, the parts its moves fire from (their own sources, each once). */
    private final int[][] singers;

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
        boolean turns = false;
        boolean own = boss.partWindows().isPresent();
        for (PhasePlaybook b : books) {
            own |= b.ensemble().any();
            for (PhasePlaybook.Move m : b.moves()) {
                turns |= m.bays().isPresent()
                        && (!m.bays().get().turns().isEmpty() || m.bays().get().fromOpen());
                own |= m.source() >= 0;
            }
        }
        alternates = turns;
        ensembles = own;
        int parts = piece.spec().parts().size();
        sourceDrawnAt = new int[parts];
        deadlines = new int[parts];
        coversTells = new boolean[books.size()];
        bayParts = new int[books.size()][];
        singers = new int[books.size()][];
        for (int b = 0; b < books.size(); b++) {
            List<PhasePlaybook.Move> moves = books.get(b).moves();
            bayParts[b] = new int[moves.size()];
            for (int m = 0; m < moves.size(); m++) {
                coversTells[b] |= moves.get(m).kind() == PhasePlaybook.Kind.REAR_SWEEP;
                bayParts[b][m] = moves.get(m).kind() == PhasePlaybook.Kind.STREAM
                        ? bayPart(piece.spec(), moves.get(m).bays().orElseThrow())
                        : -1;
            }
            singers[b] = moves.stream()
                    .mapToInt(PhasePlaybook.Move::source)
                    .filter(p -> p >= 0)
                    .distinct()
                    .toArray();
        }
        reset();
    }

    /**
     * M11 part F, step F1c: per playbook, whether it fires its told move's cover as each tell starts too (a playbook
     * with a rear sweep: the Chimera's phases 2 and 3).
     */
    private final boolean[] coversTells;

    /**
     * M11 part F, step F1c: per playbook and move, a stream's bay part (the part whose centre is its one bay place; -1
     * for none: hull bays, as the Ascendant's): its stream is drawn and launches only while that part lives.
     */
    private final int[][] bayParts;

    /** M11 part F, step F1c: the part a stream's bays sit on: its one place on a part's centre (within 1 px); else -1. */
    static int bayPart(SetPieceSpec spec, PhasePlaybook.Bays bays) {
        if (bays.places().size() != 1 || bays.onWalls()) {
            return -1;
        }
        BossSpec.Offset place = bays.places().get(0);
        for (int p = 0; p < spec.parts().size(); p++) {
            PartSpec part = spec.parts().get(p);
            if (part.hp() > 0 && Math.abs(part.dx() - place.dx()) <= 1 && Math.abs(part.dy() - place.dy()) <= 1) {
                return p;
            }
        }
        return -1;
    }

    /** M11 part F, step F1c: whether move {@code m} of the running playbook is a stream whose bay part is dead. */
    private boolean bayDead(int m) {
        if (m < 0) {
            return false;
        }
        int part = bayParts[book][m];
        return part >= 0 && piece.partHp(part) <= 0;
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
        restTicks = b < 0 ? 0 : SimStep.ticks(books.get(b).restSeconds());
        last = -1;
        draws = 0;
        java.util.Arrays.fill(drawnAt, Playbook.NEVER);
        sinceLance = 0;
        broods = 0;
        for (int l = 0; l < LANES; l++) {
            mode[l] = Mode.REST.ordinal();
            modeTicks[l] = 0;
            move[l] = -1;
            shots[l] = 0;
            pending[l] = false;
            spiralAngle[l] = 0;
            spiralTicks[l] = 0;
            side[l] = 0;
            broodFrom[l] = 0;
            laneSource[l] = -1;
            laneSinger[l] = -1;
            waitTicks[l] = 0;
        }
        lastOther = -1;
        java.util.Arrays.fill(sourceDrawnAt, 0);
        if (piece.partWindows != null) {
            // M11 part H, step H1b: no part sings into a new phase.
            piece.partWindows.silence();
        }
        if (piece.rearSweep != null) {
            // M11 part F, step F1: a sweep told but not started is called off with its phase.
            piece.rearSweep.untell();
        }
    }

    /** The ship's place this step (a lob's marks keep off its x); derived each step, not hashed. */
    private double shipX;

    private double shipY;

    /**
     * One engaged step with the ship at (shipX, shipY): the phase's playbook rests, draws, tells or runs; its fire
     * reaches the level only while {@code firing}.
     */
    void step(double shipX, double shipY, BossActions actions, boolean firing) {
        this.shipX = shipX;
        this.shipY = shipY;
        int b = bookFor(piece.phase());
        if (b != book) {
            enter(b);
        }
        if (book < 0) {
            return;
        }
        PhasePlaybook playbook = books.get(book);
        int src = source(playbook);
        fallen = src >= 0 && !isSource(playbook, src);
        if (src < 0) {
            // Every source dead: no playbook runs (the batteries fire on).
            for (int l = 0; l < LANES; l++) {
                unsing(l);
                mode[l] = Mode.REST.ordinal();
                move[l] = -1;
            }
            return;
        }
        if (idle()) {
            if (piece.rearSweep != null && piece.rearSweep.flying()) {
                // M11 part F, step F1: a rear sweep outliving its move (its phase ended mid-climb) ends before a draw.
                return;
            }
            if (restTicks > 0) {
                restTicks--;
                return;
            }
            if (piece.lulls != null && piece.lulls.due()) {
                // Step PT2: no move is drawn while a lull is due or runs.
                return;
            }
            draw(playbook, src, actions, firing);
            return;
        }
        boolean ended = false;
        for (int l = 0; l < LANES; l++) {
            if (mode[l] != Mode.REST.ordinal()) {
                ended |= stepLane(l, playbook, src, actions, firing);
            }
        }
        if (ended && idle()) {
            restTicks = SimStep.ticks(low(playbook) ? playbook.lowRestSeconds() : playbook.restSeconds());
        }
    }

    /** Step PT2: whether it rests now (every lane; a lull may start). */
    boolean resting() {
        return idle();
    }

    /** Whether every lane rests. */
    private boolean idle() {
        for (int l = 0; l < LANES; l++) {
            if (mode[l] != Mode.REST.ordinal()) {
                return false;
            }
        }
        return true;
    }

    /** The part lane {@code l} fires from: its move's own source, else the playbook's source {@code src} now. */
    private int from(int l, int src) {
        return laneSource[l] >= 0 ? laneSource[l] : src;
    }

    /** One step of busy lane {@code l}: its wait, its tell or its run; returns whether its move ended now. */
    private boolean stepLane(int l, PhasePlaybook playbook, int src, BossActions actions, boolean firing) {
        if (laneSource[l] >= 0 && piece.partHp(laneSource[l]) <= 0) {
            // M11 part H, step H1b: its singer died mid-song: the song ends there (no cover from a dead part).
            endLane(l);
            return true;
        }
        int from = from(l, src);
        if (mode[l] == WAIT) {
            // M11 part H, step H1b: a pair's second waits its gap, then tells.
            if (--waitTicks[l] > 0) {
                return false;
            }
            tell(l, playbook, from, actions, firing);
            return false;
        }
        if (mode[l] == Mode.TELL.ordinal()) {
            int tellTicks = SimStep.ticks(playbook.tellSeconds());
            ++modeTicks[l];
            PhasePlaybook.Move told = playbook.moves().get(move[l]);
            if (told.kind() == PhasePlaybook.Kind.BARRAGE && modeTicks[l] < tellTicks) {
                // R81a: the barrage's first battery tells under the barrage's own tell, so it fires as the tell ends.
                barrageCue(l, told, modeTicks[l], tellTicks, actions, firing);
            }
            if (modeTicks[l] < tellTicks) {
                return false;
            }
            mode[l] = Mode.RUN.ordinal();
            modeTicks[l] = 0;
            int at = at(told, from);
            actions.ring(SimEvents.Type.BOSS_PLAY, piece.partX(at), piece.partY(at), move[l]);
        }
        PhasePlaybook.Move now = playbook.moves().get(move[l]);
        boolean over = run(l, now, from, actions, firing);
        modeTicks[l]++;
        if (over && firing && now.cover() >= 0) {
            // R81a: every covered move ends with its cover from the source (the bow's ports), closing on the ship
            // through the rest and the next tell (a told line's beam dying, a volley's last fan, a stream's last Dart;
            // from a fallback part, its own gun). M11 part H, step H1b: from the part that fired (the aimed pair).
            actions.aimed(
                    piece.partX(from),
                    piece.partY(from),
                    attacks.get(now.cover()).gun());
        }
        if (over) {
            // M11 part G, step G1: windows that open on a move's end open now (the Brood Carrier echo's sac pairs).
            piece.windows.moveEnded();
            if (now.kind() == PhasePlaybook.Kind.BROOD) {
                broods++;
            }
            endLane(l);
            return true;
        }
        return false;
    }

    /** Lane {@code l}'s move is over (or called off): it rests, its singer's window closes. */
    private void endLane(int l) {
        unsing(l);
        mode[l] = Mode.REST.ordinal();
        modeTicks[l] = 0;
        move[l] = -1;
        shots[l] = 0;
        pending[l] = false;
        spiralAngle[l] = 0;
        spiralTicks[l] = 0;
        laneSource[l] = -1;
        waitTicks[l] = 0;
    }

    /** M11 part H, step H1b: lane {@code l}'s singer stops singing (its window closes but for a hold). */
    private void unsing(int l) {
        if (laneSinger[l] >= 0) {
            piece.partWindows.sing(laneSinger[l], false);
            laneSinger[l] = -1;
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

    /**
     * The first living source of {@code playbook}; R81a: with none, the first living part of its fallback (then only
     * the moves off the source are drawn: {@link #fallen}); -1 for none.
     */
    private int source(PhasePlaybook playbook) {
        List<Integer> sources = playbook.sources();
        for (int i = 0; i < sources.size(); i++) {
            if (piece.partHp(sources.get(i)) > 0) {
                return sources.get(i);
            }
        }
        List<Integer> fallback = playbook.fallback();
        for (int i = 0; i < fallback.size(); i++) {
            if (piece.partHp(fallback.get(i)) > 0) {
                return fallback.get(i);
            }
        }
        return -1;
    }

    private static boolean isSource(PhasePlaybook playbook, int part) {
        for (int i = 0; i < playbook.sources().size(); i++) {
            if (playbook.sources().get(i) == part) {
                return true;
            }
        }
        return false;
    }

    /** R81a: whether the playbook draws from its fallback now (its sources dead); derived each step, not hashed. */
    private boolean fallen;

    /** Whether the sources hold less than the playbook's low share of their HP (its short rest). */
    private boolean low(PhasePlaybook playbook) {
        // M10 part H, step H1b: by its low parts' HP when it names any (phase 2's: the sacs' and the heart's).
        List<Integer> sources = playbook.lowParts().isEmpty() ? playbook.sources() : playbook.lowParts();
        double left = 0;
        double full = 0;
        for (int i = 0; i < sources.size(); i++) {
            int p = sources.get(i);
            left += Math.max(0, piece.partHp(p));
            full += piece.spec().parts().get(p).hp();
        }
        return left < playbook.lowBelow() * full;
    }

    /**
     * The next move, by weight among those allowed now; its tell starts (none allowed: it waits a step). M11 part H, step
     * H1b: with two moves at once, a second for lane 1, its tell after the gap.
     */
    private void draw(PhasePlaybook playbook, int src, BossActions actions, boolean firing) {
        int prevA = last;
        int prevB = lastOther;
        int first = pick(playbook, src, -1, prevA, prevB, actions);
        if (first < 0) {
            return;
        }
        start(0, first, playbook, src);
        tell(0, playbook, from(0, src), actions, firing);
        int second = -1;
        java.util.Optional<PhasePlaybook.Parallel> parallel =
                playbook.ensemble().parallel();
        if (parallel.isPresent() && (!parallel.get().fromHalf() || halfDead(playbook))) {
            second = pick(playbook, src, first, prevA, prevB, actions);
            if (second >= 0) {
                start(1, second, playbook, src);
                mode[1] = WAIT;
                waitTicks[1] = Math.max(1, SimStep.ticks(parallel.get().gapSeconds()));
            }
        }
        last = first;
        lastOther = second;
    }

    /**
     * One draw for a lane: the move picked by weight among those allowed (the rules in full; then without the spacing;
     * then without the repeat, the lines' rule, the first draw's and the cover), beside lane 0's {@code partner} (-1
     * for lane 0); {@link Playbook#NONE} when none is allowed (no draw consumed). The draw counts.
     */
    private int pick(PhasePlaybook playbook, int src, int partner, int prevA, int prevB, BossActions actions) {
        List<PhasePlaybook.Move> moves = playbook.moves();
        // The rules in full; then without the spacing; then the repeat too (a pool that would stall).
        // R80a: a told line at least every lanceEvery-th draw (given way with the repeat).
        // M11 part F, step F1: the rear sweep counts as a told line (phase 3's "a lance or the rear sweep").
        boolean needLance = playbook.lanceEvery() > 0
                && sinceLance >= playbook.lanceEvery() - 1
                && lanceAvailable(playbook, src, partner);
        // M11 part H, step H1b: the earliest due source when one must be drawn now (each in any coverEvery draws).
        int due = ensembles ? due(playbook) : NONE_DUE;
        for (int pass = 0; pass < 3; pass++) {
            drawn.clear();
            for (int i = 0; i < moves.size(); i++) {
                PhasePlaybook.Move candidate = moves.get(i);
                boolean allowed = available(playbook, i, src)
                        && (partner < 0 || pairs(playbook, partner, i, src))
                        && (pass >= 2 || (i != prevA && i != prevB))
                        && (pass >= 2 || !needLance || PhasePlaybook.countsAsLance(candidate.kind()))
                        && (pass >= 2 || draws > 0 || !candidate.notFirst())
                        && (pass >= 2 || due == NONE_DUE || deadline(playbook, i, src) == due)
                        && (pass >= 1 || Playbook.spaced(draws, drawnAt[i], candidate.every()));
                drawn.offer(candidate.weight(), allowed);
            }
            if (drawn.total() > 0) {
                break;
            }
        }
        if (!(drawn.total() > 0)) {
            return Playbook.NONE;
        }
        int picked = drawn.pick(actions.draw() * drawn.total());
        draws++;
        sinceLance = PhasePlaybook.countsAsLance(moves.get(picked).kind()) ? 0 : sinceLance + 1;
        drawnAt[picked] = draws;
        if (ensembles) {
            int own = ownSource(playbook, picked, src);
            if (own >= 0) {
                sourceDrawnAt[own] = draws;
            }
        }
        return picked;
    }

    /** Lane {@code l} takes move {@code picked} (drawn now): its own source fixed, nothing run yet. */
    private void start(int l, int picked, PhasePlaybook playbook, int src) {
        move[l] = picked;
        mode[l] = Mode.TELL.ordinal();
        modeTicks[l] = 0;
        shots[l] = 0;
        pending[l] = false;
        laneSource[l] = ownSource(playbook, picked, src);
        waitTicks[l] = 0;
    }

    /**
     * Lane {@code l}'s tell starts from part {@code from}: the sweep's side, the tell's event, the singer's window opens
     * (M11 part H, step H1b), the rear sweep's flare, a covered tell's pair.
     */
    private void tell(int l, PhasePlaybook playbook, int from, BossActions actions, boolean firing) {
        mode[l] = Mode.TELL.ordinal();
        modeTicks[l] = 0;
        PhasePlaybook.Move told = playbook.moves().get(move[l]);
        // R80a: a sweep turns toward the side of the source the ship is not on now (its tell shows it).
        side[l] = shipX < piece.partX(from) ? 1 : -1;
        int at = at(told, from);
        actions.ring(SimEvents.Type.BOSS_PLAY_TELL, piece.partX(at), piece.partY(at), move[l]);
        if (piece.partWindows != null && piece.partWindows.fitted(from)) {
            // M11 part H, step H1b: its singer's window opens on the tell's first frame.
            piece.partWindows.sing(from, true);
            laneSinger[l] = from;
        }
        if (told.kind() == PhasePlaybook.Kind.REAR_SWEEP) {
            // M11 part F, step F1: the far flank flares, Rook calls it.
            piece.rearSweep.tell(shipX, actions);
        }
        if (coversTells[book] && firing && told.cover() >= 0) {
            // M11 part F, step F1c: a playbook with a rear sweep also fires its told move's cover as the tell starts,
            // so a pair is still closing through the tell after the rest (the spec's no-quiet rule with the sacs dead:
            // phase 3's capsule alone).
            actions.aimed(
                    piece.partX(from),
                    piece.partY(from),
                    attacks.get(told.cover()).gun());
        }
    }

    /** The part a move tells and runs at: a brood's swelling part, else the source. */
    private int at(PhasePlaybook.Move move, int src) {
        return move.kind() == PhasePlaybook.Kind.BROOD ? brooder(move, src) : src;
    }

    /**
     * M10 part H, step H1b: the part a brood splits from: the first living of its racks, else the source. M11 part G,
     * step G1: from the open racks first when its bays say so.
     */
    private int brooder(PhasePlaybook.Move move, int src) {
        if (move.bays().isPresent() && move.bays().get().fromOpen()) {
            for (int i = 0; i < move.racks().size(); i++) {
                if (piece.partOpen(move.racks().get(i))) {
                    return move.racks().get(i);
                }
            }
        }
        for (int i = 0; i < move.racks().size(); i++) {
            if (piece.partHp(move.racks().get(i)) > 0) {
                return move.racks().get(i);
            }
        }
        return src;
    }

    /** R81a: whether a told line can be drawn now (a fallen playbook draws none), beside lane 0's {@code partner}. */
    private boolean lanceAvailable(PhasePlaybook playbook, int src, int partner) {
        List<PhasePlaybook.Move> moves = playbook.moves();
        for (int i = 0; i < moves.size(); i++) {
            if (PhasePlaybook.countsAsLance(moves.get(i).kind())
                    && available(playbook, i, src)
                    && (partner < 0 || pairs(playbook, partner, i, src))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether move {@code i} can run now: a line with the boss's lines, a salvo with a living rack; R81a: from a fallback
     * part only a move off the source; M11 part H, step H1b: a move of its own source while that lives (or the last
     * source's), a wall spit or a wall pit's brood while the walls stand.
     */
    private boolean available(PhasePlaybook playbook, int i, int src) {
        PhasePlaybook.Move move = playbook.moves().get(i);
        if (fallen && !PhasePlaybook.offSource(move.kind())) {
            return false;
        }
        if (move.source() >= 0 && ownSource(playbook, i, src) < 0) {
            return false;
        }
        boolean pits = move.bays().isPresent() && move.bays().get().onWalls();
        return switch (move.kind()) {
            case LINE, SWEEP, TWIN -> piece.lines != null;
            case SALVO -> racksAlive(move) > 0;
            case BARRAGE -> piece.bursts != null && racksAlive(move) > 0;
            case VOLLEY, SPIRAL, LOBS, MOUNTS, SPORES -> true;
            case BROOD -> !pits || wallsStand();
            // M11 part F, step F1c: a stream on a part's bay only while that part lives.
            case STREAM -> !bayDead(i) && (!pits || wallsStand());
            case REAR_SWEEP -> piece.rearSweep != null && !piece.rearSweep.flying();
            case WALL_SPIT -> wallsStand();
        };
    }

    /** M11 part H, step H1b: whether the boss's arena walls stand (their phase started). */
    private boolean wallsStand() {
        return piece.walls != null && piece.walls.standing();
    }

    /**
     * M11 part H, step H1b: the part move {@code i} fires from when it has a source of its own: that part while it
     * lives, else (one of the last source's moves) the last living source; -1 for none (or a move of the playbook's
     * source).
     */
    private int ownSource(PhasePlaybook playbook, int i, int src) {
        int own = playbook.moves().get(i).source();
        if (own < 0) {
            return -1;
        }
        if (piece.partHp(own) > 0) {
            return own;
        }
        int lastAlive = lastSinger();
        return lastAlive >= 0 && playbook.ensemble().lastMove(i) ? lastAlive : -1;
    }

    /** M11 part H, step H1b: the one living part among the running playbook's own sources; -1 for none or several. */
    private int lastSinger() {
        int alive = -1;
        int[] parts = singers[book];
        for (int k = 0; k < parts.length; k++) {
            if (piece.partHp(parts[k]) > 0) {
                if (alive >= 0) {
                    return -1;
                }
                alive = parts[k];
            }
        }
        return alive;
    }

    /**
     * M11 part H, step H1b: whether move {@code i} may run beside lane 0's {@code partner}: not the same move, not the
     * same own source, never two told lines and at most one releasing when the pair's rules say so.
     */
    private boolean pairs(PhasePlaybook playbook, int partner, int i, int src) {
        if (i == partner) {
            return false;
        }
        int a = ownSource(playbook, partner, src);
        if (a >= 0 && a == ownSource(playbook, i, src)) {
            return false;
        }
        PhasePlaybook.Parallel rules = playbook.ensemble().parallel().orElseThrow();
        PhasePlaybook.Kind first = playbook.moves().get(partner).kind();
        PhasePlaybook.Kind kind = playbook.moves().get(i).kind();
        if (rules.oneLine() && PhasePlaybook.countsAsLance(first) && PhasePlaybook.countsAsLance(kind)) {
            return false;
        }
        return !(rules.oneRelease() && PhasePlaybook.releases(first) && PhasePlaybook.releases(kind));
    }

    /** M11 part H, step H1b: whether half (or more) of the playbook's sources are dead (phase 1's half). */
    private boolean halfDead(PhasePlaybook playbook) {
        int dead = 0;
        for (int i = 0; i < playbook.sources().size(); i++) {
            dead += piece.partHp(playbook.sources().get(i)) <= 0 ? 1 : 0;
        }
        return 2 * dead >= playbook.sources().size();
    }

    /**
     * M11 part H, step H1b ({@link PhasePlaybook.Ensemble#coverEvery}): the due draw of the earliest due living own
     * source when one must be drawn now so that every living one is drawn at least once in any {@code coverEvery} draws
     * (earliest due first: some k of them due within the next k draws); {@link #NONE_DUE} when none must.
     */
    private int due(PhasePlaybook playbook) {
        int every = playbook.ensemble().coverEvery();
        if (every <= 0) {
            return NONE_DUE;
        }
        int[] parts = singers[book];
        int n = 0;
        for (int k = 0; k < parts.length; k++) {
            if (piece.partHp(parts[k]) > 0) {
                int d = sourceDrawnAt[parts[k]] + every;
                int j = n++;
                while (j > 0 && deadlines[j - 1] > d) {
                    deadlines[j] = deadlines[j - 1];
                    j--;
                }
                deadlines[j] = d;
            }
        }
        if (n < 2) {
            return NONE_DUE;
        }
        int next = draws + 1;
        for (int k = 0; k < n; k++) {
            if (deadlines[k] <= next + k) {
                return deadlines[0];
            }
        }
        return NONE_DUE;
    }

    /** M11 part H, step H1b: the due draw of move {@code i}'s own source; {@link #NONE_DUE} for none. */
    private int deadline(PhasePlaybook playbook, int i, int src) {
        int own = ownSource(playbook, i, src);
        return own < 0 ? NONE_DUE : sourceDrawnAt[own] + playbook.ensemble().coverEvery();
    }

    private int racksAlive(PhasePlaybook.Move move) {
        int alive = 0;
        for (int i = 0; i < move.racks().size(); i++) {
            alive += piece.partHp(move.racks().get(i)) > 0 ? 1 : 0;
        }
        return alive;
    }

    /** Lane {@code l}'s running move's step {@link #modeTicks} (0 first) from {@code src}; returns whether it is over. */
    private boolean run(int l, PhasePlaybook.Move now, int src, BossActions actions, boolean firing) {
        int run = modeTicks[l];
        int gap = Math.max(1, SimStep.ticks(now.gapSeconds()));
        switch (now.kind()) {
            case LINE, SWEEP, TWIN -> {
                return line(l, now, src, run, actions, firing);
            }
            case BARRAGE -> {
                // R80a: the living batteries in turn, each its own told burst (R81a: the first cued in the tell).
                int n = now.racks().size();
                int tellTicks = SimStep.ticks(books.get(book).tellSeconds());
                barrageCue(l, now, tellTicks + run, tellTicks, actions, firing);
                if (shots[l] < n) {
                    return false;
                }
                boolean busy = false;
                for (int i = 0; i < n; i++) {
                    int rack = now.racks().get(i);
                    busy |= piece.bursts.telling(rack) || piece.bursts.bursting(rack);
                }
                return !busy || run > n * gap + SimStep.ticks(BARRAGE_TAIL_SECONDS);
            }
            case VOLLEY -> {
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    if (firing && now.sweepRadians() > 0) {
                        sweptVolley(l, now, src, shots[l] - 1, actions);
                    } else if (firing) {
                        actions.aimed(
                                piece.partX(src),
                                piece.partY(src),
                                attacks.get(now.attack()).gun());
                    }
                }
                return shots[l] >= now.count();
            }
            case SALVO -> {
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    EnemyGun gun = attacks.get(now.attack()).gun();
                    for (int i = 0; i < now.racks().size() && firing; i++) {
                        int rack = now.racks().get(i);
                        if (piece.partHp(rack) > 0) {
                            actions.stationMissile(piece.partX(rack), piece.partY(rack), gun);
                        }
                    }
                }
                return shots[l] >= now.count();
            }
            case STREAM -> {
                PhasePlaybook.Bays bays = now.bays().orElseThrow();
                EnemySpec.Launcher launcher = bays.launcher();
                int n = launcher.stream();
                if (bayDead(move[l])) {
                    // M11 part F, step F1c: its bay died: the stream stops (the move ends, its cover fires).
                    return true;
                }
                int every = Math.max(1, SimStep.ticks(launcher.gapSeconds()));
                if (run == shots[l] * every && shots[l] < n) {
                    double spread = launcher.spreadRadians();
                    double fan = n == 1 ? 0 : -spread / 2 + shots[l] * spread / (n - 1);
                    shots[l]++;
                    for (int k = 0; k < bays.places().size() && firing; k++) {
                        BossSpec.Offset place = bays.places().get(k);
                        if (bays.onWalls() || bays.toShip()) {
                            // M11 part H, step H1b: a wall pit's or an aimed bay's launch.
                            launch(bays, launcher, place, piece.x(), piece.y(), fan, actions);
                        } else {
                            actions.bayLaunch(
                                    launcher, piece.x() + place.dx(), piece.y() + place.dy(), -StrictMath.PI / 2 + fan);
                        }
                    }
                }
                return shots[l] >= n;
            }
            case BROOD -> {
                PhasePlaybook.Bays bays = now.bays().orElseThrow();
                // M11 part G, step G1: the launchers take turns a brood each.
                EnemySpec.Launcher launcher = bays.launcher(broods);
                int n = launcher.stream();
                int every = Math.max(1, SimStep.ticks(launcher.gapSeconds()));
                int from = brooder(now, src);
                if (run == shots[l] * every && shots[l] < n) {
                    double spread = launcher.spreadRadians();
                    double fan = n == 1 ? 0 : -spread / 2 + shots[l] * spread / (n - 1);
                    shots[l]++;
                    if (shots[l] == 1 && bays.fromOpen()) {
                        // M11 part G, step G1: the racks open as it begins (the open sac pair) split to its end.
                        broodFrom[l] = 0;
                        for (int r = 0; r < now.racks().size(); r++) {
                            broodFrom[l] |= piece.partOpen(now.racks().get(r)) ? 1 << r : 0;
                        }
                    }
                    boolean open = bays.fromOpen() && broodFrom[l] != 0;
                    for (int r = 0; r < now.racks().size() && open && firing; r++) {
                        int at = now.racks().get(r);
                        if ((broodFrom[l] & 1 << r) != 0 && piece.partHp(at) > 0) {
                            brood(bays, launcher, at, fan, actions);
                        }
                    }
                    if (!open && firing) {
                        brood(bays, launcher, from, fan, actions);
                    }
                }
                return shots[l] >= n;
            }
            case SPIRAL -> {
                return spiral(l, attacks.get(now.attack()), src, run, actions, firing);
            }
            case REAR_SWEEP -> {
                // M11 part F, step F1: the sweep runs on the boss's steps (RearSweep#step); over once it is back.
                if (run == 0) {
                    piece.rearSweep.start(actions);
                }
                return !piece.rearSweep.flying();
            }
            case LOBS -> {
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    if (firing) {
                        EnemyGun gun = attacks.get(now.attack()).gun();
                        double tx = lobX(actions.draw());
                        double ty = lobY(actions.draw());
                        actions.lobAt(piece.partX(src), piece.partY(src), gun, tx, ty);
                    }
                }
                return shots[l] >= now.count();
            }
            case MOUNTS -> {
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    EnemyGun gun = attacks.get(now.attack()).gun();
                    for (int i = 0; i < now.racks().size() && firing; i++) {
                        int mount = now.racks().get(i);
                        actions.aimed(piece.partX(mount), piece.partY(mount), gun);
                    }
                }
                return shots[l] >= now.count();
            }
            case SPORES -> {
                // M11 part H, step H1b: the spores thrown to points clear of the ship's x, near its height.
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    if (firing) {
                        EnemyGun gun = attacks.get(now.attack()).gun();
                        double tx = clearOf(actions.draw(), LOB_EDGE, PlayField.WIDTH - LOB_EDGE, shipX, now.clearPx());
                        double ty = lobY(actions.draw());
                        double life = gun.mine().orElseThrow().lifeSeconds();
                        actions.coreMine(
                                gun,
                                piece.partX(src),
                                piece.partY(src),
                                tx,
                                ty,
                                PhasePlaybook.SPORE_THROW_SECONDS,
                                PhasePlaybook.SPORE_THROW_SECONDS + life);
                    }
                }
                return shots[l] >= now.count();
            }
            case WALL_SPIT -> {
                // M11 part H, step H1b: a vein point on each wall's face, clear of the ship's height.
                if (run == shots[l] * gap && shots[l] < now.count()) {
                    shots[l]++;
                    if (firing && wallsStand()) {
                        EnemyGun gun = attacks.get(now.attack()).gun();
                        double top = PlayField.HEIGHT - SPIT_TOP_MARGIN;
                        double ly = clearOf(actions.draw(), LOB_EDGE, top, shipY, now.clearPx());
                        double ry = clearOf(actions.draw(), LOB_EDGE, top, shipY, now.clearPx());
                        actions.aimed(piece.walls.leftAt(ly) + SPIT_INSET, ly, gun);
                        actions.aimed(piece.walls.rightAt(ry) - SPIT_INSET, ry, gun);
                    }
                }
                return shots[l] >= now.count();
            }
        }
        return true;
    }

    /** M11 part H, step H1b: a wall spit's vein point lies at most this far under the top edge's, px (below the Heart). */
    static final double SPIT_TOP_MARGIN = 200;
    /** M11 part H, step H1b: a wall spit's bullets start this far in from the wall's face (off the rock), px. */
    static final double SPIT_INSET = 8;

    /** A brood's units of {@code launcher} out of its bays' places round part {@code at}. */
    private void brood(PhasePlaybook.Bays bays, EnemySpec.Launcher launcher, int at, double fan, BossActions actions) {
        for (int k = 0; k < bays.places().size(); k++) {
            BossSpec.Offset place = bays.places().get(k);
            if (bays.onWalls() || bays.toShip()) {
                // M11 part H, step H1b: a wall pit's or an aimed bay's launch.
                launch(bays, launcher, place, piece.partX(at), piece.partY(at), fan, actions);
            } else {
                actions.bayLaunch(
                        launcher, piece.partX(at) + place.dx(), piece.partY(at) + place.dy(), -StrictMath.PI / 2 + fan);
            }
        }
    }

    /**
     * M11 part H, step H1b: one unit of {@code launcher} out of {@code place}: a wall pit's on its wall's face (its
     * {@code dx} sign the wall, its size in from the face, its {@code dy} the height), else round (cx, cy); its fan's
     * {@code fan} off the direction to the ship ({@link PhasePlaybook.Bays#toShip}) or off straight down.
     */
    private void launch(
            PhasePlaybook.Bays bays,
            EnemySpec.Launcher launcher,
            BossSpec.Offset place,
            double cx,
            double cy,
            double fan,
            BossActions actions) {
        double x;
        double y;
        if (bays.onWalls()) {
            y = place.dy();
            x = place.dx() < 0 ? piece.walls.leftAt(y) - place.dx() : piece.walls.rightAt(y) - place.dx();
        } else {
            x = cx + place.dx();
            y = cy + place.dy();
        }
        double centre = bays.toShip() ? StrictMath.atan2(shipY - y, shipX - x) : -StrictMath.PI / 2;
        actions.bayLaunch(launcher, x, y, centre + fan);
    }

    /**
     * M11 part G, step G1 (the Brood Carrier echo's mandible sweep): volley {@code i} (0 first) of a swept volley from
     * {@code src}: aimed at the ship turned about the source by {@code i} of its {@code count - 1} steps toward the
     * drawn side ({@link #side}: away from the ship's side), no lead.
     */
    private void sweptVolley(int l, PhasePlaybook.Move now, int src, int i, BossActions actions) {
        double sx = piece.partX(src);
        double sy = piece.partY(src);
        double turn = now.count() <= 1 ? 0 : side[l] * now.sweepRadians() * i / (now.count() - 1);
        double dx = shipX - sx;
        double dy = shipY - sy;
        double cos = Trig.cos(turn);
        double sin = Trig.sin(turn);
        actions.aimedAt(sx, sy, attacks.get(now.attack()).gun(), sx + dx * cos - dy * sin, sy + dx * sin + dy * cos);
    }

    /** The lowest a bile lob's mark lies under the play field's top edge, px (the bow's tip is at 250). */
    static final double LOB_TOP_MARGIN = 260;
    /** A bile lob's mark lies within this of the ship's height, px. */
    static final double LOB_REACH_Y = 80;
    /** A bile lob's mark keeps this far inside the field's edges, px. */
    static final double LOB_EDGE = 24;

    /**
     * M10 part H, step H1b: a bile lob's mark's x for a draw {@code r} in [0, 1): over the field inside its edges,
     * never within {@link PhasePlaybook#LOB_CLEAR_X} of the ship's x (the field's two sides of it, by their widths).
     */
    double lobX(double r) {
        return clearOf(r, LOB_EDGE, PlayField.WIDTH - LOB_EDGE, shipX, PhasePlaybook.LOB_CLEAR_X);
    }

    /**
     * A place in [lo, hi] for a draw {@code r} in [0, 1), never within {@code clear} of {@code at} (the two sides of it,
     * by their sizes); with no room on either side, the end farther from it (M11 part H, step H1b: a spore's x, a wall
     * spit's height).
     */
    static double clearOf(double r, double lo, double hi, double at, double clear) {
        double gapLo = Math.clamp(at - clear, lo, hi);
        double gapHi = Math.clamp(at + clear, lo, hi);
        double left = gapLo - lo;
        double right = hi - gapHi;
        double pick = r * (left + right);
        if (!(left + right > 0)) {
            return at < (lo + hi) / 2 ? hi : lo;
        }
        return pick < left ? lo + pick : gapHi + (pick - left);
    }

    /** M10 part H, step H1b: a bile lob's mark's height for a draw {@code r}: near the ship's, under the bow. */
    double lobY(double r) {
        double y = shipY + (2 * r - 1) * LOB_REACH_Y;
        return Math.clamp(y, LOB_EDGE, PlayField.HEIGHT - LOB_TOP_MARGIN);
    }

    /** M10 part H, step H1b: lane {@code l}'s spiral's step {@code run} from {@code src}; returns whether it is over. */
    private boolean spiral(int l, BossSpec.Attack attack, int src, int run, BossActions actions, boolean firing) {
        spiralAngle[l] =
                Math.IEEEremainder(spiralAngle[l] + attack.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        if (--spiralTicks[l] <= 0) {
            spiralTicks[l] = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
            for (int k = 0; k < attack.arms() && firing; k++) {
                actions.bullet(
                        piece.partX(src),
                        piece.partY(src),
                        spiralAngle[l] + 2 * StrictMath.PI * k / attack.arms(),
                        attack.gun().bulletSpeed(),
                        attack.gun().damage());
            }
        }
        return run + 1 >= SimStep.ticks(attack.durationSeconds());
    }

    /**
     * Lane {@code l}'s told line: requested at once from the source; over once it has gone live and run out, or lapsed
     * (its part died, its telegraph cancelled).
     */
    private boolean line(int l, PhasePlaybook.Move now, int src, int run, BossActions actions, boolean firing) {
        BossLines lines = piece.lines;
        if (run == 0) {
            if (!firing) {
                return true;
            }
            if (now.cover() >= 0) {
                // R81a: a told line's cover also as its line is drawn (it arrives about as the beam fires).
                actions.aimed(
                        piece.partX(src),
                        piece.partY(src),
                        attacks.get(now.cover()).gun());
            }
            switch (now.kind()) {
                case SWEEP -> lines.requestSweep(now.attack(), src, side[l] * now.sweepRadians());
                case TWIN ->
                    // R80a: the flank on the ship's side first, the other re-aimed at the ship gapSeconds later.
                    lines.requestTwin(
                            now.attack(),
                            src,
                            (shipX >= piece.partX(src) ? 1 : -1) * now.flankPx(),
                            SimStep.ticks(now.gapSeconds()));
                default -> lines.request(now.attack(), src);
            }
            pending[l] = true;
            return false;
        }
        if (pending[l] && lines.part() == src && lines.live()) {
            pending[l] = false;
        }
        if (pending[l]) {
            return !lines.due(src) && lines.part() != src && !lines.twinBusy();
        }
        if (now.kind() == PhasePlaybook.Kind.TWIN && shots[l] == 0) {
            // R80a: the twin's gold fan from between the flanks as the first lance fires.
            shots[l] = 1;
            if (firing) {
                actions.aimed(
                        piece.partX(src),
                        piece.partY(src),
                        attacks.get(now.with()).gun());
            }
        }
        return lines.part() != src && !lines.twinBusy();
    }

    /**
     * R81a: the barrage's cues, {@code tau} steps after its tell started: the first a battery's tell before the
     * barrage's tell ends (so its burst comes as the tell ends), the next every gap after it, each the next living
     * battery side to side.
     */
    private void barrageCue(
            int l, PhasePlaybook.Move now, int tau, int tellTicks, BossActions actions, boolean firing) {
        int gap = Math.max(1, SimStep.ticks(now.gapSeconds()));
        int start = Math.max(0, tellTicks - piece.bursts.tellTicks());
        if (tau < start || (tau - start) % gap != 0) {
            return;
        }
        while (shots[l] < now.racks().size()) {
            int rack = now.racks().get(shots[l]++);
            if (firing && piece.bursts.cue(rack, actions)) {
                break;
            }
        }
    }

    /** R80a: the barrage's wait for its last burst at most this long after its last cue, s. */
    static final double BARRAGE_TAIL_SECONDS = 2;

    /** R80a: the side the drawn move's sweep turns to (+1 right, -1 left; its tell shows it); 0 before a draw. */
    public int side() {
        return side[0];
    }

    /** R80a: the move told or running; null while it rests. */
    public PhasePlaybook.Move moveSpec() {
        return moveSpec(0);
    }

    /** M11 part H, step H1b: lane {@code l}'s move told, running or waiting for its tell; null while it rests. */
    public PhasePlaybook.Move moveSpec(int l) {
        return move[l] < 0 ? null : books.get(book).moves().get(move[l]);
    }

    /** What it does now. */
    public Mode mode() {
        return mode(0);
    }

    /** M11 part H, step H1b: what lane {@code l} does now (a pair's second waiting for its tell: {@link Mode#REST}). */
    public Mode mode(int l) {
        return mode[l] == WAIT ? Mode.REST : MODES[mode[l]];
    }

    /** M11 part H, step H1b: whether lane {@code l}'s drawn move waits for its tell (a pair's second). */
    public boolean waiting(int l) {
        return mode[l] == WAIT;
    }

    /** The move told or running (its index in its playbook's moves); -1 while it rests. */
    public int move() {
        return move[0];
    }

    /** M11 part H, step H1b: lane {@code l}'s move (its index); -1 while it rests. */
    public int move(int l) {
        return move[l];
    }

    /** The kind of the move told or running; null while it rests. */
    public PhasePlaybook.Kind kind() {
        return kind(0);
    }

    /** M11 part H, step H1b: the kind of lane {@code l}'s move; null while it rests. */
    public PhasePlaybook.Kind kind(int l) {
        return move[l] < 0 ? null : books.get(book).moves().get(move[l]).kind();
    }

    /** How far the tell has run, 0–1 (the glows brightening); 0 when it does not tell. */
    public double tellShare() {
        return tellShare(0);
    }

    /** M11 part H, step H1b: how far lane {@code l}'s tell has run, 0–1; 0 when it does not tell. */
    public double tellShare(int l) {
        if (mode[l] != Mode.TELL.ordinal()) {
            return 0;
        }
        return Math.min(
                1,
                (double) modeTicks[l]
                        / Math.max(1, SimStep.ticks(books.get(book).tellSeconds())));
    }

    /** The firing source now (the first living source of the phase's playbook); -1 without one. */
    public int source() {
        return book < 0 ? -1 : source(books.get(book));
    }

    /**
     * M11 part H, step H1b: the part lane {@code l}'s move fires from (its own source, else the playbook's source); -1
     * while it rests.
     */
    public int source(int l) {
        if (move[l] < 0) {
            return -1;
        }
        return laneSource[l] >= 0 ? laneSource[l] : source();
    }

    /** The draws so far in this phase's playbook. */
    public int draws() {
        return draws;
    }

    void addTo(StateHash hash) {
        hash.add(book)
                .add(mode[0])
                .add(modeTicks[0])
                .add(restTicks)
                .add(move[0])
                .add(last)
                .add(draws)
                .add(shots[0])
                .add(pending[0] ? 1 : 0)
                .add(spiralAngle[0])
                .add(spiralTicks[0])
                .add(sinceLance)
                .add(side[0]);
        for (int at : drawnAt) {
            hash.add(at);
        }
        if (alternates) {
            // M11 part G, step G1: only a boss whose bays take turns (the Brood Carrier echo's).
            hash.add(broods).add(broodFrom[0]);
        }
        if (ensembles) {
            // M11 part H, step H1b: only a boss with own sources, the ensemble's rules or part windows (Level 50's).
            hash.add(mode[1])
                    .add(modeTicks[1])
                    .add(move[1])
                    .add(shots[1])
                    .add(pending[1] ? 1 : 0)
                    .add(spiralAngle[1])
                    .add(spiralTicks[1])
                    .add(side[1])
                    .add(broodFrom[1])
                    .add(lastOther)
                    .add(broods);
            for (int l = 0; l < LANES; l++) {
                hash.add(laneSource[l]).add(laneSinger[l]).add(waitTicks[l]);
            }
            for (int at : sourceDrawnAt) {
                hash.add(at);
            }
        }
    }
}
