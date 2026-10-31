package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.CRAWLER_CUT_LEVEL;
import static vanguard.game.audio.FlightSounds.CRAWLER_CUT_PITCH;
import static vanguard.game.audio.FlightSounds.ENEMY_FIRE;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.HUM_DORMANT;
import static vanguard.game.audio.FlightSounds.LAMPREY_LATCH_LEVEL;
import static vanguard.game.audio.FlightSounds.LAMPREY_SHAKE_LEVEL;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.SIEGE_LEVEL;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.game.render.EnemyLooks;
import vanguard.sim.Chain;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Links;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tendril;

/**
 * The enemy units' sounds for {@link FlightSounds}: their hits, kills and volleys (a Driftjelly's ring,
 * a Herald's song), the spore mines, the chains, the Mantis, the Hive Node, the pouncers, the Wraith and
 * the Mote Swarm, the mortars, the Burrowers, the Lampreys, Level 17's units; the Vrell screech and a
 * swarm's rush as they enter the screen; and the unit loops (the tendrils' hum and burn, the
 * Threadcrawler's walk, the Herald's aura, the Shellback's walk and roll), faded in and out.
 */
final class UnitCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.ENEMY_HIT,
            SimEvents.Type.ENEMY_DESTROYED,
            SimEvents.Type.NEUTRAL_DOWN,
            SimEvents.Type.PREY_KILLED,
            SimEvents.Type.ENEMY_FIRED,
            SimEvents.Type.MINE_DROPPED,
            SimEvents.Type.MINE_BURST,
            SimEvents.Type.MINE_DESTROYED,
            SimEvents.Type.LAMPREY_LATCHED,
            SimEvents.Type.LAMPREY_SHAKEN,
            SimEvents.Type.BURROW_BULGE,
            SimEvents.Type.BURROW_DIVE,
            SimEvents.Type.MORTAR_LOBBED,
            SimEvents.Type.RING_ABSORBED,
            SimEvents.Type.RINGS_COLLAPSED,
            SimEvents.Type.RINGS_STRIPPED,
            SimEvents.Type.CURLED,
            SimEvents.Type.UNCURLED,
            SimEvents.Type.UNIT_PART_DESTROYED,
            SimEvents.Type.MORTAR_IMPACT,
            SimEvents.Type.BROOD_HATCHED,
            SimEvents.Type.CHAIN_CUT,
            SimEvents.Type.TENDRIL_BROKEN,
            SimEvents.Type.CHAIN_REGROWN,
            SimEvents.Type.CHAIN_POP,
            SimEvents.Type.SWEEP_TELEGRAPH,
            SimEvents.Type.SPAWN_TELEGRAPH,
            SimEvents.Type.SPAWN_RELEASED,
            SimEvents.Type.POUNCE,
            SimEvents.Type.SWEEP_FIRED,
            SimEvents.Type.DECLOAK,
            SimEvents.Type.LOOP_BACK);

    /**
     * M7 part D: an event's unit lies within this distance of it, px: a fire or lob event carries the unit's
     * position at the moment it fired, and a walker moves on with the scroll in the same step.
     */
    private static final double FIRED_REACH = 6;

    /** M7 part D: the units of Level 17 with sounds of their own, told apart by their stat blocks. */
    enum Unit {
        /** A unit that sings an {@code aura} (the Choir Herald). */
        HERALD,
        /** A walker with a curl-roll (the Shellback). */
        SHELLBACK;

        /** Whether {@code spec} is this unit. */
        boolean is(EnemySpec spec) {
            return switch (this) {
                case HERALD -> spec.aura().isPresent();
                case SHELLBACK -> spec.roll().isPresent();
            };
        }
    }

    private final EnemyLooks[] looks;
    /** M7 part E: Level 18's units, whose volleys are a grit spiral's hiss or a drone's burst. */
    private final StormCues storm;
    /** M7 part F: Level 19's turret units, whose burst plays once ({@link FoundryCues#fired}). */
    private final FoundryCues foundry;
    /** The Vrell screech as a large unit enters the screen, at most one every 3 s. */
    private final ScreechCue screech = new ScreechCue(SimStep.ticks(ScreechCue.THROTTLE_SECONDS));
    /** M5 part D: a Mote Swarm's rush of wings as it enters the screen. */
    private final SwarmCue swarms = new SwarmCue();
    /** M7 part C: the unit loops' levels now (their fades), by {@link Sfx} ordinal. */
    private final float[] unitLevels = new float[Sfx.values().length];
    /** M7 part C: the level's enemy kinds that are a Threadcrawler's parts (its cut's sound), from the first watch. */
    private boolean[] crawlerParts;
    /** M7 part D: the step of the last absorb's ping. */
    private long lastAbsorb = Long.MIN_VALUE / 2;
    /** This step's step (the absorb's spacing). */
    private long now;
    /** Round 43: the walker's sounds, asked whether a hit lands on its open vents; null in tests without it. */
    private final WalkerCues walker;
    /** M8 part A: the harpoons' hit (and the Act 4 systems' other sounds). */
    private final DeepWaterCues deepWater;
    /** M8 part C: the kills' under-water rung and Level 23's units' own sounds. */
    private final UnderIceCues underIce;
    /** M8 part D: Level 24's units' own sounds (the Ray, the Barbfin, the reef). */
    private final AbyssCues abyss;
    /** M8 part E: Level 25's units' own deaths (the Siren, the Spiral Nautilus). */
    private final TrenchCues trench;
    /** M8 part G: Level 27's Depth Hunters and relay beacons. */
    private final VentFieldCues ventField;
    /** M8 part H: a lob's burst in Level 28's run is a cruiser's flak airburst. */
    private final MawCues maw;
    /** M9 part B: Level 29's units' shots and the clamps springing open. */
    private final CeresCues ceres;
    /** M9 part C: Level 30's line mines, Gunships' rings and turret cars' shots. */
    private final MinefieldCues minefield;
    /** M9 part E: Level 32's Sentinel sweeps and deaths, the Striders' cannons. */
    private final DeepCoreCues deepCore;

    private final LeechFieldCues leechField;

    UnitCues(
            SoundMix mix,
            EnemyLooks[] looks,
            StormCues storm,
            FoundryCues foundry,
            WalkerCues walker,
            DeepWaterCues deepWater,
            UnderIceCues underIce,
            AbyssCues abyss,
            TrenchCues trench,
            VentFieldCues ventField,
            MawCues maw,
            CeresCues ceres,
            MinefieldCues minefield,
            DeepCoreCues deepCore,
            LeechFieldCues leechField) {
        super(mix);
        this.deepCore = deepCore;
        this.leechField = leechField;
        this.ceres = ceres;
        this.minefield = minefield;
        this.trench = trench;
        this.ventField = ventField;
        this.maw = maw;
        this.deepWater = deepWater;
        this.underIce = underIce;
        this.abyss = abyss;
        this.looks = looks;
        this.storm = storm;
        this.foundry = foundry;
        this.walker = walker;
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case ENEMY_HIT -> {
                // Round 43: a hit on the walker's open vents is its own metal hit (WalkerCues); M8 part A: a harpoon's
                // hit is its heavy thud (DeepWaterCues).
                if (!deepWater.harpoonHit(events.value(i), pan)
                        && (walker == null || !walker.ventHit(events.x(i), events.y(i), pan))) {
                    bank.play(alternate(Sfx.HIT_ORGANIC_A, Sfx.HIT_ORGANIC_B), HITS, pitch(0.05), pan);
                }
            }
            case ENEMY_DESTROYED -> {
                EnemyLooks kind = looks[events.value(i)];
                bank.play(explosion(kind), EXPLOSIONS, pitch(0.04), pan);
                // M8 part C: a Glassfin's shatter, an eel part's tear, a pump's break over it.
                underIce.destroyed(events.value(i), pan);
                // M8 part D: a Ray's dying groan, a reef's collapse over it.
                abyss.destroyed(events.value(i), pan);
                // M8 part E: a Siren's song choked off, a Nautilus's shell cracking over it.
                trench.destroyed(events.value(i), pan);
                // M8 part B: a Drill Rig's core also plays the rig's collapse (FoundryCues).
                foundry.destroyed(events.value(i), pan);
                // M8 part G: a Depth Hunter's hull break, a relay beacon's break over it.
                ventField.destroyed(events.value(i), pan);
                // M9 part B: a clamp gun's clamp springing open over its burst.
                ceres.destroyed(events.value(i), pan);
                // M9 part E: a Sentinel's crash and its head's fall over its rung.
                deepCore.destroyed(events.value(i), pan);
                // M9 part F: a Void Leech's crack, a Rail Bunker's blast over its rung.
                leechField.destroyed(events.value(i), pan);
            }
            // Step FIX6 (the user's playthrough 2026-10-30): a destroyed neutral craft (Level 29's Stormhawks) bursts
            // with its kind's explosion, as a kill does.
            // M10 part A: a unit lost to the three-way war plays its rung as any death (design/audio/sfx, Act 6).
            case NEUTRAL_DOWN, PREY_KILLED ->
                bank.play(explosion(looks[events.value(i)]), EXPLOSIONS, pitch(0.04), pan);
            // A heavy shot for a medium bullet: the event's value is the bullet's damage, the same
            // class boundary as its large orb.
            case ENEMY_FIRED -> {
                if (ringAt(events.x(i), events.y(i))) {
                    // M5 part E: a Driftjelly's proximity ring (round 33 a), not a gun's report.
                    bank.play(Sfx.DRIFTJELLY_PULSE, 1, pitch(0.04), pan);
                } else if (firedBy(events.x(i), events.y(i), Unit.HERALD)) {
                    // M7 part D: the Choir Herald's song with its ring (round 40 a), not a gun's report.
                    bank.play(Sfx.HERALD_SONG, SIEGE_LEVEL, 1, pan);
                } else if (storm.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M7 part E: a Dust Devil's grit spiral or a Ghost Drone's burst (StormCues).
                } else if (foundry.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M7 part F: a Warden Tank's burst, once at its first shot (FoundryCues).
                } else if (underIce.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M8 part C: an Eel Swarm head's pulse shot (UnderIceCues).
                } else if (abyss.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M8 part D: an Abyss Ray's fan, a Barbfin's needle (AbyssCues).
                } else if (ventField.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M8 part G: a Depth Hunter's torpedo launch (VentFieldCues).
                } else if (ceres.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M9 part B: a Talon's twin crack, a clamp gun's shot (CeresCues).
                } else if (minefield.fired(firer(events.x(i), events.y(i)), pan)) {
                    // M9 part C: a Gunship's ring, a turret car's shot (MinefieldCues).
                } else if (deepCore.fired(events.x(i), events.y(i), pan)) {
                    // M9 part E: a Strider's twin cannons once a fan, its pod's salvo silent (DeepCoreCues).
                } else {
                    bank.play(
                            FlightSounds.heavyShot(events.value(i))
                                    ? alternate(Sfx.ENEMY_HEAVY_SHOT_A, Sfx.ENEMY_HEAVY_SHOT_B)
                                    : alternate(Sfx.ENEMY_SHOT_A, Sfx.ENEMY_SHOT_B),
                            ENEMY_FIRE,
                            pitch(0.05),
                            pan);
                }
            }
            case MINE_DROPPED -> {
                // M9 part C: a line mine's drop and arming (MinefieldCues).
                if (!minefield.mine(events.type(i), events.value(i), pan)) {
                    bank.play(alternate(Sfx.HIT_ORGANIC_A, Sfx.HIT_ORGANIC_B), ENEMY_FIRE, 0.6f * pitch(0.05), pan);
                }
            }
            case MINE_BURST, MINE_DESTROYED -> {
                // M9 part C: a line mine's metallic bang (MinefieldCues).
                if (!minefield.mine(events.type(i), events.value(i), pan)) {
                    bank.play(alternate(Sfx.EXPLOSION_TINY_A, Sfx.EXPLOSION_TINY_B), EXPLOSIONS, pitch(0.06), pan);
                }
            }
            // M5 part F, Level 12 (round 34, the user's picks): the latch and the shake-off; the drain loop
            // follows the state (LevelCues).
            // M9 part F: a Void Leech's own latch and throw (LeechFieldCues).
            case LAMPREY_LATCHED -> {
                if (!leechField.latched(pan)) {
                    bank.play(Sfx.LAMPREY_LATCH, LAMPREY_LATCH_LEVEL, pitch(0.04), pan);
                }
            }
            case LAMPREY_SHAKEN -> {
                if (!leechField.shaken(pan)) {
                    bank.play(Sfx.LAMPREY_SHAKE, LAMPREY_SHAKE_LEVEL, pitch(0.04), pan);
                }
            }
            // M7 part B, Level 15: the Burrower's eruption from its bulge's start (its rumble is the tell,
            // its impact lands as it surfaces) with round 38's crack layer over it (S.1 = b, at the same
            // pitch so the cracks stay on the eruption's break), and its dive's grain slide.
            case BURROW_BULGE -> {
                float bulgePitch = pitch(0.03);
                bank.play(Sfx.BURROW_ERUPTION, 1, bulgePitch, pan);
                bank.play(Sfx.BURROW_BULGE, 1, bulgePitch, pan);
            }
            case BURROW_DIVE -> bank.play(Sfx.BURROW_DIVE, 1, pitch(0.04), pan);
            // Level 05's sounds (round 21): the Polyp Mortar's lob and impact.
            case MORTAR_LOBBED -> {
                if (firedBy(events.x(i), events.y(i), Unit.SHELLBACK)) {
                    // M7 part D: a Shellback's vent coughs lower than the Polyp Mortar (round 40 a).
                    bank.play(Sfx.SHELLBACK_LOB, SIEGE_LEVEL, pitch(0.05), pan);
                } else {
                    bank.play(Sfx.MORTAR_LOB, 0.6f * EXPLOSIONS, pitch(0.05), pan);
                }
            }
            // M7 part D (round 40's proposals, levelled in their files): a ring absorbing a hit (spaced out),
            // the rings collapsing at their Herald's death or stripped by the EMP Burst, a Shellback curling
            // and uncurling (its roll is a loop, watch), its vent destroyed.
            case RING_ABSORBED -> {
                if (FlightSounds.absorbDue(lastAbsorb, now)) {
                    bank.play(Sfx.AURA_ABSORB, SIEGE_LEVEL, pitch(0.04), pan);
                    lastAbsorb = now;
                }
            }
            case RINGS_COLLAPSED, RINGS_STRIPPED, CURLED, UNCURLED -> {
                if (events.type(i) == SimEvents.Type.CURLED
                        || events.type(i) == SimEvents.Type.UNCURLED
                        || events.value(i) > 0) {
                    bank.play(FlightSounds.siegeSound(events.type(i)), SIEGE_LEVEL, pitch(0.03), pan);
                }
            }
            case UNIT_PART_DESTROYED ->
                bank.play(alternate(Sfx.EXPLOSION_SMALL_A, Sfx.EXPLOSION_SMALL_B), EXPLOSIONS, pitch(0.04), pan);
            case MORTAR_IMPACT -> {
                // M8 part H: in the run a lob's burst is a cruiser's flak shell's airburst.
                if (!maw.flakImpact(pan)) {
                    bank.play(Sfx.MORTAR_IMPACT, 0.7f * EXPLOSIONS, pitch(0.05), pan);
                }
            }
            // A Brood Pod's fleshy burst, shot or on its own (round 08 b), as its Skitters fly out.
            case BROOD_HATCHED -> bank.play(Sfx.BROOD_BURST, EXPLOSIONS, pitch(0.04), pan);
            // Level 06: a cut chain's wet tear (round 27 a, provisional), its regrowth, the
            // chained pops, the Mantis's telegraph and beam.
            case CHAIN_CUT -> {
                // M7 part C: a Threadcrawler's cut (value: the segment's kind) tears lower than the Coilwyrm's.
                if (underIce.eelPart(events.value(i))) {
                    // M8 part C: an eel's cut tears as its segments do (no regrowth: the rear part scatters).
                    bank.play(Sfx.EEL_SEGMENT, EXPLOSIONS, pitch(0.04), pan);
                } else if (crawlerPart(events.value(i))) {
                    bank.play(Sfx.COILWYRM_CUT, CRAWLER_CUT_LEVEL, CRAWLER_CUT_PITCH * pitch(0.04), pan);
                } else {
                    bank.play(Sfx.COILWYRM_CUT, EXPLOSIONS, pitch(0.04), pan);
                }
            }
            // M7 part C: a tendril breaking, an anchor's death or burnt through (round 39 a).
            case TENDRIL_BROKEN -> bank.play(Sfx.TENDRIL_BREAK, EXPLOSIONS, pitch(0.04), pan);
            // The new head growing (round 23 b, an insect growl and chitter).
            case CHAIN_REGROWN -> bank.play(Sfx.COILWYRM_REGROW, EXPLOSIONS, pitch(0.04), pan);
            // A chained death's burst (round 24): the member's own burst, in the step its look bursts.
            case CHAIN_POP -> {
                int value = events.value(i);
                EnemyLooks kind = looks[SimEvents.chainPopKind(value)];
                bank.play(
                        explosion(kind),
                        EXPLOSIONS,
                        FlightSounds.ripplePitch(SimEvents.chainPopWidth(value)) * pitch(0.02),
                        pan);
                // M8 part C: an eel's segments tear as they pop.
                underIce.destroyed(SimEvents.chainPopKind(value), pan);
            }
            // The Mantis (round 23): its telegraph a warning (the player-damage level), its beam a
            // little above the enemy fire, one long sound.
            case SWEEP_TELEGRAPH -> {
                // M9 part E: a Sentinel Tower's charge (DeepCoreCues).
                if (!deepCore.sweep(events.type(i), events.value(i), events.x(i), events.y(i), pan)) {
                    bank.play(Sfx.MANTIS_TELEGRAPH, PLAYER_DAMAGE, pitch(0.03), pan);
                }
            }
            // M5 part C: the Hive Node's iris and release, the Ravager's take-off.
            case SPAWN_TELEGRAPH -> {
                // M8 part D: a spawning reef's iris grinds open instead (AbyssCues).
                if (!abyss.telegraph(events.value(i), pan)) {
                    bank.play(Sfx.HIVE_IRIS, HITS, pitch(0.04), pan);
                }
            }
            case SPAWN_RELEASED -> {
                if (!abyss.released(events.value(i))) {
                    bank.play(Sfx.HIVE_SPAWN, EXPLOSIONS, pitch(0.04), pan);
                }
            }
            case POUNCE -> bank.play(FlightSounds.pounce(mix.random), HITS, pitch(0.06), pan);
            case SWEEP_FIRED -> {
                // M9 part E: a Sentinel's beam is a loop (DeepCoreCues).
                if (!deepCore.sweep(events.type(i), events.value(i), events.x(i), events.y(i), pan)) {
                    bank.play(Sfx.MANTIS_SWEEP, HITS, pitch(0.03), pan);
                }
            }
            // M5 part D: a Wraith's decloak and a Mote Swarm's loop-back, levelled like the Vrell
            // spawns (round 32: decloak b, swarm b).
            case DECLOAK, LOOP_BACK -> bank.play(FlightSounds.eventSound(events.type(i)), EXPLOSIONS, pitch(0.03), pan);
            default -> {}
        }
    }

    /**
     * The large Vrell units entering the screen (the screech, levelled like the Vrell spawns, under the
     * explosions) and a swarm's entry (M5 part D).
     */
    void watchEntries(Sortie sortie) {
        Sfx screeched = screech.watch(sortie, mix.resync);
        if (screeched != null) {
            bank.play(screeched, EXPLOSIONS, pitch(0.03), pan(screech.x()));
        }
        if (swarms.watch(sortie, mix.resync)) {
            bank.play(Sfx.MOTE_SWARM, EXPLOSIONS, pitch(0.03), pan(swarms.x()));
        }
    }

    /**
     * A kill's explosion: its rung's two files alternated (or its own); M8 part C: below the waterline the under-water
     * rung's file ({@link Sfx#underwater}).
     */
    private Sfx explosion(EnemyLooks kind) {
        Sfx air = alternate(kind.explosionA(), kind.explosionB());
        return underIce.underwater() ? Sfx.underwater(air) : air;
    }

    /** Whether a unit with a proximity ring stands at (x, y), where its ring just fired. */
    private boolean ringAt(double x, double y) {
        Sortie watched = mix.watched;
        if (watched == null) {
            return false;
        }
        for (int e = 0; e < watched.enemyCount(); e++) {
            var enemy = watched.enemy(e);
            if (Math.abs(enemy.renderX(1) - x) < 0.5
                    && Math.abs(enemy.renderY(1) - y) < 0.5
                    && enemy.spec().ring().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** M7 part D: whether the unit nearest (x, y), where a volley or lob left, is a {@code unit}. */
    private boolean firedBy(double x, double y, Unit unit) {
        Enemy nearest = firer(x, y);
        return nearest != null && unit.is(nearest.spec());
    }

    /** M7 part E: the unit nearest (x, y), where a volley or lob left; null when none is within reach. */
    private Enemy firer(double x, double y) {
        Sortie watched = mix.watched;
        if (watched == null) {
            return null;
        }
        Enemy nearest = null;
        double best = FIRED_REACH;
        for (int e = 0; e < watched.enemyCount(); e++) {
            Enemy enemy = watched.enemy(e);
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance <= best) {
                best = distance;
                nearest = enemy;
            }
        }
        return nearest;
    }

    /** M7 part C: whether the level's enemy kind {@code kind} is a Threadcrawler's part (head, segment or tail). */
    private boolean crawlerPart(int kind) {
        return crawlerParts != null && kind >= 0 && kind < crawlerParts.length && crawlerParts[kind];
    }

    /** M7 part C: a loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        unitLevels[loop.ordinal()] = 0;
    }

    /**
     * M7 part C (design/audio/sfx, Act 3 sounds and Mixing): the unit loops, each once for the nearest of its
     * kind and panned to it, fading in and out over 0.2 s: the tendril hum for the nearest standing tendril (a
     * share of it while dormant, rising with the flare), the burn while a beam burns one, the Threadcrawler's
     * walk while one is on screen.
     */
    void watchCanyon(Sortie sortie) {
        if (crawlerParts == null) {
            crawlerParts = FlightSounds.crawlerParts(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Links links = sortie.links();
        double nearest = Double.POSITIVE_INFINITY;
        float hum = 0;
        float humPan = 0;
        float burnPan = 0;
        boolean burning = false;
        for (int i = 0; links != null && i < links.size(); i++) {
            Tendril tendril = links.get(i);
            if (!tendril.standing()) {
                continue;
            }
            double ax = tendril.ax();
            double ay = tendril.ay();
            double dx = tendril.bx() - ax;
            double dy = tendril.by() - ay;
            double lengthSquared = dx * dx + dy * dy;
            double u =
                    lengthSquared == 0 ? 0 : Math.clamp(((shipX - ax) * dx + (shipY - ay) * dy) / lengthSquared, 0, 1);
            double x = ax + dx * u;
            double distance = Math.hypot(x - shipX, ay + dy * u - shipY);
            if (tendril.burning()) {
                burning = true;
                burnPan = pan(x);
            }
            if (distance < nearest) {
                nearest = distance;
                hum = tendril.dormant() ? HUM_DORMANT : HUM_DORMANT + (1 - HUM_DORMANT) * (float) tendril.flare();
                humPan = pan(x);
            }
        }
        fadeLoop(Sfx.TENDRIL_HUM, flying ? hum * UNIT_LOOP_LEVEL : 0, humPan);
        fadeLoop(Sfx.TENDRIL_BURN, flying && burning ? UNIT_LOOP_LEVEL : 0, burnPan);
        nearest = Double.POSITIVE_INFINITY;
        float walkPan = 0;
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (!chain.ground() || !chain.alive()) {
                continue;
            }
            for (int k = 0; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                if (member == null || chain.stopped(k)) {
                    continue;
                }
                double x = member.renderX(1);
                double y = member.renderY(1);
                if (y < 0 || y > PlayField.HEIGHT) {
                    continue;
                }
                double distance = Math.hypot(x - shipX, y - shipY);
                if (distance < nearest) {
                    nearest = distance;
                    walkPan = pan(x);
                }
            }
        }
        fadeLoop(Sfx.CRAWLER_WALK, flying && nearest < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0, walkPan);
    }

    /**
     * M7 part D (design/audio/sfx, Act 3 sounds and Mixing): Level 17's unit loops, fading in and out over 0.2 s:
     * the Herald's aura while any unit carries a ring (panned to the nearest ringed unit), the Shellback's walk for
     * the nearest one walking on screen and its roll for the nearest one rolling, each panned to it.
     */
    void watchSiege(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double ringed = Double.POSITIVE_INFINITY;
        double walking = Double.POSITIVE_INFINITY;
        double rolling = Double.POSITIVE_INFINITY;
        float ringPan = 0;
        float walkPan = 0;
        float rollPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            double distance = Math.hypot(x - shipX, y - shipY);
            if (enemy.ring() > 0 && distance < ringed) {
                ringed = distance;
                ringPan = pan(x);
            }
            if (!Unit.SHELLBACK.is(enemy.spec()) || y < 0 || y > PlayField.HEIGHT) {
                continue;
            }
            if (enemy.curlPhase() == Enemy.CurlPhase.WALK && distance < walking) {
                walking = distance;
                walkPan = pan(x);
            } else if (enemy.curlPhase() == Enemy.CurlPhase.ROLLING && distance < rolling) {
                rolling = distance;
                rollPan = pan(x);
            }
        }
        fadeLoop(Sfx.HERALD_AURA, flying && ringed < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0, ringPan);
        fadeLoop(Sfx.SHELLBACK_WALK, flying && walking < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0, walkPan);
        fadeLoop(Sfx.SHELLBACK_ROLL, flying && rolling < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0, rollPan);
    }

    /** M7 part C: moves a unit loop's level toward {@code target} by a fade step, starting or stopping it. */
    private void fadeLoop(Sfx loop, float target, float pan) {
        unitLevels[loop.ordinal()] = fadeLoop(loop, unitLevels[loop.ordinal()], target, pan);
    }
}
