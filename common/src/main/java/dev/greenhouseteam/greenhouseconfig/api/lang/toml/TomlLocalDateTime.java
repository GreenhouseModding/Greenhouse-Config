package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.time.LocalDateTime;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML value holding a local date-time.
 *
 * @param dateTime the local date-time value.
 * @param comments the comments associated with this value.
 */
public record TomlLocalDateTime(LocalDateTime dateTime, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public LocalDateTime getValue() {
        return dateTime;
    }

    @Override
    public String getStringValue() {
        return dateTime.toString();
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlLocalDateTime(dateTime, comments);
    }
}
