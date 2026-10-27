package vanguard.sim;

import java.util.List;

/**
 * Round 44 (the user in round 43, W.a and W.e; design/enemies/bosses/revenant-walker, Attacks): a walking boss's rail
 * lance and stomp shockwave, stepped by its {@link StrideArena} after its flame cone.
 *
 * <p><b>The lance</b> ({@link BossSpec.Lance}) wakes once its listed parts (the pods) are all destroyed, or from its
 * phase on. While the walker walks on the screen it charges every interval: the line runs from its lance (on its back)
 * through where the ship was as the charge began, glowing over the charge; then the shot runs along that line for its
 * fire time and hits the ship and Rook once a shot each where it touches them. The line pivots on that point as the
 * walker walks on, so the ship that stays put is hit at any range; the one that moves is not.
 *
 * <p><b>The stomp</b> ({@link BossSpec.Stomp}): the ship or Rook within its reach of the walker's centre starts a tell
 * (at most once per cooldown); then a ring expands from where the walker stands to its radius and hits the ship and
 * Rook once a stomp each as it reaches them.
 *
 * <p>Everything is allocated here; stepping does not allocate. Its state is hashed with its walker.
 */
public final class StrideStrikes implements Hashed {
    /** Where the lance is in its cycle. */
    public enum LancePhase {
        /** Cold (or asleep: its pods live and its phase has not come). */
        IDLE,
        /** Charging: the line glows; it does not hurt yet. */
        CHARGE,
        /** Firing: the shot runs along the line and hurts once a shot each. */
        FIRE
    }

    /** Where the stomp is in its cycle. */
    public enum StompPhase {
        /** Not stomping. */
        IDLE,
        /** The legs brace; a ring marks the ground it will hit. */
        TELL,
        /** The ring expands from where it stomped. */
        RING
    }

    /** What a strike hit: the ship. */
    static final int SHIP = 1;
    /** ... Rook. */
    static final int WINGMAN = 2;
    /** How far the lance's line runs from its origin, px (past every edge of the play field). */
    public static final double LANCE_REACH = 1000;
    /** The lance's first charge comes this long after it wakes, s. */
    static final double WAKE_SECONDS = 1;

    private final StrideArena arena;
    private final SetPiece piece;
    private final int phases;
    /** Null without one. */
    private final BossSpec.Lance lance;

    private final BossSpec.Stomp stomp;
    private final int chargeTicks;
    private final int fireTicks;
    private final int intervalTicks;
    private final int lastIntervalTicks;
    private final int wakeTicks;
    private final int stompTellTicks;
    private final int expandTicks;
    private final int stompCooldownTicks;

    private boolean awake;
    private LancePhase lancePhase;
    private int lanceTicks;
    /** Steps since the last charge started. */
    private int lanceSince;
    /** The point the line runs through (where the ship was as the charge began). */
    private double targetX;

    private double targetY;
    private int lanceHits;

    private StompPhase stompPhase;
    private int stompTicks;
    /** Steps since the last tell started. */
    private int stompSince;
    /** Where the ring expands from. */
    private double ringX;

    private double ringY;
    private int stompHits;

    StrideStrikes(StrideArena arena, SetPiece piece, BossSpec.Stride spec, int phases) {
        this.arena = arena;
        this.piece = piece;
        this.phases = phases;
        lance = spec.lance().orElse(null);
        stomp = spec.stomp().orElse(null);
        chargeTicks = lance == null ? 0 : Math.max(1, SimStep.ticks(lance.chargeSeconds()));
        fireTicks = lance == null ? 0 : Math.max(1, SimStep.ticks(lance.fireSeconds()));
        intervalTicks = lance == null ? 0 : SimStep.ticks(lance.intervalSeconds());
        lastIntervalTicks = lance == null ? 0 : SimStep.ticks(lance.lastIntervalSeconds());
        wakeTicks = SimStep.ticks(WAKE_SECONDS);
        stompTellTicks = stomp == null ? 0 : SimStep.ticks(stomp.tellSeconds());
        expandTicks = stomp == null ? 0 : Math.max(1, SimStep.ticks(stomp.expandSeconds()));
        stompCooldownTicks = stomp == null ? 0 : SimStep.ticks(stomp.cooldownSeconds());
        reset();
    }

