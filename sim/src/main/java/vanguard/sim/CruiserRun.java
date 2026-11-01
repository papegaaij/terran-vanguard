package vanguard.sim;

import java.util.Optional;

/**
 * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The cruisers (draft); design/campaign Level 28, sections 8–10;
 * m8h-gaps.md §1.3): the finale's two Ascendancy cruisers on the level clock ({@link CruiserRunSpec}), stepped by the
 * {@link Sortie} after the set pieces fly.
 *
 * <p>They are invulnerable {@code low-air} set pieces: no HP, no bounty, no bar, no contact. From the run's start to
 * their fall's end their hulls are solid: a shot that reaches {@code low-air} and meets one sparks off ({@link
 * #blockShots}), a beam stops on it ({@link #column}). While they run they fire, every attack on the level clock from
 * the run's start: the broadsides (a told band, then a volley of shells across it from the cruiser's side; the band on
 * the ship's y and a random one in turn; later two bands at once from both flanks with a free band between them), the
 * flak (markers near the ship that burst into rings: the boss lob rule, through the enemies' lobs) and the point
 * defence (aimed bursts from the turrets along each hull's inner edge, which the Bulwark may draw). No attack starts
 * whose shots would come after the run's end. The bands' random places and the flak's scatter come from its own
 * generator (seeded from the sortie's seed), so the sortie's draws stay as they were.
 *
 * <p>Its state follows the level clock: a retry or a boss checkpoint (both before the run) starts it afresh ({@link
 * #reset}). Hashed only in a level with cruisers. Made once; stepping allocates nothing.
 */
public final class CruiserRun implements Hashed {
    /** Its generator's salt over the sortie's seed (its own stream). */
    private static final long SALT = 0x43525549534552L;
    /** The left cruiser (its side's index). */
    public static final int LEFT = 0;
    /** The right cruiser. */
    public static final int RIGHT = 1;
    /** The bands told at once at most. */
    public static final int BANDS = 2;

    private final CruiserRunSpec spec;
    private final CruiserRunSpec.Broadside broadside;
    private final CruiserRunSpec.Flak flak;
    private final CruiserRunSpec.PointDefence defence;
    private final long seed;
    private final SplitMix64 draws;
    private final EnemyGun flakGun;
    private final EnemyGun defenceGun;
    /** M10 part H, step H1b: per run, its rise, run, end and fall in level clock steps, and its flank. */
    private final int[] riseTicks;

    private final int[] runTicks;
    private final int[] endTicks;
    private final int[] fallTicksOf;
    private final CruiserRunSpec.Flank[] flanks;
    /** The first run's, as before M10 part H (the side-less views read it: Level 28's one run). */
    private final int riseTick;

    private final int runTick;
    private final int endTick;
    private final int fallTicks;
    private final int tellTicks;
    private final int broadsideTicks;
    private final int doubleTicks;
    private final int flakTicks;
    private final int fuseTicks;
    private final int defenceTicks;
    private final int gapTicks;

    /** The last level clock step it stepped at; -1 before the first. */
    private int at;

    /** Per run, its broadsides and flak volleys due so far. */
    private final int[] volleys;

    private final int[] flakVolleys;
    private final boolean[] telling = new boolean[BANDS];
    private final int[] bandSide = new int[BANDS];
    private final double[] bandY = new double[BANDS];
    private final int[] bandTicks = new int[BANDS];
    /** Per run and side ({@code 2 * run + side}), its point defence bursts due so far. */
    private final int[] bursts;

    private final int[] burstLeft = new int[2];
    private final int[] burstGap = new int[2];
    private final int[] turret = new int[2];

