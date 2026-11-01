package vanguard.sim;

import java.util.List;

/**
 * M10 part F, step F1b (design/enemies/bosses/honour-guard, The playbook, Tells, The escort; m10f-gaps.md §1 rows 4–6):
 * a squadron's playbook and conductor, kept by its {@link Squadron} when its spec has one ({@link
 * SquadronSpec#playbook()}). Round 70's lesson, as the Iron Sovereign's core ({@link RingCore}; copied, not shared, so
 * Level 35 cannot move): each ace on its station rests, then draws its next play from its pool by weight with the
 * simulation's seeded random ({@link BossActions#draw}: a replay draws the same), never the play it drew last; tells it
 * ({@link SimEvents.Type#ACE_PLAY_TELL}); runs it ({@link SimEvents.Type#ACE_PLAY}: its move through {@link
 * Squadron#start}, its fire through the actions, the leader's lines through the boss's {@link BossLines}); and rests
 * again.
 *
 * <p>The conductor, with two or three aces: at most {@link SquadronPlaybook.Conductor#told} told lines or dives at a
 * time (a told play is not drawn while another runs), at most {@link SquadronPlaybook.Conductor#firing} aces in a
 * firing play (the third slides or waits), and no ace draws while another's tell runs. The pincer is drawn by a
 * flanker with its partner resting on its station, every ace flying, at most every {@link
 * SquadronPlaybook.Pincer#every}-th draw; the leader holds fire through it (the firing limit). Each death: the
 * survivors' rests shorter, each its {@link SquadronPlaybook.Play#edge}; the last ace's rests shorter still and it
 * draws the dead aces' signature plays too, flown its own way (a line or a spiral from its station, a dive with the
 * feinter's committed pull-out).
 *
 * <p>Its state is hashed with the squadron's. Allocates nothing once made.
 */
public final class AcePlaybook {
    /** What an ace's playbook does now. */
    public enum Mode {
        /** Resting (or waiting for the conductor) on its station. */
        REST,
        /** Telling its drawn play. */
        TELL,
        /** Running it. */
        RUN
    }

    private static final Mode[] MODES = Mode.values();
    private static final SquadronPlaybook.Kind[] KINDS = SquadronPlaybook.Kind.values();
    /** The pincer's play code (after the kinds' ordinals). */
    public static final int PINCER = KINDS.length;
    /** How many play codes there are ({@link #playCode}). */
    public static final int PLAYS = KINDS.length + 1;
    /** No play. */
    private static final int NONE = -1;
    /** Rook's keep-off from a told dive's line, px (his hull's edge from the line). */
    public static final double DIVE_KEEP_OFF = 40;

    private final SetPiece piece;
    private final Squadron squadron;
    private final SquadronPlaybook book;
    private final List<BossSpec.Attack> attacks;
    private final int count;
    private final int tellTicks;
    private final int upper;
    private final int lower;
    /** The feinter's committed pull-out (a borrowed dive's least), px. */
    private final double committedKeepOff;

    /** The draw's candidates and weights ({@link Playbook}, the draw H's boss playbooks share). */
    private final Playbook drawn;

    /** Per ace: its {@link Mode} ordinal and the steps in it, the steps of rest left. */
    private final int[] mode;

    private final int[] modeTicks;
    private final int[] restTicks;
    /** Per ace: the pool its play comes from (its own or a dead ace's), the play's index in it ({@link #NONE}: the pincer). */
    private final int[] pool;

    private final int[] play;
    /** Per ace: its play's code ({@link #playCode}), its last play's (never twice running), its tell's length. */
    private final int[] code;

    private final int[] last;
    private final int[] tell;
    /** Per ace: the shots (volleys, taps, bullets laid) so far, the step its timed fire counts from (-1: not yet). */
    private final int[] shots;

    private final int[] mark;
    /** Per ace: a sniper line requested and not yet seen firing. */
    private final boolean[] pending;
    /** Per ace: a spiral's first arm's bearing (radians, 0 = right, y up), a ribbon's pass start and length, its gaps. */
    private final double[] base;

    private final double[] passX;
    private final double[] passLength;
    private final int[] gapMask;
    /** The draws so far (all aces), and the draw the pincer was last drawn at. */
    private int draws;

    private int pincerAt;
    /** The ship's place at the last step (Rook's told dive line during a tell). */
    private double shipX;

