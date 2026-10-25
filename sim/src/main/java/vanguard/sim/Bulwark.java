package vanguard.sim;

import java.util.List;

/**
 * M8 part A, the Bulwark heavy drone in the escort slot (design/player/wingmen, Heavy drone "Bulwark"; m8a-gaps.md
 * §1.7, D10, D11): it holds its wing slot beside the ship (no formation changes, no dodging), its acceleration taking
 * the water's drag as Rook's does; its cannon fires straight ahead while the player fires (its shots are the player's
 * side's, with the escort's mount: kills pay as usual); the enemies' bullets, mines, homing missiles and air contacts
 * hit its armour (no shield, no mercy time); at zero it is destroyed, out for the attempt.
 *
 * <p>Draws fire: while it is within its draw radius of the ship, each aimed volley an enemy fires at the ship goes for
 * it instead with its draw share, drawn from its <em>own</em> generator (seeded from the sortie's seed), so the
 * sortie's draws, and so every replay without a Bulwark, stay as they were (D11: aimed bullets only, not missiles,
 * sweeps or patterns). A turret firing along its barrel, a fan along a walker's facing and the bullets aimed at a
 * convoy are not drawn. Hashed only when it flies.
 */
public final class Bulwark {
    /** Its generator's salt over the sortie's seed (its own stream). */
    private static final long DRAW_SALT = 0x42554C5741524BL;
    /** Standing still to full speed, s (default pending the user's review: the data names none; Rook's is 0.2 s). */
    static final double ACCELERATION_SECONDS = 0.4;
    /** It slows into its slot over this, s (it arrives without overshooting). */
    private static final double ARRIVE_SECONDS = 0.25;
    /** The gap its craft keeps to every play field edge, px. */
    private static final double EDGE_GAP = 4;
    /** The air enemies it can tell apart touching it at once. */
    private static final int CONTACTS = 4;

    private final BulwarkSpec spec;
    private final SplitMix64 draws;
    private final Hull hull;
    private final double accelerationStep;
    private final double limit;
    private final double drawRadiusSquared;
    /** The level's medium (the drag); null in the air. */
    private Medium medium;
    /** M8 part G: where its goal moves to keep out of the plumes and the torpedoes' lines. */
    private final BulwarkHazards hazards;

    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double vx;
    private double vy;
    private double armour;
    private boolean destroyed;
    private int cooldown;
    private int sinceShot;
    /** The aimed volleys it drew in this attempt (the game's lure halo pulses on each). */
    private int drawn;

    private final int[] contacts = new int[CONTACTS];
    private final int[] touching = new int[CONTACTS];
    private int contactCount;
    private int touchingCount;

    /** @param seed the sortie's seed, from which its own generator is seeded */
    Bulwark(BulwarkSpec spec, long seed) {
        this.spec = spec;
        draws = new SplitMix64(seed ^ DRAW_SALT);
        hull = new Hull(List.of(new Hull.Part(0, 0, spec.hitbox())));
        accelerationStep = spec.speed() / ACCELERATION_SECONDS * SimStep.SECONDS;
        limit = spec.size() / 2 + EDGE_GAP;
        hazards = new BulwarkHazards(spec.hitbox(), limit);
        drawRadiusSquared = spec.drawRadius() * spec.drawRadius();
        reset(spec.armour(), Ship.START_X, Ship.START_Y);
    }

    /** The level's medium: under water its acceleration takes the drag. */
    void medium(Medium water) {
        medium = water;
    }

    /** The start of an attempt: {@code startArmour} points, in its slot beside the ship at (shipX, shipY). */
    void reset(double startArmour, double shipX, double shipY) {
        armour = startArmour;
        destroyed = false;
        cooldown = 0;
        sinceShot = Integer.MAX_VALUE;
        drawn = 0;
        contactCount = 0;
        touchingCount = 0;
        formUp(shipX, shipY);
        prevX = x;
        prevY = y;
    }

    /** Back to a boss checkpoint: its armour then, destroyed or not, its generator as it was, in its slot. */
    void restore(double checkpointArmour, boolean wasDestroyed, long checkpointDraws, double shipX, double shipY) {
        reset(wasDestroyed ? spec.armour() : checkpointArmour, shipX, shipY);
        armour = wasDestroyed ? 0 : checkpointArmour;
        destroyed = wasDestroyed;
        draws.state(checkpointDraws);
    }

    void rememberPosition() {
        prevX = x;
        prevY = y;
    }

    /** In its slot beside the ship at (shipX, shipY), at rest: during the launch and at a (re)start. */
    void formUp(double shipX, double shipY) {
        x = slotX(shipX);
        y = slotY(shipY);
        vx = vy = 0;
    }

    /** Its slot's x beside the ship at {@code shipX} (M8 part G: the goal its hazards may move). */
    double slotX(double shipX) {
        return Math.clamp(shipX + spec.side().sign() * spec.slot().x(), limit, PlayField.WIDTH - limit);
    }

    /** Its slot's y beside the ship at {@code shipY}. */
    double slotY(double shipY) {
        return Math.clamp(shipY - spec.slot().y(), limit, PlayField.HEIGHT - limit);
    }

    /** One step: it flies toward its slot beside the ship at (shipX, shipY); its gun's interval runs. */
    void fly(double shipX, double shipY) {
        flyTo(slotX(shipX), slotY(shipY));
    }

