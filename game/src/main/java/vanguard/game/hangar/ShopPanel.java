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
 * cooldown special's detail shows its cooldown. The rows and chips double as the mouse's hit regions
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
            } else if (state.bayRow().isPresent()) {
                drawBayRow(batch, state.bayRow().get(), y, shop && i == state.row());
            } else {
                drawSideRow(batch, state, y, shop && i == state.row());
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
            drawBay(batch, state.bayRow().orElseThrow(), shopFocus, DETAIL_Y);
        }
        Optional<String> refused = shopFocus
                ? state.choice().filter(choice -> !choice.allowed()).map(HangarState::refusal)
                : Optional.empty();
        if (shopFocus && state.baySelected()) {
            refused = state.bayRow().flatMap(Hangar.BayOffer::refusal).map(HangarState::bayRefusal);
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
        glass.shadowed(
                batch,
                glass.fonts.label,
                Names.of(offer.item().name()),
                colour,
                NAME_X,
                y,
                x - matches * 10 - NAME_X - 4);
    }

    /** The escort's last row: Rook's side, {@code ROOK'S SIDE ... LEFT}. */
    private void drawSideRow(SpriteBatch batch, HangarState state, int y, boolean selected) {
        glass.row(batch, X + 4, y - 5, WIDTH - 8, ROW - 2);
        if (selected) {
            glass.selection(batch, X + 2, y - 5, WIDTH - 4, ROW - 2);
            glass.cursor(batch, glass.fonts.label, X + 4, y + 4);
        }
        drawIcon(batch, icons.escort().small(), INNER - 2, y - 3, false);
        glass.right(batch, glass.fonts.label, state.escortSide().name(), Glass.CYAN, RIGHT, y);
        glass.shadowed(batch, glass.fonts.label, SIDE, selected ? Glass.AMBER : Glass.WHITE, NAME_X, y);
    }

    /** The side row's detail: where Rook flies, and the {@code LEFT} / {@code RIGHT} choice. */
    private void drawSide(SpriteBatch batch, HangarState state, int y) {
        glass.shadowed(batch, glass.fonts.body, SIDE, Glass.AMBER, INNER, y);
        glass.shadowed(batch, glass.fonts.label, "WHERE ROOK FLIES BESIDE YOU.", Glass.WHITE, INNER, y + 24);
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
            }
        }
    }

    /** The side row's name. */
    static final String SIDE = "ROOK'S SIDE";

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
    private void drawBay(SpriteBatch batch, Hangar.BayOffer line, boolean shop, int y) {
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

    private static String status(Offer offer) {
        String level = offer.item().maxLevel() > 1 ? "L" + offer.level() + " " : "";
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
        String level = item.maxLevel() > 1 ? "  L" + offer.level() : "";
        TextureRegion icon = icons.of(item).large();
        drawIcon(batch, icon, RIGHT - icon.getRegionWidth(), y - 6, offer.state() == State.LOCKED);
        glass.shadowed(
                batch, glass.fonts.body, Names.of(item.name()) + level, Glass.AMBER, INNER, y, RIGHT - 28 - INNER);
        int traitX = INNER;
        for (String trait : item.traits()) {
            String text = "<" + trait.toUpperCase(Locale.ROOT) + ">";
            glass.shadowed(batch, glass.fonts.label, text, Glass.AMBER, traitX, y + 24);
            traitX += Fonts.width(glass.fonts.label, text) + 8;
        }
        int line = statsY(item);
        Map<Stat, Double> stats = item.stats(offer.level());
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
        numbers.append("DRAW ").append(Names.megawatts(item.draw(offer.level())));
        glass.shadowed(batch, glass.fonts.label, numbers.toString(), Glass.WHITE, INNER, line);
        List<Delta> deltas = offer.state() != State.FITTED && fitted.isPresent()
                ? deltas(item, offer.level(), fitted.get(), fittedLevel(state))
                : List.of();
        if (!deltas.isEmpty()) {
            drawDeltas(batch, deltas, line + 14);
        } else if (fullRefund(offer)) {
            // M7 part A (round 37, R.4 e): a sale of what this visit bought returns all it cost.
            glass.shadowed(batch, glass.fonts.label, FULL_REFUND, Glass.LABEL, INNER, line + 14);
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

    private static int fittedLevel(HangarState state) {
        Fitted fitted = state.hangar().campaign().loadout().get(state.slot());
        return fitted == null ? 1 : fitted.level();
    }

    private boolean flies(LoadoutSlot slot, Item item) {
        return Flight.flies(content, slot, new Fitted(item.id(), 1));
    }

    /** M7 part A: what the detail says of a sale of an item this visit bought (its full price comes back). */
    static final String FULL_REFUND = "BOUGHT THIS VISIT: FULL REFUND";

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
            }
        }
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

    /** A choice's chip under the detail of a row that is not locked, as wide as its label. */
    static Box choiceChip(Offer offer, int index) {
        float x = INNER;
        for (int i = 0; i < index; i++) {
            x += chipWidth(choiceLabel(offer.choices().get(i))) + 6;
        }
        return new Box(
                x,
                statsY(offer.item()) + 44,
                chipWidth(choiceLabel(offer.choices().get(index))),
                20);
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
