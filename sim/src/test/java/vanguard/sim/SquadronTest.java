package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1a: the squadron mid-boss ({@link Squadron}; design/enemies/bosses/honour-guard) on the test guard
 * of {@link Act6FSpecs}: one set piece whose parts are the three aces, absent and untouchable until they join, each
 * first entry told at its own edge, the joins by time, HP share and the first death per difficulty, the one bar, each
 * ace paid as it dies, the last death ending the piece and the clock's halt, the contact, the shields, the tumbling
 * wrecks, the hash, the retry from the checkpoint and the allocation.
 */
class SquadronTest {
    private static final double EPS = 1e-9;
    private static final int H = Act6FSpecs.HARKER;
    private static final int V = Act6FSpecs.VESPER;
    private static final int K = Act6FSpecs.KESTREL;

    /** The boss's seconds at which each ace's tell and crossing first came, stepping {@code seconds}. */
    private static double[][] joins(Act6FSpecs.Rig rig, double seconds) {
        double[][] at = {{-1, -1, -1}, {-1, -1, -1}};
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            for (int value : rig.values(SimEvents.Type.ACE_TELL)) {
                at[0][Squadron.tellAce(value)] = rig.piece.bossSeconds();
            }
            for (int value : rig.values(SimEvents.Type.ACE_JOINED)) {
                at[1][value] = rig.piece.bossSeconds();
            }
        }
        return at;
    }

    @Test
    void theAcesJoinInTurnEachToldAtItsOwnEdge() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        rig.step();
        assertTrue(rig.piece.present() && rig.piece.engaged(), "parked and engaged at the sting");
        Squadron squadron = rig.squadron;
        for (int a = 0; a < 3; a++) {
            assertEquals(Squadron.State.WAITING, squadron.state(a));
            assertTrue(rig.piece.partShielded(squadron.part(a)));
            assertFalse(rig.piece.partOnField(squadron.part(a)));
        }
        List<Integer> tell = rig.until(SimEvents.Type.ACE_TELL, 2);
        assertEquals(1, rig.piece.bossSeconds(), 0.02, "Harker's tell 1 s after the sting");
        assertEquals(H, Squadron.tellAce(tell.getFirst()));
        assertEquals(WarningEdge.TOP, Squadron.tellEdge(tell.getFirst()));
        assertEquals(PlayField.HEIGHT, rig.places.getFirst()[1], EPS, "on the top edge");
        assertEquals(Act6FSpecs.HARKER_STATION.x(), rig.places.getFirst()[0], EPS, "where he will cross");
        double told = rig.piece.bossSeconds();
        rig.until(SimEvents.Type.ACE_JOINED, 3);
        assertEquals(2.5, rig.piece.bossSeconds() - told, 0.02, "he crosses 2.5 s after his tell");
        assertTrue(rig.piece.partShielded(H), "untouchable while he flies in");
        double joined = rig.piece.bossSeconds();
        while (!squadron.touchable(H)) {
            assertTrue(rig.piece.partShielded(H));
            rig.step();
        }
        assertTrue(rig.piece.bossSeconds() - joined < 1, "wholly on the field within a second");
        assertFalse(rig.piece.partShielded(H));
        assertTrue(rig.piece.partArmed(H));
        assertFalse(rig.piece.partArmed(V), "an absent ace never fires");
        double[][] at = joins(rig, 45);
        assertEquals(joined + 20, at[0][V], 0.02, "Vesper told 20 s after Harker crossed");
        assertEquals(WarningEdge.RIGHT, squadron.edge(V), "the side away from Harker (at x 160)");
        assertEquals(at[0][V] + 2.5, at[1][V], 0.02);
        assertEquals(at[1][V] + 15, at[0][K], 0.02, "Kestrel told 15 s after Vesper crossed");
        assertEquals(WarningEdge.BOTTOM, squadron.edge(K));
        assertEquals(3, squadron.flyingCount());
        assertEquals(1, rig.piece.barShare(), EPS, "nobody hit: the bar full");
    }

    @Test
    void vesperJoinsAtHarkersShareAndKestrelAtTheFirstDeath() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        while (!rig.squadron.touchable(H)) {
            rig.step();
        }
        // The shield first (60), then his HP to just above 2/3.
        rig.piece.damagePartFlat(H, Act6FSpecs.SHIELD.hp() + Act6FSpecs.HP / 3 - 2);
        rig.steps(0.5);
        assertEquals(Squadron.State.WAITING, rig.squadron.state(V));
        rig.piece.damagePartFlat(H, 4);
        rig.step();
        assertEquals(Squadron.State.TELLING, rig.squadron.state(V), "told the step his share fell to 2/3");
        assertEquals(Squadron.State.WAITING, rig.squadron.state(K));
        Act6FSpecs.kill(rig.piece, H);
        rig.step();
        assertEquals(List.of(H), rig.values(SimEvents.Type.ACE_DOWN));
        assertEquals(Squadron.State.TELLING, rig.squadron.state(K), "told at the first death");
        assertEquals(1, rig.squadron.deaths());
        assertEquals(0, rig.squadron.flyingCount());
        assertFalse(rig.piece.destroyed(), "two aces to come");
    }

    @Test
    void theJoinsComeEarlierOnHardAndLaterOnEasy() {
        double[][] expected = {{25, 20}, {20, 15}, {15, 10}};
        SquadronSpec[] specs = {Act6FSpecs.EASY, Act6FSpecs.MEDIUM, Act6FSpecs.HARD};
        for (int d = 0; d < 3; d++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.guard(specs[d]));
            double[][] at = joins(rig, 60);
            assertEquals(3.5, at[1][H], 0.02, "Harker crosses 3.5 s after the sting");
            assertEquals(at[1][H] + expected[d][0], at[0][V], 0.02, "Vesper's time");
            assertEquals(at[1][V] + expected[d][1], at[0][K], 0.02, "Kestrel's time");
        }
        // The shares: easy at Harker's half.
        Act6FSpecs.Rig easy = new Act6FSpecs.Rig(Act6FSpecs.guard(Act6FSpecs.EASY));
        while (!easy.squadron.touchable(H)) {
            easy.step();
        }
        easy.piece.damagePartFlat(H, Act6FSpecs.SHIELD.hp() + Act6FSpecs.HP * 0.4);
        easy.step();
        assertEquals(Squadron.State.WAITING, easy.squadron.state(V), "60 % left: not yet on easy");
        easy.piece.damagePartFlat(H, Act6FSpecs.HP * 0.1 + 1);
        easy.step();
        assertEquals(Squadron.State.TELLING, easy.squadron.state(V));
    }

    @Test
    void anAbsentAceIsNotThereAndTheBarSumsTheThree() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        while (!rig.squadron.touchable(H)) {
            rig.step();
        }
        SetPiece piece = rig.piece;
        Hitbox shot = new Hitbox(6, 12);
        assertFalse(piece.partTouches(V, shot, piece.partX(V), piece.partY(V)), "absent: nothing touches it");
        assertFalse(piece.partInBlast(V, piece.partX(V), piece.partY(V), 100));
        assertEquals(Double.POSITIVE_INFINITY, piece.partColumn(V, piece.partX(V), 20, 0));
        assertTrue(piece.partTouches(H, shot, piece.partX(H), piece.partY(H)));
        assertEquals(piece.partX(H), rig.squadron.x(H), EPS, "a part is where its ace flies");
        assertEquals(piece.renderX(0) + piece.partOffsetX(H), piece.partX(H), EPS);
        assertEquals(Act6FSpecs.PARK_Y, piece.y(), EPS, "the piece parked above the field");
        piece.damagePartFlat(H, 800);
        assertEquals(1600 - 740, piece.partHp(H), EPS, "the shield took 60");
        assertEquals((4800 - 740) / 4800.0, piece.barShare(), EPS, "one bar over the three");
        rig.steps(30);
        assertTrue(rig.squadron.joined(V));
        assertEquals((4800 - 740) / 4800.0, piece.barShare(), EPS, "the bar's sum stays as aces join");
    }

    @Test
    void eachAcePaysAsItDiesAndTheLastEndsTheDuelAndTheHalt() {
        Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.GUARD, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = Act6FSpecs.boss(sortie);
        Squadron squadron = Act6FSpecs.squadron(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
        assertTrue(sortie.bossCheckpoint());
        while (!squadron.touchable(H)) {
            sortie.step(0);
        }
        int credits = sortie.credits();
        Act6FSpecs.kill(sortie, H);
        assertEquals(1, sortie.events().count(SimEvents.Type.PART_DESTROYED), "Harker paid");
        assertEquals(0, sortie.events().count(SimEvents.Type.BOSS_DESTROYED));
        sortie.step(0);
        assertTrue(sortie.credits() > credits, "Harker paid as he died");
        assertFalse(boss.destroyed());
        assertEquals(1, sortie.events().count(SimEvents.Type.ACE_DOWN), "his wreck tumbles");
        // The first death tells both the others; they fly in.
        for (int i = 0; i < SimStep.ticks(6) && !(squadron.touchable(V) && squadron.touchable(K)); i++) {
            sortie.step(0);
        }
        assertTrue(squadron.touchable(V) && squadron.touchable(K));
        double halted = sortie.levelSeconds();
        assertEquals(Act6FSpecs.ARENA_END, halted, 0.05, "the clock halts in the arena");
        Act6FSpecs.kill(sortie, V);
        MawSpecs.steps(sortie, 1, 0);
        assertEquals(halted, sortie.levelSeconds(), EPS, "halted while an ace lives");
        int before = sortie.credits();
        double kx = squadron.x(K);
        double ky = squadron.y(K);
        Act6FSpecs.kill(sortie, K);
        assertTrue(boss.destroyed(), "the last death ends the piece");
        assertEquals(1, sortie.events().count(SimEvents.Type.PART_DESTROYED), "Kestrel paid, nobody twice");
        assertTrue(sortie.credits() > before);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_DESTROYED));
        assertEquals(kx, boss.x(), EPS, "its death plays where Kestrel died");
        assertEquals(ky, boss.y(), EPS);
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.ACE_DOWN), "the last ace's wreck after the kill");
        assertEquals(Squadron.State.DOWN, squadron.state(K));
        assertTrue(boss.killSeconds() > 0);
        MawSpecs.steps(sortie, 3.5, 0);
        assertEquals(halted, sortie.levelSeconds(), EPS, "held 4 s after the kill");
        MawSpecs.steps(sortie, 1.5, 0);
        assertTrue(sortie.levelSeconds() > halted, "the clock runs again");
    }

    @Test
    void aSquadronIsHashedAndARetryFromTheCheckpointRepeatsTheDuelExactly() {
        Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.GUARD, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = Act6FSpecs.boss(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
        List<Long> first = new ArrayList<>();
        int length = SimStep.ticks(50);
        while (boss.bossTicks() < length) {
            int t = boss.bossTicks();
            sortie.step(command(t));
            poke(sortie, boss.bossTicks());
            first.add(pieceHash(boss));
        }
        assertTrue(Act6FSpecs.squadron(sortie).deaths() >= 1, "the run saw a death");
        sortie.retryFromBoss();
        sortie.step(0);
        for (int a = 0; a < 3; a++) {
            assertEquals(Squadron.State.WAITING, Act6FSpecs.squadron(sortie).state(a));
            assertEquals(Act6FSpecs.HP, boss.partHp(a), EPS);
        }
        while (boss.bossTicks() < 0) {
            sortie.step(0);
        }
        List<Long> second = new ArrayList<>();
        while (boss.bossTicks() < length) {
            int t = boss.bossTicks();
            sortie.step(command(t));
            poke(sortie, boss.bossTicks());
            second.add(pieceHash(boss));
        }
        assertEquals(first, second, "the retry flies the duel exactly again");
    }

    /** A weave and fire by the boss's clock. */
    private static int command(int t) {
        return Command.FIRE.bit() | ((t / 50) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
    }

    /** Flat hits on the boss's clock (as the Lance's): Harker at 8 s, Vesper's share at 30 s. */
    private static void poke(Sortie sortie, int t) {
        SetPiece boss = Act6FSpecs.boss(sortie);
        if (t == SimStep.ticks(8)) {
            boss.damagePartFlat(H, 700);
        }
        if (t == SimStep.ticks(12)) {
            Act6FSpecs.kill(sortie, H);
        }
        if (t == SimStep.ticks(30)) {
            boss.damagePartFlat(V, 900);
        }
    }

    static long pieceHash(SetPiece piece) {
        StateHash hash = new StateHash();
        piece.addTo(hash);
        return hash.value();
    }

    @Test
    void anAceTouchingTheShipStrikesOnceAnIntervalPushesItAndFliesOn() {
        Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.GUARD, TestSpecs.RULES);
        SetPiece boss = Act6FSpecs.boss(sortie);
        Squadron squadron = Act6FSpecs.squadron(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
        while (!squadron.touchable(H)) {
            sortie.step(0);
        }
        int contacts = 0;
        double pushed = 0;
        for (int i = 0; i < SimStep.ticks(2.5); i++) {
            Ship ship = sortie.ship();
            // Onto Harker each step.
            ship.pushOut(1, 0, squadron.x(H) - ship.x());
            ship.pushOut(0, 1, squadron.y(H) - ship.y());
            sortie.step(0);
            if (sortie.events().count(SimEvents.Type.ACE_CONTACT) > 0) {
                contacts++;
                pushed = Math.hypot(ship.x() - squadron.x(H), ship.y() - squadron.y(H));
            }
        }
        assertEquals(3, contacts, "once a second");
        assertTrue(pushed > 15, "the ship pushed away: " + pushed);
        PartShields shields = boss.shields().orElseThrow();
        assertEquals(0, shields.shield(H), EPS, "his shield spent first");
        assertEquals(Act6FSpecs.HP - (3 * 40 - 60), boss.partHp(H), EPS, "he took 40 a touch");
        assertFalse(boss.destroyed());
        assertTrue(squadron.touchable(H), "and flies on");
    }

    @Test
    void eachAceCarriesItsShieldTheEmpStripsThemAllNeverABreakerTwice() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        rig.untilAllFly(50);
        SetPiece piece = rig.piece;
        PartShields shields = piece.shields().orElseThrow();
        for (int a = 0; a < 3; a++) {
            assertTrue(shields.fitted(a));
            assertEquals(60, shields.shield(a), EPS);
        }
        piece.damagePart(V, 30);
        assertEquals(30, shields.shield(V), EPS);
        assertEquals(Act6FSpecs.HP, piece.partHp(V), EPS);
        rig.steps(1.9);
        assertEquals(30, shields.shield(V), EPS, "no refill before 2 s");
        rig.steps(1.5);
        assertEquals(60, shields.shield(V), EPS, "full again");
        piece.stripShields(SimStep.ticks(3));
        for (int a = 0; a < 3; a++) {
            assertEquals(0, shields.shield(a), EPS, "the EMP empties all three");
            assertTrue(shields.held(a));
        }
        assertTrue(piece.engaged(), "never a stun");
        rig.steps(5.5);
        piece.damagePartBreaking(K, 20, piece.partX(K), piece.partY(K));
        assertEquals(0, shields.shield(K), EPS, "the breaker empties it");
        assertEquals(Act6FSpecs.HP - 20, piece.partHp(K), EPS, "the whole hit to the ace");
    }

    @Test
    void aDeadAcesHullTumblesAwayDownAndOutTouchingNothing() {
        Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.GUARD, TestSpecs.RULES);
        SetPiece boss = Act6FSpecs.boss(sortie);
        Squadron squadron = Act6FSpecs.squadron(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
        while (!squadron.touchable(H)) {
            sortie.step(0);
        }
        Act6FSpecs.kill(sortie, H);
        sortie.step(0);
        sortie.step(0);
        assertEquals(Squadron.State.DOWN, squadron.state(H));
        assertTrue(squadron.tumbling(H));
        assertFalse(boss.partArmed(H));
        double lastY = squadron.y(H);
        double startX = squadron.x(H);
        int steps = 0;
        while (squadron.tumbling(H)) {
            Ship ship = sortie.ship();
            ship.pushOut(1, 0, squadron.x(H) - ship.x());
            ship.pushOut(0, 1, squadron.y(H) - ship.y());
            sortie.step(0);
            assertEquals(0, sortie.events().count(SimEvents.Type.ACE_CONTACT), "a wreck touches nothing");
            assertTrue(squadron.y(H) < lastY, "it falls");
            lastY = squadron.y(H);
            assertTrue(++steps < SimStep.ticks(6), "out of the field within 6 s");
        }
        assertTrue(squadron.x(H) < startX, "out to the nearer (left) side");
        assertTrue(squadron.spin(H) != 0);
    }

    @Test
    void aSquadronHasNoArenaAndEveryPartIsAnAceNoneVital() {
        BossSpec boss = Act6FSpecs.GUARD.boss().orElseThrow();
        assertTrue(boss.squadron().isPresent());
        assertTrue(boss.withSquadron(Optional.empty()).squadron().isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpecBuilder(boss)
                        .arena(Optional.of(new BossSpec.Arena(BossSpec.Lanes.NONE, null, List.of(), List.of())))
                        .build(),
                "no arena");
        List<PartSpec> parts = new ArrayList<>(Act6FSpecs.GUARD.parts());
        PartSpec first = parts.getFirst();
        parts.set(
                0,
                new PartSpec(
                        first.name(), 0, 0, first.box(), first.hp(), true, first.bounty(), Optional.empty(), 0, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SetPieceSpec(
                        "guard",
                        new Hitbox(60, 54),
                        new Hitbox(1, 1),
                        15,
                        parts,
                        List.of(),
                        Optional.empty(),
                        Optional.of(boss)),
                "no vital ace");
        assertThrows(
                IllegalArgumentException.class,
                () -> new SetPieceSpec(
                        "guard",
                        new Hitbox(60, 54),
                        new Hitbox(1, 1),
                        15,
                        Act6FSpecs.GUARD.parts().subList(0, 2),
                        List.of(),
                        Optional.empty(),
                        Optional.of(boss)),
                "every part an ace");
        assertThrows(
                IllegalArgumentException.class,
                () -> new SquadronSpec.Ace(0, "TOOLONGNAME", SquadronSpec.Role.SNIPER, null, null),
                "a tag of at most 7");
    }

    @Test
    void aDuelStepsWithoutAllocating() {
        Act6FSpecs.Playbook[] playbook = new Act6FSpecs.Playbook[1];
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.GUARD, TestSpecs.RULES.withInvulnerableShip());
                    MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
                    playbook[0] = new Act6FSpecs.Playbook(3);
                    return sortie;
                },
                sortie -> {
                    SetPiece boss = Act6FSpecs.boss(sortie);
                    Squadron squadron = Act6FSpecs.squadron(sortie);
                    for (int i = 0; i < SimStep.ticks(45); i++) {
                        sortie.step(command(i));
                        playbook[0].step(
                                squadron, sortie.ship().x(), sortie.ship().y());
                        if (i == SimStep.ticks(8)) {
                            boss.damagePartFlat(H, 1000);
                        }
                        if (i == SimStep.ticks(20)) {
                            Act6FSpecs.kill(sortie, H);
                        }
                        if (i == SimStep.ticks(32)) {
                            Act6FSpecs.kill(sortie, V);
                        }
                        if (i == SimStep.ticks(38)) {
                            Act6FSpecs.kill(sortie, K);
                        }
                    }
                });
        assertEquals(0, allocated, "joins, flight, contacts, deaths and wrecks allocate nothing");
    }
}
