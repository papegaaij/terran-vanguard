package vanguard.sim;

import java.util.Optional;

/**
 * An enemy in flight, as its {@link Spawn} planned it: it flies its path; at the path's end it is
 * gone, or it holds there (hovering, or orbiting in a circle) while its gun fires, and then
 * leaves the play field; a diver's hold is its pause, its leaving the dive, in which it fires
 * once. A ground unit ({@link #root}) is fixed to the ground and scrolls with it; a turret turns
 * its barrel towards the ship and fires along it while the ship is inside its arc. Pooled:
 * {@link #spawn} and {@link #root} reuse the instance.
 */
public final class Enemy implements Hashed {
    enum Phase {
        ENTER,
        HOLD,
        LEAVE,
        /** Fixed to the ground layer. */
        GROUND,
        /** Spiralling out of a whirl cluster's release point. */
        SPIRAL,
        /** Circling its carrier as an escort, or about to break off once the carrier ended. */
        ESCORT,
        /** A walker on its ground path. */
        WALK,
        /** A member of a segment chain, placed by its {@link Chain}. */
        CHAIN,
        /** M5 part D: a {@code rear ambush} unit on its way (design/enemies/air/wraith); see {@link AmbushPhase}. */
        AMBUSH,
        /** M5 part D: a member of a swarm, placed by its {@link Flock}. */
        FLOCK,
        /** M5 part F: a chaser steering toward the ship (design/enemies/air/lamprey). */
        CHASE,
        /** M5 part F: latched onto the ship's hull, placed at its spot. */
        LATCHED,
        /** M5 part F: thrown off the hull, slowing to a stop. */
        STUNNED,
        /** M5 part G: a lane walker on its lane in ground space (design/enemies/naval/skimmer). */
        LANE,
        /** M7 part B: a Burrower in its cycle (design/enemies/ground/burrower), placed by its {@link Burrowing}. */
        BURROW,
        /** M7 part B: in its swoop across the ship's lane (design/enemies/air/dust-kite), then straight on. */
        SWOOP,
        /**
         * M7 part D: a hooked mine layer's bomb run on the ground allies (design/campaign Level 17, What goes
         * for the domes): to its row, steering over its dome, then (its load spent, or no dome left) straight
         * down.
         */
        BOMB,
        /**
         * M7 part E: a vortex in its cycle (design/enemies/ground/dust-devil), placed by its {@link VortexCycle}: it
         * swirls round its centre line with the ground and leaves at the bottom.
         */
        VORTEX,
        /**
         * M7 part E: a lane strafer (design/enemies/air/ghost-drone), placed by its {@link LaneStrafing}: down to its
         * lane, across, and (hard) back once.
         */
        STRAFE,
        /**
         * M8 part D: a unit with a rise (design/enemies/naval/abyss-ray), placed by its {@link Rising}: its shadow, the
         * rise, the glide and the sink.
         */
        RISE,
        /**
         * M8 part E: a nest on a wall's foot (design/enemies/naval/siren; {@link WallMounts}): on the play plane, fixed to
         * the ground at its spot and scrolling with it.
         */
        MOUNT,
        /** M8 part E: a coiling unit (design/enemies/naval/spiral-nautilus), placed by its {@link Coiling}. */
        COIL,
        /** M8 part F: a lunging unit (design/enemies/naval/glow-angler), placed by its {@link Lunging}. */
        LURE
    }

    /**
     * M7 part E: where a lane strafer is in its flight (design/enemies/air/ghost-drone; {@link
     * EnemySpec.LaneStrafe}), for the presentation and the tests; {@link #DESCENT} for any other unit.
     */
    public enum StrafePhase {
        /** Dropping straight down from the top edge to its lane. */
        DESCENT,
        /** Banking 90° from the descent into the strafe. */
        BANK,
        /** Strafing across at its lane's height (a side entry from its start); its pass trigger armed once. */
        STRAFE,
        /** Hard's second pass: its half turn at the far edge, then strafing back once; its pass trigger armed again. */
        RETURN
    }

    /**
     * M7 part B: where a Burrower is in its cycle (design/enemies/ground/burrower, user decision Q1 = a),
     * for the presentation and the tests; {@link #NONE} for any other unit. It is on {@link Layer#BURIED}
     * in every phase but {@link #SURFACED} and the first half of {@link #DIVE}.
     */
    public enum BurrowPhase {
        NONE,
        /** Buried, scrolling in with the ground from above the top edge (a ripple). */
        ENTER,
        /** Buried, swimming to its spot, fixed on the screen (the ripple trail). */
        TRAVEL,
        /** Buried at its spot, waiting for its wave's stagger. */
        WAIT,
        /** The tell at its spot: the mound, its cracks and glow, the rumble. */
        BULGE,
        /** Up on {@code ground}, hittable; its rings fire. */
        SURFACED,
        /** Diving; buried from the dive's middle, a filling hole left. */
        DIVE,
        /** Buried, swimming off the bottom edge with the scroll: its escape. */
        ESCAPE
    }

    /**
     * M5 part F: where a chaser is in its life (design/enemies/air/lamprey, user decision F4 = a), for
     * the presentation and the tests; {@link #NONE} for any other unit.
     */
    public enum LatchPhase {
        NONE,
        /** Flying in along its path, until its chase starts (a swarm member's delay after its entry). */
        ENTER,
        /** Steering toward the ship. */
        CHASE,
        /** Flying straight on after its chase. */
        ON,
        /** Latched onto the ship's hull at its {@link #spot()}, draining the shield. */
        LATCHED,
        /** Thrown off the hull: stunned, slowing to a stop, drifting with the wind. */
        STUNNED,
        /** Fleeing to the nearer side edge; it never latches again. */
        FLEE
    }

    /**
     * M5 part D: where a {@code rear ambush} unit is on its way (design/enemies/air/wraith), for the
     * presentation and the tests; {@link #NONE} for any other unit.
     */
    public enum AmbushPhase {
        NONE,
        /** Cloaked, straight down its lane from the top edge, past the ship and off the bottom edge. */
        SWOOP,
        /** Cloaked, below the bottom edge; the edge's warning runs ahead of its re-entry. */
        GAP,
        /** Cloaked, back up its lane from below the bottom edge to its hold point. */
        RISE,
        /** At its hold point: the decloak flash, on its decloaked layer, its gun silent. */
        DECLOAK,
        /** Decloaked, holding at its hold point, its bursts up the screen. */
        HOLD,
        /** Decloaked, out up the nearer side lane and off the top edge. */
        EXIT
    }

    /**
     * M7 part D: where a walker with a curl-roll is in it (design/enemies/ground/shellback), for the
     * presentation and the tests; {@link #WALK} for any other unit. From {@link #CURLING} to the end of
     * {@link #UNCURLING} it is armoured and its hit box is the ball.
     */
    public enum CurlPhase {
        /** Walking its path (before its roll, or after it). */
        WALK,
        /** Tucking in where it stands. */
        CURLING,
        /** Rolling along its path at the roll's speed, crushing ground targets. */
        ROLLING,
        /** Uncurling where it stands. */
        UNCURLING,
        /** Its belly showing where it stands: the body takes the belly factor. */
        BELLY
    }

    /**
     * M7 part E: where a vortex is in its cycle (design/enemies/ground/dust-devil; {@link EnemySpec.Vortex}), for
     * the presentation and the tests; {@link #CLOSED} for any other unit. It is on {@link Layer#LOW_AIR} and its
     * core hittable only while {@link #OPEN}; in every other phase it is on {@code ground}, its vanes armoured.
     */
    public enum VortexPhase {
        /** Closed on the ground, spinning fast (and before its first cycle, its wave's offset). */
        CLOSED,
        /** Rising: the tell before it opens (the swirl tightens and brightens, the spin speeds up). */
        RISING,
        /** Open on {@code low-air}: its core exposed and hittable, its spiral firing. */
        OPEN,
        /** Closed again and sinking back to the ground (the first part of its closed time after an opening). */
        SINKING
    }

    // The fields are package-private: its state, shared with its behaviour components ({@link Chasing}, {@link
    // EnemyFlight} ...), which move it through them; nothing else in the package writes them.
    EnemySpec spec;
    /** Its hit box: its stat block's, or a chain member's own (the segments taper). */
    Hitbox box;
    /** The chain it is a member of; null for any other unit (the link is hashed only with one). */
    Chain chain;
    /** Its place in its chain: 0 the head. */
    int link;

    /** A laser sweep (design/enemies/air/mantis; hashed only with one). */
    final SweepBeam sweepBeam = new SweepBeam();

    int kind;
    int serial;
    /** The ground group it belongs to (a dock of Level 02); -1 for none. */
    int group;

