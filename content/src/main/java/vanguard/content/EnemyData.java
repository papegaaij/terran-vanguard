package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * design/enemies/&lt;category&gt;/&lt;slug&gt;/data.yaml: an enemy's stat block at medium
 * (design/enemies/README.md, Stat block template).
 *
 * <p>Its nested records are declared by family in the sealed interfaces it implements ({@link EnemyMovementData},
 * {@link EnemyAttackData}, {@link EnemyBossData}, {@link EnemyHookData}, and the arena bosses' {@link ColossusBlock}
 * and {@link MawBlock}), so they are still named {@code EnemyData.Hook} and so on; only {@link Intel} is declared here.
 *
 * @param hp hit points in damage units
 * @param speed the default speed in px/s; movement patterns may set their own
 * @param bounty credits at medium in Act 1 terms
 * @param firstLevel the level it is introduced in
 * @param cloak M5 part D: its cloak (the Wraith): its {@code layer} is the cloaked one
 * @param submerge M5 part E: how it surfaces and submerges (the Driftjelly), a unit on {@code ground}
 * @param latch M5 part F: it latches onto the ship's hull and drains the shield (the Lamprey; with its
 *     {@code chase} movement and its {@code drain} attack)
 * @param machine M7 part A: it is a machine, which the EMP Burst stuns longest (design/player/specials,
 *     EMP Burst); false (biomechanical, Vrell) when not given
 * @param roll M7 part D: its curl-roll (the Shellback): once, badly hurt, it curls into an armoured ball, rolls along
 *     its walk path and uncurls with its belly exposed
 * @param vortex M7 part E: its spin cycle (the Dust Devil): closed on {@code ground} with armoured vanes, it rises
 *     and opens on {@code low-air} once a cycle, and wanders round its wave's path
 * @param pull M7 part E: it pulls the ship and the loose pickups toward its centre (the Dust Devil)
 * @param intel M7 part E: what the hangar intel shows of it (the Ghost Drone's "unclassified" silhouette)
 * @param turret M7 part F: its turret turns on its own, toward the ship, and fires its burst along its aim (the Warden
 *     Tank)
 * @param rearPlate M7 part F: its thin rear plate (the Warden Tank): direct shots from behind do more to its body
 * @param pursuit M7 part G: its {@code pursuit} formation's defaults (the Mote Swarm in Level 20): a flock's band behind
 *     the ship, its lunges, its refills and its break-off
 * @param seekersIgnore M7 part G (user decision 2026-10-20, the Mote Swarm): {@code seekers_ignore: true}: the seekers
 *     (the player's homing shots and turrets, Rook) never pick it as a target; false when not given
 * @param underwater M8 part C (design/enemies/naval, Returning units under water): how the unit differs in a wave or a
 *     nest that enters under water (a section with {@code medium: underwater}); none if it is the same ({@link
 *     #inWater})
 * @param shield M9 part A (design/enemies, Enemy shields): its hex shield's HP at medium (the difficulty's HP lever and
 *     a returning unit's act factor on it, never the beam era's); none without one
 * @param neutral M9 part B (design/campaign Level 29, Hold fire): a neutral craft ({@link #isNeutral})
 * @param leader M9 part B (design/enemies/air/talon): the airframe its wave's leader flies ({@link Leader})
 * @param livery M9 part G (design/enemies/ground/warden-tank, Act 5: the return in livery): the look and the intel's
 *     faction it flies with from an act on ({@link Livery}); the simulation never sees it
 * @param mirror M10 part A (design/enemies, Reflect): its mirror over a frontal arc ({@link Mirror}); none without
 * @param regeneration M10 part A (design/enemies, Regeneration): its HP's refill after a quiet spell ({@link
 *     Regeneration}); none without
 * @param regrowth M10 part A (design/enemies/ground/graft-turret, Regrowth): its turret's regrowth over an exposed root
 *     ({@link Regrowth}); none without
 * @param dome M10 part A (design/enemies/ground/shield-pylon, The dome): the dome it projects over the ground units near
 *     it ({@link Dome}); none without
 * @param spoofable M10 part A (design/player/systems, the IFF spoofer): {@code spoofable: true}, its aimed volleys and
 *     told attacks come late while the ship carries the spoofer; false when not given
 * @param counted M10 part B (design/campaign Level 36, the harvesters): {@code counted: false}, a hostile craft that is
 *     not counted as an enemy (density, the floor, kill ratios) but picked, hit and paid like one ({@link #isCounted});
 *     true when not given
 * @param haul M10 part B (Level 36, the harvesters' 30 credits "with the ground targets' share"): the credit source its
 *     bounty is paid from, {@code ground-targets} or {@code kills}; the built rule when not given (a ground unit's as a
 *     ground target's, the others' as kills)
 */
public record EnemyData(
        String name,
        String faction,
        String layer,
        Tier tier,
        Size size,
        Size hitbox,
        String parts,
        Optional<List<PartData>> partList,
        Orientation orientation,
        double hp,
        Armour armour,
        double speed,
        Movement movement,
        List<Attack> attacks,
        List<FormationUse> formations,
        List<WeakPoint> weakPoints,
        List<Drop> drops,
        List<String> traits,
        int bounty,
        int firstLevel,
        Optional<Hooks> difficulty,
        Optional<List<ChainData>> chains,
        Optional<BossData> boss,
        Optional<SegmentChain> segmentChain,
        Optional<Cloak> cloak,
        Optional<Submerge> submerge,
        Optional<Latch> latch,
        Optional<Boolean> machine,
        Optional<Roll> roll,
        Optional<Vortex> vortex,
        Optional<Pull> pull,
        Optional<Intel> intel,
        Optional<Turret> turret,
        Optional<RearPlate> rearPlate,
        Optional<Pursuit> pursuit,
        Optional<Boolean> seekersIgnore,
        Optional<Underwater> underwater,
        Optional<Double> shield,
        Optional<Boolean> neutral,
        Optional<Leader> leader,
        Optional<Livery> livery,
        Optional<Mirror> mirror,
        Optional<Regeneration> regeneration,
        Optional<Regrowth> regrowth,
        Optional<Dome> dome,
        Optional<Boolean> spoofable,
        Optional<Boolean> counted,
        Optional<String> haul)
        implements EnemyMovementData,
                EnemyAttackData,
                EnemyBossData,
                EnemyHookData,
                ColossusBlock,
                MawBlock,
                RingBlock {
    public EnemyData {
        // M9 part H: a ringed boss's batteries are parts too, appended in ring order (RingBlock).
        partList = RingBlock.withBatteries(partList, boss);
        Layers.of(layer);
        Check.positive("hp", hp);
        Check.notNegative("speed", speed);
        Check.notNegative("bounty", bounty);
        Check.positive("first_level", firstLevel);
        shield.ifPresent(points -> Check.positive("shield", points));
        // M10 part A (design/enemies, Reflect): a unit carries a mirror or a hex shield, never both.
        Check.that(mirror.isEmpty() || shield.isEmpty(), "a unit carries a mirror or a hex shield, never both");
        Check.that(regrowth.isEmpty() || dome.isEmpty(), "a pylon (dome) does not regrow");
        haul.ifPresent(source ->
                Check.that(HAUL_SOURCES.contains(source), "haul: ground-targets or kills, was '" + source + "'"));
        // M10 part B: an uncounted craft is a hostile one; a neutral craft is never counted anyway.
        Check.that(counted.orElse(true) || !neutral.orElse(false), "a neutral craft needs no counted: false");
    }

    /** M10 part B: the {@code haul} sources a bounty may be paid from. */
    static final List<String> HAUL_SOURCES = List.of("ground-targets", "kills");

    /**
     * M10 part B (design/campaign Level 36, the harvesters): whether it counts as an enemy ({@code counted}, true when not
     * given; a neutral craft never does, {@link #isNeutral}).
     */
    public boolean isCounted() {
        return counted.orElse(true);
    }

    /** M10 part A (design/player/systems, the IFF spoofer): whether the spoofer fools it ({@code spoofable: true}). */
    public boolean isSpoofable() {
        return spoofable.orElse(false);
    }

    /**
     * M9 part G (design/enemies/ground/warden-tank, Act 5: the return in livery; m9g-gaps.md §1 #2): from act {@code
     * from_act} on, the unit flies the {@code sprite} set (the game's looks pick its frames: the Warden Tank's {@code
     * warden-tank-asc}) and the intel names it of {@code faction} ({@code Jovian Ascendancy}); its stat block is
     * unchanged (no new slug) and the simulation never sees a look.
     */
    public record Livery(int fromAct, String sprite, String faction) {
        public Livery {
            Check.that(fromAct >= 2 && fromAct <= 7, "livery.from_act: an act from 2 to 7, was " + fromAct);
            Check.that(
                    sprite.matches("[a-z0-9]+(-[a-z0-9]+)*"),
                    "livery.sprite: a kebab-case sprite set, was '" + sprite + "'");
            Check.that(!faction.isBlank(), "livery.faction: the faction the intel names");
        }
    }

    /**
     * M9 part G: the sprite set it flies with in act {@code act}: its livery's from that act on; empty for its own.
     */
    public Optional<String> liverySprite(int act) {
        return livery.filter(l -> act >= l.fromAct()).map(Livery::sprite);
    }

    /** M9 part G: the faction the intel names it of in act {@code act}: its livery's from that act on, else its own. */
    public String factionIn(int act) {
        return livery.filter(l -> act >= l.fromAct()).map(Livery::faction).orElse(faction);
    }

    /**
     * M9 part B (design/enemies/air/talon, Squad leader): the airframe a wave's leader flies ({@code leader: tip} on a
     * V-wing): its HP, hex shield and bounty at medium, the unit's own otherwise.
     */
    public record Leader(double hp, Optional<Double> shield, int bounty) {
        public Leader {
            Check.positive("leader.hp", hp);
            shield.ifPresent(points -> Check.positive("leader.shield", points));
            Check.notNegative("leader.bounty", bounty);
        }
    }

    /**
     * M9 part B (design/enemies/air/talon, Squad leader): the stat block of its wave's leader: its {@link Leader}'s HP,
     * shield and bounty, everything else its own.
     */
    public EnemyData asLeader() {
        Leader lead = leader.orElseThrow(() -> new IllegalStateException(name + " has no leader"));
        return new EnemyData(
                name,
                faction,
                layer,
                tier,
                size,
                hitbox,
                parts,
                partList,
                orientation,
                lead.hp(),
                armour,
                speed,
                movement,
                attacks,
                formations,
                weakPoints,
                drops,
                traits,
                lead.bounty(),
                firstLevel,
                difficulty,
                chains,
                boss,
                segmentChain,
                cloak,
                submerge,
                latch,
                machine,
                roll,
                vortex,
                pull,
                intel,
                turret,
                rearPlate,
                pursuit,
                seekersIgnore,
                underwater,
                lead.shield().or(() -> shield),
                neutral,
                leader,
                livery,
                mirror,
                regeneration,
                regrowth,
                dome,
                spoofable,
                counted,
                haul);
    }

    /**
     * M9 part B (design/campaign Level 29, Hold fire): whether it is a neutral craft ({@code neutral: true}): it never
     * fires or rams, never counts as an enemy and the seekers never pick it; false when not given.
     */
    public boolean isNeutral() {
        return neutral.orElse(false);
    }

    /**
     * M7 part E, what the hangar intel shows (design/enemies/air/ghost-drone, round 36 Q.22h): with {@code
     * unclassified_until_flown} the intel shows an "unclassified" silhouette of its layer and size instead of its
     * portrait and name until its first level has been flown once.
     */
    public record Intel(boolean unclassifiedUntilFlown) {}

    /** M7 part F: whether it is a walking boss (movement {@code stride}: the Revenant Walker). */
    public boolean walks() {
        return movement.stride().isPresent();
    }

    /**
     * Whether it is an arena boss, fought in a halted arena: anchored (M5 part E, the Harbour Kraken, the Siege Spire)
     * or walking its passes there (M7 part F, the Revenant Walker).
     */
    public boolean arenaBoss() {
        return movement.anchored().isPresent() || walks() || breaches();
    }

    /** M7 part H: whether it is a breaching boss (movement {@code breach}: the Dust Colossus). */
    public boolean breaches() {
        return movement.breach().isPresent();
    }

    /** M7 part H: its breach cycle on {@code difficulty}: the stat block's with the difficulty's bite tell. */
    public Optional<Breach> breach(Difficulty difficulty) {
        return movement.breach().map(cycle -> cycle.with(hook(difficulty).flatMap(Hook::breach)));
    }

    /** M7 part H: its suction on {@code difficulty}: the boss script's with the difficulty's speed. */
    public Optional<Suction> suction(Difficulty difficulty) {
        return boss.flatMap(BossData::suction).map(s -> s.with(hook(difficulty).flatMap(Hook::suction)));
    }

    /**
     * M8 part H: its coil on {@code difficulty} (a coiled boss: the Abyssal Maw): the boss script's with the difficulty's
     * {@code coil_arena} changes (the sweeps' tell, the lunge's shadow, the implants' open time, the shed units'
     * segments, the fan walls' gaps).
     */
    public Optional<CoilData> coilArena(Difficulty difficulty) {
        Optional<CoilArenaChange> change = hook(difficulty).flatMap(Hook::coilArena);
        return boss.flatMap(BossData::coil)
                .map(coil -> new CoilData(
                        coil.intro(),
                        coil.wind(),
                        coil.body(),
                        coil.sacs(),
                        coil.jaw(),
                        coil.wall().map(w -> w.with(change.flatMap(CoilArenaChange::wallGaps))),
                        coil.sweeps().with(change.flatMap(CoilArenaChange::sweepTell)),
                        coil.dive().map(d -> d.with(change.flatMap(CoilArenaChange::shadow))),
                        coil.implants().map(i -> i.with(change.flatMap(CoilArenaChange::implantOpen))),
                        coil.shed().map(shed -> shed.with(change.flatMap(CoilArenaChange::shedSegments)))));
    }

    /** M8 part H: whether it is a coiled boss ({@code boss.coil}: the Abyssal Maw). */
    public boolean coils() {
        return boss.flatMap(BossData::coil).isPresent();
    }

    /** M9 part H: whether it is a ringed boss ({@code boss.rings}: the Iron Sovereign). */
    public boolean ringed() {
        return boss.flatMap(BossData::rings).isPresent();
    }

    /** M9 part H: its ring changes on {@code difficulty} ({@code ring_arena}); empty without. */
    public Optional<RingArenaChange> ringArena(Difficulty difficulty) {
        return hook(difficulty).flatMap(Hook::ringArena);
    }

    /**
     * M9 part H: this stat block with {@code resolved} attacks instead (the loader's: a {@code from} attack taken from
     * its ground unit, {@link RingRules#resolve}).
     */
    public EnemyData withAttacks(List<Attack> resolved) {
        return new EnemyData(
                name,
                faction,
                layer,
                tier,
                size,
                hitbox,
                parts,
                partList,
                orientation,
                hp,
                armour,
                speed,
                movement,
                resolved,
                formations,
                weakPoints,
                drops,
                traits,
                bounty,
                firstLevel,
                difficulty,
                chains,
                boss,
                segmentChain,
                cloak,
                submerge,
                latch,
                machine,
                roll,
                vortex,
                pull,
                intel,
                turret,
                rearPlate,
                pursuit,
                seekersIgnore,
                underwater,
                shield,
                neutral,
                leader,
                livery,
                mirror,
                regeneration,
                regrowth,
                dome,
                spoofable,
                counted,
                haul);
    }

    /** M7 part H: its eruption on {@code difficulty}: the boss script's with the difficulty's marker time. */
    public Optional<Eruption> eruption(Difficulty difficulty) {
        return boss.flatMap(BossData::eruption).map(e -> e.with(hook(difficulty).flatMap(Hook::eruption)));
    }

    /** M7 part F: its turret on {@code difficulty}: the stat block's with the difficulty's turn rate. */
    public Optional<Turret> turret(Difficulty difficulty) {
        Optional<Double> rate = hook(difficulty).flatMap(Hook::turret).flatMap(TurretChange::turnRate);
        return turret.map(
                t -> rate.map(r -> new Turret(r, t.lock(), t.within())).orElse(t));
    }

    /** M7 part E: whether the intel keeps it unclassified until its first level has been flown. */
    public boolean unclassifiedUntilFlown() {
        return intel.map(Intel::unclassifiedUntilFlown).orElse(false);
    }

    /** M7 part E: its spin cycle on {@code difficulty}: the stat block's with the difficulty's open window. */
    public Optional<Vortex> vortex(Difficulty difficulty) {
        Optional<Double> open = hook(difficulty).flatMap(Hook::vortex).flatMap(VortexChange::open);
        return vortex.map(cycle -> open.map(cycle::withOpen).orElse(cycle));
    }

    /** M7 part E: its pull on {@code difficulty}: the stat block's with the difficulty's speed on the ship. */
    public Optional<Pull> pull(Difficulty difficulty) {
        Optional<Double> speed = hook(difficulty).flatMap(Hook::pull).flatMap(PullChange::shipSpeed);
        return pull.map(p -> speed.map(p::withShipSpeed).orElse(p));
    }

    /** The difficulty's hook: easy's, hard's, none at medium. */
    private Optional<Hook> hook(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> difficulty().flatMap(Hooks::easy);
            case HARD -> difficulty().flatMap(Hooks::hard);
            case MEDIUM -> Optional.empty();
        };
    }

    /** M7 part D: its {@code aura} attack's aura (the Choir Herald); none without one. */
    public Optional<Aura> aura() {
        return attacks.stream().flatMap(attack -> attack.aura().stream()).findFirst();
    }

    /** M7 part A: whether its stat block marks it a machine ({@code machine: true}); a Vrell unit is not. */
    public boolean isMachine() {
        return machine.orElse(false);
    }

    /** M7 part G: whether the seekers ignore it ({@code seekers_ignore: true}); every seeker may pick it when not given. */
    public boolean ignoredBySeekers() {
        return seekersIgnore.orElse(false);
    }

    /**
     * M8 part C (design/enemies/naval, Returning units under water): the unit as it flies in a wave or a nest that
     * enters under water: its {@code underwater} block's layer, no surface/submerge swap with {@code submerge: false},
     * its terrain drift instead; itself without the block. The look is the game's ({@code underwater.look}).
     */
    public EnemyData inWater() {
        if (underwater.isEmpty()) {
            return this;
        }
        Underwater water = underwater.get();
        Movement moved = water.drift()
                .map(drift -> new Movement(
                        movement.snake(),
                        movement.swoop(),
                        movement.straight(),
                        movement.hover(),
                        movement.orbit(),
                        movement.dive(),
                        // a drift of 0: none (it scrolls with the ground)
                        Optional.of(new Terrain(drift > 0 ? Optional.of(drift) : Optional.empty())),
                        movement.strafe(),
                        movement.spiralOut(),
                        movement.drift(),
                        movement.sine(),
                        movement.walk(),
                        movement.path(),
                        movement.ambush(),
                        movement.flock(),
                        movement.anchored(),
                        movement.chase(),
                        movement.burrow(),
                        movement.crawl(),
                        movement.laneStrafe(),
                        movement.stride(),
                        movement.breach(),
                        movement.rise(),
                        movement.coil(),
                        movement.lunge(),
                        movement.swoopPath(),
                        movement.ride(),
                        movement.scatter(),
                        movement.lay(),
                        movement.dash()))
                .orElse(movement);
        return new EnemyData(
                name,
                faction,
                water.layer().orElse(layer),
                tier,
                size,
                hitbox,
                parts,
                partList,
                orientation,
                hp,
                armour,
                speed,
                moved,
                attacks,
                formations,
                weakPoints,
                drops,
                traits,
                bounty,
                firstLevel,
                difficulty,
                chains,
                boss,
                segmentChain,
                cloak,
                water.submerge().filter(keep -> !keep).isPresent() ? Optional.empty() : submerge,
                latch,
                machine,
                roll,
                vortex,
                pull,
                intel,
                turret,
                rearPlate,
                pursuit,
                seekersIgnore,
                Optional.empty(),
                shield,
                neutral,
                leader,
                livery,
                mirror,
                regeneration,
                regrowth,
                dome,
                spoofable,
                counted,
                haul);
    }

    /** M8 part F: its lunge on {@code difficulty}: the stat block's with the difficulty's trigger distance. */
    public Optional<Lunge> lunge(Difficulty difficulty) {
        Optional<Double> trigger = hook(difficulty).flatMap(Hook::lunge).flatMap(LungeChange::trigger);
        return movement.lunge().map(lunge -> trigger.map(lunge::withTrigger).orElse(lunge));
    }

    /** M8 part F: its lunge's ring (the {@code ring} attack of a lunging unit, the Glow Angler's); none without one. */
    public Optional<Attack> lungeRing() {
        if (movement.lunge().isEmpty()) {
            return Optional.empty();
        }
        return attacks.stream()
                .filter(attack -> attack.pattern().equals("ring"))
                .findFirst();
    }

    /** M8 part E: its coiling orbit on {@code difficulty}: the stat block's with the difficulty's open window. */
    public Optional<Coil> coil(Difficulty difficulty) {
        Optional<Double> open = hook(difficulty).flatMap(Hook::coil).flatMap(CoilChange::open);
        return movement.coil().map(cycle -> open.map(cycle::withOpen).orElse(cycle));
    }

    /** M8 part E: its slowing ring's attack (the Siren's song); none without one. */
    public Optional<Attack> slowRing() {
        return attacks.stream().filter(attack -> attack.slow().isPresent()).findFirst();
    }

    /** M8 part E: its slowing ring on {@code difficulty}: the attack's slow with the difficulty's change. */
    public Optional<Slow> slow(Difficulty difficulty) {
        Optional<SlowChange> change = hook(difficulty).flatMap(Hook::slow);
        return slowRing().flatMap(Attack::slow).map(slow -> change.map(slow::with)
                .orElse(slow));
    }

    /** The attack named {@code name} (a part's {@code attack}). */
    public Optional<Attack> attack(String name) {
        return attacks.stream()
                .filter(attack -> attack.name().filter(name::equals).isPresent())
                .findFirst();
    }

    /** The part named {@code name}'s index in the part list; -1 without one. */
    public int partIndex(String name) {
        List<PartData> parts = partList.orElse(List.of());
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Whether it is a multi-part unit ({@code parts: multi} with a {@code part_list}). */
    public boolean multiPart() {
        return parts.equals("multi");
    }
}
