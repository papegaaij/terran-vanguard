package vanguard.game.hangar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.EnemyData;
import vanguard.content.LevelData.Entry;
import vanguard.content.campaign.Intel;
import vanguard.content.campaign.Intel.Field;
import vanguard.game.render.PixelScreen;
import vanguard.game.render.Portraits;
import vanguard.game.render.Sprites;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Speaker;
import vanguard.game.ui.Words;

/**
 * The hangar's intel panel (right, design/ui/hangar, Intel): the speaker of the level's hangar
 * teaser (Dr. Varga) with her 72×72 radio portrait and the teaser beside it, the level's name and
 * the sensor level, and the threat profile's fields; a field the sensor level does not show names
 * the level that would (design/player/systems, Sensor levels and hangar intel); from sensor L2 the
 * enemy types show their sensor portraits, a boss its silhouette and a set piece its silhouette as
 * an unknown contact of its size tier (tools/art/intel.py); M7 part E: a unit kept {@code
 * unclassified_until_flown} shows, until the campaign has flown its first level, as an "unclassified" silhouette of
 * its size and layer ({@code intel/unclassified-<slug>}) named by them alone (round 36 Q.22h). Varga's line for the sensor level (one
 * per level, the vaguer the lower the sensor) closes the panel, its last line at the panel's foot.
 * Without a built next level the panel says so.
 *
 * <p>The panel lays itself out on a {@link Canvas}: the screen, or a test's measure of where every
 * part lands.
 */
final class IntelPanel {
    static final int X = 668;
    static final int Y = 52;
    static final int WIDTH = 276;
    static final int HEIGHT = 444;

    private static final int INNER = X + 8;
    private static final int RIGHT = X + WIDTH - 8;
    private static final int VALUE_X = X + 86;
    /** The teaser's speaker: the radio portrait (tools/art/portraits.py), and its name and lines beside it. */
    private static final int SPEAKER_PORTRAIT = 72;

    private static final int TEXT_X = INNER + SPEAKER_PORTRAIT + 10;
    /** The fields' rows, a line apart, when they fit above Varga's quote. */
    private static final int STEP = 14;
    /** The label font's lines, the teaser's and Varga's quote's. */
    static final int LINE = 12;
    /** The top of the quote's last line: this far above the panel's bottom edge. */
    static final int QUOTE_FOOT = 10;
    /** The label font's ink below a line's top, with its shadow. */
    private static final int LABEL_INK = 10;

    private static final int VALUE_CHARS = (RIGHT - VALUE_X) / 8;
    /** The characters of a line of Varga's quote, across the panel. */
    private static final int QUOTE_CHARS = (RIGHT - INNER) / 8;
    /** The sensor L2 pictures (tools/art/intel.py): enemy portraits, and a boss's silhouette. */
    private static final int PORTRAIT = 30;

    private static final int SILHOUETTE = 40;
    /** M7 part H (H.7e): the boss's silhouette on the wave strip at its arrival, px square. */
    static final int STRIP_SILHOUETTE = 14;
    /** M8 part B: the crossbar of a target site's "T" on the wave strip, px. */
    static final int TARGET_BAR = 6;
    /** Step GI1: the ridge pass's band on the wave strip, a muted sand (the Red Dust's ground, dimmed to the glass). */
    static final Color RIDGE_SAND = new Color(132 / 255f, 106 / 255f, 70 / 255f, 1);
    /**
     * M8 part C: the waterline's marks (Level 23's descent): the water from a waterline on as a dim teal band under the
     * waves (to the next waterline or the strip's end: dry behind, water ahead), the line itself a bright teal tick.
     */
    static final Color WATER_BAND = new Color(18 / 255f, 70 / 255f, 92 / 255f, 1);

    static final Color WATERLINE = new Color(70 / 255f, 210 / 255f, 230 / 255f, 1);
    /**
     * M8 part D: a rising unit's wave (Level 24's three Abyss Rays): a violet tick the strip's full depth with a pixel
     * above (the Ray's violet eye spots; it rises from below, so its mark reaches up).
     */
    static final Color RAY_VIOLET = new Color(176 / 255f, 110 / 255f, 236 / 255f, 1);
    /**
     * M8 part E (design/campaign Level 25, Threat profile at L3: "the side caves' entry points"): a wave out of a side
     * cave as a pale slate tick over the strip's upper half and a pixel above it (it comes out of the wall, not in from
     * an edge).
     */
    static final Color CAVE_SLATE = new Color(168 / 255f, 192 / 255f, 210 / 255f, 1);
    /**
     * M8 part F (design/campaign Level 26, Threat profile at L3: "the scarp's nest positions on the threat arrows"): a
     * wall nest's wave in the dark as a Siren-teal tick (the sac's glow, the only thing of it that shows).
     */
    static final Color NEST_TEAL = new Color(40 / 255f, 230 / 255f, 170 / 255f, 1);
    /**
     * M8 part G (design/campaign Level 27, Threat profile at L3: "the timeline strip marks the three Hunter columns"): a
     * column from the rear as a hull-grey tick the strip's full depth and a pixel below it (it rises from below).
     */
    static final Color HULL_GREY = new Color(150 / 255f, 158 / 255f, 166 / 255f, 1);
    /**
     * M8 part H (design/campaign Level 28, Threat profile at L3): the Choir's song on the strip, a pale lime tick (the
     * Vrell's lime) over the strip's upper half, two pixels above it and a pixel's gap, then a dot (a sung note).
     */
    static final Color CHOIR_LIME = new Color(214 / 255f, 236 / 255f, 120 / 255f, 1);

