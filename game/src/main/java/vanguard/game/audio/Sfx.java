package vanguard.game.audio;

import static vanguard.game.audio.Sfx.Priority.AMBIENCE;
import static vanguard.game.audio.Sfx.Priority.BOSS;
import static vanguard.game.audio.Sfx.Priority.ENEMY_FIRE;
import static vanguard.game.audio.Sfx.Priority.EXPLOSION;
import static vanguard.game.audio.Sfx.Priority.PICKUP;
import static vanguard.game.audio.Sfx.Priority.PLAYER_FIRE;
import static vanguard.game.audio.Sfx.Priority.WARNING;

import vanguard.content.Tier;
import vanguard.content.campaign.Hangar;

/**
 * The sound effects: the chosen concept sounds of design/audio/sfx, imported
 * into {@code assets/} by {@code :pipeline:importPlaceholders}, each with its instance limit
 * (design/audio/sfx, Mixing rules): beyond it the oldest instance is stopped, the {@link Bus}
 * whose volume it follows, and its {@link Priority} for the global voice limit ({@link SfxBank}).
 */
public enum Sfx {
    PULSE_SHOT("sfx/player-shot-r02-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    /** The weapon sound families of the Act 1 arsenal (design/audio/sfx, Weapon sound families). */
    VULCAN_SHOT("sfx/shot-vulcan-r03-b.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),

    BALLISTIC_SHOT("sfx/player-shot-r02-d.ogg", 3, Bus.EFFECTS, PLAYER_FIRE),
    LASER_SHOT("sfx/shot-laser-r03-b.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    MICROMISSILE_SHOT("sfx/shot-micromissile-r03-a.ogg", 3, Bus.EFFECTS, PLAYER_FIRE),
    MORTAR_SHOT("sfx/shot-mortar-r03-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    BOMB_SHOT("sfx/shot-bomb-r03-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    /** The Act 2 families: the Hornet Launcher's rocket launch and the proximity mine's drop-and-bounce clunk. */
    MISSILE_SHOT("sfx/shot-missile-r03-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),

    MINE_DROP("sfx/shot-mine-r03-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    /**
     * A proximity mine arming (round 28 a, provisional until the round closes: a soft rising
     * two-blip chirp); two at a time, so mines arming together do not stack.
     */
    MINE_ARM("sfx/weapon-mine-arm-r28-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    OVERDRIVE_START("sfx/overdrive-start-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    OVERDRIVE_END("sfx/overdrive-end-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /** Hits are the player's fire landing: they share its priority. */
    HIT_ORGANIC_A("sfx/hit-organic-r08-a.ogg", 4, Bus.EFFECTS, PLAYER_FIRE),
    HIT_ORGANIC_B("sfx/hit-organic-r08-b.ogg", 4, Bus.EFFECTS, PLAYER_FIRE),
    EXPLOSION_TINY_A("sfx/explosion-tiny-r03-a.ogg", 6, Bus.EFFECTS, EXPLOSION),
    EXPLOSION_TINY_B("sfx/explosion-tiny-r03-b.ogg", 6, Bus.EFFECTS, EXPLOSION),
    SHIELD_HIT("sfx/player-shield-hit-r08-a.ogg", 2, Bus.EFFECTS, WARNING),
    SHIELD_BREAK("sfx/player-shield-break-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** The shield full again after a break (round 08 a, a rising charge). */
    SHIELD_RESTORE("sfx/player-shield-restore-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    ARMOUR_HIT("sfx/player-armour-hit-r08-a.ogg", 2, Bus.EFFECTS, WARNING),
    /** One beep of the low-armour warning (round 08 a), repeated slowly at 30 % armour and fast at 15 %. */
    LOW_ARMOUR("sfx/player-low-armour-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    SHIP_DESTROYED("sfx/player-destroyed-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    ENEMY_SHOT_A("sfx/enemy-shot-small-r08-a.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    ENEMY_SHOT_B("sfx/enemy-shot-small-r08-b.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * A heavy (medium-bullet) enemy shot (round 08 a and b): played for an {@code ENEMY_FIRED} event
     * whose value, the bullet's damage, is a medium bullet's (FlightSounds).
     */
    ENEMY_HEAVY_SHOT_A("sfx/enemy-shot-heavy-r08-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),

    ENEMY_HEAVY_SHOT_B("sfx/enemy-shot-heavy-r08-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** The {@code small} explosion family (Needler and other small units). */
    EXPLOSION_SMALL_A("sfx/explosion-r02-a.ogg", 6, Bus.EFFECTS, EXPLOSION),
    EXPLOSION_SMALL_B("sfx/explosion-small-r03-a.ogg", 6, Bus.EFFECTS, EXPLOSION),
    /** The small ladder's third file, a heavier pop: destroyed ground targets. */
    EXPLOSION_SMALL_C("sfx/explosion-r02-b.ogg", 4, Bus.EFFECTS, EXPLOSION),
    /** The {@code medium} rung: medium units (Mantis, Scuttler, Spore Bomber, Brood Pod) and set-piece parts. */
    EXPLOSION_MEDIUM_A("sfx/explosion-r02-c.ogg", 4, Bus.EFFECTS, EXPLOSION),
    EXPLOSION_MEDIUM_B("sfx/explosion-medium-r03-b.ogg", 4, Bus.EFFECTS, EXPLOSION),
    /** The {@code large} rung: a mid-boss's death, a boss's phase ends, a set piece's break-up. */
    EXPLOSION_LARGE_A("sfx/explosion-r02-d.ogg", 3, Bus.EFFECTS, EXPLOSION),
    EXPLOSION_LARGE_B("sfx/explosion-large-r03-a.ogg", 3, Bus.EFFECTS, EXPLOSION),
    /** The large rung's boss blast ("boss destroyed / capital ship"): under an act boss's final blast. */
    EXPLOSION_LARGE_C("sfx/explosion-r02-e.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** The {@code huge} rung: an act boss's or a huge set piece's death (a recorded TNT blast with debris). */
    EXPLOSION_HUGE_A("sfx/explosion-huge-r04-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** The huge rung's sub-heavy boom: under the Smart Bomb's blast. */
    EXPLOSION_HUGE_B("sfx/explosion-huge-r03-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** A large destroyed ground target crumbling (round 08 a, a building collapse). */
    CRUMBLE_LARGE("sfx/hit-crumble-r08-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** A small destroyed ground target's rubble burst (round 08 b). */
    CRUMBLE_SMALL("sfx/hit-crumble-r08-b.ogg", 3, Bus.EFFECTS, EXPLOSION),
    /** Hits on machines and ground objects such as cargo containers. */
    HIT_METAL_A("sfx/hit-metal-r08-a.ogg", 4, Bus.EFFECTS, PLAYER_FIRE),
    HIT_METAL_B("sfx/hit-metal-r08-b.ogg", 4, Bus.EFFECTS, PLAYER_FIRE),
    SALVAGE_SMALL("sfx/pickup-salvage-small-r08-a.ogg", 2, Bus.EFFECTS, PICKUP),
    /** Salvage medium (round 01 c, the rising sweep with sparkles). */
    SALVAGE_MEDIUM("sfx/pickup-r01-c.ogg", 2, Bus.EFFECTS, PICKUP),
    /** Salvage large: a set piece's drop, the hidden crate, the boss's credit shower. */
    SALVAGE_LARGE("sfx/pickup-salvage-large-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    SHIELD_CELL("sfx/pickup-shield-cell-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    ARMOUR_PATCH("sfx/pickup-armour-patch-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /** A special charge picked up (round 08 a, three rising notes and a bell). */
    SPECIAL_CHARGE("sfx/pickup-special-charge-r08-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /** The overdrive pickup (round 01 a, the standard power-up arpeggio), with the overdrive's start cue. */
    POWER_UP("sfx/pickup-r01-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /** A data core picked up (round 01 b, the two-bell chime for rare upgrades). */
    DATA_CORE("sfx/pickup-r01-b.ogg", 1, Bus.EFFECTS, PICKUP),
    RADIO_OPEN("sfx/ui-radio-open-r08-a.ogg", 1, Bus.RADIO, WARNING),
    RADIO_CLOSE("sfx/ui-radio-close-r08-a.ogg", 1, Bus.RADIO, WARNING),
    TYPEWRITER("sfx/ui-typewriter-r08-a.ogg", 2, Bus.RADIO, WARNING),
    TALLY_TICK("sfx/ui-tally-tick-r08-a.ogg", 2, Bus.EFFECTS, WARNING),
    TALLY_TOTAL("sfx/ui-tally-total-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    GRADE_STAMP("sfx/ui-grade-stamp-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * The catapult run of a level's launch (round 47 b, the user's pick, from the Freesound previews until the
     * originals: a real carrier launch under an afterburner, a coupler's steel clank at the stop, steam; 4.95 s):
     * its stop, the release, is 3.55 s in.
     */
    LAUNCH_RAIL("sfx/launch-rail-r47-b.ogg", 1, Bus.EFFECTS, WARNING),
    /** The contact ping with an edge warning (round 11 b); one at a time, so overlapping warnings do not stack. */
    EDGE_WARNING("sfx/ui-edge-warning-r11-b.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * The warning klaxon (round 08 a, a 4.76 s loop played once): under an act boss's warning banner
     * and the boss warning track (design/campaign, Level 07 music).
     */
    KLAXON("sfx/ui-klaxon-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * The Leviathan's whale-song cry under its death's explosion (round 47 b, the user's pick, from the Freesound
     * previews until the originals: a real humpback's moan, high then a long deep groan, slowed to 0.8; 4 s).
     */
    LEVIATHAN_CRY("sfx/enemy-leviathan-cry-r47-b.ogg", 1, Bus.EFFECTS, BOSS),
    /** The Polyp Mortar's lob (round 21 a, a real mortar thump). */
    MORTAR_LOB("sfx/enemy-mortar-lob-r21-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** The Polyp Mortar's blob landing on its marker (round 21 a, a wet splat). */
    MORTAR_IMPACT("sfx/enemy-mortar-impact-r21-a.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /** The mass-driver rail's charge hum while the lights chase before a sled (round 21 a, a 0.78 s loop). */
    SLED_WHINE("sfx/hazard-sled-whine-r21-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** A sled racing up the rail (round 21 b, a rushing flyby). */
    SLED_PASS("sfx/hazard-sled-pass-r21-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** The Mantis's 0.6 s telegraph before a sweep (round 23 a, a laser charge-up): a warning. */
    MANTIS_TELEGRAPH("sfx/enemy-mantis-telegraph-r23-a.ogg", 2, Bus.EFFECTS, WARNING),
    /** The Mantis's beam sweep (round 23 b, a death ray with crackle, CC-BY). */
    MANTIS_SWEEP("sfx/enemy-mantis-sweep-r23-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** The perimeter beacon firing a flare shell (round 23 a, a flare-gun shot, CC-BY). */
    FLARE_LAUNCH("sfx/hazard-flare-launch-r23-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** A flare burning as it falls (round 23 a, a road flare, a seamless 3 s loop played back to back). */
    FLARE_BURN("sfx/hazard-flare-burn-r23-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** A cut Coilwyrm's rear part growing its new head (round 23 b, an insect growl and chitter). */
    COILWYRM_REGROW("sfx/enemy-coilwyrm-regrow-r23-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * The Vrell screech (round 08 c and d, in turn) as a large Vrell unit enters the screen: the
     * Mantis, the Coilwyrm's head, the Spore Bomber, the Scuttler; at most one every 3 s
     * ({@link ScreechCue}).
     */
    ENEMY_SCREECH_C("sfx/enemy-screech-r08-c.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),

    ENEMY_SCREECH_D("sfx/enemy-screech-r08-d.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** A Coilwyrm's chain cut through: the body torn in two (round 27 a, a wet flesh rip). */
    COILWYRM_CUT("sfx/enemy-coilwyrm-cut-r27-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** A Brood Pod bursting into its Skitters, shot or on its own (round 08 b, the fleshy burst). */
    BROOD_BURST("sfx/enemy-spawn-r08-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * A Coilwyrm segment or tail bursting, shot or in the chained death's ripple (round 24 b, the
     * fleshy burst). Six instances, for segments shot together by a spread.
     */
    COILWYRM_BURST("sfx/enemy-coilwyrm-burst-r24-b.ogg", 6, Bus.EFFECTS, EXPLOSION),
    /** A Coilwyrm head's (or regrown head's) burst that starts the chained death (round 24 c, the slowed splatter). */
    COILWYRM_HEAD_BURST("sfx/enemy-coilwyrm-head-burst-r24-c.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * The Brood Carrier's roar as it arrives and, lower, as it turns broadside (round 25 b, a bear's
     * roar over a didgeridoo drone).
     */
    CARRIER_ROAR("sfx/enemy-carrier-roar-r25-b.ogg", 1, Bus.EFFECTS, BOSS),
    /** A bay sac's membrane parting as its window opens (round 25 b, a tear out of sucking mud). */
    CARRIER_SAC_OPEN("sfx/enemy-carrier-sac-open-r25-b.ogg", 2, Bus.EFFECTS, BOSS),
    /** A bay sac sucking shut as its window closes (round 25 b, the opening's tear reversed). */
    CARRIER_SAC_CLOSE("sfx/enemy-carrier-sac-close-r25-b.ogg", 2, Bus.EFFECTS, BOSS),
    /** A unit spat out of an open sac (round 25 b, a slime lunge). */
    CARRIER_LAUNCH("sfx/enemy-carrier-launch-r25-b.ogg", 3, Bus.EFFECTS, BOSS),
    /** A bay sac bursting, shot or in the death chain (round 25 a, a very wet, fleshy explosion). */
    CARRIER_SAC_BURST("sfx/enemy-carrier-sac-burst-r25-a.ogg", 3, Bus.EFFECTS, BOSS),
    /** The plate iris opening over the core (round 25 b, a grinding organic morph). */
    CARRIER_IRIS("sfx/enemy-carrier-iris-r25-b.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M5 part C: a Hive Node's iris opening, the telegraph of its release (design/audio/sfx: the Brood
     * Carrier's chosen iris b reused, a grinding organic morph).
     */
    HIVE_IRIS("sfx/enemy-carrier-iris-r25-b.ogg", 2, Bus.EFFECTS, WARNING),
    /** M5 part C: a Hive Node releasing its Skitters (design/audio/sfx: the Vrell spawn a, a wet creature swell). */
    HIVE_SPAWN("sfx/enemy-spawn-r08-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part C: a Ravager taking off for its pounce (round 31, both kept: a, a rasping dog-like snarl
     * over a quick swish; b, a snapping attack roar over a heavier whoosh). The game picks one at
     * random per pounce ({@link FlightSounds#pounce}).
     */
    RAVAGER_POUNCE_A("sfx/ravager-pounce-r31-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),

    RAVAGER_POUNCE_B("sfx/ravager-pounce-r31-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part C: the arcology's collapse (Level 09; round 31 a): a stony rumble that swells over the
     * collapse's 1.5 s lean and 1.5 s drop into a tumbling masonry crash at the impact, {@link
     * FlightSounds#COLLAPSE_CRASH_SECONDS}, about 7 s in all; played as the warning starts ({@link
     * FlightSounds#COLLAPSE_AT_WARNING}).
     */
    ARCOLOGY_COLLAPSE("sfx/collapse-r31-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M5 part D: a Wraith decloaking, as its 0.4 s flash starts (design/audio/sfx, Level 10; round 32
     * b): a metallic sci-fi rip over a membrane swish, 0.6 s.
     */
    WRAITH_DECLOAK("sfx/wraith-decloak-r32-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part D: a Mote Swarm's rush of wings, one-shot as a swarm enters the screen and again at each
     * loop-back (round 32 b): an insect chitter over a whoosh, 1.7 s.
     */
    MOTE_SWARM("sfx/mote-swarm-r32-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part D: the lance of Level 10's scripted loss (round 32 a): an alien falling whine into a
     * thunder crack and its roll, 3.8 s, started {@link FlightSounds#LANCE_IMPACT_SECONDS} before the
     * hit so its impact (1.2 s in) lands on it.
     */
    LANCE_STRIKE("sfx/lance-r32-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M5 part E: the Torpedo Pod's launch and the Harpoon's (design/audio/sfx, weapon family {@code
     * torpedo}; round 03): a muffled launch with bubbles.
     */
    SHOT_TORPEDO("sfx/shot-torpedo-r03-a.ogg", 3, Bus.EFFECTS, PLAYER_FIRE),
    /** M5 part E: a kill or a landing blast on the water's surface (round 03), over its size rung. */
    EXPLOSION_WATER("sfx/explosion-water-r03-a.ogg", 3, Bus.EFFECTS, EXPLOSION),
    /** M5 part E: a kill or a torpedo's impact under the water (round 03 a), over its size rung. */
    EXPLOSION_UNDERWATER("sfx/explosion-underwater-r03-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M5 part E, the Harbour Kraken (round 33 a, accepted): a slam's rush and impact; the impact is
     * {@link FlightSounds#SLAM_IMPACT_SECONDS} into the file, so it starts as the arm starts to rise.
     */
    KRAKEN_SLAM("sfx/kraken-slam-r33-a.ogg", 2, Bus.EFFECTS, BOSS),
    /** M5 part E: a lane's telegraph, a surge of waves over a rumble swelling for 1.0 s (round 33 a). */
    KRAKEN_CHURN("sfx/kraken-churn-r33-a.ogg", 3, Bus.EFFECTS, BOSS),
    /** M5 part E: the head surfacing, a deep swell peaking 1.1 s in as the crown breaks (round 33 a). */
    KRAKEN_SURFACE("sfx/kraken-surface-r33-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** M5 part E: the Kraken's death, a long wet groan under the {@code huge} water burst (round 33 a). */
    KRAKEN_DEATH("sfx/kraken-death-r33-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** M5 part E: a convoy ship hit by a slam (round 33 a): metal struck, water rushing in. */
    SHIP_HIT("sfx/ship-hit-r33-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /** M5 part E: a ship sinking, from its loss (round 33 a): a steel groan, a muffled boom, waves washing over; 3.8 s. */
    SHIP_SINK("sfx/ship-sink-r33-a.ogg", 3, Bus.EFFECTS, EXPLOSION),
    /** M5 part E: the escort frigate's distant flak (round 33 a, raised to -16 dB); one file per burst. */
    FRIGATE_FLAK("sfx/frigate-flak-r33-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M5 part E: a Driftjelly's proximity ring firing (round 46 b, the user's pick: a deep structural groan, 0.9 s). */
    DRIFTJELLY_PULSE("sfx/driftjelly-pulse-r46-b.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part F, Level 12's thunder (round 34 b, the user's pick: a close strike, a crack then the roll,
     * at three speeds from near to far; 7.1, 6.1 and 5.6 s): three variants, one 0.3 to 1.5 s after
     * each lightning flash ({@link FlightSounds}); one of each at a time (a variant comes back after
     * three flashes, 10.5 s at the closest).
     */
    THUNDER_1("sfx/thunder-1-r34-b.ogg", 1, Bus.EFFECTS, EXPLOSION),

    THUNDER_2("sfx/thunder-2-r34-b.ogg", 1, Bus.EFFECTS, EXPLOSION),
    THUNDER_3("sfx/thunder-3-r34-b.ogg", 1, Bus.EFFECTS, EXPLOSION),
    /**
     * M5 part F: a gust's wind, started with its telegraph and panned from the upwind side (round 34 b,
     * the user's pick: a recorded gust that rises over the 1.0 s telegraph, holds through the gust and
     * falls; 3.6 s).
     */
    GUST("sfx/gust-r34-b.ogg", 2, Bus.EFFECTS, WARNING),
    /** M5 part F: a Lamprey latching onto the hull (round 34 a, the user's pick: suck and bite). */
    LAMPREY_LATCH("sfx/lamprey-latch-r34-a.ogg", 2, Bus.EFFECTS, WARNING),
    /** M5 part F: the drain's loop while a Lamprey drains the shield (round 46 b: a Tesla coil's hum, a 2.4 s loop). */
    LAMPREY_DRAIN("sfx/lamprey-drain-r46-b.ogg", 1, Bus.EFFECTS, WARNING),
    /** M5 part F: the shake-off throwing the latched Lampreys off (round 34 a, a wet slap and a squeal). */
    LAMPREY_SHAKE("sfx/lamprey-shake-r34-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** M5 part F: a weather sonde collected (round 46 a: a heavy clamp slamming shut with a radio squelch, 1.0 s). */
    SONDE_CHIME("sfx/sonde-chime-r46-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /**
     * M5 part G, Level 13's Nansen Relay (round 35 a, the user's pick in round 34's review): a relay-aimed
     * hit on it, struck plating with a spark (0.55 s; every hit, so short).
     */
    RELAY_HIT("sfx/relay-hit-r35-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M5 part G: the relay's integrity alarm, a hi-lo station alert three times (round 35 a, 1.5 s), once
     * as it drops below 50 % and once below 25 % (the {@code ally-below} events), under the radio's line.
     */
    INTEGRITY_ALARM("sfx/integrity-alarm-r35-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** M5 part G: the relay destroyed, a power-down over a blast and collapsing metal (round 35 a, 3.7 s). */
    RELAY_DESTROYED("sfx/relay-destroyed-r35-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** M5 part G: a supply drone lifting off the relay's helipad, rotors spinning up and receding (round 46 a, 2.2 s). */
    DRONE_LAUNCH("sfx/drone-launch-r46-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /**
     * M5 part G: grid online, a power charge spinning up into a breaker's contact clunk and a chord
     * (round 35 b, the user's pick; 2.6 s), as the relay's lights come on in sequence when the
     * synchronisation completes.
     */
    GRID_ONLINE("sfx/grid-online-r35-b.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * M5 part H, Level 14's root burst (round 36 a, the user's pick in round 34's review): the paving
     * under stress through the 1 s telegraph, then the root breaking out at 1.0 s (2.1 s), from the
     * burst's telegraph event.
     */
    ROOT_BURST("sfx/root-burst-r36-a.ogg", 2, Bus.EFFECTS, WARNING),
    /**
     * M7 part B, the Burrower's surfacing (round 36 a, chosen; design/audio/sfx, Act 3 sounds): the tell's
     * rumble swelling over its first 1.0 s, then the break-out and the sand raining down (2.1 s). Played
     * at the bulge's start, so the impact lands as it surfaces, with {@link #BURROW_BULGE} over it.
     */
    BURROW_ERUPTION("sfx/burrow-eruption-r36-a.ogg", 2, Bus.EFFECTS, WARNING),
    /**
     * M7 part B: the Burrower's bulge crack layer (round 38 b, the user's pick of 2026-10-14): sand and crust
     * cracking faster towards the break (1.27 s), started with {@link #BURROW_ERUPTION} at the bulge's start.
     */
    BURROW_BULGE("sfx/burrow-bulge-r38-b.ogg", 2, Bus.EFFECTS, WARNING),
    /**
     * M7 part C: a hull touching a canyon wall or rock island (round 36 b, S.3, the metal screech with a
     * spark hiss, 0.6 s): once per touch, panned to the touching side, a little under the damage sound.
     */
    CANYON_SCRAPE("sfx/canyon-scrape-r36-b.ogg", 2, Bus.EFFECTS, WARNING),
    /**
     * M7 part C: a standing tendril barrier's hum (round 39 a, proposed: a steady 120 Hz harmonic drone, a seamless
     * 6 s loop, levelled in its file): one loop for the nearest standing tendril, panned to it, rising from the
     * dormant tell into the flare.
     */
    TENDRIL_HUM("sfx/tendril-hum-r39-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part C: a beam burning a tendril (round 46 a, the user's pick: a welder's crackle, a seamless 2 s loop). */
    TENDRIL_BURN("sfx/tendril-burn-r46-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part C: a tendril breaking (round 46 b: a cable snapping with a whip crack and a fan winding down, 0.9 s). */
    TENDRIL_BREAK("sfx/tendril-break-r46-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part C: the Threadcrawler's walk (round 46 a, the user's pick: a many-legged skitter over gravel, a
     * seamless 3 s loop): one loop for the nearest crawler on screen, panned to its head.
     */
    CRAWLER_WALK("sfx/crawler-walk-r46-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part D: a dome of Level 17 taking a hit ({@code ALLY_HIT} on a ground ally; round 49 b, the user's pick, from
     * the Freesound previews until the originals: a hammer striking a glass pane over a struck glass table's ring an
     * octave down, in a cathedral, 1.2 s), panned to the dome.
     */
    DOME_HIT("sfx/dome-hit-r49-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part D: a dome breaking ({@code ALLY_LOST} on a ground ally; round 40's redo c, chosen in round 41 (S.h):
     * a low-passed building collapse over a deep crust rumble with smashing glass, 2.4 s, from CC0 Freesound
     * originals), under the radio's ally-loss line.
     */
    DOME_BREAK("sfx/dome-break-r40-c.ogg", 1, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part D: the Choir Herald's song with its ring of bullets (round 47 a, the user's pick, from the Freesound
     * previews until the originals: a concert-hall choir's swelling chord slowed to 0.7 over throat singing, 2.4 s),
     * instead of a gun's report.
     */
    HERALD_SONG("sfx/herald-song-r47-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part D: the Herald's aura (round 46 b, the user's pick: a choir's held chord, high-passed, a 4 s loop,
     * levelled in its file): one loop while any unit carries a ring, panned to the nearest ringed unit.
     */
    HERALD_AURA("sfx/herald-aura-r46-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part D: a ring absorbing a hit (round 46 b: a force field's hit with sparks, 0.4 s), spaced out. */
    AURA_ABSORB("sfx/aura-absorb-r46-b.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    /**
     * M7 part D: the rings collapsing at their Herald's death, or stripped by the EMP Burst (round 40 a, proposed: a
     * falling chord of five voices with a sheet of ring-glass shimmer, 2.0 s).
     */
    AURA_COLLAPSE("sfx/aura-collapse-r40-a.ogg", 1, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part D: the Shellback's walk (round 46 b, the user's pick: two heavy stomps and stone knocks, a seamless
     * 4.16 s loop): one loop for the nearest walking Shellback on screen, panned to it.
     */
    SHELLBACK_WALK("sfx/shellback-walk-r46-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part D: a Shellback's mortar lob (round 46 b: a slowed mortar shot and a pressure hiss, 0.64 s). */
    SHELLBACK_LOB("sfx/shellback-lob-r46-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part D: a Shellback curling up (round 46 b: a heavy stone grind closing on a knock, 0.85 s). */
    SHELLBACK_CURL("sfx/shellback-curl-r46-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part D: a Shellback's roll (round 46 b: a large low rumble with stone dragging, a seamless 2.5 s loop):
     * one loop for the nearest rolling Shellback, panned to it.
     */
    SHELLBACK_ROLL("sfx/shellback-roll-r46-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part D: a Shellback uncurling (round 46 b: a stone door grinding open, a clack, sand sliding off, 0.7 s). */
    SHELLBACK_UNCURL("sfx/shellback-uncurl-r46-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part E: the Dust Devil's spin (round 46 a, the user's pick: inside a tornado with a sandstorm's grit, a
     * seamless 3.0 s loop): one loop for the nearest Dust Devil on screen, panned to it.
     */
    DEVIL_SPIN("sfx/devil-spin-r46-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part E: a Dust Devil's rise and opening (round 46 a: a gust over a heavy stone door grinding open, 0.7
     * s), from the start of its 0.4 s rise, the tell.
     */
    DEVIL_OPEN("sfx/devil-open-r46-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part E: a Dust Devil's grit spiral (round 46 a: a dust throw and sliding sand, 0.47 s), for its volleys. */
    DEVIL_SPIRAL("sfx/devil-spiral-r46-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part E: a Ghost Drone dropping into its lane (round 46 b: a jet's close flyby gliding down, 0.9 s). */
    GHOST_DRONE_DROP("sfx/ghost-drone-drop-r46-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part E: a Ghost Drone's 0.3 s eye glint before its burst (round 46 b: a shutter and a bolt release, 0.3 s). */
    GHOST_DRONE_GLINT("sfx/ghost-drone-glint-r46-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part E: a Ghost Drone's burst (round 46 a: a rifle's three-round burst, 0.6 s), once at its first shot. */
    GHOST_DRONE_BURST("sfx/ghost-drone-burst-r46-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part E: a Ghost Drone's death (round 46 b: a drone crashing into a wall, debris and sparks, 0.87 s): the {@code
     * small} rung's Ghost Drone entry ({@code EnemyLooks}).
     */
    GHOST_DRONE_DEATH("sfx/ghost-drone-death-r46-b.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part F: a Revenant Walker's footfall (round 46 a, the user's pick: a big robot's footstep and a hydraulic
     * hiss, 1.0 s), at each foot that comes down. Round 44: pitched down, its stomp's shockwave (a placeholder).
     */
    WALKER_STOMP("sfx/walker-stomp-r46-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a walker's pod hatch opening 0.4 s before its salvo, the tell (round 43 b, the user's pick of
     * 2026-10-20: a submarine hatch's clank and creak over a steam release, a deep impact under the clank, 1.01 s).
     * Round 44: pitched down, the legs bracing for a stomp (a placeholder until its own sound).
     */
    WALKER_HATCH("sfx/walker-hatch-r43-b.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: one homing missile leaving a walker's pod (round 43: the chosen enemy missile launch of round 42, S.c,
     * the user's pick of 2026-10-20).
     */
    WALKER_MISSILE("sfx/enemy-missile-r08-a.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part F: the walker's flamer's tell (round 43 a, the user's pick of 2026-10-20: a huge gas vent opening, 0.85 s). */
    WALKER_FLAME_TELL("sfx/walker-flame-tell-r43-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: the walker's flame cone burning (round 43 b, the user's pick of 2026-10-20: a flamethrower's deep roar,
     * a seamless 4 s loop), faded in and out with the burn.
     */
    WALKER_FLAME("sfx/walker-flame-r43-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * Round 43 (SNDI): a hit on the walker's open reactor vents (round 43 a, the user's pick of 2026-10-20: a big stereo
     * metal hit, 0.55 s), instead of the plain hit, at most one each 0.15 s.
     */
    WALKER_VENT_HIT("sfx/walker-vent-hit-r43-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * Round 43 (SNDI): the walker's death (round 43 a, the user's pick of 2026-10-20: a large explosion with debris, the
     * hull landing with a massive impact at ≈ 1.85 s and a metal crash, 5.6 s), its rung instead of the generic blast;
     * the impact lands with the 2 s topple.
     */
    WALKER_DEATH("sfx/walker-death-r43-a.ogg", 1, Bus.EFFECTS, EXPLOSION),
    /**
     * Round 43 (SNDI): the walker's wreck settling (round 43 a, the user's pick of 2026-10-20: a bunker door's low
     * metallic groan over an engine cooling and ticking, 5.0 s, quiet by design), once as the topple lands, under the
     * hint-2 crawl.
     */
    WALKER_WRECK_SETTLE("sfx/walker-wreck-settle-r43-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: the Warden Tank's tracks (round 43 a, the user's pick of 2026-10-20: a T-26 tank driving, tracks and
     * engine, a seamless 4 s loop): one loop for the nearest tank on screen, panned to it.
     */
    TANK_TRACKS("sfx/tank-tracks-r43-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a Warden Tank's turret slewing onto the ship (round 43 a, the user's pick of 2026-10-20: an artillery
     * turret re-aligning, 0.8 s).
     */
    TANK_TURRET("sfx/tank-turret-r43-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a Warden Tank's burst (round 43 a, the user's pick of 2026-10-20: a tank cannon, three shots 0.15 s
     * apart, 1.18 s), once at the first shot of each burst instead of the shots' reports.
     */
    TANK_BURST("sfx/tank-burst-r43-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a Warden Tank's death (round 46 a, the user's pick: an explosive, a vehicle crash and a flame burst,
     * 1.8 s): the {@code medium} rung's Warden Tank entry ({@code EnemyLooks}).
     */
    TANK_DEATH("sfx/tank-death-r46-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part F: a furnace vent's tell (round 43 a, the user's pick of 2026-10-20: a balloon burner's roar swelling in,
     * 1.05 s), as a row's grates start to glow; a ripple's grates share one.
     */
    VENT_TELL("sfx/vent-tell-r43-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a furnace vent's burst (round 43 a, the user's pick of 2026-10-20: a deep flame burst, a gas burner's
     * burn dying away, 1.6 s), as its flame column stands up.
     */
    VENT_BURST("sfx/vent-burst-r43-a.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part F: a conveyor belt's rumble (round 42 a, proposed: a roller rumble, plate seams knocking and a motor
     * hum, a seamless 2.4 s loop): one quiet loop while a belt runs on the screen, under the {@code mars-foundry} bed.
     */
    BELT_RUMBLE("sfx/belt-rumble-r42-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M8 part B: an ice geyser's tell (round 51 a, proposed: a frozen lake's ice cracking over a pressure release's
     * rising hiss, 1.2 s), as each crack of a row darkens and glows (the vents' {@code look: ice-geyser}, Level 22).
     */
    GEYSER_TELL("sfx/geyser-tell-r51-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M8 part B: an ice geyser's burst (round 51 a, proposed: a steam safety valve's roar over a sea blowhole's spout
     * and a muffled lake-ice boom, 1.5 s), as its steam column stands up.
     */
    GEYSER_BURST("sfx/geyser-burst-r51-a.ogg", 3, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M8 part B: a Drill Rig's collar boring (round 51 a, proposed: an oil rig's drill whine over a digging rumble, a
     * seamless 4 s loop): one quiet loop while a rig's collar turns on the screen, panned to the one nearest the ship.
     */
    RIG_COLLAR("sfx/rig-collar-r51-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M8 part B: a Drill Rig's ring of ice shards (round 51 a, proposed: a hammer smashing ice on an air hose's burst,
     * 0.7 s), instead of the shots' report.
     */
    RIG_RING("sfx/rig-ring-r51-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M8 part B: a Drill Rig's core destroyed (round 51 a, proposed: a metal crash on a lake-ice boom, a groan and a
     * steam release, 3.1 s), with its {@code large} explosion.
     */
    RIG_DEATH("sfx/rig-death-r51-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part G: the pursuit swarm's band hum (round 43 a, proposed: a designed swarm drone slowed to 0.85 over a bee
     * box's hive hum, a seamless 6 s loop, -32 dB): one loop while a pursuit holds the top band, panned to its leader
     * point.
     */
    SWARM_HUM("sfx/swarm-hum-r43-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part G: the pursuit's lunge (round 43 a, proposed: the hum swelling in and gliding up, a whoosh rushing in at
     * its end, 0.8 s, -20 dB; no pop): through the tell, its whoosh on the dive.
     */
    SWARM_LUNGE("sfx/swarm-lunge-r43-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M7 part H: the Dust Colossus's bulge, a breach's tell and an eruption's marker (round 44 a, proposed: an
     * earthquake's cracking soil and stone swelling in, sand pouring off the bulge, 2.1 s, -16 LUFS); a warning.
     */
    COLOSSUS_BULGE("sfx/colossus-bulge-r44-a.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * M7 part H: the mouth breaching, at the rise and at an eruption (round 44 a, proposed: an earth impact's boom, a
     * deep roar rising out of it, rock and sand pouring back, 2.6 s).
     */
    COLOSSUS_ERUPTION("sfx/colossus-eruption-r44-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M7 part H: the mouth's suction draw (round 44 a, proposed: air rushing into a chimney under a sandstorm, a
     * seamless 4 s loop, -28 dB band RMS as the walker's flame): while the mouth is open and through its bite's tell,
     * louder the nearer the ship.
     */
    COLOSSUS_SUCTION("sfx/colossus-suction-r44-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M7 part H: the jaws snapping shut (round 44 a, proposed: a massive structure's deep impact with bones crunching
     * and a large stone scraping, 0.9 s).
     */
    COLOSSUS_BITE("sfx/colossus-bite-r44-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M7 part H: the Colossus's death (round 44 a, proposed: a deep roar shuddering, chained eruptions, a massive blast
     * at ≈ 2.6 s, then a rock avalanche subsiding, 6.1 s), its own instead of the huge rung's blast, over the 3 s chain.
     */
    COLOSSUS_DEATH("sfx/colossus-death-r44-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M7 part H: Level 21's ridge pass (round 44 a, the user's pick: a subway train passing under a bridge with sand
     * flowing off the ridge, mirrored to pan right to left as Level 21's ridge runs, 7 s, -20 LUFS).
     */
    COLOSSUS_RIDGE("sfx/colossus-ridge-r44-a.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * Round 44 (step HB): the Revenant Walker's rail lance charging (round 44 b, the user's pick: a high-voltage
     * discharge's buzz swelling and gliding up, Tesla sparks on top, a low rumble rising; 1.05 s, about 5 LU louder
     * than a).
     */
    WALKER_LANCE_CHARGE("sfx/walker-lance-charge-r44-b.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /** Round 44 (step HB): the rail lance's shot (round 44 a, the user's pick: a real railgun, 1.6 s). */
    WALKER_LANCE_SHOT("sfx/walker-lance-shot-r44-a.ogg", 1, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * Round 44 (step HB): the stomp's shockwave (round 44 a, the user's pick: a giant's footfall and an explosion's shockwave
     * rolling out of it, 1.6 s).
     */
    WALKER_SHOCKWAVE("sfx/walker-shockwave-r44-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M7 part B: a Burrower's 0.4 s dive, the grain slide (round 46 a, the user's pick: sand sliding off a shovel). */
    BURROW_DIVE("sfx/burrow-dive-r46-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /** M5 part H: a burst's growths growing in, a stretching creak set with a dull knock at 1.45 s (round 36 a, one proposal). */
    ROOT_GROWTH("sfx/root-growth-r36-a.ogg", 2, Bus.EFFECTS, ENEMY_FIRE),
    /**
     * M5 part H: the Siege Spire tearing free at phase 3's lift, a creature's bellow rising with the
     * lift over wet tearing and an air shockwave (round 36 b, the user's pick; 3.4 s).
     */
    TEAR_FREE_ROAR("sfx/tear-free-roar-r36-b.ogg", 1, Bus.EFFECTS, BOSS),
    /**
     * M5 part H: the canopy withering after the Spire's death, a dying exhale, a groan and a sigh
     * settling into a soft wind (round 36 b, the user's pick; 7.5 s).
     */
    CANOPY_WITHERING("sfx/canopy-withering-r36-b.ogg", 1, Bus.EFFECTS, BOSS),
    /** A tow's cable snapping as its pod falls free: Level 07's lifeboat (round 25 a, a chain snap). */
    CABLE_SNAP("sfx/secret-cable-snap-r25-a.ogg", 1, Bus.EFFECTS, EXPLOSION),
    /** The Airstrike's jets flyby (round 08 a), as the bombers enter. */
    AIRSTRIKE_JETS("sfx/special-airstrike-jets-r08-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** The Airstrike's bomb carpet (round 08 a), from its first blast. */
    AIRSTRIKE_BOMBS("sfx/special-airstrike-bombs-r08-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** The Smart Bomb's charge-up and white-out boom (round 08 a). */
    SMART_BOMB("sfx/special-smartbomb-r08-a.ogg", 1, Bus.EFFECTS, BOSS),
    /** The special button with no charge, no special or a strike still flying (round 08 a). */
    SPECIAL_DENIED("sfx/special-denied-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /**
     * M7 part A, the Act 3 arsenal (design/audio/sfx, round 37's picks of 2026-10-13: b for the rock impact,
     * the deflector's return and the lance's rumble, a for the rest; the beam's loop round 36's chosen a;
     * round 46's picks for the beam's stop and impact, the drone rebuild, the EMP, the overcharge and the
     * dash): the Ion Beam's start, played with its loop, and its stop.
     */
    ION_BEAM_START("sfx/ion-beam-start-r37-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),

    ION_BEAM_LOOP("sfx/ion-beam-loop-r36-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    ION_BEAM_STOP("sfx/ion-beam-stop-r46-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    /** M7 part A: the beam's sizzle loop on a target it burns, and on rock (a ground object). */
    BEAM_IMPACT("sfx/beam-impact-r46-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),

    BEAM_IMPACT_ROCK("sfx/beam-impact-rock-r37-b.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    /** M7 part A: a Deflector Pod's catch tick and its charge's return. */
    DEFLECTOR_TICK("sfx/deflector-tick-r37-a.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),

    DEFLECTOR_RETURN("sfx/deflector-return-r37-b.ogg", 1, Bus.EFFECTS, PICKUP),
    /** M7 part A: a light drone breaking, and its bay's rebuilt drone fading in. */
    DRONE_BREAK("sfx/drone-break-r37-a.ogg", 2, Bus.EFFECTS, EXPLOSION),

    DRONE_REBUILD("sfx/drone-rebuild-r46-a.ogg", 1, Bus.EFFECTS, PICKUP),
    /** M7 part A: the EMP Burst's ring, and a stunned unit's power-down whine. */
    EMP_RING("sfx/emp-ring-r46-b.ogg", 1, Bus.EFFECTS, BOSS),

    EMP_POWERDOWN("sfx/emp-powerdown-r46-a.ogg", 2, Bus.EFFECTS, EXPLOSION),
    /**
     * M7 part A: the Orbital Lance's targeting line (the chosen lock beep, round 08 a), its column's loop
     * (the `beam` family's loop c, round 04) and the rumble under it.
     */
    LANCE_LOCK("sfx/enemy-lock-r08-a.ogg", 1, Bus.EFFECTS, BOSS),

    LANCE_LOOP("sfx/shot-beam-r04-c.ogg", 1, Bus.EFFECTS, BOSS),
    LANCE_RUMBLE("sfx/lance-rumble-r37-b.ogg", 1, Bus.EFFECTS, BOSS),
    /** M7 part A: the Shield Overcharge's activation whoosh and its end shimmer. */
    OVERCHARGE_WHOOSH("sfx/overcharge-whoosh-r46-b.ogg", 1, Bus.EFFECTS, BOSS),

    OVERCHARGE_END("sfx/overcharge-end-r46-b.ogg", 1, Bus.EFFECTS, PICKUP),
    /** M7 part A: a cooldown special ready again (the HUD ring's chime). */
    COOLDOWN_READY("sfx/cooldown-ready-r37-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** M7 part A: the Evasive thrusters' burst as a dash starts. */
    DASH("sfx/dash-r46-b.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    /**
     * M8 part A, the Act 4 arsenal (design/audio/sfx, round 50's picks, 2026-10-22: launch b, run a, hit a, roll-out b,
     * burst a; cut from the Freesound previews until their originals): the Harpoon Torpedoes' launch, its run (one loop
     * while any harpoon is in flight) and its hit; the Depth Charges' roll-out and burst.
     */
    SHOT_HARPOON("sfx/shot-harpoon-r50-b.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),

    HARPOON_RUN("sfx/harpoon-run-r50-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    HARPOON_HIT("sfx/harpoon-hit-r50-a.ogg", 3, Bus.EFFECTS, EXPLOSION),
    SHOT_DEPTH_CHARGE("sfx/shot-depth-charge-r50-b.ogg", 2, Bus.EFFECTS, PLAYER_FIRE),
    DEPTH_CHARGE_BURST("sfx/depth-charge-burst-r50-a.ogg", 3, Bus.EFFECTS, EXPLOSION),
    /** M8 part A: the Sonar Pulse's ping (round 04 a, chosen) and the Decoy Flares' launch (round 08 a, chosen). */
    SPECIAL_SONAR("sfx/special-sonar-r04-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),

    SPECIAL_FLARES("sfx/special-flares-r08-a.ogg", 1, Bus.EFFECTS, PLAYER_FIRE),
    /** M8 part A: the special's row changing state at a waterline (round 50 b, chosen; synthesized). */
    SPECIAL_LIMIT("sfx/special-limit-r50-b.ogg", 1, Bus.EFFECTS, WARNING),
    /** The Earth-orbit ambience, looped (design/audio/sfx, Ambience per setting). */
    AMBIENCE_ORBIT("sfx/ambience-orbit-r08-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /** The Luna ambience, looped (design/audio/sfx, Ambience per setting): Level 04. */
    AMBIENCE_LUNA("sfx/ambience-luna-r08-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /** The Earth megacity ambience (night city, distant sirens), looped: Level 08 (M5 part B). */
    AMBIENCE_CITY("sfx/ambience-city-r08-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * The Earth ocean ambience (waves at speed), looped: Level 10 from its section 4 (M5 part D,
     * brought forward from part E), then Level 11.
     */
    AMBIENCE_OCEAN("sfx/ambience-ocean-r08-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M5 part F: the Earth ocean storm's rain-only bed (user decision F6 = a; round 34 a, accepted:
     * the CC0 source of round 08's chosen storm ambience after its thunder, a seamless 10 s loop), so
     * the thunder can follow the lightning: Level 12 but in the eye.
     */
    AMBIENCE_STORM("sfx/ambience-storm-rain-r34-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M5 part G: the Earth arctic ambience (round 08 a, chosen: cold wind over the pack ice, CC0), looped:
     * Level 13, about 3 dB louder in the whiteout ({@link LevelMusic#ambienceLevels}).
     */
    AMBIENCE_ARCTIC("sfx/ambience-arctic-r08-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M5 part H: the Vrell canopy's hum over Geneva (round 46 a, the user's pick: a deep voice drone over a
     * furnace hum, a seamless 16 s loop, 3 dB under the megacity's bed), Level 14's ambience instead of the megacity's sirens until the Siege
     * Spire dies; then the megacity's own ambience comes back ({@link #afterBoss}).
     */
    AMBIENCE_CANOPY("sfx/canopy-hum-r46-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part B: Level 15's descent through the thin air (round 38's entry roar a, the user's pick, a
     * seamless 12 s loop), sections 1-2, crossfading to {@link #AMBIENCE_MARS_DUST} at section 3.
     */
    AMBIENCE_MARS_ENTRY("sfx/entry-roar-r38-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /** M7 part B: the Martian dust wind (round 38 a, the user's pick, a seamless 16 s loop): Level 15 from section 3. */
    AMBIENCE_MARS_DUST("sfx/mars-dust-r38-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part C: the Martian dust wind with a canyon howl (round 46 a, the user's pick, a seamless 16 s loop):
     * Level 16 from its start.
     */
    AMBIENCE_MARS_CANYON("sfx/mars-canyon-r46-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part D: Ares Landing's colony ambience (round 46 b, the user's pick: a thin sand wind, a ventilation hum and
     * a distant freight train, a seamless 16 s loop): Level 17 from its start.
     */
    AMBIENCE_MARS_COLONY("sfx/mars-colony-r46-b.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part E: the Martian dust storm (round 36 a, chosen, "recorded front": a sandstorm over a furious low howl, a
     * seamless 16 s loop): Level 18 in the storm, swelling as the view closes in ({@code StormLooks#swellDb}).
     */
    AMBIENCE_MARS_STORM("sfx/dust-storm-bed-r36-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part F: the Tharsis foundry (round 46 b, the user's pick: a furnace, the blowers, a steel mill's drone, the
     * plateau wind and a far hammer, a seamless 16 s loop): Level 19 from its start.
     */
    AMBIENCE_MARS_FOUNDRY("sfx/mars-foundry-r46-b.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M7 part G: the basin rumble (round 43 a, proposed: an earthquake's deep earth rumble with rock grinding in it, a
     * seamless 16 s loop, -28 LUFS): Level 20's ambience layer under the travelling ridge ({@code ambience_layers}).
     */
    AMBIENCE_BASIN_RUMBLE("sfx/basin-rumble-r43-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M8 part B: Europa's ice (round 51 a, proposed: a glacial wind over lake-ice fractures an octave down, the deep
     * booms and groans rolling under it, a seamless 16 s loop): Level 22 to its run-in.
     */
    AMBIENCE_EUROPA_ICE("sfx/europa-ice-r51-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /**
     * M8 part B: the run-in to Conamara Station (round 51 a, proposed: the Europa ice bed with an underground hall's
     * ventilation hum and a transformer's drone, a seamless 16 s loop): Level 22's section 3.
     */
    AMBIENCE_CONAMARA("sfx/conamara-station-r51-a.ogg", 1, Bus.EFFECTS, AMBIENCE),
    /** The mission complete jingle (design/audio/music, track 23), played as a one-shot. */
    MISSION_COMPLETE("music/mission-complete.ogg", 1, Bus.MUSIC, WARNING),
    /** The music's failure sting (design/audio/music, track 25), played over the cut music. */
    MISSION_FAILED("music/mission-failed.ogg", 1, Bus.MUSIC, WARNING),
    /** Track 21, the mini-boss sting (round 08 a "Contact Heavy"). */
    MINIBOSS_STING("music/miniboss-sting.ogg", 1, Bus.MUSIC, WARNING),
    /** The game over cue (design/audio/music, track 26), played as a one-shot. */
    GAME_OVER("music/game-over.ogg", 1, Bus.MUSIC, WARNING),
    MENU_MOVE("sfx/ui-menu-move-r08-a.ogg", 2, Bus.EFFECTS, WARNING),
    MENU_CONFIRM("sfx/ui-menu-confirm-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    MENU_BACK("sfx/ui-menu-back-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** The hangar's purchase: an item or a special charge bought, a repair paid ({@link #shop}). */
    SHOP_BUY("sfx/ui-shop-buy-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** The hangar's sale, and an undo's refund. */
    SHOP_SELL("sfx/ui-shop-sell-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** The hangar refusing a choice: can't afford, won't fit (power), at its most. */
    SHOP_DENIED("sfx/ui-shop-denied-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** An item fitted to a slot or moved to the inventory. */
    EQUIP("sfx/ui-equip-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** A weapon upgraded a level. */
    UPGRADE("sfx/ui-upgrade-r08-a.ogg", 1, Bus.EFFECTS, WARNING),
    /** A save written: the hangar's autosave as it opens, a save to a slot (round 27 b, the calm chime). */
    SAVE_DONE("sfx/ui-save-r27-b.ogg", 1, Bus.EFFECTS, WARNING);

    /**
     * Which voices give way when more than {@link VoiceLimit#MAX_VOICES} effects play (design/audio/sfx,
     * Mixing rules), lowest first: a new sound steals the oldest voice of the lowest priority at or
     * below its own, and is dropped when every voice ranks above it.
     */
    public enum Priority {
        /** Setting loops; a running loop is never stolen (it would not come back). */
        AMBIENCE,
        PICKUP,
        /** The player's shots and their hits. */
        PLAYER_FIRE,
        /** Enemy shots, beams and hazards. */
        ENEMY_FIRE,
        /** Explosions, bursts and crumbling targets. */
        EXPLOSION,
        /** A boss's own sounds and the specials' (rare, long and called by the player). */
        BOSS,
        /** Warnings and player damage, and the interface: menus, radio, the hangar, the music's cues. */
        WARNING
    }

    private final String path;
    private final int instanceLimit;
    private final Bus bus;
    private final Priority priority;

    Sfx(String path, int instanceLimit, Bus bus, Priority priority) {
        this.path = path;
        this.instanceLimit = instanceLimit;
        this.bus = bus;
        this.priority = priority;
    }

    public String path() {
        return path;
    }

    public int instanceLimit() {
        return instanceLimit;
    }

    public Bus bus() {
        return bus;
    }

    public Priority priority() {
        return priority;
    }

    /**
     * A setting's ambience loop by its key, a level's {@code music.ambience} (design/audio/sfx,
     * Ambience per setting).
     */
    public static Sfx ambience(String setting) {
        return switch (setting) {
            case "earth-orbit" -> AMBIENCE_ORBIT;
            case "luna" -> AMBIENCE_LUNA;
            case "earth-megacity" -> AMBIENCE_CITY;
            case "earth-ocean" -> AMBIENCE_OCEAN;
            case "earth-storm" -> AMBIENCE_STORM;
            case "earth-arctic" -> AMBIENCE_ARCTIC;
            case "earth-canopy" -> AMBIENCE_CANOPY;
            case "mars-entry" -> AMBIENCE_MARS_ENTRY;
            case "mars-dust" -> AMBIENCE_MARS_DUST;
            case "mars-canyon" -> AMBIENCE_MARS_CANYON;
            case "mars-colony" -> AMBIENCE_MARS_COLONY;
            case "mars-storm" -> AMBIENCE_MARS_STORM;
            case "mars-foundry" -> AMBIENCE_MARS_FOUNDRY;
            // M7 part G: an ambience layer over the setting's (Level 20's ridge), not a setting of its own.
            case "basin-rumble" -> AMBIENCE_BASIN_RUMBLE;
            // M8 part B: Europa's ice shelf and the station's bed (Level 22).
            case "europa-ice" -> AMBIENCE_EUROPA_ICE;
            case "conamara-station" -> AMBIENCE_CONAMARA;
            default -> throw new IllegalArgumentException("no ambience for " + setting + " yet");
        };
    }

    /**
     * M5 part H: the ambience that takes over from {@code ambience} when the level's boss dies, or null
     * when it plays on: the canopy's hum dies with the Siege Spire and the megacity's own ambience comes
     * back (Level 14's daylight).
     */
    public static Sfx afterBoss(Sfx ambience) {
        return ambience == AMBIENCE_CANOPY ? AMBIENCE_CITY : null;
    }

    /**
     * An enemy's explosion by its size tier (design/audio/sfx, Explosion ladder), variant a or b: two
     * files a rung, alternated so no file plays twice in a row.
     */
    public static Sfx explosion(Tier tier, boolean variantB) {
        return switch (tier) {
            case TINY -> variantB ? EXPLOSION_TINY_B : EXPLOSION_TINY_A;
            case SMALL -> variantB ? EXPLOSION_SMALL_B : EXPLOSION_SMALL_A;
            case MEDIUM -> variantB ? EXPLOSION_MEDIUM_B : EXPLOSION_MEDIUM_A;
            case LARGE -> variantB ? EXPLOSION_LARGE_B : EXPLOSION_LARGE_A;
            case HUGE -> variantB ? EXPLOSION_HUGE_B : EXPLOSION_HUGE_A;
        };
    }

    /**
     * The hangar's sound for a shop action that was made (design/audio/sfx, UI and radio): buying an
     * item or a charge, selling, fitting or unfitting (and the power dial's free refit), upgrading. A refused choice plays
     * {@link #SHOP_DENIED}, a paid repair {@link #SHOP_BUY} and an undo's refund {@link #SHOP_SELL}.
     */
    public static Sfx shop(Hangar.Action action) {
        return switch (action) {
            case BUY, BUY_CHARGE -> SHOP_BUY;
            case SELL -> SHOP_SELL;
            case FIT, UNFIT, POWER_DOWN, POWER_UP -> EQUIP;
            case UPGRADE -> UPGRADE;
        };
    }
}
