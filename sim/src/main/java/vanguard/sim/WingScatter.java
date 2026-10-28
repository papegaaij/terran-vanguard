package vanguard.sim;

/**
 * M9 part B (design/enemies/air/talon, Squad leader; out of {@link EnemyForce}): a wave's leader was killed: its wing
 * breaks off.
 */
final class WingScatter {
    private WingScatter() {}

    /**
     * Every living member of {@code leader}'s wave that scatters breaks off now, away from the wing's centre (the
     * members' and the leader's mean x); on hard ({@link EnemySpec.SquadScatter#partingPair()}) each fires one volley of
     * its gun at the ship as it breaks off.
     */
    static void scatter(Pool<Enemy> enemies, Enemy leader, Ship ship, SimEvents events, EnemyFire guns) {
        double sum = leader.x();
        int count = 1;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy member = enemies.get(i);
            if (member != leader && member.wave == leader.wave && scatters(member)) {
                sum += member.x();
                count++;
            }
        }
        double centre = sum / count;
        events.add(SimEvents.Type.SQUAD_SCATTER, leader.x(), leader.y(), leader.kind());
        for (int i = 0; i < enemies.size(); i++) {
            Enemy member = enemies.get(i);
            if (member == leader || member.wave != leader.wave || !scatters(member)) {
                continue;
            }
            member.scattering.start(centre);
            EnemySpec.SquadScatter rule = member.spec().scatter().orElseThrow();
            if (rule.partingPair()
                    && member.spec().gun().isPresent()
                    && PlayField.overlaps(member.x(), member.y(), member.hitbox())) {
                guns.fireAt(member, ship, -1);
            }
        }
    }

    /** Whether a wing member breaks off at its leader's death: it scatters, is not scattering yet, and flies free. */
    private static boolean scatters(Enemy member) {
        return member.spec().scatter().isPresent()
                && !member.scattering()
                && !member.leader()
                && member.flock() == null
                && member.chain() == null
                && !member.riding.rider();
    }
}
