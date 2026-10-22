package vanguard.game.audio;

import java.util.List;
import vanguard.game.level.LowArmour;
import vanguard.game.render.BulletLooks;
import vanguard.game.render.EnemyLooks;
import vanguard.sim.Armament;
import vanguard.sim.EnemySpec;
import vanguard.sim.PickupType;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SplitMix64;
import vanguard.sim.WarningEdge;

/**
 * Turns the simulation's events into sound effects, mixed per design/audio/sfx (Mixing rules):
 * relative levels, a few percent of random pitch, two variants alternating so no file repeats
 * twice in a row, and a subtle pan by the position in the play field. Explosions follow the size
 * ladder: a set piece's part blows with the {@code medium} rung, a mid-boss and a boss's phase end
 * with the {@code large} one, an act boss or a huge set piece with the {@code huge} one. Level
 * 03's events reuse the sounds there are until they get their own: a spore drops with a soft low
 * organic plop, bursts or is shot with the tiny explosion, debris rings like metal and breaks with
 * a low tiny explosion. Besides the events it follows the ship's state ({@link #watch}): the
 * low-armour beeps and the shield's restore chime; and the large Vrell units entering the screen,
 * which screech.
 *
 * <p>Each event type belongs to one family ({@link SoundFamily}): the player's side ({@link PlayerCues}),
 * the bosses ({@link BossCues}; Level 19's walker in {@link WalkerCues}), the enemy units ({@link UnitCues}, Level 18's in
 * {@link StormCues}, Level 19's tanks, vents and belts in {@link FoundryCues}, Level 20's pursuit swarm in {@link
 * PursuitCues}, Level 21's Dust Colossus in {@link ColossusCues}), the allies ({@link AllyCues}), the
 * arsenal ({@link ArsenalCues}) and the levels' own ({@link LevelCues}); they share one {@link SoundMix},
 * and this class routes the events to them in their order and keeps the order of the watched states.
 */
public final class FlightSounds {
    /** The levels relative to player damage, the loudest group (+2 dB in the mixing rules). */
    static final float PLAYER_DAMAGE = 1f;

    static final float EXPLOSIONS = decibels(-2);
    /** Enemy hits sit 8 dB below player damage. */
    static final float HITS = decibels(-8);

    static final float ENEMY_FIRE = decibels(-11);
    static final float PICKUPS = decibels(-8);
    static final float PLAYER_FIRE = decibels(-14);

    static final float MAX_PAN = 0.4f;

    private final SfxBank bank;
    /** What the families share: the bank, the pitch variation, the variants, the waiting sounds. */
    private final SoundMix mix;
    /** The player's side: shots, ship hits, ground targets, debris, pickups and the ship's warnings. */
    private final PlayerCues player;
    /** The bosses and set pieces, the Kraken's and the Siege Spire's among them. */
    private final BossCues bosses;
    /** The enemy units' one-shots and loops. */
    private final UnitCues units;
    /** M7 part E: Level 18's units: the Dust Devil's spin, opening and spiral, the Ghost Drone's drop, glint and burst. */
    private final StormCues storm;
    /** M7 part F: the Revenant Walker's (Level 19). */
    private final WalkerCues walker;
    /** M7 part F: Level 19's Warden Tanks, furnace vents and belts. */
    private final FoundryCues foundry;
    /** M7 part G: Level 20's pursuit swarm: its band hum and lunge. */
    private final PursuitCues pursuit;
    /** M7 part H: Level 21's Dust Colossus: its bulge, eruption, suction, bite and ridge pass. */
    private final ColossusCues colossus;
    /** The convoy, the relay, the domes, Rook, the scripted loss's lance and the tows. */
    private final AllyCues allies;
    /** The specials and the Act 3 arsenal, its held loops. */
    private final ArsenalCues arsenal;
    /** The levels' own sounds: the sled, the flares, the collapse, the storm, the wall scrape. */
    private final LevelCues levels;
    /** Per event type the family that plays it (by ordinal); null for an event without a sound. */
    private final SoundFamily[] families = new SoundFamily[SimEvents.Type.values().length];
    /** Every family, in the order they hear a step's start and end. */
    private final SoundFamily[] all;
    /** Whether the first {@link #watch} has noted the level's state. */
    private boolean watchedOnce;

