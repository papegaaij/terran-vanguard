package vanguard.sim;

/**
 * What happened during the last step, for the presentation (sound effects, explosions, HUD
 * flashes, radio); the simulation never calls the presentation itself. Fixed capacity in parallel
 * arrays, so recording an event never allocates; events beyond the capacity are dropped, which is
 * harmless because they are not part of the game state.
 */
public final class SimEvents {
    /** Event kinds; the position is where it happened, in play-field pixels, and the value is named per kind. */
    public enum Type {
        /** A weapon fired a volley (at its muzzles); value: the mount's index in the sortie's {@link Armament}. */
        SHOT_FIRED,
        /** A shot hit an enemy (at the shot); value: the mount that fired it. */
        ENEMY_HIT,
        /** An enemy was destroyed (at the enemy); value: its kind, an index into {@link Sortie#enemyKinds()}. */
        ENEMY_DESTROYED,
        /**
         * An enemy fired a shot (at the enemy); value: its bullets' damage in whole points, which tells
         * the bullet class (a {@code medium} bullet deals 6, a {@code small} one less).
         */
        ENEMY_FIRED,
        /** A shot hit a ground object (at the shot); value: the mount that fired it. */
        GROUND_HIT,
        /**
         * A shot glanced off a hardened ground target or (M5 part C) a hardened enemy without damage
         * (at the shot; a blast's glance at the enemy); value: the mount.
         */
        SHOT_GLANCED,
        /** A bomb or shell burst on the ground (at its landing point); value: the mount that fired it. */
        BLAST,
        /** The overdrive ran out (at the ship). */
        OVERDRIVE_ENDED,
        /** A shot hit a crane's clamp (at the shot); value: the mount that fired it. */
        CLAMP_HIT,
        /** A group of the secondary objective was cleared; value: the group. */
        GROUP_CLEARED,
        /** A group was lost; value: the group. */
        GROUP_LOST,
        /** A mine layer dropped a spore (at the layer). */
        MINE_DROPPED,
        /** A spore burst into its ring, or on contact with the ship (at the spore). */
        MINE_BURST,
        /** A spore was shot (at the spore). */
        MINE_DESTROYED,
        /** A shot hit a small debris chunk (at the shot); a large one makes it glance ({@link #SHOT_GLANCED}). */
        DEBRIS_HIT,
        /** A small debris chunk broke (at the chunk). */
        DEBRIS_DESTROYED,
        /** A set piece reached the player's layer (at the set piece); value: its index. */
        SET_PIECE_DESCENDED,
        /** A part of a set piece was destroyed (at the part); value: the part's index. */
        PART_DESTROYED,
        /** A set piece was destroyed (at its centre, its chained death); value: its index. */
        SET_PIECE_DESTROYED,
        /** A set piece ended its last pass alive (at its centre); value: its index. */
        SET_PIECE_ESCAPED,
        /** A destructible ground object was destroyed (at the object); value: its index in the script. */
        GROUND_DESTROYED,
        /** A trigger released its secret's hidden crate (at the trigger). */
        SECRET_FOUND,
        /**
         * A ground trigger took its last hit and is spent (at the trigger), before the
         * {@link #SECRET_FOUND} it may bring (with the last of several triggers sharing a secret);
         * value: its index in the script's ground objects. Level 08's billboard topples with it.
         */
        TRIGGER_SPENT,
        /** The ship collected a pickup (at the pickup); value: its {@link PickupType} ordinal. */
        PICKUP_COLLECTED,
        /** Credits were picked up (at the pickup); value: the credits. */
        CREDITS_PICKED_UP,
        /** The shield absorbed damage (at the ship). */
        SHIELD_HIT,
        /** The shield dropped to zero (at the ship). */
        SHIELD_BROKEN,
        /** The armour took damage (at the ship). */
        ARMOUR_HIT,
        /**
         * An armour hit left the armour at or below {@link Defences#CRITICAL_SHARE} of its maximum for
         * the first time in the attempt (at the ship): Okafor's low-armour radio line.
         */
        ARMOUR_CRITICAL,
        /** Armour reached zero (at the ship). */
        SHIP_DESTROYED,
        /** A radio cue starts; value: its index in the level script's radio list. */
        RADIO,
        /** The secondary objective was met. */
        OBJECTIVE_MET,
        /** The secondary objective failed (a unit of an escapes objective got away). */
        OBJECTIVE_FAILED,
        /** The scroll reached the end: the primary objective is met and the level is over. */
        LEVEL_COMPLETE,
        /** The level started again after the ship was destroyed (at the ship's start position). */
        SORTIE_RESTARTED,
        /** The special was called and used a charge (at the ship): the Airstrike's radio call. */
        SPECIAL_CALLED,
        /**
         * The special button found no charge, no special, or a strike still flying (at the ship); M8 part A: or the
         * setting forbids the special; value: the {@link SpecialLimits.Reason} ordinal then, 0 otherwise.
         */
        SPECIAL_DENIED,
        /** The Airstrike's bombers entered at the bottom edge (between them). */
        AIRSTRIKE_INBOUND,
        /** An Airstrike bomb burst on the ground (at its landing point); value: its number in the strike, from 0. */
        AIRSTRIKE_BLAST,
        /** A convoy unit took a hit, or a walker's claws started on it (at the unit); value: its index. */
        ALLY_HIT,
        /** A convoy unit was destroyed (at the unit); value: its index. M5 part D: not for the scripted loss. */
        ALLY_LOST,
        /** The primary objective failed (the last convoy unit was lost): the level fails without a wreck (at the ship). */
        PRIMARY_FAILED,
        /**
         * A spawner released its units, destroyed or bursting on its own (at its centre); value: its
         * kind. A self-burst has no {@link #ENEMY_DESTROYED}: this event is its end.
         */
        BROOD_HATCHED,
        /** A spawner burst on its own (at its centre), after {@link #BROOD_HATCHED}; value: its kind. */
        BROOD_BURST,
        /**
         * A walker was destroyed (at it), after its {@link #ENEMY_DESTROYED}, for its remains at its
         * last heading; value: {@link #walkerValue(int, double)}.
         */
        WALKER_DOWN,
        /** A boss arrived: it starts its descent, its bar and its sting start (at its centre); value: its set piece's index. */
        BOSS_ARRIVED,
        /** A boss ended its descent (at its centre); value: its set piece's index. */
        BOSS_SETTLED,
        /** A boss entered a phase after its first (at its centre); value: the phase's index. */
        BOSS_PHASE,
        /**
         * A boss was destroyed (at its centre), after its {@link #SET_PIECE_DESTROYED}: the credit
         * shower; value: the credits its parts paid.
         */
        BOSS_DESTROYED,
        /** A Retry from boss restarted the level at its boss checkpoint (at the ship). */
        BOSS_RETRY,
        /** A mortar lobbed a blob (at the mortar). */
        MORTAR_LOBBED,
        /** A lob landed and burst (at its marker); value: 1 for a direct hit on the ship, else 0. */
        MORTAR_IMPACT,
        /** The rail lights start their chase before a sled (at the rail's foot). */
        SLED_LIGHTS,
        /** A sled shoots up the rail (at the rail's foot). */
        SLED_LAUNCHED,
        /** A sled struck the ship (at the ship). */
        SLED_HIT,
        /** A destroyed ground target threw a rock (at the rock). */
        ROCK_THROWN,
        /**
         * A chain's segment was cut and its rear part starts to grow a new head (at the cut); M7 part C: or, a
         * ground chain's ({@link Chain#ground()}), stops and dies after its cut delay. Value: the segment's kind.
         */
        CHAIN_CUT,
        /** A cut chain's new head has grown (at it); value: its kind. */
        CHAIN_REGROWN,
        /**
         * A dying chain member burst (at it), paying nothing; value: its kind and its hit box's width
         * ({@link #chainPopKind}, {@link #chainPopWidth}), so the burst can follow the chain's taper.
         */
        CHAIN_POP,
        /** A laser sweep's telegraph started (at the unit); value: its kind. */
        SWEEP_TELEGRAPH,
        /** A laser sweep's beam started (at the unit); value: its kind. */
        SWEEP_FIRED,
        /** A laser sweep's beam hit the ship (at the ship); M5 part H: a boss sweep's too. */
        SWEEP_HIT,
        /** A Smart Bomb went off (at the ship). */
        SMART_BOMB,
        /** A scripted flare was fired (at its start); value: its index. */
        FLARE_FIRED,
        /**
         * Part G: a boss's open window launched a unit (at the window part; the Brood Carrier's sacs);
         * value: the unit's kind.
         */
        BOSS_LAUNCHED,
        /**
         * M5 part A: the wingman took damage (at him); value: the damage, rounded up. His shots carry
         * {@link Sortie#wingmanMount()} as their mount in the shot events.
         */
        WINGMAN_HIT,
        /** The wingman's armour dropped below his low-armour share, once per attempt (at him): his bark. */
        WINGMAN_CRITICAL,
        /**
         * The wingman's armour reached zero: he ejects, his craft explodes (at him); value: the pod's
         * drift, -1 to the left edge, 1 to the right.
         */
        WINGMAN_EJECTED,
        /**
         * M5 part A: one of the ship's proximity mines armed (at the mine), 0.4 s after its drop: its
         * arming beep; value: the mount that dropped it.
         */
        PROXIMITY_MINE_ARMED,
        /**
         * M5 part C: a periodic spawner's iris started to open, the telegraph of its release (at its
         * centre; design/enemies/ground/hive-node); value: its kind.
         */
        SPAWN_TELEGRAPH,
        /** M5 part C: a periodic spawner released its units (at its centre); value: its kind. */
        SPAWN_RELEASED,
        /** M5 part C: a walker took off for a pounce (at it; design/enemies/ground/ravager); value: its kind. */
        POUNCE,
        /** M5 part C: a pounce landed (at it); value: its kind. */
        POUNCE_LANDED,
        /**
         * M5 part C: a hold zone started (at the ship; design/campaign Level 09): the scroll eases down
         * to its speed from the next step; value: the hold's index. The music's {@code full_on: hold}
         * fades in from here.
         */
        HOLD_START,
        /**
         * M5 part C: a hold zone ended, every unit of its groups gone (at the ship): the scroll eases
         * back to the section's speed; value: the hold's index. The {@code full_on: hold} mix fades
         * out from here.
         */
        HOLD_END,
        /**
         * M5 part C: the collapse started (at the ship): its tower leans (the warning) over the band,
         * {@link Sortie#collapseBandTop()} to {@link Sortie#collapseBandBottom()}.
         */
        COLLAPSE_WARNING,
        /** M5 part C: the collapse's tower starts to drop straight down (at the ship), the warning over. */
        COLLAPSE_FALL,
        /**
         * M5 part C: the collapse's tower hits the ground (at the ship): its blast rolls out from the
         * foot, killing the ground units in the band as it reaches them ({@link Sortie#collapseBlastRadius()}).
         */
        COLLAPSE_IMPACT,
        /** M5 part C: the collapse's blast has rolled out (at the ship): it is over, a hold through it ends. */
        COLLAPSE_END,
        /**
         * M5 part D: a cloaked unit decloaks at its hold point, the start of its flash (at it; design/
         * enemies/air/wraith): on its decloaked layer from here, its gun silent until the flash ends;
         * the decloak sound and the radio event {@code first-decloak}; value: its kind.
         */
        DECLOAK,
        /**
         * M5 part D: a swarm's leader point re-enters below the bottom edge for a loop-back (at the
         * leader point; design/enemies/air/mote-swarm), when the bottom edge's warning ends: the
         * swarm's sound and the radio event {@code first-loop-back}; value: the loop-back's number,
         * from 1.
         */
        LOOP_BACK,
        /**
         * M5 part D: the scripted loss's glow starts over its unit (at the unit; design/campaign Level
         * 10, the alien glow in the cloud deck, which follows the unit's sway until the lance); value:
         * the unit's index.
         */
        LOSS_GLOW,
        /**
         * M5 part D: the scripted loss's lance takes its unit (at the unit): the lance, its sound and
         * the music's duck; the unit is lost (it glides down, a presentation effect) without an
         * {@link #ALLY_LOST} event; value: the unit's index.
         */
        SCRIPTED_LOSS,
        /**
         * M5 part E: a naval convoy unit's flak burst (design/allies, escort frigate): presentation only,
         * a puff on {@code low-air} over the convoy and a quiet distant sound, hitting nothing (at the
         * burst); value: the unit's index.
         */
        ALLY_FLAK,
        /**
         * M5 part E: an arena boss telegraphs a slam lane (at the lane's top, its centre; design/enemies/
         * bosses/harbour-kraken): the churn and its sound, the radio event {@code first-telegraph};
         * value: the lane (1-based). The lane stays telegraphed until its impact ({@link
         * SlamArena#telegraphed()}).
         */
        TELEGRAPH,
        /**
         * M5 part E: a slam strikes its lane (at the lane's centre): the impact, its splash and sound;
         * value: the lane (1-based).
         */
        SLAM,
        /** M5 part E: an arena boss's part starts to surface (at the part); value: the part's index. */
        SURFACE,
        /** M5 part E: an arena boss's part starts to dive (at the part); value: the part's index. */
        DIVE,
        /**
         * M5 part F: a lightning flash of the level's weather (at the top edge's middle; design/campaign
         * Level 12): the sky flash, the banks lit, the thunder after it; value: the flash's index in the
         * level's {@code lightning} list. Nothing else in play changes.
         */
        LIGHTNING,
        /**
         * M5 part F: a Lamprey latched onto the ship (at the Lamprey, on the hull; design/enemies/air/
         * lamprey): its latch sound, the first one's prompt and radio line; value: the spot ({@link
         * EnemySpec.Latch#LEFT}, {@link EnemySpec.Latch#RIGHT}, {@link EnemySpec.Latch#TAIL}).
         */
        LAMPREY_LATCHED,
        /**
         * M5 part F: the shake-off threw every latched Lamprey off (at the ship): the shake sound; value:
         * how many were thrown off.
         */
        LAMPREY_SHAKEN,
        /**
         * M5 part F: the ship collected a weather sonde (at the sonde; design/campaign Level 12): the
         * chime and the tracker; value: the sondes collected in this attempt, this one included.
         */
        SONDE_COLLECTED,
        /**
         * M5 part G: the defend primary's ally dropped below one of the shares its {@code ally-below} radio
         * cues name, for the first time in the attempt (at the ally; design/campaign Level 13: the
         * integrity alarm at 50 % and 25 %); value: the share in whole percent.
         */
        ALLY_BELOW,
        /**
         * M5 part G: the defend primary's synchronisation completed with its ally standing (at the ally;
         * Level 13's "Grid sync complete"); value: the ally's integrity left in whole percent.
         */
        DEFEND_COMPLETE,
        /**
         * M5 part G: a lane walker was destroyed (at the unit; design/enemies/naval/skimmer: the hull flips
         * and sinks); value: as {@link #WALKER_DOWN}'s, its kind and its facing ({@link
         * SimEvents#walkerKind(int)}, {@link SimEvents#walkerFacing(int)}).
         */
        LANE_WALKER_DOWN,
        /**
         * M5 part G: a placed pickup came into the field (at it: the top edge, or the point a supply
         * drone releases it; design/allies, CDF supply drone); value: its {@link PickupType} ordinal.
         */
        PICKUP_PLACED,
        /**
         * M5 part H: a root burst's telegraph starts (at its ground point then, after the move away from
         * the ship; design/campaign Level 14, Hazards): the paving cracks and dust rises; value: the
         * burst's {@link RootBurst#index()}. The point scrolls with the ground ({@link
         * Sortie#burstCount()} and its accessors).
         */
        ROOT_BURST_TELEGRAPH,
        /**
         * M5 part H: a root burst's growths appeared (at the burst's point now): the root breaks out and
         * they start to grow in ({@link Enemy#growing()}); value: the burst's index.
         */
        ROOT_BURST_GREW,
        /**
         * M5 part H: an anchored boss entered a phase whose windows launch ambushes (at each window
         * part: the Siege Spire's claw crown opens, its maw exposed); value: the part's index.
         */
        SPIRE_MAW_OPENED,
        /**
         * M5 part H: an anchored boss lifts off its anchor (at the anchor: the tear-free shockwave, the
         * stump left behind; design/enemies/bosses/siege-spire, decision H4 = a); value: the phase.
         */
        SPIRE_LIFT,
        /** M5 part H: the lift is over, the boss holds at its new station and engages (at the boss); value: the phase. */
        SPIRE_LIFTED,
        /**
         * M5 part H: a boss sweep's telegraph starts (at the beam's origin; the line along its first edge);
         * value: the attack's index in {@link BossSpec#attacks()}.
         */
        SPIRE_SWEEP_TELEGRAPH,
        /** M5 part H: a boss sweep's beam starts to sweep (at its origin); value: the attack's index. */
        SPIRE_SWEEP,
        /**
         * A destroyed ground unit was drifting on top of the scroll (Level 13's mortar floe), right after
         * its {@link #ENEMY_DESTROYED} (at it): its remains drift on with what carried it; value:
         * {@link #driftValue(double, double)}.
         */
        WRECK_DRIFT,
        /**
         * M7 part A: a beam came on (at its muzzle; design/player/weapons/ion-beam): the start sound, then
         * the loop. Its column and contact are fields of {@link Sortie#beam(int)}, not events. Value: the
         * mount.
         */
        BEAM_ON,
        /** M7 part A: a beam went off (at its muzzle): the stop sound and its fade. Value: the mount. */
        BEAM_OFF,
        /**
         * M7 part A: a light drone absorbed a bullet, a laser touch or a contact (at the drone); value:
         * its bay's mount.
         */
        DRONE_HIT,
        /** M7 part A: a light drone broke (at the drone): a small spark burst; value: its bay's mount. */
        DRONE_BROKEN,
        /** M7 part A: a drone bay rebuilt its drone, which starts to fade in (at the bay); value: the mount. */
        DRONE_REBUILT,
        /**
         * M7 part A: a Deflector Pod caught an enemy bullet (at the bullet): the catch flash on its arc and
         * the deflect tick; value: the pod's mount.
         */
        DEFLECTOR_CAUGHT,
        /** M7 part A: a Deflector Pod got a charge back (on its side of the ship): its field brightens; value: the mount. */
        DEFLECTOR_CHARGED,
        /**
         * M7 part A: a cooldown special's cooldown is over (at the ship): the HUD ring's white flash and
         * the ready chime (design/player/specials, Cooldown specials and the HUD ring).
         */
        SPECIAL_READY,
        /**
         * M7 part A: an EMP Burst went off (at the ship; after its {@link #SPECIAL_CALLED}): the ring
         * ({@link SpecialSlot#empRadius(double)}) and its sound.
         */
        EMP_BURST,
        /**
         * M7 part A: the EMP Burst's ring stunned a unit (at it; a chain once, at the member it reached
         * first): the power-down whine; value: its kind. The crackle overlay follows
         * {@link Enemy#empStunned()}.
         */
        EMP_STUNNED,
        /**
         * M7 part A: an Orbital Lance was called (at the ship): its radio line and the lock beep; the red
         * targeting line shows while {@link SpecialSlot#lanceLocking()}.
         */
        LANCE_LOCK,
        /** M7 part A: the Orbital Lance fires (at its centre, the bottom edge): the column, its loop and rumble. */
        LANCE_FIRED,
        /** M7 part A: the Orbital Lance stops burning and fades (at its centre, the bottom edge): the loop stops. */
        LANCE_ENDED,
        /** M7 part A: the Shield Overcharge went off (at the ship): the white double shell flares, the whoosh. */
        OVERCHARGE,
        /** M7 part A: the Shield Overcharge's raised capacity ended (at the ship): the shell goes, the shimmer. */
        OVERCHARGE_ENDED,
        /**
         * M7 part A: a dash started (at the ship's position before it; design/player/systems, Evasive
         * thrusters): the thruster burst and the afterimages; value: its direction, 0 straight up and
         * then clockwise in eighths (2 right, 4 down, 6 left).
         */
        DASH,
        /**
         * M7 part B: a Burrower's bulge starts (at its spot; design/enemies/ground/burrower, the tell): the
         * mound, its cracks and glow, the low rumble; it surfaces there {@link EnemySpec.Burrow#bulgeSeconds()}
         * later. Value: its kind.
         */
        BURROW_BULGE,
        /**
         * M7 part B: a Burrower broke out (at it): the sand spray, the crater rim; it is on {@code ground}
         * now; the radio event {@code first-surfacing}. Value: its kind.
         */
        BURROW_SURFACED,
        /** M7 part B: a Burrower starts to dive (at it); buried from the dive's middle, a filling hole left. Value: its kind. */
        BURROW_DIVE,
        /** M7 part B: a Burrower swam off the bottom edge under the sand (at its last position): an escape. Value: its kind. */
        BURROW_ESCAPED,
        /** M7 part B: a unit with a swoop banks into it (at it; design/enemies/air/dust-kite). Value: its kind. */
        SWOOP,
        /**
         * M7 part C: a hull touched rock (at the touch, on the rock's line; design/campaign Level 16, Wall
         * contact): the rock spark, the scrape and the short hit flash. Value: -1 the rock on the ship's left,
         * 1 on its right; 2 Rook touched it. Once per touch (its cooldown's contacts only clamp).
         */
        WALL_CONTACT,
        /**
         * M7 part C: a shot or an enemy bullet stopped at rock (at it; design/campaign Level 16, What walls
         * stop): the small dust spark. Value: the shot's mount, -1 an enemy bullet.
         */
        ROCK_SPARK,
        /**
         * M7 part C: a gate's tendril ended its dormancy and flared to its full width, its touch hurting from
         * now (at its middle; design/enemies/ground/tendril-anchor, Tell); value: its {@link Tendril#serial()}.
         */
        TENDRIL_FLARE,
        /**
         * M7 part C: a flared tendril touched a hull (at the craft) and dealt its damage; value: {@link
         * Links#TOUCH_SHIP} (0) the ship, {@link Links#TOUCH_ROOK} (2) Rook.
         */
        TENDRIL_TOUCH,
        /**
         * M7 part C: a beam started burning a tendril (where its column crosses it); value: its {@link
         * Tendril#serial()}. {@link Tendril#burning()} says whether it still does.
         */
        TENDRIL_BURN,
        /**
         * M7 part C: a tendril broke for good (at its middle): value 0 an anchor died (it snaps back into the
         * survivor), 1 a beam burnt it through (it parts in the middle).
         */
        TENDRIL_BROKEN,
        /**
         * M7 part C: a ground chain's segment's pore fired its spore (at the segment; design/enemies/ground/
         * threadcrawler, Travelling wave); value: the member's index in its chain (1 the first segment).
         */
        PORE_FIRED,
        /**
         * M7 part D: a hooked bomber dropped its load on a ground ally (design/campaign Level 17, What goes for
         * the domes; at the bomber's centre, over the dome); value: the ground ally's index. The dome's {@link
         * #ALLY_HIT} follows in the same step when the load hurts it.
         */
        BOMB_DROPPED,
        /**
         * M7 part D: an aura ring absorbed damage (at the ringed unit; design/enemies/air/choir-herald): the ring's
         * flash and the absorb sound; value: the unit's kind. At most one per unit every few steps (a beam's
         * damage comes every step).
         */
        RING_ABSORBED,
        /** M7 part D: a Herald died and every ring it fed collapses (at the Herald); value: how many rings. */
        RINGS_COLLAPSED,
        /** M7 part D: the EMP Burst stripped every aura ring (at the burst's centre); value: how many rings. */
        RINGS_STRIPPED,
        /**
         * M7 part D: a unit part on a unit that is not a boss was destroyed (at the part; design/enemies/ground/
         * shellback, the mortar vent); value: the unit's kind. A set piece's part has {@link #PART_DESTROYED}.
         */
        UNIT_PART_DESTROYED,
        /** M7 part D: a walker started its curl (at it; design/enemies/ground/shellback); value: its kind. */
        CURLED,
        /** M7 part D: a curled walker started its roll (at it); value: its kind. */
        ROLL_STARTED,
        /**
         * M7 part D: a rolling ball crushed a ground target (at the ball for a ground ally, at the object for a
         * ground object); value: the ground ally's index, -1 for a ground object. The ally's {@link #ALLY_HIT}
         * follows in the same step.
         */
        ROLL_CRUSHED,
        /** M7 part D: a rolled walker started to uncurl (at it); its belly shows after the uncurl; value: its kind. */
        UNCURLED,
        /**
         * M7 part D: a walker's mortar vent swells before its lob ({@link Enemy#MORTAR_TELL_SECONDS} ahead, at the
         * walker; design/enemies/ground/shellback); value: its kind. The lob is {@link #MORTAR_LOBBED}.
         */
        MORTAR_TELL,
        /**
         * Round 40 (the reworked Harbour Kraken, proposal 3a): a rooted slam arm's root took damage this step
         * (at the root; a hit on its arm's segments counts, the damage goes to the root): its hit flash and
         * sound; at most one per root and step. Value: the root's part index. Its cut is {@link
         * #PART_DESTROYED}; whether it took the raised multiplier: {@link SlamArena#armRaised(int)}.
         */
        ROOT_HIT,
        /**
         * Round 40 (the Siege Spire's lanes, proposal 4a): a part's aim turn starts its glow, the tell before
         * its one aimed shot ({@link BossSpec.AimTurns}; at the part); value: the part's index. The glow
         * runs while {@link SetPiece#partAimGlowing(int)}.
         */
        AIM_TELL,
        /**
         * M7 part D, round 40 K.a: a singing Choir Herald rang a unit that carried no ring of its (at the unit;
         * design/enemies/air/choir-herald): the radio's {@code first-ring} cue; value: the unit's kind.
         */
        RING_SUNG,
        /**
         * M7 part E: a vortex started to rise (at it; design/enemies/ground/dust-devil): the tell before its core
         * opens, the swirl tightening and the spin speeding up; value: its kind.
         */
        VORTEX_RISING,
        /** M7 part E: a vortex opened on {@code low-air} (at it): its core is hittable and its spiral fires; value: its kind. */
        VORTEX_OPENED,
        /** M7 part E: a vortex closed and sinks back to the {@code ground} (at it); value: its kind. */
        VORTEX_CLOSED,
        /**
         * M7 part E: a destroyed unit with a pull dropped the pickups it held (at it; the Dust Devil's grit cloud);
         * value: how many.
         */
        PICKUPS_RELEASED,
        /**
         * M7 part E: a lane strafer's pass trigger fired (at it; design/enemies/air/ghost-drone): the red glint on
         * its sensor eye, the tell its gun's {@link EnemyGun#tellSeconds()} before the burst's first shot; value:
         * its kind. Each shot of the burst is an {@link #ENEMY_FIRED}.
         */
        EYE_GLINT,
        /**
         * Round 41 (the 2026-10-19 playtest, Level 14): a boss with {@link BossSpec#incomingSeconds()} is that
         * long from its arrival on the level clock (at it: off the screen still, an anchored boss where it lies
         * on the ground); the game's warning box (the klaxon, the banner) goes here instead of at its {@link
         * #BOSS_ARRIVED}, so it is shown before the boss is reached. Value: the set piece's index. Not
         * repeated by a retry from the boss checkpoint (recorded just before the arrival).
         */
        BOSS_INCOMING,
        /**
         * M7 part F (design/enemies/bosses/revenant-walker): a walking boss's pod opened its hatch, the tell its
         * gun's {@link EnemyGun.HomingSpec#hatchSeconds()} before its homing salvo (at the pod); value: the pod's part
         * index. The hatch stays open while {@link SetPiece#partHatchOpen(int)}.
         */
        POD_HATCH,
        /** M7 part F: a homing missile was launched (at its start); value: its {@link Missile#serial()}. */
        MISSILE_LAUNCHED,
        /**
         * M7 part F: a homing missile is gone (where it was); value: why, {@link Missiles#SHOT_DOWN}, {@link
         * Missiles#STRUCK} (it burst on the ship's or Rook's hull; the hit is the defences' events) or {@link
         * Missiles#FIZZLED}.
         */
        MISSILE_DESTROYED,
        /** M7 part F: a walking boss's flame cone starts its tell (at its flamer): the pilot light, the hiss. */
        CONE_TELL,
        /** M7 part F: a walking boss's flame cone starts to burn (at its flamer). */
        CONE_BURN,
        /** M7 part F: a walking boss's foot came down (at its centre): the stomp, the dust and slag puffs. */
        WALKER_STEP,
        /** Round 44: a walking boss's rail lance starts its charge (at the lance): the line glows, the charge whine. */
        LANCE_CHARGE,
        /** Round 44: the rail lance fires along its line (at the lance). */
        LANCE_FIRE,
        /**
         * Round 44: the rail lance's shot hit the ship or Rook (at the one hit; the damage is the defences' events); value:
         * {@code 1} the ship, {@code 2} Rook.
         */
        LANCE_HIT,
        /** Round 44: a walking boss's legs brace for a stomp (at its centre): the ground ring's mark. */
        STOMP_TELL,
        /** Round 44: it stomps (at its centre): the shockwave's ring expands from there. */
        STOMP,
        /** Round 44: the stomp's ring reached the ship or Rook (at the one hit); value: {@code 1} the ship, {@code 2} Rook. */
        STOMP_HIT,
        /**
         * M7 part F: a walking boss was destroyed (at its centre, with its {@link #BOSS_DESTROYED}):
         * it topples along its heading and its wreck stays on the ground, scrolling with it; value: its
         * heading, 0–7 ({@link StrideArena#heading()}).
         */
        BOSS_WRECK,
        /**
         * M7 part F: a furnace vent's grate starts to glow (design/campaign Level 19, Hazards; at the grate): its
         * tell; value: the grate's number in the level ({@link FurnaceVents#grate(int, int)}).
         */
        VENT_TELL,
        /** M7 part F: a furnace vent's flame column stands up (at the grate); value: the grate's number. */
        VENT_BURST,
        /**
         * M7 part F: a furnace vent's flame hit the ship (value 0) or Rook (value 1), at him: once per burst each. M8
         * part G: a heat plume's also the Bulwark (value 2).
         */
        VENT_HIT,
        /**
         * M7 part G (design/campaign Level 20, The pursuit swarm): a pursuit's lunge starts its tell (at its leader
         * point): the flock contracts into a ball, brightens, a rising hum; value: its flock's serial.
         */
        PURSUIT_TELL,
        /** M7 part G: a pursuit's leader point dives down the ship's lane (at it); value: its flock's serial. */
        PURSUIT_LUNGE,
        /**
         * M7 part G: a pursuit breaks off (at its leader point): it climbs off the top, its motes following alive;
         * value: its flock's serial.
         */
        PURSUIT_BREAK,
        /**
         * M7 part H (design/enemies/bosses/dust-colossus, Breaches): a breach's tell starts (at the breach point): the
         * sand bulges and cracks, a dust ring, a low rumble; value: its {@link BreachArena.Pose} ordinal.
         */
        BREACH_TELL,
        /** M7 part H: the mouth breaches (at its centre): its rise starts; value: its {@link BreachArena.Pose} ordinal. */
        BREACH_RISE,
        /** M7 part H: the bite's tell (at the mouth): the teeth flash, the jaws start to close; value: the pose ordinal. */
        BITE_TELL,
        /**
         * M7 part H: the jaws snap shut (at the mouth); value: what the bite circle caught, bit {@code 1} the ship, bit
         * {@code 2} Rook (0: nothing).
         */
        BITE_SNAP,
        /** M7 part H: the mouth dives back under the sand (at its centre); value: its {@link BreachArena.Pose} ordinal. */
        BREACH_DIVE,
        /**
         * M7 part H, step H3: an erupting under point's marker is placed (at its centre, on the ship; it erupts after
         * the eruption's marker time; it replaces the tell's {@link #BREACH_TELL}); value: the {@link BreachArena.Pose#UNDER}
         * ordinal.
         */
        ERUPTION_MARKER,
        /**
         * M7 part H, step H3: the marker erupts, the mouth up and open at once (at its centre; it replaces the rise's
         * {@link #BREACH_RISE}); value: what it caught, {@code 1} the ship, {@code 2} Rook (0: nothing).
         */
        ERUPTION,
        /**
         * Step SIM-K (round 44, Q.4): the eruption's ring ({@link BossSpec.Shockwave}) reaches the ship or Rook (at the
         * mouth's centre); value: whom it caught now, {@code 1} the ship, {@code 2} Rook.
         */
        ERUPTION_RING,
        /**
         * M8 part A: the fitted special's setting limit changed at a waterline or a section (at the ship): the HUD
         * row's chime and its flash; value: the new {@link SpecialLimits.Reason} ordinal (0: usable again).
         */
        SPECIAL_LIMIT,
        /**
         * M8 part A: a Sonar Pulse went off (at the ship; after its {@link #SPECIAL_CALLED}): the ring ({@link
         * SpecialSlot#sonarRadius(double)}) and the ping.
         */
        SONAR_PULSE,
        /** M8 part A: the Sonar Pulse's ring reached a unit (at it): its contact bracket pings; value: its serial. */
        SONAR_CONTACT,
        /** M8 part A: the Sonar Pulse's window closed (at the ship): the outlines go, the water's rules are back. */
        SONAR_ENDED,
        /** M8 part A: Decoy Flares were launched (at the rear muzzle): the pitched-up launch; value: how many. */
        FLARES_LAUNCHED,
        /** M8 part A: the Bulwark was hit (at it): its hit flash; value: the damage, rounded up. */
        BULWARK_HIT,
        /** M8 part A: the Bulwark was destroyed (at it): a {@code medium} explosion; it is out for the attempt. */
        BULWARK_DESTROYED,
        /**
         * M8 part D: a unit with a rise took its spot as a shadow (at it; design/enemies/naval/abyss-ray): the dark
         * patch starts to grow, the low horn (the tell); value: its kind. The radio's {@code first-ray} watches it.
         */
        RISE_SHADOW,
        /** M8 part D: its shadow's time is over and it rises at its spot (at it); value: its kind. */
        RISE,
        /**
         * M8 part E: a Siren's sac began its tell before a ring (design/enemies/naval/siren; at the unit); value: its
         * kind.
         */
        RING_TELL,
        /** M8 part E: a slowing ring left a Siren's sac (at the sac); value: its kind. */
        RING,
        /** M8 part E: a ring slowed a hull (at the hull: the sonar shimmer and the falling tone); value: 0 the ship, 1 Rook. */
        SLOWED,
        /**
         * M8 part E: a coiling unit began to uncoil (design/enemies/naval/spiral-nautilus; at it): its crown opens, the
         * tell before its spiral; value: its kind.
         */
        UNCOIL,
        /** M8 part E: a coiling unit closed its shell after its spiral (at it) and breaks outward; value: its kind. */
        COIL,
        /**
         * M8 part F: a lunging unit triggered and dives at where the ship is (design/enemies/naval/glow-angler; at it):
         * the lunge's sound; value: its kind.
         */
        LURE_LUNGE,
        /** M8 part F: a lunging unit's ring left it after its dive (at it); value: its kind. */
        LURE_RING,
        /**
         * M8 part F: Rook's trails start (design/player/wingmen, Rook's loss; Level 26's {@code rook_loss}; at him):
         * his barks end; the trails are the presentation's. Only while he flies (not ejected); value: 0.
         */
        ROOK_TRAILS,
        /**
         * M8 part F: Rook is lost (at him): he stops firing and taking hits and spirals down off the bottom edge; not
         * an eject (no pod). Only while he flies; value: his spin's side, -1 left or 1 right.
         */
        ROOK_LOST,
        /**
         * M8 part G: a heat plume bent a shot, an enemy bullet or a homing torpedo that entered its burning disc
         * (design/campaign Level 27, Hazards; {@link HeatPlumes}; at the bent projectile): its shimmer; value: the
         * plume's grate number ({@link FurnaceVents#grate(int, int)}).
         */
        PLUME_BEND,
        /**
         * M8 part G: a heat plume burned a swimmer over its burning disc, once per burst (at the unit; the kill, if it
         * was one, follows as the player's {@link #ENEMY_DESTROYED}); value: the unit's kind.
         */
        PLUME_BURN,
        /**
         * M8 part H (design/enemies/bosses/abyssal-maw; {@link CoilArena}): a gill sac starts to swell before its ring
         * (at the sac); value: the sac's part.
         */
        SAC_SWELL,
        /**
         * M8 part H: the throat starts to glow and the jaw opens (at the throat): value 0 before the throat's fan, 1
         * before a fan wall.
         */
        THROAT_GLOW,
        /** M8 part H: a fan wall fires (at the throat); value: its first gap's orb. */
        FAN_WALL,
        /**
         * M8 part H: a coil sweep's tell starts (at the bight's centre segment at rest): silt off the plates, the glow
         * line, the groan; value: its {@link CoilSpec.Side} ordinal.
         */
        COIL_SWEEP_TELL,
        /** M8 part H: the bight swings in (at its centre segment at rest); value: its {@link CoilSpec.Side} ordinal. */
        COIL_SWEEP,
        /** M8 part H: a swinging bight caught the ship or the escort (at the bight); value: 1 the ship, 2 the escort, 3 both. */
        COIL_STRIKE,
        /** M8 part H: the coil slides into the dark (at the head): the dive's roar, the vent gutters. */
        MAW_DIVE,
        /** M8 part H: the lunge's shadow starts up its column (at the column's foot); value: the column's x, px. */
        LUNGE_TELL,
        /** M8 part H: the head bursts up the column (at its foot). */
        LUNGE,
        /** M8 part H: the lunge caught the ship or the escort (at the column); value as {@link #COIL_STRIKE}'s. */
        LUNGE_HIT,
        /** M8 part H: the coil slides back round the edges (at the head). */
        COIL_RETURN,
        /** M8 part H: an implant starts to spark (its tell; at it); value: its index among the implants. */
        IMPLANT_SPARK,
        /** M8 part H: an implant's plate blows aside, its core open (at it); value: its index. */
        IMPLANT_OPEN,
        /** M8 part H: an open implant's arc before its burst (at it); value: its index. */
        IMPLANT_ARC,
        /** M8 part H: an implant shuts (at it); value: its index. */
        IMPLANT_SHUT,
        /** M8 part H: a coil plate flares before an Eel Swarm slips out (at the shed point); value: the point's index. */
        SHED_TELL,
        /**
         * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The cruisers; {@link CruiserRun}): the cruisers start
         * to rise through the ice (at the field's bottom centre): the engine thrum, the ice grinding.
         */
        CRUISER_RISE,
        /**
         * M8 part H: a broadside's tell starts: the gun ports glow, the tracer lines cross the band (at the field's edge
         * on the cruiser's side, the band's centre height); value: the cruiser ({@link CruiserRun#LEFT}, {@link
         * CruiserRun#RIGHT}).
         */
        BROADSIDE_TELL,
        /** M8 part H: a broadside's volley crosses its band (at its first shell's start); value: the cruiser. */
        BROADSIDE,
        /** M8 part H: a flak marker is set (at it; its burst is the lob's {@link #MORTAR_IMPACT}); value: its index. */
        FLAK,
        /** M8 part H: a cruiser's point defence fires a shot of its burst (at the turret); value: the cruiser. */
        POINT_DEFENCE,
        /**
         * M9 part A (design/enemies, Enemy shields): a hit landed on a unit's hex shield and it held (at the unit; the
         * hexes' flash); value: the unit's kind. At most one per unit every few steps (a beam's damage comes every step).
         */
        ENEMY_SHIELD_HIT,
        /**
         * M9 part A: a unit's hex shield was emptied (at the unit; the shatter ring): spent by a hit, a {@code
         * shield-breaker} arc or the EMP Burst; value: the unit's kind.
         */
        ENEMY_SHIELD_BROKEN,
        /** M9 part A: a unit's hex shield is full again after its refill (at the unit; the shimmer); value: its kind. */
        ENEMY_SHIELD_REFILLED,
        /**
         * M9 part A (design/player/weapons/tesla-coil-pod): a pod's arc struck (at its target; the arc is drawn from
         * the pod's muzzle to here for 0.08 s, {@link PlayerFire#arcX}); value: the mount.
         */
        ARC,
        /**
         * M9 part A (design/enemies/ground/rail-bunker, {@code laser-line}): a unit's line locked and its telegraph
         * starts (at the unit; the line is {@link Enemy#lineBeam()}); value: the unit's kind.
         */
        LINE_TELEGRAPH,
        /** M9 part A: a unit's laser line fires (at the unit): the rail's crack; value: the unit's kind. */
        LINE_FIRED,
        /** M9 part A: a laser line struck the ship (at the ship); value: the unit's kind. */
        LINE_HIT,
        /**
         * M9 part A (design/player/generator, Enemy drain effects): the number of drainers on the ship changed (at the
         * ship); value: how many drain now (0: the last let go).
         */
        POWER_DRAIN,
        /**
         * M9 part B (design/enemies/ground/sam-nest, Dormant until t; Level 29's turning station): a dormant unit woke
         * at its time on the level clock (at the unit): its {@link GroundUnit#WAKE_SECONDS} tell starts (the rack
         * lifts, the lights turn red); value: its kind.
         */
        WOKE,
        /**
         * M9 part B (design/enemies/air/talon, Squad leader): a wave's leader was killed and its living members break off
         * (at the leader; the radio squelch); value: the leader's kind.
         */
        SQUAD_SCATTER,
        /**
         * M9 part B (design/enemies, Rock riding): a rider's tell before its leap starts (at the rider: it crouches, its
         * back glow flares, the rock scrape); value: its kind.
         */
        RIDER_TELL,
        /** M9 part B: a rider leaps off its rock at the ship (at the rider); value: its kind. */
        RIDER_LEAP,
        /**
         * M9 part B (design/campaign Level 29, Hold fire): a neutral craft left the screen unharmed by kills (at the
         * craft); value: its kind.
         */
        NEUTRAL_LEFT,
        /** M9 part B: a neutral craft was destroyed (at the craft: its explosion); value: its kind. */
        NEUTRAL_DOWN,
        /**
         * M9 part B (Level 29, the rescue halt): the held arena's last holder fell (at it): the clock resumes, the
         * tender's clamps are open.
         */
        HOLDERS_DOWN,
        /**
         * M9 part B (design/player/wingmen, Rook's rescue): the escort swap starts (at its launch point, the tender's
         * hold); value: {@link EscortSwap#JOINS} (the rescued Rook launches and joins; the Bulwark in the slot peels
         * away), {@link EscortSwap#EMPTY} (Rook flew from the start: the hold opens empty) or {@link EscortSwap#LEAVES}
         * (no Rook to join: the Ember launches and leaves upward; the Bulwark in the slot peels away all the same).
         */
        ESCORT_SWAP,
        /** M9 part B: the rescued Rook reached his slot (at him): his barks are back. */
        ESCORT_JOINED,
        /** M9 part B: the Bulwark that peeled away at the escort swap crossed its side's edge (at it): gone. */
        BULWARK_DEPARTED,
        /**
         * M9 part B, step B1b (design/campaign Level 29, the wavering Stormhawks): a hover's peel-off started (at the
         * craft; {@link EnemySpec.Hover#peelSpeed()}); value: its kind.
         */
        PEEL_OFF,
        /**
         * M9 part C (design/enemies, Mine lines): an armed line mine's proximity blink starts (at the mine: the fast
         * tick); its burst follows after the blink ({@link #MINE_BURST} value 1: the ring and the blast).
         */
        MINE_BLINK,
        /**
         * M9 part C (Level 30, C1 a): a line mine set off by a neighbour's burst bursts (at the mine: flash and debris,
         * no ring; the chain crackle), setting off its own neighbours.
         */
        MINE_CHAINED,
        /** M9 part C (Level 30, C2 a): a line mine's blast hit the ship (at the ship); value: the damage, rounded up. */
        MINE_BLAST_HIT,
        /**
         * M9 part C (design/enemies/air/gilded-gunship): a carrier launched a unit of its stream (at the carrier: the
         * bay door's clank, the pneumatic thump); value: the launched unit's kind.
         */
        LAUNCHED,
        /**
         * M9 part C (design/enemies/air/gilded-gunship, the arcs): a direct hit on a unit with facing armour (at the
         * shot); value: {@link ArmourArcs#FRONT} (the armour plate: a grey-gold flash), {@link ArmourArcs#REAR} (the
         * engines: a white flash). Spaced like the glance events.
         */
        PLATING_HIT,
        /**
         * M9 part C (design/enemies/air/rail-serpent, the machine split): a cut's rear cars lose power (at the cut: the
         * coupler snap, the power-down whine); value: the cut member's kind. They drift to a stop, spark and blow
         * from the cut backwards ({@link #CHAIN_POP}), each paying its bounty.
         */
        CHAIN_WRECK,
        /**
         * M9 part C (design/enemies/air/rail-serpent): a turret car's lamp lights before its shot (at the car, its
         * gun's tell); value: its kind.
         */
        CAR_TELL,
        /**
         * M9 part D (design/enemies, Air links; the Shard Drone): an air link lit after its tell (at its middle: the
         * flicker steadies, the hum starts); value: its slot ({@link AirLinks#SLOTS}).
         */
        AIR_LINK_LIT,
        /**
         * M9 part D: a lit air link touched a hull (at the hull); value: {@link AirLinks#TOUCH_SHIP} or {@link
         * AirLinks#TOUCH_SECTION} + the hull escort's section.
         */
        AIR_LINK_TOUCH,
        /**
         * M9 part D: an air link broke for good (at its middle: the snap with a spark); value 0: a drone at its end was
         * destroyed, 1: a drone at its end left the screen (no spark).
         */
        AIR_LINK_BROKEN,
        /**
         * M9 part D: the EMP Burst dropped a lit air link for its drones' stun (at its middle); it relights after its
         * tell; value: its slot.
         */
        AIR_LINK_DROPPED,
        /**
         * M9 part D (design/enemies/air/buzzsaw-drone): a dashing unit's spin-up tell starts (at the unit: the whine
         * rises, the rim flickers red); value: its kind.
         */
        DASH_TELL,
        /** M9 part D: a dashing unit's dash starts along its locked bearing (at the unit); value: its kind. */
        DASH_START,
        /**
         * M9 part D: a dash ricocheted (at the unit: the scrape-clang); value 0: off a play field edge, 1: off a large
         * debris chunk.
         */
        DASH_BOUNCE,
        /**
         * M9 part D: a dashing unit smashed a small debris chunk (at the chunk; it breaks as from shots, {@link
         * #DEBRIS_DESTROYED} follows); value: its kind.
         */
        DASH_SMASH,
        /**
         * M9 part E (design/campaign Level 32, The forks): a fork's branch was chosen (at the ship); value: {@link
         * Forks#code} (fork × 2 + side, {@link Branch#LEFT} 0 / {@link Branch#RIGHT} 1).
         */
        BRANCH_CHOSEN,
        /**
         * M9 part E (design/enemies/ground/strider, The topple): a walker lost a leg and staggers (at the walker: the
         * knee sparks, the fall line is marked); value: its kind. Its fall line: {@link Enemy#fallDirX()}, {@link Enemy#fallDirY()}.
         */
        TOPPLE_TELL,
        /** M9 part E: a staggering walker starts to fall along its line (at the walker); value: its kind. */
        TOPPLE_FALL,
        /**
         * M9 part E: a toppled walker landed (at its fall line's middle: the ground shock and dust ring); value: the
         * number of ground units it crushed (their {@link #ENEMY_DESTROYED} come just before, the walker's just after).
         */
        TOPPLE_CRUSH,
        /**
         * M9 part E (design/world/belt, Mining charges): a mining charge burst (at the charge: the blast); value: the
         * number of enemies its blast destroyed.
         */
        CHARGE_BLAST,
        /**
         * M9 part E (design/campaign Level 32, Blast doors): a cache's lock was destroyed and its crate floats out (at the
         * lock); value: the ground object's index in the level's list (its doors slide open).
         */
        CACHE_OPENED,
        /**
         * M9 part F (design/enemies/space/void-leech, EMP): the EMP Burst threw a latched unit off (the ship's hull or its
         * prey; the shake-off's scrape), at the unit; value: its kind.
         */
        LATCH_THROWN,
        /**
         * M9 part F (the uneasy alliance): a hunting chaser latched on a prey unit, at the chaser on its spot; value: the
         * spot ({@link EnemySpec.Latch#LEFT} ...).
         */
        PREY_LATCHED,
        /**
         * M9 part F: a prey unit was latched: its lights go out, its shield is emptied for good and it leaves its path to
         * drift down (at the prey); value: its kind.
         */
        PREY_DRIFT,
        /**
         * M9 part G (design/campaign Level 34, The freighter): the story pickup's group fell and it starts to rise out of
         * the hold (at the group's centre: the clamps' release, the crate lifting on its seal's glow; {@link
         * StoryPickups}).
         */
        STORY_RISE,
        /**
         * M9 part G: the ship flew through the story pickup (at it; the HUD's event line {@code HELIX CORE SALVAGED});
         * its {@link #PICKUP_COLLECTED} comes just before.
         */
        STORY_COLLECTED,
        /** M9 part G: the story pickup left the screen or ran out of time uncollected (where it was last). */
        STORY_MISSED,
        /**
         * M9 part H (design/enemies/bosses/iron-sovereign; {@link RingArena}): a ring's lights come on in the spin-up (at
         * the station's centre); value: the ring (0 the first, the outer).
         */
        RING_LIT,
        /** M9 part H: the spin-up is over, every ring at speed (at the centre): the station fights; value 0. */
        RINGS_UP,
        /** M9 part H: a stepping ring's teeth flash before its step (at the centre); value: the ring. */
        RING_STEP_TELL,
        /** M9 part H: a stepping ring starts its step (at the centre: the ratchet's clank); value: the ring. */
        RING_STEP,
        /** M9 part H: a stepping ring's step is over and it holds (at the centre); value: the ring. */
        RING_HOLD,
        /**
         * M9 part H: a ring's last battery died: it locks and starts to grind to a halt (at the centre: the screech,
         * its lights out); value: the ring. The boss's phase cue follows when the lock ends its phase.
         */
        RING_LOCKED,
        /** M9 part H: a locked ring's grind is over, it stands still (at the centre); value: the ring. */
        RING_HALTED,
        /**
         * M9 part H: a wrecked battery cooks off in the boss's death (at the battery); value: {@link
         * RingArena#cookoffValue} (ring × {@link RingArena#COOKOFF_RING} + its index on the ring).
         */
        RING_COOKOFF,
        /**
         * M9 part H, step H1b ({@link BossLines}): a boss part's rail line starts its telegraph, locked from the part
         * through the ship (at the part); value: the part.
         */
        BOSS_LINE_TELEGRAPH,
        /** M9 part H: a boss part's rail line fires along its locked line (at its origin); value: the part. */
        BOSS_LINE_FIRED,
        /** M9 part H: a boss's live rail line struck the ship (at the ship); value: the part it came from. */
        BOSS_LINE_HIT,
        /**
         * M9 part H ({@link RingCore}): the shut core's shutter slits glow before its fan (at the core); value: the
         * attack.
         */
        CORE_GLOW,
        /** M9 part H: the station shudders, the rings go dark and the core's petals start to open (at the core). */
        CORE_OPENING,
        /** M9 part H: the core's petals are open: it can be hit, its shield up (at the core). */
        CORE_OPEN,
        /** M9 part H: the open core's reactor brightens before its spiral (at the core). */
        CORE_SPIRAL_TELL,
        /** M9 part H: a rim bay's doors open before its Dart stream (at the bay); value: the bay. */
        BAY_TELL,
        /**
         * Step BOSS70 ({@link RingCore}'s playbook): the open core drew its next attack and its tell starts (at the
         * core); value: its {@link RingSpec.Play} ordinal.
         */
        CORE_PLAY_TELL,
        /** Step BOSS70: the core's rail volley locks its lines and telegraphs (at the core); value: its lines. */
        CORE_VOLLEY_TELEGRAPH,
        /** Step BOSS70: the core's rail volley fires (at the core); value: its lines. */
        CORE_VOLLEY_FIRED,
        /** Step BOSS70: a line of the core's rail volley struck the ship (at the ship); value: the line. */
        CORE_VOLLEY_HIT,
        /** Step BOSS70: the core throws its mine ring (at the core); value: the mines. */
        CORE_MINES_THROWN,
        /** Step BOSS70: a dead battery's wreck is torn off and hurled at the ship (at the battery); value: the part. */
        CORE_WRECK_THROWN,
        /**
         * Step BOSS70: a thrown wreck broke (shot down, or on a hull; where it was): its debris; value: the {@link
         * Missiles} cause. Its {@link #MISSILE_DESTROYED} comes just before.
         */
        CORE_WRECK_BROKEN,
        /** Step BOSS70: the core fell below its playbook's low share, its rests shorten (at the core); value 0. */
        CORE_LOW,
        /**
         * M9 part H ({@link PartShields}): a boss part's hex shield took a hit (spaced as a unit's; at the part); value:
         * the part.
         */
        PART_SHIELD_HIT,
        /** M9 part H: a boss part's hex shield was emptied (a hit, a breaker's hit or the EMP; at the part); value: the part. */
        PART_SHIELD_BROKEN,
        /** M9 part H: a boss part's hex shield is full again (at the part); value: the part. */
        PART_SHIELD_REFILLED,
        /**
         * M9 part H ({@link RelayFlash}, design/campaign Level 35): the mass relay's line is drawn across the field,
         * through the ship's height (at the field's edge the relay lies off, on the line); value 0.
         */
        RELAY_TELEGRAPH,
        /** M9 part H: the mass relay fires along its line (at the edge, on the line); value 0. */
        RELAY_FIRED,
        /** M9 part H: the relay's flash struck the ship (at the ship); value 0. */
        RELAY_HIT,
        /**
         * M10 part A (design/enemies, Reflect): a mirror bounced a player shot back as a slow magenta bullet (at the
         * shot: the bright glassy ping); value: the mount that fired it.
         */
        MIRROR_BOUNCED,
        /**
         * M10 part A: a mirror absorbed a shot with a spark (one inside the bounce gap, Rook's, a bounced bolt, an arc
         * that is not a breaker's; at the shot); value: the mount that fired it.
         */
        MIRROR_ABSORBED,
        /**
         * M10 part A: a mirror broke under a beam (value 0: the glass shatter) or was shattered by a breaker's hit or the
         * EMP Burst (value 1); at the unit. It is down for its down time, then regrows.
         */
        MIRROR_BROKEN,
        /** M10 part A: a broken mirror starts to regrow (the silver shimmer, the low chime; at the unit); value: its kind. */
        MIRROR_REGROWING,
        /**
         * M10 part A (design/enemies, Regeneration): a regenerating unit starts to heal (value 1: the teal seam pulse,
         * the soft rising creak) or stops (value 0: a hit, or full again); at the unit.
         */
        REGENERATING,
        /**
         * M10 part A (design/enemies/ground/graft-turret, Regrowth): a regrowing turret is destroyed and its root
         * exposed (the shell bursts open; at the unit); value: its kind. Its first death also reports {@link
         * #ENEMY_DESTROYED} (the kill); a regrown turret's only this.
         */
        ROOT_EXPOSED,
        /** M10 part A: an exposed root was destroyed: the unit is gone for good (the tearing crumble); value: its kind. */
        ROOT_KILLED,
        /** M10 part A: the root's window closed: the turret knits back over it (not hittable); value: its kind. */
        KNITTING,
        /** M10 part A: the turret stands again with its full HP (it fires after its wake); value: its kind. */
        REGROWN,
        /**
         * M10 part A (design/enemies/ground/shield-pylon): a hit on a unit under a dome spent itself with a gold spark (at
         * the hit; a beam's every few steps); value: the mount (-1 for a special's blast).
         */
        DOME_HIT,
        /** M10 part A: a dome dropped (an EMP stun, a breaker's hit on its pylon; at its centre); value: the pylon's kind. */
        DOME_DOWN,
        /** M10 part A: a dropped dome stands again after its flicker (at its centre); value: the pylon's kind. */
        DOME_UP,
        /** M10 part A: a dome collapsed for good, its pylon gone (the shrinking gold ring; at its centre); value 0. */
        DOME_COLLAPSED,
        /**
         * M10 part A (design/enemies, The three-way war): a hunter's bullet or ram struck a unit of its prey wave (the
         * teal flash where it lands); value: the struck unit's kind.
         */
        PREY_HIT,
        /**
         * M10 part A: a unit was lost to the war, not to the player (a prey unit the hunters killed, a hunter dead on its
         * prey; at the unit): drawn as a kill, paying nothing; value: its kind.
         */
        PREY_KILLED,
        /**
         * M10 part A (design/player/systems, the IFF spoofer): a spoofable unit's volley came due and is held (the amber
         * muzzle stutter; at the unit); value: its kind.
         */
        SPOOF_HESITATE,
        /**
         * M10 part A, step A1b (design/player/shields, Reflex Shield (Act 6)): an enemy bullet or homing missile hit a
         * Reflex Shield inside its window and came back as the ship's bolt (the gold hex flash and the bolt's start; at
         * the hit); value: the reflected damage, rounded.
         */
        REFLEX,
        /**
         * M10 part A, step A1b (design/player/weapons/plasma-arc): a Plasma Arc's strike landed (at the struck point; the
         * arc runs from the nose for the first, from the strike before for a jump; the {@code tesla} zap at a rising
         * pitch); value: {@link ChainFire#event} (the mount and the strike's index).
         */
        CHAIN_STRIKE,
        /**
         * M10 part A, step A1b (design/world/jovian, lightning strikes): a strike's column is told (the sky flash and
         * the thin blue-white line brightening; at the column's x, the top edge); value: the strike's index.
         */
        STRIKE_TOLD,
        /** M10 part A, step A1b: a told column strikes (the bolt and the thunder; at its x, the top edge); value: its index. */
        STRIKE,
        /** M10 part A, step A1b: a strike hit the ship or Rook (at the hull); value: 1 the ship, 2 Rook. */
        STRIKE_HIT,
        /**
         * M10 part A, step A1b (design/world/jovian, eruptions): a vent's tell started (the mouth glowing, the ground
         * cracking, the rising rumble; at the vent); value: the vent's index.
         */
        ERUPTION_TELL,
        /** M10 part A, step A1b: a vent threw its rocks (at the vent); value: the rocks thrown. */
        VENT_ERUPTED,
        /** M10 part A, step A1b: a shot spent itself on a flying rock (at the shot); value: its mount. */
        ROCK_HIT,
        /** M10 part A, step A1b: a rock broke under fire (at the rock); value: its vent. Pays nothing. */
        ROCK_DESTROYED,
        /** M10 part A, step A1b: a rock broke on the ship's or Rook's hull (at the rock); value: its vent. */
        ROCK_STRUCK,
        /**
         * M10 part A, step A1b (design/enemies/ground/shield-pylon, The pylon gates): a held gate's last holder fell and
         * the gate opens (at the holder); value: the gate's index (0 first). Level 29's one arena raises {@link
         * #HOLDERS_DOWN} only.
         */
        GATE_OPENED,
        /**
         * M10 part B (design/enemies/air/chimera, a swarm's fire cap; {@link SwarmVolleys}): a swarm's member skips its
         * next volley under the cap, decided as its glint would start (at the member): the presentation runs its glint
         * clock on as if it fired; value: its kind.
         */
        VOLLEY_CAPPED;

