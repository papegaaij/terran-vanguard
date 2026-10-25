package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part G, step G1 (design/campaign Level 27, Hazards: the heat plumes; m8g-gaps.md §1.2, §5 G5, G6): a plume row is
 * a vent row of discs: each tells, then burns a disc its width across, hitting the ship and the Bulwark whose hull it
 * touches once per burst; it burns every swimmer over it once per burst (the kill is the player's: bounty and kill
 * count), not a ground unit nor one under the surface; it bends a shot, an enemy bullet or a homing torpedo entering it
 * 25° away from its centre once; not a bomb, a mine or a depth charge. The square grates of Level 19 and the geysers
 * keep their box and pass the Bulwark. Deterministic and allocation-free.
 */
class HeatPlumesTest {
    private static final double BEND = Math.toRadians(25);

    /** A plume row at {@code t}: 64 px discs at {@code xs}, tell 1.4 s, burn 1.6 s, period 4.5 s, ripple 0.5 s. */
    static HazardSpec.VentRow plumes(double t, double... xs) {
        return new HazardSpec.VentRow(t, xs, 64, 1.4, 1.6, 4.5, 0.5, 10, true, 40, BEND);
    }

    private static LevelScript level(List<HazardSpec.VentRow> rows, List<WaveSpec> waves) {
        return LevelScript.builder(27, 3)
                .sections(List.of(new Section(40, 110)))
                .waves(waves)
                .hazards(HazardSpec.NONE.withVents(rows))
                .build();
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

    /** Vents stepped by hand to {@code seconds} at 110 px/s. */
    private static FurnaceVents stepped(HazardSpec.VentRow row, double seconds) {
        FurnaceVents vents = new FurnaceVents(List.of(row), Integer.MAX_VALUE);
        SimEvents events = new SimEvents(16);
        double scroll = 0;
        for (int tick = 0; tick <= SimStep.ticks(seconds); tick++) {
            scroll += 110 * SimStep.SECONDS;
            vents.update(tick, scroll, true, events);
        }
        return vents;
    }

    @Test
    void aPlumeRowIsCheckedAndOnlyADiscBurnsOrBends() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HazardSpec.VentRow(1, new double[] {240}, 64, 1.4, 1.6, 4.5, 0, 10, false, 40, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new HazardSpec.VentRow(1, new double[] {240}, 64, 1.4, 1.6, 4.5, 0, 10, true, 0, Math.PI / 2));
        HazardSpec.VentRow grate = new HazardSpec.VentRow(1, new double[] {240}, 44, 1.2, 1.0, 3.5, 0, 10);
        assertFalse(grate.disc());
        assertEquals(0, grate.enemyDamage());
        assertEquals(0, grate.bendRadians());
        assertTrue(plumes(1, 240).disc());
    }

    @Test
    void itsTimingsAreTheRowsTellBurnAndRipple() {
        HazardSpec.VentRow row = plumes(0, 120, 360);
        // Medium: tell 1.4, burn 1.6; the second disc 0.5 s later.
        FurnaceVents vents = stepped(row, 1.0);
        assertEquals(FurnaceVents.Phase.TELL, vents.phase(0));
        assertEquals(FurnaceVents.Phase.TELL, vents.phase(1));
        vents = stepped(row, 1.6);
        assertEquals(FurnaceVents.Phase.BURN, vents.phase(0));
        assertEquals(FurnaceVents.Phase.TELL, vents.phase(1), "its ripple");
        vents = stepped(row, 3.2);
        assertEquals(FurnaceVents.Phase.IDLE, vents.phase(0), "burnt out at 3.0 s");
        assertEquals(FurnaceVents.Phase.BURN, vents.phase(1));
        // Easy's and hard's own rows (content maps them): tell 1.8 / period 6 and tell 1.0 / period 4.
        HazardSpec.VentRow easy =
                new HazardSpec.VentRow(0, new double[] {240}, 64, 1.8, 1.6, 6, 0.5, 10, true, 40, BEND);
        assertEquals(FurnaceVents.Phase.TELL, stepped(easy, 1.6).phase(0));
        assertEquals(FurnaceVents.Phase.IDLE, stepped(easy, 4.5).phase(0), "cold until 6 s");
        HazardSpec.VentRow hard =
                new HazardSpec.VentRow(0, new double[] {240}, 64, 1.0, 1.6, 4, 0.5, 10, true, 40, BEND);
        assertEquals(FurnaceVents.Phase.BURN, stepped(hard, 1.2).phase(0));
        assertEquals(FurnaceVents.Phase.TELL, stepped(hard, 4.2).phase(0), "again at 4 s");
    }

