package vanguard.sim;

/**
 * M10 part F, step F1a (design/enemies/bosses/honour-guard, round 72 Q5 a; m10f-gaps.md §1 rows 1, 2, 3 and 7): a
 * squadron mid-boss's aces, kept by its {@link SetPiece} ({@link SetPiece#squadron()}). The piece is parked off the
 * field and its parts are the aces, each flying its own position ({@link AceFlight}): a part's offset is its ace's
 * place less the parked centre. The one bar sums the aces (a segment each, in their join order); each pays its part's
 * bounty as it dies; the last death destroys the piece ({@link SetPiece#squadronFalls}), which ends the level clock's
 * halt.
 *
 * <p>The joins: the first ace's tell starts its join's time after the boss arrives (the sting), a later ace's once the
 * ace before it is told and that ace's time since it crossed its edge, its HP share or the first death says so ({@link
 * SquadronSpec.Join}). Each first entry is told {@link SquadronSpec#tellSeconds()} ahead at its edge ({@link
 * SimEvents.Type#ACE_TELL}: the gold chevron, the call sign, the engines' glint; no edge warning), then the ace crosses
 * ({@link SimEvents.Type#ACE_JOINED}) and flies in at its dash speed. Until it is told it is absent and untouchable,
 * and until its hull is wholly on the field it is untouchable (shots glance). The dancer enters by the side away from
 * the leader, the feinter from the bottom (the rear).
 *
 * <p>Each ace carries its own hex shield ({@link PartShields}); touching the ship it deals its contact damage once per
 * its interval, the ship is pushed away and the ace takes its own share and flies on ({@link #strike}). A dead ace's
 * hull tumbles away down and out of the field ({@link SimEvents.Type#ACE_DOWN}), never hitting or touching anything.
 *
 * <p>The playbook (step F1b: {@link AcePlaybook}, when the spec has one) draws each ace's moves through {@link #start}
 * and {@link #startPincer} once it {@link #ready holds on its station} and fires them; the radio's subjects of the joins
 * and deaths are raised here ({@link BossActions#squadronCue}). Its state is hashed with the piece's
 * (only a squadron hashes it), so a replay and a retry from the boss checkpoint repeat the fight exactly. Allocates
 * nothing once made.
 */
public final class Squadron implements Hashed {
    /** How an ace is now. */
    public enum State {
        /** Not told yet: absent (off the field) and untouchable. */
        WAITING,
        /** Its entry's tell at its edge: absent and untouchable. */
        TELLING,
        /** Flying in over its edge: untouchable until its hull is wholly on the field. */
        ENTERING,
        /** On the field: it can be hit, it holds its station and flies its moves. */
        FLYING,
        /** Dead: its hull tumbling away (no hit, no contact). */
        DOWN
    }

    private static final State[] STATES = State.values();
    private static final AceMove[] MOVES = AceMove.values();
    private static final WarningEdge[] EDGES = WarningEdge.values();

    private final SetPiece piece;
    private final SquadronSpec spec;
    private final int count;
    private final AceFlight flight;
    /** Per part, its ace (all parts are aces). */
    private final int[] aceOf;

    private final int tellTicks;
    private final int strikeEvery;
    /** Per ace: its {@link State} ordinal, the steps in it, its edge ({@link WarningEdge} ordinal), the boss's step it crossed at. */
    private final int[] state;

    private final int[] stateTicks;
    private final int[] edge;
    private final int[] joinedAt;
    /** Per ace: the steps until it can strike the ship again. */
    private final int[] strikeTicks;
    /** The steps since the boss arrived (its own clock: the level clock halts). */
    private int ticks;
    /** The aces dead so far. */
    private int deaths;
    /** Step F1b: its playbook and conductor; null without one (a caller starts the moves). */
    private final AcePlaybook playbook;
    /** {@link #playbook} for the presentation, made once. */
    private final java.util.Optional<AcePlaybook> playbookView;
    /**
     * Step F1b (m10f-gaps.md §1 row 9): per ace, the radio's {@code boss-phase} subjects: its join after the first
     * ({@code <call sign>-joins}, at its tell), its death ({@code <call sign>-down}), the first loss spoken by it
     * ({@code first-loss-<call sign>}), "just us now" by it ({@code last-ace-<call sign>}); lower case, made once.
     */
    private final String[] joinsSubject;

    private final String[] downSubject;
    private final String[] firstLossSubject;
    private final String[] lastAceSubject;

