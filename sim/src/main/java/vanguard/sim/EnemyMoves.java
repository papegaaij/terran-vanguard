package vanguard.sim;

/**
 * The units' step of an {@link EnemyForce} (out of the force's {@link EnemyForce#move}): per unit, last to first, its
 * aim (the ship or the objective the hook picks), its flight, the events of its flight (a dropped bomb, a rise, a song,
 * a decloak, a swoop, a burrow's escape, a roll's crush), then its end (escaped, burst) or its attack: a brood's
 * hatch, a spawner's cycle, a walker's, a sweep's, a ring's, a burrow's, a vortex's, a coil's, a lure's, a lane
 * strafer's, or its gun's (a missile, a mine, a lob, a shot up or at the ship or the objective).
 */
final class EnemyMoves {
    private final EnemyForce force;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final EnemyForce.Escapes escapes;
    private final EnemyFire guns;
    private final ObjectiveHook objectiveHook;
    private final Burrowers burrowers;
    private final EnemyCarriers carriers;
    private final WalkerFire walkers;
    /** M8 part E: the slowing rings in flight; null in a level without a ring unit. */
    private final SonarRings rings;
    /** M7 part D: whether a kind of the level has a curl-roll (its ball crushes ground targets). */
    private final boolean rollers;
    /** M9 part F: the prey latches; null in a level without a prey unit. */
    private final PreyLatches prey;
    /** M5 part F: the level's wind this step, px/s to the right; exactly 0 without weather. */
    private double wind;

    EnemyMoves(
            EnemyForce force,
            Pool<Enemy> enemies,
            SimEvents events,
            EnemyForce.Escapes escapes,
            EnemyFire guns,
            ObjectiveHook objectiveHook,
            Burrowers burrowers,
            EnemyCarriers carriers,
            WalkerFire walkers,
            SonarRings rings,
            boolean rollers,
            PreyLatches prey) {
        this.force = force;
        this.enemies = enemies;
        this.events = events;
        this.escapes = escapes;
        this.guns = guns;
        this.objectiveHook = objectiveHook;
        this.burrowers = burrowers;
        this.carriers = carriers;
        this.walkers = walkers;
        this.rings = rings;
        this.rollers = rollers;
        this.prey = prey;
    }

    /** M5 part F: the level's wind for the next step, px/s to the right. */
    void wind(double pxPerSecond) {
        wind = pxPerSecond;
    }

