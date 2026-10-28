package vanguard.sim;

/**
 * A projectile of one of the ship's weapons: a bolt flying straight (a turret's shot too), a homing
 * missile, a bomb or shell on its way to the ground, a proximity mine holding its place or (M5 part
 * E) a torpedo running under the water (see {@link WeaponSpec.Delivery}). Pooled: {@link #fire}
 * reuses the instance.
 */
public final class Shot implements Hashed {
    /** The most targets a piercing bolt remembers, so it hits each only once. */
    static final int MAX_STRUCK = 8;

    private WeaponSpec weapon;
    private int mount;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double vx;
    private double vy;
    /** Radians clockwise from straight up the screen. */
    private double heading;

    private double damage;
    private double travelled;
    private int ticks;
    private int pierceLeft;
    private final int[] struck = new int[MAX_STRUCK];
    private int struckCount;
    /** A homing shot's (or a torpedo's) locked target, a {@link Enemy#serial()}; -1 while it has none. */
    private int target;

    private double startX;
    private double startY;
    private double landX;
    private double landY;
    private int airTicks;
    /** The share of the full lob it flies, 0..1: its arc's height; 1 for a full lob or a bomb. */
    private double arc = 1;
    /**
     * M8 part A: its speed factor, fixed at its spawn (the water's bullet speed, a depth charge's roll-out); 1 in the
     * air.
     */
    private double speedFactor = 1;
    /** M8 part A: how far it flies, px: its weapon's range, under water a torpedo's longer run or a lance's reach. */
    private double range;
    /** M8 part G: whether a heat plume bent it already (once per shot; hashed only once set). */
    private boolean bent;

    /** A bolt, a homing missile, a mine or a torpedo leaving (x, y) at {@code angle}. */
    void fire(WeaponSpec spec, int mountIndex, double startX, double startY, double angle) {
        start(spec, mountIndex, startX, startY);
        heading = angle;
        vx = spec.speed() * Trig.sin(angle);
        vy = spec.speed() * Trig.cos(angle);
    }

    /** A bomb or shell released at (x, y) that lands at (landX, landY) on the ground. */
    void lob(WeaponSpec spec, int mountIndex, double releaseX, double releaseY, double groundX, double groundY) {
        lob(spec, mountIndex, releaseX, releaseY, groundX, groundY, 1);
    }

    /**
     * A shell lobbed a {@code share} (0..1) of its full range: it flies that share of the weapon's
     * air time, on an arc that much lower (Rook's aimed Mortar).
     */
    void lob(
            WeaponSpec spec,
            int mountIndex,
            double releaseX,
            double releaseY,
            double groundX,
            double groundY,
            double share) {
        start(spec, mountIndex, releaseX, releaseY);
        landX = groundX;
        landY = groundY;
        arc = share;
        airTicks = Math.max(1, SimStep.ticks(spec.airSeconds() * share));
        heading = 0;
        vx = vy = 0;
        placeInAir();
        prevX = x;
        prevY = y;
    }

    private void start(WeaponSpec spec, int mountIndex, double startX, double startY) {
        weapon = spec;
        mount = mountIndex;
        x = prevX = this.startX = startX;
        y = prevY = this.startY = startY;
        damage = spec.damage();
        travelled = 0;
        ticks = 0;
        pierceLeft = spec.pierce();
        struckCount = 0;
        target = -1;
        arc = 1;
        speedFactor = 1;
        range = spec.range();
        bent = false;
    }

    /**
     * M8 part A (design/world/europa, Under water rules): the water's factors on this shot, just fired: its damage
     * times {@code damageFactor}, its speed times {@code speedFactor} (its velocity now and its acceleration's), its
     * range times {@code rangeFactor} and at most {@code rangeLimit} px. Called once per shot, only under water (or
     * easing out of it).
     */
    void soak(double damageFactor, double speedFactor, double rangeFactor, double rangeLimit) {
        damage *= damageFactor;
        this.speedFactor = speedFactor;
        vx *= speedFactor;
        vy *= speedFactor;
        range = Math.min(range * rangeFactor, rangeLimit);
    }

