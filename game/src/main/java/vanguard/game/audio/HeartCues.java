package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.BossPlaybook;
import vanguard.sim.LastWords;
import vanguard.sim.PartWindows;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H4 (design/campaign Level 50, Music &amp; ambience; design/enemies/bosses/choir-heart; design/audio/sfx,
 * Act 7 sounds; round 84's picks levelled in their files, step AUD-GH): the Choir Heart's and the closing gate's sounds
 * for {@link FlightSounds}:
 *
 * <ul>
 *   <li>the beat on each heartbeat ring, both beats ({@link Sfx#HEART_BEAT}, round 98 S.1 c, on {@code HEARTBEAT});
 *   <li>a membrane irising open to sing or under a hold ({@link Sfx#HEART_MEMBRANE}, S.26 a, on {@code
 *       PART_WINDOW_OPEN}), panned to it;
 *   <li>the closing as the walls' phase starts and the opening as the core's does ({@link Sfx#HEART_CLOSING} S.27 a,
 *       {@link Sfx#HEART_OPENING} S.30 a, followed on the boss's phase);
 *   <li>the line's tell from the Heart's centre ({@link Sfx#HEART_LINE}, S.28 a, as a playbook lane starts telling its
 *       {@code LINE});
 *   <li>a wall node cut out ({@link Sfx#HEART_NODE_CUT}, S.29 a, on its part's death), panned to its wall;
 *   <li>the Heart's death at K ({@link Sfx#HEART_DEATH}, S.31 a, its last words spoken), under the chain;
 *   <li>the gate's groan and the greying at its closing ({@link Sfx#GATE_GROAN} S.39 a, {@link Sfx#GATE_GREYING} S.40
 *       b, on {@code GATE_CLOSING}); the warp home ({@link Sfx#WARP_HOME}, S.35 a, on {@code WARP_HOME}); each glyph of
 *       the ring going dark, quieter each ({@link Sfx#GLYPH_DARK}, S.41 a, on {@code GLYPH_DARK});
 *   <li>the seed vault broken open over the built crate break ({@link Sfx#POD_SEED_VAULT}, S.55 a; {@link #watch}).
 * </ul>
 *
 * The phase, the deaths, K and the tells are followed from the Heart's state each step (their events are other
 * families'); nothing is allocated per step once the parts are counted.
 */
final class HeartCues extends SoundFamily {
    static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.HEARTBEAT,
            SimEvents.Type.PART_WINDOW_OPEN,
            SimEvents.Type.GATE_CLOSING,
            SimEvents.Type.WARP_HOME,
            SimEvents.Type.GLYPH_DARK);

    static final String SLUG = "choir-heart";
    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** Each glyph after the first is this much quieter than the one before (the ring falling silent). */
    static final float GLYPH_STEP = 0.08f;
    /** The quietest glyph's level. */
    static final float GLYPH_FLOOR = 0.35f;
    /** The walls' phase and the core's (0-based). */
    static final int WALLS_PHASE = 1;

    static final int CORE_PHASE = 2;

    private long now;
    /** The Heart's phase at the last step (-1: none seen yet). */
    private int phase = -1;
    /** Per part whether it lived at the last step. */
    private boolean[] alive = new boolean[0];
    /** Per playbook lane whether it told a line at the last step. */
    private final boolean[] lineTold = new boolean[BossPlaybook.LANES];
    /** Whether its last words were spoken at the last step. */
    private boolean spoken;

    HeartCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The Choir Heart among the set pieces, or null. */
    static SetPiece heart(Sortie sortie) {
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
    void begin(long now) {
        boolean back = now < this.now;
        this.now = now;
        SetPiece piece = heart(mix.watched);
        if (piece == null) {
            phase = -1;
            spoken = false;
            return;
        }
        if (!piece.present()) {
            // K bursts it at once (its last words at 0 s): the death under the chain as it goes.
            boolean nowSpoken = piece.lastWords().map(LastWords::spoken).orElse(false);
            if (nowSpoken && !spoken && phase >= 0 && !back) {
                bank.play(Sfx.HEART_DEATH, CUE_LEVEL, 1, 0);
            }
            spoken = nowSpoken;
            return;
        }
        if (back || alive.length != piece.partCount()) {
            // A retry from a checkpoint, or the first step seen: the state as it is, no sound for it.
            remember(piece);
            return;
        }
        int nowPhase = piece.phase();
        if (nowPhase != phase && phase >= 0 && !spoken) {
            if (nowPhase == WALLS_PHASE) {
                bank.play(Sfx.HEART_CLOSING, CUE_LEVEL, 1, 0);
            } else if (nowPhase == CORE_PHASE) {
                bank.play(Sfx.HEART_OPENING, CUE_LEVEL, 1, 0);
            }
        }
        boolean nowSpoken = piece.lastWords().map(LastWords::spoken).orElse(false);
        if (nowSpoken && !spoken) {
            bank.play(Sfx.HEART_DEATH, CUE_LEVEL, 1, pan(piece.renderX(1)));
        }
        for (int p = 0; p < alive.length; p++) {
            boolean lives = piece.partHp(p) > 0;
            if (alive[p] && !lives && !nowSpoken && node(mix.watched, p)) {
                bank.play(Sfx.HEART_NODE_CUT, CUE_LEVEL, pitch(0.04), pan(piece.partX(p)));
            }
            alive[p] = lives;
        }
        BossPlaybook book = piece.playbook().orElse(null);
        for (int l = 0; l < lineTold.length; l++) {
            boolean told = book != null
                    && !nowSpoken
                    && book.kind(l) == PhasePlaybook.Kind.LINE
                    && book.mode(l) == BossPlaybook.Mode.TELL;
            if (told && !lineTold[l]) {
                bank.play(Sfx.HEART_LINE, CUE_LEVEL, 1, pan(piece.renderX(1)));
            }
            lineTold[l] = told;
        }
        phase = nowPhase;
        spoken = nowSpoken;
    }

    private void remember(SetPiece piece) {
        if (alive.length != piece.partCount()) {
            alive = new boolean[piece.partCount()];
        }
        for (int p = 0; p < alive.length; p++) {
            alive[p] = piece.partHp(p) > 0;
        }
        phase = piece.phase();
        spoken = piece.lastWords().map(LastWords::spoken).orElse(false);
        java.util.Arrays.fill(lineTold, false);
    }

    /** Whether part {@code p} is a wall node (it rides the arena's walls). */
    private static boolean node(Sortie sortie, int p) {
        return sortie.arenaWalls() != null && sortie.arenaWalls().rides(p);
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case HEARTBEAT -> bank.play(Sfx.HEART_BEAT, CUE_LEVEL, 1, pan);
            case PART_WINDOW_OPEN -> {
                SetPiece piece = heart(mix.watched);
                PartWindows windows = piece == null ? null : piece.partWindows().orElse(null);
                int p = events.value(i);
                if (windows != null && p >= 0 && p < piece.partCount() && piece.partHp(p) > 0) {
                    bank.play(Sfx.HEART_MEMBRANE, CUE_LEVEL, pitch(0.05), pan(piece.partX(p)));
                }
            }
            case GATE_CLOSING -> {
                bank.play(Sfx.GATE_GROAN, CUE_LEVEL, 1, 0);
                bank.play(Sfx.GATE_GREYING, CUE_LEVEL, 1, 0);
            }
            case WARP_HOME -> bank.play(Sfx.WARP_HOME, CUE_LEVEL, 1, 0);
            case GLYPH_DARK -> bank.play(Sfx.GLYPH_DARK, glyphLevel(events.value(i)), 1, pan);
            default -> {}
        }
    }

    /** Level 50's seed vault's look (design/campaign Level 50, the secret). */
    static final String VAULT = "seed-vault";
    /** A secret found within this of where the vault stood at the last step is the vault's, px. */
    static final double VAULT_REACH = 48;
    /** Whether the vault's break has sounded in this attempt; where it stood at the last watch (NaN: not there). */
    private boolean vaultOpened;

    private double vaultX = Double.NaN;
    private double vaultY = Double.NaN;

    /** Once a step: the seed vault broken open ({@link Sfx#POD_SEED_VAULT}, S.55 a; its secret found where it stood). */
    void watch(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size() && !Double.isNaN(vaultX); i++) {
            if (events.type(i) == SimEvents.Type.SECRET_FOUND
                    && Math.hypot(events.x(i) - vaultX, events.y(i) - vaultY) <= VAULT_REACH
                    && !vaultOpened
                    && !mix.resync) {
                vaultOpened = true;
                bank.play(Sfx.POD_SEED_VAULT, CUE_LEVEL, 1, pan(vaultX));
            }
        }
        vaultX = Double.NaN;
        vaultY = Double.NaN;
        for (int g = 0; g < sortie.groundObjectCount(); g++) {
            vanguard.sim.GroundObject object = sortie.groundObject(g);
            if (object.spec().look().equals(VAULT) && !object.spent()) {
                vaultX = object.renderX(1);
                vaultY = object.renderY(1);
                vaultOpened = false;
            }
        }
    }

    /** Glyph {@code n}'s (1…) level: each quieter than the one before, down to {@value #GLYPH_FLOOR}. */
    static float glyphLevel(int n) {
        return Math.max(GLYPH_FLOOR, CUE_LEVEL - GLYPH_STEP * Math.max(0, n - 1));
    }
}