    /** Back to the level start (or a retry): the lance asleep, no stomp. */
    void reset() {
        awake = false;
        lancePhase = LancePhase.IDLE;
        lanceTicks = 0;
        lanceSince = 0;
        targetX = 0;
        targetY = 0;
        lanceHits = 0;
        stompPhase = StompPhase.IDLE;
        stompTicks = 0;
        stompSince = Integer.MAX_VALUE / 2;
        ringX = 0;
        ringY = 0;
        stompHits = 0;
    }

    /** It was destroyed: the lance and the stomp stop. */
    void died() {
        lancePhase = LancePhase.IDLE;
        lanceTicks = 0;
        stompPhase = StompPhase.IDLE;
        stompTicks = 0;
    }

    /**
     * One step in {@code phase} after the cone: {@code ready}, it walks on the screen with its hull on it and the
     * level's fire runs (a charge or a tell left unready stops).
     */
    void act(int phase, BossActions actions, boolean ready) {
        if (lance != null) {
            stepLance(phase, actions, ready);
        }
        if (stomp != null) {
            stepStomp(actions, ready);
        }
    }

    /** Whether the lance is awake in {@code phase}: its parts all destroyed, or its phase come. */
    boolean lanceAwake(int phase) {
        if (lance == null) {
            return false;
        }
        if (phase >= lance.fromPhase()) {
            return true;
        }
        List<Integer> parts = lance.afterParts();
        if (parts.isEmpty()) {
            return false;
        }
        for (int i = 0; i < parts.size(); i++) {
            if (!piece.partWrecked(parts.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void stepLance(int phase, BossActions actions, boolean ready) {
        if (!lanceAwake(phase)) {
            awake = false;
            lancePhase = LancePhase.IDLE;
            lanceTicks = 0;
            return;
        }
        int interval = phase == phases - 1 ? lastIntervalTicks : intervalTicks;
        if (!awake) {
            awake = true;
            lanceSince = Math.max(0, interval - wakeTicks);
        }
        if (lanceSince < Integer.MAX_VALUE / 2) {
            lanceSince++;
        }
        switch (lancePhase) {
            case IDLE -> {
                if (ready && lanceSince >= interval) {
                    lancePhase = LancePhase.CHARGE;
                    lanceTicks = 0;
                    lanceSince = 0;
                    targetX = arena.shipX();
                    targetY = arena.shipY();
                    actions.lanceCharge(lanceX(), lanceY());
                }
            }
            case CHARGE -> {
                if (!ready) {
                    lancePhase = LancePhase.IDLE;
                    lanceTicks = 0;
                } else if (++lanceTicks >= chargeTicks) {
                    lancePhase = LancePhase.FIRE;
                    lanceTicks = 0;
                    lanceHits = 0;
                    actions.lanceFire(lanceX(), lanceY());
                }
            }
            case FIRE -> {
                if (++lanceTicks >= fireTicks || !ready) {
                    lancePhase = LancePhase.IDLE;
                    lanceTicks = 0;
                }
            }
        }
    }

    private void stepStomp(BossActions actions, boolean ready) {
        if (stompSince < Integer.MAX_VALUE / 2) {
            stompSince++;
        }
        switch (stompPhase) {
            case IDLE -> {
                if (ready
                        && stompSince >= stompCooldownTicks
                        && (near(arena.shipX(), arena.shipY())
                                || (arena.rookIn() && near(arena.rookX(), arena.rookY())))) {
                    stompPhase = StompPhase.TELL;
                    stompTicks = 0;
                    stompSince = 0;
                    actions.stompTell(arena.x(), arena.y());
                }
            }
            case TELL -> {
                if (!ready) {
                    stompPhase = StompPhase.IDLE;
                    stompTicks = 0;
                } else if (++stompTicks >= stompTellTicks) {
                    stompPhase = StompPhase.RING;
                    stompTicks = 0;
                    stompHits = 0;
                    ringX = arena.x();
                    ringY = arena.y();
                    actions.stomp(ringX, ringY);
                }
            }
            case RING -> {
                if (++stompTicks >= expandTicks) {
                    stompPhase = StompPhase.IDLE;
                    stompTicks = 0;
                }
            }
        }
    }

    private boolean near(double px, double py) {
        double dx = px - arena.x();
        double dy = py - arena.y();
        return dx * dx + dy * dy <= stomp.reach() * stomp.reach();
    }

    /** Whether the firing lance's line touches {@code hull} at (hx, hy) (whatever it hit before). */
    boolean lanceTouches(Hull hull, double hx, double hy) {
        if (lancePhase != LancePhase.FIRE) {
            return false;
        }
        double ox = lanceX();
        double oy = lanceY();
        return hull.touchesSegment(
                hx, hy, ox, oy, ox + lanceDirX() * LANCE_REACH, oy + lanceDirY() * LANCE_REACH, lance.width() / 2);
    }

    /** Whether the expanding ring has reached (px, py). */
    boolean stompReaches(double px, double py) {
        if (stompPhase != StompPhase.RING) {
            return false;
        }
        double dx = px - ringX;
        double dy = py - ringY;
        double r = ringRadius();
        return dx * dx + dy * dy <= r * r;
    }

    /** Whether the current shot hit {@code what} ({@link #SHIP}, {@link #WINGMAN}) already. */
    boolean lanceHit(int what) {
        return (lanceHits & what) != 0;
    }

    void markLanceHit(int what) {
        lanceHits |= what;
    }

    /** Whether the current stomp hit {@code what} already. */
    boolean stompHit(int what) {
        return (stompHits & what) != 0;
    }

    void markStompHit(int what) {
        stompHits |= what;
    }

    /** The lance's damage (0 without one). */
    double lanceDamage() {
        return lance == null ? 0 : lance.damage();
    }

    /** The stomp's damage (0 without one). */
    double stompDamage() {
        return stomp == null ? 0 : stomp.damage();
    }

    // --- the presentation's view ---------------------------------------------------------------

    /** Whether it has a rail lance. */
    public boolean hasLance() {
        return lance != null;
    }

    /** Whether it stomps. */
    public boolean hasStomp() {
        return stomp != null;
    }

    /** Where the lance is in its cycle. */
    public LancePhase lancePhase() {
        return lancePhase;
    }

    /** How far the charge or the shot has run, 0–1 (0 when cold). */
    public double lanceShare() {
        return switch (lancePhase) {
            case IDLE -> 0;
            case CHARGE -> Math.min(1, (double) lanceTicks / chargeTicks);
            case FIRE -> Math.min(1, (double) lanceTicks / fireTicks);
        };
    }

    /** The lance's origin on its back, px from the left. */
    public double lanceX() {
        return arena.x() + (lance == null ? 0 : arena.turnX(0, lance.originBehind()));
    }

    /** ... px up. */
    public double lanceY() {
        return arena.y() + (lance == null ? 0 : arena.turnY(0, lance.originBehind()));
    }

    /** The line's direction from the origin through its point, x (a unit vector; straight down when on it). */
    public double lanceDirX() {
        double dx = targetX - lanceX();
        double dy = targetY - lanceY();
        double d = Math.sqrt(dx * dx + dy * dy);
        return d < 1 ? 0 : dx / d;
    }

    /** ... y. */
    public double lanceDirY() {
        double dx = targetX - lanceX();
        double dy = targetY - lanceY();
        double d = Math.sqrt(dx * dx + dy * dy);
        return d < 1 ? -1 : dy / d;
    }

    /** The lance's line width, px (0 without one). */
    public double lanceWidth() {
        return lance == null ? 0 : lance.width();
    }

    /** Where the stomp is in its cycle. */
    public StompPhase stompPhase() {
        return stompPhase;
    }

    /** How far the tell or the ring has run, 0–1 (0 when not stomping). */
    public double stompShare() {
        return switch (stompPhase) {
            case IDLE -> 0;
            case TELL -> Math.min(1, (double) stompTicks / Math.max(1, stompTellTicks));
            case RING -> Math.min(1, (stompTicks + 1.0) / expandTicks);
        };
    }

    /** The ring's centre (in a tell: the walker's centre, where it will stomp), px from the left. */
    public double ringX() {
        return stompPhase == StompPhase.RING ? ringX : arena.x();
    }

    /** ... px up. */
    public double ringY() {
        return stompPhase == StompPhase.RING ? ringY : arena.y();
    }

    /** The expanding ring's radius now, px (0 when not expanding). */
    public double ringRadius() {
        return stompPhase == StompPhase.RING ? stomp.radius() * Math.min(1, (stompTicks + 1.0) / expandTicks) : 0;
    }

    /** The stomp's full radius, px (0 without one): the tell's ground mark. */
    public double stompRadius() {
        return stomp == null ? 0 : stomp.radius();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(awake ? 1 : 0)
                .add(lancePhase.ordinal())
                .add(lanceTicks)
                .add(lanceSince)
                .add(targetX)
                .add(targetY)
                .add(lanceHits)
                .add(stompPhase.ordinal())
                .add(stompTicks)
                .add(stompSince)
                .add(ringX)
                .add(ringY)
                .add(stompHits);
    }
}