    /**
     * A chained death's bursts (design/enemies/air/coilwyrm) follow the chain's taper: the pitch of
     * a member's burst rises as its hit box narrows, 0.94 at the first segment's 37.8 px (54 px x
     * 0.7) up to at most 1.2 at the last one's 18.9 px (the tail, 28 px, in between), with ±2 %
     * random on top; one burst every 0.25 s, so they do not overlap and play at the explosion level.
     */
    static final double RIPPLE_REFERENCE_WIDTH = 37.8;

    private static final double RIPPLE_PITCH_START = 0.94;
    private static final double RIPPLE_PITCH_EXPONENT = 0.35;
    private static final double RIPPLE_PITCH_MAX = 1.2;

    /**
     * The low-armour warning (design/player/armor): a beep every {@value #SLOW_BEEP_SECONDS} s at or
     * below 30 % armour, every {@value #FAST_BEEP_SECONDS} s at or below 15 %, while the ship flies:
     * the {@link LowArmour} stages, the same as the HUD's flashing readout. It repeats for as long as
     * the armour stays low (armour does not regenerate), so it plays 6 dB under the warnings' level.
     */
    static final double SLOW_BEEP_SECONDS = 1.2;

    static final double FAST_BEEP_SECONDS = 0.6;

    /**
     * M5 part D: where the impact sits in {@link Sfx#LANCE_STRIKE}, seconds from its start: round 32's
     * lance a lands it 1.2 s in (tools/concept/audio/sfx_r32.py), so the sound starts this long
     * before the scripted loss's hit (Level 10: at 116.8 for the hit at 118), inside the glow.
     */
    static final double LANCE_IMPACT_SECONDS = 1.2;

    /** M7 part A: the loops a flight holds, stopped as the level screen closes. */
    private static final Sfx[] FLIGHT_LOOPS = {
        Sfx.LAMPREY_DRAIN,
        Sfx.ION_BEAM_LOOP,
        Sfx.BEAM_IMPACT,
        Sfx.BEAM_IMPACT_ROCK,
        Sfx.LANCE_LOOP,
        Sfx.LANCE_RUMBLE,
        Sfx.TENDRIL_HUM,
        Sfx.TENDRIL_BURN,
        Sfx.CRAWLER_WALK,
        Sfx.HERALD_AURA,
        Sfx.SHELLBACK_WALK,
        Sfx.SHELLBACK_ROLL,
        Sfx.DEVIL_SPIN,
        Sfx.WALKER_FLAME,
        Sfx.TANK_TRACKS,
        Sfx.BELT_RUMBLE,
        Sfx.SWARM_HUM,
        Sfx.COLOSSUS_SUCTION
    };
    /**
     * M7 part C: a unit's loop (the tendril hum and burn, the Threadcrawler's walk; design/audio/sfx, Mixing) is
     * levelled in its file (−34 to −38 dB, as the beam's sizzle) and plays as it is; it fades in and out over
     * {@value #UNIT_FADE_STEPS} steps (0.2 s).
     */
    static final float UNIT_LOOP_LEVEL = 1f;

    static final int UNIT_FADE_STEPS = 12;
    /** M7 part C: the hum's share while its tendril is dormant, rising to the full hum with the flare. */
    static final float HUM_DORMANT = 0.4f;
    /** M7 part C: a cut Threadcrawler's tear: the Coilwyrm's wet rip, lower and a little quieter (dry chitin). */
    static final float CRAWLER_CUT_PITCH = 0.8f;

