package vanguard.content;

import java.util.Arrays;
import java.util.Locale;
import vanguard.sim.AceMove;
import vanguard.sim.AcePlaybook;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.SquadronSpec;

/**
 * M10 part F, step F2 (design/enemies/bosses/honour-guard, Duration and HP; m10f-gaps.md §1 row 18): a squadron
 * mid-boss's duel on the attempt that completes the level (Level 40's Honour Guard): its time from the bar to the last
 * ace's death, each ace's join (on the field, touchable) and death after the bar, the time with one, two and three aces
 * flying (the together time: all three), the kill order, and the conductor's most at once (told plays, firing aces,
 * tells running). The level report's {@code guard} part. Step F3 (round 78's boss question, m10-gaps.md F1a): the
 * separation pushes on the leader: while he holds his station or slides (once back in his band), the steps his centre lies outside his
 * station's band (the 80 px hull rule's solver pushing him away from the dancer; his strafe and slide stay inside it),
 * how often (each run of such steps one push) and how far at most.
 */
final class GuardWatch implements LevelMetrics.Probe {
    /**
     * A duel: {@code seconds} from the bar to the kill (NaN if it lives), {@code joined}/{@code down} per ace in s after
     * the bar (NaN: never), {@code flying} s with 0–3 aces on the field, the kill {@code order} (aces' indices), the most
     * {@code told}, {@code firing} and {@code telling} at once, the aces' call signs; the leader's {@code pushes} out of
     * his band, the {@code pushedSeconds} out of it and the {@code pushedMost} px out.
     */
    record Duel(
            long seed,
            double seconds,
            double[] joined,
            double[] down,
            double[] flying,
            int[] order,
            int told,
            int firing,
            int telling,
            String[] signs,
            int pushes,
            double pushedSeconds,
            double pushedMost) {
        double together() {
            return flying[3];
        }

        @Override
        public String toString() {
            StringBuilder aces = new StringBuilder();
            for (int a = 0; a < signs.length; a++) {
                aces.append(String.format(
                        Locale.ROOT, "%s%s in %.1f down %.1f", a == 0 ? "" : ", ", signs[a], joined[a], down[a]));
            }
            StringBuilder kills = new StringBuilder();
            for (int k = 0; k < order.length && order[k] >= 0; k++) {
                kills.append(k == 0 ? "" : " > ").append(signs[order[k]]);
            }
            return String.format(
                    Locale.ROOT,
                    "seed %d: %.1f s, together %.1f s (1 flying %.1f, 2 %.1f, 3 %.1f); %s; kills %s; most told %d,"
                            + " firing %d, telling %d; the leader pushed out of his band %d times, %.1f s, at most"
                            + " %.0f px",
                    seed,
                    seconds,
                    together(),
                    flying[1],
                    flying[2],
                    flying[3],
                    aces,
                    kills,
                    told,
                    firing,
                    telling,
                    pushes,
                    pushedSeconds,
                    pushedMost);
        }
    }

    private final long seed;
    private int start = -1;
    private int end = -1;
    private int[] joined = new int[0];
    private int[] down = new int[0];
    private final int[] flying = new int[4];
    private int[] order = new int[0];
    private int deaths;
    private int told;
    private int firing;
    private int telling;
    private String[] signs = new String[0];
    /** The leader's ace (-1 until seen), his pushes, the steps out of his band, the most px out, whether out now. */
    private int leader = -1;

    private int pushes;
    private int pushedSteps;
    private double pushedMost;
    private boolean out;
    /** Whether the leader has been inside his band since his last move other than holding or sliding. */
    private boolean settled;

    GuardWatch(long seed) {
        this.seed = seed;
    }

    @Override
    public void newAttempt() {
        start = -1;
        end = -1;
        Arrays.fill(flying, 0);
        deaths = 0;
        told = 0;
        firing = 0;
        telling = 0;
        Arrays.fill(joined, -1);
        Arrays.fill(down, -1);
        Arrays.fill(order, -1);
        pushes = 0;
        pushedSteps = 0;
        pushedMost = 0;
        out = false;
        settled = false;
    }

