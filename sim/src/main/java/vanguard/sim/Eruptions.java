package vanguard.sim;

import java.util.Arrays;

/**
 * M10 part A, step A1b (design/world/jovian, Hazards & set pieces: eruptions; Level 39; {@link
 * HazardSpec.EruptionSpec}): Io's dry eruptions. Each ground vent enters at the top edge at its time and scrolls with the
 * ground; while on the screen it erupts every period, drawn from its range ({@link #rng}) for each cycle, the first one
 * period after its entry: its tell ({@link SimEvents.Type#ERUPTION_TELL}), then its rocks ({@link
 * SimEvents.Type#VENT_ERUPTED}, value the count, drawn from its range). A rock rises over the vent (harmless), flies out on
 * the play plane along its share of the fan, its speed easing down, then falls back (harmless) and is gone. A flying rock
 * is shootable: the player's shots that reach the air spend themselves on it ({@link SimEvents.Type#ROCK_HIT}); at no HP
 * it breaks ({@link SimEvents.Type#ROCK_DESTROYED}), paying nothing and counting as no kill. It hurts the ship and Rook
 * on contact and breaks on the impact ({@link SimEvents.Type#ROCK_STRUCK}, in the sortie's hit passes); enemy bullets
 * pass it, homing shots and Rook never pick it, and it never hurts an enemy. Its own pool (not {@link Debris}: a debris
 * chunk drifts down and blocks both sides' shots). The cycles run on the level clock (they wait while it is held); the
 * rocks fly on the real steps. A restart brings the vents back; a boss checkpoint leaves the vents entered before it on
 * the screen where the scroll put them. Owned by the sortie only in a level with eruptions ({@link
 * Sortie#eruptions()}), so the other levels hash as before. Fixed arrays, allocates nothing.
 */
public final class Eruptions implements Hashed {
    /** The most rocks in flight at once (a rock that finds no room is not thrown). */
    static final int CAPACITY = 48;

    /** What a rock does. */
    public enum Phase {
        /** Rising over its vent, growing: harmless. */
        RISE,
        /** Flying out along its share of the fan: shootable, it hurts on contact. */
        FLY,
        /** Falling back, shrinking: harmless. */
        FALL
    }

    private static final Phase[] PHASES = Phase.values();

    private final HazardSpec.EruptionSpec spec;
    private final SplitMix64 rng;
    private final int ventCount;
    private final int[] ventTicks;
    private final int tellTicks;
    private final int riseTicks;
    private final int flyTicks;
    private final int fallTicks;
    /** Per vent: the ground's scroll when it entered at the top edge; NaN before. */
    private final double[] entryScroll;
    /** Per vent: the level step its next tell starts at; -1 before its entry. */
    private final int[] nextTell;
    /** Per vent: the rocks its current tell will throw (drawn at the tell's start); 0 outside a tell. */
    private final int[] telling;

