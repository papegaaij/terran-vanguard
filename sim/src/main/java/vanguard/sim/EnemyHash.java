package vanguard.sim;

/** One {@link Enemy}'s state hash (split out of {@link Enemy}); the order is the replays' and must not change. */
final class EnemyHash {
    private EnemyHash() {}

    /**
     * Adds {@code u}'s state to {@code hash} (its {@link Hashed#addTo}): the state every unit has, then each
     * behaviour's only for a unit that has it, so the earlier levels hash as before.
     */
    static void add(Enemy u, StateHash hash) {
        hash.add(u.kind)
                .add(u.serial)
                .add(u.group)
                .add(u.aim)
                .add(u.diveTicks)
                .add(u.diveFired ? 1 : 0)
                .add(u.spiralling.ticks())
                .add(u.spiralling.ricochetsLeft())
                .add(u.distance)
                .add(u.segment)
                .add(u.phase.ordinal())
                .add(u.holdTicks)
                .add(u.angle)
                .add(u.vx)
                .add(u.vy)
                .add(u.x)
                .add(u.y)
                .add(u.hp)
                .add(u.volleyTicks)
                .add(u.burstLeft)
                .add(u.burstTicks);
        if (u.frame.flipped()) {
            // M7 part G: its state is its frame's; only a flipped unit adds the mark, so the other levels hash as
            // before.
            hash.add(1);
        }
        // Level 04's units add their state; the units of Levels 01-03 hash as before.
        if (u.spec.brood().isPresent()) {
            hash.add(u.broodTicks);
        }
        if (u.glideHoldTicks > 0) {
            hash.add(u.glideTicks);
        }
        u.escorting.addTo(hash);
        if (u.chain != null) {
            hash.add(u.chain.serial()).add(u.link);
        }
        if (u.spec.groundSweep().isPresent()) {
            // M9 part E: a ground sweep's state instead of the hover's (the Sentinel Tower's).
            u.groundSweep.addTo(hash);
        } else if (u.spec.sweep().isPresent()) {
            u.sweepBeam.addTo(hash);
        }
        u.walking.addTo(hash);
        // M5 part C's spawners and pounces add their state; earlier units hash as before.
        if (u.spec.spawner().isPresent()) {
            u.spawnerIris.addTo(hash);
        }
        if (u.spec.pounce().isPresent()) {
            u.pouncing.addTo(hash);
        }
        // M5 part D's ambush units and flock members add theirs; earlier units hash as before.
        u.ambushing.addTo(hash);
        if (u.flock != null) {
            hash.add(u.flock.serial()).add(u.member);
        }
        if (u.allyContacts != 0) {
            // M5 part D: only while it overlaps an air escort's unit, so the other levels hash as before.
            hash.add(u.allyContacts);
        }
        // M5 part E's drifting, submerging and pulsing units add theirs; earlier units hash as before.
        if (u.driftX != 0 || u.driftY != 0) {
            hash.add(u.driftX).add(u.driftY);
        }
        if (u.spec.submerge().isPresent()) {
            u.submerging.addTo(hash);
        }
        if (u.spec.ring().isPresent()) {
            hash.add(u.ringWait);
        }
        // M5 part G's lane walkers and hooked attacks add theirs; earlier units hash as before.
        if (u.lane) {
            hash.add(u.scrolled).add(u.facing).add(u.entered ? 1 : 0);
        }
        if (u.attacks > 0) {
            hash.add(u.attacks).add(u.attackTarget);
        }
        if (u.divedAtObjective) {
            hash.add(u.diveHit ? 1 : 0);
        }
        // M7 part D's bomb runs add theirs; earlier units hash as before.
        u.bombRun.addTo(hash, u.entered);
        // M5 part H's root-burst growths and boss-launched ambushes add theirs; earlier units hash as before.
        if (u.rootBurst >= 0) {
            hash.add(u.rootBurst).add(u.growTicks).add(u.growTotal);
        }
        if (u.launched) {
            hash.add(1);
        }
        // M9 part D's dashing units add theirs; earlier units hash as before.
        if (u.spec.dash().isPresent()) {
            u.dashing.addTo(hash);
        }
        // M5 part F's chasers add theirs; earlier units hash as before.
        if (u.spec.chase().isPresent()) {
            u.chasing.addTo(hash);
            hash.add(u.entered ? 1 : 0);
        }
        if (u.preyDrift > 0) {
            // M9 part F: only a preyed unit, so the other levels hash as before.
            hash.add(u.preyDrift);
        }
        if (u.empStun != 0 || u.offX != 0 || u.offY != 0) {
            // M7 part A: only while the EMP Burst stuns it or after it drifted, so other runs hash as before.
            hash.add(u.empStun).add(u.stunDx).add(u.stunDy).add(u.offX).add(u.offY);
        }
        // M7 part B's burrowers and swoopers add theirs; earlier units hash as before.
        if (u.spec.burrow().isPresent()) {
            u.burrowing.addTo(hash);
            hash.add(u.burrowGroup).add(u.facing);
        }
        if (u.spec.swoop().isPresent()) {
            u.swooping.addTo(hash);
        }
        // M7 part D's rings, unit parts, curl-rolls and Herald hovers add theirs; earlier units hash as before.
        u.auraRing.addTo(hash);
        u.partHp.addTo(hash);
        if (u.spec.roll().isPresent()) {
            u.curlRoll.addTo(hash);
        }
        if (u.spec.aura().isPresent()) {
            hash.add(u.hoverTicks);
        }
        // M7 part E's vortices add theirs; earlier units hash as before.
        if (u.phase == Enemy.Phase.VORTEX) {
            u.vortexCycle.addTo(hash);
        }
        // M7 part E's lane strafers add theirs; earlier units hash as before.
        if (u.phase == Enemy.Phase.LAY) {
            u.mineLaying.addTo(hash);
        }
        if (u.launching.active()) {
            u.launching.addTo(hash);
        }
        if (u.phase == Enemy.Phase.STRAFE) {
            u.laneStrafing.addTo(hash);
        }
        // M8 part C's rams add theirs; earlier units hash as before.
        if (u.spec.flock().isPresent() && u.spec.flock().get().ram().isPresent()) {
            u.ramming.addTo(hash);
        }
        // M8 part D's rises and hovers above the ship add theirs; earlier units hash as before.
        if (u.spec.rise().isPresent()) {
            u.rising.addTo(hash);
        }
        if (u.hoverHold.active() || u.hoverHold.driftsOnly()) {
            u.hoverHold.addTo(hash);
        }
        // M8 part E's songs add theirs; earlier units hash as before.
        if (u.song.active()) {
            u.song.addTo(hash);
        }
        // M8 part E's coils add theirs; earlier units hash as before.
        if (u.phase == Enemy.Phase.COIL) {
            u.coiling.addTo(hash);
        }
        // M8 part F's lunges add theirs; earlier units hash as before.
        if (u.phase == Enemy.Phase.LURE) {
            u.lunging.addTo(hash);
        }
        // M7 part F's turrets add theirs (the aim is hashed above); earlier units hash as before.
        if (u.spec.turret().isPresent()) {
            u.turretGun.addTo(hash);
        }
        // M9 part A's shields and laser lines add theirs; earlier units hash as before.
        if (u.spec.shield().isPresent()) {
            u.hexShield.addTo(hash);
        }
        if (u.spec.laserLine().isPresent()) {
            u.lineBeam.addTo(hash);
        }
        // M9 part B's scatters, riders, dormant units, holders and leaders add theirs; earlier units hash as before.
        u.scattering.addTo(hash);
        u.riding.addTo(hash);
        if (u.dormantTick >= 0 || u.wakeTicks > 0) {
            hash.add(u.dormantTick).add(u.wakeTicks);
        }
        if (u.holder || u.leader) {
            hash.add(u.holder ? 1 : 0).add(u.leader ? 1 : 0);
        }
        // M9 part E's topples add theirs; earlier units hash as before.
        if (u.spec.topple().isPresent()) {
            u.toppling.addTo(hash);
        }
    }
}
