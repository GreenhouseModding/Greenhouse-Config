package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlElement;

public record TomlFileValue(String[] path, TomlElement value, String[] comments) implements TomlFileElement {
    @Override
    public String[] getComments() {
        return comments;
    }
}
