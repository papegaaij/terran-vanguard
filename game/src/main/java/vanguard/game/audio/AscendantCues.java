package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.BossPlaybook;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.PartRegens;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part H, step H4b: the Ascendant's sounds for {@link FlightSounds} (design/enemies/bosses/ascendant, Sounds brief;
 * design/audio/sfx, the Ascendant's row; round 80's a variants until the user's picks, levelled in their files and
 * played as they are):
 *
 * <ul>
 *   <li>the spinal lance's charge ({@link Sfx#ASCENDANT_LANCE_CHARGE}) from the bow cannon's tell of a line move, timed
 *       so its 2 s end at the shot (easy's longer red line starts it later, hard's shorter one is cut by the shot:
 *       {@link SovereignCues}' boss lines play the shot);
 *   <li>the brood ({@link Sfx#ASCENDANT_BROOD}) as a sac splits (its move's run);
 *   <li>a sac healing ({@link Sfx#ASCENDANT_SAC_HEAL}), again every {@value #SAC_HEAL_SECONDS} s while any part heals,
 *       panned to the nearest healing part ({@link #watch});
 *   <li>the panels' mirrors breaking, shattering and regrowing (the Mirror Interceptor's {@link Sfx#MIRROR_BREAK} and
 *       {@link Sfx#MIRROR_REGROW});
 *   <li>the hull breaking ({@link Sfx#ASCENDANT_HULL_BREAK}) from K, its deep boom on the flash, and the {@code huge}
 *       rung at the flash (K + 2.5 s: the level's only huge moment); the lifeboat tearing free and climbing away
 *       ({@link Sfx#ASCENDANT_LIFEBOAT}).
 * </ul>
 *
 * The arrival, the eruption and a generator's death are {@link BossCues}' (the boss's own arrival, phase and part
 * sounds); a panel's bounce is {@link JovianCues}' (the mirrors' ping); the escape sting is the music's ({@link
 * LevelMusic#bossEscaped}).
 */
final class AscendantCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.BOSS_PLAY_TELL,
            SimEvents.Type.BOSS_PLAY,
            SimEvents.Type.PART_MIRROR_BROKEN,
            SimEvents.Type.PART_MIRROR_SHATTERED,
            SimEvents.Type.PART_MIRROR_REGROWING,
            SimEvents.Type.HULL_BROKEN,
            SimEvents.Type.HULL_FLASH,
            SimEvents.Type.LIFEBOAT_AWAY);

    /** The cues are levelled in their files (step AUD-H) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The lance charge's file is this long: it starts so it ends at the shot. */
    static final double LANCE_CHARGE_SECONDS = 2.0;
    /** The sac heal's file is this long: it starts again this often while a part heals. */
    static final double SAC_HEAL_SECONDS = 2.4;

    private static final int SAC_HEAL_STEPS = SimStep.ticks(SAC_HEAL_SECONDS);

    private long lastHeal = Long.MIN_VALUE / 2;

    AscendantCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The escaping boss of a sortie (Level 42's Ascendant), or null. */
    static SetPiece ascendant(Sortie sortie) {
        if (sortie == null) {
            return null;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (!piece.held() && piece.escape().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case BOSS_PLAY_TELL -> {
                SetPiece piece = ascendant(mix.watched);
                if (piece != null && kind(piece) == PhasePlaybook.Kind.LINE) {
                    later(Sfx.ASCENDANT_LANCE_CHARGE, CUE_LEVEL, 1, pan, lanceChargeDelay(piece));
                }
            }
            case BOSS_PLAY -> {
                SetPiece piece = ascendant(mix.watched);
                Sfx own = piece == null ? null : playSound(kind(piece));
                if (own != null) {
                    bank.play(own, CUE_LEVEL, pitch(0.03), pan);
                }
            }
            case PART_MIRROR_BROKEN, PART_MIRROR_SHATTERED ->
                bank.play(Sfx.MIRROR_BREAK, CUE_LEVEL, 0.85f * pitch(0.03), pan);
            case PART_MIRROR_REGROWING -> bank.play(Sfx.MIRROR_REGROW, CUE_LEVEL, 0.85f * pitch(0.02), pan);
            case HULL_BROKEN -> bank.play(Sfx.ASCENDANT_HULL_BREAK, CUE_LEVEL, 1, pan);
            // The act boss's huge rung at the flash, over the hull break's deep boom 2.5 s into its file.
            case HULL_FLASH -> bank.play(Sfx.EXPLOSION_HUGE_A, CUE_LEVEL, 1, pan);
            case LIFEBOAT_AWAY -> bank.play(Sfx.ASCENDANT_LIFEBOAT, CUE_LEVEL, 1, pan);
            default -> {}
        }
    }

    /**
     * The Ascendant's own sound as a move of {@code kind} runs: the brood's (a sac splitting, its Chimeras flying out);
     * null for the others (the borrowed attacks keep their own sounds; the lance's charge runs from its tell).
     */
    static Sfx playSound(PhasePlaybook.Kind kind) {
        return kind == PhasePlaybook.Kind.BROOD ? Sfx.ASCENDANT_BROOD : null;
    }

    /** The kind of the move the piece's playbook tells or runs now; null without one. */
    private static PhasePlaybook.Kind kind(SetPiece piece) {
        return piece.playbook().map(BossPlaybook::kind).orElse(null);
    }

    /**
     * The steps from a line move's tell to the lance charge's start, so its {@value #LANCE_CHARGE_SECONDS} s end at the
     * shot: the tell plus the line's red telegraph, less the file (0 when shorter: the shot cuts it).
     */
    static int lanceChargeDelay(double tellSeconds, double telegraphSeconds) {
        return Math.max(0, SimStep.ticks(tellSeconds + telegraphSeconds - LANCE_CHARGE_SECONDS));
    }

    private static int lanceChargeDelay(SetPiece piece) {
        BossSpec boss = piece.boss().orElse(null);
        BossPlaybook book = piece.playbook().orElse(null);
        if (boss == null || book == null || book.move() < 0) {
            return 0;
        }
        for (PhasePlaybook phase : boss.playbooks()) {
            if (phase.phase() != piece.phase() || book.move() >= phase.moves().size()) {
                continue;
            }
            int attack = phase.moves().get(book.move()).attack();
            double telegraph = attack < 0 || attack >= boss.attacks().size()
                    ? 0
                    : boss.attacks()
                            .get(attack)
                            .line()
                            .map(EnemySpec.LaserLine::telegraphSeconds)
                            .orElse(0.0);
            return lanceChargeDelay(phase.tellSeconds(), telegraph);
        }
        return 0;
    }

    /** Once a step: a sac's healing creak again every {@value #SAC_HEAL_SECONDS} s while a part heals. */
    void watch(Sortie sortie) {
        SetPiece piece = ascendant(sortie);
        if (piece == null || piece.regens().isEmpty() || mix.resync) {
            return;
        }
        long tick = sortie.tick();
        if (tick < lastHeal) {
            // A retry from the boss checkpoint.
            lastHeal = Long.MIN_VALUE / 2;
        }
        if (!sortie.flying() || sortie.complete() || tick - lastHeal < SAC_HEAL_STEPS) {
            return;
        }
        PartRegens regens = piece.regens().get();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int p = 0; p < piece.partCount(); p++) {
            if (regens.fitted(p) && regens.healing(p)) {
                double d = Math.hypot(piece.partX(p) - shipX, piece.partY(p) - shipY);
                if (d < best) {
                    best = d;
                    nearest = p;
                }
            }
        }
        if (nearest >= 0) {
            lastHeal = tick;
            bank.play(Sfx.ASCENDANT_SAC_HEAL, CUE_LEVEL, pitch(0.03), pan(piece.partX(nearest)));
        }
    }

    /** The step of the last sac heal, for the tests. */
    long lastHeal() {
        return lastHeal;
    }
}
