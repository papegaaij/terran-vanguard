package vanguard.sim;

/**
 * A segment chain in flight (design/enemies/air/coilwyrm): a head, its segments and its tail,
 * each an {@link Enemy} with its own hit box and HP. The chain's head point flies the wave's path
 * (and its loop-back) and leaves a history of the points it passed; member {@code i} sits on that
 * history {@code offsets[i]} px behind the head point, facing along it. The head point flies on
 * when the head itself is destroyed, so the body keeps flowing while it pops.
 *
 * <p>A cut (a destroyed segment) of a chain that has not regrown yet splits it: the rear part
 * becomes a chain of its own that holds still while it grows a new head, then lunges at the ship
 * (the cut used the regrowth, for both parts). A cut of any other chain, and the head's death, let
 * the members behind it die one by one, from the cut backwards; while they wait for their burst
 * they are {@linkplain #doomed doomed}. Pooled, with fixed arrays, so the
 * chain never allocates while it flies.
 *
 * <p>M7 part C, a ground chain ({@link #ground()}; design/enemies/ground/threadcrawler): its head point crawls
 * its head's {@link EnemySpec.Crawl} path in ground space on top of the scroll, its history is kept in ground
 * space (x, row) and placed by the ground's scroll now, so the body scrolls with the ground; it never regrows
 * and never loops back. A cut stops the members behind it where they are; they die after the crawl's cut
 * delay, from the cut backwards. Its segments fire the pore wave ({@link #poreFiring()}, {@link #poreGlow}).
 * Opt-in: an air chain (the Coilwyrm) flies and hashes exactly as before.
 *
 * <p>M8 part C, a water chain (design/enemies/naval/eel-swarm; opt-in by its {@link EnemySpec.ChainSpec#scatter()},
 * its head's {@link EnemySpec#sine()} and its {@link EnemySpec.ChainSpec#headAttacks()}): its head point sways about
 * its route ({@link ChainSwim}); it never regrows: every cut splits off its rear part, which scatters ({@link
 * Mode#SCATTER}): it swims on straight along the cut's heading, then dies; its head fires its own attacks ({@link
 * HeadAttacks}). A head-first drop ({@link #headFirstDrop()}) is its wave's pickup that only a head-first kill drops.
 */
public final class Chain implements Hashed {
    /** History points kept: enough for the longest chain at the slowest sampling. */
    static final int HISTORY = 512;
    /** The most members a chain has: head, segments and tail. */
    static final int MAX_MEMBERS = 24;
    /** A prefilled history has a point every this many px. */
    private static final double PREFILL_STEP = 4;
    /** The prefilled history reaches this far behind the last member. */
    private static final double PREFILL_MARGIN = 64;
    /**
     * A headless body's speed is multiplied by this every step: at 180 px/s it drifts about 60 px
     * and comes to rest in about 1.5 s, so the bursts of its chained death happen in place, on the
     * play field (design/enemies/air/coilwyrm, Chained death).
     */
    private static final double DRIFT = 0.95;
    /** Below this speed, px/s, a drifting body is at rest. */
    private static final double STILL = 1;

    /** How the head point moves. */
    enum Mode {
        /** Along its path. */
        PATH,
        /** Straight on past its path's end (waiting for its loop-back, or leaving). */
        STRAIGHT,
        /** Holding still while its new head grows. */
        REGROW,
        /** Straight at where the ship was when its new head had grown. */
        LUNGE,
        /**
         * M8 part C: a water chain's cut-off rear part, swimming on straight along the cut's heading at its scatter's
         * speed until its time runs out, then dying ({@link EnemySpec.Scatter}).
         */
        SCATTER
    }

    private final double[] hx = new double[HISTORY];
    private final double[] hy = new double[HISTORY];
    /** The arc length flown at each history point. */
    private final double[] hs = new double[HISTORY];

    private int newest;
    private int count;

    private final Enemy[] members = new Enemy[MAX_MEMBERS];
    private final double[] offsets = new double[MAX_MEMBERS];
    private int size;

    private EnemySpec.ChainSpec spec;
    /** The kind of the head a cut part grows. */
    private int regrownKind;

    private int serial;
    private Mode mode;
    private FlightPath path;
    private int segment;
    private double distance;
    private double speed;
    private double vx;
    private double vy;
    private double headX;
    private double headY;
    /** The loop-back still to come; null when there is none (or it was flown). */
    private Spawn.Loop loop;
    /** Steps flown straight on past the path's end. */
    private int straightTicks;
    /** Whether it can still regrow (an uncut chain from a wave). */
    private boolean canRegrow;
    /** Whether it is a wave's chain rather than a regrown rear part. */
    private boolean original;
    /**
     * M7 part A: steps the EMP Burst still stops it whole (design/player/specials, EMP Burst); 0 when
     * it flies. Its regrowth waits meanwhile.
     */
    private int empStun;
    /** Whether a cut split it (or killed a part of it). */
    private boolean cut;

