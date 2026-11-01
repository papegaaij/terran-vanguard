package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1a ({@link PartMirrors}; design/enemies/bosses/ascendant, the mirror batteries; design/enemies,
 * Reflect; m10h-gaps.md §1 #4): each battery stands behind a panel facing 45° to its own side, turning within ±20° of
 * that toward the ship at 30°/s; its 120° arc meets shots from its own side and the front, so a face is open from the
 * far side; the player's own shots bounce back as magenta bullets, Rook's are absorbed; a beam heats it (no damage to
 * the battery) until it breaks at 260, down 3 s, then a 0.4 s shimmer; a breaker's arc shatters it and goes on, any other
 * arc is earthed on it; the EMP Burst shatters all four; shots from above pass.
 */
class PartMirrorsTest {
    private static final double EPS = 1e-9;
    private static final int FIRE = Command.FIRE.bit();

    private static Act6HSpecs.Rig rig() {
        return new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
    }

    @Test
    void eachPanelRestsToItsOwnSideAndTurnsWithinTwentyDegreesTowardTheShip() {
        Act6HSpecs.Rig rig = rig();
        PartMirrors mirrors = rig.piece.mirrors().orElseThrow();
        assertEquals(45, Math.toDegrees(mirrors.facing(Act6HSpecs.BAT_1)), 1, "the left face: down and to the left");
        assertEquals(-45, Math.toDegrees(mirrors.facing(Act6HSpecs.BAT_3)), 1, "the right face: down and to the right");
        assertFalse(mirrors.fitted(Act6HSpecs.CANNON));
        // The ship far to the left at the batteries' height: the left panels turn out to 65°, the right ones in to
        // -25°.
        rig.shipX = 10;
        rig.shipY = rig.piece.partY(Act6HSpecs.BAT_1);
        rig.steps(0.5);
        assertEquals(45 + 15, Math.toDegrees(mirrors.facing(Act6HSpecs.BAT_1)), 1.5, "30°/s");
        rig.steps(0.5);
        assertEquals(65, Math.toDegrees(mirrors.facing(Act6HSpecs.BAT_1)), EPS, "no further than 20°");
        assertEquals(-25, Math.toDegrees(mirrors.facing(Act6HSpecs.BAT_3)), EPS);
    }

    @Test
    void aFaceIsOpenFromTheFarSideOnly() {
        Act6HSpecs.Rig rig = rig();
        PartMirrors mirrors = rig.piece.mirrors().orElseThrow();
        double a = Math.toRadians(40);
        int left = Act6HSpecs.BAT_1;
        assertTrue(mirrors.meets(left, 0, 900), "straight up from below: within its arc");
        assertTrue(mirrors.meets(left, 900 * Math.sin(a), 900 * Math.cos(a)), "from the lower left (its own side)");
        assertFalse(mirrors.meets(left, -900 * Math.sin(a), 900 * Math.cos(a)), "from the lower right: open");
        int right = Act6HSpecs.BAT_3;
        assertTrue(mirrors.meets(right, -900 * Math.sin(a), 900 * Math.cos(a)), "the right face mirrors it");
        assertFalse(mirrors.meets(right, 900 * Math.sin(a), 900 * Math.cos(a)));
        assertFalse(mirrors.meets(left, 0, -900), "from behind");
    }

    @Test
    void aBeamsHeatBreaksItAtTwoSixtyDownThreeSecondsThenItShimmersBack() {
        Act6HSpecs.Rig rig = rig();
        SimEvents events = new SimEvents(64);
        rig.piece.useEvents(events);
        PartMirrors mirrors = rig.piece.mirrors().orElseThrow();
        int p = Act6HSpecs.BAT_2;
        assertFalse(mirrors.heat(p, 130));
        assertFalse(mirrors.heat(p, 129));
        assertTrue(mirrors.standing(p));
        assertTrue(mirrors.heat(p, 1), "260: broken");
        assertEquals(1, events.count(SimEvents.Type.PART_MIRROR_BROKEN));
        assertFalse(mirrors.standing(p));
        assertFalse(mirrors.meets(p, 0, 900), "down: the shots reach the battery");
        events.clear();
        int down = 0;
        while (events.count(SimEvents.Type.PART_MIRROR_REGROWING) == 0) {
            rig.step();
            down++;
        }
        assertEquals(SimStep.ticks(3), down, 1, "down 3 s");
        int shimmer = 0;
        while (!mirrors.standing(p)) {
            rig.step();
            shimmer++;
        }
        assertEquals(SimStep.ticks(0.4), shimmer, 1, "the 0.4 s shimmer");
        assertEquals(0, mirrors.heat(p), EPS, "cold again");
    }

    @Test
    void theEmpShattersAllFourAndEmptiesTheCannonsShield() {
        Act6HSpecs.Rig rig = rig();
        SimEvents events = new SimEvents(64);
        rig.piece.useEvents(events);
        rig.piece.stripShields(SimStep.ticks(3));
        PartMirrors mirrors = rig.piece.mirrors().orElseThrow();
        for (int p : Act6HSpecs.BATTERIES) {
            assertTrue(mirrors.down(p), "shattered: " + p);
        }
        assertEquals(4, events.count(SimEvents.Type.PART_MIRROR_SHATTERED));
        assertEquals(0, rig.piece.shields().orElseThrow().shield(Act6HSpecs.CANNON), EPS);
        double hp = rig.piece.partHp(Act6HSpecs.BAT_1);
        rig.piece.damagePart(Act6HSpecs.BAT_1, 20);
        assertEquals(hp - 20, rig.piece.partHp(Act6HSpecs.BAT_1), EPS);
    }

    @Test
    void rooksShotsAreAbsorbedThePlayersOwnBounce() {
        Act6HSpecs.Rig rig = rig();
        PartMirrors mirrors = rig.piece.mirrors().orElseThrow();
        Armament armament = TestSpecs.LOADOUT.armament();
        SimEvents events = new SimEvents(16);
        int[] bounces = {0};
        Reflections reflections = new Reflections(armament, events, new PlayerFire.Hits() {
            @Override
            public void enemyDestroyed(int index, int mount) {}

            @Override
            public void groundDestroyed(int index) {}

            @Override
            public void triggerReleased(int index) {}

            @Override
            public void mineDestroyed(int index) {}

            @Override
            public void partDestroyed(int piece, int part, int mount) {}

            @Override
            public void unitPartDestroyed(int index, int part, int mount) {}

            @Override
            public boolean bounce(double x, double y, double vx, double vy, double damage) {
                bounces[0]++;
                assertEquals(4, damage, EPS, "a small 4");
                assertEquals(110, Math.hypot(vx, vy), 1e-6, "slow: 110 px/s");
                return true;
            }
        });
        int p = Act6HSpecs.BAT_1;
        Shot rook = new Shot();
        rook.fire(TestSpecs.PULSE, armament.size(), 150, 300, 0);
        assertTrue(reflections.meetPart(rook, mirrors, p));
        assertEquals(1, events.count(SimEvents.Type.MIRROR_ABSORBED), "Rook's shot sparks");
        Shot own = new Shot();
        own.fire(TestSpecs.PULSE, 0, 150, 300, 0);
        assertTrue(reflections.meetPart(own, mirrors, p));
        assertEquals(1, bounces[0], "the player's own shot bounces");
        Shot next = new Shot();
        next.fire(TestSpecs.PULSE, 0, 150, 300, 0);
        assertTrue(reflections.meetPart(next, mirrors, p));
        assertEquals(1, bounces[0], "one bounce a gap: the next absorbed");
        Shot far = new Shot();
        far.fire(TestSpecs.PULSE, 0, 300, 300, Math.toRadians(-40));
        assertFalse(reflections.meetPart(far, mirrors, p), "from the far side it reaches the battery");
    }

    /** The test Ascendant in a sortie with {@code mount}, settled, the ship steered to {@code x} (and up to {@code y}). */
    private static Sortie flown(Armament.Mount mount, double x, double y) {
        Sortie sortie =
                Act6HSpecs.sortie(Act6HSpecs.QUIET, TestSpecs.loadout(mount), TestSpecs.RULES.withInvulnerableShip());
        Act6HSpecs.settle(sortie, 0);
        for (int i = 0; i < SimStep.ticks(4); i++) {
            Ship ship = sortie.ship();
            int move = ship.x() < x - 2 ? Command.RIGHT.bit() : ship.x() > x + 2 ? Command.LEFT.bit() : 0;
            move |= ship.y() < y - 2 ? Command.UP.bit() : ship.y() > y + 2 ? Command.DOWN.bit() : 0;
            sortie.step(move);
        }
        return sortie;
    }

    private static int[] fire(Sortie sortie, double seconds) {
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(FIRE);
            for (int e = 0; e < sortie.events().size(); e++) {
                seen[sortie.events().type(e).ordinal()]++;
            }
        }
        return seen;
    }

    @Test
    void boltsFromBelowItBounceAndAbsorbOnTheSameSideTheBatteryUntouched() {
        WeaponSpec bolt = Act6HSpecs.bolt(2, 0);
        Sortie sortie = flown(new Armament.Mount(Armament.Slot.FRONT, bolt, bolt), 150, Ship.START_Y);
        SetPiece boss = sortie.setPiece(0);
        int[] seen = fire(sortie, 2);
        assertTrue(seen[SimEvents.Type.MIRROR_BOUNCED.ordinal()] > 0, "bounced");
        assertTrue(seen[SimEvents.Type.MIRROR_ABSORBED.ordinal()] > 0, "absorbed between the bounces");
        assertEquals(1100, boss.partHp(Act6HSpecs.BAT_1), EPS, "the battery untouched");
        // The EMP shatters it: the same bolts reach it.
        boss.stripShields(SimStep.ticks(3));
        fire(sortie, 1);
        assertTrue(boss.partHp(Act6HSpecs.BAT_1) < 1100, "down: hit");
    }

    @Test
    void boltsFromTheFarSideReachTheLeftFace() {
        WeaponSpec bolt = Act6HSpecs.bolt(2, -40);
        Sortie sortie = flown(new Armament.Mount(Armament.Slot.FRONT, bolt, bolt), 400, Ship.START_Y);
        SetPiece boss = sortie.setPiece(0);
        // The cannon out of the bolts' line.
        boss.damagePartFlat(Act6HSpecs.CANNON, 10_000);
        int[] seen = fire(sortie, 2);
        double lost = 2 * 1100 - boss.partHp(Act6HSpecs.BAT_1) - boss.partHp(Act6HSpecs.BAT_2);
        assertTrue(lost > 0, "the left face's batteries hit from the far side");
        assertEquals(0, seen[SimEvents.Type.MIRROR_BOUNCED.ordinal()] + seen[SimEvents.Type.MIRROR_ABSORBED.ordinal()]);
    }

    @Test
    void aBeamHeatsThePanelWithoutHurtingTheBatteryUntilItBreaks() {
        Sortie sortie = flown(PartASpecs.beamMount(5), 150, Ship.START_Y);
        SetPiece boss = sortie.setPiece(0);
        int broken = 0;
        int steps = 0;
        while (broken == 0 && steps++ < SimStep.ticks(4)) {
            sortie.step(FIRE);
            broken += sortie.events().count(SimEvents.Type.PART_MIRROR_BROKEN);
            if (broken == 0) {
                assertEquals(1100, boss.partHp(Act6HSpecs.BAT_1), EPS, "no damage while the panel stands");
            }
        }
        assertEquals(1, broken);
        // 260 heat at 130 a second (its reach grows over its first steps): about 2 s.
        assertTrue(steps >= SimStep.ticks(2.0) && steps <= SimStep.ticks(2.5), "about 2 s: " + steps);
        fire(sortie, 0.5);
        assertTrue(boss.partHp(Act6HSpecs.BAT_1) < 1100, "down: the beam burns the battery");
    }

    @Test
    void aBreakersArcShattersThePanelAndGoesOnAPlasmaArcIsEarthed() {
        Sortie tesla = flown(new Armament.Mount(Armament.Slot.FRONT, Act5Specs.tesla(0), Act5Specs.tesla(0)), 185, 250);
        SetPiece boss = tesla.setPiece(0);
        boss.damagePartFlat(Act6HSpecs.CANNON, 10_000);
        int[] seen = fire(tesla, 0.5);
        assertTrue(seen[SimEvents.Type.PART_MIRROR_SHATTERED.ordinal()] > 0, "the breaker shatters it");
        assertTrue(boss.partHp(Act6HSpecs.BAT_2) < 1100, "and goes on to the battery");

        Sortie arc = flown(Act6BSpecs.arcMount(), 185, 250);
        SetPiece other = arc.setPiece(0);
        other.damagePartFlat(Act6HSpecs.CANNON, 10_000);
        int[] earthed = fire(arc, 0.5);
        assertTrue(earthed[SimEvents.Type.MIRROR_ABSORBED.ordinal()] > 0, "the Plasma Arc is earthed on it");
        assertEquals(1100, other.partHp(Act6HSpecs.BAT_2), EPS);
    }
}
