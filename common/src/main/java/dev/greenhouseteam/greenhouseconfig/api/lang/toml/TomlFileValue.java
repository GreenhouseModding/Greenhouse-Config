package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record TomlFileValue(String[] path, TomlElement value, String[] comments) implements TomlFileElement {
    @Override
    public String[] getComments() {
        return comments;
    }
}