    private double shipY;

    AcePlaybook(SetPiece piece, Squadron squadron, BossSpec boss, SquadronPlaybook book) {
        this.piece = piece;
        this.squadron = squadron;
        this.book = book;
        attacks = boss.attacks();
        count = squadron.aceCount();
        tellTicks = SimStep.ticks(book.tellSeconds());
        int up = -1;
        int low = -1;
        double committed = 0;
        for (int a = 0; a < count; a++) {
            SquadronSpec.Ace ace = squadron.spec().aces().get(a);
            if (ace.role() == SquadronSpec.Role.DANCER && up < 0) {
                up = a;
            }
            if (ace.role() == SquadronSpec.Role.FEINTER && low < 0) {
                low = a;
                committed = ace.station().committedKeepOff();
            }
        }
        upper = up;
        lower = low;
        committedKeepOff = committed;
        int most = 0;
        for (SquadronPlaybook.Pool p : book.pools()) {
            most += p.plays().size();
        }
        drawn = new Playbook(most);
        mode = new int[count];
        modeTicks = new int[count];
        restTicks = new int[count];
        pool = new int[count];
        play = new int[count];
        code = new int[count];
        last = new int[count];
        tell = new int[count];
        shots = new int[count];
        mark = new int[count];
        pending = new boolean[count];
        base = new double[count];
        passX = new double[count];
        passLength = new double[count];
        gapMask = new int[count];
        reset();
    }

    /** Back to the level start (and the boss checkpoint): every ace resting, nothing drawn. */
    void reset() {
        for (int a = 0; a < count; a++) {
            clear(a);
            last[a] = NONE;
            restTicks[a] = 0;
        }
        draws = 0;
        pincerAt = Playbook.NEVER;
        shipX = PlayField.WIDTH / 2.0;
        shipY = 0;
    }

    private void clear(int a) {
        mode[a] = Mode.REST.ordinal();
        modeTicks[a] = 0;
        pool[a] = NONE;
        play[a] = NONE;
        code[a] = NONE;
        tell[a] = 0;
        shots[a] = 0;
        mark[a] = -1;
        pending[a] = false;
        base[a] = 0;
        passX[a] = 0;
        passLength[a] = 0;
        gapMask[a] = 0;
    }

    /**
     * One step after the squadron's flight (the ship at (sx, sy)): a dead ace's play ends; each living ace's run, tell
     * or rest goes on, in the squadron's order; {@code firing} false: nothing fires (the moves still fly).
     */
    void step(double sx, double sy, BossActions actions, boolean firing) {
        shipX = sx;
        shipY = sy;
        for (int a = 0; a < count; a++) {
            if (squadron.state(a) == Squadron.State.DOWN && mode[a] != Mode.REST.ordinal()) {
                clear(a);
            }
        }
        for (int a = 0; a < count; a++) {
            if (squadron.state(a) != Squadron.State.FLYING) {
                // Not on the field yet: its first rest counts from its station.
                restTicks[a] = restFor(a);
                continue;
            }
            modeTicks[a]++;
            switch (MODES[mode[a]]) {
                case REST -> rest(a, actions);
                case TELL -> {
                    if (modeTicks[a] >= tell[a] && squadron.ready(a)) {
                        run(a, actions);
                    }
                }
                case RUN -> {
                    if (fire(a, actions, firing)) {
                        clear(a);
                        restTicks[a] = restFor(a);
                    }
                }
            }
        }
    }

    // The draw.

    /** Ace {@code a} rests on its station; its rest over, it draws when the conductor lets it. */
    private void rest(int a, BossActions actions) {
        if (!squadron.ready(a)) {
            restTicks[a] = restFor(a);
            return;
        }
        if (restTicks[a] > 0) {
            restTicks[a]--;
            return;
        }
        if (telling(a)) {
            return;
        }
        draw(a, actions);
    }

    /** Its rest now, steps: alone or with company, shorter after a death and shorter still for the last ace. */
    private int restFor(int a) {
        double seconds = squadron.flyingCount() >= 2 ? book.restCompany() : book.restAlone();
        int deaths = squadron.deaths();
        if (deaths >= count - 1 && deaths > 0) {
            seconds *= 1 - book.lastRest();
        } else if (deaths > 0) {
            seconds *= 1 - book.lossRest();
        }
        return SimStep.ticks(seconds);
    }

