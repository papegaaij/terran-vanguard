package vanguard.sim;

import java.util.List;

/**
 * M7 part C, the level's tendril gates (design/enemies/ground/tendril-anchor, the {@code link} attack; M9 =
 * a): per {@link GroundSpec.Gate} pair that has entered, its {@link Tendril} between the centres of its two
 * anchors (ground units the {@link EnemyForce} lets in and moves). A tendril is dormant for its link's
 * dormancy, then its touch hurts the ship and Rook (each with its own cooldown, shared by every tendril; the
 * ship is not pushed). Either anchor's death breaks it for good; a beam crossing it damages it on its way past
 * ({@link #burn}) and breaks it at 0 HP (burnt); one whose anchor leaves the screen standing is lost. A broken
 * tendril withers for {@link #WITHER_SECONDS} and is gone. Pooled, with fixed arrays: nothing allocates per
 * step. Hashed only in a level with gates.
 */
public final class Links {
    /** A dormant tendril flares to its full width over this long (presentation; its touch hurts from the start). */
    public static final double FLARE_SECONDS = 0.15;

    static final int FLARE_TICKS = Math.max(1, SimStep.ticks(FLARE_SECONDS));
    /** A broken tendril withers this long (design/enemies/ground/tendril-anchor, Death), then it is gone. */
    public static final double WITHER_SECONDS = 0.4;

    static final int WITHER_TICKS = SimStep.ticks(WITHER_SECONDS);
    /** Tendrils at once: a level shows at most a few gates of up to three pairs. */
    static final int CAPACITY = 16;
    /** {@link SimEvents.Type#TENDRIL_TOUCH}'s value for the ship and for Rook. */
    public static final int TOUCH_SHIP = 0;

    public static final int TOUCH_ROOK = 2;

    /** What a tendril's end means for the level (the sortie's objectives and bounty). */
    interface Outcome {
        /** {@code tendril} was broken now: by an anchor's death, or burnt through ({@link Tendril#burnt()}). */
        void broken(Tendril tendril);

        /** {@code tendril} left the screen standing. */
        void lost(Tendril tendril);
    }

    private final GroundSpec.Gate[] gates;
    private final int[] dormantTicks;
    private final SimEvents events;
    private final Outcome outcome;
    private final Pool<Tendril> tendrils = new Pool<>(CAPACITY, Tendril::new, Tendril[]::new);
    private int shipCooldown;
    private int rookCooldown;

    Links(List<GroundSpec.Gate> levelGates, SimEvents events, Outcome outcome) {
        gates = levelGates.toArray(GroundSpec.Gate[]::new);
        dormantTicks = new int[gates.length];
        for (int g = 0; g < gates.length; g++) {
            dormantTicks[g] =
                    SimStep.ticks(gates[g].anchor().link().orElseThrow().dormantSeconds());
        }
        this.events = events;
        this.outcome = outcome;
    }

    /** Back to the level start: no tendrils, no cooldowns. */
    void reset() {
        Pools.clear(tendrils);
        shipCooldown = 0;
        rookCooldown = 0;
    }

    /** The number of gates (pairs) of the level. */
    int gates() {
        return gates.length;
    }

    /**
     * Gate {@code gate}'s left (or {@code right}) anchor entered as {@code anchor}: its tendril starts with the
     * first of the two (none when the pool is full).
     */
    void anchored(int gate, boolean right, Enemy anchor) {
        Tendril tendril = find(gate);
        if (tendril == null) {
            tendril = tendrils.obtain();
            if (tendril == null) {
                return;
            }
            tendril.start(gate, gates[gate].anchor().link().orElseThrow(), dormantTicks[gate]);
            tendril.ax = tendril.bx = anchor.x();
            tendril.ay = tendril.by = anchor.y();
        }
        if (right) {
            tendril.b = anchor;
            tendril.bSerial = anchor.serial();
            tendril.bAlive = true;
            tendril.bx = anchor.x();
            tendril.by = anchor.y();
        } else {
            tendril.a = anchor;
            tendril.aSerial = anchor.serial();
            tendril.aAlive = true;
            tendril.ax = anchor.x();
            tendril.ay = anchor.y();
        }
    }