    private int regrowTicks;
    /** Members from this index on die one by one; {@link #size} when none does. */
    private int dyingFrom;
    /** Steps until the next of them pops. */
    private int popTicks;
    /** Whether any member has been on the play field (a regrown part or a loop-back counts as on). */
    private boolean entered;
    /** Whether its head is dead: the body drifts to a halt while it bursts. */
    private boolean drifting;
    /**
     * The edge it entered from last (design/player/wingmen: a wave's edge while it is active): its
     * wave's, the rear once it has looped back, its parent's for a rear part.
     */
    private WaveSpec.Entry entry = WaveSpec.Entry.FRONT;

    /**
     * M7 part C: whether it crawls the ground: its history and head point are then in ground space (x, row;
     * {@link Terrain#row}), {@link #segment} and {@link #distance} its crawl path's leg and the way along it.
     */
    private boolean ground;

    private EnemySpec.Crawl crawl;
    /** M7 part C: the ground's scroll now, by which a ground chain places its members. */
    private double scroll;
    /** M7 part C: whether no member has been destroyed yet (the head's first bonus). */
    private boolean intact;
    /** M7 part C: members from here on stopped where a cut left them (a ground chain's rear part); MAX when none. */
    private int stoppedFrom;

    private final double[] stoppedX = new double[MAX_MEMBERS];
    private final double[] stoppedRow = new double[MAX_MEMBERS];
    private final double[] stoppedFacing = new double[MAX_MEMBERS];
    /** M7 part C: the pore wave's clock, steps since the ground chain entered the play field; -1 before. */
    private int poreClock;
    /** M7 part C: the member whose pore fires in this step; -1 for none. */
    private int poreFiring;
    /** M7 part C: whether the last {@link #destroyed} cut a ground chain (stopping its rear part). */
    private boolean cutNow;

    /** M8 part C: its head point's sway and a rear part's scatter clock. */
    private final ChainSwim swim = new ChainSwim();
    /** M8 part C: its head's attacks' clocks. */
    private final HeadAttacks headAttacks = new HeadAttacks();
    /** M8 part C: whether its wave's pickup drops only when its head is destroyed first (not hashed: from the plan). */
    private boolean headFirstDrop;
    /**
     * M8 part H, step H2: whether a boss shed it (the Abyssal Maw's Eel Swarms): a kill ratio that leaves the bosses out
     * leaves it out too (not hashed: from the release).
     */
    private boolean bossShed;
    /**
     * M9 part C (design/enemies/air/rail-serpent): a machine split's rear part: powerless, slowing to a stop, its cars
     * blowing one by one and paying ({@link EnemySpec.Scatter#wreck()}).
     */
    private boolean wreck;
    /** M9 part C: a wreck's speed at the cut, and its steps left and in all of slowing to a stop. */
    private double wreckSpeed;

    private int wreckTicks;
    private int wreckTotal;

    /**
     * A wave's chain entering from {@code from}: the head point starts at its path's start, the
     * members strung out behind it.
     */
    void start(EnemySpec.ChainSpec chainSpec, int chainSerial, Spawn plan, int regrownHeadKind, WaveSpec.Entry from) {
        clear(chainSpec, chainSerial);
        entry = from;
        regrownKind = regrownHeadKind;
        original = true;
        // M8 part C: a chain whose cut parts scatter never regrows.
        canRegrow = chainSpec.scatter().isEmpty();
        mode = Mode.PATH;
        path = plan.path();
        speed = plan.speed();
        loop = plan.loop().orElse(null);
        if (plan.enemy().sine().isPresent()) {
            swim.sway(plan.enemy().sine().get(), speed);
        }
        headAttacks.start(chainSpec.headAttacks());
        headX = path.x(0, 0);
        headY = path.y(0, 0);
        size = spec.members();
        for (int i = 0; i < size; i++) {
            offsets[i] = spec.offsets().get(i);
        }
        prefill(path.dx(0), path.dy(0));
    }

    private void clear(EnemySpec.ChainSpec chainSpec, int chainSerial) {
        spec = chainSpec;
        serial = chainSerial;
        count = 0;
        newest = -1;
        size = 0;
        segment = 0;
        distance = 0;
        vx = 0;
        vy = 0;
        straightTicks = 0;
        loop = null;
        cut = false;
        regrowTicks = 0;
        dyingFrom = MAX_MEMBERS;
        popTicks = 0;
        entered = false;
        drifting = false;
        empStun = 0;
        entry = WaveSpec.Entry.FRONT;
        for (int i = 0; i < MAX_MEMBERS; i++) {
            members[i] = null;
        }
        ground = false;
        crawl = null;
        scroll = 0;
        intact = true;
        stoppedFrom = MAX_MEMBERS;
        poreClock = -1;
        poreFiring = -1;
        cutNow = false;
        swim.clear();
        headAttacks.clear();
        headFirstDrop = false;
        bossShed = false;
        wreck = false;
        wreckSpeed = 0;
        wreckTicks = 0;
        wreckTotal = 0;
    }