    private static final int PICTURE_GAP = 6;
    /** The characters of a contact's label beside its silhouette. */
    private static final int CONTACT_CHARS = (RIGHT - SILHOUETTE - PICTURE_GAP - VALUE_X) / 8;
    /** How many enemy portraits fit the value column. */
    private static final int PORTRAITS = (RIGHT - VALUE_X + PICTURE_GAP) / (PORTRAIT + PICTURE_GAP);

    /** Where the panel's parts go, each with its top left in screen px from the top left. */
    interface Canvas {
        /** A line of text in the 8x12 label font, or the 10x20 body font. */
        void text(String text, Color colour, int x, int y, boolean body);

        /** The teaser speaker's portrait in its frame, {@code size} px square. */
        void portrait(BriefingPage teaser, int x, int y, int size);

        /** A sensor picture ({@code intel/...}) in an inset, {@code size} px square. */
        void picture(String name, int x, int y, int size);

        /**
         * M7 part H: a sensor picture ({@code intel/...}) scaled to {@code size} px square with no inset, its top left
         * at ({@code x}, {@code y}): a mark on the wave strip.
         */
        default void mark(String name, float x, int y, int size) {
            picture(name, Math.round(x), y, size);
        }

        /** The sensor level's chip. */
        void chip(String text, int x, int y, int width, int height);

        /** A lit bar cell, a recessed inset, or a flat fill. */
        void bar(Color colour, int x, int y, int width, int height);

        void inset(int x, int y, int width, int height);

        void fill(Color colour, float x, int y, float width, int height);

        /** The quote's lines, which close the panel. */
        default void quote(String text, int x, int y) {
            text(text, Glass.CYAN, x, y, false);
        }
    }

    private final Glass glass;
    private final Sprites sprites;

    IntelPanel(Glass glass, Sprites sprites) {
        this.glass = glass;
        this.sprites = sprites;
    }

    /**
     * @param teaser the level's hangar teaser, with its intel
     * @param unclassified M7 part E: the enemy types still unclassified, by name, with what the intel calls them
     *     ({@link #unclassified})
     * @param limited M8 part A: the fitted special the level forbids, with its reason ({@link #limited})
     */
    void draw(
            SpriteBatch batch,
            int number,
            Optional<Intel> intel,
            Optional<BriefingPage> teaser,
            String levelKey,
            Map<String, String> unclassified,
            Optional<String> limited) {
        glass.panel(batch, X, Y, WIDTH, HEIGHT, 0.86f);
        glass.header(batch, String.format(Locale.ROOT, "INTEL - L%02d", number), INNER, RIGHT, Y + 10);
        Canvas canvas = new Screen(batch);
        if (intel.isEmpty() || teaser.isEmpty()) {
            canvas.text(String.format(Locale.ROOT, "MISSION %02d", number), Glass.CYAN, INNER, Y + 34, false);
            canvas.text("NO INTEL YET: THE MISSION", Glass.LABEL, INNER, Y + 52, false);
            canvas.text("FOLLOWS IN A LATER RELEASE.", Glass.LABEL, INNER, Y + 64, false);
            return;
        }
        layout(canvas, intel.get(), teaser.get(), levelKey, unclassified, limited);
    }

    /** M7 part E: an unclassified unit's sensor picture: its silhouette of its size and layer (tools/art/intel.py). */
    static final String UNCLASSIFIED_PICTURE = "intel/unclassified-";

    /**
     * M7 part E (round 36 Q.22h): of {@code enemies} (by name) the ones whose stat block keeps them {@code
     * unclassified_until_flown} while the campaign, whose next level is {@code nextLevel}, has not flown their first
     * level (a replay hangar's campaign has flown the level it replays), each with what the intel calls it instead:
     * {@code Unclassified small air}.
     */
    static Map<String, String> unclassified(Content content, List<String> enemies, int nextLevel) {
        Map<String, String> found = new java.util.LinkedHashMap<>();
        for (EnemyData enemy : content.enemies().values()) {
            if (enemy.unclassifiedUntilFlown() && nextLevel <= enemy.firstLevel() && enemies.contains(enemy.name())) {
                found.put(
                        enemy.name(),
                        "Unclassified " + enemy.tier().name().toLowerCase(Locale.ROOT) + " " + enemy.layer());
            }
        }
        return Map.copyOf(found);
    }

