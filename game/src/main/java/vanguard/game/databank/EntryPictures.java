package vanguard.game.databank;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.EnemyData;
import vanguard.content.Orientation;
import vanguard.content.campaign.Catalogue;
import vanguard.content.databank.Entry;
import vanguard.content.databank.Pages;
import vanguard.game.hangar.ItemIcons;
import vanguard.game.render.FlashShader;
import vanguard.game.render.PixelScreen;
import vanguard.game.render.Sprites;

/**
 * Step DB2 (design/ui/databank, Screen and Where the data comes from): the entry page's picture: a unit's sprite
 * animated at 1:1 (2:1 under 40 px; frame 0, the heading straight down, of a unit that turns) from the unit atlas of
 * the level its numbers are of (loaded alone, no backdrop, a moment after the cursor rests, the last two kept), a
 * databank plate ({@code databank/<slug>}, 192×192) for a boss and a unit over 128 px, the silhouette ({@link
 * Silhouette}) of either below sensor L2; a gear item's large icon at 4×; a concept's sprite (decided as trivia,
 * pending the user's review). A unit whose frames are missing falls back to its intel portrait at 4×.
 */
final class EntryPictures implements Disposable {
    /** The cursor rests this long before a level's unit atlas loads (a held key runs through the list unhindered). */
    static final float LOAD_DELAY = 0.2f;
    /** The unit atlases kept loaded. */
    static final int KEPT = 2;
    /** The organic units' animation rate. */
    static final float FPS = 8;
    /** An icon's and a portrait's scale in the picture box. */
    static final int ICON_SCALE = 4;

    /**
     * The concepts' pictures (design/ui/databank, Concept entries), sprites the shared pages hold: the region and its
     * scale. Decided as trivia, pending the user's review: the captures the table names may replace them (step DB3).
     */
    static final Map<String, String> CONCEPT_PICTURES = Map.ofEntries(
            Map.entry("shield-and-armour", "icons/shield-mk-i-large"),
            Map.entry("power", "icons/generator-mk-i-spark-large"),
            Map.entry("intel-and-sensors", "icons/utility-sensor-suite-large"),
            Map.entry("weapon-traits", "icons/tesla-coil-pod-large"),
            Map.entry("layers", "intel/spine-turret"),
            Map.entry("specials", "icons/special-airstrike-large"),
            Map.entry("rook-and-escort", "icons/escort-rook-large"),
            Map.entry("hardened-targets", "intel/hive-node"),
            Map.entry("under-water", "intel/driftjelly"),
            Map.entry("ascendancy", "databank/ascendancy-cruiser"),
            Map.entry("vacuum", "intel/asteroid-mite"),
            Map.entry("enemy-shields", "icons/shield-reflex-shield-large"),
            Map.entry("mirrors", "intel/mirror-interceptor"),
            Map.entry("three-way-war", "intel/chimera"));

    private final Sprites sprites;
    private final FlashShader flash;
    private final Content content;
    private final Catalogue catalogue;
    private final Silhouette silhouette = new Silhouette();
    private final Deque<Integer> loaded = new ArrayDeque<>();

    /** What the box shows now. */
    private Object page;

    private Array<AtlasRegion> frames;
    private int phases = 1;
    private int base;
    private float scale = 1;
    private boolean dark;
    private float waiting;
    private boolean pending;
    private float elapsed;

    EntryPictures(Sprites sprites, FlashShader flash, Content content, Catalogue catalogue) {
        this.sprites = sprites;
        this.flash = flash;
        this.content = content;
        this.catalogue = catalogue;
    }

    /** Whether a concept's page has a 192×192 plate for its picture (the Ascendancy's cruiser). */
    static boolean conceptPlate(Pages.ConceptPage page) {
        return CONCEPT_PICTURES.getOrDefault(page.entry().slug(), "").startsWith("databank/");
    }

