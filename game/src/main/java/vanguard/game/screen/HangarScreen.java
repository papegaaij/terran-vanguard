package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Intel;
import vanguard.content.campaign.SaveSlots;
import vanguard.game.GameServices;
import vanguard.game.audio.MusicStreamer;
import vanguard.game.audio.Sfx;
import vanguard.game.hangar.HangarPointer;
import vanguard.game.hangar.HangarState;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.hangar.HangarView;
import vanguard.game.hangar.Names;
import vanguard.game.input.MenuInput;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.game.ui.Dialog;
import vanguard.game.ui.Glass;

/**
 * The hangar between levels (design/ui/hangar, chosen hangar-r07-b: layout B in the glass style
 * over the tactical map): the shop drawer, the ship's schematic with its module tiles and the power
 * bar, the intel on the next level, and the command bar with undo, repair, save and launch. Keys:
 * previous / next tab select the slot, up / down the shop row (and the command bar above it),
 * left / right the row's choice or the command, confirm makes it; selling, launching with a
 * warning and quitting ask first; the selected weapon's test fire loops in the shop's box. It
 * autosaves when it opens (design/systems/saves), except right after a save was loaded, and when
 * the player quits from it (never in a debug run, which says so instead), and plays the hangar theme.
 * The mouse reaches the same places ({@link HangarPointer}); a frame with a key press ignores it.
 *
 * <p>The replay hangar (the user's decision of 2026-10-18: a mission select's replay goes briefing,
 * hangar, level) works on the real campaign of the save the replay was started from and is refit only
 * (the user's correction of 2026-10-19): the owned items are fitted and unfitted and that is kept, but
 * nothing is bought, sold, upgraded or repaired ({@link HangarState#refitOnly()}). Its top bar says {@code REPLAY L05}
 * instead of {@code BEFORE L05} and its launch {@code > REPLAY}; it opens without an autosave (a save
 * was just loaded), and its launch writes the campaign as the hangar left it to the autosave, of which
 * the flight is then a replay ({@link Campaign#replay}): full armour, unlimited retries, nothing earned,
 * progress unchanged, a better grade kept in that autosave.
 */
public final class HangarScreen implements GameScreen {
    private static final float MUSIC_VOLUME = 0.6f;
    /** The autosave line of a debug run, which writes no save. */
    private static final String DEBUG_RUN = "AUTOSAVE OFF - DEBUG RUN";

    private final GameServices services;
    private final Campaign campaign;
    /** The replay this hangar launches; empty for the campaign's next level. */
    private final Optional<Campaign.Replay> replay;
    /** The number of the level the hangar launches. */
    private final int number;

    private final Optional<String> nextLevel;
    private final HangarState state;
    private final HangarView view;
    private final HangarPointer pointer = new HangarPointer();
    private final Optional<MusicStreamer> music;
    private final String autosave;
    private Optional<Dialog> dialog = Optional.empty();
    private Optional<Pending> pending = Optional.empty();

    /** What a confirmation is for. */
    private enum Pending {
        SELL,
        LAUNCH,
        QUIT
    }

    /** @param autosave write the autosave as the hangar opens */
    public HangarScreen(GameServices services, Campaign campaign, boolean autosave) {
        this(services, campaign, autosave, Optional.empty());
    }

    /**
     * @param replay the replay the hangar launches (the slot the campaign was loaded from, the level), or
     *     empty for the campaign's next level
     */
    private HangarScreen(GameServices services, Campaign campaign, boolean autosave, Optional<Campaign.Replay> replay) {
        this.services = services;
        this.campaign = campaign;
        this.replay = replay;
        number = replay.map(Campaign.Replay::level).orElse(campaign.nextLevel());
        nextLevel = replay.isPresent()
                ? services.content.levelKey(number)
                : CampaignRoute.launch(services.content, campaign);
        // A special's free charges at its unlock (the first Airstrike charge), before the autosave.
        List<Campaign.FreeCharges> given = campaign.giveFreeCharges(services.content.specials());
        state = new HangarState(new Hangar(services.catalogue, campaign), nextLevel.isPresent(), replay.isPresent());
        for (Campaign.FreeCharges free : given) {
            state.notice(HangarState.freeChargesNotice(free));
        }
        view = new HangarView(services.files, services.glass, services.sprites, services.catalogue, services.content);
        if (autosave && services.debugRun()) {
            // A debug run writes no save (design/systems/saves).
            this.autosave = DEBUG_RUN;
        } else if (autosave) {
            boolean written = services.save(SaveSlots.Slot.AUTOSAVE, campaign);
            this.autosave = written ? "AUTOSAVED" : "AUTOSAVE FAILED";
            if (written) {
                services.play(Sfx.SAVE_DONE);
            }
        } else {
            this.autosave = "";
        }
        music = MusicStreamer.play(
                services.audio, services.files.internal("music/hangar-theme.ogg"), MUSIC_VOLUME, services.mixer);
    }

