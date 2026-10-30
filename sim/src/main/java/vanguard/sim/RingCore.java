package vanguard.sim;

/**
 * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Phases, Tells and Dart Drones; {@link RingSpec.Core}): a
 * ringed boss's core, kept by its {@link RingArena}. Shut (before its phase) its fans glow before each volley ({@link
 * #glowDue}). When its phase comes it opens over its opening time ({@link SimEvents.Type#CORE_OPENING}, {@link
 * SimEvents.Type#CORE_OPEN}): untouchable and silent until open (the phase's clocks wait). Open, its spiral runs in its
 * cycle (tell, run, rest; {@link #spiralling}), its rail line fires only below its HP share ({@link #fires}), and its
 * rim bays launch Dart streams, one bay a turn: the nearest the ship outside the skip distance, after the bay's tell
 * ({@link SimEvents.Type#BAY_TELL}), the stream's units its gap apart through {@link BossActions#bayLaunch}. Its state
 * is hashed with its arena's. Allocates nothing once made.
 *
 * <p>Step BOSS70 (round 70): with a playbook ({@link RingSpec.Pool}) the open core rests, then draws its next attack
 * (the spiral, the rail volley, the Dart surge, the mine ring or the wreck throw) with the simulation's seeded random
 * ({@link BossActions#draw}), tells it ({@link SimEvents.Type#CORE_PLAY_TELL}), runs it and rests again; its timed spiral
 * cycle, its rail below its share and its bays' timed streams give way to it.
 */
public final class RingCore {
    private static final RingSpec.Play[] PLAYS = RingSpec.Play.values();

    private final SetPiece piece;
    private final RingSpec.Core spec;
    /** The phase that exposes the core (it opens then). */
    private final int phase;

    private final int openingTicks;
    private final int glowTicks;
    private final int tellTicks;
    private final int runTicks;
    private final int cycleTicks;
    private final RingSpec.Bays bays;
    private final int bayEveryTicks;
    private final int bayTellTicks;
    private final int bayGapTicks;

    /** Steps since its opening began; -1 before. */
    private int openTicks;
    /** Steps to the next bay's stream (from the opening's end); counts down while open. */
    private int bayTicks;
    /** The bay told for the next stream; -1 for none (its turn passes). */
    private int bay;
    /** The stream's units still to launch, and the steps to the next one. */
    private int streamLeft;

    private int streamGapTicks;
    /** The bay the running stream comes out of. */
    private int streamBay;

    /** Step BOSS70: its ring arena (the locked rings' wrecks); its playbook, null without one. */
    private final RingArena arena;

    private final RingSpec.Pool pool;
    private final int playTellTicks;
    private final int restPlayTicks;
    private final int lowRestTicks;
    private final CoreVolley volley;
    /** {@link #volley} for the presentation and the hit passes, made once. */
    private final java.util.Optional<CoreVolley> volleyView;

    private final int wreckGapTicks;
    /** The play running (its ordinal), -1 while it rests. */
    private int play;
    /** Steps into the running play (its tell, then its run); its run's length, steps. */
    private int playTicks;

    private int playRun;
    /** Steps left of the rest before the next draw. */
    private int restTicks;
    /** The draws so far, and per play the draw it was last drawn at (-1: never). */
    private int draws;

    private final int[] drawnAt = new int[PLAYS.length];
    /** The surge's units still to launch from each bay, and the steps to the next. */
    private int surgeLeft;

    private int surgeGapTicks;
    /** Per part, whether its wreck was thrown (its socket empty). */
    private final boolean[] thrown;
    /** The running throw's wrecks (parts), their count and the next to throw. */
    private final int[] wrecks;

    private int wreckCount;
    private int wreckNext;
    /** Whether the core went below its playbook's low share (the radio's line, once). */
    private boolean low;