    /** The page whose picture the box shows: resolved now, or (a unit's sprite) once the cursor rests. */
    void show(Object shown) {
        if (shown == page) {
            return;
        }
        page = shown;
        frames = null;
        phases = 1;
        base = 0;
        scale = 1;
        dark = false;
        elapsed = 0;
        pending = false;
        switch (shown) {
            case Pages.EnemyPage enemy -> {
                dark = enemy.picture().silhouette();
                String plate = "databank/" + enemy.entry().slug();
                if (enemy.picture().plate() && sprites.has(plate)) {
                    single(plate, 1);
                } else {
                    pending = true;
                    waiting = LOAD_DELAY;
                }
            }
            case Pages.GearPage gear -> single("icons/" + iconName(gear) + "-large", ICON_SCALE);
            case Pages.ConceptPage concept -> {
                String region = CONCEPT_PICTURES.get(concept.entry().slug());
                if (region != null && sprites.has(region)) {
                    single(region, region.startsWith("databank/") ? 1 : ICON_SCALE);
                }
            }
            default -> {}
        }
    }

    private String iconName(Pages.GearPage gear) {
        Entry entry = gear.entry();
        if (entry.kind() == Entry.Kind.ESCORT) {
            return entry.slug().equals("rook") ? "escort-rook" : ItemIcons.BULWARK;
        }
        Catalogue.Item item =
                catalogue.item(entry.itemKind().orElseThrow(), entry.itemId().orElseThrow());
        return ItemIcons.iconName(item, catalogue);
    }

    private void single(String region, float at) {
        frames = new Array<>(new AtlasRegion[] {sprites.region(region)});
        scale = at;
    }

    /** Runs the animation and, once the cursor has rested, loads a unit's frames. */
    void update(float seconds) {
        elapsed += seconds;
        if (!pending) {
            return;
        }
        waiting -= seconds;
        if (waiting <= 0 && page instanceof Pages.EnemyPage enemy) {
            pending = false;
            resolve(enemy);
        }
    }

    /** A unit's sprite from the unit atlas of the level its numbers are of (or its first met), else its portrait. */
    private void resolve(Pages.EnemyPage enemy) {
        String slug = enemy.entry().slug();
        Optional<Array<AtlasRegion>> found = framesAt(enemy.lastMet(), slug);
        if (found.isEmpty() && enemy.firstMet() != enemy.lastMet()) {
            found = framesAt(enemy.firstMet(), slug);
        }
        if (found.isEmpty()) {
            String portrait = "intel/" + slug;
            if (sprites.has(portrait)) {
                single(portrait, ICON_SCALE);
            }
            return;
        }
        frames = found.get();
        EnemyData data = content.enemies().get(slug);
        Orientation orientation = data == null ? Orientation.FIXED : data.orientation();
        int headings = Math.max(1, orientation.headings());
        phases = Math.max(1, frames.size / headings);
        base = orientation == Orientation.TILT_30 ? headings / 2 * phases : 0;
        scale = enemy.picture().scale();
    }

    private Optional<Array<AtlasRegion>> framesAt(int level, String slug) {
        if (!loaded.contains(level)) {
            sprites.enterUnits(level);
            loaded.addLast(level);
            while (loaded.size() > KEPT) {
                sprites.leaveUnits(loaded.removeFirst());
            }
        }
        return sprites.levelFrames(level, slug);
    }

    /** The picture centred in the box of {@code size} px whose top-left is ({@code x}, {@code y}). */
    void draw(SpriteBatch batch, int x, int y, int size) {
        if (frames == null || frames.isEmpty()) {
            return;
        }
        int frame = Math.min(frames.size - 1, base + (int) (elapsed * FPS) % phases);
        AtlasRegion region = frames.get(frame);
        int inner = size - 4;
        float at = scale;
        int largest = Math.max(region.getRegionWidth(), region.getRegionHeight());
        if (largest * at > inner) {
            at = inner / (float) largest;
        }
        float width = region.getRegionWidth() * at;
        float height = region.getRegionHeight() * at;
        float left = Math.round(x + (size - width) / 2);
        float bottom = Math.round(PixelScreen.HEIGHT - y - size + (size - height) / 2);
        if (dark) {
            silhouette.draw(batch, flash, region, left, bottom, at);
        } else {
            batch.draw(region, left, bottom, width, height);
        }
    }

    @Override
    public void dispose() {
        while (!loaded.isEmpty()) {
            sprites.leaveUnits(loaded.removeFirst());
        }
    }
}
