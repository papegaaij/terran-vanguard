package vanguard.sim;

import java.util.List;

/**
 * M7 part H: a breaching arena boss's breach cycle (design/enemies/bosses/dust-colossus, Breaches; the user's round 43
 * answers H.4 and H.7), stepped by its {@link SetPiece} on the simulation's real steps (the level clock is held in its
 * arena while the ground crawls on, {@link Section#crawls()}), in the style of the {@link StrideArena}.
 *
 * <p><b>The cycle.</b> Its body is always {@code buried}. It arrives (the bar) and lies buried for its {@link
 * BossSpec.Breach#introSeconds() intro}; then its mouth (the first vital part) breaches at the points of its phase's
 * {@link BossSpec.Order} in turn: the {@link Phase#TELL tell} (the bulge at the point), the {@link Phase#RISE rise}
 * (the mouth's layer {@code buried} → {@code ground} at its middle), {@link Phase#OPEN open} (the gullet's {@code
 * while: open} weak spots, the suction, the phase's attacks), the {@link Phase#BITE bite} (its tell, then the snap:
 * {@link SetPiece.BossActions#bite}), the {@link Phase#DIVE dive} (back to {@code buried} at its middle), then {@link
 * Phase#BURIED buried} until the next tell. The cycle's times are the breach's; the next rise comes the breach's
 * {@code buriedSeconds} (at least its tell) after the dive's end.
 *
 * <p><b>Points.</b> The next point is fixed {@link BossSpec.Breach#warnSeconds()} before its rise for a side or rear
 * point (no edge warning since 2026-10-21: a boss never warns; its bulge and ridge are its tells), else at its tell
 * (H.7h). A following order's front and
 * rear points take the ship's x, its side points the ship's y, each within its clamp; an {@link Pose#UNDER} point lies
 * on the ship, {@link #UNDER_INSET} px inside the edges at most. A new boss phase starts its order from the first point
 * at its next pick (a breach under way runs its cycle out). The mouths do not move once risen.
 *
 * <p><b>The ridge.</b> Between two breaches its back runs under the sand from the last point to the next along a
 * quadratic curve bent toward the field's centre (the first from above the top edge, straight): {@link #ridgeX(int)},
 * {@link #ridgeY(int)} sample it, {@link #ridgeHead()} is how far its head has come (it starts when the next point is
 * fixed and the last mouth has dived, and reaches the point at the rise).
 *
 * <p><b>The suction</b> ({@link BossSpec.Suction}) pulls the ship and Rook toward the mouth while it is open and
 * through its bite's tell; never past its centre.
 *
 * <p><b>Step H3.</b> The {@link BossSpec.Vents spine vents} rise with each breach of a boss phase that fires their
 * burst ({@link #ventUp(int)}); an {@link BossSpec.Eruption eruption} replaces an under point's tell and rise by its
 * marker ({@link #marker()}: {@link SimEvents.Type#ERUPTION_MARKER} instead of the tell's event) and the eruption ({@link
 * SimEvents.Type#ERUPTION} instead of the rise's: the mouth is up and open at once, the marker hits); the {@link
 * BossSpec.DebrisFlow debris} is torn loose while the mouth draws ({@link PulledDebris}). A {@code SPIRAL} among the
 * phase's attacks fires while the mouth is open from {@link BossSpec.Breach#glowSeconds()} after the open (the gullet
 * glows before it when the phase has no ring).
 *
 * <p>Everything is allocated here; stepping does not allocate. Its state is hashed with its boss.
 */
public final class BreachArena implements Hashed {
    /** Where a breach is in its cycle. */
    public enum Phase {
        /** Under the sand, before its first tell or between a dive and the next tell. */
        BURIED,
        /** The bulge at the next point (the sand cracks, a dust ring); the mouth is still buried. */
        TELL,
        /** The mouth breaches, leaning into the field; {@code ground} from the middle. */
        RISE,
        /** The jaws open, the gullet glows (its weak spots), the suction runs, the phase's attacks fire. */
        OPEN,
        /** The bite's tell: the teeth flash, the jaws close; the snap at its end. */
        BITE,
        /** The mouth dives back under the sand; {@code buried} from the middle. */
        DIVE
    }

    /** Where the mouth breaches (its sprite's pose). */
    public enum Pose {
        /** At the top edge, leaning down. */
        FRONT,
        /** At the left edge, leaning right. */
        LEFT,
        /** At the right edge, leaning left. */
        RIGHT,
        /** At the bottom edge, leaning up (a rear wave for Rook). */
        REAR,
        /** Under the ship, straight up (phase 3; its marker). */
        UNDER;

        /**
         * Its edge's {@link WarningEdge} bit; 0 for the front and under points. Only the point's fixing reads it (a
         * side or rear point is fixed {@link BossSpec.Breach#warnSeconds()} ahead): a boss shows no edge warning.
         */
        int edgeBit() {
            return switch (this) {
                case LEFT -> WarningEdge.LEFT.bit();
                case RIGHT -> WarningEdge.RIGHT.bit();
                case REAR -> WarningEdge.BOTTOM.bit();
                default -> 0;
            };
        }
    }

