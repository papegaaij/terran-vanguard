package vanguard.sim;

/**
 * M10 part I, step I1 (design/player/wingmen, Rook in Act 6: his weave through the fire): Rook's dodge from Act 6 on.
 * Measured on Levels 38-42 (medium and hard, 3 seeds), plain aimed fire took his armour, not the storm's hazards: his
 * Act 2 dodge decides one bullet at a time and stops looking while a sidestep waits out his reaction delay, his slot is
 * dragged through the bullets by the ship's jinks (the Mk IV+ outruns his 250 px/s), so most bullets that hit him were
 * seen late or never. Here:
 *
 * <ul>
 *   <li>His slot hangs off where he takes the ship to be: its position smoothed over {@link #SMOOTH_SECONDS}.
 *   <li>Every dodge interval he decides <b>every</b> bullet that would pass within his clearance of him where he is
 *       over the look-ahead, each once, with his reaction share (the bullets he misses he ignores, as before); one he
 *       reacts to has him plan his way after his reaction delay.
 *   <li>A bullet not at him where he is but on his way to his goal (one he missed aside) has him plan his way at once:
 *       he does not fly into fire.
 *   <li>His plan: his goal as it is, his slot, the spots at right angles to the soonest bullet he reacts to (away from
 *       its line, then the other way), where he is, then eight directions round him at his dodge step and twice it;
 *       the first whose way there (flown at his top speed, the slot moving with the ship) keeps every bullet he reacts
 *       to, and every one not yet decided, his clearance off over the look-ahead (else the one that keeps them
 *       farthest), inside the field's margins and outside his distance from the player. A spot other than his slot he
 *       holds whatever the ship does for the look-ahead, then back to his slot.
 * </ul>
 *
 * Each {@link Wingman} holds one only in a level of Act 6 or later, made once; it allocates nothing per step.
 */
final class RookWeave {
    /** The first act he weaves in. */
    static final int FROM_ACT = 6;

    /** {@link #scan}: no bullet he reacts to threatens his way. */
    static final int NONE = 0;

    /** {@link #scan}'s bit: a bullet he reacts to since his reaction delay threatens his way. */
    static final int KNOWN = 1;

    /** {@link #scan}'s bit: a bullet he newly reacts to threatens his way. */
    static final int NEW = 2;

    /** The bullets he remembers his decision on at once (the oldest gives way). */
    private static final int MEMORY = 16;

    /** The directions round him a planned spot may lie in, beyond the two at right angles to the soonest bullet. */
    private static final int DIRECTIONS = 8;

    /** The rings of those directions: at his dodge step, then twice it. */
    private static final int RINGS = 2;

    /** How often along the look-ahead his way is checked against a bullet, s (a bullet flies 8 px meanwhile). */
    private static final double SAMPLE_SECONDS = 0.05;

    private static final double SAME_LINE = 1;

    /** How long the ship's jinks take to move his slot, s (the time constant of his anchor's smoothing). */
    private static final double SMOOTH_SECONDS = 0.5;

    private final double clearance;
    private final double step;
    private final double reacts;
    private final double topSpeed;
    private final int samples;
    private final int holdTicks;
    /** His reaction delay, steps: a bullet he reacts to counts in his plans this long after his decision. */
    private final int reactionTicks;

    private final double[] decidedVx = new double[MEMORY];
    private final double[] decidedVy = new double[MEMORY];
    private final double[] decidedLine = new double[MEMORY];
    private final int[] decidedTicks = new int[MEMORY];
    private final boolean[] reacted = new boolean[MEMORY];

    /** The spot {@link #plan} chose to hold (absolute), while he {@link #holds} it. */
    private double planX;

    private double planY;

    private boolean holds;

    /** The ship's last position, px. */
    private double shipX;

    private double shipY;
    /** Where he takes the ship to be: its position smoothed over {@link #SMOOTH_SECONDS} (his slot hangs off it). */
    private double anchorX;

    private double anchorY;
    private double anchorVx;
    private double anchorVy;

    /** Whether {@link #shipX} is known (his anchor starts on the ship at the first {@link #ship} after he is made). */
    private boolean shipKnown;

