package vanguard.sim;

/**
 * M9 part D, one unit's dash cycle (design/enemies/air/buzzsaw-drone; {@link EnemySpec.Dash}). Each {@link Enemy} owns
 * one, idle for any unit without a dash; it steers and places its unit through the unit's movement fields while the unit
 * is in {@link Enemy.Phase#DASH}. From its entry (its centre inside the play field) or the end of its circle's hold it
 * chases the ship, then spins up (the tell, its bearing to the ship locked at the tell's start), dashes along the locked
 * bearing bouncing off the play field's edges ({@link #step}) and the large debris chunks ({@link DashRocks}) until its
 * bounces are spent, slows to its chase speed and chases again; after its last dash it leaves through the nearest edge.
 */
final class Dashing {
    /** Its part of the cycle: waiting to enter, chasing, telling, dashing, slowing or leaving. */
    enum Stage {
        IDLE,
        ENTER,
        CHASE,
        TELL,
        DASH,
        SLOW,
        OUT
    }

    private final Enemy unit;
    private Stage stage = Stage.IDLE;
    private int ticks;
    /** Its heading while it chases or tells, radians clockwise from straight down (as {@link Enemy#facing()}). */
    private double heading;
    /** The locked dash's (and the slow's) unit vector. */
    private double dirX;

    private double dirY;
    private int bounces;
    private int dashes;
    /** Whether it bounced in this step. */
    private boolean bounced;

