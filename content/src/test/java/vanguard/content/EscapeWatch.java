package vanguard.content;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import vanguard.sim.BossSpec;
import vanguard.sim.EscapeRun;
import vanguard.sim.PartRegen;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part H, step H2 (design/enemies/bosses/ascendant, Duration and HP; m10h-gaps.md §1 row 22): an escaping boss's
 * fight on the attempt that completes the level (or the last one), Level 42's Ascendant: each phase's start after the
 * bar, the hull's break (K) and the escape (E), the mirror downs (a panel broken or shattered), the heals (a
 * regenerating part healed back to full after it fell below 90 %). The level report's {@code escape} part.
 */
final class EscapeWatch implements LevelMetrics.Probe {
    /** A part healed back to this share of its HP after falling below {@link #HURT} counts one heal. */
    static final double HEALED = 0.999;

    static final double HURT = 0.9;

    /**
     * A fight: {@code phases} each phase's start in s after the bar (NaN: never), {@code broken} K and {@code escaped} E
     * in s after the bar (NaN: never), the {@code mirrorDowns}, the {@code heals}; {@code unharmed}: flown with the ship
     * invulnerable.
     */
    record Fight(
            long seed, boolean unharmed, double[] phases, double broken, double escaped, int mirrorDowns, int heals) {
        @Override
        public String toString() {
            StringBuilder starts = new StringBuilder();
            for (int f = 1; f < phases.length; f++) {
                starts.append(String.format(Locale.ROOT, "%sphase %d at %.1f", f == 1 ? "" : ", ", f + 1, phases[f]));
            }
            return String.format(
                    Locale.ROOT,
                    "seed %d%s: %s; K at %.1f, ESCAPED at %.1f s; %d mirror downs, %d heals",
                    seed,
                    unharmed ? " unharmed" : "",
                    starts,
                    broken,
                    escaped,
                    mirrorDowns,
                    heals);
        }
    }

    private final long seed;
    private final boolean unharmed;
    private double[] phases = new double[0];
    private double broken = Double.NaN;
    private double escaped = Double.NaN;
    private int mirrorDowns;
    private int heals;
    private boolean[] hurt = new boolean[0];

    EscapeWatch(long seed, boolean unharmed) {
        this.seed = seed;
        this.unharmed = unharmed;
    }

    @Override
    public void newAttempt() {
        Arrays.fill(phases, Double.NaN);
        broken = Double.NaN;
        escaped = Double.NaN;
        mirrorDowns = 0;
        heals = 0;
        Arrays.fill(hurt, false);
    }

    @Override
    public void stepped(Sortie sortie, int step) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.escape().isEmpty() || piece.bossSeconds() < 0) {
                continue;
            }
            BossSpec boss = piece.boss().orElseThrow();
            if (phases.length != boss.phases().size()) {
                phases = new double[boss.phases().size()];
                Arrays.fill(phases, Double.NaN);
                hurt = new boolean[piece.spec().parts().size()];
            }
            double at = piece.bossSeconds();
            if (Double.isNaN(phases[piece.phase()])) {
                phases[piece.phase()] = at;
            }
            List<PartRegen> regrowing = boss.regeneration();
            for (PartRegen regen : regrowing) {
                int p = regen.part();
                double max = piece.spec().parts().get(p).hp();
                double hp = piece.partHp(p);
                if (hp <= 0) {
                    hurt[p] = false;
                } else if (hp < HURT * max) {
                    hurt[p] = true;
                } else if (hurt[p] && hp >= HEALED * max) {
                    hurt[p] = false;
                    heals++;
                }
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                SimEvents.Type type = events.type(e);
                if (type == SimEvents.Type.PART_MIRROR_BROKEN || type == SimEvents.Type.PART_MIRROR_SHATTERED) {
                    mirrorDowns++;
                }
            }
            EscapeRun run = piece.escape().get();
            if (run.broken() && Double.isNaN(broken)) {
                broken = at;
            }
            if (run.escaped() && Double.isNaN(escaped)) {
                escaped = at;
            }
        }
    }

    Fight fight() {
        return new Fight(seed, unharmed, phases.clone(), broken, escaped, mirrorDowns, heals);
    }

    /** Flies {@code key} with the autopilot and {@code loadout} on {@code seed} and returns its escaping boss's fight. */
    static Fight fly(Content content, String key, vanguard.sim.Loadout loadout, Difficulty difficulty, long seed) {
        return fly(content, key, loadout, difficulty, seed, false);
    }

    /**
     * As {@link #fly(Content, String, vanguard.sim.Loadout, Difficulty, long)}; {@code unharmed}: the ship invulnerable
     * (nothing hits it, a lost battery fails nothing), the fight's shape measured whatever the autopilot's losses.
     */
    static Fight fly(
            Content content,
            String key,
            vanguard.sim.Loadout loadout,
            Difficulty difficulty,
            long seed,
            boolean unharmed) {
        Sortie sortie = unharmed
                ? new Sortie(
                        seed,
                        loadout,
                        SimSpecs.level(content, key, difficulty),
                        SimSpecs.rules(content, key, difficulty).withInvulnerableShip(),
                        loadout.plating().maxArmour())
                : LevelMetrics.sortie(content, key, difficulty, loadout, seed);
        EscapeWatch watch = new EscapeWatch(seed, unharmed);
        LevelMetrics.fly(sortie, LevelReport.AUTOPILOT_STEPS, watch);
        return watch.fight();
    }

    /** Whether the level has an escaping boss. */
    static boolean hasEscape(vanguard.sim.LevelScript level) {
        return level.setPieces().stream()
                .anyMatch(piece -> piece.boss().flatMap(BossSpec::escape).isPresent());
    }
}