    /** Whether an ace other than {@code a} is telling its play. */
    private boolean telling(int a) {
        for (int b = 0; b < count; b++) {
            if (b != a && mode[b] == Mode.TELL.ordinal()) {
                return true;
            }
        }
        return false;
    }

    /** The aces other than {@code a} and {@code b} in a told (fires: firing) play, telling or running it. */
    private int busy(int a, int b, boolean told) {
        int n = 0;
        for (int c = 0; c < count; c++) {
            if (c == a || c == b || mode[c] == Mode.REST.ordinal()) {
                continue;
            }
            if (code[c] == PINCER) {
                n += told ? 0 : 1;
            } else {
                SquadronPlaybook.Kind kind = KINDS[code[c]];
                n += (told ? kind.told() : kind.fires()) ? 1 : 0;
            }
        }
        return n;
    }

    /** Ace {@code a} draws its next play among those the conductor allows now (none allowed: it waits). */
    private void draw(int a, BossActions actions) {
        drawn.clear();
        boolean company = squadron.flyingCount() >= 2;
        int told = busy(a, -1, true);
        int firing = busy(a, -1, false);
        boolean lastAce = squadron.deaths() >= count - 1 && squadron.deaths() > 0;
        for (int p = 0; p < count; p++) {
            if (p != a && !(lastAce && squadron.state(p) == Squadron.State.DOWN)) {
                continue;
            }
            List<SquadronPlaybook.Play> plays = book.pools().get(p).plays();
            for (int i = 0; i < plays.size(); i++) {
                SquadronPlaybook.Play candidate = plays.get(i);
                boolean allowed = (p == a || i == book.pools().get(p).signature())
                        && candidate.kind().ordinal() != last[a]
                        && (candidate.kind() != SquadronPlaybook.Kind.SLIDE || company)
                        && (!candidate.kind().told() || told < book.conductor().told())
                        && (!candidate.kind().fires()
                                || (firing < book.conductor().firing() && !pincering()))
                        && (candidate.kind() != SquadronPlaybook.Kind.SNIPER_LINE || piece.lines != null);
                drawn.offer(candidate.weight(), allowed);
            }
        }
        double pincer = pincerAllowed(a) ? book.pincer().orElseThrow().weight() : 0;
        double total = drawn.total() + pincer;
        if (!(total > 0)) {
            return;
        }
        double r = actions.draw() * total;
        draws++;
        if (pincer > 0 && r < pincer) {
            pincerAt = draws;
            int partner = a == upper ? lower : upper;
            begin(a, NONE, NONE, PINCER, tellTicks, actions);
            begin(partner, NONE, NONE, PINCER, tellTicks, actions);
            return;
        }
        // The last allowed play takes the rounding's rest.
        int at = drawn.pick(r - pincer);
        int n = 0;
        int pickPool = NONE;
        int pickPlay = NONE;
        for (int p = 0; p < count && pickPool == NONE; p++) {
            if (p != a && !(lastAce && squadron.state(p) == Squadron.State.DOWN)) {
                continue;
            }
            List<SquadronPlaybook.Play> plays = book.pools().get(p).plays();
            for (int i = 0; i < plays.size(); i++) {
                if (n++ == at) {
                    pickPool = p;
                    pickPlay = i;
                    break;
                }
            }
        }
        SquadronPlaybook.Play picked = book.pools().get(pickPool).plays().get(pickPlay);
        int ticks = Double.isNaN(picked.tellSeconds()) ? tellTicks : SimStep.ticks(picked.tellSeconds());
        begin(a, pickPool, pickPlay, picked.kind().ordinal(), ticks, actions);
    }

    /** Whether a flanker tells or flies the pincer now (the leader holds fire). */
    private boolean pincering() {
        for (int b = 0; b < count; b++) {
            if (mode[b] != Mode.REST.ordinal() && code[b] == PINCER) {
                return true;
            }
        }
        return false;
    }

