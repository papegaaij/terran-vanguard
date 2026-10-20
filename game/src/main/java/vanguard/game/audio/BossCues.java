package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.AIM_TELL_LEVEL;
import static vanguard.game.audio.FlightSounds.AIM_TELL_PITCH;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.PICKUPS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.ROOT_HIT_LEVEL;
import static vanguard.game.audio.FlightSounds.ROOT_HIT_PITCH;
import static vanguard.game.audio.FlightSounds.ROOT_HIT_RAISED_LEVEL;
import static vanguard.game.audio.FlightSounds.ROOT_HIT_RAISED_PITCH;
import static vanguard.game.audio.FlightSounds.SLAM_IMPACT_SECONDS;
import static vanguard.game.audio.FlightSounds.WITHERING_DELAY_SECONDS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The bosses' and set pieces' sounds for {@link FlightSounds}: a set piece's death on the explosion
 * ladder with its cry, its parts' blasts, a boss's arrival, phases and launches, a boss's own sounds
 * (round 25, the Brood Carrier: its roar at the turn, its sacs and its iris, followed in {@link #watch}),
 * the Harbour Kraken's churn, slam and surfacing, the Siege Spire's and the root bursts' sounds.
 */
final class BossCues extends SoundFamily {
    /**
     * A boss's own sounds (design/audio/sfx, round 25: the Brood Carrier): its roar as it arrives
     * and as it turns, its sacs opening and shutting with their windows, a sac bursting, its iris
     * opening over the core. A boss without them keeps the generic sounds. The units its windows
     * launch spit out with {@link Sfx#CARRIER_LAUNCH} (only the carrier has windows).
     */
    record BossSounds(Sfx roar, Sfx sacOpen, Sfx sacClose, Sfx sacBurst, Sfx iris) {}

    /** The roar starts this many steps after the klaxon, so the klaxon's first blast is heard on its own. */
    private static final int ARRIVAL_ROAR_STEPS = 45;
    /** The roar at the turn is a little lower than the arrival's. */
    private static final float TURN_ROAR_PITCH = 0.85f;
    /** At most this many launch sounds for one opening, this many steps apart after it opens. */
    private static final int LAUNCH_SOUNDS = 3;

    private static final int LAUNCH_STEPS = 6;

    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.PART_DESTROYED,
            SimEvents.Type.SET_PIECE_DESTROYED,
            SimEvents.Type.TELEGRAPH,
            SimEvents.Type.SURFACE,
            SimEvents.Type.ROOT_HIT,
            SimEvents.Type.AIM_TELL,
            SimEvents.Type.BOSS_ARRIVED,
            SimEvents.Type.BOSS_PHASE,
            SimEvents.Type.BOSS_LAUNCHED,
            SimEvents.Type.BOSS_DESTROYED,
            SimEvents.Type.ROOT_BURST_TELEGRAPH,
            SimEvents.Type.ROOT_BURST_GREW,
            SimEvents.Type.SPIRE_MAW_OPENED,
            SimEvents.Type.SPIRE_LIFT,
            SimEvents.Type.SPIRE_SWEEP_TELEGRAPH,
            SimEvents.Type.SPIRE_SWEEP);

    /** Each set piece's death cry, by index in the script; null for none. */
    private final Sfx[] cries;
    /** Round 43: each set piece's own death instead of its rung's blast, by index in the script; null for the rung's. */
    private final Sfx[] deaths;
    /** Each set piece's own sounds, by index in the script; null for the generic ones. */
    private final BossSounds[] bossSounds;
    /** Whether a boss of the level brings its own phase sounds (its turn and its iris, not the generic phase blast). */
    private final boolean ownPhaseSounds;
    /** Whether each set piece's death is huge: an act boss, or a set piece that is no boss. */
    private boolean[] hugeDeath;
    /** Whether each set piece's death is an act boss's tail-to-head chain. */
    private boolean[] finale;
    /** What {@link #watch} saw at the last step: each own-sound boss's parts open and wrecked, its turn. */
    private boolean[][] wasOpen;

    private boolean[][] wasWrecked;
    private boolean[] wasTurning;
    /** The steps from a lane's telegraph to the slam sound's start, and the second lane's lag; 0 without arena. */
    private int slamSteps;

    private int secondSteps;
    /** M5 part H: the level is under the Vrell canopy (Level 14): the canopy withers at the boss's death. */
    private boolean canopy;
    /** Round 40: per root part the step of its last wet hit. */
    private long[] lastRootHit = new long[0];
    /** This step's: the step, the units leaving open windows and their pan's sum, the lanes telegraphed. */
    private long now;

    private int launches;
    private float launchPan;
    private int telegraphs;

    BossCues(SoundMix mix, List<String> setPieces) {
        super(mix);
        cries = setPieces.stream().map(BossCues::cry).toArray(Sfx[]::new);
        deaths = setPieces.stream().map(BossCues::death).toArray(Sfx[]::new);
        bossSounds = setPieces.stream().map(BossCues::bossSounds).toArray(BossSounds[]::new);
        ownPhaseSounds = Arrays.stream(bossSounds).anyMatch(Objects::nonNull);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** A set piece's cry at its death, or null for none: the Leviathan's whale song. */
    private static Sfx cry(String slug) {
        return switch (slug) {
            case "leviathan" -> Sfx.LEVIATHAN_CRY;
            // M5 part E: the Kraken's death groan, under the huge water burst (round 33 a).
            case "harbour-kraken" -> Sfx.KRAKEN_DEATH;
            default -> null;
        };
    }

    /** Round 43: a set piece's own death instead of its rung's blast, or null: the Revenant Walker's (round 43 a). */
    private static Sfx death(String slug) {
        return "revenant-walker".equals(slug) ? Sfx.WALKER_DEATH : null;
    }

    /** A boss's own sounds, or null for the generic ones: the Brood Carrier's (round 25 a, provisional). */
    private static BossSounds bossSounds(String slug) {
        return switch (slug) {
            case "brood-carrier" ->
                new BossSounds(
                        Sfx.CARRIER_ROAR,
                        Sfx.CARRIER_SAC_OPEN,
                        Sfx.CARRIER_SAC_CLOSE,
                        Sfx.CARRIER_SAC_BURST,
                        Sfx.CARRIER_IRIS);
            default -> null;
        };
    }

    /** Set piece {@code k}'s sac burst for its chained death's bursts at its sacs, or null for the generic explosions. */
    Sfx sacBurst(int k) {
        return k < bossSounds.length && bossSounds[k] != null ? bossSounds[k].sacBurst() : null;
    }

    /** See {@link FlightSounds#deathBlast}. */
    Sfx deathBlast(int k) {
        return hugeDeath == null || k >= hugeDeath.length || hugeDeath[k]
                ? Sfx.EXPLOSION_HUGE_A
                : Sfx.EXPLOSION_LARGE_B;
    }

    /** Whether set piece {@code k}'s death is an act boss's tail-to-head chain, which ends in its {@link #deathBlast}. */
    private boolean finale(int k) {
        return finale != null && k < finale.length && finale[k];
    }

    /** See {@link FlightSounds#slamCycle}. */
    void slamCycle(double telegraph, double rise, double second) {
        slamSteps = Math.max(1, SimStep.ticks(telegraph + rise - SLAM_IMPACT_SECONDS));
        secondSteps = SimStep.ticks(second);
    }

    /** See {@link FlightSounds#canopy}. */
    void canopy(boolean canopy) {
        this.canopy = canopy;
    }

    /** At the first watch: which set pieces die huge, and which in an act boss's chain. */
    void noteDeaths(Sortie sortie) {
        hugeDeath = new boolean[sortie.setPieceCount()];
        finale = new boolean[sortie.setPieceCount()];
        for (int k = 0; k < hugeDeath.length; k++) {
            SetPiece piece = sortie.setPiece(k);
            boolean boss = piece.boss().isPresent();
            hugeDeath[k] = !boss || LevelRenderer.flashesAtDeath(piece);
            finale[k] = boss && LevelRenderer.tailToHead(piece);
        }
    }

    /** At the first watch: the state the own-sound bosses' parts are followed by. */
    void noteParts(Sortie sortie) {
        int pieces = pieces(sortie);
        wasOpen = new boolean[pieces][];
        wasWrecked = new boolean[pieces][];
        wasTurning = new boolean[pieces];
        for (int k = 0; k < pieces; k++) {
            int parts = sortie.setPiece(k).partCount();
            wasOpen[k] = new boolean[parts];
            wasWrecked[k] = new boolean[parts];
        }
    }

    private int pieces(Sortie sortie) {
        return Math.min(bossSounds.length, sortie.setPieceCount());
    }

    /** The own-sound bosses' turns, sacs and irises (see {@link FlightSounds#watch}). */
    void watch(Sortie sortie) {
        int pieces = pieces(sortie);
        for (int k = 0; k < pieces; k++) {
            if (bossSounds[k] != null) {
                watchBoss(bossSounds[k], sortie.setPiece(k), k);
            }
        }
    }

    /** A restart or a boss checkpoint's restore: the roots' hits start over. */
    void restarted() {
        Arrays.fill(lastRootHit, Long.MIN_VALUE / 2);
    }

    @Override
    void begin(long now) {
        this.now = now;
        launches = 0;
        launchPan = 0;
        telegraphs = 0;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case PART_DESTROYED ->
                bank.play(alternate(Sfx.EXPLOSION_MEDIUM_A, Sfx.EXPLOSION_MEDIUM_B), EXPLOSIONS, pitch(0.04), pan);
            // The whole unit: its rung at once, or, for an act boss's tail-to-head chain, the large
            // rung as the chain starts (its huge blast ends the chain, deathBlast).
            case SET_PIECE_DESTROYED -> {
                int k = events.value(i);
                Sfx own = k < deaths.length ? deaths[k] : null;
                if (own != null) {
                    // Round 43: the walker's own death (levelled in its file), its impact on the topple's landing.
                    bank.play(own, 1, 1, pan);
                } else {
                    bank.play(finale(k) ? Sfx.EXPLOSION_LARGE_A : deathBlast(k), PLAYER_DAMAGE, pitch(0.03), pan);
                }
                // Levelled like the enemy sounds, so it sits under the burst (design/audio/sfx).
                Sfx cry = cries[k];
                if (cry != null) {
                    bank.play(cry, EXPLOSIONS, 1, pan);
                }
            }
            // The Harbour Kraken: a lane's churn now and its slam's rush so the impact lands on the
            // slam; the second lane of a volley 0.5 s after the first; the head's surfacing swell.
            case TELEGRAPH -> {
                bank.play(Sfx.KRAKEN_CHURN, 1, 1, pan);
                later(Sfx.KRAKEN_SLAM, 1, 1, pan, slamSteps + telegraphs * secondSteps);
                telegraphs++;
            }
            case SURFACE -> bank.play(Sfx.KRAKEN_SURFACE, 1, 1, pan);
            // Round 40 (proposal 3a, pending the user's review): a rooted arm's root hit: a wet splat
            // (the Polyp Mortar's impact, reused) over the shot's organic hit, spaced out per root,
            // meatier while its arm is raised (the raised multiplier). Its cut is PART_DESTROYED's blast.
            case ROOT_HIT -> rootHit(events.value(i), now, pan);
            // Round 40 (proposal 4a, pending the user's review): a Spire pod's aim tell: the Mantis's
            // charge-up (reused), higher and quieter than the sweep's telegraph.
            case AIM_TELL -> bank.play(Sfx.MANTIS_TELEGRAPH, AIM_TELL_LEVEL, AIM_TELL_PITCH * pitch(0.03), pan);
            // The boss's arrival warns like an edge warning; its death pays out in a shower.
            // A boss with its own sounds roars as it comes in (after the klaxon's first blast); its
            // phases sound in watch() (the turn's roar, the iris), not as the generic blast.
            case BOSS_ARRIVED -> {
                bank.play(Sfx.EDGE_WARNING, PICKUPS, 1, 0);
                BossSounds own = events.value(i) < bossSounds.length ? bossSounds[events.value(i)] : null;
                if (own != null) {
                    later(own.roar(), EXPLOSIONS, 1, pan, ARRIVAL_ROAR_STEPS);
                }
            }
            case BOSS_PHASE -> {
                if (!ownPhaseSounds) {
                    bank.play(alternate(Sfx.EXPLOSION_LARGE_A, Sfx.EXPLOSION_LARGE_B), EXPLOSIONS, 0.9f, pan);
                }
            }
            // A unit leaving an open window (the carrier's sacs): counted, played after the step's events.
            case BOSS_LAUNCHED -> {
                launches++;
                launchPan += pan;
            }
            case BOSS_DESTROYED -> {
                bank.play(Sfx.SALVAGE_LARGE, PICKUPS, 1, pan);
                if (canopy) {
                    // M5 part H (round 36 b): the canopy withers over the city as it tears.
                    later(Sfx.CANOPY_WITHERING, 1, 1, 0, SimStep.ticks(WITHERING_DELAY_SECONDS));
                }
            }
            // M5 part H, Level 14 (round 36, the user's picks): a root burst's stone giving way from its telegraph,
            // its growths growing in; the Siege Spire's crown opening (the carrier's sac), its
            // tear-free roar at the lift, its sweep's telegraph and beam (the Mantis's).
            case ROOT_BURST_TELEGRAPH -> bank.play(Sfx.ROOT_BURST, 1, pitch(0.03), pan);
            case ROOT_BURST_GREW -> bank.play(Sfx.ROOT_GROWTH, 1, pitch(0.04), pan);
            case SPIRE_MAW_OPENED -> bank.play(Sfx.CARRIER_SAC_OPEN, EXPLOSIONS, 0.85f, pan);
            case SPIRE_LIFT -> bank.play(Sfx.TEAR_FREE_ROAR, 1, 1, pan);
            case SPIRE_SWEEP_TELEGRAPH -> bank.play(Sfx.MANTIS_TELEGRAPH, PLAYER_DAMAGE, pitch(0.03), pan);
            case SPIRE_SWEEP -> bank.play(Sfx.MANTIS_SWEEP, HITS, pitch(0.03), pan);
            default -> {}
        }
    }

    @Override
    void end() {
        // An opening's units all leave in one step: a few spits just after the sac opens, not one each.
        for (int n = 0; n < Math.min(launches, LAUNCH_SOUNDS); n++) {
            later(Sfx.CARRIER_LAUNCH, EXPLOSIONS, pitch(0.06), launchPan / launches, (n + 1) * LAUNCH_STEPS);
        }
    }

    private void rootHit(int part, long now, float pan) {
        if (part >= lastRootHit.length) {
            int from = lastRootHit.length;
            lastRootHit = Arrays.copyOf(lastRootHit, part + 1);
            Arrays.fill(lastRootHit, from, lastRootHit.length, Long.MIN_VALUE / 2);
        }
        if (!FlightSounds.rootHitDue(lastRootHit[part], now)) {
            return;
        }
        lastRootHit[part] = now;
        boolean raised = rootRaised(part);
        bank.play(
                Sfx.MORTAR_IMPACT,
                raised ? ROOT_HIT_RAISED_LEVEL : ROOT_HIT_LEVEL,
                (raised ? ROOT_HIT_RAISED_PITCH : ROOT_HIT_PITCH) * pitch(0.04),
                pan);
    }

    /** Round 40: whether root part {@code part}'s rooted arm is raised (in the last watched sortie). */
    private boolean rootRaised(int part) {
        Sortie watched = mix.watched;
        if (watched == null) {
            return false;
        }
        for (int k = 0; k < watched.setPieceCount(); k++) {
            var arena = watched.setPiece(k).arena();
            if (arena.isEmpty() || !arena.get().rooted()) {
                continue;
            }
            for (int a = 0; a < arena.get().armCount(); a++) {
                if (arena.get().armPart(a) == part) {
                    return arena.get().armRaised(a);
                }
            }
        }
        return false;
    }

    private void watchBoss(BossSounds own, SetPiece piece, int k) {
        boolean resync = mix.resync;
        boolean turning = piece.motion() == SetPiece.Motion.TURN;
        if (!resync && turning && !wasTurning[k]) {
            bank.play(own.roar(), EXPLOSIONS, TURN_ROAR_PITCH, pan(piece.renderX(1)));
        }
        wasTurning[k] = turning;
        boolean dying = false;
        for (int p = 0; p < piece.partCount(); p++) {
            dying |= vital(piece, p) && piece.partWrecked(p) && !wasWrecked[k][p];
        }
        int opened = 0;
        int shut = 0;
        double openedX = 0;
        double shutX = 0;
        for (int p = 0; p < piece.partCount(); p++) {
            boolean wrecked = piece.partWrecked(p);
            // As the renderer reads it: a sac open in its window, the core once it takes damage.
            boolean open = !wrecked && (piece.partWindowed(p) ? piece.partOpen(p) : !piece.partShielded(p));
            if (!resync && !piece.partArmoured(p)) {
                if (vital(piece, p)) {
                    if (open && !wasOpen[k][p]) {
                        bank.play(own.iris(), EXPLOSIONS, 1, pan(piece.partX(p)));
                    }
                } else if (wrecked && !wasWrecked[k][p]) {
                    if (!dying) {
                        bank.play(own.sacBurst(), EXPLOSIONS, pitch(0.04), pan(piece.partX(p)));
                    }
                } else if (open && !wasOpen[k][p]) {
                    opened++;
                    openedX += piece.partX(p);
                } else if (!open && wasOpen[k][p] && !wrecked) {
                    shut++;
                    shutX += piece.partX(p);
                }
            }
            wasOpen[k][p] = open;
            wasWrecked[k][p] = wrecked;
        }
        if (opened > 0) {
            bank.play(own.sacOpen(), EXPLOSIONS, pitch(0.04), pan(openedX / opened));
        }
        if (shut > 0) {
            bank.play(own.sacClose(), EXPLOSIONS, pitch(0.04), pan(shutX / shut));
        }
    }

    private static boolean vital(SetPiece piece, int p) {
        return piece.spec().parts().get(p).vital();
    }
}
