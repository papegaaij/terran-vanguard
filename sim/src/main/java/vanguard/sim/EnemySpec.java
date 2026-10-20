package vanguard.sim;

import java.util.Optional;

/**
 * An enemy's stat block (design/enemies/&lt;category&gt;/&lt;slug&gt;/data.yaml) at one
 * difficulty, built by {@code vanguard.content.SimSpecs}: the HP already scaled, the movement
 * numbers its formations use, its gun and its drop rule.
 *
 * @param slug its name in the design data, e.g. {@code needler}
 * @param hp hit points in damage units
 * @param hitbox the hit box (the sprite is larger)
 * @param layer the layer it flies on
 * @param contactDamage damage of ramming the player
 * @param destroyedByRamming whether ramming the ship destroys it ({@code tiny} and {@code small} units)
 * @param bounty credits at medium in Act 1 terms
 * @param speed its own speed in px/s
 * @param snake the gap between two units of a snake
 * @param streamSpeed its speed in streams
 * @param hover how long and how far below the top edge it hovers
 * @param orbit its orbit in circle formations
 * @param gun its attack; none for rammers
 * @param drop the pickup every n-th kill drops
 * @param dive how it dives, for a diver
 * @param terrain whether it is fixed to the ground layer and moves only with the scroll
 * @param spiral how it spirals out of a whirl cluster
 * @param strafe how far below the top edge a convoy unit turns across the screen
 * @param deathBurst the puff of bullets it pops into when destroyed; none for most
 * @param sine a side-to-side offset on its path (the Brood Pod's drift)
 * @param brood the units it releases when it is destroyed or bursts on its own (a spawner)
 * @param walker how a walker (the Scuttler) walks its authored ground path, its frontal armour and
 *     its second attack
 * @param sideHover a unit entering from a side edge (the Mantis): where it hovers and how it leaves
 * @param sweep its laser sweep (the Mantis), fired while it hovers instead of a gun
 * @param chain the segment chain behind its head (the Coilwyrm)
 * @param hardened M5 part C (design/enemies/ground/hive-node): only {@code anti-ground} deliveries,
 *     the Airstrike and the Smart Bomb damage it; other shots and blasts glance off, and homing
 *     shots and the wingman do not pick it as a target unless their weapon is {@code anti-ground}
 * @param spawner M5 part C: a periodic spawner's cycle (the Hive Node)
 * @param pounce M5 part C: a walker's leap at the ship (the Ravager)
 * @param cloak M5 part D: its cloak (design/enemies/air/wraith): its stat block's {@code layer} is the
 *     cloaked one, the cloak's from its decloak on
 * @param ambush M5 part D: the path of a {@code rear ambush} wave's unit (the Wraith)
 * @param flock M5 part D: how the members of a {@code swarm} wave steer round its leader point (the
 *     Mote Swarm)
 * @param submerge M5 part E: how it surfaces and submerges (design/enemies/naval/driftjelly): its
 *     current layer swaps between its stat block's ({@code ground}) and {@link Layer#SUB}
 * @param ring M5 part E: its proximity ring (the Driftjelly), fired instead of a gun
 * @param drift M5 part E: a ground unit's drift along its nest's current on top of the scroll, px/s
 *     (the Reef Spitter's raft); 0 for none. A {@code field} wave's units drift at their {@code speed}
 * @param chase M5 part F: it homes on the ship after its entry (design/enemies/air/lamprey), then flies
 *     straight on
 * @param latch M5 part F: it latches onto the ship's hull on contact and drains the shield (the
 *     Lamprey); a unit with a latch takes the level's wind in every state but latched
 * @param machine M7 part A: a machine (its stat block's {@code machine: true}), not a biomechanical
 *     Vrell unit: the EMP Burst stuns it for the machine stun (design/player/specials, EMP Burst)
 * @param burrow M7 part B: it swims under the sand and surfaces near the ship (design/enemies/ground/
 *     burrower): its current layer is {@link Layer#BURIED} but while it is up ({@link Layer#GROUND})
 * @param swoop M7 part B: its swoop across the ship's lane once it nears the ship's row (design/enemies/
 *     air/dust-kite)
 * @param link M7 part C: the tendril it spans with its partner in a gate (design/enemies/ground/
 *     tendril-anchor): a {@link GroundSpec.Gate}'s anchor
 * @param crawl M7 part C: the ground path its chain's head crawls and the pore wave of its segments
 *     (design/enemies/ground/threadcrawler); a chain's head only
 * @param aura M7 part D: the shield rings it sings onto the other units near it (design/enemies/air/
 *     choir-herald); a unit with one is also a carrier its escorts circle
 * @param part M7 part D: its destroyable part, on a unit that is not a boss (design/enemies/ground/shellback,
 *     the mortar vent); {@link #hp()} and {@link #bounty()} are then the body's
 * @param roll M7 part D: a walker's curl-roll (design/enemies/ground/shellback)
 * @param vortex M7 part E: its vortex cycle (design/enemies/ground/dust-devil): closed on {@code ground}, it
 *     rises and opens on {@link Layer#LOW_AIR}; it wanders round its centre line and its vanes are armoured
 *     while it is not open
 * @param pull M7 part E: its radial pull on the ship and on the loose pickups (the Dust Devil); the pickups it
 *     reaches are held until its death
 * @param laneStrafe M7 part E: its lane strafe (design/enemies/air/ghost-drone): it drops to its lane, banks and
 *     strafes across; its gun fires one burst per pass by its pass trigger ({@link EnemyGun#passWithinX()})
 * @param turret M7 part F: its turret, turning independently of its hull (design/enemies/ground/warden-tank): its
 *     gun's bursts go along the turret's aim ({@link Enemy#turretAim()}), in any direction; a walker's with a gun
 * @param rearPlate M7 part F: its thin rear plate (the Warden Tank's): direct shots arriving within its half arc of
 *     its rear do its multiplier to the body; a walker's
 * @param seekersIgnore M7 part G (user decision 2026-10-20, the Mote Swarm): the seekers never pick it as a target:
 *     the player's homing shots and turrets, Rook's target; a shot that flies into it still hits
 */
