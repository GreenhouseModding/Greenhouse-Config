package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

sealed interface TomlFileElement permits TomlFileTableHeader, TomlFileValue {
    String[] getComments();
}