    /** M7 part F: the level's enemy types (by name) with its boss, which may be kept unclassified too. */
    static List<String> withBoss(Intel level) {
        String boss = level.profile().boss();
        if (boss.equals("none") || level.enemies().contains(boss)) {
            return level.enemies();
        }
        List<String> all = new ArrayList<>(level.enemies());
        all.add(boss);
        return all;
    }

    /** The sensor level's chip beside the level's name. */
    private static final int CHIP_WIDTH = 84;
    /** The body font's cell, px; the label font's is 8. */
    private static final int BODY_CELL = 10;
    /** The least gap between the level's name and the chip. */
    private static final int NAME_GAP = 4;

    /** Whether the level's name fits beside the sensor chip in the body font. */
    static boolean nameFits(String name) {
        return INNER + BODY_CELL * name.length() + NAME_GAP <= RIGHT - CHIP_WIDTH;
    }

    /**
     * The intel on a level with its teaser, laid out on {@code canvas}. The fields' rows run {@link #STEP}
     * apart; on a level whose fields would run into Varga's quote at the foot (Level 14's eight enemy types
     * at sensor L2 after the Act 2 wave rework), the rows close up a pixel at a time to {@link #LINE}, and
     * if that is not enough either, the enemy names end on "+N MORE" in as many lines as fit.
     */
    static void layout(Canvas canvas, Intel level, BriefingPage teaser, String levelKey) {
        layout(canvas, level, teaser, levelKey, Map.of());
    }

    /** As above, with the enemy types still unclassified ({@link #unclassified}) as their silhouettes. */
    static void layout(
            Canvas canvas, Intel level, BriefingPage teaser, String levelKey, Map<String, String> unclassified) {
        layout(canvas, level, teaser, levelKey, unclassified, Optional.empty());
    }

    /**
     * As above; M8 part A: {@code limited}, the fitted special the level forbids with its reason ({@link #limited}),
     * makes the specials row a {@value #SPECIAL_LIMITED} warning from sensor L1.
     */
    static void layout(
            Canvas canvas,
            Intel level,
            BriefingPage teaser,
            String levelKey,
            Map<String, String> unclassified,
            Optional<String> limited) {
        List<String> lines = quote(level);
        int quote = Y + HEIGHT - QUOTE_FOOT - LINE * (lines.size() - 1);
        fields(
                canvas,
                level,
                teaser,
                levelKey,
                rows(level, teaser, levelKey, quote, unclassified, limited),
                unclassified,
                limited);
        for (String part : lines) {
            canvas.quote(part, INNER, quote);
            quote += LINE;
        }
    }

    /** The fields' row spacing, and the most lines of enemy names. */
    private record Rows(int step, int names) {}

    /** Draws nothing: the fields are measured by where their last row lands. */
    private static final Canvas MEASURE = new Canvas() {
        @Override
        public void text(String text, Color colour, int x, int y, boolean body) {}

        @Override
        public void portrait(BriefingPage teaser, int x, int y, int size) {}

        @Override
        public void picture(String name, int x, int y, int size) {}

        @Override
        public void chip(String text, int x, int y, int width, int height) {}

        @Override
        public void bar(Color colour, int x, int y, int width, int height) {}

        @Override
        public void inset(int x, int y, int width, int height) {}

        @Override
        public void fill(Color colour, float x, int y, float width, int height) {}
    };

    /** The roomiest rows whose last one ends above the quote's top line at {@code quote}. */
    private static Rows rows(
            Intel level,
            BriefingPage teaser,
            String levelKey,
            int quote,
            Map<String, String> unclassified,
            Optional<String> limited) {
        for (int step = STEP; step >= LINE; step--) {
            Rows rows = new Rows(step, Integer.MAX_VALUE);
            if (fields(MEASURE, level, teaser, levelKey, rows, unclassified, limited) <= quote) {
                return rows;
            }
        }
        for (int names = enemyNames(shown(level.enemies(), unclassified), Integer.MAX_VALUE)
                                .size()
                        - 1;
                names > 1;
                names--) {
            Rows rows = new Rows(LINE, names);
            if (fields(MEASURE, level, teaser, levelKey, rows, unclassified, limited) <= quote) {
                return rows;
            }
        }
        return new Rows(LINE, 1);
    }

