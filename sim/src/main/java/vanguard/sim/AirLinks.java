package vanguard.sim;

/**
 * M9 part D, the air links (design/enemies, Air links; design/enemies/space/shard-drone; {@link EnemySpec.AirLink}): a
 * wave of units with an air link is one linked group, each unit joined to the next in its wave's order by an energy
 * link ({@link #closed} for a {@code circle} wave: the last to the first too). A group's links start their tell when its
 * last unit is on the play field and are lit after it; the EMP Burst's stun of either end drops a link, which relights
 * after the tell again. Lit, a link touches the ship's hull (its own cooldown, shared by every link; Rook and the enemies
 * never) and, of a kind the hull escort's hook names, the hull escort's sections (a cooldown per link and section, the
 * hull's damage). Every shot passes through a link (nothing here is hit); killing either end breaks it for good, an end
 * leaving the screen ends it. Fixed arrays: nothing allocates per step. Hashed only in a level with such a unit.
 */
public final class AirLinks {
    /** Groups at once (the design shows at most two on the screen). */
    static final int GROUPS = 6;
    /** Units in a group (3–4 by design). */
    public static final int MEMBERS = 4;
    /** Link slots: group g's link i (joining its units i and i + 1, the last the first in a ring) is slot g × 4 + i. */
    public static final int SLOTS = GROUPS * MEMBERS;
    /** A {@link SimEvents.Type#AIR_LINK_TOUCH} value: the ship. */
    public static final int TOUCH_SHIP = 0;
    /** A {@link SimEvents.Type#AIR_LINK_TOUCH} value: this + the hull escort's section. */
    public static final int TOUCH_SECTION = 1;

    private final SimEvents events;
    /** Per group, the wave it is ({@link Enemy#wave()}); -1 for a free group. */
    private final int[] waves = new int[GROUPS];

    private final int[] sizes = new int[GROUPS];
    private final boolean[] closed = new boolean[GROUPS];
    private final boolean[] allIn = new boolean[GROUPS];
    private final int[] kinds = new int[GROUPS];
    private final EnemySpec.AirLink[] specs = new EnemySpec.AirLink[GROUPS];
    /** Per member slot (group × {@link #MEMBERS} + place): the unit, null until it joined or once gone. */
    private final Enemy[] members = new Enemy[SLOTS];

    private final int[] serials = new int[SLOTS];
    /** Per member slot: whether it joined (its wave let it in); a gone member stays joined. */
    private final boolean[] joined = new boolean[SLOTS];
    /** Per member slot: whether it was on the play field (its group's tell waits for every one). */
    private final boolean[] entered = new boolean[SLOTS];

    private final double[] memberX = new double[SLOTS];
    private final double[] memberY = new double[SLOTS];
    /** Per link slot: steps left of its tell (lit at 0). */
    private final int[] waits = new int[SLOTS];
    /** Per link slot: broken for good (an end destroyed or gone). */
    private final boolean[] broken = new boolean[SLOTS];
    /** Per link slot: lit in the last step (its {@link SimEvents.Type#AIR_LINK_LIT} once). */
    private final boolean[] lit = new boolean[SLOTS];

    private int shipCooldown;

    AirLinks(SimEvents events) {
        this.events = events;
        reset();
    }

    /** Back to the level start: no groups, no cooldown. */
    void reset() {
        java.util.Arrays.fill(waves, -1);
        java.util.Arrays.fill(members, null);
        java.util.Arrays.fill(joined, false);
        java.util.Arrays.fill(entered, false);
        java.util.Arrays.fill(broken, false);
        java.util.Arrays.fill(lit, false);
        java.util.Arrays.fill(waits, 0);
        java.util.Arrays.fill(specs, null);
        shipCooldown = 0;
    }

    /**
     * {@code unit} of an air-linked kind was let in at {@code place} of its wave {@code wave} of {@code waveSize} units
     * ({@code ring}: a closed ring, a {@code circle} wave): it joins its wave's group (a new one with its first unit;
     * none when every group is taken or its place is past {@link #MEMBERS}).
     */
    void join(Enemy unit, int wave, int place, int waveSize, boolean ring) {
        if (place < 0 || place >= MEMBERS) {
            return;
        }
        int g = group(wave);
        if (g < 0) {
            g = free();
            if (g < 0) {
                return;
            }
            waves[g] = wave;
            sizes[g] = Math.min(waveSize, MEMBERS);
            closed[g] = ring && sizes[g] >= 3;
            allIn[g] = false;
            kinds[g] = unit.kind();
            specs[g] = unit.spec().airLink().orElseThrow();
            for (int m = 0; m < MEMBERS; m++) {
                int slot = g * MEMBERS + m;
                members[slot] = null;
                joined[slot] = false;
                entered[slot] = false;
                broken[slot] = false;
                lit[slot] = false;
                waits[slot] = SimStep.ticks(specs[g].tellSeconds());
            }
        }
        int slot = g * MEMBERS + place;
        members[slot] = unit;
        serials[slot] = unit.serial();
        joined[slot] = true;
        memberX[slot] = unit.x();
        memberY[slot] = unit.y();
    }

