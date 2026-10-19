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
 * @param beamEraHp M7 part E: the HP factor of the beam era, from the level the balance plan fits the Ion Beam
 * @param formations the formation names with their descriptions
 */
public record EnemyBasisData(
        Map<Integer, Double> referenceDps,
        List<BulletDamage> bulletDamage,
        Map<Tier, Double> contactDamage,
        SmallActHp smallActHp,
        BeamEraHp beamEraHp,
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

    /**
     * M7 part E (the user's decision of 2026-10-19, "tougher, not more"; step E5): from Level {@code fromLevel} on, the
     * first level the balance plan fits the Ion Beam, a unit of one of the listed {@code tiers} (bosses aside) takes
     * {@code factor} on its HP on top of the act HP factor, its own first level included: the beam burns the first
     * target in its column every step from the moment it touches, so the plan's fit kills a standard unit far sooner
     * than its single-target DPS against the reference says, and the factor gives the units back Level 10's time on the
     * screen (the user's reference) so they live long enough to shoot back. Its bounty is unchanged.
     */
    public record BeamEraHp(int fromLevel, double factor, List<Tier> tiers) {
        public BeamEraHp {
            Check.that(fromLevel >= 1, "beam_era_hp.from_level: at least 1");
            Check.that(factor >= 1, "beam_era_hp.factor: at least 1");
            tiers = List.copyOf(tiers);
        }

        /** The factor on a unit of {@code tier} in Level {@code level}: {@link #factor} from {@link #fromLevel}, else 1. */
        public double of(Tier tier, int level) {
            return level >= fromLevel && tiers.contains(tier) ? factor : 1;
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
