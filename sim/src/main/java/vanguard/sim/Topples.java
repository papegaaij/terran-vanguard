package vanguard.sim;

/**
 * M9 part E, the walkers' topples (design/enemies/ground/strider, The topple; design/enemies, Topple): each step every
 * toppling walker's stagger and fall run on ({@link Toppling}; the EMP Burst does not stop them), and a walker whose
 * fall is over lands: every ground unit whose hit box overlaps its fall line is destroyed (hardened ones included,
 * paid and counted as kills), then the walker itself (its bounty and its standing parts' first bonus: the whole).
 * Static, like {@link Auras}; runs only in a level whose units topple. Allocates nothing.
 */
final class Topples {
    private Topples() {}

    /** One step of the topples among {@code enemies}; a crush pays through {@code hits}. */
    static void step(Pool<Enemy> enemies, SimEvents events, PlayerFire.Hits hits, Radio radio) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy walker = enemies.get(i);
            if (walker.spec().topple().isPresent() && walker.toppling.active()) {
                int ticks = walker.toppling.step();
                if (ticks == SimStep.ticks(walker.spec().topple().get().staggerSeconds())) {
                    events.add(SimEvents.Type.TOPPLE_FALL, walker.x(), walker.y(), walker.kind());
                }
            }
        }
        // A crush frees units (and may let in a death burst's): look again from the start after each.
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy walker = enemies.get(i);
            if (walker.spec().topple().isEmpty() || !walker.toppling.active()) {
                continue;
            }
            EnemySpec.Topple topple = walker.spec().topple().get();
            if (walker.toppling.ticks()
                    < SimStep.ticks(topple.staggerSeconds()) + SimStep.ticks(topple.fallSeconds())) {
                continue;
            }
            crush(walker, topple, enemies, events, hits, radio);
            i = enemies.size();
        }
    }

    private static void crush(
            Enemy walker,
            EnemySpec.Topple topple,
            Pool<Enemy> enemies,
            SimEvents events,
            PlayerFire.Hits hits,
            Radio radio) {
        double x = walker.x();
        double y = walker.y();
        double dirX = walker.toppling.dirX();
        double dirY = walker.toppling.dirY();
        int crushed = 0;
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy unit = enemies.get(j);
            if (unit == walker
                    || unit.layer() != Layer.GROUND
                    || !unit.spec().countsAsEnemy()
                    || !ShotGeometry.onField(unit)
                    || !Toppling.underLine(
                            x,
                            y,
                            dirX,
                            dirY,
                            topple.lineWidth(),
                            topple.lineLength(),
                            unit.x(),
                            unit.y(),
                            unit.hitbox().width() / 2,
                            unit.hitbox().height() / 2)) {
                continue;
            }
            // Destroyed outright (no damage rule applies: a shield, the hardened rule, a part's HP).
            hits.enemyDestroyed(j, -1);
            crushed++;
        }
        events.add(
                SimEvents.Type.TOPPLE_CRUSH,
                x + dirX * topple.lineLength() / 2,
                y + dirY * topple.lineLength() / 2,
                crushed);
        if (crushed > 0) {
            radio.cue(CueTrigger.TOPPLE_CRUSH, "");
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            if (enemies.get(j) == walker) {
                hits.enemyDestroyed(j, -1);
                return;
            }
        }
    }
}
