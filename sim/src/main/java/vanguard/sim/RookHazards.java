package vanguard.sim;

/**
 * M7 part F (design/player/wingmen, Rook; design/campaign Level 19): where Rook's goal moves to keep clear of the
 * walking boss's flame cone while it tells or burns, and of the furnace vents that glow or burn; M7 part H: of a
 * breaching boss's bite circle and its eruption's marker; M8 part H, step H2: of a coiled boss's coil, bights and lunge
 * column and of the cruisers' told bands ({@link MawHazards}). Each Wingman holds
 * one, made once; it allocates nothing per step. After {@link #clear} the goal is ({@link #x()}, {@link #y()}).
 */
final class RookHazards {
    /** He keeps this far outside a cone's edge or a vent's box (his hull's edge to it), px. */
    static final double GAP = 8;

    /** Passes over the vents (a move clear of one row may land on another). */
    private static final int VENT_PASSES = 3;

    /**
     * M9 part C: his goal keeps this far outside a line mine's proximity, px (the 112 px gap leaves 2 px of goal; his
     * steering's lag stays inside the margin).
     */
    static final double MINE_GAP = 6;

    /** M9 part C: he keeps out of where a line mine comes down to in this many steps (1 s). */
    private static final int MINE_LOOKAHEAD_TICKS = SimStep.ticks(1);

    /** M9 part C: passes over the line mines (a cluster's discs overlap: a move clear of one lands in the next). */
    private static final int MINE_PASSES = 6;

    private final double halfW;
    private final double halfH;
    private final double limit;
    /** M8 part H: the coiled boss's and the cruisers' hazards. */
    private final MawHazards maw;

    private double gx;
    private double gy;
    /** M9 part H, step H1b: where he is now ({@link #at}). */
    private double rx;

    private double ry;

    /**
     * @param hitbox his hit box
     * @param fieldLimit his centre's margin inside the play field's edges
     */
    RookHazards(Hitbox hitbox, double fieldLimit) {
        halfW = hitbox.width() / 2;
        halfH = hitbox.height() / 2;
        limit = fieldLimit;
        maw = new MawHazards(Math.max(halfW, halfH), fieldLimit);
    }

    double x() {
        return gx;
    }

    double y() {
        return gy;
    }

    /**
     * Moves the goal (goalX, goalY) out of a telling or burning flame cone of a walking boss among {@code setPieces}
     * and out of a glowing or burning furnace vent ({@code vents}, null for none); M8 part H: then clear of a coiled
     * boss among {@code setPieces} and the told bands of {@code cruisers} (null for none); returns whether it moved.
     */
    boolean clear(double goalX, double goalY, SetPiece[] setPieces, FurnaceVents vents) {
        return clear(goalX, goalY, setPieces, vents, null);
    }

