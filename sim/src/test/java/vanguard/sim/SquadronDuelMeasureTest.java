package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1b (m10f-gaps.md §5, the riskiest item): the duel's length and the three aces' time together,
 * measured on the fixtures with the paper's effective fire (0.6 × 160 × 0.7 ≈ 67 a second, design/enemies/bosses/
 * honour-guard, Duration and HP) over five seeds and the three difficulties, the playbook and conductor flying: the
 * fire spread over the aces on the field (the paper's), or all on the one nearest the ship. It prints the numbers for
 * the report and step F2's tuning (the window 45–75 s and ≥ 10 s together are Level 40's data, measured again with the
 * autopilot by F3); it asserts only that every duel ends.
 */
class SquadronDuelMeasureTest {
    private static final double DPS = 0.6 * 160 * 0.7;

    /** One duel: its seconds from the sting, the seconds all three flew together, the order they died in. */
    record Duel(double seconds, double together, String order) {}

    static Duel fly(SetPieceSpec spec, long seed, boolean nearest) {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, seed);
        Squadron squadron = rig.squadron;
        int together = 0;
        StringBuilder order = new StringBuilder();
        double per = DPS * SimStep.SECONDS;
        for (int i = 0; i < SimStep.ticks(240); i++) {
            rig.step();
            for (int a : rig.values(SimEvents.Type.ACE_DOWN)) {
                order.append(squadron.callSign(a).charAt(0));
            }
            if (squadron.deaths() == squadron.aceCount()) {
                return new Duel(rig.piece.bossSeconds(), together * SimStep.SECONDS, order.toString());
            }
            int on = 0;
            int near = -1;
            double best = Double.MAX_VALUE;
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (squadron.touchable(a)) {
                    on++;
                    double d = Math.hypot(squadron.x(a) - rig.shipX, squadron.y(a) - rig.shipY);
                    if (d < best) {
                        best = d;
                        near = a;
                    }
                }
            }
            together += on == squadron.aceCount() ? 1 : 0;
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (squadron.touchable(a) && (!nearest || a == near)) {
                    rig.piece.damagePartFlat(squadron.part(a), nearest ? per : per / on);
                }
            }
        }
        return new Duel(Double.NaN, together * SimStep.SECONDS, order.toString());
    }

    @Test
    void theDuelsLengthAndTheTimeTogetherOverSeedsAndDifficulties() {
        String[] names = {"easy", "medium", "hard"};
        SetPieceSpec[] specs = {Act6FSpecs.PLAYED_EASY, Act6FSpecs.PLAYED, Act6FSpecs.PLAYED_HARD};
        StringBuilder out = new StringBuilder("Honour Guard duel (fixture, " + Math.round(DPS) + " dps)\n");
        for (int fire = 0; fire < 2; fire++) {
            for (int d = 0; d < specs.length; d++) {
                out.append(String.format(Locale.ROOT, "%-7s %-6s", fire == 0 ? "spread" : "nearest", names[d]));
                double sum = 0;
                for (long seed = 1; seed <= 5; seed++) {
                    Duel duel = fly(specs[d], seed, fire == 1);
                    assertTrue(!Double.isNaN(duel.seconds()), "the duel ends: " + names[d] + " " + seed);
                    sum += duel.seconds();
                    out.append(String.format(
                            Locale.ROOT,
                            " | %5.1f s, %4.1f s together, %s",
                            duel.seconds(),
                            duel.together(),
                            duel.order()));
                }
                out.append(String.format(Locale.ROOT, " | mean %.1f s%n", sum / 5));
            }
        }
        System.out.println(out);
    }
}
