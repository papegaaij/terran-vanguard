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
        if (u.growTicks > 0) {
            // M5 part H: a root burst's growth fires nothing while it grows.
            return false;
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
                || ambushPhase == Enemy.AmbushPhase.HOLD;
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
        if (--u.volleyTicks > 0) {
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
     * M8 part B (design/enemies/ground/drill-rig): its gun's interval now: the stat block's, longer by each lost part's
     * slowdown (exactly the stat block's without parts).
     */
    private int volleyInterval(EnemyGun gun) {
        return SimStep.ticks(gun.intervalSeconds() + u.partHp.slowSeconds(u.spec.parts()));
    }
}
