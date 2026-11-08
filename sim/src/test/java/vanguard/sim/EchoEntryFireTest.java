package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G5 (round 93 L.3, the user's boss recipe: "make sure it enters the screen firing real rounds at you";
 * design/enemies/bosses, Boss rush): the echoes' entry fire on the test bosses. A breaching echo's {@link
 * BossSpec.Burrow burrow fire} spits aimed volleys from its breach point through its tell (from its first one), its rise,
 * its open, its bite and its dive; an anchored echo's forming fire ({@link EchoSpec#fireAttack()}) fires
 * while it forms, untouchable; a breach draw's eruption waits for its stage ({@link BreachDraw.Spec#eruptionFrom()}) and its first point is the
 * front ({@link BreachDraw.Spec#frontFirst()}).
 * The originals (without them) fly as recorded (the golden flights).
 */
class EchoEntryFireTest {
    /** Every this many s the burrow volley comes. */
    static final double EVERY = 0.4;

    static SetPieceSpec with(SetPieceSpec piece, BossSpec boss) {
        return new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                Optional.of(boss));
    }

    /** The test colossus's echo (ColossusEchoTest's) with its burrow fire (the vents' burst gun as its volley). */
    static SetPieceSpec burrowing() {
        SetPieceSpec piece = ColossusEchoTest.echo();
        BossSpec boss = piece.boss().orElseThrow();
        return with(
                piece,
                boss.withBreach(Optional.of(boss.breach()
                        .orElseThrow()
                        .withBurrow(Optional.of(new BossSpec.Burrow(BreachHazardsTest.BURST, EVERY, 0))))));
    }

    /** The test sovereign's echo (SovereignEchoTest's) firing its core's fan while it forms. */
    static SetPieceSpec formingFire() {
        SetPieceSpec piece = SovereignEchoTest.echo();
        BossSpec boss = piece.boss().orElseThrow();
        return with(
                piece,
                boss.withEcho(
                        Optional.of(new EchoSpec(SovereignEchoTest.FORM, 1.0).firing(Act5HSpecs.FAN_1, 0.35, 0))));
    }

    @Test
    void theBurrowFiresFromTheFirstTellThroughTheRiseAndTheOpen() {
        Sortie sortie = Act7GSpecs.sortie(BreachHazardsTest.level(burrowing()));
        SetPiece piece = sortie.setPiece(0);
        BreachArena breach = piece.breach().orElseThrow();
        int tell = 0;
        int rise = 0;
        int dive = 0;
        int open = 0;
        boolean first = true;
        int firstTellShots = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            sortie.step(0);
            int fired = 0;
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.ENEMY_FIRED
                        && Math.abs(sortie.events().x(e) - breach.mouthX()) < 1
                        && Math.abs(sortie.events().y(e) - breach.mouthY()) < 1) {
                    fired++;
                }
            }
            switch (breach.phase()) {
                case TELL -> tell += fired;
                case RISE -> rise += fired;
                case DIVE -> dive += fired;
                case OPEN -> open += fired;
                default -> {}
            }
            if (first && breach.phase() == BreachArena.Phase.TELL) {
                firstTellShots += fired;
            }
            first &= breach.breaches() == 0;
        }
        assertTrue(firstTellShots >= 3, "it fires from its first tell: " + firstTellShots);
        // (the still ship is drawn into the mouth by its dive: inside the bullets' 72 px no-fire distance there)
        assertTrue(tell > 0 && rise > 0, "tell " + tell + ", rise " + rise + ", dive " + dive);
        assertTrue(open > 0, "and while the mouth is open: " + open);
    }

    @Test
    void anAnchoredEchoFiresWhileItForms() {
        Sortie sortie = Act7GSpecs.sortie(Act5HSpecs.level(formingFire()));
        SetPiece piece = sortie.setPiece(0);
        int forming = 0;
        for (int i = 0; i < SimStep.ticks(60) && !piece.engaged(); i++) {
            sortie.step(0);
            if (piece.present()) {
                forming += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
                assertTrue(piece.partShielded(Act5HSpecs.CORE), "untouchable while it forms");
            }
        }
        assertTrue(piece.engaged(), "it engages once formed");
        assertTrue(forming >= 6, "it fires while it forms: " + forming);
        Sortie silent = Act7GSpecs.sortie(Act5HSpecs.level(SovereignEchoTest.echo()));
        SetPiece quiet = silent.setPiece(0);
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(60) && !quiet.engaged(); i++) {
            silent.step(0);
            shots += silent.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertEquals(0, shots, "without its forming fire it forms silent (step G1's)");
    }

    @Test
    void theDrawsEruptionWaitsForItsStage() {
        BreachDraw.Spec spec = new BreachDraw.Spec(
                BreachDrawTest.SPEC.points(),
                true,
                BreachDrawTest.SPEC.rearEvery(),
                BreachDrawTest.SPEC.eruptionEvery(),
                BreachDrawTest.SPEC.eruptionNotFirst(),
                1,
                true);
        BreachDraw draw = new BreachDraw(spec, true);
        draw.draws(new SplitMix64(7));
        assertEquals(BreachArena.Pose.FRONT, draw.take(0), "the front first");
        for (int i = 0; i < 40; i++) {
            assertNotEquals(BreachArena.Pose.UNDER, draw.take(0), "no eruption in the first stage");
        }
        int eruptions = 0;
        for (int i = 0; i < 40; i++) {
            eruptions += draw.take(1) == BreachArena.Pose.UNDER ? 1 : 0;
        }
        assertTrue(eruptions > 0, "the eruption in the second stage");
    }

    @Test
    void theEntryFireStepsWithoutAllocating() {
        assertEquals(
                0,
                Allocations.least(
                        () -> Act7GSpecs.sortie(BreachHazardsTest.level(burrowing())),
                        sortie -> Act7GAllocationsTest.fly(sortie, 40)));
        assertEquals(
                0,
                Allocations.least(
                        () -> Act7GSpecs.sortie(Act5HSpecs.level(formingFire())), Act7GAllocationsTest::flySovereign));
    }
}
