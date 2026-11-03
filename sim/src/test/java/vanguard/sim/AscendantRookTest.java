package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The escort (Rook); m10h-gaps.md §1 #12), on the test Ascendant
 * ({@link Act6HSpecs}): Rook shoots the generators first, then a battery only from its open side (a panel facing him
 * is never his target), else the cannon; in phase 2 the sac with the least HP; after the break nothing (round 80: the
 * lifeboat is scenery). He
 * steps out of a told spinal lance's line and out of the bile lobs' marks like the ship.
 */
class AscendantRookTest {
    private static final Hitbox ROOK = new Hitbox(30, 26);
    private static final double LIMIT = 20;
    private static final double HALF = 15;

    @Test
    void theGeneratorsFirstABatteryFromItsOpenSideOnlyElseTheCannon() {
        SetPiece boss = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled().piece;
        double rx = 240;
        double ry = 80;
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act6HSpecs.GEN_L, rx, ry));
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act6HSpecs.GEN_R, rx, ry));
        assertEquals(PartPicks.REST, PartPicks.rank(boss, Act6HSpecs.CANNON, rx, ry));
        PartMirrors mirrors = boss.mirrors().orElseThrow();
        int open = 0;
        int closed = 0;
        for (int p : Act6HSpecs.BATTERIES) {
            for (double x = 30; x <= 450; x += 30) {
                boolean meets = mirrors.meets(p, boss.partX(p) - x, boss.partY(p) - ry);
                int rank = PartPicks.rank(boss, p, x, ry);
                assertEquals(meets ? PartPicks.SKIP : PartPicks.OPEN, rank, "battery " + p + " from x " + x);
                open += meets ? 0 : 1;
                closed += meets ? 1 : 0;
            }
        }
        assertTrue(open > 0 && closed > 0, "open from one side, closed from the other: " + open + "/" + closed);
        // The left face's batteries are open from the right, closed from the left.
        assertEquals(PartPicks.SKIP, PartPicks.rank(boss, Act6HSpecs.BAT_1, 40, ry));
        assertEquals(PartPicks.OPEN, PartPicks.rank(boss, Act6HSpecs.BAT_1, 440, ry));
    }

    @Test
    void inPhaseTwoTheWeakestSacThenAfterTheBreakNothing() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled().toEruption().pastEruption();
        SetPiece boss = rig.piece;
        assertEquals(Act6HSpecs.SAC_1, PartPicks.weakest(boss), "the first of equals");
        boss.damagePart(Act6HSpecs.SAC_3, 200);
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act6HSpecs.SAC_3, 240, 80));
        assertEquals(PartPicks.REST, PartPicks.rank(boss, Act6HSpecs.SAC_1, 240, 80));
        assertTrue(PartPicks.broodUp(new SetPiece[] {boss}));
        for (int p : Act6HSpecs.PHASE_2) {
            rig.kill(p);
        }
        boss.breakHull(0);
        assertFalse(PartPicks.broodUp(new SetPiece[] {boss}));
        for (int p = 0; p < boss.partCount(); p++) {
            assertTrue(boss.partWrecked(p) || boss.partShielded(p), "no part for him: " + p);
        }
    }

    @Test
    void everyOtherBossKeepsHisNearestPartPick() {
        assertFalse(PartPicks.applies(new SetPiece(Act6FSpecs.PLAYED)));
        assertFalse(PartPicks.applies(new SetPiece(PartHSpecs.spire())));
    }

    @Test
    void inTheFightHisTargetFollowsThePicks() {
        WingmanSpec.Ai wide = new WingmanSpec.Ai(
                WingmanTest.AI.wing(),
                WingmanTest.AI.wide(),
                WingmanTest.AI.trail(),
                WingmanTest.AI.glideSeconds(),
                WingmanTest.AI.swapSeconds(),
                WingmanTest.AI.flankDistance(),
                WingmanTest.AI.reactionSeconds(),
                WingmanTest.AI.dodgeInterval(),
                WingmanTest.AI.lookAhead(),
                WingmanTest.AI.clearance(),
                WingmanTest.AI.dodgeStep(),
                WingmanTest.AI.reacts(),
                Math.toRadians(75),
                700,
                WingmanTest.AI.recentHitSeconds());
        WingmanSpec rook = new WingmanSpec(WingmanSpec.Side.RIGHT, 80, WingmanTest.CRAFT, wide, WingmanTest.GUN);
        Sortie sortie = Act6HSpecs.sortie(Act6HSpecs.QUIET, TestSpecs.LOADOUT.withWingman(rook), TestSpecs.RULES);
        SetPiece boss = Act6HSpecs.settle(sortie, 0);
        Wingman wingman = sortie.wingman().orElseThrow();
        EscapeRunTest.stepSeconds(sortie, 1);
        int part = part(wingman.target());
        assertTrue(part == Act6HSpecs.GEN_L || part == Act6HSpecs.GEN_R, "a generator first: " + part);
        sortie.destroyPart(0, Act6HSpecs.GEN_L);
        sortie.destroyPart(0, Act6HSpecs.GEN_R);
        EscapeRunTest.stepSeconds(sortie, 1);
        part = part(wingman.target());
        assertTrue(part >= 0, "a part");
        if (Act6HSpecs.BATTERIES.contains(part)) {
            assertFalse(
                    boss.mirrors()
                            .orElseThrow()
                            .meets(part, boss.partX(part) - wingman.x(), boss.partY(part) - wingman.y()),
                    "a battery from its open side only");
        } else {
            assertEquals(Act6HSpecs.CANNON, part);
        }
        for (int p : Act6HSpecs.PHASE_1) {
            sortie.destroyPart(0, p);
        }
        EscapeRunTest.stepSeconds(sortie, 4.2);
        boss.damagePart(Act6HSpecs.SAC_2, 300);
        EscapeRunTest.stepSeconds(sortie, 0.5);
        assertEquals(Act6HSpecs.SAC_2, part(wingman.target()), "the weakest sac");
        for (int p : Act6HSpecs.PHASE_2) {
            sortie.destroyPart(0, p);
            sortie.step(0);
        }
        EscapeRunTest.stepSeconds(sortie, 3.5);
        assertEquals(-1, part(wingman.target()), "not the lifeboat (scenery)");
    }

    /** The set piece part a target serial names; -1 for none or a unit. */
    private static int part(int serial) {
        int part = serial - PlayerFire.PART_SERIAL;
        return serial < 0 || part < 0 || part >= SetPieceSpec.MAX_PARTS ? -1 : part;
    }

    @Test
    void heStepsOutOfAToldLance() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        BossLines lines = rig.piece.lines().orElseThrow();
        for (int i = 0; i < SimStep.ticks(120) && !lines.telegraphing(); i++) {
            rig.step();
        }
        assertTrue(lines.telegraphing(), "a lance told");
        RookHazards hazards = new RookHazards(ROOK, LIMIT);
        double gx = lines.fromX() + lines.dirX() * 150;
        double gy = lines.fromY() + lines.dirY() * 150;
        hazards.at(gx, gy);
        assertTrue(hazards.clear(gx, gy, new SetPiece[] {rig.piece}, null), "on the line: he moves");
        double dx = hazards.x() - lines.fromX();
        double dy = hazards.y() - lines.fromY();
        double off = Math.abs(dx * lines.dirY() - dy * lines.dirX());
        assertTrue(off >= lines.bandPx() / 2 + HALF + RookHazards.GAP, "outside the band: " + off);
    }

    @Test
    void heStepsOutOfTheLobsMarks() {
        Pool<Lob> lobs = new Pool<>(8, Lob::new, Lob[]::new);
        lobs.obtain().launch(Act6HSpecs.BILE, 240, 400, 200, 150);
        lobs.obtain().launch(Act6HSpecs.BILE, 240, 400, 260, 150);
        RookHazards hazards = new RookHazards(ROOK, LIMIT);
        hazards.at(230, 150);
        assertTrue(hazards.clearOfLobs(230, 150, lobs), "in a mark: he moves");
        double out = 20 + HALF + RookHazards.GAP;
        assertTrue(Math.hypot(hazards.x() - 200, hazards.y() - 150) >= out - 1e-6, "out of the first");
        assertTrue(Math.hypot(hazards.x() - 260, hazards.y() - 150) >= out - 1e-6, "out of the second");
        assertFalse(hazards.clearOfLobs(400, 300, lobs), "clear of them: he stays");
    }
}
