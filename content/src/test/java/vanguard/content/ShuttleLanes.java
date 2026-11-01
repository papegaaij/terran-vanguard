package vanguard.content;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vanguard.sim.Ally;
import vanguard.sim.Enemy;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C, step C6a (round 75 F.3, the user's note 2026-11-01: "when there are friendlies on the screen, you cannot
 * have the gunship fly in like in the capture: you cannot hit it behind the friendlies"): with air allies the player's
 * fire hits ({@code player_hits}, Level 37's shuttles), no enemy may hover or fly in behind one in the player's line of
 * fire. Flown as {@link LevelMetrics#stacking} flies a level (the ship holds the lower centre without firing,
 * invulnerable, so every unit flies its whole path): every span an enemy stays <em>behind</em> a shuttle that is on its
 * station, sliding aside or climbing out: above it on the screen with more than half its width inside the shuttle's
 * hull column. A span longer than {@link #SECONDS} is a unit hovering or flying in behind it; a shorter one a unit
 * crossing the column (a swarm's sweep). Air units ({@code air}, {@code low-air}) are the rule; the ground units
 * scrolling under the column are listed apart (the nests off the pads' lanes, L.2).
 */
final class ShuttleLanes implements LevelMetrics.Probe {
    /** Longer than this behind a shuttle, s, a unit hovers or flies in behind it. */
    static final double SECONDS = 1.0;

    /** Spans shorter than this are not kept, s. */
    static final double KEPT = 0.3;

    /**
     * One span a unit stayed behind a shuttle: its slug, its wave's time (-1 for a ground unit), the shuttle (0-based),
     * the shuttle's phase when the span began, the level second it began and how long it lasted.
     */
    record Behind(String slug, double wave, boolean ground, int shuttle, String phase, double from, double seconds) {
        @Override
        public String toString() {
            return String.format(
                    "%s%s behind #%d (%s) at t=%.1f for %.1f s",
                    slug, ground ? " (ground)" : String.format(" (t=%s)", trim(wave)), shuttle, phase, from, seconds);
        }

        private static String trim(double t) {
            return t == Math.rint(t) ? String.valueOf((int) t) : String.valueOf(t);
        }
    }

    private final Map<Enemy, double[]> open = new IdentityHashMap<>();
    private final Map<Enemy, String> openPhase = new IdentityHashMap<>();
    private final List<Behind> spans = new ArrayList<>();

    @Override
    public void newAttempt() {
        open.clear();
        openPhase.clear();
        spans.clear();
    }

    @Override
    public void stepped(Sortie sortie, int step) {
        double t = sortie.levelSeconds();
        Hitbox hull = sortie.script()
                .objectives()
                .airAllies()
                .map(allies -> allies.ally().hitbox())
                .orElse(null);
        Map<Enemy, Boolean> seen = new IdentityHashMap<>();
        if (hull != null) {
            for (int k = 0; k < sortie.allyCount(); k++) {
                Ally shuttle = sortie.ally(k);
                String phase = phase(shuttle);
                if (phase == null) {
                    continue;
                }
                double left = shuttle.x() - hull.width() / 2;
                double right = shuttle.x() + hull.width() / 2;
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (!target(enemy) || seen.containsKey(enemy)) {
                        continue;
                    }
                    double x = enemy.renderX(1);
                    double y = enemy.renderY(1);
                    double half = enemy.hitbox().width() / 2;
                    double overlap = Math.min(right, x + half) - Math.max(left, x - half);
                    if (y > shuttle.y() && y < PlayField.HEIGHT && overlap > half) {
                        seen.put(enemy, true);
                        double[] span = open.get(enemy);
                        if (span == null) {
                            open.put(enemy, new double[] {t, t, k});
                            openPhase.put(enemy, phase);
                        } else {
                            span[1] = t;
                        }
                    }
                }
            }
        }
        for (var it = open.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            if (!seen.containsKey(entry.getKey())) {
                close(entry.getKey(), entry.getValue());
                it.remove();
            }
        }
    }

    private void close(Enemy enemy, double[] span) {
        double seconds = span[1] - span[0] + SimStep.SECONDS;
        if (seconds >= KEPT) {
            boolean ground = enemy.layer() == Layer.GROUND;
            spans.add(new Behind(
                    enemy.spec().slug(),
                    ground ? -1 : waveTime(enemy),
                    ground,
                    (int) span[2],
                    openPhase.get(enemy),
                    span[0],
                    seconds));
        }
    }

    private Sortie sortie;

    private double waveTime(Enemy enemy) {
        int wave = enemy.wave();
        return wave >= 0 && sortie != null ? sortie.script().waves().get(wave).t() : -1;
    }

    /** The shuttle's phase the rule covers: on its station or sliding aside (touchable), climbing out; else null. */
    static String phase(Ally shuttle) {
        if (shuttle.touchable()) {
            return "station";
        }
        return shuttle.state() == Ally.State.CLIMBING ? "climbing" : null;
    }

    /** A unit the player would shoot at: alive, on a layer the guns hit, not cloaked, not neutral. */
    private static boolean target(Enemy enemy) {
        return enemy.hp() > 0
                && !enemy.cloaked()
                && enemy.layer().hitByStandardShots()
                && !enemy.spec().neutral();
    }

    /** Every span (the open ones closed), longest first. */
    List<Behind> spans() {
        open.forEach(this::close);
        open.clear();
        List<Behind> all = new ArrayList<>(spans);
        all.sort((a, b) -> Double.compare(b.seconds(), a.seconds()));
        return all;
    }

    /**
     * The level's spans over its script flown unharmed for {@code seconds} (or to its end): every air unit's and ground
     * unit's, each unit and shuttle once (a retry replays the same script), in time order.
     */
    static List<Behind> behind(Content content, String key, Difficulty difficulty, Loadout loadout, double seconds) {
        Sortie sortie = new Sortie(
                LevelMetrics.SEED,
                loadout,
                SimSpecs.level(content, key, difficulty),
                SimSpecs.rules(content, key, difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
        ShuttleLanes lanes = new ShuttleLanes();
        lanes.sortie = sortie;
        int steps = SimStep.ticks(seconds);
        for (int step = 1; step <= steps && !sortie.complete(); step++) {
            sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), PlayField.WIDTH / 2, 80));
            lanes.stepped(sortie, step);
        }
        Map<String, Behind> once = new LinkedHashMap<>();
        for (Behind span : lanes.spans()) {
            once.putIfAbsent(span.slug() + "#" + span.shuttle() + "@" + Math.round(span.from() * 10), span);
        }
        List<Behind> all = new ArrayList<>(once.values());
        all.sort((a, b) -> Double.compare(a.from(), b.from()));
        return all;
    }

    /** The spans of air units over {@link #SECONDS}: the rule's breaches. */
    static List<Behind> breaches(List<Behind> spans) {
        return spans.stream()
                .filter(span -> !span.ground() && span.seconds() > SECONDS)
                .toList();
    }
}
