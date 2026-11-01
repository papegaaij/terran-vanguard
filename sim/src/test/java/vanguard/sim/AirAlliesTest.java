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
 * M10 part C, step C1 (design/campaign Level 37, The shuttles): air allies outside the objectives. Six of Level 10's
 * shuttles lift in three pairs, each pair at its own time from its pads to its stations (untouchable), holds them and
 * climbs out (untouchable); a pair's "FRIENDLIES INBOUND" banner opens 2 s before it lifts ({@link
 * Sortie#friendliesInbound()} and {@link SimEvents.Type#FRIENDLIES_INBOUND}); enemy bullets and bodies hurt them as
 * Level 10's; losing them all never fails the mission and they pay nothing of their own; the waves the escort hooks aim
 * at the nearest shuttle on its station, the other units of the same kind at the ship. Deterministic, allocation-free.
 */
class AirAlliesTest {
    private static Sortie sortie(List<WaveSpec> waves, List<Integer> hooked) {
        return Act6CSpecs.sortie(Act6CSpecs.level(Act6CSpecs.SAVED, waves, hooked));
    }

    private static void runTo(Sortie sortie, double seconds) {
        while (sortie.levelSeconds() < seconds - 1e-9) {
            sortie.step(Command.NONE);
        }
    }

    /** Where a unit of the pair lifting at {@code lift} is at {@code t}: on its pad, lifting, on station, out, home. */
    private static Ally.State expected(double lift, double t) {
        double hold = lift + Act6CSpecs.LIFT_SECONDS;
        double climb = hold + Act6CSpecs.HOLD_SECONDS;
        if (t < lift) {
            return Ally.State.PAD;
        }
        if (t < hold) {
            return Ally.State.LIFTING;
        }
        if (t < climb) {
            return Ally.State.FLYING;
        }
        return t < climb + Act6CSpecs.CLIMB_SECONDS ? Ally.State.CLIMBING : Ally.State.HOME;
    }

    @Test
    void eachPairLiftsHoldsAndClimbsOutAtItsOwnTimes() {
        Sortie sortie = sortie(List.of(), List.of());
        assertTrue(sortie.airEscort());
        assertEquals(6, sortie.allyCount());
        int checked = 0;
        while (sortie.levelSeconds() < Act6CSpecs.END - 1) {
            sortie.step(Command.NONE);
            double t = sortie.levelSeconds();
            int home = 0;
            for (int k = 0; k < 6; k++) {
                double lift = Act6CSpecs.LIFTS[k / 2];
                Ally ally = sortie.ally(k);
                home += ally.state() == Ally.State.HOME ? 1 : 0;
                Ally.State state = expected(lift, t);
                if (state != expected(lift, t - SimStep.SECONDS) || state != expected(lift, t + SimStep.SECONDS)) {
                    continue; // a step from a boundary
                }
                assertEquals(state, ally.state(), "unit " + k + " at " + t);
                assertEquals(state == Ally.State.FLYING, ally.touchable(), "touchable only on its station");
                if (state == Ally.State.FLYING) {
                    ObjectiveSpec.Station station = Act6CSpecs.stations().get(k);
                    assertEquals(station.xAt(t), ally.x(), 1e-9);
                    assertEquals(station.yAt(t), ally.y(), 1e-9);
                }
                checked++;
            }
            assertEquals(home, sortie.alliesHome());
        }
        assertTrue(checked > 6 * SimStep.ticks(30));
        assertEquals(6, sortie.alliesHome());
    }

