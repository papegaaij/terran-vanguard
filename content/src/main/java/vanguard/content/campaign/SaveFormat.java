package vanguard.content.campaign;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vanguard.content.Difficulty;

/**
 * The save file format (design/systems/saves): a {@link SaveGame} as a JSON document with a
 * {@code version} field. A save of this game's version is read as it is; an older version is
 * migrated first; a newer or unknown version, a missing or unknown field and an invalid value are
 * rejected, so a save is never half read.
 *
 * <p>Versions: 1 (M3 part B1); 2 (M4 part G) adds {@code stats.levels}, each won level's banked
 * credits and kills for the act summary. A version 1 save loads with none recorded. 3 (M5 part A)
 * adds {@code escort}, Rook in the escort slot; a version 2 save gets the new campaign's (Rook not
 * hired), or Rook hired with the free Autocannon at L1, full armour, on the left, when its next
 * level is 8 or later (design/systems/saves, Version 3). 4 (M7 part A, user decision Q1 = a) adds
 * {@code bays}, the utility bays bought (the third bay, from Act 3); a version 3 save gets 0, and (M7 part B, user
 * decision L.6 of round 36; a default pending the user's review in round 38) the story flags {@code act-intro:1}
 * and, from Level 08 on, {@code act-intro:2}: the release that wrote it had Acts 1–2 only, so their intros have
 * been shown and Act 3's has not ({@link CampaignRoute#onContinue}). 5 (M7, the user's power dial of 2026-10-21)
 * adds an item's optional {@code power}, its power setting ({@link Fitted#power}), written only below full power; a
 * version 4 save has none, so every item is at full power and the migration only raises the version. 6 (M8 part A,
 * design/player/wingmen) adds the escort slot's {@code flies} ({@code rook} or {@code bulwark}) and its {@code bulwark}
 * ({@code owned}, {@code armour}): a version 5 save (the v0.1 release, Acts 1–3) has Rook chosen and no Bulwark. 7
 * (step DB1, design/ui/databank) adds the {@code databank} record ({@link DatabankRecord}): a version 6 save gets every
 * unit, set piece and boss of the levels below its next level at its difficulty as met (that first level, no kills) and
 * read ({@link Roster}); a version 7 save without it is unreadable.
 */
public final class SaveFormat {
    /** The version this game writes. */
    public static final int VERSION = 7;

    /**
     * Step DB1: what the version 6 to 7 migration counts as met (design/ui/databank, The record in the save): per unit,
     * set piece and boss slug, the first level below {@code nextLevel} whose waves (and ground units, set pieces and
     * boss) hold it at {@code difficulty} (the waves the difficulty skips left out, the escort-gated ones in).
     */
    public interface Roster {
        Map<String, Integer> firstMet(int nextLevel, Difficulty difficulty);

        /**
         * The databank's other entries (gear, concepts) a campaign before {@code nextLevel} knows by its level alone, by
         * id: the migration marks them read with the units.
         */
        default java.util.List<String> known(int nextLevel) {
            return java.util.List.of();
        }
    }

    /** The roster of the design data on the classpath, built on the first version 6 save read. */
    private static final Supplier<Roster> CLASSPATH_ROSTER = new Supplier<>() {
        private volatile Roster roster;

        @Override
        public Roster get() {
            Roster built = roster;
            if (built == null) {
                synchronized (this) {
                    built = roster;
                    if (built == null) {
                        built = vanguard.content.databank.Databank.of(vanguard.content.ContentLoader.parseClasspath()
                                        .content())
                                .roster();
                        roster = built;
                    }
                }
            }
            return built;
        }
    };

    /**
     * What a migrated version 2 save that is past Level 07 gets: Rook's state as he joins, frozen as
     * M5 part A defined it (design/player/wingmen/data.yaml then), as a migration must be.
     */
    static final int ESCORT_JOINS = 8;

    static final String ESCORT_GUN = "autocannon";
    static final double ESCORT_ARMOUR = 80;
    static final String ESCORT_SIDE = "left";