    static final float CRAWLER_CUT_LEVEL = decibels(-3) * EXPLOSIONS;
    /**
     * M7 part A: the beam's start, loop and stop sit at the shots' level (the loop is levelled like them,
     * design/audio/sfx); the sizzle is levelled in its file (−38 dB) and plays under it as it is; the
     * Orbital Lance's loop and rumble with the specials, a little under the player's damage.
     */
    static final float BEAM_LEVEL = PLAYER_FIRE;

    static final float SIZZLE_LEVEL = 1f;
    static final float LANCE_LEVEL = decibels(-3);

    /**
     * @param looks the explosions of the level's enemy kinds
     * @param armament the fitted weapons, whose sound families the shots play
     * @param setPieces the slugs of the level's set pieces, whose death cries they play
     */
    public FlightSounds(SfxBank bank, EnemyLooks[] looks, Armament armament, List<String> setPieces) {
        this(bank, looks, armament, setPieces, java.util.Optional.empty());
    }

    /**
     * With a wingman's gun too (M5 part A): its shots carry the mount index past the armament's and
     * play its family.
     */
    public FlightSounds(
            SfxBank bank,
            EnemyLooks[] looks,
            Armament armament,
            List<String> setPieces,
            java.util.Optional<vanguard.sim.WeaponSpec> wingman) {
        this.bank = bank;
        mix = new SoundMix(bank);
        bosses = new BossCues(mix, setPieces);
        player = new PlayerCues(mix, armament, wingman, bosses);
        storm = new StormCues(mix);
        walker = new WalkerCues(mix);
        foundry = new FoundryCues(mix);
        pursuit = new PursuitCues(mix);
        colossus = new ColossusCues(mix);
        units = new UnitCues(mix, looks, storm, foundry, walker);
        allies = new AllyCues(mix);
        arsenal = new ArsenalCues(mix);
        levels = new LevelCues(mix);
        all = new SoundFamily[] {
            player, bosses, units, storm, walker, foundry, pursuit, colossus, allies, arsenal, levels
        };
        for (SoundFamily family : all) {
            for (SimEvents.Type type : family.types()) {
                if (families[type.ordinal()] != null) {
                    throw new IllegalStateException(type + " has two families");
                }
                families[type.ordinal()] = family;
            }
        }
    }

    /**
     * M7 part H: Level 21's ridge pass (the level's {@code ridge} with its breaching boss) starts at level time {@code
     * seconds}: its rumble then ({@link ColossusCues}).
     */
    public void ridgePass(double seconds) {
        colossus.ridgePass(seconds);
    }

    /** How long the level's flares burn (its darkness at the difficulty): the burn loop's length. */
    public void flareSeconds(double seconds) {
        levels.flareSeconds(seconds);
    }

    /**
     * The launch rail at the start of an attempt's launch: its stop, the release, lands 3.55 s
     * into the 5 s launch. The file is levelled like the interface sounds, so it plays as it is.
     */
    public void launch() {
        bank.play(Sfx.LAUNCH_RAIL, 1, 1, 0);
    }

    /**
     * The warning tone for edge warnings that started (design/audio/sfx: warnings at the player-damage
     * level), panned towards the warned edge; one tone at a time, so overlapping warnings do not stack.
     *
     * @param edges the edges whose warning started, as {@link WarningEdge} bits
     */
    public void edgeWarnings(int edges) {
        if (edges == 0) {
            return;
        }
        float pan = WarningEdge.LEFT.in(edges) == WarningEdge.RIGHT.in(edges)
                ? 0
                : WarningEdge.LEFT.in(edges) ? -MAX_PAN : MAX_PAN;
        bank.play(Sfx.EDGE_WARNING, PLAYER_DAMAGE, 1, pan);
    }

