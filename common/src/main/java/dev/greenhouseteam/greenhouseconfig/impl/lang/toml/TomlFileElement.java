package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

public sealed interface TomlFileElement permits TomlFileTableHeader, TomlFileValue {
    String[] getComments();
}