    /** As {@link #clear(double, double, SetPiece[], FurnaceVents)}, then clear of the told bands of {@code cruisers}. */
    boolean clear(double goalX, double goalY, SetPiece[] setPieces, FurnaceVents vents, CruiserRun cruisers) {
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
            if (piece.ring().isPresent() && piece.present() && !piece.destroyed()) {
                // M9 part H, step H1b: out of a ringed boss's telling or sweeping laser wedge (its rail lines never hit
                // him).
                moved |= clearOfWedges(piece);
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
        CoilArena coil = null;
        for (SetPiece piece : setPieces) {
            if (coil == null && piece.coil().isPresent() && piece.present() && !piece.destroyed()) {
                coil = piece.coil().get();
            }
        }
        if ((coil != null || cruisers != null) && maw.clear(gx, gy, coil, cruisers)) {
            // M8 part H, step H2: in the coil, out of its bights and the lunge's column, out of a told band.
            gx = maw.x();
            gy = maw.y();
            moved = true;
        }
        return moved;
    }

    /**
     * M10 part A, step A1b (design/world/jovian, Hazards & set pieces): moves the goal (goalX, goalY) sideways out of
     * every told or live lightning strike's column of {@code lightning} (his hull's edge {@link #GAP} outside it, to the
     * side the field allows that he is on now ({@link #at}; M10 part B, step B3: by the goal's side he crossed the
     * column when the ship's drift carried his slot over its middle, and was struck in it on Level 36) and out of the fan of every telling vent of {@code eruptions} (the sector its rocks
     * fly through, up the screen from the vent to their reach; sideways to the nearer edge). Either may be null. Returns
     * whether it moved; the goal is then ({@link #x()}, {@link #y()}).
     */
    boolean clearOfStorm(double goalX, double goalY, Lightning lightning, Eruptions eruptions) {
        gx = goalX;
        gy = goalY;
        boolean moved = false;
        if (lightning != null) {
            for (int i = 0; i < lightning.count(); i++) {
                if (lightning.threatens(i)) {
                    moved |= clearOfColumn(lightning.x(i), lightning.width() / 2 + halfW + GAP, rx);
                }
            }
        }
        if (eruptions != null) {
            HazardSpec.EruptionSpec spec = eruptions.spec();
            double reach = spec.reach() + Math.max(halfW, halfH) + GAP;
            double slope = Math.tan(spec.fanRadians() / 2);
            for (int v = 0; v < eruptions.vents(); v++) {
                if (!eruptions.ventPresent(v) || !eruptions.telling(v)) {
                    continue;
                }
                double dy = gy - eruptions.ventY(v);
                if (dy < -halfH - GAP || dy > reach) {
                    continue;
                }
                moved |= clearOfColumn(eruptions.ventX(v), Math.max(0, dy) * slope + halfW + GAP, gx);
            }
        }
        return moved;
    }

    /**
     * Moves the goal's x at least {@code reach} px from {@code cx}, to the side the field allows nearer {@code sideX}
     * (the goal's x, or his own for a strike's column).
     */
    private boolean clearOfColumn(double cx, double reach, double sideX) {
        if (Math.abs(gx - cx) >= reach) {
            return false;
        }
        double right = cx + reach;
        double left = cx - reach;
        boolean rightOk = right <= PlayField.WIDTH - limit;
        boolean leftOk = left >= limit;
        if (rightOk && (!leftOk || right - sideX <= sideX - left)) {
            gx = right;
        } else if (leftOk) {
            gx = left;
        } else {
            return false;
        }
        return true;
    }

    /**
     * M9 part C (design/player/wingmen, Rook in the minefield; Level 30): moves the goal (goalX, goalY) out of the
     * proximity discs of the line mines among {@code mines} (his centre {@link #MINE_GAP} outside each), sideways
     * toward {@code towardX} (the ship's x: the gap it flies) where the field allows, else straight away from the mine;
     * spores are left to his dodge. Returns whether it moved; the goal is then ({@link #x()}, {@link #y()}).
     */
    boolean clearOfMines(double goalX, double goalY, Pool<Mine> mines, double towardX) {
        gx = goalX;
        gy = goalY;
        boolean moved = false;
        for (int pass = 0; pass < MINE_PASSES; pass++) {
            boolean shifted = false;
            for (int i = 0; i < mines.size(); i++) {
                Mine mine = mines.get(i);
                if (mine.line()) {
                    // Where it comes down to in his look-ahead: he moves before the line reaches him.
                    double lowY = mine.y() + Math.min(0, mine.stepY()) * MINE_LOOKAHEAD_TICKS;
                    shifted |= clearOfMine(
                            mine.x(),
                            mine.y(),
                            lowY,
                            mine.spec().line().orElseThrow().proximity(),
                            towardX);
                }
            }
            moved |= shifted;
            if (!shifted) {
                break;
            }
        }
        return moved;
    }

    /**
     * Out of one mine's disc swept from (cx, cy) down to (cx, lowY): across it toward {@code towardX} at the goal's
     * height, else away from its centre.
     */
    private boolean clearOfMine(double cx, double cy, double lowY, double proximity, double towardX) {
        double out = proximity + MINE_GAP;
        double dx = gx - cx;
        double dy = gy - Math.clamp(gy, lowY, cy);
        if (dx * dx + dy * dy >= out * out) {
            return false;
        }
        double across = Math.sqrt(out * out - dy * dy) + 1;
        double side = towardX >= cx ? 1 : -1;
        double tx = cx + side * across;
        if (tx >= limit && tx <= PlayField.WIDTH - limit) {
            gx = tx;
            return true;
        }
        tx = cx - side * across;
        if (tx >= limit && tx <= PlayField.WIDTH - limit) {
            gx = tx;
            return true;
        }
        double d = Math.sqrt(dx * dx + dy * dy);
        double uy = d > 1e-6 ? dy / d : -1;
        gy = Math.clamp(cy + (uy >= 0 ? 1 : -1) * (out + 1), limit, PlayField.HEIGHT - limit);
        return true;
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

    /** M9 part H, step H1b: where he is now (the laser wedges ask which side of the beam he is on). */
    void at(double x, double y) {
        rx = x;
        ry = y;
    }

    /**
     * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, The escort): out of the way of each telling or sweeping
     * laser wedge of {@code piece} (its origin, its first to its last edge, its beam's length; grown by his half size,
     * the gap and half the beam's width). While it tells, a goal in the wedge turns round the origin past the edge
     * nearer to him (the first edge is safe: the beam leaves it; the last: it comes there last). While the beam sweeps:
     * ahead of the beam he goes past the last edge (else beyond its reach); behind it he stays behind it (a goal ahead
     * of the beam turns back behind it, or he holds where he is), so he never crosses the beam.
     */
    private boolean clearOfWedges(SetPiece piece) {
        boolean moved = false;
        for (int a = 0; a < piece.attackCount(); a++) {
            boolean beaming = piece.sweepBeaming(a);
            if (!piece.sweepTelegraphing(a) && !beaming) {
                continue;
            }
            EnemySpec.Sweep sweep =
                    piece.boss().orElseThrow().attacks().get(a).sweep().orElseThrow();
            double ox = piece.sweepOriginX(a);
            double oy = piece.sweepOriginY(a);
            double first = piece.sweepFrom(a);
            double dir = piece.sweepTo(a) >= first ? 1 : -1;
            double arc = Math.abs(piece.sweepTo(a) - first);
            double margin = Math.max(halfW, halfH) + GAP + sweep.width() / 2;
            double reach = sweep.length() + margin;
            double goalDistance = Math.hypot(gx - ox, gy - oy);
            double rookDistance = Math.hypot(rx - ox, ry - oy);
            double goalGrow = grow(margin, goalDistance);
            double rookGrow = grow(margin, rookDistance);
            double goalAlong = along(gx - ox, gy - oy, first, dir);
            double rookAlong = along(rx - ox, ry - oy, first, dir);
            boolean goalIn = goalDistance < reach && goalAlong >= -goalGrow && goalAlong <= arc + goalGrow;
            if (!beaming) {
                if (!goalIn) {
                    continue;
                }
                // Past the edge nearer to him, at the goal's distance.
                boolean lastNearer = rookAlong > arc / 2;
                if (!place(ox, oy, edge(first, dir, arc, goalGrow, lastNearer), goalDistance)
                        && !place(ox, oy, edge(first, dir, arc, goalGrow, !lastNearer), goalDistance)) {
                    beyond(ox, oy, reach);
                }
                moved = true;
                continue;
            }
            double beam = Math.IEEEremainder(piece.sweepBeam(a, 0) - first, 2 * StrictMath.PI) * dir;
            boolean rookBehind = rookDistance >= reach || rookAlong < beam - rookGrow;
            boolean goalBehind = goalDistance >= reach || goalAlong < beam - goalGrow;
            if (rookBehind && goalBehind) {
                continue;
            }
            if (rookBehind) {
                // The goal lies ahead of the beam: back behind it, or he holds.
                if (!place(ox, oy, first + dir * (beam - goalGrow - 0.05), goalDistance)) {
                    gx = rx;
                    gy = ry;
                }
            } else if (!place(ox, oy, edge(first, dir, arc, rookGrow, true), rookDistance)) {
                // Ahead of the beam: past its last edge, else out of its reach.
                beyond(ox, oy, reach);
            }
            moved = true;
        }
        return moved;
    }

    /** The angle {@code margin} spans at {@code distance} (a half turn when closer than it). */
    private static double grow(double margin, double distance) {
        return distance > margin ? StrictMath.asin(Math.min(1, margin / distance)) : StrictMath.PI;
    }

    /** How far the bearing of (dx, dy) lies along a sweep from its {@code first} edge in its direction {@code dir}. */
    private static double along(double dx, double dy, double first, double dir) {
        return Math.IEEEremainder(StrictMath.atan2(-dx, -dy) - first, 2 * StrictMath.PI) * dir;
    }

    /** The heading just past the sweep's last edge ({@code last}) or before its first, by {@code grow}. */
    private static double edge(double first, double dir, double arc, double grow, boolean last) {
        return last ? first + dir * (arc + grow + 0.01) : first - dir * (grow + 0.01);
    }

    /** The goal at {@code distance} from (ox, oy) along {@code heading}, if inside the field's margins. */
    private boolean place(double ox, double oy, double heading, double distance) {
        double tx = ox - Trig.sin(heading) * distance;
        double ty = oy - Trig.cos(heading) * distance;
        if (tx < limit || tx > PlayField.WIDTH - limit || ty < limit || ty > PlayField.HEIGHT - limit) {
            return false;
        }
        gx = tx;
        gy = ty;
        return true;
    }

    /** The goal beyond the beam's reach from (ox, oy), along his bearing from it, inside the margins. */
    private void beyond(double ox, double oy, double reach) {
        double dx = rx - ox;
        double dy = ry - oy;
        double distance = Math.max(1e-9, Math.hypot(dx, dy));
        gx = Math.clamp(ox + dx / distance * (reach + 1), limit, PlayField.WIDTH - limit);
        gy = Math.clamp(oy + dy / distance * (reach + 1), limit, PlayField.HEIGHT - limit);
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
