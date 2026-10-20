package vanguard.sim;

/**
 * M7 part C, a gate's tendril (design/enemies/ground/tendril-anchor; M9 = a): the line between the centres
 * of its two anchors, kept by {@link Links}. It is dormant for its link's dormancy after the pair entered,
 * then stands (it flares to its full width and its touch hurts) until an anchor dies or a beam burns it
 * through; a broken tendril withers for {@link Links#WITHER_SECONDS} (presentation) and is gone. Positions
 * are play-field px, y up; a dead anchor's end stays where it died, scrolling with the ground.
 */
public final class Tendril {
    EnemySpec.Link spec;
    int serial;
    /** The anchors (null until entered or once gone) and their serials. */
    Enemy a;

    Enemy b;
    int aSerial;
    int bSerial;
    boolean aAlive;
    boolean bAlive;
    double ax;
    double ay;
    double bx;
    double by;
    double hp;
    /** Steps since the first anchor entered (0 in that step). */
    int ticks;

    int dormantTicks;
    boolean broken;
    boolean burnt;
    /** Steps since it broke. */
    int brokenTicks;

    boolean burning;
    boolean wasBurning;

    void start(int gate, EnemySpec.Link link, int dormant) {
        spec = link;
        serial = gate;
        a = null;
        b = null;
        aSerial = -1;
        bSerial = -1;
        aAlive = false;
        bAlive = false;
        ax = ay = bx = by = 0;
        hp = link.tendrilHp();
        // Its first step (the one it entered in) counts as step 0.
        ticks = -1;
        dormantTicks = dormant;
        broken = false;
        burnt = false;
        brokenTicks = 0;
        burning = false;
        wasBurning = false;
    }

    /** Whether both anchors are in and it is not broken: it stands (dormant or flared). */
    public boolean standing() {
        return !broken && aAlive && bAlive;
    }

    /** Whether it stands and its touch hurts (its dormancy is over). */
    boolean live() {
        return standing() && ticks >= dormantTicks;
    }

    /** The left anchor's centre (the tendril's left end), px. */
    public double ax() {
        return ax;
    }

    public double ay() {
        return ay;
    }

    /** The right anchor's centre (the tendril's right end), px. */
    public double bx() {
        return bx;
    }

    public double by() {
        return by;
    }

    /** Whether it is still in its dormancy: a thin flickering thread that hurts nothing. */
    public boolean dormant() {
        return !broken && ticks < dormantTicks;
    }

    /**
     * How far it has flared to its full width, 0 (dormant) to 1, over {@link Links#FLARE_SECONDS} after its
     * dormancy; 1 once broken (it withers from full width).
     */
    public double flare() {
        if (broken) {
            return 1;
        }
        int since = ticks - dormantTicks;
        return since < 0 ? 0 : Math.min(1, (double) since / Links.FLARE_TICKS);
    }

    /** Its HP's share left, 1 to 0 (a beam burning it). */
    public double hpShare() {
        return Math.max(0, hp / spec.tendrilHp());
    }

    /** Whether a beam burns it in this step. */
    public boolean burning() {
        return burning;
    }

    /** Whether it is broken (by an anchor's death or burnt through); it withers meanwhile. */
    public boolean broken() {
        return broken;
    }

    /** Whether a beam burnt it through (it parts in the middle), rather than an anchor's death. */
    public boolean burnt() {
        return burnt;
    }

    /** How far a broken one has withered, 0 to 1 over {@link Links#WITHER_SECONDS}; 0 while it stands. */
    public double wither() {
        return broken ? Math.min(1, (double) brokenTicks / Links.WITHER_TICKS) : 0;
    }

    /** Whether its left anchor is alive (the end a broken one snaps back into, with {@link #bAlive}). */
    public boolean aAlive() {
        return aAlive;
    }

    public boolean bAlive() {
        return bAlive;
    }

    /** Its gate pair's index in the level's {@link GroundSpec#gates()}: unique in an attempt. */
    public int serial() {
        return serial;
    }

    /** Its band's width, px: a hull this close to the line, either side by half, touches it. */
    public double band() {
        return spec.band();
    }

    /** The line's height at {@code x}, clamped to its ends. */
    double yAt(double x) {
        if (bx == ax) {
            return (ay + by) / 2;
        }
        double t = Math.clamp((x - ax) / (bx - ax), 0, 1);
        return ay + (by - ay) * t;
    }

    void addTo(StateHash hash) {
        hash.add(serial)
                .add(aSerial)
                .add(bSerial)
                .add(aAlive ? 1 : 0)
                .add(bAlive ? 1 : 0)
                .add(ax)
                .add(ay)
                .add(bx)
                .add(by)
                .add(hp)
                .add(ticks)
                .add(broken ? 1 : 0)
                .add(burnt ? 1 : 0)
                .add(brokenTicks)
                .add(wasBurning ? 1 : 0);
    }
}