    @Test
    void eachPairsBannerOpensTwoSecondsBeforeItLifts() {
        Sortie sortie = sortie(List.of(), List.of());
        List<Double> opened = new ArrayList<>();
        List<Integer> groups = new ArrayList<>();
        List<Double> inbound = new ArrayList<>();
        boolean was = false;
        while (sortie.levelSeconds() < 30) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.FRIENDLIES_INBOUND) {
                    opened.add(sortie.levelSeconds());
                    groups.add(events.value(i));
                    assertEquals(240, events.x(i), 1e-9, "at its pads' centre");
                }
            }
            if (sortie.friendliesInbound() && !was) {
                inbound.add(sortie.levelSeconds());
            }
            was = sortie.friendliesInbound();
        }
        assertEquals(List.of(0, 1, 2), groups);
        for (int pair = 0; pair < 3; pair++) {
            assertEquals(Act6CSpecs.LIFTS[pair] - 2, opened.get(pair), 1e-9);
            assertEquals(Act6CSpecs.LIFTS[pair] - 2, inbound.get(pair), 1e-9, "the banner's own flag");
        }
    }

    @Test
    void losingThemAllNeverFailsTheMissionAndTheyPayNothingOfTheirOwn() {
        Sortie sortie = Act6CSpecs.vulnerable(Act6CSpecs.level(Secondary.NONE, List.of(), List.of()));
        for (int pair = 0; pair < 3; pair++) {
            runTo(sortie, Act6CSpecs.LIFTS[pair] + Act6CSpecs.LIFT_SECONDS + 1);
            Act6CSpecs.fromBelow(sortie, 2 * pair, 500);
            Act6CSpecs.fromBelow(sortie, 2 * pair + 1, 500);
            runTo(sortie, sortie.levelSeconds() + 0.5);
            assertFalse(sortie.ally(2 * pair).alive());
            assertFalse(sortie.ally(2 * pair + 1).alive());
        }
        assertFalse(sortie.primaryFailed(), "air allies outside the objectives never fail it");
        runTo(sortie, Act6CSpecs.END + 1);
        assertTrue(sortie.complete());
        assertEquals(0, sortie.credits(), "no pay per shuttle");
        assertEquals(LevelResult.Escort.NONE, sortie.result().escort(), "no escort in the debrief");
    }

    /** An aimed shooter holding 100 px below the top for 20 s, a shot (4) every 0.5 s. */
    private static final EnemySpec SHOOTER = EnemySpec.of(
                    "talon-stand-in", 1000, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 220)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(20, 20), new Range(100, 100))))
            .withGun(Optional.of(EnemyGun.aimed(0.5, 0.2, 1, 200, 4, false)));

    private static WaveSpec shooter(double t, WaveSpec.Edge edge) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, SHOOTER, 1, WaveSpec.Entry.FRONT, edge)
                .build();
    }

    @Test
    void aHookedWaveAimsAtTheNearestShuttleOnItsStationTheOthersAtTheShip() {
        // Wave 0 hooked, wave 1 (the same kind) not: both on the screen while pair 1 holds its stations.
        Sortie sortie = sortie(List.of(shooter(9, WaveSpec.Edge.LEFT), shooter(9, WaveSpec.Edge.RIGHT)), List.of(0));
        runTo(sortie, 12);
        Enemy hooked = null;
        Enemy free = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            if (unit.wave() == 0) {
                hooked = unit;
            } else {
                free = unit;
            }
        }
        assertTrue(hooked != null && free != null);
        int seenHooked = 0;
        int seenFree = 0;
        for (int step = 0; step < SimStep.ticks(2); step++) {
            int before = sortie.bulletCount();
            sortie.step(Command.NONE);
            for (int b = before; b < sortie.bulletCount(); b++) {
                EnemyBullet bullet = sortie.bullet(b);
                double heading = Math.atan2(bullet.vy(), bullet.vx());
                boolean fromHooked = Math.abs(bullet.x() - hooked.x()) < Math.abs(bullet.x() - free.x());
                Enemy from = fromHooked ? hooked : free;
                double toShip =
                        Math.atan2(sortie.ship().y() - from.y(), sortie.ship().x() - from.x());
                if (fromHooked) {
                    Ally nearest = nearestOnStation(sortie, from);
                    double toShuttle = Math.atan2(nearest.y() - from.y(), nearest.x() - from.x());
                    assertEquals(toShuttle, heading, 0.05, "the hooked wave at the shuttle");
                    seenHooked++;
                } else {
                    assertEquals(toShip, heading, 0.05, "the unhooked wave at the ship");
                    seenFree++;
                }
            }
        }
        assertTrue(seenHooked > 0 && seenFree > 0, seenHooked + " / " + seenFree);
    }

    private static Ally nearestOnStation(Sortie sortie, Enemy from) {
        Ally best = null;
        for (int k = 0; k < sortie.allyCount(); k++) {
            Ally ally = sortie.ally(k);
            if (ally.touchable()
                    && (best == null
                            || Math.hypot(ally.x() - from.x(), ally.y() - from.y())
                                    < Math.hypot(best.x() - from.x(), best.y() - from.y()))) {
                best = ally;
            }
        }
        return best;
    }

    @Test
    void airAlliesFlyLiftGroupsBesideNoOtherConvoy() {
        ObjectiveSpec.Escort shuttles = Act6CSpecs.shuttles(List.of());
        assertThrows(
                IllegalArgumentException.class,
                () -> ObjectiveSpec.NONE.withEscort(Optional.of(shuttles)).withAirAllies(Optional.of(shuttles)));
        assertThrows(
                IllegalArgumentException.class,
                () -> ObjectiveSpec.Air.lifting(Act6CSpecs.stations().subList(0, 5), Act6CSpecs.groups(), List.of()),
                "one station per pad");
        LevelScript without = Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), List.of()).toBuilder()
                .objectives(ObjectiveSpec.NONE.withSecondary(Act6CSpecs.SAVED))
                .build();
        assertThrows(IllegalArgumentException.class, () -> Act6CSpecs.sortie(without), "a saved secondary needs them");
    }

    private static final int FIRE = Command.FIRE.bit();

    /** Step C6s: a heavy bolt, 30 a shot at 10 a second (four shots down a 120 HP shuttle). */
    private static final WeaponSpec HEAVY = TestSpecs.bolt(
            "heavy-bolt", 10, 30, 900, new Hitbox(4, 12), TestSpecs.INFINITE, 1, TestSpecs.muzzle(0, 21, 0));

    private static Loadout front(WeaponSpec weapon) {
        return TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, weapon, weapon));
    }

    /** Step C6s: a sortie of {@code level} with {@code loadout}, the ship invulnerable. */
    private static Sortie armed(LevelScript level, Loadout loadout) {
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Per step C6s's row: the ally events, the first-loss cue and the secondary's failure seen while {@code steps} run. */
    private static int[] fly(Sortie sortie, int command, int steps) {
        int[] seen = new int[4];
        for (int i = 0; i < steps; i++) {
            sortie.step(command);
            seen[0] += PartCSpecs.count(sortie, SimEvents.Type.ALLY_HIT);
            seen[1] += PartCSpecs.count(sortie, SimEvents.Type.ALLY_LOST);
            seen[2] += Act6CSpecs.cued(sortie, CueTrigger.FIRST_ALLY_LOST) ? 1 : 0;
            seen[3] += PartCSpecs.count(sortie, SimEvents.Type.OBJECTIVE_FAILED);
        }
        return seen;
    }

    @Test
    void thePlayersShotsHitTheShuttlesAndOneShotDownIsALostShuttle() {
        // Step C6s (round 75 F.3): pair 0 on its stations from t=9, over the ship in the centre lane.
        Sortie sortie = armed(Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false), front(HEAVY));
        runTo(sortie, 9.5);
        int[] seen = fly(sortie, FIRE, SimStep.ticks(3));
        assertFalse(sortie.ally(1).alive(), "the lower shuttle, first in the line of fire");
        assertFalse(sortie.ally(0).alive(), "then the one behind it");
        assertTrue(seen[0] >= 8, "each hit is an ally hit: " + seen[0]);
        assertEquals(2, seen[1], "two lost shuttles");
        assertEquals(1, seen[2], "the first loss's line");
        assertEquals(1, seen[3], "the saved secondary (5 of 6) fails at the second loss");
        assertFalse(sortie.primaryFailed(), "never the mission");
        assertEquals(0, sortie.kills(), "no kill");
        assertEquals(0, sortie.credits(), "no bounty");
        // Off (Level 10's escort, Level 37 as first built): the same fire passes them.
        Sortie off = armed(Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), false, false), front(HEAVY));
        runTo(off, 9.5);
        assertEquals(0, fly(off, FIRE, SimStep.ticks(3))[0]);
        assertTrue(off.ally(0).alive() && off.ally(1).alive());
    }

    @Test
    void theEscortsShotsPassAPlayersShotHitsAndTheSeekersNeverPickOne() {
        Sortie sortie = Act6CSpecs.sortie(Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false));
        runTo(sortie, 10);
        Ally ally = sortie.ally(1);
        int escortMount = TestSpecs.LOADOUT.armament().size();
        sortie.fire.shots().obtain().fire(HEAVY, escortMount, ally.x(), ally.y() - 30, 0);
        sortie.fire.shots().obtain().fire(HEAVY, escortMount + 1, ally.x(), ally.y() - 30, 0);
        assertEquals(0, fly(sortie, 0, 3)[0], "Rook's (and a Reflex bolt's) shots pass");
        assertEquals(1, ally.hpShare(), 1e-12);
        sortie.fire.shots().obtain().fire(HEAVY, 0, ally.x(), ally.y() - 30, 0);
        assertEquals(1, fly(sortie, 0, 3)[0], "the player's shot hits, once");
        assertEquals(1 - 30.0 / 120, ally.hpShare(), 1e-9);
        // The seekers: homing shots fired with only the shuttles on the screen lock onto nothing.
        Sortie seeker = armed(Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false), front(TestSpecs.HOMING));
        runTo(seeker, 9.5);
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            seeker.step(FIRE);
            for (int s = 0; s < seeker.fire.shots().size(); s++) {
                assertTrue(seeker.fire.shots().get(s).target() < 0, "no lock on a shuttle");
                shots++;
            }
        }
        assertTrue(shots > 0);
    }

    @Test
    void aBeamStopsOnAShuttleAndBurnsIt() {
        Sortie sortie = armed(
                Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false), TestSpecs.loadout(PartASpecs.beamMount(3)));
        runTo(sortie, 9.5);
        int[] seen = fly(sortie, FIRE, SimStep.ticks(0.5));
        Ally lower = sortie.ally(1);
        assertTrue(seen[0] > 0 && lower.hpShare() < 1, "the beam burns the lower shuttle");
        assertEquals(1, sortie.ally(0).hpShare(), 1e-12, "it stops there: the one behind is untouched");
        Beam beam = sortie.beam(0);
        assertEquals(lower.y() - 14 + Beam.BORE, beam.endY(), 2, "the beam ends at the shuttle's lower edge");
        fly(sortie, FIRE, SimStep.ticks(3));
        assertFalse(lower.alive(), "burnt down: a lost shuttle");
    }

    @Test
    void theSmartBombsRingHurtsEachShuttleOnStationOnce() {
        SmartBombSpec bomb = new SmartBombSpec(0.35, 50, 60, 1.0, 1.5, 0.1, 0.8, 0.25);
        Sortie sortie = armed(
                Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false),
                TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Smart Bomb", 1, 3, 0.1, bomb)));
        runTo(sortie, 9.5);
        sortie.step(Command.SPECIAL.bit());
        int[] seen = fly(sortie, 0, SimStep.ticks(1.5));
        assertEquals(2, seen[0], "one hit each");
        assertEquals(1 - 50.0 / 120, sortie.ally(0).hpShare(), 1e-9);
        assertEquals(1 - 50.0 / 120, sortie.ally(1).hpShare(), 1e-9);
        assertEquals(1, sortie.ally(2).hpShare(), 1e-12, "pair 1 still on its pads: untouchable");
    }

    @Test
    void theOrbitalLancesColumnBurnsAShuttleDown() {
        Sortie sortie = armed(
                Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), true, false),
                TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.lance(1)));
        runTo(sortie, 9.2);
        sortie.step(Command.SPECIAL.bit());
        int[] seen = fly(sortie, 0, SimStep.ticks(4));
        assertTrue(seen[0] > 0, "the column burns the shuttles in it");
        assertTrue(sortie.ally(0).hpShare() < 1 && sortie.ally(1).hpShare() < 1);
    }

    @Test
    void withASlideAsideEachHoldsThenSlidesTowardItsEdgeAndClimbsOutAlongIt() {
        Sortie sortie = Act6CSpecs.sortie(Act6CSpecs.level(Act6CSpecs.SAVED, List.of(), false, true));
        double lifted = Act6CSpecs.LIFTS[0] + Act6CSpecs.LIFT_SECONDS;
        double slide = lifted + Act6CSpecs.ASIDE_HOLD;
        double climb = slide + Act6CSpecs.ASIDE_SECONDS;
        double home = climb + Act6CSpecs.CLIMB_SECONDS;
        double[] lastY = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        int[] checked = new int[4];
        while (sortie.levelSeconds() < home + 1) {
            sortie.step(Command.NONE);
            double t = sortie.levelSeconds();
            for (int k = 0; k < 2; k++) {
                Ally ally = sortie.ally(k);
                ObjectiveSpec.Station station = Act6CSpecs.stations().get(k);
                double edge = Act6CSpecs.ASIDE_X.get(k);
                double fromX = station.xAt(slide);
                double fromY = station.yAt(slide);
                if (t > lifted + 0.05 && t < slide - 0.05) {
                    assertEquals(Ally.State.FLYING, ally.state());
                    assertTrue(ally.touchable());
                    assertEquals(station.xAt(t), ally.x(), 1e-9, "on its station");
                    assertEquals(station.yAt(t), ally.y(), 1e-9);
                    checked[0]++;
                } else if (t > slide + 0.05 && t < climb - 0.05) {
                    double s = (t - slide) / Act6CSpecs.ASIDE_SECONDS;
                    double e = s * s * (3 - 2 * s);
                    assertEquals(Ally.State.FLYING, ally.state());
                    assertTrue(ally.touchable(), "touchable while it slides");
                    assertEquals(fromX + (edge - fromX) * e, ally.x(), 1e-6, "toward its side edge");
                    assertEquals(fromY - Act6CSpecs.ASIDE_DROP * e, ally.y(), 1e-6, "and a little down");
                    checked[1]++;
                } else if (t > climb + 0.05 && t < home - 0.05) {
                    assertEquals(Ally.State.CLIMBING, ally.state());
                    assertFalse(ally.touchable());
                    assertEquals(edge, ally.x(), 1e-12, "out along its edge");
                    assertTrue(ally.y() > fromY - Act6CSpecs.ASIDE_DROP && ally.y() > lastY[k], "climbing");
                    checked[2]++;
                } else if (t > home + 0.05) {
                    assertEquals(Ally.State.HOME, ally.state());
                    checked[3]++;
                }
                lastY[k] = ally.y();
            }
        }
        for (int part : checked) {
            assertTrue(part > 20, java.util.Arrays.toString(checked));
        }
        assertEquals(2, sortie.alliesHome());
        assertEquals(
                ObjectiveSpec.Lift.holding(
                                Act6CSpecs.asideGroups().getFirst().liftoff(),
                                Act6CSpecs.ASIDE_HOLD,
                                Act6CSpecs.asideGroups().getFirst().aside().orElseThrow(),
                                Act6CSpecs.CLIMB_SECONDS)
                        .asideStart(),
                slide,
                1e-9);
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectiveSpec.Lift(
                        Act6CSpecs.groups().getFirst().liftoff(),
                        new ObjectiveSpec.Climb(lifted + 1, 3),
                        Optional.of(new ObjectiveSpec.Aside(Act6CSpecs.ASIDE_X, 40, 1.5))),
                "the slide starts after the lift");
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectiveSpec.Aside(List.of(90.0, 500.0), 40, 1.5),
                "it ends on the play field");
    }

    @Test
    void theShuttlesWithHitsAndASlideReplayAndAllocateNothing() {
        Supplier<Sortie> make = () -> armed(
                Act6CSpecs.level(
                        Act6CSpecs.SAVED,
                        List.of(shooter(9, WaveSpec.Edge.LEFT), shooter(20, WaveSpec.Edge.RIGHT)),
                        true,
                        true),
                front(TestSpecs.PULSE).withSpecial(Act3SpecialsTest.lance(2)));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(35); i++) {
            int command = (i % 7 < 3 ? FIRE : 0) | (i == SimStep.ticks(10) ? Command.SPECIAL.bit() : 0);
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertTrue(a.ally(0).hpShare() < 1 || a.ally(1).hpShare() < 1, "the player's fire hit");
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(35); i++) {
                sortie.step((i % 7 < 3 ? FIRE : 0) | (i == SimStep.ticks(10) ? Command.SPECIAL.bit() : 0));
            }
        }));
    }

    @Test
    void itReplaysAndAllocatesNothing() {
        Supplier<Sortie> make =
                () -> sortie(List.of(shooter(9, WaveSpec.Edge.LEFT), shooter(20, WaveSpec.Edge.RIGHT)), List.of(0, 1));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(35); i++) {
            a.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            b.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(35); i++) {
                sortie.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            }
        }));
    }
}