    @Test
    void theDiscHitsAHullItTouchesNotTheBoxCorners() {
        FurnaceVents vents = stepped(plumes(0, 240), 1.6);
        assertTrue(vents.disc(0));
        assertEquals(32, vents.radius(0));
        double y = vents.y(0);
        Hull small = new Hull(List.of(new Hull.Part(0, 0, new Hitbox(4, 4))));
        // In the box's corner but outside the disc.
        assertEquals(-1, vents.strike(small, 240 + 28, y + 28, FurnaceVents.SHIP, 0), "the corner is clear");
        assertEquals(0, vents.strike(small, 240 + 20, y + 20, FurnaceVents.SHIP, 0), "inside the disc");
        assertEquals(-1, vents.strike(small, 240, y, FurnaceVents.SHIP, 0), "once per burst");
        assertEquals(0, vents.strike(small, 240, y, FurnaceVents.BULWARK, 0), "the Bulwark has its own");
        assertEquals(0, vents.strike(small, 240, y, FurnaceVents.ROOK, 0), "Rook has his");
        // A square grate passes the Bulwark, as before.
        HazardSpec.VentRow grate = new HazardSpec.VentRow(0, new double[] {240}, 44, 1.4, 1.6, 4.5, 0, 10);
        FurnaceVents square = stepped(grate, 1.6);
        assertEquals(-1, square.strike(small, 240, square.y(0), FurnaceVents.BULWARK, 0));
        assertEquals(0, square.strike(small, 240 + 20, square.y(0) + 20, FurnaceVents.SHIP, 0), "its box");
    }

    @Test
    void aBendTurnsAwayFromTheCentre() {
        // Flying up (0, 1), right of the centre: it turns clockwise (to the right).
        assertEquals(BEND, HeatPlumes.away(0, 1, 5, 0, BEND));
        assertEquals(-BEND, HeatPlumes.away(0, 1, -5, 0, BEND));
        // Flying down, right of the centre: counter-clockwise on the screen (still away, to the right).
        assertEquals(-BEND, HeatPlumes.away(0, -1, 5, 0, BEND));
        Shot shot = new Shot();
        shot.fire(TestSpecs.PULSE, 0, 100, 100, 0);
        assertTrue(shot.bendable());
        shot.bend(BEND);
        assertFalse(shot.bendable(), "once");
        assertEquals(Math.sin(BEND), shot.vx() / TestSpecs.PULSE.speed(), 1e-6);
        assertEquals(Math.cos(BEND), shot.vy() / TestSpecs.PULSE.speed(), 1e-6);
        assertEquals(BEND, shot.heading(), 1e-12);
        EnemyBullet bullet = new EnemyBullet();
        bullet.fire(100, 100, 0, -100, 4);
        bullet.bend(-BEND);
        assertTrue(bullet.bent());
        assertEquals(100 * Math.sin(BEND), bullet.vx(), 1e-5, "down, turned counter-clockwise: to the right");
    }

