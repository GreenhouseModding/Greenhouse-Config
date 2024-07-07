package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.time.LocalDate;

/**
 * A TOML value holding a local date.
 *
 * @param date     the local date.
 * @param comments the comments associated with this value.
 */
public record TomlLocalDate(LocalDate date, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public LocalDate getValue() {
        return date;
    }

    @Override
    public String getStringValue() {
        return date.toString();
    }

    @Override
    public TomlLocalDate withComment(String[] comments) {
        return new TomlLocalDate(date, comments);
    }
}
