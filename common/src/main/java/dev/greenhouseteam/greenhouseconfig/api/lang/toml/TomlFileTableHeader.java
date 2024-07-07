package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record TomlFileTableHeader(String[] path, boolean array, String[] comments) implements TomlFileElement {
    @Override
    public String[] getComments() {
        return comments;
    }
}