    CruiserRun(CruiserRunSpec spec, long seed) {
        this.spec = spec;
        this.seed = seed ^ SALT;
        broadside = spec.broadside();
        flak = spec.flak();
        defence = spec.pointDefence();
        draws = new SplitMix64(this.seed);
        flakGun = new EnemyGun(
                flak.everySeconds(),
                0,
                1,
                flak.speed(),
                flak.damage(),
                false,
                1,
                0,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Optional.empty(),
                Optional.of(
                        new EnemyGun.MortarSpec(flak.fuseSeconds(), flak.radius(), flak.ring(), flak.ringDamage())));
        defenceGun = EnemyGun.aimed(defence.everySeconds(), 0, 1, defence.speed(), defence.damage(), false);
        int runs = spec.runs().size();
        riseTicks = new int[runs];
        runTicks = new int[runs];
        endTicks = new int[runs];
        fallTicksOf = new int[runs];
        flanks = new CruiserRunSpec.Flank[runs];
        for (int i = 0; i < runs; i++) {
            CruiserRunSpec.Run run = spec.runs().get(i);
            riseTicks[i] = SimStep.ticks(run.riseAt());
            runTicks[i] = SimStep.ticks(run.runAt());
            endTicks[i] = SimStep.ticks(run.endAt());
            fallTicksOf[i] = SimStep.ticks(run.fallSeconds());
            flanks[i] = run.flank();
        }
        volleys = new int[runs];
        flakVolleys = new int[runs];
        bursts = new int[2 * runs];
        riseTick = riseTicks[0];
        runTick = runTicks[0];
        endTick = endTicks[0];
        fallTicks = fallTicksOf[0];
        tellTicks = SimStep.ticks(broadside.tellSeconds());
        broadsideTicks = Math.max(1, SimStep.ticks(broadside.everySeconds()));
        doubleTicks = Double.isFinite(broadside.doubleAfterSeconds())
                ? SimStep.ticks(broadside.doubleAfterSeconds())
                : Integer.MAX_VALUE;
        flakTicks = Math.max(1, SimStep.ticks(flak.everySeconds()));
        fuseTicks = SimStep.ticks(flak.fuseSeconds());
        defenceTicks = Math.max(1, SimStep.ticks(defence.everySeconds()));
        gapTicks = Math.max(1, SimStep.ticks(EnemyGun.BURST_GAP_SECONDS));
        reset();
    }

    /** Back to before the rise (a retry, a boss checkpoint): no tell, no burst, its generator as at the start. */
    void reset() {
        at = -1;
        java.util.Arrays.fill(volleys, 0);
        java.util.Arrays.fill(flakVolleys, 0);
        java.util.Arrays.fill(bursts, 0);
        draws.state(seed);
        for (int k = 0; k < BANDS; k++) {
            telling[k] = false;
            bandSide[k] = 0;
            bandY[k] = 0;
            bandTicks[k] = 0;
        }
        for (int s = 0; s < 2; s++) {
            burstLeft[s] = 0;
            burstGap[s] = 0;
            turret[s] = 0;
        }
    }

