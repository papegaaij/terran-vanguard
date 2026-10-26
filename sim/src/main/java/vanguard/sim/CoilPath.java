package vanguard.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * Round 58 F.2 (step m8i1, design/enemies/bosses/abyssal-maw, *Arena* and *The dive*): a coiled boss's centre line from
 * its head to its tail tip, with rounded turns. The neck rises from the head (at rest, facing down) straight to the
 * first leg's height and bends into it; every corner of the body's path is a circular arc of the body's {@code bend}
 * radius (less where a leg is too short for it), so the line has no corner and its tangent turns smoothly. Beyond the
 * path's last point it runs on straight (and before the head, straight on ahead of it).
 *
 * <p>Places along it are its <i>chain distance</i> from the head, px; {@link #at(double)} puts a place and its tangent
 * (toward the tail) into its fields without allocating.
 */
final class CoilPath {
    // Its pieces (lines and arcs in turn): where each starts along the line, its first place and heading, an arc's
    // centre and its turn per px.
    private final double[] pieceStart;
    private final double[] pieceX;
    private final double[] pieceY;
    private final double[] pieceUx;
    private final double[] pieceUy;
    private final double[] pieceCx;
    private final double[] pieceCy;
    private final double[] pieceTurn;
    private final double endX;
    private final double endY;
    private final double length;
    private final double neck;
    // The turns (the neck's first, if it has one): centre, radius, the legs' directions in and out.
    private final double[] turnX;
    private final double[] turnY;
    private final double[] turnR;
    private final double[] inX;
    private final double[] inY;
    private final double[] outX;
    private final double[] outY;
    private final boolean neckTurn;

    /** {@link #at}'s place and unit tangent (toward the tail). */
    double x;

    double y;
    double ux;
    double uy;

    /**
     * The line from the head at ({@code headX}, {@code headY}) through {@code path} (the body's centre line from the
     * head's end), its turns {@code bend} px in radius.
     */
    CoilPath(double headX, double headY, List<BossSpec.Point> path, double bend) {
        if (!(bend >= 0)) {
            throw new IllegalArgumentException("a coil bends with a radius of 0 or more");
        }
        BossSpec.Point first = path.getFirst();
        List<double[]> points = new ArrayList<>();
        points.add(new double[] {headX, headY});
        // The neck: up (or down) from the head to the first leg's height, then into it.
        boolean neckCorner = headX != first.x() && headY != first.y();
        if (neckCorner) {
            points.add(new double[] {headX, first.y()});
        }
        for (BossSpec.Point p : path) {
            double[] last = points.getLast();
            if (p.x() != last[0] || p.y() != last[1]) {
                points.add(new double[] {p.x(), p.y()});
            }
        }
        // Collinear points are no corners.
        for (int i = points.size() - 2; i >= 1; i--) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            double[] c = points.get(i + 1);
            double cross = (b[0] - a[0]) * (c[1] - b[1]) - (b[1] - a[1]) * (c[0] - b[0]);
            double dot = (b[0] - a[0]) * (c[0] - b[0]) + (b[1] - a[1]) * (c[1] - b[1]);
            if (Math.abs(cross) < 1e-9 && dot > 0) {
                points.remove(i);
            }
        }
        int m = points.size();
        int turns = Math.max(0, m - 2);
        turnX = new double[turns];
        turnY = new double[turns];
        turnR = new double[turns];
        inX = new double[turns];
        inY = new double[turns];
        outX = new double[turns];
        outY = new double[turns];
        double[] cut = new double[turns];
        double[] sweep = new double[turns];
        double used = 0;
        for (int k = 0; k < turns; k++) {
            double[] a = points.get(k);
            double[] b = points.get(k + 1);
            double[] c = points.get(k + 2);
            double inLength = Math.hypot(b[0] - a[0], b[1] - a[1]);
            double outLength = Math.hypot(c[0] - b[0], c[1] - b[1]);
            inX[k] = (b[0] - a[0]) / inLength;
            inY[k] = (b[1] - a[1]) / inLength;
            outX[k] = (c[0] - b[0]) / outLength;
            outY[k] = (c[1] - b[1]) / outLength;
            double cross = inX[k] * outY[k] - inY[k] * outX[k];
            double angle = StrictMath.atan2(Math.abs(cross), inX[k] * outX[k] + inY[k] * outY[k]);
            double tan = StrictMath.tan(angle / 2);
            double radius = bend;
            if (k == 0 && neckCorner) {
                // The neck bends into the first leg at the body's first point at the latest.
                radius = Math.min(radius, Math.hypot(first.x() - b[0], first.y() - b[1]) / tan);
            }
            double free = Math.min(inLength - used, k == turns - 1 ? outLength : outLength / 2);
            double t = Math.max(0, Math.min(radius * tan, free));
            cut[k] = t;
            sweep[k] = cross >= 0 ? angle : -angle;
            turnR[k] = tan > 0 ? t / tan : 0;
            // The centre: from the arc's first tangent point, toward the inside of the turn.
            double side = cross >= 0 ? 1 : -1;
            turnX[k] = b[0] - inX[k] * t - inY[k] * turnR[k] * side;
            turnY[k] = b[1] - inY[k] * t + inX[k] * turnR[k] * side;
            used = t;
        }
        neckTurn = neckCorner && turns > 0;
        // The pieces: a line to each turn's first tangent point, its arc, ..., the last line.
        double total = 0;
        double neckLength = Double.NaN;
        double[] lineFromX = new double[turns + 1];
        double[] lineFromY = new double[turns + 1];
        double[] lineLength = new double[turns + 1];
        double[] arcLength = new double[turns];
        double fromX = headX;
        double fromY = headY;
        for (int k = 0; k <= turns; k++) {
            double[] b = points.get(k + 1);
            double dx = k < turns ? inX[k] : 0;
            double dy = k < turns ? inY[k] : 0;
            double toX = k < turns ? b[0] - dx * cut[k] : b[0];
            double toY = k < turns ? b[1] - dy * cut[k] : b[1];
            lineFromX[k] = fromX;
            lineFromY[k] = fromY;
            lineLength[k] = Math.hypot(toX - fromX, toY - fromY);
            total += lineLength[k];
            if (k < turns) {
                arcLength[k] = turnR[k] * Math.abs(sweep[k]);
                total += arcLength[k];
                fromX = b[0] + outX[k] * cut[k];
                fromY = b[1] + outY[k] * cut[k];
                if (k == 0 && neckTurn) {
                    neckLength = total + Math.hypot(first.x() - fromX, first.y() - fromY);
                }
            }
        }
        length = total;
        neck = Double.isNaN(neckLength) ? Math.hypot(first.x() - headX, first.y() - headY) : neckLength;
        // The pieces in order: line 0, arc 0, line 1, arc 1, ..., line `turns`.
        int pieces = 2 * turns + 1;
        pieceStart = new double[pieces];
        pieceX = new double[pieces];
        pieceY = new double[pieces];
        pieceUx = new double[pieces];
        pieceUy = new double[pieces];
        pieceCx = new double[pieces];
        pieceCy = new double[pieces];
        pieceTurn = new double[pieces];
        double start = 0;
        for (int piece = 0; piece < pieces; piece++) {
            int k = piece / 2;
            pieceStart[piece] = start;
            if (piece % 2 == 0) {
                pieceX[piece] = lineFromX[k];
                pieceY[piece] = lineFromY[k];
                pieceUx[piece] = k < turns ? inX[k] : (turns > 0 ? outX[turns - 1] : unit(points, 0));
                pieceUy[piece] = k < turns ? inY[k] : (turns > 0 ? outY[turns - 1] : unitY(points, 0));
                start += lineLength[k];
            } else {
                pieceX[piece] = points.get(k + 1)[0] - inX[k] * cut[k];
                pieceY[piece] = points.get(k + 1)[1] - inY[k] * cut[k];
                pieceUx[piece] = inX[k];
                pieceUy[piece] = inY[k];
                pieceCx[piece] = turnX[k];
                pieceCy[piece] = turnY[k];
                // Radians turned per px along it (signed).
                pieceTurn[piece] = turnR[k] > 0 ? Math.signum(sweep[k]) / turnR[k] : 0;
                start += arcLength[k];
            }
        }
        endX = points.getLast()[0];
        endY = points.getLast()[1];
    }

    private static double unit(List<double[]> points, int k) {
        double[] a = points.get(k);
        double[] b = points.get(k + 1);
        return (b[0] - a[0]) / Math.hypot(b[0] - a[0], b[1] - a[1]);
    }

    private static double unitY(List<double[]> points, int k) {
        double[] a = points.get(k);
        double[] b = points.get(k + 1);
        return (b[1] - a[1]) / Math.hypot(b[0] - a[0], b[1] - a[1]);
    }

    /** The line's length from the head to the path's last point, px. */
    double length() {
        return length;
    }

    /** The chain distance from the head to the path's first point (where the body's first segment rests), px. */
    double neck() {
        return neck;
    }

    /** The place {@code c} px along the line from the head (straight on past its ends) and its tangent: {@link #x}... */
    void at(double c) {
        if (c >= length) {
            int last = pieceStart.length - 1;
            ux = pieceUx[last];
            uy = pieceUy[last];
            x = endX + ux * (c - length);
            y = endY + uy * (c - length);
            return;
        }
        int piece = 0;
        while (piece + 1 < pieceStart.length && c >= pieceStart[piece + 1]) {
            piece++;
        }
        double into = c - pieceStart[piece];
        double turn = pieceTurn[piece];
        if (turn == 0) {
            ux = pieceUx[piece];
            uy = pieceUy[piece];
            x = pieceX[piece] + ux * into;
            y = pieceY[piece] + uy * into;
            return;
        }
        double phi = into * turn;
        double cos = Trig.cos(phi);
        double sin = Trig.sin(phi);
        double rx = pieceX[piece] - pieceCx[piece];
        double ry = pieceY[piece] - pieceCy[piece];
        x = pieceCx[piece] + rx * cos - ry * sin;
        y = pieceCy[piece] + rx * sin + ry * cos;
        ux = pieceUx[piece] * cos - pieceUy[piece] * sin;
        uy = pieceUx[piece] * sin + pieceUy[piece] * cos;
    }

    /** The turns (the neck's first when {@link #neckTurn()}). */
    int turnCount() {
        return turnR.length;
    }

    boolean neckTurn() {
        return neckTurn;
    }

    /** Turn {@code k}'s centre, px. */
    double turnX(int k) {
        return turnX[k];
    }

    double turnY(int k) {
        return turnY[k];
    }

    /** Turn {@code k}'s radius, px (0: a sharp corner where a leg had no room). */
    double turnRadius(int k) {
        return turnR[k];
    }

    /** The direction of the leg into turn {@code k} (toward the tail). */
    double inX(int k) {
        return inX[k];
    }

    double inY(int k) {
        return inY[k];
    }

    /** The direction of the leg out of turn {@code k}. */
    double outX(int k) {
        return outX[k];
    }

    double outY(int k) {
        return outY[k];
    }
}