    /**
     * M7 part C: a wave's ground chain entering from {@code from} at the ground scroll {@code groundScroll}: the
     * head point starts at its crawl path's first point, the members strung out behind it along the path's
     * first leg (in ground space).
     */
    void startGround(
            EnemySpec.ChainSpec chainSpec,
            int chainSerial,
            EnemySpec.Crawl path,
            double groundScroll,
            WaveSpec.Entry from) {
        clear(chainSpec, chainSerial);
        ground = true;
        crawl = path;
        scroll = groundScroll;
        entry = from;
        regrownKind = -1;
        original = true;
        canRegrow = false;
        mode = Mode.PATH;
        speed = path.speed();
        headX = path.x(0);
        headY = path.row(0);
        size = spec.members();
        for (int i = 0; i < size; i++) {
            offsets[i] = spec.offsets().get(i);
        }
        prefill(path.x(1) - path.x(0), path.row(1) - path.row(0));
    }

    /** M7 part C: the head point crawls one step along its path (ground space), straight on past its end. */
    private void crawlStep() {
        double step = speed * SimStep.SECONDS;
        int last = crawl.points() - 1;
        while (true) {
            double dx = crawl.x(segment + 1) - crawl.x(segment);
            double dr = crawl.row(segment + 1) - crawl.row(segment);
            double length = Math.sqrt(dx * dx + dr * dr);
            if (distance + step < length) {
                distance += step;
                headX = crawl.x(segment) + dx / length * distance;
                headY = crawl.row(segment) + dr / length * distance;
                return;
            }
            step -= length - distance;
            if (segment + 1 >= last) {
                // Past the end: straight on along the last leg.
                distance = length;
                vx = dx / length * speed;
                vy = dr / length * speed;
                headX = crawl.x(last) + dx / length * step;
                headY = crawl.row(last) + dr / length * step;
                mode = Mode.STRAIGHT;
                straightTicks = 0;
                return;
            }
            segment++;
            distance = 0;
        }
    }

    /** A history straight back from the head point against the direction (dx, dy), long enough for every member. */
    private void prefill(double dx, double dy) {
        double length = Math.sqrt(dx * dx + dy * dy);
        double ux = length > 0 ? dx / length : 0;
        double uy = length > 0 ? dy / length : -1;
        double reach = offsets[Math.max(0, size - 1)] + PREFILL_MARGIN;
        count = 0;
        newest = -1;
        for (double back = reach; back > 0; back -= PREFILL_STEP) {
            push(headX - ux * back, headY - uy * back);
        }
        push(headX, headY);
    }

    private void push(double x, double y) {
        double s = 0;
        if (count > 0) {
            double dx = x - hx[newest];
            double dy = y - hy[newest];
            s = hs[newest] + Math.sqrt(dx * dx + dy * dy);
        }
        newest = (newest + 1) % HISTORY;
        hx[newest] = x;
        hy[newest] = y;
        hs[newest] = s;
        count = Math.min(count + 1, HISTORY);
    }

    /** Member {@code i} is {@code enemy}, linked by the caller. */
    void member(int i, Enemy enemy) {
        members[i] = enemy;
    }