    /** How many breach points there are ({@link Pose}): a boss's data poses, one per point, then place its parts. */
    public static final int POSES = 5;
    /** Samples along the ridge's curve, the last point's first ({@link #ridgeX(int)}). */
    public static final int RIDGE_POINTS = 17;
    /** An under point's least distance from the play field's edges without an eruption, px (an eruption gives its own). */
    public static final double UNDER_INSET = 80;
    /** Step H3: the spine vents' first burst comes this long after the mouth opens, s (then every its interval). */
    public static final double VENT_FIRST_SECONDS = 1;
    /** Where the ridge comes from before the first breach: this far above the top edge, px. */
    static final double START_ABOVE = 200;
    /** Step SVF: a spine vent's heading is the way along the ridge this far either side of it, px (the back's). */
    private static final double SPINE_CHORD = 16;
    /** Step SIM-K: a spine vent's centre stays this far inside the play field's edges, px (half a vent). */
    private static final double VENT_MARGIN = 20;

    /** What a bite or an eruption caught ({@link SetPiece.BossActions#bite}): the ship. */
    public static final int BITE_SHIP = 1;
    /** The bite caught Rook. */
    public static final int BITE_WINGMAN = 2;

    private static final Phase[] PHASES = Phase.values();

    private final SetPiece piece;
    private final BossSpec boss;
    private final BossSpec.Breach spec;
    /** Null without a suction. */
    private final BossSpec.Suction suction;
    /** Step H3: null without spine vents, an eruption, debris. */
    private final BossSpec.Vents vents;

    private final BossSpec.Eruption eruption;
    private final PulledDebris rocks;
    /** The spine vents' parts (empty without), and where they rise (screen px, y up). */
    private final int[] ventParts;

    private final double[] ventX;
    private final double[] ventY;
    /** Step SIM-K: per vent, the spine's direction there toward the mouth (a unit vector, screen px, y up). */
    private final double[] ventDirX;

    private final double[] ventDirY;
    /** Step SVF: the place {@link #alongRidge} found last (screen px, y up). */
    private double spotX;

    private double spotY;
    /** Per part, its index among the vents; -1 for none. */
    private final int[] partVent;

    private final List<BossSpec.Spot> spots;
    /** The mouth: the first vital part (else the first). */
    private final int mouth;

    private final int tellTicks;
    private final int riseTicks;
    private final int openTicks;
    private final int biteTicks;
    private final int diveTicks;
    /** From a dive's end to the next rise: the breach's buried time, at least its tell. */
    private final int gapTicks;

    private final int warnTicks;
    private final int introTicks;
    private final int glowTicks;
    /** Step H3: an erupting under point's marker, open time and gap from a dive's end to its eruption. */
    private final int markerTicks;

    private final int eruptOpenTicks;
    private final int eruptGapTicks;

    // Where the ship is (set by the sortie before each step).
    private double shipX;
    private double shipY;

    private Phase phase;
    private int phaseTicks;
    /** The phase entered in this step (its event in {@link #act}); -1 for none. */
    private int entered;
    /** Steps to the next rise while buried, telling or diving; -1 otherwise. */
    private int toRise;

    private boolean arrived;
    private Pose pose;
    private double mouthX;
    private double mouthY;
    /** Whether the next point is fixed (from its pick to its rise). */
    private boolean picked;

    private Pose nextPose;
    private double nextX;
    private double nextY;
    /** The boss phase whose order the picks follow, and the next index in it. */
    private int orderPhase;

    private int orderIndex;
    /** Rises in this attempt. */
    private int breaches;
    /** The ridge's curve: from (fromX, fromY) bent toward (viaX, viaY) to the next point. */
    private double fromX;

    private double fromY;
    private double viaX;
    private double viaY;
    private double toX;
    private double toY;
    /** The steps the ridge's head takes to the next point (from its start); -1 before it starts. */
    private int transitTicks;
    /** Steps to the next ring and to the next of the phase's other attacks while open. */
    private int ringTicks;

    private int otherTicks;
    /** What fires in this step ({@link #RING}, {@link #OTHERS}); set by {@link #step}, read by the piece. */
    private int fires;
    /** Whether the bite snaps in this step. */
    private boolean snap;
    /** The suction on the ship in this step, px/s ({@link #pull}). */
    private double pullX;

    private double pullY;
    /** Step H3: the next breach's tell (or marker) length, steps, as the last step saw it. */
    private int tellNow;
    /** Whether the vents rose with the current breach (until its dive's middle). */
    private boolean ventsRaised;
    /** Steps to the next rock torn loose while the mouth draws. */
    private int debrisTicks;
    /** The open phase's first timed attack's interval, steps (0: none), and whether it has a spiral. */
    private int intervalNow;

    private boolean spiralNow;
    /** In this step: the marker erupted, the mouth opened, a rock is torn loose. */
    private boolean erupted;

    private boolean opened;
    private boolean tear;
    /** Step SIM-K: the eruption's ring: null without; its steps since the eruption (-1: none expanding), whom it hit. */
    private final BossSpec.Shockwave shock;

    private final int shockTicks;
    private int shockAge;
    private int shockHit;

    /** {@link #fires()}: the phase's first attack (the ring). */
    static final int RING = 1;
    /** {@link #fires()}: the phase's other attacks (the fan), half an interval after each ring. */
    static final int OTHERS = 2;