    /** M8 part A: what a migrated version 5 save's escort slot gets: Rook chosen, no Bulwark at its full armour. */
    static final String ESCORT_FLIES = "rook";

    static final double BULWARK_ARMOUR = 120;

    /**
     * M7 part B: the first level of each act a version 3 save could have reached, frozen as the release that wrote
     * such saves had them (Act 1 from Level 01, Act 2 from Level 08): a migrated save whose next level is at least
     * an act's first has seen that act's intro.
     */
    static final int[] RELEASED_ACT_FIRST_LEVELS = {1, 8};

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .build();

    private SaveFormat() {}

    public static String write(SaveGame save) {
        return MAPPER.writeValueAsString(save);
    }

    /** Reads a save; a version 6 save's databank comes from the design data on the classpath ({@link Roster}). */
    public static SaveGame read(String json) throws SaveException {
        return read(json, Optional.empty());
    }

    /** Reads a save, a version 6 save's databank from {@code roster}. */
    public static SaveGame read(String json, Roster roster) throws SaveException {
        return read(json, Optional.of(roster));
    }

    private static SaveGame read(String json, Optional<Roster> roster) throws SaveException {
        JsonNode tree;
        try {
            tree = MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new SaveException("not a save file: " + e.getOriginalMessage());
        }
        JsonNode version = tree == null ? null : tree.get("version");
        if (version == null || !version.isInt()) {
            throw new SaveException("no format version");
        }
        if (version.intValue() > VERSION) {
            throw new SaveException("saved by a newer version of the game (format " + version.intValue() + ")");
        }
        if (version.intValue() < 1) {
            throw new SaveException("unknown format version " + version.intValue());
        }
        if (version.intValue() == 1) {
            migrateFrom1(tree);
        }
        if (tree.get("version").intValue() == 2) {
            migrateFrom2(tree);
        }
        if (tree.get("version").intValue() == 3) {
            migrateFrom3(tree);
        }
        if (tree.get("version").intValue() == 4) {
            migrateFrom4(tree);
        }
        if (tree.get("version").intValue() == 5) {
            migrateFrom5(tree);
        }
        if (tree.get("version").intValue() == 6) {
            migrateFrom6(tree, roster.orElseGet(CLASSPATH_ROSTER));
        }
        try {
            return MAPPER.treeToValue(tree, SaveGame.class);
        } catch (JacksonException e) {
            throw new SaveException("invalid save: " + e.getOriginalMessage());
        }
    }

    /** Version 1 to 2: no level recorded in the stats yet. */
    private static void migrateFrom1(JsonNode tree) throws SaveException {
        if (!(tree instanceof ObjectNode save) || !(save.get("stats") instanceof ObjectNode stats)) {
            throw new SaveException("invalid save: no stats");
        }
        if (stats.has("levels")) {
            throw new SaveException("invalid save: a version 1 save has no stats.levels");
        }
        stats.putObject("levels");
        save.put("version", 2);
    }

    /** Version 2 to 3: the escort slot, with Rook hired when the save is past Level 07. */
    private static void migrateFrom2(JsonNode tree) throws SaveException {
        if (!(tree instanceof ObjectNode save) || save.has("escort")) {
            throw new SaveException("invalid save: a version 2 save has no escort");
        }
        JsonNode next = save.get("nextLevel");
        boolean hired = next != null && next.isInt() && next.intValue() >= ESCORT_JOINS;
        ObjectNode escort = save.putObject("escort");
        escort.put("hired", hired);
        escort.put("side", ESCORT_SIDE);
        if (hired) {
            escort.put("fitted", ESCORT_GUN);
            ObjectNode gun = escort.putArray("guns").addObject();
            gun.put("item", ESCORT_GUN);
            gun.put("level", 1);
        } else {
            escort.putNull("fitted");
            escort.putArray("guns");
        }
        escort.put("armour", ESCORT_ARMOUR);
        save.put("version", 3);
    }