    Squadron(SetPiece piece, BossSpec boss, SetPieceSpec setPiece) {
        this.piece = piece;
        spec = boss.squadron().orElseThrow();
        count = spec.aces().size();
        flight = new AceFlight(spec);
        aceOf = new int[setPiece.parts().size()];
        java.util.Arrays.fill(aceOf, -1);
        for (int a = 0; a < count; a++) {
            aceOf[spec.aces().get(a).part()] = a;
        }
        tellTicks = SimStep.ticks(spec.tellSeconds());
        strikeEvery = Math.max(1, SimStep.ticks(spec.contact().intervalSeconds()));
        state = new int[count];
        stateTicks = new int[count];
        edge = new int[count];
        joinedAt = new int[count];
        strikeTicks = new int[count];
        joinsSubject = new String[count];
        downSubject = new String[count];
        firstLossSubject = new String[count];
        lastAceSubject = new String[count];
        for (int a = 0; a < count; a++) {
            String sign = spec.aces().get(a).callSign().toLowerCase(java.util.Locale.ROOT);
            joinsSubject[a] = sign + "-joins";
            downSubject[a] = sign + "-down";
            firstLossSubject[a] = "first-loss-" + sign;
            lastAceSubject[a] = "last-ace-" + sign;
        }
        playbook = spec.playbook().isPresent()
                ? new AcePlaybook(piece, this, boss, spec.playbook().get())
                : null;
        playbookView = java.util.Optional.ofNullable(playbook);
        reset();
    }

    /** Back to the level start (and the boss checkpoint): every ace waiting, whole. */
    void reset() {
        flight.reset();
        java.util.Arrays.fill(state, State.WAITING.ordinal());
        java.util.Arrays.fill(stateTicks, 0);
        java.util.Arrays.fill(edge, WarningEdge.TOP.ordinal());
        java.util.Arrays.fill(joinedAt, -1);
        java.util.Arrays.fill(strikeTicks, 0);
        ticks = 0;
        deaths = 0;
        if (playbook != null) {
            playbook.reset();
        }
    }

    /**
     * One step of the fight (from the boss's arrival, on the real steps): the deaths seen, the joins and their tells,
     * the aces' flight; step F1b: then the playbook's draws, tells and fire ({@code firing} false: none fires). Events
     * go through {@code actions}.
     */
    void step(double shipX, double shipY, BossActions actions, boolean firing) {
        flight.keepPrev();
        ticks++;
        for (int a = 0; a < count; a++) {
            stateTicks[a]++;
            if (strikeTicks[a] > 0) {
                strikeTicks[a]--;
            }
        }
        deaths(actions);
        for (int a = 0; a < count; a++) {
            join(a, shipX, actions);
        }
        double leaderX = leaderX();
        for (int a = 0; a < count; a++) {
            if (state[a] == State.FLYING.ordinal() && flight.move[a] == AceMove.HOLD.ordinal()) {
                // The dancer's next eight goes to the side away from the leader.
                flight.awayFrom(a, leaderX);
            }
        }
        for (int a = 0; playbook != null && a < count; a++) {
            // Step F6s: an ace about to fire from its station (or telling a dive) turns its nose onto the ship.
            flight.aim[a] = playbook.aims(a);
        }
        flight.step(shipX, shipY, actions);
        for (int a = 0; a < count; a++) {
            if (state[a] == State.ENTERING.ordinal() && flight.wholeOnField(a)) {
                set(a, State.FLYING);
            }
        }
        if (playbook != null) {
            playbook.step(shipX, shipY, actions, firing);
        }
    }

    /** Step F1b: as {@link #step(double, double, BossActions, boolean)}, firing. */
    void step(double shipX, double shipY, BossActions actions) {
        step(shipX, shipY, actions, true);
    }

    /** After the kill: the last death seen and the hulls tumbling on (the last ace's under its chain). */
    void dying(BossActions actions) {
        flight.keepPrev();
        deaths(actions);
        flight.fallOnly();
    }

    /** An ace whose part's HP ran out dies: its event, its tumble; the feinter's pull-out commits from the first. */
    private void deaths(BossActions actions) {
        for (int a = 0; a < count; a++) {
            int s = state[a];
            if ((s == State.ENTERING.ordinal() || s == State.FLYING.ordinal()) && !(piece.partHp(part(a)) > 0)) {
                set(a, State.DOWN);
                deaths++;
                flight.deaths = deaths;
                flight.tumble(a);
                actions.squadron(SimEvents.Type.ACE_DOWN, flight.x[a], flight.y[a], a);
                lines(a, actions);
            }
        }
    }

