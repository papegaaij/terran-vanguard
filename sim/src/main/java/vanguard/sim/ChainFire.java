package vanguard.sim;

import static vanguard.sim.ShotGeometry.distanceSquared;
import static vanguard.sim.ShotGeometry.onField;

/**
 * M10 part A, step A1b: the Plasma Arc's chain (design/player/weapons/plasma-arc, Behaviour; {@link
 * WeaponSpec.Delivery#CHAIN}), for {@link PlayerFire}, beside the Tesla Coil Pod's {@link ArcFire}. On its fire step a
 * mount discharges at once, no projectile:
 *
 * <ul>
 *   <li>the <b>first strike</b> lands on the nearest valid target within the weapon's reach of the nose in its forward
 *       cone (the weapon's half angle either side of straight up); with none there it does not fire;
 *   <li>from each struck target the arc <b>jumps</b> to the nearest target this discharge has not struck within the
 *       chain's jump radius of it, in any direction, until the chain's count is struck or none is in reach; each strike
 *       does the chain's falloff × the damage of the one before.
 * </ul>
 *
 * <p>A valid target: a unit on the play field on a layer the weapon reaches ({@code air}, {@code low-air}, {@code
 * ground}), not one the seekers ignore, not latched onto the hull (a front mount), not invulnerable (growing, under a
 * dome); or a boss's open part, where the parts of one body are <b>one</b> target (the nearest part; the chain never
 * jumps to a second part of a struck body). Mines are never struck (they are not units). A <b>hardened</b> unit glances
 * the arc off ({@link SimEvents.Type#SHOT_GLANCED}) and the chain ends there. A strike on a <b>standing mirror's
 * face</b> (seen from where the arc comes: the nose, or the unit it jumped from) is earthed: no damage, {@link
 * SimEvents.Type#MIRROR_ABSORBED}, and the chain ends; a jump that comes in from the side reaches the open flank. It is
 * not a {@code shield-breaker}: each strike wears a shield down as any shot. Only with no unit or part in reach does the
 * first strike take an eruption's burning rock ({@link Eruptions}); a jump never lands on one.
 *
 * <p>Each strike reports {@link SimEvents.Type#CHAIN_STRIKE} at the struck point (value {@link #event}: the mount and the
 * strike's index) and an {@link SimEvents.Type#ENEMY_HIT}; each mount keeps its last discharge's struck points for the
 * look ({@link #strikes}, {@link #x}, {@link #y}). Fixed arrays, allocates nothing.
 */
public final class ChainFire {
    /** The struck-list code of set piece k: {@code -(k + 1)} (units are listed by their serial, 0 or more). */
    private static final int PIECE = -1;

    private final PlayerFire fire;
    private final Ship ship;
    private final SetPiece[] setPieces;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    /** Per mount: its last discharge's struck points and how many. */
    private final double[][] xs;

    private final double[][] ys;
    private final int[] counts;
    /** This discharge's struck targets (unit serials, or {@link #PIECE} codes). */
    private final int[] struck;

    private int struckCount;
    /** The level's eruptions, whose rocks a first strike takes when nothing else is in reach; null without. */
    private Eruptions eruptions;

    ChainFire(
            PlayerFire fire,
            Ship ship,
            SetPiece[] setPieces,
            SimEvents events,
            PlayerFire.Hits hits,
            Armament armament) {
        this.fire = fire;
        this.ship = ship;
        this.setPieces = setPieces;
        this.events = events;
        this.hits = hits;
        int most = 1;
        for (int m = 0; m < armament.size(); m++) {
            Armament.Mount mount = armament.mount(m);
            if (mount.fires()) {
                most = Math.max(most, count(mount.weapon()));
                most = Math.max(most, count(mount.overdrive()));
            }
        }
        xs = new double[armament.size()][most];
        ys = new double[armament.size()][most];
        counts = new int[armament.size()];
        struck = new int[most];
    }

    private static int count(WeaponSpec weapon) {
        return weapon == null ? 0 : weapon.chain().map(WeaponSpec.Chain::count).orElse(0);
    }

    /** The level's eruptions (their rocks are a first strike's last resort); null without. */
    void eruptions(Eruptions rocks) {
        eruptions = rocks;
    }

