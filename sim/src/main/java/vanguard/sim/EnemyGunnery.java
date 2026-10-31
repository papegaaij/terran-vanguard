package vanguard.sim;

/**
 * One {@link Enemy}'s trigger (split out of {@link Enemy}): its gun's volley and burst clocks per kind of unit (a
 * chain's head, a walker's fan or turret, a mine layer, a diver's one shot, a holding, ground, escort, lane or ambush
 * unit's bursts), a growth's and a silenced gun's quiet, and a lost part's slower interval.
 */
final class EnemyGunnery {
    private final Enemy u;

    EnemyGunnery(Enemy unit) {
        this.u = unit;
    }

    /**
     * Counts down its gun while it holds (a turret: while the ship is in its arc); returns whether
     * it fires a shot this step. A diver fires once, in its dive.
     */
    boolean trigger() {
        if (u.dormantTick >= 0 || u.wakeTicks > 0 || u.scattering.active()) {
            // M9 part B: a dormant unit and its wake tell fire nothing; nor does a wing member scattering.
            return false;
        }
        if (u.growTicks > 0 || u.regrowing.quiet()) {
            // M5 part H: a root burst's growth fires nothing while it grows; M10 part A: nor a regrowing turret while
            // its root is exposed or it knits.
            return false;
        }
        if (u.phase == Enemy.Phase.CHAIN && u.chain.spec().cars().isPresent()) {
            return carTrigger();
        }
        if (u.phase == Enemy.Phase.CHAIN) {
            // A chain's head fires its fan while it is on the screen.
            if (u.spec.gun().isEmpty() || !PlayField.overlaps(u.x, u.y, u.box) || --u.volleyTicks > 0) {
                return false;
            }
            u.volleyTicks = SimStep.ticks(u.spec.gun().get().intervalSeconds());
            return true;
        }
        if (u.phase == Enemy.Phase.WALK && u.spec.turret().isPresent()) {
            return u.turretGun.trigger();
        }
        if (u.phase == Enemy.Phase.WALK) {
            // A walker's fan, along its facing, while it is on the screen (M7 part D: not while it is
            // curled, its timer waiting, nor once its vent is gone).
            if (u.spec.gun().isEmpty() || u.gunQuiet() || !PlayField.overlaps(u.x, u.y, u.box) || --u.volleyTicks > 0) {
                if (u.volleyTicks == SimStep.ticks(Enemy.MORTAR_TELL_SECONDS)
                        && u.spec.gun().isPresent()
                        && u.spec.gun().get().mortar().isPresent()
                        && !u.gunQuiet()
                        && PlayField.overlaps(u.x, u.y, u.box)) {
                    // M7 part D: the mortar vent swells before its lob (design/enemies/ground/shellback).
                    u.events.add(SimEvents.Type.MORTAR_TELL, u.x, u.y(), u.kind);
                }
                return false;
            }
            u.volleyTicks = volleyInterval(u.spec.gun().get());
            return true;
        }
        if (u.spec.gun().isPresent() && u.spec.gun().get().mine().isPresent()) {
            // A mine layer drops a spore at its interval wherever it flies on the screen.
            if (u.phase == Enemy.Phase.GROUND || !PlayField.overlaps(u.x, u.y, u.box) || --u.volleyTicks > 0) {
                return false;
            }
            u.volleyTicks = SimStep.ticks(u.spec.gun().get().intervalSeconds());
            return true;
        }
        if (u.spec.dive().isPresent()) {
            boolean shot = u.diveShot;
            u.diveShot = false;
            return shot;
        }
        Enemy.AmbushPhase ambushPhase = u.ambushing.phase();
        boolean ready = u.phase == Enemy.Phase.HOLD
                || (u.phase == Enemy.Phase.GROUND && u.inArc)
                || (u.phase == Enemy.Phase.ESCORT && u.y < PlayField.HEIGHT)
                || (u.phase == Enemy.Phase.LANE && PlayField.overlaps(u.x, u.y, u.box))
                || ambushPhase == Enemy.AmbushPhase.DECLOAK
                || ambushPhase == Enemy.AmbushPhase.HOLD
                // M8 part D: a rising unit fires while it glides on the play field.
                || (u.phase == Enemy.Phase.RISE && u.rising.firing(u))
                // M9 part B: a swoop path's unit fires on the play field in its band below the top edge.
                || (u.phase == Enemy.Phase.ENTER && inSwoopBand())
                // M9 part C: a launcher (the Gilded Gunship) fires its ring all the way through the play field.
                || (u.spec.launcher().isPresent() && PlayField.overlaps(u.x, u.y, u.box))
                // M10 part B (design/enemies/air/chimera): a firing swarm's member in its fire band.
                || (u.phase == Enemy.Phase.FLOCK && inSwarmBand());
        if (!ready || u.spec.gun().isEmpty() || (u.phase == Enemy.Phase.GROUND && u.partHp.silenced(u.spec.parts()))) {
            // M8 part B: a ground unit whose gun is bound to its parts falls silent with the last of them.
            return false;
        }
        EnemyGun gun = u.spec.gun().get();
        if (u.burstLeft > 0) {
            if (ambushPhase != Enemy.AmbushPhase.NONE) {
                // M5 part D: an ambush unit's interval runs from its burst's start (its two bursts
                // 1.2 s apart in its hold, design/enemies/air/wraith); other units' from its end.
                u.volleyTicks--;
            }
            if (--u.burstTicks > 0) {
                return false;
            }
            u.burstLeft--;
            u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
            return true;
        }
        u.volleyTicks--;
        if (u.phase == Enemy.Phase.FLOCK && swarmCapped(gun)) {
            return false;
        }
        if (u.volleyTicks > 0) {
            return false;
        }
        if (ambushPhase == Enemy.AmbushPhase.DECLOAK) {
            // M5 part D: the gun is silent until the decloak flash ends.
            u.volleyTicks = 1;
            return false;
        }
        if (ambushPhase != Enemy.AmbushPhase.NONE && u.ambushing.volleysSpent()) {
            u.volleyTicks = Integer.MAX_VALUE;
            return false;
        }
        u.volleyTicks = volleyInterval(gun);
        u.burstLeft = gun.burst() - 1;
        u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
        return true;
    }

