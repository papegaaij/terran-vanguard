package vanguard.sim;

import java.util.List;

/**
 * A level in play, advanced in fixed 60 Hz steps by {@link #step(int)}: the launch, the scroll
 * through the sections, the ship and its {@link PlayerFire}, the {@link EnemyForce} of the
 * {@link LevelScript}, its {@link SetPiece}s, the ground objects, the {@link Debris} chunks, the
 * pickups, the {@link Radio} cues, the {@link Objectives} and the tally. All entities live in pools allocated up front and the waves
 * are planned when the sortie is created, so stepping does not allocate, and the same seed and
 * commands give the same {@link #stateHash()} on every platform.
 *
 * <p>When armour reaches zero the ship explodes and the level runs on without it until the
 * presentation, after the mission failed screen, restarts it with {@link #retry(double)} from the
 * level-start state (design/systems/retry): what the attempt earned is lost. The level is over
 * when the scroll reaches the end of the last section; the ship then flies on, out of harm's way,
 * until the presentation moves to the debrief.
 *
 * <p>A boss (a {@link SetPiece} with a {@link BossSpec}) arrives on the level clock; in its arena
 * section the clock halts at the section's end while it lives, and jumps to that end when it dies
 * earlier (the ground scroll jumps with it), after which the scroll ramps up to the next section's
 * speed over {@link #ARENA_RAMP_SECONDS}. Just before it arrives a boss checkpoint is recorded
 * (design/systems/retry): {@link #retryFromBoss()} restarts there on an empty field with the
 * defences, charges, credits, score and objective tallies of that moment.
 *
 * <p>A level with an {@code escort} primary objective has a {@link Convoy} on its road; it fails
 * when the last unit is lost (design/systems/retry, on a failed primary objective): the ship flies
 * on but nothing can hurt it, and the presentation retries the level as after a wreck.
 *
 * <p>M5 part E: a level with a naval convoy ({@link ObjectiveSpec.Naval}, Level 11) has a {@link Convoy}
 * outside the objectives: it never fails the mission, only the boss's slams hurt it ({@link
 * #slamAllies(int)}), its glides run on the real steps, and the boss checkpoint keeps its state; an
 * {@code afloat} secondary is met at the boss's death with no unit sunk and failed at the first sinking.
 *
 * <p>M5 part E, step E2c: an arena boss ({@link BossSpec#anchored()}, the Harbour Kraken) has an
 * arena of speed 0: over the arena's first second ({@link #ARENA_RAMP_SECONDS}) the scroll eases
 * from the section before's speed to a halt, and the level clock halts at the arena's end while the
 * boss lives. The boss lies on the ground and scrolls in with it ({@link SetPiece#approaching()}), so
 * that its centre is its height above the bottom edge at the halt; it arrives at the arena's start (the
 * bar, the sting, the boss checkpoint) and engages at the halt. Its slams strike the ship, Rook and the
 * naval convoy's ships in their lane ({@link #slamAllies(int)}); its release lets in units planned
 * when the sortie is made (the arena's jelly field).
 *
 * <p>M5 part A: a {@link Wingman} (Rook) flies beside the ship when the loadout has one. His shots
 * share the ship's shot pool with the mount index {@link #wingmanMount()}, and what they destroy
 * pays like the player's kills. He ejects at zero armour without failing anything; a retry starts
 * him with his level-start armour, the boss checkpoint keeps his armour and whether he ejected.
 * His state is hashed only when he flies, so a sortie without him keeps its hash.
 *
 * <p>M5 part C (design/campaign Level 09): the level clock is <em>script time</em>, slowed over a hold zone
 * ({@link LevelClock}), while units, bullets and the wingman run on the real steps ({@link #realSeconds()}); the
 * {@link ObjectiveSpec.Collapse collapse} runs on the real steps once its groups are cleared.
 *
 * <p>M5 part G (design/campaign Level 13, user decisions G1–G6 = a): a {@code defend} primary ({@link
 * ObjectiveSpec.Defend}) has a {@link Convoy} of one ally standing on the ground, placed so it stands at
 * its place at the defend's start ({@link #scrollAt}); the hook's modes aim at it, only what they aim
 * at it hurts it, its loss fails the mission (as an escort's), its {@code ally-below} cues and an
 * {@code integrity} secondary follow its share, and it is defended when its synchronisation is over.
 *
 * <p>M7 part D (design/campaign Level 17; design/allies, Ares Landing dome): a level's ground allies ({@link
 * ObjectiveSpec.GroundAllies}) are a {@link Convoy} outside the objectives standing on the ground, each placed
 * so its footprint enters the top edge at its time ({@link #scrollAt}); the hook's modes aim at them per unit
 * (a hooked mine layer's bomb run, a lob's direct hit in ground space), each hooked hit counts as hits, they
 * never fail the mission, and an {@code untouched} secondary fails at the first hit and is met when the last
 * of them leaves the bottom edge unhit.
 *
 * <p>M5 part H (design/campaign Level 14 and design/enemies/bosses/siege-spire; decisions H1–H5 = a):
 * root bursts place their growths at a point of the screen after their telegraph ({@link
 * RootBurst}); an anchored boss without lanes lobs and sweeps from its parts, its windows'
 * ambush launches fly planned rear paths ({@link Launches}) (no edge warning since 2026-10-21: a boss never warns),
 * and a phase's move lifts it
 * off its anchor, the stump staying behind. New state is hashed only in a level that uses it.
 *
 * <p>The step's passes live in package-private classes, each with the state only it uses: {@link HitPasses}
 * (what hits the ship, Rook and the drones), {@link ContactPasses} (what stops shots and bullets, the touches
 * of rock), {@link ObjectivePasses} (kills, losses, the objectives they decide and the level's end), {@link
 * AllyPasses} (the convoy), {@link GroundPasses} (ground objects and debris), {@link PickupPasses}, {@link
 * BossPasses} (set pieces and what the bosses do), {@link HoldZones} (holds and the collapse), {@link Latches},
 * {@link Sightings} (first-sighting cues), {@link EscortSlot} (Rook or the Bulwark) and the {@link BossCheckpoint};
 * the level clock and the scroll are {@link LevelClock}'s (planned before play: {@link ScrollPlan}), the attempts
 * {@link Attempts}', the outcomes of hits and escapes {@link SortieOutcomes}' and the hash {@link SortieHash}'s.
 * The sortie keeps the order of the passes in {@link #step(int)}, the clock's and the attempt's state the passes
 * share, and the accessors.
 */
public final class Sortie {
    private static final int GROUND_CAPACITY = 32;
    private static final int PICKUP_CAPACITY = 64;
    private static final int EVENT_CAPACITY = 256;
    private static final int DEBRIS_CAPACITY = 48;

    final SplitMix64 rng;
    final Ship ship;
    final LevelScript script;
    final Rules rules;
    final PlayerFire fire;
    final SpecialSlot special;
    final EnemyForce force;
    final Pool<GroundObject> ground = new Pool<>(GROUND_CAPACITY, GroundObject::new, GroundObject[]::new);
    final Pool<Pickup> pickups = new Pool<>(PICKUP_CAPACITY, Pickup::new, Pickup[]::new);
    final Pool<Debris> debris = new Pool<>(DEBRIS_CAPACITY, Debris::new, Debris[]::new);
    final SetPiece[] setPieces;

    // The step's passes and their state (see the class comment).
    /** What hits the ship, Rook and the drones. */
    final HitPasses hitPasses;
    /** What stops shots and bullets, and the ship's and Rook's touches of rock. */
    final ContactPasses contacts;
    /** The outcomes of kills and losses: pay, drops, lines, objectives. */
    final ObjectivePasses objectivePasses;
    /** The convoy's passes: what hurts its units, and what follows. */
    final AllyPasses allies;
    /** The ground objects and debris chunks: let in, scrolled, destroyed. */
    final GroundPasses groundPasses;
    /** The pickups: dropped, placed, drifting and collected. */
    final PickupPasses pickupPasses;
    /** The set pieces' and bosses' passes, and what the bosses do to the level. */
    final BossPasses bossPasses;
    /** M5 part C: the hold zones and the collapse. */
    final HoldZones holdZones;
    /** M5 part F: the units latched onto the ship (the Lamprey's), their shake-off and drain. */
    final Latches latches;
    /** The radio's first-sighting cues (a pounce, a decloak, a loop-back, a surfacing). */
    final Sightings sightings;
    /** The boss checkpoint; null in a level without a boss. */
    final BossCheckpoint checkpoint;
    /** M7 part F: whether a boss walks passes (Level 19's walker): only then are the homing missiles hashed. */
    final boolean walks;
    /** M7 part H: the breaching boss (Level 21's Dust Colossus); null without one. */
    final SetPiece breachBoss;
    /** The level clock and the scroll in play: the arena's halt, the ramps and eases, the crawl, the travel. */
    final LevelClock clock;
    /** Step SIM-K: the sets pieces the level's enemy total counts (its bosses left out when the secondary says so). */
    private final int countedPieces;

