package vanguard.sim;

/**
 * A {@link Sortie}'s state hash (split out of {@link Sortie}): the state every level has, then each part's only in a
 * level that uses it, so the earlier levels hash as before. The order is the replays' and must not change.
 */
final class SortieHash {
    private SortieHash() {}

    /** {@link Sortie#stateHash()}: a hash over {@code s}'s complete state; equal hashes mean equal replays. */
    static long of(Sortie s) {
        StateHash hash = new StateHash()
                .add(s.tick)
                .add(s.levelTick)
                .add(s.attempt)
                .add(s.wrecked ? 1 : 0)
                .add(s.groundScroll)
                .add(s.groundPasses.nextGroundObject)
                .add(s.groundPasses.nextDebris)
                .add(s.objectives.secretsFound())
                .add(s.objectives.secondaryMet() ? 1 : 0)
                .add(s.complete ? 1 : 0)
                .add(s.rng.state());
        s.ship.addTo(hash);
        s.fire.addTo(hash);
        // M8 part A: only in a level with water, so the other levels hash as before.
        s.medium.addTo(hash);
        s.special.addTo(hash);
        s.tally.addTo(hash);
        s.objectives.addKillsTo(hash);
        for (Crane crane : s.cranes) {
            crane.addTo(hash);
        }
        for (Tow tow : s.tows) {
            tow.addTo(hash);
        }
        for (SetPiece piece : s.setPieces) {
            piece.addTo(hash);
        }
        for (int spent : s.groundPasses.secretTriggersSpent) {
            hash.add(spent);
        }
        for (int demolished : s.groundPasses.groupDemolished) {
            hash.add(demolished);
        }
        Pools.addAll(hash, s.fire.shots());
        Pools.addAll(hash, s.force.enemies());
        Pools.addAll(hash, s.force.bullets());
        Pools.addAll(hash, s.ground);
        Pools.addAll(hash, s.pickups);
        Pools.addAll(hash, s.force.mines());
        if (s.walks || s.force.missiles().size() > 0) {
            // M7 part F: only in a level with a walking boss (or homing missiles), so the other levels hash as before.
            s.force.missiles().addTo(hash);
        }
        Pools.addAll(hash, s.debris);
        if (s.force.lobs().size() > 0) {
            Pools.addAll(hash, s.force.lobs());
        }
        if (s.sled != null) {
            s.sled.addTo(hash);
        }
        // M7 part F: nothing without furnace vents, so the other levels hash as before.
        s.vents.addTo(hash);
        if (s.plumes != null) {
            // M8 part G: only in a level with heat plumes.
            s.plumes.addTo(hash);
        }
        if (s.cruisers != null) {
            // M8 part H: only in a level with the cruisers' run.
            s.cruisers.addTo(hash);
        }
        if (s.relay != null) {
            // M9 part H, step H1b: only in a level with the mass relay's flash.
            s.relay.addTo(hash);
        }
        if (s.force.chains().size() > 0) {
            Pools.addAll(hash, s.force.chains());
        }
        // M5 part D: the swarms (none before Level 10).
        if (s.force.flocks().size() > 0) {
            Pools.addAll(hash, s.force.flocks());
        }
        // M8 part D: only while a rising carrier holds escorts, so the other levels hash as before.
        s.force.addRiseEscortsTo(hash);
        if (s.force.skipped() > 0) {
            // M7 part G: only once a pursuit's refill was left out, so the other levels hash as before.
            hash.add(s.force.skipped());
        }
        // M5 part B: the staggered walker waves' volley clocks (none before Level 08).
        s.force.addVolleyClocksTo(hash);
        s.pickupPasses.addCoresTo(hash);
        if (!s.script.objectives().targets().isEmpty()) {
            hash.add(s.failed ? 1 : 0).add(s.failedGroup).add(s.groundPasses.rocksThrown);
        }
        if (s.convoy != null) {
            hash.add(s.failed ? 1 : 0).add(s.escortCredits);
            s.convoy.addTo(hash);
        }
        if (s.checkpoint != null || s.arenaEndTicks >= 0) {
            hash.add(s.checkpointTaken ? 1 : 0)
                    .add(s.streamReleased)
                    .add(s.arenaJump ? 1 : 0)
                    .add(s.clock.rampFrom)
                    .add(s.clock.rampTicks);
        }
        if (s.clock.crawling) {
            // Step SIM-K: only in a crawling arena's level, so the others hash as before.
            hash.add(s.clock.crawlLevel);
        }
        if (s.clock.holdAfterBoss > 0) {
            // M7 part F: only in a level whose arena holds after its boss, so the others hash as before.
            hash.add(s.clock.postBossHold);
        }
        if (s.holdZones.present()) {
            // M5 part C: only in a level with hold zones or a collapse, so the others hash as before.
            hash.add(s.tickFraction).add(s.clock.clockStep).add(s.realTick);
            s.holdZones.addTo(hash);
        }
        s.sightings.addTo(hash);
        s.contacts.addTo(hash);
        if (s.links != null) {
            // M7 part C: only in a level with tendril gates.
            s.links.addTo(hash);
        }
        // M5 part F: only in a level that uses them, so the other levels hash as before.
        if (s.wind.present()) {
            s.wind.addTo(hash);
        }
        s.latches.addTo(hash);
        if (s.sondes.count() > 0) {
            s.sondes.addTo(hash);
        }
        s.pickupPasses.addPlacedTo(hash);
        // M5 part H: only in a level with root bursts or ambush launches, so the other levels hash as before.
        if (s.burstSlots.length > 0) {
            s.force.addBurstsTo(hash);
        }
        s.bossPasses.addTo(hash);
        // M5 part G: only with ally-below cues (a defend primary), so the other levels hash as before.
        s.allies.addTo(hash);
        if (s.wingman != null) {
            // Only with a wingman, so the hashes of sorties without one stay as they were.
            s.wingman.addTo(hash);
            s.fire.addRecentHitTo(hash);
            hash.add(s.escorts.wingmanStart).add(s.objectivePasses.escortKilled ? 1 : 0);
        }
        if (s.bulwark != null) {
            // M8 part A: only with a Bulwark, so the hashes of sorties without one stay as they were.
            s.bulwark.addTo(hash);
            hash.add(s.escorts.bulwarkStart);
        }
        if (s.objectivePasses.targetKilled) {
            // M8 part B: only once a target-destroyed cue's unit fell, so the other levels hash as before.
            hash.add(1);
        }
        // M9 part B: only in a level with a held arena, rocks or an escort swap, so the other levels hash as before.
        if (s.clock.heldArena) {
            hash.add(s.holdersLeft)
                    .add(s.holdersOutSeen)
                    .add(s.clock.haltTicks)
                    .add(s.clock.heldJumped ? 1 : 0)
                    .add(s.clock.lastSpeed)
                    .add(s.force.haltReleased());
        }
        if (s.rides != null) {
            s.rides.addTo(hash);
        }
        if (s.forks != null) {
            // M9 part E: only in a level with forks, so the other levels hash as before.
            s.forks.addTo(hash);
        }
        if (s.story != null) {
            // M9 part G: only in a level with a story pickup, so the other levels hash as before.
            s.story.addTo(hash);
            s.groundPasses.addStoryTo(hash);
        }
        s.escorts.swap.addTo(hash, s.bulwark);
        // M8 part E: only in a level with a ring unit, so the other levels hash as before.
        s.force.addRingsTo(hash, s.ship, s.wingman);
        return hash.value();
    }
}