    /**
     * A set piece's break-up after its death cry (design/enemies/space/leviathan): the {@code
     * large} rung layered under the blasts, one as the cluster builds, two together at the swap
     * where the body comes apart, and one under the trailing blasts.
     *
     * @param swap the step after the death at which the body is replaced by its chunks
     */
    public void breakUp(double x, int swap) {
        float pan = SoundMix.pan(x);
        mix.later(Sfx.EXPLOSION_MEDIUM_A, EXPLOSIONS, 0.8f, pan, swap - 24);
        mix.later(Sfx.EXPLOSION_LARGE_A, PLAYER_DAMAGE, 0.85f, pan, swap);
        mix.later(Sfx.EXPLOSION_LARGE_B, EXPLOSIONS, 0.8f, -pan, swap + 3);
        mix.later(Sfx.EXPLOSION_MEDIUM_B, EXPLOSIONS, 0.75f, pan, swap + 40);
    }

    /**
     * Set piece {@code k}'s death blast on the explosion ladder: the {@code huge} rung for an act
     * boss and for a set piece that is no boss (a huge regular enemy, design/enemies: Size tiers),
     * the {@code large} one for a mid-boss. An act boss's chained death plays it at the chain's end.
     */
    public Sfx deathBlast(int k) {
        return bosses.deathBlast(k);
    }

    /** Set piece {@code k}'s sac burst for its chained death's bursts at its sacs, or null for the generic explosions. */
    public Sfx sacBurst(int k) {
        return bosses.sacBurst(k);
    }

    /** Advances the waiting sounds by one simulation step and plays the due ones. */
    public void step() {
        mix.step();
    }

    /**
     * M5 part C: whether the arcology's collapse sound starts with its warning (round 31 a: a rumble
     * that swells under the lean and the drop into the crash) rather than with the drop (a sound that
     * crashes from its first moment, as the placeholder did).
     */
    static final boolean COLLAPSE_AT_WARNING = true;

    /**
     * Where the crash sits in {@link Sfx#ARCOLOGY_COLLAPSE}, in seconds from its start: Level 09's
     * collapse impact (the 1.5 s lean and the 1.5 s drop, round 31's look c), so a sound started with
     * the warning crashes as the tower hits the ground (the file cut by tools/concept/audio/
     * sfx_r31.py's PRODUCTION at its IMPACT, built by tools/art/sfx_originals.py). The test {@code
     * SfxFilesTest} ties it to the level's impact.
     */
    static final double COLLAPSE_CRASH_SECONDS = 3.0;

    /**
     * Where the impact sits in {@link Sfx#KRAKEN_SLAM}, seconds from its start (round 33 a,
     * tools/concept/audio/sfx_r33.py): the sound starts this long before the slam.
     */
    static final double SLAM_IMPACT_SECONDS = 0.5;

    /**
     * M5 part H: the canopy's withering starts this long after the Siege Spire's death: at the end of its
     * 3 s death chain (step H7b; at 0.5 s, with the tear's first frame, the chain's bursts masked it).
     */
    static final double WITHERING_DELAY_SECONDS = 3.0;

    /** M5 part G: the level steps the supply drones lift off at, for the tests. */
    long[] launchTicks() {
        return allies.launchTicks();
    }

    /**
     * M5 part E: the Harbour Kraken's slam cycle (design/enemies/bosses/harbour-kraken): its slam
     * sound starts {@code telegraph + rise - }{@link #SLAM_IMPACT_SECONDS} after the lane's telegraph,
     * so its impact lands on the slam, and the second lane of a volley {@code second} s later.
     */
    public void slamCycle(double telegraph, double rise, double second) {
        bosses.slamCycle(telegraph, rise, second);
    }

    /**
     * M5 part H: the level is under the Vrell canopy (Level 14's {@code earth-canopy} ambience): the
     * boss's death plays the canopy's withering.
     */
    public void canopy(boolean canopy) {
        bosses.canopy(canopy);
    }

