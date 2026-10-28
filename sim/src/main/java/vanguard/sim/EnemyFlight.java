package vanguard.sim;

/**
 * One {@link Enemy}'s movement step (split out of {@link Enemy}): its phase's flight, path, hold, orbit, lane, walk,
 * the ground's scroll and drift, a turret's tracking, a sweep's start, and its stunned step under the EMP Burst. It
 * moves its unit through the unit's movement fields and its behaviour components ({@link Chasing} ...).
 */
final class EnemyFlight {
    /** The most its facing turns in one step: one step of a 16-angle set (art direction, Rotation). */
    private static final double MAX_TURN = StrictMath.PI / 8;

    private final Enemy u;

    EnemyFlight(Enemy unit) {
        this.u = unit;
    }

    /**
     * One step of its own movement, its timers and its cycles (see {@link Enemy#move(double, double, double, double,
     * double, double, double, double, boolean)}, which adds the wind): the screen's points and scroll come in its
     * frame. Returns false once it is gone.
     */
    boolean fly(
            double shipX,
            double shipY,
            double groundScroll,
            double aimX,
            double aimY,
            double diveX,
            double diveY,
            boolean objective) {
        if (u.ticksSinceHit < Integer.MAX_VALUE) {
            u.ticksSinceHit++;
        }
        if (u.absorbQuiet > 0) {
            u.absorbQuiet--;
        }
        if (u.hexShield.fitted()) {
            // M9 part A: a hex shield's hold and refill run on the real steps (the EMP's hold covers its stun).
            u.hexShield.step(u.spec.shield().orElseThrow(), u.events, u.x, u.y(), u.kind);
        }
        if (u.empStun > 0) {
            // M7 part A: its own clock stops (its timers included) while the EMP Burst's stun lasts.
            u.empStun--;
            return u.phase == Enemy.Phase.CHAIN
                    || u.phase == Enemy.Phase.FLOCK
                    || stunnedStep(shipX, shipY, groundScroll);
        }
        if (u.wakeTicks > 0 && --u.wakeTicks == 0 && u.spec.gun().isPresent()) {
            // M9 part B: awake, its gun waits its first-shot delay from here.
            u.volleyTicks = SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1;
            u.burstLeft = 0;
        }
        // M5 part E: the swap timer and the ring's cooldown run on the real steps (a halted arena too).
        if (u.spec.submerge().isPresent()) {
            u.submerging.step(u.spec.submerge().get());
        }
        if (u.ringWait > 0) {
            u.ringWait--;
        }
        u.swooping.scratch();
        // M8 part E: a song's clock (only with one; it stops while stunned, above).
        u.song.step();
        if (u.phase == Enemy.Phase.CHAIN || u.phase == Enemy.Phase.FLOCK) {
            // Its chain or its flock placed it this step already.
            return true;
        }
        u.prevX = u.x;
        u.prevY = u.y;
        switch (u.phase) {
            case RIDE -> {
                // M9 part B: its rock's pass places it after the rocks moved; gone when its rock carried it off.
                return !u.riding.carriedOff;
            }
            case MOUNT -> {
                // M8 part E: a wall's nest scrolls with the ground; gone below the bottom edge.
                u.y -= groundScroll;
                return u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll);
            }
            case RISE -> {
                // M8 part D: its shadow, rise, glide and sink; gone at the sink's end or off the play field.
                return u.rising.step(u.spec.rise().orElseThrow(), u, shipX, shipY);
            }
            case BURROW -> {
                // M7 part B: fixed on the screen but in its entry and escape (Q1 = a).
                boolean on = u.burrowing.step(shipX, shipY, groundScroll, u.box.height() / 2);
                u.x = u.burrowing.x();
                u.y = u.burrowing.y();
                turn();
                return on && !carriedOffTop(groundScroll);
            }
            case COIL -> {
                // M8 part E: its orbit round the ship (the trench clamp), its cycle; gone once off the field leaving.
                boolean on = u.coiling.step(u, shipX, shipY);
                turn();
                return on;
            }
            case LURE -> {
                // M8 part F: its drift with the water, its lunge at the ship and its retreat; gone off a side or the
                // bottom.
                return u.lunging.step(u, shipX, shipY, groundScroll) && !carriedOffTop(groundScroll);
            }
            case VORTEX -> {
                // M7 part E: its swirl round its centre line, scrolling with the ground, kept off the ship.
                boolean on = u.vortexCycle.step(shipX, shipY, groundScroll, u.box.height() / 2);
                u.x = u.vortexCycle.x();
                u.y = u.vortexCycle.y();
                return on && !carriedOffTop(groundScroll);
            }
            case STRAFE -> {
                // M7 part E: down to its lane, across it and (hard) back once; gone once off the play field.
                u.laneStrafing.step();
                u.x = u.laneStrafing.x();
                u.y = u.laneStrafing.y();
                turn();
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || !u.entered;
            }
            case LAY -> {
                // M9 part C: up its lane, then across its lay row; gone once off the play field.
                u.mineLaying.step();
                u.x = u.mineLaying.x();
                u.y = u.mineLaying.y();
                turn();
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || !u.entered;
            }
            case SWOOP -> {
                u.swooping.step(u.spec.swoop().orElseThrow());
                u.x = u.swooping.x();
                u.y = u.swooping.y();
                turn();
                return PlayField.overlaps(u.x, u.y, u.box);
            }
            case BOMB -> {
                // M7 part D: toward its row and its dome's slot at its speed, or straight down.
                u.bombRun.step(u.x, u.y, u.speed * SimStep.SECONDS);
                u.x = u.bombRun.x();
                u.y = u.bombRun.y();
                turn();
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                u.bombRun.flown(on);
                return on || (!u.entered && u.y + u.box.height() / 2 > 0);
            }
            case SPIRAL -> {
                u.spiralling.step();
                return true;
            }
            case CHASE -> {
                // M9 part D (design/allies/mining-tug): under the always hook it steers at the objective's nearest
                // section instead of the ship; crossing it deals the dive once ({@link AllyPasses}).
                u.divedAtObjective |= objective;
                u.chasing.chase(objective ? diveX : shipX, objective ? diveY : shipY);
                return true;
            }
            case DASH -> {
                // M9 part D: its dash cycle; gone once out through an edge after its last dash.
                return u.dashing.step(shipX, shipY);
            }
            case LATCHED -> {
                u.chasing.hold(shipX, shipY);
                return true;
            }
            case STUNNED -> {
                u.chasing.stun();
                return true;
            }
            case AMBUSH -> {
                if (!u.ambushing.step()) {
                    return false;
                }
            }
            case LANE -> {
                // M5 part G: along its lane, which scrolls with the ground; its facing is its velocity over it.
                u.scrolled += groundScroll;
                u.distance += u.speed * SimStep.SECONDS;
                placeLane();
                double dx = u.x - u.prevX;
                double dy = u.y - u.prevY + groundScroll;
                if (dx != 0 || dy != 0) {
                    u.facing = Enemy.heading(dx, dy);
                }
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || (!u.entered && u.distance < u.path.length());
            }
            case GROUND -> {
                u.y -= groundScroll;
                if (u.growTicks > 0 && --u.growTicks == 0) {
                    // M5 part H: grown: its gun fires as soon as it can (in its arc).
                    u.volleyTicks = 1;
                    u.burstLeft = 0;
                }
                if (u.driftX != 0 || u.driftY != 0) {
                    // M5 part E: a field unit or a raft drifts on the current, on top of the scroll.
                    u.x += u.driftX * SimStep.SECONDS;
                    u.y += u.driftY * SimStep.SECONDS;
                    if (u.x + u.box.width() / 2 < 0 || u.x - u.box.width() / 2 > PlayField.WIDTH) {
                        return false;
                    }
                }
                if (u.dormantTick < 0) {
                    // M9 part B: a dormant unit's rack stays still until it wakes.
                    track(aimX, aimY);
                }
                return u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll);
            }
            case WALK -> {
                u.scrolled += groundScroll;
                u.pouncing.scratch();
                if (u.pouncing.leaping()) {
                    // In a pounce it flies over the ship's position at take-off and lands beyond it.
                    u.pouncing.step(u.spec.pounce().orElseThrow());
                    u.x = u.pouncing.x();
                    u.y = u.pouncing.y();
                } else {
                    u.y -= groundScroll;
                    if (u.curlRoll.phase() == Enemy.CurlPhase.WALK) {
                        walk();
                    } else {
                        u.curlRoll.step(u, u.spec.roll().orElseThrow(), u.events, groundScroll);
                    }
                    u.pouncing.waitStep();
                }
                if (u.spec.turret().isPresent()) {
                    u.turretGun.turn(aimX, aimY);
                }
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || (!u.entered && u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll));
            }
            case ESCORT -> {
                if (!u.escorting.step()) {
                    leave(shipX, shipY);
                    turn();
                    return true;
                }
            }
            case ENTER -> {
                u.distance += u.speed * SimStep.SECONDS;
                if (u.distance >= u.path.length()) {
                    u.distance = u.path.length();
                    place();
                    if (u.holdTicks == 0) {
                        return false;
                    }
                    hold();
                } else {
                    place();
                    if (u.orbit.isEmpty() && u.holdTicks > 0 && u.hoverHold.stopsNow(u, shipY)) {
                        // M8 part D: down to its height above the ship, it stops there (before its path's end).
                        hold();
                    }
                }
                if (u.chasing.phase() == Enemy.LatchPhase.ENTER) {
                    u.chasing.await();
                }
                if (u.dashing.entering()) {
                    // M9 part D: on the play field its dash cycle starts.
                    u.dashing.await();
                    if (u.phase == Enemy.Phase.DASH) {
                        return true;
                    }
                }
                if (u.spec.brood().isPresent() && (u.broodTicks >= 0 || u.y <= PlayField.HEIGHT)) {
                    u.broodTicks++;
                }
                if (u.spec.swoop().isPresent()) {
                    swoopDue(shipX, shipY);
                }
                if (u.spec.flock().isPresent() && u.spec.flock().get().ram().isPresent()) {
                    ram(u.spec.flock().get().ram().get(), shipX, shipY);
                }
            }
            case HOLD -> {
                if (u.orbit.isPresent()
                        && u.spec.airLink().isPresent()
                        && drifts(u.spec.airLink().get())) {
                    // M9 part D (design/enemies/space/shard-drone): a linked ring turns round a centre that descends
                    // and
                    // drifts toward the ship's x; it never breaks off, gone once past the bottom edge.
                    driftRing(u.orbit.get(), u.spec.airLink().get(), shipX);
                    circle(u.orbit.get());
                    return u.orbit.get().centreY() + u.offY + u.orbit.get().radius() + u.box.height() / 2 > 0;
                } else if (u.orbit.isPresent()) {
                    circle(u.orbit.get());
                } else if (u.spec.aura().isPresent() && u.spec.sine().isPresent()) {
                    hoverDrift(u.spec.sine().get());
                } else if (u.hoverHold.active()) {
                    // M8 part D: its drift sideways, its facing toward the ship.
                    u.hoverHold.hold(u, shipX, shipY);
                } else if (u.hoverHold.driftsOnly()) {
                    // M8 part G: its drift sideways at its depth, its facing up the screen (the Depth Hunter's).
                    u.hoverHold.driftUp(u);
                }
                if (u.spec.sweep().isPresent()) {
                    sweep(shipX, shipY);
                }
                if (--u.holdTicks <= 0) {
                    if (u.hoverHold.active() && u.orbit.isEmpty()) {
                        // M8 part D: it leaves down and out to the side it holds on now (after its drift).
                        u.exit = Spawn.Exit.awayFromCentre(u.x);
                    }
                    if (u.spec.dive().isPresent()) {
                        // M5 part G: a diver dives at the point it is given (the objective under the always hook).
                        u.divedAtObjective = objective;
                        leave(diveX, diveY);
                    } else if (u.spec.hover().isPresent()
                            && u.spec.hover().get().peelSpeed() > 0) {
                        peel(u.spec.hover().get().peelSpeed());
                    } else if (u.spec.dash().isPresent()) {
                        // M9 part D: a dashing unit breaks off its circle into its chase.
                        u.dashing.begin(Enemy.heading(shipX - u.x, shipY - u.y));
                        return true;
                    } else {
                        leave(shipX, shipY);
                    }
                }
                if (u.phase == Enemy.Phase.HOLD
                        && u.orbit.isEmpty()
                        && (u.hoverHold.active() || u.hoverHold.driftsOnly())) {
                    // M8 part D: its facing is on the ship, not along its drift (M8 part G: up the screen).
                    return true;
                }
            }
            case LEAVE -> {
                u.x += u.vx * SimStep.SECONDS;
                u.y += u.vy * SimStep.SECONDS;
                if (u.chasing.launched() && u.chasing.phase() == Enemy.LatchPhase.ENTER) {
                    // M9 part C (design/enemies/air/dart-drone): out of its carrier's bay, its chase starts on time.
                    u.chasing.await();
                    if (u.phase == Enemy.Phase.CHASE) {
                        turn();
                        return true;
                    }
                }
                if (u.scattering.active() || u.riding.rider()) {
                    // M9 part B: a scattering wing member breaks off sideways, a rider's leap is a straight dive: no
                    // swoop, dive or ram of their stat blocks' on the way out.
                    turn();
                    return PlayField.overlaps(u.x, u.y, u.box);
                }
                // M8 AW: a hover's way out keeps off the level's rock (none: as before).
                u.hoverHold.leaving(u);
                u.spiralling.ricochet();
                turn();
                if (u.glideTicks > 0 && --u.glideTicks == 0) {
                    // A launched unit's glide is over: it holds, its gun firing, then leaves down.
                    u.holdTicks = u.glideHoldTicks;
                    hold();
                    return true;
                }
                if (u.spec.dive().isPresent() && !u.diveFired) {
                    EnemySpec.Dive dive = u.spec.dive().get();
                    // Its stat block's timing, also under the always hook (its fan then turns back at the objective).
                    boolean passed = u.prevY > shipY && u.y <= shipY;
                    if (passed || ++u.diveTicks >= SimStep.ticks(dive.fireAfterSeconds())) {
                        u.diveFired = true;
                        u.diveShot = true;
                    }
                }
                if (u.spec.swoop().isPresent()) {
                    swoopDue(shipX, shipY);
                }
                if (u.spec.flock().isPresent() && u.spec.flock().get().ram().isPresent() && !u.ramming.diving()) {
                    // M8 part D: a flock's kind released on its own (a spawning reef's fry) flies straight out and
                    // rams as a column's member does.
                    ram(u.spec.flock().get().ram().get(), shipX, shipY);
                }
                return PlayField.overlaps(u.x, u.y, u.box);
            }
        }
        turn();
        return true;
    }

    /**
     * M7 part A: one stunned step: an air unit drifts on (its anchor shifted along, see {@link Enemy#offX}),
     * a ground unit or a walker scrolls with the ground (a leaping one hangs where it is), a lane walker
     * stays on its lane's spot, a latched one stays on its spot on the hull. Returns whether it is still
     * in play, as its own movement would.
     */
    private boolean stunnedStep(double shipX, double shipY, double groundScroll) {
        u.prevX = u.x;
        u.prevY = u.y;
        switch (u.phase) {
            case RIDE -> {
                // M9 part B: its rock's pass places it.
                return !u.riding.carriedOff;
            }
            case LATCHED -> {
                u.chasing.hold(shipX, shipY);
                return true;
            }
            case GROUND, MOUNT -> {
                u.y -= groundScroll;
                return u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll);
            }
            case WALK -> {
                u.scrolled += groundScroll;
                if (!u.pouncing.leaping()) {
                    u.y -= groundScroll;
                }
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || (!u.entered && u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll));
            }
            case LANE -> {
                u.scrolled += groundScroll;
                placeLane();
                boolean on = PlayField.overlaps(u.x, u.y, u.box);
                u.entered |= on;
                return on || (!u.entered && u.distance < u.path.length());
            }
            case BURROW -> {
                // M7 part B: up (only then can the EMP reach it) it is fixed on the screen; its timers wait.
                return true;
            }
            case COIL -> {
                // M8 part E: its cycle and orbit wait; it stays where it is.
                return true;
            }
            case LURE -> {
                // M8 part F: its drift, lunge and retreat wait; it scrolls with the ground.
                u.y -= groundScroll;
                return u.y + u.box.height() / 2 > 0 && !carriedOffTop(groundScroll);
            }
            case VORTEX -> {
                // M7 part E: its cycle and swirl wait (it stays as it is, open or closed); it scrolls with the ground.
                boolean on = u.vortexCycle.stunned(groundScroll, u.box.height() / 2);
                u.x = u.vortexCycle.x();
                u.y = u.vortexCycle.y();
                return on && !carriedOffTop(groundScroll);
            }
            default -> {
                u.x += u.stunDx;
                u.y += u.stunDy;
                switch (u.phase) {
                    case ENTER, HOLD, ESCORT, AMBUSH -> {
                        u.offX += u.stunDx;
                        u.offY += u.stunDy;
                    }
                    case SPIRAL -> {
                        u.spiralling.drift(u.stunDx, u.stunDy);
                    }
                    case SWOOP -> {
                        // M7 part B: its curve drifts with it.
                        u.swooping.drift(u.stunDx, u.stunDy);
                    }
                    case STRAFE -> {
                        // M7 part E: it drifts on its line; its flight resumes from there.
                        u.laneStrafing.drift(u.stunDx, u.stunDy);
                        boolean on = PlayField.overlaps(u.x, u.y, u.box);
                        u.entered |= on;
                        return on || !u.entered;
                    }
                    case LAY -> {
                        // M9 part C: it drifts on; its lay path resumes from there (laying nothing meanwhile).
                        u.mineLaying.drift(u.stunDx, u.stunDy);
                        boolean on = PlayField.overlaps(u.x, u.y, u.box);
                        u.entered |= on;
                        return on || !u.entered;
                    }
                    default -> {
                        // It flies on its velocity, which carries on from where it drifted to.
                    }
                }
                return u.phase != Enemy.Phase.LEAVE || PlayField.overlaps(u.x, u.y, u.box);
            }
        }
    }

    /**
     * M7 part G (design/campaign Level 20, the turnabout): whether the ground, scrolling up the screen in its frame
     * this step ({@code groundScroll} below 0), has carried it off the top edge: a unit on the ground leaves by
     * either edge. Never while the ground scrolls down, so an unreversed level steps as before.
     */
    private boolean carriedOffTop(double groundScroll) {
        return groundScroll < 0 && u.y - u.box.height() / 2 >= PlayField.HEIGHT;
    }

    /**
     * Turns its facing toward the direction it flew this step, by at most {@link #MAX_TURN}, so a
     * sharp turn (breaking out of an orbit) plays through the in-between headings; standing still
     * keeps the facing.
     */
    private void turn() {
        double dx = u.x - u.prevX;
        double dy = u.y - u.prevY;
        if (dx == 0 && dy == 0) {
            return;
        }
        double delta = Math.IEEEremainder(Enemy.heading(dx, dy) - u.facing, 2 * StrictMath.PI);
        u.facing = Math.IEEEremainder(u.facing + Math.clamp(delta, -MAX_TURN, MAX_TURN), 2 * StrictMath.PI);
    }

    /**
     * One step of a walker over the ground (design/enemies/ground/scuttler): its facing turns toward
     * the path's next point by at most its turn rate, and it walks along its facing; a point it
     * reached, or one it can no longer turn into (behind it, inside its turning circle), is passed.
     * Past the last point it walks straight on.
     */
    private void walk() {
        u.walking.step(u.spec.walker().orElseThrow().speed());
    }

    /**
     * A turret on the screen turns its barrel towards its target (the ship, or the convoy unit the
     * target-the-objective hook chose) by at most its turn rate while the target is inside its arc;
     * outside it, the barrel holds.
     */
    private void track(double shipX, double shipY) {
        u.inArc = false;
        if (u.spec.gun().isEmpty() || !PlayField.overlaps(u.x, u.y, u.box)) {
            return;
        }
        EnemyGun gun = u.spec.gun().get();
        double wanted = Enemy.heading(shipX - u.x, shipY - u.y);
        // M7 part F: the arc is measured from its facing (straight down, or up the screen for {@code arc: up}).
        double off = gun.arcFacingRadians() == 0
                ? wanted
                : Math.IEEEremainder(wanted - gun.arcFacingRadians(), 2 * StrictMath.PI);
        if (Math.abs(off) > gun.arcRadians()) {
            return;
        }
        u.inArc = true;
        double delta = Math.IEEEremainder(wanted - u.aim, 2 * StrictMath.PI);
        double most = gun.turnRate() * SimStep.SECONDS;
        u.aim = Math.IEEEremainder(u.aim + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
        u.facing = u.aim;
    }

    /** Places it on its path at its distance (its sine weave and its stun drift added). */
    void place() {
        u.segment = u.path.segmentAt(u.distance, u.segment);
        u.x = u.path.x(u.segment, u.distance);
        u.y = u.path.y(u.segment, u.distance);
        if (u.spec.sine().isPresent()) {
            EnemySpec.Sine sine = u.spec.sine().get();
            u.x += sine.amplitude() * Trig.sin(2 * StrictMath.PI * u.distance / (u.speed * sine.periodSeconds()));
        }
        if (u.offX != 0 || u.offY != 0) {
            // M7 part A: shifted by its drift while stunned.
            u.x += u.offX;
            u.y += u.offY;
        }
    }

    /**
     * M5 part G: a lane walker's place: {@link Enemy#distance} along its lane (straight on past its end along
     * the last direction), the lane scrolled by {@link Enemy#scrolled}, and its {@code sine} weave as an offset
     * across the lane (to the right of its direction for a positive swing).
     */
    void placeLane() {
        double along = Math.min(u.distance, u.path.length());
        u.segment = u.path.segmentAt(along, u.segment);
        double dx = u.path.dx(u.segment);
        double dy = u.path.dy(u.segment);
        double norm = Math.sqrt(dx * dx + dy * dy);
        double ux = norm > 0 ? dx / norm : 0;
        double uy = norm > 0 ? dy / norm : -1;
        double past = u.distance - along;
        u.x = u.path.x(u.segment, along) + ux * past;
        u.y = u.path.y(u.segment, along) + uy * past - u.scrolled;
        if (u.spec.sine().isPresent()) {
            EnemySpec.Sine sine = u.spec.sine().get();
            double offset =
                    sine.amplitude() * Trig.sin(2 * StrictMath.PI * u.distance / (u.speed * sine.periodSeconds()));
            u.x += uy * offset;
            u.y -= ux * offset;
        }
    }

    private void hold() {
        u.phase = Enemy.Phase.HOLD;
        // No Optional.map here: it would allocate in the step loop.
        u.angle = u.orbit.isPresent() ? u.orbit.get().angle() : 0;
        // + 1: the gun already counts down in the step the unit stops, which is step 0 of the delay.
        u.volleyTicks =
                u.spec.gun().isPresent() ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1 : 0;
        if (u.spec.sweep().isPresent()) {
            u.sweepBeam.arm(u.spec.sweep().get());
        }
    }

    /** M9 part D: whether a linked ring's centre moves (a {@code circle} wave of air-linked units). */
    private static boolean drifts(EnemySpec.AirLink link) {
        return link.descend() > 0 || link.follow() > 0;
    }

    /**
     * M9 part D: a linked ring's centre descends at its link's {@code descend} and drifts toward the ship's x at its
     * {@code follow} (kept in the unit's offset, the same for every unit of the ring).
     */
    private void driftRing(Spawn.Orbit circle, EnemySpec.AirLink link, double shipX) {
        u.offY -= link.descend() * SimStep.SECONDS;
        double toShip = shipX - (circle.centreX() + u.offX);
        double most = link.follow() * SimStep.SECONDS;
        u.offX += Math.clamp(toShip, -most, most);
    }

    private void circle(Spawn.Orbit circle) {
        u.angle += circle.radiansPerSecond() * SimStep.SECONDS;
        u.x = circle.centreX() + circle.radius() * Trig.cos(u.angle);
        u.y = circle.centreY() + circle.radius() * Trig.sin(u.angle);
        if (u.offX != 0 || u.offY != 0) {
            // M7 part A: shifted by its drift while stunned.
            u.x += u.offX;
            u.y += u.offY;
        }
    }

    private void leave(double shipX, double shipY) {
        u.phase = Enemy.Phase.LEAVE;
        u.burstLeft = 0;
        if (u.spec.dive().isPresent()) {
            u.speed = u.spec.dive().get().speed();
        }
        double dx = u.exit.dx();
        double dy = u.exit.dy();
        if (u.exit.towardShip()) {
            double length = Math.sqrt((shipX - u.x) * (shipX - u.x) + (shipY - u.y) * (shipY - u.y));
            dx = length > 0 ? (shipX - u.x) / length : 0;
            dy = length > 0 ? (shipY - u.y) / length : -1;
        }
        u.vx = dx * u.speed;
        u.vy = dy * u.speed;
    }

    /**
     * M9 part B (design/campaign Level 29, the wavering Stormhawks; {@link EnemySpec.Hover#peelSpeed()}): its hover is
     * over: it peels off straight sideways toward the nearer side edge (right from the middle line) at {@code speed},
     * gone past that edge ({@link SimEvents.Type#PEEL_OFF}).
     */
    private void peel(double speed) {
        u.phase = Enemy.Phase.LEAVE;
        u.burstLeft = 0;
        u.vx = u.x < PlayField.WIDTH / 2.0 ? -speed : speed;
        u.vy = 0;
        u.events.add(SimEvents.Type.PEEL_OFF, u.x, u.y(), u.kind);
    }

    /**
     * One step of a laser sweep while it hovers (design/enemies/air/mantis): the wait for the next
     * telegraph runs; a telegraph starts only if its sweep ends before the hover does, and fixes the
     * sweep's centre on the ship's bearing from its eye, clamped between straight inward and straight
     * down.
     */
    private void sweep(double shipX, double shipY) {
        EnemySpec.Sweep sweep = u.spec.sweep().orElseThrow();
        if (!u.sweepBeam.due(sweep, u.holdTicks)) {
            return;
        }
        boolean left = u.x < PlayField.WIDTH / 2.0;
        double inward = left ? -StrictMath.PI / 2 : StrictMath.PI / 2;
        double bearing = Enemy.heading(
                shipX - u.x - u.beamOffsetX(),
                shipY - u.y + u.spec.sweep().orElseThrow().originDown());
        u.sweepBeam.start(sweep, left ? Math.clamp(bearing, inward, 0) : Math.clamp(bearing, 0, inward));
    }

    /**
     * M7 part D: a Herald's hover: its side-to-side drift ({@code sine}, the stat block's 32 px over 10 s) goes
     * on in time where its entry's weave along its path left off.
     */
    private void hoverDrift(EnemySpec.Sine sine) {
        u.hoverTicks++;
        u.x = u.path.x(u.segment, u.distance);
        u.y = u.path.y(u.segment, u.distance);
        double along = u.distance / u.speed + u.hoverTicks * SimStep.SECONDS;
        u.x += sine.amplitude() * Trig.sin(2 * StrictMath.PI * along / sine.periodSeconds());
        if (u.offX != 0 || u.offY != 0) {
            u.x += u.offX;
            u.y += u.offY;
        }
    }

    /**
     * M8 part C, a ram on a path (design/enemies/naval/glassfin, a {@code column}): its clock, draw and tell run while
     * it flies its path; its dive leaves along a straight line ({@link Enemy.Phase#LEAVE}) and is gone off the play
     * field.
     */
    private void ram(EnemySpec.Ram ram, double shipX, double shipY) {
        if (u.ramming.step(ram, u, u.x, u.y, shipX, shipY)) {
            u.vx = u.ramming.vx();
            u.vy = u.ramming.vy();
            u.phase = Enemy.Phase.LEAVE;
        }
    }

    /**
     * M7 part B, a swoop (design/enemies/air/dust-kite): once it flies above the ship's centre by its
     * trigger distance or less (on the play field, once per unit) it banks into its swoop.
     */
    private void swoopDue(double shipX, double shipY) {
        if (u.swooping.due(u.spec.swoop().orElseThrow(), u.x, u.y, u.box, shipX, shipY)) {
            u.phase = Enemy.Phase.SWOOP;
        }
    }
}
