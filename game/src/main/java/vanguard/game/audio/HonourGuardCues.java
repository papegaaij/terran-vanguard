package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.AceLeg;
import vanguard.sim.AceMove;
import vanguard.sim.AcePlaybook;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.SquadronPlaybook;

/**
 * M10 part F, step F4: the Honour Guard's sounds for {@link FlightSounds} (design/enemies/bosses/honour-guard, Sounds
 * brief; round 78's a variants until the user's picks, levelled in their files and played as they are):
 *
 * <ul>
 *   <li>an ace's fly-by: an ace on the field passing within {@value #FLYBY_RANGE} px of the ship ({@link Sfx#ACE_FLYBY},
 *       Kestrel's rougher {@link Sfx#KESTREL_FLYBY}), at most every {@value #FLYBY_GAP_SECONDS} s an ace; Vesper's roll
 *       and ribbon passes as they start; Kestrel's climb back after a pull-out;
 *   <li>Kestrel's dive: its scream from the dive's start ({@code ACE_PLAY} of a feint dive or an overhead feint, {@link
 *       Sfx#KESTREL_DIVE}), cut by the pull-out's roar ({@link Sfx#KESTREL_PULLOUT}) as its leg turns to {@link
 *       AceLeg#PULL_OUT};
 *   <li>Vesper's spiral cannon: a loop while a spiral loop runs ({@link Sfx#VESPER_CANNON}), panned with her;
 *   <li>an ace's death ({@code ACE_DOWN}): {@link Sfx#ACE_DEATH} over its rung; the last ace's {@link Sfx#ACE_LAST_CHAIN}
 *       (its 2 s chain);
 *   <li>Harker's sniper line is {@link SovereignCues}' boss lines' ({@link Sfx#SNIPER_CHARGE}, {@link Sfx#SNIPER_SHOT}
 *       in a squadron's level); the hex shields' sounds are the units' shared ones.
 * </ul>
 */
