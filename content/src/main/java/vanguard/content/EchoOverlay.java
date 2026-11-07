package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * M11 part G, step G2 (design/enemies/bosses, Boss rush; Level 49; m11g-gaps.md §1 #2, §4): a boss's <b>echo</b> is a
 * variant of its original's stat block, written as an overlay in the original's {@code data.yaml} ({@code boss.echo})
 * and read into a stat block of its own ({@link EnemyData.BossData#echo()}), so the original is read exactly as before
 * and its flights cannot move. The overlay is laid over a copy of the whole stat block (without the {@code echo} key):
 *
 * <ul>
 *   <li>a mapping merges into the mapping under it, key by key;
 *   <li>a mapping over a list of named items (each a mapping with a {@code name}, or each with a {@code play}: the
 *       Sovereign's pool) merges item by item by that key, in the list's order, an unknown key appended as a new item;
 *   <li>{@code null} removes the key (or the named item);
 *   <li>anything else (a list, a number, a text) replaces what is under it.
 * </ul>
 */
final class EchoOverlay {
    /** The overlay's key in a boss script. */
    static final String KEY = "echo";

    private static final List<String> ITEM_KEYS = List.of("name", "play");

    private EchoOverlay() {}

    /**
     * Moves {@code root}'s {@code boss.echo} overlay into a whole stat block of its own (the overlay laid over a copy of
     * {@code root} without it) under the same key; whether {@code root} had one.
     */
    static boolean apply(JsonNode root) {
        if (!(root instanceof ObjectNode enemy) || !(enemy.get("boss") instanceof ObjectNode boss)) {
            return false;
        }
        JsonNode overlay = boss.get(KEY);
        if (overlay == null) {
            return false;
        }
        boss.remove(KEY);
        if (!(overlay instanceof ObjectNode)) {
            throw new IllegalArgumentException("boss.echo: a mapping laid over the stat block");
        }
        ObjectNode echo = enemy.deepCopy();
        merge(echo, (ObjectNode) overlay, "boss.echo");
        boss.set(KEY, echo);
        return true;
    }

    private static void merge(ObjectNode base, ObjectNode overlay, String at) {
        for (Map.Entry<String, JsonNode> entry : overlay.properties()) {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            JsonNode under = base.get(key);
            if (value.isNull()) {
                base.remove(key);
            } else if (value instanceof ObjectNode map && under instanceof ObjectNode inner) {
                merge(inner, map, at + "." + key);
            } else if (value instanceof ObjectNode map && under instanceof ArrayNode list && named(list) != null) {
                base.set(key, mergeItems(list, map, named(list), at + "." + key));
            } else {
                base.set(key, value.deepCopy());
            }
        }
    }

    /** The key naming every item of {@code list} ({@link #ITEM_KEYS}); null if its items are not all named by one. */
    private static String named(ArrayNode list) {
        for (String key : ITEM_KEYS) {
            boolean all = !list.isEmpty();
            for (JsonNode item : list) {
                all &= item instanceof ObjectNode
                        && item.get(key) != null
                        && item.get(key).isString();
            }
            if (all) {
                return key;
            }
        }
        return null;
    }

    private static ArrayNode mergeItems(ArrayNode list, ObjectNode overlay, String key, String at) {
        ArrayNode out = list.arrayNode();
        List<String> seen = new ArrayList<>();
        for (JsonNode item : list) {
            String name = item.get(key).asString();
            seen.add(name);
            JsonNode change = overlay.get(name);
            if (change == null) {
                out.add(item.deepCopy());
            } else if (change.isNull()) {
                continue;
            } else if (change instanceof ObjectNode map) {
                ObjectNode copy = ((ObjectNode) item).deepCopy();
                merge(copy, map, at + "." + name);
                out.add(copy);
            } else {
                throw new IllegalArgumentException(at + "." + name + ": a mapping laid over the item, or null");
            }
        }
        for (Map.Entry<String, JsonNode> entry : overlay.properties()) {
            if (seen.contains(entry.getKey())) {
                continue;
            }
            if (!(entry.getValue() instanceof ObjectNode map)) {
                throw new IllegalArgumentException(at + "." + entry.getKey() + ": a new item is a mapping");
            }
            ObjectNode added = map.deepCopy();
            added.put(key, entry.getKey());
            out.add(added);
        }
        return out;
    }
}