    /**
     * One step: the head point flies (along its path, straight on, held while a head grows, or
     * lunging) and the members follow on its history. Returns false once it has flown its course
     * and no member is on the play field any more.
     */
    boolean advance(double groundScroll) {
        if (ground) {
            scroll = groundScroll;
            poreFiring = -1;
        }
        if (empStun > 0) {
            // M7 part A: stunned, it stops whole where it is; its members stay on their places.
            empStun--;
            place();
            return true;
        }
        if (wreck) {
            // M9 part C: a wreck slows evenly to a stop on its heading, then waits for its cars to blow.
            if (wreckTicks <= 0) {
                return place() || alive();
            }
            wreckTicks--;
            double share = (double) wreckTicks / wreckTotal;
            double was = speed;
            speed = wreckSpeed * share;
            if (was > 0) {
                vx *= speed / was;
                vy *= speed / was;
            }
        }
        if (mode == Mode.SCATTER && swim.scatterDue()) {
            // M8 part C: its swim is over: it dies from the cut backwards, drifting to a halt.
            dyingFrom = Math.min(dyingFrom, 1);
            popTicks = SimStep.ticks(spec.popSeconds());
            drift();
        }
        if (drifting) {
            speed *= DRIFT;
            vx *= DRIFT;
            vy *= DRIFT;
            if (speed < STILL && mode != Mode.REGROW) {
                // At rest: the members stay where they are until they burst.
                return place() || alive();
            }
        }
        boolean moved = true;
        switch (mode) {
            case PATH -> {
                if (ground) {
                    crawlStep();
                    break;
                }
                distance += speed * SimStep.SECONDS;
                if (distance >= path.length()) {
                    segment = path.segmentAt(path.length(), segment);
                    double dx = path.dx(segment);
                    double dy = path.dy(segment);
                    double length = Math.sqrt(dx * dx + dy * dy);
                    vx = length > 0 ? dx / length * speed : 0;
                    vy = length > 0 ? dy / length * speed : -speed;
                    double over = distance - path.length();
                    headX = path.x(segment, path.length()) + vx / speed * over;
                    headY = path.y(segment, path.length()) + vy / speed * over;
                    mode = Mode.STRAIGHT;
                    straightTicks = 0;
                } else {
                    segment = path.segmentAt(distance, segment);
                    headX = path.x(segment, distance);
                    headY = path.y(segment, distance);
                }
            }
            case STRAIGHT, LUNGE, SCATTER -> {
                headX += vx * SimStep.SECONDS;
                headY += vy * SimStep.SECONDS;
                straightTicks++;
                if (loop != null && straightTicks >= SimStep.ticks(loop.gapSeconds())) {
                    reenter();
                    moved = false;
                }
            }
            case REGROW -> moved = false;
        }
        if (moved) {
            if (swim.swaying() && (mode == Mode.PATH || mode == Mode.STRAIGHT)) {
                // M8 part C: the history takes the swaying head point; headX/headY stay on the route.
                swim.step(mode == Mode.PATH ? path.dx(segment) : vx, mode == Mode.PATH ? path.dy(segment) : vy, speed);
                push(headX + swim.offsetX(), headY + swim.offsetY());
            } else {
                push(headX, headY);
            }
        }
        boolean onField = place();
        entered |= onField;
        if (ground) {
            pores();
            // Once in, it is done when no member is on the play field; before, while it can still scroll in.
            return onField || (!entered && Terrain.y(scroll, headY) > 0);
        }
        if (onField || mode == Mode.PATH || mode == Mode.REGROW || loop != null) {
            return true;
        }
        if (wreck && alive()) {
            // M9 part C: a wreck lives until its last car blew, on the field or off it.
            return true;
        }
        if (mode == Mode.SCATTER && swim.scatterLeft() > 0) {
            // M8 part C: a scattering part lives out its swim, its members above an edge still coming on.
            return true;
        }
        return !entered && straightTicks < SimStep.ticks(1);
    }

    /** M7 part C: the pore wave's clock runs from the entry on and picks the pore that fires now (if any). */
    private void pores() {
        if (poreClock < 0 && entered) {
            poreClock = 0;
        }
        if (poreClock < 0 || drifting) {
            return;
        }
        int k = poreAt(poreClock % poreCycle());
        poreFiring = k < 0 ? -1 : poreMember(k);
        poreClock++;
    }

    /** M7 part C: the number of pores in a wave: every segment's, or every other one's. */
    private int poreCount() {
        int segments = spec.members() - 2;
        return crawl.pores().everyOther() ? (segments + 1) / 2 : segments;
    }

    /** M7 part C: the member of the wave's {@code k}-th pore. */
    private int poreMember(int k) {
        return crawl.pores().everyOther() ? 1 + 2 * k : 1 + k;
    }

    /** M7 part C: one wave and its rest, steps: the first glow, the pores' spacing and the rest. */
    private int poreCycle() {
        EnemySpec.PoreWave wave = crawl.pores();
        int cycle = SimStep.ticks(wave.glow())
                + Math.max(0, poreCount() - 1) * SimStep.ticks(wave.interval())
                + SimStep.ticks(wave.rest());
        return Math.max(1, cycle);
    }

    /** M7 part C: the pore that fires at step {@code c} of a wave's cycle; -1 for none. */
    private int poreAt(int c) {
        int t = c - SimStep.ticks(crawl.pores().glow());
        int interval = Math.max(1, SimStep.ticks(crawl.pores().interval()));
        if (t < 0 || t % interval != 0) {
            return -1;
        }
        int k = t / interval;
        return k < poreCount() ? k : -1;
    }

    /** The loop-back: the head point re-enters on its second path, the members strung out behind it. */
    private void reenter() {
        path = loop.path();
        loop = null;
        entry = WaveSpec.Entry.REAR;
        mode = Mode.PATH;
        segment = 0;
        distance = 0;
        headX = path.x(0, 0);
        headY = path.y(0, 0);
        prefill(path.dx(0), path.dy(0));
    }

    /** Places the members on the history where they enter, without an in-between position. */
    void settle() {
        place();
        for (int i = 0; i < size; i++) {
            if (members[i] != null) {
                members[i].chainTo(members[i].x(), members[i].frameY(), members[i].frameFacing());
            }
        }
    }