    /**
     * After a new game's difficulty select and after the act outro: the next level's briefing and
     * then the hangar, or the hangar while that level is not built.
     */
    public static GameScreen beforeNextLevel(GameServices services, Campaign campaign) {
        return screen(services, campaign, CampaignRoute.beforeNextLevel(services.content, campaign));
    }

    /**
     * After a won level's debrief: the act outro first when the level ended its act (design/campaign,
     * Act intro and outro), then as {@link #beforeNextLevel}, or the release's end when the content
     * has no next level (M6 part B).
     */
    public static GameScreen afterLevel(GameServices services, Campaign campaign) {
        return screen(services, campaign, CampaignRoute.afterLevel(services.content, campaign));
    }

    /**
     * A loaded save (the main menu's Continue, a slot's load; the user's decision of 2026-10-18): the next
     * level's mission briefing, then the hangar without an autosave, as a save was just loaded. M7 part B
     * (user decision L.6 of round 36): a save whose next level opens an act whose intro it has not shown (a
     * release save at the Level 15 hangar) plays that intro first ({@code CampaignRoute.onContinue}), then
     * the level's briefing and the hangar with its autosave, which keeps the flag. The hangar alone while
     * the next level is not built.
     */
    public static GameScreen onContinue(GameServices services, Campaign campaign) {
        CampaignRoute.Step step = CampaignRoute.onContinue(services.content, campaign);
        if (continueAutosaves(step)) {
            return screen(services, campaign, step);
        }
        return switch (step) {
            case CampaignRoute.Step.Briefing briefing ->
                new BriefingScreen(
                        services, campaign, briefing.script(), () -> new HangarScreen(services, campaign, false));
            case CampaignRoute.Step other -> new HangarScreen(services, campaign, false);
        };
    }

    /** Whether the hangar after a Continue's step autosaves: after an act intro, to keep its flag. */
    static boolean continueAutosaves(CampaignRoute.Step step) {
        return step instanceof CampaignRoute.Step.Briefing briefing
                && briefing.script().titleCard().isPresent();
    }

    /**
     * A mission select's replay (the user's decision of 2026-10-18): the level's mission briefing, then the
     * replay hangar on the save's real campaign (refit only, the user's correction of 2026-10-19), then the
     * level as a replay.
     *
     * @param slot the slot the save was read from
     */
    static GameScreen replay(GameServices services, SaveSlots.Slot slot, Campaign campaign, int level) {
        Optional<Campaign.Replay> replay = Optional.of(new Campaign.Replay(slot, level));
        return switch (CampaignRoute.beforeReplay(services.content, level)) {
            case CampaignRoute.Step.Briefing briefing ->
                new BriefingScreen(
                        services,
                        campaign,
                        briefing.script(),
                        () -> new HangarScreen(services, campaign, false, replay));
            case CampaignRoute.Step step -> new HangarScreen(services, campaign, false, replay);
        };
    }

    /**
     * The flight of a replay hangar's launch: a replay of the campaign as the hangar left it ({@link
     * Campaign#replay}), whose better grade goes into {@code slot}.
     *
     * @param slot the autosave when the launch wrote the campaign there, otherwise the slot it was loaded from
     */
    static Campaign replayFlight(CampaignRules rules, Campaign campaign, SaveSlots.Slot slot, int level) {
        return Campaign.replay(rules, slot, campaign.save(Instant.now()), level);
    }

    private static GameScreen screen(GameServices services, Campaign campaign, CampaignRoute.Step step) {
        return switch (step) {
            case CampaignRoute.Step.Briefing briefing ->
                new BriefingScreen(
                        services, campaign, briefing.script(), () -> new HangarScreen(services, campaign, true));
            case CampaignRoute.Step.Outro outro ->
                BriefingScreen.outro(
                        services,
                        campaign,
                        outro.script(),
                        () -> CampaignRoute.releaseEnd(services.content, campaign)
                                ? new ReleaseEndScreen(services, campaign)
                                : beforeNextLevel(services, campaign));
            case CampaignRoute.Step.Hangar hangar -> new HangarScreen(services, campaign, true);
        };
    }

