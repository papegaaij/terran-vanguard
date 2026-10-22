package vanguard.game.hangar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.WingmenData;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Catalogue.Stat;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Flight;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Choice;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.Refusal;
import vanguard.content.campaign.Hangar.State;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Words;
import vanguard.sim.WingmanSpec;

/**
 * The hangar's shop drawer (left, design/ui/hangar): the rows for the selected slot (fitted, owned,
 * buyable with price and ◆ trait matches and the NEW tag, locked with their unlock level), each
 * with its icon on a glass row band, the scroll markers, the selected item with its large icon,
 * traits, numbers and the deltas against the fitted item, its choices, the last transaction's
 * message; the test-fire box below them is the {@link TestFirePanel}'s. The escort's shop (M5 part
 * A) lists Rook's guns and, last, the row of his side with its {@code LEFT} / {@code RIGHT} choice.
 * M7 part A: a utility slot's shop ends with its bay row ({@link HangarState#bayRow()}: the third
 * bay for sale with its {@code BUY} chip, or in the bought bay's own shop its {@code SELL} chip), and a
 * cooldown special's detail shows its cooldown. M8 part A: the escort's shop lists the Bulwark's row between Rook's
 * guns and the side row ({@link HangarState#bulwarkRow()}): its icon, its price, {@code OWNED}, {@code FITTED} or
 * {@code LOCKED L22}, and in its detail its numbers, what it does and its chips. The rows and chips double as the mouse's hit regions
 * ({@link HangarPointer}). The replay hangar's drawer (refit only, the user's correction of 2026-10-19) is
 * headed {@code REFIT} and lists only the owned items, or says the slot has none.
 */
final class ShopPanel {
    static final int X = 16;
    static final int Y = 52;
    static final int WIDTH = 284;
    static final int HEIGHT = 444;
    /** The last transaction's message: two lines from here, 12 px apart, above the test-fire box. */
    static final int MESSAGE_Y = Y + HEIGHT - 100;

    private static final int INNER = X + 10;
    private static final int RIGHT = X + WIDTH - 10;
    private static final int ROWS_Y = Y + 26;
    private static final int ROW = 22;
    /** A row's name starts right of its icon. */
    private static final int NAME_X = INNER + 20;

    private static final Color NEW_TAG = new Color(1, 0, 0.67f, 1);
    /** A locked item's icon is drawn this much darker. */
    private static final Color LOCKED_ICON = new Color(0.45f, 0.45f, 0.55f, 1);

    private final Glass glass;
    private final ItemIcons icons;
    private final Content content;
    private final Catalogue catalogue;

    ShopPanel(Glass glass, ItemIcons icons, Content content, Catalogue catalogue) {
        this.content = content;
        this.glass = glass;
        this.icons = icons;
        this.catalogue = catalogue;
    }