    /** Back to the level start: no discharge yet. */
    void reset() {
        java.util.Arrays.fill(counts, 0);
    }

    /** How many targets mount {@code m}'s last discharge struck (0 before its first). */
    public int strikes(int m) {
        return counts[m];
    }

    /** The point mount {@code m}'s last discharge's strike {@code i} landed on, on the screen. */
    public double x(int m, int i) {
        return xs[m][i];
    }

    public double y(int m, int i) {
        return ys[m][i];
    }

    /** A {@link SimEvents.Type#CHAIN_STRIKE}'s value: the mount {@code m} and the strike's index {@code i} (0 first). */
    public static int event(int m, int i) {
        return i * 16 + m;
    }

    /** The mount of a {@link SimEvents.Type#CHAIN_STRIKE} value. */
    public static int eventMount(int value) {
        return value % 16;
    }

    /** The strike's index in its chain (0: the first strike) of a {@link SimEvents.Type#CHAIN_STRIKE} value. */
    public static int eventIndex(int value) {
        return value / 16;
    }

    /**
     * Mount {@code m} discharges {@code weapon}'s chain if it has a first target; returns whether it fired (without a
     * target it does not).
     */
    boolean discharge(int m, WeaponSpec weapon, Pool<Enemy> enemies) {
        WeaponSpec.Chain chain = weapon.chain().orElseThrow();
        WeaponSpec.Muzzle nose = weapon.muzzles().getFirst();
        double fromX = ship.x() + nose.dx();
        double fromY = ship.y() + nose.dy();
        Medium medium = fire.medium();
        double damage = weapon.damage() * (medium.wet() ? medium.damage(weapon) : 1);
        counts[m] = 0;
        struckCount = 0;
        double reach = weapon.range() * weapon.range();
        double jump = chain.jump() * chain.jump();
        for (int i = 0; i < chain.count(); i++) {
            boolean first = i == 0;
            double radius = first ? reach : jump;
            int unit = nearestUnit(m, weapon, enemies, fromX, fromY, radius, first);
            double unitDistance = unit >= 0
                    ? distanceSquared(
                            fromX,
                            fromY,
                            enemies.get(unit).x(),
                            enemies.get(unit).y())
                    : Double.MAX_VALUE;
            int piece = -1;
            int part = -1;
            double best = Math.min(unitDistance, radius);
            for (int k = 0; k < setPieces.length; k++) {
                SetPiece candidate = setPieces[k];
                if (!candidate.present() || isStruck(PIECE - k)) {
                    continue;
                }
                for (int p = 0; p < candidate.partCount(); p++) {
                    if (candidate.partWrecked(p)
                            || candidate.partShielded(p)
                            || !candidate.partOnField(p)
                            || !medium.reaches(weapon, candidate.partLayer(p))
                            || (first && !inCone(fromX, fromY, candidate.partX(p), candidate.partY(p), weapon))) {
                        continue;
                    }
                    double d = distanceSquared(fromX, fromY, candidate.partX(p), candidate.partY(p));
                    if (d < best) {
                        best = d;
                        piece = k;
                        part = p;
                    }
                }
            }
            boolean goesOn;
            if (piece >= 0) {
                goesOn = strikePart(m, i, piece, part, damage);
                fromX = setPieces[piece].partX(part);
                fromY = setPieces[piece].partY(part);
            } else if (unit >= 0) {
                Enemy enemy = enemies.get(unit);
                double ex = enemy.x();
                double ey = enemy.y();
                goesOn = strikeUnit(m, i, unit, enemy, damage, ex - fromX, ey - fromY);
                fromX = ex;
                fromY = ey;
            } else {
                if (first && rock(m, weapon, damage, fromX, fromY, reach)) {
                    return true;
                }
                return counts[m] > 0;
            }
            damage *= chain.falloff();
            if (!goesOn) {
                break;
            }
        }
        return true;
    }

