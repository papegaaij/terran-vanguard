package vanguard.sim;

/**
 * One escort circling its carrier (design/enemies/air, the escorts of Level 04; M7 part D: a Herald's inside its
 * aura), then breaking off toward the ship once the carrier ended. Each {@link Enemy} owns one, idle for any unit
 * that did not enter as an escort; it places its unit on its circle through the unit's movement fields (its
 * {@code angle} is the unit's).
 */
final class Escorting {
    private final Enemy unit;

    /** Whether it entered as an escort; the escort's fields are in the state hash only then. */
    private boolean escort;
    /** The carrier it circles; null once that ended (or when it entered without one). */
    private Enemy carrier;
    /**
     * M10 part B (design/campaign Level 36, the harvesters): its carrier's partner, a drifting carrier of the same wave:
     * it circles the two's midpoint while both live; null for none (every earlier escort).
     */
    private Enemy partner;

    private double escortRadius;
    private double escortRate;
    /** Steps left before an escort whose carrier ended breaks off toward the ship. */
    private int breakTicks;

    private double centreX;
    private double centreY;

    Escorting(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit that is no escort. */
    void clear() {
        escort = false;
        carrier = null;
        partner = null;
        escortRadius = 0;
        escortRate = 0;
        breakTicks = 0;
        centreX = 0;
        centreY = 0;
    }

    /** It enters as an escort on {@code circle} (its carrier comes with {@link #circle(Enemy)}). */
    void start(Spawn.Escort circle) {
        escort = true;
        escortRadius = circle.radius();
        escortRate = circle.radiansPerSecond();
        breakTicks = SimStep.ticks(circle.breakSeconds());
    }

    /**
     * It starts circling {@code circled} (null: it entered without a carrier, so it breaks off at once from where it
     * is).
     */
    void circle(Enemy circled) {
        circle(circled, null);
    }

    /**
     * As {@link #circle(Enemy)}; M10 part B (design/campaign Level 36, the harvesters): with {@code pair} (a drifting
     * carrier of {@code circled}'s wave; null for none) it circles the two's midpoint while both live, then the one
     * left, and breaks off once both are gone.
     */
    void circle(Enemy circled, Enemy pair) {
        Enemy u = unit;
        carrier = circled;
        partner = circled == null ? null : pair;
        if (circled != null && circled.spec.aura().isPresent()) {
            // M7 part D: a Herald's escorts circle it inside its aura (the gap check's 0.8, pending round 40).
            EnemySpec.Aura aura = circled.spec.aura().get();
            escortRadius = aura.radius() * aura.escortRadiusShare();
        }
        centreX = circled != null ? circled.x : u.x;
        // M7 part G: the carrier's height in this unit's frame.
        centreY = circled != null ? u.frame.y(circled.y()) : u.y;
        if (partner != null) {
            centreX = (circled.x + partner.x) / 2;
            centreY = u.frame.y((circled.y() + partner.y()) / 2);
        }
        u.x = u.prevX = centreX + escortRadius * Trig.cos(u.angle);
        u.y = u.prevY = centreY + escortRadius * Trig.sin(u.angle);
    }

    /** Whether it circles {@code circled} (M10 part B: or the midpoint of it and its partner). */
    boolean escorts(Enemy circled) {
        return carrier == circled || (partner != null && partner == circled);
    }

    /**
     * M10 part B: carrier {@code gone} ended: with a partner left it circles that one from now; otherwise as {@link
     * #carrierEnded()}.
     */
    void ended(Enemy gone) {
        if (partner == null) {
            carrierEnded();
        } else if (partner == gone) {
            partner = null;
        } else if (carrier == gone) {
            carrier = partner;
            partner = null;
        }
    }

    /** Its carrier ended: it circles the carrier's last position until it breaks off toward the ship. */
    void carrierEnded() {
        carrier = null;
    }

    /**
     * One step on its circle round its carrier (or the carrier's last position); returns false when it breaks off
     * now instead (its break time after its carrier ended is over).
     */
    boolean step() {
        Enemy u = unit;
        if (carrier != null && partner != null) {
            // M10 part B (design/campaign Level 36): the midpoint of the pair of drifting carriers.
            centreX = (carrier.x + partner.x) / 2;
            centreY = u.frame.y((carrier.y() + partner.y()) / 2);
        } else if (carrier != null) {
            centreX = carrier.x;
            centreY = u.frame.y(carrier.y());
        } else if (--breakTicks < 0) {
            return false;
        }
        u.angle += escortRate * SimStep.SECONDS;
        u.x = centreX + escortRadius * Trig.cos(u.angle);
        u.y = centreY + escortRadius * Trig.sin(u.angle);
        if (u.offX != 0 || u.offY != 0) {
            // M7 part A: shifted by its drift while stunned.
            u.x += u.offX;
            u.y += u.offY;
        }
        return true;
    }

    /** Its state, as the unit's hash has always added it; nothing for a unit that is no escort. */
    void addTo(StateHash hash) {
        if (escort) {
            hash.add(breakTicks).add(centreX).add(centreY).add(carrier == null ? -1 : carrier.serial());
            if (partner != null) {
                // M10 part B: only a pair's escort adds the partner, so the other levels hash as before.
                hash.add(partner.serial());
            }
        }
    }
}
