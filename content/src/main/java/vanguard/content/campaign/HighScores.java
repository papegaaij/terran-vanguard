package vanguard.content.campaign;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import vanguard.content.Difficulty;

/**
 * The local high-score tables (design/systems/scoring, High scores): a top 10 per difficulty, each
 * entry a name, the score, the furthest level and the date, and the name entered last, which the
 * name entry offers again. Higher scores rank first; of two equal scores the earlier date ranks
 * first, and of two on the same day the one entered first. A table is a value: {@link #add}
 * returns a new one. The file is {@link ScoreFile}.
 */
public final class HighScores {
    /** The entries per difficulty. */
    public static final int SIZE = 10;

    public static final int MIN_NAME = 3;
    public static final int MAX_NAME = 10;

    /**
     * One entry.
     *
     * @param name 3–10 of A–Z, 0–9, space and {@code -}, upper case, no space at either end
     * @param score the campaign's score, more than 0
     * @param level the furthest level: the level that ended the campaign at a game over, the last won one
     *     at the campaign's end
     * @param date the day it was entered
     */
    public record Entry(String name, long score, int level, LocalDate date) {
        public Entry {
            if (!validName(name)) {
                throw new IllegalArgumentException("not a high-score name: '" + name + "'");
            }
            if (score <= 0) {
                throw new IllegalArgumentException("a high score is more than 0: " + score);
            }
            if (level < 1) {
                throw new IllegalArgumentException("no level " + level);
            }
        }
    }

    private static final HighScores EMPTY = new HighScores(new EnumMap<>(Difficulty.class), Optional.empty());

    private final Map<Difficulty, List<Entry>> tables;
    private final Optional<String> lastName;

    private HighScores(Map<Difficulty, List<Entry>> tables, Optional<String> lastName) {
        EnumMap<Difficulty, List<Entry>> copy = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            copy.put(difficulty, List.copyOf(tables.getOrDefault(difficulty, List.of())));
        }
        this.tables = Collections.unmodifiableMap(copy);
        this.lastName = lastName;
    }

    /** No entries and no last name: a first start, or a missing or unreadable file. */
    public static HighScores empty() {
        return EMPTY;
    }

    /**
     * Tables from stored entries: each table put in rank order (a stable sort, so equal entries keep
     * their stored order) and cut to {@link #SIZE}; a last name that is not a valid name is dropped.
     */
    public static HighScores of(Map<Difficulty, List<Entry>> entries, Optional<String> lastName) {
        EnumMap<Difficulty, List<Entry>> sorted = new EnumMap<>(Difficulty.class);
        for (Map.Entry<Difficulty, List<Entry>> table : entries.entrySet()) {
            List<Entry> list = new ArrayList<>(table.getValue());
            list.sort((a, b) -> a.score() != b.score()
                    ? Long.compare(b.score(), a.score())
                    : a.date().compareTo(b.date()));
            sorted.put(table.getKey(), list.subList(0, Math.min(SIZE, list.size())));
        }
        return new HighScores(sorted, lastName.filter(HighScores::validName));
    }

    /** A difficulty's table, best first, at most {@link #SIZE} entries. */
    public List<Entry> table(Difficulty difficulty) {
        return tables.get(difficulty);
    }

    /** The name entered last, which the name entry starts with. */
    public Optional<String> lastName() {
        return lastName;
    }

    /**
     * The place (0 = first) a score entered on {@code date} would take in the difficulty's table,
     * below every entry with the same score (those were entered earlier); empty when it does not make
     * the top 10 or is 0.
     */
    public OptionalInt rank(Difficulty difficulty, long score, LocalDate date) {
        if (score <= 0) {
            return OptionalInt.empty();
        }
        int above = 0;
        for (Entry entry : table(difficulty)) {
            if (entry.score() > score
                    || (entry.score() == score && !entry.date().isAfter(date))) {
                above++;
            }
        }
        return above < SIZE ? OptionalInt.of(above) : OptionalInt.empty();
    }

    /** Whether the score makes the difficulty's top 10 on {@code date}: the name entry is offered. */
    public boolean qualifies(Difficulty difficulty, long score, LocalDate date) {
        return rank(difficulty, score, date).isPresent();
    }

    /**
     * The tables with {@code entry} in its place in the difficulty's table (the last entry drops out
     * of a full table) and its name as the last name. The entry must {@link #qualifies qualify}.
     */
    public HighScores add(Difficulty difficulty, Entry entry) {
        int place = rank(difficulty, entry.score(), entry.date())
                .orElseThrow(() -> new IllegalArgumentException("not a top-10 score: " + entry.score()));
        EnumMap<Difficulty, List<Entry>> changed = new EnumMap<>(tables);
        List<Entry> list = new ArrayList<>(table(difficulty));
        list.add(place, entry);
        changed.put(difficulty, list.subList(0, Math.min(SIZE, list.size())));
        return new HighScores(changed, Optional.of(entry.name()));
    }

    /** Whether the character can be part of a name: A–Z, 0–9, space and {@code -} (lower case is raised). */
    public static boolean allowed(char c) {
        char upper = Character.toUpperCase(c);
        return (upper >= 'A' && upper <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '-';
    }

    /** Whether the name can be stored: 3–10 allowed characters in upper case, no space at either end. */
    public static boolean validName(String name) {
        if (name == null || name.length() < MIN_NAME || name.length() > MAX_NAME || !name.equals(name.strip())) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!allowed(c) || Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    /** The name as typed made storable: upper case, the spaces at either end removed (it may still be too short). */
    public static String normalise(String typed) {
        return typed.toUpperCase(Locale.ROOT).strip();
    }
}