        private static final Type[] VALUES = values();
    }

    /** A {@link Type#WALKER_DOWN} value: the kind and its facing (radians clockwise from straight down). */
    static int walkerValue(int kind, double facing) {
        double turn = 2 * StrictMath.PI;
        return kind * WALKER_KIND + (int) Math.floor(((facing % turn) + turn) % turn * 1000);
    }

    /** The kind of a {@link Type#WALKER_DOWN} value. */
    public static int walkerKind(int value) {
        return value / WALKER_KIND;
    }

    /** The facing of a {@link Type#WALKER_DOWN} value, radians clockwise from straight down. */
    public static double walkerFacing(int value) {
        return value % WALKER_KIND / 1000.0;
    }

    private static final int WALKER_KIND = 10_000;

    /** A {@link Type#CHAIN_POP} value: the member's kind and its hit box's width, px (to 0.1 px, below 100). */
    static int chainPopValue(int kind, double width) {
        return kind * CHAIN_POP_KIND + (int) Math.min(CHAIN_POP_KIND - 1, Math.round(width * 10));
    }

    /** The kind of a {@link Type#CHAIN_POP} value. */
    public static int chainPopKind(int value) {
        return value / CHAIN_POP_KIND;
    }

    /** The hit box width of a {@link Type#CHAIN_POP} value, px. */
    public static double chainPopWidth(int value) {
        return value % CHAIN_POP_KIND / 10.0;
    }