    /**
     * @param speed his top speed, px/s
     * @param memoryTicks how long he remembers a decision (until the bullet has passed)
     * @param reaction his reaction delay, steps
     */
    RookWeave(WingmanSpec.Ai ai, double speed, int memoryTicks, int reaction) {
        clearance = ai.clearance();
        step = ai.dodgeStep();
        reacts = ai.reacts();
        topSpeed = speed;
        samples = (int) Math.ceil(ai.lookAhead() / SAMPLE_SECONDS - 1e-9);
        holdTicks = memoryTicks;
        reactionTicks = reaction;
    }

    /** An attempt's start, the ship at (atX, atY): every decision forgotten, back to his slot. */
    void reset(double atX, double atY) {
        for (int m = 0; m < MEMORY; m++) {
            decidedTicks[m] = 0;
        }
        planX = planY = 0;
        holds = false;
        shipX = atX;
        shipY = atY;
        shipKnown = true;
        anchorX = atX;
        anchorY = atY;
        anchorVx = anchorVy = 0;
    }

    /** The ship at (x, y) this step: his anchor follows it. */
    void ship(double x, double y) {
        if (!shipKnown) {
            anchorX = x;
            anchorY = y;
        }
        shipKnown = true;
        shipX = x;
        shipY = y;
        double share = SimStep.SECONDS / SMOOTH_SECONDS;
        anchorVx = (x - anchorX) * share / SimStep.SECONDS;
        anchorVy = (y - anchorY) * share / SimStep.SECONDS;
        anchorX += (x - anchorX) * share;
        anchorY += (y - anchorY) * share;
    }

    /** Where he takes the ship to be (its position smoothed), x. */
    double anchorX() {
        return anchorX;
    }

    /** Where he takes the ship to be, y. */
    double anchorY() {
        return anchorY;
    }

    /** One step of his memory. */
    void tick() {
        for (int m = 0; m < MEMORY; m++) {
            if (decidedTicks[m] > 0) {
                decidedTicks[m]--;
            }
        }
    }

    /**
     * Decides every bullet of {@code bullets} not yet decided that would pass within his clearance of him where he is,
     * (x, y), over the look-ahead, each with his reaction share ({@code luck}). Returns {@link #NONE} or the bits
     * {@link #KNOWN} (he plans his way at once) when a bullet he reacts to since his reaction delay comes at him, or a
     * bullet not at him (one he missed aside) lies on his way to his goal (gx, gy) (a goal that moves with the ship
     * unless he {@link #holds} a spot), and {@link #NEW} (he plans after his reaction delay) when one he newly reacts to
     * comes at him.
     */
    int scan(Pool<EnemyBullet> bullets, double x, double y, double gx, double gy, SplitMix64 luck) {
        int threat = NONE;
        for (int i = 0; i < bullets.size(); i++) {
            EnemyBullet bullet = bullets.get(i);
            double speed = bullet.vx() * bullet.vx() + bullet.vy() * bullet.vy();
            if (speed <= 0) {
                continue;
            }
            int memory = decided(bullet, speed);
            if (nearest(bullet, x, y, x, y, false) >= clearance) {
                // Not at him where he is: on his way, he steers round it at once (one he missed aside).
                if ((memory < 0 || reacted[memory]) && nearest(bullet, x, y, gx, gy, !holds) < clearance) {
                    threat |= KNOWN;
                }
                continue;
            }
            if (memory < 0) {
                boolean react = luck.nextDouble() < reacts;
                remember(bullet, react);
                if (react) {
                    threat |= NEW;
                }
            } else if (reacted[memory] && holdTicks - decidedTicks[memory] >= reactionTicks) {
                threat |= KNOWN;
            }
        }
        return threat;
    }

