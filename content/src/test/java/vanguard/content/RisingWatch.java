package vanguard.content;

import java.util.List;
import java.util.Locale;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part F, step DB1 (design/campaign Level 40, the two Talon V-wings from the rear; F1c's rising frame): the units of a
 * front formation rising from the bottom edge ({@code from: rear} on a V-wing or a swooping line abreast), over every
 * attempt of the autopilot's run: the seconds one is on the screen, the hits on the ship meanwhile (shield or armour),
 * their shots (those leaving from below the ship's centre: from behind it) and the lowest a shot left from, the highest
 * a riser climbed (its mirrored swoop's turn) and the closest a riser's centre came to the ship's (px; heights above the
 * bottom edge). The level report's {@code rising} part.
 */
final class RisingWatch implements LevelMetrics.Probe {
    /**
     * A run: {@code seconds} with a riser on the screen, the ship's {@code hits} meanwhile, the risers' {@code shots}, the
     * {@code behind} of them from below the ship's centre, the {@code lowestShot} px above the bottom edge one left from,
     * the {@code highest} a riser climbed, the {@code closest} px between a riser's centre and the ship's.
     */
    record Rise(
            long seed,
            double seconds,
            int hits,
            int shots,
            int behind,
            double lowestShot,
            double highest,
            double closest) {
        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "seed %d: %.1f s on the screen, %d hits on the ship, %d shots (%d from behind it, the lowest from"
                            + " %.0f px), highest %.0f px, closest %.0f px",
                    seed,
                    seconds,
                    hits,
                    shots,
                    behind,
                    lowestShot,
                    highest,
                    closest);
        }
    }

    private final long seed;
    private final boolean[] rising;
    private int steps;
    private int hits;
    private int shots;
    private int behind;
    private double highest = Double.NaN;
    private double lowestShot = Double.NaN;
    private double closest = Double.POSITIVE_INFINITY;

    private RisingWatch(long seed, LevelScript level) {
        this.seed = seed;
        List<WaveSpec> waves = level.waves();
        rising = new boolean[waves.size()];
        for (int w = 0; w < waves.size(); w++) {
            rising[w] = rises(waves.get(w));
        }
    }

    @Override
    public void stepped(Sortie sortie, int step) {
        boolean any = false;
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        SimEvents events = sortie.events();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int wave = enemy.wave();
            if (wave < 0 || wave >= rising.length || !rising[wave]) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (!LevelMetrics.onScreen(x, y, enemy.spec().hitbox())) {
                continue;
            }
            any = true;
            highest = Double.isNaN(highest) ? y : Math.max(highest, y);
            closest = Math.min(closest, Math.hypot(x - shipX, y - shipY));
            double halfW = enemy.spec().hitbox().width() / 2;
            double halfH = enemy.spec().hitbox().height() / 2;
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_FIRED
                        && Math.abs(events.x(e) - x) <= halfW
                        && Math.abs(events.y(e) - y) <= halfH) {
                    shots++;
                    behind += events.y(e) < shipY ? 1 : 0;
                    lowestShot = Double.isNaN(lowestShot) ? events.y(e) : Math.min(lowestShot, events.y(e));
                }
            }
        }
        if (!any) {
            return;
        }
        steps++;
        hits += events.count(SimEvents.Type.SHIELD_HIT) + events.count(SimEvents.Type.ARMOUR_HIT);
    }

    Rise rise() {
        return new Rise(seed, steps * SimStep.SECONDS, hits, shots, behind, lowestShot, highest, closest);
    }

    /** F1c's rule (the simulation's {@code WaveSchedule.rises}): a front formation that rises from the bottom edge. */
    static boolean rises(WaveSpec wave) {
        return wave.entry() == WaveSpec.Entry.REAR
                && (wave.formation() == WaveSpec.Formation.V_WING
                        || (wave.formation() == WaveSpec.Formation.LINE_ABREAST
                                && wave.enemy().swoopPath().isPresent()));
    }

    /** Whether the level flies a rising front formation. */
    static boolean hasRising(LevelScript level) {
        return level.waves().stream().anyMatch(RisingWatch::rises);
    }

    /** Flies {@code key} with the autopilot and {@code loadout} on {@code seed} and returns its risers' numbers. */
    static Rise fly(Content content, String key, vanguard.sim.Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = LevelMetrics.sortie(content, key, difficulty, loadout, seed);
        RisingWatch watch = new RisingWatch(seed, SimSpecs.level(content, key, difficulty));
        LevelMetrics.fly(sortie, LevelReport.AUTOPILOT_STEPS, watch);
        return watch.rise();
    }
}