    /**
     * Step F1b: ace {@code a}'s death on the radio: its own subject; the first loss spoken by the senior flying
     * survivor; the last ace's "just us now" by it (an ace not yet across its edge speaks neither).
     */
    private void lines(int a, BossActions actions) {
        actions.squadronCue(downSubject[a]);
        int speaker = -1;
        int alive = 0;
        for (int b = 0; b < count; b++) {
            boolean flies = (state[b] == State.ENTERING.ordinal() || state[b] == State.FLYING.ordinal())
                    && piece.partHp(part(b)) > 0;
            if (flies && speaker < 0) {
                speaker = b;
            }
            alive += state[b] != State.DOWN.ordinal() ? 1 : 0;
        }
        if (speaker < 0) {
            return;
        }
        if (deaths == 1 && count > 2) {
            actions.squadronCue(firstLossSubject[speaker]);
        }
        if (deaths == count - 1 && alive == 1) {
            actions.squadronCue(lastAceSubject[speaker]);
        }
    }

    /** Ace {@code a}'s join: its tell when its time, share or the first death comes; its crossing after the tell. */
    private void join(int a, double shipX, BossActions actions) {
        if (state[a] == State.WAITING.ordinal() && due(a)) {
            WarningEdge e = edgeFor(a, shipX);
            edge[a] = e.ordinal();
            flight.prepare(a, e, leaderX());
            set(a, State.TELLING);
            actions.squadron(SimEvents.Type.ACE_TELL, tellX(a), tellY(a), tellValue(a, e));
            if (a > 0) {
                // Step F1b: its taunt (the first ace's is the boss's arrival line).
                actions.squadronCue(joinsSubject[a]);
            }
        }
        if (state[a] == State.TELLING.ordinal() && stateTicks[a] >= tellTicks) {
            set(a, State.ENTERING);
            joinedAt[a] = ticks;
            flight.enter(a);
            actions.squadron(SimEvents.Type.ACE_JOINED, flight.x[a], flight.y[a], a);
        }
    }

    /** Whether ace {@code a}'s tell is due now. */
    private boolean due(int a) {
        SquadronSpec.Join join = spec.aces().get(a).join();
        if (a == 0) {
            return ticks >= SimStep.ticks(join.afterSeconds());
        }
        int previous = a - 1;
        if (state[previous] == State.WAITING.ordinal()) {
            return false;
        }
        if (join.onFirstDeath() && deaths > 0) {
            return true;
        }
        if (joinedAt[previous] >= 0 && ticks - joinedAt[previous] >= SimStep.ticks(join.afterSeconds())) {
            return true;
        }
        return join.atShare() > 0 && hpShare(previous) <= join.atShare();
    }

    /** The edge ace {@code a} enters by: the top, the bottom, or the side away from the leader (else from the ship). */
    private WarningEdge edgeFor(int a, double shipX) {
        return switch (spec.aces().get(a).join().from()) {
            case TOP -> WarningEdge.TOP;
            case BOTTOM -> WarningEdge.BOTTOM;
            case SIDE -> {
                double leader = leaderX();
                double from = Double.isNaN(leader) ? shipX : leader;
                yield from <= PlayField.WIDTH / 2.0 ? WarningEdge.RIGHT : WarningEdge.LEFT;
            }
        };
    }

    /** The first flying leader's x (the first sniper on the field); NaN without one. */
    private double leaderX() {
        for (int a = 0; a < count; a++) {
            if (spec.aces().get(a).role() == SquadronSpec.Role.SNIPER
                    && (state[a] == State.ENTERING.ordinal() || state[a] == State.FLYING.ordinal())) {
                // Step R79b2: his station's (the dancer's eight goes to the side away from where he holds, not from
                // where an aim or a slide has him now).
                return state[a] == State.FLYING.ordinal() ? flight.centreX[a] : flight.x[a];
            }
        }
        return Double.NaN;
    }

    private void set(int a, State s) {
        state[a] = s.ordinal();
        stateTicks[a] = 0;
    }

    /** Ace {@code a}'s part's HP as a share of its full HP. */
    private double hpShare(int a) {
        int p = part(a);
        return Math.max(0, piece.partHp(p)) / piece.spec().parts().get(p).hp();
    }

