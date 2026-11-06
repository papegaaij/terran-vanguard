package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/enemies/bosses, Boss rush; Q6 a; Level 49's engine): three small bosses, each holding a
 * gate of its own; the refill between the fights, a checkpoint per fight that restores the fight in progress (the
 * earlier bosses staying dead), one boss time from the first bar to the last kill against the rush's par.
 */
class BossRushTest {
    /** 100 px/s to 10 s, gates halting at 12, 22 and 32 s with 120 px/s eased sections between, on to 45 s. */
    static final List<Section> SECTIONS = List.of(
            new Section(10, 100),
            new Section(12, 0, true),
            new Section(20, 120, false, 2, 2),
            new Section(22, 0, true),
            new Section(30, 120, false, 2, 2),
            new Section(32, 0, true),
            new Section(45, 120, false, 2, 2));

    static final VrellLevel.Rush RUSH = new VrellLevel.Rush(true, true, 30);

    /** A small boss arriving at {@code arrive} s: Act 6 G's stand-in piece under its own slug. */
    static SetPieceSpec echo(String slug, double arrive) {
        SetPieceSpec piece = Act6GSpecs.piece(arrive);
        return new SetPieceSpec(
                slug,
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                piece.boss());
    }

    static LevelScript rush(Optional<VrellLevel.Rush> rush) {
        return LevelScript.builder(49, 7)
                .sections(SECTIONS)
                .setPieces(List.of(echo("carrier", 11.5), echo("colossus", 21.5), echo("sovereign", 31.5)))
                .objectives(ObjectiveSpec.NONE)
                .vrell(VrellLevel.NONE.withRush(rush))
                .build();
    }