    /** The teaser, the level's name and the fields; returns the bottom of the last row's ink. */
    private static int fields(
            Canvas canvas,
            Intel level,
            BriefingPage teaser,
            String levelKey,
            Rows rows,
            Map<String, String> unclassified,
            Optional<String> limited) {
        int step = rows.step();
        int portraitY = Y + 26;
        canvas.portrait(teaser, INNER, portraitY, SPEAKER_PORTRAIT);
        canvas.text(Speaker.plate(teaser.speaker()), Glass.AMBER, TEXT_X, portraitY, false);
        canvas.text(Speaker.role(teaser.speaker()), Glass.CYAN, TEXT_X, portraitY + 13, false);
        int line = portraitY + 30;
        for (String text : Words.wrap(Names.of("\"" + teaser.line() + "\""), (RIGHT - TEXT_X) / 8)) {
            canvas.text(text, Glass.BODY, TEXT_X, line, false);
            line += LINE;
        }
        int y = Math.max(portraitY + SPEAKER_PORTRAIT, line) + 10;
        // M5 part D: a name too long for the body font beside the chip (EVACUATION CORRIDOR) takes the
        // label font, a line's height lower so it stays level with the chip.
        String name = Names.of(Content.levelName(levelKey));
        boolean body = nameFits(name);
        canvas.text(name, Glass.WHITE, INNER, body ? y : y + 2, body);
        String sensor = level.sensor() == 0 ? "NO SENSOR" : "SENSOR L" + level.sensor();
        canvas.chip(sensor, RIGHT - CHIP_WIDTH, y + 2, CHIP_WIDTH, 16);
        y += 26;
        y = field(
                canvas,
                level,
                Field.SETTING,
                "SETTING",
                List.of(Names.of(level.profile().setting())),
                y,
                step);
        y = field(
                canvas,
                level,
                Field.LAYERS,
                "LAYERS",
                List.of(joined(level.profile().layers())),
                y,
                step);
        if (level.shows(Field.DIRECTIONS)) {
            List<String> shares = level.directions().entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .map(entry -> direction(entry.getKey()) + " " + entry.getValue() + " %")
                    .toList();
            y = field(canvas, level, Field.DIRECTIONS, "FROM", packed(shares), y, step);
        } else {
            y = field(
                    canvas,
                    level,
                    Field.MAIN_DIRECTION,
                    "FROM",
                    List.of(direction(level.mainDirection()) + " (MAIN)"),
                    y,
                    step);
        }
        y = density(canvas, level, y, step);
        y = field(
                canvas,
                level,
                Field.HAZARDS,
                "HAZARDS",
                // M7 part F: wrapped to the value column (Level 19's "furnace vents, rear-firing turrets").
                Words.wrap(joinedOrNone(level.profile().hazards()), VALUE_CHARS),
                y,
                step);
        if (level.profile().objective().isPresent()) {
            y = field(
                    canvas,
                    level,
                    Field.OBJECTIVE,
                    "OBJECTIVE",
                    // M7 part H: wrapped to the value column (Level 21's "destroy the Dust Colossus").
                    Words.wrap(Names.of(level.profile().objective().get()), VALUE_CHARS),
                    y,
                    step);
        }
        if (limited.isPresent() && level.sensor() >= LIMITED_SENSOR) {
            // M8 part A (design/player/specials, Setting restrictions): the fitted special the level forbids, in
            // advance, from the Sensor suite's first level.
            canvas.text("SPECIALS", Glass.LABEL, INNER, y, false);
            canvas.text(SPECIAL_LIMITED, Glass.ALERT, VALUE_X, y, false);
            y += step;
            for (String part : Words.wrap(limited.get(), VALUE_CHARS)) {
                canvas.text(part, Glass.ALERT, VALUE_X, y, false);
                y += step;
            }
        } else {
            y = field(
                    canvas,
                    level,
                    Field.SPECIALS,
                    "SPECIALS",
                    List.of(level.profile().specials().map(Names::of).orElse("NO LIMIT")),
                    y,
                    step);
        }
        y = boss(canvas, level, y, step, unclassified);
        y = enemies(canvas, level, y, step, rows.names(), unclassified);
        y = field(canvas, level, Field.SECRETS, "SECRETS", List.of(Integer.toString(level.secrets())), y, step);
        y = field(
                canvas,
                level,
                Field.TRAITS,
                "RECOMMEND",
                List.of(joined(level.profile().traits())),
                y,
                step);
        return waves(canvas, level, y, step, unclassified) - step + LABEL_INK;
    }

    /** M8 part A: the specials row's warning for a fitted special the next level forbids. */
    static final String SPECIAL_LIMITED = "SPECIAL LIMITED";

