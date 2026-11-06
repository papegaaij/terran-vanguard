package vanguard.sim;

import static vanguard.sim.ShotGeometry.onField;

/**
 * M11 part A, step A1b: the Vrell Swarm Call in flight (design/player/specials, Vrell Swarm Call (L46)), owned by the
 * {@link SpecialSlot} with the special fitted ({@link SpecialSlot#swarm()}). A call opens the bloom behind the ship
 * ({@link SimEvents.Type#SWARM_CALLED}); at its end the drones fan out from it and hunt for their life, then wither
 * ({@link SimEvents.Type#SWARM_WITHER}) and are gone. They fly in the screen's frame (the current never drifts them;
 * their orbit is round their target, so they follow it) and nothing slows them.
 *
 * <p>Each drone hunts the enemy nearest the ship that fewer than {@link SwarmSpec#hunters()} drones hunt: a unit on
 * the field on {@code air}, {@code low-air} or {@code ground} (not a neutral craft, one latched onto the hull, a unit
 * the seekers ignore or one gone in a teleport), or a boss's open part while the call's boss cap holds; it re-picks
 * when its target is gone. Out of its orbit it flies at it, turning at its rate; inside it circles it and spits a
 * thorn every {@link SwarmSpec#thornEvery()} s, aimed: a bolt of the ship's ({@link PlayerFire#addThorn}, its own
 * mount {@link PlayerFire#swarmMount()}: it pays as the ship's, in no DPS measure; a hardened unit glances it off). A
 * boss takes at most the cap from one call (the thorns aimed at its parts count against it). With no target a drone
 * circles the ship. An enemy bullet that touches a drone hits it and is gone ({@link SimEvents.Type#SWARM_DRONE_HIT});
 * a drone at 0 HP bursts ({@link SimEvents.Type#SWARM_DRONE_LOST}). Lasers, beams and bodies pass; the enemies never
 * aim at them; the player's and Rook's shots, the Smart Bomb and the EMP never touch them (they are not enemies).
 * Allocates nothing.
 */
public final class SwarmCall {
    /** The most drones a call sends. */
    static final int MOST = 8;

    private static final int NONE = -1;
    /** A part target's code: {@code PART − (k × MAX_PARTS + p)}. */
    private static final int PART = -2;
    /** The fan the drones leave the bloom in, either side of straight up, radians. */
    private static final double FAN = Math.toRadians(75);
    /** The bloom's place below the ship's centre, px. */
    static final double BLOOM_BEHIND = 30;
    /** The slow orbit's turn round its target, radians a second (the build's number: "a slow orbit"). */
    static final double ORBIT_TURN = Math.toRadians(90);

    private final SwarmSpec spec;
    private final SimEvents events;
    private final WeaponSpec thorn;
    private final int bloomTicks;
    private final int lifeTicks;
    private final int witherTicks;
    private final int thornTicks;
    private final double turnPerStep;
    private final double stepLength;
    private final Hitbox box;

    private final double[] xs = new double[MOST];
    private final double[] ys = new double[MOST];
    private final double[] headings = new double[MOST];
    private final double[] hps = new double[MOST];
    private final int[] targets = new int[MOST];
    private final int[] thornWait = new int[MOST];
    /** Per drone in its orbit: its angle round the target (clockwise from straight up) and its radius; -1: not in it. */
    private final double[] orbitAngles = new double[MOST];

    private final double[] orbitRadii = new double[MOST];
    /** Per drone, where its target was in the last step (the thorn's lead); NaN before. */
    private final double[] lastXs = new double[MOST];

    private final double[] lastYs = new double[MOST];
    private final boolean[] alive = new boolean[MOST];

    /** Steps since the call; -1 with no call in flight. */
    private int age = -1;

    private double bloomX;
    private double bloomY;
    /** The damage the thorns aimed at a boss's parts may still do in this call. */
    private double bossLeft;

