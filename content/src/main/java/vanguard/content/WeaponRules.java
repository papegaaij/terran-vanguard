package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.Armament;
import vanguard.sim.DeflectorSpec;
import vanguard.sim.DroneSpec;
import vanguard.sim.WeaponSpec;

/**
 * The player's weapons for the simulation ({@link SimSpecs}): which weapons fly, a weapon in a slot at an
 * upgrade level as its mount and pattern (muzzles from the ship's mount points), a Deflector Pod's field
 * and a Light Drone Bay's drone (design/player/weapons).
 */
final class WeaponRules {
    private WeaponRules() {}

    /**
     * Whether the simulation flies the weapon: standard bolts, homing missiles and turrets, dropped
     * and lobbed ground-only shots, proximity mines and (M5 part E) torpedoes, which run only in a
     * level over water. M7 part A: the Rear Lance flies (standard bolts), and the Ion Beam ({@code hits:
     * beam}), the Light Drone Bay (a {@code drone}) and the Deflector Pod ({@code hits: deflector}), each
     * as its {@link #mount}.
     */
    static boolean flies(Content content, String weapon) {
        WeaponData data = content.weapon(weapon);
        return data.deflector() || delivery(data).isPresent();
    }

    /**
     * M7 part A: a weapon fitted in a slot at an upgrade level as the simulation's mount, with its
     * overdrive (the next level's numbers, the overdrive row at L5): a gun ({@link #weapon}), a Light
     * Drone Bay (its drone's gun and the drone, {@link Armament.Mount#droneBay}) or a Deflector Pod (its
     * field, {@link Armament.Mount#deflector}); the turn rate raised by {@code turnBonus} as in {@link
     * #weapon(Content, Armament.Slot, String, int, double)}.
     */
    static Armament.Mount mount(Content content, Armament.Slot slot, String slug, int upgradeLevel, double turnBonus) {
        WeaponData data = content.weapon(slug);
        if (data.deflector()) {
            return Armament.Mount.deflector(
                    slot, field(level(data, upgradeLevel)), field(level(data, upgradeLevel + 1)));
        }
        WeaponSpec weapon = weapon(content, slot, slug, upgradeLevel, turnBonus);
        WeaponSpec overdrive = weapon(content, slot, slug, upgradeLevel + 1, turnBonus);
        if (data.drone().isPresent()) {
            WeaponData.Drone drone = data.drone().get();
            return Armament.Mount.droneBay(
                    slot,
                    weapon,
                    overdrive,
                    drone(drone, level(data, upgradeLevel)),
                    drone(drone, level(data, upgradeLevel + 1)));
        }
        return new Armament.Mount(slot, weapon, overdrive);
    }

    /** An upgrade level's numbers, 6 being the overdrive row. */
    private static WeaponData.Level level(WeaponData weapon, int upgradeLevel) {
        return upgradeLevel > weapon.levels().size()
                ? weapon.overdrive()
                : weapon.levels().get(upgradeLevel - 1);
    }

    /** M7 part A: a Deflector Pod's field at one level. */
    private static DeflectorSpec field(WeaponData.Level level) {
        return new DeflectorSpec(
                level.radius().orElseThrow(),
                level.charges().orElseThrow(),
                level.recharge().orElseThrow());
    }

    /** M7 part A: the Light Drone Bay's drone at one level. */
    private static DroneSpec drone(WeaponData.Drone drone, WeaponData.Level level) {
        WeaponData.Contact contact = drone.contact();
        return new DroneSpec(
                drone.orbit(),
                drone.period(),
                drone.converge(),
                level.droneHits().orElseThrow(),
                level.rebuild().orElseThrow(),
                drone.fadeIn(),
                contact.hits(),
                contact.smallHits(),
                contact.laserHits(),
                contact.damage(),
                SimSpecs.hitbox(drone.hitbox()));
    }

    private static Optional<WeaponSpec.Delivery> delivery(WeaponData weapon) {
        return switch (weapon.hits()) {
            // M7 part A: the Light Drone Bay's drone fires standard bolts too.
            case "standard" -> Optional.of(WeaponSpec.Delivery.BOLT);
            case WeaponData.BEAM -> Optional.of(WeaponSpec.Delivery.BEAM);
            case "homing" ->
                Optional.of(weapon.slew().isPresent() ? WeaponSpec.Delivery.TURRET : WeaponSpec.Delivery.HOMING);
            case WeaponData.MINES -> Optional.of(WeaponSpec.Delivery.MINE);
            case WeaponData.TORPEDO -> Optional.of(WeaponSpec.Delivery.TORPEDO);
            case "ground-only" ->
                Optional.of(
                        weapon.range().orElseThrow().kind() == WeaponData.Range.Kind.DROP
                                ? WeaponSpec.Delivery.DROPPED
                                : WeaponSpec.Delivery.LOBBED);
            default -> Optional.empty();
        };
    }

