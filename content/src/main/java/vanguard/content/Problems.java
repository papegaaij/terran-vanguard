package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The problems the content's checks find, each {@code design/<file>: <field>: <message>}, in the order found. */
final class Problems {
    private final Map<Object, String> paths;
    private final List<String> list = new ArrayList<>();

    /** {@code paths}: each parsed file's path under {@code design/}. */
    Problems(Map<Object, String> paths) {
        this.paths = paths;
    }

    void add(Object file, String field, String message) {
        list.add("design/" + paths.get(file) + ": " + field + ": " + message);
    }

    List<String> list() {
        return list;
    }
}
