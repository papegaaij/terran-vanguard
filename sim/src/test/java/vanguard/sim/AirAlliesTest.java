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
