package vanguard.sim;

/**
 * The state of a {@link Sortie} recorded just before its boss arrives (design/systems/retry, the boss
 * checkpoint), allocated with the sortie: {@link #record()} copies it, {@link #restore()} restarts the
 * sortie there on an empty field with the defences, charges, credits, score and objective tallies of
 * that moment.
 */
final class BossCheckpoint {
    private final Sortie s;
    private long rng;
    private final Tally tally;
    private final Objectives objectives;
    private final boolean[] radioFired;
    private final int[] secretTriggersSpent;
    private final int[] groupDemolished;
    private final boolean[] groupCalled;
    private final int[] towHits;
    private final int[] towCuts;
    private final boolean[] towCrates;
    private double shield;
    private double armour;
    private double armourLost;
    private int charges;
    private int chargesUsed;
    private int chargesFound;
    private int scheduleNext;
    private int nextGround;
    private int spawned;
    /** M7 part G: the pursuits' refill motes left out by then. */
    private int skipped;

    private int nextGroundObject;
    private int nextDebris;
    private double groundScroll;
    private int levelTick;
    private int streamReleased;
    private double wingmanArmour;
    private boolean wingmanEjected;
    private boolean wingmanCritical;
    private long wingmanLuck;
    /** M8 part A: the Bulwark's armour, whether it was destroyed and its generator then. */
    private double bulwarkArmour;

    private boolean bulwarkDestroyed;
    private long bulwarkDraws;
    private boolean escortKilled;
    private int tickFraction;
    private int realTick;
    private final int[] holdState;
    private int activeHold;
    private int easingHold;
    private int easeTicks;
    private boolean holdStarted;
    private int collapseTicks;
    private final Sightings sightings = new Sightings(null, null);
    /** M5 part E: the naval convoy's state (afloat, damaged, sunk, where); null without one. */
    private final Convoy naval;

    BossCheckpoint(Sortie s) {
        this.s = s;
        LevelScript script = s.script;
        tally = new Tally(s.rules.scoring());
        objectives = new Objectives(
                script.objectives().secondary(),
                script.groups().size(),
                s.force.kinds().size(),
                s.enemyTotal(),
                script.groundUnits(),
                0);
        radioFired = new boolean[s.radio.size()];
        secretTriggersSpent = new int[s.groundPasses.secretTriggersSpent.length];
        groupDemolished = new int[s.groundPasses.groupDemolished.length];
        groupCalled = new boolean[script.groups().size()];
        towHits = new int[s.tows.length];
        towCuts = new int[s.tows.length];
        towCrates = new boolean[s.tows.length];
        holdState = new int[s.holdZones.holdState.length];
        naval = script.objectives().convoy().map(Convoy::new).orElse(null);
    }

    /** Records the sortie's state now. */
    void record() {
        rng = s.rng.state();
        tally.copyFrom(s.tally);
        objectives.copyFrom(s.objectives);
        s.radio.saveFired(radioFired);
        GroundPasses ground = s.groundPasses;
        System.arraycopy(ground.secretTriggersSpent, 0, secretTriggersSpent, 0, secretTriggersSpent.length);
        System.arraycopy(ground.groupDemolished, 0, groupDemolished, 0, groupDemolished.length);
        System.arraycopy(s.objectivePasses.groupCalled, 0, groupCalled, 0, groupCalled.length);
        Tow[] tows = s.tows;
        for (int i = 0; i < tows.length; i++) {
            towHits[i] = tows[i].hitsLeft();
            towCuts[i] = tows[i].cutTick();
            towCrates[i] = tows[i].crateDue();
        }
        Defences defences = s.ship.defences();
        shield = defences.shield();
        armour = defences.armour();
        armourLost = defences.armourLost();
        charges = s.special.charges();
        chargesUsed = s.special.used();
        chargesFound = s.special.found();
        scheduleNext = s.force.scheduleNext();
        nextGround = s.force.nextGround();
        spawned = s.force.spawned();
        skipped = s.force.skipped();
        nextGroundObject = ground.nextGroundObject;
        nextDebris = ground.nextDebris;
        groundScroll = s.groundScroll;
        levelTick = s.levelTick;
        streamReleased = s.streamReleased;
        Wingman wingman = s.wingman;
        if (wingman != null) {
            wingmanArmour = wingman.armour();
            wingmanEjected = wingman.ejected();
            wingmanCritical = wingman.wasCritical();
            wingmanLuck = wingman.luck();
        }
        Bulwark bulwark = s.bulwark;
        if (bulwark != null) {
            bulwarkArmour = bulwark.armour();
            bulwarkDestroyed = bulwark.destroyed();
            bulwarkDraws = bulwark.drawState();
        }
        escortKilled = s.objectivePasses.escortKilled;
        tickFraction = s.tickFraction;
        realTick = s.realTick;
        HoldZones holds = s.holdZones;
        System.arraycopy(holds.holdState, 0, holdState, 0, holdState.length);
        activeHold = holds.activeHold;
        easingHold = holds.easingHold;
        easeTicks = holds.easeTicks;
        holdStarted = holds.holdStarted;
        sightings.copyFrom(s.sightings);
        collapseTicks = holds.collapseTicks;
        if (naval != null) {
            naval.copyFrom(s.convoy);
        }
    }