    /** Places the members on the history; returns whether any of them is on the play field. */
    private boolean place() {
        boolean onField = false;
        int at = newest;
        int walked = 0;
        double headS = hs[newest];
        for (int i = 0; i < size; i++) {
            double target = headS - offsets[i];
            while (walked < count - 1 && hs[at] > target) {
                at = (at - 1 + HISTORY) % HISTORY;
                walked++;
            }
            // Between the point at (older) and the next newer one.
            int newer = walked == 0 ? at : (at + 1) % HISTORY;
            double x;
            double y;
            if (newer == at || hs[newer] == hs[at]) {
                x = hx[at];
                y = hy[at];
            } else {
                double t = Math.clamp((target - hs[at]) / (hs[newer] - hs[at]), 0, 1);
                x = hx[at] + (hx[newer] - hx[at]) * t;
                y = hy[at] + (hy[newer] - hy[at]) * t;
            }
            Enemy member = members[i];
            if (member == null) {
                continue;
            }
            int older = walked == 0 ? (at - 1 + HISTORY) % HISTORY : at;
            int front = walked == 0 ? at : newer;
            double dx = hx[front] - hx[older];
            double dy = hy[front] - hy[older];
            double heading = dx == 0 && dy == 0 ? member.frameFacing() : StrictMath.atan2(-dx, -dy);
            if (ground) {
                // M7 part C: the history is in ground space; a stopped member stays where the cut left it.
                if (i >= stoppedFrom) {
                    x = stoppedX[i];
                    y = stoppedRow[i];
                    heading = stoppedFacing[i];
                }
                y = Terrain.y(scroll, y);
            }
            member.chainTo(x, y, heading);
            onField |= PlayField.overlaps(x, y, member.hitbox());
        }
        return onField;
    }

    /**
     * Member {@code i} was destroyed: it leaves the chain. The head's death lets the whole chain die
     * from the front; a segment's cut either splits off the rear part (returned, to grow its new
     * head: the caller fills {@code rear} from the pool, null when none is free) or lets the
     * members behind the cut die.
     *
     * @return whether the rear part was split off into {@code rear}
     */
    boolean destroyed(int i, Chain rear, int rearSerial) {
        members[i] = null;
        intact = false;
        if (ground) {
            groundCut(i);
            return false;
        }
        if (i == 0) {
            canRegrow = false;
            dyingFrom = Math.min(dyingFrom, 1);
            drift();
            popTicks = popTicks > 0 ? popTicks : SimStep.ticks(spec.popSeconds());
            return false;
        }
        boolean behind = false;
        for (int k = i + 1; k < size; k++) {
            behind |= members[k] != null;
        }
        if (!behind) {
            size = i;
            dyingFrom = Math.min(dyingFrom, size);
            return false;
        }
        if (mode == Mode.SCATTER) {
            // M8 part C: a scattering part's cut only takes that member out of it.
            return false;
        }
        if (spec.scatter().isPresent() && dyingFrom >= size && rear != null) {
            scatter(i, rear, rearSerial);
            return true;
        }
        if (canRegrow && dyingFrom >= size && rear != null) {
            split(i, rear, rearSerial);
            return true;
        }
        cut = true;
        canRegrow = false;
        if (i + 1 < dyingFrom) {
            dyingFrom = i + 1;
            popTicks = SimStep.ticks(spec.popSeconds());
        }
        return false;
    }

    /**
     * M7 part C: member {@code i} of a ground chain was destroyed: the head's death lets the whole body die from
     * the front (it slows to a halt); a cut stops the members behind it where they are, and they die from the
     * cut backwards after the crawl's cut delay ({@link #takeCut}); the tail's leaves the rest crawling on.
     */
    private void groundCut(int i) {
        if (i == 0) {
            dyingFrom = Math.min(dyingFrom, 1);
            drift();
            popTicks = popTicks > 0 ? popTicks : SimStep.ticks(spec.popSeconds());
            return;
        }
        boolean behind = false;
        for (int k = i + 1; k < size; k++) {
            behind |= members[k] != null;
        }
        if (!behind) {
            size = i;
            dyingFrom = Math.min(dyingFrom, size);
            return;
        }
        cut = true;
        if (i + 1 < dyingFrom) {
            dyingFrom = i + 1;
            popTicks = Math.max(1, SimStep.ticks(crawl.cutSeconds()));
            for (int k = i + 1; k < Math.min(size, stoppedFrom); k++) {
                if (members[k] != null) {
                    stoppedX[k] = members[k].x();
                    stoppedRow[k] = Terrain.row(scroll, members[k].y());
                    stoppedFacing[k] = members[k].facing();
                }
            }
            stoppedFrom = Math.min(stoppedFrom, i + 1);
            cutNow = true;
        }
    }

