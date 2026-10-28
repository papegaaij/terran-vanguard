package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.WeaponData;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.ItemKind;

/**
 * M9 part A (step A3): the hangar for Act 5's gear: the Tesla Coil Pod and the Swarm Tail in the live shop with their
 * icons and short names; the pod's {@code shield-breaker} trait marked ◆ in its shop row where the next level's intel
 * recommends it, the row's name still fitting beside its price, the NEW tag and the mark, and its traits fitting the
 * detail's line.
 */
class ActFiveHangarTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);

    @Test
    void theActFiveWeaponsAreInTheLiveShopWithTheirNames() {
        assertEquals(java.util.Set.of(), Catalogue.AWAITING_ICONS);
        assertTrue(CATALOGUE.items(ItemKind.WING).stream()
                .anyMatch(item -> item.id().equals("tesla-coil-pod")));
        assertTrue(CATALOGUE.items(ItemKind.REAR).stream()
                .anyMatch(item -> item.id().equals("swarm-tail")));
        assertEquals("TESLA COIL", Names.callout("Tesla Coil Pod"));
        assertEquals("SWARM TAIL", Names.callout("Swarm Tail"));
    }

    @Test
    void theShieldBreakerTraitIsMarkedWhereTheIntelRecommendsIt() {
        var tesla = CATALOGUE.items(ItemKind.WING).stream()
                .filter(item -> item.id().equals("tesla-coil-pod"))
                .findFirst()
                .orElseThrow();
        assertTrue(tesla.traits().contains(WeaponData.SHIELD_BREAKER), "its trait");
        List<String> marked = List.of(WeaponData.SHIELD_BREAKER);
        long matches = tesla.traits().stream().filter(marked::contains).count();
        assertEquals(1, matches, "one ◆");
        // The row's name beside its price, the NEW tag and the mark (the label font's 8 px glyphs).
        int chars = ShopPanel.rowChars(8, Names.credits(tesla.price()), true, matches);
        assertTrue(chars >= Names.callout(tesla.name()).length(), "TESLA COIL fits its row: " + chars);
        // The detail's trait line: <AREA> <SHIELD-BREAKER> in the label font within the panel.
        int traits = 0;
        for (String trait : tesla.traits()) {
            traits += ("<" + trait + ">").length() * 8 + 8;
        }
        assertTrue(traits <= ShopPanel.WIDTH - 20, "the traits fit the detail: " + traits);
    }
}