    SwarmCall(SwarmSpec spec, SimEvents events) {
        this.spec = spec;
        this.events = events;
        bloomTicks = SimStep.ticks(spec.bloomSeconds());
        lifeTicks = Math.max(1, SimStep.ticks(spec.lifeSeconds()));
        witherTicks = SimStep.ticks(spec.witherSeconds());
        thornTicks = Math.max(1, SimStep.ticks(spec.thornEvery()));
        turnPerStep = Math.toRadians(spec.turnDegrees()) * SimStep.SECONDS;
        stepLength = spec.speed() * SimStep.SECONDS;
        box = new Hitbox(spec.size(), spec.size());
        thorn = new WeaponSpec(
                "swarm-thorn",
                "swarm-thorn",
                "swarm-thorn",
                WeaponSpec.Delivery.BOLT,
                false,
                1,
                spec.thornDamage(),
                spec.thornSpeed(),
                EnemyGun.BULLET,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                1,
                0,
                0,
                StrictMath.PI,
                0,
                0,
                java.util.List.of(new WeaponSpec.Muzzle(0, 0, 0)));
    }

    /** No call in flight. */
    void reset() {
        age = -1;
        java.util.Arrays.fill(alive, false);
    }

    /** The call: the bloom opens behind the ship; the drones of a call still in flight are gone. */
    void call(double shipX, double shipY) {
        reset();
        age = 0;
        bloomX = shipX;
        bloomY = shipY - BLOOM_BEHIND;
        bossLeft = spec.bossCap();
        events.add(SimEvents.Type.SWARM_CALLED, bloomX, bloomY, spec.drones());
    }

    /** One step of the call in flight: the release, the hunt, the thorns, the bullets on the drones, the wither. */
    void step(Ship ship, Pool<Enemy> enemies, SetPiece[] setPieces, Pool<EnemyBullet> bullets, PlayerFire fire) {
        if (age < 0) {
            return;
        }
        age++;
        if (age == bloomTicks + 1 || (bloomTicks == 0 && age == 1)) {
            release();
        }
        if (age > bloomTicks + lifeTicks + witherTicks) {
            reset();
            return;
        }
        boolean hunting = age <= bloomTicks + lifeTicks;
        if (age == bloomTicks + lifeTicks + 1) {
            events.add(SimEvents.Type.SWARM_WITHER, ship.x(), ship.y());
        }
        for (int d = 0; d < spec.drones(); d++) {
            if (!alive[d]) {
                continue;
            }
            if (hunting) {
                if (!valid(targets[d], fire, enemies, setPieces)) {
                    targets[d] = pick(d, ship, fire, enemies, setPieces);
                    orbitRadii[d] = -1;
                    lastXs[d] = Double.NaN;
                }
                fly(d, ship, enemies, setPieces);
                thorn(d, enemies, setPieces, fire);
                screen(d, bullets);
            } else {
                // Withering: they drift on, firing nothing, screening nothing.
                xs[d] += stepLength * Trig.sin(headings[d]);
                ys[d] += stepLength * Trig.cos(headings[d]);
            }
        }
    }

    private void release() {
        int n = spec.drones();
        for (int d = 0; d < n; d++) {
            alive[d] = true;
            xs[d] = bloomX;
            ys[d] = bloomY;
            headings[d] = n == 1 ? 0 : -FAN + 2 * FAN * d / (n - 1);
            hps[d] = spec.hp();
            targets[d] = NONE;
            thornWait[d] = 0;
            orbitRadii[d] = -1;
            lastXs[d] = Double.NaN;
            lastYs[d] = Double.NaN;
        }
    }