    FlightPath path;
    int segment;
    double distance;
    double speed;
    Phase phase;
    int holdTicks;
    Optional<Spawn.Orbit> orbit;
    double angle;
    Spawn.Exit exit;
    double vx;
    double vy;
    double x;
    double y;
    double prevX;
    double prevY;
    /**
     * Where it faces: radians clockwise from straight down the screen. For the presentation only,
     * and not part of the state hash, except for a walker, whose facing decides its armour and its
     * fan (design/enemies/ground/scuttler).
     */
    double facing;
    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): the frame it lives in. Its own state (position, velocity,
     * facing, aim, its cycles' state) is in this frame; the accessors and the events give the screen's, and the ship,
     * its aim point and the ground's scroll come in converted ({@link TravelFrame}). {@link TravelFrame#SCREEN} for
     * every unit not spawned in a reversed scroll.
     */
    TravelFrame frame = TravelFrame.SCREEN;

    double hp;
    /**
     * Simulation steps since it last took damage, for the Targeting computer's HP bar; {@link
     * Integer#MAX_VALUE} before its first hit. For the presentation only, not part of the state hash.
     */
    int ticksSinceHit = Integer.MAX_VALUE;

    int volleyTicks;
    int burstLeft;
    int burstTicks;
    boolean leadsTarget;
    Optional<PickupType> carried;
    /** A turret's barrel: radians clockwise from straight down the screen. */
    double aim;
    /** Whether the ship is inside a turret's arc this step. */
    boolean inArc;
    /** M7 part F: its turret and its gun (design/enemies/ground/warden-tank; hashed only with a turret). */
    final TurretGun turretGun = new TurretGun(this);

    int diveTicks;
    boolean diveFired;
    boolean diveShot;
    /** A whirl seed's spiral and ricochets (design/enemies/air/whirl-seed). */
    final Spiralling spiralling = new Spiralling(this);

    /** A spawner's steps since its centre crossed the top edge; -1 before. */
    int broodTicks;
    /** A boss's launched unit (part G): steps it glides out before it holds, and how long it then holds; 0 for none. */
    int glideTicks;

    int glideHoldTicks;

    /** An escort's circle round its carrier (hashed only for an escort). */
    final Escorting escorting = new Escorting(this);

    /** A walker's walk (design/enemies/ground/scuttler; hashed only for a walker or a volley wave's unit). */
    final Walking walking = new Walking(this);
    /** How far the ground scrolled since it entered (a walker's path's points and a lane scroll with it). */
    double scrolled;
    /** Whether it has been on the play field (a walker enters from outside it). */
    boolean entered;
    /** The edge its wave entered from (a wingman's formation follows the sides and rear waves); front for any other unit. */
    WaveSpec.Entry entry = WaveSpec.Entry.FRONT;

    /** {@link #spawnStep}: nothing happens. */
    static final int SPAWN_NONE = 0;
    /** {@link #spawnStep}: the iris starts to open (the telegraph). */
    static final int SPAWN_TELEGRAPH = 1;
    /** {@link #spawnStep}: the spawner releases its units now. */
    static final int SPAWN_RELEASE = 2;

    /** M5 part C, a periodic spawner's cycle (design/enemies/ground/hive-node; hashed only with one). */
    final SpawnerIris spawnerIris = new SpawnerIris();

    /** M5 part C, a pounce (design/enemies/ground/ravager; hashed only with one). */
    final Pouncing pouncing = new Pouncing();

    /** M5 part C: its wave's tag; "" for none (not hashed: it follows from the plan). */
    String tag = "";

    /** M5 part D: a rear ambush unit's way (design/enemies/air/wraith; hashed only for one). */
    final Ambushing ambushing = new Ambushing(this);
    /**
     * M5 part D: the volleys of an ambush unit's hold (design/enemies/air/wraith: "twice in its
     * hold"); hard's longer hold does not add a third.
     */
    static final int AMBUSH_VOLLEYS = 2;

    /** M5 part D: the flock it is a member of; null for any other unit (hashed only with one). */
    Flock flock;
    /** Its place in its flock, from 0 in entry order. */
    int member;

    /**
     * M5 part D: the air escort's units its body overlapped in the last step, one bit per unit (bit k
     * for unit k): a contact hurts a unit once, when it starts (design/allies, evacuation shuttle).
     */
    int allyContacts;

    /**
     * M5 part E: its drift on top of the scroll, px/s (y up): a field unit's along its wave's current
     * (design/enemies/naval/driftjelly), a raft's along its nest's (design/enemies/naval/reef-spitter);
     * hashed only while it drifts.
     */
    double driftX;

    double driftY;
    /** M5 part E, a submerging unit's swap timer (the Driftjelly; hashed only with a submerge). */
    final Submerging submerging = new Submerging();
    /** M5 part E, a proximity ring (hashed only with one): steps until it may fire its ring again; 0 ready. */
    int ringWait;
    /** M7 part B: a Burrower's cycle (hashed only with a burrow); idle for any other unit. */
    final Burrowing burrowing = new Burrowing();
    /** M7 part B: a Burrower's wave's stagger clock in the {@link EnemyForce}; -1 for none. */
    int burrowGroup = -1;
    /** M7 part B, a swoop (design/enemies/air/dust-kite; hashed only with one). */
    final Swooping swooping = new Swooping();
    /** M8 part C, a ram (design/enemies/naval/glassfin; hashed only with one). */
    final Ramming ramming = new Ramming();
    /** M8 part D, a rise (design/enemies/naval/abyss-ray; hashed only with one). */
    final Rising rising = new Rising();
    /** M8 part D, a hover held above the ship (design/enemies/naval/barbfin; hashed only with one). */
    final HoverHold hoverHold = new HoverHold();
    /** M8 part E, a song of slowing rings (design/enemies/naval/siren; hashed only with one). */
    final SirenSong song = new SirenSong();
    /** M8 part E, a coiling orbit round the ship (design/enemies/naval/spiral-nautilus; hashed only with one). */
    final Coiling coiling = new Coiling();
    /** M8 part F, a lunge from a lure (design/enemies/naval/glow-angler; hashed only with one). */
    final Lunging lunging = new Lunging();
    /** M5 part F: a chaser's chase, latch and shake-off (design/enemies/air/lamprey; hashed only with a chase). */
    final Chasing chasing = new Chasing(this);

    /**
     * M5 part G: a lane walker (design/enemies/naval/skimmer, user decision G4 = a): {@link #path} is its
     * lane at its entry, scrolled by {@link #scrolled}; hashed only for one.
     */
    boolean lane;
    /**
     * M5 part G, the {@code alternate} hook (user decision G1 = a): the attacks it started (a burst is
     * one) and whom the running one goes at (a convoy unit, -1 the ship); hashed once it attacked.
     */
    int attacks;

    int attackTarget = -1;
    /**
     * M5 part G, the {@code always} hook: whether its dive went at the objective, and whether its body
     * crossing the objective dealt the dive's hit (once); hashed only once it dove at it.
     */
    boolean divedAtObjective;

    boolean diveHit;
    /** M7 part D, a bomb run (design/campaign Level 17, What goes for the domes; hashed only once it flies one). */
    final BombRun bombRun = new BombRun();

    /**
     * M5 part H, a root burst's growth (design/campaign Level 14, Hazards; decision H2 = a): steps left
     * of its growth, in which it takes no damage and fires nothing; 0 once grown (and for every other
     * unit). Its growth's length and its burst's index (-1 for a unit no burst grew); hashed only for a
     * growth.
     */
    int growTicks;

    int growTotal;
    int rootBurst = -1;
    /**
     * M5 part H: whether a boss's window launched it as an ambush (the Siege Spire's maw: at most a
     * number of them alive at once); hashed only when set.
     */
    boolean launched;

    /**
     * M7 part A, the EMP Burst's stun (design/player/specials, EMP Burst): steps left; 0 when it is not
     * stunned. Separate from a Lamprey's own stun after a shake-off ({@link Chasing}).
     */
    int empStun;
    /** M7 part A: an air unit's drift per step while stunned: its last step's movement when the stun began. */
    double stunDx;

    double stunDy;
    /**
     * M7 part A: how far its stuns let it drift off the path, circle or carrier it is placed on; it
     * resumes there shifted by this, so it does not jump when a stun ends (0 for most units).
     */
    double offX;

    double offY;

    /** A pounce's contact with the ship. */
    static final int TOUCHED_SHIP = 1;
    /** A pounce's contact with the wingman. */
    static final int TOUCHED_WINGMAN = 2;

    /** M7 part D: a mortar vent swells this long before its lob (design/enemies/ground/shellback). */
    static final double MORTAR_TELL_SECONDS = 0.5;

    /** Where {@link #damage} reports what an aura ring absorbs and what a curled shell glances off. */
    final SimEvents events;

    /** M7 part D, an aura ring (design/enemies/air/choir-herald; {@link Auras}); none for most units. */
    final AuraRing auraRing = new AuraRing();
    /** Steps until {@link #damage} reports again (not hashed: it only spaces the events). */
    int absorbQuiet;
    /** M7 part D: a unit part (design/enemies/ground/shellback, the vent); none for most units. */
    final PartHp partHp = new PartHp();
    /** M7 part D: its curl-roll (design/enemies/ground/shellback; hashed only with a roll). */
    final CurlRoll curlRoll = new CurlRoll();
    /** M7 part D: a Herald's steps into its hover, for its side-to-side drift. */
    int hoverTicks;
    /** M7 part E: a vortex's cycle (design/enemies/ground/dust-devil; hashed only with a vortex). */
    final VortexCycle vortexCycle = new VortexCycle();
    /** M7 part E: a lane strafer's flight and pass trigger (design/enemies/air/ghost-drone; hashed only with one). */
    final LaneStrafing laneStrafing = new LaneStrafing();

    /** Its movement step (its flight, its stunned step). */
    final EnemyFlight flight = new EnemyFlight(this);
    /** Its (re)starts as a pooled unit. */
    final EnemyStarts starts = new EnemyStarts(this);
    /** Its trigger (its gun's clocks). */
    final EnemyGunnery gunnery = new EnemyGunnery(this);
    /** How it takes damage. */
    final EnemyDamage hurt = new EnemyDamage(this);

    /** A unit whose events go nowhere (for tests that fly one on its own). */
    Enemy() {
        this(new SimEvents(16));
    }

    /** A pooled unit reporting into {@code events}. */
    Enemy(SimEvents events) {
        this.events = events;
    }

    // ---- its (re)starts as a pooled unit: see EnemyStarts ----

    /** @param unitSerial unique among the units of an attempt, for the shots that lock onto it */
    void spawn(Spawn plan, int unitSerial) {
        starts.spawn(plan, unitSerial);
    }

    /** A unit released by a spawner (design/enemies/air/brood-pod; {@link EnemyStarts#hatch}). */
    void hatch(EnemySpec enemySpec, int enemyKind, double atX, double atY, double velX, double velY, int unitSerial) {
        starts.hatch(enemySpec, enemyKind, atX, atY, velX, velY, unitSerial);
    }

    /** M8 part E: placed by its wave's mount: a wall's nest, or a cave's swimmer ({@link EnemyStarts#mounted}). */
    void mounted(WallMounts.Placed placed) {
        starts.mounted(placed);
    }

    /** M8 part E: whether it is a nest on a wall's foot (design/enemies/naval/siren), scrolling with the ground. */
    public boolean wallMounted() {
        return phase == Phase.MOUNT;
    }

    /**
     * M8 part E: how far its sac's tell before its next ring has run between the previous and the current step, 0 to 1
     * (design/enemies/naval/siren); 0 outside the tell or for a unit without a song.
     */
    public double ringTell(double alpha) {
        return song.tell(alpha);
    }

    /** M8 part E: seconds to its next ring; NaN for a unit without a song. */
    public double secondsToRing() {
        return song.secondsToRing();
    }

    /** M8 part D: an escort peeling off its rising carrier ({@link EnemyStarts#peel}). */
    void peel(Spawn plan, double atX, double atY, double offsetX, double shipY, int unitSerial) {
        starts.peel(plan, atX, atY, offsetX, shipY, unitSerial);
    }

    /** A unit a boss's window launches (design/enemies/bosses, part G; {@link EnemyStarts#launch}). */
    void launch(
            EnemySpec enemySpec,
            int enemyKind,
            double atX,
            double atY,
            double velX,
            double velY,
            double glideSeconds,
            int unitSerial) {
        starts.launch(enemySpec, enemyKind, atX, atY, velX, velY, glideSeconds, unitSerial);
    }

    /**
     * An escort starts circling {@code circled} (null: it entered without a carrier, so it breaks
     * off at once from where it is).
     */
    void escort(Enemy circled) {
        escorting.circle(circled);
    }

    /** Whether it circles {@code unit} as an escort. */
    boolean escorts(Enemy unit) {
        return escorting.escorts(unit);
    }

    /** Its carrier ended: it circles the carrier's last position until it breaks off toward the ship. */
    void carrierEnded() {
        escorting.carrierEnded();
    }

    /**
     * A ground unit entering at the top edge at {@code atX}, in {@code inGroup} (-1 for none). Its
     * gun waits its first-shot delay from here.
     */
    void root(EnemySpec enemySpec, int enemyKind, double atX, int unitSerial, int inGroup) {
        root(enemySpec, enemyKind, atX, unitSerial, inGroup, 0, -1);
    }

    /** As {@link #root(EnemySpec, int, double, int, int)}, drifting along its nest's current ({@link EnemyStarts#root}). */
    void root(
            EnemySpec enemySpec,
            int enemyKind,
            double atX,
            int unitSerial,
            int inGroup,
            double currentX,
            double currentY) {
        starts.root(enemySpec, enemyKind, atX, unitSerial, inGroup, currentX, currentY);
    }

    /** A ground unit entering at a side edge (design/campaign Level 13; {@link EnemyStarts#rootAtSide}). */
    void rootAtSide(EnemySpec enemySpec, int enemyKind, SideEntry side, int unitSerial, int inGroup) {
        starts.rootAtSide(enemySpec, enemyKind, side, unitSerial, inGroup);
    }

    /** Links it into a chain as member {@code index} ({@link EnemyStarts#link}). */
    void link(
            Chain into,
            int index,
            EnemySpec memberSpec,
            int memberKind,
            Hitbox memberBox,
            int unitSerial,
            double atX,
            double atY,
            double heading) {
        starts.link(into, index, memberSpec, memberKind, memberBox, unitSerial, atX, atY, heading);
    }

    /** M5 part D: joins a flock as member {@code index} ({@link EnemyStarts#join}). */
    void join(Flock into, int index, Spawn plan, int unitSerial, double atX, double atY, double heading) {
        starts.join(into, index, plan, unitSerial, atX, atY, heading);
    }

    /**
     * M5 part H (design/campaign Level 14, Hazards; decision H2 = a): a ground unit just {@link #root
     * rooted} is a growth of root burst {@code burst}: it stands at ({@code atX}, {@code atY}) instead of
     * the top edge and grows for {@code ticks} steps, taking no damage and firing nothing; its gun fires
     * as soon as it can once it has grown.
     */
    void grow(int burst, double atX, double atY, int ticks) {
        rootBurst = burst;
        x = prevX = atX;
        y = prevY = atY;
        growTotal = Math.max(0, ticks);
        // + 1: it counts down in the step it appears already, which is step 0 of its growth.
        growTicks = growTotal + 1;
        volleyTicks = 1;
    }

    /** M5 part H: whether it is a root burst's growth still growing (it takes no damage and fires nothing). */
    public boolean growing() {
        return growTicks > 0;
    }

    /**
     * M5 part H: how far a root burst's growth has grown, between the previous and the current step
     * ({@code alpha} in [0, 1]): from 0 as it appears to 1 once grown; 1 for every other unit.
     */
    public double growShare(double alpha) {
        if (growTotal == 0) {
            return 1;
        }
        return Math.clamp((growTotal - growTicks + alpha) / growTotal, 0.0, 1.0);
    }

    /** M5 part H: the index of the root burst that grew it; -1 for a unit no burst grew. */
    public int rootBurst() {
        return rootBurst;
    }

    /** M5 part H: a boss's window launched it as an ambush (the Siege Spire's maw). */
    void launchedByBoss() {
        launched = true;
    }

    /** M5 part H: whether a boss's window launched it as an ambush (the Siege Spire's maw's Wraiths). */
    public boolean launched() {
        return launched;
    }

    /** M5 part E: whether a submerging unit's current layer is {@link Layer#SUB}, under the surface. */
    public boolean submerged() {
        return submerging.submerged();
    }

    /**
     * M5 part E: how far a submerging unit is through the swap running now, between the previous
     * and the current step, 0 to 1 (its layer flips at 0.5); -1 while none runs (for the looks).
     */
    public double swap(double alpha) {
        if (spec.submerge().isEmpty()) {
            return -1;
        }
        return submerging.swap(spec.submerge().get(), alpha);
    }

    /** M5 part E: whether the swap running now goes down (it dives); false for one that surfaces or none. */
    public boolean diving() {
        return submerging.diving();
    }

    /** M7 part B: its place in its wave (from 0): a swoop bends left for an even place, right for an odd one. */
    void wavePlace(int place) {
        swooping.wavePlace(place);
        // M7 part E: a vortex's cycle starts its place times the wave's offset later.
        vortexCycle.wavePlace(place);
        // M8 part E: a coil's cycle starts its place times the offset later; a pair orbits in opposite directions.
        coiling.wavePlace(place);
        if (hoverHold.driftsOnly()) {
            // M8 part G: a column of Depth Hunters fans out, an even place drifting left and an odd one right.
            hoverHold.driftSide(place % 2 == 0 ? -1 : 1);
        }
    }

    /** M7 part B: whether it is in its swoop (design/enemies/air/dust-kite). */
    public boolean swooping() {
        return phase == Phase.SWOOP;
    }

    /** M8 part C: whether it broke off to ram the ship and glints now (design/enemies/naval/glassfin, the tell). */
    public boolean ramGlinting() {
        return spec.flock().isPresent()
                && spec.flock().get().ram().isPresent()
                && ramming.glinting(spec.flock().get().ram().get());
    }

    /** M8 part C: whether it dives at the ship's lane in its ram (design/enemies/naval/glassfin). */
    public boolean ramDiving() {
        return ramming.diving();
    }

    /** M7 part B: whether its swoop started in this step (the {@link SimEvents.Type#SWOOP} event). */
    boolean swoopedNow() {
        return swooping.swoopedNow();
    }

    /** M7 part B: the side its swoop bends to, -1 left and 1 right (from its place in its wave). */
    public int swoopSide() {
        return swooping.side();
    }

    /** M7 part B: whether it is a Burrower (its stat block has a burrow). */
    public boolean burrowing() {
        return spec.burrow().isPresent();
    }

    /** M7 part B: where a Burrower is in its cycle; {@link BurrowPhase#NONE} for any other unit. */
    public BurrowPhase burrowPhase() {
        return burrowing.phase();
    }

    /**
     * M7 part B: how far a Burrower is through its current swim, bulge, surfaced window or dive, 0 to 1,
     * between the previous and the current step; 0 in its other phases (for the looks).
     */
    public double burrowProgress(double alpha) {
        return burrowing.progress(alpha);
    }

    /** M7 part B: a Burrower's surfacings so far (skipped ones included). */
    public int burrowSurfacings() {
        return burrowing.surfacings();
    }

    /** M7 part B: the spot a Burrower swims to, surfaces at or surfaced at, px. */
    public double burrowSpotX() {
        return burrowing.spotX();
    }

    /** M7 part B: the spot's height, px from the bottom edge. */
    public double burrowSpotY() {
        return frame.y(burrowing.spotY());
    }

    /** M7 part B: the level's no-surface zones its spots keep out of (set as it enters). */
    void surfaceZones(SurfaceZones zones) {
        burrowing.zones(zones);
    }

    /** M7 part B: the stagger clock of its wave in the {@link EnemyForce}; -1 for none. */
    void burrowGroup(int group) {
        burrowGroup = spec.burrow().isPresent() ? group : -1;
    }

    int burrowGroup() {
        return burrowGroup;
    }

    Burrowing burrow() {
        return burrowing;
    }

    /** M5 part E: whether its proximity ring may fire now (its cooldown from the last ring is over). */
    boolean ringReady() {
        return ringWait == 0;
    }

    /** M5 part E: it fired its proximity ring: the cooldown starts. */
    void ringFired() {
        ringWait = Math.max(1, SimStep.ticks(spec.ring().orElseThrow().cooldownSeconds()));
    }

    /**
     * Flies one step; {@code shipX}, {@code shipY} is where a unit that breaks off heads for, and
     * the ground scrolls by {@code groundScroll}. Returns false once it is gone: at the end of its
     * path without a hold, off the play field after leaving, or off the bottom edge for a ground unit.
     */
    boolean move(double shipX, double shipY, double groundScroll) {
        return move(shipX, shipY, groundScroll, shipX, shipY);
    }

    /**
     * As {@link #move(double, double, double)}, with a turret's barrel turning toward ({@code aimX},
     * {@code aimY}): the ship, or the convoy unit the target-the-objective hook chose this step.
     */
    boolean move(double shipX, double shipY, double groundScroll, double aimX, double aimY) {
        return move(shipX, shipY, groundScroll, aimX, aimY, 0);
    }

    /**
     * As {@link #move(double, double, double, double, double)}, then (M5 part F) pushed sideways by the
     * level's {@code wind}, px/s (design/campaign Level 12, Weather): a unit with a latch in every
     * state but latched, a Whirl Seed (a unit with a spiral) with its spiral's centre. The push adds
     * to its position after its own movement, so its facing does not turn with it; a wind of exactly
     * 0 changes nothing.
     */
    boolean move(double shipX, double shipY, double groundScroll, double aimX, double aimY, double wind) {
        return move(shipX, shipY, groundScroll, aimX, aimY, wind, shipX, shipY, false);
    }

    /**
     * As {@link #move(double, double, double, double, double, double)}; M5 part G: a diver whose pause
     * ends dives at ({@code diveX}, {@code diveY}), which is {@code objective} (the {@code always} hook's)
     * or the ship; it fires as its stat block says (passing the ship's height or its time into the dive).
     */
    boolean move(
            double shipX,
            double shipY,
            double groundScroll,
            double aimX,
            double aimY,
            double wind,
            double diveX,
            double diveY,
            boolean objective) {
        // M7 part G: the screen's points and scroll in its frame (unchanged for an unflipped unit).
        boolean flying = flight.fly(
                shipX,
                frame.y(shipY),
                frame.scroll(groundScroll),
                aimX,
                frame.y(aimY),
                diveX,
                frame.y(diveY),
                objective);
        if (flying && wind != 0) {
            if (spec.spiral().isPresent()) {
                double push = wind * SimStep.SECONDS;
                x += push;
                if (phase == Phase.SPIRAL) {
                    spiralling.push(push);
                }
            } else if (spec.latch().isPresent() && phase != Phase.LATCHED) {
                x += wind * SimStep.SECONDS;
            }
        }
        return flying;
    }

    /**
     * M7 part A, the EMP Burst (design/player/specials, EMP Burst): it is stunned for {@code ticks}
     * steps, restarting a running stun (stuns do not add up). Its own clock stops: it does not steer,
     * turn, fire or change state, and its timers resume where they stopped. An air unit drifts on along
     * its last step's movement; a unit on the ground stays where it is on the ground; a latched Lamprey
     * stays latched (its drain pauses: {@link #latched()} stays true, {@link #draining()} not). It stays
     * hittable and keeps its contact damage. A chain member is stunned with its chain ({@link Chain#empStun}).
     */
    void empStun(int ticks) {
        if (ticks <= 0) {
            return;
        }
        if (empStun == 0) {
            stunDx = x - prevX;
            stunDy = y - prevY;
        }
        empStun = ticks;
    }

    /** M7 part A: whether the EMP Burst's stun holds it now (the crackle overlay, the dimmed glow). */
    public boolean empStunned() {
        return empStun > 0;
    }

    /** M7 part A: steps left of the EMP Burst's stun; 0 when it is not stunned. */
    public int empStunTicks() {
        return empStun;
    }

    /** M7 part A: whether it is latched and draining the shield now (not while stunned). */
    boolean draining() {
        return phase == Phase.LATCHED && empStun == 0;
    }

    /**
     * M5 part F: it latches onto the ship at {@code ship}'s centre plus its {@code at} spot's offset;
     * it moves with the ship from now on.
     */
    void latch(int at, double shipX, double shipY) {
        chasing.latch(at, shipX, shipY);
    }

    /**
     * M5 part F: thrown off the hull (the shake-off): outward from its spot at the latch's throw speed,
     * slowing to a stop over its stun.
     */
    void shakeOff() {
        chasing.shakeOff();
    }

    /** M5 part F: where a chaser is in its life; {@link LatchPhase#NONE} for any other unit. */
    public LatchPhase latchPhase() {
        return chasing.phase();
    }

    /** M5 part F: whether it is latched onto the ship. */
    public boolean latched() {
        return phase == Phase.LATCHED;
    }

    /**
     * M5 part F: whether it may latch onto the ship when it touches it: a chaser flying in, chasing or
     * flying on that never latched (stunned or fleeing it does not even collide with the ship).
     */
    boolean latchable() {
        return chasing.latchable();
    }

    /** M5 part F: the hull spot it latched onto ({@link EnemySpec.Latch#LEFT} ...); -1 before it latched. */
    public int spot() {
        return chasing.spot();
    }

    /** M5 part F: how far into its stun it is, 0 to 1 (1 outside a stun). */
    public double stunProgress() {
        return chasing.stunProgress(phase == Phase.STUNNED);
    }

    /** M5 part D: where a rear ambush unit is on its way; {@link AmbushPhase#NONE} for any other unit. */
    public AmbushPhase ambushPhase() {
        return ambushing.phase();
    }

    /** M5 part D: whether it decloaked in this step (the {@link SimEvents.Type#DECLOAK} event). */
    boolean decloakedNow() {
        return ambushing.decloakedNow();
    }

    /**
     * M5 part D: whether it flies cloaked (design/enemies/air/wraith): a unit with a cloak before its
     * decloak, drawn as a shimmer on its stat block's layer; false for any other unit.
     */
    public boolean cloaked() {
        return spec.cloak().isPresent() && !ambushing.decloakedPhase();
    }

    /**
     * M5 part D: how far its decloak is, between the previous and the current step ({@code alpha} in
     * [0, 1]): 0 while it is cloaked, rising evenly to 1 over the flash, 1 after it and for a unit
     * without a cloak.
     */
    public double decloak(double alpha) {
        return spec.cloak().isEmpty() ? 1 : ambushing.decloak(alpha);
    }

    /** One step over the ground at {@code walkSpeed} px/s (M7 part D: a roll goes at the roll's speed). */
    void walk(double walkSpeed) {
        walking.step(walkSpeed);
    }

    /**
     * M5 part C (design/enemies/ground/ravager): a walker with a pounce leaps at the ship at
     * (shipX, shipY) when it is on the screen, ready (not leaping, its interval since the last
     * landing over) and the ship's centre is within its range; returns whether it took off now.
     * The leap overshoots (user decision 2026-10-07): its middle, the apex and the air window, passes
     * over the ship's position at take-off and it lands as far beyond, its landing point kept on the
     * play field (its centre at least half its hit box inside every edge; the second half of the leap
     * is then shorter), so a ship that holds still is touched and one that moves away is not.
     */
    boolean pounce(double shipX, double screenShipY) {
        double shipY = frame.y(screenShipY);
        if (!walking.active() || spec.pounce().isEmpty() || !pouncing.ready() || !PlayField.overlaps(x, y, box)) {
            return false;
        }
        double dx = shipX - x;
        double dy = shipY - y;
        double range = spec.pounce().get().range();
        if (dx * dx + dy * dy > range * range) {
            return false;
        }
        pouncing.takeOff(x, y, shipX, shipY, box.width() / 2, box.height() / 2);
        if (dx != 0 || dy != 0) {
            facing = heading(dx, dy);
        }
        return true;
    }

    /** Whether it is in a pounce's leap. */
    public boolean leaping() {
        return pouncing.leaping();
    }

    /** Whether its pounce landed in this step. */
    boolean landedNow() {
        return pouncing.landedNow();
    }

    /**
     * How far its leap is, from 0 at take-off to 1 at landing, between the previous and the current
     * step ({@code alpha} in [0, 1]); 0 while it is not leaping.
     */
    public double leapProgress(double alpha) {
        return pouncing.progress(alpha);
    }

    /**
     * Its drawn scale in a leap (design/enemies/ground/ravager): 1 at take-off and landing, its
     * pounce's scale at the apex, along a parabola; 1 while it is not leaping.
     */
    public double leapScale(double alpha) {
        if (!pouncing.leaping()) {
            return 1;
        }
        double p = leapProgress(alpha);
        return 1 + (spec.pounce().orElseThrow().scale() - 1) * 4 * p * (1 - p);
    }

    /**
     * The layer it is on now (M5 part C): its stat block's, except {@link Layer#AIR} in the air
     * window of a pounce's leap and (M5 part D) its cloak's layer from its decloak flash's start on.
     * Every hit, contact and targeting rule reads this.
     */
    public Layer layer() {
        if (spec.burrow().isPresent()) {
            // M7 part B: buried but while it is up (M7 = a).
            return burrowing.up() ? spec.layer() : Layer.BURIED;
        }
        if (pouncing.inAir()) {
            return Layer.AIR;
        }
        if (phase == Phase.VORTEX) {
            // M7 part E: risen on low-air while open, on the ground otherwise.
            return vortexCycle.open() ? Layer.LOW_AIR : spec.layer();
        }
        // M5 part D: a cloaked unit is on its decloaked layer from its flash's start on.
        if (spec.cloak().isPresent() && ambushing.decloakedPhase()) {
            return spec.cloak().get().layer();
        }
        // M5 part E: a submerging unit is under the surface from its dive's middle to its rise's.
        if (submerging.submerged()) {
            return Layer.SUB;
        }
        if (phase == Phase.RISE) {
            // M8 part D: a shadow on sub, on air from the rise's middle to the sink's.
            return rising.layer();
        }
        return spec.layer();
    }

    /**
     * Whether its body touches the craft on the play plane now (the ship, the wingman, the escort slot, the drones,
     * an air escort's units): its layer collides with them, except (M8 part D) in a rise before its glide.
     */
    boolean bodyTouches() {
        return layer().collidesWithPlayer() && !(phase == Phase.RISE && rising.harmless());
    }

    /** M8 part D: where a unit with a rise is in it (design/enemies/naval/abyss-ray); {@link RisePhase#NONE} else. */
    public RisePhase risePhase() {
        return phase == Phase.RISE ? rising.phase() : RisePhase.NONE;
    }

    /**
     * M8 part D: how far a unit with a rise is through its current phase of it, between the previous and the current
     * step, 0 to 1 (the rise and the sink flip its layer at 0.5); 0 for any other unit (for the looks).
     */
    public double riseProgress(double alpha) {
        return phase == Phase.RISE ? rising.progress(spec.rise().orElseThrow(), alpha) : 0;
    }

    /** M8 part D: its rise's glide direction, 1 to the right and -1 to the left; 0 for a unit without a rise. */
    public int riseDirection() {
        return phase == Phase.RISE ? rising.direction() : 0;
    }

    /** M8 part D: whether its rise flipped it to {@code air} in this step (its held escorts peel off then). */
    boolean risenNow() {
        return phase == Phase.RISE && rising.flippedNow();
    }

    /** M8 part D: whether its rise holds its escorts still (before the rise's middle). */
    boolean riseHolding() {
        return phase == Phase.RISE && rising.holding();
    }

    /**
     * A pounce touches {@code what} ({@link #TOUCHED_SHIP} or {@link #TOUCHED_WINGMAN}): returns
     * whether it is the leap's first touch of it, which deals its contact; once per leap.
     */
    boolean pounceTouch(int what) {
        return pouncing.touch(what);
    }

    /**
     * One step of a periodic spawner's cycle (design/enemies/ground/hive-node), after it moved:
     * {@link #SPAWN_RELEASE} when its units go now, {@link #SPAWN_TELEGRAPH} when its iris starts to
     * open, else {@link #SPAWN_NONE}. The cycle starts as its centre crosses the top edge; an
     * opening due while the ship at (shipX, shipY) is within the spawner's shut distance, or while
     * its centre is below the bottom edge, is skipped, and so is a release the ship came that close
     * to during the telegraph (the iris shuts again without it).
     */
    int spawnStep(double shipX, double shipY) {
        return spawnerIris.step(spec.spawner().orElseThrow(), x, y, shipX, frame.y(shipY));
    }

    /**
     * How far its iris is open (design/enemies/ground/hive-node): from 0 (shut) it opens evenly over
     * the telegraph to 1 at the release and shuts again over as long; 0 for other units.
     */
    public double iris() {
        return spec.spawner().isEmpty() ? 0 : spawnerIris.iris(spec.spawner().get());
    }

    /** Whether its iris is opening for a release now: the spawn's telegraph. */
    public boolean irisOpening() {
        return spawnerIris.opening();
    }

    /**
     * Whether a direct shot flying at (shotVx, shotVy) glances off a walker's frontal armour: it
     * arrives from within the armour's arc of the walker's facing.
     */
    boolean glances(double shotVx, double screenShotVy) {
        double shotVy = frame.vy(screenShotVy);
        if (armoured()) {
            // M7 part D: a curled shell sparks off every shot.
            return true;
        }
        return walking.glances(shotVx, shotVy);
    }

    /**
     * Counts down a walker's spit while it is on the screen; returns whether it spits this step:
     * when it is ready and the ship at (shipX, shipY) is more than its away angle off its facing.
     */
    boolean spit(double shipX, double screenShipY) {
        return walking.spit(shipX, frame.y(screenShipY));
    }

    /**
     * A walker of a staggered wave joins its wave's volley clock {@code clock} as unit {@code unit}
     * (from 0, in entry order); -1: it keeps its own clock.
     */
    void volley(int clock, int unit) {
        walking.volley(clock, unit);
    }

    /** Its wave's volley clock; -1 for a unit on its own clock. */
    int volleyGroup() {
        return walking.volleyGroup();
    }

    /**
     * Whether a walker of a staggered wave fires its fan this step (M5 part B, design/enemies/
     * ground/creeper): the wave's volley clock {@code clocks[volleyGroup]} starts as the first of
     * its units is on the screen; the first volley comes half an interval later and then one every
     * interval, unit i firing i × the stagger after the volley's start. A unit off the screen skips
     * its turn.
     */
    boolean staggeredVolley(int[] clocks) {
        return walking.staggeredVolley(clocks);
    }

    /** Whether a spawner's time ran out: it bursts on its own this step. */
    boolean burstDue() {
        return broodTicks >= 0
                && broodTicks >= SimStep.ticks(spec.brood().orElseThrow().afterSeconds());
    }

    /**
     * A spawner's seconds left before it bursts on its own (for the telegraph); its whole time
     * until its centre crosses the top edge, and infinite for other units.
     */
    public double burstSeconds() {
        if (spec.brood().isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        return spec.brood().get().afterSeconds() - Math.max(0, broodTicks) * SimStep.SECONDS;
    }

    /** A walker's distance walked over the ground in px (its walk cycle); 0 for other units. */
    public double walked() {
        return walking.walked();
    }

    /** Whether it is a walker. */
    public boolean walking() {
        return phase == Phase.WALK;
    }

    /** M5 part G: whether it is a lane walker (the Skimmer); its {@link #facing()} is its velocity's heading. */
    public boolean laneWalking() {
        return lane;
    }

    /**
     * M5 part G, the {@code alternate} hook: it starts an attack; returns how many it has started, this
     * one included.
     */
    int startAttack() {
        return ++attacks;
    }

    /** M5 part G: whom its running attack goes at: a convoy unit, -1 for the ship. */
    int attackTarget() {
        return attackTarget;
    }

    void attackTarget(int target) {
        attackTarget = target;
    }

    /**
     * M7 part D: starts its bomb run (design/campaign Level 17, What goes for the domes; defaults pending the
     * user's review): it flies to its row at height {@code rowY} (y up) and steers at its speed to its
     * dome's x plus {@code offset} (its slot in its wave), instead of its path.
     */
    void bombRun(double rowY, double offset) {
        bombRun(rowY, offset, 0);
    }

    /** Round 41: as {@link #bombRun(double, double)}, its load held for its first {@code runIn} steps on the play field. */
    void bombRun(double rowY, double offset, int runIn) {
        phase = Phase.BOMB;
        bombRun.start(frame.y(rowY), offset, x, runIn);
        entered = false;
    }

    /** Round 41: whether its bomb run's run-in is over, so it may drop its load over its dome. */
    boolean bombArmed() {
        return bombRun.armed();
    }

    /** M7 part D: the ground ally its bomb run steers to this step ({@code dome}, -1 for none), standing at {@code domeX}. */
    void bombTarget(int dome, double domeX) {
        bombRun.target(dome, domeX, box.width() / 2);
    }

    /** M7 part D: it dropped its load over its dome: it flies on straight down. */
    void dropBomb() {
        bombRun.drop();
    }

    /** M7 part D: whether it flies a bomb run with its load still aboard (it steers to a dome or looks for one). */
    boolean bombLoaded() {
        return phase == Phase.BOMB && !bombRun.spent();
    }

    /**
     * M7 part D: the ground ally its bomb run steers to now (design/campaign Level 17, What goes for the
     * domes); -1 when it flies none, its load is spent or no dome is left.
     */
    public int bombRun() {
        return bombRun.dome();
    }

    /** M7 part D: whether its bomb run's load is spent (dropped on a dome): it flies on straight down. */
    public boolean bombSpent() {
        return bombRun.spent();
    }

    /** M7 part D: whether it flies a bomb run (Level 17's hooked Spore Bombers), its load aboard or spent. */
    public boolean bombing() {
        return bombRun.bombing();
    }

    /** M5 part G: whether its dive went at the objective (the {@code always} hook). */
    public boolean divedAtObjective() {
        return divedAtObjective;
    }

    /** M5 part G: whether its crossing of the objective already dealt the dive's hit. */
    boolean diveHit() {
        return diveHit;
    }

    void markDiveHit() {
        diveHit = true;
    }

    /**
     * M7 part F: its turret's aim, radians clockwise from straight down (design/enemies/ground/warden-tank: drawn on
     * its own over the hull, which faces its path); the hull's {@link #facing()} for a unit without a turret.
     */
    public double turretAim() {
        return frame.facing(spec.turret().isPresent() ? aim : facing);
    }

    /** M7 part F: whether its turret has stopped turning for its burst (the tell, then the burst); false without one. */
    public boolean turretLocked() {
        return spec.turret().isPresent() && turretGun.locked();
    }

    /**
     * M7 part F (design/enemies/ground/warden-tank, the rear plate): the factor a direct shot flying at (shotVx,
     * shotVy) does to its body: its rear plate's multiplier when the shot arrives within the plate's half arc of
     * its rear (flies along its facing), 1 otherwise and for a unit without a rear plate.
     */
    double rearFactor(double shotVx, double screenShotVy) {
        return walking.rearFactor(shotVx, frame.vy(screenShotVy));
    }

    /**
     * M7 part F (design/campaign Level 19, Conveyors): a belt carries it {@code dy} px up the screen in this step (a
     * ground unit standing or walking on it).
     */
    void ride(double dy) {
        y += frame.vy(dy);
    }

    /**
     * M7 part F: whether a belt under it would carry it: a unit on the ground layer standing (a ground unit) or walking
     * (not in a pounce's leap).
     */
    boolean rides() {
        return (phase == Phase.GROUND || phase == Phase.WALK) && layer() == Layer.GROUND && !pouncing.leaping();
    }

    /** Places it on its path at its distance (see {@link EnemyFlight#place}). */
    void place() {
        flight.place();
    }

    /** The direction of a movement (y up) in radians clockwise from straight down the screen. */
    static double heading(double dx, double dy) {
        return StrictMath.atan2(-dx, -dy);
    }

    /**
     * Counts down its gun while it holds (a turret: while the ship is in its arc); returns whether it fires a shot this
     * step ({@link EnemyGunnery#trigger}). A diver fires once, in its dive.
     */
    boolean trigger() {
        return gunnery.trigger();
    }

    /**
     * The index of the shot {@link #trigger()} just let go in its burst, from 0 (M5 part D: a burst
     * fired straight up walks across its fan).
     */
    int burstShot() {
        return spec.gun().isPresent() ? spec.gun().get().burst() - 1 - burstLeft : 0;
    }

    /** Takes damage from a shot; returns whether it was destroyed. */
    boolean damage(double amount) {
        return damage(amount, false, false);
    }

    /** As {@link #damage(double, boolean, boolean)}, not a blast (shots, beams, the Lance, rams, weapon blasts). */
    boolean damage(double amount, boolean fromAbove) {
        return damage(amount, fromAbove, false);
    }

    /**
     * Takes damage; returns whether it was destroyed ({@link EnemyDamage#damage}). {@code fromAbove} marks damage that
     * comes down from above (bomb and shell blasts, the Airstrike); {@code blast} the specials' blasts, which a
     * vortex's closed vanes let through.
     */
    boolean damage(double amount, boolean fromAbove, boolean blast) {
        return hurt.damage(amount, fromAbove, blast, false);
    }

    /** As {@link #damage(double, boolean, boolean)}; M8 part E: {@code antiSub} fire cracks a closed coil's shell. */
    boolean damage(double amount, boolean fromAbove, boolean blast, boolean antiSub) {
        return hurt.damage(amount, fromAbove, blast, antiSub);
    }

    /** The hit points left. */
    public double hp() {
        return hp;
    }

    /** Simulation steps since it last took damage; {@link Integer#MAX_VALUE} before (the Targeting computer's HP bar). */
    public int ticksSinceHit() {
        return ticksSinceHit;
    }

    @Override
    public void addTo(StateHash hash) {
        EnemyHash.add(this, hash);
    }

    /** M5 part D: the air escort's units it overlapped in the last step, one bit per unit. */
    int allyContacts() {
        return allyContacts;
    }

    /** M5 part D: the air escort's units it overlaps now, one bit per unit. */
    void allyContacts(int units) {
        allyContacts = units;
    }

    double x() {
        return x;
    }

    /** Its height on the screen, px up from the bottom edge (M7 part G: its frame's mirrored for a flipped unit). */
    double y() {
        return frame.y(y);
    }

    /**
     * M7 part G: its height in its own frame ({@link TravelFrame}): what its flock and its chain place it by
     * ({@link #chainTo}); {@link #y()} for an unflipped unit.
     */
    double frameY() {
        return y;
    }

    /** M7 part G: its facing in its own frame (its flock's, its chain's and its roll's); {@link #facing()} unflipped. */
    double frameFacing() {
        return facing;
    }

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll; default pending the user's review): whether it was spawned
     * while the scroll ran reversed and lives in the mirrored frame (its "ahead" the screen's bottom).
     */
    public boolean flipped() {
        return frame.flipped();
    }

    /** M7 part G: from now on it lives in the mirrored frame (set as it enters, after its spawn placed it). */
    void flip() {
        frame = TravelFrame.FLIPPED;
    }

    /** M7 part G: its frame, to bring the screen's points into it (its own conversions are their own inverse). */
    TravelFrame frame() {
        return frame;
    }

    /**
     * Unique among the units of an attempt (a shot's lock; the presentation's once-per-unit cues,
     * such as the Vrell screech as a large unit enters the screen).
     */
    public int serial() {
        return serial;
    }

    /** Its ground group (a dock), or -1. */
    int group() {
        return group;
    }

    /** Its ground group (a dock, a battery), or -1; for the presentation. */
    public int groupIndex() {
        return group;
    }

    /** A turret's barrel, radians clockwise from straight down. */
    double aim() {
        return frame.facing(aim);
    }

    /** Whether it is a ground unit. */
    public boolean grounded() {
        return phase == Phase.GROUND;
    }

    /** M5 part G: its drift across on top of the scroll, px/s (a field unit, a raft, a side entry's floe); 0 for none. */
    public double driftX() {
        return driftX;
    }

    /** M5 part G: its drift up on top of the scroll, px/s; 0 for none. */
    public double driftY() {
        return frame.vy(driftY);
    }

    /** Whether it is a diver in its pause before the dive (its telegraph). */
    public boolean paused() {
        return phase == Phase.HOLD && spec.dive().isPresent();
    }

    boolean leadsTarget() {
        return leadsTarget;
    }

    Optional<PickupType> carried() {
        return carried;
    }

    public EnemySpec spec() {
        return spec;
    }

    /** Its enemy's index in {@link Sortie#enemyKinds()}. */
    public int kind() {
        return kind;
    }

    /**
     * Where it faces, in radians clockwise from straight down the screen: toward its direction of
     * flight, turning at most one 16-angle step per simulation step (art direction, Rotation).
     */
    public double facing() {
        return frame.facing(facing);
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return frame.y(prevY + (y - prevY) * alpha);
    }

    /** Whether its sweep is in its telegraph now. */
    public boolean telegraphing() {
        return sweepBeam.running()
                && empStun == 0
                && sweepBeam.telegraphing(spec.sweep().orElseThrow());
    }

    /** Whether its beam is sweeping now (M7 part A: not while the EMP Burst stuns it; it resumes after). */
    public boolean sweeping() {
        return sweepBeam.running()
                && empStun == 0
                && sweepBeam.sweeping(spec.sweep().orElseThrow());
    }

    /** Whether the sweep just entered its beam this step (for its sound). */
    boolean sweepStarted() {
        return sweepBeam.sweepStarted(spec.sweep().orElseThrow());
    }

    /** Whether the sweep's telegraph just started this step (for its sound). */
    boolean telegraphStarted() {
        return sweepBeam.telegraphStarted();
    }

    /** The sweep's centre, radians clockwise from straight down. */
    public double sweepCentre() {
        return frame.facing(sweepBeam.centre());
    }

    /** The sweep's first edge: from straight inward's side, radians clockwise from straight down. */
    public double sweepFrom() {
        double half = spec.sweep().orElseThrow().arcRadians() / 2;
        return frame.facing(x < PlayField.WIDTH / 2.0 ? sweepBeam.centre() - half : sweepBeam.centre() + half);
    }

    /** The sweep's last edge, toward straight down. */
    public double sweepTo() {
        double half = spec.sweep().orElseThrow().arcRadians() / 2;
        return frame.facing(x < PlayField.WIDTH / 2.0 ? sweepBeam.centre() + half : sweepBeam.centre() - half);
    }

    /**
     * The beam's heading now, radians clockwise from straight down, moving evenly from
     * {@link #sweepFrom()} to {@link #sweepTo()} over the sweep; {@code alpha} interpolates between steps.
     */
    public double beam(double alpha) {
        double into = sweepBeam.into(spec.sweep().orElseThrow(), alpha);
        double half = spec.sweep().orElseThrow().arcRadians() / 2;
        double from = x < PlayField.WIDTH / 2.0 ? sweepBeam.centre() - half : sweepBeam.centre() + half;
        double to = x < PlayField.WIDTH / 2.0 ? sweepBeam.centre() + half : sweepBeam.centre() - half;
        // M7 part G: along its frame's arc, then onto the screen.
        return frame.facing(from + (to - from) * Math.min(1, into));
    }

    /**
     * The beam's origin (its eye) from the unit's centre along x: toward the field it hovers over,
     * so mirrored on the right edge; with {@link #beamOffsetY()} where the beam, its hit test and
     * its telegraph start.
     */
    public double beamOffsetX() {
        double in = spec.sweep().orElseThrow().originIn();
        return x < PlayField.WIDTH / 2.0 ? in : -in;
    }

    /** The beam's origin from the unit's centre along y (up the field: its eye hangs below it). */
    public double beamOffsetY() {
        return frame.vy(-spec.sweep().orElseThrow().originDown());
    }

    /** Whether the current sweep hit the ship already. */
    boolean sweepHit() {
        return sweepBeam.hit();
    }

    void markSweepHit() {
        sweepBeam.markHit();
    }

    /** Its hit box: its stat block's, or a chain member's own. */
    public Hitbox hitbox() {
        // M7 part D: curled, its hit box is the ball.
        return curlRoll.armoured() ? spec.roll().get().ball() : box;
    }

    /** M5 part C: marks it as a unit of a wave tagged {@code waveTag} ("" for none). */
    void tag(String waveTag) {
        tag = waveTag;
    }

    /** M5 part C: the tag of its wave (Level 09's {@code bridge}); "" for none or a unit that is not a wave's. */
    public String tag() {
        return tag;
    }

    /** Marks it as a unit of a wave that entered from {@code edge}. */
    void entered(WaveSpec.Entry edge) {
        entry = edge;
    }

    /**
     * The edge its wave entered from; front for a unit that is not a wave's (a chain's member: see
     * {@link Chain#entry()}; a flock's member: see {@link Flock#entry()}); M5 part D: a rear ambush
     * unit's is the front until its re-entry.
     */
    public WaveSpec.Entry entry() {
        // M5 part D: a rear ambush unit's wave is a rear wave from its re-entry on.
        if (ambushing.phase() == AmbushPhase.SWOOP || ambushing.phase() == AmbushPhase.GAP) {
            return WaveSpec.Entry.FRONT;
        }
        return entry;
    }

    /** It carries {@code pickup}, dropped when it is destroyed (a chain's head carries its wave's). */
    void carry(Optional<PickupType> pickup) {
        carried = pickup;
    }

    /**
     * M7 part C (design/campaign Level 16, What walls stop): moved across to {@code toX}, kept off rock (a
     * swoop clamped to the flyable side, no damage).
     */
    void clampX(double toX) {
        x = toX;
    }

    /** Its chain moved it to (atX, atY), facing {@code heading}. */
    void chainTo(double atX, double atY, double heading) {
        prevX = x;
        prevY = y;
        x = atX;
        y = atY;
        facing = heading;
    }

    /** Moves it to another chain as member {@code index} (the rear part of a cut). */
    void relink(Chain into, int index) {
        chain = into;
        link = index;
    }

    /** The chain it is a member of; null for other units. */
    public Chain chain() {
        return chain;
    }

    /** Its place in its chain, 0 the head. */
    public int link() {
        return link;
    }

    // ---- M7 part D: aura rings (design/enemies/air/choir-herald), stepped by Auras ----

    /** M7 part D: its aura ring, stepped by {@link Auras}. */
    AuraRing auraRing() {
        return auraRing;
    }

    /**
     * M7 part D: whether Herald {@code herald}'s aura reaches this unit now: another unit than a Herald, not a
     * chain's member, not buried nor under the water, its centre within the aura's radius of the Herald's.
     */
    boolean inAura(Enemy herald, double radius) {
        if (this == herald || spec.aura().isPresent() || chain != null) {
            return false;
        }
        Layer now = layer();
        if (now == Layer.BURIED || now == Layer.SUB) {
            return false;
        }
        double dx = x - herald.x;
        // M7 part G: on the screen (the Herald may live in the other frame).
        double dy = y() - herald.y();
        return dx * dx + dy * dy <= radius * radius;
    }

    /** M7 part D: whether a Herald sings now: alive on the play field and not stunned by the EMP Burst. */
    boolean singing() {
        return spec.aura().isPresent() && empStun == 0 && PlayField.overlaps(x, y, box);
    }

    /**
     * {@link Auras#step}: Herald {@code herald} rings it from now (it entered the radius, or its old feeder
     * lost it): its maximum is its HP at the level, capped by the aura, and it fills over the aura's fill time.
     */
    void claimRing(Enemy herald) {
        auraRing.claim(herald.spec.aura().get(), herald.serial, spec.hp());
    }

    /** M7 part D, its Herald died: its ring collapses over the aura's collapse time. */
    void collapseRing() {
        auraRing.collapse();
    }

    /**
     * M7 part D, the EMP Burst (design/player/specials): its ring is stripped to nothing (its Herald keeps
     * it, the refill waits its delay again); returns whether it had one.
     */
    boolean stripRing() {
        return auraRing.strip();
    }

    /** M7 part D: its aura ring's damage left; 0 without one. */
    public double ring() {
        return auraRing.ring();
    }

    /** M7 part D: its aura ring's maximum (its HP at the level, capped by the aura); 0 without a ring. */
    public double ringMax() {
        return auraRing.ringMax();
    }

    /** M7 part D: its aura ring's share left, 0 to 1; 0 without a ring. */
    public double ringShare() {
        return auraRing.ringShare();
    }

    /** M7 part D: the serial of the Herald that feeds its ring (the thread of light to it); -1 for none. */
    public int ringFeeder() {
        return auraRing.ringFeeder();
    }

    /** M7 part D: how far its ring's collapse at its Herald's death is, 0 to 1; 0 while none runs. */
    public double ringCollapse() {
        return auraRing.ringCollapse();
    }

    // ---- M7 part D: a unit part (design/enemies/ground/shellback, the mortar vent); M8 part B: several ----

    /** M8 part B: the number of its unit parts (design/enemies/ground/drill-rig: 4 pumps); 0 without. */
    public int partCount() {
        return partHp.count();
    }

    /** M8 part B: the number of its unit parts still standing. */
    public int partsStanding() {
        return partHp.standing();
    }

    /** M7 part D: whether its (first) unit part stands; false for a unit without one. */
    public boolean partAlive() {
        return partHp.alive(0);
    }

    /** M8 part B: whether its unit part {@code p} stands; false for a part it does not have. */
    public boolean partAlive(int p) {
        return partHp.alive(p);
    }

    /** M7 part D: its (first) unit part's HP share left, 0 to 1; 0 without one or once it is destroyed. */
    public double partShare() {
        return partShare(0);
    }

    /** M8 part B: its unit part {@code p}'s HP share left, 0 to 1; 0 for a part it does not have. */
    public double partShare(int p) {
        return p < partHp.count() ? partHp.share(p, spec.parts().get(p)) : 0;
    }

    /** M7 part D: its (first) unit part's centre, px (its offset turned with its facing); its own centre without one. */
    public double partX() {
        return partX(0);
    }

    /**
     * M8 part B: its unit part {@code p}'s centre, px: its offset turned with a walker's facing; a unit that does not
     * walk keeps its parts in place (the Drill Rig's fixed collar). Its own centre for a part it does not have.
     */
    public double partX(int p) {
        if (p >= partHp.count()) {
            return x;
        }
        return PartHp.x(spec.parts().get(p), x, partFacing());
    }

    /** M7 part D: its (first) unit part's centre's height, px. */
    public double partY() {
        return partY(0);
    }

    /** M8 part B: its unit part {@code p}'s centre's height, px. */
    public double partY(int p) {
        if (p >= partHp.count()) {
            return y();
        }
        return frame.y(PartHp.y(spec.parts().get(p), y, partFacing()));
    }

    /** M8 part B: the facing its parts turn with: a walker's own; straight down (no turn) for any other unit. */
    private double partFacing() {
        return spec.walker().isPresent() ? facing : 0;
    }

    /** M7 part D: its (first) unit part's hit box; its own without one. */
    Hitbox partBox() {
        return partBox(0);
    }

    /** M8 part B: its unit part {@code p}'s hit box; its own for a part it does not have. */
    Hitbox partBox(int p) {
        return p < partHp.count() ? spec.parts().get(p).hitbox() : box;
    }

    /** M7 part D: whether its standing (first) part lies in a blast of {@code radius} around (bx, by). */
    boolean partInBlast(double bx, double by, double radius) {
        return partInBlast(0, bx, by, radius);
    }

    /** M8 part B: whether its standing part {@code p} lies in a blast of {@code radius} around (bx, by). */
    boolean partInBlast(int p, double bx, double by, double radius) {
        return partAlive(p) && PlayerFire.inBlast(bx, by, radius, partX(p), partY(p), partBox(p));
    }

    /** M7 part D: its standing (first) part takes damage; returns whether this destroyed it. */
    boolean damagePart(double amount, boolean fromAbove) {
        return damagePart(0, amount, fromAbove);
    }

    /** M7 part D: its standing part {@code p} takes damage; returns whether this destroyed it ({@link EnemyDamage}). */
    boolean damagePart(int p, double amount, boolean fromAbove) {
        return hurt.damagePart(p, amount, fromAbove);
    }

    /**
     * M7 part D: whether its gun is quiet now: curled, or silenced for good with its part gone (M8 part B: every part
     * that carries it).
     */
    boolean gunQuiet() {
        if (curlRoll.phase() != CurlPhase.WALK) {
            return true;
        }
        return partHp.silenced(spec.parts());
    }

    // ---- M7 part D: the curl-roll (design/enemies/ground/shellback) ----

    /**
     * M7 part D: whether it is curled: armoured against every damage, its hit box the ball. M7 part E: or a vortex
     * that is not open: its vanes glance off every shot (the specials' blasts excepted, see {@link #damage(double,
     * boolean, boolean)}).
     */
    public boolean armoured() {
        return curlRoll.armoured() || vanesClosed() || shellClosed();
    }

    /**
     * M8 part E: whether it is a coiling unit whose shell is closed (design/enemies/naval/spiral-nautilus): it glances
     * off all but the specials' blasts and {@code anti-sub} fire.
     */
    boolean shellClosed() {
        return phase == Phase.COIL && coiling.closed();
    }

    /** M8 part E: where its coil is in its cycle; {@link CoilPhase#NONE} for a unit without one. */
    public CoilPhase coilPhase() {
        return phase == Phase.COIL ? coiling.phase() : CoilPhase.NONE;
    }

    /** M8 part E: how far into its coil's current phase it is, 0 to 1; 0 for a unit without one. */
    public double coilProgress() {
        return phase == Phase.COIL ? coiling.progress() : 0;
    }

    /** M8 part E: whether its crown glows before it opens (its coil's tell); false for a unit without one. */
    public boolean coilTell() {
        return phase == Phase.COIL && coiling.tell();
    }

    /** M8 part E: whether its coil orbits clockwise (the shell's roll); false for a unit without one. */
    public boolean coilClockwise() {
        return phase == Phase.COIL && coiling.clockwise();
    }

    /** M8 part F: where its lunge is (design/enemies/naval/glow-angler); {@link LurePhase#NONE} for a unit without one. */
    public LurePhase lurePhase() {
        return phase == Phase.LURE ? lunging.phase() : LurePhase.NONE;
    }

    /** M8 part F: whether it is a lunging unit (its stat block has a lunge). */
    boolean lunges() {
        return phase == Phase.LURE;
    }

    /** M8 part E: whether it is a coiling unit (its stat block has a coil). */
    boolean coils() {
        return phase == Phase.COIL;
    }

    /** M7 part E: whether it is a vortex whose vanes are closed (it is not open). */
    boolean vanesClosed() {
        return phase == Phase.VORTEX && !vortexCycle.open();
    }

    // ---- M7 part E: the vortex cycle (design/enemies/ground/dust-devil) ----

    /** M7 part E: where its vortex is in its cycle; {@link VortexPhase#CLOSED} for a unit without one. */
    public VortexPhase vortexPhase() {
        return phase == Phase.VORTEX ? vortexCycle.phase() : VortexPhase.CLOSED;
    }

    /** M7 part E: how far into its vortex's current phase it is, 0 to 1; 0 for a unit without one. */
    public double vortexProgress() {
        return phase == Phase.VORTEX ? vortexCycle.progress() : 0;
    }

    /** M7 part E: whether it is a vortex in its cycle (its stat block has a vortex). */
    boolean vortex() {
        return phase == Phase.VORTEX;
    }

    /** M7 part E: its vortex's cycle, for the {@link EnemyForce}'s events and spiral. */
    VortexCycle vortexCycle() {
        return vortexCycle;
    }

    /** M7 part D: where it is in its curl-roll; {@link CurlPhase#WALK} for a unit without one. */
    public CurlPhase curlPhase() {
        return curlRoll.phase();
    }

    /** M7 part D: how far into the curl-roll's current phase it is, 0 to 1; 0 while it walks. */
    public double curlProgress() {
        if (curlRoll.phase() == CurlPhase.WALK) {
            return 0;
        }
        return curlRoll.progress(spec.roll().get());
    }

    /** M7 part D: whether it is rolling now (its ball crushes ground targets). */
    boolean rolling() {
        return curlRoll.phase() == CurlPhase.ROLLING;
    }

    /** M7 part D: whether this roll crushed ground ally {@code k} already. */
    boolean crushedAlly(int k) {
        return curlRoll.crushedAlly(k);
    }

    /** M7 part D: this roll crushed ground ally {@code k}. */
    void markCrushed(int k) {
        curlRoll.markCrushed(k);
    }

    /** M5 part D: the flock it is a member of; null for other units. */
    public Flock flock() {
        return flock;
    }

    /** M5 part D: its place in its flock, from 0 in entry order. */
    public int member() {
        return member;
    }

    // ---- M7 part E: the lane strafe (design/enemies/air/ghost-drone) ----

    /** M7 part E: where its lane strafe is in its flight; {@link StrafePhase#DESCENT} for a unit without one. */
    public StrafePhase strafePhase() {
        return phase == Phase.STRAFE ? laneStrafing.phase() : StrafePhase.DESCENT;
    }

    /** M7 part E: whether it is a lane strafer in its flight (its stat block has a lane strafe). */
    public boolean laneStrafer() {
        return phase == Phase.STRAFE;
    }

    /** M7 part E: a lane strafer's direction now, 1 to the right and -1 to the left; 0 for any other unit. */
    public int strafeSide() {
        return phase == Phase.STRAFE ? laneStrafing.side() : 0;
    }

    /** M7 part E: whether a lane strafer's eye glints now (its pass trigger's tell runs before its burst). */
    public boolean eyeGlinting() {
        return phase == Phase.STRAFE && laneStrafing.glinting();
    }

    /** M7 part E: its lane strafe, for the {@link EnemyForce}'s pass trigger. */
    LaneStrafing laneStrafing() {
        return laneStrafing;
    }
}