    /**
     * Its step at level clock step {@code levelTick} (once per new level clock step; a step the clock does not move is
     * skipped): the rise's event, then while {@code firing} (the ship flies, the level not complete) the attacks due.
     */
    void step(int levelTick, boolean firing, Ship ship, EnemyForce force, int bulletBudget, SimEvents events) {
        if (levelTick <= at) {
            return;
        }
        at = levelTick;
        boolean running = false;
        for (int i = 0; i < riseTicks.length; i++) {
            if (levelTick == riseTicks[i]) {
                // M10 part H, step H1b: a one-flank run rises on its side (value: the side; both: the middle, 0).
                CruiserRunSpec.Flank flank = flanks[i];
                double x = flank == CruiserRunSpec.Flank.BOTH
                        ? PlayField.WIDTH / 2.0
                        : flank == CruiserRunSpec.Flank.LEFT ? 0 : PlayField.WIDTH;
                events.add(SimEvents.Type.CRUISER_RISE, x, 0, flank == CruiserRunSpec.Flank.RIGHT ? RIGHT : LEFT);
            }
            running |= levelTick >= runTicks[i] && levelTick <= endTicks[i];
        }
        if (!running) {
            return;
        }
        // The tells running out first, so a volley comes its tell's steps after the tell's start.
        for (int k = 0; k < BANDS; k++) {
            if (telling[k] && --bandTicks[k] <= 0) {
                telling[k] = false;
                if (firing) {
                    fireBand(k, force.bullets(), bulletBudget, events);
                }
            }
        }
        for (int i = 0; i < runTicks.length && firing; i++) {
            if (levelTick < runTicks[i] || levelTick > endTicks[i]) {
                continue;
            }
            int r = levelTick - runTicks[i];
            int left = endTicks[i] - levelTick;
            startBroadside(i, r, left, ship, events);
            startFlak(i, r, left, ship, force, events);
            startBursts(i, r, left);
        }
        for (int s = 0; s < 2; s++) {
            if (burstLeft[s] > 0 && --burstGap[s] <= 0) {
                burstLeft[s]--;
                burstGap[s] = gapTicks;
                if (firing) {
                    double x = s == LEFT ? spec.leftInner() : spec.rightInner();
                    double y = defence.turrets().get(turret[s]);
                    force.fireFrom(x, y, defenceGun, ship);
                    events.add(SimEvents.Type.POINT_DEFENCE, x, y, s);
                }
            }
        }
    }

    /** A broadside's tell when one is due and its volley would come by the run's end. */
    private void startBroadside(int run, int r, int left, Ship ship, SimEvents events) {
        if (r < volleys[run] * broadsideTicks) {
            return;
        }
        int k = volleys[run]++;
        if (Math.max(1, tellTicks) > left) {
            return;
        }
        // Both hulls' run: the flanks in turn, the left first; a one-flank run: its own side.
        CruiserRunSpec.Flank flank = flanks[run];
        int side = flank == CruiserRunSpec.Flank.BOTH ? k % 2 : flank == CruiserRunSpec.Flank.LEFT ? LEFT : RIGHT;
        double half = broadside.band() / 2;
        double first = k % 2 == 0
                ? Math.clamp(ship.y(), half, PlayField.HEIGHT - half)
                : draws.range(half, PlayField.HEIGHT - half);
        tell(0, side, first, events);
        if (r >= doubleTicks) {
            tell(1, 1 - side, secondBand(first), events);
        }
    }

    private void tell(int k, int side, double y, SimEvents events) {
        telling[k] = true;
        bandSide[k] = side;
        bandY[k] = y;
        bandTicks[k] = Math.max(1, tellTicks);
        events.add(SimEvents.Type.BROADSIDE_TELL, side == LEFT ? 0 : PlayField.WIDTH, y, side);
    }

    /** A random band's centre at least the free band away from the band at {@code first} (edge to edge). */
    private double secondBand(double first) {
        double half = broadside.band() / 2;
        double apart = broadside.band() + broadside.freeBand();
        double lowEnd = first - apart;
        double highStart = first + apart;
        double below = Math.max(0, lowEnd - half);
        double above = Math.max(0, PlayField.HEIGHT - half - highStart);
        double pick = draws.range(0, below + above);
        return pick < below ? half + pick : highStart + (pick - below);
    }

    /** Band {@code k}'s volley: its shells across the band from its cruiser's side (the field's edge). */
    private void fireBand(int k, Pool<EnemyBullet> bullets, int budget, SimEvents events) {
        boolean fromLeft = bandSide[k] == LEFT;
        double x = fromLeft ? EnemyGun.BULLET.width() / 2 : PlayField.WIDTH - EnemyGun.BULLET.width() / 2;
        double vx = fromLeft ? broadside.speed() : -broadside.speed();
        double step = broadside.band() / broadside.shells();
        double bottom = bandY[k] - broadside.band() / 2;
        for (int i = 0; i < broadside.shells() && bullets.size() < budget; i++) {
            EnemyBullet shell = bullets.obtain();
            if (shell == null) {
                break;
            }
            shell.fire(x, bottom + (i + 0.5) * step, vx, 0, broadside.damage());
        }
        events.add(SimEvents.Type.BROADSIDE, x, bandY[k], bandSide[k]);
    }

