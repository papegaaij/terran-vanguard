package vanguard.sim;

/**
 * M7 part F (design/player/wingmen, Rook; design/campaign Level 19): where Rook's goal moves to keep clear of the
 * walking boss's flame cone while it tells or burns, and of the furnace vents that glow or burn; M7 part H: of a
 * breaching boss's bite circle and its eruption's marker. Each Wingman holds
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
                // Round 44: out of the stomp's ring and off the rail lance's line.
                moved |= clearOfStomp(piece.stride().get().strikes());
                moved |= clearOfLance(piece.stride().get().strikes());
            }
            if (piece.breach().isPresent() && piece.present() && !piece.destroyed()) {
                // M7 part H, step H3: out of the bite circle of a telling, risen or open mouth and out of a marker.
                moved |= clearOfBreach(piece.breach().get());
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

    /**
     * M7 part H, step H3: out of a breaching boss's bite circle from its tell to its snap (at the point it breaches
     * at), and out of an eruption's marker (or the bite circle the mouth then opens with, the larger) until it erupts;
     * step SIM-K: out of the eruption's ring's full reach from the marker until the ring has expanded.
     */
    private boolean clearOfBreach(BreachArena breach) {
        BreachArena.Phase phase = breach.phase();
        if (phase == BreachArena.Phase.BURIED || phase == BreachArena.Phase.DIVE) {
            return false;
        }
        double radius = breach.marker() ? Math.max(breach.markerRadius(), breach.biteRadius()) : breach.biteRadius();
        if (breach.marker() || breach.ringing()) {
            // Step SIM-K: out of the eruption's full ring too, until it has expanded.
            radius = Math.max(radius, breach.shockReach());
        }
        return clearOfCircle(breach.mouthX(), breach.mouthY(), radius);
    }

    /** Round 44: out of a bracing or expanding stomp's full ring, straight away from its centre (or across it). */
    private boolean clearOfStomp(StrideStrikes strikes) {
        if (strikes.stompPhase() == StrideStrikes.StompPhase.IDLE) {
            return false;
        }
        return clearOfCircle(strikes.ringX(), strikes.ringY(), strikes.stompRadius());
    }

    /** Out of the circle of {@code radius} about (cx, cy) (his hull's edge {@link #GAP} outside it), straight away from its centre (or across it). */
    private boolean clearOfCircle(double cx, double cy, double radius) {
        double out = radius + Math.max(halfW, halfH) + GAP;
        double dx = gx - cx;
        double dy = gy - cy;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d >= out) {
            return false;
        }
        double ux = d > 1e-6 ? dx / d : 0;
        double uy = d > 1e-6 ? dy / d : -1;
        for (int side = 1; side >= -1; side -= 2) {
            double tx = Math.clamp(cx + ux * side * (out + 1), limit, PlayField.WIDTH - limit);
            double ty = Math.clamp(cy + uy * side * (out + 1), limit, PlayField.HEIGHT - limit);
            if ((tx - cx) * (tx - cx) + (ty - cy) * (ty - cy) >= out * out) {
                gx = tx;
                gy = ty;
                return true;
            }
        }
        return false;
    }

    /** Round 44: off a charging or firing rail lance's line, across it to the side the goal is on. */
    private boolean clearOfLance(StrideStrikes strikes) {
        if (strikes.lancePhase() == StrideStrikes.LancePhase.IDLE) {
            return false;
        }
        double ox = strikes.lanceX();
        double oy = strikes.lanceY();
        double ux = strikes.lanceDirX();
        double uy = strikes.lanceDirY();
        double clear = strikes.lanceWidth() / 2 + Math.max(halfW, halfH) + GAP;
        double along = (gx - ox) * ux + (gy - oy) * uy;
        double lateral = (gx - ox) * uy - (gy - oy) * ux;
        if (along < -clear || Math.abs(lateral) >= clear) {
            return false;
        }
        double side = lateral >= 0 ? 1 : -1;
        for (int attempt = 0; attempt < 2; attempt++) {
            double s = attempt == 0 ? side : -side;
            double tx = Math.clamp(ox + ux * along + uy * s * (clear + 1), limit, PlayField.WIDTH - limit);
            double ty = Math.clamp(oy + uy * along - ux * s * (clear + 1), limit, PlayField.HEIGHT - limit);
            if (Math.abs((tx - ox) * uy - (ty - oy) * ux) >= clear) {
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