    private final boolean[] used = new boolean[CAPACITY];
    private final int[] vent = new int[CAPACITY];
    private final int[] phase = new int[CAPACITY];
    private final int[] rockTicks = new int[CAPACITY];
    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] dirX = new double[CAPACITY];
    private final double[] dirY = new double[CAPACITY];
    private final double[] hp = new double[CAPACITY];

    private int tick;
    private double scroll;

    Eruptions(HazardSpec.EruptionSpec spec, SplitMix64 rng) {
        this.spec = spec;
        this.rng = rng;
        ventCount = spec.vents().size();
        ventTicks = new int[ventCount];
        for (int v = 0; v < ventCount; v++) {
            ventTicks[v] = SimStep.ticks(spec.vents().get(v).t());
        }
        tellTicks = Math.max(1, SimStep.ticks(spec.tellSeconds()));
        riseTicks = SimStep.ticks(spec.riseSeconds());
        flyTicks = Math.max(1, SimStep.ticks(spec.flySeconds()));
        fallTicks = SimStep.ticks(spec.fallSeconds());
        entryScroll = new double[ventCount];
        nextTell = new int[ventCount];
        telling = new int[ventCount];
        reset();
    }

    /** Back to the level start: no vent entered, no rock in flight. */
    void reset() {
        Arrays.fill(entryScroll, Double.NaN);
        Arrays.fill(nextTell, -1);
        Arrays.fill(telling, 0);
        Arrays.fill(used, false);
        tick = 0;
        scroll = 0;
    }

    /**
     * Back to a boss checkpoint at {@code levelTick} with the ground scrolled {@code groundScroll}: the vents entered
     * after it enter again, no rock in flight, no tell running (a vent on the screen erupts a period after it).
     */
    void restore(int levelTick, double groundScroll) {
        Arrays.fill(used, false);
        Arrays.fill(telling, 0);
        for (int v = 0; v < ventCount; v++) {
            if (ventTicks[v] > levelTick) {
                entryScroll[v] = Double.NaN;
                nextTell[v] = -1;
            } else if (nextTell[v] >= 0) {
                nextTell[v] = levelTick + period(v);
            }
        }
        tick = levelTick;
        scroll = groundScroll;
    }

    /**
     * One step: the vents enter and scroll with the ground ({@code groundScroll}), their cycles follow the level clock
     * ({@code levelTick}; a tell starts or ends only when it ticked), the rocks fly on.
     */
    void step(int levelTick, double groundScroll, boolean clockTicked, SimEvents events) {
        tick = levelTick;
        scroll = groundScroll;
        for (int v = 0; v < ventCount; v++) {
            if (Double.isNaN(entryScroll[v]) && levelTick >= ventTicks[v]) {
                entryScroll[v] = groundScroll;
                nextTell[v] = ventTicks[v] + period(v);
            }
            if (!clockTicked || Double.isNaN(entryScroll[v]) || !ventOnField(v)) {
                continue;
            }
            if (telling[v] == 0 && levelTick >= nextTell[v]) {
                HazardSpec.Vent spec = this.spec.vents().get(v);
                telling[v] = spec.minRocks() + rng.nextInt(spec.maxRocks() - spec.minRocks() + 1);
                events.add(SimEvents.Type.ERUPTION_TELL, ventX(v), ventY(v), v);
            } else if (telling[v] > 0 && levelTick >= nextTell[v] + tellTicks) {
                throwRocks(v, events);
                nextTell[v] = levelTick + period(v);
            }
        }
        for (int r = 0; r < CAPACITY; r++) {
            if (used[r]) {
                fly(r);
            }
        }
    }

    /** A cycle's period of vent {@code v}, steps, drawn from its range. */
    private int period(int v) {
        HazardSpec.Vent spec = this.spec.vents().get(v);
        double seconds = spec.minPeriod() + (spec.maxPeriod() - spec.minPeriod()) * rng.nextDouble();
        return Math.max(1, SimStep.ticks(seconds));
    }

    /** Vent {@code v}'s tell ends: it throws its rocks along the fan, evenly spread (one rock: straight up). */
    private void throwRocks(int v, SimEvents events) {
        int count = telling[v];
        telling[v] = 0;
        int thrown = 0;
        for (int k = 0; k < count; k++) {
            int r = free();
            if (r < 0) {
                break;
            }
            double angle = count == 1 ? 0 : spec.fanRadians() * ((double) k / (count - 1) - 0.5);
            used[r] = true;
            vent[r] = v;
            phase[r] = riseTicks > 0 ? Phase.RISE.ordinal() : Phase.FLY.ordinal();
            rockTicks[r] = 0;
            xs[r] = ventX(v);
            ys[r] = ventY(v);
            dirX[r] = Trig.sin(angle);
            dirY[r] = Trig.cos(angle);
            hp[r] = spec.hp();
            thrown++;
        }
        events.add(SimEvents.Type.VENT_ERUPTED, ventX(v), ventY(v), thrown);
    }

    private int free() {
        for (int r = 0; r < CAPACITY; r++) {
            if (!used[r]) {
                return r;
            }
        }
        return -1;
    }

    /** One step of rock {@code r}: over its vent while rising, out along its line while flying, still while falling. */
    private void fly(int r) {
        rockTicks[r]++;
        Phase now = PHASES[phase[r]];
        switch (now) {
            case RISE -> {
                xs[r] = ventX(vent[r]);
                ys[r] = ventY(vent[r]);
                if (rockTicks[r] >= riseTicks) {
                    phase[r] = Phase.FLY.ordinal();
                    rockTicks[r] = 0;
                }
            }
            case FLY -> {
                double share = Math.min(1, (double) (rockTicks[r] - 1) / flyTicks);
                double speed = spec.speed() + (spec.endSpeed() - spec.speed()) * share;
                xs[r] += dirX[r] * speed * SimStep.SECONDS;
                ys[r] += dirY[r] * speed * SimStep.SECONDS;
                if (rockTicks[r] >= flyTicks) {
                    phase[r] = Phase.FALL.ordinal();
                    rockTicks[r] = 0;
                    if (fallTicks == 0) {
                        used[r] = false;
                    }
                }
            }
            case FALL -> {
                if (rockTicks[r] >= fallTicks) {
                    used[r] = false;
                }
            }
        }
    }

    /**
     * The player's shots that reach the air spend themselves on a flying rock they touch, dealing their damage ({@link
     * SimEvents.Type#ROCK_HIT}, value the mount); a rock at no HP breaks ({@link SimEvents.Type#ROCK_DESTROYED}).
     */
    void hitBy(Pool<Shot> shots, SimEvents events) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (!weapon.delivery().reaches(Layer.AIR)) {
                continue;
            }
            for (int r = 0; r < CAPACITY; r++) {
                if (!shootable(r) || !spec.rock().overlaps(xs[r], ys[r], weapon.size(), shot.x(), shot.y())) {
                    continue;
                }
                events.add(SimEvents.Type.ROCK_HIT, shot.x(), shot.y(), shot.mount());
                double damage = shot.damage();
                shots.free(i);
                damageRock(r, damage, events);
                break;
            }
        }
    }

    /** Rock {@code r} takes {@code damage}; at no HP it breaks. */
    void damageRock(int r, double damage, SimEvents events) {
        hp[r] -= damage;
        if (hp[r] <= 0) {
            used[r] = false;
            events.add(SimEvents.Type.ROCK_DESTROYED, xs[r], ys[r], vent[r]);
        }
    }

    /** The first flying rock whose box touches {@code hull} at (x, y): it breaks on the impact; -1 for none. */
    int strike(Hull hull, double x, double y, SimEvents events) {
        for (int r = 0; r < CAPACITY; r++) {
            if (shootable(r) && hull.overlaps(x, y, spec.rock(), xs[r], ys[r])) {
                used[r] = false;
                events.add(SimEvents.Type.ROCK_STRUCK, xs[r], ys[r], vent[r]);
                return r;
            }
        }
        return -1;
    }

    /** A rock's contact damage. */
    double damage() {
        return spec.damage();
    }

    /** Its spec. */
    public HazardSpec.EruptionSpec spec() {
        return spec;
    }

    /** The level's vents in all. */
    public int vents() {
        return ventCount;
    }

    /** Whether vent {@code v} is on the screen (it entered at the top edge and has not left the bottom one). */
    public boolean ventPresent(int v) {
        return !Double.isNaN(entryScroll[v]) && ventOnField(v);
    }

    private boolean ventOnField(int v) {
        return ventY(v) > 0;
    }

    /** Vent {@code v}'s x, px from the left. */
    public double ventX(int v) {
        return spec.vents().get(v).x();
    }

    /** Vent {@code v}'s y, px up: the top edge when it enters, then with the ground; above the top edge before. */
    public double ventY(int v) {
        double entered = Double.isNaN(entryScroll[v]) ? 0 : scroll - entryScroll[v];
        return PlayField.HEIGHT - entered;
    }

    /** Whether vent {@code v} tells now (its mouth glowing before it throws). */
    public boolean telling(int v) {
        return telling[v] > 0;
    }

    /** Vent {@code v}'s tell's progress, 0 to 1; 0 outside a tell. */
    public double tellProgress(int v) {
        return telling[v] > 0 ? Math.clamp((double) (tick - nextTell[v]) / tellTicks, 0, 1) : 0;
    }

    /** The rock slots (some free: {@link #rockUsed}). */
    public int rocks() {
        return CAPACITY;
    }

    /** Whether rock slot {@code r} holds a rock now. */
    public boolean rockUsed(int r) {
        return used[r];
    }

    /** Whether rock {@code r} flies now: shootable, and it hurts on contact. */
    public boolean shootable(int r) {
        return used[r] && phase[r] == Phase.FLY.ordinal();
    }

    /** Rock {@code r}'s phase (meaningful while {@link #rockUsed}). */
    public Phase rockPhase(int r) {
        return PHASES[phase[r]];
    }

    /** How far rock {@code r}'s phase has run, 0 to 1 (its growth while rising, its shrink while falling). */
    public double rockProgress(int r) {
        int span =
                switch (PHASES[phase[r]]) {
                    case RISE -> riseTicks;
                    case FLY -> flyTicks;
                    case FALL -> fallTicks;
                };
        return span == 0 ? 1 : Math.min(1, (double) rockTicks[r] / span);
    }

    public double rockX(int r) {
        return xs[r];
    }

    public double rockY(int r) {
        return ys[r];
    }

    @Override
    public void addTo(StateHash hash) {
        for (int v = 0; v < ventCount; v++) {
            hash.add(Double.isNaN(entryScroll[v]) ? -1 : entryScroll[v])
                    .add(nextTell[v])
                    .add(telling[v]);
        }
        for (int r = 0; r < CAPACITY; r++) {
            if (used[r]) {
                hash.add(r)
                        .add(phase[r])
                        .add(rockTicks[r])
                        .add(xs[r])
                        .add(ys[r])
                        .add(hp[r]);
            }
        }
    }
}
