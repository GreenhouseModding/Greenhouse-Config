package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

/**
 * A TOML value holding a long integer.
 *
 * @param value    the integer value.
 * @param comments the comments associated with this value.
 */
public record TomlInteger(long value, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(value);
    }

    @Override
    public TomlInteger withComment(String[] comments) {
        return new TomlInteger(value, comments);
    }
}
