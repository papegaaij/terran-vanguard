package vanguard.sim;

import java.util.List;

/**
 * A set piece's parts (out of {@link SetPiece}): their HP and hit clocks, which of them take damage now, where they lie
 * (their pose offsets, a lifted boss's stump, a walking boss's turn, a breaching boss's vents, a coiled boss's
 * implants), what touches them and the damage they take. Its arrays are allocated once; the coordinator hashes them.
 */
final class SetPieceParts {
    private final SetPiece piece;
    private final SetPieceSpec spec;
    private final BossSpec boss;
    private final BossChains chains;
    private final BossWindows windows;
    private final BossHatches hatches;
    private final SlamArena arena;
    private final StrideArena stride;
    private final BreachArena breach;
    private final CoilArena coil;

    final double[] partHp;
    final int[] partTicksSinceHit;
    /** Per part, whether it is a fire-only part (never damaged, out of the bar). */
    final boolean[] armoured;
    /** The parts out of the boss bar: the fire-only ones (M7 part H: and a breaching boss's spine vents). */
    private final boolean[] outOfBar;

    private final double maxHp;
    private final int[] exposedFrom;
    /** Per pose and part, its offset from the centre on the play plane. */
    final double[][] poseDx;

    final double[][] poseDy;
    final Hitbox[] poseBody;

    SetPieceParts(SetPiece piece, SetPieceSpec spec, BossSpec boss) {
        this.piece = piece;
        this.spec = spec;
        this.boss = boss;
        chains = piece.chains;
        windows = piece.windows;
        hatches = piece.hatches;
        arena = piece.arena;
        stride = piece.stride;
        breach = piece.breach;
        coil = piece.coil;
        int parts = spec.parts().size();
        partHp = new double[parts];
        partTicksSinceHit = new int[parts];
        armoured = new boolean[parts];
        if (boss != null) {
            for (int p : boss.armoured()) {
                armoured[p] = true;
            }
        }
        outOfBar = armoured.clone();
        if (boss != null && boss.coil().isPresent()) {
            // M8 part H: a coiled boss's gill sacs and implants are out of the bar (the implants count on the head's).
            CoilSpec coiled = boss.coil().get();
            for (int p : coiled.sacs()) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
            for (int p : coiled.implants().map(CoilSpec.Implants::parts).orElse(List.of())) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
        }
        if (boss != null && boss.vents().isPresent()) {
            // M7 part H, step H3: the spine vents are out of the bar.
            for (int p : boss.vents().get().parts()) {
                if (p < parts) {
                    outOfBar[p] = true;
                }
            }
        }
        double hp = 0;
        for (int p = 0; p < parts; p++) {
            hp += outOfBar[p] ? 0 : spec.parts().get(p).hp();
        }
        maxHp = hp;
        exposedFrom = new int[parts];
        int poses = boss == null || boss.poses().isEmpty() ? 1 : boss.poses().size();
        poseDx = new double[poses][parts];
        poseDy = new double[poses][parts];
        poseBody = new Hitbox[poses];
        for (int k = 0; k < poses; k++) {
            BossSpec.Pose layout =
                    boss == null || boss.poses().isEmpty() ? null : boss.poses().get(k);
            if (layout != null && layout.offsets().size() != parts) {
                throw new IllegalArgumentException(spec.slug() + ": pose " + layout.name() + " places every part");
            }
            poseBody[k] = layout == null ? spec.body() : layout.body();
            for (int p = 0; p < parts; p++) {
                poseDx[k][p] = layout == null
                        ? spec.parts().get(p).dx()
                        : layout.offsets().get(p).dx();
                poseDy[k][p] = layout == null
                        ? spec.parts().get(p).dy()
                        : layout.offsets().get(p).dy();
            }
        }
        int phases = boss == null ? 0 : boss.phases().size();
        if (phases > Long.SIZE - 1) {
            throw new IllegalArgumentException(spec.slug() + ": at most " + (Long.SIZE - 1) + " phases");
        }
        for (int f = 0; f < phases; f++) {
            for (int p : boss.phases().get(f).exposes()) {
                exposedFrom[p] = Math.max(exposedFrom[p], f);
            }
        }
    }

    /** Back to the level start: every part whole, none hit. */
    void reset() {
        for (int p = 0; p < partHp.length; p++) {
            partHp[p] = spec.parts().get(p).hp();
            partTicksSinceHit[p] = Integer.MAX_VALUE;
        }
    }

    /** One step on every part's hit clock. */
    void tickHits() {
        for (int p = 0; p < partTicksSinceHit.length; p++) {
            if (partTicksSinceHit[p] < Integer.MAX_VALUE) {
                partTicksSinceHit[p]++;
            }
        }
    }

