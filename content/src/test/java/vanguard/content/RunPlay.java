package vanguard.content;

import vanguard.sim.CruiserRun;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Lob;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H (Level 28's finale, design/enemies/bosses/abyssal-maw, The cruisers): the autopilot's play in the cruisers'
 * run (see {@link Autopilot}). Every move is planned ({@link #move}), as {@link VentPlay} does: of the eight ways and
 * standing still, the one whose look-ahead keeps the hull off the shells, the point defence's bolts and the flak's
 * rings, out of a told broadside band (the dearer the nearer its volley) and out of a flak marker's burst, and nearest
 * its line: the field's middle between the hulls, at {@link #CRUISE_Y}, moved off a told band to the free side. The run
 * cannot be lost (the armour floor), so this only keeps the losses down. Allocation-free and stateless.
 */
final class RunPlay {
    private RunPlay() {}

    static final int SAMPLES = 16;
    static final double SAMPLE = 0.05;
    static final double HIT = 1000;
    static final double CLEAR = 14;
    /** The cost of each sample in a told band, scaled by how far its tell has run. */
    static final double BAND = 300;
    /** The cost of each sample in a flak marker's burst, scaled by how far its fuse has burnt. */
    static final double BURST = 300;

    static final double MARGIN = 10;
    /** Its cruise height (y up), px. */
    static final double CRUISE_Y = 160;

    static final double ALONG_X = 0.3;
    static final double ALONG_Y = 0.15;
    /** M10 part I (step I2): the cost of each px off an Act 6 secret's line (as {@link VentPlay#SEEK_X}). */
    static final double SEEK_X = VentPlay.SEEK_X;

    /** Whether the cruisers' run is on: the level has one and the cruisers are up (from the rise on). */
    static boolean running(Sortie sortie) {
        if (sortie.cruisers().isEmpty()) {
            return false;
        }
        CruiserRunSpec spec = sortie.cruisers().get().spec();
        return sortie.levelSeconds() >= spec.riseAt() && sortie.levelSeconds() <= spec.endAt() + spec.fallSeconds();
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie) {
        CruiserRun run = sortie.cruisers().orElseThrow();
        CruiserRunSpec spec = run.spec();
        double targetX = (spec.leftInner() + spec.rightInner()) / 2;
        double targetY = CRUISE_Y;
        // M10 part I (step I2): from Act 6 on its secret between the hulls (Level 42's tender drifting down the right
        // half during the run: its cable, then its crate; SecretPlay, NaN before Act 6), pulled harder.
        double secretX = SecretPlay.x(sortie, sortie.ship().y());
        boolean seek = !Double.isNaN(secretX);
        if (seek) {
            // over a hull only while it is not solid (it rises, then runs: a solid hull takes the shots, the beam too)
            int tick = (int) Math.rint(sortie.levelSeconds() / SimStep.SECONDS);
            double left = run.solid(CruiserRun.LEFT, tick) ? spec.leftInner() : 0;
            double right = run.solid(CruiserRun.RIGHT, tick) ? spec.rightInner() : PlayField.WIDTH;
            targetX = Math.clamp(secretX, left, right);
            vanguard.sim.Pickup crate = SecretPlay.crate(sortie);
            if (crate != null) {
                targetY = Math.max(40, crate.renderY(1) - 10);
            }
        }
        for (int k = 0; k < CruiserRun.BANDS; k++) {
            if (run.bandTelling(k)
                    && Math.abs(targetY - run.bandY(k)) < run.bandHeight() / 2 + DarkPlay.HULL_Y + MARGIN) {
                double above = run.bandY(k) + run.bandHeight() / 2 + DarkPlay.HULL_Y + 2 * MARGIN;
                double below = run.bandY(k) - run.bandHeight() / 2 - DarkPlay.HULL_Y - 2 * MARGIN;
                targetY = below > 60
                                && Math.abs(below - sortie.ship().y())
                                        < Math.abs(above - sortie.ship().y())
                        ? below
                        : above;
            }
        }
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(
                    sortie,
                    run,
                    MovePlanner.MOVE_X[m],
                    MovePlanner.MOVE_Y[m],
                    targetX,
                    targetY,
                    seek ? SEEK_X : ALONG_X);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        double shipX = sortie.ship().x();
        if (seek && Math.abs(shipX - targetX) > VentPlay.SECRET_COLUMN) {
            // M10 part I (step I2): the look-ahead's full-speed end overshoots the secret's line as far as it falls
            // short of it (VentPlay's lifeboat rule): the way straight at it when its hazards cost no more than the
            // best move's.
            int toward = shipX < targetX ? 2 : 1;
            double hazards =
                    cost(sortie, run, MovePlanner.MOVE_X[toward], MovePlanner.MOVE_Y[toward], targetX, targetY, 0);
            if (hazards
                    <= cost(sortie, run, MovePlanner.MOVE_X[best], MovePlanner.MOVE_Y[best], targetX, targetY, 0)
                            + VentPlay.SECRET_TIE) {
                best = toward;
            }
        }
        return MovePlanner.MOVES[best];
    }

    static double cost(
            Sortie sortie, CruiserRun run, double dx, double dy, double targetX, double targetY, double alongX) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, DarkPlay.HULL_X, DarkPlay.HULL_Y));
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
            for (int b = 0; b < CruiserRun.BANDS; b++) {
                if (run.bandTelling(b)
                        && Math.abs(y - run.bandY(b)) < run.bandHeight() / 2 + DarkPlay.HULL_Y + MARGIN) {
                    cost += weight * BAND * (0.3 + run.bandProgress(b));
                }
            }
            for (int i = 0; i < sortie.lobCount(); i++) {
                Lob lob = sortie.lob(i);
                double reach = lob.impactRadius() + DarkPlay.HULL_X + MARGIN;
                if (Math.hypot(lob.targetX() - x, lob.targetY() - y) < reach) {
                    cost += weight * BURST * (0.3 + lob.progress(1));
                }
            }
        }
        cost += alongX * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        return cost;
    }
}