    private Tendril find(int gate) {
        for (int i = 0; i < tendrils.size(); i++) {
            if (tendrils.get(i).serial == gate) {
                return tendrils.get(i);
            }
        }
        return null;
    }

    /**
     * {@code anchor} is gone, {@code destroyed} or (not) away off the bottom edge alive (before it is freed):
     * its end stays where it was; a standing tendril breaks at its death and is lost at its escape.
     */
    void anchorGone(Enemy anchor, boolean destroyed) {
        for (int i = tendrils.size() - 1; i >= 0; i--) {
            Tendril tendril = tendrils.get(i);
            boolean left = tendril.aAlive && tendril.a == anchor && tendril.aSerial == anchor.serial();
            boolean right = tendril.bAlive && tendril.b == anchor && tendril.bSerial == anchor.serial();
            if (!left && !right) {
                continue;
            }
            if (left) {
                tendril.ax = anchor.x();
                tendril.ay = anchor.y();
                tendril.aAlive = false;
                tendril.a = null;
            } else {
                tendril.bx = anchor.x();
                tendril.by = anchor.y();
                tendril.bAlive = false;
                tendril.b = null;
            }
            if (tendril.broken) {
                return;
            }
            if (destroyed) {
                breakOff(tendril, false);
            } else {
                outcome.lost(tendril);
                tendrils.free(i);
            }
            return;
        }
    }

    private void breakOff(Tendril tendril, boolean burnt) {
        tendril.broken = true;
        tendril.burnt = burnt;
        tendril.brokenTicks = 0;
        events.add(
                SimEvents.Type.TENDRIL_BROKEN,
                (tendril.ax + tendril.bx) / 2,
                (tendril.ay + tendril.by) / 2,
                burnt ? 1 : 0);
        outcome.broken(tendril);
    }

    /**
     * A step, after the enemies moved ({@code scrollStep} px of ground scroll): the cooldowns run, each
     * tendril's ends follow its anchors (a dead anchor's end scrolls with the ground), a dormancy that ends
     * raises {@link SimEvents.Type#TENDRIL_FLARE}, and a broken tendril that has withered is gone.
     */
    void step(double scrollStep) {
        if (shipCooldown > 0) {
            shipCooldown--;
        }
        if (rookCooldown > 0) {
            rookCooldown--;
        }
        for (int i = tendrils.size() - 1; i >= 0; i--) {
            Tendril tendril = tendrils.get(i);
            tendril.wasBurning = tendril.burning;
            tendril.burning = false;
            if (tendril.aAlive) {
                tendril.ax = tendril.a.x();
                tendril.ay = tendril.a.y();
            } else {
                tendril.ay -= scrollStep;
            }
            if (tendril.bAlive) {
                tendril.bx = tendril.b.x();
                tendril.by = tendril.b.y();
            } else {
                tendril.by -= scrollStep;
            }
            tendril.ticks++;
            if (tendril.broken) {
                if (++tendril.brokenTicks >= WITHER_TICKS) {
                    tendrils.free(i);
                }
            } else if (tendril.ticks == tendril.dormantTicks && tendril.standing()) {
                events.add(
                        SimEvents.Type.TENDRIL_FLARE,
                        (tendril.ax + tendril.bx) / 2,
                        (tendril.ay + tendril.by) / 2,
                        tendril.serial);
            }
        }
    }

    /**
     * A beam's column, {@code half} px either side of {@code x} from {@code fromY} up to where it ends ({@code
     * toY}: its first target, the rock or its reach), crosses the standing tendrils in it: each takes {@code
     * damage} (burning, {@link SimEvents.Type#TENDRIL_BURN} as a burn starts) and breaks at 0 HP, burnt. The
     * beam goes on past them (M9 = a), dormant or flared.
     */
    void burn(double x, double half, double fromY, double toY, double damage) {
        double xl = x - half;
        double xr = x + half;
        for (int i = 0; i < tendrils.size(); i++) {
            Tendril tendril = tendrils.get(i);
            if (!tendril.standing()) {
                continue;
            }
            double minX = Math.min(tendril.ax, tendril.bx);
            double maxX = Math.max(tendril.ax, tendril.bx);
            if (maxX <= xl || minX >= xr) {
                continue;
            }
            double y1 = tendril.yAt(Math.max(xl, minX));
            double y2 = tendril.yAt(Math.min(xr, maxX));
            double margin = tendril.spec.band() / 2;
            if (Math.max(y1, y2) + margin < fromY || Math.min(y1, y2) - margin > toY) {
                continue;
            }
            if (!tendril.burning && !tendril.wasBurning) {
                events.add(SimEvents.Type.TENDRIL_BURN, x, tendril.yAt(x), tendril.serial);
            }
            tendril.burning = true;
            tendril.hp -= damage;
            if (tendril.hp <= 0) {
                breakOff(tendril, true);
            }
        }
    }