    /** Whether ace {@code a} may draw the pincer now. */
    private boolean pincerAllowed(int a) {
        if (book.pincer().isEmpty() || upper < 0 || lower < 0 || (a != upper && a != lower) || last[a] == PINCER) {
            return false;
        }
        int partner = a == upper ? lower : upper;
        if (squadron.flyingCount() < count
                || squadron.deaths() > 0
                || mode[partner] != Mode.REST.ordinal()
                || !squadron.ready(partner)
                || !Playbook.spaced(draws, pincerAt, book.pincer().get().every())) {
            return false;
        }
        for (int b = 0; b < count; b++) {
            if (!squadron.touchable(b)) {
                return false;
            }
        }
        return busy(a, partner, false) + 2 <= book.conductor().firing();
    }

    /** Ace {@code a} starts telling play {@code p} of pool {@code from} (code {@code c}) for {@code ticks} steps. */
    private void begin(int a, int from, int p, int c, int ticks, BossActions actions) {
        clear(a);
        mode[a] = Mode.TELL.ordinal();
        pool[a] = from;
        play[a] = p;
        code[a] = c;
        last[a] = c;
        tell[a] = ticks;
        actions.squadron(SimEvents.Type.ACE_PLAY_TELL, squadron.x(a), squadron.y(a), playValue(a, c));
    }

    // The run.

    /** Ace {@code a}'s tell is over: its move starts. */
    private void run(int a, BossActions actions) {
        if (code[a] == PINCER) {
            int partner = a == upper ? lower : upper;
            if (mode[partner] != Mode.TELL.ordinal() || code[partner] != PINCER || !squadron.ready(partner)) {
                // Its partner is gone (or off its station): the pincer is off; it rests.
                clear(a);
                restTicks[a] = restFor(a);
                return;
            }
            // Both flankers cross from here, whichever's tell ended first.
            for (int f = 0; f < 2; f++) {
                int b = f == 0 ? a : partner;
                mode[b] = Mode.RUN.ordinal();
                modeTicks[b] = 0;
                actions.squadron(SimEvents.Type.ACE_PLAY, squadron.x(b), squadron.y(b), playValue(b, PINCER));
            }
            squadron.startPincer(upper, lower, shipX, shipY);
            return;
        }
        mode[a] = Mode.RUN.ordinal();
        modeTicks[a] = 0;
        actions.squadron(SimEvents.Type.ACE_PLAY, squadron.x(a), squadron.y(a), playValue(a, code[a]));
        SquadronPlaybook.Play now = now(a);
        boolean own = pool[a] == a;
        switch (now.kind()) {
            case SLIDE -> squadron.start(a, AceMove.SLIDE, shipX, shipY, actions);
            case SPIRAL_LOOP -> {
                base[a] = StrictMath.atan2(shipY - squadron.y(a), shipX - squadron.x(a));
                if (own) {
                    squadron.start(a, AceMove.LOOP, shipX, shipY, actions);
                }
            }
            case ROLL_FAN -> squadron.start(a, AceMove.ROLL, shipX, shipY, actions);
            case RIBBON -> squadron.start(a, AceMove.RIBBON, shipX, shipY, actions);
            case FEINT_DIVE -> squadron.flight().startDive(a, shipX, shipY, own ? 0 : committedKeepOff, actions);
            case REAR_RAKE -> squadron.start(a, AceMove.RAKE, shipX, shipY, actions);
            case OVERHEAD -> squadron.start(a, AceMove.OVERHEAD, shipX, shipY, actions);
            case SNIPER_LINE, PAIR_VOLLEY, FAN -> {}
        }
    }

    private SquadronPlaybook.Play now(int a) {
        return book.pools().get(pool[a]).plays().get(play[a]);
    }

