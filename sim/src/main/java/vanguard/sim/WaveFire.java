package vanguard.sim;

import static vanguard.sim.ShotGeometry.onField;

/**
 * M11 part A, step A1b: the Choir Resonator's wave fronts (design/player/weapons/choir-resonator, Behaviour; {@link
 * WeaponSpec.Delivery#WAVE}), for {@link PlayerFire} ({@link PlayerFire#waves()}), beside the Plasma Arc's {@link
 * ChainFire}. Each shot is a front leaving the nose: it flies straight up the screen at its speed (in the screen's
 * frame: not steered, not drifted), its arc widening evenly from its nose width to its full width at its reach, where
 * it is gone. Each step it sweeps the band between where it was and where it is, its width there:
 *
 * <ul>
 *   <li>every unit, boss body and armed mine whose box the band overlaps is met <b>once</b> by that wave, the nearest
 *       first (the box's edge the front reaches first); a body's parts are one hull (the first part reached is hit);
 *   <li>each one met spends a step: the hit at the wave's damage now, then the wave falls off to its {@link
 *       WeaponSpec.Wave#falloff()} of it; at its {@link WeaponSpec.Wave#hulls()} it is spent;
 *   <li>a closed window shell (a flake), a hardened unit (it is not {@code anti-ground}: a glance), a unit under a dome
 *       or still growing, and a standing mirror's face (it takes nothing and bounces nothing) take no damage but spend
 *       the step all the same;
 *   <li>it never touches a bullet, and reaches {@code air}, {@code low-air} and {@code ground}, never {@code high-air}
 *       or {@code sub}; under water it does the medium's share of its damage.
 * </ul>
 *
 * <p>M11 part B, step B1 (Terrain): in a level with rock each front is {@link #SLICES} slices across its width (sectors
 * of its fan); a slice whose band meets rock (a wall or an island) stops at the rock's face and is gone for the rest of
 * the wave, the rest flies on ({@link #sliceLive}); a front with every slice stopped is gone. Without rock the band is
 * whole, as before.
 *
 * <p>Each hull met raises {@link SimEvents.Type#WAVE_HIT} (its index in the wave; at most one per {@link #CLANG_SECONDS}
 * per wave, the clang) and an {@link SimEvents.Type#ENEMY_HIT}; each wave fired {@link SimEvents.Type#WAVE_FIRED}. It is
 * not a breaker. Its own fixed pool of fronts; allocates nothing.
 */
public final class WaveFire {
    /** The most fronts in flight at once (a mount fires 3.5 a second at most; each lives under 0.8 s). */
    static final int CAPACITY = 24;
    /** The least time between two clangs of one wave, s (design/player/weapons/choir-resonator: 0.12 s). */
    static final double CLANG_SECONDS = 0.12;

    /** M11 part B, step B1: the slices of a front that rock stops one by one. */
    public static final int SLICES = 16;

    private static final int ALL_STOPPED = (1 << SLICES) - 1;
    private static final int PIECE = -1;
    private static final int MOST = WeaponSpec.Wave.MOST_HULLS;
    /** The candidates a step's band may hold at once. */
    private static final int CANDIDATES = 64;

    private final PlayerFire fire;
    private final Ship ship;
    private final SetPiece[] setPieces;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    private final int clangTicks;

    private final WeaponSpec[] weapons = new WeaponSpec[CAPACITY];
    private final int[] mounts = new int[CAPACITY];
    private final double[] xs = new double[CAPACITY];
    private final double[] startYs = new double[CAPACITY];
    private final double[] travelled = new double[CAPACITY];
    private final double[] damages = new double[CAPACITY];
    private final int[] met = new int[CAPACITY];
    private final int[] clangWait = new int[CAPACITY];
    /** Per front, the slices rock stopped (a bit each); 0 without rock. */
    private final int[] stopped = new int[CAPACITY];
    /** A step's per-slice top: the band's top, the rock's face where it stops, NaN for a slice stopped before. */
    private final double[] sliceTops = new double[SLICES];
    /** Whether this step's sweep is sliced (the level has rock). */
    private boolean sliced;
    /** This step's front: its left edge and its slices' width. */
    private double sliceLeft;

