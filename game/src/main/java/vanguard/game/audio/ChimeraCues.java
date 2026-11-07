package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4 (design/campaign Level 48, Music &amp; ambience; design/enemies/bosses/vornes-chimera, Sounds;
 * design/audio/sfx, Act 7 sounds; round 84's picks levelled in their files, played as they are): Level 48's sounds for
 * {@link FlightSounds}:
 *
 * <ul>
 *   <li>the spire well splitting and the Chimera rising ({@link Sfx#CHIMERA_RISE}, S.20 a): its arrival's own sound, {@link
 *       BossCues}' arrival roar after the klaxon's first blast;
 *   <li>the fusion's pain at phase 2's start ({@link Sfx#CHIMERA_PAIN}, S.21 b) and the capsule opening at phase 3's
 *       ({@link Sfx#CHIMERA_CAPSULE}, S.23 b), instead of a phase's generic blast ({@link BossCues} hands its {@code
 *       BOSS_PHASE} here: {@link #phase});
 *   <li>the rear sweep ({@link Sfx#CHIMERA_SWEEP}, S.22 b): its drop out of the top edge ({@code SWEEP_OUT}, at the boss)
 *       and its climb from the bottom edge ({@code SWEEP_CLIMB}, at its mark, a shade higher): a jet's pass each;
 *   <li>the burst at the last words' end ({@link Sfx#CHIMERA_BURST}, S.24 a) as its own death, instead of a rung's blast
 *       ({@link BossCues}' death sound at the burst's {@code SET_PIECE_DESTROYED});
 *   <li>a reactor core's death over its rung ({@link Sfx#REACTOR_CORE_DEATH}, S.52 a; {@link UnitCues} hands its {@code
 *       ENEMY_DESTROYED} here: {@link #destroyed});
 *   <li>Vorne's archive wrenched open ({@link Sfx#POD_ARCHIVE}, S.54 b) as it breaks, over the built crate break, once an
 *       attempt at most ({@link #watch}).
 * </ul>
 *
 * The lance charges and fires on the Ascendant's files at {@value #LANCE_PITCH} ({@link AscendantCues}, {@link
 * SovereignCues}). Its tell ({@code SWEEP_TELL}) is Rook's bark and the flank's flare, its mark ({@code SWEEP_MARK}) and its end ({@code
 * SWEEP_DONE}) silent; K ({@code LAST_WORDS}) cuts the boss music ({@link LevelMusic#bossCut}) under Vorne's last line
 * and the bullets' fade ({@code BULLETS_FADE}) is silent.
 */
final class ChimeraCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.SWEEP_TELL,
            SimEvents.Type.SWEEP_OUT,
            SimEvents.Type.SWEEP_MARK,
            SimEvents.Type.SWEEP_CLIMB,
            SimEvents.Type.SWEEP_DONE,
            SimEvents.Type.LAST_WORDS,
            SimEvents.Type.BULLETS_FADE,
            SimEvents.Type.LAST_BURST);

    static final String SLUG = "vornes-chimera";
    static final String CORE = "reactor-core";
    /** Vorne's archive's ground object look (design/campaign Level 48, the secret). */
    static final String ARCHIVE = "vorne-archive";
    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The lance: the Ascendant's charge and shot at this pitch (higher and thinner, no new file; AscendantCues). */
    static final float LANCE_PITCH = 1.25f;
    /** The climb's pass a shade higher than the drop's (rising toward the ship). */
    static final float CLIMB_PITCH = 1.08f;
    /** The phases whose start has its own sound: the fusion (phase 2) and the capsule's opening (phase 3). */
    static final int FUSION_PHASE = 1;

    static final int CAPSULE_PHASE = 2;

    /** Per enemy kind whether it is a reactor core; null until the first sortie is seen. */
    private boolean[] cores;
    /** Whether the archive's break has sounded in this attempt; where it stood at the last watch (NaN: not there). */
    private boolean archiveOpened;

    private double archiveX = Double.NaN;
    private double archiveY = Double.NaN;

    ChimeraCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** Vorne's Chimera among a sortie's set pieces, or null. */
    static SetPiece chimera(Sortie sortie) {
        if (sortie == null) {
            return null;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.spec().slug().equals(SLUG)) {
                return piece;
            }
        }
        return null;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        Sfx sfx = sound(events.type(i));
        if (sfx != null) {
            bank.play(sfx, CUE_LEVEL, events.type(i) == SimEvents.Type.SWEEP_CLIMB ? CLIMB_PITCH : 1, pan);
        }
    }

    /** The sound of one of its events, null for a silent one. */
    static Sfx sound(SimEvents.Type type) {
        return switch (type) {
            case SWEEP_OUT, SWEEP_CLIMB -> Sfx.CHIMERA_SWEEP;
            default -> null;
        };
    }

    /** The sound at the start of the Chimera's phase {@code phase}, null for none. */
    static Sfx phaseSound(int phase) {
        return switch (phase) {
            case FUSION_PHASE -> Sfx.CHIMERA_PAIN;
            case CAPSULE_PHASE -> Sfx.CHIMERA_CAPSULE;
            default -> null;
        };
    }

    /** A boss entered phase {@code phase}: the Chimera's own sound for it; false (playing nothing) for another boss. */
    boolean phase(int phase, float pan) {
        if (chimera(mix.watched) == null) {
            return false;
        }
        Sfx sfx = phaseSound(phase);
        if (sfx != null) {
            bank.play(sfx, CUE_LEVEL, 1, pan);
        }
        return true;
    }

    private void noted() {
        if (cores == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    private void noteKinds(List<EnemySpec> kinds) {
        cores = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            cores[k] = kinds.get(k).slug().equals(CORE);
        }
    }

    /** A unit of kind {@code kind} was destroyed: a reactor core's crack and power-down over its rung. */
    void destroyed(int kind, float pan) {
        noted();
        if (cores != null && kind >= 0 && kind < cores.length && cores[kind]) {
            bank.play(Sfx.REACTOR_CORE_DEATH, CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /** A secret found within this of where the archive was at the last step is the archive's, px. */
    static final double ARCHIVE_REACH = 48;

    /** Once a step: Vorne's archive wrenched open as it breaks (its secret found where it stood; once an attempt). */
    void watch(Sortie sortie) {
        if (cores == null) {
            noteKinds(sortie.enemyKinds());
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size() && !Double.isNaN(archiveX); i++) {
            if (events.type(i) == SimEvents.Type.SECRET_FOUND
                    && Math.hypot(events.x(i) - archiveX, events.y(i) - archiveY) <= ARCHIVE_REACH
                    && !archiveOpened
                    && !mix.resync) {
                archiveOpened = true;
                bank.play(Sfx.POD_ARCHIVE, CUE_LEVEL, 1, pan(archiveX));
            }
        }
        archiveX = Double.NaN;
        archiveY = Double.NaN;
        for (int g = 0; g < sortie.groundObjectCount(); g++) {
            GroundObject object = sortie.groundObject(g);
            if (object.spec().look().equals(ARCHIVE) && !object.spent()) {
                archiveX = object.renderX(1);
                archiveY = object.renderY(1);
                // Standing: a new attempt (or one before the archive) may sound it again.
                archiveOpened = false;
            }
        }
    }

    /** Whether the archive's break has sounded in this attempt, for the tests. */
    boolean archiveOpened() {
        return archiveOpened;
    }
}
