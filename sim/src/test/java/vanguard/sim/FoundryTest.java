package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part F, step F3: Level 19's systems (design/campaign Level 19): the furnace vents ({@link FurnaceVents}: rows
 * scrolling with the ground, each firing a ripple of tells and flame columns on the level clock, hitting the ship and
 * Rook whatever their layer once per burst, cold from the arena on), the "12 of 14" escapes objective ({@link
 * Secondary#escapesAllowed()}), the clock's hold after the arena boss's death chain and the crawl's ease up
 * from the halt ({@link Section#holdAfterBossSeconds()}), and Rook in the foundry: out of the walker's cone
 * while it tells or burns, its pods before its hull, off the glowing vents.
 */
class FoundryTest {
    private static final int FIRE = Command.FIRE.bit();

    /** A row of three grates at t=1: 1.2 s tell, 1.0 s burn, 3.5 s period, 0.4 s ripple from the left, `heavy` 10. */
    static final HazardSpec.VentRow ROW =
            new HazardSpec.VentRow(1, new double[] {100, 240, 380}, 44, 1.2, 1.0, 3.5, 0.4, 10);

    /** A level of {@code sections} with these vent rows and waves. */
    private static LevelScript level(
            List<Section> sections,
            List<HazardSpec.VentRow> rows,
            List<WaveSpec> waves,
            Secondary secondary,
            List<SetPieceSpec> pieces) {
        return LevelScript.builder(19, 3)
                .sections(sections)
                .waves(waves)
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .setPieces(pieces)
                .hazards(HazardSpec.NONE.withVents(rows))
                .build();
    }

    private static LevelScript ventLevel(HazardSpec.VentRow... rows) {
        return level(List.of(new Section(30, 130)), List.of(rows), List.of(), Secondary.NONE, List.of());
    }

    private static Sortie sortie(LevelScript level, Loadout loadout, Rules rules) {
        return new Sortie(1, loadout, level, rules, TestSpecs.FULL_ARMOUR);
    }

    private static List<Integer> values(Sortie sortie, SimEvents.Type type) {
        List<Integer> values = new ArrayList<>();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                values.add(events.value(i));
            }
        }
        return values;
    }

    private static double health(Sortie sortie) {
        return sortie.ship().defences().shield() + sortie.ship().defences().armour();
    }

    // ---- the furnace vents ----

    @Test
    void aVentRowIsCheckedAndItsGratesAreNumberedThroughTheLevel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HazardSpec.VentRow(1, new double[] {100, 200}, 44, 1.2, 1.0, 2.5, 0.4, 10),
                "a period that does not hold its ripple");
        assertThrows(
                IllegalArgumentException.class,
                () -> new HazardSpec.VentRow(1, new double[0], 44, 1.2, 1.0, 3.5, 0.4, 10));
        FurnaceVents vents =
                sortie(ventLevel(ROW, ROW), TestSpecs.LOADOUT, TestSpecs.RULES).vents();
        assertEquals(6, vents.grates());
        assertEquals(4, vents.grate(1, 1));
        assertEquals(1, vents.row(4));
        assertEquals(240, vents.x(4), 0);
        assertEquals(
                0,
                sortie(TestSpecs.level(10, List.of()), TestSpecs.LOADOUT, TestSpecs.RULES)
                        .vents()
                        .grates(),
                "no grates in a level without vents");
    }

    @Test
    void aRowRipplesItsTellsAndBurstsOnTheLevelClockWhileItScrollsWithTheGround() {
        Sortie sortie = sortie(ventLevel(ROW), TestSpecs.LOADOUT, TestSpecs.RULES.withInvulnerableShip());
        FurnaceVents vents = sortie.vents();
        List<int[]> tells = new ArrayList<>();
        List<int[]> bursts = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            for (int g : values(sortie, SimEvents.Type.VENT_TELL)) {
                tells.add(new int[] {g, sortie.levelTick});
                assertEquals(FurnaceVents.Phase.TELL, vents.phase(g));
                assertEquals(0, vents.progress(g), 0);
            }
            for (int g : values(sortie, SimEvents.Type.VENT_BURST)) {
                bursts.add(new int[] {g, sortie.levelTick});
                assertEquals(FurnaceVents.Phase.BURN, vents.phase(g));
            }
            if (sortie.levelTick == SimStep.ticks(2)) {
                assertEquals(PlayField.HEIGHT + 22 - 130, vents.y(0), 1e-6, "entered at the top edge at t=1");
                assertTrue(vents.present(0));
            }
        }
        int t = SimStep.ticks(1);
        assertEquals(List.of(0, 1, 2, 0, 1, 2), tells.stream().map(e -> e[0]).toList(), "two cycles on the screen");
        assertEquals(t, tells.get(0)[1]);
        assertEquals(t + SimStep.ticks(0.4), tells.get(1)[1], "0.4 s ripple from the first grate");
        assertEquals(t + SimStep.ticks(0.8), tells.get(2)[1]);
        assertEquals(t + SimStep.ticks(3.5), tells.get(3)[1], "every 3.5 s");
        assertEquals(t + SimStep.ticks(1.2), bursts.get(0)[1], "the flame after its 1.2 s tell");
        assertEquals(List.of(0, 1, 2), bursts.stream().map(e -> e[0]).toList(), "the row left before its next flames");
        assertFalse(vents.present(0));
        assertEquals(FurnaceVents.Phase.IDLE, vents.phase(0));
    }

    /** One grate at x 240 at t=0.5 that tells 0.2 s and burns 1.0 s of every 1.2 s (so it burns as it passes). */
    private static final HazardSpec.VentRow HOT =
            new HazardSpec.VentRow(0.5, new double[] {240}, 44, 0.2, 1.0, 1.2, 0, 10);

    @Test
    void aBurningGrateHitsTheShipOncePerBurst() {
        Sortie sortie = sortie(ventLevel(HOT), TestSpecs.LOADOUT, TestSpecs.RULES);
        double before = health(sortie);
        int hits = 0;
        int lastBurst = -1;
        int burst = -1;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            if (!values(sortie, SimEvents.Type.VENT_BURST).isEmpty()) {
                burst++;
            }
            List<Integer> hit = values(sortie, SimEvents.Type.VENT_HIT);
            if (!hit.isEmpty()) {
                assertEquals(List.of(0), hit, "the ship");
                assertTrue(burst != lastBurst, "once per burst");
                assertEquals(FurnaceVents.Phase.BURN, sortie.vents().phase(0));
                lastBurst = burst;
                hits++;
            }
        }
        assertTrue(hits >= 1, "it passed over the ship burning");
        assertEquals(before - 10 * hits, health(sortie), 1e-6, "`heavy` 10 a hit");
    }

    @Test
    void aBurstHitsTheShipAndRookOnceEachWhateverTheirLayer() {
        FurnaceVents vents = new FurnaceVents(List.of(HOT), Integer.MAX_VALUE);
        SimEvents events = new SimEvents(16);
        double scroll = 0;
        for (int tick = 0; tick <= SimStep.ticks(4.4); tick++) {
            scroll += 130 * SimStep.SECONDS;
            vents.update(tick, scroll, true, events);
        }
        assertEquals(FurnaceVents.Phase.BURN, vents.phase(0));
        Hull hull = TestSpecs.HULL;
        double y = vents.y(0);
        assertEquals(0, vents.strike(hull, 240, y, FurnaceVents.SHIP, 0));
        assertEquals(-1, vents.strike(hull, 240, y, FurnaceVents.SHIP, 0), "once per burst");
        assertEquals(0, vents.strike(hull, 240, y, FurnaceVents.ROOK, 0), "Rook has his own");
        assertEquals(-1, vents.strike(hull, 330, y, FurnaceVents.SHIP, 0), "beside the column");
        assertEquals(10, vents.damage(0), 0);
    }

    @Test
    void theVentsAreColdFromTheArenaOn() {
        HazardSpec.VentRow slow = new HazardSpec.VentRow(1, new double[] {240}, 44, 0.2, 1.0, 1.2, 0, 10);
        Sortie sortie = sortie(
                level(
                        List.of(new Section(3, 40), new Section(4, 0, true), new Section(20, 40)),
                        List.of(slow),
                        List.of(),
                        Secondary.NONE,
                        List.of()),
                TestSpecs.LOADOUT,
                TestSpecs.RULES.withInvulnerableShip());
        int before = 0;
        while (sortie.levelSeconds() < 8) {
            sortie.step(Command.NONE);
            int events = values(sortie, SimEvents.Type.VENT_TELL).size()
                    + values(sortie, SimEvents.Type.VENT_BURST).size();
            if (sortie.levelTick < SimStep.ticks(3)) {
                before += events;
            } else {
                assertEquals(0, events, "cold at " + sortie.levelSeconds());
                assertEquals(FurnaceVents.Phase.IDLE, sortie.vents().phase(0));
            }
        }
        assertTrue(before > 0);
        assertTrue(sortie.vents().present(0), "the row still on the screen, cold");
    }

    @Test
    void rookKeepsOffAGlowingOrBurningGrate() {
        FurnaceVents vents = new FurnaceVents(List.of(HOT), Integer.MAX_VALUE);
        RookHazards hazards = new RookHazards(WingmanTest.CRAFT.hitbox(), WingmanTest.CRAFT.edgeLimit());
        SimEvents events = new SimEvents(16);
        vents.update(SimStep.ticks(0.5), 0, true, events);
        assertEquals(FurnaceVents.Phase.TELL, vents.phase(0));
        // Move the row down to y 300 (ground scrolled 262 px) while it tells.
        vents.update(SimStep.ticks(0.5), vents.y(0) - 300, false, events);
        assertEquals(300, vents.y(0), 1e-9);
        assertTrue(hazards.clear(240, 300, new SetPiece[0], vents), "on the grate");
        double reach = 22 + WingmanTest.CRAFT.hitbox().height() / 2 + RookHazards.GAP;
        assertEquals(240, hazards.x(), 0);
        assertTrue(Math.abs(hazards.y() - 300) >= reach - 1e-9, "above or below its row: " + hazards.y());
        assertFalse(hazards.clear(240, 300 + reach + 1, new SetPiece[0], vents), "clear of it already");
        assertFalse(hazards.clear(330, 300, new SetPiece[0], vents), "beside it");
    }

    @Test
    void ventsAreDeterministicAndAllocateNothing() {
        LevelScript level = ventLevel(ROW, HOT);
        Loadout rook = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        Sortie a = sortie(level, rook, TestSpecs.RULES);
        Sortie b = sortie(level, rook, TestSpecs.RULES);
        for (int i = 0; i < SimStep.ticks(8); i++) {
            int commands = (i / 40) % 2 == 0 ? Command.LEFT.bit() | FIRE : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(level, rook, TestSpecs.RULES.withInvulnerableShip());
                    sortie.step(Command.NONE);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(6); i++) {
                        sortie.step((i / 30) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
                    }
                });
        assertEquals(0, allocated, "the vents and Rook's clearance allocate nothing per step");
    }

    // ---- "12 of 14" ----

    private static Sortie skitters(int allowed) {
        WaveSpec column = TestSpecs.wave(
                1, WaveSpec.Formation.COLUMN, TestSpecs.SKITTER, 4, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        LevelScript level = level(
                List.of(new Section(20, 150)),
                List.of(),
                List.of(column),
                new Secondary(0, 50, List.of(), "skitter").withEscapesAllowed(allowed),
                List.of());
        return sortie(level, TestSpecs.LOADOUT, TestSpecs.RULES.withInvulnerableShip());
    }

    /** Destroys the first {@code destroy} skitters on the field; returns the met and failed events to the end. */
    private static int[] fly(Sortie sortie, int destroy) {
        int destroyed = 0;
        int met = 0;
        int failed = 0;
        while (!sortie.complete()) {
            sortie.step(Command.NONE);
            for (int i = sortie.enemyCount() - 1; i >= 0 && destroyed < destroy; i--) {
                Enemy enemy = sortie.enemy(i);
                if (PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
                    sortie.destroyEnemy(i);
                    destroyed++;
                }
            }
            met += values(sortie, SimEvents.Type.OBJECTIVE_MET).size();
            failed += values(sortie, SimEvents.Type.OBJECTIVE_FAILED).size();
        }
        return new int[] {met, failed};
    }

    @Test
    void anEscapesObjectiveWithEscapesAllowedIsMetWithNoMoreThanThoseAway() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Secondary.links(10, "X").withEscapesAllowed(1),
                "only an escapes objective");
        Sortie sortie = skitters(2);
        assertEquals(4, sortie.escapesTotal());
        int[] outcome = fly(sortie, 2);
        assertEquals(2, sortie.escapesDestroyed(), "the HUD's 2 of 2 needed");
        assertEquals(0, sortie.escapesLost(), "met before the other two got away: they no longer count");
        assertTrue(sortie.secondaryMet());
        assertFalse(sortie.secondaryFailed());
        assertEquals(1, outcome[0], "met once");
        assertEquals(0, outcome[1]);
    }

    @Test
    void itIsMetAtOnceWhenItCanNoLongerFail() {
        // Round 42: the GREY TANKS counter sat at 12 / 12 until the last two tanks were decided; it turns DONE at the
        // destruction that leaves no more than the allowed ones, with the others still on the field.
        Sortie sortie = skitters(2);
        int destroyed = 0;
        long metAt = -1;
        int decidedAtMet = -1;
        while (!sortie.complete() && metAt < 0) {
            sortie.step(Command.NONE);
            for (int i = sortie.enemyCount() - 1; i >= 0 && destroyed < 2; i--) {
                Enemy enemy = sortie.enemy(i);
                if (PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
                    sortie.destroyEnemy(i);
                    destroyed++;
                }
            }
            if (!values(sortie, SimEvents.Type.OBJECTIVE_MET).isEmpty()) {
                metAt = sortie.tick();
                decidedAtMet = sortie.escapesDestroyed() + sortie.escapesLost();
            }
        }
        assertTrue(metAt >= 0, "met");
        assertEquals(2, sortie.escapesDestroyed());
        assertTrue(sortie.secondaryMet());
        assertEquals(0, sortie.escapesLost());
        assertEquals(2, decidedAtMet, "met with two of the four still undecided");
    }

    @Test
    void theOnePastTheAllowanceFailsIt() {
        Sortie sortie = skitters(2);
        int[] outcome = fly(sortie, 1);
        assertTrue(sortie.secondaryFailed());
        assertFalse(sortie.secondaryMet());
        assertEquals(0, outcome[0]);
        assertEquals(1, outcome[1], "failed at the third away");
        assertEquals(3, sortie.escapesLost());
    }

    @Test
    void withoutAnAllowanceOneAwayFailsItAsBefore() {
        Sortie sortie = skitters(0);
        int[] outcome = fly(sortie, 3);
        assertTrue(sortie.secondaryFailed());
        assertEquals(1, outcome[1]);
        assertEquals(0, sortie.escapesLost(), "not counted without an allowance");
    }

    // ---- the hold after the walker and the crawl's ease ----

    private static final int HULL = RevenantWalkerSimTest.HULL;
    private static final double ARRIVE = RevenantWalkerSimTest.ARRIVE;

    /** The walker's level: its arena 3–4 s holding {@code hold} s after the death chain, then the crawl and the run. */
    private static LevelScript walkerLevel(double hold, double ease) {
        return level(
                List.of(
                        new Section(ARRIVE, 140),
                        new Section(ARRIVE + 1, 0, true, 2, 0, hold),
                        new Section(ARRIVE + 11, 20, false, 0, ease),
                        new Section(ARRIVE + 21, 150, false, 0, ease)),
                List.of(),
                List.of(),
                Secondary.NONE,
                List.of(RevenantWalkerSimTest.walker(false, 6)));
    }

    private static Sortie settled(LevelScript level, Loadout loadout) {
        return settled(level, loadout, TestSpecs.RULES.withInvulnerableShip());
    }

    private static Sortie settled(LevelScript level, Loadout loadout, Rules rules) {
        Sortie sortie = sortie(level, loadout, rules);
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.BOSS_SETTLED) > 0) {
                return sortie;
            }
        }
        throw new AssertionError("the walker settles");
    }

    private static void kill(Sortie sortie) {
        sortie.setPiece(0).damagePart(HULL, 2 * RevenantWalkerSimTest.HULL_HP);
        sortie.objectivePasses.wreck(0, HULL);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_DESTROYED));
    }

    @Test
    void onlyAnArenaHoldsAfterItsBoss() {
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 100, false, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 0, true, 0, 0, -1));
        assertEquals(0, new Section(10, 100).holdAfterBossSeconds(), 0);
    }

    @Test
    void theClockHoldsOverTheDeathChainAndASecondThenTheCrawlEasesUpFromTheHalt() {
        Sortie sortie = settled(walkerLevel(1, 2), TestSpecs.LOADOUT);
        while (!sortie.arenaHalted()) {
            sortie.step(Command.NONE);
        }
        kill(sortie);
        assertTrue(sortie.holdingAfterBoss(), "the hold starts at the death");
        int tick = sortie.levelTick;
        int held = 0;
        while (sortie.levelTick == tick) {
            double scroll = sortie.groundScroll();
            sortie.step(Command.NONE);
            if (sortie.levelTick == tick) {
                assertEquals(scroll, sortie.groundScroll(), 0, "the scroll stands");
                held++;
            }
        }
        assertEquals(SimStep.ticks(2 + 1), held, "the 2 s topple and 1 s");
        assertFalse(sortie.holdingAfterBoss());
        // The crawl eases up from the halt to 20 px/s over 2 s (smoothstep), then crawls.
        List<Double> speeds = new ArrayList<>();
        double scroll = sortie.groundScroll();
        speeds.add(scroll);
        for (int i = 0; i < SimStep.ticks(3); i++) {
            double before = sortie.groundScroll();
            sortie.step(Command.NONE);
            speeds.add((sortie.groundScroll() - before) / SimStep.SECONDS);
        }
        int n = SimStep.ticks(2);
        assertTrue(speeds.get(1) > 0 && speeds.get(1) < 0.5, "it starts from the halt: " + speeds.get(1));
        for (int k = 2; k <= n; k++) {
            assertTrue(speeds.get(k) >= speeds.get(k - 1) - 1e-9, "never slower");
        }
        assertEquals(10, speeds.get(n / 2), 1, "half way at half the time (smoothstep)");
        assertEquals(20, speeds.get(n + 1), 1e-9, "the crawl at 20 px/s");
        while (sortie.levelSeconds() < ARRIVE + 1 + 10 + 2.5) {
            sortie.step(Command.NONE);
        }
        assertEquals(150, sortie.groundSpeed(), 1e-9, "eased up to 150 px/s after the crawl");
    }

    @Test
    void withoutAHoldTheClockGoesOnAtOnceAsBefore() {
        Sortie sortie = settled(walkerLevel(0, 0), TestSpecs.LOADOUT);
        while (!sortie.arenaHalted()) {
            sortie.step(Command.NONE);
        }
        kill(sortie);
        int tick = sortie.levelTick;
        sortie.step(Command.NONE);
        assertEquals(tick + 1, sortie.levelTick, "the clock resumes in the next step");
        assertFalse(sortie.holdingAfterBoss());
    }

    // ---- Rook in the walker fight ----

    private static Loadout rook() {
        return TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
    }

    @Test
    void rookLeavesTheConeWhileItTellsAndIsNeverBurnt() {
        // Not the invulnerable ship's rules: they spare Rook too.
        Sortie sortie = settled(walkerLevel(1, 2), rook(), TestSpecs.RULES);
        Wingman rook = sortie.wingman().orElseThrow();
        SetPiece boss = sortie.setPiece(0);
        StrideArena stride = boss.stride().orElseThrow();
        double armour = rook.armour();
        int tells = 0;
        int inside = 0;
        int burnsInside = 0;
        for (int i = 0; i < SimStep.ticks(40) && tells < 4; i++) {
            // The ship keeps behind the walker's back, beside it, so the cone tells at it (and Rook in his slot).
            double target = boss.x();
            int steer = Math.abs(sortie.ship().x() - target) < 4
                    ? 0
                    : (sortie.ship().x() < target ? Command.RIGHT : Command.LEFT).bit();
            sortie.step(steer);
            if (sortie.events().count(SimEvents.Type.CONE_TELL) > 0) {
                tells++;
                if (stride.inCone(rook.x(), rook.y())) {
                    inside++;
                }
            }
            if (sortie.events().count(SimEvents.Type.CONE_BURN) > 0 && stride.inCone(rook.x(), rook.y())) {
                burnsInside++;
            }
        }
        assertTrue(tells > 0, "the cone told");
        assertTrue(inside > 0, "Rook was in a cone as it told: " + inside + " of " + tells);
        assertEquals(0, burnsInside, "out of it by the burn");
        assertEquals(armour, rook.armour(), 1e-9, "he left it before it burnt");
    }

    @Test
    void rookShootsThePodsBeforeTheHull() {
        Sortie sortie = settled(walkerLevel(1, 2), rook());
        Wingman rook = sortie.wingman().orElseThrow();
        SetPiece boss = sortie.setPiece(0);
        int hull = PlayerFire.PART_SERIAL + HULL;
        int hullWhileBoth = 0;
        int podWhileBoth = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            // The ship keeps Rook (in his left slot) a little right of the walker's centre: hull and a pod in his cone.
            double target = boss.x() + 100;
            int steer = Math.abs(sortie.ship().x() - target) < 4
                    ? 0
                    : (sortie.ship().x() < target ? Command.RIGHT : Command.LEFT).bit();
            sortie.step(FIRE | steer);
            boolean pod = false;
            for (int p = 1; p <= 2; p++) {
                pod |= !boss.partWrecked(p) && inRooksCone(rook, boss.partX(p), boss.partY(p));
            }
            if (!pod || !inRooksCone(rook, boss.partX(HULL), boss.partY(HULL))) {
                continue;
            }
            if (rook.target() == hull) {
                hullWhileBoth++;
            } else if (rook.target() == PlayerFire.PART_SERIAL + 1 || rook.target() == PlayerFire.PART_SERIAL + 2) {
                podWhileBoth++;
            }
        }
        assertTrue(podWhileBoth > 0, "a pod and the hull in his cone, he on a pod: " + podWhileBoth);
        assertTrue(hullWhileBoth * 4 <= podWhileBoth, "the hull only in his reaction time: " + hullWhileBoth);
    }

    /** Rook's firing cone (design/player/wingmen, WingmanTest.AI: 15° each way, 360 px). */
    private static boolean inRooksCone(Wingman rook, double px, double py) {
        double dx = px - rook.x();
        double dy = py - rook.y();
        return dy > 0 && Math.abs(dx) <= dy * Math.tan(Math.toRadians(15)) && dx * dx + dy * dy <= 360 * 360;
    }

    @Test
    void theFoundrysRookIsDeterministicAndAllocatesNothing() {
        LevelScript level = walkerLevel(1, 2);
        Sortie a = sortie(level, rook(), TestSpecs.RULES);
        Sortie b = sortie(level, rook(), TestSpecs.RULES);
        for (int i = 0; i < SimStep.ticks(30); i++) {
            int commands = (i / 50) % 2 == 0 ? Command.LEFT.bit() | FIRE : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(() -> settled(level, rook()), sortie -> {
            for (int i = 0; i < SimStep.ticks(15); i++) {
                sortie.step(FIRE | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
            }
        });
        assertEquals(0, allocated, "Rook's cone clearance and the hold allocate nothing per step");
    }
}
