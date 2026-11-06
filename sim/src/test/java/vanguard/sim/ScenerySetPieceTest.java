package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part D, step CD1 (design/campaign Level 46, the dying Leviathan; m11d-gaps.md §1 #4, §4): a set piece flown as
 * scenery is no enemy for the counts: it pays no bounty and is no kill, never counts toward the kill ratio's total, its
 * guns are silent, its body has no contact and Rook never picks it; each part shot off drops a small salvage and its
 * death (the vital part first) drops every part's salvage and a medium one.
 */
class ScenerySetPieceTest {
    private static Sortie sortie(boolean scenery, Rules rules, Loadout loadout) {
        LevelScript level = Act7CDSpecs.level(30, 90, List.of(), List.of(), VrellLevel.NONE).toBuilder()
                .setPieces(List.of(Act7CDSpecs.leviathan(Layer.AIR, scenery)))
                .build();
        return new Sortie(1, loadout, level, rules, TestSpecs.FULL_ARMOUR);
    }

    private static Sortie sortie(boolean scenery) {
        return sortie(scenery, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.LOADOUT);
    }

    /** Flies until the piece is on the field (its centre past the top edge). */
    private static SetPiece arrive(Sortie sortie) {
        SetPiece piece = sortie.setPiece(0);
        while (!(piece.present() && piece.y() < PlayField.HEIGHT - 60)) {
            sortie.step(Command.NONE);
        }
        return piece;
    }

    private static int pickups(Sortie sortie, PickupType type) {
        int n = 0;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            n += sortie.pickup(i).type() == type ? 1 : 0;
        }
        return n;
    }

    @Test
    void itIsNoEnemyForTheCountsAndPaysNothing() {
        assertEquals(1, sortie(false).enemyTotal(), "a set piece counts");
        Sortie sortie = sortie(true);
        assertEquals(0, sortie.enemyTotal(), "scenery does not");
        arrive(sortie);
        int credits = sortie.credits();
        sortie.destroyPart(0, 1);
        assertEquals(credits, sortie.credits(), "a part pays nothing");
        assertEquals(1, pickups(sortie, PickupType.SMALL_SALVAGE), "its small salvage");
        sortie.destroyPart(0, 0);
        assertEquals(credits, sortie.credits(), "nor its death");
        assertEquals(0, sortie.kills(), "no kill");
        assertEquals(3, pickups(sortie, PickupType.SMALL_SALVAGE), "every part's salvage at its death");
        assertEquals(1, pickups(sortie, PickupType.MEDIUM_SALVAGE), "and the death's medium");
        assertEquals(0, pickups(sortie, PickupType.LARGE_SALVAGE), "the stat block's drop overridden");
    }

    @Test
    void theBlowholeFirstDropsEverySalvageAtOnce() {
        Sortie sortie = sortie(true);
        arrive(sortie);
        sortie.destroyPart(0, 0);
        assertEquals(3, pickups(sortie, PickupType.SMALL_SALVAGE));
        assertEquals(1, pickups(sortie, PickupType.MEDIUM_SALVAGE));
    }

    @Test
    void itsGunsAreSilentAndItsBodyNeverTouchesTheShip() {
        Sortie piece = sortie(false, TestSpecs.RULES, TestSpecs.LOADOUT);
        Sortie scenery = sortie(true, TestSpecs.RULES, TestSpecs.LOADOUT);
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(17); i++) {
            piece.step(Command.NONE);
            scenery.step(Command.NONE);
            fired += piece.bulletCount();
            assertEquals(0, scenery.bulletCount(), "silent");
        }
        assertTrue(fired > 0, "the same piece fires when it is no scenery");
        Defences hurt = piece.ship().defences();
        Defences whole = scenery.ship().defences();
        assertTrue(hurt.shield() + hurt.armour() < whole.shield() + whole.armour(), "only the piece touched the ship");
        assertEquals(TestSpecs.FULL_ARMOUR, whole.armour(), 1e-9);
    }

    @Test
    void rookNeverPicksIt() {
        Loadout withRook = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        Sortie piece = sortie(false, TestSpecs.RULES.withInvulnerableShip(), withRook);
        Sortie scenery = sortie(true, TestSpecs.RULES.withInvulnerableShip(), withRook);
        boolean picked = false;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            piece.step(Command.NONE);
            scenery.step(Command.NONE);
            picked |= piece.wingman().orElseThrow().target() >= PlayerFire.PART_SERIAL;
            assertTrue(scenery.wingman().orElseThrow().target() < PlayerFire.PART_SERIAL, "never a scenery part");
        }
        assertTrue(picked, "he picks the same piece's parts when it is no scenery");
    }
}