    static Sortie sortie() {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(Act4SpecialsTest.airstrike(2)),
                rush(Optional.of(RUSH)),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps until fight {@code f} begins (its {@link SimEvents.Type#FIGHT_NEXT}); the refills on the way. */
    private static int untilFight(Sortie sortie, int f) {
        int refills = 0;
        for (int i = 0; i < SimStep.ticks(60); i++) {
            sortie.step(0);
            refills += sortie.events().count(SimEvents.Type.BOSS_REFILL);
            if (sortie.events().count(SimEvents.Type.FIGHT_NEXT) > 0) {
                assertEquals(f, sortie.events().value(index(sortie, SimEvents.Type.FIGHT_NEXT)));
                return refills;
            }
        }
        throw new AssertionError("fight " + f + " never began");
    }

    private static int index(Sortie sortie, SimEvents.Type type) {
        for (int i = 0; i < sortie.events().size(); i++) {
            if (sortie.events().type(i) == type) {
                return i;
            }
        }
        return -1;
    }

    private static void hurt(Sortie sortie, double points) {
        Defences defences = sortie.ship().defences();
        defences.restore(defences.shield() / 2, defences.armour() - points, points);
    }

    @Test
    void eachBossHoldsAGateOfItsOwn() {
        Sortie sortie = sortie();
        BossFights fights = sortie.bosses();
        assertTrue(fights.rush());
        assertEquals(3, fights.fights());
        assertEquals(3, sortie.gates());
        for (int f = 0; f < 3; f++) {
            assertEquals(f, fights.fightPiece(f));
        }
        assertTrue(fights.everyDifficulty(), "the rush's checkpoint is offered on hard too");
        assertTrue(fights.noFlash());
        assertEquals(-1, fights.fight());
        Sortie plain = TestSpecs.sortie(Act6GSpecs.pieceLevel(List.of()));
        assertFalse(plain.bosses().rush());
        assertFalse(plain.bosses().everyDifficulty());
        assertFalse(plain.bosses().noFlash());
    }

    @Test
    void eachFightRecordsTheCheckpointAndTheLaterOnesRefillFirst() {
        Sortie sortie = sortie();
        assertEquals(0, untilFight(sortie, 0), "no refill before the first fight");
        assertTrue(sortie.bossCheckpoint());
        assertEquals(0, sortie.bosses().fight());
        assertEquals(0, sortie.gate());
        hurt(sortie, 20);
        Defences defences = sortie.ship().defences();
        assertTrue(defences.armour() < defences.maxArmour());
        sortie.step(Command.SPECIAL.bit());
        assertEquals(1, sortie.special().charges(), "a charge called");
        double clock = sortie.levelSeconds();
        Act6BSpecs.run(sortie, SimStep.ticks(2), 0, SimEvents.Type.GATE_OPENED);
        assertEquals(clock, sortie.levelSeconds(), 1e-9, "the clock stands while the boss lives");
        sortie.objectivePasses.wreck(0, 0);
        assertEquals(1, untilFight(sortie, 1), "the refill as the second fight begins");
        assertEquals(1, sortie.bosses().fight());
        assertEquals(defences.maxArmour(), defences.armour(), "armour to full");
        assertEquals(defences.shieldCapacity(), defences.shield(), 1e-9, "shield to full");
        assertEquals(2, sortie.special().charges(), "the charges back to the level's start");
        sortie.objectivePasses.wreck(1, 0);
        assertEquals(1, untilFight(sortie, 2));
        assertEquals(2, sortie.bosses().fight());
    }

    @Test
    void aRetryRestartsTheFightInProgressWithTheEarlierBossesDead() {
        Sortie sortie = sortie();
        untilFight(sortie, 0);
        sortie.objectivePasses.wreck(0, 0);
        double firstKill = sortie.setPiece(0).killSeconds();
        assertTrue(firstKill >= 0);
        untilFight(sortie, 1);
        double clock = sortie.levelSeconds();
        long recorded = sortie.stateHash();
        Act6BSpecs.run(sortie, SimStep.ticks(1), 0, SimEvents.Type.GATE_OPENED);
        hurt(sortie, 25);
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_RETRY));
        assertEquals(1, sortie.gate(), "the second fight's gate");
        assertEquals(clock, sortie.levelSeconds(), 2 * SimStep.SECONDS);
        assertTrue(sortie.setPiece(0).destroyed(), "the first boss stays dead");
        assertFalse(sortie.setPiece(0).present());
        assertEquals(firstKill, sortie.setPiece(0).killSeconds(), 1e-9, "its kill time kept");
        assertFalse(sortie.setPiece(1).destroyed(), "the second boss fights again");
        assertEquals(
                sortie.ship().defences().maxArmour(), sortie.ship().defences().armour(), "the fight starts full");
        assertEquals(1, sortie.bosses().fight());
        assertEquals(0, sortie.events().count(SimEvents.Type.FIGHT_NEXT), "the fight is not begun twice");
        assertTrue(recorded != 0);
        sortie.objectivePasses.wreck(1, 0);
        assertEquals(1, untilFight(sortie, 2), "the third fight refills as ever");
        sortie.objectivePasses.wreck(2, 0);
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(2, sortie.gate(), "back to the third fight");
        assertTrue(sortie.setPiece(0).destroyed() && sortie.setPiece(1).destroyed());
        assertFalse(sortie.setPiece(2).destroyed());
    }

    @Test
    void theRushHasOneBossTimeFromTheFirstBarToTheLastKill() {
        Sortie sortie = sortie();
        untilFight(sortie, 0);
        Act6BSpecs.run(sortie, SimStep.ticks(1), 0, SimEvents.Type.GATE_OPENED);
        double firstBar = sortie.setPiece(0).bossSeconds();
        assertTrue(firstBar >= 0, "the first boss arrived");
        double start = sortie.realSeconds() - firstBar;
        sortie.objectivePasses.wreck(0, 0);
        assertTrue(sortie.result().bossTime().killSeconds() < 0, "no time before the last kill");
        untilFight(sortie, 1);
        sortie.objectivePasses.wreck(1, 0);
        untilFight(sortie, 2);
        Act6BSpecs.run(sortie, SimStep.ticks(3), 0, SimEvents.Type.GATE_OPENED);
        sortie.objectivePasses.wreck(2, 0);
        double end = sortie.realSeconds();
        LevelResult.BossTime time = sortie.result().bossTime();
        assertEquals(RUSH.parSeconds(), time.parSeconds(), "the rush's one par");
        assertEquals(end - start, time.killSeconds(), 2 * SimStep.SECONDS, "first bar to last kill, gaps included");
        assertTrue(time.killSeconds() > sortie.setPiece(2).killSeconds() + 20, "the gaps count");
    }

    @Test
    void aRushIsDeterministic() {
        Sortie a = sortie();
        Sortie b = sortie();
        for (int i = 0; i < SimStep.ticks(20); i++) {
            a.step(0);
            b.step(0);
            if (i == SimStep.ticks(14)) {
                a.objectivePasses.wreck(0, 0);
                b.objectivePasses.wreck(0, 0);
            }
        }
        assertEquals(a.stateHash(), b.stateHash());
    }

    @Test
    void aRushsGatesAreHeldByItsBosses() {
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(49, 7)
                .sections(SECTIONS)
                .setPieces(List.of(echo("carrier", 11.5), echo("colossus", 21.5)))
                .vrell(VrellLevel.NONE.withRush(Optional.of(RUSH)))
                .build());
    }
}
