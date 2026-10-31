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
 * @param rise M8 part D: its rise (design/enemies/naval/abyss-ray): a shadow on {@code sub}, the rise to {@code air},
 *     the glide and the sink ({@link Rise}); its stat block's layer is {@code sub}
 * @param slowRing M8 part E: its sung ring that slows the ship (design/enemies/naval/siren; {@link SlowRing}); empty for
 *     none
 * @param coil M8 part E: its coiling orbit round the ship (design/enemies/naval/spiral-nautilus; {@link Coil}); empty
 *     for none
 * @param lunge M8 part F: its lunge from a lure (design/enemies/naval/glow-angler; {@link Lunge}); empty for none
 * @param shield M9 part A: its hex shield (design/enemies, Enemy shields; {@link Shield}); empty for none
 * @param laserLine M9 part A: its {@code laser-line} (design/enemies/ground/rail-bunker; {@link LaserLine}); empty for
 *     none
 * @param scatter M9 part B: how it breaks off when its wave's leader is killed (design/enemies/air/talon, Squad leader;
 *     {@link SquadScatter}); empty for none (it flies on)
 * @param ride M9 part B: it rides a rock of its wave (design/enemies, Rock riding; {@link Ride}); empty for none
 * @param neutral M9 part B (design/campaign Level 29, the wavering Stormhawks): a neutral craft, not an enemy: no gun, no
 *     contact, no bounty, never counted among the level's enemies (its kills, its total, the kill ratio), never a
 *     seeker's target, and the escort's shots pass through it; the player's shots and specials hit it ({@link
 *     #countsAsEnemy()})
 * @param mineLayer M9 part C: it lays mine lines (design/enemies/space/minelayer; {@link MineLayer}): up a side lane from
 *     the bottom edge to its lay row, then across, laying its gun's line mines; empty for none
 * @param facingArmour M9 part C: its facing armour (design/enemies/air/gilded-gunship; {@link FacingArmour}): a direct hit's
 *     factor by the direction it arrives from, after its shield; empty for none
 * @param launcher M9 part C: the units it launches in streams while on the play field (the Gilded Gunship's Dart
 *     Drones; {@link Launcher}); empty for none
 * @param dash M9 part D: its dash cycle (design/enemies/air/buzzsaw-drone; {@link Dash}): chase, spin-up tell, a
 *     locked dash that ricochets, again; empty for none
 * @param airLink M9 part D: its wave's energy links (design/enemies/space/shard-drone; {@link AirLink}); empty for none
 * @param core M9 part E (design/enemies/ground/sentinel-tower, the hardened collar): on a {@link #hardened()} unit, the
 *     box round its centre that every weapon damages (the emitter): a shot of a weapon without {@code anti-ground} that
 *     lands on the hit box outside it glances off, one that lands in it (or flies on into it) damages the unit, a beam
 *     whose line crosses it reaches it, and the seekers (homing shots, Rook) pick the unit and aim at its centre;
 *     empty for a plain hardened unit (or none)
 * @param groundSweep M9 part E (the Sentinel Tower): its {@link #sweep()} is a ground unit's, aimed all round on the
 *     ship's bearing and due while it is on the play field, every few sweeps a full turn ({@link GroundSweep}); empty
 *     for a hovering unit's sweep (the Mantis's) or none
 * @param topple M9 part E (design/enemies/ground/strider, The topple): a walker's stagger, fall and crush once a part
 *     that {@link UnitPart#topples()} is lost ({@link Topple}); empty for none
 * @param preyRole M9 part F (design/enemies/space/void-leech, The uneasy alliance; Level 33): a wave's marked prey (a
 *     hunting latch takes it instead of the ship: {@link PreyLatches}) or a chaser that hunts the nearest free prey
 *     instead of the ship ({@link PreyRole}); {@link PreyRole#NONE} for every other unit
 * @param mirror M10 part A (design/enemies, Reflect): its mirror over a frontal arc ({@link Mirror}, its state a
 *     {@link Mirroring}); empty for none. A unit carries a mirror or a hex shield, never both
 * @param regeneration M10 part A (design/enemies, Regeneration): its HP's refill after a quiet spell ({@link
 *     Regeneration}); empty for none
 * @param regrowth M10 part A (design/enemies/ground/graft-turret): its turret's regrowth over an exposed root ({@link
 *     Regrowth}); a fixed ground unit's; empty for none
 * @param dome M10 part A (design/enemies/ground/shield-pylon): the dome it projects over the ground units near it
 *     ({@link Dome}); a fixed ground unit's; empty for none
 * @param spoofable M10 part A (design/player/systems, the IFF spoofer): its aimed volleys and told attacks come late
 *     while the ship carries the spoofer ({@link Spoofing})
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
        boolean seekersIgnore,
        Optional<Rise> rise,
        Optional<SlowRing> slowRing,
        Optional<Coil> coil,
        Optional<Lunge> lunge,
        Optional<Shield> shield,
        Optional<LaserLine> laserLine,
        Optional<SquadScatter> scatter,
        Optional<Ride> ride,
        boolean neutral,
        Optional<SwoopPath> swoopPath,
        Optional<MineLayer> mineLayer,
        Optional<FacingArmour> facingArmour,
        Optional<Launcher> launcher,
        Optional<Dash> dash,
        Optional<AirLink> airLink,
        Optional<Hitbox> core,
        Optional<GroundSweep> groundSweep,
        Optional<Topple> topple,
        PreyRole preyRole,
        Optional<Mirror> mirror,
        Optional<Regeneration> regeneration,
        Optional<Regrowth> regrowth,
        Optional<Dome> dome,
        boolean spoofable)
        implements FlightSpecs, WalkerSpecs, ChainSpecs, LayerSpecs, AttackSpecs, HostSpecs, JovianSpecs {
    /** M8 part B: the most destroyable parts a unit carries (the Drill Rig's four pumps). */
    public static final int MOST_PARTS = 4;

    public EnemySpec {
        java.util.Objects.requireNonNull(mineLayer);
        java.util.Objects.requireNonNull(facingArmour);
        java.util.Objects.requireNonNull(launcher);
        java.util.Objects.requireNonNull(dash);
        java.util.Objects.requireNonNull(airLink);
        java.util.Objects.requireNonNull(core);
        java.util.Objects.requireNonNull(groundSweep);
        java.util.Objects.requireNonNull(topple);
        java.util.Objects.requireNonNull(preyRole);
        java.util.Objects.requireNonNull(mirror);
        java.util.Objects.requireNonNull(regeneration);
        java.util.Objects.requireNonNull(regrowth);
        java.util.Objects.requireNonNull(dome);
        if (mirror.isPresent() && (shield.isPresent() || chain.isPresent() || flock.isPresent() || !parts.isEmpty())) {
            throw new IllegalArgumentException(
                    slug + ": a mirror is a single unit's, never beside a hex shield (M10 part A, Reflect)");
        }
        if ((regrowth.isPresent() || dome.isPresent())
                && (layer != Layer.GROUND || !terrain || chain.isPresent() || walker.isPresent() || !parts.isEmpty())) {
            throw new IllegalArgumentException(
                    slug + ": a regrowth or a dome is a fixed single ground unit's (M10 part A)");
        }
        if (regrowth.isPresent() && dome.isPresent()) {
            throw new IllegalArgumentException(slug + ": a pylon does not regrow (M10 part A)");
        }
        if (preyRole == PreyRole.PREY
                && (layer != Layer.AIR
                        || terrain
                        || latch.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent())) {
            throw new IllegalArgumentException(slug + ": prey is a plain air unit without a latch (M9 part F)");
        }
        if (preyRole == PreyRole.HUNTER
                && (chase.isEmpty() || latch.isEmpty() || !latch.get().hunts())) {
            throw new IllegalArgumentException(
                    slug + ": a prey hunter is a chaser whose latch takes prey (M9 part F, the Void Leech)");
        }
        if (core.isPresent()
                && (!hardened
                        || core.get().width() > hitbox.width()
                        || core.get().height() > hitbox.height())) {
            throw new IllegalArgumentException(slug + ": a core lies inside a hardened unit's hit box (M9 part E)");
        }
        if (groundSweep.isPresent() && (sweep.isEmpty() || !terrain || hover.isPresent() || sideHover.isPresent())) {
            throw new IllegalArgumentException(slug + ": a ground sweep is a fixed ground unit's sweep (M9 part E)");
        }
        boolean toppling = parts.stream().anyMatch(UnitPart::topples);
        if (toppling != topple.isPresent() || (topple.isPresent() && walker.isEmpty())) {
            throw new IllegalArgumentException(slug + ": a walker topples with a part that topples it (M9 part E)");
        }
        if (parts.stream().anyMatch(UnitPart::silencesSpit)
                && (walker.isEmpty() || walker.get().spit().isEmpty())) {
            throw new IllegalArgumentException(
                    slug + ": only a walker's second attack is silenced by a part (M9 part E)");
        }
        if (dash.isPresent()
                && (layer != Layer.AIR
                        || terrain
                        || gun.isPresent()
                        || hover.isPresent()
                        || dive.isPresent()
                        || swoop.isPresent()
                        || swoopPath.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent()
                        || laneStrafe.isPresent()
                        || chase.isPresent()
                        || spiral.isPresent()
                        || ride.isPresent()
                        || coil.isPresent()
                        || lunge.isPresent()
                        || rise.isPresent()
                        || mineLayer.isPresent()
                        || launcher.isPresent())) {
            throw new IllegalArgumentException(slug
                    + ": a dash is a plain air unit's without a gun, its dash cycle its only movement after its entry"
                    + " (M9 part D, the Buzzsaw Drone)");
        }
        if (airLink.isPresent()
                && (layer != Layer.AIR
                        || terrain
                        || chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent()
                        || dash.isPresent()
                        || chase.isPresent()
                        || mineLayer.isPresent()
                        || launcher.isPresent()
                        || link.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": an air link joins plain air units of one wave (M9 part D, the Shard Drone)");
        }
        if (mineLayer.isPresent()
                && (layer != Layer.AIR
                        || gun.isEmpty()
                        || gun.get().mine().isEmpty()
                        || gun.get().mine().get().line().isEmpty()
                        || terrain
                        || hover.isPresent()
                        || dive.isPresent()
                        || swoop.isPresent()
                        || swoopPath.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent()
                        || laneStrafe.isPresent()
                        || chase.isPresent()
                        || spiral.isPresent()
                        || ride.isPresent()
                        || coil.isPresent()
                        || lunge.isPresent()
                        || rise.isPresent()
                        || sideHover.isPresent()
                        || ambush.isPresent()
                        || launcher.isPresent())) {
            throw new IllegalArgumentException(slug
                    + ": a mine layer is a plain air unit whose gun lays line mines, its lay path its only movement"
                    + " (M9 part C, the Minelayer)");
        }
        if (facingArmour.isPresent() && (walker.isPresent() || chain.isPresent() || rearPlate.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": facing armour is a plain flying unit's (a walker has its frontal armour and rear plate)");
        }
        if (launcher.isPresent()
                && (chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent()
                        || spawner.isPresent()
                        || brood.isPresent()
                        || ring.isPresent()
                        || terrain)) {
            throw new IllegalArgumentException(
                    slug + ": a launcher is a plain flying unit's, not a spawner's, a brood's or a ring's (M9 part C)");
        }
        java.util.Objects.requireNonNull(shield);
        java.util.Objects.requireNonNull(laserLine);
        java.util.Objects.requireNonNull(scatter);
        java.util.Objects.requireNonNull(ride);
        java.util.Objects.requireNonNull(swoopPath);
        if (swoopPath.isPresent()
                && (layer != Layer.AIR
                        || terrain
                        || hover.isPresent()
                        || dive.isPresent()
                        || swoop.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || walker.isPresent()
                        || laneStrafe.isPresent()
                        || chase.isPresent()
                        || spiral.isPresent()
                        || ride.isPresent()
                        || coil.isPresent()
                        || lunge.isPresent()
                        || rise.isPresent()
                        || sideHover.isPresent()
                        || ambush.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": a swoop path is a plain air unit's, its only movement (M9 part B, the Talon)");
        }
        if (neutral && (gun.isPresent() || contactDamage != 0 || bounty != 0 || !seekersIgnore)) {
            throw new IllegalArgumentException(
                    slug + ": a neutral craft has no gun, no contact, no bounty, and the seekers ignore it");
        }
        if (ride.isPresent()
                && (layer != Layer.AIR || gun.isPresent() || chain.isPresent() || flock.isPresent() || terrain)) {
            throw new IllegalArgumentException(slug + ": a rider is a plain air unit without a gun (M9 part B)");
        }
        if (scatter.isPresent() && (chain.isPresent() || flock.isPresent() || terrain)) {
            throw new IllegalArgumentException(slug + ": a scatter is a plain flying unit's (M9 part B)");
        }
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
        java.util.Objects.requireNonNull(rise);
        if (rise.isPresent()
                && (layer != Layer.SUB
                        || walker.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || burrow.isPresent()
                        || submerge.isPresent()
                        || vortex.isPresent()
                        || laneStrafe.isPresent()
                        || swoop.isPresent()
                        || chase.isPresent()
                        || terrain)) {
            throw new IllegalArgumentException(
                    slug + ": a rise is a plain unit's entering on the sub layer (M8 part D, the Abyss Ray)");
        }
        java.util.Objects.requireNonNull(slowRing);
        java.util.Objects.requireNonNull(coil);
        if (coil.isPresent()
                && (layer != Layer.AIR
                        || walker.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || burrow.isPresent()
                        || submerge.isPresent()
                        || vortex.isPresent()
                        || laneStrafe.isPresent()
                        || swoop.isPresent()
                        || chase.isPresent()
                        || rise.isPresent()
                        || hover.isPresent()
                        || orbit.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": a coil is a plain air unit's (M8 part E, the Spiral Nautilus)");
        }
        java.util.Objects.requireNonNull(lunge);
        if (lunge.isPresent()
                && (layer != Layer.AIR
                        || gun.isPresent()
                        || ring.isPresent()
                        || walker.isPresent()
                        || chain.isPresent()
                        || flock.isPresent()
                        || burrow.isPresent()
                        || submerge.isPresent()
                        || vortex.isPresent()
                        || laneStrafe.isPresent()
                        || swoop.isPresent()
                        || chase.isPresent()
                        || rise.isPresent()
                        || coil.isPresent()
                        || hover.isPresent()
                        || orbit.isPresent()
                        || dive.isPresent()
                        || spawner.isPresent()
                        || brood.isPresent())) {
            throw new IllegalArgumentException(
                    slug + ": a lunge is a plain air unit's, its ring its only attack (M8 part F, the Glow Angler)");
        }
    }

    /**
     * M8 part E: this air unit with its coiling orbit round the ship (design/enemies/naval/spiral-nautilus); empty for
     * none.
     */
    public EnemySpec withCoil(Optional<Coil> withCoil) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.coil = withCoil;
        return copy.build();
    }

    /** M9 part C: this air unit laying mine lines (design/enemies/space/minelayer); empty for none. */
    public EnemySpec withMineLayer(Optional<MineLayer> withLayer) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.mineLayer = withLayer;
        return copy.build();
    }

    /** M9 part C: this flying unit with its facing armour (design/enemies/air/gilded-gunship); empty for none. */
    public EnemySpec withFacingArmour(Optional<FacingArmour> withArmour) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.facingArmour = withArmour;
        return copy.build();
    }

    /** M9 part C: this flying unit launching its streams (the Gilded Gunship's Darts); empty for none. */
    public EnemySpec withLauncher(Optional<Launcher> withLauncher) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.launcher = withLauncher;
        return copy.build();
    }

    /** M9 part D: this air unit with its dash cycle (design/enemies/air/buzzsaw-drone); empty for none. */
    public EnemySpec withDash(Optional<Dash> withDash) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.dash = withDash;
        return copy.build();
    }

    /** M9 part E: this hardened unit with its core, the box every weapon damages ({@link #core()}); empty for none. */
    public EnemySpec withCore(Optional<Hitbox> withCore) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.core = withCore;
        return copy.build();
    }

    /** M9 part E: this fixed ground unit's sweep aimed all round, with its full turns ({@link GroundSweep}). */
    public EnemySpec withGroundSweep(Optional<GroundSweep> withGroundSweep) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.groundSweep = withGroundSweep;
        return copy.build();
    }

    /**
     * M9 part E: this walker with its destroyable {@code withParts} and its topple ({@link Topple}), set together (a
     * part that topples needs the topple and the other way round).
     */
    public EnemySpec withToppleParts(List<UnitPart> withParts, Optional<Topple> withTopple) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.parts = withParts;
        copy.topple = withTopple;
        return copy.build();
    }

    /**
     * M9 part F (Level 33, the uneasy alliance): this unit as a wave's prey ({@link PreyRole#PREY}), a prey hunter
     * ({@link PreyRole#HUNTER}) or neither.
     */
    public EnemySpec withPreyRole(PreyRole role) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.preyRole = role;
        return copy.build();
    }

    /**
     * M9 part F (design/enemies/space/void-leech, The uneasy alliance): a unit's part in Level 33's prey beat. A {@link
     * #PREY} unit (a marked Talon wing's) is latched by a touching chaser whose latch {@link Latch#hunts()} instead of
     * the ship: its shield emptied for good, no fire, it leaves its path and drifts down until it leaves (its hover
     * ends straight up while it is free). A {@link #HUNTER} chaser drifts and chases the nearest free prey on the play
     * field instead of the ship (the ship once none is left).
     */
    public enum PreyRole {
        NONE,
        PREY,
        HUNTER
    }

    /** M10 part A (design/enemies, Reflect): this unit with its mirror; empty for none. */
    public EnemySpec withMirror(Optional<Mirror> withMirror) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.mirror = withMirror;
        return copy.build();
    }

    /** M10 part A (design/enemies, Regeneration): this unit with its regeneration; empty for none. */
    public EnemySpec withRegeneration(Optional<Regeneration> withRegeneration) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.regeneration = withRegeneration;
        return copy.build();
    }

    /** M10 part A (design/enemies/ground/graft-turret): this ground unit with its regrowth; empty for none. */
    public EnemySpec withRegrowth(Optional<Regrowth> withRegrowth) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.regrowth = withRegrowth;
        return copy.build();
    }

    /** M10 part A (design/enemies/ground/shield-pylon): this ground unit with its dome; empty for none. */
    public EnemySpec withDome(Optional<Dome> withDome) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.dome = withDome;
        return copy.build();
    }

    /** M10 part A (design/player/systems, the IFF spoofer): this unit as one the spoofer fools or not. */
    public EnemySpec withSpoofable(boolean isSpoofable) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.spoofable = isSpoofable;
        return copy.build();
    }

    /** M9 part D: this air unit linked to its wave's others (design/enemies/space/shard-drone); empty for none. */
    public EnemySpec withAirLink(Optional<AirLink> withLink) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.airLink = withLink;
        return copy.build();
    }

    /** M9 part B: this unit with its scatter when its wave's leader is killed (design/enemies/air/talon); empty for none. */
    public EnemySpec withScatter(Optional<SquadScatter> withScatter) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.scatter = withScatter;
        return copy.build();
    }

    /** M9 part B: this unit riding a rock of its wave (design/enemies, Rock riding); empty for none. */
    public EnemySpec withSwoopPath(Optional<SwoopPath> withPath) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.swoopPath = withPath;
        return copy.build();
    }

    public EnemySpec withRide(Optional<Ride> withRide) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.ride = withRide;
        return copy.build();
    }

    /**
     * M9 part B: this unit as a neutral craft or not ({@link #neutral()}); a neutral one is one the seekers ignore too.
     */
    public EnemySpec withNeutral(boolean isNeutral) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.neutral = isNeutral;
        copy.seekersIgnore |= isNeutral;
        return copy.build();
    }

    /**
     * M9 part B: whether it counts as an enemy: in the level's enemy total, the kills, the kill ratio, the density, the
     * floor and the haul (false only for a {@link #neutral()} craft). Every counter asks this one predicate.
     */
    public boolean countsAsEnemy() {
        return !neutral;
    }

    /** M9 part A: this unit with its hex shield (design/enemies, Enemy shields); empty for none. */
    public EnemySpec withShield(Optional<Shield> withShield) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.shield = withShield;
        return copy.build();
    }

    /** M9 part A: this unit with its {@code laser-line} (design/enemies/ground/rail-bunker); empty for none. */
    public EnemySpec withLaserLine(Optional<LaserLine> withLine) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.laserLine = withLine;
        return copy.build();
    }

    /**
     * M8 part F: this air unit with its lunge from a lure (design/enemies/naval/glow-angler); empty for none.
     */
    public EnemySpec withLunge(Optional<Lunge> withLunge) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.lunge = withLunge;
        return copy.build();
    }

    /**
     * M8 part E: this unit with its sung ring that slows the ship (design/enemies/naval/siren); empty for none.
     */
    public EnemySpec withSlowRing(Optional<SlowRing> ring) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.slowRing = ring;
        return copy.build();
    }

    /**
     * M8 part D: this unit with its rise (design/enemies/naval/abyss-ray): a shadow on {@code sub}, the rise, the glide
     * and the sink; empty for none.
     */
    public EnemySpec withRise(Optional<Rise> withRise) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.rise = withRise;
        return copy.build();
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

    /**
     * A unit with only its base stats: every movement, attack and mechanic left out. The {@code withX} builders add the
     * rest; add them in the components' order (a component's checks see the ones before it, e.g. a turret needs its
     * walker and gun).
     */
    public static EnemySpec of(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed) {
        return new EnemySpecCopy(slug, hp, hitbox, layer, contactDamage, destroyedByRamming, bounty, speed).build();
    }

    /** This unit with the gap between two units of its snake ({@link #snake()}); empty for none. */
    public EnemySpec withSnake(Optional<Snake> withSnake) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.snake = withSnake;
        return copy.build();
    }

    /** This unit with its speed in streams ({@link #streamSpeed()}); empty for none. */
    public EnemySpec withStreamSpeed(Optional<Double> withStreamSpeed) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.streamSpeed = withStreamSpeed;
        return copy.build();
    }

    /** This unit with its hover below the top edge ({@link #hover()}); empty for none. */
    public EnemySpec withHover(Optional<Hover> withHover) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.hover = withHover;
        return copy.build();
    }

    /** This unit with its orbit in circle formations ({@link #orbit()}); empty for none. */
    public EnemySpec withOrbit(Optional<Orbit> withOrbit) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.orbit = withOrbit;
        return copy.build();
    }

    /** This unit with its attack ({@link #gun()}); empty for none. */
    public EnemySpec withGun(Optional<EnemyGun> withGun) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.gun = withGun;
        return copy.build();
    }

    /** This unit with the pickup every n-th kill drops ({@link #drop()}); empty for none. */
    public EnemySpec withDrop(Optional<Drop> withDrop) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.drop = withDrop;
        return copy.build();
    }

    /** This unit with its dive ({@link #dive()}); empty for none. */
    public EnemySpec withDive(Optional<Dive> withDive) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.dive = withDive;
        return copy.build();
    }

    /** This unit with its spiral out of a whirl cluster ({@link #spiral()}); empty for none. */
    public EnemySpec withSpiral(Optional<Spiral> withSpiral) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.spiral = withSpiral;
        return copy.build();
    }

    /** This unit with its convoy turn below the top edge ({@link #strafe()}); empty for none. */
    public EnemySpec withStrafe(Optional<Range> withStrafe) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.strafe = withStrafe;
        return copy.build();
    }

    /** This unit with the puff of bullets it pops into when destroyed ({@link #deathBurst()}); empty for none. */
    public EnemySpec withDeathBurst(Optional<DeathBurst> withDeathBurst) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.deathBurst = withDeathBurst;
        return copy.build();
    }

    /** This unit with its side-to-side offset on its path ({@link #sine()}); empty for none. */
    public EnemySpec withSine(Optional<Sine> withSine) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.sine = withSine;
        return copy.build();
    }

    /** This unit with the units it releases (a spawner) ({@link #brood()}); empty for none. */
    public EnemySpec withBrood(Optional<Brood> withBrood) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.brood = withBrood;
        return copy.build();
    }

    /** This unit with its walk along its authored ground path ({@link #walker()}); empty for none. */
    public EnemySpec withWalker(Optional<Walker> withWalker) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.walker = withWalker;
        return copy.build();
    }

    /** This unit with its side-edge hover ({@link #sideHover()}); empty for none. */
    public EnemySpec withSideHover(Optional<SideHover> withSideHover) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.sideHover = withSideHover;
        return copy.build();
    }

    /** This unit with its laser sweep ({@link #sweep()}); empty for none. */
    public EnemySpec withSweep(Optional<Sweep> withSweep) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.sweep = withSweep;
        return copy.build();
    }

    /** This unit with the segment chain behind its head ({@link #chain()}); empty for none. */
    public EnemySpec withChain(Optional<ChainSpec> withChain) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.chain = withChain;
        return copy.build();
    }

    /** This unit with its periodic spawner cycle ({@link #spawner()}); empty for none. */
    public EnemySpec withSpawner(Optional<Spawner> withSpawner) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.spawner = withSpawner;
        return copy.build();
    }

    /** This unit with its leap at the ship ({@link #pounce()}); empty for none. */
    public EnemySpec withPounce(Optional<Pounce> withPounce) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.pounce = withPounce;
        return copy.build();
    }

    /** This unit with its cloak ({@link #cloak()}); empty for none. */
    public EnemySpec withCloak(Optional<Cloak> withCloak) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.cloak = withCloak;
        return copy.build();
    }

    /** This unit with its rear ambush path ({@link #ambush()}); empty for none. */
    public EnemySpec withAmbush(Optional<Ambush> withAmbush) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.ambush = withAmbush;
        return copy.build();
    }

    /** This unit with its flock steering round its leader point ({@link #flock()}); empty for none. */
    public EnemySpec withFlock(Optional<FlockSpec> withFlock) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.flock = withFlock;
        return copy.build();
    }

    /** This unit with its surfacing and submerging ({@link #submerge()}); empty for none. */
    public EnemySpec withSubmerge(Optional<Submerge> withSubmerge) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.submerge = withSubmerge;
        return copy.build();
    }

    /** This unit with its proximity ring ({@link #ring()}); empty for none. */
    public EnemySpec withRing(Optional<ProximityRing> withRing) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.ring = withRing;
        return copy.build();
    }

    /** This unit fixed to the ground layer or not ({@link #terrain()}). */
    public EnemySpec withTerrain(boolean isTerrain) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.terrain = isTerrain;
        return copy.build();
    }

    /** This unit hardened or not ({@link #hardened()}): only anti-ground deliveries and the bombs damage it. */
    public EnemySpec withHardened(boolean isHardened) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.hardened = isHardened;
        return copy.build();
    }

    /** This ground unit with its drift along its nest's current ({@link #drift()}), px/s; 0 for none. */
    public EnemySpec withDrift(double withDrift) {
        EnemySpecCopy copy = new EnemySpecCopy(this);
        copy.drift = withDrift;
        return copy.build();
    }
}
