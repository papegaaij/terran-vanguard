package vanguard.content.databank;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import vanguard.content.Content;
import vanguard.content.EnemyData;
import vanguard.content.SpecialsData;
import vanguard.content.WeaponData;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.ItemKind;

/**
 * The generated weapon advice (step DB1, design/ui/databank, Weapon advice): one row per rule that applies to a unit,
 * from its data alone, each with its reason and the player's matching gear. The rules, each owned by its document: the
 * layer rules ({@code ground}, hardened, {@code high-air}, {@code sub} and the under-water factor), the enemy shields, the
 * reflect rule (a mirror), the EMP Burst's stun of a machine and of a Vrell unit, frontal armour, and every effective
 * trait no rule above covers; a unit with no effective trait and no rule gets {@code ANY WEAPON}. Nothing is hand-written.
 */
public final class Advice {
    /** The Airstrike, the Smart Bomb and the EMP Burst, by their slugs. */
    static final String AIRSTRIKE = "airstrike";

    static final String SMART_BOMB = "smart-bomb";
    static final String EMP_BURST = "emp-burst";

    private Advice() {}

    /**
     * A row: its {@code label} (a trait, a weapon kind or a special), its {@code reason}, {@code red} when the weapon
     * hurts the player (or a neutral craft must be spared), and the player's matching gear, the fitted first.
     */
    public record Row(String label, String reason, boolean red, List<GearRef> gear) {
        public Row {
            gear = List.copyOf(gear);
        }
    }

    /** An item of the player's that a row names: its kind and catalogue id, its name, level and whether it is fitted. */
    public record GearRef(ItemKind kind, String id, String name, int level, boolean fitted) {}

    /**
     * A rule before the player's gear is matched: what it says and which items answer it. An {@code anyFront} rule
     * matches the fitted front gun alone ({@code ANY WEAPON}, {@code ANY} on a shield).
     */
    record Rule(String label, String reason, boolean red, Predicate<Item> matches, boolean anyFront) {
        Rule(String label, String reason, boolean red, Predicate<Item> matches) {
            this(label, reason, red, matches, false);
        }
    }

    /** An item as a rule sees it: its kind, catalogue id, traits, and a weapon's {@code hits} and whether it falls. */
    record Item(ItemKind kind, String id, Set<String> traits, String hits, boolean falls) {
        boolean has(String trait) {
            return traits.contains(trait);
        }

        boolean special(String slug) {
            return kind == ItemKind.SPECIAL && SpecialsData.slug(id).equals(slug);
        }
    }