    /** A swimmer that holds still over x 240 (a plain hover), 100 HP, bounty 7. */
    private static EnemySpec swimmer(Layer layer, double hp) {
        return new EnemySpec(
                "swimmer",
                hp,
                new Hitbox(20, 20),
                layer,
                0,
                false,
                7,
                400,
                Optional.empty(),
                Optional.empty(),
                Optional.of(new EnemySpec.Hover(new Range(30, 30), new Range(300, 300))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false);
    }

    private static WaveSpec single(EnemySpec enemy) {
        return TestSpecs.wave(0.2, WaveSpec.Formation.SINGLE, enemy, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    /** A plume row that reaches the swimmer's row (240 px above the bottom edge) while it burns. */
    private static HazardSpec.VentRow under() {
        // Enters at t; at 110 px/s its disc centre comes down 300 px below the top edge after ~2.7 s.
        return new HazardSpec.VentRow(0.4, new double[] {240}, 64, 2.4, 1.6, 30, 0, 10, true, 40, BEND);
    }

    @Test
    void aBurningPlumeBurnsASwimmerOncePerBurstAndTheKillIsThePlayers() {
        Sortie sortie = sortie(
                level(List.of(under()), List.of(single(swimmer(Layer.AIR, 100)))),
                TestSpecs.loadout(),
                TestSpecs.RULES.withInvulnerableShip());
        int burns = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            burns += values(sortie, SimEvents.Type.PLUME_BURN).size();
        }
        assertEquals(1, burns, "once per burst");
        Enemy unit = AbyssRayTest.find(sortie, "swimmer");
        assertEquals(60, unit.hp(), 1e-9, "40 off");

        Sortie kill = sortie(
                level(List.of(under()), List.of(single(swimmer(Layer.AIR, 30)))),
                TestSpecs.loadout(),
                TestSpecs.RULES.withInvulnerableShip());
        int destroyed = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            kill.step(Command.NONE);
            destroyed += values(kill, SimEvents.Type.ENEMY_DESTROYED).size();
        }
        assertEquals(1, destroyed);
        assertEquals(7, kill.credits(), "its bounty is the player's");
    }

    @Test
    void aPlumeBurnsNoGroundUnitNorOneUnderTheSurface() {
        for (Layer layer : List.of(Layer.GROUND, Layer.SUB)) {
            Sortie sortie = sortie(
                    level(List.of(under()), List.of(single(swimmer(layer, 100)))),
                    TestSpecs.loadout(),
                    TestSpecs.RULES.withInvulnerableShip());
            int burns = 0;
            for (int i = 0; i < SimStep.ticks(6); i++) {
                sortie.step(Command.NONE);
                burns += values(sortie, SimEvents.Type.PLUME_BURN).size();
            }
            assertEquals(0, burns, layer + " is not burned");
        }
    }

    @Test
    void aShotEnteringABurningPlumeIsBentOnceAwayFromItsCentre() {
        // The ship fires straight up through a plume just right of its line.
        Sortie sortie = sortie(
                level(List.of(plumes(0.2, 250)), List.of()), TestSpecs.LOADOUT, TestSpecs.RULES.withInvulnerableShip());
        int bends = 0;
        int bentShots = 0;
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.FIRE.bit());
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.PLUME_BEND) {
                    bends++;
                    assertTrue(events.x(e) < 250, "the shots left of the centre");
                }
            }
            // Every shot that was bent turned left (away from the centre on its right), and only once.
            for (int k = 0; k < sortie.shotCount(); k++) {
                Shot shot = sortie.shot(k);
                if (!shot.bendable()) {
                    bentShots++;
                    assertEquals(-BEND, shot.heading(), 1e-9, "25° to the left, once");
                }
            }
        }
        assertTrue(bends > 0, "shots crossed the burning plume");
        assertTrue(bentShots > 0);
    }

    @Test
    void anEnemyBulletAndATorpedoAreBentButNotAMineNorADepthCharge() {
        Sortie sortie = sortie(level(List.of(plumes(0, 240)), List.of()), TestSpecs.loadout(), TestSpecs.RULES);
        TorpedoTest.run(sortie, SimStep.ticks(2.5), Command.NONE);
        FurnaceVents vents = sortie.vents();
        assertEquals(FurnaceVents.Phase.BURN, vents.phase(0));
        Missiles missiles = sortie.missiles();
        missiles.launchAlong(240 + 10, vents.y(0) - 40, DepthHunterTest.TORPEDO, Math.PI);
        sortie.step(Command.NONE);
        int bends = 0;
        for (int i = 0; i < SimStep.ticks(0.6); i++) {
            sortie.step(Command.NONE);
            bends += values(sortie, SimEvents.Type.PLUME_BEND).size();
        }
        assertEquals(1, bends, "the torpedo, once");
        assertTrue(missiles.get(0).bent());

        Shot charge = new Shot();
        charge.fire(DepthChargeTest.CHARGES, 0, 0, 0, 0);
        assertFalse(charge.bendable(), "a depth charge");
    }

    @Test
    void thePlumesReplayToTheSameHashAndDoNotAllocate() {
        Supplier<Sortie> make = () -> sortie(
                level(
                        List.of(plumes(0.5, 120, 360), plumes(5, 240), plumes(9, 120, 360)),
                        List.of(single(swimmer(Layer.AIR, 100)), DepthHunterTest.column(3, 3))),
                TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK),
                TestSpecs.RULES);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(16); i++) {
            int command =
                    i % 100 < 50 ? Command.FIRE.bit() | Command.LEFT.bit() : Command.FIRE.bit() | Command.RIGHT.bit();
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(16); i++) {
                sortie.step(
                        i % 100 < 50
                                ? Command.FIRE.bit() | Command.LEFT.bit()
                                : Command.FIRE.bit() | Command.RIGHT.bit());
            }
        });
        assertEquals(0, allocated);
    }
}
