package vanguard.sim;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M9 part B (design/enemies, Rock riding; design/enemies/space/asteroid-mite; m9b-gaps.md §1.5): the rocks of a {@link
 * Sortie}'s waves on rocks ({@link WaveSpec#onRocks()}) and their riders. A wave's first rider to enter places its rock
 * (a small breakable {@link Debris} chunk with a serial from {@link #SERIAL_BASE}, so it blocks shots of both sides and
 * breaks as the debris does); each step after the rocks moved ({@link #step}) it places the riders on their rocks'
 * rims, crawling round them, starts a rock's leaps when the ship's centre comes within its riders' reach of the rock's
 * centre, frees a broken rock's riders (they tumble off, {@link #TUMBLE_SPEED} outward), and lets a rock that left the
 * screen carry its clinging riders off (they leave without paying). Each rider's tell starts its leap order × the gap
 * after the rock's leap (a broken rock's: after the tumble), holds still for the tell, then leaps: a straight dive at
 * the ship's position then ({@link Enemy.Phase#LEAVE}). Riders keep their offsets on an unrotated rock (the look draws
 * the rocks unrotated; the crawl is the riders'), so the hits match the look. Allocation-free in a step; hashed only in a
 * level with rocks.
 */
final class RockRides {
    /** The serials of the waves' rocks start here (above a thrown rock's). */
    static final int SERIAL_BASE = 1 << 21;
    /** A rider tumbling off a broken rock drifts outward at this, px/s, on top of the rock's last drift. */
    static final double TUMBLE_SPEED = 40;

    private static final int WAITING = 0;
    private static final int ON = 1;
    private static final int BROKEN = 2;
    private static final int GONE = 3;

    /**
     * A rider's seat: its wave's rock {@code rock} (its number in the plan), the rock's entry {@code at} and kind {@code
     * chunk}, its place on the rim {@code angle} (radians, y up) and its turn in the rock's leap order.
     */
    record Seat(int rock, WaveSpec.Rock at, HazardSpec.DebrisSpec chunk, double angle, int order) {}

    private final Pool<Debris> debris;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    /** Per rock: {@link #WAITING}, {@link #ON}, {@link #BROKEN} or {@link #GONE}. */
    private final int[] state;
    /** Per rock: its chunk while it is on (null otherwise). */
    private final Debris[] chunks;
    /** Per rock: where it was and its drift, last seen (a broken rock's riders tumble from there). */
    private final double[] lastX;

    private final double[] lastY;
    private final double[] lastVx;
    private final double[] lastVy;
    /** Per rock: the step its leaps (or its riders' tumble) started; -1 before. */
    private final int[] leapTick;
    /** Per rock: its riders' reach, px (their stat block's {@code within}). */
    private final double[] within;
    /** Steps in this attempt. */
    private int ticks;
    /** Riders on their rocks now (none: the step skips the units). */
    private int riding;

    RockRides(int rocks, Pool<Debris> debris, Pool<Enemy> enemies, SimEvents events) {
        this.debris = debris;
        this.enemies = enemies;
        this.events = events;
        state = new int[rocks];
        chunks = new Debris[rocks];
        lastX = new double[rocks];
        lastY = new double[rocks];
        lastVx = new double[rocks];
        lastVy = new double[rocks];
        leapTick = new int[rocks];
        within = new double[rocks];
        reset();
    }

    /**
     * Plans the riders of {@code wave} (its units ride its rocks, {@link WaveSpec.OnRocks#riders()} a rock in the rocks'
     * order) into {@code out}, each with its seat in {@code seats}: evenly round the rim from a seeded start, the leap
     * order a seeded shuffle; its rocks' numbers start at {@code firstRock}. Returns the wave's rocks.
     */
    static int plan(WaveSpec wave, int kind, SplitMix64 rng, List<Spawn> out, Map<Spawn, Seat> seats, int firstRock) {
        WaveSpec.OnRocks onRocks = wave.onRocks().orElseThrow();
        int tick = SimStep.ticks(wave.t());
        int riders = onRocks.riders();
        int[] order = new int[riders];
        for (int r = 0; r < onRocks.rocks().size(); r++) {
            WaveSpec.Rock rock = onRocks.rocks().get(r);
            double start = rng.range(0, 2 * StrictMath.PI);
            for (int j = 0; j < riders; j++) {
                order[j] = j;
            }
            for (int j = riders - 1; j > 0; j--) {
                int k = rng.nextInt(j + 1);
                int swap = order[j];
                order[j] = order[k];
                order[k] = swap;
            }
            for (int j = 0; j < riders; j++) {
                Spawn spawn = new Spawn(
                        tick,
                        kind,
                        wave.enemy(),
                        FlightPath.through(rock.x(), rock.y(), rock.x() + rock.vx(), rock.y() + rock.vy()),
                        0,
                        0,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        Optional.empty());
                out.add(spawn);
                seats.put(
                        spawn,
                        new Seat(
                                firstRock + r,
                                rock,
                                onRocks.chunk(),
                                start + j * 2 * StrictMath.PI / riders,
                                order[j]));
            }
        }
        return onRocks.rocks().size();
    }

    /** Back to an attempt's start: no rock placed. */
    void reset() {
        java.util.Arrays.fill(state, WAITING);
        java.util.Arrays.fill(chunks, null);
        java.util.Arrays.fill(leapTick, -1);
        java.util.Arrays.fill(lastX, 0);
        java.util.Arrays.fill(lastY, 0);
        java.util.Arrays.fill(lastVx, 0);
        java.util.Arrays.fill(lastVy, 0);
        java.util.Arrays.fill(within, 0);
        ticks = 0;
        riding = 0;
    }

    /** {@code rider} enters on its seat: the first of its rock places the rock (none free: it starts tumbled off). */
    void board(Enemy rider, Seat seat) {
        int k = seat.rock();
        if (state[k] == WAITING) {
            Debris chunk = debris.obtain();
            within[k] = rider.spec.ride().orElseThrow().within();
            lastX[k] = seat.at().x();
            lastY[k] = seat.at().y();
            lastVx[k] = seat.at().vx();
            lastVy[k] = seat.at().vy();
            if (chunk == null) {
                state[k] = BROKEN;
                leapTick[k] = ticks;
            } else {
                chunk.enter(
                        seat.chunk(),
                        SERIAL_BASE + k,
                        seat.at().x(),
                        seat.at().y(),
                        seat.at().vx(),
                        seat.at().vy());
                chunks[k] = chunk;
                lastX[k] = chunk.x();
                lastY[k] = chunk.y();
                state[k] = ON;
            }
        }
        rider.riding.board(k, seat.angle(), seat.order());
        riding++;
        double rim = rim(seat.chunk());
        rider.x = rider.prevX = lastX[k] + rim * Trig.cos(seat.angle());
        rider.y = rider.prevY = lastY[k] + rim * Trig.sin(seat.angle());
        rider.facing = Enemy.heading(Trig.cos(seat.angle()), Trig.sin(seat.angle()));
    }

    /** A chunk broke (a shot): if it is a wave's rock, its riders are freed. */
    void broken(Debris chunk) {
        int k = chunk.serial() - SERIAL_BASE;
        if (k >= 0 && k < state.length && state[k] == ON) {
            seen(k, chunk);
            state[k] = BROKEN;
            chunks[k] = null;
            leapTick[k] = ticks;
        }
    }

    /** A chunk left the screen: if it is a wave's rock, its clinging riders are carried off with it. */
    void left(Debris chunk) {
        int k = chunk.serial() - SERIAL_BASE;
        if (k >= 0 && k < state.length && state[k] == ON) {
            seen(k, chunk);
            state[k] = GONE;
            chunks[k] = null;
        }
    }

    private void seen(int k, Debris chunk) {
        lastX[k] = chunk.x();
        lastY[k] = chunk.y();
        lastVx[k] = chunk.vx();
        lastVy[k] = chunk.vy();
    }

    /**
     * One step after the rocks moved, the ship's centre at (shipX, shipY): the rocks' leaps start, the riders are placed,
     * tumble, tell and leap.
     */
    void step(double shipX, double shipY) {
        ticks++;
        for (int k = 0; k < state.length; k++) {
            if (state[k] != ON) {
                continue;
            }
            seen(k, chunks[k]);
            if (leapTick[k] < 0) {
                double dx = shipX - lastX[k];
                double dy = shipY - lastY[k];
                if (dx * dx + dy * dy <= within[k] * within[k]) {
                    leapTick[k] = ticks;
                }
            }
        }
        if (riding == 0) {
            return;
        }
        int left = 0;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy rider = enemies.get(i);
            if (rider.riding.riding()) {
                ride(rider, shipX, shipY);
                left += rider.riding.riding() ? 1 : 0;
            }
        }
        riding = left;
    }

    /** One rider's step on (or off) its rock. */
    private void ride(Enemy u, double shipX, double shipY) {
        Riding r = u.riding;
        EnemySpec.Ride ride = u.spec.ride().orElseThrow();
        int k = r.rock;
        int gap = SimStep.ticks(ride.gapSeconds());
        int tell = Math.max(1, SimStep.ticks(ride.tellSeconds()));
        if (state[k] == GONE && r.state != Riding.TELL) {
            // Carried off the screen on its rock: it leaves with it (no credits).
            r.carriedOff = true;
            return;
        }
        if (state[k] == BROKEN && r.state == Riding.CLING) {
            r.state = Riding.TUMBLE;
            r.ticks = 0;
            r.waitTicks = SimStep.ticks(ride.tumbleSeconds()) + r.order * gap;
            u.vx = lastVx[k] + TUMBLE_SPEED * Trig.cos(r.angle);
            u.vy = lastVy[k] + TUMBLE_SPEED * Trig.sin(r.angle);
        }
        switch (r.state) {
            case Riding.CLING -> {
                r.angle += Math.toRadians(ride.crawlDegreesPerSecond()) * SimStep.SECONDS;
                place(u, k);
                if (leapTick[k] >= 0 && ticks - leapTick[k] >= r.order * gap) {
                    tell(u);
                }
            }
            case Riding.TUMBLE -> {
                u.x += u.vx * SimStep.SECONDS;
                u.y += u.vy * SimStep.SECONDS;
                if (++r.ticks >= r.waitTicks) {
                    tell(u);
                }
            }
            default -> {
                // Its tell: on the rim while the rock is on, where it is once the rock is gone.
                if (state[k] == ON) {
                    place(u, k);
                }
                if (++r.ticks >= tell) {
                    leap(u, ride, shipX, shipY);
                }
            }
        }
    }

    private void place(Enemy u, int k) {
        double rim = rim(chunks[k].spec());
        u.x = lastX[k] + rim * Trig.cos(u.riding.angle);
        u.y = lastY[k] + rim * Trig.sin(u.riding.angle);
        u.facing = Enemy.heading(Trig.cos(u.riding.angle), Trig.sin(u.riding.angle));
    }

    private void tell(Enemy u) {
        u.riding.state = Riding.TELL;
        u.riding.ticks = 0;
        events.add(SimEvents.Type.RIDER_TELL, u.x, u.y, u.kind);
    }

    /** Its leap: straight at the ship's position now at its dive speed (straight down onto the ship's own point). */
    private void leap(Enemy u, EnemySpec.Ride ride, double shipX, double shipY) {
        double dx = shipX - u.x;
        double dy = shipY - u.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        u.vx = length > 0 ? dx / length * ride.diveSpeed() : 0;
        u.vy = length > 0 ? dy / length * ride.diveSpeed() : -ride.diveSpeed();
        u.phase = Enemy.Phase.LEAVE;
        u.speed = ride.diveSpeed();
        u.facing = Enemy.heading(u.vx, u.vy);
        events.add(SimEvents.Type.RIDER_LEAP, u.x, u.y, u.kind);
    }

    /** How far from a rock's centre its riders cling, px: its half width. */
    private static double rim(HazardSpec.DebrisSpec chunk) {
        return chunk.size().width() / 2;
    }

    /** The rocks' state (only in a level with rocks). */
    void addTo(StateHash hash) {
        hash.add(ticks).add(riding);
        for (int k = 0; k < state.length; k++) {
            hash.add(state[k]).add(leapTick[k]);
        }
    }
}