    BreachArena(SetPiece piece, BossSpec boss, SetPieceSpec setPiece) {
        this.piece = piece;
        this.boss = boss;
        spec = boss.breach().orElseThrow();
        suction = boss.suction().orElse(null);
        spots = boss.arena().orElseThrow().spots();
        int vital = -1;
        for (int p = setPiece.parts().size() - 1; p >= 0; p--) {
            vital = setPiece.parts().get(p).vital() ? p : vital;
        }
        mouth = Math.max(0, vital);
        tellTicks = Math.max(1, SimStep.ticks(spec.tellSeconds()));
        riseTicks = Math.max(1, SimStep.ticks(spec.riseSeconds()));
        openTicks = Math.max(1, SimStep.ticks(spec.openSeconds()));
        biteTicks = Math.max(1, SimStep.ticks(spec.biteTellSeconds()));
        diveTicks = Math.max(1, SimStep.ticks(spec.diveSeconds()));
        gapTicks = Math.max(tellTicks, SimStep.ticks(spec.buriedSeconds()));
        warnTicks = SimStep.ticks(spec.warnSeconds());
        introTicks = SimStep.ticks(spec.introSeconds());
        glowTicks = SimStep.ticks(spec.glowSeconds());
        vents = boss.vents().orElse(null);
        eruption = boss.eruption().orElse(null);
        rocks = boss.debris().map(PulledDebris::new).orElse(null);
        int parts = setPiece.parts().size();
        ventParts = new int[vents == null ? 0 : vents.parts().size()];
        ventX = new double[ventParts.length];
        ventY = new double[ventParts.length];
        ventDirX = new double[ventParts.length];
        ventDirY = new double[ventParts.length];
        partVent = new int[parts];
        java.util.Arrays.fill(partVent, -1);
        for (int i = 0; i < ventParts.length; i++) {
            ventParts[i] = vents.parts().get(i);
            if (ventParts[i] >= parts) {
                throw new IllegalArgumentException(setPiece.slug() + ": a spine vent is one of its parts");
            }
            partVent[ventParts[i]] = i;
        }
        markerTicks = eruption == null ? tellTicks : Math.max(1, SimStep.ticks(eruption.markerSeconds()));
        eruptOpenTicks = eruption == null ? openTicks : Math.max(1, SimStep.ticks(eruption.openSeconds()));
        eruptGapTicks = eruption == null ? gapTicks : SimStep.ticks(eruption.buriedSeconds()) + markerTicks;
        shock = eruption == null ? null : eruption.ring().orElse(null);
        shockTicks = shock == null ? 0 : Math.max(1, SimStep.ticks(shock.expandSeconds()));
        reset();
    }

    /** Back to the level start: not arrived, buried above the top edge, nothing fixed. */
    void reset() {
        phase = Phase.BURIED;
        phaseTicks = 0;
        entered = -1;
        toRise = -1;
        arrived = false;
        pose = Pose.FRONT;
        mouthX = PlayField.WIDTH / 2.0;
        mouthY = PlayField.HEIGHT + START_ABOVE;
        picked = false;
        nextPose = Pose.FRONT;
        nextX = mouthX;
        nextY = mouthY;
        orderPhase = 0;
        orderIndex = 0;
        breaches = 0;
        fromX = toX = viaX = mouthX;
        fromY = toY = viaY = mouthY;
        transitTicks = -1;
        ringTicks = 0;
        otherTicks = 0;
        fires = 0;
        snap = false;
        pullX = 0;
        pullY = 0;
        tellNow = tellTicks;
        ventsRaised = false;
        for (int i = 0; i < ventParts.length; i++) {
            ventX[i] = mouthX;
            ventY[i] = mouthY;
            ventDirX[i] = 0;
            ventDirY[i] = 1;
        }
        debrisTicks = 0;
        intervalNow = 0;
        spiralNow = false;
        erupted = false;
        opened = false;
        tear = false;
        shockAge = -1;
        shockHit = 0;
        if (rocks != null) {
            rocks.reset();
        }
    }

    /** It was destroyed: no suction, no fire, no snap (its pose and place stay for the death). */
    void died() {
        shockAge = -1;
        fires = 0;
        snap = false;
        erupted = false;
        opened = false;
        tear = false;
        entered = -1;
        pullX = 0;
        pullY = 0;
    }

    /** Where the ship is now; the next point reads it when it is fixed. */
    void targets(double ship1X, double ship1Y) {
        shipX = ship1X;
        shipY = ship1Y;
    }

    /** It arrives (the bar): buried for its intro, then its first tell. */
    void arrive() {
        arrived = true;
        phase = Phase.BURIED;
        phaseTicks = 0;
        toRise = introTicks + tellTicks;
    }

    /** Whether its intro is over (its first tell has started): it engages then. */
    boolean engages() {
        return arrived && (breaches > 0 || phase != Phase.BURIED);
    }

