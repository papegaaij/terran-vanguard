package vanguard.game.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.content.Expression;
import vanguard.game.render.Portraits;
import vanguard.game.render.Sprites;

/**
 * A briefing speaker's name plate and 144x144 portrait in an expression (design/story/characters,
 * tools/art/portraits.py: name and role as on the chosen briefing frames).
 *
 * @param name the name plate, {@code CMDR A. OKAFOR}
 * @param role the line under it, {@code CDF COMMAND}
 */
public record Speaker(String name, String role, TextureRegion portrait) {
    /** The speaker of a briefing page by its short name, as in the data files. */
    public static Speaker of(String speaker, Expression expression, Sprites sprites) {
        TextureRegion portrait = Portraits.briefing(sprites, speaker, expression);
        return new Speaker(plate(speaker), role(speaker), portrait);
    }

    /**
     * The role line under a briefing speaker's name plate by its short name, as in the briefing and
     * the hangar's intel panel: {@code Rook} is {@code AEGIS TWO}; the enemy channel's Vorne is {@code
     * JOVIAN ASCENDANCY} (design/ui/briefing, Act 5 additions), Harker (Level 29) {@code CERES WING}.
     */
    public static String role(String speaker) {
        return switch (speaker) {
            case "Okafor" -> "CDF COMMAND";
            case "Rook" -> "AEGIS TWO";
            case "Varga" -> "CDF INTELLIGENCE";
            case "Vorne" -> "JOVIAN ASCENDANCY";
            case "Harker" -> "CERES WING";
            default -> throw new IllegalArgumentException("no briefing name plate for " + speaker);
        };
    }

    /** The name plate of a briefing speaker by its short name: {@code Varga} is {@code DR. E. VARGA}. */
    public static String plate(String speaker) {
        return switch (speaker) {
            case "Okafor" -> "CMDR A. OKAFOR";
            case "Rook" -> "LT. K. TANAKA";
            case "Varga" -> "DR. E. VARGA";
            case "Vorne" -> "VORNE";
            case "Harker" -> "HARKER";
            default -> throw new IllegalArgumentException("no briefing name plate for " + speaker);
        };
    }
}
