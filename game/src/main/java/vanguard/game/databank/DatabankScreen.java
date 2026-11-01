package vanguard.game.databank;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import vanguard.content.Content;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.DatabankRecord;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveSlots;
import vanguard.content.databank.Databank;
import vanguard.content.databank.DatabankView;
import vanguard.content.databank.Entry;
import vanguard.content.databank.Pages;
import vanguard.game.GameServices;
import vanguard.game.audio.Sfx;
import vanguard.game.hangar.TestFire;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;
import vanguard.game.render.PixelScreen;
import vanguard.game.render.PodPivots;
import vanguard.game.render.Portraits;
import vanguard.game.render.TestFireView;
import vanguard.game.screen.GameScreen;
import vanguard.game.screen.Transition;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.sim.FixedStepClock;
import vanguard.sim.SimStep;

/**
 * Step DB2 (design/ui/databank): the databank screen, opened over the hangar (its {@code DATA} chip, Tab / gamepad Y,
 * a click on an intel portrait) or the main menu ({@code DATABANK}, the current campaign), drawn over that screen's
 * backdrop with its music playing on. A top bar with the campaign's next level, act and difficulty, the sensor badge
 * and a {@code < BACK} chip; the five tabs with their counts; the list (layout a, {@link DatabankList}); the entry page
 * ({@link EntryPage}) with its picture ({@link EntryPictures}) and, for a weapon, the hangar's test fire at the level
 * LEFT / RIGHT step to. Q / E and the bumpers (or a click) change the tab, up / down the entry; Enter on a page with a
 * locked field (the callout's chip, a {@code SENSOR Ln} tag) opens the sensor suite's entry, from which Back returns;
 * Esc / B / the right button go back to the screen it came from, as it was. An entry shown for {@value #READ_SECONDS}
 * s counts as opened ({@code NEW} goes; decided as trivia, pending the user's review): kept in the campaign, which the
 * hangar's next autosave writes; opened from the main menu, the save's record is written back into its slot on the way
 * out (only its {@code databank} changes; a debug run writes nothing).
 */
public final class DatabankScreen implements GameScreen {
    private static final Logger LOG = Logger.getLogger(DatabankScreen.class.getName());

    /** An entry shown this long counts as opened. */
    static final float READ_SECONDS = 0.75f;
    /** The backdrop's dimming under the panels. */
    static final float DIMMED = 0.7f;

    /** The top bar. */
    static final int BAR_X = 16;

    static final int BAR_Y = 6;
    static final int BAR_WIDTH = PixelScreen.WIDTH - 32;
    static final int BAR_HEIGHT = 40;
    static final int MISSION_X = 200;
    /** The sensor badge and the back chip in the top bar. */
    static final Box BADGE = new Box(572, 10, 240, 32);

    static final Box BACK = new Box(824, 15, 108, 22);
    /** The tabs under the top bar. */
    static final int TAB_Y = 52;

    static final int TAB_WIDTH = 120;
    static final int TAB_HEIGHT = 24;
    static final int TAB_GAP = 6;
    /** The most steps one frame catches up on in the test fire (a stall drops the rest). */
    private static final int MAX_STEPS_PER_FRAME = 4;

    private final GameServices services;
    private final Campaign campaign;
    private final Databank databank;
    private final Consumer<SpriteBatch> backdrop;
    /** The main menu's campaign: its slot, whose save takes the record back on the way out; empty from the hangar. */
    private final Optional<SaveSlots.Entry.Saved> menuSave;

    private final EntryPictures pictures;
    private final Map<Entry.Tab, String> lastShown = new EnumMap<>(Entry.Tab.class);
    private final List<Link> links = new ArrayList<>();
    private final FixedStepClock clock = new FixedStepClock(SimStep.SECONDS, MAX_STEPS_PER_FRAME);
    private TestFireView testFireView;

    private DatabankView view;
    private Entry.Tab tab = Entry.Tab.ENEMIES;
    private DatabankList list;
    /** The page shown: a {@link Pages.EnemyPage}, {@link Pages.GearPage} or {@link Pages.ConceptPage}; null for none. */
    private Object page;
    /** The entry a link left, which Back returns to. */
    private Optional<String> returnTo = Optional.empty();

    private float shownFor;
    /** The level a gear page's table lights and its test fire loops. */
    private int shownLevel = 1;