    /** Restarts the sortie at the recorded state: a new attempt on an empty field; the boss arrives again. */
    void restore() {
        s.bossRetry = false;
        s.fire.reset();
        s.special.restore(charges, chargesUsed, chargesFound);
        // M7 part G: the kill ratio follows the force's skipped refills at the next step.
        s.force.restore(scheduleNext, nextGround, spawned, skipped);
        Pools.clear(s.ground);
        Pools.clear(s.pickups);
        Pools.clear(s.debris);
        for (SetPiece piece : s.setPieces) {
            piece.reset();
        }
        s.rng.state(rng);
        GroundPasses ground = s.groundPasses;
        System.arraycopy(secretTriggersSpent, 0, ground.secretTriggersSpent, 0, secretTriggersSpent.length);
        System.arraycopy(groupDemolished, 0, ground.groupDemolished, 0, groupDemolished.length);
        s.tally.copyFrom(tally);
        s.objectives.copyFrom(objectives);
        s.radio.restoreFired(radioFired);
        for (Crane crane : s.cranes) {
            crane.reset();
        }
        System.arraycopy(groupCalled, 0, s.objectivePasses.groupCalled, 0, groupCalled.length);
        s.levelTick = levelTick;
        Tow[] tows = s.tows;
        for (int i = 0; i < tows.length; i++) {
            tows[i].restore(towHits[i], towCuts[i], towCrates[i], s.levelTick);
        }
        s.groundScroll = groundScroll;
        ground.nextGroundObject = nextGroundObject;
        ground.nextDebris = nextDebris;
        s.streamReleased = streamReleased;
        s.resetArena();
        s.vents.restore(s.levelTick, s.groundScroll);
        s.bossPasses.clearLaunches();
        s.complete = false;
        s.wrecked = false;
        s.failed = false;
        s.failedGroup = -1;
        s.attempt++;
        Ship ship = s.ship;
        ship.reset(armour);
        ship.defences().restore(shield, armour, armourLost);
        if (s.wingman != null) {
            s.wingman.restore(wingmanArmour, wingmanEjected, wingmanCritical, wingmanLuck, ship.x(), ship.y());
        }
        if (s.bulwark != null) {
            s.bulwark.restore(bulwarkArmour, bulwarkDestroyed, bulwarkDraws, ship.x(), ship.y());
        }
        s.objectivePasses.escortKilled = escortKilled;
        s.tickFraction = tickFraction;
        s.realTick = realTick;
        HoldZones holds = s.holdZones;
        System.arraycopy(holdState, 0, holds.holdState, 0, holdState.length);
        holds.activeHold = activeHold;
        holds.easingHold = easingHold;
        holds.easeTicks = easeTicks;
        holds.holdStarted = holdStarted;
        s.sightings.copyFrom(sightings);
        holds.collapseTicks = collapseTicks;
        // M7 part C: no push or cooldown carries over (a boss checkpoint in a level with terrain).
        s.contacts.resetContacts();
        if (naval != null) {
            // M5 part E (the stated default): Retry from boss restores the ships as they were.
            s.convoy.copyFrom(naval);
        }
        s.events.add(SimEvents.Type.SORTIE_RESTARTED, ship.x(), ship.y());
        s.events.add(SimEvents.Type.BOSS_RETRY, ship.x(), ship.y());
    }
}