    private double sliceWidth;
    /** Per front, the units (serials) and bodies ({@link #PIECE} − k) it met. */
    private final int[][] struck = new int[CAPACITY][MOST];
    /** Per front, the mines it met (by the pooled mine: a mine lives shorter than a wave). */
    private final Mine[][] minesStruck = new Mine[CAPACITY][MOST];

    private int count;
    /**
     * A step's candidates: their kind ({@link #UNIT}, {@link #PIECE} − k for a body, or {@link #MINE}). A unit or mine
     * is held by reference (a unit with its serial), never by pool index: a kill frees its slot and the pool moves its
     * last one there, so an index taken before the first meet may be stale by the next (M11 FIXA).
     */
    private final int[] candidates = new int[CANDIDATES];
    /** A unit candidate, and its serial when it was taken. */
    private final Enemy[] candidateUnits = new Enemy[CANDIDATES];

    private final int[] candidateSerials = new int[CANDIDATES];
    /** A mine candidate. */
    private final Mine[] candidateMines = new Mine[CANDIDATES];
    /** Their part (a body's), and the y of the edge the front reaches first. */
    private final int[] candidateParts = new int[CANDIDATES];

    private final double[] candidateYs = new double[CANDIDATES];
    private static final int UNIT = 0;
    private static final int MINE = 1 << 20;

    WaveFire(PlayerFire fire, Ship ship, SetPiece[] setPieces, SimEvents events, PlayerFire.Hits hits) {
        this.fire = fire;
        this.ship = ship;
        this.setPieces = setPieces;
        this.events = events;
        this.hits = hits;
        clangTicks = Math.max(1, SimStep.ticks(CLANG_SECONDS));
    }

    /** No front in flight. */
    void reset() {
        for (int i = 0; i < count; i++) {
            weapons[i] = null;
            java.util.Arrays.fill(minesStruck[i], null);
        }
        count = 0;
    }

    /** Mount {@code m} fires {@code weapon}'s wave from its nose; false with the pool full (it does not fire). */
    boolean launch(int m, WeaponSpec weapon) {
        if (count >= CAPACITY) {
            return false;
        }
        WeaponSpec.Muzzle nose = weapon.muzzles().getFirst();
        int i = count++;
        weapons[i] = weapon;
        mounts[i] = m;
        xs[i] = ship.x() + nose.dx();
        startYs[i] = ship.y() + nose.dy();
        travelled[i] = 0;
        Medium medium = fire.medium();
        damages[i] = weapon.damage() * (medium.wet() ? medium.damage(weapon) : 1);
        met[i] = 0;
        clangWait[i] = 0;
        stopped[i] = 0;
        events.add(SimEvents.Type.WAVE_FIRED, xs[i], startYs[i], m);
        return true;
    }

    /** One step: each front flies on and meets what its band sweeps; a spent or finished one is gone. */
    void step(Pool<Enemy> enemies, Pool<Mine> mines) {
        for (int i = count - 1; i >= 0; i--) {
            WeaponSpec.Wave wave = weapons[i].wave().orElseThrow();
            if (clangWait[i] > 0) {
                clangWait[i]--;
            }
            double from = travelled[i];
            double to = Math.min(wave.reach(), from + wave.speed() * SimStep.SECONDS);
            travelled[i] = to;
            double half = wave.widthAt(to) / 2;
            Canyon canyon = fire.canyon();
            sliced = canyon != null;
            if (sliced) {
                slice(i, canyon, startYs[i] + from, startYs[i] + to, half);
            }
            sweep(i, wave, enemies, mines, startYs[i] + from, startYs[i] + to, half);
            if (met[i] >= wave.hulls() || to >= wave.reach() || stopped[i] == ALL_STOPPED) {
                remove(i);
            }
        }
    }