    private Optional<TestFire> fire = Optional.empty();

    /** A place on the page the mouse follows to an entry. */
    private record Link(Box box, String id) {}

    private DatabankScreen(
            GameServices services,
            Campaign campaign,
            Consumer<SpriteBatch> backdrop,
            Optional<SaveSlots.Entry.Saved> menuSave,
            Optional<String> entry) {
        this.services = services;
        this.campaign = campaign;
        this.backdrop = backdrop;
        this.menuSave = menuSave;
        databank = services.databank();
        pictures = new EntryPictures(services.sprites, services.flash, services.content, databank.catalogue());
        view = databank.view(campaign);
        for (String id : campaign.databank().read()) {
            databank.entry(id).ifPresent(known -> lastShown.put(known.tab(), id));
        }
        Optional<Entry> wanted = entry.flatMap(databank::entry);
        if (wanted.isPresent()) {
            tab = wanted.get().tab();
            lastShown.put(tab, wanted.get().id());
        } else if (view.sensorLevel() == 0 && !view.ownsSuite()) {
            // The first visits with no suite open on the enemies, the selected one's callout to the suite.
            tab = Entry.Tab.ENEMIES;
        }
        openTab(tab);
    }

    /**
     * Over the hangar (its {@code DATA} chip, Tab / Y, or a click on an intel portrait: {@code entry}, a unit's slug),
     * the hangar's {@code backdrop} drawn under it; Back returns to the hangar as it was.
     */
    public static DatabankScreen overHangar(
            GameServices services, Campaign campaign, Consumer<SpriteBatch> backdrop, Optional<String> entry) {
        return new DatabankScreen(services, campaign, backdrop, Optional.empty(), entry);
    }

    /** Over the main menu, for the current campaign (the most recent save, the one Continue loads). */
    public static DatabankScreen overMenu(GameServices services, SaveSlots.Entry.Saved saved) {
        return new DatabankScreen(
                services,
                Campaign.load(services.campaignRules, saved.save()),
                batch -> services.titleScene.draw(batch, 0.35f),
                Optional.of(saved),
                Optional.empty());
    }

    private void openTab(Entry.Tab next) {
        tab = next;
        list = new DatabankList(view.list(tab));
        list.start(Optional.ofNullable(lastShown.get(tab)));
        showSelected();
    }

    /** The page of the entry under the cursor. */
    private void showSelected() {
        Optional<Entry> entry = list.entry();
        page = entry.map(this::pageOf).orElse(null);
        shownFor = 0;
        entry.ifPresent(e -> lastShown.put(tab, e.id()));
        pictures.show(page);
        shownLevel = page instanceof Pages.GearPage gear ? Math.max(1, gear.level()) : 1;
        startTestFire();
    }

    private Object pageOf(Entry entry) {
        if (entry.kind().unit()) {
            return view.enemyPage(entry);
        }
        if (entry.kind() == Entry.Kind.CONCEPT) {
            return view.conceptPage(entry);
        }
        return view.gearPage(entry);
    }