    /** The boss whose arrival records the checkpoint (the first boss); -1 without one. */
    final int checkpointBoss;
    /** Round 40: whether the arena boss's slam arms are rooted (their roots' hits are events). */
    private final boolean rootedArena;
    /** The arena section's end and start, steps; -1 without an arena. */
    final int arenaEndTicks;

    final int arenaStartTicks;
    /** What the guns and the Airstrike destroy is handed here (and what escapes, and a tendril's end: {@link SortieOutcomes}). */
    private final PlayerFire.Hits hits;

    final SimEvents events = new SimEvents(EVENT_CAPACITY);
    final Tally tally;
    final Objectives objectives;
    final Radio radio;
    final Crane[] cranes;
    /** The mass-driver sleds; null without them. */
    final Sled sled;
    /** {@link #sled} for the presentation, made once (stepping allocates nothing). */
    private final java.util.Optional<Sled> sledView;
    /** M7 part F: the conveyor belts (none in most levels). */
    final Belts belts;
    /** M7 part F: the furnace vents (no grates in most levels). */
    final FurnaceVents vents;
    /** The level's darkness; null in a lit level. */
    private final HazardSpec.Darkness darkness;
    /** Part G: the tows (Level 07's lifeboat). */
    final Tow[] tows;
    /** Part G: the set piece of a parts objective (Level 07's carrier); -1 without one. */
    private final int partsPiece;

    /** The damage of the lobs that landed on the ship in this step. */
    double directHits;
    /** The escort objective's convoy, or (M5 part E) the naval convoy; null without one. */
    final Convoy convoy;

    final int launchTicks;
    private final int endTicks;
    /** A secret's crate or data core: the level's own life for it. */
    private final int crateTicks;
    /** Its attempts: their start, the restart, the boss checkpoint's record. */
    private final Attempts attempts;
    /** The escort slot: the wingman (Rook) or the Bulwark, their step, hits and start armour. */
    final EscortSlot escorts;
    /** M5 part A: the wingman in the escort slot (Rook); null without one. */
    final Wingman wingman;
    /** M8 part A: the Bulwark heavy drone in the escort slot; null without one (then nothing of it is hashed). */
    final Bulwark bulwark;

    long tick;
    int levelTick;
    /** M5 part C: the level clock's part of a step past {@link #levelTick}, in 1/{@link #CLOCK_ONE} steps. */
    int tickFraction;
    /** Whether {@link #levelTick} moved on in this step (a step in a hold may leave it). */
    boolean clockTicked;
    /** Steps since this attempt started: the real clock. */
    int realTick;
    /** M7 part B: the level's no-surface zones, scrolled every step; none in most levels. */
    private final SurfaceZones surfaceZones;
    /**
     * M7 part C: the level's rock and what touching it does (Level 16); null (and the contacts null)
     * without terrain.
     */
    private final GroundSpec.TerrainSpec terrainSpec;
    /** M7 part C: the rock with the scroll now, shared by the fire, the enemies and the wingman. */
    private final Canyon canyon;
    /** M7 part C: the level's tendril gates (design/enemies/ground/tendril-anchor); null without. */
    final Links links;

    int attempt = 1;
    boolean wrecked;
    /** Whether the primary objective failed in this attempt (the convoy is lost, a battery got away). */
    boolean failed;
    /** The destroy-targets group whose loss failed the primary; -1 for none. */
    int failedGroup = -1;
    /** What the convoy earned at the level end, for the debrief. */
    int escortCredits;
    /** The armour of the next attempt, once {@link #retry(double)} asked for one; 0 while none is asked for. */
    double retryArmour;

    double groundScroll;
    private int edgeWarnings;
    boolean complete;
    /** Boss stream units and (M5 part C) periodic spawners' units let in in this attempt (they count among the enemies). */
    int streamReleased;
    /**
     * M7 part G: the pursuits' refill motes left out that the kill ratio knows of (not hashed: it follows the force's
     * count at the next spawn, after a restart or a checkpoint too).
     */
    private int skippedSeen;
    /** Whether this attempt recorded the boss checkpoint. */
    boolean checkpointTaken;
    /** Whether the next step restarts at the boss checkpoint. */
    boolean bossRetry;
    /** Whether the next step jumps the clock to the arena's end (the boss died early). */
    boolean arenaJump;
    /** M5 part F: the level's wind and lightning (exactly no push without weather). */
    final Wind wind;
    /**
     * M8 part A (design/world/europa, Under water rules): the medium the ship flies in, from the level clock; the air
     * (no factor) in every level before Act 4. Hashed only in a level with water.
     */
    final Medium medium;
    /** M5 part F: the weather sondes (none in most levels). */
    final Sondes sondes;
    /** M5 part G: the step the defend primary's synchronisation completes at; -1 without one. */
    final int defendEndTicks;
    /** M5 part H: per root burst (in the level's order), its first growth's slot in the force's burst arrays. */
    final int[] burstSlots;