    /** M7 part C: whether the last {@link #destroyed} cut a ground chain; clears it. */
    boolean takeCut() {
        boolean was = cutNow;
        cutNow = false;
        return was;
    }

    /** The head is dead: no loop-back, and the body drifts to a halt while it bursts. */
    private void drift() {
        drifting = true;
        loop = null;
    }

    /** The members behind the cut at {@code i} become {@code rear}, which grows a new head at the cut. */
    private void split(int i, Chain rear, int rearSerial) {
        rear.clear(spec, rearSerial);
        rear.regrownKind = regrownKind;
        rear.mode = Mode.REGROW;
        rear.regrowTicks = SimStep.ticks(spec.regrowSeconds());
        rear.speed = spec.regrowSpeed();
        rear.entered = true;
        rear.entry = entry;
        // M7 part A: a stunned chain's rear part stays stunned with its members.
        rear.empStun = empStun;
        // The rear part's history: this one's, up to the cut point.
        double cutS = hs[newest] - offsets[i];
        int at = newest;
        int walked = 0;
        while (walked < count - 1 && hs[at] > cutS) {
            at = (at - 1 + HISTORY) % HISTORY;
            walked++;
        }
        int oldest = (newest - count + 1 + HISTORY) % HISTORY;
        for (int k = 0, p = oldest; k < count - walked; k++, p = (p + 1) % HISTORY) {
            rear.push(hx[p], hy[p]);
        }
        rear.headX = cutX(i);
        rear.headY = cutY(i);
        rear.push(rear.headX, rear.headY);
        rear.size = size - i;
        rear.offsets[0] = 0;
        for (int k = i + 1; k < size; k++) {
            int index = k - i;
            rear.members[index] = members[k];
            rear.offsets[index] = offsets[k] - offsets[i];
            if (members[k] != null) {
                members[k].relink(rear, index);
            }
            members[k] = null;
        }
        size = i;
        cut = true;
        canRegrow = false;
        dyingFrom = Math.min(dyingFrom, size);
    }

    /**
     * M8 part C: the members behind the cut at {@code i} become {@code rear}, which swims on from the cut along the
     * heading the history has there at its scatter's speed ({@link Mode#SCATTER}).
     */
    private void scatter(int i, Chain rear, int rearSerial) {
        EnemySpec.Scatter cutSpec = spec.scatter().orElseThrow();
        double cutS = hs[newest] - offsets[i];
        double dx = pointAt(cutS + 1, true) - pointAt(cutS - 1, true);
        double dy = pointAt(cutS + 1, false) - pointAt(cutS - 1, false);
        double length = Math.sqrt(dx * dx + dy * dy);
        split(i, rear, rearSerial);
        rear.mode = Mode.SCATTER;
        rear.regrowTicks = 0;
        rear.speed = cutSpec.speed();
        rear.vx = length > 0 ? dx / length * cutSpec.speed() : 0;
        rear.vy = length > 0 ? dy / length * cutSpec.speed() : -cutSpec.speed();
        if (cutSpec.wreck()) {
            // M9 part C: the machine split: powerless at once, its cars doomed until they blow from the cut backwards.
            rear.wreck = true;
            rear.wreckSpeed = cutSpec.speed();
            rear.wreckTotal = Math.max(1, SimStep.ticks(cutSpec.seconds()));
            rear.wreckTicks = rear.wreckTotal;
            rear.loop = null;
            rear.dyingFrom = 1;
            rear.popTicks = rear.wreckTotal;
            return;
        }
        rear.swim.scatter(cutSpec.seconds());
    }

    /** Where member {@code i} sits on the history: the cut point when it is destroyed. */
    private double cutX(int i) {
        return pointAt(hs[newest] - offsets[i], true);
    }

    private double cutY(int i) {
        return pointAt(hs[newest] - offsets[i], false);
    }

    /** The history's x (or y) at arc length {@code s}. */
    private double pointAt(double s, boolean wantX) {
        int at = newest;
        int walked = 0;
        while (walked < count - 1 && hs[at] > s) {
            at = (at - 1 + HISTORY) % HISTORY;
            walked++;
        }
        int newer = walked == 0 ? at : (at + 1) % HISTORY;
        double[] values = wantX ? hx : hy;
        if (newer == at || hs[newer] == hs[at]) {
            return values[at];
        }
        double t = Math.clamp((s - hs[at]) / (hs[newer] - hs[at]), 0, 1);
        return values[at] + (values[newer] - values[at]) * t;
    }

    /**
     * Counts down the regrowth; returns true in the step its new head has grown: the caller links
     * the head as member 0 ({@link #grown}) or, when the rear part died meanwhile, lets it end.
     */
    boolean regrowDue() {
        return mode == Mode.REGROW && empStun == 0 && --regrowTicks <= 0;
    }

