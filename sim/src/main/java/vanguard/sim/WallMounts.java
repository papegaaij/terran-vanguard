package vanguard.sim;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M8 part E (design/campaign Level 25, The caves and Siren nests; m8e-gaps.md §1.1, §1.2, §5 E1, E3, E5): the waves that
 * come out of a trench's walls ({@link WaveSpec#mount()}), planned unit by unit when the level is loaded, from the
 * level's terrain and its planned scroll ({@link ScrollPlan#scrollAt}).
 *
 * <ul>
 *   <li><b>Wall nests</b> ({@link WaveSpec#withWall}): each unit stands on its wall's foot, {@link #FOOT_INSET} px out
 *       from the collision line at its row, entering at the top edge with the ground and scrolling with it ({@link
 *       Enemy} in its mount); staggered, a nest every {@link #STAGGER_SECONDS} s (on both walls taking turns from the
 *       right), or opposite: in pairs at one row, a pair every two staggers, the left nest's song a stagger behind the
 *       right one's (the phase rule wins, §5 E3).
 *   <li><b>Cave entries</b> ({@link WaveSpec#withCave}): planned by their formation (a side column), then each unit
 *       starts at its own time at the notch's back, inside the opening, and swims straight out of the mouth sinking
 *       with the scroll (sinking at up to {@link #MOST_SINK} of its speed, so it stays inside the opening, its
 *       floor and ceiling kept {@link #MARGIN} px off), then flies on to where its formation put it. A hover held above
 *       the ship stops only once out of the notch.
 * </ul>
 *
 * Planned once; the per-unit facts are read back by the {@link WaveSchedule} ({@link #placed}).
 */
public final class WallMounts {
    /** The nests' stagger, and the opposite pair's song offset (the phase rule: rings at least this far apart), s. */
    public static final double STAGGER_SECONDS = 1.5;
    /** A nest's centre stands this far out from its wall's line into the water (the 24 px foot band's middle), px. */
    public static final double FOOT_INSET = 12;
    /** A cave unit keeps this far off the notch's rock while it swims out, px. */
    static final double MARGIN = 2;
    /** The most of its speed a cave unit sinks with while it swims out (the rest takes it across). */
    static final double MOST_SINK = 0.8;
    /** Past the mouth its swim goes straight on this far before it turns toward its formation's point, px. */
    static final double LEAD = 16;

    /** No walls: a mounted wave cannot be planned. */
    static final WallMounts NONE = new WallMounts(null);

    /**
     * One unit's mount: a wall nest's wall and song offset, or a cave unit's swim out.
     *
     * @param left a nest on the left wall (false: the right one, or a cave unit)
     * @param delaySeconds a nest's song offset after its first ring's time (the phase rule); NaN for a cave unit
     * @param swimOut how far along its path a cave unit is out of its notch, px (its hover may stop past it); 0 for a
     *     nest
     */
    record Placed(boolean left, double delaySeconds, double swimOut) {
        /** Whether it is a wall's nest (else it swims out of a cave). */
        boolean nest() {
            return !Double.isNaN(delaySeconds);
        }
    }

    private final LevelScript script;
    private final Map<Spawn, Placed> placed = new IdentityHashMap<>();

    private WallMounts(LevelScript script) {
        this.script = script;
    }

    /** The mounts of {@code script}'s level: its terrain and scroll (none without terrain). */
    static WallMounts of(LevelScript script) {
        return script.ground().terrain().isPresent() ? new WallMounts(script) : NONE;
    }

    /**
     * Plans {@code wave} (with its mount) into {@code out}: a wall's nests, or a cave's units from their formation (laid
     * out on {@code span}).
     */
    void plan(WaveSpec wave, int kind, SplitMix64 rng, List<Spawn> out, Optional<Formations.Span> span) {
        WaveSpec.Mount mount = wave.mount().orElseThrow();
        if (script == null) {
            throw new IllegalArgumentException(
                    wave.enemy().slug() + ": a wave out of the walls needs a level with walls");
        }
        if (!script.objectives().holds().isEmpty() || script.sections().stream().anyMatch(Section::arena)) {
            throw new IllegalArgumentException(
                    wave.enemy().slug() + ": a wave out of the walls needs a planned scroll (no holds, no arena)");
        }
        if (mount.wall()) {
            nests(wave, kind, out);
        } else {
            cave(wave, mount.cave(), kind, rng, out, span);
        }
    }

    /** The mount of a unit this planned; null for any other. */
    Placed placed(Spawn spawn) {
        return placed.get(spawn);
    }

    private Terrain terrain() {
        return script.ground().terrain().orElseThrow().terrain();
    }

    private void nests(WaveSpec wave, int kind, List<Spawn> out) {
        EnemySpec enemy = wave.enemy();
        boolean opposite = wave.mount().orElseThrow().opposite();
        double halfHeight = enemy.hitbox().height() / 2;
        for (int i = 0; i < wave.count(); i++) {
            boolean left;
            double enter;
            double delay = 0;
            if (opposite) {
                // A pair every two staggers: the right nest, then the left one at its row, its song a stagger later.
                left = i % 2 == 1;
                enter = wave.t() + (i / 2) * 2 * STAGGER_SECONDS;
                delay = left ? STAGGER_SECONDS : 0;
            } else {
                left = switch (wave.edge()) {
                    case LEFT -> true;
                    case RIGHT -> false;
                    default -> i % 2 == 1;
                };
                enter = wave.t() + i * STAGGER_SECONDS;
            }
            int tick = SimStep.ticks(enter);
            double y = PlayField.HEIGHT + halfHeight;
            double row = Terrain.row(ScrollPlan.scrollAt(script, enter), y);
            double line = left ? terrain().leftAt(row) : terrain().rightAt(row);
            if (Double.isNaN(line)) {
                throw new IllegalArgumentException(enemy.slug() + ": no " + (left ? "left" : "right") + " wall at row "
                        + row + " for its nest at t=" + enter);
            }
            double x = line + (left ? FOOT_INSET : -FOOT_INSET);
            Spawn nest = new Spawn(
                    tick,
                    kind,
                    enemy,
                    FlightPath.through(x, y, x, -Formations.OUTSIDE),
                    0,
                    0,
                    Optional.empty(),
                    Spawn.Exit.DOWN,
                    false,
                    carried(wave, i));
            out.add(nest);
            placed.put(nest, new Placed(left, delay, 0));
        }
    }

    private void cave(
            WaveSpec wave, int index, int kind, SplitMix64 rng, List<Spawn> out, Optional<Formations.Span> span) {
        Terrain terrain = terrain();
        if (index >= terrain.notches().size()) {
            throw new IllegalArgumentException(wave.enemy().slug() + ": no notch " + index + " ("
                    + terrain.notches().size() + " notches)");
        }
        Terrain.Notch notch = terrain.notches().get(index);
        boolean left = notch.side() == Terrain.Side.LEFT;
        if (left != (wave.edge() == WaveSpec.Edge.LEFT)) {
            throw new IllegalArgumentException(wave.enemy().slug() + ": a cave wave enters from its notch's side, "
                    + notch.side() + ", not " + wave.edge());
        }
        int before = out.size();
        Formations.plan(wave, kind, rng, out, span);
        EnemySpec enemy = wave.enemy();
        double halfWidth = enemy.hitbox().width() / 2;
        double halfHeight = enemy.hitbox().height() / 2;
        double room = notch.width() - 2 * halfHeight - 2 * MARGIN;
        if (!(room > 0)) {
            throw new IllegalArgumentException(
                    enemy.slug() + " does not fit notch " + index + " (" + notch.width() + " px wide)");
        }
        double sign = left ? 1 : -1;
        double mouth = terrain.notchMouth(index);
        double fromX = mouth - sign * notch.depth() + sign * (halfWidth + MARGIN);
        double toX = mouth + sign * (halfWidth + MARGIN);
        double across = Math.abs(toX - fromX);
        for (int i = before; i < out.size(); i++) {
            Spawn unit = out.get(i);
            double t = unit.tick() * SimStep.SECONDS;
            double speed = unit.speed();
            double scroll = ScrollPlan.timedHaltSpeed(script.sections(), unit.tick());
            if (!(speed > 0)) {
                throw new IllegalArgumentException(enemy.slug() + ": a cave unit swims out at a speed");
            }
            // Sinking with the scroll keeps it in the opening; what it cannot match drifts it toward the ceiling.
            double sink = scroll > 0 ? Math.min(speed / scroll, MOST_SINK) : 0;
            double cross = Math.sqrt(1 - sink * sink);
            double seconds = across / (speed * cross);
            double drift = Math.max(0, scroll - speed * sink) * seconds;
            if (drift > room) {
                throw new IllegalArgumentException(enemy.slug() + " swims out of notch " + index + " too slowly at t="
                        + t + ": it drifts " + drift + " px of " + room + " toward the ceiling");
            }
            double startRow = notch.low() + halfHeight + MARGIN + (room - drift) / 2;
            double fromY = Terrain.y(ScrollPlan.scrollAt(script, t), startRow);
            double toY = fromY - speed * sink * seconds;
            double leadX = toX + sign * cross * LEAD;
            double leadY = toY - sink * LEAD;
            Spawn swim = unit.withPath(FlightPath.through(
                    fromX,
                    fromY,
                    toX,
                    toY,
                    leadX,
                    leadY,
                    unit.path().endX(),
                    unit.path().endY()));
            out.set(i, swim);
            placed.put(swim, new Placed(false, Double.NaN, Math.hypot(leadX - fromX, leadY - fromY)));
        }
    }

    private static Optional<PickupType> carried(WaveSpec wave, int unit) {
        for (WaveSpec.Carried carried : wave.carried()) {
            if (carried.carrier(wave.count()) == unit) {
                return Optional.of(carried.pickup());
            }
        }
        return Optional.empty();
    }
}
