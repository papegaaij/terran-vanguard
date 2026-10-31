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

    private int volleys;
    private int flakVolleys;
    private final boolean[] telling = new boolean[BANDS];
    private final int[] bandSide = new int[BANDS];
    private final double[] bandY = new double[BANDS];
    private final int[] bandTicks = new int[BANDS];
    private final int[] bursts = new int[2];
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
        riseTick = SimStep.ticks(spec.riseAt());
        runTick = SimStep.ticks(spec.runAt());
        endTick = SimStep.ticks(spec.endAt());
        fallTicks = SimStep.ticks(spec.fallSeconds());
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
        volleys = 0;
        flakVolleys = 0;
        draws.state(seed);
        for (int k = 0; k < BANDS; k++) {
            telling[k] = false;
            bandSide[k] = 0;
            bandY[k] = 0;
            bandTicks[k] = 0;
        }
        for (int s = 0; s < 2; s++) {
            bursts[s] = 0;
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
        if (levelTick == riseTick) {
            events.add(SimEvents.Type.CRUISER_RISE, PlayField.WIDTH / 2.0, 0);
        }
        if (levelTick < runTick || levelTick > endTick) {
            return;
        }
        int r = levelTick - runTick;
        int left = endTick - levelTick;
        // The tells running out first, so a volley comes its tell's steps after the tell's start.
        for (int k = 0; k < BANDS; k++) {
            if (telling[k] && --bandTicks[k] <= 0) {
                telling[k] = false;
                if (firing) {
                    fireBand(k, force.bullets(), bulletBudget, events);
                }
            }
        }
        if (firing) {
            startBroadside(r, left, ship, events);
            startFlak(r, left, ship, force, events);
            startBursts(r, left);
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
    private void startBroadside(int r, int left, Ship ship, SimEvents events) {
        if (r < volleys * broadsideTicks) {
            return;
        }
        int k = volleys++;
        if (Math.max(1, tellTicks) > left) {
            return;
        }
        int side = k % 2;
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
    private void startFlak(int r, int left, Ship ship, EnemyForce force, SimEvents events) {
        if (r < flakVolleys * flakTicks) {
            return;
        }
        flakVolleys++;
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
            // From the nearer hull's inner edge at the marker's height (the airburst's shell).
            double from = tx < PlayField.WIDTH / 2.0 ? spec.leftInner() : spec.rightInner();
            lob.launch(flakGun, from, ty, tx, ty);
            events.add(SimEvents.Type.FLAK, tx, ty, m);
        }
    }

    /** Each cruiser's burst when due and its last shot would come by the run's end; the right half an interval later. */
    private void startBursts(int r, int left) {
        for (int s = 0; s < 2; s++) {
            int due = bursts[s] * defenceTicks + (s == RIGHT ? defenceTicks / 2 : 0);
            if (r < due) {
                continue;
            }
            int k = bursts[s]++;
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

    /**
     * At the level clock step it last stepped at: the shots that reach {@code low-air} and meet a solid hull spark off ({@link SimEvents.Type#SHOT_GLANCED}); bombs,
     * shells and laid charges pass beneath.
     */
    void blockShots(Pool<Shot> shots, Medium medium, SimEvents events) {
        if (!solid(at)) {
            return;
        }
        double hullY = hullY(at);
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || weapon.delivery().laid() || !medium.reaches(weapon, Layer.LOW_AIR)) {
                continue;
            }
            if (weapon.size().overlaps(shot.x(), shot.y(), spec.hull(), hullX(LEFT), hullY)
                    || weapon.size().overlaps(shot.x(), shot.y(), spec.hull(), hullX(RIGHT), hullY)) {
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
        if (!solid(at)) {
            return Double.POSITIVE_INFINITY;
        }
        double hullY = hullY(at);
        return Math.min(
                ShotGeometry.columnContact(cx, halfWidth, fromY, hullX(LEFT), hullY, spec.hull()),
                ShotGeometry.columnContact(cx, halfWidth, fromY, hullX(RIGHT), hullY, spec.hull()));
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
        double middle = PlayField.HEIGHT / 2.0;
        double share = fallShare(levelTick);
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
        hash.add(at).add(volleys).add(flakVolleys).add(draws.state());
        for (int k = 0; k < BANDS; k++) {
            hash.add(telling[k] ? 1 : 0).add(bandSide[k]).add(bandY[k]).add(bandTicks[k]);
        }
        for (int s = 0; s < 2; s++) {
            hash.add(bursts[s]).add(burstLeft[s]).add(burstGap[s]).add(turret[s]);
        }
    }
}
