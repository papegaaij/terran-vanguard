package vanguard.game.hangar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vanguard.content.campaign.LoadoutSlot;

/** How the hangar writes item and slot names and numbers in the bitmap fonts' upper case. */
public final class Names {
    private Names() {}

    /** An item's name in upper case: {@code Mk II "Arc"} is {@code MK II "ARC"}. */
    public static String of(String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    /** The most characters a module tile's name shows: 48 px of tile in the 8 px label font. */
    public static final int TILE_CHARS = 6;

    /** The tiles' abbreviations, longest first so that a name is not shortened twice. */
    private static final Map<String, String> TILE_WORDS = tileWords();

    /**
     * A weapon's or Rook's gun's short name for the schematic's callouts: the model without its
     * nickname, {@code MICRO-MSL} for the Micro-missile Pod; M7 part A: {@code DRONE BAY} for the Light
     * Drone Bay (its full name is wider than a wing callout); M8 part A: {@code HARPOON} for the Harpoon Torpedoes
     * (wider than the front callout).
     */
    public static String callout(String name) {
        return of(model(name))
                .replace("MICRO-MISSILE", "MICRO-MSL")
                .replace("LIGHT DRONE BAY", "DRONE BAY")
                .replace("HARPOON TORPEDOES", "HARPOON")
                .replace(" POD", "");
    }

    /**
     * A core part's, special's or utility module's name on its module tile, at most {@link
     * #TILE_CHARS} characters: the model without its nickname ({@code MK II} for Mk II "Arc"),
     * with the tiles' abbreviations ({@code CMP I}, {@code STRIKE}, {@code S-BOMB}, {@code
     * FLARES}, {@code TARGET}, {@code SALVGE}; M7 part A: {@code EMP}, {@code LANCE}, {@code OVRCHG}; M8 part A:
     * {@code SONAR}, {@code HULL}, {@code NANITE});
     * a longer name loses its spaces and is cut.
     */
    public static String tile(String name) {
        String text = abbreviated(name);
        if (text.length() <= TILE_CHARS) {
            return text;
        }
        String joined = text.replace(" ", "");
        return joined.substring(0, Math.min(TILE_CHARS, joined.length()));
    }

    /** The tile name before the fallback cut: the model with the tiles' abbreviations. */
    static String abbreviated(String name) {
        String text = of(model(name));
        for (Map.Entry<String, String> word : TILE_WORDS.entrySet()) {
            text = text.replace(word.getKey(), word.getValue());
        }
        return text;
    }

    private static Map<String, String> tileWords() {
        Map<String, String> words = new LinkedHashMap<>();
        // M7 part A (pending the user's review): the Act 3 specials' tile names.
        // M8 part A (pending the user's review): the Act 4 items' tile names.
        words.put("AUTO-REPAIR NANITES", "NANITE");
        words.put("PRESSURE HULL", "HULL");
        words.put("SONAR PULSE", "SONAR");
        words.put("SHIELD OVERCHARGE", "OVRCHG");
        words.put("ORBITAL LANCE", "LANCE");
        words.put("EMP BURST", "EMP");
        words.put("TARGETING COMPUTER", "TARGET");
        words.put("SALVAGE SCANNER", "SALVGE");
        words.put("PICKUP MAGNET", "MAGNET");
        words.put("SENSOR SUITE", "SENSOR");
        words.put("DECOY FLARES", "FLARES");
        words.put("SMART BOMB", "S-BOMB");
        words.put("COMPOSITE", "CMP");
        words.put("AIRSTRIKE", "STRIKE");
        words.put("STANDARD", "STD");
        return words;
    }

    /**
     * Step GFX2: the shop's abbreviations, longest first, for a name too wide for its row or the detail's heading
     * ({@link #fit}): the Act 3 names the round 43 capture saw cut.
     */
    private static final Map<String, String> SHOP_WORDS = shopWords();

    private static Map<String, String> shopWords() {
        Map<String, String> words = new LinkedHashMap<>();
        words.put("REAR-GUARD DRONE BAY", "REAR-GUARD BAY");
        words.put("SHIELD OVERCHARGE", "SHIELD OVRCHG");
        words.put("MICRO-MISSILE", "MICRO-MSL");
        words.put("PROXIMITY", "PROX.");
        words.put("LAUNCHER", "LNCHR");
        words.put("ASCENDANCY", "ASCEND.");
        return words;
    }

    /**
     * Step GFX2 (the round 43 capture: the shop cut "REAR-GUARD DRONE BAY" mid-word): an item's name in the shop, at
     * most {@code chars} characters: the full name if it fits, else with the shop's abbreviations ({@code REAR-GUARD
     * BAY}, {@code MICRO-MSL}, {@code SHIELD OVRCHG}), else the model without its nickname (abbreviated), else that cut
     * at its last word that fits, or mid-word as the last resort.
     */
    public static String fit(String name, int chars) {
        List<String> forms = forms(name);
        for (String form : forms) {
            if (form.length() <= chars) {
                return form;
            }
        }
        String model = forms.getLast();
        int space = model.lastIndexOf(' ', chars);
        return space > 0 ? model.substring(0, space) : model.substring(0, Math.max(0, chars));
    }

    /**
     * Step GFX2: an item's uncut shop names, widest first: its full name, with the shop's abbreviations, its model
     * (abbreviated).
     */
    static List<String> forms(String name) {
        return List.of(of(name), shopAbbreviated(of(name)), shopAbbreviated(of(model(name))));
    }

    /**
     * Step GFX2: the shop detail's heading, the item's name and its level ({@code L3}; empty for an item without
     * levels), at most {@code chars} characters: two spaces before the level, one when the name needs it, the name
     * {@link #fit} to what is left so that the level always shows.
     */
    public static String heading(String name, String level, int chars) {
        if (level.isEmpty()) {
            return fit(name, chars);
        }
        String full = of(name);
        if (full.length() + 2 + level.length() <= chars) {
            return full + "  " + level;
        }
        return fit(name, chars - 1 - level.length()) + " " + level;
    }

    private static String shopAbbreviated(String text) {
        String shortened = text;
        for (Map.Entry<String, String> word : SHOP_WORDS.entrySet()) {
            shortened = shortened.replace(word.getKey(), word.getValue());
        }
        return shortened;
    }

    /** The model without its nickname: {@code Mk II} for {@code Mk II "Arc"}. */
    private static String model(String name) {
        return name.contains("\"") ? name.substring(0, name.indexOf('"')).strip() : name;
    }

    /** Power in MW with at most one decimal: {@code 4.5 MW}, {@code 8 MW}. */
    public static String megawatts(double mw) {
        return number(mw) + " MW";
    }

    /** A number with at most one decimal. */
    public static String number(double value) {
        double rounded = Math.round(value * 10) / 10.0;
        return rounded == Math.rint(rounded)
                ? Long.toString((long) rounded)
                : String.format(Locale.ROOT, "%.1f", rounded);
    }

    /** Credits grouped by thousands with a space: {@code CR 12 450}. */
    public static String credits(long value) {
        return "CR " + grouped(value);
    }

    /** A number grouped by thousands with a space: {@code 12 450}. */
    public static String grouped(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }

    /** The slot's name on the schematic and in the shop's header. */
    public static String slot(LoadoutSlot slot) {
        return switch (slot) {
            case FRONT -> "FRONT";
            case REAR -> "REAR";
            case LEFT_WING -> "L WING";
            case RIGHT_WING -> "R WING";
            case GENERATOR -> "GENERATOR";
            case SHIELD -> "SHIELD";
            case ARMOUR -> "ARMOUR";
            case ENGINE -> "ENGINE";
            case SPECIAL -> "SPECIAL";
            case UTILITY_1 -> "UTILITY BAY 1";
            case UTILITY_2 -> "UTILITY BAY 2";
            case UTILITY_3 -> "UTILITY BAY 3";
            case ESCORT -> "ESCORT";
        };
    }

    /** The slot's three-letter code on its module tile. */
    public static String code(LoadoutSlot slot) {
        return switch (slot) {
            case GENERATOR -> "GEN";
            case SHIELD -> "SHD";
            case ARMOUR -> "ARM";
            case ENGINE -> "ENG";
            case SPECIAL -> "SPC";
            case UTILITY_1, UTILITY_2, UTILITY_3 -> "UTL";
            case ESCORT -> "ESC";
            case FRONT, REAR, LEFT_WING, RIGHT_WING -> slot(slot);
        };
    }
}
