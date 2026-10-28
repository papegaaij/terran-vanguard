package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part D, step D1 (design/enemies/air/buzzsaw-drone): the dash cycle. A Buzzsaw chases the ship, spins up for its
 * full tell before every dash (its bearing locked at the tell's start), dashes and ricochets off the edges at equal
 * angles up to its bounces, bounces off a large rock and smashes a small one, slows and chases again, and leaves after
 * its last dash; a circle's saws break off 0.4 s apart.
 */
class BuzzsawTest {
    private static LevelScript level(List<WaveSpec> waves) {
        return LevelScript.builder(31, 5)
                .sections(List.of(new Section(60, 120)))
                .waves(waves)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    private static Sortie sortie(List<WaveSpec> waves) {
        return new Sortie(9, TestSpecs.LOADOUT, level(waves), TestSpecs.RULES.withInvulnerableShip(), 60);
    }

    private static Enemy saw(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().dash().isPresent()) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    @Test
    void everyDashFollowsItsFullTellAlongTheBearingLockedAtItsStart() {
        Sortie sortie = sortie(List.of(Act5DSpecs.saws(1, 1, WaveSpec.Edge.LEFT)));
        List<Integer> tells = new ArrayList<>();
        List<Integer> dashes = new ArrayList<>();
        double lockedX = Double.NaN;
        double lockedY = Double.NaN;
        int weave = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            // The ship moves during the tells: the dash keeps the bearing at the tell's start.
            weave = (i / SimStep.ticks(0.3)) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit();
            sortie.step(weave);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.DASH_TELL) {
                    tells.add(i);
                    Enemy u = saw(sortie);
                    double dx = sortie.ship().x() - u.x();
                    double dy = sortie.ship().y() - u.y();
                    double length = Math.hypot(dx, dy);
                    lockedX = dx / length;
                    lockedY = dy / length;
                } else if (events.type(e) == SimEvents.Type.DASH_START) {
                    dashes.add(i);
                    Enemy u = saw(sortie);
                    assertEquals(lockedX, u.dashing.dirX(), 1e-9, "the bearing locked at the tell's start");
                    assertEquals(lockedY, u.dashing.dirY(), 1e-9);
                }
            }
        }
        assertEquals(2, dashes.size(), "two dashes, then it leaves");
        assertEquals(2, tells.size());
        for (int d = 0; d < 2; d++) {
            assertEquals(SimStep.ticks(0.6), dashes.get(d) - tells.get(d), "the full 0.6 s tell before each dash");
        }
        assertEquals(null, saw(sortie), "gone through the nearest edge after its second dash");
    }

    @Test
    void itRicochetsOffTheEdgesAtEqualAnglesUpToItsBounces() {
        Sortie sortie = sortie(List.of(Act5DSpecs.saws(1, 1, WaveSpec.Edge.RIGHT)));
        int bounces = 0;
        double beforeX = 0;
        double beforeY = 0;
        boolean dashing = false;
        for (int i = 0; i < SimStep.ticks(12) && bounces < 3; i++) {
            Enemy before = saw(sortie);
            if (before != null && before.dashing()) {
                beforeX = before.dashing.dirX();
                beforeY = before.dashing.dirY();
                dashing = true;
            }
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.DASH_BOUNCE && dashing) {
                    assertEquals(0, events.value(e), "off an edge");
                    Enemy u = saw(sortie);
                    double afterX = u.dashing.dirX();
                    double afterY = u.dashing.dirY();
                    boolean side = Math.abs(afterX + beforeX) < 1e-9 && Math.abs(afterY - beforeY) < 1e-9;
                    boolean end = Math.abs(afterY + beforeY) < 1e-9 && Math.abs(afterX - beforeX) < 1e-9;
                    assertTrue(side || end, "equal angles: " + beforeX + "," + beforeY + " → " + afterX + "," + afterY);
                    assertTrue(
                            u.x() >= 13
                                    && u.x() <= PlayField.WIDTH - 13
                                    && u.y() >= 13
                                    && u.y() <= PlayField.HEIGHT - 13,
                            "kept inside the field");
                    bounces++;
                }
            }
        }
        assertEquals(3, bounces, "three bounces in its first dash");
        Enemy u = saw(sortie);
        assertEquals(Dashing.Stage.SLOW, u.dashing.stage(), "its bounces spent, it slows");
    }

    /** Steps until the saw dashes; returns it. */
    private static Enemy untilDash(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(0);
            Enemy u = saw(sortie);
            if (u != null && u.dashing()) {
                return u;
            }
        }
        throw new AssertionError("no dash");
    }

    /** A rock 60 px ahead of {@code u} on its dash, nearly still. */
    private static Debris rockAhead(Sortie sortie, Enemy u, HazardSpec.DebrisSpec spec) {
        Debris chunk = sortie.debris.obtain();
        double x = u.x() + u.dashing.dirX() * 60;
        double y = u.y() + u.dashing.dirY() * 60;
        chunk.enter(spec, 77, x, y, 0, -0.01);
        return chunk;
    }

    @Test
    void aLargeRockBouncesTheDash() {
        Sortie sortie = sortie(List.of(Act5DSpecs.saws(1, 1, WaveSpec.Edge.LEFT)));
        Enemy u = untilDash(sortie);
        HazardSpec.DebrisSpec large = new HazardSpec.DebrisSpec(
                0, 0, "large-a", new Hitbox(64, 64), 0, -0.01, Double.POSITIVE_INFINITY, 15, 0);
        Debris rock = rockAhead(sortie, u, large);
        double inX = u.dashing.dirX();
        double inY = u.dashing.dirY();
        boolean bounced = false;
        for (int i = 0; i < SimStep.ticks(0.5) && !bounced; i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                bounced |= events.type(e) == SimEvents.Type.DASH_BOUNCE && events.value(e) == 1;
            }
        }
        assertTrue(bounced, "it bounced off the rock");
        double nx = u.x() - rock.x();
        double ny = u.y() - rock.y();
        double length = Math.hypot(nx, ny);
        double out = (u.dashing.dirX() * nx + u.dashing.dirY() * ny) / length;
        double in = (inX * nx + inY * ny) / length;
        assertTrue(out > 0, "away from the rock after the bounce");
        assertEquals(-in, out, 0.05, "equal angles off the rock's outline");
        assertEquals(1, sortie.debrisCount(), "the large rock stands");
    }

    @Test
    void aSmallRockBreaksAndDoesNotDeflectTheDash() {
        Sortie sortie = sortie(List.of(Act5DSpecs.saws(1, 1, WaveSpec.Edge.LEFT)));
        Enemy u = untilDash(sortie);
        HazardSpec.DebrisSpec small =
                new HazardSpec.DebrisSpec(0, 0, "small-a", new Hitbox(28, 28), 0, -0.01, 12, 0, 0);
        rockAhead(sortie, u, small);
        double inX = u.dashing.dirX();
        double inY = u.dashing.dirY();
        int smashed = 0;
        for (int i = 0; i < SimStep.ticks(0.4); i++) {
            sortie.step(0);
            smashed += sortie.events().count(SimEvents.Type.DASH_SMASH);
        }
        assertEquals(1, smashed);
        assertEquals(0, sortie.debrisCount(), "the small rock broke");
        if (u.dashing()) {
            boolean edge = Math.abs(Math.abs(u.dashing.dirX()) - Math.abs(inX)) < 1e-9
                    && Math.abs(Math.abs(u.dashing.dirY()) - Math.abs(inY)) < 1e-9;
            assertTrue(edge, "not deflected by the small rock");
        }
    }

    @Test
    void aCirclesSawsBreakOffOneByOneTheirTellsApart() {
        WaveSpec circle = Act5DSpecs.wave(
                        1,
                        WaveSpec.Formation.CIRCLE,
                        Act5DSpecs.CIRCLE_SAW,
                        4,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE,
                        Optional.of(2.0),
                        Optional.empty())
                .withAt(new WaveSpec.At(240, 160));
        Sortie sortie = sortie(List.of(circle));
        List<Integer> tells = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(0);
            for (int k = 0; k < sortie.events().count(SimEvents.Type.DASH_TELL); k++) {
                tells.add(i);
            }
        }
        assertTrue(tells.size() >= 4, "every saw tells: " + tells);
        for (int k = 1; k < 4; k++) {
            assertEquals(SimStep.ticks(0.4), tells.get(k) - tells.get(k - 1), 1, "0.4 s apart: " + tells);
        }
    }

    @Test
    void aDashKeepsItsTellAtHalfASecondOrMore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.Dash(Math.toRadians(120), 2.5, 0.4, 40, 360, 3, 0.5, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5DSpecs.BUZZSAW.withGun(Optional.of(EnemyGun.aimed(1, 1, 1, 100, 4, false))),
                "a dash has no gun");
    }

    @Test
    void itReplays() {
        List<WaveSpec> waves = List.of(Act5DSpecs.saws(1, 4, WaveSpec.Edge.ALTERNATING));
        Sortie one = sortie(waves);
        Sortie two = sortie(waves);
        for (Sortie sortie : List.of(one, two)) {
            for (int i = 0; i < SimStep.ticks(15); i++) {
                sortie.step(Command.FIRE.bit() | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
            }
        }
        assertEquals(one.stateHash(), two.stateHash());
    }
}