public record EnemySpec(
        String slug,
        double hp,
        Hitbox hitbox,
        Layer layer,
        double contactDamage,
        boolean destroyedByRamming,
        int bounty,
        double speed,
        Optional<Snake> snake,
        Optional<Double> streamSpeed,
        Optional<Hover> hover,
        Optional<Orbit> orbit,
        Optional<EnemyGun> gun,
        Optional<Drop> drop,
        Optional<Dive> dive,
        boolean terrain,
        Optional<Spiral> spiral,
        Optional<Range> strafe,
        Optional<DeathBurst> deathBurst,
        Optional<Sine> sine,
        Optional<Brood> brood,
        Optional<Walker> walker,
        Optional<SideHover> sideHover,
        Optional<Sweep> sweep,
        Optional<ChainSpec> chain,
        boolean hardened,
        Optional<Spawner> spawner,
        Optional<Pounce> pounce,
        Optional<Cloak> cloak,
        Optional<Ambush> ambush,
        Optional<FlockSpec> flock,
        Optional<Submerge> submerge,
        Optional<ProximityRing> ring,
        double drift,
        Optional<Chase> chase,
        Optional<Latch> latch,
        boolean machine,
        Optional<Burrow> burrow,
        Optional<Swoop> swoop,
        Optional<Link> link,
        Optional<Crawl> crawl,
        Optional<Aura> aura,
        Optional<UnitPart> part,
        Optional<Roll> roll,
        Optional<Vortex> vortex,
        Optional<Pull> pull,
        Optional<LaneStrafe> laneStrafe,
        Optional<Turret> turret,
        Optional<RearPlate> rearPlate,
        boolean seekersIgnore) {
    public EnemySpec {
        if (turret.isPresent() && (walker.isEmpty() || gun.isEmpty() || chain.isPresent())) {
            throw new IllegalArgumentException(slug + ": a turret is a walker's with a gun (the Warden Tank)");
        }
        if (rearPlate.isPresent() && walker.isEmpty()) {
            throw new IllegalArgumentException(slug + ": a rear plate is a walker's (its facing is its path's)");
        }
        if (latch.isPresent() && chase.isEmpty()) {
            throw new IllegalArgumentException(slug + ": a unit that latches chases the ship first");
        }
        if (!(drift >= 0)) {
            throw new IllegalArgumentException(slug + ": a drift is 0 px/s or more: " + drift);
        }
        if (submerge.isPresent() && layer != Layer.GROUND) {
            throw new IllegalArgumentException(
                    slug + ": only a unit on the ground layer (the sea's surface) submerges");
        }
        if (burrow.isPresent() && (layer != Layer.GROUND || submerge.isPresent() || gun.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": a burrower is a ground unit without a submerge or a gun (its rings are its burrow's)");
        }
        if (swoop.isPresent() && (layer != Layer.AIR || burrow.isPresent() || chase.isPresent())) {
            throw new IllegalArgumentException(slug + ": a swoop is an air unit's, without a chase");
        }
        if (crawl.isPresent() && chain.isEmpty()) {
            throw new IllegalArgumentException(slug + ": a crawl is a segment chain's head's");
        }
        if (link.isPresent() && (chain.isPresent() || burrow.isPresent())) {
            throw new IllegalArgumentException(slug + ": a gate's anchor is a plain ground unit");
        }
        if (aura.isPresent() && (chain.isPresent() || brood.isPresent())) {
            throw new IllegalArgumentException(slug + ": an aura is a plain unit's (not a chain's or a spawner's)");
        }
        if (part.isPresent() && chain.isPresent()) {
            throw new IllegalArgumentException(slug + ": a chain's parts are its members, not a unit part");
        }
        if (roll.isPresent() && walker.isEmpty()) {
            throw new IllegalArgumentException(slug + ": a curl-roll is a walker's");
        }
        if (vortex.isPresent()
                && (layer != Layer.GROUND
                        || walker.isPresent()
                        || chain.isPresent()
                        || burrow.isPresent()
                        || submerge.isPresent()
                        || flock.isPresent())) {
            throw new IllegalArgumentException(slug
                    + ": a vortex is a plain ground unit's (not a walker's, a chain's, a burrower's or a flock's)");
        }
        if (laneStrafe.isPresent()
                && (layer != Layer.AIR
                        || walker.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || vortex.isPresent()
                        || swoop.isPresent()
                        || chase.isPresent()
                        || spiral.isPresent())) {
            throw new IllegalArgumentException(slug
                    + ": a lane strafe is a plain air unit's (not a walker's, a chain's, a flock's, a swooper's or a"
                    + " chaser's)");
        }
        if (laneStrafe.isPresent() && gun.isPresent() && Double.isNaN(gun.get().passWithinX())) {
            throw new IllegalArgumentException(slug + ": a lane strafer's gun fires by a pass trigger");
        }
    }

    /** The constructor without M7 part G's {@code seekersIgnore}: every seeker may pick it. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop,
            Optional<Link> link,
            Optional<Crawl> crawl,
            Optional<Aura> aura,
            Optional<UnitPart> part,
            Optional<Roll> roll,
            Optional<Vortex> vortex,
            Optional<Pull> pull,
            Optional<LaneStrafe> laneStrafe,
            Optional<Turret> turret,
            Optional<RearPlate> rearPlate) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                false);
    }

    /** The constructor of M7 part E's Ghost Drone (step E2): without M7 part F's turret and rear plate. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop,
            Optional<Link> link,
            Optional<Crawl> crawl,
            Optional<Aura> aura,
            Optional<UnitPart> part,
            Optional<Roll> roll,
            Optional<Vortex> vortex,
            Optional<Pull> pull,
            Optional<LaneStrafe> laneStrafe) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                Optional.empty(),
                Optional.empty());
    }

    /** M7 part F: this walker with its turret (design/enemies/ground/warden-tank); empty for none. */
    public EnemySpec withTurret(Optional<Turret> withTurretSpec) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                withTurretSpec,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part F: this walker with its rear plate (design/enemies/ground/warden-tank); empty for none. */
    public EnemySpec withRearPlate(Optional<RearPlate> withPlate) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                withPlate,
                seekersIgnore);
    }

    /**
     * M7 part F, a turret turning independently of its unit's hull (design/enemies/ground/warden-tank): it turns
     * toward its target (the ship, or the objective the hook picks) at {@code turnRate} radians per second all
     * round, while the unit is on the screen; once its gun is ready and its aim is within {@code withinRadians} of
     * the target it stops turning for {@code lockSeconds} (the tell) and then fires its burst along its aim.
     */
    public record Turret(double turnRate, double lockSeconds, double withinRadians) {
        public Turret {
            if (!(turnRate > 0) || !(lockSeconds >= 0) || !(withinRadians >= 0)) {
                throw new IllegalArgumentException("a turret turns, locks 0 s or more and aims within an angle");
            }
        }
    }

    /**
     * M7 part F, a walker's thin rear plate (design/enemies/ground/warden-tank): a direct shot (a bolt, a missile,
     * a beam) arriving within {@code halfArcRadians} of its rear (flying along its facing) does {@code multiplier}
     * × its damage to the body; a unit part's damage is its own.
     */
    public record RearPlate(double halfArcRadians, double multiplier) {
        public RearPlate {
            if (!(halfArcRadians >= 0) || !(halfArcRadians <= StrictMath.PI) || !(multiplier > 0)) {
                throw new IllegalArgumentException("a rear plate has an arc and a multiplier");
            }
        }
    }

    /** The constructor of M7 part E's Dust Devil (step E1): without the Ghost Drone's lane strafe. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop,
            Optional<Link> link,
            Optional<Crawl> crawl,
            Optional<Aura> aura,
            Optional<UnitPart> part,
            Optional<Roll> roll,
            Optional<Vortex> vortex,
            Optional<Pull> pull) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                Optional.empty());
    }

    /** The constructor of M7 part D: without M7 part E's vortex and pull. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop,
            Optional<Link> link,
            Optional<Crawl> crawl,
            Optional<Aura> aura,
            Optional<UnitPart> part,
            Optional<Roll> roll) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                Optional.empty(),
                Optional.empty());
    }

    /** The constructor of M7 part C: without M7 part D's aura, unit part and curl-roll. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop,
            Optional<Link> link,
            Optional<Crawl> crawl) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /** The constructor of M7 part B: without M7 part C's link and crawl. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine,
            Optional<Burrow> burrow,
            Optional<Swoop> swoop) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                Optional.empty(),
                Optional.empty());
    }

    /** M7 part C: this unit with its {@code link}, the tendril of a gate's anchor (design/enemies/ground/tendril-anchor); empty for none. */
    public EnemySpec withLink(Optional<Link> withLink) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                withLink,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part D: this unit with its {@code aura} (design/enemies/air/choir-herald): it rings the other units
     * near it with shields ({@link Enemy#ring()}); empty for none.
     */
    public EnemySpec withAura(Optional<Aura> withAura) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                withAura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part D: this unit with its {@code part}, a destroyable part on a unit that is not a boss
     * (design/enemies/ground/shellback, the mortar vent); empty for none.
     */
    public EnemySpec withPart(Optional<UnitPart> withUnitPart) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                withUnitPart,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part E: this ground unit with its vortex cycle (design/enemies/ground/dust-devil); empty for none. */
    public EnemySpec withVortex(Optional<Vortex> withVortex) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                withVortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part E: this unit with its radial pull on the ship and the pickups (the Dust Devil); empty for none. */
    public EnemySpec withPull(Optional<Pull> withPull) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                withPull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part E: this air unit with its lane strafe (design/enemies/air/ghost-drone); empty for none. Its gun, if
     * any, needs a pass trigger ({@link EnemyGun#passWithinX()}).
     */
    public EnemySpec withLaneStrafe(Optional<LaneStrafe> withLaneStrafe) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                withLaneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part D: this walker with its curl-roll (design/enemies/ground/shellback); empty for none. */
    public EnemySpec withRoll(Optional<Roll> withRoll) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                withRoll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part C: this chain's head with its {@code crawl} (design/enemies/ground/threadcrawler); empty for none. */
    public EnemySpec withCrawl(Optional<Crawl> withCrawl) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                withCrawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** The constructor of M7 part A: without the burrows and swoops of Level 15 (M7 part B). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch,
            boolean machine) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                Optional.empty(),
                Optional.empty());
    }

    /** The constructor of Act 2: every unit biomechanical (Vrell), none a machine (M7 part A). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift,
            Optional<Chase> chase,
            Optional<Latch> latch) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                false);
    }

    /** The constructor of Level 11: without the chasers and latches of Level 12 (M5 part F). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock,
            Optional<Submerge> submerge,
            Optional<ProximityRing> ring,
            double drift) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                Optional.empty(),
                Optional.empty());
    }

    /** This unit with M5 part F's {@code chase} and {@code latch} (a test's own Lamprey). */
    public EnemySpec withChase(Chase withChase, Optional<Latch> withLatch) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                Optional.of(withChase),
                withLatch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part A: this unit as a machine or not (its stat block's {@code machine} key; design/enemies,
     * Stat block template): the EMP Burst stuns a machine longer than a Vrell unit.
     */
    public EnemySpec withMachine(boolean isMachine) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                isMachine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part G: this unit as one the seekers ignore or not (its stat block's {@code seekers_ignore}; user decision
     * 2026-10-20, the Mote Swarm): the player's homing shots and turrets and Rook never pick it as a target.
     */
    public EnemySpec withSeekersIgnore(boolean ignored) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                ignored);
    }

    /** M7 part B: this unit with its {@code burrow} (design/enemies/ground/burrower); empty for none. */
    public EnemySpec withBurrow(Optional<Burrow> withBurrow) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                withBurrow,
                swoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /** M7 part B: this unit with its {@code swoop} (design/enemies/air/dust-kite); empty for none. */
    public EnemySpec withSwoop(Optional<Swoop> withSwoop) {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                withSwoop,
                link,
                crawl,
                aura,
                part,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore);
    }

    /**
     * M7 part B, the {@code burrow} movement (design/enemies/ground/burrower; user decision Q1 = a: it is
     * fixed on the screen, not on the ground, while it swims, bulges and is up). A {@code field} wave's
     * unit appears buried above the top edge and scrolls in with the ground until it is {@link
     * SurfaceSpot#edgeMargin()} inside the top edge and no higher than {@link SurfaceSpot#aheadMax()} above the ship;
     * then it cycles: it picks a spot ({@link SurfaceSpot}) and swims to it under the sand at {@code swimSpeed}
     * px/s for a seeded {@code travelSeconds} (on until it arrives; it waits buried there when it
     * arrives early); its wave's stagger lets it bulge ({@code staggerSeconds} after the wave's last
     * bulge, and only while fewer than {@code maxUp} of its wave would be up when it surfaces); it bulges
     * for {@code bulgeSeconds} at the spot, is up ({@link Layer#GROUND}) for {@code surfacedSeconds},
     * firing {@code ring} (and {@code secondRing}, hard) at their times after it surfaced, then dives for
     * {@code diveSeconds}, buried again from the dive's middle. After {@code surfacings} surfacings, or
     * once it has lived {@code lifeSeconds} (not while up), it swims off the bottom edge under the sand
     * at its swim speed on top of the scroll: an escape, no bounty.
     */
    public record Burrow(
            double swimSpeed,
            SurfaceSpot spot,
            Range travelSeconds,
            double bulgeSeconds,
            double surfacedSeconds,
            double diveSeconds,
            int surfacings,
            BurrowRing ring,
            Optional<BurrowRing> secondRing,
            double staggerSeconds,
            int maxUp,
            double lifeSeconds) {
        public Burrow {
            if (!(swimSpeed > 0)
                    || !(travelSeconds.min() > 0)
                    || !(bulgeSeconds > 0)
                    || !(surfacedSeconds > 0)
                    || !(diveSeconds > 0)
                    || surfacings < 1
                    || !(staggerSeconds >= 0)
                    || maxUp < 1
                    || !(lifeSeconds > 0)) {
                throw new IllegalArgumentException(
                        "a burrow has a swim speed, travel, bulge, surfaced and dive times, surfacings, a stagger,"
                                + " a cap on units up and a life");
            }
            if (!(ring.atSeconds() < surfacedSeconds)
                    || (secondRing.isPresent() && !(secondRing.get().atSeconds() < surfacedSeconds))) {
                throw new IllegalArgumentException("a burrow's rings fire while it is up");
            }
        }
    }

    /**
     * M7 part B: where a Burrower surfaces (design/enemies/ground/burrower, Choosing a spot), picked as it
     * starts to swim: {@code aheadMin}–{@code aheadMax} px above the ship's centre and within ±{@code
     * across} px of its x (seeded), kept {@code edgeMargin} px inside the play field's edges and at least
     * {@code clearance} px from the ship's centre (checked again as the bulge starts); outside the level's
     * no-surface zones ({@link GroundSpec.NoSurface}): the nearest allowed point, else that surfacing is
     * skipped (it still counts).
     */
    public record SurfaceSpot(double aheadMin, double aheadMax, double across, double edgeMargin, double clearance) {
        public SurfaceSpot {
            if (!(aheadMin >= 0)
                    || !(aheadMax >= aheadMin)
                    || !(across >= 0)
                    || !(edgeMargin >= 0)
                    || !(clearance >= 0)) {
                throw new IllegalArgumentException(
                        "a spot has a band ahead of the ship, a spread, a margin and a clearance");
            }
        }
    }

    /**
     * M7 part B: a Burrower's ring: {@code count} bullets at {@code bulletSpeed} px/s, each dealing {@code
     * damage}, {@code atSeconds} after it surfaced (none while the ship is closer than bullets may spawn).
     */
    public record BurrowRing(int count, double bulletSpeed, double damage, double atSeconds) {
        public BurrowRing {
            if (count < 1 || !(bulletSpeed > 0) || !(damage >= 0) || !(atSeconds >= 0)) {
                throw new IllegalArgumentException("a burrow ring has bullets, a speed, a damage and a time");
            }
        }
    }

    /**
     * M7 part B, the {@code swoop} (design/enemies/air/dust-kite): flying its path, once its centre is
     * above the ship's centre by {@code triggerAbove} px or less, it leaves the path on one shallow curve at
     * {@code speed} px/s through the point where the ship was as the swoop started (aimed, no homing): a
     * quadratic curve whose control point lies {@code bend} px to its side of its start, halfway down; the
     * units of a wave bend to alternate sides (the first to the left). Past that point it flies straight
     * on along the curve's end and is gone once off the play field.
     */
    public record Swoop(double triggerAbove, double speed, double bend) {
        public Swoop {
            if (!(triggerAbove > 0) || !(speed > 0) || !(bend >= 0)) {
                throw new IllegalArgumentException("a swoop has a trigger distance, a speed and a bend");
            }
        }
    }

    /**
     * M5 part F, the {@code chase} movement (design/enemies/air/lamprey): from its entry (its centre
     * inside the play field, then a swarm member's start delay) it steers toward the ship's centre at
     * {@code speed} px/s, turning at most {@code turnRate} radians a second, for {@code seconds}; then
     * it flies straight on along its heading at its speed and leaves the screen.
     */
    public record Chase(double speed, double turnRate, double seconds) {
        public Chase {
            if (!(speed > 0) || !(turnRate > 0) || !(seconds > 0)) {
                throw new IllegalArgumentException("a chase has a speed, a turn rate and a time");
            }
        }
    }

    /**
     * M5 part F, the {@code latch} block with the {@code drain} attack (design/enemies/air/lamprey; user
     * decisions F3 and F4 = a). Touching the ship while it chases or flies on, with fewer than
     * {@code slots} units latched, it latches at a hull spot ({@link #spots}, indexed by {@link #LEFT},
     * {@link #RIGHT} and {@link #TAIL}: the tail for a touch within 45 degrees of straight down from the
     * ship's centre, else the side by its x, a tie to the left) and moves with the ship, draining
     * {@code drainRate} shield points a second (the shield only, its regeneration paused, nothing once
     * it broke) and taking {@code damageFactor} times the damage. {@code reversals} reversals of the
     * commanded horizontal direction within {@code withinSeconds} throw off every latched unit: thrown
     * outward from its spot at {@code throwSpeed} px/s, slowing to a stop over {@code stunSeconds}, it
     * then flees at its speed to the nearer side edge and never latches again.
     *
     * @param spots the offsets of the left, right and tail spots from the ship's centre, px, y up
     */
    public record Latch(
            int slots,
            java.util.List<Spot> spots,
            double damageFactor,
            int reversals,
            double withinSeconds,
            double stunSeconds,
            double throwSpeed,
            double drainRate) {
        /** The spot on the hull's left side. */
        public static final int LEFT = 0;
        /** The spot on the hull's right side. */
        public static final int RIGHT = 1;
        /** The spot at the hull's tail. */
        public static final int TAIL = 2;

        public Latch {
            spots = java.util.List.copyOf(spots);
            if (slots < 1 || spots.size() != 3 || reversals < 2 || !(withinSeconds > 0) || !(stunSeconds > 0)) {
                throw new IllegalArgumentException("a latch has slots, the left, right and tail spots, a shake of at"
                        + " least two reversals within a time, and a stun");
            }
            if (!(damageFactor > 0) || throwSpeed < 0 || drainRate < 0) {
                throw new IllegalArgumentException(
                        "a latch's damage factor is positive, its throw and drain not negative");
            }
        }

        /** The hull spot for a touch at ({@code dx}, {@code dy}) from the ship's centre, y up. */
        public static int spotFor(double dx, double dy) {
            if (dy < 0 && Math.abs(dx) <= -dy) {
                return TAIL;
            }
            return dx <= 0 ? LEFT : RIGHT;
        }
    }

    /** A hull spot's offset from the ship's centre, px, y up. */
    public record Spot(double dx, double dy) {}

    /** The constructor of Level 10: without the submerging, proximity rings and drifting rafts of Level 11 (M5 part E). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce,
            Optional<Cloak> cloak,
            Optional<Ambush> ambush,
            Optional<FlockSpec> flock) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                Optional.empty(),
                Optional.empty(),
                0);
    }

    /**
     * M5 part E, surfacing and submerging (design/enemies/naval/driftjelly; the stated defaults of
     * 2026-10-08): every {@code everyMin}–{@code everyMax} s, drawn per unit from its own seed on the
     * simulation's real steps (so it keeps going in a halted arena), the unit swaps between the
     * surface ({@link Layer#GROUND}) and {@link Layer#SUB} over {@code swapSeconds}, its current
     * layer flipping at the swap's middle; a {@code field} wave starts the share {@code start} of its
     * units submerged.
     */
    public record Submerge(double everyMin, double everyMax, double swapSeconds, double start) {
        public Submerge {
            if (!(everyMin > 0) || !(everyMax >= everyMin) || !(swapSeconds > 0) || !(start >= 0 && start <= 1)) {
                throw new IllegalArgumentException(
                        "a submerge swaps every [min, max] s (0 < min <= max) over a swap time, a share starting submerged");
            }
        }
    }

    /**
     * M5 part E, a proximity ring (design/enemies/naval/driftjelly, user decision E3 = b): while the
     * ship's centre is within {@code within} px of its centre (on either of its layers) it fires a
     * ring of {@code count} bullets at {@code bulletSpeed} px/s, each dealing {@code damage}, at most
     * once every {@code cooldownSeconds} (counted from the last ring). A ring due while the ship is
     * closer than the bullets may spawn waits (design/enemies, bullet readability rules).
     */
    public record ProximityRing(int count, double bulletSpeed, double damage, double within, double cooldownSeconds) {
        public ProximityRing {
            if (count < 1 || !(bulletSpeed > 0) || !(within > 0) || !(cooldownSeconds > 0)) {
                throw new IllegalArgumentException("a proximity ring has bullets, a speed, a reach and a cooldown");
            }
        }
    }

    /** The constructor of Level 09: without the cloaks, ambushes and flocks of Level 10 (M5 part D). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain,
            boolean hardened,
            Optional<Spawner> spawner,
            Optional<Pounce> pounce) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /**
     * M5 part D, a cloak (design/enemies/air/wraith): the unit flies on its stat block's layer (the
     * cloaked one, {@code high-air}: only homing hits it, no contact, the specials skip it) until it
     * decloaks at its hold point; from the start of the {@code flashSeconds} flash on its current
     * layer is {@code layer}, and its gun stays silent until the flash ends.
     */
    public record Cloak(Layer layer, double flashSeconds) {
        public Cloak {
            if (!(flashSeconds >= 0)) {
                throw new IllegalArgumentException("a decloak flash lasts 0 s or more: " + flashSeconds);
            }
        }
    }

    /**
     * M5 part D, the path of a {@code rear ambush} wave's unit (design/enemies/air/wraith): it enters
     * at the top edge in its lane and flies straight down it at its speed, past the ship and off the
     * bottom edge; {@code gapSeconds} below it, it comes back up its lane to its {@link Hover} depth
     * (the hold point), decloaks there (its {@link Cloak}'s flash) and holds its hover time, its gun
     * counting its first-shot delay from the stop; then it leaves up the nearer side lane, its centre
     * {@code lane} px from that edge (a tie goes left), at {@code exitSpeed} px/s.
     */
    public record Ambush(double gapSeconds, double lane, double exitSpeed) {
        public Ambush {
            if (!(gapSeconds >= 0) || !(lane >= 0) || !(exitSpeed > 0)) {
                throw new IllegalArgumentException("an ambush has a gap, a lane and an exit speed");
            }
        }
    }

    /**
     * M5 part D, a flock (design/enemies/air/mote-swarm, user decision D8 = a): the members of a
     * {@code swarm} wave steer round a leader point that flies the wave's route. Each member keeps
     * {@code separation} px from the others, matches the heading of ({@code alignment}) and closes on
     * ({@code cohesion}) the members within {@code radius} px, and is pulled toward the leader point
     * ({@code leader}); it turns at most {@code turnRate} radians per second and flies about
     * {@code speed} px/s, {@code diveSpeed} after a loop-back. At most {@code max} members ({@link
     * Flock#MAX_MEMBERS} at the most).
     */
    public record FlockSpec(
            double separation,
            double radius,
            double alignment,
            double cohesion,
            double leader,
            double speed,
            double diveSpeed,
            double turnRate,
            int max) {
        public FlockSpec {
            if (!(separation > 0)
                    || !(radius >= separation)
                    || !(alignment >= 0)
                    || !(cohesion >= 0)
                    || !(leader > 0)
                    || !(speed > 0)
                    || !(diveSpeed > 0)
                    || !(turnRate > 0)
                    || max < 1
                    || max > Flock.MAX_MEMBERS) {
                throw new IllegalArgumentException("a flock has a separation inside its radius, a pull toward"
                        + " its leader, speeds, a turn rate and 1 to " + Flock.MAX_MEMBERS + " members");
            }
        }
    }

    /** The constructor of Level 08: without the hardened units, spawners and pounces of Level 09 (M5 part C). */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker,
            Optional<SideHover> sideHover,
            Optional<Sweep> sweep,
            Optional<ChainSpec> chain) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                false,
                Optional.empty(),
                Optional.empty());
    }

    /**
     * A periodic spawner (design/enemies/ground/hive-node, M5 part C): every {@code everySeconds},
     * counted from when its centre crosses the top edge, its iris opens over {@code
     * telegraphSeconds} and then releases {@code count} units of {@code enemy} from its centre,
     * spread evenly over {@code arcRadians} centred on the direction to the ship and flying straight
     * out at {@code speed} px/s (as a {@link Brood}'s). An opening due while the ship's centre is
     * within {@code shutWithin} px of its own is skipped (no telegraph), and so is a release the ship
     * came that close to during the telegraph; the next one is due a whole cycle later. It never
     * bursts on its own.
     */
    public record Spawner(
            EnemySpec enemy,
            int count,
            double everySeconds,
            double telegraphSeconds,
            double arcRadians,
            double speed,
            double shutWithin) {
        public Spawner {
            if (count < 1 || !(everySeconds > telegraphSeconds) || telegraphSeconds < 0 || shutWithin < 0) {
                throw new IllegalArgumentException(
                        "a spawner releases at least one unit, its telegraph inside its cycle");
            }
        }
    }

    /**
     * A pounce (design/enemies/ground/ravager, M5 part C): a walker on the screen whose centre is
     * within {@code range} px of the ship's leaps at the ship's position at take-off over {@code
     * leapSeconds} (no homing); for the middle {@code airSeconds} of the leap its current layer is
     * {@link Layer#AIR} (contact with the ship and the wingman once per leap, air-reaching weapons,
     * mines), else {@link Layer#GROUND}; drawn up to {@code scale} at the apex. It lands, rejoins its
     * path and may leap again {@code intervalSeconds} after landing.
     */
    public record Pounce(double range, double leapSeconds, double airSeconds, double scale, double intervalSeconds) {
        public Pounce {
            if (!(range > 0)
                    || !(leapSeconds > 0)
                    || airSeconds < 0
                    || airSeconds > leapSeconds
                    || !(scale > 0)
                    || intervalSeconds < 0) {
                throw new IllegalArgumentException("a pounce has a range, a leap and its air window inside it");
            }
        }
    }

    /** The constructor of Level 05: without the side hover, sweep and chain of Level 06. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst,
            Optional<Sine> sine,
            Optional<Brood> brood,
            Optional<Walker> walker) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /**
     * A unit that enters from a side edge and hovers {@code edgeX} px from it (design/enemies/air/mantis);
     * with {@code exitBack} it leaves through that edge when its hover ends.
     */
    public record SideHover(double edgeX, boolean exitBack) {}

    /**
     * A laser sweep (design/enemies/air/mantis): while it hovers, every {@code intervalSeconds} (the
     * first {@code firstDelaySeconds} after it settles) a telegraph of {@code telegraphSeconds}
     * fixes the sweep's centre on the ship's bearing, clamped between straight inward and straight
     * down; then a beam {@code length} × {@code width} px from its eye sweeps {@code arcRadians}
     * over {@code sweepSeconds}, dealing {@code damage} at most once per sweep. A sweep starts only
     * if it ends before the hover does. The eye, where the beam and its telegraph start, is
     * {@code originIn} px from the unit's centre toward the field (mirrored on the right edge) and
     * {@code originDown} px down the screen.
     *
     * <p>Round 41 (the 2026-10-19 playtest, the Siege Spire's sweep; a boss's only): with a {@code
     * leadRadians}, the arc is not centred on the ship's bearing at the telegraph but starts that far from it
     * on the side the beam comes from, so the telegraph's line lies just beside the ship: a ship that crosses
     * it before the beam fires is behind the beam (safe), one that stands still or moves the other way is
     * swept. NaN (the default) centres the arc on the bearing.
     */
    public record Sweep(
            double arcRadians,
            double sweepSeconds,
            double telegraphSeconds,
            double length,
            double width,
            double intervalSeconds,
            double firstDelaySeconds,
            double damage,
            double originIn,
            double originDown,
            double leadRadians) {
        public Sweep {
            if (!Double.isNaN(leadRadians) && !(leadRadians >= 0 && leadRadians <= arcRadians)) {
                throw new IllegalArgumentException("a sweep's lead lies within its arc: " + leadRadians);
            }
        }

        /** A sweep centred on the ship's bearing (without round 41's lead). */
        public Sweep(
                double arcRadians,
                double sweepSeconds,
                double telegraphSeconds,
                double length,
                double width,
                double intervalSeconds,
                double firstDelaySeconds,
                double damage,
                double originIn,
                double originDown) {
            this(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    originIn,
                    originDown,
                    Double.NaN);
        }

        /** Round 41: this sweep with its first edge {@code radians} from the ship's bearing (NaN: centred). */
        public Sweep withLead(double radians) {
            return new Sweep(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    originIn,
                    originDown,
                    radians);
        }

        /** Round 41: whether its arc starts {@link #leadRadians()} from the ship's bearing (else centred on it). */
        public boolean leads() {
            return !Double.isNaN(leadRadians);
        }

        /** A sweep from the unit's centre. */
        public Sweep(
                double arcRadians,
                double sweepSeconds,
                double telegraphSeconds,
                double length,
                double width,
                double intervalSeconds,
                double firstDelaySeconds,
                double damage) {
            this(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    0,
                    0);
        }
    }

    /**
     * A segment chain (design/enemies/air/coilwyrm): the unit is its head, flying the wave's path;
     * behind it {@code segmentBoxes.size()} segments of {@code segment} (each with its own hit box)
     * and the {@code tail} follow the head's path history, member {@code i} at {@code offsets[i]} px
     * of history behind the head (the head's is 0). A segment's destruction cuts the chain: the
     * rear part grows a {@code regrown} head over {@code regrowSeconds} (once per chain, and not
     * when the rear part died meanwhile) and lunges at the ship at {@code regrowSpeed}; a second cut
     * kills the severed part from the cut backwards, one member every {@code popSeconds}, as the
     * head's death does with the whole chain. The tail pays {@code tailFirstBonus} more when it is
     * destroyed before any other part. The head is a weak point: every damage it takes is
     * multiplied by {@code headMultiplier} (its part's {@code multiplier}, like a set piece's part);
     * a regrown head's is 1. M7 part C: the head pays {@code headFirstBonus} more when it is destroyed
     * before any other part (design/enemies/ground/threadcrawler: a head-first kill pays the whole); a
     * ground chain ({@link Crawl}) never regrows, and its {@code regrown} may be null.
     */
    public record ChainSpec(
            EnemySpec segment,
            java.util.List<Hitbox> segmentBoxes,
            EnemySpec tail,
            int tailFirstBonus,
            EnemySpec regrown,
            double regrowSeconds,
            double regrowSpeed,
            java.util.List<Double> offsets,
            double popSeconds,
            double headMultiplier,
            int headFirstBonus) {
        public ChainSpec {
            segmentBoxes = java.util.List.copyOf(segmentBoxes);
            offsets = java.util.List.copyOf(offsets);
            if (offsets.size() != segmentBoxes.size() + 2) {
                throw new IllegalArgumentException("a chain has an offset per member: head, segments, tail");
            }
            if (!(headMultiplier > 0)) {
                throw new IllegalArgumentException("a chain's head multiplier is positive: " + headMultiplier);
            }
        }

        /** Without M7 part C's head-first bonus. */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds,
                double headMultiplier) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    0);
        }

        /** A chain whose head takes plain damage (multiplier 1). */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    1);
        }

        /** The members: head, segments and tail. */
        public int members() {
            return offsets.size();
        }
    }

    /**
     * M7 part C, a gate's tendril (design/enemies/ground/tendril-anchor, the {@code link} attack; M9 = a): the
     * line between the centres of the two anchors of a {@link GroundSpec.Gate}. It is dormant (harmless, a
     * flickering thread) for {@code dormantSeconds} after the pair enters at the top edge, then its touch
     * deals {@code contactDamage} to the ship (shield first) or Rook, with {@code cooldownSeconds} of its
     * own cooldown per craft (the ship is not pushed), for a hull within {@code band}/2 px of the line.
     * Either anchor's death breaks it; a {@code beam} damages it ({@code tendrilHp}, the beam going on past
     * it), and burning it through pays {@code burntBounty} (Act 1 terms). Every other shot, bomb, enemy and
     * bullet passes through it.
     */
    public record Link(
            double tendrilHp,
            double dormantSeconds,
            double contactDamage,
            double cooldownSeconds,
            double band,
            int burntBounty) {
        public Link {
            if (!(tendrilHp > 0)
                    || !(dormantSeconds >= 0)
                    || !(contactDamage >= 0)
                    || !(cooldownSeconds >= 0)
                    || !(band > 0)
                    || burntBounty < 0) {
                throw new IllegalArgumentException(
                        "a tendril has HP, a dormancy, a touch, a cooldown, a band and a bounty");
            }
        }
    }

    /**
     * M7 part C, the {@code crawl} movement of a ground chain's head (design/enemies/ground/threadcrawler):
     * the head crawls the ground path through the points ({@code xs[i]}, row {@code scrolls[i]}: the ground
     * scroll at which that row reaches the top edge, as a canyon wall's points; linear between them) at
     * {@code speed} px/s on top of the scroll, from the wave's time on, and straight on along its last leg
     * past the end; the members follow on its history in ground space and scroll with the ground. A cut
     * (a destroyed segment) stops the members behind it where they are; they die {@code cutSeconds} later,
     * from the cut backwards, one every {@link ChainSpec#popSeconds()}; it never regrows. Its segments fire
     * the {@code pores} wave.
     */
    public record Crawl(double speed, double[] scrolls, double[] xs, PoreWave pores, double cutSeconds) {
        public Crawl {
            scrolls = scrolls.clone();
            xs = xs.clone();
            java.util.Objects.requireNonNull(pores);
            if (scrolls.length < 2 || scrolls.length != xs.length) {
                throw new IllegalArgumentException("a crawl path has at least two points, each a row and an x");
            }
            for (int i = 1; i < scrolls.length; i++) {
                if (scrolls[i] == scrolls[i - 1] && xs[i] == xs[i - 1]) {
                    throw new IllegalArgumentException("a crawl path has no repeated point");
                }
            }
            if (!(speed > 0) || !(cutSeconds >= 0)) {
                throw new IllegalArgumentException("a crawl has a speed and a cut delay: " + speed + ", " + cutSeconds);
            }
        }

        /** A crawl whose cut part dies 1.0 s after the cut (the stat block's {@code cut.delay}). */
        public Crawl(double speed, double[] scrolls, double[] xs, PoreWave pores) {
            this(speed, scrolls, xs, pores, 1.0);
        }

        @Override
        public double[] scrolls() {
            return scrolls.clone();
        }

        @Override
        public double[] xs() {
            return xs.clone();
        }

        /** The path's points, without a copy (the chain reads them every step). */
        int points() {
            return xs.length;
        }

        double row(int i) {
            return scrolls[i];
        }

        double x(int i) {
            return xs[i];
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Crawl crawl
                    && speed == crawl.speed
                    && cutSeconds == crawl.cutSeconds
                    && pores.equals(crawl.pores)
                    && java.util.Arrays.equals(scrolls, crawl.scrolls)
                    && java.util.Arrays.equals(xs, crawl.xs);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    speed, cutSeconds, pores, java.util.Arrays.hashCode(scrolls), java.util.Arrays.hashCode(xs));
        }

        @Override
        public String toString() {
            return "Crawl[" + speed + ", " + java.util.Arrays.toString(scrolls) + ", " + java.util.Arrays.toString(xs)
                    + ", " + pores + ", " + cutSeconds + "]";
        }
    }

    /**
     * M7 part C, a ground chain's pore wave (design/enemies/ground/threadcrawler, Travelling wave): the
     * segments' pores fire one slow aimed spore each in turn, head to tail, {@code interval} s apart (every
     * other segment's only when {@code everyOther}: easy), each aimed at the ship as it fires, at {@code
     * bulletSpeed} px/s dealing {@code damage}; a pore glows for {@code glow} s before it fires (the tell),
     * and the next wave's first glow starts {@code rest} s after a wave's last pore fired. The first wave's
     * first glow starts as the chain enters the play field (default pending the user's review); a pore off
     * the play field, of a doomed or destroyed segment, or of a stunned chain does not fire (its turn
     * passes).
     */
    public record PoreWave(
            double interval, double rest, double glow, double bulletSpeed, double damage, boolean everyOther) {
        public PoreWave {
            if (!(interval > 0) || !(rest >= 0) || !(glow >= 0) || !(bulletSpeed > 0) || !(damage >= 0)) {
                throw new IllegalArgumentException("a pore wave has an interval, a rest, a glow, a speed and a damage");
            }
        }
    }

    /**
     * M7 part D, a Choir Herald's {@code aura} (design/enemies/air/choir-herald; M8 = a): while it lives, is on
     * the play field and the EMP Burst does not stun it, every other unit whose centre is within {@code radius}
     * px of its centre carries a shield ring of at most its HP at the level, capped at {@code cap}, which
     * absorbs damage before its HP. A unit entering the radius gets its full ring over {@code fillSeconds};
     * one that left it keeps its ring {@code keepSeconds}, then the ring fades (over {@code fillSeconds}); a
     * ring untouched for {@code refillDelaySeconds} refills at {@code refillShare} of its maximum a second
     * while the unit stays in range. At the Herald's death every ring it fed collapses over {@code
     * collapseSeconds}. Its escorts circle it at {@code escortRadiusShare} × the radius (the gap check's
     * default 0.8, pending round 40) so that they stay ringed. Never on another Herald, a chain's member,
     * a unit buried or under the water, nor a boss or set piece (those are no units).
     */
    public record Aura(
            double radius,
            double cap,
            double refillShare,
            double refillDelaySeconds,
            double fillSeconds,
            double keepSeconds,
            double collapseSeconds,
            double escortRadiusShare) {
        public Aura {
            if (!(radius > 0) || !(cap > 0) || !(refillShare >= 0) || !(refillDelaySeconds >= 0)) {
                throw new IllegalArgumentException("an aura has a radius, a cap and a refill: " + radius + ", " + cap);
            }
            if (!(fillSeconds > 0) || !(keepSeconds >= 0) || !(collapseSeconds > 0) || !(escortRadiusShare > 0)) {
                throw new IllegalArgumentException("an aura's fill and collapse take time: " + fillSeconds);
            }
        }
    }

    /**
     * M7 part D, a destroyable part on a unit that is not a boss (design/enemies/ground/shellback, the mortar
     * vent; part F's Warden Tank turret may reuse it): {@code hp} of its own, its {@code hitbox} centred
     * {@code dx} px to the unit's right and {@code dy} px towards its tail in its own frame (facing down the
     * screen), turning with the unit's facing in its 16 headings. Shots test it before the body; a blast
     * covering both hurts both. Destroyed, it pays {@code bounty} (as a chain's part does) and, with {@code
     * silencesGun}, the unit's gun stops for good. Killing the body while it stands pays the unit's bounty
     * plus {@code firstBonus} (the whole: the Shellback's 40 + 8).
     */
    public record UnitPart(
            String name,
            double hp,
            Hitbox hitbox,
            double dx,
            double dy,
            int bounty,
            boolean silencesGun,
            int firstBonus) {
        public UnitPart {
            if (!(hp > 0)) {
                throw new IllegalArgumentException(name + ": a unit part has HP: " + hp);
            }
        }

        /** A part without a whole-kill bonus. */
        public UnitPart(String name, double hp, Hitbox hitbox, double dx, double dy, int bounty, boolean silencesGun) {
            this(name, hp, hitbox, dx, dy, bounty, silencesGun, 0);
        }
    }

    /**
     * M7 part D, a walker's curl-roll (design/enemies/ground/shellback): once, when its HP falls to {@code
     * belowShare} of its stat block's (or below) without destroying it, it curls for {@code curlSeconds},
     * rolls {@code rollSeconds} along its walk path at {@code rollSpeed} px/s (on along its last heading past
     * the path's end), uncurls for {@code uncurlSeconds} and shows its belly for {@code bellySeconds}, taking
     * {@code bellyFactor} × the damage, then walks on. From the curl's start to the uncurl's end its hit box is
     * the {@code ball}, every damage glances off and its gun waits; it stands while it curls, uncurls and
     * shows its belly. The rolling ball crushes ground allies (one hit per roll per ally, round 40: where the
     * ball touches the ally's footprint) and destructible ground objects in its path, never enemies or the ship.
     * Round 40 R.e: while it drifts down the screen, a roll ends early once the scroll would carry it below {@code
     * bellyRow} px above the bottom edge before its uncurl ends, so its belly shows at that row or higher (0: never
     * early).
     */
    public record Roll(
            double belowShare,
            double curlSeconds,
            double rollSeconds,
            double rollSpeed,
            double uncurlSeconds,
            double bellySeconds,
            double bellyFactor,
            Hitbox ball,
            double bellyRow) {
        public Roll {
            if (!(belowShare > 0 && belowShare < 1) || !(rollSpeed > 0) || !(bellyFactor >= 1)) {
                throw new IllegalArgumentException("a roll starts below a share of the HP: " + belowShare);
            }
            if (!(bellyRow >= 0 && bellyRow < PlayField.HEIGHT)) {
                throw new IllegalArgumentException("a roll's belly row lies on the play field: " + bellyRow);
            }
        }

        /** A roll that always runs its full time (no belly row). */
        public Roll(
                double belowShare,
                double curlSeconds,
                double rollSeconds,
                double rollSpeed,
                double uncurlSeconds,
                double bellySeconds,
                double bellyFactor,
                Hitbox ball) {
            this(belowShare, curlSeconds, rollSeconds, rollSpeed, uncurlSeconds, bellySeconds, bellyFactor, ball, 0);
        }
    }

    /**
     * M7 part E, a vortex cycle (design/enemies/ground/dust-devil): a cycle of {@code closedSeconds} closed on
     * {@code ground} (its first part sinking after an opening), {@code riseSeconds} rising (the tell), then {@code
     * openSeconds} open on {@link Layer#LOW_AIR}, its core hittable and its spiral gun firing; then it closes and
     * sinks again. The units of a wave start their cycles {@code offsetSeconds} apart (by their place in the
     * wave). It wanders a seeded swirl at {@code wanderSpeed} px/s at most {@code wanderAmplitude} px either side
     * of its centre line, which scrolls with the ground, and never comes closer than {@code keepAway} px to the
     * ship's centre (it slides round the ship).
     */
    public record Vortex(
            double closedSeconds,
            double riseSeconds,
            double openSeconds,
            double offsetSeconds,
            double wanderSpeed,
            double wanderAmplitude,
            double keepAway) {
        public Vortex {
            if (!(closedSeconds > 0) || !(riseSeconds >= 0) || !(openSeconds > 0)) {
                throw new IllegalArgumentException(
                        "a vortex is closed and open a while: " + closedSeconds + ", " + openSeconds);
            }
            if (!(offsetSeconds >= 0) || !(wanderSpeed >= 0) || !(wanderAmplitude >= 0) || !(keepAway >= 0)) {
                throw new IllegalArgumentException("a vortex's offset, wander and keep-away are 0 or more");
            }
        }

        /** The whole cycle, s. */
        public double cycleSeconds() {
            return closedSeconds + riseSeconds + openSeconds;
        }
    }

    /**
     * M7 part E, a lane strafe (design/enemies/air/ghost-drone): a unit entering at the top drops straight down at
     * {@code descentSpeed} px/s to its lane, banks 90° (a quarter turn of {@link #BANK_RADIUS} px, its speed
     * easing to the strafe's) and strafes across at {@code strafeSpeed} px/s at a fixed height toward the far side
     * from where it entered, leaving through that side edge; a unit entering from a side edge strafes in at its
     * lane and leaves through the opposite edge. A wave's lanes lie {@code laneSpacing} px apart from the top of
     * {@code laneDepth} (px below the top edge), closer when the wave has more units than the band holds at that
     * spacing, unit i on lane i. With {@code secondPass} (hard) a unit that entered at the top turns at the far
     * edge (a half turn of {@link #RETURN_RADIUS} px toward the bottom) and strafes back once, its gun's pass
     * trigger armed again, leaving through the edge it came from.
     */
    public record LaneStrafe(
            double descentSpeed, double strafeSpeed, Range laneDepth, double laneSpacing, boolean secondPass) {
        /** The radius of the bank from the descent into the strafe, px (pending round 41). */
        public static final double BANK_RADIUS = 24;
        /**
         * The radius of the second pass's half turn at the edge, px: the strafe back runs twice this lower (one
         * lane of the approved 40 px spacing; pending round 41).
         */
        public static final double RETURN_RADIUS = 20;

        public LaneStrafe {
            if (!(descentSpeed > 0) || !(strafeSpeed > 0)) {
                throw new IllegalArgumentException("a lane strafe flies: " + descentSpeed + ", " + strafeSpeed);
            }
            if (!(laneDepth.min() > 0) || !(laneDepth.max() < PlayField.HEIGHT) || !(laneSpacing >= 0)) {
                throw new IllegalArgumentException("a lane strafe's lanes lie on the play field: " + laneDepth);
            }
        }

        /** The depth of lane {@code lane} of a wave of {@code lanes} units, px below the top edge. */
        public double depth(int lane, int lanes) {
            double step = lanes > 1 ? Math.min(laneSpacing, (laneDepth.max() - laneDepth.min()) / (lanes - 1)) : 0;
            return laneDepth.min() + lane * step;
        }
    }

    /**
     * M7 part E, a radial pull toward the unit's centre (design/enemies/ground/dust-devil): on the ship within
     * {@code shipRadius} px, {@code shipSpeed} px/s at the centre fading evenly to 0 at the edge, added to the
     * ship's step; on the loose pickups within {@code pickupRadius} px at {@code pickupSpeed} px/s (a fitted
     * Pickup magnet's pull wins where it is the stronger). A pickup that reaches the centre is held.
     */
    public record Pull(double shipRadius, double shipSpeed, double pickupRadius, double pickupSpeed) {
        public Pull {
            if (!(shipRadius >= 0) || !(shipSpeed >= 0) || !(pickupRadius >= 0) || !(pickupSpeed >= 0)) {
                throw new IllegalArgumentException("a pull's radii and speeds are 0 or more");
            }
        }

        /** Its speed on the ship at {@code distance} px from the centre, px/s; 0 at the edge and beyond. */
        public double shipSpeedAt(double distance) {
            return distance >= shipRadius ? 0 : shipSpeed * (1 - distance / shipRadius);
        }
    }

    /** A snake's units follow one another {@code spacingSeconds} apart. */
    public record Snake(double spacingSeconds) {}

    /** Hovers for {@code seconds}, {@code depth} px below the top edge of the play field. */
    public record Hover(Range seconds, Range depth) {}

    /** Circles a point at {@code radius} px and {@code degreesPerSecond}. */
    public record Orbit(double radius, double degreesPerSecond) {}

    /** Every {@code every}-th kill of this enemy in an attempt drops {@code pickup}. */
    public record Drop(PickupType pickup, int every) {}

    /**
     * Pauses {@code depth} px below the top edge for {@code pauseSeconds}, then dives at
     * {@code speed} px/s at where the ship was, firing once when it passes the ship's height or
     * {@code fireAfterSeconds} into the dive.
     */
    public record Dive(Range depth, double pauseSeconds, double speed, double fireAfterSeconds) {}

    /**
     * Circles its release point for {@code seconds} at {@code turnsPerSecond} while its radius grows
     * {@code growth} px/s and the release point drifts down {@code drift} px/s; then flies on at its
     * speed along its outward angle turned downward, bouncing off the side edges up to
     * {@code ricochets} times.
     */
    public record Spiral(double seconds, double turnsPerSecond, double growth, double drift, int ricochets) {}

    /** {@code count} bullets in a ring at {@code speed} px/s, each dealing {@code damage}. */
    public record DeathBurst(int count, double speed, double damage) {}

    /** Swings {@code amplitude} px to each side of its path, {@code periodSeconds} per full swing. */
    public record Sine(double amplitude, double periodSeconds) {}

    /**
     * A spawner (design/enemies/air/brood-pod): {@code afterSeconds} after its centre crosses the
     * top edge it bursts on its own (the pulse speeding up over the last {@code telegraphSeconds}),
     * paying {@code burstBounty} instead of its bounty, which is not a kill; destroyed or burst, it
     * releases {@code count} units of {@code enemy} from its centre, spread evenly over
     * {@code arcRadians} centred on the direction to the ship and flying straight out at
     * {@code speed} px/s.
     */
    public record Brood(
            EnemySpec enemy,
            int count,
            double afterSeconds,
            double telegraphSeconds,
            double arcRadians,
            double speed,
            int burstBounty) {}

    /**
     * A walker (design/enemies/ground/scuttler): it walks its wave's ground path at {@code speed}
     * px/s over the ground, its facing turning at most {@code turnRate} radians per second; one walk
     * cycle per {@code stride} px. Direct shots arriving within {@code frontArc} radians of its
     * facing glance off; its {@link EnemySpec#gun()} is a fan along its facing (the Scuttler) or, with
     * {@code fanAtShip}, centred on the ship (the Creeper, M5 part B), and {@code spit} an aimed
     * attack that fires only while the ship is more than {@code awayRadians} off its facing.
     *
     * @param staggerSeconds M5 part B (design/enemies/ground/creeper): with more than 0, the units of
     *     one wave share a volley clock that starts as the first of them comes onto the screen; unit
     *     i (in the order they enter) fires its fan i × this many seconds after the volley's start,
     *     the first volley half an interval after the clock starts and then one every interval; a
     *     unit off the screen skips its turn. 0: each unit keeps its own clock
     */
    public record Walker(
            double speed,
            double turnRate,
            double stride,
            double frontArc,
            Optional<EnemyGun> spit,
            double awayRadians,
            boolean fanAtShip,
            double staggerSeconds) {
        /** A walker whose fan goes along its facing, each unit on its own clock (Level 04's Scuttler). */
        public Walker(
                double speed,
                double turnRate,
                double stride,
                double frontArc,
                Optional<EnemyGun> spit,
                double awayRadians) {
            this(speed, turnRate, stride, frontArc, spit, awayRadians, false, 0);
        }

        /** Whether the units of a wave share a staggered volley clock. */
        public boolean staggered() {
            return staggerSeconds > 0;
        }
    }

    /** The constructor of Level 03: without the spawners and walkers of Level 04. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain,
            Optional<Spiral> spiral,
            Optional<Range> strafe,
            Optional<DeathBurst> deathBurst) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /** The current constructor with the movements and bursts of Level 03 left out. */
    public EnemySpec(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed,
            Optional<Snake> snake,
            Optional<Double> streamSpeed,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<EnemyGun> gun,
            Optional<Drop> drop,
            Optional<Dive> dive,
            boolean terrain) {
        this(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }
}
