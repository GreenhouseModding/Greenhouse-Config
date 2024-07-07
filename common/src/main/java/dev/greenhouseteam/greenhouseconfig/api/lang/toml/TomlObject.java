package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.Map;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML object, holding multiple named child elements.
 *
 * @param map      the map of string keys to TOML elements.
 * @param comments the comments associated with this value.
 */
public record TomlObject(Map<String, TomlElement> map, String[] comments) implements TomlElement {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public Map<String, TomlElement> getValue() {
        return map;
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlObject(map, comments);
    }
}