    RingCore(SetPiece piece, BossSpec boss, RingSpec.Core spec, RingArena arena) {
        this.piece = piece;
        this.spec = spec;
        this.arena = arena;
        pool = spec.pool().orElse(null);
        playTellTicks = pool == null ? 0 : SimStep.ticks(pool.tellSeconds());
        restPlayTicks = pool == null ? 0 : SimStep.ticks(pool.restSeconds());
        lowRestTicks = pool == null ? 0 : SimStep.ticks(pool.lowRestSeconds());
        volley = pool == null
                ? null
                : pool.volley().map(v -> new CoreVolley(v, boss)).orElse(null);
        volleyView = java.util.Optional.ofNullable(volley);
        wreckGapTicks = pool == null
                ? 1
                : pool.wrecks()
                        .map(w -> Math.max(1, SimStep.ticks(w.gapSeconds())))
                        .orElse(1);
        thrown = new boolean[piece.spec().parts().size()];
        wrecks = new int
                [pool == null ? 0 : pool.wrecks().map(RingSpec.WreckThrow::most).orElse(0)];
        phase = boss.rings().orElseThrow().corePhase(boss.phases());
        openingTicks = SimStep.ticks(spec.openingSeconds());
        glowTicks = SimStep.ticks(spec.glowSeconds());
        tellTicks = SimStep.ticks(spec.spiralTellSeconds());
        runTicks = Math.max(1, SimStep.ticks(spec.spiralSeconds()));
        cycleTicks = Math.max(1, SimStep.ticks(spec.cycleSeconds()));
        bays = spec.bays().orElse(null);
        bayEveryTicks =
                bays == null ? 0 : Math.max(1, SimStep.ticks(bays.launcher().everySeconds()));
        bayTellTicks = bays == null ? 0 : SimStep.ticks(bays.tellSeconds());
        bayGapTicks =
                bays == null ? 0 : Math.max(1, SimStep.ticks(bays.launcher().gapSeconds()));
        reset();
    }

    /** Back to the level start (and the boss checkpoint): shut. */
    void reset() {
        openTicks = -1;
        bayTicks = 0;
        bay = -1;
        streamLeft = 0;
        streamGapTicks = 0;
        streamBay = -1;
        play = -1;
        playTicks = 0;
        playRun = 0;
        restTicks = restPlayTicks;
        draws = 0;
        java.util.Arrays.fill(drawnAt, -1);
        surgeLeft = 0;
        surgeGapTicks = 0;
        java.util.Arrays.fill(thrown, false);
        wreckCount = 0;
        wreckNext = 0;
        low = false;
        if (volley != null) {
            volley.reset();
        }
    }

    /**
     * One engaged step with the ship at (shipX, shipY), the boss in {@code current}: in its phase the opening runs; open,
     * the spiral's tells and the bays' turns and streams (their launches only while {@code firing}).
     */
    void step(int current, double shipX, double shipY, BossActions actions, boolean firing) {
        if (current < phase || piece.partHp(spec.part()) <= 0) {
            return;
        }
        double cx = piece.partX(spec.part());
        double cy = piece.partY(spec.part());
        if (openTicks < 0) {
            openTicks = 0;
            actions.ring(SimEvents.Type.CORE_OPENING, cx, cy, 0);
        } else {
            openTicks++;
        }
        if (openTicks < openingTicks) {
            return;
        }
        int sinceOpen = openTicks - openingTicks;
        if (sinceOpen == 0) {
            actions.ring(SimEvents.Type.CORE_OPEN, cx, cy, 0);
            if (bays != null) {
                bayTicks = Math.max(1, SimStep.ticks(bays.launcher().firstSeconds()));
            }
        }
        if (pool != null) {
            playbook(cx, cy, shipX, shipY, actions, firing);
            return;
        }
        if (spec.spiral() >= 0 && sinceOpen % cycleTicks == 0) {
            actions.ring(SimEvents.Type.CORE_SPIRAL_TELL, cx, cy, 0);
        }
        if (bays != null) {
            bays(shipX, shipY, actions, firing);
        }
    }

