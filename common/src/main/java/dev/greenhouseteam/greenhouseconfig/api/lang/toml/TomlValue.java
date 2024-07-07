package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

/**
 * A TOML tree leaf, holding only a single value.
 */
public sealed interface TomlValue extends TomlElement
    permits TomlBoolean, TomlFloat, TomlInteger, TomlLocalDate, TomlLocalDateTime, TomlLocalTime, TomlOffsetDateTime,
    TomlString {

    /**
     * {@return this value's value as a string}
     */
    String getStringValue();
}
