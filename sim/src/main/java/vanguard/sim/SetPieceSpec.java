package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A huge multi-part unit that flies its {@code passes} one after another (design/enemies/space/
 * leviathan): an armoured {@code body} that makes shots glance and deals {@code contactDamage}
 * on the player's layer, at most once per {@link #HIT_INTERVAL_SECONDS}, and parts at fixed
 * offsets that take the hits. Destroying the vital part destroys the rest and the unit, which
 * drops its {@code drop}; a unit that ends its last pass alive has escaped.
 *
 * @param slug its enemy's directory name
 * @param size its sprite, px, facing down; the unit is gone when this box around it has left the screen
 * @param body the armoured body's hit box around its centre
 */
public record SetPieceSpec(
        String slug,
        Hitbox size,
        Hitbox body,
        double contactDamage,
        List<PartSpec> parts,
        List<Pass> passes,
        Optional<PickupType> drop,
        Optional<BossSpec> boss) {
    /** The body hits the ship at most once in this time. */
    public static final double HIT_INTERVAL_SECONDS = 1;
    /** The most parts a set piece can have (homing locks and pierce keys encode the part in this). */
    public static final int MAX_PARTS = 32;

    public SetPieceSpec {
        parts = List.copyOf(parts);
        passes = List.copyOf(passes);
        if (parts.isEmpty() || parts.size() > MAX_PARTS || passes.isEmpty() == boss.isEmpty()) {
            throw new IllegalArgumentException(
                    slug + ": a set piece has 1–" + MAX_PARTS + " parts and passes, or a boss script");
        }
        if (parts.stream().noneMatch(PartSpec::vital)) {
            throw new IllegalArgumentException(slug + ": a set piece has a vital part");
        }
    }

    /** A set piece flying passes, without a boss script. */
    public SetPieceSpec(
            String slug,
            Hitbox size,
            Hitbox body,
            double contactDamage,
            List<PartSpec> parts,
            List<Pass> passes,
            Optional<PickupType> drop) {
        this(slug, size, body, contactDamage, parts, passes, drop, Optional.empty());
    }

    /** Whether it is a boss (it has a boss script rather than passes). */
    public boolean isBoss() {
        return boss.isPresent();
    }

    /** Its bounty: the parts' together. */
    public int bounty() {
        return parts.stream().mapToInt(PartSpec::bounty).sum();
    }
}
