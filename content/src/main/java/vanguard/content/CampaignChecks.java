package vanguard.content;

import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * The campaign's checks across files: the shop items and Rook's guns are available, the HP bases name what exists, a
 * level's act has its file.
 */
final class CampaignChecks extends ContentChecks {
    CampaignChecks(Content content, Problems problems) {
        super(content, problems);
    }

    /** The basis's {@code small_act_hp} names known {@code small} units (the user's Act 2 playtest, 2026-10-18). */
    void checkSmallActHp() {
        List<String> units = content.enemyBasis().smallActHp().units();
        for (int i = 0; i < units.size(); i++) {
            String field = "small_act_hp.units[" + i + "]";
            checkEnemyName(content.enemyBasis(), field, units.get(i));
            EnemyData enemy = content.enemies().get(units.get(i));
            if (enemy != null && enemy.tier() != Tier.SMALL) {
                problem(content.enemyBasis(), field, units.get(i) + " is not a small unit");
            }
        }
        for (int level : content.enemyBasis().smallActHp().levels()) {
            if (!content.enemyBasis().referenceDps().containsKey(level)) {
                problem(content.enemyBasis(), "small_act_hp.levels", "Level " + level + " has no reference DPS");
            }
        }
    }

    /**
     * M7 part E (step E5): the basis's {@code beam_era_hp} starts at a level with a reference DPS and lists size tiers
     * below {@code huge} (the set pieces keep the HP their arenas set).
     */
    void checkBeamEraHp() {
        EnemyBasisData.BeamEraHp era = content.enemyBasis().beamEraHp();
        if (!content.enemyBasis().referenceDps().containsKey(era.fromLevel())) {
            problem(
                    content.enemyBasis(),
                    "beam_era_hp.from_level",
                    "Level " + era.fromLevel() + " has no reference DPS");
        }
        if (era.tiers().isEmpty() || era.tiers().contains(Tier.HUGE)) {
            problem(content.enemyBasis(), "beam_era_hp.tiers", "one or more tiers below huge");
        }
    }

    /** A level's act has its data file, and the act's levels include the level. */
    void checkAct(String key, LevelData level) {
        ActData act = content.acts().get(Content.actDirectory(key));
        int number = Content.levelNumber(key);
        if (act == null) {
            problem(level, "level", "its act " + Content.actDirectory(key) + " has no data file");
        } else if (!act.levels().contains(number)) {
            problem(act, "levels", "does not include level " + number + " (" + key + ")");
        }
    }

    void checkItems() {
        ShieldData shields = content.shields();
        for (int i = 0; i < shields.models().size(); i++) {
            ShieldData.Model m = shields.models().get(i);
            checkItem(shields, "models[" + i + "]", i, m.price(), m.available());
        }
        ArmourData armour = content.armour();
        for (int i = 0; i < armour.plating().size(); i++) {
            ArmourData.Plating p = armour.plating().get(i);
            checkItem(armour, "plating[" + i + "]", i, p.price(), p.available());
        }
        GeneratorData generators = content.generators();
        for (int i = 0; i < generators.models().size(); i++) {
            GeneratorData.Model m = generators.models().get(i);
            checkItem(generators, "models[" + i + "]", i, m.price(), m.available());
        }
        SystemsData systems = content.systems();
        for (int i = 0; i < systems.engines().size(); i++) {
            SystemsData.Engine e = systems.engines().get(i);
            checkItem(systems, "engines[" + i + "]", i, e.price(), e.available());
        }
        for (int i = 0; i < systems.bays().extra().size(); i++) {
            checkAvailable(
                    systems,
                    "bays.extra[" + i + "].available",
                    systems.bays().extra().get(i).available());
        }
        for (int i = 0; i < systems.utility().size(); i++) {
            checkAvailable(
                    systems,
                    "utility[" + i + "].available",
                    systems.utility().get(i).available());
        }
    }

    /**
     * Rook's guns are based on player weapons the simulation flies, each with a price, so his
     * escort shop can derive theirs.
     */
    void checkWingmen() {
        WingmenData wingmen = content.wingmen();
        var guns = wingmen.guns().list();
        for (int i = 0; i < guns.size(); i++) {
            WingmenData.Gun gun = guns.get(i);
            String field = "guns.list[" + i + "]";
            checkAvailable(wingmen, field + ".available", gun.available());
            WeaponData weapon = content.weapons().get(gun.base());
            if (weapon == null) {
                problem(wingmen, field + ".base", "no player weapon '" + gun.base() + "'");
            } else if (!SimSpecs.flies(content, gun.base())) {
                problem(wingmen, field + ".base", "its weapon's hits '" + weapon.hits() + "' do not fly");
            }
        }
        checkAvailable(wingmen, "bulwark.available", wingmen.bulwark().available());
        if (wingmen.barks()
                .bark(WingmenData.ROOK_ARMOUR)
                .flatMap(WingmenData.Bark::below)
                .isEmpty()) {
            problem(wingmen, "barks.triggers", "no " + WingmenData.ROOK_ARMOUR + " trigger with its share below");
        }
        if (!content.voices().speakers().containsKey(wingmen.barks().speaker().toLowerCase(Locale.ROOT))) {
            problem(
                    wingmen,
                    "barks.speaker",
                    "no voice speaker '" + wingmen.barks().speaker() + "'");
        }
    }

    /** The first model of a list is the starter: free and available from the start. */
    private void checkItem(Object file, String field, int index, int price, String available) {
        checkAvailable(file, field + ".available", available);
        if (index == 0 && (price != 0 || !available.equals("start"))) {
            problem(file, field, "the first entry is the starter: price 0, available start");
        }
    }

    private void checkAvailable(Object file, String field, String available) {
        if (!content.player().knows(available)) {
            problem(
                    file,
                    field,
                    "unknown availability '" + available + "' (known: Lnn or "
                            + String.join(
                                    ", ",
                                    new TreeSet<>(
                                            content.player().availability().keySet())) + ")");
        }
    }
}