    /** The next level's intel at the sensor level the loadout gives now. */
    private Optional<Intel> intel() {
        return nextLevel.map(
                key -> Intel.of(services.content, key, state.hangar().sensorLevel()));
    }

    @Override
    public Transition update(float seconds) {
        campaign.play(seconds);
        MenuInput input = services.menu;
        Transition next = input(input);
        // The test fire follows what the input selected.
        view.update(seconds, state);
        return next;
    }

    private Transition input(MenuInput input) {
        if (dialog.isPresent()) {
            return answer(dialog.get().update(input));
        }
        if (input.back()) {
            if (state.back()) {
                services.play(Sfx.MENU_BACK);
            } else {
                ask(
                        Pending.QUIT,
                        new Dialog(
                                "QUIT TO MAIN MENU?",
                                services.debugRun()
                                        ? "A DEBUG RUN WRITES NO SAVE."
                                        : "THE AUTOSAVE KEEPS THIS HANGAR VISIT.",
                                "YES, QUIT",
                                "NO, BACK"));
            }
            return Transition.STAY;
        }
        Outcome outcome = Outcome.NONE;
        if (input.previousTab()
                || input.nextTab()
                || input.up()
                || input.down()
                || input.left()
                || input.right()
                || input.confirm()) {
            // The last device used owns the look: a key takes the mouse's hover off.
            state.hover(Optional.empty());
        }
        if (input.previousTab()) {
            outcome = state.previousSlot();
        } else if (input.nextTab()) {
            outcome = state.nextSlot();
        } else if (input.up()) {
            outcome = state.up();
        } else if (input.down()) {
            outcome = state.down();
        } else if (input.left()) {
            outcome = state.left();
        } else if (input.right()) {
            outcome = state.right();
        } else if (input.confirm()) {
            return confirm();
        } else {
            HangarPointer.Result mouse = pointer.update(state, input.pointer());
            if (mouse.confirm()) {
                return confirm();
            }
            outcome = mouse.outcome();
        }
        return react(outcome, Sfx.MENU_CONFIRM);
    }

    /** Confirm, by the key or a click: makes the highlighted choice or command. */
    private Transition confirm() {
        // What the confirmation makes, read before it changes the selection.
        Sfx done = doneSound(
                state.focus() == Focus.SHOP ? state.selectedAction() : Optional.empty(),
                state.focus(),
                state.command());
        return react(state.confirm(), done);
    }

    /**
     * The sound of a confirmation that was made (design/audio/sfx, UI and radio): a shop action's
     * own ({@link Sfx#shop}), a paid repair's purchase, an undo's refund, else the menu's confirm.
     *
     * @param made the highlighted shop choice's action, when the shop had the focus
     * @param focus where the focus was
     * @param command the highlighted command
     */
    static Sfx doneSound(Optional<Hangar.Action> made, Focus focus, Command command) {
        if (made.isPresent()) {
            return Sfx.shop(made.get());
        }
        if (focus == Focus.REPAIR) {
            return Sfx.SHOP_BUY;
        }
        if (focus == Focus.COMMANDS && command == Command.UNDO) {
            return Sfx.SHOP_SELL;
        }
        return Sfx.MENU_CONFIRM;
    }

    private Transition react(Outcome outcome, Sfx done) {
        switch (outcome) {
            case NONE -> {}
            case MOVED -> services.play(Sfx.MENU_MOVE);
            case DONE -> services.play(done);
            case REFUSED -> services.play(Sfx.SHOP_DENIED);
            case SELL -> {
                services.play(Sfx.MENU_CONFIRM);
                var sale = state.sale().orElseThrow();
                ask(
                        Pending.SELL,
                        new Dialog(
                                "SELL " + sale.name() + " FOR " + Names.credits(sale.refund()) + "?",
                                sale.bought()
                                        ? "BOUGHT DURING THIS VISIT: THE FULL PRICE COMES BACK."
                                        : "UNDO RETURNS THIS VISIT'S TRANSACTIONS FOR 100 %.",
                                "YES, SELL",
                                "NO, KEEP"));
            }
            case SAVE -> {
                services.play(Sfx.MENU_CONFIRM);
                return Transition.open(SlotsScreen.save(services, campaign));
            }
            case LAUNCH -> {
                services.play(Sfx.MENU_CONFIRM);
                List<String> warnings = state.launchWarnings(intel());
                if (warnings.isEmpty()) {
                    return launch();
                }
                ask(
                        Pending.LAUNCH,
                        new Dialog(
                                warnings.getFirst() + ". LAUNCH ANYWAY?",
                                String.join(" - ", warnings.subList(1, warnings.size())),
                                "YES, LAUNCH",
                                "NO, BACK"));
            }
        }
        return Transition.STAY;
    }

