package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M9 part F, step F1: Level 33's fixtures at the approved docs' medium numbers (design/enemies/space/void-leech,
 * design/enemies/ground/rail-bunker, the Talon): the Void Leech (12 HP in the beam era, 3 slots, the generator drain,
 * thrown off by the EMP, prey drift 60 px/s; drift 50 px/s, chase within 220 px at 190 px/s turning 160°/s for 4 s),
 * its prey hunter, a prey Talon wing on its refuelling hold (QF-1 a: 5 s at 140–200 px below the top, then straight
 * up), the rail with its gate of 2 telegraphs. Content builds the same from the data (F2).
 */
final class Act5FSpecs {
    private Act5FSpecs() {}

    /** The leech's latch: the Lamprey's spots, ×2 damage, 4 reversals in 1.2 s, stunned 1 s, thrown at 120 px/s. */
    static final EnemySpec.Latch LATCH = new EnemySpec.Latch(3, LampreyTest.SPOTS, 2, 4, 1.2, 1, 120, 0)
            .withDrainsPower(true)
            .withEmpThrows(true)
            .withPreyDrift(60);

    /** Drift 50 px/s, then within 220 px a 4 s chase at 190 px/s turning 160°/s. */
    static final EnemySpec.Chase CHASE = new EnemySpec.Chase(190, Math.toRadians(160), 4).withDrift(220, 50);

    /** The Void Leech: small, 12 HP, a `small` contact 10, 12 credits, its way in at its drift speed. */
    static final EnemySpec LEECH = EnemySpec.of("void-leech", 12, new Hitbox(20, 24), Layer.AIR, 10, true, 12, 50)
            .withChase(CHASE, Optional.of(LATCH));

    /** A leech of a {@code hunts: prey} wave. */
    static final EnemySpec HUNTER = LEECH.withPreyRole(EnemySpec.PreyRole.HUNTER);

    /** A Talon (32 HP, its 2-point shield, 220 px/s, an aimed shot every 1.6 s) on a refuelling hold: prey. */
    static final EnemySpec PREY_TALON = EnemySpec.of("talon", 32, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 220)
            .withGun(Optional.of(EnemyGun.aimed(1.6, 0.25, 1, 160, 4, false)))
            .withMachine(true)
            .withShield(Optional.of(Act5Specs.shield(2)))
            .withHover(Optional.of(new EnemySpec.Hover(new Range(5, 5), new Range(140, 200))))
            .withPreyRole(EnemySpec.PreyRole.PREY);

    /** The Rail Bunker's rail with the gate: never more than 2 lines telegraphed at once. */
    static final EnemySpec.LaserLine RAIL = Act5Specs.RAIL.withMostTelegraphing(2);

    /** A stream of {@code count} {@code unit}s 0.5 s apart from {@code entry} (an edge for the sides). */
    static WaveSpec stream(double t, EnemySpec unit, int count, WaveSpec.Entry entry, WaveSpec.Edge edge) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.STREAM,
                unit,
                count,
                entry,
                edge,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.of(0.5),
                List.of());
    }

    /**
     * A side stream from {@code edge} flying in at {@code depth} px below the top edge, straight across (the wave's
     * route: Level 33's prey streams enter at their wing's height).
     */
    static WaveSpec sideStreamAt(double t, EnemySpec unit, int count, WaveSpec.Edge edge, double depth) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.STREAM,
                unit,
                count,
                WaveSpec.Entry.SIDES,
                edge,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.of(0.5),
                List.of(),
                Optional.empty(),
                List.of(List.of(new WaveSpec.At(-40, depth), new WaveSpec.At(PlayField.WIDTH + 40, depth))));
    }

    /** A prey V-wing of {@code count} Talons from the front at {@code t}: it settles on its hold, then leaves up. */
    static WaveSpec preyWing(double t, int count) {
        return TestSpecs.wave(
                t, WaveSpec.Formation.V_WING, PREY_TALON, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    /** A sortie of {@code waves} over {@code seconds} with {@code loadout}. */
    static Sortie sortie(Loadout loadout, Rules rules, double seconds, WaveSpec... waves) {
        return new Sortie(1, loadout, TestSpecs.level(seconds, List.of(waves)), rules, TestSpecs.FULL_ARMOUR);
    }

    /** The leeches loose on the screen now (not latched on the ship nor riding a prey): Level 33's at most 6. */
    static int loose(Sortie sortie) {
        int count = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals("void-leech") && enemy.loose()) {
                count++;
            }
        }
        return count;
    }
}