final class HonourGuardCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.ACE_PLAY, SimEvents.Type.ACE_DOWN);

    /** The cues are levelled in their files (step AUD-F) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** An ace passing this near the ship is heard flying by, at most this often an ace. */
    static final double FLYBY_RANGE = 110;

    static final double FLYBY_GAP_SECONDS = 1.5;
    private static final int FLYBY_GAP = SimStep.ticks(FLYBY_GAP_SECONDS);
    private static final int MAX_ACES = 3;

    private final double[] lastDistance = new double[MAX_ACES];
    private final long[] lastFlyby = new long[MAX_ACES];
    private final int[] lastLeg = new int[MAX_ACES];
    private boolean diving;
    private float cannonLevel;
    private long now = -1;

    HonourGuardCues(SoundMix mix) {
        super(mix);
        clear();
    }

    private void clear() {
        java.util.Arrays.fill(lastDistance, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(lastFlyby, Long.MIN_VALUE / 2);
        java.util.Arrays.fill(lastLeg, -1);
        diving = false;
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The squadron piece of the sortie being watched, or null. */
    private static SetPiece guard(Sortie sortie) {
        if (sortie == null) {
            return null;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (sortie.setPiece(k).squadron().isPresent()) {
                return sortie.setPiece(k);
            }
        }
        return null;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        SetPiece piece = guard(mix.watched);
        Squadron squadron = piece == null ? null : piece.squadron().orElseThrow();
        switch (events.type(i)) {
            case ACE_PLAY -> {
                int code = AcePlaybook.playCode(events.value(i));
                if (code == AcePlaybook.PINCER) {
                    return;
                }
                SquadronPlaybook.Kind kind = SquadronPlaybook.Kind.values()[code];
                switch (kind) {
                    case FEINT_DIVE, OVERHEAD -> {
                        bank.play(Sfx.KESTREL_DIVE, CUE_LEVEL, pitch(0.03), pan);
                        diving = true;
                    }
                    case ROLL_FAN, RIBBON -> flyby(AcePlaybook.playAce(events.value(i)), squadron, pan, true);
                    default -> {}
                }
            }
            case ACE_DOWN -> {
                boolean last = squadron != null && squadron.deaths() >= squadron.aceCount();
                bank.play(last ? Sfx.ACE_LAST_CHAIN : Sfx.ACE_DEATH, CUE_LEVEL, last ? 1 : pitch(0.04), pan);
            }
            default -> {}
        }
    }

    /** An ace's fly-by, unless one played for it within {@value #FLYBY_GAP_SECONDS} s ({@code force}: a pass starts). */
    private void flyby(int a, Squadron squadron, float pan, boolean force) {
        if (a < 0 || a >= MAX_ACES || squadron == null) {
            return;
        }
        if (!force && now - lastFlyby[a] < FLYBY_GAP) {
            return;
        }
        lastFlyby[a] = now;
        boolean kestrel = squadron.callSign(a).equalsIgnoreCase("kestrel");
        bank.play(kestrel ? Sfx.KESTREL_FLYBY : Sfx.ACE_FLYBY, CUE_LEVEL, pitch(0.04), pan);
    }

    /** Once a step: the fly-bys, the dive's pull-out and the climb after it, Vesper's cannon loop. */
    void watch(Sortie sortie) {
        SetPiece piece = guard(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (tick < now) {
            // A retry from the boss checkpoint.
            clear();
        }
        now = tick;
        Squadron squadron = piece.squadron().orElseThrow();
        AcePlaybook book = squadron.playbook().orElse(null);
        boolean flying = sortie.flying() && !sortie.complete() && piece.present() && !piece.destroyed();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double cannonPan = 0;
        boolean cannon = false;
        for (int a = 0; a < Math.min(MAX_ACES, squadron.aceCount()); a++) {
            boolean on = flying && squadron.state(a) == Squadron.State.FLYING;
            double d = on ? Math.hypot(squadron.x(a) - shipX, squadron.y(a) - shipY) : Double.POSITIVE_INFINITY;
            float pan = pan(squadron.x(a));
            if (!mix.resync && d < FLYBY_RANGE && lastDistance[a] >= FLYBY_RANGE) {
                flyby(a, squadron, pan, false);
            }
            lastDistance[a] = d;
            int leg = on ? squadron.leg(a).ordinal() : -1;
            AceMove move = squadron.move(a);
            boolean dive = move == AceMove.DIVE || move == AceMove.OVERHEAD;
            if (!mix.resync && dive && leg != lastLeg[a]) {
                if (leg == AceLeg.PULL_OUT.ordinal()) {
                    if (diving) {
                        bank.stop(Sfx.KESTREL_DIVE);
                        diving = false;
                    }
                    bank.play(Sfx.KESTREL_PULLOUT, CUE_LEVEL, pitch(0.03), pan);
                } else if (leg == AceLeg.CLIMB.ordinal() && lastLeg[a] == AceLeg.PULL_OUT.ordinal()) {
                    flyby(a, squadron, pan, true);
                }
            }
            lastLeg[a] = leg;
            if (on
                    && book != null
                    && book.kind(a) == SquadronPlaybook.Kind.SPIRAL_LOOP
                    && book.mode(a) == AcePlaybook.Mode.RUN) {
                cannon = true;
                cannonPan = pan;
            }
        }
        if (!flying && diving) {
            bank.stop(Sfx.KESTREL_DIVE);
            diving = false;
        }
        cannonLevel =
                fadeLoop(Sfx.VESPER_CANNON, cannonLevel, cannon ? FlightSounds.UNIT_LOOP_LEVEL : 0, (float) cannonPan);
    }

    /** The cannon loop's level now, for the tests. */
    float cannonLevel() {
        return cannonLevel;
    }

    /** The flight closes: the cannon loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.VESPER_CANNON) {
            cannonLevel = 0;
        }
        if (loop == Sfx.KESTREL_DIVE) {
            diving = false;
        }
    }
}