    /**
     * M9 part C (design/enemies/air/rail-serpent): a train's member with a gun (a turret car) while it is on the
     * screen: its lamp lights its gun's tell before a volley ({@link SimEvents.Type#CAR_TELL}), a burst's shots go its
     * burst gap apart; a car doomed in a split or the engine's death is dark.
     */
    private boolean carTrigger() {
        if (u.spec.gun().isEmpty() || Chain.doomed(u) || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        EnemyGun gun = u.spec.gun().get();
        if (u.burstLeft > 0) {
            if (--u.burstTicks > 0) {
                return false;
            }
            u.burstLeft--;
            u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
            return true;
        }
        if (--u.volleyTicks > 0) {
            int tell = SimStep.ticks(gun.tellSeconds());
            if (tell > 0 && u.volleyTicks == tell) {
                u.events.add(SimEvents.Type.CAR_TELL, u.x, u.y(), u.kind);
            }
            return false;
        }
        u.volleyTicks = SimStep.ticks(gun.intervalSeconds());
        u.burstLeft = gun.burst() - 1;
        u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
        return true;
    }

    /**
     * M10 part B (design/enemies/air/chimera; {@link EnemySpec.SwarmFire}): whether it is a firing swarm's member on the
     * play field within its fire band of the top edge.
     */
    private boolean inSwarmBand() {
        if (u.flock == null || u.flock.spec().fire().isEmpty() || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        return u.y >= PlayField.HEIGHT - u.flock.spec().fire().get().fireBand();
    }

    /**
     * M10 part B (design/enemies/air/chimera; {@link SwarmVolleys}): as its glint starts (its gun's first-shot delay
     * before the volley) a capped swarm's member asks its flock's fire cap; refused, it skips that volley, its clock
     * running on a whole interval ({@link SimEvents.Type#VOLLEY_CAPPED}), and returns true. False for every other
     * step and unit.
     */
    private boolean swarmCapped(EnemyGun gun) {
        if (u.flock == null || !u.flock.volleys().capped()) {
            return false;
        }
        int interval = volleyInterval(gun);
        int lead = Math.min(SimStep.ticks(gun.firstShotDelay()), interval - 1);
        if (u.volleyTicks != lead || u.flock.volleys().grant(u.flock.ticks() + lead)) {
            return false;
        }
        u.volleyTicks += interval;
        u.events.add(SimEvents.Type.VOLLEY_CAPPED, u.x, u.y(), u.kind);
        return true;
    }

    /**
     * M9 part B (design/enemies/air/talon; {@link EnemySpec.SwoopPath#fireBand()}): whether it flies a swoop path on
     * the play field within its fire band of the top edge.
     */
    private boolean inSwoopBand() {
        if (u.spec.swoopPath().isEmpty() || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        return u.y >= PlayField.HEIGHT - u.spec.swoopPath().get().fireBand();
    }

    /**
     * M8 part B (design/enemies/ground/drill-rig): its gun's interval now: the stat block's, longer by each lost part's
     * slowdown (exactly the stat block's without parts).
     */
    private int volleyInterval(EnemyGun gun) {
        return SimStep.ticks(gun.intervalSeconds() + u.partHp.slowSeconds(u.spec.parts()));
    }
}