    /** Flak's markers when due and their bursts would come by the run's end: the first on the ship. */
    private void startFlak(int run, int r, int left, Ship ship, EnemyForce force, SimEvents events) {
        if (r < flakVolleys[run] * flakTicks) {
            return;
        }
        flakVolleys[run]++;
        if (fuseTicks > left) {
            return;
        }
        Pool<Lob> lobs = force.lobs();
        for (int m = 0; m < flak.markers(); m++) {
            double tx = ship.x();
            double ty = ship.y();
            if (m > 0) {
                double angle = draws.range(0, 2 * StrictMath.PI);
                double distance = flak.scatter() * StrictMath.sqrt(draws.nextDouble());
                tx += Trig.cos(angle) * distance;
                ty += Trig.sin(angle) * distance;
            }
            tx = Math.clamp(tx, 0, PlayField.WIDTH);
            ty = Math.clamp(ty, 0, PlayField.HEIGHT);
            Lob lob = lobs.obtain();
            if (lob == null) {
                break;
            }
            // From the nearer hull's inner edge at the marker's height (the airburst's shell); a one-flank run's own.
            CruiserRunSpec.Flank flank = flanks[run];
            double from = flank == CruiserRunSpec.Flank.LEFT
                    ? spec.leftInner()
                    : flank == CruiserRunSpec.Flank.RIGHT
                            ? spec.rightInner()
                            : tx < PlayField.WIDTH / 2.0 ? spec.leftInner() : spec.rightInner();
            lob.launch(flakGun, from, ty, tx, ty);
            events.add(SimEvents.Type.FLAK, tx, ty, m);
        }
    }

    /** Each cruiser's burst when due and its last shot would come by the run's end; the right half an interval later. */
    private void startBursts(int run, int r, int left) {
        CruiserRunSpec.Flank flank = flanks[run];
        for (int s = 0; s < 2; s++) {
            if (!flank.flies(s)) {
                continue;
            }
            int due = bursts[2 * run + s] * defenceTicks
                    + (s == RIGHT && flank == CruiserRunSpec.Flank.BOTH ? defenceTicks / 2 : 0);
            if (r < due) {
                continue;
            }
            int k = bursts[2 * run + s]++;
            if ((defence.burst() - 1) * gapTicks > left || defence.burst() == 0) {
                continue;
            }
            turret[s] = k % defence.turrets().size();
            burstLeft[s] = defence.burst();
            burstGap[s] = 1;
        }
    }

    /** Whether the hulls are solid at level clock step {@code levelTick}: from the run's start to the fall's end. */
    boolean solid(int levelTick) {
        return levelTick >= runTick && levelTick <= endTick + fallTicks;
    }

    /** M10 part H, step H1b: whether the hull on {@code side} is solid at level clock step {@code levelTick}. */
    public boolean solid(int side, int levelTick) {
        int i = runOf(side, levelTick);
        return i >= 0 && levelTick >= runTicks[i] && levelTick <= endTicks[i] + fallTicksOf[i];
    }

    /**
     * M10 part H, step H1b: the run that flies the hull on {@code side} at level clock step {@code levelTick}: the one
     * on the screen then, else the last one risen before it, else its first; -1 when no run flies that side.
     */
    public int runOf(int side, int levelTick) {
        int found = -1;
        for (int i = 0; i < flanks.length; i++) {
            if (!flanks[i].flies(side)) {
                continue;
            }
            if (found < 0 || levelTick >= riseTicks[i]) {
                found = i;
            }
            if (levelTick >= riseTicks[i] && levelTick <= endTicks[i] + fallTicksOf[i]) {
                return i;
            }
        }
        return found;
    }

