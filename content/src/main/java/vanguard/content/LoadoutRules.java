package vanguard.content;

import static vanguard.content.SimSpecs.AIRSTRIKE;
import static vanguard.content.SimSpecs.EMP_BURST;
import static vanguard.content.SimSpecs.EVASIVE_THRUSTERS;
import static vanguard.content.SimSpecs.ORBITAL_LANCE;
import static vanguard.content.SimSpecs.PICKUP_MAGNET;
import static vanguard.content.SimSpecs.PULSE_CANNON;
import static vanguard.content.SimSpecs.SALVAGE_SCANNER;
import static vanguard.content.SimSpecs.SHIELD_OVERCHARGE;
import static vanguard.content.SimSpecs.SMART_BOMB;
import static vanguard.content.SimSpecs.TARGETING_COMPUTER;

import java.util.List;
import java.util.function.Function;
import vanguard.content.SimSpecs.FittedWeapon;
import vanguard.sim.AirstrikeSpec;
import vanguard.sim.Armament;
import vanguard.sim.EmpSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Hull;
import vanguard.sim.LanceSpec;
import vanguard.sim.Loadout;
import vanguard.sim.Magnet;
import vanguard.sim.OverchargeSpec;
import vanguard.sim.Plating;
import vanguard.sim.ShieldModel;
import vanguard.sim.ShipSpec;
import vanguard.sim.SmartBombSpec;
import vanguard.sim.SpecialSpec;
import vanguard.sim.ThrusterSpec;
import vanguard.sim.WeaponSpec;
import vanguard.sim.WingmanSpec;

/**
 * The Stormhawk's fit for the simulation ({@link SimSpecs}): the hull with its engine, the shield and
 * plating, the utility modules, the specials and Rook in the escort slot (design/player).
 */
final class LoadoutRules {
    private LoadoutRules() {}

    /**
     * The starting fit: the hull with the starter engine, the Pulse Cannon at L1, the starter
     * shield (with the difficulty's regen lever and the spare-power bonus of the starter generator)
     * and plating.
     */
    static Loadout starterLoadout(Content content, Difficulty difficulty) {
        SystemsData.Engine engine = content.systems().engines().getFirst();
        ShieldData.Model shield = content.shields().models().getFirst();
        WeaponData pulse = content.weapon(PULSE_CANNON);
        double spare =
                content.generators().models().getFirst().output() - pulse.draw().min() - shield.draw() - engine.draw();
        return loadout(
                content,
                engine.name(),
                List.of(new FittedWeapon(Armament.Slot.FRONT, PULSE_CANNON, 1)),
                shield.name(),
                content.armour().plating().getFirst().name(),
                spare,
                difficulty);
    }

    /**
     * A fit of the parts the simulation flies: the hull with an engine, the weapons at their
     * levels, a shield (with the difficulty's regen lever and the bonus of {@code sparePower} MW,
     * design/player/generator) and a plating, each by its name in its data file.
     */
    static Loadout loadout(
            Content content,
            String engine,
            List<FittedWeapon> weapons,
            String shield,
            String plating,
            double sparePower,
            Difficulty difficulty) {
        return loadout(content, engine, weapons, shield, plating, sparePower, difficulty, 0);
    }

    /**
     * As {@link #loadout(Content, String, List, String, String, double, Difficulty)}, the weapons'
     * one turn rate (a homing missile's turn, a turret's slew) raised by {@code turnBonus}: the
     * fitted Targeting computer's share (design/player/systems), 0 without one. Only the
     * Stormhawk's own weapons: Rook's guns are built by {@link #wingman} at their base turn.
     */
    static Loadout loadout(
            Content content,
            String engine,
            List<FittedWeapon> weapons,
            String shield,
            String plating,
            double sparePower,
            Difficulty difficulty,
            double turnBonus) {
        ShieldModel model =
                shield(content, named(content.shields().models(), ShieldData.Model::name, shield), difficulty);
        double regen = model.regenPerSecond() * (1 + regenBonus(content, sparePower));
        return new Loadout(
                ship(content, named(content.systems().engines(), SystemsData.Engine::name, engine)),
                new Armament(weapons.stream()
                        .map(fitted ->
                                WeaponRules.mount(content, fitted.slot(), fitted.weapon(), fitted.level(), turnBonus))
                        .toList()),
                new ShieldModel(model.capacity(), regen, model.regenDelaySeconds(), model.breakSeconds()),
                plating(named(content.armour().plating(), ArmourData.Plating::name, plating)));
    }