    void draw(SpriteBatch batch, HangarState state, List<String> markedTraits) {
        glass.panel(batch, X, Y, WIDTH, HEIGHT, 0.86f);
        glass.header(
                batch, (state.refitOnly() ? "REFIT - " : "SHOP - ") + Names.slot(state.slot()), INNER, RIGHT, Y + 10);
        List<Offer> rows = state.rows();
        int count = state.rowCount();
        boolean shop = state.focus() == HangarState.Focus.SHOP;
        if (count == 0 && state.refitOnly()) {
            glass.shadowed(batch, glass.fonts.label, "NOTHING OWNED FOR THIS SLOT", Glass.DIM, INNER, ROWS_Y);
        }
        for (int i = state.top(); i < Math.min(count, state.top() + HangarState.VISIBLE_ROWS); i++) {
            int y = ROWS_Y + (i - state.top()) * ROW;
            if (i < rows.size()) {
                drawRow(batch, rows.get(i), y, shop && i == state.row(), markedTraits);
            } else if (i == rows.size() && state.bulwarkRow().isPresent()) {
                drawBulwarkRow(batch, state, state.bulwarkRow().get(), y, shop && i == state.row());
            } else if (state.bayRow().isPresent()) {
                drawBayRow(batch, state.bayRow().get(), y, shop && i == state.row());
            } else {
                drawSideRow(batch, state, y, shop && i == state.row());
            }
            if (state.hovers(HangarState.Target.ROW, i) && !(shop && i == state.row())) {
                hover(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
            }
        }
        if (count > HangarState.VISIBLE_ROWS) {
            String more =
                    (state.top() + 1) + "-" + Math.min(count, state.top() + HangarState.VISIBLE_ROWS) + " OF " + count;
            int moreY = ROWS_Y + HangarState.VISIBLE_ROWS * ROW;
            glass.right(batch, glass.fonts.label, more, Glass.DIM, RIGHT, moreY);
            boolean below = state.top() + HangarState.VISIBLE_ROWS < count;
            glass.scrollMarkers(
                    batch, RIGHT - Fonts.width(glass.fonts.label, more) - 6, moreY - 2, state.top() > 0, below);
        }
        glass.header(batch, "SELECTED", INNER, RIGHT, DETAIL_Y - 16);
        state.selected().ifPresent(offer -> drawDetail(batch, state, offer, DETAIL_Y));
        if (state.sideSelected()) {
            drawSide(batch, state, DETAIL_Y);
        }
        boolean shopFocus = state.focus() == HangarState.Focus.SHOP;
        if (state.baySelected()) {
            drawBay(batch, state.bayRow().orElseThrow(), shopFocus, state.hovers(HangarState.Target.BAY, 0), DETAIL_Y);
        }
        if (state.bulwarkSelected()) {
            drawBulwark(batch, state, state.bulwarkRow().orElseThrow(), DETAIL_Y);
        }
        Optional<String> refused = shopFocus
                ? state.previewChoice().filter(choice -> !choice.allowed()).map(HangarState::refusal)
                : Optional.empty();
        if (shopFocus && state.baySelected()) {
            refused = state.bayRow().flatMap(Hangar.BayOffer::refusal).map(HangarState::bayRefusal);
        }
        if (shopFocus && state.bulwarkSelected()) {
            refused = state.bulwarkChoice().flatMap(HangarState.BulwarkChoice::refusal);
        }
        String message = refused.orElse(state.message());
        List<String> lines = Words.wrap(message, (WIDTH - 20) / Fonts.advance(glass.fonts.label));
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            Color colour = refused.isPresent() ? Glass.ALERT : Glass.CYAN;
            glass.shadowed(batch, glass.fonts.label, lines.get(i), colour, INNER, MESSAGE_Y + i * 12);
        }
    }

