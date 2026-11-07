package vanguard.game.render;

import vanguard.sim.LastWords;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4 (design/enemies/bosses/vornes-chimera, Death: "its bullets fade over 0.5 s"; {@link LastWords}):
 * from a boss's last words every enemy projectile on the screen is harmless and fades out ({@code fading()}: the bullets,
 * the homing diamonds, the lobs and their markers); each is drawn at {@link #alpha} while it fades. Nothing is allocated.
 */
final class LastWordsFade {
    private LastWordsFade() {}

    /** The fading projectiles' opacity now: the speaking boss's {@link LastWords#bulletAlpha()}; 1 without one. */
    static float alpha(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            LastWords words = sortie.setPiece(k).lastWords().orElse(null);
            if (words != null && words.spoken()) {
                return (float) words.bulletAlpha();
            }
        }
        return 1;
    }
}
