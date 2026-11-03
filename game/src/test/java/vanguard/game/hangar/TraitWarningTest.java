package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Intel;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.content.campaign.TraitWords;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Dialog;
import vanguard.game.ui.Fonts;

/**
 * The launch warnings in plain words (the user's Act 6 playthrough, 2026-11-03, design/ui/hangar, Launch and Trait
 * words): each says what the level needs, not the tag, and suggests up to two items that would really clear it: owned
 * and unfitted first, then on sale, cheapest first.
 */
class TraitWarningTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final String LEVEL_36 = "act-6-jovian-storm/level-36-cloud-divers";

    private static Hangar hangar(
            int level,
            Map<LoadoutSlot, Fitted> loadout,
            Map<ItemKind, List<Fitted>> inventory,
            Map<String, Integer> specials) {
        return new Hangar(
                CATALOGUE,
                Campaign.load(
                        RULES,
                        new SaveGame(
                                SaveFormat.VERSION,
                                Instant.EPOCH,
                                0,
                                Difficulty.MEDIUM,
                                level,
                                0,
                                0,
                                loadout,
                                inventory,
                                List.of(),
                                specials,
                                60,
                                Optional.empty(),
                                Map.of(),
                                List.of(),
                                List.of(),
                                new SaveGame.Stats(0, 0))));
    }

    /** The starter loadout without its front gun, plus {@code fitted}. */
    private static Map<LoadoutSlot, Fitted> noFront(Map<LoadoutSlot, Fitted> fitted) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(RULES.starterLoadout());
        loadout.remove(LoadoutSlot.FRONT);
        loadout.putAll(fitted);
        return loadout;
    }

    @Test
    void everyTraitTheLevelsUseHasItsWords() {
        for (int number = 1; number <= 50; number++) {
            Optional<String> key = CONTENT.levelKey(number);
            if (key.isEmpty()) {
                continue;
            }
            var profile = CONTENT.level(key.get()).threatProfile();
            for (String trait : profile.traits()) {
                assertTrue(TraitWords.of(trait).isPresent(), key.get() + ": " + trait);
            }
            for (String trait : profile.requiredTraits()) {
                assertTrue(TraitWords.of(trait).isPresent(), key.get() + ": required " + trait);
            }
        }
        for (ItemKind kind : List.of(ItemKind.FRONT, ItemKind.WING, ItemKind.REAR)) {
            CATALOGUE.items(kind).forEach(item -> item.traits()
                    .forEach(trait -> assertTrue(TraitWords.of(trait).isPresent(), item.id() + ": " + trait)));
        }
    }

    @Test
    void theWarningsSayWhatTheLevelNeeds() {
        assertEquals("NOTHING FITTED BREAKS HARDENED GROUND TARGETS", HangarState.traitWarning("anti-ground"));
        assertEquals("NOTHING FITTED HITS A WIDE FORMATION", HangarState.traitWarning("spread"));
        assertEquals("NOTHING FITTED HITS AHEAD", HangarState.traitWarning("forward"));
        assertEquals("NOTHING FITTED BREAKS ENEMY SHIELDS", HangarState.traitWarning("shield-breaker"));
        assertEquals("ANTI-GROUND: BREAKS HARDENED GROUND TARGETS, ×2 ON GROUND", TraitWords.explained("anti-ground"));
    }

    /** The longest warning with the two longest names fits one line of the launch dialog, inside the screen. */
    @Test
    void theLongestWarningWithTwoSuggestionsFitsOneDialogLine() {
        List<Item> longest = java.util.stream.Stream.of(ItemKind.FRONT, ItemKind.WING, ItemKind.REAR, ItemKind.SPECIAL)
                .flatMap(kind -> CATALOGUE.items(kind).stream())
                .sorted(Comparator.comparingInt((Item item) -> item.name().length())
                        .reversed())
                .limit(2)
                .toList();
        String widest = TraitWords.ALL.stream()
                .map(words -> HangarState.traitWarning(words.tag(), longest))
                .max(Comparator.comparingInt(String::length))
                .orElseThrow();
        assertTrue(widest.length() <= Dialog.DETAIL_CHARS, widest);
        Dialog dialog = HangarState.launchDialog(List.of(widest, widest, widest, widest, "ARMOUR BELOW 50 %"));
        assertEquals(5, dialog.detailLines().size(), "one line each");
        assertTrue(dialog.width() <= PixelScreen.WIDTH - 2 * Fonts.LABEL_CELL, "inside the screen: " + dialog.width());
        assertTrue(dialog.height() <= PixelScreen.HEIGHT - 40, "inside the screen: " + dialog.height());
    }

    @Test
    void ownedItemsComeFirstThenTheCheapestOnSale() {
        // Level 36, no front gun: owned and unfitted, the Ion Beam (front) and the Tesla Coil Pod both cover forward.
        Hangar owned = hangar(
                36,
                noFront(Map.of()),
                Map.of(
                        ItemKind.FRONT,
                        List.of(new Fitted("ion-beam", 1)),
                        ItemKind.WING,
                        List.of(new Fitted("tesla-coil-pod", 1))),
                Map.of());
        List<Item> suggested = owned.suggestions(Hangar.FORWARD, false);
        assertEquals(
                List.of("ion-beam", "tesla-coil-pod").stream()
                        .sorted(Comparator.comparingInt(id -> price(id)))
                        .toList(),
                suggested.stream().map(Item::id).toList(),
                "both owned, the cheaper first");

        // One owned: it leads, then the cheapest forward item on sale.
        Hangar one =
                hangar(36, noFront(Map.of()), Map.of(ItemKind.FRONT, List.of(new Fitted("ion-beam", 1))), Map.of());
        List<Item> mixed = one.suggestions(Hangar.FORWARD, false);
        assertEquals("ion-beam", mixed.getFirst().id(), "the owned one first, though dearer");
        Item cheapest = java.util.stream.Stream.of(ItemKind.FRONT, ItemKind.WING, ItemKind.REAR)
                .flatMap(kind -> CATALOGUE.items(kind).stream())
                .filter(item -> item.price() > 0 && one.available(item) && Hangar.coversForward(item))
                .filter(item -> !item.id().equals("ion-beam"))
                .min(Comparator.comparingInt(Item::price).thenComparing(Item::name))
                .orElseThrow();
        assertEquals(cheapest.id(), mixed.get(1).id(), "then the cheapest on sale");
        assertTrue(cheapest.price() < price("ion-beam"), "the test needs a cheaper one on sale");
    }

    @Test
    void theLaunchWarningNamesTheSuggestions() {
        Hangar owned =
                hangar(36, noFront(Map.of()), Map.of(ItemKind.FRONT, List.of(new Fitted("ion-beam", 1))), Map.of());
        HangarState state = new HangarState(owned, true);
        List<String> warnings =
                state.launchWarnings(Optional.of(Intel.of(CONTENT, LEVEL_36, Intel.Field.TRAITS.sensor())));
        String forward = warnings.stream()
                .filter(line -> line.startsWith("NOTHING FITTED HITS AHEAD"))
                .findFirst()
                .orElseThrow();
        assertTrue(forward.startsWith("NOTHING FITTED HITS AHEAD — TRY ION BEAM, "), forward);
    }

    /** Every suggestion, fitted, clears its warning: at a level of each act, for each trait, required or not. */
    @Test
    void aSuggestionAlwaysCoversItsTrait() {
        for (int level : List.of(3, 9, 12, 22, 30, 36, 42)) {
            Hangar bare = hangar(level, noFront(Map.of()), Map.of(), Map.of());
            for (TraitWords.Trait words : TraitWords.ALL) {
                for (boolean required : List.of(true, false)) {
                    List<Item> suggested = bare.suggestions(words.tag(), required);
                    assertTrue(suggested.size() <= Hangar.SUGGESTIONS);
                    for (Item item : suggested) {
                        Map<LoadoutSlot, Fitted> fitted = new EnumMap<>(LoadoutSlot.class);
                        Map<String, Integer> charges = Map.of();
                        switch (item.kind()) {
                            case FRONT -> fitted.put(LoadoutSlot.FRONT, new Fitted(item.id(), 1));
                            case WING -> fitted.put(LoadoutSlot.LEFT_WING, new Fitted(item.id(), 1));
                            case REAR -> fitted.put(LoadoutSlot.REAR, new Fitted(item.id(), 1));
                            case SPECIAL -> {
                                assertTrue(required, "a special only for a required trait: " + item.id());
                                fitted.put(LoadoutSlot.SPECIAL, new Fitted(item.id(), 1));
                                charges = Map.of(item.id(), 1);
                            }
                            default -> throw new AssertionError(item.kind() + " " + item.id());
                        }
                        Hangar with = hangar(level, noFront(fitted), Map.of(), charges);
                        assertTrue(
                                required
                                        ? with.sourceTraits().contains(words.tag())
                                        : with.fittedTraits().contains(words.tag()),
                                "L" + level + " " + words.tag() + (required ? " required: " : ": ") + item.id());
                    }
                }
            }
        }
    }

    private static int price(String id) {
        for (ItemKind kind : List.of(ItemKind.FRONT, ItemKind.WING, ItemKind.REAR)) {
            for (Item item : CATALOGUE.items(kind)) {
                if (item.id().equals(id)) {
                    return item.price();
                }
            }
        }
        throw new AssertionError(id);
    }
}