    private int group(int wave) {
        for (int g = 0; g < GROUPS; g++) {
            if (waves[g] == wave) {
                return g;
            }
        }
        return -1;
    }

    private int free() {
        for (int g = 0; g < GROUPS; g++) {
            if (waves[g] < 0) {
                return g;
            }
        }
        return -1;
    }

    /** The links of group {@code g}: its units less one, a ring as many as its units. */
    private int links(int g) {
        return sizes[g] < 2 ? 0 : closed[g] ? sizes[g] : sizes[g] - 1;
    }

    /**
     * {@code unit} is gone, {@code destroyed} or (not) away off the play field alive (before it is freed): its links
     * break ({@link SimEvents.Type#AIR_LINK_BROKEN}, a destroyed end's with its snap).
     */
    void gone(Enemy unit, boolean destroyed) {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (members[slot] != unit || serials[slot] != unit.serial()) {
                continue;
            }
            int g = slot / MEMBERS;
            int m = slot % MEMBERS;
            memberX[slot] = unit.x();
            memberY[slot] = unit.y();
            members[slot] = null;
            int n = sizes[g];
            // The link from it to the next and the one from the one before to it.
            breakLink(g, m, destroyed);
            breakLink(g, (m + n - 1) % n, destroyed);
            return;
        }
    }

    private void breakLink(int g, int i, boolean destroyed) {
        if (i < 0 || i >= links(g)) {
            return;
        }
        int slot = g * MEMBERS + i;
        if (broken[slot]) {
            return;
        }
        boolean shown = bothJoined(g, i);
        broken[slot] = true;
        lit[slot] = false;
        if (shown) {
            events.add(
                    SimEvents.Type.AIR_LINK_BROKEN,
                    (ax(slot) + bx(slot)) / 2,
                    (ay(slot) + by(slot)) / 2,
                    destroyed ? 0 : 1);
        }
    }

    private boolean bothJoined(int g, int i) {
        int a = g * MEMBERS + i;
        int b = g * MEMBERS + (i + 1) % sizes[g];
        return joined[a] && joined[b];
    }

    /**
     * A step, after the enemies moved: the ship's cooldown runs, each link's ends follow its units, a group whose last
     * unit came onto the play field starts its tell, a link with a stunned end drops (and waits its tell again), a tell
     * that ends lights its link; a group whose links are all broken is free again.
     */
    void step(HullEscort hull) {
        if (shipCooldown > 0) {
            shipCooldown--;
        }
        for (int g = 0; g < GROUPS; g++) {
            if (waves[g] < 0) {
                continue;
            }
            boolean every = true;
            for (int m = 0; m < sizes[g]; m++) {
                int slot = g * MEMBERS + m;
                Enemy unit = members[slot];
                if (unit != null && unit.serial() == serials[slot]) {
                    memberX[slot] = unit.x();
                    memberY[slot] = unit.y();
                    entered[slot] |= PlayField.overlaps(unit.x(), unit.y(), unit.hitbox());
                }
                every &= joined[slot] && (entered[slot] || members[slot] == null);
            }
            allIn[g] |= every;
            int n = links(g);
            boolean standing = false;
            for (int i = 0; i < n; i++) {
                int slot = g * MEMBERS + i;
                if (broken[slot]) {
                    continue;
                }
                standing = true;
                if (!allIn[g]) {
                    continue;
                }
                Enemy a = members[slot];
                Enemy b = members[g * MEMBERS + (i + 1) % sizes[g]];
                boolean stunned = (a != null && a.empStunned()) || (b != null && b.empStunned());
                if (stunned) {
                    if (lit[slot]) {
                        events.add(
                                SimEvents.Type.AIR_LINK_DROPPED,
                                (ax(slot) + bx(slot)) / 2,
                                (ay(slot) + by(slot)) / 2,
                                slot);
                    }
                    lit[slot] = false;
                    waits[slot] = SimStep.ticks(specs[g].tellSeconds());
                    continue;
                }
                if (waits[slot] > 0) {
                    waits[slot]--;
                }
                if (waits[slot] == 0 && !lit[slot]) {
                    lit[slot] = true;
                    events.add(SimEvents.Type.AIR_LINK_LIT, (ax(slot) + bx(slot)) / 2, (ay(slot) + by(slot)) / 2, slot);
                }
            }
            boolean anyMember = false;
            for (int m = 0; m < sizes[g]; m++) {
                anyMember |= members[g * MEMBERS + m] != null || !joined[g * MEMBERS + m];
            }
            if (!standing || !anyMember) {
                free(g, hull);
            }
        }
    }

    private void free(int g, HullEscort hull) {
        waves[g] = -1;
        specs[g] = null;
        for (int m = 0; m < MEMBERS; m++) {
            int slot = g * MEMBERS + m;
            members[slot] = null;
            joined[slot] = false;
            broken[slot] = false;
            lit[slot] = false;
            if (hull != null) {
                hull.linkFreed(slot);
            }
        }
    }

    /**
     * The touch of the lit links on the ship's {@code shipHull} at (x, y): the link's damage when one touches it outside
     * the ship's cooldown (which starts, with {@link SimEvents.Type#AIR_LINK_TOUCH}); 0 otherwise.
     */
    double touch(Hull shipHull, double x, double y) {
        if (shipCooldown > 0) {
            return 0;
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!lit[slot]) {
                continue;
            }
            EnemySpec.AirLink spec = specs[slot / MEMBERS];
            if (!shipHull.touchesSegment(x, y, ax(slot), ay(slot), bx(slot), by(slot), spec.band() / 2)) {
                continue;
            }
            shipCooldown = Math.max(1, SimStep.ticks(spec.cooldownSeconds()));
            events.add(SimEvents.Type.AIR_LINK_TOUCH, x, y, TOUCH_SHIP);
            return spec.damage();
        }
        return 0;
    }

    /**
     * The lit links of a kind {@code hook} names touch the hull escort's sections (design/allies/mining-tug): a section a
     * link crosses takes the hull's link damage outside that link's and section's cooldown ({@link HullEscort#hit},
     * applied after the moves).
     */
    void touchHull(HullEscort hull, ObjectiveHook hook) {
        int sections = hull.spec().sections().size();
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!lit[slot]
                    || hook.mode(kinds[slot / MEMBERS]) == HookMode.NONE
                    || !hull.hooked(waves[slot / MEMBERS])) {
                continue;
            }
            double half = specs[slot / MEMBERS].band() / 2;
            for (int k = 0; k < sections; k++) {
                if (hull.crosses(k, ax(slot), ay(slot), bx(slot), by(slot), half) && hull.linkTouch(slot, k)) {
                    hull.hit(k, hull.spec().link());
                    events.add(SimEvents.Type.AIR_LINK_TOUCH, hull.x(), hull.y(), TOUCH_SECTION + k);
                }
            }
        }
    }

    /** Whether link slot {@code slot} shows now: both ends joined, not broken (in its tell or lit). */
    public boolean shown(int slot) {
        int g = slot / MEMBERS;
        int i = slot % MEMBERS;
        return waves[g] >= 0 && i < links(g) && !broken[slot] && bothJoined(g, i);
    }

    /** Whether link slot {@code slot} is lit now (it hurts). */
    public boolean lit(int slot) {
        return lit[slot];
    }

    /** Link slot {@code slot}'s tell left, 0–1 (1 while its group is not in yet; 0 when lit). */
    public double tell(int slot) {
        int g = slot / MEMBERS;
        if (waves[g] < 0 || lit[slot]) {
            return 0;
        }
        int total = Math.max(1, SimStep.ticks(specs[g].tellSeconds()));
        return allIn[g] ? Math.min(1, (double) waits[slot] / total) : 1;
    }

    /** Link slot {@code slot}'s first end (its unit i), px, y up. */
    public double ax(int slot) {
        return memberX[slot];
    }

    public double ay(int slot) {
        return memberY[slot];
    }

    /** Link slot {@code slot}'s second end (its unit i + 1, the first for a ring's last link). */
    public double bx(int slot) {
        return memberX[other(slot)];
    }

    public double by(int slot) {
        return memberY[other(slot)];
    }

    private int other(int slot) {
        int g = slot / MEMBERS;
        int n = Math.max(1, sizes[g]);
        return g * MEMBERS + (slot % MEMBERS + 1) % n;
    }

    /** Link slot {@code slot}'s band, px (its spec's; 0 for a free slot). */
    public double band(int slot) {
        EnemySpec.AirLink spec = specs[slot / MEMBERS];
        return spec == null ? 0 : spec.band();
    }

    /** Whether the ship's touch cooldown runs now (the hit flash may follow it). */
    public boolean shipCooling() {
        return shipCooldown > 0;
    }

    void addTo(StateHash hash) {
        hash.add(shipCooldown);
        for (int g = 0; g < GROUPS; g++) {
            hash.add(waves[g]);
            if (waves[g] < 0) {
                continue;
            }
            hash.add(sizes[g]).add(closed[g] ? 1 : 0).add(allIn[g] ? 1 : 0);
            for (int m = 0; m < MEMBERS; m++) {
                int slot = g * MEMBERS + m;
                hash.add(joined[slot] ? 1 : 0)
                        .add(entered[slot] ? 1 : 0)
                        .add(members[slot] == null ? -1 : serials[slot])
                        .add(waits[slot])
                        .add(broken[slot] ? 1 : 0)
                        .add(lit[slot] ? 1 : 0)
                        .add(memberX[slot])
                        .add(memberY[slot]);
            }
        }
    }
}
