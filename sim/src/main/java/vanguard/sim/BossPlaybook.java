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
    /** M10 part H, step H1b: the running spiral's arm angle, radians, and its steps to the next bullets. */
    private double spiralAngle;

    private int spiralTicks;
    /** M10 round 80 fixes, R80a: the draws since the last told line ({@link PhasePlaybook#lanceEvery}). */
    private int sinceLance;
    /** R80a: the drawn sweep's side, +1 toward the right (the ship was on the left at the draw), -1 the left. */
    private int side;
    /**
     * M11 part G, step G1: the broods run so far in this playbook (the launchers' turns, {@link
     * PhasePlaybook.Bays#launcher(int)}); hashed only for a boss whose bays take turns.
     */
    private int broods;
    /**
     * M11 part G, step G1: the running brood's racks (bits by their place in its racks) that were open as it began: it
     * splits from them to its end (its window may shut meanwhile); 0: from its one part.
     */
    private int broodFrom;
    /** M11 part G, step G1: whether a playbook's bays take turns or split from the open racks. */
    private final boolean alternates;

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
        for (PhasePlaybook b : books) {
            for (PhasePlaybook.Move m : b.moves()) {
                turns |= m.bays().isPresent()
                        && (!m.bays().get().turns().isEmpty() || m.bays().get().fromOpen());
            }
        }
        alternates = turns;
        coversTells = new boolean[books.size()];
        bayParts = new int[books.size()][];
        for (int b = 0; b < books.size(); b++) {
            List<PhasePlaybook.Move> moves = books.get(b).moves();
            bayParts[b] = new int[moves.size()];
            for (int m = 0; m < moves.size(); m++) {
                coversTells[b] |= moves.get(m).kind() == PhasePlaybook.Kind.REAR_SWEEP;
                bayParts[b][m] = moves.get(m).kind() == PhasePlaybook.Kind.STREAM
                        ? bayPart(piece.spec(), moves.get(m).bays().orElseThrow())
                        : -1;
            }
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
        if (bays.places().size() != 1) {
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
        mode = Mode.REST.ordinal();
        modeTicks = 0;
        restTicks = b < 0 ? 0 : SimStep.ticks(books.get(b).restSeconds());
        move = -1;
        last = -1;
        draws = 0;
        java.util.Arrays.fill(drawnAt, Playbook.NEVER);
        shots = 0;
        pending = false;
        spiralAngle = 0;
        spiralTicks = 0;
        sinceLance = 0;
        side = 0;
        broods = 0;
        broodFrom = 0;
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
            mode = Mode.REST.ordinal();
            move = -1;
            return;
        }
        if (mode == Mode.REST.ordinal()) {
            if (piece.rearSweep != null && piece.rearSweep.flying()) {
                // M11 part F, step F1: a rear sweep outliving its move (its phase ended mid-climb) ends before a draw.
                return;
            }
            if (restTicks > 0) {
                restTicks--;
                return;
            }
            draw(playbook, src, actions, firing);
            return;
        }
        if (mode == Mode.TELL.ordinal()) {
            int tellTicks = SimStep.ticks(playbook.tellSeconds());
            ++modeTicks;
            PhasePlaybook.Move told = playbook.moves().get(move);
            if (told.kind() == PhasePlaybook.Kind.BARRAGE && modeTicks < tellTicks) {
                // R81a: the barrage's first battery tells under the barrage's own tell, so it fires as the tell ends.
                barrageCue(told, modeTicks, tellTicks, actions, firing);
            }
            if (modeTicks < tellTicks) {
                return;
            }
            mode = Mode.RUN.ordinal();
            modeTicks = 0;
            int at = at(playbook.moves().get(move), src);
            actions.ring(SimEvents.Type.BOSS_PLAY, piece.partX(at), piece.partY(at), move);
        }
        PhasePlaybook.Move now = playbook.moves().get(move);
        boolean over = run(now, src, actions, firing);
        modeTicks++;
        if (over && firing && now.cover() >= 0) {
            // R81a: every covered move ends with its cover from the source (the bow's ports), closing on the ship
            // through the rest and the next tell (a told line's beam dying, a volley's last fan, a stream's last Dart;
            // from a fallback part, its own gun).
            actions.aimed(
                    piece.partX(src), piece.partY(src), attacks.get(now.cover()).gun());
        }
        if (over) {
            // M11 part G, step G1: windows that open on a move's end open now (the Brood Carrier echo's sac pairs).
            piece.windows.moveEnded();
            if (now.kind() == PhasePlaybook.Kind.BROOD) {
                broods++;
            }
            mode = Mode.REST.ordinal();
            modeTicks = 0;
            move = -1;
            shots = 0;
            pending = false;
            spiralAngle = 0;
            spiralTicks = 0;
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

    /** The next move, by weight among those allowed now; its tell starts (none allowed: it waits a step). */
    private void draw(PhasePlaybook playbook, int src, BossActions actions, boolean firing) {
        List<PhasePlaybook.Move> moves = playbook.moves();
        // The rules in full; then without the spacing; then the repeat too (a pool that would stall).
        // R80a: a told line at least every lanceEvery-th draw (given way with the repeat).
        // M11 part F, step F1: the rear sweep counts as a told line (phase 3's "a lance or the rear sweep").
        boolean needLance =
                playbook.lanceEvery() > 0 && sinceLance >= playbook.lanceEvery() - 1 && lanceAvailable(moves);
        for (int pass = 0; pass < 3; pass++) {
            drawn.clear();
            for (int i = 0; i < moves.size(); i++) {
                PhasePlaybook.Move candidate = moves.get(i);
                boolean allowed = available(candidate)
                        && (pass >= 2 || i != last)
                        && (pass >= 2 || !needLance || PhasePlaybook.countsAsLance(candidate.kind()))
                        && (pass >= 2 || draws > 0 || !candidate.notFirst())
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
        sinceLance = PhasePlaybook.countsAsLance(moves.get(picked).kind()) ? 0 : sinceLance + 1;
        // R80a: a sweep turns toward the side of the source the ship is not on now (its tell shows it).
        side = shipX < piece.partX(src) ? 1 : -1;
        drawnAt[picked] = draws;
        last = picked;
        move = picked;
        mode = Mode.TELL.ordinal();
        modeTicks = 0;
        shots = 0;
        pending = false;
        int at = at(moves.get(picked), src);
        actions.ring(SimEvents.Type.BOSS_PLAY_TELL, piece.partX(at), piece.partY(at), picked);
        if (moves.get(picked).kind() == PhasePlaybook.Kind.REAR_SWEEP) {
            // M11 part F, step F1: the far flank flares, Rook calls it.
            piece.rearSweep.tell(shipX, actions);
        }
        if (coversTells[book] && firing && moves.get(picked).cover() >= 0) {
            // M11 part F, step F1c: a playbook with a rear sweep also fires its told move's cover as the tell starts,
            // so a pair is still closing through the tell after the rest (the spec's no-quiet rule with the sacs dead:
            // phase 3's capsule alone).
            actions.aimed(
                    piece.partX(src),
                    piece.partY(src),
                    attacks.get(moves.get(picked).cover()).gun());
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

    /** R81a: whether a told line can be drawn now (a fallen playbook draws none). */
    private boolean lanceAvailable(List<PhasePlaybook.Move> moves) {
        for (int i = 0; i < moves.size(); i++) {
            if (PhasePlaybook.countsAsLance(moves.get(i).kind()) && available(moves.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code move} can run now: a line with the boss's lines, a salvo with a living rack; R81a: from a fallback
     * part only a move off the source.
     */
    private boolean available(PhasePlaybook.Move move) {
        if (fallen && !PhasePlaybook.offSource(move.kind())) {
            return false;
        }
        return switch (move.kind()) {
            case LINE, SWEEP, TWIN -> piece.lines != null;
            case SALVO -> racksAlive(move) > 0;
            case BARRAGE -> piece.bursts != null && racksAlive(move) > 0;
            case VOLLEY, BROOD, SPIRAL, LOBS, MOUNTS -> true;
            // M11 part F, step F1c: a stream on a part's bay only while that part lives.
            case STREAM -> !bayDead(moveIndex(move));
            case REAR_SWEEP -> piece.rearSweep != null && !piece.rearSweep.flying();
        };
    }

    /** The index of {@code move} in the running playbook (identity). */
    private int moveIndex(PhasePlaybook.Move move) {
        List<PhasePlaybook.Move> moves = books.get(book).moves();
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i) == move) {
                return i;
            }
        }
        return -1;
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
            case LINE, SWEEP, TWIN -> {
                return line(now, src, run, actions, firing);
            }
            case BARRAGE -> {
                // R80a: the living batteries in turn, each its own told burst (R81a: the first cued in the tell).
                int n = now.racks().size();
                int tellTicks = SimStep.ticks(books.get(book).tellSeconds());
                barrageCue(now, tellTicks + run, tellTicks, actions, firing);
                if (shots < n) {
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
                if (run == shots * gap && shots < now.count()) {
                    shots++;
                    if (firing && now.sweepRadians() > 0) {
                        sweptVolley(now, src, shots - 1, actions);
                    } else if (firing) {
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
                if (bayDead(move)) {
                    // M11 part F, step F1c: its bay died: the stream stops (the move ends, its cover fires).
                    return true;
                }
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
            case BROOD -> {
                PhasePlaybook.Bays bays = now.bays().orElseThrow();
                // M11 part G, step G1: the launchers take turns a brood each.
                EnemySpec.Launcher launcher = bays.launcher(broods);
                int n = launcher.stream();
                int every = Math.max(1, SimStep.ticks(launcher.gapSeconds()));
                int from = brooder(now, src);
                if (run == shots * every && shots < n) {
                    double spread = launcher.spreadRadians();
                    double angle = -StrictMath.PI / 2 + (n == 1 ? 0 : -spread / 2 + shots * spread / (n - 1));
                    shots++;
                    if (shots == 1 && bays.fromOpen()) {
                        // M11 part G, step G1: the racks open as it begins (the open sac pair) split to its end.
                        broodFrom = 0;
                        for (int r = 0; r < now.racks().size(); r++) {
                            broodFrom |= piece.partOpen(now.racks().get(r)) ? 1 << r : 0;
                        }
                    }
                    boolean open = bays.fromOpen() && broodFrom != 0;
                    for (int r = 0; r < now.racks().size() && open && firing; r++) {
                        int at = now.racks().get(r);
                        if ((broodFrom & 1 << r) != 0 && piece.partHp(at) > 0) {
                            brood(bays, launcher, at, angle, actions);
                        }
                    }
                    if (!open && firing) {
                        brood(bays, launcher, from, angle, actions);
                    }
                }
                return shots >= n;
            }
            case SPIRAL -> {
                return spiral(attacks.get(now.attack()), src, run, actions, firing);
            }
            case REAR_SWEEP -> {
                // M11 part F, step F1: the sweep runs on the boss's steps (RearSweep#step); over once it is back.
                if (run == 0) {
                    piece.rearSweep.start(actions);
                }
                return !piece.rearSweep.flying();
            }
            case LOBS -> {
                if (run == shots * gap && shots < now.count()) {
                    shots++;
                    if (firing) {
                        EnemyGun gun = attacks.get(now.attack()).gun();
                        double tx = lobX(actions.draw());
                        double ty = lobY(actions.draw());
                        actions.lobAt(piece.partX(src), piece.partY(src), gun, tx, ty);
                    }
                }
                return shots >= now.count();
            }
            case MOUNTS -> {
                if (run == shots * gap && shots < now.count()) {
                    shots++;
                    EnemyGun gun = attacks.get(now.attack()).gun();
                    for (int i = 0; i < now.racks().size() && firing; i++) {
                        int mount = now.racks().get(i);
                        actions.aimed(piece.partX(mount), piece.partY(mount), gun);
                    }
                }
                return shots >= now.count();
            }
        }
        return true;
    }

    /** A brood's units of {@code launcher} out of its bays' places round part {@code at}. */
    private void brood(
            PhasePlaybook.Bays bays, EnemySpec.Launcher launcher, int at, double angle, BossActions actions) {
        for (int k = 0; k < bays.places().size(); k++) {
            BossSpec.Offset place = bays.places().get(k);
            actions.bayLaunch(launcher, piece.partX(at) + place.dx(), piece.partY(at) + place.dy(), angle);
        }
    }

    /**
     * M11 part G, step G1 (the Brood Carrier echo's mandible sweep): volley {@code i} (0 first) of a swept volley from
     * {@code src}: aimed at the ship turned about the source by {@code i} of its {@code count - 1} steps toward the
     * drawn side ({@link #side}: away from the ship's side), no lead.
     */
    private void sweptVolley(PhasePlaybook.Move now, int src, int i, BossActions actions) {
        double sx = piece.partX(src);
        double sy = piece.partY(src);
        double turn = now.count() <= 1 ? 0 : side * now.sweepRadians() * i / (now.count() - 1);
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
        double lo = LOB_EDGE;
        double hi = PlayField.WIDTH - LOB_EDGE;
        double gapLo = Math.clamp(shipX - PhasePlaybook.LOB_CLEAR_X, lo, hi);
        double gapHi = Math.clamp(shipX + PhasePlaybook.LOB_CLEAR_X, lo, hi);
        double left = gapLo - lo;
        double right = hi - gapHi;
        double pick = r * (left + right);
        if (!(left + right > 0)) {
            return shipX < PlayField.WIDTH / 2.0 ? hi : lo;
        }
        return pick < left ? lo + pick : gapHi + (pick - left);
    }

    /** M10 part H, step H1b: a bile lob's mark's height for a draw {@code r}: near the ship's, under the bow. */
    double lobY(double r) {
        double y = shipY + (2 * r - 1) * LOB_REACH_Y;
        return Math.clamp(y, LOB_EDGE, PlayField.HEIGHT - LOB_TOP_MARGIN);
    }

    /** M10 part H, step H1b: the spiral's step {@code run} from {@code src}; returns whether its duration is over. */
    private boolean spiral(BossSpec.Attack attack, int src, int run, BossActions actions, boolean firing) {
        spiralAngle =
                Math.IEEEremainder(spiralAngle + attack.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        if (--spiralTicks <= 0) {
            spiralTicks = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
            for (int k = 0; k < attack.arms() && firing; k++) {
                actions.bullet(
                        piece.partX(src),
                        piece.partY(src),
                        spiralAngle + 2 * StrictMath.PI * k / attack.arms(),
                        attack.gun().bulletSpeed(),
                        attack.gun().damage());
            }
        }
        return run + 1 >= SimStep.ticks(attack.durationSeconds());
    }

    /**
     * The told line: requested at once from the source; over once it has gone live and run out, or lapsed (its part
     * died, its telegraph cancelled).
     */
    private boolean line(PhasePlaybook.Move now, int src, int run, BossActions actions, boolean firing) {
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
                case SWEEP -> lines.requestSweep(now.attack(), src, side * now.sweepRadians());
                case TWIN ->
                    // R80a: the flank on the ship's side first, the other re-aimed at the ship gapSeconds later.
                    lines.requestTwin(
                            now.attack(),
                            src,
                            (shipX >= piece.partX(src) ? 1 : -1) * now.flankPx(),
                            SimStep.ticks(now.gapSeconds()));
                default -> lines.request(now.attack(), src);
            }
            pending = true;
            return false;
        }
        if (pending && lines.part() == src && lines.live()) {
            pending = false;
        }
        if (pending) {
            return !lines.due(src) && lines.part() != src && !lines.twinBusy();
        }
        if (now.kind() == PhasePlaybook.Kind.TWIN && shots == 0) {
            // R80a: the twin's gold fan from between the flanks as the first lance fires.
            shots = 1;
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
    private void barrageCue(PhasePlaybook.Move now, int tau, int tellTicks, BossActions actions, boolean firing) {
        int gap = Math.max(1, SimStep.ticks(now.gapSeconds()));
        int start = Math.max(0, tellTicks - piece.bursts.tellTicks());
        if (tau < start || (tau - start) % gap != 0) {
            return;
        }
        while (shots < now.racks().size()) {
            int rack = now.racks().get(shots++);
            if (firing && piece.bursts.cue(rack, actions)) {
                break;
            }
        }
    }

    /** R80a: the barrage's wait for its last burst at most this long after its last cue, s. */
    static final double BARRAGE_TAIL_SECONDS = 2;

    /** R80a: the side the drawn move's sweep turns to (+1 right, -1 left; its tell shows it); 0 before a draw. */
    public int side() {
        return side;
    }

    /** R80a: the move told or running; null while it rests. */
    public PhasePlaybook.Move moveSpec() {
        return move < 0 ? null : books.get(book).moves().get(move);
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
                .add(pending ? 1 : 0)
                .add(spiralAngle)
                .add(spiralTicks)
                .add(sinceLance)
                .add(side);
        for (int at : drawnAt) {
            hash.add(at);
        }
        if (alternates) {
            // M11 part G, step G1: only a boss whose bays take turns (the Brood Carrier echo's).
            hash.add(broods).add(broodFrom);
        }
    }
}
