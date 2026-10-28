package vanguard.sim;

/**
 * M9 part B (design/enemies/air/talon, Squad leader; m9b-gaps.md §1.4): one wing member's break-off after its wave's
 * leader was killed. Each {@link Enemy} owns one, idle for any other unit. It turns its unit into a plain leaver
 * ({@link Enemy.Phase#LEAVE}) flying sideways at its scatter's speed, which holds its fire ({@link EnemyGunnery}) and
 * leaves through a side edge (its escape pays nothing; a scattering unit killed pays its bounty as ever). Hashed only
 * while it scatters.
 */
final class Scattering {
    private final Enemy unit;
    /** Whether it scatters (from its leader's death to its exit). */
    private boolean active;

    Scattering(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit that does not scatter. */
    void clear() {
        active = false;
    }

    /**
     * It breaks off now, away from the wing's centre at {@code centreX} (to the nearer side edge when it is on the
     * centre), at its scatter's speed or faster, so it is past that edge within its scatter's time.
     */
    void start(double centreX) {
        Enemy u = unit;
        EnemySpec.SquadScatter scatter = u.spec.scatter().orElseThrow();
        double side = u.x > centreX ? 1 : u.x < centreX ? -1 : u.x >= PlayField.WIDTH / 2.0 ? 1 : -1;
        double toEdge = side > 0 ? PlayField.WIDTH - u.x + u.box.width() / 2 : u.x + u.box.width() / 2;
        double speed = Math.max(scatter.speed(), Math.max(0, toEdge) / scatter.seconds());
        active = true;
        u.phase = Enemy.Phase.LEAVE;
        u.burstLeft = 0;
        u.glideTicks = 0;
        u.vx = side * speed;
        u.vy = 0;
    }

    /** Whether it scatters now (its gun holds). */
    boolean active() {
        return active;
    }

    /** Hashed only while it scatters, so other units hash as before. */
    void addTo(StateHash hash) {
        if (active) {
            hash.add(1);
        }
    }
}
