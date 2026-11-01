package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part D, step D1: Level 38's unit and hazard fixtures at medium (design/enemies/air/harrow, design/campaign Level
 * 38: the Harrow's chase and ram, Aurelia's cargo pod, the CDF supply canister, {@code HARROWS n / 23}); the content's
 * own fixtures are step D2's.
 */
final class Act6DSpecs {
    private Act6DSpecs() {}

    /**
     * The Harrow's cycle at medium: chase 90°/s for a seeded 1.5–2.5 s, the lock tell 0.6 s at 50 px/s, one straight
     * ram at 300 px/s along the bearing at the tell's end, no bounce, out through the far edge.
     */
    static final EnemySpec.Dash RAM =
            new EnemySpec.Dash(Math.toRadians(90), 1.5, 0.6, 50, 300, 0, 0, 1, EnemySpec.Dash.Variant.ram(2.5));

    /** The Harrow: 16 HP (beam era), 22 px hit box, contact 10 (destroyed by it), 12 credits, 110 px/s, its ring. */
    static final EnemySpec HARROW = EnemySpec.of("harrow", 16, new Hitbox(22, 22), Layer.AIR, 10, true, 12, 110)
            .withMachine(true)
            .withDeathBurst(Optional.of(Act6ASpecs.HARROW_RING))
            .withDash(Optional.of(RAM));

    /** A plain air unit flying down the screen (40 HP, 10 credits, 60 px/s). */
    static final EnemySpec DRONE = EnemySpec.of("drone", 40, new Hitbox(24, 24), Layer.AIR, 10, true, 10, 60);

    /** A stream of {@code count} Harrows at {@code t} from {@code entry} ({@code edge}), 0.6 s apart. */
    static WaveSpec stream(double t, int count, WaveSpec.Entry entry, WaveSpec.Edge edge) {
        return WaveSpec.builder(t, WaveSpec.Formation.STREAM, HARROW, count, entry, edge)
                .intervalSeconds(Optional.of(0.6))
                .build();
    }

    /** A cross of {@code count} Harrows at {@code t} from all four edges. */
    static WaveSpec cross(double t, int count) {
        return WaveSpec.builder(t, WaveSpec.Formation.CROSS, HARROW, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .build();
    }

    /** Aurelia's cargo pod at {@code t}, {@code x}: a 20-HP chunk tumbling down at 60 px/s, its crate 100 credits. */
    static HazardSpec.DebrisSpec pod(double t, double x) {
        return new HazardSpec.DebrisSpec(t, x, "cargo-pod", new Hitbox(28, 28), 0, -60, 20, 0, 0)
                .withSecret("aurelia-pod", 100);
    }

    /** The CDF supply canister at {@code t}, {@code x}: a 10-HP latch on its parachute, the armour patch, marked. */
    static HazardSpec.DebrisSpec canister(double t, double x) {
        return new HazardSpec.DebrisSpec(t, x, "canister", new Hitbox(24, 32), 0, -50, 10, 0, 0)
                .withDrop(PickupType.ARMOUR_PATCH)
                .withMarked(true);
    }

    /**
     * Round 76 L.1 b: the CDF supply canister at {@code t}, {@code x} with no latch: it opens by itself over the middle
     * of the screen (y 270) and lets the armour patch fall; marked, the fire passes it.
     */
    static HazardSpec.DebrisSpec openingCanister(double t, double x) {
        return canister(t, x).withOpensAt(PlayField.HEIGHT / 2.0);
    }

    /** A level of {@code seconds} at 140 px/s with {@code hazards}, {@code waves} and {@code secondary}. */
    static LevelScript level(double seconds, HazardSpec hazards, List<WaveSpec> waves, Secondary secondary) {
        return LevelScript.builder(38, 6)
                .sections(List.of(new Section(seconds, 140)))
                .hazards(hazards)
                .waves(waves)
                .secrets(1)
                .radio(List.of(CloudDiversSpecs.cue(CueTrigger.SECRET, "aurelia-pod")))
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .build();
    }

    /** A level of {@code seconds} with {@code waves} only (a plain kill ratio). */
    static LevelScript level(double seconds, List<WaveSpec> waves) {
        return level(seconds, HazardSpec.NONE, waves, new Secondary(0.8, 50));
    }

    /** A sortie of {@code level} with the starter loadout; the ship is hit as in play. */
    static Sortie sortie(LevelScript level) {
        return new Sortie(3, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** A sortie of {@code level} with the starter loadout; nothing hits the ship. */
    static Sortie untouchable(LevelScript level) {
        return new Sortie(3, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** The units of {@code slug} on the screen. */
    static int count(Sortie sortie, String slug) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            n += sortie.enemy(i).spec().slug().equals(slug) ? 1 : 0;
        }
        return n;
    }
}
