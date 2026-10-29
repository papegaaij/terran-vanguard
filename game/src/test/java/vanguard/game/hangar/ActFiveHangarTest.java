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

    /**
     * M9 part E (step E4; design/campaign Level 32, Sensor-suite detail): Level 32's two forks on the L3 wave strip at
     * their branches' first items, the richer channel marked (fork one's charge seam right, fork two's cooling ducts
     * left); a level without forks has none. Its fitted Airstrike or Orbital Lance reads {@code IN ROCK} (part A).
     */
    @Test
    void level32sForksAreOnTheWaveStripAndItsSpecialsAreInRock() {
        String deepCore = CONTENT.levelKey(32).orElseThrow();
        List<IntelPanel.Fork> forks = IntelPanel.forks(CONTENT, deepCore);
        assertEquals(2, forks.size());
        assertTrue(
                forks.get(0).t() >= 56 && forks.get(0).t() < 70,
                "fork one at " + forks.get(0).t());
        assertTrue(
                forks.get(1).t() >= 116 && forks.get(1).t() < 130,
                "fork two at " + forks.get(1).t());
        assertEquals(1, forks.get(0).richer(), "the charge seam (right) is the richer");
        assertEquals(0, forks.get(1).richer(), "the cooling ducts (left) are the richer");
        assertEquals(List.of(), IntelPanel.forks(CONTENT, CONTENT.levelKey(31).orElseThrow()));
        assertEquals(List.of(), IntelPanel.forks(CONTENT, ""));
        assertEquals(
                java.util.Optional.of("AIRSTRIKE: IN ROCK"),
                IntelPanel.limited(CONTENT, deepCore, java.util.Optional.of("Airstrike")));
        assertEquals(
                java.util.Optional.of("ORBITAL LANCE: IN ROCK"),
                IntelPanel.limited(CONTENT, deepCore, java.util.Optional.of("Orbital Lance")));
        assertEquals(
                java.util.Optional.empty(), IntelPanel.limited(CONTENT, deepCore, java.util.Optional.of("EMP Burst")));
    }

    /**
     * M9 part F (step F4; design/campaign Level 33, Sensor-suite detail at L3: "the rear and side entries on the threat
     * arrows"): Level 33's Void Leech waves from the rear and the sides are marked on the wave strip, its Talons' front
     * waves are not; a level without power drainers has none. The Rail Bunker and the leech have their sensor portraits.
     */
    @Test
    void level33sStripMarksTheLeechesRearAndSideEntries() {
        String field = CONTENT.levelKey(33).orElseThrow();
        List<IntelPanel.Ambush> ambushes = IntelPanel.ambushes(CONTENT, field);
        assertTrue(ambushes.stream().anyMatch(IntelPanel.Ambush::rear), "rear ambushes: " + ambushes);
        assertTrue(ambushes.stream().anyMatch(a -> !a.rear()), "side streams: " + ambushes);
        assertTrue(ambushes.stream().allMatch(a -> a.t() > 0 && a.t() < 180), "inside the level: " + ambushes);
        assertEquals(
                List.of(), IntelPanel.ambushes(CONTENT, CONTENT.levelKey(32).orElseThrow()));
        assertEquals(List.of(), IntelPanel.ambushes(CONTENT, ""));
        assertEquals(
                IntelPanel.Strip.NONE.forks(), IntelPanel.strip(CONTENT, field).forks());
        java.nio.file.Path intel =
                java.nio.file.Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "sprites", "intel");
        for (String slug : new String[] {"rail-bunker", "void-leech"}) {
            assertTrue(java.nio.file.Files.isRegularFile(intel.resolve(slug + ".png")), slug + "'s sensor portrait");
        }
    }
}