    /** Ace {@code a}'s run's fire this step; returns whether its play is over. */
    private boolean fire(int a, BossActions actions, boolean firing) {
        int run = modeTicks[a] - 1;
        if (code[a] == PINCER) {
            return pincerFan(a, run, actions, firing);
        }
        SquadronPlaybook.Play now = now(a);
        BossSpec.Attack attack = now.attack() >= 0 ? attacks.get(now.attack()) : null;
        boolean edge = squadron.deaths() > 0 && now.edge() > 0;
        int gap = Math.max(1, SimStep.ticks(now.gapSeconds()));
        double x = squadron.x(a);
        double y = squadron.y(a);
        boolean home = run > 0 && squadron.ready(a);
        switch (now.kind()) {
            case SNIPER_LINE -> {
                return sniper(a, run, now, edge ? now.edge() : now.count(), gap, firing);
            }
            case PAIR_VOLLEY, FAN -> {
                if (run == shots[a] * gap && shots[a] < now.count()) {
                    shots[a]++;
                    if (firing) {
                        actions.aimed(x, y, attack.gun());
                    }
                }
                return shots[a] >= now.count();
            }
            case SLIDE -> {
                return home;
            }
            case SPIRAL_LOOP -> {
                boolean looping = pool[a] == a ? !home : run < SimStep.ticks(now.seconds());
                if (looping) {
                    spiral(a, run, attack, edge ? now.edge() : attack.arms(), x, y, actions, firing);
                }
                return !looping;
            }
            case ROLL_FAN -> {
                if (shots[a] == 0 && run > 0 && (squadron.leg(a) != AceLeg.PASS || home)) {
                    // The roll's middle: the end of its pass.
                    shots[a] = 1;
                    if (firing) {
                        actions.aimed(x, y, attack.gun());
                    }
                }
                return home && shots[a] > 0;
            }
            case RIBBON -> {
                ribbon(a, now, attack, actions, firing);
                return home && shots[a] >= now.trail().orElseThrow().count();
            }
            case FEINT_DIVE, OVERHEAD -> {
                int burst = edge ? now.edge() : now.count();
                if (mark[a] < 0 && squadron.leg(a) == AceLeg.PULL_OUT) {
                    mark[a] = run;
                }
                if (mark[a] >= 0 && shots[a] < burst && run == mark[a] + shots[a] * gap) {
                    shots[a]++;
                    if (firing) {
                        actions.aimed(x, y, attack.gun());
                    }
                }
                return home && (shots[a] >= burst || mark[a] < 0);
            }
            case REAR_RAKE -> {
                if (squadron.leg(a) == AceLeg.CLIMB) {
                    if (mark[a] < 0) {
                        mark[a] = run;
                    }
                    if ((run - mark[a]) % gap == 0) {
                        shots[a]++;
                        if (firing) {
                            actions.aimed(x, y, attack.gun());
                        }
                    }
                }
                return home;
            }
        }
        return true;
    }

    /**
     * The sniper line: its line requested at once, then {@code taps} taps, each {@code gap} steps after the line
     * before it fired; over once the last has been requested, started and run out.
     */
    private boolean sniper(int a, int run, SquadronPlaybook.Play now, int taps, int gap, boolean firing) {
        BossLines lines = piece.lines;
        int p = squadron.part(a);
        if (run == 0) {
            if (!firing) {
                return true;
            }
            lines.request(now.attack(), p);
            pending[a] = true;
            return false;
        }
        if (pending[a] && lines.part() == p && lines.live()) {
            pending[a] = false;
            mark[a] = run;
        }
        if (pending[a]) {
            // Waiting for its line to start and fire; over if it lapsed (its part died, its telegraph cancelled).
            return !lines.due(p) && lines.part() != p;
        }
        if (shots[a] < taps) {
            if (run < mark[a] + gap) {
                return false;
            }
            if (!firing) {
                return true;
            }
            shots[a]++;
            mark[a] = -1;
            lines.request(now.follow(), p);
            pending[a] = true;
            return false;
        }
        return lines.part() != p;
    }

    /** One step of a spiral of {@code arms} from (x, y): a bullet per arm every interval, the arms turning. */
    private void spiral(
            int a, int run, BossSpec.Attack attack, int arms, double x, double y, BossActions actions, boolean firing) {
        int interval = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
        if (run % interval != 0 || !firing || arms < 1) {
            return;
        }
        double turn = base[a] + attack.turnRadiansPerSecond() * run * SimStep.SECONDS;
        for (int k = 0; k < arms; k++) {
            actions.bullet(
                    x,
                    y,
                    turn + 2 * StrictMath.PI * k / arms,
                    attack.gun().bulletSpeed(),
                    attack.gun().damage());
        }
    }

