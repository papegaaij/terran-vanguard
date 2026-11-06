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
 * @param scenery M11 part D, step CD1 (design/campaign Level 46, the dying Leviathan; m11d-gaps.md §1 #4, §4): it is
 *     scenery for the counts ({@link Scenery}); empty for every other set piece
 */
public record SetPieceSpec(
        String slug,
        Hitbox size,
        Hitbox body,
        double contactDamage,
        List<PartSpec> parts,
        List<Pass> passes,
        Optional<PickupType> drop,
        Optional<BossSpec> boss,
        Optional<Scenery> scenery) {
    /** The body hits the ship at most once in this time. */
    public static final double HIT_INTERVAL_SECONDS = 1;
    /** The most parts a set piece can have (homing locks and pierce keys encode the part in this). */
    public static final int MAX_PARTS = 32;

    public SetPieceSpec {
        java.util.Objects.requireNonNull(scenery);
        if (scenery.isPresent() && boss.isPresent()) {
            throw new IllegalArgumentException(slug + ": a boss is never scenery (M11 part D)");
        }
        parts = List.copyOf(parts);
        passes = List.copyOf(passes);
        if (parts.isEmpty() || parts.size() > MAX_PARTS || passes.isEmpty() == boss.isEmpty()) {
            throw new IllegalArgumentException(
                    slug + ": a set piece has 1–" + MAX_PARTS + " parts and passes, or a boss script");
        }
        if (boss.isPresent() && boss.get().squadron().isPresent()) {
            // M10 part F: a squadron's aces are its parts, none vital: the last one's death destroys it.
            boss.get().squadron().get().check(slug, parts, boss.get().attacks());
        } else if (boss.isPresent() && boss.get().escape().isPresent()) {
            // M10 part H, step H1b: an escaping boss has no vital part: its hull breaks on its breaking part instead.
            if (parts.stream().anyMatch(PartSpec::vital)) {
                throw new IllegalArgumentException(slug + ": an escaping boss has no vital part");
            }
        } else if (parts.stream().noneMatch(PartSpec::vital)) {
            throw new IllegalArgumentException(slug + ": a set piece has a vital part");
        }
        // M10 part H, step H1a: the per-part components name its parts.
        if (boss.isPresent()) {
            boss.get().checkParts(slug, parts.size());
        }
    }

    /** A set piece or a boss, not scenery (every one before Act 7). */
    public SetPieceSpec(
            String slug,
            Hitbox size,
            Hitbox body,
            double contactDamage,
            List<PartSpec> parts,
            List<Pass> passes,
            Optional<PickupType> drop,
            Optional<BossSpec> boss) {
        this(slug, size, body, contactDamage, parts, passes, drop, boss, Optional.empty());
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

    /**
     * M11 part D, step CD1 (design/campaign Level 46, the dying Leviathan; m11d-gaps.md §4): a set piece flown as
     * scenery for the counts: no bounty (its parts pay nothing), not a kill, never an enemy for the kill ratio, its guns
     * silent, no contact, Rook never picks it. Each part shot off drops {@code partDrop}; its death (the vital part
     * first) drops every part's {@code partDrop} not yet dropped, then its own {@link SetPieceSpec#drop()} (the death
     * drop).
     */
    public record Scenery(Optional<PickupType> partDrop) {
        public Scenery {
            java.util.Objects.requireNonNull(partDrop);
        }
    }

    /**
     * M11 part D, step CD1: this set piece flown as scenery ({@link Scenery}): each part shot off drops {@code partDrop},
     * its death {@code deathDrop} (overriding its stat block's drop); empty for none.
     */
    public SetPieceSpec withScenery(Optional<PickupType> partDrop, Optional<PickupType> deathDrop) {
        return new SetPieceSpec(
                slug, size, body, contactDamage, parts, passes, deathDrop, boss, Optional.of(new Scenery(partDrop)));
    }

    /** M11 part D, step CD1: whether it is scenery for the counts ({@link Scenery}). */
    public boolean isScenery() {
        return scenery.isPresent();
    }

    /** Whether it is a boss (it has a boss script rather than passes). */
    public boolean isBoss() {
        return boss.isPresent();
    }

    /** Its bounty: the parts' together (M11 part D: none for scenery). */
    public int bounty() {
        return scenery.isPresent()
                ? 0
                : parts.stream().mapToInt(PartSpec::bounty).sum();
    }
}