    /**
     * At the level clock step it last stepped at: the shots that reach {@code low-air} and meet a solid hull spark off ({@link SimEvents.Type#SHOT_GLANCED}); bombs,
     * shells and laid charges pass beneath.
     */
    void blockShots(Pool<Shot> shots, Medium medium, SimEvents events) {
        boolean leftSolid = solid(LEFT, at);
        boolean rightSolid = solid(RIGHT, at);
        if (!leftSolid && !rightSolid) {
            return;
        }
        double leftY = hullY(LEFT, at);
        double rightY = hullY(RIGHT, at);
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || weapon.delivery().laid() || !medium.reaches(weapon, Layer.LOW_AIR)) {
                continue;
            }
            if ((leftSolid && weapon.size().overlaps(shot.x(), shot.y(), spec.hull(), hullX(LEFT), leftY))
                    || (rightSolid && weapon.size().overlaps(shot.x(), shot.y(), spec.hull(), hullX(RIGHT), rightY))) {
                events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                shots.free(i);
            }
        }
    }

    /**
     * At the level clock step it last stepped at: where a beam's column (centred on {@code cx}, {@code halfWidth} either side, up from {@code fromY}) first meets a
     * solid hull; {@link Double#POSITIVE_INFINITY} for none.
     */
    double column(double cx, double halfWidth, double fromY) {
        double left = solid(LEFT, at)
                ? ShotGeometry.columnContact(cx, halfWidth, fromY, hullX(LEFT), hullY(LEFT, at), spec.hull())
                : Double.POSITIVE_INFINITY;
        double right = solid(RIGHT, at)
                ? ShotGeometry.columnContact(cx, halfWidth, fromY, hullX(RIGHT), hullY(RIGHT, at), spec.hull())
                : Double.POSITIVE_INFINITY;
        return Math.min(left, right);
    }

    /**
     * Whether a hull of half size {@code half} centred at height {@code y} is in a band being told now, with {@code margin} px
     * to spare; {@code out[0]} then holds the nearer band edge's goal height (the hull just clear of the band), {@code
     * out[1]} the other edge's.
     */
    boolean inToldBand(double y, double half, double margin, double[] out) {
        for (int k = 0; k < BANDS; k++) {
            if (!telling[k]) {
                continue;
            }
            double reach = broadside.band() / 2 + half + margin;
            double dy = y - bandY[k];
            if (Math.abs(dy) < reach) {
                out[0] = dy >= 0 ? bandY[k] + reach : bandY[k] - reach;
                out[1] = dy >= 0 ? bandY[k] - reach : bandY[k] + reach;
                return true;
            }
        }
        return false;
    }

    // --- the presentation's view -------------------------------------------------------------------

    public CruiserRunSpec spec() {
        return spec;
    }

    /** A hull's centre x: the left one's inner edge at {@code leftInner}, the right one's at {@code rightInner}. */
    public double hullX(int side) {
        return side == LEFT
                ? spec.leftInner() - spec.hull().width() / 2
                : spec.rightInner() + spec.hull().width() / 2;
    }

    /** The hulls' centre y at level clock step {@code levelTick}: the field's middle, then falling off the bottom. */
    public double hullY(int levelTick) {
        return fallenY(fallShare(levelTick));
    }

    /** The hulls' centre y at a fall's {@code share}. */
    private double fallenY(double share) {
        double middle = PlayField.HEIGHT / 2.0;
        if (share <= 0) {
            return middle;
        }
        double gone = -spec.hull().height() / 2;
        double s = share * share * (3 - 2 * share);
        return middle + (gone - middle) * s;
    }

    /** How far they have risen at level clock step {@code levelTick}, 0 (not yet) to 1 (full scale, from the run on). */
    public double riseShare(int levelTick) {
        if (levelTick < riseTick) {
            return 0;
        }
        return runTick <= riseTick ? 1 : Math.min(1, (double) (levelTick - riseTick) / (runTick - riseTick));
    }

    /** How far they have fallen at level clock step {@code levelTick}, 0 (running) to 1 (gone). */
    public double fallShare(int levelTick) {
        if (levelTick <= endTick) {
            return 0;
        }
        return fallTicks == 0 ? 1 : Math.min(1, (double) (levelTick - endTick) / fallTicks);
    }

    /** Whether they are on the screen at level clock step {@code levelTick}: from the rise to the fall's end. */
    public boolean present(int levelTick) {
        return levelTick >= riseTick && levelTick <= endTick + fallTicks;
    }

    /** M10 part H, step H1b: whether the hull on {@code side} is on the screen at level clock step {@code levelTick}. */
    public boolean present(int side, int levelTick) {
        int i = runOf(side, levelTick);
        return i >= 0 && levelTick >= riseTicks[i] && levelTick <= endTicks[i] + fallTicksOf[i];
    }

    /** M10 part H, step H1b: how far the hull on {@code side} has risen at {@code levelTick}, 0–1. */
    public double riseShare(int side, int levelTick) {
        int i = runOf(side, levelTick);
        if (i < 0 || levelTick < riseTicks[i]) {
            return 0;
        }
        return runTicks[i] <= riseTicks[i]
                ? 1
                : Math.min(1, (double) (levelTick - riseTicks[i]) / (runTicks[i] - riseTicks[i]));
    }

    /** M10 part H, step H1b: how far the hull on {@code side} has fallen at {@code levelTick}, 0 (running) to 1. */
    public double fallShare(int side, int levelTick) {
        int i = runOf(side, levelTick);
        if (i < 0 || levelTick <= endTicks[i]) {
            return 0;
        }
        return fallTicksOf[i] == 0 ? 1 : Math.min(1, (double) (levelTick - endTicks[i]) / fallTicksOf[i]);
    }

    /** M10 part H, step H1b: the hull on {@code side}'s centre y at {@code levelTick} (its run's fall). */
    public double hullY(int side, int levelTick) {
        return fallenY(fallShare(side, levelTick));
    }

    /** M10 part H, step H1b: whether the CDF frigate pair flies beside the run of the hull on {@code side} then. */
    public boolean frigates(int side, int levelTick) {
        int i = runOf(side, levelTick);
        return i >= 0 && spec.runs().get(i).frigates();
    }

    /** Whether band slot {@code k} (0 the first, 1 the double's) is being told now. */
    public boolean bandTelling(int k) {
        return telling[k];
    }

    /** Band slot {@code k}'s centre height, px. */
    public double bandY(int k) {
        return bandY[k];
    }

    /** Band slot {@code k}'s cruiser ({@link #LEFT}, {@link #RIGHT}). */
    public int bandSide(int k) {
        return bandSide[k];
    }

    /** Band slot {@code k}'s tell, 0 (started) to 1 (the volley). */
    public double bandProgress(int k) {
        return telling[k] ? 1 - (double) bandTicks[k] / Math.max(1, tellTicks) : 0;
    }

    /** A band's height, px. */
    public double bandHeight() {
        return broadside.band();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(at).add(volleys[0]).add(flakVolleys[0]).add(draws.state());
        for (int k = 0; k < BANDS; k++) {
            hash.add(telling[k] ? 1 : 0).add(bandSide[k]).add(bandY[k]).add(bandTicks[k]);
        }
        for (int s = 0; s < 2; s++) {
            hash.add(bursts[s]).add(burstLeft[s]).add(burstGap[s]).add(turret[s]);
        }
        // M10 part H, step H1b: a later run's counters (Level 28's one run hashes as before).
        for (int i = 1; i < volleys.length; i++) {
            hash.add(volleys[i]).add(flakVolleys[i]).add(bursts[2 * i]).add(bursts[2 * i + 1]);
        }
    }
}