    /**
     * The ribbon: as its pass starts, the gaps are drawn; along it, each bullet is laid as the dancer passes its point
     * (at even spacing over the pass less the gaps), falling straight down; what the pass left unlaid at its end is laid
     * there.
     */
    private void ribbon(int a, SquadronPlaybook.Play now, BossSpec.Attack attack, BossActions actions, boolean firing) {
        SquadronPlaybook.Trail trail = now.trail().orElseThrow();
        AceFlight flight = squadron.flight();
        boolean passing = squadron.leg(a) == AceLeg.PASS;
        if (mark[a] < 0) {
            if (!passing) {
                return;
            }
            mark[a] = modeTicks[a];
            passX[a] = flight.legStartX(a);
            passLength[a] = flight.legEndX(a) - passX[a];
            int mask = 0;
            for (int g = 0; g < trail.gaps(); g++) {
                int free = trail.count() - 1 - g;
                int pick = Math.min(free - 1, (int) (actions.draw() * free));
                for (int i = 0; i < trail.count() - 1; i++) {
                    if ((mask & (1 << i)) == 0 && pick-- == 0) {
                        mask |= 1 << i;
                        break;
                    }
                }
            }
            gapMask[a] = mask;
        }
        double length = Math.abs(passLength[a]);
        double dir = passLength[a] >= 0 ? 1 : -1;
        double spacing = (length - trail.gaps() * trail.gapPx()) / (trail.count() - 1);
        double done = passing ? Math.abs(flight.pathX[a] - passX[a]) : length;
        while (shots[a] < trail.count()) {
            int i = shots[a];
            double at = i * spacing + trail.gapPx() * Integer.bitCount(gapMask[a] & ((1 << i) - 1));
            if (at > done + 1e-9) {
                break;
            }
            shots[a]++;
            if (firing) {
                actions.bullet(
                        passX[a] + dir * at,
                        squadron.y(a),
                        -StrictMath.PI / 2,
                        trail.speed(),
                        attack.gun().damage());
            }
        }
    }

    /**
     * A flanker's pincer: the upper fires its fan as its pass crosses the ship's x (or as its pass ends), the lower
     * {@link SquadronPlaybook.Pincer#gapSeconds} after it (or at its own crossing when the upper is gone).
     */
    private boolean pincerFan(int a, int run, BossActions actions, boolean firing) {
        SquadronPlaybook.Pincer pincer = book.pincer().orElseThrow();
        boolean home = run > 0 && squadron.ready(a);
        if (shots[a] == 0) {
            boolean partnerFired =
                    a == lower && mode[upper] == Mode.RUN.ordinal() && code[upper] == PINCER && shots[upper] > 0;
            if (partnerFired && mark[a] < 0) {
                mark[a] = run + SimStep.ticks(pincer.gapSeconds());
            }
            boolean upperGone = a == lower && (mode[upper] != Mode.RUN.ordinal() || code[upper] != PINCER);
            boolean due = mark[a] >= 0 ? run >= mark[a] : (a == upper || upperGone) && crossed(a);
            if (due || (home && run > 0)) {
                shots[a] = 1;
                if (firing && !home) {
                    actions.aimed(
                            squadron.x(a),
                            squadron.y(a),
                            attacks.get(pincer.attack()).gun());
                }
            }
        }
        return home && shots[a] > 0;
    }

    /** Whether ace {@code a}'s pass has reached the ship's x (or left its pass). */
    private boolean crossed(int a) {
        if (squadron.leg(a) == AceLeg.APPROACH || squadron.leg(a) == AceLeg.HOLD) {
            return false;
        }
        if (squadron.leg(a) != AceLeg.PASS) {
            return true;
        }
        AceFlight flight = squadron.flight();
        double from = flight.legStartX(a);
        double to = flight.legEndX(a);
        double at = flight.pathX[a];
        return to >= from ? at >= Math.min(shipX, to) : at <= Math.max(shipX, to);
    }

    // The reads (the presentation, Rook, the autopilot, the tests).

    /** Ace {@code a}'s playbook mode now. */
    public Mode mode(int a) {
        return MODES[mode[a]];
    }

    /** Ace {@code a}'s play's code now ({@link #playCode}); -1 while resting. */
    public int play(int a) {
        return mode[a] == Mode.REST.ordinal() ? NONE : code[a];
    }

    /** Ace {@code a}'s play's kind now; null while resting or in the pincer. */
    public SquadronPlaybook.Kind kind(int a) {
        int c = play(a);
        return c < 0 || c == PINCER ? null : KINDS[c];
    }

    /** Whether ace {@code a} flies the pincer now (telling or running it). */
    public boolean pincer(int a) {
        return play(a) == PINCER;
    }

