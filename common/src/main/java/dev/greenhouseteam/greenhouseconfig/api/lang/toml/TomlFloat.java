package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML value holding a floating-point double.
 * <p>
 * This is called <em>float</em> because that is how the spec refers to this type.
 *
 * @param value    the double value.
 * @param comments the comments associated with this value.
 */
public record TomlFloat(double value, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(value);
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlFloat(value, comments);
    }
}
