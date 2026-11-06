package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 part A, step A1a: the Act 7 enemy-side fixtures, after the approved docs (design/enemies, Window shields and
 * Distorted space; design/enemies/space/gate-warden, rift-skater; design/enemies/air/choir-seraph) at medium. The
 * content's own fixtures are step A2a's.
 */
final class Act7ASpecs {
    private Act7ASpecs() {}

    /** The window shield: open 3 s on a breaker hit or the EMP, regrowing 0.4 s, the iris 0.15 s, a tick per 0.15 s. */
    static final EnemySpec.WindowShield SHELL = new EnemySpec.WindowShield(3.0, 0.4, 0.15, 0.15);

    /**
     * The Gate Warden's line: telegraph 0.8 s, live 1.0 s, a 10 px band, heavy 10, every 4.0 s (its first {@code first}
     * s after it is on the field), at most 2 telegraphed at once; straight down or aimed.
     */
    static EnemySpec.LaserLine line(boolean straight, double first) {
        return new EnemySpec.LaserLine(0.8, 1.0, 10, 10, 4.0, first, 0)
                .withMostTelegraphing(2)
                .withStraight(straight);
    }

    /** A Gate Warden holding its station 140 px below the top edge for {@code hold} s, then rising out at 90 px/s. */
    static EnemySpec warden(double hold, boolean straight) {
        return EnemySpec.of("gate-warden", 360, new Hitbox(56, 60), Layer.AIR, 15, false, 40, 90)
                .withHover(Optional.of(
                        new EnemySpec.Hover(new Range(hold, hold), new Range(140, 140)).withExitUp(90, 2.0)))
                .withLaserLine(Optional.of(line(straight, 0.5)))
                .withWindowShield(Optional.of(SHELL));
    }

    static final EnemySpec WARDEN = warden(14, true);

    /** A shelled unit riding the ground (placed by x and time), its first line {@code first} s after it is on. */
    static EnemySpec shelled(double first) {
        return EnemySpec.of("shelled", 360, new Hitbox(56, 60), Layer.AIR, 15, false, 40, 0)
                .withTerrain(true)
                .withLaserLine(Optional.of(line(true, first)))
                .withWindowShield(Optional.of(SHELL));
    }

    /**
     * The Rift Skater's teleport: fade 0.2 s, ring 0.5 s, never within 120 px of the ship, jumps of 90–130 px at 30–60°
     * off its frame's down, glides 1.2 s then 1.0 s, 3 jumps, fires 0.25 s after landing, at most 4 rings.
     */
    static final EnemySpec.Teleport JUMPS =
            new EnemySpec.Teleport(0.2, 0.5, 120, new Range(90, 130), new Range(30, 60), 1.2, 1.0, 3, 0.25, 4);

    /** Its burst: 3 aimed shots 0.15 s apart, 160 px/s, small 4 (only after a landing). */
    static final EnemyGun SKATER_GUN = EnemyGun.aimed(10, 0, 3, 160, 4, false);

    /** A Rift Skater: 32 HP, contact 8, 140 px/s gliding. */
    static final EnemySpec SKATER = EnemySpec.of("rift-skater", 32, new Hitbox(26, 20), Layer.AIR, 8, true, 14, 140)
            .withGun(Optional.of(SKATER_GUN))
            .withTeleport(Optional.of(JUMPS));

    /** The mirror: a 0.15 s lag, 260 px/s, its row 140 px off the ship's. */
    static final EnemySpec.Shadow SHADOW = new EnemySpec.Shadow(0.15, 260, 140);

    /** A spiral: two arms turning 120°/s, each a bullet every 1/3 s (6 a second in all), 120 px/s, small 4. */
    static final EnemyGun SPIRAL = EnemyGun.aimed(1, 0, 1, 120, 4, false)
            .withSpiral(Optional.of(new EnemyGun.SpiralSpec(2, Math.toRadians(120), 1 / 3.0)));

    /** A mirroring unit holding its station {@code depth} px below the top edge for {@code hold} s. */
    static EnemySpec seraph(double hold, double depth) {
        return EnemySpec.of("choir-seraph", 90, new Hitbox(48, 52), Layer.AIR, 15, false, 35, 160)
                .withHover(Optional.of(new EnemySpec.Hover(new Range(hold, hold), new Range(depth, depth))))
                .withGun(Optional.of(SPIRAL))
                .withShadow(Optional.of(SHADOW));
    }

    static final EnemySpec SERAPH = seraph(8, 120);

    /** A single unit of {@code spec} from the front at {@code t} s. */
    static WaveSpec single(double t, EnemySpec spec) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, spec, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .build();
    }

    /** A stream of {@code count} of {@code spec} from the front at {@code t} s, {@code interval} s apart. */
    static WaveSpec stream(double t, EnemySpec spec, int count, double interval) {
        return WaveSpec.builder(t, WaveSpec.Formation.STREAM, spec, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .intervalSeconds(Optional.of(interval))
                .build();
    }

    /** A current at a steady {@code degrees}, its reversals told 2 s ahead. */
    static HazardSpec.Current steady(double degrees, List<HazardSpec.Reversal> reversals) {
        return new HazardSpec.Current(List.of(new HazardSpec.Bend(0, degrees)), reversals, 2.0);
    }

    /** A level of {@code seconds} at 130 px/s with {@code waves} and {@code current} (empty: none). */
    static LevelScript level(double seconds, List<WaveSpec> waves, Optional<HazardSpec.Current> current) {
        LevelScript level = Act6ASpecs.level(seconds, List.of(), waves);
        return level.toBuilder().hazards(level.hazards().withCurrent(current)).build();
    }

    /** A sortie of {@code level} with the front Pulse Cannon, the ship invulnerable. */
    static Sortie sortie(LevelScript level) {
        return Act6ASpecs.sortie(level, Act6ASpecs.front());
    }

    /** The first unit of {@code slug} in the sortie now; null for none. */
    static Enemy find(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** How many events of {@code type} the sortie raised over {@code steps} steps of {@code commands}. */
    static int count(Sortie sortie, int steps, int commands, SimEvents.Type type) {
        int counted = 0;
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
            counted += sortie.events().count(type);
        }
        return counted;
    }
}