    private void bays(double shipX, double shipY, BossActions actions, boolean firing) {
        if (streamLeft > 0 && --streamGapTicks <= 0) {
            EnemySpec.Launcher launcher = bays.launcher();
            int place = launcher.stream() - streamLeft;
            streamLeft--;
            streamGapTicks = bayGapTicks;
            if (firing) {
                int n = launcher.stream();
                double spread = launcher.spreadRadians();
                double out = bays.radians(streamBay);
                // Outward through the bay (y up: the bay at (-r sin a, -r cos a) from the centre), spread round it.
                double angle = StrictMath.atan2(-Trig.cos(out), -Trig.sin(out))
                        + (n == 1 ? 0 : -spread / 2 + place * spread / (n - 1));
                actions.bayLaunch(launcher, bayX(streamBay), bayY(streamBay), angle);
            }
        }
        bayTicks--;
        if (bayTicks == bayTellTicks) {
            bay = nearestBay(shipX, shipY);
            if (bay >= 0 && firing) {
                actions.ring(SimEvents.Type.BAY_TELL, bayX(bay), bayY(bay), bay);
            }
        }
        if (bayTicks <= 0) {
            bayTicks = bayEveryTicks;
            if (bay >= 0 && firing) {
                streamBay = bay;
                streamLeft = bays.launcher().stream();
                streamGapTicks = 1;
            }
            bay = -1;
        }
    }

    /**
     * Step BOSS70: the playbook's step: the running volley and surge go on; resting, the rest counts down to the next
     * draw; a play past its tell runs (its start, its launches and throws), then the rest begins.
     */
    private void playbook(double cx, double cy, double shipX, double shipY, BossActions actions, boolean firing) {
        if (volley != null && volley.step()) {
            actions.ring(SimEvents.Type.CORE_VOLLEY_FIRED, cx, cy, volley.lines());
        }
        surge(firing, actions);
        if (!low
                && piece.partHp(spec.part())
                        < pool.lowBelow()
                                * piece.spec().parts().get(spec.part()).hp()) {
            low = true;
            actions.ring(SimEvents.Type.CORE_LOW, cx, cy, 0);
        }
        if (play < 0) {
            if (restTicks-- > 0) {
                return;
            }
            draw(cx, cy, actions);
            return;
        }
        playTicks++;
        int run = playTicks - playTellTicks;
        if (run < 0) {
            return;
        }
        RingSpec.Play now = PLAYS[play];
        switch (now) {
            case SPIRAL -> {}
            case VOLLEY -> {
                if (run == 0 && firing) {
                    volley.start(cx, cy, shipX, shipY);
                    actions.ring(SimEvents.Type.CORE_VOLLEY_TELEGRAPH, cx, cy, volley.lines());
                }
            }
            case SURGE -> {
                if (run == 0) {
                    surgeLeft = pool.surge().orElseThrow().stream();
                    surgeGapTicks = 0;
                    surge(firing, actions);
                }
            }
            case MINES -> {
                if (run == 0 && firing) {
                    mines(cx, cy, actions);
                }
            }
            case WRECKS -> {
                if (run % wreckGapTicks == 0 && wreckNext < wreckCount) {
                    int p = wrecks[wreckNext++];
                    if (firing
                            && actions.throwWreck(
                                    pool.wrecks().orElseThrow().gun(), piece.partX(p), piece.partY(p), p)) {
                        thrown[p] = true;
                        actions.ring(SimEvents.Type.CORE_WRECK_THROWN, piece.partX(p), piece.partY(p), p);
                    }
                }
            }
        }
        if (run >= playRun) {
            play = -1;
            restTicks = low ? lowRestTicks : restPlayTicks;
        }
    }

