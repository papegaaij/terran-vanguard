package vanguard.sim;

/**
 * M10 part A (design/enemies, Reflect), for {@link ShotHits}: a player shot meeting a standing mirror within its arc.
 * The player's own shots bounce back as a slow enemy {@code small} bullet along the mirror image of their path, at most
 * one every gap per mirror (one in between is absorbed with a spark, as is one that finds the bullet budget full); the
 * escort's shots (Rook's) and a bounced bolt (the Reflex Shield's) are absorbed, never bounced: a bounced bullet always
 * comes from the player's own fire and never bounces twice. Shots outside the arc (the open flanks and rear) and the
 * landing and laid deliveries (bombs, shells, mines) pass to the hull. Allocates nothing.
 */
final class Reflections {
    private final Armament armament;
    private final SimEvents events;
    private final PlayerFire.Hits hits;

    Reflections(Armament armament, SimEvents events, PlayerFire.Hits hits) {
        this.armament = armament;
        this.events = events;
        this.hits = hits;
    }

    /**
     * Whether {@code unit}'s mirror takes {@code shot} (bounced: {@link SimEvents.Type#MIRROR_BOUNCED}, or absorbed:
     * {@link SimEvents.Type#MIRROR_ABSORBED}, at the shot; value the mount); false when it passes to the hull.
     */
    boolean meet(Shot shot, Enemy unit) {
        EnemySpec.Mirror spec = unit.spec().mirror().orElseThrow();
        Mirroring mirror = unit.mirror();
        if (!mirror.meets(spec, shot.vx(), shot.vy())) {
            return false;
        }
        boolean own = shot.mount() >= 0 && shot.mount() < armament.size() && !shot.bounced();
        if (own && mirror.bounceReady()) {
            mirror.reflect(spec, shot.vx(), shot.vy());
            if (hits.bounce(shot.x(), shot.y(), mirror.reflectedVx(), mirror.reflectedVy(), spec.bulletDamage())) {
                events.add(SimEvents.Type.MIRROR_BOUNCED, shot.x(), shot.y(), shot.mount());
                return true;
            }
        }
        events.add(SimEvents.Type.MIRROR_ABSORBED, shot.x(), shot.y(), shot.mount());
        return true;
    }
}