    /** @param armour the ship's armour at the level start (design/systems/retry: not full, unless it was full) */
    public Sortie(long seed, Loadout loadout, LevelScript script, Rules rules, double armour) {
        if (rules.bulletBudget() > EnemyForce.BULLET_CAPACITY) {
            throw new IllegalArgumentException("the bullet budget exceeds the pool of " + EnemyForce.BULLET_CAPACITY);
        }
        rng = new SplitMix64(seed);
        ship = new Ship(
                loadout.ship(),
                new Defences(loadout.shield(), loadout.plating(), loadout.ship().mercySeconds()),
                loadout.thrusters().orElse(null));
        // M8 part A: the Auto-repair nanites work in the defences (nothing of them without).
        loadout.nanites().ifPresent(ship.defences()::nanites);
        this.script = script;
        this.rules = rules;
        setPieces = script.setPieces().stream().map(SetPiece::new).toArray(SetPiece[]::new);
        List<BossSpec.Stream> bossStreams = script.setPieces().stream()
                .flatMap(piece -> piece.boss().stream())
                .flatMap(boss -> boss.phases().stream())
                .flatMap(phase -> phase.stream().stream())
                .toList();
        BossSpec.Stream[] streams = bossStreams.toArray(BossSpec.Stream[]::new);
        int firstBoss = -1;
        for (int k = setPieces.length - 1; k >= 0; k--) {
            firstBoss = setPieces[k].boss().isPresent() ? k : firstBoss;
        }
        checkpointBoss = firstBoss;
        clock = new LevelClock(this, script, setPieces);
        arenaEndTicks = clock.arenaEndTicks;
        arenaStartTicks = clock.arenaStartTicks;
        int counted = setPieces.length;
        if (!script.objectives().secondary().countsBosses()) {
            for (SetPiece piece : setPieces) {
                counted -= piece.spec().isBoss() ? 1 : 0;
            }
        }
        countedPieces = counted;
        SetPiece anchored = clock.arenaBoss;
        breachBoss = clock.breachBoss;
        Hitbox laneBox = anchored == null || anchored.arena().orElseThrow().laneCount() == 0
                ? null
                : new Hitbox(
                        anchored.arena().orElseThrow().laneWidth(),
                        anchored.arena().orElseThrow().laneTop());
        rootedArena = anchored != null && anchored.arena().orElseThrow().rooted();
        darkness = script.hazards().darkness().orElse(null);
        tows = script.objectives().tows().stream().map(Tow::new).toArray(Tow[]::new);
        int[] partDropPiece = new int[script.drops().partDrops().size()];
        for (int d = 0; d < partDropPiece.length; d++) {
            partDropPiece[d] = pieceOf(script.drops().partDrops().get(d).slug());
        }
        partsPiece = script.objectives().secondary().byParts()
                ? pieceOf(script.objectives().secondary().partsOf())
                : -1;
        SortieOutcomes hits = new SortieOutcomes(this);
        this.hits = hits;
        fire = new PlayerFire(
                ship,
                loadout.armament(),
                events,
                hits,
                setPieces,
                script.ground().water());
        medium = new Medium(script.medium());
        // M8 part A: the Pressure hull takes the water's drag off the ship.
        medium.hull(loadout.pressureHull());
        fire.medium(medium);
        terrainSpec = script.ground().terrain().orElse(null);
        canyon = terrainSpec == null ? null : new Canyon(terrainSpec.terrain());
        fire.canyon(canyon);
        special = new SpecialSlot(
                loadout.special(),
                events,
                ship.defences(),
                medium,
                script.ground().water(),
                script.specialLimits());
        // The boss streams' units and (part G) the units its windows launch are enemy kinds of the level.
        List<EnemySpec> streamKinds = java.util.stream.Stream.concat(
                        bossStreams.stream().map(BossSpec.Stream::enemy),
                        script.setPieces().stream()
                                .flatMap(piece -> piece.boss().stream())
                                .flatMap(boss -> boss.spawnKinds().stream()))
                .distinct()
                .toList();
        force = new EnemyForce(
                script.waves(),
                script.groundUnits(),
                streamKinds,
                rng,
                rules,
                events,
                hits,
                ScrollPlan.spans(script),
                // M7 part G: the waves and the ground units of a reversed scroll are flipped.
                script.reverses() ? t -> travelAt(script, t) < 0 : t -> false,
                WallMounts.of(script));
        force.canyon(canyon);
        if (medium.water()) {
            // M8 part A: the enemies' bullets and missiles fire at the water's speed.
            force.medium(medium);
        }
        fire.missiles(force.missiles());
        if (special.flares() != null) {
            // M8 part A: the homing missiles go for the burning decoy flares.
            force.missiles().decoys(special.flares());
        }
        boolean walking = false;
        for (SetPiece piece : setPieces) {
            walking |= piece.stride().isPresent();
        }
        walks = walking;
        links = script.ground().gates().isEmpty()
                ? null
                : new Links(script.ground().gates(), events, hits);
        if (links != null) {
            force.links(links, script.ground().gates());
            fire.links(links);
        }
        tally = new Tally(rules.scoring());
        objectives = new Objectives(
                script.objectives().secondary(),
                script.groups().size(),
                force.kinds().size(),
                enemyTotal(),
                script.groundUnits(),
                ObjectivePasses.escapers(script, force, setPieces, partsPiece));
        cranes = script.hazards().cranes().stream().map(Crane::new).toArray(Crane[]::new);
        sled = script.hazards().sled().map(Sled::new).orElse(null);
        sledView = java.util.Optional.ofNullable(sled);
        belts = new Belts(script.ground().belts());
        vents = new FurnaceVents(script.hazards().vents(), arenaStartTicks >= 0 ? arenaStartTicks : Integer.MAX_VALUE);
        radio = new Radio(script.radio(), events, ship, Radio.fitted(loadout.armament(), special));
        convoy = AllyPasses.convoy(script, force);
        surfaceZones = SurfaceZones.of(script);
        force.surfaceZones(surfaceZones);
        defendEndTicks = script.objectives()
                .defend()
                .map(defend -> SimStep.ticks(defend.end()))
                .orElse(-1);
        allies = new AllyPasses(this, script, convoy, ship, force, events, radio, objectives, rules);
        launchTicks = SimStep.ticks(script.launchSeconds());
        endTicks = SimStep.ticks(script.seconds());
        crateTicks = SimStep.ticks(rules.pickups().crateSeconds());
        escorts = new EscortSlot(this, loadout, seed, medium, canyon, links, vents, force);
        wingman = escorts.wingman;
        bulwark = escorts.bulwark;
        radio.escort(wingman);
        wind = new Wind(script.hazards().weather());
        sondes = new Sondes(script.hazards().sondes());
        if (links != null && checkpointBoss >= 0) {
            throw new IllegalArgumentException("tendril gates in a level with a boss checkpoint are not supported");
        }
        if (sondes.count() > 0 && checkpointBoss >= 0) {
            throw new IllegalArgumentException("weather sondes in a level with a boss checkpoint are not supported");
        }
        if (script.objectives().defend().isPresent() && checkpointBoss >= 0) {
            throw new IllegalArgumentException("a defend primary in a level with a boss checkpoint is not supported");
        }
        latches = new Latches(this, ship, force, events, radio);
        sightings = new Sightings(events, radio);
        holdZones = new HoldZones(this, script, ship, force, events, radio, objectives, hits);
        hitPasses = new HitPasses(
                this, ship, fire, force, events, tally, setPieces, cranes, wingman, latches, sled, debris, links);
        contacts = new ContactPasses(
                this,
                terrainSpec,
                canyon,
                ship,
                fire,
                force,
                events,
                radio,
                objectives,
                rules,
                hitPasses,
                cranes,
                tows,
                sled,
                debris,
                pickups,
                crateTicks,
                wingman);
        objectivePasses = new ObjectivePasses(
                this,
                script,
                rules,
                ship,
                force,
                events,
                radio,
                tally,
                objectives,
                setPieces,
                wingman,
                escorts.wingmanMount,
                links,
                sondes,
                partDropPiece,
                partsPiece);
        pickupPasses = new PickupPasses(
                this, script, loadout, rules, ship, fire, special, events, radio, tally, pickups, canyon);
        groundPasses = new GroundPasses(
                this,
                script,
                rng,
                ship,
                force,
                events,
                radio,
                tally,
                objectives,
                ground,
                pickups,
                debris,
                sled,
                crateTicks);
        bossPasses = new BossPasses(
                this,
                script,
                seed,
                streams,
                anchored,
                laneBox,
                partsPiece,
                ship,
                force,
                events,
                radio,
                objectives,
                rules,
                setPieces,
                wingman,
                convoy,
                hitPasses,
                allies);
        checkpoint = checkpointBoss < 0 ? null : new BossCheckpoint(this);
        int bursts = 0;
        for (int b = 0; b < force.burstSlots(); b++) {
            bursts += force.burstFirst(b) ? 1 : 0;
        }
        burstSlots = new int[bursts];
        for (int b = 0, k = 0; b < force.burstSlots(); b++) {
            if (force.burstFirst(b)) {
                burstSlots[k++] = b;
            }
        }
        attempts = new Attempts(this);
        attempts.start(armour);
    }

    /** The index of the set piece {@code slug} among the level's; -1 for none. */
    private int pieceOf(String slug) {
        for (int k = 0; k < setPieces.length; k++) {
            if (setPieces[k].slug().equals(slug)) {
                return k;
            }
        }
        return -1;
    }