    /** One step of straight flight; an accelerating missile speeds up along its heading. */
    void move() {
        prevX = x;
        prevY = y;
        double speed = speed();
        if (weapon.accelSeconds() > 0) {
            vx = speed * Trig.sin(heading);
            vy = speed * Trig.cos(heading);
        }
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
        travelled += speed * SimStep.SECONDS;
        ticks++;
    }

    /** Its speed now, px/s: an accelerating missile's grows from the weapon's speed to its end speed. */
    double speed() {
        if (weapon.accelSeconds() <= 0) {
            return weapon.speed() * speedFactor;
        }
        double share = Math.min(1, ticks * SimStep.SECONDS / weapon.accelSeconds());
        return (weapon.speed() + (weapon.endSpeed() - weapon.speed()) * share) * speedFactor;
    }

    /**
     * One step of a mine: its drift decays to nothing over the weapon's drift time, then it holds its
     * screen position.
     */
    void drift() {
        prevX = x;
        prevY = y;
        double left = 1 - ticks * SimStep.SECONDS / weapon.mines().driftSeconds();
        if (left > 0) {
            x += vx * left * SimStep.SECONDS;
            y += vy * left * SimStep.SECONDS;
        }
        ticks++;
    }

    /**
     * M8 part A: one step of a depth charge: it drifts as a mine does ({@link #drift}), then sinks with the ground,
     * which scrolls down by {@code scroll}.
     */
    void sink(double scroll) {
        boolean drifting = ticks * SimStep.SECONDS < weapon.mines().driftSeconds();
        drift();
        if (!drifting) {
            y -= scroll;
        }
    }

    /** M8 part A: whether a depth charge's fuse has run: it bursts now. */
    boolean fused() {
        return ticks >= SimStep.ticks(weapon.mines().fuseSeconds());
    }

    /** Whether a mine has armed: it bursts when an enemy comes close. */
    public boolean armed() {
        return ticks >= SimStep.ticks(weapon.mines().armSeconds());
    }

    /** Whether a mine armed in the step it just took (its arming beep). */
    boolean armsNow() {
        return ticks == SimStep.ticks(weapon.mines().armSeconds());
    }

    /** Steps since it left the muzzle. */
    public int age() {
        return ticks;
    }

    /** Its velocity across the screen, px/s (y up); 0 for a bomb or shell. */
    double vx() {
        return vx;
    }

    double vy() {
        return vy;
    }