    private static final int CHAIN_POP_KIND = 1_000;

    /** A {@link Type#WRECK_DRIFT} value: the drift across and up, px/s (to 0.1 px/s, within ±499.9). */
    static int driftValue(double driftX, double driftY) {
        return driftTenths(driftX) * DRIFT_SPAN + driftTenths(driftY);
    }

    private static int driftTenths(double drift) {
        return (int) Math.max(0, Math.min(DRIFT_SPAN - 1, Math.round(drift * 10) + DRIFT_SPAN / 2));
    }

    /** The drift across of a {@link Type#WRECK_DRIFT} value, px/s (positive to the right). */
    public static double driftX(int value) {
        return (value / DRIFT_SPAN - DRIFT_SPAN / 2) / 10.0;
    }

    /** The drift up of a {@link Type#WRECK_DRIFT} value, px/s. */
    public static double driftY(int value) {
        return (value % DRIFT_SPAN - DRIFT_SPAN / 2) / 10.0;
    }

    private static final int DRIFT_SPAN = 10_000;

    private final int[] types;
    private final double[] xs;
    private final double[] ys;
    private final int[] values;
    private int size;

    SimEvents(int capacity) {
        types = new int[capacity];
        xs = new double[capacity];
        ys = new double[capacity];
        values = new int[capacity];
    }

    void add(Type type, double x, double y) {
        add(type, x, y, 0);
    }

    void add(Type type, double x, double y, int value) {
        if (size < types.length) {
            types[size] = type.ordinal();
            xs[size] = x;
            ys[size] = y;
            values[size] = value;
            size++;
        }
    }

    void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public Type type(int index) {
        return Type.VALUES[types[index]];
    }

    public double x(int index) {
        return xs[index];
    }

    public double y(int index) {
        return ys[index];
    }

    /** The event's value; its meaning depends on the {@link Type}. */
    public int value(int index) {
        return values[index];
    }

    /** How many events of {@code type} happened in the last step. */
    public int count(Type type) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (types[i] == type.ordinal()) {
                count++;
            }
        }
        return count;
    }
}
