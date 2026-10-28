package vanguard.sim;

/**
 * M9 part D (design/enemies/air/buzzsaw-drone, Ricochet off rocks): a dashing unit meets the debris chunks after the
 * moves. A large, indestructible chunk bounces it as an edge does (its outline as a circle, equal angles; the bounce
 * counts); a small chunk it touches breaks at once ({@link SimEvents.Type#DASH_SMASH}, then {@link
 * SimEvents.Type#DEBRIS_DESTROYED}, its riders tumbling off) and does not deflect it. In pool order, so it replays.
 */
final class DashRocks {
    private DashRocks() {}

    /** Every dashing unit of {@code enemies} against every chunk of {@code debris} ({@code rides}: null without riders). */
    static void meet(Pool<Enemy> enemies, Pool<Debris> debris, RockRides rides, SimEvents events) {
        if (debris.size() == 0) {
            return;
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.phase != Enemy.Phase.DASH || !unit.dashing.dashing() || unit.empStunned()) {
                continue;
            }
            for (int d = debris.size() - 1; d >= 0; d--) {
                Debris chunk = debris.get(d);
                if (chunk.pulled() || !chunk.touches(unit.x, unit.y, unit.box)) {
                    continue;
                }
                if (chunk.large()) {
                    unit.dashing.bounceOffRock(
                            chunk.x(), chunk.y(), chunk.spec().size().width() / 2);
                    if (!unit.dashing.dashing()) {
                        break;
                    }
                    continue;
                }
                events.add(SimEvents.Type.DASH_SMASH, chunk.x(), chunk.y(), unit.kind());
                events.add(SimEvents.Type.DEBRIS_DESTROYED, chunk.x(), chunk.y());
                if (rides != null) {
                    // M9 part B: a wave's rock broke: its riders tumble off.
                    rides.broken(chunk);
                }
                debris.free(d);
            }
        }
    }
}