    /**
     * Plans where he goes from (x, y), his slot at (slotX, slotY) (absolute) and his goal now (gx, gy) (see the class):
     * the spot he holds whatever the ship does, or his slot ({@link #holds} false). {@code limit} is his centre's
     * margin inside the field, the ship kept {@code minDistance} off.
     */
    void plan(
            Pool<EnemyBullet> bullets,
            double x,
            double y,
            double slotX,
            double slotY,
            double gx,
            double gy,
            double minDistance,
            double limit) {
        // The soonest bullet he reacts to that comes his way: the sidestep's axis.
        int soonest = -1;
        double soonestT = Double.POSITIVE_INFINITY;
        for (int i = 0; i < bullets.size(); i++) {
            EnemyBullet bullet = bullets.get(i);
            double speed = bullet.vx() * bullet.vx() + bullet.vy() * bullet.vy();
            if (speed <= 0 || !reactsTo(bullet, speed)) {
                continue;
            }
            double t = -((bullet.x() - x) * bullet.vx() + (bullet.y() - y) * bullet.vy()) / speed;
            if (t >= 0 && t < soonestT) {
                soonestT = t;
                soonest = i;
            }
        }
        double px = 1;
        double py = 0;
        double away = 1;
        if (soonest >= 0) {
            EnemyBullet bullet = bullets.get(soonest);
            double speed = Math.sqrt(bullet.vx() * bullet.vx() + bullet.vy() * bullet.vy());
            px = -bullet.vy() / speed;
            py = bullet.vx() / speed;
            away = px * (x - bullet.x()) + py * (y - bullet.y()) >= 0 ? 1 : -1;
        }
        double bestX = gx;
        double bestY = gy;
        boolean bestHolds = holds;
        double bestGap = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < 5 + RINGS * DIRECTIONS; c++) {
            double ax;
            double ay;
            boolean hold = true;
            if (c == 0) {
                ax = gx;
                ay = gy;
                hold = holds;
            } else if (c == 1) {
                ax = slotX;
                ay = slotY;
                hold = false;
            } else if (c == 4) {
                ax = x;
                ay = y;
            } else if (c < 4) {
                double s = c == 2 ? away : -away;
                ax = x + s * step * px;
                ay = y + s * step * py;
            } else {
                double heading = ((c - 5) % DIRECTIONS) * 2 * StrictMath.PI / DIRECTIONS;
                double reach = step * (1 + (c - 5) / DIRECTIONS);
                ax = x + reach * Trig.cos(heading);
                ay = y + reach * Trig.sin(heading);
            }
            if (ax < limit
                    || ax > PlayField.WIDTH - limit
                    || ay < limit
                    || ay > PlayField.HEIGHT - limit
                    || (ax - shipX) * (ax - shipX) + (ay - shipY) * (ay - shipY) < minDistance * minDistance) {
                continue;
            }
            double gap = gap(bullets, x, y, ax, ay, !hold);
            if (gap >= clearance) {
                bestX = ax;
                bestY = ay;
                bestHolds = hold;
                break;
            }
            if (gap > bestGap) {
                bestGap = gap;
                bestX = ax;
                bestY = ay;
                bestHolds = hold;
            }
        }
        planX = bestX;
        planY = bestY;
        holds = bestHolds;
    }

    /** Whether he holds the spot {@link #plan} chose ({@link #x()}, {@link #y()}) instead of his slot. */
    boolean holds() {
        return holds;
    }

    /** His sidestep ends: back to his slot. */
    void release() {
        holds = false;
    }

    /** The spot he holds, x. */
    double x() {
        return planX;
    }

    /** The spot he holds, y. */
    double y() {
        return planY;
    }

    /**
     * The nearest any bullet he steers round ({@link #seen}) comes to him on his way from (x, y) to (gx, gy), the goal
     * {@code moving} with the ship or held, px (infinite without one).
     */
    private double gap(Pool<EnemyBullet> bullets, double x, double y, double gx, double gy, boolean moving) {
        double gap = Double.POSITIVE_INFINITY;
        for (int i = 0; i < bullets.size(); i++) {
            EnemyBullet bullet = bullets.get(i);
            double speed = bullet.vx() * bullet.vx() + bullet.vy() * bullet.vy();
            if (speed > 0 && seen(bullet, speed, x, y)) {
                gap = Math.min(gap, nearest(bullet, x, y, gx, gy, moving));
            }
        }
        return gap;
    }

    /**
     * The nearest {@code bullet} comes to him over the look-ahead while he flies from (x, y) after his goal (gx, gy) at
     * his top speed, the goal {@code moving} with the ship or held, px (checked every {@link #SAMPLE_SECONDS}).
     */
    private double nearest(EnemyBullet bullet, double x, double y, double gx, double gy, boolean moving) {
        // His anchor's motion this step: the slot's, held over the look-ahead.
        double gvx = moving ? anchorVx : 0;
        double gvy = moving ? anchorVy : 0;
        double reach = topSpeed * SAMPLE_SECONDS;
        double rx = x;
        double ry = y;
        double nearest = Double.POSITIVE_INFINITY;
        for (int k = 0; k <= samples; k++) {
            double t = k * SAMPLE_SECONDS;
            if (k > 0) {
                double dx = gx + gvx * t - rx;
                double dy = gy + gvy * t - ry;
                double way = Math.sqrt(dx * dx + dy * dy);
                double move = way > reach ? reach / way : 1;
                rx += dx * move;
                ry += dy * move;
            }
            double bx = bullet.x() + bullet.vx() * t - rx;
            double by = bullet.y() + bullet.vy() * t - ry;
            nearest = Math.min(nearest, bx * bx + by * by);
        }
        return Math.sqrt(nearest);
    }

    /**
     * Whether his plans from (x, y) steer round {@code bullet}: one he reacts to since his reaction delay, or one not
     * yet decided that is not at him where he is (only on a way he might take).
     */
    private boolean seen(EnemyBullet bullet, double speed, double x, double y) {
        int memory = decided(bullet, speed);
        if (memory >= 0) {
            return reacted[memory] && holdTicks - decidedTicks[memory] >= reactionTicks;
        }
        return nearest(bullet, x, y, x, y, false) >= clearance;
    }

    /** Whether he decided to react to {@code bullet} at least his reaction delay ago. */
    private boolean reactsTo(EnemyBullet bullet, double speed) {
        int memory = decided(bullet, speed);
        return memory >= 0 && reacted[memory] && holdTicks - decidedTicks[memory] >= reactionTicks;
    }

    /** His memory of his decision on {@code bullet} (its squared speed {@code speed}), or -1. */
    private int decided(EnemyBullet bullet, double speed) {
        double line = bullet.x() * bullet.vy() - bullet.y() * bullet.vx();
        double same = SAME_LINE * Math.sqrt(speed);
        for (int m = 0; m < MEMORY; m++) {
            if (decidedTicks[m] > 0
                    && decidedVx[m] == bullet.vx()
                    && decidedVy[m] == bullet.vy()
                    && Math.abs(decidedLine[m] - line) <= same) {
                return m;
            }
        }
        return -1;
    }

    /** Remembers his decision on a bullet until it has passed, in place of the oldest memory. */
    private void remember(EnemyBullet bullet, boolean react) {
        int slot = 0;
        for (int m = 1; m < MEMORY; m++) {
            if (decidedTicks[m] < decidedTicks[slot]) {
                slot = m;
            }
        }
        decidedVx[slot] = bullet.vx();
        decidedVy[slot] = bullet.vy();
        decidedLine[slot] = bullet.x() * bullet.vy() - bullet.y() * bullet.vx();
        decidedTicks[slot] = holdTicks;
        reacted[slot] = react;
    }

    void addTo(StateHash hash) {
        hash.add(planX)
                .add(planY)
                .add(holds ? 1 : 0)
                .add(shipX)
                .add(shipY)
                .add(shipKnown ? 1 : 0)
                .add(anchorX)
                .add(anchorY)
                .add(anchorVx)
                .add(anchorVy);
        for (int m = 0; m < MEMORY; m++) {
            if (decidedTicks[m] > 0) {
                hash.add(decidedVx[m])
                        .add(decidedVy[m])
                        .add(decidedLine[m])
                        .add(decidedTicks[m])
                        .add(reacted[m] ? 1 : 0);
            }
        }
    }
}
