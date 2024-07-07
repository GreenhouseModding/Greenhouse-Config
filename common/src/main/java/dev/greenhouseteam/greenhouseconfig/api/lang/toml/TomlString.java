package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML value holding a string.
 *
 * @param value    the string value.
 * @param comments the comments associated with this value.
 */
public record TomlString(String value, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlString(value, comments);
    }
}