    /**
     * The nearest unit not yet struck within {@code radius}² of (x, y) that the arc may strike (for the first strike in
     * the nose's cone too); -1 for none.
     */
    private int nearestUnit(
            int m, WeaponSpec weapon, Pool<Enemy> enemies, double x, double y, double radius, boolean first) {
        Medium medium = fire.medium();
        double nearest = radius;
        int found = -1;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (!onField(enemy)
                    || enemy.spec().seekersIgnore()
                    || (enemy.latched() && !fire.hitsLatched(m))
                    || !medium.reaches(weapon, enemy.layer())
                    || enemy.invulnerable()
                    || isStruck(enemy.serial())
                    || (first && !inCone(x, y, enemy.x(), enemy.y(), weapon))) {
                continue;
            }
            double d = distanceSquared(x, y, enemy.x(), enemy.y());
            if (d <= nearest) {
                nearest = d;
                found = j;
            }
        }
        return found;
    }

    /**
     * Strike {@code i} lands on {@code enemy} (at {@code index}) coming along (vx, vy): earthed on a mirror's face,
     * glanced off a hardened unit (both end the chain), else its damage; returns whether the chain goes on.
     */
    private boolean strikeUnit(int m, int i, int index, Enemy enemy, double damage, double vx, double vy) {
        double ex = enemy.x();
        double ey = enemy.y();
        mark(m, i, ex, ey);
        remember(enemy.serial());
        fire.hitBy(enemy.serial());
        if (enemy.spec().mirror().isPresent()
                && enemy.mirror().meets(enemy.spec().mirror().get(), vx, vy)) {
            // A standing mirror's face earths the arc: nothing, and the chain ends there.
            events.add(SimEvents.Type.MIRROR_ABSORBED, ex, ey, m);
            return false;
        }
        if (enemy.spec().hardened()) {
            // Not anti-ground: the arc glances off a hardened target and the chain ends there.
            events.add(SimEvents.Type.SHOT_GLANCED, ex, ey, m);
            return false;
        }
        if (enemy.damageFacing(damage, false, vx, vy)) {
            hits.enemyDestroyed(index, m);
        }
        return true;
    }

    /** Strike {@code i} lands on part {@code part} of set piece {@code k} (the body is struck); the chain goes on. */
    private boolean strikePart(int m, int i, int k, int part, double damage) {
        SetPiece target = setPieces[k];
        double px = target.partX(part);
        double py = target.partY(part);
        mark(m, i, px, py);
        remember(PIECE - k);
        if (target.damagePartAt(part, damage, px, py)) {
            hits.partDestroyed(k, part, m);
        }
        return true;
    }

    /** With no unit or part in reach: the nearest burning rock in reach and cone; returns whether struck. */
    private boolean rock(int m, WeaponSpec weapon, double damage, double x, double y, double reach) {
        if (eruptions == null || !fire.medium().reaches(weapon, Layer.AIR)) {
            return false;
        }
        int nearest = -1;
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (!eruptions.shootable(r) || !inCone(x, y, eruptions.rockX(r), eruptions.rockY(r), weapon)) {
                continue;
            }
            double d = distanceSquared(x, y, eruptions.rockX(r), eruptions.rockY(r));
            if (d <= reach) {
                reach = d;
                nearest = r;
            }
        }
        if (nearest < 0) {
            return false;
        }
        mark(m, 0, eruptions.rockX(nearest), eruptions.rockY(nearest));
        eruptions.damageRock(nearest, damage, events);
        return true;
    }

    /** Strike {@code i} of mount {@code m} landed on (x, y): kept for the look and reported. */
    private void mark(int m, int i, double x, double y) {
        xs[m][i] = x;
        ys[m][i] = y;
        counts[m] = i + 1;
        events.add(SimEvents.Type.CHAIN_STRIKE, x, y, event(m, i));
        events.add(SimEvents.Type.ENEMY_HIT, x, y, m);
    }

    private void remember(int code) {
        if (struckCount < struck.length) {
            struck[struckCount++] = code;
        }
    }

    private boolean isStruck(int code) {
        for (int s = 0; s < struckCount; s++) {
            if (struck[s] == code) {
                return true;
            }
        }
        return false;
    }

    /** Whether (tx, ty) lies in the nose's forward cone from (x, y): its half angle either side of straight up. */
    private static boolean inCone(double x, double y, double tx, double ty, WeaponSpec weapon) {
        double off = StrictMath.atan2(tx - x, ty - y);
        return Math.abs(off) <= weapon.coneHalfAngle();
    }
}
