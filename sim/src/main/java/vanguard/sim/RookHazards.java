package vanguard.sim;

/**
 * M7 part F (design/player/wingmen, Rook; design/campaign Level 19): where Rook's goal moves to keep clear of the
 * walking boss's flame cone while it tells or burns, and of the furnace vents that glow or burn. Each Wingman holds
 * one, made once; it allocates nothing per step. After {@link #clear} the goal is ({@link #x()}, {@link #y()}).
 */
final class RookHazards {
    /** He keeps this far outside a cone's edge or a vent's box (his hull's edge to it), px. */
    static final double GAP = 8;

    /** Passes over the vents (a move clear of one row may land on another). */
    private static final int VENT_PASSES = 3;

    private final double halfW;
    private final double halfH;
    private final double limit;
    private double gx;
    private double gy;

    /**
     * @param hitbox his hit box
     * @param fieldLimit his centre's margin inside the play field's edges
     */
    RookHazards(Hitbox hitbox, double fieldLimit) {
        halfW = hitbox.width() / 2;
        halfH = hitbox.height() / 2;
        limit = fieldLimit;
    }

    double x() {
        return gx;
    }

    double y() {
        return gy;
    }

    /**
     * Moves the goal (goalX, goalY) out of a telling or burning flame cone of a walking boss among {@code setPieces}
     * and out of a glowing or burning furnace vent ({@code vents}, null for none); returns whether it moved.
     */
    boolean clear(double goalX, double goalY, SetPiece[] setPieces, FurnaceVents vents) {
        gx = goalX;
        gy = goalY;
        boolean moved = false;
        for (SetPiece piece : setPieces) {
            if (piece.stride().isPresent() && piece.present() && !piece.destroyed()) {
                moved |= clearOfCone(piece);
            }
        }
        if (vents != null) {
            for (int pass = 0; pass < VENT_PASSES; pass++) {
                boolean shifted = clearOfVents(vents);
                moved |= shifted;
                if (!shifted) {
                    break;
                }
            }
        }
        return moved;
    }

    /**
     * Out of the cone of {@code piece} while it tells or burns: across its axis to the side the goal is on (the other
     * side, or past its tip, when the play field's margins leave no room there).
     */
    private boolean clearOfCone(SetPiece piece) {
        StrideArena stride = piece.stride().get();
        if (stride.conePhase() == StrideArena.ConePhase.IDLE) {
            return false;
        }
        BossSpec.Cone cone = piece.boss().orElseThrow().cone().orElseThrow();
        double ax = stride.coneX();
        double ay = stride.coneY();
        double ux = -Trig.sin(stride.coneHeading());
        double uy = -Trig.cos(stride.coneHeading());
        double tan = Trig.sin(cone.halfAngleRadians()) / Trig.cos(cone.halfAngleRadians());
        double margin = Math.max(halfW, halfH) + GAP;
        if (!inCone(gx, gy, ax, ay, ux, uy, cone.length(), tan, margin)) {
            return false;
        }
        double along = (gx - ax) * ux + (gy - ay) * uy;
        double lateral = (gx - ax) * uy - (gy - ay) * ux;
        double reach = Math.max(0, along) * tan + margin + 1;
        double side = lateral >= 0 ? 1 : -1;
        for (int attempt = 0; attempt < 3; attempt++) {
            double tx;
            double ty;
            if (attempt < 2) {
                double s = attempt == 0 ? side : -side;
                tx = ax + ux * along + uy * s * reach;
                ty = ay + uy * along - ux * s * reach;
            } else {
                double past = cone.length() + margin + 1;
                tx = ax + ux * past;
                ty = ay + uy * past;
            }
            tx = Math.clamp(tx, limit, PlayField.WIDTH - limit);
            ty = Math.clamp(ty, limit, PlayField.HEIGHT - limit);
            if (!inCone(tx, ty, ax, ay, ux, uy, cone.length(), tan, margin)) {
                gx = tx;
                gy = ty;
                return true;
            }
        }
        return false;
    }

    /** Whether (px, py) lies within {@code margin} of the cone from (ax, ay) along (ux, uy). */
    private static boolean inCone(
            double px,
            double py,
            double ax,
            double ay,
            double ux,
            double uy,
            double length,
            double tan,
            double margin) {
        double dx = px - ax;
        double dy = py - ay;
        double along = dx * ux + dy * uy;
        if (along < -margin || along > length + margin) {
            return false;
        }
        return Math.abs(dx * uy - dy * ux) <= Math.max(0, along) * tan + margin;
    }

    /**
     * Out of every glowing or burning grate's box (grown by his half size and the gap): above or below its row,
     * whichever is nearer and inside the play field's margins. Returns whether it moved.
     */
    private boolean clearOfVents(FurnaceVents vents) {
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.present(g) || !vents.hot(g)) {
                continue;
            }
            Hitbox box = vents.box(g);
            double reachX = box.width() / 2 + halfW + GAP;
            double reachY = box.height() / 2 + halfH + GAP;
            double vx = vents.x(g);
            double vy = vents.y(g);
            if (Math.abs(gx - vx) >= reachX || Math.abs(gy - vy) >= reachY) {
                continue;
            }
            double above = vy + reachY;
            double below = vy - reachY;
            boolean aboveOk = above <= PlayField.HEIGHT - limit;
            boolean belowOk = below >= limit;
            if (belowOk && (!aboveOk || gy - below <= above - gy)) {
                gy = below;
            } else if (aboveOk) {
                gy = above;
            } else {
                continue;
            }
            return true;
        }
        return false;
    }
}
