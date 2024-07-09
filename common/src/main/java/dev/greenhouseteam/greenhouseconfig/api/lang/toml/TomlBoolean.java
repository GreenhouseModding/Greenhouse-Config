package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.Arrays;

/**
 * A TOML value holding a boolean.
 *
 * @param value    the boolean value held.
 * @param comments the comments assocaited with this value.
 */
public record TomlBoolean(boolean value, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(value);
    }

    @Override
    public TomlBoolean withComment(String[] comments) {
        return new TomlBoolean(value, comments);
    }

    @Override
    public String toString() {
        return "TomlBoolean{" + value + ", " + Arrays.toString(comments) + "}";
    }
}
