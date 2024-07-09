package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.time.LocalTime;
import java.util.Arrays;

/**
 * A TOML value holding a local time.
 *
 * @param time     the local time value.
 * @param comments the comments associated with this value.
 */
public record TomlLocalTime(LocalTime time, String[] comments) implements TomlValue {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public LocalTime getValue() {
        return time;
    }

    @Override
    public String getStringValue() {
        return time.toString();
    }

    @Override
    public TomlLocalTime withComment(String[] comments) {
        return new TomlLocalTime(time, comments);
    }

    @Override
    public String toString() {
        return "TomlLocalTime{" + time + ", " + Arrays.toString(comments) + "}";
    }
}
