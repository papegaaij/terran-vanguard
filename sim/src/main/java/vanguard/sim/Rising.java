package vanguard.sim;

/**
 * M8 part D: one unit's rise (design/enemies/naval/abyss-ray; {@link EnemySpec.Rise}): on its first step it takes
 * its spot in the ship's lane, ahead of the ship, as a shadow on {@link Layer#SUB} ({@link
 * SimEvents.Type#RISE_SHADOW}); it rises there ({@link SimEvents.Type#RISE}), its layer flipping to {@link Layer#AIR}
 * at the rise's middle, glides toward the far side, its gun's clock starting with the glide, and sinks gliding on,
 * back to {@code sub} at the sink's middle, gone at its end. It places its unit through the unit's position, facing
 * and gun clock. Each {@link Enemy} owns one, used only while its stat block has a rise (its fields are hashed only
 * then). Allocation-free.
 */
final class Rising {
    private RisePhase phase = RisePhase.NONE;
    /** Steps into its phase. */
    private int ticks;
    /** Whether it is on {@link Layer#AIR} (from the rise's middle to the sink's). */
    private boolean up;
    /** Its glide's direction: 1 to the right, -1 to the left. */
    private int dir = 1;
    /** Whether its layer flipped to {@code air} in this step (the escorts it held peel off then). */
    private boolean flippedNow;

    /** Back to a unit without a rise. */
    void clear() {
        phase = RisePhase.NONE;
        ticks = 0;
        up = false;
        dir = 1;
        flippedNow = false;
    }

    /**
     * One step of {@code u}'s rise, the ship at (shipX, shipY) in its frame: its first step takes its spot; then its
     * shadow, rise, glide and sink. Returns false once it is gone (the sink's end, or off the play field while it
     * glides or sinks).
     */
    boolean step(EnemySpec.Rise rise, Enemy u, double shipX, double shipY) {
        flippedNow = false;
        if (phase == RisePhase.NONE) {
            double halfW = u.box.width() / 2;
            double halfH = u.box.height() / 2;
            u.x = u.prevX = Math.clamp(
                    shipX,
                    Math.min(halfW, PlayField.WIDTH / 2.0),
                    Math.max(PlayField.WIDTH - halfW, PlayField.WIDTH / 2.0));
            u.y = u.prevY = Math.clamp(
                    shipY + rise.ahead(),
                    Math.min(halfH, PlayField.HEIGHT / 2.0),
                    Math.max(PlayField.HEIGHT - halfH, PlayField.HEIGHT / 2.0));
            dir = u.x <= PlayField.WIDTH / 2.0 ? 1 : -1;
            u.facing = Enemy.heading(dir, 0);
            phase = RisePhase.SHADOW;
            ticks = 0;
            up = false;
            u.events.add(SimEvents.Type.RISE_SHADOW, u.x(), u.y(), u.kind());
        }
        switch (phase) {
            case SHADOW -> {
                if (++ticks >= SimStep.ticks(rise.shadowSeconds())) {
                    phase = RisePhase.RISE;
                    ticks = 0;
                    u.events.add(SimEvents.Type.RISE, u.x(), u.y(), u.kind());
                }
                return true;
            }
            case RISE -> {
                int total = riseTicks(rise);
                if (++ticks == total / 2) {
                    up = true;
                    flippedNow = true;
                }
                if (ticks >= total) {
                    phase = RisePhase.GLIDE;
                    ticks = 0;
                    // The gun's clock starts with the glide: its first-shot delay, counted down in this step already.
                    u.volleyTicks = u.spec.gun().isPresent()
                            ? SimStep.ticks(u.spec.gun().get().firstShotDelay()) + 1
                            : 0;
                    u.burstLeft = 0;
                }
                return true;
            }
            case GLIDE -> {
                u.x += dir * rise.glideSpeed() * SimStep.SECONDS;
                if (++ticks >= SimStep.ticks(rise.glideSeconds())) {
                    phase = RisePhase.SINK;
                    ticks = 0;
                }
                return PlayField.overlaps(u.x, u.y, u.box);
            }
            case SINK -> {
                u.x += dir * rise.glideSpeed() * SimStep.SECONDS;
                int total = sinkTicks(rise);
                if (++ticks == total / 2) {
                    up = false;
                }
                return ticks < total && PlayField.overlaps(u.x, u.y, u.box);
            }
            default -> {
                return true;
            }
        }
    }

    private static int riseTicks(EnemySpec.Rise rise) {
        return Math.max(2, SimStep.ticks(rise.riseSeconds()));
    }

    private static int sinkTicks(EnemySpec.Rise rise) {
        return Math.max(2, SimStep.ticks(rise.sinkSeconds()));
    }

    RisePhase phase() {
        return phase;
    }

    /** The layer it is on now: {@link Layer#AIR} from the rise's middle to the sink's, {@link Layer#SUB} otherwise. */
    Layer layer() {
        return up ? Layer.AIR : Layer.SUB;
    }

    /** Whether it touches nothing now: before its glide (its shadow and its whole rise). */
    boolean harmless() {
        return phase == RisePhase.NONE || phase == RisePhase.SHADOW || phase == RisePhase.RISE;
    }

    /** Whether its gun may fire now: while it glides with its centre on the play field (its fans' bullets on it). */
    boolean firing(Enemy u) {
        return phase == RisePhase.GLIDE && u.x >= 0 && u.x <= PlayField.WIDTH;
    }

    /** Whether its layer flipped to {@code air} in this step. */
    boolean flippedNow() {
        return flippedNow;
    }

    /** Whether it is still below the rise's middle (a shadow, or rising on {@code sub}): its escorts are held. */
    boolean holding() {
        return phase == RisePhase.NONE || phase == RisePhase.SHADOW || (phase == RisePhase.RISE && !up);
    }

    /** Its glide's direction: 1 to the right, -1 to the left. */
    int direction() {
        return dir;
    }

    /**
     * How far it is through its current phase, between the previous and the current step, 0 to 1 (the rise and the
     * sink flip their layer at 0.5); 0 before its first step.
     */
    double progress(EnemySpec.Rise rise, double alpha) {
        int total =
                switch (phase) {
                    case NONE -> 0;
                    case SHADOW -> SimStep.ticks(rise.shadowSeconds());
                    case RISE -> riseTicks(rise);
                    case GLIDE -> SimStep.ticks(rise.glideSeconds());
                    case SINK -> sinkTicks(rise);
                };
        if (total <= 0) {
            return phase == RisePhase.NONE ? 0 : 1;
        }
        return Math.clamp((ticks - 1 + alpha) / total, 0.0, 1.0);
    }

    /** Hashed only for a unit with a rise. */
    void addTo(StateHash hash) {
        hash.add(phase.ordinal()).add(ticks).add(up ? 1 : 0).add(dir);
    }
}
