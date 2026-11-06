package vanguard.sim;

/**
 * M11 part A, step A1b (design/world/vrell-space, Hazards & set pieces: spore clouds; J13; Level 45): the level's spore
 * clouds in play, owned by a {@link Sortie} only in a level with {@link VrellLevel.Spores} ({@link Sortie#spores()}).
 * Each cloud enters at its time with its centre at its x above the top edge and drifts down with the scroll at its
 * layer's factor (up in a reversed scroll), until it is off the field. While the ship's centre is inside one, the
 * ship's speed and acceleration ease to the clouds' slow over their ease ({@link ShipSlow#cloud}; {@link
 * SimEvents.Type#SPORE_INSIDE}, {@link SimEvents.Type#SPORE_LEFT}), and Rook's the same; nothing else is slowed (the
 * player's drones and the Swarm Call's fly on). A player blast whose radius touches a cloud (an {@code area} hit:
 * shells, bombs, mines, charges, a torpedo's splash, the Tesla's sparks, Rook's Mortar) or a Plasma Arc strike passing
 * through it sets it burning ({@link SimEvents.Type#SPORE_BURN}): it shrinks to nothing over the burn; other shots pass.
 * A cloud pays nothing and stops nothing. Its own small pool; allocates nothing.
 */
public final class SporeClouds {
    /** The most clouds in play at once (the content allows {@link VrellLevel.Spores#most()}, 2). */
    static final int CAPACITY = 6;

    private final VrellLevel.Spores spec;
    private final int[] enterTicks;
    private final int easeTicks;
    private final int burnTicks;
    private final SimEvents events;
    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] radii = new double[CAPACITY];
    /** Steps of its burn left; -1 while it is whole. */
    private final int[] burning = new int[CAPACITY];

    private int count;
    /** The next cloud of the level still to enter. */
    private int next;
    /** Whether the ship's centre was inside a cloud in the last step. */
    private boolean shipInside;

    SporeClouds(VrellLevel.Spores spec, SimEvents events) {
        this.spec = spec;
        this.events = events;
        enterTicks = new int[spec.clouds().size()];
        for (int i = 0; i < enterTicks.length; i++) {
            enterTicks[i] = SimStep.ticks(spec.clouds().get(i).t());
        }
        easeTicks = Math.max(1, SimStep.ticks(spec.easeSeconds()));
        burnTicks = Math.max(1, SimStep.ticks(spec.burnSeconds()));
    }

    /** Back to the level start: no cloud in play. */
    void reset() {
        count = 0;
        next = 0;
        shipInside = false;
    }

    /** Back to a boss checkpoint at {@code levelTick}: an empty field, the clouds due by then gone. */
    void restore(int levelTick) {
        reset();
        while (next < enterTicks.length && enterTicks[next] <= levelTick) {
            next++;
        }
    }

    /**
     * One step at {@code levelTick}, the scroll having moved {@code scrollStep} px: the clouds due enter, drift and burn;
     * the ship's and Rook's slows ease toward inside or out.
     */
    void step(int levelTick, double scrollStep, Ship ship, Wingman rook) {
        while (next < enterTicks.length && enterTicks[next] <= levelTick && count < CAPACITY) {
            VrellLevel.Cloud cloud = spec.clouds().get(next++);
            xs[count] = cloud.x();
            ys[count] = PlayField.HEIGHT + cloud.radius();
            radii[count] = cloud.radius();
            burning[count] = -1;
            count++;
        }
        double drift = scrollStep * spec.scrollFactor();
        for (int i = count - 1; i >= 0; i--) {
            ys[i] -= drift;
            boolean gone = ys[i] + radii[i] < 0 || (drift < 0 && ys[i] - radii[i] > PlayField.HEIGHT + radii[i]);
            if (burning[i] >= 0 && --burning[i] <= 0) {
                gone = true;
            }
            if (gone) {
                remove(i);
            }
        }
        boolean inside = inside(ship.x(), ship.y());
        ship.slow.cloud(inside, spec.slow(), easeTicks);
        if (inside != shipInside) {
            events.add(inside ? SimEvents.Type.SPORE_INSIDE : SimEvents.Type.SPORE_LEFT, ship.x(), ship.y());
            shipInside = inside;
        }
        if (rook != null) {
            rook.slow.cloud(inside(rook.x(), rook.y()), spec.slow(), easeTicks);
        }
    }

    private void remove(int i) {
        count--;
        xs[i] = xs[count];
        ys[i] = ys[count];
        radii[i] = radii[count];
        burning[i] = burning[count];
    }

    /** Whether (x, y) is inside a cloud now (its burning radius for a burning one). */
    public boolean inside(double x, double y) {
        for (int i = 0; i < count; i++) {
            double r = radius(i);
            double dx = x - xs[i];
            double dy = y - ys[i];
            if (dx * dx + dy * dy < r * r) {
                return true;
            }
        }
        return false;
    }

    /** A player blast of {@code blast} px at (x, y): every whole cloud it touches starts to burn. */
    void burn(double x, double y, double blast) {
        for (int i = 0; i < count; i++) {
            double reach = radii[i] + blast;
            double dx = x - xs[i];
            double dy = y - ys[i];
            if (burning[i] < 0 && dx * dx + dy * dy < reach * reach) {
                ignite(i);
            }
        }
    }

    /** A Plasma Arc strike from (ax, ay) to (bx, by): every whole cloud it passes through starts to burn. */
    void burnLine(double ax, double ay, double bx, double by) {
        double vx = bx - ax;
        double vy = by - ay;
        double length = vx * vx + vy * vy;
        for (int i = 0; i < count; i++) {
            double u = length > 0 ? ((xs[i] - ax) * vx + (ys[i] - ay) * vy) / length : 0;
            u = Math.max(0, Math.min(1, u));
            double dx = ax + u * vx - xs[i];
            double dy = ay + u * vy - ys[i];
            if (burning[i] < 0 && dx * dx + dy * dy < radii[i] * radii[i]) {
                ignite(i);
            }
        }
    }

    private void ignite(int i) {
        burning[i] = burnTicks;
        events.add(SimEvents.Type.SPORE_BURN, xs[i], ys[i], (int) Math.round(radii[i]));
    }

    /** The clouds in play. */
    public int count() {
        return count;
    }

    public double x(int i) {
        return xs[i];
    }

    public double y(int i) {
        return ys[i];
    }

    /** Cloud {@code i}'s radius now: whole, or shrinking as it burns, px. */
    public double radius(int i) {
        return burning[i] < 0 ? radii[i] : radii[i] * burning[i] / burnTicks;
    }

    /** Whether cloud {@code i} is burning away (the ember edge); its share burnt, 0–1, by {@link #burnt(int)}. */
    public boolean burning(int i) {
        return burning[i] >= 0;
    }

    /** How much of cloud {@code i} has burnt away, 0 whole to 1 gone. */
    public double burnt(int i) {
        return burning[i] < 0 ? 0 : 1 - (double) burning[i] / burnTicks;
    }

    /** The most a play-plane unit inside a cloud is veiled (the look). */
    public double veil() {
        return spec.veil();
    }

    void addTo(StateHash hash, Ship ship, Wingman rook) {
        hash.add(count).add(next).add(shipInside ? 1 : 0);
        for (int i = 0; i < count; i++) {
            hash.add(xs[i]).add(ys[i]).add(radii[i]).add(burning[i]);
        }
        ship.slow.addCloudTo(hash);
        if (rook != null) {
            rook.slow.addCloudTo(hash);
        }
    }
}
