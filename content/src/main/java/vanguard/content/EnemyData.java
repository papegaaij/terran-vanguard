package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * design/enemies/&lt;category&gt;/&lt;slug&gt;/data.yaml: an enemy's stat block at medium
 * (design/enemies/README.md, Stat block template).
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
        Optional<Underwater> underwater)
        implements ColossusBlock, MawBlock {
    public EnemyData {
        Layers.of(layer);
        Check.positive("hp", hp);
        Check.notNegative("speed", speed);
        Check.notNegative("bounty", bounty);
        Check.positive("first_level", firstLevel);
    }

    /**
     * M7 part D, the curl-roll (design/enemies/ground/shellback): once, when its body (its vital part, else the unit)
     * falls to {@code below} of its HP, it curls over {@code curl} s (already armoured), rolls {@code seconds} s along
     * its walk path at {@code speed} px/s as a ball of {@code ball} px (its hit box; fully armoured: shots spark off;
     * it crushes the ground objects in its path), uncurls over {@code uncurl} s and walks on; for {@code
     * belly_seconds} s after it uncurls its body takes {@code belly_factor} × the damage (the belly). No attack while
     * curled. Round 40 R.e: with {@code belly_row} (px above the bottom edge), a roll drifting down the screen ends
     * early so its belly shows at that row or higher.
     */
    public record Roll(
            double below,
            double curl,
            double seconds,
            double speed,
            double uncurl,
            double bellySeconds,
            double bellyFactor,
            Size ball,
            Optional<Double> bellyRow) {
        public Roll {
            Check.that(below > 0 && below < 1, "below: a share of its HP between 0 and 1, was " + below);
            Check.positive("curl", curl);
            Check.positive("seconds", seconds);
            Check.positive("speed", speed);
            Check.positive("uncurl", uncurl);
            Check.notNegative("belly_seconds", bellySeconds);
            Check.that(bellyFactor >= 1, "belly_factor: 1 or more, was " + bellyFactor);
            bellyRow.ifPresent(row -> Check.that(
                    row > 0 && row < PlayField.HEIGHT,
                    "belly_row: px above the bottom edge, on the play field, was " + row));
        }

        /** The px it rolls: {@code seconds × speed}. */
        public double length() {
            return seconds * speed;
        }
    }

    /**
     * M7 part E, the spin cycle (design/enemies/ground/dust-devil): a cycle of {@code closed} s closed on {@code
     * ground} (the vanes {@code armoured}: every shot glances off, a blast excepted), {@code rise} s rising (the
     * tell), {@code open} s open on {@code low-air} (the core hittable, its {@code while: open} attack firing), then
     * it sinks back at the next cycle's start; unit i of a wave runs its cycle {@code i × offset} s later. It wanders
     * at {@code wander_speed} px/s up to {@code wander_amplitude} px either side of its wave's path (seeded), scrolling
     * with the ground, never closer than {@code keep_away} px to the ship's centre.
     */
    public record Vortex(
            double closed,
            double rise,
            double open,
            double offset,
            double wanderSpeed,
            double wanderAmplitude,
            double keepAway) {
        public Vortex {
            Check.positive("closed", closed);
            Check.positive("rise", rise);
            Check.positive("open", open);
            Check.notNegative("offset", offset);
            Check.positive("wander_speed", wanderSpeed);
            Check.notNegative("wander_amplitude", wanderAmplitude);
            Check.notNegative("keep_away", keepAway);
        }

        /** One cycle, s: closed, rise and open. */
        public double cycle() {
            return closed + rise + open;
        }

        /** The cycle with another open window, s (a difficulty's {@link VortexChange}). */
        public Vortex withOpen(double seconds) {
            return new Vortex(closed, rise, seconds, offset, wanderSpeed, wanderAmplitude, keepAway);
        }
    }

    /** M7 part E: a difficulty's change to a spin cycle: its open window instead (the Dust Devil's easy 1.0 s). */
    public record VortexChange(Optional<Double> open) {
        public VortexChange {
            open.ifPresent(t -> Check.positive("open", t));
        }
    }

    /**
     * M7 part E, the radial pull (design/enemies/ground/dust-devil): the ship within {@code ship_radius} px of its
     * centre drifts toward it at {@code ship_speed} px/s at the centre, fading to 0 at the edge (never a trap against
     * the ship's speed); a loose pickup within {@code pickup_radius} px is drawn in at {@code pickup_speed} px/s (the
     * Magnet's inverse; with a fitted Magnet the stronger pull wins) and held until the unit dies (released then) or
     * leaves the screen (carried off).
     */
    public record Pull(double shipRadius, double shipSpeed, double pickupRadius, double pickupSpeed) {
        public Pull {
            Check.positive("ship_radius", shipRadius);
            Check.positive("ship_speed", shipSpeed);
            Check.positive("pickup_radius", pickupRadius);
            Check.positive("pickup_speed", pickupSpeed);
        }

        /** The pull with another speed on the ship, px/s (a difficulty's {@link PullChange}). */
        public Pull withShipSpeed(double speed) {
            return new Pull(shipRadius, speed, pickupRadius, pickupSpeed);
        }
    }

    /** M7 part E: a difficulty's change to a pull: its speed on the ship instead (the Dust Devil's easy 25, hard 45). */
    public record PullChange(Optional<Double> shipSpeed) {
        public PullChange {
            shipSpeed.ifPresent(s -> Check.positive("ship_speed", s));
        }
    }

    /**
     * M7 part E, what the hangar intel shows (design/enemies/air/ghost-drone, round 36 Q.22h): with {@code
     * unclassified_until_flown} the intel shows an "unclassified" silhouette of its layer and size instead of its
     * portrait and name until its first level has been flown once.
     */
    public record Intel(boolean unclassifiedUntilFlown) {}

    /**
     * M7 part F, an independent turret (design/enemies/ground/warden-tank): it turns at {@code turn_rate} °/s toward
     * the ship all round, independent of the hull; once its gun is ready and its aim is within {@code within}° of the
     * ship it stops for {@code lock} s (the tell) and fires its burst along its aim.
     */
    public record Turret(double turnRate, double lock, double within) {
        public Turret {
            Check.positive("turn_rate", turnRate);
            Check.notNegative("lock", lock);
            Check.notNegative("within", within);
        }
    }

    /**
     * M7 part F, a thin rear plate (design/enemies/ground/warden-tank): a direct shot arriving within {@code arc}° of
     * its rear (flying along its facing) does {@code multiplier} × its damage to the body (the Scuttler's front arc
     * reversed).
     */
    public record RearPlate(double arc, double multiplier) {
        public RearPlate {
            Check.that(arc > 0 && arc <= 180, "arc: degrees each side of the rear, 0-180, was " + arc);
            Check.positive("multiplier", multiplier);
        }
    }

    /**
     * M7 part G, the pursuit formation's defaults (design/enemies/air/mote-swarm; design/campaign Level 20, The pursuit
     * swarm): the flock's leader point holds a {@code band} {@code [top, bottom]} px below the top edge (behind the ship
     * once the scroll is reversed), following the ship's x at {@code lag} px/s; every {@code lunge_every} s a {@code
     * tell} of that many s (the flock contracts into a {@code ball} px across), then the leader dives down the ship's
     * lane at {@code dive} px/s to {@code overshoot} px below where the ship was at the tell's start (no homing) and
     * climbs back to the band at {@code climb} px/s; {@code refill} motes join from the top edge every {@code
     * refill_every} s while the flock has room; at its wave's break-off the leader climbs off the top edge at {@code
     * break_speed} px/s and the members follow, alive. A wave's {@code pursuit} block may change any of them.
     */
    public record Pursuit(
            Span band,
            double lag,
            double lungeEvery,
            double tell,
            double ball,
            double dive,
            double overshoot,
            double climb,
            int refill,
            double refillEvery,
            double breakSpeed) {
        public Pursuit {
            Check.that(band.min() >= 0 && band.max() > band.min(), "band: [top, bottom] px below the top edge");
            Check.that(band.max() < PlayField.HEIGHT, "band: inside the play field");
            Check.positive("lag", lag);
            Check.positive("lunge_every", lungeEvery);
            Check.positive("tell", tell);
            Check.that(tell < lungeEvery, "tell: shorter than the time between lunges");
            Check.positive("ball", ball);
            Check.positive("dive", dive);
            Check.notNegative("overshoot", overshoot);
            Check.positive("climb", climb);
            Check.notNegative("refill", refill);
            Check.positive("refill_every", refillEvery);
            Check.positive("break_speed", breakSpeed);
        }
    }

    /**
     * M7 part G: a difficulty's change to the pursuit formation: its time between lunges or its tell instead, s (the
     * Mote Swarm's easy 8 s and 1.2 s, hard 4.5 s).
     */
    public record PursuitChange(Optional<Double> lungeEvery, Optional<Double> tell) {
        public PursuitChange {
            lungeEvery.ifPresent(s -> Check.positive("lunge_every", s));
            tell.ifPresent(s -> Check.positive("tell", s));
        }
    }

    /** M7 part F: a difficulty's change to a turret: its turn rate instead, °/s (the Warden Tank's hard 160). */
    public record TurretChange(Optional<Double> turnRate) {
        public TurretChange {
            turnRate.ifPresent(r -> Check.positive("turn_rate", r));
        }
    }

    /** M7 part F: a difficulty's change to homing missiles: their turn rate instead, °/s (the walker's hard 120). */
    public record HomingChange(Optional<Double> turnRate) {
        public HomingChange {
            turnRate.ifPresent(r -> Check.positive("turn_rate", r));
        }
    }

    /**
     * M7 part F: a difficulty's change to a walking boss's flame cone: its length (px), swivel (°) or tell (s)
     * instead (the walker's hard 170 px and 35°, easy 0.8 s).
     */
    public record ConeChange(Optional<Double> length, Optional<Double> swivel, Optional<Double> tell) {
        public ConeChange {
            length.ifPresent(l -> Check.positive("length", l));
            swivel.ifPresent(w -> Check.notNegative("swivel", w));
            tell.ifPresent(t -> Check.notNegative("tell", t));
        }
    }

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

    /** M7 part D: a difficulty's change to a curl-roll: its roll time instead (the Shellback's hard 3.0 s). */
    public record RollChange(Optional<Double> seconds) {
        public RollChange {
            seconds.ifPresent(t -> Check.positive("seconds", t));
        }
    }

    /**
     * M7 part D, the {@code aura} attack (design/enemies/air/choir-herald; M8 = a): every other enemy within {@code
     * radius} px of its centre (never a boss or mid-boss, never another unit with an aura) carries a shield ring that
     * absorbs damage before its HP: at most {@code share} × that unit's HP at the level, capped at {@code cap}. A unit
     * entering the radius gets its full ring over {@code fill} s, one leaving it keeps the ring {@code keep} s; while
     * the aura's unit lives a ring untouched for {@code refill_delay} s refills at {@code refill} of its maximum a
     * second; at its death every ring it fed collapses over {@code collapse} s. Its escorts ({@code carrier +
     * escorts}) orbit it at {@code escort_radius} × the radius, so they stay inside it. The EMP Burst strips the rings.
     */
    public record Aura(
            double radius,
            double share,
            double cap,
            double refill,
            double refillDelay,
            double fill,
            double keep,
            double collapse,
            double escortRadius) {
        public Aura {
            Check.positive("radius", radius);
            Check.positive("share", share);
            Check.positive("cap", cap);
            Check.that(refill > 0 && refill <= 1, "refill: a share of the ring a second, above 0 and at most 1");
            Check.notNegative("refill_delay", refillDelay);
            Check.positive("fill", fill);
            Check.positive("keep", keep);
            Check.positive("collapse", collapse);
            Check.that(
                    escortRadius > 0 && escortRadius < 1,
                    "escort_radius: a share of the radius between 0 and 1, was " + escortRadius);
        }

        /** The ring a unit of {@code hp} carries: {@code share × hp}, at most {@code cap}. */
        public double ring(double hp) {
            return Math.min(cap, share * hp);
        }
    }

    /** M7 part D: a difficulty's change to an aura: its radius or refill instead (the Choir Herald's easy and hard). */
    public record AuraChange(Optional<Double> radius, Optional<Double> refill) {
        public AuraChange {
            radius.ifPresent(r -> Check.positive("radius", r));
            refill.ifPresent(r -> Check.that(r > 0 && r <= 1, "refill: above 0 and at most 1"));
        }
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
                        movement.lunge()))
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
                Optional.empty());
    }

    /** The movement patterns it uses, with their parameters (design/enemies/README.md, Movement pattern vocabulary). */
    /**
     * The armour: a text ({@code none}, {@code hardened}, ...), or a mapping with {@code front_arc},
     * the degrees each side of the facing from which direct shots glance off (dropped bombs, lobbed
     * shells and the specials ignore it; a walker's, Level 04). M5 part C: {@code hardened} flies for
     * an enemy (design/enemies/ground/hive-node): only anti-ground deliveries, the Airstrike and the
     * Smart Bomb damage it.
     */
    public record Armour(Optional<String> text, Optional<Double> frontArc) {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Armour {
            frontArc.ifPresent(arc -> Check.notNegative("front_arc", arc));
        }

        /** Whether it is {@code hardened}. */
        public boolean hardened() {
            return text.filter("hardened"::equals).isPresent();
        }

        /**
         * M7 part E: whether it is {@code vanes} (design/enemies/ground/dust-devil): while its spin cycle is closed
         * every shot glances off, a blast (the Smart Bomb, the Airstrike) excepted (round 36 Q.22e); open, none.
         */
        public boolean vanes() {
            return text.filter(VANES::equals).isPresent();
        }

        /** M7 part E: the closed vanes' armour. */
        public static final String VANES = "vanes";

        /**
         * M8 part E: whether it is {@code shell} (design/enemies/naval/spiral-nautilus): while its coil is closed every
         * shot glances off, the specials' blasts and {@code anti-sub} fire excepted (§5 E8); open, none.
         */
        public boolean shell() {
            return text.filter(SHELL::equals).isPresent();
        }

        /** M8 part E: the closed coil's shell. */
        public static final String SHELL = "shell";

        /** The armour written as a text. */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Armour of(String text) {
            return new Armour(Optional.of(text), Optional.empty());
        }
    }

    /**
     * @param ambush M5 part D: the path of a {@code rear ambush} wave's unit (the Wraith)
     * @param flock M5 part D: how a {@code swarm} wave's members steer (the Mote Swarm)
     * @param anchored M5 part E: an arena boss's anchored arrival (the Harbour Kraken)
     * @param chase M5 part F: it homes on the ship from its entry, then flies straight on (the Lamprey)
     * @param burrow M7 part B: it travels under the sand and surfaces near the ship (the Burrower)
     * @param crawl M7 part C: its head crawls a wave's ground path (the Threadcrawler; the wave's {@code crawl})
     * @param laneStrafe M7 part E: it drops to a lane and strafes across the screen (the Ghost Drone)
     * @param stride M7 part F: a walking boss's passes across its halted arena (the Revenant Walker)
     * @param breach M7 part H: a breaching boss's cycle under its crawling arena (the Dust Colossus)
     * @param rise M8 part D: it rises out of the deep water below the play plane, glides across and sinks away (the
     *     Abyss Ray)
     * @param coil M8 part E: it orbits the ship in a closed, armoured shell, spiralling in, then uncoils and fires (the
     *     Spiral Nautilus)
     * @param lunge M8 part F: it hangs drifting by its lure and lunges at the ship that comes close, rings and retreats
     *     (the Glow Angler)
     */
    public record Movement(
            Optional<Snake> snake,
            Optional<Swoop> swoop,
            Optional<Straight> straight,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<Dive> dive,
            Optional<Terrain> terrain,
            Optional<Strafe> strafe,
            Optional<SpiralOut> spiralOut,
            Optional<Drift> drift,
            Optional<Sine> sine,
            Optional<Walk> walk,
            Optional<PathMove> path,
            Optional<Ambush> ambush,
            Optional<Flock> flock,
            Optional<Anchored> anchored,
            Optional<Chase> chase,
            Optional<Burrow> burrow,
            Optional<Crawl> crawl,
            Optional<LaneStrafe> laneStrafe,
            Optional<Stride> stride,
            Optional<Breach> breach,
            Optional<Rise> rise,
            Optional<Coil> coil,
            Optional<Lunge> lunge) {}

    /**
     * M8 part F, the lunge from a lure (design/enemies/naval/glow-angler; m8f-gaps.md §1.1, §5 F2): it hangs drifting
     * with the water at its speed (a field's current, else its path's way); when the ship's centre comes within {@code
     * trigger} px of its centre (its lure, at most 48 px from the body, lies inside that: touching the lure needs no test
     * of its own) it dives at {@code speed} px/s at where the ship was then, stopping {@code stop_short} px short of that
     * point or after {@code seconds}; there its {@code ring} attack fires once and it retreats away from the ship at
     * {@code retreat_speed} px/s for {@code retreat_seconds}, then drifts again; it re-arms the ring attack's {@code
     * interval} after the ring. The ring is the lunge's, not a gun.
     */
    public record Lunge(
            double trigger,
            double speed,
            double seconds,
            double stopShort,
            double retreatSpeed,
            double retreatSeconds) {
        public Lunge {
            Check.notNegative("stop_short", stopShort);
            Check.that(trigger > stopShort, "trigger: beyond stop_short, was " + trigger);
            Check.positive("speed", speed);
            Check.positive("seconds", seconds);
            Check.notNegative("retreat_speed", retreatSpeed);
            Check.notNegative("retreat_seconds", retreatSeconds);
        }

        /** The lunge with another trigger distance, px (a difficulty's {@link LungeChange}). */
        public Lunge withTrigger(double px) {
            return new Lunge(px, speed, seconds, stopShort, retreatSpeed, retreatSeconds);
        }
    }

    /** M8 part F: a difficulty's change to a lunge: its trigger distance instead (the Glow Angler's easy 100, hard 160). */
    public record LungeChange(Optional<Double> trigger) {
        public LungeChange {
            trigger.ifPresent(t -> Check.positive("trigger", t));
        }
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

    /**
     * M8 part E, the coiling orbit (design/enemies/naval/spiral-nautilus; m8e-gaps.md §1.3, §5 E7–E8): closed in its
     * armoured shell it spirals in round the ship from {@code outer} to {@code inner} px over {@code closing} s at its
     * speed (never closer than {@code inner}); then it uncoils: {@code opening} s opening (still armoured, its crown's
     * tell the last {@code tell} s of it) and {@code open} s open (hittable, its spiral firing) at {@code open_speed}
     * px/s; then it closes, breaks outward and spirals in again. A wave's units orbit in turn the other way round, each
     * cycle {@code offset} s after the one before; they leave after their wave's hold.
     */
    public record Coil(
            double outer,
            double inner,
            double closing,
            double opening,
            double tell,
            double open,
            double openSpeed,
            double offset) {
        public Coil {
            Check.positive("inner", inner);
            Check.that(outer >= inner, "outer: at least inner, was " + outer);
            Check.positive("closing", closing);
            Check.notNegative("opening", opening);
            Check.that(tell >= 0 && tell <= opening, "tell: within the opening, was " + tell);
            Check.positive("open", open);
            Check.positive("open_speed", openSpeed);
            Check.notNegative("offset", offset);
        }

        /** One cycle, s: spiralling in, opening and open. */
        public double cycle() {
            return closing + opening + open;
        }

        /** The cycle with another open window, s (a difficulty's {@link CoilChange}). */
        public Coil withOpen(double seconds) {
            return new Coil(outer, inner, closing, opening, tell, seconds, openSpeed, offset);
        }
    }

    /** M8 part E: a difficulty's change to a coiling orbit: its open window instead (the Nautilus's easy 1.8, hard 1.4 s). */
    public record CoilChange(Optional<Double> open) {
        public CoilChange {
            open.ifPresent(o -> Check.positive("open", o));
        }
    }

    /** M8 part E: its coiling orbit on {@code difficulty}: the stat block's with the difficulty's open window. */
    public Optional<Coil> coil(Difficulty difficulty) {
        Optional<Double> open = hook(difficulty).flatMap(Hook::coil).flatMap(CoilChange::open);
        return movement.coil().map(cycle -> open.map(cycle::withOpen).orElse(cycle));
    }

    /**
     * M8 part E, a ring that slows (design/enemies/naval/siren; m8e-gaps.md §1.2, §5 E2, E4): a {@code ring} attack with
     * this block sends a ring of sound, not bullets: it expands from the sac at the attack's {@code speed} (not the
     * water's × 0.7) to {@code radius} px, its band {@code band} px wide behind its edge; it deals no damage. A hull it
     * reaches with a clear line from the sac is slowed to {@code factor} of its speed and acceleration for {@code
     * seconds}, then immune to slows for {@code immunity} s. The attack's {@code tell} is the sac's glow before each
     * ring, its {@code first_shot_delay} the first ring's time after the unit enters, its {@code interval} the time
     * between two rings.
     */
    public record Slow(double radius, double band, double factor, double seconds, double immunity) {
        public Slow {
            Check.positive("radius", radius);
            Check.positive("band", band);
            Check.that(factor > 0 && factor <= 1, "factor: a share of the speed above 0 and at most 1, was " + factor);
            Check.positive("seconds", seconds);
            Check.notNegative("immunity", immunity);
        }

        /** The slow with a difficulty's {@link SlowChange} applied. */
        public Slow with(SlowChange change) {
            return new Slow(
                    radius,
                    band,
                    change.factor().orElse(factor),
                    change.seconds().orElse(seconds),
                    change.immunity().orElse(immunity));
        }
    }

    /** M8 part E: a difficulty's change to a slowing ring: its factor, time or immunity instead (the Siren's). */
    public record SlowChange(Optional<Double> factor, Optional<Double> seconds, Optional<Double> immunity) {
        public SlowChange {
            factor.ifPresent(f -> Check.that(f > 0 && f <= 1, "factor: above 0 and at most 1, was " + f));
            seconds.ifPresent(t -> Check.positive("seconds", t));
            immunity.ifPresent(t -> Check.notNegative("immunity", t));
        }
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

    /**
     * M8 part D, a rise (design/enemies/naval/abyss-ray, Behaviour; gap check m8d-gaps.md §5 D2): from its entry it is a
     * shadow on {@code sub} for {@code shadow} s, growing in the ship's lane (the ship's x at its entry, clamped) {@code
     * ahead} px above the ship (the tell: only {@code anti-sub} fire and the Smart Bomb reach it, no contact); it rises
     * over {@code rise} s, its layer flipping to {@code air} at the rise's middle (no contact damage until the rise
     * ends); it glides {@code glide} s at {@code speed} px/s toward the far side, its attack firing from the rise's end;
     * then it sinks over {@code sink} s (back to {@code sub} at the sink's middle) and is gone (escaped). A {@code carrier
     * + escorts} wave's escorts stay unspawned until the rise's middle and fan out from it (gap check D3).
     */
    public record Rise(double shadow, double rise, double glide, double speed, double sink, double ahead) {
        public Rise {
            Check.positive("shadow", shadow);
            Check.positive("rise", rise);
            Check.positive("glide", glide);
            Check.positive("speed", speed);
            Check.positive("sink", sink);
            Check.notNegative("ahead", ahead);
        }

        /** The whole visit from the shadow's start to the sink's end, s. */
        public double visit() {
            return shadow + rise + glide + sink;
        }
    }

    /**
     * M7 part F, a walking boss's passes (design/enemies/bosses/revenant-walker, Passes): it crosses its halted arena
     * in passes from edge to edge, entering from the {@code edges} in turn ({@code top}, {@code bottom}, {@code left},
     * {@code right}; the first {@code top}, its intro), a vertical pass on one of {@code lanes_x} (px from the left
     * edge), a horizontal one on one of {@code lanes_y} (px below the top edge); {@code off_screen} s off the screen between
     * two passes under the next edge's warning; a turn round takes {@code turn} s; it walks at {@code speeds} px/s by
     * phase (the last for later phases). {@code beside_lane}: in its first phase it takes the lane beside the ship's;
     * {@code turn_in_place}: in its last phase it turns round on the spot at a pass's end instead of leaving.
     */
    public record Stride(
            List<Double> lanesX,
            List<Double> lanesY,
            List<String> edges,
            double offScreen,
            double turn,
            List<Double> speeds,
            boolean besideLane,
            boolean turnInPlace) {
        /** The entry edges a stride knows. */
        public static final List<String> EDGES = List.of("top", "bottom", "left", "right");

        public Stride {
            lanesX = List.copyOf(lanesX);
            lanesY = List.copyOf(lanesY);
            edges = List.copyOf(edges);
            speeds = List.copyOf(speeds);
            Check.notEmpty("lanes_x", lanesX);
            Check.notEmpty("lanes_y", lanesY);
            Check.notEmpty("edges", edges);
            Check.notEmpty("speeds", speeds);
            Check.that(edges.getFirst().equals("top"), "edges: the first pass enters from the top (its intro)");
            edges.forEach(
                    edge -> Check.that(EDGES.contains(edge), "edges: top, bottom, left or right, was '" + edge + "'"));
            lanesX.forEach(x -> Check.that(x > 0 && x < PlayField.WIDTH, "lanes_x: on the play field, was " + x));
            lanesY.forEach(y -> Check.that(y > 0 && y < PlayField.HEIGHT, "lanes_y: on the play field, was " + y));
            speeds.forEach(v -> Check.positive("speeds", v));
            Check.notNegative("off_screen", offScreen);
            Check.positive("turn", turn);
        }
    }

    /**
     * M7 part F, a walking boss's front plate (design/enemies/bosses/revenant-walker): a box of {@code hitbox} (its
     * width across the heading, its height along it) {@code ahead} px in front of {@code part}'s centre along its
     * heading; every shot meeting it glances off and it stops the Ion Beam while the part lives (no HP, not in the
     * bar).
     */
    public record Plate(String part, Size hitbox, double ahead) {
        public Plate {
            Check.notNegative("ahead", ahead);
        }
    }

    /**
     * M7 part F, a walking boss's rear flame cone (design/enemies/bosses/revenant-walker, Attacks): from its flamer
     * {@code behind} px behind its centre, {@code length} px long, {@code half_angle}° each side of its axis
     * (against its heading); it starts when the ship or Rook is behind it within {@code length + reach_behind} px,
     * at most once every {@code cooldown} s: a {@code tell} s tell (from its second phase on swivelling up to {@code
     * swivel}° toward the ship), then a {@code burn} s burn hitting for the {@code bullet} class's damage once per
     * burn; after the burn its vents (its spots {@code while: open}) open for {@code vents} s. Round 43 (K.a of round 42):
     * from its second phase on a burn keeps turning toward the ship at up to {@code track} °/s within its swivel (none
     * without it).
     */
    public record Cone(
            double length,
            double halfAngle,
            double reachBehind,
            double cooldown,
            double tell,
            double burn,
            double vents,
            double swivel,
            String bullet,
            double behind,
            Optional<Double> track) {
        public Cone {
            track.ifPresent(t -> Check.notNegative("track", t));
            Check.positive("length", length);
            Check.that(halfAngle > 0 && halfAngle < 90, "half_angle: degrees, 0-90, was " + halfAngle);
            Check.notNegative("reach_behind", reachBehind);
            Check.notNegative("cooldown", cooldown);
            Check.notNegative("tell", tell);
            Check.positive("burn", burn);
            Check.notNegative("vents", vents);
            Check.notNegative("swivel", swivel);
        }
    }

    /**
     * Round 44 (the user in round 43, W.a; design/enemies/bosses/revenant-walker, Attacks): a walking boss's
     * back-mounted rail lance. It wakes once every part of {@code after} is destroyed, or from phase {@code
     * from_phase} (a phase's name) on; then, while it walks on the screen, it charges for {@code charge} s (the line from
     * its lance, {@code behind} px behind its centre, through where the ship was) and fires along that line for {@code
     * fire} s, {@code width} px wide, hitting for the {@code bullet} class's damage once a shot; a charge every {@code
     * interval} s ({@code last_interval} in its last phase).
     */
    public record Lance(
            List<String> after,
            Optional<String> fromPhase,
            double charge,
            double fire,
            double interval,
            double lastInterval,
            double width,
            String bullet,
            double behind) {
        public Lance {
            after = List.copyOf(after);
            Check.that(!after.isEmpty() || fromPhase.isPresent(), "a rail lance wakes after parts or from a phase");
            Check.positive("charge", charge);
            Check.positive("fire", fire);
            Check.that(interval >= charge + fire, "interval: holds the charge and the shot, was " + interval);
            Check.that(
                    lastInterval >= charge + fire, "last_interval: holds the charge and the shot, was " + lastInterval);
            Check.positive("width", width);
        }
    }

    /**
     * Round 44 (the user in round 43, W.e): a walking boss's stomp shockwave: the ship or Rook within {@code reach} px of
     * its centre starts a {@code tell} s tell (at most once every {@code cooldown} s), then a ring expands to {@code
     * radius} px over {@code expand} s, hitting for the {@code bullet} class's damage once a stomp.
     */
    public record Stomp(double reach, double radius, double tell, double expand, double cooldown, String bullet) {
        public Stomp {
            Check.positive("reach", reach);
            Check.that(radius >= reach, "radius: at least its reach, was " + radius);
            Check.notNegative("tell", tell);
            Check.positive("expand", expand);
            Check.that(cooldown >= tell + expand, "cooldown: holds the tell and the ring, was " + cooldown);
        }
    }

    /**
     * Round 44: a difficulty's change to a walking boss's rail lance: its charge, interval or last phase's interval
     * instead, s.
     */
    public record LanceChange(Optional<Double> charge, Optional<Double> interval, Optional<Double> lastInterval) {
        public LanceChange {
            charge.ifPresent(c -> Check.positive("charge", c));
            interval.ifPresent(i -> Check.positive("interval", i));
            lastInterval.ifPresent(i -> Check.positive("last_interval", i));
        }
    }

    /** Round 44: a difficulty's change to a walking boss's stomp: its tell or cooldown instead, s. */
    public record StompChange(Optional<Double> tell, Optional<Double> cooldown) {
        public StompChange {
            tell.ifPresent(t -> Check.notNegative("tell", t));
            cooldown.ifPresent(c -> Check.positive("cooldown", c));
        }
    }

    /**
     * M7 part F, homing missiles (design/enemies/bosses/revenant-walker, Attacks): an aimed attack's shots fly at its
     * speed turning toward the ship at up to {@code turn_rate} °/s, fizzle after {@code life} s, have {@code hp} (every
     * shot reaching the air hits them), at most {@code most} alive at once; the firing part's hatch opens {@code
     * hatch} s before each salvo (the tell).
     */
    public record Homing(double turnRate, double life, double hp, int most, double hatch) {
        public Homing {
            Check.notNegative("turn_rate", turnRate);
            Check.positive("life", life);
            Check.positive("hp", hp);
            Check.positive("most", most);
            Check.notNegative("hatch", hatch);
        }
    }

    /**
     * M7 part E, the {@code lane_strafe} movement (design/enemies/air/ghost-drone): a front entry drops straight down
     * at {@code descent_speed} px/s to its lane ({@code lane_depth} px below the top edge, a wave's lanes {@code
     * lane_spacing} px apart), banks 90° and strafes across at {@code strafe_speed} px/s, leaving through the far
     * side edge; a line abreast splits half left, half right; a side entry (a pincer, warned at its edge) strafes in
     * at its lane. The hook {@code second_pass} (hard): a line abreast's survivors turn at the edge and strafe back
     * once.
     */
    public record LaneStrafe(double descentSpeed, double strafeSpeed, Span laneDepth, double laneSpacing) {
        public LaneStrafe {
            Check.positive("descent_speed", descentSpeed);
            Check.positive("strafe_speed", strafeSpeed);
            Check.that(
                    laneDepth.min() > 0 && laneDepth.max() < PlayField.HEIGHT,
                    "lane_depth: px below the top edge, on the play field");
            Check.positive("lane_spacing", laneSpacing);
        }
    }

    /**
     * M7 part C, the {@code crawl} movement (design/enemies/ground/threadcrawler): a ground chain's head crawls its
     * wave's ground path ({@code crawl} points in the walls' rows) at {@code speed} px/s along the ground, on top of
     * the scroll; the body follows by its segment chain.
     */
    public record Crawl(double speed) {
        public Crawl {
            Check.positive("speed", speed);
        }
    }

    /**
     * M5 part F, the {@code chase} movement (design/enemies/air/lamprey): from its entry it steers
     * toward the ship's centre at {@code speed} px/s, turning at most {@code turn_rate} °/s, for
     * {@code seconds}; then it flies straight on along its heading at its speed.
     */
    public record Chase(double speed, double turnRate, double seconds) {
        public Chase {
            Check.positive("speed", speed);
            Check.positive("turn_rate", turnRate);
            Check.positive("seconds", seconds);
        }
    }

    /**
     * M5 part F, the {@code latch} block (design/enemies/air/lamprey; user decisions F3 and F4 = a):
     * {@code slots} units may be latched at once (the hook {@code latch_slots} instead); the hull
     * {@code spots} it sits at; the {@code damage_factor} on the damage it takes while latched; the
     * {@code shake} that throws every latched unit off; the {@code stun} s and the {@code throw} px/s
     * of its way off the hull, after which it flees to the nearer side edge.
     */
    public record Latch(
            int slots,
            Spots spots,
            double damageFactor,
            Shake shake,
            double stun,

            @com.fasterxml.jackson.annotation.JsonProperty("throw")
            double throwSpeed) {
        public Latch {
            Check.positive("slots", slots);
            Check.positive("damage_factor", damageFactor);
            Check.positive("stun", stun);
            Check.notNegative("throw", throwSpeed);
        }
    }

    /**
     * M5 part F: a latch's hull spots, each {@code [dx, dy]} px from the ship's centre with y up (as the
     * weapons' muzzles): {@code left} and {@code right} for a touch from the side, {@code tail} for one
     * within 45° of straight down.
     */
    public record Spots(Point left, Point right, Point tail) {}

    /** M5 part F: {@code reversals} reversals of the commanded horizontal direction within {@code within} s. */
    public record Shake(int reversals, double within) {
        public Shake {
            Check.that(reversals >= 2, "reversals: at least 2, was " + reversals);
            Check.positive("within", within);
        }
    }

    /**
     * M5 part E, an arena boss's arrival (design/enemies/bosses/harbour-kraken): no descent and no
     * hover; its centre stops {@code y} px below the top edge when the scroll halts in its arena. It
     * lies on the ground layer that far up the scroll from the halt, scrolls in with the ground
     * (invulnerable, without its bar) and engages at the halt. Round 40 (the reworked Harbour Kraken): the
     * centre may lie above the top edge (a negative {@code y}; Platform Tiamat at -46), as long as the boss's
     * hit box reaches onto the play field.
     */
    public record Anchored(double y) {
        public Anchored {
            Check.that(
                    Double.isFinite(y) && y < vanguard.sim.PlayField.HEIGHT, "y: on or above the play field, was " + y);
        }
    }

    /**
     * M5 part E, surfacing and submerging (design/enemies/naval/driftjelly; the stated defaults of
     * 2026-10-08): every {@code every} {@code [min, max]} s, drawn per unit from its own seed on the
     * simulation's real steps, it swaps between the surface ({@code ground}) and {@code sub} over
     * {@code swap} s, its layer flipping at the swap's middle; a {@code field} wave starts the share
     * {@code start} of its units submerged.
     */
    public record Submerge(Span every, double swap, double start) {
        public Submerge {
            Check.positive("every", every.min());
            Check.positive("swap", swap);
            Check.share("start", start);
            Check.that(swap < every.min(), "swap: shorter than the least time between swaps");
        }
    }

    /**
     * M5 part D, a cloak (design/enemies/air/wraith): the stat block's {@code layer} is the cloaked
     * one; from its decloak at its hold point on it is on {@code layer}, the {@code flash} (s) of
     * its decloak starting there, its gun silent until the flash ends.
     */
    public record Cloak(String layer, double flash) {
        public Cloak {
            Layers.of(layer);
            Check.notNegative("flash", flash);
        }
    }

    /**
     * M5 part D, the path of a {@code rear ambush} wave's unit (design/enemies/air/wraith): cloaked
     * straight down its lane at the stat block's {@code speed} past the ship and off the bottom
     * edge, {@code gap} s below it, back up to its {@code hover.y} at that speed, the decloak and its
     * {@code hover.seconds} hold, then out up the nearer side lane ({@code lane} px from that edge, a
     * tie to the left) at its {@code straight.speed}.
     */
    public record Ambush(double gap, double lane) {
        public Ambush {
            Check.notNegative("gap", gap);
            Check.notNegative("lane", lane);
        }
    }

    /**
     * M5 part D, a flock (design/enemies/air/mote-swarm, user decision D8 = a): a {@code swarm}
     * wave's members keep {@code separation} px apart, steer by {@code alignment} and {@code
     * cohesion} among the members within {@code radius} px and toward the wave's leader point by
     * {@code leader} (the weights), at {@code speed} px/s ({@code dive_speed} after a loop-back),
     * turning at most {@code turn_rate} °/s; at most {@code max} members (24). M8 part C (the Glassfin): a {@code
     * sway} across the leader's route (the ribbon) and a {@code ram} (members breaking off to dive at the ship's lane).
     */
    public record Flock(
            double separation,
            double radius,
            double alignment,
            double cohesion,
            double leader,
            double speed,
            double diveSpeed,
            double turnRate,
            int max,
            Optional<Sway> sway,
            Optional<Ram> ram) {
        public Flock {
            Check.positive("separation", separation);
            Check.that(radius >= separation, "radius: the neighbourhood reaches at least the separation");
            Check.notNegative("alignment", alignment);
            Check.notNegative("cohesion", cohesion);
            Check.positive("leader", leader);
            Check.positive("speed", speed);
            Check.positive("dive_speed", diveSpeed);
            Check.positive("turn_rate", turnRate);
            Check.that(max >= 1 && max <= 24, "max: 1 to 24 members");
        }
    }

    /**
     * Part F: the head of a segment chain flies the wave's authored path at {@code speed} px/s. M8 part C (the Eel
     * Swarm's {@code snake}): with a {@code sway}, its point swings across the path (a sine about it).
     */
    public record PathMove(double speed, Optional<Sway> sway) {
        /** Without M8 part C's sway. */
        public PathMove(double speed) {
            this(speed, Optional.empty());
        }

        public PathMove {
            Check.positive("speed", speed);
        }
    }

    /**
     * M8 part C (design/enemies/naval/eel-swarm, glassfin): a sine of {@code amplitude} px across the route at {@code
     * frequency} Hz, about the authored path (the Eel Swarm's head) or a flock's leader route (the Glassfin's ribbon).
     */
    public record Sway(double amplitude, double frequency) {
        public Sway {
            Check.positive("amplitude", amplitude);
            Check.positive("frequency", frequency);
        }
    }

    /**
     * M8 part C (design/enemies/naval/glassfin, the ram): after {@code after} s on the screen each member of a flock
     * draws, from its own seed, whether it breaks off; {@code share} of a school does. A member that does glints
     * {@code tell} s (the tell), dives {@code delay} s after the glint (step C3: the README's "0.5 s after a 0.3 s
     * glint tell", the simulation's wait after the tell's end), straight down the ship's lane at {@code speed} px/s with
     * no homing, and leaves at the bottom edge (none loops back).
     */
    public record Ram(double after, double share, double tell, double delay, double speed) {
        public Ram {
            Check.notNegative("after", after);
            Check.that(share >= 0 && share <= 1, "share: 0 to 1, was " + share);
            Check.positive("tell", tell);
            Check.notNegative("delay", delay);
            Check.positive("speed", speed);
        }
    }

    /** M8 part C: a difficulty's change to a flock's ram: the share of a school that breaks off. */
    public record RamChange(Optional<Double> share) {
        public RamChange {
            share.ifPresent(v -> Check.that(v >= 0 && v <= 1, "share: 0 to 1, was " + v));
        }
    }

    /**
     * M8 part C (design/enemies/naval, Returning units under water; gap check D2): a unit's changes in a wave or a nest
     * that enters under water, applied when the level's script is built (by the entry time's section). {@code layer}
     * the layer it flies on instead (the Driftjelly's {@code air}, the play plane); {@code submerge: false} drops its
     * surface/submerge swap; {@code drift} its terrain drift instead, px/s (the Reef Spitter's 0: no raft); {@code look}
     * how the game draws it ({@code floor}: on a rock-and-kelp base instead of its raft; presentation only).
     */
    public record Underwater(
            Optional<String> layer, Optional<Boolean> submerge, Optional<Double> drift, Optional<String> look) {
        /** The Reef Spitter's base on the sea floor, instead of its raft. */
        public static final String LOOK_FLOOR = "floor";

        public Underwater {
            layer.ifPresent(Layers::of);
            submerge.ifPresent(s -> Check.that(!s, "submerge: only false (the swap dropped under water)"));
            drift.ifPresent(d -> Check.notNegative("drift", d));
            look.ifPresent(l -> Check.that(LOOK_FLOOR.equals(l), "look: only 'floor', was '" + l + "'"));
        }
    }

    /**
     * Part F: the body of a segment chain (the Coilwyrm) between its head and its tail, following
     * the head's path history: {@code segments} segments whose sprites taper from {@code size[0]}
     * to {@code size[1]} px, hit boxes {@code hitboxShare} of the sprite, {@code spacing} × the
     * segment length apart, each with {@code hp} and paying {@code bounty}; {@code contact} the
     * contact class of the segments and the tail; {@code regrow} the rear part's new head after a
     * cut (once per chain); {@code popInterval} s per segment of the chained death. M7 part C (the Threadcrawler):
     * {@code width} the segments' sprite across the path, px (their hit box {@code width × share} by {@code length
     * × share}; square if not given), and {@code cut} instead of {@code regrow}: a cut chain's rear part dies.
     */
    public record SegmentChain(
            int segments,
            List<Double> size,
            Optional<Double> width,
            double hitboxShare,
            double spacing,
            double hp,
            int bounty,
            Tier contact,
            Optional<Regrow> regrow,
            Optional<Cut> cut,
            double popInterval) {
        public SegmentChain {
            Check.that(
                    regrow.isPresent() != cut.isPresent(), "a chain's rear part regrows a head (regrow) or dies (cut)");
            width.ifPresent(w -> Check.positive("width", w));
            Check.positive("segments", segments);
            Check.that(size.size() == 2, "size is [first, last]");
            Check.positive("hitbox_share", hitboxShare);
            Check.positive("spacing", spacing);
            Check.positive("hp", hp);
            Check.notNegative("bounty", bounty);
            Check.positive("pop_interval", popInterval);
        }
    }

    /**
     * M7 part C (the Threadcrawler): a cut chain's rear part stops, writhes and dies {@code delay} s after the cut, from
     * the cut backwards at the chain's pop interval (no new head; the front part crawls on). M8 part C (the Eel Swarm):
     * or it scatters: it keeps its heading at {@code scatter} px/s for {@code seconds} s, firing nothing, then dies from
     * the cut backwards (no regrowth); a cut gives {@code delay} or {@code scatter} with {@code seconds}.
     */
    public record Cut(Optional<Double> delay, Optional<Double> scatter, Optional<Double> seconds) {
        /** M7 part C's cut: the rear part dies {@code delay} s after it. */
        public Cut(double delay) {
            this(Optional.of(delay), Optional.empty(), Optional.empty());
        }

        public Cut {
            Check.that(
                    delay.isPresent() != (scatter.isPresent() || seconds.isPresent()),
                    "a cut's rear part dies after a delay, or scatters (scatter and seconds)");
            Check.that(scatter.isPresent() == seconds.isPresent(), "a scattering cut has its scatter and its seconds");
            delay.ifPresent(d -> Check.positive("delay", d));
            scatter.ifPresent(v -> Check.positive("scatter", v));
            seconds.ifPresent(v -> Check.positive("seconds", v));
        }

        /** M8 part C: whether the cut's rear part scatters (the Eel Swarm's) instead of dying after a delay. */
        public boolean scatters() {
            return scatter.isPresent();
        }
    }

    /** A cut chain's rear part grows a new head over {@code seconds}, flying at {@code speed} px/s. */
    public record Regrow(double seconds, double speed, double hp, int bounty) {
        public Regrow {
            Check.positive("seconds", seconds);
            Check.positive("speed", speed);
            Check.positive("hp", hp);
            Check.notNegative("bounty", bounty);
        }
    }

    /**
     * Part F: a {@code laser-sweep}'s beam, {@code length} × {@code width} px from the eye, sweeping
     * {@code arc} ° over {@code duration} s after a {@code telegraph} of s. The eye is at {@code
     * origin} {@code [in, down]} px from the unit's centre: toward the field (mirrored on the right
     * edge) and down the screen; the centre when it is left out. The beam, its hit test and its
     * telegraph start there. Round 41 (a boss's sweep only, the Siege Spire's; the user's playtest 2026-10-19:
     * "you are forced to keep moving"): with {@code lead} ° the arc starts that far beside the ship's bearing on the
     * side the beam comes from (the telegraph's line) and sweeps through the ship; left out, centred on the bearing.
     */
    public record Sweep(
            double arc,
            double duration,
            double telegraph,
            double length,
            double width,
            Optional<Point> origin,
            Optional<Double> lead) {
        public Sweep {
            Check.positive("arc", arc);
            Check.positive("duration", duration);
            Check.notNegative("telegraph", telegraph);
            Check.positive("length", length);
            Check.positive("width", width);
            lead.ifPresent(degrees -> Check.that(degrees >= 0 && degrees <= arc, "lead: 0 to the arc's °"));
        }
    }

    /** Planned (part D): straight down the screen at {@code speed} px/s, with no intent. Read, not flown yet. */
    public record Drift(double speed) {
        public Drift {
            Check.positive("speed", speed);
        }
    }

    /** Planned (part D): a side-to-side offset of {@code amplitude} px, {@code period} s per swing. Read, not flown yet. */
    public record Sine(double amplitude, double period) {
        public Sine {
            Check.notNegative("amplitude", amplitude);
            Check.positive("period", period);
        }
    }

    /**
     * Planned (part D): a walker on a ground path at {@code speed} px/s, turning at {@code turn_rate}
     * °/s, {@code stride} px of ground per walk cycle. Read, not flown yet.
     */
    public record Walk(double speed, double turnRate, double stride) {
        public Walk {
            Check.positive("speed", speed);
            Check.positive("turn_rate", turnRate);
            Check.positive("stride", stride);
        }
    }

    /** A convoy unit turns across the screen at a height in {@code y}, px from the top. */
    public record Strafe(Span y) {}

    /**
     * Spirals out from its release point for {@code seconds} ({@code turns} per second, the radius
     * growing {@code growth} px/s, the release point drifting down {@code drift} px/s), then flies on
     * along its outward angle turned downward, bouncing off the side edges up to {@code ricochets} times.
     */
    public record SpiralOut(double seconds, double turns, double growth, double drift, int ricochets) {
        public SpiralOut {
            Check.positive("seconds", seconds);
            Check.notNegative("ricochets", ricochets);
        }
    }

    /**
     * A part of a multi-part unit ({@code parts: multi}): a hit box around the unit's centre in its
     * own frame (facing down the screen: dx right, dy up towards its tail).
     *
     * @param kind {@code armoured}, {@code destroyable} or {@code vital} (destroying it destroys the
     *     rest); part G: a boss's {@code armoured} part is fire-only (it fires its attack, never
     *     takes damage, is not in the bar and pays nothing), written without {@code hp} and
     *     {@code bounty} (0 here)
     * @param attack the name of the unit's attack it fires
     * @param multiplier the damage it takes is multiplied by this (a weak point); 1 when not given
     * @param layer M5 part E: an arena boss's part's starting layer ({@code sub} or {@code ground});
     *     its script moves it
     * @param spots M5 part E: weak spots on it (the Kraken's eyes)
     * @param aim round 34 (P2, the kinder Siege Spire): {@code down}, the part fires its aimed attack
     *     straight down the screen instead of at the ship; empty: as its attack aims
     * @param slows M8 part B (design/enemies/ground/drill-rig: each pump lost slows the collar's ring 0.5 s): on a
     *     unit's part that carries its {@code attack}, the seconds its loss adds to that attack's interval; the attack
     *     stops when every part that carries it is lost; none if not given
     * @param attacks M8 part C (design/enemies/naval/eel-swarm: the head's aimed shot and its fan): the unit's attacks
     *     the part fires, by name, instead of one {@code attack} ({@link #attackNames})
     */
    public record PartData(
            String name,
            Point offset,
            Size hitbox,
            double hp,
            String kind,
            int bounty,
            Optional<String> attack,
            Optional<Double> multiplier,
            Optional<Integer> firstBonus,
            Optional<String> layer,
            Optional<List<SpotData>> spots,
            Optional<String> aim,
            Optional<Double> slows,
            Optional<List<String>> attacks) {
        public PartData {
            Check.that(attack.isEmpty() || attacks.isEmpty(), "a part fires one attack or a list of attacks, not both");
            attacks.ifPresent(names -> Check.that(names.size() >= 2, "attacks: two or more (else write attack)"));
            attacks = attacks.map(List::copyOf);
            aim.ifPresent(a -> Check.that(a.equals("down"), "a part's aim is down, was '" + a + "'"));
            slows.ifPresent(v -> Check.positive("slows", v));
            Check.that(slows.isEmpty() || attack.isPresent(), "only a part that carries an attack slows it (slows)");
            Check.that(aim.isEmpty() || attack.isPresent(), "a part aimed down fires an attack");
            firstBonus.ifPresent(b -> Check.notNegative("first_bonus", b));
            // M7 part H: or buried (a breaching boss's mouth until its rise, its vents until they rise with it).
            layer.ifPresent(l -> Check.that(
                    l.equals("sub") || l.equals("ground") || l.equals("buried"),
                    "a part's layer is sub, ground or buried, was '" + l + "'"));
            Check.that(
                    kind.equals("destroyable") || kind.equals("vital") || kind.equals("armoured"),
                    "kind must be armoured, destroyable or vital, was '" + kind + "'");
            if (kind.equals("armoured")) {
                Check.that(hp == 0 && bounty == 0, "an armoured part has no hp and no bounty");
            } else {
                Check.positive("hp", hp);
            }
            multiplier.ifPresent(m -> Check.positive("multiplier", m));
        }

        /** A part as written: an {@code armoured} one without {@code hp} and {@code bounty}. */
        @JsonCreator
        static PartData of(
                @com.fasterxml.jackson.annotation.JsonProperty("name") String name,
                @com.fasterxml.jackson.annotation.JsonProperty("offset") Point offset,
                @com.fasterxml.jackson.annotation.JsonProperty("hitbox") Size hitbox,
                @com.fasterxml.jackson.annotation.JsonProperty("hp") Optional<Double> hp,
                @com.fasterxml.jackson.annotation.JsonProperty("kind") String kind,
                @com.fasterxml.jackson.annotation.JsonProperty("bounty") Optional<Integer> bounty,
                @com.fasterxml.jackson.annotation.JsonProperty("attack") Optional<String> attack,
                @com.fasterxml.jackson.annotation.JsonProperty("multiplier") Optional<Double> multiplier,
                @com.fasterxml.jackson.annotation.JsonProperty("first_bonus") Optional<Integer> firstBonus,
                @com.fasterxml.jackson.annotation.JsonProperty("layer") Optional<String> layer,
                @com.fasterxml.jackson.annotation.JsonProperty("spots") Optional<List<SpotData>> spots,
                @com.fasterxml.jackson.annotation.JsonProperty("aim") Optional<String> aim,
                @com.fasterxml.jackson.annotation.JsonProperty("slows") Optional<Double> slows,
                @com.fasterxml.jackson.annotation.JsonProperty("attacks") Optional<List<String>> attacks) {
            Check.that(
                    name != null && offset != null && hitbox != null && kind != null,
                    "a part has a name, an offset, a hit box and a kind");
            boolean armoured = kind.equals("armoured");
            Optional<Double> hpValue = hp == null ? Optional.empty() : hp;
            Optional<Integer> bountyValue = bounty == null ? Optional.empty() : bounty;
            Check.that(armoured || (hpValue.isPresent() && bountyValue.isPresent()), "a part has hp and a bounty");
            Check.that(
                    !armoured || (hpValue.isEmpty() && bountyValue.isEmpty()),
                    "an armoured part has no hp and no bounty");
            return new PartData(
                    name,
                    offset,
                    hitbox,
                    hpValue.orElse(0.0),
                    kind,
                    bountyValue.orElse(0),
                    attack == null ? Optional.empty() : attack,
                    multiplier == null ? Optional.empty() : multiplier,
                    firstBonus == null ? Optional.empty() : firstBonus,
                    layer == null ? Optional.empty() : layer,
                    spots == null ? Optional.empty() : spots,
                    aim == null ? Optional.empty() : aim,
                    slows == null ? Optional.empty() : slows,
                    attacks == null ? Optional.empty() : attacks);
        }

        /** M8 part C: the names of the attacks it fires, its {@code attack} or its {@code attacks}; none if neither. */
        public List<String> attackNames() {
            return attack.map(List::of).or(() -> attacks).orElse(List.of());
        }

        /** Whether it is a fire-only part. */
        public boolean armoured() {
            return kind.equals("armoured");
        }
    }

    /**
     * M5 part E, a weak spot on a part (the Kraken's eyes): a box of {@code hitbox} at {@code offset}
     * ({@code [dx, dy]} px from the part's centre, dx right, dy up) that routes the damage to the part
     * at its {@code multiplier}; with {@code while: open} only while the part's surfacing window is
     * open (the part's own multiplier elsewhere).
     */
    public record SpotData(
            String name,
            Point offset,
            Size hitbox,
            double multiplier,

            @com.fasterxml.jackson.annotation.JsonProperty("while")
            Optional<String> during) {
        public SpotData {
            Check.positive("multiplier", multiplier);
            during.ifPresent(w -> Check.that(w.equals("open"), "while must be open, was '" + w + "'"));
        }
    }

    /**
     * Enters to a height of {@code y} px below the top of the play field, pauses for {@code pause}
     * seconds (its telegraph), then dives at {@code speed} px/s towards where the player was when
     * the pause ended, and flies on off the screen.
     *
     * @param fireAfter seconds into the dive at which it fires, unless it passes the player's
     *     height first
     */
    public record Dive(Span y, double pause, double speed, double fireAfter) {
        public Dive {
            Check.notNegative("pause", pause);
            Check.positive("speed", speed);
            Check.positive("fire_after", fireAfter);
        }
    }

    /**
     * Fixed to the ground layer, moving only with the scroll; M5 part E: with a {@code drift}, px/s
     * along its nest's {@code current} on top of the scroll (a raft on the water, the Reef Spitter).
     */
    public record Terrain(Optional<Double> drift) {
        public Terrain {
            drift.ifPresent(d -> Check.positive("drift", d));
        }
    }

    /** Along an authored path, {@code spacing} seconds between two units. */
    public record Snake(double spacing) {
        public Snake {
            Check.positive("spacing", spacing);
        }
    }

    /**
     * An entry curve of {@code radius} px, reaching {@code topSpeed} px/s. M7 part B (design/enemies/air/dust-kite):
     * with a {@code trigger} it is the Dust Kite's final swoop instead, started once the unit is within that many px
     * above the ship's row: one shallow curve across the ship's lane, aimed at the ship's x when it starts (no
     * homing), at {@code topSpeed} (the hook {@code swoop_speed} instead); then it glides on off the bottom or a
     * side. Mapped for the simulation by {@link MarsRules#kite}.
     */
    public record Swoop(Optional<Span> radius, Optional<Double> topSpeed, Optional<Double> trigger) {
        public Swoop {
            topSpeed.ifPresent(speed -> Check.positive("top_speed", speed));
            trigger.ifPresent(px -> Check.positive("trigger", px));
            Check.that(trigger.isEmpty() || topSpeed.isPresent(), "a triggered swoop has its top_speed");
        }
    }

    /**
     * M7 part B, the {@code burrow} movement (design/enemies/ground/burrower; M7 = a and the user's Q1 = a of
     * 2026-10-14): it travels under the sand on the {@code buried} layer at {@code swim} px/s on the screen (screen-fixed
     * while it travels, bulges and is surfaced: it works against the scroll) to a spot picked relative to the ship
     * when the bulge starts ({@code spot}), bulges there for {@code bulge} s, is a {@code ground} unit for {@code
     * surfaced} s (its attack's rings at the {@code rings}' times after it breaks out), dives over {@code dive} s
     * ({@code buried} from the dive's middle) and travels {@code travel} s (seeded) to its next spot; after {@code
     * surfacings} surfacings it travels off the bottom edge under the sand (an escape, no bounty). A wave's units
     * surface at least {@code stagger} s apart and at most {@code max_up} at once (a unit that would break it waits
     * buried). Mapped for the simulation by {@link MarsRules#burrower}.
     */
    public record Burrow(
            double swim,
            Span travel,
            Spot spot,
            double bulge,
            double surfaced,
            double dive,
            int surfacings,
            List<Ring> rings,
            double stagger,
            int maxUp) {
        public Burrow {
            Check.positive("swim", swim);
            Check.positive("travel", travel.min());
            Check.positive("bulge", bulge);
            Check.positive("surfaced", surfaced);
            Check.positive("dive", dive);
            Check.positive("surfacings", surfacings);
            Check.notEmpty("rings", rings);
            rings = List.copyOf(rings);
            for (Ring ring : rings) {
                Check.that(ring.at() < surfaced, "rings: each fires while it is surfaced, before " + surfaced + " s");
            }
            Check.notNegative("stagger", stagger);
            Check.positive("max_up", maxUp);
        }
    }

    /**
     * M7 part B: where a Burrower surfaces, picked when the bulge starts: {@code ahead} {@code [min, max]} px above the
     * ship's centre, within {@code side} px either side of its x, {@code edge} px inside the play field's side edges and
     * at least {@code clear} px from the ship's centre.
     */
    public record Spot(Span ahead, double side, double edge, double clear) {
        public Spot {
            Check.positive("ahead", ahead.min());
            Check.notNegative("side", side);
            Check.notNegative("edge", edge);
            Check.positive("clear", clear);
        }
    }

    /**
     * M7 part B: a Burrower's ring {@code at} s after it breaks out, of {@code count} bullets (its attack's count, or
     * the hook's {@code attacks.<name>.count}, if not given).
     */
    public record Ring(double at, Optional<Integer> count) {
        public Ring {
            Check.notNegative("at", at);
            count.ifPresent(n -> Check.positive("count", n));
        }
    }

    /** M7 part B: a difficulty's change to the {@code burrow} movement (the Burrower's easy and hard windows and rings). */
    public record BurrowChange(Optional<Double> surfaced, Optional<List<Ring>> rings) {
        public BurrowChange {
            surfaced.ifPresent(s -> Check.positive("surfaced", s));
            rings.ifPresent(list -> Check.notEmpty("rings", list));
        }
    }

    /** Straight at {@code speed} px/s. */
    public record Straight(double speed) {
        public Straight {
            Check.positive("speed", speed);
        }
    }

    /**
     * Hovers for {@code seconds} at a height of {@code y} px from the top of the play field; without
     * {@code seconds} it holds until it is killed (a boss). Part F: a unit entering from a side
     * edge hovers {@code edgeX} px from that edge, and {@code exit} {@code back} leaves through it. M8 part D
     * (design/enemies/naval/barbfin): with {@code above_ship} instead of {@code y} it stops a seeded {@code [min, max]}
     * px above the ship's y at the moment it stops (the Barbfin's 90–200), drifting {@code drift} px/s sideways while it
     * holds (the Barbfin's 6; 0 if not given) and turning toward the ship.
     */
    public record Hover(
            Optional<Span> seconds,
            Optional<Span> y,
            Optional<Double> edgeX,
            Optional<String> exit,
            Optional<Span> aboveShip,
            Optional<Double> drift) {
        public Hover {
            drift.ifPresent(speed -> Check.notNegative("drift", speed));
            // M8 part G: a hover at its y drifts too (the Depth Hunter's, in its lower band).
            Check.that(
                    drift.isEmpty() || aboveShip.isPresent() || y.isPresent(),
                    "a hover drifts at its y or above the ship");
            Check.that(y.isPresent() != aboveShip.isPresent(), "a hover holds at a y or above_ship, one of the two");
            aboveShip.ifPresent(above -> {
                Check.notNegative("above_ship", above.min());
                Check.that(above.max() >= above.min(), "above_ship: [min, max]");
            });
            Check.that(aboveShip.isEmpty() || edgeX.isEmpty(), "a hover above the ship enters from the top, no edge_x");
            edgeX.ifPresent(x -> Check.notNegative("edge_x", x));
            exit.ifPresent(e -> Check.that(e.equals("back"), "exit must be back, was '" + e + "'"));
        }

        /** {@code y}, {@code seconds} and {@code above_ship} written as {@code [min, max]} or as one value. */
        @JsonCreator
        static Hover of(
                @com.fasterxml.jackson.annotation.JsonProperty("seconds")
                        Optional<tools.jackson.databind.JsonNode> seconds,
                @com.fasterxml.jackson.annotation.JsonProperty("y") Optional<tools.jackson.databind.JsonNode> y,
                @com.fasterxml.jackson.annotation.JsonProperty("edge_x") Optional<Double> edgeX,
                @com.fasterxml.jackson.annotation.JsonProperty("exit") Optional<String> exit,
                @com.fasterxml.jackson.annotation.JsonProperty("above_ship")
                        Optional<tools.jackson.databind.JsonNode> aboveShip,
                @com.fasterxml.jackson.annotation.JsonProperty("drift") Optional<Double> drift) {
            Optional<tools.jackson.databind.JsonNode> height = given(y == null ? Optional.empty() : y);
            Optional<tools.jackson.databind.JsonNode> above = given(aboveShip == null ? Optional.empty() : aboveShip);
            Check.that(height.isEmpty() || span(height.get()) != null, "y is a height or [min, max]");
            Check.that(above.isEmpty() || span(above.get()) != null, "above_ship is a height or [min, max]");
            Optional<tools.jackson.databind.JsonNode> time = given(seconds == null ? Optional.empty() : seconds);
            Check.that(time.isEmpty() || span(time.get()) != null, "seconds is a time or [min, max]");
            return new Hover(
                    time.map(Hover::span),
                    height.map(Hover::span),
                    edgeX == null ? Optional.empty() : edgeX,
                    exit == null ? Optional.empty() : exit,
                    above.map(Hover::span),
                    drift == null ? Optional.empty() : drift);
        }

        /** The hold's height from the top of the play field; a hover above the ship has none (M8 part D). */
        public Span heightFromTop() {
            return y.orElseThrow(() -> new IllegalStateException("a hover above the ship holds at no fixed y"));
        }

        private static Optional<tools.jackson.databind.JsonNode> given(Optional<tools.jackson.databind.JsonNode> node) {
            return node.filter(n -> !n.isNull() && !n.isMissingNode());
        }

        private static Span span(tools.jackson.databind.JsonNode node) {
            if (node.isNumber()) {
                return new Span(node.doubleValue(), node.doubleValue());
            }
            if (node.isArray() && node.size() == 2) {
                return new Span(node.get(0).doubleValue(), node.get(1).doubleValue());
            }
            return null;
        }
    }

    /** Orbits a point at {@code radius} px and {@code turnRate} °/s. */
    public record Orbit(double radius, double turnRate) {
        public Orbit {
            Check.positive("radius", radius);
            Check.positive("turn_rate", turnRate);
        }
    }

    /**
     * An attack pattern (design/enemies/README.md, Attack pattern vocabulary).
     *
     * @param bullet the bullet class, which sets the damage
     * @param interval seconds between shots
     * @param speed bullet speed in px/s
     * @param firstShotDelay seconds from stopping to the first shot
     * @param count a {@code fan}'s bullets
     * @param spread a {@code fan}'s angle from its first to its last bullet, degrees; M5 part D: an aimed
     *     attack's with {@code aim: up}, from its burst's first shot to its last
     * @param turnRate a turret's barrel turn rate, °/s; the shots leave along the barrel
     * @param arc a turret fires while the player is within this many degrees of its facing (down the screen)
     */
    /**
     * @param aim where a fan points, {@code target} (the default), {@code down} or {@code facing}; a walker's fan
     *     (M5 part B) is aimed at the player with {@code target} and along its facing with {@code facing};
     *     planned (part D) for the other units; M5 part D: an aimed attack's {@code up} (the Wraith, user
     *     decision of 2026-10-08) sends its bursts straight up the screen as a fixed fan of {@code spread},
     *     its shots in turn from the left edge to the right, aimed at nobody and fired however close the
     *     ship is
     * @param away planned (part D): an aimed attack fires only while the player is more than this many ° off its facing
     * @param spawn planned (part D): the {@code spawn} pattern's release, which has no bullet, interval or speed
     * @param mortar the {@code mortar} pattern's lob (its {@code bullet} is the direct hit, its {@code speed} the ring's)
     * @param burst an aimed attack's shots per volley
     * @param burstGap seconds between the shots of a burst
     * @param rotate the parts sharing the attack take turns, one volley every interval among the living ones
     * @param arms a {@code spiral}'s arms; its {@code interval} is between two bullets of an arm
     * @param duration seconds a {@code spiral} runs before it hands over in an alternation; part G:
     *     without it a spiral fires for as long as its phase lasts (it cannot alternate)
     * @param stagger M5 part B, a walker's fan (design/enemies/ground/creeper): the units of one wave
     *     share a volley clock, unit i (in the order they enter) firing i × this many seconds after
     *     the first; a unit off the screen skips its turn
     * @param pounce M5 part C, the {@code pounce} pattern's leap (design/enemies/ground/ravager): no
     *     bullet and no speed; its {@code interval} is the s from landing until it may leap again
     * @param within M5 part E, a {@code ring}'s reach (design/enemies/naval/driftjelly): it fires only
     *     while the ship's centre is this close, px, on either of its layers; its {@code interval}
     *     the cooldown from the last ring
     * @param rate M5 part F, the {@code drain} pattern (design/enemies/air/lamprey): shield points a
     *     second it drains while latched, no bullet
     * @param staggered M5 part H, a boss's {@code aimed} or {@code mortar} attack fired by several parts
     *     (design/enemies/bosses/siege-spire: the pods' orbs, the mortar roots' lobs): each part on its own
     *     clock, part k of n {@code k × interval ÷ n} after the first; not with {@code rotate}
     * @param link M7 part C, the {@code link} pattern (design/enemies/ground/tendril-anchor): the tendril between two
     *     anchors of a linked nest, its touch the attack's {@code bullet} class; no speed or interval
     * @param pores M7 part C, the {@code pores} pattern (design/enemies/ground/threadcrawler): a chain's pore wave,
     *     each segment's pore an aimed {@code bullet} at {@code speed} in turn head to tail, {@code interval} s apart
     * @param only M7 part C: the difficulties it fires on (the Tendril Anchor's hard orbs); every one if not given
     * @param aura M7 part D, the {@code aura} pattern (design/enemies/air/choir-herald): the shield rings it sings onto
     *     the enemies near it; no bullet, speed, interval or count
     * @param aimTurns round 40 (design/enemies/bosses/siege-spire, proposal 4a of the Act 2 playtest): a boss's
     *     {@code aimed} attack fired by several parts takes turns to aim, one part at a time, each after a glow
     *     (the tell); between its turns a part fires as the attack says ({@code aim: down}: straight down its
     *     column); not with {@code rotate}
     * @param whileState M7 part E ({@code while}, design/enemies/ground/dust-devil): the state of its unit it fires
     *     in, {@code open} (a spin cycle's open window: the Dust Devil's grit spiral); always if not given
     * @param trigger M7 part E (design/enemies/air/ghost-drone): an aimed attack fired once per pass, its burst when
     *     the ship's x comes within {@code within_x} px of the unit's, instead of on an interval
     * @param tell M7 part E: s of the tell before the burst's first shot (the Ghost Drone's red eye glint)
     * @param reaim M7 part E: every shot of the burst is aimed at the ship anew (not the first shot's line)
     * @param parts M7 part F: a boss's parts that fire it, besides those naming it as their {@code attack} (the
     *     walker's pods fire one salvo pattern in its first phase, another later)
     * @param homing M7 part F: an aimed attack's shots are homing missiles (the walker's pods)
     */
    public record Attack(
            String pattern,
            Optional<String> name,
            Optional<String> bullet,
            Optional<Double> interval,
            Optional<Double> speed,
            Optional<Double> firstShotDelay,
            Optional<Integer> count,
            Optional<Double> spread,
            Optional<Double> turnRate,
            Optional<Double> arc,
            Optional<Mine> mine,
            Optional<String> aim,
            Optional<Double> away,
            Optional<Spawn> spawn,
            Optional<Mortar> mortar,
            Optional<Integer> burst,
            Optional<Double> burstGap,
            Optional<Boolean> rotate,
            Optional<Integer> arms,
            Optional<Double> duration,
            Optional<Sweep> sweep,
            Optional<Double> stagger,
            Optional<Pounce> pounce,
            Optional<Double> within,
            Optional<Double> rate,
            Optional<Boolean> staggered,
            Optional<Link> link,
            Optional<Pores> pores,
            Optional<List<String>> only,
            Optional<Aura> aura,
            Optional<AimTurns> aimTurns,
            @JsonProperty("while") Optional<String> whileState,
            Optional<Trigger> trigger,
            Optional<Double> tell,
            Optional<Boolean> reaim,
            Optional<List<String>> parts,
            Optional<Homing> homing,
            Optional<Slow> slow) {
        /** M7 part E: {@code while: open}, a spin cycle's open window. */
        public static final String WHILE_OPEN = "open";

        public Attack {
            whileState.ifPresent(state -> Check.that(
                    WHILE_OPEN.equals(state), "while: only 'open' (a spin cycle's window), was '" + state + "'"));
            Check.that(
                    trigger.isEmpty() || (pattern.equals("aimed") && interval.isEmpty()),
                    "a triggered attack is aimed and fires once per pass, no interval");
            tell.ifPresent(t -> Check.positive("tell", t));
            Check.that(reaim.isEmpty() || burst.isPresent(), "only a burst re-aims its shots (reaim needs a burst)");
            parts.ifPresent(list -> Check.notEmpty("parts", list));
            Check.that(
                    homing.isEmpty()
                            || (pattern.equals("aimed")
                                    && aim.filter("up"::equals).isEmpty()),
                    "only an aimed attack fires homing missiles (not straight up)");
            Check.that(pattern.equals("aura") == aura.isPresent(), "an aura attack has its aura, the others none");
            Check.that(
                    !pattern.equals("aura")
                            || (bullet.isEmpty()
                                    && speed.isEmpty()
                                    && interval.isEmpty()
                                    && count.isEmpty()
                                    && firstShotDelay.isEmpty()),
                    "an aura has no bullet, speed, interval, count or first shot");
            Check.that(pattern.equals("link") == link.isPresent(), "a link attack has its link, the others none");
            Check.that(
                    !pattern.equals("link")
                            || (bullet.isPresent()
                                    && speed.isEmpty()
                                    && interval.isEmpty()
                                    && firstShotDelay.isEmpty()
                                    && only.isEmpty()),
                    "a link has its touch's bullet class and no speed, interval, first shot or difficulties");
            Check.that(pattern.equals("pores") == pores.isPresent(), "a pores attack has its pores, the others none");
            Check.that(
                    !pattern.equals("pores") || interval.isPresent(),
                    "a pores attack has its interval (s between two pores)");
            only.ifPresent(names -> {
                Check.notEmpty("only", names);
                names.forEach(Difficulty::of);
            });
            Check.that(
                    staggered.isEmpty() || pattern.equals("aimed") || pattern.equals("mortar"),
                    "only an aimed or a mortar attack is staggered");
            Check.that(
                    !staggered.orElse(false) || !rotate.orElse(false), "an attack rotates or is staggered, not both");
            // M5 part F: a drain (the Lamprey's, while latched) has its rate of shield points a second and nothing
            // else.
            Check.that(pattern.equals("drain") == rate.isPresent(), "a drain has its rate, the others none");
            rate.ifPresent(r -> Check.positive("rate", r));
            Check.that(
                    !pattern.equals("drain")
                            || (bullet.isEmpty() && speed.isEmpty() && interval.isEmpty() && count.isEmpty()),
                    "a drain has no bullet, speed, interval or count");
            Check.that(pattern.equals("ring") || within.isEmpty(), "only a ring fires within a reach");
            within.ifPresent(w -> Check.positive("within", w));
            Check.that(within.isEmpty() || interval.isPresent(), "a ring fired within a reach has its interval");
            Check.that(
                    pattern.equals("laser-sweep") == sweep.isPresent(), "a laser-sweep has its sweep, the others none");
            Check.that(
                    !pattern.equals("laser-sweep") || (bullet.isPresent() && speed.isEmpty()),
                    "a laser-sweep has a bullet and no speed");
            Check.that(
                    pattern.equals("aimed")
                            || pattern.equals("fan")
                            || pattern.equals("mine")
                            || pattern.equals("spawn")
                            || pattern.equals("mortar")
                            || pattern.equals("ring")
                            || pattern.equals("spiral")
                            || pattern.equals("laser-sweep")
                            || pattern.equals("pounce")
                            || pattern.equals("drain")
                            || pattern.equals("link")
                            || pattern.equals("pores")
                            || pattern.equals("aura"),
                    "pattern must be aimed, fan, mine, spawn, mortar, ring, spiral, laser-sweep, pounce, drain, link,"
                            + " pores or aura, was '" + pattern + "'");
            Check.that(pattern.equals("pounce") == pounce.isPresent(), "a pounce has its pounce, the others none");
            Check.that(
                    !pattern.equals("pounce") || (bullet.isEmpty() && speed.isEmpty() && interval.isPresent()),
                    "a pounce has an interval and no bullet or speed");
            Check.that(pattern.equals("mine") == mine.isPresent(), "a mine attack has its mine, the others none");
            Check.that(
                    pattern.equals("mortar") == mortar.isPresent(), "a mortar attack has its mortar, the others none");
            // Round 43: a boss's fan may come in a burst too (the walker's chin and flank guns; FoundryChecks).
            Check.that(
                    pattern.equals("aimed")
                            || (pattern.equals("fan") && rotate.isEmpty())
                            || (burst.isEmpty() && burstGap.isEmpty() && rotate.isEmpty()),
                    "only an aimed attack (or a boss's fan) has a burst, a burst gap or rotates");
            burst.ifPresent(b -> Check.positive("burst", b));
            burstGap.ifPresent(g -> Check.positive("burst_gap", g));
            Check.that(pattern.equals("spiral") == arms.isPresent(), "a spiral has its arms, the others none");
            Check.that(pattern.equals("spiral") || duration.isEmpty(), "only a spiral has a duration");
            arms.ifPresent(a -> Check.positive("arms", a));
            duration.ifPresent(d -> Check.positive("duration", d));
            Check.that(!pattern.equals("spiral") || turnRate.isPresent(), "a spiral has a turn rate");
            Check.that(pattern.equals("spawn") == spawn.isPresent(), "a spawn attack has its spawn, the others none");
            Check.that(
                    pattern.equals("laser-sweep")
                            || pattern.equals("pounce")
                            || pattern.equals("drain")
                            || pattern.equals("link")
                            || pattern.equals("aura")
                            // M8 part E: a slowing ring is sound, no bullet (its speed below).
                            || slow.isPresent()
                            || pattern.equals("spawn") != (bullet.isPresent() && speed.isPresent()),
                    "an attack has a bullet and a speed, a spawn attack neither");
            Check.that(!pattern.equals("spawn") || interval.isEmpty(), "a spawn attack has no interval");
            aim.ifPresent(a -> Check.that(
                    a.equals("target") || a.equals("down") || a.equals("facing") || a.equals("up"),
                    "aim must be target, down, facing or up, was '" + a + "'"));
            away.ifPresent(a -> Check.notNegative("away", a));
            interval.ifPresent(i -> Check.positive("interval", i));
            speed.ifPresent(s -> Check.positive("speed", s));
            firstShotDelay.ifPresent(d -> Check.notNegative("first_shot_delay", d));
            Check.that(
                    (pattern.equals("fan") || (pattern.equals("ring") && slow.isEmpty())) == count.isPresent(),
                    "a fan or a ring of bullets has a count, the others none");
            Check.that(slow.isEmpty() || pattern.equals("ring"), "only a ring slows");
            Check.that(
                    slow.isEmpty()
                            || (bullet.isEmpty()
                                    && speed.isPresent()
                                    && interval.isPresent()
                                    && tell.isPresent()
                                    && within.isEmpty()),
                    "a slowing ring has its speed, interval and tell, and no bullet or reach");
            boolean up = aim.filter("up"::equals).isPresent();
            Check.that(!up || pattern.equals("aimed"), "only an aimed attack fires straight up");
            Check.that(
                    (pattern.equals("fan") || up) == spread.isPresent(),
                    "a fan or an aimed attack fired up has a spread, the others none");
            count.ifPresent(c -> Check.positive("count", c));
            Check.that(pattern.equals("fan") || stagger.isEmpty(), "only a fan has a stagger");
            stagger.ifPresent(st -> Check.positive("stagger", st));
            Check.that(
                    aimTurns.isEmpty() || (pattern.equals("aimed") && !rotate.orElse(false)),
                    "only an aimed attack that does not rotate takes aim turns");
        }

        /** M7 part C: whether it fires on {@code difficulty}: its {@code only}, every one if not given. */
        public boolean firesOn(Difficulty difficulty) {
            return only.map(names -> names.stream().map(Difficulty::of).anyMatch(difficulty::equals))
                    .orElse(true);
        }

        /** M7 part E: whether it fires only while its unit's spin cycle is open ({@code while: open}). */
        public boolean whileOpen() {
            return whileState.filter(WHILE_OPEN::equals).isPresent();
        }

        /** M7 part E: the pass trigger's reach, px ({@code trigger.within_x}); NaN without a trigger. */
        public double passWithinX() {
            return trigger.map(Trigger::withinX).orElse(Double.NaN);
        }
    }

    /**
     * M7 part E, a pass trigger (design/enemies/air/ghost-drone): the attack fires its burst once per pass, when the
     * ship's centre comes within {@code within_x} px of the unit's x.
     */
    public record Trigger(double withinX) {
        public Trigger {
            Check.positive("within_x", withinX);
        }
    }

    /**
     * Round 40, an aimed boss attack's aim turns (design/enemies/bosses/siege-spire, proposal 4a): every {@code every}
     * s the next living part in turn fires one shot at the ship, after glowing {@code glow} s (the tell).
     */
    public record AimTurns(double every, double glow) {
        public AimTurns {
            Check.positive("every", every);
            Check.notNegative("glow", glow);
            Check.that(glow < every, "an aim turn's glow is shorter than its every");
        }
    }

    /**
     * M7 part C, a {@code link}'s tendril (design/enemies/ground/tendril-anchor; M9 = a): {@code hp} (damaged only by
     * a {@code beam}, which goes on past it), {@code dormant} s of a harmless flickering tell after the pair scrolls
     * onto the screen, then a touch in its {@code band} px contact band costs the attack's bullet class with a {@code
     * cooldown} s of its own (the ship and the escort; no push); {@code width} its strip's width, px; {@code
     * burnt_bounty} the credits for burning it (Act 1 terms). Either anchor's death breaks it for good.
     */
    public record Link(double hp, double dormant, double band, double width, double cooldown, int burntBounty) {
        public Link {
            Check.positive("hp", hp);
            Check.notNegative("dormant", dormant);
            Check.positive("band", band);
            Check.positive("width", width);
            Check.positive("cooldown", cooldown);
            Check.notNegative("burnt_bounty", burntBounty);
        }
    }

    /**
     * M7 part C, a chain's pore wave (design/enemies/ground/threadcrawler): after {@code rest} s, each segment's pore
     * fires in turn head to tail (the attack's {@code interval} s apart), each glowing {@code glow} s before it fires
     * (the tell); with {@code every_other} only every other pore fires (easy).
     */
    public record Pores(double rest, double glow, Optional<Boolean> everyOther) {
        public Pores {
            Check.positive("rest", rest);
            Check.positive("glow", glow);
        }

        /** Whether only every other pore fires. */
        public boolean alternate() {
            return everyOther.orElse(false);
        }
    }

    /** M7 part C: a difficulty's change to a pore wave (the Threadcrawler's easy every other pore, hard 1.5 s rest). */
    public record PoresChange(Optional<Double> rest, Optional<Boolean> everyOther) {
        public PoresChange {
            rest.ifPresent(r -> Check.positive("rest", r));
        }
    }

    /**
     * A spawner's release: {@code count} units of {@code enemy} flying out at {@code speed} in an
     * {@code arc} of ° toward the ship. A brood (Level 04's Brood Pod) releases them when it is
     * killed or {@code after} s from entering (a self-burst paying {@code burst_bounty}, not a kill),
     * with a {@code telegraph} of s before it. M5 part C, a periodic spawner (the Hive Node): instead
     * of {@code after} and {@code burst_bounty}, {@code every} s from its centre crossing the top
     * edge, its iris opening over the {@code telegraph}, and an opening due while the ship's centre
     * is within {@code shut_within} px skipped; it never bursts on its own. M8 part D (the spawning reef): a periodic
     * spawn of a kind with a flock's ram (the Glassfin) may give its released units' {@code ram_after}, s on the field
     * before each draws its ram, instead of the kind's own (released mid-screen, the school's 3 s would see them gone).
     */
    public record Spawn(
            String enemy,
            int count,
            Optional<Double> after,
            Optional<Double> every,
            double telegraph,
            double arc,
            double speed,
            Optional<Integer> burstBounty,
            Optional<Double> shutWithin,
            Optional<Double> ramAfter) {
        public Spawn {
            ramAfter.ifPresent(a -> Check.notNegative("ram_after", a));
            Check.that(every.isPresent() || ramAfter.isEmpty(), "only a periodic spawn has a ram_after");
            Check.positive("count", count);
            Check.that(after.isPresent() != every.isPresent(), "a spawn has an after (a brood) or an every, not both");
            after.ifPresent(a -> Check.positive("after", a));
            every.ifPresent(e -> Check.positive("every", e));
            Check.notNegative("telegraph", telegraph);
            Check.positive("arc", arc);
            Check.positive("speed", speed);
            Check.that(
                    after.isPresent() == burstBounty.isPresent(),
                    "a brood has a burst_bounty, a periodic spawn none (it never bursts)");
            burstBounty.ifPresent(b -> Check.notNegative("burst_bounty", b));
            Check.that(every.isPresent() || shutWithin.isEmpty(), "only a periodic spawn has a shut_within");
            shutWithin.ifPresent(d -> Check.notNegative("shut_within", d));
            every.ifPresent(e -> Check.that(telegraph < e, "the telegraph lies inside the cycle (every)"));
        }

        /** Whether it is a periodic spawn (with {@code every}). */
        public boolean periodic() {
            return every.isPresent();
        }
    }

    /**
     * M5 part C, a pounce's leap (design/enemies/ground/ravager): within {@code range} px of the
     * ship (centre to centre, on the screen) it leaps at the ship's position over {@code leap} s;
     * for the middle {@code air} s it is on the {@code air} layer; drawn {@code scale} times its size
     * at the apex.
     */
    public record Pounce(double range, double leap, double air, double scale) {
        public Pounce {
            Check.positive("range", range);
            Check.positive("leap", leap);
            Check.notNegative("air", air);
            Check.that(air <= leap, "the air window lies inside the leap");
            Check.positive("scale", scale);
        }
    }

    /** A formation it appears in, with the unit count: {@code [n]} or {@code [min, max]}. */
    public record FormationUse(String name, Optional<List<Integer>> size) {
        public FormationUse {
            size.ifPresent(s -> Check.that(s.size() == 1 || s.size() == 2, "size must be [n] or [min, max]"));
        }
    }

    /**
     * The glowing weak point; only a part of a multi-part unit has a damage {@code multiplier}
     * (design/enemies, stat block template), a single-part unit's is drawn only.
     */
    public record WeakPoint(String name, Optional<Double> multiplier) {
        public WeakPoint {
            multiplier.ifPresent(m -> Check.positive("multiplier", m));
        }
    }

    /** Every {@code every}-th kill of this enemy in a level drops {@code pickup}. */
    public record Drop(Pickup pickup, int every) {
        public Drop {
            Check.positive("every", every);
        }
    }

    /**
     * A dropped spore mine: it arms (rises to the player plane) after {@code arm} s, drifts
     * {@code drift} px/s in a random direction, has {@code hp}, and bursts after {@code life} s into
     * a {@code ring} of {@code ringBullet} bullets at the attack's speed; destroyed it pays {@code credits}.
     */
    public record Mine(double arm, double life, double drift, double hp, int ring, String ringBullet, int credits) {
        public Mine {
            Check.notNegative("arm", arm);
            Check.positive("life", life);
            Check.positive("hp", hp);
        }
    }

    /**
     * A mortar's lob: the impact marker shows {@code marker} s ahead (the blob's flight), a ship
     * inside the {@code impact} px circle when it lands takes the direct hit, and it bursts into a
     * ring of {@code ring} bullets of class {@code ringBullet}. Parsed; flown with the Polyp Mortar.
     */
    public record Mortar(double marker, double impact, int ring, String ringBullet) {
        public Mortar {
            Check.positive("marker", marker);
            Check.positive("impact", impact);
            Check.positive("ring", ring);
        }
    }

    /**
     * An articulated neck: {@code segments} {@code armoured} hit boxes from the anchor {@code from}
     * on the body to the part {@code to} at its end (whose offset is the chain's rest end); each
     * segment follows the one before it {@code lag} s late, and the chain turns at most
     * {@code bend} degrees towards the player.
     */
    public record ChainData(
            String name,
            Point from,
            String to,
            int segments,
            Size hitbox,
            double lag,
            double bend,
            Optional<String> motion) {
        public ChainData {
            Check.positive("segments", segments);
            Check.positive("lag", lag);
            Check.notNegative("bend", bend);
            motion.ifPresent(m -> Check.that(m.equals("slam"), "motion must be slam, was '" + m + "'"));
        }

        /** M5 part E: whether it is a slam arm ({@code motion: slam}): it follows the slam cycle instead of bending. */
        public boolean slams() {
            return motion.isPresent();
        }
    }

    /**
     * A boss's script: {@code kind} {@code boss} or {@code mid-boss} (the short bar), its bar's
     * name, the par time in s (the Boss rush bonus) and its phases in order.
     *
     * @param engagesOnArrival part G: its first phase starts when it arrives (the bar appearing),
     *     its entrance part of the fight; otherwise when it settles at its hover height
     * @param deathSeconds part G: how long its chained death runs, s (the act boss's 3 s, tail to
     *     head); left out, the mid-boss's quick chain
     * @param poses part G: its further part layouts (the {@code part_list} offsets and the
     *     {@code hitbox} are the arrival pose), which a phase's {@code move} turns it into
     * @param lanes M5 part E: an arena boss's slam lanes (the Harbour Kraken)
     * @param slam M5 part E: its slam cycle (only with lanes)
     * @param plate M7 part F: a walking boss's front plate (the Revenant Walker)
     * @param cone M7 part F: a walking boss's rear flame cone (the Revenant Walker)
     * @param hatchArmour round 44: a walking boss's pods (the parts firing its homing attacks) take damage only while
     *     their hatch is open
     * @param lance round 44: a walking boss's back-mounted rail lance
     * @param stomp round 44: a walking boss's stomp shockwave
     * @param suction M7 part H: a breaching boss's pull toward its open mouth (the Dust Colossus)
     * @param vents M7 part H: a breaching boss's spine vents
     * @param eruption M7 part H: a breaching boss's eruption under the ship
     * @param debris M7 part H: a breaching boss's debris rocks
     * @param coil M8 part H: a coiled arena boss's body and script (the Abyssal Maw; {@link MawBlock})
     */
    public record BossData(
            String kind,
            String barName,
            double par,
            List<PhaseData> phases,
            Optional<Boolean> engagesOnArrival,
            Optional<Double> deathSeconds,
            Optional<List<PoseData>> poses,
            Optional<Lanes> lanes,
            Optional<SlamData> slam,
            Optional<Plate> plate,
            Optional<Cone> cone,
            Optional<Boolean> hatchArmour,
            Optional<Lance> lance,
            Optional<Stomp> stomp,
            Optional<Suction> suction,
            Optional<Vents> vents,
            Optional<Eruption> eruption,
            Optional<DebrisData> debris,
            Optional<CoilData> coil) {
        public BossData {
            Check.that(
                    kind.equals("boss") || kind.equals("mid-boss"),
                    "kind must be boss or mid-boss, was '" + kind + "'");
            Check.positive("par", par);
            Check.notEmpty("phases", phases);
            deathSeconds.ifPresent(d -> Check.notNegative("death_seconds", d));
            Check.that(lanes.isPresent() == slam.isPresent(), "a boss with lanes has a slam cycle, and only it");
            poses.ifPresent(list -> list.forEach(pose -> Check.that(
                    list.stream()
                                    .filter(other -> other.name().equals(pose.name()))
                                    .count()
                            == 1,
                    "pose names are unique, '" + pose.name() + "' is not")));
        }

        /** The index of pose {@code name} in the boss's poses (0 the arrival pose, the further ones from 1); -1 without one. */
        public int poseIndex(String name) {
            List<PoseData> list = poses.orElse(List.of());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).name().equals(name)) {
                    return i + 1;
                }
            }
            return -1;
        }

        /** Whether it is a mid-boss, with the short bar. */
        public boolean midBoss() {
            return kind.equals("mid-boss");
        }
    }

    /**
     * M5 part E, an arena boss's slam lanes (design/enemies/bosses/harbour-kraken, user decision E5 =
     * a): {@code count} lanes {@code width} px wide from the play field's left edge, lane 1 first;
     * {@code arms}, each slam arm's part name with the lanes (1-based) it owns.
     */
    public record Lanes(
            int count,
            double width,
            Optional<java.util.Map<String, List<Integer>>> arms,
            Optional<Boolean> rooted,
            Optional<Double> raised) {
        public Lanes {
            Check.positive("count", count);
            Check.positive("width", width);
            Check.that(
                    count * width <= vanguard.sim.PlayField.WIDTH + 1e-9,
                    "the lanes lie on the play field: " + count + " × " + width + " px");
            arms.ifPresent(map -> map.values()
                    .forEach(owned -> owned.forEach(lane -> Check.that(
                            lane >= 1 && lane <= count, "arms: lane " + lane + " is not one of the " + count))));
            boolean isRooted = rooted.orElse(false);
            Check.that(raised.isEmpty() || isRooted, "only rooted arms take more damage while raised (raised)");
            raised.ifPresent(r -> Check.positive("raised", r));
            if (isRooted) {
                java.util.Set<Integer> owned = new java.util.HashSet<>();
                arms.orElse(java.util.Map.of())
                        .forEach((part, lanes) -> Check.that(
                                lanes.size() == 1 && owned.add(lanes.getFirst()),
                                "a rooted arm owns one lane of its own: '" + part + "' owns " + lanes));
            }
        }

        /** Lanes whose arms are not rooted (M5 part E). */
        public Lanes(int count, double width, Optional<java.util.Map<String, List<Integer>>> arms) {
            this(count, width, arms, Optional.empty(), Optional.empty());
        }

        /** Round 40: whether every arm is a rooted one-lane arm. */
        public boolean isRooted() {
            return rooted.orElse(false);
        }
    }

    /**
     * M5 part E, an arena boss's slam cycle (design/enemies/bosses/harbour-kraken): the lane's
     * {@code telegraph}, the arm's {@code rise}, {@code awash} and {@code sink} times (s); the
     * impact's {@code damage} class on the ship and the escort in the lane (once per slam); the
     * {@code splash} bullets from along the arm; {@code choose: alternate} (the ship's lane and the
     * nearest convoy ship's in turn, the only rule); the hooks {@code telegraph} and {@code lanes} change
     * the telegraph and the lanes a volley slams (one per arm when not given). Round 40: {@code volley}, the
     * lanes a volley slams at medium (the rooted Kraken's 2: the ship's lane and a convoy ship's), one per arm
     * when not given.
     */
    public record SlamData(
            double telegraph,
            double rise,
            double awash,
            double sink,
            String damage,
            Splash splash,
            String choose,
            Optional<Integer> volley) {
        public SlamData {
            Check.positive("telegraph", telegraph);
            Check.positive("rise", rise);
            Check.positive("awash", awash);
            Check.positive("sink", sink);
            Check.that(choose.equals("alternate"), "choose must be alternate, was '" + choose + "'");
            volley.ifPresent(v -> Check.positive("volley", v));
        }
    }

    /** The slam's splash: {@code count} bullets of class {@code bullet} at {@code speed} px/s. */
    public record Splash(int count, double speed, String bullet) {
        public Splash {
            Check.notNegative("count", count);
            Check.positive("speed", speed);
        }
    }

    /**
     * M5 part E, how a phase slams: {@code chain} (one at a time, the next telegraph when the arm has
     * sunk), {@code after_dive} (one after each dive) or {@code volley} with {@code every} s (one per
     * living arm at once); written as the mode alone or as {@code {mode, every}}.
     */
    public record SlammingData(String mode, Optional<Double> every) {
        public SlammingData {
            Check.that(
                    mode.equals("chain") || mode.equals("after_dive") || mode.equals("volley"),
                    "slamming is chain, after_dive or volley, was '" + mode + "'");
            Check.that(every.isPresent() == mode.equals("volley"), "a volley, and only it, has its every");
            every.ifPresent(e -> Check.positive("every", e));
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        static SlammingData of(
                @com.fasterxml.jackson.annotation.JsonProperty("mode") String mode,
                @com.fasterxml.jackson.annotation.JsonProperty("every") Optional<Double> every) {
            Check.that(mode != null, "slamming has a mode");
            return new SlammingData(mode, every == null ? Optional.empty() : every);
        }

        /** The mode written alone. */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static SlammingData of(String mode) {
            return new SlammingData(mode, Optional.empty());
        }
    }

    /**
     * M5 part E, a phase's surfacing part (the Kraken's head): it rises crown first over {@code rise}
     * s (its layer flipping at the middle), stays {@code open} s (its weak spots open), dives over
     * {@code dive} s; {@code glow} s is the tell before each of the phase's attacks; {@code stay: true}
     * keeps it up (no open or dive time then).
     */
    public record SurfaceData(
            String part,
            double rise,
            Optional<Double> open,
            Optional<Double> dive,
            Optional<Double> glow,
            Optional<Boolean> stay) {
        public SurfaceData {
            Check.positive("rise", rise);
            boolean stays = stay.orElse(false);
            Check.that(
                    stays ? open.isEmpty() && dive.isEmpty() : open.isPresent() && dive.isPresent(),
                    "a surfacing that stays up has no open or dive time; one that dives has both");
            open.ifPresent(o -> Check.positive("open", o));
            dive.ifPresent(d -> Check.positive("dive", d));
            glow.ifPresent(g -> Check.notNegative("glow", g));
        }
    }

    /**
     * M5 part E, units an arena boss releases once (the arena's Driftjelly field): {@code count}
     * units of {@code enemy} as a {@code field} over {@code lanes}, {@code on: first-surface} (its
     * surfacing part's first rise); they count among the level's enemies as they are released.
     */
    public record ReleaseData(String enemy, int count, String formation, List<Integer> lanes, String on) {
        public ReleaseData {
            Check.positive("count", count);
            Check.notEmpty("lanes", lanes);
            Check.that(formation.equals("field"), "a release is a field, was '" + formation + "'");
            Check.that(on.equals("first-surface"), "a release comes on first-surface, was '" + on + "'");
        }
    }

    /**
     * A boss phase: it ends when at most {@code until.left} of {@code until.parts} are alive or
     * {@code until.seconds} after it engaged; it fires its {@code attacks} together or the
     * {@code alternate} ones in turn, sends its {@code streams}, {@code exposes} parts that took no
     * damage before it and bends the chains {@code bend} degrees.
     *
     * @param delay part G: s from the phase engaging to its first attack and window (a concurrent
     *     attack's first volley one interval after that); left out, 1 s for a later phase that
     *     alternates (the frigate's crown opening), else 0
     * @param move part G: its opening move, invulnerable: the glide to a station and a layer, then a
     *     turn into a pose
     * @param windows part G: the parts that open and shut in turn (vulnerable only while open), and
     *     what each opening releases
     * @param slamming M5 part E: how it slams
     * @param surface M5 part E: its surfacing part
     * @param release M5 part E: what it releases once
     * @param drop M5 part E: the pickup dropped as it begins (the shield cell)
     */
    public record PhaseData(
            String name,
            Until until,
            Optional<List<String>> attacks,
            Optional<List<String>> alternate,
            Optional<StreamData> streams,
            Optional<List<String>> exposes,
            Optional<Double> bend,
            Optional<Double> delay,
            Optional<MoveData> move,
            Optional<WindowData> windows,
            Optional<SlammingData> slamming,
            Optional<SurfaceData> surface,
            Optional<ReleaseData> release,
            Optional<Pickup> drop) {
        public PhaseData {
            Check.that(attacks.isEmpty() || alternate.isEmpty(), "a phase has attacks or an alternate list, not both");
            bend.ifPresent(b -> Check.notNegative("bend", b));
            delay.ifPresent(d -> Check.notNegative("delay", d));
        }
    }

    /**
     * The end of a phase: at most {@code left} (0 when left out) of {@code parts} alive, or part G
     * {@code seconds} after the phase engaged (after its move), whichever comes first; at least one
     * of the two. M5 part E: or after {@code slams} impacts, or once {@code parts} hold less than the
     * share {@code below} of their HP (instead of {@code left}).
     */
    public record Until(
            Optional<List<String>> parts,
            Optional<Integer> left,
            Optional<Double> seconds,
            Optional<Integer> slams,
            Optional<Double> below,
            Optional<Share> share) {
        public Until {
            Check.that(
                    parts.isPresent() || seconds.isPresent() || slams.isPresent(), "until has parts, seconds or slams");
            parts.ifPresent(list -> Check.notEmpty("parts", list));
            left.ifPresent(l -> Check.notNegative("left", l));
            Check.that(left.isEmpty() || parts.isPresent(), "until has a left only with its parts");
            Check.that(
                    below.isEmpty() || (parts.isPresent() && left.isEmpty()),
                    "until has a below with its parts, not a left");
            seconds.ifPresent(s -> Check.positive("seconds", s));
            slams.ifPresent(n -> Check.positive("slams", n));
            below.ifPresent(b -> Check.that(b > 0 && b < 1, "below is a share between 0 and 1, was " + b));
            Check.that(
                    share.isEmpty() || (parts.isPresent() && below.isEmpty()),
                    "until has a share beside its parts (and a left), not with a below");
        }
    }

    /**
     * M7 part F (design/enemies/bosses/revenant-walker, Phases): a phase's second end beside its parts' {@code left}:
     * once {@code parts} hold less than {@code below} of their HP (the walker's first phase: both pods gone, or the
     * hull below 60 %).
     */
    public record Share(List<String> parts, double below) {
        public Share {
            parts = List.copyOf(parts);
            Check.notEmpty("parts", parts);
            Check.that(below > 0 && below < 1, "below is a share between 0 and 1, was " + below);
        }
    }

    /**
     * Part G: a further part layout of a boss: its {@code name}, its armoured body's {@code hitbox}
     * (the stat block's when left out) and its parts' {@code offsets} by part name ({@code [dx, dy]}
     * px from the centre, dx right, dy up; a part left out keeps its {@code part_list} offset).
     */
    public record PoseData(String name, Optional<Size> hitbox, java.util.Map<String, Point> offsets) {}

    /**
     * Part G: a phase's opening move: over {@code descend} s the boss glides to {@code to}
     * ({@code [x, y]}: px from the left edge, px below the top edge, as {@code hover.y}) and sinks or
     * rises to {@code layer}, then over {@code turn} s it turns in place into {@code pose}; each
     * left out keeps what it has (0 s for the times). It takes no damage and holds its fire until the
     * move ends.
     */
    public record MoveData(
            Optional<Point> to,
            Optional<String> layer,
            Optional<Double> descend,
            Optional<String> pose,
            Optional<Double> turn) {
        public MoveData {
            layer.ifPresent(Layers::of);
            descend.ifPresent(d -> Check.notNegative("descend", d));
            turn.ifPresent(t -> Check.notNegative("turn", t));
        }
    }

    /**
     * Part G: a phase's windows: every {@code every} s the next of the {@code groups} (part names, in
     * the order the windows cycle) with a living part on the field opens for {@code open} s (every
     * such group at once when {@code all}), the first {@code offset} s after the phase's delay. A
     * part in a group takes damage only while open. Each opening releases the next of the
     * {@code spawns} in turn from every group it opened.
     */
    public record WindowData(
            List<List<String>> groups,
            double every,
            double open,
            Optional<Double> offset,
            Optional<Boolean> all,
            Optional<List<SpawnData>> spawns) {
        public WindowData {
            Check.notEmpty("groups", groups);
            groups.forEach(group -> Check.notEmpty("groups[]", group));
            Check.positive("every", every);
            Check.positive("open", open);
            offset.ifPresent(o -> Check.notNegative("offset", o));
        }
    }

    /**
     * Part G: what an opened window group releases: {@code count} units of {@code enemy} (a group
     * with parts destroyed its share, rounded up) from its open parts, spread over {@code arc} °
     * centred on the ship, flying out at {@code speed} px/s; with a {@code glide} of s they then
     * hold for their hover time, firing, and leave down the screen. The difficulty hook
     * {@code spawns} changes the count by {@code name}.
     */
    public record SpawnData(
            String name,
            String enemy,
            int count,
            double speed,
            Optional<Double> arc,
            Optional<Double> glide,
            Optional<AmbushLaunch> ambush) {
        public SpawnData {
            Check.notNegative("count", count);
            Check.positive("speed", speed);
            arc.ifPresent(a -> Check.notNegative("arc", a));
            glide.ifPresent(g -> Check.notNegative("glide", g));
            Check.that(
                    ambush.isEmpty() || (arc.isEmpty() && glide.isEmpty()),
                    "an ambush launch flies its enemy's rear path: no arc or glide");
        }
    }

    /**
     * M5 part H, a window spawn's ambush launch (design/enemies/bosses/siege-spire, the maw's Wraiths;
     * decision H3 = a): each unit flies its enemy's rear-ambush path from the part (cloaked down its lane
     * past the ship, back in at the bottom edge after its gap, warned 3 s ahead, the decloak, the hold,
     * out up a side lane); a launch that would put more than {@code most} of the boss's launched units on
     * the field leaves the rest out.
     */
    public record AmbushLaunch(int most) {
        public AmbushLaunch {
            Check.positive("most", most);
        }
    }

    /**
     * Streams of {@code count} units of {@code enemy}, {@code interval} s apart, from the side
     * edges ({@code left}, {@code right} or {@code alternating}): the first when the boss settles,
     * then every {@code every} s while the phase lasts.
     */
    public record StreamData(String enemy, int count, double every, double interval, String edge) {
        public StreamData {
            Check.positive("count", count);
            Check.positive("every", every);
            Check.positive("interval", interval);
            Check.that(
                    edge.equals("left") || edge.equals("right") || edge.equals("alternating"),
                    "edge must be left, right or alternating, was '" + edge + "'");
        }
    }

    /**
     * A difficulty's change to one named attack. M8 part B (design/enemies/ground/drill-rig, hard): {@code aimed}, the
     * bullets fired straight at the ship besides a {@code ring}'s with each volley (the hard rig's aimed shard).
     */
    public record AttackChange(
            Optional<Integer> burst,
            Optional<Integer> count,
            Optional<Double> interval,
            Optional<Integer> arms,
            Optional<Double> within,
            Optional<Integer> aimed) {
        /** Without M8 part B's aimed shards. */
        public AttackChange(
                Optional<Integer> burst,
                Optional<Integer> count,
                Optional<Double> interval,
                Optional<Integer> arms,
                Optional<Double> within) {
            this(burst, count, interval, arms, within, Optional.empty());
        }

        public AttackChange {
            arms.ifPresent(a -> Check.positive("arms", a));
            within.ifPresent(w -> Check.positive("within", w));
            aimed.ifPresent(a -> Check.positive("aimed", a));
        }
    }

    /** Overrides of the global difficulty levers. */
    public record Hooks(Optional<Hook> easy, Optional<Hook> hard) {}

    /**
     * A difficulty's changes to the stat block.
     *
     * @param leadsTargetIn selected units in these formations lead the target
     * @param fanCount a fan's bullets instead
     * @param divePause a dive's pause instead, s
     * @param burst shots per volley instead of one
     * @param ring a mine's ring bullets instead
     * @param mineBursts false: mines never burst on their own
     * @param deathBurst the puff of bullets it pops into when destroyed
     * @param attacks changes per attack name (a boss's {@code burst} or {@code count}, an {@code interval}, a spiral's {@code arms})
     * @param sweepArc a laser sweep's arc instead, °
     * @param segments a segment chain's segments instead
     * @param regrownFanCount a regrown head's fan bullets instead
     * @param spawns part G: a boss's window spawns' counts instead, by spawn name
     * @param hoverSeconds M5 part D: the hover's time instead, s (the Wraith's hard hold)
     * @param telegraph M5 part E: an arena boss's slam telegraph instead, s (hard's 0.8)
     * @param lanes M5 part E: the lanes an arena boss's volley slams instead (hard's 3)
     * @param drain M5 part F: a drain's rate instead, shield points a second (the Lamprey's hard 12)
     * @param latchSlots M5 part F: a latch's slots instead (the Lamprey's hard 2)
     * @param burrow M7 part B: the burrow's surfaced time and rings instead (the Burrower's easy and hard)
     * @param swoopSpeed M7 part B: a triggered swoop's top speed instead, px/s (the Dust Kite's hard 230)
     * @param pores M7 part C: a pore wave's rest or every other pore instead (the Threadcrawler's easy and hard)
     * @param aura M7 part D: an aura's radius or refill instead (the Choir Herald's easy 75 px and 15 %/s, hard 105 px)
     * @param roll M7 part D: a curl-roll's roll time instead (the Shellback's hard 3.0 s)
     * @param vortex M7 part E: a spin cycle's open window instead (the Dust Devil's easy 1.0 s)
     * @param pull M7 part E: a pull's speed on the ship instead (the Dust Devil's easy 25, hard 45 px/s)
     * @param secondPass M7 part E: a lane strafe's line-abreast survivors turn at the edge and strafe back once (the
     *     Ghost Drone's hard)
     * @param turret M7 part F: a turret's turn rate instead (the Warden Tank's hard 160°/s)
     * @param homing M7 part F: homing missiles' turn rate instead (the Revenant Walker's hard 120°/s)
     * @param cone M7 part F: a flame cone's length, swivel or tell instead (the Revenant Walker's hard and easy)
     * @param pursuit M7 part G: a pursuit's time between lunges or tell instead (the Mote Swarm's easy and hard)
     * @param breach M7 part H: a breach cycle's bite tell instead (the Dust Colossus's easy 1.0 s, hard 0.6 s)
     * @param suction M7 part H: a suction's speed instead (the Dust Colossus's easy 30, hard 55 px/s)
     * @param eruption M7 part H: an eruption's marker time instead (the Dust Colossus's easy 1.8 s, hard 1.2 s)
     * @param ram M8 part C: a flock's ram share instead (the Glassfin's easy 0.15, hard 0.40)
     * @param coil M8 part E: a coiling orbit's open window instead (the Spiral Nautilus's easy 1.8, hard 1.4 s)
     * @param slow M8 part E: a slowing ring's factor, time or immunity instead (the Siren's easy 1.5 s, hard 2 s)
     * @param lunge M8 part F: a lunge's trigger distance instead (the Glow Angler's easy 100, hard 160 px)
     * @param coilArena M8 part H: a coiled boss's changes instead (the Abyssal Maw's sweep tell, shadow, implants' open
     *     time, shed segments and wall gaps; {@link MawBlock.CoilArenaChange})
     */
    public record Hook(
            Optional<List<String>> leadsTargetIn,
            Optional<Integer> fanCount,
            Optional<Double> divePause,
            Optional<Integer> burst,
            Optional<Integer> ring,
            Optional<Boolean> mineBursts,
            Optional<DeathBurst> deathBurst,
            Optional<Integer> spawnCount,
            Optional<Double> spawnAfter,
            Optional<java.util.Map<String, AttackChange>> attacks,
            Optional<Double> sweepArc,
            Optional<Integer> segments,
            Optional<Integer> regrownFanCount,
            Optional<java.util.Map<String, Integer>> spawns,
            Optional<Double> hoverSeconds,
            Optional<Double> telegraph,
            Optional<Integer> lanes,
            Optional<Double> drain,
            Optional<Integer> latchSlots,
            Optional<BurrowChange> burrow,
            Optional<Double> swoopSpeed,
            Optional<PoresChange> pores,
            Optional<AuraChange> aura,
            Optional<RollChange> roll,
            Optional<VortexChange> vortex,
            Optional<PullChange> pull,
            Optional<Boolean> secondPass,
            Optional<TurretChange> turret,
            Optional<HomingChange> homing,
            Optional<ConeChange> cone,
            Optional<PursuitChange> pursuit,
            Optional<LanceChange> lance,
            Optional<StompChange> stomp,
            Optional<BreachChange> breach,
            Optional<SuctionChange> suction,
            Optional<EruptionChange> eruption,
            Optional<RamChange> ram,
            Optional<CoilChange> coil,
            Optional<SlowChange> slow,
            Optional<LungeChange> lunge,
            Optional<CoilArenaChange> coilArena) {
        public Hook {
            swoopSpeed.ifPresent(s -> Check.positive("swoop_speed", s));
            drain.ifPresent(d -> Check.positive("drain", d));
            latchSlots.ifPresent(n -> Check.positive("latch_slots", n));
            hoverSeconds.ifPresent(h -> Check.positive("hover_seconds", h));
            telegraph.ifPresent(t -> Check.positive("telegraph", t));
            lanes.ifPresent(n -> Check.positive("lanes", n));
        }
    }

    /** {@code count} bullets of class {@code bullet} in a ring at {@code speed} px/s. */
    public record DeathBurst(int count, double speed, String bullet) {
        public DeathBurst {
            Check.positive("count", count);
            Check.positive("speed", speed);
        }
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