    /** Step BOSS70: the next play, drawn by weight among those allowed now; its tell starts. */
    private void draw(double cx, double cy, BossActions actions) {
        double total = 0;
        java.util.List<RingSpec.Move> moves = pool.moves();
        for (int i = 0; i < moves.size(); i++) {
            total += allowed(moves.get(i)) ? moves.get(i).weight() : 0;
        }
        RingSpec.Play picked = null;
        if (total > 0) {
            double r = actions.draw() * total;
            for (int i = 0; i < moves.size(); i++) {
                RingSpec.Move move = moves.get(i);
                if (!allowed(move)) {
                    continue;
                }
                picked = move.play();
                r -= move.weight();
                if (r < 0) {
                    break;
                }
            }
        } else {
            // Every play held by its spacing: the first that is not the last one drawn.
            for (int i = 0; i < moves.size(); i++) {
                RingSpec.Move move = moves.get(i);
                if (picked == null
                        && available(move.play())
                        && drawnAt[move.play().ordinal()] != draws - 1) {
                    picked = move.play();
                }
            }
            if (picked == null) {
                picked = RingSpec.Play.SPIRAL;
            }
        }
        play = picked.ordinal();
        drawnAt[play] = draws++;
        playTicks = 0;
        playRun = runTicks(picked, actions);
        actions.ring(SimEvents.Type.CORE_PLAY_TELL, cx, cy, play);
        actions.corePlay(picked, low);
    }

    /** Whether {@code move} may be drawn now: not drawn within its spacing, and its play possible. */
    private boolean allowed(RingSpec.Move move) {
        int at = drawnAt[move.play().ordinal()];
        return available(move.play()) && (at < 0 || draws - at >= move.spacing());
    }

    /** Whether {@code p} can run: the wreck throw only with a wreck left on a locked ring. */
    private boolean available(RingSpec.Play p) {
        return p != RingSpec.Play.WRECKS || wreckLeft(0) >= 0;
    }

    /** The {@code n}-th (from 0) dead battery on a locked ring whose wreck is still on its socket; -1 for none. */
    private int wreckLeft(int n) {
        for (int p = 0; p < thrown.length; p++) {
            int i = arena.battery(p);
            if (i >= 0 && arena.locked(i) && piece.partHp(p) <= 0 && !thrown[p] && !chosen(p) && n-- == 0) {
                return p;
            }
        }
        return -1;
    }

    private boolean chosen(int p) {
        for (int k = 0; k < wreckCount; k++) {
            if (wrecks[k] == p) {
                return true;
            }
        }
        return false;
    }

    /** The run's length of {@code p}, steps; the wreck throw picks its wrecks (seeded) as it is drawn. */
    private int runTicks(RingSpec.Play p, BossActions actions) {
        return switch (p) {
            case SPIRAL -> runTicks;
            case VOLLEY -> volley.ticks();
            case SURGE -> {
                yield (pool.surge().orElseThrow().stream() - 1) * bayGapTicks + 1;
            }
            case MINES -> Math.max(1, SimStep.ticks(pool.mines().orElseThrow().throwSeconds()));
            case WRECKS -> {
                RingSpec.WreckThrow spec = pool.wrecks().orElseThrow();
                wreckCount = 0;
                wreckNext = 0;
                int n = spec.fewest() + (int) (actions.draw() * (spec.most() - spec.fewest() + 1));
                n = Math.min(n, spec.most());
                for (int k = 0; k < n; k++) {
                    int left = 0;
                    while (wreckLeft(left) >= 0) {
                        left++;
                    }
                    if (left == 0) {
                        break;
                    }
                    wrecks[wreckCount++] = wreckLeft(Math.min(left - 1, (int) (actions.draw() * left)));
                }
                yield (Math.max(1, wreckCount) - 1) * wreckGapTicks + 1;
            }
        };
    }

    /** The surge's streams: every bay launches its next unit, its gap apart, until the stream is out. */
    private void surge(boolean firing, BossActions actions) {
        if (surgeLeft <= 0 || --surgeGapTicks > 0) {
            return;
        }
        int n = pool.surge().orElseThrow().stream();
        int place = n - surgeLeft;
        surgeLeft--;
        surgeGapTicks = bayGapTicks;
        if (!firing) {
            return;
        }
        double spread = bays.launcher().spreadRadians();
        for (int k = 0; k < bays.count(); k++) {
            double out = bays.radians(k);
            double angle = StrictMath.atan2(-Trig.cos(out), -Trig.sin(out))
                    + (n == 1 ? 0 : -spread / 2 + place * spread / (n - 1));
            actions.bayLaunch(bays.launcher(), bayX(k), bayY(k), angle);
        }
    }