    /**
     * Ace {@code a} touching the ship: whether it deals its contact now (alive and on the field, at most once per its
     * interval); the caller damages the ship, pushes it and the ace.
     */
    boolean strike(int a) {
        if (strikeTicks[a] > 0 || state[a] != State.FLYING.ordinal()) {
            return false;
        }
        strikeTicks[a] = strikeEvery;
        return true;
    }

    // F1b's hooks: the playbook's moves.

    /** Ace {@code a} starts {@code move} (its tell over) with the ship at (shipX, shipY); a move needs it on the field. */
    void start(int a, AceMove move, double shipX, double shipY, BossActions actions) {
        if (state[a] == State.FLYING.ordinal()) {
            flight.start(a, move, shipX, shipY, actions);
        }
    }

    /** Step F6s3: whether the pincer's lanes fit under the other aces' bands with the ship at height {@code sy}. */
    boolean pincerFits(int upper, int lower, double sy) {
        return flight.pincerFits(upper, lower, sy);
    }

    /** The pincer: {@code upper} and {@code lower} cross from opposite sides at once (both on the field). */
    void startPincer(int upper, int lower, double shipX, double shipY) {
        if (state[upper] == State.FLYING.ordinal() && state[lower] == State.FLYING.ordinal()) {
            flight.startPincer(upper, lower, shipX, shipY);
        }
    }

    /** Whether ace {@code a} is on the field holding its station (the playbook may draw its next move). */
    public boolean ready(int a) {
        return state[a] == State.FLYING.ordinal() && flight.move[a] == AceMove.HOLD.ordinal();
    }

    /**
     * Step F6s2: whether ace {@code a} holds its station and is not on its way over the ship to its other lane (the
     * feinter draws from its lane, so its dive has its run).
     */
    boolean steady(int a) {
        return ready(a) && !flight.crossing(a);
    }

    // The parts.

    /** The ace flying part {@code p}; -1 for none. */
    public int ace(int p) {
        return p < aceOf.length ? aceOf[p] : -1;
    }

    /** Ace {@code a}'s part. */
    public int part(int a) {
        return spec.aces().get(a).part();
    }

    /** Whether part {@code p}'s ace takes no damage now: not yet wholly on the field (absent, told, entering). */
    boolean untouchable(int p) {
        int a = ace(p);
        return a >= 0 && state[a] < State.FLYING.ordinal();
    }

    /** Whether part {@code p}'s ace is absent: not yet crossed its edge (nothing touches it, wherever it waits). */
    boolean absent(int p) {
        int a = ace(p);
        return a >= 0 && state[a] < State.ENTERING.ordinal();
    }

    /** Part {@code p}'s centre now, px from the left: its ace's. */
    double partX(int p) {
        return flight.x[ace(p)];
    }

    /** Part {@code p}'s centre now, px up. */
    double partY(int p) {
        return flight.y[ace(p)];
    }

    /** Where part {@code p}'s ace will be {@code ticks} steps on, moving as over its last step (a line's lead). */
    double leadX(int p, int ticks) {
        return flight.leadX(ace(p), ticks);
    }

    double leadY(int p, int ticks) {
        return flight.leadY(ace(p), ticks);
    }

    // The presentation's and the playbook's reads.

    public SquadronSpec spec() {
        return spec;
    }

    public int aceCount() {
        return count;
    }

    public String callSign(int a) {
        return spec.aces().get(a).callSign();
    }

    public State state(int a) {
        return STATES[state[a]];
    }

    /** Seconds ace {@code a} has been in its state (the tell's and the entry's clocks). */
    public double stateSeconds(int a) {
        return stateTicks[a] * SimStep.SECONDS;
    }

    /** How far ace {@code a}'s entry tell has run, 0–1 (0 before it, 1 after). */
    public double tellShare(int a) {
        if (state[a] == State.WAITING.ordinal()) {
            return 0;
        }
        return state[a] == State.TELLING.ordinal() ? Math.min(1, stateTicks[a] / (double) Math.max(1, tellTicks)) : 1;
    }

    /** The edge ace {@code a} enters (entered) by. */
    public WarningEdge edge(int a) {
        return EDGES[edge[a]];
    }

    /** Whether ace {@code a} has crossed its edge (entering, on the field or dead). */
    public boolean joined(int a) {
        return state[a] >= State.ENTERING.ordinal();
    }