    /** M5 part E: the level's convoy sails on the water: its hits and losses play the ships' sounds. */
    public void naval(boolean naval) {
        allies.naval(naval);
    }

    /**
     * M7 part D: the level's convoy is ground allies (Level 17's Ares Landing domes): a hit plays the dome's glass
     * under stress, a break its cave-in, a hooked bomber's drop over a dome the falling bomb.
     */
    public void ground(boolean ground) {
        allies.ground(ground);
    }

    /**
     * M7 part D: a militia flak burst at {@code x} (Level 17's spaceport guns, the level's {@code flak}; presentation,
     * no event): the escort frigate's distant flak, as quiet.
     */
    public void militiaFlak(double x) {
        allies.militiaFlak(x);
    }

    /**
     * M5 part G: the level's convoy is a defend primary's ally (Level 13's Nansen Relay): its hits, its
     * loss, its integrity alarm and grid online play the relay's sounds; its supply drones lift off
     * {@code arcSeconds} before the drops at {@code dropTimes} (level seconds) with the launch's sound.
     */
    public void defend(boolean defend, double[] dropTimes, double arcSeconds) {
        allies.defend(defend, dropTimes, arcSeconds);
    }

    /**
     * M5 part E: a kill, a torpedo's impact or a landing blast on the water at {@code x}: the surface
     * burst, or the under-water one ({@code under}), played over the size rung.
     */
    public void waterExplosion(boolean under, double x) {
        bank.play(under ? Sfx.EXPLOSION_UNDERWATER : Sfx.EXPLOSION_WATER, EXPLOSIONS, mix.pitch(0.04), SoundMix.pan(x));
    }

    /**
     * M7 part D: a ring's absorb pings at most once every {@value #ABSORB_GAP_STEPS} steps (0.15 s) for the whole
     * field: a beam or a stream on a ringed group would otherwise ring on.
     */
    static final int ABSORB_GAP_STEPS = 9;

    /** M7 part D: whether an absorb's ping is due at step {@code now} after the last one at {@code last}. */
    static boolean absorbDue(long last, long now) {
        return now - last >= ABSORB_GAP_STEPS || now < last;
    }

    /**
     * Round 40: a root's wet hit plays at most once every {@value #ROOT_HIT_GAP_STEPS} steps (the hit
     * flash's cooldown, so it lands with the blink): a stream on a root would otherwise splat on.
     */
    static final int ROOT_HIT_GAP_STEPS = 15;
    /** Round 40: the root's wet hit, levelled under the shot's hit; while raised louder and lower. */
    static final float ROOT_HIT_LEVEL = 0.5f * HITS;

    static final float ROOT_HIT_RAISED_LEVEL = HITS;
    static final float ROOT_HIT_PITCH = 1.05f;
    static final float ROOT_HIT_RAISED_PITCH = 0.85f;
    /** Round 40: the Spire pod's aim tell: the Mantis's 0.6 s charge-up at this pitch (shorter) and level. */
    static final float AIM_TELL_PITCH = 1.3f;

    static final float AIM_TELL_LEVEL = 0.6f;

    /** Round 40: whether a root's wet hit is due at step {@code now} after its last one at {@code last}. */
    static boolean rootHitDue(long last, long now) {
        return now - last >= ROOT_HIT_GAP_STEPS || now < last;
    }

    /** M7 part D: the one-shot of a Level 17 unit event; null for another event. */
    static Sfx siegeSound(SimEvents.Type type) {
        return switch (type) {
            case RINGS_COLLAPSED, RINGS_STRIPPED -> Sfx.AURA_COLLAPSE;
            case CURLED -> Sfx.SHELLBACK_CURL;
            case UNCURLED -> Sfx.SHELLBACK_UNCURL;
            case RING_ABSORBED -> Sfx.AURA_ABSORB;
            default -> null;
        };
    }