    /** Turns towards (tx, ty) by at most the weapon's turn rate times {@code turnFactor} for one step. */
    void steer(double tx, double ty, double turnFactor) {
        double wanted = StrictMath.atan2(tx - x, ty - y);
        double delta = Math.IEEEremainder(wanted - heading, 2 * StrictMath.PI);
        double most = weapon.turnRate() * turnFactor * SimStep.SECONDS;
        heading = Math.IEEEremainder(heading + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
        double speed = speed();
        vx = speed * Trig.sin(heading);
        vy = speed * Trig.cos(heading);
    }

    /**
     * One step on the way to the ground: the landing point scrolls down with the ground by
     * {@code scroll}. Returns whether it lands in this step.
     */
    boolean fall(double scroll) {
        prevX = x;
        prevY = y;
        landY -= scroll;
        ticks++;
        placeInAir();
        return ticks >= airTicks;
    }

    /**
     * M7 part C: its landing point moved across to {@code x} (a bomb or shell aimed over rock lands at the
     * wall's foot); a dropped bomb is seen there from now.
     */
    void landAt(double x) {
        if (x != landX) {
            landX = x;
            placeInAir();
        }
    }

    /** M7 part C: a mine moved across to {@code x} (kept off rock). */
    void placeX(double toX) {
        x = toX;
    }

    /** A dropped bomb is seen at its landing point; a lobbed shell between the muzzle and it. */
    private void placeInAir() {
        if (weapon.delivery() == WeaponSpec.Delivery.DROPPED) {
            x = landX;
            y = landY;
        } else {
            double p = (double) ticks / airTicks;
            x = startX + (landX - startX) * p;
            y = startY + (landY - startY) * p;
        }
    }

    /** Whether it already hit the target with {@code key}; otherwise remembers it if there is room. */
    boolean struck(int key) {
        for (int i = 0; i < struckCount; i++) {
            if (struck[i] == key) {
                return true;
            }
        }
        if (struckCount < MAX_STRUCK) {
            struck[struckCount++] = key;
        }
        return false;
    }

    /** Counts one target it passed through; returns whether it is spent. */
    boolean pierced() {
        return --pierceLeft <= 0;
    }

    /** Whether it has flown (or a torpedo run) its range, or its lifetime for a homing shot or a mine. */
    boolean spent() {
        if (weapon.delivery() == WeaponSpec.Delivery.HOMING || weapon.delivery() == WeaponSpec.Delivery.MINE) {
            return ticks * SimStep.SECONDS >= weapon.lifetimeSeconds();
        }
        return travelled >= range;
    }

    /** M8 part A: how far it flies (or a torpedo runs, or a homing shot seeks), px; under water as soaked. */
    double range() {
        return range;
    }

    /** M8 part A: its speed factor, fixed at its spawn; 1 in the air. */
    public double speedFactor() {
        return speedFactor;
    }

    void lock(int serial) {
        target = serial;
    }

    /**
     * M8 part G (design/campaign Level 27, Hazards; {@link HeatPlumes}): whether a heat plume can bend it: a bolt, a
     * homing missile, a turret's shot or a torpedo (a harpoon) not bent yet; never a bomb, a shell, a mine or a depth
     * charge (§5 G6).
     */
    boolean bendable() {
        if (bent) {
            return false;
        }
        return switch (weapon.delivery()) {
            case BOLT, HOMING, TURRET, TORPEDO -> true;
            case DROPPED, LOBBED, MINE, BEAM, DEPTH_CHARGE, ARC -> false;
        };
    }

    /**
     * M8 part G: a heat plume turns it by {@code radians} (clockwise on the screen for a positive angle), once; a homing
     * shot steers on from its new heading.
     */
    void bend(double radians) {
        bent = true;
        heading = Math.IEEEremainder(heading + radians, 2 * StrictMath.PI);
        double cos = Trig.cos(radians);
        double sin = Trig.sin(radians);
        double turnedX = vx * cos + vy * sin;
        vy = -vx * sin + vy * cos;
        vx = turnedX;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(mount)
                .add(x)
                .add(y)
                .add(vx)
                .add(vy)
                .add(heading)
                .add(damage)
                .add(travelled)
                .add(ticks)
                .add(pierceLeft)
                .add(struckCount)
                .add(target)
                .add(landX)
                .add(landY);
        if (weapon.delivery() == WeaponSpec.Delivery.LOBBED) {
            // A shell's flight: Rook's aimed lobs fly shorter (user decision 2026-10-07).
            hash.add(airTicks);
        }
        for (int i = 0; i < struckCount; i++) {
            hash.add(struck[i]);
        }
        if (speedFactor != 1 || range != weapon.range()) {
            // M8 part A: only a shot fired in the water, so the other levels hash as before.
            hash.add(speedFactor).add(range);
        }
        if (bent) {
            // M8 part G: only a shot a plume bent, so the other levels hash as before.
            hash.add(1);
        }
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    double damage() {
        return damage;
    }

    int target() {
        return target;
    }

    double landX() {
        return landX;
    }

    double landY() {
        return landY;
    }

    public WeaponSpec weapon() {
        return weapon;
    }

    /** The index of the mount that fired it, in the sortie's {@link Armament}. */
    public int mount() {
        return mount;
    }

    /** Its heading, radians clockwise from straight up the screen. */
    public double heading() {
        return heading;
    }

    /** How far a dropped or lobbed projectile is on its way to the ground, 0..1; 0 for others. */
    public double airProgress(double alpha) {
        if (!weapon.delivery().landing()) {
            return 0;
        }
        return Math.min(1, (ticks + alpha) / airTicks);
    }

    /** The height of a lobbed shell's arc as a share of a full-range lob's, 0..1 (Rook aims shorter lobs). */
    public double arc() {
        return arc;
    }

    /** The share of a bolt's range that is left, 1 for one that flies to the screen edge. */
    public double rangeLeft() {
        if ((weapon.delivery() != WeaponSpec.Delivery.BOLT && weapon.delivery() != WeaponSpec.Delivery.TURRET)
                || Double.isInfinite(range)) {
            return 1;
        }
        return Math.max(0, 1 - travelled / range);
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