    /** Advances the sortie by one step with the given {@link Command} set. */
    public void step(int commands) {
        tick++;
        events.clear();
        attempts.step();
        realTick++;
        clockTicked = false;
        double scrollStep = clock.step();
        // M8 part A: the medium at the level clock now (the waterline's crossing eases the factors).
        medium.at(levelTick);
        if (surfaceZones.active()) {
            surfaceZones.scroll(groundScroll);
        }
        if (canyon != null) {
            canyon.scroll(groundScroll);
        }
        force.groundScroll(groundScroll);
        ship.rememberPosition();
        // M5 part F: the level's wind this step; exactly 0 without weather.
        double push = wind.push(levelTick);
        // M7 part A: the drones' orbit and rebuild, the Deflector Pods' charges (nothing without them).
        fire.tend();
        if (launching()) {
            ship.launch((double) levelTick / launchTicks, script.launchDrop());
        } else if (flying()) {
            double fromX = ship.x();
            double fromY = ship.y();
            // M7 part E: the Dust Devils' pull adds to the wind's push (exactly 0 without a unit with a pull).
            force.pull(fromX, fromY);
            // M8 part A: the water's drag (exactly 1 in the air).
            ship.drag(medium.wet() ? medium.shipAcceleration() : 1);
            if (breachBoss != null) {
                // M7 part H: the breaching boss's suction adds to the wind and the devils' pull.
                BreachArena breach = breachBoss.breach().orElseThrow();
                breach.pull(fromX, fromY);
                ship.fly(commands, push + force.pullX() + breach.pullX(), force.pullY() + breach.pullY());
            } else {
                ship.fly(commands, push + force.pullX(), force.pullY());
            }
            if (ship.dashStarted()) {
                // M7 part A: the thruster burst and the afterimages, from where the dash started.
                events.add(SimEvents.Type.DASH, fromX, fromY, ship.dashEighth());
            }
            if (latches.present()) {
                latches.shake(commands);
            }
            fire.fire(commands, force.enemies(), ground);
        }
        if (launching() || !flying()) {
            // M7 part A: a beam runs only while the ship flies.
            fire.ceaseBeams();
            force.noPull();
        }
        escorts.fly(commands, push);
        force.wind(push);
        // M8 part A: the special's setting limit now (the medium at the level clock, the level's lists).
        special.setting(levelTick, ship.x(), ship.y());
        special.command(commands, !launching() && flying() && !complete, ship.x(), ship.y());
        tally.step(PlayerFire.chainTargets(force.enemies(), setPieces));
        if (!complete) {
            if (burstSlots.length > 0) {
                // M5 part H: the root bursts' telegraphs, before their growths are let in.
                force.bursts(levelTick, ship.x(), groundScroll, scrollStep);
            }
            force.spawn(levelTick);
            if (force.skipped() != skippedSeen) {
                // M7 part G: a pursuit's refill motes left out (no room in the flock) count nowhere.
                skippedSeen = force.skipped();
                objectives.unitsSkipped(skippedSeen);
            }
            groundPasses.placeGroundObjects();
            groundPasses.placeDebris();
            radio.byTime(levelTick);
            wind.flash(levelTick, events);
            pickupPasses.placePickups();
        }
        for (Crane crane : cranes) {
            crane.update(levelTick);
        }
        for (Tow tow : tows) {
            tow.update(levelTick);
            if (tow.releaseCrate()) {
                contacts.dropTowCrate(tow);
            }
        }
        if (sled != null) {
            groundPasses.updateSled();
        }
        if (vents.grates() > 0) {
            // M7 part F: the furnace vents' ripples follow the level clock.
            vents.update(levelTick, groundScroll, clockTicked, events);
        }
        bossPasses.flySetPieces();
        // 2026-10-21 (user): only the waves' first entries warn; a boss (the walker's entries, the breaching boss's
        // points, the Spire's launched Wraiths) never does: the boss fight keeps its surprise.
        edgeWarnings = complete ? 0 : force.warnings(levelTick);
        fire.move(scrollStep, force.enemies(), ground);
        boolean firing = flying() && !complete;
        force.move(ship, firing, scrollStep);
        if (belts.any()) {
            // M7 part F: the conveyor belts carry the ground units on them.
            belts.carry(force.enemies(), groundScroll);
        }
        if (force.rollers()) {
            // M7 part D: a rolling Shellback crushes the destructible ground objects in its path.
            groundPasses.crushGround();
        }
        if (links != null) {
            // M7 part C: the tendrils follow their anchors, before the beam burns them and they touch.
            links.step(scrollStep);
        }
        bossPasses.fireSetPieces(firing);
        force.moveBullets();
        force.moveMines(ship);
        if (force.missiles().size() > 0) {
            // M7 part F: the homing missiles steer toward the ship.
            force.missiles().fly(ship.x(), ship.y());
        }
        directHits = force.moveLobs(
                ship,
                scrollStep,
                flying() && !complete && !failed && fire.deflectors().any() ? fire.deflectors() : null);
        groundPasses.moveDebris();
        contacts.blockShots();
        groundPasses.scrollGround(scrollStep);
        pickupPasses.driftPickups();
        fire.hitEnemies(force.enemies());
        fire.hitSetPieces();
        fire.hitMines(force.mines());
        fire.hitMissiles();
        if (darkness != null) {
            groundPasses.light();
        }
        fire.hitGround(ground, force.enemies(), force.mines());
        fire.burn(force.enemies(), ground, force.mines());
        if (canyon != null) {
            // M7 part C: after the hit passes, so a unit at a wall's foot is hit before the rock stops the shot.
            fire.stopAtRock(ground, force.enemies());
            contacts.stopBulletsAtRock();
        }
        special.update(scrollStep, force.enemies(), ground, setPieces, hits);
        special.bomb(force.enemies(), ground, setPieces, hits, force.bullets(), force.lobs(), force.mines());
        special.lance(ship.x(), force.enemies(), ground, setPieces, hits);
        if (rootedArena) {
            bossPasses.rootHits();
        }
        if (convoy != null && convoy.naval()) {
            // M5 part E: the naval convoy glides on the real steps at the arena's halt and after it.
            convoy.sail(clock.clockHeld(), scrollStep, events);
        } else if (convoy != null) {
            convoy.update(levelTick, groundScroll, scrollStep);
            if (convoy.air() && !complete && !wrecked && !failed) {
                allies.scriptedLoss();
            }
        }
        if (flying() && !complete && !failed) {
            // M7 part A: the drones absorb what touches them first, then the Deflector Pods catch.
            if (!rules.invulnerableShip()) {
                hitPasses.hitDrones();
            }
            hitPasses.deflect();
        }
        if (flying() && !complete && !failed && !rules.invulnerableShip()) {
            if (!ship.evading()) {
                // M7 part A: in a dash's invulnerability bullets pass through, contacts and hazards do not hurt.
                hitPasses.hitShip();
                hitPasses.ramShip();
                hitPasses.hitByCranes();
                hitPasses.hitBySled();
                if (vents.grates() > 0) {
                    hitPasses.hitByVents();
                }
                hitPasses.hitByDebris();
                hitPasses.hitBySetPieces();
                if (links != null && !wrecked) {
                    hitPasses.touchTendrils();
                }
            }
            if (latches.present()) {
                latches.drainShield();
            }
        }
        if (force.rings() != null && flying() && !complete && !failed) {
            // M8 part E: the Sirens' rings slow the ship (not in a dash) and Rook.
            force.rings()
                    .reach(
                            ship,
                            !ship.evading(),
                            wingman == null || wingman.ejected() ? null : wingman,
                            canyon,
                            events);
        }
        if (canyon != null) {
            contacts.scrape();
        }
        if (flying() && !complete && !failed && !rules.invulnerableShip()) {
            escorts.hit();
        }
        if (convoy != null && !convoy.naval() && !complete && !wrecked && !failed) {
            allies.hitAllies();
        }
        if (defendEndTicks >= 0 && !complete && !wrecked && !failed) {
            allies.defendStep();
        }
        if (convoy != null && convoy.groundAllies() && !complete && !wrecked && !failed && convoy.allPassed()) {
            // M7 part D: the last ground ally left the bottom edge: an untouched secondary none was hit is met.
            if (objectives.alliesPassed()) {
                objectivePasses.paySecondary();
            }
        }
        if (flying()) {
            pickupPasses.collectPickups();
        }
        if (sondes.count() > 0) {
            objectivePasses.updateSondes();
        }
        sightings.step();
        if (holdZones.collapse != null) {
            holdZones.updateCollapse();
        }
        if (holdZones.holds() && !complete) {
            // After the collapse: a hold over the collapse's groups ends in the step its dust has settled.
            holdZones.updateHolds();
        }
        if (!complete && !wrecked && !failed && levelTick >= endTicks) {
            objectivePasses.completeLevel();
        }
    }

    /** The level clock's step: {@link #CLOCK_ONE} is one step of script time. */
    public static final int CLOCK_ONE = 1 << 16;

    /**
     * Restarts the level at the next step from its start state, after the ship's destruction or from
     * the pause menu (design/systems/retry): a new attempt with {@code armour} points of armour, and
     * what this one earned is lost.
     */
    public void retry(double armour) {
        if (!(armour > 0)) {
            throw new IllegalArgumentException("a retry needs armour");
        }
        retryArmour = armour;
    }

    /**
     * As {@link #retry(double)}, the wingman starting the next attempt with {@code wingmanArmour}
     * (design/player/wingmen: his level-start armour, at least the retry's armour floor).
     */
    public void retry(double armour, double wingmanArmour) {
        retry(armour);
        escorts.retry(wingmanArmour);
    }

    /** M5 part E: whether the scroll has come to a halt in an arena of speed 0 ({@link LevelClock#scrollHalted}). */
    boolean scrollHalted() {
        return clock.scrollHalted();
    }

    /**
     * M7 part F: arena boss {@code boss} died at the level clock's tick now ({@link LevelClock#bossDownInArena}): an
     * arena with a hold after its boss keeps the clock halted for its death chain and then the hold.
     */
    void bossDownInArena(BossSpec boss) {
        clock.bossDownInArena(boss);
    }

    /**
     * M7 part H: whether the level's arena crawls ({@link Section#crawls()}): its ground scrolls on at the arena's
     * speed while the level clock is held, and the kill's jump moves the clock only.
     */
    public boolean crawling() {
        return clock.crawling;
    }

    /** Step SIM-K: the share of its speed a crawling arena's ground scrolls at now, 0–1 (1 in any other level). */
    public double crawlFactor() {
        return clock.crawlFactor();
    }

    /** Whether the level clock is halted in the arena now (the boss outlived its arena). */
    public boolean arenaHalted() {
        return clock.clockHeld();
    }

    /**
     * Restarts at the boss checkpoint at the next step (design/systems/retry, Retry from boss): a
     * new attempt with the defences, charges, credits, score and objective tallies recorded just
     * before the boss arrived, on an empty field; the boss arrives again.
     */
    public void retryFromBoss() {
        if (!bossCheckpoint()) {
            throw new IllegalStateException("no boss checkpoint in this attempt");
        }
        bossRetry = true;
    }