    /** Whether ace {@code a} can be hit: alive and wholly on the field once. */
    public boolean touchable(int a) {
        return state[a] == State.FLYING.ordinal();
    }

    /** The aces alive and joined (entering or on the field): the playbook's company. */
    public int flyingCount() {
        int n = 0;
        for (int a = 0; a < count; a++) {
            n += state[a] == State.ENTERING.ordinal() || state[a] == State.FLYING.ordinal() ? 1 : 0;
        }
        return n;
    }

    /** The aces dead so far. */
    public int deaths() {
        return deaths;
    }

    /** Ace {@code a}'s centre now, px from the left (its wreck's while it tumbles). */
    public double x(int a) {
        return flight.x[a];
    }

    /** Ace {@code a}'s centre now, px up. */
    public double y(int a) {
        return flight.y[a];
    }

    /** Ace {@code a}'s centre between the last and this step, {@code alpha} in [0, 1]. */
    public double renderX(int a, double alpha) {
        return flight.prevX[a] + (flight.x[a] - flight.prevX[a]) * alpha;
    }

    public double renderY(int a, double alpha) {
        return flight.prevY[a] + (flight.y[a] - flight.prevY[a]) * alpha;
    }

    /** Where ace {@code a} faces, radians clockwise from straight down ({@link Enemy#facing()}'s); a wreck spins. */
    public double facing(int a) {
        return flight.facing[a];
    }

    /**
     * Step F6s: ace {@code a}'s bank now, radians (at most {@link AceFlight#MAX_BANK}; positive: leaning into a turn
     * to its right, its nose turning clockwise on the screen), for the look.
     */
    public double bank(int a) {
        return flight.bank[a];
    }

    /** Whether ace {@code a}'s hull still tumbles on the field (dead, not yet gone). */
    public boolean tumbling(int a) {
        return flight.tumbling[a];
    }

    /** A dead ace's tumble: its spin, radians a second (its sign: the way it rolls out). */
    public double spin(int a) {
        return flight.spin[a];
    }

    /** Ace {@code a}'s move now. */
    public AceMove move(int a) {
        return MOVES[flight.move[a]];
    }

    /** Ace {@code a}'s move's leg now (the playbook times its fire on it). */
    public AceLeg leg(int a) {
        return flight.legOf(a);
    }

    /** Steps since ace {@code a}'s current leg began (0 in its first step). */
    public int legTicks(int a) {
        return flight.legTicks[a];
    }

    /** Seconds since ace {@code a}'s move began. */
    public double moveSeconds(int a) {
        return flight.moveTicks[a] * SimStep.SECONDS;
    }

    /** Ace {@code a}'s keep-off from the ship now (the feinter's pull-out, committed after a death); 0 for none. */
    public double keepOff(int a) {
        return flight.keepOff(a);
    }

    /** The gap between aces {@code a}'s and {@code b}'s hulls, edge to edge, px. */
    public double gap(int a, int b) {
        return flight.gap(a, b);
    }

    /** The {@link SimEvents.Type#ACE_TELL} value: ace × 4 + its edge's ordinal. */
    static int tellValue(int a, WarningEdge e) {
        return a * EDGES.length + e.ordinal();
    }

    /** The ace of an {@link SimEvents.Type#ACE_TELL} value. */
    public static int tellAce(int value) {
        return value / EDGES.length;
    }

    /** The edge of an {@link SimEvents.Type#ACE_TELL} value. */
    public static WarningEdge tellEdge(int value) {
        return EDGES[value % EDGES.length];
    }

    /** Where ace {@code a}'s tell shows: on its edge where it will cross. */
    private double tellX(int a) {
        return Math.clamp(flight.x[a], 0, PlayField.WIDTH);
    }

    private double tellY(int a) {
        return Math.clamp(flight.y[a], 0, PlayField.HEIGHT);
    }

    AceFlight flight() {
        return flight;
    }

    /** Step F1b: its playbook and conductor (the tells, the told dive lines); empty without one. */
    public java.util.Optional<AcePlaybook> playbook() {
        return playbookView;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(ticks).add(deaths);
        for (int a = 0; a < count; a++) {
            hash.add(state[a]).add(stateTicks[a]).add(edge[a]).add(joinedAt[a]).add(strikeTicks[a]);
        }
        flight.addTo(hash);
        if (playbook != null) {
            playbook.addTo(hash);
        }
    }
}