    /**
     * M7 part D: Level 17's unit one-shots (round 40's proposals) are levelled in their files (−17 to −22 dB, as the
     * dome's) and play as they are; its loops at {@link #UNIT_LOOP_LEVEL} with the 0.2 s fades (round 39 B.4d).
     */
    static final float SIEGE_LEVEL = 1f;

    /** Plays the sounds of one step's events. */
    public void play(SimEvents events) {
        long now = mix.watched == null ? 0 : mix.watched.tick();
        for (SoundFamily family : all) {
            family.begin(now);
        }
        for (int i = 0; i < events.size(); i++) {
            SoundFamily family = families[events.type(i).ordinal()];
            if (family != null) {
                family.play(events, i, SoundMix.pan(events.x(i)));
            }
        }
        for (SoundFamily family : all) {
            family.end();
        }
    }

    /**
     * M5 part F: Level 12's levels over the files' own (round 34; the gust's b and the thunder's b sit
     * within 1 dB of the a they replaced, the third thunder 2 dB over it, step K1): the chime and the drain loop under the
     * warnings; the latch, the shake-off and the gust with them at the loudest group's level (the round
     * 34 capture could not hear them at -2 and -4 dB).
     */
    static final float SONDE_LEVEL = decibels(-4);

    static final float LAMPREY_LATCH_LEVEL = PLAYER_DAMAGE;
    static final float LAMPREY_SHAKE_LEVEL = PLAYER_DAMAGE;
    static final float GUST_LEVEL = PLAYER_DAMAGE;
    static final float DRAIN_LEVEL = decibels(-6);
    /** The warnings' level (the edge warning's and the shield hit's), for the tests. */
    static final float WARNING_LEVEL = PLAYER_DAMAGE;
    /**
     * M5 part F: a latch and a shake-off duck the level's theme by this much for this long (the level
     * screen), so their one-shots cut through the storm's mix as the radio's lines do.
     */
    public static final double CUE_DUCK_DB = -6;

    public static final double CUE_DUCK_SECONDS = 1.0;
    /** M5 part F: the thunder follows its flash after 0.3 to 1.5 s (design/campaign Level 12, Weather). */
    static final double THUNDER_MIN_SECONDS = 0.3;

    static final double THUNDER_MAX_SECONDS = 1.5;
    /** M5 part F: the gust's wind comes from the upwind side, this far to it. */
    static final float GUST_PAN = 0.3f;

    /**
     * M5 part F: the thunder after flash {@code flash} (its index in the level's lightning): the three
     * variants in turn, so no two flashes in a row roll alike.
     */
    static Sfx thunder(int flash) {
        return switch (Math.floorMod(flash, 3)) {
            case 0 -> Sfx.THUNDER_1;
            case 1 -> Sfx.THUNDER_2;
            default -> Sfx.THUNDER_3;
        };
    }

    /**
     * M5 part F: the steps from flash {@code flash} to its thunder, 0.3 to 1.5 s: spread by the golden
     * ratio over the flash index, so the delays differ from flash to flash but replay alike.
     */
    static int thunderSteps(int flash) {
        double share = (flash * 0.6180339887498949) % 1;
        return SimStep.ticks(THUNDER_MIN_SECONDS + (THUNDER_MAX_SECONDS - THUNDER_MIN_SECONDS) * share);
    }

    /** M5 part F: the thunder's side, the bolt's ({@code LevelRenderer}: even flashes strike on the left). */
    static float thunderPan(int flash) {
        return flash % 2 == 0 ? -MAX_PAN / 2 : MAX_PAN / 2;
    }

    /**
     * M5 part G: Level 13's levels over the files' own (round 35, levelled by sfx_r35.py against the
     * set; grid online's b within 1 dB of its a): the relay's hit with the explosions (it plays often), its alarm and grid online with the
     * warnings, its destruction at the loudest group's level, the drone's launch with the warnings too
     * (the user found quiet layers too quiet; the G7 capture measured it about 3–5 dB under the rest of the
     * mix with the explosions' level, so it went up 2 dB).
     */
    static final float RELAY_HIT_LEVEL = EXPLOSIONS;

