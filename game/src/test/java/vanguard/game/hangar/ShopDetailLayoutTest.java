package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Flight;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.Choice;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.State;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;

/**
 * The user's Act 7 playthrough, 2026-11-13: "in the shop, if you go to shield, the description of the shield often does
 * not fit in the bottom-left pane. The 'draw' part often falls outside the panel." Every text line of the shop's detail
 * pane (the heading, the traits, the numbers, the {@code VS FITTED:} changes and the notes) lies inside the pane, clear
 * of the chips and of each other, for every item of the catalogue in every slot at every level and power setting,
 * against every fitted item at every level and setting; measured with the real fonts' glyphs (read without a GL
 * context, as {@code EscortBoxLayoutTest} does).
 */
class ShopDetailLayoutTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final BitmapFont.BitmapFontData LABEL = font(Fonts.LABEL_FILE);
    private static final BitmapFont.BitmapFontData BODY = font(Fonts.BODY_FILE);
    private static final Set<ItemKind> DIALLED = Set.of(ItemKind.FRONT, ItemKind.WING, ItemKind.REAR);

    /** The pane's text and chips run from here to {@link #RIGHT}. */
    private static final int INNER = ShopPanel.X + 10;

    private static final int RIGHT = ShopPanel.X + ShopPanel.WIDTH - 10;
    /** The detail ends above the message line. */
    private static final int BOTTOM = ShopPanel.MESSAGE_Y - 2;

    private static BitmapFont.BitmapFontData font(String file) {
        return new BitmapFont.BitmapFontData(
                new FileHandle(new File(System.getProperty("vanguard.assetsDir"), file)), false);
    }

    private static int width(BitmapFont.BitmapFontData font, String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += font.getGlyph(text.charAt(i)).xadvance;
        }
        return width;
    }

    /** A line's ink, from its top to its descenders and the one-pixel shadow. */
    private static Box ink(BitmapFont.BitmapFontData font, String text, float x, float y) {
        return new Box(x, y, width(font, text) + 1, font.capHeight - font.descent + 1);
    }

    private static boolean overlap(Box a, Box b) {
        return a.x() < b.x() + b.width()
                && b.x() < a.x() + a.width()
                && a.y() < b.y() + b.height()
                && b.y() < a.y() + a.height();
    }

    @Test
    void everyDetailLineFitsThePane() {
        Set<String> wrapped = new TreeSet<>();
        int checked = 0;
        for (LoadoutSlot slot : LoadoutSlot.values()) {
            List<Item> items = CATALOGUE.items(slot.kind());
            int topLevel = items.stream().mapToInt(Item::maxLevel).max().orElse(1);
            for (Item fitted : items) {
                for (int level = 1; level <= fitted.maxLevel(); level++) {
                    for (int power = DIALLED.contains(slot.kind()) ? 1 : level; power <= level; power++) {
                        for (int visit : List.of(1, 50)) {
                            for (int owned = 1; owned <= topLevel; owned++) {
                                Hangar hangar = hangar(slot, new Fitted(fitted.id(), level, power), visit, owned);
                                for (Offer offer : hangar.shop(slot)) {
                                    String what = offer.item().name() + " " + offer.state() + " L" + offer.power() + "/"
                                            + offer.level() + " in " + slot + " vs " + fitted.name() + " L" + power
                                            + "/" + level + " (visit " + visit + ")";
                                    assertFits(hangar, slot, offer, what, wrapped);
                                    if (offer.choice(Action.SELL).isPresent()) {
                                        assertFits(hangar, slot, boughtThisVisit(offer), what + ", bought", wrapped);
                                    }
                                    checked++;
                                }
                            }
                        }
                    }
                }
            }
        }
        assertTrue(checked > 10_000, "the catalogue in every slot: " + checked);
        assertEquals(
                Set.of(
                        "Bastion Shield",
                        "Mk I (SHIELD)",
                        "Mk II (SHIELD)",
                        "Mk III (SHIELD)",
                        "Mk IV (SHIELD)",
                        "Mk V (SHIELD)",
                        "Reflex Shield"),
                wrapped,
                "the items whose numbers take a second line: the shields' CAP, REGEN, DELAY and DRAW");
    }

    private static void assertFits(Hangar hangar, LoadoutSlot slot, Offer offer, String what, Set<String> wrapped) {
        Item item = offer.item();
        Optional<Double> cooldown = CATALOGUE.cooldown(item);
        List<String> groups = ShopPanel.numberGroups(item, offer.power(), cooldown);
        List<String> lines = ShopPanel.numberLines(item, offer.power(), cooldown);
        assertEquals(
                String.join(ShopPanel.GROUP_GAP, groups),
                String.join(ShopPanel.GROUP_GAP, lines),
                () -> what + ": the numbers break only between their groups");
        assertTrue(groups.getLast().startsWith("DRAW "), what);
        assertEquals(lines.size(), ShopPanel.numberRows(offer), () -> what + ": the chips follow the numbers' lines");
        if (lines.size() > 1) {
            wrapped.add(item.name() + (item.name().startsWith("Mk ") ? " (" + item.kind() + ")" : ""));
        }

        String heading = Names.heading(item.name(), ShopPanel.level(offer), ShopPanel.headingChars(Fonts.BODY_CELL));
        assertTrue(
                INNER + width(BODY, heading) <= RIGHT - ShopPanel.DETAIL_ICON_GAP,
                () -> what + ": the heading " + heading + " runs into the icon");
        float headingBottom = ShopPanel.DETAIL_Y + BODY.capHeight - BODY.descent + 1;

        List<ShopPanel.Text> texts = ShopPanel.detailTexts(
                offer,
                cooldown,
                hangar.fitted(slot),
                ShopPanel.fittedLevel(hangar, slot),
                Flight.flies(CONTENT, slot, new Fitted(item.id(), 1)),
                -1);
        List<Box> chips = new ArrayList<>();
        if (offer.state() != State.LOCKED) {
            for (int i = 0; i < offer.choices().size(); i++) {
                Box chip = ShopPanel.choiceChip(offer, i);
                String label = ShopPanel.choiceLabel(offer.choices().get(i));
                assertTrue(chip.x() >= INNER && chip.x() + chip.width() <= RIGHT, () -> what + ": " + label + " wide");
                assertTrue(chip.y() + chip.height() <= BOTTOM, () -> what + ": " + label + " covers the message");
                chips.add(chip);
            }
        }
        List<Box> drawn = new ArrayList<>();
        for (ShopPanel.Text text : texts) {
            Box box = ink(LABEL, text.text(), text.x(), text.y());
            String line = what + ": \"" + text.text() + "\" at " + box;
            if (box.x() < INNER || box.x() + box.width() - 1 > RIGHT) {
                fail(line + " passes the pane's side (" + INNER + "-" + RIGHT + ")");
            }
            if (box.y() < headingBottom || box.y() + box.height() > BOTTOM) {
                fail(line + " passes the pane's height (" + headingBottom + "-" + BOTTOM + ")");
            }
            for (Box other : drawn) {
                assertTrue(!overlap(box, other), () -> line + " overlaps the text at " + other);
            }
            for (Box chip : chips) {
                assertTrue(!overlap(box, chip), () -> line + " overlaps the chip at " + chip);
            }
            drawn.add(box);
        }
    }

    /** A hangar before {@code visit}, {@code fitted} in {@code slot}, the slot's other items owned at {@code owned}. */
    private static Hangar hangar(LoadoutSlot slot, Fitted fitted, int visit, int owned) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(RULES.starterLoadout());
        Map<ItemKind, List<Fitted>> inventory = new HashMap<>();
        Map<String, Integer> specials = new HashMap<>();
        List<Fitted> others = new ArrayList<>();
        for (Item item : CATALOGUE.items(slot.kind())) {
            if (!item.id().equals(fitted.item()) && !loadout.containsValue(new Fitted(item.id(), 1))) {
                others.add(new Fitted(item.id(), Math.min(owned, item.maxLevel())));
            }
        }
        SaveGame.EscortSlot escort = SaveGame.EscortSlot.NOT_HIRED;
        boolean hired = visit >= RULES.escort().joins();
        if (slot == LoadoutSlot.ESCORT) {
            List<Fitted> guns = new ArrayList<>(others);
            guns.add(fitted);
            escort = new SaveGame.EscortSlot(true, "left", Optional.of(fitted.item()), guns, 80);
        } else {
            loadout.put(slot, fitted);
            if (slot.kind() == ItemKind.SPECIAL) {
                for (Fitted other : others) {
                    if (CATALOGUE.item(ItemKind.SPECIAL, other.item()).cooldownSpecial()) {
                        inventory
                                .computeIfAbsent(ItemKind.SPECIAL, kind -> new ArrayList<>())
                                .add(new Fitted(other.item(), 1));
                    } else {
                        specials.put(other.item(), 1);
                    }
                }
            } else {
                inventory.put(slot.kind(), others);
            }
            if (hired) {
                String gun = RULES.escort().starterGun();
                escort = new SaveGame.EscortSlot(true, "left", Optional.of(gun), List.of(new Fitted(gun, 1)), 80);
            }
        }
        return new Hangar(
                CATALOGUE,
                Campaign.load(
                        RULES,
                        new SaveGame(
                                SaveFormat.VERSION,
                                Instant.EPOCH,
                                0,
                                Difficulty.MEDIUM,
                                slot == LoadoutSlot.ESCORT
                                        ? Math.max(visit, RULES.escort().joins())
                                        : visit,
                                100_000,
                                0,
                                loadout,
                                inventory,
                                List.of(),
                                specials,
                                60,
                                escort,
                                1,
                                Optional.empty(),
                                Map.of(),
                                List.of(),
                                List.of(),
                                new SaveGame.Stats(0, 0))));
    }

    /** The row as it is right after its purchase: its sale returns all it cost, and the detail says so. */
    private static Offer boughtThisVisit(Offer offer) {
        List<Choice> choices = offer.choices().stream()
                .map(choice -> choice.action() == Action.SELL
                        ? new Choice(
                                Action.SELL,
                                -offer.item().spent(offer.level()),
                                choice.load(),
                                choice.output(),
                                choice.fits(),
                                choice.refusal())
                        : choice)
                .toList();
        return new Offer(offer.item(), offer.state(), offer.level(), offer.owned(), true, false, choices);
    }
}