    /** The rules that apply to {@code unit} ({@code slug}), in the table's order; {@code beamDps} the fitted beam's. */
    static List<Rule> rules(
            Content content, String slug, EnemyData unit, Optional<Double> heat, Optional<Double> beamDps) {
        List<Rule> rules = new ArrayList<>();
        Set<String> covered = new LinkedHashSet<>();
        String layer = unit.layer();
        if (layer.equals("ground")) {
            covered.add("anti-ground");
            rules.add(
                    unit.armour().hardened()
                            ? new Rule(
                                    "ANTI-GROUND",
                                    "THE ONLY WAY IN; OTHER SHOTS GLANCE OFF",
                                    false,
                                    item -> item.has("anti-ground")
                                            || item.special(AIRSTRIKE)
                                            || item.special(SMART_BOMB))
                            : new Rule("ANTI-GROUND", "X2 ON EVERY PART", false, item -> item.has("anti-ground")));
        } else if (layer.equals("high-air")) {
            covered.add("homing");
            covered.add("beam");
            rules.add(new Rule("HOMING", "THE ONLY WEAPONS THAT REACH IT", false, item -> item.has("homing")));
            rules.add(new Rule("BEAM", "THE ONLY WEAPONS THAT REACH IT", false, item -> item.has("beam")));
        } else if (layer.equals("sub")) {
            covered.add("anti-sub");
            rules.add(new Rule(
                    "ANTI-SUB",
                    "THE ONLY WEAPON THAT REACHES IT",
                    false,
                    item -> item.has("anti-sub") || item.special(SMART_BOMB)));
        }
        if (unit.underwater().isPresent() && !layer.equals("sub")) {
            covered.add("anti-sub");
            rules.add(new Rule(
                    "ANTI-SUB",
                    "UNDER WATER THE OTHERS DO X"
                            + number(content.europa().underwater().damage()),
                    false,
                    item -> item.has("anti-sub")));
        }
        var shields = content.enemyBasis().shields();
        if (unit.shield().isPresent()) {
            covered.add("shield-breaker");
            rules.add(new Rule(
                    "SHIELD-BREAKER",
                    "EMPTIES IT, NO REFILL FOR " + number(shields.breakerHold()) + " S",
                    false,
                    item -> item.has("shield-breaker")));
            rules.add(new Rule("EMP BURST", "EMPTIES EVERY SHIELD", false, item -> item.special(EMP_BURST)));
            rules.add(new Rule(
                    "ANY",
                    "WEARS IT DOWN; IT REFILLS AFTER " + number(shields.delay()) + " S",
                    false,
                    item -> false,
                    true));
        }
        if (unit.mirror().isPresent()) {
            covered.addAll(List.of("shield-breaker", "beam", "side", "rear"));
            var mirror = unit.mirror().get();
            rules.add(new Rule("SHIELD-BREAKER", "SHATTERS IT", false, item -> item.has("shield-breaker")));
            rules.add(new Rule("EMP BURST", "SHATTERS IT", false, item -> item.special(EMP_BURST)));
            String breaks = heat.isPresent() && beamDps.isPresent() && beamDps.get() > 0
                    ? "HEATS IT: BREAKS IN " + number(Math.round(heat.get() / beamDps.get() * 10) / 10.0) + " S"
                    : "HEATS IT UNTIL IT BREAKS";
            rules.add(new Rule("BEAM", breaks, false, item -> item.has("beam")));
            rules.add(new Rule(
                    "SIDE / REAR", "THE FLANKS ARE OPEN", false, item -> item.has("side") || item.has("rear")));
            rules.add(new Rule("BOMBS / SHELLS", "FALL PAST IT", false, item -> item.falls()));
            double damage = content.enemyBasis().bulletDamage().stream()
                    .filter(bullet -> bullet.bullet().equals(mirror.bullet()))
                    .mapToDouble(vanguard.content.EnemyBasisData.BulletDamage::damage)
                    .findFirst()
                    .orElse(0);
            rules.add(new Rule(
                    "FORWARD",
                    "BOUNCES BACK AS A " + mirror.bullet().toUpperCase(Locale.ROOT) + " BULLET (" + number(damage)
                            + ")",
                    true,
                    item -> item.has("forward")));
        }
        var stun = content.specials().emp().stun();
        if (unit.isMachine()) {
            rules.add(new Rule(
                    "EMP BURST", "STUNS IT " + number(stun.machine()) + " S", false, item -> item.special(EMP_BURST)));
        } else if (unit.faction().equals("Vrell")) {
            rules.add(new Rule(
                    "EMP BURST", "STUNS IT " + number(stun.vrell()) + " S", false, item -> item.special(EMP_BURST)));
        }
        if (unit.armour().frontArc().isPresent() && unit.mirror().isEmpty()) {
            covered.add("side");
            covered.add("rear");
            rules.add(new Rule(
                    "SIDE / REAR", "ITS ARMOUR FACES FORWARD", false, item -> item.has("side") || item.has("rear")));
        }
        for (String trait : unit.traits()) {
            if (!covered.contains(trait)) {
                rules.add(new Rule(trait.toUpperCase(Locale.ROOT), "EFFECTIVE", false, item -> item.has(trait)));
            }
        }
        if (rules.isEmpty()) {
            rules.add(new Rule("ANY WEAPON", "NO TRAIT STANDS OUT", false, item -> false, true));
        }
        return rules;
    }

    /** {@code value} without a trailing {@code .0}: 3, 1.5. */
    static String number(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value)) : Double.toString(value);
    }

    /** An item of the catalogue as the rules see it: its traits, and a weapon's {@code hits} and fall. */
    static Item item(Content content, Catalogue.Item item) {
        if (item.kind().weapon()) {
            WeaponData weapon = content.weapon(item.id());
            return new Item(
                    item.kind(),
                    item.id(),
                    Set.copyOf(item.traits()),
                    weapon.hits(),
                    weapon.fall().isPresent() || weapon.flight().isPresent());
        }
        return new Item(item.kind(), item.id(), Set.copyOf(item.traits()), "", false);
    }
}