    static final float ALARM_LEVEL = PLAYER_DAMAGE;
    static final float GRID_ONLINE_LEVEL = PLAYER_DAMAGE;
    static final float RELAY_DESTROYED_LEVEL = PLAYER_DAMAGE;
    static final float DRONE_LEVEL = PLAYER_DAMAGE;
    /**
     * M7 part D: Level 17's levels (round 40's proposals, levelled in their files): a dome's hit with the explosions
     * (as the relay's), its break likewise, under the radio's ally-loss line; a bomber's drop with the enemy shots.
     */
    static final float DOME_HIT_LEVEL = EXPLOSIONS;

    static final float DOME_BREAK_LEVEL = EXPLOSIONS;
    static final float BOMB_DROP_LEVEL = ENEMY_FIRE;
    /** M7 part C: the canyon scrape, a little under the shield or armour hit it plays with (design/audio/sfx). */
    static final float SCRAPE_LEVEL = decibels(-2) * PLAYER_DAMAGE;

    /** M5 part D: the sound of a Level 10 event: the Wraith's decloak, the swarm's loop-back; null for another. */
    static Sfx eventSound(SimEvents.Type type) {
        return switch (type) {
            case DECLOAK -> Sfx.WRAITH_DECLOAK;
            case LOOP_BACK -> Sfx.MOTE_SWARM;
            default -> null;
        };
    }

    /**
     * M5 part D: whether the lance's sound starts between the level clock {@code before} and {@code
     * now}: its start {@link #LANCE_IMPACT_SECONDS} before the hit at {@code hitSeconds} lies after
     * the one and at or before the other.
     */
    static boolean lanceDue(double before, double now, double hitSeconds) {
        double start = hitSeconds - LANCE_IMPACT_SECONDS;
        return before < start && start <= now;
    }

    /**
     * Once a simulation step, after {@link #play}: the sounds that follow a state rather than an
     * event. A boss with its own sounds (round 25, the Brood Carrier): its sacs opening (one sound
     * for the sacs that open together) and shutting with their windows, a sac shot off bursting (not
     * at its death, whose chain plays them), its iris opening as the core is exposed, its roar as its
     * turn starts. A tow's cable snapping as its pod falls free. And the Vrell screech as a large
     * unit enters the screen ({@link ScreechCue}).
     */
    public void watch(Sortie sortie) {
        if (!player.groundNoted()) {
            player.noteGround(sortie);
            bosses.noteDeaths(sortie);
        }
        player.watchShip(sortie);
        // M5 part D: a swarm's entry; the scripted loss's lance, timed so its impact lands on the hit.
        units.watchEntries(sortie);
        allies.watchLance(sortie);
        arsenal.watch(sortie);
        units.watchCanyon(sortie);
        units.watchSiege(sortie);
        storm.watch(sortie);
        walker.watch(sortie);
        foundry.watch(sortie);
        pursuit.watch(sortie);
        colossus.watch(sortie);
        levels.watchWeather(sortie);
        allies.watchDrones(sortie);
        mix.watched = sortie;
        if (!watchedOnce) {
            watchedOnce = true;
            bosses.noteParts(sortie);
            allies.noteTows(sortie);
            mix.resync = true;
        }
        bosses.watch(sortie);
        allies.watchTows(sortie);
        mix.resync = false;
    }

