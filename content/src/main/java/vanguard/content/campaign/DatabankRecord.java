package vanguard.content.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The databank's record in a save (design/ui/databank, The record in the save; format version 7, step DB1): the only
 * new state, everything else of the databank is derived from what the save already holds.
 *
 * @param met per unit or boss slug, the first level the campaign met it in and its kills of it (a boss's kill is its
 *     defeat)
 * @param read the databank entries opened at least once, by id ({@code mirror-interceptor}, {@code concept:shields});
 *     the {@code NEW} tag is a known entry not in it
 */
public record DatabankRecord(Map<String, Met> met, List<String> read) {
    /** A new campaign's: nothing met, nothing read. */
    public static final DatabankRecord EMPTY = new DatabankRecord(Map.of(), List.of());

    public DatabankRecord {
        met.keySet().forEach(slug -> {
            if (slug == null || slug.isBlank()) {
                throw new IllegalArgumentException("invalid databank: a blank unit slug");
            }
        });
        met = Collections.unmodifiableMap(new TreeMap<>(met));
        if (read.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("invalid databank: a blank entry id in read");
        }
        read = List.copyOf(new LinkedHashSet<>(read));
    }

    /**
     * A unit met: {@code first} the level it was first met in, {@code kills} the campaign's kills of it.
     */
    public record Met(int first, int kills) {
        public Met {
            if (first < 1 || first > 50 || kills < 0) {
                throw new IllegalArgumentException("invalid databank: first " + first + ", kills " + kills);
            }
        }
    }

    /** Whether the campaign has met {@code slug}. */
    public boolean met(String slug) {
        return met.containsKey(slug);
    }

    /** Whether the entry {@code id} has been opened. */
    public boolean wasRead(String id) {
        return read.contains(id);
    }

    /**
     * The record after an attempt at Level {@code level} (won, failed, restarted or aborted; a replay's too): every
     * unit {@code seen} on the screen and every unit of {@code kills} is met (a new one first met at {@code level}, an
     * earlier first level kept), and the kills add up.
     */
    public DatabankRecord attempt(int level, Collection<String> seen, Map<String, Integer> kills) {
        Map<String, Met> merged = new TreeMap<>(met);
        for (String slug : seen) {
            merged.merge(slug, new Met(level, 0), DatabankRecord::earlier);
        }
        kills.forEach((slug, count) -> {
            if (count < 0) {
                throw new IllegalArgumentException("negative kills of " + slug + ": " + count);
            }
            merged.merge(
                    slug,
                    new Met(level, count),
                    (old, now) -> new Met(Math.min(old.first(), now.first()), old.kills() + now.kills()));
        });
        return new DatabankRecord(merged, read);
    }

    private static Met earlier(Met old, Met now) {
        return old.first() <= now.first() ? old : new Met(now.first(), old.kills());
    }

    /** The record with the entry {@code id} opened. */
    public DatabankRecord opened(String id) {
        if (read.contains(id)) {
            return this;
        }
        List<String> more = new ArrayList<>(read);
        more.add(id);
        return new DatabankRecord(met, more);
    }
}
