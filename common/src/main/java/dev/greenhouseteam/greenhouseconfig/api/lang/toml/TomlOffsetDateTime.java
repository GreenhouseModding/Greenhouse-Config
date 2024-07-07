package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.time.ZonedDateTime;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML value holding a zoned date time.
 *
 * @param dateTime the zoned date time value.
 * @param comments the comments associatd with this value.
 */
public record TomlOffsetDateTime(ZonedDateTime dateTime, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public ZonedDateTime getValue() {
        return dateTime;
    }

    @Override
    public String getStringValue() {
        return dateTime.toString();
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlOffsetDateTime(dateTime, comments);
    }
}