    /** How far ace {@code a}'s tell has run, 0–1; 0 unless telling. */
    public double tellShare(int a) {
        return mode[a] == Mode.TELL.ordinal() ? Math.min(1, modeTicks[a] / (double) Math.max(1, tell[a])) : 0;
    }

    /** Whether the leader holds fire for the pincer now (its crest dims): a flanker tells or runs it. */
    public boolean holdingFire() {
        return pincering();
    }

    /** The aces in a told line or dive now (telling or running it). */
    public int told() {
        return busy(-1, -1, true);
    }

    /** The aces in a firing play now (telling or running it; the pincer's two). */
    public int firing() {
        return busy(-1, -1, false);
    }

    /** The aces telling now. */
    public int telling() {
        int n = 0;
        for (int a = 0; a < count; a++) {
            n += mode[a] == Mode.TELL.ordinal() ? 1 : 0;
        }
        return n;
    }

    /** The draws so far. */
    public int draws() {
        return draws;
    }

    /**
     * Whether ace {@code a}'s dive is told or running (its tell: the line from it through the ship; its run: the locked
     * line): Rook keeps {@link #DIVE_KEEP_OFF} px off it, the autopilot side-steps it.
     */
    public boolean diveTold(int a) {
        SquadronPlaybook.Kind kind = kind(a);
        if (kind != SquadronPlaybook.Kind.FEINT_DIVE && kind != SquadronPlaybook.Kind.OVERHEAD) {
            return false;
        }
        return (mode[a] == Mode.TELL.ordinal() && kind == SquadronPlaybook.Kind.FEINT_DIVE)
                || squadron.flight().running(a);
    }

    /**
     * Step F6s: whether ace {@code a} wants its nose on the ship now: telling or running a play it fires from its
     * station (a sniper line, a pair volley, a fan), or telling a dive (its nose swings to the ship).
     */
    public boolean aims(int a) {
        SquadronPlaybook.Kind kind = kind(a);
        if (kind == null) {
            return false;
        }
        return switch (kind) {
            case SNIPER_LINE, PAIR_VOLLEY, FAN -> true;
            case FEINT_DIVE, OVERHEAD -> mode[a] == Mode.TELL.ordinal();
            default -> false;
        };
    }

    /** The told dive's line's start, px from the left: the ace in its tell, its run's start once running. */
    public double diveFromX(int a) {
        return mode[a] == Mode.TELL.ordinal()
                ? squadron.x(a)
                : squadron.flight().legStartX(a);
    }

    public double diveFromY(int a) {
        return mode[a] == Mode.TELL.ordinal()
                ? squadron.y(a)
                : squadron.flight().legStartY(a);
    }

    /** The told dive's aim, px from the left: the ship in its tell, the locked spot once running. */
    public double diveToX(int a) {
        return mode[a] == Mode.TELL.ordinal() ? shipX : squadron.flight().runToX(a);
    }

    public double diveToY(int a) {
        return mode[a] == Mode.TELL.ordinal() ? shipY : squadron.flight().runToY(a);
    }

    /** The {@link SimEvents.Type#ACE_PLAY_TELL} and {@link SimEvents.Type#ACE_PLAY} value: ace × {@link #PLAYS} + code. */
    static int playValue(int a, int c) {
        return a * PLAYS + c;
    }

    /** The ace of an {@link SimEvents.Type#ACE_PLAY_TELL} or {@link SimEvents.Type#ACE_PLAY} value. */
    public static int playAce(int value) {
        return value / PLAYS;
    }

    /** The play code of such a value: a {@link SquadronPlaybook.Kind} ordinal, or {@link #PINCER}. */
    public static int playCode(int value) {
        return value % PLAYS;
    }

    void addTo(StateHash hash) {
        hash.add(draws).add(pincerAt);
        for (int a = 0; a < count; a++) {
            hash.add(mode[a])
                    .add(modeTicks[a])
                    .add(restTicks[a])
                    .add(pool[a])
                    .add(play[a])
                    .add(code[a])
                    .add(last[a])
                    .add(tell[a])
                    .add(shots[a])
                    .add(mark[a])
                    .add(pending[a] ? 1 : 0)
                    .add(base[a])
                    .add(passX[a])
                    .add(passLength[a])
                    .add(gapMask[a]);
        }
    }
}
