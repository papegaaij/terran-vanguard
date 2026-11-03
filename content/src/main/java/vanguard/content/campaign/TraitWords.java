package vanguard.content.campaign;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The weapon traits in plain words (design/ui/hangar, Trait words; the user's Act 6 playthrough, 2026-11-03): for each
 * trait the levels use, its one-line meaning (the shop, the intel panel and the databank show it beside the tag) and
 * the launch warning when nothing fitted brings it (it says what the level needs, not the tag). The one place these
 * words live.
 */
public final class TraitWords {
    /** A trait: its {@code tag} (as in the data), its {@code meaning} and its launch {@code warning}. */
    public record Trait(String tag, String meaning, String warning) {}

    /** Every trait the levels use, in the design's order (design/README.md, Weapon traits). */
    public static final List<Trait> ALL = List.of(
            new Trait("forward", "HITS AHEAD OF THE SHIP", "NOTHING FITTED HITS AHEAD"),
            new Trait("spread", "COVERS A WIDE FORMATION AT ONCE", "NOTHING FITTED HITS A WIDE FORMATION"),
            new Trait("rear", "FIRES BEHIND THE SHIP", "NOTHING FITTED FIRES BEHIND THE SHIP"),
            new Trait("side", "FIRES OUT TO THE SIDES", "NOTHING FITTED FIRES TO THE SIDES"),
            new Trait("homing", "SHOTS SEEK THEIR TARGET, REACH HIGH AIR", "NOTHING FITTED SEEKS ITS TARGET"),
            new Trait("piercing", "SHOTS PASS THROUGH A LINE OF TARGETS", "NOTHING FITTED PIERCES A LINE OF TARGETS"),
            new Trait("beam", "A HELD BEAM, REACHES HIGH AIR", "NOTHING FITTED HOLDS A BEAM ON TARGET"),
            new Trait(
                    "anti-ground",
                    "BREAKS HARDENED GROUND TARGETS, ×2 ON GROUND",
                    "NOTHING FITTED BREAKS HARDENED GROUND TARGETS"),
            new Trait("anti-sub", "REACHES UNITS UNDER THE WATER", "NOTHING FITTED HITS UNITS UNDER WATER"),
            new Trait("area", "SPLASHES ONTO TARGETS CLOSE BY", "NOTHING FITTED SPLASHES A TIGHT CLUSTER"),
            new Trait("shield-breaker", "EMPTIES AN ENEMY SHIELD AT ONCE", "NOTHING FITTED BREAKS ENEMY SHIELDS"));

    /** The longest warning, in characters: the launch dialog is laid out for it (with two suggested names). */
    public static final int LONGEST_WARNING =
            ALL.stream().mapToInt(words -> words.warning().length()).max().orElse(0);

    private TraitWords() {}

    /** A trait's words, if it is one the levels use. */
    public static Optional<Trait> of(String trait) {
        return ALL.stream().filter(words -> words.tag().equals(trait)).findFirst();
    }

    /** A trait's meaning in plain words; empty for a trait with none. */
    public static String meaning(String trait) {
        return of(trait).map(Trait::meaning).orElse("");
    }

    /** The tag with its meaning, as the shop and the databank show it: {@code ANTI-GROUND: BREAKS HARDENED ...}. */
    public static String explained(String trait) {
        String tag = trait.toUpperCase(Locale.ROOT);
        String meaning = meaning(trait);
        return meaning.isEmpty() ? tag : tag + ": " + meaning;
    }

    /** The launch warning when nothing fitted brings the trait; an unknown trait gets {@code NO <TAG> FITTED}. */
    public static String warning(String trait) {
        return of(trait).map(Trait::warning).orElse("NO " + trait.toUpperCase(Locale.ROOT) + " FITTED");
    }
}