    /**
     * M11 part B, step B1: front {@code i}'s slices this step, the band from {@code bottom} up to {@code top} {@code
     * half} px either side of its centre: each one still flying reaches up to the rock it meets (and stops there) or to
     * {@code top}.
     */
    private void slice(int i, Canyon canyon, double bottom, double top, double half) {
        sliceWidth = 2 * half / SLICES;
        sliceLeft = xs[i] - half;
        for (int k = 0; k < SLICES; k++) {
            if ((stopped[i] & (1 << k)) != 0) {
                sliceTops[k] = Double.NaN;
                continue;
            }
            double face =
                    canyon.terrain.ceiling(canyon.scroll(), sliceLeft + (k + 0.5) * sliceWidth, sliceWidth / 2, bottom);
            if (face < top) {
                stopped[i] |= 1 << k;
                sliceTops[k] = face;
            } else {
                sliceTops[k] = top;
            }
        }
    }

    /** Front {@code i} meets the hulls in the band from {@code bottom} up to {@code top}, {@code half} px either side. */
    private void sweep(
            int i,
            WeaponSpec.Wave wave,
            Pool<Enemy> enemies,
            Pool<Mine> mines,
            double bottom,
            double top,
            double half) {
        WeaponSpec weapon = weapons[i];
        Medium medium = fire.medium();
        double x = xs[i];
        int n = 0;
        for (int j = 0; j < enemies.size() && n < CANDIDATES; j++) {
            Enemy enemy = enemies.get(j);
            Hitbox box = enemy.hitbox();
            if (!medium.reaches(weapon, enemy.layer())
                    || !onField(enemy)
                    || enemy.latched()
                    || met(i, enemy.serial())
                    || !reaches(x, half, bottom, top, enemy.x(), enemy.y(), box)) {
                continue;
            }
            candidateUnits[n] = enemy;
            candidateSerials[n] = enemy.serial();
            n = add(n, UNIT, -1, enemy.y() - box.height() / 2);
        }
        for (int k = 0; k < setPieces.length && n < CANDIDATES; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present() || met(i, PIECE - k)) {
                continue;
            }
            int best = -1;
            double bestY = Double.POSITIVE_INFINITY;
            for (int p = 0; p < piece.partCount(); p++) {
                Hitbox box = piece.spec().parts().get(p).box();
                if (piece.partWrecked(p)
                        || piece.partShielded(p)
                        || !piece.partOnField(p)
                        || !medium.reaches(weapon, piece.partLayer(p))
                        || !reaches(x, half, bottom, top, piece.partX(p), piece.partY(p), box)) {
                    continue;
                }
                double edge = piece.partY(p) - box.height() / 2;
                if (edge < bestY) {
                    bestY = edge;
                    best = p;
                }
            }
            if (best >= 0) {
                n = add(n, PIECE - k, best, bestY);
            }
        }
        if (medium.reaches(weapon, Layer.AIR)) {
            for (int j = 0; j < mines.size() && n < CANDIDATES; j++) {
                Mine mine = mines.get(j);
                if (mine.armed()
                        && !metMine(i, mine)
                        && reaches(x, half, bottom, top, mine.x(), mine.y(), EnemyGun.MineSpec.BOX)) {
                    candidateMines[n] = mine;
                    n = add(n, MINE, -1, mine.y() - EnemyGun.MineSpec.BOX.height() / 2);
                }
            }
        }
        // The nearest first: the box edge the rising front reaches first (the lowest on the screen).
        for (int a = 0; a < n && met[i] < wave.hulls(); a++) {
            int pick = a;
            for (int b = a + 1; b < n; b++) {
                if (candidateYs[b] < candidateYs[pick]) {
                    pick = b;
                }
            }
            swap(a, pick);
            meet(i, wave, enemies, mines, a, x, half, bottom, top);
        }
        java.util.Arrays.fill(candidateUnits, 0, n, null);
        java.util.Arrays.fill(candidateMines, 0, n, null);
    }

    private int add(int n, int kind, int part, double y) {
        candidates[n] = kind;
        candidateParts[n] = part;
        candidateYs[n] = y;
        return n + 1;
    }

    private void swap(int a, int b) {
        int kind = candidates[a];
        int part = candidateParts[a];
        double y = candidateYs[a];
        candidates[a] = candidates[b];
        candidateParts[a] = candidateParts[b];
        candidateYs[a] = candidateYs[b];
        candidates[b] = kind;
        candidateParts[b] = part;
        candidateYs[b] = y;
        Enemy unit = candidateUnits[a];
        candidateUnits[a] = candidateUnits[b];
        candidateUnits[b] = unit;
        int serial = candidateSerials[a];
        candidateSerials[a] = candidateSerials[b];
        candidateSerials[b] = serial;
        Mine mine = candidateMines[a];
        candidateMines[a] = candidateMines[b];
        candidateMines[b] = mine;
    }

    /** Where {@code enemy} (still of {@code serial}) is in the pool now; -1 when a meet before freed it. */
    private static int indexOf(Pool<Enemy> enemies, Enemy enemy, int serial) {
        if (enemy.serial() != serial) {
            return -1;
        }
        for (int j = 0; j < enemies.size(); j++) {
            if (enemies.get(j) == enemy) {
                return j;
            }
        }
        return -1;
    }

    /** Where {@code mine} is in the pool now; -1 when a meet before freed it. */
    private static int indexOf(Pool<Mine> mines, Mine mine) {
        for (int j = 0; j < mines.size(); j++) {
            if (mines.get(j) == mine) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Front {@code i} meets candidate {@code c}: the hit (or none), its step spent, the clang. A candidate an earlier
     * meet of this step took away (a unit or mine killed and freed with it, a body wrecked or gone) is passed over and
     * spends nothing.
     */
    private void meet(
            int i,
            WeaponSpec.Wave wave,
            Pool<Enemy> enemies,
            Pool<Mine> mines,
            int c,
            double x,
            double half,
            double bottom,
            double top) {
        int kind = candidates[c];
        int part = candidateParts[c];
        int m = mounts[i];
        double damage = damages[i];
        double vy = wave.speed();
        double hx;
        double hy;
        int hull = met[i];
        if (kind == MINE) {
            Mine mine = candidateMines[c];
            int index = indexOf(mines, mine);
            if (index < 0
                    || !mine.armed()
                    || !reaches(x, half, bottom, top, mine.x(), mine.y(), EnemyGun.MineSpec.BOX)) {
                return;
            }
            hx = mine.x();
            hy = mine.y();
            minesStruck[i][hull] = mine;
            struck[i][hull] = Integer.MIN_VALUE;
            events.add(SimEvents.Type.ENEMY_HIT, hx, hy, m);
            if (mine.damage(damage)) {
                hits.mineDestroyed(index);
            }
        } else if (kind <= PIECE) {
            int k = PIECE - kind;
            SetPiece piece = setPieces[k];
            if (!piece.present() || piece.partWrecked(part)) {
                return;
            }
            hx = piece.partX(part);
            hy = piece.partY(part);
            struck[i][hull] = kind;
            if (piece.mirrors != null && piece.mirrors.meets(part, 0, vy)) {
                // A standing panel's face takes nothing and bounces nothing (no projectile to turn round).
                events.add(SimEvents.Type.MIRROR_ABSORBED, hx, hy, m);
            } else {
                events.add(SimEvents.Type.ENEMY_HIT, hx, hy, m);
                if (piece.damagePartAt(part, damage, hx, hy)) {
                    hits.partDestroyed(k, part, m);
                }
            }
        } else {
            Enemy enemy = candidateUnits[c];
            int index = indexOf(enemies, enemy, candidateSerials[c]);
            if (index < 0) {
                return;
            }
            hx = enemy.x();
            hy = enemy.y();
            struck[i][hull] = enemy.serial();
            fire.hitBy(enemy.serial());
            if (enemy.spec().mirror().isPresent()
                    && enemy.mirror().meets(enemy.spec().mirror().get(), 0, vy)) {
                events.add(SimEvents.Type.MIRROR_ABSORBED, hx, hy, m);
            } else if (enemy.window().closed() && !enemy.covered()) {
                // M11 part A (design/enemies, Window shields): a closed shell flakes it off.
                enemy.window().spark(hx, hy);
            } else if (enemy.spec().hardened() || enemy.invulnerable()) {
                // Not anti-ground: a hardened unit glances it off (its core too); a covered or growing one sparks.
                events.add(enemy.covered() ? SimEvents.Type.DOME_HIT : SimEvents.Type.SHOT_GLANCED, hx, hy, m);
            } else {
                events.add(SimEvents.Type.ENEMY_HIT, hx, hy, m);
                if (enemy.damageFacing(damage, false, 0, vy)) {
                    hits.enemyDestroyed(index, m);
                }
            }
        }
        met[i] = hull + 1;
        damages[i] = damage * wave.falloff();
        if (clangWait[i] == 0) {
            events.add(SimEvents.Type.WAVE_HIT, hx, hy, hull);
            clangWait[i] = clangTicks;
        }
    }

    private boolean met(int i, int code) {
        for (int h = 0; h < met[i]; h++) {
            if (struck[i][h] == code) {
                return true;
            }
        }
        return false;
    }

    private boolean metMine(int i, Mine mine) {
        for (int h = 0; h < met[i]; h++) {
            if (minesStruck[i][h] == mine) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether this step's band at {@code x} ± {@code half} from {@code bottom} up to {@code top} reaches {@code box} at
     * (bx, by): the whole band without rock; M11 part B, step B1: with rock, one of its slices up to that slice's top.
     */
    private boolean reaches(double x, double half, double bottom, double top, double bx, double by, Hitbox box) {
        if (!sliced) {
            return overlaps(x, half, bottom, top, bx, by, box);
        }
        for (int k = 0; k < SLICES; k++) {
            double sliceTop = sliceTops[k];
            if (sliceTop > bottom
                    && overlaps(sliceLeft + (k + 0.5) * sliceWidth, sliceWidth / 2, bottom, sliceTop, bx, by, box)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the band at {@code x} ± {@code half} from {@code bottom} up to {@code top} overlaps {@code box} at (bx, by). */
    private static boolean overlaps(
            double x, double half, double bottom, double top, double bx, double by, Hitbox box) {
        return Math.abs(bx - x) < half + box.width() / 2
                && by - box.height() / 2 < top
                && by + box.height() / 2 > bottom;
    }

    private void remove(int i) {
        count--;
        weapons[i] = weapons[count];
        mounts[i] = mounts[count];
        xs[i] = xs[count];
        startYs[i] = startYs[count];
        travelled[i] = travelled[count];
        damages[i] = damages[count];
        met[i] = met[count];
        clangWait[i] = clangWait[count];
        stopped[i] = stopped[count];
        int[] codes = struck[i];
        struck[i] = struck[count];
        struck[count] = codes;
        Mine[] hit = minesStruck[i];
        minesStruck[i] = minesStruck[count];
        minesStruck[count] = hit;
        java.util.Arrays.fill(minesStruck[count], null);
        weapons[count] = null;
    }

    /** The fronts in flight. */
    public int count() {
        return count;
    }

    /** Front {@code i}'s centre x (its x at the nose when it fired), px. */
    public double x(int i) {
        return xs[i];
    }

    /** Front {@code i}'s y now, px. */
    public double y(int i) {
        return startYs[i] + travelled[i];
    }

    /** Front {@code i}'s width now, px. */
    public double width(int i) {
        return weapons[i].wave().orElseThrow().widthAt(travelled[i]);
    }

    /** How far front {@code i} has flown of its reach, 0–1 (it fades over the last fifth). */
    public double progress(int i) {
        return travelled[i] / weapons[i].wave().orElseThrow().reach();
    }

    /** The hulls front {@code i} has met (each dims the arc a step). */
    public int met(int i) {
        return met[i];
    }

    /**
     * M11 part B, step B1: whether slice {@code k} (0 at the left, of {@link #SLICES}) of front {@code i} still flies
     * (rock stops a slice it meets; always without rock).
     */
    public boolean sliceLive(int i, int k) {
        return (stopped[i] & (1 << k)) == 0;
    }

    /** The mount that fired front {@code i}. */
    public int mount(int i) {
        return mounts[i];
    }

    void addTo(StateHash hash) {
        hash.add(count);
        for (int i = 0; i < count; i++) {
            hash.add(mounts[i])
                    .add(xs[i])
                    .add(startYs[i])
                    .add(travelled[i])
                    .add(damages[i])
                    .add(met[i])
                    .add(stopped[i]);
        }
    }
}
