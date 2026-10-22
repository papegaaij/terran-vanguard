package vanguard.sim;

import java.util.Optional;

/**
 * The (re)starts of one pooled {@link Enemy} (split out of {@link Enemy}): a planned unit's spawn, a spawner's
 * hatch, a boss's launch, a ground unit's root, a chain's link and a flock's join, each from a cleared unit.
 */
final class EnemyStarts {
    private final Enemy u;

    EnemyStarts(Enemy unit) {
        this.u = unit;
    }

    /** @param unitSerial unique among the units of an attempt, for the shots that lock onto it */
    void spawn(Spawn plan, int unitSerial) {
        clearLevel04();
        u.serial = unitSerial;
        u.group = -1;
        u.aim = 0;
        u.inArc = false;
        u.diveTicks = 0;
        u.diveFired = false;
        u.diveShot = false;
        u.spec = plan.enemy();
        u.box = u.spec.hitbox();
        u.kind = plan.kind();
        u.path = plan.path();
        u.segment = 0;
        u.distance = 0;
        u.speed = plan.speed();
        u.phase = Enemy.Phase.ENTER;
        u.holdTicks = SimStep.ticks(plan.holdSeconds());
        u.orbit = plan.orbit();
        u.exit = plan.exit();
        u.hp = u.spec.hp();
        u.partHp.start(u.spec);
        u.ticksSinceHit = Integer.MAX_VALUE;
        u.burstLeft = 0;
        u.leadsTarget = plan.leadsTarget();
        u.carried = plan.carried();
        u.place();
        u.spiralling.reset(u.spec.spiral().isPresent() ? u.spec.spiral().get().ricochets() : 0);
        if (plan.release().isPresent() && u.spec.spiral().isPresent()) {
            Spawn.Release release = plan.release().get();
            u.phase = Enemy.Phase.SPIRAL;
            u.x = release.x();
            u.y = release.y();
            u.spiralling.release(u.x, u.y, release.angle());
        }
        if (u.spec.gun().isPresent() && u.spec.gun().get().mine().isPresent()) {
            u.volleyTicks = SimStep.ticks(u.spec.gun().get().intervalSeconds());
        }
        u.prevX = u.x;
        u.prevY = u.y;
        u.facing = Enemy.heading(u.path.dx(u.segment), u.path.dy(u.segment));
        if (plan.escort().isPresent()) {
            Spawn.Escort circle = plan.escort().get();
            u.phase = Enemy.Phase.ESCORT;
            u.escorting.start(circle);
            u.angle = circle.angle();
            u.volleyTicks =
                    u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        }
        if (plan.walk().isPresent()) {
            WalkPath walkPath = plan.walk().get();
            u.walking.start(walkPath, u.spec.walker().orElseThrow());
            u.phase = Enemy.Phase.WALK;
            u.x = u.prevX = walkPath.x(0);
            u.y = u.prevY = walkPath.y(0);
            u.facing = walkPath.points() > 1 ? Enemy.heading(walkPath.x(1) - u.x, walkPath.y(1) - u.y) : StrictMath.PI;
            u.turretGun.start();
            if (u.spec.turret().isPresent()) {
                // M7 part F: the turret starts along the hull.
                u.aim = u.facing;
            }
            // The first volleys come half an interval after it walks onto the screen.
            u.volleyTicks =
                    u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().intervalSeconds() / 2) : 0;
            if (u.spec.pounce().isPresent()) {
                u.pouncing.start(u.spec.pounce().get());
            }
        }
        if (plan.field().isPresent()) {
            // M5 part E: a field unit lies on the sea from its appearance, scrolling and drifting.
            Spawn.Field field = plan.field().get();
            u.phase = Enemy.Phase.GROUND;
            u.x = u.prevX = field.x();
            u.y = u.prevY = field.y();
            u.driftX = field.vx();
            u.driftY = field.vy();
            u.facing = 0;
            submerge(field.submerged(), field.seed(), true);
        }
        if (u.spec.burrow().isPresent()) {
            // M7 part B: a Burrower starts buried where it appears and scrolls in with the ground.
            u.phase = Enemy.Phase.BURROW;
            u.driftX = 0;
            u.driftY = 0;
            u.facing = 0;
            long seed = plan.field().isPresent() ? plan.field().get().seed() : 0x9E3779B97F4A7C15L * (unitSerial + 1L);
            u.burrowing.start(u.spec.burrow().get(), u.x, u.y, seed, SurfaceZones.NONE);
        }
        if (u.spec.vortex().isPresent()) {
            // M7 part E: a vortex starts closed where it appears; its centre line is its entry's x.
            u.phase = Enemy.Phase.VORTEX;
            u.facing = 0;
            EnemyGun.SpiralSpec spiralGun =
                    u.spec.gun().isPresent() && u.spec.gun().get().spiral().isPresent()
                            ? u.spec.gun().get().spiral().get()
                            : null;
            u.vortexCycle.start(
                    u.spec.vortex().get(),
                    spiralGun,
                    u.x,
                    u.y,
                    0x5DEECE66DL ^ (0x9E3779B97F4A7C15L * (unitSerial + 1L)));
        }
        if (u.spec.laneStrafe().isPresent() && u.phase == Enemy.Phase.ENTER) {
            // M7 part E: a lane strafer flies by its strafe from its planned path's start toward its end.
            u.phase = Enemy.Phase.STRAFE;
            u.laneStrafing.start(
                    u.spec.laneStrafe().get(), u.path.startX(), u.path.startY(), u.path.endX(), u.path.endY());
            u.x = u.prevX = u.laneStrafing.x();
            u.y = u.prevY = u.laneStrafing.y();
            u.facing =
                    u.laneStrafing.phase() == Enemy.StrafePhase.DESCENT ? 0 : Enemy.heading(u.laneStrafing.side(), 0);
        }
        if (u.spec.chase().isPresent() && u.phase == Enemy.Phase.ENTER) {
            // M5 part F: a chaser flies in along its path; its chase starts its delay after its entry.
            u.chasing.enter(SimStep.ticks(plan.chaseDelay()));
        }
        if (plan.lane()) {
            // M5 part G: a lane walker starts on its lane, facing along it; its gun counts down on the screen.
            u.lane = true;
            u.phase = Enemy.Phase.LANE;
            u.flight.placeLane();
            u.prevX = u.x;
            u.prevY = u.y;
            u.facing = Enemy.heading(u.path.dx(0), u.path.dy(0));
            u.volleyTicks =
                    u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        }
        if (plan.ambush().isPresent()) {
            u.phase = Enemy.Phase.AMBUSH;
            u.ambushing.start(plan.ambush().get(), Math.max(1, plan.ambushReentryTick() - plan.tick()));
        }
    }

    /** The fields of Level 04's spawners, escorts and walkers back to a plain unit's. */
    void clearLevel04() {
        u.frame = TravelFrame.SCREEN;
        u.entry = WaveSpec.Entry.FRONT;
        u.tag = "";
        u.chain = null;
        u.link = 0;
        u.sweepBeam.clear();
        u.broodTicks = -1;
        u.escorting.clear();
        u.walking.clear();
        u.scrolled = 0;
        u.entered = false;
        u.glideTicks = 0;
        u.glideHoldTicks = 0;
        u.spawnerIris.clear();
        u.pouncing.clear();
        u.allyContacts = 0;
        u.ambushing.clear();
        u.flock = null;
        u.member = 0;
        u.driftX = 0;
        u.driftY = 0;
        u.submerging.clear();
        u.ringWait = 0;
        u.chasing.clear();
        u.lane = false;
        u.attacks = 0;
        u.attackTarget = -1;
        u.divedAtObjective = false;
        u.diveHit = false;
        u.bombRun.clear();
        u.growTicks = 0;
        u.growTotal = 0;
        u.rootBurst = -1;
        u.launched = false;
        u.empStun = 0;
        u.stunDx = 0;
        u.stunDy = 0;
        u.offX = 0;
        u.offY = 0;
        u.burrowing.clear();
        u.burrowGroup = -1;
        u.swooping.clear();
        u.ramming.clear();
        u.auraRing.reset();
        u.absorbQuiet = 0;
        u.partHp.clear();
        u.curlRoll.clear();
        u.hoverTicks = 0;
        u.vortexCycle.clear();
        u.laneStrafing.clear();
    }

    /**
     * A unit released by a spawner at (atX, atY), flying straight out at (velX, velY) px/s until
     * it leaves the play field (design/enemies/air/brood-pod).
     */
    void hatch(EnemySpec enemySpec, int enemyKind, double atX, double atY, double velX, double velY, int unitSerial) {
        clearLevel04();
        u.spec = enemySpec;
        u.box = u.spec.hitbox();
        u.kind = enemyKind;
        u.serial = unitSerial;
        u.group = -1;
        u.aim = 0;
        u.inArc = false;
        u.diveTicks = 0;
        u.diveFired = false;
        u.diveShot = false;
        u.segment = 0;
        u.distance = 0;
        u.speed = Math.sqrt(velX * velX + velY * velY);
        u.phase = Enemy.Phase.LEAVE;
        u.holdTicks = 0;
        u.orbit = Optional.empty();
        u.exit = Spawn.Exit.DOWN;
        u.hp = u.spec.hp();
        u.partHp.start(u.spec);
        u.ticksSinceHit = Integer.MAX_VALUE;
        u.volleyTicks = 0;
        u.burstLeft = 0;
        u.burstTicks = 0;
        u.leadsTarget = false;
        u.carried = Optional.empty();
        u.spiralling.reset(0);
        u.x = u.prevX = atX;
        u.y = u.prevY = atY;
        u.vx = velX;
        u.vy = velY;
        u.facing = Enemy.heading(velX, velY);
    }

    /**
     * A unit a boss's window launches at (atX, atY) (design/enemies/bosses, part G): it flies
     * straight out at (velX, velY) px/s; with a glide it holds after {@code glideSeconds} for its
     * hover time (the middle of its range), its gun firing, and then leaves down the screen at that
     * speed; without one it flies on until it leaves the play field.
     */
    void launch(
            EnemySpec enemySpec,
            int enemyKind,
            double atX,
            double atY,
            double velX,
            double velY,
            double glideSeconds,
            int unitSerial) {
        hatch(enemySpec, enemyKind, atX, atY, velX, velY, unitSerial);
        if (glideSeconds > 0 && enemySpec.hover().isPresent()) {
            Range hover = enemySpec.hover().get().seconds();
            u.glideTicks = Math.max(1, SimStep.ticks(glideSeconds));
            u.glideHoldTicks = Math.max(1, SimStep.ticks((hover.min() + hover.max()) / 2));
        }
    }

    /**
     * As {@link Enemy#root(EnemySpec, int, double, int, int)}; M5 part E: a unit whose stat block drifts
     * (a raft, design/enemies/naval/reef-spitter) drifts at that speed along its nest's current,
     * given as the current's direction ({@code currentX}, {@code currentY}: its sine and minus its
     * cosine from straight down, worked out once when the level is set up, since {@link StrictMath}'s
     * sine allocates).
     */
    void root(
            EnemySpec enemySpec,
            int enemyKind,
            double atX,
            int unitSerial,
            int inGroup,
            double currentX,
            double currentY) {
        clearLevel04();
        u.spec = enemySpec;
        u.box = u.spec.hitbox();
        u.kind = enemyKind;
        u.serial = unitSerial;
        u.group = inGroup;
        u.phase = Enemy.Phase.GROUND;
        u.x = u.prevX = atX;
        u.y = u.prevY = PlayField.HEIGHT + u.box.height() / 2;
        u.hp = u.spec.hp();
        u.partHp.start(u.spec);
        u.ticksSinceHit = Integer.MAX_VALUE;
        // M7 part F: a turret whose arc faces up the screen ({@code arc: up}) starts along it; 0 for any other.
        u.aim = u.spec.gun().isPresent() ? u.spec.gun().get().arcFacingRadians() : 0;
        u.facing = u.aim;
        u.inArc = false;
        u.burstLeft = 0;
        u.leadsTarget = false;
        u.carried = Optional.empty();
        u.volleyTicks =
                u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        u.diveFired = u.diveShot = false;
        u.diveTicks = 0;
        if (u.spec.drift() > 0) {
            u.driftX = u.spec.drift() * currentX;
            u.driftY = u.spec.drift() * currentY;
        }
    }

    /**
     * M5 part G (design/campaign Level 13, the mortar floe): a ground unit entering just outside a side
     * edge at its {@code side}'s height and drifting across at its drift on top of the scroll; its wave
     * counts as a side entry (the wingman's formation).
     */
    void rootAtSide(EnemySpec enemySpec, int enemyKind, SideEntry side, int unitSerial, int inGroup) {
        root(enemySpec, enemyKind, 0, unitSerial, inGroup, 0, -1);
        double half = u.box.width() / 2;
        u.x = u.prevX = side.fromLeft() ? -half : PlayField.WIDTH + half;
        u.y = u.prevY = side.y();
        u.driftX = side.fromLeft() ? side.drift() : -side.drift();
        u.driftY = 0;
        u.entry = WaveSpec.Entry.SIDES;
    }

    /**
     * M5 part E: a submerging unit starts {@code under} the surface or on it, its swap timer seeded
     * with {@code seed}; a field's units start at a seeded point of their first wait ({@code
     * anyPoint}: a wait drawn from 0 to the longest), so a field swaps on the screen and not in step.
     */
    private void submerge(boolean under, long seed, boolean anyPoint) {
        if (u.spec.submerge().isEmpty()) {
            return;
        }
        u.submerging.start(u.spec.submerge().get(), under, seed, anyPoint);
    }

    /**
     * Links it into {@code into} as member {@code index} at (atX, atY), facing {@code heading}: a
     * chain's head, segment or tail with its own hit box. A head's gun waits its first-shot delay.
     */
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
        clearLevel04();
        u.chain = into;
        u.link = index;
        u.spec = memberSpec;
        u.box = memberBox;
        u.kind = memberKind;
        u.serial = unitSerial;
        u.group = -1;
        u.aim = 0;
        u.inArc = false;
        u.diveTicks = 0;
        u.diveFired = false;
        u.diveShot = false;
        u.segment = 0;
        u.distance = 0;
        u.speed = 0;
        u.phase = Enemy.Phase.CHAIN;
        u.holdTicks = 0;
        u.orbit = Optional.empty();
        u.exit = Spawn.Exit.DOWN;
        u.hp = u.spec.hp();
        u.partHp.start(u.spec);
        u.ticksSinceHit = Integer.MAX_VALUE;
        u.volleyTicks =
                u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        u.burstLeft = 0;
        u.burstTicks = 0;
        u.leadsTarget = false;
        u.carried = Optional.empty();
        u.spiralling.reset(0);
        u.vx = 0;
        u.vy = 0;
        u.x = u.prevX = atX;
        u.y = u.prevY = atY;
        u.facing = heading;
    }

    /**
     * M5 part D: joins {@code into} as member {@code index} at (atX, atY), facing {@code heading}: a
     * swarm's member, placed by its flock from now on (design/enemies/air/mote-swarm).
     */
    void join(Flock into, int index, Spawn plan, int unitSerial, double atX, double atY, double heading) {
        clearLevel04();
        u.flock = into;
        u.member = index;
        u.spec = plan.enemy();
        u.box = u.spec.hitbox();
        u.kind = plan.kind();
        u.serial = unitSerial;
        u.group = -1;
        u.aim = 0;
        u.inArc = false;
        u.diveTicks = 0;
        u.diveFired = false;
        u.diveShot = false;
        u.path = plan.path();
        u.segment = 0;
        u.distance = 0;
        u.speed = plan.speed();
        u.phase = Enemy.Phase.FLOCK;
        u.holdTicks = 0;
        u.orbit = Optional.empty();
        u.exit = Spawn.Exit.DOWN;
        u.hp = u.spec.hp();
        u.partHp.start(u.spec);
        u.ticksSinceHit = Integer.MAX_VALUE;
        u.volleyTicks = 0;
        u.burstLeft = 0;
        u.burstTicks = 0;
        u.leadsTarget = false;
        u.carried = plan.carried();
        u.spiralling.reset(0);
        u.vx = 0;
        u.vy = 0;
        u.x = u.prevX = atX;
        u.y = u.prevY = atY;
        u.facing = heading;
    }
}
