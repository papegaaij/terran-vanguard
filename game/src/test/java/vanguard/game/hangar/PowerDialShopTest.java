package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
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
import vanguard.content.campaign.Flight;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.Choice;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.State;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.input.FakePointer;
import vanguard.game.ui.Box;

/**
 * Step PD-G, the power dial in the shop (the user, 2026-10-21; design/ui/hangar): a fitted weapon's {@code PWR -} /
 * {@code PWR +} chips, its setting in the detail's heading, the row's status and the loadout callout ({@code L3/5}),
 * every layout checked against the whole catalogue as {@link ShopNamesTest} checks the names; and the dial's keys,
 * messages, refusals and test fire.
 */
class PowerDialShopTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final List<LoadoutSlot> WEAPON_SLOTS =
            List.of(LoadoutSlot.FRONT, LoadoutSlot.LEFT_WING, LoadoutSlot.RIGHT_WING, LoadoutSlot.REAR);
    /** The drawer's text and chips end here. */
    private static final int RIGHT = ShopPanel.X + ShopPanel.WIDTH - 10;

    private static final int LABEL_ADVANCE = 8;
    private static final int BODY_ADVANCE = 10;

    private static Campaign campaign(Map<LoadoutSlot, Fitted> loadout, int credits) {
        return Campaign.load(
                RULES,
                new SaveGame(
                        SaveFormat.VERSION,
                        Instant.EPOCH,
                        0,
                        Difficulty.MEDIUM,
                        21,
                        credits,
                        0,
                        loadout,
                        Map.of(),
                        List.of(),
                        Map.of(),
                        60,
                        Optional.empty(),
                        Map.of(),
                        List.of(),
                        List.of(),
                        new SaveGame.Stats(0, 0)));
    }

    private static Hangar hangar(LoadoutSlot slot, Fitted weapon, int credits) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(RULES.starterLoadout());
        loadout.put(slot, weapon);
        return new Hangar(CATALOGUE, campaign(loadout, credits));
    }

    private static Offer fittedRow(List<Offer> rows) {
        return rows.stream()
                .filter(offer -> offer.state() == State.FITTED)
                .findFirst()
                .orElseThrow();
    }

    private static List<Action> actions(Offer offer) {
        return offer.choices().stream().map(Choice::action).toList();
    }

    @Test
    void everyFittedWeaponsChoicesFitTheDrawerInAtMostTwoRows() {
        int checked = 0;
        for (LoadoutSlot slot : WEAPON_SLOTS) {
            for (Item item : CATALOGUE.items(slot.kind())) {
                assertTrue(
                        Flight.flies(CONTENT, slot, new Fitted(item.id(), 1)),
                        () -> item.name() + " would show NOT YET IN FLIGHT where the dial's chips are");
                for (int level = 1; level <= item.maxLevel(); level++) {
                    for (int power = 1; power <= level; power++) {
                        Hangar hangar = hangar(slot, new Fitted(item.id(), level, power), 100_000);
                        Offer offer = fittedRow(hangar.shop(slot));
                        String what = item.name() + " L" + power + "/" + level + " in " + slot;
                        List<Action> actions = actions(offer);
                        int dial = actions.indexOf(Action.POWER_DOWN);
                        assertTrue(dial >= 0, () -> what + " has no dial: " + actions);
                        assertEquals(Action.POWER_UP, actions.get(dial + 1), what);
                        assertFits(offer, what);
                        assertFits(boughtThisVisit(offer), what + ", bought this visit");
                        HangarState refit = new HangarState(hangar, true, true);
                        while (refit.slot() != slot) {
                            refit.nextSlot();
                        }
                        Offer refitRow = fittedRow(refit.rows());
                        assertTrue(actions(refitRow).containsAll(List.of(Action.POWER_DOWN, Action.POWER_UP)), what);
                        assertFits(refitRow, what + ", replay refit");
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 100, "the catalogue's weapons at every level and setting");
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

    private static void assertFits(Offer offer, String what) {
        int rows = ShopPanel.chipRows(offer);
        assertTrue(rows <= 2, () -> what + ": " + rows + " rows of chips");
        int statsBottom = ShopPanel.DETAIL_Y + (offer.item().traits().isEmpty() ? 24 : 40) + 12;
        Box previous = null;
        for (int i = 0; i < offer.choices().size(); i++) {
            Box chip = ShopPanel.choiceChip(offer, i);
            String label = ShopPanel.choiceLabel(offer.choices().get(i));
            assertTrue(chip.x() + chip.width() <= RIGHT, () -> what + ": " + label + " passes the drawer's edge");
            assertTrue(chip.y() >= statsBottom + 4, () -> what + ": " + label + " covers the numbers");
            assertTrue(
                    chip.y() + chip.height() <= ShopPanel.MESSAGE_Y - 2,
                    () -> what + ": " + label + " covers the message");
            if (previous != null && previous.y() == chip.y()) {
                assertTrue(chip.x() >= previous.x() + previous.width(), () -> what + ": " + label + " overlaps");
            } else if (previous != null) {
                assertTrue(chip.y() >= previous.y() + previous.height(), () -> what + ": " + label + " overlaps");
            }
            previous = chip;
        }
        if (ShopPanel.fullRefund(offer)) {
            Box first = ShopPanel.choiceChip(offer, 0);
            if (rows == 1) {
                assertTrue(statsBottom + 14 <= first.y(), () -> what + ": the refund line meets the chips");
            } else {
                Box note = ShopPanel.refundNote(offer);
                assertTrue(note.x() + note.width() <= RIGHT, () -> what + ": FULL REFUND passes the drawer's edge");
            }
        }
    }

    @Test
    void theDialsChipsReadInOrderUpgradeAndDialAboveTheRest() {
        Hangar hangar = hangar(LoadoutSlot.REAR, new Fitted("rear-lance", 4, 2), 100_000);
        Offer offer = fittedRow(hangar.shop(LoadoutSlot.REAR));

        assertEquals(
                List.of(Action.UPGRADE, Action.POWER_DOWN, Action.POWER_UP, Action.UNFIT, Action.SELL), actions(offer));
        assertEquals(2, ShopPanel.chipRows(offer));
        float top = ShopPanel.choiceChip(offer, 0).y();
        assertEquals(top, ShopPanel.choiceChip(offer, 2).y());
        assertEquals(top + ShopPanel.CHIP_ROW, ShopPanel.choiceChip(offer, 3).y(), "UNFIT starts the second row");
        assertEquals(
                ShopPanel.choiceChip(offer, 0).x(),
                ShopPanel.choiceChip(offer, 3).x());
        assertEquals("PWR -", ShopPanel.choiceLabel(offer.choices().get(1)));
        assertEquals("PWR +", ShopPanel.choiceLabel(offer.choices().get(2)));
    }

    @Test
    void theHeadingAndTheRowShowTheSettingOverTheLevel() {
        int heading = ShopPanel.headingChars(BODY_ADVANCE);
        List<String> cut = new ArrayList<>();
        for (LoadoutSlot slot : WEAPON_SLOTS) {
            for (Item item : CATALOGUE.items(slot.kind())) {
                for (int level = 2; level <= item.maxLevel(); level++) {
                    for (int power = 1; power < level; power++) {
                        String suffix = ShopPanel.setting(power, level);
                        assertEquals("L" + power + "/" + level, suffix);
                        String shown = Names.heading(item.name(), suffix, heading);
                        assertTrue(shown.length() <= heading, () -> shown + " is wider than " + heading);
                        assertTrue(shown.endsWith(suffix), () -> shown + " lost its setting " + suffix);
                        Offer row = new Offer(
                                item,
                                State.FITTED,
                                level,
                                Optional.of(new Fitted(item.id(), level, power)),
                                false,
                                false,
                                List.of());
                        String status = ShopPanel.status(row);
                        assertEquals(suffix + " FITTED", status);
                        int chars = ShopPanel.rowChars(LABEL_ADVANCE, status, false, 0);
                        String name = Names.fit(item.name(), chars);
                        assertTrue(name.length() <= chars, () -> name + " is wider than its row beside " + status);
                        if (!Names.forms(item.name()).contains(name)) {
                            cut.add(item.name() + " -> " + name + " beside " + status);
                        }
                    }
                }
                assertEquals("L3", ShopPanel.setting(3, 3), "full power shows the level alone");
            }
        }
        assertEquals(List.of(), cut, "names cut beside a turned-down status");
    }

    @Test
    void theCalloutsSettingFitsBesideItsSlotName() {
        Map<LoadoutSlot, Box> boxes = LoadoutPanel.slots(true, false);
        for (LoadoutSlot slot : WEAPON_SLOTS) {
            Box callout = boxes.get(slot);
            int maxLevel = CATALOGUE.items(slot.kind()).stream()
                    .mapToInt(Item::maxLevel)
                    .max()
                    .orElseThrow();
            String setting = LoadoutPanel.setting(new Fitted("any", maxLevel, maxLevel - 1));
            float nameEnd = callout.x() + 6 + Names.slot(slot).length() * LABEL_ADVANCE;
            float settingStart = callout.x() + callout.width() - 6 - setting.length() * LABEL_ADVANCE;
            assertTrue(nameEnd + 6 <= settingStart, () -> slot + ": " + setting + " meets its slot name");
        }
        assertEquals("L3/5", LoadoutPanel.setting(new Fitted("pulse-cannon", 5, 3)));
    }

    @Test
    void theKeysTurnTheFrontGunDownAndUpAgainForFree() {
        Hangar hangar = hangar(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 3), 0);
        Campaign campaign = hangar.campaign();
        HangarState state = new HangarState(hangar, true);
        while (state.choice().orElseThrow().action() != Action.POWER_DOWN) {
            state.right();
        }
        assertEquals(-1, TestFirePanel.dialling(state));
        assertEquals(2, TestFirePanel.level(state, state.selected().orElseThrow()), "the test fire previews L2");

        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(new Fitted("pulse-cannon", 3, 2), campaign.loadout().get(LoadoutSlot.FRONT));
        assertEquals("PULSE CANNON POWER L2 OF 3", state.message());
        assertEquals(0, campaign.credits());
        assertEquals("L2/3", ShopPanel.setting(state.selected().orElseThrow()));
        assertEquals(2, state.selected().orElseThrow().power());

        state.confirm();
        assertEquals(Outcome.REFUSED, state.confirm());
        assertEquals("AT ITS LOWEST POWER", state.message());
        assertEquals(1, campaign.loadout().get(LoadoutSlot.FRONT).power());

        state.right();
        assertEquals(Action.POWER_UP, state.choice().orElseThrow().action());
        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(Outcome.DONE, state.confirm());
        assertEquals("PULSE CANNON POWER L3 OF 3", state.message());
        assertEquals(Outcome.REFUSED, state.confirm());
        assertEquals("AT FULL POWER", state.message());
        assertEquals(0, TestFirePanel.dialling(state), "nothing to preview at full power");
        assertEquals(new Fitted("pulse-cannon", 3), campaign.loadout().get(LoadoutSlot.FRONT));
    }

    @Test
    void theMouseClicksTheDialsChips() {
        Hangar hangar = hangar(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 3), 0);
        HangarState state = new HangarState(hangar, true);
        Offer offer = state.selected().orElseThrow();
        int down = actions(offer).indexOf(Action.POWER_DOWN);
        FakePointer mouse = new FakePointer();
        HangarPointer pointer = new HangarPointer();
        mouse.move(ShopPanel.choiceChip(offer, down));
        pointer.update(state, mouse.pointer);
        mouse.click();

        assertTrue(pointer.update(state, mouse.pointer).confirm(), "a click on PWR - confirms it");
        assertEquals(Action.POWER_DOWN, state.choice().orElseThrow().action());
        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(
                new Fitted("pulse-cannon", 3, 2), hangar.campaign().loadout().get(LoadoutSlot.FRONT));
    }

    @Test
    void raisingTheSettingOverTheOutputSaysHowMuchPowerItNeeds() {
        Choice refused = new Choice(Action.POWER_UP, 0, 13.5, 12, true, Optional.of(Hangar.Refusal.POWER));

        assertEquals("NEEDS 1.5 MW MORE POWER", HangarState.refusal(refused));
    }

    @Test
    void anUpgradeOfATurnedDownWeaponSaysItKeepsItsSetting() {
        Item cannon = CATALOGUE.item(vanguard.content.campaign.ItemKind.FRONT, "pulse-cannon");
        Offer down = new Offer(
                cannon, State.FITTED, 3, Optional.of(new Fitted("pulse-cannon", 3, 2)), false, false, List.of());
        Offer full =
                new Offer(cannon, State.FITTED, 3, Optional.of(new Fitted("pulse-cannon", 3)), false, false, List.of());

        assertEquals("PULSE CANNON UPGRADED TO L4 - POWER L2", HangarState.upgraded("PULSE CANNON", down));
        assertEquals("PULSE CANNON UPGRADED TO L4", HangarState.upgraded("PULSE CANNON", full));
    }

    @Test
    void theReplayHangarTurnsTheDialToo() {
        Hangar hangar = hangar(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 3), 0);
        HangarState state = new HangarState(hangar, true, true);
        while (state.choice().orElseThrow().action() != Action.POWER_DOWN) {
            state.right();
        }

        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(
                new Fitted("pulse-cannon", 3, 2), hangar.campaign().loadout().get(LoadoutSlot.FRONT));
    }

    @Test
    void theTestFireLabelShowsTheDialsStep() {
        assertEquals("TEST FIRE L3>L2", TestFirePanel.label(3, 2));
        assertEquals("TEST FIRE L4>L5", TestFirePanel.label(4, 5));
        assertEquals("TEST FIRE L2", TestFirePanel.label(2, 2));
    }
}
