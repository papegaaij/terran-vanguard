package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part A, step A1a: the Act 6 enemy-side fixtures, after the approved unit docs (design/enemies/air/mirror-interceptor,
 * chimera, harrow; design/enemies/ground/graft-turret, shield-pylon) at medium. The units ride the ground (terrain) so a
 * test places them by x and time; the content's own fixtures are step A2a's.
 */
final class Act6ASpecs {
    private Act6ASpecs() {}

    /** The Mirror Interceptor's mirror: arc 120°, heat 130, 60°/s, down 3 s, regrow 0.4 s, gap 0.15 s, 110 px/s × 4. */
    static final EnemySpec.Mirror MIRROR = new EnemySpec.Mirror(120, 130, 60, 3.0, 0.4, 0.15, 0.5, 0.3, 110, 4, 30);

    /** A mirrored air unit riding the ground, facing straight down (the ship's shots meet its mirror). */
    static EnemySpec mirrored(double hp) {
        return EnemySpec.of("mirror-interceptor", hp, new Hitbox(52, 48), Layer.AIR, 15, false, 30, 0)
                .withTerrain(true)
                .withMachine(true)
                .withMirror(Optional.of(MIRROR));
    }

    static final EnemySpec MIRRORED = mirrored(120);

    /** The Chimera's regeneration: 20 % of its maximum a second after 1.0 s without a hit. */
    static final EnemySpec.Regeneration REGENERATION = new EnemySpec.Regeneration(0.20, 1.0);

    /** A Chimera riding the ground: 40 HP (its 20 with the beam era's ×2), regenerating. */
    static final EnemySpec CHIMERA = EnemySpec.of("chimera", 40, new Hitbox(28, 24), Layer.AIR, 10, true, 14, 0)
            .withTerrain(true)
            .withMachine(true)
            .withRegeneration(Optional.of(REGENERATION));

    /** The Graft Turret's regrowth: window 5 s, knit 0.8 s, wake 0.5 s, its root 16 HP (8 × 2) in 24×24, bonus 5. */
    static final EnemySpec.Regrowth REGROWTH = new EnemySpec.Regrowth(5.0, 0.8, 0.5, 16, new Hitbox(24, 24), 5);

    /** An aimed pair every 2.2 s (two shots 4° apart), 160 px/s, {@code small} 4; turning 120°/s, silent past 110°. */
    static final EnemyGun GRAFT_GUN =
            new EnemyGun(2.2, 0.5, 1, 160, 4, false, 2, Math.toRadians(4), Math.toRadians(120), Math.toRadians(110));

    /** A Graft Turret: 24 HP (12 × 2), its regrowth; a hybrid, so not spoofable. */
    static final EnemySpec GRAFT = EnemySpec.of("graft-turret", 24, new Hitbox(32, 32), Layer.GROUND, 10, true, 15, 0)
            .withTerrain(true)
            .withMachine(true)
            .withGun(Optional.of(GRAFT_GUN))
            .withRegrowth(Optional.of(REGROWTH));

    /** The Harrow's ring: 8 {@code small} bullets (4) at 90 px/s on every death, turned between the ship, 0.25 s grace. */
    static final EnemySpec.DeathBurst HARROW_RING = new EnemySpec.DeathBurst(8, 90, 4, true, true, 0.25);

    /** A Harrow riding the ground (it rams the ship that sits in its way): 16 HP, contact 10, destroyed by the ram. */
    static final EnemySpec HARROW = EnemySpec.of("harrow", 16, new Hitbox(22, 22), Layer.AIR, 10, true, 12, 0)
            .withTerrain(true)
            .withMachine(true)
            .withDeathBurst(Optional.of(HARROW_RING));

    /** The Shield Pylon's dome: r 110, 140 px along its crown's bearing ({@code bearing}), breaker 2 s, back 0.3 s. */
    static EnemySpec.Dome dome(double bearing) {
        return new EnemySpec.Dome(110, 140, bearing, 2.0, 0.3);
    }

    /** A Shield Pylon whose crown faces straight up the screen: its dome's centre 140 px above it. */
    static final EnemySpec PYLON = EnemySpec.of("shield-pylon", 240, new Hitbox(40, 40), Layer.GROUND, 0, false, 45, 0)
            .withTerrain(true)
            .withMachine(true)
            .withDome(Optional.of(dome(Math.PI)));

    /** A spoofable Ascendancy turret (a stand-in for the SAM Nest, Rail Bunker, ...): an aimed shot every 2 s. */
    static final EnemyGun TURRET_GUN = EnemyGun.aimed(2.0, 0.5, 1, 160, 6, false);

    static final EnemySpec TURRET = EnemySpec.of(
                    "ascendancy-turret", 60, new Hitbox(30, 30), Layer.GROUND, 0, false, 20, 0)
            .withTerrain(true)
            .withMachine(true)
            .withGun(Optional.of(TURRET_GUN))
            .withSpoofable(true);

    /** A plain ground target of {@code hp}. */
    static EnemySpec target(double hp) {
        return TorpedoTest.dummy(Layer.GROUND, hp);
    }

    /** A plain air target of {@code hp}. */
    static EnemySpec air(double hp) {
        return TorpedoTest.dummy(Layer.AIR, hp);
    }

    /** A one-section level of {@code seconds} at 130 px/s with its ground units and waves. */
    static LevelScript level(double seconds, List<GroundUnit> units, List<WaveSpec> waves) {
        return level(seconds, 130, units, waves);
    }

    /** As {@link #level(double, List, List)} at a scroll of {@code speed} px/s. */
    static LevelScript level(double seconds, double speed, List<GroundUnit> units, List<WaveSpec> waves) {
        return LevelScript.builder(1, 1)
                .sections(List.of(new Section(seconds, speed)))
                .groundUnits(units)
                .waves(waves)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    /** A sortie of {@code level} with {@code mounts}, the ship invulnerable (nothing hits it). */
    static Sortie sortie(LevelScript level, Armament.Mount... mounts) {
        return new Sortie(
                1, TestSpecs.loadout(mounts), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** The ship's front Pulse Cannon (TestSpecs's). */
    static Armament.Mount front() {
        return new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2);
    }

    /** Steps {@code steps} times with {@code commands}. */
    static void run(Sortie sortie, int steps, int commands) {
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
        }
    }

    /** The unit with {@code serial}; null when it is gone. */
    static Enemy bySerial(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).serial() == serial) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** The index of the unit with {@code serial}; -1 when it is gone. */
    static int indexOf(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).serial() == serial) {
                return i;
            }
        }
        return -1;
    }

    /** Steps until a unit with {@code slug} is on the play field; returns it. */
    static Enemy until(Sortie sortie, String slug, double belowTop) {
        for (int i = 0; i < SimStep.ticks(20); i++) {
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy unit = sortie.enemy(j);
                if (unit.spec().slug().equals(slug) && unit.y() <= PlayField.HEIGHT - belowTop) {
                    return unit;
                }
            }
            sortie.step(Command.NONE);
        }
        throw new AssertionError("no " + slug + " came " + belowTop + " px down");
    }
}