    private void ask(Pending what, Dialog question) {
        pending = Optional.of(what);
        dialog = Optional.of(question);
    }

    private Transition answer(Dialog.Answer answer) {
        if (answer == Dialog.Answer.NONE) {
            return Transition.STAY;
        }
        Pending what = pending.orElseThrow();
        dialog = Optional.empty();
        pending = Optional.empty();
        if (answer == Dialog.Answer.NO) {
            services.play(Sfx.MENU_BACK);
            return Transition.STAY;
        }
        services.play(what == Pending.SELL ? Sfx.SHOP_SELL : Sfx.MENU_CONFIRM);
        return switch (what) {
            case SELL -> {
                state.sell();
                yield Transition.STAY;
            }
            case LAUNCH -> launch();
            case QUIT -> {
                services.save(SaveSlots.Slot.AUTOSAVE, campaign);
                yield Transition.replace(MainMenuScreen.menu(services));
            }
        };
    }

    private Transition launch() {
        if (replay.isEmpty()) {
            return Transition.replace(new LevelScreen(services, campaign, nextLevel.orElseThrow()));
        }
        // The replay hangar's changes are the campaign's: written to the autosave, which the flight replays.
        boolean written = services.save(SaveSlots.Slot.AUTOSAVE, campaign);
        SaveSlots.Slot slot = written ? SaveSlots.Slot.AUTOSAVE : replay.get().slot();
        Campaign flight = replayFlight(services.campaignRules, campaign, slot, number);
        return Transition.replace(new LevelScreen(services, flight, nextLevel.orElseThrow()));
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        Optional<Intel> intel = intel();
        Optional<BriefingPage> teaser =
                nextLevel.map(key -> services.content.level(key).briefing().teaser());
        view.draw(batch, state, intel, teaser, nextLevel);
        drawTopBar(batch, glass);
        glass.right(
                batch,
                glass.fonts.label,
                autosave,
                autosave.endsWith("FAILED") ? Glass.ALERT : autosave.equals(DEBUG_RUN) ? Glass.AMBER : Glass.GREEN,
                PixelScreen.WIDTH - 16,
                PixelScreen.HEIGHT - 36);
        glass.hints(batch, hints());
        if (state.focus() == Focus.REPAIR) {
            drawRepair(batch, glass);
        }
        dialog.ifPresent(question -> glass.dialog(batch, question));
    }

    private void drawTopBar(SpriteBatch batch, Glass glass) {
        glass.panel(batch, 16, 6, PixelScreen.WIDTH - 32, 40, 0.86f);
        glass.shadowed(batch, glass.fonts.heading, "HANGAR", Glass.WHITE, 26, 11);
        String before = replay.isPresent() ? "REPLAY" : "BEFORE";
        String mission = nextLevel
                .map(key -> String.format(Locale.ROOT, "%s L%02d - %s", before, number, Content.levelName(key)))
                .orElseGet(() -> ReleaseTexts.hangarLine(services.content))
                .toUpperCase(Locale.ROOT);
        glass.shadowed(batch, glass.fonts.label, mission, Glass.CYAN, 160, 13);
        String act = services.content
                        .actOf(number)
                        .map(entry -> entry.getValue().titleCard().act() + " - "
                                + entry.getValue().titleCard().name() + " - ")
                        .orElse("")
                + campaign.difficulty().name();
        glass.shadowed(batch, glass.fonts.label, act, Glass.LABEL, 160, 27);
        for (Command command : List.of(Command.UNDO, Command.REPAIR, Command.SAVE)) {
            Box chip = HangarPointer.command(command);
            glass.chip(
                    batch,
                    glass.fonts.label,
                    command.name(),
                    chip.x(),
                    chip.y(),
                    chip.width(),
                    chip.height(),
                    on(command),
                    state.enabled(command));
        }
        glass.inset(batch, 662, 12, 144, 28);
        glass.right(batch, glass.fonts.body, Names.credits(campaign.credits()), Glass.AMBER, 798, 16);
        Box launch = HangarPointer.command(Command.LAUNCH);
        glass.chip(
                batch,
                glass.fonts.body,
                replay.isPresent() ? "> REPLAY" : "> LAUNCH",
                launch.x(),
                launch.y(),
                launch.width(),
                launch.height(),
                on(Command.LAUNCH),
                state.enabled(Command.LAUNCH));
        drawCommandHover(batch, glass);
    }

    private boolean on(Command command) {
        return state.focus() == Focus.COMMANDS && state.command() == command;
    }