    /**
     * The touch of the flared tendrils on a hull at (x, y), the ship's or ({@code rook}) Rook's: the touch's
     * damage when one touches it outside that craft's cooldown (which starts, with {@link
     * SimEvents.Type#TENDRIL_TOUCH}); 0 otherwise.
     */
    double touch(Hull hull, double x, double y, boolean rook) {
        if ((rook ? rookCooldown : shipCooldown) > 0) {
            return 0;
        }
        for (int i = 0; i < tendrils.size(); i++) {
            Tendril tendril = tendrils.get(i);
            if (!tendril.live()
                    || !hull.touchesSegment(
                            x, y, tendril.ax, tendril.ay, tendril.bx, tendril.by, tendril.spec.band() / 2)) {
                continue;
            }
            int cooldown = SimStep.ticks(tendril.spec.cooldownSeconds());
            if (rook) {
                rookCooldown = cooldown;
            } else {
                shipCooldown = cooldown;
            }
            events.add(SimEvents.Type.TENDRIL_TOUCH, x, y, rook ? TOUCH_ROOK : TOUCH_SHIP);
            return tendril.spec.contactDamage();
        }
        return 0;
    }

    /**
     * For Rook (design/enemies/ground/tendril-anchor, Rook): the lowest height at which a standing tendril
     * (dormant or flared) crosses the column {@code halfWidth} px either side of {@code x} above {@code y}; +∞
     * for none.
     */
    double ceiling(double x, double halfWidth, double y) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < tendrils.size(); i++) {
            Tendril tendril = tendrils.get(i);
            if (!tendril.standing()) {
                continue;
            }
            double minX = Math.min(tendril.ax, tendril.bx);
            double maxX = Math.max(tendril.ax, tendril.bx);
            if (maxX <= x - halfWidth || minX >= x + halfWidth) {
                continue;
            }
            double low =
                    Math.min(tendril.yAt(Math.max(x - halfWidth, minX)), tendril.yAt(Math.min(x + halfWidth, maxX)));
            if (low > y && low < best) {
                best = low;
            }
        }
        return best;
    }

    /**
     * For Rook: the serial of the anchor nearer to (x, y) of the lowest standing tendril above him whose both
     * anchors are on the play field; -1 for none.
     */
    int nearerAnchor(double x, double y) {
        int best = -1;
        double lowest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < tendrils.size(); i++) {
            Tendril tendril = tendrils.get(i);
            if (!tendril.standing()
                    || !ShotGeometry.onField(tendril.a)
                    || !ShotGeometry.onField(tendril.b)
                    || Math.min(tendril.ay, tendril.by) <= y
                    || Math.min(tendril.ay, tendril.by) >= lowest) {
                continue;
            }
            lowest = Math.min(tendril.ay, tendril.by);
            double da = (tendril.ax - x) * (tendril.ax - x) + (tendril.ay - y) * (tendril.ay - y);
            double db = (tendril.bx - x) * (tendril.bx - x) + (tendril.by - y) * (tendril.by - y);
            best = da <= db ? tendril.aSerial : tendril.bSerial;
        }
        return best;
    }

    /** The tendrils showing now (standing, or broken and withering). */
    public int size() {
        return tendrils.size();
    }

    /** Tendril {@code i} of {@link #size()}. */
    public Tendril get(int i) {
        return tendrils.get(i);
    }

    /** Whether a standing tendril's touch cooldown runs for the ship now (the hit flash may follow it). */
    public boolean shipCooling() {
        return shipCooldown > 0;
    }

    void addTo(StateHash hash) {
        hash.add(shipCooldown).add(rookCooldown).add(tendrils.size());
        for (int i = 0; i < tendrils.size(); i++) {
            tendrils.get(i).addTo(hash);
        }
    }
}
