package vanguard.game.render;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The backdrop atlases, one per backdrop folder ({@code level-NN}, packed into {@code backdrop-level-NN.atlas}),
 * loaded only while a level that draws them runs: entering a level loads its own folder's atlas, a lookup of an image
 * in another folder (a level borrowing another level's images, {@code images: level-NN}) loads that folder's atlas
 * for the most recently entered level, and leaving a level frees every atlas no entered level holds any more. Before
 * the startup step all 35 levels' backdrops (66 pages, about 1 GB of textures) were loaded before the title screen.
 *
 * @param <A> the atlas (a texture atlas in the game, anything in a test)
 */
final class BackdropAtlases<A> {
    private final Function<String, A> load;
    private final Consumer<A> dispose;
    /** The loaded atlases by folder (null for a folder without one), each with how many holders hold it. */
    private final Map<String, Held<A>> loaded = new HashMap<>();
    /** The entered levels by number, each with its folders and how many screens entered it. */
    private final Map<Integer, Holder> levels = new LinkedHashMap<>();
    /** The folders looked up while no level was entered: held until {@link #dispose()}. */
    private final Holder outside = new Holder();
    /** The level entered last; another folder's images it looks up are held for it. */
    private int latest = -1;

    /**
     * @param load a folder's atlas ({@code level-NN}), or null when that folder has none
     * @param dispose frees an atlas
     */
    BackdropAtlases(Function<String, A> load, Consumer<A> dispose) {
        this.load = load;
        this.dispose = dispose;
    }

    /** The backdrop folder of level {@code number}: {@code level-NN}. */
    static String folder(int number) {
        return String.format(Locale.ROOT, "level-%02d", number);
    }

    /**
     * Loads level {@code number}'s own backdrop atlas until {@link #leave} with the same number; a level entered twice
     * (a retry starting before the last attempt's screen closes) keeps its atlases.
     */
    void enter(int number) {
        latest = number;
        Holder level = levels.get(number);
        if (level != null) {
            level.users++;
            return;
        }
        level = new Holder();
        levels.put(number, level);
        hold(level, folder(number));
    }

    /** Frees the atlases level {@code number} held once no screen of that level uses it and no other level holds them. */
    void leave(int number) {
        Holder level = levels.get(number);
        if (level == null || --level.users > 0) {
            return;
        }
        levels.remove(number);
        level.folders.forEach(this::release);
        if (latest == number) {
            latest = levels.keySet().stream().reduce((first, second) -> second).orElse(-1);
        }
    }

    /**
     * The atlas that holds the images of {@code name} ({@code level-NN/<id>}): its folder's atlas, loaded now for the
     * latest entered level if no level holds it yet; null when the name has no folder or the folder no atlas.
     */
    A atlas(String name) {
        int slash = name.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        String folder = name.substring(0, slash);
        Holder level = levels.getOrDefault(latest, outside);
        return hold(level, folder).atlas;
    }

    /** Whether the atlas of {@code folder} is loaded (for tests). */
    boolean loaded(String folder) {
        return loaded.containsKey(folder);
    }

    /** Frees every loaded atlas. */
    void dispose() {
        loaded.values().forEach(held -> {
            if (held.atlas != null) {
                dispose.accept(held.atlas);
            }
        });
        loaded.clear();
        levels.clear();
        outside.folders.clear();
        latest = -1;
    }

    private Held<A> hold(Holder holder, String folder) {
        // A folder without an atlas is remembered too, so it is looked for once; it holds nothing to free.
        Held<A> held = loaded.computeIfAbsent(folder, key -> new Held<>(load.apply(key)));
        if (holder.folders.add(folder)) {
            held.holders++;
        }
        return held;
    }

    private void release(String folder) {
        Held<A> held = loaded.get(folder);
        if (held != null && --held.holders == 0) {
            loaded.remove(folder);
            if (held.atlas != null) {
                dispose.accept(held.atlas);
            }
        }
    }

    /** A loaded atlas (null for a folder without one) and how many holders hold it. */
    private static final class Held<A> {
        final A atlas;
        int holders;

        Held(A atlas) {
            this.atlas = atlas;
        }
    }

    /** An entered level (or the outside of every level): the folders it holds and how many screens entered it. */
    private static final class Holder {
        final Set<String> folders = new LinkedHashSet<>();
        int users = 1;
    }
}
