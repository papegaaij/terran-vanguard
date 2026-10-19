package vanguard.content;

import java.util.List;
import java.util.Map;

/**
 * design/enemies/data.yaml: the balancing basis and the formation vocabulary all enemies share.
 *
 * @param referenceDps the single-target player DPS the stat blocks assume, per level
 * @param bulletDamage damage to the player per bullet class
 * @param contactDamage damage of touching an enemy, per size tier
 * @param smallActHp the returning {@code small} units that get the act HP factor, and its cap for them
 * @param formations the formation names with their descriptions
 */
public record EnemyBasisData(
        Map<Integer, Double> referenceDps,
        List<BulletDamage> bulletDamage,
        Map<Tier, Double> contactDamage,
        SmallActHp smallActHp,
        Map<String, String> formations) {
    public EnemyBasisData {
        referenceDps = Map.copyOf(referenceDps);
        Check.that(contactDamage.size() == Tier.values().length, "contact_damage: one value per size tier");
        contactDamage = Map.copyOf(contactDamage);
        formations = Map.copyOf(formations);
    }

    /**
     * The user's Act 2 playtest (2026-10-18, PT2-B 2c): the {@code small} units (by slug) that get the act HP
     * factor like the {@code medium} ones in the listed levels, capped at {@code cap}.
     */
    public record SmallActHp(double cap, List<String> units, List<Integer> levels) {
        public SmallActHp {
            Check.that(cap >= 1, "small_act_hp.cap: at least 1");
            units = List.copyOf(units);
            levels = List.copyOf(levels);
        }

        /** Whether the cap applies to {@code slug} in Level {@code level}. */
        public boolean applies(String slug, int level) {
            return units.contains(slug) && levels.contains(level);
        }
    }

    /** Damage of one bullet class. */
    public record BulletDamage(String bullet, double damage) {
        public BulletDamage {
            Check.positive("damage", damage);
        }
    }

    /** Whether {@code bullet} names a bullet class. */
    boolean knowsBullet(String bullet) {
        return bulletDamage.stream().anyMatch(b -> b.bullet().equals(bullet));
    }
}