    /**
     * M7 part A, the EMP Burst: the whole chain stops for {@code ticks} steps (restarting a running
     * stun), each member stunned with it ({@link Enemy#empStun}); a head grown meanwhile flies at once.
     */
    void empStun(int ticks) {
        empStun = ticks;
        for (int i = 0; i < size; i++) {
            if (members[i] != null) {
                members[i].empStun(ticks);
            }
        }
    }

    /** M7 part A: whether the EMP Burst stops it now. */
    public boolean empStunned() {
        return empStun > 0;
    }

    /** The new head has grown (or not, {@code head} null): it lunges at (shipX, shipY). */
    void grown(Enemy head, double shipX, double shipY) {
        members[0] = head;
        double dx = shipX - headX;
        double dy = shipY - headY;
        double length = Math.sqrt(dx * dx + dy * dy);
        vx = length > 0 ? dx / length * speed : 0;
        vy = length > 0 ? dy / length * speed : -speed;
        mode = Mode.LUNGE;
        straightTicks = 0;
        if (head == null) {
            canRegrow = false;
            dyingFrom = Math.min(dyingFrom, 1);
            drift();
            popTicks = SimStep.ticks(spec.popSeconds());
        }
    }

    /**
     * Counts down the next pop of the dying members; returns the member to pop now (the first alive
     * one from the cut backwards), or null.
     */
    Enemy popDue() {
        if (dyingFrom >= size) {
            return null;
        }
        if (--popTicks > 0) {
            return null;
        }
        popTicks = SimStep.ticks(spec.popSeconds());
        for (int k = dyingFrom; k < size; k++) {
            if (members[k] != null) {
                Enemy member = members[k];
                members[k] = null;
                dyingFrom = k + 1;
                return member;
            }
        }
        dyingFrom = size;
        return null;
    }

    /**
     * Whether {@code enemy} is a chain member waiting for its burst in a chained death: it flies on
     * with the chain and is drawn intact, but nothing hits it, it rams nothing and it pays nothing
     * until it bursts (design/enemies/air/coilwyrm, Chained death).
     */
    public static boolean doomed(Enemy enemy) {
        Chain chain = enemy.chain();
        return chain != null && enemy.link() >= chain.dyingFrom && enemy.link() < chain.size;
    }

    /** Whether any member is alive. */
    public boolean alive() {
        for (int k = 0; k < size; k++) {
            if (members[k] != null) {
                return true;
            }
        }
        return false;
    }

    /** The kind of the head a cut part of it grows. */
    public int regrownKind() {
        return regrownKind;
    }

    /** Whether its regrowth is under way: the rear part holds while its head grows. */
    public boolean regrowing() {
        return mode == Mode.REGROW;
    }

    /** The regrowth's progress, 0 to 1 (the new head's scale while it grows). */
    public double regrowth() {
        int total = SimStep.ticks(spec.regrowSeconds());
        return mode == Mode.REGROW ? 1 - (double) Math.max(0, regrowTicks) / total : 1;
    }

    /** The head point: where a new head grows. */
    public double headX() {
        return headX;
    }

    public double headY() {
        return ground ? Terrain.y(scroll, headY) : headY;
    }

    /** The head point's heading, radians clockwise from straight down (a growing head faces it). */
    public double headFacing() {
        int older = (newest - 1 + HISTORY) % HISTORY;
        double dx = hx[newest] - hx[older];
        double dy = hy[newest] - hy[older];
        return count < 2 || (dx == 0 && dy == 0) ? 0 : StrictMath.atan2(-dx, -dy);
    }

    /** Whether destroying member {@code i} now pays the first bonus: the tail of an uncut wave's chain. */
    boolean firstBonusDue(int i) {
        if (ground || spec.scatter().isPresent()) {
            // M7 part C: a ground chain never regrows (M8 part C: nor a water chain): the tail destroyed before any
            // other part.
            return original && intact && i > 0 && i == size - 1 && i == spec.members() - 1;
        }
        return original && !cut && canRegrow && i == size - 1 && i == spec.members() - 1;
    }

    /**
     * M7 part C: whether destroying the head now pays its {@link EnemySpec.ChainSpec#headFirstBonus}: a wave's
     * chain's head destroyed before any other part (call before {@link #destroyed}).
     */
    boolean headFirstBonusDue() {
        return original && intact && members[0] != null;
    }

    /** M7 part C: whether it crawls the ground (a Threadcrawler) rather than flying (the Coilwyrm). */
    public boolean ground() {
        return ground;
    }

    /** M7 part C: whether member {@code i} is stopped where a cut left it (writhing until it dies). */
    public boolean stopped(int i) {
        return ground && i >= stoppedFrom && i < size;
    }

    /** M7 part C: the member whose pore fires in this step; -1 for none (and for an air chain). */
    int poreFiring() {
        return poreFiring;
    }

    /** M7 part C: the crawl of a ground chain; null for an air chain. */
    EnemySpec.Crawl crawl() {
        return crawl;
    }

