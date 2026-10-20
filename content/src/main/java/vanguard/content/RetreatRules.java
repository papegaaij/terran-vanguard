package vanguard.content;

import java.util.Optional;
import vanguard.sim.DroneSpec;
import vanguard.sim.WaveSpec;

/**
 * M7 part G (Level 20 Retreat from Hellas, step G4): the mapping of the retreat's own keys for the simulation. The
 * signed sections and their eases through zero ({@link MarsRules#sections}), the waves' entries by travel and the
 * BOTTOM lessons' {@code warning} ({@link WaveRules}) map as they are; this class adds the {@code pursuit} formation
 * (the Mote Swarm's {@code pursuit} defaults, the wave's {@code pursuit} block and the difficulty's hook merged, the
 * refills sized by the formation lever) and the rear-guard drone's trail station (design/player/wingmen, Rear-guard
 * drone; design/player/weapons/rear-guard-drone-bay).
 */
final class RetreatRules {
    private RetreatRules() {}

    /**
     * A pursuit wave's block at {@code difficulty}: the unit's defaults, the wave's own keys over them, the unit's hook
     * (its lunge cadence and tell) over those, and the refill by the formation lever (easy 2, hard 4 of medium's 3).
     */
    static WaveSpec.Pursuit pursuit(Content content, LevelData.Wave wave, String enemy, Difficulty difficulty) {
        EnemyData unit = content.enemy(enemy);
        EnemyData.Pursuit defaults = unit.pursuit()
                .orElseThrow(() -> new IllegalArgumentException(enemy + ": a pursuit wave's unit has a pursuit block"));
        LevelData.Pursuit block = wave.pursuit().orElseThrow();
        Optional<EnemyData.PursuitChange> change =
                UnitRules.hook(unit, difficulty).flatMap(EnemyData.Hook::pursuit);
        Span band = block.band().orElse(defaults.band());
        int refill = block.refill().orElse(defaults.refill());
        return new WaveSpec.Pursuit(
                band.min(),
                band.max(),
                block.lag().orElse(defaults.lag()),
                change.flatMap(EnemyData.PursuitChange::lungeEvery)
                        .or(block::lungeEvery)
                        .orElse(defaults.lungeEvery()),
                change.flatMap(EnemyData.PursuitChange::tell).or(block::tell).orElse(defaults.tell()),
                defaults.ball(),
                block.dive().orElse(defaults.dive()),
                block.overshoot().orElse(defaults.overshoot()),
                block.climb().orElse(defaults.climb()),
                refill == 0 ? 0 : content.difficulty().formationSize(refill, difficulty),
                block.refillEvery().orElse(defaults.refillEvery()),
                block.breakAt(),
                defaults.breakSpeed());
    }

    /** A trailing drone (the Rear-guard Drone Bay's) at one level: its trail station, contact as the light drone's. */
    static DroneSpec trail(WeaponData.Drone drone, WeaponData.Level level) {
        WeaponData.Contact contact = drone.contact();
        return DroneSpec.trail(
                drone.behind().orElseThrow(),
                drone.side().orElseThrow(),
                drone.lag().orElseThrow(),
                drone.fire().orElseThrow() == WeaponData.Fire.BACK,
                level.droneHits().orElseThrow(),
                level.rebuild().orElseThrow(),
                drone.fadeIn(),
                contact.hits(),
                contact.smallHits(),
                contact.laserHits(),
                contact.damage(),
                SimSpecs.hitbox(drone.hitbox()));
    }
}
