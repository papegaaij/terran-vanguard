package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 parts C and D, step CD1 (Levels 45 and 46): the units and levels of their new mechanics for the simulation tests,
 * with the medium numbers of the approved docs: the spawning organ (its whirl cluster and Skitters alternating, first at
 * 1.5 s, held near the ship, its clock EMP-proof), the Whirl Seed, the Husk (its drift and its crack), the seed casket
 * and the dying Leviathan as scenery.
 */
final class Act7CDSpecs {
    private Act7CDSpecs() {}

    /** The Whirl Seed: a 6-seed cluster's unit (design/enemies/air/whirl-seed). */
    static final EnemySpec SEED = EnemySpec.of("whirl-seed", 1, new Hitbox(16, 16), Layer.AIR, 6, true, 3, 160)
            .withSpiral(Optional.of(new EnemySpec.Spiral(1.5, 1, 90, 60, 3)));

    /** The organ's cluster: 6 seeds, every 3 s, its pod's swell 0.5 s, held while the ship is within 120 px. */
    static EnemySpec.Spawner organSpawner(double shutWithin) {
        return new EnemySpec.Spawner(SEED, 6, 3.0, 0.5, 0, 0, shutWithin)
                .withCluster(true)
                .withFirst(1.5)
                .withHold(true)
                .withEmpImmune(true)
                .withAlternate(
                        new EnemySpec.Spawner(TestSpecs.SKITTER, 2, 3.0, 0.5, Math.toRadians(120), 160, shutWithin));
    }

    /** The spawning organ (design/enemies/ground/spawning-organ): 183 HP, 60 × 60, not hardened, uncounted, 45 credits. */
    static EnemySpec organ(double shutWithin) {
        return EnemySpec.of("spawning-organ", 183, new Hitbox(60, 60), Layer.GROUND, 0, false, 45, 0)
                .withTerrain(true)
                .withUncounted(true)
                .withSpawner(Optional.of(organSpawner(shutWithin)));
    }

    /** The Husk's crack: at half its HP, a 0.5 s tell, then a fan of 5 shards 15 degrees apart at 220 px/s. */
    static final EnemySpec.Crack CRACK = new EnemySpec.Crack(
            0.5,
            0.5,
            new EnemyGun(
                    Double.POSITIVE_INFINITY,
                    0,
                    1,
                    220,
                    4,
                    false,
                    5,
                    Math.toRadians(60),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY));

    /** The Husk's drift: 20–40 px/s, a 10–25 degrees a second spin. */
    static final EnemySpec.Drift DRIFT = new EnemySpec.Drift(new Range(20, 40), new Range(10, 25));

    /** The Husk (design/enemies/space/husk): 150 HP, 64 × 48, contact 15, 25 credits, its ring of 8 when it dies. */
    static EnemySpec husk(double hp) {
        return EnemySpec.of("husk", hp, new Hitbox(64, 48), Layer.AIR, 15, false, 25, 30)
                .withLineDrift(Optional.of(DRIFT))
                .withCrack(Optional.of(CRACK))
                .withDeathBurst(Optional.of(new EnemySpec.DeathBurst(8, 90, 4)));
    }

    /** The seed casket (design/enemies/space/seed-casket): 20 HP, drifting 15–25 px/s, uncounted, ignored by seekers. */
    static final EnemySpec CASKET = EnemySpec.of("seed-casket", 20, new Hitbox(40, 28), Layer.AIR, 0, false, 0, 20)
            .withLineDrift(Optional.of(new EnemySpec.Drift(new Range(15, 25), new Range(0, 0))))
            .withUncounted(true)
            .withSeekersIgnore(true);

    /** A drifting wave of {@code count} at {@code t} on {@code lines} (x, depth below the top edge), unwarned or not. */
    static WaveSpec drift(double t, EnemySpec unit, int count, boolean unwarned, List<WaveSpec.Line> lines) {
        WaveSpecBuilder builder = WaveSpec.builder(
                        t, WaveSpec.Formation.SINGLE, unit, count, WaveSpec.Entry.SIDES, WaveSpec.Edge.LEFT)
                .lines(lines);
        return unwarned ? builder.unwarned().build() : builder.build();
    }

    /** A line from (x0, depth0) to (x1, depth1). */
    static WaveSpec.Line line(double x0, double depth0, double x1, double depth1) {
        return new WaveSpec.Line(new WaveSpec.At(x0, depth0), new WaveSpec.At(x1, depth1));
    }

    /** A level of {@code seconds} at {@code speed} px/s with these waves, ground units and Act 7 parts. */
    static LevelScript level(
            double seconds, double speed, List<WaveSpec> waves, List<GroundUnit> units, VrellLevel vrell) {
        return LevelScript.builder(45, 7)
                .sections(List.of(new Section(seconds, speed)))
                .waves(waves)
                .groundUnits(units)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .vrell(vrell)
                .build();
    }

    /** A sortie of {@code level} with the test loadout, the ship invulnerable. */
    static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** The first unit of {@code slug} in the sortie; null for none. */
    static Enemy find(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** How many units of {@code slug} are in the sortie. */
    static int count(Sortie sortie, String slug) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            n += sortie.enemy(i).spec().slug().equals(slug) ? 1 : 0;
        }
        return n;
    }

    /**
     * The dying Leviathan in small, as scenery: a vital blowhole and two fins (10 HP each, bounties 50/30/30), a gun on
     * a fin, one pass on {@code layer} straight down the middle from {@code t} over 20 s; small salvage per part, a
     * medium at its death.
     */
    static SetPieceSpec leviathan(Layer layer, boolean scenery) {
        EnemyGun gun = EnemyGun.aimed(0.5, 0, 1, 150, 4, false);
        List<PartSpec> parts = List.of(
                new PartSpec("blowhole", 0, 40, new Hitbox(40, 40), 10, true, 50, Optional.empty(), 0, 1),
                new PartSpec("fin left", -60, 0, new Hitbox(30, 30), 10, false, 30, Optional.of(gun), 0, 1),
                new PartSpec("fin right", 60, 0, new Hitbox(30, 30), 10, false, 30, Optional.empty(), 0, 1));
        Pass pass = Pass.flight(
                "dying",
                layer,
                0,
                List.of(
                        new Waypoint(0.5, PlayField.WIDTH / 2.0, PlayField.HEIGHT + 150),
                        new Waypoint(20.5, PlayField.WIDTH / 2.0, -150)));
        SetPieceSpec piece = new SetPieceSpec(
                "leviathan",
                new Hitbox(200, 260),
                new Hitbox(80, 200),
                25,
                parts,
                List.of(pass),
                Optional.of(PickupType.LARGE_SALVAGE));
        return scenery
                ? piece.withScenery(Optional.of(PickupType.SMALL_SALVAGE), Optional.of(PickupType.MEDIUM_SALVAGE))
                : piece;
    }
}