    /** As {@link SetPiece#partShielded}. */
    boolean partShielded(int p) {
        if (boss == null) {
            return false;
        }
        int phase = piece.phase();
        if (!piece.engaged() || piece.moving() || armoured[p] || phase < exposedFrom[p]) {
            return true;
        }
        if (breach != null && !arena.layer(p).touchable()) {
            // M7 part H: a breaching boss's buried mouth: nothing touches it (the Lance and the Smart Bomb included).
            return true;
        }
        if (coil != null && coil.implant(p) >= 0 && !coil.implantOpen(coil.implant(p))) {
            // M8 part H: a shut implant is armoured.
            return true;
        }
        if (hatches.armoured(p) && !hatches.open(p) && !stride.overheating()) {
            // Round 44: a walking boss's pod is armoured while its hatch is shut (until it overheats).
            return true;
        }
        return windows.windowed(phase, p) && windows.openTicks(p) == 0;
    }

    /** As {@link SetPiece#partWindowed}. */
    boolean partWindowed(int p) {
        return boss != null && windows.windowed(piece.phase(), p);
    }

    /** As {@link SetPiece#partOpen}. */
    boolean partOpen(int p) {
        return windows.openTicks(p) > 0 && partHp[p] > 0;
    }

    /** As {@link SetPiece#partColumn}. */
    double partColumn(int p, double cx, double halfWidth, double fromY) {
        if (piece.partStays(p)) {
            return Double.POSITIVE_INFINITY;
        }
        double nearest = PlayerFire.columnContact(cx, halfWidth, fromY, partX(p), partY(p), box(p));
        int c = chains.slamChain(p);
        return c < 0 ? nearest : chains.segmentsColumn(c, cx, halfWidth, fromY, nearest);
    }

    /** As {@link SetPiece#partLayer}. */
    Layer partLayer(int p) {
        return arena == null ? piece.layer() : arena.layer(p);
    }

