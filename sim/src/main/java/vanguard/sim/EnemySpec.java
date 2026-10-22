package vanguard.sim;

import java.util.List;
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
 * @param parts M7 part D: its destroyable parts, on a unit that is not a boss (design/enemies/ground/shellback,
 *     the mortar vent; M8 part B: design/enemies/ground/drill-rig, its four pumps); {@link #hp()} and {@link
 *     #bounty()} are then the body's; at most {@link #MOST_PARTS}, empty for none
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
        List<UnitPart> parts,
        Optional<Roll> roll,
        Optional<Vortex> vortex,
        Optional<Pull> pull,
        Optional<LaneStrafe> laneStrafe,
        Optional<Turret> turret,
        Optional<RearPlate> rearPlate,
        boolean seekersIgnore)
        implements FlightSpecs, WalkerSpecs, ChainSpecs, LayerSpecs, AttackSpecs, HostSpecs {
    /** M8 part B: the most destroyable parts a unit carries (the Drill Rig's four pumps). */
    public static final int MOST_PARTS = 4;

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
        if (chain.isPresent() && chain.get().scatter().isPresent() && crawl.isPresent()) {
            throw new IllegalArgumentException(slug + ": a cut part scatters only in an air chain (M8 part C)");
        }
        if (chain.isPresent() && !chain.get().headAttacks().isEmpty() && gun.isPresent()) {
            throw new IllegalArgumentException(slug + ": a chain's head fires its head attacks or its gun, not both");
        }
        if (link.isPresent() && (chain.isPresent() || burrow.isPresent())) {
            throw new IllegalArgumentException(slug + ": a gate's anchor is a plain ground unit");
        }
        if (aura.isPresent() && (chain.isPresent() || brood.isPresent())) {
            throw new IllegalArgumentException(slug + ": an aura is a plain unit's (not a chain's or a spawner's)");
        }
        parts = List.copyOf(parts);
        if (parts.size() > MOST_PARTS) {
            throw new IllegalArgumentException(slug + ": a unit has at most " + MOST_PARTS + " parts: " + parts.size());
        }
        if (!parts.isEmpty() && chain.isPresent()) {
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
                part.stream().toList(),
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
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.turret = withTurretSpec;
        return copy.build();
    }

    /** M7 part F: this walker with its rear plate (design/enemies/ground/warden-tank); empty for none. */
    public EnemySpec withRearPlate(Optional<RearPlate> withPlate) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.rearPlate = withPlate;
        return copy.build();
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
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.link = withLink;
        return copy.build();
    }

    /**
     * M7 part D: this unit with its {@code aura} (design/enemies/air/choir-herald): it rings the other units
     * near it with shields ({@link Enemy#ring()}); empty for none.
     */
    public EnemySpec withAura(Optional<Aura> withAura) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.aura = withAura;
        return copy.build();
    }

    /**
     * M7 part D: this unit with its {@code part}, a destroyable part on a unit that is not a boss
     * (design/enemies/ground/shellback, the mortar vent); empty for none.
     */
    public EnemySpec withPart(Optional<UnitPart> withUnitPart) {
        return withParts(withUnitPart.stream().toList());
    }

    /**
     * M8 part B: this unit with its destroyable {@code parts} (design/enemies/ground/drill-rig, the four pumps round
     * its vital core), tested in their order; empty for none.
     */
    public EnemySpec withParts(List<UnitPart> withUnitParts) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.parts = withUnitParts;
        return copy.build();
    }

    /** M7 part D: its first destroyable part (the Shellback's vent); empty for a unit without one. */
    public Optional<UnitPart> part() {
        return parts.isEmpty() ? Optional.empty() : Optional.of(parts.getFirst());
    }

    /** M7 part E: this ground unit with its vortex cycle (design/enemies/ground/dust-devil); empty for none. */
    public EnemySpec withVortex(Optional<Vortex> withVortex) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.vortex = withVortex;
        return copy.build();
    }

    /** M7 part E: this unit with its radial pull on the ship and the pickups (the Dust Devil); empty for none. */
    public EnemySpec withPull(Optional<Pull> withPull) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.pull = withPull;
        return copy.build();
    }

    /**
     * M7 part E: this air unit with its lane strafe (design/enemies/air/ghost-drone); empty for none. Its gun, if
     * any, needs a pass trigger ({@link EnemyGun#passWithinX()}).
     */
    public EnemySpec withLaneStrafe(Optional<LaneStrafe> withLaneStrafe) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.laneStrafe = withLaneStrafe;
        return copy.build();
    }

    /** M7 part D: this walker with its curl-roll (design/enemies/ground/shellback); empty for none. */
    public EnemySpec withRoll(Optional<Roll> withRoll) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.roll = withRoll;
        return copy.build();
    }

    /** M7 part C: this chain's head with its {@code crawl} (design/enemies/ground/threadcrawler); empty for none. */
    public EnemySpec withCrawl(Optional<Crawl> withCrawl) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.crawl = withCrawl;
        return copy.build();
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
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.chase = Optional.of(withChase);
        copy.latch = withLatch;
        return copy.build();
    }

    /**
     * M7 part A: this unit as a machine or not (its stat block's {@code machine} key; design/enemies,
     * Stat block template): the EMP Burst stuns a machine longer than a Vrell unit.
     */
    public EnemySpec withMachine(boolean isMachine) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.machine = isMachine;
        return copy.build();
    }

    /**
     * M7 part G: this unit as one the seekers ignore or not (its stat block's {@code seekers_ignore}; user decision
     * 2026-10-20, the Mote Swarm): the player's homing shots and turrets and Rook never pick it as a target.
     */
    public EnemySpec withSeekersIgnore(boolean ignored) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.seekersIgnore = ignored;
        return copy.build();
    }

    /** M7 part B: this unit with its {@code burrow} (design/enemies/ground/burrower); empty for none. */
    public EnemySpec withBurrow(Optional<Burrow> withBurrow) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.burrow = withBurrow;
        return copy.build();
    }

    /** M7 part B: this unit with its {@code swoop} (design/enemies/air/dust-kite); empty for none. */
    public EnemySpec withSwoop(Optional<Swoop> withSwoop) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.swoop = withSwoop;
        return copy.build();
    }

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