    /** Whether {@code target} is still one a drone may hunt. */
    private boolean valid(int target, PlayerFire fire, Pool<Enemy> enemies, SetPiece[] setPieces) {
        if (target == NONE) {
            return false;
        }
        if (target >= 0) {
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (enemy.serial() == target) {
                    return huntable(enemy, fire);
                }
            }
            return false;
        }
        int code = PART - target;
        int k = code / SetPieceSpec.MAX_PARTS;
        int p = code % SetPieceSpec.MAX_PARTS;
        return k < setPieces.length && partHuntable(setPieces[k], p, fire);
    }

    private boolean huntable(Enemy enemy, PlayerFire fire) {
        return onField(enemy)
                && fire.medium().reaches(thorn, enemy.layer())
                && !enemy.spec().neutral()
                && !enemy.latched()
                && !enemy.spec().seekersIgnore();
    }

    private boolean partHuntable(SetPiece piece, int p, PlayerFire fire) {
        return bossLeft >= spec.thornDamage()
                && piece.present()
                && !piece.destroyed()
                && !piece.partWrecked(p)
                && !piece.partShielded(p)
                && piece.partOnField(p)
                && fire.medium().reaches(thorn, piece.partLayer(p));
    }

    /** The target nearest the ship that fewer than the hunters' count of the other drones hunt; none for none. */
    private int pick(int d, Ship ship, PlayerFire fire, Pool<Enemy> enemies, SetPiece[] setPieces) {
        int best = NONE;
        double bestDistance = Double.MAX_VALUE;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (!huntable(enemy, fire) || hunters(d, enemy.serial()) >= spec.hunters()) {
                continue;
            }
            double distance = distance(ship.x(), ship.y(), enemy.x(), enemy.y());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = enemy.serial();
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (piece.boss().isEmpty()) {
                continue;
            }
            for (int p = 0; p < piece.partCount(); p++) {
                int code = PART - (k * SetPieceSpec.MAX_PARTS + p);
                if (!partHuntable(piece, p, fire) || hunters(d, code) >= spec.hunters()) {
                    continue;
                }
                double distance = distance(ship.x(), ship.y(), piece.partX(p), piece.partY(p));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = code;
                }
            }
        }
        return best;
    }

    /** How many drones other than {@code d} hunt {@code target}. */
    private int hunters(int d, int target) {
        int n = 0;
        for (int o = 0; o < spec.drones(); o++) {
            n += o != d && alive[o] && targets[o] == target ? 1 : 0;
        }
        return n;
    }

    /** Drone {@code d} flies at its target out of its orbit and circles it inside (round the ship with none). */
    private void fly(int d, Ship ship, Pool<Enemy> enemies, SetPiece[] setPieces) {
        double tx = ship.x();
        double ty = ship.y();
        if (targets[d] != NONE) {
            tx = targetX(targets[d], enemies, setPieces);
            ty = targetY(targets[d], enemies, setPieces);
        }
        double dx = tx - xs[d];
        double dy = ty - ys[d];
        if (orbitRadii[d] < 0 && dx * dx + dy * dy <= spec.orbit() * spec.orbit()) {
            // Into its orbit: it holds its distance and circles slowly round the target, following it.
            orbitRadii[d] = Math.sqrt(dx * dx + dy * dy);
            orbitAngles[d] = StrictMath.atan2(-dx, -dy);
        }
        if (orbitRadii[d] >= 0) {
            orbitAngles[d] = Math.IEEEremainder(orbitAngles[d] + ORBIT_TURN * SimStep.SECONDS, 2 * Math.PI);
            xs[d] = tx + orbitRadii[d] * Trig.sin(orbitAngles[d]);
            ys[d] = ty + orbitRadii[d] * Trig.cos(orbitAngles[d]);
            headings[d] = Math.IEEEremainder(orbitAngles[d] + Math.PI / 2, 2 * Math.PI);
            return;
        }
        double want = StrictMath.atan2(dx, dy);
        double turn = Math.IEEEremainder(want - headings[d], 2 * Math.PI);
        headings[d] =
                Math.IEEEremainder(headings[d] + Math.max(-turnPerStep, Math.min(turnPerStep, turn)), 2 * Math.PI);
        xs[d] += stepLength * Trig.sin(headings[d]);
        ys[d] += stepLength * Trig.cos(headings[d]);
    }

    /** Drone {@code d} spits a thorn at its target when inside its orbit and its thorn is due. */
    private void thorn(int d, Pool<Enemy> enemies, SetPiece[] setPieces, PlayerFire fire) {
        int target = targets[d];
        if (target == NONE) {
            if (thornWait[d] > 0) {
                thornWait[d]--;
            }
            return;
        }
        double tx = targetX(target, enemies, setPieces);
        double ty = targetY(target, enemies, setPieces);
        double dx = tx - xs[d];
        double dy = ty - ys[d];
        // Aimed: led by the target's move in the last step over the thorn's flight.
        double lead =
                Double.isNaN(lastXs[d]) ? 0 : Math.sqrt(dx * dx + dy * dy) / (spec.thornSpeed() * SimStep.SECONDS);
        double vx = Double.isNaN(lastXs[d]) ? 0 : tx - lastXs[d];
        double vy = Double.isNaN(lastXs[d]) ? 0 : ty - lastYs[d];
        lastXs[d] = tx;
        lastYs[d] = ty;
        if (thornWait[d] > 0) {
            thornWait[d]--;
            return;
        }
        if (dx * dx + dy * dy > spec.orbit() * spec.orbit()) {
            return;
        }
        dx += vx * lead;
        dy += vy * lead;
        if (target <= PART) {
            bossLeft -= spec.thornDamage();
        }
        fire.addThorn(thorn, xs[d], ys[d], StrictMath.atan2(dx, dy));
        thornWait[d] = thornTicks - 1;
        events.add(SimEvents.Type.SWARM_THORN, xs[d], ys[d], d);
    }

    /** The enemy bullets touching drone {@code d} hit it and are gone. */
    private void screen(int d, Pool<EnemyBullet> bullets) {
        for (int i = bullets.size() - 1; i >= 0 && alive[d]; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (box.overlaps(xs[d], ys[d], EnemyGun.BULLET, bullet.x(), bullet.y())) {
                hps[d] -= bullet.damage();
                bullets.free(i);
                events.add(SimEvents.Type.SWARM_DRONE_HIT, xs[d], ys[d], d);
                if (hps[d] <= 0) {
                    alive[d] = false;
                    events.add(SimEvents.Type.SWARM_DRONE_LOST, xs[d], ys[d], d);
                }
            }
        }
    }

    private static double targetX(int target, Pool<Enemy> enemies, SetPiece[] setPieces) {
        if (target >= 0) {
            for (int j = 0; j < enemies.size(); j++) {
                if (enemies.get(j).serial() == target) {
                    return enemies.get(j).x();
                }
            }
            return 0;
        }
        int code = PART - target;
        return setPieces[code / SetPieceSpec.MAX_PARTS].partX(code % SetPieceSpec.MAX_PARTS);
    }

    private static double targetY(int target, Pool<Enemy> enemies, SetPiece[] setPieces) {
        if (target >= 0) {
            for (int j = 0; j < enemies.size(); j++) {
                if (enemies.get(j).serial() == target) {
                    return enemies.get(j).y();
                }
            }
            return 0;
        }
        int code = PART - target;
        return setPieces[code / SetPieceSpec.MAX_PARTS].partY(code % SetPieceSpec.MAX_PARTS);
    }

    private static double distance(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        return dx * dx + dy * dy;
    }

    /** Whether a call is in flight (its bloom, its drones, their wither). */
    public boolean active() {
        return age >= 0;
    }

    /** Whether the bloom is opening now (its first {@link SwarmSpec#bloomSeconds()}); its share by {@link #bloom()}. */
    public boolean blooming() {
        return age >= 0 && age <= bloomTicks;
    }

    /** How far the bloom has opened, 0–1; 1 once it has. */
    public double bloom() {
        return bloomTicks == 0 || age < 0 ? 1 : Math.min(1, (double) age / bloomTicks);
    }

    public double bloomX() {
        return bloomX;
    }

    public double bloomY() {
        return bloomY;
    }

    /** How far the drones have withered, 0 hunting to 1 gone. */
    public double withered() {
        int into = age - bloomTicks - lifeTicks;
        return into <= 0 || age < 0 ? 0 : witherTicks == 0 ? 1 : Math.min(1, (double) into / witherTicks);
    }

    /** The call's drones (alive or not: {@link #alive(int)}). */
    public int drones() {
        return age > bloomTicks ? spec.drones() : 0;
    }

    public boolean alive(int d) {
        return alive[d];
    }

    public double x(int d) {
        return xs[d];
    }

    public double y(int d) {
        return ys[d];
    }

    /** Drone {@code d}'s heading, radians clockwise from straight up. */
    public double heading(int d) {
        return headings[d];
    }

    public double hp(int d) {
        return hps[d];
    }

    /** Drone {@code d}'s target: an enemy's serial, a boss part's code (negative), or -1 for none. */
    int target(int d) {
        return targets[d];
    }

    /** The damage a boss may still take from this call's thorns. */
    double bossLeft() {
        return bossLeft;
    }

    void addTo(StateHash hash) {
        hash.add(age);
        if (age < 0) {
            return;
        }
        hash.add(bloomX).add(bloomY).add(bossLeft);
        for (int d = 0; d < spec.drones(); d++) {
            hash.add(alive[d] ? 1 : 0)
                    .add(xs[d])
                    .add(ys[d])
                    .add(headings[d])
                    .add(hps[d])
                    .add(targets[d]);
            hash.add(thornWait[d])
                    .add(orbitAngles[d])
                    .add(orbitRadii[d])
                    .add(lastXs[d])
                    .add(lastYs[d]);
        }
    }
}