    /** Whether this attempt has reached a boss checkpoint, so {@link #retryFromBoss()} can restart there. */
    public boolean bossCheckpoint() {
        return checkpointTaken;
    }

    void resetArena() {
        clock.reset();
        arenaJump = false;
        bossPasses.bossPhase = -1;
    }

    /** Whether the ground object {@code object} is lit now: always outside a dark level. */
    public boolean lit(GroundObject object) {
        return darkness == null
                || darkness.lit(object.x(), object.y(), object.spec().size(), ship.x(), ship.y(), levelSeconds());
    }

    /** M7 part F: whether the clock is halted after an arena boss's death (its death chain and the hold after it). */
    public boolean holdingAfterBoss() {
        return clock.postBossHold > 0;
    }

    /** Out of the arena the scroll ramps up to the next section's speed over this time. */
    public static final double ARENA_RAMP_SECONDS = 1;

    /**
     * Step SIM-K (round 44, K.c): a crawling arena's ground eases from its speed to a stop (and back) over this time
     * as the breaching boss comes out of the sand (and goes back under), s.
     */
    public static final double CRAWL_EASE_SECONDS = 0.5;

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): the direction of travel now, +1 or -1: the scroll's sign,
     * or the last non-zero sign through a zero of the scroll (the turnabout's); +1 before the scroll first moves.
     */
    public int travel() {
        return clock.travel;
    }

    /** M7 part G: whether the scroll runs reversed now ({@link #travel()} is -1): the ground moves up the screen. */
    public boolean reversed() {
        return clock.travel < 0;
    }

    /**
     * M7 part G: the direction of travel at level time {@code t} s of a level ({@link #travel()} at that time; a level
     * with a reversed section has no holds nor arena, so its clock is the steps'): what plans the waves and ground
     * units of a reversed scroll as flipped. +1 throughout a level without a reversed section.
     */
    public static int travelAt(LevelScript script, double t) {
        return travelAt(script.sections(), SimStep.ticks(t));
    }

    /** M7 part G: {@link #travelAt(LevelScript, double)} at level step {@code tick} ({@link ScrollPlan#travelAt}). */
    static int travelAt(List<Section> sections, int tick) {
        return ScrollPlan.travelAt(sections, tick);
    }

    /**
     * M5 part G (design/campaign Level 13, The halt): the scroll's speed in px/s at level step {@code tick} of a level
     * without hold zones or an arena: its section's speed, eased into and out of a timed halt and (M7 part B) into a
     * section with an ease ({@link ScrollPlan#timedHaltSpeed}).
     */
    public static double timedHaltSpeed(List<Section> sections, int tick) {
        return ScrollPlan.timedHaltSpeed(sections, tick);
    }

    /**
     * M5 part G: how far the ground has scrolled when the level clock reaches {@code t} s in a level without hold
     * zones or an arena, px ({@link ScrollPlan#scrollAt}).
     */
    public static double scrollAt(LevelScript script, double t) {
        return ScrollPlan.scrollAt(script, t);
    }

    /**
     * M5 part E: the ground's scroll when it has halted in the arena: the scroll at the arena's start plus the ease
     * into the halt ({@link ScrollPlan#haltScroll}).
     */
    static double haltScroll(LevelScript script, double fromSpeed) {
        return ScrollPlan.haltScroll(script, fromSpeed);
    }

    /**
     * M5 part E: how far the ground scrolls while it eases from {@code speed} to a halt at an arena of speed 0, px
     * ({@link ScrollPlan#easeDistance}); a level's backdrop pieces that must lie under the halted boss are placed with it.
     */
    public static double easeDistance(double speed) {
        return ScrollPlan.easeDistance(speed);
    }

    /** M7 part C: how far from rock a pickup slides, px (about half its size). */
    static final double PICKUP_ROCK_MARGIN = 8;

    /** Whether the wingman made a kill with his gun in this attempt (the {@code escort-first-kill} radio event fired). */
    public boolean escortKilled() {
        return objectivePasses.escortKilled;
    }

    /**
     * Fires an enemy bullet from ({@code x}, {@code y}) at ({@code vx}, {@code vy}) px/s dealing {@code
     * damage}, as a gun does; package-private for the tests (M5 part D's shuttle hits).
     */
    void fireEnemyBullet(double x, double y, double vx, double vy, double damage) {
        fireEnemyBullet(x, y, vx, vy, damage, -1);
    }

    /** As {@link #fireEnemyBullet(double, double, double, double, double)}, aimed at convoy unit {@code target} (-1: the ship). */
    void fireEnemyBullet(double x, double y, double vx, double vy, double damage, int target) {
        EnemyBullet bullet = force.bullets().obtain();
        if (bullet != null) {
            bullet.fire(x, y, vx, vy, damage, target);
        }
    }

    /**
     * M7 part C: the level's tendril gates and their tendrils (design/enemies/ground/tendril-anchor); null in a
     * level without gates.
     */
    public Links links() {
        return links;
    }

    /** M7 part C: the level's rock (design/campaign Level 16, Terrain); null in a level without terrain. */
    public Terrain terrain() {
        return terrainSpec == null ? null : terrainSpec.terrain();
    }

    /** M7 part C: whether the ship's wall contact cooldown runs (the hit flash may follow it); false without terrain. */
    public boolean wallCooling() {
        return contacts.wallCooling();
    }

    /** A hash over the complete state; equal hashes mean equal replays. */
    public long stateHash() {
        return SortieHash.of(this);
    }

    /** The totals for the debrief; meaningful once the level is {@link #complete()}. */
    public LevelResult result() {
        Defences defences = ship.defences();
        return LevelResult.of(
                        rules.scoring(),
                        script,
                        tally,
                        enemyTotal(),
                        defences.armourLost(),
                        defences.maxArmour(),
                        objectives.secretsFound(),
                        objectives.secondaryMet(),
                        convoy == null || convoy.naval() || convoy.defend() || convoy.groundAllies()
                                ? LevelResult.Escort.NONE
                                : new LevelResult.Escort(
                                        convoy.escort().ally().slug(),
                                        convoy.saveableAlive(),
                                        convoy.saveable(),
                                        escortCredits),
                        bossTime())
                .withDataCores(dataCores());
    }

    /** The data cores collected in this attempt, in the level's secret order. */
    public List<LevelResult.DataCore> dataCores() {
        return pickupPasses.dataCores();
    }

    /** The level's boss's kill time and par, for the debrief and the Boss rush bonus; {@link LevelResult.BossTime#NONE} without a boss. */
    private LevelResult.BossTime bossTime() {
        if (checkpointBoss < 0) {
            return LevelResult.BossTime.NONE;
        }
        SetPiece boss = setPieces[checkpointBoss];
        return new LevelResult.BossTime(boss.boss().orElseThrow().parSeconds(), boss.killSeconds());
    }

    public LevelScript script() {
        return script;
    }

    /** The distinct enemies of the level; {@link Enemy#kind()} and enemy events index this list. */
    public List<EnemySpec> enemyKinds() {
        return force.kinds();
    }

    public Ship ship() {
        return ship;
    }

    /** The fitted weapons; shot events and {@link Shot#mount()} index its mounts. */
    public Armament armament() {
        return fire.armament();
    }

    /** Steps since mount {@code m} last fired, for its muzzle flash; {@link Integer#MAX_VALUE} before its first shot. */
    public int ticksSinceShot(int m) {
        return fire.ticksSinceShot(m);
    }

    /**
     * M5 part E: whether mount {@code m} fires nothing in this level, a torpedo pod over land (the HUD's
     * NO WATER; it still draws its power).
     */
    public boolean mountIdle(int m) {
        return fire.idle(fire.armament().mount(m).weapon());
    }

    /**
     * M7 part A: mount {@code m}'s beam (design/player/weapons/ion-beam): whether it runs, its column
     * and what it ends on this step; null unless mount {@code m} is a {@link WeaponSpec.Delivery#BEAM}
     * weapon.
     */
    public Beam beam(int m) {
        return fire.beam(m);
    }

    /** M7 part A: the drone bays' drones (design/player/wingmen, Light drone), by mount. */
    public Drones drones() {
        return fire.drones();
    }

    /** M7 part A: the Deflector Pods' fields and charges (design/player/weapons/deflector-pod), by mount. */
    public Deflectors deflectors() {
        return fire.deflectors();
    }

    /** Mount {@code m}'s turret heading (the Swivel Gun's pod), radians clockwise from up. */
    public double turretHeading(int m) {
        return fire.turretHeading(m);
    }

    /** M8 part E: the Sirens' rings in flight (design/enemies/naval/siren); null in a level without a ring unit. */
    public SonarRings sonarRings() {
        return force.rings();
    }

    /** M5 part A: the wingman flying in the escort slot (Rook), if one flies in this sortie. */
    public java.util.Optional<Wingman> wingman() {
        return escorts.wingmanView;
    }

    /** M8 part A: the Bulwark heavy drone flying in the escort slot, if one flies in this sortie. */
    public java.util.Optional<Bulwark> bulwark() {
        return escorts.bulwarkView;
    }

    /**
     * The mount index the wingman's shots ({@link Shot#mount()}) and their events
     * ({@link SimEvents.Type#SHOT_FIRED}, hits, blasts) carry: one past the {@link #armament()}'s.
     */
    public int wingmanMount() {
        return escorts.wingmanMount;
    }

    /** The special slot: its charges, and the Airstrike in flight for the presentation. */
    public SpecialSlot special() {
        return special;
    }

    /** Seconds of overdrive left; 0 without one. */
    public double overdriveSeconds() {
        return fire.overdriveTicks() * SimStep.SECONDS;
    }

    /**
     * M5 part E (design/allies, convoy cargo ship): a boss slam in lane {@code lane} (1-based) hits
     * every naval convoy unit afloat in it that slams hurt, once each (for E2c's slam cycle). Returns
     * the units hit; nothing without a naval convoy.
     */
    int slamAllies(int lane) {
        return allies.slamAllies(lane);
    }

    /**
     * Drops a pickup of {@code type} at ({@code x}, {@code y}), as a kill or a destroyed object does;
     * package-private for the tests. Returns whether it dropped (a special charge without a special
     * fitted does not, nor one the pool has no room for).
     */
    boolean drop(PickupType type, double x, double y) {
        return pickupPasses.drop(type, x, y);
    }

    /** Destroys enemy {@code index} as a hit would, paying it (for tests). */
    void destroyEnemy(int index) {
        objectivePasses.destroy(index);
    }

    /** The segment chains in flight (a regrowing one draws its growing head). */
    public int chainCount() {
        return force.chains().size();
    }

    public Chain chain(int index) {
        return force.chains().get(index);
    }

    /**
     * M7 part G (design/campaign Level 20, The pursuit swarm): the pursuit in flight, its leader point's phase and place
     * for the presentation and the autopilot (the first pursuit flock's; it lasts from its wave's entry until its last
     * mote is gone after the break-off, or until it has no members and no refill left); empty for none. No allocation.
     */
    public java.util.Optional<Pursuit> pursuit() {
        return force.pursuit();
    }

    /** M5 part D: the swarms in flight (design/enemies/air/mote-swarm); their members are among the enemies. */
    public int flockCount() {
        return force.flocks().size();
    }

    public Flock flock(int index) {
        return force.flocks().get(index);
    }

    /** The level's darkness, if it is dark. */
    public java.util.Optional<HazardSpec.Darkness> darkness() {
        return script.hazards().darkness();
    }

    /** Destroys part {@code part} of set piece {@code piece} as a hit would, paying it (for tests). */
    void destroyPart(int piece, int part) {
        if (setPieces[piece].damagePart(part, Double.MAX_VALUE)) {
            objectivePasses.wreck(piece, part);
        }
    }

    /** Drops a spore of {@code gun}'s mine at (x, y) drifting at {@code angle}, as a mine layer does (for tests). */
    void dropMine(EnemyGun gun, double x, double y, double angle) {
        force.dropMine(gun, x, y, angle);
    }

    /** Fires an enemy bullet from (x, y) at {@code angle} (0 = right, y up), as a gun does (for tests). */
    void fireBullet(double x, double y, double angle, double speed, double damage) {
        force.fireAngle(x, y, angle, speed, damage, ship);
    }

    /** Starts an overdrive of {@code seconds}, as its pickup does; a running one starts over. */
    void overdrive(double seconds) {
        fire.overdrive(SimStep.ticks(seconds));
    }

    public int shotCount() {
        return fire.shots().size();
    }

    public Shot shot(int index) {
        return fire.shots().get(index);
    }

    public int enemyCount() {
        return force.enemies().size();
    }

    public Enemy enemy(int index) {
        return force.enemies().get(index);
    }

    public int bulletCount() {
        return force.bullets().size();
    }

    public EnemyBullet bullet(int index) {
        return force.bullets().get(index);
    }

    public int groundObjectCount() {
        return ground.size();
    }

    public GroundObject groundObject(int index) {
        return ground.get(index);
    }

    public int pickupCount() {
        return pickups.size();
    }

    public Pickup pickup(int index) {
        return pickups.get(index);
    }

    /** M7 part F: the homing missiles in flight (the Revenant Walker's). */
    public int missileCount() {
        return force.missiles().size();
    }

    /** M7 part F: homing missile {@code index} in flight (0 ≤ index &lt; {@link #missileCount()}). */
    public Missile missile(int index) {
        return force.missiles().get(index);
    }

    /** M7 part F: the homing missiles in flight ({@link Missiles#size()}, {@link Missiles#get(int)}). */
    public Missiles missiles() {
        return force.missiles();
    }

    /** The spore mines drifting on the screen (below the player's layer until {@link Mine#armed()}). */
    public int mineCount() {
        return force.mines().size();
    }

    public Mine mine(int index) {
        return force.mines().get(index);
    }

    /** The debris chunks on the screen. */
    public int debrisCount() {
        return debris.size();
    }

    public Debris debris(int index) {
        return debris.get(index);
    }

    /** The level's set pieces, present or not; {@link SetPiece#present()} tells whether one is on screen. */
    public int setPieceCount() {
        return setPieces.length;
    }

    public SetPiece setPiece(int index) {
        return setPieces[index];
    }

    public int craneCount() {
        return cranes.length;
    }

    /** Part G: the tows of the level (Level 07's lifeboat), present or not. */
    public int towCount() {
        return tows.length;
    }

    public Tow tow(int index) {
        return tows[index];
    }

    public Crane crane(int index) {
        return cranes[index];
    }

    /** The groups of a group objective (Level 02's docks); 0 otherwise. */
    public int groupCount() {
        return objectives.groupCount();
    }

    /** Whether group {@code g} is still open (0), cleared (1) or lost (2). */
    public int groupState(int g) {
        return objectives.groupState(g);
    }

    /** The units of the escort objective's convoy; 0 without one. */
    public int allyCount() {
        return convoy == null ? 0 : convoy.size();
    }

    /** Convoy unit {@code k}, the leading one first. */
    public Ally ally(int k) {
        return convoy.get(k);
    }

    /** The convoy's units still alive in this attempt; 0 without a convoy. */
    public int alliesAlive() {
        return convoy == null ? 0 : convoy.alive();
    }

    /** The convoy unit hit first in this attempt, for its radio line; -1 before. */
    public int firstAllyHit() {
        return convoy == null ? -1 : convoy.firstHit();
    }

    /** The convoy unit lost first in this attempt, for its radio line; -1 before. */
    public int firstAllyLost() {
        return convoy == null ? -1 : convoy.firstLost();
    }

    /**
     * M5 part D: the convoy unit lost last in this attempt (never the scripted loss's), for the
     * {@code ally-lost} line ({@code {ally}}); -1 before. That cue's radio event follows the unit's
     * {@link SimEvents.Type#ALLY_LOST} event in the same step.
     */
    public int lastAllyLost() {
        return convoy == null ? -1 : convoy.lastLost();
    }

    /**
     * M5 part E: the convoy unit hit last in this attempt, for the {@code ally-hit} line ({@code
     * {ally}}); -1 before. That cue's radio event follows the unit's {@link SimEvents.Type#ALLY_HIT}
     * event in the same step.
     */
    public int lastAllyHit() {
        return convoy == null ? -1 : convoy.lastHit();
    }

    /** M5 part E: whether the level has a naval convoy outside the objectives (Level 11's ships). */
    public boolean navalConvoy() {
        return convoy != null && convoy.naval();
    }

    /** M5 part E: naval convoy unit {@code k}'s name ({@code {ally}} in its lines: "Halvorsen"); "" for an escort's unit. */
    public String allyName(int k) {
        if (groundAllies()) {
            return convoy.groundSpec().units().get(k).name();
        }
        return navalConvoy() ? convoy.navalSpec().units().get(k).name() : "";
    }

    /**
     * M7 part D: whether the level has ground allies outside the objectives (Level 17's Ares Landing domes):
     * {@link #ally(int)} {@code k} is unit k ({@link #allySpec(int)}, {@link #allyName(int)}, {@link
     * Ally#hitsTaken()}), in the tracker's order.
     */
    public boolean groundAllies() {
        return convoy != null && convoy.groundAllies();
    }

    /** M7 part D: the ground allies never hit in this attempt (the debrief's {@code DOMES UNTOUCHED n / 5}); 0 without them. */
    public int alliesUntouched() {
        return groundAllies() ? convoy.untouched() : 0;
    }

    /**
     * M7 part D: whether an untouched secondary failed in this attempt (a ground ally was hit); round 41: or a
     * standing one ("No dome lost": a ground ally broke).
     */
    public boolean untouchedFailed() {
        return objectives.untouchedFailed();
    }

    /** Round 41: the ground allies still standing (the debrief's {@code DOMES STANDING n / 5}); 0 without them. */
    public int alliesStanding() {
        return groundAllies() ? convoy.standing() : 0;
    }

    /** M5 part E: convoy unit {@code k}'s spec (the cargo ship's, the frigate's). */
    public AllySpec allySpec(int k) {
        return convoy.spec(k);
    }

    /** M5 part E: the arena lane (1-based) naval unit {@code k} is in now; 0 for none (off the field, no lanes). */
    public int allyLane(int k) {
        return navalConvoy() ? convoy.lane(k) : 0;
    }

    /**
     * M5 part E: the naval convoy's units that can be damaged (Level 11's three cargo ships, not the
     * frigate): the tracker's pips and the debrief's {@code HULLS AFLOAT n / 3}; 0 without one.
     */
    public int damageableAllies() {
        return navalConvoy() ? convoy.saveable() : 0;
    }

    /** M5 part E: the naval convoy's damageable units still afloat in this attempt; 0 without one. */
    public int alliesAfloat() {
        return navalConvoy() ? convoy.afloat() : 0;
    }

    /** M5 part E: whether an afloat objective failed in this attempt (a unit sank). */
    public boolean afloatFailed() {
        return objectives.afloatFailed();
    }

    /** M5 part D: whether the escort objective's convoy is an air escort (Level 10's shuttles). */
    public boolean airEscort() {
        return convoy != null && convoy.air();
    }

    /**
     * M5 part D: the units the player can save, every unit but the scripted loss's (Level 10: 4 of
     * 5; the tracker's {@code SHUTTLES n / 4}); 0 without a convoy.
     */
    public int saveableAllies() {
        return convoy == null ? 0 : convoy.saveable();
    }

    /** M5 part D: the saveable units still alive in this attempt (the tracker's n); 0 without a convoy. */
    public int saveableAlliesAlive() {
        return convoy == null ? 0 : convoy.saveableAlive();
    }

    /** M5 part D: the scripted loss's unit, from 0 (Level 10's Lifeline Three: 2); -1 for none. */
    public int scriptedAlly() {
        return convoy == null ? -1 : convoy.scripted();
    }

    /** The lobs in flight, their markers on the ground. */
    public int lobCount() {
        return force.lobs().size();
    }

    public Lob lob(int index) {
        return force.lobs().get(index);
    }

    /** M7 part F: the conveyor belts (none in most levels: {@link Belts#any()}). */
    public Belts belts() {
        return belts;
    }

    /** M7 part F: the speed of the belt under the ground point at (x, y) now, px/s up the screen; 0 off every belt. */
    public double beltSpeedAt(double x, double y) {
        return belts.speedAt(x, y, groundScroll);
    }

    /** M7 part F: the furnace vents ({@link FurnaceVents#grates()} 0 in most levels). */
    public FurnaceVents vents() {
        return vents;
    }

    /** The mass-driver sleds, if the level has them. */
    public java.util.Optional<Sled> sled() {
        return sledView;
    }

    /** The destroy-targets group whose loss failed the primary objective; -1 for none. */
    public int failedGroup() {
        return failedGroup;
    }

    /** Whether the primary objective failed in this attempt: the level is lost though the ship flies on. */
    public boolean primaryFailed() {
        return failed;
    }

    /** Events of the last step. */
    public SimEvents events() {
        return events;
    }

    /** Steps since the sortie was created. */
    public long tick() {
        return tick;
    }

    /**
     * The level clock: script time since the start of this attempt, s (M5 part C: in a hold zone it
     * advances slower than real time, see {@link #scriptRate()}). Every time in a level file is on
     * this clock.
     */
    public double levelSeconds() {
        return (levelTick + (double) tickFraction / CLOCK_ONE) * SimStep.SECONDS;
    }

    /** M5 part C: the level clock, s; the same as {@link #levelSeconds()}. */
    public double scriptSeconds() {
        return levelSeconds();
    }

    /**
     * M5 part C: how fast the level clock ran in the last step, script seconds per real second: 1
     * outside a hold zone, the eased speed ÷ the section's in one (0.2 at 30 of 150), 0 while an
     * arena halts it. For drawing between steps: script time at {@code alpha} is {@link
     * #levelSeconds()} − (1 − alpha) × {@link SimStep#SECONDS} × this.
     */
    public double scriptRate() {
        return (double) clock.clockStep / CLOCK_ONE;
    }

    /**
     * M5 part C: the real clock, seconds of steps since the start of this attempt (units, bullets,
     * spawn cycles, pounces, the wingman, the radio queue and the backdrop's animation run on it).
     */
    public double realSeconds() {
        return realTick * SimStep.SECONDS;
    }

    /** M5 part C: the hold zone running now (its index in {@link ObjectiveSpec#holds()}); -1 for none. */
    public int activeHold() {
        return holdZones.activeHold;
    }

    /**
     * M5 part C: whether a hold zone runs now, from its {@link SimEvents.Type#HOLD_START} to its
     * {@link SimEvents.Type#HOLD_END} (the music's {@code full_on: hold}).
     */
    public boolean holdActive() {
        return holdZones.activeHold >= 0;
    }

    /** M5 part C: how far the scroll has eased toward a hold's speed, 0 (section speed) to 1 (the hold's). */
    public double holdEase() {
        return holdZones.holdEase();
    }

    /** M5 part C: whether the collapse has started in this attempt (its warning and on; the music's {@code full_on: collapse}). */
    public boolean collapseStarted() {
        return holdZones.collapseStarted();
    }

    /** M5 part C: real seconds since the collapse's warning started, running on after its end; -1 before it. */
    public double collapseSeconds() {
        return holdZones.collapseSeconds();
    }

    /** M5 part C: whether the collapse's warning (its tower's lean) runs, before the drop. */
    public boolean collapseWarning() {
        return holdZones.collapseWarning();
    }

    /** M5 part C: whether the collapse's tower has hit the ground (its blast rolling out, or over). */
    public boolean collapseImpacted() {
        return holdZones.collapseImpacted();
    }

    /**
     * M5 part C: the collapse's blast's kill radius round the tower's foot now, px: 0 before the
     * impact, then growing from the blast's {@code from} to its {@code to}, which it keeps once over.
     */
    public double collapseBlastRadius() {
        return holdZones.collapseBlastRadius();
    }

    /** M5 part C: the collapse's tower's footprint's centre, play-field x. */
    public double collapseFootX() {
        return holdZones.collapseFootX();
    }

    /** M5 part C: the collapse's tower's footprint's centre now, play-field y (up), scrolling with the ground. */
    public double collapseFootY() {
        return holdZones.collapseFootY();
    }

    /** M5 part C: the top of the collapse's band now, play-field y (up), scrolling with the ground. */
    public double collapseBandTop() {
        return holdZones.collapseBandTop();
    }

    /** M5 part C: the bottom of the collapse's band now, play-field y (up), scrolling with the ground. */
    public double collapseBandBottom() {
        return holdZones.collapseBandBottom();
    }

    /** 1 for the first attempt, counting up with every restart after the ship was destroyed. */
    public int attempt() {
        return attempt;
    }

    /** Distance the ground layer has scrolled in this attempt, in pixels. */
    public double groundScroll() {
        return groundScroll;
    }

    /** The 1-based number of the section the scroll is in; the last one after the end. */
    public int section() {
        double t = levelSeconds();
        List<Section> sections = script.sections();
        for (int i = 0; i < sections.size(); i++) {
            if (t < sections.get(i).end()) {
                return i + 1;
            }
        }
        return sections.size();
    }

    /**
     * The ground layer's current scroll speed in px/s; step SIM-K: in a crawling arena the crawl's speed while the clock
     * is held, eased to 0 while the breaching boss is out of the sand ({@link #crawlFactor()}).
     */
    public double groundSpeed() {
        return clock.groundSpeed();
    }

    /** Whether the non-playable launch is still running. */
    public boolean launching() {
        return levelTick <= launchTicks;
    }

    /**
     * M8 part A (design/world/europa, Under water rules): whether the ship flies under water now (the level's or its
     * section's medium; the layers the torpedoes reach, the hydro-kit's look, the HUD).
     */
    public boolean underwater() {
        return medium.underwater();
    }

    /**
     * M8 part A: the water's share of the factors now, 0 in the air to 1 under water (eased over the rules' ease at a
     * crossing: the render grade and the sound can follow it).
     */
    public double waterBlend() {
        return medium.blend();
    }

    /** Whether the ship is flying, rather than wrecked and waiting for a retry. */
    public boolean flying() {
        return !wrecked;
    }

    /** Whether the scroll has reached the end: the level is won. */
    public boolean complete() {
        return complete;
    }

    /** The edges showing an edge warning, as {@link WarningEdge} bits. */
    public int edgeWarnings() {
        return edgeWarnings;
    }

    public long score() {
        return tally.score();
    }

    /** Credits earned in this attempt. */
    public int credits() {
        return tally.credits();
    }

    public int chain() {
        return tally.chain();
    }

    public double chainMultiplier() {
        return tally.multiplier();
    }

    /** The share of the chain window left, 1 right after a kill. */
    public double chainWindow() {
        return tally.window();
    }

    public int kills() {
        return tally.kills();
    }

    /**
     * Every enemy the level sends, its set pieces among them (its bosses only when the secondary counts them, {@link
     * Secondary#countsBosses()}) and the boss streams' units let in so far.
     */
    public int enemyTotal() {
        // M7 part G: less the pursuits' refill motes left out (planned, they never entered).
        // Step SIM-K: its bosses left out when the secondary does not count them (Level 21's Colossus).
        return force.units() - force.skipped() + countedPieces + streamReleased;
    }

    /** Kills needed for the secondary objective. */
    public int requiredKills() {
        return objectives.requiredKills();
    }

    public boolean secondaryMet() {
        return objectives.secondaryMet();
    }

    /**
     * Whether the secondary objective counts units (parts) destroyed of all: none of an enemy gets
     * through (Level 03's Spore Bombers), every unit of some enemies (Level 05), or every one of a
     * boss's parts shot off in time (Level 07's bay sacs).
     */
    public boolean secondaryByEscapes() {
        return script.objectives().secondary().byEscapes();
    }

    /** The units of an escapes objective's enemy (the parts of a parts objective) destroyed so far in this attempt. */
    public int escapesDestroyed() {
        return objectives.escapesDestroyed();
    }

    /** The units of an escapes objective's enemy the level sends (the parts of a parts objective). */
    public int escapesTotal() {
        return objectives.escapesTotal();
    }

    /** M7 part F: the units of an escapes objective's enemy that got away in this attempt (with escapes allowed). */
    public int escapesLost() {
        return objectives.escapesLost();
    }

    /** Whether a unit of an escapes objective's enemy got away: the objective failed for this attempt. */
    public boolean secondaryFailed() {
        return objectives.escapesFailed();
    }

    /** M5 part F: the level's wind and lightning (its push, the gust telegraph, the next flash). */
    public Wind wind() {
        return wind;
    }

    /**
     * M7 part E (design/enemies/ground/dust-devil, Pull): the Dust Devils' pull on the ship in this step, px/s to
     * the right; 0 without one (and while the ship does not fly).
     */
    public double pullX() {
        return force.pullX();
    }

    /** M7 part E: the Dust Devils' pull on the ship in this step, px/s up the screen; 0 without one. */
    public double pullY() {
        return force.pullY();
    }

    /** M5 part F: the wind's push this step, px/s to the right; 0 without weather. */
    public double windPush() {
        return wind.push(levelTick);
    }

    /** M5 part F: the weather sondes (none in most levels). */
    public Sondes sondes() {
        return sondes;
    }

    /** M5 part F: the Lampreys latched onto the ship after the last step. */
    public int latched() {
        return latches.count();
    }

    /**
     * M5 part F: whether something drains the shield: the HUD's {@code SHIELD DRAIN} and the bar's flicker
     * (M7 part A: a latched unit the EMP Burst stuns does not).
     */
    public boolean draining() {
        return latches.draining();
    }

    /**
     * M5 part G: whether the level has a defend primary (Level 13's Nansen Relay): {@link #ally(int)} 0 is
     * its ally ({@link #allySpec(int)} its spec, {@link Ally#hpShare()} its integrity), {@link
     * #primaryFailed()} its loss.
     */
    public boolean defending() {
        return convoy != null && convoy.defend();
    }

    /**
     * M5 part G: the defend's synchronisation's seconds left on the level clock (the HUD's countdown): its
     * duration until its start, then down to 0 at its end; 0 without a defend primary.
     */
    public double defendSecondsLeft() {
        if (!defending()) {
            return 0;
        }
        ObjectiveSpec.Defend defend = convoy.defendSpec();
        return Math.clamp(defend.end() - levelSeconds(), 0, defend.duration());
    }

    /** M5 part G: whether the defend's synchronisation has started (the level clock is past its start). */
    public boolean defendStarted() {
        return defending() && levelSeconds() >= convoy.defendSpec().start();
    }

    /** M5 part G: whether the defend's synchronisation completed in this attempt ({@link SimEvents.Type#DEFEND_COMPLETE}). */
    public boolean defendComplete() {
        return defending() && convoy.defended();
    }

    /** M5 part G: whether an integrity secondary failed in this attempt (the ally dropped below its share). */
    public boolean integrityFailed() {
        return objectives.integrityFailed();
    }

    /** M5 part G: whether the scroll is in a timed halt now (Level 13's defend phase), its eases not counted. */
    public boolean timedHalted() {
        return clock.timedHalts && clock.scrollSpeed() == 0;
    }

    /**
     * M5 part H: the ground units in group {@code g} of a groups objective (Level 14's {@code ROOTS}: its
     * eight root-burst growths, easy four), counted from the level's script: the tracker's {@code n / 8}.
     */
    public int groupUnits(int g) {
        return objectives.groupUnits(g);
    }

    /** M5 part H: the units of group {@code g} destroyed in this attempt: the tracker's {@code n}. */
    public int groupUnitsDestroyed(int g) {
        return objectives.groupDestroyed(g);
    }

    /** M5 part H: the level's root bursts (design/campaign Level 14, Hazards), in the level's order; 0 without. */
    public int burstCount() {
        return burstSlots.length;
    }

    /** M5 part H: root burst {@code k}'s {@link RootBurst#index()} (its events' value). */
    public int burstIndex(int k) {
        return force.burstOf(burstSlots[k]).index();
    }

    /** M5 part H: whether root burst {@code k}'s telegraph runs now (the cracking paving and the dust), until its growths appear. */
    public boolean burstTelegraphing(int k) {
        return force.burstTelegraphed(burstSlots[k]) && !force.burstGrown(burstSlots[k]);
    }

    /** M5 part H: whether root burst {@code k}'s growths have appeared in this attempt (its crater stays on the ground). */
    public boolean burstGrown(int k) {
        return force.burstGrown(burstSlots[k]);
    }

    /**
     * M5 part H: how far root burst {@code k}'s telegraph is, 0 at its start to 1 as its growths appear
     * (on the level clock); 0 before it, 1 after.
     */
    public double burstTelegraphShare(int k) {
        RootBurst burst = force.burstOf(burstSlots[k]);
        if (!force.burstTelegraphed(burstSlots[k])) {
            return 0;
        }
        if (force.burstGrown(burstSlots[k]) || !(burst.telegraphSeconds() > 0)) {
            return 1;
        }
        return Math.clamp((levelSeconds() - burst.t()) / burst.telegraphSeconds(), 0.0, 1.0);
    }

    /** M5 part H: root burst {@code k}'s centre, px from the left (after its move away from the ship once its telegraph started). */
    public double burstX(int k) {
        return force.burstTelegraphed(burstSlots[k])
                ? force.burstX(burstSlots[k])
                : force.burstOf(burstSlots[k]).x();
    }

    /** M5 part H: root burst {@code k}'s point now, px up from the bottom edge: it scrolls with the ground from its telegraph on. */
    public double burstY(int k) {
        return force.burstTelegraphed(burstSlots[k])
                ? force.burstBase(burstSlots[k]) - groundScroll
                : PlayField.HEIGHT - force.burstOf(burstSlots[k]).depth();
    }

    /** M5 part H: the units the arena boss launched as ambushes alive now (the Siege Spire's maw's Wraiths, at most 4). */
    public int launchedAlive() {
        return force.launchedAlive();
    }
}