    /** The mine ring: its mines thrown out at even bearings (a seeded turn) to seeded distances. */
    private void mines(double cx, double cy, BossActions actions) {
        RingSpec.MineRing ring = pool.mines().orElseThrow();
        double turn = actions.draw() * 2 * StrictMath.PI / ring.count();
        for (int k = 0; k < ring.count(); k++) {
            double bearing = turn + 2 * StrictMath.PI * k / ring.count();
            double d = ring.nearPx() + actions.draw() * (ring.farPx() - ring.nearPx());
            actions.coreMine(
                    ring.gun(),
                    cx,
                    cy,
                    cx + d * Trig.cos(bearing),
                    cy + d * Trig.sin(bearing),
                    ring.throwSeconds(),
                    ring.lifeSeconds());
        }
        actions.ring(SimEvents.Type.CORE_MINES_THROWN, cx, cy, ring.count());
    }

    /** The bay nearest (shipX, shipY) that lies farther than the skip distance from it; -1 for none. */
    private int nearestBay(double shipX, double shipY) {
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        double skip = bays.skipWithinPx();
        for (int k = 0; k < bays.count(); k++) {
            double dx = bayX(k) - shipX;
            double dy = bayY(k) - shipY;
            double d = dx * dx + dy * dy;
            if (d > skip * skip && d < bestDistance) {
                best = k;
                bestDistance = d;
            }
        }
        return best;
    }

    /** Whether the fan attack {@code a}'s glow starts now: its volley {@code ticks} steps away, the core shut. */
    boolean glowDue(BossSpec.Attack attack, int ticks) {
        return attack.pattern() == BossSpec.Pattern.FAN
                && ticks == glowTicks
                && glowTicks > 0
                && attack.parts().contains(spec.part())
                && openTicks < 0;
    }

    /** Whether the core is in its phase and not yet open (untouchable, silent). */
    public boolean opening() {
        return piece.phase() >= phase && (openTicks < 0 || openTicks < openingTicks) && !piece.destroyed();
    }

    /** Whether the core is open (its phase, past its opening). */
    public boolean open() {
        return openTicks >= openingTicks && openTicks >= 0;
    }

    /** The opening's progress, 0 shut to 1 open. */
    public double openShare() {
        if (openTicks < 0) {
            return 0;
        }
        return openingTicks == 0 ? 1 : Math.min(1, (double) openTicks / openingTicks);
    }

    /** Whether part {@code p} is the core and cannot be touched now (in its phase until open). */
    boolean untouchable(int p) {
        return p == spec.part() && opening();
    }

    /** Whether attack {@code a} may fire now: the core's rail only below its HP share; any other attack always. */
    boolean fires(int a) {
        return a != spec.rail()
                || pool == null
                        && piece.partHp(spec.part())
                                < spec.railBelow()
                                        * piece.spec().parts().get(spec.part()).hp();
    }

    /** Whether attack {@code a} runs now: the core's spiral only in its cycle's run; any other attack always. */
    boolean spiralling(int a) {
        if (a != spec.spiral()) {
            return true;
        }
        return open() && spiralOn();
    }

    /** Whether the open core's spiral is in its run now (past its tell, before its rest). */
    public boolean spiralOn() {
        if (!open()) {
            return false;
        }
        if (pool != null) {
            return play == RingSpec.Play.SPIRAL.ordinal() && playTicks >= playTellTicks;
        }
        int at = (openTicks - openingTicks) % cycleTicks;
        return at >= tellTicks && at < tellTicks + runTicks;
    }

    /** Whether the open core's spiral is in its tell now (the reactor brightening). */
    public boolean spiralTelling() {
        if (pool != null) {
            return telling() && play == RingSpec.Play.SPIRAL.ordinal();
        }
        return open() && (openTicks - openingTicks) % cycleTicks < tellTicks;
    }

    /** Step BOSS70: whether it has a playbook. */
    public boolean playbook() {
        return pool != null;
    }