    /** The shield regen bonus of {@code sparePower} spare MW (design/player/generator): +10 % per MW, at most +50 %. */
    static double regenBonus(Content content, double sparePower) {
        GeneratorData.SparePower rule = content.generators().sparePower();
        return Math.clamp(sparePower * rule.regenBonusPerMw(), 0, rule.maxRegenBonus());
    }

    private static <T> T named(List<T> parts, Function<T, String> name, String wanted) {
        return parts.stream()
                .filter(part -> name.apply(part).equals(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no part '" + wanted + "'"));
    }

    static ShipSpec ship(Content content, SystemsData.Engine engine) {
        ShipData ship = content.ship();
        return new ShipSpec(
                engine.speed(),
                ship.accelerationSeconds(),
                ship.stopSeconds(),
                ship.precisionFactor(),
                ship.size(),
                ship.edgeGap(),
                hull(ship),
                ship.mercySeconds(),
                ship.size() / 2 - ship.mounts().front().y(),
                ship.bankChangeSteps() / ShipSpec.HARD_BANK);
    }

    /**
     * Whether the simulation flies the utility module of this name: the Pickup magnet, the Targeting
     * computer, the Salvage scanner and (M7 part A) the Evasive thrusters; the later acts' modules
     * follow with them.
     */
    static boolean fliesUtility(String name) {
        return name.equals(PICKUP_MAGNET)
                || name.equals(TARGETING_COMPUTER)
                || name.equals(SALVAGE_SCANNER)
                || name.equals(EVASIVE_THRUSTERS);
    }

    /**
     * M7 part A: the Evasive thrusters' dash from design/player/systems/data.yaml: its distance over its
     * time, the invulnerable time from its start, the cooldown and the double-tap window.
     */
    static ThrusterSpec thrusters(Content content) {
        SystemsData.Dash dash = named(content.systems().utility(), SystemsData.Utility::name, EVASIVE_THRUSTERS)
                .dash()
                .orElseThrow(() -> new IllegalArgumentException(EVASIVE_THRUSTERS + " has no dash numbers"));
        return new ThrusterSpec(
                dash.distance(), dash.seconds(), dash.invulnerable(), dash.cooldown(), dash.doubleTap());
    }

    /** The Targeting computer's numbers from design/player/systems/data.yaml. */
    static SystemsData.Targeting targeting(Content content) {
        return named(content.systems().utility(), SystemsData.Utility::name, TARGETING_COMPUTER)
                .targeting()
                .orElseThrow(() -> new IllegalArgumentException(TARGETING_COMPUTER + " has no targeting numbers"));
    }

    /** The Salvage scanner's credit bonus at {@code level} (1–2): the share added to salvage and hidden crates. */
    static double salvageBonus(Content content, int level) {
        return named(content.systems().utility(), SystemsData.Utility::name, SALVAGE_SCANNER)
                .salvage()
                .orElseThrow(() -> new IllegalArgumentException(SALVAGE_SCANNER + " has no salvage numbers"))
                .bonus()
                .get(level - 1);
    }

    /** The Pickup magnet at {@code level} (1–3) from design/player/systems/data.yaml. */
    static Magnet magnet(Content content, int level) {
        SystemsData.Magnet numbers = named(content.systems().utility(), SystemsData.Utility::name, PICKUP_MAGNET)
                .magnet()
                .orElseThrow(() -> new IllegalArgumentException(PICKUP_MAGNET + " has no magnet numbers"));
        return new Magnet(numbers.radius().get(level - 1), numbers.pull().get(level - 1));
    }

    /**
     * Rook in the escort slot (design/player/wingmen) with his gun {@code gunId} at {@code level}
     * (1–5), on {@code side}, starting the level with {@code armour}: his gun is the base weapon's
     * pattern at that level with each projectile's damage scaled, fired from his one muzzle at the
     * nose (a pod weapon's pattern as the pod on his side has it, so the Missiles launch outward);
     * no overdrive.
     */
    static WingmanSpec wingman(Content content, String gunId, int level, WingmanSpec.Side side, double armour) {
        WingmenData data = content.wingmen();
        WingmenData.Rook rook = data.rook();
        WingmenData.Gun gun = data.guns().gun(gunId);
        WeaponData base = content.weapon(gun.base());
        if (level < 1 || level > base.levels().size()) {
            throw new IllegalArgumentException(
                    gunId + ": level " + level + " outside 1.." + base.levels().size());
        }
        WeaponSpec pattern = WeaponRules.weapon(content, Armament.Slot.FRONT, gun.base(), level);
        Point front = content.ship().mounts().front();
        double shipCentre = content.ship().size() / 2;
        double mirror = side == WingmanSpec.Side.LEFT && base.pod().orElse(false) ? -1 : 1;
        double noseX = rook.muzzle().x() - rook.size().width() / 2;
        double noseY = rook.size().height() / 2 - rook.muzzle().y();
        List<WeaponSpec.Muzzle> muzzles = pattern.muzzles().stream()
                .map(muzzle -> new WeaponSpec.Muzzle(
                        noseX + mirror * (muzzle.dx() - (front.x() - shipCentre)), noseY, mirror * muzzle.angle()))
                .toList();
        WingmenData.Formations formations = rook.formations();
        WingmenData.Dodge dodge = rook.dodge();
        double lowArmour = data.barks()
                .bark(WingmenData.ROOK_ARMOUR)
                .flatMap(WingmenData.Bark::below)
                .orElseThrow();
        return new WingmanSpec(
                side,
                armour,
                new WingmanSpec.Craft(
                        rook.size().width(),
                        rook.armour(),
                        SimSpecs.hitbox(rook.hitbox()),
                        rook.speed(),
                        rook.accelerationSeconds(),
                        rook.minDistance(),
                        rook.edgeGap(),
                        rook.ramDamage(),
                        lowArmour,
                        rook.eject().podSpeed(),
                        content.ship().bankChangeSteps() / ShipSpec.HARD_BANK),
                new WingmanSpec.Ai(
                        offset(formations.wing()),
                        offset(formations.wide()),
                        offset(formations.trail()),
                        rook.glideSeconds(),
                        rook.swapSeconds(),
                        rook.flankDistance(),
                        rook.reactionSeconds(),
                        dodge.interval(),
                        dodge.lookAhead(),
                        dodge.clearance(),
                        dodge.step(),
                        dodge.reacts(),
                        Math.toRadians(rook.cone() / 2),
                        rook.range(),
                        rook.recentHitSeconds()),
                pattern.scaled(gun.scale(), muzzles));
    }

    private static WingmanSpec.Offset offset(Point slot) {
        return new WingmanSpec.Offset(slot.x(), slot.y());
    }

    /** Whether the simulation flies the special of this name; the Decoy Flares follow with the homing projectiles. */
    static boolean fliesSpecial(String name) {
        return name.equals(AIRSTRIKE)
                || name.equals(SMART_BOMB)
                || name.equals(EMP_BURST)
                || name.equals(ORBITAL_LANCE)
                || name.equals(SHIELD_OVERCHARGE);
    }

    /**
     * The special of this name with {@code charges} carried into the level (at most its most); it must
     * fly. M7 part A: a cooldown special (the Shield Overcharge) carries no charges, {@code charges} is
     * ignored: it is ready at the start and again its cooldown after each use, a special charge pickup
     * taking {@code cooldown_pickup} s off a running cooldown.
     */
    static SpecialSpec special(Content content, String name, int charges) {
        if (!fliesSpecial(name)) {
            throw new IllegalArgumentException("the special " + name + " does not fly yet");
        }
        SpecialsData specials = content.specials();
        SpecialsData.Special special = specials.specials().stream()
                .filter(item -> item.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no special '" + name + "'"));
        SpecialSpec.Effect effect = effect(specials, name);
        if (special.cooldownSpecial()) {
            return SpecialSpec.cooldown(
                    name, specials.inputBuffer(), special.cooldown().orElseThrow(), specials.cooldownPickup(), effect);
        }
        int most = special.maxCharges().orElseThrow();
        return new SpecialSpec(name, Math.min(charges, most), most, specials.inputBuffer(), effect);
    }

    /** What the special of this name does, from its block in design/player/specials/data.yaml. */
    private static SpecialSpec.Effect effect(SpecialsData specials, String name) {
        return switch (name) {
            case SMART_BOMB -> {
                SpecialsData.SmartBomb bomb = specials.smartBomb();
                yield new SmartBombSpec(
                        bomb.ring(),
                        bomb.damage().all(),
                        bomb.damage().bossPart(),
                        bomb.invulnerable(),
                        bomb.repeat(),
                        bomb.flash().seconds(),
                        bomb.flash().opacity(),
                        bomb.flash().fade());
            }
            case EMP_BURST -> {
                // M7 part A: the ring, the machine's and the Vrell unit's stun, the repeat.
                SpecialsData.Emp emp = specials.emp();
                yield new EmpSpec(emp.ring(), emp.stun().machine(), emp.stun().vrell(), emp.repeat());
            }
            case ORBITAL_LANCE -> {
                // M7 part A: the lock, the column and its follow, the DPS and the boss parts' cap.
                SpecialsData.OrbitalLance lance = specials.orbitalLance();
                yield new LanceSpec(
                        lance.lock(),
                        lance.width(),
                        lance.seconds(),
                        lance.fade(),
                        lance.follow(),
                        lance.dps(),
                        lance.bossPart().dps(),
                        lance.bossPart().cap(),
                        lance.repeat());
            }
            case SHIELD_OVERCHARGE -> {
                SpecialsData.Overcharge overcharge = specials.overcharge();
                yield new OverchargeSpec(overcharge.factor(), overcharge.seconds(), overcharge.bleed());
            }
            default -> {
                SpecialsData.Airstrike data = specials.airstrike();
                yield new AirstrikeSpec(
                        data.delay(),
                        data.offset(),
                        data.speed(),
                        SimSpecs.hitbox(data.bomberSize()),
                        data.bombSpacing(),
                        data.fall(),
                        data.blastRadius(),
                        data.damage().ground(),
                        data.cap().ground(),
                        data.damage().air(),
                        data.cap().air(),
                        data.cap().bossPart());
            }
        };
    }

    static ShieldModel shield(Content content, ShieldData.Model model, Difficulty difficulty) {
        return new ShieldModel(
                model.capacity(),
                model.regen() * content.difficulty().shieldRegen().of(difficulty),
                model.delay(),
                content.shields().breakSeconds());
    }

    static Plating plating(ArmourData.Plating plating) {
        return new Plating(plating.max());
    }

    /** The hull boxes, from sprite pixels (y down from the top left) to offsets around the centre (y up). */
    private static Hull hull(ShipData ship) {
        double centre = ship.size() / 2;
        return new Hull(ship.hull().stream()
                .map(box -> new Hull.Part(
                        box.x() + box.width() / 2 - centre,
                        centre - (box.y() + box.height() / 2),
                        new Hitbox(box.width(), box.height())))
                .toList());
    }
}
