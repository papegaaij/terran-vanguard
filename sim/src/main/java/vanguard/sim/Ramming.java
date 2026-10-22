package vanguard.sim;

/**
 * M8 part C: one unit's ram (design/enemies/naval/glassfin; {@link EnemySpec.Ram}): it counts its time on the play
 * field from its first step there, draws once by its own seed whether it breaks off, glints (its tell), waits and
 * dives straight at where the ship was at the dive's start. Each {@link Enemy} owns one, used only while its stat
 * block's flock has a ram (its fields are hashed only then); a flock's member dives in its {@link Flock}, a unit on a
 * path (a {@code column}) leaves along the dive. Allocation-free.
 */
final class Ramming {
    /** Where a unit is in its ram. */
    enum Phase {
        /** Flying with its school or its path (it has not drawn yet, or drew to stay). */
        CRUISE,
        /** Broken off: its glint, then its wait, still flying with its school or its path. */
        TELL,
        /** Diving straight at where the ship was. */
        DIVE
    }

    private Phase phase = Phase.CRUISE;
    /** Steps since it first was on the play field; -1 before. */
    private int onTicks = -1;
    /** Whether it drew already (once per unit). */
    private boolean drawn;
    /** Steps since its tell started. */
    private int tellTicks;
    /** Its dive's velocity, px/s, in the frame its flight runs in. */
    private double vx;

    private double vy;

    /** Back to a unit that has not rammed. */
    void clear() {
        phase = Phase.CRUISE;
        onTicks = -1;
        drawn = false;
        tellTicks = 0;
        vx = 0;
        vy = 0;
    }

    /**
     * One step of {@code u}'s ram at (x, y), the ship at (shipX, shipY), both in the frame its flight runs in: its
     * clock, its draw, its tell ({@link SimEvents.Type#EYE_GLINT} as it starts) and its dive. Returns true in the step
     * its dive starts ({@link #vx()}, {@link #vy()}).
     */
    boolean step(EnemySpec.Ram ram, Enemy u, double x, double y, double shipX, double shipY) {
        switch (phase) {
            case CRUISE -> {
                if (onTicks < 0 && PlayField.overlaps(u.x(), u.y(), u.hitbox())) {
                    onTicks = 0;
                }
                if (onTicks < 0 || drawn) {
                    return false;
                }
                if (++onTicks >= SimStep.ticks(ram.afterSeconds())) {
                    drawn = true;
                    if (draw(u.serial()) < ram.share()) {
                        phase = Phase.TELL;
                        tellTicks = 0;
                        u.events.add(SimEvents.Type.EYE_GLINT, u.x(), u.y(), u.kind());
                    }
                }
                return false;
            }
            case TELL -> {
                if (++tellTicks < SimStep.ticks(ram.tellSeconds()) + SimStep.ticks(ram.waitSeconds())) {
                    return false;
                }
                double dx = shipX - x;
                double dy = shipY - y;
                double length = Math.sqrt(dx * dx + dy * dy);
                if (!(dy < 0) || length == 0) {
                    // The ship is not below it: straight down.
                    dx = 0;
                    dy = -1;
                    length = 1;
                }
                vx = dx / length * ram.speed();
                vy = dy / length * ram.speed();
                phase = Phase.DIVE;
                return true;
            }
            case DIVE -> {
                return false;
            }
        }
        return false;
    }

    /** A uniform draw in [0, 1) from {@code seed} alone (SplitMix64's mix): a unit's own, whatever else drew. */
    static double draw(long seed) {
        long z = seed * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    Phase phase() {
        return phase;
    }

    /** Whether it glints now (its tell, before its wait). */
    boolean glinting(EnemySpec.Ram ram) {
        return phase == Phase.TELL && tellTicks < SimStep.ticks(ram.tellSeconds());
    }

    /** Whether it dives. */
    boolean diving() {
        return phase == Phase.DIVE;
    }

    double vx() {
        return vx;
    }

    double vy() {
        return vy;
    }

    /** Hashed only for a unit whose flock has a ram. */
    void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(onTicks)
                .add(drawn ? 1 : 0)
                .add(tellTicks)
                .add(vx)
                .add(vy);
    }
}
