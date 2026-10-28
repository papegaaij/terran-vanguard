package vanguard.sim;

/**
 * A spore mine dropped by a mine layer (design/enemies/air/spore-bomber): it drifts in a random
 * direction; below the player plane until it arms, then it can be shot and bursts on contact; at
 * the end of its life it bursts into a ring of bullets (or fades on easy). M9 part C: a line mine laid by a Minelayer
 * ({@link EnemyGun.LineMine}) holds its place in the field instead, blinks near the ship and bursts with a blast, and
 * a shot one sets off its neighbours ({@link MineLines}). Pooled.
 */
public final class Mine implements Hashed {
    private EnemyGun gun;
    private EnemyGun.MineSpec spec;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double vx;
    private double vy;
    private double hp;
    private int age;
    private int armTicks;
    private int lifeTicks;
    /** M9 part C: whether it is a line mine ({@link EnemyGun.LineMine}): it follows the scroll; hashed only then. */
    private boolean line;
    /** M9 part C: steps left of its proximity blink; -1 while it does not blink. */
    private int blinkTicks = -1;
    /** M9 part C: steps until a neighbour's burst sets it off; 0 while it is not set off. */
    private int chainTicks;

    /** Dropped at (x, y) by a layer with {@code layerGun}, drifting at {@code angle} radians. */
    void drop(EnemyGun layerGun, double startX, double startY, double angle) {
        gun = layerGun;
        spec = layerGun.mine().orElseThrow();
        x = prevX = startX;
        y = prevY = startY;
        vx = spec.drift() * Trig.cos(angle);
        vy = spec.drift() * Trig.sin(angle);
        hp = spec.hp();
        age = 0;
        armTicks = SimStep.ticks(spec.armSeconds());
        lifeTicks = SimStep.ticks(spec.lifeSeconds());
        line = spec.line().isPresent();
        blinkTicks = -1;
        chainTicks = 0;
    }

    /** Drifts one step; returns false once its life is over or it has left the play field. */
    boolean move() {
        return move(0);
    }

    /**
     * Drifts one step, M9 part C: a line mine comes down with the scroll's {@code scroll} px of this step instead;
     * returns false once its life is over or it has left the play field.
     */
    boolean move(double scroll) {
        prevX = x;
        prevY = y;
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
        if (line) {
            y -= scroll;
        }
        age++;
        return age < lifeTicks && PlayField.overlaps(x, y, EnemyGun.MineSpec.BOX);
    }

    /** M9 part C: how far it moved up in its last step, px (negative: down with the scroll). */
    double stepY() {
        return y - prevY;
    }

    /** M9 part C: whether it is a line mine (it follows the scroll, blinks, blasts and chains). */
    public boolean line() {
        return line;
    }

    /** M9 part C: whether its proximity blink runs (the fast red-gold blink before its burst). */
    public boolean blinking() {
        return blinkTicks >= 0;
    }

    /** M9 part C: its blink's share done, 0..1 (0 while it does not blink). */
    public double blinkShare() {
        if (blinkTicks < 0) {
            return 0;
        }
        int total = SimStep.ticks(spec.line().orElseThrow().blinkSeconds());
        return total <= 0 ? 1 : 1 - (double) blinkTicks / total;
    }

    /** M9 part C: its blink starts (an armed line mine near the ship or Rook). */
    void blink() {
        blinkTicks = SimStep.ticks(spec.line().orElseThrow().blinkSeconds());
    }

    /** M9 part C: counts its blink down; returns whether it is over now (the mine bursts). */
    boolean blinkOver() {
        return blinkTicks >= 0 && blinkTicks-- == 0;
    }

    /** M9 part C: whether a neighbour's burst has set it off (it bursts after its hop). */
    public boolean setOff() {
        return chainTicks > 0;
    }

    /** M9 part C: a neighbour's burst sets it off: it bursts in {@code ticks} steps. */
    void setOff(int ticks) {
        chainTicks = Math.max(1, ticks);
    }

    /** M9 part C: counts a set-off down; returns whether it bursts now. */
    boolean chainDue() {
        return chainTicks > 0 && --chainTicks == 0;
    }

    /** Whether its life ran out (rather than it drifting off the screen). */
    boolean expired() {
        return age >= lifeTicks;
    }

    /** Takes damage; returns whether it was destroyed. */
    boolean damage(double amount) {
        hp -= amount;
        return hp <= 0;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(x).add(y).add(vx).add(vy).add(hp).add(age);
        if (line) {
            // M9 part C: only a line mine's own state, so a spore hashes as before.
            hash.add(blinkTicks).add(chainTicks);
        }
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    EnemyGun gun() {
        return gun;
    }

    EnemyGun.MineSpec spec() {
        return spec;
    }

    /** Whether it has risen to the player plane: it can be hit and hits. */
    public boolean armed() {
        return age >= armTicks;
    }

    /** Its share of the time until it arms, 0..1, for the glow that brightens as it rises. */
    public double rising() {
        return Math.min(1, (double) age / Math.max(1, armTicks));
    }

    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }
}