    /** Step BOSS70: the play running (in its tell or its run); null while it rests or without a playbook. */
    public RingSpec.Play play() {
        return play < 0 ? null : PLAYS[play];
    }

    /** Step BOSS70: whether the running play is in its tell. */
    public boolean telling() {
        return open() && play >= 0 && playTicks < playTellTicks;
    }

    /** Step BOSS70: the running play's tell's share, 0 to 1 (1 past it, 0 while resting). */
    public double tellShare() {
        if (play < 0) {
            return 0;
        }
        return playTellTicks == 0 ? 1 : Math.min(1, (double) playTicks / playTellTicks);
    }

    /** Step BOSS70: the steps the running play has run past its tell (-1 while telling or resting). */
    public int runTicksNow() {
        return play < 0 || playTicks < playTellTicks ? -1 : playTicks - playTellTicks;
    }

    /** Step BOSS70: its rail volley; empty without one. */
    public java.util.Optional<CoreVolley> volley() {
        return volleyView;
    }

    /** Step BOSS70: whether its bays are lit for the surge (its tell and its streams). */
    public boolean baysLit() {
        return play == RingSpec.Play.SURGE.ordinal() || surgeLeft > 0;
    }

    /** Step BOSS70: the wreck glowing now before its throw (a part), -1 for none. */
    public int glowingWreck() {
        if (play != RingSpec.Play.WRECKS.ordinal() || wreckNext >= wreckCount) {
            return -1;
        }
        return wrecks[wreckNext];
    }

    /** Step BOSS70: the steps until the glowing wreck is thrown (its glow's rest); 0 for none. */
    public int wreckDueTicks() {
        if (glowingWreck() < 0) {
            return 0;
        }
        int at = playTellTicks + wreckNext * wreckGapTicks;
        return Math.max(0, at - playTicks);
    }

    /** Step BOSS70: whether part {@code p}'s wreck was thrown (its socket is empty). */
    public boolean thrown(int p) {
        return p >= 0 && p < thrown.length && thrown[p];
    }

    /** Step BOSS70: the plays drawn so far. */
    public int draws() {
        return draws;
    }

    /** Step BOSS70: whether it went below its playbook's low share (its rest shortened). */
    public boolean low() {
        return low;
    }

    /** The bay whose doors are open now (its tell, then its stream); -1 for none. */
    public int bayOpen() {
        if (streamLeft > 0) {
            return streamBay;
        }
        return bay >= 0 && bayTicks <= bayTellTicks ? bay : -1;
    }

    /** Its spec. */
    public RingSpec.Core spec() {
        return spec;
    }

    /** Its bays' count; 0 without bays. */
    public int bayCount() {
        return bays == null ? 0 : bays.count();
    }

    /** Bay {@code k}'s centre, px from the left. */
    public double bayX(int k) {
        return piece.partX(spec.part()) - bays.radius() * Trig.sin(bays.radians(k));
    }

    /** Bay {@code k}'s centre, px up. */
    public double bayY(int k) {
        return piece.partY(spec.part()) - bays.radius() * Trig.cos(bays.radians(k));
    }

    void addTo(StateHash hash) {
        hash.add(openTicks)
                .add(bayTicks)
                .add(bay)
                .add(streamLeft)
                .add(streamGapTicks)
                .add(streamBay);
        if (pool != null) {
            hash.add(play)
                    .add(playTicks)
                    .add(playRun)
                    .add(restTicks)
                    .add(draws)
                    .add(surgeLeft)
                    .add(surgeGapTicks);
            hash.add(wreckCount).add(wreckNext).add(low ? 1 : 0);
            for (int k = 0; k < drawnAt.length; k++) {
                hash.add(drawnAt[k]);
            }
            for (int p = 0; p < thrown.length; p++) {
                hash.add(thrown[p] ? 1 : 0);
            }
            for (int k = 0; k < wreckCount; k++) {
                hash.add(wrecks[k]);
            }
            if (volley != null) {
                volley.addTo(hash);
            }
        }
    }

    /** Step BOSS70: the core died: its volley stops. */
    void stop() {
        if (volley != null) {
            volley.stop();
        }
    }
}
