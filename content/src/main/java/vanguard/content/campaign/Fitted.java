package vanguard.content.campaign;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * An owned item at its upgrade level and (M7, the user's power dial of 2026-10-21, design/player#power-budget) its
 * power setting: a weapon flies and draws at its setting, 1 up to the bought level, which the hangar turns down and
 * up again for free. Every other item stays at full power.
 *
 * <p>In a save (format version 5) {@code power} is written only below full power; a missing one is full power, so a
 * save of version 4 and before reads as it is.
 *
 * @param item a weapon's slug, or a core part's name in its data file ({@code Mk I}, {@code Standard})
 * @param level the upgrade level, from 1
 * @param power the power setting, 1 up to {@code level}: the level the item flies and draws at
 */
@JsonDeserialize(builder = Fitted.Json.class)
public record Fitted(String item, int level, @JsonIgnore int power) {
    public Fitted {
        if (item == null || item.isBlank() || level < 1) {
            throw new IllegalArgumentException("invalid item " + item + " L" + level);
        }
        if (power < 1 || power > level) {
            throw new IllegalArgumentException("invalid power setting " + power + " of " + item + " L" + level);
        }
    }

    /** An item at full power: its setting is its level. */
    public Fitted(String item, int level) {
        this(item, level, level);
    }

    /** Whether it flies at its bought level. */
    @JsonIgnore
    public boolean fullPower() {
        return power == level;
    }

    /** This item at another power setting, 1 up to its level. */
    public Fitted withPower(int setting) {
        return new Fitted(item, level, setting);
    }

    /**
     * This item one level up: the setting rises with the level when it was at full power, otherwise it is kept (the
     * player turned it down on purpose).
     */
    public Fitted upgraded() {
        return new Fitted(item, level + 1, fullPower() ? level + 1 : power);
    }

    /** The setting as a save writes it: none at full power. */
    @JsonProperty("power")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer savedPower() {
        return fullPower() ? null : power;
    }

    /** Reads a saved item; a missing {@code power} is full power (format version 5, see {@link SaveFormat}). */
    @JsonPOJOBuilder(withPrefix = "")
    static final class Json {
        private String item;
        private Integer level;
        private Integer power;

        Json item(String value) {
            item = value;
            return this;
        }

        Json level(int value) {
            level = value;
            return this;
        }

        Json power(int value) {
            power = value;
            return this;
        }

        Fitted build() {
            if (item == null || level == null) {
                throw new IllegalArgumentException("an item needs item and level");
            }
            return new Fitted(item, level, power == null ? level : power);
        }
    }
}