    /** M8 part A: the sensor level that shows {@link #SPECIAL_LIMITED} (below it Varga's line says so). */
    static final int LIMITED_SENSOR = 1;

    /**
     * M8 part A (design/player/specials, Setting restrictions): the fitted special {@code special} where level {@code
     * levelKey} (any of its sections) forbids it, with its HUD reason word: {@code AIRSTRIKE: UNDER ICE}; empty when it
     * is allowed throughout, or none is fitted. Only the fitted special is named (pending the user's review): an
     * owned Sonar Pulse in the inventory would otherwise mark every dry level.
     */
    static Optional<String> limited(Content content, String levelKey, Optional<String> special) {
        if (special.isEmpty() || levelKey.isEmpty()) {
            return Optional.empty();
        }
        vanguard.content.LevelData level = content.level(levelKey);
        String name = special.get();
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.forbiddenSpecials(i, content.specials()).contains(name)) {
                return Optional.of(Names.of(name)
                        + content.specials()
                                .reason(name)
                                .map(reason -> ": " + reason)
                                .orElse(""));
            }
        }
        return Optional.empty();
    }

    /** Varga's line for the intel's sensor level, in quotes, wrapped to the panel's width. */
    static List<String> quote(Intel intel) {
        return Words.wrap(Names.of("\"" + intel.varga() + "\""), QUOTE_CHARS);
    }

    private static int field(
            Canvas canvas, Intel intel, Field field, String label, List<String> values, int y, int step) {
        canvas.text(label, Glass.LABEL, INNER, y, false);
        if (!intel.shows(field)) {
            canvas.text("-- SENSOR L" + field.sensor(), Glass.DIM, VALUE_X, y, false);
            return y + step;
        }
        for (String value : values) {
            canvas.text(value, Glass.CYAN, VALUE_X, y, false);
            y += step;
        }
        return y;
    }

    /** The enemy types' sensor portraits in a row, their names below in at most {@code lines} lines. */
    private static int enemies(
            Canvas canvas, Intel intel, int y, int step, int lines, Map<String, String> unclassified) {
        List<String> names = enemyNames(shown(intel.enemies(), unclassified), lines);
        if (!intel.shows(Field.ENEMIES)) {
            return field(canvas, intel, Field.ENEMIES, "ENEMIES", names, y, step);
        }
        int x = VALUE_X;
        List<String> pictured = pictured(intel.enemies(), unclassified);
        for (String enemy : pictured.subList(0, Math.min(pictured.size(), PORTRAITS))) {
            // M7 part E: an unclassified unit's silhouette instead of its portrait.
            String picture = unclassified.containsKey(enemy) ? UNCLASSIFIED_PICTURE : "intel/";
            canvas.picture(picture + Portraits.slug(enemy), x, y, PORTRAIT);
            x += PORTRAIT + PICTURE_GAP;
        }
        y = field(canvas, intel, Field.ENEMIES, "ENEMIES", names, y + PORTRAIT + PICTURE_GAP, step);
        if (intel.enemies().stream().map(Portraits::slug).anyMatch(LURES::contains)) {
            // M8 part F (design/campaign Level 26, Threat profile at L2): the lure marked as no pickup.
            canvas.text(NOT_A_PICKUP, Glass.ALERT, VALUE_X, y, false);
            y += step;
        }
        if (intel.enemies().stream().map(Portraits::slug).anyMatch(UNMARKED::contains)) {
            // M8 part G (design/campaign Level 27, Threat profile at L2: the Depth Hunter "shown as unmarked grey,
            // human-built"; the value column fits the tag, not the gloss).
            canvas.text(UNMARKED_GREY, Glass.ALERT, VALUE_X, y, false);
            y += step;
        }
        return y;
    }

    /** M8 part G: the units the intel shows as human-built and unmarked (the Depth Hunter), tagged from sensor L2. */
    static final java.util.Set<String> UNMARKED = java.util.Set.of("depth-hunter");

    /** M8 part G: their tag under the names. */
    static final String UNMARKED_GREY = "UNMARKED GREY";

    /** M8 part F: the units whose lure looks like a pickup (the Glow Angler's teal bead), tagged from sensor L2. */
    static final java.util.Set<String> LURES = java.util.Set.of("glow-angler");

    /** M8 part F: the tag under their names. */
    static final String NOT_A_PICKUP = "LURE: NOT A PICKUP";

    /**
     * M7 part E: the enemy types in the order of their pictures: the unclassified ones' silhouettes first (the row
     * shows {@value #PORTRAITS} pictures; the silhouette is the level's warning), then the others in order of
     * appearance.
     */
    private static List<String> pictured(List<String> enemies, Map<String, String> unclassified) {
        if (unclassified.isEmpty()) {
            return enemies;
        }
        List<String> order = new ArrayList<>();
        enemies.stream().filter(unclassified::containsKey).forEach(order::add);
        enemies.stream().filter(name -> !unclassified.containsKey(name)).forEach(order::add);
        return order;
    }

    /** M7 part E: the enemy types' names as the intel shows them: an unclassified one by its size and layer. */
    private static List<String> shown(List<String> enemies, Map<String, String> unclassified) {
        if (unclassified.isEmpty()) {
            return enemies;
        }
        return enemies.stream()
                .map(name -> unclassified.getOrDefault(name, name))
                .toList();
    }

    /**
     * The enemy types' names wrapped to the value column in at most {@code lines} lines: as many as fit
     * and "+N MORE" for the rest when they do not all fit.
     */
    private static List<String> enemyNames(List<String> enemies, int lines) {
        List<String> all = Words.wrap(Names.of(String.join(", ", enemies)), VALUE_CHARS);
        if (all.size() <= lines) {
            return all;
        }
        for (int shown = enemies.size() - 1; shown > 0; shown--) {
            String more = "+" + (enemies.size() - shown) + " MORE";
            List<String> capped =
                    Words.wrap(Names.of(String.join(", ", enemies.subList(0, shown)) + ", " + more), VALUE_CHARS);
            if (capped.size() <= lines) {
                return capped;
            }
        }
        return List.of("+" + enemies.size() + " TYPES");
    }

    /**
     * The boss's name, with its sensor silhouette at the row's right end; below it each set piece as
     * an unknown contact of its size tier ("UNKNOWN HUGE CONTACT") with its silhouette, beside the
     * boss row's {@code NONE} where the level has no boss. Below sensor L2 the row names the level
     * that shows it, and nothing tells of a contact. M7 part F: a boss kept {@code unclassified_until_flown} is named
     * by what the intel calls it instead ("unclassified huge ground", Level 19's walker: unnamed before the fight).
     */
    private static int boss(Canvas canvas, Intel intel, int y, int step, Map<String, String> unclassified) {
        String boss = intel.profile().boss();
        String name = unclassified.getOrDefault(boss, boss);
        int next = field(canvas, intel, Field.BOSS, "BOSS", Words.wrap(Names.of(name), VALUE_CHARS), y, step);
        if (!intel.shows(Field.BOSS)) {
            return next;
        }
        int top = y;
        if (!boss.equals("none")) {
            canvas.picture("intel/boss-" + Portraits.slug(boss), RIGHT - SILHOUETTE, top, SILHOUETTE);
            top += SILHOUETTE + PICTURE_GAP;
            next = top;
        }
        if (!intel.shows(Field.CONTACTS)) {
            return next;
        }
        for (Intel.Contact contact : intel.contacts()) {
            canvas.picture("intel/boss-" + contact.enemy(), RIGHT - SILHOUETTE, top, SILHOUETTE);
            int line = next;
            for (String text : Words.wrap(Names.of(contact.label()), CONTACT_CHARS)) {
                canvas.text(text, Glass.CYAN, VALUE_X, line, false);
                line += step;
            }
            top = Math.max(line, top + SILHOUETTE + PICTURE_GAP);
            next = top;
        }
        return next;
    }

    /** Values side by side, as many to a line as fit the value column. */
    private static List<String> packed(List<String> values) {
        List<String> lines = new ArrayList<>();
        for (String value : values) {
            int last = lines.size() - 1;
            if (last >= 0 && lines.get(last).length() + 2 + value.length() <= VALUE_CHARS) {
                lines.set(last, lines.get(last) + "  " + value);
            } else {
                lines.add(value);
            }
        }
        return lines;
    }

    private static int density(Canvas canvas, Intel intel, int y, int step) {
        if (!intel.shows(Field.DENSITY)) {
            return field(canvas, intel, Field.DENSITY, "DENSITY", List.of(), y, step);
        }
        canvas.text("DENSITY", Glass.LABEL, INNER, y, false);
        int density = intel.profile().density();
        for (int i = 0; i < 5; i++) {
            canvas.bar(i < density ? Glass.AMBER : Glass.UNLIT, VALUE_X + i * 14, y, 12, 8);
        }
        canvas.text(density + "/5", Glass.CYAN, VALUE_X + 76, y, false);
        return y + step;
    }

    private static int waves(Canvas canvas, Intel intel, int y, int step, Map<String, String> unclassified) {
        if (!intel.shows(Field.WAVES)) {
            return field(canvas, intel, Field.WAVES, "WAVES", List.of(), y, step);
        }
        canvas.text("WAVES", Glass.LABEL, INNER, y, false);
        int width = RIGHT - VALUE_X;
        canvas.inset(VALUE_X - 2, y - 2, width + 4, 12);
        // M8 part H (design/campaign Level 28, Threat profile): nothing after the boss's arrival is on the strip (the
        // finale after the Maw, its breakout's waterline included, is a surprise).
        double finale = finale(intel);
        // M5 part G: a defend level's phases as dim bands under the waves (a pixel apart, so the three read
        // as three), a hazard span (the whiteout, the root field) as a pale band over them.
        for (Intel.Mark mark : intel.marks()) {
            if (mark.from() > finale) {
                continue;
            }
            float left = (float) (VALUE_X + width * mark.from() / intel.seconds());
            float right = (float) (VALUE_X + width * mark.to() / intel.seconds());
            switch (mark.kind()) {
                case HAZARD -> canvas.fill(Glass.TRIM_LIGHT, left, y + 7, right - left, 2);
                case PHASE -> canvas.fill(Glass.TRIM, left + 1, y - 1, right - left - 2, 10);
                // M7 part G: a lull (Level 20's upper terrace) as a dim band under the waves, a phase's look.
                case LULL -> canvas.fill(Glass.TRIM, left + 1, y - 1, right - left - 2, 10);
                // Round 44 Q.8 d (step GI1): the Dust Colossus's ridge pass (Levels 20 and 21) as a muted sand band
                // under the waves, from its surfacing to its head off the far edge.
                case RIDGE -> canvas.fill(RIDGE_SAND, left + 1, y - 1, right - left - 2, 10);
                case BOSS, BURROW_FIELD, GATE, CRAWLER, TURN, TARGET, WATERLINE, RAY, CAVE, NEST, COLUMN, CHOIR -> {
                    // drawn over the waves, below
                }
            }
        }
        // M8 part C: the water from each entering waterline (every other one, from the first) to the next or the end.
        List<Intel.Mark> waterlines = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE && mark.from() <= finale)
                .toList();
        for (int w = 0; w < waterlines.size(); w += 2) {
            double to = w + 1 < waterlines.size() ? waterlines.get(w + 1).from() : intel.seconds();
            float left = (float) (VALUE_X + width * waterlines.get(w).from() / intel.seconds());
            float right = (float) (VALUE_X + width * to / intel.seconds());
            canvas.fill(WATER_BAND, left + 1, y - 1, right - left - 2, 10);
        }
        for (double t : intel.waves()) {
            canvas.fill(Glass.AMBER, (float) (VALUE_X + width * t / intel.seconds()), y, 2, 8);
        }
        // Step K10: the boss's arrival as a red tick the strip's full depth, over the waves; M7 part H (the user's
        // round 43 answer H.7e): the boss's sensor silhouette at its time instead, a boss kept unclassified aside.
        String boss = intel.profile().boss();
        boolean silhouette = !boss.equals("none") && !unclassified.containsKey(boss);
        for (Intel.Mark mark : intel.marks()) {
            if (mark.from() > finale) {
                continue;
            }
            if (mark.kind() == Intel.Mark.Kind.BOSS && silhouette) {
                float at = (float) (VALUE_X + width * mark.from() / intel.seconds());
                canvas.mark(
                        "intel/boss-" + Portraits.slug(boss), at + 1 - STRIP_SILHOUETTE / 2f, y - 3, STRIP_SILHOUETTE);
            } else if (mark.kind() == Intel.Mark.Kind.BOSS) {
                canvas.fill(Glass.ALERT, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 1, 2, 10);
            } else if (mark.kind() == Intel.Mark.Kind.BURROW_FIELD) {
                // M7 part B: a Burrower field (Level 15's threat profile) as a green tick the strip's full depth.
                canvas.fill(Glass.GREEN, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 1, 2, 10);
            } else if (mark.kind() == Intel.Mark.Kind.GATE) {
                // M7 part C: a gate of tendril barriers (Level 16's threat profile) as a cyan tick the strip's full
                // depth.
                canvas.fill(Glass.CYAN, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 1, 2, 10);
            } else if (mark.kind() == Intel.Mark.Kind.CRAWLER) {
                // M7 part C: a Threadcrawler as a green tick over the strip's lower half (a field's is full depth).
                canvas.fill(Glass.GREEN, (float) (VALUE_X + width * mark.from() / intel.seconds()), y + 3, 2, 6);
            } else if (mark.kind() == Intel.Mark.Kind.TURN) {
                // M7 part G: the reverse scroll's turn (Level 20) as a white tick the strip's full depth and a pixel
                // above and below it.
                canvas.fill(Glass.WHITE, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 2, 2, 12);
            } else if (mark.kind() == Intel.Mark.Kind.WATERLINE) {
                // M8 part C: the waterline (Level 23's descent) as a bright teal tick a pixel above and below the
                // strip.
                canvas.fill(WATERLINE, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 2, 2, 12);
            } else if (mark.kind() == Intel.Mark.Kind.RAY) {
                // M8 part D: a rising unit's wave (Level 24's Abyss Rays) as a violet tick the strip's full depth and a
                // pixel above it.
                canvas.fill(RAY_VIOLET, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 2, 2, 11);
            } else if (mark.kind() == Intel.Mark.Kind.NEST) {
                // M8 part F: a wall nest's entry in the dark (Level 26's scarp) as a Siren-teal tick the strip's full
                // depth and a pixel below it (it sits on the wall, unseen in the black).
                canvas.fill(NEST_TEAL, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 1, 2, 11);
            } else if (mark.kind() == Intel.Mark.Kind.COLUMN) {
                // M8 part G: a column from below (Level 27's Depth Hunters) as a hull-grey tick the strip's full depth
                // and a pixel below it.
                canvas.fill(HULL_GREY, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 1, 2, 11);
            } else if (mark.kind() == Intel.Mark.Kind.CAVE) {
                // M8 part E: a cave wave's entry (Level 25's side caves) as a slate tick over the strip's upper half
                // and a pixel above it.
                canvas.fill(CAVE_SLATE, (float) (VALUE_X + width * mark.from() / intel.seconds()), y - 2, 2, 7);
            } else if (mark.kind() == Intel.Mark.Kind.CHOIR) {
                // M8 part H: the Choir's song (Level 28's at 97.5 s) as a pale lime tick over the strip's upper half
                // and two pixels above it, a dot over it.
                float at = (float) (VALUE_X + width * mark.from() / intel.seconds());
                canvas.fill(CHOIR_LIME, at, y - 2, 2, 6);
                canvas.fill(CHOIR_LIME, at, y - 5, 2, 2);
            } else if (mark.kind() == Intel.Mark.Kind.TARGET) {
                // M8 part B: a site of the secondary's targets (Level 22's four rigs) as a red "T" over the strip's
                // lower half: its tick and a crossbar on it (the boss's tick is plain and full depth).
                float at = (float) (VALUE_X + width * mark.from() / intel.seconds());
                canvas.fill(Glass.ALERT, at, y + 3, 2, 6);
                canvas.fill(Glass.ALERT, at - TARGET_BAR / 2f + 1, y + 2, TARGET_BAR, 2);
            }
        }
        return y + step;
    }

    /**
     * M8 part H: the time after which the strip marks nothing: the boss's arrival (what follows it, Level 28's finale, is
     * a surprise); infinite without a boss.
     */
    static double finale(Intel intel) {
        double finale = Double.POSITIVE_INFINITY;
        for (Intel.Mark mark : intel.marks()) {
            if (mark.kind() == Intel.Mark.Kind.BOSS) {
                finale = Math.min(finale, mark.from());
            }
        }
        return finale;
    }

    private static String direction(Entry entry) {
        return entry.name();
    }

    private static String joined(List<String> values) {
        return Names.of(String.join(", ", values));
    }

    private static String joinedOrNone(List<String> values) {
        return values.isEmpty() ? "NONE" : joined(values);
    }

    /** The panel drawn with the glass kit. */
    private final class Screen implements Canvas {
        private final SpriteBatch batch;

        Screen(SpriteBatch batch) {
            this.batch = batch;
        }

        @Override
        public void text(String text, Color colour, int x, int y, boolean body) {
            BitmapFont font = body ? glass.fonts.body : glass.fonts.label;
            glass.shadowed(batch, font, text, colour, x, y);
        }

        @Override
        public void portrait(BriefingPage teaser, int x, int y, int size) {
            glass.frame(batch, x - 3, y - 3, size + 6, size + 6);
            batch.draw(
                    Portraits.radio(sprites, teaser.speaker(), teaser.portrait().slug())
                            .first(),
                    x,
                    PixelScreen.HEIGHT - y - size);
        }

        @Override
        public void picture(String name, int x, int y, int size) {
            glass.inset(batch, x - 1, y - 1, size + 2, size + 2);
            batch.draw(sprites.region(name), x, PixelScreen.HEIGHT - y - size);
        }

        @Override
        public void mark(String name, float x, int y, int size) {
            batch.draw(sprites.region(name), x, PixelScreen.HEIGHT - y - size, size, size);
        }

        @Override
        public void chip(String text, int x, int y, int width, int height) {
            glass.chip(batch, text, x, y, width, height, false);
        }

        @Override
        public void bar(Color colour, int x, int y, int width, int height) {
            glass.bar(batch, colour, x, y, width, height);
        }

        @Override
        public void inset(int x, int y, int width, int height) {
            glass.inset(batch, x, y, width, height);
        }

        @Override
        public void fill(Color colour, float x, int y, float width, int height) {
            glass.fill(batch, colour, x, y, width, height);
        }
    }
}
