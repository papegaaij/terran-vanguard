package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, The escort; {@link Wingman}, {@link RookHazards}): Rook
 * against the test Sovereign shoots an armed battery (inside the fire arc) before the others, the core once it is open,
 * and keeps out of a telling or sweeping laser wedge, which then never hits him.
 */
class RookRingsTest {
    private static Sortie sortie() {
        // Not the invulnerable ship's rules: they spare Rook too.
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                Act5HSpecs.level(Act5HSpecs.SOVEREIGN),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void heShootsTheArmedBatteriesFirstAndTheCoreOnceOpen() {
        Sortie sortie = sortie();
        Act5HSpecs.untilUp(sortie);
        SetPiece boss = Act5HSpecs.boss(sortie);
        RingArena rings = boss.ring().orElseThrow();
        Wingman rook = sortie.wingman().orElseThrow();
        int armed = 0;
        int unarmed = 0;
        for (int i = 0; i < SimStep.ticks(20) && !sortie.failed; i++) {
            sortie.step(Command.NONE);
            int part = rook.target() - PlayerFire.PART_SERIAL;
            if (part < 0 || part >= boss.partCount() || rings.battery(part) < 0) {
                continue;
            }
            if (rings.armed(part)) {
                armed++;
            } else if (anyArmedInCone(rook, boss, rings)) {
                unarmed++;
            }
        }
        assertTrue(armed > 0, "he picks the armed batteries: " + armed);
        assertTrue(unarmed <= armed / 10, "rarely another while an armed one is in reach: " + unarmed + " / " + armed);
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(boss, ring);
        }
        boolean core = false;
        for (int i = 0; i < SimStep.ticks(10) && !core && !sortie.failed; i++) {
            sortie.step(Command.NONE);
            core = rook.target() == PlayerFire.PART_SERIAL + Act5HSpecs.CORE;
            if (core) {
                assertTrue(rings.core().orElseThrow().open(), "the core only once open");
            }
        }
        assertTrue(core, "the open core");
    }

    /** Whether an armed battery lies in his cone and range now. */
    private static boolean anyArmedInCone(Wingman rook, SetPiece boss, RingArena rings) {
        for (int p = 0; p < boss.partCount(); p++) {
            if (rings.armed(p) && rook.inCone(boss.partX(p) - rook.x(), boss.partY(p) - rook.y())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void heKeepsOutOfTheLaserWedgesThatNeverHitHim() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                Act5HSpecs.level(Act5HSpecs.LASERS_ONLY),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Act5HSpecs.untilUp(sortie);
        SetPiece boss = Act5HSpecs.boss(sortie);
        Wingman rook = sortie.wingman().orElseThrow();
        int sweeps = 0;
        int shipHits = 0;
        int rookHits = 0;
        boolean evaded = false;
        boolean wasHit = false;
        for (int i = 0; i < SimStep.ticks(40) && !sortie.failed && !rook.out(); i++) {
            // The ship drifts slowly side to side under the station, into the wedges.
            sortie.step((i / 150) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
            sweeps += sortie.events().count(SimEvents.Type.SPIRE_SWEEP_TELEGRAPH);
            shipHits += sortie.events().count(SimEvents.Type.SWEEP_HIT);
            boolean wedge = boss.sweepTelegraphing(Act5HSpecs.LASER) || boss.sweepBeaming(Act5HSpecs.LASER);
            evaded |= wedge && rook.evading();
            boolean hitNow =
                    boss.sweepBeaming(Act5HSpecs.LASER) && boss.sweepHit(Act5HSpecs.LASER, SetPiece.SWEPT_WINGMAN);
            rookHits += hitNow && !wasHit ? 1 : 0;
            wasHit = hitNow;
        }
        assertTrue(sweeps >= 4, "sweeps: " + sweeps);
        assertTrue(evaded, "he stepped out of a wedge");
        assertTrue(rookHits == 0, "never in a wedge's beam: " + rookHits + " (the ship's hits: " + shipHits + ")");
    }
}
