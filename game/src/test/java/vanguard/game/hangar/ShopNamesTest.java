package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.ContentLoader;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.State;
import vanguard.content.campaign.ItemKind;

/**
 * Step GFX2 (the round 43 capture: the shop cut "REAR-GUARD DRONE BAY L" with its level, and the row's name
 * mid-word): every item of the catalogue fits the shop. The detail's heading always shows the level; a row's name fits
 * beside its status by its full name, the shop's abbreviations or its model, and only past a NEW tag and trait marks
 * is it cut, at a word if it can be.
 */
class ShopNamesTest {
    private static final Catalogue CATALOGUE = Catalogue.of(ContentLoader.fromClasspath());
    /** The fonts' advances: the 8x12 label font and the 10x20 body font. */
    private static final int LABEL_ADVANCE = 8;

    private static final int BODY_ADVANCE = 10;

    @Test
    void theRearGuardDroneBaysHeadingShowsItsNameAndLevel() {
        int chars = ShopPanel.headingChars(BODY_ADVANCE);
        assertEquals(23, chars, "236 px of body font up to the large icon");
        assertEquals("REAR-GUARD DRONE BAY L1", Names.heading("Rear-guard Drone Bay", "L1", chars));
        assertEquals("PULSE CANNON  L3", Names.heading("Pulse Cannon", "L3", chars), "two spaces where it fits");
        assertEquals("MK VI \"CHOIR CORE\"", Names.heading("Mk VI \"Choir Core\"", "", chars));
    }

    @Test
    void everyHeadingFitsAndShowsItsLevel() {
        int chars = ShopPanel.headingChars(BODY_ADVANCE);
        for (Item item : items()) {
            for (int level = 1; level <= item.maxLevel(); level++) {
                String suffix = ShopPanel.level(item, level);
                String heading = Names.heading(item.name(), suffix, chars);
                assertTrue(heading.length() <= chars, () -> heading + " is wider than " + chars);
                assertTrue(heading.endsWith(suffix), () -> heading + " lost its level " + suffix);
                assertTrue(
                        heading.startsWith(Names.of(item.name())),
                        () -> heading + ": the catalogue's names all fit the heading in full");
            }
        }
    }

    @Test
    void everyRowNameFitsBesideItsStatusWithoutACut() {
        List<String> cut = new java.util.ArrayList<>();
        for (Item item : items()) {
            for (Offer offer : offers(item, false)) {
                String status = ShopPanel.status(offer);
                int chars = ShopPanel.rowChars(LABEL_ADVANCE, status, false, 0);
                String name = Names.fit(item.name(), chars);
                assertTrue(name.length() <= chars, () -> name + " is wider than its row beside " + status);
                if (!uncut(item.name(), name)) {
                    cut.add(item.name() + " -> " + name + " (" + chars + ") beside " + status);
                }
            }
        }
        assertEquals(List.of(), cut, "names cut in their rows");
        int locked = ShopPanel.rowChars(LABEL_ADVANCE, "LOCKED L20", false, 0);
        assertEquals("REAR-GUARD BAY", Names.fit("Rear-guard Drone Bay", locked));
    }

    @Test
    void aRowNameFitsPastItsNewTagAndTraitMarksToo() {
        for (Item item : items()) {
            for (Offer offer : offers(item, true)) {
                String status = ShopPanel.status(offer);
                int chars = ShopPanel.rowChars(
                        LABEL_ADVANCE, status, true, item.traits().size());
                String name = Names.fit(item.name(), chars);
                assertTrue(name.length() <= chars, () -> name + " is wider than its row beside NEW " + status);
                assertFalse(name.isBlank(), () -> item.name() + " has no name beside NEW " + status);
            }
        }
        assertEquals("REAR-GUARD", Names.fit("Rear-guard Drone Bay", 12), "cut at a word");
    }

    /** Whether {@code shown} is {@code name} in full, abbreviated or as its model: not cut. */
    private static boolean uncut(String name, String shown) {
        return Names.forms(name).contains(shown);
    }

    private static List<Item> items() {
        return java.util.Arrays.stream(ItemKind.values())
                .flatMap(kind -> CATALOGUE.items(kind).stream())
                .toList();
    }

    /** The offers a row shows for {@code item}: fitted and owned at its top level, for sale, locked. */
    private static List<Offer> offers(Item item, boolean isNew) {
        int top = item.maxLevel();
        return List.of(
                new Offer(item, State.FITTED, top, Optional.empty(), false, isNew, List.of()),
                new Offer(item, State.OWNED, top, Optional.empty(), false, isNew, List.of()),
                new Offer(item, State.BUYABLE, 1, Optional.empty(), false, isNew, List.of()),
                new Offer(item, State.LOCKED, 1, Optional.empty(), false, isNew, List.of()));
    }
}