    /** The weapon's loop in the test-fire box at the shown level, in the slot it is fitted to (else its group's). */
    private void startTestFire() {
        fire = Optional.empty();
        if (!(page instanceof Pages.GearPage gear) || gear.entry().kind() != Entry.Kind.WEAPON) {
            return;
        }
        LoadoutSlot slot = campaign.loadout().entrySet().stream()
                .filter(fitted -> fitted.getValue().item().equals(gear.entry().slug()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(
                        switch (gear.entry().group()) {
                            case "WINGS" -> LoadoutSlot.LEFT_WING;
                            case "REAR" -> LoadoutSlot.REAR;
                            default -> LoadoutSlot.FRONT;
                        });
        Optional<TestFire.Shown> shown =
                TestFire.of(services.content, slot, gear.entry().slug(), shownLevel);
        if (shown.isEmpty()) {
            return;
        }
        if (testFireView == null) {
            testFireView = new TestFireView(services.sprites, new PodPivots(services.files));
        }
        TestFire loop = new TestFire(services.content, shown.get());
        testFireView.show(loop.sortie(), shown.get().level());
        fire = Optional.of(loop);
    }

    /** Opens entry {@code id} (a link), remembering where it came from for Back. */
    private void follow(String id) {
        Optional<Entry> target = databank.entry(id);
        if (target.isEmpty() || !view.known(target.get())) {
            services.play(Sfx.SHOP_DENIED);
            return;
        }
        Optional<Entry> from = list.entry();
        if (from.isPresent() && from.get().id().equals(id)) {
            return;
        }
        services.play(Sfx.MENU_CONFIRM);
        returnTo = from.map(Entry::id);
        go(target.get());
    }

    private void go(Entry entry) {
        lastShown.put(entry.tab(), entry.id());
        openTab(entry.tab());
    }

    @Override
    public Transition update(float seconds) {
        campaign.play(seconds);
        MenuInput input = services.menu;
        Transition next = input(input);
        if (next != Transition.STAY) {
            return next;
        }
        pictures.update(seconds);
        markRead(seconds);
        runTestFire(seconds);
        return Transition.STAY;
    }

    private Transition input(MenuInput input) {
        Pointer pointer = input.pointer();
        if (input.back() || pointer.clicks(BACK)) {
            if (returnTo.isPresent()) {
                services.play(Sfx.MENU_BACK);
                String id = returnTo.get();
                returnTo = Optional.empty();
                databank.entry(id).ifPresent(this::go);
                return Transition.STAY;
            }
            services.play(Sfx.MENU_BACK);
            close();
            return Transition.BACK;
        }
        if (input.previousTab() || input.nextTab()) {
            Entry.Tab[] tabs = Entry.Tab.values();
            int index = Math.floorMod(tab.ordinal() + (input.nextTab() ? 1 : -1), tabs.length);
            returnTo = Optional.empty();
            services.play(Sfx.MENU_MOVE);
            openTab(tabs[index]);
            return Transition.STAY;
        }
        int direction = input.up() ? -1 : input.down() ? 1 : 0;
        if (direction != 0) {
            if (list.move(direction)) {
                services.play(Sfx.MENU_MOVE);
                returnTo = Optional.empty();
                showSelected();
            }
            return Transition.STAY;
        }
        if (input.left() || input.right()) {
            step(input.right() ? 1 : -1);
            return Transition.STAY;
        }
        if (input.confirm()) {
            if (leadsToSuite()) {
                follow(EntryPage.SUITE);
            }
            return Transition.STAY;
        }
        mouse(pointer);
        return Transition.STAY;
    }

    /** Whether Enter on the page opens the suite's entry: an enemy page with a locked field or the callout. */
    private boolean leadsToSuite() {
        return page instanceof Pages.EnemyPage enemy
                && (enemy.suiteCallout() || enemy.fields().stream().anyMatch(field -> field.locked(enemy.sensor())));
    }

    /** LEFT / RIGHT: a gear page's shown level, L1 to its last. */
    private void step(int direction) {
        if (!(page instanceof Pages.GearPage gear) || gear.items().size() != 1) {
            return;
        }
        int levels = gear.items().getFirst().maxLevel();
        int next = Math.clamp(shownLevel + direction, 1, levels);
        if (next != shownLevel) {
            shownLevel = next;
            services.play(Sfx.MENU_MOVE);
            startTestFire();
        }
    }

    private void mouse(Pointer pointer) {
        for (Entry.Tab candidate : Entry.Tab.values()) {
            if (pointer.clicks(tab(candidate.ordinal())) && candidate != tab) {
                services.play(Sfx.MENU_MOVE);
                returnTo = Optional.empty();
                openTab(candidate);
                return;
            }
        }
        if (pointer.clicks(BADGE)) {
            follow(EntryPage.SUITE);
            return;
        }
        if (new Box(DatabankList.X, DatabankList.Y, DatabankList.WIDTH, DatabankList.HEIGHT)
                        .contains(pointer.x(), pointer.y())
                && pointer.wheel() != 0) {
            list.wheel(pointer.wheel());
        }
        for (int shown = 0; shown < DatabankList.VISIBLE; shown++) {
            int index = list.top() + shown;
            if (!list.selectable(index)) {
                continue;
            }
            Box row = DatabankList.row(shown);
            if ((pointer.clicks(row) || pointer.hovers(row)) && index != list.selected()) {
                services.play(Sfx.MENU_MOVE);
                returnTo = Optional.empty();
                list.select(index);
                showSelected();
                return;
            }
        }
        if (pointer.clicked()) {
            for (Link link : links) {
                if (pointer.clicks(link.box())) {
                    follow(link.id());
                    return;
                }
            }
        }
    }

    /** The entry shown long enough counts as opened: its {@code NEW} tag goes (design/ui/databank, The record). */
    private void markRead(float seconds) {
        Optional<Entry> entry = list.entry();
        if (entry.isEmpty() || !view.fresh(entry.get())) {
            return;
        }
        shownFor += seconds;
        if (shownFor < READ_SECONDS) {
            return;
        }
        campaign.openedEntry(entry.get().id());
        view = databank.view(campaign);
        int top = list.top();
        list = new DatabankList(view.list(tab));
        list.start(entry.map(Entry::id));
        list.scrollTo(top);
    }

    private void runTestFire(float seconds) {
        if (fire.isEmpty()) {
            return;
        }
        int steps = clock.advance(seconds);
        for (int i = 0; i < steps; i++) {
            if (fire.get().step()) {
                testFireView.restart();
            }
            testFireView.stepped(fire.get().sortie());
        }
    }

    /** From the main menu: the read entries go back into the save's slot (only its databank changes). */
    private void close() {
        if (menuSave.isEmpty()) {
            return;
        }
        SaveSlots.Entry.Saved saved = menuSave.get();
        DatabankRecord record = campaign.databank();
        if (record.equals(saved.save().databank()) || services.debugRun()) {
            return;
        }
        try {
            services.saves.write(saved.slot(), saved.save().withDatabank(record));
        } catch (IOException | RuntimeException e) {
            LOG.log(
                    Level.WARNING,
                    "could not keep the databank's read entries in slot "
                            + saved.slot().index(),
                    e);
        }
    }

    // ---- Layout --------------------------------------------------------------------------------------------------

    /** A tab's box under the top bar. */
    public static Box tab(int index) {
        return new Box(BAR_X + index * (TAB_WIDTH + TAB_GAP), TAB_Y, TAB_WIDTH, TAB_HEIGHT);
    }

    /** A tab's label: {@code ENEMIES 63}. */
    static String tabLabel(Entry.Tab tab, int known) {
        return tab.name() + " " + known;
    }

    /** The top bar's mission line: {@code CAMPAIGN - BEFORE L40 - HONOUR GUARD}. */
    static String missionLine(Content content, int next) {
        return content.levelKey(next)
                .map(key -> String.format(Locale.ROOT, "CAMPAIGN - BEFORE L%02d - %s", next, Content.levelName(key)))
                .orElse(String.format(Locale.ROOT, "CAMPAIGN - BEFORE L%02d", next))
                .toUpperCase(Locale.ROOT);
    }

    /** The sensor badge's two lines: the level the entries use and what it shows. */
    static List<String> badge(int sensor, boolean owned) {
        String level = sensor == 0 ? "NONE" : "L" + sensor + (owned ? "" : " (EASY)");
        String shows =
                switch (sensor) {
                    case 0 -> "SEE SHIP TAB";
                    case 1 -> "SHOWS CLASS AND LAYER";
                    case 2 -> "SHOWS THE NUMBERS";
                    default -> "SHOWS EVERYTHING";
                };
        return List.of("SENSOR SUITE  " + level, shows);
    }

    /** The key hints for the page shown. */
    String hints() {
        StringBuilder hints = new StringBuilder("Q/E TAB    UP/DOWN ENTRY");
        if (page instanceof Pages.GearPage gear && gear.items().size() == 1) {
            hints.append("    LEFT/RIGHT LEVEL");
        }
        if (leadsToSuite()) {
            hints.append("    ENTER SENSOR SUITE");
        }
        hints.append(returnTo.isPresent() ? "    ESC RETURN" : "    ESC BACK");
        return hints.toString();
    }

    // ---- Drawing -------------------------------------------------------------------------------------------------

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        backdrop.accept(batch);
        glass.dim(batch, DIMMED);
        drawTopBar(batch, glass);
        drawTabs(batch, glass);
        drawList(batch, glass);
        drawPage(batch, glass);
        glass.hints(batch, hints());
    }

    private void drawTopBar(SpriteBatch batch, Glass glass) {
        glass.panel(batch, BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 0.9f);
        glass.shadowed(batch, glass.fonts.heading, "DATABANK", Glass.WHITE, BAR_X + 10, BAR_Y + 5);
        int next = campaign.nextLevel();
        glass.shadowed(batch, glass.fonts.label, missionLine(services.content, next), Glass.CYAN, MISSION_X, 13);
        String act = services.content
                        .actOf(next)
                        .map(entry -> entry.getValue().titleCard().act() + " - "
                                + entry.getValue().titleCard().name() + " - ")
                        .orElse("")
                + campaign.difficulty().name();
        glass.shadowed(batch, glass.fonts.label, act.toUpperCase(Locale.ROOT), Glass.LABEL, MISSION_X, 27);
        glass.inset(batch, BADGE.x(), BADGE.y(), BADGE.width(), BADGE.height());
        List<String> lines = badge(view.sensorLevel(), view.ownsSuite());
        Color colour = view.sensorLevel() == 0 ? Glass.ALERT : Glass.GREEN;
        glass.shadowed(batch, glass.fonts.label, lines.get(0), colour, BADGE.x() + 8, BADGE.y() + 5);
        glass.shadowed(batch, glass.fonts.label, lines.get(1), Glass.LABEL, BADGE.x() + 8, BADGE.y() + 18);
        glass.chip(batch, glass.fonts.label, "< BACK", BACK.x(), BACK.y(), BACK.width(), BACK.height(), false, true);
    }

    private void drawTabs(SpriteBatch batch, Glass glass) {
        for (Entry.Tab candidate : Entry.Tab.values()) {
            Box box = tab(candidate.ordinal());
            glass.tab(
                    batch,
                    glass.fonts.body,
                    tabLabel(candidate, view.knownCount(candidate)),
                    box.x(),
                    box.y(),
                    box.width(),
                    box.height(),
                    candidate == tab);
        }
        Box last = tab(Entry.Tab.values().length - 1);
        glass.shadowed(batch, glass.fonts.label, "Q/E", Glass.DIM, last.x() + last.width() + 10, TAB_Y + 8);
    }

    private void drawList(SpriteBatch batch, Glass glass) {
        glass.panel(batch, DatabankList.X, DatabankList.Y, DatabankList.WIDTH, DatabankList.HEIGHT, 0.88f);
        glass.shadowed(batch, glass.fonts.label, list.count(), Glass.LABEL, DatabankList.TEXT_X, DatabankList.COUNT_Y);
        List<DatabankList.Line> lines = list.lines();
        for (int shown = 0; shown < DatabankList.VISIBLE && list.top() + shown < lines.size(); shown++) {
            int index = list.top() + shown;
            int y = DatabankList.lineY(shown);
            switch (lines.get(index)) {
                case DatabankList.Line.Header header ->
                    glass.header(batch, header.text(), DatabankList.X + 8, DatabankList.RIGHT, y + 2);
                case DatabankList.Line.Row row -> drawRow(batch, glass, row.row(), index == list.selected(), shown);
            }
        }
        glass.scrollMarkers(batch, DatabankList.RIGHT + 8, DatabankList.FIRST_Y, list.above(), list.below());
    }

    private void drawRow(SpriteBatch batch, Glass glass, Pages.Row row, boolean selected, int shown) {
        BitmapFont font = glass.fonts.label;
        int y = DatabankList.lineY(shown);
        if (selected) {
            Box box = DatabankList.row(shown);
            glass.selection(batch, box.x(), box.y(), box.width(), box.height());
        }
        if (!row.known()) {
            glass.shadowed(batch, font, "???", Glass.DIM, DatabankList.TEXT_X, y);
            glass.right(batch, font, row.right(), Glass.DIM, DatabankList.RIGHT, y);
            return;
        }
        Entry entry = row.entry().orElseThrow();
        int tagRight = DatabankList.RIGHT - 7 * Fonts.LABEL_CELL;
        List<String> tags = new ArrayList<>();
        entry.tag().ifPresent(tags::add);
        if (row.fresh()) {
            tags.add("NEW");
        }
        int x = tagRight;
        for (int i = tags.size() - 1; i >= 0; i--) {
            String tag = tags.get(i);
            int width = tag.length() * Fonts.LABEL_CELL + 6;
            x -= width;
            glass.tag(batch, tag, EntryPage.tagColour(tag), x, y - 2, width, 12);
            x -= 4;
        }
        Color colour = selected ? Glass.AMBER : Glass.WHITE;
        glass.shadowed(
                batch, font, EntryPage.listName(entry), colour, DatabankList.TEXT_X, y, x - DatabankList.TEXT_X - 4);
        Color right = selected
                ? Glass.AMBER
                : switch (row.right()) {
                    case "FITTED", "OWNED" -> Glass.GREEN;
                    case "SHOP" -> Glass.LABEL;
                    default -> Glass.CYAN;
                };
        glass.right(batch, font, row.right(), right, DatabankList.RIGHT, y);
    }

    private void drawPage(SpriteBatch batch, Glass glass) {
        glass.panel(batch, EntryPage.X, EntryPage.Y, EntryPage.WIDTH, EntryPage.HEIGHT, 0.88f);
        links.clear();
        ScreenCanvas canvas = new ScreenCanvas(batch, glass);
        switch (page) {
            case Pages.EnemyPage enemy -> EntryPage.enemy(canvas, enemy);
            case Pages.GearPage gear -> EntryPage.gear(canvas, gear, shownLevel, fire.isPresent());
            case Pages.ConceptPage concept -> EntryPage.concept(canvas, concept, EntryPictures.conceptPlate(concept));
            case null, default ->
                glass.shadowed(
                        batch,
                        glass.fonts.label,
                        "NOTHING KNOWN IN THIS TAB YET.",
                        Glass.LABEL,
                        EntryPage.INNER,
                        EntryPage.HEADER_Y);
        }
    }

    /** The page drawn on the screen. */
    private final class ScreenCanvas implements EntryPage.Canvas {
        private final SpriteBatch batch;
        private final Glass glass;

        ScreenCanvas(SpriteBatch batch, Glass glass) {
            this.batch = batch;
            this.glass = glass;
        }

        @Override
        public void text(String text, Color colour, int x, int y) {
            glass.shadowed(batch, glass.fonts.label, text, colour, x, y);
        }

        @Override
        public void name(String text, int x, int y, boolean body) {
            glass.shadowed(batch, body ? glass.fonts.body : glass.fonts.heading, text, Glass.WHITE, x, y);
        }

        @Override
        public void header(String text, int x, int right, int y) {
            glass.header(batch, text, x, right, y);
        }

        @Override
        public void tag(String text, Color colour, int x, int y, int width) {
            glass.tag(batch, text, colour, x, y, width, 12);
        }

        @Override
        public void picture(int x, int y, int size) {
            glass.inset(batch, x, y, size, size);
            pictures.draw(batch, x, y, size);
        }

        @Override
        public void portrait(int x, int y, int size) {
            glass.frame(batch, x - 3, y - 3, size + 6, size + 6);
            batch.draw(
                    Portraits.radio(services.sprites, EntryPage.VARGA, "neutral")
                            .first(),
                    x,
                    PixelScreen.HEIGHT - y - size,
                    size,
                    size);
        }

        @Override
        public void inset(int x, int y, int width, int height) {
            glass.inset(batch, x, y, width, height);
        }

        @Override
        public void chip(String text, int x, int y, int width, int height, boolean on) {
            glass.chip(batch, glass.fonts.label, text, x, y, width, height, on, true);
        }

        @Override
        public void lit(int x, int y, int width, int height) {
            glass.selection(batch, x, y, Math.max(100, width), height);
        }

        @Override
        public void testFire(int x, int y, int width, int height) {
            glass.inset(batch, x, y, width, height);
            fire.ifPresent(loop -> testFireView.draw(
                    batch, loop.sortie(), clock.alpha(), x, y, width, height, TestFire.SHIP_X, TestFire.SHIP_Y));
            String label = "TEST FIRE L" + shownLevel;
            glass.shadowed(batch, glass.fonts.label, label, Glass.AMBER, x + 4, y + 3);
        }

        @Override
        public void link(Box box, String id) {
            links.add(new Link(box, id));
        }
    }

    /** For tests: the tab shown and its list. */
    Entry.Tab shownTab() {
        return tab;
    }

    @Override
    public void dispose() {
        pictures.dispose();
        if (testFireView != null) {
            testFireView.dispose();
        }
    }
}