    /**
     * One step of the cycle in boss phase {@code bossPhase}: the next point fixed at its lead or its tell, the
     * phases' times, the layer flips at the rise's and the dive's middles, the attacks' times while open.
     */
    void step(int bossPhase) {
        entered = -1;
        fires = 0;
        snap = false;
        erupted = false;
        opened = false;
        tear = false;
        if (!arrived) {
            return;
        }
        if (shockAge >= 0 && ++shockAge > shockTicks) {
            shockAge = -1;
        }
        phaseTicks++;
        if (toRise > 0) {
            toRise--;
        }
        Pose coming = picked ? nextPose : peek(bossPhase);
        int tell = tellFor(coming);
        tellNow = tell;
        if (!picked && toRise >= 0 && (toRise <= tell || (coming.edgeBit() != 0 && toRise <= warnTicks))) {
            pick(bossPhase);
        }
        if (picked && transitTicks < 0 && (phase == Phase.BURIED || phase == Phase.TELL)) {
            transitTicks = Math.max(1, toRise);
        }
        switch (phase) {
            case BURIED -> {
                if (toRise <= tell) {
                    startTell(bossPhase);
                }
            }
            case TELL -> {
                if (toRise == 0) {
                    breaches++;
                    picked = false;
                    toRise = -1;
                    transitTicks = -1;
                    raiseVents(bossPhase);
                    if (erupts(pose)) {
                        // Step H3: the marker erupts: the mouth is up and open at once.
                        erupted = true;
                        if (shock != null) {
                            // Step SIM-K: its ring starts expanding from the mouth.
                            shockAge = 0;
                            shockHit = 0;
                        }
                        layer(Layer.GROUND);
                        open(bossPhase);
                    } else {
                        enter(Phase.RISE);
                    }
                }
            }
            case RISE -> {
                if (2 * phaseTicks >= riseTicks) {
                    layer(Layer.GROUND);
                }
                if (phaseTicks >= riseTicks) {
                    open(bossPhase);
                }
            }
            case OPEN -> {
                if (phaseTicks >= openFor(pose)) {
                    enter(Phase.BITE);
                } else {
                    int interval = interval(bossPhase);
                    if (interval > 0 && --ringTicks <= 0) {
                        ringTicks = interval;
                        fires |= RING;
                    }
                    if (interval > 0 && --otherTicks <= 0) {
                        otherTicks = interval;
                        fires |= OTHERS;
                    }
                }
                debris(bossPhase);
            }
            case BITE -> {
                if (phaseTicks >= biteTicks) {
                    snap = true;
                    enter(Phase.DIVE);
                    toRise = diveTicks + gapFor(peek(bossPhase));
                } else {
                    debris(bossPhase);
                }
            }
            case DIVE -> {
                if (2 * phaseTicks >= diveTicks) {
                    layer(Layer.BURIED);
                }
                if (phaseTicks >= diveTicks) {
                    if (toRise <= tellFor(picked ? nextPose : peek(bossPhase))) {
                        startTell(bossPhase);
                    } else {
                        enter(Phase.BURIED);
                    }
                }
            }
        }
    }

    /** The mouth opens: the attacks' clocks from the glow, the debris's clock. */
    private void open(int bossPhase) {
        enter(Phase.OPEN);
        opened = true;
        int interval = interval(bossPhase);
        intervalNow = interval;
        spiralNow = spirals(bossPhase);
        ringTicks = glowTicks;
        otherTicks = glowTicks + interval / 2;
        double every = rocks == null ? 0 : boss.debris().orElseThrow().every(bossPhase);
        debrisTicks = every > 0 ? Math.max(1, SimStep.ticks(every)) : 0;
    }

    /** Step H3: while the mouth draws, a rock is torn loose every the phase's debris interval. */
    private void debris(int bossPhase) {
        if (rocks == null) {
            return;
        }
        double every = boss.debris().orElseThrow().every(bossPhase);
        if (!(every > 0)) {
            debrisTicks = 0;
            return;
        }
        if (debrisTicks <= 0 || --debrisTicks <= 0) {
            debrisTicks = Math.max(1, SimStep.ticks(every));
            tear = true;
        }
    }

    /**
     * Step H3: the living spine vents rise with this breach when the boss phase fires their burst. Step SIM-K (round 44,
     * K.b: "on its spine, not in the sand"): each sits on the body behind the mouth, its distance measured along the
     * ridge's curve that ends at the mouth (the curve the presentation draws behind the risen mouth, {@link
     * #ridgeX(int)}), straight on along the curve's start past its far end.
     */
    private void raiseVents(int bossPhase) {
        if (vents == null || !boss.phases().get(bossPhase).attacks().contains(vents.burst())) {
            return;
        }
        ventsRaised = true;
        for (int i = 0; i < ventParts.length; i++) {
            if (!piece.partWrecked(ventParts[i])) {
                onSpine(i, vents.distances().get(i));
            }
        }
    }

