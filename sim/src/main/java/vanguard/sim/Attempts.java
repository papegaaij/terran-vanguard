package vanguard.sim;

/**
 * A {@link Sortie}'s attempts (split out of {@link Sortie}): at a step's start the restart a retry asked for (or the
 * boss checkpoint's restore) and the checkpoint's record as the boss arrives; an attempt's start.
 */
final class Attempts {
    private final Sortie s;

    Attempts(Sortie sortie) {
        this.s = sortie;
    }

    /** The step's start: a restart or the checkpoint's restore when one was asked for, then the checkpoint's record. */
    void step() {
        if (s.retryArmour > 0) {
            restart();
        } else if (s.bossRetry) {
            s.checkpoint.restore();
        }
        if (s.checkpoint != null && !s.checkpointTaken && s.levelTick + 1 == arrivalTicks()) {
            s.checkpoint.record();
            s.checkpointTaken = true;
        }
    }

    /** When the checkpoint boss arrives, steps on the level clock; -1 without one. */
    private int arrivalTicks() {
        return SimStep.ticks(s.setPieces[s.checkpointBoss].boss().orElseThrow().arriveSeconds());
    }

    /** An attempt's start: the travel at the level start, the ship with {@code armour} (in its launch), the escort. */
    void start(double armour) {
        // M7 part G: the travel at the level start.
        s.clock.travel = Sortie.travelAt(s.script.sections(), 0);
        s.ship.reset(armour);
        if (s.launchTicks > 0) {
            s.ship.launch(0, s.script.launchDrop());
            s.ship.rememberPosition();
        }
        s.escorts.reset(s.ship.x(), s.ship.y());
    }

    /**
     * Restarts the level from its start state (design/systems/retry): everything back to the level start, a new
     * attempt with the retry's armour, what the last one earned lost.
     */
    private void restart() {
        s.checkpointTaken = false;
        s.objectivePasses.reset();
        s.pickupPasses.reset();
        s.bossRetry = false;
        s.streamReleased = 0;
        s.resetArena();
        s.holdersLeft = s.holders;
        s.holdersOutSeen = 0;
        if (s.rides != null) {
            s.rides.reset();
        }
        if (s.forks != null) {
            s.forks.reset();
        }
        if (s.charges != null) {
            s.charges.reset();
        }
        if (s.story != null) {
            s.story.reset();
        }
        s.bossPasses.clearLaunches();
        s.fire.reset();
        s.special.reset();
        s.force.reset();
        Pools.clear(s.ground);
        Pools.clear(s.pickups);
        Pools.clear(s.debris);
        for (SetPiece piece : s.setPieces) {
            piece.reset();
        }
        s.groundPasses.reset();
        s.tally.reset();
        s.objectives.reset();
        s.radio.reset();
        for (Crane crane : s.cranes) {
            crane.reset();
        }
        if (s.sled != null) {
            s.sled.reset();
        }
        s.vents.reset();
        if (s.plumes != null) {
            s.plumes.reset();
        }
        for (Tow tow : s.tows) {
            tow.reset();
        }
        s.wind.reset();
        s.sondes.reset();
        if (s.cruisers != null) {
            s.cruisers.reset();
        }
        if (s.relay != null) {
            s.relay.reset();
        }
        if (s.domes != null) {
            s.domes.reset();
        }
        s.latches.reset();
        s.allies.reset();
        s.levelTick = 0;
        s.tickFraction = 0;
        s.clock.clockStep = Sortie.CLOCK_ONE;
        s.realTick = 0;
        s.holdZones.reset();
        s.sightings.reset();
        s.contacts.reset();
        if (s.links != null) {
            s.links.reset();
        }
        s.groundScroll = 0;
        s.complete = false;
        s.wrecked = false;
        s.failed = false;
        s.failedGroup = -1;
        s.escortCredits = 0;
        if (s.convoy != null) {
            s.convoy.reset();
        }
        s.attempt++;
        start(s.retryArmour);
        s.retryArmour = 0;
        s.events.add(SimEvents.Type.SORTIE_RESTARTED, s.ship.x(), s.ship.y());
    }
}
