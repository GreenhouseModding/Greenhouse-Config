package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.Arrays;
import java.util.Map;

/**
 * A TOML object, holding multiple named child elements.
 *
 * @param map      the map of string keys to TOML elements.
 * @param comments the comments associated with this value.
 */
public record TomlTable(Map<String, TomlElement> map, String[] comments) implements TomlElement {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public Map<String, TomlElement> getValue() {
        return map;
    }

    @Override
    public TomlTable withComment(String[] comments) {
        return new TomlTable(map, comments);
    }

    @Override
    public String toString() {
        return "TomlTable{" + map + ", " + Arrays.toString(comments) + "}";
    }
}
