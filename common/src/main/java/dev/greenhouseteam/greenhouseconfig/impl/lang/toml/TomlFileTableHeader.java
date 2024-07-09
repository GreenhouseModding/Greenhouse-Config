package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

public record TomlFileTableHeader(String[] path, boolean array, String[] comments) implements TomlFileElement {
    @Override
    public String[] getComments() {
        return comments;
    }
}
