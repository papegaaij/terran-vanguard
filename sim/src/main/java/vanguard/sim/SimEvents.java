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
         * {@link Enemy#empStunned()}. M10 part I: a held set piece stunned (at its centre): value -1 (no unit kind;
         * a machine), its crackle following {@link SetPiece#empStunned()}.
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
        VOLLEY_CAPPED,
        /**
         * M10 part C (design/campaign Level 37, the shuttles; design/ui/hud, the friendly marker): a lift group of air
         * allies lifts off in 2 s: the "FRIENDLIES INBOUND — HOLD FIRE" banner opens ({@link Sortie#friendliesInbound()}
         * holds until the liftoff) and the evacuation PA chime sounds (at the group's pads' centre at its liftoff);
         * value: the group's index (0 first).
         */
        FRIENDLIES_INBOUND,
        /**
         * M10 part D, step D1 (design/campaign Level 38, the CDF supply canister): a debris chunk with a drop broke under
         * the player's shot (its latch) and lets its pickup fall (the latch's break and the parachute torn away; at the
         * pickup, after {@link #DEBRIS_DESTROYED}); round 76 L.1 b: or a chunk that opens by itself reached its height
         * ({@link HazardSpec.DebrisSpec#opensAt}; no {@code DEBRIS_DESTROYED}: the chunk is gone, its release plays on);
         * value: the pickup's {@link PickupType} ordinal.
         */
        DEBRIS_RELEASED,
        /**
         * M10 part F, step F1a (design/enemies/bosses/honour-guard, Entry tells; {@link Squadron}): an ace's first entry
         * is told at its edge (the gold chevron with its call sign, the engines' glint; no edge warning), {@link
         * SquadronSpec#tellSeconds()} before it crosses (on the edge where it will); value: {@link Squadron#tellAce}
         * and {@link Squadron#tellEdge} of it (ace × 4 + its {@link WarningEdge} ordinal).
         */
        ACE_TELL,
        /** M10 part F: an ace crosses its edge and flies in (its bar segment lights; beyond the edge); value: the ace. */
        ACE_JOINED,
        /**
         * M10 part F: an ace died: its shield shatters, a vacuum flash and a debris ring, its hull tumbles away ({@link
         * Squadron#facing}, {@link Squadron#spin}; at the ace); value: the ace. The last one's comes after the boss's
         * {@link #BOSS_DESTROYED}.
         */
        ACE_DOWN,
        /** M10 part F: an ace touched the ship (its contact; at the ship); value: the ace. */
        ACE_CONTACT,
        /**
         * M10 part F, step F1b ({@link AcePlaybook}): an ace starts telling its drawn play (the sniper's barrel glint,
         * Vesper's ribbons flaring or wingtips trailing, Kestrel's engine flare or tail band; the pincer: both flankers'
         * engines and the leader's crest dimming); at the ace; value: {@link AcePlaybook#playAce} and {@link
         * AcePlaybook#playCode} of it (ace × {@link AcePlaybook#PLAYS} + the play's code).
         */
        ACE_PLAY_TELL,
        /** M10 part F, step F1b: an ace's told play starts (its move, its fire); at the ace; value: as {@link #ACE_PLAY_TELL}. */
        ACE_PLAY,
        /**
         * M10 part G, step G1a (design/campaign Level 41, the halts): a held gate's halt starts (the scroll at rest, the
         * level clock standing; at the ship), beside {@link #GATE_OPENED} at its end; value: the gate's index (0 first).
         * Only in a level of several gates (Level 29's one arena raises none).
         */
        GATE_HALTED,
        /**
         * M10 part G, step G1a (Level 41, the secondary {@code FILES}; {@link Uplinks}): an archive core's hatch broke and
         * the core is live (the teal glow; at the core); value: the core's index.
         */
        UPLINK_OPEN,
        /**
         * M10 part G: the ship's centre came within an open core's radius and its upload starts or resumes (the seat tone,
         * the data chatter while {@link Uplinks#uploading}; the ring fills by {@link Uplinks#share}); at the core; value:
         * the core's index.
         */
        UPLINK_PROGRESS,
        /** M10 part G: a core's file is in (the confirmation chirp; at the core); value: the files in so far (1 first). */
        FILE_IN,
        /**
         * M10 part G: a core was lost before its file was in (its centre, or its unbroken hatch, left the bottom edge; at
         * it); value: the core's index.
         */
        FILE_LOST,
        /**
         * M10 part G, step G1b (design/enemies/ground/halo-platform; {@link RingSpec.Tell}): a ringed piece's told attack
         * will fire from this part after its tell (a gun turret's muzzles glow and its barrels stop tracking; the open
         * core's eye flares); at the part; value: the part.
         */
        TURRET_TELL,
        /**
         * M10 part G, step G1b (design/player/systems, the IFF spoofer; {@link BossSpoofing}): a spoofable set piece's
         * volley came due and is held for the spoofer's delay (the amber stutter at its gun); at the part; value: the part.
         */
        BOSS_SPOOFED,
        /**
         * M10 part H, step H1a (design/enemies/bosses/ascendant, Tells; {@link BatteryBursts}): a battery starts telling its
         * burst (its panel's rim glows red as it charges; the burst follows its tell); at the part; value: the part.
         */
        BATTERY_TELL,
        /**
         * M10 part H, step H1a ({@link BossPlaybook}): a boss's phase playbook drew a move and tells it (the spinal lance:
         * the bow's muzzle glowing white-gold; the gold fan: the gun ports flashing; the salvo: the racks' hatches with
         * their red lock lights; the Dart stream: the bays' doors with their gold light); at the source part; value: the
         * move's index in its {@link PhasePlaybook#moves()}.
         */
        BOSS_PLAY_TELL,
        /** M10 part H, step H1a: the told move starts (its fire); at the source part; value: as {@link #BOSS_PLAY_TELL}. */
        BOSS_PLAY,
        /**
         * M10 part H, step H1a ({@link PhaseCues}): a boss's phase cue was raised (its radio subject, once: the
         * Ascendant's {@code first-generator}, {@code cannon-half}); at the boss; value: the cue's index in {@link
         * BossSpec#cues()}.
         */
        BOSS_CUE,
        /**
         * M10 part H, step H1a ({@link PartMirrors}): a boss part's mirror panel broke under a beam's heat (white-hot,
         * cracking, then shards); at the part; value: the part.
         */
        PART_MIRROR_BROKEN,
        /**
         * M10 part H, step H1a: a boss part's mirror panel shattered at once (a {@code shield-breaker} arc, the EMP
         * Burst); at the part; value: the part.
         */
        PART_MIRROR_SHATTERED,
        /**
         * M10 part H, step H1a: a boss part's mirror panel's down time is over and it grows back (the 0.4 s silver
         * shimmer; it stands again after it); at the part; value: the part.
         */
        PART_MIRROR_REGROWING,
        /**
         * M10 part H, step H1b ({@link PartRegens}): a boss part starts to heal (the lime core's healing pulse, the deeper
         * regrowth creak); at the part; value: the part.
         */
        PART_HEALING,
        /** M10 part H, step H1b: a boss part stops healing (a hit, full again, its death); at the part; value: the part. */
        PART_HEALING_STOPPED,
        /**
         * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks; {@link EscapeRun}): an escaping boss's
         * hull breaks (K: the heart bursts, the surviving sacs with it, the keel chain starts); at the boss; value: the
         * hull's credit shower (its parts' credits). No {@link #BOSS_DESTROYED} follows.
         */
        HULL_BROKEN,
        /**
         * M10 part H, step H1b: the broken hull's flash ({@link EscapeSpec#flashSeconds()} after K: the act boss's screen
         * flash, the credit shower over the wreck, the {@code huge} rung's sound); at the boss; value: the shower's credits.
         */
        HULL_FLASH,
        /**
         * M10 part H, step H1b: the lifeboat tears free of the heart's cavity ({@link EscapeSpec#breakoutSeconds()} after
         * K); at its breakout place; value: its part.
         */
        LIFEBOAT_AWAY,
        /**
         * M10 part H, step H1b: the lifeboat reached the top edge (its flash toward the gate, the escape sting cuts the
         * boss track): the fight is over, the clock resumes; at its last place; value: the set piece.
         */
        BOSS_ESCAPED,
        /**
         * M11 part A (design/enemies, Window shields): a hit on a closed window shield sparks off it (the pale gold
         * flake; no damage); at the hit; value: the unit's kind. Every shot, arc or chain strike raises one; a beam on
         * the shell one every {@link EnemySpec.WindowShield#tickGapSeconds()}.
         */
        SHELL_FLAKE,
        /**
         * M11 part A: the closed shell's dull stone tick (the sound), at most one every {@link
         * EnemySpec.WindowShield#tickGapSeconds()} per unit; at the hit; value: the unit's kind.
         */
        SHELL_TICK,
        /**
         * M11 part A: a window shield opens (its line's telegraph, a breaker hit, the EMP; the iris draws back and the
         * stone grind); at the unit; value: its kind.
         */
        SHELL_OPEN,
        /** M11 part A: a window shield is closed again (after its beam, or regrown after a break); value: its kind. */
        SHELL_CLOSE,
        /**
         * M11 part A: a {@code shield-breaker} hit or the EMP Burst broke a window shield open for its hold (each hit
         * that restarts the hold raises one); at the unit; value: its kind.
         */
        SHELL_BROKEN,
        /**
         * M11 part A (design/enemies/space/gate-warden, The leaving tell): a hovering unit with an exit up starts its
         * leaving tell (the last seconds of its station: the glyphs flicker, it tilts toward its edge); value: its kind.
         */
        LEAVING_TELL,
        /**
         * M11 part A (design/enemies, Distorted space): a reversal is told ahead (Rook's call, the HUD's edge chevron,
         * the groan); at the field's centre; value: the reversal's index in the level.
         */
        REVERSAL_TOLD,
        /** M11 part A: a told reversal starts running (the scroll eases through zero); value: its index. */
        REVERSAL_RUN,
        /**
         * M11 part A (design/enemies, Movement pattern vocabulary: {@code teleport}): a teleporting unit starts its
         * fade out (hittable for the fade); at the unit; value: its kind.
         */
        JUMP_OUT,
        /**
         * M11 part A: a teleporting unit is gone and its arrival ring forms (shrinking onto its point); at the ring's
         * point; value: its kind.
         */
        JUMP_RING,
        /** M11 part A: a teleporting unit lands at its ring's point; value: its kind. */
        ARRIVE,
        /**
         * M11 part A, step A1b (design/enemies/bosses, Boss rush): the refill between two fights (the shield and
         * armour to full, the special's charges, Rook's armour; the HUD's 1 s fill and chime); at the ship.
         */
        BOSS_REFILL,
        /**
         * M11 part A, step A1b: a rush's fight begins (its boss's gate halts, its checkpoint recorded; the HUD's {@code
         * ECHO n / 3}); at the ship; value: the fight, 0 first.
         */
        FIGHT_NEXT,
        /**
         * M11 part A, step A1b (design/world/vrell-space, Spore clouds): the ship's centre entered a spore cloud (its
         * slow eases in); at the ship.
         */
        SPORE_INSIDE,
        /** M11 part A, step A1b: the ship's centre left the spore clouds (the slow eases out); at the ship. */
        SPORE_LEFT,
        /**
         * M11 part A, step A1b: an {@code area} hit or a Plasma Arc strike set a spore cloud burning (it burns away
         * in 1 s); at the cloud's centre; value: its radius, px.
         */
        SPORE_BURN,
        /**
         * M11 part A, step A1b (design/world/vrell-space, The quiet; Level 46): the quiet is broken (the player hit a
         * unit or a unit fired): Rook's pick takes the units again; at the ship.
         */
        QUIET_BROKEN,
        /**
         * M11 part A, step A1b (design/player/specials, the Vrell Swarm Call): the call (the bloom behind the ship, the
         * drones out); at the bloom; value: the drones.
         */
        SWARM_CALLED,
        /** M11 part A, step A1b: a Swarm Call drone loosed a thorn; at the drone; value: the drone's slot. */
        SWARM_THORN,
        /** M11 part A, step A1b: an enemy bullet struck a Swarm Call drone (and is gone); at the drone; value: its slot. */
        SWARM_DRONE_HIT,
        /** M11 part A, step A1b: a Swarm Call drone was shot down; at the drone; value: its slot. */
        SWARM_DRONE_LOST,
        /** M11 part A, step A1b: the Swarm Call's drones wither (its life is over: 0.5 s); at the ship. */
        SWARM_WITHER,
        /**
         * M11 part A, step A1b (design/player/weapons/choir-resonator, the {@code wave} delivery): a wave left the
         * nose; at the nose; value: the mount.
         */
        WAVE_FIRED,
        /**
         * M11 part A, step A1b: a wave struck a hull; at the hull; value: the hull's index in the wave (0 first: the
         * clang's pitch, −6 % a step; throttled to one per 0.12 s per wave).
         */
        WAVE_HIT,
        /**
         * M11 part B, step B1 (design/world/vrell-space, gate transit; Level 43): the transit's formation starts (no
         * input, no hits; the ship and Rook ease into formation); at the ship.
         */
        TRANSIT_FORM,
        /**
         * M11 part B, step B1: the transit's warp starts (the renderer's radial pull and grade to the event plane's
         * violet; the transit's sound); at the formation point.
         */
        TRANSIT_WARP,
        /**
         * M11 part C, step CD1 (design/enemies/ground/spawning-organ): a spawner's whirl cluster is due: its pod swells
         * over the release's telegraph (at its centre); value: its kind.
         */
        ORGAN_SWELL,
        /**
         * M11 part C, step CD1: a spawner released its whirl cluster (the seeds spiral out of its centre, at it); value:
         * its kind. Its fan-out releases (the organ's Skitters) stay {@link #SPAWN_TELEGRAPH} / {@link
         * #SPAWN_RELEASED}.
         */
        ORGAN_RELEASE,
        /**
         * M11 part C, step CD1 (design/world/vrell-space, Living terrain; {@link Pits}): a pit's tell starts, its lips
         * glow and part (at the pit); value: the pit's index among the level's.
         */
        PIT_TELL,
        /** M11 part C, step CD1: a pit opens and its wave rises out of it (at the pit); value: the pit's index. */
        PIT_OPEN,
        /** M11 part C, step CD1: a pit closes again after its wave rose (at the pit); value: the pit's index. */
        PIT_CLOSE,
        /**
         * M11 part C, step CD1: a pit due to open holds closed, the ship within its hold distance (at the pit; once per
         * opening); value: the pit's index.
         */
        PIT_HELD,
        /**
         * M11 part D, step CD1 (design/enemies/space/husk, the crack; {@link Cracking}): a unit's crack starts its tell
         * (the hull flares; at it); value: its kind.
         */
        CRACK_TELL,
        /** M11 part D, step CD1: a cracking unit fires its fan of shards (at it); value: its kind. */
        CRACK_FAN,
        /**
         * M11 part D, step CD1 (design/campaign Level 46, the quiet's end): the level's crack cue starts: every armed
         * cracking unit on the screen cracks in turn, farthest from the ship first (at the ship); value: how many.
         */
        CRACK_CUE,
        /**
         * M11 part E, step E1 (design/enemies/air/choir-seraph, the cycle's fold; {@link EnemySpec.Teleport.Cycle}): a
         * cycling teleporter folds before a jump (it holds still and silent, hittable: the jump's tell); at the unit;
         * value: its kind.
         */
        JUMP_FOLD,
        /**
         * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The rear sweep; {@link RearSweep}): a boss's rear
         * sweep is told: its far flank flares (Rook's call); at that flank; value: its side, +1 right, -1 left.
         */
        SWEEP_TELL,
        /** M11 part F, step F1: the rear sweep's drop out of the top edge starts; at the boss; value: the tell's flank. */
        SWEEP_OUT,
        /**
         * M11 part F, step F1: the rear sweep is out of sight: the point on the bottom edge it will rise from is marked
         * (its glow; the boss's own tell, never an edge warning); at that point (y 0); value: its side, +1 right.
         */
        SWEEP_MARK,
        /** M11 part F, step F1: the rear sweep rises from the bottom edge at its mark; value: its side, +1 right. */
        SWEEP_CLIMB,
        /** M11 part F, step F1: the rear sweep is over: the boss is back in its sway; at the boss; value: its side. */
        SWEEP_DONE,
        /**
         * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Death; {@link LastWords}): K: a boss that dies on
         * its last words lost its vital part: it stops firing, its bar goes, it hangs convulsing; at the boss; value: its
         * set piece. Its {@link SimEvents.Type#BOSS_DESTROYED} comes with its burst ({@link #LAST_BURST}).
         */
        LAST_WORDS,
        /**
         * M11 part F, step F1: at K the enemy bullets on the screen start their fade (harmless from now, gone at its
         * end); F1b: every enemy projectile, the homing missiles and the lobs in flight too; at the boss; value: how
         * many.
         */
        BULLETS_FADE,
        /**
         * M11 part F, step F1: the last words are over: the boss bursts (its death chain and credit shower follow with
         * {@link SimEvents.Type#BOSS_DESTROYED}); at the boss; value: its set piece.
         */
        LAST_BURST,
        /**
         * M11 part G, step G1 (design/campaign Level 49, the echoes; {@link EchoDissolve}): an echo forms as it arrives
         * (its rise from its station, or an anchored one's forming); at the boss; value: its set piece.
         */
        ECHO_FORM,
        /**
         * M11 part G, step G1: an echo's death chain is over: it dissolves into motes (no flash); every enemy projectile
         * starts its fade; at the boss; value: its set piece.
         */
        ECHO_DISSOLVE,
        /**
         * M11 part G, step G1: a unit a boss launched dissolves with its echo, unpaid and uncounted; at the unit; value:
         * its kind.
         */
        ECHO_UNIT_DISSOLVE,
        /**
         * M11 part H, step H1a (design/enemies/bosses/choir-heart, Arena; {@link ArenaWalls}): a half-wall's veins glow
         * for a told move (the walls' grow, a breath inward, a retreat); at the half's face; value: the half (0 the left
         * wall's upper, 1 its lower, 2 the right wall's upper, 3 its lower).
         */
        WALL_TOLD,
        /**
         * M11 part H, step H1a (design/enemies/bosses/choir-heart, the checkpoint at each phase's start): the boss
         * checkpoint is recorded again as a later phase starts; at the boss; value: the phase.
         */
        PHASE_CHECKPOINT,
        /**
         * M11 part H, step H1b (design/enemies/bosses/choir-heart, the membranes; {@link PartWindows}): a living part's
         * window opens (its song's tell, a breaker's or the EMP's hold: the mouth irises open); at the part; value: the
         * part.
         */
        PART_WINDOW_OPEN,
        /** M11 part H, step H1b: a living part's window closes (its song's last bullet, a hold's end); value: the part. */
        PART_WINDOW_CLOSE,
        /**
         * M11 part H, step H1b (design/enemies/bosses/choir-heart, Attack: the heartbeat): the heartbeat's ring fires
         * (the pulse's peak: the looks' pulse and its sound in time); at the boss; value: the phase.
         */
        HEARTBEAT,
        /**
         * M11 part H, step H1b (design/enemies/bosses/choir-heart, Death: the act's last flash at K + 3.5 s): a boss
         * dying on its last words with a {@link LastWordsSpec#flashSeconds()} flashes now (the act boss's screen flash,
         * under the flash-reduction option, instead of at its chain's start); at the boss; value: its set piece.
         */
        BOSS_FLASH,
        /**
         * M11 part H, step H1b (design/campaign Level 50, the closing gate; {@link GateTransit}): a beat of the closing
         * gate scene: the hive greys, the groan, the line {@code THE GATE IS CLOSING}; at the field's centre; value: the
         * beat's value.
         */
        GATE_CLOSING,
        /**
         * M11 part H, step H1b: the warp home: a reversed transit's warp starts (one-way grade to the starfield, no
         * flash, no inversion; instead of {@link #TRANSIT_WARP}); at the formation point; value 0.
         */
        WARP_HOME,
        /** M11 part H, step H1b: a beat of the closing gate scene: the near side (Neptune, the radio's noise 0). */
        NEAR_SIDE,
        /** M11 part H, step H1b: a beat of the closing gate scene: the event plane shrinks to a point and goes out. */
        PLANE_OUT,
        /** M11 part H, step H1b: a beat of the closing gate scene: a glyph of the ring goes dark; value: the glyph (1…). */
        GLYPH_DARK;

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
