package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Round 41, the Siege Spire after the user's playtest of 2026-10-19 (pt3-findings items 1, 2 and 5): the warning
 * ahead of its arrival ({@link SimEvents.Type#BOSS_INCOMING}), no fire before the halt and its first phase's delay,
 * the leading sweep that reaches the whole field (no still place is safe, a ship that crosses the telegraph's line
 * is), and the dead spire's parts scrolling away with the ground.
 */
class SpirePlaytestTest {
    /** The sweep proposed for Level 14: 120° over 1.2 s after a 0.8 s telegraph, 600 px long, 8 px wide, 12° lead. */
    static final EnemySpec.Sweep LEADING =
            new EnemySpec.Sweep(Math.toRadians(120), 1.2, 0.8, 600, 8, 5, 0, 8, 0, 20, Math.toRadians(12));

    // --- 1. the warning and the held fire ------------------------------------------------------------

    @Test
    void theWarningComesItsLeadBeforeTheArrivalOnceAndNotAgainOnARetryFromTheBoss() {
        SetPieceSpec spire = withBoss(PartHSpecs.spire(0, 10, 120), boss -> boss.withIncomingSeconds(2.5));
        Sortie sortie = PartHSpecs.sortie(PartHSpecs.level(spire), TestSpecs.RULES.withInvulnerableShip());
        long incomingAt = -1;
        long arrivedAt = -1;
        for (int i = 0; i < SimStep.ticks(PartHSpecs.ARRIVE + 2); i++) {
            sortie.step(0);
            List<Integer> incoming = PartHSpecs.values(sortie, SimEvents.Type.BOSS_INCOMING);
            if (!incoming.isEmpty()) {
                assertEquals(-1, incomingAt, "once");
                assertEquals(List.of(0), incoming, "the set piece's index");
                incomingAt = sortie.tick();
                assertTrue(
                        sortie.setPiece(0).approaching() || !sortie.setPiece(0).present(), "before it is reached");
            }
            if (!PartHSpecs.values(sortie, SimEvents.Type.BOSS_ARRIVED).isEmpty()) {
                arrivedAt = sortie.tick();
            }
        }
        assertEquals(SimStep.ticks(0.5), incomingAt, "2.5 s before the arrival at 3 s");
        assertEquals(SimStep.ticks(2.5), arrivedAt - incomingAt);
        sortie.retryFromBoss();
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(0);
            assertTrue(
                    PartHSpecs.values(sortie, SimEvents.Type.BOSS_INCOMING).isEmpty(),
                    "the retry starts at the arrival");
        }
    }

    @Test
    void withoutALeadThereIsNoWarningEvent() {
        Sortie sortie = PartHSpecs.sortie(PartHSpecs.level(PartHSpecs.spire()), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(PartHSpecs.ARRIVE + 2); i++) {
            sortie.step(0);
            assertTrue(PartHSpecs.values(sortie, SimEvents.Type.BOSS_INCOMING).isEmpty());
        }
    }

    @Test
    void itHoldsItsFireThroughTheApproachAndItsFirstPhasesDelayAfterTheHalt() {
        for (double delay : new double[] {0, 2}) {
            SetPieceSpec spire = withBoss(PartHSpecs.spire(0, 10, 120), boss -> withFirstDelay(boss, delay));
            Sortie sortie = PartHSpecs.sortie(PartHSpecs.level(spire), TestSpecs.RULES.withInvulnerableShip());
            SetPiece boss = sortie.setPiece(0);
            long settled = -1;
            long firstShot = -1;
            for (int i = 0; i < SimStep.ticks(PartHSpecs.ARRIVE + 8) && firstShot < 0; i++) {
                sortie.step(0);
                if (!PartHSpecs.values(sortie, SimEvents.Type.BOSS_SETTLED).isEmpty()) {
                    settled = sortie.tick();
                    assertTrue(sortie.scrollHalted(), "it settles at the halt: its approach is over");
                    for (int p : PartHSpecs.ROOTS) {
                        assertTrue(boss.partOnField(p), "root " + p + " on the field");
                    }
                }
                boolean fired = sortie.bulletCount() > 0
                        || sortie.lobCount() > 0
                        || !PartHSpecs.values(sortie, SimEvents.Type.AIM_TELL).isEmpty();
                if (fired) {
                    firstShot = sortie.tick();
                }
            }
            assertTrue(settled > 0, "it settled");
            assertTrue(firstShot > settled, "nothing before the halt (delay " + delay + ")");
            assertTrue(
                    firstShot - settled >= SimStep.ticks(delay),
                    "nothing in the first phase's delay of " + delay + " s: " + (firstShot - settled));
        }
    }

    // --- 2. the leading sweep -------------------------------------------------------------------------

    @Test
    void theLeadPutsTheTelegraphsLineItsLeadFromTheShipsBearingOnTheSideTheBeamComesFrom() {
        Sortie sortie = freeSpire(leadingSpire(120));
        SetPiece boss = sortie.setPiece(0);
        int a = PartHSpecs.SWEEP;
        for (int sweep = 0; sweep < 2; sweep++) {
            PartHSpecs.until(sortie, SimEvents.Type.SPIRE_SWEEP_TELEGRAPH, 5.1, 0);
            double bearing = Math.atan2(
                    -(sortie.ship().x() - boss.sweepOriginX(a)), -(sortie.ship().y() - boss.sweepOriginY(a)));
            double direction = Math.signum(boss.sweepTo(a) - boss.sweepFrom(a));
            assertEquals(bearing - direction * Math.toRadians(12), boss.sweepFrom(a), 1e-9, "the line 12° short");
            assertEquals(Math.toRadians(120), Math.abs(boss.sweepTo(a) - boss.sweepFrom(a)), 1e-9);
        }
    }

    @Test
    void noStillPlaceOnTheFieldIsSafeForASweep() {
        // Every place the ship can stand but under the free spire's swaying body (its contact hurts there).
        double margin = TestSpecs.SHIP.edgeLimit();
        double[] xs = {margin, 140, 240, 340, PlayField.WIDTH - margin};
        double[] ys = {margin, 150, 270, 390, PlayField.HEIGHT - margin};
        for (double x : xs) {
            for (double y : ys) {
                if (underTheBody(x, y)) {
                    continue;
                }
                Sortie sortie = freeSpire(leadingSpire(120));
                flyTo(sortie, x, y);
                PartHSpecs.until(sortie, SimEvents.Type.SPIRE_SWEEP_TELEGRAPH, 5.1, 0);
                assertTrue(swept(sortie, null), "a ship standing still at (" + x + ", " + y + ") is swept");
            }
        }
    }

    @Test
    void aShipThatCrossesTheTelegraphsLineIsNeverSwept() {
        // In the open (a ship pinned to a wall may find the way across barred: it has to keep moving).
        double[][] starts = {{120, 100}, {240, 100}, {360, 100}, {140, 250}, {340, 250}, {100, 380}, {380, 380}};
        for (double[] start : starts) {
            Sortie sortie = freeSpire(leadingSpire(120));
            flyTo(sortie, start[0], start[1]);
            for (int sweep = 0; sweep < 2; sweep++) {
                // Both directions: the sweeps alternate.
                PartHSpecs.until(sortie, SimEvents.Type.SPIRE_SWEEP_TELEGRAPH, 5.1, 0);
                assertFalse(
                        swept(sortie, new double[] {0.2}),
                        "a ship starting at (" + start[0] + ", " + start[1] + ") crossing the line 0.2 s into the"
                                + " telegraph, sweep " + sweep);
            }
        }
    }

    /** Whether the free spire's body (the test pose's 100 × 140 hit box) passes over a ship at (x, y) as it sways. */
    private static boolean underTheBody(double x, double y) {
        double hull = TestSpecs.SHIP.size() / 2.0;
        return Math.abs(y - PartHSpecs.FREE_Y) < 70 + hull
                && Math.abs(x - PartHSpecs.FREE_X) < PartHSpecs.SWAY + 50 + hull;
    }

    // --- 5. the death ----------------------------------------------------------------------------------

    @Test
    void afterTheKillEveryPartMovesWithTheGroundScroll() {
        Sortie sortie = PartHSpecs.atLift(TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = sortie.setPiece(0);
        PartHSpecs.until(sortie, SimEvents.Type.SPIRE_LIFTED, PartHSpecs.LIFT_SECONDS + 0.1, 0);
        sortie.destroyPart(0, PartHSpecs.CORE);
        assertTrue(boss.destroyed());
        double[] ys = new double[boss.partCount()];
        double scrolled = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            for (int p = 0; p < ys.length; p++) {
                ys[p] = boss.partY(p);
            }
            double scroll = sortie.groundScroll();
            double y = boss.y();
            sortie.step(0);
            double step = sortie.groundScroll() - scroll;
            scrolled += step;
            assertEquals(y - step, boss.y(), 1e-9, "the dead spire moves down with the ground");
            for (int p = 0; p < ys.length; p++) {
                assertEquals(ys[p] - step, boss.partY(p), 1e-9, "part " + p + " moves with the ground");
            }
        }
        assertTrue(scrolled > 20, "the ground scrolls on after the kill: " + scrolled);
    }

    // --- helpers ---------------------------------------------------------------------------------------

    /** The test spire with the leading sweep over {@code arc}° and no Wraiths. */
    static SetPieceSpec leadingSpire(double arc) {
        return withBoss(PartHSpecs.spire(0, 10, arc), boss -> {
            List<BossSpec.Attack> attacks = new ArrayList<>(boss.attacks());
            BossSpec.Attack sweep = attacks.get(PartHSpecs.SWEEP);
            EnemySpec.Sweep leading = new EnemySpec.Sweep(
                    Math.toRadians(arc),
                    LEADING.sweepSeconds(),
                    LEADING.telegraphSeconds(),
                    LEADING.length(),
                    LEADING.width(),
                    LEADING.intervalSeconds(),
                    LEADING.firstDelaySeconds(),
                    LEADING.damage(),
                    LEADING.originIn(),
                    LEADING.originDown(),
                    LEADING.leadRadians());
            attacks.set(
                    PartHSpecs.SWEEP,
                    new BossSpec.Attack(
                            sweep.name(),
                            sweep.pattern(),
                            sweep.gun(),
                            sweep.burstGapSeconds(),
                            sweep.rotate(),
                            sweep.count(),
                            sweep.arms(),
                            sweep.turnRadiansPerSecond(),
                            sweep.durationSeconds(),
                            sweep.parts(),
                            sweep.stagger(),
                            Optional.of(leading),
                            sweep.down(),
                            sweep.aimTurns()));
            return BossSpec.builder(
                            boss.arriveSeconds(),
                            boss.x(),
                            boss.hoverY(),
                            boss.descentSpeed(),
                            boss.sineAmplitude(),
                            boss.sinePeriod(),
                            boss.layer(),
                            boss.midBoss(),
                            boss.barName(),
                            boss.parSeconds(),
                            boss.chains(),
                            attacks,
                            boss.phases())
                    .engagesOnArrival(boss.engagesOnArrival())
                    .armoured(boss.armoured())
                    .poses(boss.poses())
                    .deathSeconds(boss.deathSeconds())
                    .arena(boss.arena())
                    .incomingSeconds(boss.incomingSeconds())
                    .build();
        });
    }

    /** From the level start to the free spire at its station (the ship invulnerable: it only looks). */
    static Sortie freeSpire(SetPieceSpec spire) {
        Sortie sortie = PartHSpecs.sortie(PartHSpecs.level(spire), TestSpecs.RULES.withInvulnerableShip());
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        PartHSpecs.killRoots(sortie);
        PartHSpecs.until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        sortie.destroyPart(0, PartHSpecs.MAW);
        PartHSpecs.until(sortie, SimEvents.Type.SPIRE_LIFTED, PartHSpecs.LIFT_SECONDS + 0.2, 0);
        return sortie;
    }

    /** Flies the ship to (x, y) (within a step's flight) and stops it there. */
    static void flyTo(Sortie sortie, double x, double y) {
        for (int i = 0; i < SimStep.ticks(4); i++) {
            Ship ship = sortie.ship();
            double dx = x - ship.x();
            double dy = y - ship.y();
            if (Math.abs(dx) < 3 && Math.abs(dy) < 3) {
                break;
            }
            int commands = (dx > 2 ? Command.RIGHT.bit() : dx < -2 ? Command.LEFT.bit() : 0)
                    | (dy > 2 ? Command.UP.bit() : dy < -2 ? Command.DOWN.bit() : 0);
            sortie.step(commands);
        }
        sortie.step(0);
        sortie.step(0);
    }

    /**
     * The sweep whose telegraph started in the last step, to its end: whether its beam touched the ship. Without
     * {@code reaction} the ship stands still; with it, it stands still for {@code reaction[0]} s and then flies the
     * way that crosses the telegraph's line soonest (of its eight, along a wall it touches) until it is {@link
     * #CLEAR} behind it.
     */
    static boolean swept(Sortie sortie, double[] reaction) {
        SetPiece boss = sortie.setPiece(0);
        int a = PartHSpecs.SWEEP;
        double from = boss.sweepFrom(a);
        double direction = Math.signum(boss.sweepTo(a) - from);
        // The side behind the telegraph's line: the way the beam does not go.
        double nx = direction * Math.cos(from);
        double ny = -direction * Math.sin(from);
        int commands = escape(sortie.ship(), nx, ny);
        boolean touched = false;
        int steps = SimStep.ticks(LEADING.telegraphSeconds() + LEADING.sweepSeconds()) + 1;
        for (int i = 0; i < steps; i++) {
            Ship at = sortie.ship();
            // Across the line by its hull and the beam: it stops there, behind the beam.
            double behind = (at.x() - boss.sweepOriginX(a)) * nx + (at.y() - boss.sweepOriginY(a)) * ny;
            boolean moving = reaction != null && i >= SimStep.ticks(reaction[0]) && behind < CLEAR;
            sortie.step(moving ? commands : 0);
            Ship ship = sortie.ship();
            touched |= boss.sweepBeaming(a)
                    && HitPasses.beamTouches(boss, a, ship.spec().hull(), ship.x(), ship.y());
        }
        return touched;
    }

    /** How far behind the telegraph's line a dodging ship stops, px: its hull's reach and the beam's half width, and some. */
    private static final double CLEAR = 40;

    /** The command of the eight ways that moves the ship furthest along (nx, ny), without one into a wall it is at. */
    private static int escape(Ship ship, double nx, double ny) {
        double margin = ship.spec().edgeLimit();
        int best = 0;
        double most = Double.NEGATIVE_INFINITY;
        for (int sx = -1; sx <= 1; sx++) {
            for (int sy = -1; sy <= 1; sy++) {
                if (sx == 0 && sy == 0) {
                    continue;
                }
                double scale = sx != 0 && sy != 0 ? Math.sqrt(0.5) : 1;
                double vx = sx * scale;
                double vy = sy * scale;
                if ((sx < 0 && ship.x() <= margin + 1) || (sx > 0 && ship.x() >= PlayField.WIDTH - margin - 1)) {
                    vx = 0;
                }
                if ((sy < 0 && ship.y() <= margin + 1) || (sy > 0 && ship.y() >= PlayField.HEIGHT - margin - 1)) {
                    vy = 0;
                }
                double along = vx * nx + vy * ny;
                if (along > most) {
                    most = along;
                    best = (sx > 0 ? Command.RIGHT.bit() : sx < 0 ? Command.LEFT.bit() : 0)
                            | (sy > 0 ? Command.UP.bit() : sy < 0 ? Command.DOWN.bit() : 0);
                }
            }
        }
        return best;
    }

    static SetPieceSpec withBoss(SetPieceSpec spec, java.util.function.UnaryOperator<BossSpec> change) {
        return new SetPieceSpec(
                spec.slug(),
                spec.size(),
                spec.body(),
                spec.contactDamage(),
                spec.parts(),
                spec.passes(),
                spec.drop(),
                Optional.of(change.apply(spec.boss().orElseThrow())));
    }

    /** {@code boss} with its first phase's delay {@code seconds}. */
    static BossSpec withFirstDelay(BossSpec boss, double seconds) {
        List<BossSpec.Phase> phases = new ArrayList<>(boss.phases());
        BossSpec.Phase first = phases.getFirst();
        phases.set(
                0,
                new BossSpec.Phase(
                        first.name(),
                        first.untilParts(),
                        first.left(),
                        first.attacks(),
                        first.alternate(),
                        first.stream(),
                        first.exposes(),
                        first.bendRadians(),
                        first.seconds(),
                        seconds,
                        first.move(),
                        first.windows(),
                        first.arena()));
        return BossSpec.builder(
                        boss.arriveSeconds(),
                        boss.x(),
                        boss.hoverY(),
                        boss.descentSpeed(),
                        boss.sineAmplitude(),
                        boss.sinePeriod(),
                        boss.layer(),
                        boss.midBoss(),
                        boss.barName(),
                        boss.parSeconds(),
                        boss.chains(),
                        boss.attacks(),
                        phases)
                .engagesOnArrival(boss.engagesOnArrival())
                .armoured(boss.armoured())
                .poses(boss.poses())
                .deathSeconds(boss.deathSeconds())
                .arena(boss.arena())
                .incomingSeconds(boss.incomingSeconds())
                .build();
    }
}