    /**
     * A weapon in a slot at an upgrade level, 6 being its overdrive pattern. The muzzles follow from
     * the ship's mount points: the front muzzle, the rear muzzle or, for a weapon that fires to both
     * sides, the wing roots (the pattern to the right, mirrored to the left); a pod fires from its
     * wing mount, the left one mirrored, and turns in by the weapon's convergence. A weapon with
     * ports fires the shots of a volley from them in turn, left first.
     */
    static WeaponSpec weapon(Content content, Armament.Slot slot, String slug, int upgradeLevel) {
        return weapon(content, slot, slug, upgradeLevel, 0);
    }

    /** As {@link #weapon(Content, Armament.Slot, String, int)}, its turn rate raised by {@code turnBonus} (a share). */
    static WeaponSpec weapon(Content content, Armament.Slot slot, String slug, int upgradeLevel, double turnBonus) {
        WeaponData weapon = content.weapon(slug);
        WeaponSpec.Delivery delivery = delivery(weapon)
                .orElseThrow(
                        () -> new IllegalArgumentException(slug + ": its hits '" + weapon.hits() + "' do not fly yet"));
        WeaponData.Level level = level(weapon, upgradeLevel);
        ShipData.Mounts mounts = content.ship().mounts();
        double centre = content.ship().size() / 2;
        if (delivery == WeaponSpec.Delivery.BEAM) {
            // M7 part A: one column from the nose (the front mount), its width and damage per second.
            return WeaponSpec.beam(
                    slug,
                    weapon.vfx(),
                    weapon.sfx(),
                    level.width().orElseThrow(),
                    level.damage(),
                    muzzle(mounts.front(), centre, 0, 0));
        }
        List<WeaponSpec.Muzzle> muzzles = new ArrayList<>();
        double ports = weapon.ports().orElse(0.0);
        for (WeaponData.Shot shot : level.pattern()) {
            double port = muzzles.size() % 2 == 0 ? -ports : ports;
            switch (slot) {
                case FRONT -> muzzles.add(muzzle(mounts.front(), centre, shot.x() + port, shot.angle()));
                case REAR -> {
                    if (weapon.mirrored().orElse(false)) {
                        muzzles.add(muzzle(mounts.roots().get(1), centre, shot.x(), shot.angle()));
                        muzzles.add(muzzle(mounts.roots().get(0), centre, -shot.x(), -shot.angle()));
                    } else {
                        muzzles.add(muzzle(mounts.rear(), centre, shot.x(), shot.angle()));
                    }
                }
                case LEFT_WING ->
                    muzzles.add(muzzle(
                            mounts.wings().get(0),
                            centre,
                            -shot.x(),
                            -shot.angle() + weapon.converge().orElse(0.0)));
                case RIGHT_WING ->
                    muzzles.add(muzzle(
                            mounts.wings().get(1),
                            centre,
                            shot.x(),
                            shot.angle() - weapon.converge().orElse(0.0)));
            }
        }
        double speed = weapon.speed().map(WeaponData.Speed::start).orElse(0.0);
        WeaponSpec.Mines mines = delivery == WeaponSpec.Delivery.MINE
                ? new WeaponSpec.Mines(
                        weapon.drift().orElseThrow(),
                        weapon.arm().orElseThrow(),
                        weapon.trigger().orElseThrow(),
                        level.maxLive().orElseThrow())
                : WeaponSpec.Mines.NONE;
        return new WeaponSpec(
                        slug,
                        weapon.vfx(),
                        weapon.sfx(),
                        delivery,
                        weapon.traits().contains("anti-ground"),
                        level.rate(),
                        level.damage(),
                        speed,
                        SimSpecs.hitbox(weapon.size().orElseThrow()),
                        weapon.range()
                                .map(range -> switch (range.kind()) {
                                    case SCREEN -> Double.POSITIVE_INFINITY;
                                    case DROP -> 0.0;
                                    case DISTANCE -> range.px();
                                })
                                .orElse(Double.POSITIVE_INFINITY),
                        weapon.lifetime().orElse(Double.POSITIVE_INFINITY),
                        level.pierce().orElse(1),
                        level.blast().orElse(0.0),
                        // The one turn rate (a homing missile's or a turret's slew) the Targeting computer's bonus
                        // scales; not a torpedo's, which is not homing (M5 part E).
                        Math.toRadians(level.turn().or(weapon::slew).orElse(0.0)
                                * (1 + (delivery == WeaponSpec.Delivery.TORPEDO ? 0 : turnBonus))),
                        Math.toRadians(weapon.cone().orElse(360.0) / 2),
                        weapon.fall().or(weapon::flight).orElse(0.0),
                        weapon.snap().orElse(0.0),
                        muzzles,
                        weapon.speed().map(WeaponData.Speed::end).orElse(speed),
                        weapon.accelerate().orElse(0.0),
                        mines)
                // M5 part F (F3 = a): a side or rear weapon's shots hit a latched Lamprey.
                .flanking(PartFRules.flanking(weapon));
    }

    /** A projectile leaving a mount point (sprite pixels from the top left) with an offset and an angle in degrees. */
    private static WeaponSpec.Muzzle muzzle(Point mount, double centre, double offset, double degrees) {
        return new WeaponSpec.Muzzle(mount.x() - centre + offset, centre - mount.y(), Math.toRadians(degrees));
    }
}
