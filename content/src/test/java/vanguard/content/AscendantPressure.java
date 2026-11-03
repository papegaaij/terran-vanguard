package vanguard.content;

import java.util.Locale;
import java.util.function.ToIntFunction;
import vanguard.sim.BossLines;
import vanguard.sim.BossPlaybook;
import vanguard.sim.Command;
import vanguard.sim.EnemyBullet;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 round 81 fixes, R81a (design/enemies/bosses/ascendant, Phases and Duration and HP; the user's round 81 B.1: "still
 * too many quiet periods; you don't even need to dodge the bullets in the first part, because the shield regenerates
 * before the next round"): the Ascendant's phase-1 pressure as two measures.
 *
 * <ul>
 *   <li>{@link Quiet}: from phase 1's first shot to its end, the longest stretch with nothing of the boss's that can
 *       still reach the ship: no enemy bullet (not a bounced player shot) still closing on it, no homing missile, no
 *       boss-launched unit, no live lance beam. The stretch from the engagement to the first shot (the hull settling,
 *       Vorne's line) is reported apart. It also notes Rook at phase 1's end.
 *   <li>{@link #standStill}: a ship that never moves (its front gun firing) at its start place from the engagement on:
 *       its armour lost in phase 1 (the incoming damage beats the shield's regeneration between attacks).
 * </ul>
 */
final class AscendantPressure {
    private AscendantPressure() {}

    /** The longest quiet stretch phase 1 may hold, s (the round 81 rule). */
    static final double MOST_QUIET = 1.0;

    /** The longest stretch in phase 1 with nothing of the boss's that can reach the ship, on the completing attempt. */
    static final class Quiet implements LevelMetrics.Probe {
        private int run;
        private int longest;
        private double longestAt = Double.NaN;
        private int over;
        /** Whether phase 1 has fired its first shot (the stretch before it is the hull settling). */
        private boolean opened;
        /** Steps from the engagement to the first shot. */
        private int opening;
        /** What the playbook did as each stretch passed {@link #MOST_QUIET} (s after the bar, its mode and move). */
        private final StringBuilder notes = new StringBuilder();
        /** Rook at phase 1's end (null before). */
        private String rook;

        @Override
        public void newAttempt() {
            run = 0;
            longest = 0;
            longestAt = Double.NaN;
            over = 0;
            opened = false;
            opening = 0;
            notes.setLength(0);
            rook = null;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SetPiece piece = AscendantPlay.escapingBoss(sortie);
            if (piece != null && piece.phase() > 0 && rook == null) {
                rook = sortie.wingman()
                        .map(w -> w.ejected() ? "ejected" : String.format(Locale.ROOT, "armour %.0f", w.armour()))
                        .orElse("none");
            }
            if (piece == null || !piece.engaged() || piece.phase() != 0) {
                run = 0;
                return;
            }
            if (threat(sortie, piece)) {
                opened = true;
                run = 0;
                return;
            }
            if (!opened) {
                opening++;
                return;
            }
            run++;
            if (run == SimStep.ticks(MOST_QUIET) + 1) {
                over++;
                BossPlaybook book = piece.playbook().orElseThrow();
                notes.append(
                        String.format(Locale.ROOT, " {%.1f s %s %s}", piece.bossSeconds(), book.mode(), book.kind()));
            }
            if (run > longest) {
                longest = run;
                longestAt = piece.bossSeconds();
            }
        }

        /** The longest quiet stretch, s. */
        double longest() {
            return longest * SimStep.SECONDS;
        }

        /** Whether Rook came through phase 1 flying. */
        boolean rookThrough() {
            return rook != null && rook.startsWith("armour");
        }

        @Override
        public String toString() {
            return String.format(
                            Locale.ROOT,
                            "phase 1 quiet at most %.2f s (ending %.1f s after the bar), %d over %.1f s, its first shot"
                                    + " %.2f s after the engagement, Rook after it: %s",
                            longest(),
                            longestAt,
                            over,
                            MOST_QUIET,
                            opening * SimStep.SECONDS,
                            rook)
                    + notes;
        }
    }

    /** Whether anything of the boss's can still reach the ship this step. */
    static boolean threat(Sortie sortie, SetPiece piece) {
        double sx = sortie.ship().x();
        double sy = sortie.ship().y();
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (bullet.bounced()) {
                continue;
            }
            // closing on the ship: its flight (heading 0 = up, clockwise) has a part toward it
            double h = bullet.heading();
            double dx = sx - bullet.renderX(1);
            double dy = sy - bullet.renderY(1);
            if (dx * StrictMath.sin(h) + dy * StrictMath.cos(h) > 0) {
                return true;
            }
        }
        if (sortie.missileCount() > 0 || sortie.enemyCount() > 0) {
            return true;
        }
        BossLines lines = piece.lines().orElse(null);
        return lines != null && (lines.live() || lines.twinLive());
    }

    /** What a ship that stood still lost. */
    record Still(long seed, double armour, double shield, double seconds, boolean lost) {
        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "seed %d: stood still %.1f s of phase 1, lost armour %.0f, shield %.0f%s",
                    seed,
                    seconds,
                    armour,
                    shield,
                    lost ? " (shot down)" : "");
        }
    }

    /** It stands still in phase 1 for at most this long, s (about phase 1's measured time). */
    static final double STILL_SECONDS = 75;

    /**
     * The autopilot flies the approach (retried after a loss) until the hull engages; then the ship flies back to its
     * start place and stands there, its front gun firing, for at most {@link #STILL_SECONDS} of phase 1: the armour
     * and shield it loses.
     */
    static Still standStill(Content content, long seed, Difficulty difficulty) {
        Sortie sortie =
                Level42Test.sortie(content, Level42Test.planLoadout(content, difficulty), seed, difficulty, false);
        double homeX = sortie.ship().x();
        double homeY = sortie.ship().y();
        int steps = 0;
        SetPiece boss = null;
        while (!sortie.complete() && steps++ < Level42Test.MAX_STEPS) {
            Level04Test.step(sortie);
            boss = AscendantPlay.escapingBoss(sortie);
            if (boss != null && boss.engaged()) {
                break;
            }
        }
        if (boss == null || !boss.engaged()) {
            throw new AssertionError("the hull engages, seed " + seed);
        }
        ToIntFunction<Sortie> home = s -> {
            double dx = homeX - s.ship().x();
            double dy = homeY - s.ship().y();
            int c = Command.FIRE.bit();
            c |= dx > 2 ? Command.RIGHT.bit() : dx < -2 ? Command.LEFT.bit() : 0;
            c |= dy > 2 ? Command.UP.bit() : dy < -2 ? Command.DOWN.bit() : 0;
            return c;
        };
        double armour = 0;
        double shield = 0;
        int ticks = 0;
        for (int i = 0; i < SimStep.ticks(STILL_SECONDS) && boss.phase() == 0 && sortie.flying(); i++) {
            double a = sortie.ship().defences().armour();
            double s = sortie.ship().defences().shield();
            sortie.step(home.applyAsInt(sortie));
            ticks++;
            armour += Math.max(0, a - sortie.ship().defences().armour());
            shield += Math.max(0, s - sortie.ship().defences().shield());
        }
        return new Still(seed, armour, shield, ticks * SimStep.SECONDS, !sortie.flying());
    }
}