    /**
     * Version 3 to 4 (M7 part A): no utility bay bought, which no save before Act 3 can have; M7 part B: the intros
     * of the released acts the save has reached are marked as shown ({@link #RELEASED_ACT_FIRST_LEVELS}).
     */
    private static void migrateFrom3(JsonNode tree) throws SaveException {
        if (!(tree instanceof ObjectNode save) || save.has("bays")) {
            throw new SaveException("invalid save: a version 3 save has no bays");
        }
        save.put("bays", 0);
        JsonNode next = save.get("nextLevel");
        if (save.get("storyFlags") instanceof ArrayNode flags && next != null && next.isInt()) {
            for (int act = 0; act < RELEASED_ACT_FIRST_LEVELS.length; act++) {
                String flag = "act-intro:" + (act + 1); // frozen, as Campaign.actIntroFlag writes it
                if (next.intValue() >= RELEASED_ACT_FIRST_LEVELS[act] && !contains(flags, flag)) {
                    flags.add(flag);
                }
            }
        }
        save.put("version", 4);
    }

    /**
     * Version 4 to 5: the power dial's setting is optional, a missing one full power, so no item changes.
     */
    private static void migrateFrom4(JsonNode tree) throws SaveException {
        if (!(tree instanceof ObjectNode save)) {
            throw new SaveException("invalid save: not an object");
        }
        save.put("version", 5);
    }

    /**
     * Version 5 to 6 (M8 part A): the escort slot chooses Rook and owns no Bulwark, which no save before Act 4 can
     * have ({@link #ESCORT_FLIES}, {@link #BULWARK_ARMOUR}).
     */
    private static void migrateFrom5(JsonNode tree) throws SaveException {
        if (!(tree instanceof ObjectNode save)
                || !(save.get("escort") instanceof ObjectNode escort)
                || escort.has("flies")
                || escort.has("bulwark")) {
            throw new SaveException("invalid save: a version 5 save has an escort without flies or a Bulwark");
        }
        escort.put("flies", ESCORT_FLIES);
        ObjectNode bulwark = escort.putObject("bulwark");
        bulwark.put("owned", false);
        bulwark.put("armour", BULWARK_ARMOUR);
        save.put("version", 6);
    }

    /**
     * Version 6 to 7 (step DB1): the databank record, every unit of the levels flown met at its first level with no
     * kills (the kills count from the update on) and read, so an updated campaign opens with no flood of {@code NEW}
     * tags ({@link Roster}).
     */
    private static void migrateFrom6(JsonNode tree, Roster roster) throws SaveException {
        if (!(tree instanceof ObjectNode save) || save.has("databank")) {
            throw new SaveException("invalid save: a version 6 save has no databank");
        }
        JsonNode next = save.get("nextLevel");
        JsonNode difficulty = save.get("difficulty");
        if (next == null || !next.isInt() || difficulty == null || !difficulty.isString()) {
            throw new SaveException("invalid save: no next level or difficulty to migrate the databank from");
        }
        Difficulty level;
        try {
            level = Difficulty.valueOf(difficulty.asString());
        } catch (IllegalArgumentException e) {
            throw new SaveException("invalid save: difficulty " + difficulty.asString());
        }
        ObjectNode databank = save.putObject("databank");
        ObjectNode met = databank.putObject("met");
        ArrayNode read = databank.putArray("read");
        new java.util.TreeMap<>(roster.firstMet(next.intValue(), level)).forEach((slug, first) -> {
            ObjectNode unit = met.putObject(slug);
            unit.put("first", first);
            unit.put("kills", 0);
            read.add(slug);
        });
        roster.known(next.intValue()).forEach(read::add);
        save.put("version", 7);
    }

    private static boolean contains(ArrayNode flags, String flag) {
        for (JsonNode node : flags) {
            if (node.isString() && node.asString().equals(flag)) {
                return true;
            }
        }
        return false;
    }
}