    @Override
    public void stepped(Sortie sortie, int step) {
        SetPiece guard = null;
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            if (sortie.setPiece(i).squadron().isPresent()) {
                guard = sortie.setPiece(i);
            }
        }
        if (guard == null || end >= 0) {
            return;
        }
        Squadron squadron = guard.squadron().orElseThrow();
        if (joined.length != squadron.aceCount()) {
            int n = squadron.aceCount();
            joined = new int[n];
            down = new int[n];
            order = new int[n];
            signs = new String[n];
            for (int a = 0; a < n; a++) {
                signs[a] = squadron.callSign(a);
                if (squadron.spec().aces().get(a).role() == SquadronSpec.Role.SNIPER) {
                    leader = a;
                }
            }
            Arrays.fill(joined, -1);
            Arrays.fill(down, -1);
            Arrays.fill(order, -1);
        }
        if (start < 0) {
            if (!guard.present()) {
                return;
            }
            start = step;
        }
        int on = 0;
        for (int a = 0; a < squadron.aceCount(); a++) {
            if (squadron.touchable(a)) {
                on++;
                if (joined[a] < 0) {
                    joined[a] = step - start;
                }
            }
            if (squadron.state(a) == Squadron.State.DOWN && down[a] < 0) {
                down[a] = step - start;
                order[deaths++] = a;
            }
        }
        flying[Math.min(3, on)]++;
        pushed(squadron);
        squadron.playbook().ifPresent(this::conductor);
        if (guard.destroyed() || deaths == squadron.aceCount()) {
            end = step;
            // The last ace's death ends the piece in the same step.
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (down[a] < 0) {
                    down[a] = step - start;
                    order[deaths++] = a;
                }
            }
        }
    }

    /**
     * The leader out of his station's band now (on the field, holding or sliding, once back inside the band since his
     * last other move: his own strafe and slide stay in it; the way back from a borrowed dive as the last ace, or from
     * his entry, starts outside it on purpose): a push.
     */
    private void pushed(Squadron squadron) {
        AceMove move = leader < 0 ? null : squadron.move(leader);
        if (leader < 0 || !squadron.touchable(leader) || (move != AceMove.HOLD && move != AceMove.SLIDE)) {
            out = false;
            settled = false;
            return;
        }
        SquadronSpec.Station station = squadron.spec().aces().get(leader).station();
        double y = squadron.y(leader);
        double beyond = Math.max(station.yMin() - y, y - station.yMax());
        if (beyond <= OUT_OF_BAND) {
            settled = true;
            out = false;
        } else if (settled) {
            pushedSteps++;
            pushedMost = Math.max(pushedMost, beyond);
            if (!out) {
                pushes++;
            }
            out = true;
        }
    }

    /** A push counts once the leader's centre is this far outside his band, px (rounding on the band's edge). */
    static final double OUT_OF_BAND = 0.5;

    private void conductor(AcePlaybook book) {
        told = Math.max(told, book.told());
        firing = Math.max(firing, book.firing());
        telling = Math.max(telling, book.telling());
    }

    /** The duel seen. */
    Duel duel() {
        double[] in = new double[joined.length];
        double[] out = new double[down.length];
        for (int a = 0; a < joined.length; a++) {
            in[a] = joined[a] < 0 ? Double.NaN : joined[a] * SimStep.SECONDS;
            out[a] = down[a] < 0 ? Double.NaN : down[a] * SimStep.SECONDS;
        }
        double[] seconds = new double[4];
        for (int k = 0; k < 4; k++) {
            seconds[k] = flying[k] * SimStep.SECONDS;
        }
        return new Duel(
                seed,
                end < 0 ? Double.NaN : (end - start) * SimStep.SECONDS,
                in,
                out,
                seconds,
                order.clone(),
                told,
                firing,
                telling,
                signs.clone(),
                pushes,
                pushedSteps * SimStep.SECONDS,
                pushedMost);
    }

    /** Flies {@code key} with the autopilot and {@code loadout} on {@code seed} and returns its duel. */
    static Duel fly(Content content, String key, vanguard.sim.Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = LevelMetrics.sortie(content, key, difficulty, loadout, seed);
        GuardWatch watch = new GuardWatch(seed);
        LevelMetrics.fly(sortie, LevelReport.AUTOPILOT_STEPS, watch);
        return watch.duel();
    }

    /** Whether the level has a squadron mid-boss. */
    static boolean hasGuard(vanguard.sim.LevelScript level) {
        return level.setPieces().stream()
                .anyMatch(piece ->
                        piece.boss().flatMap(vanguard.sim.BossSpec::squadron).isPresent());
    }
}