    /** Every unit's step, last to first (a unit gone is freed at once). */
    void step(Ship ship, boolean firing, double groundScroll) {
        Convoy convoy = objectiveHook.convoy();
        Canyon canyon = force.canyon();
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            // M7 part A: a unit the EMP Burst stuns at the step's start does nothing of its own this step.
            boolean stunned = enemy.empStunned();
            if (!stunned && enemy.bombLoaded()) {
                // M7 part D: its bomb run steers to the next dome coming down.
                int dome = convoy.nextDome(enemy.x(), enemy.y());
                enemy.bombTarget(dome, dome < 0 ? 0 : convoy.get(dome).x());
            }
            int target = objectiveHook.target(enemy, ship);
            double aimX = target < 0 ? ship.x() : convoy.get(target).x();
            double aimY = target < 0 ? ship.y() : convoy.get(target).y();
            // M5 part G: a diver under the always hook dives at the objective while it can be aimed at.
            int dive = objectiveHook.mode(enemy) == HookMode.ALWAYS ? convoy.objective(enemy.x(), enemy.y()) : -1;
            if (enemy.hoverHold.active()) {
                // M8 part E: a hover's drift keeps off the level's rock (none: as before).
                enemy.hoverHold.canyon(canyon);
            }
            double chaseX = ship.x();
            double chaseY = ship.y();
            if (prey != null && enemy.spec().preyRole() == EnemySpec.PreyRole.HUNTER && enemy.latchable()) {
                // M9 part F (the uneasy alliance): a hunter drifts and chases the nearest free prey (else the ship).
                Enemy hunted = prey.target(enemy);
                if (hunted != null) {
                    chaseX = hunted.x();
                    chaseY = hunted.y();
                }
            }
            boolean flying = dive < 0
                    ? enemy.move(chaseX, chaseY, groundScroll, aimX, aimY, wind)
                    : enemy.move(
                            ship.x(),
                            ship.y(),
                            groundScroll,
                            aimX,
                            aimY,
                            wind,
                            convoy.get(dive).x(),
                            convoy.get(dive).y(),
                            true);
            if (flying
                    && !stunned
                    && enemy.bombRun() >= 0
                    && enemy.bombArmed()
                    && convoy.over(enemy.bombRun(), enemy.x(), enemy.y())) {
                // M7 part D: over its dome's hit box: it drops its load, one hit, and flies on down (round 41: once
                // its run-in is over).
                int dome = enemy.bombRun();
                enemy.dropBomb();
                events.add(SimEvents.Type.BOMB_DROPPED, enemy.x(), enemy.y(), dome);
                objectiveHook.hitAlly(dome, convoy.spec(dome).bombs());
            }
            if (flying && enemy.risenNow()) {
                // M8 part D: its layer flipped: its escorts peel off its shadow.
                carriers.peel(enemy, ship);
            }
            if (flying && !stunned && rings != null) {
                // M8 part E: its sac's tell, its ring leaving the sac.
                rings.sing(enemy, events);
            }
            if (flying && !stunned && enemy.decloakedNow()) {
                events.add(SimEvents.Type.DECLOAK, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && !stunned && enemy.swoopedNow()) {
                events.add(SimEvents.Type.SWOOP, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && canyon != null && enemy.swooping()) {
                // M7 part C: a swoop is clamped to the flyable side (no damage).
                enemy.clampX(
                        canyon.flyableX(enemy.y(), enemy.x(), enemy.hitbox().width() / 2));
            }
            if (!flying && enemy.burrowing()) {
                events.add(SimEvents.Type.BURROW_ESCAPED, enemy.x(), enemy.y(), enemy.kind());
            }
            if (flying && !stunned && rollers && enemy.rolling() && convoy != null && convoy.groundAllies()) {
                objectiveHook.crush(enemy);
            }
            if (flying && !stunned && enemy.spec().launcher().isPresent()) {
                // M9 part C: a launcher's streams (the Gilded Gunship's Darts), beside its gun.
                carriers.launch(enemy, firing);
            }
            if (flying && !stunned && enemy.mineLayer()) {
                // M9 part C: a mine layer lays its line as it strafes (design/enemies/space/minelayer).
                guns.layLine(enemy, firing);
            }
            if (!flying) {
                if (enemy.spec().brood().isPresent()) {
                    carriers.carrierEnded(enemy);
                }
                if (enemy.spec().aura().isPresent()) {
                    carriers.heraldGone(enemy, false);
                }
                if (enemy.spec().rise().isPresent()) {
                    carriers.riseEnded(enemy, ship);
                }
                escapes.escaped(enemy);
                enemies.free(i);
            } else if (stunned) {
                // Stunned: it neither fires nor bursts, spawns, pounces or sweeps; its timers wait.
                continue;
            } else if (enemy.preyed()) {
                // M9 part F (the uneasy alliance): a preyed unit no longer fires.
                continue;
            } else if (enemy.spec().brood().isPresent() && enemy.burstDue()) {
                carriers.hatch(enemy, ship);
                escapes.burst(enemy);
                enemies.free(i);
            } else if (enemy.spec().spawner().isPresent()) {
                carriers.spawnCycle(enemy, ship, firing);
            } else if (enemy.walking()) {
                walkers.fire(enemy, ship, firing);
            } else if (enemy.spec().sweep().isPresent()) {
                if (enemy.telegraphStarted()) {
                    events.add(SimEvents.Type.SWEEP_TELEGRAPH, enemy.x(), enemy.y(), enemy.kind());
                } else if (enemy.sweepStarted()) {
                    events.add(SimEvents.Type.SWEEP_FIRED, enemy.x(), enemy.y(), enemy.kind());
                }
            } else if (enemy.spec().ring().isPresent()) {
                guns.pulse(enemy, ship, firing);
            } else if (enemy.burrowing()) {
                burrowers.burrow(enemy, ship, firing);
            } else if (enemy.vortex()) {
                guns.vortex(enemy, ship, firing);
            } else if (enemy.coils()) {
                guns.coil(enemy, ship, firing);
            } else if (enemy.lunges()) {
                guns.lure(enemy, ship, firing);
            } else if (enemy.laneStrafer()) {
                guns.passTrigger(enemy, ship, firing);
            } else if (enemy.mineLayer()) {
                // M9 part C: its gun lays its line (above), nothing else.
                continue;
            } else if (enemy.holdsFireAt(ship.x(), ship.y())) {
                // M9 part B, step B1b (design/enemies/ground/sam-nest): it holds its fire, its clock waiting, while the
                // ship is close (no missile launched on the ship).
                continue;
            } else if (enemy.trigger() && firing) {
                EnemyGun gun = enemy.spec().gun().orElseThrow();
                if (gun.homing().isPresent() && gun.homing().get().aimed()) {
                    // M9 part D (design/enemies/air/hornet): at its target, the ship or (the always hook) the hull
                    // escort's nearest section, homing on it.
                    if (guns.missiles().launchAt(enemy.x(), enemy.y(), gun, aimX, aimY, enemy.serial(), target)) {
                        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) gun.damage());
                    }
                } else if (gun.homing().isPresent()) {
                    // M8 part G (design/enemies/naval/depth-hunter): a torpedo along its facing (up), not at the ship.
                    // M9 part B: the SAM Nest's along its rack's bearing, under its own cap.
                    if (guns.missiles().launchAlong(enemy.x(), enemy.y(), gun, enemy.facing(), enemy.serial())) {
                        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) gun.damage());
                    }
                } else if (gun.mine().isPresent()) {
                    guns.dropMine(enemy, gun);
                } else if (gun.mortar().isPresent()) {
                    guns.lob(enemy, gun, ship);
                } else if (gun.up()) {
                    guns.fireUp(enemy, gun);
                } else if (objectiveHook.mode(enemy.kind()) == HookMode.ALTERNATE) {
                    guns.fireAt(enemy, ship, objectiveHook.alternate(enemy, ship));
                } else {
                    guns.fireAt(enemy, ship, objectiveHook.target(enemy, ship));
                }
            }
        }
    }
}