    /**
     * M7 part C: how brightly member {@code i}'s pore glows now, 0 to 1 over its glow before it fires (the
     * tell); 0 for a member without a pore in the wave, before the chain entered and for an air chain.
     */
    public double poreGlow(int i) {
        if (!ground || poreClock <= 0 || drifting || i < 1 || i > spec.members() - 2) {
            return 0;
        }
        boolean everyOther = crawl.pores().everyOther();
        if (everyOther && (i - 1) % 2 != 0) {
            return 0;
        }
        int k = everyOther ? (i - 1) / 2 : i - 1;
        int glow = SimStep.ticks(crawl.pores().glow());
        int fires = glow + k * Math.max(1, SimStep.ticks(crawl.pores().interval()));
        // The clock's value in this step (it has moved on since).
        int t = (poreClock - 1) % poreCycle() - (fires - glow);
        if (t < 0 || t > glow) {
            return 0;
        }
        return glow == 0 ? 1 : (double) t / glow;
    }

    /** M9 part C: whether it is a machine split's wreck (its cars blow one by one, each paying its bounty). */
    public boolean wreck() {
        return wreck;
    }

    /** M8 part C: whether it is a water chain's cut-off rear part, swimming on before it dies. */
    public boolean scattering() {
        return mode == Mode.SCATTER;
    }

    /** M8 part C: the steps a scattering part still swims before it dies; 0 once it dies (and for any other). */
    public int scatterLeft() {
        return swim.scatterLeft();
    }

    /** M8 part C: its head's attacks' clocks. */
    HeadAttacks headAttacks() {
        return headAttacks;
    }

    /** M8 part C: its wave's pickup drops only when its head is destroyed before any other part. */
    void headFirstDrop(boolean only) {
        headFirstDrop = only;
    }

    /** M8 part C: whether its wave's pickup drops only when its head is destroyed before any other part. */
    boolean headFirstDrop() {
        return headFirstDrop;
    }

    /** M8 part H, step H2: it is a boss's shed unit (released by the boss, not a wave). */
    void bossShed(boolean shed) {
        bossShed = shed;
    }

    /** M8 part H, step H2: whether a boss shed it (a kill ratio that leaves the bosses out leaves it out too). */
    boolean bossShed() {
        return bossShed;
    }

    /** The edge it entered from last: its wave's, or the rear after its loop-back. */
    WaveSpec.Entry entry() {
        return entry;
    }

    /** Whether it is a wave's chain (its head is the unit that counts as the kill). */
    boolean original() {
        return original;
    }

    /** The member slots, head first; a slot of a destroyed member is empty. */
    public int size() {
        return size;
    }

    /** Member {@code i}, or null when it was destroyed. */
    public Enemy member(int i) {
        return members[i];
    }

    /** Unique among the chains of an attempt. */
    int serial() {
        return serial;
    }

    public EnemySpec.ChainSpec spec() {
        return spec;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(serial)
                .add(mode.ordinal())
                .add(segment)
                .add(distance)
                .add(speed)
                .add(vx)
                .add(vy)
                .add(headX)
                .add(headY)
                .add(loop == null ? 0 : 1)
                .add(straightTicks)
                .add(canRegrow ? 1 : 0)
                .add(original ? 1 : 0)
                .add(cut ? 1 : 0)
                .add(regrowTicks)
                .add(dyingFrom)
                .add(popTicks)
                .add(entered ? 1 : 0)
                .add(drifting ? 1 : 0)
                .add(size)
                .add(count)
                .add(newest);
        for (int k = 0; k < size; k++) {
            hash.add(members[k] == null ? -1 : members[k].serial()).add(offsets[k]);
        }
        for (int k = 0, p = newest; k < count; k++, p = (p - 1 + HISTORY) % HISTORY) {
            hash.add(hx[p]).add(hy[p]).add(hs[p]);
        }
        if (empStun != 0) {
            // M7 part A: only while the EMP Burst stops it, so other runs hash as before.
            hash.add(empStun);
        }
        if (ground) {
            // M7 part C: only a ground chain's, so the Coilwyrm hashes as before.
            hash.add(intact ? 1 : 0).add(stoppedFrom).add(poreClock).add(poreFiring);
            for (int k = stoppedFrom; k < size; k++) {
                hash.add(stoppedX[k]).add(stoppedRow[k]);
            }
        } else if (spec.headFirstBonus() > 0) {
            hash.add(intact ? 1 : 0);
        }
        if (wreck) {
            // M9 part C: only a wreck's.
            hash.add(wreckSpeed).add(wreckTicks).add(wreckTotal);
        }
        // M8 part C: only a water chain's, so the Coilwyrm and the Threadcrawler hash as before.
        swim.addTo(hash);
        headAttacks.addTo(hash);
    }
}