    /**
     * One step toward (goalX, goalY) (its slot, or M8 part G: its slot moved out of a plume or a torpedo's line,
     * {@link BulwarkHazards}); its gun's interval runs.
     */
    void flyTo(double goalX, double goalY) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (sinceShot < Integer.MAX_VALUE) {
            sinceShot++;
        }
        if (destroyed) {
            return;
        }
        double ex = goalX - x;
        double ey = goalY - y;
        double distance = Math.sqrt(ex * ex + ey * ey);
        double wantedX = 0;
        double wantedY = 0;
        if (distance > 0) {
            double speed = Math.min(spec.speed(), distance / ARRIVE_SECONDS);
            wantedX = ex / distance * speed;
            wantedY = ey / distance * speed;
        }
        double dvx = wantedX - vx;
        double dvy = wantedY - vy;
        double change = Math.sqrt(dvx * dvx + dvy * dvy);
        double most = accelerationStep;
        if (medium != null && medium.wet()) {
            // The water's drag on its acceleration (it lags in its slot).
            most *= medium.acceleration();
        }
        if (change > most) {
            dvx *= most / change;
            dvy *= most / change;
        }
        vx += dvx;
        vy += dvy;
        x = Math.clamp(x + vx * SimStep.SECONDS, limit, PlayField.WIDTH - limit);
        y = Math.clamp(y + vy * SimStep.SECONDS, limit, PlayField.HEIGHT - limit);
    }

    /** Whether its cannon fires now: ready, not destroyed and the player fires. Starts the interval when it does. */
    boolean fires(boolean playerFiring) {
        if (destroyed || cooldown > 0 || !playerFiring) {
            return false;
        }
        cooldown = spec.gun().intervalTicks();
        sinceShot = 0;
        return true;
    }

    /**
     * Whether it draws an aimed volley fired at the ship at (shipX, shipY) to itself: not destroyed, within its draw
     * radius of the ship, and its own generator's draw below its share. Draws from its generator only then.
     */
    boolean draws(double shipX, double shipY) {
        if (destroyed || spec.drawShare() <= 0) {
            return false;
        }
        double dx = x - shipX;
        double dy = y - shipY;
        if (dx * dx + dy * dy > drawRadiusSquared) {
            return false;
        }
        if (draws.nextDouble() < spec.drawShare()) {
            drawn++;
            return true;
        }
        return false;
    }

    /** Starts a new step's contact bookkeeping. */
    void beginContacts() {
        touchingCount = 0;
    }

    /** An air enemy touches it: whether it is a new contact (one that did not touch it in the last step), which hurts. */
    boolean touch(int serial) {
        if (touchingCount < CONTACTS) {
            touching[touchingCount++] = serial;
        }
        for (int i = 0; i < contactCount; i++) {
            if (contacts[i] == serial) {
                return false;
            }
        }
        return true;
    }

    /** Ends the step's contact bookkeeping. */
    void endContacts() {
        System.arraycopy(touching, 0, contacts, 0, touchingCount);
        contactCount = touchingCount;
    }

    /**
     * It takes {@code damage} on its armour: {@link SimEvents.Type#BULWARK_HIT}, and at zero {@link
     * SimEvents.Type#BULWARK_DESTROYED} (a {@code medium} explosion; out for the attempt). Returns whether it was
     * destroyed.
     */
    boolean hit(double damage, SimEvents events) {
        if (destroyed || !(damage > 0)) {
            return false;
        }
        armour = Math.max(0, armour - damage);
        events.add(SimEvents.Type.BULWARK_HIT, x, y, (int) Math.ceil(damage));
        if (armour <= 0) {
            destroyed = true;
            vx = vy = 0;
            contactCount = 0;
            events.add(SimEvents.Type.BULWARK_DESTROYED, x, y);
            return true;
        }
        return false;
    }

    void addTo(StateHash hash) {
        hash.add(x)
                .add(y)
                .add(vx)
                .add(vy)
                .add(armour)
                .add(destroyed ? 1 : 0)
                .add(cooldown)
                .add(sinceShot)
                .add(drawn)
                .add(draws.state())
                .add(contactCount);
        for (int i = 0; i < contactCount; i++) {
            hash.add(contacts[i]);
        }
    }

    Hull hull() {
        return hull;
    }

    /** M8 part G: its hazards' goal finder ({@link BulwarkHazards}). */
    BulwarkHazards hazards() {
        return hazards;
    }

    /** Its generator's state (the boss checkpoint keeps it). */
    long drawState() {
        return draws.state();
    }

    public BulwarkSpec spec() {
        return spec;
    }

    public WeaponSpec gun() {
        return spec.gun();
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    public double armour() {
        return armour;
    }

    public double maxArmour() {
        return spec.maxArmour();
    }

    /** Whether it was destroyed in this attempt (out for the rest of it). */
    public boolean destroyed() {
        return destroyed;
    }

    /** Whether it draws fire now: not destroyed and within its draw radius of the ship at (shipX, shipY) (the halo). */
    public boolean drawing(double shipX, double shipY) {
        double dx = x - shipX;
        double dy = y - shipY;
        return !destroyed && dx * dx + dy * dy <= drawRadiusSquared;
    }

    /** The aimed volleys it drew in this attempt. */
    public int drawn() {
        return drawn;
    }

    /** Steps since its cannon last fired, for its muzzle flash; {@link Integer#MAX_VALUE} before its first shot. */
    public int ticksSinceShot() {
        return sinceShot;
    }
}
