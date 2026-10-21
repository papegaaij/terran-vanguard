package vanguard.content.campaign;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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
 * version 4 save has none, so every item is at full power and the migration only raises the version.
 */
public final class SaveFormat {
    /** The version this game writes. */
    public static final int VERSION = 5;

    /**
     * What a migrated version 2 save that is past Level 07 gets: Rook's state as he joins, frozen as
     * M5 part A defined it (design/player/wingmen/data.yaml then), as a migration must be.
     */
    static final int ESCORT_JOINS = 8;

    static final String ESCORT_GUN = "autocannon";
    static final double ESCORT_ARMOUR = 80;
    static final String ESCORT_SIDE = "left";

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

    public static SaveGame read(String json) throws SaveException {
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

    private static boolean contains(ArrayNode flags, String flag) {
        for (JsonNode node : flags) {
            if (node.isString() && node.asString().equals(flag)) {
                return true;
            }
        }
        return false;
    }
}