    private void drawRow(SpriteBatch batch, Offer offer, int y, boolean selected, List<String> markedTraits) {
        boolean locked = offer.state() == State.LOCKED;
        glass.row(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
        if (selected) {
            glass.selection(batch, X + 2, y - 5, WIDTH - 4, ROW - 2);
            glass.cursor(batch, glass.fonts.label, X + 4, y + 4);
        }
        drawIcon(batch, icons.of(offer.item()).small(), INNER - 2, y - 3, locked);
        String status = status(offer);
        glass.right(batch, glass.fonts.label, status, locked ? Glass.DIM : Glass.CYAN, RIGHT, y);
        float x = RIGHT - Fonts.width(glass.fonts.label, status) - 6;
        if (offer.isNew()) {
            x -= 30;
            glass.tag(batch, "NEW", NEW_TAG, x, y - 2, 28, 14);
        }
        long matches = 0;
        for (String trait : offer.item().traits()) {
            if (markedTraits.contains(trait)) {
                matches++;
            }
        }
        for (int i = 1; i <= matches; i++) {
            glass.diamond(batch, x - i * 10, y + 1);
        }
        Color colour = selected ? Glass.AMBER : locked ? Glass.DIM : Glass.WHITE;
        int chars = rowChars(Fonts.width(glass.fonts.label, "M"), status, offer.isNew(), matches);
        glass.shadowed(batch, glass.fonts.label, Names.fit(offer.item().name(), chars), colour, NAME_X, y);
    }

    /**
     * Step GFX2: the characters a row's name has, in a label font of {@code advance} px a glyph: from right of its
     * icon to the marks left of its {@code status} (and its {@code NEW} tag), {@code matches} trait marks 10 px each.
     */
    static int rowChars(int advance, String status, boolean isNew, long matches) {
        int x = RIGHT - status.length() * advance - 6 - (isNew ? 30 : 0);
        return Math.max(0, (int) ((x - matches * 10 - NAME_X - 4) / advance));
    }

    /** Step GFX2: the characters the detail's heading has, in a body font of {@code advance} px: up to the icon. */
    static int headingChars(int advance) {
        return (RIGHT - DETAIL_ICON_GAP - INNER) / advance;
    }

    /** The detail's heading keeps this far from the panel's right edge: the large icon (24 px) and a gap. */
    static final int DETAIL_ICON_GAP = 28;

    /** The escort's last row: Rook's side, {@code ROOK'S SIDE ... LEFT}. */
    private void drawSideRow(SpriteBatch batch, HangarState state, int y, boolean selected) {
        glass.row(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
        if (selected) {
            glass.selection(batch, X + 2, y - 5, WIDTH - 4, ROW - 2);
            glass.cursor(batch, glass.fonts.label, X + 4, y + 4);
        }
        boolean bulwark = state.bulwarkFliesNext();
        drawIcon(batch, (bulwark ? icons.bulwark() : icons.escort()).small(), INNER - 2, y - 3, false);
        glass.right(batch, glass.fonts.label, state.escortSide().name(), Glass.CYAN, RIGHT, y);
        glass.shadowed(batch, glass.fonts.label, side(bulwark), selected ? Glass.AMBER : Glass.WHITE, NAME_X, y);
    }

    /** The side row's detail: where Rook flies, and the {@code LEFT} / {@code RIGHT} choice. */
    private void drawSide(SpriteBatch batch, HangarState state, int y) {
        boolean bulwark = state.bulwarkFliesNext();
        glass.shadowed(batch, glass.fonts.body, side(bulwark), Glass.AMBER, INNER, y);
        glass.shadowed(
                batch,
                glass.fonts.label,
                bulwark ? "WHERE THE BULWARK FLIES BESIDE YOU." : "WHERE ROOK FLIES BESIDE YOU.",
                Glass.WHITE,
                INNER,
                y + 24);
        glass.shadowed(batch, glass.fonts.label, "FREE: LEFT / RIGHT SET IT.", Glass.LABEL, INNER, y + 38);
        boolean shop = state.focus() == HangarState.Focus.SHOP;
        for (WingmanSpec.Side side : WingmanSpec.Side.values()) {
            Box chip = sideChip(side);
            boolean on = state.escortSide() == side;
            glass.chip(
                    batch,
                    glass.fonts.label,
                    side.name(),
                    chip.x(),
                    chip.y(),
                    chip.width(),
                    chip.height(),
                    on && shop,
                    true);
            if (on && !shop) {
                glass.outline(batch, Glass.AMBER, chip.x(), chip.y(), chip.width(), chip.height());
            } else if (!on && state.hovers(HangarState.Target.SIDE, side.ordinal())) {
                hover(batch, chip.x(), chip.y(), chip.width(), chip.height());
            }
        }
    }

    /** The side row's name. */
    static final String SIDE = "ROOK'S SIDE";

    /** M8 part A: the side row's name while the Bulwark flies the next level. */
    static final String BULWARK_SIDE = "BULWARK'S SIDE";

    static String side(boolean bulwark) {
        return bulwark ? BULWARK_SIDE : SIDE;
    }

    /**
     * M8 part A: the escort's Bulwark row: its icon, its name, and its price, {@code OWNED}, {@code FITTED} (it flies
     * the next level) or {@code LOCKED L22}.
     */
    private void drawBulwarkRow(
            SpriteBatch batch, HangarState state, Hangar.BulwarkOffer line, int y, boolean selected) {
        boolean locked = line.state() == State.LOCKED;
        glass.row(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
        if (selected) {
            glass.selection(batch, X + 2, y - 5, WIDTH - 4, ROW - 2);
            glass.cursor(batch, glass.fonts.label, X + 4, y + 4);
        }
        drawIcon(batch, icons.bulwark().small(), INNER - 2, y - 3, locked);
        glass.right(
                batch,
                glass.fonts.label,
                bulwarkStatus(line, state.bulwarkFlies()),
                locked ? Glass.DIM : Glass.CYAN,
                RIGHT,
                y);
        glass.shadowed(
                batch,
                glass.fonts.label,
                BULWARK_NAME,
                selected ? Glass.AMBER : locked ? Glass.DIM : Glass.WHITE,
                NAME_X,
                y);
    }

    /**
     * M8 part A: the Bulwark's row name and detail heading (the design's Heavy drone "Bulwark", turned so that it fits
     * beside {@code LOCKED L22}).
     */
    static final String BULWARK_NAME = "BULWARK HEAVY DRONE";

    /** M8 part A: the Bulwark row's status: its price, {@code OWNED}, {@code FITTED} or {@code LOCKED L22}. */
    static String bulwarkStatus(Hangar.BulwarkOffer line, boolean flies) {
        return switch (line.state()) {
            case OWNED -> flies ? "FITTED" : "OWNED";
            case BUYABLE -> Names.credits(line.credits());
            case LOCKED -> String.format(Locale.ROOT, "LOCKED L%02d", line.unlock());
            case FITTED -> "FITTED";
        };
    }

    /**
     * M8 part A: the Bulwark row's detail: its numbers, what it does, a note (its unlock, the loaner while Rook is
     * missing, its repair cost) and its chips.
     */
    private void drawBulwark(SpriteBatch batch, HangarState state, Hangar.BulwarkOffer line, int y) {
        boolean locked = line.state() == State.LOCKED;
        TextureRegion icon = icons.bulwark().large();
        drawIcon(batch, icon, RIGHT - icon.getRegionWidth(), y - 6, locked);
        glass.shadowed(batch, glass.fonts.body, BULWARK_NAME, Glass.AMBER, INNER, y);
        WingmenData.Bulwark bulwark = content.wingmen().bulwark();
        glass.shadowed(batch, glass.fonts.label, bulwarkNumbers(bulwark), Glass.WHITE, INNER, y + 24);
        glass.shadowed(batch, glass.fonts.label, bulwarkDraws(bulwark), Glass.LABEL, INNER, y + 38);
        glass.shadowed(batch, glass.fonts.label, bulwarkNote(state, line, bulwark), Glass.LABEL, INNER, y + 50);
        boolean shop = state.focus() == HangarState.Focus.SHOP;
        Optional<HangarState.BulwarkChoice> selected = shop ? state.bulwarkChoice() : Optional.empty();
        List<HangarState.BulwarkChoice> choices = state.bulwarkChoices();
        for (int i = 0; i < choices.size(); i++) {
            HangarState.BulwarkChoice choice = choices.get(i);
            Box chip = bulwarkChip(choices, i);
            boolean on = selected.filter(c -> c.action() == choice.action()).isPresent();
            glass.chip(
                    batch,
                    glass.fonts.label,
                    bulwarkLabel(choice),
                    chip.x(),
                    chip.y(),
                    chip.width(),
                    chip.height(),
                    on && choice.allowed(),
                    choice.allowed());
            if (on && !choice.allowed()) {
                glass.outline(batch, Glass.AMBER, chip.x(), chip.y(), chip.width(), chip.height());
            } else if (!on && state.hovers(HangarState.Target.CHOICE, i)) {
                hover(batch, chip.x(), chip.y(), chip.width(), chip.height());
            }
        }
    }

    /** M8 part A: the Bulwark's numbers: {@code ARMOUR 120   DPS 14   DRAW 0 MW} (its own power, as Rook's). */
    static String bulwarkNumbers(WingmenData.Bulwark bulwark) {
        return "ARMOUR " + Names.number(bulwark.armour()) + "   DPS "
                + Names.number(bulwark.gun().dps()) + "   DRAW " + Names.megawatts(0);
    }

    /** M8 part A: what the Bulwark does: {@code DRAWS 30 % OF AIMED FIRE NEAR YOU}. */
    static String bulwarkDraws(WingmenData.Bulwark bulwark) {
        return "DRAWS " + Math.round(bulwark.draw().share() * 100) + " % OF AIMED FIRE NEAR YOU";
    }

    /**
     * M8 part A: the Bulwark's note: its unlock while locked; while Rook is missing who flies in his place (the owned
     * one, or the loaner while none is owned); once owned its repair cost; else that buying it fits it.
     */
    static String bulwarkNote(HangarState state, Hangar.BulwarkOffer line, WingmenData.Bulwark bulwark) {
        boolean owned = line.state() == State.OWNED;
        if (line.state() == State.LOCKED) {
            return String.format(Locale.ROOT, "IN THE SHOP BEFORE MISSION %02d", line.unlock());
        }
        if (state.hangar().rookMissing()) {
            return owned ? "ROOK IS MIA: IT FLIES FOR HIM" : "ROOK IS MIA: A LOANER FLIES FREE";
        }
        return owned ? "REPAIRS " + Names.credits(bulwark.repair()) + " A POINT" : "BOUGHT, IT FLIES IN ROOK'S PLACE";
    }

    /** M8 part A: a Bulwark choice's chip: {@code BUY 6 000}, {@code FIT}, {@code UNFIT}, {@code SELL 3 600}. */
    static String bulwarkLabel(HangarState.BulwarkChoice choice) {
        String credits = Names.grouped(Math.abs(choice.credits()));
        return switch (choice.action()) {
            case BUY -> "BUY " + credits;
            case SELL -> "SELL " + credits;
            case FIT -> "FIT";
            default -> "UNFIT";
        };
    }

    /** M8 part A: the Bulwark row's chips in one row, where the side row's chips are. */
    static Box bulwarkChip(List<HangarState.BulwarkChoice> choices, int index) {
        float x = INNER;
        for (int i = 0; i < index; i++) {
            x += chipWidth(bulwarkLabel(choices.get(i))) + 6;
        }
        return new Box(x, DETAIL_Y + 64, chipWidth(bulwarkLabel(choices.get(index))), 20);
    }

    /**
     * M7 part A: a utility slot's bay row: the bay's name with a bay glyph, its price while for sale or
     * {@code OWNED} once bought.
     */
    private void drawBayRow(SpriteBatch batch, Hangar.BayOffer line, int y, boolean selected) {
        glass.row(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
        if (selected) {
            glass.selection(batch, X + 2, y - 5, WIDTH - 4, ROW - 2);
            glass.cursor(batch, glass.fonts.label, X + 4, y + 4);
        }
        drawBayGlyph(batch, INNER - 2, y - 3, 16);
        boolean owned = line.state() == State.OWNED;
        glass.right(
                batch,
                glass.fonts.label,
                owned ? "OWNED" : Names.credits(line.bay().price()),
                Glass.CYAN,
                RIGHT,
                y);
        glass.shadowed(
                batch,
                glass.fonts.label,
                Names.slot(line.bay().slot()),
                selected ? Glass.AMBER : Glass.WHITE,
                NAME_X,
                y);
    }

    /** A bay's glyph (it has no icon of its own): a module tile's frame with its slot open, {@code size} px square. */
    private void drawBayGlyph(SpriteBatch batch, float x, float y, int size) {
        glass.outline(batch, Glass.CYAN, x + 1, y + 1, size - 2, size - 2);
        glass.fill(batch, Glass.CYAN, x + size / 2f - 1, y + 4, 2, size - 8);
        glass.fill(batch, Glass.CYAN, x + 4, y + size / 2f - 1, size - 8, 2);
    }

    /** M7 part A: the bay row's detail: what the bay is, what selling it asks, and its one chip. */
    private void drawBay(SpriteBatch batch, Hangar.BayOffer line, boolean shop, boolean hovered, int y) {
        drawBayGlyph(batch, RIGHT - 24, y - 6, 24);
        glass.shadowed(batch, glass.fonts.body, Names.slot(line.bay().slot()), Glass.AMBER, INNER, y);
        glass.shadowed(batch, glass.fonts.label, BAY_WHAT, Glass.WHITE, INNER, y + 24);
        glass.shadowed(
                batch, glass.fonts.label, line.state() == State.OWNED ? BAY_SELL : BAY_BUY, Glass.LABEL, INNER, y + 38);
        Box chip = bayChip(line);
        boolean allowed = line.allowed();
        glass.chip(
                batch,
                glass.fonts.label,
                bayLabel(line),
                chip.x(),
                chip.y(),
                chip.width(),
                chip.height(),
                shop && allowed,
                allowed);
        if (shop && !allowed) {
            glass.outline(batch, Glass.AMBER, chip.x(), chip.y(), chip.width(), chip.height());
        } else if (!shop && hovered) {
            hover(batch, chip.x(), chip.y(), chip.width(), chip.height());
        }
    }

    /** The bay row's detail lines. */
    static final String BAY_WHAT = "A THIRD SLOT FOR ANY MODULE.";

    static final String BAY_BUY = "ITS TILE SHOWS ONCE BOUGHT.";
    static final String BAY_SELL = "UNFIT ITS MODULE TO SELL IT.";

    /** The bay row's chip: {@code BUY 5 000} or {@code SELL 3 000}. */
    static String bayLabel(Hangar.BayOffer line) {
        return (HangarState.bayAction(line) == Hangar.Action.SELL ? "SELL " : "BUY ")
                + Names.grouped(Math.abs(line.credits()));
    }

    /** The bay row's one chip, where the side row's chips are. */
    static Box bayChip(Hangar.BayOffer line) {
        return new Box(INNER, DETAIL_Y + 64, chipWidth(bayLabel(line)), 20);
    }

    /** An item's icon with its top-left corner at (x, y), darkened while it is locked. */
    private static void drawIcon(SpriteBatch batch, TextureRegion icon, float x, float y, boolean locked) {
        if (locked) {
            batch.setColor(LOCKED_ICON);
        }
        batch.draw(icon, x, PixelScreen.HEIGHT - y - icon.getRegionHeight());
        batch.setColor(Color.WHITE);
    }

    /** Step GFX2: the detail heading's level, {@code L3}; empty for an item without levels. */
    static String level(Item item, int level) {
        return item.maxLevel() > 1 ? "L" + level : "";
    }

    /**
     * The power dial (2026-10-21): an owned item's level with its power setting, {@code L3/5} while it is turned
     * down below its bought level 5, else {@code L5}.
     */
    static String setting(Offer offer) {
        return setting(offer.power(), offer.level());
    }

    /** A power setting over a bought level: {@code L3/5}, or {@code L5} at full power. */
    static String setting(int power, int level) {
        return power < level ? "L" + power + "/" + level : "L" + level;
    }

    /** The detail heading's level: {@link #setting(Offer)}; empty for an item without levels. */
    static String level(Offer offer) {
        return offer.item().maxLevel() > 1 ? setting(offer) : "";
    }

    /**
     * A row's status on its right: {@code L3 FITTED}, {@code L2 OWNED}, the price or {@code LOCKED L20}; a weapon
     * turned down by the power dial shows its setting over its level, {@code L3/5 FITTED}.
     */
    static String status(Offer offer) {
        String level = offer.item().maxLevel() > 1 ? setting(offer) + " " : "";
        return switch (offer.state()) {
            case FITTED -> offer.item().kind() == ItemKind.SPECIAL ? "FITTED" : level + "FITTED";
            case OWNED -> level + "OWNED";
            case BUYABLE -> Names.credits(offer.item().price());
            case LOCKED ->
                String.format(Locale.ROOT, "LOCKED L%02d", offer.item().unlock());
        };
    }

    private void drawDetail(SpriteBatch batch, HangarState state, Offer offer, int y) {
        Item item = offer.item();
        TextureRegion icon = icons.of(item).large();
        drawIcon(batch, icon, RIGHT - icon.getRegionWidth(), y - 6, offer.state() == State.LOCKED);
        String heading = Names.heading(item.name(), level(offer), headingChars(Fonts.width(glass.fonts.body, "M")));
        glass.shadowed(batch, glass.fonts.body, heading, Glass.AMBER, INNER, y);
        int traitX = INNER;
        for (String trait : item.traits()) {
            String text = "<" + trait.toUpperCase(Locale.ROOT) + ">";
            glass.shadowed(batch, glass.fonts.label, text, Glass.AMBER, traitX, y + 24);
            traitX += Fonts.width(glass.fonts.label, text) + 8;
        }
        int line = statsY(item);
        // The power dial: a turned-down weapon's numbers are its setting's.
        Map<Stat, Double> stats = item.stats(offer.power());
        Optional<Item> fitted = state.hangar().fitted(state.slot());
        StringBuilder numbers = new StringBuilder();
        stats.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(stat -> numbers.append(
                        label(stat.getKey()))
                .append(' ')
                .append(Names.number(stat.getValue()))
                .append("   "));
        // M7 part A: a cooldown special's cooldown, where a charge special shows its most charges.
        catalogue.cooldown(item).ifPresent(seconds -> numbers.append("COOLDOWN ")
                .append(Names.number(seconds))
                .append(" S   "));
        numbers.append("DRAW ").append(Names.megawatts(item.draw(offer.power())));
        glass.shadowed(batch, glass.fonts.label, numbers.toString(), Glass.WHITE, INNER, line);
        List<Delta> deltas = offer.state() != State.FITTED && fitted.isPresent()
                ? deltas(item, offer.power(), fitted.get(), fittedLevel(state))
                : List.of();
        if (!deltas.isEmpty()) {
            drawDeltas(batch, deltas, line + 14);
        } else if (fullRefund(offer) && chipRows(offer) == 1) {
            // M7 part A (round 37, R.4 e): a sale of what this visit bought returns all it cost.
            glass.shadowed(batch, glass.fonts.label, FULL_REFUND, Glass.LABEL, INNER, line + 14);
        } else if (fullRefund(offer)) {
            // The power dial's two rows of chips take that line: the note's short form follows the last chip.
            Box note = refundNote(offer);
            glass.shadowed(batch, glass.fonts.label, SHORT_REFUND, Glass.LABEL, note.x(), note.y());
        }
        if (!flies(state.slot(), item)) {
            glass.shadowed(batch, glass.fonts.label, "NOT YET IN FLIGHT", Glass.DIM, INNER, line + 28);
        }
        if (offer.state() == State.LOCKED) {
            String unlock = String.format(Locale.ROOT, "IN THE SHOP BEFORE MISSION %02d", item.unlock());
            glass.shadowed(batch, glass.fonts.label, unlock, Glass.LABEL, INNER, line + 42);
            return;
        }
        drawChoices(batch, state, offer);
    }

    /** The level the fitted item flies at, its power setting: what the {@code VS FITTED:} line compares with. */
    private static int fittedLevel(HangarState state) {
        Fitted fitted = state.hangar().campaign().loadout().get(state.slot());
        return fitted == null ? 1 : fitted.power();
    }

    private boolean flies(LoadoutSlot slot, Item item) {
        return Flight.flies(content, slot, new Fitted(item.id(), 1));
    }

    /** M7 part A: what the detail says of a sale of an item this visit bought (its full price comes back). */
    static final String FULL_REFUND = "BOUGHT THIS VISIT: FULL REFUND";

    /** {@link #FULL_REFUND} beside the chips, when the power dial's two rows of chips take its line. */
    static final String SHORT_REFUND = "FULL REFUND";

    /** One stat's change against the fitted item, as the {@code VS FITTED:} line shows it. */
    record Delta(String text, boolean better) {}

    /** M7 part A: whether a row's sale returns its full price: it was bought during this visit and can be sold. */
    static boolean fullRefund(Offer offer) {
        return offer.bought() && offer.choice(Hangar.Action.SELL).isPresent();
    }

    /**
     * The stats that change against the fitted item (both have the stat, the change is not zero) and the
     * draw's change; M7 part A: empty when nothing changes, and the {@code VS FITTED:} line is then left
     * out (a cooldown special against a charge special shares no stat and draws the same).
     */
    static List<Delta> deltas(Item item, int level, Item fitted, int fittedLevel) {
        List<Delta> deltas = new ArrayList<>();
        Map<Stat, Double> mine = item.stats(level);
        Map<Stat, Double> theirs = fitted.stats(fittedLevel);
        for (Stat stat : Stat.values()) {
            if (mine.containsKey(stat) && theirs.containsKey(stat)) {
                delta(deltas, label(stat), mine.get(stat) - theirs.get(stat), stat.higherIsBetter());
            }
        }
        delta(deltas, "DRAW", item.draw(level) - fitted.draw(fittedLevel), false);
        return deltas;
    }

    private static void delta(List<Delta> deltas, String label, double change, boolean higherIsBetter) {
        if (Math.abs(change) >= 1e-9) {
            deltas.add(new Delta(
                    label + " " + (change > 0 ? "+" : "") + Names.number(change), (change > 0) == higherIsBetter));
        }
    }

    private void drawDeltas(SpriteBatch batch, List<Delta> deltas, int y) {
        float x = INNER;
        glass.shadowed(batch, glass.fonts.label, "VS FITTED:", Glass.LABEL, x, y);
        x += Fonts.width(glass.fonts.label, "VS FITTED:") + 8;
        for (Delta delta : deltas) {
            glass.shadowed(batch, glass.fonts.label, delta.text(), delta.better() ? Glass.GREEN : Glass.ALERT, x, y);
            x += Fonts.width(glass.fonts.label, delta.text()) + 10;
        }
    }

    private void drawChoices(SpriteBatch batch, HangarState state, Offer offer) {
        Optional<Choice> selected = state.focus() == HangarState.Focus.SHOP ? state.choice() : Optional.empty();
        List<Choice> choices = offer.choices();
        for (int i = 0; i < choices.size(); i++) {
            Choice choice = choices.get(i);
            Box chip = choiceChip(offer, i);
            boolean on = selected.filter(c -> c.action() == choice.action()).isPresent();
            glass.chip(
                    batch,
                    glass.fonts.label,
                    choiceLabel(choice),
                    chip.x(),
                    chip.y(),
                    chip.width(),
                    chip.height(),
                    on && choice.allowed(),
                    choice.allowed());
            if (on && !choice.allowed()) {
                glass.outline(batch, Glass.AMBER, chip.x(), chip.y(), chip.width(), chip.height());
            } else if (!on && state.hovers(HangarState.Target.CHOICE, i)) {
                hover(batch, chip.x(), chip.y(), chip.width(), chip.height());
            }
        }
    }

    /**
     * The hover look (design/ui/hangar, Mouse): a cyan outline, distinct from the selection's amber bar
     * and chips, on what the pointer rests on.
     */
    private void hover(SpriteBatch batch, float x, float y, float width, float height) {
        glass.outline(batch, Glass.CYAN, x, y, width, height);
    }

    /** The selected item's detail starts here, under its header. */
    static final int DETAIL_Y = ROWS_Y + HangarState.VISIBLE_ROWS * ROW + 32;

    /** The hit region of the list's {@code shown}-th shown row: its selection bar, the rows touching. */
    static Box row(int shown) {
        return new Box(X + 2, ROWS_Y - 5 + shown * ROW, WIDTH - 4, ROW);
    }

    /** The shown rows, where the wheel scrolls the list. */
    static Box list() {
        return new Box(X, ROWS_Y - 5, WIDTH, HangarState.VISIBLE_ROWS * ROW);
    }

    /** The y of the detail's numbers: under the name, and under the traits when it has some. */
    private static int statsY(Item item) {
        return DETAIL_Y + (item.traits().isEmpty() ? 24 : 40);
    }

    /**
     * A choice's chip under the detail of a row that is not locked, as wide as its label. The chips sit in one row;
     * when they do not fit it (a fitted weapon's power dial makes five), in two: the upgrade and the dial ({@code
     * PWR -}, {@code PWR +}) above, the rest below in the one row's place, so left / right still run through them in
     * reading order.
     */
    static Box choiceChip(Offer offer, int index) {
        int[] rows = chipRows(offer.choices());
        int count = rows.length == 0 ? 1 : rows[rows.length - 1] + 1;
        float x = INNER;
        for (int i = 0; i < index; i++) {
            x = rows[i + 1] == rows[i]
                    ? x + chipWidth(choiceLabel(offer.choices().get(i))) + 6
                    : INNER;
        }
        return new Box(
                x,
                statsY(offer.item()) + 44 - (count - 1 - rows[index]) * CHIP_ROW,
                chipWidth(choiceLabel(offer.choices().get(index))),
                20);
    }

    /** The second row of chips sits this far above the first. */
    static final int CHIP_ROW = 24;

    /** How many rows the row's chips take: 1, or 2 when they do not fit one. */
    static int chipRows(Offer offer) {
        int[] rows = chipRows(offer.choices());
        return rows.length == 0 ? 1 : rows[rows.length - 1] + 1;
    }

    /**
     * Each choice's row of chips, from 0: all 0 when they fit the drawer's width; otherwise the row breaks after the
     * power dial's {@code PWR +} (or, without a dial, before the first chip past the drawer's edge).
     */
    static int[] chipRows(List<Choice> choices) {
        int[] rows = new int[choices.size()];
        int width = -6;
        for (Choice choice : choices) {
            width += chipWidth(choiceLabel(choice)) + 6;
        }
        if (width <= RIGHT - INNER) {
            return rows;
        }
        int dial = -1;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).action() == Hangar.Action.POWER_UP) {
                dial = i;
            }
        }
        int row = 0;
        float x = INNER;
        for (int i = 0; i < choices.size(); i++) {
            int chip = chipWidth(choiceLabel(choices.get(i)));
            boolean wrap = dial >= 0 ? i == dial + 1 : i > 0 && x + chip > RIGHT;
            if (wrap) {
                row++;
                x = INNER;
            }
            rows[i] = row;
            x += chip + 6;
        }
        return rows;
    }

    /** Where {@link #SHORT_REFUND} is written: after the last chip, on its row. */
    static Box refundNote(Offer offer) {
        Box last = choiceChip(offer, offer.choices().size() - 1);
        return new Box(last.x() + last.width() + 8, last.y() + 4, SHORT_REFUND.length() * Fonts.LABEL_CELL, 12);
    }

    /** The side row's {@code LEFT} or {@code RIGHT} chip. */
    static Box sideChip(WingmanSpec.Side side) {
        float x = INNER;
        for (WingmanSpec.Side before : WingmanSpec.Side.values()) {
            if (before == side) {
                break;
            }
            x += chipWidth(before.name()) + 6;
        }
        return new Box(x, DETAIL_Y + 64, chipWidth(side.name()), 20);
    }

    private static int chipWidth(String label) {
        return label.length() * Fonts.LABEL_CELL + 14;
    }

    /** The power dial's chips (2026-10-21), short so that they fit beside the upgrade's. */
    static final String POWER_DOWN = "PWR -";

    static final String POWER_UP = "PWR +";

    /** A choice's chip: the action and its credits, without "CR" so three chips fit the drawer. */
    static String choiceLabel(Choice choice) {
        String credits = Names.grouped(Math.abs(choice.credits()));
        return switch (choice.action()) {
            case BUY -> "BUY " + credits;
            case UPGRADE ->
                choice.refusal().filter(Refusal.MAX_LEVEL::equals).isPresent() ? "MAX LEVEL" : "UPGRADE " + credits;
            case FIT -> "FIT";
            case UNFIT -> "UNFIT";
            case SELL -> "SELL " + credits;
            case BUY_CHARGE -> "CHARGE " + credits;
            case POWER_DOWN -> POWER_DOWN;
            case POWER_UP -> POWER_UP;
        };
    }

    static String label(Stat stat) {
        return switch (stat) {
            case DPS -> "DPS";
            case OUTPUT -> "OUTPUT";
            case CAPACITY -> "CAP";
            case REGEN -> "REGEN";
            case DELAY -> "DELAY";
            case ARMOUR -> "ARMOUR";
            case SPEED -> "SPEED";
            case CHARGES -> "MAX";
        };
    }
}