    /** The hover look on the command the pointer rests on (design/ui/hangar, Mouse), unless it is selected. */
    private void drawCommandHover(SpriteBatch batch, Glass glass) {
        for (Command command : Command.values()) {
            if (!on(command) && state.hovers(HangarState.Target.COMMAND, command.ordinal())) {
                Box chip = HangarPointer.command(command);
                glass.outline(batch, Glass.CYAN, chip.x(), chip.y(), chip.width(), chip.height());
            }
        }
    }

    private String hints() {
        return switch (state.focus()) {
            case SHOP ->
                state.sideSelected()
                        ? "Q/E SLOT    UP/DOWN ITEM    LEFT/RIGHT SIDE    ESC MAIN MENU"
                        : "Q/E SLOT    UP/DOWN ITEM    LEFT/RIGHT CHOICE    ENTER CONFIRM    ESC MAIN MENU";
            case COMMANDS -> "LEFT/RIGHT COMMAND    ENTER CONFIRM    DOWN SHOP    ESC MAIN MENU";
            case REPAIR ->
                state.hangar().escortHired()
                        ? "Q/E SHIP/ROOK    LEFT/RIGHT 1 POINT    UP/DOWN 10 POINTS    ENTER REPAIR    ESC CLOSE"
                        : "LEFT/RIGHT 1 POINT    UP/DOWN 10 POINTS    ENTER REPAIR    ESC CLOSE";
        };
    }

    /**
     * The repair panel: the ship's armour line and, once Rook is hired, his ({@code SHIP} and
     * {@code ROOK}, design/ui/hangar, Escort), the selected line's points on the slider and its cost.
     */
    private void drawRepair(SpriteBatch batch, Glass glass) {
        Hangar hangar = state.hangar();
        boolean rook = hangar.escortHired();
        Box panel = HangarPointer.repairPanel(rook);
        float x = panel.x();
        float y = panel.y();
        float width = panel.width();
        float height = panel.height();
        glass.dim(batch, 0.4f);
        glass.panel(batch, x, y, width, height, 0.94f);
        glass.header(batch, "REPAIR ARMOUR", x + 12, x + width - 12, y + 12);
        float lineY = y + 32;
        for (HangarState.RepairLine line : HangarState.RepairLine.values()) {
            if (line == HangarState.RepairLine.ROOK && !rook) {
                continue;
            }
            boolean on = state.repairLine() == line;
            String text = repairLine(line, rook);
            glass.shadowed(batch, glass.fonts.label, text, on ? Glass.AMBER : Glass.WHITE, x + 12, lineY);
            lineY += 16;
        }
        String rate = hangar.repairCost() == 0 ? "FREE ON EASY" : Names.credits(hangar.repairCost()) + " A POINT";
        glass.shadowed(batch, glass.fonts.label, rate, Glass.LABEL, x + 12, lineY);
        int points = state.repairPoints();
        Box slider = HangarPointer.repairSlider(rook);
        glass.slider(
                batch,
                slider.x(),
                slider.y(),
                slider.width(),
                (double) points / Math.max(1, state.affordable(state.repairLine())),
                points + " PT");
        String cost = String.format(
                Locale.ROOT,
                "REPAIR %s%d POINTS FOR %s",
                state.repairLine() == HangarState.RepairLine.ROOK ? "ROOK: " : "",
                points,
                Names.credits((long) points * hangar.repairCost()));
        Box button = HangarPointer.repairButton(rook);
        glass.chip(batch, glass.fonts.label, cost, button.x(), button.y(), button.width(), button.height(), true, true);
    }

    /**
     * A repair line: {@code SHIP  ARMOUR 64 / 80 - 16 POINTS MISSING}, or without Rook the ship's
     * line as before ({@code ARMOUR 64 / 80 - 16 POINTS MISSING}).
     */
    private String repairLine(HangarState.RepairLine line, boolean named) {
        boolean ship = line == HangarState.RepairLine.SHIP;
        double armour = ship ? campaign.armour() : campaign.gear().escort().armour();
        double max = ship ? campaign.maxArmour() : campaign.escortMaxArmour();
        String text = String.format(
                Locale.ROOT,
                "ARMOUR %d / %d - %d POINTS MISSING",
                (int) Math.ceil(armour),
                (int) max,
                state.missing(line));
        if (!named) {
            return text;
        }
        return (state.repairLine() == line ? "> " : "  ") + line.name() + "  " + text;
    }

    @Override
    public void dispose() {
        music.ifPresent(MusicStreamer::close);
        view.dispose();
    }
}
