package vanguard.sim;

/**
 * M8 part A, the Bulwark heavy drone in the escort slot (design/player/wingmen, Heavy drone "Bulwark"), built by
 * {@code vanguard.content.SimSpecs} from design/player/wingmen/data.yaml's {@code bulwark}: it holds its wing slot
 * beside the ship (no formation changes), fires its cannon straight ahead while the player fires, takes the enemies'
 * bullets, mines, missiles and contacts on its armour (no shield) and is out for the attempt at zero; while within
 * {@code drawRadius} of the ship it draws a {@code drawShare} of the enemies' aimed shots at the ship to itself.
 *
 * @param side the player's side its slot is on (the escort's hangar setting)
 * @param armour its armour at the level start (a retry starts it with it again)
 * @param maxArmour its full armour
 * @param size the edge of its square sprite, px
 * @param hitbox on the {@code air} layer
 * @param speed its top speed, px/s (under water its acceleration takes the drag, as Rook's)
 * @param slot its wing slot: px beside the ship on its side, px behind (down the screen)
 * @param gun its cannon (one shell a second straight ahead, {@code anti-sub}), its muzzle around its centre
 * @param drawRadius while its centre is within this of the ship's, it draws fire, px
 * @param drawShare the share of the aimed shots at the ship it draws then
 */
public record BulwarkSpec(
        WingmanSpec.Side side,
        double armour,
        double maxArmour,
        double size,
        Hitbox hitbox,
        double speed,
        WingmanSpec.Offset slot,
        WeaponSpec gun,
        double drawRadius,
        double drawShare) {
    public BulwarkSpec {
        if (!(maxArmour > 0) || !(armour > 0 && armour <= maxArmour)) {
            throw new IllegalArgumentException("a Bulwark flies with armour in (0, " + maxArmour + "], not " + armour);
        }
        if (!(size > 0) || !(speed > 0) || !(drawRadius >= 0) || !(drawShare >= 0 && drawShare <= 1)) {
            throw new IllegalArgumentException("a Bulwark needs a size, a speed, a draw radius and a share in [0, 1]");
        }
    }

    /** The same Bulwark starting the level with {@code startArmour}. */
    public BulwarkSpec withArmour(double startArmour) {
        return new BulwarkSpec(side, startArmour, maxArmour, size, hitbox, speed, slot, gun, drawRadius, drawShare);
    }
}
