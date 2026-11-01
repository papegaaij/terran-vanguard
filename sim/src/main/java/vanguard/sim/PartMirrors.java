package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, the mirror batteries; design/enemies, Reflect; m10h-gaps.md §1
 * #4): a boss's mirror panels, one per mirrored part ({@link PartMirror}), on the same rule and math as a unit's mirror
 * ({@link Mirroring}'s shared helpers). A panel rests facing its own side and turns toward the ship within its turn
 * either way at its mirror's turn rate (holding still while its battery tells or bursts); it meets a shot arriving
 * within its arc ({@link Reflections#meetPart}: the player's own shots bounce back as its magenta bullets, one per gap,
 * the rest and Rook's absorbed), a beam heats it until it breaks, a {@code shield-breaker} arc or the EMP Burst
 * shatters it; down its down time, then up again over its regrowth (the shimmer). Shots from above (the Airstrike, the
 * Smart Bomb, the Lance) and blasts pass it. A dead part's panel meets nothing.
 *
 * <p>Its events, at the part: {@link SimEvents.Type#PART_MIRROR_BROKEN} (a beam's heat), {@link
 * SimEvents.Type#PART_MIRROR_SHATTERED} (a breaker's arc, the EMP), {@link SimEvents.Type#PART_MIRROR_REGROWING} (its
 * shimmer starts); value the part. Kept by its {@link SetPiece}, made only for a boss with part mirrors and hashed only
 * then. Allocates nothing once made.
 */
public final class PartMirrors {
    private final SetPiece piece;
    private final List<PartMirror> specs;
    /** Per part, its panel's index in {@link #specs}; -1 for a part without one. */
    private final int[] index;

    private final double[] facing;
    private final double[] heat;
    private final int[] coolWait;
    private final int[] down;
    private final int[] regrow;
    private final int[] gap;
    /** Per part, whether its panel holds still this step (its battery tells or bursts); set by the boss each step. */
    private final boolean[] holding;
    /** The velocity {@link #reflect} worked out last (scratch, not state). */
    private double reflectedVx;

    private double reflectedVy;
    /** Where its events go (the sortie's); a sink that keeps none until the boss is wired into a sortie. */
    private SimEvents events = new SimEvents(0);

    PartMirrors(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        specs = boss.mirrors();
        index = new int[parts];
        java.util.Arrays.fill(index, -1);
        for (int i = 0; i < specs.size(); i++) {
            index[specs.get(i).part()] = i;
        }
        facing = new double[parts];
        heat = new double[parts];
        coolWait = new int[parts];
        down = new int[parts];
        regrow = new int[parts];
        gap = new int[parts];
        holding = new boolean[parts];
        reset();
    }

    /** Whether {@code boss} has part mirrors (then its set piece keeps a {@link PartMirrors}). */
    static boolean any(BossSpec boss) {
        return boss != null && !boss.mirrors().isEmpty();
    }

    /** The sortie's events, where its events go. */
    void events(SimEvents sortieEvents) {
        events = sortieEvents;
    }

    /** Back to the level start (and the boss checkpoint): every panel whole, cold, at its rest facing. */
    void reset() {
        for (int p = 0; p < index.length; p++) {
            facing[p] = index[p] < 0 ? 0 : specs.get(index[p]).facingRadians();
            heat[p] = 0;
            coolWait[p] = 0;
            down[p] = 0;
            regrow[p] = 0;
            gap[p] = 0;
            holding[p] = false;
        }
    }

    /**
     * One engaged step with the ship at (shipX, shipY): each living panel's timers (the bounce gap, the cooling, its
     * down time and regrowth) and its turn toward the ship, within its turn of its rest facing, unless it holds.
     */
    void step(double shipX, double shipY) {
        for (int p = 0; p < index.length; p++) {
            if (index[p] < 0 || piece.partHp(p) <= 0) {
                continue;
            }
            PartMirror spec = specs.get(index[p]);
            EnemySpec.Mirror mirror = spec.mirror();
            if (gap[p] > 0) {
                gap[p]--;
            }
            if (heat[p] > 0) {
                if (coolWait[p] >= SimStep.ticks(mirror.coolDelaySeconds())) {
                    heat[p] = Mirroring.cooled(mirror, heat[p]);
                } else {
                    coolWait[p]++;
                }
            }
            if (down[p] > 0 && --down[p] == 0) {
                regrow[p] = SimStep.ticks(mirror.regrowSeconds());
                events.add(SimEvents.Type.PART_MIRROR_REGROWING, piece.partX(p), piece.partY(p), p);
            } else if (regrow[p] > 0) {
                regrow[p]--;
            }
            if (holding[p]) {
                continue;
            }
            double rest = spec.facingRadians();
            double bearing = Enemy.heading(shipX - piece.partX(p), shipY - piece.partY(p));
            double most = StrictMath.toRadians(spec.turnDegrees());
            double target = rest + Math.clamp(Math.IEEEremainder(bearing - rest, 2 * StrictMath.PI), -most, most);
            facing[p] = Mirroring.turned(
                    facing[p], target, StrictMath.toRadians(mirror.turnDegreesPerSecond()) * SimStep.SECONDS);
        }
    }

    /** Part {@code p}'s panel holds still ({@code hold}) or turns again: its battery tells or bursts. */
    void hold(int p, boolean hold) {
        holding[p] = hold;
    }

    /** Whether part {@code p} stands behind a panel. */
    public boolean fitted(int p) {
        return index[p] >= 0;
    }

    /** Whether part {@code p}'s panel stands (living, not broken, shattered or regrowing): only then it meets shots. */
    public boolean standing(int p) {
        return index[p] >= 0 && piece.partHp(p) > 0 && down[p] == 0 && regrow[p] == 0;
    }

    /** Whether part {@code p}'s standing panel meets a shot flying at (vx, vy) px/s (y up): within its arc. */
    boolean meets(int p, double vx, double vy) {
        return standing(p)
                && Mirroring.faces(facing[p], specs.get(index[p]).mirror().halfArcRadians(), vx, vy);
    }

    /** Whether part {@code p}'s panel faces within its fire angle of the ship at (shipX, shipY) (its battery may fire). */
    public boolean aimed(int p, double shipX, double shipY) {
        return index[p] < 0
                || Mirroring.within(
                        facing[p],
                        Enemy.heading(shipX - piece.partX(p), shipY - piece.partY(p)),
                        specs.get(index[p]).mirror().fireWithinDegrees());
    }

    /** Whether part {@code p}'s panel may bounce now (its gap since the last bounce has passed). */
    boolean bounceReady(int p) {
        return gap[p] == 0;
    }

    /** A shot flying at (vx, vy) bounces off part {@code p}'s panel: read with {@link #reflectedVx()}; the gap starts. */
    void reflect(int p, double vx, double vy) {
        EnemySpec.Mirror mirror = specs.get(index[p]).mirror();
        reflectedVx = Mirroring.reflectedX(facing[p], vx, vy, mirror.bulletSpeed());
        reflectedVy = Mirroring.reflectedY(facing[p], vx, vy, mirror.bulletSpeed());
        gap[p] = SimStep.ticks(mirror.gapSeconds());
    }

    double reflectedVx() {
        return reflectedVx;
    }

    double reflectedVy() {
        return reflectedVy;
    }

    /** Part {@code p}'s bounced bullet's damage. */
    double bulletDamage(int p) {
        return specs.get(index[p]).mirror().bulletDamage();
    }

    /** A beam's touch heats part {@code p}'s standing panel by {@code amount} (no damage); at its heat it breaks. */
    boolean heat(int p, double amount) {
        if (!standing(p)) {
            return false;
        }
        heat[p] += amount;
        coolWait[p] = 0;
        if (heat[p] < specs.get(index[p]).mirror().heat()) {
            return false;
        }
        knockDown(p, SimEvents.Type.PART_MIRROR_BROKEN);
        return true;
    }

    /** A {@code shield-breaker} arc shatters part {@code p}'s standing panel at once. */
    void shatter(int p) {
        if (standing(p)) {
            knockDown(p, SimEvents.Type.PART_MIRROR_SHATTERED);
        }
    }

    /** The EMP Burst shatters every standing panel of a touchable part; returns how many. */
    int shatterAll() {
        int shattered = 0;
        for (int p = 0; p < index.length; p++) {
            if (standing(p) && !piece.partShielded(p)) {
                knockDown(p, SimEvents.Type.PART_MIRROR_SHATTERED);
                shattered++;
            }
        }
        return shattered;
    }

    private void knockDown(int p, SimEvents.Type type) {
        heat[p] = 0;
        coolWait[p] = 0;
        down[p] = Math.max(1, SimStep.ticks(specs.get(index[p]).mirror().downSeconds()));
        regrow[p] = 0;
        events.add(type, piece.partX(p), piece.partY(p), p);
    }

    /** Part {@code p}'s panel's facing, radians clockwise from straight down. */
    public double facing(int p) {
        return facing[p];
    }

    /** Part {@code p}'s panel's heat (the look: dull red, orange, then white; cracks from 70 % of its heat). */
    public double heat(int p) {
        return heat[p];
    }

    /** Part {@code p}'s panel's heat as a share of its breaking heat, 0–1; 0 without a panel. */
    public double heatShare(int p) {
        return index[p] < 0
                ? 0
                : Math.min(1, heat[p] / specs.get(index[p]).mirror().heat());
    }

    /** Whether part {@code p}'s panel is down (broken or shattered, before its regrowth). */
    public boolean down(int p) {
        return down[p] > 0;
    }

    /** Steps left of part {@code p}'s regrowth (the silver shimmer); 0 when it stands or is down. */
    public int regrowTicks(int p) {
        return regrow[p];
    }

    void addTo(StateHash hash) {
        for (int p = 0; p < index.length; p++) {
            if (index[p] >= 0) {
                hash.add(facing[p])
                        .add(heat[p])
                        .add(coolWait[p])
                        .add(down[p])
                        .add(regrow[p])
                        .add(gap[p])
                        .add(holding[p] ? 1 : 0);
            }
        }
    }
}