    Dashing(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit without a dash. */
    void clear() {
        stage = Stage.IDLE;
        ticks = 0;
        heading = 0;
        dirX = 0;
        dirY = 0;
        bounces = 0;
        dashes = 0;
        bounced = false;
    }

    /** It flies in along its path; its chase starts as its centre comes onto the play field. */
    void enter() {
        clear();
        stage = Stage.ENTER;
    }

    /** Whether it still waits to come onto the play field. */
    boolean entering() {
        return stage == Stage.ENTER;
    }

    /** Flying in: once its centre is inside the play field its chase starts (along its path's heading). */
    void await() {
        Enemy u = unit;
        if (u.x >= 0 && u.x <= PlayField.WIDTH && u.y >= 0 && u.y <= PlayField.HEIGHT) {
            begin(Enemy.heading(u.path.dx(u.segment), u.path.dy(u.segment)));
        }
    }

    /** Its circle's hold is over (or it entered): its chase starts along {@code startHeading}. */
    void begin(double startHeading) {
        Enemy u = unit;
        u.phase = Enemy.Phase.DASH;
        stage = Stage.CHASE;
        ticks = 0;
        heading = startHeading;
    }

    /** As {@link #begin(double)}, along its facing. */
    void begin() {
        begin(unit.facing);
    }

    /**
     * One step of its cycle toward the ship at (shipX, shipY); returns false once it left the play field after its last
     * dash.
     */
    boolean step(double shipX, double shipY) {
        Enemy u = unit;
        EnemySpec.Dash dash = u.spec.dash().orElseThrow();
        bounced = false;
        ticks++;
        switch (stage) {
            case CHASE -> {
                double delta = Math.IEEEremainder(Enemy.heading(shipX - u.x, shipY - u.y) - heading, 2 * StrictMath.PI);
                double most = dash.turnRate() * SimStep.SECONDS;
                heading = Math.IEEEremainder(heading + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
                fly(-Trig.sin(heading), -Trig.cos(heading), u.spec.speed());
                if (ticks >= SimStep.ticks(dash.chaseSeconds())) {
                    // The tell starts: its bearing to the ship now is the dash's, locked.
                    double dx = shipX - u.x;
                    double dy = shipY - u.y;
                    double length = Math.sqrt(dx * dx + dy * dy);
                    dirX = length > 0 ? dx / length : 0;
                    dirY = length > 0 ? dy / length : -1;
                    stage = Stage.TELL;
                    ticks = 0;
                    u.events.add(SimEvents.Type.DASH_TELL, u.x, u.y, u.kind);
                }
            }
            case TELL -> {
                fly(-Trig.sin(heading), -Trig.cos(heading), dash.tellSpeed());
                if (ticks >= SimStep.ticks(dash.tellSeconds())) {
                    stage = Stage.DASH;
                    ticks = 0;
                    bounces = 0;
                    heading = Enemy.heading(dirX, dirY);
                    u.events.add(SimEvents.Type.DASH_START, u.x, u.y, u.kind);
                }
            }
            case DASH -> {
                fly(dirX, dirY, dash.dashSpeed());
                bounceOffEdges(dash);
            }
            case SLOW -> {
                double share = Math.min(1, (double) ticks / Math.max(1, SimStep.ticks(dash.slowSeconds())));
                fly(dirX, dirY, dash.dashSpeed() + (u.spec.speed() - dash.dashSpeed()) * share);
                if (share >= 1) {
                    dashes++;
                    ticks = 0;
                    heading = Enemy.heading(dirX, dirY);
                    if (dashes >= dash.dashes()) {
                        out();
                    } else {
                        stage = Stage.CHASE;
                    }
                }
            }
            case OUT -> {
                fly(dirX, dirY, u.spec.speed());
                return PlayField.overlaps(u.x, u.y, u.box);
            }
            default -> {
                return true;
            }
        }
        return true;
    }

    private void fly(double ux, double uy, double speed) {
        Enemy u = unit;
        u.vx = ux * speed;
        u.vy = uy * speed;
        u.x += u.vx * SimStep.SECONDS;
        u.y += u.vy * SimStep.SECONDS;
        u.facing = Enemy.heading(ux, uy);
    }

    /** Off a side, the top or the bottom edge: the dash's component across it turns round, the unit kept inside. */
    private void bounceOffEdges(EnemySpec.Dash dash) {
        Enemy u = unit;
        double rx = u.box.width() / 2;
        double ry = u.box.height() / 2;
        if ((u.x - rx < 0 && dirX < 0) || (u.x + rx > PlayField.WIDTH && dirX > 0)) {
            dirX = -dirX;
            u.x = Math.clamp(u.x, rx, PlayField.WIDTH - rx);
            bounce(dash, 0);
        }
        if (stage == Stage.DASH && ((u.y - ry < 0 && dirY < 0) || (u.y + ry > PlayField.HEIGHT && dirY > 0))) {
            dirY = -dirY;
            u.y = Math.clamp(u.y, ry, PlayField.HEIGHT - ry);
            bounce(dash, 0);
        }
    }

    /**
     * Off a large debris chunk at (cx, cy) of {@code radius} (its outline as a circle): reflected on the line from the
     * chunk's centre (equal angles), moved out of it; counts toward its bounces. Nothing while it is not dashing or
     * moving away from the chunk.
     */
    void bounceOffRock(double cx, double cy, double radius) {
        Enemy u = unit;
        if (stage != Stage.DASH) {
            return;
        }
        double nx = u.x - cx;
        double ny = u.y - cy;
        double length = Math.sqrt(nx * nx + ny * ny);
        if (length == 0) {
            nx = -dirX;
            ny = -dirY;
            length = 1;
        }
        nx /= length;
        ny /= length;
        double along = dirX * nx + dirY * ny;
        if (along >= 0) {
            return;
        }
        dirX -= 2 * along * nx;
        dirY -= 2 * along * ny;
        double clear = radius + u.box.width() / 2;
        u.x = cx + nx * clear;
        u.y = cy + ny * clear;
        bounce(u.spec.dash().orElseThrow(), 1);
    }

    private void bounce(EnemySpec.Dash dash, int what) {
        Enemy u = unit;
        bounced = true;
        bounces++;
        u.facing = Enemy.heading(dirX, dirY);
        u.events.add(SimEvents.Type.DASH_BOUNCE, u.x, u.y, what);
        if (bounces >= dash.bounces()) {
            stage = Stage.SLOW;
            ticks = 0;
        }
    }

    /** After its last dash: straight out through the nearest edge at its speed. */
    private void out() {
        Enemy u = unit;
        stage = Stage.OUT;
        double left = u.x;
        double right = PlayField.WIDTH - u.x;
        double bottom = u.y;
        double top = PlayField.HEIGHT - u.y;
        double least = Math.min(Math.min(left, right), Math.min(bottom, top));
        if (least == left) {
            dirX = -1;
            dirY = 0;
        } else if (least == right) {
            dirX = 1;
            dirY = 0;
        } else if (least == bottom) {
            dirX = 0;
            dirY = -1;
        } else {
            dirX = 0;
            dirY = 1;
        }
    }

    /** Where it is in its cycle; {@link Stage#IDLE} for a unit without a dash. */
    Stage stage() {
        return stage;
    }

    /** Whether it is dashing now (a small chunk it touches breaks, a large one bounces it). */
    boolean dashing() {
        return stage == Stage.DASH;
    }

    /** Whether it is in its spin-up tell. */
    boolean telling() {
        return stage == Stage.TELL;
    }

    /** Its tell's progress, 0–1 (0 outside it). */
    double tellShare() {
        if (stage != Stage.TELL) {
            return 0;
        }
        return Math.min(
                1,
                (double) ticks
                        / Math.max(
                                1, SimStep.ticks(unit.spec.dash().orElseThrow().tellSeconds())));
    }

    /** The bounces of its current dash. */
    int bounces() {
        return bounces;
    }

    /** The dashes it finished. */
    int dashes() {
        return dashes;
    }

    /** Whether it bounced this step. */
    boolean bounced() {
        return bounced;
    }

    /** The locked dash's direction, x (a unit vector with {@link #dirY()}). */
    double dirX() {
        return dirX;
    }

    double dirY() {
        return dirY;
    }

    void addTo(StateHash hash) {
        hash.add(stage.ordinal())
                .add(ticks)
                .add(heading)
                .add(dirX)
                .add(dirY)
                .add(bounces)
                .add(dashes);
    }
}
