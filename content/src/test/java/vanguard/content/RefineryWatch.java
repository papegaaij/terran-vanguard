package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.Eruptions;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part E, step E2 (design/campaign Level 39, Layout: the holds; Hazards: the eruptions; Waves: the limits): the
 * level report's {@code refinery} part, measured with an invulnerable pilot (without Rook, whose Mortar would take the
 * towers early), so the script runs as authored. First a
 * flight that never fires: each hold's window (from its start to its first tower leaving the screen alive). Then a
 * flight that fires only in a hold and only at the lowest cracking tower once it is low ({@link #FIRE_BELOW}), so each
 * hold lasts about its last tower's window: each hold's length, the vents on the field in it
 * and their tells (an eruption each), and the most Striders walking, Sentinels sweeping, SAM Nests and Harrows on the
 * screen at once (nothing dies outside the holds: the caps' worst case).
 */
final class RefineryWatch {
    /** px above the bottom edge: the pilot opens fire on the lowest tower once its centre is this low. */
    static final double FIRE_BELOW = 170;

    /** Steps a flight may take at most. */
    static final int MAX_STEPS = 60 * 60 * 6;

    /** The pilot's height above the bottom edge, px. */
    static final double PILOT_Y = 60;

    private RefineryWatch() {}

    /** One hold: its start (level s), how long it held (real s), and the tells of each vent on the field in it. */
    record Hold(double levelSeconds, double seconds, List<Integer> tellsPerVent) {
        /** The fewest tells of a vent on the field in the hold; 0 without vents. */
        int fewest() {
            return tellsPerVent.stream().mapToInt(Integer::intValue).min().orElse(0);
        }
    }

    /**
     * The measures: each hold's window, real s (from its start to its first tower leaving the screen alive, without a
     * shot); the holds the pilot flew; the towers that passed it alive (the invulnerable rules fail no mission); the most
     * at once of the capped units.
     */
    record Refinery(
            LevelMetrics.Flight autopilot,
            List<Double> windows,
            List<Hold> holds,
            int passed,
            int striders,
            int sentinels,
            int sams,
            int harrows,
            int bigHullOverlaps) {}

    /** Whether the level has holds over destroy-targets groups (Level 39's works, Level 09's clusters). */
    static boolean hasHolds(LevelScript level) {
        return !level.objectives().holds().isEmpty();
    }

    static Refinery measure(Content content, String key, Difficulty difficulty, Loadout loadout) {
        List<Double> windows = windows(content, key, difficulty, loadout);
        Sortie sortie = sortie(content, key, difficulty, loadout);
        Towers towers = new Towers();
        int passed = 0;
        LevelMetrics.Stacking stacking = new LevelMetrics.Stacking(content);
        List<Hold> holds = new ArrayList<>();
        double holdStart = -1;
        double holdLevel = 0;
        int[] tells = new int[0];
        boolean[] present = new boolean[0];
        int striders = 0;
        int sentinels = 0;
        int sams = 0;
        int harrows = 0;
        for (int step = 1; step <= MAX_STEPS && !sortie.complete(); step++) {
            sortie.step(pilot(sortie, holdStart >= 0));
            stacking.stepped(sortie, step);
            passed += towers.left(sortie);
            SimEvents events = sortie.events();
            Eruptions eruptions = sortie.eruptions().orElse(null);
            for (int i = 0; i < events.size(); i++) {
                switch (events.type(i)) {
                    case HOLD_START -> {
                        holdStart = sortie.realSeconds();
                        holdLevel = sortie.levelSeconds();
                        int vents = eruptions == null ? 0 : eruptions.vents();
                        tells = new int[vents];
                        present = new boolean[vents];
                    }
                    case HOLD_END -> {
                        if (holdStart >= 0) {
                            holds.add(new Hold(holdLevel, sortie.realSeconds() - holdStart, counted(tells, present)));
                            holdStart = -1;
                        }
                    }
                    case ERUPTION_TELL -> {
                        if (holdStart >= 0 && events.value(i) < tells.length) {
                            tells[events.value(i)]++;
                        }
                    }
                    default -> {}
                }
            }
            if (holdStart >= 0 && eruptions != null) {
                for (int v = 0; v < present.length; v++) {
                    present[v] |= eruptions.ventPresent(v);
                }
            }
            int walking = 0;
            int sweeping = 0;
            int nests = 0;
            int dashers = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!LevelMetrics.onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                    continue;
                }
                switch (enemy.spec().slug()) {
                    case "strider" -> walking += enemy.walking() ? 1 : 0;
                    case "sentinel-tower" -> sweeping += enemy.sweeping() ? 1 : 0;
                    case "sam-nest" -> nests += enemy.dormant() ? 0 : 1;
                    case "harrow" -> dashers++;
                    default -> {}
                }
            }
            striders = Math.max(striders, walking);
            sentinels = Math.max(sentinels, sweeping);
            sams = Math.max(sams, nests);
            harrows = Math.max(harrows, dashers);
        }
        if (holdStart >= 0) {
            holds.add(new Hold(holdLevel, sortie.realSeconds() - holdStart, counted(tells, present)));
        }
        return new Refinery(
                autopilot(content, key, difficulty, loadout),
                windows,
                holds,
                passed,
                striders,
                sentinels,
                sams,
                harrows,
                stacking.stacks().size());
    }

    /**
     * The plain autopilot ({@link Level04Test#step}) invulnerable, a tower passing failing nothing: the pacing probe's
     * view of a run through the holds (the floor's on-screen mean and shots a minute, the gaps, the end calm) until the
     * refinery autopilot (step E3) wins the level.
     */
    private static LevelMetrics.Flight autopilot(Content content, String key, Difficulty difficulty, Loadout loadout) {
        Sortie sortie = sortie(content, key, difficulty, loadout);
        LevelMetrics.Pacing pacing = new LevelMetrics.Pacing();
        LevelMetrics.fly(sortie, MAX_STEPS, pacing);
        return pacing.flight();
    }

    /** Each hold's window: from its start to its first tower leaving the screen alive, flying without a shot. */
    private static List<Double> windows(Content content, String key, Difficulty difficulty, Loadout loadout) {
        Sortie sortie = sortie(content, key, difficulty, loadout);
        Towers towers = new Towers();
        List<Double> windows = new ArrayList<>();
        double start = -1;
        for (int step = 1; step <= MAX_STEPS && !sortie.complete(); step++) {
            sortie.step(pilot(sortie, false));
            if (sortie.events().count(SimEvents.Type.HOLD_START) > 0) {
                start = sortie.realSeconds();
            }
            if (towers.left(sortie) > 0 && start >= 0) {
                windows.add(sortie.realSeconds() - start);
                start = -1;
            }
        }
        return windows;
    }

    /** The cracking towers on the screen, by serial: their last height, to tell a tower that left from one destroyed. */
    private static final class Towers {
        private final java.util.Map<Integer, Double> last = new java.util.HashMap<>();
        private final java.util.Set<Integer> seen = new java.util.HashSet<>();

        /** The towers that left the screen alive since the last step (gone at the bottom edge, not destroyed). */
        int left(Sortie sortie) {
            seen.clear();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().slug().equals("cracking-tower")) {
                    seen.add(enemy.serial());
                    last.put(enemy.serial(), enemy.renderY(1));
                }
            }
            int gone = 0;
            for (var it = last.entrySet().iterator(); it.hasNext(); ) {
                var tower = it.next();
                if (!seen.contains(tower.getKey())) {
                    gone += tower.getValue() < BOTTOM_EDGE ? 1 : 0;
                    it.remove();
                }
            }
            return gone;
        }
    }

    /** px: a tower gone with its centre this low left at the bottom edge (it was not destroyed). */
    static final double BOTTOM_EDGE = 0;

    private static Sortie sortie(Content content, String key, Difficulty difficulty, Loadout loadout) {
        return new Sortie(
                LevelMetrics.SEED,
                loadout,
                SimSpecs.level(content, key, difficulty),
                SimSpecs.rules(content, key, difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    /**
     * Low on the screen: in a hold under the lowest cracking tower, firing once it is {@link #FIRE_BELOW} px from the
     * bottom edge (each tower falls shortly before it would leave: the hold lasts about its last tower's window); else
     * the middle, silent.
     */
    private static int pilot(Sortie sortie, boolean firing) {
        double x = PlayField.WIDTH / 2.0;
        double lowest = Double.MAX_VALUE;
        for (int i = 0; firing && i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals("cracking-tower")
                    && LevelMetrics.onScreen(
                            enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())
                    && enemy.renderY(1) < lowest) {
                lowest = enemy.renderY(1);
                x = enemy.renderX(1);
            }
        }
        int commands = MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, PILOT_Y);
        return firing && lowest < FIRE_BELOW ? commands | Command.FIRE.bit() : commands;
    }

    /** The tells of the vents that were on the field in the hold. */
    private static List<Integer> counted(int[] tells, boolean[] present) {
        List<Integer> counts = new ArrayList<>();
        for (int v = 0; v < tells.length; v++) {
            if (present[v]) {
                counts.add(tells[v]);
            }
        }
        return counts;
    }
}