    /** M5 part G: whether a drone lifts off after level step {@code watched} up to {@code now} (none before the first watch). */
    static boolean launched(long watched, long now, long[] launchTicks) {
        if (watched < 0) {
            return false;
        }
        for (long launch : launchTicks) {
            if (watched < launch && launch <= now) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part F: stops the loops the flight started (the drain's; M7 part A: the beam's, its sizzle and
     * the Orbital Lance's), as the level screen closes.
     */
    public void stopLoops() {
        for (Sfx loop : FLIGHT_LOOPS) {
            if (bank.looping(loop)) {
                bank.stop(loop);
            }
            units.silence(loop);
            storm.silence(loop);
            walker.silence(loop);
            foundry.silence(loop);
            pursuit.silence(loop);
            colossus.silence(loop);
        }
    }

    /** M7 part C: the kinds that are a crawling chain's parts: a head with a {@code crawl}, its segment and tail. */
    static boolean[] crawlerParts(List<EnemySpec> kinds) {
        boolean[] parts = new boolean[kinds.size()];
        for (EnemySpec head : kinds) {
            if (head.crawl().isEmpty()) {
                continue;
            }
            for (int k = 0; k < kinds.size(); k++) {
                EnemySpec kind = kinds.get(k);
                boolean member = head.chain()
                        .map(chain -> chain.segment() == kind || chain.tail() == kind)
                        .orElse(false);
                parts[k] |= kind == head || member;
            }
        }
        return parts;
    }

    /** Whether an enemy shot of that damage ({@code ENEMY_FIRED}'s value) fires a medium bullet: a heavy shot. */
    static boolean heavyShot(int damage) {
        return damage >= BulletLooks.MEDIUM_DAMAGE;
    }

    /** The steps between low-armour beeps at {@code armour} of {@code maxArmour}; 0 for none (a wreck neither). */
    static int beepPeriod(double armour, double maxArmour) {
        if (armour <= 0) {
            return 0;
        }
        return switch (LowArmour.of(armour, maxArmour)) {
            case NONE -> 0;
            case LOW -> SimStep.ticks(SLOW_BEEP_SECONDS);
            case CRITICAL -> SimStep.ticks(FAST_BEEP_SECONDS);
        };
    }

    /** A chained burst's pitch by the member's hit box width (see {@link #RIPPLE_REFERENCE_WIDTH}). */
    static float ripplePitch(double width) {
        double ratio = RIPPLE_REFERENCE_WIDTH / Math.max(1, width);
        return (float) Math.min(RIPPLE_PITCH_MAX, RIPPLE_PITCH_START * Math.pow(ratio, RIPPLE_PITCH_EXPONENT));
    }

    /** A pickup's sound (design/audio/sfx, Pickups); the overdrive's start cue plays with its pickup. */
    static Sfx pickupSound(PickupType type) {
        return switch (type) {
            case SMALL_SALVAGE -> Sfx.SALVAGE_SMALL;
            case MEDIUM_SALVAGE -> Sfx.SALVAGE_MEDIUM;
            case HIDDEN_CRATE, LARGE_SALVAGE -> Sfx.SALVAGE_LARGE;
            case SPECIAL_CHARGE -> Sfx.SPECIAL_CHARGE;
            case OVERDRIVE -> Sfx.POWER_UP;
            case DATA_CORE -> Sfx.DATA_CORE;
            case SHIELD_CELL -> Sfx.SHIELD_CELL;
            case ARMOUR_PATCH -> Sfx.ARMOUR_PATCH;
        };
    }

    /**
     * The Ravager's pounce, a or b at random each time (round 31: both kept): drawn from the sound's
     * own generator, never the simulation's, which stays deterministic.
     */
    static Sfx pounce(SplitMix64 random) {
        return random.nextInt(2) == 0 ? Sfx.RAVAGER_POUNCE_A : Sfx.RAVAGER_POUNCE_B;
    }

    /** M7 part C: the scrape's pan: hard to the touching side for the ship, by his x for Rook (value 2). */
    static float scrapePan(int side, float pan) {
        return side == -1 ? -MAX_PAN : side == 1 ? MAX_PAN : pan;
    }

    static float decibels(double db) {
        return (float) Math.pow(10, db / 20);
    }
}