    /** As {@link SetPiece#partTouches}. */
    boolean partTouches(int p, Hitbox size, double sx, double sy) {
        if (piece.partStays(p)) {
            // M5 part H: the stump is scenery: shots fly over it.
            return false;
        }
        if (size.overlaps(sx, sy, box(p), partX(p), partY(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsTouch(c, size, sx, sy);
    }

    /** As {@link SetPiece#partInBlast}. */
    boolean partInBlast(int p, double bx, double by, double radius) {
        if (piece.partStays(p)) {
            return false;
        }
        if (PlayerFire.inBlast(bx, by, radius, partX(p), partY(p), box(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsInBlast(c, bx, by, radius);
    }

    /** As {@link SetPiece#partOnField}. */
    boolean partOnField(int p) {
        if (PlayField.overlaps(partX(p), partY(p), box(p))) {
            return true;
        }
        int c = chains.slamChain(p);
        return c >= 0 && chains.segmentsOnField(c);
    }

    /** M7 part F: whether part {@code p}'s box lies wholly on the play field. */
    boolean wholeOnField(int p) {
        Hitbox box = box(p);
        double px = partX(p);
        double py = partY(p);
        return px - box.width() / 2 >= 0
                && px + box.width() / 2 <= PlayField.WIDTH
                && py - box.height() / 2 >= 0
                && py + box.height() / 2 <= PlayField.HEIGHT;
    }

    /** Part {@code p}'s hit box now: its spec's, M7 part F a walking boss's turned with its heading. */
    private Hitbox box(int p) {
        return stride != null ? stride.partBox(p) : spec.parts().get(p).box();
    }

    /** As {@link SetPiece#plateTouches}. */
    boolean plateTouches(Hitbox size, double sx, double sy) {
        return (stride != null && stride.plateTouches(size, sx, sy)) || (coil != null && coil.touches(size, sx, sy));
    }

    /** As {@link SetPiece#plateColumn}. */
    double plateColumn(double cx, double halfWidth, double fromY) {
        double plate = stride == null ? Double.POSITIVE_INFINITY : stride.plateColumn(cx, halfWidth, fromY);
        return coil == null ? plate : Math.min(plate, coil.column(cx, halfWidth, fromY));
    }

    /** As {@link SetPiece#damagePartAt(int, double, double, double, double, double)}. */
    boolean damagePartAt(int p, double amount, double hx, double hy, double dirX, double dirY) {
        if (arena == null) {
            return damagePart(p, amount);
        }
        partTicksSinceHit[p] = 0;
        if (coil != null) {
            // M8 part H: the throat while the jaw is open; an implant's hits count on the head.
            int q = coil.hpPart(p);
            partTicksSinceHit[q] = 0;
            boolean alive = partHp[q] > 0;
            partHp[q] -= amount
                    * coil.multiplier(p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[q] <= 0;
        }
        boolean alive = partHp[p] > 0;
        if (breach != null) {
            // M7 part H: the gullet's weak spots while the mouth is open.
            partHp[p] -= amount
                    * breach.multiplier(
                            p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[p] <= 0;
        }
        if (stride != null) {
            // M7 part F: the vents, turned with the heading, open after each burn.
            partHp[p] -= amount
                    * stride.multiplier(
                            p, hx, hy, dirX, dirY, spec.parts().get(p).multiplier());
            return alive && partHp[p] <= 0;
        }
        partHp[p] -= amount
                * arena.multiplier(
                        p,
                        hx,
                        hy,
                        dirX,
                        dirY,
                        partX(p),
                        partY(p),
                        spec.parts().get(p).multiplier());
        return alive && partHp[p] <= 0;
    }

    /** As {@link SetPiece#damagePart}. */
    boolean damagePart(int p, double amount) {
        partTicksSinceHit[p] = 0;
        int q = hpPart(p);
        partTicksSinceHit[q] = 0;
        boolean alive = partHp[q] > 0;
        partHp[q] -= amount * spec.parts().get(p).multiplier();
        return alive && partHp[q] <= 0;
    }

    /** As {@link SetPiece#hpPart}. */
    int hpPart(int p) {
        return coil == null ? p : coil.hpPart(p);
    }

    /** As {@link SetPiece#damagePartFlat}. */
    boolean damagePartFlat(int p, double amount) {
        partTicksSinceHit[p] = 0;
        int q = hpPart(p);
        partTicksSinceHit[q] = 0;
        boolean alive = partHp[q] > 0;
        partHp[q] -= amount;
        return alive && partHp[q] <= 0;
    }

    /** As {@link SetPiece#wreckPart}. */
    boolean wreckPart(int p) {
        // A fire-only part goes down with the boss but pays nothing.
        boolean alive = partHp[p] > 0 && !armoured[p];
        partHp[p] = Math.min(partHp[p], 0);
        return alive;
    }

    /** As {@link SetPiece#barShare}. */
    double barShare() {
        double left = 0;
        for (int p = 0; p < partHp.length; p++) {
            left += outOfBar[p] ? 0 : Math.max(0, partHp[p]);
        }
        return left / maxHp;
    }

    /** Part {@code p}'s centre on the screen now: its offset turned with the heading. */
    double partX(int p) {
        return piece.motion.x + partOffsetX(p);
    }

    /** Part {@code p}'s centre on the screen now (y up). */
    double partY(int p) {
        return piece.motion.y + partOffsetY(p);
    }

    /** As {@link SetPiece#partOffsetX}. */
    double partOffsetX(int p) {
        SetPieceMotion motion = piece.motion;
        double x = motion.x;
        int pose = motion.pose;
        if (motion.lifted && motion.stays[p]) {
            // M5 part H: a part of the stump stays at the anchor, in the pose it lifted from.
            return motion.stumpX() + poseDx[motion.liftPose][p] - x;
        }
        if (chains.atTip(p)) {
            return chains.tipX(p);
        }
        if (stride != null) {
            // M7 part F: a walking boss's offsets turn with its heading.
            return stride.turnX(poseDx[pose][p], poseDy[pose][p]);
        }
        if (breach != null && breach.ventOf(p) >= 0) {
            // M7 part H, step H3: a spine vent where it rose along the ridge.
            return breach.ventX(breach.ventOf(p)) - x;
        }
        if (coil != null && coil.implant(p) >= 0) {
            // M8 part H: an implant rides its coil segment.
            return coil.implantX(coil.implant(p)) - x;
        }
        if (coil != null) {
            // Round 58 F.2: the head's parts (the gill sacs) turn with the head as it swims along the coil's path.
            return coil.turnX(poseDx[pose][p], poseDy[pose][p]) * motion.scale();
        }
        if (boss != null) {
            return poseDx[pose][p] * motion.scale();
        }
        PartSpec part = spec.parts().get(p);
        return part.dx() * motion.cos + part.dy() * motion.sin;
    }

    /** As {@link SetPiece#partOffsetY}. */
    double partOffsetY(int p) {
        SetPieceMotion motion = piece.motion;
        double y = motion.y;
        int pose = motion.pose;
        if (motion.lifted && motion.stays[p]) {
            return motion.stumpY() + poseDy[motion.liftPose][p] - y;
        }
        if (chains.atTip(p)) {
            return chains.tipY(p);
        }
        if (stride != null) {
            return stride.turnY(poseDx[pose][p], poseDy[pose][p]);
        }
        if (breach != null && breach.ventOf(p) >= 0) {
            return breach.ventY(breach.ventOf(p)) - y;
        }
        if (coil != null && coil.implant(p) >= 0) {
            return coil.implantY(coil.implant(p)) - y;
        }
        if (coil != null) {
            return coil.turnY(poseDx[pose][p], poseDy[pose][p]) * motion.scale();
        }
        if (boss != null) {
            return poseDy[pose][p] * motion.scale();
        }
        PartSpec part = spec.parts().get(p);
        return -part.dx() * motion.sin + part.dy() * motion.cos;
    }

    /** As {@link SetPiece#allWrecked}. */
    boolean allWrecked() {
        for (int p = 0; p < partHp.length; p++) {
            if (partHp[p] > 0 && !armoured[p]) {
                return false;
            }
        }
        return true;
    }
}
