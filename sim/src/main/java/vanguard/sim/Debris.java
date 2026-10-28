package vanguard.sim;

/**
 * A debris chunk drifting across the screen on the air layer (design/world/earth-orbit, hazards):
 * it blocks shots and enemy bullets; a large one is indestructible and deals contact damage, a
 * small one breaks. It counts the steps since its last hit for its hit flash. Pooled.
 */
public final class Debris implements Hashed {
    private HazardSpec.DebrisSpec spec;
    private int serial;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double hp;
    private int hitCooldown;
    private int ticksSinceHit;
    private double vx;
    private double vy;
    /** A thrown rock's steps left; -1 for a chunk drifting through. */
    private int life = -1;
    /** M7 part H: a rock pulled toward a breaching boss's mouth ({@link #pull}). */
    private boolean pulled;
    /** Where a pulled rock flies to (the mouth's centre), its speed and its gain each second. */
    private double toX;

    private double toY;
    private double speed;
    private double acceleration;

    /** Enters at the top edge at {@code atX}; {@code index} is its place in the level's list, unique in an attempt. */
    void place(HazardSpec.DebrisSpec debrisSpec, int index, double atX) {
        spec = debrisSpec;
        serial = index;
        x = prevX = atX;
        y = prevY = PlayField.HEIGHT + debrisSpec.size().height() / 2;
        hp = debrisSpec.hp();
        hitCooldown = 0;
        ticksSinceHit = Integer.MAX_VALUE;
        vx = debrisSpec.vx();
        vy = debrisSpec.vy();
        life = -1;
        pulled = false;
    }

    /**
     * M9 part B (design/enemies, Rock riding): a wave's rock entering at (atX, atY) (y up; a side entry is moved to
     * touch the play field's edge so it is not gone at once) and drifting at (velocityX, velocityY) px/s.
     */
    void enter(
            HazardSpec.DebrisSpec debrisSpec, int index, double atX, double atY, double velocityX, double velocityY) {
        place(debrisSpec, index, atX);
        double half = debrisSpec.size().width() / 2;
        x = prevX = Math.clamp(atX, 1 - half, PlayField.WIDTH + half - 1);
        y = prevY = atY;
        vx = velocityX;
        vy = velocityY;
    }

    /**
     * Thrown from (x, y) at (vx, vy) px/s (a rock from a destroyed ground unit, Level 05); it vanishes
     * after {@code lifeTicks} steps. {@code index} is unique in an attempt.
     */
    void toss(
            HazardSpec.DebrisSpec debrisSpec,
            int index,
            double atX,
            double atY,
            double velocityX,
            double velocityY,
            int lifeTicks) {
        place(debrisSpec, index, atX);
        y = prevY = atY;
        vx = velocityX;
        vy = velocityY;
        life = lifeTicks;
    }

    /**
     * M7 part H (design/enemies/bosses/dust-colossus, Suction and debris): torn loose at (atX, atY), it flies straight
     * for (targetX, targetY) at {@code startSpeed} px/s, gaining {@code gain} px/s each second, until it is
     * swallowed {@link PulledDebris#SWALLOW} px from there. {@code index} is unique in an attempt.
     */
    void pull(
            HazardSpec.DebrisSpec debrisSpec,
            int index,
            double atX,
            double atY,
            double targetX,
            double targetY,
            double startSpeed,
            double gain) {
        place(debrisSpec, index, atX);
        y = prevY = atY;
        vx = 0;
        vy = 0;
        pulled = true;
        toX = targetX;
        toY = targetY;
        speed = startSpeed;
        acceleration = gain;
    }

    /**
     * Drifts one step; returns false once it has left the screen through the bottom or a side edge (M7 part H: or a
     * pulled rock was swallowed).
     */
    boolean move() {
        prevX = x;
        prevY = y;
        if (pulled) {
            return pulledMove();
        }
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
        if (life >= 0 && --life < 0) {
            return false;
        }
        if (hitCooldown > 0) {
            hitCooldown--;
        }
        if (ticksSinceHit < Integer.MAX_VALUE) {
            ticksSinceHit++;
        }
        Hitbox size = spec.size();
        return y + size.height() / 2 > 0 && x + size.width() / 2 > 0 && x - size.width() / 2 < PlayField.WIDTH;
    }

    private boolean pulledMove() {
        speed += acceleration * SimStep.SECONDS;
        double dx = toX - x;
        double dy = toY - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double step = speed * SimStep.SECONDS;
        if (d <= step + PulledDebris.SWALLOW) {
            return false;
        }
        vx = dx / d * speed;
        vy = dy / d * speed;
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
        if (hitCooldown > 0) {
            hitCooldown--;
        }
        if (ticksSinceHit < Integer.MAX_VALUE) {
            ticksSinceHit++;
        }
        return true;
    }

    /** Whether a box of {@code box} around (bx, by) touches it. */
    boolean touches(double bx, double by, Hitbox box) {
        return spec.size().overlaps(x, y, box, bx, by);
    }

    /** A small chunk takes damage; returns whether it broke. A large one only flashes. */
    boolean damage(double amount) {
        ticksSinceHit = 0;
        hp -= amount;
        return hp <= 0;
    }

    /** A large chunk touching the ship: whether it deals its damage now (at most once per its interval). */
    boolean strike() {
        if (hitCooldown > 0) {
            return false;
        }
        hitCooldown = SimStep.ticks(HazardSpec.DebrisSpec.HIT_INTERVAL_SECONDS);
        return true;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(serial).add(x).add(y).add(hp).add(hitCooldown).add(ticksSinceHit);
        if (life >= 0) {
            hash.add(life).add(vx).add(vy);
        }
        if (pulled) {
            // M7 part H: only a breaching boss's rocks (Level 21) hash these.
            hash.add(toX).add(toY).add(speed).add(acceleration);
        }
    }

    double x() {
        return x;
    }

    /** M9 part B: its drift across the screen, px/s. */
    double vx() {
        return vx;
    }

    double vy() {
        return vy;
    }

    double y() {
        return y;
    }

    public HazardSpec.DebrisSpec spec() {
        return spec;
    }

    /** Its place in the level's list, unique in an attempt (a thrown rock's look picks its shape by it). */
    public int serial() {
        return serial;
    }

    /** Whether it is a thrown rock, which rises and vanishes. */
    public boolean thrown() {
        return life >= 0;
    }

    /** M7 part H: whether it is a rock pulled toward a breaching boss's mouth (it stops shots, not bullets). */
    public boolean pulled() {
        return pulled;
    }

    /** M7 part H: a pulled rock's speed now, px/s (0 for any other chunk). */
    public double pullSpeed() {
        return pulled ? speed : 0;
    }

    /** A thrown rock's steps left; -1 for a chunk drifting through. */
    public int life() {
        return life;
    }

    /** Whether it is a large, indestructible chunk. */
    public boolean large() {
        return spec.large();
    }

    /** The sprite to draw: {@code debris-large-a}, ... (assets/sprites). */
    public String sprite() {
        return spec.sprite();
    }

    /** Simulation steps since the last shot hit it; {@link Integer#MAX_VALUE} before the first. */
    public int ticksSinceHit() {
        return ticksSinceHit;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }
}