    /**
     * Step SIM-K: places vent {@code i} {@code d} px back from the mouth along the ridge's curve. Step SVF: along the
     * curve as the presentation lays the body and its back (the {@value #RIDGE_POINTS} samples of {@link #ridgeX(int)}
     * from the mouth back, straight on along the first stretch past the curve's start: a short ridge), so vent i sits
     * on spine segment i at a hairpin too; its heading the way along the samples {@value #SPINE_CHORD} px either side.
     */
    private void onSpine(int i, double d) {
        alongRidge(d - SPINE_CHORD);
        double aheadX = spotX;
        double aheadY = spotY;
        alongRidge(d + SPINE_CHORD);
        double dx = aheadX - spotX;
        double dy = aheadY - spotY;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1e-9) {
            dx = 0;
            dy = -1;
        } else {
            dx /= length;
            dy /= length;
        }
        alongRidge(d);
        // On the play field, so it can be shot (a short ridge's straight run may leave it).
        ventX[i] = Math.clamp(spotX, VENT_MARGIN, PlayField.WIDTH - VENT_MARGIN);
        ventY[i] = Math.clamp(spotY, VENT_MARGIN, PlayField.HEIGHT - VENT_MARGIN);
        ventDirX[i] = dx;
        ventDirY[i] = dy;
    }

    /**
     * Step SVF: the place {@code d} px back from the mouth along the ridge's samples ({@link #ridgeX(int)}, the last
     * the mouth), straight on along the first stretch past the curve's start, into {@link #spotX}, {@link #spotY}
     * (the presentation's {@code ColossusSpine.onTrack}).
     */
    private void alongRidge(double d) {
        double left = Math.max(0, d);
        for (int k = RIDGE_POINTS - 1; k > 0; k--) {
            double x = ridgeX(k);
            double y = ridgeY(k);
            double dx = ridgeX(k - 1) - x;
            double dy = ridgeY(k - 1) - y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length >= left || k == 1) {
                double u = length > 1e-9 ? left / length : 0;
                spotX = x + dx * u;
                spotY = y + dy * u;
                return;
            }
            left -= length;
        }
        spotX = ridgeX(0);
        spotY = ridgeY(0);
    }

    /** Whether a breach at {@code p} erupts (an under point with an eruption). */
    private boolean erupts(Pose p) {
        return p == Pose.UNDER && eruption != null;
    }

    /** The tell's (or the marker's) length at {@code p}, steps. */
    private int tellFor(Pose p) {
        return erupts(p) ? markerTicks : tellTicks;
    }

    /** How long the mouth stands open at {@code p}, steps. */
    private int openFor(Pose p) {
        return erupts(p) ? eruptOpenTicks : openTicks;
    }

    /** From a dive's end to the rise (or the eruption) at {@code p}, steps. */
    private int gapFor(Pose p) {
        return erupts(p) ? eruptGapTicks : gapTicks;
    }

    private void startTell(int bossPhase) {
        if (!picked) {
            pick(bossPhase);
        }
        enter(Phase.TELL);
        pose = nextPose;
        mouthX = nextX;
        mouthY = nextY;
        if (transitTicks < 0) {
            transitTicks = Math.max(1, toRise);
        }
    }

    private void enter(Phase next) {
        phase = next;
        phaseTicks = 0;
        entered = next.ordinal();
    }

    private void layer(Layer layer) {
        if (piece.partLayer(mouth) != layer) {
            piece.arena().orElseThrow().lift(mouth, layer);
        }
        if (layer == Layer.BURIED) {
            ventsRaised = false;
        }
        for (int i = 0; i < ventParts.length; i++) {
            // Step H3: the risen living vents with the mouth; every vent sinks with it (a dead one stays down).
            int p = ventParts[i];
            Layer to = layer == Layer.GROUND && ventsRaised && !piece.partWrecked(p) ? Layer.GROUND : Layer.BURIED;
            if (piece.partLayer(p) != to) {
                piece.arena().orElseThrow().lift(p, to);
            }
        }
    }

    /** The boss phase's first {@linkplain #timed timed} attack's interval, steps; 0 without one. */
    private int interval(int bossPhase) {
        List<Integer> attacks = boss.phases().get(bossPhase).attacks();
        for (int i = 0; i < attacks.size(); i++) {
            if (timed(attacks.get(i))) {
                return Math.max(
                        1,
                        SimStep.ticks(boss.attacks().get(attacks.get(i)).gun().intervalSeconds()));
            }
        }
        return 0;
    }

    /**
     * Whether attack {@code a} fires on the ring's (the first such) or the others' times: not a spiral (its own clock
     * while open) and not the spine vents' burst (theirs).
     */
    boolean timed(int a) {
        return boss.attacks().get(a).pattern() != BossSpec.Pattern.SPIRAL && (vents == null || a != vents.burst());
    }

    /** Whether the boss phase fires a spiral. */
    private boolean spirals(int bossPhase) {
        List<Integer> attacks = boss.phases().get(bossPhase).attacks();
        for (int i = 0; i < attacks.size(); i++) {
            if (boss.attacks().get(attacks.get(i)).pattern() == BossSpec.Pattern.SPIRAL) {
                return true;
            }
        }
        return false;
    }

    /** The point the next pick in {@code bossPhase} takes. */
    private Pose peek(int bossPhase) {
        List<Pose> poses = spec.order(bossPhase).poses();
        return poses.get(bossPhase == orderPhase ? orderIndex % poses.size() : 0);
    }

    /** Fixes the next point by {@code bossPhase}'s order: its place, the ridge's curve toward it. */
    private void pick(int bossPhase) {
        BossSpec.Order order = spec.order(bossPhase);
        if (bossPhase != orderPhase) {
            orderPhase = bossPhase;
            orderIndex = 0;
        }
        nextPose = order.poses().get(orderIndex);
        orderIndex = (orderIndex + 1) % order.poses().size();
        if (nextPose == Pose.UNDER) {
            double inset = eruption != null ? eruption.inset() : UNDER_INSET;
            nextX = Math.clamp(shipX, inset, PlayField.WIDTH - inset);
            nextY = Math.clamp(shipY, inset, PlayField.HEIGHT - inset);
        } else {
            BossSpec.Point point = spec.points().get(nextPose.ordinal());
            BossSpec.Clamp clamp = spec.clamps().get(nextPose.ordinal());
            boolean across = nextPose == Pose.FRONT || nextPose == Pose.REAR;
            nextX = order.follow() && across ? clamp.apply(shipX) : point.x();
            nextY = order.follow() && !across ? clamp.apply(shipY) : point.y();
        }
        picked = true;
        fromX = mouthX;
        fromY = mouthY;
        toX = nextX;
        toY = nextY;
        if (breaches == 0) {
            viaX = (fromX + toX) / 2;
            viaY = (fromY + toY) / 2;
        } else {
            viaX = PlayField.WIDTH / 2.0;
            viaY = PlayField.HEIGHT / 2.0;
        }
        transitTicks = -1;
    }

    /**
     * One step of its actions after the cycle (engaged): the event of the phase it entered, the bite's snap on the
     * ship and Rook.
     */
    void act(SetPiece.BossActions actions, boolean firing) {
        if (snap) {
            int hit = actions.bite(mouthX, mouthY, spec.biteRadius(), spec.biteDamage());
            actions.breach(SimEvents.Type.BITE_SNAP, mouthX, mouthY, hit);
        }
        if (erupted) {
            // Step H3: the marker erupts (the Polyp Mortar marker's rule: where it was placed).
            int hit = actions.bite(mouthX, mouthY, eruption.radius(), eruption.damage());
            actions.breach(SimEvents.Type.ERUPTION, mouthX, mouthY, hit);
        }
        if (shockAge >= 0) {
            // Step SIM-K: the ring catches the ship and Rook once each as it reaches them.
            int hit = actions.strike(mouthX, mouthY, shockRadius(), shock.damage(), shockHit);
            if (hit != 0) {
                shockHit |= hit;
                actions.breach(SimEvents.Type.ERUPTION_RING, mouthX, mouthY, hit);
            }
        }
        if (tear && firing) {
            actions.tearLoose(rocks, mouthX, mouthY);
        }
        if (entered < 0 || erupted) {
            return;
        }
        SimEvents.Type type =
                switch (PHASES[entered]) {
                    case TELL -> erupts(pose) ? SimEvents.Type.ERUPTION_MARKER : SimEvents.Type.BREACH_TELL;
                    case RISE -> SimEvents.Type.BREACH_RISE;
                    case BITE -> SimEvents.Type.BITE_TELL;
                    case DIVE -> SimEvents.Type.BREACH_DIVE;
                    default -> null;
                };
        if (type != null) {
            actions.breach(type, mouthX, mouthY, pose.ordinal());
        }
    }

    /** What fires in this step: {@link #RING} and {@link #OTHERS} bits (0 for nothing). */
    int fires() {
        return fires;
    }

    /** Step H3: whether the mouth opened in this step (the vents' burst starts its clock). */
    boolean opened() {
        return opened;
    }

    /** Step H3: whether a spiral fires now: the mouth open, from its glow's end. */
    boolean spiralling() {
        return phase == Phase.OPEN && phaseTicks >= glowTicks;
    }

    /** Step H3: whether a spiral starts in this step (its first bullets). */
    boolean spiralStarts() {
        return phase == Phase.OPEN && phaseTicks == glowTicks;
    }

    /** Step H3: whether the risen vents fire their burst now (the mouth open). */
    boolean ventsFiring() {
        return ventsRaised && phase == Phase.OPEN;
    }

    /**
     * Step SIM-K (round 44, K.c: "when the Colossus is out of the ground, the background cannot move"): whether a
     * crawling arena's ground must stand still now, or come to a stop within {@code easeTicks} steps: from {@code
     * easeTicks} before a tell (or an eruption's marker) through the tell (the bulge stays put on the sand), the rise,
     * the open mouth, the bite and the whole dive; never before it arrives nor once it is destroyed (its body then
     * scrolls with the ground).
     */
    boolean groundStill(int easeTicks) {
        if (!arrived || piece.destroyed()) {
            return false;
        }
        return switch (phase) {
            case TELL, RISE, OPEN, BITE, DIVE -> true;
            case BURIED -> toRise >= 0 && toRise <= tellNow + easeTicks;
        };
    }

    /** Step H3: the spine vents' burst attack; -1 without vents. */
    int ventBurst() {
        return vents == null ? -1 : vents.burst();
    }

    /** Step H3: part {@code p}'s index among the spine vents; -1 for none. */
    int ventOf(int p) {
        return partVent[p];
    }

    /** Step H3: where a phase's drop lands: the field's centre (pickups are not pulled: H.7b). */
    double dropX() {
        return PlayField.WIDTH / 2.0;
    }

    double dropY() {
        return PlayField.HEIGHT / 2.0;
    }

    /** Whether its mouth is open now: the suction and the bite's circle are about it (its tell included). */
    private boolean drawing() {
        return phase == Phase.OPEN || phase == Phase.BITE;
    }

    /** The suction toward the mouth at (px, py) now, px/s to the right; 0 without one or while it is not open. */
    double pullXAt(double px, double py) {
        double dx = mouthX - px;
        double d = suctionDistance(px, py);
        return d > 0 ? dx / d * suctionSpeed(d) : 0;
    }

    /** ... and up. */
    double pullYAt(double px, double py) {
        double dy = mouthY - py;
        double d = suctionDistance(px, py);
        return d > 0 ? dy / d * suctionSpeed(d) : 0;
    }

    /** The distance from (px, py) to the mouth's centre while it draws; 0 without a suction or while it does not. */
    private double suctionDistance(double px, double py) {
        if (suction == null || !drawing() || piece.destroyed()) {
            return 0;
        }
        double dx = mouthX - px;
        double dy = mouthY - py;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** The suction's speed at {@code d} px, never past the centre in a step. */
    private double suctionSpeed(double d) {
        return Math.min(suction.speedAt(d), d / SimStep.SECONDS);
    }

    /** The suction on the ship at (px, py) in this step ({@link #pullX()}, {@link #pullY()}). */
    void pull(double px, double py) {
        pullX = pullXAt(px, py);
        pullY = pullYAt(px, py);
    }

    /**
     * The damage multiplier of a hit on part {@code p} at (hx, hy) by a shot flying along (dirX, dirY) (0, 0 for a
     * blast): a weak spot's on the shot's line ahead ({@code while: open} ones only while the mouth is open), else the
     * part's own.
     */
    double multiplier(int p, double hx, double hy, double dirX, double dirY, double partMultiplier) {
        for (int i = 0; i < spots.size(); i++) {
            BossSpec.Spot spot = spots.get(i);
            if (spot.part() == p
                    && (!spot.whileOpen() || phase == Phase.OPEN)
                    && SlamArena.ahead(
                            hx - piece.partX(p) - spot.dx(),
                            hy - piece.partY(p) - spot.dy(),
                            dirX,
                            dirY,
                            spot.box().width() / 2,
                            spot.box().height() / 2)) {
                return spot.multiplier();
            }
        }
        return partMultiplier;
    }

    // --- the presentation's view ---------------------------------------------------------------

    /** Where the breach is in its cycle. */
    public Phase phase() {
        return phase;
    }

    /** How far its phase has run, 0–1 (buried: toward the next tell; 0 before it arrives). */
    public double progress() {
        int length =
                switch (phase) {
                    case BURIED -> phaseTicks + Math.max(0, toRise - tellNow);
                    case TELL -> tellFor(pose);
                    case RISE -> riseTicks;
                    case OPEN -> openFor(pose);
                    case BITE -> biteTicks;
                    case DIVE -> diveTicks;
                };
        return length <= 0 ? 0 : Math.min(1, (double) phaseTicks / length);
    }

    /** The current (or last) breach's point: its sprite's pose. */
    public Pose pose() {
        return pose;
    }

    /** The current (or last) mouth's centre, px from the left (above the top edge before the first tell). */
    public double mouthX() {
        return mouthX;
    }

    /** The mouth's centre, px up from the bottom edge. */
    public double mouthY() {
        return mouthY;
    }

    /** Whether the next point is fixed ({@link #nextX()}, {@link #nextY()}, {@link #nextPose()}). */
    public boolean nextFixed() {
        return picked;
    }

    /** The next breach's point once fixed; else the mouth's. */
    public Pose nextPose() {
        return picked ? nextPose : pose;
    }

    /** The next breach's centre once fixed (else the mouth's), px from the left. */
    public double nextX() {
        return picked ? nextX : mouthX;
    }

    /** ... px up from the bottom edge. */
    public double nextY() {
        return picked ? nextY : mouthY;
    }

    /** How many samples {@link #ridgeX(int)} has. */
    public int ridgeCount() {
        return RIDGE_POINTS;
    }

    /** Sample {@code i} of the ridge's curve from the last point (0) to the next one ({@code ridgeCount() - 1}), px from the left. */
    public double ridgeX(int i) {
        double u = (double) i / (RIDGE_POINTS - 1);
        return (1 - u) * (1 - u) * fromX + 2 * u * (1 - u) * viaX + u * u * toX;
    }

    /** ... px up from the bottom edge. */
    public double ridgeY(int i) {
        double u = (double) i / (RIDGE_POINTS - 1);
        return (1 - u) * (1 - u) * fromY + 2 * u * (1 - u) * viaY + u * u * toY;
    }

    /**
     * How far the ridge's head has come along the curve, 0–1 (smoothstep over its transit): from the next point's
     * pick 0 until its transit starts (the last mouth has dived), 1 at the rise and until the next point is fixed (0
     * before the first one).
     */
    public double ridgeHead() {
        if (!picked) {
            return breaches > 0 ? 1 : 0;
        }
        if (transitTicks < 0) {
            return 0;
        }
        double s = Math.clamp(1 - (double) toRise / transitTicks, 0, 1);
        return s * s * (3 - 2 * s);
    }

    /** The bottom edge's {@link WarningEdge} bit while a rear breach tells, rises, opens, bites or dives (a rear wave for Rook). */
    public int rear() {
        return pose == Pose.REAR && phase != Phase.BURIED && arrived && !piece.destroyed()
                ? WarningEdge.BOTTOM.bit()
                : 0;
    }

    /** The suction on the ship in this step, px/s to the right (0 while the mouth is not open). */
    public double pullX() {
        return pullX;
    }

    /** The suction on the ship in this step, px/s up. */
    public double pullY() {
        return pullY;
    }

    /** Whether the gullet glows before its next ring (the last {@link BossSpec.Breach#glowSeconds()} before it). */
    public boolean glowing() {
        if (phase != Phase.OPEN) {
            return false;
        }
        if (intervalNow == 0) {
            // Step H3: a phase with a spiral and no ring glows before the spiral.
            return spiralNow && phaseTicks < glowTicks;
        }
        return ringTicks <= glowTicks && openFor(pose) - phaseTicks >= ringTicks;
    }

    /** Whether its gullet is open (its {@code while: open} weak spots count). */
    public boolean open() {
        return phase == Phase.OPEN;
    }

    /** The mouth's part. */
    public int mouth() {
        return mouth;
    }

    /** How many mouths have breached in this attempt. */
    public int breaches() {
        return breaches;
    }

    /** The bite circle's radius, px. */
    public double biteRadius() {
        return spec.biteRadius();
    }

    /**
     * Step H3: whether an eruption's marker is down (an erupting under point's tell, from its placing on the ship to
     * its eruption); it does not follow the ship.
     */
    public boolean marker() {
        return arrived && phase == Phase.TELL && erupts(pose) && !piece.destroyed();
    }

    /** The marker's (the coming eruption's) centre, px from the left; the mouth's when no marker is down. */
    public double markerX() {
        return mouthX;
    }

    /** ... px up from the bottom edge. */
    public double markerY() {
        return mouthY;
    }

    /** How far the marker has run to its eruption, 0–1; 0 while none is down. */
    public double markerProgress() {
        return marker() ? progress() : 0;
    }

    /** The marker's (the eruption's) radius, px; 0 without an eruption. */
    public double markerRadius() {
        return eruption == null ? 0 : eruption.radius();
    }

    /** Whether an under point erupts (the boss has an {@link BossSpec.Eruption}). */
    public boolean erupting() {
        return eruption != null;
    }

    /**
     * Step SIM-K: whether the eruption's ring is expanding now (from the eruption over its expansion time); false
     * without one.
     */
    public boolean ringing() {
        return shockAge >= 0;
    }

    /** Step SIM-K: the eruption's ring's radius now, px (its centre the mouth's); 0 while none expands. */
    public double shockRadius() {
        return shockAge < 0 ? 0 : shock.radius() * Math.min(1, (double) shockAge / shockTicks);
    }

    /** Step SIM-K: the eruption's ring's full radius, px; 0 without one. */
    public double shockReach() {
        return shock == null ? 0 : shock.radius();
    }

    /** How many spine vents it has (0 without). */
    public int ventCount() {
        return ventParts.length;
    }

    /** Spine vent {@code i}'s part. */
    public int ventPart(int i) {
        return ventParts[i];
    }

    /** Whether spine vent {@code i} is up: risen with the current breach, alive, {@code ground} (from the rise's middle). */
    public boolean ventUp(int i) {
        int p = ventParts[i];
        return ventsRaised && !piece.partWrecked(p) && piece.partLayer(p) == Layer.GROUND;
    }

    /** Whether spine vent {@code i} rose with the current breach and lives (its rise and sinking drawn with the mouth's). */
    public boolean ventRaised(int i) {
        return ventsRaised && !piece.partWrecked(ventParts[i]);
    }

    /** Where spine vent {@code i} rises (its last place once it sank), px from the left. */
    public double ventX(int i) {
        return ventX[i];
    }

    /** ... px up from the bottom edge. */
    public double ventY(int i) {
        return ventY[i];
    }

    /**
     * Step SIM-K: the spine's direction at spine vent {@code i} toward the mouth, a unit vector (screen px to the
     * right; {@link #ventDirY(int)} up): the organ's heading on the body.
     */
    public double ventDirX(int i) {
        return ventDirX[i];
    }

    /** ... up. */
    public double ventDirY(int i) {
        return ventDirY[i];
    }

    /** Rocks torn loose in this attempt (0 without debris). */
    public int rocksTorn() {
        return rocks == null ? 0 : rocks.torn();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(phaseTicks)
                .add(toRise)
                .add(arrived ? 1 : 0)
                .add(pose.ordinal())
                .add(mouthX)
                .add(mouthY)
                .add(picked ? 1 : 0)
                .add(nextPose.ordinal())
                .add(nextX)
                .add(nextY)
                .add(orderPhase)
                .add(orderIndex)
                .add(breaches)
                .add(fromX)
                .add(fromY)
                .add(viaX)
                .add(viaY)
                .add(toX)
                .add(toY)
                .add(transitTicks)
                .add(ringTicks)
                .add(otherTicks)
                .add(pullX)
                .add(pullY)
                .add(tellNow)
                .add(ventsRaised ? 1 : 0)
                .add(debrisTicks)
                .add(intervalNow)
                .add(spiralNow ? 1 : 0)
                .add(rocksTorn());
        if (shock != null) {
            // Step SIM-K: only with an eruption ring, so the other bosses hash as before.
            hash.add(shockAge).add(shockHit);
        }
        for (int i = 0; i < ventParts.length; i++) {
            hash.add(ventX[i]).add(ventY[i]);
        }
    }
}
